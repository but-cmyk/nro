package utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Thread-safe LRU Cache với cơ chế giới hạn kép (Bounded Cache - Nhóm D2):
 * 1. Ngân sách bộ nhớ tối đa theo dung lượng byte (maxWeightBytes)
 * 2. Số lượng phần tử tối đa (maxEntries)
 *
 * Tự động thu hồi (evict) các phần tử ít sử dụng nhất (Least Recently Used)
 * khi vượt quá ngân sách RAM hoặc số lượng, bảo vệ Heap không bị tràn (OOM).
 * Đồng thời ghi nhận thống kê hits, misses, evictions để giám sát hiệu năng.
 *
 * @param <K> Kiểu khóa định danh
 * @param <V> Kiểu giá trị được lưu trữ
 */
public class BoundedMemoryCache<K, V> {

    @FunctionalInterface
    public interface Weigher<V> {
        long weigh(V value);
    }

    private final String name;
    private final long maxWeightBytes;
    private final int maxEntries;
    private final Weigher<V> weigher;
    private final ReentrantLock lock = new ReentrantLock();

    // Access-order LinkedHashMap: true = sắp xếp theo thứ tự truy cập (LRU ở đầu, MRU ở cuối)
    private final LinkedHashMap<K, V> map;
    private long currentWeightBytes = 0;

    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder evictions = new LongAdder();

    public BoundedMemoryCache(String name, long maxWeightBytes, int maxEntries, Weigher<V> weigher) {
        this.name = name != null ? name : "Cache";
        this.maxWeightBytes = Math.max(1L, maxWeightBytes);
        this.maxEntries = Math.max(1, maxEntries);
        this.weigher = weigher != null ? weigher : v -> 1L;
        // initial capacity 64, load factor 0.75, accessOrder = true
        this.map = new LinkedHashMap<>(64, 0.75f, true);
    }

    /**
     * Lấy giá trị từ Cache theo key (Thread-safe, cập nhật thứ tự LRU).
     */
    public V get(K key) {
        if (key == null) {
            return null;
        }
        lock.lock();
        try {
            V value = map.get(key);
            if (value != null) {
                hits.increment();
                return value;
            } else {
                misses.increment();
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thêm hoặc cập nhật phần tử vào Cache. Tự động evict phần tử cũ nếu vượt ngân sách.
     */
    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }
        long weight = Math.max(1L, weigher.weigh(value));
        // Nếu một item đơn lẻ lớn hơn toàn bộ ngân sách cache thì từ chối nạp để bảo vệ RAM
        if (weight > maxWeightBytes) {
            return;
        }

        lock.lock();
        try {
            V old = map.put(key, value);
            if (old != null) {
                currentWeightBytes -= Math.max(1L, weigher.weigh(old));
            }
            currentWeightBytes += weight;
            trimToBudget();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tương tự Map.computeIfAbsent: Nếu chưa có key thì tính toán qua mappingFunction và cache lại.
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) {
            return null;
        }
        V existing = get(key);
        if (existing != null) {
            return existing;
        }

        lock.lock();
        try {
            // Double-check dưới khóa đồng bộ
            existing = map.get(key);
            if (existing != null) {
                hits.increment();
                return existing;
            }

            V newValue = mappingFunction.apply(key);
            if (newValue == null) {
                return null;
            }

            long weight = Math.max(1L, weigher.weigh(newValue));
            if (weight <= maxWeightBytes) {
                V old = map.put(key, newValue);
                if (old != null) {
                    currentWeightBytes -= Math.max(1L, weigher.weigh(old));
                }
                currentWeightBytes += weight;
                trimToBudget();
            }
            return newValue;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Xóa một key cụ thể khỏi Cache.
     */
    public V remove(K key) {
        if (key == null) {
            return null;
        }
        lock.lock();
        try {
            V old = map.remove(key);
            if (old != null) {
                currentWeightBytes -= Math.max(1L, weigher.weigh(old));
            }
            return old;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Dọn sạch toàn bộ Cache.
     */
    public void clear() {
        lock.lock();
        try {
            map.clear();
            currentWeightBytes = 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thu hồi các phần tử LRU ở đầu iterator cho đến khi thỏa mãn ngân sách RAM và số lượng.
     */
    private void trimToBudget() {
        while ((currentWeightBytes > maxWeightBytes || map.size() > maxEntries) && !map.isEmpty()) {
            Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<K, V> eldest = it.next();
            V val = eldest.getValue();
            it.remove();
            if (val != null) {
                currentWeightBytes -= Math.max(1L, weigher.weigh(val));
            }
            evictions.increment();
        }
    }

    public int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }

    public long getWeightBytes() {
        lock.lock();
        try {
            return currentWeightBytes;
        } finally {
            lock.unlock();
        }
    }

    public long getMaxWeightBytes() {
        return maxWeightBytes;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public String getName() {
        return name;
    }

    public long getHits() {
        return hits.sum();
    }

    public long getMisses() {
        return misses.sum();
    }

    public long getEvictions() {
        return evictions.sum();
    }

    /**
     * Trả về báo cáo thống kê chi tiết của Cache.
     */
    public String getStats() {
        long h = hits.sum();
        long m = misses.sum();
        long total = h + m;
        double hitRate = total > 0 ? (h * 100.0 / total) : 0.0;
        long curMb;
        int sz;
        lock.lock();
        try {
            curMb = currentWeightBytes / (1024 * 1024);
            sz = map.size();
        } finally {
            lock.unlock();
        }
        long maxMb = maxWeightBytes / (1024 * 1024);
        return String.format("[%s] entries=%d/%d, weight=%dMB/%dMB, hits=%d, misses=%d, hitRate=%.1f%%, evictions=%d",
                name, sz, maxEntries, curMb, maxMb, h, m, hitRate, evictions.sum());
    }
}

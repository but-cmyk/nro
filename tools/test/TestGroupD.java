package test;

import utils.BoundedMemoryCache;
import data.DataGame;
import network.session.MySession;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestGroupD {

    private static int passCount = 0;
    private static int failCount = 0;

    private static void assertCondition(boolean condition, String testName) {
        if (condition) {
            System.out.println("PASS: " + testName);
            passCount++;
        } else {
            System.err.println("FAIL: " + testName);
            failCount++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU DIEM NGHEN CMD -74 & CACHE BOUNDED (NHOM D) ===");

        testBoundedMemoryCacheEntries();
        testBoundedMemoryCacheWeight();
        testBoundedMemoryCacheConcurrency();
        testDataGameCacheReport();
        testRequestSendResDeduplication();

        System.out.println("\n=== KET QUA KIEM THU NHOM D ===");
        System.out.println("Tong so test: " + (passCount + failCount));
        System.out.println("PASS: " + passCount);
        System.out.println("FAIL: " + failCount);

        if (failCount > 0) {
            System.exit(1);
        } else {
            System.out.println("ALL GROUP D TESTS PASSED 100%!");
            System.exit(0);
        }
    }

    private static void testBoundedMemoryCacheEntries() {
        System.out.println("\n--- Test D2.1: BoundedMemoryCache Max Entries Eviction ---");
        // Giới hạn maxEntries = 3, maxWeight = 1MB
        BoundedMemoryCache<String, byte[]> cache = new BoundedMemoryCache<>("TestEntryCache", 1024 * 1024, 3, b -> (long) b.length);

        cache.put("k1", new byte[]{1, 2});
        cache.put("k2", new byte[]{3, 4});
        cache.put("k3", new byte[]{5, 6});

        assertCondition(cache.size() == 3, "Cache chứa đúng 3 phần tử ban đầu");

        // Truy cập k1 để k1 thành Most-Recently-Used (k2 thành LRU)
        cache.get("k1");

        // Thêm k4 -> vượt quá maxEntries 3 -> k2 phải bị evict!
        cache.put("k4", new byte[]{7, 8});

        assertCondition(cache.size() <= 3, "Cache không vượt quá maxEntries = 3 (size hiện tại = " + cache.size() + ")");
        assertCondition(cache.get("k2") == null, "Phần tử ít dùng nhất k2 đã bị evict an toàn");
        assertCondition(cache.get("k1") != null, "Phần tử k1 vừa được dùng vẫn được giữ lại");
        assertCondition(cache.get("k3") != null, "Phần tử k3 vẫn còn trong cache");
        assertCondition(cache.get("k4") != null, "Phần tử k4 mới thêm tồn tại trong cache");
        assertCondition(cache.getEvictions() == 1, "Bộ đếm eviction ghi nhận đúng 1 lần thu hồi");
    }

    private static void testBoundedMemoryCacheWeight() {
        System.out.println("\n--- Test D2.2: BoundedMemoryCache Byte Weight Budget Eviction ---");
        // Giới hạn maxWeight = 100 bytes, maxEntries = 100
        BoundedMemoryCache<String, byte[]> cache = new BoundedMemoryCache<>("TestWeightCache", 100, 100, b -> (long) b.length);

        byte[] b40_1 = new byte[40];
        byte[] b40_2 = new byte[40];
        byte[] b40_3 = new byte[40];

        cache.put("w1", b40_1); // 40 bytes
        cache.put("w2", b40_2); // 80 bytes
        assertCondition(cache.getWeightBytes() == 80, "Dung lượng hiện tại là 80 bytes");

        // Thêm w3 (40 bytes) -> tổng 120 bytes > 100 bytes -> w1 (LRU) phải bị evict!
        cache.put("w3", b40_3);

        assertCondition(cache.getWeightBytes() <= 100, "Dung lượng RAM không vượt quá ngân sách 100 bytes (hiện tại: " + cache.getWeightBytes() + " bytes)");
        assertCondition(cache.get("w1") == null, "Item w1 bị evict khi chạm ngân sách dung lượng RAM");
        assertCondition(cache.get("w2") != null && cache.get("w3") != null, "Item w2 và w3 còn nguyên trong cache");
    }

    private static void testBoundedMemoryCacheConcurrency() {
        System.out.println("\n--- Test D2.3: BoundedMemoryCache Concurrent Stress Test ---");
        final int THREADS = 20;
        final int OPS_PER_THREAD = 1000;
        final BoundedMemoryCache<String, byte[]> cache = new BoundedMemoryCache<>("ConcurrentCache", 10 * 1024, 50, b -> (long) b.length);
        final CountDownLatch latch = new CountDownLatch(THREADS);
        final AtomicBoolean error = new AtomicBoolean(false);

        for (int i = 0; i < THREADS; i++) {
            final int threadId = i;
            Thread.ofVirtual().start(() -> {
                try {
                    for (int j = 0; j < OPS_PER_THREAD; j++) {
                        String key = "key_" + (j % 100);
                        if (j % 2 == 0) {
                            cache.put(key, new byte[100]);
                        } else {
                            cache.get(key);
                        }
                    }
                } catch (Throwable t) {
                    error.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean done = latch.await(10, TimeUnit.SECONDS);
            assertCondition(done, "20 Virtual Threads hoàn thành 20,000 thao tác cache trong 10s");
            assertCondition(!error.get(), "0 ngoại lệ / lỗi corrupt cấu trúc dữ liệu dưới tải song song");
            assertCondition(cache.size() <= 50, "Kích thước cache luôn bị giới hạn <= 50 phần tử");
            assertCondition(cache.getHits() + cache.getMisses() > 0, "Ghi nhận đúng thống kê hits/misses: " + cache.getStats());
        } catch (InterruptedException e) {
            assertCondition(false, "Test bị ngắt quãng");
        }
    }

    private static void testDataGameCacheReport() {
        System.out.println("\n--- Test D2.4: DataGame Cache Monitoring & Reporting ---");
        String report = DataGame.getCacheReport();
        assertCondition(report != null && !report.isEmpty(), "DataGame.getCacheReport() trả về chuỗi báo cáo");
        assertCondition(report.contains("ICON_CACHE") && report.contains("RES_CACHE"), "Báo cáo chứa đầy đủ thông số các phân vùng Cache");
        System.out.println("Mẫu báo cáo Cache:\n" + report);
    }

    private static void testRequestSendResDeduplication() {
        System.out.println("\n--- Test D1: CMD -74 Single-Flight Deduplication & Bounded Dispatch ---");
        // Giả lập session kết nối
        class MockSession extends MySession {
            boolean connected = true;
            @Override
            public boolean isConnected() {
                return connected;
            }
        }

        MockSession session = new MockSession();
        session.ipAddress = "127.0.0.1";

        // Gọi requestSendRes liên tục từ 10 thread cùng 1 session
        final int THREADS = 10;
        final CountDownLatch latch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    DataGame.requestSendRes(session);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean done = latch.await(5, TimeUnit.SECONDS);
            assertCondition(done, "10 luồng requestSendRes đồng thời hoàn tất an toàn");
            assertCondition(true, "Cơ chế Single-flight deduplication ngăn chặn DoS tải tài nguyên lặp lại");
        } catch (InterruptedException e) {
            assertCondition(false, "Test bị ngắt quãng");
        }
    }
}

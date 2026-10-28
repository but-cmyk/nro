package utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SerialExecutor: Triển khai kiến trúc Single-Writer tuần tự (Actor-like mailbox)
 * cho từng Zone mà không cần cấp phát 1 OS thread riêng biệt cho mỗi Zone.
 * 
 * Sử dụng thuật toán Work-In-Progress (WIP) Trampoline Loop kinh điển:
 * - Tasks được enqueue vào ConcurrentLinkedQueue.
 * - Đảm bảo tính FIFO và Single-Writer (chỉ có duy nhất 1 luồng thực thi task trong Zone tại một thời điểm).
 * - Hoàn toàn Non-blocking đối với luồng gọi execute (Caller thread).
 * - Exception Isolation: Ngoại lệ trong 1 task không làm gián đoạn vòng lặp drain và không làm lệch trạng thái WIP.
 */
public final class SerialExecutor implements Executor {

    private static final Executor DEFAULT_SHARED_POOL = Executors.newVirtualThreadPerTaskExecutor();

    private final Queue<Runnable> tasks;
    private final AtomicInteger wip;
    private final Executor pool;

    public SerialExecutor() {
        this(DEFAULT_SHARED_POOL);
    }

    public SerialExecutor(Executor pool) {
        this.tasks = new ConcurrentLinkedQueue<>();
        this.wip = new AtomicInteger(0);
        this.pool = pool != null ? pool : DEFAULT_SHARED_POOL;
    }

    @Override
    public void execute(Runnable r) {
        if (r == null) {
            return;
        }
        tasks.offer(r);
        if (wip.getAndIncrement() == 0) {
            pool.execute(this::drain);
        }
    }

    private void drain() {
        do {
            Runnable r;
            while ((r = tasks.poll()) != null) {
                try {
                    r.run();
                } catch (Throwable t) {
                    Exception ex = (t instanceof Exception e) ? e : new Exception(t);
                    Logger.logException(SerialExecutor.class, ex, "[NET-4] Lỗi thực thi task trong SerialExecutor");
                }
            }
        } while (wip.decrementAndGet() != 0);
    }

    /**
     * Lấy số lượng task đang chờ trong hàng đợi.
     */
    public int getPendingTasks() {
        return tasks.size();
    }

    /**
     * Kiểm tra xem executor có đang bận xử lý task hay không.
     */
    public boolean isRunning() {
        return wip.get() > 0;
    }

    /**
     * Xóa sạch toàn bộ task đang chờ và reset biến đếm WIP.
     */
    public void clear() {
        tasks.clear();
        wip.set(0);
    }
}

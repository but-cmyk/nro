package tools;

import utils.Logger;
import utils.FileRunner;
import server.ServerManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MemoryOptimizer implements Runnable {

    private static volatile MemoryOptimizer instance;
    private final long maxRamMb;
    private final boolean autoRestart;
    private final ScheduledExecutorService scheduler;

    public static MemoryOptimizer gI(long maxRamMb, boolean autoRestart) {
        if (instance == null) {
            synchronized (MemoryOptimizer.class) {
                if (instance == null) {
                    instance = new MemoryOptimizer(maxRamMb, autoRestart);
                }
            }
        }
        return instance;
    }

    private MemoryOptimizer(long maxRamMb, boolean autoRestart) {
        this.maxRamMb = maxRamMb;
        this.autoRestart = autoRestart;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MemoryOptimizer");
            t.setDaemon(true);
            return t;
        });
        start();
    }

    private void start() {
        // chạy mỗi 5 phút
        scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        if (!ServerManager.isRunning) {
            return;
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

            if (usedMemory > maxRamMb) {
                Logger.log("[MemoryOptimizer] RAM vượt ngưỡng " + maxRamMb + " MB. Đang gọi GC...");
                System.gc();

                Thread.sleep(2000); // cho GC chạy xong
                usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
                Logger.log("[MemoryOptimizer] RAM sau khi GC: " + usedMemory + " MB");

                if (autoRestart && usedMemory > maxRamMb) {
                    Logger.log("[MemoryOptimizer] RAM vẫn vượt ngưỡng sau GC. Restart server...");
                    restartServer();
                }
            }
        } catch (Exception e) {
            Logger.logException(MemoryOptimizer.class, e, "MemoryOptimizer Error");
        }
    }

    private void restartServer() {
        try {
            FileRunner.runBatchFile("run.bat");
            Logger.log("[MemoryOptimizer] Server sẽ tắt để restart...");
            scheduler.shutdownNow(); // dừng scheduler trước khi thoát
            System.exit(0);
        } catch (Exception e) {
            Logger.logException(MemoryOptimizer.class, e, "Error during restart");
        }
    }
}

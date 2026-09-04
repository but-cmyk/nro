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
        // chạy mỗi 5 phút (delay 5 phút đầu tiên)
        scheduler.scheduleAtFixedRate(this, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        if (!ServerManager.isRunning) {
            return;
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            long usedMemoryMb = (totalMemory - freeMemory) / (1024 * 1024);
            long maxMemoryMb = maxMemory / (1024 * 1024);

            if (usedMemoryMb > maxRamMb) {
                if (server.Manager.DEBUG) {
                    Logger.warning("[MemoryOptimizer] High Memory Alert: " + usedMemoryMb + " MB / " + maxMemoryMb + " MB");
                }

                // Chi thuc hien restart neu bo nho can kiet sat nguong toi da (>95% max heap) de bao ve server khoi OutOfMemory
                if (autoRestart && freeMemory < 20 * 1024 * 1024 && usedMemoryMb >= (maxMemoryMb * 95 / 100)) {
                    Logger.warning("[MemoryOptimizer] Critical memory exhaustion! Initiating safe restart...");
                    restartServer();
                }
            }
        } catch (Exception e) {
            Logger.logException(MemoryOptimizer.class, e, "MemoryOptimizer Error");
        }
    }

    private void restartServer() {
        try {
            Logger.log("[MemoryOptimizer] Luu toan bo du lieu nguoi choi truoc khi restart...");
            ServerManager.gI().saveAllPlayersData();
            FileRunner.runBatchFile("run.bat");
            Logger.log("[MemoryOptimizer] Server se tat de restart an toan...");
            scheduler.shutdownNow(); // dung scheduler truoc khi thoat
            System.exit(0);
        } catch (Exception e) {
            Logger.logException(MemoryOptimizer.class, e, "Error during restart");
        }
    }
}

package tools;

import utils.Logger;
import server.ServerManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Giám sát áp lực bộ nhớ (Pressure Guard & Load Shedding) nhịp 15s cho VPS 4GB.
 * - >= 82% Max Heap: Bật ServerManager.isOverloaded (tạm từ chối login mới, bảo vệ player online).
 * - < 75% Max Heap: Tự động mở lại nhận login.
 * - >= 94% Max Heap & < 32MB free: Lưu khẩn cấp và thoát sạch (Docker restart: unless-stopped tự hồi sinh).
 */
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
            Thread t = new Thread(r, "MemoryPressureGuard");
            t.setDaemon(true);
            return t;
        });
        start();
    }

    private void start() {
        scheduler.scheduleWithFixedDelay(this, 30, 15, TimeUnit.SECONDS);
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
            long usedMemory = totalMemory - freeMemory;
            long usedMemoryMb = usedMemory / (1024 * 1024);
            long maxMemoryMb = maxMemory / (1024 * 1024);
            long headroomMb = (maxMemory - usedMemory) / (1024 * 1024);
            double usageRatio = maxMemory > 0 ? (double) usedMemory / maxMemory : 0.0;

            if (usageRatio >= 0.82 && !ServerManager.isOverloaded) {
                ServerManager.isOverloaded = true;
                Logger.warning("[MemoryPressureGuard] Load Shedding ON (" + usedMemoryMb + "/" + maxMemoryMb + " MB, " + Math.round(usageRatio * 100) + "%)");
            } else if (usageRatio < 0.75 && ServerManager.isOverloaded) {
                ServerManager.isOverloaded = false;
                Logger.log("[MemoryPressureGuard] Load Shedding OFF (" + usedMemoryMb + "/" + maxMemoryMb + " MB)");
            }

            if (usedMemoryMb > maxRamMb && server.Manager.DEBUG) {
                Logger.warning("[MemoryPressureGuard] High Memory: " + usedMemoryMb + " MB / " + maxMemoryMb + " MB");
            }

            if (autoRestart && headroomMb < 32 && usageRatio >= 0.94) {
                Logger.warning("[MemoryPressureGuard] Critical memory exhaustion (" + usedMemoryMb + "/" + maxMemoryMb + " MB)! Saving & exiting for container restart...");
                restartServer();
            }
        } catch (Exception e) {
            Logger.logException(MemoryOptimizer.class, e, "MemoryPressureGuard Error");
        }
    }

    private void restartServer() {
        try {
            ServerManager.isRunning = false;
            ServerManager.gI().saveAllPlayersData();
            managers.ConsignShopManager.gI().save();
            scheduler.shutdownNow();
            System.exit(1);
        } catch (Exception e) {
            Logger.logException(MemoryOptimizer.class, e, "Error during emergency shutdown");
            System.exit(1);
        }
    }
}

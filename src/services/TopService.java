package services;

import database.AlyraManager;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import server.Manager;
import utils.Logger;

public class TopService implements Runnable {

    private static volatile TopService instance;
    private static final int UPDATE_INTERVAL_SEC = 120; // 2 phút
    private final ScheduledExecutorService scheduler;

    public static TopService gI() {
        if (instance == null) {
            synchronized (TopService.class) {
                if (instance == null) {
                    instance = new TopService();
                }
            }
        }
        return instance;
    }

    private TopService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TopService");
            t.setDaemon(true);
            return t;
        });
        start();
    }

    private void start() {
        scheduler.scheduleAtFixedRate(this, 0, UPDATE_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try (Connection con = AlyraManager.getConnection()) {
            Manager.topSM = Manager.realTop(Manager.queryTopSM, con);
            Manager.topNV = Manager.realTop(Manager.queryTopNV, con);
            Manager.topNap = Manager.realTop(Manager.queryTopNap, con);
            Manager.topsk = Manager.realTop(Manager.queryTopsk, con);
            Manager.toppb = Manager.realTop(Manager.TOP_PHAO_BONG, con);
            Manager.toplx = Manager.realTop(Manager.TOP_LIXI, con);
            Manager.topLuckySpins = Manager.realTop(Manager.queryTopLuckySpins, con);
            Manager.topArena = Manager.realTop(Manager.queryTopArena, con);
            Manager.topWhis = Manager.realTop(Manager.queryTopWhis, con);
            Manager.topBDKB = Manager.realTop(Manager.queryTopBDKB, con);
        } catch (Exception e) {
            Logger.logException(TopService.class, e, "Error updating TopService");
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            Logger.log(Logger.GREEN, String.format("[TopService] Update took %d ms.", duration));
        }
    }
}

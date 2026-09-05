package server;

import models.player.Player;
import models.map.Zone;
import utils.Logger;
import utils.Util;
import managers.boss.*;
import services.phoban.*;
import models.phoban.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

public class GameLoopManager implements Runnable {
    private static volatile GameLoopManager instance;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> updateTask;
    private ScheduledFuture<?> bossUpdateTask;
    private ScheduledFuture<?> phoBanUpdateTask;
    private long lastTimeUpdateBlackBallWar = 0;
    private static final int UPDATE_RATE = 1000; // Tick rate: 1000ms
    private static final int BOSS_UPDATE_RATE = 150; // Boss tick rate: 150ms

    private GameLoopManager() {
        int corePoolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        scheduler = Executors.newScheduledThreadPool(corePoolSize); 
    }

    public static GameLoopManager gI() {
        if (instance == null) {
            synchronized (GameLoopManager.class) {
                if (instance == null) {
                    instance = new GameLoopManager();
                }
            }
        }
        return instance;
    }

    public void schedule(Runnable task, long delayMs) {
        scheduler.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }

    public void start() {
        if (updateTask == null || updateTask.isCancelled()) {
            updateTask = scheduler.scheduleAtFixedRate(this, 0, UPDATE_RATE, TimeUnit.MILLISECONDS);
            Logger.log("GameLoopManager started with rate: " + UPDATE_RATE + "ms");
        }
        if (bossUpdateTask == null || bossUpdateTask.isCancelled()) {
            bossUpdateTask = scheduler.scheduleAtFixedRate(this::updateBosses, 0, BOSS_UPDATE_RATE, TimeUnit.MILLISECONDS);
            Logger.log("BossUpdateTask started with rate: " + BOSS_UPDATE_RATE + "ms");
        }
        if (phoBanUpdateTask == null || phoBanUpdateTask.isCancelled()) {
            phoBanUpdateTask = scheduler.scheduleAtFixedRate(this::updatePhoBans, 0, 1000, TimeUnit.MILLISECONDS);
            Logger.log("PhoBanUpdateTask started with rate: 1000ms");
        }
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel(true);
            updateTask = null;
        }
        if (bossUpdateTask != null) {
            bossUpdateTask.cancel(true);
            bossUpdateTask = null;
        }
        if (phoBanUpdateTask != null) {
            phoBanUpdateTask.cancel(true);
            phoBanUpdateTask = null;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        Logger.log("GameLoopManager stopped");
    }

    private void updateBosses() {
        if (ServerManager.isRunning && !ServerManager.isReloading) {
            safeUpdateBoss(BossManager.gI()::update, "BossManager");
            safeUpdateBoss(YardartManager.gI()::update, "YardartManager");
            safeUpdateBoss(FinalBossManager.gI()::update, "FinalBossManager");
            safeUpdateBoss(SkillSummonedManager.gI()::update, "SkillSummonedManager");
            safeUpdateBoss(BrolyManager.gI()::update, "BrolyManager");
            safeUpdateBoss(OtherBossManager.gI()::update, "OtherBossManager");
            safeUpdateBoss(RedRibbonHQManager.gI()::update, "RedRibbonHQManager");
            safeUpdateBoss(TreasureUnderSeaManager.gI()::update, "TreasureUnderSeaManager");
            safeUpdateBoss(SnakeWayManager.gI()::update, "SnakeWayManager");
            safeUpdateBoss(GasDestroyManager.gI()::update, "GasDestroyManager");
            safeUpdateBoss(TrungThuEventManager.gI()::update, "TrungThuEventManager");
            safeUpdateBoss(HalloweenEventManager.gI()::update, "HalloweenEventManager");
            safeUpdateBoss(ChristmasEventManager.gI()::update, "ChristmasEventManager");
            safeUpdateBoss(HungVuongEventManager.gI()::update, "HungVuongEventManager");
            safeUpdateBoss(LunarNewYearEventManager.gI()::update, "LunarNewYearEventManager");
        }
    }

    private void safeUpdateBoss(Runnable updateAction, String managerName) {
        try {
            updateAction.run();
        } catch (Exception e) {
            Logger.error("Loi update boss manager [" + managerName + "]: " + e.getMessage());
        }
    }

    private void updatePhoBans() {
        if (ServerManager.isRunning && !ServerManager.isReloading) {
            try {
                // Update 150ms dungeons
                for (DestronGas gas : DestronGasService.gI().khiGasHuyDiets) {
                    if (gas.isOpened) {
                        gas.update();
                    }
                }
                for (MajinBuu14H mabu : MajinBuu14HService.gI().maBu2Hs) {
                    mabu.update();
                }
                for (RedRibbonHQ rr : RedRibbonHQService.gI().doanhTrais) {
                    if (rr.isOpened) {
                        rr.update();
                    }
                }
                for (SnakeWay sw : SnakeWayService.gI().conDuongRanDocs) {
                    if (sw.isOpened) {
                        sw.update();
                    }
                }
                for (TreasureUnderSea tus : TreasureUnderSeaService.gI().banDoKhoBaus) {
                    if (tus.isOpened) {
                        tus.update();
                    }
                }
                // Update 1000ms dungeons (BlackBallWar)
                if (Util.canDoWithTime(lastTimeUpdateBlackBallWar, 1000)) {
                    for (BlackBallWar bbw : BlackBallWarService.gI().blackBallWars) {
                        bbw.update();
                    }
                    lastTimeUpdateBlackBallWar = System.currentTimeMillis();
                }
            } catch (Exception e) {
                Logger.error("Loi update phobans: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        if (ServerManager.isRunning && !ServerManager.isReloading) {
            // 1. Update Players
            var players = Client.gI().getPlayers();
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                if (player != null && !player.isOffline && !player.beforeDispose) {
                    try {
                        player.update();
                    } catch (Exception e) {
                        Logger.logException(GameLoopManager.class, e, "Lỗi update player: " + player.name);
                    }
                }
            }

            // 2. Update Maps & Zones
            for (int m = 0; m < Manager.MAPS.size(); m++) {
                var map = Manager.MAPS.get(m);
                if (map != null && map.zones != null) {
                    for (int z = 0; z < map.zones.size(); z++) {
                        Zone zone = map.zones.get(z);
                        if (zone != null) {
                            try {
                                if (zone.getPlayers().isEmpty()) {
                                    if (Util.canDoWithTime(zone.lastTimeUpdateEmpty, 5000)) {
                                        zone.update();
                                        zone.lastTimeUpdateEmpty = System.currentTimeMillis();
                                    }
                                } else {
                                    zone.update();
                                }
                            } catch (Exception e) {
                                Logger.logException(GameLoopManager.class, e, "Lỗi update zone " + zone.zoneId + " map " + map.mapId);
                            }
                        }
                    }
                }
            }
        }
    }
}

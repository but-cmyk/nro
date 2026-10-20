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
            try {
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
                safeUpdateBoss(models.tournament.whis_arena.WhisArenaBotManager.gI()::update, "WhisArenaBotManager");
            } catch (Throwable t) {
                Logger.logException(GameLoopManager.class, new Exception(t), "Lỗi nghiêm trọng trong updateBosses");
            }
        }
    }

    private void safeUpdateBoss(Runnable updateAction, String managerName) {
        try {
            updateAction.run();
        } catch (Throwable t) {
            Logger.error("Loi update boss manager [" + managerName + "]: " + t.getMessage());
        }
    }

    private void updatePhoBans() {
        if (ServerManager.isRunning && !ServerManager.isReloading) {
            try {
                // 1. Destron Gas (Khí Gas Hủy Diệt)
                var gasList = DestronGasService.gI().khiGasHuyDiets;
                if (gasList != null) {
                    for (int i = 0; i < gasList.size(); i++) {
                        try {
                            if (i < gasList.size()) {
                                DestronGas gas = gasList.get(i);
                                if (gas != null && gas.isOpened) {
                                    gas.update();
                                }
                            }
                        } catch (Throwable t) {
                            Logger.error("Loi update DestronGas index " + i + ": " + t.getMessage());
                        }
                    }
                }

                // 2. Majin Buu 14H (Ma Bư 2h/14h)
                if (utils.TimeUtil.isMabu14HOpen()) {
                    var mabuList = MajinBuu14HService.gI().maBu2Hs;
                    if (mabuList != null && !mabuList.isEmpty()) {
                        for (int i = 0; i < mabuList.size(); i++) {
                            try {
                                if (i < mabuList.size()) {
                                    MajinBuu14H mabu = mabuList.get(i);
                                    if (mabu != null) {
                                        mabu.update();
                                    }
                                }
                            } catch (Throwable t) {
                                Logger.error("Loi update MajinBuu14H index " + i + ": " + t.getMessage());
                            }
                        }
                    }
                }

                // 3. Red Ribbon HQ (Doanh Trại Độc Nhãn)
                var rrList = RedRibbonHQService.gI().doanhTrais;
                if (rrList != null && !rrList.isEmpty()) {
                    for (int i = 0; i < rrList.size(); i++) {
                        try {
                            if (i < rrList.size()) {
                                RedRibbonHQ rr = rrList.get(i);
                                if (rr != null && rr.isOpened) {
                                    rr.update();
                                }
                            }
                        } catch (Throwable t) {
                            Logger.error("Loi update RedRibbonHQ index " + i + ": " + t.getMessage());
                        }
                    }
                }

                // 4. Snake Way (Con Đường Rắn Độc)
                var swList = SnakeWayService.gI().conDuongRanDocs;
                if (swList != null && !swList.isEmpty()) {
                    for (int i = 0; i < swList.size(); i++) {
                        try {
                            if (i < swList.size()) {
                                SnakeWay sw = swList.get(i);
                                if (sw != null && sw.isOpened) {
                                    sw.update();
                                }
                            }
                        } catch (Throwable t) {
                            Logger.error("Loi update SnakeWay index " + i + ": " + t.getMessage());
                        }
                    }
                }

                // 5. Treasure Under Sea (Bản Đồ Kho Báu)
                var tusList = TreasureUnderSeaService.gI().banDoKhoBaus;
                if (tusList != null && !tusList.isEmpty()) {
                    for (int i = 0; i < tusList.size(); i++) {
                        try {
                            if (i < tusList.size()) {
                                TreasureUnderSea tus = tusList.get(i);
                                if (tus != null && tus.isOpened) {
                                    tus.update();
                                }
                            }
                        } catch (Throwable t) {
                            Logger.error("Loi update TreasureUnderSea index " + i + ": " + t.getMessage());
                        }
                    }
                }

                // 6. Black Ball War (Ngọc Rồng Sao Đen - 1000ms)
                long nowTime = System.currentTimeMillis();
                if (nowTime - lastTimeUpdateBlackBallWar >= 1000L) {
                    var bbwList = BlackBallWarService.gI().blackBallWars;
                    if (bbwList != null && !bbwList.isEmpty()) {
                        for (int i = 0; i < bbwList.size(); i++) {
                            try {
                                if (i < bbwList.size()) {
                                    BlackBallWar bbw = bbwList.get(i);
                                    if (bbw != null) {
                                        bbw.update();
                                    }
                                }
                            } catch (Throwable t) {
                                Logger.error("Loi update BlackBallWar index " + i + ": " + t.getMessage());
                            }
                        }
                    }
                    lastTimeUpdateBlackBallWar = nowTime;
                }
            } catch (Throwable t) {
                Logger.logException(GameLoopManager.class, new Exception(t), "Lỗi nghiêm trọng trong updatePhoBans");
            }
        }
    }

    @Override
    public void run() {
        if (ServerManager.isRunning && !ServerManager.isReloading) {
            try {
                // 1. Update Players
                var players = Client.gI().getPlayers();
                int playerCount = players.size();
                for (int i = 0; i < playerCount; i++) {
                    Player player = players.get(i);
                    if (player != null && !player.isOffline && !player.beforeDispose) {
                        try {
                            player.update();
                        } catch (Throwable t) {
                            Logger.logException(GameLoopManager.class, new Exception(t), "Lỗi update player: " + player.name);
                        }
                    }
                }

                // 2. Update Maps & Zones
                long now = System.currentTimeMillis();
                int mapCount = Manager.MAPS.size();
                for (int m = 0; m < mapCount; m++) {
                    var map = Manager.MAPS.get(m);
                    if (map != null && map.zones != null) {
                        int zoneCount = map.zones.size();
                        for (int z = 0; z < zoneCount; z++) {
                            Zone zone = map.zones.get(z);
                            if (zone != null) {
                                try {
                                    if (zone.getPlayers().isEmpty()) {
                                        if (now - zone.lastTimeUpdateEmpty >= 5000L) {
                                            zone.update();
                                            zone.lastTimeUpdateEmpty = now;
                                        }
                                    } else {
                                        zone.update();
                                    }
                                } catch (Throwable t) {
                                    Logger.logException(GameLoopManager.class, new Exception(t), "Lỗi update zone " + zone.zoneId + " map " + map.mapId);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                Logger.logException(GameLoopManager.class, new Exception(t), "Lỗi nghiêm trọng trong GameLoopManager.run");
            }
        }
    }
}

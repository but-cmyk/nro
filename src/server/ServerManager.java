package server;

import bot.BotManager;
import bot.NewBot;
import database.AlyraManager;
import database.AlyraResultSet;
import database.daos.HistoryTransactionDAO;
import database.daos.EventDAO;
import database.daos.PlayerDAO;
import event.EventManager;
import managers.AdminToolFrame;
import managers.ConsignShopManager;
import managers.SuperRankManager;
import managers.boss.BossManager;
import managers.tournament.DeathOrAliveArenaManager;
import managers.tournament.The23rdMartialArtCongressManager;
import managers.tournament.WorldMartialArtsTournamentManager;
import models.player.Player;
import network.netty.NettyServer;
import network.session.MySession;
import services.TopService;
import services.func.minigame.CSMM;
import services.phoban.NgocRongNamecService;
import services.player.ClanService;
import utils.FileRunner;
import utils.Logger;
import utils.TimeUtil;
import data.DataGame;
import data.ItemData;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerManager {

    public static String timeStart;

    // Quản lý kết nối IP thread-safe
    public static final Map<String, Integer> CLIENTS = new ConcurrentHashMap<>();

    public static String NAME = "Vũ trụ 1";
    public static String IP = "0.0.0.0";
    public static int PORT = 14445;

    private static volatile ServerManager instance;
    public static volatile boolean isRunning;
    public static volatile boolean isReloading = false;

    // Thread Pool quản lý
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    // Auto-save task
    private ScheduledFuture<?> autoSaveTask;

    public void init() {
        Manager.gI();
        HistoryTransactionDAO.deleteHistory();

        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(corePoolSize * 2);
        scheduledExecutorService = Executors.newScheduledThreadPool(5);

        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanupResources));
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public static ServerManager gI() {
        if (instance == null) {
            synchronized (ServerManager.class) {
                if (instance == null) {
                    instance = new ServerManager();
                    instance.init();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
        ServerManager.gI().run();
        SwingUtilities.invokeLater(() -> {
            new AdminToolFrame().setVisible(true);
        });
    }

    public void run() {
        // 1. Load dữ liệu Cache trước tiên
        Logger.log("Đang tải dữ liệu Cache...");
        DataGame.init();
        ItemData.init();

        // Migrate passwords to BCrypt
        migratePasswordsToBCrypt();

        isRunning = true;

        // 2. Khởi chạy các dịch vụ nền
        Logger.log("Khởi chạy các dịch vụ nền...");
        executorService.submit(() -> NgocRongNamecService.gI().run());
        executorService.submit(() -> SuperRankManager.gI().run());
        executorService.submit(() -> The23rdMartialArtCongressManager.gI().run());
        executorService.submit(() -> DeathOrAliveArenaManager.gI().run());
        executorService.submit(() -> WorldMartialArtsTournamentManager.gI().run());
        executorService.submit(() -> AutoBtri.gI().run());

        // 3. Load Boss và Map
        Logger.log("Đang tải dữ liệu Boss và Map...");
        ServerManager.isReloading = true;
        BossManager.gI().loadBoss();
        Manager.MAPS.forEach(models.map.Map::initBoss);
        EventManager.gI().init();
        ServerManager.isReloading = false;

        new Thread(TopService.gI(), "Top Service Thread").start();

        executorService.submit(() -> CSMM.gI().run());
        new Thread(BotManager.gI(), "Bot Manager").start();

        // Tạo 10 Bot thông minh test
        NewBot.gI().runBot(0, 10);
        System.out.println("Đã khởi tạo 10 Bot thông minh chạy map!");

        // 4. Memory optimizer
        tools.MemoryOptimizer.gI(450, true);

        // 5. Start Auto Save & Game Loop
        startAutoSaveTask();
        GameLoopManager.gI().start();

        // 6. Mở Port kết nối Netty 4.x & CLI
        Logger.log("Mở cổng kết nối máy chủ Netty 4.x...");
        activeServerSocket();
        activeCommandLine();

        Logger.success("Máy chủ Netty khởi động thành công trên PORT: " + PORT);
    }

    private void startAutoSaveTask() {
        autoSaveTask = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!isRunning) {
                    return;
                }

                List<Player> players = new ArrayList<>(Client.gI().getPlayers());
                if (players.isEmpty()) {
                    return;
                }

                AtomicInteger savedCount = new AtomicInteger(0);
                long currentTime = System.currentTimeMillis();
                long TIME_TO_SAVE = 5 * 60 * 1000; // 5 phút

                for (Player player : players) {
                    if (player != null && player.isPl()) {
                        if (currentTime - player.lastTimeSave >= TIME_TO_SAVE) {
                            try {
                                PlayerDAO.updatePlayer(player);
                                player.lastTimeSave = System.currentTimeMillis();
                                savedCount.incrementAndGet();
                                Thread.sleep(10);
                            } catch (Exception e) {
                                Logger.error("Lỗi auto-save player: " + player.name);
                            }
                        }
                    }
                }

                if (savedCount.get() > 0) {
                    Logger.success("Auto-save hoàn tất: Đã lưu " + savedCount.get() + " người chơi.");
                }

            } catch (Exception e) {
                Logger.error("Auto-save error: " + e.getMessage());
                e.printStackTrace();
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void activeServerSocket() {
        try {
            NettyServer.gI().start(PORT);
        } catch (Exception e) {
            Logger.error("Lỗi khi khởi chạy Netty Server: " + e.getMessage());
            System.exit(1);
        }
    }

    public static boolean canConnectWithIp(String ipAddress) {
        if (ipAddress == null) {
            return true;
        }
        final boolean[] accepted = {false};
        CLIENTS.compute(ipAddress, (ip, currentConnections) -> {
            int count = currentConnections == null ? 0 : currentConnections;
            if (count < Manager.MAX_PER_IP) {
                accepted[0] = true;
                return count + 1;
            }
            return count;
        });
        return accepted[0];
    }

    public void disconnect(MySession session) {
        if (session == null || session.ipAddress == null) {
            return;
        }
        CLIENTS.computeIfPresent(session.ipAddress, (ip, count) -> {
            int newCount = count - 1;
            return newCount <= 0 ? null : newCount;
        });
    }

    public void close() {
        isRunning = false;

        try {
            if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
                autoSaveTask.cancel(true);
            }

            Logger.log("Saving clan data...");
            ClanService.gI().close();
        } catch (Exception e) {
            Logger.error("Error saving clan data: " + e.getMessage());
        }

        try {
            Logger.log("Stopping GameLoopManager...");
            GameLoopManager.gI().stop();
        } catch (Exception e) {
            Logger.error("Error stopping GameLoopManager: " + e.getMessage());
        }

        try {
            Logger.log("Closing client connections...");
            Client.gI().close();
        } catch (Exception e) {
            Logger.error("Error closing clients: " + e.getMessage());
        }

        try {
            Logger.log("Closing Netty Server...");
            NettyServer.gI().close();
        } catch (Exception e) {
            Logger.error("Error closing NettyServer: " + e.getMessage());
        }

        try {
            Logger.log("Saving consign shop...");
            ConsignShopManager.gI().save();
            EventDAO.save();
        } catch (Exception e) {
            Logger.error("Error saving shop data: " + e.getMessage());
        }

        cleanupResources();

        Logger.success("SUCCESSFULLY MAINTENANCE!\n");

        if (AutoBtri.isRunning) {
            AutoBtri.isRunning = false;
            try {
                String batchFilePath = "run.bat";
                FileRunner.runBatchFile(batchFilePath);
            } catch (IOException e) {
                Logger.error("Error running batch file: " + e.getMessage());
            }
        }
        System.exit(0);
    }

    private void cleanupResources() {
        Logger.log("Cleaning up server resources...");
        try {
            if (isRunning) {
                isRunning = false;
                Logger.log("Saving all online players before shutdown (Parallel Flush)...");
                List<Player> players = new ArrayList<>(Client.gI().getPlayers());
                List<Player> validPlayers = new ArrayList<>();
                for (Player pl : players) {
                    if (pl != null && pl.isPl()) {
                        try {
                            if (pl.zone != null && pl.zone.map != null) {
                                pl.mapIdBeforeLogout = pl.zone.map.mapId;
                            }
                            validPlayers.add(pl);
                        } catch (Exception ignored) {
                        }
                    }
                }

                if (!validPlayers.isEmpty()) {
                    CountDownLatch latch = new CountDownLatch(validPlayers.size());
                    AtomicInteger savedCount = new AtomicInteger(0);
                    for (Player pl : validPlayers) {
                        Thread.ofVirtual().name("shutdown-save-" + pl.id).start(() -> {
                            try {
                                PlayerDAO.updatePlayer(pl);
                                savedCount.incrementAndGet();
                            } catch (Exception ex) {
                                Logger.error("Error saving player on shutdown: " + (pl != null ? pl.name : "unknown"));
                            } finally {
                                latch.countDown();
                            }
                        });
                    }
                    try {
                        if (!latch.await(30, TimeUnit.SECONDS)) {
                            Logger.warning("Shutdown save timeout: " + (validPlayers.size() - savedCount.get()) + " players could not finish saving in 30s!");
                        } else {
                            Logger.success("Shutdown save completed: " + savedCount.get() + "/" + validPlayers.size() + " players saved successfully.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                try {
                    ConsignShopManager.gI().save();
                    ClanService.gI().close();
                } catch (Exception ignored) {
                }
            }
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
                scheduledExecutorService.shutdown();
            }
            CLIENTS.clear();
        } catch (Exception e) {
            Logger.error("Error during cleanup: " + e.getMessage());
        }
    }

    private void activeCommandLine() {
        executorService.submit(() -> {
            try (Scanner sc = new Scanner(System.in)) {
                while (isRunning) {
                    try {
                        String line = sc.nextLine();
                        handleCommand(line);
                    } catch (Exception e) {
                        if (isRunning) {
                            Logger.error("Command line error: " + e.getMessage());
                        }
                        break;
                    }
                }
            }
        });
    }

    private void handleCommand(String line) {
        server.command.CommandRegistry.dispatch(line);
    }

    public void saveAllPlayersData() {
        try {
            Logger.log("Starting player data auto-save (Parallel Flush)...");
            List<Player> players = new ArrayList<>(Client.gI().getPlayers());
            List<Player> validPlayers = new ArrayList<>();
            for (Player pl : players) {
                if (pl != null && pl.isPl()) {
                    validPlayers.add(pl);
                }
            }

            if (!validPlayers.isEmpty()) {
                CountDownLatch latch = new CountDownLatch(validPlayers.size());
                AtomicInteger savedCount = new AtomicInteger(0);
                for (Player pl : validPlayers) {
                    Thread.ofVirtual().name("autosave-" + pl.id).start(() -> {
                        try {
                            PlayerDAO.updatePlayer(pl);
                            savedCount.incrementAndGet();
                        } catch (Exception exx) {
                            Logger.error("Error saving player: " + (pl != null ? pl.name : "unknown"));
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                try {
                    if (!latch.await(30, TimeUnit.SECONDS)) {
                        Logger.warning("Auto-save timeout: " + (validPlayers.size() - savedCount.get()) + " players did not complete in 30s!");
                    } else {
                        Logger.success("Auto-save completed: " + savedCount.get() + "/" + validPlayers.size() + " players saved.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                Logger.log("No players online to save.");
            }
        } catch (Exception ex) {
            Logger.error("Error in player data save: " + ex.getMessage());
        }
    }

    private void migratePasswordsToBCrypt() {
        Logger.log(Logger.YELLOW, "Đang kiểm tra và migrate mật khẩu sang BCrypt...\n");
        AlyraResultSet rs = null;
        int count = 0;
        try {
            rs = AlyraManager.executeQuery("SELECT id, password FROM account");
            java.util.Map<Integer, String> toUpdate = new java.util.HashMap<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String password = rs.getString("password");
                if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                    toUpdate.put(id, utils.PasswordUtils.hashPassword(password));
                }
            }
            rs.dispose();
            rs = null;
            for (java.util.Map.Entry<Integer, String> entry : toUpdate.entrySet()) {
                AlyraManager.executeUpdate("UPDATE account SET password = ? WHERE id = ?", entry.getValue(), entry.getKey());
                count++;
            }
            if (count > 0) {
                Logger.log(Logger.GREEN, "Đã migrate thành công " + count + " tài khoản sang mật khẩu BCrypt.\n");
            } else {
                Logger.log(Logger.GREEN, "Tất cả mật khẩu đã được mã hóa BCrypt.\n");
            }
        } catch (Exception e) {
            Logger.log(Logger.RED, "Lỗi khi migrate mật khẩu sang BCrypt: " + e.getMessage() + "\n");
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }
}

package server;

import bot.BotManager;
import bot.NewBot;
import database.AlyraManager;
import database.AlyraResultSet;
import utils.FileRunner;
import managers.boss.BrolyManager;
import database.daos.HistoryTransactionDAO;
import managers.boss.BossManager;
import managers.boss.OtherBossManager;
import managers.boss.TreasureUnderSeaManager;
import managers.boss.SnakeWayManager;
import managers.boss.RedRibbonHQManager;
import managers.boss.GasDestroyManager;
import managers.boss.YardartManager;
import managers.boss.ChristmasEventManager;
import managers.boss.FinalBossManager;
import managers.boss.HalloweenEventManager;
import managers.boss.HungVuongEventManager;
import managers.boss.LunarNewYearEventManager;
import managers.boss.SkillSummonedManager;
import managers.boss.TrungThuEventManager;
import java.io.IOException;
import interfaces.ISession;
import network.Network;
import network.io.MyKeyHandler;
import network.session.MySession;
import services.player.ClanService;
import services.phoban.NgocRongNamecService;
import services.func.minigame.CSMM;
import utils.Logger;
import utils.TimeUtil;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import managers.tournament.The23rdMartialArtCongressManager;
import managers.tournament.DeathOrAliveArenaManager;
import event.EventManager;
import database.daos.EventDAO;
import database.daos.PlayerDAO;
import managers.tournament.WorldMartialArtsTournamentManager;
import network.io.MessageSendCollect;
import managers.ShenronEventManager;
import interfaces.ISessionAcceptHandler;
import java.net.Socket;
import javax.swing.SwingUtilities;
import managers.AdminToolFrame;
import managers.ConsignShopManager;
import managers.SuperRankManager;
import models.player.Player;
import services.Service;
import services.TopService;

// Import thêm các class Data cache
import data.DataGame;
import data.ItemData;

public class ServerManager {

    public static String timeStart;

    // Sử dụng ConcurrentHashMap để thread-safe khi quản lý kết nối IP
    public static final Map<String, Integer> CLIENTS = new ConcurrentHashMap<>();

    public static String NAME = "Vũ trụ 1";
    // Để 0.0.0.0 để lắng nghe tất cả các interface mạng (tránh lỗi trên VPS)
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

        // Tối ưu số lượng Thread dựa trên CPU Core
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(corePoolSize * 2);
        scheduledExecutorService = Executors.newScheduledThreadPool(5);

        // Cleanup khi tắt server
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
        // 1. Load dữ liệu Cache trước tiên (Rất quan trọng)
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
        executorService.submit(() -> ShenronEventManager.gI().run());

        // 3. Load Boss và Map
        Logger.log("Đang tải dữ liệu Boss và Map...");
        ServerManager.isReloading = true;
        BossManager.gI().loadBoss();
        Manager.MAPS.forEach(models.map.Map::initBoss);
        EventManager.gI().init();
        ServerManager.isReloading = false;

        // 4. Chạy luồng Boss (Hiện tại được quản lý và cập nhật tập trung thông qua GameLoopManager)

        new Thread(TopService.gI(), "Top Service Thread").start();

        executorService.submit(() -> CSMM.gI().run());
        // Khởi chạy luồng quản lý Bot
        new Thread(BotManager.gI(), "Bot Manager").start();

        // Tạo Bot (Khởi tạo 10 Bot thông minh để test tính năng)
        NewBot.gI().runBot(0, 10);
        System.out.println("Đã khởi tạo 10 Bot thông minh chạy map!");
        // 5. Memory optimizer
        tools.MemoryOptimizer.gI(450, true);

        // 6. Start Auto Save
        startAutoSaveTask();
        GameLoopManager.gI().start();

        // 7. Mở Port kết nối và CMD (Mở cuối cùng để đảm bảo data đã load xong)
        Logger.log("Mở cổng kết nối máy chủ...");
        activeServerSocket();
        activeCommandLine();

        Logger.success("Máy chủ khởi động thành công trên PORT: " + PORT);
    }

    private void startAutoSaveTask() {
        // SỬA 1: Đổi thời gian chạy luồng check từ 5 phút thành 60 giây
        // Lý do: Để server check liên tục xem ai cần lưu, thay vì dồn cục 5 phút mới làm 1 lần.
        autoSaveTask = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!isRunning) {
                    return;
                }

                // Copy list để tránh lỗi khi người chơi thoát game giữa chừng
                List<Player> players = new ArrayList<>(Client.gI().getPlayers());
                if (players.isEmpty()) {
                    return;
                }

                // Logger.log("Đang kiểm tra auto-save cho " + players.size() + " người chơi..."); // Comment lại cho đỡ spam log
                AtomicInteger savedCount = new AtomicInteger(0);
                long currentTime = System.currentTimeMillis();
                long TIME_TO_SAVE = 5 * 60 * 1000; // 5 phút (300.000ms)

                // Duyệt qua danh sách người chơi
                for (Player player : players) {
                    if (player != null && player.isPl()) {

                        // SỬA 2: Thêm kiểm tra lastTimeSave
                        // Chỉ lưu nếu thời gian hiện tại trừ thời gian lưu cuối > 5 phút
                        if (currentTime - player.lastTimeSave >= TIME_TO_SAVE) {
                            try {
                                PlayerDAO.updatePlayer(player);

                                // SỬA 3: Cập nhật lại thời gian đã lưu
                                player.lastTimeSave = System.currentTimeMillis();

                                savedCount.incrementAndGet();

                                // Delay cực nhỏ để giảm tải CPU và DB (quan trọng)
                                Thread.sleep(10);
                            } catch (Exception e) {
                                Logger.error("Lỗi auto-save player: " + player.name);
                            }
                        }
                    }
                }

                // Chỉ log nếu có người được lưu để đỡ spam console
                if (savedCount.get() > 0) {
                    Logger.success("Auto-save hoàn tất: Đã lưu " + savedCount.get() + " người chơi.");
                }

            } catch (Exception e) {
                Logger.error("Auto-save error: " + e.getMessage());
                e.printStackTrace();
            }
        }, 60, 60, TimeUnit.SECONDS); // Chạy mỗi 60 giây
    }

    private void activeServerSocket() {
        try {
            Network.gI().init().setAcceptHandler(new ISessionAcceptHandler() {
                @Override
                public void sessionInit(ISession is) {
                    if (!canConnectWithIp(is.getIP())) {
                        is.disconnect();
                        return;
                    }
                    is.setMessageHandler(Controller.gI())
                            .setSendCollect(new MessageSendCollect())
                            .setKeyHandler(new MyKeyHandler())
                            .startCollect().startQueueHandler();
                }

                @Override
                public void sessionDisconnect(ISession session) {
                    Client.gI().kickSession((MySession) session);
                    disconnect((MySession) session);
                }
            }).setTypeSessionClone(MySession.class)
                    .setDoSomeThingWhenClose(this::cleanupResources)
                    .start(PORT);
        } catch (Exception e) {
            Logger.error("Error starting server socket: " + e.getMessage());
            System.exit(1); // Lỗi không mở được port thì tắt server luôn
        }
    }

    private boolean canConnectWithIp(String ipAddress) {
        int currentConnections = CLIENTS.computeIfAbsent(ipAddress, k -> 0);
        if (currentConnections < Manager.MAX_PER_IP) {
            CLIENTS.put(ipAddress, currentConnections + 1);
            return true;
        } else {
            return false;
        }
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
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                // Không force shutdown ngay để các task quan trọng kịp chạy xong
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
            Logger.log("Starting manual player data save...");
            List<Player> players = new ArrayList<>(Client.gI().getPlayers());
            AtomicInteger savedCount = new AtomicInteger(0);

            for (Player pl : players) {
                try {
                    if (pl != null && pl.isPl()) {
                        PlayerDAO.updatePlayer(pl);
                        savedCount.incrementAndGet();
                        // Delay nhỏ để Database thở
                        Thread.sleep(10);
                    }
                } catch (Exception exx) {
                    Logger.error("Error saving player: " + (pl != null ? pl.name : "unknown"));
                }
            }
            Logger.success("Manual save completed: " + savedCount.get() + " players saved");

        } catch (Exception ex) {
            Logger.error("Error in manual player data save");
        }
    }

    private void migratePasswordsToBCrypt() {
        Logger.log(Logger.YELLOW, "Đang kiểm tra và migrate mật khẩu sang BCrypt...\n");
        AlyraResultSet rs = null;
        int count = 0;
        try {
            rs = AlyraManager.executeQuery("SELECT id, password FROM account");
            while (rs.next()) {
                int id = rs.getInt("id");
                String password = rs.getString("password");
                if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                    String hashed = utils.PasswordUtils.hashPassword(password);
                    AlyraManager.executeUpdate("UPDATE account SET password = ? WHERE id = ?", hashed, id);
                    count++;
                }
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

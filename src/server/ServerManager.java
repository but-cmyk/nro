package server;

import bot.BotManager;
import bot.NewBot;
import database.AlyraManager;
import database.AlyraResultSet;
import database.daos.HistoryTransactionDAO;
import database.daos.EventDAO;
import database.daos.PlayerDAO;
import event.EventManager;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerManager {

    public static String timeStart;

    // Quản lý kết nối IP thread-safe + Burst Limiter (chống flood kết nối mới/giây)
    public static final Map<String, Integer> CLIENTS = new ConcurrentHashMap<>();
    private static final Map<String, long[]> IP_CONNECT_BURST = new ConcurrentHashMap<>();
    private static final int MAX_CONN_PER_SEC_PER_IP = 15;

    public static String NAME = "Vũ trụ 1";
    public static String IP = "0.0.0.0";
    public static int PORT = 14445;

    private static volatile ServerManager instance;
    public static volatile boolean isRunning;
    public static volatile boolean isReloading = false;
    public static volatile boolean isOverloaded = false;

    // Thread Pool quản lý
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    // Auto-save task
    private ScheduledFuture<?> autoSaveTask;
    private ScheduledFuture<?> dailyResetTask;

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

    public void execute(Runnable task) {
        if (task == null)
            return;
        if (this.executorService != null && !this.executorService.isShutdown()) {
            this.executorService.submit(task);
        } else {
            Thread.startVirtualThread(task);
        }
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
    }

    public void run() {
        // 1. Load dữ liệu Cache trước tiên
        Logger.log("Đang tải dữ liệu Cache...");
        DataGame.init();
        ItemData.init();

        // Migrate passwords to BCrypt (chạy ngầm, không block tiến trình khởi động
        // Server)
        Thread.ofVirtual().name("BCrypt-Migration-Thread").start(this::migratePasswordsToBCrypt);

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
        models.tournament.whis_arena.WhisArenaBotManager.gI().initLobbyBotsOnStartup();
        ServerManager.isReloading = false;

        Thread.ofVirtual().name("Top-Service-Thread").start(TopService.gI());

        // Bot Manager and Namec bot fleet disabled

        // 4. Memory optimizer
        tools.MemoryOptimizer.gI(450, true);

        // 5. Start Auto Save, Daily Reset & Game Loop
        startAutoSaveTask();
        startDailyResetTask();
        services.tournament.DeathOrAliveArenaWeeklyService.gI().init();
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
                utils.Logger.logException(ServerManager.class, e, "ServerManager exception");
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void startDailyResetTask() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        java.time.ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        long initialDelaySeconds = java.time.Duration.between(now, nextMidnight).getSeconds();
        long periodSeconds = 24 * 60 * 60; // Lặp lại mỗi 24 giờ

        dailyResetTask = scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                if (!isRunning) {
                    return;
                }
                List<Player> players = new ArrayList<>(Client.gI().getPlayers());
                for (Player player : players) {
                    if (player != null && player.isPl()) {
                        if (player.playerTask != null && player.playerTask.sideTask != null) {
                            try {
                                player.playerTask.sideTask.reset();
                                player.playerTask.sideTask.leftTask = consts.ConstTask.MAX_SIDE_TASK;
                                player.playerTask.sideTask.cancelCount = 0;
                                player.playerTask.sideTask.receivedTime = System.currentTimeMillis();
                                services.Service.gI().sendThongBao(player, "Nhiệm vụ hàng ngày đã được làm mới!");
                            } catch (Exception ex) {
                                Logger.error("Lỗi reset task cho player " + player.name + ": " + ex.getMessage());
                            }
                        }
                        if (player.canReceiveDailyMobRuby()) {
                            if (player.idMark != null) {
                                player.idMark.setLastTimeNotifyDailyMobRuby(System.currentTimeMillis());
                            }
                            services.Service.gI().sendThongBao(player,
                                    "Hôm nay bạn có thể nhận 30 Hồng ngọc khi tiêu diệt quái!");
                        }
                    }
                }
                // Chốt giải và trao thưởng tuần Võ Đài Sinh Tử nếu vào 0h Chủ Nhật
                services.tournament.DeathOrAliveArenaWeeklyService.gI().checkAndResetWeekly();
            } catch (Exception e) {
                Logger.error("Lỗi Daily Reset Task: " + e.getMessage());
            }
        }, initialDelaySeconds, periodSeconds, TimeUnit.SECONDS);
        Logger.log("DailyResetTask đã lên lịch: Reset lần tới sau " + (initialDelaySeconds / 60) + " phút (00:00:00)");
    }

    private void activeServerSocket() {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                NettyServer.gI().start(PORT, Manager.TLS_ENABLED, Manager.TLS_PORT, Manager.TLS_CERT, Manager.TLS_KEY);
                return;
            } catch (Exception e) {
                Logger.error(
                        "Lỗi khi khởi chạy Netty Server (lần " + attempt + "/" + maxRetries + "): " + e.getMessage());
                if (attempt < maxRetries) {
                    Logger.warning(">> Đang tự động giải phóng port " + PORT + " và thử khởi động lại sau 1.5 giây...");
                    freePort(PORT);
                    if (Manager.TLS_ENABLED && Manager.TLS_PORT > 0 && Manager.TLS_PORT != PORT) {
                        freePort(Manager.TLS_PORT);
                    }
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    Logger.error("Không thể giải phóng hoặc lắng nghe trên PORT " + PORT + ". Server buộc phải dừng!");
                    System.exit(1);
                }
            }
        }
    }

    public static void freePort(int port) {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            long currentPid = ProcessHandle.current().pid();
            if (os.contains("win")) {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr :" + port);
                Process proc = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || !line.toUpperCase().contains("LISTENING")) {
                            continue;
                        }
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 5) {
                            String pidStr = parts[parts.length - 1];
                            try {
                                long pid = Long.parseLong(pidStr);
                                if (pid > 0 && pid != currentPid) {
                                    Logger.warning(">> [PortRecovery] Phát hiện tiến trình PID " + pid
                                            + " đang chiếm giữ port " + port + ". Đang tiến hành giải phóng...");
                                    new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid)).start().waitFor(3,
                                            TimeUnit.SECONDS);
                                    Logger.success(
                                            ">> [PortRecovery] Đã gửi lệnh giải phóng PID " + pid + " thành công!");
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
                proc.waitFor(2, TimeUnit.SECONDS);
            } else {
                Logger.warning(">> [PortRecovery] Đang giải phóng port " + port + " trên môi trường Unix/Linux...");
                new ProcessBuilder("sh", "-c", "fuser -k -n tcp " + port + " || lsof -ti :" + port + " | xargs kill -9")
                        .start().waitFor(3, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            Logger.error(">> [PortRecovery] Lỗi khi giải phóng port " + port + ": " + ex.getMessage());
        }
    }

    public static boolean canConnectWithIp(String ipAddress) {
        if (ipAddress == null) {
            return true;
        }
        // Chống bão kết nối mới (Connection Burst Flood) theo cửa sổ 1 giây cho IP ngoài
        if (!"127.0.0.1".equals(ipAddress) && !"0:0:0:0:0:0:0:1".equals(ipAddress)) {
            long nowSec = System.currentTimeMillis() / 1000L;
            if (IP_CONNECT_BURST.size() > 2000) {
                IP_CONNECT_BURST.entrySet().removeIf(e -> nowSec - e.getValue()[0] > 5);
            }
            final boolean[] burstOk = { false };
            IP_CONNECT_BURST.compute(ipAddress, (ip, state) -> {
                if (state == null || state[0] != nowSec) {
                    burstOk[0] = true;
                    return new long[] { nowSec, 1L };
                }
                if (state[1] < MAX_CONN_PER_SEC_PER_IP) {
                    state[1]++;
                    burstOk[0] = true;
                }
                return state;
            });
            if (!burstOk[0]) {
                return false;
            }
        }
        final boolean[] accepted = { false };
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

    private final java.util.concurrent.atomic.AtomicBoolean shutdownStarted = new java.util.concurrent.atomic.AtomicBoolean(
            false);

    private void cleanupResources() {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        Logger.log("Cleaning up server resources (Graceful Shutdown)...");
        try {
            isRunning = false;
            try {
                NettyServer.gI().close();
            } catch (Exception e) {
                Logger.error("Error closing NettyServer on shutdown: " + e.getMessage());
            }
            try {
                GameLoopManager.gI().stop();
            } catch (Exception e) {
                Logger.error("Error stopping GameLoopManager on shutdown: " + e.getMessage());
            }

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
                flushPlayersInParallel(validPlayers, "shutdown-save-", "Shutdown save");
            }

            try {
                ConsignShopManager.gI().save();
                EventDAO.save();
                ClanService.gI().close();
            } catch (Exception e) {
                Logger.error("Error saving global state on shutdown: " + e.getMessage());
            }

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
                scheduledExecutorService.shutdown();
            }
            try {
                AlyraManager.close();
            } catch (Exception ignored) {
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

    private void flushPlayersInParallel(List<Player> validPlayers, String threadPrefix, String label) {
        CountDownLatch latch = new CountDownLatch(validPlayers.size());
        AtomicInteger savedCount = new AtomicInteger(0);
        // Điều tiết tối đa 20 flush đồng thời khớp với HikariCP MAX_CONN=25 tránh tranh chấp connection
        Semaphore dbFlushPermits = new Semaphore(20);
        for (Player pl : validPlayers) {
            Thread.ofVirtual().name(threadPrefix + pl.id).start(() -> {
                boolean acquired = false;
                try {
                    dbFlushPermits.acquire();
                    acquired = true;
                    PlayerDAO.updatePlayer(pl);
                    savedCount.incrementAndGet();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (Exception exx) {
                    Logger.error("Error saving player: " + (pl != null ? pl.name : "unknown"));
                } finally {
                    if (acquired) {
                        dbFlushPermits.release();
                    }
                    latch.countDown();
                }
            });
        }
        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                Logger.warning(label + " timeout: " + (validPlayers.size() - savedCount.get())
                        + " players did not complete in 30s!");
            } else {
                Logger.success(label + " completed: " + savedCount.get() + "/" + validPlayers.size()
                        + " players saved.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
                flushPlayersInParallel(validPlayers, "autosave-", "Auto-save");
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
                if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$")
                        && !password.startsWith("$2y$")) {
                    toUpdate.put(id, utils.PasswordUtils.hashPassword(password));
                }
            }
            rs.dispose();
            rs = null;
            for (java.util.Map.Entry<Integer, String> entry : toUpdate.entrySet()) {
                AlyraManager.executeUpdate("UPDATE account SET password = ? WHERE id = ?", entry.getValue(),
                        entry.getKey());
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

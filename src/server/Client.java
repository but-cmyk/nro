package server;

import database.AlyraManager;
import database.daos.PlayerDAO;
import lombok.Getter;
import models.map.ItemMap;
import models.player.Player;
import network.session.SessionManager;
import interfaces.ISession;
import java.sql.SQLException;
import network.session.MySession;
import services.Service;
import services.map.ChangeMapService;
import services.func.SummonDragon;
import services.func.TransactionService;
import services.phoban.NgocRongNamecService;
import utils.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import services.func.SummonDragonNamek;

public class Client implements Runnable {

    // Constants
    private static final long SESSION_CLEANUP_INTERVAL = 300000; // 5 minutes
    private static final long UPDATE_INTERVAL = 1000; // 1 second
    private static final int NRNAM_OFFSET = 353;

    private static Client instance;
    private static final Object instanceLock = new Object();

    // Thread-safe collections
    private final ConcurrentHashMap<Long, Player> playersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Player> playersByUserId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Player> playersByName = new ConcurrentHashMap<>();

    @Getter
    private final List<Player> players = new CopyOnWriteArrayList<>();

    // Thread management
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private volatile boolean running = true;

    private Client() {
        initializeThreads();
    }

    public static Client gI() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

    private void initializeThreads() {
        // Main update thread
        new Thread(this, "Update Client").start();

        // Session cleanup scheduler
        scheduler.scheduleAtFixedRate(() -> {
            if (ServerManager.isRunning) {
                try {
                    clearCloneSessions();
                    logMemory();
                    System.gc();
                } catch (Exception e) {
                    Logger.logException(Client.class, e, "Error in session cleanup task");
                }
            }
        }, SESSION_CLEANUP_INTERVAL, SESSION_CLEANUP_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public Player getPlayerByUserName(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        return players.stream()
                .filter(player -> player.getSession() != null && username.equals(player.getSession().uu))
                .findFirst()
                .orElse(null);
    }

    public void clearCloneSessions() {
        int cleared = 0;
        try {
            List<ISession> sessions = SessionManager.gI().getSessions();
            synchronized (sessions) {
                Iterator<ISession> iterator = sessions.iterator();
                while (iterator.hasNext()) {
                    MySession session = (MySession) iterator.next();
                    if (session != null && session.player == null) {
                        iterator.remove();
                        cleared++;
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error clearing clone sessions");
        }
    }

    public void put(Player player) {
        if (player == null) {
            Logger.error("Attempted to add null player to client");
            return;
        }

        try {
            this.playersById.put(player.id, player);
            this.playersByName.put(player.name, player);

            if (player.getSession() != null) {
                this.playersByUserId.put(player.getSession().userId, player);
            }

            if (!players.contains(player)) {
                this.players.add(player);
            }

            Logger.log("Added player: " + player.name + " (ID: " + player.id + ")");
        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error adding player: " + player.name);
        }
    }

    public void remove(MySession session) {
        if (session == null) {
            return;
        }

        try {
            if (session.player != null) {
                removePlayerFromGame(session.player);
                session.player = null;
            }

            if (session.joinedGame) {
                updateLastLogoutTime(session);
                session.joinedGame = false;
            }

            ServerManager.gI().disconnect(session);

        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error removing session for user: " + session.userId);
        }
    }

    // Trong class Client.java
    private void removePlayerFromGame(Player player) {
        if (player == null) {
            return;
        }

        // 1. Xóa khỏi danh sách quản lý ngay lập tức để tránh tương tác mới
        this.players.remove(player);
        this.playersById.remove(player.id);
        this.playersByName.remove(player.name);
        if (player.getSession() != null) {
            this.playersByUserId.remove(player.getSession().userId);
        }

        // 2. Xử lý logic game (thoát map, hủy giao dịch...)
        try {
            if (player.zone != null && player.zone.map != null) {
                player.mapIdBeforeLogout = player.zone.map.mapId;
            }
            handlePlayerDisconnectCleanup(player);
        } catch (Exception e) {
            Logger.error("Lỗi cleanup player " + player.name);
        }

        // 3. Lưu dữ liệu bất đồng bộ (Không sleep, không delay)
        CompletableFuture.runAsync(() -> {
            try {
                PlayerDAO.updatePlayer(player);
            } catch (Exception e) {
                Logger.error("Lỗi lưu data player " + player.name);
            }
        }).thenRun(() -> {
            // Dispose ngay sau khi lưu xong
            if (player != null) {
                player.dispose();
            }
        });
    }

    private void handlePlayerDisconnectCleanup(Player player) {
        try {
            if (!player.beforeDispose) {
                player.beforeDispose = true;

                // Handle Ngoc Rong Namek items
                if (player.idNRNM != -1) {
                    dropNgocRongNamekItem(player);
                }

                // Exit current map
                ChangeMapService.gI().exitMap(player);

                // Cancel trades
                TransactionService.gI().cancelTrade(player);

                // Handle clan
                if (player.clan != null) {
                    player.clan.removeMemberOnline(null, player);
                }

                // Handle dragon summons
                handleDragonSummonCleanup(player);

                // Handle pets and mobs
                handlePetAndMobCleanup(player);
            }
        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error in player disconnect cleanup: " + player.name);
        }
    }

    private void dropNgocRongNamekItem(Player player) {
        try {
            if (player.zone != null && player.location != null) {
                ItemMap itemMap = new ItemMap(player.zone, player.idNRNM, 1,
                        player.location.x, player.location.y, -1);
                Service.gI().dropItemMap(player.zone, itemMap);

                int index = player.idNRNM - NRNAM_OFFSET;
                if (index >= 0 && index < NgocRongNamecService.gI().pNrNamec.length) {
                    NgocRongNamecService.gI().pNrNamec[index] = "";
                    NgocRongNamecService.gI().idpNrNamec[index] = -1;
                }

                player.idNRNM = -1;
            }
        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error dropping Ngoc Rong Namek item for player: " + player.name);
        }
    }

    private void handleDragonSummonCleanup(Player player) {
        try {
            // Handle Shenron summons
            if (SummonDragon.gI().playerSummonShenron != null
                    && SummonDragon.gI().playerSummonShenron.id == player.id) {
                SummonDragon.gI().isPlayerDisconnect = true;
            }

            if (SummonDragonNamek.gI().playerSummonShenron != null
                    && SummonDragonNamek.gI().playerSummonShenron.id == player.id) {
                SummonDragonNamek.gI().isPlayerDisconnect = true;
            }

            // Handle shenron events
            if (player.shenronEvent != null) {
                player.shenronEvent.isPlayerDisconnect = true;
            }
        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error handling dragon summon cleanup for player: " + player.name);
        }
    }

    private void handlePetAndMobCleanup(Player player) {
        try {
            // Handle player's mob
            if (player.mobMe != null) {
                player.mobMe.mobMeDie();
            }

            // Handle pet
            if (player.pet != null) {
                if (player.pet.mobMe != null) {
                    player.pet.mobMe.mobMeDie();
                }
                ChangeMapService.gI().exitMap(player.pet);
            }
        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error handling pet/mob cleanup for player: " + player.name);
        }
    }

    private void updateLastLogoutTime(MySession session) {
        CompletableFuture.runAsync(() -> {
            try {
                AlyraManager.executeUpdate(
                        "UPDATE account SET last_time_logout = ? WHERE id = ?",
                        new Timestamp(System.currentTimeMillis()),
                        session.userId
                );
            } catch (SQLException e) {
                Logger.logException(Client.class, e, "Error updating last_time_logout for user: " + session.userId);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public void kickSession(MySession session) {
        if (session != null) {
            Logger.log("Kicking session for user: " + session.userId);
            remove(session);
            session.disconnect();
        }
    }

    public Player getPlayer(long playerId) {
        return this.playersById.get(playerId);
    }

    public Player getPlayerByUser(int userId) {
        return this.playersByUserId.get(userId);
    }

    public Player getPlayer(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return this.playersByName.get(name);
    }

    public void close() {
        Logger.log(Logger.YELLOW, "BEGIN SHUTDOWN - KICKING " + players.size() + " PLAYERS");
        running = false;

        try {
            // Stop scheduled tasks
            scheduler.shutdown();
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }

            // Kick all players
            List<Player> playersToKick = new ArrayList<>(players);
            for (Player player : playersToKick) {
                if (player != null && player.getSession() != null) {
                    kickSession(player.getSession());
                }
            }

            // Clear collections
            players.clear();
            playersById.clear();
            playersByUserId.clear();
            playersByName.clear();

        } catch (InterruptedException e) {
            Logger.logException(Client.class, e, "Interrupted during shutdown");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error during shutdown");
        }

        Logger.success("CLIENT SHUTDOWN SUCCESSFUL");
    }

    public void cloneMySessionNotConnect() {
        Logger.error("---------START KICK SESSION DDOS---------");

        try {
            List<ISession> sessions = SessionManager.gI().getSessions();
            int initialSize = sessions.size();
            Logger.error("INITIAL SESSION COUNT: " + initialSize);

            synchronized (sessions) {
                Iterator<ISession> iterator = sessions.iterator();
                while (iterator.hasNext()) {
                    MySession session = (MySession) iterator.next();
                    if (session != null && session.player == null) {
                        iterator.remove();
                    }
                }
            }

            int finalSize = sessions.size();
            int removed = initialSize - finalSize;
            Logger.error("REMOVED " + removed + " DDOS SESSIONS");
            Logger.error("FINAL SESSION COUNT: " + finalSize);

        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error during DDOS session cleanup");
        }

        Logger.error("---------DDOS CLEANUP SUCCESSFUL---------");
    }

    private void update() {
        try {
            List<ISession> sessions = SessionManager.gI().getSessions();

            // Create a copy to avoid concurrent modification
            List<MySession> sessionsToCheck = new ArrayList<>();
            synchronized (sessions) {
                for (int i = sessions.size() - 1; i >= 0; i--) {
                    ISession session = sessions.get(i);
                    if (session instanceof MySession) {
                        sessionsToCheck.add((MySession) session);
                    } else if (session == null) {
                        sessions.remove(i);
                    }
                }
            }

            // Process sessions outside of synchronized block
            for (MySession session : sessionsToCheck) {
                if (session.timeWait > 0) {
                    session.timeWait--;
                    if (session.timeWait == 0) {
                        CompletableFuture.runAsync(() -> kickSession(session));
                    }
                }
            }

        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error in client update loop");
        }

    }

    public void logMemory() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long freeMB = runtime.freeMemory() / (1024 * 1024);
            long totalMB = runtime.totalMemory() / (1024 * 1024);
            long maxMB = runtime.maxMemory() / (1024 * 1024);

            double memoryUsagePercent = (double) usedMB / maxMB * 100;

            Logger.log("========== MEMORY USAGE REPORT ==========");
            Logger.log("Used Memory: " + usedMB + " MB (" + String.format("%.1f", memoryUsagePercent) + "%)");
            Logger.log("Free Memory: " + freeMB + " MB");
            Logger.log("Total Memory: " + totalMB + " MB");
            Logger.log("Max Memory: " + maxMB + " MB");
            Logger.log("Players Online: " + players.size());
            Logger.log("Active Sessions: " + SessionManager.gI().getSessions().size());

            // Warning if memory usage is high
            if (memoryUsagePercent > 80) {
                Logger.error("WARNING: High memory usage detected (" + String.format("%.1f", memoryUsagePercent) + "%)");
            }

            Logger.log("=========================================");

        } catch (Exception e) {
            Logger.logException(Client.class, e, "Error logging memory usage");
        }
    }

    @Override
    public void run() {
        Logger.log("Client update thread started");

        while (running && ServerManager.isRunning) {
            if (ServerManager.isReloading) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                } // Chờ một chút
                continue; // Bỏ qua lượt update này
            }
            long startTime = System.currentTimeMillis();

            try {
                update();
            } catch (Exception e) {
                Logger.logException(Client.class, e, "Error in main update loop");
            }

            // Calculate sleep time
            long processingTime = System.currentTimeMillis() - startTime;
            long sleepTime = UPDATE_INTERVAL - processingTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Logger.log("Client update thread interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                Logger.warning("Client update took longer than expected: " + processingTime + "ms");
            }
        }

        Logger.log("Client update thread stopped");
    }

}

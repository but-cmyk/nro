package services.admin;

import database.AlyraManager;
import database.daos.NDVSqlFetcher;
import database.daos.PlayerDAO;
import managers.boss.BossManager;
import models.boss.Boss;
import models.item.Item;
import models.player.Player;
import server.Client;
import server.Maintenance;
import server.Manager;
import server.ServerManager;
import server.ServerNotify;
import services.ItemService;
import services.Service;
import services.map.ChangeMapService;
import services.player.InventoryService;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private static AdminService instance;

    public static AdminService gI() {
        if (instance == null) {
            synchronized (AdminService.class) {
                if (instance == null) {
                    instance = new AdminService();
                }
            }
        }
        return instance;
    }

    private AdminService() {}

    /**
     * Kicks a player session by player name.
     */
    public boolean kickPlayer(String adminName, String playerName, String reason) {
        Player player = Client.gI().getPlayer(playerName);
        if (player != null && player.getSession() != null) {
            String kickReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Yeu cau boi Admin";
            Service.gI().sendThongBao(player, "Ban da bi kick khoi server. Ly do: " + kickReason);
            Client.gI().kickSession(player.getSession());
            AuditLogService.gI().log(adminName, "KICK_PLAYER", playerName, "Reason: " + kickReason);
            return true;
        }
        return false;
    }

    /**
     * Bans an account by player name with optional duration in hours (0 = permanent).
     */
    public boolean banPlayer(String adminName, String playerName, String reason) {
        return banPlayer(adminName, playerName, 0, reason);
    }

    public boolean banPlayer(String adminName, String playerName, int hours, String reason) {
        String durationStr = (hours > 0) ? (hours + " gio") : "vinh vien";
        String banReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Vi pham quy dinh";
        String notifyMsg = "Tai khoan da bi KHOA (" + durationStr + ") boi Admin. Ly do: " + banReason;
        Player player = Client.gI().getPlayer(playerName);

        if (player != null) {
            // Online player
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE `account` SET `ban` = 1 WHERE `id` = ?")) {
                int accountId = player.getSession() != null ? player.getSession().userId : player.account_id;
                ps.setInt(1, accountId);
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("[AdminService] Failed to ban online account: " + e.getMessage());
            }
            if (player.getSession() != null) {
                Service.gI().sendThongBao(player, notifyMsg);
                Client.gI().kickSession(player.getSession());
            }
            AuditLogService.gI().log(adminName, "BAN_PLAYER", playerName, "Online ban (" + durationStr + "). Reason: " + banReason);
            return true;
        }

        // Offline player: update via JOIN on player.account_id
        String sql = "UPDATE `account` a JOIN `player` p ON a.`id` = p.`account_id` SET a.`ban` = 1 WHERE p.`name` = ?";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, playerName);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                AuditLogService.gI().log(adminName, "BAN_PLAYER", playerName, "Offline ban (" + durationStr + "). Reason: " + banReason);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("[AdminService] Failed to ban offline account: " + e.getMessage());
        }
        return false;
    }

    /**
     * Unbans an account by username or player name.
     */
    public boolean unbanAccount(String adminName, String usernameOrPlayerName) {
        try (Connection con = AlyraManager.getConnection()) {
            // Try unban by account username first
            try (PreparedStatement ps = con.prepareStatement("UPDATE `account` SET `ban` = 0 WHERE `username` = ?")) {
                ps.setString(1, usernameOrPlayerName);
                if (ps.executeUpdate() > 0) {
                    AuditLogService.gI().log(adminName, "UNBAN_ACCOUNT", usernameOrPlayerName, "Unbanned by username");
                    return true;
                }
            }
            // Try unban by player name
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE `account` a JOIN `player` p ON a.`id` = p.`account_id` SET a.`ban` = 0 WHERE p.`name` = ?")) {
                ps.setString(1, usernameOrPlayerName);
                if (ps.executeUpdate() > 0) {
                    AuditLogService.gI().log(adminName, "UNBAN_ACCOUNT", usernameOrPlayerName, "Unbanned by character name");
                    return true;
                }
            }
        } catch (SQLException e) {
            Logger.error("[AdminService] Failed to unban account: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mutes player chat for a duration in minutes.
     */
    public boolean mutePlayer(String adminName, String playerName, int minutes, String reason) {
        Player player = Client.gI().getPlayer(playerName);
        if (player != null) {
            int mins = Math.max(1, minutes);
            player.isMuted = true;
            player.muteUntil = System.currentTimeMillis() + ((long) mins * 60 * 1000);
            Service.gI().sendThongBao(player, "Ban da bi cam chat " + mins + " phut. Ly do: " + (reason != null ? reason : "Admin can thiep"));
            AuditLogService.gI().log(adminName, "MUTE_PLAYER", playerName, "Duration: " + mins + "m. Reason: " + reason);
            return true;
        }
        return false;
    }

    /**
     * Unmutes player chat immediately.
     */
    public boolean unmutePlayer(String adminName, String playerName) {
        Player player = Client.gI().getPlayer(playerName);
        if (player != null) {
            player.isMuted = false;
            player.muteUntil = 0;
            Service.gI().sendThongBao(player, "Ban da duoc mo khoa chat!");
            AuditLogService.gI().log(adminName, "UNMUTE_PLAYER", playerName, "Unmuted");
            return true;
        }
        return false;
    }

    /**
     * Freezes a player (prevents movement and skill casting).
     */
    public boolean freezePlayer(String adminName, String playerName) {
        Player player = Client.gI().getPlayer(playerName);
        if (player != null) {
            player.isFrozen = true;
            Service.gI().sendThongBao(player, "Ban dang bi dong bang boi Quan tri vien!");
            AuditLogService.gI().log(adminName, "FREEZE_PLAYER", playerName, "Frozen");
            return true;
        }
        return false;
    }

    /**
     * Unfreezes a player.
     */
    public boolean unfreezePlayer(String adminName, String playerName) {
        Player player = Client.gI().getPlayer(playerName);
        if (player != null) {
            player.isFrozen = false;
            Service.gI().sendThongBao(player, "Hieu ung dong bang da duoc go bo.");
            AuditLogService.gI().log(adminName, "UNFREEZE_PLAYER", playerName, "Unfrozen");
            return true;
        }
        return false;
    }

    /**
     * Teleports a player back to their gender-specific home village.
     */
    public boolean teleportToVillage(String adminName, String playerName) {
        Player player = Client.gI().getPlayer(playerName);
        if (player != null) {
            int villageMapId = 21 + player.gender;
            ChangeMapService.gI().changeMapBySpaceShip(player, villageMapId, -1, -1);
            Service.gI().sendThongBao(player, "Quan tri vien da dua ban ve lang!");
            AuditLogService.gI().log(adminName, "TELEPORT_VILLAGE", playerName, "Sent to Map " + villageMapId);
            return true;
        }
        return false;
    }

    /**
     * Teleports a player to a specific map, zone, and coordinates.
     */
    public boolean teleportPlayer(String adminName, String playerName, int mapId, int zoneId, int x, int y) {
        Player player = Client.gI().getPlayer(playerName);
        if (player != null) {
            ChangeMapService.gI().changeMap(player, mapId, zoneId, x, y);
            Service.gI().sendThongBao(player, "Quan tri vien da dich chuyen ban!");
            AuditLogService.gI().log(adminName, "TELEPORT_MAP", playerName, String.format("Map: %d, Zone: %d, X: %d, Y: %d", mapId, zoneId, x, y));
            return true;
        }
        return false;
    }

    /**
     * Buffs Gold, Gem, or Ruby to a player (online or offline).
     */
    public boolean buffMoney(String adminName, String playerName, long gold, int gem, int ruby) {
        Player player = Client.gI().getPlayer(playerName);
        boolean isOnline = (player != null);

        if (player == null) {
            player = NDVSqlFetcher.loadPlayerByName(playerName);
        }

        if (player == null || player.inventory == null) {
            return false;
        }

        if (gold != 0) {
            player.inventory.addGold(gold);
        }
        if (gem > 0) {
            player.inventory.gem += gem;
        }
        if (ruby > 0) {
            player.inventory.ruby += ruby;
        }

        if (isOnline) {
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, String.format("Admin da cong: %s Vang, %d Ngoc xanh, %d Hong ngoc",
                    utils.Util.formatNumber(gold), gem, ruby));
        }

        PlayerDAO.updatePlayer(player);
        AuditLogService.gI().log(adminName, "BUFF_MONEY", playerName,
                String.format("Gold: %d, Gem: %d, Ruby: %d (Online: %b)", gold, gem, ruby, isOnline));
        return true;
    }

    /**
     * Buffs an item with custom options to a player (online or offline).
     */
    public boolean buffItem(String adminName, String playerName, short itemId, int quantity, String optionsStr) {
        Player player = Client.gI().getPlayer(playerName);
        boolean isOnline = (player != null);

        if (player == null) {
            player = NDVSqlFetcher.loadPlayerByName(playerName);
        }

        if (player == null || player.inventory == null) {
            return false;
        }

        Item item = ItemService.gI().createNewItem(itemId, Math.max(1, quantity));
        if (optionsStr != null && !optionsStr.trim().isEmpty()) {
            String[] pairs = optionsStr.trim().split("v");
            for (String pair : pairs) {
                String[] parts = pair.trim().split("-");
                if (parts.length == 2) {
                    try {
                        int optId = Integer.parseInt(parts[0].trim());
                        int param = Integer.parseInt(parts[1].trim());
                        item.itemOptions.add(new Item.ItemOption(optId, param));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        InventoryService.gI().addItemBag(player, item);

        if (isOnline) {
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Ban vua nhan duoc [" + item.template.name + " x" + quantity + "] tu Admin!");
        }

        PlayerDAO.updatePlayer(player);
        AuditLogService.gI().log(adminName, "BUFF_ITEM", playerName,
                String.format("Item: %s [ID: %d, Qty: %d, Opts: %s] (Online: %b)",
                        item.template.name, itemId, quantity, optionsStr, isOnline));
        return true;
    }

    /**
     * Broadcasts a server announcement.
     */
    public void broadcast(String adminName, String message, boolean isRedBanner) {
        if (message == null || message.trim().isEmpty()) return;
        if (isRedBanner) {
            Service.gI().sendThongBaoAllPlayer(message.trim());
        } else {
            ServerNotify.gI().notify(message.trim());
        }
        AuditLogService.gI().log(adminName, "BROADCAST", "ALL", (isRedBanner ? "[RED] " : "[GLOBAL] ") + message);
    }

    /**
     * Starts maintenance countdown in minutes.
     */
    public void startMaintenance(String adminName, int minutes) {
        int safeMinutes = Math.max(1, minutes);
        Maintenance.gI().start(safeMinutes * 60);
        AuditLogService.gI().log(adminName, "MAINTENANCE", "SERVER", "Countdown: " + safeMinutes + " minutes");
    }

    /**
     * Triggers parallel save of all active players.
     */
    public int saveAllPlayers(String adminName) {
        int count = 0;
        List<Player> players = new ArrayList<>(Client.gI().getPlayers());
        for (Player pl : players) {
            if (pl != null && pl.isPl()) {
                PlayerDAO.updatePlayer(pl);
                count++;
            }
        }
        AuditLogService.gI().log(adminName, "SAVE_ALL_PLAYERS", "SERVER", "Saved " + count + " players to database");
        return count;
    }

    /**
     * Hot-reloads game templates and shops safely.
     */
    public boolean reloadGameData(String adminName, String category) {
        if (ServerManager.isReloading) {
            return false;
        }
        ServerManager.isReloading = true;
        try {
            AlyraManager.reloadData();
            Manager.gI().reloadData();
            AuditLogService.gI().log(adminName, "RELOAD_DATA", "SERVER", "Category: " + (category != null ? category : "ALL"));
            return true;
        } catch (Exception e) {
            Logger.error("[AdminService] Reload failed: " + e.getMessage());
            return false;
        } finally {
            ServerManager.isReloading = false;
        }
    }

    /**
     * Despawns / kills a live boss by Boss ID.
     */
    public boolean killBoss(String adminName, int bossId) {
        for (Boss boss : BossManager.gI().getBosses()) {
            if (boss != null && boss.id == bossId) {
                boss.leaveMap();
                AuditLogService.gI().log(adminName, "KILL_BOSS", boss.name, "Boss ID: " + bossId);
                return true;
            }
        }
        return false;
    }
}


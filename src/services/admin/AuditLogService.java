package services.admin;

import database.AlyraManager;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AuditLogService {

    private static AuditLogService instance;
    private final BlockingQueue<AuditEntry> logQueue = new LinkedBlockingQueue<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // In-memory fallback ring buffer (holds up to 200 recent entries if DB is unavailable)
    private final List<AuditEntry> inMemoryLogs = Collections.synchronizedList(new LinkedList<>());
    private static final int MAX_IN_MEMORY_LOGS = 200;
    private volatile boolean dbTableAvailable = false;

    public static class AuditEntry {
        public long id;
        public String adminName;
        public String action;
        public String targetName;
        public String detail;
        public String createdAt;

        public AuditEntry() {}

        public AuditEntry(String adminName, String action, String targetName, String detail) {
            this.adminName = adminName != null ? adminName : "System";
            this.action = action;
            this.targetName = targetName;
            this.detail = detail;
            this.createdAt = LocalDateTime.now().format(FORMATTER);
        }

        public AuditEntry(long id, String adminName, String action, String targetName, String detail, String createdAt) {
            this.id = id;
            this.adminName = adminName;
            this.action = action;
            this.targetName = targetName;
            this.detail = detail;
            this.createdAt = createdAt;
        }
    }

    public static AuditLogService gI() {
        if (instance == null) {
            synchronized (AuditLogService.class) {
                if (instance == null) {
                    instance = new AuditLogService();
                }
            }
        }
        return instance;
    }

    private AuditLogService() {
        checkAndInitTable();
        startWorker();
    }

    private void checkAndInitTable() {
        // 1. First test if table already exists (avoids CREATE TABLE privilege requirement)
        String checkSql = "SELECT 1 FROM `admin_audit_log` LIMIT 1";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            dbTableAvailable = true;
            Logger.log(Logger.GREEN, "[AuditLog] Table `admin_audit_log` is ready in database.");
            return;
        } catch (SQLException ignored) {
            // Table doesn't exist or query failed, try create
        }

        // 2. Try to create if user has DDL privileges
        String createSql = "CREATE TABLE IF NOT EXISTS `admin_audit_log` ("
                + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "`admin_name` VARCHAR(50) NOT NULL, "
                + "`action` VARCHAR(50) NOT NULL, "
                + "`target_name` VARCHAR(50) NULL, "
                + "`detail` TEXT NULL, "
                + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "INDEX `idx_admin` (`admin_name`), "
                + "INDEX `idx_target` (`target_name`), "
                + "INDEX `idx_created` (`created_at`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(createSql)) {
            ps.executeUpdate();
            dbTableAvailable = true;
            Logger.log(Logger.GREEN, "[AuditLog] Table `admin_audit_log` created successfully.");
        } catch (SQLException e) {
            dbTableAvailable = false;
            Logger.warning("[AuditLog] Database user lacks DDL CREATE TABLE privilege. Falling back to in-memory audit logs. (To persist to DB, run docker/initdb/04_audit_log.sql with MySQL root).");
        }
    }

    private void startWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    AuditEntry entry = logQueue.take();
                    persistLog(entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Logger.error("[AuditLog] Error in background worker: " + e.getMessage());
                }
            }
        }, "AuditLog-Worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void persistLog(AuditEntry entry) {
        if (!dbTableAvailable) {
            return;
        }
        String sql = "INSERT INTO `admin_audit_log` (`admin_name`, `action`, `target_name`, `detail`, `created_at`) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entry.adminName);
            ps.setString(2, entry.action);
            ps.setString(3, entry.targetName);
            ps.setString(4, entry.detail);
            ps.setString(5, entry.createdAt);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.error("[AuditLog] Failed to persist log entry to database: " + e.getMessage());
        }
    }

    public void log(String adminName, String action, String targetName, String detail) {
        AuditEntry entry = new AuditEntry(adminName, action, targetName, detail);
        logQueue.offer(entry);

        // Always keep in memory ring buffer as immediate cache
        synchronized (inMemoryLogs) {
            inMemoryLogs.add(0, entry);
            if (inMemoryLogs.size() > MAX_IN_MEMORY_LOGS) {
                inMemoryLogs.remove(inMemoryLogs.size() - 1);
            }
        }

        Logger.log(Logger.PURPLE, String.format("[AUDIT] Admin [%s] -> Action [%s] on [%s]: %s",
                adminName, action, targetName != null ? targetName : "*", detail != null ? detail : ""));
    }

    public List<AuditEntry> getRecentLogs(int limit) {
        int safeLimit = Math.min(Math.max(1, limit), 200);

        // Try fetching from database first if available
        if (dbTableAvailable) {
            List<AuditEntry> result = new ArrayList<>();
            String sql = "SELECT `id`, `admin_name`, `action`, `target_name`, `detail`, `created_at` FROM `admin_audit_log` ORDER BY `id` DESC LIMIT ?";
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, safeLimit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new AuditEntry(
                                rs.getLong("id"),
                                rs.getString("admin_name"),
                                rs.getString("action"),
                                rs.getString("target_name"),
                                rs.getString("detail"),
                                rs.getString("created_at")
                        ));
                    }
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            } catch (SQLException e) {
                Logger.warning("[AuditLog] DB fetch error, falling back to in-memory: " + e.getMessage());
            }
        }

        // Fallback to in-memory logs
        synchronized (inMemoryLogs) {
            int toIndex = Math.min(safeLimit, inMemoryLogs.size());
            return new ArrayList<>(inMemoryLogs.subList(0, toIndex));
        }
    }
}

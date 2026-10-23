package database.daos;

import database.AlyraManager;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DAO ghi nhận lịch sử thao tác của Admin (A5 - Audit Admin)
 * Bảng: admin_audit
 */
public class AdminAuditDAO {

    private static volatile boolean initialized = false;

    private static void ensureAuditTable() {
        if (initialized) {
            return;
        }
        synchronized (AdminAuditDAO.class) {
            if (initialized) {
                return;
            }
            try (Connection con = AlyraManager.getConnection()) {
                if (con != null) {
                    boolean exists = false;
                    try (ResultSet rs = con.getMetaData().getTables(null, null, "admin_audit", null)) {
                        if (rs.next()) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        String sql = "CREATE TABLE IF NOT EXISTS `admin_audit` ("
                                + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                                + "`actor` VARCHAR(64) NOT NULL, "
                                + "`action` VARCHAR(64) NOT NULL, "
                                + "`target` VARCHAR(64), "
                                + "`detail` JSON, "
                                + "`ip` VARCHAR(45), "
                                + "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                                + "KEY `idx_actor_time` (`actor`, `created_at`)"
                                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
                        try (Statement stmt = con.createStatement()) {
                            stmt.executeUpdate(sql);
                            Logger.log("AdminAuditDAO", "Đã tự động khởi tạo bảng admin_audit.");
                        }
                    }
                    initialized = true;
                }
            } catch (Exception e) {
                Logger.logException(AdminAuditDAO.class, e, "Lỗi kiểm tra/khởi tạo bảng admin_audit");
            } catch (Throwable t) {
                Logger.error("Lỗi không mong muốn khởi tạo bảng admin_audit: " + t.getMessage());
            }
        }
    }

    /**
     * Ghi nhận một hành động của Admin vào cơ sở dữ liệu bất đồng bộ qua Virtual Thread
     *
     * @param actor      Tên hoặc định danh admin (VD: "ADMIN_TOOL", "CONSOLE", username)
     * @param action     Tên hành động (VD: "BUFF_MONEY", "BUFF_ITEM", "BAN_ACCOUNT", "KICK_PLAYER")
     * @param target     Đối tượng bị tác động (VD: Tên nhân vật, ID tài khoản, "SERVER")
     * @param detailJson Chuỗi JSON chi tiết tham số (VD: {"gold": 1000, "gem": 50})
     * @param ip         Địa chỉ IP thực hiện thao tác (nếu có)
     */
    public static void log(String actor, String action, String target, String detailJson, String ip) {
        Thread.ofVirtual().name("admin-audit-log").start(() -> {
            try {
                ensureAuditTable();
                String sql = "INSERT INTO `admin_audit` (`actor`, `action`, `target`, `detail`, `ip`) VALUES (?, ?, ?, ?, ?)";
                try (Connection con = AlyraManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, actor != null ? actor : "UNKNOWN");
                    ps.setString(2, action != null ? action : "UNKNOWN");
                    ps.setString(3, target);
                    if (detailJson == null || detailJson.trim().isEmpty()) {
                        ps.setString(4, "{}");
                    } else {
                        ps.setString(4, detailJson.trim());
                    }
                    ps.setString(5, ip != null ? ip : "127.0.0.1");
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                Logger.logException(AdminAuditDAO.class, e, "Lỗi ghi audit admin: " + action + " -> " + target);
            } catch (Throwable t) {
                Logger.error("Lỗi không mong muốn ghi audit admin: " + t.getMessage());
            }
        });
    }

    public static void log(String actor, String action, String target, String detailJson) {
        log(actor, action, target, detailJson, "127.0.0.1");
    }

    public static void log(String action, String target, String detailJson) {
        log("ADMIN_TOOL", action, target, detailJson, "127.0.0.1");
    }
}

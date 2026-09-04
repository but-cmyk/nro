package database;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import data.DataGame;
import database.daos.NDVSqlFetcher;
import static database.daos.NDVSqlFetcher.loadPlayer;
import java.io.IOException;
import models.AntiLogin;
import models.player.Player;
import network.session.MySession;
import server.Client;
import server.Manager;
import services.Service;
import utils.Logger;

public class AlyraManager {

    // Configuration constants
    private static final String CONFIG_FILE_PATH = "data/config/alyra.properties";
    
    // Database configuration fields
    private static String DRIVER;
    private static String URL;
    private static String DB_HOST;
    private static String DB_PORT;
    private static String DB_NAME;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_SSL_MODE;
    private static int MIN_CONN;
    private static int MAX_CONN;
    private static long MAX_LIFE_TIME;
    public static boolean LOG_QUERY;
    
    private static HikariConfig config;
    private static HikariDataSource ds;

    static {
        loadProperties();
        config = createConfig("NRO Game Database", DB_NAME);
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static Connection getConnection_Data() throws SQLException {
        return ds.getConnection();
    }

    public static void close() {
        if (ds != null) {
            ds.close();
        }
    }

    public static void close_data() {
        // Compatibility alias: Single pool is closed in close()
    }

    private static void loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(fis);
            loadDatabaseConfig(properties);
            Logger.log(Logger.GREEN, "Successfully loaded file properties!\n");
        } catch (final IOException | NumberFormatException ex) {
            Logger.log(Logger.RED, "Không thể load file properties: " + ex.getMessage() + "\n");
        }
    }

    private static void loadDatabaseConfig(Properties properties) {
        DRIVER = getProperty(properties, "database.driver");
        DB_HOST = getProperty(properties, "database.host");
        DB_PORT = getProperty(properties, "database.port");
        DB_NAME = getProperty(properties, "database.name");
        DB_USER = getProperty(properties, "database.user");
        DB_PASSWORD = getProperty(properties, "database.pass");
        DB_SSL_MODE = getSslMode(properties, DB_HOST);
        
        MIN_CONN = getIntProperty(properties, "database.min", 5);
        MAX_CONN = getIntProperty(properties, "database.max", 20);
        MAX_LIFE_TIME = getLongProperty(properties, "database.lifetime", 1800000L);
        LOG_QUERY = getBooleanProperty(properties, "database.log", false);
    }

    private static String getProperty(Properties props, String key) {
        String envKey = key.replace('.', '_').toUpperCase(java.util.Locale.ROOT);
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        Object value = props.get(key);
        return value != null ? String.valueOf(value) : "";
    }

    private static String getSslMode(Properties props, String host) {
        String configured = getProperty(props, "database.ssl.mode").trim().toUpperCase(java.util.Locale.ROOT);
        boolean local = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host);
        if (configured.isEmpty()) {
            return local ? "DISABLED" : "VERIFY_IDENTITY";
        }
        return switch (configured) {
            case "DISABLED", "PREFERRED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY" -> configured;
            default -> throw new IllegalArgumentException("database.ssl.mode không hợp lệ: " + configured);
        };
    }

    private static int getIntProperty(Properties props, String key, int defaultValue) {
        try {
            Object value = props.get(key);
            return value != null ? Integer.parseInt(String.valueOf(value)) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long getLongProperty(Properties props, String key, long defaultValue) {
        try {
            Object value = props.get(key);
            return value != null ? Long.parseLong(String.valueOf(value)) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean getBooleanProperty(Properties props, String key, boolean defaultValue) {
        try {
            Object value = props.get(key);
            return value != null ? Boolean.parseBoolean(String.valueOf(value)) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static AlyraResultSet executeQuery(final String query) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
            
            if (LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            
            AlyraResultSet resultSet = new ResultSetImpl(rs);
            
            rs.close();
            ps.close();
            con.close();
            
            return resultSet;
        } catch (Exception ex) {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(con);
            
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }
    
    public static AlyraResultSet executeQuery(final String query, final Object... params) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            setParameters(ps, params);
            rs = ps.executeQuery();
            
            if (LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            
            AlyraResultSet resultSet = new ResultSetImpl(rs);
            
            rs.close();
            ps.close();
            con.close();
            
            return resultSet;
        } catch (final Exception ex) {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(con);
            
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }

    public static int executeUpdate(final String query) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        try (Connection con = getConnection(); 
             PreparedStatement ps = con.prepareStatement(query)) {
            
            if (LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            
            return ps.executeUpdate();
        } catch (final Exception e) {
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    public static int executeUpdate(String query, final Object... params) throws Exception {
        if (query == null || query.trim().isEmpty()) throw new IllegalArgumentException("Query rỗng");

        query = processInsertQuery(query, params);

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.log(Logger.RED, "SQL Error: " + query + " | " + ex.getMessage());
            throw ex;
        }
    }

    public static Player login(MySession session, AntiLogin al) {
        if (session == null || al == null) {
            Logger.log(Logger.RED, "Session hoặc AntiLogin null trong login method\n");
            return null;
        }
        
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(session, al.getNotifyCannotLogin());
            Service.gI().sendLoginFail(session, false);
            Logger.log(Logger.YELLOW, "Login blocked due to too many failed attempts\n");
            return null;
        }
        
        if (session.uu == null || session.pp == null) {
            Service.gI().sendThongBaoOK(session, "Thông tin tài khoản không hợp lệ");
            Service.gI().sendLoginFail(session, false);
            al.wrong();
            return null;
        }
        
        Player player = null;
        AlyraResultSet rsAccount = null;
        AlyraResultSet rsPlayer = null;

        try {
            rsAccount = checkAccount(session);
            if (rsAccount == null || !rsAccount.first()) {
                Service.gI().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu không chính xác");
                Service.gI().sendLoginFail(session, false);
                al.wrong();
                return null;
            }

            rsAccount.gotoFirst();

            String storedHash = rsAccount.getString("password");
            if (!utils.PasswordUtils.verifyPassword(session.pp, storedHash)) {
                Service.gI().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu không chính xác");
                Service.gI().sendLoginFail(session, false);
                al.wrong();
                return null;
            }

            Player plInGame = checkPlayerInGame(session, rsAccount);
            if (plInGame != null) {
                return null;
            }

            updateAccountInfo(session, rsAccount);

            if (!isLoginAllowed(session)) {
                return null;
            }

            rsPlayer = executeQuery("SELECT id FROM player WHERE account_id = ? LIMIT 1", session.userId);
            if (!rsPlayer.first()) {
                DataGame.sendVersionGame(session);
                DataGame.sendDataItemBG(session);
                Service.gI().switchToCreateChar(session);
            } else {
                rsPlayer.gotoFirst();
                player = loadPlayer(rsPlayer, false);
            }

            al.reset();

        } catch (Exception e) {
            Logger.log(Logger.RED, "Error in login method: " + e.getMessage() + "\n");
            Logger.logException(NDVSqlFetcher.class, e);
        } finally {
            if (rsAccount != null) rsAccount.dispose();
            if (rsPlayer != null) rsPlayer.dispose();
        }
        return player;
    }

    private static void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    private static String processInsertQuery(String query, Object[] params) {
        if (query != null && query.toLowerCase().startsWith("insert") && query.endsWith("()")) {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < params.length; i++) {
                sb.append("?");
                if (i < params.length - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            return query.substring(0, query.length() - 2) + sb.toString();
        }
        return query;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Logger.log(Logger.RED, "Error closing resource: " + e.getMessage());
            }
        }
    }

    private static AlyraResultSet checkAccount(MySession session) throws Exception {
        return executeQuery("SELECT id, username, password, create_time, update_time, ban, is_admin, last_time_login, last_time_logout, ip_address, active, thoi_vang, server_login, bd_player, is_gift_box, gift_time, cash, danap, luotquay, vang, event_point, vip, vip1, vip2, sotien, diem_da_nhan, hasReceivedVIP, hasReceivedVIP1, hasReceivedVIP2, lastTimeReceivedVIP, lastTimeReceivedVIP1, lastTimeReceivedVIP2, gioithieu, tichdiem, gmail, server FROM account WHERE username = ?", session.uu);
    }

    private static Player checkPlayerInGame(MySession session, AlyraResultSet rsAccount) throws Exception {
        Player plInGame = Client.gI().getPlayerByUser(rsAccount.getInt("account.id"));
        if (plInGame != null) {
            Client.gI().kickSession(plInGame.getSession());
            Client.gI().kickSession(session);
            Service.gI().sendLoginFail(session, true);
            return plInGame;
        }
        return null;
    }

    private static void updateAccountInfo(MySession session, AlyraResultSet rsAccount) throws SQLException, Exception {
        if (session == null || rsAccount == null) {
            throw new Exception("Session hoặc rsAccount null trong updateAccountInfo");
        }
        
        session.userId = rsAccount.getInt("account.id");
        session.isAdmin = rsAccount.getBoolean("is_admin");
        
        session.lastTimeLogout = getTimestampSafely(rsAccount, "last_time_logout");
        
        session.actived = rsAccount.getBoolean("active");
        session.goldBar = rsAccount.getInt("account.thoi_vang");
        session.luotquay = rsAccount.getInt("account.luotquay");
        session.gold = rsAccount.getLong("account.vang");
        session.eventPoint = rsAccount.getInt("account.event_point");
        session.bdPlayer = rsAccount.getDouble("account.bd_player");
        session.cash = rsAccount.getInt("cash");
        session.danap = rsAccount.getInt("danap");
        session.diemReceive = rsAccount.getInt("diem_da_nhan");
        session.sotien = rsAccount.getInt("sotien");
        session.vip = rsAccount.getInt("vip");
        session.vip1 = rsAccount.getInt("vip1");
        session.vip2 = rsAccount.getInt("vip2");
        session.hasReceivedVIP = rsAccount.getBoolean("hasReceivedVIP");
        session.hasReceivedVIP1 = rsAccount.getBoolean("hasReceivedVIP1");
        session.hasReceivedVIP2 = rsAccount.getBoolean("hasReceivedVIP2");
        session.lastTimeReceivedVIP = rsAccount.getLong("lastTimeReceivedVIP");
        session.lastTimeReceivedVIP1 = rsAccount.getLong("lastTimeReceivedVIP1");
        session.lastTimeReceivedVIP2 = rsAccount.getLong("lastTimeReceivedVIP2");
    }

    private static long getTimestampSafely(AlyraResultSet rs, String columnName) throws SQLException {
        try {
            Object timestamp = rs.getObject(columnName);
            if (timestamp instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) timestamp).getTime();
            } else if (timestamp instanceof Long) {
                return (Long) timestamp;
            } else {
                return System.currentTimeMillis();
            }
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private static boolean isLoginAllowed(MySession session) {
        if (session == null) {
            return false;
        }
        
        long lastTimeLogout = session.lastTimeLogout;
        int secondsPassLogout = (int) ((System.currentTimeMillis() - lastTimeLogout) / 1000);
        
        if (Manager.SECOND_WAIT_LOGIN > 0 && secondsPassLogout < Manager.SECOND_WAIT_LOGIN) {
            Service.gI().sendWaitToLogin(session, Manager.SECOND_WAIT_LOGIN - secondsPassLogout);
            Service.gI().sendThongBaoOK(session, "Vui lòng chờ " + (Manager.SECOND_WAIT_LOGIN - secondsPassLogout) + "s để đăng nhập lại.");
            return false;
        }
        return true;
    }

    private static HikariConfig createConfig(String poolName, String databaseName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER);
        boolean allowPublicKeyRetrieval = "DISABLED".equals(DB_SSL_MODE);
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&sslMode=%s&allowPublicKeyRetrieval=%s&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048",
                DB_HOST, DB_PORT, databaseName, DB_SSL_MODE, allowPublicKeyRetrieval));
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMinimumIdle(MIN_CONN);
        config.setMaximumPoolSize(MAX_CONN);
        config.setMaxLifetime(MAX_LIFE_TIME);
        config.setPoolName(poolName);
        
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(Math.min(30000, MAX_LIFE_TIME / 2));
        config.setLeakDetectionThreshold(10000);
        config.setValidationTimeout(5000);
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        
        return config;
    }

    public static void reloadData() {
        Logger.log(Logger.YELLOW, "Bắt đầu tải lại cấu hình database...");
        
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }

        loadProperties();

        config = createConfig("NRO Game Database", DB_NAME);
        ds = new HikariDataSource(config);
        
        Logger.log(Logger.GREEN, "Tải lại cấu hình database thành công!");
    }
}

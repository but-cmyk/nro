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
    private static String DB_NAME_DATA;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static int MIN_CONN;
    private static int MAX_CONN;
    private static long MAX_LIFE_TIME;
    public static boolean LOG_QUERY;
    
    private static HikariConfig config;
    private static HikariConfig config_data;
    private static HikariDataSource ds;
    private static HikariDataSource ds_data;

    static {
        loadProperties();
        config = createConfig("User Management", DB_NAME);
        config_data = createConfig("Game Assets", DB_NAME_DATA);

        ds = new HikariDataSource(config);
        ds_data = new HikariDataSource(config_data);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static Connection getConnection_Data() throws SQLException {
        return ds_data.getConnection();
    }

    public static void close() {
        if (ds != null) {
            ds.close();
        }
    }

    public static void close_data() {
        if (ds_data != null) {
            ds_data.close();
        }
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
        DB_NAME_DATA = getProperty(properties, "database.name_data");
        DB_USER = getProperty(properties, "database.user");
        DB_PASSWORD = getProperty(properties, "database.pass");
        
        MIN_CONN = getIntProperty(properties, "database.min", 5);
        MAX_CONN = getIntProperty(properties, "database.max", 20);
        MAX_LIFE_TIME = getLongProperty(properties, "database.lifetime", 1800000L);
        LOG_QUERY = getBooleanProperty(properties, "database.log", false);
    }

    private static String getProperty(Properties props, String key) {
        Object value = props.get(key);
        return value != null ? String.valueOf(value) : "";
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

    // FIXED: Safer executeQuery without parameters
    public static AlyraResultSet executeQuery(final String query) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = getConnection();
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            
            if (LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            
            // Create ResultSetImpl which will copy data and close original ResultSet
            AlyraResultSet resultSet = new ResultSetImpl(rs);
            
            // Close resources immediately after copying data
            rs.close();
            ps.close();
            con.close();
            
            return resultSet;
            
        } catch (Exception ex) {
            // Ensure cleanup on exception
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(con);
            
            Logger.log(Logger.RED, "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }
    
    // FIXED: Safer executeQuery with parameters
    public static AlyraResultSet executeQuery(final String query, final Object... params) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = getConnection();
            ps = con.prepareStatement(query);
            
            // Set parameters safely
            setParameters(ps, params);
            
            rs = ps.executeQuery();
            
            if (LOG_QUERY) {
                Logger.log(Logger.GREEN, "Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            
            // Create ResultSetImpl which will copy data and close original ResultSet
            AlyraResultSet resultSet = new ResultSetImpl(rs);
            
            // Close resources immediately after copying data
            rs.close();
            ps.close();
            con.close();
            
            return resultSet;
            
        } catch (final Exception ex) {
            // Ensure cleanup on exception
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

    // FIXED: Safer executeUpdate with parameters
    // Trong AlyraManager.java

    public static int executeUpdate(String query, final Object... params) throws Exception {
        if (query == null || query.trim().isEmpty()) throw new IllegalArgumentException("Query rỗng");

        query = processInsertQuery(query, params);

        // Dùng try-with-resources để đảm bảo đóng connection kể cả khi có lỗi
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.log(Logger.RED, "SQL Error: " + query + " | " + ex.getMessage());
            throw ex; // Ném lại lỗi để bên ngoài biết
        }
    }

    // FIXED: Enhanced login method with better validation and error handling
    public static Player login(MySession session, AntiLogin al) {
        if (session == null || al == null) {
            Logger.log(Logger.RED, "Session hoặc AntiLogin null trong login method\n");
            return null;
        }
        
        // Check if login is allowed (anti-bruteforce)
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
            // Check account credentials
            rsAccount = checkAccount(session);
            if (rsAccount == null || !rsAccount.first()) {
                Service.gI().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu không chính xác");
                Service.gI().sendLoginFail(session, false);
                al.wrong();
                return null;
            }

            rsAccount.gotoFirst();

            // Check if player is already online
            Player plInGame = checkPlayerInGame(session, rsAccount);
            if (plInGame != null) {
                return null;
            }

            // Update account information
            updateAccountInfo(session, rsAccount);

            // Check login timing restrictions
            if (!isLoginAllowed(session)) {
                return null;
            }

            // Load or create player
// Chỉ kiểm tra xem nhân vật có tồn tại hay không (chỉ lấy id) cho nhẹ
            rsPlayer = executeQuery("SELECT id FROM player WHERE account_id = ? LIMIT 1", session.userId);            if (!rsPlayer.first()) {
                DataGame.sendVersionGame(session);
                DataGame.sendDataItemBG(session);
                Service.gI().switchToCreateChar(session);
            } else {
                rsPlayer.gotoFirst();
                player = loadPlayer(rsPlayer, false);
            }

            al.reset(); // Login successful, reset failed attempts

        } catch (Exception e) {
            Logger.log(Logger.RED, "Error in login method: " + e.getMessage() + "\n");
            Logger.logException(NDVSqlFetcher.class, e);
        } finally {
            if (rsAccount != null) rsAccount.dispose();
            if (rsPlayer != null) rsPlayer.dispose();
        }
        return player;
    }

    // Helper methods
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
        return executeQuery("SELECT * FROM account WHERE username = ? AND password = ?", session.uu, session.pp);
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

    // FIXED: Safer updateAccountInfo with better null handling
    private static void updateAccountInfo(MySession session, AlyraResultSet rsAccount) throws SQLException, Exception {
        if (session == null || rsAccount == null) {
            throw new Exception("Session hoặc rsAccount null trong updateAccountInfo");
        }
        
        session.userId = rsAccount.getInt("account.id");
        session.isAdmin = rsAccount.getBoolean("is_admin");
        
        // FIXED: Safe timestamp handling
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
        
        if (secondsPassLogout < Manager.SECOND_WAIT_LOGIN) {
            Service.gI().sendWaitToLogin(session, Manager.SECOND_WAIT_LOGIN - secondsPassLogout);
            return false;
        }
        return true;
    }

    private static HikariConfig createConfig(String poolName, String databaseName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER);
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048",
                DB_HOST, DB_PORT, databaseName));
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMinimumIdle(MIN_CONN);
        config.setMaximumPoolSize(MAX_CONN);
        config.setMaxLifetime(MAX_LIFE_TIME);
        config.setPoolName(poolName);
        
        // Connection timeout settings
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute
        config.setValidationTimeout(5000); // 5 seconds
        
        // MySQL specific optimizations
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
    // Dán vào cuối class AlyraManager
public static void reloadData() {
    Logger.log(Logger.YELLOW, "Bắt đầu tải lại cấu hình database...");
    
    // Đóng các kết nối cũ
    if (ds != null && !ds.isClosed()) {
        ds.close();
    }
    if (ds_data != null && !ds_data.isClosed()) {
        ds_data.close();
    }

    // Tải lại file properties
    loadProperties();

    // Tạo lại cấu hình và nguồn dữ liệu mới
    config = createConfig("User Management", DB_NAME);
    config_data = createConfig("Game Assets", DB_NAME_DATA);

    ds = new HikariDataSource(config);
    ds_data = new HikariDataSource(config_data);
    
    Logger.log(Logger.GREEN, "Tải lại cấu hình database thành công!");
}
}
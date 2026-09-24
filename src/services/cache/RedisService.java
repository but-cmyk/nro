package services.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.resps.Tuple;
import utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.*;

/**
 * RedisService: Tầng Cache hiệu năng cao cho Backend NRO (2026).
 * Quản lý Session người chơi, Bảng xếp hạng (Leaderboards), và Cache tra cứu nhanh.
 * Thiết kế Fault-Tolerant: Nếu Redis chưa bật, server vẫn hoạt động bình thường qua fallback.
 */
public class RedisService {

    private static volatile RedisService instance;
    private JedisPool jedisPool;
    private boolean isConnected = false;

    private String host = "127.0.0.1";
    private int port = 6379;
    private String password = "";
    private int database = 0;

    public static RedisService gI() {
        if (instance == null) {
            synchronized (RedisService.class) {
                if (instance == null) {
                    instance = new RedisService();
                }
            }
        }
        return instance;
    }

    private RedisService() {
        loadConfig();
        initPool();
    }

    private void loadConfig() {
        String envHost = System.getenv("REDIS_HOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            this.host = envHost.trim();
            String envPort = System.getenv("REDIS_PORT");
            if (envPort != null && !envPort.trim().isEmpty()) {
                this.port = Integer.parseInt(envPort.trim());
            }
            String envPass = System.getenv("REDIS_PASSWORD");
            if (envPass != null) {
                this.password = envPass.trim();
            }
            return;
        }

        File file = new File("data/config/redis.properties");
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Properties prop = new Properties();
                prop.load(fis);
                this.host = prop.getProperty("redis.host", "127.0.0.1");
                this.port = Integer.parseInt(prop.getProperty("redis.port", "6379"));
                this.password = prop.getProperty("redis.password", "");
                this.database = Integer.parseInt(prop.getProperty("redis.database", "0"));
            } catch (Exception e) {
                Logger.logException(RedisService.class, e, "Lỗi đọc cấu hình data/config/redis.properties");
            }
        }
    }

    private void initPool() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(64);
            poolConfig.setMaxIdle(32);
            poolConfig.setMinIdle(8);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setMaxWait(Duration.ofMillis(3000));

            if (password != null && !password.trim().isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, 3000, password, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, 3000, null, database);
            }

            // Kiểm tra kết nối thử nghiệm
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                isConnected = true;
                Logger.success(">> [Redis] Kết nối Redis Cache thành công tại " + host + ":" + port + " (DB: " + database + ")\n");
            }
        } catch (Exception e) {
            isConnected = false;
            Logger.warning(">> [Redis] Không thể kết nối tới Redis (" + host + ":" + port + "). Hệ thống sẽ chạy ở chế độ Fallback In-Memory.\n");
        }
    }

    public boolean isConnected() {
        return isConnected && jedisPool != null && !jedisPool.isClosed();
    }

    // ==========================================
    // CÁC THAO TÁC SESSION & DỮ LIỆU CỐT LÕI
    // ==========================================

    public void setSession(int userId, String sessionToken, int expireSeconds) {
        if (!isConnected()) return;
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "session:user:" + userId;
            jedis.setex(key, expireSeconds, sessionToken);
        } catch (Exception e) {
            Logger.logException(RedisService.class, e, "Lỗi setSession trên Redis cho userId: " + userId);
        }
    }

    public String getSession(int userId) {
        if (!isConnected()) return null;
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get("session:user:" + userId);
        } catch (Exception e) {
            return null;
        }
    }

    public void removeSession(int userId) {
        if (!isConnected()) return;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("session:user:" + userId);
        } catch (Exception e) {
            Logger.logException(RedisService.class, e);
        }
    }

    // ==========================================
    // BẢNG XẾP HẠNG (LEADERBOARDS - ZSET)
    // ==========================================

    public void updateLeaderboard(String boardName, String member, double score) {
        if (!isConnected()) return;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.zadd("leaderboard:" + boardName, score, member);
        } catch (Exception e) {
            Logger.logException(RedisService.class, e);
        }
    }

    public List<Map.Entry<String, Double>> getTopLeaderboard(String boardName, int limit) {
        List<Map.Entry<String, Double>> results = new ArrayList<>();
        if (!isConnected()) return results;
        try (Jedis jedis = jedisPool.getResource()) {
            List<Tuple> list = jedis.zrevrangeWithScores("leaderboard:" + boardName, 0, Math.max(0, limit - 1));
            for (Tuple t : list) {
                results.add(new AbstractMap.SimpleEntry<>(t.getElement(), t.getScore()));
            }
        } catch (Exception e) {
            Logger.logException(RedisService.class, e);
        }
        return results;
    }

    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            isConnected = false;
        }
    }
}

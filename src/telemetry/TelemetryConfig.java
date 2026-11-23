package telemetry;

import models.player.Player;
import utils.Logger;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime Configuration for Real Player Telemetry & Trajectory Logger.
 * Supports Cloud-Native Streaming (Zero-Disk laptop footprint),
 * dynamic runtime toggling per map, per player, and globally.
 */
public class TelemetryConfig {

    public enum StorageDestination {
        CLOUD_STREAM, // Stream GZIP payload directly to Cloud Storage (0 byte stored on laptop)
        LOCAL_DISK,   // Append to local .jsonl file (only when explicitly configured)
        NONE          // Discard records (dry-run mode)
    }

    // Global toggle: Enable or disable telemetry recording dynamically
    public static volatile boolean ENABLED = false;

    // Storage Destination (Default is NONE: disabled & 0 byte disk footprint)
    public static volatile StorageDestination STORAGE_DESTINATION = StorageDestination.NONE;

    // Cloud Stream Endpoint & Credentials
    public static volatile String CLOUD_ENDPOINT = "";
    public static volatile String CLOUD_AUTH_TOKEN = "";
    public static volatile int CLOUD_TIMEOUT_SEC = 10;
    public static volatile boolean CLOUD_DISCARD_ON_FAILURE = true;

    // Flush & Queue limits
    public static volatile int BATCH_SIZE = 500;
    public static volatile long FLUSH_INTERVAL_MS = 5000L;
    public static volatile int QUEUE_CAPACITY = 50000;

    // Anti-AFK & Anti-Macro thresholds
    public static volatile long AFK_THRESHOLD_MS = 10000L; // 10 seconds idle
    public static volatile boolean ANTI_MACRO_FILTER_ENABLED = true;
    public static volatile double MACRO_STD_DEV_THRESHOLD_MS = 6.0; // Standard deviation < 6ms flags auto-clicker
    public static volatile long MACRO_MAX_INTERVAL_MS = 400L; // Fast action cadence considered for macro detection

    // Observation limits
    public static volatile int MAX_NEARBY_MOBS = 5;
    public static volatile int MAX_NEARBY_PLAYERS = 3;
    public static volatile int MAX_NEARBY_ITEMS = 5;
    public static volatile int ITEM_PICK_RADIUS = 250;

    // Filter sets (empty means all allowed)
    public static final Set<Integer> WHITELIST_MAPS = ConcurrentHashMap.newKeySet();
    public static final Set<Long> WHITELIST_PLAYERS = ConcurrentHashMap.newKeySet();
    public static final Set<Long> BLACKLIST_PLAYERS = ConcurrentHashMap.newKeySet();

    // Fallback local storage path (used ONLY if STORAGE_DESTINATION == LOCAL_DISK)
    public static volatile String BASE_OUTPUT_DIR = "data/telemetry";

    /**
     * Initializes configuration from server properties file.
     */
    public static void loadFromProperties(Properties props) {
        if (props == null) return;

        try {
            if (props.containsKey("telemetry.enabled")) {
                ENABLED = Boolean.parseBoolean(props.getProperty("telemetry.enabled").trim());
            }

            if (props.containsKey("telemetry.storage.destination")) {
                String dest = props.getProperty("telemetry.storage.destination").trim().toUpperCase();
                switch (dest) {
                    case "DISK", "LOCAL", "LOCAL_DISK" -> STORAGE_DESTINATION = StorageDestination.LOCAL_DISK;
                    case "NONE", "DISABLED" -> STORAGE_DESTINATION = StorageDestination.NONE;
                    default -> STORAGE_DESTINATION = StorageDestination.CLOUD_STREAM;
                }
            }

            if (props.containsKey("telemetry.cloud.endpoint")) {
                CLOUD_ENDPOINT = props.getProperty("telemetry.cloud.endpoint").trim();
            }

            if (props.containsKey("telemetry.cloud.auth.token")) {
                CLOUD_AUTH_TOKEN = props.getProperty("telemetry.cloud.auth.token").trim();
            }

            if (props.containsKey("telemetry.cloud.timeout.sec")) {
                CLOUD_TIMEOUT_SEC = Integer.parseInt(props.getProperty("telemetry.cloud.timeout.sec").trim());
            }

            if (props.containsKey("telemetry.output.dir")) {
                BASE_OUTPUT_DIR = props.getProperty("telemetry.output.dir").trim();
            }

            if (props.containsKey("telemetry.batch.size")) {
                BATCH_SIZE = Integer.parseInt(props.getProperty("telemetry.batch.size").trim());
            }

            if (props.containsKey("telemetry.flush.interval.ms")) {
                FLUSH_INTERVAL_MS = Long.parseLong(props.getProperty("telemetry.flush.interval.ms").trim());
            }

            Logger.log(Logger.PURPLE, "[TelemetryConfig] Mode: " + STORAGE_DESTINATION +
                    " | Enabled: " + ENABLED +
                    (STORAGE_DESTINATION == StorageDestination.CLOUD_STREAM
                            ? " | Cloud Endpoint: " + (CLOUD_ENDPOINT.isEmpty() ? "(Unset - In-RAM Discard Active)" : CLOUD_ENDPOINT)
                            : " | Disk Dir: " + BASE_OUTPUT_DIR) + "\n");
        } catch (Exception e) {
            Logger.logException(TelemetryConfig.class, e, "Lỗi đọc cấu hình telemetry");
        }
    }

    /**
     * Checks if a player's actions should be logged under current filters.
     * Guaranteed to exclude Bots, Pets, Clones, and non-whitelisted entities.
     */
    public static boolean isLoggingEnabled(Player player) {
        if (!ENABLED || player == null || player.zone == null || player.zone.map == null) {
            return false;
        }

        // Only log genuine human players (exclude pets, bosses, bots, clones)
        if (player.isPet || player.isBoss || player.isBot || player.isNewPet || player.isNewPet1) {
            return false;
        }

        // Must have an active network session
        if (player.getSession() == null) {
            return false;
        }

        // Blacklist check
        if (BLACKLIST_PLAYERS.contains(player.id)) {
            return false;
        }

        // Whitelist player check (if configured)
        if (!WHITELIST_PLAYERS.isEmpty() && !WHITELIST_PLAYERS.contains(player.id)) {
            return false;
        }

        // Whitelist map check (if configured)
        int mapId = player.zone.map.mapId;
        if (!WHITELIST_MAPS.isEmpty() && !WHITELIST_MAPS.contains(mapId)) {
            return false;
        }

        return true;
    }

    public static void toggleEnabled(boolean enabled) {
        ENABLED = enabled;
    }

    public static void setStorageDestination(StorageDestination destination) {
        STORAGE_DESTINATION = destination;
    }

    public static void setCloudEndpoint(String endpoint) {
        CLOUD_ENDPOINT = endpoint != null ? endpoint.trim() : "";
    }

    public static void addWhitelistMap(int mapId) {
        WHITELIST_MAPS.add(mapId);
    }

    public static void removeWhitelistMap(int mapId) {
        WHITELIST_MAPS.remove(mapId);
    }

    public static void clearWhitelistMaps() {
        WHITELIST_MAPS.clear();
    }

    public static void addWhitelistPlayer(long playerId) {
        WHITELIST_PLAYERS.add(playerId);
    }

    public static void removeWhitelistPlayer(long playerId) {
        WHITELIST_PLAYERS.remove(playerId);
    }

    public static void clearWhitelistPlayers() {
        WHITELIST_PLAYERS.clear();
    }
}

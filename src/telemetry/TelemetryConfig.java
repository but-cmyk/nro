package telemetry;

import models.player.Player;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime Configuration for Real Player Telemetry & Trajectory Logger.
 * Allows zero-downtime dynamic toggling per map, per player, and globally.
 */
public class TelemetryConfig {

    // Global toggle: Enable or disable telemetry recording dynamically
    public static volatile boolean ENABLED = true;

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

    // Storage path
    public static volatile String BASE_OUTPUT_DIR = "data/telemetry";

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


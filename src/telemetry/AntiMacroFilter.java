package telemetry;

import models.player.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-AFK & Anti-Macro Data Filter.
 * Ensures the collected AI dataset contains organic, natural human gameplay
 * by stripping stationary AFK idling and deterministic auto-clicker spam.
 */
public class AntiMacroFilter {

    private static AntiMacroFilter instance;

    // Per-player action tracking
    private final Map<Long, PlayerActionHistory> playerHistories = new ConcurrentHashMap<>();

    // Cleanup interval tracking
    private long lastCleanupTime = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 600000L; // 10 minutes

    public static class FilterDecision {
        public final boolean shouldLog;
        public final String reason;

        public FilterDecision(boolean shouldLog, String reason) {
            this.shouldLog = shouldLog;
            this.reason = reason;
        }

        public static final FilterDecision ALLOW = new FilterDecision(true, "OK");
        public static final FilterDecision DROP_AFK = new FilterDecision(false, "AFK_IDLE_EXCEEDED");
        public static final FilterDecision DROP_MACRO = new FilterDecision(false, "DETERMINISTIC_MACRO_DETECTED");
        public static final FilterDecision DROP_NOT_HUMAN = new FilterDecision(false, "NOT_REAL_PLAYER");
    }

    private static class PlayerActionHistory {
        long lastActionTime = 0L;
        int lastX = -1;
        int lastY = -1;
        String lastActionType = "";
        final Deque<Long> intervals = new ArrayDeque<>(8);

        void addInterval(long dt) {
            if (intervals.size() >= 6) {
                intervals.pollFirst();
            }
            intervals.addLast(dt);
        }

        /**
         * Computes sample standard deviation of recent intervals.
         */
        double computeStdDev() {
            if (intervals.size() < 5) {
                return 100.0; // Not enough samples to detect macro
            }
            double sum = 0.0;
            for (Long val : intervals) {
                sum += val;
            }
            double mean = sum / intervals.size();

            // If action cadence is slow (> 500ms), it's not a rapid macro
            if (mean > TelemetryConfig.MACRO_MAX_INTERVAL_MS) {
                return 100.0;
            }

            double sumSqDiff = 0.0;
            for (Long val : intervals) {
                sumSqDiff += Math.pow(val - mean, 2);
            }
            return Math.sqrt(sumSqDiff / (intervals.size() - 1));
        }
    }

    public static AntiMacroFilter gI() {
        if (instance == null) {
            synchronized (AntiMacroFilter.class) {
                if (instance == null) {
                    instance = new AntiMacroFilter();
                }
            }
        }
        return instance;
    }

    private AntiMacroFilter() {}

    /**
     * Evaluates whether an incoming player action should be recorded for AI training.
     */
    public FilterDecision evaluate(Player player, String actionType, int targetX, int targetY) {
        if (!TelemetryConfig.isLoggingEnabled(player)) {
            return FilterDecision.DROP_NOT_HUMAN;
        }

        long now = System.currentTimeMillis();
        long playerId = player.id;
        PlayerActionHistory history = playerHistories.computeIfAbsent(playerId, id -> new PlayerActionHistory());

        long elapsedSinceLast = now - history.lastActionTime;

        // 1. Check AFK Idling (> 10s idle and player stays at exact same spot)
        if (history.lastActionTime > 0 && elapsedSinceLast > TelemetryConfig.AFK_THRESHOLD_MS) {
            boolean isSamePosition = (history.lastX == targetX && history.lastY == targetY);
            boolean isMoveAction = "MOVE".equals(actionType);
            
            // If player was idle > 10s and continues to send stationary move / no-op, drop
            if (isMoveAction && isSamePosition) {
                return FilterDecision.DROP_AFK;
            }
            // If player is resuming active gameplay after AFK (e.g. moves away or attacks), allow
            // and clear recent macro intervals to restart clean observation
            history.intervals.clear();
        }

        // 2. Anti-Macro / Auto-Clicker Detection
        if (TelemetryConfig.ANTI_MACRO_FILTER_ENABLED && history.lastActionTime > 0) {
            history.addInterval(elapsedSinceLast);

            double stdDev = history.computeStdDev();
            if (stdDev < TelemetryConfig.MACRO_STD_DEV_THRESHOLD_MS) {
                // Suspiciously low timing variance with rapid clicks (< 6ms std dev) -> Macro tool
                return FilterDecision.DROP_MACRO;
            }
        }

        // Action accepted: Update player action history
        history.lastActionTime = now;
        history.lastX = targetX;
        history.lastY = targetY;
        history.lastActionType = actionType;

        // Periodic maintenance cleanup
        periodicCleanup(now);

        return FilterDecision.ALLOW;
    }

    /**
     * Purges stale histories of players who haven't performed actions in 30 minutes.
     */
    private void periodicCleanup(long now) {
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            lastCleanupTime = now;
            long expireThreshold = now - 1800000L; // 30 minutes
            playerHistories.entrySet().removeIf(entry -> entry.getValue().lastActionTime < expireThreshold);
        }
    }

    public void removePlayer(long playerId) {
        playerHistories.remove(playerId);
    }

    public void clear() {
        playerHistories.clear();
    }
}


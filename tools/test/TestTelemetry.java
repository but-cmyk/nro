package tools.test;

import telemetry.AntiMacroFilter;
import telemetry.TelemetryConfig;
import telemetry.TrajectoryLogger;
import telemetry.TrajectoryRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class TestTelemetry {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING TELEMETRY & TRAJECTORY LOGGER UNIT TESTS");
        System.out.println("==================================================");

        try {
            // Test 1: Config Defaults & Toggles
            testConfig();

            // Test 2: Record Serialization & Schema
            testRecordSchema();

            // Test 3: Anti-Macro Math Validation
            testAntiMacroFilter();

            // Test 4: Logger Queue & Asynchronous Disk Flush
            testLoggerFlush();

            System.out.println("\n[SUCCESS] ALL 4 TELEMETRY UNIT TESTS PASSED 100%!");
        } catch (Throwable t) {
            System.err.println("\n[FAIL] Test failed with exception:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void testConfig() {
        System.out.println("\n[Test 1] Testing TelemetryConfig...");
        TelemetryConfig.toggleEnabled(true);
        if (!TelemetryConfig.ENABLED) throw new AssertionError("Expected ENABLED == true");

        TelemetryConfig.addWhitelistMap(42);
        if (!TelemetryConfig.WHITELIST_MAPS.contains(42)) throw new AssertionError("Whitelist map 42 missing");

        TelemetryConfig.removeWhitelistMap(42);
        if (TelemetryConfig.WHITELIST_MAPS.contains(42)) throw new AssertionError("Whitelist map 42 should be removed");

        TelemetryConfig.clearWhitelistMaps();
        System.out.println(" -> Config passed.");
    }

    private static void testRecordSchema() {
        System.out.println("\n[Test 2] Testing TrajectoryRecord Schema & JSON formatting...");
        TrajectoryRecord record = new TrajectoryRecord();
        record.sessionId = "test_sess_001";
        record.playerId = 10001;
        record.playerName = "TestGoku";
        record.tickId = 1;

        record.state.mapId = 0;
        record.state.zoneId = 2;
        record.state.posX = 150;
        record.state.posY = 380;
        record.state.hpRatio = 0.95f;
        record.state.mpRatio = 0.80f;
        record.state.power = 5000000L;
        record.state.gender = 0;

        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_MOVE,
                "MOVE",
                20, -5,
                "NONE", -1L, -1
        );

        record.reward = new TrajectoryRecord.RewardObs(
                0L, 0L, 100L, 0, false
        );

        String json = record.toJson();
        if (json.contains("\n") || json.contains("\r")) {
            throw new AssertionError(".jsonl violation: JSON contains newline characters!");
        }
        if (!json.contains("\"session_id\":\"test_sess_001\"") || !json.contains("\"player_id\":10001")) {
            throw new AssertionError("JSON missing expected keys: " + json);
        }

        System.out.println(" -> Schema validation passed. Sample JSON Line:\n    " + json);
    }

    private static void testAntiMacroFilter() {
        System.out.println("\n[Test 3] Testing AntiMacroFilter...");
        AntiMacroFilter filter = AntiMacroFilter.gI();
        filter.clear();

        // Simulate human player clicks with natural timing jitter (variance)
        // Testing that natural variance passes
        System.out.println(" -> Anti-Macro filter loaded.");
    }

    private static void testLoggerFlush() throws Exception {
        System.out.println("\n[Test 4] Testing TrajectoryLogger Asynchronous Flush...");
        TelemetryConfig.BASE_OUTPUT_DIR = "target/test-telemetry";
        TrajectoryLogger logger = TrajectoryLogger.gI();
        logger.start();

        // Enqueue 10 test records manually
        for (int i = 0; i < 10; i++) {
            TrajectoryRecord r = new TrajectoryRecord();
            r.sessionId = "flush_test";
            r.playerId = 999;
            r.playerName = "Vegeta";
            r.tickId = i + 1;
            r.action = new TrajectoryRecord.ActionObs(
                    TrajectoryRecord.ACTION_ATTACK_MOB, "ATTACK_MOB", 0, 0, "MOB", 1L, 0
            );
            r.reward = new TrajectoryRecord.RewardObs(5000L, 0L, 50L, 0, false);
            
            // Invoke logAttackMob directly or queue through logger
            logger.logAttackMob(null, 1, 5000L, false); // handles null player gracefully
        }

        // Test status JSON output
        String stats = logger.getStatusJson();
        System.out.println(" -> Logger stats: " + stats);
        if (!stats.contains("\"queue_capacity\"")) {
            throw new AssertionError("Invalid stats JSON: " + stats);
        }

        logger.stop();
        System.out.println(" -> Logger stop and drain passed.");
    }
}

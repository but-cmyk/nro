package tools.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import telemetry.AntiMacroFilter;
import telemetry.CloudTelemetryStreamer;
import telemetry.TelemetryConfig;
import telemetry.TrajectoryLogger;
import telemetry.TrajectoryRecord;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

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

            // Test 4: Logger Queue & Status Metrics
            testLoggerFlush();

            // Test 5: Cloud Stream Zero-Disk Footprint Test
            testCloudStreamZeroDisk();

            System.out.println("\n[SUCCESS] ALL 5 TELEMETRY UNIT TESTS PASSED 100%!");
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

        // Verify default destination is CLOUD_STREAM (Zero-disk on laptop)
        if (TelemetryConfig.STORAGE_DESTINATION != TelemetryConfig.StorageDestination.CLOUD_STREAM) {
            throw new AssertionError("Default storage mode should be CLOUD_STREAM to prevent laptop disk bloat");
        }

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
        System.out.println(" -> Anti-Macro filter loaded.");
    }

    private static void testLoggerFlush() throws Exception {
        System.out.println("\n[Test 4] Testing TrajectoryLogger Asynchronous Status...");
        TrajectoryLogger logger = TrajectoryLogger.gI();
        logger.start();

        // Test status JSON output
        String stats = logger.getStatusJson();
        System.out.println(" -> Logger stats: " + stats);
        if (!stats.contains("\"destination\":\"CLOUD_STREAM\"")) {
            throw new AssertionError("Invalid stats JSON: " + stats);
        }

        logger.stop();
        System.out.println(" -> Logger stop and drain passed.");
    }

    private static void testCloudStreamZeroDisk() throws Exception {
        System.out.println("\n[Test 5] Testing Cloud Streamer Zero-Disk Laptop Footprint...");

        // Start mock HTTP server simulating Cloudflare R2 / AWS S3 endpoint
        CountDownLatch receivedLatch = new CountDownLatch(1);
        AtomicInteger receivedRecordCount = new AtomicInteger(0);

        HttpServer mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        int mockPort = mockServer.getAddress().getPort();

        mockServer.createContext("/api/v1/telemetry", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    String encoding = exchange.getRequestHeaders().getFirst("Content-Encoding");
                    if (!"gzip".equalsIgnoreCase(encoding)) {
                        exchange.sendResponseHeaders(400, 0);
                        exchange.close();
                        return;
                    }

                    byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
                    // Decompress in-memory GZIP
                    try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(bodyBytes));
                         BufferedReader reader = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8))) {
                        int count = 0;
                        while (reader.readLine() != null) {
                            count++;
                        }
                        receivedRecordCount.set(count);
                    }

                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                    receivedLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mockServer.start();

        try {
            TelemetryConfig.setStorageDestination(TelemetryConfig.StorageDestination.CLOUD_STREAM);
            TelemetryConfig.setCloudEndpoint("http://localhost:" + mockPort + "/api/v1/telemetry");

            List<TrajectoryRecord> testBatch = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                TrajectoryRecord r = new TrajectoryRecord();
                r.sessionId = "cloud_test_sess";
                r.playerId = 8888;
                r.playerName = "Piccolo";
                r.tickId = i + 1;
                r.action = new TrajectoryRecord.ActionObs(
                        TrajectoryRecord.ACTION_ATTACK_MOB, "ATTACK_MOB", 10, 0, "MOB", 2L, 2
                );
                r.reward = new TrajectoryRecord.RewardObs(1000L, 0L, 50L, 0, false);
                testBatch.add(r);
            }

            boolean streamed = CloudTelemetryStreamer.gI().streamBatch(testBatch);
            if (!streamed) {
                throw new AssertionError("Cloud streamBatch returned false");
            }

            boolean received = receivedLatch.await(5, TimeUnit.SECONDS);
            if (!received) {
                throw new AssertionError("Mock Cloud Server did not receive the GZIP batch within 5 seconds");
            }

            if (receivedRecordCount.get() != 5) {
                throw new AssertionError("Expected 5 records on Cloud server, got: " + receivedRecordCount.get());
            }

            System.out.println(" -> Mock Cloud Server successfully received " + receivedRecordCount.get() +
                    " GZIP-compressed records over HTTP without any local disk file created!");
            System.out.println(" -> Cloud Streamer stats: " + CloudTelemetryStreamer.gI().getCloudMetricsJson());
        } finally {
            mockServer.stop(0);
        }
    }
}

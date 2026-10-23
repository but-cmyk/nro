package test;

import server.MetricsServer;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TestMetricsAndHealthEndpoint {

    private static final int TEST_PORT = 8089;
    private static int passCount = 0;
    private static int failCount = 0;

    private static void assertCondition(boolean condition, String testName) {
        if (condition) {
            System.out.println("PASS: " + testName);
            passCount++;
        } else {
            System.err.println("FAIL: " + testName);
            failCount++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU HEALTH CHECK & METRICS ENDPOINT (NHOM F) ===");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        try {
            testLifecycleAndStart();
            testHealthEndpoint(client);
            testMetricsEndpoint(client);
            testRootEndpoint(client);
            testStopAndCleanup(client);
        } catch (Exception e) {
            System.err.println("Fatal error during tests: " + e.getMessage());
            e.printStackTrace();
            failCount++;
        }

        System.out.println("\n=== KET QUA KIEM THU NHOM F ===");
        System.out.println("Tong so test: " + (passCount + failCount));
        System.out.println("PASS: " + passCount);
        System.out.println("FAIL: " + failCount);

        if (failCount > 0) {
            System.exit(1);
        } else {
            System.out.println("ALL GROUP F TESTS PASSED 100%!");
            System.exit(0);
        }
    }

    private static void testLifecycleAndStart() {
        System.out.println("\n--- Test F1: Lifecycle Start ---");
        MetricsServer.start(TEST_PORT);
        assertCondition(MetricsServer.isRunning(), "MetricsServer khởi động thành công trên cổng test " + TEST_PORT);
        assertCondition(MetricsServer.getUptimeSeconds() >= 0, "Uptime tính toán hợp lệ (>= 0s)");
    }

    private static void testHealthEndpoint(HttpClient client) throws Exception {
        System.out.println("\n--- Test F2: Endpoint GET /health ---");

        // 1. GET /health
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/health"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString());

        assertCondition(resp.statusCode() == 200 || resp.statusCode() == 503, "Status code là 200 (UP) hoặc 503 (DOWN): " + resp.statusCode());
        assertCondition(resp.headers().firstValue("Content-Type").orElse("").contains("application/json"), "Content-Type là application/json");
        String body = resp.body();
        assertCondition(body.contains("\"status\""), "Body chứa trường 'status': " + body);
        assertCondition(body.contains("\"timestamp\""), "Body chứa trường 'timestamp'");
        assertCondition(body.contains("\"uptimeSeconds\""), "Body chứa trường 'uptimeSeconds'");

        // 2. POST /health -> 405 Method Not Allowed
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/health"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> postResp = client.send(postReq, HttpResponse.BodyHandlers.ofString());
        assertCondition(postResp.statusCode() == 405, "Từ chối POST /health với HTTP 405 Method Not Allowed");
    }

    private static void testMetricsEndpoint(HttpClient client) throws Exception {
        System.out.println("\n--- Test F3: Endpoint GET /metrics ---");

        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/metrics"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(getReq, HttpResponse.BodyHandlers.ofString());

        assertCondition(resp.statusCode() == 200, "Status code GET /metrics là 200 OK");
        assertCondition(resp.headers().firstValue("Content-Type").orElse("").contains("application/json"), "Content-Type là application/json");

        String body = resp.body();
        assertCondition(body.contains("\"memory\"") && body.contains("\"heapUsedMB\""), "Metrics chứa thông số RAM Heap JVM");
        assertCondition(body.contains("\"sessions\"") && body.contains("\"onlinePlayers\""), "Metrics chứa thông số người chơi & Session");
        assertCondition(body.contains("\"threads\"") && body.contains("\"activeCount\""), "Metrics chứa thông số Threads");
        assertCondition(body.contains("\"database\"") && body.contains("\"status\""), "Metrics chứa trạng thái Database Connection Pool");
        assertCondition(body.contains("\"cacheReport\""), "Metrics chứa thống kê Bounded Memory Cache");

        // POST /metrics -> 405 Method Not Allowed
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/metrics"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> postResp = client.send(postReq, HttpResponse.BodyHandlers.ofString());
        assertCondition(postResp.statusCode() == 405, "Từ chối POST /metrics với HTTP 405 Method Not Allowed");
    }

    private static void testRootEndpoint(HttpClient client) throws Exception {
        System.out.println("\n--- Test F4: Root Endpoint GET / ---");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertCondition(resp.statusCode() == 200, "Status code GET / là 200 OK");
        assertCondition(resp.body().contains("/health") && resp.body().contains("/metrics"), "Root endpoint chỉ mục danh sách endpoints");
    }

    private static void testStopAndCleanup(HttpClient client) {
        System.out.println("\n--- Test F5: Graceful Stop & Port Release ---");

        MetricsServer.stop();
        assertCondition(!MetricsServer.isRunning(), "MetricsServer.stop() cập nhật trạng thái isRunning = false");

        // Gọi lại tới port vừa stop -> Phải ném ConnectException
        boolean connectionRefused = false;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/health"))
                    .timeout(Duration.ofMillis(500))
                    .GET()
                    .build();
            client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            connectionRefused = true;
        } catch (Exception e) {
            if (e.getCause() instanceof ConnectException || e.getMessage().contains("refused") || e.getMessage().contains("timed out")) {
                connectionRefused = true;
            }
        }
        assertCondition(connectionRefused, "Port " + TEST_PORT + " đã được giải phóng hoàn toàn sau khi stop()");
    }
}

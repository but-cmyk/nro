package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import data.DataGame;
import database.AlyraManager;
import network.session.SessionManager;
import utils.JsonUtil;
import utils.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HTTP Server nội bộ nhẹ nhàng phục vụ Health Check & Metrics (Nhóm F).
 * Chỉ bind loopback interface 127.0.0.1:8081, không phụ thuộc framework bên ngoài.
 * Tương thích tiêu chuẩn Docker HEALTHCHECK, Kubernetes probe và Prometheus/UptimeRobot.
 */
public final class MetricsServer {

    public static final int DEFAULT_PORT = 8081;
    private static final long START_TIME = System.currentTimeMillis();
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static HttpServer httpServer;

    private MetricsServer() {
    }

    public static synchronized void start(int port) {
        if (isRunning.get()) {
            return;
        }

        try {
            // Ràng buộc nghiêm ngặt loopback 127.0.0.1 để tránh phơi nhiễm thông tin ra ngoài Internet
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", port);
            httpServer = HttpServer.create(address, 0);

            httpServer.createContext("/health", new HealthHandler());
            httpServer.createContext("/metrics", new MetricsHandler());
            httpServer.createContext("/assets/manifest", new services.AssetHttpService.ManifestHandler());
            httpServer.createContext("/assets/download", new services.AssetHttpService.AssetDownloadHandler());
            httpServer.createContext("/", new RootHandler());

            // Tận dụng Java 21 LTS Virtual Threads cho zero-cost HTTP request dispatching
            httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            httpServer.start();
            isRunning.set(true);

            Logger.success(String.format(">> [MetricsServer] Đang lắng nghe Health, Metrics & Assets tại http://127.0.0.1:%d/ (GET /health, GET /metrics, GET /assets/manifest)", port));
        } catch (Throwable t) {
            Logger.warning(">> [MetricsServer] Không thể khởi động trên 127.0.0.1:" + port + " (" + t.getMessage() + "). Server game vẫn hoạt động bình thường.");
        }
    }

    public static synchronized void stop() {
        if (httpServer != null) {
            try {
                httpServer.stop(1); // chờ tối đa 1s cho các request dở dang hoàn tất
                Logger.log(">> [MetricsServer] Đã đóng HTTP metrics server an toàn.");
            } catch (Throwable t) {
                Logger.error(">> [MetricsServer] Lỗi khi dừng metrics server: " + t.getMessage());
            } finally {
                httpServer = null;
                isRunning.set(false);
            }
        }
    }

    public static boolean isRunning() {
        return isRunning.get();
    }

    public static long getUptimeSeconds() {
        return (System.currentTimeMillis() - START_TIME) / 1000L;
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonUtil.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Endpoint GET /health: Trả về trạng thái hoạt động của máy chủ (UP / DOWN / DRAINING).
     */
    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            boolean serverHealthy = ServerManager.isRunning;
            String status = serverHealthy ? "UP" : "DOWN";
            int statusCode = serverHealthy ? 200 : 503;

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("status", status);
            resp.put("timestamp", System.currentTimeMillis());
            resp.put("uptimeSeconds", getUptimeSeconds());
            resp.put("gamePort", ServerManager.PORT);

            sendJsonResponse(exchange, statusCode, resp);
        }
    }

    /**
     * Endpoint GET /metrics: Báo cáo chỉ số RAM Heap, số lượng Session, DB Pool, Cache stats.
     */
    private static class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                Runtime rt = Runtime.getRuntime();
                long totalMem = rt.totalMemory();
                long freeMem = rt.freeMemory();
                long usedMem = totalMem - freeMem;
                long maxMem = rt.maxMemory();

                Map<String, Object> metrics = new LinkedHashMap<>();
                metrics.put("status", ServerManager.isRunning ? "UP" : "DOWN");
                metrics.put("timestamp", System.currentTimeMillis());
                metrics.put("uptimeSeconds", getUptimeSeconds());

                // 1. Chỉ số bộ nhớ JVM (Heap)
                Map<String, Object> memStats = new LinkedHashMap<>();
                memStats.put("heapUsedMB", usedMem / 1048576L);
                memStats.put("heapFreeMB", freeMem / 1048576L);
                memStats.put("heapTotalMB", totalMem / 1048576L);
                memStats.put("heapMaxMB", maxMem / 1048576L);
                metrics.put("memory", memStats);

                // 2. Chỉ số người chơi & phiên kết nối
                Map<String, Object> sessionStats = new LinkedHashMap<>();
                int onlinePlayers = 0;
                int totalSessions = 0;
                try {
                    if (Client.gI() != null && Client.gI().getPlayers() != null) {
                        onlinePlayers = Client.gI().getPlayers().size();
                    }
                    if (SessionManager.gI() != null && SessionManager.gI().getSessions() != null) {
                        totalSessions = SessionManager.gI().getSessions().size();
                    }
                } catch (Throwable ignored) {
                }
                sessionStats.put("onlinePlayers", onlinePlayers);
                sessionStats.put("totalSessions", totalSessions);
                sessionStats.put("uniqueIps", ServerManager.CLIENTS.size());
                sessionStats.put("maxPlayers", Manager.MAX_PLAYER);
                metrics.put("sessions", sessionStats);

                // 3. Chỉ số luồng (Threads)
                Map<String, Object> threadStats = new LinkedHashMap<>();
                threadStats.put("activeCount", Thread.activeCount());
                metrics.put("threads", threadStats);

                // 4. Trạng thái kết nối Database HikariCP
                Map<String, Object> dbStats = new LinkedHashMap<>();
                try {
                    dbStats = AlyraManager.getPoolStats();
                } catch (Throwable t) {
                    dbStats.put("status", "OFFLINE");
                    dbStats.put("error", t.getMessage());
                }
                metrics.put("database", dbStats);

                // 5. Thống kê Bounded Memory Cache (Hit Rate, Evictions)
                String cacheReport = "N/A";
                try {
                    cacheReport = DataGame.getCacheReport();
                } catch (Throwable ignored) {
                }
                metrics.put("cacheReport", cacheReport);

                // 6. Thống kê BCrypt Auth CPU Throttling
                Map<String, Object> authStats = new LinkedHashMap<>();
                authStats.put("availablePermits", utils.PasswordUtils.getAvailablePermits());
                authStats.put("maxPermits", utils.PasswordUtils.getMaxConcurrentBcrypt());
                metrics.put("authThrottling", authStats);

                // 7. Thống kê Zone SerialExecutor (C2)
                Map<String, Object> zoneStats = new LinkedHashMap<>();
                int activeZoneCount = 0;
                int totalPendingTasks = 0;
                try {
                    if (Manager.MAPS != null) {
                        for (int m = 0; m < Manager.MAPS.size(); m++) {
                            var map = Manager.MAPS.get(m);
                            if (map != null && map.zones != null) {
                                for (int z = 0; z < map.zones.size(); z++) {
                                    var zone = map.zones.get(z);
                                    if (zone != null && zone.getExecutor() != null) {
                                        activeZoneCount++;
                                        totalPendingTasks += zone.getExecutor().getPendingTasks();
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {
                }
                zoneStats.put("activeZones", activeZoneCount);
                zoneStats.put("totalPendingTasks", totalPendingTasks);
                metrics.put("zoneExecutors", zoneStats);

                sendJsonResponse(exchange, 200, metrics);
            } catch (Throwable t) {
                Logger.error("Lỗi xử lý GET /metrics: " + t.getMessage());
                sendJsonResponse(exchange, 500, Map.of("error", "Internal Server Error", "message", String.valueOf(t.getMessage())));
            }
        }
    }

    /**
     * Endpoint GET /: Chỉ mục thông tin hướng dẫn
     */
    private static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("service", "NRO Game Server - Health & Metrics API");
            info.put("endpoints", Map.of(
                    "/health", "Trạng thái máy chủ (Liveness / Readiness)",
                    "/metrics", "Chỉ số RAM, Session, DB Pool, Cache Hit Rate"
            ));
            info.put("version", "Java 21 LTS Netty 4.x");
            sendJsonResponse(exchange, 200, info);
        }
    }
}

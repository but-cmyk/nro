package services.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.AlyraManager;
import database.daos.HistoryTransactionDAO;
import event.EventManager;
import managers.ConsignShopManager;
import managers.GiftCodeManager;
import models.ConsignItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import database.daos.NDVSqlFetcher;
import managers.boss.BossManager;
import models.boss.Boss;
import models.item.Item;
import models.item.Item.ItemOption;
import models.Template.ItemTemplate;
import models.player.Player;
import models.mob.Mob;
import models.map.ItemMap;
import services.Service;
import services.player.PlayerService;

import server.Client;
import server.Manager;
import server.ServerManager;
import utils.Logger;
import utils.SystemMetrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class AdminApiService {

    private static AdminApiService instance;
    private HttpServer httpServer;
    private static final int DEFAULT_PORT = 14446;
    private static final String ADMIN_TOKEN = "nro_admin_secret_2026";
    private final Gson gson = new Gson();

    public static AdminApiService gI() {
        if (instance == null) {
            synchronized (AdminApiService.class) {
                if (instance == null) {
                    instance = new AdminApiService();
                }
            }
        }
        return instance;
    }

    private AdminApiService() {}

    public synchronized void start() {
        if (httpServer != null) {
            return;
        }
        try {
            httpServer = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
            httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // Static Web Dashboard
            httpServer.createContext("/", new DashboardHandler());
            httpServer.createContext("/dashboard", new DashboardHandler());

            // API Endpoints
            httpServer.createContext("/api/status", new StatusHandler());
            httpServer.createContext("/api/players", new PlayersHandler());
            httpServer.createContext("/api/player/detail", new PlayerDetailHandler());
            httpServer.createContext("/api/player/action", new PlayerActionHandler());
            httpServer.createContext("/api/player/live", new PlayerLiveStreamHandler());
            httpServer.createContext("/api/items/search", new ItemSearchHandler());
            httpServer.createContext("/api/bosses", new BossesHandler());
            httpServer.createContext("/api/boss/action", new BossActionHandler());
            httpServer.createContext("/api/logs", new AuditLogsHandler());
            httpServer.createContext("/api/server/action", new ServerActionHandler());

            // Phase 5 Live Ops: Transactions, Consignment, GiftCodes, Events
            httpServer.createContext("/api/transactions", new TransactionsHandler());
            httpServer.createContext("/api/consign", new ConsignHandler());
            httpServer.createContext("/api/giftcodes", new GiftCodesHandler());
            httpServer.createContext("/api/events", new EventsHandler());
            httpServer.createContext("/api/telemetry", new TelemetryHandler());

            httpServer.start();
            Logger.log(Logger.GREEN, "=======================================================");
            Logger.log(Logger.GREEN, " [ADMIN LIVE OPS] Web Dashboard & API active on port " + DEFAULT_PORT);
            Logger.log(Logger.GREEN, " URL: http://localhost:" + DEFAULT_PORT + "/dashboard");
            Logger.log(Logger.GREEN, " Token: " + ADMIN_TOKEN);
            Logger.log(Logger.GREEN, "=======================================================");
        } catch (Exception e) {
            Logger.error("[AdminApiService] Failed to start HTTP server on port " + DEFAULT_PORT + ": " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (httpServer != null) {
            httpServer.stop(1);
            httpServer = null;
            Logger.log("[AdminApiService] Server stopped.");
        }
    }

    private boolean checkAuth(HttpExchange exchange) {
        // Allow OPTIONS for CORS pre-flight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            return true;
        }

        // 1. Check Header Authorization: Bearer <token>
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (ADMIN_TOKEN.equals(token)) {
                return true;
            }
        }

        // 2. Check Query parameter ?token=<token>
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            Map<String, String> params = parseQueryParams(query);
            if (ADMIN_TOKEN.equals(params.get("token"))) {
                return true;
            }
        }

        // 3. Fallback: localhost / 127.0.0.1 without token can access dashboard page
        String remoteHost = exchange.getRemoteAddress().getAddress().getHostAddress();
        if ("127.0.0.1".equals(remoteHost) || "0:0:0:0:0:0:0:1".equals(remoteHost)) {
            // Read-only or local dev access
            return true;
        }

        return false;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] bytes = gson.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            } else if (kv.length == 1) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private JsonObject parseJsonBody(HttpExchange exchange) {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            String body = baos.toString(StandardCharsets.UTF_8);
            if (body.trim().isEmpty()) return new JsonObject();
            return gson.fromJson(body, JsonObject.class);
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    // =========================================================================
    // HANDLERS
    // =========================================================================

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            Runtime rt = Runtime.getRuntime();
            long totalMem = rt.totalMemory() / (1024 * 1024);
            long freeMem = rt.freeMemory() / (1024 * 1024);
            long usedMem = totalMem - freeMem;
            long maxMem = rt.maxMemory() / (1024 * 1024);

            int onlinePlayers = Client.gI().getPlayers().size();
            int totalThreads = Thread.activeCount();

            // HikariCP pool stats
            int activeConns = 0;
            int idleConns = 0;
            int totalConns = 0;
            int waitingThreads = 0;
            try {
                var ds = AlyraManager.getDataSource();
                if (ds != null && ds.getHikariPoolMXBean() != null) {
                    var pool = ds.getHikariPoolMXBean();
                    activeConns = pool.getActiveConnections();
                    idleConns = pool.getIdleConnections();
                    totalConns = pool.getTotalConnections();
                    waitingThreads = pool.getThreadsAwaitingConnection();
                }
            } catch (Exception ignored) {}

            String topLagReport = "";
            try {
                topLagReport = server.network.PacketProfiler.gI().getTopLagReport();
            } catch (Exception ignored) {}

            JsonObject res = new JsonObject();
            res.addProperty("status", "RUNNING");
            res.addProperty("timeStart", ServerManager.timeStart);
            res.addProperty("onlinePlayers", onlinePlayers);
            res.addProperty("totalThreads", totalThreads);

            JsonObject ram = new JsonObject();
            ram.addProperty("usedMB", usedMem);
            ram.addProperty("totalMB", totalMem);
            ram.addProperty("freeMB", freeMem);
            ram.addProperty("maxMB", maxMem);
            ram.addProperty("usagePercent", (int) ((double) usedMem / maxMem * 100));
            res.add("memory", ram);

            JsonObject pool = new JsonObject();
            pool.addProperty("active", activeConns);
            pool.addProperty("idle", idleConns);
            pool.addProperty("total", totalConns);
            pool.addProperty("waiting", waitingThreads);
            res.add("databasePool", pool);

            res.addProperty("topLag", topLagReport);

            sendJsonResponse(exchange, 200, res);
        }
    }

    private class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String search = query.getOrDefault("search", "").toLowerCase().trim();

            List<Player> allPlayers = new ArrayList<>(Client.gI().getPlayers());
            JsonArray arr = new JsonArray();

            for (Player pl : allPlayers) {
                if (pl == null || !pl.isPl()) continue;
                if (!search.isEmpty() && !pl.name.toLowerCase().contains(search)) continue;

                JsonObject obj = new JsonObject();
                obj.addProperty("id", pl.id);
                obj.addProperty("name", pl.name);
                obj.addProperty("gender", (int) pl.gender);
                obj.addProperty("power", pl.nPoint != null ? pl.nPoint.power : 0);
                obj.addProperty("hp", pl.nPoint != null ? pl.nPoint.hp : 0);
                obj.addProperty("maxHp", pl.nPoint != null ? pl.nPoint.hpMax : 0);
                obj.addProperty("mapId", pl.zone != null ? pl.zone.map.mapId : -1);
                obj.addProperty("mapName", pl.zone != null ? pl.zone.map.mapName : "N/A");
                obj.addProperty("zoneId", pl.zone != null ? pl.zone.zoneId : -1);
                obj.addProperty("gold", pl.inventory != null ? pl.inventory.gold : 0);
                obj.addProperty("gem", pl.inventory != null ? pl.inventory.gem : 0);
                obj.addProperty("ruby", pl.inventory != null ? pl.inventory.ruby : 0);
                obj.addProperty("isMuted", pl.isMuted());
                obj.addProperty("isFrozen", pl.isFrozen());
                arr.add(obj);
            }

            sendJsonResponse(exchange, 200, arr);
        }
    }

    private class PlayerDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String name = query.get("name");
            if (name == null || name.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, Map.of("error", "Missing 'name' query parameter"));
                return;
            }

            Player pl = Client.gI().getPlayer(name.trim());
            boolean isOnline = (pl != null);
            if (pl == null) {
                pl = NDVSqlFetcher.loadPlayerByName(name.trim());
            }

            if (pl == null) {
                sendJsonResponse(exchange, 404, Map.of("error", "Player not found"));
                return;
            }

            JsonObject res = new JsonObject();
            res.addProperty("id", pl.id);
            res.addProperty("name", pl.name);
            res.addProperty("gender", (int) pl.gender);
            res.addProperty("isOnline", isOnline);
            res.addProperty("power", pl.nPoint != null ? pl.nPoint.power : 0);
            res.addProperty("tiemNang", pl.nPoint != null ? pl.nPoint.tiemNang : 0);
            res.addProperty("hp", pl.nPoint != null ? pl.nPoint.hp : 0);
            res.addProperty("maxHp", pl.nPoint != null ? pl.nPoint.hpMax : 0);
            res.addProperty("ki", pl.nPoint != null ? pl.nPoint.mp : 0);
            res.addProperty("maxKi", pl.nPoint != null ? pl.nPoint.mpMax : 0);
            res.addProperty("dame", pl.nPoint != null ? pl.nPoint.dame : 0);
            res.addProperty("def", pl.nPoint != null ? pl.nPoint.def : 0);
            res.addProperty("crit", pl.nPoint != null ? pl.nPoint.crit : 0);
            res.addProperty("gold", pl.inventory != null ? pl.inventory.gold : 0);
            res.addProperty("gem", pl.inventory != null ? pl.inventory.gem : 0);
            res.addProperty("ruby", pl.inventory != null ? pl.inventory.ruby : 0);
            res.addProperty("mapId", pl.zone != null ? pl.zone.map.mapId : -1);
            res.addProperty("mapName", pl.zone != null ? pl.zone.map.mapName : "N/A");
            res.addProperty("zoneId", pl.zone != null ? pl.zone.zoneId : -1);
            res.addProperty("isMuted", pl.isMuted());
            res.addProperty("isFrozen", pl.isFrozen());

            // Items Body
            JsonArray bodyArr = new JsonArray();
            if (pl.inventory != null && pl.inventory.itemsBody != null) {
                for (int i = 0; i < pl.inventory.itemsBody.size(); i++) {
                    Item it = pl.inventory.itemsBody.get(i);
                    bodyArr.add(serializeItem(it, i));
                }
            }
            res.add("itemsBody", bodyArr);

            // Items Bag
            JsonArray bagArr = new JsonArray();
            if (pl.inventory != null && pl.inventory.itemsBag != null) {
                for (int i = 0; i < pl.inventory.itemsBag.size(); i++) {
                    Item it = pl.inventory.itemsBag.get(i);
                    bagArr.add(serializeItem(it, i));
                }
            }
            res.add("itemsBag", bagArr);

            // Items Box
            JsonArray boxArr = new JsonArray();
            if (pl.inventory != null && pl.inventory.itemsBox != null) {
                for (int i = 0; i < pl.inventory.itemsBox.size(); i++) {
                    Item it = pl.inventory.itemsBox.get(i);
                    boxArr.add(serializeItem(it, i));
                }
            }
            res.add("itemsBox", boxArr);

            // Pet Info
            if (pl.pet != null) {
                JsonObject petObj = new JsonObject();
                petObj.addProperty("name", pl.pet.name != null ? pl.pet.name : "Đệ tử");
                petObj.addProperty("gender", (int) pl.pet.gender);
                petObj.addProperty("hp", pl.pet.nPoint != null ? pl.pet.nPoint.hp : 0);
                petObj.addProperty("maxHp", pl.pet.nPoint != null ? pl.pet.nPoint.hpMax : 0);
                petObj.addProperty("ki", pl.pet.nPoint != null ? pl.pet.nPoint.mp : 0);
                petObj.addProperty("maxKi", pl.pet.nPoint != null ? pl.pet.nPoint.mpMax : 0);
                petObj.addProperty("dame", pl.pet.nPoint != null ? pl.pet.nPoint.dame : 0);
                petObj.addProperty("power", pl.pet.nPoint != null ? pl.pet.nPoint.power : 0);
                petObj.addProperty("tiemNang", pl.pet.nPoint != null ? pl.pet.nPoint.tiemNang : 0);
                JsonArray petBody = new JsonArray();
                if (pl.pet.inventory != null && pl.pet.inventory.itemsBody != null) {
                    for (int i = 0; i < pl.pet.inventory.itemsBody.size(); i++) {
                        petBody.add(serializeItem(pl.pet.inventory.itemsBody.get(i), i));
                    }
                }
                petObj.add("itemsBody", petBody);
                res.add("pet", petObj);
            }

            sendJsonResponse(exchange, 200, res);
        }
    }

    private JsonObject serializeItem(Item it, int slotIndex) {
        JsonObject obj = new JsonObject();
        obj.addProperty("slot", slotIndex);
        if (it != null && it.isNotNullItem() && it.template != null) {
            obj.addProperty("id", (int) it.template.id);
            obj.addProperty("name", it.template.name);
            obj.addProperty("quantity", it.quantity);
            obj.addProperty("type", (int) it.template.type);
            JsonArray ops = new JsonArray();
            if (it.itemOptions != null) {
                for (ItemOption opt : it.itemOptions) {
                    if (opt != null && opt.optionTemplate != null) {
                        JsonObject opObj = new JsonObject();
                        opObj.addProperty("id", opt.optionTemplate.id);
                        opObj.addProperty("param", opt.param);
                        opObj.addProperty("desc", opt.getOptionString());
                        ops.add(opObj);
                    }
                }
            }
            obj.add("options", ops);
        } else {
            obj.addProperty("id", -1);
            obj.addProperty("name", "Trống");
            obj.addProperty("quantity", 0);
        }
        return obj;
    }

    private class PlayerActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
                return;
            }

            JsonObject req = parseJsonBody(exchange);
            String action = req.has("action") ? req.get("action").getAsString() : "";
            String playerName = req.has("playerName") ? req.get("playerName").getAsString() : "";
            String adminName = req.has("adminName") ? req.get("adminName").getAsString() : "WebAdmin";
            String reason = req.has("reason") ? req.get("reason").getAsString() : "";

            if (playerName.isEmpty() && !"unban".equals(action)) {
                sendJsonResponse(exchange, 400, Map.of("error", "playerName is required"));
                return;
            }

            AdminService admin = AdminService.gI();
            boolean success = false;
            String msg = "";

            switch (action.toLowerCase()) {
                case "kick" -> {
                    success = admin.kickPlayer(adminName, playerName, reason);
                    msg = success ? "Da kick nguoi choi " + playerName : "Khong tim thay player online";
                }
                case "ban" -> {
                    success = admin.banPlayer(adminName, playerName, reason);
                    msg = success ? "Da ban thanh cong tai khoan " + playerName : "Loi khi ban";
                }
                case "unban" -> {
                    String target = req.has("target") ? req.get("target").getAsString() : playerName;
                    success = admin.unbanAccount(adminName, target);
                    msg = success ? "Da unban thanh cong " + target : "Khong tim thay account can unban";
                }
                case "mute" -> {
                    int minutes = req.has("minutes") ? req.get("minutes").getAsInt() : 15;
                    success = admin.mutePlayer(adminName, playerName, minutes, reason);
                    msg = success ? "Da mute " + playerName + " trong " + minutes + " phut" : "Player khong online";
                }
                case "unmute" -> {
                    success = admin.unmutePlayer(adminName, playerName);
                    msg = success ? "Da unmute " + playerName : "Player khong online";
                }
                case "freeze" -> {
                    success = admin.freezePlayer(adminName, playerName);
                    msg = success ? "Da freeze " + playerName : "Player khong online";
                }
                case "unfreeze" -> {
                    success = admin.unfreezePlayer(adminName, playerName);
                    msg = success ? "Da unfreeze " + playerName : "Player khong online";
                }
                case "teleport_village" -> {
                    success = admin.teleportToVillage(adminName, playerName);
                    msg = success ? "Da dua " + playerName + " ve lang" : "Player khong online";
                }
                case "teleport" -> {
                    int mapId = req.has("mapId") ? req.get("mapId").getAsInt() : 21;
                    int zoneId = req.has("zoneId") ? req.get("zoneId").getAsInt() : 0;
                    int x = req.has("x") ? req.get("x").getAsInt() : 200;
                    int y = req.has("y") ? req.get("y").getAsInt() : 300;
                    success = admin.teleportPlayer(adminName, playerName, mapId, zoneId, x, y);
                    msg = success ? "Da dich chuyen " + playerName + " toi Map " + mapId : "Player khong online";
                }
                case "buff_money" -> {
                    long gold = req.has("gold") ? req.get("gold").getAsLong() : 0;
                    int gem = req.has("gem") ? req.get("gem").getAsInt() : 0;
                    int ruby = req.has("ruby") ? req.get("ruby").getAsInt() : 0;
                    success = admin.buffMoney(adminName, playerName, gold, gem, ruby);
                    msg = success ? "Da buff tien thanh cong cho " + playerName : "Loi khi buff tien";
                }
                case "heal" -> {
                    Player targetPl = Client.gI().getPlayer(playerName);
                    if (targetPl != null && targetPl.nPoint != null) {
                        targetPl.nPoint.hp = targetPl.nPoint.hpMax;
                        targetPl.nPoint.mp = targetPl.nPoint.mpMax;
                        PlayerService.gI().sendInfoHpMp(targetPl);
                        PlayerService.gI().sendInfoHpMpMoney(targetPl);
                        success = true;
                        msg = "Da hoi phuc day HP & MP cho " + playerName;
                    } else {
                        msg = "Player khong online";
                    }
                }
                case "whisper" -> {
                    String text = req.has("text") ? req.get("text").getAsString() : "";
                    Player targetPl = Client.gI().getPlayer(playerName);
                    if (targetPl != null && !text.isEmpty()) {
                        Service.gI().sendThongBao(targetPl, "[Admin LiveOps]: " + text);
                        success = true;
                        msg = "Da gui thong bao toi " + playerName;
                    } else {
                        msg = "Player khong online hoac noi dung thong bao trong";
                    }
                }
                case "buff_item" -> {
                    short itemId = req.has("itemId") ? req.get("itemId").getAsShort() : -1;
                    int quantity = req.has("quantity") ? req.get("quantity").getAsInt() : 1;
                    String optionsStr = req.has("options") ? req.get("options").getAsString() : "";
                    if (itemId >= 0) {
                        success = admin.buffItem(adminName, playerName, itemId, quantity, optionsStr);
                        msg = success ? "Da buff item [" + itemId + " x" + quantity + "] cho " + playerName : "Loi buff item";
                    } else {
                        msg = "itemId khong hop le";
                    }
                }
                default -> {
                    msg = "Hanh dong khong hop le: " + action;
                }
            }

            sendJsonResponse(exchange, 200, Map.of("success", success, "message", msg));
        }
    }

    private class PlayerLiveStreamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String name = query.get("name");
            if (name == null || name.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, Map.of("error", "Missing 'name' query parameter"));
                return;
            }

            Player pl = Client.gI().getPlayer(name.trim());
            if (pl == null || !pl.isPl()) {
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("isOnline", false);
                err.addProperty("message", "Người chơi hiện không online hoặc không tồn tại!");
                sendJsonResponse(exchange, 200, err);
                return;
            }

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("isOnline", true);

            // Player data
            JsonObject pData = new JsonObject();
            pData.addProperty("id", pl.id);
            pData.addProperty("name", pl.name);
            pData.addProperty("gender", (int) pl.gender);
            pData.addProperty("genderName", pl.gender == 0 ? "Trái Đất" : (pl.gender == 1 ? "Namếc" : "Xayda"));
            pData.addProperty("isDie", pl.isDie());
            pData.addProperty("x", pl.location != null ? pl.location.x : 0);
            pData.addProperty("y", pl.location != null ? pl.location.y : 0);
            pData.addProperty("hp", pl.nPoint != null ? pl.nPoint.hp : 0);
            pData.addProperty("maxHp", pl.nPoint != null ? pl.nPoint.hpMax : 0);
            pData.addProperty("ki", pl.nPoint != null ? pl.nPoint.mp : 0);
            pData.addProperty("maxKi", pl.nPoint != null ? pl.nPoint.mpMax : 0);
            pData.addProperty("power", pl.nPoint != null ? pl.nPoint.power : 0);
            pData.addProperty("tiemNang", pl.nPoint != null ? pl.nPoint.tiemNang : 0);
            pData.addProperty("dame", pl.nPoint != null ? pl.nPoint.dame : 0);
            pData.addProperty("def", pl.nPoint != null ? pl.nPoint.def : 0);
            pData.addProperty("crit", pl.nPoint != null ? pl.nPoint.crit : 0);
            pData.addProperty("gold", pl.inventory != null ? pl.inventory.gold : 0);
            pData.addProperty("gem", pl.inventory != null ? pl.inventory.gem : 0);
            pData.addProperty("ruby", pl.inventory != null ? pl.inventory.ruby : 0);
            pData.addProperty("head", (int) pl.getHead());
            pData.addProperty("body", (int) pl.getBody());
            pData.addProperty("leg", (int) pl.getLeg());
            pData.addProperty("clan", pl.clan != null ? pl.clan.name : "");
            pData.addProperty("isFrozen", pl.isFrozen());
            pData.addProperty("isMuted", pl.isMuted());

            if (pl.pet != null) {
                pData.addProperty("hasPet", true);
                JsonObject petObj = new JsonObject();
                petObj.addProperty("name", pl.pet.name != null ? pl.pet.name : "Đệ Tử");
                petObj.addProperty("x", pl.pet.location != null ? pl.pet.location.x : 0);
                petObj.addProperty("y", pl.pet.location != null ? pl.pet.location.y : 0);
                petObj.addProperty("hp", pl.pet.nPoint != null ? pl.pet.nPoint.hp : 0);
                petObj.addProperty("maxHp", pl.pet.nPoint != null ? pl.pet.nPoint.hpMax : 0);
                petObj.addProperty("power", pl.pet.nPoint != null ? pl.pet.nPoint.power : 0);
                pData.add("pet", petObj);
            } else {
                pData.addProperty("hasPet", false);
            }
            res.add("player", pData);

            // Zone & Map data
            JsonObject zData = new JsonObject();
            if (pl.zone != null) {
                zData.addProperty("mapId", pl.zone.map != null ? pl.zone.map.mapId : 0);
                zData.addProperty("mapName", pl.zone.map != null ? pl.zone.map.mapName : "N/A");
                zData.addProperty("zoneId", pl.zone.zoneId);
                zData.addProperty("planetId", pl.zone.map != null ? (int) pl.zone.map.planetId : 0);
                zData.addProperty("mapWidth", (pl.zone.map != null && pl.zone.map.pxw > 0) ? pl.zone.map.pxw : 1600);
                zData.addProperty("mapHeight", (pl.zone.map != null && pl.zone.map.pxh > 0) ? pl.zone.map.pxh : 600);
                zData.addProperty("playerCount", pl.zone.getNumOfPlayers());
                zData.addProperty("maxPlayer", pl.zone.maxPlayer);

                // Mobs in zone
                JsonArray mobsArr = new JsonArray();
                if (pl.zone.mobs != null) {
                    synchronized (pl.zone.mobs) {
                        for (Mob m : pl.zone.mobs) {
                            if (m == null) continue;
                            JsonObject mObj = new JsonObject();
                            mObj.addProperty("id", m.id);
                            mObj.addProperty("name", m.name != null ? m.name : "Quái vật");
                            mObj.addProperty("tempId", m.tempId);
                            mObj.addProperty("x", m.location != null ? m.location.x : 0);
                            mObj.addProperty("y", m.location != null ? m.location.y : 0);
                            mObj.addProperty("hp", m.point != null ? m.point.gethp() : 0);
                            mObj.addProperty("maxHp", m.point != null ? m.point.getHpFull() : 0);
                            mObj.addProperty("isDie", m.isDie());
                            mobsArr.add(mObj);
                        }
                    }
                }
                res.add("mobs", mobsArr);

                // Other Players in zone
                JsonArray otherPlayers = new JsonArray();
                for (Player op : pl.zone.getPlayers()) {
                    if (op == null || op.id == pl.id) continue;
                    JsonObject opObj = new JsonObject();
                    opObj.addProperty("id", op.id);
                    opObj.addProperty("name", op.name);
                    opObj.addProperty("gender", (int) op.gender);
                    opObj.addProperty("x", op.location != null ? op.location.x : 0);
                    opObj.addProperty("y", op.location != null ? op.location.y : 0);
                    opObj.addProperty("hp", op.nPoint != null ? op.nPoint.hp : 0);
                    opObj.addProperty("maxHp", op.nPoint != null ? op.nPoint.hpMax : 0);
                    opObj.addProperty("isDie", op.isDie());
                    otherPlayers.add(opObj);
                }
                res.add("otherPlayers", otherPlayers);

                // Bosses in zone
                JsonArray bossesArr = new JsonArray();
                for (Player b : pl.zone.getBosses()) {
                    if (b == null) continue;
                    JsonObject bObj = new JsonObject();
                    bObj.addProperty("id", b.id);
                    bObj.addProperty("name", b.name);
                    bObj.addProperty("x", b.location != null ? b.location.x : 0);
                    bObj.addProperty("y", b.location != null ? b.location.y : 0);
                    bObj.addProperty("hp", b.nPoint != null ? b.nPoint.hp : 0);
                    bObj.addProperty("maxHp", b.nPoint != null ? b.nPoint.hpMax : 0);
                    bossesArr.add(bObj);
                }
                res.add("bosses", bossesArr);

                // Items on ground
                JsonArray itemsArr = new JsonArray();
                if (pl.zone.items != null) {
                    synchronized (pl.zone.items) {
                        for (ItemMap itm : pl.zone.items) {
                            if (itm == null) continue;
                            JsonObject itmObj = new JsonObject();
                            itmObj.addProperty("id", itm.itemMapId);
                            itmObj.addProperty("name", itm.itemTemplate != null ? itm.itemTemplate.name : "Vật phẩm");
                            itmObj.addProperty("x", itm.x);
                            itmObj.addProperty("y", itm.y);
                            itmObj.addProperty("quantity", itm.quantity);
                            itemsArr.add(itmObj);
                        }
                    }
                }
                res.add("items", itemsArr);
            }
            res.add("zone", zData);

            sendJsonResponse(exchange, 200, res);
        }
    }

    private class ItemSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String keyword = query.getOrDefault("q", "").toLowerCase().trim();

            JsonArray arr = new JsonArray();
            int limit = 50;
            int count = 0;

            for (ItemTemplate temp : Manager.ITEM_TEMPLATES) {
                if (temp == null) continue;
                boolean match = keyword.isEmpty()
                        || String.valueOf(temp.id).contains(keyword)
                        || (temp.name != null && temp.name.toLowerCase().contains(keyword));
                if (match) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", (int) temp.id);
                    obj.addProperty("name", temp.name);
                    obj.addProperty("type", (int) temp.type);
                    obj.addProperty("description", temp.description != null ? temp.description : "");
                    arr.add(obj);
                    count++;
                    if (count >= limit) break;
                }
            }

            sendJsonResponse(exchange, 200, arr);
        }
    }

    private class BossesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            JsonArray arr = new JsonArray();
            List<Boss> bosses = BossManager.gI().getBosses();

            if (bosses != null) {
                for (Boss b : bosses) {
                    if (b == null) continue;
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", b.id);
                    obj.addProperty("name", b.name);
                    obj.addProperty("hp", b.nPoint != null ? b.nPoint.hp : 0);
                    obj.addProperty("maxHp", b.nPoint != null ? b.nPoint.hpMax : 0);
                    obj.addProperty("mapId", b.zone != null ? b.zone.map.mapId : -1);
                    obj.addProperty("mapName", b.zone != null ? b.zone.map.mapName : "N/A");
                    obj.addProperty("zoneId", b.zone != null ? b.zone.zoneId : -1);
                    obj.addProperty("x", b.location != null ? b.location.x : 0);
                    obj.addProperty("y", b.location != null ? b.location.y : 0);
                    arr.add(obj);
                }
            }

            sendJsonResponse(exchange, 200, arr);
        }
    }

    private class BossActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            JsonObject req = parseJsonBody(exchange);
            String action = req.has("action") ? req.get("action").getAsString() : "";
            int bossId = req.has("bossId") ? req.get("bossId").getAsInt() : -999999;
            String adminName = req.has("adminName") ? req.get("adminName").getAsString() : "WebAdmin";

            boolean success = false;
            String msg = "";

            if ("kill".equalsIgnoreCase(action)) {
                success = AdminService.gI().killBoss(adminName, bossId);
                msg = success ? "Da tieu diet Boss ID " + bossId : "Khong tim thay boss";
            } else {
                msg = "Hanh dong khong ho tro";
            }

            sendJsonResponse(exchange, 200, Map.of("success", success, "message", msg));
        }
    }

    private class AuditLogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            int limit = 50;
            try {
                if (query.containsKey("limit")) limit = Integer.parseInt(query.get("limit"));
            } catch (NumberFormatException ignored) {}

            var logs = AuditLogService.gI().getRecentLogs(limit);
            sendJsonResponse(exchange, 200, logs);
        }
    }

    private class ServerActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            JsonObject req = parseJsonBody(exchange);
            String action = req.has("action") ? req.get("action").getAsString() : "";
            String adminName = req.has("adminName") ? req.get("adminName").getAsString() : "WebAdmin";

            boolean success = false;
            String msg = "";

            switch (action.toLowerCase()) {
                case "broadcast" -> {
                    String message = req.has("message") ? req.get("message").getAsString() : "";
                    boolean isRed = req.has("isRed") && req.get("isRed").getAsBoolean();
                    AdminService.gI().broadcast(adminName, message, isRed);
                    success = true;
                    msg = "Da phat thong bao server";
                }
                case "maintenance" -> {
                    int minutes = req.has("minutes") ? req.get("minutes").getAsInt() : 5;
                    AdminService.gI().startMaintenance(adminName, minutes);
                    success = true;
                    msg = "Da kich hoat bao tri dem nguoc " + minutes + " phut";
                }
                case "save_all" -> {
                    int count = AdminService.gI().saveAllPlayers(adminName);
                    success = true;
                    msg = "Da luu " + count + " player vao database";
                }
                case "reload_data" -> {
                    String category = req.has("category") ? req.get("category").getAsString() : "ALL";
                    success = AdminService.gI().reloadGameData(adminName, category);
                    msg = success ? "Da reload thanh cong" : "Server dang ban hoac co loi";
                }
                case "gc" -> {
                    System.gc();
                    success = true;
                    msg = "Da goi JVM Garbage Collection";
                }
                default -> msg = "Hanh dong khong hop le";
            }

            sendJsonResponse(exchange, 200, Map.of("success", success, "message", msg));
        }
    }

    // =========================================================================
    // DASHBOARD HANDLER (Serves disk file if present, fallback to embedded HTML)
    // =========================================================================

    
    // =========================================================================
    // TRANSACTIONS HANDLER (Audit trade history & investigate abuse)
    // =========================================================================
    private class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQueryParams(query);
                String search = params.getOrDefault("search", "");
                int limit = 50;
                try {
                    if (params.containsKey("limit")) {
                        limit = Integer.parseInt(params.get("limit"));
                    }
                } catch (Exception ignored) {}

                List<Map<String, Object>> list = HistoryTransactionDAO.searchTransactions(search, limit);
                sendJsonResponse(exchange, 200, list);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonObject req = parseJsonBody(exchange);
                String action = req.has("action") ? req.get("action").getAsString() : "";
                String adminName = req.has("admin") ? req.get("admin").getAsString() : "WebAdmin";

                if ("delete".equalsIgnoreCase(action)) {
                    int id = req.has("id") ? req.get("id").getAsInt() : 0;
                    boolean ok = HistoryTransactionDAO.deleteTransaction(id);
                    if (ok) {
                        AuditLogService.gI().log(adminName, "DELETE_TRADE_LOG", "ID: " + id, "");
                    }
                    sendJsonResponse(exchange, 200, Map.of("success", ok, "message", ok ? "Đã xóa lịch sử giao dịch ID " + id : "Lỗi khi xóa"));
                    return;
                }

                sendJsonResponse(exchange, 400, Map.of("error", "Hành động không hợp lệ"));
            }
        }
    }

    // =========================================================================
    // CONSIGN SHOP HANDLER (Consign Shop Monitor & Anti-Cheat)
    // =========================================================================
    private class ConsignHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<ConsignItem> rawList;
                synchronized (ConsignShopManager.gI()) {
                    rawList = new ArrayList<>(ConsignShopManager.gI().listItem);
                }

                JsonArray arr = new JsonArray();
                long totalGold = 0;
                long totalGem = 0;
                int suspiciousCount = 0;

                for (ConsignItem item : rawList) {
                    if (item == null) continue;
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", item.id);
                    obj.addProperty("itemId", item.itemId);

                    ItemTemplate template = Manager.ITEM_TEMPLATES.get(item.itemId);
                    obj.addProperty("name", template != null ? template.name : ("Item ID " + item.itemId));
                    obj.addProperty("iconId", template != null ? template.iconID : 0);

                    obj.addProperty("playerId", item.player_sell);
                    obj.addProperty("playerName", (item.playerName != null && !item.playerName.isEmpty()) ? item.playerName : ("ID " + item.player_sell));

                    obj.addProperty("tab", item.tab);
                    String tabName = "";
                    if (item.tab >= 0 && item.tab < ConsignShopManager.gI().tabName.length) {
                        tabName = ConsignShopManager.gI().tabName[item.tab];
                    }
                    obj.addProperty("tabName", tabName);

                    obj.addProperty("goldSell", item.goldSell);
                    obj.addProperty("gemSell", item.gemSell);
                    obj.addProperty("quantity", item.quantity);
                    obj.addProperty("isBuy", item.isBuy);
                    obj.addProperty("isUpTop", item.isUpTop);

                    totalGold += item.goldSell;
                    totalGem += item.gemSell;

                    // Suspicious criteria: abnormal gold/gem or gold bar inflated price
                    boolean isSuspicious = item.goldSell >= 1_000_000_000L || item.gemSell >= 50_000 || (item.itemId == 457 && item.goldSell > 100_000_000);
                    if (isSuspicious) suspiciousCount++;
                    obj.addProperty("isSuspicious", isSuspicious);

                    JsonArray optArr = new JsonArray();
                    if (item.options != null) {
                        for (ItemOption opt : item.options) {
                            if (opt != null && opt.optionTemplate != null) {
                                JsonObject optObj = new JsonObject();
                                optObj.addProperty("id", opt.optionTemplate.id);
                                optObj.addProperty("param", opt.param);
                                optObj.addProperty("name", opt.getOptionString());
                                optArr.add(optObj);
                            }
                        }
                    }
                    obj.add("options", optArr);
                    arr.add(obj);
                }

                JsonObject res = new JsonObject();
                res.addProperty("total", rawList.size());
                res.addProperty("totalGold", totalGold);
                res.addProperty("totalGem", totalGem);
                res.addProperty("suspiciousCount", suspiciousCount);
                res.add("items", arr);
                sendJsonResponse(exchange, 200, res);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonObject req = parseJsonBody(exchange);
                String action = req.has("action") ? req.get("action").getAsString() : "";
                String adminName = req.has("admin") ? req.get("admin").getAsString() : "WebAdmin";

                if ("remove".equalsIgnoreCase(action)) {
                    int id = req.has("id") ? req.get("id").getAsInt() : 0;
                    ConsignItem item = ConsignShopManager.gI().getItemById(id);
                    if (item != null) {
                        ConsignShopManager.gI().removeItem(item);
                        try {
                            AlyraManager.executeUpdate("DELETE FROM shop_ky_gui WHERE id = ?", id);
                        } catch (Exception ignored) {}

                        AuditLogService.gI().log(adminName, "REMOVE_CONSIGN_ITEM", "Removed consign item ID " + id + ", ItemID: " + item.itemId, "Seller: " + item.player_sell);
                        sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Đã gỡ vật phẩm ID " + id + " khỏi chợ ký gửi!"));
                        return;
                    } else {
                        sendJsonResponse(exchange, 404, Map.of("success", false, "message", "Không tìm thấy món hàng ký gửi ID " + id));
                        return;
                    }
                }

                sendJsonResponse(exchange, 400, Map.of("error", "Hành động không hợp lệ"));
            }
        }
    }

    // =========================================================================
    // GIFTCODE HANDLER (List, Create, Delete Giftcodes)
    // =========================================================================
    private class GiftCodesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Map<String, Object>> list = new ArrayList<>();
                try (Connection con = AlyraManager.getConnection();
                     PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode ORDER BY id DESC");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("code", rs.getString("code"));
                        map.put("countLeft", rs.getInt("count_left"));
                        map.put("detail", rs.getString("detail"));
                        map.put("allGender", rs.getString("allGender"));
                        Timestamp created = rs.getTimestamp("datecreate");
                        Timestamp expired = rs.getTimestamp("expired");
                        map.put("dateCreate", created != null ? created.toString() : "");
                        map.put("dateExpired", expired != null ? expired.toString() : "");
                        boolean isExpired = expired != null && expired.getTime() < System.currentTimeMillis();
                        map.put("isExpired", isExpired);
                        list.add(map);
                    }
                } catch (Exception e) {
                    utils.Logger.logException(GiftCodesHandler.class, e, "Lỗi đọc danh sách giftcode");
                }
                sendJsonResponse(exchange, 200, list);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonObject req = parseJsonBody(exchange);
                String action = req.has("action") ? req.get("action").getAsString() : "";
                String adminName = req.has("admin") ? req.get("admin").getAsString() : "WebAdmin";

                if ("create".equalsIgnoreCase(action)) {
                    String code = req.has("code") ? req.get("code").getAsString().trim() : "";
                    int count = req.has("count") ? req.get("count").getAsInt() : 100;
                    int days = req.has("days") ? req.get("days").getAsInt() : 30;
                    String allGender = req.has("allGender") ? req.get("allGender").getAsString() : "all";
                    String detailJson = req.has("detail") ? req.get("detail").getAsString() : "[]";

                    if (code.isEmpty()) {
                        sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Mã GiftCode không được để trống!"));
                        return;
                    }

                    boolean ok = GiftCodeManager.gI().createGiftCode(code, count, days, detailJson, allGender);
                    if (ok) {
                        AuditLogService.gI().log(adminName, "CREATE_GIFTCODE", "Code: " + code + ", Count: " + count + ", Days: " + days, "");
                        sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Đã tạo mã GiftCode '" + code + "' thành công!"));
                    } else {
                        sendJsonResponse(exchange, 500, Map.of("success", false, "message", "Tạo GiftCode thất bại (mã đã tồn tại hoặc lỗi định dạng detail JSON)!"));
                    }
                    return;
                }

                if ("delete".equalsIgnoreCase(action)) {
                    int id = req.has("id") ? req.get("id").getAsInt() : 0;
                    boolean ok = GiftCodeManager.gI().deleteGiftCode(id);
                    if (ok) {
                        AuditLogService.gI().log(adminName, "DELETE_GIFTCODE", "ID: " + id, "");
                    }
                    sendJsonResponse(exchange, 200, Map.of("success", ok, "message", ok ? "Đã xóa GiftCode ID " + id : "Lỗi khi xóa GiftCode"));
                    return;
                }

                if ("reload".equalsIgnoreCase(action)) {
                    GiftCodeManager.gI().reload();
                    AuditLogService.gI().log(adminName, "RELOAD_GIFTCODE", "Reloaded from DB", "");
                    sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Đã reload danh sách GiftCode từ DB!"));
                    return;
                }

                sendJsonResponse(exchange, 400, Map.of("error", "Hành động không hợp lệ"));
            }
        }
    }

    // =========================================================================
    // EVENTS HANDLER (Live Event Switcher)
    // =========================================================================
    private class EventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, Boolean> states = EventManager.gI().getAllEventStates();
                sendJsonResponse(exchange, 200, states);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonObject req = parseJsonBody(exchange);
                String eventName = req.has("event") ? req.get("event").getAsString() : "";
                boolean active = req.has("active") && req.get("active").getAsBoolean();
                String adminName = req.has("admin") ? req.get("admin").getAsString() : "WebAdmin";

                boolean ok = EventManager.gI().setEventState(eventName, active);
                if (ok) {
                    AuditLogService.gI().log(adminName, "TOGGLE_EVENT", "Event " + eventName + " -> " + (active ? "ON" : "OFF"), "");
                    try {
                        services.Service.gI().sendThongBaoAllPlayer("[SỰ KIỆN] " + eventName + (active ? " đã được kích hoạt!" : " đã kết thúc!"));
                    } catch (Exception ignored) {}
                    sendJsonResponse(exchange, 200, Map.of(
                        "success", true,
                        "message", "Cập nhật sự kiện " + eventName + " thành công!",
                        "states", EventManager.gI().getAllEventStates()
                    ));
                } else {
                    sendJsonResponse(exchange, 400, Map.of("success", false, "message", "Tên sự kiện không hợp lệ: " + eventName));
                }
            }
        }
    }

    private String loadDashboardHtml() {
        java.io.File diskFile = new java.io.File("data/admin/index.html");
        if (diskFile.exists() && diskFile.isFile()) {
            try {
                return java.nio.file.Files.readString(diskFile.toPath(), StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }
        try (InputStream is = getClass().getResourceAsStream("/data/admin/index.html")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return "<!DOCTYPE html><html><body><h2>NRO Live Ops & Admin Dashboard</h2><p>Vui lòng đảm bảo tệp data/admin/index.html tồn tại.</p></body></html>";
    }

    private class TelemetryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, Map.of("status", "OK"));
                return;
            }
            if (!checkAuth(exchange)) {
                sendJsonResponse(exchange, 401, Map.of("error", "Unauthorized"));
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String json = telemetry.TrajectoryLogger.gI().getStatusJson();
                byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                JsonObject req = parseJsonBody(exchange);
                String action = req.has("action") ? req.get("action").getAsString() : "";
                switch (action.toLowerCase()) {
                    case "toggle" -> {
                        boolean enabled = req.has("enabled") ? req.get("enabled").getAsBoolean() : !telemetry.TelemetryConfig.ENABLED;
                        telemetry.TelemetryConfig.toggleEnabled(enabled);
                    }
                    case "add_map" -> {
                        if (req.has("map_id")) {
                            telemetry.TelemetryConfig.addWhitelistMap(req.get("map_id").getAsInt());
                        }
                    }
                    case "remove_map" -> {
                        if (req.has("map_id")) {
                            telemetry.TelemetryConfig.removeWhitelistMap(req.get("map_id").getAsInt());
                        }
                    }
                    case "clear_maps" -> telemetry.TelemetryConfig.clearWhitelistMaps();
                    case "reset_metrics" -> telemetry.TrajectoryLogger.gI().resetMetrics();
                }

                String json = telemetry.TrajectoryLogger.gI().getStatusJson();
                byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            sendJsonResponse(exchange, 405, Map.of("error", "Method not allowed"));
        }
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.endsWith(".gif") || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".webp") || path.endsWith(".ico")) {
                String filename = path.substring(path.lastIndexOf('/') + 1);
                java.io.File file = new java.io.File("data/admin/" + filename);
                if (file.exists() && file.isFile()) {
                    String mime = "application/octet-stream";
                    if (filename.endsWith(".gif")) mime = "image/gif";
                    else if (filename.endsWith(".png")) mime = "image/png";
                    else if (filename.endsWith(".jpg")) mime = "image/jpeg";
                    else if (filename.endsWith(".webp")) mime = "image/webp";
                    else if (filename.endsWith(".ico")) mime = "image/x-icon";
                    
                    byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                    exchange.getResponseHeaders().set("Content-Type", mime);
                    exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (java.io.OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                    return;
                }
            }
            String html = loadDashboardHtml();
            sendHtmlResponse(exchange, 200, html);
        }
    }
}

package services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import data.DataGame;
import utils.FileIO;
import utils.JsonUtil;
import utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dịch vụ cung cấp Manifest và tải tài nguyên tĩnh (Static Assets Delta Update) qua HTTP (Nhóm D1).
 * Giúp client tải tài nguyên qua HTTP/CDN nhanh gấp nhiều lần so với streaming socket tuần tự.
 */
public class AssetHttpService {

    private static final Map<Integer, String> MANIFEST_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> SHA1_CACHE = new ConcurrentHashMap<>();

    public static String computeSha1(byte[] data) {
        if (data == null || data.length == 0) {
            return "da39a3ee5e6b4b0d3255bfef95601890afd80709"; // empty sha1
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getOrGenerateManifest(int zoom) {
        if (zoom < 1 || zoom > 4) {
            zoom = 2;
        }
        final int targetZoom = zoom;
        return MANIFEST_CACHE.computeIfAbsent(targetZoom, z -> {
            try {
                File dir = new File("data/res/x" + z);
                File[] files = dir.listFiles();
                List<Map<String, Object>> fileList = new ArrayList<>();

                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            byte[] data = FileIO.readFile(f.getAbsolutePath());
                            if (data != null) {
                                String sha1 = computeSha1(data);
                                SHA1_CACHE.put("x" + z + "/" + f.getName(), sha1);

                                Map<String, Object> item = new LinkedHashMap<>();
                                item.put("name", f.getName());
                                item.put("size", data.length);
                                item.put("sha1", sha1);
                                fileList.add(item);
                            }
                        }
                    }
                }

                Map<String, Object> manifest = new LinkedHashMap<>();
                manifest.put("version", DataGame.vsRes);
                manifest.put("zoom", z);
                manifest.put("totalFiles", fileList.size());
                manifest.put("generatedAt", System.currentTimeMillis());
                manifest.put("files", fileList);

                String json = JsonUtil.toJson(manifest);

                // Lưu bản sao manifest ra đĩa để CDN/Nginx có thể host trực tiếp
                File manifestFile = new File("data/res/manifest_x" + z + ".json");
                FileIO.writeFile(manifestFile.getAbsolutePath(), json.getBytes(StandardCharsets.UTF_8));
                Logger.success(String.format(">> [AssetHttpService] Đã khởi tạo Manifest x%d: %d files (sha1 cached)", z, fileList.size()));

                return json;
            } catch (Exception e) {
                Logger.logException(AssetHttpService.class, e, "Lỗi tạo asset manifest x" + z);
                return "{\"error\":\"cannot generate manifest\"}";
            }
        });
    }

    public static void clearCache() {
        MANIFEST_CACHE.clear();
        SHA1_CACHE.clear();
    }

    public static class ManifestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            int zoom = 2;
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("zoom=")) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "zoom".equalsIgnoreCase(pair[0])) {
                        try {
                            zoom = Integer.parseInt(pair[1]);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            String manifestJson = getOrGenerateManifest(zoom);
            byte[] bytes = manifestJson.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    public static class AssetDownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            int zoom = 2;
            String name = null;
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if ("zoom".equalsIgnoreCase(pair[0])) {
                            try {
                                zoom = Integer.parseInt(pair[1]);
                            } catch (NumberFormatException ignored) {}
                        } else if ("name".equalsIgnoreCase(pair[0])) {
                            name = pair[1];
                        }
                    }
                }
            }

            if (name == null || name.trim().isEmpty() || name.contains("..") || name.contains("/") || name.contains("\\")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            File targetFile = new File("data/res/x" + zoom + "/" + name);
            if (!targetFile.exists() || !targetFile.isFile()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] data = FileIO.readFile(targetFile.getAbsolutePath());
            if (data == null) {
                exchange.sendResponseHeaders(500, -1);
                return;
            }

            String sha1 = SHA1_CACHE.computeIfAbsent("x" + zoom + "/" + name, k -> computeSha1(data));
            String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");
            if (ifNoneMatch != null && ifNoneMatch.replace("\"", "").trim().equalsIgnoreCase(sha1)) {
                exchange.sendResponseHeaders(304, -1); // Not Modified
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.getResponseHeaders().set("ETag", "\"" + sha1 + "\"");
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=31536000, immutable");
            exchange.sendResponseHeaders(200, data.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }
}

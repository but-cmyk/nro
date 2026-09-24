package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Tiện ích xử lý JSON hiệu năng cao sử dụng Jackson Databind 2.17.x.
 * Tương thích với Java 21 LTS và Thread-safe.
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

    private JsonUtil() {
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            Logger.logException(JsonUtil.class, e, "Lỗi serialize JSON object");
            return "{}";
        }
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            Logger.logException(JsonUtil.class, e, "Lỗi deserialize JSON to " + valueType.getSimpleName());
            return null;
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, valueTypeRef);
        } catch (IOException e) {
            Logger.logException(JsonUtil.class, e, "Lỗi deserialize JSON with TypeReference");
            return null;
        }
    }

    public static JsonNode parseTree(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            return null;
        }
    }

    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return MAPPER.createArrayNode();
    }

    /**
     * Đọc giá trị long an toàn từ JsonNode array theo index, tránh ngoại lệ IndexOutOfBounds
     */
    public static long getLongSafely(JsonNode node, int index, long defaultValue) {
        if (node != null && node.isArray() && node.has(index) && !node.get(index).isNull()) {
            return node.get(index).asLong(defaultValue);
        }
        return defaultValue;
    }

    /**
     * Đọc giá trị int an toàn từ JsonNode array theo index
     */
    public static int getIntSafely(JsonNode node, int index, int defaultValue) {
        if (node != null && node.isArray() && node.has(index) && !node.get(index).isNull()) {
            return node.get(index).asInt(defaultValue);
        }
        return defaultValue;
    }

    /**
     * Đọc giá trị String an toàn từ JsonNode array theo index
     */
    public static String getStringSafely(JsonNode node, int index, String defaultValue) {
        if (node != null && node.isArray() && node.has(index) && !node.get(index).isNull()) {
            return node.get(index).asText(defaultValue);
        }
        return defaultValue;
    }
}

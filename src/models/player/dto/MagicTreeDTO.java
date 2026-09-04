package models.player.dto;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.Logger;

/**
 * MagicTreeDTO: Mô hình dữ liệu cây đậu thần định kiểu Type-Safe.
 * Hỗ trợ an toàn cả Object JSON lẫn legacy Array JSON.
 */
public class MagicTreeDTO {
    public byte level = 1;
    public byte currPeas = 5;
    public boolean isUpgrade = false;
    public long lastTimeHarvest = System.currentTimeMillis();
    public long lastTimeUpgrade = System.currentTimeMillis();

    public static MagicTreeDTO fromJson(String jsonStr) {
        MagicTreeDTO dto = new MagicTreeDTO();
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return dto;
        }

        try {
            Object parsed = JSONValue.parse(jsonStr.trim());
            if (parsed instanceof JSONObject) {
                JSONObject obj = (JSONObject) parsed;
                dto.level = getByte(obj, "level", (byte) 1);
                dto.currPeas = getByte(obj, "currPeas", (byte) 5);
                dto.isUpgrade = getBoolean(obj, "isUpgrade", false);
                dto.lastTimeHarvest = getLong(obj, "lastTimeHarvest", System.currentTimeMillis());
                dto.lastTimeUpgrade = getLong(obj, "lastTimeUpgrade", System.currentTimeMillis());
            } else if (parsed instanceof JSONArray) {
                JSONArray arr = (JSONArray) parsed;
                int size = arr.size();
                if (size > 0) dto.level = parseByte(arr.get(0), (byte) 1);
                if (size > 1) dto.currPeas = parseByte(arr.get(1), (byte) 5);
                if (size > 2) dto.isUpgrade = parseByte(arr.get(2), (byte) 0) == 1;
                if (size > 3) dto.lastTimeHarvest = parseLong(arr.get(3), System.currentTimeMillis());
                if (size > 4) dto.lastTimeUpgrade = parseLong(arr.get(4), System.currentTimeMillis());
            }
        } catch (Exception e) {
            Logger.logException(MagicTreeDTO.class, e, "Lỗi đọc dữ liệu MagicTreeDTO từ JSON: " + jsonStr);
        }
        return dto;
    }

    private static byte getByte(JSONObject obj, String key, byte def) {
        Object val = obj.get(key);
        return val != null ? parseByte(val, def) : def;
    }

    private static boolean getBoolean(JSONObject obj, String key, boolean def) {
        Object val = obj.get(key);
        if (val == null) return def;
        if (val instanceof Boolean) return (Boolean) val;
        try {
            return Integer.parseInt(String.valueOf(val)) == 1;
        } catch (Exception e) {
            return def;
        }
    }

    private static long getLong(JSONObject obj, String key, long def) {
        Object val = obj.get(key);
        return val != null ? parseLong(val, def) : def;
    }

    private static byte parseByte(Object val, byte def) {
        try {
            return Byte.parseByte(String.valueOf(val));
        } catch (Exception e) {
            return def;
        }
    }

    private static long parseLong(Object val, long def) {
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (Exception e) {
            return def;
        }
    }
}

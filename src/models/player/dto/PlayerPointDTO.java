package models.player.dto;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.Logger;

/**
 * PlayerPointDTO: Mô hình chỉ số nhân vật định kiểu Type-Safe.
 * Tự động hỗ trợ cả định dạng JSON Object mới lẫn mảng JSON Array legacy cũ,
 * triệt tiêu hoàn toàn lỗi IndexOutOfBoundsException khi đọc cơ sở dữ liệu.
 */
public class PlayerPointDTO {
    public byte limitPower = 0;
    public long power = 1000;
    public long tiemNang = 1000;
    public short stamina = 10000;
    public short maxStamina = 10000;
    public long hpg = 100;
    public long mpg = 100;
    public long dameg = 10;
    public int defg = 0;
    public byte critg = 0;
    public int nangDong = 0;
    public long hp = 100;
    public long mp = 100;

    public static PlayerPointDTO fromJson(String jsonStr) {
        PlayerPointDTO dto = new PlayerPointDTO();
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return dto;
        }

        try {
            Object parsed = JSONValue.parse(jsonStr.trim());
            if (parsed instanceof JSONObject) {
                JSONObject obj = (JSONObject) parsed;
                dto.limitPower = getByte(obj, "limitPower", (byte) 0);
                dto.power = getLong(obj, "power", 1000L);
                dto.tiemNang = getLong(obj, "tiemNang", 1000L);
                dto.stamina = getShort(obj, "stamina", (short) 10000);
                dto.maxStamina = getShort(obj, "maxStamina", (short) 10000);
                dto.hpg = getLong(obj, "hpg", 100L);
                dto.mpg = getLong(obj, "mpg", 100L);
                dto.dameg = getLong(obj, "dameg", 10L);
                dto.defg = getInt(obj, "defg", 0);
                dto.critg = getByte(obj, "critg", (byte) 0);
                dto.nangDong = getInt(obj, "nangDong", 0);
                dto.hp = getLong(obj, "hp", dto.hpg);
                dto.mp = getLong(obj, "mp", dto.mpg);
            } else if (parsed instanceof JSONArray) {
                JSONArray arr = (JSONArray) parsed;
                int size = arr.size();
                if (size > 0) dto.limitPower = parseByte(arr.get(0), (byte) 0);
                if (size > 1) dto.power = parseLong(arr.get(1), 1000L);
                if (size > 2) dto.tiemNang = parseLong(arr.get(2), 1000L);
                if (size > 3) dto.stamina = parseShort(arr.get(3), (short) 10000);
                if (size > 4) dto.maxStamina = parseShort(arr.get(4), (short) 10000);
                if (size > 5) dto.hpg = parseLong(arr.get(5), 100L);
                if (size > 6) dto.mpg = parseLong(arr.get(6), 100L);
                if (size > 7) dto.dameg = parseLong(arr.get(7), 10L);
                if (size > 8) dto.defg = parseInt(arr.get(8), 0);
                if (size > 9) dto.critg = parseByte(arr.get(9), (byte) 0);
                if (size > 10) dto.nangDong = parseInt(arr.get(10), 0);
                if (size > 11) dto.hp = parseLong(arr.get(11), dto.hpg);
                if (size > 12) dto.mp = parseLong(arr.get(12), dto.mpg);
            }
        } catch (Exception e) {
            Logger.logException(PlayerPointDTO.class, e, "Lỗi đọc dữ liệu PlayerPointDTO từ JSON: " + jsonStr);
        }
        return dto;
    }

    private static byte getByte(JSONObject obj, String key, byte def) {
        Object val = obj.get(key);
        return val != null ? parseByte(val, def) : def;
    }

    private static short getShort(JSONObject obj, String key, short def) {
        Object val = obj.get(key);
        return val != null ? parseShort(val, def) : def;
    }

    private static int getInt(JSONObject obj, String key, int def) {
        Object val = obj.get(key);
        return val != null ? parseInt(val, def) : def;
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

    private static short parseShort(Object val, short def) {
        try {
            return Short.parseShort(String.valueOf(val));
        } catch (Exception e) {
            return def;
        }
    }

    private static int parseInt(Object val, int def) {
        try {
            return Integer.parseInt(String.valueOf(val));
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

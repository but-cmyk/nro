package managers.map;

import utils.FileIO;
import utils.Logger;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * High-Performance In-Memory Map Data Manager
 * Pre-loads and caches all binary map files (item_bg_map_data, tile_map_data, eff_map) in RAM.
 * Eliminates synchronous Disk I/O bottlenecks during mass player teleports.
 */
public class MapDataManager {

    private static MapDataManager instance;

    private final Map<Integer, byte[]> bgItemDataCache = new ConcurrentHashMap<>();
    private final Map<Integer, byte[]> tileMapDataCache = new ConcurrentHashMap<>();
    private final Map<Integer, byte[]> effMapDataCache = new ConcurrentHashMap<>();

    public static MapDataManager gI() {
        if (instance == null) {
            instance = new MapDataManager();
        }
        return instance;
    }

    private MapDataManager() {
    }

    public void loadAllMapData() {
        long startTime = System.currentTimeMillis();
        bgItemDataCache.clear();
        tileMapDataCache.clear();
        effMapDataCache.clear();

        int bgCount = loadDirectory("data/map/item_bg_map_data", bgItemDataCache);
        int tileCount = loadDirectory("data/map/tile_map_data", tileMapDataCache);
        int effCount = loadDirectory("data/map/eff_map", effMapDataCache);

        long timeTaken = System.currentTimeMillis() - startTime;
        Logger.success("Successfully loaded In-Memory Map Cache in " + timeTaken + " ms! (BG Items: "
                + bgCount + ", Tile Maps: " + tileCount + ", Eff Maps: " + effCount + ")\n");
    }

    private int loadDirectory(String dirPath, Map<Integer, byte[]> cache) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }

        int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                try {
                    int mapId = Integer.parseInt(file.getName().trim());
                    byte[] data = FileIO.readFile(file.getAbsolutePath());
                    if (data != null && data.length > 0) {
                        cache.put(mapId, data);
                        count++;
                    }
                } catch (Exception ignored) {
                    // Ignore non-numeric filenames
                }
            }
        }
        return count;
    }

    public byte[] getBgItemData(int mapId) {
        byte[] data = bgItemDataCache.get(mapId);
        if (data == null) {
            // Lazy load fallback if not pre-cached
            try {
                data = FileIO.readFile("data/map/item_bg_map_data/" + mapId);
                if (data != null && data.length > 0) {
                    bgItemDataCache.put(mapId, data);
                }
            } catch (Exception ignored) {
            }
        }
        return data;
    }

    public byte[] getTileMapData(int mapId) {
        byte[] data = tileMapDataCache.get(mapId);
        if (data == null) {
            try {
                data = FileIO.readFile("data/map/tile_map_data/" + mapId);
                if (data != null && data.length > 0) {
                    tileMapDataCache.put(mapId, data);
                }
            } catch (Exception ignored) {
            }
        }
        return data;
    }

    public byte[] getEffMapData(int mapId) {
        byte[] data = effMapDataCache.get(mapId);
        if (data == null) {
            try {
                data = FileIO.readFile("data/map/eff_map/" + mapId);
                if (data != null && data.length > 0) {
                    effMapDataCache.put(mapId, data);
                }
            } catch (Exception ignored) {
            }
        }
        return data;
    }

    public void reloadMapData() {
        loadAllMapData();
    }
}

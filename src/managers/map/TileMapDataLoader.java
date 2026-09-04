package managers.map;

import consts.ConstMap;
import models.Template.MapTemplate;
import models.map.Map;
import models.npc.NonInteractiveNPC;
import server.Manager;
import services.map.MapService;
import services.map.WeatherService;
import utils.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Chuyên trách nạp dữ liệu nhị phân bản đồ (tile_set_info, tile_map_data)
 * và khởi tạo cấu trúc map thế giới trong NRO.
 * Tách biệt khỏi Manager.java theo Single Responsibility Principle (SRP).
 */
public class TileMapDataLoader {

    private static TileMapDataLoader instance;

    public static synchronized TileMapDataLoader gI() {
        if (instance == null) {
            instance = new TileMapDataLoader();
        }
        return instance;
    }

    private TileMapDataLoader() {
    }

    /**
     * Khởi tạo toàn bộ danh sách Map trong game từ MapTemplate.
     */
    public void initAllMaps() {
        MapDataManager.gI().loadAllMapData();
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
        if (Manager.MAP_TEMPLATES == null) {
            Logger.error("MAP_TEMPLATES is null, cannot init maps!");
            return;
        }

        for (MapTemplate mapTemp : Manager.MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = (tileTyleTop != null && mapTemp.tileId - 1 >= 0 && mapTemp.tileId - 1 < tileTyleTop.length)
                    ? tileTyleTop[mapTemp.tileId - 1]
                    : new int[0];

            Map map = new Map(mapTemp.id,
                    mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                    mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                    mapTemp.zones,
                    mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps);

            WeatherService.gI().initWeatherForMap(map);
            Manager.MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
        }
        new NonInteractiveNPC().initNonInteractiveNPC();
        Logger.log(Logger.GREEN, "Init map successful! (" + Manager.MAPS.size() + " maps)\n");
    }

    /**
     * Đọc chỉ số loại tile từ file nhị phân data/map/tile_set_info.
     * @param tileTypeFocus Loại tile cần lọc (ví dụ ConstMap.TILE_TOP)
     * @return ma trận [tileMapId][tileType]
     */
    public int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_set_info"))) {
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.logException(MapService.class, e);
        }
        return tileIndexTileType;
    }

    /**
     * Đọc ma trận tile map từ cache RAM (MapDataManager) hoặc file nhị phân.
     * @param mapId Mã map
     * @return ma trận tile [height][width]
     */
    public int[][] readTileMap(int mapId) {
        byte[] cachedData = MapDataManager.gI().getTileMapData(mapId);
        if (cachedData != null && cachedData.length > 0) {
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(cachedData))) {
                int w = dis.readByte();
                int h = dis.readByte();
                int[][] tileMap = new int[h][w];
                for (int[] tm : tileMap) {
                    for (int j = 0; j < tm.length; j++) {
                        tm[j] = dis.readByte();
                    }
                }
                return tileMap;
            } catch (IOException ignored) {
            }
        }

        // Fallback đọc trực tiếp từ đĩa nếu cache chưa sẵn sàng
        int[][] tileMap = null;
        try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_map_data/" + mapId))) {
            int w = dis.readByte();
            int h = dis.readByte();
            tileMap = new int[h][w];
            for (int[] tm : tileMap) {
                for (int j = 0; j < tm.length; j++) {
                    tm[j] = dis.readByte();
                }
            }
        } catch (IOException ignored) {
        }
        return tileMap;
    }
}

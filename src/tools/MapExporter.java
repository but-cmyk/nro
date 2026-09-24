package tools;

import utils.FileIO;
import utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;

/**
 * Tiled Map Editor (.tmx / .json) to NRO Binary Converter Pipeline
 * Allows map designers to visually craft multi-layered maps in Tiled Editor
 * and export directly to NRO server binary data formats.
 */
public class MapExporter {

    private static final int TILE_SIZE = 24;

    /**
     * Converts a 2D tile grid into NRO binary tile_map_data file.
     * Binary Format:
     * - byte: 0 (header/unused)
     * - byte: tileMapWidth (in tiles)
     * - byte: tileMapHeight (in tiles)
     * - byte[width * height]: tile indices
     */
    public static byte[] exportTileMap(int widthInTiles, int heightInTiles, int[][] tileGrid) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(0);
            dos.writeByte((byte) widthInTiles);
            dos.writeByte((byte) heightInTiles);

            for (int y = 0; y < heightInTiles; y++) {
                for (int x = 0; x < widthInTiles; x++) {
                    byte tileIndex = (tileGrid != null && y < tileGrid.length && x < tileGrid[y].length)
                            ? (byte) tileGrid[y][x]
                            : 0;
                    dos.writeByte(tileIndex);
                }
            }
            dos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            Logger.logException(MapExporter.class, e);
            return new byte[0];
        }
    }

    /**
     * Saves exported tile map to target file.
     */
    public static boolean saveTileMapFile(int mapId, byte[] data) {
        try {
            File dir = new File("data/map/tile_map_data");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileIO.writeFile("data/map/tile_map_data/" + mapId, data);
            return true;
        } catch (Exception e) {
            Logger.logException(MapExporter.class, e);
            return false;
        }
    }
}

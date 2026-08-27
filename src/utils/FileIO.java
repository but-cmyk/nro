package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileIO {

    public static byte[] readFile(String url) {
        try {
            // Cách 1: Dùng NIO gọn gàng, an toàn
            return Files.readAllBytes(Paths.get(url));

        } catch (IOException e) {
          // Logger.error("Error readFile");
            return null;
        }
    }

    public static void writeFile(String url, byte[] data) {
        try {
            Files.write(Paths.get(url), data);
        } catch (IOException e) {
            Logger.logException(FileIO.class, e);
        }
    }
}

package utils;

import java.io.File;
import java.io.IOException;

public class FileRunner {

    public static boolean isContainerOrLinux() {
        return new File("/.dockerenv").exists()
                || System.getenv("DOCKER_CONTAINER") != null
                || "true".equalsIgnoreCase(System.getProperty("server.container"))
                || !System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public static void runBatchFile(String batchFilePath) throws IOException {
        if (isContainerOrLinux()) {
            // Trong môi trường Docker hoặc Linux, không gọi batch file Windows.
            // Supervisor (Docker restart: unless-stopped / systemd) sẽ tự khởi động lại process.
            return;
        }
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", batchFilePath);
        processBuilder.start();
    }
}


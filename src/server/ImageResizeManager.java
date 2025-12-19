package server;

import utils.Logger;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageResizeManager {

    private static ImageResizeManager instance;

    // Đường dẫn thư mục icon
    private static final String ICON_BASE_PATH = "data/icon/";
    private static final String[] SCALE_FOLDERS = {"x4", "x3", "x2", "x1"};
    private static final int[] SCALES = {4, 3, 2, 1};

    public static ImageResizeManager gI() {
        if (instance == null) {
            instance = new ImageResizeManager();
        }
        return instance;
    }

    /**
     * Khởi động tự động resize khi server start
     */
    public void initAutoResize() {
        Logger.log(Logger.YELLOW, "Khởi động ImageResizeManager...");
        createScaleFolders();

        // Chỉ resize các icon mới để tiết kiệm thời gian
        resizeNewIconsOnly();

        // In thống kê
        printIconStatistics();
    }

    /**
     * Tự động resize tất cả icon từ x4 xuống x3, x2, x1
     */
    public void autoResizeAllIcons() {
        Logger.log(Logger.YELLOW, "Bắt đầu tự động resize icons...");

        try {
            File x4Folder = new File(ICON_BASE_PATH + "x4");

            if (!x4Folder.exists() || !x4Folder.isDirectory()) {
                Logger.error("Không tìm thấy thư mục x4!");
                return;
            }

            // Tạo các thư mục x3, x2, x1 nếu chưa có
            createScaleFolders();

            // Lấy tất cả file PNG trong thư mục x4
            File[] x4Files = x4Folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

            if (x4Files == null || x4Files.length == 0) {
                Logger.error("Không tìm thấy file PNG nào trong thư mục x4!");
                return;
            }

            int successCount = 0;
            int errorCount = 0;
            int totalFiles = x4Files.length;

            Logger.log(Logger.YELLOW, "Đang xử lý " + totalFiles + " icons...");

            // Resize từng file
            for (File sourceFile : x4Files) {
                try {
                    if (resizeImageToAllScales(sourceFile)) {
                        successCount++;
                    } else {
                        errorCount++;
                    }

                    // In progress
                    if ((successCount + errorCount) % 100 == 0) {
                        Logger.log(Logger.BLUE, String.format("Progress: %d/%d (Success: %d, Error: %d)",
                                successCount + errorCount, totalFiles, successCount, errorCount));
                    }
                } catch (Exception e) {
                    errorCount++;
                    // Không log lỗi để tránh spam console
                }
            }

            Logger.log(Logger.GREEN, String.format(
                    "Hoàn thành resize! Success: %d/%d | Error: %d",
                    successCount, totalFiles, errorCount
            ));

        } catch (Exception e) {
            Logger.logException(ImageResizeManager.class, e, "Lỗi trong autoResizeAllIcons");
        }
    }

    /**
     * Resize một icon cụ thể từ x4 xuống tất cả các scale khác
     */
    public boolean resizeImageToAllScales(File sourceFile) throws IOException {
        String fileName = sourceFile.getName();

        // Kiểm tra file size trước
        if (sourceFile.length() == 0) {
            return false; // File rỗng, bỏ qua
        }

        BufferedImage sourceImage = null;
        try {
            sourceImage = ImageIO.read(sourceFile);
        } catch (Exception e) {
            return false; // Không đọc được file
        }

        if (sourceImage == null) {
            return false; // Image null
        }

        int originalWidth = sourceImage.getWidth();
        int originalHeight = sourceImage.getHeight();

        // Kiểm tra kích thước hợp lệ
        if (originalWidth <= 0 || originalHeight <= 0) {
            return false; // Kích thước không hợp lệ
        }

        // Resize xuống x3, x2, x1 (bỏ qua x4 vì là source)
        for (int i = 1; i < SCALES.length; i++) {
            int scale = SCALES[i];
            String targetFolder = ICON_BASE_PATH + SCALE_FOLDERS[i];

            // Tính kích thước mới
            int newWidth = (originalWidth * scale) / 4;
            int newHeight = (originalHeight * scale) / 4;

            // Đảm bảo kích thước tối thiểu là 1x1
            if (newWidth < 1) newWidth = 1;
            if (newHeight < 1) newHeight = 1;

            // Resize image
            BufferedImage resizedImage = resizeImage(sourceImage, newWidth, newHeight);

            // Lưu file
            File outputFile = new File(targetFolder, fileName);
            ImageIO.write(resizedImage, "PNG", outputFile);
        }

        return true;
    }

    /**
     * Resize một icon từ thư mục x4 theo tên file
     */
    public boolean resizeIconByName(String fileName) {
        try {
            if (!fileName.toLowerCase().endsWith(".png")) {
                fileName += ".png";
            }

            File sourceFile = new File(ICON_BASE_PATH + "x4/" + fileName);

            if (!sourceFile.exists()) {
                Logger.error("Không tìm thấy file: " + fileName);
                return false;
            }

            return resizeImageToAllScales(sourceFile);

        } catch (Exception e) {
            Logger.logException(ImageResizeManager.class, e, "Lỗi khi resize icon: " + fileName);
            return false;
        }
    }

    /**
     * Resize image với thuật toán chất lượng cao
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // Đảm bảo kích thước tối thiểu
        if (targetWidth < 1) targetWidth = 1;
        if (targetHeight < 1) targetHeight = 1;

        // Tạo image mới với alpha channel
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();

        try {
            // Thiết lập rendering hints để có chất lượng tốt nhất
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            // Draw image đã resize
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }

        return resizedImage;
    }

    /**
     * Tạo các thư mục scale nếu chưa tồn tại
     */
    private void createScaleFolders() {
        for (String folder : SCALE_FOLDERS) {
            File dir = new File(ICON_BASE_PATH + folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    /**
     * Kiểm tra xem một icon đã được resize chưa
     */
    public boolean isIconResized(String fileName) {
        // Kiểm tra xem file có tồn tại trong tất cả các thư mục x3, x2, x1 không
        for (int i = 1; i < SCALE_FOLDERS.length; i++) {
            File file = new File(ICON_BASE_PATH + SCALE_FOLDERS[i] + "/" + fileName);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resize chỉ các icon mới (chưa có trong x3, x2, x1)
     */
    public void resizeNewIconsOnly() {
        Logger.log(Logger.YELLOW, "Đang kiểm tra và resize các icon mới...");

        try {
            File x4Folder = new File(ICON_BASE_PATH + "x4");

            if (!x4Folder.exists() || !x4Folder.isDirectory()) {
                Logger.error("Không tìm thấy thư mục x4!");
                return;
            }

            File[] x4Files = x4Folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

            if (x4Files == null || x4Files.length == 0) {
                Logger.log(Logger.YELLOW, "Không có file PNG nào trong thư mục x4");
                return;
            }

            int newIconCount = 0;
            int errorIconCount = 0;
            int totalFiles = x4Files.length;

            for (File file : x4Files) {
                if (!isIconResized(file.getName())) {
                    try {
                        if (resizeImageToAllScales(file)) {
                            newIconCount++;
                        } else {
                            errorIconCount++;
                        }
                    } catch (Exception e) {
                        errorIconCount++;
                    }
                }
            }

            if (newIconCount > 0) {
                Logger.log(Logger.GREEN, String.format(
                        "Đã resize %d icon mới từ tổng %d icons (Lỗi: %d)",
                        newIconCount, totalFiles, errorIconCount
                ));
            } else {
                Logger.log(Logger.GREEN, String.format(
                        "Tất cả %d icons đã được resize (Bỏ qua %d file lỗi)",
                        totalFiles, errorIconCount
                ));
            }

        } catch (Exception e) {
            Logger.logException(ImageResizeManager.class, e);
        }
    }

    /**
     * Xóa tất cả icon đã resize (x3, x2, x1) để resize lại từ đầu
     */
    public void clearResizedIcons() {
        Logger.log(Logger.YELLOW, "Đang xóa các icon đã resize...");

        int totalDeleted = 0;

        try {
            // Bỏ qua x4, chỉ xóa x3, x2, x1
            for (int i = 1; i < SCALE_FOLDERS.length; i++) {
                File folder = new File(ICON_BASE_PATH + SCALE_FOLDERS[i]);
                if (folder.exists() && folder.isDirectory()) {
                    File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
                    if (files != null) {
                        for (File file : files) {
                            if (file.delete()) {
                                totalDeleted++;
                            }
                        }
                        Logger.success("Đã xóa icon trong thư mục: " + SCALE_FOLDERS[i]);
                    }
                }
            }

            Logger.log(Logger.GREEN, "Đã xóa tổng cộng " + totalDeleted + " icons");

        } catch (Exception e) {
            Logger.logException(ImageResizeManager.class, e);
        }
    }

    /**
     * Lấy thông tin thống kê về các icon
     */
    public void printIconStatistics() {
        try {
            Logger.log(Logger.BLUE, "========== THỐNG KÊ ICON ==========");

            for (String folder : SCALE_FOLDERS) {
                File dir = new File(ICON_BASE_PATH + folder);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
                    int count = files != null ? files.length : 0;
                    Logger.log(Logger.BLUE, String.format("%-4s: %5d icons", folder, count));
                } else {
                    Logger.log(Logger.YELLOW, String.format("%-4s: Chưa tồn tại", folder));
                }
            }

            Logger.log(Logger.BLUE, "===================================");

        } catch (Exception e) {
            Logger.logException(ImageResizeManager.class, e);
        }
    }

    /**
     * Force resize một số icon cụ thể (dùng khi cần update icon)
     */
    public void forceResizeIcons(String... fileNames) {
        Logger.log(Logger.YELLOW, "Force resize " + fileNames.length + " icons...");

        int successCount = 0;

        for (String fileName : fileNames) {
            if (resizeIconByName(fileName)) {
                successCount++;
                Logger.success("Đã resize: " + fileName);
            } else {
                Logger.error("Lỗi resize: " + fileName);
            }
        }

        Logger.log(Logger.GREEN, String.format(
                "Force resize hoàn tất: %d/%d thành công",
                successCount, fileNames.length
        ));
    }

    /**
     * Kiểm tra và xóa các file icon lỗi trong x4
     */
    public void checkAndRemoveCorruptedIcons() {
        Logger.log(Logger.YELLOW, "Đang kiểm tra các file icon lỗi...");

        try {
            File x4Folder = new File(ICON_BASE_PATH + "x4");
            File[] x4Files = x4Folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

            if (x4Files == null) return;

            int corruptedCount = 0;

            for (File file : x4Files) {
                try {
                    // Kiểm tra file size
                    if (file.length() == 0) {
                        Logger.log(Logger.YELLOW, "File rỗng: " + file.getName());
                        corruptedCount++;
                        continue;
                    }

                    // Thử đọc image
                    BufferedImage img = ImageIO.read(file);
                    if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
                        Logger.log(Logger.YELLOW, "File lỗi: " + file.getName());
                        corruptedCount++;
                    }
                } catch (Exception e) {
                    Logger.log(Logger.YELLOW, "File không đọc được: " + file.getName());
                    corruptedCount++;
                }
            }

            if (corruptedCount > 0) {
                Logger.log(Logger.YELLOW, "Tìm thấy " + corruptedCount + " file icon lỗi");
            } else {
                Logger.log(Logger.GREEN, "Tất cả icon trong x4 đều hợp lệ");
            }

        } catch (Exception e) {
            Logger.logException(ImageResizeManager.class, e);
        }
    }
}
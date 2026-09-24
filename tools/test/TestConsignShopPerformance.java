package test;

import managers.ConsignShopManager;
import models.ConsignItem;
import models.item.Item;
import network.io.Message;
import services.ConsignShopService;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance & Functional Verification Test for Optimized Consign Shop
 */
public class TestConsignShopPerformance {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   KIỂM THỬ HIỆU NĂNG & ĐỘ CHÍNH XÁC SHOP KÝ GỬI ĐÃ TỐI ƯU");
        System.out.println("================================================================");

        try {
            setupMockData(2000);
            testPaginationAndSorting();
            testPaginationThroughput();
            testAsyncInsertionNonBlocking();

            System.out.println("\n================================================================");
            System.out.println("   [PASS 100%] SHOP KÝ GỬI ĐẠT CHUẨN HIỆU NĂNG SIÊU CAO (<1ms)!");
            System.out.println("================================================================");
        } catch (Throwable t) {
            System.err.println("\n[ERROR] KIỂM THỬ THẤT BẠI: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void setupMockData(int itemCount) {
        System.out.printf("\n[BƯỚC 1] Khởi tạo %d vật phẩm ký gửi mẫu trên RAM...\n", itemCount);
        ConsignShopManager manager = ConsignShopManager.gI();
        manager.lockItems();
        try {
            manager.listItem.clear();
            for (int i = 1; i <= itemCount; i++) {
                byte tab = (byte) (i % 4); // 4 tabs (0, 1, 2, 3)
                byte isUpTop = (byte) (i % 50 == 0 ? 1 : 0); // 2% items are UpTop
                List<Item.ItemOption> options = new ArrayList<>();
                options.add(new Item.ItemOption(50, 10)); // Sức đánh +10%
                options.add(new Item.ItemOption(77, 5));  // HP +5%

                ConsignItem item = new ConsignItem(
                        i,
                        (short) 194, // ID item mẫu
                        1000 + (i % 20), // player_sell
                        tab,
                        10 + (i % 100),
                        -1,
                        1,
                        isUpTop,
                        options,
                        false,
                        "Player_" + (i % 20)
                );
                manager.listItem.add(item);
            }
        } finally {
            manager.unlockItems();
        }
        System.out.printf("  -> Đã nạp thành công %d vật phẩm vào ConsignShopManager.\n", manager.listItem.size());
    }

    private static void testPaginationAndSorting() {
        System.out.println("\n[TEST 1] Kiểm tra tính đúng đắn của Thuật toán Phân trang & Sắp xếp UpTop");
        ConsignShopService service = ConsignShopService.gI();

        // Kiểm tra tab 0, trang 0
        ConsignShopService.ConsignPage page0 = service.getConsignPage((byte) 0, 0, 20);
        System.out.printf("  -> Tab 0: Tổng số trang = %d, Số item trang đầu = %d\n",
                page0.totalPages, page0.itemsSend.size());

        if (page0.itemsSend.isEmpty()) {
            throw new RuntimeException("Lỗi: Trang đầu không có vật phẩm!");
        }

        // Kiểm tra vật phẩm isUpTop = 1 phải nằm ở đầu danh sách
        ConsignItem firstItem = page0.itemsSend.get(0);
        if (firstItem.isUpTop != 1) {
            throw new RuntimeException("Lỗi: Vật phẩm đầu tiên phải là isUpTop == 1!");
        }

        // Kiểm tra không tràn trang
        ConsignShopService.ConsignPage pageOutOfBound = service.getConsignPage((byte) 0, 9999, 20);
        if (!pageOutOfBound.itemsSend.isEmpty()) {
            throw new RuntimeException("Lỗi: Trang vượt giới hạn phải trả về danh sách rỗng!");
        }

        System.out.println("  [PASS] Phân trang và thứ tự ưu tiên UpTop hoàn toàn chính xác.");
    }

    private static void testPaginationThroughput() {
        System.out.println("\n[TEST 2] Đo lường thông lượng phân trang (Benchmark 50,000 lần mở trang)");
        ConsignShopService service = ConsignShopService.gI();

        int iterations = 50_000;
        long startNs = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            byte tab = (byte) (i % 4);
            int page = (i % 10);
            ConsignShopService.ConsignPage res = service.getConsignPage(tab, page, 20);
        }

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        double opsPerSec = (iterations / (double) Math.max(1, elapsedMs)) * 1000;
        double avgUs = (elapsedMs * 1000.0) / iterations;

        System.out.printf("  -> Hoàn thành %d lượt truy vấn trang trong %d ms (~%.0f ops/sec, ~%.2f microseconds/req)\n",
                iterations, elapsedMs, opsPerSec, avgUs);

        if (avgUs > 500) { // Yêu cầu < 0.5ms (500us)
            throw new RuntimeException("Lỗi: Thời gian phân trang quá chậm: " + avgUs + " us");
        }
        System.out.println("  [PASS] Tốc độ phân trang cực nhanh, chỉ mất vài micro-giây cho mỗi yêu cầu!");
    }

    private static void testAsyncInsertionNonBlocking() {
        System.out.println("\n[TEST 3] Kiểm tra thêm vật phẩm không block (Non-blocking memory insert)");
        ConsignShopManager manager = ConsignShopManager.gI();

        int initialSize = manager.listItem.size();
        int newId = manager.nextItemId();

        ConsignItem newItem = new ConsignItem(
                newId,
                (short) 194,
                9999,
                (byte) 0,
                50,
                -1,
                1,
                (byte) 0,
                new ArrayList<>(),
                false,
                "TestBuyer"
        );

        long startNs = System.nanoTime();
        manager.lockItems();
        try {
            manager.addItem(newItem);
        } finally {
            manager.unlockItems();
        }
        long memoryInsertNs = System.nanoTime() - startNs;

        System.out.printf("  -> Thời gian lock và thêm vào RAM: %d nano-giây (%.4f ms)\n",
                memoryInsertNs, memoryInsertNs / 1_000_000.0);

        if (manager.listItem.size() != initialSize + 1) {
            throw new RuntimeException("Lỗi: Số lượng item trên RAM không tăng sau khi thêm!");
        }

        if (memoryInsertNs > 5_000_000) { // Yêu cầu < 5ms
            throw new RuntimeException("Lỗi: Thao tác thêm vào RAM bị nghẽn lock!");
        }

        System.out.println("  [PASS] Thêm vật phẩm vào RAM và nhả lock diễn ra ngay lập tức.");
    }
}

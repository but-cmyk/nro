package test;

import models.item.Item;
import models.player.Inventory;
import models.player.Player;
import services.player.InventoryService;
import services.func.Trade;

import java.util.ArrayList;
import java.util.UUID;

public class TestGroupBIntegrity {

    private static int passCount = 0;
    private static int failCount = 0;

    private static void assertCondition(boolean condition, String testName) {
        if (condition) {
            System.out.println("PASS: " + testName);
            passCount++;
        } else {
            System.err.println("FAIL: " + testName);
            failCount++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU NHOM B (DATA INTEGRITY & ECONOMY) ===");

        testB4InventoryOverflowAndCap();
        testB2TrySubQuantity();
        testB1TradeIdempotency();

        System.out.println("\n=== KET QUA KIEM THU ===");
        System.out.println("Tong so test: " + (passCount + failCount));
        System.out.println("PASS: " + passCount);
        System.out.println("FAIL: " + failCount);

        if (failCount > 0) {
            System.exit(1);
        } else {
            System.out.println("ALL GROUP B TESTS PASSED!");
            System.exit(0);
        }
    }

    private static void testB4InventoryOverflowAndCap() {
        System.out.println("\n--- Test B4: Chống tràn số Ngọc/Hồng ngọc và Cap 2 tỷ ---");
        Inventory inv = new Inventory();

        // 1. Thêm gem bình thường
        inv.addGem(500L);
        assertCondition(inv.gem == 500, "addGem(long) 500 -> gem == 500");

        // 2. Thêm số âm hoặc 0 -> không thay đổi
        inv.addGem(-100L);
        inv.addGem(0L);
        assertCondition(inv.gem == 500, "addGem với số <= 0 không thay đổi số dư");

        // 3. Thêm số lượng cực lớn -> Cap tại 2,000,000,000
        inv.addGem(3_000_000_000L);
        assertCondition(inv.gem == 2_000_000_000, "addGem vượt trần -> Cap đúng 2,000,000,000");

        // 4. subGemDirect không bị âm
        inv.subGemDirect(2_500_000_000L);
        assertCondition(inv.gem == 0, "subGemDirect trừ quá số dư -> gem == 0 (không bị âm)");

        // 5. Ruby tests
        inv.addRuby(1000L);
        assertCondition(inv.ruby == 1000, "addRuby(long) 1000 -> ruby == 1000");
        inv.addRuby(5_000_000_000L);
        assertCondition(inv.ruby == 2_000_000_000, "addRuby vượt trần -> Cap đúng 2,000,000,000");
        inv.subRuby(2_500_000_000L);
        assertCondition(inv.ruby == 0, "subRuby trừ quá số dư -> ruby == 0 (không bị âm)");
    }

    private static void testB2TrySubQuantity() {
        System.out.println("\n--- Test B2: Kiểm soát số lượng chặt chẽ với trySubQuantityItemsBag ---");
        InventoryService invService = InventoryService.gI();

        Player player = new Player();
        player.inventory = new Inventory();
        player.inventory.itemsBag = new ArrayList<>();

        Item item1 = new Item();
        item1.quantity = 10;
        player.inventory.itemsBag.add(item1);

        // 1. Trừ số lượng <= 0 -> false
        boolean resZero = invService.trySubQuantityItemsBag(player, item1, 0);
        boolean resNeg = invService.trySubQuantityItemsBag(player, item1, -5);
        assertCondition(!resZero && !resNeg, "trySubQuantity với số lượng <= 0 trả về false");
        assertCondition(item1.quantity == 10, "Số lượng không bị thay đổi khi tham số không hợp lệ");

        // 2. Trừ số lượng lớn hơn sở hữu -> false
        boolean resOver = invService.trySubQuantityItemsBag(player, item1, 15);
        assertCondition(!resOver, "trySubQuantity vượt quá số lượng sở hữu trả về false");
        assertCondition(item1.quantity == 10, "Số lượng giữ nguyên khi trừ thất bại");

        // 3. Trừ item không thuộc túi -> false (chống phantom item injection)
        Item phantomItem = new Item();
        phantomItem.quantity = 5;
        boolean resPhantom = invService.trySubQuantityItemsBag(player, phantomItem, 3);
        assertCondition(!resPhantom, "trySubQuantity với item không nằm trong túi trả về false");

        // 4. Trừ hợp lệ
        boolean resOk = invService.trySubQuantityItemsBag(player, item1, 4);
        assertCondition(resOk, "trySubQuantity hợp lệ trả về true");
        assertCondition(item1.quantity == 6, "Số lượng trong túi giảm chính xác còn 6");

        // 5. Trừ hết -> item bị xóa khỏi túi
        boolean resAll = invService.trySubQuantityItemsBag(player, item1, 6);
        assertCondition(resAll, "trySubQuantity hết số lượng trả về true");
        assertCondition(!player.inventory.itemsBag.contains(item1), "Item đã được tự động dọn khỏi túi khi quantity = 0");
    }

    private static void testB1TradeIdempotency() {
        System.out.println("\n--- Test B1: Trade Idempotency & UUID ---");
        Player p1 = new Player();
        p1.id = 1;
        p1.inventory = new Inventory();

        Player p2 = new Player();
        p2.id = 2;
        p2.inventory = new Inventory();

        Trade trade = new Trade(p1, p2);
        assertCondition(trade.tradeId != null && !trade.tradeId.isEmpty(), "Trade khởi tạo có tradeId không null");

        try {
            UUID parsed = UUID.fromString(trade.tradeId);
            assertCondition(parsed != null, "tradeId là UUID hợp lệ: " + trade.tradeId);
        } catch (Exception e) {
            assertCondition(false, "tradeId không phải UUID hợp lệ");
        }
    }
}

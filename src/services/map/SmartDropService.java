package services.map;

import java.util.ArrayList;
import java.util.List;
import models.item.Item;
import models.map.ItemMap;
import models.map.Zone;
import models.mob.Mob;
import models.player.Player;
import services.ItemService;
import utils.Util;

/**
 * Dịch vụ Rơi Đồ Thông Minh Cho Tân Thủ (Smart Adaptive Loot Engine)
 * - Phân tầng 8 Bậc trang bị theo chuẩn Shop 3 Làng (Bunma, Dende, Appule).
 * - Cơ chế bảo hiểm rớt đồ (Pity System) tích lũy chuỗi không rớt (+0.5%/con, 25 con = 100%).
 * - Thưởng ngẫu nhiên biến thiên chỉ số (+0% -> +15%) và lỗ Sao Pha Lê (1★ - 2★).
 * - Khóa giao dịch (Option 30) để bảo vệ kinh tế game.
 */
public class SmartDropService {

    private static SmartDropService instance;

    public static SmartDropService gI() {
        if (instance == null) {
            instance = new SmartDropService();
        }
        return instance;
    }

    // Ma trận 8 Tiers x 5 Slots x 3 Hành Tinh (ID lấy từ Shop Bunma, Dende, Appule)
    // Slot 0: Áo, Slot 1: Quần, Slot 2: Găng (SD), Slot 3: Giày, Slot 4: Rada
    // Gender: 0: Trái Đất, 1: Namếc, 2: Xayda
    public static final int[][][] GEAR_TEMPLATES = {
        // Tier 1 (Req SM ~1.5k)
        { {0, 1, 2}, {6, 7, 8}, {21, 22, 23}, {27, 28, 29}, {12, 12, 12} },
        // Tier 2 (Req SM ~15k)
        { {33, 41, 49}, {35, 43, 51}, {24, 46, 53}, {30, 47, 55}, {57, 57, 57} },
        // Tier 3 (Req SM ~50k)
        { {3, 4, 5}, {9, 10, 11}, {37, 25, 26}, {39, 31, 32}, {58, 58, 58} },
        // Tier 4 (Req SM ~200k)
        { {34, 42, 50}, {36, 44, 52}, {38, 45, 54}, {40, 48, 56}, {59, 59, 59} },
        // Tier 5 (Req SM ~800k)
        { {136, 152, 168}, {140, 156, 172}, {144, 160, 176}, {148, 164, 180}, {184, 184, 184} },
        // Tier 6 (Req SM ~3.2M)
        { {137, 153, 169}, {141, 157, 173}, {145, 161, 177}, {149, 165, 181}, {185, 185, 185} },
        // Tier 7 (Req SM ~10M)
        { {138, 154, 170}, {142, 158, 174}, {146, 162, 178}, {150, 166, 182}, {186, 186, 186} },
        // Tier 8 (Req SM ~20M)
        { {139, 155, 171}, {143, 159, 175}, {147, 163, 179}, {151, 167, 183}, {187, 187, 187} }
    };

    // Mốc Sức Mạnh chuẩn của Găng (món yêu cầu SM cao nhất trong từng Tier)
    public static final long[] TIER_POWERS = {
        1_500L,        // Tier 1
        15_000L,       // Tier 2
        50_000L,       // Tier 3
        200_000L,      // Tier 4
        800_000L,      // Tier 5
        3_200_000L,    // Tier 6
        10_000_000L,   // Tier 7
        20_000_000L    // Tier 8
    };

    public static final long MAX_POWER_APPLY = 50_000_000L;

    /**
     * Kiểm tra và thực hiện rơi trang bị thông minh khi hạ gục quái
     */
    public ItemMap rollDrop(Mob mob, Player player, int x, int yEnd) {
        if (mob == null || player == null || player.zone == null) {
            return null;
        }
        if (player.isBoss || player.isBot || player.nPoint == null) {
            return null;
        }
        if (player.nPoint.power >= MAX_POWER_APPLY) {
            return null;
        }

        player.checkResetSmartDropDaily();

        // 1. Kiểm tra xác suất rơi đồ (kèm Pity bảo hiểm)
        if (!shouldDropGear(player)) {
            return null;
        }

        // 2. Xác định Tier theo sức mạnh người chơi
        int targetTier = getTargetTier(player.nPoint.power);

        // Phân phối: 70% In-Tier (mặc được ngay), 20% Next-Tier (+1), 10% Sub-Tier (-1)
        int rollTier = Util.nextInt(1, 100);
        int selectedTier;
        if (rollTier <= 70) {
            selectedTier = targetTier;
        } else if (rollTier <= 90) {
            selectedTier = Math.min(targetTier + 1, GEAR_TEMPLATES.length - 1);
        } else {
            selectedTier = Math.max(targetTier - 1, 0);
        }

        // 3. Phân phối Slot: Găng (25%), Áo (20%), Quần (20%), Giày (20%), Rada (15%)
        int rollSlot = Util.nextInt(1, 100);
        int slot;
        if (rollSlot <= 25) {
            slot = 2; // Găng
        } else if (rollSlot <= 45) {
            slot = 0; // Áo
        } else if (rollSlot <= 65) {
            slot = 1; // Quần
        } else if (rollSlot <= 85) {
            slot = 3; // Giày
        } else {
            slot = 4; // Rada
        }

        // 4. Xác định Hành tinh (Gender)
        int gender = player.gender;
        if (gender < 0 || gender > 2) {
            gender = 0;
        }
        if (slot != 4) {
            // 85% rơi đúng hành tinh của người chơi, 15% rơi chéo
            if (!Util.isTrue(85, 100)) {
                gender = Util.nextInt(0, 2);
            }
        }

        int tempId = GEAR_TEMPLATES[selectedTier][slot][gender];

        // 5. Khởi tạo ItemMap và thuộc tính
        return createSmartDropItemMap(mob.zone, tempId, player, x, yEnd);
    }

    /**
     * Xác định Tier phù hợp nhất với Sức mạnh hiện tại của người chơi
     */
    public int getTargetTier(long power) {
        for (int i = TIER_POWERS.length - 1; i >= 0; i--) {
            if (power >= TIER_POWERS[i]) {
                return i;
            }
        }
        return 0; // Tier 1 (mặc định nếu < 1.500 SM)
    }

    /**
     * Kiểm tra xác suất rơi đồ với hệ thống Pity tích lũy
     */
    public boolean shouldDropGear(Player player) {
        // Pha 3: Đã nhặt trên 30 món trong ngày -> Giảm về tỉ lệ nền 0.2%
        if (player.smartDropCountToday >= 30) {
            return Util.isTrue(2, 1000);
        }

        // Hard Pity: Đánh trượt liên tiếp 25 quái -> 100% RƠI ĐỒ
        if (player.smartDropStreak >= 25) {
            return true;
        }

        // Pha 1 (1 -> 15 món đầu): Base rate = 5% (50/1000)
        // Pha 2 (16 -> 30 món):    Base rate = 2.5% (25/1000)
        int baseRate = (player.smartDropCountToday < 15) ? 50 : 25;

        // Tích lũy: Mỗi con quái đánh hụt cộng thêm +0.5% (5/1000)
        int totalRate = baseRate + (player.smartDropStreak * 5);

        return Util.isTrue(totalRate, 1000);
    }

    /**
     * Khởi tạo ItemMap với Option sao chép từ Shop, có biến thiên chỉ số và khóa Option 30
     */
    public ItemMap createSmartDropItemMap(Zone zone, int tempId, Player player, int x, int yEnd) {
        ItemMap it = new ItemMap(zone, tempId, 1, x, yEnd, player.id);
        List<Item.ItemOption> shopOps = ItemService.gI().getListOptionItemShop((short) tempId);
        List<Item.ItemOption> options = new ArrayList<>();

        if (shopOps != null && !shopOps.isEmpty()) {
            for (Item.ItemOption op : shopOps) {
                if (op == null || op.optionTemplate == null) {
                    continue;
                }
                Item.ItemOption newOp = new Item.ItemOption(op);
                int optId = newOp.optionTemplate.id;

                // Thưởng biến thiên ngẫu nhiên 0% -> 15% cho chỉ số chính:
                // 0: Sức đánh, 47: Giáp, 7 hoặc 22: HP, 8 hoặc 23: KI
                if (optId == 0 || optId == 47 || optId == 7 || optId == 22 || optId == 8 || optId == 23) {
                    int bonus = Util.nextInt(0, 15);
                    if (bonus > 0) {
                        int add = (newOp.param * bonus) / 100;
                        newOp.param += Math.max(add, 1);
                    }
                }
                options.add(newOp);
            }
        }

        // Roll ngẫu nhiên Sao Pha Lê (87% 0 sao, 12% 1 sao, 1% 2 sao)
        int starRoll = Util.nextInt(1, 100);
        if (starRoll == 1) {
            options.add(new Item.ItemOption(107, 2)); // 2 sao pha lê (1% cực phẩm)
        } else if (starRoll <= 13) {
            options.add(new Item.ItemOption(107, 1)); // 1 sao pha lê (12%)
        }

        // Option 208: Đồ rơi từ Quái (khi bán vào shop chỉ có giá 1 vàng, cho phép giao dịch giữa người chơi)
        options.add(new Item.ItemOption(208, 0));

        it.options = options;
        return it;
    }
}

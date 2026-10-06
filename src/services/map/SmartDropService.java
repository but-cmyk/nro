package services.map;

import java.util.ArrayList;
import java.util.List;
import models.item.Item;
import models.map.ItemMap;
import models.map.Zone;
import models.mob.Mob;
import models.player.Player;
import services.ItemService;
import services.player.InventoryService;
import utils.Util;

/**
 * Dịch vụ Rơi Đồ Thông Minh Cho Tân Thủ (Smart Adaptive Loot Engine)
 * - Phân tầng 8 Bậc trang bị theo chuẩn Shop 3 Làng (Bunma, Dende, Appule).
 * - Cơ chế bảo vệ chống trùng lặp (Anti-Duplicate & Missing-Slot Priority):
 *   + Ưu tiên 85%-90% rơi vào các slot còn thiếu (chưa mặc / chưa có trong túi).
 *   + Tuyệt đối không rơi trùng slot vừa rơi ở lần trước (lastSmartDropSlot).
 *   + Khóa 100% đúng Hành Tinh và đúng Tier cho tân thủ (SM < 50.000).
 * - Cơ chế bảo hiểm rớt đồ (Pity System) tích lũy chuỗi không rớt (+0.3%/con, 35 con = 100%).
 * - Bảo vệ hành trang tân thủ: Không kích hoạt rơi khi túi đồ đã đầy.
 * - Thưởng ngẫu nhiên biến thiên chỉ số (+0% -> +15%) và lỗ Sao Pha Lê (1★ - 2★).
 * - Option 208: Bán shop 1 vàng, cho phép giao dịch giữa người chơi.
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
        if (player.isBoss || player.isBot || player.isNewPet || player.nPoint == null) {
            return null;
        }
        if (player.nPoint.power >= MAX_POWER_APPLY) {
            return null;
        }

        player.checkResetSmartDropDaily();

        // 1. Kiểm tra xác suất rơi đồ (kèm Pity bảo hiểm và kiểm tra túi trống)
        if (!shouldDropGear(player)) {
            return null;
        }

        // 2. Xác định Tier theo sức mạnh người chơi
        int targetTier = getTargetTier(player.nPoint.power);
        int selectedTier;

        if (targetTier <= 0) {
            // Tân thủ < 15.000 SM: 100% đúng Tier 1 (mặc được ngay)
            selectedTier = 0;
        } else if (targetTier == 1) {
            // Sức mạnh 15.000 - 50.000 SM: 90% Tier 2, 10% Tier 1
            selectedTier = Util.isTrue(90, 100) ? 1 : 0;
        } else {
            // Tier 3 trở lên: Phân phối 70% In-Tier, 20% Next-Tier (+1), 10% Sub-Tier (-1)
            int rollTier = Util.nextInt(1, 100);
            if (rollTier <= 70) {
                selectedTier = targetTier;
            } else if (rollTier <= 90) {
                selectedTier = Math.min(targetTier + 1, GEAR_TEMPLATES.length - 1);
            } else {
                selectedTier = Math.max(targetTier - 1, 0);
            }
        }

        // 3. Phân phối Slot thông minh (Chống trùng lặp & Ưu tiên slot còn thiếu)
        int slot = selectSmartSlot(player, targetTier);

        // 4. Xác định Hành tinh (Gender)
        int gender = player.gender;
        if (gender < 0 || gender > 2) {
            gender = 0;
        }
        // Slot 4 là Rada (dùng chung cho cả 3 phái)
        if (slot != 4) {
            // Với tân thủ (Tier 1-3, SM < 200.000): 100% rơi đúng hành tinh của mình
            // Chỉ khi đạt Tier 4 trở lên mới có 10% rơi chéo phái để giao dịch
            if (selectedTier >= 3) {
                if (!Util.isTrue(90, 100)) {
                    gender = Util.nextInt(0, 2);
                }
            }
        }

        int tempId = GEAR_TEMPLATES[selectedTier][slot][gender];

        // Ghi nhận slot vừa rơi để ngăn chặn rơi trùng liên tiếp
        player.lastSmartDropSlot = slot;

        // 5. Khởi tạo ItemMap và thuộc tính
        return createSmartDropItemMap(mob.zone, tempId, player, x, yEnd);
    }

    /**
     * Thuật toán chọn Slot rơi đồ thông minh (Zero-GC):
     * - Quét trang bị trên người (itemsBody) và trong túi (itemsBag).
     * - Nếu còn slot chưa có đồ: Ưu tiên 85%-90% rơi vào các slot còn thiếu.
     * - Tránh rơi lại slot vừa nhặt ở lần trước (player.lastSmartDropSlot).
     */
    public int selectSmartSlot(Player player, int targetTier) {
        boolean[] hasSlot = new boolean[5];

        // 1. Quét trang bị đang mặc trên người (itemsBody)
        if (player.inventory != null && player.inventory.itemsBody != null) {
            int bodySize = player.inventory.itemsBody.size();
            for (int i = 0; i < 5 && i < bodySize; i++) {
                Item it = player.inventory.itemsBody.get(i);
                if (it != null && it.isNotNullItem() && it.template != null) {
                    int type = it.template.type;
                    if (type >= 0 && type <= 4) {
                        hasSlot[type] = true;
                    }
                }
            }
        }

        // 2. Quét trang bị hợp lệ trong hành trang (itemsBag)
        if (player.inventory != null && player.inventory.itemsBag != null) {
            int bagSize = player.inventory.itemsBag.size();
            for (int i = 0; i < bagSize; i++) {
                Item it = player.inventory.itemsBag.get(i);
                if (it != null && it.isNotNullItem() && it.template != null) {
                    int type = it.template.type;
                    if (type >= 0 && type <= 4) {
                        // Rada (type 4) hoặc đồ cùng phái (hoặc gender > 2 dùng chung)
                        if (type == 4 || it.template.gender == player.gender || it.template.gender > 2) {
                            hasSlot[type] = true;
                        }
                    }
                }
            }
        }

        // 3. Tìm các slot còn thiếu
        int[] missing = new int[5];
        int missingCount = 0;
        for (int i = 0; i < 5; i++) {
            if (!hasSlot[i]) {
                missing[missingCount++] = i;
            }
        }

        int selectedSlot;
        int missingPriorityRate = (targetTier <= 1) ? 90 : 80;

        // Nếu còn slot thiếu: Ưu tiên cao rơi vào các slot còn thiếu
        if (missingCount > 0 && Util.isTrue(missingPriorityRate, 100)) {
            if (missingCount > 1) {
                // Lọc tránh rơi lại lastSmartDropSlot nếu còn slot thiếu khác
                int[] candidates = new int[missingCount];
                int candCount = 0;
                for (int i = 0; i < missingCount; i++) {
                    if (missing[i] != player.lastSmartDropSlot) {
                        candidates[candCount++] = missing[i];
                    }
                }
                if (candCount > 0) {
                    selectedSlot = candidates[Util.nextInt(0, candCount - 1)];
                } else {
                    selectedSlot = missing[Util.nextInt(0, missingCount - 1)];
                }
            } else {
                selectedSlot = missing[0];
            }
        } else {
            // Đã đủ bộ (hoặc 10-20% roll nâng cấp chỉ số):
            // Chống trùng lặp liên tiếp: Loại bỏ slot vừa rơi gần nhất
            int[] candidates = new int[5];
            int candCount = 0;
            for (int i = 0; i < 5; i++) {
                if (i != player.lastSmartDropSlot) {
                    candidates[candCount++] = i;
                }
            }
            if (candCount > 0) {
                selectedSlot = candidates[Util.nextInt(0, candCount - 1)];
            } else {
                selectedSlot = Util.nextInt(0, 4);
            }
        }

        return selectedSlot;
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
     * Kiểm tra xác suất rơi đồ với hệ thống Pity tích lũy và bảo vệ hành trang
     */
    public boolean shouldDropGear(Player player) {
        // Bảo vệ hành trang tân thủ: Không rơi nếu túi đồ đã đầy
        if (player.inventory == null || InventoryService.gI().getCountEmptyBag(player) <= 0) {
            return false;
        }

        // Pha 3: Đã nhặt trên 30 món trong ngày -> Giảm về tỉ lệ nền 0.2%
        if (player.smartDropCountToday >= 30) {
            return Util.isTrue(2, 1000);
        }

        // Hard Pity: Đánh trượt liên tiếp 35 quái -> 100% RƠI ĐỒ
        if (player.smartDropStreak >= 35) {
            return true;
        }

        // Pha 1 (1 -> 15 món đầu): Base rate = 3.5% (35/1000)
        // Pha 2 (16 -> 30 món):    Base rate = 2.0% (20/1000)
        int baseRate = (player.smartDropCountToday < 15) ? 35 : 20;

        // Tích lũy: Mỗi con quái đánh hụt cộng thêm +0.3% (3/1000)
        int totalRate = baseRate + (player.smartDropStreak * 3);

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

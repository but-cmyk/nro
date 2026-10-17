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
 * - Cơ chế bảo vệ chống trùng lặp tuyệt đối (100% Strict Anti-Duplicate):
 *   + Quét đa tầng Zero-GC: Trang bị trên người (itemsBody), trong túi (itemsBag),
 *     vật phẩm đang rơi trên đất (zone.items) và lịch sử đã rơi (smartDropTierHistory).
 *   + 100% chỉ chọn trong các slot chưa từng rơi / chưa sở hữu của targetTier.
 *   + Cơ chế Tier Progression Lock: Khi đã thu thập đủ 5 món của Tier hiện tại,
 *     lập tức DỪNG RƠI để bảo vệ hành trang tân thủ và tránh rác map, chỉ mở khóa
 *     khi người chơi thăng tiến Sức Mạnh đạt mốc Tier tiếp theo.
 *   + Khóa 100% đúng Hành Tinh cho tân thủ.
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
    public static final long POWER_THRESHOLD_HIGH = 9_000_000L; // Mốc 9 triệu sức mạnh: chuyển sang tỉ lệ RẤT KHÓ RƠI

    // Fast O(1) Zero-GC lookup tables tra cứu Tier và Slot của từng ID trang bị
    private static final byte[] GEAR_TIER_LOOKUP = new byte[300];
    private static final byte[] GEAR_SLOT_LOOKUP = new byte[300];

    static {
        for (int i = 0; i < GEAR_TIER_LOOKUP.length; i++) {
            GEAR_TIER_LOOKUP[i] = -1;
            GEAR_SLOT_LOOKUP[i] = -1;
        }
        for (int t = 0; t < GEAR_TEMPLATES.length; t++) {
            for (int s = 0; s < 5; s++) {
                for (int g = 0; g < 3; g++) {
                    int id = GEAR_TEMPLATES[t][s][g];
                    if (id >= 0 && id < GEAR_TIER_LOOKUP.length) {
                        GEAR_TIER_LOOKUP[id] = (byte) t;
                        GEAR_SLOT_LOOKUP[id] = (byte) s;
                    }
                }
            }
        }
    }

    public static int getGearTier(int tempId) {
        if (tempId >= 0 && tempId < GEAR_TIER_LOOKUP.length) {
            return GEAR_TIER_LOOKUP[tempId];
        }
        return -1;
    }

    public static int getGearSlot(int tempId) {
        if (tempId >= 0 && tempId < GEAR_SLOT_LOOKUP.length) {
            return GEAR_SLOT_LOOKUP[tempId];
        }
        return -1;
    }

    /**
     * Kiểm tra xem người chơi đã sở hữu hoặc đã từng rơi trang bị tương ứng với slot ở targetTier chưa.
     * Quét đa tầng Zero-GC:
     * 1. Lịch sử rơi theo Tier (smartDropTierHistory)
     * 2. Trang bị trên người (itemsBody) có tier >= targetTier
     * 3. Trang bị trong hành trang (itemsBag) có tier >= targetTier
     * 4. Trang bị của player đang rơi trên đất trong zone chưa kịp nhặt
     */
    public boolean isSlotAcquired(Player player, int targetTier, int slot, Zone zone) {
        // 1. Kiểm tra lịch sử rơi của Tier hiện tại
        if (player.smartDropTierHistory != null && targetTier >= 0 && targetTier < player.smartDropTierHistory.length) {
            if ((player.smartDropTierHistory[targetTier] & (1 << slot)) != 0) {
                return true;
            }
        }

        // 2. Quét trang bị trên người (itemsBody)
        if (player.inventory != null && player.inventory.itemsBody != null) {
            int bodySize = player.inventory.itemsBody.size();
            if (slot < bodySize) {
                Item it = player.inventory.itemsBody.get(slot);
                if (it != null && it.isNotNullItem() && it.template != null) {
                    int t = getGearTier(it.template.id);
                    if (t >= targetTier) {
                        return true;
                    }
                    if (it.template.strRequire >= TIER_POWERS[targetTier]) {
                        return true;
                    }
                }
            }
        }

        // 3. Quét trang bị trong hành trang (itemsBag)
        if (player.inventory != null && player.inventory.itemsBag != null) {
            int bagSize = player.inventory.itemsBag.size();
            for (int i = 0; i < bagSize; i++) {
                Item it = player.inventory.itemsBag.get(i);
                if (it != null && it.isNotNullItem() && it.template != null) {
                    int itemSlot = it.template.type;
                    if (itemSlot == slot) {
                        if (slot == 4 || it.template.gender == player.gender || it.template.gender > 2) {
                            int t = getGearTier(it.template.id);
                            if (t >= targetTier) {
                                return true;
                            }
                            if (it.template.strRequire >= TIER_POWERS[targetTier]) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        // 4. Quét trang bị của người chơi đang nằm trên mặt đất trong khu vực (zone.items)
        if (zone != null && zone.items != null && !zone.items.isEmpty()) {
            synchronized (zone.items) {
                int size = zone.items.size();
                for (int i = 0; i < size; i++) {
                    if (i < zone.items.size()) {
                        ItemMap im = zone.items.get(i);
                        if (im != null && im.playerId == player.id && im.itemTemplate != null) {
                            int s = getGearSlot(im.itemTemplate.id);
                            int t = getGearTier(im.itemTemplate.id);
                            if (s == slot && t >= targetTier) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Kiểm tra người chơi đã thu thập đủ 5 slot của targetTier chưa
     */
    public boolean isTierCompleted(Player player, int targetTier, Zone zone) {
        for (int s = 0; s < 5; s++) {
            if (!isSlotAcquired(player, targetTier, s, zone)) {
                return false;
            }
        }
        return true;
    }

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

        if (player.smartDropTierHistory == null) {
            player.smartDropTierHistory = new int[8];
        }

        // 1. Xác định Tier theo sức mạnh người chơi
        int targetTier = getTargetTier(player.nPoint.power);

        // 2. Tier Progression Lock: Đã có đủ trọn bộ 5 món của Tier hiện tại -> DỪNG RƠI ngay lập tức
        // Tránh xả đồ trùng, rác map và đầy túi tân thủ. Chờ người chơi thăng tiến Sức Mạnh lên Tier tiếp theo.
        if (isTierCompleted(player, targetTier, mob.zone)) {
            player.smartDropStreak = 0;
            return null;
        }

        // 3. Kiểm tra xác suất rơi đồ (kèm Pity bảo hiểm và kiểm tra túi trống)
        if (!shouldDropGear(player)) {
            return null;
        }

        // 4. Phân phối Slot thông minh (100% Anti-Duplicate Strict Mode: chỉ chọn trong slot còn thiếu)
        int slot = selectSmartSlot(player, targetTier);
        if (slot < 0) {
            player.smartDropStreak = 0;
            return null;
        }

        // 5. Xác định Hành tinh (Gender) - 100% đúng hành tinh của mình
        int gender = player.gender;
        if (gender < 0 || gender > 2) {
            gender = 0;
        }

        int tempId = GEAR_TEMPLATES[targetTier][slot][gender];

        // 6. Ghi nhận slot vừa rơi vào lịch sử Tier để ngăn chặn rơi trùng tuyệt đối
        if (targetTier >= 0 && targetTier < player.smartDropTierHistory.length) {
            player.smartDropTierHistory[targetTier] |= (1 << slot);
        }
        player.lastSmartDropSlot = slot;

        // 7. Khởi tạo ItemMap và thuộc tính
        return createSmartDropItemMap(mob.zone, tempId, player, x, yEnd);
    }

    /**
     * Thuật toán chọn Slot rơi đồ thông minh (Zero-GC, 100% Anti-Duplicate):
     * - Chỉ chọn trong các slot chưa từng rơi / chưa sở hữu của targetTier.
     * - Nếu còn nhiều slot thiếu: tránh rơi lại lastSmartDropSlot.
     * - Nếu không còn slot thiếu nào: trả về -1 (đã hoàn thành bộ).
     */
    public int selectSmartSlot(Player player, int targetTier) {
        int[] missing = new int[5];
        int missingCount = 0;
        Zone zone = player.zone;
        for (int s = 0; s < 5; s++) {
            if (!isSlotAcquired(player, targetTier, s, zone)) {
                missing[missingCount++] = s;
            }
        }

        if (missingCount == 0) {
            return -1;
        }
        if (missingCount == 1) {
            return missing[0];
        }

        // Lọc tránh rơi lại lastSmartDropSlot nếu còn slot thiếu khác
        int[] candidates = new int[missingCount];
        int candCount = 0;
        for (int i = 0; i < missingCount; i++) {
            if (missing[i] != player.lastSmartDropSlot) {
                candidates[candCount++] = missing[i];
            }
        }
        if (candCount > 0) {
            return candidates[Util.nextInt(0, candCount - 1)];
        }
        return missing[Util.nextInt(0, missingCount - 1)];
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

        // PHÂN NHÓM: Player trên 9 triệu sức mạnh -> Tỉ lệ RẤT KHÓ RƠI (không áp dụng Pity tân thủ)
        if (player.nPoint != null && player.nPoint.power >= POWER_THRESHOLD_HIGH) {
            // Giới hạn trong ngày: đã nhặt >= 3 món trong ngày -> tỉ lệ cực hiếm 1/10.000 (0.01%)
            if (player.smartDropCountToday >= 3) {
                return Util.isTrue(1, 10000);
            }
            // Hard Pity cho người chơi lớn: 1.000 quái trượt liên tiếp mới bảo hiểm 1 món
            if (player.smartDropStreak >= 1000) {
                return true;
            }
            // Tỉ lệ cơ bản: Rất khó rơi - 1/1.000 (0.1%)
            return Util.isTrue(1, 1000);
        }

        // ================= NHÓM TÂN THỦ DƯỚI 9 TRIỆU SỨC MẠNH =================
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

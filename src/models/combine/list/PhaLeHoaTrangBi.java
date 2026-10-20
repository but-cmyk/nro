package models.combine.list;

import consts.ConstFont;
import consts.ConstNpc;
import models.item.Item;
import models.player.Player;
import server.ServerNotify;
import services.CombineService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class PhaLeHoaTrangBi {

    // --- CẤU HÌNH ---
    private static final int OPTION_SAO_PHA_LE = 107;
    private static final int OPTION_HAN_SU_DUNG = 93;
    
    // Mảng tỉ lệ thành công theo cấp sao (Từ 0 sao -> 8 sao)
    private static final float[] RATIOS = {50, 20, 10, 5, 3, 2, 1, 0.5f, 0.5f};
    
    // Mảng chi phí vàng theo cấp sao
    private static final int[] GOLD_COSTS = {
        5_000_000, 10_000_000, 20_000_000, 
        40_000_000, 60_000_000, 90_000_000, 
        120_000_000, 150_000_000, 180_000_000
    };

    // Mảng chi phí ngọc theo cấp sao (Từ 0 sao -> 8 sao)
    // 0 -> 1, 1 -> 2, 2 -> 3 sao: 0 ngọc
    // 3 -> 4 sao: 5 ngọc
    // 4 -> 5 sao: 10 ngọc
    // 5 -> 6 sao: 20 ngọc
    // 6 -> 7 sao: 40 ngọc
    // >= 7 sao: 40 ngọc
    private static final int[] GEM_COSTS = {
        0, 0, 0, 
        5, 10, 20, 
        40, 40, 40
    };

    // --- HÀNG UTILS LẤY CHỈ SỐ ---
    private static float getRatio(int star) {
        return (star >= 0 && star < RATIOS.length) ? RATIOS[star] : 0;
    }

    private static String getRatioStr(int star) {
        float ratio = getRatio(star);
        return (ratio == (int) ratio) ? String.valueOf((int) ratio) : String.valueOf(ratio);
    }

    private static int getGold(int star) {
        return (star >= 0 && star < GOLD_COSTS.length) ? GOLD_COSTS[star] : 0;
    }

    private static int getGem(int star) {
        return (star >= 0 && star < GEM_COSTS.length) ? GEM_COSTS[star] : 0;
    }

    // --- LOGIC CHÍNH ---

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "Trang bị không phù hợp");
            return;
        }
        Item item = player.combineNew.itemsCombine.get(0);
        if (item == null || !item.isNotNullItem()) return;

        if (item.isHaveOption(OPTION_HAN_SU_DUNG)) {
            Service.gI().sendDialogMessage(player, "Trang bị có hạn sử dụng, không thể pha lê hóa!");
            return;
        }
        if (!item.canPhaLeHoa()) {
            Service.gI().sendDialogMessage(player, "Trang bị không phù hợp");
            return;
        }

        int star = item.getOptionParam(OPTION_SAO_PHA_LE);
        if (star >= CombineService.MAX_STAR_ITEM) {
            Service.gI().sendDialogMessage(player, "Đã đạt số pha lê tối đa");
            return;
        }

        int gold = getGold(star);
        int gem = getGem(star);
        
        String ratioStr = getRatioStr(star);
        String goldStr = Util.powerToString(gold);
        boolean isEnoughGold = player.inventory.gold >= gold;
        boolean isEnoughGem = gem <= 0 || player.inventory.checkGemAndRuby(gem);

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append(item.template.name).append("\n");
        text.append(ConstFont.BOLD_DARK).append(item.getOptionInfo()).append("\n");
        text.append(ConstFont.BOLD_GREEN).append(star + 1).append(" ô Sao Pha Lê\n");
        text.append(ConstFont.BOLD_BLUE).append("Tỉ lệ thành công: ").append(ratioStr).append("%\n");
        text.append(isEnoughGold ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
            .append("Cần ").append(goldStr).append(" vàng");
        if (gem > 0) {
            text.append("\n").append(isEnoughGem ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Cần ").append(gem).append(" ngọc (ưu tiên hồng ngọc)");
        }

        if (!isEnoughGold) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "Còn thiếu\n" + Util.powerToString(gold - player.inventory.gold) + " vàng");
        } else if (!isEnoughGem) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "Còn thiếu\n" + (gem - player.inventory.getGemAndRuby()) + " ngọc");
        } else {
            long maxTimesGold = gold > 0 ? (player.inventory.gold / gold) : 999;
            long maxTimesGem = gem > 0 ? (player.inventory.getGemAndRuby() / gem) : 999;
            long maxTimes = Math.min(maxTimesGold, maxTimesGem);

            java.util.List<String> menuSelects = new java.util.ArrayList<>();
            player.combineNew.quantities.clear();

            menuSelects.add("Nâng cấp");
            player.combineNew.quantities.add(1);

            if (maxTimes >= 5) {
                menuSelects.add("Nâng cấp\n5 lần");
                player.combineNew.quantities.add(5);
            }
            if (maxTimes >= 10) {
                menuSelects.add("Nâng cấp\n10 lần");
                player.combineNew.quantities.add(10);
            }
            menuSelects.add("Từ chối");

            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(),
                    menuSelects.toArray(new String[0]));
        }
    }

    public static void phaLeHoa(Player player, int... args) {
        if (player.combineNew.itemsCombine.isEmpty()) return;
        
        Item item = player.combineNew.itemsCombine.get(0);
        if (item == null || !item.isNotNullItem() || item.isHaveOption(OPTION_HAN_SU_DUNG) || !item.canPhaLeHoa()) {
            return;
        }

        int maxAttempts = (args.length > 0 && args[0] > 0) ? args[0] : 1;
        int star = item.getOptionParam(OPTION_SAO_PHA_LE);
        
        if (star >= CombineService.MAX_STAR_ITEM) return;

        int goldCost = getGold(star);
        int gemCost = getGem(star);
        
        // Kiểm tra nhanh cho lần đầu tiên
        if (player.inventory.gold < goldCost || !player.inventory.checkGemAndRuby(gemCost)) {
            Service.gI().sendServerMessage(player, "Bạn không đủ vàng hoặc ngọc để thực hiện.");
            return;
        }

        int attemptsDone = 0;
        boolean isSuccess = false;

        for (int i = 0; i < maxAttempts; i++) {
            long neededGold = (long) goldCost * (attemptsDone + 1);
            long neededGem = (long) gemCost * (attemptsDone + 1);

            // Kiểm tra tài nguyên trước mỗi lần đập
            if (player.inventory.gold < neededGold || !player.inventory.checkGemAndRuby(neededGem)) {
                if (i > 0) {
                    Service.gI().sendServerMessage(player, "Dừng lại tại lần thứ " + i + " do không đủ tài nguyên.");
                }
                break;
            }

            attemptsDone++;

            // Check tỉ lệ
            if (Util.isTrue(getRatio(star), 100)) {
                isSuccess = true;
                break; // Thành công thì dừng luôn (logic game thường là vậy)
            }
        }

        // Trừ tiền một lần dựa trên attemptsDone
        if (attemptsDone > 0) {
            player.inventory.subGold((long) goldCost * attemptsDone);
            player.inventory.subGemAndRuby((long) gemCost * attemptsDone);
        }

        // Xử lý kết quả sau vòng lặp
        if (isSuccess) {
            item.addOptionParam(OPTION_SAO_PHA_LE, 1);
            CombineService.gI().sendEffectSuccessCombine(player);
            
            if (star > 4 && !player.getSession().isAdmin) {
                ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa thành công " 
                        + item.template.name + " lên " + (star + 1) + " sao pha lê");
            }
            
            if (maxAttempts > 1) {
                 Service.gI().sendServerMessage(player, "Thành công sau " + attemptsDone + " lần thử.");
            }
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            if (maxAttempts > 1) {
                Service.gI().sendServerMessage(player, "Thất bại sau " + attemptsDone + " lần thử.");
            }
        }

        // Cập nhật inventory và mở lại giao diện
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
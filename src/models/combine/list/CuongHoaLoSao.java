package models.combine.list;

import consts.ConstNpc;
import models.item.Item;
import models.item.Item.ItemOption;
import models.npc.Npc;
import models.player.Player;
import services.CombineService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class CuongHoaLoSao {

    public static void showInfoCombine(Player player, Npc baHatMit) {
        if (player.combineNew.itemsCombine.size() == 3) {
            if (player.combineNew.itemsCombine.stream().filter(
                    item -> item.isNotNullItem() && (item.template.type <= 5 || item.template.type == 32))
                    .count() < 1) {
                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu Trang Bị Cường Hóa", "Đóng");
                return;
            }
            if (player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 1019)
                    .count() < 1) {
                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu Đá Hematite", "Đóng");
                return;
            }
            if (player.combineNew.itemsCombine.stream()
                    .filter(item -> item.isNotNullItem() && item.template.id == 1020)
                    .count() < 1) {
                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu Dùi Đục", "Đóng");
                return;
            }
            String npcSay = "Ngươi Muốn Cường Hóa Vật Phẩm Này\n Tỉ Lệ Thành Công : 100% Chứ";
            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Cường Hóa", "Từ chối");
        } else {
            if (player.combineNew.itemsCombine.size() > 3) {
                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Nguyên liệu không phù hợp", "Đóng");
                return;
            }
            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Còn thiếu nguyên liệu để cường hóa hãy quay lại sau", "Đóng");
        }
    }

    public static void cuongHoa(Player player) {
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && (item.template.type <= 5 || item.template.type == 32))
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Trang Bị");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1019)
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Đá Hematite");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1020)
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Dùi Đục");
            return;
        }
        Item trangBi = null;
        Item hematite = null;
        Item duiduc = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.type <= 5 || item.template.type == 32) {
                trangBi = item;
            } else if (item.template.id == 1019) {
                hematite = item;
            } else if (item.template.id == 1020) {
                duiduc = item;
            }
        }
        int checkOption = 0;
        int star = 0;
        if (trangBi != null) {
            for (Item.ItemOption io : trangBi.itemOptions) {
                if (io.optionTemplate.id == 107) {
                    star = io.param;
                    checkOption++;
                } else if (io.optionTemplate.id == 230) {
                    checkOption = 0;
                }
            }
        }
        if (star <= 7) {
            Service.gI().sendThongBao(player, "Trang Bị Của Ngươi Chưa Đạt 8 Ô Sao Đen");
            return;
        }
        if (checkOption == 0) {
            Service.gI().sendThongBao(player, "Trang Bị Của Ngươi Đã Được Cường Hóa");
            return;
        }
        if (trangBi == null || duiduc == null || hematite == null) {
            Service.gI().sendThongBao(player, "Không đủ nguyên liệu nâng cấp!");
            CombineService.gI().reOpenItemCombine(player);
        } else if (Util.isTrue(35, 100)) {
            InventoryService.gI().subQuantityItemsBag(player, duiduc, 1);
            InventoryService.gI().subQuantityItemsBag(player, hematite, 1);
            trangBi.itemOptions.add(new ItemOption(230, 1));
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        } else {
            CombineService.gI().sendEffectSuccessCombine(player);
            CombineService.gI().sendEffectFailCombine(player);
        }
    }
}

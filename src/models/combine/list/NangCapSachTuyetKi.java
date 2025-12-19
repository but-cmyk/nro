package models.combine.list;
import consts.ConstNpc;
import models.item.Item;
import models.item.Item.ItemOption;
import models.player.Player;
import services.CombineService;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class NangCapSachTuyetKi {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item sachTuyetKy = null;
            Item kimBamGiay = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineService.gI().issachTuyetKy(item) && (item.template.id == 1181 || item.template.id == 1182 || item.template.id == 1183)) {
                    sachTuyetKy = item;
                } else if (item.template.id == 1153) {
                    kimBamGiay = item;
                }
            }
            if (sachTuyetKy != null && kimBamGiay != null) {
                String npcSay = "|2|Nâng cấp sách tuyệt kỹ\n";
                npcSay += "Cần 10 Kìm bấm giấy\n"
                        + "Tỉ lệ thành công: 10%\n"
                        + "Nâng cấp thất bại sẽ mất 10 Kìm bấm giấy";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Nâng cấp", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ 1 và 10 Kìm bấm giấy.");
                return;
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ 1 và 10 Kìm bấm giấy.");
            return;
        }
    }

    public static void NangCapSachTuyetki(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item sachTuyetKy = null;
            Item kimBamGiay = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineService.gI().issachTuyetKy(item)) {
                    sachTuyetKy = item;
                } else if (item.template.id == 1153) {
                    kimBamGiay = item;
                }
            }
            Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) ((short) sachTuyetKy.template.id + 1));
            if (sachTuyetKy != null && kimBamGiay != null) {
                if (kimBamGiay.quantity < 10) {
                    Service.gI().sendThongBao(player, "Không đủ Kìm bấm giấy");
                    return;
                }
                if (CombineService.gI().checkHaveOption(sachTuyetKy, 0, 224)) {
                    Service.gI().sendThongBao(player, "Chưa giám định");
                    return;
                }
                for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
                    sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id, sachTuyetKy.itemOptions.get(i).param));
                }
                CombineService.gI().sendEffectSuccessCombine(player);
                InventoryService.gI().addItemBag(player, sachTuyetKy_2);
                InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                InventoryService.gI().subQuantityItemsBag(player, kimBamGiay, 10);
                InventoryService.gI().sendItemBags(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }
}

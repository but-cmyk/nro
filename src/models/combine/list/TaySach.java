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

public class TaySach {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineService.gI().issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                String npcSay = "|3|Tẩy Sách Tuyệt Kỹ\n";
                npcSay += "|5|Tẩy sách loại bỏ các option HP,KI,SD";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Đồng ý", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ để tẩy");
                return;
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ để tẩy");
            return;
        }
    }

    public static void Taysach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineService.gI().issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int luotTay = 0;
                ItemOption optionLevel = null;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 225) {
                        luotTay = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (luotTay == 0) {
                    Service.gI().sendThongBao(player, "Yêu cầu lượt tẩy lớn hơn 0");
                    return;
                }
                Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) sachTuyetKy.template.id);
                if (CombineService.gI().checkHaveOption(sachTuyetKy, 0, 224)) {
                    Service.gI().sendThongBao(player, "Chưa được giám định!!");
                    return;
                }
                for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                    if (sachTuyetKy.itemOptions.get(i).optionTemplate.id == 232) {
                        sachTuyetKy.itemOptions.get(i).param -= 1;
                    }
                }
                sachTuyetKy_2.itemOptions.add(new ItemOption(231, 0));
                for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                    sachTuyetKy_2.itemOptions.add(new ItemOption(sachTuyetKy.itemOptions.get(i).optionTemplate.id, sachTuyetKy.itemOptions.get(i).param));
                }
                CombineService.gI().sendEffectSuccessCombine(player);
                InventoryService.gI().addItemBag(player, sachTuyetKy_2);
                InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                InventoryService.gI().sendItemBags(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }
}

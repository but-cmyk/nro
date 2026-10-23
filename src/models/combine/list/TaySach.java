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
                int luotTay = 0;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 225) {
                        luotTay = io.param;
                        break;
                    }
                }
                String npcSay = "|2|Tẩy Sách Tuyệt Kỹ\n";
                npcSay += "|1|Số lượt tẩy còn lại: " + luotTay + "\n";
                npcSay += "|5|Tẩy sách để làm mới Option Tuyệt Kỹ";
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
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 225) {
                        luotTay = io.param;
                        break;
                    }
                }
                if (luotTay <= 0) {
                    Service.gI().sendThongBao(player, "Sách đã hết lượt tẩy, hãy đem đi phân rã!");
                    return;
                }
                if (CombineService.gI().checkHaveOption(sachTuyetKy, 0, 224)) {
                    Service.gI().sendThongBao(player, "Chưa được giám định!!");
                    return;
                }

                Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) sachTuyetKy.template.id);
                int skillOptId = 231;
                int skillParam = 20;
                if (sachTuyetKy.template.id == 1181 || sachTuyetKy.template.id == 1182) {
                    skillOptId = 50; // Trái Đất: Tăng sức đánh
                    skillParam = (sachTuyetKy.template.id == 1182) ? 30 : 20;
                } else if (sachTuyetKy.template.id == 1183 || sachTuyetKy.template.id == 1184) {
                    skillOptId = 232; // Namếc: Makankosappo
                    skillParam = (sachTuyetKy.template.id == 1184) ? 40 : 30;
                } else {
                    skillOptId = 231; // Xayda: Tự sát
                    skillParam = (sachTuyetKy.template.id == 1186) ? 30 : 20;
                }

                sachTuyetKy_2.itemOptions.add(new ItemOption(skillOptId, skillParam));
                for (int i = 1; i < sachTuyetKy.itemOptions.size(); i++) {
                    ItemOption io = sachTuyetKy.itemOptions.get(i);
                    int param = io.param;
                    if (io.optionTemplate.id == 225) {
                        param = Math.max(0, param - 1); // Trừ 1 lượt tẩy
                    }
                    sachTuyetKy_2.itemOptions.add(new ItemOption(io.optionTemplate.id, param));
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

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

public class PhucHoiSach {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineService.gI().issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                String npcSay = "|2|Phục hồi " + sachTuyetKy.getName() + "\n"
                        + "Cần 10 cuốn sách cũ\n"
                        + "Phí phục hồi 10 triệu vàng";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Đồng ý", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
                return;
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Không tìm thấy vật phẩm");
            return;
        }
    }

    public static void PhucHoisach(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item cuonSachCu = InventoryService.gI().findItemBagByTemp(player, (short) 1187);
            int goldPhucHoi = 10_000_000;
            Item sachTuyetKy = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineService.gI().issachTuyetKy(item)) {
                    sachTuyetKy = item;
                }
            }
            if (sachTuyetKy != null) {
                int doBen = 0;
                for (ItemOption io : sachTuyetKy.itemOptions) {
                    if (io.optionTemplate.id == 226) {
                        doBen = io.param;
                        break;
                    }
                }
                if (cuonSachCu == null || cuonSachCu.quantity < 10) {
                    Service.gI().sendThongBaoOK(player, "Cần sách tuyệt kỹ và 10 cuốn sách cũ");
                    return;
                }
                if (player.inventory.gold < goldPhucHoi) {
                    Service.gI().sendThongBao(player, "Không đủ 10 triệu vàng");
                    return;
                }
                if (doBen < 1000) {
                    for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
                        if (sachTuyetKy.itemOptions.get(i).optionTemplate.id == 226) {
                            sachTuyetKy.itemOptions.get(i).param = 1000;
                            break;
                        }
                    }
                    player.inventory.gold -= goldPhucHoi;
                    InventoryService.gI().subQuantityItemsBag(player, cuonSachCu, 10);
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Phục hồi độ bền sách thành công (1000/1000)!");
                    CombineService.gI().sendEffectSuccessCombine(player);
                    CombineService.gI().reOpenItemCombine(player);
                } else {
                    Service.gI().sendThongBao(player, "Độ bền sách vẫn còn tối đa, không cần phục hồi");
                    return;
                }
            }
        }
    }
}

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
                if (CombineService.gI().issachTuyetKy(item) && (item.template.id == 1181 || item.template.id == 1183 || item.template.id == 1185)) {
                    sachTuyetKy = item;
                } else if (item.template.id == 1153) {
                    kimBamGiay = item;
                }
            }
            if (sachTuyetKy != null && kimBamGiay != null) {
                String npcSay = "|2|Nâng cấp " + sachTuyetKy.getName() + "\n";
                npcSay += "Cần 10 Kìm bấm giấy\n"
                        + "Tỉ lệ thành công: 10%\n"
                        + "Nâng cấp thất bại sẽ mất 10 Kìm bấm giấy";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Nâng cấp", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ Cấp 1 và 10 Kìm bấm giấy.");
                return;
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần Sách Tuyệt Kỹ Cấp 1 và 10 Kìm bấm giấy.");
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
            if (sachTuyetKy != null && kimBamGiay != null) {
                if (sachTuyetKy.template.id != 1181 && sachTuyetKy.template.id != 1183 && sachTuyetKy.template.id != 1185) {
                    Service.gI().sendThongBao(player, "Chỉ có thể nâng cấp Sách Tuyệt Kỹ Cấp 1!");
                    return;
                }
                if (kimBamGiay.quantity < 10) {
                    Service.gI().sendThongBao(player, "Không đủ Kìm bấm giấy (cần 10 cái)");
                    return;
                }
                if (CombineService.gI().checkHaveOption(sachTuyetKy, 0, 224)) {
                    Service.gI().sendThongBao(player, "Sách chưa được giám định!");
                    return;
                }

                if (Util.isTrue(10, 100)) {
                    // Thành công: Nâng cấp lên Cấp 2
                    Item sachTuyetKy_2 = ItemService.gI().createNewItem((short) (sachTuyetKy.template.id + 1));
                    for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
                        ItemOption io = sachTuyetKy.itemOptions.get(i);
                        int param = io.param;
                        if (io.optionTemplate.id == 87) {
                            param = 2; // Cập nhật lên Cấp 2
                        }
                        sachTuyetKy_2.itemOptions.add(new ItemOption(io.optionTemplate.id, param));
                    }
                    CombineService.gI().sendEffectSuccessCombine(player);
                    InventoryService.gI().addItemBag(player, sachTuyetKy_2);
                    InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);
                    InventoryService.gI().subQuantityItemsBag(player, kimBamGiay, 10);
                    Service.gI().sendThongBao(player, "Nâng cấp Sách Tuyệt Kỹ Cấp 2 thành công!");
                } else {
                    // Thất bại: Trừ 10 kìm bấm giấy
                    CombineService.gI().sendEffectFailCombine(player);
                    InventoryService.gI().subQuantityItemsBag(player, kimBamGiay, 10);
                    Service.gI().sendThongBao(player, "Nâng cấp thất bại, bạn bị mất 10 Kìm bấm giấy!");
                }
                InventoryService.gI().sendItemBags(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }
}

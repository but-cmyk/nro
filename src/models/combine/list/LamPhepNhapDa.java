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
import java.util.ArrayList;

public class LamPhepNhapDa {

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 2) {
                Item first = player.combineNew.itemsCombine.get(0);
                Item second = player.combineNew.itemsCombine.get(1);

                Item item = null; // mảnh đá vụn (ID 225)
                Item bnp = null;  // bình nước phép (ID 226)

                // Gán đúng item theo id
                if (first != null && first.template != null) {
                    if (first.template.id == 225) item = first;
                    if (first.template.id == 226) bnp = first;
                }
                if (second != null && second.template != null) {
                    if (second.template.id == 225) item = second;
                    if (second.template.id == 226) bnp = second;
                }

                if (item != null && bnp != null) {
                    if (item.quantity >= 99 && bnp.quantity >= 1) {
                        String npcSay = "|2|Con có muốn biến x99 " + item.template.name + " thành\n"
                                + "10 đá nâng cấp ngẫu nhiên\n\n"
                                + "|7|Cần x99 " + item.template.name + " và 1 bình nước phép";

                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
                    } else {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Không đủ nguyên liệu rồi con!", "Đóng");
                    }
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh đá vụn hoặc bình nước phép", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 2 vật phẩm: mảnh đá vụn và bình nước phép", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public static void LamPhepNhapda(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() >= 2) {

                Item first = player.combineNew.itemsCombine.get(0);
                Item second = player.combineNew.itemsCombine.get(1);

                Item item = null; // mảnh đá vụn
                Item bnp = null;  // bình nước phép

                if (first != null && first.template != null) {
                    if (first.template.id == 225) item = first;
                    if (first.template.id == 226) bnp = first;
                }
                if (second != null && second.template != null) {
                    if (second.template.id == 225) item = second;
                    if (second.template.id == 226) bnp = second;
                }

                if (item != null && bnp != null) {
                    if (item.quantity >= 99 && bnp.quantity >= 1) {

                        int dnc = Util.nextInt(220, 224);
                        Item nr = ItemService.gI().createNewItem((short) dnc, 10);

                        if (nr.itemOptions == null)
                            nr.itemOptions = new ArrayList<>();

                        int optionId = 291 - dnc;
                        nr.itemOptions.add(new ItemOption(optionId, 0));

                        InventoryService.gI().addItemBag(player, nr);
                        InventoryService.gI().subQuantityItemsBag(player, item, 99);
                        InventoryService.gI().subQuantityItemsBag(player, bnp, 1);

                        InventoryService.gI().sendItemBags(player);
                        CombineService.gI().reOpenItemCombine(player);
                        CombineService.gI().sendEffectCombineDV(player, item.template.iconID);

                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " +
                                        ItemService.gI().getTemplate(dnc).name);
                    } else {
                        Service.gI().sendThongBao(player, "Không đủ 99 mảnh đá vụn và 1 bình nước phép :3");
                    }
                } else {
                    Service.gI().sendThongBao(player, "Thiếu mảnh đá vụn hoặc bình nước phép");
                }
            } else {
                Service.gI().sendThongBao(player, "Cần đúng 2 vật phẩm để làm phép");
            }
        } else {
            Service.gI().sendThongBao(player, "Cần để trống ít nhất 1 ô hành trang");
        }
    }
}

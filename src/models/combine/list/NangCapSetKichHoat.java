package models.combine.list;
import consts.ConstNpc;
import models.item.Item;
import models.player.Player;
import services.CombineService;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class NangCapSetKichHoat {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 1 món huỷ diệt và 1 món thần linh", "Đóng");
            return;
        }
        if (player.combineNew.itemsCombine.size() == 2) {
            long dhdCount = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).count();
            long thanLinhCount = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).count();

            // Kiểm tra nếu không có 1 món huỷ diệt hoặc không có 1 món Than Linh
            if (dhdCount != 1 || thanLinhCount != 1) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ huỷ diệt hoặc Thần Linh", "Đóng");
                return;
            }

            String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|"
                    + "Và nhận được " + player.combineNew.itemsCombine.stream().filter(Item::isDHD).findFirst().get().typeName() + " kích hoạt tương ứng\n"
                    + "|1|Cần " + Util.powerToString(200_000_000) + " vàng";

            if (player.inventory.gold < 200_000_000) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn không đủ vàng", "Đóng");
                return;
            }
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                    npcSay, "Nâng cấp\n" + Util.powerToString(200_000_000) + " vàng", "Từ chối");
        } else {
            if (player.combineNew.itemsCombine.size() > 2) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Nguyên liệu không phù hợp", "Đóng");
                return;
            }
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
        }
    }

    public static void nangCapSetKichHoat(Player player, int... numm) {
        // Món đầu làm gốc
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        Item item1 = player.combineNew.itemsCombine.get(0);
        Item item2 = player.combineNew.itemsCombine.get(1);
        
        if (item1.isDHD() && item2.isDTL()) {
            if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                if (player.inventory.gold < 200_000_000) {
                    Service.gI().sendThongBao(player, "Con cần 200Tr vàng để chuyển hóa");
                    return;
                }

                player.inventory.gold -= 200_000_000;
                if (Util.isTrue(100, 100)) {
                    int type = item1.template.type;
                    int[][] items = {{0, 6, 21, 27, 12}, {1, 7, 22, 28, 12}, {2, 8, 23, 29, 12}};
                    int[][] options = {{128, 129, 127}, {130, 131, 132}, {133, 135, 134}};
                    int skhv1 = 25; // tỉ lệ
                    int skhv2 = 35; // tỉ lệ
                    int skhc = 40; // tỉ lệ
                    int skhId = -1;
                    int rd = Util.nextInt(1, 100);
                    if (rd <= skhv1) {
                        skhId = 0;
                    } else if (rd <= skhv1 + skhv2) {
                        skhId = 1;
                    } else if (rd <= skhv1 + skhv2 + skhc) {
                        skhId = 2;
                    }

                    Item item = null;
                    switch (player.gender) {
                        case 0:
                            item = ItemService.gI().itemSKH(items[0][item1.template.type], options[0][skhId]);
                            break;
                        case 1:
                            item = ItemService.gI().itemSKH(items[1][item1.template.type], options[1][skhId]);
                            break;
                        case 2:
                            item = ItemService.gI().itemSKH(items[2][item1.template.type], options[2][skhId]);
                            break;
                    }

                    if (item != null && InventoryService.gI().getCountEmptyBag(player) > 0) {
                        CombineService.gI().sendEffectSuccessCombine(player);
                        InventoryService.gI().addItemBag(player, item);
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
                        InventoryService.gI().subQuantityItemsBag(player, item1, 1);
                        InventoryService.gI().subQuantityItemsBag(player, item2, 1);
                        InventoryService.gI().sendItemBags(player);
                    } else {
                        Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
                    }
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                    InventoryService.gI().subQuantityItemsBag(player, item1, 1);
                    InventoryService.gI().subQuantityItemsBag(player, item2, 1);
                    InventoryService.gI().sendItemBags(player);
                }
            }
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            player.combineNew.itemsCombine.clear();
            CombineService.gI().reOpenItemCombine(player);
        } else {
            // Thông báo nếu thiếu món huỷ diệt hoặc món Tinh Linh
            if (!item1.isDHD()) {
                Service.gI().sendThongBao(player, "Cần 1 món huỷ diệt");
            } else if (!item2.isDTL()) {
                Service.gI().sendThongBao(player, "Cần 1 món Tinh Linh");
            }
        }
    }
}

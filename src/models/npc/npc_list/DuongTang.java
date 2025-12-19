//package models.npc.npc_list;
//
///**
// *
// * @author NGOJC
// */
//import consts.ConstItem;
//import consts.ConstNpc;
//import models.item.Item;
//import models.npc.Npc;
//import models.player.Player;
//import services.ItemService;
//import services.Service;
//import services.map.ChangeMapService;
//import services.player.InventoryService;
//import utils.Util;
//
//public class DuongTang extends Npc {
//
//    public DuongTang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
//        super(mapId, status, cx, cy, tempId, avartar);
//    }
//
//    @Override
//    public void openBaseMenu(Player player) {
//        if (canOpenNpc(player)) {
//            switch (mapId) {
//                case 0 -> {
//                    createOtherMenu(player, ConstNpc.BASE_MENU,
//                            "A mi phò phò, thí chủ hãy giúp giải cứu đồ đệ của bần tăng đang bị phong ấn tại ngũ hành sơn",
//                            "Đồng ý", "Từ chối");
//                }
//                case 123 -> {
//                    createOtherMenu(player, ConstNpc.BASE_MENU, "Ra khỏi ngôi làng này sẽ gặp ngọn núi ngũ hành sơn",
//                            "Về\nLàng Aru", "Đóng");
//                }
//                case 122 -> {
//                    createOtherMenu(player, ConstNpc.BASE_MENU, "|0|A mi phò phò, thí chủ hãy thu thập bùa 'giải khai phong ấn'\nmỗi chữ 10 cái\n",
//                            "Gải\nphong ấn", "Về\nLàng Aru");
//                }
//                default ->
//                    super.openBaseMenu(player);
//            }
//        }
//    }
//
//    @Override
//    public void confirmMenu(Player player, int select) {
//        if (canOpenNpc(player)) {
//            if (player.idMark.isBaseMenu()) {
//                switch (mapId) {
//                    case 0 -> {
//                        if (select == 0) {
//                            ChangeMapService.gI().changeMapNonSpaceship(player, 123, 50, 384);
//                        }
//                    }
//                    case 123 -> {
//                        if (select == 0) {
//                            ChangeMapService.gI().changeMapNonSpaceship(player, 0, Util.nextInt(700, 800), 432);
//                        }
//                    }
//                    case 122 -> {
//                        if (select == 0) {
//                            Item chukhai = InventoryService.gI().findItemBag(player, ConstItem.CHU_KHAI);
//                            Item chugiai = InventoryService.gI().findItemBag(player, ConstItem.CHU_GIAI);
//                            Item chuan = InventoryService.gI().findItemBag(player, ConstItem.CHU_AN);
//                            Item chuphong = InventoryService.gI().findItemBag(player, ConstItem.CHU_PHONG);
//
//                            if (chukhai != null && chukhai.quantity >= 10 || chugiai != null && chugiai.quantity >= 10
//                                    || chuan != null && chuan.quantity >= 10 || chuphong != null && chuphong.quantity >= 10) {
//                                if (chukhai != null) {
//                                    InventoryService.gI().subQuantityItemsBag(player, chukhai, 10);
//                                }
//                                if (chugiai != null) {
//                                    InventoryService.gI().subQuantityItemsBag(player, chugiai, 10);
//                                }
//                                if (chuan != null) {
//                                    InventoryService.gI().subQuantityItemsBag(player, chuan, 10);
//                                }
//                                if (chuphong != null) {
//                                    InventoryService.gI().subQuantityItemsBag(player, chuphong, 10);
//                                }
//                                Item selectedItem = null;
//                                switch (player.gender) {
//                                    case 0:
//                                        selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG);
//                                        break;
//                                    case 1:
//                                        selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG_545);
//                                        break;
//                                    case 2:
//                                        selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG_546);
//                                        break;
//                                    default:
//                                        return;
//                                }
//                                selectedItem.itemOptions.add(new Item.ItemOption(50, Util.nextInt(25, 30)));
//                                selectedItem.itemOptions.add(new Item.ItemOption(94, Util.nextInt(10, 20)));
//                                selectedItem.itemOptions.add(new Item.ItemOption(77, Util.nextInt(25, 30)));
//                                selectedItem.itemOptions.add(new Item.ItemOption(103, Util.nextInt(25, 30)));
//                                selectedItem.itemOptions.add(new Item.ItemOption(101, Util.nextInt(30, 60)));
//                                selectedItem.itemOptions.add(new Item.ItemOption(114, Util.nextInt(50, 100)));
//                                selectedItem.itemOptions.add(new Item.ItemOption(106, 0));
//                                selectedItem.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
//                                selectedItem.itemOptions.add(new Item.ItemOption(174, 2025));
//                                InventoryService.gI().addItemBag(player, selectedItem);
//                                InventoryService.gI().sendItemBags(player);
//                                this.npcChat(player.zone,
//                                        "A mi phò phò, đa tạ thí chủ tương trợ, xin hãy nhận món quà mọn này, bần tăng sẽ niệm chú giải thoát cho Ngộ Không");
//                                Service.gI().sendThongBao(player, "Bạn nhận được: " + selectedItem.template.name);
//                            } else {
//                                Service.gI().sendThongBao(player, "Cần đủ 4 loại chữ!");
//                            }
//                            break;
//                        }
//                        if (select == 1) {
//                            ChangeMapService.gI().changeMapNonSpaceship(player, 0, Util.nextInt(700, 800), 432);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//}

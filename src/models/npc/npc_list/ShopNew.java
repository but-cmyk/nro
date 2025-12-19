///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package models.npc.npc_list;
//
//import consts.ConstNpc;
//import models.item.Item;
//import models.npc.Npc;
//import models.player.Player;
//import services.CombineService;
//import services.ItemService;
//import services.Service;
//import services.ShopService;
//import services.player.InventoryService;
//
//public class ShopNew extends Npc {
//
//    public ShopNew(int mapId, int status, int cx, int cy, int tempId, int avartar) {
//        super(mapId, status, cx, cy, tempId, avartar);
//    }
//
//    @Override
//    public void openBaseMenu(Player player) {
//        if (canOpenNpc(player)) {
//            createOtherMenu(player, ConstNpc.BASE_MENU,
//                    "Xin chào, ta có một số vật phẩm đặt biệt cậu có muốn xem không?",
//                    "Cửa hàng", "Bán Đồ\nThần Linh\nLấy Tiền", "Bán Thỏi Vàng Lấy Tiền", "Đóng");
//        }
//    }
//
//    @Override
//    public void confirmMenu(Player player, int select) {
//        if (canOpenNpc(player)) {
//            if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
//                if (player.idMark.isBaseMenu()) {
//                    switch (select) {
//                        case 0 ->
//                            ShopService.gI().opendShop(player, "SU_KIEN", false);
//                        case 1 ->
//                            CombineService.gI().openTabCombine(player, CombineService.PHAN_RA_DO_THAN);
//                        case 2 ->
//                            createOtherMenu(player, 2,
//                                    "Xin chào, ta có một số vật phẩm đặt biệt cậu có muốn xem không?\n"
//                                    + "Quy Đổi Thỏi Vàng Sang Đồng Coin\n"
//                                    + "Với Tỉ Lệ 10 Thỏi Vàng = 1 Đồng Coin 8K VNĐ",
//                                    "10 Thỏi Vàng\n[ 1 Đồng ]", "100 Thỏi Vàng\n[ 10 Đồng ]", "200 Thỏi Vàng\n[ 20 Đồng ]", "500 Thỏi Vàng\n[ 50 Đồng ]", "Đóng");
//                    }
//                } else if (player.idMark.getIndexMenu() == 2) {
//                    switch (select) {
//                        case 0:
//                            Item thoiVang = InventoryService.gI().findItemBag(player, 457);
//                            if (thoiVang != null && thoiVang.quantity >= 10) {
//                                Item dongCoin = ItemService.gI().createNewItemLock(1341, 1);
//                                InventoryService.gI().subQuantityItemsBag(player, thoiVang, 10);
//                                InventoryService.gI().addItemBag(player, dongCoin);
//                                InventoryService.gI().sendItemBags(player);
//                                Service.gI().sendThongBao(player, "Quy đổi thành công nhận được 1 Đồng Coin");
//                            } else {
//                                Service.gI().sendThongBao(player, "Bạn không đủ Thỏi Vàng");
//                            }
//                            break;
//                        case 1:
//                            Item thoiVangg = InventoryService.gI().findItemBag(player, 457);
//                            if (thoiVangg != null && thoiVangg.quantity >= 100) {
//                                Item dongCoinn = ItemService.gI().createNewItemLock(1341, 10);
//                                InventoryService.gI().subQuantityItemsBag(player, thoiVangg, 100);
//                                InventoryService.gI().addItemBag(player, dongCoinn);
//                                InventoryService.gI().sendItemBags(player);
//                                Service.gI().sendThongBao(player, "Quy đổi thành công nhận được 10 Đồng Coin");
//                            } else {
//                                Service.gI().sendThongBao(player, "Bạn không đủ Thỏi Vàng");
//                            }
//                            break;
//                        case 2:
//                            Item thoiVanggg = InventoryService.gI().findItemBag(player, 457);
//                            if (thoiVanggg != null && thoiVanggg.quantity >= 200) {
//                                Item dongCoinnn = ItemService.gI().createNewItemLock(1341, 20);
//                                InventoryService.gI().subQuantityItemsBag(player, thoiVanggg, 200);
//                                InventoryService.gI().addItemBag(player, dongCoinnn);
//                                InventoryService.gI().sendItemBags(player);
//                                Service.gI().sendThongBao(player, "Quy đổi thành công nhận được 20 Đồng Coin");
//                            } else {
//                                Service.gI().sendThongBao(player, "Bạn không đủ Thỏi Vàng");
//                            }
//                            break;
//                        case 3:
//                            Item thoiVangggg = InventoryService.gI().findItemBag(player, 457);
//                            if (thoiVangggg != null && thoiVangggg.quantity >= 500) {
//                                Item dongCoinnn = ItemService.gI().createNewItemLock(1341, 50);
//                                InventoryService.gI().subQuantityItemsBag(player, thoiVangggg, 500);
//                                InventoryService.gI().addItemBag(player, dongCoinnn);
//                                InventoryService.gI().sendItemBags(player);
//                                Service.gI().sendThongBao(player, "Quy đổi thành công nhận được 50 Đồng Coin");
//                            } else {
//                                Service.gI().sendThongBao(player, "Bạn không đủ Thỏi Vàng");
//                            }
//                            break;
//                    }
//                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_PHAN_RA_DO_THAN_LINH) {
//                    switch (player.combineNew.typeCombine) {
//                        case CombineService.PHAN_RA_DO_THAN -> {
//                            switch (select) {
//                                case 0 ->
//                                    CombineService.gI().startCombine(player);
//                                default -> {
//                                }
//                            }
//                        }
//
//                    }
//                }
//            }
//        }
//    }
//}

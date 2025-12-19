///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package models.combine.list;
//
//import consts.ConstNpc;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import models.item.Item;
//import models.player.Player;
//import services.CombineService;
//import services.ItemService;
//import services.Service;
//import services.player.InventoryService;
//import utils.Util;
//
///**
// *
// * @author Administrator
// */
//public class PhanRaDoThan {
//
//    public static void showInfoCombine(Player player) {
//        if (player.combineNew.itemsCombine.size() == 0) {
//            CombineService.gI().shopNew.createOtherMenu(player, ConstNpc.IGNORE_MENU,
//                    "Con hãy đưa ta đồ thần linh để phân rã", "Đóng");
//            return;
//        }
//
//        if (player.combineNew.itemsCombine.size() == 1) {
//            Item item = player.combineNew.itemsCombine.get(0);
//
//            if (item.isNotNullItem() && item.template.id >= 555 && item.template.id <= 567) {
//                boolean isCoin10k = item.template.id == 561
//                        || item.template.id == 562
//                        || item.template.id == 564
//                        || item.template.id == 566;
//
//                String npcSay = "|2|Sau khi bán đồ thần linh này\n|7|"
//                        + "Bạn sẽ nhận được: 1 Coin "
//                        + (isCoin10k ? "10K" : "8K") + " VNĐ\n"
//                        + (player.inventory.gold < 50000000 ? "|7|" : "|1|")
//                        + "Cần " + Util.numberToMoney(50000000) + " vàng";
//
//                if (player.inventory.gold < 50000000) {
//                    CombineService.gI().shopNew.npcChat(player, "Hết vàng rồi\nẢo ít thôi con");
//                    return;
//                }
//
//                CombineService.gI().shopNew.createOtherMenu(player, ConstNpc.MENU_PHAN_RA_DO_THAN_LINH,
//                        npcSay, "Bán đồ lấy coin\n" + Util.numberToMoney(50000000) + " vàng", "Từ chối");
//            } else {
//                CombineService.gI().shopNew.createOtherMenu(player, ConstNpc.IGNORE_MENU,
//                        "Ta chỉ có thể phân rã đồ thần linh thôi", "Đóng");
//            }
//        } else {
//            CombineService.gI().shopNew.createOtherMenu(player, ConstNpc.IGNORE_MENU,
//                    "Ta chỉ có thể giúp con bán 1 lần 1 món đồ thần linh", "Đóng");
//        }
//    }
//
//    public static void PhanRaDoThan(Player player) {
//        if (player.combineNew.itemsCombine.size() == 1) {
//            player.inventory.gold -= 50000000;
//            Item item = player.combineNew.itemsCombine.get(0);
//            if (item.template.id == 561 || item.template.id == 562 || item.template.id == 564 || item.template.id == 566) {
//                Item coin10k = ItemService.gI().createNewItem((short) 1340);
//                coin10k.itemOptions.add(new Item.ItemOption(30, 0));
//                InventoryService.gI().addItemBag(player, coin10k);
//                InventoryService.gI().sendItemBags(player);
//                Service.gI().sendThongBaoOK(player, "Bạn nhận được Coin 10.000 VNĐ");
//            } else {
//                Item coin8k = ItemService.gI().createNewItem((short) 1341);
//                coin8k.itemOptions.add(new Item.ItemOption(30, 0));
//                InventoryService.gI().addItemBag(player, coin8k);
//                InventoryService.gI().sendItemBags(player);
//                Service.gI().sendThongBaoOK(player, "Bạn nhận được Coin 8.000 VNĐ");
//            }
//            InventoryService.gI().subQuantityItemsBag(player, item, 1);
//            InventoryService.gI().sendItemBags(player);
//            Service.gI().sendMoney(player);
//            CombineService.gI().reOpenItemCombine(player);
//        }
//    }
//}

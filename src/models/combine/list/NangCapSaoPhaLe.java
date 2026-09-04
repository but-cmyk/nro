package models.combine.list;

import models.item.Item;
import models.item.Item.ItemOption;
import models.player.Player;
import services.CombineService;
import services.ItemService;
import services.RewardService;
import services.Service;
import services.player.InventoryService;

public class NangCapSaoPhaLe {

    public static void nangSaoC2(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item spl = null;
        Item daHematit = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id == 1019) {
                daHematit = item;
            }
            if (item.template.id >= 441 && item.template.id <= 447) {
                spl = item;
            }
        }
        if (spl == null || daHematit == null || spl.quantity < 2 || daHematit.quantity < 1) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.ruby < 2 || player.inventory.gold < 200_000_000) {
            Service.gI().sendThongBao(player, "Không đủ hồng ngọc hoặc vàng!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }
        int newItemId = -1;
        switch (spl.template.id) {
            case 441 -> newItemId = 1352;  // Đỏ hp
            case 442 -> newItemId = 1353;  // Lam ki
            case 443 -> newItemId = 1354;  // Hồng Phản
            case 444 -> newItemId = 1355;  // Tím Xuyên giáp
            case 445 -> newItemId = 1356;  // Cam Xuyên giáp cận chiến
            case 446 -> newItemId = 1357;  // Vàng Rớt Vàng
            case 447 -> newItemId = 1358;  // Lục tnsm
        }
        if (newItemId != -1) {
            Item newItem = ItemService.gI().createNewItem((short) newItemId);
            RewardService.gI().initBaseOptionSaoPhaLe(newItem);
            newItem.itemOptions.add(new ItemOption(72, 1));
            InventoryService.gI().addItemBag(player, newItem);
            Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
            InventoryService.gI().subQuantityItemsBag(player, spl, 2);
            InventoryService.gI().subQuantityItemsBag(player, daHematit, 1);
            player.inventory.subRuby(2);
            player.inventory.subGold(200_000_000);
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
        }
    }

    public static void saoLapLanh(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item spl = null;
        Item daMai = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id == 1018) {
                daMai = item;
            }
            if (item.template.id >= 1350 && item.template.id <= 1358) {
                spl = item;
            }
        }
        if (spl == null || daMai == null || spl.quantity < 5 || daMai.quantity < 1) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.ruby < 2 || player.inventory.gold < 200_000_000) {
            Service.gI().sendThongBao(player, "Không đủ hồng ngọc hoặc vàng!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }
        int newItemId = -1;
        switch (spl.template.id) {
            case 1350 -> newItemId = 1366; // Đen
            case 1351 -> newItemId = 1367; // Trắng
            case 1352 -> newItemId = 1359; // Đỏ
            case 1353 -> newItemId = 1360; // Lam
            case 1354 -> newItemId = 1361; // Hồng
            case 1355 -> newItemId = 1362; // Tím
            case 1356 -> newItemId = 1363; // Cam
            case 1357 -> newItemId = 1364; // Vàng
            case 1358 -> newItemId = 1365; // Lục
        }
        if (newItemId != -1) {
            Item newItem = ItemService.gI().createNewItem((short) newItemId);
            RewardService.gI().initBaseOptionSaoPhaLe(newItem);
            newItem.itemOptions.add(new ItemOption(72, 1));
            InventoryService.gI().addItemBag(player, newItem);
            Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
            InventoryService.gI().subQuantityItemsBag(player, spl, 5);
            InventoryService.gI().subQuantityItemsBag(player, daMai, 1);
            player.inventory.subRuby(2);
            player.inventory.subGold(200_000_000);
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
        }
    }

    public static void taoDaHematite(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item spl = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id >= 1350 && item.template.id <= 1358) {
                spl = item;
            }
        }
        if (spl == null || spl.quantity < 5) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.ruby < 2 || player.inventory.gold < 200_000_000) {
            Service.gI().sendThongBao(player, "Không đủ hồng ngọc hoặc vàng!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }
        Item newItem = ItemService.gI().createNewItem((short) 1019);
        InventoryService.gI().addItemBag(player, newItem);
        Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
        InventoryService.gI().subQuantityItemsBag(player, spl, 5);
        player.inventory.subRuby(2);
        player.inventory.subGold(200_000_000);
        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}

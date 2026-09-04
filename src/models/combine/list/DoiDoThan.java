package models.combine.list;

import java.util.List;
import java.util.stream.Collectors;
import models.item.Item;
import models.item.Item.ItemOption;
import models.player.Player;
import server.Manager;
import services.CombineService;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class DoiDoThan {

    public static void doiVeNdung(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item dtl = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.isDTL()) {
                dtl = item;
            }
        }
        if (dtl == null) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.gold < 500_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }
        int manhHD = -1;
        switch (dtl.template.id) {
            case 555 -> manhHD = 1368; // Áo trái đất
            case 557 -> manhHD = 1369; // Áo Namek
            case 559 -> manhHD = 1370; // Áo Xayda
            case 556 -> manhHD = 1371; // Quần Trái Đất
            case 558 -> manhHD = 1372; // Quần Namek
            case 560 -> manhHD = 1373; // Quần Xaday
            case 561 -> manhHD = 1380; // Nhẫn
            case 562 -> manhHD = 1374; // Găng Trái Đất
            case 564 -> manhHD = 1375; // Găng Namek
            case 566 -> manhHD = 1376; // Găng Xayda
            case 563 -> manhHD = 1377; // Giày Trái Đất
            case 565 -> manhHD = 1378; // Giày Namek
            case 567 -> manhHD = 1379; // Giày Xayda
        }
        if (manhHD != -1) {
            Item newItem = ItemService.gI().createNewItem((short) manhHD);
            newItem.itemOptions.add(new ItemOption(30, 0));
            InventoryService.gI().addItemBag(player, newItem);
            Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
            InventoryService.gI().subQuantityItemsBag(player, dtl, 1);
            player.inventory.subGold(500_000_000);
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
        }
    }

    public static void skhHDNdung(Player player) {
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Sai nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).count() != 3) {
            Service.gI().sendThongBao(player, "Thiếu đồ huỷ diệt");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.inventory.gold < 500_000_000) {
                Service.gI().sendThongBao(player, "Con cần 500 triệu vàng để đổi...");
                return;
            }
            player.inventory.subGold(500_000_000);
            Item dohdodayne = player.combineNew.itemsCombine.stream().filter(Item::isDHD).findFirst().orElse(null);
            if (dohdodayne == null) {
                return;
            }
            List<Item> itemdohdlucbovao = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).collect(Collectors.toList());
            CombineService.gI().sendEffectOpenItem(player, dohdodayne.template.iconID, dohdodayne.template.iconID);
            short itemId;
            if (dohdodayne.template.gender == 3 || dohdodayne.template.type == 4) {
                itemId = Manager.radaSKHVip[0];
            } else {
                itemId = Manager.doSKHVip[dohdodayne.template.gender][dohdodayne.template.type][0];
            }
            int skhId = ItemService.gI().randomSKHId(player.gender);
            Item item = ItemService.gI().itemSKH(itemId, skhId);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().subQuantityItemsBag(player, dohdodayne, 1);
            itemdohdlucbovao.forEach(it -> InventoryService.gI().subQuantityItemsBag(player, it, 1));
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            player.combineNew.itemsCombine.clear();
            CombineService.gI().reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }
}

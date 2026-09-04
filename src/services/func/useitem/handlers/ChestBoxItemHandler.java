package services.func.useitem.handlers;

import consts.ConstItem;
import consts.ConstNpc;
import models.item.Item;
import models.item.Item.ItemOption;
import models.player.Inventory;
import models.player.Player;
import services.CombineService;
import services.ItemService;
import services.RewardService;
import services.Service;
import services.func.useitem.ItemActionHandler;
import services.map.NpcService;
import services.player.InventoryService;
import services.player.PlayerService;
import utils.TimeUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Xử lý mở các loại Rương, Hộp quà, Capsule Kì Bí:
 * Rương gỗ (570), CSKB (380), Trứng rồng nhí (1426, 1427, 1428, 1429), Hộp quà (648, 1143, 1198-1200, 736),
 * Hộp thần linh, Quà quê, Set kích hoạt SKH.
 */
public class ChestBoxItemHandler implements ItemActionHandler {

    @Override
    public boolean canHandle(Player player, Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        int id = item.template.id;
        return id == 570 || id == 380 || id == 1426 || id == 1427 || id == 1428 || id == 1429
                || id == 648 || id == 1143 || id == 1198 || id == 1199 || id == 1200 || id == 736
                || id == 1318 || id == 1319 || id == 2000;
    }

    @Override
    public void handle(Player player, Item item, int bagIndex) {
        int id = item.template.id;
        switch (id) {
            case 570 -> {
                if (!Util.isAfterMidnight(player.lastTimeRewardWoodChest)) {
                    Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                } else {
                    openWoodChest(player, item);
                }
            }
            case 380 -> openCSKB(player, item);
            case 1426 -> open1426(player, item);
            case 1427 -> open1427(player, item);
            case 1428 -> open1428(player, item);
            case 1429 -> open1429(player, item);
            case 648 -> ItemService.gI().OpenItem648(player, item);
            case 1143 -> ItemService.gI().OpenItem1143(player, item);
            case 1198 -> ItemService.gI().OpenItem1198(player, item);
            case 1199 -> ItemService.gI().OpenItem1199(player, item);
            case 1200 -> ItemService.gI().OpenItem1200(player, item);
            case 736 -> ItemService.gI().OpenItem736(player, item);
            case 1318, 1319 -> useHopbabytry(player, item);
            case 2000 -> itemSKH(player, item);
        }
    }

    public static void openWoodChest(Player pl, Item item) {
        int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
        if (time == 0) {
            Service.gI().sendThongBao(pl, "Vì bạn quên không lấy chìa nên cần đợi 24h để bẻ khóa");
            return;
        }

        int param = item.itemOptions.get(0).param;
        int gold = 1000 * (int) Math.pow(param, 2) * 9;
        int gem = 0;
        int ruby = 0;
        String text = "Bạn nhận được\n";
        List<Item> rewards = new ArrayList<>();

        int[] itemT1 = {223, 224, 225, 17, 18};
        int[] itemT2 = {441, 442, 443, 444};
        int[] itemT3 = {445, 446, 447, 19, 20};

        if (param >= 6 && param <= 9) {
            gem = Util.nextInt(1, 3);
        } else if (param == 10) {
            gem = Util.nextInt(3, 6);
        } else if (param > 10) {
            gem = Util.nextInt(5, 10);
            ruby = Util.nextInt(1, 3);
        }

        int numClothes = param < 6 ? 1 : (param < 10 ? 2 : 3);
        for (int i = 0; i < numClothes; i++) {
            int clothesId = ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][param - 1];
            Item eq = ItemService.gI().createNewItem((short) clothesId);
            RewardService.gI().initBaseOptionClothes(eq.template.id, eq.template.type, eq.itemOptions);
            RewardService.gI().initStarOption(eq, new RewardService.RatioStar[]{
                new RewardService.RatioStar((byte) 1, 40 - param, 100),
                new RewardService.RatioStar((byte) 2, 20, 100),
                new RewardService.RatioStar((byte) 3, 10, 100),
                new RewardService.RatioStar((byte) 4, 5, 100),
            });
            rewards.add(eq);
        }

        int[] selectedPool = param <= 4 ? itemT1 : (param <= 7 ? itemT2 : itemT3);
        int numItems = Math.min(1 + param / 3, selectedPool.length);
        int[] randomItems = Util.pickNRandInArr(selectedPool, numItems);
        for (int id : randomItems) {
            Item it = ItemService.gI().createNewItem((short) id);
            it.quantity = 1;
            RewardService.gI().initBaseOptionSaoPhaLe(it);
            rewards.add(it);
        }

        if (param >= 11) {
            Item manhNhan = ItemService.gI().createNewItem((short) ConstItem.MANH_NHAN);
            manhNhan.quantity = Util.nextInt(1, 2);
            rewards.add(manhNhan);
        }

        pl.inventory.addGold(gold);
        pl.inventory.gem += gem;
        pl.inventory.ruby += ruby;
        if (gold > 0) pl.textRuongGo.add(text + "|4| " + Util.powerToString(gold) + " Vàng");
        if (gem > 0) pl.textRuongGo.add(text + "|1| " + gem + " Ngọc");
        if (ruby > 0) pl.textRuongGo.add(text + "|1| " + ruby + " Hồng Ngọc");

        for (Item reward : rewards) {
            InventoryService.gI().addItemBag(pl, reward);
            pl.textRuongGo.add(text + reward.getInfoItem());
        }

        NpcService.gI().createMenuConMeo(pl, ConstNpc.RUONG_GO, -1,
                "Bạn nhận được\n|1|+" + Util.powerToString(gold) + " vàng", "OK [" + pl.textRuongGo.size() + "]");

        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {76, 188, 189, 190, 381, 382, 383, 384, 385};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            if (index <= 3) {
                int goldReceived;
                int chance = Util.nextInt(1, 100);
                if (chance <= 82) {
                    goldReceived = Util.nextInt(5000, 20000);
                } else if (chance <= 92) {
                    goldReceived = Util.nextInt(20000, 30000);
                } else if (chance <= 97) {
                    goldReceived = Util.nextInt(30000, 1000000);
                } else {
                    goldReceived = Util.nextInt(1000000, 11000000);
                }
                pl.inventory.addGold(goldReceived);
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }

            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");
        }
    }

    private static void open1426(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {1423, 1424, 1425};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            Item it = ItemService.gI().createNewItem(temp[index]);
            it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30)));
            it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30)));
            it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30)));
            it.itemOptions.add(new ItemOption(14, Util.nextInt(10, 15)));
            it.itemOptions.add(new ItemOption(5, Util.nextInt(10, 20)));
            it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 7)));
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);
            icon[1] = it.template.iconID;
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1427(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item it = ItemService.gI().createNewItem((short) 1426);
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được Quả Trứng Rồng Nhí");
            InventoryService.gI().subQuantityItemsBag(pl, item, 99);
            InventoryService.gI().sendItemBags(pl);
            CombineService.gI().sendEffectOpenItem(pl, item.template.iconID, (short) 15127);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1428(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item it = ItemService.gI().createNewItem((short) 1427);
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineService.gI().sendEffectOpenItem(pl, item.template.iconID, it.template.iconID);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private static void open1429(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item it = ItemService.gI().createNewItem((short) 1428);
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được " + it.template.name);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            CombineService.gI().sendEffectOpenItem(pl, item.template.iconID, it.template.iconID);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useHopbabytry(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) < 1) {
            Service.gI().sendThongBao(pl, "Hãy chừa 1 ô trống để mở.");
            return;
        }
        int[] items = new int[]{1318, 1319};
        int itemID = items[Util.nextInt(0, items.length - 1)];
        Item reward = ItemService.gI().createNewItem((short) itemID);
        reward.itemOptions.add(new ItemOption(50, 18));
        if (itemID == 1319) {
            reward.itemOptions.add(new ItemOption(77, 7));
            reward.itemOptions.add(new ItemOption(103, 7));
            reward.itemOptions.add(new ItemOption(5, 11));
        } else {
            reward.itemOptions.add(new ItemOption(5, 8));
            reward.itemOptions.add(new ItemOption(14, 5));
        }
        if (Util.isTrue(99, 100)) {
            reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
        }
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        Service.gI().sendThongBao(pl, "Bạn đã nhận được " + reward.template.name);
    }

    public static void itemSKH(Player pl, Item item) {
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Hãy chọn một món quà", "Áo", "Quần", "Găng", "Giày", "Rada", "Từ Chối");
    }
}

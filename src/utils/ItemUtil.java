package utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import models.item.Item;
import models.map.ItemMap;
import models.map.Zone;
import static utils.Util.highlightsItem;

public class ItemUtil {

    public static ItemMap saoPhaLe(Zone zone, int tempId, int quantity, int x, int y, int playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, y, playerId);
        switch (tempId) {
            case 441 ->
                it.options.add(new Item.ItemOption(95, 5));
            case 442 ->
                it.options.add(new Item.ItemOption(96, 5));
            case 443 ->
                it.options.add(new Item.ItemOption(97, 5));
            case 444 ->
                it.options.add(new Item.ItemOption(99, 3));
            case 445 ->
                it.options.add(new Item.ItemOption(98, 3));
            case 446 ->
                it.options.add(new Item.ItemOption(100, 5));
            case 447 ->
                it.options.add(new Item.ItemOption(101, 5));
            case 459 -> {
                it.options.add(new Item.ItemOption(112, 80));
                it.options.add(new Item.ItemOption(93, 90));
                it.options.add(new Item.ItemOption(30, 1));
            }
        }
        return it;
    }

    public static ItemMap ratiItemkaio(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, zone.map.yPhysicInTop(x, y - 24), playerId);
        List<Integer> ao = Arrays.asList(232, 236, 240);
        List<Integer> quan = Arrays.asList(244, 248, 252);
        List<Integer> gang = Arrays.asList(256, 260, 264);
        List<Integer> giay = Arrays.asList(268, 272, 276);
        int ntl = 280;
        if (ao.contains(tempId)) {
            it.options.add(new Item.ItemOption(47, highlightsItem(it.itemTemplate.gender == 2, Util.nextInt(330, 400))));
        }
        if (quan.contains(tempId)) {
            it.options.add(new Item.ItemOption(6, highlightsItem(it.itemTemplate.gender == 0, Util.nextInt(20000, 30000))));
        }
        if (gang.contains(tempId)) {
            it.options.add(new Item.ItemOption(0, highlightsItem(it.itemTemplate.gender == 2, Util.nextInt(1550, 1700))));
        }
        if (giay.contains(tempId)) {
            it.options.add(new Item.ItemOption(7, highlightsItem(it.itemTemplate.gender == 1, Util.nextInt(20000, 30000))));
        }
        if (ntl == tempId) {
            it.options.add(new Item.ItemOption(14, Util.nextInt(10, 12)));
        }
        it.options.add(new Item.ItemOption(87, 0));
        it.options.add(new Item.ItemOption(30, 0));
        it.options.add(new Item.ItemOption(107, new Random().nextInt(2)));
        return it;
    }

    public static ItemMap ratiDTL(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, zone.map.yPhysicInTop(x, y - 24), playerId);
        List<Integer> ao = Arrays.asList(555, 557, 559);
        List<Integer> quan = Arrays.asList(556, 558, 560);
        List<Integer> gang = Arrays.asList(562, 564, 566);
        List<Integer> giay = Arrays.asList(563, 565, 567);
        int ntl = 561;
        if (ao.contains(tempId)) {
            it.options.add(new Item.ItemOption(47, highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(501) + 1300)));
        }
        if (quan.contains(tempId)) {
            it.options.add(new Item.ItemOption(22, highlightsItem(it.itemTemplate.gender == 0, new Random().nextInt(11) + 45)));
        }
        if (gang.contains(tempId)) {
            it.options.add(new Item.ItemOption(0, highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(1001) + 3500)));
        }
        if (giay.contains(tempId)) {
            it.options.add(new Item.ItemOption(23, highlightsItem(it.itemTemplate.gender == 1, new Random().nextInt(11) + 35)));
        }
        if (ntl == tempId) {
            it.options.add(new Item.ItemOption(14, new Random().nextInt(2) + 15));
        }
        it.options.add(new Item.ItemOption(209, 1)); // đồ rơi từ boss
        it.options.add(new Item.ItemOption(21, 18)); // ycsm 18 tỉ
        it.options.add(new Item.ItemOption(86, 0)); // ko thể gd
        if (Util.isTrue(20, 100)) {// tỉ lệ ra spl
            it.options.add(new Item.ItemOption(107, 1));
        } else if (Util.isTrue(3, 100)) {
            it.options.add(new Item.ItemOption(107, new Random().nextInt(4)));
        } else {
            it.options.add(new Item.ItemOption(107, new Random().nextInt(3)));
        }
        return it;
    }

    public static ItemMap ratiItemluonglong(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, zone.map.yPhysicInTop(x, y - 24), playerId);
        List<Integer> ao = Arrays.asList(233, 237, 241);
        List<Integer> quan = Arrays.asList(245, 249, 253);
        List<Integer> gang = Arrays.asList(257, 261, 265);
        List<Integer> giay = Arrays.asList(269, 273, 277);
        int ntl = 281;
        if (ao.contains(tempId)) {
            it.options.add(new Item.ItemOption(47, highlightsItem(it.itemTemplate.gender == 2, Util.nextInt(450, 500))));
        }
        if (quan.contains(tempId)) {
            it.options.add(new Item.ItemOption(6, highlightsItem(it.itemTemplate.gender == 0, Util.nextInt(24000, 30000))));
        }
        if (gang.contains(tempId)) {
            it.options.add(new Item.ItemOption(0, highlightsItem(it.itemTemplate.gender == 2, Util.nextInt(2250, 2400))));
        }
        if (giay.contains(tempId)) {
            it.options.add(new Item.ItemOption(7, highlightsItem(it.itemTemplate.gender == 1, Util.nextInt(24000, 30000))));
        }
        if (ntl == tempId) {
            it.options.add(new Item.ItemOption(14, Util.nextInt(11, 13)));
        }

        it.options.add(new Item.ItemOption(87, 0));
        it.options.add(new Item.ItemOption(30, 0));
        it.options.add(new Item.ItemOption(107, new Random().nextInt(3)));
        return it;
    }

}

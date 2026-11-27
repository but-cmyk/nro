package services;

import consts.ConstItem;
import models.Template;
import models.Template.ItemOptionTemplate;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import models.shop.ItemShop;
import server.Manager;
import services.player.InventoryService;
import utils.TimeUtil;
import utils.Util;
import utils.Logger;
import models.item.Item.ItemOption;

import java.util.*;
import models.map.Zone;

public class ItemService extends ItemServiceBase1 {

    protected static volatile ItemService i;

    public static ItemService gI() {
        if (i == null) {
            synchronized (ItemService.class) {
                if (i == null) {
                    i = new ItemService();
                }
            }
        }
        return i;
    }

    // Hàm lấy chỉ số random cho từng vật phẩm cụ thể
    protected int getRandomStats(short itemId) {
        switch (itemId) {
            // Quần - HP
            case 250:
                return Util.nextInt(12000, 16000); // 12k-16k
            case 251:
                return Util.nextInt(16000, 20000); // 16k-20k
            case 252:
                return Util.nextInt(20000, 24000); // 20k-24k
            case 253:
                return Util.nextInt(24000, 30000); // 24k-30k
            case 242:
                return Util.nextInt(14000, 18000); // 14k-18k
            case 243:
                return Util.nextInt(18000, 22000); // 18k-22k
            case 244:
                return Util.nextInt(22000, 26000); // 22k-26k
            case 245:
                return Util.nextInt(26000, 30000); // 26k-30k
            case 246:
                return Util.nextInt(13000, 17000); // 13k-17k
            case 247:
                return Util.nextInt(17000, 21000); // 17k-21k
            case 248:
                return Util.nextInt(21000, 25000); // 21k-25k
            case 249:
                return Util.nextInt(25000, 30000); // 25k-30k

            // Áo - Defense
            case 238:
                return Util.nextInt(230, 280);
            case 239:
                return Util.nextInt(280, 330);
            case 240:
                return Util.nextInt(330, 450);
            case 241:
                return Util.nextInt(450, 500);
            case 230:
                return Util.nextInt(200, 250);
            case 231:
                return Util.nextInt(250, 300);
            case 232:
                return Util.nextInt(300, 400);
            case 233:
                return Util.nextInt(400, 500);
            case 234:
                return Util.nextInt(200, 250);
            case 235:
                return Util.nextInt(250, 300);
            case 236:
                return Util.nextInt(300, 400);
            case 237:
                return Util.nextInt(400, 500);

            // Găng tay - Damage
            case 262:
                return Util.nextInt(700, 1050);
            case 263:
                return Util.nextInt(1050, 1550);
            case 264:
                return Util.nextInt(1550, 2250);
            case 265:
                return Util.nextInt(2250, 3000);
            case 254:
                return Util.nextInt(680, 1000);
            case 255:
                return Util.nextInt(1000, 1500);
            case 256:
                return Util.nextInt(1500, 2200);
            case 257:
                return Util.nextInt(2200, 3000);
            case 258:
                return Util.nextInt(630, 950);
            case 259:
                return Util.nextInt(950, 1450);
            case 260:
                return Util.nextInt(1450, 2150); // Sửa lỗi "160" thành "260"
            case 261:
                return Util.nextInt(2150, 3000);

            // Giày - MP
            case 274:
                return Util.nextInt(8000, 13000); // 8k-13k
            case 275:
                return Util.nextInt(13000, 18000); // 13k-18k
            case 276:
                return Util.nextInt(18000, 23000); // 18k-23k
            case 277:
                return Util.nextInt(23000, 30000); // 23k-30k
            case 266:
                return Util.nextInt(9000, 14000); // 9k-14k
            case 267:
                return Util.nextInt(14000, 19000); // 14k-19k
            case 268:
                return Util.nextInt(19000, 24000); // 19k-24k
            case 269:
                return Util.nextInt(24000, 30000); // 24k-30k
            case 270:
                return Util.nextInt(10000, 15000); // 10k-15k
            case 271:
                return Util.nextInt(15000, 20000); // 15k-20k
            case 272:
                return Util.nextInt(20000, 25000); // 20k-25k
            case 273:
                return Util.nextInt(25000, 30000); // 25k-30k

            // Rada - Critical Rate
            case 278:
                return Util.nextInt(9, 10); // 9-10
            case 279:
                return Util.nextInt(10, 11); // 10-11
            case 280:
                return Util.nextInt(11, 12); // 11-12
            case 281:
                return Util.nextInt(12, 13); // 12-13

            default:
                return 0;
        }
    }

    // public ItemMap randtest(Zone zone, int quantity, int x, int y, long id) {
    // short idTempTL;
    // // Áo - Giúp bạn giảm sát thương
    // short[] ao = {230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241};
    // // Quần - Giúp bạn tăng HP
    // short[] quan = {242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253};
    // // Găng tay - Giúp bạn tăng sức đánh
    // short[] gang = {254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265};
    // // Giày - Giúp bạn tăng MP
    // short[] giay = {266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277};
    // // Rada - Giúp bạn tăng Chí Mạng
    // short[] rada = {278, 279, 280, 281};
    // short[] options = {86, 87};
    //
    // if (Util.isTrue(10, 100)) { // Rada (10%)
    // idTempTL = rada[Util.nextInt(rada.length)];
    // } else if (Util.isTrue(25, 100)) { // Găng tay (15%)
    // idTempTL = gang[Util.nextInt(gang.length)];
    // } else if (Util.isTrue(45, 100)) { // Quần (20%)
    // idTempTL = quan[Util.nextInt(quan.length)];
    // } else if (Util.isTrue(75, 100)) { // Áo (30%)
    // idTempTL = ao[Util.nextInt(ao.length)];
    // } else { // Giày (25%)
    // idTempTL = giay[Util.nextInt(giay.length)];
    // }
    //
    // int tiLe = Util.nextInt(100, 115);
    // List<ItemOption> itemoptions = new ArrayList<>();
    //
    // // Lấy level của vật phẩm để tính chỉ số
    // int level = getItemLevel(idTempTL);
    //
    // // Xử lý thuộc tính theo từng loại trang bị với chỉ số random mới
    // if (idTempTL >= 230 && idTempTL <= 241) { // Áo - Giảm sát thương
    // int defense = getRandomStats(idTempTL);
    // if (defense > 0) {
    // itemoptions.add(new ItemOption(47, defense));
    // } else {
    // // Fallback to old logic if item not in custom list
    // defense = (600 + (level - 9) * 200) / 2;
    // itemoptions.add(new ItemOption(47, defense * tiLe / 100));
    // }
    // if (tiLe > 100) {
    // itemoptions.add(new ItemOption(206, tiLe - 100));
    // }
    // } else if (idTempTL >= 242 && idTempTL <= 253) { // Quần - Tăng HP
    // int customHp = getRandomStats(idTempTL);
    // if (customHp > 0) {
    // itemoptions.add(new ItemOption(22, customHp / 1000));
    // itemoptions.add(new ItemOption(27, customHp / 20));
    // } else {
    // // Fallback to old logic if item not in custom list
    // int baseHp = (40000 + (level - 9) * 8000) / 2;
    // int chiso = baseHp * tiLe / 100;
    // itemoptions.add(new ItemOption(22, chiso / 1000));
    // itemoptions.add(new ItemOption(27, chiso / 20));
    // }
    // if (tiLe > 100) {
    // itemoptions.add(new ItemOption(206, tiLe - 100));
    // }
    // } else if (idTempTL >= 254 && idTempTL <= 265) { // Găng tay - Tăng sức đánh
    // int customDamage = getRandomStats(idTempTL);
    // if (customDamage > 0) {
    // itemoptions.add(new ItemOption(0, customDamage));
    // } else {
    // // Fallback to old logic if item not in custom list
    // int damage = (3500 + (level - 9) * 500) * 10 / 25;
    // itemoptions.add(new ItemOption(0, damage * tiLe / 100));
    // }
    // if (tiLe > 100) {
    // itemoptions.add(new ItemOption(206, tiLe - 100));
    // }
    // } else if (idTempTL >= 266 && idTempTL <= 277) { // Giày - Tăng MP
    // int customMp = getRandomStats(idTempTL);
    // if (customMp > 0) {
    // itemoptions.add(new ItemOption(23, customMp / 1000));
    // itemoptions.add(new ItemOption(28, customMp / 20));
    // } else {
    // // Fallback to old logic if item not in custom list
    // int baseMp = (35000 + (level - 9) * 7000) / 2;
    // int chiso = baseMp * tiLe / 100;
    // itemoptions.add(new ItemOption(23, chiso / 1000));
    // itemoptions.add(new ItemOption(28, chiso / 20));
    // }
    // if (tiLe > 100) {
    // itemoptions.add(new ItemOption(206, tiLe - 100));
    // }
    // } else if (idTempTL >= 278 && idTempTL <= 281) { // Rada - Tăng Chí Mạng
    // int customCritical = getRandomStats(idTempTL);
    // if (customCritical > 0) {
    // itemoptions.add(new ItemOption(14, customCritical));
    // } else {
    // // Fallback to old logic if item not in custom list
    // int criticalRate = 10 + (level - 9) * 3;
    // itemoptions.add(new ItemOption(14, criticalRate * tiLe / 100));
    // }
    // }
    //
    // if (Util.isTrue(30, 100)) {
    // if (Util.isTrue(70, 100)) {
    // itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
    // }
    // }
    //
    // // itemoptions.add(new ItemOption(21, Util.nextInt(15, 17)));
    // ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
    // it.options.clear();
    // it.options.addAll(itemoptions);
    // return it;
    // }

    public int randTempItemDoSao(int gender) {
        int[][] ao = { { 3, 34, 136, 137, 138, 139 }, { 4, 42, 152, 153, 154, 155 }, { 5, 50, 168, 169, 170, 171 } };
        int[][] quan = { { 9, 36, 140, 141, 142, 143 }, { 10, 44, 156, 157, 158, 159 },
                { 11, 52, 172, 173, 174, 175 } };
        int[][] gang = { { 37, 38, 144, 145, 146, 147 }, { 25, 45, 160, 161, 162, 163 },
                { 26, 54, 176, 177, 178, 179 } };
        int[][] giay = { { 39, 40, 148, 149, 150, 151 }, { 31, 48, 164, 165, 166, 167 },
                { 32, 56, 180, 181, 182, 183 } };
        int[][] rada = { { 58, 59, 184, 185, 186, 187 }, { 58, 59, 184, 185, 186, 187 },
                { 58, 59, 184, 185, 186, 187 } };
        int[][][] item = { ao, gang, quan, giay, rada };

        Random random = new Random();

        int type;
        if (Util.isTrue(10, 100)) {
            type = 4; // rada
        } else if (Util.isTrue(23, 100)) {
            type = 3; // giay
        } else if (Util.isTrue(23, 100)) {
            type = 1; // ao
        } else if (Util.isTrue(23, 100)) {
            type = 0; // gang
        } else {
            type = 2; // quan
        }

        int index = random.nextInt(6); // Lấy giá trị ngẫu nhiên từ 0 đến 5

        return item[type][gender][index];
    }

    public ItemMap randDoTL(Zone zone, int quantity, int x, int y, long id) {
        short idTempTL, type;
        short[] ao = { 555, 557, 559 };
        short[] quan = { 556, 558, 560 };
        short[] gang = { 562, 564, 566 };
        short[] giay = { 563, 565, 567 };
        short[] nhan = { 561 };
        short[] options = { 86, 87, 208 };
        if (Util.isTrue(10, 100)) {
            idTempTL = nhan[0];
            type = 4; // rada
        } else if (Util.isTrue(10, 100)) {
            idTempTL = gang[Util.nextInt(3)];
            type = 2; // gang
        } else if (Util.isTrue(50, 100)) {
            idTempTL = quan[Util.nextInt(3)];
            type = 1; // quan
        } else if (Util.isTrue(50, 100)) {
            idTempTL = ao[Util.nextInt(3)];
            type = 0; // ao
        } else {
            idTempTL = giay[Util.nextInt(3)];
            type = 3; // giay
        }
        int tiLe = Util.nextInt(100, 100);
        List<ItemOption> itemoptions = new ArrayList<>();
        switch (type) {
            case 0 ->
                itemoptions.add(new ItemOption(47, Util.nextInt(800, 900) * tiLe / 100));
            case 1 -> {
                int chiso = Util.nextInt(46000, 49000) * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso * 125 / 1000));
            }
            case 2 ->
                itemoptions.add(new ItemOption(0, Util.nextInt(4300, 4500) * tiLe / 100));
            case 3 -> {
                int chiso = Util.nextInt(46000, 49000) * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso * 125 / 1000));
            }
            case 4 ->
                itemoptions.add(new ItemOption(14, Util.nextInt(14, 17) * tiLe / 100));
        }
        if (Util.isTrue(90, 100)) {
            itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
        }
        itemoptions.add(new ItemOption(21, Util.nextInt(15, 19)));
        ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
        it.options.clear();
        it.options.addAll(itemoptions);
        return it;
    }

    public ItemMap randDoKaio(Zone zone, int quantity, int x, int y, long id) {
        short idTempTL, type;
        short[] ao = { 232, 236, 240 };
        short[] quan = { 244, 248, 252 };
        short[] gang = { 256, 260, 264 };
        short[] giay = { 268, 272, 276 };
        short[] nhan = { 280 };
        short[] options = { 86, 87, 208 };
        if (Util.isTrue(10, 100)) {
            idTempTL = nhan[0];
            type = 4; // rada
        } else if (Util.isTrue(20, 100)) {
            idTempTL = gang[Util.nextInt(3)];
            type = 2; // gang
        } else if (Util.isTrue(50, 100)) {
            idTempTL = quan[Util.nextInt(3)];
            type = 1; // quan
        } else if (Util.isTrue(50, 100)) {
            idTempTL = ao[Util.nextInt(3)];
            type = 0; // ao
        } else {
            idTempTL = giay[Util.nextInt(3)];
            type = 3; // giay
        }
        int tiLe = Util.nextInt(100, 100);
        List<ItemOption> itemoptions = new ArrayList<>();
        switch (type) {
            case 0 ->
                itemoptions.add(new ItemOption(47, Util.nextInt(330, 400) * tiLe / 100));
            case 1 -> {
                int chiso = Util.nextInt(20000, 25000) * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso * 125 / 1000));
            }
            case 2 ->
                itemoptions.add(new ItemOption(0, Util.nextInt(1550, 1700) * tiLe / 100));
            case 3 -> {
                int chiso = Util.nextInt(20000, 25000) * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso * 125 / 1000));
            }
            case 4 ->
                itemoptions.add(new ItemOption(14, Util.nextInt(10, 12) * tiLe / 100));
        }
        if (Util.isTrue(1, 100)) {
            itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
        }
        // itemoptions.add(new ItemOption(21, 170000000));
        itemoptions.add(new ItemOption(107, Util.nextInt(0, 2)));

        ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
        it.options.clear();
        it.options.addAll(itemoptions);
        return it;
    }

    public ItemMap randDoLuongLong(Zone zone, int quantity, int x, int y, long id) {
        short idTempTL, type;
        short[] ao = { 233, 237, 241 };
        short[] quan = { 245, 249, 253 };
        short[] gang = { 257, 261, 265 };
        short[] giay = { 269, 273, 277 };
        short[] nhan = { 281 };
        short[] options = { 86, 87, 208 };
        if (Util.isTrue(10, 100)) {
            idTempTL = nhan[0];
            type = 4; // rada
        } else if (Util.isTrue(20, 100)) {
            idTempTL = gang[Util.nextInt(3)];
            type = 2; // gang
        } else if (Util.isTrue(50, 100)) {
            idTempTL = quan[Util.nextInt(3)];
            type = 1; // quan
        } else if (Util.isTrue(50, 100)) {
            idTempTL = ao[Util.nextInt(3)];
            type = 0; // ao
        } else {
            idTempTL = giay[Util.nextInt(3)];
            type = 3; // giay
        }
        int tiLe = Util.nextInt(100, 100);
        List<ItemOption> itemoptions = new ArrayList<>();
        switch (type) {
            case 0 ->
                itemoptions.add(new ItemOption(47, Util.nextInt(450, 500) * tiLe / 100));
            case 1 -> {
                int chiso = Util.nextInt(24000, 30000) * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso * 125 / 1000));
            }
            case 2 ->
                itemoptions.add(new ItemOption(0, Util.nextInt(2250, 2450) * tiLe / 100));
            case 3 -> {
                int chiso = Util.nextInt(24000, 30000) * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso * 125 / 1000));
            }
            case 4 ->
                itemoptions.add(new ItemOption(14, Util.nextInt(11, 13) * tiLe / 100));
        }
        if (Util.isTrue(1, 100)) {
            itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
        }
        // itemoptions.add(new ItemOption(21, 1500000000));
        itemoptions.add(new ItemOption(107, Util.nextInt(0, 2)));
        ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
        it.options.clear();
        it.options.addAll(itemoptions);
        return it;
    }

    // public boolean itemHasOption(Item item, int optionId) {
    // return item.itemOptions.stream().anyMatch(io -> io.optionTemplate.id ==
    // optionId);
    // }

    public void OpenSKH(Player player, int itemUseId, int select) throws Exception {
        if (select < 0 || select > 4) {
            return;
        }
        Item itemUse = InventoryService.gI().findItem(player.inventory.itemsBag, itemUseId);
        int[][] items = { { 0, 6, 21, 27, 12 }, { 1, 7, 22, 28, 12 }, { 2, 8, 23, 29, 12 } };
        int[][] options = { { 128, 129, 246 }, { 130, 131, 132 }, { 133, 135, 134 } };
        int[][] optionsNEWTD = { { 243, 244, 245 } };
        int[][] optionsNEWNM = { { 235, 236, 237 } };
        int[][] optionsNEWXD = { { 239, 240, 241 } };
        int skhv1 = 25;// ti le
        int skhv2 = 35;// ti le
        int skhc = 40;// ti le
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
        switch (itemUseId) {
            case 1227:
                item = itemSKH(items[0][select], options[0][skhId]);
                break;
            case 1228:
                item = itemSKH(items[1][select], options[1][skhId]);
                break;
            case 1229:
                item = itemSKH(items[2][select], options[2][skhId]);
                break;
        }
        if (item != null && InventoryService.gI().getCountEmptyBag(player) > 0) {
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public void OpenSKHVIP(Player player, int itemUseId, int select) throws Exception {
        if (select < 0 || select > 4) {
            return;
        }
        Item itemUse = InventoryService.gI().findItem(player.inventory.itemsBag, itemUseId);
        int[][] items = {
                { Manager.aotdVip[Util.nextInt(0, 5)], Manager.quantdVip[Util.nextInt(0, 5)],
                        Manager.gangtdVip[Util.nextInt(0, 5)], Manager.giaytdVip[Util.nextInt(0, 5)],
                        Manager.radaSKHVipVip[Util.nextInt(0, 5)] },
                { Manager.aonmVip[Util.nextInt(0, 5)], Manager.quannmVip[Util.nextInt(0, 5)],
                        Manager.gangnmVip[Util.nextInt(0, 5)], Manager.giaynmVip[Util.nextInt(0, 5)],
                        Manager.radaSKHVipVip[Util.nextInt(0, 5)] },
                { Manager.aoxdVip[Util.nextInt(0, 5)], Manager.quanxdVip[Util.nextInt(0, 5)],
                        Manager.gangxdVip[Util.nextInt(0, 5)], Manager.giayxdVip[Util.nextInt(0, 5)],
                        Manager.radaSKHVipVip[Util.nextInt(0, 5)] } };
        int[][] options = { { 128, 129, 214 }, { 130, 131, 132 }, { 133, 135, 134 } };
        int skhv1 = 25;// ti le
        int skhv2 = 35;// ti le
        int skhc = 40;// ti le
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
        switch (itemUseId) {
            case 1467:
                item = itemSKH(items[0][select], options[0][skhId]);
                break;
            case 1468:
                item = itemSKH(items[1][select], options[1][skhId]);
                break;
            case 1469:
                item = itemSKH(items[2][select], options[2][skhId]);
                break;
        }
        if (item != null && InventoryService.gI().getCountEmptyBag(player) > 0) {
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public int randomSKHId(byte gender) {
        // Handle the case where gender is 3 (invalid or special case)
        if (gender == 3) {
            gender = (byte) Util.nextInt(0, 2); // Randomly set gender to 0 or 1
        }

        // Validate gender to be within the expected range (0, 1, 2)
        if (gender < 0 || gender > 2) {
            // Log or handle the error: gender is out of range
            return -1; // Or some other error handling value
        }

        // Array with different SKH options based on gender
        int[][] options = {
                { 128, 129, 246 }, // For gender 0
                { 130, 131, 132 }, // For gender 1
                { 133, 135, 134 } // For gender 2
        };

        // Skew values for determining the random skhId
        int skhv1 = 25;
        int skhv2 = 35;
        int skhc = 40;
        int skhId = -1;

        // Random value between 1 and 100 to determine which skhId to choose
        int rd = Util.nextInt(1, 100);

        if (rd <= skhv1) {
            skhId = 0; // First SKH option
        } else if (rd <= skhv1 + skhv2) {
            skhId = 1; // Second SKH option
        } else if (rd <= skhv1 + skhv2 + skhc) {
            skhId = 2; // Third SKH option
        }

        // Return the appropriate SKH ID based on gender and random choice
        return options[gender][skhId];
    }

    public void OpenItem1259(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = randomda();
            if (rd <= rac) {
                item = caitrangchuot(true);
            } else if (rd <= rac + ruby) {
                item = caitrangho(true);
            } else if (rd <= rac + ruby + dbv) {
                item = randomda();
            } else if (rd <= rac + ruby + dbv + vb) {
                item = petran(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1245 error", e);
        }
    }

    public void OpenItem1246(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = randomda();
            if (rd <= rac) {
                item = cobaolixi(true);
            } else if (rd <= rac + ruby) {
                item = caitrangthantai(true);
            } else if (rd <= rac + ruby + dbv) {
                item = thoivangbay(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = bunmathanhlich(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1246 error", e);
        }
    }

    public void OpenItem1247(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = randommanhkhi();
            if (rd <= rac) {
                item = cachep(true);
            } else if (rd <= rac + ruby) {
                item = caitrangthantai(true);
            } else if (rd <= rac + ruby + dbv) {
                item = caitrangbroly(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            // player.lixi += 1;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1247 error", e);
        }
    }

    public void OpenItem1305(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 25;
            int ruby = 25;
            int dbv = 25;
            int vb = 25;
            Item item = null;
            if (rd <= rac) {
                item = BabySecret(true);
            } else if (rd <= rac + ruby) {
                item = BabyRabbit(true);
            } else if (rd <= rac + ruby + dbv) {
                item = BabyDino(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = BabyPanda(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1248 error", e);
        }
    }

    public void OpenItem1258(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = daolong(true);
            if (rd <= rac) {
                item = daolong(true);
            } else if (rd <= rac + ruby) {
                item = bena(true);
            } else if (rd <= rac + ruby + dbv) {
                item = cacheprong(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = caitrangtet(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            player.phaobong += 1;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1249 error", e);
        }
    }

    public void OpenItem1245(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 20;
            int ruby = 20;
            int dbv = 20;
            int vb = 20;
            int bh = 20;
            Item item = randomda();
            if (rd <= rac) {
                item = cobaolixi(true);
            } else if (rd <= rac + ruby) {
                item = caitrangthantai(true);
            } else if (rd <= rac + ruby + dbv) {
                item = thoivangbay(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = bunmathanhlich(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1250 error", e);
        }
    }

    public Item randomda() {
        Item item = createItemSetKichHoat(Util.nextInt(1074, 1083), 1);
        return item;
    }

    public Item randommanhkhi() {
        Item item = createItemSetKichHoat(Util.nextInt(1276, 1278), 1);
        return item;
    }

    public Item petran(boolean rating) {
        Item item = createItemSetKichHoat(1240, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(10) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(10) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(10) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item cobaolixi(boolean rating) {
        Item item = createItemSetKichHoat(1274, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item cachep(boolean rating) {
        Item item = createItemSetKichHoat(1235, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item BabySecret(boolean rating) {
        Item item = createItemSetKichHoat(1301, 1);
        item.itemOptions.add(new Item.ItemOption(50, 22));
        item.itemOptions.add(new Item.ItemOption(77, 22));
        item.itemOptions.add(new Item.ItemOption(94, 8));
        item.itemOptions.add(new Item.ItemOption(5, 11));
        item.itemOptions.add(new Item.ItemOption(14, 8));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item BabyDino(boolean rating) {
        Item item = createItemSetKichHoat(1303, 1);
        item.itemOptions.add(new Item.ItemOption(77, 18));
        item.itemOptions.add(new Item.ItemOption(5, 7));
        item.itemOptions.add(new Item.ItemOption(14, 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 1));// hsd
        }
        return item;
    }

    public Item BabyRabbit(boolean rating) {
        Item item = createItemSetKichHoat(1302, 1);
        item.itemOptions.add(new Item.ItemOption(77, 18));
        item.itemOptions.add(new Item.ItemOption(94, 5));
        item.itemOptions.add(new Item.ItemOption(108, 7));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 1));// hsd
        }
        return item;
    }

    public Item BabyPanda(boolean rating) {
        Item item = createItemSetKichHoat(1304, 1);
        item.itemOptions.add(new Item.ItemOption(50, 16));
        item.itemOptions.add(new Item.ItemOption(77, 16));
        item.itemOptions.add(new Item.ItemOption(103, 16));
        item.itemOptions.add(new Item.ItemOption(229, 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 1));// hsd
        }
        return item;
    }

    public Item daolong(boolean rating) {
        Item item = createItemSetKichHoat(1273, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item bena(boolean rating) {
        Item item = createItemSetKichHoat(1240, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item cacheprong(boolean rating) {
        Item item = createItemSetKichHoat(1272, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(15) + 5));
        item.itemOptions.add(new Item.ItemOption(84, new Random().nextInt(15) + 5));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item caitrangtet(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(1269, 1271), 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(30) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(30) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(30) + 5));
        item.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(1) + 10));
        item.itemOptions.add(new Item.ItemOption(101, new Random().nextInt(10) + 40));
        item.itemOptions.add(new Item.ItemOption(95, new Random().nextInt(10) + 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item caitrangthantai(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(1266, 1268), 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(1) + 10));
        item.itemOptions.add(new Item.ItemOption(101, new Random().nextInt(10) + 40));
        item.itemOptions.add(new Item.ItemOption(95, new Random().nextInt(10) + 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item caitrangbroly(boolean rating) {
        Item item = createItemSetKichHoat(1241, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(5, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(1) + 10));
        item.itemOptions.add(new Item.ItemOption(101, new Random().nextInt(10) + 40));
        item.itemOptions.add(new Item.ItemOption(95, new Random().nextInt(10) + 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item thoivangbay(boolean rating) {
        Item item = createItemSetKichHoat(1265, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(84, 0));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item bunmathanhlich(boolean rating) {
        Item item = createItemSetKichHoat(1264, 1);
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(1) + 10));
        item.itemOptions.add(new Item.ItemOption(101, new Random().nextInt(10) + 40));
        item.itemOptions.add(new Item.ItemOption(95, new Random().nextInt(10) + 20));
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item caitrangchuot(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(754, 756), 1);
        item.itemOptions.add(new Item.ItemOption(77, 30));
        item.itemOptions.add(new Item.ItemOption(27, 50));
        item.itemOptions.add(new Item.ItemOption(50, 20));
        item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));// hsds
        return item;
    }

    public Item caitrangho(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(952, 953), 1);
        item.itemOptions.add(new Item.ItemOption(77, 22));
        item.itemOptions.add(new Item.ItemOption(103, 22));
        item.itemOptions.add(new Item.ItemOption(50, 23));
        item.itemOptions.add(new Item.ItemOption(94, 10));
        item.itemOptions.add(new Item.ItemOption(27, 11));
        item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));// hsds
        return item;
    }

    // public int[] randOptionItemKichHoatNew(int gender) {
    // int op1;
    // int op2;
    // int op3;
    // int op4;
    // switch (gender) {
    // case 0 -> {
    //
    // op1 = 242;
    // op2 = 243;
    // op3 = 244;
    // op4 = 245;
    //
    // }
    // case 1 -> {
    //
    // op1 = 234;
    // op2 = 235;
    // op3 = 236;
    // op4 = 237;
    //
    // }
    // case 2 -> {
    //
    // op1 = 238;
    // op2 = 239;
    // op3 = 240;
    // op4 = 241;
    //
    // }
    // default -> {
    //
    // op1 = -1;
    // op2 = -1;
    // op3 = -1;
    // op4 = -1;
    //
    // }
    // }
    // int[] options = {op1, op2, op3, op4};
    // return options;
    // }

    public void settltd(Player player) {
        giveSetThanLinh(player, new int[]{555, 556, 562, 563, 561}, new int[]{128, 129, 246});
    }

    public void settlnm(Player player) {
        giveSetThanLinh(player, new int[]{557, 558, 564, 565, 561}, new int[]{130, 131, 132});
    }

    public void settlxd(Player player) {
        giveSetThanLinh(player, new int[]{559, 560, 566, 567, 561}, new int[]{133, 134, 135});
    }

    protected void giveSetThanLinh(Player player, int[] itemIds, int[] skhPool) {
        if (player == null || player.inventory == null) {
            return;
        }
        Item hq = InventoryService.gI().findItem(player.inventory.itemsBag, 1324);
        if (hq == null || hq.quantity < 1) {
            Service.gI().sendThongBao(player, "Bạn không có Hộp thần linh");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < itemIds.length) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất " + itemIds.length + " ô trống hành trang");
            return;
        }

        // Chọn ngẫu nhiên 1 loại SKH đồng nhất cho cả bộ
        int skhId = skhPool[Util.nextInt(0, skhPool.length - 1)];
        int skhOpt2 = optionIdSKH(skhId);

        // Trừ hộp trước khi thêm vật phẩm
        InventoryService.gI().subQuantityItemsBag(player, hq, 1);

        for (int itemId : itemIds) {
            Item item = otpThanLinh((short) itemId);
            if (item != null) {
                item.itemOptions.add(new Item.ItemOption(skhId, 1));
                if (skhOpt2 > 0) {
                    item.itemOptions.add(new Item.ItemOption(skhOpt2, 1));
                }
                item.itemOptions.add(new Item.ItemOption(30, 1));
                item.itemOptions.add(new Item.ItemOption(87, 0));
                item.content = item.getContent();
                item.info = item.getInfo();
                InventoryService.gI().addItemBag(player, item);
            }
        }
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn đã nhận được set Thần Linh kích hoạt!");
    }

    public Item otpThanLinh(short tempId) {
        return otpTL(tempId, 1);
    }

    public Item otpTL(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        if (item.template == null) {
            return null;
        }
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(21, 15));
            item.itemOptions.add(new ItemOption(47, Util.nextInt(1000, 1500)));
        } else if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(21, 15));
            item.itemOptions.add(new ItemOption(22, Util.nextInt(45, 70)));
        } else if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(21, 15));
            item.itemOptions.add(new ItemOption(0, Util.nextInt(3500, 4500)));
        } else if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(21, 15));
            item.itemOptions.add(new ItemOption(23, Util.nextInt(45, 70)));
        } else if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(21, 15));
            item.itemOptions.add(new ItemOption(14, Util.nextInt(13, 15)));
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }
}

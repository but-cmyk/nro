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

public abstract class ItemServiceBase1 {

    public short getItemIdByIcon(short IconID) {
        for (int i = 0; i < Manager.ITEM_TEMPLATES.size(); i++) {
            if (Manager.ITEM_TEMPLATES.get(i).iconID == IconID) {
                return Manager.ITEM_TEMPLATES.get(i).id;
            }
        }
        return -1;
    }

    public Item createItemNull() {
        Item item = new Item();
        return item;
    }

    public void removeAndAddOptionTemplate(List<Item.ItemOption> itemOptions, int removeId) {
        int id = 0;
        int param = 0;
        Random random = new Random();

        if (removeId == 227) {
            int randomChoice = random.nextInt(100);

            id = (randomChoice < 60) ? 93 : 206;
            param = Util.nextInt(15, 30);

            itemOptions.removeIf(io -> io.optionTemplate.id == 227);

            itemOptions.add(new ItemOption(new Item.ItemOption(id, param)));
        }
    }

    public Item createItemFromItemShop(ItemShop itemShop) {
        Item item = new Item();
        item.template = itemShop.temp;
        item.quantity = 1;
        item.content = item.getContent();
        item.info = item.getInfo();
        for (Item.ItemOption io : itemShop.options) {
            item.itemOptions.add(new Item.ItemOption(io));

            removeAndAddOptionTemplate(item.itemOptions, new Item.ItemOption(io).optionTemplate.id);

        }
        // Tất cả vật phẩm mua bằng Ngọc Xanh hoặc Hồng Ngọc đều không thể giao dịch (chống farm clone)
        if (itemShop.typeSell == ShopService.COST_GEM || itemShop.typeSell == ShopService.COST_RUBY) {
            item.itemOptions.removeIf(io -> io != null && io.optionTemplate != null && io.optionTemplate.id == 86);
            if (!item.isHaveOption(30)) {
                item.itemOptions.add(new Item.ItemOption(30, 0));
            }
        }
        return item;
    }

    public Item itemSKH(int itemId, int skhId) {
        Item item = createItemSetKichHoat(itemId, 1);
        if (item != null) {
            item.itemOptions.addAll(ItemService.gI().getListOptionItemShop((short) itemId));
            item.itemOptions.add(new Item.ItemOption(skhId, 1));
            item.itemOptions.add(new Item.ItemOption(optionIdSKH(skhId), 1));
            item.itemOptions.add(new Item.ItemOption(30, 1));
        }
        return item;
    }

    public int optionIdSKH(int skhId) {
        switch (skhId) {
            case 127:
                return 139;
            case 128:
                return 140;
            case 129:
                return 141;
            case 130:
                return 142;
            case 131:
                return 143;
            case 132:
                return 144;
            case 133:
                return 136;
            case 134:
                return 137;
            case 135:
                return 138;
            case 212:
                return 213;
            case 246:
                return 215;
            case 216:
                return 217;
        }
        return 0;
    }

    // public int randTempItemDoSao() {
    // // Mảng các vật phẩm được chia theo loại
    // int[][] items = {
    // // Áo
    // {0, 1, 2, 3, 4, 5, 33, 34, 41, 42, 49, 50, 136, 137, 138, 139, 152, 153, 154,
    // 155, 168, 169, 170, 171},
    // // Quần
    // {6, 7, 8, 9, 10, 11, 35, 36, 43, 44, 51, 52, 140, 141, 142, 143, 156, 157,
    // 158, 159, 172, 173, 174, 175},
    // // Găng
    // {21, 22, 23, 24, 25, 26, 37, 38, 45, 46, 53, 54, 144, 145, 146, 147, 160,
    // 161, 162, 163, 176, 177, 178, 179},
    // // Giày
    // {27, 28, 29, 30, 31, 32, 39, 40, 47, 48, 55, 56, 148, 149, 150, 151},
    // // Rada
    // {12, 57, 58, 59, 184, 185, 186, 187}
    // };
    //
    // // Lựa chọn loại vật phẩm ngẫu nhiên dựa trên xác suất
    // int type;
    // if (Util.isTrue(10, 100)) {
    // type = 4; // rada
    // } else if (Util.isTrue(30, 100)) {
    // type = 3; // gang
    // } else if (Util.isTrue(50, 100)) {
    // type = 1; // quan
    // } else if (Util.isTrue(70, 100)) {
    // type = 0; // ao
    // } else {
    // type = 2; // giay
    // }
    //
    // // Kiểm tra xem `type` có hợp lệ hay không
    // if (type < 0 || type >= items.length) {
    // return -1; // Trường hợp không hợp lệ cho type
    // }
    //
    // // Kiểm tra mảng items[type] có phần tử không
    // if (items[type].length > 0) {
    // // Trả về ID ngẫu nhiên từ mảng tương ứng
    // return items[type][Util.nextInt(items[type].length)];
    // } else {
    // // Nếu mảng không có phần tử hợp lệ, trả về -1 hoặc xử lý theo cách khác
    // return -1; // ID mặc định không hợp lệ
    // }
    // }

    public Item DoThienSu(int itemId, int gender) {
        Item dots = createItemSetKichHoat(itemId, 1);
        List<Integer> ao = Arrays.asList(1048, 1049, 1050);
        List<Integer> quan = Arrays.asList(1051, 1052, 1053);
        List<Integer> gang = Arrays.asList(1054, 1055, 1056);
        List<Integer> giay = Arrays.asList(1057, 1058, 1059);
        List<Integer> nhan = Arrays.asList(1060, 1061, 1062);
        // áo
        if (ao.contains(itemId)) {
            dots.itemOptions
                    .add(new ItemOption(47, Util.highlightsItem(gender == 2, new Random().nextInt(1201) + 2800))); // áo
                                                                                                                   // từ
                                                                                                                   // 2800-4000
                                                                                                                   // giáp
        }
        // quần
        if (Util.isTrue(80, 100)) {
            if (quan.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(11) + 120))); // hp
                                                                                                                    // 120k-130k
            }
        } else {
            if (quan.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(22, Util.highlightsItem(gender == 0, new Random().nextInt(21) + 130))); // hp
                                                                                                                    // 130-150k
                                                                                                                    // 15%
            }
        }
        // găng
        if (Util.isTrue(80, 100)) {
            if (gang.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(651) + 10350))); // 9350-10000
            }
        } else {
            if (gang.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(0, Util.highlightsItem(gender == 2, new Random().nextInt(1001) + 15500))); // gang
                                                                                                                       // 15%
                                                                                                                       // 10-11k
                                                                                                                       // -xayda
                                                                                                                       // 12k1
            }
        }
        // giày
        if (Util.isTrue(80, 100)) {
            if (giay.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 90))); // ki
                                                                                                                   // 90-110k
            }
        } else {
            if (giay.contains(itemId)) {
                dots.itemOptions
                        .add(new ItemOption(23, Util.highlightsItem(gender == 1, new Random().nextInt(21) + 110))); // ki
                                                                                                                    // 110-130k
            }
        }
        if (nhan.contains(itemId)) {
            dots.itemOptions.add(new ItemOption(14, Util.highlightsItem(gender == 1, new Random().nextInt(6) + 1))); // nhẫn
                                                                                                                     // 18-20%
        }
        dots.itemOptions.add(new ItemOption(21, 30));
        dots.itemOptions.add(new ItemOption(30, 1));
        return dots;
    }

    public Item copyItem(Item item) {
        Item it = new Item();
        it.itemOptions = new ArrayList<>();
        it.template = item.template;
        it.info = item.info;
        it.content = item.content;
        it.quantity = item.quantity;
        it.createTime = item.createTime;
        for (Item.ItemOption io : item.itemOptions) {
            it.itemOptions.add(new Item.ItemOption(io));
        }
        return it;
    }

    public Item createNewItem(short tempId) {
        return createNewItem(tempId, 1);
    }

    public Item createNewItem(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();

        if (tempId == 191) {
            item.itemOptions.add(new ItemOption(104, 30));
            item.itemOptions.add(new ItemOption(30, 0));
        } else if (tempId == 192) {
            item.itemOptions.add(new ItemOption(96, 30));
            item.itemOptions.add(new ItemOption(30, 0));
        } else if (tempId == ConstItem.CHUOI) {
            item.itemOptions.add(new ItemOption(83, 20));
            item.itemOptions.add(new ItemOption(30, 0));
        } else if (item.template != null && item.template.type == 33) {
            item.itemOptions.add(new ItemOption(87, 0)); // Ký gửi ngọc
            item.itemOptions.add(new ItemOption(30, 0)); // Không thể giao dịch
        }

        if (item.template != null) {
            item.content = item.getContent();
            item.info = item.getInfo();
        } else {
            item.content = "";
            item.info = "";
        }
        return item;
    }

    public Item createNewItemWithBaseOption(short tempId, int quantity) {
        Item item = createNewItem(tempId, quantity);
        if (item.template != null && item.template.type >= 0 && item.template.type <= 4) {
            RewardService.gI().initBaseOptionClothes(tempId, item.template.type, item.itemOptions);
        }
        return item;
    }

    // public Item createNewItemLock(int tempId) {
    // return createNewItemLock(tempId, 1);
    // }

    public Item createNewItemLock(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        item.itemOptions.add(new ItemOption(30, 1));
        return item;
    }

    // public Item createNewItemPhieuTangNgoc(int tempId, int quantity) {
    // Item item = new Item();
    // item.template = getTemplate(tempId);
    // item.quantity = 1;
    // item.createTime = System.currentTimeMillis();
    // item.content = item.getContent();
    // item.info = item.getInfo();
    // item.itemOptions.add(new ItemOption(31, quantity));
    // item.itemOptions.add(new ItemOption(30, 1));
    // return item;
    // }
    //
    // public Item otpts(short tempId, int quantity) {
    // Item item = new Item();
    // item.template = getTemplate(tempId);
    // item.quantity = quantity;
    // item.createTime = System.currentTimeMillis();
    // if (item.template.type == 0) {
    // item.itemOptions.add(new ItemOption(21, 80));
    // item.itemOptions.add(new ItemOption(47, Util.nextInt(2000, 2500)));
    // }
    // if (item.template.type == 1) {
    // item.itemOptions.add(new ItemOption(21, 80));
    // item.itemOptions.add(new ItemOption(22, Util.nextInt(150, 200)));
    // }
    // if (item.template.type == 2) {
    // item.itemOptions.add(new ItemOption(21, 80));
    // item.itemOptions.add(new ItemOption(0, Util.nextInt(18000, 20000)));
    // }
    // if (item.template.type == 3) {
    // item.itemOptions.add(new ItemOption(21, 80));
    // item.itemOptions.add(new ItemOption(23, Util.nextInt(150, 200)));
    // }
    // if (item.template.type == 4) {
    // item.itemOptions.add(new ItemOption(21, 80));
    // item.itemOptions.add(new ItemOption(14, Util.nextInt(20, 25)));
    // }
    // item.content = item.getContent();
    // item.info = item.getInfo();
    // return item;
    // }

    public Item createItemSetKichHoat(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.itemOptions = createItemNull().itemOptions;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemFromItemMap(ItemMap itemMap) {
        Item item = createNewItem(itemMap.itemTemplate.id, itemMap.quantity);
        if (itemMap.options != null && !itemMap.options.isEmpty()) {
            item.itemOptions.clear();
            for (Item.ItemOption io : itemMap.options) {
                item.itemOptions.add(new Item.ItemOption(io));
            }
        }
        normalizeVeggiesOptions(item);
        return item;
    }

    public void normalizeVeggiesOptions(Item item) {
        if (item == null || item.template == null) {
            return;
        }
        if (item.template.id == 191) {
            item.itemOptions.clear();
            item.itemOptions.add(new ItemOption(104, 30));
            item.itemOptions.add(new ItemOption(30, 0));
        } else if (item.template.id == 192) {
            item.itemOptions.clear();
            item.itemOptions.add(new ItemOption(96, 30));
            item.itemOptions.add(new ItemOption(30, 0));
        } else if (item.template.id == ConstItem.CHUOI) {
            item.itemOptions.clear();
            item.itemOptions.add(new ItemOption(83, 20));
            item.itemOptions.add(new ItemOption(30, 0));
        } else if (item.template.type == 33) {
            if (!item.isHaveOption(87)) {
                item.itemOptions.add(new ItemOption(87, 0)); // Ký gửi ngọc
            }
            if (!item.isHaveOption(30)) {
                item.itemOptions.add(new ItemOption(30, 0)); // Không thể giao dịch
            }
        }
    }

    public ItemOptionTemplate getItemOptionTemplate(int id) {
        if (id >= 0 && id < Manager.ITEM_OPTION_TEMPLATES.size()) {
            return Manager.ITEM_OPTION_TEMPLATES.get(id);
        }
        return null;
    }

    public Template.ItemTemplate getTemplate(int id) {
        if (id >= 0 && id < Manager.ITEM_TEMPLATES.size()) {
            return Manager.ITEM_TEMPLATES.get(id);
        }
        return null;
    }

    public int getPercentTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                    return 10;
                case 530:
                case 535:
                    return 20;
                case 531:
                case 536:
                    return 30;
                case 1134:
                    return 40;
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    public boolean isTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                case 530:
                case 535:
                case 531:
                case 536:
                case 1134:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public boolean isOutOfDateTime(Item item) {
        if (item != null) {
            for (Item.ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == 93) {
                    int dayPass = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
                    if (dayPass != 0) {
                        io.param -= dayPass;
                        if (io.param <= 0) {
                            return true;
                        } else {
                            item.createTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        }
        return false;
    }

    public void OpenCapsuleCaiTrang(Player player, Item itemUse) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        Item item = ItemService.gI().createNewItem((short) Util.nextInt(423, 433));
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 15)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 15)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 15)));
        item.itemOptions.add(new Item.ItemOption(93, itemUse.template.id == 1157 ? 5 : 7));
        CombineService.gI().sendEffectOpenItem(player, itemUse.template.iconID, item.template.iconID);
        Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
        InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
    }

    public void OpenItem736(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 50;
            int ruby = 20;
            int dbv = 10;
            int vb = 10;
            int bh = 5;
            int ct = 5;
            Item item = randomRac();
            if (rd <= rac) {
                item = randomRac();
            } else if (rd <= rac + ruby) {
                item = createItemSetKichHoat(861, 1);
            } else if (rd <= rac + ruby + dbv) {
                item = daBaoVe();
            } else if (rd <= rac + ruby + dbv + vb) {
                item = vanBay2011(true);
            } else if (rd <= rac + ruby + dbv + vb + bh) {
                item = phuKien2011(true);
            } else if (rd <= rac + ruby + dbv + vb + bh + ct) {
                item = caitrang2011(true);
            }
            if (item.template.id == 861) {
                item.quantity = Util.nextInt(1, 10);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            player.inventory.event++;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem648 error", e);
        }
    }

    public void OpenItem648(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 50;
            int ruby = 20;
            int dbv = 10;
            int vb = 10;
            int bh = 5;
            int ct = 5;
            Item item = randomRac();
            if (rd <= rac) {
                item = randomRac2();
            } else if (rd <= rac + ruby) {
                item = createItemSetKichHoat(77, 1);
            } else if (rd <= rac + ruby + dbv) {
                item = vatphamsk(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = vanBayChrimas(true);
            } else if (rd <= rac + ruby + dbv + vb + bh) {
                item = phuKienChristmas(true);
            } else if (rd <= rac + ruby + dbv + vb + bh + ct) {
                item = caitrangChristmas(true);
            }
            if (item.template.id == 77) {
                item.quantity = Util.nextInt(10, 200);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            player.inventory.event++;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1143 error", e);
        }
    }

    public void OpenItem1143(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int vp1 = 20;
            int vp2 = 20;
            int vp3 = 20;
            int vp4 = 20;
            int vp5 = 20;
            Item item = randomRac();
            if (rd <= vp1) {
                item = luoihaihong(true);
            } else if (rd <= vp1 + vp2) {
                item = khunglong(true);
            } else if (rd <= vp1 + vp2 + vp3) {
                item = cachepxuong(true);
            } else if (rd <= vp1 + vp2 + vp3 + vp4) {
                item = vanbayxuong(true);
            } else if (rd <= vp1 + vp2 + vp3 + vp4 + vp5) {
                item = petxuong(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1144 error", e);
        }
    }

    public void OpenItem1198(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int vp1 = 20;
            int vp2 = 20;
            int vp3 = 20;
            int vp4 = 20;
            int vp5 = 20;
            Item item = ItemService.gI().createNewItem((short) 16);
            if (rd <= vp1) {
                item = cadic(true);
            } else if (rd <= vp1 + vp2) {
                item = cadicssj2(true);
            } else if (rd <= vp1 + vp2 + vp3) {
                item = cadicssj2(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1145 error", e);
        }
    }

    public void OpenItem1199(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int vp1 = 20;
            int vp2 = 20;
            int vp3 = 20;
            int vp4 = 20;
            int vp5 = 20;
            Item item = ItemService.gI().createNewItem((short) 16);
            if (rd <= vp1) {
                item = cadicssj2m(true);
            } else if (rd <= vp1 + vp2) {
                item = cadicssj3(true);
            } else if (rd <= vp1 + vp2 + vp3) {
                item = cadicblue(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1146 error", e);
        }
    }

    public void OpenItem1200(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int vp1 = 20;
            int vp2 = 20;
            int vp3 = 20;
            int vp4 = 20;
            int vp5 = 20;
            Item item = ItemService.gI().createNewItem((short) 16);
            if (rd <= vp1) {
                item = gokussj3(true);
            } else if (rd <= vp1 + vp2) {
                item = gokugod(true);
            } else if (rd <= vp1 + vp2 + vp3) {
                item = gokublue(true);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            Logger.error(player, "OpenItem1147 error", e);
        }
    }

    public Item cadic(boolean rating) {
        Item item = createItemSetKichHoat(1192, 1);
        item.itemOptions.add(new Item.ItemOption(50, 19));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 19));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 19));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    // public Item cadicssj(boolean rating) {
    // Item item = createItemSetKichHoat(1193, 1);
    // item.itemOptions.add(new Item.ItemOption(50, 22));//VIP
    // item.itemOptions.add(new Item.ItemOption(77, 22));//hp 28%
    // item.itemOptions.add(new Item.ItemOption(103, 22));//ki 25%
    // item.itemOptions.add(new Item.ItemOption(106, 0));//k chết rét
    // item.itemOptions.add(new Item.ItemOption(5, 22));//stcm 22%
    // if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
    // item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) +
    // 7));//hsd
    // }
    // return item;
    // }

    public Item cadicssj2(boolean rating) {
        Item item = createItemSetKichHoat(1194, 1);
        item.itemOptions.add(new Item.ItemOption(50, 24));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 24));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 24));// ki 25%
        item.itemOptions.add(new Item.ItemOption(5, 24));// stcm 22%
        item.itemOptions.add(new Item.ItemOption(106, 0));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    public Item cadicssj2m(boolean rating) {
        Item item = createItemSetKichHoat(1195, 1);
        item.itemOptions.add(new Item.ItemOption(50, 24));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 24));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 24));// ki 25%
        item.itemOptions.add(new Item.ItemOption(5, 24));// stcm 22%
        item.itemOptions.add(new Item.ItemOption(106, 0));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    public Item gokussj3(boolean rating) {
        Item item = createItemSetKichHoat(1203, 1);
        item.itemOptions.add(new Item.ItemOption(50, 24));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 24));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 24));// ki 25%
        item.itemOptions.add(new Item.ItemOption(5, 24));// stcm 22%
        item.itemOptions.add(new Item.ItemOption(106, 0));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    public Item cadicssj3(boolean rating) {
        Item item = createItemSetKichHoat(1196, 1);
        item.itemOptions.add(new Item.ItemOption(50, 24));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 24));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 24));// ki 25%
        item.itemOptions.add(new Item.ItemOption(106, 0));// ki 25%
        item.itemOptions.add(new Item.ItemOption(5, 24));// stcm 22%
        item.itemOptions.add(new Item.ItemOption(19, 20));// ki 25%
        item.itemOptions.add(new Item.ItemOption(14, Util.nextInt(1, 10)));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    public Item gokugod(boolean rating) {
        Item item = createItemSetKichHoat(1204, 1);
        item.itemOptions.add(new Item.ItemOption(50, 24));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 24));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 24));// ki 25%
        item.itemOptions.add(new Item.ItemOption(106, 0));// ki 25%
        item.itemOptions.add(new Item.ItemOption(5, 24));// stcm 22%
        item.itemOptions.add(new Item.ItemOption(19, 20));// ki 25%
        item.itemOptions.add(new Item.ItemOption(14, Util.nextInt(1, 10)));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    public Item cadicblue(boolean rating) {
        Item item = createItemSetKichHoat(1197, 1);
        item.itemOptions.add(new Item.ItemOption(50, 25));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 25));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 25));// ki 25%
        item.itemOptions.add(new Item.ItemOption(106, 0));// ki 25%
        item.itemOptions.add(new Item.ItemOption(5, 25));// stcm 22%
        item.itemOptions.add(new Item.ItemOption(19, 20));// ki 25%
        item.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 10)));// ki 25%
        item.itemOptions.add(new Item.ItemOption(14, Util.nextInt(1, 10)));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    public Item gokublue(boolean rating) {
        Item item = createItemSetKichHoat(1205, 1);
        item.itemOptions.add(new Item.ItemOption(50, 25));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 25));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 25));// ki 25%
        item.itemOptions.add(new Item.ItemOption(106, 0));// ki 25%
        item.itemOptions.add(new Item.ItemOption(5, 25));// stcm 22%
        item.itemOptions.add(new Item.ItemOption(19, 20));// ki 25%
        item.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 10)));// ki 25%
        item.itemOptions.add(new Item.ItemOption(14, Util.nextInt(1, 10)));// ki 25%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 7));// hsd
        }
        return item;
    }

    // Cải trang sự kiện 20/11
    public Item caitrang2011(boolean rating) {
        Item item = createItemSetKichHoat(681, 1);
        item.itemOptions.add(new Item.ItemOption(76, 1));// VIP
        item.itemOptions.add(new Item.ItemOption(77, 24));// hp 28%
        item.itemOptions.add(new Item.ItemOption(103, 24));// ki 25%
        item.itemOptions.add(new Item.ItemOption(50, 24));// sd 26%
        item.itemOptions.add(new Item.ItemOption(108, 20));// sd 26%
        item.itemOptions.add(new Item.ItemOption(106, 0));// sd 26%
        if (Util.isTrue(999, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(5) + 1));// hsd
        }
        return item;
    }

    // Cải trang sự kiện giáng sinh
    public Item caitrangChristmas(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(824, 827), 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 25)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 25)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 25)));
        item.itemOptions.add(new Item.ItemOption(95, Util.nextInt(10, 25)));
        item.itemOptions.add(new Item.ItemOption(96, Util.nextInt(10, 25)));
        item.itemOptions.add(new Item.ItemOption(106, 0));// sd 26%
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item luoihaihong(boolean rating) {
        Item item = createItemSetKichHoat(1124, 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 10)));
        if (Util.isTrue(90, 100) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(15) + 1));// hsd
        }
        return item;
    }

    public Item khunglong(boolean rating) {
        Item item = createItemSetKichHoat(1125, 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(14, Util.nextInt(1, 15)));
        if (Util.isTrue(90, 100) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(10) + 1));// hsd
        }
        return item;
    }

    public Item cachepxuong(boolean rating) {
        Item item = createItemSetKichHoat(1126, 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(12, 16)));
        item.itemOptions.add(new Item.ItemOption(101, Util.nextInt(5, 20)));
        if (Util.isTrue(90, 100) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(7) + 1));// hsd
        }
        return item;
    }

    public Item vanbayxuong(boolean rating) {
        Item item = createItemSetKichHoat(1123, 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 16)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 16)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 16)));
        item.itemOptions.add(new Item.ItemOption(94, Util.nextInt(10, 30)));
        if (Util.isTrue(90, 100) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(7) + 1));// hsd
        }
        return item;
    }

    public Item petxuong(boolean rating) {
        Item item = createItemSetKichHoat(1127, 1);
        item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 15)));
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 15)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 15)));
        item.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 5)));
        if (Util.isTrue(90, 100) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(7) + 1));// hsd
        }
        return item;
    }

    // 610 - bong hoa
    // Phụ kiện bó hoa 20/11
    public Item phuKien2011(boolean rating) {
        Item item = createItemSetKichHoat(954, 1);
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(147, new Random().nextInt(5) + 5));
        if (Util.isTrue(1, 100)) {
            item.itemOptions.get(Util.nextInt(item.itemOptions.size() - 1)).param = 10;
        }
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item phuKienChristmas(boolean rating) {
        Item item = createItemSetKichHoat(823, 1);
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(7) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(7) + 5));
        item.itemOptions.add(new Item.ItemOption(50, new Random().nextInt(7) + 5));
        if (Util.isTrue(1, 100)) {
            item.itemOptions.get(Util.nextInt(item.itemOptions.size() - 1)).param = 10;
        }
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(999, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public Item vanBay2011(boolean rating) {
        Item item = createItemSetKichHoat(795, 1);
        item.itemOptions.add(new Item.ItemOption(89, 1));
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(950, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(7) + 1));// hsd
        }
        return item;
    }

    public Item daBaoVe() {
        Item item = createItemSetKichHoat(987, 1);
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        return item;
    }

    public Item randomRac() {
        short[] racs = { 20, 19, 18, 17 };
        Item item = createItemSetKichHoat(racs[Util.nextInt(racs.length - 1)], 1);
        if (optionRac(item.template.id) != 0) {
            item.itemOptions.add(new Item.ItemOption(optionRac(item.template.id), 1));
        }
        return item;
    }

    public Item randomRac2() {
        short[] racs = { 649, 384, 385, 381, 828, 829, 830, 831, 832, 833, 834, 835, 836, 837, 838, 839, 840, 841,
                842 };
        int idItem = racs[Util.nextInt(racs.length - 1)];
        if (Util.isTrue(1, 100)) {
            idItem = 956;
        }
        Item item = createItemSetKichHoat(idItem, 1);
        if (optionRac(item.template.id) != 0) {
            item.itemOptions.add(new Item.ItemOption(optionRac(item.template.id), 1));
        }
        return item;
    }

    public Item vanBayChrimas(boolean rating) {
        Item item = createItemSetKichHoat(746, 1);
        item.itemOptions.add(new Item.ItemOption(89, 1));
        item.itemOptions.add(new Item.ItemOption(30, 1));// ko the gd
        if (Util.isTrue(950, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));// hsd
        }
        return item;
    }

    public byte optionRac(short itemId) {
        switch (itemId) {
            case 220:
                return 71;
            case 221:
                return 70;
            case 222:
                return 69;
            case 224:
                return 67;
            case 223:
                return 68;
            default:
                return 0;
        }
    }

    public Item vatphamsk(boolean hsd) {
        int[] itemId = { 954, 955, 822, 795, 744 };
        byte[] option = { 77, 80, 81, 103, 50, 94, 5 };
        byte[] option_v2 = { 14, 16, 17, 19, 27, 28, 47, 87 }; // 77 %hp // 80 //81 //103 //50 //94 //5 % sdcm
        byte optionid = 0;
        byte optionid_v2 = 0;
        byte param = 0;
        Item lt = ItemService.gI().createNewItem((short) itemId[Util.nextInt(itemId.length)]);
        lt.itemOptions.clear();
        optionid = option[Util.nextInt(0, 6)];
        param = (byte) Util.nextInt(5, 13);
        lt.itemOptions.add(new Item.ItemOption(optionid, param));
        if (Util.isTrue(1, 100)) {
            optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
            lt.itemOptions.add(new Item.ItemOption(optionid_v2, param));
        }
        if (Util.isTrue(999, 1000) && hsd) {
            lt.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
        }
        lt.itemOptions.add(new Item.ItemOption(30, 0));
        return lt;
    }

    public List<Item.ItemOption> getListOptionItemShop(short id) {
        List<Item.ItemOption> list = new ArrayList<>();
        Manager.SHOPS.forEach(shop -> shop.tabShops.forEach(tabShop -> tabShop.itemShops.forEach(itemShop -> {
            if (itemShop.temp.id == id && list.isEmpty()) {
                for (Item.ItemOption io : itemShop.options) {
                    list.add(new Item.ItemOption(io));
                }
            }
        })));
        return list;
    }

    public int randTempItemKichHoat(int gender) {
        int[][][] items = { { { 0, 33 }, { 1, 41 }, { 2, 49 } }, { { 6, 35 }, { 7, 43 }, { 8, 51 } },
                { { 27, 30 }, { 28, 47 }, { 29, 55 } }, { { 21, 24 }, { 22, 46 }, { 23, 53 } },
                { { 12, 57 }, { 12, 57 }, { 12, 57 } } };
        // a w j g rd
        int type;
        if (Util.isTrue(30, 100)) {
            type = 4; // rada
        } else if (Util.isTrue(30, 100)) {
            type = 3; // gang
        } else if (Util.isTrue(30, 100)) {
            type = 1; // quan
        } else if (Util.isTrue(30, 100)) {
            type = 0; // ao
        } else {
            type = 2; // giay
        }

        return items[type][gender][Util.nextInt(items[type][gender].length)];
    }

    public int[] randOptionItemKichHoat(int gender) {
        int op1;
        int op2;
        switch (gender) {
            case 0 -> {
                if (Util.isTrue(50, 100)) {
                    op1 = 128;
                    op2 = 140;
                } else if (Util.isTrue(50, 100)) {
                    op1 = 127;
                    op2 = 139;
                } else if (Util.isTrue(40, 100)) {
                    op1 = 129;
                    op2 = 141;
                } else {
                    op1 = 246;
                    op2 = 215;
                }
            }
            case 1 -> {
                if (Util.isTrue(60, 100)) {
                    op1 = 130;
                    op2 = 142;
                } else if (Util.isTrue(50, 100)) {
                    op1 = 131;
                    op2 = 143;
                } else {
                    op1 = 132;
                    op2 = 144;
                }
            }
            default -> {
                if (Util.isTrue(60, 100)) {
                    op1 = 134;
                    op2 = 137;
                } else if (Util.isTrue(50, 100)) {
                    op1 = 135;
                    op2 = 138;
                } else {
                    op1 = 133;
                    op2 = 136;
                }
            }
        }
        return new int[] { op1, op2 };
    }

    // public int randDoSao(int gender) {
    // int[][][] items = {
    // {{0, 33}, {1, 41}, {2, 49}},
    // {{6, 35}, {7, 43}, {8, 51}},
    // {{27, 30}, {28, 47}, {29, 55}},
    // {{21, 24}, {22, 46}, {23, 53}},
    // {{12, 57}, {12, 57}, {12, 57}}
    // };
    //
    // int rand = Util.nextInt(100);
    //
    // int type;
    // if (rand < 10) {
    // type = 4; // rada (10%)
    // } else if (rand < 32) {
    // type = 0; // ao (22.5%)
    // } else if (rand < 55) {
    // type = 1; // quan (22.5%)
    // } else if (rand < 77) {
    // type = 2; // giày (22.5%)
    // } else {
    // type = 3; // găng (22.5%)
    // }
    //
    // return items[type][gender][Util.nextInt(2)];
    // }

    protected int getItemLevel(short itemId) {
        // Áo (230-241): level 9,10,11,12,9,10,11,12,9,10,11,12
        if (itemId >= 230 && itemId <= 241) {
            return 9 + (itemId - 230) % 4;
        } // Quần (242-253): level 9,10,11,12,9,10,11,12,9,10,11,12
        else if (itemId >= 242 && itemId <= 253) {
            return 9 + (itemId - 242) % 4;
        } // Găng tay (254-265): level 9,10,11,12,9,10,11,12,9,10,11,12
        else if (itemId >= 254 && itemId <= 265) {
            return 9 + (itemId - 254) % 4;
        } // Giày (266-277): level 9,10,11,12,9,10,11,12,9,10,11,12
        else if (itemId >= 266 && itemId <= 277) {
            return 9 + (itemId - 266) % 4;
        } // Rada (278-281): level 9,10,11,12
        else if (itemId >= 278 && itemId <= 281) {
            return 9 + (itemId - 278);
        }
        return 9; // Default level
    }
}

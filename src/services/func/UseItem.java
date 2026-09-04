package services.func;

import models.item.Item;
import models.player.Player;
import network.io.Message;
import network.session.MySession;
import services.Service;
import services.TaskService;
import services.func.useitem.ItemActionManager;
import services.func.useitem.handlers.CapsuleItemHandler;
import services.func.useitem.handlers.ChestBoxItemHandler;
import services.func.useitem.handlers.ConsumableItemHandler;
import services.func.useitem.handlers.SpecialItemHandler;
import services.player.InventoryService;
import utils.Logger;
import utils.Util;

/**
 * Facade & Controller xử lý tương tác vật phẩm từ Client (Packet Opcode -43).
 * Đã được tái cấu trúc theo Strategy Pattern & Facade Pattern.
 * Logic hành vi vật phẩm được phân rã sang các Handler chuyên trách trong services.func.useitem.handlers.*.
 */
public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;

    private static UseItem instance;

    private UseItem() {
    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    /**
     * Xử lý chuyển đổi vật phẩm giữa Hành trang (Bag), Rương đồ (Box), Thân (Body) và Đệ tử.
     */
    public void getItem(MySession session, Message msg) {
        Player player = session.player;
        if (player == null || player.inventory == null) {
            return;
        }
        if (System.currentTimeMillis() - player.inventory.lastTimeGetItem < 150) {
            return;
        }
        player.inventory.lastTimeGetItem = System.currentTimeMillis();
        TransactionService.gI().cancelTrade(player);
        synchronized (player) {
            try {
                int type = msg.reader().readByte();
                int index = msg.reader().readByte();
                if (index == -1) {
                    return;
                }
                switch (type) {
                    case ITEM_BOX_TO_BODY_OR_BAG -> {
                        InventoryService.gI().itemBoxToBodyOrBag(player, index);
                        TaskService.gI().checkDoneTaskGetItemBox(player);
                    }
                    case ITEM_BAG_TO_BOX -> InventoryService.gI().itemBagToBox(player, index);
                    case ITEM_BODY_TO_BOX -> InventoryService.gI().itemBodyToBox(player, index);
                    case ITEM_BAG_TO_BODY -> InventoryService.gI().itemBagToBody(player, index);
                    case ITEM_BODY_TO_BAG -> InventoryService.gI().itemBodyToBag(player, index);
                    case ITEM_BAG_TO_PET_BODY -> InventoryService.gI().itemBagToPetBody(player, index);
                    case ITEM_BODY_PET_TO_BAG -> InventoryService.gI().itemPetBodyToBag(player, index);
                }
                if (player.setClothes != null) {
                    player.setClothes.setup();
                }
                if (player.pet != null && player.pet.setClothes != null) {
                    player.pet.setClothes.setup();
                }
                player.setClanMember();
                Service.gI().sendFlagBag(player);
                Service.gI().point(player);
                Service.gI().sendSpeedPlayer(player, -1);
            } catch (Exception e) {
                Logger.logException(UseItem.class, e);
            }
        }
    }

    /**
     * Tìm vật phẩm theo template ID trong hành trang.
     */
    public Item finditem(Player player, int iditem) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return null;
        }
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == iditem) {
                return item;
            }
        }
        return null;
    }

    /**
     * Xử lý gói tin hành động đồ (dùng đồ, vứt đồ, xác nhận vứt/dùng) từ Client.
     */
    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg = null;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM -> {
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index < 0 || index >= player.inventory.itemsBag.size()) {
                                return;
                            }
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học "
                                            + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else if (item.template.id == 570) {
                                    if (player.getSession().isAdmin) {
                                        openWoodChest(player, item);
                                        return;
                                    }
                                    if (!Util.isAfterMidnight(player.lastTimeRewardWoodChest)) {
                                        Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn mở\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else if (item.template.type == 22) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn dùng\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else {
                                    useItem(player, item, index);
                                }
                            }
                        } else {
                            int iditem = _msg.reader().readShort();
                            Item item = finditem(player, iditem);
                            useItem(player, item, index);
                        }
                    }
                }
                case DO_THROW_ITEM -> {
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (index < 0) {
                            return;
                        }
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }
                        if (item.isNotNullItem() && item.template.id == 570) {
                            Service.gI().sendThongBao(player, "Không thể bỏ vật phẩm này.");
                            return;
                        }
                        if (!item.isNotNullItem()) {
                            return;
                        }
                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                case ACCEPT_THROW_ITEM -> {
                    InventoryService.gI().throwItem(player, where, index);
                    Service.gI().point(player);
                    InventoryService.gI().sendItemBags(player);
                }
                case ACCEPT_USE_ITEM -> {
                    if (index >= 0 && index < player.inventory.itemsBag.size()) {
                        useItem(player, player.inventory.itemsBag.get(index), index);
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Dispatcher sử dụng vật phẩm - ủy thác xử lý cho ItemActionManager.
     */
    public void useItem(Player pl, Item item, int indexBag) {
        if (item == null || !item.isNotNullItem()) {
            return;
        }
        if (item.template.id == 570) {
            if (!Util.isAfterMidnight(pl.lastTimeRewardWoodChest)) {
                Service.gI().sendThongBao(pl, "Hãy chờ đến ngày mai");
            } else {
                openWoodChest(pl, item);
            }
            return;
        }

        if (item.template.strRequire <= pl.nPoint.power) {
            boolean handled = ItemActionManager.gI().dispatch(pl, item, indexBag);
            if (!handled) {
                Service.gI().sendThongBao(pl, "Vật phẩm này chưa có công dụng.");
            }
        } else {
            Service.gI().sendThongBao(pl, "Sức mạnh không đủ để sử dụng");
        }
    }

    // =========================================================================
    // FACADE DELEGATORS - Đảm bảo 100% tương thích ngược với các class khác
    // =========================================================================

    public void choseMapCapsule(Player pl, int index) {
        CapsuleItemHandler.choseMapCapsule(pl, index);
    }

    public void eatPea(Player player) {
        ConsumableItemHandler.eatPea(player);
    }

    public void changePet(Player player, Item item) {
        SpecialItemHandler.changePet(player, item);
    }

    public void usePorata(Player pl) {
        SpecialItemHandler.usePorata(pl);
    }

    public void usePorata2(Player pl) {
        SpecialItemHandler.usePorata2(pl);
    }

    public void usePorata3(Player pl) {
        SpecialItemHandler.usePorata3(pl);
    }

    public void ItemSKH(Player pl, Item item) {
        ChestBoxItemHandler.itemSKH(pl, item);
    }

    public void ItemManhGiay(Player pl, Item item) {
        SpecialItemHandler.itemManhGiay(pl, item);
    }

    public void ItemSieuThanThuy(Player pl, Item item) {
        SpecialItemHandler.itemSieuThanThuy(pl, item);
    }

    public void UseCard(Player pl, Item item) {
        SpecialItemHandler.useCard(pl, item);
    }

    public void openWoodChest(Player pl, Item item) {
        ChestBoxItemHandler.openWoodChest(pl, item);
    }

    public void useSatellite(Player pl, Item item) {
        ItemActionManager.gI().getSatelliteHandler().handle(pl, item, -1);
    }
}

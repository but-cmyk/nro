package services.func;
import database.daos.HistoryTransactionDAO;
import models.item.Item;
import models.player.Inventory;
import models.player.Player;
import network.io.Message;
import services.ItemService;
import services.player.PlayerService;
import services.Service;
import services.player.InventoryService;
import utils.Logger;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import services.TaskService;

public class Trade {

    public static final int TIME_TRADE = 180000;
    public static final int QUANLITY_MAX = 500_000_000;

    private Player player1;
    private Player player2;

    private long gold1Before;
    private long gold2Before;
    private List<Item> bag1Before;
    private List<Item> bag2Before;

    private List<Item> itemsBag1;
    private List<Item> itemsBag2;

    private List<Item> itemsTrade1;
    private List<Item> itemsTrade2;
    private int goldTrade1;
    private int goldTrade2;

    public byte accept;
    public boolean player1Locked;
    public boolean player2Locked;
    private boolean player1Accepted;
    private boolean player2Accepted;
    private volatile boolean isDisposed;

    private long lastTimeStart;
    private boolean start;

    public Trade(Player pl1, Player pl2) {
        this.player1 = pl1;
        this.player2 = pl2;
        this.gold1Before = pl1.inventory.gold;
        this.gold2Before = pl2.inventory.gold;
        this.bag1Before = InventoryService.gI().copyItemsBag(player1);
        this.bag2Before = InventoryService.gI().copyItemsBag(player2);
        this.itemsBag1 = InventoryService.gI().copyItemsBag(player1);
        this.itemsBag2 = InventoryService.gI().copyItemsBag(player2);
        this.itemsTrade1 = new ArrayList<>();
        this.itemsTrade2 = new ArrayList<>();
        TransactionService.PLAYER_TRADE.put(pl1, this);
        TransactionService.PLAYER_TRADE.put(pl2, this);
    }

    public synchronized void openTabTrade() {
        if (this.isDisposed || player1 == null || player2 == null) {
            return;
        }
        player1.idMark.setAcpTrade(true);
        player2.idMark.setAcpTrade(true);
        player1.isTrade = true;
        player2.isTrade = true;
        this.lastTimeStart = System.currentTimeMillis();
        this.start = true;
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) player1.id);
            player2.sendMessage(msg);
            msg.cleanup();
            msg = new Message(-86);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) player2.id);
            player1.sendMessage(msg);
        } catch (Exception ignored) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public synchronized void addItemTrade(Player pl, byte index, int quantity) {
        if (this.isDisposed || pl == null) {
            return;
        }
        if (player1Locked || player2Locked || player1Accepted || player2Accepted) {
            Service.gI().sendThongBaoFromAdmin(pl, "|7|Không thể thay đổi nội dung sau khi đã khóa hoặc xác nhận giao dịch!");
            return;
        }
        if (this.player1.getSession() == null || !this.player1.getSession().actived
                || this.player2.getSession() == null || !this.player2.getSession().actived) {
            Service.gI().sendThongBaoFromAdmin(pl,
                    "|7|Bạn chưa mở thành viên hoặc người kia chưa mở");
            removeItemTrade(pl, index);
            return;
        }
        if (index == -1) {
            if (quantity < 0 || pl.inventory == null || pl.inventory.gold < quantity) {
                Service.gI().sendThongBaoFromAdmin(pl, "|7|Số vàng giao dịch không hợp lệ!");
                return;
            }
            if (pl.equals(this.player1)) {
                goldTrade1 = quantity;
            } else {
                goldTrade2 = quantity;
            }
            return;
        }

        Item item;
        if (pl.equals(this.player1)) {
            item = itemsBag1.get(index);
        } else {
            item = itemsBag2.get(index);
        }

        if (item == null || quantity > item.quantity || quantity <= 0) {
            Service.gI().sendThongBaoFromAdmin(pl, "|7|Số lượng hoặc vật phẩm không hợp lệ!");
            return;
        }

        if (isItemCannotTran(item)) {
            Service.gI().sendThongBaoFromAdmin(pl, "|7|Vật phẩm này không thể giao dịch!");
            removeItemTrade(pl, index);
            return;
        }

        if (quantity > 99999) {
            int n = quantity / 99999;
            int left = quantity % 99999;

            for (int i = 0; i < n; i++) {
                Item itemTrade = ItemService.gI().copyItem(item);
                itemTrade.quantity = 99999;
                if (pl.equals(this.player1)) {
                    InventoryService.gI().subQuantityItem(itemsBag1, item, itemTrade.quantity);
                    itemsTrade1.add(itemTrade);
                } else {
                    InventoryService.gI().subQuantityItem(itemsBag2, item, itemTrade.quantity);
                    itemsTrade2.add(itemTrade);
                }
            }

            if (left > 0) {
                Item itemTrade = ItemService.gI().copyItem(item);
                itemTrade.quantity = left;
                if (pl.equals(this.player1)) {
                    InventoryService.gI().subQuantityItem(itemsBag1, item, itemTrade.quantity);
                    itemsTrade1.add(itemTrade);
                } else {
                    InventoryService.gI().subQuantityItem(itemsBag2, item, itemTrade.quantity);
                    itemsTrade2.add(itemTrade);
                }
            }
        } else {
            Item itemTrade = ItemService.gI().copyItem(item);
            itemTrade.quantity = quantity;
            if (pl.equals(this.player1)) {
                InventoryService.gI().subQuantityItem(itemsBag1, item, itemTrade.quantity);
                itemsTrade1.add(itemTrade);
            } else {
                InventoryService.gI().subQuantityItem(itemsBag2, item, itemTrade.quantity);
                itemsTrade2.add(itemTrade);
            }
        }
    }

    private synchronized void removeItemTrade(Player pl, byte index) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(2);
            msg.writer().write(index);
            pl.sendMessage(msg);
            Service.gI().sendThongBao(pl, "Không thể giao dịch vật phẩm này");
        } catch (Exception ignored) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private boolean isItemCannotTran(Item item) {
        if (item == null) {
            return true;
        }
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 30) {
                return true;
            }
        }
        switch (item.template.id) {
            case 454:
            case 579:
            case 921:
                return true;
        }
        switch (item.template.type) {
            case 27: //
                return item.template.id == 590;
            case 5: //cải trang
            case 6: //đậu thần
            case 7: //sách skill
            case 8: //vật phẩm nhiệm vụ
            case 11: //flag bag
            case 13: //bùa
            case 22: //vệ tinh
            case 23: //ván bay
            case 24: //ván bay vip
            case 28: //cờ
            case 21: //cờ
            case 31: //bánh trung thu, bánh tết
            case 32: //giáp tập luyện
            case 36: //danh hiệu   
                return true;
            default:
                return false;
        }
    }

    public synchronized void cancelTrade() {
        if (this.isDisposed) {
            return;
        }
        String notifiText = "Giao dịch bị hủy bỏ";
        if (player1 != null) {
            Service.gI().sendThongBao(player1, notifiText);
        }
        if (player2 != null) {
            Service.gI().sendThongBao(player2, notifiText);
        }
        closeTab();
        dispose();
    }

    private void closeTab() {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(7);
            if (player1 != null) {
                player1.sendMessage(msg);
            }
            if (player2 != null) {
                player2.sendMessage(msg);
            }
        } catch (Exception ignored) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public synchronized void dispose() {
        if (this.isDisposed) {
            return;
        }
        this.isDisposed = true;
        this.start = false;
        if (player1 != null) {
            player1.isTrade = false;
            if (player1.idMark != null) {
                player1.idMark.setPlayerTradeId(-1);
                player1.idMark.setAcpTrade(false);
            }
            TransactionService.PLAYER_TRADE.remove(player1);
        }
        if (player2 != null) {
            player2.isTrade = false;
            if (player2.idMark != null) {
                player2.idMark.setPlayerTradeId(-1);
                player2.idMark.setAcpTrade(false);
            }
            TransactionService.PLAYER_TRADE.remove(player2);
        }
        this.player1 = null;
        this.player2 = null;
        this.itemsBag1 = null;
        this.itemsBag2 = null;
        this.itemsTrade1 = null;
        this.itemsTrade2 = null;
    }

    public synchronized void lockTran(Player pl) {
        if (this.isDisposed || pl == null) {
            return;
        }
        if (pl.equals(player1)) {
            if (player1Locked) {
                return;
            }
            player1Locked = true;
        } else if (pl.equals(player2)) {
            if (player2Locked) {
                return;
            }
            player2Locked = true;
        } else {
            return;
        }

        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(6);
            if (pl.equals(player1)) {
                msg.writer().writeInt(goldTrade1);
                msg.writer().writeByte(itemsTrade1.size());
                for (Item item : itemsTrade1) {
                    msg.writer().writeShort(item.template.id);
                    if (player1.getSession() != null && player1.getSession().version < 222) {
                        msg.writer().writeByte(item.quantity > Byte.MAX_VALUE ? Byte.MAX_VALUE : item.quantity);
                    } else {
                        msg.writer().writeInt(item.quantity);
                    }
                    msg.writer().writeByte(item.itemOptions.size());
                    for (Item.ItemOption io : item.itemOptions) {
                        msg.writer().writeByte(io.optionTemplate.id);
                        msg.writer().writeShort(io.param);
                    }
                }
                if (player2 != null) {
                    player2.sendMessage(msg);
                }
            } else {
                msg.writer().writeInt(goldTrade2);
                msg.writer().writeByte(itemsTrade2.size());
                for (Item item : itemsTrade2) {
                    msg.writer().writeShort(item.template.id);
                    if (player2.getSession() != null && player2.getSession().version < 222) {
                        msg.writer().writeByte(item.quantity > Byte.MAX_VALUE ? Byte.MAX_VALUE : item.quantity);
                    } else {
                        msg.writer().writeInt(item.quantity);
                    }
                    msg.writer().writeByte(item.itemOptions.size());
                    for (Item.ItemOption io : item.itemOptions) {
                        msg.writer().writeByte(io.optionTemplate.id);
                        msg.writer().writeShort(io.param);
                    }
                }
                if (player1 != null) {
                    player1.sendMessage(msg);
                }
            }
        } catch (Exception e) {
            Logger.logException(Trade.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public synchronized void acceptTrade(Player pl) {
        if (this.isDisposed || pl == null || (pl != player1 && pl != player2)) {
            return;
        }
        if (!player1Locked || !player2Locked) {
            Service.gI().sendThongBao(pl, "Cả hai bên phải khóa giao dịch trước khi xác nhận!");
            return;
        }
        if (pl.equals(player1)) {
            if (player1Accepted) {
                return;
            }
            player1Accepted = true;
        } else {
            if (player2Accepted) {
                return;
            }
            player2Accepted = true;
        }
        this.accept = (byte) ((player1Accepted ? 1 : 0) + (player2Accepted ? 1 : 0));
        if (player1Accepted && player2Accepted) {
            this.startTrade();
        }
    }

    private void startTrade() {
        if (this.isDisposed || player1 == null || player2 == null) {
            return;
        }
        Player firstLock = player1.id < player2.id ? player1 : player2;
        Player secondLock = player1.id < player2.id ? player2 : player1;

        synchronized (firstLock) {
            synchronized (secondLock) {
                byte tradeStatus = SUCCESS;
                try {
                    if (player1.isDie() || player2.isDie() || player1.getSession() == null || player2.getSession() == null) {
                        tradeStatus = FAIL_ERROR;
                    } else if (goldTrade1 < 0 || player1.inventory.gold < goldTrade1) {
                        tradeStatus = FAIL_NOT_ENOUGH_GOLD_PLAYER1;
                    } else if (goldTrade2 < 0 || player2.inventory.gold < goldTrade2) {
                        tradeStatus = FAIL_NOT_ENOUGH_GOLD_PLAYER2;
                    } else if (player1.inventory.gold > player1.inventory.getGoldLimit() - goldTrade2) {
                        tradeStatus = FAIL_MAX_GOLD_PLAYER1;
                    } else if (player2.inventory.gold > player2.inventory.getGoldLimit() - goldTrade1) {
                        tradeStatus = FAIL_MAX_GOLD_PLAYER2;
                    }

                    if (tradeStatus != SUCCESS) {
                        sendNotifyTrade(tradeStatus);
                        return;
                    }

                    for (Item item : itemsTrade1) {
                        if (!InventoryService.gI().addItemList(itemsBag2, item)) {
                            tradeStatus = FAIL_NOT_ENOUGH_BAG_P1;
                            break;
                        }
                    }

                    if (tradeStatus != SUCCESS) {
                        sendNotifyTrade(tradeStatus);
                        return;
                    }

                    for (Item item : itemsTrade2) {
                        if (!InventoryService.gI().addItemList(itemsBag1, item)) {
                            tradeStatus = FAIL_NOT_ENOUGH_BAG_P2;
                            break;
                        }
                    }

                    if (tradeStatus != SUCCESS) {
                        sendNotifyTrade(tradeStatus);
                        return;
                    }

                    player1.inventory.subGold(goldTrade1);
                    player1.inventory.addGold(goldTrade2);
                    player2.inventory.subGold(goldTrade2);
                    player2.inventory.addGold(goldTrade1);
                    player1.inventory.itemsBag = itemsBag1;
                    player2.inventory.itemsBag = itemsBag2;

                    InventoryService.gI().sendItemBags(player1);
                    InventoryService.gI().sendItemBags(player2);
                    PlayerService.gI().sendInfoHpMpMoney(player1);
                    PlayerService.gI().sendInfoHpMpMoney(player2);

                    HistoryTransactionDAO.insert(player1, player2, goldTrade1, goldTrade2, itemsTrade1, itemsTrade2,
                            bag1Before, bag2Before, this.player1.inventory.itemsBag, this.player2.inventory.itemsBag,
                            gold1Before, gold2Before, this.player1.inventory.gold, this.player2.inventory.gold);

                    database.daos.PlayerDAO.updatePlayerAsync(player1);
                    database.daos.PlayerDAO.updatePlayerAsync(player2);

                    sendNotifyTrade(SUCCESS);
                } catch (Exception e) {
                    Logger.logException(Trade.class, e);
                    sendNotifyTrade(FAIL_ERROR);
                } finally {
                    closeTab();
                    dispose();
                }
            }
        }
    }

    private static final byte SUCCESS = 0;
    private static final byte FAIL_MAX_GOLD_PLAYER1 = 1;
    private static final byte FAIL_MAX_GOLD_PLAYER2 = 2;
    private static final byte FAIL_NOT_ENOUGH_BAG_P1 = 3;
    private static final byte FAIL_NOT_ENOUGH_BAG_P2 = 4;
    private static final byte FAIL_NOT_ENOUGH_GOLD_PLAYER1 = 5;
    private static final byte FAIL_NOT_ENOUGH_GOLD_PLAYER2 = 6;
    private static final byte FAIL_ERROR = 7;

    private void sendNotifyTrade(byte status) {
        if (player1 != null && player1.idMark != null) {
            player1.idMark.setLastTimeTrade(System.currentTimeMillis());
        }
        if (player2 != null && player2.idMark != null) {
            player2.idMark.setLastTimeTrade(System.currentTimeMillis());
        }
        switch (status) {
            case SUCCESS:
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thành công");
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thành công");
                break;
            case FAIL_MAX_GOLD_PLAYER1:
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thất bại do số lượng vàng sau giao dịch vượt tối đa");
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thất bại do số lượng vàng " + (player1 != null ? player1.name : "") + " sau giao dịch vượt tối đa");
                break;
            case FAIL_MAX_GOLD_PLAYER2:
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thất bại do số lượng vàng sau giao dịch vượt tối đa");
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thất bại do số lượng vàng " + (player2 != null ? player2.name : "") + " sau giao dịch vượt tối đa");
                break;
            case FAIL_NOT_ENOUGH_BAG_P1:
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thất bại vì không đủ chỗ chứa hành trang");
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thất bại vì đối phương không đủ chỗ chứa hành trang");
                break;
            case FAIL_NOT_ENOUGH_BAG_P2:
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thất bại vì đối phương không đủ chỗ chứa hành trang");
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thất bại vì không đủ chỗ chứa hành trang");
                break;
            case FAIL_NOT_ENOUGH_GOLD_PLAYER1:
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thất bại vì bạn không đủ vàng");
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thất bại vì đối phương không đủ vàng");
                break;
            case FAIL_NOT_ENOUGH_GOLD_PLAYER2:
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thất bại vì bạn không đủ vàng");
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thất bại vì đối phương không đủ vàng");
                break;
            case FAIL_ERROR:
                if (player1 != null) Service.gI().sendThongBao(player1, "Giao dịch thất bại do xảy ra lỗi hệ thống");
                if (player2 != null) Service.gI().sendThongBao(player2, "Giao dịch thất bại do xảy ra lỗi hệ thống");
                break;
        }
    }

    public synchronized void update() {
        if (this.start && !this.isDisposed && Util.canDoWithTime(lastTimeStart, TIME_TRADE)) {
            this.cancelTrade();
        }
    }
}

package services.func;
import models.item.Item;
import java.io.IOException;
import java.util.ArrayList;
import models.player.Player;
import network.io.Message;
import services.RewardService;
import services.Service;
import java.util.List;
import services.player.InventoryService;
import services.ItemService;

public class LuckyRound {

    private static final byte MAX_ITEM_IN_BOX = 100;

    public static final byte USING_GEM = 2;
    public static final byte USING_GOLD = 0;
    public static final byte USING_TICKET = 1;

    private static final byte PRICE_GEM = 4;
    private static final int PRICE_GOLD = 10000000;
    private static final int PRICE_TICKET = 1;
    private static final int TICKET = 821;

    private static LuckyRound instance;

    public static LuckyRound gI() {
        if (instance == null) {
            instance = new LuckyRound();
        }
        return instance;
    }

    public void openCrackBallUI(Player pl, byte type) {
        pl.idMark.setTypeLuckyRound(type);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(419 + i);
            }
            msg.writer().writeByte(type); //type price
            msg.writer().writeInt(type == USING_GEM ? PRICE_GEM : PRICE_GOLD); //price
            msg.writer().writeShort(-1); //id ticket
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void openCrackBallVipUI(Player pl, byte type) {
        pl.idMark.setTypeLuckyRound(type);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(7390);
            }
            msg.writer().writeByte(type); //type price
            msg.writer().writeInt(PRICE_TICKET); //price
            msg.writer().writeShort(TICKET); //id ticket
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void readOpenBall(Player player, Message msg) {
        if (player == null) {
            return;
        }
        if (player.isDie()) {
            Service.gI().sendThongBao(player, "Bạn đang kiệt sức, không thể thực hiện");
            return;
        }
        if (TransactionService.gI().check(player)) {
            Service.gI().sendThongBao(player, "Không thể thực hiện khi đang giao dịch");
            return;
        }
        try {
            msg.reader().readByte(); // type
            if (msg.reader().available() <= 0) {
                // Client ấn nút Replay/Quay lại (chỉ gửi 1 byte type mà không gửi count)
                reopenUI(player);
                return;
            }
            byte count = msg.reader().readByte();
            if (count <= 0 || count > 7) {
                Service.gI().sendThongBao(player, "Số lượt mở không hợp lệ (tối đa 7 quả)");
                return;
            }
            switch (player.idMark.getTypeLuckyRound()) {
                case USING_GEM -> openBallByGem(player, count);
                case USING_GOLD -> openBallByGold(player, count);
                case USING_TICKET -> openBallByTicket(player, count);
            }
        } catch (Exception e) {
            reopenUI(player);
        }
    }

    public void reopenUI(Player player) {
        if (player == null) {
            return;
        }
        switch (player.idMark.getTypeLuckyRound()) {
            case USING_TICKET -> openCrackBallVipUI(player, player.idMark.getTypeLuckyRound());
            default -> openCrackBallUI(player, player.idMark.getTypeLuckyRound());
        }
    }

    private void openBallByGem(Player player, byte count) {
        int gemNeed = count * PRICE_GEM;
        if (player.inventory.gem < gemNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc để mở");
            return;
        }
        synchronized (player.inventory.itemsBoxCrackBall) {
            if (count + player.inventory.itemsBoxCrackBall.size() > MAX_ITEM_IN_BOX) {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy (tối đa " + MAX_ITEM_IN_BOX + " món)");
                return;
            }
            player.inventory.gem -= gemNeed;
            player.luckySpins += count;
            List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, USING_GEM);
            addItemToBox(player, list);
            sendReward(player, list);
            Service.gI().sendMoney(player);
        }
    }

    private void openBallByGold(Player player, byte count) {
        long goldNeed = (long) count * PRICE_GOLD;
        if (player.inventory.gold < goldNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng để mở");
            return;
        }
        synchronized (player.inventory.itemsBoxCrackBall) {
            if (count + player.inventory.itemsBoxCrackBall.size() > MAX_ITEM_IN_BOX) {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy (tối đa " + MAX_ITEM_IN_BOX + " món)");
                return;
            }
            player.inventory.gold -= (int) goldNeed;
            player.luckySpins += count;
            List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, USING_GOLD);
            addItemToBox(player, list);
            sendReward(player, list);
            Service.gI().sendMoney(player);
        }
    }

    private void openBallByTicket(Player player, byte count) {
        int ticketNeed = count * PRICE_TICKET;
        Item ticket = InventoryService.gI().findItemBag(player, TICKET);
        if (ticket == null || ticket.quantity < ticketNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + ItemService.gI().createNewItem((short) TICKET).template.name + " để quay");
            return;
        }
        synchronized (player.inventory.itemsBoxCrackBall) {
            if (count + player.inventory.itemsBoxCrackBall.size() > MAX_ITEM_IN_BOX) {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy (tối đa " + MAX_ITEM_IN_BOX + " món)");
                return;
            }
            InventoryService.gI().subQuantityItemsBag(player, ticket, ticketNeed);
            InventoryService.gI().sendItemBags(player);
            player.luckySpins += count;
            List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, USING_TICKET);
            addItemToBox(player, list);
            sendReward(player, list);
            Service.gI().sendMoney(player);
        }
    }

    private void sendReward(Player player, List<Item> items) {
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.iconID);
            }
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void addItemToBox(Player player, List<Item> items) {
        synchronized (player.inventory.itemsBoxCrackBall) {
            player.inventory.itemsBoxCrackBall.addAll(items);
        }
    }
    
    
}

package services;

import consts.ConstNpc;
import managers.ConsignShopManager;
import models.ConsignItem;
import models.item.Item;
import models.item.Item.ItemOption;
import models.player.Player;
import network.io.Message;
import services.player.InventoryService;
import services.map.NpcService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import database.AlyraManager;
import database.daos.NDVSqlFetcher; // Unused import
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.Logger;

public class ConsignShopService {

    private static ConsignShopService instance;

    public static ConsignShopService gI() {
        if (instance == null) {
            instance = new ConsignShopService();
        }
        return instance;
    }

    public static class ConsignPage {
        public final int totalPages;
        public final List<ConsignItem> itemsSend;

        public ConsignPage(int totalPages, List<ConsignItem> itemsSend) {
            this.totalPages = totalPages;
            this.itemsSend = itemsSend;
        }
    }

    public ConsignPage getConsignPage(byte tab, int page, int pageSize) {
        List<ConsignItem> filtered = new ArrayList<>();
        List<ConsignItem> snapshot = ConsignShopManager.gI().getItemsSnapshot();
        for (ConsignItem it : snapshot) {
            if (it != null && it.tab == tab && !it.isBuy) {
                filtered.add(it);
            }
        }
        filtered.sort((a, b) -> Byte.compare(b.isUpTop, a.isUpTop));

        int totalItems = filtered.size();
        int totalPages = Math.max(1, (totalItems + pageSize - 1) / pageSize);

        int fromIndex = page * pageSize;
        if (fromIndex >= totalItems) {
            return new ConsignPage(totalPages, Collections.emptyList());
        }
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        return new ConsignPage(totalPages, filtered.subList(fromIndex, toIndex));
    }

    public void buyItem(Player pl, int id) {
        if (pl == null || pl.getSession() == null) {
            return;
        }
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBaoResetWait(pl, "Không thể mua vật phẩm ký gửi khi đang giao dịch!");
            return;
        }
        if (pl.nPoint.power < 17_000_000_000L) {
            Service.gI().sendThongBao(pl, "Yêu cầu sức mạnh lớn hơn 17 tỷ");
            openShopKyGui(pl);
            return;
        }
        ConsignShopManager manager = ConsignShopManager.gI();
        manager.lockItems();
        try {
            ConsignItem it = manager.getItemById(id);
            if (it == null || it.isBuy) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                return;
            }
            if (it.player_sell == pl.id) {
                Service.gI().sendThongBao(pl, "Không thể mua vật phẩm bản thân đăng bán");
                openShopKyGui(pl);
                return;
            }

            Item item = ItemService.gI().createNewItem(it.itemId);
            item.quantity = it.quantity;
            if (it.options != null && !it.options.isEmpty()) {
                item.itemOptions.clear();
                item.itemOptions.addAll(it.options);
            }
            List<Item> previewBag = InventoryService.gI().copyItemsBag(pl);
            if (!InventoryService.gI().addItemList(previewBag, ItemService.gI().copyItem(item))) {
                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                return;
            }

            boolean paid = false;
            if (it.goldSell > 0) {
                if (pl.inventory.gold < it.goldSell) {
                    Service.gI().sendThongBao(pl, "Bạn không đủ vàng để mua vật phẩm");
                    return;
                }
                pl.inventory.subGold(it.goldSell);
                paid = true;
            } else if (it.gemSell > 0) {
                if (pl.inventory.gem < it.gemSell) {
                    Service.gI().sendThongBao(pl, "Bạn không đủ Ngọc Xanh để mua vật phẩm này!");
                    return;
                }
                pl.inventory.subGemDirect(it.gemSell);
                paid = true;
            }

            if (!paid) {
                Service.gI().sendThongBao(pl, "Giá bán không hợp lệ");
                return;
            }

            if (!InventoryService.gI().addItemBag(pl, item)) {
                if (it.goldSell > 0) {
                    pl.inventory.addGold(it.goldSell);
                } else {
                    pl.inventory.addGem(it.gemSell);
                }
                Service.gI().sendThongBao(pl, "Không thể nhận vật phẩm, giao dịch đã được hoàn lại");
                return;
            }

            it.isBuy = true;
            manager.updateItemBoughtAsync(it.id);
            database.daos.PlayerDAO.updatePlayerAsync(pl);
            Player seller = server.Client.gI().getPlayer(it.player_sell);
            if (seller != null) {
                database.daos.PlayerDAO.updatePlayerAsync(seller);
            }
            Service.gI().sendMoney(pl);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Bạn đã nhận được " + item.template.name);
            openShopKyGui(pl);
        } finally {
            manager.unlockItems();
        }
    }

    public ConsignItem getItemBuy(int id) {
        return ConsignShopManager.gI().getItemById(id);
    }

    public ConsignItem getItemBuy(Player pl, int id) {
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.id == id && it.player_sell == pl.id) {
                return it;
            }
        }
        return null;
    }

    public void openShopKyGui(Player pl, byte index, int page) {
        if (pl == null || pl.getSession() == null) {
            return;
        }
        ConsignPage pageResult = getConsignPage(index, page, 20);
        if (page >= pageResult.totalPages && page > 0) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-100);
            msg.writer().writeByte(index);
            msg.writer().writeByte((byte) pageResult.totalPages); // max page
            msg.writer().writeByte(page);
            msg.writer().writeByte(pageResult.itemsSend.size());
            for (int j = 0; j < pageResult.itemsSend.size(); j++) {
                ConsignItem itk = pageResult.itemsSend.get(j);
                if (itk == null)
                    continue;
                msg.writer().writeShort(itk.itemId);
                msg.writer().writeShort(itk.id);
                msg.writer().writeInt(itk.goldSell);
                msg.writer().writeInt(itk.gemSell);
                msg.writer().writeByte(0); // buy type
                if (pl.getSession().version >= 222) {
                    msg.writer().writeInt(itk.quantity);
                } else {
                    msg.writer().writeByte(itk.quantity);
                }
                msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe

                if (itk.options == null || itk.options.isEmpty()) {
                    msg.writer().writeByte(1);
                    msg.writer().writeByte(73);
                    msg.writer().writeShort(0);
                } else {
                    msg.writer().writeByte(itk.options.size());
                    for (int a = 0; a < itk.options.size(); a++) {
                        Item.ItemOption opt = itk.options.get(a);
                        msg.writer().writeByte(opt.optionTemplate != null ? opt.optionTemplate.id : 73);
                        msg.writer().writeShort(opt.param);
                    }
                }
                msg.writer().writeByte(0); // partTemp (b3)
                if (pl.getSession().version >= 237) {
                    msg.writer().writeUTF(itk.playerName != null ? itk.playerName : "");
                }
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void upItemToTop(Player pl, int id) {
        ConsignItem it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
            return;
        }
        if (it.player_sell != pl.id) {
            Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
            openShopKyGui(pl);
            return;
        }
        models.Template.ItemTemplate temp = ItemService.gI().getTemplate(it.itemId);
        String itemName = temp != null ? temp.name : "Vật phẩm";
        NpcService.gI().createMenuConMeo(pl, ConstNpc.UP_TOP_ITEM, -1,
                "Bạn có muốn đưa vật phẩm ['" + itemName + "'] của bản thân lên trang đầu?\nYêu cầu 5 Ngọc Xanh.",
                "Đồng ý", "Từ Chối");
    }

    // public void StartupItemToTop(Player pl) {
    // // Changed "Ngọc Xanh" requirement to "thỏi vàng" based on SubThoiVang
    // if (!SubThoiVang(pl, 2)) {
    // Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 2 thỏi vàng đưa vật phẩm
    // lên trang đầu");
    // return;
    // }
    // for (ConsignItem its : ConsignShopManager.gI().listItem) {
    // if (its.id == pl.idMark.getIdItemUpTop()) {
    // its.isUpTop = 1;
    // Service.gI().sendThongBao(pl, "Đưa vật phẩm lên trang đầu thành công");
    // break;
    // }
    // }
    //
    // ConsignShopManager.gI().save();
    // openShopKyGui(pl);
    // }

    public void claimOrDel(Player pl, byte action, int id) {
        if (pl == null || pl.getSession() == null) {
            return;
        }
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBaoResetWait(pl, "Không thể nhận/hủy vật phẩm ký gửi khi đang giao dịch!");
            return;
        }
        ConsignShopManager manager = ConsignShopManager.gI();
        manager.lockItems();
        try {
            ConsignItem it = getItemBuy(pl, id);
            switch (action) {
                case 1: // hủy vật phẩm
                    if (it == null || it.isBuy) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                        return;
                    }
                    if (it.player_sell != pl.id) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                        openShopKyGui(pl);
                        return;
                    }
                    Item item = ItemService.gI().createNewItem(it.itemId);
                    item.quantity = it.quantity;
                    if (it.options != null && !it.options.isEmpty()) {
                        item.itemOptions.clear();
                        item.itemOptions.addAll(it.options);
                    }
                    List<Item> previewBag = InventoryService.gI().copyItemsBag(pl);
                    if (!InventoryService.gI().addItemList(previewBag, ItemService.gI().copyItem(item))) {
                        Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                        return;
                    }
                    if (manager.listItem.remove(it)) {
                        if (!InventoryService.gI().addItemBag(pl, item)) {
                            manager.listItem.add(it);
                            Service.gI().sendThongBao(pl, "Không thể nhận lại vật phẩm");
                            return;
                        }
                        InventoryService.gI().sendItemBags(pl);
                        Service.gI().sendMoney(pl);
                        manager.deleteItemAsync(it.id);
                        database.daos.PlayerDAO.updatePlayerAsync(pl);
                        Service.gI().sendThongBao(pl, "Hủy bán vật phẩm thành công");
                        openShopKyGui(pl);
                    }
                    break;
                case 2: // nhận tiền
                    if (it == null || !it.isBuy) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc chưa được bán");
                        return;
                    }
                    if (it.player_sell != pl.id) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                        openShopKyGui(pl);
                        return;
                    }

                    // Tính phí 10% và làm tròn
                    long fee = 0;
                    long receivedAmount = 0;

                    if (it.goldSell > 0) {
                        fee = Math.round(it.goldSell * 0.1);
                        receivedAmount = it.goldSell - fee;
                        if (pl.inventory.gold + receivedAmount > pl.inventory.getGoldLimit()) {
                            Service.gI().sendThongBao(pl, "Vàng sau khi nhận sẽ vượt quá giới hạn mang theo");
                            return;
                        }
                    } else if (it.gemSell > 0) {
                        fee = Math.round(it.gemSell * 0.1);
                        receivedAmount = it.gemSell - fee;
                    }

                    if (manager.listItem.remove(it)) {
                        if (it.goldSell > 0) {
                            pl.inventory.addGold(receivedAmount);
                        } else {
                            pl.inventory.addGem((int) receivedAmount);
                        }
                        Service.gI().sendMoney(pl);
                        InventoryService.gI().sendItemBags(pl); // Send updated inventory to player
                        manager.deleteItemAsync(it.id);
                        database.daos.PlayerDAO.updatePlayerAsync(pl);
                        Service.gI().sendThongBao(pl, String.format("Bạn đã nhận được %s (đã trừ %s phí)",
                                utils.Util.powerToString(receivedAmount), utils.Util.powerToString(fee)));
                        openShopKyGui(pl);
                    }
                    break;
            }
        } finally {
            manager.unlockItems();
        }
    }

    public List<ConsignItem> getItemCanKiGui(Player pl) {
        List<ConsignItem> its = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && it.player_sell == pl.id))
                .forEachOrdered((it) -> {
                    its.add(it);
                });
        pl.inventory.itemsBag.stream().filter((it) -> (itemCanConsign(it))).forEachOrdered((it) -> {
            // Note: When adding to this list, the 'id' field of ConsignItem is used as
            // index in bag.
            // This is only for display in the "can consign" tab, not for actual
            // consignment.
            its.add(new ConsignItem(InventoryService.gI().getIndexBag(pl, it), it.template.id, (int) pl.id, (byte) 4,
                    -1, -1, it.quantity, (byte) -1, new ArrayList<>(it.itemOptions), false, pl.name));
        });
        return its;
    }

    public boolean itemCanConsign(Item it) {
        if (it != null && it.template != null) {
            boolean hasConsignOption = it.itemOptions.stream()
                    .anyMatch(op -> op != null && op.optionTemplate != null
                            && (op.optionTemplate.id == 86 || op.optionTemplate.id == 87));
            if (hasConsignOption || it.template.type == 14 || it.template.type == 15) {
                return true;
            }
            // Chặn nếu vật phẩm có Option 30 (Không thể giao dịch) mà không có Option ký gửi
            if (it.isHaveOption(30)) {
                return false;
            }
        }
        return false;
    }

    public int getMaxId() {
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT COALESCE(MAX(id), 0) as max_id FROM shop_ky_gui");
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("max_id");
            }
            return 0;
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error getting max ID from shop_ky_gui");
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public int insertConsignItemAndGetId(ConsignItem item) {
        // Validate dữ liệu
        if (item == null || item.player_sell <= 0 || item.itemId <= 0 || item.quantity <= 0) {
            Logger.log("ConsignShopService", "Invalid item data for insertion");
            return -1;
        }

        String sql = "INSERT INTO `shop_ky_gui`(`player_id`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `isUpTop`, `isBuy`) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Xử lý JSON options an toàn
        String optionsJson = "[]";
        if (item.options != null && !item.options.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (Item.ItemOption option : item.options) {
                    if (option != null && option.optionTemplate != null) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("id", option.optionTemplate.id);
                        jsonObj.put("param", option.param);
                        jsonArray.add(jsonObj);
                    }
                }
                optionsJson = JSONValue.toJSONString(jsonArray);
            } catch (Exception jsonEx) {
                Logger.logException(ConsignShopService.class, jsonEx, "Error creating JSON for item options");
                optionsJson = "[]";
            }
        }

        try (Connection con = AlyraManager.getConnection()) {
            if (con == null) {
                Logger.logException(ConsignShopService.class, new Exception("Database connection is null"),
                        "Failed to get database connection for insertConsignItem");
                return -1;
            }
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, item.player_sell);
                ps.setByte(2, item.tab);
                ps.setInt(3, item.itemId);
                ps.setInt(4, item.goldSell);
                ps.setInt(5, item.gemSell);
                ps.setInt(6, item.quantity);
                ps.setString(7, optionsJson);
                ps.setByte(8, item.isUpTop);
                ps.setBoolean(9, item.isBuy);

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    con.rollback();
                    return -1;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        con.commit();
                        return newId;
                    } else {
                        con.rollback();
                        return -1;
                    }
                }
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error inserting consign item");
            return -1;
        }
    }

    public byte getTabKiGui(Item it) {
        if (it.template.type >= 0 && it.template.type <= 2) {
            return 0;
        } else if ((it.template.type >= 3 && it.template.type <= 4)) {
            return 1;
        } else if (it.template.type == 29) {
            return 2;
        } else {
            return 3;
        }
    }

    public void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        if (pl == null || pl.getSession() == null) {
            return;
        }
        if (pl.isTrade || services.func.TransactionService.gI().check(pl)) {
            Service.gI().sendThongBaoResetWait(pl, "Không thể đăng bán ký gửi khi đang giao dịch!");
            return;
        }
        ConsignShopManager manager = ConsignShopManager.gI();
        boolean feeCharged = false;
        try {
            if (id < 0 || id >= pl.inventory.itemsBag.size()) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại");
                openShopKyGui(pl);
                return;
            }

            if ((moneyType != 0 && moneyType != 1) || money <= 0) {
                Service.gI().sendThongBao(pl, "Giá bán hoặc loại tiền không hợp lệ");
                openShopKyGui(pl);
                return;
            }

            if (moneyType == 0 && (money < 10_000 || money > 2_000_000_000)) {
                Service.gI().sendThongBao(pl, "Giá vàng phải từ 10.000 đến 2.000.000.000 vàng");
                openShopKyGui(pl);
                return;
            }

            if (moneyType == 1 && (money < 1 || money > 100_000)) {
                Service.gI().sendThongBao(pl, "Giá ngọc phải từ 1 đến 100.000 ngọc");
                openShopKyGui(pl);
                return;
            }

            Item it = pl.inventory.itemsBag.get(id);
            if (it == null || !it.isNotNullItem() || it.template == null) {
                Service.gI().sendThongBao(pl, "Vật phẩm không hợp lệ");
                openShopKyGui(pl);
                return;
            }

            // Ensure quantity does not exceed item's actual quantity
            if (quantity <= 0 || quantity > it.quantity) {
                Service.gI().sendThongBao(pl, "Số lượng vật phẩm không hợp lệ.");
                openShopKyGui(pl);
                return;
            }

            // Check if item can be consigned
            if (!itemCanConsign(it)) {
                Service.gI().sendThongBao(pl, "Vật phẩm này không thể ký gửi.");
                openShopKyGui(pl);
                return;
            }

            // Chặn ký gửi vàng nếu chỉ có Option 87 (Ký gửi ngọc)
            if (it.isHaveOption(87) && !it.isHaveOption(86) && moneyType == 0) {
                Service.gI().sendThongBao(pl, "Vật phẩm này chỉ có thể ký gửi bằng ngọc.");
                openShopKyGui(pl);
                return;
            }
            // Chặn ký gửi ngọc nếu chỉ có Option 86 (Ký gửi vàng)
            if (it.isHaveOption(86) && !it.isHaveOption(87) && moneyType == 1) {
                Service.gI().sendThongBao(pl, "Vật phẩm này chỉ có thể ký gửi bằng vàng.");
                openShopKyGui(pl);
                return;
            }

            manager.lockItems();
            boolean lockHeld = true;
            try {
                if (manager.listItem.stream().anyMatch(consignIt -> consignIt.player_sell == pl.id
                        && consignIt.itemId == it.template.id
                        && consignIt.quantity == quantity
                        && consignIt.options.equals(it.itemOptions)
                        && !consignIt.isBuy)) {
                    Service.gI().sendThongBao(pl, "Bạn đã có vật phẩm tương tự đang ký gửi.");
                    openShopKyGui(pl);
                    return;
                }

                // Check maximum consignment items per player (optional, but good practice)
                long consignedCount = manager.listItem.stream()
                        .filter(consignIt -> consignIt.player_sell == pl.id && !consignIt.isBuy).count();
                if (consignedCount >= 20) { // Example limit: 10 items per player
                    Service.gI().sendThongBao(pl, "Bạn chỉ có thể ký gửi tối đa 20 vật phẩm.");
                    openShopKyGui(pl);
                    return;
                }

                long feeGold = 5_000_000L;
                if (pl.inventory.gold < feeGold) {
                    Service.gI().sendThongBao(pl,
                            "Bạn cần có ít nhất " + utils.Util.powerToString(feeGold) + " vàng để làm phí đăng bán");
                    return;
                }
                pl.inventory.subGold(feeGold);
                Service.gI().sendMoney(pl);
                feeCharged = true;

                // Lấy ID nguyên tử và tạo consign item
                int newId = manager.nextItemId();
                ConsignItem newConsignItem = new ConsignItem(
                        newId,
                        it.template.id,
                        (int) pl.id,
                        getTabKiGui(it),
                        moneyType == 0 ? money : -1,
                        moneyType == 1 ? money : -1,
                        quantity,
                        (byte) 0,
                        new ArrayList<>(it.itemOptions), // Create a copy to avoid reference issues
                        false,
                        pl.name);

                manager.addItem(newConsignItem);

                // Subtract item from player's inventory
                InventoryService.gI().subQuantityItemsBag(pl, it, quantity);
                InventoryService.gI().sendItemBags(pl);
                database.daos.PlayerDAO.updatePlayerAsync(pl);

                // GIẢI PHÓNG GLOBAL LOCK NGAY LẬP TỨC để các người chơi khác không bị chặn
                manager.unlockItems();
                lockHeld = false;

                // Đẩy ghi DB bất đồng bộ qua Virtual Thread (không block Netty)
                manager.insertItemAsync(newConsignItem);

                Service.gI().sendThongBao(pl, "Ký gửi vật phẩm thành công!");
                openShopKyGui(pl);
            } finally {
                if (lockHeld) {
                    manager.unlockItems();
                }
            }

        } catch (Exception e) {
            if (feeCharged) {
                pl.inventory.addGold(5_000_000L);
                Service.gI().sendMoney(pl);
            }
            Service.gI().sendThongBao(pl, "Có lỗi xảy ra khi xử lý yêu cầu");
            Logger.logException(ConsignShopService.class, e, "Error in KiGui method");
            openShopKyGui(pl);
        }
    }

    public void openShopKyGui(Player pl) {
        if (pl == null || pl.getSession() == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(2);
            msg.writer().writeByte(5);
            for (byte i = 0; i < 5; i++) {
                if (i == 4) {
                    List<ConsignItem> canKiGui = getItemCanKiGui(pl);
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(canKiGui.size());
                    for (int j = 0; j < canKiGui.size(); j++) {
                        ConsignItem itk = canKiGui.get(j);
                        if (itk == null) {
                            continue;
                        }
                        msg.writer().writeShort(itk.itemId);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.goldSell);
                        msg.writer().writeInt(itk.gemSell);
                        if (getItemBuy(pl, itk.id) == null) {
                            msg.writer().writeByte(0); // buy type
                        } else if (itk.isBuy) {
                            msg.writer().writeByte(2);
                        } else {
                            msg.writer().writeByte(1);
                        }
                        msg.writer().writeInt(itk.quantity);
                        msg.writer().writeByte(1); // isMe
                        if (itk.options == null || itk.options.isEmpty()) {
                            msg.writer().writeByte(1);
                            msg.writer().writeByte(73);
                            msg.writer().writeShort(0);
                        } else {
                            msg.writer().writeByte(itk.options.size());
                            for (int a = 0; a < itk.options.size(); a++) {
                                Item.ItemOption opt = itk.options.get(a);
                                msg.writer().writeByte(opt.optionTemplate != null ? opt.optionTemplate.id : 73);
                                msg.writer().writeShort(opt.param);
                            }
                        }
                        msg.writer().writeByte(0); // newItem (b34)
                        msg.writer().writeByte(0); // partTemp (b35)
                        if (pl.getSession().version >= 237) {
                            msg.writer().writeUTF(pl.name);
                        }
                    }
                } else {
                    ConsignPage pageResult = getConsignPage(i, 0, 20);
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    msg.writer().writeByte((byte) pageResult.totalPages); // max page
                    msg.writer().writeByte(pageResult.itemsSend.size());
                    for (int j = 0; j < pageResult.itemsSend.size(); j++) {
                        ConsignItem itk = pageResult.itemsSend.get(j);
                        if (itk == null)
                            continue;
                        msg.writer().writeShort(itk.itemId);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.goldSell);
                        msg.writer().writeInt(itk.gemSell);
                        msg.writer().writeByte(0); // buy type
                        msg.writer().writeInt(itk.quantity);
                        msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                        if (itk.options == null || itk.options.isEmpty()) {
                            msg.writer().writeByte(1);
                            msg.writer().writeByte(73);
                            msg.writer().writeShort(0);
                        } else {
                            msg.writer().writeByte(itk.options.size());
                            for (int a = 0; a < itk.options.size(); a++) {
                                Item.ItemOption opt = itk.options.get(a);
                                msg.writer().writeByte(opt.optionTemplate != null ? opt.optionTemplate.id : 73);
                                msg.writer().writeShort(opt.param);
                            }
                        }
                        msg.writer().writeByte(0); // newItem (b34)
                        msg.writer().writeByte(0); // partTemp (b35)
                        if (pl.getSession().version >= 237) {
                            msg.writer().writeUTF(itk.playerName != null ? itk.playerName : "");
                        }
                    }
                }
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }
}

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

    private List<ConsignItem> getItemKyGui2(Player pl, byte tab, byte to, byte max) {
        List<ConsignItem> its = new ArrayList<>();
        List<ConsignItem> listSort = new ArrayList<>();
        List<ConsignItem> listSort2 = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && it.tab == tab && !it.isBuy)).forEachOrdered((it) -> {
            its.add(it);
        });
        its.stream().filter(i -> i != null).sorted(Comparator.comparing(i -> i.isUpTop, Comparator.reverseOrder())).forEach(i -> listSort.add(i));
        for (int i = to; i <= max && i < listSort.size(); i++) {
            listSort2.add(listSort.get(i));
        }
        return listSort2;
    }

    private List<ConsignItem> getItemKyGui(Player pl, byte tab, byte... max) {
        List<ConsignItem> its = new ArrayList<>();
        List<ConsignItem> listSort = new ArrayList<>();
        List<ConsignItem> listSort2 = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && it.tab == tab && !it.isBuy && it.player_sell != pl.id)).forEachOrdered((it) -> {
            its.add(it);
        });
        its.stream().filter(i -> i != null).sorted(Comparator.comparing(i -> i.isUpTop, Comparator.reverseOrder())).forEach(i -> listSort.add(i));
        if (max.length == 2) {
            if (listSort.size() > max[1]) {
                for (int i = max[0]; i < max[1]; i++) {
                    if (listSort.get(i) != null) {
                        listSort2.add(listSort.get(i));
                    }
                }
            } else {
                for (int i = max[0]; i <= max[0]; i++) {
                    if (listSort.get(i) != null) {
                        listSort2.add(listSort.get(i));
                    }
                }
            }
            return listSort2;
        }
        if (max.length == 1 && listSort.size() > max[0]) {
            for (int i = 0; i < max[0]; i++) {
                if (listSort.get(i) != null) {
                    listSort2.add(listSort.get(i));
                }
            }
            return listSort2;
        }
        return listSort;
    }

    private List<ConsignItem> getItemKyGui() {
        List<ConsignItem> its = new ArrayList<>();
        List<ConsignItem> listSort = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && !it.isBuy)).forEachOrdered((it) -> {
            its.add(it);
        });
        its.stream().filter(i -> i != null).sorted(Comparator.comparing(i -> i.isUpTop, Comparator.reverseOrder())).forEach(i -> listSort.add(i));
        return listSort;
    }

//    private boolean isKyGui(Item item) {
//        switch (item.template.type) {
//            case 27:
//                switch (item.template.id) {
//                    case 921:
//                    case 1155:
//                    case 1156:
//                    case 568:
//                        return true;
//                }
//                return false;
//            case 21:
//            case 72:
//                return true;
//        }
//        for (int i = 0; i < item.itemOptions.size(); i++) {
//            if (item.itemOptions.get(i).optionTemplate.id == 86) {
//                return true;
//            }
//        }
//        return false;
//    }

    private boolean SubThoiVang(Player pl, int quatity) {
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457 && item.quantity >= quatity) {
                services.player.InventoryService.gI().subQuantityItemsBag(pl, item, quatity);
                return true;
            }
        }
        return false;
    }

    public void buyItem(Player pl, int id) {
        if (pl.nPoint.power < 1700L) {
            Service.gI().sendThongBao(pl, "Yêu cầu sức mạnh lớn hơn 17 tỷ");
            openShopKyGui(pl);
            return;
        }

        ConsignItem it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
            return;
        }
        if (it.player_sell == pl.id) {
            Service.gI().sendThongBao(pl, "Không thể mua vật phẩm bản thân đăng bán");
            openShopKyGui(pl);
            return;
        }

        boolean isBuy = false;
        if (it.goldSell > 0) {
            // Kiểm tra và trừ thỏi vàng
            int requiredGoldBars = it.goldSell;
            int goldBarsCount = InventoryService.gI().countItemBag(pl, (short) 457);

            if (goldBarsCount >= requiredGoldBars) {
                // Trừ thỏi vàng từ inventory
                InventoryService.gI().removeItemQuantity(pl, (short) 457, requiredGoldBars);
                isBuy = true;
            } else {
                Service.gI().sendThongBao(pl, "Bạn không đủ thỏi vàng để mua vật phẩm");
                isBuy = false;
            }
        } else if (it.gemSell > 0) {
            if (pl.inventory.gem >= it.gemSell) {
                pl.inventory.gem -= it.gemSell;
                isBuy = true;
            } else {
                Service.gI().sendThongBao(pl, "Bạn không đủ Ngọc Xanh để mua vật phẩm này!");
                isBuy = false;
            }
        }

        Service.gI().sendMoney(pl);
        if (isBuy) {
            Item item = ItemService.gI().createNewItem(it.itemId);
            item.quantity = it.quantity;
            item.itemOptions.addAll(it.options);
            it.isBuy = true;

            InventoryService.gI().addItemBag(pl, item);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Bạn đã nhận được " + item.template.name);
            ConsignShopManager.gI().save();
            openShopKyGui(pl);
        }
    }

    public ConsignItem getItemBuy(int id) {
        for (ConsignItem it : getItemKyGui()) {
            if (it != null && it.id == id) {
                return it;
            }
        }
        return null;
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
        if (page > getItemKyGui(pl, index).size()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-100);
            msg.writer().writeByte(index);
            List<ConsignItem> items = getItemKyGui(pl, index);
            List<ConsignItem> itemsSend = getItemKyGui2(pl, index, (byte) (page * 20), (byte) (page * 20 + 20));
            byte tab = (byte) (items.size() / 20 > 0 ? (items.size() / 20) + 1 : 1);
            msg.writer().writeByte(tab); // max page
            msg.writer().writeByte(page);
            msg.writer().writeByte(itemsSend.size());
            for (int j = 0; j < itemsSend.size(); j++) {
                ConsignItem itk = itemsSend.get(j);
                Item it = ItemService.gI().createNewItem(itk.itemId);
                it.itemOptions.clear();
                if (itk.options.isEmpty()) {
                    it.itemOptions.add(new ItemOption(73, 0));
                } else {
                    it.itemOptions.addAll(itk.options);
                }
                msg.writer().writeShort(it.template.id);
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
                msg.writer().writeByte(it.itemOptions.size());
                for (int a = 0; a < it.itemOptions.size(); a++) {
                    msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                    msg.writer().writeShort(it.itemOptions.get(a).param);
                }
                msg.writer().writeByte(0);
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
        pl.idMark.setIdItemUpTop(id);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.UP_TOP_ITEM, -1, "Bạn có muốn đưa vật phẩm ['" + ItemService.gI().createNewItem(it.itemId).template.name + "'] của bản thân lên trang đầu?\nYêu cầu 5 Ngọc Xanh.", "Đồng ý", "Từ Chối");
    }

//    public void StartupItemToTop(Player pl) {
//        // Changed "Ngọc Xanh" requirement to "thỏi vàng" based on SubThoiVang
//        if (!SubThoiVang(pl, 2)) {
//            Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 2 thỏi vàng đưa vật phẩm lên trang đầu");
//            return;
//        }
//        for (ConsignItem its : ConsignShopManager.gI().listItem) {
//            if (its.id == pl.idMark.getIdItemUpTop()) {
//                its.isUpTop = 1;
//                Service.gI().sendThongBao(pl, "Đưa vật phẩm lên trang đầu thành công");
//                break;
//            }
//        }
//
//        ConsignShopManager.gI().save();
//        openShopKyGui(pl);
//    }

    public void claimOrDel(Player pl, byte action, int id) {
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
                item.itemOptions.addAll(it.options);
                if (ConsignShopManager.gI().listItem.remove(it)) {
                    InventoryService.gI().addItemBag(pl, item);
                    InventoryService.gI().sendItemBags(pl);
                    Service.gI().sendMoney(pl);
                    ConsignShopManager.gI().save();
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
                int fee = 0;
                int receivedAmount = 0;

                if (it.goldSell > 0) {
                    // Calculate fee and round it to the nearest whole number
                    fee = (int) Math.round(it.goldSell * 0.1);
                    receivedAmount = it.goldSell - fee;

                    // Thêm thỏi vàng vào inventory
                    Item goldBar = ItemService.gI().createNewItem((short) 457);
                    goldBar.quantity = receivedAmount;
                    InventoryService.gI().addItemBag(pl, goldBar);

                } else if (it.gemSell > 0) {
                    // Calculate fee and round it to the nearest whole number
                    fee = (int) Math.round(it.gemSell * 0.1);
                    receivedAmount = it.gemSell - fee;
                    pl.inventory.gem += receivedAmount;
                }

                if (ConsignShopManager.gI().listItem.remove(it)) {
                    Service.gI().sendMoney(pl);
                    InventoryService.gI().sendItemBags(pl); // Send updated inventory to player
                    ConsignShopManager.gI().save();
                    Service.gI().sendThongBao(pl, String.format("Bạn đã nhận được %d (đã trừ %d phí)", receivedAmount, fee));
                    openShopKyGui(pl);
                }
                break;
        }
    }

    public List<ConsignItem> getItemCanKiGui(Player pl) {
        List<ConsignItem> its = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && it.player_sell == pl.id)).forEachOrdered((it) -> {
            its.add(it);
        });
        pl.inventory.itemsBag.stream().filter((it) -> (itemCanConsign(it))).forEachOrdered((it) -> {
            // Note: When adding to this list, the 'id' field of ConsignItem is used as index in bag.
            // This is only for display in the "can consign" tab, not for actual consignment.
            its.add(new ConsignItem(InventoryService.gI().getIndexBag(pl, it), it.template.id, (int) pl.id, (byte) 4, -1, -1, it.quantity, (byte) -1, new ArrayList<>(it.itemOptions), false));
        });
        return its;
    }

    public boolean itemCanConsign(Item it) {
        if (it != null && it.template != null) {
            if (it.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 86)
                    || it.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 87)
                    || it.template.type == 14
                    || it.template.type == 15
                    || it.template.type == 6
                    || (it.template.id >= 14 && it.template.id <= 20)) {
                return true;
            }
        }
        return false;
    }

//    // Sửa phương thức getMaxId()
//    public int getMaxId() {
//        try (Connection con = AlyraManager.getConnection();
//             // Use java.sql.Statement directly, not com.mysql.jdbc.Statement, for better portability
//             java.sql.Statement s = con.createStatement();
//             ResultSet rs = s.executeQuery("SELECT MAX(id) FROM shop_ky_gui")) {
//
//            if (rs.next()) {
//                return rs.getInt(1);
//            }
//            return 0;
//        } catch (Exception e) {
//            e.printStackTrace();
//            Logger.logException(ConsignShopService.class, e, "Error getting max ID from shop_ky_gui");
//            return 0;
//        }
//    }
public int getMaxId() {
    Connection con = null;
    java.sql.PreparedStatement ps = null;  // Changed to java.sql interface
    ResultSet rs = null;
    
    try {
        con = AlyraManager.getConnection();
        if (con == null) {
            Logger.logException(ConsignShopService.class, 
                new Exception("Database connection is null"), 
                "Failed to get database connection for getMaxId");
            return 0;
        }
        
        // Using standard JDBC PreparedStatement interface
        ps = con.prepareStatement("SELECT COALESCE(MAX(id), 0) as max_id FROM shop_ky_gui FOR UPDATE");
        rs = ps.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("max_id");
        }
        return 0;
        
    } catch (Exception e) {
        Logger.logException(ConsignShopService.class, e, "Error getting max ID from shop_ky_gui");
        return 0;
    } finally {
        if (rs != null) {
            try { rs.close(); } catch (Exception e) {
                Logger.logException(ConsignShopService.class, e, "Error closing ResultSet in getMaxId");
            }
        }
        if (ps != null) {
            try { ps.close(); } catch (Exception e) {
                Logger.logException(ConsignShopService.class, e, "Error closing PreparedStatement in getMaxId");
            }
        }
        if (con != null) {
            try { con.close(); } catch (Exception e) {
                Logger.logException(ConsignShopService.class, e, "Error closing Connection in getMaxId");
            }
        }
    }
}
@SuppressWarnings("unchecked")
public int insertConsignItemAndGetId(ConsignItem item) {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet generatedKeys = null;
    
    try {
        con = AlyraManager.getConnection();
        if (con == null) {
            Logger.logException(ConsignShopService.class, new Exception("Database connection is null"), "Failed to get database connection for insertConsignItem");
            return -1;
        }
        
        con.setAutoCommit(false);
        
        String sql = "INSERT INTO `shop_ky_gui`(`player_id`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `isUpTop`, `isBuy`) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        
        // Validate dữ liệu
        if (item.player_sell <= 0 || item.itemId <= 0 || item.quantity <= 0) {
            Logger.log("ConsignShopService", "Invalid item data for insertion");
            return -1;
        }
        
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
        
        generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            int newId = generatedKeys.getInt(1);
            con.commit();
            return newId;
        } else {
            con.rollback();
            return -1;
        }
        
    } catch (Exception e) {
        Logger.logException(ConsignShopService.class, e, "Error inserting consign item");
        if (con != null) {
            try {
                con.rollback();
            } catch (Exception ex) {
                Logger.logException(ConsignShopService.class, ex, "Error rolling back transaction in insertConsignItem");
            }
        }
        return -1;
    } finally {
        if (generatedKeys != null) {
            try { 
                generatedKeys.close(); 
            } catch (Exception e) {
                Logger.logException(ConsignShopService.class, e, "Error closing generated keys ResultSet");
            }
        }
        if (ps != null) {
            try { 
                ps.close(); 
            } catch (Exception e) {
                Logger.logException(ConsignShopService.class, e, "Error closing PreparedStatement in insertConsignItem");
            }
        }
        if (con != null) {
            try { 
                con.setAutoCommit(true);
                con.close(); 
            } catch (Exception e) {
                Logger.logException(ConsignShopService.class, e, "Error closing Connection in insertConsignItem");
            }
        }
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
        try {
            // Kiểm tra phí đăng bán
            if (!SubThoiVang(pl, 1)) {
                Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 1 thỏi vàng để làm phí đăng bán");
                return;
            }

            // Kiểm tra item có tồn tại không
            if (id < 0 || id >= pl.inventory.itemsBag.size()) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại");
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

            // Check if item is already being consigned by this player
            if (ConsignShopManager.gI().listItem.stream().anyMatch(consignIt
                    -> consignIt.player_sell == pl.id
                    && consignIt.itemId == it.template.id
                    && consignIt.quantity == quantity
                    && consignIt.options.equals(it.itemOptions)
                    && !consignIt.isBuy
            )) {
                Service.gI().sendThongBao(pl, "Bạn đã có vật phẩm tương tự đang ký gửi.");
                openShopKyGui(pl);
                return;
            }


            // Check maximum consignment items per player (optional, but good practice)
            long consignedCount = ConsignShopManager.gI().listItem.stream().filter(consignIt -> consignIt.player_sell == pl.id && !consignIt.isBuy).count();
            if (consignedCount >= 20) { // Example limit: 10 items per player
                Service.gI().sendThongBao(pl, "Bạn chỉ có thể ký gửi tối đa 20 vật phẩm.");
                openShopKyGui(pl);
                return;
            }
            
            // Lấy ID mới từ database
            // WARNING: Using MAX(id) + 1 for ID generation in a concurrent environment
            // can lead to race conditions and duplicate IDs. It's highly recommended
            // to use AUTO_INCREMENT primary keys in the database for ID generation.
            int newId = getMaxId() + 1;

            // Tạo consign item
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
                    false
            );

            // Add the item to the consignment list
            ConsignShopManager.gI().listItem.add(newConsignItem);

            // Subtract item from player's inventory
            InventoryService.gI().subQuantityItemsBag(pl, it, quantity);
            InventoryService.gI().sendItemBags(pl);
            ConsignShopManager.gI().save(); // Save the updated consignment list
            Service.gI().sendThongBao(pl, "Ký gửi vật phẩm thành công!");
            openShopKyGui(pl);

        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Có lỗi xảy ra khi xử lý yêu cầu");
            Logger.logException(ConsignShopService.class, e, "Error in KiGui method");
            openShopKyGui(pl);
        }
    }

    public void openShopKyGui(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(2);
            msg.writer().writeByte(5);
            for (byte i = 0; i < 5; i++) {
                if (i == 4) {
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(getItemCanKiGui(pl).size());
                    for (int j = 0; j < getItemCanKiGui(pl).size(); j++) {
                        ConsignItem itk = getItemCanKiGui(pl).get(j);
                        if (itk == null) {
                            continue;
                        }
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
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
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
                    }
                } else {
                    List<ConsignItem> items = getItemKyGui(pl, i);
                    List<ConsignItem> itemsSend = getItemKyGui2(pl, i, (byte) 0, (byte) 20);
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    byte tab = (byte) (items.size() / 20 > 0 ? (items.size() / 20) + 1 : 1);
                    msg.writer().writeByte(tab); // max page
                    msg.writer().writeByte(itemsSend.size());
                    for (int j = 0; j < itemsSend.size(); j++) {
                        ConsignItem itk = itemsSend.get(j);
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.goldSell);
                        msg.writer().writeInt(itk.gemSell);
                        msg.writer().writeByte(0); // buy type
                        msg.writer().writeInt(itk.quantity);
                        msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
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
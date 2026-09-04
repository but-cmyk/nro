package managers;

import database.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import models.ConsignItem;
import models.item.Item;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.Logger;

public class ConsignShopManager {

    private static ConsignShopManager instance;
    private final ReentrantLock saveLock = new ReentrantLock();
    private final ReentrantLock listLock = new ReentrantLock();
    private final AtomicInteger nextItemId = new AtomicInteger(0);

    public static ConsignShopManager gI() {
        if (instance == null) {
            synchronized (ConsignShopManager.class) {
                if (instance == null) {
                    instance = new ConsignShopManager();
                }
            }
        }
        return instance;
    }

    public long lastTimeUpdate;
    public String[] tabName = {"Áo Quần", "Găng Tay", "Phụ Kiện", "Linh tinh", ""};
    public List<ConsignItem> listItem = new ArrayList<>();

   @SuppressWarnings("unchecked")
    public void save() {
        saveLock.lock();
        try {
            listLock.lock();
            List<ConsignItem> itemsToSave = new ArrayList<>(this.listItem);
            listLock.unlock();

            if (itemsToSave.isEmpty()) {
                Logger.log("ConsignShopManager", "No consignment items to save.");
                return;
            }

            try (Connection con = AlyraManager.getConnection()) {
                if (con == null) {
                    Logger.logException(ConsignShopManager.class, new Exception("Database connection is null"), "Failed to get database connection");
                    return;
                }

                con.setAutoCommit(false);

                try (Statement stmt = con.createStatement()) {
                    stmt.execute("DELETE FROM shop_ky_gui");
                }

                String sql = "INSERT INTO shop_ky_gui(id, player_id, tab, item_id, gold, gem, quantity, itemOption, isUpTop, isBuy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    int batchCount = 0;
                    final int BATCH_SIZE = 100;

                    for (ConsignItem it : itemsToSave) {
                        if (it == null || it.player_sell <= 0 || it.itemId <= 0 || it.quantity <= 0) {
                            continue;
                        }

                        try {
                            String optionsJson = "[]";
                            if (it.options != null && !it.options.isEmpty()) {
                                JSONArray array = new JSONArray();
                                for (Item.ItemOption option : it.options) {
                                    if (option != null && option.optionTemplate != null) {
                                        JSONObject obj = new JSONObject();
                                        obj.put("id", option.optionTemplate.id);
                                        obj.put("param", option.param);
                                        array.add(obj);
                                    }
                                }
                                optionsJson = JSONValue.toJSONString(array);
                            }

                            ps.setInt(1, it.id);
                            ps.setInt(2, it.player_sell);
                            ps.setByte(3, it.tab);
                            ps.setInt(4, it.itemId);
                            ps.setInt(5, it.goldSell);
                            ps.setInt(6, it.gemSell);
                            ps.setInt(7, it.quantity);
                            ps.setString(8, optionsJson);
                            ps.setByte(9, it.isUpTop);
                            ps.setBoolean(10, it.isBuy);

                            ps.addBatch();
                            batchCount++;

                            if (batchCount >= BATCH_SIZE) {
                                ps.executeBatch();
                                ps.clearBatch();
                                batchCount = 0;
                            }
                        } catch (Exception ex) {
                            Logger.logException(ConsignShopManager.class, ex, "Error inserting item ID: " + it.id);
                        }
                    }

                    if (batchCount > 0) {
                        ps.executeBatch();
                    }
                }

                con.commit();
                Logger.log("Successfully saved " + itemsToSave.size() + " items\n");

            } catch (Exception e) {
                Logger.logException(ConsignShopManager.class, e, "Failed to save consignment shop data");
            }
        } finally {
            saveLock.unlock();
        }
    }

    public void load() {
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM shop_ky_gui ORDER BY created_at ASC");
             ResultSet rs = ps.executeQuery()) {

            listLock.lock();
            try {
                this.listItem.clear();
                int loadedCount = 0;
                int errorCount = 0;

                while (rs.next()) {
                    try {
                        int id = rs.getInt("id");
                        nextItemId.accumulateAndGet(id, Math::max);
                        int idPl = rs.getInt("player_id");
                        byte tab = rs.getByte("tab");
                        short itemId = rs.getShort("item_id");
                        int gold = rs.getInt("gold");
                        int gem = rs.getInt("gem");
                        int quantity = rs.getInt("quantity");
                        byte isUp = rs.getByte("isUpTop");
                        boolean isBuy = rs.getByte("isBuy") == 1;

                        if (idPl <= 0 || itemId <= 0 || quantity <= 0) {
                            errorCount++;
                            continue;
                        }

                        List<Item.ItemOption> options = new ArrayList<>();
                        String itemOptionsJson = rs.getString("itemOption");

                        if (itemOptionsJson != null && !itemOptionsJson.trim().isEmpty() && !itemOptionsJson.equals("null")) {
                            try {
                                Object parsed = JSONValue.parse(itemOptionsJson);
                                if (parsed instanceof JSONArray) {
                                    JSONArray jsa2 = (JSONArray) parsed;
                                    for (int j = 0; j < jsa2.size(); ++j) {
                                        Object item = jsa2.get(j);
                                        if (item instanceof JSONObject) {
                                            JSONObject jso2 = (JSONObject) item;
                                            Object idObj = jso2.get("id");
                                            Object paramObj = jso2.get("param");

                                            if (idObj != null && paramObj != null) {
                                                try {
                                                    int idOptions = Integer.parseInt(idObj.toString());
                                                    int param = Integer.parseInt(paramObj.toString());
                                                    options.add(new Item.ItemOption(idOptions, param));
                                                } catch (NumberFormatException nfe) {
                                                    Logger.logException(ConsignShopManager.class, nfe, "Invalid number format in item options for item ID: " + id);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception jsonEx) {
                                Logger.logException(ConsignShopManager.class, jsonEx, "Error parsing JSON for item options, item ID: " + id);
                            }
                        }

                        ConsignItem consignItem = new ConsignItem(id, itemId, idPl, tab, gold, gem, quantity, isUp, options, isBuy);
                        this.listItem.add(consignItem);
                        loadedCount++;

                    } catch (Exception itemEx) {
                        errorCount++;
                        Logger.logException(ConsignShopManager.class, itemEx, "Error loading individual consign item from database");
                    }
                }

                Logger.log("ConsignShopManager", "Loaded " + loadedCount + " consign items from database"
                        + (errorCount > 0 ? " (" + errorCount + " errors)" : ""));

            } finally {
                listLock.unlock();
            }

        } catch (Exception e) {
            Logger.logException(ConsignShopManager.class, e, "Error loading consignment shop data");
        }
    }

    public void init() {
        load();
        this.lastTimeUpdate = System.currentTimeMillis();
    }

    // Thread-safe method để thêm item
    public void addItem(ConsignItem item) {
        if (item != null) {
            listLock.lock();
            try {
                this.listItem.add(item);
            } finally {
                listLock.unlock();
            }
        }
    }

    // Thread-safe method để xóa item
    public boolean removeItem(ConsignItem item) {
        if (item != null) {
            listLock.lock();
            try {
                return this.listItem.remove(item);
            } finally {
                listLock.unlock();
            }
        }
        return false;
    }

    // Thread-safe method để lấy item theo ID
    public ConsignItem getItemById(int id) {
        listLock.lock();
        try {
            for (ConsignItem it : this.listItem) {
                if (it != null && it.id == id) {
                    return it;
                }
            }
            return null;
        } finally {
            listLock.unlock();
        }
    }

    // Atomic DB operations - Cập nhật trạng thái đã mua
    public void updateItemBoughtAsync(int itemId) {
        Thread.ofVirtual().name("db-consign-bought").start(() -> {
            String sql = "UPDATE `shop_ky_gui` SET `isBuy` = 1 WHERE `id` = ?";
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, itemId);
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.logException(ConsignShopManager.class, e, "Lỗi updateItemBoughtAsync ID: " + itemId);
            }
        });
    }

    // Atomic DB operations - Xóa item khi hủy hoặc nhận tiền
    public void deleteItemAsync(int itemId) {
        Thread.ofVirtual().name("db-consign-delete").start(() -> {
            String sql = "DELETE FROM `shop_ky_gui` WHERE `id` = ?";
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, itemId);
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.logException(ConsignShopManager.class, e, "Lỗi deleteItemAsync ID: " + itemId);
            }
        });
    }

    // Atomic DB operations - Cập nhật đưa lên top
    public void updateItemUpTopAsync(int itemId, byte isUpTop) {
        Thread.ofVirtual().name("db-consign-uptop").start(() -> {
            String sql = "UPDATE `shop_ky_gui` SET `isUpTop` = ? WHERE `id` = ?";
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setByte(1, isUpTop);
                ps.setInt(2, itemId);
                ps.executeUpdate();
            } catch (Exception e) {
                Logger.logException(ConsignShopManager.class, e, "Lỗi updateItemUpTopAsync ID: " + itemId);
            }
        });
    }

    // Atomic DB operations - Chèn item mới và thêm vào danh sách
    @SuppressWarnings("unchecked")
    public int insertItem(ConsignItem item) {
        if (item == null || item.player_sell <= 0 || item.itemId <= 0 || item.quantity <= 0) {
            return -1;
        }
        String sql = "INSERT INTO `shop_ky_gui`(`player_id`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `isUpTop`, `isBuy`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            } catch (Exception ignored) {
                optionsJson = "[]";
            }
        }
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.player_sell);
            ps.setByte(2, item.tab);
            ps.setInt(3, item.itemId);
            ps.setInt(4, item.goldSell);
            ps.setInt(5, item.gemSell);
            ps.setInt(6, item.quantity);
            ps.setString(7, optionsJson);
            ps.setByte(8, item.isUpTop);
            ps.setBoolean(9, item.isBuy);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        item.id = newId;
                        nextItemId.accumulateAndGet(newId, Math::max);
                        addItem(item);
                        return newId;
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(ConsignShopManager.class, e, "Lỗi insertItem");
        }
        int fallbackId = nextItemId();
        item.id = fallbackId;
        addItem(item);
        return fallbackId;
    }

    public void lockItems() {
        listLock.lock();
    }

    public void unlockItems() {
        listLock.unlock();
    }

    public int nextItemId() {
        return nextItemId.incrementAndGet();
    }

    public List<ConsignItem> getItemsSnapshot() {
        listLock.lock();
        try {
            return new ArrayList<>(listItem);
        } finally {
            listLock.unlock();
        }
    }
}

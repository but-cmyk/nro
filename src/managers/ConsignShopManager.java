package managers;

import database.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "db-consign-worker");
        t.setDaemon(true);
        return t;
    });

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
    private final java.util.Set<Integer> pendingDeleteIds = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unchecked")
    private static String serializeOptions(ConsignItem it) {
        if (it == null || it.options == null || it.options.isEmpty()) {
            return "[]";
        }
        try {
            JSONArray array = new JSONArray();
            for (Item.ItemOption option : it.options) {
                if (option != null && option.optionTemplate != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", option.optionTemplate.id);
                    obj.put("param", option.param);
                    array.add(obj);
                }
            }
            return JSONValue.toJSONString(array);
        } catch (Exception ignored) {
            return "[]";
        }
    }

   @SuppressWarnings("unchecked")
    public void save() {
        saveLock.lock();
        try {
            listLock.lock();
            List<ConsignItem> snapshot = new ArrayList<>(this.listItem);
            listLock.unlock();

            List<ConsignItem> toInsert = new ArrayList<>();
            List<ConsignItem> toUpdate = new ArrayList<>();
            for (ConsignItem it : snapshot) {
                if (it == null || it.player_sell <= 0 || it.itemId <= 0 || it.quantity <= 0) {
                    continue;
                }
                if (!it.persistedInDb) {
                    toInsert.add(it);
                } else if (it.dirty) {
                    toUpdate.add(it);
                }
            }
            List<Integer> toDelete = new ArrayList<>(pendingDeleteIds);

            if (toInsert.isEmpty() && toUpdate.isEmpty() && toDelete.isEmpty()) {
                return;
            }

            try (Connection con = AlyraManager.getConnection()) {
                if (con == null) {
                    Logger.logException(ConsignShopManager.class, new Exception("Database connection is null"), "Failed to get database connection");
                    return;
                }

                con.setAutoCommit(false);

                try {
                    final int BATCH_SIZE = 100;

                    if (!toDelete.isEmpty()) {
                        String deleteSql = "DELETE FROM `shop_ky_gui` WHERE `id` = ?";
                        try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
                            int batchCount = 0;
                            for (Integer id : toDelete) {
                                psDel.setInt(1, id);
                                psDel.addBatch();
                                if (++batchCount >= BATCH_SIZE) {
                                    psDel.executeBatch();
                                    psDel.clearBatch();
                                    batchCount = 0;
                                }
                            }
                            if (batchCount > 0) {
                                psDel.executeBatch();
                            }
                        }
                    }

                    if (!toInsert.isEmpty()) {
                        String insertSql = "INSERT INTO `shop_ky_gui`(`id`, `player_id`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `isUpTop`, `isBuy`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement psIns = con.prepareStatement(insertSql)) {
                            int batchCount = 0;
                            for (ConsignItem it : toInsert) {
                                psIns.setInt(1, it.id);
                                psIns.setInt(2, it.player_sell);
                                psIns.setByte(3, it.tab);
                                psIns.setInt(4, it.itemId);
                                psIns.setInt(5, it.goldSell);
                                psIns.setInt(6, it.gemSell);
                                psIns.setInt(7, it.quantity);
                                psIns.setString(8, serializeOptions(it));
                                psIns.setByte(9, it.isUpTop);
                                psIns.setBoolean(10, it.isBuy);
                                psIns.addBatch();
                                if (++batchCount >= BATCH_SIZE) {
                                    psIns.executeBatch();
                                    psIns.clearBatch();
                                    batchCount = 0;
                                }
                            }
                            if (batchCount > 0) {
                                psIns.executeBatch();
                            }
                        }
                    }

                    if (!toUpdate.isEmpty()) {
                        String updateSql = "UPDATE `shop_ky_gui` SET `gold` = ?, `gem` = ?, `quantity` = ?, `itemOption` = ?, `isUpTop` = ?, `isBuy` = ? WHERE `id` = ?";
                        try (PreparedStatement psUpd = con.prepareStatement(updateSql)) {
                            int batchCount = 0;
                            for (ConsignItem it : toUpdate) {
                                psUpd.setInt(1, it.goldSell);
                                psUpd.setInt(2, it.gemSell);
                                psUpd.setInt(3, it.quantity);
                                psUpd.setString(4, serializeOptions(it));
                                psUpd.setByte(5, it.isUpTop);
                                psUpd.setBoolean(6, it.isBuy);
                                psUpd.setInt(7, it.id);
                                psUpd.addBatch();
                                if (++batchCount >= BATCH_SIZE) {
                                    psUpd.executeBatch();
                                    psUpd.clearBatch();
                                    batchCount = 0;
                                }
                            }
                            if (batchCount > 0) {
                                psUpd.executeBatch();
                            }
                        }
                    }

                    con.commit();
                    for (ConsignItem it : toInsert) {
                        it.persistedInDb = true;
                        it.dirty = false;
                    }
                    for (ConsignItem it : toUpdate) {
                        it.dirty = false;
                    }
                    pendingDeleteIds.removeAll(toDelete);
                    Logger.log("ConsignShop incremental save: +" + toInsert.size() + " ~" + toUpdate.size() + " -" + toDelete.size() + "\n");
                } catch (Exception txEx) {
                    try {
                        con.rollback();
                    } catch (Exception ignored) {
                    }
                    Logger.logException(ConsignShopManager.class, txEx, "Transaction failed & rolled back while saving consignment shop");
                }
            } catch (Exception e) {
                Logger.logException(ConsignShopManager.class, e, "Failed to save consignment shop data");
            }
        } finally {
            saveLock.unlock();
        }
    }

    public void load() {
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT shop_ky_gui.*, player.name AS player_name " +
                     "FROM shop_ky_gui " +
                     "LEFT JOIN player ON shop_ky_gui.player_id = player.id " +
                     "ORDER BY shop_ky_gui.created_at ASC");
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
                        String plName = rs.getString("player_name");
                        if (plName == null) {
                            plName = "";
                        }

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

                        ConsignItem consignItem = new ConsignItem(id, itemId, idPl, tab, gold, gem, quantity, isUp, options, isBuy, plName);
                        consignItem.persistedInDb = true;
                        consignItem.dirty = false;
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

    // Atomic DB operations - Cập nhật trạng thái đã mua (tuần tự dưới saveLock)
    public void updateItemBoughtAsync(int itemId) {
        dbExecutor.execute(() -> {
            saveLock.lock();
            try {
                String sql = "UPDATE `shop_ky_gui` SET `isBuy` = 1 WHERE `id` = ?";
                try (Connection con = AlyraManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, itemId);
                    ps.executeUpdate();
                } catch (Exception e) {
                    Logger.logException(ConsignShopManager.class, e, "Lỗi updateItemBoughtAsync ID: " + itemId);
                }
            } finally {
                saveLock.unlock();
            }
        });
    }

    // Atomic DB operations - Xóa item khi hủy hoặc nhận tiền (tuần tự dưới saveLock)
    public void deleteItemAsync(int itemId) {
        pendingDeleteIds.add(itemId);
        dbExecutor.execute(() -> {
            saveLock.lock();
            try {
                String sql = "DELETE FROM `shop_ky_gui` WHERE `id` = ?";
                try (Connection con = AlyraManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, itemId);
                    ps.executeUpdate();
                    pendingDeleteIds.remove(itemId);
                } catch (Exception e) {
                    Logger.logException(ConsignShopManager.class, e, "Lỗi deleteItemAsync ID: " + itemId);
                }
            } finally {
                saveLock.unlock();
            }
        });
    }

    // Atomic DB operations - Cập nhật đưa lên top (tuần tự dưới saveLock)
    public void updateItemUpTopAsync(int itemId, byte isUpTop) {
        dbExecutor.execute(() -> {
            saveLock.lock();
            try {
                String sql = "UPDATE `shop_ky_gui` SET `isUpTop` = ? WHERE `id` = ?";
                try (Connection con = AlyraManager.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setByte(1, isUpTop);
                    ps.setInt(2, itemId);
                    ps.executeUpdate();
                } catch (Exception e) {
                    Logger.logException(ConsignShopManager.class, e, "Lỗi updateItemUpTopAsync ID: " + itemId);
                }
            } finally {
                saveLock.unlock();
            }
        });
    }

    // Atomic Async DB operation - Chèn item mới tuần tự dưới saveLock (không block Netty)
    @SuppressWarnings("unchecked")
    public void insertItemAsync(ConsignItem item) {
        if (item == null || item.player_sell <= 0 || item.itemId <= 0 || item.quantity <= 0) {
            return;
        }
        dbExecutor.execute(() -> {
            saveLock.lock();
            try {
                String sql = "INSERT INTO `shop_ky_gui`(`id`, `player_id`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `isUpTop`, `isBuy`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, item.id);
                    ps.setInt(2, item.player_sell);
                    ps.setByte(3, item.tab);
                    ps.setInt(4, item.itemId);
                    ps.setInt(5, item.goldSell);
                    ps.setInt(6, item.gemSell);
                    ps.setInt(7, item.quantity);
                    ps.setString(8, optionsJson);
                    ps.setByte(9, item.isUpTop);
                    ps.setBoolean(10, item.isBuy);
                    ps.executeUpdate();
                    item.persistedInDb = true;
                    item.dirty = false;
                } catch (Exception e) {
                    Logger.logException(ConsignShopManager.class, e, "Lỗi insertItemAsync ID: " + item.id);
                }
            } finally {
                saveLock.unlock();
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

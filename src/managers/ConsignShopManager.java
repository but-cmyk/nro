package managers;

import database.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
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
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = AlyraManager.getConnection();
            if (con == null) {
                Logger.logException(ConsignShopManager.class, new Exception("Database connection is null"), "Failed to get database connection");
                return;
            }

            con.setAutoCommit(false);

            try (Statement stmt = con.createStatement()) {
                stmt.execute("DELETE FROM shop_ky_gui");
            }

            listLock.lock();
            List<ConsignItem> itemsToSave = new ArrayList<>(this.listItem);
            listLock.unlock();

            if (itemsToSave.isEmpty()) {
                con.commit();
                Logger.log("ConsignShopManager", "No consignment items to save.");
                return;
            }

            String sql = "INSERT INTO shop_ky_gui(player_id, tab, item_id, gold, gem, quantity, itemOption, isUpTop, isBuy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);

            int batchCount = 0;
            final int BATCH_SIZE = 100;

            for (ConsignItem it : itemsToSave) {
                if (it == null || it.player_sell <= 0 || it.itemId <= 0 || it.quantity <= 0) {
                    Logger.log("ConsignShopManager", "Skipping invalid item: " + (it != null ? it.itemId : "null"));
                    continue;
                }

                try {
                    // Convert item options to JSON
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

                    ps.setInt(1, it.player_sell);
                    ps.setByte(2, it.tab);
                    ps.setInt(3, it.itemId);
                    ps.setInt(4, it.goldSell);
                    ps.setInt(5, it.gemSell);
                    ps.setInt(6, it.quantity);
                    ps.setString(7, optionsJson);
                    ps.setByte(8, it.isUpTop);
                    ps.setBoolean(9, it.isBuy);

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

            con.commit();
            Logger.log("Successfully saved " + itemsToSave.size() + " items\n");

        } catch (Exception e) {
            Logger.logException(ConsignShopManager.class, e, "Failed to save consignment shop data");
            try {
                if (con != null) {
                    con.rollback();
                    Logger.log("ConsignShopManager", "Transaction rolled back");
                }
            } catch (Exception rollbackEx) {
                Logger.logException(ConsignShopManager.class, rollbackEx, "Error during rollback");
            }
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception closeEx) {
                Logger.logException(ConsignShopManager.class, closeEx, "Error closing resources");
            }
            saveLock.unlock();
        }
    }

    public void load() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = AlyraManager.getConnection();
            if (con == null) {
                Logger.logException(ConsignShopManager.class, new Exception("Database connection is null"), "Failed to get database connection for load");
                return;
            }

            ps = con.prepareStatement("SELECT * FROM shop_ky_gui ORDER BY created_at ASC");
            rs = ps.executeQuery();

            listLock.lock();
            this.listItem.clear();

            int loadedCount = 0;
            int errorCount = 0;

            while (rs.next()) {
                try {
                    int id = rs.getInt("id");
                    int idPl = rs.getInt("player_id");
                    byte tab = rs.getByte("tab");
                    short itemId = rs.getShort("item_id");
                    int gold = rs.getInt("gold");
                    int gem = rs.getInt("gem");
                    int quantity = rs.getInt("quantity");
                    byte isUp = rs.getByte("isUpTop");
                    boolean isBuy = rs.getByte("isBuy") == 1;

                    // Validate dữ liệu cơ bản
                    if (idPl <= 0 || itemId <= 0 || quantity <= 0) {
                        Logger.log("ConsignShopManager", "Skipping invalid item data: ID=" + id + ", PlayerID=" + idPl + ", ItemID=" + itemId + ", Quantity=" + quantity);
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
                            Logger.logException(ConsignShopManager.class, jsonEx, "Error parsing JSON for item options, item ID: " + id + ", JSON: " + itemOptionsJson);
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

        } catch (Exception e) {
            Logger.logException(ConsignShopManager.class, e, "Error loading consignment shop data");
        } finally {
            listLock.unlock();
            // Đóng resources theo thứ tự
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    Logger.logException(ConsignShopManager.class, e, "Error closing ResultSet");
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    Logger.logException(ConsignShopManager.class, e, "Error closing PreparedStatement");
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    Logger.logException(ConsignShopManager.class, e, "Error closing Connection");
                }
            }
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
}

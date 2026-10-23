package database.daos;

import database.AlyraManager;
import models.item.Item;
import models.player.Player;
import utils.TimeUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HistoryTransactionDAO {

    private static volatile boolean checkedTradeIdColumn = false;

    private static void ensureTradeIdColumn() {
        if (checkedTradeIdColumn) {
            return;
        }
        synchronized (HistoryTransactionDAO.class) {
            if (checkedTradeIdColumn) {
                return;
            }
            try (Connection con = AlyraManager.getConnection()) {
                if (con != null) {
                    boolean hasColumn = false;
                    try (java.sql.ResultSet rs = con.getMetaData().getColumns(null, null, "history_transaction", "trade_id")) {
                        if (rs.next()) {
                            hasColumn = true;
                        }
                    }
                    if (!hasColumn) {
                        try (java.sql.Statement stmt = con.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE `history_transaction` ADD COLUMN `trade_id` VARCHAR(36) NULL UNIQUE AFTER `id`");
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            } finally {
                checkedTradeIdColumn = true;
            }
        }
    }

    public static void insert(String tradeId, Player pl1, Player pl2,
            int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After,
            List<Item> bag2After,
            long gold1Before, long gold2Before, long gold1After, long gold2After) {

        ensureTradeIdColumn();
        String player1 = pl1.name + " (" + pl1.id + ")";
        String player2 = pl2.name + " (" + pl2.id + ")";
        String itemPlayer1 = "Gold: " + goldP1 + ", ";
        String itemPlayer2 = "Gold: " + goldP2 + ", ";
        List<Item> doGD1 = new ArrayList<>();
        List<Item> doGD2 = new ArrayList<>();
        for (Item item : itemP1) {
            if (item.isNotNullItem() && doGD1.stream().noneMatch(item1 -> item1.template.id == item.template.id)) {
                doGD1.add(item);
            } else if (item.isNotNullItem()) {
                doGD1.stream().filter(item1 -> item1.template.id == item.template.id).findFirst().get().quantityGD += item.quantityGD;
            }
        }
        for (Item item : itemP2) {
            if (item.isNotNullItem() && doGD2.stream().noneMatch(item1 -> item1.template.id == item.template.id)) {
                doGD2.add(item);
            } else if (item.isNotNullItem()) {
                doGD2.stream().filter(item1 -> item1.template.id == item.template.id).findFirst().get().quantityGD += item.quantityGD;
            }
        }

        for (Item item : doGD1) {
            if (item.isNotNullItem()) {
                itemPlayer1 += item.template.name + " (x" + item.quantityGD + "),";
            }
        }
        for (Item item : doGD2) {
            if (item.isNotNullItem()) {
                itemPlayer2 += item.template.name + " (x" + item.quantityGD + "),";
            }
        }
        String beforeTran1 = "";
        String beforeTran2 = "";
        for (Item item : bag1Before) {
            if (item.isNotNullItem()) {
                beforeTran1 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        for (Item item : bag2Before) {
            if (item.isNotNullItem()) {
                beforeTran2 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        String afterTran1 = "";
        String afterTran2 = "";
        for (Item item : bag1After) {
            if (item.isNotNullItem()) {
                afterTran1 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        for (Item item : bag2After) {
            if (item.isNotNullItem()) {
                afterTran2 += item.template.name + " (x" + item.quantity + "),";
            }
        }
        try {
            if (tradeId != null && !tradeId.isEmpty()) {
                AlyraManager.executeUpdate("INSERT INTO history_transaction (trade_id, player_1, player_2, item_player_1, item_player_2, bag_1_before_tran, bag_2_before_tran, bag_1_after_tran, bag_2_after_tran, time_tran) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE id=id",
                        tradeId, player1, player2, itemPlayer1, itemPlayer2, beforeTran1, beforeTran2, afterTran1, afterTran2, new Timestamp(System.currentTimeMillis()));
            } else {
                AlyraManager.executeUpdate("INSERT INTO history_transaction (player_1, player_2, item_player_1, item_player_2, bag_1_before_tran, bag_2_before_tran, bag_1_after_tran, bag_2_after_tran, time_tran) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        player1, player2, itemPlayer1, itemPlayer2, beforeTran1, beforeTran2, afterTran1, afterTran2, new Timestamp(System.currentTimeMillis()));
            }
        } catch (Exception ex) {
            // Fallback nếu cột trade_id chưa sẵn sàng trong database
            try {
                AlyraManager.executeUpdate("INSERT INTO history_transaction (player_1, player_2, item_player_1, item_player_2, bag_1_before_tran, bag_2_before_tran, bag_1_after_tran, bag_2_after_tran, time_tran) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        player1, player2, itemPlayer1, itemPlayer2, beforeTran1, beforeTran2, afterTran1, afterTran2, new Timestamp(System.currentTimeMillis()));
            } catch (Exception ignored) {
            }
        }
    }

    public static void insert(Player pl1, Player pl2,
            int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After,
            List<Item> bag2After,
            long gold1Before, long gold2Before, long gold1After, long gold2After) {
        insert(null, pl1, pl2, goldP1, goldP2, itemP1, itemP2, bag1Before, bag2Before, bag1After, bag2After, gold1Before, gold2Before, gold1After, gold2After);
    }

    public static void insertAsync(String tradeId, Player pl1, Player pl2,
            int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After,
            List<Item> bag2After,
            long gold1Before, long gold2Before, long gold1After, long gold2After) {
        server.ServerManager.gI().execute(() -> {
            try {
                insert(tradeId, pl1, pl2, goldP1, goldP2, itemP1, itemP2, bag1Before, bag2Before, bag1After, bag2After, gold1Before, gold2Before, gold1After, gold2After);
            } catch (Exception e) {
                utils.Logger.logException(HistoryTransactionDAO.class, e, "Lỗi khi ghi lịch sử giao dịch async");
            }
        });
    }

    public static void insertAsync(Player pl1, Player pl2,
            int goldP1, int goldP2, List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After,
            List<Item> bag2After,
            long gold1Before, long gold2Before, long gold1After, long gold2After) {
        insertAsync(null, pl1, pl2, goldP1, goldP2, itemP1, itemP2, bag1Before, bag2Before, bag1After, bag2After, gold1Before, gold2Before, gold1After, gold2After);
    }

    public static void deleteHistory() {
        long cutoffMillis = System.currentTimeMillis() - (23L * 24 * 60 * 60 * 1000);
        String sql = "DELETE FROM history_transaction WHERE time_tran < ?";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(cutoffMillis));
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                utils.Logger.log("Đã dọn dẹp " + deleted + " bản ghi lịch sử giao dịch cũ (>23 ngày).");
            }
        } catch (Exception e) {
            utils.Logger.logException(HistoryTransactionDAO.class, e, "Lỗi khi xóa lịch sử giao dịch cũ");
        }
    }

}

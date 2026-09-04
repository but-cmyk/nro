package database.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import database.AlyraManager;
import database.AlyraResultSet;
import java.util.Date;
import models.tournament.super_rank.SuperRank;
import models.tournament.super_rank.SuperRankBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import models.player.Player;
import org.json.simple.parser.JSONParser;
import utils.Logger;
import utils.TimeUtil;
import utils.Util;

public class SuperRankDAO {

    public static List<SuperRankBuilder> getPlayerListInRankRange(int rank, int limit) {
        List<SuperRankBuilder> list = new ArrayList<>();
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("SELECT * FROM super_rank WHERE rank <= ? AND rank > 0 ORDER BY rank DESC LIMIT ?", Math.max(rank, 10), limit);
            while (rs.next()) {
                list.add(readData(rs));
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        try {
            int rand = random(rank);
            if (rand != -1) {
                rs = AlyraManager.executeQuery("SELECT * FROM super_rank WHERE rank = ? LIMIT 1", rand);
                if (rs.first()) {
                    list.add(readData(rs));
                }
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        Collections.reverse(list);
        return list;
    }

    public static List<SuperRankBuilder> getPlayerListInRank(int rank, int limit) {
        List<SuperRankBuilder> list = new ArrayList<>();
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("SELECT * FROM super_rank WHERE rank > 0 ORDER BY rank ASC LIMIT ?", limit);
            while (rs.next()) {
                list.add(readData(rs));
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        try {
            if (rank > 100) {
                rs = AlyraManager.executeQuery("SELECT * FROM super_rank WHERE rank > ? AND rank < ? ORDER BY rank ASC LIMIT 4", rank - 3, rank + 2);
                while (rs.next()) {
                    list.add(readData(rs));
                }
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return list;
    }

    public static int random(int rank) {
        if (rank > 10000) {
            return Util.nextInt(6666, 10000);
        } else if (rank > 6666) {
            return Util.nextInt(3333, 6666);
        } else if (rank > 3333) {
            return Util.nextInt(1000, 3333);
        } else if (rank > 1000) {
            return Util.nextInt(666, 1000);
        } else if (rank > 666) {
            return Util.nextInt(333, 666);
        } else if (rank > 333) {
            return Util.nextInt(100, 333);
        }
        return -1;
    }

    public static SuperRankBuilder readData(AlyraResultSet rs) throws Exception {

        SuperRankBuilder superRankbuilder = new SuperRankBuilder();

        if (rs != null) {
            superRankbuilder.setId(rs.getInt("player_id"));
            superRankbuilder.setName(rs.getString("name"));
            superRankbuilder.setRank(rs.getInt("rank"));
            superRankbuilder.setLastPKTime(rs.getLong("last_pk_time"));
            superRankbuilder.setLastTimeReward(rs.getLong("last_reward_time"));
            superRankbuilder.setTicket(rs.getInt("ticket"));

            StringBuilder text = new StringBuilder();
            JSONParser parser = new JSONParser();
            JSONObject info;

            try {
                info = (JSONObject) parser.parse(rs.getString("info"));
                if (info != null) {
                    superRankbuilder.setHead(((Long) info.get("head")).intValue());
                    superRankbuilder.setBody(((Long) info.get("body")).intValue());
                    superRankbuilder.setLeg(((Long) info.get("leg")).intValue());
                    text.append("HP: ").append(Util.formatNumber(((Long) info.get("hp")).intValue())).append("\n");
                    text.append("Sức đánh: ").append(Util.formatNumber(((Long) info.get("dame")).intValue())).append("\n");
                    text.append("Giáp: ").append(Util.formatNumber(((Long) info.get("def")).intValue())).append("\n");
                }
            } catch (Exception e) {
            }

            JSONArray historyList;

            try {
                historyList = (JSONArray) parser.parse(rs.getString("history"));
                if (historyList != null) {
                    text.append(rs.getInt("win")).append(":").append(rs.getInt("lose"));
                    for (Object obj : historyList) {
                        JSONObject history = null;
                        if (obj instanceof JSONObject jo) {
                            history = jo;
                        } else if (obj instanceof String s) {
                            history = (JSONObject) parser.parse(s);
                        }
                        if (history != null) {
                            text.append("\n").append((String) history.get("event")).append(" ")
                                    .append(TimeUtil.getTimeLeft((Long) history.get("timestamp")));
                        }
                    }
                } else {
                    text.append("Thắng/Thua: ").append(rs.getInt("win")).append("/").append(rs.getInt("lose"));
                }
            } catch (Exception e) {
            }

            superRankbuilder.setInfo(text.toString());
        }

        return superRankbuilder;
    }
@SuppressWarnings("unchecked")
    public static void updatePlayer(Player player) {
        if (player != null && player.idMark.isLoadedAllDataPlayer()) {
            try {
                JSONArray dataArray = new JSONArray();
                dataArray.add(player.inventory.gold);
                dataArray.add(player.inventory.gem);
                dataArray.add(player.inventory.ruby);
                dataArray.add(player.inventory.coupon);
                dataArray.add(player.inventory.event);
                String inventory = dataArray.toJSONString();
                dataArray.clear();
                updateData(player);
                String query = "UPDATE player SET data_inventory = ? WHERE id = ?";
                AlyraManager.executeUpdate(query, inventory, player.id);
            } catch (Exception e) {
                Logger.logException(SuperRankDAO.class, e);
            }
        }
    }

    public static void loadData(Player player) {
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("SELECT * FROM super_rank WHERE player_id = ?", player.id);
            if (rs.first()) {
                player.superRank.rank = rs.getInt("rank");
                player.superRank.lastPKTime = rs.getLong("last_pk_time");
                player.superRank.lastRewardTime = rs.getLong("last_reward_time");
                player.superRank.ticket = rs.getInt("ticket");
                player.superRank.win = rs.getInt("win");
                player.superRank.lose = rs.getInt("lose");
                player.superRank.nhanGiai = rs.getInt("received") == 1;
                if (Util.isAfterMidnight(player.superRank.lastRewardTime)) {
                    player.superRank.nhanGiai = false;
                }
                player.superRank.history.clear();
                player.superRank.lastTime.clear();
                JSONParser parser = new JSONParser();
                try {
                    String hStr = rs.getString("history");
                    if (hStr != null && !hStr.isEmpty()) {
                        JSONArray history = (JSONArray) parser.parse(hStr);
                        for (Object ob : history) {
                            if (ob instanceof JSONObject obj) {
                                String event = (String) obj.get("event");
                                Long timestamp = (Long) obj.get("timestamp");
                                player.superRank.history.add(event);
                                player.superRank.lastTime.add(timestamp);
                            } else if (ob instanceof String s) {
                                JSONObject obj = (JSONObject) parser.parse(s);
                                String event = (String) obj.get("event");
                                Long timestamp = (Long) obj.get("timestamp");
                                player.superRank.history.add(event);
                                player.superRank.lastTime.add(timestamp);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            } else {
                // Người chơi mới chưa có rank: Tự động khởi tạo rank cuối bảng
                int highestRank = getCurrentHighestRank();
                player.superRank.rank = highestRank + 1;
                player.superRank.ticket = 3;
                player.superRank.win = 0;
                player.superRank.lose = 0;
                player.superRank.nhanGiai = false;
                player.superRank.lastPKTime = System.currentTimeMillis();
                player.superRank.lastRewardTime = System.currentTimeMillis();
                player.superRank.history.clear();
                player.superRank.lastTime.clear();
                insertData(player);
            }

            if (Util.isAfterMidnight(player.superRank.lastPKTime)) {
                if (player.superRank.ticket < 3) {
                    player.superRank.ticket++;
                }
                player.superRank.lastPKTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Logger.logException(SuperRankDAO.class, e);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }

    public static int getRank(int playerId) {
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("SELECT rank FROM super_rank WHERE player_id = ?", playerId);
            if (rs.first()) {
                return rs.getInt("rank");
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return getCurrentHighestRank() + 1;
    }

    public static int getCurrentHighestRank() {
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("SELECT rank FROM super_rank ORDER BY rank DESC LIMIT 1");
            if (rs.first()) {
                return rs.getInt("rank");
            }
        } catch (Exception e) {
            Logger.logException(SuperRankDAO.class, e);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public static void insertData(Player player) {
        JSONArray historyList = new JSONArray();
        for (int i = 0; i < player.superRank.history.size(); i++) {
            JSONObject history = new JSONObject();
            history.put("event", player.superRank.history.get(i));
            history.put("timestamp", (i < player.superRank.lastTime.size()) ? player.superRank.lastTime.get(i) : player.superRank.lastPKTime);
            historyList.add(history);
        }
        JSONObject info = new JSONObject();
        info.put("head", player.getHead());
        info.put("body", player.getBody());
        info.put("leg", player.getLeg());
        info.put("hp", player.nPoint.hpMax);
        info.put("dame", player.nPoint.dame);
        info.put("def", player.nPoint.def);
        try {
            AlyraManager.executeUpdate("INSERT INTO `super_rank` (`player_id`, `rank`, `name`, `info`, `last_pk_time`, `last_reward_time`, `ticket`, `win`, `lose`, `history`)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    player.id,
                    player.superRank.rank,
                    player.name,
                    info.toString(),
                    player.superRank.lastPKTime,
                    player.superRank.lastRewardTime,
                    player.superRank.ticket,
                    player.superRank.win,
                    player.superRank.lose,
                    historyList.toString());
            Logger.successln(player.name + ": Data inserted successfully....");
        } catch (Exception e) {
            Logger.logException(SuperRankDAO.class, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void updateData(Player player) {
        JSONArray historyList = new JSONArray();
        for (int i = 0; i < player.superRank.history.size(); i++) {
            JSONObject history = new JSONObject();
            history.put("event", player.superRank.history.get(i));
            history.put("timestamp", (i < player.superRank.lastTime.size()) ? player.superRank.lastTime.get(i) : player.superRank.lastPKTime);
            historyList.add(history);
        }
        JSONObject info = new JSONObject();
        info.put("head", player.getHead());
        info.put("body", player.getBody());
        info.put("leg", player.getLeg());
        info.put("hp", player.nPoint.hpMax);
        info.put("dame", player.nPoint.dame);
        info.put("def", player.nPoint.def);
        try {
            AlyraManager.executeUpdate("UPDATE `super_rank` SET `rank` = ?, `name` = ?, `info` = ?,"
                    + "last_pk_time = ?, last_reward_time = ?, `ticket` = ?, `win` = ?, `lose` = ?, `history` = ?, `received` = ? WHERE `player_id` = ?",
                    player.superRank.rank,
                    player.name,
                    info.toString(),
                    player.superRank.lastPKTime,
                    player.superRank.lastRewardTime,
                    player.superRank.ticket,
                    player.superRank.win,
                    player.superRank.lose,
                    historyList.toString(),
                    player.superRank.nhanGiai ? 1 : 0,
                    player.id);
            Logger.successln(player.name + ": Data update successfully....");
        } catch (Exception e) {
            Logger.logException(SuperRank.class, e);
        }
    }
}

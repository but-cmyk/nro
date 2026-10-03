package services.tournament;

import database.AlyraManager;
import database.daos.PlayerDAO;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import models.player.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.Client;
import server.Manager;
import services.Service;
import services.TopService;
import utils.Logger;

/**
 * Quản lý giải đấu tuần và trao thưởng TOP 1 - 5 Võ Đài Sinh Tử.
 * Chu kỳ: 1 tuần.
 * Chốt giải & reset điểm: 00:00:00 Chủ Nhật hàng tuần.
 * Phần thưởng:
 *  - TOP 1: 200 Ngọc
 *  - TOP 2: 100 Ngọc
 *  - TOP 3: 50 Ngọc
 *  - TOP 4: 30 Ngọc
 *  - TOP 5: 20 Ngọc
 */
public class DeathOrAliveArenaWeeklyService {

    private static DeathOrAliveArenaWeeklyService instance;

    private static final String DATA_FILE = "data/vdst_weekly.json";

    public static DeathOrAliveArenaWeeklyService gI() {
        if (instance == null) {
            synchronized (DeathOrAliveArenaWeeklyService.class) {
                if (instance == null) {
                    instance = new DeathOrAliveArenaWeeklyService();
                }
            }
        }
        return instance;
    }

    public static class TopRecord {
        public int rank;
        public long playerId;
        public String playerName;
        public int wins;
        public int gemReward;
        public boolean claimed;

        public TopRecord() {}

        public TopRecord(int rank, long playerId, String playerName, int wins, int gemReward, boolean claimed) {
            this.rank = rank;
            this.playerId = playerId;
            this.playerName = playerName;
            this.wins = wins;
            this.gemReward = gemReward;
            this.claimed = claimed;
        }
    }

    private long lastResetTime = 0;
    private final List<TopRecord> lastTopList = new CopyOnWriteArrayList<>();

    private DeathOrAliveArenaWeeklyService() {
    }

    public void init() {
        loadData();
        checkAndResetWeekly();
    }

    public static int getGemRewardByRank(int rank) {
        return switch (rank) {
            case 1 -> 200;
            case 2 -> 100;
            case 3 -> 50;
            case 4 -> 30;
            case 5 -> 20;
            default -> 0;
        };
    }

    /**
     * Kiểm tra và thực hiện chốt giải nếu đúng 0:00 Chủ Nhật hàng tuần.
     */
    public synchronized void checkAndResetWeekly() {
        try {
            ZonedDateTime now = ZonedDateTime.now();
            if (now.getDayOfWeek() != DayOfWeek.SUNDAY) {
                return;
            }

            long todayMidnight = now.toLocalDate().atStartOfDay(now.getZone()).toInstant().toEpochMilli();
            if (lastResetTime >= todayMidnight) {
                // Đã chốt giải Chủ Nhật tuần này rồi
                return;
            }

            rewardAndReset();
        } catch (Exception e) {
            Logger.logException(DeathOrAliveArenaWeeklyService.class, e, "Lỗi checkAndResetWeekly VDST");
        }
    }

    /**
     * Chốt giải, trao thưởng TOP 1 - 5 và reset arena_wins toàn bộ người chơi về 0.
     */
    public synchronized void rewardAndReset() {
        Logger.log("=== BẮT ĐẦU CHỐT GIẢI & TRAO THƯỞNG TOP VÕ ĐÀI SINH TỬ TUẦN ===");
        List<TopRecord> newTopList = new ArrayList<>();

        // 1. Truy vấn Top 5 player có arena_wins > 0 từ Database
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, name, arena_wins FROM player WHERE arena_wins > 0 ORDER BY arena_wins DESC, id ASC LIMIT 5");
             ResultSet rs = ps.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                TopRecord rec = new TopRecord();
                rec.rank = rank;
                rec.playerId = rs.getLong("id");
                rec.playerName = rs.getString("name");
                rec.wins = rs.getInt("arena_wins");
                rec.gemReward = getGemRewardByRank(rank);
                rec.claimed = false;
                newTopList.add(rec);
                rank++;
            }
        } catch (Exception e) {
            Logger.logException(DeathOrAliveArenaWeeklyService.class, e, "Lỗi query Top VDST tuần");
        }

        // 2. Trao thưởng cho player online ngay lập tức
        StringBuilder topAnnounce = new StringBuilder();
        for (TopRecord rec : newTopList) {
            Player pl = Client.gI().getPlayer(rec.playerId);
            if (pl != null && pl.isPl()) {
                pl.inventory.addGem(rec.gemReward);
                Service.gI().sendMoney(pl);
                Service.gI().sendThongBao(pl, "Chúc mừng bạn đạt TOP " + rec.rank + " Võ Đài Sinh Tử tuần qua và nhận được " + rec.gemReward + " ngọc!");
                PlayerDAO.updatePlayer(pl);
                rec.claimed = true;
            }
            topAnnounce.append("- TOP ").append(rec.rank).append(": ").append(rec.playerName)
                       .append(" (").append(rec.wins).append(" trận - ").append(rec.gemReward).append(" ngọc)\n");
        }

        // 3. Thông báo toàn server
        if (!newTopList.isEmpty()) {
            Service.gI().sendThongBaoAllPlayer("[VÕ ĐÀI SINH TỬ] Đã trao thưởng TOP Tuần:\n" + topAnnounce.toString() + "Toàn bộ điểm số tuần mới đã được làm mới!");
        } else {
            Service.gI().sendThongBaoAllPlayer("[VÕ ĐÀI SINH TỬ] Đã bước sang tuần mới! Toàn bộ điểm số Võ Đài đã được làm mới!");
        }

        // 4. Reset điểm arena_wins của TẤT CẢ player về 0 trong Database
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE player SET arena_wins = 0")) {
            int updated = ps.executeUpdate();
            Logger.success("Reset điểm arena_wins cho " + updated + " người chơi về 0 thành công!");
        } catch (Exception e) {
            Logger.logException(DeathOrAliveArenaWeeklyService.class, e, "Lỗi reset arena_wins trong database");
        }

        // Reset trong RAM cho toàn bộ player đang online
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null) {
                pl.arenaWins = 0;
            }
        }

        // Cập nhật lại cache bảng xếp hạng
        if (Manager.topArena != null) {
            Manager.topArena.clear();
        }
        try {
            TopService.gI().run();
        } catch (Exception ignored) {
        }

        // 5. Cập nhật dữ liệu và lưu ra file JSON
        this.lastTopList.clear();
        this.lastTopList.addAll(newTopList);
        this.lastResetTime = System.currentTimeMillis();
        saveData();

        Logger.success("=== HOÀN TẤT CHỐT GIẢI VÕ ĐÀI SINH TỬ TUẦN ===");
    }

    /**
     * Người chơi nhận quà hoặc xem bảng vàng tuần trước tại NPC Bà Hạt Mít.
     */
    public synchronized void claimReward(Player player) {
        if (player == null) {
            return;
        }
        if (lastTopList.isEmpty()) {
            Service.gI().sendThongBao(player, "Hiện chưa có kết quả Top tuần trước hoặc chưa ai ghi danh!");
            return;
        }

        TopRecord myRec = null;
        for (TopRecord rec : lastTopList) {
            if (rec.playerId == player.id) {
                myRec = rec;
                break;
            }
        }

        if (myRec == null) {
            // Không nằm trong Top 5 -> Cho xem bảng vàng tuần trước
            StringBuilder sb = new StringBuilder("BẢNG VÀNG VÕ ĐÀI SINH TỬ TUẦN TRƯỚC:\n");
            for (TopRecord rec : lastTopList) {
                sb.append("Top ").append(rec.rank).append(": ").append(rec.playerName)
                  .append(" (").append(rec.wins).append(" trận - ").append(rec.gemReward).append(" ngọc)\n");
            }
            sb.append("Con hãy tích cực thi đấu tuần này để lọt vào Top 5 nhận ngọc nhé!");
            Service.gI().sendThongBao(player, sb.toString());
            return;
        }

        if (myRec.claimed) {
            Service.gI().sendThongBao(player, "Con đã nhận phần thưởng TOP " + myRec.rank + " (" + myRec.gemReward + " ngọc) của tuần trước rồi nhé!");
            return;
        }

        // Trao thưởng cho người chơi
        myRec.claimed = true;
        player.inventory.addGem(myRec.gemReward);
        Service.gI().sendMoney(player);
        PlayerDAO.updatePlayer(player);
        saveData();

        Service.gI().sendThongBao(player, "Chúc mừng con đã xuất sắc đạt TOP " + myRec.rank + " Võ Đài Sinh Tử tuần qua!\nPhần thưởng: " + myRec.gemReward + " Ngọc đã được cộng vào tài khoản!");
        Service.gI().sendThongBaoAllPlayer(player.name + " vừa nhận thưởng TOP " + myRec.rank + " Võ Đài Sinh Tử tuần qua (" + myRec.gemReward + " ngọc)!");
    }

    public List<TopRecord> getLastTopList() {
        return lastTopList;
    }

    public long getLastResetTime() {
        return lastResetTime;
    }

    private void saveData() {
        try {
            JSONObject root = new JSONObject();
            root.put("lastResetTime", lastResetTime);

            JSONArray topArr = new JSONArray();
            for (TopRecord rec : lastTopList) {
                JSONObject obj = new JSONObject();
                obj.put("rank", rec.rank);
                obj.put("playerId", rec.playerId);
                obj.put("playerName", rec.playerName);
                obj.put("wins", rec.wins);
                obj.put("gemReward", rec.gemReward);
                obj.put("claimed", rec.claimed);
                topArr.add(obj);
            }
            root.put("topList", topArr);

            File file = new File(DATA_FILE);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(root.toJSONString());
                writer.flush();
            }
        } catch (Exception e) {
            Logger.logException(DeathOrAliveArenaWeeklyService.class, e, "Lỗi saveData VDST weekly");
        }
    }

    private void loadData() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                return;
            }

            try (FileReader reader = new FileReader(file)) {
                Object obj = JSONValue.parse(reader);
                if (obj instanceof JSONObject root) {
                    if (root.containsKey("lastResetTime")) {
                        this.lastResetTime = Long.parseLong(root.get("lastResetTime").toString());
                    }
                    if (root.containsKey("topList") && root.get("topList") instanceof JSONArray topArr) {
                        this.lastTopList.clear();
                        for (Object o : topArr) {
                            if (o instanceof JSONObject item) {
                                TopRecord rec = new TopRecord();
                                rec.rank = Integer.parseInt(item.get("rank").toString());
                                rec.playerId = Long.parseLong(item.get("playerId").toString());
                                rec.playerName = item.get("playerName").toString();
                                rec.wins = Integer.parseInt(item.get("wins").toString());
                                rec.gemReward = Integer.parseInt(item.get("gemReward").toString());
                                rec.claimed = Boolean.parseBoolean(item.get("claimed").toString());
                                this.lastTopList.add(rec);
                            }
                        }
                    }
                }
            }
            Logger.log("Đã nạp dữ liệu VDST Weekly: " + lastTopList.size() + " top record, lastResetTime = " + lastResetTime);
        } catch (Exception e) {
            Logger.logException(DeathOrAliveArenaWeeklyService.class, e, "Lỗi loadData VDST weekly");
        }
    }
}

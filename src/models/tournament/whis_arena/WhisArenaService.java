package models.tournament.whis_arena;

import consts.ConstItem;
import database.daos.PlayerDAO;
import models.item.Item;
import models.item.Item.ItemOption;
import models.map.Map;
import models.map.Zone;
import models.npc.Npc;
import models.player.Player;
import network.io.Message;
import services.ItemService;
import services.Service;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.InventoryService;
import utils.TimeUtil;
import utils.Util;

import database.AlyraManager;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WhisArenaService {

    private static WhisArenaService instance;

    public static WhisArenaService gI() {
        if (instance == null) {
            instance = new WhisArenaService();
        }
        return instance;
    }

    /**
     * Tạm thời mở 24/24 để test theo yêu cầu của User
     */
    public static boolean TEST_MODE_24_7 = true;

    public boolean isOpen() {
        if (TEST_MODE_24_7) {
            return true;
        }
        return TimeUtil.getCurrHour() == 21;
    }

    public long getRemainingMinutes() {
        if (TEST_MODE_24_7) {
            return 101;
        }
        int min = TimeUtil.getCurrMin();
        return Math.max(1, 60 - min);
    }

    // =========================================================================
    // QUẢN LÝ PHẦN THƯỞNG VÕ ĐÀI (MAP 48 - Image 4 / media_1790827215023.png)
    // =========================================================================
    private final ConcurrentHashMap<Long, List<Integer>> pendingArenaRewards = new ConcurrentHashMap<>();

    public synchronized void addReward(long playerId, int itemId) {
        List<Integer> list = pendingArenaRewards.computeIfAbsent(playerId, k -> new CopyOnWriteArrayList<>());
        list.add(itemId);
        saveRewardsToDB(playerId, list);
    }

    public synchronized void addReward(Player player, int itemId) {
        if (player != null) {
            addReward(player.id, itemId);
        }
    }

    public void addDemoRewards(Player player) {
        if (player == null)
            return;
        // Chuẩn danh sách 15 phần thưởng y hệt ảnh chụp media_1790827215023.png:
        // Phiếu giảm giá (x9), Capsule Vàng, Phiếu giảm giá, Capsule Vàng, Phiếu giảm
        // giá, Capsule Vàng, Phiếu giảm giá
        int[] demo = { 459, 459, 459, 459, 459, 459, 459, 459, 459, 574, 459, 574, 459, 574, 459 };
        for (int itemId : demo) {
            addReward(player.id, itemId);
        }
    }

    public List<Integer> getRewards(Player player) {
        if (player == null) {
            return Collections.emptyList();
        }
        List<Integer> list = pendingArenaRewards.get(player.id);
        if (list == null) {
            list = loadRewardsFromDB(player.id);
            if (list != null && !list.isEmpty()) {
                pendingArenaRewards.put(player.id, new CopyOnWriteArrayList<>(list));
            }
        }
        return list != null ? list : Collections.emptyList();
    }

    public boolean hasRewards(Player player) {
        List<Integer> rewards = getRewards(player);
        return rewards != null && !rewards.isEmpty();
    }

    public synchronized void clearRewards(Player player) {
        if (player == null) {
            return;
        }
        pendingArenaRewards.remove(player.id);
        deleteRewardsFromDB(player.id);
    }

    public synchronized void claimRewards(Player player) {
        if (player == null) {
            return;
        }
        List<Integer> rewards = getRewards(player);
        if (rewards.isEmpty()) {
            Service.gI().sendThongBao(player, "Bạn không có phần thưởng nào để nhận!");
            return;
        }

        int emptyCount = InventoryService.gI().getCountEmptyBag(player);
        if (emptyCount < rewards.size()) {
            Service.gI().sendThongBao(player,
                    "Hành trang không đủ chỗ trống! Cần ít nhất " + rewards.size() + " ô trống để nhận thưởng.");
            return;
        }

        for (int itemId : rewards) {
            Item item = ItemService.gI().createNewItem((short) itemId);
            if (item != null) {
                item.quantity = 1;
                item.itemOptions.add(new ItemOption(30, 0)); // Không thể giao dịch
                InventoryService.gI().addItemBag(player, item);
            }
        }
        InventoryService.gI().sendItemBags(player);

        clearRewards(player);
        Service.gI().sendThongBao(player, "Chúc mừng bạn đã nhận thành công toàn bộ phần thưởng Võ Đài Liên Vũ Trụ!");
    }

    public String getItemRewardName(int itemId) {
        if (itemId == 459) {
            return "Phiếu giảm giá";
        } else if (itemId == 574) {
            return "Capsule Vàng";
        }
        models.Template.ItemTemplate temp = services.ItemService.gI().getTemplate(itemId);
        if (temp != null && temp.name != null && !temp.name.isEmpty()) {
            return temp.name;
        }
        return "Vật phẩm " + itemId;
    }

    private List<Integer> loadRewardsFromDB(long playerId) {
        List<Integer> res = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con
                        .prepareStatement("SELECT `rewards` FROM `whis_arena_rewards` WHERE `player_id` = ?")) {
            ps.setLong(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String str = rs.getString("rewards");
                    if (str != null && !str.isEmpty()) {
                        org.json.simple.JSONArray arr = (org.json.simple.JSONArray) org.json.simple.JSONValue
                                .parse(str);
                        if (arr != null) {
                            for (Object obj : arr) {
                                res.add(Integer.parseInt(obj.toString()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(WhisArenaService.class, e, "Lỗi load whis_arena_rewards cho player " + playerId);
        }
        return res;
    }

    private void saveRewardsToDB(long playerId, List<Integer> list) {
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO `whis_arena_rewards` (`player_id`, `rewards`) VALUES (?, ?) "
                                + "ON DUPLICATE KEY UPDATE `rewards` = ?")) {
            org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
            for (int id : list) {
                arr.add(id);
            }
            String str = arr.toJSONString();
            ps.setLong(1, playerId);
            ps.setString(2, str);
            ps.setString(3, str);
            ps.executeUpdate();
        } catch (Exception e) {
            Logger.logException(WhisArenaService.class, e, "Lỗi save whis_arena_rewards cho player " + playerId);
        }
    }

    private void deleteRewardsFromDB(long playerId) {
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM `whis_arena_rewards` WHERE `player_id` = ?")) {
            ps.setLong(1, playerId);
            ps.executeUpdate();
        } catch (Exception e) {
            Logger.logException(WhisArenaService.class, e, "Lỗi delete whis_arena_rewards cho player " + playerId);
        }
    }

    // =========================================================================
    // MENU TẠI HÀNH TINH KAIO (MAP 48 - Image 1 & Image 4)
    // =========================================================================
    public void openMenuAtKaio(Npc npc, Player player) {
        // Tự động nạp demo thưởng cho Admin khi test nếu chưa có phần thưởng
        if (TEST_MODE_24_7 && player != null && player.isAdmin() && !hasRewards(player) && player.arenaWins == 0) {
            addDemoRewards(player);
        }

        List<Integer> rewards = getRewards(player);
        if (rewards != null && !rewards.isEmpty()) {
            // Tab Trao Thưởng chuẩn theo ảnh media_1790827215023.png
            StringBuilder sb = new StringBuilder();
            sb.append("Ngươi có phần thưởng của Võ Đài Liên Vũ Trụ:\n");
            List<String> names = new ArrayList<>();
            for (int itemId : rewards) {
                names.add(getItemRewardName(itemId));
            }
            sb.append(String.join(", ", names));
            sb.append("\nhãy chừa ô trống trong hành trang để nhận");

            npc.createOtherMenu(player, 103, sb.toString(),
                    "Hướng\ndẫn\nthêm",
                    "Nhận\nthưởng",
                    "Từ chối");
            return;
        }

        // Tab Vào Võ Đài chuẩn theo ảnh media_1790827083551.png
        String text = "Đang trong giờ thi đấu\n"
                + "Ta sẽ dẫn ngươi đến võ đài liên vũ trụ\n"
                + "tại hành tinh Vô Danh";

        npc.createOtherMenu(player, 100, text,
                "Hướng\ndẫn\nthêm",
                "OK",
                "Từ chối");
    }

    public void confirmMenuAtKaio(Npc npc, Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();
        if (indexMenu == 103) {
            // Menu nhận thưởng
            switch (select) {
                case 0 -> services.map.NpcService.gI().createTutorial(player, npc.tempId, npc.avartar,
                        consts.ConstNpc.HUONG_DAN_BILL);
                case 1 -> claimRewards(player);
                case 2 -> {
                } // Từ chối
            }
            return;
        }

        // Menu vào võ đài (index 100)
        switch (select) {
            case 0 -> services.map.NpcService.gI().createTutorial(player, npc.tempId, npc.avartar,
                    consts.ConstNpc.HUONG_DAN_BILL);
            case 1 -> {
                if (!isOpen()) {
                    Service.gI().sendThongBao(player,
                            "Võ đài Vũ Trụ chỉ mở cửa vào khung giờ 21:00 - 22:00 hàng ngày!");
                    return;
                }
                goToArena(player);
            }
            case 2 -> {
            } // Từ chối
        }
    }

    // =========================================================================
    // MENU TẠI SẢNH CHỜ VÕ ĐÀI (MAP 186 - Image 3)
    // =========================================================================
    private final java.util.Set<Long> readyPlayerIds = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public java.util.Set<Long> getReadyPlayerIds() {
        return readyPlayerIds;
    }

    public void openMenuAtArena(Npc npc, Player player) {
        long remaining = getRemainingMinutes();
        String text = "Giải đấu sẽ kết thúc sau " + remaining + " phút nữa\n"
                + "Hãy chọn Sẵn Sàng để lên Võ Đài";

        npc.createOtherMenu(player, 101, text,
                "Giải đấu\nLiên Vũ\nTrụ 6-7",
                "Danh sách\nThi Đấu",
                "Sẵn sàng",
                "Về chỗ\nThần\nVũ trụ",
                "Từ chối");
    }

    public void confirmMenuAtArena(Npc npc, Player player, int select) {
        switch (select) {
            case 0 -> goToU6Tournament(player); // Qua map Giải Đấu U6-7
            case 1 -> showLeaderboard(player); // Danh Sách Thi Đấu
            case 2 -> findMatch(player); // Sẵn sàng: Solo 1vs1 Random All BXH / AI Clone Bot
            case 3 -> ChangeMapService.gI().changeMapInYard(player, 48, -1, 350); // Quay về Kaio
            case 4 -> {
            } // Từ chối
        }
    }

    // =========================================================================
    // MENU TẠI MAP GIẢI ĐẤU LIÊN VŨ TRỤ 6-7 (MAP 187 - Image 4)
    // =========================================================================
    public void openMenuAtU6Tournament(Npc npc, Player player) {
        String text = "Giải đấu Liên Vũ Trụ 6-7\n"
                + "quy tụ nhiều cao thủ khắp vũ trụ\n"
                + "Ngươi có dám tham gia không ?\n"
                + "Sẽ có những phần thưởng hấp dẫn";

        npc.createOtherMenu(player, 102, text,
                "Quay về\nGiải đấu\nLiên Vũ Trụ",
                "Sẵn sàng",
                "Từ chối");
    }

    public void confirmMenuAtU6Tournament(Npc npc, Player player, int select) {
        switch (select) {
            case 0 -> goToArena(player); // Quay về sảnh Giải Đấu Liên Vũ Trụ (Map 186)
            case 1 -> startU6TournamentMatch(player); // Sẵn sàng: Lên đài thi đấu với boss
            case 2 -> {
            } // Từ chối
        }
    }

    // =========================================================================
    // ĐIỀU HƯỚNG SANG MAP GIẢI ĐẤU U6-7 (MAP 187)
    // =========================================================================
    public void goToU6Tournament(Player player) {
        if (player.isPKDHVT) {
            Service.gI().sendThongBao(player, "Bạn đang trong trận đấu!");
            return;
        }
        Map u6Map = MapService.gI().getMapById(187);
        if (u6Map == null) {
            u6Map = MapService.gI().getMapById(186);
        }
        if (u6Map != null && !u6Map.zones.isEmpty()) {
            Zone targetZone = getFreeZoneInMap(u6Map, player);
            if (targetZone != null) {
                ChangeMapService.gI().changeMap(player, targetZone, 255, 360);
            } else {
                ChangeMapService.gI().changeMapInYard(player, u6Map.mapId, -1, 255);
            }
        }
    }

    // =========================================================================
    // BẮT ĐẦU TRẬN ĐẤU VỚI BOSS U6 KHI BẤM "SẴN SÀNG"
    // =========================================================================
    public void startU6TournamentMatch(Player player) {
        if (player.zone == null || (player.zone.map.mapId != 187 && player.zone.map.mapId != 186)) {
            Service.gI().sendThongBao(player, "Cậu phải có mặt ở Giải Đấu mới có thể thi đấu!");
            return;
        }

        if (player.isPKDHVT) {
            Service.gI().sendThongBao(player, "Bạn đang trong trận đấu khác!");
            return;
        }

        Map targetMap = player.zone.map;
        Zone targetZone = getFreeZoneInMap(targetMap, player);
        if (targetZone == null) {
            Service.gI().sendThongBao(player, "Các sàn đài đều đang có người thi đấu, xin vui lòng chờ giây lát!");
            return;
        }

        // Hồi phục 100% HP/KI và giải trừ hồi chiêu
        Service.gI().releaseCooldownSkill(player);
        if (player.nPoint != null) {
            player.nPoint.hp = player.nPoint.hpMax;
            player.nPoint.mp = player.nPoint.mpMax;
            Service.gI().point(player);
            Service.gI().Send_Info_NV(player);
        }

        int roundToFight = Math.min(3, Math.max(0, player.whisArenaU6Round));
        String bossName = WhisArenaU6Boss.getBossName(roundToFight);
        Service.gI().sendThongBao(player,
                "Bước lên võ đài: Đối đầu với " + bossName + " (Vòng " + (roundToFight + 1) + ")!");

        WhisArenaBossMatch match = new WhisArenaBossMatch(player, targetZone);
        match.start();
    }

    // =========================================================================
    // GHÉP TRẬN 1VS1 RANDOM SOLO VỚI ALL BẢNG XẾP HẠNG (KHÔNG GIỚI HẠN TOP 5)
    // Tự động xử lý khi player đối thủ đang bận thi đấu: kích hoạt Bản Sao AI Clone
    // Bot
    // =========================================================================
    public void findMatch(Player player) {
        if (player.zone == null || player.zone.map.mapId != 186) {
            Service.gI().sendThongBao(player, "Cậu phải có mặt ở Võ Đài mới có thể thi đấu!");
            return;
        }

        if (player.isPKDHVT) {
            Service.gI().sendThongBao(player, "Bạn đang trong trận đấu!");
            return;
        }

        Zone targetZone = getFreeZone(player);
        if (targetZone == null) {
            Service.gI().sendThongBao(player, "Các sàn đài đều đang có người thi đấu, xin vui lòng chờ giây lát!");
            return;
        }

        // Thêm player vào danh sách sẵn sàng để LobbyBot có thể ghép trận
        readyPlayerIds.add(player.id);

        // 0. Ưu tiên cao nhất: Ghép với LobbyBot đang READY ở sảnh
        WhisArenaLobbyBot readyLobbyBot = WhisArenaBotManager.gI().findReadyLobbyBot();
        if (readyLobbyBot != null) {
            readyPlayerIds.remove(player.id);
            Service.gI().sendThongBao(player,
                    "Đã tìm thấy đối thủ " + readyLobbyBot.name + " trên sảnh chờ! Cùng lên võ đài thôi!");
            readyLobbyBot.startMatchWithPlayer(player);
            return;
        }

        // 1. Ưu tiên: Ghép với người chơi thật tại Map 186 đang rảnh rỗi (không bận thi
        // đấu)
        Player rivalPlayer = null;
        synchronized (readyPlayerIds) {
            for (java.util.Iterator<Long> it = readyPlayerIds.iterator(); it.hasNext();) {
                Long pid = it.next();
                if (pid != null && pid != player.id) {
                    Player pl = server.Client.gI().getPlayer(pid);
                    if (pl != null && pl.zone != null && pl.zone.map.mapId == 186 && !pl.isDie() && !pl.isPKDHVT) {
                        rivalPlayer = pl;
                        it.remove();
                        break;
                    }
                }
            }
        }

        if (rivalPlayer != null) {
            readyPlayerIds.remove(player.id);
            Service.gI().sendThongBao(player,
                    "Đã ghép đấu thành công với " + rivalPlayer.name + "! Trận đấu sắp bắt đầu.");
            WhisArenaPvPMatch pvpMatch = new WhisArenaPvPMatch(player, rivalPlayer, targetZone);
            pvpMatch.start();
            return;
        }

        // Luôn dọn player ra khỏi readyPlayerIds trước khi bước vào đấu với Bot/Clone
        readyPlayerIds.remove(player.id);

        // 2. Random Solo với ALL BẢNG XẾP HẠNG (Top 1 - 100)
        List<WhisArenaBotManager.WhisArenaTopEntry> allEntries = WhisArenaBotManager.gI().getLeaderboardEntries(player,
                100);
        if (allEntries != null && !allEntries.isEmpty()) {
            List<java.util.function.Supplier<Boolean>> matchActions = new java.util.ArrayList<>();
            List<WhisArenaBotManager.WhisArenaTopEntry> candidates = new java.util.ArrayList<>();
            for (WhisArenaBotManager.WhisArenaTopEntry entry : allEntries) {
                if (entry.id != player.id) {
                    candidates.add(entry);
                }
            }

            if (!candidates.isEmpty()) {
                WhisArenaBotManager.WhisArenaTopEntry randomEntry = candidates
                        .get(Util.nextInt(0, candidates.size() - 1));

                // Nếu trúng Bot
                WhisArenaBot bot = WhisArenaBotManager.gI().getBotById(randomEntry.id);
                if (bot != null) {
                    Service.gI().sendThongBao(player, "Đối thủ ngẫu nhiên: " + bot.name + " (Hạng " + bot.rank + ")!");
                    WhisArenaMatch match = new WhisArenaMatch(player, bot, targetZone);
                    match.start();
                    return;
                }

                // Nếu trúng Player thật trên BXH
                Player plTarget = services.tournament.SuperRankService.gI().loadPlayer(randomEntry.id);
                if (plTarget != null) {
                    Player onlinePl = server.Client.gI().getPlayer(randomEntry.id);
                    // Nếu người chơi thật này đang rảnh tại map 186 -> PvP trực tiếp
                    if (onlinePl != null && onlinePl.zone != null && onlinePl.zone.map.mapId == 186 && !onlinePl.isDie()
                            && !onlinePl.isPKDHVT) {
                        Service.gI().sendThongBao(player,
                                "Đã ghép với người chơi: " + onlinePl.name + " (Hạng " + randomEntry.rank + ")!");
                        WhisArenaPvPMatch pvpMatch = new WhisArenaPvPMatch(player, onlinePl, targetZone);
                        pvpMatch.start();
                        return;
                    }

                    // Nếu player thật đang BẬN THI ĐẤU (isPKDHVT), ở map khác hoặc offline:
                    // Tự động kích hoạt Bản Sao AI Clone Bot của họ để đấu ngay lập tức
                    WhisArenaBot cloneBot = new WhisArenaBot(
                            (int) -plTarget.id,
                            plTarget.name,
                            (byte) plTarget.gender,
                            plTarget.getHead(),
                            plTarget.getBody(),
                            plTarget.getLeg(),
                            plTarget.getFlagBag(),
                            plTarget.getAura(),
                            Math.max(10_000_000L, plTarget.nPoint.hpMax),
                            Math.max(10_000_000L, plTarget.nPoint.hpMax),
                            Math.max(10_000L, plTarget.nPoint.dame),
                            plTarget.arenaWins);
                    cloneBot.rank = randomEntry.rank;
                    String statusMsg = (onlinePl != null && onlinePl.isPKDHVT)
                            ? "Người chơi " + plTarget.name
                                    + " đang bận thi đấu, đã kích hoạt bản sao AI để so tài ngay!"
                            : "Đối thủ ngẫu nhiên: " + plTarget.name + " (Bản sao AI - Hạng " + cloneBot.rank + ")!";
                    Service.gI().sendThongBao(player, statusMsg);
                    WhisArenaMatch match = new WhisArenaMatch(player, cloneBot, targetZone);
                    match.start();
                    return;
                }
            }
        }

        // Fallback ngẫu nhiên với 1 Bot bất kỳ
        WhisArenaBot bot = WhisArenaBotManager.gI().getRandomBot();
        if (bot != null) {
            Service.gI().sendThongBao(player, "Đối thủ ngẫu nhiên: " + bot.name + " (Hạng " + bot.rank + ")!");
            WhisArenaMatch match = new WhisArenaMatch(player, bot, targetZone);
            match.start();
        } else {
            Service.gI().sendThongBao(player, "Không tìm thấy đối thủ phù hợp, hãy thử lại!");
        }
    }

    // =========================================================================
    // TÌM ZONE TRỐNG TRONG MAP 186 (ƯU TIÊN ZONE 1..N ĐỂ ZONE 0 LÀM SẢNH CHỜ)
    // =========================================================================
    public synchronized Zone getFreeZone(Player player) {
        Map map186 = MapService.gI().getMapById(186);
        return getFreeZoneInMap(map186, player);
    }

    public synchronized Zone getFreeZoneInMap(Map map, Player player) {
        if (map == null || map.zones.isEmpty()) {
            return (player != null && player.zone != null) ? player.zone : null;
        }
        // Ưu tiên 1: Dùng ngay zone hiện tại của người chơi nếu sàn đấu khu này đang
        // rảnh
        // Giúp người chơi ở yên trong khu của mình, lên sàn và xuống sàn mượt mà, không
        // bị đổi khu đột ngột gây giật!
        if (player != null && player.zone != null && player.zone.map.mapId == map.mapId && !player.zone.isCompeting) {
            return player.zone;
        }
        // Ưu tiên 2: Tìm zone hoàn toàn trống khác trong map
        for (Zone z : map.zones) {
            if (!z.isCompeting && z.getPlayers().isEmpty()) {
                return z;
            }
        }
        // Ưu tiên 3: Tìm zone có người nhưng sàn đấu chưa có ai thi đấu
        for (Zone z : map.zones) {
            if (!z.isCompeting) {
                return z;
            }
        }
        return (player != null && player.zone != null && player.zone.map.mapId == map.mapId) ? player.zone
                : map.zones.get(0);
    }

    // =========================================================================
    // BẢNG THÁCH ĐẤU (HIỂN THỊ ALL BẢNG XẾP HẠNG TOP 1 - 100)
    // =========================================================================
    public void showChallengeBoard(Player player) {
        showLeaderboard(player);
    }

    /**
     * Xử lý khiêu chiến từ Bảng Xếp Hạng (Opcode -118 từ Client Panel)
     * Cho phép thách đấu TẤT CẢ đối thủ trên BXH (Top 1 - 100), không khoanh vùng
     * top 5
     * Nếu đối thủ là player đang bận thi đấu: tự động kích hoạt Bản Sao AI để solo
     * ngay
     */
    public void competing(Player player, int targetId) {
        if (player.zone == null || player.zone.map.mapId != 186) {
            Service.gI().sendThongBao(player, "Cậu phải có mặt ở Võ Đài mới có thể thi đấu!");
            return;
        }

        if (player.isPKDHVT) {
            Service.gI().sendThongBao(player, "Bạn đang trong trận đấu khác!");
            return;
        }

        if (targetId == player.id) {
            Service.gI().sendThongBao(player, "Không thể tự khiêu chiến chính mình!");
            return;
        }

        Zone targetZone = getFreeZone(player);
        if (targetZone == null) {
            Service.gI().sendThongBao(player, "Sàn đài khu vực này đang có người thi đấu, xin chờ giây lát!");
            return;
        }

        WhisArenaBot bot = WhisArenaBotManager.gI().getBotById(targetId);
        if (bot != null) {
            // Kiểm tra nếu Bot đang có LobbyBot entity ở sảnh
            if (bot.lobbyBot != null) {
                if (bot.lobbyBot.isPKDHVT || bot.lobbyBot.botState == WhisArenaLobbyBot.BotLobbyState.FIGHTING) {
                    Service.gI().sendThongBao(player, bot.name + " đang bận thi đấu, vui lòng chọn đối thủ khác!");
                    return;
                }
                // LobbyBot rảnh -> dùng trực tiếp LobbyBot
                Service.gI().sendThongBao(player,
                        "Khiêu chiến thành công! Đối thủ: " + bot.name + " (Hạng " + bot.rank + ")!");
                bot.lobbyBot.startMatchWithPlayer(player);
                return;
            }
            // Bot không có LobbyBot -> tạo Rival bình thường
            Service.gI().sendThongBao(player,
                    "Khiêu chiến thành công! Đối thủ: " + bot.name + " (Hạng " + bot.rank + ")!");
            WhisArenaMatch match = new WhisArenaMatch(player, bot, targetZone);
            match.start();
            return;
        }

        // Nếu đối thủ là người chơi thật trên BXH
        Player rivalPlayer = services.tournament.SuperRankService.gI().loadPlayer(targetId);
        if (rivalPlayer != null) {
            Player onlineRival = server.Client.gI().getPlayer(targetId);
            // Nếu người chơi thật này đang online ở map 186 và rảnh rỗi (không bận thi đấu)
            if (onlineRival != null && onlineRival.zone != null && onlineRival.zone.map.mapId == 186
                    && !onlineRival.isDie() && !onlineRival.isPKDHVT) {
                Service.gI().sendThongBao(player, "Bắt đầu trận đấu PvP với " + onlineRival.name + "!");
                WhisArenaPvPMatch pvpMatch = new WhisArenaPvPMatch(player, onlineRival, targetZone);
                pvpMatch.start();
                return;
            }

            // Nếu người chơi thật đang BẬN THI ĐẤU (isPKDHVT), ở map khác hoặc offline:
            // Tự động kích hoạt Bản Sao AI Clone Bot để solo ngay, không bao giờ bị từ chối
            WhisArenaBot cloneBot = new WhisArenaBot(
                    (int) -rivalPlayer.id,
                    rivalPlayer.name,
                    (byte) rivalPlayer.gender,
                    rivalPlayer.getHead(),
                    rivalPlayer.getBody(),
                    rivalPlayer.getLeg(),
                    rivalPlayer.getFlagBag(),
                    rivalPlayer.getAura(),
                    Math.max(10_000_000L, rivalPlayer.nPoint.hpMax),
                    Math.max(10_000_000L, rivalPlayer.nPoint.hpMax),
                    Math.max(10_000L, rivalPlayer.nPoint.dame),
                    rivalPlayer.arenaWins);
            cloneBot.rank = WhisArenaBotManager.gI().getPlayerRank(rivalPlayer);
            String statusMsg = (onlineRival != null && onlineRival.isPKDHVT)
                    ? "Người chơi " + rivalPlayer.name + " đang bận thi đấu, đã kích hoạt bản sao để so tài ngay!"
                    : "Khiêu chiến thành công! Đối thủ: " + rivalPlayer.name + " (Bản sao AI - Hạng " + cloneBot.rank
                            + ")!";
            Service.gI().sendThongBao(player, statusMsg);
            WhisArenaMatch match = new WhisArenaMatch(player, cloneBot, targetZone);
            match.start();
            return;
        }

        Service.gI().sendThongBao(player, "Không tìm thấy thông tin đối thủ này!");
    }

    // =========================================================================
    // ĐỔI CAPSULE VÀNG KÌ BÍ (ITEM 574) - CHỈ TOP 1 ĐẾN TOP 5
    // =========================================================================
    public void exchangeCapsuleVang(Player player) {
        int rank = WhisArenaBotManager.gI().getPlayerRank(player);

        if (rank > 5) {
            Service.gI().sendThongBao(player,
                    "Chỉ Top 1 đến Top 5 Bảng Xếp Hạng mới có thể đổi Capsule Vàng Kì Bí (Hạng hiện tại của bạn: Top "
                            + rank + " - " + player.arenaWins + " điểm). Hãy tích cực lên đài thi đấu nhé!");
            return;
        }

        int cost = 50; // 50 điểm / 1 Capsule Vàng
        if (player.arenaWins < cost) {
            Service.gI().sendThongBao(player, "Bạn không đủ Điểm Võ Đài. Cần tối thiểu " + cost
                    + " điểm để đổi 1 Capsule Vàng Kì Bí (Hiện có: " + player.arenaWins + " điểm).");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang của bạn đã đầy!");
            return;
        }

        // Trừ điểm và trao quà
        player.arenaWins -= cost;
        PlayerDAO.updatePlayer(player);

        Item capsule = ItemService.gI().createNewItem((short) ConstItem.CAPSULE_VANG);
        capsule.quantity = 1;
        capsule.itemOptions.add(new ItemOption(30, 0)); // Không thể giao dịch
        InventoryService.gI().addItemBag(player, capsule);
        InventoryService.gI().sendItemBags(player);

        Service.gI().sendThongBao(player, "Chúc mừng bạn đã dùng " + cost
                + " Điểm Võ Đài đổi thành công 1 Capsule Vàng Kì Bí (Còn lại: " + player.arenaWins + " điểm).");
    }

    // =========================================================================
    // HIỂN THỊ BẢNG XẾP HẠNG TUẦN (OPCODE -96 PANEL GIỐNG BXH SỨC MẠNH)
    // =========================================================================
    public void showLeaderboard(Player player) {
        player.idMark.setMenuType(186); // Đánh dấu menuType cho Whis Arena Thách Đấu
        Message msg = null;
        try {
            int myRank = WhisArenaBotManager.gI().getPlayerRank(player);
            List<WhisArenaBotManager.WhisArenaTopEntry> list = WhisArenaBotManager.gI().getLeaderboardEntries(player,
                    100);
            if (list == null || list.isEmpty()) {
                Service.gI().sendThongBao(player, "Chưa có dữ liệu bảng xếp hạng.");
                return;
            }

            msg = new Message(-96);
            msg.writer().writeByte(0); // typeTop = 0: Mở Panel chuẩn giống Bảng Xếp Hạng Sức Mạnh
            String headerTitle = "Võ Đài Liên Vũ Trụ";
            msg.writer().writeUTF(headerTitle); // Header Panel hiển thị hạng của player xem
            msg.writer().writeByte((byte) Math.min(list.size(), 100));

            for (WhisArenaBotManager.WhisArenaTopEntry entry : list) {
                msg.writer().writeInt(entry.rank);
                msg.writer().writeInt(entry.id);
                msg.writer().writeShort(entry.head);
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1); // headICON theo chuẩn Client >= 214
                }
                msg.writer().writeShort(entry.body);
                msg.writer().writeShort(entry.leg);
                msg.writer().writeUTF(entry.name);
                msg.writer().writeUTF(entry.info1);
                msg.writer().writeUTF(entry.info2);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void goToArena(Player player) {
        Map map186 = MapService.gI().getMapById(186);
        if (map186 != null && !map186.zones.isEmpty()) {
            Zone z = map186.zones.get(0);
            ChangeMapService.gI().changeMap(player, z, 300, 360);
        } else {
            ChangeMapService.gI().changeMapInYard(player, 186, -1, 300);
        }
    }
}

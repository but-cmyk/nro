package database.daos;

import models.radar.OptionCard;
import models.radar.Card;
import database.AlyraManager;
import database.AlyraResultSet;
import consts.ConstPlayer;
import consts.ConstTask;
import data.DataGame;
import models.clan.Clan;
import models.clan.ClanMember;
import models.item.Item;
import models.item.ItemTime;
import models.npc.mabu.MabuEgg;
import models.npc.mabu.MagicTree;
import models.player.Enemy;
import models.player.Friend;
import models.player.Fusion;
import models.player.Pet;
import models.player.Player;
import models.skill.Skill;
import models.task.TaskMain;
import server.Client;
import server.Manager;
import network.session.MySession;
import models.AntiLogin;
import services.player.ClanService;
import services.player.IntrinsicService;
import services.ItemService;
import services.map.MapService;
import services.Service;
import services.TaskService;
import utils.Logger;
import utils.SkillUtil;
import utils.TimeUtil;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Template.AchievementQuest;

import utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import services.player.PlayerService;

public class NDVSqlFetcher {

    public static Player login(MySession session, AntiLogin al) {
        Player player = null;
        AlyraResultSet rsAccount = null;
        AlyraResultSet rsPlayer = null;
        Player plInGame;

        try {
            rsAccount = AlyraManager.executeQuery(
                    "SELECT account.id, account.username, account.password, account.create_time, account.update_time, account.ban, account.is_admin, account.last_time_login, account.last_time_logout, account.ip_address, account.active, account.thoi_vang, account.server_login, account.bd_player, account.is_gift_box, account.gift_time, account.cash, account.danap, account.luotquay, account.vang, account.event_point, account.vip, account.vip1, account.vip2, account.sotien, account.diem_da_nhan, account.hasReceivedVIP, account.hasReceivedVIP1, account.hasReceivedVIP2, account.lastTimeReceivedVIP, account.lastTimeReceivedVIP1, account.lastTimeReceivedVIP2, account.gioithieu, account.tichdiem, account.gmail, account.server FROM account WHERE username = ?",
                    session.uu
            );

            if (rsAccount.first()) {
                // Verify BCrypt password hash
                String storedHash = rsAccount.getString("password");
                if (!utils.PasswordUtils.verifyPassword(session.pp, storedHash)) {
                    Service.gI().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu không chính xác");
                    Service.gI().sendLoginFail(session, false);
                    al.wrong();
                    return null;
                }

                session.userId = rsAccount.getInt("account.id");

                if ((plInGame = Client.gI().getPlayerByUser(session.userId)) != null) {
                    Client.gI().kickSession(plInGame.getSession());
                    Client.gI().kickSession(session);
                    Service.gI().sendLoginFail(session, true);
                    return null;
                }

                session.isAdmin = rsAccount.getBoolean("is_admin");
                Timestamp logoutTs = rsAccount.getTimestamp("last_time_logout");
                session.lastTimeLogout = (logoutTs != null) ? logoutTs.getTime() : System.currentTimeMillis();
                session.actived = rsAccount.getBoolean("active");
                session.goldBar = rsAccount.getInt("account.thoi_vang");
                session.luotquay = rsAccount.getInt("account.luotquay");
                session.gold = rsAccount.getLong("account.vang");
                session.eventPoint = rsAccount.getInt("account.event_point");
                session.bdPlayer = rsAccount.getDouble("account.bd_player");
                session.cash = rsAccount.getInt("cash");
                session.danap = rsAccount.getInt("danap");
                session.diemReceive = rsAccount.getInt("diem_da_nhan");
                session.sotien = rsAccount.getInt("sotien");
                session.vip = rsAccount.getInt("vip");
                session.vip1 = rsAccount.getInt("vip1");
                session.vip2 = rsAccount.getInt("vip2");
                session.hasReceivedVIP = rsAccount.getBoolean("hasReceivedVIP");
                session.hasReceivedVIP1 = rsAccount.getBoolean("hasReceivedVIP1");
                session.hasReceivedVIP2 = rsAccount.getBoolean("hasReceivedVIP2");
                session.lastTimeReceivedVIP = rsAccount.getLong("lastTimeReceivedVIP");
                session.lastTimeReceivedVIP1 = rsAccount.getLong("lastTimeReceivedVIP1");
                session.lastTimeReceivedVIP2 = rsAccount.getLong("lastTimeReceivedVIP2");

                long lastTimeLogin = rsAccount.getTimestamp("last_time_login").getTime();
                long lastTimeLogout = rsAccount.getTimestamp("last_time_logout").getTime();
                long createTime = rsAccount.getTimestamp("create_time").getTime();

                int secondsPassLogin = (int) ((System.currentTimeMillis() - lastTimeLogin) / 1000);
                int secondsPassLogout = (int) ((System.currentTimeMillis() - lastTimeLogout) / 1000);
                int deltaTime = (int) ((System.currentTimeMillis() - createTime) / 1000);

                if (session.isAdmin) {
                    secondsPassLogin = 100;
                    secondsPassLogout = 100;
                }

                if (rsAccount.getBoolean("ban")) {
                    Service.gI().sendThongBaoOK(session, "Tài khoản này đang bị khóa. Liên hệ Admin để biết thêm thông tin");
                    return null;
                }

                if (Manager.DEBUG && !session.isAdmin) {
                    Service.gI().sendThongBaoOK(session, "Máy chủ đang bảo trì, vui lòng quay lại sau!");
                    return null;
                }

                if (Client.gI().getPlayerByUser(session.userId) != null) {
                    plInGame = Client.gI().getPlayerByUser(session.userId);
                    Client.gI().kickSession(plInGame.getSession());
                    Client.gI().kickSession(session);
                    Service.gI().sendThongBaoOK(session, "Ai đó đang đăng nhập tài khoản?");
                    return null;
                }

                if (Manager.SECOND_WAIT_LOGIN > 0 && secondsPassLogout < Manager.SECOND_WAIT_LOGIN) {
                    Service.gI().sendWaitToLogin(session, Manager.SECOND_WAIT_LOGIN - secondsPassLogout);
                    Service.gI().sendThongBaoOK(session, "Vui lòng chờ " + (Manager.SECOND_WAIT_LOGIN - secondsPassLogout) + "s để đăng nhập lại.");
                    return null;
                }

                // --- ĐOẠN CODE MỚI TỐI ƯU ---

                // Bước 1: Chỉ lấy ID để kiểm tra tồn tại (Siêu nhẹ)
                rsPlayer = AlyraManager.executeQuery(
                        "SELECT id FROM player WHERE account_id = ? LIMIT 1",
                        session.userId
                );

                boolean hasCharacter = rsPlayer.first();
                rsPlayer.dispose(); // Đóng kết nối kiểm tra ngay lập tức

                if (!hasCharacter) {
                    // Nếu chưa có nhân vật -> Chuyển sang tạo nhân vật
                    DataGame.sendVersionGame(session);
                    DataGame.sendDataItemBG(session);
                    Service.gI().switchToCreateChar(session);
                } else {
                    // Bước 2: Nếu đã có nhân vật -> Mới bắt đầu load Full dữ liệu (SELECT *)
                    // Kiểm tra xem có ai đang online nick này không trước khi load DB nặng
                    plInGame = Client.gI().getPlayerByUser(session.userId);
                    if (plInGame != null) {
                        Client.gI().kickSession(plInGame.getSession());
                        Client.gI().kickSession(session);
                        return null;
                    }

                    // Load dữ liệu thật
                    rsPlayer = AlyraManager.executeQuery(
                            "SELECT * FROM player WHERE account_id = ? LIMIT 1",
                            session.userId
                    );

                    if (rsPlayer.first()) {
                        if ((player = loadPlayer(rsPlayer, false)) != null) {
                            player.isPlayer = true;
                            player.deltaTime = deltaTime;
                            player.isNewMember = Util.isTimeSnak(createTime, 45);

                            try {
                                String updateQuery = "UPDATE account SET last_time_login = ?, ip_address = ? WHERE id = ?";
                                AlyraManager.executeUpdate(updateQuery, new Timestamp(System.currentTimeMillis()), session.ipAddress, session.userId);
                            } catch (Exception e) {
                                Client.gI().kickSession(session);
                            }
                        }
                    }
                }

                al.reset();

            } else {
                Service.gI().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu không chính xác");
                Service.gI().sendLoginFail(session, false);
                al.wrong();
            }

        } catch (Exception e) {
            Logger.error(session.uu);
            if (player != null) {
                player.dispose();
                player = null;
            }
            Logger.logException(NDVSqlFetcher.class, e);
        } finally {
            if (rsAccount != null) {
                rsAccount.dispose();
            }
            if (rsPlayer != null) {
                rsPlayer.dispose();
            }
        }

        return player;
    }

    public static Player loadById(long id) {
        Player player = null;
        AlyraResultSet rs = null;
        try {
            // Thực thi truy vấn SQL và lấy kết quả
            rs = AlyraManager.executeQuery("select * from player where id = ? limit 1", id);

            // Nếu có dữ liệu và dữ liệu hợp lệ
            if (rs.first()) {
                // Gọi hàm loadPlayer để xử lý dữ liệu và tạo đối tượng player
                player = loadPlayer(rs, true);
                if (player != null) {
                    player.isOffline = true;
                    player.idMark.setLoadedAllDataPlayer(true);
                }
            }
        } catch (Exception e) {
            // Nếu có lỗi, chắc chắn giải phóng tài nguyên và log lỗi
            if (player != null) {
                player.dispose();
                player = null;
            }
            Logger.logException(NDVSqlFetcher.class, e);
        } finally {
            // Đảm bảo giải phóng tài nguyên ResultSet
            if (rs != null) {
                rs.dispose();
            }
        }
        return player;
    }

    public static Player loadPlayerByName(String name) {
        Player player = null;
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("SELECT * FROM player WHERE name = ? LIMIT 1", name);
            if (rs.first()) {
                player = loadPlayer(rs, true); // Dùng isOffline = true
                if (player != null) {
                    player.isOffline = true;
                    player.idMark.setLoadedAllDataPlayer(true);
                }
            }
        } catch (Exception e) {
            Logger.logException(NDVSqlFetcher.class, e, "Lỗi khi tải player bằng tên: " + name);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return player;
    }

//    private static Player loadPlayer(AlyraResultSet rs, boolean isOffline) throws Exception {
    public static Player loadPlayer(AlyraResultSet rs, boolean isOffline) throws Exception {
        Player player = null;
        try {
            int plHp;
            int plMp;
            JSONArray dataArray;

            //base info
            player = new Player();
            player.account_id = rs.getInt("account_id"); // <-- THÊM DÒNG NÀY VÀO
            player.id = rs.getInt("id");
            player.name = rs.getString("name");
            player.head = rs.getShort("head");
            player.gender = rs.getByte("gender");
            if (player.head == -1) {
                switch (player.gender) {
                    case 0 ->
                        player.head = 64;
                    case 1 ->
                        player.head = 9;
                    case 2 ->
                        player.head = 6;
                }
            }
            player.haveTennisSpaceShip = rs.getBoolean("have_tennis_space_ship");

            int clanId = rs.getInt("clan_id");
            if (clanId != -1) {
                try {
                    Clan clan = ClanService.gI().getClanById(clanId);
                    for (ClanMember cm : clan.getMembers()) {
                        if (cm.id == player.id) {
                            if (!isOffline) {
                                clan.addMemberOnline(player);
                            }
                            player.clan = clan;
                            player.clanMember = cm;
                            break;
                        }
                    }
                } catch (Exception e) {
                    player.clan = null;
                }
            }
            dataArray = (JSONArray) JSONValue.parse(rs.getString("dien_sukien"));
            player.diemsukien = Integer.parseInt(dataArray.get(0).toString());
            player.phaobong = Integer.parseInt(dataArray.get(1).toString());
            player.lixi = Integer.parseInt(dataArray.get(2).toString());
            dataArray.clear();

            player.banhtet = rs.getLong("banhtet");
            player.banhtrung = rs.getLong("banhchung");
            //data danh hiệu shop
            dataArray = (JSONArray) JSONValue.parse(rs.getString("danh_hieu_shop"));
            int dh1 = Integer.parseInt(String.valueOf(dataArray.get(0)));
            int dh2 = Integer.parseInt(String.valueOf(dataArray.get(1)));
            int dh3 = Integer.parseInt(String.valueOf(dataArray.get(2)));
            int dh4 = Integer.parseInt(String.valueOf(dataArray.get(3)));
            int dh5 = Integer.parseInt(String.valueOf(dataArray.get(4)));
            int dh6 = Integer.parseInt(String.valueOf(dataArray.get(5)));
            int dh7 = Integer.parseInt(String.valueOf(dataArray.get(6)));
            int dh8 = Integer.parseInt(String.valueOf(dataArray.get(7)));

            player.effect.setPointDaiGiaMoiNhu(dh1);
            player.effect.setPointTrumUocRong(dh2);
            player.effect.setPointTrumSanBoss(dh3);
            player.effect.setPointThanhDapDo(dh4);
            player.effect.setPointNongDanChamChi(dh5);
            player.effect.setPointOngThanVeChai(dh6);
            player.effect.setPointBiMocSachTui(dh7);
            player.effect.setPointPhanCung(dh8);
            dataArray.clear();
            //Nhận bùa ngẫu nhiên
            dataArray = (JSONArray) JSONValue.parse(rs.getString("buarandom"));
            int b1 = Integer.parseInt(String.valueOf(dataArray.get(0)));
            int b2 = Integer.parseInt(String.valueOf(dataArray.get(1)));
            player.luotNhanBuaMienPhi = b1;
            player.diemDanhSuKien = b2;
            dataArray.clear();
            //data kim lượng
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_inventory"));
                player.inventory.gold = Long.parseLong(String.valueOf(dataArray.get(0)));
                player.inventory.gem = Integer.parseInt(String.valueOf(dataArray.get(1)));
                player.inventory.ruby = Integer.parseInt(String.valueOf(dataArray.get(2)));
                player.inventory.coupon = Integer.parseInt(String.valueOf(dataArray.get(3)));
                if (dataArray.size() >= 4) {
                    player.inventory.coupon = Integer.parseInt(String.valueOf(dataArray.get(3)));
                } else {
                    player.inventory.coupon = 0;
                }
                if (dataArray.size() >= 5 && false) {
                    player.inventory.event = Integer.parseInt(String.valueOf(dataArray.get(4)));
                } else {
                    player.inventory.event = 0;
                }
                dataArray.clear();
            } catch (Exception e) {
                JSONObject ob = (JSONObject) JSONValue.parse(rs.getString("data_inventory"));
                if (ob != null) {
                    player.inventory.gold = ob.containsKey("gold") ? Long.parseLong(String.valueOf(ob.get("gold"))) : 0L;
                    player.inventory.gem = ob.containsKey("gem") ? Integer.parseInt(String.valueOf(ob.get("gem"))) : 0;
                    player.inventory.ruby = ob.containsKey("ruby") ? Integer.parseInt(String.valueOf(ob.get("ruby"))) : 0;
                    player.inventory.coupon = 0;
                    player.inventory.event = 0;
                }
                ob.clear();
            }

            PlayerService.gI().dailyLogin(player);

            player.firstTimeLogin = rs.getTimestamp("firstTimeLogin");

            // data điểm danh
//            try {
//                Object parsed = JSONValue.parse(rs.getString("checkNhanQua"));
//                int luotNhan = 1;
//                int capsuleBang = 1;
//                LocalDateTime lastCheckIn = null;
//
//                if (parsed instanceof JSONArray arr) {
//                    if (arr.size() > 0) {
//                        luotNhan = Integer.parseInt(String.valueOf(arr.get(0)));
//                    }
//                    if (arr.size() > 1) {
//                        capsuleBang = Integer.parseInt(String.valueOf(arr.get(1)));
//                    }
//                    if (arr.size() > 2 && arr.get(2) != null) {
//                        lastCheckIn = LocalDateTime.parse(String.valueOf(arr.get(2)));
//                    }
//                }
//
//           //     player.event.luotNhanNgocMienPhi = luotNhan;
//            //    player.event.luotNhanCapsuleBang = capsuleBang;
//                player.lastCheckIn = lastCheckIn;
//            } catch (Exception e) {
//              //  player.event.luotNhanNgocMienPhi = 1;
//             //   player.event.luotNhanCapsuleBang = 1;
//                player.lastCheckIn = null;
//                System.err.println("Lỗi đọc checkNhanQua: " + e.getMessage());
//            }
            try {
                player.arenaWins = rs.getInt("arena_wins");
            } catch (Exception e) {
                player.arenaWins = 0; // Gán giá trị mặc định nếu cột chưa tồn tại hoặc có lỗi
            }
            try {
                // Lấy chuỗi JSON từ ResultSet
                String checkNhanQuaJson = rs.getString("checkNhanQua");

                // Chỉ xử lý nếu chuỗi không phải là null và không rỗng
                if (checkNhanQuaJson != null && !checkNhanQuaJson.isBlank()) {
                    Object parsed = JSONValue.parse(checkNhanQuaJson);
                    int luotNhan = 1;
                    int capsuleBang = 1;
                    LocalDateTime lastCheckIn = null;

                    if (parsed instanceof JSONArray arr) {
                        if (arr.size() > 0 && arr.get(0) != null) {
                            luotNhan = Integer.parseInt(String.valueOf(arr.get(0)));
                        }
                        if (arr.size() > 1 && arr.get(1) != null) {
                            capsuleBang = Integer.parseInt(String.valueOf(arr.get(1)));
                        }
                        if (arr.size() > 2 && arr.get(2) != null) {
                            // Thêm kiểm tra "null" dưới dạng chuỗi để an toàn hơn
                            String dateStr = String.valueOf(arr.get(2));
                            if (!"null".equalsIgnoreCase(dateStr)) {
                                lastCheckIn = LocalDateTime.parse(dateStr);
                            }
                        }
                    }
                    // player.event.luotNhanNgocMienPhi = luotNhan;
                    // player.event.luotNhanCapsuleBang = capsuleBang;
                    player.lastCheckIn = lastCheckIn;
                } else {
                    // Nếu dữ liệu là NULL hoặc rỗng, gán giá trị mặc định
                    // player.event.luotNhanNgocMienPhi = 1;
                    // player.event.luotNhanCapsuleBang = 1;
                    player.lastCheckIn = null;
                }
            } catch (Exception e) {
                // Khối catch này bây giờ chỉ xử lý các lỗi bất ngờ khác (ví dụ: JSON không hợp lệ)
                // player.event.luotNhanNgocMienPhi = 1;
                // player.event.luotNhanCapsuleBang = 1;
                player.lastCheckIn = null;
                System.err.println("Lỗi phân tích cú pháp checkNhanQua: " + e.getMessage());
            }

            //data tọa độ
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_location"));
            int mapId = Integer.parseInt(String.valueOf(dataArray.get(0)));
            player.location.x = Integer.parseInt(String.valueOf(dataArray.get(1)));
            player.location.y = Integer.parseInt(String.valueOf(dataArray.get(2)));
            player.location.lastTimeplayerMove = System.currentTimeMillis();
            if (mapId == 51 || MapService.gI().isMapDoanhTrai(mapId) || MapService.gI().isMapBlackBallWar(mapId) || MapService.gI().isMapSieuThanhThuy(mapId) || MapService.gI().isMapMabu2H(mapId)) {
                mapId = player.gender + 21;
                player.location.x = 300;
                player.location.y = 336;
            }
            if (MapService.gI().isMapMaBu(mapId)) {
                if (!TimeUtil.isMabuOpen()) {
                    mapId = player.gender + 21;
                    player.location.x = 300;
                    player.location.y = 336;
                }
            }
            if (mapId == 112) {
                player.location.y = 408;
            } else if (mapId == 129 || mapId == 113) {
                player.location.y = 360;
            }
            if (mapId == 49) {
                mapId = 45;
                player.location.x = 359;
                player.location.y = 408;
            }
            player.zone = MapService.gI().getMapCanJoin(player, mapId, -1);
            if (player.zone == null) {
                mapId = player.gender + 21;
                player.location.x = 300;
                player.location.y = 336;
                player.zone = MapService.gI().getMapCanJoin(player, mapId, -1);
            }
            player.mapIdBeforeLogout = mapId;
            dataArray.clear();

            //data chỉ số (Type-Safe DTO)
            models.player.dto.PlayerPointDTO pointDTO = models.player.dto.PlayerPointDTO.fromJson(rs.getString("data_point"));
            player.nPoint.limitPower = pointDTO.limitPower;
            player.nPoint.power = pointDTO.power;
            player.nPoint.tiemNang = pointDTO.tiemNang;
            player.nPoint.stamina = pointDTO.stamina;
            player.nPoint.maxStamina = pointDTO.maxStamina;
            player.nPoint.hpg = pointDTO.hpg;
            player.nPoint.mpg = pointDTO.mpg;
            player.nPoint.dameg = pointDTO.dameg;
            player.nPoint.defg = pointDTO.defg;
            player.nPoint.critg = pointDTO.critg;
            plHp = pointDTO.hp;
            plMp = pointDTO.mp;

            //data đậu thần (Type-Safe DTO)
            models.player.dto.MagicTreeDTO treeDTO = models.player.dto.MagicTreeDTO.fromJson(rs.getString("data_magic_tree"));
            player.magicTree = new MagicTree(player, treeDTO.level, treeDTO.currPeas, treeDTO.lastTimeHarvest, treeDTO.isUpgrade, treeDTO.lastTimeUpgrade);

            //data phần thưởng sao đen
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_black_ball"));
            JSONArray dataBlackBall;
            for (int i = 0; i < dataArray.size(); i++) {
                dataBlackBall = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                player.rewardBlackBall.timeOutOfDateReward[i] = Long.parseLong(String.valueOf(dataBlackBall.get(0)));
                player.rewardBlackBall.lastTimeGetReward[i] = Long.parseLong(String.valueOf(dataBlackBall.get(1)));
                try {
                    player.rewardBlackBall.quantilyBlackBall[i] = dataBlackBall.get(2) != null ? Integer.parseInt(String.valueOf(dataBlackBall.get(2))) : 0;
                } catch (NumberFormatException e) {
                    player.rewardBlackBall.quantilyBlackBall[i] = player.rewardBlackBall.timeOutOfDateReward[i] != 0 ? 1 : 0;
                }
                dataBlackBall.clear();
            }
            dataArray.clear();

            //data body
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBody.add(item);
            }
            if (player.inventory.itemsBody.size() == 10) {
                player.inventory.itemsBody.add(ItemService.gI().createItemNull());
            }
            if (player.inventory.itemsBody.size() == 11) {
                player.inventory.itemsBody.add(ItemService.gI().createItemNull());
            }
//            if (player.inventory.itemsBody.size() == 12) {
//                player.inventory.itemsBody.add(ItemService.gI().createItemNull());
//            }
            dataArray.clear();

            //data bag
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_bag"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBag.add(item);
            }
            dataArray.clear();

            //data box
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_box"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    if (item.template.id == 2132) {
                        if (item.createTime >= 1710435600000L && item.createTime <= 1711645199000L) {
                            item = ItemService.gI().createItemNull();
                        }
                    }
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBox.add(item);
            }
            dataArray.clear();

            //data box lucky round
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_box_lucky_round"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    player.inventory.itemsBoxCrackBall.add(item);
                }
            }
            dataArray.clear();

            //data item da ban
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_daban"));
            for (int i = 0; i < dataArray.size() && i < 20; i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    // 26/06/2023 - Giảm Ngày Trong Shop
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    if (item.template.id == 2132) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                        try {
                            Date currentDate = new Date(item.createTime);
                            Date startDate = formatter.parse("15/03/2024");
                            Date endDate = formatter.parse("28/03/2024");
                            if (currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
                                System.out.println("Thu hồi cải trang rồng lộn bug.");
                                item = ItemService.gI().createItemNull();
                            }
                        } catch (ParseException e) {
                        }
                    }
                    if (!ItemService.gI().isOutOfDateTime(item)) {
                        player.inventory.itemsDaBan.add(item);
                    }
                    //player.inventory.itemsDaBan.add(item);
                }
            }
            dataArray.clear();

            //data friends
            dataArray = (JSONArray) JSONValue.parse(rs.getString("friends"));
            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray dataFE = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    Friend friend = new Friend();
                    friend.id = Integer.parseInt(String.valueOf(dataFE.get(0)));
                    friend.name = String.valueOf(dataFE.get(1));
                    friend.head = Short.parseShort(String.valueOf(dataFE.get(2)));
                    friend.body = Short.parseShort(String.valueOf(dataFE.get(3)));
                    friend.leg = Short.parseShort(String.valueOf(dataFE.get(4)));
                    friend.bag = Byte.parseByte(String.valueOf(dataFE.get(5)));
                    friend.power = Long.parseLong(String.valueOf(dataFE.get(6)));
                    player.friends.add(friend);
                    dataFE.clear();
                }
                dataArray.clear();
            }

            //data enemies
            dataArray = (JSONArray) JSONValue.parse(rs.getString("enemies"));
            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray dataFE = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    Enemy enemy = new Enemy();
                    enemy.id = Integer.parseInt(String.valueOf(dataFE.get(0)));
                    enemy.name = String.valueOf(dataFE.get(1));
                    enemy.head = Short.parseShort(String.valueOf(dataFE.get(2)));
                    enemy.body = Short.parseShort(String.valueOf(dataFE.get(3)));
                    enemy.leg = Short.parseShort(String.valueOf(dataFE.get(4)));
                    enemy.bag = Byte.parseByte(String.valueOf(dataFE.get(5)));
                    enemy.power = Long.parseLong(String.valueOf(dataFE.get(6)));
                    player.enemies.add(enemy);
                    dataFE.clear();
                }
                dataArray.clear();
            }

            //data nội tại
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_intrinsic"));
            byte intrinsicId = Byte.parseByte(dataArray.get(0).toString());
            player.playerIntrinsic.intrinsic = IntrinsicService.gI().getIntrinsicById(intrinsicId);
            player.playerIntrinsic.intrinsic.param1 = Short.parseShort(dataArray.get(1).toString());
            player.playerIntrinsic.countOpen = Byte.parseByte(dataArray.get(2).toString());
            player.playerIntrinsic.intrinsic.param2 = Short.parseShort(dataArray.get(3).toString());
            dataArray.clear();

            // data item time Dev Minh Dũng
            dataArray = (JSONArray) JSONValue.parse(rs.getString("devndung_time"));
            int timeco4la = Integer.parseInt(String.valueOf(dataArray.get(0)));
            int timechung = Integer.parseInt(String.valueOf(dataArray.get(1)));
            int timetet = Integer.parseInt(String.valueOf(dataArray.get(2)));
            int nguqua = Integer.parseInt(String.valueOf(dataArray.get(3)));
            player.itemTime.lastTimeUseCo4La = System.currentTimeMillis() - (ItemTime.TIME_CO - timeco4la);
            player.itemTime.banhchunglastTime = System.currentTimeMillis() - (ItemTime.DEVNDUNG - timechung);
            player.itemTime.banhtetlastTime = System.currentTimeMillis() - (ItemTime.DEVNDUNG - timetet);
            player.itemTime.nguqualastTime = System.currentTimeMillis() - (ItemTime.DEVNDUNG - nguqua);
            player.itemTime.isUseCo4La = timeco4la != 0;
            player.itemTime.banhchung = timechung != 0;
            player.itemTime.banhtet = timetet != 0;
            player.itemTime.nguqua = nguqua != 0;

            dataArray.clear();

            //data item time
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_item_time"));
            int timeUseTDLT = 0;
            int timeOpenPower = 0;
            int timeMayDo = 0;
            int timeMayDo2 = 0;
            int timeMeal = 0;
            int iconMeal = 0;
            int timeUseCMS = 0;
            int timeUseGTPT = 0;
            int timeUseDK = 0;
            int timeUseRX = 0;
            int timeMeal2 = 0;
            int iconMeal2 = 0;
            int timeUseNCD = 0;
            int timeKhauTrang = 0;
            int timeTnDeTu = 0;
            int timehoadao = 0;
            int timehoamai = 0;
            int timeBuaTnSm = 0;
            int timeBoHuyet = Integer.parseInt(String.valueOf(dataArray.get(0)));
            int timeBoHuyet2 = Integer.parseInt(String.valueOf(dataArray.get(1)));
            int timeBoKhi = Integer.parseInt(String.valueOf(dataArray.get(2)));
            int timeBoKhi2 = Integer.parseInt(String.valueOf(dataArray.get(3)));
            int timeGiapXen = Integer.parseInt(String.valueOf(dataArray.get(4)));
            int timeGiapXen2 = Integer.parseInt(String.valueOf(dataArray.get(5)));
            int timeCuongNo = Integer.parseInt(String.valueOf(dataArray.get(6)));
            int timeCuongNo2 = Integer.parseInt(String.valueOf(dataArray.get(7)));
            int timeAnDanh = Integer.parseInt(String.valueOf(dataArray.get(8)));
            int timeAnDanh2 = Integer.parseInt(String.valueOf(dataArray.get(9)));
            if (dataArray.size() > 10) {
                timeOpenPower = Integer.parseInt(String.valueOf(dataArray.get(10)));
            }
            if (dataArray.size() > 11) {
                timeMayDo = Integer.parseInt(String.valueOf(dataArray.get(11)));
            }
            if (dataArray.size() > 12) {
                timeMayDo2 = Integer.parseInt(String.valueOf(dataArray.get(12)));
            }
            if (dataArray.size() > 13) {
            }
            if (dataArray.size() > 14) {
                timeMeal = Integer.parseInt(String.valueOf(dataArray.get(14)));
            }
            if (dataArray.size() > 15) {
                iconMeal = Integer.parseInt(String.valueOf(dataArray.get(15)));
            }
            if (dataArray.size() > 16) {
                timeUseTDLT = Integer.parseInt(String.valueOf(dataArray.get(16)));
            }
            if (dataArray.size() > 17) {
                timeUseCMS = Integer.parseInt(String.valueOf(dataArray.get(17)));
            }
            if (dataArray.size() > 18) {
                timeUseGTPT = Integer.parseInt(String.valueOf(dataArray.get(18)));
            }
            if (dataArray.size() > 19) {
                timeUseDK = Integer.parseInt(String.valueOf(dataArray.get(19)));
            }
            if (dataArray.size() > 20) {
                timeUseRX = Integer.parseInt(String.valueOf(dataArray.get(20)));
            }
            if (dataArray.size() > 21) {
                timeMeal2 = Integer.parseInt(String.valueOf(dataArray.get(21)));
            }
            if (dataArray.size() > 22) {
                iconMeal2 = Integer.parseInt(String.valueOf(dataArray.get(22)));
            }
            if (dataArray.size() > 23) {
            }
            if (dataArray.size() > 24) {
                timeUseNCD = Integer.parseInt(String.valueOf(dataArray.get(24)));
            }
            if (dataArray.size() > 25) {
            }
            if (dataArray.size() > 26) {
            }
            if (dataArray.size() > 27) {
                timeKhauTrang = Integer.parseInt(dataArray.get(27).toString());
            }
            if (dataArray.size() > 28) {
                timeTnDeTu = Integer.parseInt(dataArray.get(28).toString());
            }
            if (dataArray.size() > 29) {
                timehoadao = Integer.parseInt(String.valueOf(dataArray.get(29)));
            }
            if (dataArray.size() > 30) {
                timehoamai = Integer.parseInt(String.valueOf(dataArray.get(30)));
            }
            if (dataArray.size() > 31) {
                timeBuaTnSm = Integer.parseInt(String.valueOf(dataArray.get(31)));
            }
            player.itemTime.lastTimeBoHuyet = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoHuyet);
            player.itemTime.lastTimeBoKhi = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoKhi);
            player.itemTime.lastTimeGiapXen = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeGiapXen);
            player.itemTime.lastTimeCuongNo = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeCuongNo);
            player.itemTime.lastTimeAnDanh = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeAnDanh);
            player.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoHuyet2);
            player.itemTime.lastTimeBoKhi2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoKhi2);
            player.itemTime.lastTimeGiapXen2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeGiapXen2);
            player.itemTime.lastTimeCuongNo2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeCuongNo2);
            player.itemTime.lastTimeAnDanh2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeAnDanh2);
            player.itemTime.lastTimeOpenPower = System.currentTimeMillis() - (ItemTime.TIME_OPEN_POWER - timeOpenPower);
            player.itemTime.lastTimeUseMayDo = System.currentTimeMillis() - (ItemTime.TIME_MAY_DO - timeMayDo);
            player.itemTime.lastTimeUseMayDo2 = System.currentTimeMillis() - (ItemTime.TIME_MAY_DO2 - timeMayDo2);
            player.itemTime.lastTimeEatMeal = System.currentTimeMillis() - (ItemTime.TIME_EAT_MEAL - timeMeal);
            player.itemTime.timeTDLT = timeUseTDLT * 60 * 1000;
            player.itemTime.lastTimeUseTDLT = System.currentTimeMillis();
            player.itemTime.lastTimeUseCMS = System.currentTimeMillis() - (ItemTime.TIME_CMS - timeUseCMS);
            player.itemTime.lastTimeUseGTPT = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeUseGTPT);
            player.itemTime.lastTimeUseDK = System.currentTimeMillis() - (ItemTime.TIME_DK - timeUseDK);
            player.itemTime.timeRX = timeUseRX * 60 * 1000;
            player.itemTime.lastTimeUseRX = System.currentTimeMillis();
            player.itemTime.lastTimeEatMeal2 = System.currentTimeMillis() - (ItemTime.TIME_EAT_MEAL - timeMeal2);
            player.itemTime.lastTimeUseNCD = System.currentTimeMillis() - (ItemTime.TIME_NCD - timeUseNCD);
            player.itemTime.lastTimeBoHuyet = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoHuyet);
            player.itemTime.lastTimeKhauTrang = System.currentTimeMillis() - (ItemTime.TIME_MAY_DO - timeKhauTrang);
            player.itemTime.lastTimeTnDeTu = System.currentTimeMillis() - (ItemTime.TIME_MAY_DO - timeTnDeTu);
            player.itemTime.lastTimeXimuoihoadao = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timehoadao);
            player.itemTime.lastTimeXimuoihoamai = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timehoamai);
            player.itemTime.lastTimeBuaTNSM = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBuaTnSm);

            player.itemTime.iconMeal = iconMeal;
            player.itemTime.isEatMeal = timeMeal != 0;
            player.itemTime.isUseBoHuyet = timeBoHuyet != 0;
            player.itemTime.isUseBoKhi = timeBoKhi != 0;
            player.itemTime.isUseGiapXen = timeGiapXen != 0;
            player.itemTime.isUseCuongNo = timeCuongNo != 0;
            player.itemTime.isUseAnDanh = timeAnDanh != 0;
            player.itemTime.isUseBoHuyet2 = timeBoHuyet2 != 0;
            player.itemTime.isUseBoKhi2 = timeBoKhi2 != 0;
            player.itemTime.isUseGiapXen2 = timeGiapXen2 != 0;
            player.itemTime.isUseCuongNo2 = timeCuongNo2 != 0;
            player.itemTime.isUseAnDanh2 = timeAnDanh2 != 0;
            player.itemTime.isOpenPower = timeOpenPower != 0;
            player.itemTime.isUseMayDo = timeMayDo != 0;
            player.itemTime.isUseMayDo2 = timeMayDo2 != 0;
            player.itemTime.isUseTDLT = timeUseTDLT != 0;
            player.itemTime.isUseCMS = timeUseCMS != 0;
            player.itemTime.isUseGTPT = timeUseGTPT != 0;
            player.itemTime.isUseDK = timeUseDK != 0;
            player.itemTime.isUseRX = timeUseRX != 0;
            player.itemTime.iconMeal2 = iconMeal2;
            player.itemTime.isEatMeal2 = timeMeal2 != 0;
            player.itemTime.isUseNCD = timeUseNCD != 0;
            player.itemTime.isKhauTrang = timeKhauTrang != 0;
            player.itemTime.isTnDeTu = timeTnDeTu != 0;
            player.itemTime.isXimuoihoadao = timehoadao != 0;
            player.itemTime.isXimuoihoamai = timehoamai != 0;
            player.itemTime.isBuaTNSM = timeBuaTnSm != 0;
            dataArray.clear();

            //data nhiệm vụ
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_task"));
            int taskId = Integer.parseInt(String.valueOf(dataArray.get(0)));
            TaskMain taskMain = TaskService.gI().getTaskMainById(player, taskId);
            int subIndex = Integer.parseInt(String.valueOf(dataArray.get(1)));
            if (subIndex < 0) {
                subIndex = 0;
            }
            if (taskMain != null && taskMain.subTasks != null && !taskMain.subTasks.isEmpty()) {
                if (subIndex >= taskMain.subTasks.size()) {
                    subIndex = taskMain.subTasks.size() - 1;
                }
                taskMain.index = subIndex;
                taskMain.subTasks.get(subIndex).count = Short.parseShort(String.valueOf(dataArray.get(2)));
            }
            if (dataArray.size() > 3) {
                taskMain.lastTime = Long.parseLong(String.valueOf(dataArray.get(3)));
            } else {
                taskMain.lastTime = System.currentTimeMillis();
            }
            player.playerTask.taskMain = taskMain;
            TaskService.gI().checkDoneTaskPower(player, player.nPoint.power);
            dataArray.clear();

            //data nhiệm vụ hàng ngày
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_side_task"));
            String format = "dd-MM-yyyy";
            long receivedTime = Long.parseLong(String.valueOf(dataArray.get(1)));
            Date date = new Date(receivedTime);
            
            player.playerTask.sideTask.template = TaskService.gI().getSideTaskTemplateById(Integer.parseInt(String.valueOf(dataArray.get(0))));
            player.playerTask.sideTask.count = Integer.parseInt(String.valueOf(dataArray.get(2)));
            player.playerTask.sideTask.maxCount = Integer.parseInt(String.valueOf(dataArray.get(3)));
            player.playerTask.sideTask.level = Integer.parseInt(String.valueOf(dataArray.get(5)));
            player.playerTask.sideTask.receivedTime = receivedTime;
            
            if (dataArray.size() > 6) {
                player.playerTask.sideTask.lastTimeCancel = Long.parseLong(String.valueOf(dataArray.get(6)));
            }
            if (dataArray.size() > 7) {
                player.playerTask.sideTask.cancelCount = Integer.parseInt(String.valueOf(dataArray.get(7)));
            }
            
            if (TimeUtil.formatTime(date, format).equals(TimeUtil.formatTime(new Date(), format))) {
                player.playerTask.sideTask.leftTask = Integer.parseInt(String.valueOf(dataArray.get(4)));
            } else {
                player.playerTask.sideTask.leftTask = ConstTask.MAX_SIDE_TASK;
                player.playerTask.sideTask.cancelCount = 0;
            }

            //data trứng bư
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_mabu_egg"));
            if (!dataArray.isEmpty()) {
                player.mabuEgg = new MabuEgg(player, Long.parseLong(String.valueOf(dataArray.get(0))),
                        Long.parseLong(String.valueOf(dataArray.get(1))));
            }
            dataArray.clear();

            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_danh_hieu"));
            if (dataArray != null && !dataArray.isEmpty()) {
                String Reset = "dd-MM-yyyy";
                long Time = Long.parseLong(String.valueOf(dataArray.get(1)));
                Date datatime = new Date(Time);

                // Các tiến độ tích lũy trọn đời LUÔN LUÔN được bảo toàn, không bao giờ bị xóa qua ngày:
                if (dataArray.size() > 2) {
                    player.playerTask.taskdh.Shenron = Integer.parseInt(String.valueOf(dataArray.get(2)));
                }
                if (dataArray.size() > 3) {
                    player.playerTask.taskdh.Hagucboss = Integer.parseInt(String.valueOf(dataArray.get(3)));
                }
                if (dataArray.size() > 5) {
                    player.playerTask.taskdh.SieuHang = Integer.parseInt(String.valueOf(dataArray.get(5)));
                }
                if (dataArray.size() > 6) {
                    player.playerTask.taskdh.TaskBoMong = Integer.parseInt(String.valueOf(dataArray.get(6)));
                }
                if (dataArray.size() > 7) {
                    player.playerTask.taskdh.ChoSuong = Integer.parseInt(String.valueOf(dataArray.get(7)));
                }
                if (dataArray.size() > 8) {
                    player.playerTask.taskdh.ChoNuoc = Integer.parseInt(String.valueOf(dataArray.get(8)));
                }
                if (dataArray.size() > 10) {
                    player.playerTask.taskdh.AnTrom = Integer.parseInt(String.valueOf(dataArray.get(10)));
                }
                if (dataArray.size() > 11) {
                    player.playerTask.taskdh.ODo = Integer.parseInt(String.valueOf(dataArray.get(11)));
                }

                // Các bộ đếm nhiệm vụ "Trong Ngày":
                if (TimeUtil.formatTime(datatime, Reset).equals(TimeUtil.formatTime(new Date(), Reset))) {
                    player.playerTask.taskdh.Nap = Integer.parseInt(String.valueOf(dataArray.get(0)));
                    if (dataArray.size() > 4) {
                        player.playerTask.taskdh.DapDo = Integer.parseInt(String.valueOf(dataArray.get(4)));
                    }
                    if (dataArray.size() > 9) {
                        player.playerTask.taskdh.NhatDo = Integer.parseInt(String.valueOf(dataArray.get(9)));
                    }
                    if (dataArray.size() > 12) {
                        player.playerTask.taskdh.DungLoa = Integer.parseInt(String.valueOf(dataArray.get(12)));
                    }
                    player.playerTask.taskdh.ResetTime = Time;
                } else {
                    player.playerTask.taskdh.Nap = 0;
                    player.playerTask.taskdh.DapDo = 0;
                    player.playerTask.taskdh.NhatDo = 0;
                    player.playerTask.taskdh.DungLoa = 0;
                    player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
                }
            }
            dataArray.clear();

            //data bùa
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_charm"));
            player.charms.tdTriTue = Long.parseLong(String.valueOf(dataArray.get(0)));
            player.charms.tdManhMe = Long.parseLong(String.valueOf(dataArray.get(1)));
            player.charms.tdDaTrau = Long.parseLong(String.valueOf(dataArray.get(2)));
            player.charms.tdOaiHung = Long.parseLong(String.valueOf(dataArray.get(3)));
            player.charms.tdBatTu = Long.parseLong(String.valueOf(dataArray.get(4)));
            player.charms.tdDeoDai = Long.parseLong(String.valueOf(dataArray.get(5)));
            player.charms.tdThuHut = Long.parseLong(String.valueOf(dataArray.get(6)));
            player.charms.tdDeTu = Long.parseLong(String.valueOf(dataArray.get(7)));
            player.charms.tdTriTue3 = Long.parseLong(String.valueOf(dataArray.get(8)));
            player.charms.tdTriTue4 = Long.parseLong(String.valueOf(dataArray.get(9)));
            dataArray.clear();

            //data skill
            dataArray = (JSONArray) JSONValue.parse(rs.getString("skills"));
            for (int i = 0; i < dataArray.size(); i++) {
                JSONArray dataSkill = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                int tempId = Integer.parseInt(String.valueOf(dataSkill.get(0)));
                byte point = Byte.parseByte(String.valueOf(dataSkill.get(1)));
                Skill skill;
                if (point != 0) {
                    skill = SkillUtil.createSkill(tempId, point);
                } else {
                    skill = SkillUtil.createSkillLevel0(tempId);
                }
                skill.lastTimeUseThisSkill = Long.parseLong(String.valueOf(dataSkill.get(2)));
                if (dataSkill.size() > 3) {
                    skill.currLevel = Short.parseShort(String.valueOf(dataSkill.get(3)));
                }
                player.playerSkill.skills.add(skill);
            }
            dataArray.clear();

            //data skill shortcut
            dataArray = (JSONArray) JSONValue.parse(rs.getString("skills_shortcut"));
            for (int i = 0; i < dataArray.size(); i++) {
                player.playerSkill.skillShortCut[i] = Byte.parseByte(String.valueOf(dataArray.get(i)));
            }

            for (int i : player.playerSkill.skillShortCut) {
                if (player.playerSkill.getSkillbyId(i) != null && player.playerSkill.getSkillbyId(i).damage > 0) {
                    player.playerSkill.skillSelect = player.playerSkill.getSkillbyId(i);
                    break;
                }
            }
            if (player.playerSkill.skillSelect == null) {
                player.playerSkill.skillSelect = player.playerSkill.getSkillbyId(player.gender == ConstPlayer.TRAI_DAT
                        ? Skill.DRAGON : (player.gender == ConstPlayer.NAMEC ? Skill.DEMON : Skill.GALICK));
            }
            dataArray.clear();

            //notify
            player.notify = rs.getString("notify");

            //data pet
            JSONArray petData = (JSONArray) JSONValue.parse(rs.getString("pet"));
            if (petData != null && !petData.isEmpty()) {
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(0)));
                Pet pet = new Pet(player);
                pet.id = -player.id;
                pet.typePet = Byte.parseByte(String.valueOf(dataArray.get(0)));
                if (pet.typePet > 1) {
                    pet.typePet = 1;
                }
                pet.gender = Byte.parseByte(String.valueOf(dataArray.get(1)));
                pet.name = String.valueOf(dataArray.get(2));
                player.fusion.typeFusion = Byte.parseByte(String.valueOf(dataArray.get(3)));
                player.fusion.lastTimeFusion = System.currentTimeMillis()
                        - (Fusion.TIME_FUSION - Integer.parseInt(String.valueOf(dataArray.get(4))));
                pet.status = Byte.parseByte(String.valueOf(dataArray.get(5)));

                //data chỉ số
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(1)));
                pet.nPoint.limitPower = Byte.parseByte(String.valueOf(dataArray.get(0)));
                pet.nPoint.power = Long.parseLong(String.valueOf(dataArray.get(1)));
                pet.nPoint.tiemNang = Long.parseLong(String.valueOf(dataArray.get(2)));
                pet.nPoint.stamina = Short.parseShort(String.valueOf(dataArray.get(3)));
                pet.nPoint.maxStamina = Short.parseShort(String.valueOf(dataArray.get(4)));
                pet.nPoint.hpg = Integer.parseInt(String.valueOf(dataArray.get(5)));
                pet.nPoint.mpg = Integer.parseInt(String.valueOf(dataArray.get(6)));
                pet.nPoint.dameg = Integer.parseInt(String.valueOf(dataArray.get(7)));
                pet.nPoint.defg = Integer.parseInt(String.valueOf(dataArray.get(8)));
                pet.nPoint.critg = Integer.parseInt(String.valueOf(dataArray.get(9)));
                int hp = Integer.parseInt(String.valueOf(dataArray.get(10)));
                int mp = Integer.parseInt(String.valueOf(dataArray.get(11)));

                //data body
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(2)));
                for (int i = 0; i < dataArray.size(); i++) {
                    Item item;
                    JSONArray dataItem = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                        JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                        for (int j = 0; j < options.size(); j++) {
                            JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                            item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                        }
                        item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                        if (item.template.id == 2132) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                Date currentDate = new Date(item.createTime);
                                Date startDate = formatter.parse("15/03/2024");
                                Date endDate = formatter.parse("28/03/2024");
                                if (currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
                                    System.out.println("Thu hồi cải trang rồng lộn bug.");
                                    item = ItemService.gI().createItemNull();
                                }
                            } catch (ParseException e) {
                            }
                        }
                        if (ItemService.gI().isOutOfDateTime(item)) {
                            item = ItemService.gI().createItemNull();
                        }
                    } else {
                        item = ItemService.gI().createItemNull();
                    }
                    pet.inventory.itemsBody.add(item);
                }

                //data skills
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(3)));
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray skillTemp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    int tempId = Integer.parseInt(String.valueOf(skillTemp.get(0)));
                    byte point = Byte.parseByte(String.valueOf(skillTemp.get(1)));
                    Skill skill;
                    if (point != 0) {
                        skill = SkillUtil.createSkill(tempId, point);
                    } else {
                        skill = SkillUtil.createSkillLevel0(tempId);
                    }
                    if (skillTemp.size() > 3) {
                        skill.lastTimeUseThisSkill = Long.parseLong(String.valueOf(skillTemp.get(2)));
                    }
                    if (skillTemp.size() > 3) {
                        skill.currLevel = Short.parseShort(String.valueOf(skillTemp.get(3)));
                    }
                    switch (skill.template.id) {
                        case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC ->
                            skill.coolDown = 1000;
                    }
                    pet.playerSkill.skills.add(skill);
                }
                if (pet.playerSkill.skills.size() < 5) {
                    pet.playerSkill.skills.add(4, SkillUtil.createSkillLevel0(-1));
                }
                if (pet.playerSkill.skills.size() < 6) {
                    pet.playerSkill.skills.add(5, SkillUtil.createSkillLevel0(-1));
                }
                if (pet.playerSkill.skills.size() < 7) {
                    pet.playerSkill.skills.add(6, SkillUtil.createSkillLevel0(-1));
                }
                pet.nPoint.hp = hp;
                pet.nPoint.mp = mp;
                player.pet = pet;
            }

            //Data bảo vệ tài khoản
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("baovetaikhoan"));
                player.mbv = Integer.parseInt(dataArray.get(0).toString());
                player.baovetaikhoan = Boolean.parseBoolean(dataArray.get(1).toString());
                player.mbvtime = Long.parseLong(dataArray.get(2).toString());
            } catch (Exception e) {
                player.mbv = 0;
                player.baovetaikhoan = false;
                player.mbvtime = System.currentTimeMillis();
            }

            // data rada card
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_card"));
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject obj = (JSONObject) dataArray.get(i);
                player.Cards.add(new Card(Short.parseShort(obj.get("id").toString()), Byte.parseByte(obj.get("amount").toString()), Byte.parseByte(obj.get("max").toString()), Byte.parseByte(obj.get("level").toString()), loadOptionCard((JSONArray) JSONValue.parse(obj.get("option").toString())), Byte.parseByte(obj.get("used").toString())));
            }
            dataArray.clear();

            //data PK Commeson
            player.lastPkCommesonTime = rs.getLong("lasttimepkcommeson");

            //Data BDKB
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("bandokhobau"));
                player.timesPerDayBDKB = Integer.parseInt(dataArray.get(0).toString());
                player.lastTimeJoinBDKB = Long.parseLong(dataArray.get(1).toString());
            } catch (Exception e) {
                player.timesPerDayBDKB = 0;
                player.lastTimeJoinBDKB = System.currentTimeMillis();
            }

            //Data doanh trại
            try {
                player.lastTimeJoinDT = rs.getLong("doanhtrai");
            } catch (Exception e) {
                player.lastTimeJoinDT = 0;
            }

            //Data CDRD
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("conduongrandoc"));
                player.joinCDRD = Boolean.parseBoolean(dataArray.get(0).toString());
                player.lastTimeJoinCDRD = Long.parseLong(dataArray.get(1).toString());
                player.talkToThuongDe = Boolean.parseBoolean(dataArray.get(2).toString());
                player.talkToThanMeo = Boolean.parseBoolean(dataArray.get(2).toString());
                if (player.clan.ConDuongRanDoc == null || player.lastTimeJoinCDRD != player.clan.lastTimeOpenConDuongRanDoc) {
                    player.joinCDRD = false;
                    player.talkToThuongDe = false;
                    player.talkToThanMeo = false;
                }
            } catch (Exception e) {
                player.joinCDRD = false;
                player.lastTimeJoinCDRD = 0;
                player.talkToThuongDe = false;
                player.talkToThanMeo = false;
            }

            // Sư phụ không tấn công
            try {
                player.doesNotAttack = rs.getBoolean("masterDoesAttack");
                player.lastTimePlayerNotAttack = System.currentTimeMillis();
            } catch (Exception e) {
                player.doesNotAttack = false;
                player.lastTimePlayerNotAttack = System.currentTimeMillis();
            }

            //data Nhận Thỏi Vàng
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("nhanthoivang"));
                player.danhanthoivang = Boolean.parseBoolean(dataArray.get(0).toString());
                player.lastRewardGoldBarTime = Long.parseLong(dataArray.get(1).toString());
            } catch (Exception e) {
                player.danhanthoivang = false;
                player.lastRewardGoldBarTime = 0;
            }

            //data Rương gỗ
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("ruonggo"));
                player.levelWoodChest = Integer.parseInt(dataArray.get(0).toString());
                player.goldChallenge = Long.parseLong(dataArray.get(1).toString());
                player.rubyChallenge = Long.parseLong(dataArray.get(2).toString());
                player.lastTimeRewardWoodChest = Long.parseLong(dataArray.get(3).toString());
                player.lastTimePKDHVT23 = Long.parseLong(dataArray.get(4).toString());
            } catch (Exception e) {
                player.levelWoodChest = 0;
                player.goldChallenge = 50000;
                player.rubyChallenge = 1;
                player.lastTimeRewardWoodChest = System.currentTimeMillis();
                player.lastTimePKDHVT23 = 0;
            }

            //data Siêu Thần Thủy
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("sieuthanthuy"));
                player.winSTT = Boolean.parseBoolean(dataArray.get(0).toString());
                player.lastTimeWinSTT = Long.parseLong(dataArray.get(1).toString());
                player.callBossPocolo = Boolean.parseBoolean(dataArray.get(2).toString());
            } catch (Exception e) {
            }

            //data Võ đài sinh tử
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("vodaisinhtu"));
                player.haveRewardVDST = Boolean.parseBoolean(dataArray.get(0).toString());
                player.thoiVangVoDaiSinhTu = Integer.parseInt(dataArray.get(1).toString());
                player.lastTimePKVoDaiSinhTu = Long.parseLong(dataArray.get(2).toString());
                player.timePKVDST = Long.parseLong(dataArray.get(3).toString());
            } catch (Exception e) {
            }

            //Thời gian gọi rồng
            player.lastTimeShenronAppeared = rs.getLong("rongxuong");

            //data item event
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_item_event"));
                player.itemEvent.remainingTVGSCount = Integer.parseInt(dataArray.get(0).toString());
                player.itemEvent.lastTVGSTime = Long.parseLong(dataArray.get(1).toString());
                player.itemEvent.remainingHHCount = Integer.parseInt(dataArray.get(2).toString());
                player.itemEvent.lastHHTime = Long.parseLong(dataArray.get(3).toString());
                player.itemEvent.remainingBNCount = Integer.parseInt(dataArray.get(4).toString());
                player.itemEvent.lastBNTime = Long.parseLong(dataArray.get(5).toString());
            } catch (Exception e) {
                player.itemEvent.remainingTVGSCount = 0;
                player.itemEvent.lastTVGSTime = 0;
                player.itemEvent.remainingHHCount = 0;
                player.itemEvent.lastHHTime = 0;
                player.itemEvent.remainingBNCount = 0;
                player.itemEvent.lastBNTime = 0;
            }

            //data luyện tập
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_luyentap"));
                player.levelLuyenTap = Integer.parseInt(dataArray.get(0).toString());
                player.dangKyTapTuDong = Boolean.parseBoolean(dataArray.get(1).toString());
                player.mapIdDangTapTuDong = Integer.parseInt(dataArray.get(2).toString());
                player.tnsmLuyenTap = Integer.parseInt(dataArray.get(3).toString());
                player.lastTimeOffline = Long.parseLong(dataArray.get(4).toString());
                if (dataArray.size() > 5) {
                    player.traning.setTop(Integer.parseInt(dataArray.get(5).toString()));
                    player.traning.setTime(Integer.parseInt(dataArray.get(6).toString()));
                    player.traning.setLastTime(Long.parseLong(dataArray.get(7).toString()));
                    player.traning.setLastTop(Integer.parseInt(dataArray.get(8).toString()));
                    player.traning.setLastRewardTime(Long.parseLong(dataArray.get(9).toString()));
                }
            } catch (Exception e) {
                player.levelLuyenTap = 0;
                player.dangKyTapTuDong = false;
                player.mapIdDangTapTuDong = -1;
                player.tnsmLuyenTap = 0;
                player.lastTimeOffline = System.currentTimeMillis();
            }
            // -------------------------------------------------------------
            // THÊM ĐOẠN NÀY ĐỂ LOAD LEVEL WHIS (data_training)
            // -------------------------------------------------------------
            try {
                // Đọc chuỗi JSON từ cột data_training
                String trainingData = rs.getString("data_training");
                
                if (trainingData != null && !trainingData.isEmpty()) {
                    JSONArray dataTraining = (JSONArray) JSONValue.parse(trainingData);
                    
                    if (dataTraining != null && !dataTraining.isEmpty()) {
                        // Index 0: Level (Top)
                        player.traning.setTop(Integer.parseInt(String.valueOf(dataTraining.get(0))));
                        
                        // Index 1: Time
                        player.traning.setTime((int) Long.parseLong(String.valueOf(dataTraining.get(1))));
                        
                        // Index 2: Last Time
                        player.traning.setLastTime(Long.parseLong(String.valueOf(dataTraining.get(2))));
                        
                        // Index 3: Last Top
                        player.traning.setLastTop(Integer.parseInt(String.valueOf(dataTraining.get(3))));
                        
                        // Index 4: Last Reward Time
                        player.traning.setLastRewardTime(Long.parseLong(String.valueOf(dataTraining.get(4))));
                    }
                } else {
                    // Nếu database chưa có dữ liệu (null), set mặc định level 1
                    player.traning.setTop(1);
                }
            } catch (Exception e) {
                // Nếu lỗi format hoặc lỗi đọc, set mặc định level 1
                player.traning.setTop(1);
            }

            //data nhiệm vụ bang hàng ngày
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_clan_task"));
                format = "dd-MM-yyyy";
                receivedTime = Long.parseLong(String.valueOf(dataArray.get(1)));
                date = new Date(receivedTime);
                
                player.playerTask.clanTask.template = TaskService.gI().getClanTaskTemplateById(Integer.parseInt(String.valueOf(dataArray.get(0))));
                player.playerTask.clanTask.count = Integer.parseInt(String.valueOf(dataArray.get(2)));
                player.playerTask.clanTask.maxCount = Integer.parseInt(String.valueOf(dataArray.get(3)));
                player.playerTask.clanTask.level = Integer.parseInt(String.valueOf(dataArray.get(5)));
                player.playerTask.clanTask.receivedTime = receivedTime;
                
                if (TimeUtil.formatTime(date, format).equals(TimeUtil.formatTime(new Date(), format))) {
                    player.playerTask.clanTask.leftTask = Integer.parseInt(String.valueOf(dataArray.get(4)));
                } else {
                    player.playerTask.clanTask.leftTask = ConstTask.MAX_CLAN_TASK;
                }
            } catch (Exception e) {
                player.playerTask.clanTask.leftTask = ConstTask.MAX_CLAN_TASK;
            }

            //data vip
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_vip"));
                player.timesPerDayCuuSat = Integer.parseInt(String.valueOf(dataArray.get(0)));
                player.lastTimeCuuSat = Long.parseLong(String.valueOf(dataArray.get(1)));
                player.nhanDeTuNangVIP = Boolean.parseBoolean(String.valueOf(dataArray.get(2)));
                player.nhanVangNangVIP = Boolean.parseBoolean(String.valueOf(dataArray.get(3)));
                if (dataArray.size() > 4) {
                    player.nhanSKHVIP = Boolean.parseBoolean(String.valueOf(dataArray.get(4)));
                }
            } catch (Exception e) {
            }

            //data super rank
            SuperRankDAO.loadData(player);
            if (Util.isAfterMidnight(player.superRank.lastPKTime)) {
                if (player.superRank.ticket < 3) {
                    player.superRank.ticket++;
                }
                player.superRank.lastPKTime = System.currentTimeMillis();
            }

            //data achievement
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_achievement"));
                for (int i = 0; i < Manager.ACHIEVEMENT_TEMPLATE.size(); i++) {
                    AchievementQuest aq;
                    if (i < dataArray.size()) {
                        JSONArray data = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                        aq = new AchievementQuest(Long.parseLong(data.get(0).toString()), Boolean.parseBoolean(data.get(1).toString()));
                    } else {
                        aq = new AchievementQuest(0, false);
                    }
                    player.achievement.add(aq);
                }
                dataArray.clear();
            } catch (Exception e) {
            }

            //Giftcode
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("giftcode"));
                for (Object code : dataArray) {
                    player.giftCode.add((String) code);
                }
                dataArray.clear();
            } catch (Exception e) {
            }
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("boughtSkills"));
                for (Object code : dataArray) {
                    player.BoughtSkills.add((int) code);
                }
                dataArray.clear();
            } catch (Exception e) {
            }
            //Clan
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_clan"));
                player.lastTimeLeaveClan = Long.parseLong(String.valueOf(dataArray.get(0)));
                player.lastTimeRemoveClan = Long.parseLong(String.valueOf(dataArray.get(1)));
                dataArray.clear();
            } catch (Exception e) {
            }
            try {
                player.luckySpins = rs.getInt("lucky_spins");
            } catch (Exception e) {
                player.luckySpins = 0; // Gán giá trị mặc định nếu có lỗi
            }

            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("hoc_ky_nang"));
                player.hocKyNang.ItemTemplateSkillId = Short.parseShort(String.valueOf(dataArray.get(0)));
                player.hocKyNang.Level = Integer.parseInt(String.valueOf(dataArray.get(1)));
                player.hocKyNang.PotentialLearn = Long.parseLong(String.valueOf(dataArray.get(2)));
                player.hocKyNang.Time = Long.parseLong(String.valueOf(dataArray.get(3)));

                dataArray.clear();
            } catch (Exception e) {
            }

            player.nPoint.hp = plHp;
            player.nPoint.mp = plMp;
            player.idMark.setLoadedAllDataPlayer(true);
        } catch (Exception e) {
            if (player != null) {
                player.dispose();
                player = null;
            }
            throw e;
        }
        return player;
    }

    public static List<OptionCard> loadOptionCard(JSONArray json) {
        List<OptionCard> ops = new ArrayList<>();
        try {
            for (Object o : json) {
                JSONObject ob = (JSONObject) o;
                if (ob != null) {
                    ops.add(new OptionCard(Integer.parseInt(ob.get("id").toString()), Integer.parseInt(ob.get("param").toString()), Byte.parseByte(ob.get("active").toString())));
                }
            }
        } catch (NumberFormatException e) {
        }
        return ops;
    }
}

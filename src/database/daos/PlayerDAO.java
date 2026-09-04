package database.daos;

import database.AlyraManager;
import models.item.Item;
import models.item.ItemTime;
import models.player.Friend;
import models.player.Fusion;
import models.player.Inventory;
import models.player.Player;
import models.skill.Skill;
import services.map.MapService;
import utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Level;
import models.Template;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import services.map.ChangeMapService;
import utils.TimeUtil;
import utils.Util;

public class PlayerDAO {


    public static boolean createNewPlayer(int userId, String name, byte gender, int hair) {
//        Vấn đề: Dữ liệu của một người chơi rất phức tạp và có cấu trúc (có nhiều danh sách lồng nhau).
//        Không tạo hàng chục bảng trong database.
//        Thay vào đó, họ "đóng gói" từng cụm dữ liệu (ví dụ: toàn bộ hòm đồ, toàn bộ kỹ năng) thành một chuỗi JSON duy nhất.
//        Lưu chuỗi JSON này vào một cột TEXT trong bảng player
        try {
            // BƯỚC 1: Khởi tạo
            JSONArray dataArray = new JSONArray();
            //BƯỚC 2: Thêm dữ liệu theo một thứ tự đã quy ước
            dataArray.add(20000); //vàng
            dataArray.add(1000); //ngọc xanh
            dataArray.add(10); //hồng ngọc
            dataArray.add(0); //point
            dataArray.add(0); //event

            // BƯỚC 3: Chuyển đổi thành chuỗi JSON
            String inventory = dataArray.toJSONString();
            dataArray.clear();// Dọn dẹp để tái sử dụng

            dataArray.add(39 + gender); //map
            dataArray.add(100); //x
            dataArray.add(384); //y
            String location = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); //giới hạn sức mạnh
            dataArray.add(2000); //sức mạnh
            dataArray.add(2000); //tiềm năng
            dataArray.add(1000); //thể lực
            dataArray.add(1000); //thể lực đầy
            dataArray.add(gender == 0 ? 200 : 100); //hp gốc
            dataArray.add(gender == 1 ? 200 : 100); //ki gốc
            dataArray.add(gender == 2 ? 15 : 10); //sức đánh gốc
            dataArray.add(0); //giáp gốc
            dataArray.add(0); //chí mạng gốc
            dataArray.add(0); //năng động
            dataArray.add(gender == 0 ? 200 : 100); //hp hiện tại
            dataArray.add(gender == 1 ? 200 : 100); //ki hiện tại
            String point = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(1); //level
            dataArray.add(5); //curent pea
            dataArray.add(0); //is upgrade
            dataArray.add(new Date().getTime()); //last time harvest
            dataArray.add(new Date().getTime()); //last time upgrade
            String magicTree = dataArray.toJSONString();
            dataArray.clear();
            /**
             *
             * [
             * {"temp_id":"1","option":[[5,7],[7,3]],"create_time":"49238749283748957""},
             * {"temp_id":"1","option":[[5,7],[7,3]],"create_time":"49238749283748957""},
             * {"temp_id":"-1","option":[],"create_time":"0""}, ... ]
             */

            int idAo = gender == 0 ? 0 : gender == 1 ? 1 : 2;
            int idQuan = gender == 0 ? 6 : gender == 1 ? 7 : 8;
            int def = gender == 2 ? 3 : 2;
            int hp = gender == 0 ? 30 : 20;

            JSONArray item = new JSONArray();
            JSONArray options = new JSONArray();
            JSONArray opt = new JSONArray();
            for (int i = 0; i < 10; i++) {// lặp qua 10 ô đồ
                switch (i) {
                    case 0:
                        //áo
                        opt.add(47); //id option
                        opt.add(def); //param option
                        item.add(idAo); //id item
                        item.add(1); //số lượng
                        options.add(opt.toJSONString());// options là ["[47,def]"]
                        opt.clear();
                        break;
                    case 1:
                        //quần
                        opt.add(6); //id option
                        opt.add(hp); //param option
                        item.add(idQuan); //id item
                        item.add(1); //số lượng
                        options.add(opt.toJSONString());
                        opt.clear();
                        break;
                    default:
                        item.add(-1); //id item
                        item.add(0); //số lượng
                        break;
                }
                item.add(options.toJSONString()); // Thêm chuỗi options vào mảng item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString()); // 4. Thêm chuỗi vật phẩm vào mảng body
                options.clear();
                item.clear();
            }
            String itemsBody = dataArray.toJSONString();// 6. Hoàn thành body
            dataArray.clear();

            for (int i = 0; i < 20; i++) {
                if (i == 0) { //thỏi vàng
                    opt.add(1); //id option
                    opt.add(500); //param option
                    item.add(521); //id item
                    item.add(1); //số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); //id item
                    item.add(0); //số lượng
                }
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBag = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 20; i++) {
                if (i == 0) { //rada
                    opt.add(14); //id option
                    opt.add(1); //param option
                    item.add(12); //id item
                    item.add(1); //số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); //id item
                    item.add(0); //số lượng
                }
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBox = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 110; i++) {
                item.add(-1); //id item
                item.add(0); //số lượng
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBoxLuckyRound = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 110; i++) {
                item.add(-1); //id item
                item.add(0); //số lượng
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsDaBan = dataArray.toJSONString();
            dataArray.clear();

            String friends = dataArray.toJSONString();
            String enemies = dataArray.toJSONString();

            dataArray.add(0); //id nội tại
            dataArray.add(0); //chỉ số 1
            dataArray.add(0); //chỉ số 2
            dataArray.add(0); //số lần mở
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            String intrinsic = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); //bổ huyết
            dataArray.add(0); //bổ khí
            dataArray.add(0); //giáp xên
            dataArray.add(0); //cuồng nộ
            dataArray.add(0); //ẩn danh
            dataArray.add(0); //bổ huyết
            dataArray.add(0); //bổ khí
            dataArray.add(0); //giáp xên
            dataArray.add(0); //cuồng nộ
            dataArray.add(0); //ẩn danh
            dataArray.add(0); //mở giới hạn sức mạnh
            dataArray.add(0); //máy dò
            dataArray.add(0); //thức ăn cold
            dataArray.add(0); //icon thức ăn cold
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            String itemTime = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            String itemTime_ndung = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0);  // thời gian nhận
            dataArray.add(0);  // Sử Dụng Loa Liên Vũ Trụ 10 Lần Trong Ngày
            dataArray.add(0);  // Nạp Tích Lũy 3250 Ngọc Trong Ngày
            dataArray.add(0);  // Ước Rồng Thần 1 Sao x10 Lần
            dataArray.add(0);  // Hạ Gục Cumber, Black Goku, Cooler, Xên (30 Lần)
            dataArray.add(0);  // Đập 3 Trang Bị +7 Trong Ngày
            dataArray.add(0);  // Top 1 Đại Hội Võ Đài Siêu Hạng
            dataArray.add(0);  // Hoàn Thành 10 Nhiệm Vụ Siêu Khó Tại Bò Mộng
            dataArray.add(0);  // Đánh Bại, Hoặc Cho Xương Sói 20 Lần
            dataArray.add(0);  // Hoàn Thành 5 Lần Nhiệm Vụ Cho Nước Xinbato
            dataArray.add(0);  // Nhặt Đồ 500 Lần Trong Ngày
            dataArray.add(0);  // Tiêu Diệt 30 Lần Boss Ăn Trộm
            dataArray.add(0);  // Tiêu Diệt 30 Lần Boss Ở Dơ
            dataArray.add(0);  // Sử Dụng Loa Liên Vũ Trụ 10 Lần Trong Ngày
            String dataDanhhieu = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); //id nhiệm vụ
            dataArray.add(0); //index nhiệm vụ con
            dataArray.add(0); //số lượng đã làm
            String task = dataArray.toJSONString();
            dataArray.clear();

            String mabuEgg = dataArray.toJSONString();

            dataArray.add(System.currentTimeMillis()); //bùa trí tuệ
            dataArray.add(System.currentTimeMillis()); //bùa mạnh mẽ
            dataArray.add(System.currentTimeMillis()); //bùa da trâu
            dataArray.add(System.currentTimeMillis()); //bùa oai hùng
            dataArray.add(System.currentTimeMillis()); //bùa bất tử
            dataArray.add(System.currentTimeMillis()); //bùa dẻo dai
            dataArray.add(System.currentTimeMillis()); //bùa thu hút
            dataArray.add(System.currentTimeMillis()); //bùa đệ tử
            dataArray.add(System.currentTimeMillis()); //bùa trí tuệ x3
            dataArray.add(System.currentTimeMillis()); //bùa trí tuệ x4
            String charms = dataArray.toJSONString();
            dataArray.clear();

            int[] skillsArr = gender == 0 ? new int[]{0, 1, 6, 9, 10, 20, 22, 19, 24}
                    : gender == 1 ? new int[]{2, 3, 7, 11, 12, 17, 18, 19, 26}
                    : new int[]{4, 5, 8, 13, 14, 21, 23, 19, 25};
            //[{"temp_id":"4","point":0,"last_time_use":0},]

            JSONArray skill = new JSONArray();
            for (int i = 0; i < skillsArr.length; i++) {
                skill.add(skillsArr[i]); //id skill
                if (i == 0) {
                    skill.add(1); //level skill
                } else {
                    skill.add(0); //level skill
                }
                skill.add(0); //thời gian sử dụng trước đó
                dataArray.add(skill.toString());
                skill.clear();
            }
            String skills = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(gender == 0 ? 0 : gender == 1 ? 2 : 4);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            String skillsShortcut = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(gender == 0 ? 0 : gender == 1 ? 2 : 4);
            String boughtSkills = dataArray.toJSONString();
            dataArray.clear();

            String petData = dataArray.toJSONString();

            JSONArray blackBall = new JSONArray();
            for (int i = 1; i <= 7; i++) {
                blackBall.add(0);
                blackBall.add(0);
                blackBall.add(0);
                dataArray.add(blackBall.toJSONString());
                blackBall.clear();
            }
            String dataBlackBall = dataArray.toString();
            dataArray.clear();

            dataArray.add(-1); //id side task
            dataArray.add(0); //thời gian nhận
            dataArray.add(0); //số lượng đã làm
            dataArray.add(0); //số lượng cần làm
            dataArray.add(20); //số nhiệm vụ còn lại có thể nhận
            dataArray.add(0); //mức độ nhiệm vụ
            String dataSideTask = dataArray.toJSONString();
            dataArray.clear();

            AlyraManager.executeUpdate("insert into player"
                    + "(account_id, name, head, gender, have_tennis_space_ship, clan_id, "
                    + "data_inventory, data_location, data_point, data_magic_tree, items_body, "
                    + "items_bag, items_box, items_box_lucky_round, items_daban, friends, enemies, data_intrinsic, data_item_time, devndung_time ,"
                    + "data_task, data_mabu_egg, data_charm, skills, skills_shortcut, pet,"
                    + "data_black_ball, data_side_task, data_danh_hieu,masterDoesNotAttack,data_achievement,giftcode, boughtSkills) "
                    + "values ()", userId, name, hair, gender, 0, -1, inventory, location, point, magicTree,
                    itemsBody, itemsBag, itemsBox, itemsBoxLuckyRound, itemsDaBan, friends, enemies, intrinsic,
                    itemTime, itemTime_ndung, task, mabuEgg, charms, skills, skillsShortcut, petData, dataBlackBall, dataSideTask, dataDanhhieu, 0, 0, 0, boughtSkills);
            Logger.success("Tạo player mới thành công!\n");
            return true;
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi tạo player mới");
            return false;
        }
    }


    public static void updatePlayerAsync(Player player) {
        Thread.ofVirtual().name("db-player-save").start(() -> {
            try {
                updatePlayer(player);
            } catch (Exception e) {
                Logger.logException(PlayerDAO.class, e, "Lỗi updatePlayerAsync: " + (player != null ? player.name : "null"));
            }
        });
    }

    public static void updatePlayer(Player player) {
        if (player == null || !player.idMark.isLoadedAllDataPlayer()) {
            return;
        }

        if (player.inventory == null) {
            Logger.error("Lỗi nghiêm trọng: Cố gắng lưu người chơi " + player.name + " với inventory bị null.");
            return;
        }

        synchronized (player) {
            long st = System.currentTimeMillis();
            try {
            JSONArray dataArray = new JSONArray();

            //data kim lượng
            dataArray.add(player.inventory.gold > Inventory.LIMIT_GOLD
                    ? Inventory.LIMIT_GOLD : player.inventory.gold);
            dataArray.add(player.inventory.gem);
            dataArray.add(player.inventory.ruby);
            dataArray.add(player.inventory.coupon);
            dataArray.add(player.inventory.event);
            String inventory = dataArray.toJSONString();
            dataArray.clear();

            int mapId = -1;
            if (player.zone != null && player.zone.map != null) {
                mapId = player.zone.map.mapId;
            } else if (player.mapIdBeforeLogout >= 0) {
                mapId = player.mapIdBeforeLogout;
            } else {
                mapId = player.gender + 21;
            }
            int x = (player.location != null && player.location.x > 0) ? player.location.x : 300;
            int y = (player.location != null && player.location.y > 0) ? player.location.y : 336;
            long hp = player.nPoint != null ? player.nPoint.hp : 1;
            long mp = player.nPoint != null ? player.nPoint.mp : 1;
            if (player.isDie()) {
                mapId = player.gender + 21;
                x = 300;
                y = 336;
                hp = 1;
                mp = 1;
            } else if (!player.isOffline) {
                if (MapService.gI().isMapPhoBan(mapId) || MapService.gI().isMapBlackBallWar(mapId) || MapService.gI().isMapMaBu(mapId) || MapService.gI().isMapSieuThanhThuy(mapId) || ChangeMapService.gI().checkMapCanJoin(player, MapService.gI().getMapCanJoin(player, mapId, 0)) == null) {
                    mapId = player.gender + 21;
                    x = 300;
                    y = 336;
                }
            }

            //data vị trí
            dataArray.add(mapId);
            dataArray.add(x);
            dataArray.add(y);
            String location = dataArray.toJSONString();
            dataArray.clear();

            //data chỉ số
            dataArray.add(player.nPoint.limitPower);
            dataArray.add(player.nPoint.power);
            dataArray.add(player.nPoint.tiemNang);
            dataArray.add(player.nPoint.stamina);
            dataArray.add(player.nPoint.maxStamina);
            dataArray.add(player.nPoint.hpg);
            dataArray.add(player.nPoint.mpg);
            dataArray.add(player.nPoint.dameg);
            dataArray.add(player.nPoint.defg);
            dataArray.add(player.nPoint.critg);
            dataArray.add(0);
            dataArray.add(hp);
            dataArray.add(mp);
            String point = dataArray.toJSONString();
            dataArray.clear();

            //data nhiệm vụ danh hiệu
            dataArray.add(player.playerTask.taskdh.Nap);
            dataArray.add(player.playerTask.taskdh.ResetTime);
            dataArray.add(player.playerTask.taskdh.Shenron);
            dataArray.add(player.playerTask.taskdh.Hagucboss);
            dataArray.add(player.playerTask.taskdh.DapDo);
            dataArray.add(player.playerTask.taskdh.SieuHang);
            dataArray.add(player.playerTask.taskdh.TaskBoMong);
            dataArray.add(player.playerTask.taskdh.ChoSuong);
            dataArray.add(player.playerTask.taskdh.ChoNuoc);
            dataArray.add(player.playerTask.taskdh.NhatDo);
            dataArray.add(player.playerTask.taskdh.AnTrom);
            dataArray.add(player.playerTask.taskdh.ODo);
            dataArray.add(player.playerTask.taskdh.DungLoa);
            String Danhieu = dataArray.toJSONString();
            dataArray.clear();

            //data đậu thần
            dataArray.add(player.magicTree.level);
            dataArray.add(player.magicTree.currPeas);
            dataArray.add(player.magicTree.isUpgrade ? 1 : 0);
            dataArray.add(player.magicTree.lastTimeHarvest);
            dataArray.add(player.magicTree.lastTimeUpgrade);
            String magicTree = dataArray.toJSONString();
            dataArray.clear();

            //data body
            JSONArray dataItem = new JSONArray();
            for (Item item : player.inventory.itemsBody) {
                JSONArray opt = new JSONArray();
                if (item.isNotNullItem()) {
                    dataItem.add(item.template.id);
                    dataItem.add(item.quantity);
                    JSONArray options = new JSONArray();
                    for (Item.ItemOption io : item.itemOptions) {
                        opt.add(io.optionTemplate.id);
                        opt.add(io.param);
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    dataItem.add(options.toJSONString());
                } else {
                    dataItem.add(-1);
                    dataItem.add(0);
                    dataItem.add(opt.toJSONString());
                }
                dataItem.add(item.createTime);
                dataArray.add(dataItem.toJSONString());
                dataItem.clear();
            }
            String itemsBody = dataArray.toJSONString();
            dataArray.clear();

            int thoiVangBag = 0;
            int thoiVangBox = 0;
            int ngocRong3sBag = 0;
            int ngocRong3sBox = 0;
            int ngocRong4sBag = 0;
            int ngocRong4sBox = 0;

            //data bag
            for (Item item : player.inventory.itemsBag) {
                JSONArray opt = new JSONArray();
                if (item.isNotNullItem()) {
                    dataItem.add(item.template.id);
                    dataItem.add(item.quantity);
                    if (item.template.id == 457) {
                        thoiVangBag = item.quantity;
                    } else if (item.template.id == 16) {
                        ngocRong3sBag = item.quantity;
                    } else if (item.template.id == 17) {
                        ngocRong4sBag = item.quantity;
                    }
                    JSONArray options = new JSONArray();
                    for (Item.ItemOption io : item.itemOptions) {
                        opt.add(io.optionTemplate.id);
                        opt.add(io.param);
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    dataItem.add(options.toJSONString());
                } else {
                    dataItem.add(-1);
                    dataItem.add(0);
                    dataItem.add(opt.toJSONString());
                }
                dataItem.add(item.createTime);
                dataArray.add(dataItem.toJSONString());
                dataItem.clear();
            }
            String itemsBag = dataArray.toJSONString();
            dataArray.clear();

            //data box
            for (Item item : player.inventory.itemsBox) {
                JSONArray opt = new JSONArray();
                if (item.isNotNullItem()) {
                    dataItem.add(item.template.id);
                    dataItem.add(item.quantity);
                    if (item.template.id == 457) {
                        thoiVangBox = item.quantity;
                    } else if (item.template.id == 16) {
                        ngocRong3sBox = item.quantity;
                    } else if (item.template.id == 17) {
                        ngocRong4sBox = item.quantity;
                    }
                    JSONArray options = new JSONArray();
                    for (Item.ItemOption io : item.itemOptions) {
                        opt.add(io.optionTemplate.id);
                        opt.add(io.param);
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    dataItem.add(options.toJSONString());
                } else {
                    dataItem.add(-1);
                    dataItem.add(0);
                    dataItem.add(opt.toJSONString());
                }
                dataItem.add(item.createTime);
                dataArray.add(dataItem.toJSONString());
                dataItem.clear();
            }
            String itemsBox = dataArray.toJSONString();
            dataArray.clear();

            //data box crack ball
            for (Item item : player.inventory.itemsBoxCrackBall) {
                JSONArray opt = new JSONArray();
                if (item.isNotNullItem()) {
                    dataItem.add(item.template.id);
                    dataItem.add(item.quantity);
                    JSONArray options = new JSONArray();
                    for (Item.ItemOption io : item.itemOptions) {
                        opt.add(io.optionTemplate.id);
                        opt.add(io.param);
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    dataItem.add(options.toJSONString());
                } else {
                    dataItem.add(-1);
                    dataItem.add(0);
                    dataItem.add(opt.toJSONString());
                }
                dataItem.add(item.createTime);
                dataArray.add(dataItem.toJSONString());
                dataItem.clear();
            }
            String itemsBoxLuckyRound = dataArray.toJSONString();
            dataArray.clear();

            //data item da ban
            for (Item item : player.inventory.itemsDaBan) {
                JSONArray opt = new JSONArray();
                if (item.isNotNullItem()) {
                    dataItem.add(item.template.id);
                    dataItem.add(item.quantity);
                    JSONArray options = new JSONArray();
                    for (Item.ItemOption io : item.itemOptions) {
                        opt.add(io.optionTemplate.id);
                        opt.add(io.param);
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    dataItem.add(options.toJSONString());
                } else {
                    dataItem.add(-1);
                    dataItem.add(0);
                    dataItem.add(opt.toJSONString());
                }
                dataItem.add(item.createTime);
                dataArray.add(dataItem.toJSONString());
                dataItem.clear();
            }
            String itemsDaBan = dataArray.toJSONString();
            dataArray.clear();

            //data bạn bè
            JSONArray dataFE = new JSONArray();
            for (Friend f : player.friends) {
                dataFE.add(f.id);
                dataFE.add(f.name);
                dataFE.add(f.head);
                dataFE.add(f.body);
                dataFE.add(f.leg);
                dataFE.add(f.bag);
                dataFE.add(f.power);
                dataArray.add(dataFE.toJSONString());
                dataFE.clear();
            }
            String friend = dataArray.toJSONString();
            dataArray.clear();

            //data kẻ thù
            for (Friend e : player.enemies) {
                dataFE.add(e.id);
                dataFE.add(e.name);
                dataFE.add(e.head);
                dataFE.add(e.body);
                dataFE.add(e.leg);
                dataFE.add(e.bag);
                dataFE.add(e.power);
                dataArray.add(dataFE.toJSONString());
                dataFE.clear();
            }
            String enemy = dataArray.toJSONString();
            dataArray.clear();

            //data nội tại
            JSONArray dataIntrinsic = new JSONArray();
            dataIntrinsic.add(player.playerIntrinsic.intrinsic.id);
            dataIntrinsic.add(player.playerIntrinsic.intrinsic.param1);
            dataIntrinsic.add(player.playerIntrinsic.countOpen);
            dataIntrinsic.add(player.playerIntrinsic.intrinsic.param2);
            String intrinsic = dataIntrinsic.toJSONString();

            //data item time
            dataArray.add((player.itemTime.isUseBoHuyet ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet)) : 0));
            dataArray.add((player.itemTime.isUseBoHuyet2 ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet2)) : 0));
            dataArray.add((player.itemTime.isUseBoKhi ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi)) : 0));
            dataArray.add((player.itemTime.isUseBoKhi2 ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi2)) : 0));
            dataArray.add((player.itemTime.isUseGiapXen ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen)) : 0));
            dataArray.add((player.itemTime.isUseGiapXen2 ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen2)) : 0));
            dataArray.add((player.itemTime.isUseCuongNo ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo)) : 0));
            dataArray.add((player.itemTime.isUseCuongNo2 ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo2)) : 0));
            dataArray.add((player.itemTime.isUseAnDanh ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh)) : 0));
            dataArray.add((player.itemTime.isUseAnDanh2 ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh2)) : 0));
            dataArray.add((player.itemTime.isOpenPower ? (ItemTime.TIME_OPEN_POWER - (System.currentTimeMillis() - player.itemTime.lastTimeOpenPower)) : 0));
            dataArray.add((player.itemTime.isUseMayDo ? (ItemTime.TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo)) : 0));
            dataArray.add((player.itemTime.isUseMayDo2 ? (ItemTime.TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo2)) : 0));
            dataArray.add(0);
            dataArray.add((player.itemTime.isEatMeal ? (ItemTime.TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal)) : 0));
            dataArray.add(player.itemTime.iconMeal);
            dataArray.add((player.itemTime.isUseTDLT ? ((player.itemTime.timeTDLT - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT)) / 60 / 1000) : 0));
            dataArray.add((player.itemTime.isUseCMS ? (ItemTime.TIME_CMS - (System.currentTimeMillis() - player.itemTime.lastTimeUseCMS)) : 0));
            dataArray.add((player.itemTime.isUseGTPT ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeUseGTPT)) : 0));
            dataArray.add((player.itemTime.isUseDK ? (ItemTime.TIME_DK - (System.currentTimeMillis() - player.itemTime.lastTimeUseDK)) : 0));
            dataArray.add((player.itemTime.isUseRX ? ((player.itemTime.timeRX - (System.currentTimeMillis() - player.itemTime.lastTimeUseRX)) / 60 / 1000) : 0));
            dataArray.add((player.itemTime.isEatMeal2 ? (ItemTime.TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal2)) : 0));
            dataArray.add(player.itemTime.iconMeal2);
            dataArray.add(0);
            dataArray.add((player.itemTime.isUseNCD ? (ItemTime.TIME_NCD - (System.currentTimeMillis() - player.itemTime.lastTimeUseNCD)) : 0));
            dataArray.add(0);
            dataArray.add(0);
            dataArray.add(player.itemTime.isKhauTrang ? (ItemTime.TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeKhauTrang)) : 0);
            dataArray.add(player.itemTime.isTnDeTu ? (ItemTime.TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeTnDeTu)) : 0);
            dataArray.add((player.itemTime.isXimuoihoadao ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeXimuoihoadao)) : 0));
            dataArray.add((player.itemTime.isXimuoihoamai ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeXimuoihoamai)) : 0));
            dataArray.add((player.itemTime.isBuaTNSM ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBuaTNSM)) : 0));
            String itemTime = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add((player.itemTime.isUseCo4La ? (ItemTime.TIME_CO - (System.currentTimeMillis() - player.itemTime.lastTimeUseCo4La)) : 0));
            dataArray.add((player.itemTime.banhchung ? (ItemTime.DEVNDUNG - (System.currentTimeMillis() - player.itemTime.banhchunglastTime)) : 0));
            dataArray.add((player.itemTime.banhtet ? (ItemTime.DEVNDUNG - (System.currentTimeMillis() - player.itemTime.banhtetlastTime)) : 0));
            dataArray.add((player.itemTime.nguqua ? (ItemTime.DEVNDUNG - (System.currentTimeMillis() - player.itemTime.nguqualastTime)) : 0));
            String DevNdung = dataArray.toJSONString();
            dataArray.clear();

            //data nhiệm vụ
            dataArray.add(player.playerTask.taskMain.id);
            dataArray.add(player.playerTask.taskMain.index);
            dataArray.add(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            dataArray.add(player.playerTask.taskMain.lastTime);
            String task = dataArray.toJSONString();
            dataArray.clear();

            //data nhiệm vụ hàng ngày
            dataArray.add(player.playerTask.sideTask.template != null ? player.playerTask.sideTask.template.id : -1);
            dataArray.add(player.playerTask.sideTask.receivedTime);
            dataArray.add(player.playerTask.sideTask.count);
            dataArray.add(player.playerTask.sideTask.maxCount);
            dataArray.add(player.playerTask.sideTask.leftTask);
            dataArray.add(player.playerTask.sideTask.level);
            dataArray.add(player.playerTask.sideTask.lastTimeCancel);
            dataArray.add(player.playerTask.sideTask.cancelCount);
            String sideTask = dataArray.toJSONString();
            dataArray.clear();

            //data trứng bư
            if (player.mabuEgg != null) {
                dataArray.add(player.mabuEgg.lastTimeCreate);
                dataArray.add(player.mabuEgg.timeDone);
            }
            String mabuEgg = dataArray.toJSONString();
            dataArray.clear();

            //data bùa
            dataArray.add(player.charms.tdTriTue);
            dataArray.add(player.charms.tdManhMe);
            dataArray.add(player.charms.tdDaTrau);
            dataArray.add(player.charms.tdOaiHung);
            dataArray.add(player.charms.tdBatTu);
            dataArray.add(player.charms.tdDeoDai);
            dataArray.add(player.charms.tdThuHut);
            dataArray.add(player.charms.tdDeTu);
            dataArray.add(player.charms.tdTriTue3);
            dataArray.add(player.charms.tdTriTue4);
            String charm = dataArray.toJSONString();
            dataArray.clear();

            //data skill
            JSONArray dataSkill = new JSONArray();
            for (Skill skill : player.playerSkill.skills) {
                dataSkill.add(skill.template.id);
                dataSkill.add(skill.point);
                dataSkill.add(skill.lastTimeUseThisSkill);
                dataSkill.add(skill.currLevel);
                dataArray.add(dataSkill.toJSONString());
                dataSkill.clear();
            }
            String skills = dataArray.toJSONString();
            dataArray.clear();

            //data skill shortcut
            for (int skillId : player.playerSkill.skillShortCut) {
                dataArray.add(skillId);
            }
            String skillShortcut = dataArray.toJSONString();
            dataArray.clear();

            String pet = "[]";
            if (player.pet != null) {
                JSONArray petDataArr = new JSONArray();
                //pet info
                dataArray.add(player.pet.typePet);
                dataArray.add(player.pet.gender);
                dataArray.add(player.pet.name);
                dataArray.add(player.fusion.typeFusion);
                int timeLeftFusion = (int) (Fusion.TIME_FUSION - (System.currentTimeMillis() - player.fusion.lastTimeFusion));
                dataArray.add(timeLeftFusion < 0 ? 0 : timeLeftFusion);
                dataArray.add(player.pet.status);
                petDataArr.add(dataArray.toJSONString());
                dataArray.clear();
                //pet point
                dataArray.add(player.pet.nPoint.limitPower);
                dataArray.add(player.pet.nPoint.power);
                dataArray.add(player.pet.nPoint.tiemNang);
                dataArray.add(player.pet.nPoint.stamina);
                dataArray.add(player.pet.nPoint.maxStamina);
                dataArray.add(player.pet.nPoint.hpg);
                dataArray.add(player.pet.nPoint.mpg);
                dataArray.add(player.pet.nPoint.dameg);
                dataArray.add(player.pet.nPoint.defg);
                dataArray.add(player.pet.nPoint.critg);
                dataArray.add(player.pet.nPoint.hp);
                dataArray.add(player.pet.nPoint.mp);
                petDataArr.add(dataArray.toJSONString());
                dataArray.clear();
                //pet body
                JSONArray items = new JSONArray();
                for (Item item : player.pet.inventory.itemsBody) {
                    JSONArray opt = new JSONArray();
                    if (item.isNotNullItem()) {
                        dataItem.add(item.template.id);
                        dataItem.add(item.quantity);
                        JSONArray options = new JSONArray();
                        for (Item.ItemOption io : item.itemOptions) {
                            opt.add(io.optionTemplate.id);
                            opt.add(io.param);
                            options.add(opt.toJSONString());
                            opt.clear();
                        }
                        dataItem.add(options.toJSONString());
                    } else {
                        dataItem.add(-1);
                        dataItem.add(0);
                        dataItem.add(opt.toJSONString());
                    }
                    dataItem.add(item.createTime);
                    items.add(dataItem.toJSONString());
                    dataItem.clear();
                }
                petDataArr.add(items.toJSONString());
                //pet skill
                JSONArray petSkills = new JSONArray();
                for (Skill s : player.pet.playerSkill.skills) {
                    JSONArray pskill = new JSONArray();
                    if (s.skillId != -1) {
                        pskill.add(s.template.id);
                        pskill.add(s.point);
                        pskill.add(s.lastTimeUseThisSkill);
                        pskill.add(s.currLevel);
                    } else {
                        pskill.add(-1);
                        pskill.add(0);
                        pskill.add(0);
                        pskill.add(0);
                    }
                    petSkills.add(pskill.toJSONString());
                }
                petDataArr.add(petSkills.toJSONString());
                pet = petDataArr.toJSONString();
            }

            //data thưởng ngọc rồng đen
            for (int i = 0; i < player.rewardBlackBall.timeOutOfDateReward.length; i++) {
                JSONArray dataBlackBallArr = new JSONArray();
                dataBlackBallArr.add(player.rewardBlackBall.timeOutOfDateReward[i]);
                dataBlackBallArr.add(player.rewardBlackBall.lastTimeGetReward[i]);
                dataBlackBallArr.add(player.rewardBlackBall.quantilyBlackBall[i]);
                dataArray.add(dataBlackBallArr.toJSONString());
                dataBlackBallArr.clear();
            }
            String dataBlackBall = dataArray.toJSONString();
            dataArray.clear();

            //Ma Bao Ve
            dataArray.add(player.mbv);
            dataArray.add(player.baovetaikhoan);
            dataArray.add(player.mbvtime);
            String dataBVTK = dataArray.toJSONString();
            dataArray.clear();

            //Card
            String dataCard = JSONValue.toJSONString(player.Cards);

            //BDKB
            dataArray.add(player.timesPerDayBDKB);
            dataArray.add(player.lastTimeJoinBDKB);
            String dataBDKB = dataArray.toJSONString();
            dataArray.clear();

            //CDRD
            dataArray.add(player.joinCDRD);
            dataArray.add(player.lastTimeJoinCDRD);
            dataArray.add(player.talkToThuongDe);
            dataArray.add(player.talkToThanMeo);
            String dataCDRD = dataArray.toJSONString();
            dataArray.clear();

            //Nhận Thỏi Vàng
            dataArray.add(player.danhanthoivang);
            dataArray.add(player.lastRewardGoldBarTime);
            String dataNhanThoiVang = dataArray.toJSONString();
            dataArray.clear();

            //Rương Gỗ
            dataArray.add(player.levelWoodChest);
            dataArray.add(player.goldChallenge);
            dataArray.add(player.rubyChallenge);
            dataArray.add(player.lastTimeRewardWoodChest);
            dataArray.add(player.lastTimePKDHVT23);
            String dataRuongGo = dataArray.toJSONString();
            dataArray.clear();

            //Siêu thần thủy
            dataArray.add(player.winSTT);
            dataArray.add(player.lastTimeWinSTT);
            dataArray.add(player.callBossPocolo);
            String dataSieuThanThuy = dataArray.toJSONString();
            dataArray.clear();

            //Võ đài sinh tử
            dataArray.add(player.haveRewardVDST);
            dataArray.add(player.thoiVangVoDaiSinhTu);
            dataArray.add(player.lastTimePKVoDaiSinhTu);
            dataArray.add(player.timePKVDST);
            String dataVoDaiSinhTu = dataArray.toJSONString();
            dataArray.clear();

            //Data item event
            dataArray.add(player.itemEvent.remainingTVGSCount);
            dataArray.add(player.itemEvent.lastTVGSTime);
            dataArray.add(player.itemEvent.remainingHHCount);
            dataArray.add(player.itemEvent.lastHHTime);
            dataArray.add(player.itemEvent.remainingBNCount);
            dataArray.add(player.itemEvent.lastBNTime);
            String dataItemEvent = dataArray.toJSONString();
            dataArray.clear();

            //Data Luyện Tập
//            dataArray.add(player.levelLuyenTap);
//            dataArray.add(player.dangKyTapTuDong);
//            dataArray.add(player.mapIdDangTapTuDong);
//            dataArray.add(player.tnsmLuyenTap);
//            if (player.isOffline) {
//                dataArray.add(player.lastTimeOffline);
//            } else {
//                dataArray.add(System.currentTimeMillis());
//            }
//            dataArray.add(player.traning.getTop());
//            dataArray.add(player.traning.getTime());
//            dataArray.add(player.traning.getLastTime());
//            dataArray.add(player.traning.getLastTop());
//            dataArray.add(player.traning.getLastRewardTime());
//            String dataLuyenTap = dataArray.toJSONString();
//            dataArray.clear();
// --- 1. Xử lý Data Luyện Tập (Auto tập luyện offline) ---
            // Cột: data_luyentap
            dataArray.add(player.levelLuyenTap);
            dataArray.add(player.dangKyTapTuDong);
            dataArray.add(player.mapIdDangTapTuDong);
            dataArray.add(player.tnsmLuyenTap);
            if (player.isOffline) {
                dataArray.add(player.lastTimeOffline);
            } else {
                dataArray.add(System.currentTimeMillis());
            }
            String dataLuyenTap = dataArray.toJSONString();
            dataArray.clear();

            // --- 2. Xử lý Data Training (Luyện tập Whis) ---
            // Cột: data_training
            // Cấu trúc mảng: [Level, Time, LastTime, LastTop, LastRewardTime]
            if (player.traning != null) {
                dataArray.add(player.traning.getTop()); // Index 0: Level (Dùng để xếp hạng)
                dataArray.add(player.traning.getTime());
                dataArray.add(player.traning.getLastTime());
                dataArray.add(player.traning.getLastTop());
                dataArray.add(player.traning.getLastRewardTime());
            } else {
                // Mặc định nếu chưa có dữ liệu
                dataArray.add(1); dataArray.add(0); dataArray.add(0); dataArray.add(0); dataArray.add(0);
            }
            String dataTrainingWhis = dataArray.toJSONString();
            dataArray.clear();

            //data nhiệm vụ bang hàng ngày
            dataArray.add(player.playerTask.clanTask.template != null ? player.playerTask.clanTask.template.id : -1);
            dataArray.add(player.playerTask.clanTask.receivedTime);
            dataArray.add(player.playerTask.clanTask.count);
            dataArray.add(player.playerTask.clanTask.maxCount);
            dataArray.add(player.playerTask.clanTask.leftTask);
            dataArray.add(player.playerTask.clanTask.level);
            String clanTask = dataArray.toJSONString();
            dataArray.clear();

            //data vip
            dataArray.add(player.timesPerDayCuuSat);
            dataArray.add(player.lastTimeCuuSat);
            dataArray.add(player.nhanDeTuNangVIP);
            dataArray.add(player.nhanVangNangVIP);
            dataArray.add(player.nhanSKHVIP);
            String dataVip = dataArray.toJSONString();
            dataArray.clear();

            //Data doanh trại
            dataArray.add(player.lastTimeJoinDT);
            String doanhtrai = dataArray.toJSONString();
            dataArray.clear();

            //data achievement
            if (player.achievement != null) {
                for (Template.AchievementQuest aq : player.achievement.getAchievementList()) {
                    JSONArray a = new JSONArray();
                    a.add(aq.completed);
                    a.add(aq.isRecieve);
                    dataArray.add(a.toJSONString());
                    a.clear();
                }
            }
            String achievement = dataArray.toJSONString();
            dataArray.clear();

            //gift code
            for (String code : player.giftCode.rewards) {
                dataArray.add(code);
            }
            String giftCode = dataArray.toJSONString();
            dataArray.clear();

            // Shop santa danh hiệu
            JSONArray dataShopDanhHieu = new JSONArray();
            dataShopDanhHieu.add(player.effect.getPointDaiGiaMoiNhu());
            dataShopDanhHieu.add(player.effect.getPointTrumUocRong());
            dataShopDanhHieu.add(player.effect.getPointTrumSanBoss());
            dataShopDanhHieu.add(player.effect.getPointThanhDapDo());
            dataShopDanhHieu.add(player.effect.getPointNongDanChamChi());
            dataShopDanhHieu.add(player.effect.getPointOngThanVeChai());
            dataShopDanhHieu.add(player.effect.getPointBiMocSachTui());
            dataShopDanhHieu.add(player.effect.getPointPhanCung());
            String shopDanhHieu = dataShopDanhHieu.toJSONString();
            dataShopDanhHieu.clear();

            dataArray.add(player.lastTimeLeaveClan);
            dataArray.add(player.lastTimeRemoveClan);
            String dataClan = dataArray.toJSONString();
            dataArray.clear();

            // Bùa Miễn phí
            JSONArray bua = new JSONArray();
            bua.add(player.luotNhanBuaMienPhi);
            bua.add(player.diemDanhSuKien);
            String buarandom = bua.toJSONString();
            bua.clear();

            JSONArray diemsk = new JSONArray();
            diemsk.add(player.diemsukien);
            diemsk.add(player.phaobong);
            diemsk.add(player.lixi);
            String skhe = diemsk.toJSONString();
            diemsk.clear();

            JSONArray hocKyNang = new JSONArray();
            hocKyNang.add(player.hocKyNang.ItemTemplateSkillId);
            hocKyNang.add(player.hocKyNang.Level);
            hocKyNang.add(player.hocKyNang.PotentialLearn);
            hocKyNang.add(player.hocKyNang.Time);
            String sHocKyNang = hocKyNang.toJSONString();

            //==================================================================
            // PHẦN SỬA LỖI
            //==================================================================
//            String infoLog;
//            boolean isAdmin = player.getSession() != null && player.getSession().isAdmin;
//            infoLog = "Vàng tươi: " + (isAdmin ? Util.formatNumber(2000) : Util.formatNumber(player.inventory.gold)) + " (" + (isAdmin ? Util.powerToString(2000) : Util.powerToString(player.inventory.gold))
//                    + ") | Thỏi vàng: " + (isAdmin ? 0 : Util.formatNumber(thoiVangBag + thoiVangBox)) + " (Bag: " + (isAdmin ? 0 : Util.formatNumber(thoiVangBag)) + " - Box: " + (isAdmin ? 0 : Util.formatNumber(thoiVangBox))
//                    + ") | Ngọc rồng 3s: " + (isAdmin ? 0 : Util.formatNumber(ngocRong3sBag + ngocRong3sBox)) + " (Bag: " + (isAdmin ? 0 : Util.formatNumber(ngocRong3sBag)) + " - Box: " + (isAdmin ? 0 : Util.formatNumber(ngocRong3sBox))
//                    + ") | Ngọc rồng 4s: " + Util.formatNumber(ngocRong4sBag + ngocRong4sBox) + " (Bag: " + Util.formatNumber(ngocRong4sBag) + " - Box: " + Util.formatNumber(ngocRong4sBox) + ")";
//            //==================================================================

            String query = "update player set head = ?, have_tennis_space_ship = ?, "
                    + "clan_id = ?, data_inventory = ?, data_location = ?, data_point = ?, data_magic_tree = ?, "
                    + "items_body = ?, items_bag = ?, items_box = ?, items_box_lucky_round = ?, items_daban = ?, friends = ?, "
                    + "enemies = ?, data_intrinsic = ?, data_item_time = ?, devndung_time = ?, data_task = ?, data_mabu_egg = ?, pet = ?, "
                    + "data_black_ball = ?, data_side_task = ?, data_danh_hieu = ?, data_charm = ?, skills = ?, skills_shortcut = ?, notify = ?, "
                    + "baovetaikhoan = ?, data_card = ?, lasttimepkcommeson = ?, bandokhobau = ?, doanhtrai = ?, conduongrandoc = ?, masterDoesNotAttack = ?, "
                    + "nhanthoivang = ?, ruonggo = ?, sieuthanthuy = ?, vodaisinhtu = ?, rongxuong = ?, data_item_event = ?, data_luyentap = ?, data_training = ?, data_clan_task = ?, data_vip = ?, "
                    + "rank = ?, data_achievement = ?, giftcode = ?,danh_hieu_shop = ?, data_clan = ?, firstTimeLogin = ? ,buarandom = ?, dien_sukien = ?, banhtet = ?, "
                    + "banhchung = ?, hoc_ky_nang = ?, boughtSkills = ?, arena_wins = ?, lucky_spins = ?"
                    + " where id = ?";

            AlyraManager.executeUpdate(query,
                    player.getHead(), player.haveTennisSpaceShip, (player.clan != null ? player.clan.id : -1),
                    inventory, location, point, magicTree,
                    itemsBody, itemsBag, itemsBox, itemsBoxLuckyRound, itemsDaBan, friend,
                    enemy, intrinsic, itemTime, DevNdung, task, mabuEgg, pet,
                    dataBlackBall, sideTask, Danhieu, charm, skills, skillShortcut, player.notify,
                    dataBVTK, dataCard, player.lastPkCommesonTime, dataBDKB, doanhtrai, dataCDRD, player.doesNotAttack,
                    dataNhanThoiVang, dataRuongGo, dataSieuThanThuy, dataVoDaiSinhTu, player.lastTimeShenronAppeared, dataItemEvent, dataLuyenTap, dataTrainingWhis, clanTask, dataVip,
                    player.superRank.rank, achievement, giftCode, shopDanhHieu, dataClan, Util.toDateString(player.firstTimeLogin), buarandom, skhe, player.banhtet,
                    player.banhtrung,
                    sHocKyNang, "[]", player.arenaWins,player.luckySpins, player.id);

            if (player.isOffline) {
                Logger.log(Logger.PURPLE, TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Player OFFLINE " + player.name + " updated successfully! " + (System.currentTimeMillis() - st) + "ms\n");
                player.dispose();
            } else {
                Logger.success(TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Player " + player.name + " saved successfully! " + (System.currentTimeMillis() - st) + "ms\n");
            }
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi save player " + player.name);
        }
        }
    }

    public static boolean checkLogout(Connection con, Player player) {
        long lastTimeLogout = 0;
        long lastTimeLogin = 0;
        try (PreparedStatement ps = con.prepareStatement("select last_time_logout, last_time_login from account where id = ? limit 1")) {
            ps.setInt(1, player.getSession().userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp logoutTime = rs.getTimestamp("last_time_logout");
                    java.sql.Timestamp loginTime = rs.getTimestamp("last_time_login");
                    lastTimeLogout = logoutTime != null ? logoutTime.getTime() : 0;
                    lastTimeLogin = loginTime != null ? loginTime.getTime() : 0;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return lastTimeLogout > lastTimeLogin;
    }

    public static boolean addPointEvent(Player player, int num) {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement("update account set diem_da_nhan = (diem_da_nhan + ?) where id = ?")) {
            ps.setInt(1, num);
            ps.setInt(2, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().diemReceive += num;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean subcash(Player player, int num) {
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("update account set cash = (cash - ?) where id = ?")) {
            ps.setInt(1, num);
            ps.setInt(2, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().cash -= num;
            return true;
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi update cash" + player.name);
            return false;
        }
    }

    public static void LogAddPoint(String name, int id, int point, String type) {
        System.out.println(name + " - " + id + " - " + point + " - " + type);
//        try {
//            NDVDB.executeUpdate("INSERT INTO histotyevent(name, id_account, event_point, type) VALUES ()", "cc", "cc", "cc", "cc");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    @SuppressWarnings("unchecked")
    public static boolean createPlAo(int userId, String name, byte gender, int hair) {
        try {
            JSONArray dataArray = new JSONArray();

            dataArray.add(200000); //vàng
            dataArray.add(100); //ngọc xanh
            dataArray.add(0); //hồng ngọc
            dataArray.add(0); //point
            dataArray.add(0); //event

            String inventory = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(Util.nextInt(173)); //map
            dataArray.add(100); //x
            dataArray.add(384); //y
            String location = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); //giới hạn sức mạnh
            dataArray.add(2000); //sức mạnh
            dataArray.add(2000); //tiềm năng
            dataArray.add(1000); //thể lực
            dataArray.add(1000); //thể lực đầy
            dataArray.add(gender == 0 ? 200 : 100); //hp gốc
            dataArray.add(gender == 1 ? 200 : 100); //ki gốc
            dataArray.add(gender == 2 ? 15 : 10); //sức đánh gốc
            dataArray.add(0); //giáp gốc
            dataArray.add(0); //chí mạng gốc
            dataArray.add(0); //năng động
            dataArray.add(1000000); //hp hiện tại
            dataArray.add(gender == 1 ? 200 : 100); //ki hiện tại
            String point = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(1); //level
            dataArray.add(5); //curent pea
            dataArray.add(0); //is upgrade
            dataArray.add(new Date().getTime()); //last time harvest
            dataArray.add(new Date().getTime()); //last time upgrade
            String magicTree = dataArray.toJSONString();
            dataArray.clear();
            /**
             *
             * [
             * {"temp_id":"1","option":[[5,7],[7,3]],"create_time":"49238749283748957""},
             * {"temp_id":"1","option":[[5,7],[7,3]],"create_time":"49238749283748957""},
             * {"temp_id":"-1","option":[],"create_time":"0""}, ... ]
             */

            int idAo = gender == 0 ? 0 : gender == 1 ? 1 : 2;
            int idQuan = gender == 0 ? 6 : gender == 1 ? 7 : 8;
            int def = gender == 2 ? 3 : 2;
            int hp = gender == 0 ? 30 : 20;

            JSONArray item = new JSONArray();
            JSONArray options = new JSONArray();
            JSONArray opt = new JSONArray();
            for (int i = 0; i < 11; i++) {
                if (i == 0) { //áo
                    opt.add(47); //id option
                    opt.add(def); //param option
                    item.add(idAo); //id item
                    item.add(1); //số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else if (i == 1) { //quần
                    opt.add(6); //id option
                    opt.add(hp); //param option
                    item.add(idQuan); //id item
                    item.add(1); //số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); //id item
                    item.add(0); //số lượng
                }
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBody = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 20; i++) {
                if (i == 0) { //thỏi vàng
                    opt.add(1); //id option
                    opt.add(500); //param option
                    item.add(521); //id item
                    item.add(1); //số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); //id item
                    item.add(0); //số lượng
                }
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBag = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 20; i++) {
                if (i == 0) { //rada
                    opt.add(14); //id option
                    opt.add(1); //param option
                    item.add(12); //id item
                    item.add(1); //số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); //id item
                    item.add(0); //số lượng
                }
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBox = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 110; i++) {
                item.add(-1); //id item
                item.add(0); //số lượng
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBoxLuckyRound = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 110; i++) {
                item.add(-1); //id item
                item.add(0); //số lượng
                item.add(options.toJSONString()); //full option item
                item.add(System.currentTimeMillis()); //thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsDaBan = dataArray.toJSONString();
            dataArray.clear();

            String friends = dataArray.toJSONString();
            String enemies = dataArray.toJSONString();

            dataArray.add(0); //id nội tại
            dataArray.add(0); //chỉ số 1
            dataArray.add(0); //chỉ số 2
            dataArray.add(0); //số lần mở
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            String intrinsic = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); //bổ huyết
            dataArray.add(0); //bổ khí
            dataArray.add(0); //giáp xên
            dataArray.add(0); //cuồng nộ
            dataArray.add(0); //ẩn danh
            dataArray.add(0); //bổ huyết
            dataArray.add(0); //bổ khí
            dataArray.add(0); //giáp xên
            dataArray.add(0); //cuồng nộ
            dataArray.add(0); //ẩn danh
            dataArray.add(0); //mở giới hạn sức mạnh
            dataArray.add(0); //máy dò
            dataArray.add(0); //thức ăn cold
            dataArray.add(0); //icon thức ăn cold
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            String itemTime = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); //
            String itemTime_ndung = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(31); //id nhiệm vụ
            dataArray.add(0); //index nhiệm vụ con
            dataArray.add(0); //số lượng đã làm
            String task = dataArray.toJSONString();
            dataArray.clear();

            String mabuEgg = dataArray.toJSONString();

            dataArray.add(System.currentTimeMillis()); //bùa trí tuệ
            dataArray.add(System.currentTimeMillis()); //bùa mạnh mẽ
            dataArray.add(System.currentTimeMillis()); //bùa da trâu
            dataArray.add(System.currentTimeMillis()); //bùa oai hùng
            dataArray.add(System.currentTimeMillis()); //bùa bất tử
            dataArray.add(System.currentTimeMillis()); //bùa dẻo dai
            dataArray.add(System.currentTimeMillis()); //bùa thu hút
            dataArray.add(System.currentTimeMillis()); //bùa đệ tử
            dataArray.add(System.currentTimeMillis()); //bùa trí tuệ x3
            dataArray.add(System.currentTimeMillis()); //bùa trí tuệ x4
            String charms = dataArray.toJSONString();
            dataArray.clear();

            int[] skillsArr = gender == 0 ? new int[]{0, 1, 6, 9, 10, 20, 22, 19}
                    : gender == 1 ? new int[]{2, 3, 7, 11, 12, 17, 18, 19}
                    : new int[]{4, 5, 8, 13, 14, 21, 23, 19};
            //[{"temp_id":"4","point":0,"last_time_use":0},]

            JSONArray skill = new JSONArray();
            for (int i = 0; i < skillsArr.length; i++) {
                skill.add(skillsArr[i]); //id skill
                if (i == 0) {
                    skill.add(1); //level skill
                } else {
                    skill.add(0); //level skill
                }
                skill.add(0); //thời gian sử dụng trước đó
                dataArray.add(skill.toString());
                skill.clear();
            }
            String skills = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(gender == 0 ? 0 : gender == 1 ? 2 : 4);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            String skillsShortcut = dataArray.toJSONString();
            dataArray.clear();

            String petData = dataArray.toJSONString();

            JSONArray blackBall = new JSONArray();
            for (int i = 1; i <= 7; i++) {
                blackBall.add(0);
                blackBall.add(0);
                blackBall.add(0);
                dataArray.add(blackBall.toJSONString());
                blackBall.clear();
            }
            String dataBlackBall = dataArray.toString();
            dataArray.clear();

            dataArray.add(-1); //id side task
            dataArray.add(0); //thời gian nhận
            dataArray.add(0); //số lượng đã làm
            dataArray.add(0); //số lượng cần làm
            dataArray.add(20); //số nhiệm vụ còn lại có thể nhận
            dataArray.add(0); //mức độ nhiệm vụ
            String dataSideTask = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0);  // thời gian nhận
            dataArray.add(0);  // Sử Dụng Loa Liên Vũ Trụ 10 Lần Trong Ngày
            dataArray.add(0);  // Nạp Tích Lũy 3250 Ngọc Trong Ngày
            dataArray.add(0);  // Ước Rồng Thần 1 Sao x10 Lần
            dataArray.add(0);  // Hạ Gục Cumber, Black Goku, Cooler, Xên (30 Lần)
            dataArray.add(0);  // Đập 3 Trang Bị +7 Trong Ngày
            dataArray.add(0);  // Top 1 Đại Hội Võ Đài Siêu Hạng
            dataArray.add(0);  // Hoàn Thành 10 Nhiệm Vụ Siêu Khó Tại Bò Mộng
            dataArray.add(0);  // Đánh Bại, Hoặc Cho Xương Sói 20 Lần
            dataArray.add(0);  // Hoàn Thành 5 Lần Nhiệm Vụ Cho Nước Xinbato
            dataArray.add(0);  // Nhặt Đồ 500 Lần Trong Ngày
            dataArray.add(0);  // Tiêu Diệt 30 Lần Boss Ăn Trộm
            dataArray.add(0);  // Tiêu Diệt 30 Lần Boss Ở Dơ
            dataArray.add(0);  // Sử Dụng Loa Liên Vũ Trụ 10 Lần Trong Ngày
            String dataDanhhieu = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0);
            dataArray.add(0);
            String dataClan = dataArray.toJSONString();
            dataArray.clear();

            AlyraManager.executeUpdate("insert into player"
                    + "(account_id, name, head, gender, have_tennis_space_ship, clan_id, "
                    + "data_inventory, data_location, data_point, data_magic_tree, items_body, "
                    + "items_bag, items_box, items_box_lucky_round, items_daban, friends, enemies, data_intrinsic, data_item_time, devndung_time,"
                    + "data_task, data_mabu_egg, data_charm, skills, skills_shortcut, pet,"
                    + "data_black_ball, data_side_task, data_danh_hieu, data_clan) "
                    + "values ()", userId, name, hair, gender, 0, -1, inventory, location, point, magicTree,
                    itemsBody, itemsBag, itemsBox, itemsBoxLuckyRound, itemsDaBan, friends, enemies, intrinsic,
                    itemTime, itemTime_ndung, task, mabuEgg, charms, skills, skillsShortcut, petData, dataBlackBall, dataSideTask, dataDanhhieu, dataClan);
            Logger.success("New player created successfully!\n");
            return true;
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi tạo player mới");
            return false;
        }
    }

    public static boolean subvnd(Player player, int num) {
        PreparedStatement ps;
        try (Connection con = AlyraManager.getConnection();) {
            ps = con.prepareStatement("update account set sotien = (sotien - ?) where id = ?");
            ps.setInt(1, num);
            ps.setInt(2, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().sotien -= num;
            ps.close();
            con.close();
            return true;
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi trừ vnd");
            return false;
        }
    }

    public static boolean subVND2(Player player, int sotien) {
        if (player.getSession().isAdmin) {
            sotien = 0;
        }
        PreparedStatement ps;
        try (Connection con = AlyraManager.getConnection();) {
            ps = con.prepareStatement("UPDATE account SET sotien = (sotien - ?), active = ? WHERE id = ?");
            if (!player.getSession().actived) {
                player.getSession().actived = true;
            }
            ps.setInt(1, sotien);
            ps.setInt(2, player.getSession().actived ? 1 : 0);
            ps.setInt(3, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().sotien -= sotien;
            return true;
        } catch (SQLException e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi update vnd " + player.name);
        }
        return false;
    }

    public static boolean addVndNe(Player player, int sotien) {
        String sql = "UPDATE account SET sotien = sotien + ? WHERE id = ?";
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sotien);
            ps.setInt(2, player.getSession().userId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                player.getSession().sotien += sotien;
                return true;
            } else {
                Logger.log("Không tìm thấy account với id=" + player.getSession().userId);
            }

        } catch (SQLException e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi update vnd " + player.name);
        }
        return false;
    }

    public static boolean Addvnd(String username, int num) {
        username = username.trim();
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("update account set sotien = (sotien + ?) where username = ? ")) {
            ps.setInt(1, num);
            ps.setString(2, username);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            Logger.error("Lỗi khi cộng vnd cho " + username + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean Addtotalvnd(String username, int num) {
        username = username.trim();
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("update account set danap = (danap + ?) where username = ?")) {
            ps.setInt(1, num);
            ps.setString(2, username);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            Logger.error("Lỗi khi cộng total vnd cho " + username + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean Addtotalvnd2(String username, int num) {
        username = username.trim();
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("update account set danap = (danap + ?) where username = ?")) {
            ps.setInt(1, num);
            ps.setString(2, username);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            Logger.error("Lỗi khi cộng total vnd2 cho " + username + ": " + e.getMessage());
            return false;
        }
    }

    public static void subVIP(Player player) {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE account SET vip = ?, hasReceivedVIP = 1, lastTimeReceivedVIP = ? WHERE id = ?")) {
            // Thực thi câu lệnh SQL
            ps.setInt(1, player.getSession().vip);
            ps.setLong(2, player.getSession().lastTimeReceivedVIP = System.currentTimeMillis());
            ps.setInt(3, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().hasReceivedVIP = true;
        } catch (SQLException e) {
            // Ghi lại log nếu có lỗi
            Logger.logException(PlayerDAO.class, e, "Lỗi update hasReceivedVIP " + player.name);
        }
    }

    public static void subVIP1(Player player) {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE account SET vip1 = ?, hasReceivedVIP1 = 1, lastTimeReceivedVIP1 = ? WHERE id = ?")) {
            // Thực thi câu lệnh SQL
            ps.setInt(1, player.getSession().vip1);
            ps.setLong(2, player.getSession().lastTimeReceivedVIP1 = System.currentTimeMillis());
            ps.setInt(3, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().hasReceivedVIP1 = true;
        } catch (SQLException e) {
            // Ghi lại log nếu có lỗi
            Logger.logException(PlayerDAO.class, e, "Lỗi update hasReceivedVIP1 " + player.name);
        }
    }

    public static void subVIP2(Player player) {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE account SET vip2 = ?, hasReceivedVIP2 = 1, lastTimeReceivedVIP2 = ? WHERE id = ?")) {
            // Thực thi câu lệnh SQL
            ps.setInt(1, player.getSession().vip2);
            ps.setLong(2, player.getSession().lastTimeReceivedVIP2 = System.currentTimeMillis());
            ps.setInt(3, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().hasReceivedVIP2 = true;
        } catch (SQLException e) {
            // Ghi lại log nếu có lỗi
            Logger.logException(PlayerDAO.class, e, "Lỗi update hasReceivedVIP2 " + player.name);
        }
    }

    @SuppressWarnings("unchecked")
    public static void updateBlackBallReward(long playerId, byte star) {
        if (star < 1 || star > 7) {
            return;
        }
        int starIndex = star - 1;
        long timeReward = 79200000L;
        long now = System.currentTimeMillis();
        long newExpire = now + timeReward;

        String selectQuery = "SELECT data_black_ball FROM player WHERE id = ?";
        String updateQuery = "UPDATE player SET data_black_ball = ? WHERE id = ?";

        try (Connection con = AlyraManager.getConnection()) {
            String currentData = null;
            try (PreparedStatement ps = con.prepareStatement(selectQuery)) {
                ps.setLong(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentData = rs.getString("data_black_ball");
                    }
                }
            }

            JSONArray dataArray = null;
            if (currentData != null && !currentData.isEmpty()) {
                try {
                    dataArray = (JSONArray) JSONValue.parse(currentData);
                } catch (Exception ignored) {
                }
            }

            long[] timeOutOfDate = new long[7];
            long[] lastTimeGet = new long[7];
            int[] quantily = new int[7];

            if (dataArray != null) {
                for (int i = 0; i < Math.min(dataArray.size(), 7); i++) {
                    try {
                        JSONArray itemArr = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                        if (itemArr != null) {
                            timeOutOfDate[i] = Long.parseLong(String.valueOf(itemArr.get(0)));
                            lastTimeGet[i] = Long.parseLong(String.valueOf(itemArr.get(1)));
                            if (itemArr.size() > 2 && itemArr.get(2) != null) {
                                quantily[i] = Integer.parseInt(String.valueOf(itemArr.get(2)));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            timeOutOfDate[starIndex] = newExpire;
            quantily[starIndex]++;

            JSONArray newMainArray = new JSONArray();
            for (int i = 0; i < 7; i++) {
                JSONArray itemArr = new JSONArray();
                itemArr.add(timeOutOfDate[i]);
                itemArr.add(lastTimeGet[i]);
                itemArr.add(quantily[i]);
                newMainArray.add(itemArr.toJSONString());
            }

            try (PreparedStatement ps = con.prepareStatement(updateQuery)) {
                ps.setString(1, newMainArray.toJSONString());
                ps.setLong(2, playerId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi updateBlackBallReward cho playerId: " + playerId);
        }
    }
}

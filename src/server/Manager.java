package server;

import models.radar.OptionCard;
import services.RadarService;
import models.radar.RadarCard;
import database.AlyraManager;
import database.loaders.DatabaseTemplateLoader;
import managers.map.TileMapDataLoader;
import consts.ConstPlayer;
import consts.ConstMap;
import data.DataGame;
import database.daos.ShopDAO;
import models.Template.*;
import models.clan.Clan;
import models.clan.ClanMember;
import static data.DataGame.MAP_MOUNT_NUM;
import models.GiftCode;
import managers.GiftCodeManager;
import models.intrinsic.Intrinsic;
import models.item.Item;
import models.item.Item.ItemOption;
import models.map.WayPoint;
import models.npc.Npc;
import models.npc.NpcFactory;
import models.shop.Shop;
import models.skill.NClass;
import models.skill.Skill;
import models.task.SideTaskTemplate;
import models.task.SubTaskMain;
import models.task.TaskMain;
import services.map.MapService;
import utils.Logger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map;

import managers.ConsignShopManager;
import models.map.*;
import models.ConsignItem;
import models.map.Zone;
import models.matches.TOP;
import models.npc.NonInteractiveNPC;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import models.task.ClanTaskTemplate;
import utils.Util;

public final class Manager {

    private static Manager instance;
    public static String SERVER_NAME = "";

    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = 5;
    public static int MAX_PER_IP = 10;
    public static int MAX_PLAYER = 2000;
    public static int RATE_EXP_SERVER = 1;
    public static byte SU_KIEN = 0;
    public static boolean LOCAL = false;
    public static boolean TEST = false;
    public static boolean DAO_AUTO_UPDATER = false;
    public static long timeRealTop = 0;
    public static boolean DEBUG = true;

    public static MapTemplate[] MAP_TEMPLATES;
    public static final List<models.map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final List<ArrHead2Frames> ARR_HEAD_2_FRAMES = new ArrayList<>();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<ClanTaskTemplate> CLAN_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<AchievementTemplate> ACHIEVEMENT_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<BgItem> BG_ITEMS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static List<Shop> SHOPS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static List<TOP> topSM;
    public static List<TOP> topNV;
    public static List<TOP> topNap;
    public static List<TOP> topsk;
    public static List<TOP> toppb;
    public static List<TOP> toplx;
     public static List<TOP> topArena;
    public static List<TOP> topLuckySpins;
    public static List<TOP> topWhis;
    public static List<TOP> topBDKB;

    public static final short[] itemsDHD = {650, 651, 657, 658, 656, 652, 653, 659, 660, 656, 654, 655, 661, 662, 656}; //td, namec,xd
    public static final byte[] itemIds_NR_FULL = {14, 15, 16, 17, 18, 19, 20};

    public static final short[] itemIds_Kaio_AWJ = {232, 236, 240, 244, 248, 252, 268, 272, 276};
    public static final short[] itemIds_tl_AWJ = {555, 557, 559, 556, 558, 560, 563, 565, 567};
    public static final short[] itemIds_tl_GN = {562, 564, 566, 561};
    public static final short[] itemIds_Kaio_GN = {256, 260, 264, 280};
    public static final short[] itemIds_LuongLong_AWJ = {233, 237, 241, 245, 249, 253, 269, 273, 277};
    public static final short[] itemIds_LuongLong_GN = {257, 261, 265, 281};
    public static final short[] itemIds_GIAY_TL = {563, 565, 567};
    public static final short[] aotd = {232, 233, 555};
    public static final short[] quantd = {244, 245, 556};
    public static final short[] gangtd = {256, 257, 562};
    public static final short[] giaytd = {268, 269, 563};
    public static final short[] aoxd = {240, 241, 559};
    public static final short[] quanxd = {252, 253, 560};
    public static final short[] gangxd = {264, 265, 566};
    public static final short[] giayxd = {276, 277, 567};
    public static final short[] aonm = {236, 237, 557};
    public static final short[] quannm = {248, 249, 558};
    public static final short[] gangnm = {260, 261, 564};
    public static final short[] giaynm = {272, 273, 565};
    public static final short[] radaSKHVip = {280, 281, 561};

    public static final short[] aotdVip = {138, 139, 230, 231, 232, 233, 555};
    public static final short[] quantdVip = {142, 143, 242, 243, 244, 245, 556};
    public static final short[] gangtdVip = {146, 147, 254, 255, 256, 257, 562};
    public static final short[] giaytdVip = {150, 151, 266, 267, 268, 269, 563};
    public static final short[] aoxdVip = {170, 171, 238, 239, 240, 241, 559};
    public static final short[] quanxdVip = {174, 175, 250, 251, 252, 253, 560};
    public static final short[] gangxdVip = {178, 179, 262, 263, 264, 265, 566};
    public static final short[] giayxdVip = {182, 183, 274, 275, 276, 277, 567};
    public static final short[] aonmVip = {154, 155, 234, 235, 236, 237, 557};
    public static final short[] quannmVip = {158, 159, 246, 247, 248, 249, 558};
    public static final short[] gangnmVip = {162, 163, 258, 259, 260, 261, 564};
    public static final short[] giaynmVip = {166, 167, 270, 271, 272, 273, 565};
    public static final short[] radaSKHVipVip = {186, 187, 278, 279, 280, 281, 561};
    public static final short[][][] doSKHVip = {{aotd, quantd, gangtd, giaytd}, {aonm, quannm, gangnm, giaynm},
    {aoxd, quanxd, gangxd, giayxd}};
   

    public static final String queryTopNV = "SELECT p.id, \n"
            + "       JSON_UNQUOTE(JSON_EXTRACT(p.data_task, '$[0]')) AS second_value\n"
            + "FROM player p\n"
            + "ORDER BY CAST(JSON_UNQUOTE(JSON_EXTRACT(p.data_task, '$[0]')) AS UNSIGNED) DESC\n"
            + "LIMIT 100;";
    public static final String queryTopSM = "SELECT p.id,\n"
            + "       CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(p.data_point, ',', 2), ',', -1) AS UNSIGNED) AS sm\n"
            + "FROM player p\n"
            + "LEFT JOIN account a ON p.id = a.id AND a.is_admin = 1\n"
            + "WHERE a.id IS NULL\n"
            + "ORDER BY sm DESC\n"
            + "LIMIT 100;";
    public static final String queryTopNap = "SELECT p.id, a.danap\n"
            + "FROM player p\n"
            + "JOIN account a ON p.account_id = a.id\n"
            + "WHERE a.danap IS NOT NULL\n"
            + "ORDER BY a.danap DESC\n"
            + "LIMIT 100;";
    public static final String queryTopsk = "SELECT id, CAST(REPLACE(REPLACE(dien_sukien, '[', ''), ']', '') AS UNSIGNED) AS sk\n"
            + "FROM player\n"
            + "ORDER BY sk DESC\n"
            + "LIMIT 100;";
    public static final String TOP_PHAO_BONG = "SELECT id, \n"
            + "       CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(dien_sukien, ',', 2), ',', -1) AS UNSIGNED) AS phaobong\n"
            + "FROM player\n"
            + "ORDER BY phaobong DESC\n"
            + "LIMIT 100;";
    public static final String TOP_LIXI = "SELECT id, \n"
            + "       CAST(SUBSTRING_INDEX(dien_sukien, ',', -1) AS UNSIGNED) AS lixi\n"
            + "FROM player\n"
            + "ORDER BY lixi DESC\n"
            + "LIMIT 100;";
    public static final String queryTopArena = "SELECT id, arena_wins FROM player WHERE arena_wins > 0 ORDER BY arena_wins DESC LIMIT 100;";
    
    public static final String queryTopLuckySpins = "SELECT id, lucky_spins FROM player WHERE lucky_spins > 0 ORDER BY lucky_spins DESC LIMIT 100;";
    // Tìm đoạn khai báo các String query và thêm:
public static final String queryTopWhis = "SELECT id, CAST(JSON_UNQUOTE(JSON_EXTRACT(data_training, '$[0]')) AS UNSIGNED) AS level FROM player ORDER BY level DESC LIMIT 100;";

// Sử dụng JSON_EXTRACT để lấy id của thành viên đầu tiên trong mảng members (Bang chủ)
    public static final String queryTopBDKB = "SELECT id, name, bdkb_level, bdkb_time, "
            + "CAST(JSON_UNQUOTE(JSON_EXTRACT(members, '$[0].id')) AS UNSIGNED) AS leader_id "
            + "FROM clan WHERE bdkb_level > 0 "
            + "ORDER BY bdkb_level DESC, bdkb_time ASC LIMIT 100;";
    
    
    public static Manager gI() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public void reloadData() {
        Logger.log(Logger.YELLOW, "Bắt đầu xóa dữ liệu game cũ...");

        // Xóa sạch dữ liệu cũ trong bộ nhớ
        MAPS.clear();
        ITEM_OPTION_TEMPLATES.clear();
        ARR_HEAD_2_FRAMES.clear();
        IMAGES_BY_NAME.clear();
        ITEM_TEMPLATES.clear();
        MOB_TEMPLATES.clear();
        NPC_TEMPLATES.clear();
        TASKS.clear();
        SIDE_TASKS_TEMPLATE.clear();
        CLAN_TASKS_TEMPLATE.clear();
        ACHIEVEMENT_TEMPLATE.clear();
        INTRINSICS.clear();
        INTRINSIC_TD.clear();
        INTRINSIC_NM.clear();
        INTRINSIC_XD.clear();
        HEAD_AVATARS.clear();
        BG_ITEMS.clear();
        FLAGS_BAGS.clear();
        NCLASS.clear();
        NPCS.clear();
        SHOPS.clear();
        CLANS.clear();
        NOTIFY.clear();
        MAP_MOUNT_NUM.clear();
        if (topArena != null) {
            topArena.clear();
        }
        if (topBDKB != null) topBDKB.clear();
        if (topLuckySpins != null) topLuckySpins.clear();
        RadarService.gI().RADAR_TEMPLATE.clear();
        GiftCodeManager.gI().listGiftCode.clear();
        ConsignShopManager.gI().listItem.clear();

        Logger.log(Logger.YELLOW, "Đã xóa dữ liệu cũ. Bắt đầu tải lại dữ liệu mới...");

        // Gọi lại các hàm tải dữ liệu chính
        try {
            loadProperties(); // Tải lại file config
        } catch (IOException ex) {
            Logger.logException(Manager.class, ex, "Lỗi khi tải lại properites");
        }

        loadDatabase(); // Tải lại từ database
        initMap(); // Khởi tạo lại map dựa trên dữ liệu mới

        Logger.log(Logger.GREEN, "Tải lại dữ liệu trong Manager thành công!");
    }

    private Manager() {
            try {
                loadProperties();
            } catch (IOException ex) {
                Logger.logException(Manager.class, ex, "Lỗi load properites");
                System.exit(0);
            }

            // TỰ ĐỘNG RESIZE ICONS TRONG LUỒNG NỀN (KHÔNG CHẶN SERVER KHỞI ĐỘNG)
            new Thread(() -> ImageResizeManager.gI().initAutoResize(), "ImageResizeWorker").start();

            this.loadDatabase();
            NpcFactory.createNpcConMeo();
            NpcFactory.createNpcRongThieng();
            this.initMap();
        }

    public void initMap() {
        TileMapDataLoader.gI().initAllMaps();
    }

    public static void logLoaded(String text) {
        if (DEBUG) {
            Logger.success(text);
        }
    }

    public void loadDatabase() {
        DatabaseTemplateLoader.gI().loadAllTemplates();
    }

    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("data/config/alyra.properties"));
        Object value;
        if ((value = properties.get("server.sv")) != null) {
            SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.name")) != null) {
            String name = String.valueOf(value);
            ServerManager.NAME = name.equals("NROXUA") ? "C" : name;
        }
        if ((value = properties.get("server.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        String linkServer = "";
        if ((value = properties.get("server.ip")) != null) {
            ServerManager.IP = String.valueOf(value);
            // linkServer += ServerManager.NAME + ":" + ServerManager.IP + ":" + ServerManager.PORT + ":0,";
        }
        for (int i = 1; i <= 10; i++) {
            value = properties.get("server.sv" + i);
            if (value != null) {
                linkServer += String.valueOf(value) + ":0,";
            }
        }
//        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.waitlogin")) != null) {
            SECOND_WAIT_LOGIN = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.maxperip")) != null) {
            MAX_PER_IP = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.maxplayer")) != null) {
            MAX_PLAYER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.expserver")) != null) {
            RATE_EXP_SERVER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.debug")) != null) {
            DEBUG = Byte.parseByte(String.valueOf(value)) != 0;
        }
        if ((value = properties.get("server.local")) != null) {
            LOCAL = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.test")) != null) {
            TEST = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.daoautoupdater")) != null) {
            DAO_AUTO_UPDATER = String.valueOf(value).equalsIgnoreCase("true");
        }
    }

    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        return TileMapDataLoader.gI().readTileIndexTileType(tileTypeFocus);
    }

    private int[][] readTileMap(int mapId) {
        return TileMapDataLoader.gI().readTileMap(mapId);
    }

//    public static Clan getClanById(int id) throws Exception {
//        for (Clan clan : CLANS) {
//            if (clan.id == id) {
//                return clan;
//            }
//        }
//        throw new Exception("Không tìm thấy clan id: " + id);
//    }

    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    public static int getNumClan() {
        return CLANS.size();

    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }

    public static List<TOP> realTop(String query, Connection con) {
        List<TOP> tops = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TOP top = TOP.builder().id_player(rs.getInt("id")).build();
                switch (query) {
                    case queryTopSM:
                        top.setInfo1(Util.powerToString(rs.getLong("sm")) + " sức mạnh");
                        top.setInfo2(Util.powerToString(rs.getLong("sm")) + " sức mạnh");
                        break;
                    case queryTopNV:
                        String nvVal = rs.getString("second_value");
                        if (nvVal == null) {
                            nvVal = "0";
                        }
                        top.setInfo1(nvVal);
                        top.setInfo2(nvVal);
                        break;
                    case queryTopNap:
                        top.setInfo1(rs.getInt("danap") + " VND");
                        top.setInfo2(rs.getInt("danap") + " VND");
                        break;
                    case queryTopsk:
                        top.setInfo1(rs.getInt("sk") + " điểm");
                        top.setInfo2(rs.getInt("sk") + " điểm");
                        break;
                    case TOP_PHAO_BONG:
                        top.setInfo1(rs.getInt("phaobong") + " điểm");
                        top.setInfo2(rs.getInt("phaobong") + " điểm");
                        break;
                    case TOP_LIXI:
                        top.setInfo1(rs.getInt("lixi") + " điểm");
                        top.setInfo2(rs.getInt("lixi") + " điểm");
                        break;
                    case queryTopArena:
                        top.setInfo1("Thắng: " + rs.getInt("arena_wins") + " trận");
                        top.setInfo2("Số lần thắng Võ Đài");
                        break;                       
                     case queryTopLuckySpins:
                        top.setInfo1("Đã quay: " + rs.getInt("lucky_spins") + " lần");
                        top.setInfo2("Số lần quay Vòng Quay Đặc Biệt");
                        break;
                    case queryTopWhis:
                        top.setInfo1("Cấp độ: " + rs.getInt("level"));
                        top.setInfo2("Cấp độ luyện tập Whis");
                        break;
                  case queryTopBDKB:
                        // Ghi đè lại đối tượng top với ID của Bang Chủ
                        top = TOP.builder().id_player(rs.getInt("leader_id")).build();
                        
                        top.setInfo1("Bang: " + rs.getString("name"));
                        top.setInfo2("Cấp: " + rs.getInt("bdkb_level") + " - Thời gian: " + utils.Util.getFormatTime(rs.getLong("bdkb_time")));
                        break;

                }
                tops.add(top);
            }
        } catch (Exception e) {
            Logger.error("Lỗi trong Manager.realTop với query: " + query);
            e.printStackTrace(); // <<-- THÊM DÒNG NÀY

        }
        return tops;
    }
    

}

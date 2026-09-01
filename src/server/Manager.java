package server;

import models.radar.OptionCard;
import services.RadarService;
import models.radar.RadarCard;
import database.AlyraManager;
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
    public static String MONGO_HOST = "localhost";
    public static int MONGO_PORT = 27017;
    public static String MONGO_DATABASE_NAME = "ngocrong4u";

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

            // TỰ ĐỘNG RESIZE ICONS KHI SERVER KHỞI ĐỘNG
            ImageResizeManager.gI().initAutoResize();

            this.loadDatabase();
            NpcFactory.createNpcConMeo();
            NpcFactory.createNpcRongThieng();
            this.initMap();
        }

    private void initMap() {
        managers.map.MapDataManager.gI().loadAllMapData();
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
        for (MapTemplate mapTemp : MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            models.map.Map map = new models.map.Map(mapTemp.id,
                    mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                    mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                    mapTemp.zones,
                    mapTemp.maxPlayerPerZone, mapTemp.wayPoints, mapTemp.effectMaps);
            services.map.WeatherService.gI().initWeatherForMap(map);
            MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
        }
        new NonInteractiveNPC().initNonInteractiveNPC();
        Logger.log(Logger.GREEN, "Init map successful! \n");
    }

    private void loadDatabase() {
        long st = System.currentTimeMillis();
        JSONArray dataArray;
        JSONObject dataObject;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = AlyraManager.getConnection(); Connection conn = AlyraManager.getConnection_Data()) {
            //load part
            ps = conn.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data").replaceAll("\\\"", ""));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    part.partDetails.add(new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) {
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.partDetails) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            Logger.success("Successfully loaded part (" + parts.size() + ")\n");

            //load bg item template
            ps = conn.prepareStatement("select * from bg_item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BgItem bgItem = new BgItem();
                bgItem.id = rs.getInt("id");
                bgItem.layer = rs.getByte("layer");
                bgItem.dx = rs.getShort("dx");
                bgItem.dy = rs.getShort("dy");
                bgItem.idImage = rs.getShort("image_id");
                BG_ITEMS.add(bgItem);
            }
            Logger.success("Successfully loaded bg item template (" + BG_ITEMS.size() + ")\n");

            //load array head 2 frames
            ps = conn.prepareStatement("select * from array_head_2_frames");
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int i = 0; i < dataArray.size(); i++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(i).toString()));
                }
                ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
            Logger.success("Successfully loaded arr head 2 frames (" + ARR_HEAD_2_FRAMES.size() + ")\n");

            //load clan
            ps = con.prepareStatement("select * from clan");
            rs = ps.executeQuery();
            while (rs.next()) {
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.name2 = rs.getString("name_2");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                if (clan.level < 1) {
                    clan.level = 1;
                }
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("members"));
                for (int i = 0; i < dataArray.size(); i++) {
                    dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (NumberFormatException e) {
                    }
                    clan.addClanMember(cm);
                }
                dataArray.clear();
                CLANS.add(clan);
            }

            ps = con.prepareStatement("select id from clan order by id desc limit 1");
            rs = ps.executeQuery();
            if (rs.next()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }

            Logger.success("Successfully loaded clan (" + CLANS.size() + "), clan next id: " + Clan.NEXT_ID + "\n");

            //load skill
            ps = conn.prepareStatement("select * from skill_template order by nclass_id, slot");
            rs = ps.executeQuery();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{")
                );
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONObject dts = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
            }
            Logger.success("Successfully loaded skill (" + NCLASS.size() + ")\n");

            //load head avatar
            ps = conn.prepareStatement("select * from head_avatar");
            rs = ps.executeQuery();
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                HEAD_AVATARS.add(headAvatar);
            }
            Logger.success("Successfully loaded head avatar (" + HEAD_AVATARS.size() + ")\n");

            //load flag bag
            ps = conn.prepareStatement("select * from flag_bag");
            rs = ps.executeQuery();
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            Logger.success("Successfully loaded flag bag (" + FLAGS_BAGS.size() + ")\n");

            //load intrinsic
            ps = conn.prepareStatement("select * from intrinsic");
            rs = ps.executeQuery();
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT ->
                        INTRINSIC_TD.add(intrinsic);
                    case ConstPlayer.NAMEC ->
                        INTRINSIC_NM.add(intrinsic);
                    case ConstPlayer.XAYDA ->
                        INTRINSIC_XD.add(intrinsic);
                    default -> {
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                    }
                }
                INTRINSICS.add(intrinsic);
            }
            Logger.success("Successfully loaded intrinsic (" + INTRINSICS.size() + ")\n");

            //load task
            ps = conn.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id");
            rs = ps.executeQuery();
            int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail");
                    TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = rs.getString("sub_name");
                subTask.maxCount = rs.getShort("max_count");
                subTask.notify = rs.getString("notify");
                subTask.npcId = rs.getByte("npc_id");
                subTask.mapId = rs.getShort("map");
                task.subTasks.add(subTask);
            }
            Logger.success("Successfully loaded task (" + TASKS.size() + ")\n");

            //load side task
            ps = conn.prepareStatement("select * from side_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            Logger.success("Successfully loaded side task (" + SIDE_TASKS_TEMPLATE.size() + ")\n");

            //load clan task
            ps = conn.prepareStatement("select * from clan_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ClanTaskTemplate clanTask = new ClanTaskTemplate();
                clanTask.id = rs.getInt("id");
                clanTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                clanTask.count[0][0] = Integer.parseInt(mc1[0]);
                clanTask.count[0][1] = Integer.parseInt(mc1[1]);
                clanTask.count[1][0] = Integer.parseInt(mc2[0]);
                clanTask.count[1][1] = Integer.parseInt(mc2[1]);
                clanTask.count[2][0] = Integer.parseInt(mc3[0]);
                clanTask.count[2][1] = Integer.parseInt(mc3[1]);
                clanTask.count[3][0] = Integer.parseInt(mc4[0]);
                clanTask.count[3][1] = Integer.parseInt(mc4[1]);
                clanTask.count[4][0] = Integer.parseInt(mc5[0]);
                clanTask.count[4][1] = Integer.parseInt(mc5[1]);
                CLAN_TASKS_TEMPLATE.add(clanTask);
            }
            Logger.success("Successfully loaded clan task (" + CLAN_TASKS_TEMPLATE.size() + ")\n");

            //load achievement template
            ps = conn.prepareStatement("select * from achievement_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ACHIEVEMENT_TEMPLATE.add(new AchievementTemplate(rs.getString("info1"), rs.getString("info2"), rs.getInt("money"), rs.getLong("max_count")));
            }
            Logger.success("Successfully loaded achievement (" + ACHIEVEMENT_TEMPLATE.size() + ")\n");

            //load item template
            ps = conn.prepareStatement("select * from item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = rs.getShort("id");
                itemTemp.type = rs.getByte("type");
                itemTemp.gender = rs.getByte("gender");
                itemTemp.name = rs.getString("name");
                itemTemp.description = rs.getString("description");
                itemTemp.level = rs.getByte("level");
                itemTemp.iconID = rs.getShort("icon_id");
                itemTemp.part = rs.getShort("part");
                itemTemp.isUpToUp = itemTemp.type == 5 || itemTemp.type == 32 ? false : rs.getBoolean("is_up_to_up");
                itemTemp.strRequire = rs.getInt("power_require");
                itemTemp.gold = rs.getInt("gold");
                itemTemp.gem = rs.getInt("gem");
                itemTemp.head = rs.getInt("head");
                itemTemp.body = rs.getInt("body");
                itemTemp.leg = rs.getInt("leg");
                ITEM_TEMPLATES.add(itemTemp);
            }
            Logger.success("Successfully loaded map item template (" + ITEM_TEMPLATES.size() + ")\n");

            //load item option template
            ps = conn.prepareStatement("select id, name from item_option_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            Logger.success("Successfully loaded map item option template (" + ITEM_OPTION_TEMPLATES.size() + ")\n");

            //load shop
            SHOPS = ShopDAO.getShops(conn);
            Logger.success("Successfully loaded shop (" + SHOPS.size() + ")\n");

            //load notify
            ps = conn.prepareStatement("select * from notify order by id desc");
            rs = ps.executeQuery();
            while (rs.next()) {
                NOTIFY.add(rs.getString("name") + "<>" + rs.getString("text"));
            }
            Logger.success("Successfully loaded notify (" + NOTIFY.size() + ")\n");

            //load image by name
            ps = conn.prepareStatement("select name, n_frame from img_by_name");
            rs = ps.executeQuery();
            while (rs.next()) {
                IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            Logger.success("Successfully loaded images by name (" + IMAGES_BY_NAME.size() + ")\n");

            //Load mount
            for (ItemTemplate item : ITEM_TEMPLATES) {
                if (item.type == 23 && getNFrameImageByName("mount_" + item.part + "_0") != 0) {
                    MAP_MOUNT_NUM.put(item.id, (short) (item.part + 30000));
                }
            }
            Logger.success("Successfully loaded mount (" + MAP_MOUNT_NUM.size() + ")\n");

            //load mob template
            ps = conn.prepareStatement("select * from mob_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getByte("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                MOB_TEMPLATES.add(mobTemp);
            }
            Logger.success("Successfully loaded mob template (" + MOB_TEMPLATES.size() + ")\n");

            //load npc template
            ps = conn.prepareStatement("select * from npc_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                NPC_TEMPLATES.add(npcTemp);
            }
            Logger.success("Successfully loaded npc template (" + NPC_TEMPLATES.size() + ")\n");

            //Load item ki gui
            ps = con.prepareStatement("SELECT * FROM shop_ky_gui");
            rs = ps.executeQuery();
            while (rs.next()) {
                int i = rs.getInt("id");
                int idPl = rs.getInt("player_id");
                byte tab = rs.getByte("tab");
                short itemId = rs.getShort("item_id");
                int gold = rs.getInt("gold");
                int gem = rs.getInt("gem");
                int quantity = rs.getInt("quantity");
                byte isUp = rs.getByte("isUpTop");
                boolean isBuy = rs.getByte("isBuy") == 1;
                List<Item.ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new Item.ItemOption(idOptions, param));
                }

                ConsignShopManager.gI().listItem.add(new ConsignItem(i, itemId, idPl, tab, gold, gem, quantity, isUp, op, isBuy));
            }
//            } ps = con.prepareStatement("SELECT * FROM shop_ky_gui");
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                int i = rs.getInt("id");
//                int idPl = rs.getInt("player_id");
//                byte tab = rs.getByte("tab");
//                short itemId = rs.getShort("item_id");
//                int gold = rs.getInt("gold");
//                int gem = rs.getInt("gem");
//                int quantity = rs.getInt("quantity");
//                byte isUp = rs.getByte("isUpTop");
//                boolean isBuy = rs.getByte("isBuy") == 1;
//                List<Item.ItemOption> op = new ArrayList<>();
//                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
//                for (int j = 0; j < jsa2.size(); ++j) {
//                    JSONObject jso2 = (JSONObject) jsa2.get(j);
//                    int idOptions = Integer.parseInt(jso2.get("id").toString());
//                    int param = Integer.parseInt(jso2.get("param").toString());
//                    op.add(new Item.ItemOption(idOptions, param));
//                }
//                ConsignShopManager.gI().listItem.add(new ConsignItem(i, itemId, idPl, tab, gold, gem, quantity, isUp, op, isBuy));
//            }

            //load map template
            ps = conn.prepareStatement("select count(id) from map_template");
            rs = ps.executeQuery();
            if (rs.next()) {
                int countRow = rs.getShort(1);
                MAP_TEMPLATES = new MapTemplate[countRow];
                ps = conn.prepareStatement("select * from map_template");
                rs = ps.executeQuery();
                short i = 0;
                while (rs.next()) {
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = rs.getInt("id");
                    String mapName = rs.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    mapTemplate.type = rs.getByte("type");
                    mapTemplate.planetId = rs.getByte("planet_id");
                    mapTemplate.bgType = rs.getByte("bg_type");
                    mapTemplate.tileId = rs.getByte("tile_id");
                    mapTemplate.bgId = rs.getByte("bg_id");
                    mapTemplate.zones = rs.getByte("zones");
                    mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
                    //load waypoints
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ",")
                    );
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    //load mobs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("mobs").replaceAll("\\\"", ""));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new int[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        mapTemplate.mobHp[j] = Integer.parseInt(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    //load npcs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("npcs").replaceAll("\\\"", ""));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        dtn.clear();
                    }
                    dataArray.clear();
                    // load eff
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("effect"));
                    for (int j = 0; j < dataArray.size(); j++) {
                        EffectMap em = new EffectMap();
                        dataObject = (JSONObject) JSONValue.parse(dataArray.get(j).toString());
                        em.setKey(String.valueOf(dataObject.get("key")));
                        em.setValue(String.valueOf(dataObject.get("value")));
                        mapTemplate.effectMaps.add(em);
                    }
                    dataArray.clear();
                    MAP_TEMPLATES[i++] = mapTemplate;
                }
                Logger.success("Successfully loaded map template (" + MAP_TEMPLATES.length + ")\n");
            }

            ps = conn.prepareStatement("select * from radar");
            rs = ps.executeQuery();
            while (rs.next()) {
                RadarCard rd = new RadarCard();
                rd.Id = rs.getShort("id");
                rd.IconId = rs.getShort("iconId");
                rd.Rank = rs.getByte("rank");
                rd.Max = rs.getByte("max");
                rd.Type = rs.getByte("type");
                rd.Template = rs.getShort("template");
                rd.Name = rs.getString("name");
                rd.Info = rs.getString("info");
                JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("body"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Head = Short.parseShort(ob.get("head").toString());
                        rd.Body = Short.parseShort(ob.get("body").toString());
                        rd.Leg = Short.parseShort(ob.get("leg").toString());
                        rd.Bag = Short.parseShort(ob.get("bag").toString());
                    }
                }
                rd.Options.clear();
                arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Options.add(new OptionCard(Integer.parseInt(ob.get("id").toString()), Short.parseShort(ob.get("param").toString()), Byte.parseByte(ob.get("activeCard").toString())));
                    }
                }
                rd.Require = rs.getShort("require");
                rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
            Logger.success("Successfully loaded radar template (" + RadarService.gI().RADAR_TEMPLATE.size() + ")\n");

            //Load giftcode
            ps = con.prepareStatement("SELECT * FROM giftcode");
            rs = ps.executeQuery();
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                giftcode.code = rs.getString("code");
                giftcode.id = rs.getInt("id");
                giftcode.countLeft = rs.getInt("count_left");
                giftcode.allGender = rs.getString("allGender").equals("all") || rs.getBoolean("allGender");
                if (giftcode.countLeft == -1) {
                    giftcode.countLeft = 999999999;
                }
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);

                        int id = Integer.parseInt(jsonObj.get("id").toString());
                        int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                        JSONArray option = (JSONArray) jsonObj.get("options");
                        ArrayList<ItemOption> optionList = new ArrayList<>();

                        if (option != null) {
                            for (int u = 0; u < option.size(); u++) {
                                JSONObject jsonobject = (JSONObject) option.get(u);
                                int optionId = Integer.parseInt(jsonobject.get("id").toString());
                                int param = Integer.parseInt(jsonobject.get("param").toString());
                                optionList.add(new Item.ItemOption(optionId, param));
                            }
                        }

                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }

                GiftCodeManager.gI().listGiftCode.add(giftcode);
            }
            Logger.success("Successfully loaded giftcode (" + GiftCodeManager.gI().listGiftCode.size() + ")\n");

            File directory = new File("data/icon/x4");
            if (directory.isDirectory()) {
                Optional<File> maxFile = Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                        .filter(File::isFile)
                        .filter(file -> file.getName().endsWith(".png"))
                        .max(Comparator.comparingInt(file -> {
                            String name = file.getName();
                            return Integer.parseInt(name.substring(0, name.length() - 4));
                        }));
                if (maxFile.isPresent()) {
                    String fileName = maxFile.get().getName();
                    short maxVersion = Short.parseShort(fileName.substring(0, fileName.length() - 4));
                    DataGame.maxSmallVersion = (short) (maxVersion + 1);
                    Logger.success("Successfully loaded max small version (" + DataGame.maxSmallVersion + ")\n");
                }
            }
            toplx = realTop(TOP_LIXI, con);
            Logger.success("LOAD TOP LIXI(" + toplx.size() + ")\n");
            toppb = realTop(TOP_PHAO_BONG, con);
            Logger.success("LOAD TOP PHAO BONG(" + toppb.size() + ")\n");
            topSM = realTop(queryTopSM, con);
            Logger.success("LOAD TOP SUC MANH(" + topSM.size() + ")\n");
            topNV = realTop(queryTopNV, con);
            Logger.success("LOAD TOP NHIEM VU (" + topNV.size() + ")\n");
            topNap = realTop(queryTopNap, con);
            Logger.success("LOAD TOP NAP (" + topNap.size() + ")\n");
            topsk = realTop(queryTopsk, con);
            Logger.success("LOAD SU KIEN (" + topsk.size() + ")\n");
            topArena = realTop(queryTopArena, con);
            Logger.success("LOAD SU KIEN (" + topArena.size() + ")\n");
            topLuckySpins = realTop(queryTopLuckySpins, con);
            Logger.success("LOAD SU KIEN (" + topLuckySpins.size() + ")\n");
            
            
            
        } catch (Exception e) {
            Logger.logException(Manager.class, e, "Database loading error");
            System.exit(0);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                AlyraManager.close_data();
            } catch (SQLException e) {
            }
        }

        Logger.log(Logger.GREEN, "Total database loading time: " + (System.currentTimeMillis() - st) + " (ms)\n");

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
            DAO_AUTO_UPDATER = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.daoautoupdater")) != null) {
            DAO_AUTO_UPDATER = String.valueOf(value).equalsIgnoreCase("true");
        }
        if ((value = properties.get("mongo.host")) != null) {
            MONGO_HOST = String.valueOf(value);
        }
        if ((value = properties.get("mongo.port")) != null) {
            MONGO_PORT = Integer.parseInt(String.valueOf(value));
        }
    }

    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;

                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.logException(MapService.class, e);
        }
        return tileIndexTileType;
    }

    /**
     * @param mapId mapId
     * @return tile map for paint
     */
    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_map_data/" + mapId))) {
                int w = dis.readByte();
                int h = dis.readByte();
                tileMap = new int[h][w];
                for (int[] tm : tileMap) {
                    for (int j = 0; j < tm.length; j++) {
                        tm[j] = dis.readByte();
                    }
                }
            }
        } catch (IOException e) {
        }
        return tileMap;
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

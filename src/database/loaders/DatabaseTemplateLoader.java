package database.loaders;

import consts.ConstPlayer;
import data.DataGame;
import database.AlyraManager;
import database.daos.ShopDAO;
import managers.ConsignShopManager;
import managers.GiftCodeManager;
import models.ConsignItem;
import models.GiftCode;
import models.Template.*;
import models.clan.Clan;
import models.clan.ClanMember;
import models.intrinsic.Intrinsic;
import models.item.Item;
import models.item.Item.ItemOption;
import models.map.EffectMap;
import models.map.WayPoint;
import models.radar.OptionCard;
import models.radar.RadarCard;
import models.skill.NClass;
import models.skill.Skill;
import models.task.ClanTaskTemplate;
import models.task.SideTaskTemplate;
import models.task.SubTaskMain;
import models.task.TaskMain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.Manager;
import services.RadarService;
import utils.Logger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static data.DataGame.MAP_MOUNT_NUM;

/**
 * Chuyên trách nạp toàn bộ cấu trúc dữ liệu Template của trò chơi từ cơ sở dữ liệu MySQL.
 * Tách biệt khỏi Manager.java theo Single Responsibility Principle (SRP).
 */
public class DatabaseTemplateLoader {

    private static DatabaseTemplateLoader instance;

    public static synchronized DatabaseTemplateLoader gI() {
        if (instance == null) {
            instance = new DatabaseTemplateLoader();
        }
        return instance;
    }

    private DatabaseTemplateLoader() {
    }

    /**
     * Nạp toàn bộ dữ liệu bảng từ cơ sở dữ liệu vào các static collections của Manager.
     */
    public void loadAllTemplates() {
        long st = System.currentTimeMillis();
        JSONArray dataArray;
        JSONObject dataObject;

        try {
            // 1. Load part
            List<Part> parts = new ArrayList<>();
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from part ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
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
                        }
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/update_data/part"))) {
                dos.writeShort(parts.size());
                for (Part part : parts) {
                    dos.writeByte(part.type);
                    int expected = (part.type == 0) ? 3 : (part.type == 1) ? 17 : (part.type == 2) ? 14 : (part.type == 3) ? 2 : part.partDetails.size();
                    if (part.partDetails.size() < expected) {
                        Logger.warning("[Part Export] Part ID " + part.id + " (type=" + part.type + ") thiếu frame (" + part.partDetails.size() + "/" + expected + "). Tự động thêm dummy detail [2955, 0, 0] để bảo toàn stream!");
                        while (part.partDetails.size() < expected) {
                            part.partDetails.add(new PartDetail((short) 2955, (byte) 0, (byte) 0));
                        }
                    } else if (part.partDetails.size() > expected && expected > 0) {
                        Logger.warning("[Part Export] Part ID " + part.id + " (type=" + part.type + ") thừa frame (" + part.partDetails.size() + "/" + expected + "). Cắt bớt để khớp cấu trúc Client!");
                        while (part.partDetails.size() > expected) {
                            part.partDetails.remove(part.partDetails.size() - 1);
                        }
                    }
                    for (PartDetail partDetail : part.partDetails) {
                        dos.writeShort(partDetail.iconId);
                        dos.writeByte(partDetail.dx);
                        dos.writeByte(partDetail.dy);
                    }
                }
                dos.flush();
            }
            DataGame.reloadPart();
            Manager.logLoaded("Successfully loaded part (" + parts.size() + ")\n");

// 2. Load bg item template
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from bg_item_template ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                BgItem bgItem = new BgItem();
                bgItem.id = rs.getInt("id");
                bgItem.layer = rs.getByte("layer");
                bgItem.dx = rs.getShort("dx");
                bgItem.dy = rs.getShort("dy");
                bgItem.idImage = rs.getShort("image_id");
                Manager.BG_ITEMS.add(bgItem);
            }
            }
            Manager.logLoaded("Successfully loaded bg item template (" + Manager.BG_ITEMS.size() + ")\n");

            // 3. Load array head 2 frames
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from array_head_2_frames");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int i = 0; i < dataArray.size(); i++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(i).toString()));
                }
                Manager.ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
            }
            Manager.logLoaded("Successfully loaded arr head 2 frames (" + Manager.ARR_HEAD_2_FRAMES.size() + ")\n");

            // 4. Load clan
            try (Connection con = AlyraManager.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("select * from clan");
                     ResultSet rs = ps.executeQuery()) {
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
                try {
                    clan.bdkb_level = rs.getInt("bdkb_level");
                    clan.bdkb_time = rs.getLong("bdkb_time");
                } catch (Exception ignored) {
                }
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
                    } catch (NumberFormatException ignored) {
                    }
                    clan.addClanMember(cm);
                }
                dataArray.clear();
                Manager.CLANS.add(clan);
            }
                }
                try (PreparedStatement ps = con.prepareStatement("select id from clan order by id desc limit 1");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }
                }
            }
            Manager.logLoaded("Successfully loaded clan (" + Manager.CLANS.size() + "), clan next id: " + Clan.NEXT_ID + "\n");

            // 5. Load skill template
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from skill_template order by nclass_id, slot");
                 ResultSet rs = ps.executeQuery()) {
                byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    Manager.NCLASS.add(nClass);
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

                String skillsRaw = rs.getString("skills");
                dataArray = null;
                try {
                    String cleaned = skillsRaw
                            .replaceAll("\\\\\"", "\"")
                            .replaceAll("\\[\"", "[")
                            .replaceAll("\"\\[", "[")
                            .replaceAll("\"\\]", "]")
                            .replaceAll("\\]\"", "]")
                            .replaceAll("\\}\"\\s*,\\s*\"\\{", "},{")
                            .replaceAll("\\}\",\"\\{", "},{");
                    Object parsed = JSONValue.parse(cleaned);
                    if (parsed instanceof JSONArray ja) {
                        dataArray = ja;
                    }
                } catch (Exception ignored) {
                }
                if (dataArray == null) {
                    try {
                        Object parsed = JSONValue.parse(skillsRaw);
                        if (parsed instanceof JSONArray ja) {
                            dataArray = ja;
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (dataArray == null) {
                    dataArray = new JSONArray();
                }
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
            }
            Manager.logLoaded("Successfully loaded skill (" + Manager.NCLASS.size() + ")\n");

            // 6. Load head avatar
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from head_avatar");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                Manager.HEAD_AVATARS.add(headAvatar);
            }
            }
            Manager.logLoaded("Successfully loaded head avatar (" + Manager.HEAD_AVATARS.size() + ")\n");

            // 7. Load flag bag
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from flag_bag");
                 ResultSet rs = ps.executeQuery()) {
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
                Manager.FLAGS_BAGS.add(flagBag);
            }
            }
            Manager.logLoaded("Successfully loaded flag bag (" + Manager.FLAGS_BAGS.size() + ")\n");

            // 8. Load intrinsic
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from intrinsic");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name") != null ? rs.getString("name").replace("\\r\\n", "\n").replace("\\n", "\n") : "";
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT -> Manager.INTRINSIC_TD.add(intrinsic);
                    case ConstPlayer.NAMEC -> Manager.INTRINSIC_NM.add(intrinsic);
                    case ConstPlayer.XAYDA -> Manager.INTRINSIC_XD.add(intrinsic);
                    default -> {
                        Manager.INTRINSIC_TD.add(intrinsic);
                        Manager.INTRINSIC_NM.add(intrinsic);
                        Manager.INTRINSIC_XD.add(intrinsic);
                    }
                }
                Manager.INTRINSICS.add(intrinsic);
            }
            }
            Manager.logLoaded("Successfully loaded intrinsic (" + Manager.INTRINSICS.size() + ")\n");

            // 9. Load task main
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id");
                 ResultSet rs = ps.executeQuery()) {
                int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail") != null ? rs.getString("detail").replace("\\r\\n", "\n").replace("\\n", "\n") : "";
                    if (taskId == 16) {
                        task.name = "Vô địch Võ Đài Sinh Tử";
                        task.detail = "Đến Đảo Kamê gặp Bà Hạt Mít, tham gia và chiến thắng cả 5 hiệp để Vô địch Võ Đài Sinh Tử.\nThưởng 150 k sức mạnh\nThưởng 150 k tiềm năng";
                    }
                    Manager.TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                if (task.id == 16 && task.subTasks.isEmpty()) {
                    subTask.name = "Vô địch Võ Đài Sinh Tử";
                    subTask.maxCount = 1;
                    subTask.notify = "Hãy đến Đảo Kamê gặp Bà Hạt Mít tham gia và vô địch Võ Đài Sinh Tử.";
                    subTask.npcId = consts.ConstNpc.BA_HAT_MIT;
                    subTask.mapId = 5;
                } else {
                    subTask.name = rs.getString("sub_name");
                    subTask.maxCount = rs.getShort("max_count");
                    subTask.notify = rs.getString("notify");
                    subTask.npcId = rs.getByte("npc_id");
                    subTask.mapId = rs.getShort("map");
                }
                task.subTasks.add(subTask);
            }
            }
            Manager.logLoaded("Successfully loaded task (" + Manager.TASKS.size() + ")\n");

            // 10. Load side task
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from side_task_template");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                int multiplier = 1; // Số lượng quái tiêu diệt chuẩn gameplay NRO (giảm 10 lần)
                sideTask.count[0][0] = Integer.parseInt(mc1[0]) * multiplier;
                sideTask.count[0][1] = Integer.parseInt(mc1[1]) * multiplier;
                sideTask.count[1][0] = Integer.parseInt(mc2[0]) * multiplier;
                sideTask.count[1][1] = Integer.parseInt(mc2[1]) * multiplier;
                sideTask.count[2][0] = Integer.parseInt(mc3[0]) * multiplier;
                sideTask.count[2][1] = Integer.parseInt(mc3[1]) * multiplier;
                sideTask.count[3][0] = Integer.parseInt(mc4[0]) * multiplier;
                sideTask.count[3][1] = Integer.parseInt(mc4[1]) * multiplier;
                sideTask.count[4][0] = Integer.parseInt(mc5[0]) * multiplier;
                sideTask.count[4][1] = Integer.parseInt(mc5[1]) * multiplier;
                Manager.SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            }
            Manager.logLoaded("Successfully loaded side task (" + Manager.SIDE_TASKS_TEMPLATE.size() + ")\n");

            // 11. Load clan task
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from clan_task_template");
                 ResultSet rs = ps.executeQuery()) {
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
                Manager.CLAN_TASKS_TEMPLATE.add(clanTask);
            }
            }
            Manager.logLoaded("Successfully loaded clan task (" + Manager.CLAN_TASKS_TEMPLATE.size() + ")\n");

            // 12. Load achievement template
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from achievement_template");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                Manager.ACHIEVEMENT_TEMPLATE.add(new AchievementTemplate(rs.getString("info1"), rs.getString("info2"), rs.getInt("money"), rs.getLong("max_count")));
            }
            }
            Manager.logLoaded("Successfully loaded achievement (" + Manager.ACHIEVEMENT_TEMPLATE.size() + ")\n");

            // 13. Load item template
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from item_template ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = rs.getShort("id");
                itemTemp.type = rs.getByte("type");
                itemTemp.gender = rs.getByte("gender");
                itemTemp.name = rs.getString("name");
                itemTemp.description = rs.getString("description");
                if (itemTemp.id == 191 || itemTemp.id == 192) {
                    itemTemp.description = "Tác dụng trong 10 phút";
                } else {
                    itemTemp.description = getCardDescription(itemTemp.id, itemTemp.description);
                }
                normalizeSaoPhaLeTemplate(itemTemp);
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
                Manager.ITEM_TEMPLATES.add(itemTemp);
            }
            }
            if (Manager.ITEM_TEMPLATES.size() <= consts.ConstItem.CHUOI) {
                boolean hasChuoi = false;
                for (ItemTemplate it : Manager.ITEM_TEMPLATES) {
                    if (it.id == consts.ConstItem.CHUOI) {
                        hasChuoi = true;
                        break;
                    }
                }
                if (!hasChuoi) {
                    ItemTemplate chuoi = new ItemTemplate();
                    chuoi.id = (short) consts.ConstItem.CHUOI;
                    chuoi.type = 27;
                    chuoi.gender = 3;
                    chuoi.name = "Chuối";
                    chuoi.description = "Tác dụng trong 10 phút";
                    chuoi.level = 0;
                    chuoi.iconID = 1042;
                    chuoi.part = -1;
                    chuoi.isUpToUp = true;
                    chuoi.strRequire = 0;
                    chuoi.gold = 0;
                    chuoi.gem = 0;
                    chuoi.head = -1;
                    chuoi.body = -1;
                    chuoi.leg = -1;
                    Manager.ITEM_TEMPLATES.add(chuoi);
                }
            }
            Manager.logLoaded("Successfully loaded map item template (" + Manager.ITEM_TEMPLATES.size() + ")\n");

            // 14. Load item option template
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select id, name from item_option_template ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                Manager.ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            }
            Manager.logLoaded("Successfully loaded map item option template (" + Manager.ITEM_OPTION_TEMPLATES.size() + ")\n");

            // 15. Load shop
            try (Connection con = AlyraManager.getConnection()) {
                Manager.SHOPS = ShopDAO.getShops(con);
            }
            Manager.logLoaded("Successfully loaded shop (" + Manager.SHOPS.size() + ")\n");

// 16. Load notify
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from notify order by id desc");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                Manager.NOTIFY.add(rs.getString("name") + "<>" + rs.getString("text"));
            }
            }
            Manager.logLoaded("Successfully loaded notify (" + Manager.NOTIFY.size() + ")\n");

            // 17. Load image by name
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select name, n_frame from img_by_name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                Manager.IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            }
            Manager.logLoaded("Successfully loaded images by name (" + Manager.IMAGES_BY_NAME.size() + ")\n");

            // 18. Load mount
            for (ItemTemplate item : Manager.ITEM_TEMPLATES) {
                if (item.type == 23 && Manager.getNFrameImageByName("mount_" + item.part + "_0") != 0) {
                    MAP_MOUNT_NUM.put(item.id, (short) (item.part + 30000));
                }
            }
            Manager.logLoaded("Successfully loaded mount (" + MAP_MOUNT_NUM.size() + ")\n");

            // 19. Load mob template
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from mob_template ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
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
                Manager.MOB_TEMPLATES.add(mobTemp);
            }
            }
            Manager.logLoaded("Successfully loaded mob template (" + Manager.MOB_TEMPLATES.size() + ")\n");

            // 20. Load npc template
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from npc_template ORDER BY id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                Manager.NPC_TEMPLATES.add(npcTemp);
            }
            }
            Manager.logLoaded("Successfully loaded npc template (" + Manager.NPC_TEMPLATES.size() + ")\n");

            // 21. Load item ki gui
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM shop_ky_gui");
                 ResultSet rs = ps.executeQuery()) {
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
                List<ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new ItemOption(idOptions, param));
                }
                ConsignShopManager.gI().listItem.add(new ConsignItem(i, itemId, idPl, tab, gold, gem, quantity, isUp, op, isBuy));
            }
            }

// 22. Load map template
            try (Connection con = AlyraManager.getConnection()) {
                int countRow = 0;
                try (PreparedStatement ps = con.prepareStatement("select count(id) from map_template");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        countRow = rs.getShort(1);
                    }
                }
                if (countRow > 0) {
                    Manager.MAP_TEMPLATES = new MapTemplate[countRow];
                    try (PreparedStatement ps = con.prepareStatement("select * from map_template ORDER BY id ASC");
                         ResultSet rs = ps.executeQuery()) {
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

                    // Load waypoints
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

                    // Load mobs
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

                    // Load npcs
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

                    // Load eff
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("effect"));
                    for (int j = 0; j < dataArray.size(); j++) {
                        EffectMap em = new EffectMap();
                        dataObject = (JSONObject) JSONValue.parse(dataArray.get(j).toString());
                        em.setKey(String.valueOf(dataObject.get("key")));
                        em.setValue(String.valueOf(dataObject.get("value")));
                        mapTemplate.effectMaps.add(em);
                    }
                    dataArray.clear();
                    Manager.MAP_TEMPLATES[i++] = mapTemplate;
                }
                    }
                    Manager.logLoaded("Successfully loaded map template (" + Manager.MAP_TEMPLATES.length + ")\n");
            }
            }

// 23. Load radar
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("select * from radar");
                 ResultSet rs = ps.executeQuery()) {
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
                List<OptionCard> balancedOpts = getBalancedRadarOptions(rd.Id);
                if (balancedOpts != null && !balancedOpts.isEmpty()) {
                    rd.Options.addAll(balancedOpts);
                } else {
                    arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject ob = (JSONObject) arr.get(i);
                        if (ob != null) {
                            rd.Options.add(new OptionCard(Integer.parseInt(ob.get("id").toString()), Short.parseShort(ob.get("param").toString()), Byte.parseByte(ob.get("activeCard").toString())));
                        }
                    }
                }
                rd.Require = rs.getShort("require");
                rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
            RadarService.gI().initCardsByRank();
            }
            Manager.logLoaded("Successfully loaded radar template (" + RadarService.gI().RADAR_TEMPLATE.size() + ")\n");

            // 24. Load giftcode
            try (Connection con = AlyraManager.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode");
                 ResultSet rs = ps.executeQuery()) {
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
                                optionList.add(new ItemOption(optionId, param));
                            }
                        }
                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }
                GiftCodeManager.gI().listGiftCode.add(giftcode);
            }
            }
            Manager.logLoaded("Successfully loaded giftcode (" + GiftCodeManager.gI().listGiftCode.size() + ")\n");

            // 25. Small version check
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
                    Manager.logLoaded("Successfully loaded max small version (" + DataGame.maxSmallVersion + ")\n");
                }
            }

            // 26. Real TOP tables
            try (Connection con = AlyraManager.getConnection()) {
                Manager.toplx = Manager.realTop(Manager.TOP_LIXI, con);
            Manager.logLoaded("LOAD TOP LIXI(" + Manager.toplx.size() + ")\n");
            Manager.toppb = Manager.realTop(Manager.TOP_PHAO_BONG, con);
            Manager.logLoaded("LOAD TOP PHAO BONG(" + Manager.toppb.size() + ")\n");
            Manager.topSM = Manager.realTop(Manager.queryTopSM, con);
            Manager.logLoaded("LOAD TOP SUC MANH(" + Manager.topSM.size() + ")\n");
            Manager.topNV = Manager.realTop(Manager.queryTopNV, con);
            Manager.logLoaded("LOAD TOP NHIEM VU (" + Manager.topNV.size() + ")\n");
            Manager.topNap = Manager.realTop(Manager.queryTopNap, con);
            Manager.logLoaded("LOAD TOP NAP (" + Manager.topNap.size() + ")\n");
            Manager.topsk = Manager.realTop(Manager.queryTopsk, con);
            Manager.logLoaded("LOAD SU KIEN (" + Manager.topsk.size() + ")\n");
            Manager.topArena = Manager.realTop(Manager.queryTopArena, con);
            Manager.logLoaded("LOAD SU KIEN (" + Manager.topArena.size() + ")\n");
            Manager.topLuckySpins = Manager.realTop(Manager.queryTopLuckySpins, con);
            Manager.logLoaded("LOAD SU KIEN (" + Manager.topLuckySpins.size() + ")\n");
            }

        } catch (Exception e) {
            Logger.logException(Manager.class, e, "Database loading error");
            System.exit(0);
        }

        int mapCount = Manager.MAP_TEMPLATES != null ? Manager.MAP_TEMPLATES.length : 0;
        Logger.log(Logger.GREEN, String.format(">> [DATABASE] Load thanh cong: %d Maps, %d Mobs, %d NPCs, %d Items, %d Clans trong %d ms!\n",
                mapCount, Manager.MOB_TEMPLATES.size(), Manager.NPC_TEMPLATES.size(), Manager.ITEM_TEMPLATES.size(), Manager.CLANS.size(), (System.currentTimeMillis() - st)));
    }

    public static void reloadAchievementTemplate() {
        try (java.sql.Connection con = AlyraManager.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement("select * from achievement_template ORDER BY id ASC");
             java.sql.ResultSet rs = ps.executeQuery()) {
            java.util.List<AchievementTemplate> list = new java.util.ArrayList<>();
            while (rs.next()) {
                list.add(new AchievementTemplate(rs.getString("info1"), rs.getString("info2"), rs.getInt("money"), rs.getLong("max_count")));
            }
            Manager.ACHIEVEMENT_TEMPLATE.clear();
            Manager.ACHIEVEMENT_TEMPLATE.addAll(list);
            Manager.logLoaded("Successfully reloaded achievement (" + Manager.ACHIEVEMENT_TEMPLATE.size() + ")\n");
        } catch (Exception e) {
            Logger.logException(DatabaseTemplateLoader.class, e);
        }
    }

    public static String getCardDescription(short id, String defaultDesc) {
        return switch (id) {
            case 828 -> "Hai chi trước của Khủng long rất ngắn nên chúng không thể cầm thức ăn được.";
            case 829 -> "Lợn lòi có sở thích mài răng nanh dưới đất, vô tình tạo ra những rãnh đất để người Namec trồng trọt.";
            case 830 -> "Bản tính của Quỷ đất khá nhút nhát, chúng thường núp vào cây khi gặp người lạ.";
            case 831 -> "Khủng long mẹ với bản năng bảo vệ con hung dữ, nhưng hai chi trước vẫn quá ngắn để bế con.";
            case 832 -> "Lợn lòi mẹ cày xới đất cực khỏe, được coi là 'máy cày đất' tự nhiên của hành tinh Namec.";
            case 833 -> "Quỷ đất mẹ to lớn nhưng vẫn giữ tính nhút nhát, hễ nghe tiếng động lạ là cố trốn sau bụi cây.";
            case 834 -> "Thằn lằn bay dùng mỏ dài và cứng để tấn công kẻ thù, đôi khi chúng vẫn dùng để gõ hạt óc chó ăn.";
            case 835 -> "Dùng tốc độ cực nhanh để tiếp cận mục tiêu. Phi long khi bay luôn há to miệng để đón gió.";
            case 836 -> "Sở trường tấn công chớp nhoáng kẻ thù từ trên cao, nhưng đâu ai ngờ Quỷ bay lại sợ độ cao.";
            case 837 -> "Quân lính Red Ribbon được huấn luyện nghiêm ngặt, nhưng ra trận chưa đầy 5 phút là đã đầu hàng.";
            case 838 -> "Lính Red Ribbon trang bị súng ống tối tân nhưng độ chính xác lại phụ thuộc vào... may rủi.";
            case 839 -> "Được lính độc nhãn thuần hóa để giữ doanh trại, nhưng sở thích lớn nhất lại là chơi nhặt bóng.";
            case 840 -> "Thân hình hơi béo, luôn đeo chiếc khăn quàng đỏ và thường hay mơ mộng làm thơ giữa trận chiến.";
            case 841 -> "Được xưng là ninja bậc thầy, nhưng kỹ thuật ẩn thân lại không có nên không thể làm sát thủ.";
            case 842 -> "Sĩ quan sở hữu đôi mắt thôi miên cực mạnh, nhưng điểm yếu chết người là cực kỳ sợ chuột.";
            case 859 -> "Cần dùng đủ 120 thẻ của 5 loại thẻ : Lính Độc Nhãn, Sói Xám, Trung Úy Trắng, Ninja Áo Tím, Trung Úy Xanh Lơ để có thể sử dụng thẻ này";
            case 956 -> "Đội trưởng Vàng là con hổ hình người chắc nịch, từng bị Goku đấm văng ra khỏi máy bay giữa trời.";
            case 1278 -> "Oozaru là sinh vật khổng lồ của người Xayda khi nhìn thấy trăng tròn, sức tàn phá kinh thiên động địa.";
            case 1276 -> "Hình thái Khỉ Đột Siêu Saiyan huyền thoại với bộ lông hoàng kim rực sáng, sức mạnh bùng nổ vô tiền khoáng hậu.";
            case 1277 -> "Hình thái Siêu Saiyan 3 tối thượng của Khỉ Đột khổng lồ, bao bọc bởi luồng hào quang sấm sét hủy diệt.";
            default -> (defaultDesc != null) ? defaultDesc : "";
        };
    }

    private static void normalizeSaoPhaLeTemplate(ItemTemplate itemTemp) {
        switch (itemTemp.id) {
            case 441 -> {
                itemTemp.name = "Sao pha lê đỏ";
                itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% hút máu";
            }
            case 442 -> {
                itemTemp.name = "Sao pha lê xanh dương";
                itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% hút KI";
            }
            case 443 -> {
                itemTemp.name = "Sao pha lê đỏ hồng";
                itemTemp.description = "Ép vào đồ sao pha lê để phản lại 5% sát thương nhận vào";
            }
            case 444 -> {
                itemTemp.name = "Sao pha lê tím";
                itemTemp.description = "Ép vào đồ sao pha lê để tăng 2% sát thương khi đánh quái";
            }
            case 445 -> {
                itemTemp.name = "Sao pha lê cam";
                itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% may mắn nhặt vật phẩm từ quái";
            }
            case 446 -> {
                itemTemp.name = "Sao pha lê vàng";
                itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% vàng từ quái";
            }
            case 447 -> {
                itemTemp.name = "Sao pha lê xanh lá";
                itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% sức mạnh, tiềm năng";
            }
            case 1352 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% hút máu";
            case 1353 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% hút KI";
            case 1354 -> itemTemp.description = "Ép vào đồ sao pha lê để phản lại 5% sát thương nhận vào";
            case 1355 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 3% sát thương khi đánh quái";
            case 1356 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 6% may mắn nhặt vật phẩm từ quái";
            case 1357 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% vàng từ quái";
            case 1358 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 5% sức mạnh, tiềm năng";
            case 1359 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 6% hút máu";
            case 1360 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 6% hút KI";
            case 1361 -> itemTemp.description = "Ép vào đồ sao pha lê để phản lại 6% sát thương nhận vào";
            case 1362 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 4% sát thương khi đánh quái";
            case 1363 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 7% may mắn nhặt vật phẩm từ quái";
            case 1364 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 6% vàng từ quái";
            case 1365 -> itemTemp.description = "Ép vào đồ sao pha lê để tăng 6% sức mạnh, tiềm năng";
            default -> {}
        }
    }

    public static List<OptionCard> getBalancedRadarOptions(short cardId) {
        List<OptionCard> list = new ArrayList<>();
        switch (cardId) {
            // Rank 0: Quái sơ cấp (Hỗ trợ tân thủ đầu game - Flat stats)
            case 828 -> { // Thẻ Khủng long (Tấn công flat)
                list.add(new OptionCard(0, 100, (byte) 0));
                list.add(new OptionCard(0, 200, (byte) 1));
                list.add(new OptionCard(0, 300, (byte) 2));
            }
            case 829 -> { // Thẻ Lợn lòi (KI flat)
                list.add(new OptionCard(7, 1000, (byte) 0));
                list.add(new OptionCard(7, 2000, (byte) 1));
                list.add(new OptionCard(7, 3000, (byte) 2));
            }
            case 830 -> { // Thẻ Quỷ đất (HP flat)
                list.add(new OptionCard(6, 1000, (byte) 0));
                list.add(new OptionCard(6, 2000, (byte) 1));
                list.add(new OptionCard(6, 3000, (byte) 2));
            }
            // Rank 1: Quái mẹ (Flat stats tầm trung)
            case 831 -> { // Thẻ Khủng long mẹ (Tấn công flat)
                list.add(new OptionCard(0, 200, (byte) 0));
                list.add(new OptionCard(0, 400, (byte) 1));
                list.add(new OptionCard(0, 600, (byte) 2));
            }
            case 832 -> { // Thẻ Lợn lòi mẹ (KI flat)
                list.add(new OptionCard(7, 2000, (byte) 0));
                list.add(new OptionCard(7, 4000, (byte) 1));
                list.add(new OptionCard(7, 6000, (byte) 2));
            }
            case 833 -> { // Thẻ Quỷ đất mẹ (HP flat)
                list.add(new OptionCard(6, 2000, (byte) 0));
                list.add(new OptionCard(6, 4000, (byte) 1));
                list.add(new OptionCard(6, 6000, (byte) 2));
            }
            // Rank 2: Quái bay (Chỉ số % HP/KI & Giáp flat)
            case 834 -> { // Thẻ Thằn lằn bay (HP %)
                list.add(new OptionCard(77, 1, (byte) 0));
                list.add(new OptionCard(77, 1, (byte) 1));
                list.add(new OptionCard(77, 2, (byte) 2));
            }
            case 835 -> { // Thẻ Phi long (KI %)
                list.add(new OptionCard(103, 1, (byte) 0));
                list.add(new OptionCard(103, 1, (byte) 1));
                list.add(new OptionCard(103, 2, (byte) 2));
            }
            case 836 -> { // Thẻ Quỷ bay (Giáp flat)
                list.add(new OptionCard(47, 150, (byte) 0));
                list.add(new OptionCard(47, 250, (byte) 1));
                list.add(new OptionCard(47, 400, (byte) 2));
            }
            // Rank 3: Lính Red Ribbon & Sói xám (Flat stats nâng cao)
            case 837 -> { // Thẻ Lính Độc Nhãn 1 (Tấn công flat)
                list.add(new OptionCard(0, 300, (byte) 0));
                list.add(new OptionCard(0, 700, (byte) 1));
                list.add(new OptionCard(0, 1000, (byte) 2));
            }
            case 838 -> { // Thẻ Lính Độc Nhãn 2 (KI flat)
                list.add(new OptionCard(7, 3000, (byte) 0));
                list.add(new OptionCard(7, 7000, (byte) 1));
                list.add(new OptionCard(7, 10000, (byte) 2));
            }
            case 839 -> { // Thẻ Sói xám (HP flat)
                list.add(new OptionCard(6, 3000, (byte) 0));
                list.add(new OptionCard(6, 7000, (byte) 1));
                list.add(new OptionCard(6, 10000, (byte) 2));
            }
            // Rank 4: Boss Doanh Trại & Vòng Quay (% Sức đánh / HP / Giáp / Né / Chí mạng)
            case 840 -> { // Thẻ Trung Úy Trắng (Giáp % / HP % / Dame %)
                list.add(new OptionCard(94, 1, (byte) 0));
                list.add(new OptionCard(77, 2, (byte) 1));
                list.add(new OptionCard(50, 3, (byte) 2));
            }
            case 841 -> { // Thẻ Ninja Áo Tím (Né đòn % / Dame % / Crit %)
                list.add(new OptionCard(108, 2, (byte) 0));
                list.add(new OptionCard(50, 2, (byte) 1));
                list.add(new OptionCard(14, 2, (byte) 2));
            }
            case 842 -> { // Thẻ Trung Úy Xanh Lơ (KI % / HP % / Giáp %)
                list.add(new OptionCard(103, 2, (byte) 0));
                list.add(new OptionCard(77, 2, (byte) 1));
                list.add(new OptionCard(94, 2, (byte) 2));
            }
            case 859 -> { // Thẻ Độc Nhãn (Dame % / HP % / Dame %)
                list.add(new OptionCard(50, 2, (byte) 0));
                list.add(new OptionCard(77, 3, (byte) 1));
                list.add(new OptionCard(50, 3, (byte) 2));
            }
            case 956 -> { // Thẻ Đội Trưởng Vàng (Dame % / HP % - Aura Vàng Hoàng Kim 0)
                list.add(new OptionCard(50, 2, (byte) 0));
                list.add(new OptionCard(50, 3, (byte) 1));
                list.add(new OptionCard(77, 4, (byte) 2));
            }
            // Rank 5: Thần Thoại Oozaru (Aura 2, 3, 4)
            case 1278 -> { // Thẻ Oozaru Khỉ Đen (Chí mạng / Sức đánh % / HP % / Giáp % - Aura 2)
                list.add(new OptionCard(14, 1, (byte) 0));
                list.add(new OptionCard(50, 3, (byte) 1));
                list.add(new OptionCard(77, 5, (byte) 2));
                list.add(new OptionCard(50, 4, (byte) 2));
                list.add(new OptionCard(94, 2, (byte) 2));
            }
            case 1276 -> { // Thẻ Oozaru SSJ Khỉ Vàng (Dame % / HP % / Crit % - Aura 3)
                list.add(new OptionCard(50, 2, (byte) 0));
                list.add(new OptionCard(77, 3, (byte) 1));
                list.add(new OptionCard(50, 5, (byte) 2));
                list.add(new OptionCard(14, 2, (byte) 2));
                list.add(new OptionCard(77, 4, (byte) 2));
            }
            case 1277 -> { // Thẻ Oozaru SSJ3 Siêu VIP (Jackpot Vòng Quay Thần Thoại - Aura 4)
                list.add(new OptionCard(50, 3, (byte) 0));
                list.add(new OptionCard(77, 3, (byte) 0));
                list.add(new OptionCard(50, 4, (byte) 1));
                list.add(new OptionCard(77, 4, (byte) 1));
                list.add(new OptionCard(14, 3, (byte) 2));
                list.add(new OptionCard(94, 3, (byte) 2));
                list.add(new OptionCard(50, 6, (byte) 2));
                list.add(new OptionCard(77, 6, (byte) 2));
            }
            default -> {
                return null;
            }
        }
        return list;
    }
}

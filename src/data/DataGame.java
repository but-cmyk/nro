package data;

import models.Template.HeadAvatar;
import models.Template.MapTemplate;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import utils.FileIO;
import services.Service;
import models.skill.NClass;
import models.skill.Skill;
import models.Template.MobTemplate;
import models.Template.NpcTemplate;
import models.Template.SkillTemplate;
import interfaces.ISession;
import network.io.Message;
import java.io.IOException;
import server.Manager;
import network.session.MySession;
import utils.Logger;
import models.Template.BgItem;
import models.player.Player;

public class DataGame {

    public static byte vsData = 11;
    public static byte vsMap = 2;
    public static byte vsSkill = 1;
    public static byte vsItem = 9;
    public static int vsRes = 1;
    public static short maxSmallVersion = 32767;

    public static Map<Object, Object> MAP_MOUNT_NUM = new HashMap<>();

    private static byte[] DART, ARROW, EFFECT, IMAGE, PART, SKILL;

    private static final Map<String, byte[]> ICON_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> IMG_BY_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> EFFECT_DATA_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> EFFECT_IMG_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> MOB_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> BG_ITEM_CACHE = new ConcurrentHashMap<>();

    private static final long[] SM_TIEU_CHUAN = {
        1000L, 3000L, 15000L, 40000L, 90000L, 170000L, 340000L, 700000L,
        1500000L, 15000000L, 150000000L, 1500000000L, 5000000000L, 10000000000L, 40000000000L,
        50010000000L, 60010000000L, 70010000000L, 80010000000L, 100010000000L, 1000010000000L, 10000010000000L
    };

    public static void init() {
        try {
            DART = FileIO.readFile("data/update_data/dart");
            ARROW = FileIO.readFile("data/update_data/arrow");
            EFFECT = FileIO.readFile("data/update_data/effect");
            IMAGE = FileIO.readFile("data/update_data/image");
            PART = FileIO.readFile("data/update_data/part");
            SKILL = FileIO.readFile("data/update_data/skill");
            Logger.success("Load DataGame (Cache) thành công!");
        } catch (Exception e) {
            Logger.error("Lỗi load DataGame: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void sendVersionGame(MySession session) {
        Message msg = null;
        try {
            msg = Service.gI().messageNotMap((byte) 4);
            msg.writer().writeByte(vsData);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(vsItem);
            msg.writer().writeByte(0);

            msg.writer().writeByte(SM_TIEU_CHUAN.length);
            for (long sm : SM_TIEU_CHUAN) {
                msg.writer().writeLong(sm);
            }
            session.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendVersionGame(Player pl, MySession session) {
        sendVersionGame(session);
    }

    public static void updateData(MySession session) {
        if (DART == null) {
            Logger.error("CHƯA GỌI DataGame.init() KHI KHỞI ĐỘNG SERVER!");
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-87);
            msg.writer().writeByte(vsData);
            msg.writer().writeInt(DART.length);
            msg.writer().write(DART);
            msg.writer().writeInt(ARROW.length);
            msg.writer().write(ARROW);
            msg.writer().writeInt(EFFECT.length);
            msg.writer().write(EFFECT);
            msg.writer().writeInt(IMAGE.length);
            msg.writer().write(IMAGE);
            msg.writer().writeInt(PART.length);
            msg.writer().write(PART);
            msg.writer().writeInt(SKILL.length);
            msg.writer().write(SKILL);

            session.doSendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // vMap
    public static void updateMap(MySession session) {
        Message msg = null;
        try {
            msg = Service.gI().messageNotMap((byte) 6);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(Manager.MAP_TEMPLATES.length);
            for (MapTemplate temp : Manager.MAP_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
            }
            msg.writer().writeByte(Manager.NPC_TEMPLATES.size());
            for (NpcTemplate temp : Manager.NPC_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
                msg.writer().writeShort(temp.head);
                msg.writer().writeShort(temp.body);
                msg.writer().writeShort(temp.leg);
                msg.writer().writeByte(0);
            }
            msg.writer().writeByte(Manager.MOB_TEMPLATES.size());
            for (MobTemplate temp : Manager.MOB_TEMPLATES) {
                msg.writer().writeByte(temp.type);
                msg.writer().writeUTF(temp.name);
                msg.writer().writeInt(temp.hp);
                msg.writer().writeByte(temp.rangeMove);
                msg.writer().writeByte(temp.speed);
                msg.writer().writeByte(temp.dartType);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // vSkill
    public static void updateSkill(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(7);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(0); // count skill option

            msg.writer().writeByte(Manager.NCLASS.size());
            for (NClass nClass : Manager.NCLASS) {
                msg.writer().writeUTF(nClass.name);

                msg.writer().writeByte(nClass.skillTemplatess.size());
                for (SkillTemplate skillTemp : nClass.skillTemplatess) {
                    msg.writer().writeByte(skillTemp.id);
                    msg.writer().writeUTF(skillTemp.name);
                    msg.writer().writeByte(skillTemp.maxPoint);
                    msg.writer().writeByte(skillTemp.manaUseType);
                    msg.writer().writeByte(skillTemp.type);
                    msg.writer().writeShort(skillTemp.iconId);
                    msg.writer().writeUTF(skillTemp.damInfo);
                    msg.writer().writeUTF("Ngọc Rồng Online");

                    if (skillTemp.id != 0) {
                        msg.writer().writeByte(skillTemp.skillss.size());
                        writeSkills(msg, skillTemp.skillss);
                    } else {
                        // Thêm 2 skill trống 105, 106
                        msg.writer().writeByte(skillTemp.skillss.size() + 2);
                        writeSkills(msg, skillTemp.skillss);

                        // Write 2 skill trống
                        for (int i = 105; i <= 106; i++) {
                            msg.writer().writeShort(i);
                            msg.writer().writeByte(0);
                            msg.writer().writeLong(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeInt(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeByte(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeUTF("");
                        }
                    }
                }
            }
            session.doSendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private static void writeSkills(Message msg, java.util.List<Skill> skills) throws IOException {
        for (Skill skill : skills) {
            msg.writer().writeShort(skill.skillId);
            msg.writer().writeByte(skill.point);
            msg.writer().writeLong(skill.powRequire);
            msg.writer().writeShort(skill.manaUse);
            msg.writer().writeInt(skill.coolDown);
            msg.writer().writeShort(skill.dx);
            msg.writer().writeShort(skill.dy);
            msg.writer().writeByte(skill.maxFight);
            msg.writer().writeShort(skill.damage);
            msg.writer().writeShort(skill.price);
            msg.writer().writeUTF(skill.moreInfo);
        }
    }

    public static void sendDataImageVersion(MySession session) {
        // Hàm này trống trong code gốc
    }

    public static void sendEffectTemplate(MySession session, int id, int... idtemp) {
        int idT = id;
        if (idtemp.length > 0 && idtemp[0] != 0) {
            idT = idtemp[0];
        }
        Message msg = null;
        try {
            // Caching Effect Data
            String dataKey = "eff_data_" + idT;
            byte[] effData = EFFECT_DATA_CACHE.get(dataKey);
            if (effData == null) {
                effData = FileIO.readFile("data/effdata/DataEffect_" + idT);
                if (effData != null) {
                    EFFECT_DATA_CACHE.put(dataKey, effData);
                }
            }

            // Caching Effect Img (phụ thuộc zoomLevel)
            String imgKey = "eff_img_" + idT + "_x" + session.zoomLevel;
            byte[] effImg = EFFECT_IMG_CACHE.get(imgKey);
            if (effImg == null) {
                effImg = FileIO.readFile("data/effect/x" + session.zoomLevel + "/ImgEffect_" + idT + ".png");
                if (effImg != null) {
                    EFFECT_IMG_CACHE.put(imgKey, effImg);
                }
            }

            if (effData == null || effImg == null) {
                return;
            }

            msg = new Message(-66);
            msg.writer().writeShort(id);
            msg.writer().writeInt(effData.length);
            msg.writer().write(effData);
            if (session.version > 216) {
                msg.writer().write(idT == 60 ? 2 : 0);
            }
            msg.writer().writeInt(effImg.length);
            msg.writer().write(effImg);
            session.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendBgItemVersion(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-93);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeByte(bgItem.id);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendItemBGTemplate(MySession session, int id) {
        Message msg = null;
        try {
            String key = "bg_" + id + "_x" + session.zoomLevel;
            byte[] bg_temp = BG_ITEM_CACHE.get(key);
            if (bg_temp == null) {
                bg_temp = FileIO.readFile("data/item_bg_temp/x" + session.zoomLevel + "/" + id + ".png");
                if (bg_temp != null) {
                    BG_ITEM_CACHE.put(key, bg_temp);
                }
            }

            if (bg_temp == null) {
                return;
            }

            msg = new Message(-32);
            msg.writer().writeShort(id);
            msg.writer().writeInt(bg_temp.length);
            msg.writer().write(bg_temp);
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendDataItemBG(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-31);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeShort(bgItem.idImage);
                msg.writer().writeByte(bgItem.layer);
                msg.writer().writeShort(bgItem.dx);
                msg.writer().writeShort(bgItem.dy);
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendIcon(MySession session, int id) {
        Message msg = null;
        try {
            String key = "icon_" + id + "_x" + session.zoomLevel;
            byte[] icon = ICON_CACHE.get(key);
            if (icon == null) {
                icon = FileIO.readFile("data/icon/x" + session.zoomLevel + "/" + id + ".png");
                if (icon != null) {
                    ICON_CACHE.put(key, icon);
                }
            }

            if (icon != null) {
                msg = new Message(-67);
                msg.writer().writeInt(id);
                msg.writer().writeInt(icon.length);
                msg.writer().write(icon);
                session.sendMessage(msg);
            }
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendSmallVersion(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-77);
            msg.writer().writeShort(maxSmallVersion);
            for (int i = 0; i < maxSmallVersion; i++) {
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void requestMobTemplate(MySession session, int id) {
        Message msg = null;
        try {
            String key = "mob_" + id + "_x" + session.zoomLevel;
            byte[] mob = MOB_CACHE.get(key);
            if (mob == null) {
                mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + id);
                if (mob != null) {
                    MOB_CACHE.put(key, mob);
                }
            }

            if (mob != null) {
                msg = new Message(11);
                msg.writer().writeByte(id);
                msg.writer().write(mob);
                session.sendMessage(msg);
            }
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendTileSetInfo(MySession session) {
        Message msg = null;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_set_info"); // File này nhỏ, có thể đọc trực tiếp hoặc cache nếu muốn
            if (data != null) {
                msg = new Message(-82);
                msg.writer().write(data);
                session.sendMessage(msg);
            }
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // data vẽ map
    public static void sendMapTemp(MySession session, int id) {
        Message msg = null;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_map_data/" + id);
            if (data == null) {
                return;
            }
            msg = new Message(-28);
            msg.writer().writeByte(10);
            msg.writer().write(data);
            session.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // head-avatar
    public static void sendHeadAvatar(Message msg) {
        try {
            msg.writer().writeShort(Manager.HEAD_AVATARS.size());
            for (HeadAvatar ha : Manager.HEAD_AVATARS) {
                msg.writer().writeShort(ha.headId);
                msg.writer().writeShort(ha.avatarId);
            }
        } catch (Exception e) {
        }
    }

    public static void sendImageByName(MySession session, String imgName) {
        // BẢO MẬT: Chặn hacker đọc trộm file hệ thống bằng cách gửi tên file chứa ".." hoặc "/"
        if (imgName == null || !imgName.matches("^[a-zA-Z0-9_]+$")) {
            return;
        }

        Message msg = null;
        try {
            String key = imgName + "_x" + session.zoomLevel;
            byte[] data = IMG_BY_NAME_CACHE.get(key);

            if (data == null) {
                data = FileIO.readFile("data/img_by_name/x" + session.zoomLevel + "/" + imgName + ".png");
                if (data != null) {
                    IMG_BY_NAME_CACHE.put(key, data);
                }
            }

            if (data != null) {
                msg = new Message(66);
                msg.writer().writeUTF(imgName);
                msg.writer().writeByte(Manager.getNFrameImageByName(imgName));
                msg.writer().writeInt(data.length);
                msg.writer().write(data);
                session.sendMessage(msg);
            }
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendVersionRes(ISession session) {
        Message msg = null;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(0);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendSizeRes(MySession session) {
        Message msg = null;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(1);
            final File[] files = new File("data/res/x" + session.zoomLevel).listFiles();
            if (files != null) {
                msg.writer().writeShort(files.length);
            } else {
                msg.writer().writeShort(0);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            // Ignore
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void sendRes(MySession session) {
        Message msg = null;
        try {
            File dir = new File("data/res/x" + session.zoomLevel);
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }

            for (final File fileEntry : files) {
                String original = fileEntry.getName();
                byte[] res = FileIO.readFile(fileEntry.getAbsolutePath());
                if (res == null) {
                    continue;
                }

                msg = new Message(-74);
                msg.writer().writeByte(2);
                msg.writer().writeUTF(original);
                msg.writer().writeInt(res.length);
                msg.writer().write(res);
                session.sendMessage(msg);
                msg.cleanup(); // Cleanup sau mỗi lần gửi loop
            }

            msg = new Message(-74);
            msg.writer().writeByte(3);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }
}

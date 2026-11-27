package services;

import services.map.MapService;
import services.map.NpcService;
import services.player.PlayerService;
import utils.Functions;
import database.AlyraManager;
import consts.ConstNpc;
import consts.ConstPlayer;
import utils.FileIO;
import data.DataGame;
import models.boss.BossData;
import models.boss.boss_list.Commeson.NhanBan;
import models.boss.boss_list.Training.TrainingBoss;
import consts.ConstAchievement;
import database.AlyraResultSet;
import database.daos.NDVSqlFetcher;
import interfaces.ISession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.item.Item;
import models.map.ItemMap;
import models.mob.Mob;
import models.player.Pet;
import models.item.Item.ItemOption;
import models.map.Zone;
import models.player.Player;
import network.session.MySession;
import models.skill.Skill;
import network.io.Message;
import server.Client;
import services.map.ChangeMapService;
import utils.Logger;
import utils.TimeUtil;
import utils.Util;
import models.map.MaBuHold;
import models.matches.TOP;
import models.npc.NonInteractiveNPC;
import models.npc.Npc;
import models.player.NPoint;

import interfaces.IGameNotifier;
import models.DomainPorts;

public abstract class ServicePacketBase1 {
    public static final int[] flagTempId = {363, 364, 365, 366, 367, 368, 369, 370, 371, 519, 520, 747};
    public static final int[] flagIconId = {2761, 2330, 2323, 2327, 2326, 2324, 2329, 2328, 2331, 4386, 4385, 2325};

    public void switchToRegisterScr(ISession session) {
        Message msg;
        try {
            msg = new Message(42);
            msg.writer().writeByte(0);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("Error switchToRegisterScr");
        }
    }
    public void regisAccount(ISession session, Message _msg) {
        try {
            String user = _msg.reader().readUTF();
            String pass = _msg.reader().readUTF();

            if (user == null || user.trim().isEmpty()) {
                sendThongBaoOK((MySession) session, "Vui lòng nhập tên tài khoản!");
                return;
            }
            if (pass == null || pass.trim().isEmpty()) {
                sendThongBaoOK((MySession) session, "Vui lòng nhập mật khẩu!");
                return;
            }

            user = user.trim().toLowerCase(java.util.Locale.ROOT);
            pass = pass.trim();

            if (user.length() < 5 || user.length() > 18) {
                sendThongBaoOK((MySession) session, "Tên tài khoản phải từ 5 đến 18 ký tự!");
                return;
            }

            if (!user.matches("^[a-zA-Z0-9]+$")) {
                sendThongBaoOK((MySession) session, "Tài khoản chỉ được gồm chữ cái và số (không dấu, không khoảng trắng)!");
                return;
            }

            if (pass.length() < 5 || pass.length() > 18) {
                sendThongBaoOK((MySession) session, "Mật khẩu phải từ 5 đến 18 ký tự!");
                return;
            }

            if (!pass.matches("^[a-zA-Z0-9]+$")) {
                sendThongBaoOK((MySession) session, "Mật khẩu chỉ được gồm chữ cái và số!");
                return;
            }

            AlyraResultSet rs = AlyraManager.executeQuery("SELECT id FROM account WHERE username = ? LIMIT 1", user);
            if (rs.first()) {
                sendThongBaoOK((MySession) session, "Tên tài khoản này đã được sử dụng. Vui lòng chọn tên khác!");
            } else {
                // Hash password with BCrypt before inserting
                String hashedPassword = utils.PasswordUtils.hashPassword(pass);
                AlyraManager.executeUpdate("INSERT INTO account (username, password, is_admin, sotien, danap) VALUES(?, ?, ?, ?, ?)",
                        user,           // username
                        hashedPassword, // password
                        0,              // is_admin (int)
                        0,              // sotien (int)
                        0               // danap (int)
                );
                sendThongBaoOK((MySession) session, "Đăng ký tài khoản thành công!\nBạn có thể đăng nhập ngay.");
            }

            rs.dispose();
        } catch (Exception e) {
            Logger.error("Error regisAccount: " + e.getMessage(), e);
        }
    }

    public void sendPetFollow(Player player, short smallId) {
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) player.id);
            if (smallId == 0) {
                msg.writer().writeByte(0);
            } else {
                msg.writer().writeByte(1);
                msg.writer().writeShort(smallId);
                msg.writer().writeByte(1);
                int[] fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
                msg.writer().writeByte(fr.length);
                for (int j : fr) {
                    msg.writer().writeByte(j);
                }
                msg.writer().writeShort(smallId == 15067 ? 65 : 75);
                msg.writer().writeShort(smallId == 15067 ? 65 : 75);
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("Error sendPetFollow");
        }
    }

    public void sendChibi(Player player) {
        if (player == null) {
            return;
        }
        Item linhThu = (player.inventory != null && player.inventory.itemsBody != null && player.inventory.itemsBody.size() > 11) ? player.inventory.itemsBody.get(11) : null;
        short smallId = (short) (player.effectSkill.isChibi ? player.typeChibi + 5000 : (linhThu != null && linhThu.isNotNullItem()) ? linhThu.template.iconID - 1 : 0);
        if (!player.effectSkill.isChibi) {
            sendPetFollow(player, smallId);
            return;
        }
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) player.id);
            if (smallId == 0) {
                msg.writer().writeByte(0);
            } else {
                msg.writer().writeByte(1);
                msg.writer().writeShort(smallId);
                msg.writer().writeByte(1);
                int[] fr = new int[]{0, 1, 2};
                msg.writer().writeByte(fr.length);
                for (int j : fr) {
                    msg.writer().writeByte(j);
                }
                msg.writer().writeShort(32);
                msg.writer().writeShort(32);
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendHaveChibiFollowToAllMap(Player pl) {
        if (pl != null && pl.zone != null) {
            for (Player plMap : pl.zone.getPlayers()) {
                if (plMap != null && plMap.isPl()) {
                    sendChibiFollowToMe(plMap, pl);
                }
            }
        }
    }

    public void sendChibiFollowToMe(Player me, Player pl) {
        if (me == null || pl == null) {
            return;
        }
        Item linhThu = (pl.inventory != null && pl.inventory.itemsBody != null && pl.inventory.itemsBody.size() > 11) ? pl.inventory.itemsBody.get(11) : null;
        short smallId = (short) (pl.effectSkill.isChibi ? pl.typeChibi + 5000 : (linhThu != null && linhThu.isNotNullItem()) ? linhThu.template.iconID - 1 : 0);
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) pl.id);
            if (smallId == 0) {
                msg.writer().writeByte(0);
            } else {
                msg.writer().writeByte(1);
                msg.writer().writeShort(smallId);
                msg.writer().writeByte(1);
                int[] fr = (pl.effectSkill.isChibi) ? new int[]{0, 1, 2} : new int[]{0, 1, 2, 3, 4, 5, 6, 7};
                msg.writer().writeByte(fr.length);
                for (int j : fr) {
                    msg.writer().writeByte(j);
                }
                short size = (short) (pl.effectSkill.isChibi ? 32 : (smallId == 15067 ? 65 : 75));
                msg.writer().writeShort(size);
                msg.writer().writeShort(size);
            }
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendMessAllPlayer(Message msg) {
        PlayerService.gI().sendMessageAllPlayer(msg);
    }

    public void sendMessAllPlayerIgnoreMe(Player player, Message msg) {
        PlayerService.gI().sendMessageIgnore(player, msg);
    }

    public void sendMessAllPlayerInMap(Zone zone, Message msg) {
        if (zone == null) {
            msg.dispose();
            return;
        }
        List<Player> players = zone.getPlayers();
        if (players.isEmpty()) {
            msg.dispose();
            return;
        }
        for (int i = players.size() - 1; i >= 0; i--) {
            Player pl = players.get(i);
            if (pl != null) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();
    }

    public void sendMessAllPlayerInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.dispose();
            return;
        }
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            if (player instanceof TrainingBoss || player instanceof NonInteractiveNPC) {
                List<Player> players = player.zone.getPlayers();
                if (players.isEmpty()) {
                    msg.dispose();
                    return;
                }
                for (Player pl : players) {
                    if (pl != null && (player instanceof NonInteractiveNPC || ((TrainingBoss) player).playerAtt.equals(pl))) {
                        pl.sendMessage(msg);
                    }
                }
            } else {
                player.sendMessage(msg);
            }
        } else {
            List<Player> players = player.zone.getPlayers();
            if (players.isEmpty()) {
                msg.dispose();
                return;
            }
            for (Player pl : players) {
                if (pl != null && pl.getSession() != null && pl.isPl()) {
                    pl.sendMessage(msg);
                }
            }
        }
        msg.cleanup();
    }

    public void sendMessAnotherNotMeInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.cleanup();
            return;
        }
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            if (player instanceof TrainingBoss || player instanceof NonInteractiveNPC) {
                List<Player> players = player.zone.getPlayers();
                if (players.isEmpty()) {
                    msg.dispose();
                    return;
                }
                for (Player pl : players) {
                    if (pl != null && !pl.equals(player) && (player instanceof NonInteractiveNPC || ((TrainingBoss) player).playerAtt.equals(pl))) {
                        pl.sendMessage(msg);
                    }
                }
            }
        } else {
            List<Player> players = player.zone.getPlayers();
            if (players.isEmpty()) {
                msg.dispose();
                return;
            }
            for (Player pl : players) {
                if (pl != null && pl.getSession() != null && !pl.equals(player) && pl.isPl()) {
                    pl.sendMessage(msg);
                }
            }
        }
        msg.cleanup();
    }

    public void Send_Info_NV(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);//Cập nhật máu
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hp));
            msg.writer().writeByte(0);//Hiệu ứng Ăn Đậu
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void Send_Info_NV_do_Injure(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);//Cập nhật máu
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hp));
            msg.writer().writeByte(2);
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendInfoPlayerEatPea(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hp));
            msg.writer().writeByte(1);
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void reload_HP_NV(Player pl) {
        Message msg = null;
        try {
            msg = messageSubCommand((byte) 9);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hp));
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void resetPoint(Player player, int x, int y) {
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            msg = new Message(46);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            player.sendMessage(msg);
            msg.cleanup();

        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void clearMap(Player player) {
        Message msg;
        try {
            msg = new Message(-22);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void chat(Player player, String text) {
        if (player != null && player.isMuted()) {
            long remainingMin = player.muteUntil > 0 ? Math.max(1, (player.muteUntil - System.currentTimeMillis()) / 60000) : 0;
            sendThongBao(player, "Ban dang bi cam chat! " + (remainingMin > 0 ? "Con lai: " + remainingMin + " phut." : ""));
            return;
        }
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeUTF(text);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void chatJustForMe(Player me, Player plChat, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeUTF(text);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public Npc getNpc(Player player) {
        Npc closestNpc = null;
        double closestDistance = Double.MAX_VALUE;
        for (Npc npc : player.zone.map.npcs) {
            double distance = Util.getDistance(player, npc);
            if (distance <= 150 && distance < closestDistance) {
                closestDistance = distance;
                closestNpc = npc;
            }
        }
        return closestNpc;
    }

    public void Transport(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(pl.type);
            pl.sendMessage(msg);
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void Transport(Player pl, int type) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(type);
            pl.sendMessage(msg);
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void point(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        player.nPoint.calPoint();
        Send_Info_NV(player);
        if (!player.isPet && !player.isBoss && !player.isNewPet) {
            Message msg;
            try {
                msg = new Message(-42);
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.hpg));
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.mpg));
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.dameg));
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.hpMax));
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.mpMax));
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.hp));
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.mp));
                msg.writer().writeByte(player.nPoint.speed);
                msg.writer().writeByte(20);
                msg.writer().writeByte(20);
                msg.writer().writeByte(1);
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.dame));
                msg.writer().writeInt(NPoint.safeInt(player.nPoint.def));
                msg.writer().writeByte((byte) Math.min(100, Math.max(0, player.nPoint.crit)));
                msg.writer().writeLong(player.nPoint.tiemNang);
                msg.writer().writeShort(100);
                msg.writer().writeShort(player.nPoint.defg);
                msg.writer().writeByte(player.nPoint.critg);
                msg.writer().writeLong(player.nPoint.power);
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    public String name(Player player) {
        if (player.isPl() && player.clan != null) {
            try {
                if (!player.clan.name2.isEmpty()) {
                    return "[" + player.clan.name2 + "] " + player.name;
                } else if (player.clan.name.length() > 3) {
                    return "[" + player.clan.name.substring(0, 3) + "] " + player.name;
                } else {
                    return "[" + player.clan.name + "] " + player.name;
                }
            } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
        } else if (player.name == null) {
            return "";
        }
        return player.name;
    }

    public void player(Player pl) {
        if (pl == null) {
            return;
        }
        Message msg;
        try {
            msg = messageSubCommand((byte) 0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.playerTask.taskMain.id);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(pl.head);
            msg.writer().writeUTF(pl.name);
            msg.writer().writeByte(0); //cPK
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeLong(pl.nPoint.power);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(pl.gender);
            ArrayList<Skill> skills = (ArrayList<Skill>) pl.playerSkill.skills;
            msg.writer().writeByte(pl.playerSkill.getSizeSkill());
            for (Skill skill : skills) {
                if (skill.skillId != -1) {
                    msg.writer().writeShort(skill.skillId);
                }
            }
            //---vang---luong--luongKhoa
            long gold = pl.inventory.getGoldDisplay();
            if (pl.isVersionAbove(214)) {
                msg.writer().writeLong(gold);
            } else {
                msg.writer().writeInt((int) gold);
            }
            msg.writer().writeInt(pl.inventory.ruby);
            msg.writer().writeInt(pl.inventory.gem);
            ArrayList<Item> itemsBody = (ArrayList<Item>) pl.inventory.itemsBody;
            msg.writer().writeByte(itemsBody.size());
            for (Item item : itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }

            }
            ArrayList<Item> itemsBag = (ArrayList<Item>) pl.inventory.itemsBag;
            msg.writer().writeByte(itemsBag.size());
            for (Item item : itemsBag) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }

            }
            ArrayList<Item> itemsBox = (ArrayList<Item>) pl.inventory.itemsBox;
            msg.writer().writeByte(itemsBox.size());
            for (Item item : itemsBox) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }
            }
            DataGame.sendHeadAvatar(msg);
            msg.writer().writeShort(514);
            msg.writer().writeShort(515);
            msg.writer().writeShort(1139);
            msg.writer().writeByte(pl.fusion.typeFusion != ConstPlayer.NON_FUSION ? 1 : 0);
            msg.writer().writeInt(pl.deltaTime);
            msg.writer().writeByte(pl.isNewMember ? 1 : 0);
            msg.writer().writeShort(pl.getAura());
            msg.writer().writeByte(pl.getEffFront());
            msg.writer().writeShort(pl.getHat());
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public Message messageNotLogin(byte command) throws IOException {
        Message ms = new Message(-29);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageNotMap(byte command) throws IOException {
        Message ms = new Message(-28);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageSubCommand(byte command) throws IOException {
        Message ms = new Message(-30);
        ms.writer().writeByte(command);
        return ms;
    }

    public void addSMTN(Player player, byte type, long param, boolean isOri) {
        if (player.isPet && player.nPoint != null) {
            player.nPoint.powerUp(param);
            player.nPoint.tiemNangUp(param);
            Player master = ((Pet) player).master;
            if (master != null && master.nPoint != null) {
                param = master.nPoint.calSubTNSM(param);
                addSMTN(master, type, param, true);
            }
        } else if (player.isBot) {
            assert player.nPoint != null;
            player.nPoint.power += param;
            player.nPoint.tiemNang += param;
        } else {
            if (player.nPoint == null || player.nPoint.power > player.nPoint.getPowerLimit()) {
                return;
            }
            switch (type) {
                case 1:
                    player.nPoint.tiemNangUp(param);
                    break;
                case 2:
                    player.nPoint.powerUp(param);
                    player.nPoint.tiemNangUp(param);
                    break;
                default:
                    player.nPoint.powerUp(param);
                    break;
            }
            PlayerService.gI().sendTNSM(player, type, param);
            if (isOri) {
                if (player.clan != null) {
                    player.clan.addSMTNClan(player, param);
                }
            }
        }
    }

    public String get_HanhTinh(int hanhtinh) {
        return switch (hanhtinh) {
            case 0 -> "Trái Đất";
            case 1 -> "Namếc";
            case 2 -> "Xayda";
            default -> "";
        };
    }

    public List<String> ListCaption(int gender) {
        List<String> Captions = new ArrayList<>();
        Captions.add("Tân thủ");
        Captions.add("Tập sự sơ cấp");
        Captions.add("Tập sự trung cấp");
        Captions.add("Tập sự cao cấp");
        Captions.add("Tân binh");
        Captions.add("Chiến binh");
        Captions.add("Chiến binh cao cấp");
        Captions.add("Vệ binh");
        Captions.add("Vệ binh hoàng gia");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 1");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 2");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 3");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 4");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 1");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 2");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 3");
        Captions.add("Giới Vương Thần cấp 1");
        Captions.add("Giới Vương Thần cấp 2");
        Captions.add("Giới Vương Thần cấp 3");
        Captions.add("Thần hủy diệt cấp 1");
        Captions.add("Thần hủy diệt cấp 2");
        Captions.add("Ngọc Rồng Online Siêu Cấp");
        return Captions;
    }

    public String getCurrStrLevel(Player pl) {
        return ListCaption(pl.gender).get(getCurrLevel(pl));
    }

    public int getCurrLevel(Player pl) {
        if (pl.nPoint == null) {
            return 0;
        }
        long sucmanh = pl.nPoint.power;
        if (sucmanh < 3000) {
            return 0;
        } else if (sucmanh < 15000) {
            return 1;
        } else if (sucmanh < 40000) {
            return 2;
        } else if (sucmanh < 90000) {
            return 3;
        } else if (sucmanh < 170000) {
            return 4;
        } else if (sucmanh < 340000) {
            return 5;
        } else if (sucmanh < 700000) {
            return 6;
        } else if (sucmanh < 1500000) {
            return 7;
        } else if (sucmanh < 15000000) {
            return 8;
        } else if (sucmanh < 150000000) {
            return 9;
        } else if (sucmanh < 1500000000) {
            return 10;
        } else if (sucmanh < 5000000000L) {
            return 11;
        } else if (sucmanh < 10000000000L) {
            return 12;
        } else if (sucmanh < 40000000000L) {
            return 13;
        } else if (sucmanh < 50010000000L) {
            return 14;
        } else if (sucmanh < 60010000000L) {
            return 15;
        } else if (sucmanh < 70010000000L) {
            return 16;
        } else if (sucmanh < 80010000000L) {
            return 17;
        } else if (sucmanh < 100010000000L) {
            return 18;
        } else if (sucmanh < 11100010000000L) {
            return 19;
        }
        return 20;
    }

    public void hsChar(Player pl, long hp, long mp) {
        Message msg;
        try {
            if (pl.isPl() && pl.effectSkill != null && pl.effectSkill.isBodyChangeTechnique) {
                PlayerService.gI().changeAndSendTypePK(pl, 5);
            }
            pl.setJustRevivaled();
            pl.nPoint.setHp(hp);
            pl.nPoint.setMp(mp);
            if (pl.isPl()) {
                msg = new Message(-16);
                pl.sendMessage(msg);
                msg.cleanup();
                PlayerService.gI().sendInfoHpMpMoney(pl);
                Service.gI().point(pl);
            }

            msg = messageSubCommand((byte) 15);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(NPoint.safeInt(hp));
            msg.writer().writeInt(NPoint.safeInt(mp));
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            Send_Info_NV(pl);
            PlayerService.gI().sendInfoHpMp(pl);
            AchievementService.gI().checkDoneTask(pl, ConstAchievement.THANH_HOI_SINH);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void charDie(Player pl) {
        if (pl == null || pl.location == null) {
            return;
        }
        Message msg;
        try {
            if (!pl.isPet && !pl.isBot && !pl.isNewPet && pl.isPl()) {
                msg = new Message(-17);
                msg.writer().writeByte((int) pl.id);
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                pl.sendMessage(msg);
                msg.cleanup();
            } else if (pl.isPet) {
                ((Pet) pl).lastTimeDie = System.currentTimeMillis();
            }
            msg = new Message(-8);
            msg.writer().writeInt((int) pl.id);
            int cPk = 0;
            msg.writer().writeByte(cPk); //cpk
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();

            Send_Info_NV(pl);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void attackMob(Player pl, int mobId, boolean isMobMe, int masterId) {
        if (pl != null && pl.zone != null) {
            if (pl.playerSkill != null && pl.playerSkill.skillSelect == null) {
                pl.playerSkill.skillSelect = pl.playerSkill.getSkillbyId(pl.gender == consts.ConstPlayer.TRAI_DAT
                        ? Skill.DRAGON : (pl.gender == consts.ConstPlayer.NAMEC ? Skill.DEMON : Skill.GALICK));
            }
            if (!isMobMe) {
                for (Mob mob : pl.zone.mobs) {
                    if (mob.id == mobId) {
                        SkillService.gI().useSkill(pl, null, mob, -1, null);
                        break;
                    }
                }
            } else {
                Player plAtt = pl.zone.getPlayerInMap(masterId);
                if (plAtt != null && SkillService.gI().canAttackPlayer(pl, plAtt)) {
                    Mob mob = plAtt.mobMe;
                    if (mob != null) {
                        mob.injured(pl, pl.nPoint.getDameAttack(false), true);
                    }
                }
            }
        }
    }

    public void Send_Caitrang(Player player) {
        if (player != null) {
            Message msg;
            try {
                msg = new Message(-90);
                msg.writer().writeByte(1);// check type
                msg.writer().writeInt((int) player.id); //id player
                short head = player.getHead();
                short body = player.getBody();
                short leg = player.getLeg();

                msg.writer().writeShort(head);//set head
                msg.writer().writeShort(body);//setbody
                msg.writer().writeShort(leg);//set leg
                msg.writer().writeByte(player.effectSkill.isMonkey ? 1 : 0);//set khỉ
                sendMessAllPlayerInMap(player, msg);
                msg.cleanup();
            } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
        }
    }

    public void setNotMonkey(Player player) {
        Message msg;
        try {
            msg = new Message(-90);
            msg.writer().writeByte(-1);
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFlagBag(Player pl) {
        Message msg;
        try {
            msg = new Message(-64);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.getFlagBag());
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("Error sendFlagBag");
        }
    }

    public void sendThongBaoOK(Player pl, String text) {
        if (pl.isPet || pl.isNewPet) {
            return;
        }
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoOK(MySession session, String text) {
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendThongBaoAllPlayer(String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            this.sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendBigMessage(Player player, int iconId, String text) {
        try {
            Message msg;
            msg = new Message(-70);
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException ignored) {

        }
    }

    public void sendThongBaoFromAdmin(Player player, String text) {
        sendBigMessage(player, 15271, text);
    }

    public void sendThongBao(Player pl, String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            pl.sendMessage(msg);
            msg.cleanup();

        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendThongBao(List<Player> pl, String thongBao) {
        for (Player ply : pl) {
            if (ply != null) {
                this.sendThongBao(ply, thongBao);
            }
        }
    }

    public void sendThongBaoToAnotherNotMe(Player me, String text) {
        for (int i = 0; i < Client.gI().getPlayers().size(); i++) {
            Player pl = Client.gI().getPlayers().get(i);
            if (pl != null && !pl.equals(me)) {
                this.sendThongBao(pl, text);
            }
        }
    }

    public void sendMoney(Player pl) {
        Message msg;
        try {
            msg = new Message(6);
            long gold = pl.inventory.getGoldDisplay();
            if (pl.isVersionAbove(214)) {
                msg.writer().writeLong(gold);
            } else {
                msg.writer().writeInt((int) gold);
            }
            msg.writer().writeInt(pl.inventory.gem);
            msg.writer().writeInt(pl.inventory.ruby);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendToAntherMePickItem(Player player, int itemMapId) {
        Message msg;
        try {
            msg = new Message(-19);
            msg.writer().writeShort(itemMapId);
            msg.writer().writeInt((int) player.id);
            sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void openFlagUI(Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(0);
            msg.writer().writeByte(flagTempId.length);
            for (int j : flagTempId) {
                msg.writer().writeShort(j);
                msg.writer().writeByte(1);
                switch (j) {
                    case 363:
                        msg.writer().writeByte(73);
                        msg.writer().writeShort(0);
                        break;
                    case 371:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(10);
                        break;
                    default:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(5);
                        break;
                }
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void changeFlag(Player pl, int index) {
        Message msg;
        try {
            pl.cFlag = (byte) index;
            msg = new Message(-103);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(index);
            Service.gI().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(index);
            msg.writer().writeShort(flagIconId[index]);
            Service.gI().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            if (pl.pet != null) {
                pl.pet.cFlag = (byte) index;
                msg = new Message(-103);
                msg.writer().writeByte(1);
                msg.writer().writeInt((int) pl.pet.id);
                msg.writer().writeByte(index);
                Service.gI().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();

                msg = new Message(-103);
                msg.writer().writeByte(2);
                msg.writer().writeByte(index);
                msg.writer().writeShort(flagIconId[index]);
                Service.gI().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();
            }
            pl.idMark.setLastTimeChangeFlag(System.currentTimeMillis());
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFlagPlayerToMe(Player me, Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(pl.cFlag);
            msg.writer().writeShort(flagIconId[pl.cFlag]);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void chooseFlag(Player pl, int index) {
        if (pl == null || index < 0 || index >= flagIconId.length) {
            return;
        }
        if (pl.zone == null || pl.zone.map == null) {
            return;
        }
        if (MapService.gI().isMapBlackBallWar(pl.zone.map.mapId) || MapService.gI().isMapMaBu(pl.zone.map.mapId)) {
            sendThongBao(pl, "Không được đổi cờ lúc này");
            return;
        }
        if (Util.canDoWithTime(pl.idMark.getLastTimeChangeFlag(), 60000)) {
            changeFlag(pl, index);
        } else {
            sendThongBao(pl, "Chỉ được đổi cờ sau " + TimeUtil.getTimeLeft(pl.idMark.getLastTimeChangeFlag(), 60) + " nữa");
        }
    }

    public void attackPlayer(Player pl, int idPlAnPem) {
        if (pl == null || pl.zone == null) {
        //    System.out.println("Người chơi hoặc khu vực không hợp lệ.");
            return;
        }
        Player player;
        if (MapService.gI().isMapOffline(pl.zone.map.mapId)) {
            player = pl.zone.getPlayerInMapOffline(pl, idPlAnPem);
        } else {
            player = pl.zone.getPlayerInMap(idPlAnPem);
        }
        if (player == null) {
       //     System.out.println("Không tìm thấy người chơi với ID: " + idPlAnPem);
            return;
        }
        SkillService.gI().useSkill(pl, player, null, -1, null);
    }

    public void releaseCooldownSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                skill.lastTimeUseThisSkill = System.currentTimeMillis() - skill.coolDown;
                int leftTime = 0;
                msg.writer().writeInt(leftTime);
            }
            pl.sendMessage(msg);
            pl.nPoint.setMp(pl.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();

        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendTimeSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                msg.writer().writeInt(timeLeft);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("Error sendTimeSkill");
        }
    }

//    public void sendTimeSkill(Player pl, Skill skill) {
//        Message msg;
//        try {
//            msg = new Message(-94);
//            msg.writer().writeShort(skill.skillId);
//            int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
//            if (timeLeft < 0) {
//                timeLeft = 0;
//            }
//            msg.writer().writeInt(timeLeft);
//            pl.sendMessage(msg);
//            msg.cleanup();
//        } catch (Exception e) {
//        }
//    }

    public void releaseCooldownSkill(Player pl, Skill skill) {
        Message msg;
        try {
            msg = new Message(-94);
            msg.writer().writeShort(skill.skillId);
            skill.lastTimeUseThisSkill = System.currentTimeMillis() - skill.coolDown;
            int leftTime = 0;
            msg.writer().writeInt(leftTime);
            pl.sendMessage(msg);
            pl.nPoint.setMp(pl.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void dropItemMap(Zone zone, ItemMap item) {
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt(3);//
            sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void sendChiSoPetGoc(Player pl) {
        if (pl == null || pl.pet == null) {
            return;
        }
        try {
            Message msg = new Message(-109);
            msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.hpg));
            msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.mpg));
            msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.dameg));
            msg.writer().writeByte(pl.pet.nPoint.defg);
            msg.writer().writeShort(pl.pet.nPoint.critg);

            pl.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e, "sendChiSoPetGoc");
        }
    }

    public void dropItemMapForMe(Player player, ItemMap item) {
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt(3);//
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }
}

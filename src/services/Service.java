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

public class Service extends ServicePacketBase1 implements IGameNotifier {

    protected static volatile Service instance;

    public static Service getInstance() {
        return gI();
    }

    public static Service gI() {
    if (instance == null) {
        synchronized (Service.class) {
            if (instance == null) {
                instance = new Service();
                DomainPorts.registerNotifier(instance);
            }
        }
    }
    return instance;
}

    public void showInfoPet(Player pl) {
        if (pl != null && pl.pet != null) {
            Message msg;
            try {
                msg = new Message(-107);
                msg.writer().writeByte(2);
                msg.writer().writeShort(pl.pet.getAvatar());
                msg.writer().writeByte(pl.pet.inventory.itemsBody.size());

                for (Item item : pl.pet.inventory.itemsBody) {
                    if (!item.isNotNullItem()) {
                        msg.writer().writeShort(-1);
                    } else {
                        msg.writer().writeShort(item.template.id);
                        msg.writer().writeInt(item.quantity);
                        msg.writer().writeUTF(item.getInfo());
                        msg.writer().writeUTF(item.getContent());

                        int countOption = item.itemOptions.size();
                        msg.writer().writeByte(countOption);
                        for (ItemOption iop : item.itemOptions) {
                            msg.writer().writeByte(iop.optionTemplate.id);
                            msg.writer().writeShort(iop.param);
                        }
                    }
                }

                msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.hp)); //hp
                msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.hpMax)); //hpfull
                msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.mp)); //mp
                msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.mpMax)); //mpfull
                msg.writer().writeInt(NPoint.safeInt(pl.pet.nPoint.dame)); //damefull
                msg.writer().writeUTF(pl.pet.name); //name
                msg.writer().writeUTF(getCurrStrLevel(pl.pet)); //curr level
                msg.writer().writeLong(pl.pet.nPoint.power); //power
                msg.writer().writeLong(pl.pet.nPoint.tiemNang); //tiềm năng
                msg.writer().writeByte(pl.pet.getStatus()); //status
                msg.writer().writeShort(pl.pet.nPoint.stamina); //stamina
                msg.writer().writeShort(pl.pet.nPoint.maxStamina); //stamina full
                msg.writer().writeByte(pl.pet.nPoint.crit); //crit
                msg.writer().writeShort(pl.pet.nPoint.def); //def
                msg.writer().writeByte(4); //counnt pet skill
                for (int i = 0; i < pl.pet.playerSkill.skills.size(); i++) {
                    if (pl.pet.playerSkill.skills.get(i).skillId != -1) {
                        msg.writer().writeShort(pl.pet.playerSkill.skills.get(i).skillId);
                    } else {
                        switch (i) {
                            case 1 -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 150tr để mở");
                            }
                            case 2 -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 1tỷ5 để mở");
                            }
                            default -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 20tỷ để mở");
                            }
                        }
                    }
                }

                pl.sendMessage(msg);
                msg.cleanup();

            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    public void sendSpeedPlayer(Player pl, int speed) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 8);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(speed != -1 ? speed : pl.nPoint.speed);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setPos(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void setPos2(Player player, int x, int y) {
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
    }

    public void setPos0(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(0);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void getPlayerMenu(Player player, int playerId) {

        Message msg;
        try {
            msg = new Message(-79);
            Player pl = player.zone.getPlayerInMap(playerId);
            if (pl != null && (pl.nPoint != null)) {
                msg.writer().writeInt(playerId);
                msg.writer().writeLong(pl.nPoint.power);
                msg.writer().writeUTF(Service.gI().getCurrStrLevel(pl));
                player.sendMessage(msg);
            }
            msg.cleanup();
            if (player.idMark.isAcpTrade()) {
                player.idMark.setAcpTrade(false);
                return;
            }
            SubMenuService.gI().showMenu(player);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void hideWaitDialog(Player pl) {
        Message msg;
        try {
            msg = new Message(-99);
            msg.writer().writeByte(-1);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendThongBaoResetWait(Player pl, String text) {
        if (pl == null) {
            return;
        }
        hideWaitDialog(pl);
        sendThongBao(pl, text);
    }

    public void chatPrivate(Player plChat, Player plReceive, String text) {
        if (Functions.isSpam(plChat, text)) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(92);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            if (plChat.getSession().version > 214) {
                msg.writer().writeShort(-1);
            }
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag());
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plChat.sendMessage(msg);
            // Receive
            msg = new Message(92);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            if (plReceive.getSession().version > 214) {
                msg.writer().writeShort(-1);
            }
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag());
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plReceive.sendMessage(msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void changePassword(Player player, String oldPass, String newPass, String rePass) {
        if (player.getSession().pp.equals(oldPass)) {
            if (newPass.length() >= 6) {
                if (newPass.equals(rePass)) {
                    player.getSession().pp = newPass;
                    try {
                        String hashedPassword = utils.PasswordUtils.hashPassword(rePass);
                        AlyraManager.executeUpdate("update account set password = ? where id = ? and username = ?",
                                hashedPassword, player.getSession().userId, player.getSession().uu);
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thành công!");
                    } catch (Exception ex) {
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thất bại!");
                        Logger.logException(Service.class, ex);
                    }
                } else {
                    Service.gI().sendThongBao(player, "Mật khẩu nhập lại không đúng!");
                }
            } else {
                Service.gI().sendThongBao(player, "Mật khẩu ít nhất 6 ký tự!");
            }
        } else {
            Service.gI().sendThongBao(player, "Mật khẩu cũ không đúng!");
        }
    }

    public void switchToCreateChar(MySession session) {
        Message msg;
        try {
            msg = new Message(2);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendCaption(MySession session, byte gender) {
        Message msg;
        try {
            msg = new Message(-41);
            List<String> captions = ListCaption(gender);
            msg.writer().writeByte(captions.size());
            for (String caption : captions) {
                msg.writer().writeUTF(caption);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendHavePet(Player player) {
        Message msg;
        try {
            msg = new Message(-107);
            msg.writer().writeByte(player.pet == null ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendWaitToLogin(MySession session, int secondsWait) {
        Message msg;
        try {
            msg = new Message(122);
            msg.writer().writeShort(secondsWait);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMessage(MySession session, int cmd, String path) {
        Message msg;
        try {
            msg = new Message(cmd);
            msg.writer().write(FileIO.readFile(path));
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendNangDong(Player player) {
        Message msg;
        try {
            msg = new Message(-97);
            msg.writer().writeInt(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void setClientType(MySession session, Message msg) {
        try {
            session.typeClient = (msg.reader().readByte());//client_type
            session.zoomLevel = msg.reader().readByte();//zoom_level
            msg.reader().readBoolean();//is_gprs
            msg.reader().readInt();//width
            msg.reader().readInt();//height
            msg.reader().readBoolean();//is_qwerty
            msg.reader().readBoolean();//is_touch
            String platform = msg.reader().readUTF();
            String[] arrPlatform = platform.split("\\|");
            session.version = Integer.parseInt(arrPlatform[1].replaceAll("\\.", ""));
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            msg.cleanup();
        }
        DataGame.sendLinkIP(session);
    }

    public void mabaove(Player player, int mbv) {
        if (Integer.toString(mbv).length() != 6) {
            Service.gI().sendThongBaoOK(player, "Mã bảo vệ phải có độ dài là 6 số.");
        } else if (player.mbv == 0) {
            player.idMark.setMbv(mbv);
            NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1, "Bạn chưa từng kích hoạt chức năng mã bảo vệ để kích hoạt bạn cần có 30K vàng, mật khẩu của bạn là: " + mbv, "Đồng ý", "Từ chối");
        } else if (player.mbv != mbv) {
            Service.gI().sendThongBao(player, "Mật khẩu không đúng. Vui lòng kiểm tra lại");
        } else {
            if (player.baovetaikhoan) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1, "Tài khoản đang được bảo vệ\nBạn có muốn tắt bảo vệ không?", "Đồng ý", "Từ chối");
            } else {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1, "Tài khoản không được bảo vệ\nBạn muốn bật chứ năng bảo vệ tài khoản?", "Đồng ý", "Từ chối");
            }
        }
    }

    public void sendEffPlayer(Player pl, Player plReceive, int idEff, int layer, int loop, int loopCount) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopCount);
            msg.writer().writeByte(0);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("sendItemToPlayer error", e);
        }
    }

    public void sendEffAllPlayer(Player pl, int idEff, int layer, int loop, int loopCount) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopCount);
            msg.writer().writeByte(0);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("sendEffAllPlayer (layer, loop) error", e);
        }
    }

    public void sendEffAllPlayer(Player pl, int idEff) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            msg.writer().writeByte(0);
            msg.writer().writeByte(50);
            msg.writer().writeShort(1);
            msg.writer().writeByte(-1);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("sendEffAllPlayer error", e);
        }
    }

//    public void removeEffAllPlayer(Player pl) {
//        Message msg = null;
//        try {
//            msg = new Message(-128);
//            msg.writer().writeByte(2);
//            msg.writer().writeInt((int) pl.id);
//            sendMessAllPlayerInMap(pl.zone, msg);
//            msg.cleanup();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void removeEffPlayer(Player pl, int idEff) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("removeEffPlayer error", e);
        }
    }

    public void sendEffPlayer(Player pl) {
        if (pl != null && pl.isPl() && pl.inventory != null && pl.inventory.itemsBody != null && pl.inventory.itemsBody.size() > 10) {
            Item danhhieu = pl.inventory.itemsBody.get(10);
            if (danhhieu != null && danhhieu.isNotNullItem() && danhhieu.template != null) {
                Service.gI().sendEffAllPlayer(pl, danhhieu.template.part, 1, -1, 1);
            }
        }
    }
    public void addEffectChar(Player pl, int id, int layer, int loop, int loopcount, int stand) {
        if (!pl.idEffChar.contains(id)) {
            pl.idEffChar.add(id);
        }
        try {
            Message msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(id);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopcount);
            msg.writer().writeByte(stand);
            sendMessAllPlayerInMap(pl.zone, msg);
        } catch (IOException e) {
            Logger.error("addEffectChar error", e);
        }
    }

    public void sendEffAllPlayerMapToMe(Player pl) {
        if (pl == null || pl.zone == null) {
            return;
        }
        try {
            for (Player plM : pl.zone.getPlayers()) {
                if (plM != null && plM.isPl() && plM.inventory != null && plM.inventory.itemsBody != null && plM.inventory.itemsBody.size() > 10) {
                    Item danhhieu = plM.inventory.itemsBody.get(10);
                    if (danhhieu != null && danhhieu.isNotNullItem() && danhhieu.template != null) {
                        Service.gI().sendEffPlayer(plM, pl, danhhieu.template.part, 1, -1, 1);
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e, "Lỗi sendEffAllPlayerMapToMe");
        }
    }

    public void Send_Body_Mob(Mob mob, int type, int idIcon) {
        Message msg = null;
        try {
            msg = new Message(-112);
            msg.writer().writeByte(type);
            msg.writer().writeByte(mob.id);
            if (type == 1) {
                msg.writer().writeShort(idIcon);//set body
            }
            sendMessAllPlayerInMap(mob.zone, msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendPlayerVS(Player pVS1, Player pVS2, byte type) {
        Message msg = null;
        try {
            pVS1.typePk = type;
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS1.id); //ID PLAYER
            msg.writer().writeByte(type); //TYPE PK
            pVS1.sendMessage(msg);
            if (pVS2 != null && pVS2.isPl()) {
                pVS2.sendMessage(msg);
            }
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendPVB(Player pVS1, Player pVS2, byte type) {
        Message msg = null;
        try {
            pVS1.typePk = type;
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS1.id); //ID PLAYER
            msg.writer().writeByte(type); //TYPE PK
            pVS1.sendMessage(msg);
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS2.id); //ID PLAYER
            msg.writer().writeByte(type); //TYPE PK
            pVS1.sendMessage(msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendPVP(Player p1, Player p2) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) p2.id);
            msg.writer().writeByte(3);
            p1.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void exitMap(Player player, long playerExitMapId) {
        Message msg;
        try {
            msg = new Message(-6);
            msg.writer().writeInt((int) playerExitMapId);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void SendPowerInfo(Player player) {
        Message msg = null;
        try {
            msg = new Message(-115);
            msg.writer().writeUTF("TL");
            msg.writer().writeShort(player.fightMabu.pointMabu);
            msg.writer().writeShort(player.fightMabu.POINT_MAX);
            msg.writer().writeShort(3);
            player.sendMessage(msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void SendPercentPowerInfo(Player player) {
        Message msg = null;
        try {
            msg = new Message(-115);
            msg.writer().writeUTF("%");
            msg.writer().writeShort(player.fightMabu.pointPercent);
            msg.writer().writeShort(player.fightMabu.POINT_MAX * 2);
            msg.writer().writeShort(3);
            player.sendMessage(msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void SendMabu(Zone zone, int percent) {
        Message msg = null;
        try {
            msg = new Message(-117);
            msg.writer().writeByte(percent);
            sendMessAllPlayerInMap(zone, msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void callNhanBan(Player player) {
        List<Skill> skillList = new ArrayList<>();
        for (byte i = 0; i < player.playerSkill.skills.size(); i++) {
            Skill skill = player.playerSkill.skills.get(i);
            if (skill.point > 0 && skill.template.id != Skill.TU_SAT && skill.template.id != Skill.TROI) {
                skillList.add(skill);
            }
        }
        int[][] skillTemp = new int[skillList.size()][3];
        for (byte i = 0; i < skillList.size(); i++) {
            Skill skill = skillList.get(i);
            if (skill.point > 0 && skill.template.id != Skill.TU_SAT && skill.template.id != Skill.TROI) {
                skillTemp[i][0] = skill.template.id;
                skillTemp[i][1] = skill.point;
                skillTemp[i][2] = skill.coolDown;
            }
        }
        BossData bossDataClone = new BossData(
                player.name,
                player.gender,
                new short[]{player.getHead(), player.getBody(), player.getLeg(), player.getFlagBag(), player.getAura(), player.getEffFront()},
                Functions.maxint(player.nPoint.dame * 10L),
                new int[]{Functions.maxint(player.nPoint.hpMax * 10L)},
                new int[]{140},
                skillTemp,
                new String[]{"|-2|Boss nhân bản đã xuất hiện rồi"}, //text chat 1
                new String[]{"|-1|Ta sẽ thay thế ngươi, haha"}, //text chat 2
                new String[]{"|-1|Lần khác ta sẽ xử đẹp ngươi"}, //text chat 3
                60
        );

        try {
            new NhanBan(player, bossDataClone);
            EffectSkillService.gI().setPKCommeson(player, 300000);
            player.lastPkCommesonTime = System.currentTimeMillis();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendBigBoss(Zone zone, int action, int size, int id, int dame) {
        Message msg = null;
        try {
            msg = new Message(102);
            msg.writer().writeByte(action);
            if (action != 6 && action != 7) {
                msg.writer().writeByte(size); // SIZE PLAYER ATTACK
                msg.writer().writeInt(id); // PLAYER ID
                msg.writer().writeInt(dame); // DAME
            }
            sendMessAllPlayerInMap(zone, msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendBigBoss2(Zone zone, int action, Mob bigboss) {
        Message msg = null;
        try {
            msg = new Message(101);
            msg.writer().writeByte(action);
            msg.writer().writeShort(bigboss.location.x);
            msg.writer().writeShort(bigboss.location.y);
            sendMessAllPlayerInMap(zone, msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendBigBoss2(Player player, int action, Mob bigboss) {
        Message msg = null;
        try {
            msg = new Message(101);
            msg.writer().writeByte(action);
            msg.writer().writeShort(bigboss.location.x);
            msg.writer().writeShort(bigboss.location.y);
            player.sendMessage(msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendMabuHold(Player player, int action, short x, short y) {
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            if (action == 0) {
                setPos(player, x, y);
            }
            msg = new Message(52);
            msg.writer().writeByte(action); // 0 false, 1 true
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendMabuHoldToMe(Player player, Player plReceive, int action, short x, short y) {
        Message msg;
        try {
            msg = new Message(52);
            msg.writer().writeByte(action); // 0 false, 1 true
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendEffMabuHoldAllPlayerMapToMe(Player pl) {
        if (pl.zone == null) {
            // Log hoặc xử lý khi zone của player là null
            //      System.out.println("Player's zone is null");
            return;
        }

        // Lặp qua tất cả các player trong zone
        for (Player plM : pl.zone.getPlayers()) {
            if (plM.isPl()) {
                // Kiểm tra xem maBuHold có khác null không trước khi sử dụng
                if (plM.maBuHold != null) {
                    // Gửi hiệu ứng nếu maBuHold khác null
                    sendMabuHoldToMe(plM, pl, 1, (short) plM.maBuHold.x, (short) plM.maBuHold.y);
                }
            }
        }
    }

    public void sendEffMabuEat(Player player, Player plTarget) {
        Message msg;
        try {
            msg = new Message(52);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt((int) plTarget.id);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendMabuEat(Player player, Player plTarget) {
        if (plTarget.isPl() && plTarget.maBuHold == null) {
            MaBuHold mabuHold = player.zone.getMaBuHold();
            if (mabuHold != null) {
                int zoneId = player.zone.zoneId;
                player.zone.setMaBuHold(mabuHold.slot, zoneId, plTarget);
                sendEffMabuEat(player, plTarget);

                server.GameLoopManager.gI().schedule(() -> {
                    try {
                        if (player == null || player.zone == null || player.zone.map.mapId != 127) {
                            return;
                        }
                        if (plTarget == null || plTarget.isOffline || plTarget.isDie() || plTarget.zone == null) {
                            return;
                        }
                        var map128 = services.map.MapService.gI().getMapById(128);
                        if (map128 == null || zoneId >= map128.zones.size()) {
                            return;
                        }
                        Zone zone = map128.zones.get(zoneId);
                        if (zone == null) {
                            return;
                        }
                        services.map.ChangeMapService.gI().changeMap(plTarget, zone, -1, 336);

                        server.GameLoopManager.gI().schedule(() -> {
                            try {
                                if (plTarget == null || plTarget.isOffline || plTarget.isDie() || plTarget.zone == null || plTarget.zone.map.mapId != 128) {
                                    return;
                                }
                                plTarget.isMabuHold = false;
                                if (plTarget.effectSkill != null && !plTarget.effectSkill.isShielding) {
                                    services.EffectSkillService.gI().setMabuHold(plTarget, mabuHold);

                                    server.GameLoopManager.gI().schedule(() -> {
                                        try {
                                            if (plTarget != null && !plTarget.isOffline && plTarget.fusion != null && plTarget.pet != null && plTarget.fusion.typeFusion != consts.ConstPlayer.NON_FUSION) {
                                                plTarget.pet.unFusion();
                                            }
                                        } catch (Exception ignored) {
            utils.Logger.error("[Service] " + ignored.getMessage());
        }
                                    }, 1500);
                                }
                            } catch (Exception ex) {
                                Logger.error("unFusion scheduled task error", ex);
                            }
                        }, 500);
                    } catch (Exception e) {
                        Logger.error("fusion scheduled task error", e);
                    }
                }, 3000);
            }
        }
    }

    public void sendMabuAttackSkill(Player player) {
        Message msg;
        try {
            int skillId[] = {0, 1, 3};
            int skill = skillId[Util.nextInt(3)];
            if (Util.isTrue(1, 10)) {
                skill = 2;
            }
            msg = new Message(51);
            msg.writer().writeInt((int) player.id); // charid
            msg.writer().writeByte(skill); // skill id 0 1 2 3
            msg.writer().writeShort(player.location.x); // x
            msg.writer().writeShort(player.location.y); // y
            msg.writer().writeByte(player.zone.getNotBosses().size()); // số player
            for (Player plM : player.zone.getNotBosses()) {
                msg.writer().writeInt((int) plM.id);
                long damage = plM.injured(player, player.nPoint.dame + plM.nPoint.hp / 10, true, false);
                msg.writer().writeInt(NPoint.safeInt(damage));
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    //========================READ OPT========================
    public Message messageReadOpt(byte command) throws IOException {
        Message ms = new Message(24);
        ms.writer().writeByte(command);
        return ms;
    }

//    public void sendMessageServer(String data) {
//        Message msg;
//        try {
//            msg = messageReadOpt((byte) 4);
//            msg.writer().writeUTF(data);
//            sendMessAllPlayer(msg);
//        } catch (Exception e) {
//        }
//    }

    public void sendHideNpc(Player player, int npcId, boolean isHide) {
        Message msg;
        try {
            msg = new Message(-73);
            msg.writer().writeByte(npcId);
            msg.writer().writeByte(isHide ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendTopRank(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-119);
            msg.writer().writeInt(pl.superRank.rank);
            pl.sendMessage(msg);
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void moveFast(Player pl, int x, int y) {
        Message msg;
        try {
            msg = new Message(58);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeInt((int) pl.id);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void dropAndPickItemDNC(Player pl, int itemId) {
        ItemMap item = new ItemMap(pl.zone, itemId, 1, pl.location.x, pl.location.y, pl.id);
        item.options.add(new ItemOption(71 - (itemId - 220), 0));
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void dropAndPickItem(Player pl, int itemId, int quantity) {
        ItemMap item = new ItemMap(pl.zone, itemId, quantity, pl.location.x, pl.location.y, pl.id);
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void sendTypePK(Player player, Player boss) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void playerInfoUpdate(Player pl, Player plR, String plName, int plHead, int plBody, int plLeg) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            msg = messageSubCommand((byte) 7);
            msg.writer().writeInt((int) pl.id);
            if (pl.clan != null) {
                msg.writer().writeInt(pl.clan.id);
            } else if (pl.isCopy) {
                msg.writer().writeInt(-2);
            } else {
                msg.writer().writeInt(-1);
            }
            msg.writer().writeByte(Service.gI().getCurrLevel(pl));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(plHead);
            msg.writer().writeUTF(plName);
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hp));
            msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hpMax));
            msg.writer().writeShort(plBody);
            msg.writer().writeShort(plLeg);
            int flagbag = pl.getFlagBag();
            if (pl.isPl() && plR.getSession().version >= 228) {
                switch (flagbag) {
                    case 83 ->
                        flagbag = 250;
                }
            }
            msg.writer().writeByte(flagbag); //bag
            msg.writer().writeByte(-1);
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(0);
            msg.writer().writeShort(pl.getAura()); //idauraeff
            msg.writer().writeByte(pl.getEffFront()); //seteff
            msg.writer().writeShort(pl.getHat()); //id hat
            plR.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }

    }

    public void sendLoginFail(MySession session, boolean isLoggingIn) {
        Message msg;
        try {
            msg = new Message(-102);
            msg.writer().writeByte(isLoggingIn ? 1 : 0);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

   public void showListTop(Player player, List<TOP> tops) {
        Message msg;
        try {
            if (tops == null || tops.isEmpty()) {
                Service.gI().sendThongBao(player, "Chưa có dữ liệu bảng xếp hạng.");
                return;
            }

            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top");
            msg.writer().writeByte(tops.size());

            for (int i = 0; i < tops.size(); i++) {
                TOP top = tops.get(i);
                Player pl = null;
                
                // --- SỬA ĐOẠN NÀY ---
                // Bước 1: Ưu tiên lấy người chơi đang Online để có Avatar chính xác nhất
                if (top.getId_player() != -1) {
                    pl = Client.gI().getPlayer(top.getId_player());
                }

                // Bước 2: Nếu người chơi không Online (null), thì mới load từ Database
                if (pl == null && top.getId_player() != -1) {
                     pl = NDVSqlFetcher.loadById(top.getId_player());
                }
                // --------------------

                msg.writer().writeInt(i + 1); // Rank

                if (pl != null) {
                    msg.writer().writeInt((int) pl.id);
                    msg.writer().writeShort(pl.getHead());
                    if (player.getSession().version > 214) {
                        msg.writer().writeShort(-1);
                    }
                    msg.writer().writeShort(pl.getBody());
                    msg.writer().writeShort(pl.getLeg());
                    msg.writer().writeUTF(pl.name);
                } else {
                    // Trường hợp không tìm thấy (Acc xóa, hoặc Top ảo)
                    msg.writer().writeInt(-1); 
                    msg.writer().writeShort(95); // Head mặc định
                    if (player.getSession().version > 214) {
                        msg.writer().writeShort(-1);
                    }
                    msg.writer().writeShort(96); // Body mặc định
                    msg.writer().writeShort(97); // Leg mặc định
                    msg.writer().writeUTF("---"); 
                }

                msg.writer().writeUTF(top.getInfo1());
                msg.writer().writeUTF(top.getInfo2());
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.error("showTop error", e);
        }
    }

    public void stealMoney(Player pl, int stealMoney) {//danh cho boss an trom
        Message msg;
        try {
            msg = new Message(95);
            msg.writer().writeInt(stealMoney);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.error("[Service] " + e.getMessage());
        }
    }

    public void sendServerMessage(Player player, String text) {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendDialogMessage(Player pl, String text) {
        Message msg = null;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

}

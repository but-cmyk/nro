package models.mob;
import consts.ConstItem;
import services.player.InventoryService;
import services.player.ClanService;
import services.Service;
import services.TaskService;
import services.map.ItemMapService;
import consts.ConstMap;
import consts.ConstMob;
import consts.ConstTask;
import services.map.SmartDropService;
import event.EventManager;
import models.item.Item;
import models.map.ItemMap;
import java.util.List;
import models.map.Zone;
import models.player.Location;
import models.player.Pet;
import models.player.Player;
import network.io.Message;
import java.io.IOException;
import server.Maintenance;
import server.Manager;
import utils.Util;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;
import services.AchievementService;
import services.phoban.TrainingService;
import services.ItemService;
import services.map.MapService;
import models.skill.Skill;
import services.ChatGlobalService;
import services.RewardService;
import utils.TimeUtil;
import utils.Logger;
import models.player.NPoint;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;
    
    private long hp;
    private long maxHp;

    public List<Player> temporaryEnemies = new CopyOnWriteArrayList<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;

    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;

    private long lastTimeAttackPlayer;
    private long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp(this.point.getHpFull());
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
    }

    public void setTiemNang() {
        this.maxTiemNang = (long) this.point.getHpFull() * (long) (this.pTiemNang + Util.nextInt(-2, 2)) / 100L;
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
        this.lvMob = 0;
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }
    public long getHp() { return hp; }
//    public int getMaxHp() { return maxHp; }
    

    public synchronized void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (!this.isDie()) {
            if (plAtt != null) {
                Player plCheck = (plAtt.isPet && plAtt instanceof Pet && ((Pet) plAtt).master != null) ? ((Pet) plAtt).master : plAtt;
                if (plCheck.clan != null) {
                    int percentDameMob = ClanService.getPercentDameMobByClanFlag(plCheck.clan.imgId);
                    if (percentDameMob > 0) {
                        damage += damage * percentDameMob / 100;
                    }
                }
            }
            if (damage >= this.point.hp) {
                damage = this.point.hp;
            }
            if (!dieWhenHpFull) {
                if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                    damage = this.point.hp - 1;
                }
                if ((this.tempId == ConstMob.MOC_NHAN || this.tempId == ConstMob.BU_NHIN_MA_QUAI) && damage > this.point.maxHp / 10) {
                    damage = 1;
                }
            }
            if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
                boolean mob76Die = true;
                for (Mob mob : this.zone.mobs) {
                    if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                        mob76Die = false;
                        break;
                    }
                }
                if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.LIEN_HOAN, Skill.ANTOMIC, Skill.MASENKO, Skill.KAMEJOKO ->
                            damage = 1;
                    }
                }
            } // tnsm
            if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0 && plAtt != null && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
                damage = (long) (this.point.maxHp * (10.0 / 100));
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2) && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }

            this.point.hp -= damage;
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, damage);
                    TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                    AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                }
                if (this.id == 13) {
                    this.zone.isbulon1Alive = false;
                }
                if (this.id == 14) {
                    this.zone.isbulon2Alive = false;
                }
            } else {
                this.sendMobStillAliveAffterAttacked(damage, plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                long tnsm = getTiemNangForPlayer(plAtt, damage);
                Service.gI().addSMTN(plAtt, (byte) 2, tnsm, true);
                TrainingService.gI().tangTnsmLuyenTap(plAtt, tnsm);
            }
        }
    }

    public long getTiemNangForPlayer(Player pl, long dame) {
        int levelPlayer = Service.gI().getCurrLevel(pl);
        int n = levelPlayer - this.level;
        long tiemNang = (long) (dame + (point.getHpFull() * 0.00035)) / 3;
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (n >= 0) {
            for (int i = 0; i < n; i++) {
                long sub = tiemNang * 10 / 100;
                if (sub <= 0) {
                    sub = 1;
                }
                tiemNang -= sub;
            }
        } else {
            for (int i = 0; i < -n; i++) {
                long add = tiemNang * 10 / 100;
                if (add <= 0) {
                    add = 1;
                }
                tiemNang += add;
            }
        }
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        if (this.tempId == ConstMob.MOC_NHAN||this.tempId == ConstMob.MAY_DO_SUC_MANH) {
            tiemNang = 1;
        }
        
        if (pl.nPoint != null) {
            tiemNang = pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }
        if (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124) {
            tiemNang *= 2;
        }
        if (pl.zone.map.mapId == 153) {
            tiemNang /= 2;
        }
        if (pl.zone.map.mapId >= 156 && pl.zone.map.mapId <= 159) {
            tiemNang /= 2;
        }
        if (pl.zone.map.mapId >= 53 && pl.zone.map.mapId <= 63) {
            tiemNang *= 3;
        }
        if (this.lvMob > 0) {
            tiemNang *= 3;
        }
        if (tiemNang <= 0) {
            tiemNang = 1;
        }
        return tiemNang;
    }

    public void update() {
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }

        if (this.isDie() && !Maintenance.isRunning && !isBigBoss()) {
            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive && Util.canDoWithTime(lastTimeDie, 30000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    break;
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                    break;
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                    break;
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default:
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimeDie, 5000)) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }
                    break;
            }
        } else if (!this.isDie()) {
            if (Util.canDoWithTime(lastTimePhucHoi, 30000)) {
                lastTimePhucHoi = System.currentTimeMillis();
                long hpMax = this.point.maxHp;
                if (this.point.hp < hpMax) {
                    hoi_hp(NPoint.safeInt(hpMax / 10));
                }
            }
        }
        effectSkill.update();
        attack();
    }

    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN || this.tempId == ConstMob.VUA_BACH_TUOC
                || this.tempId == ConstMob.ROBOT_BAO_VE || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA || this.tempId == ConstMob.GA_CHIN_CUA
                || this.tempId == ConstMob.NGUA_CHIN_LMAO || this.tempId == ConstMob.PIANO||this.tempId == ConstMob.MAY_DO_SUC_MANH);
    }

    public void attack() {
        if (isDie() || this.zone == null || this.zone.map == null) {
            return;
        }
        Player player = getPlayerCanAttack();
        if (!effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN && tempId != ConstMob.MAY_DO_SUC_MANH && tempId != ConstMob.BU_NHIN_MA_QUAI && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss() && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId)) && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
            if (player != null) {
                this.mobAttackPlayer(player);
            }
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            return plAttack;
        }
        int distance = 100;
        try {
            if (this.zone == null) {
                return null;
            }
            List<Player> players = this.zone.getNotBosses();
            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    try {
                        if (i >= players.size()) {
                            break;
                        }
                        Player pl = players.get(i);
                        if (pl == null || pl.beforeDispose || pl.isOffline || pl.location == null || pl.nPoint == null || pl.isDie()) {
                            continue;
                        }
                        if (pl.isBoss || pl.isNewPet) {
                            continue;
                        }
                        if ((pl.satellite != null && pl.satellite.isDefend) || (pl.effectSkin != null && pl.effectSkin.isVoHinh)) {
                            continue;
                        }
                        if ((this.tempId > 18 || (this.tempId > 9 && this.type == 4)) || isBigBoss()) {
                            int dis = Util.getDistance(pl, this);
                            if (dis <= distance || isBigBoss()) {
                                plAttack = pl;
                                distance = dis;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi getPlayerCanAttack của mob: " + this.id);
        }
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            if (zone == null) {
                return null;
            }
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (int i = 0; i < playersMap.size(); i++) {
                    try {
                        if (i >= playersMap.size()) {
                            break;
                        }
                        Player plAttt = playersMap.get(i);
                        if (plAttt == null || plAttt.beforeDispose || plAttt.isOffline || plAttt.location == null || plAttt.nPoint == null || plAttt.isDie()) {
                            continue;
                        }
                        if (plAttt.isBoss || (plAttt.satellite != null && plAttt.satellite.isDefend) || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh) || !this.temporaryEnemies.contains(plAttt)) {
                            continue;
                        }
                        int d = Util.getDistance(plAttt, this);
                        if (d <= dis) {
                            dis = d;
                            plAtt = plAttt;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi getFirstPlayerCanAttack của mob: " + this.id);
        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        if (player == null || player.beforeDispose || player.isOffline || player.nPoint == null || player.location == null || player.isDie()) {
            return;
        }
        long dameMob = this.point.getDameAttack();
        if (this.lvMob > 0 && this.zone != null && this.zone.map != null && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            dameMob = (long) (player.nPoint.hpMax * (10.0 / 100));
        }
        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob -= (dameMob * 30 / 100);
        }
        if (player.isPet && player instanceof Pet && ((Pet) player).master != null && ((Pet) player).master.charms != null && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }
        if (player.itemTime != null && player.itemTime.isUseCMS) {
            dameMob = (long) Math.round(dameMob * 0.1);
        }
        if (this.lvMob > 0 && player.charms != null && player.charms.tdOaiHung > System.currentTimeMillis()) {
            dameMob = 0;
        }
        long dame = player.injured(null, dameMob, false, true);

        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, long dame) {
        if (!player.isPet && !player.isNewPet && !player.isBot) {
            Message msg = null;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writer().writeInt(NPoint.safeInt(dame)); //dame
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(Mob.class, e, "Lỗi gửi mob tấn công tôi: player=" + player.name);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg = null;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt(NPoint.safeInt(player.nPoint.hp));
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi gửi mob tấn công player khác: player=" + (player != null ? player.name : "null"));
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        if (this.zone != null && this.zone.map != null && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            this.lvMob();
        }
    }

    public int lvMob() {
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap.lvMob > 0) {
                return 0;
            }
        }
        // Giảm tần suất xuất hiện siêu quái xuống 2% (mặc định cũ 10% quá dày)
        this.lvMob = (byte) (this.tempId > 18 && !isBigBoss() ? Util.isTrue(2, 100) ? 1 : 0 : 0);
        this.point.hp = this.point.getHpFull();
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob);
            msg.writer().writeInt(NPoint.safeInt(this.point.hp));
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi gửi gói tin mob hồi sinh: mobId=" + this.id);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoi_hp(int hp) {
        Message msg = null;
        try {
            this.point.sethp(this.point.gethp() + hp);
            long HP = hp > 0 ? 1L : Math.abs((long) hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(NPoint.safeInt(this.point.gethp()));
            msg.writer().writeInt(NPoint.safeInt(HP));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi gửi gói tin mob hồi hp: mobId=" + this.id);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(NPoint.safeInt(this.point.gethp()));
            msg.writer().writeInt(NPoint.safeInt(this.point.gethp()));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi gửi effect mob: mobId=" + this.id + ", effect=" + Effect);
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, long dameHit) {
        Message msg = null;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(NPoint.safeInt(dameHit));
            msg.writer().writeBoolean(plKill != null && plKill.nPoint != null && plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            try {
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            } catch (Exception ex) {
                Logger.logException(Mob.class, ex, "Lỗi gửi gói tin mob die tới zone " + (this.zone != null ? this.zone.zoneId : "null"));
            } finally {
                msg.cleanup();
                msg = null;
            }
            if (plKill != null && items != null && !items.isEmpty()) {
                hutItem(plKill, items);
            }
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi xử lý mob chết sau tấn công: mobId=" + this.id + ", plKill=" + (plKill != null ? plKill.name : "null"));
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (player == null || items == null || items.isEmpty() || player.isBot) {
            return;
        }
        Player plMaster = player;
        if (player.isPet && player instanceof Pet) {
            plMaster = ((Pet) player).master;
        } else if (player.isNewPet && player instanceof models.player.NewPet) {
            plMaster = ((models.player.NewPet) player).master;
        }
        if (plMaster != null && plMaster.charms != null && plMaster.charms.tdThuHut > System.currentTimeMillis()) {
            for (ItemMap item : items) {
                if (item != null) {
                    ItemMapService.gI().pickItem(plMaster, item.itemMapId, true);
                }
            }
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));
            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size()); //sl item roi
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);// itemmapid
                msg.writer().writeShort(itemMap.itemTemplate.id); // id item
                msg.writer().writeShort(itemMap.x); // xend item
                msg.writer().writeShort(itemMap.y); // yend item
                msg.writer().writeInt((int) itemMap.playerId); // id nhan vat
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemReward;
    }

//    private boolean isHeoNe() {
//        return this.tempId == ConstMob.HEO_DA_XANH || this.tempId == ConstMob.HEO_RUNG
//                || this.tempId == ConstMob.HEO_RUNG_ME
//                || this.tempId == ConstMob.HEO_XANH_ME || this.tempId == ConstMob.HEO_XAYDA
//                || this.tempId == ConstMob.HEO_XAYDA_ME;
//    }

    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();
        if (player.isBoss) {
            return list;
        }
        if (this.tempId == 0) {
            return list;
        }

        Player plReward = (player.isPet && player instanceof Pet) ? ((Pet) player).master : player;
        if (plReward != null && !plReward.isBot && !plReward.isNewPet && plReward.canReceiveDailyMobRuby()) {
            list.add(new ItemMap(this.zone, 861, 30, x, yEnd, plReward.id));
            plReward.lastTimeRewardDailyMobRuby = System.currentTimeMillis();
            Service.gI().sendThongBao(plReward, "Bạn đã nhận được 30 Hồng ngọc từ quái vật đầu tiên trong ngày!");
            database.daos.PlayerDAO.updatePlayerAsync(plReward);
        }

        // ======================== SMART DROP TÂN THỦ ========================
        if (plReward != null && !plReward.isBoss && !plReward.isBot && !plReward.isNewPet
                && plReward.nPoint != null && plReward.nPoint.power < SmartDropService.MAX_POWER_APPLY) {
            ItemMap smartItem = SmartDropService.gI().rollDrop(this, plReward, x, yEnd);
            if (smartItem != null) {
                list.add(smartItem);
                plReward.smartDropCountToday++;
                plReward.smartDropStreak = 0;
            } else {
                plReward.smartDropStreak++;
            }
        }

        int mapid = player.zone.map.mapId;
        int co4LaRate = player.itemTime.isUseCo4La ? 2 : 1; // x2 khi có cỏ 4 lá, bình thường = 1


//       //========================SKH NEW========================
//        if (MapService.gI().isMapUpSKH(mapid)) {
//            if (Util.isTrue(co4LaRate, 100)) { // 1/2000 mặc định, 2/2000 khi có cỏ 4 lá
//                short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//
//                // Lấy option mặc định theo shop
//                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
//                if (!ops.isEmpty()) {
//                    it.options = ops;
//                }
//
//                // Lấy option random kích hoạt (tối đa 4 dòng)
//                int[] opsrand = ItemService.gI().randOptionItemKichHoatNew(player.gender);
//                it.options.add(new Item.ItemOption(opsrand[0], 0));
//                it.options.add(new Item.ItemOption(opsrand[1], 0));
//                it.options.add(new Item.ItemOption(opsrand[2], 0));
//                it.options.add(new Item.ItemOption(opsrand[3], 0));
//                it.options.add(new Item.ItemOption(30, 0)); // option mặc định cuối
//
//                list.add(it);
//
//                ChatGlobalService.gI().sendThongBaoTheGioi(player,
//                        "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name + " Sét Kích Hoạt");
//            }
//        }


        //========================CSKB========================
        if (player.itemTime.isUseMayDo && (Util.isTrue(20, 100)) && this.tempId > 57 && this.tempId < 66) {
            list.add(new ItemMap(zone, 380, 1, x, yEnd, player.id));
        }
         
        //========================NV 7S========================
        if (player.isPl() && TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
            if (player.gender == 0 && this.tempId == 11 || player.gender == 1 && this.tempId == 12 || player.gender == 2 && this.tempId == 10) {
                list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
            }
        }               
        
        //========================NV nhặt 993========================
        if (player.isPl() && TaskService.gI().getIdTask(player) == ConstTask.TASK_31_6) {
            if (Util.isTrue(co4LaRate, 100)) { // 1/100 mặc định, 2/100 khi có cỏ 4 lá
                list.add(new ItemMap(zone, 993, 1, x, yEnd, player.id));
            }
        }


     //========================Map Bang Hội========================
        if (MapService.gI().isMapUpPorata(mapid)) {
            int dropRate1 = 1;
            int dropRate2 = 1;
            int dropRate3 = 1;

            if (player.itemTime.isUseCo4La) {
                dropRate1 += 3;
                dropRate2 += 2;
                dropRate3 += 1;
            }

            if (Util.isTrue(dropRate1, 10)) {
                ItemMap it = new ItemMap(zone, 933, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(31, 1000));
                list.add(it);
            } else if (Util.isTrue(dropRate2, 10)) {
                ItemMap it = new ItemMap(zone, 934, 1000, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 1));
                list.add(it);
            } else if (Util.isTrue(dropRate3, 10)) {
                ItemMap it = new ItemMap(zone, 935, 1000, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 1));
                list.add(it);
            }
        }

      //========================VÀNG RƠI========================
        if (this.lvMob > 0 || Util.isTrue(5 * co4LaRate, 100)) { // 10% mặc định, 20% khi có cỏ 4 lá, siêu quái luôn rơi vàng
            int vang = MapService.gI().isMapNappa(mapid) ? Util.nextInt(5000, 10000)
                    : MapService.gI().isMap3Planets(mapid) ? Util.nextInt(500, 1000)
                    : MapService.gI().isMapTuongLai(mapid) ? Util.nextInt(9000, 15000)
                    : MapService.gI().isMapCold(mapid) ? Util.nextInt(15000, 20000)
                    : (mapid >= 155 && mapid <= 159) ? Util.nextInt(10000, 15000)
                            : Util.nextInt(1000, 5000);

            if (this.lvMob > 0) {
                vang *= 3;
            }

            if (player.nPoint.tlGold > 0) {
                vang += vang * player.nPoint.tlGold / 100;
            }

            if (vang < 20000) {
                list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
            } else if (vang < 30000) {
                list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
            } else {
                list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
            }
        }

        
        
       //========================SÁCH HỌC SKILL 9========================
//        if (player.getSession() != null && player.getSession().player != null && player.getSession().player.nPoint != null && player.getSession().player.nPoint.power >= 80000000000L) {
//            ItemMap it = new ItemMap(zone, 1191, 1, x, yEnd, player.id);
//            it.options.add(new Item.ItemOption(30, 0));
//            list.add(it);
//        }

        //========================BÌNH NƯỚC========================
        if (mapid == 5 && Util.isTrue(1, 20000)) {
            ItemMap it = new ItemMap(zone, 456, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(93, 1));
            it.options.add(new Item.ItemOption(30, 1));
            list.add(it);
        }
        
        
         //========================SKH CŨ (ĐÃ TẮT RƠI THEO YÊU CẦU)========================
//        if (MapService.gI().isMapUpSKH(mapid)) {
//            if (Util.isTrue(co4LaRate, 2000)) { // 1/2000 thường, 2/2000 khi có cỏ 4 lá (0.05% - 0.1%)
//                short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//
//                RewardService.gI().initBaseOptionClothes(itTemp, it.itemTemplate.type, it.options);
//
//                int[] opsrand = ItemService.gI().randOptionItemKichHoat(player.gender);
//                it.options.add(new Item.ItemOption(opsrand[0], 0));
//                it.options.add(new Item.ItemOption(opsrand[1], 0));
//                it.options.add(new Item.ItemOption(30, 0));
//
//                list.add(it);
//
//                ChatGlobalService.gI().sendThongBaoTheGioi(player,
//                        "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name + " Sét Kích Hoạt");
//            }
//        }

          //========================ĐỒ SAO 3 MAP ĐẦU (ĐÃ TẮT THEO YÊU CẦU)========================
//        if (player.isPl() && MapService.gI().isMapUpSKH(mapid)) {           
//            if (Util.isTrue(co4LaRate, 2500)) {
//                short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
//                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
//
//                RewardService.gI().initBaseOptionClothes(itTemp, it.itemTemplate.type, it.options);
//
//                it.options.add(new Item.ItemOption(107, randomSaoDo())); // sao đỏ ngẫu nhiên
//                it.options.add(new Item.ItemOption(208, 0));
//
//                list.add(it);
//
//                ChatGlobalService.gI().sendThongBaoTheGioi(player,
//                        "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name + " Sét Kích Hoạt");
//            }
//        }

        
         //========================ĐỒ SAO MAP < NAPPA========================
        if (MapService.gI().isMapUpDoSao(mapid)) {          
            if (Util.isTrue(co4LaRate, 2000)) { // 1/2000 hoặc 2/2000 khi có cỏ 4 lá
                short itTemp1 = (short) ItemService.gI().randTempItemDoSao(player.gender);
                ItemMap it = new ItemMap(zone, itTemp1, 1, x, yEnd, player.id);

                RewardService.gI().initBaseOptionClothes(itTemp1, it.itemTemplate.type, it.options);

                it.options.add(new Item.ItemOption(107, randomSaoDo())); // thêm sao đỏ ngẫu nhiên

                list.add(it);
            }
        }


         //========================SAO PHA LÊ========================
        if (Util.isTrue(1, 2000) || (player.nPoint.isDoSPL && Util.isTrue(5, 1000))) {
            int rand = Util.nextInt(0, 6);
            ItemMap it = new ItemMap(zone, 441 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
            list.add(it);
        }

         //========================ĐÁ NÂNG CẤP========================
        if (Util.isTrue(1, 2000) || (Util.isTrue(5, 2000) && MapService.gI().isMapCold(mapid))) {
            int rand = Util.nextInt(0, 4);
            ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(71 - rand, 0));
            list.add(it);
            updateTaskDopDo(player);
        }
        
         //========================ĐỒ THẦN MAP COLD========================
        if (MapService.gI().isMapCold(mapid)) {
            // Lấy player gốc nếu là pet
            if (player.isPet && player instanceof Pet) {
                player = ((Pet) player).master;
            }

            if (player != null && !player.isPet) { // đảm bảo không bị null hoặc là pet tiếp
                if (Util.isTrue(1 * co4LaRate, 2000)) { // 0.05% mặc định, 0.1% khi có cỏ 4 lá
                    ItemMap it = ItemService.gI().randDoTL(this.zone, 1, x, yEnd, player.id);
                    list.add(it);

                    updateTaskDopDo(player);

                    ChatGlobalService.gI().sendThongBaoTheGioi(player,
                            "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                            + " tại " + this.zone.map.mapName + " khu " + this.zone.zoneId);
                }
            }
        }

         //========================THỨC ĂN========================
          if (InventoryService.gI().fullSetThan(player) && Util.isTrue(1 * co4LaRate, 2000)) {
            int tempId = Util.nextInt(663, 667); // random 663 - 666
            ItemMap it = new ItemMap(zone, tempId, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(73, 0)); // option mặc định

            list.add(it);

            updateTaskDopDo(player);
        }

            
        //========================ĐỒ LƯỠNG LONG MAP COLD========================
        if (MapService.gI().isMapCold(mapid) && Util.isTrue(1 * co4LaRate, 2000)) {
            // 0.05% mặc định, 0.1% khi có cỏ 4 lá

            // Nhặt item Lưỡng Long ngẫu nhiên
            ItemMap it = ItemService.gI().randDoLuongLong(this.zone, 1, x, yEnd, player.id);
            list.add(it);

            updateTaskDopDo(player);

            // Thông báo server
            ChatGlobalService.gI().sendThongBaoTheGioi(player,
                    "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                    + " tại " + this.zone.map.mapName + " khu " + this.zone.zoneId);
        }
        
          // ======================== ĐỒ KAIO MAP NAPPA ========================
        if (MapService.gI().isMapNappa(mapid) && Util.isTrue(1 * co4LaRate, 2000)) {
            // 0.05% mặc định, 0.1% khi có cỏ 4 lá
            ItemMap it = ItemService.gI().randDoKaio(this.zone, 1, x, yEnd, player.id);
            list.add(it);

            // 5% cơ hội thêm option ngẫu nhiên
            if (Util.isTrue(5 * co4LaRate, 1000)) {
                int randomOption = Util.nextInt(34, 35);
                it.options.add(new Item.ItemOption(randomOption, 0));
            }

            updateTaskDopDo(player);

            ChatGlobalService.gI().sendThongBaoTheGioi(player,
                    "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                    + " tại " + this.zone.map.mapName + " khu " + this.zone.zoneId);
        }

// ======================== ĐỒ KAIO MAP TƯƠNG LAI ========================
        if (MapService.gI().isMapTuongLai(mapid) && Util.isTrue(1 * co4LaRate, 2000)) {
            // 0.05% mặc định, 0.1% khi có cỏ 4 lá
            ItemMap it = ItemService.gI().randDoKaio(this.zone, 1, x, yEnd, player.id);
            list.add(it);

            // 5% cơ hội thêm option ngẫu nhiên
            if (Util.isTrue(5 * co4LaRate, 1000)) {
                int randomOption = Util.nextInt(34, 35);
                it.options.add(new Item.ItemOption(randomOption, 0));
            }

            updateTaskDopDo(player);

            ChatGlobalService.gI().sendThongBaoTheGioi(player,
                    "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                    + " tại " + this.zone.map.mapName + " khu " + this.zone.zoneId);
        }



         //========================MẢNH ĐÁ VỤN========================
        if (Util.isTrue(1, 1000)) {
            list.add(new ItemMap(zone, 225, 1, x, yEnd, player.id));
        }
        
         //========================NRO========================
        if (Util.isTrue(1, 2000)) {
            list.add(new ItemMap(zone, 19, 1, x, yEnd, player.id));
        }
        if (Util.isTrue(1, 2000)) {
            list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
        }
        
        //========================NGỌC========================
        if (Util.isTrue(1, 500000)) {
            list.add(new ItemMap(zone, 77, 1000, x, yEnd, player.id));
        }
       
         //========================VÀNG MAP DOANH TRẠI========================
        if (this.zone.map.mapId >= 53 && this.zone.map.mapId <= 63) {
               list.add(new ItemMap(zone, 190, 31000, x, yEnd, player.id));
        }
         
        //========================VÀNG BDKB========================
        if ((zone.map.mapId >= 135 && zone.map.mapId <= 138) && player.clan != null && player.clan.BanDoKhoBau != null) {
            boolean isBigMob = (this.lvMob == 1 || this.tempId == 71 || this.tempId == 72);
            int dropY = (yEnd > 0) ? yEnd : this.location.y;

            if (isBigMob) {
                // Quái lớn / Thủ lĩnh: 5 - 8 cọc vàng lớn, mỗi cọc 300.000 - 600.000 vàng
                int numPiles = Util.nextInt(5, 8);
                for (int i = 0; i < numPiles; i++) {
                    int goldAmount = Util.nextInt(300_000, 600_000);
                    int itemId = (i % 2 == 0) ? 190 : 76;
                    int offsetX = (i % 2 == 0) ? (i * 18) : (-i * 18);
                    ItemMap it = new ItemMap(this.zone, itemId, goldAmount, this.location.x + offsetX, dropY, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                }
                // 15% rơi 1 Thỏi vàng (Item 457)
                if (Util.isTrue(15, 100)) {
                    ItemMap itGoldBar = new ItemMap(this.zone, 457, 1, this.location.x, dropY, player.id);
                    Service.gI().dropItemMap(this.zone, itGoldBar);
                }
            } else {
                // Quái nhỏ: 3 - 5 cọc vàng, mỗi cọc 60.000 - 150.000 vàng
                int numPiles = Util.nextInt(3, 5);
                for (int i = 0; i < numPiles; i++) {
                    int goldAmount = Util.nextInt(60_000, 150_000);
                    int itemId = (i % 2 == 0) ? 190 : 76;
                    int offsetX = (i % 2 == 0) ? (i * 15) : (-i * 15);
                    ItemMap it = new ItemMap(this.zone, itemId, goldAmount, this.location.x + offsetX, dropY, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                }
            }
        }
        
        //========================MẢNH THIÊN SỨ========================
        if (MapService.gI().isMapHanhTinhThucVat(mapid) && InventoryService.gI().findItemNTK(player)) {
            if (Util.isTrue(1 * co4LaRate, 2500)) { // 0.04% - 0.08% khi có cỏ 4 lá
                int tempId = Util.nextInt(1066, 1070); // random item
                ItemMap it = new ItemMap(zone, tempId, 1, x, yEnd, player.id);
                list.add(it);

                // Nếu cần log để anti tool farm:
                ChatGlobalService.gI().sendThongBaoTheGioi(player,
                        "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                        + " tại " + zone.map.mapName + " khu " + zone.zoneId);
            }
        }


        //========================CHUỐI (ĐÃ TẮT RƠI CÀ CHUA & CÀ RỐT)========================
        Player plOwner = (player.isPet && player instanceof Pet) ? ((Pet) player).master : player;
        if (plOwner != null && plOwner.nPoint != null) {
            boolean isOver1B5 = plOwner.nPoint.power > 1_500_000_000L;
            if (player.isPet && player.nPoint != null && player.nPoint.power > 1_500_000_000L) {
                isOver1B5 = true;
            } else if (plOwner.pet != null && plOwner.pet.nPoint != null && plOwner.pet.nPoint.power > 1_500_000_000L) {
                isOver1B5 = true;
            }

            boolean isDrop = isOver1B5 ? Util.isTrue(1, 1000) : Util.isTrue(1, 100); // 0.1% nếu > 1.5 tỷ, 1% nếu <= 1.5 tỷ
            if (isDrop) {
                ItemMap it = new ItemMap(zone, ConstItem.CHUOI, 1, x, yEnd, plOwner.id);
                it.options.add(new Item.ItemOption(83, 20));
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            }
        }

        //========================THẺ SỔ SƯU TẦM (RADAR CARD)========================
        rollCardMobReward(plOwner, x, yEnd, co4LaRate, list);

        return list;
    }

    private void rollCardMobReward(Player plOwner, int x, int yEnd, int co4LaRate, List<ItemMap> list) {
        if (plOwner == null || plOwner.isBoss || plOwner.isBot || plOwner.isNewPet) {
            return;
        }

        short cardId = -1;
        int chance = 0;       // Xác suất
        int totalChance = 10000;
        boolean isRare = false;

        switch (this.tempId) {
            // Rank 0: Quái sơ cấp (3 / 10.000 = 0.03% ~ 1/3.333 con)
            case ConstMob.KHUNG_LONG -> { cardId = 828; chance = 3; }
            case ConstMob.LON_LOI -> { cardId = 829; chance = 3; }
            case ConstMob.QUY_DAT -> { cardId = 830; chance = 3; }

            // Rank 1: Quái mẹ tinh anh (60 / 10.000 = 0.60% ~ 1/166 con mẹ)
            case ConstMob.KHUNG_LONG_ME -> { cardId = 831; chance = 60; }
            case ConstMob.LON_LOI_ME -> { cardId = 832; chance = 60; }
            case ConstMob.QUY_DAT_ME -> { cardId = 833; chance = 60; }

            // Rank 2: Quái bay (4 / 10.000 = 0.04% ~ 1/2.500 con)
            case ConstMob.THAN_LAN_BAY, ConstMob.THAN_LAN_BAY_2 -> { cardId = 834; chance = 4; }
            case ConstMob.PHI_LONG, ConstMob.PHI_LONG_2 -> { cardId = 835; chance = 4; }
            case ConstMob.QUY_BAY, ConstMob.QUY_BAY_2 -> { cardId = 836; chance = 4; }

            // Rank 3: Lính Red Ribbon, Sói xám, Quái BDKB (25 / 10.000 = 0.25% ~ 1/400 con)
            case ConstMob.LINH_DOC_NHAN -> { cardId = 837; chance = 25; }
            case ConstMob.LINH_DOC_NHAN2 -> { cardId = 838; chance = 25; }
            case ConstMob.SOI_XAM, ConstMob.ROBOT_BAY, ConstMob.ROBOT_THEP -> { cardId = 839; chance = 25; }

            // Rank 5: Thẻ Khỉ VIP (Rất hiếm - Tạo giá trị cực phẩm)
            case ConstMob.KHI_LONG_DEN -> {
                cardId = 1278; // Thẻ Oozaru Khỉ Đen (Aura 2)
                chance = 1;    // 1 / 10.000 = 0.01%
                isRare = true;
            }
            case ConstMob.KHI_LONG_VANG -> {
                cardId = 1276; // Thẻ Oozaru SSJ Khỉ Vàng (Aura 3 - Siêu Cực Phẩm)
                chance = 1;
                totalChance = 20000; // 1 / 20.000 = 0.005%
                isRare = true;
            }
            default -> {}
        }

        if (cardId != -1) {
            int finalChance = chance * co4LaRate;
            if (Util.isTrue(finalChance, totalChance)) {
                ItemMap itCard = new ItemMap(this.zone, cardId, 1, x, yEnd, plOwner.id);
                itCard.options.add(new Item.ItemOption(87, 0)); // Ký gửi ngọc
                itCard.options.add(new Item.ItemOption(30, 0)); // Khóa giao dịch
                list.add(itCard);

                if (isRare && itCard.itemTemplate != null && this.zone != null && this.zone.map != null) {
                    ChatGlobalService.gI().sendThongBaoTheGioi(plOwner,
                            "[ Hệ Thống ] " + plOwner.name + " vừa săn được "
                            + itCard.itemTemplate.name + " tại " + this.zone.map.mapName + "!");
                }
            }
        }
    }


    private void updateTaskDopDo(Player player) {
        services.TaskService.gI().checkDoneTaskVeChai(player, 1);
    }

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 20)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc mượn hồn này không giữ truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    }
                }
                break;
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 20)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con heo xayda mẹ này không giữ truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    }
                }
                break;
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 20)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc sên này không giữ truyện tranh, hãy thử tìm con ốc sên khác");
                    }
                }
                break;
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(long dameHit, boolean crit) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(NPoint.safeInt(this.point.gethp()));
            msg.writer().writeInt(NPoint.safeInt(dameHit));
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi gửi thông báo mob còn sống sau tấn công: mobId=" + this.id);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writer().writeInt(NPoint.safeInt(this.point.hp));
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi gửi gói tin hồi sinh mob phó bản: mobId=" + this.id);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writer().writeInt(NPoint.safeInt(this.point.hp));
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi gửi gói tin hồi sinh mob tháp Karin: mobId=" + this.id);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

//    public void sendSieuQuai(int type) {
//        Message msg;
//        try {
//            msg = new Message(-75);
//            msg.writer().writeByte(this.id);
//            msg.writer().writeByte(type);
//            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
//            msg.cleanup();
//        } catch (IOException e) {
//        }
//    }
//
//    public void sendDisable(boolean bool) {
//        Message msg;
//        try {
//            msg = new Message(81);
//            msg.writer().writeByte(this.id);
//            msg.writer().writeBoolean(bool);
//            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
//            msg.cleanup();
//        } catch (IOException e) {
//        }
//    }
//
//    public void sendDoneMove(boolean bool) {
//        Message msg;
//        try {
//            msg = new Message(82);
//            msg.writer().writeByte(this.id);
//            msg.writer().writeBoolean(bool);
//            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
//            msg.cleanup();
//        } catch (IOException e) {
//        }
//    }
//
//    public void sendFire(boolean bool) {
//        Message msg;
//        try {
//            msg = new Message(85);
//            msg.writer().writeByte(this.id);
//            msg.writer().writeBoolean(bool);
//            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
//            msg.cleanup();
//        } catch (IOException e) {
//        }
//    }
//
//    public void sendIce(boolean bool) {
//        Message msg;
//        try {
//            msg = new Message(86);
//            msg.writer().writeByte(this.id);
//            msg.writer().writeBoolean(bool);
//            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
//            msg.cleanup();
//        } catch (IOException e) {
//        }
//    }
//
//    public void sendWind(boolean bool) {
//        Message msg;
//        try {
//            msg = new Message(87);
//            msg.writer().writeByte(this.id);
//            msg.writer().writeBoolean(bool);
//            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
//            msg.cleanup();
//        } catch (IOException e) {
//        }
//    }

    public void sendMobMaxHp(long maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(NPoint.safeInt(maxHp));
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private void phanSatThuong(Player plTarget, long dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST > 0) {
            long damePST = dame * percentPST / 100L;
            if (damePST > 0) {
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                if (damePST > 0) {
                    injured(null, damePST, true);
                }
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
    private int randomSaoDo() {
    int rand = Util.nextInt(1, 100);
    if (rand <= 5) return 3;         // 5%
    else if (rand <= 15) return 2;   // 10%
    else if (rand <= 40) return 1;   // 25%
    else return 0;                   // 60%
}

}

package models.mob;
import consts.ConstItem;
import services.player.InventoryService;
import services.Service;
import services.TaskService;
import services.map.ItemMapService;
import consts.ConstMap;
import consts.ConstMob;
import consts.ConstTask;
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
import java.util.Random;
import services.AchievementService;
import services.phoban.TrainingService;
import services.ItemService;
import services.map.MapService;
import models.skill.Skill;
import services.ChatGlobalService;
import utils.TimeUtil;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;
    
    private int hp;
    private int maxHp;

    public List<Player> temporaryEnemies = new ArrayList<>();

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
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }
    public int getHp() { return hp; }
//    public int getMaxHp() { return maxHp; }
    

    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (!this.isDie()) {
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
                damage = (int) ((this.point.maxHp <= 20_000_000 ? this.point.maxHp * 1 : 2) * (10.0 / 100));
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2) && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }

            if (damage > 2_000_000_000) {
                damage = 2_000_000_000;
            }

            this.point.hp -= damage;
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, (int) damage);
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
                this.sendMobStillAliveAffterAttacked((int) damage, plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                Service.gI().addSMTN(plAtt, (byte) 2, getTiemNangForPlayer(plAtt, damage), true);
                TrainingService.gI().tangTnsmLuyenTap(plAtt, getTiemNangForPlayer(plAtt, damage));
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
            tiemNang = (int) pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }
        if (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124) {
            tiemNang *= 2;
        }
        if (pl.zone.map.mapId == 153) {
            tiemNang /= 2;
        }
        if (pl.zone.map.mapId == 156 || pl.zone.map.mapId == 157 || pl.zone.map.mapId == 158 || pl.zone.map.mapId == 159) {
            tiemNang /= 2;
        }
        if (pl.zone.map.mapId >= 53 && pl.zone.map.mapId <= 63) {
            tiemNang *= 3;
        }
        if (pl.zone.map.mapId >= 135 && pl.zone.map.mapId <= 138) {
            tiemNang *= 2;
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
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive && Util.canDoWithTime(lastTimeDie, 10000)) {
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
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        int hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
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
        Player player = getPlayerCanAttack();
        if (!isDie() && !effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN  && tempId != ConstMob.MAY_DO_SUC_MANH && tempId != ConstMob.BU_NHIN_MA_QUAI && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss() && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId)) && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
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
            List<Player> players = this.zone.getNotBosses();
            for (Player pl : players) {
                if (!pl.isDie() && !pl.isBoss && !pl.isNewPet && (pl.satellite == null || !pl.satellite.isDefend) && (pl.effectSkin == null || !pl.effectSkin.isVoHinh) && (this.tempId > 18 || (this.tempId > 9 && this.type == 4)) || isBigBoss()) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {

        }
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (Player plAttt : playersMap) {
                    if (plAttt.isDie() || plAttt.isBoss || (plAttt.satellite != null && plAttt.satellite.isDefend) || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh) || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {

        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        int dameMob = this.point.getDameAttack();
        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (player.isPet && ((Pet) player).master.charms != null && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (this.lvMob > 0 && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            dameMob = (int) (player.nPoint.hpMax * (10.0 / 100));
        }
        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }
        if (player.itemTime != null && player.itemTime.isUseCMS) {
            dameMob = (int) Math.round(dameMob * 0.1);
        }
        if (this.lvMob > 0 && player.charms.tdOaiHung > System.currentTimeMillis()) {
            dameMob = 0;
        }
        int dame = player.injured(null, dameMob, false, true);

        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, int dame) {
        if (!player.isPet && !player.isNewPet && !player.isBot) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writer().writeInt(dame); //dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt(player.nPoint.hp);
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
    }

    public int lvMob() {
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap.lvMob > 0) {
                return 0;
            }
        }
        this.lvMob = this.tempId > 18 && !isBigBoss() ? Util.isTrue(10, 100) ? 1 : 0 : 0;
        this.point.hp = this.lvMob > 0 ? this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000 : this.point.maxHp;
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(0);
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
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
            int HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(HP);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
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
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, int dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isPet && !player.isBot && !player.isNewPet) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        } else {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId, true);
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
        if (Util.isTrue(5 * co4LaRate, 100)) { // 10% mặc định, 20% khi có cỏ 4 lá
            int vang = MapService.gI().isMapNappa(mapid) ? Util.nextInt(5000, 10000)
                    : MapService.gI().isMap3Planets(mapid) ? Util.nextInt(500, 1000)
                    : MapService.gI().isMapTuongLai(mapid) ? Util.nextInt(9000, 15000)
                    : MapService.gI().isMapCold(mapid) ? Util.nextInt(15000, 20000)
                    : (mapid >= 155 && mapid <= 159) ? Util.nextInt(10000, 15000)
                            : Util.nextInt(1000, 5000);

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
        if (mapid == 5 && Util.isTrue(1, 2)) {
            ItemMap it = new ItemMap(zone, 456, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(93, 1));
            it.options.add(new Item.ItemOption(30, 1));
            list.add(it);
        }
        
        
         //========================SKH CŨ========================
        if (MapService.gI().isMapUpSKH(mapid)) {
            if (Util.isTrue(co4LaRate, 20)) { // 1/2000 thường, 2/2000 khi có cỏ 4 lá
                short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);

                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                int[] opsrand = ItemService.gI().randOptionItemKichHoat(player.gender);
                it.options.add(new Item.ItemOption(opsrand[0], 0));
                it.options.add(new Item.ItemOption(opsrand[1], 0));
                it.options.add(new Item.ItemOption(30, 0));

                list.add(it);

                ChatGlobalService.gI().sendThongBaoTheGioi(player,
                        "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name + " Sét Kích Hoạt");
            }
        }

          //========================ĐỒ SAO 3 MAP ĐẦU ========================
        if (player.isPl() && MapService.gI().isMapUpSKH(mapid)) {           
            if (Util.isTrue(co4LaRate, 10)) {
                short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);

                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                it.options.add(new Item.ItemOption(107, randomSaoDo())); // sao đỏ ngẫu nhiên
                it.options.add(new Item.ItemOption(208, 0));

                list.add(it);

                ChatGlobalService.gI().sendThongBaoTheGioi(player,
                        "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name + " Sét Kích Hoạt");
            }
        }

        
         //========================ĐỒ SAO MAP < NAPPA========================
        if (MapService.gI().isMapUpDoSao(mapid)) {          
            if (Util.isTrue(co4LaRate, 50000)) { // 1/2000 hoặc 2/2000 khi có cỏ 4 lá
                short itTemp1 = (short) ItemService.gI().randTempItemDoSao(player.gender);
                ItemMap it = new ItemMap(zone, itTemp1, 1, x, yEnd, player.id);

                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp1);
                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                it.options.add(new Item.ItemOption(107, randomSaoDo())); // thêm sao đỏ ngẫu nhiên

                list.add(it);
            }
        }


         //========================SAO PHA LÊ========================
        if (Util.isTrue(1, 10) || (player.nPoint.isDoSPL && Util.isTrue(5, 100))) {
            int rand = Util.nextInt(0, 6);
            ItemMap it = new ItemMap(zone, 441 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
            list.add(it);
        }

         //========================ĐÁ NÂNG CẤP========================
        if (Util.isTrue(1, 10) || (Util.isTrue(5, 300) && MapService.gI().isMapCold(mapid))) {
            int rand = Util.nextInt(0, 4);
            ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(71 - rand, 0));
            list.add(it);
            updateTaskDopDo(player);
        }
        
         //========================ĐỒ THẦN MAP COLD========================
        if (MapService.gI().isMapCold(mapid)) {
            // Lấy player gốc nếu là pet
            if (player.isPet) {
                player = ((Pet) player).master;
            }

            if (player != null && !player.isPet) { // đảm bảo không bị null hoặc là pet tiếp
                if (Util.isTrue(1 * co4LaRate, 50000)) { // 0.05% mặc định, 0.1% khi có cỏ 4 lá
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
          if (InventoryService.gI().fullSetThan(player) && Util.isTrue(1 * co4LaRate,2)) {
            int tempId = Util.nextInt(663, 667); // random 663 - 666
            ItemMap it = new ItemMap(zone, tempId, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(73, 0)); // option mặc định

            list.add(it);

            updateTaskDopDo(player);
        }

            
        //========================ĐỒ LƯỠNG LONG MAP COLD========================
        if (MapService.gI().isMapCold(mapid) && Util.isTrue(1 * co4LaRate, 50000)) {
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
        if (MapService.gI().isMapNappa(mapid) && Util.isTrue(1 * co4LaRate, 50000)) {
            // 0.05% mặc định, 0.1% khi có cỏ 4 lá
            ItemMap it = ItemService.gI().randDoKaio(this.zone, 1, x, yEnd, player.id);
            list.add(it);

            // 5% cơ hội thêm option ngẫu nhiên
            if (Util.isTrue(5 * co4LaRate, 100)) {
                int randomOption = Util.nextInt(34, 35);
                it.options.add(new Item.ItemOption(randomOption, 0));
            }

            updateTaskDopDo(player);

            ChatGlobalService.gI().sendThongBaoTheGioi(player,
                    "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                    + " tại " + this.zone.map.mapName + " khu " + this.zone.zoneId);
        }

// ======================== ĐỒ KAIO MAP TƯƠNG LAI ========================
        if (MapService.gI().isMapTuongLai(mapid) && Util.isTrue(1 * co4LaRate, 50000)) {
            // 0.1% mặc định, 0.2% khi có cỏ 4 lá
            ItemMap it = ItemService.gI().randDoKaio(this.zone, 1, x, yEnd, player.id);
            list.add(it);

            // 5% cơ hội thêm option ngẫu nhiên
            if (Util.isTrue(5 * co4LaRate, 100)) {
                int randomOption = Util.nextInt(34, 35);
                it.options.add(new Item.ItemOption(randomOption, 0));
            }

            updateTaskDopDo(player);

            ChatGlobalService.gI().sendThongBaoTheGioi(player,
                    "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                    + " tại " + this.zone.map.mapName + " khu " + this.zone.zoneId);
        }



         //========================MẢNH ĐÁ VỤN========================
        if (Util.isTrue(1, 10)) {
            list.add(new ItemMap(zone, 225, 1, x, yEnd, player.id));
        }
        
         //========================NRO========================
        if (Util.isTrue(1, 10000)) {
            list.add(new ItemMap(zone, 19, 1, x, yEnd, player.id));
        }
        if (Util.isTrue(1, 10000)) {
            list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
        }
        
        //========================NGỌC========================
        if (Util.isTrue(1, 100000)) {
            list.add(new ItemMap(zone, 77, 1000, x, yEnd, player.id));
        }
       
         //========================VÀNG MAP DOANH TRẠI========================
        if (this.zone.map.mapId >= 53 && this.zone.map.mapId <= 63) {
               list.add(new ItemMap(zone, 190, 31000, x, yEnd, player.id));
        }
         
        //========================VÀNG BDKB========================
        if ((zone.map.mapId >= 135 && zone.map.mapId <= 138) && Util.isTrue(100, 100)) {
            if (player.clan.BanDoKhoBau.level <= 10) {
                int min = 1000;
                int max = 1700;
                Random random = new Random();
                int randomvang = random.nextInt(max - min + 1) + min;
                int randomvang2 = random.nextInt(max - min + 1) + min;

                for (int i = 0; i < player.clan.BanDoKhoBau.level / 2; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang, this.location.x + i * 20, this.location.y, player.id);
                    //   ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x + i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                    //   Service.gI().dropItemMap(this.zone, it2);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 3; i++) {
                    ItemMap it = new ItemMap(this.zone, 190, randomvang2, this.location.x - i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 4; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x + i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 4; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x - i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
            }
            if (player.clan.BanDoKhoBau.level > 10 && player.clan.BanDoKhoBau.level <= 50) {
                int min = 1200;
                int max = 2000;
                Random random = new Random();
                int randomvang = random.nextInt(max - min + 1) + min;
                int randomvang2 = random.nextInt(max - min + 1) + min;

                for (int i = 0; i < player.clan.BanDoKhoBau.level * (3 / 5); i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang, this.location.x + i * 20, this.location.y, player.id);
                    //   ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x + i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                    //   Service.gI().dropItemMap(this.zone, it2);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 2; i++) {
                    ItemMap it = new ItemMap(this.zone, 190, randomvang2, this.location.x - i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 3; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x + i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 3; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x - i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
            } else if (player.clan.BanDoKhoBau.level > 50 && player.clan.BanDoKhoBau.level <= 80) {
                int min = 3000;
                int max = 3500;
                int minx = 42;
                int maxx = 1165;
                Random random = new Random();
                int randomvang2 = random.nextInt(max - min + 1) + min;
//                int randomtoado = ;
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 4; i++) {
                    ItemMap it = new ItemMap(this.zone, 190, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);

                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 4; i++) {
                    ItemMap it = new ItemMap(this.zone, 190, randomvang2, this.location.x - i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 6; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x + i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 6; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x - i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
            } else {
                int min = 3500;
                int max = 5500;
                int minx = 42;
                int maxx = 1165;
                Random random = new Random();
                int randomvang2 = random.nextInt(max - min + 1) + min;
//                int randomtoado = ;
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 3; i++) {
                    ItemMap it = new ItemMap(this.zone, 190, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);

                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 3; i++) {
                    ItemMap it = new ItemMap(this.zone, 190, randomvang2, this.location.x - i * 20, this.location.y, player.id);
                    Service.gI().dropItemMap(this.zone, it);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 6; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x + i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
                for (int i = 0; i < player.clan.BanDoKhoBau.level / 6; i++) {
                    ItemMap it = new ItemMap(this.zone, 76, randomvang2, this.location.x + i * 20, this.location.y, player.id);
                    //    ItemMap it2 = new ItemMap(this.zone, 861, 1,this.location.x - i * 17, this.location.y, player.id);
                    //    Service.gI().dropItemMap(this.zone, it2);
                }
            }
        }
        
        //========================MẢNH THIÊN SỨ========================
        if (MapService.gI().isMapHanhTinhThucVat(mapid) && InventoryService.gI().findItemNTK(player)) {
            if (Util.isTrue(1 * co4LaRate, 20)) { // 0.05% mặc định, 0.1% khi có cỏ 4 lá
                int tempId = Util.nextInt(1066, 1070); // random item
                ItemMap it = new ItemMap(zone, tempId, 1, x, yEnd, player.id);
                list.add(it);

                // Nếu cần log để anti tool farm:
                ChatGlobalService.gI().sendThongBaoTheGioi(player,
                        "[ Hệ Thống ] " + player.name + " vừa nhặt được " + it.itemTemplate.name
                        + " tại " + zone.map.mapName + " khu " + zone.zoneId);
            }
        }


        return list;
    }


    private void updateTaskDopDo(Player player) {
        if (player.playerTask.taskdh.NhatDo < 500) {
            int percentDone = (int) ((double) player.playerTask.taskdh.NhatDo / 500 * 100);
            player.playerTask.taskdh.NhatDo++;
            player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
           // Service.gI().sendThongBao(player, "Tiến độ hiện tại:  " + percentDone + "%");
        }
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
            case ConstMob.THAN_LAN_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 10)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 20)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc mượn hồn này không giữ truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    }
                }
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 20)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con heo xayda mẹ này không giữ truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    }
                }
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 20)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con ốc xên này không giữ truyện tranh, hãy thử tìm con ốc xên khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(int dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
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

    public void sendMobMaxHp(int maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(maxHp);
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
        if (percentPST != 0) {
            int damePST = (int) (long) (dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                int hpMob = this.point.hp;
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writer().writeInt(this.point.hp);
                msg.writer().writeInt(damePST);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (IOException e) {
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

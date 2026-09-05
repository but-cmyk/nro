package models.phoban;
import utils.Functions;
import models.boss.boss_list.SnakeWay.SAIBAMEN;
import models.boss.boss_list.SnakeWay.NADIC;
import models.boss.boss_list.SnakeWay.CADICH;
import models.boss.Boss;
import consts.BossStatus;
import models.clan.Clan;
import models.map.Zone;
import models.mob.Mob;
import models.player.Player;
import services.ItemTimeService;
import services.map.MapService;
import services.Service;
import services.map.ChangeMapService;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import utils.TimeUtil;
import utils.Logger;
import services.map.ItemMapService;
import services.player.PlayerService;

public class SnakeWay {

//    public static final long POWER_CAN_GO_TO_CDRD = 2000000000;
    public static final int AVAILABLE = 5;
    public static final int TIME_CON_DUONG_RAN_DOC = 1800000;

    public int id;
    public byte level;
    public final List<Zone> zones;
    public Clan clan;

    public List<Zone> getZones() { return this.zones; }
    public Clan getClan() { return this.clan; }
    public void setClan(Clan clan) { this.clan = clan; }
    public int getId() { return this.id; }
    public long getLastTimeOpen() { return this.lastTimeOpen; }
    public boolean isOpened;
    private long lastTimeOpen;
    private long lastTimeUpdateMessage;
    private boolean kickoutcdrd;
    private long timeKickOutCDRD;
    public List<Boss> bosses = new ArrayList<>();
    public boolean endCDRD;
    public boolean allMobsDead;

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public SnakeWay(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }



    public void update() {
        if (isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_CON_DUONG_RAN_DOC) || (kickoutcdrd && Util.canDoWithTime(timeKickOutCDRD, 60000))) {
                finish();
                dispose();
            }

            boolean allCharactersDead = true;
            for (Zone zone : zones) {
                for (Mob mob : zone.mobs) {
                    if (!mob.isDie()) {
                        allCharactersDead = false;
                        break;
                    }
                }
            }
            if (allCharactersDead) {
                allMobsDead = true;
            }

            if (!kickoutcdrd && (endCDRD || Util.canDoWithTime(lastTimeOpen, TIME_CON_DUONG_RAN_DOC - 60000))) {
                kickoutcdrd = true;
                timeKickOutCDRD = System.currentTimeMillis();
            }
            if (kickoutcdrd && Util.canDoWithTime(lastTimeUpdateMessage, 10000)) {
                lastTimeUpdateMessage = System.currentTimeMillis();
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Trận chiến với người Xayda sẽ kết thúc sau " + TimeUtil.getTimeLeft(timeKickOutCDRD, 60) + " nữa");
                    }

                }
            }

        }
    }

    public void openConDuongRanDoc(Player plOpen, Clan clan, byte level) {
        try {
            this.level = level;
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.lastTimeOpenConDuongRanDoc = this.lastTimeOpen;
            this.clan.playerOpenConDuongRanDoc = plOpen;
            this.clan.ConDuongRanDoc = this;
            this.isOpened = true;
            this.init();
            sendTextConDuongRanDoc();
        } catch (Exception e) {
            plOpen.clan.lastTimeOpenConDuongRanDoc = 0;
            this.dispose();
        }
    }

    private void init() {
        //Hồi sinh quái
        for (Zone zone : this.zones) {
            List<Mob> mobs = zone.mobs;
            for (int i = 0; i < mobs.size(); i++) {
                Mob mob = mobs.get(i);
                if (i == 5) {
                    mob.lvMob = 1;
                    mob.point.dame = (int) level * 100 * mob.tempId * 12;
                    mob.point.maxHp = (int) level * 1000 * mob.tempId * 12;
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                } else {
                    mob.lvMob = 0;
                    mob.point.dame = (int) level * 100 * mob.tempId;
                    mob.point.maxHp = (int) level * 1000 * mob.tempId;
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            }

            // Trong SnakeWay.java - hàm init()
            if (zone.map.mapId == 144) {
                try {
                    long baseDamage = 5000 * level;
                    long baseHP = 1000000 * level; // 1 triệu mỗi level

                    // SAIBAMEN (6 con) - HP giảm 20 lần
                    for (int i = 6; i > 0; i--) {
                        long saibamenDamage = Math.min(baseDamage, 200000000L);
                        long saibamenHP = Math.min(baseHP / 20, 2000000000L); // Giảm 20 lần

                        bosses.add(new SAIBAMEN(
                                zone,
                                clan,
                                i,
                                (int) saibamenDamage,
                                (int) saibamenHP
                        ));
                    }

                    // NADIC - HP giảm 3 lần so với base x2
                    long nadicDamage = Math.min((long)(baseDamage * 1.5), 200000000L);
                    long nadicHP = Math.min((long)(baseHP * 0.67), 2000000000L);

                    bosses.add(new NADIC(
                            zone,
                            clan,
                            (int) nadicDamage,
                            (int) nadicHP
                    ));

                    // CADICH - Boss chính
                    long cadichDamage = Math.min(baseDamage * 2, 200000000L);
                    long cadichHP;

                    if (level <= 50) {
                        cadichHP = 5000000 + (level - 1) * 1122449L;
                    } else {
                        cadichHP = 60000000 + (long)((level - 50) * (level - 50) * 538888);
                    }

                    cadichHP = Math.min(cadichHP, 2000000000L);

                    bosses.add(new CADICH(
                            zone,
                            clan,
                            level,
                            (int) cadichDamage,
                            (int) cadichHP
                    ));

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    //kết thúc con đường rắn độc
    public void finish() {
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    if (pl != null) {
                        pl.joinCDRD = false;
                        kickOutOfCDRD(pl);
                    }
                }
            }
        }
        if (this.clan != null) {
            for (Player pl : this.clan.membersInGame) {
                if (pl != null) {
                    pl.joinCDRD = false;
                }
            }
        }
    }

    private void kickOutOfCDRD(Player player) {
        if (player != null && player.zone != null && MapService.gI().isMapConDuongRanDoc(player.zone.map.mapId)) {
            if (player.isDie()) {
                player.nPoint.hp = player.nPoint.hpMax;
                player.nPoint.mp = player.nPoint.mpMax;
                Service.gI().Send_Info_NV(player);
                PlayerService.gI().sendInfoHpMp(player);
            }
            ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1038);
        }
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void sendTextConDuongRanDoc() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextConDuongRanDoc(pl);
        }
    }

    private void removeTextConDuongRanDoc() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextConDuongRanDoc(pl);
        }
    }

    public long getNumBossAlive() {
        return bosses.stream().filter(boss -> boss.bossStatus != BossStatus.REST).count();
    }

    public void dispose() {
        try {
            // remove bosses
            for (Boss boss : bosses) {
                if (boss != null && !boss.isDie()) {
                    boss.leaveMap();
                }
            }
            for (Zone zone : zones) {
                synchronized (zone.items) {
                    for (int i = zone.items.size() - 1; i >= 0; i--) {
                        if (i < zone.items.size()) {
                            ItemMapService.gI().removeItemMap(zone.items.get(i));
                        }
                    }
                }
            }
            this.removeTextConDuongRanDoc();
        } catch (Exception e) {
            Logger.logException(SnakeWay.class, e, "Lỗi dispose CDRD");
        } finally {
            this.bosses.clear();
            this.allMobsDead = false;
            this.endCDRD = false;
            this.isOpened = false;
            if (this.clan != null) {
                for (Player pl : this.clan.membersInGame) {
                    if (pl != null) {
                        pl.joinCDRD = false;
                    }
                }
                this.clan.ConDuongRanDoc = null;
            }
            this.clan = null;
            this.kickoutcdrd = false;
        }
    }
}

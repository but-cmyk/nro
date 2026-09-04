package models.phoban;
import utils.Functions;
import models.boss.Boss;
import models.boss.boss_list.TreasureUnderSea.TrungUyXanhLo;
import models.clan.Clan;
import models.map.TrapMap;
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
import server.Maintenance;
import services.map.ItemMapService;
import utils.Logger;
import utils.TimeUtil;
import services.player.PlayerService;

public class TreasureUnderSea {

    public static final long POWER_CAN_GO_TO_DBKB = 2000000000;
    public static final int AVAILABLE = 5;
    public static final int TIME_BAN_DO_KHO_BAU = 1800000;

    public int id;
    public byte level;
    public final List<Zone> zones;
    public Clan clan;

    public List<Zone> getZones() { return this.zones; }
    public Clan getClan() { return this.clan; }
    public void setClan(Clan clan) { this.clan = clan; }
    public int getId() { return this.id; }
    public boolean isOpened;
    private long lastTimeOpen;
    private boolean kickoutbdkb;
    private long timeKickOutBDKB;
    private Boss boss;
    private long lastTimeSendNotify;
    private boolean allCharactersDead;
    private boolean isRecordUpdated = false;

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public TreasureUnderSea(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }



    public void update() {
        if (isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_BAN_DO_KHO_BAU) || (kickoutbdkb && Util.canDoWithTime(timeKickOutBDKB, 60000))) {
                finish();
                dispose();
            }

            allCharactersDead = true;
            for (Zone zone : zones) {

                if (zone.map.mapId == 135) {
                    for (Player pl : zone.getNotBosses()) {
                        if (pl != null) {
                            TrapMap trap = zone.isInTrap(pl);
                            if (trap != null) {
                                trap.doPlayer(pl);
                            }
                        }
                    }
                }

                for (Mob mob : zone.mobs) {
                    if (!mob.isDie()) {
                        allCharactersDead = false;
                        break;
                    }
                }

                if (allCharactersDead) {
                    for (Player cBoss : zone.getBosses()) {
                        if (!cBoss.isDie()) {
                            allCharactersDead = false;
                            break;
                        }
                    }
                }
            }

            if (!kickoutbdkb && (allCharactersDead || Util.canDoWithTime(lastTimeOpen, TIME_BAN_DO_KHO_BAU - 60000))) {
                kickoutbdkb = true;
                timeKickOutBDKB = System.currentTimeMillis();
                if (allCharactersDead && !isRecordUpdated && this.clan != null) {
                    isRecordUpdated = true;
                    long timeTaken = System.currentTimeMillis() - lastTimeOpen; // Thời gian đã tốn

                    // Nếu Level cao hơn kỷ lục cũ HOẶC (Level bằng kỷ lục cũ NHƯNG thời gian ngắn hơn)
                    if (this.level > this.clan.bdkb_level ||
                            (this.level == this.clan.bdkb_level && (this.clan.bdkb_time == 0 || timeTaken < this.clan.bdkb_time))) {

                        this.clan.bdkb_level = this.level;
                        this.clan.bdkb_time = timeTaken;
                        this.clan.update(); // Lưu vào Database

                        // Thông báo cho người chơi trong map biết đã phá kỷ lục
                        for (Zone zone : zones) {
                            for (Player pl : zone.getPlayers()) {
                                services.Service.gI().sendThongBao(pl, "Bang hội đã thiết lập kỷ lục mới!\nLevel: " + this.level + " - Thời gian: " + utils.Util.getFormatTime(timeTaken));
                            }
                        }
                    }
                }
            }

            if (kickoutbdkb && Util.canDoWithTime(lastTimeSendNotify, 10000)) {
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Cái hang này sắp sập rồi, chúng ta phải rời khỏi đây ngay " + TimeUtil.getTimeLeft(timeKickOutBDKB, 60) + " nữa");
                    }
                    lastTimeSendNotify = System.currentTimeMillis();
                }
            }

        }
    }

    public void openBanDoKhoBau(Player plOpen, Clan clan, byte level) {
        try {
            this.level = level;
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.lastTimeOpenBanDoKhoBau = this.lastTimeOpen;
            this.clan.playerOpenBanDoKhoBau = plOpen;
            this.clan.BanDoKhoBau = this;
            this.kickoutbdkb = false;
            this.isOpened = true;
            this.allCharactersDead = false;
            this.init();
            ChangeMapService.gI().goToDBKB(plOpen);
            sendTextBanDoKhoBau();
        } catch (Exception e) {
            plOpen.clan.lastTimeOpenBanDoKhoBau = 0;
            this.dispose();
        }
    }

    private void init() {
        //Hồi sinh quái
        for (Zone zone : this.zones) {
            for (TrapMap trap : zone.trapMaps) {
                trap.dame = this.level * 100000;
            }

            if (zone.map.mapId == 135 || zone.map.mapId == 136 || zone.map.mapId == 137) {
                List<Mob> mobs = zone.mobs;
                for (int i = 0; i < mobs.size(); i++) {
                    Mob mob = mobs.get(i);
                    if (((i == 5 || i == 10) && zone.map.mapId == 135) || (i == 5 && zone.map.mapId == 136) || (i == 5 && zone.map.mapId == 137)) {
                        mob.lvMob = 1;
                        mob.point.dame = (int) Math.min((long) level * 600 * mob.tempId * 10, 2_000_000_000);
                        mob.point.maxHp = (int) Math.min((long) level * 4697* mob.tempId , 2_000_000_000);
                        mob.hoiSinh();
                        mob.hoiSinhMobPhoBan();
                    } else {
                        mob.lvMob = 0;
                        mob.point.dame = (int) Math.min((long) level *  200 * mob.tempId, 2_000_000_000);
                        mob.point.maxHp = (int) Math.min((long) level * 4697 * mob.tempId, 2_000_000_000);
                        mob.hoiSinh();
                        mob.hoiSinhMobPhoBan();
                    }
                }
            } else {
                for (Mob mob : zone.mobs) {
                    mob.point.dame = (int) Math.min((long) level * 31 * 50 * mob.tempId, 2_000_000_000);
                    mob.point.maxHp = (int) Math.min((long) level * 3107 * 50 * mob.tempId, 2_000_000_000);
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            }
// hết
            // Trong TreasureUnderSea.java - hàm init()
            if (zone.map.mapId == 137) {
                try {
                    // [SỬA] Công thức mới: HP tăng dần theo level
                    long bossDamage = (200000 * level);
                    long bossMaxHealth = (5000000 * level); // 5 triệu mỗi level thay vì 200 triệu

                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);

                    boss = new TrungUyXanhLo(
                            zone,
                            level,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    );
                } catch (Exception exception) {
                }

            }
        }
    }

    //kết thúc bản đồ kho báu
    public void finish() {
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfBDKB(pl);
                }
            }

        }
    }

    private void kickOutOfBDKB(Player player) {
        if (player != null && player.zone != null && MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId)) {
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

    private void sendTextBanDoKhoBau() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextBanDoKhoBau(pl);
        }
    }

    private void removeTextBanDoKhoBau() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextBanDoKhoBau(pl);
        }
    }

    public void dispose() {
        try {
            if (boss != null) {
                this.boss.leaveMap();
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
            this.removeTextBanDoKhoBau();
        } catch (Exception e) {
            Logger.logException(TreasureUnderSea.class, e, "Lỗi dispose BDKB");
        } finally {
            this.allCharactersDead = false;
            this.boss = null;
            this.isOpened = false;
            if (this.clan != null) {
                this.clan.BanDoKhoBau = null;
            }
            this.clan = null;
            this.kickoutbdkb = false;
        }
    }
}

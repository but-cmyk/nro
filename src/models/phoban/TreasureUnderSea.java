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
    private long lastTimeUpdateHPDame;

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

            if (Util.canDoWithTime(lastTimeUpdateHPDame, 15000)) {
                this.updateHPDame();
                this.lastTimeUpdateHPDame = System.currentTimeMillis();
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

    public List<Player> getPlayersInBDKB() {
        List<Player> list = new ArrayList<>();
        for (Zone zone : this.zones) {
            for (Player pl : zone.getPlayers()) {
                if (pl != null && !pl.isBoss && !pl.isPet && !pl.isNewPet) {
                    list.add(pl);
                }
            }
        }
        return list;
    }

    public void updateHPDame() {
        List<Player> players = getPlayersInBDKB();
        if (players.isEmpty() && this.clan != null && this.clan.playerOpenBanDoKhoBau != null) {
            players = new ArrayList<>();
            players.add(this.clan.playerOpenBanDoKhoBau);
        }

        long totalDame = 0;
        long totalHp = 0;
        for (Player pl : players) {
            if (pl != null && pl.nPoint != null) {
                totalDame += pl.nPoint.dame;
                totalHp += pl.nPoint.hpMax;
            }
        }

        totalDame = Math.max(totalDame, 10_000L);
        totalHp = Math.max(totalHp, 100_000L);

        // Cập nhật sát thương bẫy (độ khó cao hơn Doanh trại)
        for (Zone zone : this.zones) {
            for (TrapMap trap : zone.trapMaps) {
                trap.dame = Math.max(1000L, Math.min(totalHp / 25, 200_000L));
            }
        }

        // Cập nhật HP, Sức đánh quái
        for (Zone zone : this.zones) {
            for (Mob mob : zone.mobs) {
                if (mob.isDie()) {
                    continue;
                }
                boolean isBigMob = (mob.lvMob == 1 || mob.tempId == 71 || mob.tempId == 72);
                int newDame;
                int newMaxHp;

                if (isBigMob) {
                    // Quái lớn / Thủ lĩnh: Mạnh gấp ~5 lần Doanh trại
                    // Dame gấp 4.3 lần DT (totalHp / 7 so với totalHp / 30)
                    // HP gấp 6 lần DT (totalDame * 150 so với totalDame * 25)
                    newDame = (int) Math.max(1500, Math.min(totalHp / 7, 2_000_000_000L));
                    newMaxHp = (int) Math.max(150_000, Math.min(totalDame * 150, 2_000_000_000L));
                } else {
                    // Quái nhỏ: Mạnh gấp 3 lần Doanh trại
                    // Dame gấp 3 lần DT (totalHp / 10 so với totalHp / 30)
                    // HP gấp 3 lần DT (totalDame * 75 so với totalDame * 25)
                    newDame = (int) Math.max(500, Math.min(totalHp / 10, 2_000_000_000L));
                    newMaxHp = (int) Math.max(30_000, Math.min(totalDame * 75, 2_000_000_000L));
                }

                mob.point.dame = newDame;
                if (mob.point.hp >= mob.point.maxHp) {
                    mob.point.hp = newMaxHp;
                } else if (mob.point.maxHp > 0) {
                    mob.point.hp = (int) Math.max(1, (long) mob.point.hp * newMaxHp / mob.point.maxHp);
                }
                mob.point.maxHp = newMaxHp;
                mob.setTiemNang();
            }
        }

        // Cập nhật HP, Sức đánh Boss Trung Úy Xanh Lơ: Mạnh gấp 4 - 5 lần Boss Doanh trại
        // Dame gấp 4 lần Boss DT (totalHp / 5 so với totalHp / 20)
        // HP gấp 5 lần Boss DT (totalDame * 250 so với totalDame * 50)
        if (this.boss != null && !this.boss.isDie() && this.boss.zone != null && this.boss.nPoint != null) {
            long bossDamage = Math.max(5000, Math.min(totalHp / 5, 200_000_000L));
            long bossMaxHealth = Math.max(500_000, Math.min(totalDame * 250, 2_000_000_000L));

            try {
                if (this.boss.nPoint.hp >= this.boss.nPoint.hpMax) {
                    this.boss.nPoint.hp = (int) bossMaxHealth;
                } else if (this.boss.nPoint.hpMax > 0) {
                    this.boss.nPoint.hp = (int) Math.max(1, (long) this.boss.nPoint.hp * bossMaxHealth / this.boss.nPoint.hpMax);
                }
                this.boss.nPoint.hpMax = (int) bossMaxHealth;
                this.boss.nPoint.dame = (int) bossDamage;
            } catch (Exception e) {
            }
        }
    }

    private void init() {
        // Hồi sinh và phân loại quái
        for (Zone zone : this.zones) {
            if (zone.map.mapId == 135) {
                List<Mob> mobs = zone.mobs;
                for (int i = 0; i < mobs.size(); i++) {
                    Mob mob = mobs.get(i);
                    mob.lvMob = (i == 5 || i == 10) ? 1 : 0;
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            } else if (zone.map.mapId == 136) {
                List<Mob> mobs = zone.mobs;
                for (int i = 0; i < mobs.size(); i++) {
                    Mob mob = mobs.get(i);
                    mob.lvMob = (i == 5 || mob.tempId == 71) ? 1 : 0;
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            } else if (zone.map.mapId == 137) {
                List<Mob> mobs = zone.mobs;
                for (int i = 0; i < mobs.size(); i++) {
                    Mob mob = mobs.get(i);
                    mob.lvMob = (i == 5) ? 1 : 0;
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
                try {
                    boss = new TrungUyXanhLo(
                            zone,
                            level,
                            1000,
                            100000
                    );
                } catch (Exception exception) {
                }
            } else {
                for (Mob mob : zone.mobs) {
                    mob.lvMob = (mob.tempId == 72) ? 1 : 0;
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            }
        }
        // Tính toán HP và Dame theo thành viên tham gia
        this.updateHPDame();
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

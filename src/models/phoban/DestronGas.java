package models.phoban;

import utils.Functions;
import models.boss.Boss;
import models.boss.boss_list.DestronGas.DrLychee;
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
import java.util.Date;
import java.util.List;
import server.Maintenance;
import services.map.ItemMapService;
import utils.Logger;
import utils.TimeUtil;
import services.player.PlayerService;

public class DestronGas {

//    public static final long POWER_CAN_GO_TO_KHI_GAS_HUY_DIET = 2000000000;
    public static final int AVAILABLE = 5;
    public static final int TIME_KHI_GAS_HUY_DIET = 1800000;
    public static final int N_PLAYER_CLAN = 0;

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
    private boolean kickoutkghd;
    private long timeKickOutKGHD;
    public List<Boss> bosses = new ArrayList<>();
    private boolean callBoss;
    public boolean hatchiyatchDead;

    public DestronGas(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }



    public void update() {
        if (isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_KHI_GAS_HUY_DIET) || (kickoutkghd && Util.canDoWithTime(timeKickOutKGHD, 60000))) {
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

            if (allCharactersDead && !callBoss) {
                try {
                    long bossDamage = (10000 * level);
                    long bossMaxHealth = (15000000 * level);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    bosses.add(new DrLychee(
                            getMapById(148),
                            clan,
                            level,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                    callBoss = true;
                } catch (Exception exception) {
                }
            }

            if (!kickoutkghd && (hatchiyatchDead || Util.canDoWithTime(lastTimeOpen, TIME_KHI_GAS_HUY_DIET - 60000))) {
                kickoutkghd = true;
                timeKickOutKGHD = System.currentTimeMillis();
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Nơi này sắp nổ tung mau chạy đi");
                    }

                }
            }
            if (kickoutkghd && Util.canDoWithTime(lastTimeUpdateMessage, 10000)) {
                lastTimeUpdateMessage = System.currentTimeMillis();
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Về làng Aru sau " + TimeUtil.getTimeLeft(timeKickOutKGHD, 60) + " nữa");
                    }
                }
            }

        }
    }

    public void openKhiGasHuyDiet(Player plOpen, Clan clan, byte level) {
        try {
            this.level = level;
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.timesPerDayKGHD = 1;
            this.clan.lastTimeOpenKhiGasHuyDiet = this.lastTimeOpen;
            this.clan.playerOpenKhiGasHuyDiet = plOpen;
            this.clan.KhiGasHuyDiet = this;
            this.callBoss = false;
            this.isOpened = true;
            this.init();
            sendTextKhiGasHuyDiet();
        } catch (Exception e) {
            plOpen.clan.lastTimeOpenKhiGasHuyDiet = 0;
            this.dispose();
        }
    }

    private void init() {
        //Hồi sinh quái
        for (Zone zone : this.zones) {
            List<Mob> mobs = zone.mobs;
            for (int i = 0; i < mobs.size(); i++) {
                Mob mob = mobs.get(i);
                if (((i == 5 || i == 10) && zone.map.mapId == 149) || ((i == 5 || i == 10 || i == 15)
                        && zone.map.mapId == 147) || ((i == 5 || i == 10 || i == 15 || i == 20 || i == 25)
                        && zone.map.mapId == 152) || (i == 5 && zone.map.mapId == 151)
                        || ((i == 5 || i == 10) && zone.map.mapId == 148)) {
                    mob.lvMob = 1;
                    mob.point.dame = (int) Math.min((long) level * 31 * 5 * mob.tempId * 10, 2_000_000_000);
                    mob.point.maxHp = (int) Math.min((long) level * 3107 * 5 * mob.tempId * 10, 2_000_000_000);
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                } else {
                    mob.lvMob = mob.tempId == 76 ? 1 : 0;
                    mob.point.dame = (int) Math.min((long) level * 31 * 5 * mob.tempId, 2_000_000_000);
                    mob.point.maxHp = (int) Math.min((long) level * 3107 * 5 * mob.tempId, 2_000_000_000);
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            }
        }
    }

    //kết thúc khí gas hủy diệt
    public void finish() {
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfKGHD(pl);
                }
            }

        }
    }

    private void kickOutOfKGHD(Player player) {
        if (player != null && player.zone != null && MapService.gI().isMapKhiGasHuyDiet(player.zone.map.mapId)) {
            if (player.isDie()) {
                player.nPoint.hp = player.nPoint.hpMax;
                player.nPoint.mp = player.nPoint.mpMax;
                Service.gI().Send_Info_NV(player);
                PlayerService.gI().sendInfoHpMp(player);
            }
            ChangeMapService.gI().changeMapBySpaceShip(player, 0, -1, -1);
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

    private void sendTextKhiGasHuyDiet() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextKhiGasHuyDiet(pl);
        }
    }

    private void removeTextKhiGasHuyDiet() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextKhiGasHuyDiet(pl);
        }
    }

    public void dispose() {
        try {
            for (Zone zone : zones) {
                synchronized (zone.items) {
                    for (int i = zone.items.size() - 1; i >= 0; i--) {
                        if (i < zone.items.size()) {
                            ItemMapService.gI().removeItemMap(zone.items.get(i));
                        }
                    }
                }
            }
            for (Boss boss : bosses) {
                if (boss != null && !boss.isDie()) {
                    boss.leaveMap();
                }
            }
            this.removeTextKhiGasHuyDiet();
        } catch (Exception e) {
            Logger.logException(DestronGas.class, e, "Lỗi dispose Khi Gas");
        } finally {
            this.bosses.clear();
            this.isOpened = false;
            if (this.clan != null) {
                this.clan.KhiGasHuyDiet = null;
            }
            this.clan = null;
            this.kickoutkghd = false;
            this.hatchiyatchDead = false;
        }
    }
}

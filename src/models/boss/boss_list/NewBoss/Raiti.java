package models.boss.boss_list.NewBoss;

import consts.BossStatus;
import models.boss.Boss;
import models.boss.BossesData;
import models.map.Zone;
import models.player.Player;
import services.map.ChangeMapService;
import utils.Util;

public class Raiti extends Boss {

    private boolean isEnraged; // Cờ trạng thái nổi giận
    private long lastTimeJoinMap;
    private static final long TIME_CHANGE_MAP = 300000; // 5 phút đổi map tìm player
    private long lastTimeTargetPlayer;
    private int timeTargetPlayer;

    public Raiti() throws Exception {
        super(-Util.nextInt(1000, 1000000), true, false, BossesData.RAITI);
        this.isEnraged = false;
        this.lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
    }

    @Override
    public Zone getMapJoin() {
        Zone target = managers.boss.BossManager.getRoamingZoneWithPlayer(this.zone, Raiti.class);
        if (target != null) {
            return target;
        }
        return managers.boss.BossManager.getRandomOutdoorZone();
    }

    @Override
    public void joinMap() {
        if (this.zone == null) {
            this.zone = getMapJoin();
        }
        if (this.zone != null) {
            int x = Util.nextInt(100, Math.max(100, this.zone.map.mapWidth - 100));
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
            this.changeStatus(BossStatus.CHAT_S);
            this.lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
            this.lastTimeHadPlayer = System.currentTimeMillis();
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) {
            return;
        }

        // Đổi map tìm đến người chơi
        if (this.zone != null && (System.currentTimeMillis() > this.lastTimeJoinMap 
                || (this.zone.getNumOfPlayers() == 0 && Util.canDoWithTime(this.lastTimeJoinMap - TIME_CHANGE_MAP, 25000)))) {
            Zone targetZone = managers.boss.BossManager.getRoamingZoneWithPlayer(this.zone, Raiti.class);
            if (targetZone == null && this.zone.getNumOfPlayers() == 0) {
                targetZone = managers.boss.BossManager.getRandomOutdoorZone();
            }
            if (targetZone != null && targetZone != this.zone) {
                this.lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
                this.chat("|-1|Ở đây chán quá, ta đi tìm đối thủ mạnh hơn!");
                ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
                ChangeMapService.gI().exitMap(this);
                this.zone = targetZone;
                this.location.x = Util.nextInt(100, Math.max(100, zone.map.mapWidth - 100));
                this.location.y = zone.map.yPhysicInTop(this.location.x, 100);
                this.joinMap();
            }
        }

        // Tìm mục tiêu tấn công người chơi trong khu
        if (this.zone != null) {
            if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
                this.playerTarger = this.zone.getRandomPlayerInMap();
                this.lastTimeTargetPlayer = System.currentTimeMillis();
                this.timeTargetPlayer = Util.nextInt(3000, 6000);
            }
        }

        // Kiểm tra điều kiện nổi giận
        if (!isEnraged && this.nPoint.hp < this.nPoint.hpMax / 2) {
            enrage();
        }
    }

    // Hàm kích hoạt trạng thái nổi giận
    private void enrage() {
        this.isEnraged = true;
        this.chat("Các ngươi đã chọc giận ta rồi! HAAAAA!");
        // Tăng sức mạnh, ví dụ tăng 30% sát thương
        this.nPoint.dameg *= 1.3;
        // Tăng tốc độ tấn công (giảm thời gian nghỉ)
        this.data[this.currentLevel].setSecondsRest(1);
    }

    @Override
    protected void notifyJoinMap() {
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            // Khi nổi giận, giảm 20% sát thương nhận vào
            if (isEnraged) {
                damage *= 0.8;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (damage <= 0) {
                damage = 1;
            }

            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        }
        return 0;
    }

    @Override
    public void reward(Player plKill) {
        managers.boss.BossManager.dropGoldAndSPL(this, plKill);
    }
}

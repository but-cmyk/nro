package models.boss.boss_list.MajinBuu14H;

import models.boss.Boss;
import managers.boss.FinalBossManager;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import static consts.BossType.FINAL;
import java.util.ArrayList;
import java.util.List;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;
import server.ServerNotify;
import services.SkillService;
import services.TaskService;
import services.map.ChangeMapService;
import utils.SkillUtil;

public class SuperBu extends Boss {

    private long lastTimeUseSkill;
    private long timeUseSkill;

    public SuperBu() throws Exception {
        super(FINAL, BossID.SUPERBU, BossesData.SUPER_BU_BUNG);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            this.zone = zoneFinal;
        }
        ChangeMapService.gI().changeMap(this, this.zone, -1, -1);
        this.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100)) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                if (Util.canDoWithTime(lastTimeUseSkill, timeUseSkill)) {
                    Service.gI().sendMabuAttackSkill(this);
                    lastTimeUseSkill = System.currentTimeMillis();
                    timeUseSkill = Util.nextInt(5000, 10000);
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

                // Logic di chuyển
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        int rangeX = SkillUtil.isUseSkillChuong(this) ? Util.nextInt(20, 200) : Util.nextInt(10, 40);
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * rangeX), pl.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        // Truyền sát thương sang Mabu (Boss chính ở map ngoài)
        Boss boss = FinalBossManager.gI().getBossById(BossID.MABU, 127, this.zone.zoneId);
        if (boss != null) {
            boss.injured(plAtt, damage, piercing, isMobAttack);
        }

        if (!this.isDie()) {
            if (!piercing && Util.isTrue(10, 100)) {
                this.chat("Xí hụt");
                return 0;
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            if (damage >= 30000000) {
                damage = 30000000 + Util.nextInt(-10000, 10000);
            }

            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }

            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            // Khi SuperBu (trong bụng) chết -> Đẩy người chơi ra ngoài
            Boss boss = FinalBossManager.gI().getBossById(BossID.MABU, 127, this.zone.zoneId);
            if (boss != null && boss instanceof Mabu2H) {
                Mabu2H mabu = (Mabu2H) boss;
                if(!mabu.maBuEat.isEmpty()){
                    List<Player> players = new ArrayList<>(mabu.maBuEat);
                    for (Player pl : players) {
                        if (pl != null && pl.zone != null && pl.zone.map.mapId == 128) {
                            ChangeMapService.gI().changeMap(pl, 127, this.zone.zoneId, -1, 312);
                        }
                    }
                    mabu.maBuEat.clear();
                }
            }
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }
}
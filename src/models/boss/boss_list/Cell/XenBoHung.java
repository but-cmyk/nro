package models.boss.boss_list.Cell;

import consts.ConstPlayer;
import models.boss.Boss;
import models.boss.BossesData;
import consts.BossID;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.EffectSkillService;
import services.player.PlayerService;
import services.Service;
import services.TaskService;
import utils.Util;
import services.map.ChangeMapService;

public class XenBoHung extends Boss {

    private long lastTimeHapThu;
    private int timeHapThu;

    public XenBoHung() throws Exception {
        super(BossID.XEN_BO_HUNG, BossesData.XEN_BO_HUNG_1, BossesData.XEN_BO_HUNG_2, BossesData.XEN_BO_HUNG_3);
    }

    @Override
    public void reward(Player plKill) {
        plKill.effect.addPointTrumSanBoss();
        // Chỉ hoàn thành nhiệm vụ khi hạ gục ở form cuối cùng
        if (this.currentLevel == this.data.length - 1) {
            TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        }
        if (Util.isTrue(5, 100)) {
            ItemMap it = new ItemMap(this.zone, 17, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
         if (Util.isTrue(10, 100)) {
            ItemMap it = new ItemMap(this.zone, 18, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (plKill.playerTask.taskdh.Hagucboss < 30) {
            int required = 30;
            int percentDone = (int) ((double) plKill.playerTask.taskdh.Hagucboss / required * 100);
            plKill.playerTask.taskdh.Hagucboss++;
            plKill.playerTask.taskdh.ResetTime = System.currentTimeMillis();
            //Service.gI().sendThongBao(plKill, "Tiến độ hiện tại: " + percentDone + "%");
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.hapThu();
        this.attack();
    }

    private void hapThu() {
        if (!Util.canDoWithTime(this.lastTimeHapThu, this.timeHapThu) || !Util.isTrue(1, 100)) {
            return;
        }

        Player pl = this.zone.getRandomPlayerInMap();
        if (pl == null || pl.isDie()) {
            return;
        }
        ChangeMapService.gI().changeMapYardrat(this, this.zone, pl.location.x, pl.location.y);
        long baseDame = this.data[this.currentLevel].getDame();
        long maxDameAllowed = baseDame * 2;
        if (this.nPoint.dameg < maxDameAllowed) {
            this.nPoint.dameg += (pl.nPoint.dame * 2 / 100);
            if (this.nPoint.dameg > maxDameAllowed) {
                this.nPoint.dameg = (int) maxDameAllowed;
            }
        }
        long baseHp = this.data[this.currentLevel].getHp()[0];
        long maxHpAllowed = baseHp * 2;
        if (this.nPoint.hpg < maxHpAllowed) {
            this.nPoint.hpg += (pl.nPoint.hp * 1 / 100);
            if (this.nPoint.hpg > maxHpAllowed) {
                this.nPoint.hpg = (int) maxHpAllowed;
            }
        }
        if (this.nPoint.critg < 20) {
            this.nPoint.critg++;
        }
        this.nPoint.calPoint();
        PlayerService.gI().hoiPhuc(this, pl.nPoint.hp, 0);
        pl.injured(null, pl.nPoint.hpMax, true, false);
        Service.gI().sendThongBao(pl, "Bạn vừa bị " + this.name + " hấp thu!");
        this.chat(2, "Ui cha cha, kinh dị quá. " + pl.name + " vừa bị tên " + this.name + " nuốt chửng kìa!!!");
        this.chat("Haha, ngọt lắm đấy " + pl.name + "..");
        this.lastTimeHapThu = System.currentTimeMillis();
        this.timeHapThu = Util.nextInt(10000, 20000);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
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

}

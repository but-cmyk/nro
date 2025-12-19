package models.boss.boss_list.Android;


import models.boss.Boss;
import consts.BossID;
import models.boss.BossesData;
import models.map.ItemMap;
import models.player.Player;
import models.skill.Skill;
import services.player.PlayerService;
import services.Service;
import services.TaskService;
import utils.Util;

public class Android19 extends Boss {

    public Android19() throws Exception {
        super(BossID.ANDROID_19, BossesData.ANDROID_19);
    }

      @Override
public void reward(Player plKill) {
    plKill.effect.addPointTrumSanBoss();

    int[] itemRan = new int[]{20, 19};
    int oldItemId = itemRan[Util.nextInt(itemRan.length)]; // Đổi tên biến để tránh nhầm lẫn
    if (Util.isTrue(15, 100)) {
        ItemMap itOld = new ItemMap(this.zone, oldItemId, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, itOld);
    }
    int newItemId = 190;
    int newQuantity = 31000;
    ItemMap itNew = new ItemMap(this.zone, newItemId, newQuantity, this.location.x,
            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
    Service.gI().dropItemMap(this.zone, itNew);

    TaskService.gI().checkDoneTaskKillBoss(plKill, this);
}

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }
    private long st;

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (plAtt != null) {
            switch (plAtt.playerSkill.skillSelect.template.id) {
                case Skill.KAMEJOKO:
                case Skill.MASENKO:
                case Skill.ANTOMIC:
                    int hpHoi = (int) ((long) damage * 80 / 100);
                    PlayerService.gI().hoiPhuc(this, hpHoi, 0);
                    if (Util.isTrue(1, 5)) {
                        this.chat("Hấp thụ.. các ngươi nghĩ sao vậy?");
                    }
                    return 0;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
        if (this.parentBoss != null) {
            this.parentBoss.changeToTypePK();
        }
    }

}

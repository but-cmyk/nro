package models.boss.boss_list.Android;
import models.boss.Boss;
import consts.BossID;
import models.boss.BossesData;
import models.map.ItemMap;
import models.player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class Android13 extends Boss {

    public Android13() throws Exception {
        super(BossID.ANDROID_13, BossesData.ANDROID_13);
    }

   @Override
   public void reward(Player plKill) {
    plKill.effect.addPointTrumSanBoss();

    int[] itemRan = new int[]{20, 19};
    int oldItemId = itemRan[Util.nextInt(itemRan.length)];
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
    public void doneChatS() {
        if (this.parentBoss == null) {
            return;
        }
        if (this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.ANDROID_15 && !boss.isDie()) {
                boss.changeToTypePK();
                break;
            }
        }
        this.parentBoss.changeToTypePK();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (damage >= this.nPoint.hp) {
            boolean flag = true;
            if (this.parentBoss != null) {
                if (this.parentBoss.bossAppearTogether != null && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                    for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
                        if (boss.id == BossID.ANDROID_15 && !boss.isDie()) {
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag && !this.parentBoss.isDie()) {
                    flag = false;
                }
            }
            if (!flag) {
                return 0;
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }
}

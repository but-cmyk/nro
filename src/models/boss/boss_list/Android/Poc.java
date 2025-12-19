package models.boss.boss_list.Android;
import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import models.map.ItemMap;
import models.player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class Poc extends Boss {

    public Poc() throws Exception {
        super(BossID.POC, BossesData.POC);
    }

     @Override
public void reward(Player plKill) {
    plKill.effect.addPointTrumSanBoss();
    int[] itemRan = new int[]{380, 381, 382, 383, 384};
    int itemId = itemRan[Util.nextInt(itemRan.length)];
    if (Util.isTrue(70, 100)) {
        ItemMap it = new ItemMap(this.zone, itemId, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, it);
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
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.PIC && !boss.isDie()) {
                boss.changeStatus(BossStatus.ACTIVE);
                break;
            }
        }
    }
}

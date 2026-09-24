package models.boss.boss_list.Frieza;
import models.boss.Boss;
import consts.BossID;
import models.boss.BossesData;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class Fide extends Boss {

    private long st;

    public Fide() throws Exception {
        super(BossID.FIDE, BossesData.FIDE_DAI_CA_1, BossesData.FIDE_DAI_CA_2, BossesData.FIDE_DAI_CA_3);
    }

    @Override
    public void reward(Player plKill) {
        if (plKill == null) {
            return;
        }
        if (this.currentLevel == this.data.length - 1) {
            TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        }
        if (plKill.effect != null) {
            plKill.effect.addPointTrumSanBoss();
        }
        int rateNR = (this.currentLevel == this.data.length - 1) ? 30 : 15;
        if (Util.isTrue(rateNR, 100) && this.zone != null && this.location != null) {
            ItemMap it = new ItemMap(this.zone, 19, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (this.currentLevel == this.data.length - 1 && this.zone != null && this.location != null) {
            int gold = Util.nextInt(100_000, 200_000);
            ItemMap itGold = new ItemMap(this.zone, 190, gold, this.location.x + Util.nextInt(-15, 15), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, itGold);
        }
    }

    @Override
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
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

}

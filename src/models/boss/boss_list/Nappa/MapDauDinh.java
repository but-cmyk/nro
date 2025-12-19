package models.boss.boss_list.Nappa;

import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import models.map.ItemMap;
import models.player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class MapDauDinh extends Boss {

    private long lastTimeUpdate;

    public MapDauDinh() throws Exception {
        super(BossID.MAP_DAU_DINH, true, true, BossesData.MAP_DAU_DINH);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        lastTimeUpdate = System.currentTimeMillis();
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        ItemMap it = new ItemMap(this.zone, 190, 31000,
                this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id);

        Service.gI().dropItemMap(this.zone, it);
    }

    @Override
    public void autoLeaveMap() {
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            lastTimeUpdate = System.currentTimeMillis();
            return;
        }
        if (Util.canDoWithTime(lastTimeUpdate, 900000)) {
            this.leaveMap();
        }
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        this.changeStatus(BossStatus.LEAVE_MAP);
    }
}
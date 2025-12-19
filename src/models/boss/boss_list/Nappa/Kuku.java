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

public class Kuku extends Boss {

    private long lastTimeUpdate;

    public Kuku() throws Exception {
        // BossID.KUKU, respawn=true, isBoss=true, Data
        super(BossID.KUKU, true, true, BossesData.KUKU);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        lastTimeUpdate = System.currentTimeMillis();
    }

    @Override
    public void reward(Player plKill) {
        // Check nhiệm vụ tiêu diệt Kuku
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // Rơi 31k vàng (Item ID 190)
        int quantity = 31000;
        ItemMap it = new ItemMap(this.zone, 190, quantity,
                this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id);

        Service.gI().dropItemMap(this.zone, it);
    }

    @Override
    public void autoLeaveMap() {
        // Nếu có người chơi trong khu vực, boss sẽ không biến mất (Reset thời gian)
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            lastTimeUpdate = System.currentTimeMillis();
            return;
        }

        // Nếu không có ai trong 15 phút -> Boss rời đi
        if (Util.canDoWithTime(lastTimeUpdate, 900000)) {
            this.leaveMap();
        }
    }

    // Thêm hàm này để boss rời đi đúng quy trình hơn
    @Override
    public void leaveMap() {
        super.leaveMap();
        this.changeStatus(BossStatus.LEAVE_MAP);
    }
}
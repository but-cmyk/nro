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

        // 20% rơi Đồ Sao hoặc Đá Nâng Cấp
        if (Util.isTrue(20, 100)) {
            if (Util.isTrue(50, 100)) {
                short itTemp = (short) services.ItemService.gI().randTempItemDoSao(plKill.gender);
                ItemMap itDoSao = new ItemMap(this.zone, itTemp, 1, this.location.x + Util.nextInt(-20, 20), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                var ops = services.ItemService.gI().getListOptionItemShop(itTemp);
                if (!ops.isEmpty()) {
                    itDoSao.options = ops;
                }
                itDoSao.options.add(new models.item.Item.ItemOption(107, Util.nextInt(1, 3)));
                Service.gI().dropItemMap(this.zone, itDoSao);
            } else {
                int randDNC = Util.nextInt(0, 4);
                ItemMap itDNC = new ItemMap(this.zone, 220 + randDNC, 1, this.location.x + Util.nextInt(-20, 20), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                itDNC.options.add(new models.item.Item.ItemOption(71 - randDNC, 0));
                Service.gI().dropItemMap(this.zone, itDNC);
            }
        }
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
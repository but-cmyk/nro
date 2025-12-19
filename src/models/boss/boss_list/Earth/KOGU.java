package models.boss.boss_list.Earth;

import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.Service;
import utils.TimeUtil;
import utils.Util;

public class KOGU extends Boss {

    private long lastTimeUpdate;

    public KOGU() throws Exception {
        super(BossID.KOGU, false, true, BossesData.KOGU);
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player pl) {
        // Drop rác
        for (int i = 0; i < 20; i++) {
            int range = (i + 1) * 15;
            int xDrop = this.location.x + (i % 2 == 0 ? range : -range);
            if (xDrop < 50) xDrop = 50;
            if (xDrop > this.zone.map.mapWidth - 50) xDrop = this.zone.map.mapWidth - 50;

            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 77, 1,
                    xDrop, this.zone.map.yPhysicInTop(xDrop, this.location.y - 24), pl.id));
        }

        // Drop đồ KOGU (ID 424)
        ItemMap id = new ItemMap(this.zone, 424, 1,
                this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                pl.id);

        int randomValue = Util.nextInt(8, 11);
        id.options.add(new Item.ItemOption(50, 11));
        id.options.add(new Item.ItemOption(94, 13));
        id.options.add(new Item.ItemOption(93, randomValue));

        Service.gI().dropItemMap(this.zone, id);
    }

    @Override
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) {
            return;
        }
        super.notifyJoinMap();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(lastTimeUpdate, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            lastTimeUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        if (!TimeUtil.timeEarth()) {
            this.leaveMap();
            return;
        }
        lastTimeUpdate = System.currentTimeMillis();
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id != this.id && !boss.isDie()) {
                return;
            }
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }
}
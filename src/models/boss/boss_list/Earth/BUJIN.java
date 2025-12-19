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

public class BUJIN extends Boss {

    private long lastTimeUpdate;

    public BUJIN() throws Exception {
        super(BossID.BUJIN, false, true, BossesData.BUJIN);
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
        // Rơi 20 item rác (ID 77)
        int itemCount = 20;
        for (int i = 0; i < itemCount; i++) {
            // Logic: Rải đều sang 2 bên trái phải
            int range = (i + 1) * 15;
            int xDrop = this.location.x + (i % 2 == 0 ? range : -range);

            // Check giới hạn map (quan trọng)
            if (xDrop < 50) xDrop = 50;
            if (xDrop > this.zone.map.mapWidth - 50) xDrop = this.zone.map.mapWidth - 50;

            int yDrop = this.zone.map.yPhysicInTop(xDrop, this.location.y - 24);

            ItemMap it = new ItemMap(this.zone, 77, 1, xDrop, yDrop, pl.id);
            Service.gI().dropItemMap(this.zone, it);
        }

        // Rơi item đặc biệt của BUJIN (ID 423)
        // Check tỉ lệ nếu cần: if (Util.isTrue(50, 100))
        ItemMap id = new ItemMap(this.zone, 423, 1,
                this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                pl.id);

        int randomValue = Util.nextInt(8, 11); // Random 8-10
        id.options.add(new Item.ItemOption(50, 11)); // sd
        id.options.add(new Item.ItemOption(94, 13)); // hp
        id.options.add(new Item.ItemOption(93, randomValue)); // hsd

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
        // Check xem đồng đội còn sống không
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            // Nếu boss đó không phải là mình VÀ boss đó chưa chết -> Return (Chưa kích hoạt cha)
            if (boss.id != this.id && !boss.isDie()) {
                return;
            }
        }
        // Nếu tất cả đã chết -> Kích hoạt Boss cha
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }
}
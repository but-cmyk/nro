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

public class BOJACK extends Boss {

    private long lastTimeUpdate;

    public BOJACK() throws Exception {
        super(BossID.BOJACK, false, true, BossesData.BOJACK, BossesData.SUPER_BOJACK);
    }

    @Override
    public void reward(Player pl) {
        // Tỉ lệ rớt đồ (Code cũ 100/100 thì không cần if, nhưng giữ lại cho chuẩn form)
        if (Util.isTrue(100, 100)) {

            // Rơi rác (Item 77)
            int itemCount = 20;
            for (int i = 0; i < itemCount; i++) {
                int range = (i + 1) * 15; // Giãn cách rộng hơn chút
                int xDrop = this.location.x + (i % 2 == 0 ? range : -range);
                if (xDrop < 50) xDrop = 50;
                if (xDrop > this.zone.map.mapWidth - 50) xDrop = this.zone.map.mapWidth - 50;

                Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 77, 1,
                        xDrop, this.zone.map.yPhysicInTop(xDrop, this.location.y - 24), pl.id));
            }

            // Rơi đồ xịn (Item 427)
            ItemMap id = new ItemMap(this.zone, 427, 1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    pl.id);

            int randomValue = Util.nextInt(8, 10);
            id.options.add(new Item.ItemOption(50, 11));
            id.options.add(new Item.ItemOption(94, 13));
            id.options.add(new Item.ItemOption(93, randomValue));

            Service.gI().dropItemMap(this.zone, id);
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
    public void autoLeaveMap() {
        if (Util.canDoWithTime(lastTimeUpdate, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            lastTimeUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void doneChatS() {
        if (this.currentLevel == 0) { // Level 0 là Bojack thường
            this.changeStatus(BossStatus.AFK); // AFK đợi đệ tử chết
        }
    }
}
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

public class BIDO extends Boss {

    private long lastTimeUpdate;

    public BIDO() throws Exception {
        super(BossID.BIDO, false, true, BossesData.BIDO);
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
        // Rơi 20 item ID 77 (Vàng/Đá?)
        int itemCount = 20;
        for (int i = 0; i < itemCount; i++) {
            // Logic rải item sang 2 bên: Chẵn bên phải, Lẻ bên trái
            int range = (i + 1) * 10;
            int xDrop = this.location.x + (i % 2 == 0 ? range : -range);

            // Check giới hạn map để không rơi ra ngoài
            if (xDrop < 50) xDrop = 50;
            if (xDrop > this.zone.map.mapWidth - 50) xDrop = this.zone.map.mapWidth - 50;

            int yDrop = this.zone.map.yPhysicInTop(xDrop, this.location.y - 24);

            ItemMap it = new ItemMap(this.zone, 77, 1, xDrop, yDrop, pl.id);
            Service.gI().dropItemMap(this.zone, it);
        }

        // Rơi item đặc biệt (ID 426 cho Bido - Hãy thay ID khác cho Boss khác)
        // Bido: 426, Bujin: 423, Kogu: 424, Zangya: 425
        int itemSpecialID = 426;

        ItemMap id = new ItemMap(this.zone, itemSpecialID, 1,
                this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                pl.id);

        int randomValue = Util.nextInt(8, 10); // Random từ 8 đến 10
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
    public void joinMap() {
        super.joinMap(); // Gọi super trước
        // Kiểm tra giờ spawn (9h-16h)
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
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        // Kiểm tra xem các boss con khác còn sống không
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id != this.id && !boss.isDie()) {
                return; // Nếu còn con nào sống thì chưa kích hoạt Boss cha
            }
        }
        // Nếu chết hết thì kích hoạt Boss cha
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }
}
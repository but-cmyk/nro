package models.boss.boss_list.NamekGinyuForce;
import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import java.util.Random;
import models.boss.BossesData;
import models.item.Item;
import models.item.Item.ItemOption;
import models.map.ItemMap;
import models.player.Player;
import services.Service;
import utils.TimeUtil;
import utils.Util;

public class TDT_NM extends Boss {

    private long st;

    public TDT_NM() throws Exception {
        super(BossID.TIEU_DOI_TRUONG_NM, false, true, BossesData.TIEU_DOI_TRUONG_NM);
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player plKill) {
        // Drop rác (Item 77 - Đá/Vàng?)
        int itemCount = 10;
        for (int i = 0; i < itemCount; i++) {
            // Logic rải đều item sang 2 bên
            int range = (i + 1) * 15;
            int xDrop = this.location.x + (i % 2 == 0 ? range : -range);

            // Check biên map
            if (xDrop < 50) xDrop = 50;
            if (xDrop > this.zone.map.mapWidth - 50) xDrop = this.zone.map.mapWidth - 50;

            int yDrop = this.zone.map.yPhysicInTop(xDrop, this.location.y - 24);

            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 77, 1, xDrop, yDrop, plKill.id));
        }

        // Drop Item Đặc biệt của Số 1 (ID 432)
        ItemMap it = new ItemMap(this.zone, 433, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);

        it.options.add(new Item.ItemOption(50, Util.nextInt(12, 15))); // Sức đánh
        it.options.add(new Item.ItemOption(94, Util.nextInt(8, 10)));  // HP
        it.options.add(new Item.ItemOption(93, Util.nextInt(3, 5)));   // Hạn sử dụng (3-5 ngày)

        Service.gI().dropItemMap(this.zone, it);
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
        // Kiểm tra chỉ cho spawn từ 9h-16h
        if (!TimeUtil.timeNamek()) {
            this.leaveMap();
            return;
        }
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
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

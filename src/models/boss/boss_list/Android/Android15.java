package models.boss.boss_list.Android;


import models.boss.Boss;
import consts.BossID;
import models.boss.BossesData;
import models.map.ItemMap;
import models.player.Player;
import services.player.PlayerService;
import services.Service;
import services.TaskService;
import utils.Util;

public class Android15 extends Boss {

    public boolean callApk13;

    public Android15() throws Exception {
        super(BossID.ANDROID_15, BossesData.ANDROID_15);
    }

    @Override
public void reward(Player plKill) {
    plKill.effect.addPointTrumSanBoss();

    // ---- PHẦN THƯỞNG CŨ (GIỮ NGUYÊN TỶ LỆ 15%) ----
    int[] itemRan = new int[]{20, 19};
    int oldItemId = itemRan[Util.nextInt(itemRan.length)]; // Đổi tên biến để tránh nhầm lẫn
    if (Util.isTrue(15, 100)) {
        ItemMap itOld = new ItemMap(this.zone, oldItemId, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        Service.gI().dropItemMap(this.zone, itOld);
    }
    // ---- KẾT THÚC PHẦN THƯỞNG CŨ ----


    // ---- PHẦN THƯỞNG MỚI (THÊM VÀO, TỶ LỆ 100%) ----
    int newItemId = 190;
    int newQuantity = 31000;
    ItemMap itNew = new ItemMap(this.zone, newItemId, newQuantity, this.location.x,
            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
    Service.gI().dropItemMap(this.zone, itNew);
    // ---- KẾT THÚC PHẦN THƯỞNG MỚI ----

    TaskService.gI().checkDoneTaskKillBoss(plKill, this);
}


    @Override
    protected void resetBase() {
        super.resetBase();
        this.callApk13 = false;
    }

    @Override
    public void active() {
        this.attack();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.callApk13 && damage >= this.nPoint.hp) {
            if (this.parentBoss != null) {
                ((Android14) this.parentBoss).callApk13();
            }
            return 0;
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
    }
}

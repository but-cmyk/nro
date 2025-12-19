package models.boss.boss_list.huydiet;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */
import models.boss.Boss;
import consts.BossID;
import models.boss.BossesData;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;

import java.util.Random;
import server.Manager;
import services.TaskService;
import utils.ItemUtil;

public class Cumber extends Boss {

    private long st;

    public Cumber() throws Exception {
        super(BossID.CUMBER, BossesData.CUMBER, BossesData.CUMBER_2);
    }

    @Override
    public void reward(Player plKill) {
        plKill.effect.addPointTrumSanBoss();
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        byte randomDo = (byte) new Random().nextInt(Manager.itemIds_tl_GN.length - 1);
        byte randomDo1 = (byte) new Random().nextInt(Manager.itemIds_tl_AWJ.length - 1);
        if (Util.isTrue(1, 3)) {
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiDTL(zone, Manager.itemIds_tl_GN[randomDo], 1, this.location.x, this.location.y, plKill.id));
        }
        if (Util.isTrue(2, 8)) {
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiDTL(zone, Manager.itemIds_tl_AWJ[randomDo1], 1, this.location.x, this.location.y, plKill.id));
        }
        if (Util.isTrue(1, 2)) {
            ItemMap item = new ItemMap(zone, 16, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), -1);
            item.options.add(new Item.ItemOption(73, 0));
        }
        if (Util.isTrue(1, 10)) {
            ItemMap item2 = new ItemMap(zone, 926, 1, this.location.x + Util.nextInt(50), this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), -1);
            item2.options.add(new Item.ItemOption(87, 0));
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (piercing) {
                damage /= 100;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
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

//package models.boss.boss_list.Cooler;
//
//
//import models.boss.Boss;
//import consts.BossID;
//import models.boss.BossesData;
//import models.item.Item;
//import models.map.ItemMap;
//import models.player.Player;
//import services.EffectSkillService;
//import services.Service;
//import utils.Util;
//
//import java.util.Random;
//import server.Manager;
//import services.TaskService;
//import utils.ItemUtil;
//
//public class Chiller extends Boss {
//
//    private long st;
//
//    public Chiller() throws Exception {
//        super(BossID.CHILLER, BossesData.CHILLER, BossesData.CHILLER_2);
//    }
//
//     @Override
//    public void reward(Player plKill) {
//        plKill.effect.addPointTrumSanBoss();
//        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
//        byte randomDo = (byte) new Random().nextInt(Manager.itemIds_tl_GN.length - 1);
//        byte randomDo1 = (byte) new Random().nextInt(Manager.itemIds_tl_AWJ.length - 1);
//        if (Util.isTrue(1, 100)) {
//            Service.gI().dropItemMap(this.zone, ItemUtil.ratiDTL(zone, Manager.itemIds_tl_GN[randomDo], 1, this.location.x, this.location.y, plKill.id));
//        }
//        if (Util.isTrue(3, 100)) {
//            Service.gI().dropItemMap(this.zone, ItemUtil.ratiDTL(zone, Manager.itemIds_tl_AWJ[randomDo1], 1, this.location.x, this.location.y, plKill.id));
//        }
//    }
//
//    @Override
//    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
//        if (!this.isDie()) {
//            if (this.currentLevel != 0) {
//                damage /= 2;
//            }
//            damage = this.nPoint.subDameInjureWithDeff(damage - Util.nextInt(100000));
//            if (!piercing && effectSkill.isShielding) {
//                if (damage > nPoint.hpMax) {
//                    EffectSkillService.gI().breakShield(this);
//                }
//                damage = 1;
//            }
//            this.nPoint.subHP(damage);
//            if (isDie()) {
//                this.setDie(plAtt);
//                die(plAtt);
//            }
//            return (int) damage;
//        } else {
//            return 0;
//        }
//    }
//
//    @Override
//    public void joinMap() {
//        super.joinMap();
//        st = System.currentTimeMillis();
//    }
//
//    @Override
//    public void autoLeaveMap() {
//        if (Util.canDoWithTime(st, 900000)) {
//            this.leaveMapNew();
//        }
//        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
//            st = System.currentTimeMillis();
//        }
//    }
//
//}

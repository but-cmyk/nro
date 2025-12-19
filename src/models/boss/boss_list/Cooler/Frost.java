//package models.boss.boss_list.Cooler;
//
///*
// * @Author: DienCoLamCoi
// * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
// * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
// */
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
//import server.Manager;
//import services.TaskService;
//
//public class Frost extends Boss {
//
//    private long st;
//
//    public Frost() throws Exception {
//        super(BossID.FROST, BossesData.FROST, BossesData.FROST_2, BossesData.FROST_3);
//    }
//
//    @Override
//    public void reward(Player plKill) {
//        plKill.effect.addPointTrumSanBoss();
//        if (Util.isTrue(1, 5)) {
//            Service.gI().dropItemMap(this.zone, Util.ratiDTL(zone, Manager.itemIds_tl_GN[Util.nextInt(Manager.itemIds_tl_GN.length - 1)], 1, this.location.x, this.location.y, plKill.id));
//        } else if (Util.isTrue(2, 8)) {
//            Service.gI().dropItemMap(this.zone, Util.ratiDTL(zone, Manager.itemIds_tl_AWJ[Util.nextInt(Manager.itemIds_tl_AWJ.length - 1)], 1, this.location.x, this.location.y, plKill.id));
//        } else if (Util.isTrue(1, 2)) {
//            ItemMap item = new ItemMap(zone, 16, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), -1);
//            item.options.add(new Item.ItemOption(30, 0));
//        }
//        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
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

//package models.boss.boss_list.huydiet;
//
///*
// * @Author: DienCoLamCoi
// * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
// * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
// */
//
//
//import models.boss.boss_list.Android.*;
//import models.boss.Boss;
//import consts.BossID;
//import java.util.Random;
//import models.boss.BossesData;
//import models.map.ItemMap;
//import models.player.Player;
//import models.skill.Skill;
//import server.Manager;
//import services.player.PlayerService;
//import services.Service;
//import services.TaskService;
//import utils.Util;
//
//public class berus extends Boss {
//
//    public berus() throws Exception {
//        super(BossID.BERUS, BossesData.BERUS);
//    }
//
//    @Override
//    public void reward(Player plKill) {
//        plKill.effect.addPointTrumSanBoss();
//        byte randomDo = (byte) new Random().nextInt(Manager.itemsDHD.length - 1);
//        byte randomDo1 = (byte) new Random().nextInt(Manager.itemIds_NR_FULL.length - 1);
//        if (Util.isTrue(8, 30)) {
//            Service.gI().dropItemMap(this.zone, Util.ratiDHD(zone, Manager.itemsDHD[randomDo], 1, this.location.x, this.location.y, plKill.id));
//        } else if (Util.isTrue(15, 50)) {
//            Service.gI().dropItemMap(this.zone, Util.ratiDTL(zone, Manager.itemIds_NR_FULL[randomDo1], 1, this.location.x, this.location.y, plKill.id));
//        }
//        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
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
//    @Override
//    public void joinMap() {
//        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
//        st = System.currentTimeMillis();
//    }
//    private long st;
//
////    @Override
////    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
////        if (plAtt != null) {
////            switch (plAtt.playerSkill.skillSelect.template.id) {
////                case Skill.KAMEJOKO:
////                case Skill.MASENKO:
////                case Skill.ANTOMIC:
////                    int hpHoi = (int) ((long) damage * 80 / 100);
////                    PlayerService.gI().hoiPhuc(this, hpHoi, 0);
////                    if (Util.isTrue(1, 5)) {
////                        this.chat("Hấp thụ.. các ngươi nghĩ sao vậy?");
////                    }
////                    return 0;
////            }
////        }
////        return super.injured(plAtt, damage, piercing, isMobAttack);
////    }
//
//    @Override
//    public void wakeupAnotherBossWhenDisappear() {
//        if (this.parentBoss != null) {
//            this.parentBoss.changeToTypePK();
//        }
//    }
//
//}

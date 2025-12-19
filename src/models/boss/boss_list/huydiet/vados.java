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
//import models.boss.BossesData;
//import models.map.ItemMap;
//import models.player.Player;
//import models.skill.Skill;
//import services.player.PlayerService;
//import services.Service;
//import services.TaskService;
//import utils.Util;
//
//public class vados extends Boss {
//
//    public vados() throws Exception {
//        super(BossID.VADOS, BossesData.VADOS);
//    }
//
//    @Override
//    public void reward(Player plKill) {
//        plKill.effect.addPointTrumSanBoss();
//        int[] itemRan = new int[]{380, 381, 382, 383, 384, 385};
//        int itemId = itemRan[2];
//        if (Util.isTrue(15, 100)) {
//            ItemMap it = new ItemMap(this.zone, itemId, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
//                    this.location.y - 24), plKill.id);
//            Service.gI().dropItemMap(this.zone, it);
//        }
//        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
//    }
//
//    @Override
//    public void chatM() {
//        if (Util.isTrue(60, 61)) {
//            super.chatM();
//            return;
//        }
//        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
//            return;
//        }
//        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
//            if (boss.id == BossID.CHAMPA && !boss.isDie()) {
//                this.chat("Hút năng lượng của nó, mau lên");
//                boss.chat("Tuân lệnh đại ca, hê hê hê");
//                break;
//            }
//        }
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
////                    PlayerService.gI().hoiPhuc(this, damage, 0);
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
//    public void doneChatS() {
//        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
//            if (boss.id == BossID.CHAMPA) {
//                boss.changeToTypePK();
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void changeToTypePK() {
//        super.changeToTypePK();
//        this.chat("Mau đền mạng cho thằng em trai ta");
//    }
//}

package models.boss.boss_list.MajinBuu12H;
import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import static consts.BossType.FINAL;
import consts.ConstPlayer;
import java.util.Random;
import models.map.ItemMap;
import models.player.Player;
import models.skill.Skill;
import server.Manager;
import services.EffectSkillService;
import services.Service;
import utils.Util;

import server.ServerNotify;
import services.player.PlayerService;
import services.SkillService;
import services.TaskService;
import services.map.ChangeMapService;
import utils.ItemUtil;
import utils.SkillUtil;

public class Drabura3 extends Boss {

    private long lastTimeJoin;

    private long lastTimePetrify;

    private long lastTimeChatAfk;

    private int timeChat;

    public Drabura3() throws Exception {
        super(FINAL, BossID.DRABURA_3, BossesData.DRABURA_3);
    }

    @Override
    public void joinMap() {
        this.lastTimeJoin = System.currentTimeMillis();
        this.zone = this.parentBoss.zoneFinal;
        ChangeMapService.gI().changeMap(this, this.zone, Util.nextInt(300, 400), 336);
//        ChangeMapService.gI().changeMap(this, this.zone,
//                this.parentBoss.location.x + Util.nextInt(-100, 100), this.parentBoss.location.y);
        Service.gI().changeFlag(this, 10);
        this.changeStatus(BossStatus.CHAT_S);
    }

    private void petrifyPlayersInTheMap() {
        for (Player pl : this.zone.getNotBosses()) {
            if (Util.isTrue(1, 10)) {
                this.chat("phẹt");
                EffectSkillService.gI().setIsStone(pl, 22000);
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        if(plKill.fightMabu != null) plKill.fightMabu.changePoint((byte) 10);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // --- LOGIC DROP ITEM TỐI ƯU ---
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);

        // 1. Drop Đá nâng cấp (Item 18, 19, 20)
        int[] itemDos = new int[]{19, 20, 18, 18};
        if (Util.isTrue(5, 100)) {
            int randomItem = itemDos[Util.nextInt(itemDos.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, randomItem, 1, x, y, plKill.id));
        }

        // 2. Drop Đồ Kaio (Huy Hiệu/Bông Tai...)
        if (Manager.itemIds_Kaio_AWJ.length > 0 && Util.isTrue(2, 100)) {
            int idItem = Manager.itemIds_Kaio_AWJ[Util.nextInt(Manager.itemIds_Kaio_AWJ.length)];
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiItemkaio(zone, idItem, 1, x, y, plKill.id));
        }

        if (Manager.itemIds_Kaio_GN.length > 0 && Util.isTrue(1, 100)) {
            int idItem = Manager.itemIds_Kaio_GN[Util.nextInt(Manager.itemIds_Kaio_GN.length)];
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiItemkaio(zone, idItem, 1, x, y, plKill.id));
        }

        // 3. Drop Đồ Lưỡng Long
        if (Manager.itemIds_LuongLong_AWJ.length > 0 && Util.isTrue(2, 100)) {
            int idItem = Manager.itemIds_LuongLong_AWJ[Util.nextInt(Manager.itemIds_LuongLong_AWJ.length)];
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiItemluonglong(zone, idItem, 1, x, y, plKill.id));
        }

        if (Manager.itemIds_LuongLong_GN.length > 0 && Util.isTrue(1, 100)) {
            int idItem = Manager.itemIds_LuongLong_GN[Util.nextInt(Manager.itemIds_LuongLong_GN.length)];
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiItemluonglong(zone, idItem, 1, x, y, plKill.id));
        }
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(this.lastTimeJoin, 60000)) {
            this.leaveMap();
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

             int skillID = plAtt.playerSkill.skillSelect.template.id;

        // Danh sách các skill được phép phá giới hạn damage
        boolean isSpecialSkill = (skillID == Skill.TU_SAT ||
                skillID == Skill.MAKANKOSAPPO ||
                skillID == Skill.QUA_CAU_KENH_KHI);

        // 1. Giới hạn trần 199,999 damage
        // Chỉ áp dụng giới hạn này nếu KHÔNG PHẢI là skill đặc biệt
        if (!isSpecialSkill && damage >= 199999) {
            damage = 199999;
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
    public void afk() {
        if (Util.canDoWithTime(lastTimeChatAfk, timeChat)) {
            this.chat("Đừng vội mừng, ta sẽ hồi sinh và thịt hết bọn mi");
            this.lastTimeChatAfk = System.currentTimeMillis();
            this.timeChat = Util.nextInt(10000, 15000);
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.lastTimeChatAfk = System.currentTimeMillis();
        this.changeStatus(BossStatus.AFK);
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            if (Util.canDoWithTime(lastTimePetrify, 10000)) {
                petrifyPlayersInTheMap();
                this.lastTimePetrify = System.currentTimeMillis();
            }
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)), pl.location.y);
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)), pl.location.y);
                        }
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(50, 100);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
    }

    @Override
    public void moveToPlayer(Player pl) {
        moveTo(pl.location.x, pl.location.y);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

}

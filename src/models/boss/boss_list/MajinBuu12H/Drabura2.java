package models.boss.boss_list.MajinBuu12H;

import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import consts.AppearType;
import static consts.BossType.FINAL;
import java.util.Random;
import models.map.ItemMap;

import models.player.Player;
import services.EffectSkillService;
import utils.Util;

import services.TaskService;
import services.map.ChangeMapService;
import models.skill.Skill;
import server.Manager;
import services.Service;
import utils.ItemUtil;

public class Drabura2 extends Boss {

    private boolean callBoss = true;

    private long lastTimeJoin;

    public Drabura2() throws Exception {
        super(FINAL, BossID.DRABURA_2, BossesData.DRABURA_2);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            this.zone = zoneFinal;
        }
        this.lastTimeJoin = System.currentTimeMillis();
        this.callBoss = false;
        ChangeMapService.gI().changeMap(this, this.zone, Util.nextInt(300, 400), 336);
        this.changeStatus(BossStatus.CHAT_S);
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
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {

            if (plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null
                    && plAtt.playerSkill.skillSelect.template != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                    case Skill.LIEN_HOAN:
                        return 0;
                }
            }

            if (plAtt != null && plAtt.isPl() && Util.isTrue(1, 5)) {
                plAtt.fightMabu.changePercentPoint((byte) 1);
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            int skillID = -1;
            if (plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null
                    && plAtt.playerSkill.skillSelect.template != null) {
                skillID = plAtt.playerSkill.skillSelect.template.id;
            }

        // Danh sách các skill được phép phá giới hạn damage
        boolean isSpecialSkill = (skillID == Skill.TU_SAT ||
                skillID == Skill.MAKANKOSAPPO ||
                skillID == Skill.QUA_CAU_KENH_KHI);

        // 1. Giới hạn trần 199,999 damage
        // Chỉ áp dụng giới hạn này nếu KHÔNG PHẢI là skill đặc biệt
        if (!isSpecialSkill && damage >= 199999) {
            damage = 199999;
        }

            if (damage >= this.nPoint.hp) {
                this.changeStatus(BossStatus.AFK);
                damage = 0;
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
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }
        if (this.data[nextLevel].getTypeAppear() == AppearType.DEFAULT_APPEAR
                && Util.canDoWithTime(lastTimeRest, secondsRest * 1000)) {
            this.changeStatus(BossStatus.RESPAWN);
        }

        if (Util.canDoWithTime(lastTimeRest, 5000)) {
            if (!this.callBoss) {
                for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                    boss.changeStatus(BossStatus.RESPAWN);
                }
                this.callBoss = true;
            }
        }

    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(this.lastTimeJoin, 250000)) {
            this.leaveMap();
        }
    }

    @Override
    public void afk() {
        this.changeToTypeNonPK();
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

}

package models.boss.boss_list.MajinBuu12H;

import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import static consts.BossType.FINAL;
import models.map.ItemMap;
import models.player.Player;
import services.Service;
import utils.Util;
import server.ServerNotify;
import services.EffectSkillService;
import services.TaskService;
import models.skill.Skill;
import server.Manager;
import utils.ItemUtil;

public class BuiBui extends Boss {

    private long lastTimeAfk;
    private long lastTimeChatAfk;
    private int timeChat;

    public BuiBui() throws Exception {
        super(FINAL, BossID.BUI_BUI, BossesData.BUI_BUI);
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
        if (this.isDie()) return 0;

        if (!piercing && Util.isTrue(200, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }

        // Kháng skill đặc biệt
        if (plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null
                && plAtt.playerSkill.skillSelect.template != null) {
            int skillId = plAtt.playerSkill.skillSelect.template.id;
            if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO ||
                    skillId == Skill.ANTOMIC || skillId == Skill.LIEN_HOAN) {
                return 0; // Boss né skill chưởng?
            }
        }

        if (plAtt != null && plAtt.isPl() && plAtt.fightMabu != null && Util.isTrue(1, 5)) {
            plAtt.fightMabu.changePercentPoint((byte) 1);
        }

        damage = this.nPoint.subDameInjureWithDeff(damage);

        if (!piercing && effectSkill.isShielding) {
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
    }

    @Override
    public void afk() {
        if (Util.canDoWithTime(lastTimeChatAfk, timeChat)) {
            this.chat("Đừng vội mừng, ta sẽ hồi sinh và thịt hết bọn mi");
            this.lastTimeChatAfk = System.currentTimeMillis();
            this.timeChat = Util.nextInt(10000, 15000);
        }
        // Logic hồi sinh liên tục trong phó bản 12H
        if (Util.canDoWithTime(lastTimeAfk, 60000)) {
            Service.gI().hsChar(this, this.nPoint.hpMax, this.nPoint.mpMax);
            this.changeStatus(BossStatus.CHAT_S);
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.lastTimeAfk = System.currentTimeMillis();
        this.changeStatus(BossStatus.AFK);
    }
}
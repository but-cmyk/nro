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
import services.Service;
import utils.Util;

import server.ServerNotify;
import services.EffectSkillService;
import services.SkillService;
import services.TaskService;
import models.skill.Skill;
import server.Manager;
import utils.ItemUtil;
import utils.SkillUtil;

public class Yacon extends Boss {

    private long lastTimeTanHinh;

    private long lastTimeAfk;

    private long lastTimeChatAfk;

    private int timeChat;

    public Yacon() throws Exception {
        super(FINAL, BossID.YA_CON, BossesData.YACON);
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
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.canDoWithTime(this.lastTimeTanHinh, 10000) && Util.isTrue(5, 20)) {
                    if (SkillUtil.isUseSkillChuong(this)) {
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                    } else {
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                    }
                }
                SkillService.gI().useSkill(this, pl, null, -1, null);
                checkPlayerDie(pl);
                if (!Util.canDoWithTime(this.lastTimeTanHinh, 10000)) {
                    this.nPoint.crit = 100;
                    Service.gI().setPos2(this, pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                            10000);
                } else {
                    this.nPoint.crit = 10;
                }
                if (Util.canDoWithTime(this.lastTimeTanHinh, 30000)) {
                    if (Util.isTrue(1, 10)) {
                        String[] chat = {"Mi đâu rồi", "Đồ ăn gian!"};
                        Service.gI().chat(pl, chat[Util.nextInt(chat.length)]);
                        this.lastTimeTanHinh = System.currentTimeMillis();
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void afk() {
        if (Util.canDoWithTime(lastTimeChatAfk, timeChat)) {
            this.chat("Đừng vội mừng, ta sẽ hồi sinh và thịt hết bọn mi");
            this.lastTimeChatAfk = System.currentTimeMillis();
            this.timeChat = Util.nextInt(10000, 15000);
        }
        if (Util.canDoWithTime(lastTimeAfk, 60000)) {
            Service.gI().hsChar(this, this.nPoint.hpMax, this.nPoint.mpMax);
            this.changeStatus(BossStatus.CHAT_S);
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
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.lastTimeAfk = System.currentTimeMillis();
        this.changeStatus(BossStatus.AFK);
    }
}

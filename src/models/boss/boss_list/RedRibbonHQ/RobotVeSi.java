package models.boss.boss_list.RedRibbonHQ;

import consts.BossID;
import consts.BossStatus;
import consts.ConstPlayer;
import managers.boss.RedRibbonHQManager;
import models.boss.*;
import static consts.BossType.PHOBANDT;
import models.map.ItemMap;
import models.map.Zone;
import models.player.Player;
import models.skill.Skill;
import services.EffectSkillService;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import utils.SkillUtil;
import utils.Util;

public class RobotVeSi extends Boss {

    public RobotVeSi(Zone zone, int id, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.ROBOT_VE_SI - id, new BossData(
                "Rôbốt Vệ Sĩ 0" + id, //name
                ConstPlayer.TRAI_DAT, //gender
                new short[]{138, 139, 140, -1, -1, -1}, //outfit {head, body, leg, bag, aura, eff}
                ((dame)), //dame
                new long[]{((hp))}, //hp
                new int[]{57}, //map join
                new int[][]{
                    {Skill.KAMEJOKO, 7, 6}, {Skill.KAMEJOKO, 6, 7}, {Skill.KAMEJOKO, 5, 8}, {Skill.KAMEJOKO, 4, 9}, {Skill.KAMEJOKO, 3, 10}, {Skill.KAMEJOKO, 2, 11}, {Skill.KAMEJOKO, 1, 12},
                    {Skill.ANTOMIC, 1, 13}, {Skill.ANTOMIC, 2, 14}, {Skill.ANTOMIC, 3, 15}, {Skill.ANTOMIC, 4, 16}, {Skill.ANTOMIC, 5, 17}, {Skill.ANTOMIC, 6, 19}, {Skill.ANTOMIC, 7, 20},
                    {Skill.MASENKO, 1, 21}, {Skill.MASENKO, 5, 22}, {Skill.MASENKO, 6, 23},
                    {Skill.KAMEJOKO, 7, 1000},},
                new String[]{}, //text chat 1
                new String[]{}, //text chat 2
                new String[]{}, //text chat 3
                60
        ));

        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        if (this.zone == null || this.zone.map == null) {
            return;
        }
        long plId = (plKill != null) ? plKill.id : -1;

        // Rơi ngọc rồng (4 - 7 sao: id 17 - 20)
        int nrId = Util.nextInt(17, 20);
        int nrX = Math.max(24, Math.min(this.zone.map.mapWidth - 24, this.location.x));
        int nrY = this.zone.map.yPhysicInTop(nrX, this.location.y - 24);
        ItemMap itNr = new ItemMap(this.zone, nrId, 1, nrX, nrY, plId);
        Service.gI().dropItemMap(this.zone, itNr);

        // Rơi 10 cục vàng, mỗi cục 31k vàng (Item 190)
        for (int i = 0; i < 10; i++) {
            int offsetX = (i % 2 == 0 ? 1 : -1) * ((i / 2 + 1) * 15);
            int dropX = Math.max(24, Math.min(this.zone.map.mapWidth - 24, this.location.x + offsetX));
            int dropY = this.zone.map.yPhysicInTop(dropX, this.location.y - 24);
            ItemMap itGold = new ItemMap(this.zone, 190, 31000, dropX, dropY, plId);
            Service.gI().dropItemMap(this.zone, itGold);
        }

        // Rơi nguyên liệu Sách Tuyệt Kĩ
        // 1. Rơi 1 - 2 Bìa Sách (Item 1154)
        int biaSachCount = Util.nextInt(1, 2);
        ItemMap itBia = new ItemMap(this.zone, 1154, biaSachCount, this.location.x - 25, this.zone.map.yPhysicInTop(this.location.x - 25, this.location.y - 24), plId);
        itBia.options.add(new models.item.Item.ItemOption(30, 0));
        Service.gI().dropItemMap(this.zone, itBia);

        // 2. Rơi 1 - 2 Bùa Giám Định (Item 1152)
        int buaCount = Util.nextInt(1, 2);
        ItemMap itBua = new ItemMap(this.zone, 1152, buaCount, this.location.x + 25, this.zone.map.yPhysicInTop(this.location.x + 25, this.location.y - 24), plId);
        itBua.options.add(new models.item.Item.ItemOption(30, 0));
        Service.gI().dropItemMap(this.zone, itBua);

        // 3. 25% rơi 1 Kìm Bấm Giấy (Item 1153)
        if (Util.isTrue(25, 100)) {
            ItemMap itKim = new ItemMap(this.zone, 1153, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plId);
            itKim.options.add(new models.item.Item.ItemOption(30, 0));
            Service.gI().dropItemMap(this.zone, itKim);
        }

        if (plKill != null) {
            services.TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        }
    }

    @Override
    public void joinMap() {
        int x = Util.nextInt(50, this.zone.map.mapWidth - 50);
        ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 240));
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
        Service.gI().setPos(this, 300, 312);
    }

    @Override
    public void afk() {
        Player pl = getPlayerAttack();
        if (pl == null || pl.isDie()) {
            return;
        }
        Service.gI().setPos(this, pl.location.x + Util.nextInt(-100, 100), 0);
        this.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 2;
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
        reward(plKill);
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        RedRibbonHQManager.gI().removeBoss(this);
        this.dispose();
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 700) && this.typePk == ConstPlayer.PK_ALL) {//800;time tung chiêu của boss
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }

                // Chọn kỹ năng ngẫu nhiên từ danh sách kỹ năng chưởng
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    // Di chuyển tới gần mục tiêu
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }

                    // Dùng kỹ năng
                    SkillService.gI().useSkill(this, pl, null, -1, null);

                    // Kiểm tra người chơi có chết không
                    checkPlayerDie(pl);

                } else {

                    this.moveToPlayer(pl);

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

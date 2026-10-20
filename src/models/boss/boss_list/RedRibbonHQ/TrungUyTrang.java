package models.boss.boss_list.RedRibbonHQ;

import consts.BossID;
import consts.BossStatus;
import consts.ConstPlayer;
import managers.boss.RedRibbonHQManager;
import models.boss.*;
import static consts.BossType.PHOBANDT;
import consts.ConstRatio;
import models.map.ItemMap;
import models.map.Zone;
import models.player.Player;
import models.skill.Skill;
import services.EffectSkillService;
import services.player.PlayerService;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import utils.SkillUtil;
import utils.Util;

public class TrungUyTrang extends Boss {

    public TrungUyTrang(Zone zone, long dame, long hp) throws Exception {
        super(PHOBANDT, BossID.TRUNG_UY_TRANG, new BossData(
                "Trung uý Trắng", //name
                ConstPlayer.TRAI_DAT, //gender
                new short[]{141, 142, 143, -1, -1, -1}, //outfit {head, body, leg, bag, aura, eff}
                (dame), //dame
                new long[]{(hp)}, //hp
                new int[]{59}, //map join
                new int[][]{
                    {Skill.DEMON, 3, 1}, {Skill.DEMON, 6, 2}, {Skill.DRAGON, 7, 3}, {Skill.DRAGON, 1, 4}, {Skill.GALICK, 5, 5},
                    {Skill.KAMEJOKO, 7, 6}, {Skill.KAMEJOKO, 6, 7}, {Skill.KAMEJOKO, 5, 8}, {Skill.KAMEJOKO, 4, 9}, {Skill.KAMEJOKO, 3, 10}, {Skill.KAMEJOKO, 2, 11}, {Skill.KAMEJOKO, 1, 12},
                    {Skill.ANTOMIC, 1, 13}, {Skill.ANTOMIC, 2, 14}, {Skill.ANTOMIC, 3, 15}, {Skill.ANTOMIC, 4, 16}, {Skill.ANTOMIC, 5, 17}, {Skill.ANTOMIC, 6, 19}, {Skill.ANTOMIC, 7, 20},
                    {Skill.MASENKO, 1, 21}, {Skill.MASENKO, 5, 22}, {Skill.MASENKO, 6, 23},
                    {Skill.KAMEJOKO, 7, 1000},},
                new String[]{}, //text chat 1
                new String[]{"|-1|Xem mi dùng cách nào hạ được ta",
                    "|-1|Ha ha ha",
                    "|-1|Bulon đâu tiêu diệt hết bọn chúng cho ta"}, //text chat 2
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

        if (plKill != null) {
            services.TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        }
    }

    @Override
    public void joinMap() {
        int x = 1036;
        ChangeMapService.gI().changeMap(this, this.zone, x, this.zone.map.yPhysicInTop(x, 0));
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void active() {
        super.active();
    }

    @Override
    public void doneChatS() {
        Service.gI().setPos(this, 198, 456);
        this.zone.isTUTAlive = true;
    }

    private void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
        } else {
            Service.gI().setPos(this, x, y);
        }
    }

    private void goToPlayer(Player pl, boolean isTeleport) {
        goToXY(pl.location.x, pl.location.y, isTeleport);
    }

    @Override
    public void attack() {
        try {
            Player playerAtt = getPlayerAttack();
            if (playerAtt == null || playerAtt.isDie() || playerAtt.location.x < 755 || playerAtt.location.x > 1060) {
                return;
            }
            if (this.location.x < 775) {
                goToPlayer(playerAtt, true);
            }

            if (playerAtt.location != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
                if (this.isDie()) {
                    return;
                }

                // Chọn kỹ năng ngẫu nhiên
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

                if (Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                    // Boss di chuyển ngẫu nhiên khi dùng kỹ năng chưởng
                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(
                                playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)),
                                Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 50),
                                false
                        );
                    }

                    // Boss dùng kỹ năng
                    SkillService.gI().useSkill(this, playerAtt, null, -1, null);

                    // Kiểm tra người chơi có chết không
                    checkPlayerDie(playerAtt);

                } else {
                    // Boss chưa trong tầm, tiếp cận mục tiêu
                    goToPlayer(playerAtt, false);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace(); // Ghi log lỗi nếu có
        }
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        // Nếu boss tự đánh chính mình (PST) thì vẫn nhận dame
        if (plAtt != null && plAtt.id == this.id) {
            damage = this.nPoint.subDameInjureWithDeff(damage / 2);
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        }

        // Logic cũ - cần Bulon chết mới nhận dame từ player
        if (!this.isDie() && !this.zone.isbulon1Alive && !this.zone.isbulon2Alive) {
            if (!piercing && Util.isTrue(20, 100)) {
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
        this.zone.isTUTAlive = false;
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        RedRibbonHQManager.gI().removeBoss(this);
        this.dispose();
    }
   
}

package models.boss.boss_list.DeathOrAliveArena;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.ConstRatio;
import models.boss.Boss;
import models.boss.BossData;
import managers.boss.OtherBossManager;
import consts.BossStatus;
import consts.BossType;
import models.player.Player;
import services.EffectSkillService;
import services.player.PlayerService;
import services.SkillService;
import services.map.ChangeMapService;
import utils.SkillUtil;
import utils.Util;

public abstract class DeathOrAliveArena extends Boss {

    protected Player playerAtt;
    protected long timeJoinMap;
    protected long lastTimeAttack = 0;
    protected long lastTimeMove = 0;
    protected long delayNextMove = 3500;

    public DeathOrAliveArena(BossType ducvupro, int id, BossData data) throws Exception {
        super(ducvupro, id, data);
        this.bossStatus = BossStatus.RESPAWN;
    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    public void hutMau() {

    }

    public void tanHinh() {

    }

    public void bayLungTung() {
        if (Util.canDoWithTime(lastTimeMove, delayNextMove)) {
            lastTimeMove = System.currentTimeMillis();
            delayNextMove = Util.nextInt(3500, 5000);
            int nextX = Util.nextInt(350, 590);
            int nextY = Util.nextInt(220, 336);
            goToXY(nextX, nextY, false);
        }
    }

    @Override
    public void afk() {
        if (!(playerAtt != null && playerAtt.location != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone))) {
            this.leaveMap();
        }
    }

    protected void goToXY(int x, int y, boolean isTeleport) {
        int safeX = Math.max(340, Math.min(600, x));
        int safeY = Math.max(220, Math.min(336, y));
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - safeX < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(40, 80);
            int newX = this.location.x + (dir == 1 ? move : -move);
            newX = Math.max(340, Math.min(600, newX));
            PlayerService.gI().playerMove(this, newX, safeY);
        } else {
            ChangeMapService.gI().changeMapYardrat(this, this.zone, safeX, safeY);
        }
    }

    @Override
    public void attack() {
        try {
            if (playerAtt != null && playerAtt.location != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
                if (this.isDie() || this.nPoint == null || playerAtt.isDie() || playerAtt.nPoint == null) {
                    return;
                }
                hutMau();
                tanHinh();
                bayLungTung();

                // Tần suất: 3s đấm hoặc chưởng 1 lần
                if (!Util.canDoWithTime(lastTimeAttack, 3000)) {
                    return;
                }

                // Chọn chiêu: Ưu tiên chọn giữa Đấm và Chưởng
                java.util.List<models.skill.Skill> attackSkills = new java.util.ArrayList<>();
                if (this.playerSkill.skills != null) {
                    for (models.skill.Skill s : this.playerSkill.skills) {
                        if (s.template.id != models.skill.Skill.TAI_TAO_NANG_LUONG) {
                            attackSkills.add(s);
                        }
                    }
                }

                if (!attackSkills.isEmpty()) {
                    this.playerSkill.skillSelect = attackSkills.get(Util.nextInt(0, attackSkills.size() - 1));
                } else if (!this.playerSkill.skills.isEmpty()) {
                    this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                }

                boolean isChuong = SkillUtil.isUseSkillChuong(this);
                if (isChuong) {
                    // Chưởng từ xa trong phạm vi võ đài
                    SkillService.gI().useSkill(this, playerAtt, null, -1, null);
                    checkPlayerDie(playerAtt);
                    lastTimeAttack = System.currentTimeMillis();
                } else {
                    // Đấm: Áp sát player nếu còn xa
                    int distance = Util.getDistance(this, playerAtt);
                    if (distance <= 80) {
                        SkillService.gI().useSkill(this, playerAtt, null, -1, null);
                        checkPlayerDie(playerAtt);
                        lastTimeAttack = System.currentTimeMillis();
                    } else {
                        goToPlayer(playerAtt, false);
                    }
                }
            } else {
                this.leaveMap();
            }
        } catch (Exception ex) {
        }
    }

    protected void goToPlayer(Player pl, boolean isTeleport) {
        goToXY(pl.location.x, pl.location.y, isTeleport);
    }

    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            this.nPoint.hpMax = (long) playerAtt.nPoint.hpMax * (100 + (Math.abs(this.id) - 82)) / 100;
            this.nPoint.hp = this.nPoint.hpMax;
            ChangeMapService.gI().changeMap(this, this.zone, 523, 336);
        }
    }

    protected void immortalMp() {
        this.nPoint.mp = this.nPoint.mpg;
    }

    @Override
    public void update() {
        try {
            super.updateInfo();
            if ((this.effectSkill != null && this.effectSkill.isHaveEffectSkill()) || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
                return;
            }
            switch (this.bossStatus) {
                case RESPAWN:
                    this.respawn();
                    this.changeStatus(BossStatus.JOIN_MAP);
                case JOIN_MAP:
                    joinMap();
                    if (this.zone != null) {
                        changeStatus(BossStatus.AFK);
                        timeJoinMap = System.currentTimeMillis();
                        this.immortalMp();
                        this.typePk = 3;
                    }
                    break;
                case AFK:
                    afk();
                    break;
                case ACTIVE:
                    if (this.playerSkill.prepareTuSat || this.playerSkill.prepareLaze || this.playerSkill.prepareQCKK) {
                        break;
                    } else {
                        this.attack();
                    }
                    break;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(100, 1000)) {
                String[] missChat = {
                    "Xí hụt rồi nha cưng!",
                    "Lêu lêu, đánh vào không khí à!",
                    "Nhanh như chớp, đố trúng được ta!",
                    "Trượt rồi bé ơi, ngắm kỹ lại đi nào!"
                };
                this.chat(missChat[Util.nextInt(0, missChat.length - 1)]);
                return 0;
            }

            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            if (damage > this.nPoint.hpMax / 10) {
                damage = this.nPoint.hpMax / 10;
            }

            this.nPoint.subHP(damage);

            if (isDie()) {
                String[] dieChat = {
                    "Ui da đau quá, ta xin hàng!",
                    "Bà Hạt Mít ơi cứu con, đối thủ bạo lực quá!",
                    "Gãy hết xương cốt rồi, ta rút lui đây!",
                    "Chờ đấy, ta đi méch mẹ ta!"
                };
                this.chat(dieChat[Util.nextInt(0, dieChat.length - 1)]);
                this.setDie(plAtt);
                die(plAtt);
            }

            return (int) damage;
        } else {
            return 0;
        }
    }

    protected void notifyPlayeKill(Player player) {

    }

    @Override
    public void die(Player plKill) {
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        OtherBossManager.gI().removeBoss(this);
        this.dispose();
    }
}

package models.boss.boss_list.NewBoss;

import models.boss.Boss;
import models.boss.BossesData;
import models.player.Player;
import services.player.PlayerService;
import services.Service;
import services.SkillService;
import utils.Util;

public class BaDo extends Boss {

    private static final String[] textOdo = new String[]{
        "Hôi quá, tránh xa ta ra", "Biến đi", "Trời ơi đồ ở dơ",
        "Thúi quá", "Mùi gì hôi quá", "Ngươi cũng có mùi giống ta đó!"
    };

    // --- BIẾN QUẢN LÝ TRẠNG THÁI AI ---
    private long lastTimeOdoAura;
    private long lastTimeAttack;
    private long moveAwayTime;
    private boolean isMovingAway;

    private static final long SMELL_AURA_COOLDOWN = 10000;
    private static final long MOVE_AWAY_DURATION = 1500;
    private static final long ATTACK_COOLDOWN = 2000;

    public BaDo() throws Exception {
        // Tắt thông báo khi xuất hiện
        super(-Util.nextInt(1000, 1000000), true, false, BossesData.O_DO_NEW);
        this.isMovingAway = false;
    }

    // Kỹ năng hào quang hôi thối (hành vi đặc trưng, giữ lại ở lớp con)
    private void activateSmellAura() {
        if (this.zone != null && Util.canDoWithTime(lastTimeOdoAura, SMELL_AURA_COOLDOWN)) {
            this.chat(textOdo[Util.nextInt(0, textOdo.length - 1)]);
            for (Player pl : this.zone.getNotBosses()) {
                if (pl != null && !pl.isDie() && Util.getDistance(this, pl) < 150) {
                    long subHp = pl.nPoint.hpMax * 20 / 100;
                    if (subHp >= pl.nPoint.hp) subHp = pl.nPoint.hp - 1;
                    pl.injured(null, subHp, true, false);
                    PlayerService.gI().sendInfoHpMpMoney(pl);
                    Service.gI().Send_Info_NV(pl);
                }
            }
            this.lastTimeOdoAura = System.currentTimeMillis();
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) return;
        activateSmellAura();
    }

    @Override
    public void attack() {
        if (this.isDie() || this.effectSkill.isHaveEffectSkill()) return;

        if (isMovingAway && Util.canDoWithTime(moveAwayTime, MOVE_AWAY_DURATION)) {
            isMovingAway = false;
        }
        if (isMovingAway) return;

        if (Util.canDoWithTime(this.lastTimeAttack, ATTACK_COOLDOWN)) {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) return;

            if (Util.getDistance(this, pl) > 40) {
                this.moveToPlayer(pl);
            } else {
                this.playerSkill.skillSelect = this.playerSkill.skills.get(0);
                SkillService.gI().useSkill(this, pl, null, -1, null);
                this.lastTimeAttack = System.currentTimeMillis();
            }
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!isMovingAway) {
                isMovingAway = true;
                moveAwayTime = System.currentTimeMillis();
                // Sử dụng phương thức kế thừa từ lớp cha
                moveAwayFromPlayer(plAtt, 80);
            }
            damage = this.nPoint.subDameInjureWithDeff(Util.nextInt(300, 500));
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        }
        return 0;
    }

    @Override
    public void reward(Player plKill) {
        // Thêm phần thưởng khi bị tiêu diệt (nếu có)
    }

    @Override
    protected void notifyJoinMap() {
        // Ghi đè để tắt thông báo
    }
}
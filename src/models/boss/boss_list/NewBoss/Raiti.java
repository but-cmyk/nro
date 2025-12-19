package models.boss.boss_list.NewBoss;

import models.boss.Boss;
import models.boss.BossesData;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import utils.ItemUtil;
import utils.Util;

public class Raiti extends Boss {

    private boolean isEnraged; // Cờ trạng thái nổi giận

    public Raiti() throws Exception {
        super(-Util.nextInt(1000, 1000000), true, false, BossesData.RAITI);
        this.isEnraged = false;
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) {
            return;
        }

        // Kiểm tra điều kiện nổi giận
        if (!isEnraged && this.nPoint.hp < this.nPoint.hpMax / 2) {
            enrage();
        }
    }

    // Hàm kích hoạt trạng thái nổi giận
    private void enrage() {
        this.isEnraged = true;
        this.chat("Các ngươi đã chọc giận ta rồi! HAAAAA!");
        // Tăng sức mạnh, ví dụ tăng 30% sát thương
        this.nPoint.dameg *= 1.3;
        // Tăng tốc độ tấn công (giảm thời gian nghỉ)
        this.data[this.currentLevel].setSecondsRest(1);
        // Tạo hiệu ứng aura đỏ

    }

    @Override
    protected void notifyJoinMap() {
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            // Khi nổi giận, giảm 20% sát thương nhận vào
            if (isEnraged) {
                damage *= 0.8;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (damage <= 0) {
                damage = 1;
            }

            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        }
        return 0;
    }

}

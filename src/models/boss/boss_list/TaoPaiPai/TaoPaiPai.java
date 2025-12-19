package models.boss.boss_list.TaoPaiPai;

import models.boss.Boss;
import models.boss.BossesData;
import static consts.BossType.FINAL;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;


public class TaoPaiPai extends Boss {

    private long lastTimeCheckPlayer;

    public TaoPaiPai() throws Exception {
        super(FINAL, Util.randomBossId(), BossesData.TAU_PAY_PAY_DONG_NAM_KARIN);
       
    }
   
    @Override
    protected void notifyJoinMap() {
        // Không gọi super => không gửi thông báo
    }

    @Override
    public void update() {
        super.update();
        // Kiểm tra mỗi 5 giây
        if (!isDie() && Util.canDoWithTime(this.lastTimeCheckPlayer, 5000)) {
            // Giả định rằng 'this.zone' cho phép truy cập vào bản đồ mà boss đang đứng
            // và 'this.zone.getPlayers()' trả về danh sách người chơi trong bản đồ đó.
            if (this.zone != null && this.zone.getPlayers().isEmpty()) {
                if (this.nPoint.hp < this.nPoint.hpMax) {
                    this.nPoint.hp = this.nPoint.hpMax;
                    this.chat("Chúng bay đâu hết rồi? Sợ ta rồi sao!");
                }
            }
            this.lastTimeCheckPlayer = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (damage >= 100) {
                damage = 100;
            }

            long bossDamage = (long) (plAtt.nPoint.hpMax * 0.005);

            if (bossDamage < 1) {
                bossDamage = 1;
            }
            this.nPoint.dame = (int) bossDamage;

            this.nPoint.subHP(damage);

            long tnSm;
            if (damage >= 90) {

                tnSm = 325;
            } else if (damage >= 80 && damage < 90) {

                tnSm = 300;
            } else if (damage >= 70 && damage < 80) {

                tnSm = 280;
            } else if (damage >= 60 && damage < 70) {

                tnSm = 250;
            } else if (damage >= 50 && damage < 60) {

                tnSm = 200;
            } else if (damage >= 40 && damage < 50) {

                tnSm = 150;
            } else if (damage >= 30 && damage < 40) {

                tnSm = 100;

            } else {

                tnSm = 50;
            }
            if (plAtt.nPoint.power >= 1_500_000) {
                tnSm = Util.nextInt(1);
            }
            Service.gI().addSMTN(plAtt, (byte) 2, tnSm, true);

            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        } else {
            return 0;
        }
    }
}
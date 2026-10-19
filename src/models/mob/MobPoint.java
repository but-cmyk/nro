package models.mob;

import utils.Util;

public class MobPoint {

    public final Mob mob;
    public long hp;
    public long maxHp;
    public long dame;

    public MobPoint(Mob mob) {
        this.mob = mob;
    }

    public long getHpFull() {
        if (mob != null && mob.lvMob > 0) {
            return maxHp * 10L;
        }
        return maxHp;
    }

    public void setHpFull(long hp) {
        maxHp = hp;
    }

    public long gethp() {
        return hp;
    }

    public void sethp(long hp) {
        if (hp < 0) {
            this.hp = 0;
        } else {
            long hpFull = this.getHpFull();
            this.hp = (hpFull > 0) ? Math.min(hp, hpFull) : hp;
        }
    }

    public long getDameAttack() {
        return this.dame != 0 ? this.dame + Util.nextLong(-(this.dame / 100), (this.dame / 100))
                : this.getHpFull() * (long) Util.nextInt(mob.pDame - 1, mob.pDame + 1) / 100L
                + (long) Util.nextInt(-(mob.level * 10), mob.level * 10);
    }
}

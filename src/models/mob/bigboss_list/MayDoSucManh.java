package models.mob.bigboss_list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.mob.BigBoss;
import models.mob.Mob;
import models.player.Player;
import utils.Util;

public class MayDoSucManh extends BigBoss {

    private final Map<Long, Long> damageMap = new HashMap<>();
    private final Map<Long, Integer> milestoneMap = new HashMap<>();
    private long lastTimeRegen = System.currentTimeMillis();

    public MayDoSucManh(Mob mob) {
        super(mob);
    }

    @Override
    public void setDie() {
        super.setDie();
        damageMap.clear();
        milestoneMap.clear();
    }

    public void moveTo(int x, int y) {
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (isDie() || plAtt == null || damage <= 0) {
            return;
        }

        long hpBefore = this.point.hp;
        super.injured(plAtt, damage, dieWhenHpFull);
        long hpAfter = Math.max(this.point.hp, 0);
        long realDamage = hpBefore - hpAfter;

        if (realDamage > 0) {
            updateMilestone(plAtt, realDamage);
        }
    }

    public void updateMilestone(Player pl, long realDamage) {
        if (pl == null) {
            return;
        }

        long playerId = pl.id;

        long totalDamage = damageMap.getOrDefault(playerId, 0L) + realDamage;
        damageMap.put(playerId, totalDamage);

        int prevMilestone = milestoneMap.getOrDefault(playerId, 0);
        int currentMilestone = (int) (totalDamage / 10_000_000L);

        if (currentMilestone > prevMilestone) {
            int milestonesPassed = currentMilestone - prevMilestone;
            int points = milestonesPassed * 10;

//            pl.point_maydam += points;
//            milestoneMap.put(playerId, currentMilestone);
//
//            if (!Manager.isTopMaydamChanged) {
//                Manager.isTopMaydamChanged = true;
//            }
//
//            Service.gI().sendThongBao(pl, "Bạn đã nhận được " + points + " điểm Máy Đấm!");
//            Service.gI().updatePlayerPointMayDam(pl);
//        }
//
//        pl.total_damage_maydam += realDamage;
//        Service.gI().updatePlayerTotalDamage(pl);
    }}

    @Override
    public void update() {
        super.update();
        if (!isDie() && Util.canDoWithTime(lastTimeRegen, 1000)) {
            this.point.hp = this.point.maxHp;
            lastTimeRegen = System.currentTimeMillis();
        }
    }
}

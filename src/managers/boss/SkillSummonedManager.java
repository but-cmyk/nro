package managers.boss;

import models.boss.Boss;
import server.Maintenance;

public class SkillSummonedManager extends BossManager {

    private static SkillSummonedManager instance;

    public static SkillSummonedManager gI() {
        if (instance == null) {
            instance = new SkillSummonedManager();
        }
        return instance;
    }

    @Override
    public void update() {
        for (int i = this.bosses.size() - 1; i >= 0; i--) {
            if (i < this.bosses.size()) {
                Boss boss = this.bosses.get(i);
                try {
                    boss.update();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        removeBoss(boss);
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }
}

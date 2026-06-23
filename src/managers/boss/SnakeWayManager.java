package managers.boss;

import models.boss.Boss;
import server.Maintenance;

public class SnakeWayManager extends BossManager {

    private static SnakeWayManager instance;

    public static SnakeWayManager gI() {
        if (instance == null) {
            instance = new SnakeWayManager();
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

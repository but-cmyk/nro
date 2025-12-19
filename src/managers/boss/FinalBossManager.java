// use for final boss ví dụ: mabu2h
package managers.boss;

public class FinalBossManager extends BossManager {

    private static FinalBossManager instance;

    public static FinalBossManager gI() {
        if (instance == null) {
            instance = new FinalBossManager();
        }
        return instance;
    }

}

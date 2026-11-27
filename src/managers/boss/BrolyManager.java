//package managers.boss;
//public class BrolyManager extends BossManager {
//
//    private static BrolyManager instance;
//
//    public static BrolyManager gI() {
//        if (instance == null) {
//            instance = new BrolyManager();
//        }
//        return instance;
//    }
//
//}
package managers.boss;

public class BrolyManager extends BossManager {

    private static BrolyManager instance;

    private boolean running = false;

    public static BrolyManager gI() {
        if (instance == null) {
            instance = new BrolyManager();
        }
        return instance;
    }


    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (!running) {
            running = true;
            spawnBroly();
            utils.Logger.log(String.valueOf("Broly đã bắt đầu.") + "\n");
        }
    }

    public void stop() {
        if (running) {
            running = false;
            despawnBroly();
            utils.Logger.log(String.valueOf("Broly đã dừng.") + "\n");
        }
    }


    private void spawnBroly() {
        utils.Logger.log(String.valueOf("Broly đã spawn (fake).") + "\n");
    }


    private void despawnBroly() {
        utils.Logger.log(String.valueOf("Broly đã despawn (fake).") + "\n");
    }
}

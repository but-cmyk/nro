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
            System.out.println("Broly đã bắt đầu.");
        }
    }

    public void stop() {
        if (running) {
            running = false;
            despawnBroly();
            System.out.println("Broly đã dừng.");
        }
    }


    private void spawnBroly() {
        System.out.println("Broly đã spawn (fake).");
    }


    private void despawnBroly() {
        System.out.println("Broly đã despawn (fake).");
    }
}

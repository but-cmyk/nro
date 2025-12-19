//Auto btri 4h30 sáng hàng ngày
package server;

import java.time.LocalTime;
import utils.Logger;

public class AutoBtri extends Thread {

    public static boolean AutoMaintenance = false; // Bật/tắt bảo trì tự động
    public static final int hours = 4; // Giờ bảo trì
    public static final int mins = 59; // Phút bảo trì
    private static AutoBtri instance;
    public static boolean isRunning;

    public static AutoBtri gI() {
        if (instance == null) {
            instance = new AutoBtri();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning && !isRunning) {
            try {
                if (AutoMaintenance) {
                    LocalTime currentTime = LocalTime.now();
                    if (currentTime.getHour() == hours && currentTime.getMinute() == mins) {
                        Logger.log(Logger.PURPLE, "Đang tiến hành quá trình bảo trì tự động\n");
                        Maintenance.gI().start(60);
                        isRunning = true;
                        AutoMaintenance = false;
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

}

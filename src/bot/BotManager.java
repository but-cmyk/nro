package bot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // Import thư viện này
import server.ServerManager;

public class BotManager implements Runnable {

    public static BotManager i;

    // Đổi ArrayList thành CopyOnWriteArrayList để tránh lỗi ConcurrentModificationException
    public List<Bot> bot = new CopyOnWriteArrayList<>();

    public static BotManager gI(){
        if(i == null){
            i = new BotManager();
        }
        return i;
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                // Vòng lặp này bây giờ an toàn dù bot có bị xóa giữa chừng
                for (Bot bot : this.bot) {
                    if (bot != null) {
                        try {
                            bot.update();
                        } catch (Exception ex) {
                            // Bảo vệ 1,000 bots không bị gián đoạn khi 1 bot gặp lỗi cục bộ
                        }
                    }
                }
                long sleepTime = 150 - (System.currentTimeMillis() - st);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                } else {
                    Thread.sleep(5);
                }
            } catch (Exception e) {
                e.printStackTrace(); // In lỗi ra để biết nếu có gì sai
            }
        }
    }
}
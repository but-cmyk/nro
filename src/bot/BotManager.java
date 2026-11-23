package bot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import server.ServerManager;

public class BotManager implements Runnable {

    public static BotManager i;

    // Chức năng Bot đã bị vô hiệu hóa hoàn toàn
    public static volatile boolean ENABLED = false;

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
                if (!ENABLED || this.bot.isEmpty()) {
                    Thread.sleep(2000);
                    continue;
                }
                long st = System.currentTimeMillis();
                for (Bot bot : this.bot) {
                    if (bot != null) {
                        try {
                            bot.update();
                        } catch (Exception ex) {
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
            }
        }
    }

    public void clearAllBots() {
        for (Bot b : this.bot) {
            if (b != null) {
                try {
                    services.map.ChangeMapService.gI().exitMap(b);
                } catch (Exception ignored) {}
            }
        }
        this.bot.clear();
    }
}

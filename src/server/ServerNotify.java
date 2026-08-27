package server;

import java.util.ArrayList;
import java.util.List;
import models.player.Player;
import network.io.Message;
import services.Service;
import utils.Util;

public class ServerNotify extends Thread {

    private long lastNotifyTime;

    private final List<String> notifies;

    private int indexNotify;

    private final String notify[] = {"Dành cho người chơi trên 12 tuổi. Chơi quá 180 phút một ngày sẽ ảnh hưởng đến sức khỏe."};

    private static ServerNotify instance;

    private ServerNotify() {
        this.notifies = new ArrayList<>();
        this.start();
    }

    public static ServerNotify gI() {
        if (instance == null) {
            instance = new ServerNotify();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                if (Util.canDoWithTime(this.lastNotifyTime, 200000)) {
                    sendChatVip(notify[indexNotify]);
                    this.lastNotifyTime = System.currentTimeMillis();
                    indexNotify++;
                    if (indexNotify >= notify.length) {
                        indexNotify = 0;
                    }
                }
                if (!notifies.isEmpty()) {
                    sendChatVip(notifies.removeFirst());
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void sendChatVip(String text) {
        Message msg;
        try {
            msg = new Message(93);
            msg.writer().writeUTF(text);
            Service.gI().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void notify(String text) {
        this.notifies.add(text);
    }

    public void sendNotifyTab(Player player) {
        Message msg;
        try {
            msg = new Message(50);
            java.util.List<String[]> validNotifies = new java.util.ArrayList<>();
            if (Manager.NOTIFY != null) {
                for (int i = 0; i < Manager.NOTIFY.size(); i++) {
                    String raw = Manager.NOTIFY.get(i);
                    if (raw != null) {
                        String[] arr = raw.split("<>");
                        String title = arr.length > 0 ? arr[0] : "";
                        String content = arr.length > 1 ? arr[1] : "";
                        validNotifies.add(new String[]{String.valueOf(i), title, content});
                    }
                }
            }
            msg.writer().writeByte(validNotifies.size());
            for (String[] item : validNotifies) {
                msg.writer().writeShort(Integer.parseInt(item[0]));
                msg.writer().writeUTF(item[1]);
                msg.writer().writeUTF(item[2]);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }
}

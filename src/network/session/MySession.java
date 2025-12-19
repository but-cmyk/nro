package network.session;

import java.net.Socket;

import models.player.Player;
import server.Controller;
import data.DataGame;
import database.daos.NDVSqlFetcher;
import server.Client;
import server.Maintenance;
import server.Manager;
import models.AntiLogin;
import services.Service;
import utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import utils.TimeUtil;

public class MySession extends Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();
    public Player player;

    public byte timeWait = 100;

    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel;

    public long lastTimeLogout;

    public boolean joinedGame;

    public boolean actived;
    public boolean hasReceivedVIP;
    public boolean hasReceivedVIP1;
    public boolean hasReceivedVIP2;
    public long lastTimeReceivedVIP;
    public long lastTimeReceivedVIP1;
    public long lastTimeReceivedVIP2;

    public boolean check;

    public int goldBar;
    public long gold;
    public int eventPoint;
//    public List<Item> itemsReward;
//    public String dataReward;
//    public boolean is_gift_box;
    public double bdPlayer;

    public int version;
    public int cash;
    public int danap;
    public int diemReceive;
    public int sotien;
    public int vip;
    public int vip1;
    public int vip2;
    public int luotquay;

   public boolean finishUpdate;

// logim
    //private static final ConcurrentHashMap<String, Object> soulmateLocks = new ConcurrentHashMap<>();

    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

//    public void sendSessionKey() {
//        Message msg = new Message(-27);
//        try {
//            msg.writer().writeByte(KEYS.length);
//            msg.writer().writeByte(KEYS[0]);
//            for (int i = 1; i < KEYS.length; i++) {
//                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
//            }
//            this.sendMessage(msg);
//            msg.cleanup();
//            sentKey = true;
//        } catch (IOException e) {
//        }
//    }

    public void login(String username, String password) {
        AntiLogin al = ANTILOGIN.computeIfAbsent(this.ipAddress, _ -> new AntiLogin());
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }

        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRunning) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }

        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }

      //  Object lock = soulmateLocks.computeIfAbsent(username, _ -> new Object());

        // Đồng bộ hóa theo tài khoản (khóa theo username)
        synchronized (username.intern()) {
            if (this.player == null) {
                Player pl = null;
                try {
                    System.currentTimeMillis();
                    this.uu = username;
                    this.pp = password;
                    pl = NDVSqlFetcher.login(this, al);
                    if (pl != null) {
                        // -77 max small
                        DataGame.sendSmallVersion(this);
                        // -93 bgitem version
                        DataGame.sendBgItemVersion(this);

                        this.timeWait = 0;
                        this.joinedGame = true;
                        pl.nPoint.calPoint();
                        pl.nPoint.setHp(pl.nPoint.hp);
                        pl.nPoint.setMp(pl.nPoint.mp);
                        pl.zone.addPlayer(pl);
                        if (pl.pet != null) {
                            pl.pet.nPoint.calPoint();
                            pl.pet.nPoint.setHp(pl.pet.nPoint.hp);
                            pl.pet.nPoint.setMp(pl.pet.nPoint.mp);
                        }

                        pl.setSession(this);
                        Client.gI().put(pl);
                        this.player = pl;
                        //-28 -4 version data game
                        DataGame.sendVersionGame(this);
                        //-31 data item background
                        DataGame.sendDataItemBG(this);
                        Controller.gI().sendInfo(this);
                        Logger.log("[" + TimeUtil.getCurrHour() + ":" + TimeUtil.getCurrMin() + "] - Player Login: " + this.player.name + "\n");
                        if (this.player.notify != null && !this.player.notify.equals("null") && !this.player.notify.isEmpty() && this.player.notify.length() > 0) {
                            Service.gI().sendThongBao(this.player, this.player.notify);
                            this.player.notify = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (pl != null) {
                        pl.dispose();
                    }
                }
            }
        }
    }
}

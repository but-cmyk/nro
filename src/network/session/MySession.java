package network.session;

import consts.SocketType;
import interfaces.IKeySessionHandler;
import interfaces.ISession;
import network.io.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Locale;

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
import utils.TimeUtil;

public class MySession implements ISession {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private volatile byte[] KEYS = "NguyenDucVuEntertainment".getBytes();
    private volatile boolean sentKey;
    private volatile boolean connected = true;
    private SocketType socketType = SocketType.SERVER;
    private IKeySessionHandler keyHandler;
    private String ip;
    public int id;

    private static final Map<String, AntiLogin> ANTILOGIN = new ConcurrentHashMap<>();
    private static final Map<String, Object> ACCOUNT_LOCKS = new ConcurrentHashMap<>();
    private static final AtomicInteger LOGIN_ATTEMPTS = new AtomicInteger();
    private static final long LOGIN_GUARD_TTL = 10 * 60 * 1000L;

    public Player player;

    public byte timeWait = 0;
    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel = 2;

    public long lastTimeLogout;
    public boolean joinedGame;

    public boolean actived = true;
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

    public MySession() {
        this.id = ID_GENERATOR.getAndIncrement();
    }

    public MySession(String ip) {
        this();
        this.ip = ip;
        this.ipAddress = ip;
    }

    @Override
    public ISession setKeyHandler(IKeySessionHandler handler) {
        this.keyHandler = handler;
        return this;
    }

    @Override
    public String getIP() {
        return this.ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    @Override
    public long getID() {
        return this.id;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void sendMessage(Message msg) {
    }

    @Override
    public void doSendMessage(Message msg) throws Exception {
    }

    @Override
    public void disconnect() {
        this.connected = false;
        this.sentKey = false;
        this.dispose();
    }

    @Override
    public void dispose() {
        SessionManager.gI().removeSession(this);
        this.ip = null;
    }

    @Override
    public void sendKey() throws Exception {
        if (this.keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        this.keyHandler.sendKey(this);
    }

    @Override
    public void setKey(Message message) throws Exception {
        if (this.keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        this.keyHandler.setKey(this, message);
    }

    @Override
    public void setKey(byte[] key) {
        this.KEYS = key;
    }

    @Override
    public byte[] getKey() {
        return this.KEYS;
    }

    @Override
    public boolean sentKey() {
        return this.sentKey;
    }

    @Override
    public void setSentKey(boolean sent) {
        this.sentKey = sent;
    }

    @Override
    public SocketType getSocketType() {
        return this.socketType;
    }

    public void login(String username, String password) {
        if (username == null || password == null) {
            Service.gI().sendThongBaoOK(this, "Thông tin tài khoản không hợp lệ");
            return;
        }
        username = username.trim().toLowerCase(Locale.ROOT);
        if (username.isEmpty() || username.length() > 50 || password.length() > 128) {
            Service.gI().sendThongBaoOK(this, "Thông tin tài khoản không hợp lệ");
            return;
        }
        if ((LOGIN_ATTEMPTS.incrementAndGet() & 255) == 0) {
            long now = System.currentTimeMillis();
            ANTILOGIN.entrySet().removeIf(entry -> entry.getValue().isExpired(now, LOGIN_GUARD_TTL));
        }
        AntiLogin al = ANTILOGIN.computeIfAbsent(this.ipAddress, k -> new AntiLogin());
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

        if (!this.isAdmin && server.ServerManager.isOverloaded) {
            Service.gI().sendThongBaoOK(this, "Máy chủ đang quá tải tài nguyên, vui lòng thử lại sau giây lát!");
            return;
        }

        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }

        Object accountLock = ACCOUNT_LOCKS.computeIfAbsent(username, k -> new Object());
        try {
            synchronized (accountLock) {
                if (this.player == null) {
                    Player pl = null;
                    try {
                        this.uu = username;
                        this.pp = password;
                        pl = NDVSqlFetcher.login(this, al);
                        if (pl != null) {
                            DataGame.sendSmallVersion(this);
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
                            DataGame.sendVersionGame(this);
                            DataGame.sendDataItemBG(this);
                            Controller.gI().sendInfo(this);
                            Logger.log("[" + TimeUtil.getCurrHour() + ":" + TimeUtil.getCurrMin() + "] - Player Login: " + this.player.name + "\n");
                            if (this.player.notify != null && !this.player.notify.equals("null") && !this.player.notify.isEmpty() && this.player.notify.length() > 0) {
                                Service.gI().sendThongBao(this.player, this.player.notify);
                                this.player.notify = null;
                            }
                        }
                    } catch (Exception e) {
                        Logger.error(pl, "Player login initialization failed for account: " + username, e);
                        if (pl != null) {
                            pl.dispose();
                        }
                    }
                }
            }
        } finally {
            ACCOUNT_LOCKS.remove(username, accountLock);
        }
    }
}

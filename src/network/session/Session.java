package network.session;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import interfaces.IKeySessionHandler;
import interfaces.IMessageHandler;
import interfaces.IMessageSendCollect;
import interfaces.ISession;
import consts.SocketType;
import network.Collector;
import network.QueueHandler;
import network.Sender;
import network.io.Message;

public class Session implements ISession {

    private static ISession instance;
    private static int ID_INIT = 1;
    private SocketType socketType = SocketType.SERVER;
    private byte[] KEYS;
    private boolean sentKey;
    public int id;
    private Socket socket;
    private boolean connected;
    private Sender sender;
    private Collector collector;
    private QueueHandler queueHandler;
    private Thread tSender;
    private Thread tCollector;
    private Thread tQueueHandler;
    private IKeySessionHandler keyHandler;
    private String ip;

    public static ISession gI() throws Exception {
        if (instance == null) {
            throw new Exception("Instance has not been initialized!");
        }
        return instance;
    }

    public Session() {
        this.KEYS = "NguyenDucVuEntertainment".getBytes();
        this.id = ID_INIT++;
        this.socketType = SocketType.SERVER;
        this.connected = true;
    }

    public Session(String ip) {
        this();
        this.ip = ip;
    }

    public Session(Socket socket) {
        this.KEYS = "NguyenDucVuEntertainment".getBytes();
        this.id = ID_INIT++;
        this.socket = socket;
        try {
            this.socket.setSendBufferSize(0x100000);
            this.socket.setReceiveBufferSize(0x100000);
        } catch (SocketException ignored) {
        }
        this.socketType = SocketType.SERVER;
        this.connected = true;
        this.ip = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().replace("/", "");
        this.sender = this.sender != null ? this.sender.setSocket(this.socket) : new Sender(this, this.socket);
        this.collector = this.collector != null ? this.collector.setSocket(this.socket) : new Collector(this, this.socket);
        this.queueHandler = new QueueHandler(this);
        this.tSender = new Thread(this.sender != null ? this.sender.setSocket(this.socket) : (this.sender = new Sender(this, this.socket)), "Sender - IP : " + this.ip);
        this.tCollector = new Thread(this.collector != null ? this.collector.setSocket(this.socket) : (this.collector = new Collector(this, this.socket)), "Collector - IP : " + this.ip);
        this.tQueueHandler = new Thread(this.queueHandler);
    }

    @Override
    public void sendMessage(Message msg) {
        if (this.isConnected() && msg != null) {
            if (this.sender != null) {
                this.sender.sendMessage(msg);
            }
        }
    }

    @Override
    public ISession setSendCollect(IMessageSendCollect collect) {
        if (this.sender != null) this.sender.setSend(collect);
        if (this.collector != null) this.collector.setCollect(collect);
        return this;
    }

    @Override
    public ISession setMessageHandler(IMessageHandler handler) {
        if (this.queueHandler != null) {
            this.queueHandler.setMessageHandler(handler);
        }
        return this;
    }

    @Override
    public ISession setKeyHandler(IKeySessionHandler handler) {
        this.keyHandler = handler;
        return this;
    }

    @Override
    public ISession startSend() {
        if (this.tSender != null) this.tSender.start();
        return this;
    }

    @Override
    public ISession startCollect() {
        if (this.tCollector != null) this.tCollector.start();
        return this;
    }

    @Override
    public ISession startQueueHandler() {
        if (this.tQueueHandler != null) this.tQueueHandler.start();
        return this;
    }

    @Override
    public ISession start() {
        if (this.tSender != null) this.tSender.start();
        if (this.tCollector != null) this.tCollector.start();
        if (this.tQueueHandler != null) this.tQueueHandler.start();
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
    public synchronized void disconnect() {
        if (!this.connected && this.socket == null) {
            return;
        }
        this.connected = false;
        this.sentKey = false;
        if (this.tSender != null) this.tSender.interrupt();
        if (this.tCollector != null) this.tCollector.interrupt();
        if (this.tQueueHandler != null) this.tQueueHandler.interrupt();
        if (this.sender != null) {
            this.sender.close();
        }
        if (this.collector != null) {
            this.collector.close();
        }
        if (this.queueHandler != null) {
            this.queueHandler.close();
        }
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException ignored) {
            }
        }
        this.dispose();
    }

    @Override
    public void dispose() {
        if (this.sender != null) this.sender.dispose();
        if (this.collector != null) this.collector.dispose();
        if (this.queueHandler != null) this.queueHandler.dispose();

        this.socket = null;
        this.sender = null;
        this.collector = null;
        this.queueHandler = null;
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
    public boolean sentKey() {
        return this.sentKey;
    }

    @Override
    public void setSentKey(boolean sent) {
        this.sentKey = sent;
    }

    @Override
    public void doSendMessage(Message msg) throws Exception {
        if (this.sender != null) {
            this.sender.doSendMessage(msg);
        }
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public byte[] getKey() {
        return this.KEYS;
    }

    @Override
    public SocketType getSocketType() {
        return this.socketType;
    }

    @Override
    public QueueHandler getQueueHandler() {
        return this.queueHandler;
    }
}

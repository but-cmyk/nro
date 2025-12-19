package network;

import interfaces.IServerClose;
import java.net.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import interfaces.ISession;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import interfaces.ISessionAcceptHandler;
import interfaces.INetwork;
import network.session.Session;
import network.session.SessionFactory;
import network.session.SessionManager;
import utils.Logger;
import java.util.Iterator;
import java.util.Set;

public class Network implements INetwork, Runnable {

    private static Network instance;
    private int port;
    private ServerSocketChannel serverSocketChannel;
    private Class sessionClone;
    private volatile boolean start; // Sử dụng volatile để thread-safe
    private IServerClose serverClose;
    private ISessionAcceptHandler acceptHandler;
    private Thread loopServer;
    private Selector selector;

    public static Network gI() {
        if (instance == null) {
            synchronized (Network.class) { // Thread-safe singleton
                if (instance == null) {
                    instance = new Network();
                }
            }
        }
        return instance;
    }

    private Network() {
        this.port = -1;
        this.sessionClone = Session.class;
    }

    @Override
    public INetwork init() {
        try {
            this.selector = Selector.open();
        } catch (IOException ex) {
            Logger.errorln(ex.toString());
        }
        this.loopServer = new Thread(this, "Network");
        return this;
    }

    @Override
    public INetwork start(final int port) throws Exception {
        if (port < 0) {
            throw new Exception("Please initialize the server port!");
        }
        if (this.acceptHandler == null) {
            throw new Exception("AcceptHandler has not been initialized!");
        }
        if (!ISession.class.isAssignableFrom(this.sessionClone)) {
            throw new Exception("The type 'session clone' is invalid!");
        }
        try {
            this.port = port;
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
            this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            Logger.error("Error initializing server at port " + port + "\n");
            System.exit(0);
        }
        this.start = true;
        this.loopServer.start();
        Logger.success("Server initialized and listening on port " + this.port + "\n");
        return this;
    }

    @Override
    public INetwork close() {
        this.start = false;
        
        // Đợi thread kết thúc
        if (this.loopServer != null && this.loopServer.isAlive()) {
            try {
                this.loopServer.interrupt();
                this.loopServer.join(5000); // Đợi tối đa 5 giây
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Đóng selector
        if (this.selector != null && this.selector.isOpen()) {
            try {
                this.selector.close();
            } catch (IOException ex) {
                Logger.errorln("Error closing selector: " + ex.getMessage());
            }
        }
        
        // Đóng server socket
        if (this.serverSocketChannel != null) {
            try {
                this.serverSocketChannel.close();
            } catch (IOException ex) {
                Logger.errorln("Error closing server socket: " + ex.getMessage());
            }
        }
        
        if (this.serverClose != null) {
            this.serverClose.serverClose();
        }
        return this;
    }

    @Override
    public INetwork dispose() {
        close(); // Đảm bảo tài nguyên được giải phóng
        this.acceptHandler = null;
        this.loopServer = null;
        this.serverSocketChannel = null;
        this.selector = null;
        this.serverClose = null;
        return this;
    }

    @Override
    public INetwork setAcceptHandler(final ISessionAcceptHandler handler) {
        this.acceptHandler = handler;
        return this;
    }

    @Override
    public void run() {
        while (start && !Thread.currentThread().isInterrupted()) {
            try {
                // Timeout để tránh block vô thời hạn
                int readyChannels = selector.select(1000); 
                
                if (readyChannels == 0) {
                    continue;
                }
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        try {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            Socket socket = server.accept().socket();

                            // Kiểm tra giới hạn connection
                            if (SessionManager.gI().getSessionCount() >= 1000) { // Giới hạn 1000 connections
                                socket.close();
                                Logger.warn();
                                continue;
                            }

                            final ISession session = SessionFactory.gI().cloneSession(this.sessionClone, socket);
                            this.acceptHandler.sessionInit(session);
                            SessionManager.gI().putSession(session);

                        } catch (Exception ex) {
                            Logger.errorln("Error accepting connection: " + ex.getMessage());
                        }
                    }

                    keyIterator.remove(); // Quan trọng: remove key đã xử lý
                }

            } catch (IOException ex) {
                if (start) { // Chỉ log nếu server vẫn đang chạy
                    Logger.errorln("Network loop error: " + ex.getMessage());
                }
            } catch (Exception ex2) {
                Logger.errorln("Unexpected error in network loop: " + ex2.toString());
            }
        }

        // Cleanup khi thoát loop
        cleanup();
    }
    
    private void cleanup() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (IOException ex) {
            Logger.errorln("Error in cleanup: " + ex.getMessage());
        }
    }

    @Override
    public INetwork setDoSomeThingWhenClose(final IServerClose serverClose) {
        this.serverClose = serverClose;
        return this;
    }

    @Override
    public INetwork setTypeSessionClone(final Class clazz) throws Exception {
        this.sessionClone = clazz;
        return this;
    }

    @Override
    public ISessionAcceptHandler getAcceptHandler() throws Exception {
        if (this.acceptHandler == null) {
            throw new Exception("AcceptHandler has not been initialized!");
        }
        return this.acceptHandler;
    }

    @Override
    public void stopConnect() {
        this.start = false;
        if (this.loopServer != null) {
            this.loopServer.interrupt();
        }
    }
}
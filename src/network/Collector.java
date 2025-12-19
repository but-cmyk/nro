package network;

import java.net.Socket;
import lombok.Setter;
import java.io.DataInputStream;
import java.io.IOException;
import interfaces.IMessageSendCollect;
import interfaces.ISession;
import consts.Cmd_message;
import consts.SocketType;
import network.io.Message;
import utils.Logger;

public final class Collector implements Runnable {
    private volatile ISession session; // Volatile để thread-safe
    private DataInputStream dis;
    @Setter
    private IMessageSendCollect collect;
    private volatile boolean running = true; // Control flag

    public Collector(ISession session, Socket socket) {
        this.session = session;
        this.setSocket(socket);
    }

    public Collector setSocket(Socket socket) {
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.errorln("Error setting socket: " + ex.getMessage());
        }
        return this;
    }

    @Override
    public void run() {
        try {
            while (running && session != null && session.isConnected() && !Thread.currentThread().isInterrupted()) {
                try {
                    final Message msg = this.collect.readMessage(this.session, this.dis);

                    // Kiểm tra message null
                    if (msg == null) {
                        break;
                    }

                    if (msg.command == Cmd_message.GET_SESSION_ID) {
                        try {
                            if (session.getSocketType() == SocketType.SERVER) {
                                this.session.sendKey();
                            } else {
                                this.session.setKey(msg);
                            }
                        } finally {
                            msg.cleanup(); // Đảm bảo cleanup trong finally
                        }
                    } else {
                        // Kiểm tra queue size trước khi add
                        if (this.session.getQueueHandler() != null) {
                            this.session.getQueueHandler().addMessage(msg);
                        } else {
                            msg.cleanup(); // Cleanup nếu không có queue handler
                        }
                    }

                } catch (IOException ex) {
                    // Connection đã đóng
                    break;
                } catch (Exception ex) {
                    Logger.errorln("Error processing message: " + ex.getMessage());
                    // Tiếp tục loop thay vì break
                }
            }
        } catch (Exception ex) {
            Logger.errorln("Collector thread error: " + ex.getMessage());
        } finally {
            // Cleanup trong finally block
            cleanup();
        }
    }

    private void cleanup() {
        running = false;

        // Thông báo session disconnect
        try {
            if (session != null) {
                Network.gI().getAcceptHandler().sessionDisconnect(session);
            }
        } catch (Exception ex) {
            Logger.errorln("Error notifying session disconnect: " + ex.getMessage());
        }

        // Disconnect session
        if (this.session != null) {
            this.session.disconnect();
        }

        // Close input stream
        close();
    }

    public void close() {
        running = false;
        if (dis != null) {
            try {
                dis.close();
            } catch (IOException ex) {
                Logger.errorln("Error closing DataInputStream: " + ex.getMessage());
            }
        }
    }

    public void dispose() {
        running = false;
        close();
        session = null;
        dis = null;
        collect = null;
    }

    public void stop() {
        running = false;
    }
}
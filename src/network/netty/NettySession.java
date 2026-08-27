package network.netty;

import io.netty.channel.Channel;
import network.io.Message;
import network.session.MySession;
import network.session.SessionManager;
import server.Controller;
import utils.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NettySession: Quản lý phiên kết nối của người chơi dựa trên Netty Channel.
 * Tích hợp Session Mailbox (Lock-free FIFO Queue) đảm bảo xử lý gói tin tuần tự,
 * triệt tiêu hoàn toàn Out-Of-Order Race Conditions khi chạy trên Java 21 Virtual Threads.
 */
public class NettySession extends MySession {

    private final Channel channel;
    private final ConcurrentLinkedQueue<Message> messageMailbox = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public NettySession(Channel channel) {
        super();
        this.channel = channel;
        if (channel != null && channel.remoteAddress() instanceof InetSocketAddress addr) {
            String rawIp = addr.getAddress().getHostAddress();
            this.setIP(rawIp);
            this.ipAddress = rawIp;
        }
    }

    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public boolean isConnected() {
        return this.channel != null && this.channel.isActive();
    }

    /**
     * Nhận gói tin từ Netty I/O thread và đưa vào Mailbox xử lý tuần tự (FIFO).
     */
    public void enqueueMessage(Message msg) {
        if (msg == null) {
            return;
        }
        if (!this.isConnected()) {
            msg.cleanup();
            return;
        }
        messageMailbox.offer(msg);
        tryProcessNextMessage();
    }

    /**
     * Kích hoạt Virtual Thread xử lý toàn bộ Mailbox của Session này.
     * Đảm bảo chỉ có DUY NHẤT 1 luồng xử lý gói tin cho 1 session tại 1 thời điểm.
     */
    private void tryProcessNextMessage() {
        if (isProcessing.compareAndSet(false, true)) {
            Thread.ofVirtual().name("session-mailbox-" + (this.userId != 0 ? this.userId : this.getIP())).start(() -> {
                try {
                    Message currentMsg;
                    while ((currentMsg = messageMailbox.poll()) != null) {
                        try {
                            if (currentMsg.command == -27) { // Cmd_message.GET_SESSION_ID
                                this.sendKey();
                            } else {
                                Controller.gI().onMessage(this, currentMsg);
                            }
                        } catch (Exception e) {
                            Logger.logException(NettySession.class, e, "Lỗi xử lý packet cmd " + currentMsg.command + " cho IP " + this.getIP());
                        } finally {
                            currentMsg.cleanup();
                        }
                    }
                } finally {
                    isProcessing.set(false);
                    // Kiểm tra lại nếu có gói tin mới đến ngay trước khi nhả lock
                    if (!messageMailbox.isEmpty()) {
                        tryProcessNextMessage();
                    }
                }
            });
        }
    }

    @Override
    public void sendMessage(Message msg) {
        if (this.isConnected() && msg != null) {
            this.channel.writeAndFlush(msg);
        }
    }

    @Override
    public void doSendMessage(Message msg) throws Exception {
        this.sendMessage(msg);
    }

    @Override
    public synchronized void disconnect() {
        if (!this.isConnected()) {
            return;
        }
        this.setConnected(false);
        this.setSentKey(false);
        if (this.channel != null) {
            this.channel.close();
        }
        this.dispose();
    }

    @Override
    public void dispose() {
        SessionManager.gI().removeSession(this);
        // Dọn sạch các packet còn tồn đọng trong mailbox khi disconnect
        Message remainingMsg;
        while ((remainingMsg = messageMailbox.poll()) != null) {
            remainingMsg.cleanup();
        }
        this.setIP(null);
        this.ipAddress = null;
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
    }
}

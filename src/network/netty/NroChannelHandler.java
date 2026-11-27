package network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import network.io.Message;
import network.io.MyKeyHandler;
import network.session.SessionManager;
import server.Client;
import server.Controller;
import server.ServerManager;
import utils.Logger;

import java.io.IOException;

/**
 * NroChannelHandler: Xử lý vòng đời kết nối và phân phối Message tới Controller.
 * Tách biệt luồng I/O mạng của Netty và Game Worker Pool.
 */
public class NroChannelHandler extends SimpleChannelInboundHandler<Message> {

    public static final AttributeKey<NettySession> SESSION_ATTR = AttributeKey.valueOf("NETTY_SESSION");
    private int idleTicks = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettySession session = ctx.channel().attr(SESSION_ATTR).get();
        if (session == null) {
            session = new NettySession(ctx.channel());
            ctx.channel().attr(SESSION_ATTR).set(session);
        }

        // 1. Kiểm tra giới hạn kết nối IP
        if (!ServerManager.canConnectWithIp(session.getIP())) {
            Logger.warning("Chặn kết nối từ IP quá giới hạn: " + session.getIP());
            ctx.channel().attr(SESSION_ATTR).set(null);
            ctx.close();
            return;
        }

        // 2. Gán KeyHandler và thêm Session vào Manager
        session.setKeyHandler(new MyKeyHandler());
        SessionManager.gI().addSession(session);

        // 3. Không gửi Session Key Handshake ngay lập tức (Chờ client gửi Message -27)
        // session.sendKey();
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        NettySession session = ctx.channel().attr(SESSION_ATTR).get();
        if (session == null || !session.isConnected()) {
            msg.cleanup();
            return;
        }

        this.idleTicks = 0;
        // Đẩy vào Mailbox của Session để xử lý tuần tự (FIFO In-Order) trên Virtual Thread
        session.enqueueMessage(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettySession session = ctx.channel().attr(SESSION_ATTR).get();
        if (session != null) {
            try {
                Client.gI().remove(session);
                ServerManager.gI().disconnect(session);
            } catch (Exception e) {
                Logger.logException(NroChannelHandler.class, e, "Lỗi disconnect session: " + session.getIP());
            } finally {
                session.dispose();
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleEvent) {
            if (idleEvent.state() == IdleState.READER_IDLE) {
                idleTicks++;
                NettySession session = ctx.channel().attr(SESSION_ATTR).get();
                // Pre-auth zombie: ngắt ngay sau 30s (1 tick). Đã login: ngắt sau 180s (6 ticks)
                if (session == null || session.player == null || idleTicks >= 6) {
                    Logger.warning("Đóng kết nối Idle (" + (idleTicks * 30) + "s) từ " + ctx.channel().remoteAddress());
                    ctx.close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // Ngắt kết nối socket thông thường (đóng app, mất mạng)
            ctx.close();
        } else {
            Logger.error("Netty Exception trên " + ctx.channel().remoteAddress(), cause);
            ctx.close();
        }
    }
}

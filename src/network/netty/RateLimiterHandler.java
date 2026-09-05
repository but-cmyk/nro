package network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import network.io.Message;
import utils.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bộ lọc giới hạn tần suất gửi packet (Rate Limiter) chống Flood / DDoS / Auto spam.
 * Đặt sau Decoder để lọc trực tiếp trên Game Message, triệt tiêu 100% rò rỉ ByteBuf.
 */
public class RateLimiterHandler extends SimpleChannelInboundHandler<Message> {

    private final int maxPacketsPerSecond;
    private final AtomicInteger packetCounter = new AtomicInteger(0);
    private long currentWindowSecond = System.currentTimeMillis() / 1000;

    public RateLimiterHandler(int maxPacketsPerSecond) {
        super(false); // Không tự động release vì cần chuyển tiếp Message cho Handler phía sau
        this.maxPacketsPerSecond = maxPacketsPerSecond;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        long currentSecond = System.currentTimeMillis() / 1000;
        if (currentSecond != currentWindowSecond) {
            currentWindowSecond = currentSecond;
            packetCounter.set(0);
        }

        int count = packetCounter.incrementAndGet();
        if (count > maxPacketsPerSecond) {
            Logger.warning("Phát hiện spam packet (" + count + " pkts/s) từ " + ctx.channel().remoteAddress() + " -> Đóng kết nối!");
            msg.cleanup();
            ctx.close();
            return;
        }

        ctx.fireChannelRead(msg); // Chuyển tiếp Message hợp lệ sang NroChannelHandler
    }
}

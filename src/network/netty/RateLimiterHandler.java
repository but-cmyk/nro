package network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import utils.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bộ lọc giới hạn tần suất gửi packet (Rate Limiter) chống Flood / DDoS / Auto spam.
 */
public class RateLimiterHandler extends ChannelInboundHandlerAdapter {

    private final int maxPacketsPerSecond;
    private final AtomicInteger packetCounter = new AtomicInteger(0);
    private volatile long currentWindowSecond = System.currentTimeMillis() / 1000;

    public RateLimiterHandler(int maxPacketsPerSecond) {
        this.maxPacketsPerSecond = maxPacketsPerSecond;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        long currentSecond = System.currentTimeMillis() / 1000;
        if (currentSecond != currentWindowSecond) {
            currentWindowSecond = currentSecond;
            packetCounter.set(0);
        }

        int count = packetCounter.incrementAndGet();
        if (count > maxPacketsPerSecond) {
            Logger.warning("Phát hiện spam packet (" + count + " pkts/s) từ " + ctx.channel().remoteAddress() + " -> Đóng kết nối!");
            ctx.close();
            return;
        }

        super.channelRead(ctx, msg);
    }
}

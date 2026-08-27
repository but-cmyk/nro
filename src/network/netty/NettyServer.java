package network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import utils.Logger;

import java.util.concurrent.TimeUnit;

/**
 * NettyServer: Máy chủ Socket Non-blocking I/O hiệu năng cao thay thế toàn bộ hệ thống socket cũ.
 */
public class NettyServer {

    private static volatile NettyServer instance;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean isRunning = false;

    public static NettyServer gI() {
        if (instance == null) {
            synchronized (NettyServer.class) {
                if (instance == null) {
                    instance = new NettyServer();
                }
            }
        }
        return instance;
    }

    private NettyServer() {
    }

    public synchronized void start(int port) throws Exception {
        if (this.isRunning) {
            Logger.warning("NettyServer đã đang chạy!");
            return;
        }

        // 1 Boss thread để accept kết nối, Worker threads để xử lý I/O
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // Tối ưu hóa Socket Options
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            NettySession session = new NettySession(ch);
                            ch.attr(NroChannelHandler.SESSION_ATTR).set(session);

                            ChannelPipeline pipeline = ch.pipeline();
                            // 1. Kiểm tra Zombie Connection (ngắt nếu không có data sau 180s)
                            pipeline.addLast("idleStateHandler", new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS));
                            // 2. Bộ lọc chống Spam/Flood packet (>100 pkts/s)
                            pipeline.addLast("rateLimiter", new RateLimiterHandler(100));
                            // 3. Decoder & Encoder theo từng Session riêng biệt (Thread-safe XOR keys)
                            pipeline.addLast("decoder", new NroPacketDecoder(session));
                            pipeline.addLast("encoder", new NroPacketEncoder(session));
                            // 4. Handler nhận message và chuyển tiếp tới Controller
                            pipeline.addLast("handler", new NroChannelHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            this.serverChannel = f.channel();
            this.isRunning = true;
            Logger.success(">> [Netty 4.x] Server đã khởi động thành công và lắng nghe trên PORT: " + port + "\n");
        } catch (Exception e) {
            this.close();
            throw e;
        }
    }

    public synchronized void close() {
        this.isRunning = false;
        if (this.serverChannel != null) {
            try {
                this.serverChannel.close().sync();
            } catch (Exception ignored) {
            }
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
        }
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
        }
        Logger.log(">> [Netty 4.x] Đã đóng NettyServer hoàn tất.");
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}

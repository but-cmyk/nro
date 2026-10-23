package network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.timeout.IdleStateHandler;
import server.Manager;
import utils.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * NettyServer: Máy chủ Socket Non-blocking I/O hiệu năng cao thay thế toàn bộ hệ thống socket cũ.
 */
public class NettyServer {

    private static volatile NettyServer instance;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private Channel tlsServerChannel;
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
        start(port, Manager.TLS_ENABLED, Manager.TLS_PORT);
    }

    public synchronized void start(int port, boolean tls) throws Exception {
        start(port, tls, 0);
    }

    public synchronized void start(int port, boolean tls, int tlsPort) throws Exception {
        if (this.isRunning) {
            Logger.warning("NettyServer đã đang chạy!");
            return;
        }

        final boolean dualPort = (tlsPort > 0 && tlsPort != port);
        final SslContext sslContext;
        if (tls || dualPort) {
            File cert = new File(Manager.TLS_CERT);
            File key = new File(Manager.TLS_KEY);
            if (!cert.isFile() || !key.isFile()) {
                throw new IllegalStateException("TLS đang bật nhưng không thấy cert/key: "
                        + cert.getAbsolutePath() + " / " + key.getAbsolutePath());
            }
            sslContext = SslContextBuilder.forServer(cert, key)
                    .sslProvider(SslProvider.JDK)
                    .protocols("TLSv1.3", "TLSv1.2")
                    .build();
        } else {
            sslContext = null;
        }

        // 1 Boss thread để accept kết nối, Worker threads để xử lý I/O
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        try {
            if (dualPort) {
                // Dual-port mode: Legacy port (plaintext) + TLS port chạy song song trong 30 ngày
                ServerBootstrap bPlain = createBootstrap(bossGroup, workerGroup, null, false);
                this.serverChannel = bPlain.bind(port).sync().channel();
                Logger.success(">> [Netty 4.x] Server lắng nghe PORT legacy (plaintext): " + port);

                ServerBootstrap bTls = createBootstrap(bossGroup, workerGroup, sslContext, true);
                this.tlsServerChannel = bTls.bind(tlsPort).sync().channel();
                Logger.success(">> [Netty 4.x] Server lắng nghe PORT TLS (1.2/1.3): " + tlsPort + "\n");
            } else {
                // Single-port mode: Toàn bộ kết nối chạy qua 1 cổng (hoặc TLS hoặc plaintext)
                boolean isTls = (sslContext != null);
                ServerBootstrap b = createBootstrap(bossGroup, workerGroup, sslContext, isTls);
                this.serverChannel = b.bind(port).sync().channel();
                Logger.success(">> [Netty 4.x] Server đã khởi động thành công và lắng nghe trên PORT: "
                        + port + (isTls ? " (TLS 1.2/1.3)" : " (plaintext)") + "\n");
            }
            this.isRunning = true;
        } catch (Exception e) {
            this.close();
            throw e;
        }
    }

    private ServerBootstrap createBootstrap(EventLoopGroup boss, EventLoopGroup worker,
                                            SslContext sslContext, boolean isTlsSession) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                // Tối ưu hóa Socket Options
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        NettySession session = new NettySession(ch);
                        session.isTls = isTlsSession;
                        ch.attr(NroChannelHandler.SESSION_ATTR).set(session);

                        ChannelPipeline pipeline = ch.pipeline();
                        // 0. TLS phải là handler đầu tiên để giải mã toàn bộ traffic nếu có SslContext
                        if (sslContext != null) {
                            SslHandler sslHandler = sslContext.newHandler(ch.alloc());
                            sslHandler.setHandshakeTimeout(10, TimeUnit.SECONDS);
                            pipeline.addLast("ssl", sslHandler);
                        }
                        // 1. Kiểm tra Zombie Connection (ngắt nếu không có data sau 180s)
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS));
                        // 2. Decoder & Encoder theo từng Session riêng biệt (Thread-safe XOR keys)
                        pipeline.addLast("decoder", new NroPacketDecoder(session));
                        pipeline.addLast("encoder", new NroPacketEncoder(session));
                        // 3. Bộ lọc chống Spam/Flood packet áp dụng trên Game Message (>120 pkts/s)
                        pipeline.addLast("rateLimiter", new RateLimiterHandler(120));
                        // 4. Handler nhận message và chuyển tiếp tới Controller
                        pipeline.addLast("handler", new NroChannelHandler());
                    }
                });
        return b;
    }

    public synchronized void close() {
        this.isRunning = false;
        if (this.serverChannel != null) {
            try {
                this.serverChannel.close().sync();
            } catch (Exception ignored) {
            }
        }
        if (this.tlsServerChannel != null) {
            try {
                this.tlsServerChannel.close().sync();
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

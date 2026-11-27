package network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import utils.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * NettyServer: Máy chủ Socket Non-blocking I/O hiệu năng cao hỗ trợ Dual-Port (TCP + TLS 1.2/1.3).
 */
public class NettyServer {

    private static volatile NettyServer instance;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private Channel tlsChannel;
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
        start(port, false, 14446, null, null);
    }

    public synchronized void start(int port, boolean tlsEnabled, int tlsPort, String certPath, String keyPath)
            throws Exception {
        if (this.isRunning) {
            Logger.warning("NettyServer đã đang chạy!");
            return;
        }

        // 1 Boss thread để accept kết nối, 2 Worker threads tối ưu cho VPS 2-4 CPU
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(2);

        try {
            ServerBootstrap b = createBootstrap(null);
            ChannelFuture f = b.bind(port).sync();
            this.serverChannel = f.channel();
            this.isRunning = true;
            Logger.success(">> [Netty 4.x] Server đã khởi động thành công và lắng nghe trên PORT: " + port + "\n");

            if (tlsEnabled && tlsPort > 0 && tlsPort != port) {
                startTlsListener(tlsPort, certPath, keyPath);
            }
        } catch (Exception e) {
            this.close();
            throw e;
        }
    }

    private ServerBootstrap createBootstrap(SslContext sslCtx) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
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
                        ch.attr(NroChannelHandler.SESSION_ATTR).set(session);

                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslCtx != null) {
                            pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()));
                        }
                        // 1. Kiểm tra Idle mỗi 30s (Pre-auth đóng sau 30s, đã login đóng sau 180s)
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
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

    private void startTlsListener(int tlsPort, String certPath, String keyPath) {
        if (certPath == null || keyPath == null) {
            Logger.warning(">> [Netty TLS] Thiếu đường dẫn cert/key, bỏ qua listener TLS port " + tlsPort + "\n");
            return;
        }
        File certFile = new File(certPath);
        File keyFile = new File(keyPath);
        if (!certFile.exists() || !keyFile.exists()) {
            Logger.warning(">> [Netty TLS] Không tìm thấy chứng chỉ (" + certPath + ", " + keyPath
                    + "), chỉ chạy cổng TCP.\n");
            return;
        }
        try {
            SslContext sslCtx = SslContextBuilder.forServer(certFile, keyFile)
                    .protocols("TLSv1.3", "TLSv1.2")
                    .build();
            ChannelFuture tlsFuture = createBootstrap(sslCtx).bind(tlsPort).sync();
            this.tlsChannel = tlsFuture.channel();
            Logger.success(">> [Netty TLS] Đã bật Dual-Port TLS 1.2/1.3 trên PORT: " + tlsPort + "\n");
        } catch (Exception e) {
            Logger.warning(">> [Netty TLS] Không thể mở cổng TLS " + tlsPort + ": " + e.getMessage() + "\n");
        }
    }

    public synchronized void close() {
        this.isRunning = false;
        if (this.tlsChannel != null) {
            try {
                this.tlsChannel.close().sync();
            } catch (Exception ignored) {
            }
            this.tlsChannel = null;
        }
        if (this.serverChannel != null) {
            try {
                this.serverChannel.close().sync();
            } catch (Exception ignored) {
            }
            this.serverChannel = null;
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

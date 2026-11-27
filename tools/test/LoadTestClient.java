package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Công cụ Load Test TCP (mô phỏng 500-1000+ CCU handshake -27 đồng thời)
 * sử dụng Java 21 Virtual Threads để đo ngưỡng chịu tải và độ trễ của NettyServer.
 *
 * Chạy thủ công khi Server đang bật:
 *   java -cp build/classes test.LoadTestClient 127.0.0.1 14445 500
 */
public class LoadTestClient {

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 14445;
        int totalClients = args.length > 2 ? Integer.parseInt(args[2]) : 500;

        System.out.println("============================================================");
        System.out.println("  NRO LOAD TEST CLIENT: " + totalClients + " CCU -> " + host + ":" + port);
        System.out.println("============================================================");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Long> latenciesMs = Collections.synchronizedList(new ArrayList<>(totalClients));
        CountDownLatch latch = new CountDownLatch(totalClients);

        long startWall = System.currentTimeMillis();
        for (int i = 0; i < totalClients; i++) {
            Thread.ofVirtual().name("load-client-" + i).start(() -> {
                long t0 = System.nanoTime();
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), 5000);
                    socket.setSoTimeout(5000);
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    // Send NRO handshake opcode -27 (unencrypted, 0-length payload)
                    dos.writeByte((byte) -27);
                    dos.writeShort(0);
                    dos.flush();

                    // Expect server -27 key response header
                    byte cmd = dis.readByte();
                    int len = dis.readUnsignedShort();
                    if (cmd == (byte) -27 && len > 0 && len < 4096) {
                        byte[] buf = new byte[len];
                        dis.readFully(buf);
                        successCount.incrementAndGet();
                        latenciesMs.add((System.nanoTime() - t0) / 1_000_000L);
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        long totalWall = System.currentTimeMillis() - startWall;

        Collections.sort(latenciesMs);
        long p50 = latenciesMs.isEmpty() ? -1 : latenciesMs.get(latenciesMs.size() / 2);
        long p95 = latenciesMs.isEmpty() ? -1 : latenciesMs.get((int) (latenciesMs.size() * 0.95));
        long max = latenciesMs.isEmpty() ? -1 : latenciesMs.get(latenciesMs.size() - 1);

        System.out.println("Elapsed Wall Time : " + totalWall + " ms");
        System.out.println("Handshake Success : " + successCount.get() + " / " + totalClients);
        System.out.println("Handshake Failed  : " + failCount.get());
        System.out.println("Latency (ms)      : p50=" + p50 + ", p95=" + p95 + ", max=" + max);
        System.exit(failCount.get() == 0 ? 0 : 1);
    }
}

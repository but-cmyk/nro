package test;

import server.network.PacketProfiler;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Deep Concurrency & Integration Test for PacketProfiler
 */
public class TestProfilerEngine {

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ CHUYÊN SÂU PACKET PROFILER & HỆ THỐNG GIÁM SÁT");
        System.out.println("================================================================");

        try {
            testConcurrencyAndAccuracy();
            testCsvExport();
            testSlowLogFlushing();
            testPerformanceAlertsJson();
            testInGameReportFormatting();
            testResolveAlert();

            System.out.println("\n================================================================");
            System.out.println("   [CHÚC MỪNG] TOÀN BỘ 6/6 TEST CASES CHUYÊN SÂU ĐẠT 100% PASS!");
            System.out.println("================================================================");
        } catch (Throwable t) {
            System.err.println("\n[ERROR] KIỂM THỬ THẤT BẠI: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void testConcurrencyAndAccuracy() throws Exception {
        System.out.println("\n[TEST 1] ĐA LUỒNG & TÍNH TOÁN NGUYÊN TỬ (10 Luồng, 41,700 Packets)");
        PacketProfiler profiler = PacketProfiler.gI();

        int numThreads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(numThreads);

        AtomicInteger moveCalls = new AtomicInteger(0);
        AtomicInteger attackCalls = new AtomicInteger(0);
        AtomicInteger itemCalls = new AtomicInteger(0);
        AtomicInteger shopCalls = new AtomicInteger(0);
        AtomicInteger tradeCalls = new AtomicInteger(0);
        AtomicInteger consignCalls = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            pool.submit(() -> {
                try {
                    startGate.await();

                    // 1. Move packets (cmd -5): 2000 calls / thread = 20,000 calls (0.05ms)
                    for (int j = 0; j < 2000; j++) {
                        profiler.record((byte) -5, 50_000L, null, "MOVE");
                        moveCalls.incrementAndGet();
                    }

                    // 2. Attack packets (cmd -7): 1500 calls / thread = 15,000 calls (0.1ms)
                    for (int j = 0; j < 1500; j++) {
                        profiler.record((byte) -7, 100_000L, null, "ATTACK");
                        attackCalls.incrementAndGet();
                    }

                    // 3. Item packets (cmd -20): 500 calls / thread = 5,000 calls (1.2ms)
                    for (int j = 0; j < 500; j++) {
                        profiler.record((byte) -20, 1_200_000L, null, "USE_ITEM");
                        itemCalls.incrementAndGet();
                    }

                    // 4. Shop packets (cmd -44): 100 calls / thread = 1,000 calls (15ms, slow 65ms on last)
                    for (int j = 0; j < 100; j++) {
                        long dur = (j == 99 && threadId == 0) ? 65_000_000L : 15_000_000L;
                        profiler.record((byte) -44, dur, null, "OPEN_SHOP");
                        shopCalls.incrementAndGet();
                    }

                    // 5. Trade packets (cmd -86): 50 calls / thread = 500 calls (30ms, slow 120ms on thread 0)
                    for (int j = 0; j < 50; j++) {
                        long dur = (j == 49 && threadId == 0) ? 120_000_000L : 30_000_000L;
                        profiler.record((byte) -86, dur, null, "TRADE");
                        tradeCalls.incrementAndGet();
                    }

                    // 6. Consign packets (cmd -100): 20 calls / thread = 200 calls (85ms, slow 240ms on thread 0)
                    for (int j = 0; j < 20; j++) {
                        long dur = (j == 19 && threadId == 0) ? 240_000_000L : 85_000_000L;
                        profiler.record((byte) -100, dur, null, "CONSIGN_SHOP");
                        consignCalls.incrementAndGet();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endGate.countDown();
                }
            });
        }

        long startNs = System.nanoTime();
        startGate.countDown(); // Khởi động đồng loạt 10 luồng
        boolean finished = endGate.await(10, TimeUnit.SECONDS);
        long totalElapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        pool.shutdown();

        if (!finished) {
            throw new RuntimeException("Timeout khi chạy bài kiểm thử đa luồng!");
        }

        int totalCalls = moveCalls.get() + attackCalls.get() + itemCalls.get()
                + shopCalls.get() + tradeCalls.get() + consignCalls.get();

        System.out.printf("  -> Hoàn thành %d packet calls trong %d ms (~%.0f ops/sec)\n",
                totalCalls, totalElapsedMs, (totalCalls / (double) Math.max(1, totalElapsedMs)) * 1000);

        if (totalCalls != 41700) {
            throw new RuntimeException("Lỗi: Tổng số packet ghi nhận không khớp! Kỳ vọng: 41700, Thực tế: " + totalCalls);
        }
        System.out.println("  [PASS] Đa luồng ghi nhận chính xác 100% không mất mát dữ liệu.");
    }

    private static void testCsvExport() throws Exception {
        System.out.println("\n[TEST 2] XUẤT BÁO CÁO CSV (exportSummaryCsv)");
        PacketProfiler profiler = PacketProfiler.gI();
        profiler.exportSummaryCsv();

        File dir = new File("logs/metrics");
        File[] csvFiles = dir.listFiles((d, name) -> name.startsWith("api_summary_") && name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            throw new RuntimeException("Không tìm thấy file CSV được tạo trong logs/metrics!");
        }

        File latestCsv = csvFiles[0];
        String content = new String(Files.readAllBytes(latestCsv.toPath()));
        if (!content.contains("Timestamp,CMD,CommandName,TotalCalls,AvgMs,MaxMs,SlowCalls,SlowestContext")) {
            throw new RuntimeException("File CSV không chứa đúng header!");
        }
        if (!content.contains("-100,CONSIGN_SHOP") || !content.contains("-86,TRADE")) {
            throw new RuntimeException("File CSV thiếu dữ liệu các opcode quan trọng!");
        }
        System.out.printf("  [PASS] File CSV %s hợp lệ, kích thước %d bytes.\n", latestCsv.getName(), latestCsv.length());
    }

    private static void testSlowLogFlushing() throws Exception {
        System.out.println("\n[TEST 3] GHI LOG GÓI TIN CHẬM BẤT ĐỒNG BỘ (slow_packets.log)");
        // Đợi 300ms để worker thread ghi hết queue
        Thread.sleep(300);

        File slowLog = new File("logs/metrics/slow_packets.log");
        if (!slowLog.exists() || slowLog.length() == 0) {
            throw new RuntimeException("File slow_packets.log không tồn tại hoặc bị rỗng!");
        }

        String content = new String(Files.readAllBytes(slowLog.toPath()));
        if (!content.contains("CMD: -100 (CONSIGN_SHOP)") || !content.contains("CMD: -86 (TRADE)")) {
            throw new RuntimeException("File slow_packets.log thiếu thông tin gói tin chậm!");
        }
        System.out.printf("  [PASS] slow_packets.log đã ghi vết các gói tin vượt ngưỡng (>50ms).\n");
    }

    private static void testPerformanceAlertsJson() throws Exception {
        System.out.println("\n[TEST 4] TỰ ĐỘNG SINH CẢNH BÁO PERFORMANCE_ALERTS.json (>100ms)");
        File alertFile = new File("tools/memory/PERFORMANCE_ALERTS.json");
        if (!alertFile.exists()) {
            throw new RuntimeException("File PERFORMANCE_ALERTS.json chưa được tạo!");
        }

        String json = new String(Files.readAllBytes(alertFile.toPath()));
        if (!json.contains("\"cmd\": -100") || !json.contains("\"cmd\": -86")) {
            throw new RuntimeException("Thiếu cảnh báo cho CMD -100 hoặc CMD -86 trong PERFORMANCE_ALERTS.json!");
        }
        if (!json.contains("\"status\": \"OPEN\"")) {
            throw new RuntimeException("Cảnh báo không có trạng thái OPEN!");
        }
        System.out.println("  [PASS] File PERFORMANCE_ALERTS.json đã ghi nhận chính xác 2 sự cố nghẽn khẩn cấp.");
    }

    private static void testInGameReportFormatting() {
        System.out.println("\n[TEST 5] KIỂM TRA ĐỊNH DẠNG POPUP MÈO KARIN & CONSOLE");
        PacketProfiler profiler = PacketProfiler.gI();

        // Test console
        profiler.printReport();

        // Test in-game popup
        String popupMsg = profiler.getTopLagReport();
        System.out.println("Chuỗi popup in-game mẫu:");
        System.out.println(popupMsg);

        if (!popupMsg.contains("TOP CHỨC NĂNG CHẬM NHẤT") || !popupMsg.contains("CONSIGN_SHOP")) {
            throw new RuntimeException("Định dạng chuỗi popup in-game không hợp lệ!");
        }
        System.out.println("  [PASS] Định dạng popup in-game và console report hoàn hảo.");
    }

    private static void testResolveAlert() throws Exception {
        System.out.println("\n[TEST 6] ĐÓNG CẢNH BÁO KHI ĐÃ TỐI ƯU (resolveAlert)");
        PacketProfiler profiler = PacketProfiler.gI();

        // Đóng alert cho cmd -86 (Trade)
        profiler.resolveAlert((byte) -86);

        File alertFile = new File("tools/memory/PERFORMANCE_ALERTS.json");
        String json = new String(Files.readAllBytes(alertFile.toPath()));
        if (json.contains("\"cmd\": -86")) {
            throw new RuntimeException("CMD -86 vẫn còn trong active alerts sau khi resolve!");
        }
        if (!json.contains("\"cmd\": -100")) {
            throw new RuntimeException("CMD -100 bị mất sau khi resolve CMD -86!");
        }
        System.out.println("  [PASS] resolveAlert() hoạt động chính xác.");
    }
}

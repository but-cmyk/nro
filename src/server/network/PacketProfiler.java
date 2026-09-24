package server.network;

import models.player.Player;
import utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Module Giám Sát Hiệu Năng Gói Tin / API (Latency Profiler)
 * - Zero-Allocation trong luồng nóng (Hot Path)
 * - Ghi log bất đồng bộ qua BlockingQueue, không block Netty IO Thread
 * - Tự động phát hiện và ghi nhận sự cố nghẽn vào tools/memory/PERFORMANCE_ALERTS.json
 */
public class PacketProfiler {

    private static final PacketProfiler instance = new PacketProfiler();

    public static PacketProfiler gI() {
        return instance;
    }

    public static final long SLOW_THRESHOLD_MS = 50;    // Cảnh báo log chậm > 50ms
    public static final long CRITICAL_ALERT_MS = 100;  // Ngưỡng tạo Performance Alert > 100ms
    private static final int FLUSH_INTERVAL_MIN = 5;   // Chu kỳ xuất CSV (phút)

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static class CommandMetric {
        public final byte cmd;
        public volatile String cmdName = "";
        public final LongAdder callCount = new LongAdder();
        public final LongAdder totalDurationNs = new LongAdder();
        public final AtomicLong maxDurationNs = new AtomicLong(0);
        public final AtomicLong minDurationNs = new AtomicLong(Long.MAX_VALUE);
        public final LongAdder slowCount = new LongAdder();
        public volatile String slowestContext = "";
        public volatile long lastSlowTime = 0;

        public CommandMetric(byte cmd) {
            this.cmd = cmd;
        }

        public long getAvgMs() {
            long count = callCount.sum();
            return count > 0 ? (totalDurationNs.sum() / count) / 1_000_000 : 0;
        }

        public long getMaxMs() {
            return maxDurationNs.get() / 1_000_000;
        }
    }

    private final ConcurrentHashMap<Byte, CommandMetric> metrics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Byte, Boolean> activeAlerts = new ConcurrentHashMap<>();
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(20000);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "PacketProfiler-Scheduler");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean isRunning = true;

    private PacketProfiler() {
        initDirectories();
        startAsyncLogWriter();
        startPeriodicCsvExporter();
    }

    private void initDirectories() {
        File logDir = new File("logs/metrics");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        File memDir = new File("tools/memory");
        if (!memDir.exists()) {
            memDir.mkdirs();
        }
    }

    private void startAsyncLogWriter() {
        Thread worker = new Thread(() -> {
            BufferedWriter slowWriter = null;
            try {
                slowWriter = new BufferedWriter(new FileWriter("logs/metrics/slow_packets.log", true));
                while (isRunning || !logQueue.isEmpty()) {
                    String logLine = logQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (logLine != null) {
                        slowWriter.write(logLine);
                        slowWriter.newLine();
                        slowWriter.flush();
                    }
                }
            } catch (Exception e) {
                Logger.logException(PacketProfiler.class, e, "Error in PacketProfiler log writer");
            } finally {
                if (slowWriter != null) {
                    try {
                        slowWriter.close();
                    } catch (IOException ignored) {}
                }
            }
        }, "PacketProfiler-LogWriter");
        worker.setDaemon(true);
        worker.start();
    }

    private void startPeriodicCsvExporter() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                exportSummaryCsv();
            } catch (Exception e) {
                Logger.logException(PacketProfiler.class, e, "Error exporting CSV summary");
            }
        }, FLUSH_INTERVAL_MIN, FLUSH_INTERVAL_MIN, TimeUnit.MINUTES);
    }

    /**
     * Ghi nhận thời gian thực thi của một packet (Gọi tại khối finally của Controller)
     */
    public void record(byte cmd, long durationNs, Player player, String cmdName) {
        CommandMetric metric = metrics.computeIfAbsent(cmd, CommandMetric::new);
        if (metric.cmdName.isEmpty() && cmdName != null && !cmdName.isEmpty()) {
            metric.cmdName = cmdName;
        }

        metric.callCount.increment();
        metric.totalDurationNs.add(durationNs);

        // Cập nhật min
        long curMin = metric.minDurationNs.get();
        while (durationNs < curMin && !metric.minDurationNs.compareAndSet(curMin, durationNs)) {
            curMin = metric.minDurationNs.get();
        }

        // Cập nhật max
        long curMax = metric.maxDurationNs.get();
        while (durationNs > curMax && !metric.maxDurationNs.compareAndSet(curMax, durationNs)) {
            curMax = metric.maxDurationNs.get();
        }
        if (metric.maxDurationNs.get() == durationNs) {
            metric.slowestContext = buildContext(player);
        }

        long durationMs = durationNs / 1_000_000;
        if (durationMs >= SLOW_THRESHOLD_MS) {
            metric.slowCount.increment();
            metric.lastSlowTime = System.currentTimeMillis();

            String timeStr = DATE_FORMAT.format(new Date());
            String context = buildContext(player);
            String logEntry = String.format("[%s] [SLOW] CMD: %d (%s) | %d ms | Context: %s",
                    timeStr, cmd, metric.cmdName, durationMs, context);

            logQueue.offer(logEntry);

            // Kiểm tra ngưỡng nghiêm trọng để kích hoạt Alert cho AI Agent
            if (durationMs >= CRITICAL_ALERT_MS) {
                triggerPerformanceAlert(cmd, metric, durationMs);
            }
        }
    }

    private String buildContext(Player player) {
        if (player == null) {
            return "Non-login session";
        }
        String mapInfo = (player.zone != null && player.zone.map != null) ? "Map " + player.zone.map.mapId : "No Map";
        return String.format("Player %s (ID: %d, %s)", player.name, player.id, mapInfo);
    }

    /**
     * Tự động ghi vết sự cố vào tools/memory/PERFORMANCE_ALERTS.json
     */
    private synchronized void triggerPerformanceAlert(byte cmd, CommandMetric metric, long peakLatencyMs) {
        activeAlerts.put(cmd, Boolean.TRUE);
        syncAlertsJson();
    }

    public synchronized void resolveAlert(byte cmd) {
        activeAlerts.remove(cmd);
        syncAlertsJson();
    }

    private void syncAlertsJson() {
        File file = new File("tools/memory/PERFORMANCE_ALERTS.json");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"version\": \"1.0.0\",\n");
            sb.append("  \"updated_at\": \"").append(DATE_FORMAT.format(new Date())).append("\",\n");
            sb.append("  \"alerts\": [\n");

            int idx = 0;
            for (Map.Entry<Byte, Boolean> entry : activeAlerts.entrySet()) {
                byte cmd = entry.getKey();
                CommandMetric metric = metrics.get(cmd);
                if (metric == null) continue;

                if (idx > 0) sb.append(",\n");
                sb.append("    {\n");
                sb.append("      \"cmd\": ").append((int) cmd).append(",\n");
                sb.append("      \"command_name\": \"").append(metric.cmdName).append("\",\n");
                sb.append("      \"max_latency_ms\": ").append(metric.getMaxMs()).append(",\n");
                sb.append("      \"avg_latency_ms\": ").append(metric.getAvgMs()).append(",\n");
                sb.append("      \"call_count\": ").append(metric.callCount.sum()).append(",\n");
                sb.append("      \"slow_count\": ").append(metric.slowCount.sum()).append(",\n");
                sb.append("      \"severity\": \"").append(metric.getMaxMs() > 200 ? "CRITICAL" : "HIGH").append("\",\n");
                sb.append("      \"status\": \"OPEN\",\n");
                sb.append("      \"slowest_context\": \"").append(metric.slowestContext.replace("\"", "\\\"")).append("\",\n");
                sb.append("      \"detected_at\": \"").append(DATE_FORMAT.format(new Date(metric.lastSlowTime))).append("\"\n");
                sb.append("    }");
                idx++;
            }

            sb.append("\n  ]\n");
            sb.append("}\n");
            bw.write(sb.toString());
        } catch (IOException e) {
            Logger.logException(PacketProfiler.class, e, "Error writing PERFORMANCE_ALERTS.json");
        }
    }

    /**
     * Xuất file CSV định kỳ để theo dõi xu hướng
     */
    public synchronized void exportSummaryCsv() {
        String today = DAY_FORMAT.format(new Date());
        File csvFile = new File("logs/metrics/api_summary_" + today + ".csv");
        boolean isNew = !csvFile.exists();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, true))) {
            if (isNew) {
                bw.write("Timestamp,CMD,CommandName,TotalCalls,AvgMs,MaxMs,SlowCalls,SlowestContext");
                bw.newLine();
            }

            String timeNow = DATE_FORMAT.format(new Date());
            for (CommandMetric m : metrics.values()) {
                if (m.callCount.sum() == 0) continue;
                String line = String.format("%s,%d,%s,%d,%d,%d,%d,\"%s\"",
                        timeNow,
                        m.cmd,
                        m.cmdName,
                        m.callCount.sum(),
                        m.getAvgMs(),
                        m.getMaxMs(),
                        m.slowCount.sum(),
                        m.slowestContext.replace("\"", "'")
                );
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            Logger.logException(PacketProfiler.class, e, "Error exporting CSV metric");
        }
    }

    /**
     * In báo cáo ra Server Console (phục vụ lệnh console `latency`)
     */
    public void printReport() {
        System.out.println("============================== PACKET LATENCY REPORT ==============================");
        System.out.printf("%-6s %-20s %-10s %-10s %-10s %-10s %-25s\n",
                "CMD", "NAME", "CALLS", "AVG(ms)", "MAX(ms)", "SLOW", "SLOWEST CONTEXT");
        System.out.println("-----------------------------------------------------------------------------------");

        metrics.values().stream()
                .filter(m -> m.callCount.sum() > 0)
                .sorted((a, b) -> Long.compare(b.maxDurationNs.get(), a.maxDurationNs.get()))
                .limit(15)
                .forEach(m -> System.out.printf("%-6d %-20s %-10d %-10d %-10d %-10d %-25s\n",
                        m.cmd,
                        m.cmdName.isEmpty() ? "CMD_" + m.cmd : m.cmdName,
                        m.callCount.sum(),
                        m.getAvgMs(),
                        m.getMaxMs(),
                        m.slowCount.sum(),
                        m.slowestContext.length() > 25 ? m.slowestContext.substring(0, 22) + "..." : m.slowestContext
                ));
        System.out.println("===================================================================================");
    }

    /**
     * Chuẩn bị chuỗi báo cáo gửi Popup in-game cho Admin (lệnh chat `toplag`)
     */
    public String getTopLagReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("|7|--- TOP CHỨC NĂNG CHẬM NHẤT ---\n");

        List<CommandMetric> list = new ArrayList<>(metrics.values());
        list.sort((a, b) -> Long.compare(b.maxDurationNs.get(), a.maxDurationNs.get()));

        int count = 0;
        for (CommandMetric m : list) {
            if (m.callCount.sum() == 0) continue;
            count++;
            String color = m.getMaxMs() > 100 ? "|2|" : (m.getMaxMs() > 50 ? "|8|" : "|0|");
            sb.append(color).append(count).append(". ")
              .append(m.cmdName.isEmpty() ? "CMD_" + m.cmd : m.cmdName)
              .append(" (").append(m.cmd).append(")\n")
              .append("|4|  Calls: ").append(m.callCount.sum())
              .append(" | Avg: ").append(m.getAvgMs()).append("ms")
              .append(" | Max: ").append(m.getMaxMs()).append("ms\n");

            if (count >= 5) break;
        }

        if (count == 0) {
            sb.append("|0|Chưa có dữ liệu thống kê hoặc server đang chạy rất mượt.");
        }
        return sb.toString();
    }
}

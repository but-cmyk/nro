package utils;

import models.player.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Logger {

    // ANSI Colors for Console
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";

    public enum LogCategory {
        SERVER("logs/server/"),
        ERROR("logs/error/"),
        AUDIT("logs/audit/"),
        ECONOMY("logs/economy/"),
        METRICS("logs/metrics/");

        final String dirPath;
        LogCategory(String dirPath) {
            this.dirPath = dirPath;
        }
    }

    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    public static class LogEvent {
        final LogCategory category;
        final LogLevel level;
        final long timestamp;
        final String threadName;
        final String playerContext;
        final String message;
        final Throwable throwable;

        public LogEvent(LogCategory category, LogLevel level, String playerContext, String message, Throwable throwable) {
            this.category = category;
            this.level = level;
            this.timestamp = System.currentTimeMillis();
            this.threadName = Thread.currentThread().getName();
            this.playerContext = playerContext != null ? playerContext : "";
            this.message = message != null ? message : "";
            this.throwable = throwable;
        }
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final BlockingQueue<LogEvent> EVENT_QUEUE = new LinkedBlockingQueue<>(50000);
    private static final ConcurrentHashMap<String, Long[]> THROTTLE_MAP = new ConcurrentHashMap<>();
    private static volatile boolean running = true;
    public static volatile boolean DEBUG_CONSOLE_AUDIT = false;

    // Active file writers mapped by category
    private static final Map<LogCategory, BufferedWriter> WRITERS = new EnumMap<>(LogCategory.class);
    private static String currentDateStr = "";

    static {
        // Ensure root and sub directories exist
        for (LogCategory cat : LogCategory.values()) {
            File d = new File(cat.dirPath);
            if (!d.exists()) {
                d.mkdirs();
            }
        }

        Thread worker = new Thread(Logger::workerLoop, "ServerLogWorker-Thread");
        worker.setDaemon(true);
        worker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            // Drain remaining logs
            drainQueue();
            closeWriters();
        }));
    }

    private static void workerLoop() {
        List<LogEvent> batch = new ArrayList<>(100);
        long lastFlush = System.currentTimeMillis();

        while (running || !EVENT_QUEUE.isEmpty()) {
            try {
                LogEvent first = EVENT_QUEUE.poll(100, TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    EVENT_QUEUE.drainTo(batch, 99);
                }

                if (!batch.isEmpty()) {
                    checkDateRoll();
                    for (LogEvent event : batch) {
                        writeEvent(event);
                    }
                    batch.clear();
                }

                long now = System.currentTimeMillis();
                if (now - lastFlush >= 500) {
                    flushWriters();
                    lastFlush = now;
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                System.err.println("[ServerLogWorker] Error writing log batch: " + e.getMessage());
            }
        }
    }

    private static void drainQueue() {
        checkDateRoll();
        LogEvent event;
        while ((event = EVENT_QUEUE.poll()) != null) {
            writeEvent(event);
        }
        flushWriters();
    }

    private static void checkDateRoll() {
        String today = DATE_FORMAT.format(new Date());
        if (!today.equals(currentDateStr)) {
            closeWriters();
            currentDateStr = today;
            for (LogCategory cat : LogCategory.values()) {
                try {
                    File dir = new File(cat.dirPath);
                    if (!dir.exists()) dir.mkdirs();
                    String fileName = cat.dirPath + cat.name().toLowerCase() + "-" + today + ".log";
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), StandardCharsets.UTF_8), 32768);
                    WRITERS.put(cat, writer);
                } catch (IOException e) {
                    System.err.println("[Logger] Cannot open log file for category " + cat + ": " + e.getMessage());
                }
            }
        }
    }

    private static void writeEvent(LogEvent event) {
        try {
            String timeStr = TIME_FORMAT.format(new Date(event.timestamp));
            StringBuilder sb = new StringBuilder(128);
            sb.append('[').append(timeStr).append("] ");
            sb.append('[').append(event.level).append("] ");
            sb.append('[').append(event.threadName).append("] ");
            if (!event.playerContext.isEmpty()) {
                sb.append(event.playerContext).append(' ');
            }
            sb.append(event.message);

            if (event.throwable != null) {
                sb.append('\n');
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    event.throwable.printStackTrace(pw);
                }
                sb.append(sw.toString().trim());
            }

            String line = sb.toString();

            // Write to primary category file
            BufferedWriter writer = WRITERS.get(event.category);
            if (writer != null) {
                writer.write(line);
                writer.newLine();
            }

            // Also mirror all ERROR logs to ERROR category file if not already ERROR category
            if (event.level == LogLevel.ERROR && event.category != LogCategory.ERROR) {
                BufferedWriter errWriter = WRITERS.get(LogCategory.ERROR);
                if (errWriter != null) {
                    errWriter.write(line);
                    errWriter.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("[Logger] Write error: " + e.getMessage());
        }
    }

    private static void flushWriters() {
        for (BufferedWriter writer : WRITERS.values()) {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void closeWriters() {
        for (BufferedWriter writer : WRITERS.values()) {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
        WRITERS.clear();
    }

    public static String formatPlayerContext(Player player) {
        if (player == null) {
            return "";
        }
        String mapZone = "";
        try {
            if (player.zone != null && player.zone.map != null) {
                mapZone = String.format(" | Map:%d-Z:%d", player.zone.map.mapId, player.zone.zoneId);
            }
        } catch (Exception ignored) {
        }
        return String.format("[Player: %s (ID:%d%s)]", player.name != null ? player.name : "noname", player.id, mapZone);
    }

    // --- High-Performance Async APIs ---

    public static void enqueue(LogCategory category, LogLevel level, String playerContext, String message, Throwable t) {
        EVENT_QUEUE.offer(new LogEvent(category, level, playerContext, message, t));
    }

    public static void info(String message) {
        System.out.println(GREEN + message + RESET);
        enqueue(LogCategory.SERVER, LogLevel.INFO, null, message, null);
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static void warn(String message) {
        System.out.println(YELLOW + message + RESET);
        enqueue(LogCategory.SERVER, LogLevel.WARN, null, message, null);
    }

    public static void warn(String format, Object... args) {
        warn(String.format(format, args));
    }

    public static void error(String message) {
        System.err.println(RED + message + RESET);
        enqueue(LogCategory.ERROR, LogLevel.ERROR, null, message, null);
    }

    public static void error(String message, Throwable t) {
        if (isThrottled(t != null ? t.getClass().getName() + ":" + message : message)) {
            return;
        }
        System.err.println(RED + "[ERROR] " + message + (t != null ? " (" + t.getMessage() + ")" : "") + RESET);
        enqueue(LogCategory.ERROR, LogLevel.ERROR, null, message, t);
    }

    public static void error(Player player, String message, Throwable t) {
        String plInfo = formatPlayerContext(player);
        if (isThrottled(plInfo + ":" + message)) {
            return;
        }
        System.err.println(RED + "[ERROR] " + plInfo + " " + message + (t != null ? " (" + t.getMessage() + ")" : "") + RESET);
        enqueue(LogCategory.ERROR, LogLevel.ERROR, plInfo, message, t);
    }

    public static void audit(String action, Player player, String details) {
        String plInfo = formatPlayerContext(player);
        String msg = action + ": " + details;
        if (DEBUG_CONSOLE_AUDIT) {
            System.out.println(CYAN + "[AUDIT] " + plInfo + " " + msg + RESET);
        }
        enqueue(LogCategory.AUDIT, LogLevel.INFO, plInfo, msg, null);
    }

    public static void economy(Player player, String currencyType, long amount, long balance, String reason, String details) {
        String plInfo = formatPlayerContext(player);
        String msg = String.format("Type:%s | Change:%+d | Balance:%d | Reason:%s | %s", currencyType, amount, balance, reason, details);
        enqueue(LogCategory.ECONOMY, LogLevel.INFO, plInfo, msg, null);
    }

    public static void metrics(String metricName, long durationMs, String details) {
        String msg = String.format("%s took %dms | %s", metricName, durationMs, details);
        enqueue(LogCategory.METRICS, LogLevel.INFO, null, msg, null);
    }

    private static boolean isThrottled(String key) {
        long now = System.currentTimeMillis();
        Long[] state = THROTTLE_MAP.compute(key, (k, v) -> {
            if (v == null) {
                return new Long[]{now, 1L};
            }
            if (now - v[0] > 5000L) {
                long count = v[1];
                if (count > 1) {
                    enqueue(LogCategory.ERROR, LogLevel.WARN, null, "[THROTTLED] Message repeated " + count + " times: " + k, null);
                }
                return new Long[]{now, 1L};
            }
            v[1]++;
            return v;
        });
        return state[1] > 1; // Drop direct logging if repeated within 5s
    }

    // --- Backward Compatibility Methods ---

    public static void log(String text) {
        System.out.print(text);
    }

    public static void log(String color, String text) {
        System.out.print(color + text + RESET);
    }

    public static void success(String text) {
        System.out.print(GREEN + text + RESET);
    }

    public static void successln(String text) {
        System.out.println(GREEN + text + RESET);
        enqueue(LogCategory.SERVER, LogLevel.INFO, null, text, null);
    }

    public static void warning(String text) {
        System.out.print(YELLOW + text + RESET);
    }

    public static void errorln(String text) {
        System.out.println(RED + text + RESET);
        enqueue(LogCategory.ERROR, LogLevel.ERROR, null, text, null);
    }

    public static void primaryln(String text) {
        System.out.println(BLUE + text + RESET);
        enqueue(LogCategory.SERVER, LogLevel.INFO, null, text, null);
    }

    public static void logException(Class<?> clazz, Exception ex, String... log) {
        try {
            String msg = (log != null && log.length > 0) ? log[0] : (clazz != null ? clazz.getSimpleName() : "Exception");
            error("[" + (clazz != null ? clazz.getName() : "Unknown") + "] " + msg, ex);
        } catch (Exception e) {
            System.err.println("Failed to log exception: " + e.getMessage());
        }
    }

    public static void info() {
        info("");
    }

    public static void warn() {
        warn("");
    }
}

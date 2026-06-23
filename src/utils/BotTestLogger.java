package utils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BotTestLogger {

    private static final String LOG_FILE = "log/bot_test.log";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static synchronized void log(String botName, int botId, String eventType, String message) {
        writeLog(botName, botId, eventType, message, null);
    }

    public static synchronized void logException(String botName, int botId, String eventType, String message, Throwable throwable) {
        writeLog(botName, botId, eventType, message, throwable);
    }

    private static void writeLog(String botName, int botId, String eventType, String message, Throwable throwable) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            String time = dateFormat.format(new Date());
            String header = String.format("[%s] [%s (ID:%d)] [%s] - %s", time, botName, botId, eventType, message);
            pw.println(header);
            
            if (throwable != null) {
                StringWriter sw = new StringWriter();
                PrintWriter exceptionPw = new PrintWriter(sw);
                throwable.printStackTrace(exceptionPw);
                pw.print(sw.toString());
                pw.println("--------------------------------------------------------------------------------");
            }
        } catch (IOException e) {
            System.err.println("Failed to write to bot_test.log: " + e.getMessage());
        }
    }
}

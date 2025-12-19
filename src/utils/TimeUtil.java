package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import models.phoban.BlackBallWar;

public class TimeUtil {

    public static final byte SECOND = 1;
    public static final byte MINUTE = 2;
    public static final byte HOUR = 3;
    public static final byte DAY = 4;
    public static final byte WEEK = 5;
    public static final byte MONTH = 6;
    public static final byte YEAR = 7;

    public static long diffDate(Date d1, Date d2, byte type) {
        if (d1 == null || d2 == null) {
            return 0;
        }

        Instant i1 = d1.toInstant();
        Instant i2 = d2.toInstant();

        switch (type) {
            case SECOND:
                return Duration.between(i1, i2).abs().getSeconds();
            case MINUTE:
                return Duration.between(i1, i2).abs().toMinutes();
            case HOUR:
                return Duration.between(i1, i2).abs().toHours();
            case DAY:
                return Duration.between(i1, i2).abs().toDays();
            case WEEK:
                return ChronoUnit.WEEKS.between(i1, i2);
            case MONTH:
                LocalDate ld1 = d1.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();
                LocalDate ld2 = d2.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();
                return ChronoUnit.MONTHS.between(ld1, ld2);
            case YEAR:
                LocalDate y1 = d1.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();
                LocalDate y2 = d2.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();
                return ChronoUnit.YEARS.between(y1, y2);
            default:
                return 0;
        }
    }

    public static int getCurrDay() {
        LocalDateTime now = LocalDateTime.now();
        return now.getDayOfWeek().getValue();
    }

    public static int getCurrHour() {
        LocalDateTime now = LocalDateTime.now();
        return now.getHour();
    }

    public static int getCurrMin() {
        LocalDateTime now = LocalDateTime.now();
        return now.getMinute();
    }

    public static String convertTime(long totalSeconds) {
        long days = TimeUnit.SECONDS.toDays(totalSeconds);
        long hours = TimeUnit.SECONDS.toHours(totalSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(" ngày ");
        }
        if (hours > 0) {
            result.append(hours).append(" giờ ");
        }
        if (minutes > 0) {
            result.append(minutes).append(" phút ");
        }
        if (seconds > 0) {
            result.append(seconds).append(" giây");
        }
        return result.toString().trim();
    }

    public static String getTimeLeft(long lastTime, int secondTarget) {
        // 1. Dùng phương thức đã có để tính toán chính xác số giây còn lại
        int secondsLeft = getSecondLeft(lastTime, secondTarget);

        // 2. Dùng phương thức đã có để định dạng số giây đó thành chuỗi
        return convertTime(secondsLeft);
    }

    public static String getTimeLeft(long lastTime) {
        int secondPassed = (int) ((System.currentTimeMillis() - lastTime) / 1000);
        return secondPassed > 86400 ? (secondPassed / 86400) + "n trước" : secondPassed > 3600 ? (secondPassed / 3600) + "g trước" : secondPassed > 60 ? (secondPassed / 60) + "p trước" : secondPassed + "gi trước";
    }

    public static int getSecondLeft(long lastTime, int secondTarget) {
        int secondPassed = (int) ((System.currentTimeMillis() - lastTime) / 1000);
        int secondsLeft = secondTarget - secondPassed;
        if (secondsLeft < 0) {
            secondsLeft = 0;
        }
        return secondsLeft;
    }

    public static String getDateLeft(long lastTime, int secondTarget) {
        return convertTime(getSecondLeft(lastTime, secondTarget));
    }

    public static String convertTimeNow(long lastTime) {
        int secondsLeft = (int) ((System.currentTimeMillis() - lastTime) / 1000);
        if (secondsLeft < 0) {
            secondsLeft = 0;
        }
        return convertTime(secondsLeft);
    }

    public static long getTime(String time, String format) throws Exception {
        SimpleDateFormat fm = new SimpleDateFormat(format);
        try {
            return fm.parse(time).getTime();
        } catch (ParseException ex) {
            throw new Exception("Thời gian không hợp lệ");
        }
    }

    public static String getTimeNow(String format) {
        SimpleDateFormat fm = new SimpleDateFormat(format);
        return fm.format(new Date());
    }

    public static String getTimeBeforeCurrent(int subTime, String format) {
        SimpleDateFormat fm = new SimpleDateFormat(format);
        Date date = new Date(System.currentTimeMillis() - subTime);
        return fm.format(date);
    }

    public static String formatTime(Date time, String format) {
        SimpleDateFormat fm = new SimpleDateFormat(format);
        return fm.format(time);
    }

    public static String formatTime(long time, String format) {
        SimpleDateFormat fm = new SimpleDateFormat(format);
        return fm.format(new Date(time));
    }

    public static boolean isMabuOpen() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // lấy giờ hiện tại
        return hour >= 1 && hour < 23;
    }

    public static boolean isMabu14HOpen() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // lấy giờ hiện tại
        return hour == 14;  // Chỉ trả về true khi giờ là 14
    }

    public static boolean is21H() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 21 && hour < 22);
    }

    public static boolean timeEarth() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 9 && hour < 16;
    }

    public static boolean timeNamek() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 16 || hour < 9;
    }

    public static long getStartTimeBlackBallWar() {
        LocalTime startTime = LocalTime.of(BlackBallWar.HOUR_OPEN, BlackBallWar.MIN_OPEN, BlackBallWar.SECOND_OPEN);
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), startTime);
        Instant startInstant = startDateTime.toInstant(ZoneOffset.UTC);

        return startInstant.toEpochMilli();
    }

    public static boolean isBlackBallWarOpen() {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(BlackBallWar.HOUR_OPEN, BlackBallWar.MIN_OPEN, BlackBallWar.SECOND_OPEN);
        LocalTime endTime = LocalTime.of(BlackBallWar.HOUR_CLOSE, BlackBallWar.MIN_CLOSE, BlackBallWar.SECOND_CLOSE);

        return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
    }

    public static boolean isBlackBallWarCanPick() {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(BlackBallWar.HOUR_CAN_PICK_DB, BlackBallWar.MIN_CAN_PICK_DB, BlackBallWar.SECOND_CAN_PICK_DB);

        return currentTime.isAfter(startTime) && isBlackBallWarOpen();
    }

    public static long getSecondsUntilCanPick() {
        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(BlackBallWar.HOUR_CAN_PICK_DB, BlackBallWar.MIN_CAN_PICK_DB, BlackBallWar.SECOND_CAN_PICK_DB);

        if (currentTime.isBefore(startTime)) {
            Duration duration = Duration.between(currentTime, startTime);
            return duration.getSeconds();
        } else {
            return 0;
        }
    }

    public static boolean checkTime(long time) {
        return (time - System.currentTimeMillis()) / 1000 > 0;
    }

    public static String getTimeLeftInSeconds(long lastTime, int targetSeconds) {
        long currentTime = System.currentTimeMillis();
        long passedTime = (currentTime - lastTime) / 1000; // Đổi ra giây
        long remaining = targetSeconds - passedTime;
        return remaining > 0 ? remaining + " giây" : "0 giây";
    }

}

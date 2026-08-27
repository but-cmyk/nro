package utils;

import managers.boss.BossManager;

import java.text.NumberFormat;
import java.util.*;

import models.mob.Mob;
import models.npc.Npc;
import models.player.Player;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

import java.time.*;

import models.map.Zone;
import services.map.MapService;

public class Util {

    private static final Random rand;
    private static final SimpleDateFormat dateFormat;
    private static final SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
    private static final Locale locale = Locale.of("vi", "VN");
    private static final NumberFormat num = NumberFormat.getInstance(locale);
    private static final NumberFormat numShortFormat;

    static {
        rand = new Random();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        numShortFormat = NumberFormat.getInstance(locale);
        numShortFormat.setMaximumFractionDigits(1);
    }

    public static String format(double power) {
        return num.format(power);
    }

    public static long maxIntValue(double a) {
        if (a > Integer.MAX_VALUE) {
            a = Integer.MAX_VALUE;
        }
        return (int) a;
    }

    public static String toDateString(Date date) {
        try {
            return Util.dateFormat.format(date);
        } catch (Exception e) {
            Date now = new Date();
            return dateFormat.format(now);
        }
    }

    public static synchronized boolean compareDay(Date now, Date when) {
        if (now == null || when == null) {
            return false;
        }

        try {
            Date date1 = Util.formatDay.parse(Util.formatDay.format(now));
            Date date2 = Util.formatDay.parse(Util.formatDay.format(when));
            return !date1.equals(date2) && !date1.before(date2);
        } catch (Exception e) {
            Logger.error("Error compareDay");
            return false;
        }
    }

    public static double myGetDistance(int x1, int y1, int x2, int y2) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;
        return Math.abs(Math.sqrt(deltaX * deltaX + deltaY * deltaY));
    }

    public static int createIdBossClone(int idPlayer) {
        return -idPlayer - 1_000_000_000;
    }

    public static Zone randomAllMap() {
        int mapId = Util.nextInt(0, 20);
        Zone zoneJoin = MapService.gI().getMapWithRandZone(mapId);
        while (zoneJoin == null || zoneJoin.zoneId == 0) {
            try {
                zoneJoin = MapService.gI().getMapWithRandZone(mapId);
            } catch (Exception e) {
                Logger.error("Error randomAllMap");
            }
        }
        return zoneJoin;
    }

    public static int highlightsItem(boolean highlights, int value) {
        double highlightsNumber = 1.1;
        return highlights ? (int) (value * highlightsNumber) : value;
    }

    public static boolean contains(String[] arr, String key) {
        return Arrays.toString(arr).contains(key);
    }

    public static int[] pickNRandInArr(int[] array, int n) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int i : array) {
            list.add(i);
        }
        Collections.shuffle(list);
        int[] answer = new int[n];
        for (int i = 0; i < n; i++) {
            answer[i] = list.get(i);
        }
        Arrays.sort(answer);
        return answer;
    }

    public static String msToTime(long ms) {
        if (ms <= 0) {
            return "0s";
        }
        long day = ms / (1000 * 60 * 60 * 24);
        ms = ms % (1000 * 60 * 60 * 24);
        long hour = ms / (1000 * 60 * 60);
        ms = ms % (1000 * 60 * 60);
        long min = ms / (1000 * 60);
        ms = ms % (1000 * 60);
        long sec = ms / 1000;
        StringBuilder time = new StringBuilder();
        if (day > 0) {
            time.append(day).append(" ngày, ");
        }
        if (hour > 0) {
            time.append(hour).append(" giờ, ");
        }
        if (min > 0) {
            time.append(min).append(" phút, ");
        }
        if (sec > 0) {
            time.append(sec).append(" giây");
        }
        String timeStr = time.toString();
        if (timeStr.endsWith(", ")) {
            timeStr = timeStr.substring(0, timeStr.length() - 2);
        }
        return timeStr.isEmpty() ? "0s" : timeStr;
    }

    public static String powerToString(long power) {
        if (power >= 1000000000) {
            return numShortFormat.format((double) power / 1000000000) + " Tỷ";
        }
        if (power >= 1000000) {
            return numShortFormat.format((double) power / 1000000) + " Tr";
        }
        if (power >= 1000) {
            return numShortFormat.format((double) power / 1000) + " k";
        }
        return num.format(power);
    }

    public static int getDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static int getDistance(Player pl1, Player pl2) {
        return getDistance(pl1.location.x, pl1.location.y, pl2.location.x, pl2.location.y);
    }

    public static int getDistance(Player pl, Npc npc) {
        return getDistance(pl.location.x, pl.location.y, npc.cx, npc.cy);
    }

    public static int getDistance(Player pl, Mob mob) {
        return getDistance(pl.location.x, pl.location.y, mob.location.x, mob.location.y);
    }

    public static int getDistance(Mob mob1, Mob mob2) {
        return getDistance(mob1.location.x, mob1.location.y, mob2.location.x, mob2.location.y);
    }

    public static int nextInt(int from, int to) {
        if (to < from) {
            int tmp = from;
            from = to;
            to = tmp;
        }
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(from, to + 1);
    }

    public static int nextInt(int max) {
        if (max <= 0) return 0;
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(max);
    }

    public static long nextLong(long from, long to) {
        if (to < from) {
            long tmp = from;
            from = to;
            to = tmp;
        }
        return java.util.concurrent.ThreadLocalRandom.current().nextLong(from, to + 1);
    }

    public static long nextLong(long max) {
        if (max <= 0) return 0;
        return java.util.concurrent.ThreadLocalRandom.current().nextLong(max);
    }

    public static int nextInt(int[] percen) {
        int next = nextInt(1000), i;
        for (i = 0; i < percen.length; i++) {
            if (next < percen[i]) {
                return i;
            }
            next -= percen[i];
        }
        return i;
    }

    public static int getOne(int n1, int n2) {
        return java.util.concurrent.ThreadLocalRandom.current().nextBoolean() ? n1 : n2;
    }

    public static String replace(String text, String regex, String replacement) {
        return text.replace(regex, replacement);
    }

    public static boolean isTrue(long ratioPercentage, long totalPercentage) {
        long num = Util.nextLong(totalPercentage);
        return num < ratioPercentage;
    }

    public static boolean isTrue(float ratioPercentage, long totalPercentage) {
        if (ratioPercentage < 1) {
            ratioPercentage *= 100;
            totalPercentage *= 100;
        }
        return isTrue((long) ratioPercentage, totalPercentage);
    }

    public static boolean isTrue(long ratioPercentage, long totalPercentage, int accuracy) {
        return Util.nextLong(totalPercentage * accuracy) < ratioPercentage && Util.nextInt(accuracy) == 0;
    }

    public static boolean isTrue(float ratioPercentage, long totalPercentage, int accuracy) {
        if (ratioPercentage < 1) {
            ratioPercentage *= 100;
            totalPercentage *= 100;
        }
        return isTrue((long) ratioPercentage, totalPercentage, accuracy);
    }

    public static boolean haveSpecialCharacter(String text) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        boolean b = m.find();
        return b || text.contains(" ");
    }

    public static boolean canDoWithTime(long lastTime, long miniTimeTarget) {
        return System.currentTimeMillis() - lastTime > miniTimeTarget;
    }

    public static Object[] addArray(Object[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            return null;
        }
        if (arrays.length == 1) {
            return arrays[0];
        }
        Object[] arr0 = arrays[0];
        for (int i = 1; i < arrays.length; i++) {
            arr0 = ArrayUtils.addAll(arr0, arrays[i]);
        }
        return arr0;
    }

    public static int randomBossId() {
        int bossId = Util.nextInt(-1000000, -100000);
        while (BossManager.gI().getBossById(bossId) != null) {
            bossId = Util.nextInt(-1000000, 100000);
        }
        return bossId;
    }

    public static boolean isAfterMidnight(long currenttimemillis) {
        Instant instant = Instant.ofEpochMilli(currenttimemillis);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        LocalDate otherDate = zonedDateTime.toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return currentDate.isAfter(otherDate);
    }

    public static boolean isTimeSnak(long setTime, int nDays) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - setTime;
        long daysDifference = timeDifference / 86400000;
        return daysDifference < nDays;
    }

    public static String formatNumber(long number) {
        return num.format(number);
    }

    public static void setTimeout(Runnable runnable, int delay) {
        server.GameLoopManager.gI().schedule(runnable, delay);
    }

    public static int getDistanceSq(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public static String getFormatTime(long millis) {
        if (millis <= 0) return "Chưa có";
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long hours = minutes / 60;
        minutes = minutes % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" giờ ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" phút ");
        }
        // Luôn hiện giây nếu tổng thời gian > 0, hoặc nếu giờ và phút đều = 0
        if (seconds > 0 || (hours == 0 && minutes == 0)) {
            sb.append(seconds).append(" giây");
        }

        return sb.toString().trim();
    }

}

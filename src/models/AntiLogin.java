package models;

/**
 * Per-key login attempt guard (key = username or IP).
 *
 * <p>Default policy: 5 failures / 15 phút. Callers should combine an
 * account-keyed guard with an IP-keyed guard so that:
 * - one account cannot be brute-forced from many IPs;
 * - one IP cannot spray many accounts.</p>
 */
public class AntiLogin {

    public static final int DEFAULT_MAX_WRONG = 5;
    public static final int DEFAULT_LOCK_MS = 15 * 60 * 1000;

    private final int maxWrong;
    private final int timeAntiMs;

    private long lastTimeLogin;
    private volatile long lastActivity;
    private int timeCanLogin;
    public byte wrongLogin;

    public AntiLogin() {
        this(DEFAULT_MAX_WRONG, DEFAULT_LOCK_MS);
    }

    public AntiLogin(int maxWrong, int timeAntiMs) {
        this.maxWrong = Math.max(1, maxWrong);
        this.timeAntiMs = Math.max(1_000, timeAntiMs);
        this.wrongLogin = 0;
        this.lastTimeLogin = -1;
        this.timeCanLogin = 0;
        this.lastActivity = System.currentTimeMillis();
    }

    public synchronized boolean canLogin() {
        this.lastActivity = System.currentTimeMillis();
        if (wrongLogin < maxWrong) {
            return true;
        }
        if (lastTimeLogin != -1) {
            long timePassed = System.currentTimeMillis() - lastTimeLogin;
            if (timePassed >= timeAntiMs) {
                reset();
                return true;
            }
            return false;
        }
        return true;
    }

    public synchronized void wrong() {
        this.lastActivity = System.currentTimeMillis();
        if (wrongLogin < Byte.MAX_VALUE) {
            wrongLogin++;
        }
        if (wrongLogin >= maxWrong) {
            this.lastTimeLogin = System.currentTimeMillis();
            this.timeCanLogin = timeAntiMs;
        }
    }

    public synchronized void reset() {
        this.lastActivity = System.currentTimeMillis();
        this.wrongLogin = 0;
        this.lastTimeLogin = -1;
        this.timeCanLogin = 0;
    }

    public synchronized long getRemainingLockMillis() {
        if (wrongLogin < maxWrong || lastTimeLogin == -1) {
            return 0;
        }
        long left = timeAntiMs - (System.currentTimeMillis() - lastTimeLogin);
        return Math.max(0, left);
    }

    public synchronized String getNotifyCannotLogin() {
        long left = getRemainingLockMillis();
        if (left > 0) {
            long secondsLeft = Math.max(1, left / 1000);
            return "Bạn đã đăng nhập sai quá " + maxWrong + " lần. Vui lòng thử lại sau " + secondsLeft + " giây.";
        }
        return "Bạn đã đăng nhập tài khoản sai quá nhiều lần. Vui lòng thử lại sau ít phút";
    }

    public synchronized boolean isExpired(long now, long maxIdleMillis) {
        return now - this.lastActivity >= maxIdleMillis;
    }
}

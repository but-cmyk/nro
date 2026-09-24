package models;

public class AntiLogin {
    private static final byte MAX_WRONG = 5; // Changed to 5 as requested
    private static final int TIME_ANTI = 60000; // Changed to 1 minute (60000ms)
    private long lastTimeLogin;
    private volatile long lastActivity;
    private int timeCanLogin;
    public byte wrongLogin;
    
    public AntiLogin() {
        this.wrongLogin = 0;
        this.lastTimeLogin = -1;
        this.timeCanLogin = 0;
        this.lastActivity = System.currentTimeMillis();
    }
    
    public synchronized boolean canLogin() {
        this.lastActivity = System.currentTimeMillis();
        // If no failed attempts, can login
        if (wrongLogin < MAX_WRONG) {
            return true;
        }
        
        // If blocked, check if block time has passed
        if (lastTimeLogin != -1) {
            long currentTime = System.currentTimeMillis();
            long timePassed = currentTime - lastTimeLogin;
            
            // If enough time has passed, reset and allow login
            if (timePassed >= TIME_ANTI) {
                reset();
                return true;
            }
            
            // Still blocked
            return false;
        }
        
        return true;
    }
    
    public synchronized void wrong() {
        this.lastActivity = System.currentTimeMillis();
        wrongLogin++;
        if (wrongLogin >= MAX_WRONG) {
            this.lastTimeLogin = System.currentTimeMillis();
            this.timeCanLogin = TIME_ANTI;
        }
    }
    
    public synchronized void reset() {
        this.lastActivity = System.currentTimeMillis();
        this.wrongLogin = 0;
        this.lastTimeLogin = -1;
        this.timeCanLogin = 0;
    }
    
    public synchronized String getNotifyCannotLogin() {
        if (wrongLogin >= MAX_WRONG && lastTimeLogin != -1) {
            long currentTime = System.currentTimeMillis();
            long timePassed = currentTime - lastTimeLogin;
            long timeLeft = TIME_ANTI - timePassed;
            
            if (timeLeft > 0) {
                int secondsLeft = (int) (timeLeft / 1000);
                return "Bạn đã đăng nhập sai quá " + MAX_WRONG + " lần. Vui lòng thử lại sau " + secondsLeft + " giây.";
            }
        }
        return "Bạn đã đăng nhập tài khoản sai quá nhiều lần. Vui lòng thử lại sau ít phút";
    }

    public synchronized boolean isExpired(long now, long maxIdleMillis) {
        return now - this.lastActivity >= maxIdleMillis;
    }
    
    // Helper method to get remaining block time in seconds
//    public int getRemainingBlockTime() {
//        if (wrongLogin >= MAX_WRONG && lastTimeLogin != -1) {
//            long currentTime = System.currentTimeMillis();
//            long timePassed = currentTime - lastTimeLogin;
//            long timeLeft = TIME_ANTI - timePassed;
//
//            if (timeLeft > 0) {
//                return (int) (timeLeft / 1000);
//            }
//        }
//        return 0;
//    }
}

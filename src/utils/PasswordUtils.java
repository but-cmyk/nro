package utils;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Password hashing/verification.
 *
 * <p>Invariant: only BCrypt hashes are accepted for login. Legacy plaintext
 * rows are handled once by {@link #verifyLegacyPlaintext(String, String)} and
 * must be re-hashed immediately by the caller (lazy migration). Do not add a
 * permanent plaintext fallback here.</p>
 */
public final class PasswordUtils {

    private static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    /** BCrypt work factor. 12 is strong but ~250ms/login; rate-limit login. */
    private static final int BCRYPT_COST = 12;

    /** Giới hạn số lượng phép toán BCrypt chạy song song để bảo vệ CPU máy chủ (2 x số CPU cores) */
    private static final int MAX_CONCURRENT_BCRYPT = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
    private static final java.util.concurrent.Semaphore BCRYPT_SEMAPHORE = new java.util.concurrent.Semaphore(MAX_CONCURRENT_BCRYPT);
    private static final long ACQUIRE_TIMEOUT_MS = 2000L;

    private PasswordUtils() {
    }

    public static int getAvailablePermits() {
        return BCRYPT_SEMAPHORE.availablePermits();
    }

    public static int getMaxConcurrentBcrypt() {
        return MAX_CONCURRENT_BCRYPT;
    }

    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        boolean acquired = false;
        try {
            acquired = BCRYPT_SEMAPHORE.tryAcquire(ACQUIRE_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!acquired) {
                Logger.warning("[BCrypt Throttling] Quá tải CPU khi băm mật khẩu (> " + MAX_CONCURRENT_BCRYPT + " luồng). Tạm thời dùng cost 10.");
                return BCrypt.hashpw(password, BCrypt.gensalt(10));
            }
            return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return BCrypt.hashpw(password, BCrypt.gensalt(10));
        } finally {
            if (acquired) {
                BCRYPT_SEMAPHORE.release();
            }
        }
    }

    public static boolean isBcrypt(String stored) {
        return stored != null && BCRYPT_PATTERN.matcher(stored).matches();
    }

    /**
     * Strict BCrypt verification. Returns false for null, malformed or legacy
     * (non-BCrypt) values; never compares plaintext.
     */
    public static boolean verifyPassword(String password, String stored) {
        if (password == null || stored == null || !isBcrypt(stored)) {
            return false;
        }
        boolean acquired = false;
        try {
            acquired = BCRYPT_SEMAPHORE.tryAcquire(ACQUIRE_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!acquired) {
                Logger.warning("[BCrypt Throttling] Quá tải CPU xác thực mật khẩu (> " + MAX_CONCURRENT_BCRYPT + " luồng). Tạm thời từ chối login!");
                return false;
            }
            return BCrypt.checkpw(password, stored);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        } finally {
            if (acquired) {
                BCRYPT_SEMAPHORE.release();
            }
        }
    }

    /**
     * Constant-time comparison for legacy plaintext rows. Callers MUST re-hash
     * the password and persist it immediately after a successful login.
     */
    public static boolean verifyLegacyPlaintext(String password, String stored) {
        if (password == null || stored == null || password.isEmpty() || stored.isEmpty()) {
            return false;
        }
        // A malformed BCrypt-looking value must never be accepted as plaintext.
        if (stored.startsWith("$2")) {
            return false;
        }
        return MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }
}

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

    private PasswordUtils() {
    }

    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
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
        try {
            return BCrypt.checkpw(password, stored);
        } catch (IllegalArgumentException e) {
            return false;
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

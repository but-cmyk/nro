package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    /**
     * Hashes a plaintext password using BCrypt.
     * @param password the plaintext password
     * @return the hashed password
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verifies a plaintext password against a BCrypt hash.
     * Supports falling back to plaintext comparison for non-hashed legacy passwords,
     * which helps if any account is not migrated or for safe checks.
     * @param password the plaintext password to verify
     * @param hashed the stored hash (or legacy plaintext password)
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashed) {
        if (password == null || hashed == null) {
            return false;
        }
        
        // BCrypt hashes start with $2a$, $2b$ or $2y$ and are 60 characters long
        if (hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(password, hashed);
            } catch (Exception e) {
                // If checking fails, fall back to plaintext comparison
                return password.equals(hashed);
            }
        }
        
        // Fallback for plaintext (if migration has not run yet or for legacy users)
        return password.equals(hashed);
    }
}

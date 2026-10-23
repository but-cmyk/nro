import models.AntiLogin;
import utils.PasswordUtils;

/**
 * Standalone regression checks for the P0 security fixes.
 * Run after `build.bat`:
 *   pwsh tools/security/run_security_checks.ps1
 */
public class TestSecurityFixes {
    public static void main(String[] args) {
        String hash = PasswordUtils.hashPassword("abc123");
        check("bcrypt verify ok", PasswordUtils.verifyPassword("abc123", hash));
        check("bcrypt wrong rejected", !PasswordUtils.verifyPassword("wrong", hash));
        check("plaintext never accepted", !PasswordUtils.verifyPassword("abc123", "abc123"));
        check("legacy compare ok", PasswordUtils.verifyLegacyPlaintext("abc123", "abc123"));
        check("legacy compare wrong", !PasswordUtils.verifyLegacyPlaintext("abc123", "abc124"));
        check("malformed hash not legacy",
                !PasswordUtils.verifyLegacyPlaintext("$2a$12$broken", "$2a$12$broken"));
        check("empty password not legacy", !PasswordUtils.verifyLegacyPlaintext("", ""));
        check("malformed hash rejected", !PasswordUtils.verifyPassword("abc123", "$2a$12$broken"));

        AntiLogin a = new AntiLogin(5, 60_000);
        for (int i = 0; i < 5; i++) {
            a.wrong();
        }
        check("locked after 5", !a.canLogin());
        check("remaining > 0", a.getRemainingLockMillis() > 0);
        a.reset();
        check("reset unlocks", a.canLogin());

        // Kiểm tra Semaphore CPU Throttling bảo toàn permits
        check("semaphore permits restored", PasswordUtils.getAvailablePermits() == PasswordUtils.getMaxConcurrentBcrypt());
        System.out.println("ALL SECURITY CHECKS PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) {
            throw new AssertionError("FAILED: " + name);
        }
        System.out.println("PASS: " + name);
    }
}

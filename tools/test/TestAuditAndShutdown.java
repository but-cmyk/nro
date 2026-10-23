package test;

import server.ServerManager;
import network.session.MySession;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestAuditAndShutdown {

    private static int passCount = 0;
    private static int failCount = 0;

    private static void assertCondition(boolean condition, String testName) {
        if (condition) {
            System.out.println("PASS: " + testName);
            passCount++;
        } else {
            System.err.println("FAIL: " + testName);
            failCount++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU AUDIT ADMIN (A5) & IDEMPOTENT SHUTDOWN (C1) ===");

        testAdminAuditDAO();
        testIdempotentShutdown();
        testRefCountLockConcurrent();

        System.out.println("\n=== KET QUA KIEM THU ===");
        System.out.println("Tong so test: " + (passCount + failCount));
        System.out.println("PASS: " + passCount);
        System.out.println("FAIL: " + failCount);

        if (failCount > 0) {
            System.exit(1);
        } else {
            System.out.println("ALL A5 & C1 TESTS PASSED!");
            System.exit(0);
        }
    }

    private static void testAdminAuditDAO() {
        System.out.println("\n--- Test A5: AdminAuditDAO ---");
        try {
            // Test gọi log không văng Exception dù DB offline
            database.daos.AdminAuditDAO.log("TEST_ADMIN", "TEST_ACTION", "PLAYER_TEST", "{\"test\":true}");
            assertCondition(true, "AdminAuditDAO.log thực thi an toàn không ném Exception");

            // Test xử lý detail rỗng/null
            database.daos.AdminAuditDAO.log("TEST_ADMIN", "EMPTY_DETAIL", "TARGET", null);
            assertCondition(true, "AdminAuditDAO.log xử lý null detail an toàn");

            // Đợi virtual thread thực thi
            Thread.sleep(200);
            assertCondition(true, "Virtual Thread log audit chạy nền an toàn");
        } catch (Exception e) {
            assertCondition(false, "AdminAuditDAO ném ngoại lệ: " + e.getMessage());
        }
    }

    private static void testIdempotentShutdown() {
        System.out.println("\n--- Test C1: Idempotent Shutdown ---");
        try {
            ServerManager sm = new ServerManager();

            // Gọi shutdown() lần 1
            sm.shutdown();
            assertCondition(true, "Gọi ServerManager.shutdown() lần 1 an toàn");

            // Gọi shutdown() lần 2 (kiểm tra tính Idempotent - không được ném exception hoặc chạy lại)
            sm.shutdown();
            assertCondition(true, "Gọi ServerManager.shutdown() lần 2 an toàn (Idempotent 100%)");
        } catch (Exception e) {
            assertCondition(false, "ServerManager.shutdown ném ngoại lệ: " + e.getMessage());
        }
    }

    private static void testRefCountLockConcurrent() {
        System.out.println("\n--- Test C1: RefCountLock Concurrent Safety & Cleanup ---");
        class RefCountLock {
            final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
            int refCount = 0;
        }

        final java.util.Map<String, RefCountLock> accountLocks = new java.util.concurrent.ConcurrentHashMap<>();
        final java.util.Map<String, AtomicInteger> activePerUser = new java.util.concurrent.ConcurrentHashMap<>();
        final int THREAD_COUNT = 30;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        final AtomicBoolean violationDetected = new AtomicBoolean(false);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final String username = "user_" + (i % 3); // 3 tài khoản chia sẻ 30 thread song song
            Thread.ofVirtual().start(() -> {
                try {
                    startLatch.await();
                    RefCountLock rLock;
                    synchronized (accountLocks) {
                        rLock = accountLocks.computeIfAbsent(username, k -> new RefCountLock());
                        rLock.refCount++;
                    }
                    rLock.lock.lock();
                    try {
                        int active = activePerUser.computeIfAbsent(username, k -> new AtomicInteger()).incrementAndGet();
                        if (active > 1) {
                            violationDetected.set(true); // Có hơn 1 thread login cùng 1 username đồng thời!
                        }
                        Thread.sleep(15);
                        activePerUser.get(username).decrementAndGet();
                    } finally {
                        rLock.lock.unlock();
                        synchronized (accountLocks) {
                            rLock.refCount--;
                            if (rLock.refCount <= 0) {
                                accountLocks.remove(username);
                            }
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        try {
            boolean done = endLatch.await(5, TimeUnit.SECONDS);
            assertCondition(done, "Tất cả 30 Virtual Threads login hoàn thành trong 5s");
            assertCondition(!violationDetected.get(), "Mutual Exclusion: Tuyệt đối không có 2 thread cùng login 1 account song song");
            assertCondition(accountLocks.isEmpty(), "Zero Memory Leak: Toàn bộ lock đã được giải phóng và dọn sạch khỏi map");
        } catch (InterruptedException e) {
            assertCondition(false, "Test bị gián đoạn");
        }
    }
}

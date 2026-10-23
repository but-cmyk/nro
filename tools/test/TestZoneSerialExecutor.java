package test;

import models.Template.ItemTemplate;
import models.map.ItemMap;
import models.map.Zone;
import utils.SerialExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TestZoneSerialExecutor: Kiểm thử toàn diện kiến trúc Single-Writer (SerialExecutor) cho Zone.
 * - Test 1: Thứ tự thực thi FIFO 100% tuần tự.
 * - Test 2: Stress test đa luồng (50 threads đồng thời submit 100.000 tasks) bảo toàn Single-Writer.
 * - Test 3: Exception Isolation (task con văng lỗi không làm chết hàng đợi hoặc kẹt WIP).
 * - Test 4: Zone Integration Concurrency (30 threads cùng nhặt 1 item duy nhất trong Zone + update).
 */
public class TestZoneSerialExecutor {

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
        System.out.println("=== BAT DAU KIEM THU ZONE SINGLE-WRITER (SERIALEXECUTOR) [C2] ===");

        try {
            testFifoExecutionOrder();
            testMultiThreadStressSingleWriter();
            testExceptionIsolation();
            testZoneIntegrationConcurrency();
        } catch (Throwable t) {
            System.err.println("Loi nghiem trong khi chay TestZoneSerialExecutor: " + t.getMessage());
            t.printStackTrace();
            failCount++;
        }

        System.out.println("\n=== KET QUA TEST ZONE SERIALEXECUTOR ===");
        System.out.println("Tong pass: " + passCount);
        System.out.println("Tong fail: " + failCount);

        if (failCount > 0) {
            System.exit(1);
        } else {
            System.out.println("TAT CA CAC BAI TEST C2 ZONE SERIALEXECUTOR DEU PASS 100%!");
            System.exit(0);
        }
    }

    /**
     * Test 1: Đảm bảo thứ tự thực thi FIFO tuần tự tuyệt đối 100% cho 10.000 tasks.
     */
    private static void testFifoExecutionOrder() throws Exception {
        System.out.println("\n--- [Test 1] FIFO Execution Order ---");
        SerialExecutor executor = new SerialExecutor();
        int taskCount = 10_000;
        List<Integer> executionOrder = new ArrayList<>(taskCount);
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            executor.execute(() -> {
                executionOrder.add(taskId);
                latch.countDown();
            });
        }

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        assertCondition(finished, "10.000 tasks hoan tat trong thoi gian quy dinh (< 10s)");
        assertCondition(executionOrder.size() == taskCount, "So luong task thuc thi dung 10.000");

        boolean isOrdered = true;
        for (int i = 0; i < taskCount; i++) {
            if (executionOrder.get(i) != i) {
                isOrdered = false;
                break;
            }
        }
        assertCondition(isOrdered, "Thu tu thuc thi cua 10.000 tasks khop 100% FIFO (0 -> 9.999)");
    }

    /**
     * Test 2: Stress test 50 threads dong thoi submit 100.000 tasks.
     * Kiem tra:
     * - Khong bao gio co 2 thread chay dong thoi tai bat ky thoi diem nao (Single-Writer).
     * - Khong bi that thoat bat ky task nao (Atomic counter = 100.000).
     */
    private static void testMultiThreadStressSingleWriter() throws Exception {
        System.out.println("\n--- [Test 2] Multi-Thread Stress Single-Writer (100k tasks, 50 threads) ---");
        SerialExecutor executor = new SerialExecutor();
        int threadCount = 50;
        int tasksPerThread = 2_000;
        int totalTasks = threadCount * tasksPerThread;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalTasks);

        AtomicInteger concurrencyCounter = new AtomicInteger(0);
        AtomicBoolean hasConcurrentExecution = new AtomicBoolean(false);
        int[] sharedUnsafeCounter = new int[1]; // Bien khong dung volatile/atomic de test Single-Writer

        ExecutorService submitters = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            submitters.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < tasksPerThread; i++) {
                        executor.execute(() -> {
                            int current = concurrencyCounter.incrementAndGet();
                            if (current > 1) {
                                hasConcurrentExecution.set(true);
                            }

                            // Thao tac tren non-thread-safe counter
                            sharedUnsafeCounter[0]++;

                            concurrencyCounter.decrementAndGet();
                            doneLatch.countDown();
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(15, TimeUnit.SECONDS);
        submitters.shutdown();

        assertCondition(finished, "100.000 tasks tu 50 threads hoan tat trong < 15s");
        assertCondition(!hasConcurrentExecution.get(), "Single-Writer Invariant: Khong bao gio co 2 luong chay dong thoi");
        assertCondition(sharedUnsafeCounter[0] == totalTasks, "Gia tri shared counter dat dung 100.000 khong bi race condition");
    }

    /**
     * Test 3: Exception Isolation.
     * Khi mot so task nem RuntimeException hoac Error, executor van tiep tuc drain
     * va khong bi deadlock, khong lam ro ri bien WIP.
     */
    private static void testExceptionIsolation() throws Exception {
        System.out.println("\n--- [Test 3] Exception Isolation & Non-blocking Drain ---");
        SerialExecutor executor = new SerialExecutor();
        int totalTasks = 200;
        AtomicInteger successCounter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(totalTasks);

        for (int i = 0; i < totalTasks; i++) {
            final int index = i;
            executor.execute(() -> {
                try {
                    if (index % 5 == 0) {
                        throw new RuntimeException("Test deliberate exception tai index " + index);
                    }
                    successCounter.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(5, TimeUnit.SECONDS);
        assertCondition(finished, "Toan bo 200 tasks hoan tat bat chap ngoai le lien tuc");
        assertCondition(successCounter.get() == 160, "So task thanh cong dung 160 (40 tasks gap exception da duoc isolate an toan)");

        // Doi drain loop tren Virtual Thread hoan tat decrement WIP ve 0
        boolean isDrainDone = false;
        for (int i = 0; i < 50; i++) {
            if (!executor.isRunning()) {
                isDrainDone = true;
                break;
            }
            Thread.sleep(10);
        }
        assertCondition(isDrainDone, "Executor drain het sach va WIP tro ve 0 sau khi ket thuc");
    }

    /**
     * Test 4: Zone Integration Concurrency Test.
     * Mo phong 30 threads (nguoi choi) dong thoi tranh gianh nhat 1 ItemMap duy nhat trong Zone
     * trong luc Zone.update() dang lien tuc chay.
     */
    private static void testZoneIntegrationConcurrency() throws Exception {
        System.out.println("\n--- [Test 4] Zone Integration Concurrency (30 Players Pick 1 Item + Zone.update) ---");

        // Tao Map va Zone dummy (map != null de tranh NullPointerException khi item.update() doc map.mapId)
        models.map.Map dummyMap = new models.map.Map(1, "Dummy Map", (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, new int[1][1], new int[1], 0, 20, new ArrayList<>(), new ArrayList<>());
        Zone zone = new Zone(dummyMap, 0, 20);

        // Tao 1 item duy nhat trong zone
        ItemTemplate template = new ItemTemplate();
        template.id = 100;
        template.name = "Ngoc Rong 1 Sao";
        template.type = 12; // Do quy
        ItemMap item = new ItemMap(zone, template, 1, 500, 300, -1);
        // new ItemMap da tu dong dang ky vao Zone qua zone.addItem() tren SerialExecutor.
        // Cho task addItem khoi tao hoan tat tren SerialExecutor de tranh race condition lam trung lap item trong zone.items.
        CountDownLatch initLatch = new CountDownLatch(1);
        zone.execute(initLatch::countDown);
        initLatch.await(2, TimeUnit.SECONDS);

        int playerCount = 30;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(playerCount);
        AtomicInteger pickedCount = new AtomicInteger(0);

        ExecutorService playerThreads = Executors.newFixedThreadPool(playerCount);

        for (int i = 0; i < playerCount; i++) {
            playerThreads.submit(() -> {
                try {
                    startLatch.await();
                    // Thu nhat item qua Zone.execute
                    zone.execute(() -> {
                        synchronized (zone.items) {
                            if (zone.items.contains(item)) {
                                zone.items.remove(item);
                                pickedCount.incrementAndGet();
                            }
                        }
                        doneLatch.countDown();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Thread mo phong GameLoop update lien tuc
        AtomicBoolean stopUpdate = new AtomicBoolean(false);
        Thread gameLoopThread = Thread.ofVirtual().start(() -> {
            while (!stopUpdate.get()) {
                zone.update();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        });

        startLatch.countDown();
        boolean finished = doneLatch.await(5, TimeUnit.SECONDS);
        stopUpdate.set(true);
        playerThreads.shutdown();

        assertCondition(finished, "Tat ca 30 nguoi choi da thuc hien yeu cau nhat do trong < 5s");
        assertCondition(pickedCount.get() == 1, "Chinh xac duy nhat 1 nguoi choi nhat duoc vat pham (pickedCount = 1)");
        assertCondition(zone.items.isEmpty(), "Item da duoc loai bo an toan khoi danh sach cua Zone ma khong bi dupe");
    }
}

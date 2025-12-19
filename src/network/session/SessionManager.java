package network.session;

import interfaces.ISession;
import utils.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SessionManager {
    
    // Constants - Tối ưu hóa intervals
    private static final long SESSION_CLEANUP_INTERVAL = 30000; // Giảm xuống 30 giây
    private static final long DEAD_SESSION_TIMEOUT = 180000; // Giảm xuống 3 phút
    private static final int MAX_SESSIONS = 5000; // Giảm limit để an toàn hơn
    private static final int CLEANUP_BATCH_SIZE = 100; // Cleanup theo batch
    private static final long MEMORY_CHECK_INTERVAL = 60000; // Check memory mỗi phút
    
    private static volatile SessionManager instance;
    private static final Object instanceLock = new Object();
    
    // Thread-safe collections - Tối ưu initial capacity
    private final CopyOnWriteArrayList<ISession> sessions;
    private final ConcurrentHashMap<Long, ISession> sessionIndex;
    private final ConcurrentHashMap<String, AtomicInteger> ipSessionCount; // Track sessions per IP
    private final ReentrantReadWriteLock sessionLock = new ReentrantReadWriteLock();
    
    // Cleanup scheduler
    private final ScheduledExecutorService cleanupScheduler;
    private final ScheduledExecutorService memoryMonitor;
    
    // Statistics - Sử dụng AtomicLong để thread-safe
    private final AtomicLong totalSessionsCreated = new AtomicLong(0);
    private final AtomicLong totalSessionsRemoved = new AtomicLong(0);
    private volatile long lastCleanupTime = 0;
    private volatile long lastMemoryCheckTime = 0;
    private volatile boolean isShuttingDown = false;

    public static SessionManager gI() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    private SessionManager() {
        // Khởi tạo với initial capacity để giảm resize
        this.sessions = new CopyOnWriteArrayList<>();
        this.sessionIndex = new ConcurrentHashMap<>(1024, 0.75f, 16); // Tối ưu concurrency level
        this.ipSessionCount = new ConcurrentHashMap<>(256, 0.75f, 8);
        
        // Tạo thread pools với daemon threads
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SessionManager-Cleanup");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1); // Lower priority
            return t;
        });
        
        this.memoryMonitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SessionManager-MemoryMonitor");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        
        // Start periodic cleanup and memory monitoring
        startPeriodicCleanup();
        startMemoryMonitoring();
    }

    private void startPeriodicCleanup() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            if (isShuttingDown) return;
            
            try {
                int cleaned = cleanupDeadSessions();
                
                // Log chỉ khi có sessions bị cleanup
                if (cleaned > 0) {
                    logSessionStats();
                }
                
                // Cleanup IP tracking
                cleanupIPTracking();
                
            } catch (Exception e) {
                Logger.logException(SessionManager.class, e, "Error in periodic session cleanup");
            }
        }, SESSION_CLEANUP_INTERVAL, SESSION_CLEANUP_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void startMemoryMonitoring() {
        memoryMonitor.scheduleAtFixedRate(() -> {
            if (isShuttingDown) return;
            
            try {
                checkMemoryUsage();
            } catch (Exception e) {
                Logger.logException(SessionManager.class, e, "Error in memory monitoring");
            }
        }, MEMORY_CHECK_INTERVAL, MEMORY_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void putSession(ISession session) {
        if (session == null) {
            Logger.warning("Attempted to add null session");
            return;
        }
        
        if (isShuttingDown) {
            Logger.warning("SessionManager is shutting down, rejecting new session");
            session.disconnect();
            return;
        }
        
        // Check session limit
        if (sessions.size() >= MAX_SESSIONS) {
            Logger.error("Session limit reached (" + MAX_SESSIONS + "), rejecting new session from " + session.getIP());
            session.disconnect();
            return;
        }
        
        // Check IP-based limits (anti-DDoS)
        String ip = session.getIP();
        if (ip != null) {
            AtomicInteger ipCount = ipSessionCount.computeIfAbsent(ip, k -> new AtomicInteger(0));
            if (ipCount.incrementAndGet() > 10) { // Max 10 connections per IP
                Logger.warning("Too many connections from IP: " + ip + ", rejecting");
                ipCount.decrementAndGet();
                session.disconnect();
                return;
            }
        }
        
        sessionLock.writeLock().lock();
        try {
            // Check for duplicate and cleanup old session
            ISession existingSession = sessionIndex.get(session.getID());
            if (existingSession != null) {
                Logger.warning("Duplicate session ID " + session.getID() + ", cleaning up old session");
                removeSessionInternal(existingSession);
            }
            
            sessions.add(session);
            sessionIndex.put(session.getID(), session);
            totalSessionsCreated.incrementAndGet();
            
            Logger.log("Added session: " + session.getID() + " from " + ip + 
                      " (Total: " + sessions.size() + "/" + MAX_SESSIONS + ")");
                      
        } finally {
            sessionLock.writeLock().unlock();
        }
    }

    public boolean removeSession(ISession session) {
        if (session == null) {
            return false;
        }
        
        sessionLock.writeLock().lock();
        try {
            return removeSessionInternal(session);
        } finally {
            sessionLock.writeLock().unlock();
        }
    }
    
    private boolean removeSessionInternal(ISession session) {
        if (session == null) return false;
        
        boolean removed = sessions.remove(session);
        ISession indexRemoved = sessionIndex.remove(session.getID());
        
        if (removed || indexRemoved != null) {
            totalSessionsRemoved.incrementAndGet();
            
            // Giảm IP counter
            String ip = session.getIP();
            if (ip != null) {
                AtomicInteger ipCount = ipSessionCount.get(ip);
                if (ipCount != null) {
                    if (ipCount.decrementAndGet() <= 0) {
                        ipSessionCount.remove(ip); // Remove IP entry khi không còn sessions
                    }
                }
            }
            
            Logger.log("Removed session: " + session.getID() + " from " + ip + 
                      " (Remaining: " + sessions.size() + ")");
            return true;
        }
        
        return false;
    }

    public List<ISession> getSessions() {
        // Return defensive copy to prevent external modification
        sessionLock.readLock().lock();
        try {
            return new ArrayList<>(sessions);
        } finally {
            sessionLock.readLock().unlock();
        }
    }
    
    /**
     * Get sessions without copying (for internal use only)
     */
//    public List<ISession> getSessionsInternal() {
//        return sessions;
//    }
//
//    public ISession findByID(long id) throws Exception {
//        sessionLock.readLock().lock();
//        try {
//            ISession session = sessionIndex.get(id);
//            if (session == null) {
//                throw new Exception("Session " + id + " does not exist");
//            }
//
//            // Verify session is still valid
//            if (!session.isConnected()) {
//                Logger.warning("Found disconnected session " + id + ", removing");
//                // Cleanup trong background thread để không block
//                cleanupScheduler.submit(() -> removeSession(session));
//                throw new Exception("Session " + id + " is disconnected");
//            }
//
//            return session;
//        } finally {
//            sessionLock.readLock().unlock();
//        }
//    }

    public int getNumSession() {
        return sessions.size();
    }
    
    public int getSessionCount() {
        return sessions.size();
    }
    
    /**
     * Get connected session count
     */
    public int getConnectedSessionCount() {
        return (int) sessions.stream()
                .filter(session -> session != null && session.isConnected())
                .count();
    }

    /**
     * Cleanup dead/disconnected sessions - OPTIMIZED
     */
    public int cleanupDeadSessions() {
        if (isShuttingDown) return 0;
        
        long startTime = System.currentTimeMillis();
        int removedCount = 0;
        
        sessionLock.writeLock().lock();
        try {
            // Sử dụng batch cleanup để tránh lock quá lâu
            List<ISession> deadSessions = new ArrayList<>();
            
            // Tìm dead sessions
            for (ISession session : sessions) {
                if (session == null || !session.isConnected()) {
                    deadSessions.add(session);
                    if (deadSessions.size() >= CLEANUP_BATCH_SIZE) {
                        break; // Cleanup theo batch
                    }
                }
            }
            
            // Remove dead sessions
            for (ISession deadSession : deadSessions) {
                if (removeSessionInternal(deadSession)) {
                    removedCount++;
                }
            }
            
            lastCleanupTime = startTime;
            
            if (removedCount > 0) {
                Logger.log("Cleaned up " + removedCount + " dead sessions in " + 
                          (System.currentTimeMillis() - startTime) + "ms");
            }
            
        } finally {
            sessionLock.writeLock().unlock();
        }
        
        // Trigger GC if removed many sessions
        if (removedCount > 50) {
         //   System.gc();
          //  Logger.log("Triggered garbage collection after removing " + removedCount + " sessions");
        }
        
        return removedCount;
    }
    
    /**
     * Cleanup IP tracking for removed IPs
     */
    private void cleanupIPTracking() {
        try {
            Iterator<String> ipIterator = ipSessionCount.keySet().iterator();
            while (ipIterator.hasNext()) {
                String ip = ipIterator.next();
                AtomicInteger count = ipSessionCount.get(ip);
                if (count == null || count.get() <= 0) {
                    ipIterator.remove();
                }
            }
        } catch (Exception e) {
            Logger.logException(SessionManager.class, e, "Error cleaning up IP tracking");
        }
    }
    
    /**
     * Check memory usage and trigger cleanup if needed
     */
    private void checkMemoryUsage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            double memoryUsagePercent = ((double) usedMemory / maxMemory) * 100;
            
            lastMemoryCheckTime = System.currentTimeMillis();
            
          
            
            // Trigger aggressive cleanup if memory usage is high
            if (memoryUsagePercent > 80) {
                Logger.warning("High memory usage detected (" + String.format("%.1f", memoryUsagePercent) + "%), triggering aggressive cleanup");
                
                // Force cleanup dead sessions
                int cleaned = cleanupDeadSessions();
                
                // If still high memory, suggest GC
                if (memoryUsagePercent > 90) {
                   // System.gc();
                    Logger.warning("Critical memory usage (No GC triggered)");
                }
            }
            
        } catch (Exception e) {
            Logger.logException(SessionManager.class, e, "Error checking memory usage");
        }
    }

    /**
     * Force cleanup all sessions (for shutdown)
     */
    public void forceCleanupAllSessions() {
        Logger.log("Force cleanup all sessions initiated");
        
        isShuttingDown = true;
        
        sessionLock.writeLock().lock();
        try {
            int count = sessions.size();
            
            // Disconnect all sessions in batches
            int batchSize = 50;
            List<ISession> batch = new ArrayList<>(batchSize);
            
            for (ISession session : sessions) {
                if (session != null) {
                    batch.add(session);
                    
                    if (batch.size() >= batchSize) {
                        disconnectBatch(batch);
                        batch.clear();
                        
                        // Small delay between batches to prevent overwhelming
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            // Process remaining sessions
            if (!batch.isEmpty()) {
                disconnectBatch(batch);
            }
            
            // Clear all collections
            sessions.clear();
            sessionIndex.clear();
            ipSessionCount.clear();
            
            Logger.log("Force cleanup completed - removed " + count + " sessions");
            
        } finally {
            sessionLock.writeLock().unlock();
        }
    }
    
    private void disconnectBatch(List<ISession> batch) {
        for (ISession session : batch) {
            try {
                if (session.isConnected()) {
                    session.disconnect();
                }
            } catch (Exception e) {
                Logger.logException(SessionManager.class, e, "Error disconnecting session: " + session.getID());
            }
        }
    }

    /**
     * Get session by IP address - OPTIMIZED
     */
    public List<ISession> getSessionsByIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ISession> result = new ArrayList<>();
        
        sessionLock.readLock().lock();
        try {
            // Estimate size từ IP tracking
            AtomicInteger ipCount = ipSessionCount.get(ip);
            if (ipCount != null && ipCount.get() > 0) {
                result = new ArrayList<>(ipCount.get());
                
                for (ISession session : sessions) {
                    if (session != null && ip.equals(session.getIP())) {
                        result.add(session);
                    }
                }
            }
        } finally {
            sessionLock.readLock().unlock();
        }
        
        return result;
    }

    /**
     * Count sessions from specific IP - OPTIMIZED
     */
//    public int countSessionsByIP(String ip) {
//        if (ip == null || ip.trim().isEmpty()) {
//            return 0;
//        }
//
//        AtomicInteger count = ipSessionCount.get(ip);
//        return count != null ? count.get() : 0;
//    }

    /**
     * Kick sessions from specific IP
     */
//    public int kickSessionsByIP(String ip) {
//        if (ip == null || ip.trim().isEmpty()) {
//            return 0;
//        }
//
//        List<ISession> sessionsToKick = getSessionsByIP(ip);
//
//        for (ISession session : sessionsToKick) {
//            try {
//                session.disconnect();
//            } catch (Exception e) {
//                Logger.logException(SessionManager.class, e, "Error kicking session from IP: " + ip);
//            }
//        }
//
//        Logger.log("Kicked " + sessionsToKick.size() + " sessions from IP: " + ip);
//        return sessionsToKick.size();
//    }

    /**
     * Get memory usage statistics
     */
    public SessionManagerStats getStats() {
        sessionLock.readLock().lock();
        try {
            int totalSessions = sessions.size();
            int connectedSessions = getConnectedSessionCount();
            int deadSessions = totalSessions - connectedSessions;
            
            // Get memory info
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            
            return new SessionManagerStats(
                totalSessions,
                connectedSessions, 
                deadSessions,
                totalSessionsCreated.get(),
                totalSessionsRemoved.get(),
                lastCleanupTime,
                lastMemoryCheckTime,
                usedMemory,
                maxMemory,
                System.currentTimeMillis()
            );
        } finally {
            sessionLock.readLock().unlock();
        }
    }

    /**
     * Log session statistics - OPTIMIZED
     */
    public void logSessionStats() {
        try {
            SessionManagerStats stats = getStats();
            
            // Chỉ log detailed stats mỗi 5 phút
            long timeSinceLastLog = System.currentTimeMillis() - lastMemoryCheckTime;
            boolean detailedLog = timeSinceLastLog > 300000; // 5 minutes
            
            if (detailedLog) {
                Logger.log("========== SESSION MANAGER STATS ==========");
                Logger.log("Sessions: " + stats.connectedSessions + "/" + stats.totalSessions + 
                          " (Connected/Total)");
                Logger.log("Dead Sessions: " + stats.deadSessions);
                Logger.log("Lifetime: Created=" + stats.totalCreated + ", Removed=" + stats.totalRemoved);
                Logger.log("Efficiency: " + String.format("%.1f", stats.getEfficiencyPercentage()) + "%");
                Logger.log("Memory: " + String.format("%.1f", stats.getMemoryUsagePercentage()) + "% " +
                          "(" + (stats.usedMemory / 1024 / 1024) + "MB/" + 
                          (stats.maxMemory / 1024 / 1024) + "MB)");
                Logger.log("IP Tracking: " + ipSessionCount.size() + " unique IPs");
                Logger.log("==========================================");
            } else {
                // Simple log for frequent updates
                Logger.log("Sessions: " + stats.connectedSessions + "/" + stats.totalSessions + 
                          ", Memory: " + String.format("%.1f", stats.getMemoryUsagePercentage()) + "%");
            }
            
            // Memory warnings
            if (stats.deadSessions > 50) {
                Logger.warning("High number of dead sessions detected: " + stats.deadSessions);
            }
            
            if (stats.getMemoryUsagePercentage() > 85) {
                Logger.warning("Critical memory usage: " + String.format("%.1f", stats.getMemoryUsagePercentage()) + "%");
            }
            
        } catch (Exception e) {
            Logger.logException(SessionManager.class, e, "Error logging session stats");
        }
    }

    /**
     * Shutdown session manager - IMPROVED
     */
//    public void shutdown() {
//        Logger.log("SessionManager shutdown initiated");
//        isShuttingDown = true;
//
//        try {
//            // Stop schedulers first
//            shutdownScheduler(cleanupScheduler, "cleanup");
//            shutdownScheduler(memoryMonitor, "memory monitor");
//
//            // Cleanup all sessions
//            forceCleanupAllSessions();
//
//            Logger.log("SessionManager shutdown completed");
//
//        } catch (Exception e) {
//            Logger.logException(SessionManager.class, e, "Error during SessionManager shutdown");
//        }
//    }
    
//    private void shutdownScheduler(ScheduledExecutorService scheduler, String name) {
//        try {
//            scheduler.shutdown();
//            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
//                Logger.warning("Force shutdown " + name + " scheduler");
//                scheduler.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            scheduler.shutdownNow();
//        }
//    }
//
//    /**
//     * Health check for session manager - IMPROVED
//     */
//    public boolean isHealthy() {
//        try {
//            SessionManagerStats stats = getStats();
//
//            // Check multiple health indicators
//            boolean sessionLimitOk = stats.totalSessions < MAX_SESSIONS * 0.9; // 90% threshold
//            boolean deadRatioOk = stats.getDeadSessionRatio() < 0.2; // Less than 20% dead
//            boolean memoryOk = stats.getMemoryUsagePercentage() < 85; // Less than 85% memory
//            boolean schedulersOk = !cleanupScheduler.isShutdown() && !memoryMonitor.isShutdown();
//
//            return sessionLimitOk && deadRatioOk && memoryOk && schedulersOk && !isShuttingDown;
//
//        } catch (Exception e) {
//            Logger.logException(SessionManager.class, e, "Error checking SessionManager health");
//            return false;
//        }
//    }

    /**
     * Session manager statistics - ENHANCED
     */
    public static class SessionManagerStats {
        public final int totalSessions;
        public final int connectedSessions;
        public final int deadSessions;
        public final long totalCreated;
        public final long totalRemoved;
        public final long lastCleanup;
        public final long lastMemoryCheck;
        public final long usedMemory;
        public final long maxMemory;
        public final long currentTime;
        
        public SessionManagerStats(int totalSessions, int connectedSessions, int deadSessions,
                                 long totalCreated, long totalRemoved, long lastCleanup, 
                                 long lastMemoryCheck, long usedMemory, long maxMemory, long currentTime) {
            this.totalSessions = totalSessions;
            this.connectedSessions = connectedSessions;
            this.deadSessions = deadSessions;
            this.totalCreated = totalCreated;
            this.totalRemoved = totalRemoved;
            this.lastCleanup = lastCleanup;
            this.lastMemoryCheck = lastMemoryCheck;
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
            this.currentTime = currentTime;
        }
        
        public double getEfficiencyPercentage() {
            if (totalSessions == 0) return 100.0;
            return ((double) connectedSessions / totalSessions) * 100.0;
        }
        
//        public double getDeadSessionRatio() {
//            if (totalSessions == 0) return 0.0;
//            return (double) deadSessions / totalSessions;
//        }
        
        public double getMemoryUsagePercentage() {
            if (maxMemory == 0) return 0.0;
            return ((double) usedMemory / maxMemory) * 100.0;
        }

//        public long getActiveSessionsLifetime() {
//            return totalCreated - totalRemoved;
//        }
        
        @Override
        public String toString() {
            return "SessionManagerStats{" +
                    "total=" + totalSessions +
                    ", connected=" + connectedSessions +
                    ", dead=" + deadSessions +
                    ", efficiency=" + String.format("%.1f", getEfficiencyPercentage()) + "%" +
                    ", memory=" + String.format("%.1f", getMemoryUsagePercentage()) + "%" +
                    '}';
        }
    }
}
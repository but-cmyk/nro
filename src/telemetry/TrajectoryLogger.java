package telemetry;

import models.map.ItemMap;
import models.map.Zone;
import models.mob.Mob;
import models.player.Player;
import models.skill.Skill;
import utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Real Player Telemetry & Trajectory Logger.
 * Captures high-frequency player decision states (S_t, A_t, R_t) in RAM
 * and writes batch partitioned .jsonl files asynchronously without blocking the game loop.
 */
public class TrajectoryLogger {

    private static TrajectoryLogger instance;

    // Asynchronous Bounded Ring Buffer
    private final BlockingQueue<TrajectoryRecord> queue;

    // Per-player sequential tick tracking
    private final Map<Long, AtomicLong> playerTickMap = new ConcurrentHashMap<>();

    // Metrics & Observability
    private final AtomicLong totalEnqueued = new AtomicLong(0);
    private final AtomicLong totalFlushed = new AtomicLong(0);
    private final AtomicLong totalDroppedQueueFull = new AtomicLong(0);
    private final AtomicLong totalMacroFiltered = new AtomicLong(0);
    private final AtomicLong totalAfkFiltered = new AtomicLong(0);

    // Flusher lifecycle
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread flusherThread;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static TrajectoryLogger gI() {
        if (instance == null) {
            synchronized (TrajectoryLogger.class) {
                if (instance == null) {
                    instance = new TrajectoryLogger();
                }
            }
        }
        return instance;
    }

    private TrajectoryLogger() {
        this.queue = new ArrayBlockingQueue<>(TelemetryConfig.QUEUE_CAPACITY);
    }

    /**
     * Starts the asynchronous flusher thread.
     */
    public synchronized void start() {
        if (running.compareAndSet(false, true)) {
            flusherThread = new Thread(this::flushLoop, "AI-Trajectory-Flusher");
            flusherThread.setDaemon(true);
            flusherThread.start();
            Logger.log(Logger.PURPLE, "[TrajectoryLogger] Asynchronous flusher thread started. Queue capacity: "
                    + TelemetryConfig.QUEUE_CAPACITY + "\n");
        }
    }

    /**
     * Gracefully flushes remaining records and terminates the flusher thread.
     */
    public synchronized void stop() {
        if (running.compareAndSet(true, false)) {
            if (flusherThread != null) {
                flusherThread.interrupt();
                try {
                    flusherThread.join(3000);
                } catch (InterruptedException ignored) {}
            }
            // Drain remaining events in RAM to disk
            drainRemainingQueue();
            Logger.log(Logger.PURPLE, "[TrajectoryLogger] Stopped. Total flushed: " + totalFlushed.get() + "\n");
        }
    }

    // =========================================================================
    // ENQUEUE & ZERO-BLOCKING BUFFER LOGIC
    // =========================================================================

    private void enqueue(TrajectoryRecord record) {
        if (record == null) return;

        boolean offered = queue.offer(record);
        if (!offered) {
            // Buffer full protection: Drop oldest record to maintain continuous real-time feed
            queue.poll();
            queue.offer(record);
            totalDroppedQueueFull.incrementAndGet();
        } else {
            totalEnqueued.incrementAndGet();
        }
    }

    private long getNextTick(long playerId) {
        return playerTickMap.computeIfAbsent(playerId, id -> new AtomicLong(0)).incrementAndGet();
    }

    private String getSessionId(Player player) {
        if (player.getSession() != null) {
            return "sess_" + player.getSession().id + "_" + player.id;
        }
        return "sess_p" + player.id;
    }

    // =========================================================================
    // HOOK DISPATCHERS (CALLED FROM CONTROLLER / SERVICES)
    // =========================================================================

    public void logMove(Player player, int toX, int toY) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "MOVE", toX, toY);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        int dx = toX - player.location.x;
        int dy = toY - player.location.y;

        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_MOVE,
                "MOVE",
                dx, dy,
                "NONE", -1L, -1
        );

        enqueue(record);
    }

    public void logAttackMob(Player player, int mobId, long damageDealt, boolean isDie) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "ATTACK_MOB", player.location.x, player.location.y);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        int skillId = (player.playerSkill != null && player.playerSkill.skillSelect != null && player.playerSkill.skillSelect.template != null)
                ? player.playerSkill.skillSelect.template.id : -1;

        int dx = 0;
        int dy = 0;
        if (player.zone != null && player.zone.mobs != null) {
            for (Mob mob : player.zone.mobs) {
                if (mob != null && mob.id == mobId && mob.location != null) {
                    dx = mob.location.x - player.location.x;
                    dy = mob.location.y - player.location.y;
                    break;
                }
            }
        }

        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_ATTACK_MOB,
                "ATTACK_MOB",
                dx, dy,
                "MOB", (long) mobId, skillId
        );

        record.reward.damageDealt = damageDealt;
        record.reward.isDead = isDie;

        enqueue(record);
    }

    public void logAttackPlayer(Player player, int targetPlayerId, long damageDealt, boolean isDie) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "ATTACK_PLAYER", player.location.x, player.location.y);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        int skillId = (player.playerSkill != null && player.playerSkill.skillSelect != null && player.playerSkill.skillSelect.template != null)
                ? player.playerSkill.skillSelect.template.id : -1;

        int dx = 0;
        int dy = 0;
        if (player.zone != null) {
            Player target = player.zone.getPlayerInMap(targetPlayerId);
            if (target != null && target.location != null) {
                dx = target.location.x - player.location.x;
                dy = target.location.y - player.location.y;
            }
        }

        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_ATTACK_PLAYER,
                "ATTACK_PLAYER",
                dx, dy,
                "PLAYER", (long) targetPlayerId, skillId
        );

        record.reward.damageDealt = damageDealt;
        record.reward.isDead = isDie;

        enqueue(record);
    }

    public void logPickItem(Player player, int itemMapId, int templateId, String itemName, int quantity) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "PICK_ITEM", player.location.x, player.location.y);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_PICK_ITEM,
                "PICK_ITEM",
                0, 0,
                "ITEM", (long) itemMapId, -1
        );

        record.reward.itemPickedValue = quantity;

        enqueue(record);
    }

    public void logEatPea(Player player, int hpKiRecovered) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "EAT_PEA", player.location.x, player.location.y);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_EAT_PEA,
                "EAT_PEA",
                0, 0,
                "SELF", player.id, -1
        );

        record.reward.expGained = hpKiRecovered; // tracks positive resource gain

        enqueue(record);
    }

    public void logChangeZone(Player player, int targetZoneId) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "CHANGE_ZONE", player.location.x, player.location.y);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_CHANGE_ZONE,
                "CHANGE_ZONE",
                0, 0,
                "ZONE", (long) targetZoneId, -1
        );

        enqueue(record);
    }

    public void logUseCapsule(Player player, int targetMapId) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "USE_CAPSULE", player.location.x, player.location.y);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_USE_CAPSULE,
                "USE_CAPSULE",
                0, 0,
                "MAP", (long) targetMapId, -1
        );

        enqueue(record);
    }

    public void logSelectSkill(Player player, int skillTemplateId) {
        if (player == null || player.location == null) return;

        AntiMacroFilter.FilterDecision decision = AntiMacroFilter.gI().evaluate(player, "SELECT_SKILL", player.location.x, player.location.y);
        if (!decision.shouldLog) {
            recordFilterMetric(decision);
            return;
        }

        TrajectoryRecord record = createBaseRecord(player);
        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_SELECT_SKILL,
                "SELECT_SKILL",
                0, 0,
                "NONE", -1L, skillTemplateId
        );

        enqueue(record);
    }

    public void logDamageTaken(Player player, long damageTaken) {
        if (player == null || player.location == null) return;
        if (!TelemetryConfig.isLoggingEnabled(player)) return;

        TrajectoryRecord record = createBaseRecord(player);
        record.action = new TrajectoryRecord.ActionObs(
                TrajectoryRecord.ACTION_MOVE,
                "DAMAGE_REACTION",
                0, 0,
                "SELF", player.id, -1
        );

        record.reward.damageTaken = damageTaken;
        record.reward.isDead = player.isDie();

        enqueue(record);
    }

    private void recordFilterMetric(AntiMacroFilter.FilterDecision decision) {
        if (decision == AntiMacroFilter.FilterDecision.DROP_MACRO) {
            totalMacroFiltered.incrementAndGet();
        } else if (decision == AntiMacroFilter.FilterDecision.DROP_AFK) {
            totalAfkFiltered.incrementAndGet();
        }
    }

    // =========================================================================
    // STATE CAPTURE & SNAPSHOTTING (S_t)
    // =========================================================================

    private TrajectoryRecord createBaseRecord(Player player) {
        TrajectoryRecord record = new TrajectoryRecord();
        record.sessionId = getSessionId(player);
        record.playerId = player.id;
        record.playerName = player.name;
        record.timestamp = System.currentTimeMillis();
        record.tickId = getNextTick(player.id);

        try {
            capturePlayerState(player, record.state);
        } catch (Exception ignored) {
            // Guard against edge cases during zone switches
        }

        return record;
    }

    private void capturePlayerState(Player player, TrajectoryRecord.StateObs s) {
        if (player.zone != null && player.zone.map != null) {
            s.mapId = player.zone.map.mapId;
            s.zoneId = player.zone.zoneId;
        }
        if (player.location != null) {
            s.posX = player.location.x;
            s.posY = player.location.y;
        }
        if (player.nPoint != null) {
            long maxHp = Math.max(1L, player.nPoint.hpMax);
            long maxMp = Math.max(1L, player.nPoint.mpMax);
            s.hpRatio = (float) player.nPoint.hp / maxHp;
            s.mpRatio = (float) player.nPoint.mp / maxMp;
            s.stamina = player.nPoint.stamina;
            s.power = player.nPoint.power;
        }
        s.gender = player.gender;

        // Active Skill Effects
        if (player.effectSkill != null) {
            s.activeEffects.put("shield", player.effectSkill.isShielding);
            s.activeEffects.put("monkey", player.effectSkill.isMonkey);
            s.activeEffects.put("stun", player.effectSkill.isStun);
            s.activeEffects.put("sleep", player.effectSkill.isThoiMien);
            s.activeEffects.put("blind", player.effectSkill.isBlindDCTT);
            s.activeEffects.put("socola", player.effectSkill.isSocola || player.effectSkill.isSocola2);
            s.activeEffects.put("tied", player.effectSkill.anTroi);
            s.activeEffects.put("charge", player.effectSkill.isCharging);
            s.activeEffects.put("invisible", player.effectSkill.isTanHinh);
        }

        // Skill Cooldowns
        if (player.playerSkill != null && player.playerSkill.skills != null) {
            long now = System.currentTimeMillis();
            for (Skill skill : player.playerSkill.skills) {
                if (skill != null && skill.template != null) {
                    long remaining = Math.max(0L, (long) skill.coolDown - (now - skill.lastTimeUseThisSkill));
                    s.cooldownSkills.add(new TrajectoryRecord.SkillCooldownInfo(
                            skill.template.id,
                            skill.coolDown,
                            remaining,
                            remaining == 0
                    ));
                }
            }
        }

        // Local Observation: Nearest Mobs
        if (player.zone != null && player.zone.mobs != null) {
            List<TrajectoryRecord.TargetObs> mobList = new ArrayList<>();
            synchronized (player.zone.mobs) {
                for (Mob mob : player.zone.mobs) {
                    if (mob != null && mob.location != null) {
                        int dx = mob.location.x - s.posX;
                        int dy = mob.location.y - s.posY;
                        double dist = Math.hypot(dx, dy);
                        long maxHp = Math.max(1L, mob.point != null ? mob.point.maxHp : 1L);
                        float hpR = mob.point != null ? (float) mob.point.hp / maxHp : 0f;
                        mobList.add(new TrajectoryRecord.TargetObs(
                                "MOB", mob.id, mob.tempId, dx, dy, dist, hpR, mob.isDie()
                        ));
                    }
                }
            }
            mobList.sort(Comparator.comparingDouble(m -> m.distance));
            int limit = Math.min(TelemetryConfig.MAX_NEARBY_MOBS, mobList.size());
            for (int i = 0; i < limit; i++) {
                s.nearbyTargets.add(mobList.get(i));
            }
        }

        // Local Observation: Nearest Players & Bosses
        if (player.zone != null) {
            List<TrajectoryRecord.TargetObs> entityList = new ArrayList<>();
            List<Player> players = player.zone.getPlayers();
            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    try {
                        Player other = players.get(i);
                        if (other != null && other.id != player.id && other.location != null) {
                            int dx = other.location.x - s.posX;
                            int dy = other.location.y - s.posY;
                            double dist = Math.hypot(dx, dy);
                            long maxHp = Math.max(1L, other.nPoint != null ? other.nPoint.hpMax : 1L);
                            float hpR = other.nPoint != null ? (float) other.nPoint.hp / maxHp : 0f;
                            String type = other.isBoss ? "BOSS" : "PLAYER";
                            entityList.add(new TrajectoryRecord.TargetObs(
                                    type, other.id, other.gender, dx, dy, dist, hpR, other.isDie()
                            ));
                        }
                    } catch (Exception ignored) {}
                }
            }
            entityList.sort(Comparator.comparingDouble(e -> e.distance));
            int limit = Math.min(TelemetryConfig.MAX_NEARBY_PLAYERS, entityList.size());
            for (int i = 0; i < limit; i++) {
                s.nearbyTargets.add(entityList.get(i));
            }
        }

        // Local Observation: Nearby Ground Items within pickup radius
        if (player.zone != null && player.zone.items != null) {
            List<TrajectoryRecord.ItemObs> itemList = new ArrayList<>();
            synchronized (player.zone.items) {
                for (ItemMap it : player.zone.items) {
                    if (it != null && it.itemTemplate != null) {
                        int dx = it.x - s.posX;
                        int dy = it.y - s.posY;
                        double dist = Math.hypot(dx, dy);
                        if (dist <= TelemetryConfig.ITEM_PICK_RADIUS) {
                            itemList.add(new TrajectoryRecord.ItemObs(
                                    it.itemMapId, it.itemTemplate.id, it.itemTemplate.name, dx, dy, dist, it.quantity
                            ));
                        }
                    }
                }
            }
            itemList.sort(Comparator.comparingDouble(it -> it.distance));
            int limit = Math.min(TelemetryConfig.MAX_NEARBY_ITEMS, itemList.size());
            for (int i = 0; i < limit; i++) {
                s.nearbyItems.add(itemList.get(i));
            }
        }
    }

    // =========================================================================
    // ASYNCHRONOUS FLUSHER LOOP (BATCH WRITER TO .JSONL)
    // =========================================================================

    private void flushLoop() {
        List<TrajectoryRecord> batch = new ArrayList<>(TelemetryConfig.BATCH_SIZE);
        long lastFlushTime = System.currentTimeMillis();

        while (running.get()) {
            try {
                // Wait up to 1 second for new incoming records
                TrajectoryRecord record = queue.poll(1000, TimeUnit.MILLISECONDS);
                if (record != null) {
                    batch.add(record);
                    // Drain available elements up to batch size
                    queue.drainTo(batch, TelemetryConfig.BATCH_SIZE - batch.size());
                }

                long now = System.currentTimeMillis();
                boolean sizeReached = batch.size() >= TelemetryConfig.BATCH_SIZE;
                boolean timeReached = (now - lastFlushTime >= TelemetryConfig.FLUSH_INTERVAL_MS) && !batch.isEmpty();

                if (sizeReached || timeReached) {
                    writeBatchToFile(batch);
                    batch.clear();
                    lastFlushTime = now;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Logger.logException(TrajectoryLogger.class, e);
            }
        }

        // Flush any remaining batch on loop exit
        if (!batch.isEmpty()) {
            writeBatchToFile(batch);
            batch.clear();
        }
    }

    private void drainRemainingQueue() {
        List<TrajectoryRecord> remaining = new ArrayList<>();
        queue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            writeBatchToFile(remaining);
        }
    }

    private void writeBatchToFile(List<TrajectoryRecord> records) {
        if (records == null || records.isEmpty()) return;

        try {
            String dateStr = LocalDate.now().format(DATE_FORMATTER);
            File partitionDir = new File(TelemetryConfig.BASE_OUTPUT_DIR, dateStr);
            if (!partitionDir.exists()) {
                partitionDir.mkdirs();
            }

            File targetFile = new File(partitionDir, "trajectories.jsonl");
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(targetFile, true), StandardCharsets.UTF_8))) {
                for (TrajectoryRecord rec : records) {
                    writer.write(rec.toJson());
                    writer.newLine();
                }
                writer.flush();
            }

            totalFlushed.addAndGet(records.size());
        } catch (Exception e) {
            Logger.logException(TrajectoryLogger.class, e);
        }
    }

    // =========================================================================
    // METRICS & API INSPECTION
    // =========================================================================

    public String getStatusJson() {
        return String.format(
                "{\"enabled\":%b,\"queue_size\":%d,\"queue_capacity\":%d,\"total_enqueued\":%d," +
                "\"total_flushed\":%d,\"total_dropped_queue_full\":%d,\"total_macro_filtered\":%d," +
                "\"total_afk_filtered\":%d,\"whitelist_maps_count\":%d,\"whitelist_players_count\":%d}",
                TelemetryConfig.ENABLED,
                queue.size(),
                TelemetryConfig.QUEUE_CAPACITY,
                totalEnqueued.get(),
                totalFlushed.get(),
                totalDroppedQueueFull.get(),
                totalMacroFiltered.get(),
                totalAfkFiltered.get(),
                TelemetryConfig.WHITELIST_MAPS.size(),
                TelemetryConfig.WHITELIST_PLAYERS.size()
        );
    }

    public void resetMetrics() {
        totalEnqueued.set(0);
        totalFlushed.set(0);
        totalDroppedQueueFull.set(0);
        totalMacroFiltered.set(0);
        totalAfkFiltered.set(0);
    }
}


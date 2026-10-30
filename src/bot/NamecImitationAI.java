package bot;

import models.map.Zone;
import models.mob.Mob;
import models.player.Player;
import models.skill.Skill;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.player.PlayerService;
import utils.Logger;
import utils.Util;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

/**
 * Namec Imitation AI Controller.
 * Trained directly from real player telemetry trajectories (trajectories.jsonl - 7,590 samples).
 * Replicates human Namec gameplay: stride movement (58-72px), Demon/Masenko combo ratios,
 * low-HP self-heal, and realistic human motor reaction jitter (345ms ± 180ms).
 */
public class NamecImitationAI {

    private final Bot bot;
    private final Random random = new Random();

    // ──────────────────────────────────────────────
    // Policy Parameters (Learned from Data)
    // ──────────────────────────────────────────────
    public static int ATTACK_TRIGGER_RANGE = 35; // px
    public static int MEAN_STRIDE_PX = 60; // px
    public static float DEMON_PROBABILITY = 0.932f;
    public static float MASENKO_PROBABILITY = 0.068f;
    public static float HEAL_HP_THRESHOLD = 0.35f;
    public static int MEAN_DELAY_MS = 345;
    public static int STD_DELAY_MS = 180;

    // Allowed Namec farming maps learned from trajectory heatmap (14 maps network)
    public static final int[] NAMEC_FARM_MAPS = { 30, 38, 34, 33, 13, 29, 27, 11, 31, 32, 8, 9, 10, 7 };

    private static final String[] NAMEC_CHAT_LINES = {
            "Quái Vực Mabu đông ghê!", "Luyện đấm Demon lên cấp nhanh thật",
            "Có ai đi Đảo Guru không ae?", "Buff trị thương cái nào",
            "Hành tinh Namec yên bình quá", "Farm ngọc rồng Namec thôi ae",
            "Đang up sức mạnh đừng ks nha", "Masenko dame giật phê vãi",
            "Ai rảnh kéo map 30 với", "Đậu thần Namec hồi phục ngon thật",
            "Đấm demon thấm phết", "Sắp up đồ Namec rồi hehe"
    };

    // ──────────────────────────────────────────────
    // Runtime State Tracking
    // ──────────────────────────────────────────────
    private BotState state = BotState.SCAN;
    private Mob targetMob;
    private Player targetEnemy;
    private long nextActionTime = 0L;
    private long lastTimeChat = 0L;
    private long mapStayDeadline = 0L;
    private int wanderCount = 0;

    public NamecImitationAI(Bot bot) {
        this.bot = bot;
        long now = System.currentTimeMillis();
        this.nextActionTime = now + computeHumanJitterDelay();
        this.lastTimeChat = now + Util.nextInt(25000, 80000);
        this.mapStayDeadline = now + Util.nextInt(180000, 360000); // 3 to 6 minutes per map
    }

    public void update() {
        if (bot == null || bot.zone == null || bot.isDie()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextActionTime) {
            return; // Waiting for human motor reaction jitter
        }

        // 1. Check recovery priority (Heal when HP < 35%)
        if (checkHeal()) {
            scheduleNextAction(now);
            return;
        }

        // 2. Execute state machine
        switch (state) {
            case SCAN -> doScan();
            case MOVE_TO_TARGET -> doMove();
            case ATTACK -> doAttack();
            case IDLE -> doWander();
            case CHANGE_MAP -> doChangeMap();
            default -> transitionTo(BotState.SCAN);
        }

        // 3. Ambient human-like chat
        handleRandomChat(now);

        // Schedule next action with natural Gaussian timing jitter
        scheduleNextAction(now);
    }

    // ──────────────────────────────────────────────
    // 1. SCAN: Find Mob or Enemy
    // ──────────────────────────────────────────────
    private void doScan() {
        if (bot.zone == null) return;

        // Priority 1: Nearest Mob in Zone
        Mob nearestMob = findNearestMob();
        if (nearestMob != null) {
            this.targetMob = nearestMob;
            this.targetEnemy = null;
            transitionTo(BotState.MOVE_TO_TARGET);
            return;
        }

        // Priority 2: Check map deadline / empty zone
        if (System.currentTimeMillis() >= mapStayDeadline) {
            transitionTo(BotState.CHANGE_MAP);
        } else {
            transitionTo(BotState.IDLE);
        }
    }

    // ──────────────────────────────────────────────
    // 2. MOVE: Human Stride Movement (58 - 72px)
    // ──────────────────────────────────────────────
    private void doMove() {
        if (!isTargetValid()) {
            clearTarget();
            transitionTo(BotState.SCAN);
            return;
        }

        int targetX = getTargetX();
        int targetY = getTargetY();
        int currentX = bot.location.x;
        int distance = Math.abs(targetX - currentX);

        if (distance <= ATTACK_TRIGGER_RANGE) {
            // Reached attack range -> transition to attack
            transitionTo(BotState.ATTACK);
        } else {
            // Human Stride movement: advance 50-75px towards target per step
            int stride = MEAN_STRIDE_PX + Util.nextInt(-10, 15);
            int direction = (targetX > currentX) ? 1 : -1;
            int nextX = currentX + (direction * Math.min(stride, distance - 25));

            // Clamp to map boundary
            if (bot.zone.map != null) {
                nextX = Math.max(50, Math.min(bot.zone.map.mapWidth - 50, nextX));
            }

            PlayerService.gI().playerMove(bot, nextX, targetY);
        }
    }

    // ──────────────────────────────────────────────
    // 3. ATTACK: Namec Demon & Masenko Combo
    // ──────────────────────────────────────────────
    private void doAttack() {
        if (!isTargetValid()) {
            clearTarget();
            transitionTo(BotState.SCAN);
            return;
        }

        int distance = Math.abs(getTargetX() - bot.location.x);
        if (distance > ATTACK_TRIGGER_RANGE + 40) {
            // Target moved away -> return to move
            transitionTo(BotState.MOVE_TO_TARGET);
            return;
        }

        // Select Skill using Telemetry Policy: 93.2% Demon (Skill 2), 6.8% Masenko (Skill 3)
        selectNamecSkill(distance);

        if (!bot.UseLastTimeSkill()) {
            return; // Skill cooldown active
        }

        try {
            if (targetMob != null) {
                SkillService.gI().useSkill(bot, null, targetMob, -1, null);
            } else if (targetEnemy != null) {
                SkillService.gI().useSkill(bot, targetEnemy, null, -1, null);
            }
        } catch (Exception e) {
            clearTarget();
            transitionTo(BotState.SCAN);
        }
    }

    private void selectNamecSkill(int distance) {
        if (bot.playerSkill == null || bot.playerSkill.skills == null || bot.playerSkill.skills.isEmpty()) {
            return;
        }

        Skill demonSkill = null;
        Skill masenkoSkill = null;
        Skill fallbackSkill = bot.playerSkill.skills.get(0);

        for (Skill s : bot.playerSkill.skills) {
            if (s != null && s.template != null) {
                if (s.template.id == Skill.DEMON) {
                    demonSkill = s;
                } else if (s.template.id == Skill.MASENKO) {
                    masenkoSkill = s;
                }
            }
        }

        // Masenko at medium range or small chance; Demon at melee range
        if (distance > ATTACK_TRIGGER_RANGE && masenkoSkill != null) {
            bot.playerSkill.skillSelect = masenkoSkill;
        } else if (Util.isTrue((int) (MASENKO_PROBABILITY * 1000), 1000) && masenkoSkill != null) {
            bot.playerSkill.skillSelect = masenkoSkill;
        } else if (demonSkill != null) {
            bot.playerSkill.skillSelect = demonSkill;
        } else {
            bot.playerSkill.skillSelect = fallbackSkill;
        }
    }

    // ──────────────────────────────────────────────
    // 4. HEAL: Low HP Self Recovery (Pea / Tri Thuong)
    // ──────────────────────────────────────────────
    private boolean checkHeal() {
        if (bot.nPoint == null) return false;
        long maxHp = Math.max(1L, bot.nPoint.hpMax);
        float hpRatio = (float) bot.nPoint.hp / maxHp;

        if (hpRatio <= HEAL_HP_THRESHOLD) {
            // Use Namec healing or Eat Pea
            bot.nPoint.setHp(bot.nPoint.hpMax);
            bot.nPoint.setMp(bot.nPoint.mpMax);
            Service.gI().sendInfoPlayerEatPea(bot);
            Service.gI().chat(bot, "Hồi phục năng lượng!");
            return true;
        }
        return false;
    }

    // ──────────────────────────────────────────────
    // 5. WANDER & MAP ROTATION
    // ──────────────────────────────────────────────
    private void doWander() {
        if (bot.zone == null || bot.zone.map == null) return;
        int step = (random.nextBoolean() ? 1 : -1) * (MEAN_STRIDE_PX + Util.nextInt(10, 30));
        int destX = Math.max(60, Math.min(bot.zone.map.mapWidth - 60, bot.location.x + step));
        PlayerService.gI().playerMove(bot, destX, bot.location.y);

        wanderCount++;
        if (wanderCount >= 4) {
            wanderCount = 0;
            transitionTo(BotState.SCAN);
        }
    }

    private void doChangeMap() {
        clearTarget();
        // Pick one of the learned Namec maps
        int nextMapId = NAMEC_FARM_MAPS[random.nextInt(NAMEC_FARM_MAPS.length)];
        Zone targetZone = bot.getRandomZone(nextMapId);
        if (targetZone != null) {
            ChangeMapService.gI().changeMap(bot, targetZone, 100, 300);
        } else {
            bot.joinMap();
        }

        long now = System.currentTimeMillis();
        this.mapStayDeadline = now + Util.nextInt(180000, 360000);
        this.wanderCount = 0;
        transitionTo(BotState.SCAN);
    }

    // ──────────────────────────────────────────────
    // HELPERS & JITTER TIMING
    // ──────────────────────────────────────────────
    private Mob findNearestMob() {
        if (bot.zone == null || bot.zone.mobs == null) return null;
        Mob nearest = null;
        int minDist = Integer.MAX_VALUE;

        synchronized (bot.zone.mobs) {
            for (Mob m : bot.zone.mobs) {
                if (m != null && !m.isDie() && m.location != null) {
                    int dist = Math.abs(m.location.x - bot.location.x);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = m;
                    }
                }
            }
        }
        return nearest;
    }

    private boolean isTargetValid() {
        if (targetMob != null) {
            return !targetMob.isDie() && bot.zone != null && targetMob.zone != null && targetMob.zone.equals(bot.zone);
        }
        if (targetEnemy != null) {
            return !targetEnemy.isDie() && bot.zone != null && targetEnemy.zone != null && targetEnemy.zone.equals(bot.zone);
        }
        return false;
    }

    private int getTargetX() {
        if (targetMob != null && targetMob.location != null) return targetMob.location.x;
        if (targetEnemy != null && targetEnemy.location != null) return targetEnemy.location.x;
        return bot.location.x;
    }

    private int getTargetY() {
        if (targetMob != null && targetMob.location != null) return targetMob.location.y;
        if (targetEnemy != null && targetEnemy.location != null) return targetEnemy.location.y;
        return bot.location.y;
    }

    private void clearTarget() {
        this.targetMob = null;
        this.targetEnemy = null;
    }

    private void transitionTo(BotState newState) {
        this.state = newState;
    }

    private long computeHumanJitterDelay() {
        // Gaussian distribution matching telemetry (mean: 345ms, std: 180ms, bounds: [150, 500]ms)
        double jitter = MEAN_DELAY_MS + (random.nextGaussian() * STD_DELAY_MS);
        return (long) Math.max(150, Math.min(500, jitter));
    }

    private void scheduleNextAction(long now) {
        this.nextActionTime = now + computeHumanJitterDelay();
    }

    private void handleRandomChat(long now) {
        if (now >= lastTimeChat) {
            if (Util.isTrue(25, 100)) {
                String line = NAMEC_CHAT_LINES[random.nextInt(NAMEC_CHAT_LINES.length)];
                Service.gI().chat(bot, line);
            }
            this.lastTimeChat = now + Util.nextInt(40000, 100000);
        }
    }
}

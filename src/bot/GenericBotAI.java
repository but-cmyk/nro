package bot;

import models.player.Player;
import models.mob.Mob;
import utils.Util;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.PlayerService;
import services.SkillService;
import services.Service;

public class GenericBotAI {

    // ──────────────────────────────────────────────
    // Hằng số cấu hình
    // ──────────────────────────────────────────────

    /** Khoảng cách tối đa để tấn công (pixel). Nếu xa hơn → di chuyển lại gần. */
    private static final int ATTACK_RANGE = 100;

    /** HP % mà bot bắt đầu heal (0.35 = 35%). */
    private static final float HEAL_THRESHOLD = 0.35f;

    /** Thời gian tối thiểu ở 1 map trước khi đổi (ms). */
    private static final long MAP_STAY_MIN_MS = 150_000L;

    /** Thêm random ms vào thời gian ở map để tránh đổi map đồng loạt. */
    private static final long MAP_STAY_RANDOM_MS = 150_000L;

    /** Thời gian giữa 2 lần chat (ms). */
    private static final long CHAT_COOLDOWN_MIN_MS = 20_000L;
    private static final long CHAT_COOLDOWN_MAX_MS = 90_000L;

    /** Thời gian giữa 2 lần quét tìm boss toàn server (ms). */
    private static final long BOSS_SCAN_COOLDOWN_MS = 3_000L;

    /** Số lần dạo (random move) khi không có mục tiêu trong 1 map. */
    private static final int MAX_WANDER_TICKS = 10;

    // Map ID bị cấm vào (Doanh trại, map sự kiện đặc biệt, v.v.)
    private static final int[] FORBIDDEN_MAP_IDS = { 51, 113 };

    // Các câu chat ngẫu nhiên – đủ đa dạng để trông như người thật
    private static final String[] CHAT_LINES = {
            "lag quá ae ơi", "xin vài hạt đậu với", "up sm mệt ghê",
            "có ai kéo doanh trại không?", "game mượt thật", "hello ae",
            "cho vào bang với nà", "đói quá", "đang up đừng phá nha",
            "boss đâu rồi?", "solo không bạn?", "ăn gì chưa ae",
            "server đông vãi", "ai có hạt đậu cho mình xin",
            "đang cày level đây", "sắp lên rồi hehe"
    };

    // ──────────────────────────────────────────────
    // Trạng thái nội bộ
    // ──────────────────────────────────────────────

    public final Bot bot;

    /** State hiện tại của AI. */
    private BotState state = BotState.SCAN;

    /** Mục tiêu là Boss/Player (Player vì Boss extends Player). */
    private Player targetPlayer;

    /** Mục tiêu là Quái thường. */
    private Mob targetMob;

    /** Thời điểm vào map hiện tại. */
    public long lastTimeChanM;

    /** Thời điểm chat lần cuối. */
    private long lastTimeChat;

    /** Thời điểm quét boss toàn server lần cuối. */
    private long lastTimeBossScan;

    /** Số tick đã lang thang khi không có mục tiêu. */
    private int wanderTicks;

    /** Thời điểm map stay timeout (được tính lại mỗi khi vào map mới). */
    private long mapStayDeadline;

    // ──────────────────────────────────────────────
    // Constructor
    // ──────────────────────────────────────────────

    public GenericBotAI(Bot bot) {
        this.bot = bot;
        long now = System.currentTimeMillis();
        this.lastTimeChanM = now;
        this.lastTimeChat = now + Util.nextInt((int) CHAT_COOLDOWN_MIN_MS, (int) CHAT_COOLDOWN_MAX_MS);
        this.mapStayDeadline = now + MAP_STAY_MIN_MS + Util.nextInt((int) MAP_STAY_RANDOM_MS);
    }

    // ──────────────────────────────────────────────
    // Entry point – gọi từ Bot.update() mỗi tick
    // ──────────────────────────────────────────────

    public void update() {
        if (bot == null || bot.zone == null || bot.isDie())
            return;

        checkHeal();
        runStateMachine();
        randomChat();
    }

    // ──────────────────────────────────────────────
    // State Machine
    // ──────────────────────────────────────────────

    private void runStateMachine() {
        switch (state) {
            case IDLE -> doIdle();
            case SCAN -> doScan();
            case MOVE_TO_TARGET -> doMove();
            case ATTACK -> doAttack();
            case HEAL -> doHeal();
            case CHANGE_MAP -> doChangeMap();
        }
    }

    // ── IDLE ──────────────────────────────────────
    private void doIdle() {
        // Sau 1–2 giây lang thang chuyển sang SCAN
        wanderRandomly();
        wanderTicks++;
        if (wanderTicks >= MAX_WANDER_TICKS) {
            wanderTicks = 0;
            transitionTo(BotState.SCAN);
        }
    }

    // ── SCAN ──────────────────────────────────────
    private void doScan() {
        // Bước 1: tìm boss cùng map (ưu tiên cao nhất)
        Player boss = findBossInCurrentZone();
        if (boss != null) {
            setTarget(boss, null);
            transitionTo(BotState.MOVE_TO_TARGET);
            return;
        }

        // Bước 2: nếu đang ở map PvP → tìm người chơi thù
        if (isPvpMap()) {
            Player enemy = findEnemyPlayer();
            if (enemy != null) {
                setTarget(enemy, null);
                transitionTo(BotState.MOVE_TO_TARGET);
                return;
            }
        }

        // Bước 3: tìm quái thường trong zone
        Mob mob = findMobInZone();
        if (mob != null) {
            setTarget(null, mob);
            transitionTo(BotState.MOVE_TO_TARGET);
            return;
        }

        // Bước 4: không có gì trong zone → thử tìm boss toàn server
        if (Util.canDoWithTime(lastTimeBossScan, BOSS_SCAN_COOLDOWN_MS)) {
            lastTimeBossScan = System.currentTimeMillis();
            Player globalBoss = findBossGlobal();
            if (globalBoss != null) {
                setTarget(globalBoss, null);
                teleportToBossMap(globalBoss);
                transitionTo(BotState.MOVE_TO_TARGET);
                return;
            }
        }

        // Bước 5: thực sự trống rỗng → IDLE (lang thang) hoặc đổi map
        if (shouldChangeMap()) {
            transitionTo(BotState.CHANGE_MAP);
        } else {
            transitionTo(BotState.IDLE);
        }
    }

    // ── MOVE_TO_TARGET ────────────────────────────
    private void doMove() {
        // Kiểm tra mục tiêu còn hợp lệ không
        if (!isTargetValid()) {
            clearTarget();
            transitionTo(BotState.SCAN);
            return;
        }

        int tx = getTargetX();
        int ty = getTargetY();
        int distance = distanceTo(tx, ty);

        if (distance <= ATTACK_RANGE) {
            transitionTo(BotState.ATTACK);
        } else {
            // Di chuyển tiếp cận: thêm offset nhỏ để tránh bot chồng chéo tọa độ
            int destX = tx + Util.nextInt(-30, 30);
            destX = clampX(destX);
            PlayerService.gI().playerMove(bot, destX, ty);
            // Vẫn ở state MOVE, tick sau kiểm tra lại khoảng cách
        }
    }

    // ── ATTACK ────────────────────────────────────
    private void doAttack() {
        // Kiểm tra mục tiêu
        if (!isTargetValid()) {
            clearTarget();
            transitionTo(BotState.SCAN);
            return;
        }

        // Nếu bot đã bị đẩy ra xa → về MOVE
        if (distanceTo(getTargetX(), getTargetY()) > ATTACK_RANGE) {
            transitionTo(BotState.MOVE_TO_TARGET);
            return;
        }

        // Chọn skill thông minh theo HP/MP của bot
        selectSkillSmart();

        if (!bot.UseLastTimeSkill())
            return; // Skill còn cooldown

        try {
            if (targetPlayer != null) {
                SkillService.gI().useSkill(bot, targetPlayer, null, -1, null);
            } else if (targetMob != null) {
                SkillService.gI().useSkill(bot, null, targetMob, -1, null);
            }
        } catch (Exception e) {
            clearTarget();
            transitionTo(BotState.SCAN);
        }

        // Sau khi đánh: kiểm tra đổi map
        if (shouldChangeMap()) {
            transitionTo(BotState.CHANGE_MAP);
        }
    }

    // ── HEAL ──────────────────────────────────────
    private void doHeal() {
        bot.nPoint.hp = bot.nPoint.hpMax;
        bot.nPoint.mp = bot.nPoint.mpMax;
        Service.gI().sendInfoPlayerEatPea(bot);
        // Sau heal → tiếp tục tấn công nếu còn mục tiêu, không thì SCAN
        if (isTargetValid()) {
            transitionTo(BotState.MOVE_TO_TARGET);
        } else {
            transitionTo(BotState.SCAN);
        }
    }

    // ── CHANGE_MAP ────────────────────────────────
    private void doChangeMap() {
        clearTarget();
        bot.joinMap();
        lastTimeChanM = System.currentTimeMillis();
        mapStayDeadline = lastTimeChanM + MAP_STAY_MIN_MS + Util.nextInt((int) MAP_STAY_RANDOM_MS);
        wanderTicks = 0;
        // Calibrate lại chỉ số theo mob của map mới
        // NewBot.calibrateStatsForZone(bot);
        transitionTo(BotState.SCAN);
    }

    // ──────────────────────────────────────────────
    // Heal check (ưu tiên cao nhất, trước state machine)
    // ──────────────────────────────────────────────

    private void checkHeal() {
        if (state == BotState.HEAL)
            return; // đang heal rồi
        if (bot.nPoint.hp <= bot.nPoint.hpMax * HEAL_THRESHOLD) {
            transitionTo(BotState.HEAL);
        }
    }

    // ──────────────────────────────────────────────
    // Tìm mục tiêu
    // ──────────────────────────────────────────────

    /** Tìm Boss (isBoss = true) trong zone hiện tại của bot. */
    private Player findBossInCurrentZone() {
        if (bot.zone == null || bot.zone.getPlayers() == null)
            return null;
        for (Player p : bot.zone.getPlayers()) {
            if (p != null && p.isBoss && !p.isDie() && !p.equals(bot))
                return p;
        }
        return null;
    }

    /** Tìm người chơi thật (không phải bot, không phải bot dead) để PvP. */
    private Player findEnemyPlayer() {
        if (bot.zone == null || bot.zone.getPlayers() == null)
            return null;
        for (Player p : bot.zone.getPlayers()) {
            if (p != null && !p.equals(bot) && !p.isDie() && !p.isBot && !p.isNewPet) {
                return p;
            }
        }
        return null;
    }

    /**
     * Tìm Quái chưa chết trong zone. Ưu tiên quái gần nhất để tránh di chuyển
     * nhiều.
     */
    private Mob findMobInZone() {
        if (bot.zone == null || bot.zone.mobs == null || bot.zone.mobs.isEmpty())
            return null;
        Mob nearest = null;
        int nearestDist = Integer.MAX_VALUE;
        for (Mob m : bot.zone.mobs) {
            if (m == null || m.isDie())
                continue;
            int d = distanceTo(m.location.x, m.location.y);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = m;
            }
        }
        return nearest;
    }

    /**
     * Quét Boss toàn server (dùng BossManager nếu có).
     * Ưu tiên boss CÙNG map trước, sau đó boss gần nhất có thể đến được.
     */
    private Player findBossGlobal() {
        try {
            // Thử dùng BossManager nếu project có
            Class<?> bm = Class.forName("managers.boss.BossManager");
            Object instance = bm.getMethod("gI").invoke(null);
            @SuppressWarnings("unchecked")
            java.util.List<Player> bosses = (java.util.List<Player>) bm.getMethod("getBosses").invoke(instance);
            if (bosses == null)
                return null;

            Player samemap = null, anymap = null;
            for (Player b : bosses) {
                if (b == null || b.isDie() || b.zone == null)
                    continue;
                if (isForbiddenMap(b.zone.map.mapId))
                    continue;
                if (bot.zone != null && b.zone.map.mapId == bot.zone.map.mapId && samemap == null) {
                    samemap = b;
                }
                if (anymap == null)
                    anymap = b;
            }
            return samemap != null ? samemap : anymap;
        } catch (Exception e) {
            return null; // BossManager không tồn tại → bỏ qua
        }
    }

    // ──────────────────────────────────────────────
    // Chọn skill thông minh
    // ──────────────────────────────────────────────

    /**
     * Chọn skill theo tình huống:
     * - MP cao → ưu tiên skill mạnh (index 1 nếu có)
     * - MP thấp → dùng skill 0 (tốn ít MP)
     * - Thỉnh thoảng random để trông tự nhiên
     */
    private void selectSkillSmart() {
        var skills = bot.playerSkill.skills;
        if (skills == null || skills.isEmpty())
            return;

        if (skills.size() == 1) {
            bot.playerSkill.skillSelect = skills.get(0);
            return;
        }

        float mpRatio = (float) bot.nPoint.mp / Math.max(bot.nPoint.mpMax, 1);
        boolean preferStrong = mpRatio > 0.3f; // Còn >= 30% MP → dùng skill mạnh

        if (preferStrong && Util.isTrue(60, 100)) {
            // 60% khả năng chọn skill 1 (mạnh hơn) khi MP đủ
            bot.playerSkill.skillSelect = skills.get(1);
        } else {
            bot.playerSkill.skillSelect = skills.get(0);
        }
    }

    // ──────────────────────────────────────────────
    // Chat ngẫu nhiên
    // ──────────────────────────────────────────────

    private void randomChat() {
        long now = System.currentTimeMillis();
        if (now < lastTimeChat)
            return;
        if (Util.isTrue(3, 10)) { // 30% khả năng thực sự chat khi đến giờ
            String line = CHAT_LINES[Util.nextInt(CHAT_LINES.length)];
            Service.gI().chat(bot, line);
        }
        lastTimeChat = now + Util.nextLong(CHAT_COOLDOWN_MIN_MS, CHAT_COOLDOWN_MAX_MS);
    }

    // ──────────────────────────────────────────────
    // Teleport đến map của boss (toàn server)
    // ──────────────────────────────────────────────

    private void teleportToBossMap(Player boss) {
        if (boss.zone == null)
            return;
        if (bot.zone == null || bot.zone.map.mapId != boss.zone.map.mapId) {
            try {
                ChangeMapService.gI().goToMap(bot, boss.zone);
                if (bot.zone != null)
                    bot.zone.load_Me_To_Another(bot);
            } catch (Exception ignored) {
            }
        }
    }

    // ──────────────────────────────────────────────
    // Helper: di chuyển ngẫu nhiên (lang thang)
    // ──────────────────────────────────────────────

    private void wanderRandomly() {
        if (bot.zone == null)
            return;
        int rx = Util.nextInt(80, bot.zone.map.mapWidth - 80);
        PlayerService.gI().playerMove(bot, rx, bot.location.y);
    }

    // ──────────────────────────────────────────────
    // Kiểm tra điều kiện đổi map
    // ──────────────────────────────────────────────

    private boolean shouldChangeMap() {
        return System.currentTimeMillis() >= mapStayDeadline;
    }

    // ──────────────────────────────────────────────
    // Kiểm tra map PvP (Võ đài)
    // ──────────────────────────────────────────────

    private boolean isPvpMap() {
        if (bot.zone == null)
            return false;
        return isKnownPvpMap(bot.zone.map.mapId);
    }

    private static boolean isKnownPvpMap(int mapId) {
        return mapId == 51 || mapId == 52 || mapId == 113 || mapId == 129;
    }

    // ──────────────────────────────────────────────
    // Kiểm tra map bị cấm
    // ──────────────────────────────────────────────

    private boolean isForbiddenMap(int mapId) {
        for (int id : FORBIDDEN_MAP_IDS) {
            if (id == mapId)
                return true;
        }
        try {
            return MapService.gI().isMapDoanhTrai(mapId)
                    || MapService.gI().isMapBlackBallWar(mapId)
                    || MapService.gI().isMapBanDoKhoBau(mapId)
                    || MapService.gI().isMapMaBu(mapId)
                    || MapService.gI().isMapConDuongRanDoc(mapId);
        } catch (Exception e) {
            return false;
        }
    }

    // ──────────────────────────────────────────────
    // Utility: target, distance, clamp
    // ──────────────────────────────────────────────

    private void setTarget(Player player, Mob mob) {
        this.targetPlayer = player;
        this.targetMob = mob;
    }

    private void clearTarget() {
        this.targetPlayer = null;
        this.targetMob = null;
    }

    private boolean isTargetValid() {
        if (targetPlayer != null) {
            return !targetPlayer.isDie()
                    && targetPlayer.zone != null
                    && bot.zone != null
                    && targetPlayer.zone == bot.zone;
        }
        if (targetMob != null) {
            return !targetMob.isDie()
                    && bot.zone != null
                    && bot.zone.mobs != null
                    && bot.zone.mobs.contains(targetMob);
        }
        return false;
    }

    private int getTargetX() {
        if (targetPlayer != null)
            return targetPlayer.location.x;
        if (targetMob != null)
            return targetMob.location.x;
        return bot.location.x;
    }

    private int getTargetY() {
        if (targetPlayer != null)
            return targetPlayer.location.y;
        if (targetMob != null)
            return targetMob.location.y;
        return bot.location.y;
    }

    private int distanceTo(int tx, int ty) {
        int dx = bot.location.x - tx;
        int dy = bot.location.y - ty;
        return (int) Math.sqrt((double) dx * dx + (double) dy * dy);
    }

    private int clampX(int x) {
        if (bot.zone == null)
            return x;
        int w = bot.zone.map.mapWidth;
        return Math.max(50, Math.min(x, w - 50));
    }

    private void transitionTo(BotState next) {
        this.state = next;
    }

    // ──────────────────────────────────────────────
    // Getter – cho phép BotManager log state nếu cần
    // ──────────────────────────────────────────────

    public BotState getState() {
        return state;
    }
}
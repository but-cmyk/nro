package telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MDP Trajectory Schema for AI Player Training (Imitation Learning & Offline RL).
 * Represents a single decision step: (S_t, A_t, R_t).
 */
public class TrajectoryRecord {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    // Discrete Action IDs
    public static final int ACTION_MOVE = 0;
    public static final int ACTION_ATTACK_MOB = 1;
    public static final int ACTION_ATTACK_PLAYER = 2;
    public static final int ACTION_PICK_ITEM = 3;
    public static final int ACTION_EAT_PEA = 4;
    public static final int ACTION_CHANGE_ZONE = 5;
    public static final int ACTION_USE_CAPSULE = 6;
    public static final int ACTION_SELECT_SKILL = 7;

    @SerializedName("session_id")
    public String sessionId;

    @SerializedName("player_id")
    public long playerId;

    @SerializedName("player_name")
    public String playerName;

    @SerializedName("timestamp")
    public long timestamp;

    @SerializedName("tick_id")
    public long tickId;

    @SerializedName("state")
    public StateObs state;

    @SerializedName("action")
    public ActionObs action;

    @SerializedName("reward")
    public RewardObs reward;

    public TrajectoryRecord() {
        this.timestamp = System.currentTimeMillis();
        this.state = new StateObs();
        this.action = new ActionObs();
        this.reward = new RewardObs();
    }

    /**
     * Serializes this record into a single-line JSON string (.jsonl format).
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    // -------------------------------------------------------------
    // STATE OBSERVATION (S_t)
    // -------------------------------------------------------------
    public static class StateObs {
        @SerializedName("map_id")
        public int mapId;

        @SerializedName("zone_id")
        public int zoneId;

        @SerializedName("pos_x")
        public int posX;

        @SerializedName("pos_y")
        public int posY;

        @SerializedName("hp_ratio")
        public float hpRatio;

        @SerializedName("mp_ratio")
        public float mpRatio;

        @SerializedName("stamina")
        public int stamina;

        @SerializedName("power")
        public long power;

        @SerializedName("gender")
        public byte gender; // 0: Trai Dat, 1: Namec, 2: Xayda

        @SerializedName("active_effects")
        public Map<String, Boolean> activeEffects = new HashMap<>();

        @SerializedName("cooldown_skills")
        public List<SkillCooldownInfo> cooldownSkills = new ArrayList<>();

        @SerializedName("nearby_targets")
        public List<TargetObs> nearbyTargets = new ArrayList<>();

        @SerializedName("nearby_items")
        public List<ItemObs> nearbyItems = new ArrayList<>();
    }

    public static class SkillCooldownInfo {
        @SerializedName("skill_id")
        public int skillId;

        @SerializedName("cooldown_ms")
        public int cooldownMs;

        @SerializedName("remaining_ms")
        public long remainingMs;

        @SerializedName("is_ready")
        public boolean isReady;

        public SkillCooldownInfo() {}

        public SkillCooldownInfo(int skillId, int cooldownMs, long remainingMs, boolean isReady) {
            this.skillId = skillId;
            this.cooldownMs = cooldownMs;
            this.remainingMs = remainingMs;
            this.isReady = isReady;
        }
    }

    public static class TargetObs {
        @SerializedName("entity_type")
        public String entityType; // "MOB", "BOSS", "PLAYER"

        @SerializedName("id")
        public long id;

        @SerializedName("temp_id")
        public int tempId;

        @SerializedName("dx")
        public int dx;

        @SerializedName("dy")
        public int dy;

        @SerializedName("distance")
        public double distance;

        @SerializedName("hp_ratio")
        public float hpRatio;

        @SerializedName("is_die")
        public boolean isDie;

        public TargetObs() {}

        public TargetObs(String entityType, long id, int tempId, int dx, int dy, double distance, float hpRatio, boolean isDie) {
            this.entityType = entityType;
            this.id = id;
            this.tempId = tempId;
            this.dx = dx;
            this.dy = dy;
            this.distance = Math.round(distance * 10.0) / 10.0;
            this.hpRatio = Math.round(hpRatio * 100.0f) / 100.0f;
            this.isDie = isDie;
        }
    }

    public static class ItemObs {
        @SerializedName("item_map_id")
        public int itemMapId;

        @SerializedName("template_id")
        public int templateId;

        @SerializedName("name")
        public String name;

        @SerializedName("dx")
        public int dx;

        @SerializedName("dy")
        public int dy;

        @SerializedName("distance")
        public double distance;

        @SerializedName("quantity")
        public int quantity;

        public ItemObs() {}

        public ItemObs(int itemMapId, int templateId, String name, int dx, int dy, double distance, int quantity) {
            this.itemMapId = itemMapId;
            this.templateId = templateId;
            this.name = name;
            this.dx = dx;
            this.dy = dy;
            this.distance = Math.round(distance * 10.0) / 10.0;
            this.quantity = quantity;
        }
    }

    // -------------------------------------------------------------
    // ACTION OBSERVATION (A_t)
    // -------------------------------------------------------------
    public static class ActionObs {
        @SerializedName("action_id")
        public int actionId;

        @SerializedName("action_name")
        public String actionName;

        @SerializedName("delta_x")
        public int deltaX;

        @SerializedName("delta_y")
        public int deltaY;

        @SerializedName("target_type")
        public String targetType; // "MOB", "PLAYER", "ITEM", "ZONE", "MAP", "NONE"

        @SerializedName("target_id")
        public long targetId;

        @SerializedName("skill_id")
        public int skillId;

        public ActionObs() {}

        public ActionObs(int actionId, String actionName, int deltaX, int deltaY, String targetType, long targetId, int skillId) {
            this.actionId = actionId;
            this.actionName = actionName;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.targetType = targetType;
            this.targetId = targetId;
            this.skillId = skillId;
        }
    }

    // -------------------------------------------------------------
    // REWARD & OUTCOME OBSERVATION (R_t)
    // -------------------------------------------------------------
    public static class RewardObs {
        @SerializedName("damage_dealt")
        public long damageDealt;

        @SerializedName("damage_taken")
        public long damageTaken;

        @SerializedName("exp_gained")
        public long expGained;

        @SerializedName("item_picked_value")
        public int itemPickedValue;

        @SerializedName("is_dead")
        public boolean isDead;

        public RewardObs() {}

        public RewardObs(long damageDealt, long damageTaken, long expGained, int itemPickedValue, boolean isDead) {
            this.damageDealt = damageDealt;
            this.damageTaken = damageTaken;
            this.expGained = expGained;
            this.itemPickedValue = itemPickedValue;
            this.isDead = isDead;
        }
    }
}


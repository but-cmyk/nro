package bot;

/**
 * Các trạng thái trong State Machine của Bot.
 * Bot sẽ chuyển đổi qua lại giữa các state này mỗi tick.
 *
 *   IDLE ──► SCAN ──► MOVE_TO_TARGET ──► ATTACK
 *    ▲                                      │
 *    └──────────── HEAL ◄───────────────────┘
 *         (nếu HP thấp)
 *    CHANGE_MAP (timeout hoặc zone trống)
 */
public enum BotState {
    /** Đứng yên, chờ tìm mục tiêu */
    IDLE,

    /** Đang quét tìm Boss / Quái / Người chơi mục tiêu */
    SCAN,

    /** Di chuyển tiếp cận mục tiêu */
    MOVE_TO_TARGET,

    /** Đang đánh mục tiêu */
    ATTACK,

    /** Máu thấp – ăn đậu / hồi máu */
    HEAL,

    /** Đổi map (hết quái hoặc hết thời gian ở map này) */
    CHANGE_MAP
}
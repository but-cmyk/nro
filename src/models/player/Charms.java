package models.player;

public class Charms {

    /**
     * Thời gian tối đa mà bùa có thể được stack: 72 giờ.
     * Ngăn chặn player whale stack bùa vô hạn, thu hẹp khoảng cách balance.
     */
    public static final long MAX_CHARM_DURATION = 72 * 60 * 60 * 1000L; // 72 giờ

    public long tdTriTue;
    public long tdManhMe;
    public long tdDaTrau;
    public long tdOaiHung;
    public long tdBatTu;
    public long tdDeoDai;
    public long tdThuHut;
    public long tdDeTu;
    public long tdTriTue3;
    public long tdTriTue4;

    public long lastTimeSubMinTriTueX4;

    /**
     * Helper: Reset nếu hết hạn, cộng thêm thời gian, rồi clamp tối đa MAX_CHARM_DURATION.
     */
    private long clampCharm(long currentExpiry, int min) {
        long now = System.currentTimeMillis();
        if (currentExpiry < now) {
            currentExpiry = now;
        }
        currentExpiry += min * 60 * 1000L;
        // Clamp: không được vượt quá now + MAX_CHARM_DURATION
        long maxExpiry = now + MAX_CHARM_DURATION;
        if (currentExpiry > maxExpiry) {
            currentExpiry = maxExpiry;
        }
        return currentExpiry;
    }

    public void addTimeCharms(int itemId, int min) {
        switch (itemId) {
            case 213:
                tdTriTue = clampCharm(tdTriTue, min);
                break;
            case 214:
                tdManhMe = clampCharm(tdManhMe, min);
                break;
            case 215:
                tdDaTrau = clampCharm(tdDaTrau, min);
                break;
            case 216:
                tdOaiHung = clampCharm(tdOaiHung, min);
                break;
            case 217:
                tdBatTu = clampCharm(tdBatTu, min);
                break;
            case 218:
                tdDeoDai = clampCharm(tdDeoDai, min);
                break;
            case 219:
                tdThuHut = clampCharm(tdThuHut, min);
                break;
            case 522:
                tdDeTu = clampCharm(tdDeTu, min);
                break;
            case 671:
                tdTriTue3 = clampCharm(tdTriTue3, min);
                break;
            case 672:
                tdTriTue4 = clampCharm(tdTriTue4, min);
                break;
        }
    }

    public void dispose() {
    }
}

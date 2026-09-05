package models.boss.boss_list.Broly;

import models.boss.Boss;
import models.boss.BossData;
import consts.BossID;
import consts.BossStatus;
import static consts.BossType.BROLY;
import consts.ConstPlayer;
import models.map.Zone;
import models.player.Player;
import managers.boss.BrolyManager;
import services.map.ChangeMapService;
import models.skill.Skill;
import utils.Util;

public class DetuBroly extends Boss {

    private final Boss master; // Chủ nhân

    public DetuBroly(Zone zone, int x, int y, Boss master) throws Exception {
        super(BROLY, BossID.DETU_BROLY, false, true, new BossData(
                "Đệ tử",
                ConstPlayer.XAYDA,
                getRandomOutfit(),
                100,
                new int[]{1000},
                new int[]{5},
                new int[][]{{Skill.DRAGON, 7, 100}},
                new String[]{},
                new String[]{},
                new String[]{},
                0
        ));
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
        this.master = master;

        this.currentLevel = 0;
        this.initBase();
        this.joinMap();
    }

    private static short[] getRandomOutfit() {
        int rnd = Util.nextInt(0, 2);
        switch (rnd) {
            case 0: return new short[]{285, 286, 287, -1, -1, -1};
            case 1: return new short[]{288, 289, 290, -1, -1, -1};
            default: return new short[]{282, 283, 284, -1, -1, -1};
        }
    }

    @Override
    public void joinMap() {
        // Random HP 2000 - 3000 như yêu cầu trước
        this.nPoint.hpMax = Util.nextInt(2000, 3000);
        this.nPoint.hp = this.nPoint.hpMax;

        ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void reward(Player plKill) {
        // Không có thưởng khi giết đệ
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) return 0;
        damage = this.nPoint.subDameInjureWithDeff(damage);
        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public void attack() {
        // 1. Nếu đệ tử đã chết (hồn ma), thì đứng im, không làm gì cả
        if (this.isDie()) {
            return;
        }

        // 2. Kiểm tra sư phụ (để xử lý trường hợp sư phụ biến mất thì đệ biến mất theo)
        if (this.master == null || this.master.isDie() || this.master.zone != this.zone) {
            // Ở đây gọi leaveMap là hợp lý vì sư phụ đã chết/mất tích
            this.leaveMap();
            return;
        }

        // 3. Logic di chuyển
        if (Util.getDistance(this, this.master) > 120) {
            int dir = Util.getOne(-1, 1);
            int distance = Util.nextInt(70, 100);
            int nextX = this.master.location.x + (dir * distance);
            this.moveTo(nextX, this.master.location.y);
        }
    }

    @Override
    public void leaveMap() {
        // --- LOGIC QUAN TRỌNG: GIỮ HỒN MA ---
        // Nếu sư phụ còn sống VÀ sư phụ đang ở cùng map
        // Thì đệ tử KHÔNG ĐƯỢC biến mất (giữ nguyên trạng thái hồn ma)
        if (this.master != null && !this.master.isDie() && this.master.zone == this.zone) {
            return;
        }
        // ------------------------------------

        ChangeMapService.gI().exitMap(this);
        BrolyManager.gI().removeBoss(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        this.dispose();
    }

    @Override
    public void rest() {
        // Đệ tử không bao giờ tự hồi sinh từ trạng thái REST
        // Chỉ được sinh ra khi SuperBroly xuất hiện
    }
}
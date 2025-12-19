package models.boss.boss_list.Black;

import consts.BossID;
import models.boss.*;
import consts.ConstPlayer;
import consts.ConstTask;
import models.map.ItemMap;
import models.player.Player;
import server.Manager;
import services.EffectSkillService;
import services.Service;
import utils.Util;
import services.SkillService;
import services.TaskService;
import utils.ItemUtil;

public class BlackGoku extends Boss {

    private long lastTimeUpdate;
    private int timeToLeaveMap;

    public BlackGoku() throws Exception {
        // Khởi tạo boss với 2 cấp độ: Thường và Super
        super(BossID.BLACK_GOKU, false, true, BossesData.BLACK_GOKU, BossesData.SUPER_BLACK_GOKU);
    }

    @Override
    public void reward(Player plKill) {
        // 1. Cộng điểm trùm
        plKill.effect.addPointTrumSanBoss();

        // 2. Rớt vật phẩm nhiệm vụ (Task 31)
        if (TaskService.gI().getIdTask(plKill) == ConstTask.TASK_31_0) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 992, 1, this.location.x, this.location.y, plKill.id));
        }

        // 3. Rớt đồ thần linh (Fix lỗi mảng item cũ)
        if (Util.isTrue(10, 100)) { // Tỉ lệ 10%
            // Lấy ngẫu nhiên 1 item trong danh sách
            int randomIndex = Util.nextInt(0, Manager.itemIds_GIAY_TL.length - 1);
            int itemId = Manager.itemIds_GIAY_TL[randomIndex];

            Service.gI().dropItemMap(this.zone,
                    ItemUtil.ratiDTL(zone, itemId, 1, this.location.x, this.location.y, plKill.id));
        }

        // 4. Cộng chỉ số nhiệm vụ "Hạ gục boss" (Danh hiệu)
        if (plKill.playerTask.taskdh.Hagucboss < 30) {
            plKill.playerTask.taskdh.Hagucboss++;
            plKill.playerTask.taskdh.ResetTime = System.currentTimeMillis();
            // Nếu source có hàm sendUpdateTaskDanhHieu thì gọi ở đây
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }

        // Level 2 (Super Black Goku) giảm 50% sát thương
        if (this.currentLevel > 0) {
            damage /= 2;
        }

        // Tính toán sát thương thực dựa trên chỉ số phòng thủ (Bỏ logic trừ dame random gây lỗi 0 dame)
        long realDamage = this.nPoint.subDameInjureWithDeff(damage);

        // Xử lý khiên (Shield)
        if (!piercing && effectSkill.isShielding) {
            if (realDamage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            realDamage = 1;
        }

        // Trừ HP
        this.nPoint.subHP(realDamage);

        // Kiểm tra chết
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }

        return (int) realDamage;
    }

    @Override
    public void joinMap() {
        // Đặt tên kèm số ngẫu nhiên
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        super.joinMap();

        // Khởi tạo thời gian đếm ngược rời map
        this.lastTimeUpdate = System.currentTimeMillis();
        this.timeToLeaveMap = Util.nextInt(600000, 900000); // 10 - 15 phút
    }

    @Override
    public void autoLeaveMap() {
        // LOGIC QUAN TRỌNG:
        // Nếu trong map CÓ NGƯỜI CHƠI -> Reset thời gian đếm ngược -> Boss không bao giờ biến mất
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            this.lastTimeUpdate = System.currentTimeMillis();
            return;
        }

        // Nếu map KHÔNG CÓ AI -> Kiểm tra thời gian để rời đi
        if (Util.canDoWithTime(this.lastTimeUpdate, this.timeToLeaveMap)) {
            this.leaveMapNew();
        }
    }

    @Override
    public void attack() {
        // Kiểm tra cooldown đánh và trạng thái PK
        if (!Util.canDoWithTime(this.lastTimeAttack, 100) || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }

        this.lastTimeAttack = System.currentTimeMillis();
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
                return;
            }

            int dis = Util.getDistance(this, pl);

            // AI Di chuyển
            if (dis > 450) {
                // Dịch chuyển tức thời đến cạnh người chơi (Random trái/phải 24px)
                int offset = (Util.nextInt(0, 1) == 0) ? -24 : 24;
                move(pl.location.x + offset, pl.location.y);
            } else if (dis > 100) {
                // Chạy bộ tiếp cận
                int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                int moveDist = Util.nextInt(50, 100);
                move(this.location.x + (dir * moveDist), pl.location.y);
            } else {
                // Đủ tầm đánh
                // 30% tỉ lệ di chuyển nhẹ để "múa" (né skill)
                if (Util.isTrue(30, 100)) {
                    int moveDist = Util.nextInt(30);
                    int dir = (Util.nextInt(0, 1) == 0) ? -1 : 1;
                    move(pl.location.x + (dir * moveDist), this.location.y);
                }

                // Chọn skill ngẫu nhiên và tung chiêu
                if (this.playerSkill.skills.size() > 0) {
                    this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            }
        } catch (Exception ex) {
            // Log lỗi để debug nếu cần, tránh in spam console quá nhiều
            System.out.println("Boss Black Goku Error Attack");
        }
    }
}
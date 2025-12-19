package models.boss.boss_list.Cooler;

import models.boss.Boss;
import consts.BossID;
import models.boss.BossesData;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;
import server.Manager;
import services.TaskService;
import utils.ItemUtil;

public class Cooler extends Boss {

    private long lastTimeUpdate;

    public Cooler() throws Exception {
        super(BossID.COOLER, BossesData.COOLER, BossesData.COOLER_2);
    }

    @Override
    public void reward(Player plKill) {
        // Cộng điểm trùm
        if (plKill.effect != null) {
            plKill.effect.addPointTrumSanBoss();
        }

        // Check nhiệm vụ
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // --- LOGIC DROP ĐỒ ---
        int x = this.location.x;
        int y = this.location.y;
        long playerID = plKill.id;

        // Tỉ lệ drop Thần Linh / Hủy Diệt (Sử dụng Util.nextInt thay vì new Random)
        // Lưu ý: Đã sửa lỗi không lấy được phần tử cuối mảng
        if (Manager.itemIds_tl_GN.length > 0 && Util.isTrue(2, 100)) {
            int randomIdx = Util.nextInt(0, Manager.itemIds_tl_GN.length - 1);
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiDTL(zone, Manager.itemIds_tl_GN[randomIdx], 1, x, y, playerID));
        }

        if (Manager.itemIds_tl_AWJ.length > 0 && Util.isTrue(5, 100)) {
            int randomIdx = Util.nextInt(0, Manager.itemIds_tl_AWJ.length - 1);
            Service.gI().dropItemMap(this.zone, ItemUtil.ratiDTL(zone, Manager.itemIds_tl_AWJ[randomIdx], 1, x, y, playerID));
        }

        // Logic drop Vàng (hoặc item cố định ID 190)
        // Đã sửa lỗi: Thêm dòng dropItemMap để vật phẩm thực sự rơi ra đất
        // if (Util.isTrue(1, 3)) { // Nếu muốn có tỉ lệ thì bỏ comment
        ItemMap itemMap = new ItemMap(this.zone, 190, 31000, x, this.zone.map.yPhysicInTop(x, y - 24), playerID);
        itemMap.options.add(new Item.ItemOption(30, 0)); // Option không bị khóa gd (ví dụ)
        Service.gI().dropItemMap(this.zone, itemMap);
        // }

        // --- LOGIC NHIỆM VỤ RIÊNG ---
        // Nên check null taskdh để tránh NullPointerException
        if (plKill.playerTask != null && plKill.playerTask.taskdh != null) {
            if (plKill.playerTask.taskdh.Hagucboss < 30) {
                plKill.playerTask.taskdh.Hagucboss++;
                plKill.playerTask.taskdh.ResetTime = System.currentTimeMillis();

                // Gửi thông báo tiến độ (Optional)
                // int required = 30;
                // int percentDone = (int) ((double) plKill.playerTask.taskdh.Hagucboss / required * 100);
                // Service.gI().sendThongBao(plKill, "Tiến độ hiện tại: " + percentDone + "%");
            }
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }

        // Giảm dame khi ở level khác 0
        if (this.currentLevel != 0) {
            damage /= 2;
        }

        // Fix logic trừ dame ngẫu nhiên: Đảm bảo không bị âm
        long dameRandom = Util.nextInt(100000);
        long dameAfterRandom = (damage > dameRandom) ? (damage - dameRandom) : 1; // Tối thiểu nhận 1 dame

        damage = this.nPoint.subDameInjureWithDeff(dameAfterRandom);

        // Logic khiên
        if (!piercing && effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }

        this.nPoint.subHP(damage);

        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }

        return (int) damage;
    }

    @Override
    public void joinMap() {
        super.joinMap();
        lastTimeUpdate = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        // Nếu có người chơi trong map, reset thời gian đếm ngược (Boss sẽ không biến mất khi đang đánh)
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            lastTimeUpdate = System.currentTimeMillis();
            return;
        }

        // Nếu không có ai trong 15 phút (900000ms), boss tự rời đi
        if (Util.canDoWithTime(lastTimeUpdate, 900000)) {
            this.leaveMapNew();
        }
    }
}
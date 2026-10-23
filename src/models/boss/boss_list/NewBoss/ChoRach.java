// File: ChoRach.java
package models.boss.boss_list.NewBoss;

import consts.BossStatus;
import models.boss.Boss;
import models.boss.BossesData;
import models.map.ItemMap;
import models.map.Zone;
import models.player.Player;
import server.Client;
import services.Service;
import services.map.ChangeMapService;
import services.map.ItemMapService;
import utils.ItemUtil;
import utils.Util;

public class ChoRach extends Boss {

    // Định nghĩa các trạng thái của Boss để quản lý AI
    private enum WolfState {
        HUNTING, // Trạng thái săn người chơi (mặc định)
        FETCHING_BONE, // Trạng thái chạy đi nhặt xương
        REWARDING, // Trạng thái trả quà và chuẩn bị biến mất
        LEAVING       // Trạng thái đang biến mất
    }

    private WolfState currentState;
    private long stateTimer; // Biến hẹn giờ cho các trạng thái
    private ItemMap targetBone;
    private int rewardPlayerId;
    private long lastTimeJoinMap;
    private static final long TIME_CHANGE_MAP = 300000; // 5 phút đổi map tìm player

    public ChoRach() throws Exception {
        super(-Util.nextInt(1000, 1000000), true, false, BossesData.SOI_HEC_QUYN_NEW);
        // Trạng thái ban đầu là đi săn
        this.currentState = WolfState.HUNTING;
        this.lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
    }

    @Override
    public Zone getMapJoin() {
        Zone target = managers.boss.BossManager.getRoamingZoneWithPlayer(this.zone, ChoRach.class);
        if (target != null) {
            return target;
        }
        return managers.boss.BossManager.getRandomOutdoorZone();
    }

    @Override
    public void joinMap() {
        if (this.zone == null) {
            this.zone = getMapJoin();
        }
        if (this.zone != null) {
            int x = Util.nextInt(100, Math.max(100, this.zone.map.mapWidth - 100));
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
            this.changeStatus(BossStatus.CHAT_S);
            this.lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
            this.lastTimeHadPlayer = System.currentTimeMillis();
        }
    }

    // Kiểm tra xem có cục xương nào trong map không
    private void checkForBone() {
        if (this.zone != null && this.currentState == WolfState.HUNTING) {
            ItemMap bone = this.zone.getItemMapByTempId(460); // ID của cục xương
            if (bone != null) {
                this.targetBone = bone;
                this.rewardPlayerId = (int) bone.playerId;
                this.currentState = WolfState.FETCHING_BONE; // Chuyển sang trạng thái nhặt xương
                this.chat("A, xương kìa! Ngon ngon!");
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) {
            return;
        }

        // Đổi map tìm đến người chơi khi đang ở trạng thái đi săn
        if (this.currentState == WolfState.HUNTING && this.zone != null 
                && (System.currentTimeMillis() > this.lastTimeJoinMap 
                || (this.zone.getNumOfPlayers() == 0 && Util.canDoWithTime(this.lastTimeJoinMap - TIME_CHANGE_MAP, 25000)))) {
            Zone targetZone = managers.boss.BossManager.getRoamingZoneWithPlayer(this.zone, ChoRach.class);
            if (targetZone == null && this.zone.getNumOfPlayers() == 0) {
                targetZone = managers.boss.BossManager.getRandomOutdoorZone();
            }
            if (targetZone != null && targetZone != this.zone) {
                this.lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
                this.chat("|-1|Ở đây không có ai, ta đi tìm con mồi khác đây!");
                ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
                ChangeMapService.gI().exitMap(this);
                this.zone = targetZone;
                this.location.x = Util.nextInt(100, Math.max(100, zone.map.mapWidth - 100));
                this.location.y = zone.map.yPhysicInTop(this.location.x, 100);
                this.joinMap();
            }
        }

        // Luôn kiểm tra xem có xương không để thay đổi hành vi
        checkForBone();

        // Xử lý logic AI dựa trên trạng thái hiện tại
        switch (this.currentState) {
            case FETCHING_BONE:
                handleFetchingBone();
                break;
            case REWARDING:
                handleRewarding();
                break;
            case LEAVING:
                handleLeaving();
                break;
            case HUNTING:
                // Trạng thái săn mồi sẽ được xử lý trong phương thức attack() của lớp cha
                break;
        }
    }

    // AI xử lý khi đang chạy đi nhặt xương
    private void handleFetchingBone() {
        if (targetBone == null || targetBone.zone != this.zone) {
            this.currentState = WolfState.HUNTING; // Nếu xương biến mất, quay lại đi săn
            return;
        }
        // Di chuyển mượt mà tới vị trí của xương
        moveTo(targetBone.x, targetBone.y);

        // Khi đã đến gần xương
        if (Util.getDistance(this.location.x, this.location.y, targetBone.x, targetBone.y) < 30) {
            ItemMapService.gI().removeItemMapAndSendClient(targetBone);
            this.targetBone = null;
            this.chat("Măm măm... ngon quá!");
            this.currentState = WolfState.REWARDING; // Chuyển sang trạng thái trả quà
            this.stateTimer = System.currentTimeMillis(); // Bắt đầu hẹn giờ để trả quà
        }
    }

    // AI xử lý sau khi ăn xương xong
   // AI xử lý sau khi ăn xương xong
private void handleRewarding() {
    // Sau 2 giây, nói chuyện và thả quà
    if (Util.canDoWithTime(stateTimer, 2000)) {
        this.chat("Cảm ơn nhé, tặng ngươi món quà này!");
        
        // --- BẮT ĐẦU THAY ĐỔI ---
        // 1. Chọn ID vật phẩm
        int[] itemne = {441, 442, 443, 444, 445, 446, 447, 459};
        int itemId = Util.isTrue(80, 100) ? itemne[Util.nextInt(0, itemne.length - 1)] : itemne[itemne.length - 1];

        // 2. Tự tạo đối tượng ItemMap với options chuẩn
        ItemMap itemMap = utils.ItemUtil.saoPhaLe(this.zone, itemId, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y), rewardPlayerId);

        // 3. Gọi service để thả vật phẩm ra đất
        Service.gI().dropItemMap(this.zone, itemMap);
        // --- KẾT THÚC THAY ĐỔI ---

        // Cộng nhiệm vụ cho người chơi đã thả xương
        Player pl = Client.gI().getPlayer(rewardPlayerId);
        if (pl != null && pl.playerTask.taskdh.ChoSuong < 20) {
            pl.playerTask.taskdh.ChoSuong++;
            pl.playerTask.taskdh.ResetTime = System.currentTimeMillis();
        }
        this.currentState = WolfState.LEAVING; // Chuyển sang trạng thái chuẩn bị biến mất
        this.stateTimer = System.currentTimeMillis(); // Reset hẹn giờ
    }
}

    // AI xử lý khi biến mất
    private void handleLeaving() {
        if (Util.canDoWithTime(stateTimer, 3000)) { // Sau 3 giây nữa
            this.chat("Ta đi đây! Húuuu!");
            Zone targetZone = managers.boss.BossManager.getRoamingZoneWithPlayer(this.zone, ChoRach.class);
            if (targetZone == null) {
                targetZone = managers.boss.BossManager.getRandomOutdoorZone();
            }
            if (targetZone != null) {
                ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
                ChangeMapService.gI().exitMap(this);
                this.zone = targetZone;
                this.location.x = Util.nextInt(100, Math.max(100, zone.map.mapWidth - 100));
                this.location.y = zone.map.yPhysicInTop(this.location.x, 100);
                this.joinMap();
            }
            this.currentState = WolfState.HUNTING; // Reset trạng thái về đi săn cho lần xuất hiện sau
        }
    }

    @Override
    public void attack() {
        // Chỉ tấn công khi đang ở trạng thái đi săn
        if (this.currentState != WolfState.HUNTING) {
            return;
        }
        super.attack(); // Gọi lại logic tấn công của lớp cha (di chuyển và đánh)
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        // Khi đang bận với cục xương, boss sẽ miễn nhiễm sát thương
        if (this.currentState != WolfState.HUNTING) {
            this.chat("Đừng làm phiền ta!");
            return 0; // Không nhận sát thương
        }
        // Nếu đang đi săn, nhận sát thương bình thường
        if (!this.isDie()) {
            damage = this.nPoint.subDameInjureWithDeff(Util.nextInt(300, 500));
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        }
        return 0;
    }

    @Override
    public void reward(Player plKill) {
        managers.boss.BossManager.dropGoldAndSPL(this, plKill);
    }

    @Override
    protected void notifyJoinMap() {
    }
}

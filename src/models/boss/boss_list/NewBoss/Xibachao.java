package models.boss.boss_list.NewBoss;

import models.boss.Boss;
import models.boss.BossesData;
import models.map.ItemMap;
import models.player.Player;
import server.Client;
import services.Service;
import services.SkillService;
import services.map.ItemMapService;
import utils.ItemUtil;
import utils.Util;

public class Xibachao extends Boss {

    private long lastTeleportTime;
    private static final int TELEPORT_COOLDOWN = 5000;
    private long lastAttackTime;
    private static final int ATTACK_COOLDOWN = 2000;
    private static final int SPECIAL_ITEM_ID = 456;

    private enum TricksterState {
        TRICKSTER, SEEKING_ITEM, CONSUMING_ITEM, LEAVING
    }

    public Xibachao() throws Exception {
        // Tắt thông báo khi xuất hiện
        super(-Util.nextInt(1000, 1000000), true, false, BossesData.XINBATO_NEW);
        this.currentState = TricksterState.TRICKSTER;
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) return;

        // Chỉ kiểm tra item khi đang ở trạng thái mặc định
        if (this.currentState == TricksterState.TRICKSTER) {
            // Sử dụng phương thức kế thừa từ lớp cha
            checkForSpecialItem(SPECIAL_ITEM_ID, TricksterState.SEEKING_ITEM, "Ồ, món gì trông hay thế nhỉ?");
        }

        // Ép kiểu để sử dụng switch case
        switch ((TricksterState) this.currentState) {
            case SEEKING_ITEM:
                handleSeekingItem();
                break;
            case CONSUMING_ITEM:
                handleConsumingItem();
                break;
            case LEAVING:
                handleLeaving();
                break;
            case TRICKSTER:
                // AI mặc định sẽ được xử lý trong attack()
                break;
        }
    }

    @Override
    public void attack() {
        if (this.currentState != TricksterState.TRICKSTER || this.isDie() || this.effectSkill.isHaveEffectSkill()) {
            return;
        }
        if (Util.canDoWithTime(lastAttackTime, ATTACK_COOLDOWN)) {
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) return;

                if (Util.getDistance(this, pl) > 150 && Util.canDoWithTime(lastTeleportTime, TELEPORT_COOLDOWN)) {
                    teleportNearPlayer(pl); // Kế thừa
                } else if (Util.getDistance(this, pl) <= 80) {
                    this.playerSkill.skillSelect = this.playerSkill.skills.get(0);
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    moveAwayFromPlayer(pl, 100); // Kế thừa
                    this.lastAttackTime = System.currentTimeMillis();
                } else {
                    this.moveToPlayer(pl);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (currentState != TricksterState.TRICKSTER) {
                this.chat("Đừng làm phiền ta!");
                return 0;
            }
            if (Util.isTrue(50, 100) && Util.canDoWithTime(lastTeleportTime, TELEPORT_COOLDOWN)) {
                teleportRandomly(); // Kế thừa
                this.chat("Bắt ta đi, hahaha!");
                return 0;
            }
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

    private void handleSeekingItem() {
        if (targetItem == null || targetItem.zone != this.zone) {
            this.currentState = TricksterState.TRICKSTER;
            return;
        }
        moveTo(targetItem.x, targetItem.y);
        if (Util.getDistance(this.location.x, this.location.y, targetItem.x, targetItem.y) < 30) {
            ItemMapService.gI().removeItemMapAndSendClient(targetItem);
            this.targetItem = null;
            this.chat("Xin nhé, hì hì!");
            this.currentState = TricksterState.CONSUMING_ITEM;
            this.stateTimer = System.currentTimeMillis();
        }
    }

    private void handleConsumingItem() {
        if (Util.canDoWithTime(stateTimer, 2000)) {
            this.chat("Của ngươi đây, ta không lấy không đâu!");
            int[] itemne = {441, 442, 443, 444, 445, 446, 447, 459};
            int itemId = Util.isTrue(80, 100) ? itemne[Util.nextInt(0, itemne.length - 1)] : itemne[itemne.length - 1];
            ItemMap itemMap = new ItemMap(this.zone, (short) itemId, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y), rewardPlayerId);
            Service.gI().dropItemMap(this.zone, itemMap);
            Player pl = Client.gI().getPlayer(rewardPlayerId);
            if (pl != null && pl.playerTask.taskdh.ChoNuoc < 5) {
                pl.playerTask.taskdh.ChoNuoc++;
                pl.playerTask.taskdh.ResetTime = System.currentTimeMillis();
            }
            this.currentState = TricksterState.LEAVING;
            this.stateTimer = System.currentTimeMillis();
        }
    }
    
    private void handleLeaving() {
        if (Util.canDoWithTime(stateTimer, 3000)) {
            leaveMapAfter("Xin cảm ơn bạn, tôi đi đây!"); // Kế thừa
            this.currentState = TricksterState.TRICKSTER;
        }
    }

    @Override
    public void reward(Player plKill) {
        int[] itemne = {441, 442, 443, 444, 445, 446, 447, 459};
        Service.gI().dropItemMap(this.zone, ItemUtil.saoPhaLe(zone, Util.isTrue(95, 100) ? itemne[Util.nextInt(0, itemne.length - 1)] : itemne[itemne.length - 1], 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y), (int) plKill.id));
    }

    @Override
    protected void notifyJoinMap() { }
}
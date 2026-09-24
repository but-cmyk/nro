package models.mob.bigboss_list;

import java.util.ArrayList;
import java.util.List;
import models.item.Item;
import models.map.ItemMap;
import models.mob.BigBoss;
import models.mob.Mob;
import network.io.Message;
import models.player.Player;
import services.ItemService;
import services.Service;
import utils.Util;

public class Hirudegarn extends BigBoss {

    // Constants
    private static final long RESPAWN_TIME_FINAL_FORM = 600000; // 10 minutes
    private static final long RESPAWN_TIME_NORMAL = 5000; // 5 seconds
    private static final long ATTACK_COOLDOWN = 3000; // 3 seconds
    private static final int MAX_ERROR_LOGS = 5;

    // Item constants
    private static final int CSKB_ITEM_ID = 380;
    private static final int GEM_ITEM_ID = 77;
    private static final int GOLD_ITEM_ID = 190;

    private static final int GOLD_QUANTITY = 31000;
    private static final int GEM_QUANTITY_PER_BAG = 1;

    private static final int RARE_ITEM_ID = 568;
    private static final int RARE_ITEM_OPTION_ID = 93;
    private static final int RARE_ITEM_OPTION_PARAM = 2;

    // Drop Counts (Số lượng túi rơi)
    private static final int GOLD_DROP_LIMIT = 20;
    private static final int GEM_DROP_LIMIT = 10;
    private static final int CSKB_DROP_LIMIT = 3;

    // Probability constants
    private static final int RARE_ITEM_CHANCE = 100; // 1/15
    private static final int LEGENDARY_ITEM_CHANCE = 200; // 1/5000

    // Boss form constants
    private static final int FORM_INITIAL = 0;
    private static final int FORM_SECOND = 1;
    private static final int FORM_THIRD = 2;
    private static final int FORM_FINAL = 3;

    // Action constants
    private static final int ACTION_SHOOT = 0;
    private static final int ACTION_TAIL_WHIP = 1;
    private static final int ACTION_STOMP = 2;
    private static final int ACTION_FLY = 3;
    private static final int ACTION_ATTACK = 4;
    private static final int ACTION_TRANSFORM = 5;
    private static final int ACTION_LEVEL_UP_TRANSFORM = 6;
    private static final int ACTION_CHARGE = 7;
    private static final int ACTION_MOVE = 8;
    private static final int ACTION_DIE = 9;

    // [CẬP NHẬT] Mốc rơi đồ khi HP còn 20 triệu
    private static final long HP_DROP_THRESHOLD = 20_000_000; // 20 triệu HP

    private int errors;

    // [CẬP NHẬT] Flag để kiểm tra đã rơi đồ ở mốc 20 triệu chưa
    private boolean hasDroppedAt20M = false;

    public Hirudegarn(Mob mob) {
        super(mob);
        this.errors = 0;
    }

    @Override
    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        // Boss chỉ nhận tối đa 1% MÁU TỐI ĐA mỗi đòn đánh (sửa lỗi tiệm cận Zeno 1% máu hiện tại)
        long maxDmgPerHit = Math.max(1, this.point.getHpFull() / 100);
        if (damage > maxDmgPerHit) {
            damage = maxDmgPerHit;
        }
        if (damage <= 0) {
            damage = 1;
        }
        super.injured(plAtt, damage, false);

        // [CẬP NHẬT LOGIC] Kiểm tra mốc 20 triệu HP chỉ ở FORM_FINAL để không rơi lặp lại qua các form
        if (!this.isDie() && !hasDroppedAt20M && lvMob == FORM_FINAL) {
            if (this.point.hp <= HP_DROP_THRESHOLD) {
                dropMinorItems(); // Rơi một lượng vàng nhỏ ở mốc 20 triệu Form cuối
                hasDroppedAt20M = true;
            }
        }
    }

    @Override
    public void update() {
        super.update();

        if (isDie() && (System.currentTimeMillis() - lastTimeDie) > RESPAWN_TIME_FINAL_FORM && lvMob == FORM_FINAL) {
            respawnFinalForm();
        } else if (isDie() && (System.currentTimeMillis() - lastTimeDie) > RESPAWN_TIME_NORMAL && lvMob <= FORM_THIRD) {
            handleFormTransformation();
        }
    }

    private void respawnFinalForm() {
        lvMob = FORM_INITIAL;
        action = ACTION_SHOOT;
        this.location.x = Util.nextInt(100, 900);
        if (this.zone != null && this.zone.map != null) {
            this.location.y = (short) this.zone.map.yPhysicInTop(this.location.x, 360);
        } else {
            this.location.y = 360;
        }
        this.point.hp = this.point.getHpFull();

        // [RESET] Đặt lại flag khi boss hồi sinh
        this.hasDroppedAt20M = false;

        Service.gI().sendBigBoss2(this.zone, action, this);
        sendHpUpdateMessage();
    }

    private void handleFormTransformation() {
        switch (lvMob) {
            case FORM_INITIAL -> {
                lvMob = FORM_SECOND;
                action = ACTION_LEVEL_UP_TRANSFORM;
                this.point.hp = this.point.getHpFull();
            }
            case FORM_SECOND -> {
                lvMob = FORM_THIRD;
                action = ACTION_TRANSFORM;
                this.point.hp = this.point.getHpFull();
            }
            case FORM_THIRD -> {
                lvMob = FORM_FINAL;
                action = ACTION_DIE;
            }
            default -> {
            }
        }

        // [CÂN BẰNG KINH TẾ]
        // Dạng 1, 2, 3 biến hình: chỉ rơi một lượng vàng nhỏ tạo không khí
        // Dạng cuối cùng bị tiêu diệt hoàn toàn: mới rơi quà lớn (Vàng, Ngọc, CSKB, Đồ Thần Linh, Đồ Hiếm)
        if (lvMob <= FORM_THIRD) {
            dropMinorItems();
        } else {
            dropFinalItems();
        }

        // [RESET] Đặt lại flag cho form mới
        this.hasDroppedAt20M = false;

        Service.gI().sendBigBoss2(this.zone, action, this);

        if (lvMob <= FORM_THIRD) {
            sendHpUpdateMessage();
        } else {
            this.location.x = -1000;
            this.location.y = -1000;
        }
    }

    private void dropMinorItems() {
        dropGoldItems(5);
    }

    private void dropFinalItems() {
        // Drop gold items
        dropGoldItems(GOLD_DROP_LIMIT);

        // Drop Gem and CSKB
        dropGemItems();
        dropCSKBItems();

        // Drop rare item
        if (Util.isTrue(1, RARE_ITEM_CHANCE)) {
            dropRareItem();
        }

        // Drop legendary item
        if (Util.isTrue(1, LEGENDARY_ITEM_CHANCE)) {
            dropLegendaryItem();
        }
    }

    private void dropGoldItems(int limit) {
        int leftCounter = 0;
        int rightCounter = 1;
        int direction = 0;

        for (int i = 0; i < limit; i++) {
            int offsetX = direction == 0 ? -5 * leftCounter : 5 * rightCounter;
            if (direction == 0) leftCounter++; else rightCounter++;
            direction = direction == 0 ? 1 : 0;
            if (leftCounter > 10) leftCounter = 0;
            if (rightCounter > 10) rightCounter = 1;

            Service.gI().dropItemMap(
                    this.zone,
                    new ItemMap(zone, GOLD_ITEM_ID, GOLD_QUANTITY, this.location.x + offsetX, this.location.y, -1)
            );
        }
    }

    private void dropGemItems() {
        int leftCounter = 0;
        int rightCounter = 1;
        int direction = 0;

        for (int i = 0; i < GEM_DROP_LIMIT; i++) {
            int offsetX = direction == 0 ? -15 * leftCounter : 15 * rightCounter;
            if (direction == 0) leftCounter++; else rightCounter++;
            direction = direction == 0 ? 1 : 0;

            Service.gI().dropItemMap(
                    this.zone,
                    new ItemMap(zone, GEM_ITEM_ID, GEM_QUANTITY_PER_BAG, this.location.x + offsetX, this.location.y, -1)
            );
        }
    }

    private void dropCSKBItems() {
        int leftCounter = 0;
        int rightCounter = 1;
        int direction = 1;

        for (int i = 0; i < CSKB_DROP_LIMIT; i++) {
            int offsetX = direction == 0 ? -15 * leftCounter : 15 * rightCounter;
            if (direction == 0) leftCounter++; else rightCounter++;
            direction = direction == 0 ? 1 : 0;

            Service.gI().dropItemMap(
                    this.zone,
                    new ItemMap(zone, CSKB_ITEM_ID, 1, this.location.x + offsetX, this.location.y, -1)
            );
        }
    }

    private void dropRareItem() {
        int dropY = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);
        ItemMap rareItem = new ItemMap(this.zone, RARE_ITEM_ID, 1, this.location.x, dropY, -1);
        rareItem.options.add(new Item.ItemOption(RARE_ITEM_OPTION_ID, RARE_ITEM_OPTION_PARAM));
        Service.gI().dropItemMap(this.zone, rareItem);
    }

    private void dropLegendaryItem() {
        int dropY = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);
        ItemMap legendaryItem = ItemService.gI().randDoTL(this.zone, 1, this.location.x, dropY, -1);
        Service.gI().dropItemMap(this.zone, legendaryItem);
    }

    private void sendHpUpdateMessage() {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            logError(e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    @Override
    public void attack() {
        if (isDie() || effectSkill.isHaveEffectSkill() || !Util.canDoWithTime(lastBigBossAttackTime, ATTACK_COOLDOWN)) {
            return;
        }
        if (this.zone.getPlayers().isEmpty()) return;

        Player targetPlayer = getRandomPlayer();
        if (targetPlayer == null || targetPlayer.isDie()) return;

        performAttack(targetPlayer);
        lastBigBossAttackTime = System.currentTimeMillis();
    }

    private Player getRandomPlayer() {
        try {
            if (this.zone == null) return null;
            List<Player> players = this.zone.getPlayers();
            if (players == null || players.isEmpty()) return null;
            List<Player> validPlayers = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                Player pl = players.get(i);
                if (pl != null && !pl.isDie() && !pl.isBoss && !pl.isNewPet) {
                    validPlayers.add(pl);
                }
            }
            if (validPlayers.isEmpty()) return null;
            int index = Util.nextInt(0, validPlayers.size() - 1);
            return validPlayers.get(index);
        } catch (Exception e) {
            logError(e);
            return null;
        }
    }

    private void performAttack(Player targetPlayer) {
        Message msg = null;
        try {
            int[] availableActions = getAvailableActions();
            action = action == ACTION_CHARGE ? ACTION_SHOOT : availableActions[Util.nextInt(0, availableActions.length - 1)];

            if (action == ACTION_TAIL_WHIP) {
                this.location.x = (short) targetPlayer.location.x;
                Service.gI().sendBigBoss2(this.zone, ACTION_MOVE, this);
            }

            msg = new Message(101);
            msg.writer().writeByte(action);

            switch (action) {
                case ACTION_TAIL_WHIP -> handleTailWhipAttack(msg, targetPlayer);
                case ACTION_FLY -> handleFlyAttack(msg, targetPlayer);
                case ACTION_STOMP, ACTION_SHOOT, ACTION_CHARGE -> handleAOEAttack(msg);
                default -> {}
            }

            Service.gI().sendMessAllPlayerInMap(this.zone, msg);

        } catch (Exception e) {
            logError(e);
        } finally {
            if (msg != null) msg.cleanup();
        }
    }

    private int[] getAvailableActions() {
        if (this.lvMob >= FORM_THIRD) {
            return new int[]{ACTION_TAIL_WHIP, ACTION_STOMP};
        } else {
            return new int[]{ACTION_TAIL_WHIP, ACTION_STOMP, ACTION_FLY, ACTION_CHARGE};
        }
    }

    private void handleTailWhipAttack(Message msg, Player targetPlayer) throws Exception {
        msg.writer().writeByte(1);
        int damage = targetPlayer.injured(null, this.point.getDameAttack(), false, true);
        msg.writer().writeInt((int) targetPlayer.id);
        msg.writer().writeInt(damage);
    }

    private void handleFlyAttack(Message msg, Player targetPlayer) throws Exception {
        this.location.x = (short) targetPlayer.location.x;
        msg.writer().writeShort(this.location.x);
        msg.writer().writeShort(this.location.y);
    }

    private void handleAOEAttack(Message msg) throws Exception {
        if (this.zone == null) return;
        List<Player> players = this.zone.getPlayers();
        List<Player> validTargets = new ArrayList<>();
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                if (player != null && !player.isDie() && !player.isBoss && !player.isNewPet) {
                    validTargets.add(player);
                }
            }
        }
        msg.writer().writeByte(validTargets.size());

        for (Player player : validTargets) {
            int damage = player.injured(null, this.point.getDameAttack(), false, true);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt(damage);
        }
    }

    private void logError(Exception e) {
        if (errors < MAX_ERROR_LOGS) {
            errors++;
            e.printStackTrace();
        }
    }
}
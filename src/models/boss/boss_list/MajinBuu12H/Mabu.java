package models.boss.boss_list.MajinBuu12H;

import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import consts.AppearType;
import static consts.BossType.FINAL;
import consts.ConstPlayer;
import java.util.Arrays;
import java.util.List;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import models.skill.Skill;
import services.Service;
import utils.Util;
import services.phoban.MajinBuuService;
import services.EffectSkillService;
import services.ItemTimeService;
import services.SkillService;
import services.TaskService;
import services.map.ChangeMapService;
import utils.SkillUtil;

public class Mabu extends Boss {

    private long lastTimePetrify;
    private int percent;

    // Cache danh sách ID đồ thần linh để không phải tạo lại mỗi lần boss chết
    private static final List<Integer> AO_TL = Arrays.asList(555, 557, 559); // Ví dụ ID áo
    private static final List<Integer> QUAN_TL = Arrays.asList(556, 558, 560);
    private static final List<Integer> GANG_TL = Arrays.asList(562, 564, 566);
    private static final List<Integer> GIAY_TL = Arrays.asList(563, 565, 567);
    // Lưu ý: Hãy thay ID đúng vào mảng itemDos bên dưới, code cũ của bạn dùng ID đồ hủy diệt/thần linh cũ
    private static final int[] ITEM_DOS = new int[]{233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277, 281};

    public Mabu() throws Exception {
        super(FINAL, BossID.MABU_12H, BossesData.MABU_12H);
    }

    @Override
    public void reward(Player plKill) {
        if (plKill.isPl()) {
            plKill.goHome = true;
            plKill.timeGohome = 30;
        }

        // Rơi vật phẩm 521 (Vé/Huy hiệu)
        int quantity = Util.nextInt(2, 3);
        for (int i = 0; i < quantity; i++) {
            ItemMap itemMap = new ItemMap(zone, 521, 1,
                    this.location.x + (Util.nextInt(-50, 50) * i),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);

            int param = (plKill.fightMabu != null ? plKill.fightMabu.pointPercent : 0) + 30;
            itemMap.options.add(new Item.ItemOption(1, param));
            Service.gI().dropItemMap(this.zone, itemMap);
        }

        // Logic rơi đồ Thần (Tỉ lệ 5%)
        if (Util.isTrue(5, 100)) {
            int randomIdx = Util.nextInt(ITEM_DOS.length);
            int tempId = ITEM_DOS[randomIdx];

            ItemMap item = new ItemMap(this.zone, tempId, 1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);

            // Logic thêm chỉ số (Option)
            // Lưu ý: Cần check đúng ID item trong source của bạn để map với option
            // Code dưới đây giữ nguyên logic check của bạn nhưng viết gọn hơn
            if (tempId >= 233 && tempId <= 241) { // Áo
                item.options.add(new Item.ItemOption(47, Util.nextInt(350, 470)));
            } else if (tempId >= 245 && tempId <= 253) { // Quần
                item.options.add(new Item.ItemOption(22, Util.nextInt(20000, 24000)));
            } else if (tempId >= 257 && tempId <= 265) { // Găng
                item.options.add(new Item.ItemOption(0, Util.nextInt(2200, 2250)));
            } else if (tempId >= 269 && tempId <= 277) { // Giày
                item.options.add(new Item.ItemOption(23, Util.nextInt(20000, 23000)));
            } else if (tempId == 281) { // Rada
                item.options.add(new Item.ItemOption(14, Util.nextInt(10, 12)));
            }

            item.options.add(new Item.ItemOption(209, 1)); // Option đồ rơi từ boss

            // Tỷ lệ sao pha lê
            int saoOption = 107; // ID option sao
            int paramSao = 2; // Mặc định 2 sao

            if (Util.isTrue(2, 100)) paramSao = 6;
            else if (Util.isTrue(5, 100)) paramSao = 5;
            else if (Util.isTrue(20, 100)) paramSao = 4;
            else if (Util.isTrue(50, 100)) paramSao = 3;

            item.options.add(new Item.ItemOption(saoOption, paramSao));

            Service.gI().dropItemMap(this.zone, item);
        }

        if(plKill.fightMabu != null) plKill.fightMabu.changePoint((byte) 25);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            this.zone = zoneFinal;
        }
        ChangeMapService.gI().changeMap(this, this.zone, Util.nextInt(300, 400), 336);
        this.changeStatus(BossStatus.CHAT_S);
        if(MajinBuuService.gI().getNpcBabiday(this.zone) != null) {
            MajinBuuService.gI().getNpcBabiday(this.zone).npcChat(this.zone, "Mabư ! Hãy theo lệnh ta, giết hết bọn chúng đi");
        }
    }

    private void petrifyPlayersInTheMap() {
        if(this.zone == null) return;
        for (Player pl : this.zone.getNotBosses()) {
            if(pl == null || pl.isDie()) continue;
            if (Util.isTrue(1, 10)) {
                EffectSkillService.gI().setIsStone(pl, 22000);
            } else if (Util.isTrue(1, 5)) {
                this.chat("Úm ba la xì bùa");
                EffectSkillService.gI().setSocola(pl, System.currentTimeMillis(), 30000);
                Service.gI().Send_Caitrang(pl);
                ItemTimeService.gI().sendItemTime(pl, 4133, 30);
            }
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            if (Util.canDoWithTime(lastTimePetrify, 30000)) {
                petrifyPlayersInTheMap();
                this.lastTimePetrify = System.currentTimeMillis();
            }
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

                // Logic di chuyển tấn công
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        int rangeX = SkillUtil.isUseSkillChuong(this) ? Util.nextInt(20, 200) : Util.nextInt(10, 40);
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * rangeX), pl.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }
        if (this.data[nextLevel].getTypeAppear() == AppearType.DEFAULT_APPEAR
                && Util.canDoWithTime(lastTimeRest, secondsRest * 1000)) {
            this.changeStatus(BossStatus.RESPAWN);
        }

        // Fix lỗi chia cho 0 nếu secondsRest nhỏ
        long totalTime = (secondsRest - 3) * 1000;
        if(totalTime > 0) {
            long currentTimeMillis = System.currentTimeMillis();
            long elapsedTime = currentTimeMillis - lastTimeRest;
            this.percent = (int) (elapsedTime * 100 / totalTime);
            if (percent <= 100) {
                Service.gI().SendMabu(this.zoneFinal, this.percent);
            }
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) return 0;

        if (!piercing && Util.isTrue(20, 100)) {
            this.chat("Xí hụt");
            return 0;
        }

        if (plAtt != null && plAtt.isPl() && plAtt.fightMabu != null && Util.isTrue(1, 5)) {
            plAtt.fightMabu.changePercentPoint((byte) 1);
        }

        damage = this.nPoint.subDameInjureWithDeff(damage);

        if (!piercing && effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }

        int skillID = plAtt.playerSkill.skillSelect.template.id;

        // Danh sách các skill được phép phá giới hạn damage
        boolean isSpecialSkill = (skillID == Skill.TU_SAT ||
                skillID == Skill.MAKANKOSAPPO ||
                skillID == Skill.QUA_CAU_KENH_KHI);

        // 1. Giới hạn trần 199,999 damage
        // Chỉ áp dụng giới hạn này nếu KHÔNG PHẢI là skill đặc biệt
        if (!isSpecialSkill && damage >= 199999) {
            damage = 199999;
        }

        this.nPoint.subHP(damage);

        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);

        // Hồi sinh đệ tử khi Mabu chết/biến mất
        if(this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null){
            for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                if(boss != null) boss.changeStatus(BossStatus.RESPAWN);
            }
        }
    }
}
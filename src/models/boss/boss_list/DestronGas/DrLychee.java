package models.boss.boss_list.DestronGas;

import consts.BossID;
import consts.BossStatus;
import consts.ConstPlayer;
import managers.boss.GasDestroyManager;
import models.boss.*;
import static consts.BossType.PHOBANKGHD;
import models.clan.Clan;
import models.item.Item;
import models.map.ItemMap;
import models.map.Zone;
import models.player.Player;
import services.EffectSkillService;
import models.skill.Skill;
import services.Service;
import services.map.ChangeMapService;
import utils.Util;

public class DrLychee extends Boss {

    private final int level;
    private Clan clan;

    private static final int[][] FULL_DEMON = new int[][]{
            {Skill.DEMON, 1}, {Skill.DEMON, 2}, {Skill.DEMON, 3},
            {Skill.DEMON, 4}, {Skill.DEMON, 5}, {Skill.DEMON, 6}, {Skill.DEMON, 7}
    };

    public DrLychee(Zone zone, Clan clan, int level, int dame, int hp) throws Exception {
        super(PHOBANKGHD, BossID.DR_LYCHEE, new BossData(
                "Dr Lychee",
                ConstPlayer.TRAI_DAT,
                new short[]{742, 743, 744, -1, -1, -1},
                dame, // Đã bỏ cộng cứng 10000 để linh hoạt hơn
                new int[]{hp}, // Đã bỏ cộng cứng 1tr hp
                new int[]{148},
                (int[][]) Util.addArray(FULL_DEMON),
                new String[]{"|-1|Ta đợi các ngươi mãi", "|-1|Bọn xayda các ngươi mau đền tội đi"},
                new String[]{"|-1|Đại bác báo thù...", "|-1|Heyyyyyyyy Yaaaaa"},
                new String[]{"|-1|Các ngươi khá lắm", "|-1|Hatchiyack sẽ báo thù cho ta"},
                60
        ));
        this.zone = zone;
        this.level = level;
        this.clan = clan;
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }

        // Kháng đòn đánh từ đệ Mabu (nếu có logic này)
        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
        }

        // Tính toán giảm sát thương an toàn hơn
        long dameGiam = Util.nextInt(100 * this.level);
        if (damage > dameGiam) {
            damage -= dameGiam;
        } else {
            damage = 1; // Tối thiểu nhận 1 damage
        }

        // Giảm % damage theo level
        long percentReduce = this.level / 10;
        if (percentReduce > 90) percentReduce = 90; // Không giảm quá 90%
        damage -= (damage * percentReduce / 100);

        damage = this.nPoint.subDameInjureWithDeff(damage);

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
    public void reward(Player plKill) {
        // Thay đổi logic rơi đồ: Rơi tối đa 5 món xung quanh boss để tránh lag
        // Item 1: Tại chỗ boss
        dropCt(0);
        // Item 2-5: Rơi ngẫu nhiên xung quanh
        for(int i = 0; i < 4; i++){
            dropCt(Util.nextInt(-50, 50));
        }
    }

    private void dropCt(int xOffset) {
        int x = this.location.x + xOffset;
        // Check biên map để item không rơi ra ngoài
        if (x < 50) x = 50;
        if (x > this.zone.map.mapWidth - 50) x = this.zone.map.mapWidth - 50;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        ItemMap it = new ItemMap(zone, 738, 1, x, y, -1);
        it.options.clear();

        // Logic chỉ số giữ nguyên như của bạn
        int ParamMax = (int) 2 + (level / 3) - (level > 55 ? Util.nextInt(level / 10) : 0);
        if (ParamMax < 3) ParamMax = 3;
        int ParamMin = ParamMax - 3;
        if (ParamMin < 3) ParamMin = 3;
        int hsd = Util.nextInt(ParamMin, ParamMax);

        it.options.add(new Item.ItemOption(50, Util.nextInt(ParamMin, ParamMax)));
        it.options.add(new Item.ItemOption(77, Util.nextInt(ParamMin, ParamMax)));
        it.options.add(new Item.ItemOption(103, Util.nextInt(ParamMin, ParamMax)));
        it.options.add(new Item.ItemOption(94, Util.nextInt(ParamMin, ParamMax)));
        it.options.add(new Item.ItemOption(93, hsd > 7 ? 7 : hsd));
        it.options.add(new Item.ItemOption(30, 0));

        Service.gI().dropItemMap(this.zone, it);
    }


    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 480, 295);
        this.moveTo(480, 480);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        // Tính chỉ số cho Boss sau (Hatchiyack)
        // Dùng long để tránh tràn số integer nếu dame/hp quá lớn
        long bossDamage = this.nPoint.dame * 150L / 100L;
        long bossMaxHealth = this.nPoint.hpMax * 150L / 100L;

        // Cap giới hạn
        if (bossDamage > 200_000_000L) bossDamage = 200_000_000L;
        if (bossMaxHealth > 2_000_000_000L) bossMaxHealth = 2_000_000_000L;

        try {
            clan.KhiGasHuyDiet.bosses.add(new Hatchiyack(
                    zone,
                    clan,
                    level,
                    (int) bossDamage,
                    (int) bossMaxHealth
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        GasDestroyManager.gI().removeBoss(this);
        this.dispose();
    }
}
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

public class Hatchiyack extends Boss {

    private final int level;
    private Clan clan;

    private static final int[][] FULL_DEMON = new int[][]{
            {Skill.DEMON, 1}, {Skill.DEMON, 2}, {Skill.DEMON, 3},
            {Skill.DEMON, 4}, {Skill.DEMON, 5}, {Skill.DEMON, 6}, {Skill.DEMON, 7}
    };

    public Hatchiyack(Zone zone, Clan clan, int level, int dame, int hp) throws Exception {
        super(PHOBANKGHD, BossID.HATCHIYACK, new BossData(
                "Hatchiyack",
                ConstPlayer.TRAI_DAT,
                new short[]{639, 640, 641, -1, -1, -1},
                dame,
                new int[]{hp},
                new int[]{148},
                (int[][]) Util.addArray(FULL_DEMON),
                new String[]{"|-1|Các ngươi dám hạ sư phụ ta", "|-1|Ta sẽ tiêu diệt hết các ngươi"},
                new String[]{"|-1|Đại bác báo thù...", "|-1|Heyyyyyyyy Yaaaaa"},
                new String[]{"|-1|Các ngươi khó mà rời khỏi nơi đây"},
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

        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
        }

        // Fix lỗi âm máu
        long dameGiam = Util.nextInt(200 * this.level);
        if (damage > dameGiam) {
            damage -= dameGiam;
        } else {
            damage = 1;
        }

        long percentReduce = this.level / 5;
        if (percentReduce > 90) percentReduce = 90;
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
        // Tối ưu drop: Rơi 1 set (5 món) quanh boss thay vì nhân bản theo số người chơi
        dropCt(0);
        dropCt(30);
        dropCt(-30);
        dropCt(60);
        dropCt(-60);
    }

    private void dropCt(int xOffset) {
        int x = this.location.x + xOffset;
        if (x < 50) x = 50;
        if (x > this.zone.map.mapWidth - 50) x = this.zone.map.mapWidth - 50;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // ID 729
        ItemMap it = new ItemMap(zone, 729, 1, x, y, -1);
        it.options.clear();
        int ParamMax = (int) 2 + (level / 3) - (level > 55 ? Util.nextInt(level / 10) : 0);
        if (ParamMax < 3) ParamMax = 3;
        int ParamMin = ParamMax - 3;
        if (ParamMin < 3) ParamMin = 3;
        int hsd = Util.nextInt(ParamMin, ParamMax);

        it.options.add(new Item.ItemOption(50, Util.nextInt(ParamMin, ParamMax)));
        it.options.add(new Item.ItemOption(77, Util.nextInt(ParamMin, ParamMax)));
        it.options.add(new Item.ItemOption(103, Util.nextInt(ParamMin, ParamMax)));
        it.options.add(new Item.ItemOption(5, Util.nextInt(ParamMin, ParamMax)));
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
        if (clan != null && clan.KhiGasHuyDiet != null) {
            clan.KhiGasHuyDiet.hatchiyatchDead = true;
        }
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        GasDestroyManager.gI().removeBoss(this);
        this.dispose();
    }
}
package models.boss.boss_list.TreasureUnderSea;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import consts.BossStatus;
import consts.ConstPlayer;
import managers.boss.TreasureUnderSeaManager;
import models.boss.*;
import static consts.BossType.PHOBANBDKB;
import models.map.ItemMap;
import models.map.Zone;
import models.player.Player;
import models.skill.Skill;
import services.Service;
import services.map.ChangeMapService;
import utils.Util;

public class TrungUyXanhLo extends Boss {

    private static final int[][] FULL_DEMON = new int[][]{{Skill.DEMON, 1}, {Skill.DEMON, 2}, {Skill.DEMON, 3}, {Skill.DEMON, 4}, {Skill.DEMON, 5}, {Skill.DEMON, 6}, {Skill.DEMON, 7}};

    public TrungUyXanhLo(Zone zone, int level, int dame, int hp) throws Exception {
        super(PHOBANBDKB, BossID.TRUNG_UY_XANH_LO, new BossData(
                "Trung úy Xanh Lơ",
                ConstPlayer.TRAI_DAT,
                new short[]{135, 136, 137, -1, -1, -1},
                (dame),
                new int[]{hp},
                new int[]{103},
                (int[][]) Util.addArray(FULL_DEMON),
                new String[]{},
                new String[]{"|-1|Các ngươi tới số rồi mới gặp phải ta",
                    "|-1|He he he",
                    "|-1|Xem các ngươi mạnh đến đâu"},
                new String[]{},
                60
        ));
        this.zone = zone;
    }

    @Override
    public void reward(Player plKill) {
        int bdkbLevel = (plKill != null && plKill.clan != null && plKill.clan.BanDoKhoBau != null) ? plKill.clan.BanDoKhoBau.level : 50;
        int goldReward = 2_000_000 + (bdkbLevel * 250_000);
        int dropY = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);

        // 1. Cọc vàng lớn
        ItemMap itGold = new ItemMap(this.zone, 190, goldReward, this.location.x, dropY, plKill != null ? plKill.id : -1);
        Service.gI().dropItemMap(this.zone, itGold);

        // 2. Ngọc Rồng ngẫu nhiên (3 - 6 sao)
        int nrId = Util.nextInt(16, 19);
        ItemMap itNr = new ItemMap(this.zone, nrId, 1, this.location.x + 25, dropY, plKill != null ? plKill.id : -1);
        Service.gI().dropItemMap(this.zone, itNr);

        // 3. Sao Pha Lê ngẫu nhiên (441 - 447)
        if (Util.isTrue(70, 100)) {
            int splId = Util.nextInt(441, 447);
            ItemMap itSpl = new ItemMap(this.zone, splId, 1, this.location.x - 25, dropY, plKill != null ? plKill.id : -1);
            Service.gI().dropItemMap(this.zone, itSpl);
        }
    }

    @Override
    public void joinMap() {
        ChangeMapService.gI().changeMap(this, this.zone, 198, 456);
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
        Service.gI().setPos(this, 198, 456);
    }

    @Override
    public void afk() {
        Player pl = getPlayerAttack();
        if (pl == null || pl.isDie()) {
            return;
        }
        if (Util.getDistance(this, pl) <= 200) {
            this.changeStatus(BossStatus.ACTIVE);
        }
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
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        TreasureUnderSeaManager.gI().removeBoss(this);
        this.dispose();
    }
}

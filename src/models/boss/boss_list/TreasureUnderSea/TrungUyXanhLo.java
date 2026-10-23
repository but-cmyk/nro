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

    public TrungUyXanhLo(Zone zone, int level, long dame, long hp) throws Exception {
        super(PHOBANBDKB, BossID.TRUNG_UY_XANH_LO, new BossData(
                "Trung úy Xanh Lơ",
                ConstPlayer.TRAI_DAT,
                new short[]{135, 136, 137, -1, -1, -1},
                (dame),
                new long[]{hp},
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
        int dropY = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);
        long plId = (plKill != null) ? plKill.id : -1;

        // 1. Bão vàng (8 - 12 cọc vàng, mỗi cọc 1.500.000 - 3.000.000 vàng, tổng ~ 15m - 30m vàng)
        int numGoldPiles = Util.nextInt(8, 12);
        for (int i = 0; i < numGoldPiles; i++) {
            int goldAmount = Util.nextInt(1_500_000, 3_000_000);
            int itemId = (i % 2 == 0) ? 190 : 76;
            int offsetX = (i % 2 == 0) ? (i * 18) : (-i * 18);
            ItemMap itGold = new ItemMap(this.zone, itemId, goldAmount, this.location.x + offsetX, dropY, plId);
            Service.gI().dropItemMap(this.zone, itGold);
        }

        // 2. Rơi cố định 1 - 3 Thỏi vàng (Item 457)
        int numGoldBars = Util.nextInt(1, 3);
        for (int i = 0; i < numGoldBars; i++) {
            int offsetX = (i == 0) ? 0 : (i % 2 == 0 ? (i + 1) * 22 : -(i + 1) * 22);
            ItemMap itGoldBar = new ItemMap(this.zone, 457, 1, this.location.x + offsetX, dropY, plId);
            Service.gI().dropItemMap(this.zone, itGoldBar);
        }

        // 3. Ngọc Rồng ngẫu nhiên (4 - 7 sao: id 17 - 20)
        int nrId = Util.nextInt(17, 20);
        ItemMap itNr = new ItemMap(this.zone, nrId, 1, this.location.x + 35, dropY, plId);
        Service.gI().dropItemMap(this.zone, itNr);

        // 4. Sao Pha Lê ngẫu nhiên (441 - 447)
        int splId = Util.nextInt(441, 447);
        ItemMap itSpl = utils.ItemUtil.saoPhaLe(this.zone, splId, 1, this.location.x - 35, dropY, plId);
        Service.gI().dropItemMap(this.zone, itSpl);

        // 5. Rơi 1 - 2 Thẻ Trung Úy Xanh Lơ (Item 842)
        ItemMap itCard = new ItemMap(this.zone, 842, Util.nextInt(1, 2), this.location.x + 15, dropY, plId);
        itCard.options.add(new models.item.Item.ItemOption(87, 0));
        itCard.options.add(new models.item.Item.ItemOption(30, 0));
        Service.gI().dropItemMap(this.zone, itCard);

        // 6. Rơi nguyên liệu Sách Tuyệt Kĩ
        // Rơi 1 - 2 Bìa Sách (Item 1154)
        ItemMap itBia = new ItemMap(this.zone, 1154, Util.nextInt(1, 2), this.location.x - 50, dropY, plId);
        itBia.options.add(new models.item.Item.ItemOption(30, 0));
        Service.gI().dropItemMap(this.zone, itBia);

        // Rơi 1 - 2 Bùa Giám Định (Item 1152)
        ItemMap itBua = new ItemMap(this.zone, 1152, Util.nextInt(1, 2), this.location.x + 50, dropY, plId);
        itBua.options.add(new models.item.Item.ItemOption(30, 0));
        Service.gI().dropItemMap(this.zone, itBua);

        // 25% rơi 1 Kìm Bấm Giấy (Item 1153)
        if (Util.isTrue(25, 100)) {
            ItemMap itKim = new ItemMap(this.zone, 1153, 1, this.location.x, dropY, plId);
            itKim.options.add(new models.item.Item.ItemOption(30, 0));
            Service.gI().dropItemMap(this.zone, itKim);
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

package models.boss.boss_list.DeathOrAliveArena;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.PHOBAN;
import models.player.Player;
import utils.Util;

public class VuaQuySaTang extends DeathOrAliveArena {

    private long lastTimeBay;

    public VuaQuySaTang(Player player) throws Exception {
        super(PHOBAN, BossID.VUA_QUY_SA_TANG, BossesData.VUA_QUY_SA_TANG);
        this.playerAtt = player;
    }

    @Override
    public void bayLungTung() {
        if (Util.canDoWithTime(lastTimeBay, 1000)) {
            goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)), Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 200), false);
            lastTimeBay = System.currentTimeMillis();
        }
    }
}

package models.boss.boss_list.The23rdMartialArtCongress;

/*
 * @Author: DienCoLamCoi
 * @Description: Điện Cơ Lâm Còi - Chuyên cung cấp thiết bị điện cơ uy tín chất lượng cao.
 * @Group Zalo: Giao lưu chia sẻ kinh nghiệm code - https://zalo.me/g/lsqfzx907
 */


import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.PHOBAN;
import models.player.Player;

public class ODo extends The23rdMartialArtCongress {

    public ODo(Player player) throws Exception {
        super(PHOBAN, BossID.O_DO, BossesData.O_DO);
        this.playerAtt = player;
    }
}

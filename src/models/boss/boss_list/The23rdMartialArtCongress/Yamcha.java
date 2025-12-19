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

public class Yamcha extends The23rdMartialArtCongress {

    public Yamcha(Player player) throws Exception {
        super(PHOBAN, BossID.YAMCHA, BossesData.YAMCHA);
        this.playerAtt = player;
    }
}

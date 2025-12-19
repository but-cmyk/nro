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

public class BongBang extends DeathOrAliveArena {

    public BongBang(Player player) throws Exception {
        super(PHOBAN, BossID.BONG_BANG, BossesData.BONG_BANG);
        this.playerAtt = player;
    }
}

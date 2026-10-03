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

public class ThoDauBac extends DeathOrAliveArena {

    private long lastTimeChat = System.currentTimeMillis();

    public ThoDauBac(Player player) throws Exception {
        super(PHOBAN, BossID.THO_DAU_BAC, BossesData.THO_DAU_BAC);
        this.playerAtt = player;
    }

    @Override
    public void attack() {
        super.attack();
        if (Util.canDoWithTime(lastTimeChat, 6000)) {
            lastTimeChat = System.currentTimeMillis();
            String[] funnyChat = {
                "Cà rốt thần chưởng! Ăn một củ là sáng mắt ra liền!",
                "Thỏ ngọc cute phô mai que thế này mà cũng nỡ đánh à!",
                "Một củ cà rốt, hai củ cà rốt... ai mua cà rốt không?",
                "Đánh đau thế, ta về mách Bà Hạt Mít phạt ngươi bây giờ!"
            };
            this.chat(funnyChat[Util.nextInt(0, funnyChat.length - 1)]);
        }
    }
}

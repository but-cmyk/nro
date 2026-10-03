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
        if (Util.canDoWithTime(lastTimeBay, 3500)) {
            super.bayLungTung();
            lastTimeBay = System.currentTimeMillis();
            if (Util.isTrue(50, 100)) {
                String[] funnyChat = {
                    "Đố anh bắt được em, lêu lêu đồ chậm chạp!",
                    "Ta bay như chim phượng hoàng, ngươi với tới sao được!",
                    "Ở dưới đất ngước nhìn lên có thấy ta ngầu lòi không?",
                    "Bay nhiều chóng mặt quá, ai cho xin viên thuốc say xe đi!"
                };
                this.chat(funnyChat[Util.nextInt(0, funnyChat.length - 1)]);
            }
        }
    }
}

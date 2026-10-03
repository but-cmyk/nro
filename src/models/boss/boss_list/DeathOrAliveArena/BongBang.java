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

public class BongBang extends DeathOrAliveArena {

    private long lastTimeChat = System.currentTimeMillis();

    public BongBang(Player player) throws Exception {
        super(PHOBAN, BossID.BONG_BANG, BossesData.BONG_BANG);
        this.playerAtt = player;
    }

    @Override
    public void attack() {
        super.attack();
        if (Util.canDoWithTime(lastTimeChat, 6000)) {
            lastTimeChat = System.currentTimeMillis();
            String[] funnyChat = {
                "Đứng im cho ta băng bó vết thương tâm hồn nào bạn hiền!",
                "Ối ối, rách mất lớp vải xịn ta săn sale hôm qua rồi!",
                "Quấn vải mấy ngàn năm chưa giặt, ngửi tí là xỉu liền!",
                "Cứu ta với, cuộn băng dính bị tuột hết rồi!"
            };
            this.chat(funnyChat[Util.nextInt(0, funnyChat.length - 1)]);
        }
    }
}

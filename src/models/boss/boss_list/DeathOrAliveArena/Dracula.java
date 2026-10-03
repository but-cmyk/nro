package models.boss.boss_list.DeathOrAliveArena;
import consts.BossID;
import models.boss.BossesData;
import static consts.BossType.PHOBAN;
import models.player.Player;
import services.Service;
import utils.Util;

public class Dracula extends DeathOrAliveArena {

    private long lastTimeHutMau = System.currentTimeMillis();

    public Dracula(Player player) throws Exception {
        super(PHOBAN, BossID.DRACULA, BossesData.DRACULA);
        this.playerAtt = player;
    }

    @Override
    public void hutMau() {
        try {
            if (Util.canDoWithTime(lastTimeHutMau, 15000) && this.nPoint.hp > this.nPoint.hpMax / 30) {
                long hp = Math.max(1, playerAtt.nPoint.hpMax / 100);
                playerAtt.nPoint.subHP(hp);
                this.nPoint.addHp(hp);
                Service.gI().Send_Info_NV(this);
                String[] funnyChat = {
                    "Máu vị trà sữa trân châu đường đen ngon bá cháy!",
                    "Ái chà, máu ngọt lịm thế này chắc chắn sâu răng rồi!",
                    "Hút miếng máu cho da dẻ hồng hào nào!",
                    "Ợ... no quá, xin cốc nước súc miệng nào!"
                };
                this.chat(funnyChat[Util.nextInt(0, funnyChat.length - 1)]);
                lastTimeHutMau = System.currentTimeMillis();
            }
        } catch (Exception e) {
        }
    }
}

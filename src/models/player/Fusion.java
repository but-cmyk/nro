package models.player;

import consts.ConstPlayer;
import utils.Util;

public class Fusion {

    public static final int TIME_FUSION = 600000;

    private Player player;

    public void setPlayer(Player player) {
        this.player = player;
    }
    public byte typeFusion;
    public long lastTimeFusion;

    public Fusion(Player player) {
        this.player = player;
    }

    public void update() {
        if (typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE && Util.canDoWithTime(lastTimeFusion, TIME_FUSION)) {
            if (this.player != null && this.player.pet != null) {
                this.player.pet.unFusion();
            }
        }
    }

    public void dispose() {
        this.player = null;
    }

}

package models.player;


import services.Service;
import utils.TimeUtil;
import utils.Util;

public class RewardBlackBall {

    private static final int TIME_REWARD = 79200000;

//    public static final int R1S_1 = 20;
    public static final int R1S_2 = 20;
    public static final int R2S_1 = 20;
//    public static final int R2S_2 = 20;
    public static final int R3S_1 = 20;
//    public static final int R3S_2 = 20;
//    public static final int R4S_1 = 20;
    public static final int R4S_2 = 20;
    public static final int R5S_1 = 20;
//    public static final int R5S_2 = 20;
//    public static final int R5S_3 = 20;
    public static final int R6S_1 = 20;
//    public static final int R6S_2 = 20;
    public static final int R7S_1 = 20;
//    public static final int R7S_2 = 20;

    public static final int TIME_WAIT = 3600000;
    private Player player;

    public long[] timeOutOfDateReward;
    public int[] quantilyBlackBall;
    public long[] lastTimeGetReward;

    public RewardBlackBall(Player player) {
        this.player = player;
        this.timeOutOfDateReward = new long[7];
        this.lastTimeGetReward = new long[7];
        this.quantilyBlackBall = new int[7];
    }

    public void reward(byte star) {
        if (star < 1 || star > 7) {
            return;
        }
        long time8h = TimeUtil.getStartTimeBlackBallWar();
        if (this.timeOutOfDateReward[star - 1] > time8h) {
            quantilyBlackBall[star - 1]++;
        }
        this.timeOutOfDateReward[star - 1] = System.currentTimeMillis() + TIME_REWARD;
        if (player != null) {
            Service.gI().point(player);
        }
    }

    public void getRewardSelect(byte select) {
        int index = 0;
        for (int i = 0; i < timeOutOfDateReward.length; i++) {
            if (timeOutOfDateReward[i] > System.currentTimeMillis()) {
                index++;
                if (index == select + 1) {
                    getReward(i + 1);
                    break;
                }
            }
        }
    }

    private void getReward(int star) {
        if (star < 1 || star > 7) {
            return;
        }
        if (timeOutOfDateReward[star - 1] > System.currentTimeMillis()) {
            String effectDesc = switch (star) {
                case 1 -> "Bùa 1 Sao Đen: +20% Sức Đánh";
                case 2 -> "Bùa 2 Sao Đen: +20% HP Tối Đa";
                case 3 -> "Bùa 3 Sao Đen: +20% Hút Máu HP";
                case 4 -> "Bùa 4 Sao Đen: +20% Phản Sát Thương";
                case 5 -> "Bùa 5 Sao Đen: +20% Chí Mạng & Sát Thương CM";
                case 6 -> "Bùa 6 Sao Đen: +20% KI Tối Đa";
                case 7 -> "Bùa 7 Sao Đen: +20% Né Đòn";
                default -> "Bùa Ngọc Rồng Sao Đen";
            };
            long timeLeftHours = Math.max(1, (timeOutOfDateReward[star - 1] - System.currentTimeMillis()) / 3600000L);
            Service.gI().sendThongBao(player, effectDesc + " đang có hiệu lực (còn khoảng " + timeLeftHours + " giờ)!");
        } else {
            Service.gI().sendThongBao(player, "Bùa sao đen này đã hết hạn!");
        }
    }

    public void dispose() {
        this.player = null;
    }
}

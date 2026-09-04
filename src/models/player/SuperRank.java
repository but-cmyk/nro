package models.player;

import database.daos.SuperRankDAO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import services.Service;
import services.tournament.SuperRankService;
import services.map.NpcService;
import utils.Util;

public class SuperRank {

    public boolean nhanGiai;
    private Player player;
    public int rank;
    public int win;
    public int lose;
    public List<String> history;
    public List<Long> lastTime;
    public long lastPKTime;
    public long lastRewardTime;
    public long lastDailyLoginReward;
    public int ticket = 3;

    public SuperRank(Player player) {
        this.player = player;
        this.history = new ArrayList<>();
        this.lastTime = new ArrayList<>();
        this.lastDailyLoginReward = 0;
    }

    public void history(String text, long lastTime) {
        if (this.history.size() > 4) {
            this.history.remove(0);
            this.lastTime.remove(0);
        }
        this.history.add(text);
        this.lastTime.add(lastTime);
    }

//    public void reward() {
//        int rw = SuperRankService.gI().reward(rank);
//        if (rw != -1 && nhanGiai == false) {
//            NpcService.gI().createTutorial(player, -1, "Bạn đang ở TOP " + rank + " võ đài Siêu Hạng và nhận được " + rw + " ngọc");
//            player.inventory.gem += rw;
//            nhanGiai = true;
//            lastRewardTime = System.currentTimeMillis();
//            SuperRankDAO.updatePlayer(player);
//        }
//        if (rank == 1) {
//            if (player.playerTask.taskdh.SieuHang < 1) {
//                int required = 1;
//                int percentDone = (int) ((double) player.playerTask.taskdh.SieuHang / required * 100);
//                player.playerTask.taskdh.SieuHang++;
//                player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
//                Service.gI().sendThongBao(player, "Tiến độ hiện tại:  " + percentDone + "%");
//            }
//        }
//    }
    public void reward() {
        if (player == null || player.inventory == null) {
            return;
        }
        if (Util.isAfterMidnight(lastRewardTime)) {
            nhanGiai = false;
        }
        int rw = SuperRankService.gI().reward(rank);
        if (rw != -1 && !nhanGiai) {
            nhanGiai = true;
            lastRewardTime = System.currentTimeMillis();
            player.inventory.addGem(rw);
            Service.gI().sendMoney(player);
            NpcService.gI().createTutorial(player, -1, "Bạn đang ở TOP " + rank + " võ đài Siêu Hạng và nhận được " + rw + " ngọc");
            SuperRankDAO.updatePlayer(player);
        }
        if (rank == 1 && player.playerTask != null && player.playerTask.taskdh != null) {
            if (player.playerTask.taskdh.SieuHang < 1) {
                player.playerTask.taskdh.SieuHang++;
                player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
            }
        }
    }

    public void dispose() {
        history.clear();
        lastTime.clear();
        win = -1;
        lose = -1;
        lastPKTime = -1;
        lastRewardTime = -1;
        lastDailyLoginReward = -1;
        player = null;
    }
}

package services.func.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.player.Player;
import server.Client;
import server.ServerNotify;
import services.Service;
import utils.Util;

public class CSMM implements Runnable {

    public static CSMM i;

    public static CSMM gI() {
        if (i == null) {
            i = new CSMM();
        }
        return i;
    }

    public long lastTimeRollCSMM = System.currentTimeMillis();
    public int NumRANDOM = -1;
    public HashMap<Player, List<Integer>> listRegNumber = new HashMap<>();
    public int totalRuby;
    public int SecondsTarget = 300;
    public int Size_Num = 999;
    public int costRuby = 1;

    @Override
    public void run() {
        while (true) {
            try {
                if (Util.canDoWithTime(lastTimeRollCSMM, (SecondsTarget * 1000L))) {
                    HandleCSMM();
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void HandleCSMM() {
        int ranNum = Util.nextInt(1, Size_Num);
        NumRANDOM = ranNum;

        ServerNotify.gI()
            .notify("Số may mắn lần này là [" + NumRANDOM + "]. Chúc các chiến binh gặp nhiều may mắn!");

        if (!listRegNumber.isEmpty()) {
            List<Player> players = new ArrayList<>();

            for (Map.Entry<Player, List<Integer>> entry : listRegNumber.entrySet()) {
                Player pl = entry.getKey();
                List<Integer> regNums = entry.getValue();

                if (regNums.contains(ranNum)) {
                    players.add(pl);
                }
            }

            List<Player> plWins = new ArrayList<>();
            for (Player pl : players) {
                if (pl != null && Client.gI().getPlayer(pl.id) != null) {
                    plWins.add(pl);
                }
            }

            if (!plWins.isEmpty()) {
                int reward = totalRuby / plWins.size();
                for (Player pl : plWins) {
                    giveReward(pl, reward);
                }
            }
        }

        lastTimeRollCSMM = System.currentTimeMillis();
        listRegNumber.clear();
        totalRuby = 0;
    }

    private void giveReward(Player pl, int numRuby) {
        pl.inventory.ruby += numRuby;
        Service.gI().sendThongBaoFromAdmin(pl, "Bạn vừa nhận được " + numRuby + " ngọc từ Con số may mắn!");
        Service.gI().sendMoney(pl);
    }

    public void Register(Player player, int numReg) {
        if (!listRegNumber.containsKey(player)) {
            listRegNumber.put(player, new ArrayList<>());
        }

        List<Integer> list = listRegNumber.get(player);

        if (list.contains(numReg)) {
            Service.gI().sendThongBao(player, "Bạn đã đăng ký số " + numReg + " rồi!");
            return;
        }

        if (player.inventory.ruby >= costRuby) {
            totalRuby += costRuby;
            player.inventory.ruby -= costRuby;
            list.add(numReg);
            Service.gI().sendThongBao(player, "Bạn đã đăng ký thành công với số " + numReg + "!");
            Service.gI().sendMoney(player);
        } else {
            Service.gI().sendThongBao(player, "Bạn còn thiếu " + (costRuby - player.inventory.ruby) + " ngọc nữa!");
        }
    }
}

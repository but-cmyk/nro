package models.matches.pvp;


import consts.ConstAchievement;
import models.matches.PVP;
import models.matches.TYPE_LOSE_PVP;
import models.matches.TYPE_PVP;
import services.AchievementService;
import models.player.Player;
import server.Client;
import services.Service;
import utils.Util;

public class ThachDau extends PVP {

    private int goldThachDau;
    private long goldReward;

    public ThachDau(Player p1, Player p2, int goldThachDau) {
        super(TYPE_PVP.THACH_DAU, p1, p2);
        this.goldThachDau = goldThachDau;
        this.goldReward = this.goldThachDau + (long) this.goldThachDau * 80 / 100;
        this.p1.inventory.subGold(this.goldThachDau);
        this.p2.inventory.subGold(this.goldThachDau);
        Service.gI().sendMoney(this.p1);
        Service.gI().sendMoney(this.p2);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void finish() {

    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void update() {
        if (started && System.currentTimeMillis() - this.lastTimeStart > 300_000L) {
            this.started = false;
            if (this.p1 != null) {
                this.p1.inventory.addGold(this.goldThachDau);
                Service.gI().sendMoney(this.p1);
                Service.gI().sendThongBao(this.p1, "Trận thách đấu bất phân thắng bại sau 5 phút, bạn nhận lại tiền cược.");
            }
            if (this.p2 != null) {
                this.p2.inventory.addGold(this.goldThachDau);
                Service.gI().sendMoney(this.p2);
                Service.gI().sendThongBao(this.p2, "Trận thách đấu bất phân thắng bại sau 5 phút, bạn nhận lại tiền cược.");
            }
            this.dispose();
        }
    }

    @Override
    public void reward(Player plWin) {
        if (plWin != null) {
            plWin.inventory.addGold(this.goldReward);
            Service.gI().sendMoney(plWin);
        }
    }

    @Override
    public void sendResult(Player plLose, TYPE_LOSE_PVP typeLose) {
        Player plWin = (p1 != null && p1.equals(plLose)) ? p2 : p1;
        if (typeLose == TYPE_LOSE_PVP.RUNS_AWAY) {
            Player plL = (plLose != null) ? Client.gI().getPlayer(plLose.id) : null;
            if (plWin != null) {
                if (plL == null) {
                    Service.gI().sendThongBao(plWin, "Đối thủ rời game, bạn thắng được " + Util.powerToString(this.goldReward) + " vàng");
                } else {
                    Service.gI().sendThongBao(plWin, "Đối thủ sợ quá bỏ chạy, bạn thắng được " + Util.powerToString(this.goldReward) + " vàng");
                }
            }
            if (plLose != null) {
                Service.gI().sendThongBao(plLose, "Bạn bị xử thua vì đã bỏ chạy");
            }
        } else if (typeLose == TYPE_LOSE_PVP.DEAD) {
            if (plWin != null) {
                Service.gI().sendThongBao(plWin, "Đối thủ đã kiệt sức, bạn thắng được " + Util.powerToString(this.goldReward) + " vàng");
            }
            if (plLose != null) {
                Service.gI().sendThongBao(plLose, "Bạn đã thua vì đã kiệt sức");
            }
        }
        if (p1 != null && !p1.equals(plLose)) {
            AchievementService.gI().checkDoneTask(p1, ConstAchievement.TRAM_TRAN_TRAM_THANG);
        }
    }

}

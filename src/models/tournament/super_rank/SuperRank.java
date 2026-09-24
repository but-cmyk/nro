package models.tournament.super_rank;


import database.daos.SuperRankDAO;
import managers.SuperRankManager;
import services.tournament.SuperRankService;
import utils.Functions;
import models.boss.Boss;
import consts.BossStatus;
import models.boss.boss_list.SuperRank.Rival;
import consts.ConstPlayer;
import consts.ConstSuperRank;
import models.map.Zone;
import models.matches.pvp.DHVT;
import models.player.Player;
import server.Maintenance;
import server.ServerNotify;
import services.player.PlayerService;
import services.Service;
import services.map.ChangeMapService;
import utils.Util;

public final class SuperRank implements Runnable {

    private Zone zone;
    private boolean isCompeting;
    private long playerId;
    private long rivalId;
    private Player player;
    private Boss rival;
    public int timeUp;
    public int timeDown;
    public int rankWin;
    public int rankLose;
    public boolean win;
    public int error;

    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public boolean isCompeting() { return isCompeting; }
    public void setCompeting(boolean isCompeting) { this.isCompeting = isCompeting; }

    public long getPlayerId() { return playerId; }
    public void setPlayerId(long playerId) { this.playerId = playerId; }

    public long getRivalId() { return rivalId; }
    public void setRivalId(long rivalId) { this.rivalId = rivalId; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Boss getRival() { return rival; }
    public void setRival(Boss rival) { this.rival = rival; }

    public SuperRank(Player player, long rivalId, Zone zone) {
        try {
            this.playerId = player.id;
            this.rivalId = rivalId;
            this.zone = zone;
            this.player = player;
            this.player.isPKDHVT = true;
            this.rankLose = player.superRank.rank;
            Player riv = SuperRankService.gI().loadPlayer(rivalId);
            this.rankWin = riv.superRank.rank;
            riv.nPoint.calPoint();
            this.rival = new Rival(player, riv);
            this.zone.isCompeting = true;
            this.zone.rank1 = player.superRank.rank;
            this.zone.rank2 = riv.superRank.rank;
            this.zone.rankName1 = player.name;
            this.zone.rankName2 = riv.name;
            init();
        } catch (Exception e) {
            dispose();
        }
    }

    public void init() {
        timeUp = 0;
        timeDown = 180;
        rankLose = player.superRank.rank;
        isCompeting = true;
        win = false;
        if (player.zone.zoneId != zone.zoneId) {
            ChangeMapService.gI().changeZone(player, zone.zoneId);
        }
        Thread.ofVirtual().name("SuperRank-Worker").start(this);
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning && isCompeting) {
            long startTime = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                if (error < 5) {
                    error++;
                    System.err.println(e);
                }
            }
            Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - startTime), 10));
        }
    }

    public void update() {
        if (win) {
            return;
        }
        if (timeUp < 5) {
            switch (timeUp) {
                case 0 -> {
                    Service.gI().sendThongBao(player, "Trận đấu bắt đầu");
                    Service.gI().setPos0(player, 334, 264);
                    Service.gI().setPos0(rival, 434, 264);
                }
                case 2 -> {
                    Service.gI().chat(rival, ConstSuperRank.TEXT_SAN_SANG_CHUA);
                }
                case 3 -> {
                    Service.gI().chat(player, ConstSuperRank.TEXT_SAN_SANG);
                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_PVP);
                    PlayerService.gI().changeAndSendTypePK(rival, ConstPlayer.PK_PVP);
                    Service.gI().sendTypePK(player, rival);
                    new DHVT(player, rival);
                }
                case 4 -> {
                    rival.changeStatus(BossStatus.ACTIVE);
                }
            }
            timeUp++;
            return;
        }
        if (timeDown > 0) {
            timeDown--;
            if (player != null && player.isPKDHVT && !player.lostByDeath && player.location != null && !player.isDie() && player.zone != null && player.zone.equals(zone)) {
                if (rival == null || rival.zone == null || rival.isDie()) {
                    win();
                }
            } else {
                lose();
            }
        } else {
            lose();
        }
    }

    public void win() {
        win = true;
        try {
            finish();
            int currentRivalRank = SuperRankDAO.getRank((int) rivalId);
            if (currentRivalRank > 0 && player != null && currentRivalRank < player.superRank.rank) {
                rankWin = currentRivalRank;
                rankLose = player.superRank.rank;
            }

            Player plLose = SuperRankService.gI().loadPlayer(rivalId);
            if (plLose != null) {
                plLose.superRank.lose++;
                plLose.superRank.rank = rankLose;
                plLose.superRank.history("Thua " + (player != null ? player.name : "Đối thủ") + "[" + rankWin + "]", System.currentTimeMillis());
                SuperRankDAO.updatePlayer(plLose);
            }

            if (player != null) {
                player.superRank.win++;
                if (player.superRank.ticket > 0) {
                    player.superRank.ticket--;
                } else if (player.inventory.getGem() >= 2) {
                    player.inventory.subGem(2);
                    Service.gI().sendMoney(player);
                }
                player.superRank.rank = rankWin;
                player.superRank.history("Hạ " + (plLose != null ? plLose.name : "Đối thủ") + "[" + rankLose + "]", System.currentTimeMillis());
                SuperRankDAO.updatePlayer(player);
                if (player.zone != null) {
                    Service.gI().chat(player, ConstSuperRank.TEXT_THANG.replaceAll("%1", rankWin + ""));
                }
            }

            if (rankWin <= 10 && player != null) {
                ServerNotify.gI().notify(ConstSuperRank.TEXT_TOP_10.replaceAll("%1", player.name).replaceAll("%2", rankWin + ""));
            }

            Player rv = SuperRankService.gI().getPlayer(rivalId);
            if (rv != null && plLose != null) {
                rv.superRank.lose = plLose.superRank.lose;
                rv.superRank.rank = rankLose;
                rv.superRank.history("Thua " + (player != null ? player.name : "Đối thủ") + "[" + rankWin + "]", System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dispose();
    }

    public void lose() {
        try {
            finish();
            Player plWin = SuperRankService.gI().loadPlayer(rivalId);
            if (plWin != null) {
                plWin.superRank.win++;
                plWin.superRank.rank = rankWin;
                plWin.superRank.history("Hạ " + (player != null ? player.name : "Đối thủ") + "[" + rankLose + "]", System.currentTimeMillis());
                SuperRankDAO.updatePlayer(plWin);
            }

            if (player != null) {
                player.superRank.lose++;
                if (player.superRank.ticket > 0) {
                    player.superRank.ticket--;
                } else if (player.inventory.getGem() >= 3) {
                    player.inventory.subGem(3);
                    Service.gI().sendMoney(player);
                }
                player.superRank.rank = rankLose;
                player.superRank.history("Thua " + (plWin != null ? plWin.name : "Đối thủ") + "[" + rankWin + "]", System.currentTimeMillis());
                SuperRankDAO.updatePlayer(player);
                if (player.zone != null) {
                    Service.gI().chat(player, ConstSuperRank.TEXT_THUA);
                }
            }

            Player rv = SuperRankService.gI().getPlayer(rivalId);
            if (rv != null && plWin != null) {
                rv.superRank.win = plWin.superRank.win;
                rv.superRank.rank = rankWin;
                rv.superRank.history("Hạ " + (player != null ? player.name : "Đối thủ") + "[" + rankLose + "]", System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dispose();
    }

    public void finish() {
        if (rival != null) {
            rival.leaveMap();
        }
        if (player != null && player.zone != null && player.zone.equals(zone)) {
            if (player.isDie()) {
                Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
            }
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            Service.gI().sendPlayerVS(player, null, (byte) 0);
        }
    }

    public void dispose() {
        if (player != null && player.location != null) {
            Service.gI().setPos(player, Util.nextInt(250, 450), 360);
        }
        if (this.zone != null) {
            this.zone.isCompeting = false;
            this.zone.rank1 = -1;
            this.zone.rank2 = -1;
            this.zone.rankName1 = null;
            this.zone.rankName2 = null;
        }
        isCompeting = false;
        if (player != null) {
            player.isPKDHVT = false;
            player = null;
        }
        if (rival != null) {
            rival.dispose();
        }
        rival = null;
        zone = null;
        playerId = -1;
        rivalId = -1;
        rankWin = -1;
        rankLose = -1;
        SuperRankManager.gI().removeSPR(this);
    }

}

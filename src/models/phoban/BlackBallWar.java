package models.phoban;
import utils.Functions;
import database.daos.NDVSqlFetcher;
import database.daos.PlayerDAO;
import models.player.Player;
import services.map.MapService;
import services.player.PlayerService;
import services.Service;
import services.map.ChangeMapService;
import utils.TimeUtil;
import utils.Util;

import models.map.Zone;
import server.Client;
import server.Maintenance;

public class BlackBallWar {

    public Zone getZone() { return this.zone; }

    public static final int TIME_CAN_PICK_BLACK_BALL_AFTER_DROP = 5000;

    public static final byte X3 = 3;
    public static final byte X5 = 5;
    public static final byte X7 = 7;

    public static final int COST_X3 = 5;
    public static final int COST_X5 = 10;
    public static final int COST_X7 = 20;

    public static final byte HOUR_OPEN = 20;
    public static final byte MIN_OPEN = 0;
    public static final byte SECOND_OPEN = 0;

    public static final byte HOUR_CAN_PICK_DB = 20;
    public static final byte MIN_CAN_PICK_DB = 30;
    public static final byte SECOND_CAN_PICK_DB = 0;

    public static final byte HOUR_CLOSE = 21;
    public static final byte MIN_CLOSE = 0;
    public static final byte SECOND_CLOSE = 0;

    public static final int AVAILABLE = 1;
    private static final int TIME_WIN = 300000;

    public Zone zone;

    public BlackBallWar(Zone zone) {
        this.zone = zone;
    }

    public void update() {
        if (!TimeUtil.isBlackBallWarOpen()) {
            zone.finishBlackBallWar = false;
        }
        for (int i = zone.getNumOfPlayers() - 1; i >= 0; i--) {
            try {
                updatePlayer(zone.getPlayers().get(i));
            } catch (Exception e) {
            }
        }
    }

    public void updatePlayer(Player player) {
        if (player.zone == null || !MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
            return;
        }
        if (!TimeUtil.isBlackBallWarOpen()) {
            kickOutOfMap(player);
            return;
        }

        if (player.idMark.isHoldBlackBall()) {
            if (Util.canDoWithTime(player.idMark.getLastTimeHoldBlackBall(), TIME_WIN)) {
                win(player);
            } else if (Util.canDoWithTime(player.idMark.getLastTimeNotifyTimeHoldBlackBall(), 10000)) {
                Service.gI().sendThongBao(player, "Cố giữ ngọc thêm "
                        + TimeUtil.getSecondLeft(player.idMark.getLastTimeHoldBlackBall(), TIME_WIN / 1000)
                        + " giây nữa sẽ thắng");
                player.idMark.setLastTimeNotifyTimeHoldBlackBall(System.currentTimeMillis());
            }
        }
    }

    private void win(Player player) {
        player.zone.finishBlackBallWar = true;
        int star = player.idMark.getTempIdBlackBallHold() - 371;

        // Reset trạng thái giữ ngọc của người thắng ngay tại đây để không kích hoạt dropBlackBall rơi thừa ngọc
        player.idMark.setHoldBlackBall(false);
        player.idMark.setTempIdBlackBallHold(-1);
        Service.gI().sendFlagBag(player);

        player.rewardBlackBall.reward((byte) star);
        PlayerDAO.updatePlayerAsync(player);
        Service.gI().sendThongBao(player, "Chúc mừng bạn đã dành được Ngọc rồng " + star + " sao đen cho bang");

        if (player.clan != null) {
            final long winnerId = player.id;
            Thread.ofVirtual().name("BlackBallWar-RewardWorker").start(() -> {
                try {
                    for (int i = 0; i < player.clan.members.size(); i++) {
                        var m = player.clan.members.get(i);
                        if (m.id == winnerId) {
                            continue;
                        }
                        Player onlinePlayer = Client.gI().getPlayer(m.id);
                        if (onlinePlayer != null) {
                            onlinePlayer.rewardBlackBall.reward((byte) star);
                            PlayerDAO.updatePlayerAsync(onlinePlayer);
                        } else {
                            PlayerDAO.updateBlackBallReward(m.id, (byte) star);
                        }
                    }
                } catch (Exception e) {
                    utils.Logger.error("Lỗi trao thưởng bang hội BlackBallWar: " + e.getMessage());
                }
            });
        }

        kickAllPlayersOutOfMap(player.zone);
    }

    private void kickOutOfMap(Player player) {
        if (player.isDie()) {
            PlayerService.gI().hoiSinh(player);
        }

        Service.gI().changeFlag(player, 0);

        Service.gI().sendThongBao(player, "Trò chơi tìm ngọc hôm nay đã kết thúc, hẹn gặp lại vào 20h ngày mai");

        ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 24, -1, 250);
    }

    private void kickAllPlayersOutOfMap(Zone zone) {
        for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
            if (i < zone.getPlayers().size()) {
                Player pl = zone.getPlayers().get(i);
                kickOutOfMap(pl);
            }
        }
    }

}

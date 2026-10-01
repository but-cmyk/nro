package models.tournament.whis_arena;

import consts.ConstNpc;
import consts.ConstPlayer;
import database.daos.PlayerDAO;
import models.map.Zone;
import models.matches.pvp.DHVT;
import models.player.Player;
import server.Maintenance;
import services.EffectSkillService;
import services.ItemTimeService;
import services.Service;
import services.map.ChangeMapService;
import services.map.NpcService;
import services.player.PlayerService;
import utils.Functions;
import utils.Util;

public class WhisArenaPvPMatch implements Runnable {

    private final Player p1;
    private final Player p2;
    private final Zone zone;

    private boolean isFinished;
    private Player winner;
    private int timeUp;
    private int timeDown = 180;
    private boolean inCountdown = true;

    public WhisArenaPvPMatch(Player p1, Player p2, Zone zone) {
        this.p1 = p1;
        this.p2 = p2;
        this.zone = zone;
    }

    public void start() {
        try {
            this.zone.isCompeting = true;
            this.zone.rankName1 = p1.name;
            this.zone.rank1 = p1.arenaWins;
            this.zone.rankName2 = p2.name;
            this.zone.rank2 = p2.arenaWins;
            this.p1.isPKDHVT = true;
            this.p2.isPKDHVT = true;

            // Nếu người chơi ở khác zone thì chuyển về zone đấu ở sảnh (300, 360) trước
            if (p1.zone == null || p1.zone.zoneId != zone.zoneId || p1.zone.map.mapId != 186) {
                ChangeMapService.gI().changeMap(p1, zone, 300, 360);
            }
            if (p2.zone == null || p2.zone.zoneId != zone.zoneId || p2.zone.map.mapId != 186) {
                ChangeMapService.gI().changeMap(p2, zone, 300, 360);
            }

            // Thông báo đếm ngược 3 giây trước khi đưa 2 đấu thủ lên sàn
            Service.gI().sendThongBao(p1, "Ghép đấu thành công với " + p2.name + "! Trận đấu sẽ bắt đầu sau 3 giây... Hãy chuẩn bị!");
            Service.gI().sendThongBao(p2, "Ghép đấu thành công với " + p1.name + "! Trận đấu sẽ bắt đầu sau 3 giây... Hãy chuẩn bị!");

            Thread.ofVirtual().name("WhisArenaPvPMatch-" + p1.id + "-" + p2.id).start(this);
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
        }
    }

    @Override
    public void run() {
        // Đợi 3 giây trước khi đưa 2 đấu thủ lên võ đài
        Functions.sleep(3000);
        if (isFinished || Maintenance.isRunning) {
            dispose();
            return;
        }

        try {
            ItemTimeService.gI().removeItemTime(p1, 3779);
            ItemTimeService.gI().removeItemTime(p2, 3779);

            // Đưa cả 2 player lên võ đài (P1: 335, 264; P2: 435, 264)
            if (p1.zone == null || p1.zone.zoneId != zone.zoneId || p1.zone.map.mapId != 186) {
                ChangeMapService.gI().changeMap(p1, zone, 335, 264);
            } else {
                PlayerService.gI().playerMove(p1, 335, 264);
                Service.gI().setPos0(p1, 335, 264);
            }

            if (p2.zone == null || p2.zone.zoneId != zone.zoneId || p2.zone.map.mapId != 186) {
                ChangeMapService.gI().changeMap(p2, zone, 435, 264);
            } else {
                PlayerService.gI().playerMove(p2, 435, 264);
                Service.gI().setPos0(p2, 435, 264);
            }

            Service.gI().releaseCooldownSkill(p1);
            Service.gI().releaseCooldownSkill(p2);
            if (p1.nPoint != null) {
                p1.nPoint.hp = p1.nPoint.hpMax;
                p1.nPoint.mp = p1.nPoint.mpMax;
                Service.gI().Send_Info_NV(p1);
            }
            if (p2.nPoint != null) {
                p2.nPoint.hp = p2.nPoint.hpMax;
                p2.nPoint.mp = p2.nPoint.mpMax;
                Service.gI().Send_Info_NV(p2);
            }

            // Hiệu ứng đứng yên cho 2 đấu thủ chuẩn bị giống võ đài võ thuật 23
            EffectSkillService.gI().startStun(p1, System.currentTimeMillis(), 8000);
            EffectSkillService.gI().startStun(p2, System.currentTimeMillis(), 8000);
            ItemTimeService.gI().sendItemTime(p1, 3779, 7);
            ItemTimeService.gI().sendItemTime(p2, 3779, 7);
            PlayerService.gI().changeAndSendTypePK(p1, ConstPlayer.NON_PK);
            PlayerService.gI().changeAndSendTypePK(p2, ConstPlayer.NON_PK);
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
            return;
        }

        while (!Maintenance.isRunning && !isFinished) {
            long startTick = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long elapsed = System.currentTimeMillis() - startTick;
            Functions.sleep(Math.max(1000 - elapsed, 10));
        }
    }

    private void update() {
        if (isFinished) return;

        if (inCountdown) {
            Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
            switch (timeUp) {
                case 0 -> {
                    Service.gI().setPos0(p1, 335, 264);
                    Service.gI().setPos0(p2, 435, 264);
                    EffectSkillService.gI().startStun(p1, System.currentTimeMillis(), 8000);
                    EffectSkillService.gI().startStun(p2, System.currentTimeMillis(), 8000);
                    ItemTimeService.gI().sendItemTime(p1, 3779, 7);
                    ItemTimeService.gI().sendItemTime(p2, 3779, 7);
                    if (trongTai != null) {
                        PlayerService.gI().playerMove(trongTai, 385, 264);
                        Service.gI().chat(trongTai, "Trận đấu giữa " + p1.name + " vs " + p2.name + " sắp diễn ra");
                    }
                }
                case 1 -> {
                    if (trongTai != null) {
                        Service.gI().chat(trongTai, "Xin quý vị khán giả cho 1 tràng pháo tay cổ vũ cho 2 đấu thủ nào");
                    }
                }
                case 2 -> {
                    if (trongTai != null) {
                        Service.gI().chat(trongTai, "Mọi người hãy ổn định chỗ ngồi, trận đấu sẽ bắt đầu sau 3 giây nữa");
                    }
                }
                case 3 -> {
                    if (trongTai != null) {
                        Service.gI().chat(trongTai, "3");
                    }
                }
                case 4 -> {
                    if (trongTai != null) {
                        Service.gI().chat(trongTai, "2");
                    }
                }
                case 5 -> {
                    if (trongTai != null) {
                        Service.gI().chat(trongTai, "1");
                    }
                }
                case 6 -> {
                    inCountdown = false;
                    if (trongTai != null) {
                        Service.gI().chat(trongTai, "Trận đấu bắt đầu");
                        PlayerService.gI().playerMove(trongTai, 385, 360);
                    }
                    EffectSkillService.gI().removeStun(p1);
                    EffectSkillService.gI().removeStun(p2);
                    ItemTimeService.gI().removeItemTime(p1, 3779);
                    ItemTimeService.gI().removeItemTime(p2, 3779);
                    Service.gI().chat(p1, "OK");
                    Service.gI().chat(p2, "OK");

                    PlayerService.gI().changeAndSendTypePK(p1, ConstPlayer.PK_PVP);
                    PlayerService.gI().changeAndSendTypePK(p2, ConstPlayer.PK_PVP);
                    Service.gI().sendTypePK(p1, p2);
                    Service.gI().sendTypePK(p2, p1);
                    new DHVT(p1, p2);
                }
            }
            timeUp++;
            return;
        }

        if (timeDown > 0) {
            timeDown--;

            // Kiểm tra trạng thái p1
            boolean p1Lost = (p1 == null || p1.isDie() || p1.zone == null || !p1.zone.equals(zone)
                    || (p1.location != null && p1.location.y > 275 && (p1.location.x < 160 || p1.location.x > 610)));
            // Kiểm tra trạng thái p2
            boolean p2Lost = (p2 == null || p2.isDie() || p2.zone == null || !p2.zone.equals(zone)
                    || (p2.location != null && p2.location.y > 275 && (p2.location.x < 160 || p2.location.x > 610)));

            if (p1Lost && p2Lost) {
                finishMatch(null);
                return;
            } else if (p1Lost) {
                finishMatch(p2);
                return;
            } else if (p2Lost) {
                finishMatch(p1);
                return;
            }

            if (timeDown == 60 || timeDown == 30 || timeDown <= 5) {
                Service.gI().sendThongBao(p1, "Thời gian còn lại: " + timeDown + " giây");
                Service.gI().sendThongBao(p2, "Thời gian còn lại: " + timeDown + " giây");
            }
        } else {
            // Hết giờ: phân định theo % HP
            double hpPct1 = (p1 != null && p1.nPoint != null) ? (double) p1.nPoint.hp / p1.nPoint.hpMax : 0;
            double hpPct2 = (p2 != null && p2.nPoint != null) ? (double) p2.nPoint.hp / p2.nPoint.hpMax : 0;
            if (hpPct1 >= hpPct2) {
                finishMatch(p1);
            } else {
                finishMatch(p2);
            }
        }
    }

    private synchronized void finishMatch(Player winPlayer) {
        if (isFinished) return;
        isFinished = true;
        this.winner = winPlayer;

        try {
            // 1. Tắt đồng hồ đếm ngược & giải phóng trạng thái PK
            if (p1 != null) {
                ItemTimeService.gI().removeItemTime(p1, 3779);
                PlayerService.gI().changeAndSendTypePK(p1, ConstPlayer.NON_PK);
            }
            if (p2 != null) {
                ItemTimeService.gI().removeItemTime(p2, 3779);
                PlayerService.gI().changeAndSendTypePK(p2, ConstPlayer.NON_PK);
            }

            // 2. Hồi sinh người bị hạ gục ngay trên sàn đấu để đứng dậy nghe kết quả
            if (p1 != null && p1.isDie()) {
                Service.gI().hsChar(p1, p1.nPoint.hpMax, p1.nPoint.mpMax);
            }
            if (p2 != null && p2.isDie()) {
                Service.gI().hsChar(p2, p2.nPoint.hpMax, p2.nPoint.mpMax);
            }

            // 3. Trọng tài lên võ đài tuyên bố & đấu thủ đối thoại trên sàn
            Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
            if (trongTai != null) {
                PlayerService.gI().playerMove(trongTai, 385, 264);
            }
            if (winner != null) {
                Player loser = (winner.equals(p1)) ? p2 : p1;
                if (trongTai != null) {
                    Service.gI().chat(trongTai, "Trận đấu kết thúc! " + winner.name + " đã chiến thắng!");
                } else {
                    NpcService.gI().npcChat(zone, ConstNpc.TRONG_TAI_2, "Trận đấu kết thúc! " + winner.name + " đã chiến thắng!");
                }
                Service.gI().chat(winner, "Thắng rồi! Cậu thi đấu rất cừ " + (loser != null ? loser.name : "") + "!");
                if (loser != null) {
                    Service.gI().chat(loser, "Khá lắm, lần sau ta sẽ phục thù!");
                }
            } else {
                if (trongTai != null) {
                    Service.gI().chat(trongTai, "Hết thời gian! Trận đấu kết thúc với kết quả HÒA!");
                } else {
                    NpcService.gI().npcChat(zone, ConstNpc.TRONG_TAI_2, "Hết thời gian! Trận đấu kết thúc với kết quả HÒA!");
                }
            }

            // 4. Delay 2.5s trên võ đài (chuẩn DHVT/Siêu Hạng)
            Functions.sleep(2500);

            // 5. Cập nhật điểm & thông báo kết quả
            if (winner != null) {
                Player loser = (winner.equals(p1)) ? p2 : p1;
                winner.arenaWins += 10;
                WhisArenaService.gI().addReward(winner, 459); // Thưởng 1 Phiếu giảm giá vào hòm thư Whis!
                PlayerDAO.updatePlayer(winner);
                Service.gI().sendThongBao(winner, "Chúc mừng bạn đã chiến thắng đối thủ " + (loser != null ? loser.name : "") + "! Nhận được +10 Điểm Võ Đài và 1 Phiếu giảm giá (Hiện có: " + winner.arenaWins + " điểm).");

                if (loser != null) {
                    // Thua trận: bị trừ điểm bằng đúng điểm người thắng nhận được (+10)
                    loser.arenaWins = Math.max(0, loser.arenaWins - 10);
                    PlayerDAO.updatePlayer(loser);
                    Service.gI().sendThongBao(loser, "Bạn đã bị đối thủ " + winner.name + " đánh bại và bị trừ 10 Điểm Võ Đài. Chúc bạn may mắn lần sau! (Hiện có: " + loser.arenaWins + " điểm).");
                }
            } else {
                // Trận đấu hòa: 0 điểm
                if (p1 != null) {
                    Service.gI().sendThongBao(p1, "Hết giờ! Trận đấu kết thúc với kết quả hòa.");
                }
                if (p2 != null) {
                    Service.gI().sendThongBao(p2, "Hết giờ! Trận đấu kết thúc với kết quả hòa.");
                }
            }
            WhisArenaBotManager.gI().sortBots();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dispose();
        }
    }

    private volatile boolean isDisposed = false;

    public synchronized void dispose() {
        if (isDisposed) {
            return;
        }
        isDisposed = true;
        try {
            try {
                Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
                if (trongTai != null) {
                    PlayerService.gI().playerMove(trongTai, 385, 264);
                }
            } catch (Exception ignored) {}
            cleanupPlayer(p1);
            cleanupPlayer(p2);
            if (zone != null) {
                zone.isCompeting = false;
                zone.rankName1 = null;
                zone.rankName2 = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanupPlayer(Player pl) {
        if (pl != null) {
            EffectSkillService.gI().removeStun(pl);
            ItemTimeService.gI().removeItemTime(pl, 3779);
            if (pl.pvp != null) {
                pl.pvp.dispose();
                pl.pvp = null;
            }
            pl.isPKDHVT = false;
            if (pl.zone != null && pl.zone.map.mapId == 186) {
                if (pl.isDie()) {
                    Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
                }
                PlayerService.gI().changeAndSendTypePK(pl, ConstPlayer.NON_PK);
                int groundX = Util.nextInt(250, 450);
                int groundY = 360;
                PlayerService.gI().playerMove(pl, groundX, groundY);
                Service.gI().setPos(pl, groundX, groundY);
            }
        }
    }
}

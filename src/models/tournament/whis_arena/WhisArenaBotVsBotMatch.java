package models.tournament.whis_arena;

import consts.BossStatus;
import consts.ConstNpc;
import consts.ConstPlayer;
import models.map.Zone;
import models.matches.pvp.DHVT;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import services.map.ChangeMapService;
import services.map.NpcService;
import services.player.PlayerService;
import utils.Functions;

public class WhisArenaBotVsBotMatch implements Runnable {

    private final WhisArenaLobbyBot bot1;
    private final WhisArenaLobbyBot bot2;
    private final Zone zone;
    private boolean isFinished;
    private int timeUp;
    private int timeDown = 90; // 90 giây
    private boolean inCountdown = true;

    public WhisArenaBotVsBotMatch(WhisArenaLobbyBot bot1, WhisArenaLobbyBot bot2, Zone zone) {
        this.bot1 = bot1;
        this.bot2 = bot2;
        this.zone = zone;
    }

    public void start() {
        try {
            this.zone.isCompeting = true;
            this.zone.rankName1 = bot1.name;
            this.zone.rank1 = bot1.arenaWins;
            this.zone.rankName2 = bot2.name;
            this.zone.rank2 = bot2.arenaWins;
            this.bot1.isPKDHVT = true;
            this.bot2.isPKDHVT = true;
            this.bot1.botState = WhisArenaLobbyBot.BotLobbyState.FIGHTING;
            this.bot2.botState = WhisArenaLobbyBot.BotLobbyState.FIGHTING;
            this.bot1.currentBotMatch = this;
            this.bot2.currentBotMatch = this;
            this.bot1.setPlayerAtt(bot2);
            this.bot2.setPlayerAtt(bot1);

            Thread.ofVirtual().name("WhisArenaBotMatch-" + bot1.id + "-" + bot2.id).start(this);
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
        }
    }

    @Override
    public void run() {
        // Đợi 3 giây trước khi đưa 2 bot lên võ đài
        Functions.sleep(3000);
        if (isFinished || server.Maintenance.isRunning) {
            dispose();
            return;
        }

        try {
            // Đưa cả 2 bot lên võ đài (bot1: 335, 264; bot2: 435, 264)
            if (bot1.zone != null && bot1.zone.equals(zone)) {
                PlayerService.gI().playerMove(bot1, 335, 264);
                Service.gI().setPos0(bot1, 335, 264);
            } else {
                ChangeMapService.gI().changeMap(bot1, zone, 335, 264);
            }
            if (bot2.zone != null && bot2.zone.equals(zone)) {
                PlayerService.gI().playerMove(bot2, 435, 264);
                Service.gI().setPos0(bot2, 435, 264);
            } else {
                ChangeMapService.gI().changeMap(bot2, zone, 435, 264);
            }

            Service.gI().releaseCooldownSkill(bot1);
            Service.gI().releaseCooldownSkill(bot2);
            if (bot1.nPoint != null) {
                bot1.nPoint.hp = bot1.nPoint.hpMax;
                bot1.nPoint.mp = bot1.nPoint.mpMax;
                Service.gI().Send_Info_NV(bot1);
            }
            if (bot2.nPoint != null) {
                bot2.nPoint.hp = bot2.nPoint.hpMax;
                bot2.nPoint.mp = bot2.nPoint.mpMax;
                Service.gI().Send_Info_NV(bot2);
            }

            // Hiệu ứng đứng yên cho 2 đấu thủ chuẩn bị giống võ đài võ thuật 23
            EffectSkillService.gI().startStun(bot1, System.currentTimeMillis(), 8000);
            EffectSkillService.gI().startStun(bot2, System.currentTimeMillis(), 8000);
            PlayerService.gI().changeAndSendTypePK(bot1, ConstPlayer.NON_PK);
            PlayerService.gI().changeAndSendTypePK(bot2, ConstPlayer.NON_PK);
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
            return;
        }

        while (!server.Maintenance.isRunning && !isFinished) {
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
                    Service.gI().setPos0(bot1, 335, 264);
                    Service.gI().setPos0(bot2, 435, 264);
                    EffectSkillService.gI().startStun(bot1, System.currentTimeMillis(), 8000);
                    EffectSkillService.gI().startStun(bot2, System.currentTimeMillis(), 8000);
                    if (trongTai != null) {
                        PlayerService.gI().playerMove(trongTai, 385, 264);
                        Service.gI().chat(trongTai, "Trận đấu giữa " + bot1.name + " vs " + bot2.name + " sắp diễn ra");
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
                    EffectSkillService.gI().removeStun(bot1);
                    EffectSkillService.gI().removeStun(bot2);
                    Service.gI().chat(bot1, "OK");
                    Service.gI().chat(bot2, "OK");

                    PlayerService.gI().changeAndSendTypePK(bot1, ConstPlayer.PK_PVP);
                    PlayerService.gI().changeAndSendTypePK(bot2, ConstPlayer.PK_PVP);
                    Service.gI().sendTypePK(bot1, bot2);
                    Service.gI().sendTypePK(bot2, bot1);
                    new DHVT(bot1, bot2);
                    bot1.changeStatus(BossStatus.ACTIVE);
                    bot2.changeStatus(BossStatus.ACTIVE);
                }
            }
            timeUp++;
            return;
        }

        // Đang thi đấu
        if (timeDown > 0) {
            timeDown--;

            boolean b1Lost = (bot1 == null || bot1.isDie() || bot1.zone == null || !bot1.zone.equals(zone)
                    || (bot1.location != null && bot1.location.y > 275 && (bot1.location.x < 160 || bot1.location.x > 610)));
            boolean b2Lost = (bot2 == null || bot2.isDie() || bot2.zone == null || !bot2.zone.equals(zone)
                    || (bot2.location != null && bot2.location.y > 275 && (bot2.location.x < 160 || bot2.location.x > 610)));

            if (b1Lost && b2Lost) {
                double p1 = (bot1 != null && bot1.nPoint != null) ? (double) bot1.nPoint.hp / (double) bot1.nPoint.hpMax : 0;
                double p2 = (bot2 != null && bot2.nPoint != null) ? (double) bot2.nPoint.hp / (double) bot2.nPoint.hpMax : 0;
                if (p1 >= p2) {
                    finishMatch(bot1, bot2);
                } else {
                    finishMatch(bot2, bot1);
                }
                return;
            } else if (b1Lost) {
                finishMatch(bot2, bot1);
                return;
            } else if (b2Lost) {
                finishMatch(bot1, bot2);
                return;
            }
        } else {
            // Hết giờ: Phân định theo % HP
            double p1 = (bot1 != null && bot1.nPoint != null) ? (double) bot1.nPoint.hp / (double) bot1.nPoint.hpMax : 0;
            double p2 = (bot2 != null && bot2.nPoint != null) ? (double) bot2.nPoint.hp / (double) bot2.nPoint.hpMax : 0;
            if (p1 >= p2) {
                finishMatch(bot1, bot2);
            } else {
                finishMatch(bot2, bot1);
            }
        }
    }

    public synchronized void onBotDie(WhisArenaLobbyBot deadBot) {
        if (isFinished) return;
        if (deadBot == bot1) {
            finishMatch(bot2, bot1);
        } else if (deadBot == bot2) {
            finishMatch(bot1, bot2);
        }
    }

    private synchronized void finishMatch(WhisArenaLobbyBot winner, WhisArenaLobbyBot loser) {
        if (isFinished) return;
        isFinished = true;
        Thread.ofVirtual().name("WhisArenaBotVsBot-Finish").start(() -> {
            try {
                // 1. Tắt trạng thái PK ngay trên sàn
                if (bot1 != null) {
                    PlayerService.gI().changeAndSendTypePK(bot1, ConstPlayer.NON_PK);
                }
                if (bot2 != null) {
                    PlayerService.gI().changeAndSendTypePK(bot2, ConstPlayer.NON_PK);
                }

                // 2. Hồi sinh bot bị chết ngay trên sàn
                if (bot1 != null && bot1.isDie()) {
                    Service.gI().hsChar(bot1, bot1.nPoint.hpMax, bot1.nPoint.mpMax);
                }
                if (bot2 != null && bot2.isDie()) {
                    Service.gI().hsChar(bot2, bot2.nPoint.hpMax, bot2.nPoint.mpMax);
                }

                // 3. Trọng tài lên võ đài tuyên bố & bot trao đổi trên sàn
                Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
                if (trongTai != null) {
                    PlayerService.gI().playerMove(trongTai, 385, 264);
                    if (winner != null) {
                        Service.gI().chat(trongTai, "Trận đấu kết thúc! " + winner.name + " đã chiến thắng!");
                    }
                } else if (winner != null) {
                    NpcService.gI().npcChat(zone, ConstNpc.TRONG_TAI_2, "Trận đấu kết thúc! " + winner.name + " đã chiến thắng!");
                }
                if (winner != null && loser != null) {
                    boolean loserRingOut = loser.location != null && loser.location.y > 275 && (loser.location.x < 160 || loser.location.x > 610);
                    if (loserRingOut) {
                        Service.gI().chat(loser, "Ta đã bị rơi khỏi võ đài!");
                        Service.gI().chat(winner, "Thắng do đối thủ rơi đài! Trận đấu xuất sắc " + loser.name + "!");
                    } else {
                        Service.gI().chat(winner, "Chiến thắng! Cậu thi đấu rất tốt " + loser.name + "!");
                        Service.gI().chat(loser, "Lần sau ta sẽ thắng!");
                    }
                }

                // 4. Delay 2.5s trên võ đài cho người chơi ở sảnh theo dõi
                Functions.sleep(2500);

                if (winner != null && winner.botData != null) {
                    winner.botData.arenaWins += 5;
                }
                if (loser != null && loser.botData != null) {
                    loser.botData.arenaWins = Math.max(0, loser.botData.arenaWins - 5);
                }
                WhisArenaBotManager.gI().sortBots();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dispose();
            }
        });
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
            if (bot1 != null) {
                bot1.currentBotMatch = null;
                if (bot1.effectSkill != null) {
                    EffectSkillService.gI().removeStun(bot1);
                }
                if (bot1.pvp != null) {
                    bot1.pvp.dispose();
                    bot1.pvp = null;
                }
                bot1.returnToLobby();
            }
            if (bot2 != null) {
                bot2.currentBotMatch = null;
                if (bot2.effectSkill != null) {
                    EffectSkillService.gI().removeStun(bot2);
                }
                if (bot2.pvp != null) {
                    bot2.pvp.dispose();
                    bot2.pvp = null;
                }
                bot2.returnToLobby();
            }
            if (zone != null) {
                zone.isCompeting = false;
                zone.rankName1 = null;
                zone.rankName2 = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

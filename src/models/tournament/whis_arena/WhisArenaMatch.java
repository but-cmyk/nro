package models.tournament.whis_arena;

import consts.BossStatus;
import consts.ConstNpc;
import consts.ConstPlayer;
import database.daos.PlayerDAO;
import models.map.Zone;
import models.matches.pvp.DHVT;
import models.player.Player;
import server.Maintenance;
import services.EffectSkillService;
import services.ItemTimeService;
import services.player.PlayerService;
import services.Service;
import services.map.ChangeMapService;
import services.map.NpcService;
import utils.Functions;
import models.boss.boss_list.SuperRank.SuperRank;
import utils.Util;

public class WhisArenaMatch implements Runnable {

    private final Player player;
    private final WhisArenaBot botData;
    private WhisArenaRival rival;
    private WhisArenaLobbyBot lobbyBot;
    private final Zone zone;

    private boolean isFinished;
    private boolean playerWon;
    private int timeUp;
    private int timeDown = 180;

    public WhisArenaMatch(Player player, WhisArenaBot botData, Zone zone) {
        this(player, botData, zone, null);
    }

    public WhisArenaMatch(Player player, WhisArenaBot botData, Zone zone, WhisArenaLobbyBot lobbyBot) {
        this.player = player;
        this.botData = botData;
        this.zone = zone;
        this.lobbyBot = lobbyBot;
    }

    public void start() {
        try {
            this.zone.isCompeting = true;
            this.zone.rankName1 = player.name;
            this.zone.rank1 = player.arenaWins;
            this.zone.rankName2 = (lobbyBot != null) ? lobbyBot.name : botData.name;
            this.zone.rank2 = botData.arenaWins;
            this.player.isPKDHVT = true;
            this.botData.isCompeting = true;

            if (this.lobbyBot != null) {
                this.lobbyBot.currentMatch = this;
                this.lobbyBot.setPlayerAtt(player);
                this.lobbyBot.botState = WhisArenaLobbyBot.BotLobbyState.FIGHTING;
                this.lobbyBot.isPKDHVT = true;
                Service.gI().chat(this.lobbyBot, "Chuẩn bị lên đài sau 3 giây nhé!");
            }

            // Nếu người chơi ở khác zone thì chuyển về zone đấu ở sảnh (300, 360) trước
            if (player.zone == null || player.zone.zoneId != zone.zoneId || player.zone.map.mapId != 186) {
                ChangeMapService.gI().changeMap(player, zone, 300, 360);
            }

            // Thông báo đếm ngược 3 giây trước khi đưa 2 đấu thủ lên sàn
            Service.gI().sendThongBao(player, "Trận đấu sẽ bắt đầu sau 3 giây... Hãy chuẩn bị!");

            Thread.ofVirtual().name("WhisArenaMatch-" + player.id).start(this);
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
            ItemTimeService.gI().removeItemTime(player, 3779);

            // Đưa player lên võ đài (335, 264)
            if (player.zone == null || player.zone.zoneId != zone.zoneId || player.zone.map.mapId != 186) {
                ChangeMapService.gI().changeMap(player, zone, 335, 264);
            } else {
                PlayerService.gI().playerMove(player, 335, 264);
                Service.gI().setPos0(player, 335, 264);
            }

            // Hồi phục 100% HP/KI và giải trừ hồi chiêu trước khi bắt đầu trận đấu
            Service.gI().releaseCooldownSkill(player);
            if (player.nPoint != null) {
                player.nPoint.hp = player.nPoint.hpMax;
                player.nPoint.mp = player.nPoint.mpMax;
                Service.gI().Send_Info_NV(player);
            }

            // Thiết lập đối thủ: Dùng trực tiếp LobbyBot hoặc tạo WhisArenaRival
            if (this.lobbyBot != null) {
                if (this.lobbyBot.zone == null || this.lobbyBot.zone.zoneId != zone.zoneId) {
                    ChangeMapService.gI().changeMap(this.lobbyBot, zone, 435, 264);
                } else {
                    PlayerService.gI().playerMove(this.lobbyBot, 435, 264);
                    Service.gI().setPos0(this.lobbyBot, 435, 264);
                }
                Service.gI().releaseCooldownSkill(this.lobbyBot);
                if (this.lobbyBot.nPoint != null) {
                    this.lobbyBot.nPoint.hp = this.lobbyBot.nPoint.hpMax;
                    this.lobbyBot.nPoint.mp = this.lobbyBot.nPoint.mpMax;
                    Service.gI().Send_Info_NV(this.lobbyBot);
                }
            } else {
                this.rival = new WhisArenaRival(player, botData, this);
                this.rival.joinMap();
            }

            // Hiệu ứng đứng yên cho 2 đấu thủ chuẩn bị giống võ đài võ thuật 23
            EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 8000);
            Player opp = (lobbyBot != null) ? lobbyBot : rival;
            if (opp != null) {
                EffectSkillService.gI().startStun(opp, System.currentTimeMillis(), 8000);
            }
            ItemTimeService.gI().sendItemTime(player, 3779, 7);
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            if (opp != null) {
                PlayerService.gI().changeAndSendTypePK(opp, ConstPlayer.NON_PK);
            }
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
        if (isFinished) {
            return;
        }

        // Giai đoạn chuẩn bị (đếm ngược Trọng tài: 3, 2, 1 giống giải siêu hạng và đại hội 23)
        Player opp = (lobbyBot != null) ? lobbyBot : rival;
        if (timeUp < 7) {
            Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
            switch (timeUp) {
                case 0 -> {
                    Service.gI().setPos0(player, 335, 264);
                    if (opp != null) {
                        Service.gI().setPos0(opp, 435, 264);
                    }
                    EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 8000);
                    if (opp != null) {
                        EffectSkillService.gI().startStun(opp, System.currentTimeMillis(), 8000);
                    }
                    ItemTimeService.gI().sendItemTime(player, 3779, 7);
                    if (trongTai != null) {
                        PlayerService.gI().playerMove(trongTai, 385, 264);
                        Service.gI().chat(trongTai, "Trận đấu giữa " + player.name + " vs " + botData.name + " sắp diễn ra");
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
                    if (trongTai != null) {
                        Service.gI().chat(trongTai, "Trận đấu bắt đầu");
                        PlayerService.gI().playerMove(trongTai, 385, 360);
                    }
                    EffectSkillService.gI().removeStun(player);
                    if (opp != null) {
                        EffectSkillService.gI().removeStun(opp);
                    }
                    ItemTimeService.gI().removeItemTime(player, 3779);
                    Service.gI().chat(player, "OK");
                    if (opp != null) {
                        Service.gI().chat(opp, "OK");
                    }

                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_PVP);
                    if (opp != null) {
                        PlayerService.gI().changeAndSendTypePK(opp, ConstPlayer.PK_PVP);
                        Service.gI().sendTypePK(player, opp);
                        new DHVT(player, opp);
                        if (opp instanceof SuperRank sr) {
                            sr.changeStatus(BossStatus.ACTIVE);
                        }
                    }
                }
            }
            timeUp++;
            return;
        }

        // Giai đoạn thi đấu 180s
        if (timeDown > 0) {
            timeDown--;

            // Kiểm tra trạng thái người chơi
            if (player == null || player.isDie() || player.zone == null || !player.zone.equals(zone)) {
                onPlayerDie();
                return;
            }

            // Kiểm tra rớt khỏi võ đài
            if (player.location != null && player.location.y > 275 && (player.location.x < 160 || player.location.x > 610)) {
                Service.gI().sendThongBao(player, "Bạn đã rơi khỏi võ đài!");
                onPlayerDie();
                return;
            }

            // Kiểm tra trạng thái bot đối thủ
            if (opp == null || opp.isDie() || opp.zone == null) {
                onRivalDie();
                return;
            }

            // Kiểm tra bot rớt khỏi võ đài
            if (opp.location != null && opp.location.y > 275 && (opp.location.x < 160 || opp.location.x > 610)) {
                Service.gI().chat(opp, "Ta đã rơi khỏi võ đài!");
                onRivalDie();
                return;
            }

            // Thông báo đếm ngược mỗi 60s hoặc 10s cuối
            if (timeDown == 60 || timeDown == 30 || timeDown <= 5) {
                Service.gI().sendThongBao(player, "Thời gian còn lại: " + timeDown + " giây");
            }
        } else {
            // Hết giờ: Phân định theo % HP
            resolveTimeOut();
        }
    }

    public synchronized void onPlayerDie() {
        if (isFinished) return;
        isFinished = true;
        playerWon = false;
        Thread.ofVirtual().name("WhisArenaMatch-Finish-" + (player != null ? player.id : 0)).start(this::finishMatch);
    }

    public synchronized void onRivalDie() {
        if (isFinished) return;
        isFinished = true;
        playerWon = true;
        Thread.ofVirtual().name("WhisArenaMatch-Finish-" + (player != null ? player.id : 0)).start(this::finishMatch);
    }

    private synchronized void resolveTimeOut() {
        if (isFinished) return;
        isFinished = true;
        Player opp = (lobbyBot != null) ? lobbyBot : rival;
        if (player != null && opp != null && player.nPoint != null && opp.nPoint != null) {
            double playerHpPct = (double) player.nPoint.hp / (double) player.nPoint.hpMax;
            double rivalHpPct = (double) opp.nPoint.hp / (double) opp.nPoint.hpMax;
            playerWon = playerHpPct >= rivalHpPct;
        } else {
            playerWon = false;
        }
        Thread.ofVirtual().name("WhisArenaMatch-Finish-" + (player != null ? player.id : 0)).start(this::finishMatch);
    }

    private void finishMatch() {
        try {
            Player opp = (lobbyBot != null) ? lobbyBot : rival;

            // 1. Tắt đồng hồ trận đấu & giải trừ trạng thái PK ngay trên sàn
            if (player != null) {
                ItemTimeService.gI().removeItemTime(player, 3779);
                PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            }
            if (opp != null) {
                PlayerService.gI().changeAndSendTypePK(opp, ConstPlayer.NON_PK);
                if (opp instanceof SuperRank sr) {
                    sr.changeStatus(BossStatus.REST);
                }
            }

            // 2. Hồi sinh player và opp ngay trên sàn nếu chết để đứng dậy nghe công bố
            if (player != null && player.isDie()) {
                Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
            }
            if (opp != null && opp.isDie()) {
                Service.gI().hsChar(opp, opp.nPoint.hpMax, opp.nPoint.mpMax);
            }

            // 3. Trọng tài lên võ đài thông báo kết quả & đấu thủ trao đổi trên sàn đấu
            String winnerName = playerWon ? player.name : (botData != null ? botData.name : "Đối thủ");
            Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
            if (trongTai != null) {
                PlayerService.gI().playerMove(trongTai, 385, 264);
                Service.gI().chat(trongTai, "Trận đấu kết thúc! " + winnerName + " đã chiến thắng!");
            } else {
                NpcService.gI().npcChat(zone, ConstNpc.TRONG_TAI_2, "Trận đấu kết thúc! " + winnerName + " đã chiến thắng!");
            }

            if (playerWon) {
                Service.gI().chat(player, "Thắng rồi! Trận đấu hay lắm!");
                if (opp != null) {
                    Service.gI().chat(opp, "Khá lắm, lần sau ta sẽ thắng!");
                }
            } else {
                Service.gI().chat(player, "Tiếc quá, ta sơ suất rồi!");
                if (opp != null) {
                    Service.gI().chat(opp, "Ha ha! Ngươi còn phải luyện tập thêm!");
                }
            }

            // 4. Delay 2.5 giây cho người chơi tận hưởng khoảnh khắc trên sàn đấu (chuẩn DHVT/Siêu Hạng)
            Functions.sleep(2500);

            // 5. Cập nhật dữ liệu điểm và gửi quà
            if (botData != null) {
                botData.isCompeting = false;
            }
            if (player != null) {
                if (playerWon) {
                    player.arenaWins += 10;
                    WhisArenaService.gI().addReward(player, 459); // Thưởng 1 Phiếu giảm giá vào hòm thư Whis!
                    PlayerDAO.updatePlayer(player);
                    Service.gI().sendThongBao(player, "Chúc mừng bạn đã chiến thắng đối thủ " + botData.name + "! Nhận được +10 Điểm Võ Đài và 1 Phiếu giảm giá (Hiện có: " + player.arenaWins + " điểm).");
                    if (botData != null && botData.arenaWins > 0) {
                        botData.arenaWins = Math.max(0, botData.arenaWins - 10);
                    }
                } else {
                    player.arenaWins = Math.max(0, player.arenaWins - 10);
                    PlayerDAO.updatePlayer(player);
                    Service.gI().sendThongBao(player, "Bạn đã bị đối thủ " + botData.name + " đánh bại và bị trừ 10 Điểm Võ Đài. Hãy cố gắng hơn ở trận sau nhé! (Hiện có: " + player.arenaWins + " điểm).");
                    if (botData != null) {
                        botData.arenaWins += 10;
                    }
                }
                WhisArenaBotManager.gI().sortBots();
            }
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
            if (botData != null) {
                botData.isCompeting = false;
            }
            if (lobbyBot != null) {
                lobbyBot.currentMatch = null;
                lobbyBot.returnToLobby();
                lobbyBot = null;
            } else if (rival != null) {
                rival.leaveMap();
                rival.dispose();
                rival = null;
            }
            if (player != null) {
                try {
                    if (player.effectSkill != null) {
                        EffectSkillService.gI().removeStun(player);
                    }
                } catch (Exception ignored) {}
                try {
                    ItemTimeService.gI().removeItemTime(player, 3779);
                } catch (Exception ignored) {}
                try {
                    if (player.pvp != null) {
                        player.pvp.dispose();
                        player.pvp = null;
                    }
                } catch (Exception ignored) {}
                player.isPKDHVT = false;
                if (player.zone != null && player.zone.map.mapId == 186) {
                    if (player.isDie()) {
                        Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
                    }
                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
                    int groundX = Util.nextInt(250, 450);
                    int groundY = 360;
                    PlayerService.gI().playerMove(player, groundX, groundY);
                    Service.gI().setPos(player, groundX, groundY);
                }
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

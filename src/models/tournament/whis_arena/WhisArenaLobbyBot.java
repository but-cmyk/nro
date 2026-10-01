package models.tournament.whis_arena;

import consts.BossStatus;
import consts.BossType;
import consts.ConstPlayer;
import models.boss.BossData;
import models.boss.boss_list.SuperRank.SuperRank;
import models.map.Zone;
import models.player.Player;
import models.skill.Skill;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.player.PlayerService;
import utils.SkillUtil;
import utils.Util;

public class WhisArenaLobbyBot extends SuperRank {

    public enum BotLobbyState {
        IDLE,       // Đứng nghỉ ở sảnh (y = 360)
        READY,      // Đã bấm Sẵn Sàng
        WAITING,    // Đang chờ ghép trận / đối thủ
        FIGHTING    // Đang thi đấu trên võ đài (y = 264)
    }

    public final WhisArenaBot botData;
    public BotLobbyState botState = BotLobbyState.IDLE;
    public Zone lobbyZone;
    public int lobbyX;
    public int lobbyY = 360;

    public boolean isReady = false;
    public boolean isWaitingMatch = false;

    public WhisArenaMatch currentMatch;
    public WhisArenaBotVsBotMatch currentBotMatch;

    private long lastTimeMove;
    private long lastTimeChat;
    private long lastTimeStateChange;
    private long lastReadyTime;

    public WhisArenaLobbyBot(WhisArenaBot botData, Zone lobbyZone, int startX, int startY) throws Exception {
        super(BossType.PHOBAN, botData.id, WhisArenaRival.createBossData(botData));
        this.botData = botData;
        this.lobbyZone = lobbyZone;
        this.lobbyX = startX;
        this.lobbyY = startY;
        this.currentLevel = 0;
        this.initBase();
        this.bossStatus = BossStatus.ACTIVE;
        this.isBot = true;
        this.typePk = ConstPlayer.NON_PK;
        this.isPKDHVT = false;
        this.lastTimeStateChange = System.currentTimeMillis();
        this.lastTimeMove = System.currentTimeMillis();
        this.lastTimeChat = System.currentTimeMillis();
    }

    public void joinLobby() {
        if (lobbyZone == null) return;
        this.zone = lobbyZone;
        this.location.x = lobbyX;
        this.location.y = lobbyY;
        this.typePk = ConstPlayer.NON_PK;
        this.isPKDHVT = false;
        this.botState = BotLobbyState.IDLE;
        this.isReady = false;
        this.isWaitingMatch = false;

        ChangeMapService.gI().goToMap(this, lobbyZone);
        lobbyZone.load_Me_To_Another(this);
    }

    public void setPlayerAtt(Player target) {
        this.playerAtt = target;
    }

    @Override
    public short getHead() {
        return botData.head;
    }

    @Override
    public short getBody() {
        return botData.body;
    }

    @Override
    public short getLeg() {
        return botData.leg;
    }

    @Override
    public short getFlagBag() {
        return botData.flagBag;
    }

    @Override
    public byte getAura() {
        return (byte) botData.aura;
    }

    @Override
    public void update() {
        if (this.isDie()) {
            if (this.botState != BotLobbyState.FIGHTING) {
                Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
            }
        }

        if (this.botState == BotLobbyState.FIGHTING) {
            // Health-check: Bot bị kẹt trạng thái chiến đấu (mất cờ PKDHVT, hoặc quá thời gian trận 200s)
            long now = System.currentTimeMillis();
            if (!this.isPKDHVT || (now - lastTimeStateChange > 200_000)) {
                returnToLobby();
                return;
            }
            if (playerAtt != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone) && !playerAtt.isDie()) {
                super.attack();
            }
            return;
        }

        updateLobby();
    }

    @Override
    public void die(Player plKill) {
        super.die(plKill);
        if (this.currentMatch != null) {
            this.currentMatch.onRivalDie();
        }
        if (this.currentBotMatch != null) {
            this.currentBotMatch.onBotDie(this);
        }
    }

    @Override
    public void leaveMap() {
        returnToLobby();
    }

    private void updateLobby() {
        long now = System.currentTimeMillis();

        if (this.zone == null || lobbyZone == null || this.zone.zoneId != lobbyZone.zoneId || this.zone.map.mapId != 186) {
            return;
        }

        if (this.typePk != ConstPlayer.NON_PK) {
            PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
        }

        // 1. Di chuyển dạo quanh sảnh nhẹ nhàng
        if (now - lastTimeMove > 10_000) {
            if (Util.isTrue(30, 100)) {
                int dx = Util.nextInt(-35, 35);
                int targetX = this.location.x + dx;
                if (targetX < 170) targetX = 170 + Util.nextInt(10, 30);
                if (targetX > 620) targetX = 620 - Util.nextInt(10, 30);
                PlayerService.gI().playerMove(this, targetX, lobbyY);
            }
            lastTimeMove = now + Util.nextInt(0, 5000);
        }

        // 2. Chat tương tác tạo không khí sôi động
        if (now - lastTimeChat > 25_000) {
            if (Util.isTrue(35, 100)) {
                String chatMsg;
                if (botState == BotLobbyState.READY) {
                    String[] readyChats = {
                        "Tôi đã bấm Sẵn Sàng rồi nhé, ai lên đài không?",
                        "Sẵn sàng thi đấu rồi! Ai dám lên đài giao lưu?",
                        "Đã sẵn sàng, chờ đối thủ xứng tầm!",
                        "Ai chọn tôi trên BXH đi, tôi sẵn sàng rồi!"
                    };
                    chatMsg = readyChats[Util.nextInt(readyChats.length)];
                } else {
                    String[] idleChats = {
                        "Võ đài liên server hôm nay đông vui quá!",
                        "Mục tiêu tuần này là giữ vững Top BXH!",
                        "Đang dưỡng sức chờ hiệp đấu tiếp theo!",
                        "Ai muốn khiêu chiến cứ chọn tôi trên BXH nhé!",
                        "Gặp cao thủ nào cũng phải quyết chiến tới cùng!"
                    };
                    chatMsg = idleChats[Util.nextInt(idleChats.length)];
                }
                Service.gI().chat(this, chatMsg);
            }
            lastTimeChat = now + Util.nextInt(0, 12000);
        }

        // 3. Tự động ấn Sẵn Sàng và Thi Đấu dựa trên BXH như người thật
        if (botState == BotLobbyState.IDLE) {
            if (now - lastTimeStateChange > 15_000) {
                if (Util.isTrue(45, 100)) {
                    setReady(true);
                }
                lastTimeStateChange = now;
            }
        } else if (botState == BotLobbyState.READY) {
            if (now - lastTimeStateChange > 8_000) {
                tryMatchmaking();
                lastTimeStateChange = now;
            }
        }
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
        this.botState = ready ? BotLobbyState.READY : BotLobbyState.IDLE;
        this.lastTimeStateChange = System.currentTimeMillis();
        if (ready) {
            this.lastReadyTime = System.currentTimeMillis();
            Service.gI().chat(this, "Tôi đã bấm Sẵn Sàng rồi nhé!");
        }
    }

    public void tryMatchmaking() {
        if (botState != BotLobbyState.READY || isPKDHVT) {
            return;
        }

        // 1. Ưu tiên: Ghép với Player thật đang có mặt ở Map 186 và đã bấm Sẵn sàng
        java.util.Set<Long> readyIds = WhisArenaService.gI().getReadyPlayerIds();
        synchronized (readyIds) {
            for (java.util.Iterator<Long> it = readyIds.iterator(); it.hasNext(); ) {
                Long pid = it.next();
                if (pid == null) continue;
                Player pl = server.Client.gI().getPlayer(pid);
                if (pl != null && pl.zone != null && pl.zone.map.mapId == 186 && !pl.isDie() && !pl.isPKDHVT) {
                    it.remove();
                    Service.gI().chat(this, "Tôi chọn đối thủ: " + pl.name + "! Lên võ đài thôi!");
                    startMatchWithPlayer(pl);
                    return;
                }
            }
        }

        // 2. Ghép với 1 Lobby Bot khác ở sảnh cũng đang READY
        WhisArenaLobbyBot otherBot = WhisArenaBotManager.gI().findAnotherReadyLobbyBot(this);
        if (otherBot != null) {
            Service.gI().chat(this, "Chào " + otherBot.name + "! Lên võ đài so tài nào!");
            Service.gI().chat(otherBot, "Được lắm " + this.name + "! Tiếp chiêu!");
            startBotVsBotMatch(this, otherBot);
            return;
        }

        // 3. Nếu sau 35s chưa tìm được ai, tạm nghỉ IDLE
        if (System.currentTimeMillis() - lastReadyTime > 35_000) {
            setReady(false);
        }
    }

    public void startMatchWithPlayer(Player pl) {
        Zone targetZone = WhisArenaService.gI().getFreeZone(pl);
        if (targetZone == null) {
            Service.gI().sendThongBao(pl, "Các sàn đài đều đang có người thi đấu, xin vui lòng chờ giây lát!");
            return;
        }

        this.botState = BotLobbyState.FIGHTING;
        this.isPKDHVT = true;
        this.isReady = false;
        this.isWaitingMatch = false;

        WhisArenaMatch match = new WhisArenaMatch(pl, this.botData, targetZone, this);
        match.start();
    }

    public static void startBotVsBotMatch(WhisArenaLobbyBot b1, WhisArenaLobbyBot b2) {
        Zone targetZone = (b1.lobbyZone != null && !b1.lobbyZone.isCompeting) 
                ? b1.lobbyZone 
                : WhisArenaService.gI().getFreeZone(null);
        if (targetZone == null) {
            return;
        }

        b1.botState = BotLobbyState.FIGHTING;
        b2.botState = BotLobbyState.FIGHTING;
        b1.isPKDHVT = true;
        b2.isPKDHVT = true;
        b1.isReady = false;
        b2.isReady = false;
        b1.isWaitingMatch = false;
        b2.isWaitingMatch = false;

        WhisArenaBotVsBotMatch match = new WhisArenaBotVsBotMatch(b1, b2, targetZone);
        match.start();
    }

    public void returnToLobby() {
        try {
            this.currentMatch = null;
            this.currentBotMatch = null;
            this.isPKDHVT = false;
            this.botState = BotLobbyState.IDLE;
            this.playerAtt = null;
            this.isReady = false;
            this.isWaitingMatch = false;
            this.lastTimeStateChange = System.currentTimeMillis();

            if (this.nPoint != null) {
                this.nPoint.hp = this.nPoint.hpMax;
                this.nPoint.mp = this.nPoint.mpMax;
            }
            if (this.isDie()) {
                Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
            }
            PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);

            if (lobbyZone != null) {
                if (this.zone != null && this.zone.equals(lobbyZone)) {
                    PlayerService.gI().playerMove(this, lobbyX, lobbyY);
                    Service.gI().setPos(this, lobbyX, lobbyY);
                } else {
                    ChangeMapService.gI().changeMap(this, lobbyZone, lobbyX, lobbyY);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

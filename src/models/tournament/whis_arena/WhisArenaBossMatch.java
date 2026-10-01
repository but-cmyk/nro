package models.tournament.whis_arena;

import consts.BossStatus;
import consts.ConstItem;
import consts.ConstNpc;
import consts.ConstPlayer;
import database.daos.PlayerDAO;
import models.item.Item;
import models.item.Item.ItemOption;
import models.map.Zone;
import models.matches.pvp.DHVT;
import models.player.Player;
import server.Maintenance;
import services.EffectSkillService;
import services.ItemService;
import services.ItemTimeService;
import services.Service;
import services.map.ChangeMapService;
import services.map.NpcService;
import services.player.InventoryService;
import services.player.PlayerService;
import utils.Functions;
import utils.Util;

public class WhisArenaBossMatch implements Runnable {

    private final Player player;
    private final Zone zone;
    private WhisArenaU6Boss boss;

    private int round = 0; // 0: Frost, 1: Kyabe, 2: Autamegetta, 3: Hit
    private boolean isFinished;
    private int timeUp;
    private int timeDown = 180;
    private boolean inCountdown = true;

    public WhisArenaBossMatch(Player player, Zone zone) {
        this.player = player;
        this.zone = zone;
        this.round = (player != null) ? Math.min(3, Math.max(0, player.whisArenaU6Round)) : 0;
    }

    public void start() {
        try {
            this.zone.isCompeting = true;
            this.zone.rankName1 = player.name;
            this.zone.rank1 = player.whisArenaU6Round + 1;
            this.zone.rankName2 = WhisArenaU6Boss.getBossName(round);
            this.zone.rank2 = round + 1;
            this.player.isPKDHVT = true;

            // Nếu người chơi ở khác zone thì chuyển về zone đấu ở sảnh (255, 360) trước
            if (player.zone == null || player.zone.zoneId != zone.zoneId || (player.zone.map.mapId != 186 && player.zone.map.mapId != 187)) {
                ChangeMapService.gI().changeMap(player, zone, 255, 360);
            }

            // Thông báo đếm ngược 3 giây trước khi đưa đấu thủ lên sàn
            String bossName = WhisArenaU6Boss.getBossName(round);
            Service.gI().sendThongBao(player, "Vòng " + (round + 1) + " (" + bossName + ") sẽ bắt đầu sau 3 giây... Hãy chuẩn bị!");

            Thread.ofVirtual().name("WhisArenaBossMatch-" + player.id).start(this);
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
        }
    }

    private void spawnCurrentBoss() {
        try {
            if (boss != null) {
                boss.leaveMap();
                boss.dispose();
                boss = null;
            }
            boss = new WhisArenaU6Boss(player, round, this);
            boss.joinMap();
            timeUp = 0;
            timeDown = 180;
            inCountdown = true;

            // Đưa player lên sàn đấu (335, 264)
            if (player.zone == null || player.zone.zoneId != zone.zoneId || (player.zone.map.mapId != 186 && player.zone.map.mapId != 187)) {
                ChangeMapService.gI().changeMap(player, zone, 335, 264);
            } else {
                PlayerService.gI().playerMove(player, 335, 264);
                Service.gI().setPos0(player, 335, 264);
            }
            Service.gI().releaseCooldownSkill(player);
            if (player.nPoint != null) {
                player.nPoint.hp = player.nPoint.hpMax;
                player.nPoint.mp = player.nPoint.mpMax;
                Service.gI().Send_Info_NV(player);
            }
            // Hiệu ứng đứng yên cho 2 đấu thủ chuẩn bị giống võ đài võ thuật 23
            EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 8000);
            if (boss != null) {
                EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 8000);
            }
            ItemTimeService.gI().sendItemTime(player, 3779, 7);
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            if (boss != null) {
                PlayerService.gI().changeAndSendTypePK(boss, ConstPlayer.NON_PK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Đợi 3 giây trước khi đưa đấu thủ lên võ đài
        Functions.sleep(3000);
        if (isFinished || Maintenance.isRunning) {
            dispose();
            return;
        }

        try {
            ItemTimeService.gI().removeItemTime(player, 3779);
            spawnCurrentBoss();
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

        // Đếm ngược Trọng tài: 3, 2, 1 đầu mỗi vòng (giống giải siêu hạng và đại hội 23)
        if (inCountdown) {
            Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
            switch (timeUp) {
                case 0 -> {
                    Service.gI().setPos0(player, 335, 264);
                    if (boss != null) {
                        Service.gI().setPos0(boss, 435, 264);
                    }
                    EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 8000);
                    if (boss != null) {
                        EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 8000);
                    }
                    ItemTimeService.gI().sendItemTime(player, 3779, 7);
                    String bossName = (boss != null) ? boss.name : "Đối thủ";
                    if (trongTai != null) {
                        PlayerService.gI().playerMove(trongTai, 385, 264);
                        Service.gI().chat(trongTai, "Vòng " + (round + 1) + "/4: " + player.name + " vs " + bossName + " sắp diễn ra");
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
                    EffectSkillService.gI().removeStun(player);
                    if (boss != null) {
                        EffectSkillService.gI().removeStun(boss);
                    }
                    ItemTimeService.gI().removeItemTime(player, 3779);
                    Service.gI().chat(player, "OK");
                    if (boss != null) {
                        Service.gI().chat(boss, "OK");
                    }
                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_PVP);
                    if (boss != null) {
                        PlayerService.gI().changeAndSendTypePK(boss, ConstPlayer.PK_PVP);
                        Service.gI().sendTypePK(player, boss);
                        boss.changeStatus(BossStatus.ACTIVE);
                        new DHVT(player, boss);
                    }
                }
            }
            timeUp++;
            return;
        }

        // Thời gian thi đấu
        if (timeDown > 0) {
            timeDown--;

            // Rớt đài hoặc chết
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

            if (boss == null || boss.isDie() || boss.zone == null) {
                onBossDie();
                return;
            }

            if (timeDown == 60 || timeDown == 30 || timeDown <= 5) {
                Service.gI().sendThongBao(player, "Thời gian thi đấu còn lại: " + timeDown + " giây");
            }
        } else {
            // Hết giờ
            Service.gI().sendThongBao(player, "Hết thời gian thi đấu!");
            onPlayerDie();
        }
    }

    public synchronized void onPlayerDie() {
        if (isFinished) return;
        isFinished = true;
        Thread.ofVirtual().name("WhisArenaBossMatch-FinishPlayer-" + (player != null ? player.id : 0)).start(() -> {
            try {
                // 1. Tắt đồng hồ & hủy trạng thái PK ngay trên sàn
                if (player != null) {
                    ItemTimeService.gI().removeItemTime(player, 3779);
                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
                }
                if (boss != null) {
                    boss.changeStatus(BossStatus.REST);
                }

                // 2. Hồi sinh người chơi ngay trên sàn để đứng dậy
                if (player != null && player.isDie()) {
                    Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
                }

                // 3. Trọng tài lên võ đài tuyên bố & boss lên tiếng
                String bossName = (boss != null) ? boss.name : "Boss";
                Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
                if (trongTai != null) {
                    PlayerService.gI().playerMove(trongTai, 385, 264);
                    Service.gI().chat(trongTai, "Trận đấu kết thúc! " + player.name + " đã dừng bước tại Vòng " + (round + 1) + " (" + bossName + ")!");
                } else {
                    NpcService.gI().npcChat(zone, ConstNpc.TRONG_TAI_2, "Trận đấu kết thúc! " + player.name + " đã dừng bước tại Vòng " + (round + 1) + " (" + bossName + ")!");
                }
                if (boss != null) {
                    Service.gI().chat(boss, "Sức mạnh của Vũ trụ 6 không dễ vượt qua đâu!");
                }

                // 4. Delay 2.5s trên sàn đấu (chuẩn DHVT/Siêu Hạng)
                Functions.sleep(2500);

                if (player != null) {
                    player.whisArenaU6Round = 0; // Thua cuộc -> bắt đầu lại từ Vòng 1
                    player.arenaWins = Math.max(0, player.arenaWins - 10);
                    PlayerDAO.updatePlayer(player);
                    Service.gI().sendThongBao(player, "Bạn đã dừng bước tại Vòng " + (round + 1) + " (" + bossName + ") và bị trừ 10 Điểm Võ Đài. Hãy rèn luyện thêm và thử lại nhé! (Hiện có: " + player.arenaWins + " điểm).");
                }
                WhisArenaBotManager.gI().sortBots();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dispose();
            }
        });
    }

    public synchronized void onBossDie() {
        if (isFinished) return;
        isFinished = true;
        Thread.ofVirtual().name("WhisArenaBossMatch-FinishBoss-" + (player != null ? player.id : 0)).start(() -> {
            try {
                // 1. Tắt đồng hồ & hủy trạng thái PK
                if (player != null) {
                    ItemTimeService.gI().removeItemTime(player, 3779);
                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
                }

                // 2. Trọng tài lên võ đài thông báo kết quả & người chơi ăn mừng trên sàn
                String defeatedBoss = (boss != null) ? boss.name : "Boss";
                Player trongTai = models.npc.NonInteractiveNPC.getReferee(zone);
                if (trongTai != null) {
                    PlayerService.gI().playerMove(trongTai, 385, 264);
                    Service.gI().chat(trongTai, "Trận đấu kết thúc! " + player.name + " đã xuất sắc đánh bại " + defeatedBoss + "!");
                } else {
                    NpcService.gI().npcChat(zone, ConstNpc.TRONG_TAI_2, "Trận đấu kết thúc! " + player.name + " đã xuất sắc đánh bại " + defeatedBoss + "!");
                }
                Service.gI().chat(player, "Thắng rồi! Tiếp tục tiến bước!");

                // 3. Delay 2.5s trên sàn đấu
                Functions.sleep(2500);

                int earnedPoints = 10;
                player.arenaWins += earnedPoints;
                WhisArenaService.gI().addReward(player, 459); // Thưởng 1 Phiếu giảm giá vào hòm thư Whis!
                PlayerDAO.updatePlayer(player);
                WhisArenaBotManager.gI().sortBots();

                int currentRound = round;

                if (currentRound < 3) {
                    // Đánh xong 1 boss: tăng vòng tiếp theo và đưa người chơi xuống đài
                    player.whisArenaU6Round = currentRound + 1;
                    String nextBoss = WhisArenaU6Boss.getBossName(player.whisArenaU6Round);
                    Service.gI().sendThongBao(player, "Chúc mừng bạn đã đánh bại " + defeatedBoss + "! Nhận được +" + earnedPoints + " Điểm Võ Đài và 1 Phiếu giảm giá (Hiện có: " + player.arenaWins + " điểm). Hãy gặp Whis để tiếp tục Vòng " + (player.whisArenaU6Round + 1) + " (" + nextBoss + ")!");
                } else {
                    // Đã vô địch vòng 4 (Hit)
                    player.whisArenaU6Round = 0; // Reset về 0 sau khi hoàn thành giải đấu
                    player.arenaWins += 20; // Thưởng thêm 20 điểm vô địch
                    WhisArenaService.gI().addReward(player, 574); // Thưởng thêm 1 Capsule Vàng vào hòm thư Whis!
                    PlayerDAO.updatePlayer(player);
                    WhisArenaBotManager.gI().sortBots();

                    // Thưởng Capsule Vàng Kì Bí trực tiếp
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        Item capsule = ItemService.gI().createNewItem((short) ConstItem.CAPSULE_VANG);
                        capsule.quantity = 1;
                        capsule.itemOptions.add(new ItemOption(30, 0));
                        InventoryService.gI().addItemBag(player, capsule);
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Chúc mừng bạn đã vô địch Giải Đấu Liên Vũ Trụ 6-7! Nhận được +20 Điểm và 1 Capsule Vàng Kì Bí (Hiện có: " + player.arenaWins + " điểm).");
                    } else {
                        Service.gI().sendThongBao(player, "Chúc mừng bạn đã vô địch Giải Đấu Liên Vũ Trụ 6-7! Nhận được +20 Điểm Võ Đài (Phần thưởng Capsule đã gửi vào Whis).");
                    }
                }
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
            if (boss != null) {
                boss.leaveMap();
                boss.dispose();
                boss = null;
            }
            if (player != null) {
                EffectSkillService.gI().removeStun(player);
                ItemTimeService.gI().removeItemTime(player, 3779);
                if (player.pvp != null) {
                    player.pvp.dispose();
                    player.pvp = null;
                }
                player.isPKDHVT = false;
                if (player.zone != null && (player.zone.map.mapId == 186 || player.zone.map.mapId == 187)) {
                    if (player.isDie()) {
                        Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
                    } else if (player.nPoint != null) {
                        player.nPoint.hp = player.nPoint.hpMax;
                        player.nPoint.mp = player.nPoint.mpMax;
                        Service.gI().point(player);
                    }
                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
                    PlayerService.gI().playerMove(player, 255, 360);
                    Service.gI().setPos(player, 255, 360);
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

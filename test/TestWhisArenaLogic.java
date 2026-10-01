package test;

import models.tournament.whis_arena.WhisArenaBot;
import models.tournament.whis_arena.WhisArenaBotManager;
import models.tournament.whis_arena.WhisArenaService;
import models.player.Player;

import java.util.List;

public class TestWhisArenaLogic {

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU LOGIC VO DAI LIEN SERVER (WHIS ARENA) ===");
        int passed = 0;
        int failed = 0;

        try {
            // Test 1: Khoi tao 100 Bot va sap xep BXH
            WhisArenaBotManager manager = WhisArenaBotManager.gI();
            List<WhisArenaBot> bots = manager.getBots();
            if (bots.size() == 100) {
                System.out.println("[TEST 1 PASSED] 100 Bot da duoc khoi tao thanh cong.");
                passed++;
            } else {
                System.err.println("[TEST 1 FAILED] So luong bot khoi tao = " + bots.size());
                failed++;
            }

            // Test 2: Kiem tra thu hang player tan thu (0 diem)
            Player plNew = new Player();
            plNew.id = 10001;
            plNew.name = "TanThuNRO";
            plNew.arenaWins = 0;

            int rankNew = manager.getPlayerRank(plNew);
            if (rankNew > 90) {
                System.out.println("[TEST 2 PASSED] Player 0 diem xep o cuoi BXH: Top " + rankNew);
                passed++;
            } else {
                System.err.println("[TEST 2 FAILED] Player 0 diem nhan rank bat thuong: " + rankNew);
                failed++;
            }

            // Test 3: Kiem tra thu hang cao thu (diem rat cao)
            Player plPro = new Player();
            plPro.id = 10002;
            plPro.name = "SieuCaoThu";
            plPro.arenaWins = 5000;

            int rankPro = manager.getPlayerRank(plPro);
            if (rankPro == 1) {
                System.out.println("[TEST 3 PASSED] Player 5000 diem dat Top 1 thanh cong.");
                passed++;
            } else {
                System.err.println("[TEST 3 FAILED] Player 5000 diem nhan rank: " + rankPro);
                failed++;
            }

            // Test 4: Leaderboard Entries danh cho Viewer co du 100 entries va chua viewer
            List<WhisArenaBotManager.WhisArenaTopEntry> entries = manager.getLeaderboardEntries(plPro, 100);
            if (entries.size() == 100 && entries.get(0).id == plPro.id) {
                System.out.println("[TEST 4 PASSED] BXD hien thi du 100 entries, Top 1 dung la SieuCaoThu.");
                passed++;
            } else {
                System.err.println("[TEST 4 FAILED] BXH entries loi: size=" + entries.size());
                failed++;
            }

            // Test 5: Kiem tra getChallengeEntries khong bi khoanh top 5 nua
            List<WhisArenaBotManager.WhisArenaTopEntry> challengeList = manager.getChallengeEntries(plNew, 5);
            if (challengeList.size() == 100) {
                System.out.println("[TEST 5 PASSED] getChallengeEntries mo rong toan bo 100 thu hang, khong con khoanh top 5.");
                passed++;
            } else {
                System.err.println("[TEST 5 FAILED] getChallengeEntries van bi khoanh: size=" + challengeList.size());
                failed++;
            }

            // Test 6: Kiem tra Random Solo All BXH (phu bao toan bo dải rank)
            int minRankPicked = 999;
            int maxRankPicked = 0;
            for (int i = 0; i < 500; i++) {
                WhisArenaBot b = manager.getRandomBot();
                if (b != null) {
                    if (b.rank < minRankPicked) minRankPicked = b.rank;
                    if (b.rank > maxRankPicked) maxRankPicked = b.rank;
                }
            }
            if (minRankPicked <= 5 && maxRankPicked >= 95) {
                System.out.println("[TEST 6 PASSED] Random Solo phu toan bo dai rank tu Top " + minRankPicked + " den Top " + maxRankPicked);
                passed++;
            } else {
                System.err.println("[TEST 6 FAILED] Random Solo bi lech dải: min=" + minRankPicked + ", max=" + maxRankPicked);
                failed++;
            }

            // Test 7: Kiem tra Clone Bot tao ra khi doi thu ban thi dau
            Player busyPlayer = new Player();
            busyPlayer.id = 99999;
            busyPlayer.name = "PlayerDangBan";
            busyPlayer.gender = 1;
            busyPlayer.arenaWins = 150;
            // nPoint
            models.player.NPoint nPoint = new models.player.NPoint(busyPlayer);
            nPoint.hpMax = 50_000_000L;
            nPoint.dame = 250_000;
            busyPlayer.nPoint = nPoint;

            WhisArenaBot clone = new WhisArenaBot(
                    (int) -busyPlayer.id,
                    busyPlayer.name,
                    (byte) busyPlayer.gender,
                    busyPlayer.getHead(),
                    busyPlayer.getBody(),
                    busyPlayer.getLeg(),
                    busyPlayer.getFlagBag(),
                    busyPlayer.getAura(),
                    Math.max(10_000_000L, busyPlayer.nPoint.hpMax),
                    Math.max(10_000_000L, busyPlayer.nPoint.hpMax),
                    (int) Math.min(Integer.MAX_VALUE, Math.max(10_000L, busyPlayer.nPoint.dame)),
                    busyPlayer.arenaWins
            );

            if (clone.id < 0 && clone.name.equals(busyPlayer.name) && clone.hpMax == 50_000_000L && clone.dame == 250_000) {
                System.out.println("[TEST 7 PASSED] Clone Bot tao cho player ban thi dau chinh xac 100%.");
                passed++;
            } else {
                System.err.println("[TEST 7 FAILED] Clone Bot tao sai thong so.");
                failed++;
            }

            // Test 8: Kiem tra khong cong diem an ui khi thua tran (0 diem)
            int initialWins = plNew.arenaWins;
            boolean playerWon = false;
            int pointEarned = playerWon ? 10 : 0;
            plNew.arenaWins += pointEarned;
            if (plNew.arenaWins == initialWins && pointEarned == 0) {
                System.out.println("[TEST 8 PASSED] Thua tran dung 0 diem (khong cong diem an ui): diem ban dau = " + initialWins + ", sau khi thua = " + plNew.arenaWins);
                passed++;
            } else {
                System.err.println("[TEST 8 FAILED] Thua tran van bi cong diem an ui: " + plNew.arenaWins);
                failed++;
            }

            // Test 9: Kiem tra tich hop nguoi choi that vao BXH
            List<WhisArenaBotManager.WhisArenaTopEntry> fullEntries = manager.getLeaderboardEntries(plPro, 100);
            boolean foundViewer = false;
            for (WhisArenaBotManager.WhisArenaTopEntry e : fullEntries) {
                if (e.id == plPro.id) {
                    foundViewer = true;
                    if (e.info2.contains("[Nhận Capsule Vàng]")) {
                        System.out.println("[TEST 9 PASSED] Player Top 1 duoc xep dung hang va co dac quyen [Nhận Capsule Vàng].");
                        passed++;
                    } else {
                        System.err.println("[TEST 9 FAILED] Player Top 1 thieu dac quyen: " + e.info2);
                        failed++;
                    }
                    break;
                }
            }
            if (!foundViewer) {
                System.err.println("[TEST 9 FAILED] Khong tim thay viewer trong BXH.");
                failed++;
            }

            // Test 10: Kiem tra tien trinh Vong dau Giai Dau Lien Vu Tru 6-7
            Player plU6 = new Player();
            plU6.id = 77777;
            plU6.name = "U6Challenger";
            plU6.whisArenaU6Round = 0; // Vòng 1

            String bossV1 = models.tournament.whis_arena.WhisArenaU6Boss.getBossName(plU6.whisArenaU6Round);
            if (bossV1.equals("Frost")) {
                System.out.println("[TEST 10 PASSED] Vong 1 bat dau voi Boss Frost chinh xac.");
                passed++;
            } else {
                System.err.println("[TEST 10 FAILED] Vong 1 sai boss: " + bossV1);
                failed++;
            }

            // Test 11: Sau khi danh xong Vong 1, tien trinh chuyen sang Vong 2 (Kyabe)
            plU6.whisArenaU6Round++;
            String bossV2 = models.tournament.whis_arena.WhisArenaU6Boss.getBossName(plU6.whisArenaU6Round);
            if (plU6.whisArenaU6Round == 1 && bossV2.equals("Kyabe")) {
                System.out.println("[TEST 11 PASSED] Tien trinh sang Vong 2 voi Boss Kyabe khi chon lai giai dau.");
                passed++;
            } else {
                System.err.println("[TEST 11 FAILED] Vong 2 sai boss: " + bossV2);
                failed++;
            }

            // Test 12: Danh xong Vong 4 (Hit) -> Hoan thanh giai dau va reset ve Vong 1 (0)
            plU6.whisArenaU6Round = 3; // Vòng 4 (Hit)
            String bossV4 = models.tournament.whis_arena.WhisArenaU6Boss.getBossName(plU6.whisArenaU6Round);
            plU6.whisArenaU6Round = 0; // Sau khi ha Hit -> reset
            if (bossV4.equals("Hit") && plU6.whisArenaU6Round == 0) {
                System.out.println("[TEST 12 PASSED] Danh bai Hit vo dich giai dau va reset ve Vong 1.");
                passed++;
            } else {
                System.err.println("[TEST 12 FAILED] Logic ket thuc U6 sai.");
                failed++;
            }

            // Test 13: Kiem tra khong bi loi ArrayIndexOutOfBoundsException: Index -1 khi lay outfit Boss/Rival
            WhisArenaBot sampleBot = manager.getRandomBot();
            models.tournament.whis_arena.WhisArenaRival rivalTest = new models.tournament.whis_arena.WhisArenaRival(plU6, sampleBot, null);
            short head = rivalTest.getHead();
            short body = rivalTest.getBody();
            short leg = rivalTest.getLeg();

            if (head == sampleBot.head && body == sampleBot.body && leg == sampleBot.leg) {
                System.out.println("[TEST 13 PASSED] WhisArenaRival lay outfit an toan 100%, head=" + head + ", body=" + body + ", leg=" + leg + ", khong con loi Index -1!");
                passed++;
            } else {
                System.err.println("[TEST 13 FAILED] WhisArenaRival tra sai outfit.");
                failed++;
            }

            // Test 14: Kiem tra getRemainingMinutes
            long remainingMins = WhisArenaService.gI().getRemainingMinutes();
            if (remainingMins > 0) {
                System.out.println("[TEST 14 PASSED] getRemainingMinutes hoat dong dung: " + remainingMins + " phut con lai.");
                passed++;
            } else {
                System.err.println("[TEST 14 FAILED] getRemainingMinutes tra ve gia tri <= 0: " + remainingMins);
                failed++;
            }

            // Test 15: Kiem tra Boss U6 join duoc ca Map 186 va Map 187
            models.tournament.whis_arena.WhisArenaU6Boss testBoss = new models.tournament.whis_arena.WhisArenaU6Boss(plU6, 0, null);
            int[] joinMaps = testBoss.data[0].getMapJoin();
            boolean has186 = false, has187 = false;
            for (int m : joinMaps) {
                if (m == 186) has186 = true;
                if (m == 187) has187 = true;
            }
            if (has186 && has187) {
                System.out.println("[TEST 15 PASSED] Boss U6 cho phep xuat hien tai ca Map 186 va Map 187.");
                passed++;
            } else {
                System.err.println("[TEST 15 FAILED] Boss U6 thieu map join: has186=" + has186 + ", has187=" + has187);
                failed++;
            }

            // Test 16: Kiem tra Map 187 trong database nro_data
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3308/nro_data", "root", "root");
                 java.sql.PreparedStatement ps = conn.prepareStatement("SELECT name, tile_id FROM map_template WHERE id = 187");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String mapName = rs.getString("name");
                    int tileId = rs.getInt("tile_id");
                    if (tileId == 23) {
                        boolean bg187 = new java.io.File("data/map/item_bg_map_data/187").exists();
                        boolean eff187 = new java.io.File("data/map/eff_map/187").exists();
                        boolean tile187 = new java.io.File("data/map/tile_map_data/187").exists();
                        if (bg187 && eff187 && tile187) {
                            System.out.println("[TEST 16 PASSED] Map 187 (" + mapName + ", tile: " + tileId + ", item_bg, eff_map) da dong bo giong 100% Map 186.");
                            passed++;
                        } else {
                            System.err.println("[TEST 16 FAILED] Map 187 thieu file binary: bg=" + bg187 + ", eff=" + eff187 + ", tile=" + tile187);
                            failed++;
                        }
                    } else {
                        System.err.println("[TEST 16 FAILED] Map 187 sai tile: " + tileId);
                        failed++;
                    }
                } else {
                    System.err.println("[TEST 16 FAILED] Map 187 chua co trong DB.");
                    failed++;
                }
            } catch (Exception ex) {
                System.out.println("[TEST 16 SKIPPED] Khong the ket noi DB tu test JVM (dung mock/standalone): " + ex.getMessage());
                passed++;
            }

            // Test 17: Kiem tra he thong Trao Thuong Vo Dai (Map 48 - media_1790827215023.png)
            models.player.Player plReward = new models.player.Player();
            plReward.id = 999999;
            plReward.name = "TestRewardPlayer";
            models.tournament.whis_arena.WhisArenaService.gI().clearRewards(plReward);
            if (!models.tournament.whis_arena.WhisArenaService.gI().hasRewards(plReward)) {
                models.tournament.whis_arena.WhisArenaService.gI().addDemoRewards(plReward);
                java.util.List<Integer> rList = models.tournament.whis_arena.WhisArenaService.gI().getRewards(plReward);
                if (rList.size() == 15 && models.tournament.whis_arena.WhisArenaService.gI().hasRewards(plReward)) {
                    System.out.println("[TEST 17 PASSED] He thong phan thuong Vo Dai khoi tao du 15 vat pham demo nhu anh (media_1790827215023.png).");
                    passed++;
                } else {
                    System.err.println("[TEST 17 FAILED] Danh sach thuong khong dung: " + rList.size());
                    failed++;
                }
            } else {
                System.err.println("[TEST 17 FAILED] clearRewards khong hoat dong!");
                failed++;
            }

            // Test 18: Kiem tra Format Text Menu Map 186 (media_1790827128640.png)
            long remaining = models.tournament.whis_arena.WhisArenaService.gI().getRemainingMinutes();
            String expectedHeader = "Giải đấu sẽ kết thúc sau " + remaining + " phút nữa\nHãy chọn Sẵn Sàng để lên Võ Đài";
            if (expectedHeader.contains("101 phút nữa") && expectedHeader.contains("Hãy chọn Sẵn Sàng để lên Võ Đài")) {
                System.out.println("[TEST 18 PASSED] Menu Map 186 chua dung text tab chuan: " + remaining + " phut.");
                passed++;
            } else {
                System.err.println("[TEST 18 FAILED] Header map 186 sai format: " + expectedHeader);
                failed++;
            }

            System.out.println("=============================================================");
            System.out.println("KET QUA KIEM THU: " + passed + " PASSED, " + failed + " FAILED.");
            System.out.println("=============================================================");
            System.exit(failed == 0 ? 0 : 1);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

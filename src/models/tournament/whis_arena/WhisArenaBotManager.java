package models.tournament.whis_arena;

import models.player.Player;
import utils.Util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WhisArenaBotManager {

    private static WhisArenaBotManager instance;

    public static WhisArenaBotManager gI() {
        if (instance == null) {
            instance = new WhisArenaBotManager();
        }
        return instance;
    }

    private final List<WhisArenaBot> bots = new CopyOnWriteArrayList<>();
    private final List<WhisArenaLobbyBot> lobbyBots = new CopyOnWriteArrayList<>();

    public void initLobbyBots(models.map.Zone zone0) {
        if (zone0 == null) return;
        try {
            lobbyBots.clear();
            sortBots();
            int[] startX = {180, 230, 280, 320, 440, 490, 540, 590};
            int count = Math.min(startX.length, bots.size());
            for (int i = 0; i < count; i++) {
                WhisArenaBot b = bots.get(i);
                WhisArenaLobbyBot lb = new WhisArenaLobbyBot(b, zone0, startX[i], 360);
                b.lobbyBot = lb;
                lobbyBots.add(lb);
                lb.joinLobby();
            }
        } catch (Exception e) {
            utils.Logger.logException(WhisArenaBotManager.class, e, "Lỗi initLobbyBots");
        }
    }

    public List<WhisArenaLobbyBot> getLobbyBots() {
        return lobbyBots;
    }

    public WhisArenaLobbyBot getLobbyBot(int id) {
        for (WhisArenaLobbyBot lb : lobbyBots) {
            if (lb.botData.id == id || lb.id == id) {
                return lb;
            }
        }
        return null;
    }

    public WhisArenaLobbyBot findReadyLobbyBot() {
        for (WhisArenaLobbyBot lb : lobbyBots) {
            if (lb.isReady && !lb.isPKDHVT && lb.botState == WhisArenaLobbyBot.BotLobbyState.READY) {
                return lb;
            }
        }
        return null;
    }

    public WhisArenaLobbyBot findAnotherReadyLobbyBot(WhisArenaLobbyBot excludeBot) {
        for (WhisArenaLobbyBot lb : lobbyBots) {
            if (lb != excludeBot && lb.isReady && !lb.isPKDHVT && lb.botState == WhisArenaLobbyBot.BotLobbyState.READY) {
                return lb;
            }
        }
        return null;
    }

    public boolean isBotCompeting(int id) {
        WhisArenaLobbyBot lb = getLobbyBot(id);
        if (lb != null) {
            return lb.isPKDHVT || lb.botState == WhisArenaLobbyBot.BotLobbyState.FIGHTING;
        }
        WhisArenaBot b = getBotById(id);
        return b != null && b.isCompeting;
    }

    private static final String[] BOT_NAMES = {
        "Goku_VoDai", "Vegeta_SSJ", "Trunks_TuongLai", "Gohan_Beast", "Piccolo_Cam",
        "Broly_HuyenThoai", "Hit_SatThu", "Jiren_CongLy", "Toppo_HuyDiet", "Dyspo_SieuToc",
        "ChienThan_Xayda", "DocCoCauBai", "Trum_Namek", "ThanLong_NRO", "KiemThanh_Vip",
        "HoangTu_Saiyan", "SatThu_VoDai", "VoDich_ThienHa", "BachThu_Thanh", "SieuNhan_Gao",
        "ThanhDam_Boc", "SieuCap_Goku", "Zamasu_BatTu", "Goku_Black", "Vegetto_Blue",
        "Gogeta_SSJ4", "Beerus_HuyDiet", "Champa_Map", "Cabba_Saiyan", "Frost_GianXao",
        "Magetta_NguoiSat", "Botamo_GauVang", "Kale_Berserk", "Caulifla_NuChien", "Kefla_VoSong",
        "Granolah_XaThu", "Gas_Helecit", "Moro_PhuThuy", "Merus_ThienSu", "Bardock_QuaCam",
        "Raditz_TocDai", "Nappa_DauTroc", "Saibaman_Xanh", "Cui_DoiThu", "Dodoria_Hong",
        "Zarbon_DepTrai", "Ginyu_TruongNhom", "Recoome_DoCon", "Burter_SieuToc", "Jeice_Do",
        "Guldo_NgungDong", "Frieza_Vang", "Cooler_KimLoai", "KingCold_Vua", "Android16_YeuChim",
        "Android17_KiemLam", "Android18_XinhGai", "Android19_MapTrang", "DrGero_Gia", "Cell_HoanThien",
        "Cell_Junior", "Bojack_HaiTac", "Zangya_TocCam", "Bido_DoCon", "Kogu_KiemSi",
        "Bujin_AoThuat", "Dabura_ChuaQuy", "MajinBuu_Map", "SuperBuu_Ac", "KidBuu_ThuanKhiet",
        "Gotenks_SieuQuay", "Uub_DauDen", "Pan_ChauGai", "Bra_TieuThu", "TienShinhan_3Mat",
        "Yamcha_LangTu", "Krillin_DauTroc", "Chaozu_BupBe", "Yajirobe_ThichThit", "MasterRoshi_QuyLao",
        "ChiChi_VoDai", "Videl_NuHiep", "MrSatan_VoDich", "Monaka_GiaoHang", "Arale_NghichNgom",
        "Gatchan_ThienThan", "TaoPaiPai_SatThu", "GeneralBlue_DepTrai", "NinjaMurasaki", "SonGohan_OngNoi",
        "ThoDaiKa_CaRot", "Dracula_HutMau", "NguoiVoHinh", "XacUop_AiCap", "VuaQuy_SaTang",
        "ThoDauBac", "BongBang_QuyVuong", "Pilaf_DaiVuong", "Mai_TuongLai", "Shu_ChoNho"
    };

    public WhisArenaBotManager() {
        initBots();
    }

    public static short[] getBotAppearance(String name, byte defaultGender) {
        if (name.contains("Goku_VoDai")) return new short[]{101, 65, 66, 0};
        if (name.contains("Vegeta_SSJ")) return new short[]{645, 646, 647, 2};
        if (name.contains("Trunks_TuongLai")) return new short[]{28, 16, 17, 2};
        if (name.contains("Gohan_Beast")) return new short[]{427, 428, 429, 0};
        if (name.contains("Piccolo_Cam")) return new short[]{228, 229, 230, 1};
        if (name.contains("Broly_HuyenThoai")) return new short[]{291, 292, 293, 2};
        if (name.contains("Hit_SatThu")) return new short[]{520, 521, 522, 0};
        if (name.contains("Jiren_CongLy")) return new short[]{528, 525, 524, 0};
        if (name.contains("Toppo_HuyDiet")) return new short[]{529, 525, 524, 2};
        if (name.contains("Dyspo_SieuToc")) return new short[]{527, 525, 524, 0};
        if (name.contains("ChienThan_Xayda")) return new short[]{28, 646, 647, 2};
        if (name.contains("DocCoCauBai")) return new short[]{27, 649, 650, 2};
        if (name.contains("Trum_Namek")) return new short[]{9, 10, 11, 1};
        if (name.contains("ThanLong_NRO")) return new short[]{64, 65, 66, 0};
        if (name.contains("KiemThanh_Vip")) return new short[]{28, 16, 17, 2};
        if (name.contains("HoangTu_Saiyan")) return new short[]{645, 646, 647, 2};
        if (name.contains("SatThu_VoDai")) return new short[]{520, 521, 522, 0};
        if (name.contains("VoDich_ThienHa")) return new short[]{101, 65, 66, 0};
        if (name.contains("BachThu_Thanh")) return new short[]{228, 229, 230, 1};
        if (name.contains("SieuNhan_Gao")) return new short[]{64, 65, 66, 0};
        if (name.contains("ThanhDam_Boc")) return new short[]{101, 65, 66, 0};
        if (name.contains("SieuCap_Goku")) return new short[]{1475, 1477, 1478, 0};
        if (name.contains("Zamasu_BatTu")) return new short[]{903, 904, 905, 1};
        if (name.contains("Goku_Black")) return new short[]{550, 551, 552, 0};
        if (name.contains("Vegetto_Blue")) return new short[]{1475, 1477, 1478, 0};
        if (name.contains("Gogeta_SSJ4")) return new short[]{1388, 1391, 1392, 0};
        if (name.contains("Beerus_HuyDiet")) return new short[]{514, 515, 516, 2};
        if (name.contains("Champa_Map")) return new short[]{514, 515, 516, 2};
        if (name.contains("Cabba_Saiyan")) return new short[]{517, 518, 519, 2};
        if (name.contains("Frost_GianXao")) return new short[]{499, 500, 501, 0};
        if (name.contains("Magetta_NguoiSat")) return new short[]{514, 515, 516, 0};
        if (name.contains("Botamo_GauVang")) return new short[]{297, 298, 299, 0};
        if (name.contains("Kale_Berserk")) return new short[]{757, 691, 692, 2};
        if (name.contains("Caulifla_NuChien")) return new short[]{757, 691, 692, 2};
        if (name.contains("Kefla_VoSong")) return new short[]{757, 691, 692, 2};
        if (name.contains("Granolah_XaThu")) return new short[]{528, 525, 524, 0};
        if (name.contains("Gas_Helecit")) return new short[]{451, 452, 453, 0};
        if (name.contains("Moro_PhuThuy")) return new short[]{418, 419, 420, 1};
        if (name.contains("Merus_ThienSu")) return new short[]{838, 839, 840, 0};
        if (name.contains("Bardock_QuaCam")) return new short[]{648, 649, 650, 2};
        if (name.contains("Raditz_TocDai")) return new short[]{648, 649, 650, 2};
        if (name.contains("Nappa_DauTroc")) return new short[]{29, 646, 647, 2};
        if (name.contains("Saibaman_Xanh")) return new short[]{642, 643, 644, 1};
        if (name.contains("Cui_DoiThu")) return new short[]{159, 160, 161, 2};
        if (name.contains("Dodoria_Hong")) return new short[]{165, 166, 167, 2};
        if (name.contains("Zarbon_DepTrai")) return new short[]{162, 163, 164, 2};
        if (name.contains("Ginyu_TruongNhom")) return new short[]{183, 184, 185, 2};
        if (name.contains("Recoome_DoCon")) return new short[]{174, 175, 176, 2};
        if (name.contains("Burter_SieuToc")) return new short[]{171, 172, 173, 2};
        if (name.contains("Jeice_Do")) return new short[]{177, 178, 179, 2};
        if (name.contains("Guldo_NgungDong")) return new short[]{180, 181, 182, 2};
        if (name.contains("Frieza_Vang")) return new short[]{502, 503, 504, 0};
        if (name.contains("Cooler_KimLoai")) return new short[]{317, 318, 319, 0};
        if (name.contains("KingCold_Vua")) return new short[]{320, 321, 322, 0};
        if (name.contains("Android16_YeuChim")) return new short[]{138, 139, 140, 0};
        if (name.contains("Android17_KiemLam")) return new short[]{129, 130, 131, 0};
        if (name.contains("Android18_XinhGai")) return new short[]{141, 142, 143, 0};
        if (name.contains("Android19_MapTrang")) return new short[]{249, 250, 251, 0};
        if (name.contains("DrGero_Gia")) return new short[]{252, 253, 254, 0};
        if (name.contains("Cell_HoanThien")) return new short[]{234, 235, 236, 1};
        if (name.contains("Cell_Junior")) return new short[]{264, 265, 266, 1};
        if (name.contains("Bojack_HaiTac")) return new short[]{323, 324, 325, 2};
        if (name.contains("Zangya_TocCam")) return new short[]{332, 333, 334, 2};
        if (name.contains("Bido_DoCon")) return new short[]{335, 336, 337, 2};
        if (name.contains("Kogu_KiemSi")) return new short[]{329, 330, 331, 2};
        if (name.contains("Bujin_AoThuat")) return new short[]{341, 342, 343, 2};
        if (name.contains("Dabura_ChuaQuy")) return new short[]{418, 419, 420, 2};
        if (name.contains("MajinBuu_Map")) return new short[]{297, 298, 299, 0};
        if (name.contains("SuperBuu_Ac")) return new short[]{421, 422, 423, 0};
        if (name.contains("KidBuu_ThuanKhiet")) return new short[]{439, 440, 441, 0};
        if (name.contains("Gotenks_SieuQuay")) return new short[]{424, 425, 426, 0};
        if (name.contains("Uub_DauDen")) return new short[]{946, 947, 948, 0};
        if (name.contains("Pan_ChauGai")) return new short[]{1683, 1684, 1685, 0};
        if (name.contains("Bra_TieuThu")) return new short[]{141, 142, 143, 0};
        if (name.contains("TienShinhan_3Mat")) return new short[]{368, 369, 370, 0};
        if (name.contains("Yamcha_LangTu")) return new short[]{374, 375, 376, 0};
        if (name.contains("Krillin_DauTroc")) return new short[]{3, 65, 66, 0};
        if (name.contains("Chaozu_BupBe")) return new short[]{371, 372, 373, 0};
        if (name.contains("Yajirobe_ThichThit")) return new short[]{77, 78, 79, 0};
        if (name.contains("MasterRoshi_QuyLao")) return new short[]{356, 357, 358, 0};
        if (name.contains("ChiChi_VoDai")) return new short[]{1393, 1394, 1395, 0};
        if (name.contains("Videl_NuHiep")) return new short[]{141, 142, 143, 0};
        if (name.contains("MrSatan_VoDich")) return new short[]{344, 345, 346, 0};
        if (name.contains("Monaka_GiaoHang")) return new short[]{400, 401, 402, 0};
        if (name.contains("Arale_NghichNgom")) return new short[]{573, 574, 575, 0};
        if (name.contains("Gatchan_ThienThan")) return new short[]{579, 580, 581, 0};
        if (name.contains("TaoPaiPai_SatThu")) return new short[]{92, 93, 94, 0};
        if (name.contains("GeneralBlue_DepTrai")) return new short[]{135, 136, 137, 0};
        if (name.contains("NinjaMurasaki")) return new short[]{123, 124, 125, 0};
        if (name.contains("SonGohan_OngNoi")) return new short[]{356, 357, 358, 0};
        if (name.contains("ThoDaiKa_CaRot")) return new short[]{403, 404, 405, 0};
        if (name.contains("Dracula_HutMau")) return new short[]{353, 354, 355, 0};
        if (name.contains("NguoiVoHinh")) return new short[]{377, 378, 379, 0};
        if (name.contains("XacUop_AiCap")) return new short[]{350, 351, 352, 0};
        if (name.contains("VuaQuy_SaTang")) return new short[]{344, 345, 346, 0};
        if (name.contains("ThoDauBac")) return new short[]{347, 348, 349, 0};
        if (name.contains("BongBang_QuyVuong")) return new short[]{350, 351, 352, 0};
        if (name.contains("Pilaf_DaiVuong")) return new short[]{347, 348, 349, 0};
        if (name.contains("Mai_TuongLai")) return new short[]{615, 616, 617, 0};
        if (name.contains("Shu_ChoNho")) return new short[]{347, 348, 349, 0};

        // Fallback chuẩn theo hành tinh: 100% đúng type (head: type 0, body: type 1, leg: type 2)
        if (defaultGender == 0) return new short[]{101, 65, 66, 0};
        if (defaultGender == 1) return new short[]{228, 229, 230, 1};
        return new short[]{645, 646, 647, 2};
    }

    private void initBots() {
        bots.clear();
        int baseWin = 250;
        for (int i = 0; i < 100; i++) {
            String name = (i < BOT_NAMES.length) ? BOT_NAMES[i] : ("Chiendau_Bot_" + (i + 1));
            byte defaultGender = (byte) (i % 3);
            
            // Ngoại trang chuẩn theo nhân vật / hành tinh
            short[] outfit = getBotAppearance(name, defaultGender);
            short head = outfit[0];
            short body = outfit[1];
            short leg = outfit[2];
            byte gender = (byte) outfit[3];
            short flagBag = (short) (Util.nextInt(0, 10) == 0 ? 995 : -1);
            short aura = (short) (i < 10 ? 1 : -1);

            // Sức mạnh & Chỉ số theo thứ hạng
            long hpMax;
            long dame;
            if (i < 5) {
                // Top 1 - 5 cực mạnh
                hpMax = 300_000_000L + (5 - i) * 50_000_000L;
                dame = 80_000 + (5 - i) * 15_000;
            } else if (i < 20) {
                // Top 6 - 20 khá mạnh
                hpMax = 150_000_000L + (20 - i) * 5_000_000L;
                dame = 50_000 + (20 - i) * 2_000;
            } else if (i < 50) {
                // Top 21 - 50 trung bình
                hpMax = 50_000_000L + (50 - i) * 2_000_000L;
                dame = 30_000 + (50 - i) * 500;
            } else {
                // Top 51 - 100 tân thủ lôi đài
                hpMax = 10_000_000L + (100 - i) * 500_000L;
                dame = 10_000 + (100 - i) * 200;
            }

            int arenaWins = Math.max(10, baseWin - (i * 2 + Util.nextInt(0, 3)));

            WhisArenaBot bot = new WhisArenaBot(
                -20000 - i, // ID ảo âm để không trùng player thật
                name,
                gender,
                head,
                body,
                leg,
                flagBag,
                aura,
                hpMax,
                hpMax,
                dame,
                arenaWins
            );
            bot.rank = i + 1;
            bots.add(bot);
        }
    }

    public List<WhisArenaBot> getBots() {
        return bots;
    }

    /**
     * Sắp xếp lại thứ hạng các Bot theo arenaWins giảm dần
     */
    public void sortBots() {
        bots.sort(Comparator.comparingInt((WhisArenaBot b) -> b.arenaWins).reversed());
        for (int i = 0; i < bots.size(); i++) {
            bots.get(i).rank = i + 1;
        }
    }

    /**
     * Lấy thứ hạng của người chơi trong tương quan với toàn bộ 100 Bot và người chơi khác
     * @param player Người chơi cần kiểm tra
     * @return Thứ hạng từ 1 trở lên
     */
    public int getPlayerRank(Player player) {
        if (player == null) {
            return 999;
        }
        int rank = 1;
        for (WhisArenaBot bot : bots) {
            if (bot.arenaWins > player.arenaWins) {
                rank++;
            }
        }
        try {
            java.util.Set<Long> countedPlayerIds = new java.util.HashSet<>();
            for (Player p : server.Client.gI().getPlayers()) {
                if (p != null && p.id != player.id && p.arenaWins > player.arenaWins) {
                    if (countedPlayerIds.add(p.id)) {
                        rank++;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return rank;
    }

    public WhisArenaBot getRandomBot() {
        if (bots.isEmpty()) {
            return null;
        }
        return bots.get(Util.nextInt(0, bots.size() - 1));
    }

    /**
     * Ghép ngẫu nhiên hoàn toàn với bất kỳ đối thủ nào (không phân chia cấp độ hay rank)
     */
    public WhisArenaBot getBotForMatchmaking(Player player) {
        return getRandomBot();
    }

    /**
     * Tìm Bot theo ID
     */
    public WhisArenaBot getBotById(int id) {
        for (WhisArenaBot bot : bots) {
            if (bot.id == id) {
                return bot;
            }
        }
        return null;
    }

    /**
     * Lấy danh sách Top Bot phục vụ hiển thị BXH
     */
    public List<WhisArenaBot> getTopBots(int limit) {
        sortBots();
        List<WhisArenaBot> list = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, bots.size()); i++) {
            list.add(bots.get(i));
        }
        return list;
    }

    public static class WhisArenaTopEntry {
        public int rank;
        public int id;
        public short head;
        public short body;
        public short leg;
        public String name;
        public String info1;
        public String info2;

        public WhisArenaTopEntry(int rank, int id, short head, short body, short leg, String name, String info1, String info2) {
            this.rank = rank;
            this.id = id;
            this.head = head;
            this.body = body;
            this.leg = leg;
            this.name = name;
            this.info1 = info1;
            this.info2 = info2;
        }
    }

    public static class ArenaRankCandidate {
        public int id;
        public String name;
        public short head;
        public short body;
        public short leg;
        public int arenaWins;
        public boolean isBot;
        public boolean isViewer;

        public ArenaRankCandidate(int id, String name, short head, short body, short leg, int arenaWins, boolean isBot, boolean isViewer) {
            this.id = id;
            this.name = name;
            this.head = head;
            this.body = body;
            this.leg = leg;
            this.arenaWins = arenaWins;
            this.isBot = isBot;
            this.isViewer = isViewer;
        }
    }

    /**
     * Tạo danh sách xếp hạng tổng hợp giữa Bot và Người chơi thật để hiển thị Panel BXH chuẩn
     */
    public List<WhisArenaTopEntry> getLeaderboardEntries(Player viewer, int limit) {
        sortBots();
        List<ArenaRankCandidate> candidates = new ArrayList<>();
        java.util.Set<Integer> addedRealIds = new java.util.HashSet<>();

        // 1. Thêm Viewer
        if (viewer != null) {
            addedRealIds.add((int) viewer.id);
            candidates.add(new ArenaRankCandidate(
                    (int) viewer.id,
                    viewer.name + " (Bạn)",
                    viewer.getHead(),
                    viewer.getBody(),
                    viewer.getLeg(),
                    viewer.arenaWins,
                    false,
                    true
            ));
        }

        // 2. Thêm người chơi online khác có điểm Võ Đài
        try {
            for (Player pl : server.Client.gI().getPlayers()) {
                if (pl != null && pl.arenaWins > 0 && !addedRealIds.contains((int) pl.id)) {
                    addedRealIds.add((int) pl.id);
                    candidates.add(new ArenaRankCandidate(
                            (int) pl.id,
                            pl.name,
                            pl.getHead(),
                            pl.getBody(),
                            pl.getLeg(),
                            pl.arenaWins,
                            false,
                            false
                    ));
                }
            }
        } catch (Exception ignored) {
        }

        // 3. Thêm top người chơi từ Database (topArena đã cache trong Manager)
        if (server.Manager.topArena != null) {
            for (models.matches.TOP top : server.Manager.topArena) {
                if (top != null && !addedRealIds.contains(top.getId_player())) {
                    addedRealIds.add(top.getId_player());
                    try {
                        Player pl = database.daos.NDVSqlFetcher.loadById(top.getId_player());
                        if (pl != null) {
                            candidates.add(new ArenaRankCandidate(
                                    (int) pl.id,
                                    pl.name,
                                    pl.getHead(),
                                    pl.getBody(),
                                    pl.getLeg(),
                                    pl.arenaWins,
                                    false,
                                    false
                            ));
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (addedRealIds.size() >= 15) {
                    break;
                }
            }
        }

        // 4. Thêm 100 Bots
        for (WhisArenaBot bot : bots) {
            candidates.add(new ArenaRankCandidate(
                    bot.id,
                    bot.name,
                    bot.head,
                    bot.body,
                    bot.leg,
                    bot.arenaWins,
                    true,
                    false
            ));
        }

        // 5. Sắp xếp toàn bộ ứng viên theo arenaWins giảm dần
        candidates.sort((c1, c2) -> {
            int cmp = Integer.compare(c2.arenaWins, c1.arenaWins);
            if (cmp != 0) return cmp;
            if (c1.isViewer) return -1;
            if (c2.isViewer) return 1;
            return Boolean.compare(c1.isBot, c2.isBot);
        });

        // 6. Xây dựng danh sách hiển thị
        List<WhisArenaTopEntry> list = new ArrayList<>();
        boolean viewerInList = false;
        WhisArenaTopEntry viewerEntry = null;

        for (int i = 0; i < candidates.size(); i++) {
            ArenaRankCandidate cand = candidates.get(i);
            int rank = i + 1;
            String info1 = cand.arenaWins + " điểm";
            String info2 = "Hạng " + rank;

            WhisArenaTopEntry entry = new WhisArenaTopEntry(
                    rank,
                    cand.id,
                    cand.head,
                    cand.body,
                    cand.leg,
                    cand.name,
                    info1,
                    info2
            );

            if (cand.isViewer) {
                viewerInList = true;
                viewerEntry = entry;
            }

            if (list.size() < limit) {
                list.add(entry);
            } else if (viewerInList) {
                break;
            }
        }

        // Nếu viewer vượt quá limit (ví dụ rank > 100), thay thế slot cuối để viewer luôn thấy hạng của mình
        if (!viewerInList && viewerEntry != null && !list.isEmpty()) {
            list.set(list.size() - 1, viewerEntry);
        }

        return list;
    }

    /**
     * Lấy danh sách đối thủ phục vụ thách đấu (mở rộng ALL Bảng Xếp Hạng 1-100, không bị khoanh top 5)
     */
    public List<WhisArenaTopEntry> getChallengeEntries(Player viewer, int maxSteps) {
        return getLeaderboardEntries(viewer, 100);
    }

    /**
     * Được gọi bởi GameLoopManager mỗi 150ms để cập nhật hành vi AI của các Lobby Bot
     */
    public void update() {
        for (WhisArenaLobbyBot lb : lobbyBots) {
            try {
                if (lb != null) {
                    lb.update();
                }
            } catch (Exception e) {
                utils.Logger.logException(WhisArenaBotManager.class, e, "Lỗi update LobbyBot " + (lb != null ? lb.name : "null"));
            }
        }
    }

    /**
     * Khởi tạo Lobby Bots khi server khởi động xong (sau khi maps đã load)
     */
    public void initLobbyBotsOnStartup() {
        try {
            models.map.Map map186 = services.map.MapService.gI().getMapById(186);
            if (map186 != null && !map186.zones.isEmpty()) {
                models.map.Zone zone0 = map186.zones.get(0);
                initLobbyBots(zone0);
                utils.Logger.success("[WhisArena] Đã khởi tạo " + lobbyBots.size() + " Lobby Bot tại Map 186 Zone 0");
            } else {
                utils.Logger.error("[WhisArena] Không tìm thấy Map 186 để khởi tạo Lobby Bot!");
            }
        } catch (Exception e) {
            utils.Logger.logException(WhisArenaBotManager.class, e, "Lỗi initLobbyBotsOnStartup");
        }
    }
}

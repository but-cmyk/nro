package models.tournament.whis_arena;

import consts.BossType;
import models.boss.BossData;
import models.boss.boss_list.SuperRank.SuperRank;
import models.player.Player;
import models.skill.Skill;
import services.map.ChangeMapService;

public class WhisArenaU6Boss extends SuperRank {

    private final WhisArenaBossMatch match;
    public final int round;

    public WhisArenaU6Boss(Player player, int round, WhisArenaBossMatch match) throws Exception {
        super(BossType.PHOBAN, -30001 - round, createBossData(round));
        this.playerAtt = player;
        this.match = match;
        this.round = round;
        this.currentLevel = 0;
        this.initBase();
    }

    public static String getBossName(int round) {
        return switch (round) {
            case 0 -> "Frost";
            case 1 -> "Kyabe";
            case 2 -> "Autamegetta";
            case 3 -> "Hit";
            default -> "Hit";
        };
    }

    public static BossData createBossData(int round) {
        String name;
        short head, body, leg;
        int dame;
        int hp;
        int[][] skills;
        String[] textChat1, textChat2, textChat3;

        switch (round) {
            case 0 -> { // Vòng 1: Frost (Ảnh 2)
                name = "Frost";
                head = 499;
                body = 500;
                leg = 501;
                hp = 50_000_000;
                dame = 50_000;
                skills = new int[][]{
                    {Skill.DRAGON, 7, 100},
                    {Skill.LIEN_HOAN, 7, 1000},
                    {Skill.KAMEJOKO, 7, 2000},
                    {Skill.THAI_DUONG_HA_SAN, 1, 15000}
                };
                textChat1 = new String[]{"Vũ trụ 6 sẽ cho các ngươi thấy sức mạnh thực sự!", "Frost ta đây sẽ không nương tay đâu!"};
                textChat2 = new String[]{"Ngươi cũng có chút bản lĩnh đấy!", "Nhưng kết cục vẫn là thất bại thôi!"};
                textChat3 = new String[]{"Khốn kiếp... ta không thể thua thế này được..."};
            }
            case 1 -> { // Vòng 2: Kyabe (Ảnh 3)
                name = "Kyabe";
                head = 517;
                body = 518;
                leg = 519;
                hp = 75_000_000;
                dame = 80_000;
                skills = new int[][]{
                    {Skill.GALICK, 7, 100},
                    {Skill.ANTOMIC, 7, 1000},
                    {Skill.DRAGON, 7, 500},
                    {Skill.BIEN_KHI, 1, 30000}
                };
                textChat1 = new String[]{"Tôi là Kyabe, người Saiyan của Vũ trụ 6!", "Xin hãy chỉ giáo!"};
                textChat2 = new String[]{"Tôi sẽ dốc toàn lực!", "Galick Gun!!!"};
                textChat3 = new String[]{"Sức mạnh của người Saiyan Vũ trụ 7 thật đáng kinh ngạc..."};
            }
            case 2 -> { // Vòng 3: Autamegetta (Ảnh 4: 99.203.200 HP)
                name = "Autamegetta";
                head = 514;
                body = 515;
                leg = 516;
                hp = 99_203_200;
                dame = 120_000;
                skills = new int[][]{
                    {Skill.DEMON, 7, 100},
                    {Skill.MASENKO, 7, 1000},
                    {Skill.KHIEN_NANG_LUONG, 1, 20000}
                };
                textChat1 = new String[]{"Choo-poo! Choo-poo!", "Gaaaahhhhh!!!"};
                textChat2 = new String[]{"Choo-poo! (Phun nham thạch)", "Bốc khói mù mịt!"};
                textChat3 = new String[]{"Choo... poo... (Ngồi sụp xuống tủi thân)"};
            }
            default -> { // Vòng 4: Hit (Ảnh 5: 148.804.800 HP)
                name = "Hit";
                head = 520;
                body = 521;
                leg = 522;
                hp = 148_804_800;
                dame = 200_000;
                skills = new int[][]{
                    {Skill.DRAGON, 7, 100},
                    {Skill.KAMEJOKO, 7, 1000},
                    {Skill.THAI_DUONG_HA_SAN, 1, 10000},
                    {Skill.KHIEN_NANG_LUONG, 1, 25000}
                };
                textChat1 = new String[]{"Ta là Hit - Sát thủ tối thượng của Vũ trụ 6.", "Ngươi chuẩn bị xong chưa?"};
                textChat2 = new String[]{"Bước nhảy thời gian!", "Ngươi không thể chạm vào ta đâu!"};
                textChat3 = new String[]{"Một trận đấu đáng nhớ... Ngươi rất mạnh!"};
            }
        }

        return new BossData(
                name,
                (byte) 0,
                new short[]{head, body, leg, -1, -1, -1},
                dame,
                new int[]{hp},
                new int[]{186, 187},
                skills,
                textChat1,
                textChat2,
                textChat3,
                60
        );
    }

    @Override
    public void joinMap() {
        if (playerAtt != null && playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            if (this.nPoint != null) {
                this.nPoint.hp = this.nPoint.hpMax;
                this.nPoint.mp = this.nPoint.mpMax;
            }
            ChangeMapService.gI().changeMap(this, this.zone, 435, 264);
        }
    }

    @Override
    public void die(Player plKill) {
        super.die(plKill);
        if (match != null) {
            match.onBossDie();
        }
    }

    @Override
    public void checkPlayerDie(Player pl) {
        if (pl != null && pl.equals(playerAtt) && pl.isDie()) {
            if (match != null) {
                match.onPlayerDie();
            }
        }
    }
}

package models.tournament.whis_arena;

import models.boss.BossData;
import static consts.BossType.PHOBAN;
import models.boss.boss_list.SuperRank.SuperRank;
import models.player.Player;
import models.skill.Skill;
import services.Service;
import services.map.ChangeMapService;

public class WhisArenaRival extends SuperRank {

    public WhisArenaBot botData;
    private WhisArenaMatch match;

    public WhisArenaRival(Player player, WhisArenaBot bot, WhisArenaMatch match) throws Exception {
        super(PHOBAN, bot.id, createBossData(bot));
        this.playerAtt = player;
        this.botData = bot;
        this.match = match;
        this.currentLevel = 0;
        this.initBase();
    }

    public static BossData createBossData(WhisArenaBot bot) {
        int[][] skills;
        if (bot.gender == 0) {
            skills = new int[][]{
                {Skill.DRAGON, 7, 100},
                {Skill.KAMEJOKO, 7, 1000},
                {Skill.THAI_DUONG_HA_SAN, 1, 15000}
            };
        } else if (bot.gender == 1) {
            skills = new int[][]{
                {Skill.DEMON, 7, 100},
                {Skill.MASENKO, 7, 1000},
                {Skill.TRI_THUONG, 1, 20000}
            };
        } else {
            skills = new int[][]{
                {Skill.GALICK, 7, 100},
                {Skill.ANTOMIC, 7, 1000},
                {Skill.KHIEN_NANG_LUONG, 1, 30000}
            };
        }

        return new BossData(
                bot.name,
                bot.gender,
                new short[]{bot.head, bot.body, bot.leg, bot.flagBag, bot.aura, -1},
                bot.dame,
                new long[]{bot.hpMax},
                new int[]{186},
                skills,
                new String[]{"Ta là " + bot.name + " của Vũ Đài Whis!", "Hãy xem bản lĩnh của ngươi ra sao!"},
                new String[]{"Khá lắm, nhưng chưa đủ đâu!", "Tiếp chiêu này đi!"},
                new String[]{"Ngươi khá đấy, lần sau ta sẽ thắng!"},
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
            match.onRivalDie();
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

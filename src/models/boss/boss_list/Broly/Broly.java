package models.boss.boss_list.Broly;

import models.boss.Boss;
import models.boss.BossData;
import consts.BossID;
import consts.BossStatus;
import static consts.BossType.BROLY;
import consts.ConstPlayer;
import models.map.Zone;
import models.player.Player;
import services.SkillService;
import services.map.ChangeMapService;
import models.skill.Skill;
import utils.Logger;
import utils.Util;

public class Broly extends Boss {

    // Thêm biến theo dõi thời gian dùng skill hồi
    private long lastTimeHealSkill = -1;

    public Broly() throws Exception {
        super(BROLY, BossID.BROLY, new BossData(
                "Broly", ConstPlayer.XAYDA, new short[]{291, 292, 293, -1, -1, -1},
                100, new int[]{1000}, new int[]{5, 13, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38},
                new int[][]{
                        {Skill.TAI_TAO_NANG_LUONG, 7, 1000}, {Skill.DRAGON, 7, 1000},
                        {Skill.GALICK, 7, 1000}, {Skill.KAMEJOKO, 7, 1000},
                        {Skill.MASENKO, 7, 1000}, {Skill.ANTOMIC, 7, 1000}},
                new String[]{},
                new String[]{"|-1|Haha! ta sẽ giết hết các ngươi", "|-1|Đứng xa ta ra đừng làm ta nổi giận"},
                new String[]{"|-1|Ta sẽ quay lại."}, 600
        ));
    }

    @Override
    public void joinMap() {
        this.name = "Broly " + Util.nextInt(10, 100);
        this.nPoint.hpMax = Util.nextInt(500, 10000);
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dame = this.nPoint.hpMax / 100;
        
        // Reset cooldown skill hồi khi boss mới spawn
        this.lastTimeHealSkill = -1;

        // --- LOGIC TÌM KHU CÓ NGƯỜI ---
        this.zone = getMapJoin();
        if (this.zone != null) {
            try {
                for (Zone z : this.zone.map.zones) {
                    if (z.getNumOfPlayers() > 0 && z.getBosses().isEmpty()) {
                        this.zone = z;
                        break;
                    }
                }
            } catch (Exception e) {
                Logger.logException(Broly.class, e, "Error scanning zone for Broly");
            }

            ChangeMapService.gI().changeMap(this, this.zone, -1, -1);
            this.changeStatus(BossStatus.CHAT_S);
        } else {
            this.leaveMap();
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) return 0;
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }
        damage = this.nPoint.subDameInjureWithDeff(damage);

        long limitDame = this.nPoint.hpMax / 100;
        boolean isTuSat = (plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null
                && plAtt.playerSkill.skillSelect.template != null && plAtt.playerSkill.skillSelect.template.id == Skill.TU_SAT);
        if (!piercing && !isTuSat && damage > limitDame) {
            damage = limitDame;
        }

        this.nPoint.subHP(damage);

        if (Util.isTrue(1, 10)) this.tangChiSo();

        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public Player getPlayerAttack() {
        Player pl = super.getPlayerAttack();
        // Nếu Broly đã tăng nộ mạnh (hpMax > 500.000) và mục tiêu là tân thủ (power < 1.500.000), tránh đồ sát
        if (pl != null && this.nPoint != null && this.nPoint.hpMax > 500_000 && pl.nPoint != null && pl.nPoint.power < 1_500_000) {
            this.playerTarger = null;
            return null;
        }
        return pl;
    }

    @Override
    public void attack() {
        // Cooldown 800ms cho tất cả skill
        if (!Util.canDoWithTime(this.lastTimeAttack, 800) || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        
        this.lastTimeAttack = System.currentTimeMillis();
        
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
                return;
            }

            // LOGIC CHỌN SKILL THÔNG MINH
            selectSkill();
            
            // Kiểm tra khoảng cách
            if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                SkillService.gI().useSkill(this, pl, null, -1, null);
                checkPlayerDie(pl);
            } else {
                this.moveToPlayer(pl);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Logic chọn skill thông minh:
     * - BẮT BUỘC hồi máu khi HP < 10% (cooldown 10s)
     * - Ngẫu nhiên skill tấn công khi HP đủ hoặc đang cooldown
     */
    private void selectSkill() {
        // Tính % HP hiện tại
        int hpPercent = (int) ((this.nPoint.hp * 100) / this.nPoint.hpMax);
        
        // Kiểm tra cooldown skill hồi
        boolean canUseHealSkill = (lastTimeHealSkill == -1) || 
                                  Util.canDoWithTime(lastTimeHealSkill, 20000);
        
        // Điều kiện dùng skill hồi:
        // 1. HP < 10% (nguy hiểm)
        // 2. Đã hết cooldown 10 giây
        if (hpPercent < 15 && canUseHealSkill) {
            
            // Chọn skill hồi (index 0)
            this.playerSkill.skillSelect = this.playerSkill.skills.get(0);
            this.lastTimeHealSkill = System.currentTimeMillis();
            this.chat("Tái tạo năng lượng!");
            
        } else {
            // Chọn ngẫu nhiên 1 trong 5 skill tấn công (index 1-5)
            int attackSkillIndex = Util.nextInt(1, 5);
            this.playerSkill.skillSelect = this.playerSkill.skills.get(attackSkillIndex);
        }
    }

    private void tangChiSo() {
        long hpMax = this.nPoint.hpMax;
        hpMax += hpMax / 8;
        if (hpMax > 16_070_777) hpMax = 16_070_777;
        this.nPoint.hpMax = (int)hpMax;
        this.nPoint.dame = (int)(hpMax / 10);

        if (this.nPoint.hpMax >= 16_070_777) this.leaveMap();
    }

    @Override
    public void leaveMap() {
        Zone zoneJoin = this.zone;
        int x = this.location.x;
        int y = this.location.y;
        ChangeMapService.gI().exitMap(this);

        // --- GỌI SUPER BROLY ---
        try {
            boolean spawnSuper = false;

            // Nếu HP >= 1.500.000 -> Tỉ lệ 100%
            if (this.nPoint.hpMax >= 1_500_000) {
                spawnSuper = true;
            }
            // Nếu HP < 1.500.000 -> Tỉ lệ 10%
            else {
                spawnSuper = Util.isTrue(10, 100);
            }

            if (spawnSuper) {
                new SuperBroly(zoneJoin, x, y);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // -----------------------

        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }
}
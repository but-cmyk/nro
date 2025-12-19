package models.boss.boss_list.Broly;

import models.boss.Boss;
import managers.boss.BrolyManager;
import models.boss.BossData;
import consts.BossID;
import consts.BossStatus;
import static consts.BossType.BROLY;
import consts.ConstPlayer;
import models.map.Zone;
import models.player.Player;
import services.PetService;
import services.SkillService;
import services.map.ChangeMapService;
import models.skill.Skill;
import utils.Util;

public class SuperBroly extends Boss {

    private long st;
    private Boss detu;
    
    // Thêm biến theo dõi thời gian dùng skill hồi (cooldown 10 giây)
    private long lastTimeHealSkill = -1;

    public SuperBroly(Zone zone, int x, int y) throws Exception {
        super(BROLY, BossID.SUPER_BROLY, false, true, new BossData(
                "Super Broly", ConstPlayer.XAYDA, new short[]{294, 295, 296, -1, -1, -1},
                100, new int[]{1000}, new int[]{5},
                new int[][]{
                        {Skill.TAI_TAO_NANG_LUONG, 7, 1000},
                        {Skill.DRAGON, 7, 1000},
                        {Skill.GALICK, 7, 1000},
                        {Skill.KAMEJOKO, 7, 1000},
                        {Skill.ANTOMIC, 7, 1000}
                },
                new String[]{},
                new String[]{"|-1|Haha! ta sẽ giết hết các ngươi", "|-1|tránh xa ta ra đừng để ta nổi giận!", "|-1|Vào hết đây!!!"},
                new String[]{"|-1|Các ngươi giỏi lắm. Ta sẽ quay lại."},
                600
        ));
        this.zone = zone;
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void reward(Player plKill) {
        if (plKill.pet == null) {
            PetService.gI().createNormalPet(plKill);
            this.chat("Hãy chăm sóc đệ tử của ta...");
        }
    }

    @Override
    public void joinMap() {
        this.name = "Super Broly " + Util.nextInt(10, 100);
        this.nPoint.hpMax = Util.isTrue(80, 100) ? Util.nextInt(1_050_000, 3_500_000) : Util.nextInt(3_500_001, 10_000_000);
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.dame = this.nPoint.hpMax / 100;
        
        // Reset cooldown skill hồi khi boss spawn
        this.lastTimeHealSkill = -1;

        if (this.zone != null && this.zone.getNumOfPlayers() <= 0) {
            try {
                for (Zone z : this.zone.map.zones) {
                    if (z.getNumOfPlayers() > 0) {
                        this.zone = z;
                        this.location.x = 200;
                        this.location.y = 100;
                        break;
                    }
                }
            } catch(Exception e){}
        }

        ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
        this.changeStatus(BossStatus.CHAT_S);
        this.notifyJoinMap();

        try {
            this.detu = new DetuBroly(this.zone, this.location.x - 30, this.location.y, this);
        } catch (Exception e) { e.printStackTrace(); }

        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
            return;
        }
        if (Util.canDoWithTime(st, 900000)) {
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

        int skillID = plAtt.playerSkill.skillSelect.template.id;

        boolean isSpecialSkill = (skillID == Skill.TU_SAT ||
                skillID == Skill.MAKANKOSAPPO ||
                skillID == Skill.QUA_CAU_KENH_KHI);

        if (!isSpecialSkill && damage >= 199999) {
            damage = 199999;
        }

        damage = this.nPoint.subDameInjureWithDeff(damage);

        long limit = this.nPoint.hpMax / 100;

        if (!piercing && !isSpecialSkill && damage > limit) {
            damage = limit;
        }

        this.nPoint.subHP(damage);

        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public void attack() {
        // Kiểm tra đang gồng skill
        if (this.effectSkill != null && this.effectSkill.isCharging) {
            return;
        }

        if (!Util.canDoWithTime(this.lastTimeAttack, 600) || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        
        this.lastTimeAttack = System.currentTimeMillis();
        
        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
                return;
            }

            // Tính % HP hiện tại
            int hpPercent = (int) ((this.nPoint.hp * 100) / this.nPoint.hpMax);
            
            // Kiểm tra có thể dùng skill hồi không
            boolean canUseHealSkill = (lastTimeHealSkill == -1) || 
                                      Util.canDoWithTime(lastTimeHealSkill, 40000);
            
            // === LOGIC DÙNG SKILL HỒI KHI HP < 10% ===
            if (hpPercent < 10 && canUseHealSkill) {
                
                // Tìm skill TAI_TAO_NANG_LUONG
                Skill skillTTNL = null;
                for (Skill s : this.playerSkill.skills) {
                    if (s.template.id == Skill.TAI_TAO_NANG_LUONG) {
                        skillTTNL = s;
                        break;
                    }
                }
                
                if (skillTTNL != null) {
                    // 1. Tăng chỉ số HP Max TRƯỚC
                    this.tangChiSo();
                    
                    // 2. Chọn và dùng skill hồi
                    this.playerSkill.skillSelect = skillTTNL;
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    
                    // 3. Bắt đầu cooldown 10 giây
                    this.lastTimeHealSkill = System.currentTimeMillis();
                    this.chat("Tái tạo năng lượng!");
                    
                    return; // Kết thúc lượt này
                }
            }
            
            // === LOGIC TẤN CÔNG THÔNG THƯỜNG ===
            // Chọn ngẫu nhiên 1 skill tấn công (tránh skill hồi)
            Skill selectedSkill = null;
            int maxTries = 10;
            
            while (maxTries > 0) {
                Skill s = this.playerSkill.skills.get(
                    Util.nextInt(0, this.playerSkill.skills.size() - 1)
                );
                
                // Chỉ chọn skill khác TAI_TAO_NANG_LUONG
                if (s.template.id != Skill.TAI_TAO_NANG_LUONG) {
                    selectedSkill = s;
                    break;
                }
                maxTries--;
            }
            
            // Fallback: Nếu không tìm được, chọn skill index 1
            if (selectedSkill == null) {
                selectedSkill = this.playerSkill.skills.get(1);
            }
            
            this.playerSkill.skillSelect = selectedSkill;
            
            // Kiểm tra khoảng cách và tấn công
            if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                // Di chuyển ngẫu nhiên (5% tỷ lệ)
                if (Util.isTrue(5, 20)) {
                    int dir = Util.getOne(-1, 1);
                    this.moveTo(pl.location.x + (dir * 200), pl.location.y);
                }
                
                SkillService.gI().useSkill(this, pl, null, -1, null);
                checkPlayerDie(pl);
                
            } else {
                this.moveToPlayer(pl);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

   private void tangChiSo() {
        long hpMax = this.nPoint.hpMax;
        hpMax += hpMax / 10;
        if (hpMax > 16_070_777) hpMax = 16_070_777;
        this.nPoint.hpMax = (int)hpMax;
        this.nPoint.dame = (int)(hpMax / 10);

        if (this.nPoint.hpMax >= 16_070_777) ;
    }

    @Override
    public void leaveMap() {
        if (this.detu != null) this.detu.leaveMap();

        ChangeMapService.gI().exitMap(this);
        BrolyManager.gI().removeBoss(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        this.dispose();
    }
}
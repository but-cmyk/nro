package models.mob;

import models.map.Zone;
import models.player.Player;
import utils.SkillUtil;
import services.Service;
import services.EffectSkillService;
import utils.Util;
import network.io.Message;
import models.player.NPoint;

public final class MobMe extends Mob {

    private Player player;
    private final long lastTimeSpawn;
    private final int timeSurvive;

    public MobMe(Player player) {
        super();
        this.player = player;
        this.id = (int) player.id;
        int level = player.playerSkill.getSkillbyId(12).point;
        this.tempId = SkillUtil.getTempMobMe(level);
        this.point.maxHp = (int) Math.min(SkillUtil.getHPMobMe(player.nPoint.hpMax, level), 2_000_000_000);
        this.point.dame = (int) Math.min(SkillUtil.getHPMobMe(player.nPoint.getDameAttack(false), level), 2_000_000_000);
        this.point.hp = this.point.maxHp;
        this.zone = player.zone;
        this.lastTimeSpawn = System.currentTimeMillis();
        this.timeSurvive = SkillUtil.getTimeSurviveMobMe(level);
        spawn();
    }

    @Override
    public void update() {
        if (Util.canDoWithTime(lastTimeSpawn, timeSurvive) && this.player.setClothes.pikkoroDaimao != 5) {
            this.mobMeDie();
            this.dispose();
        }
    }

    public void attack(Player pl, Mob mob, boolean miss) {
        Message msg;
        try {
            if (pl != null) {
                int dame = !miss ? this.point.dame : 0;
                boolean isTargetShielding = pl.effectSkill != null && pl.effectSkill.isShielding;
                boolean hasPikkoro = this.player != null && this.player.setClothes != null && this.player.setClothes.pikkoroDaimao == 5;

                // Điều kiện tấn công:
                // 1. Có set Pikkoro Daimao 5 món (được phép kết liễu)
                // 2. Mục tiêu đang bật khiên năng lượng (cho phép pem xuống dưới 2% HP để phá khiên)
                // 3. Cơ chế gốc: máu hiện tại > dame và máu hiện tại > 5% HP tối đa
                boolean canAttack = hasPikkoro
                        || (isTargetShielding && pl.nPoint.hp > 1)
                        || (pl.nPoint.hp > dame && pl.nPoint.hp > pl.nPoint.hpMax * 5 / 100);

                if (canAttack) {
                    // Nếu không có set Pikkoro: giữ lại ít nhất 1 HP để không kết liễu người chơi
                    if (!hasPikkoro && dame >= pl.nPoint.hp) {
                        dame = (int) Math.max(1, pl.nPoint.hp - 1);
                    }

                    int dameHit = pl.injured(this.player, dame, true, true);

                    // Phá vỡ khiên năng lượng nếu mục tiêu dùng khiên bị pem xuống dưới 2% HP tối đa
                    if (isTargetShielding && !pl.isDie() && pl.nPoint.hp <= pl.nPoint.hpMax * 2 / 100) {
                        EffectSkillService.gI().breakShield(pl);
                    }

                    msg = new Message(-95);
                    msg.writer().writeByte(2);
                    msg.writer().writeInt(this.id);
                    msg.writer().writeInt((int) pl.id);
                    msg.writer().writeInt(dameHit);
                    msg.writer().writeInt(NPoint.safeInt(pl.nPoint.hp));
                    Service.gI().sendMessAllPlayerInMap(this.player, msg);
                    msg.cleanup();
                }
            }

            if (mob != null && !mob.isDie()) {
                int dame = !miss ? this.point.dame : 0;
                if (dame > 0) {
                    int mobHpBefore = mob.point.gethp();
                    mob.injured(this.player, dame, true);
                    int realDamage = Math.max(0, mobHpBefore - mob.point.gethp());
                    msg = new Message(-95);
                    msg.writer().writeByte(3);
                    msg.writer().writeInt(this.id);
                    msg.writer().writeInt((int) mob.id);
                    msg.writer().writeInt(mob.point.gethp());
                    msg.writer().writeInt(realDamage > 0 ? realDamage : dame);
                    Service.gI().sendMessAllPlayerInMap(this.player, msg);
                    msg.cleanup();
                }
            }
        } catch (Exception e) {
        }
    }

    //tạo mobme
    public void spawn() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(0);//type
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(this.tempId);
            msg.writer().writeInt(this.point.hp);// hp mob
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void goToMap(Zone zone) {
        if (zone != null) {
            this.removeMobInMap();
            this.zone = zone;
        }
    }

    //xóa mobme khỏi map
    private void removeMobInMap() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(7);//type
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void mobMeDie() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(6);//type
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    @Override
    public synchronized void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        Message msg;
        try {
            if (damage > point.maxHp / 20) {
                damage = point.maxHp / 20;
            }
            point.hp -= damage;
            msg = new Message(-95);
            msg.writer().writeByte(5);//type
            msg.writer().writeInt((int) plAtt.id);
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.template.id); // id skill
            msg.writer().writeInt(id); //mob id
            msg.writer().writeInt((int) damage);
            msg.writer().writeInt(point.hp);
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
            if (isDie()) {
                mobMeDie();
                dispose();
            }
        } catch (Exception e) {
        }
    }

    public void dispose() {
        player.mobMe = null;
        this.player = null;
    }
}

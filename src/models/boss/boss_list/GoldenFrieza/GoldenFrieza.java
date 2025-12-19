package models.boss.boss_list.GoldenFrieza;

import consts.BossID;
import consts.BossStatus;
import models.boss.*;
import consts.ConstPlayer;
import java.util.List;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;
import models.mob.Mob;
import network.io.Message;
import services.map.MapService;
import services.player.PlayerService;
import services.SkillService;
import services.map.ChangeMapService;
import utils.SkillUtil;
import utils.TimeUtil;

public class GoldenFrieza extends Boss {

    private int status;
    private long lastStatusChange;
    private int timeChanges;
    private boolean callDeathBeam;

    // Biến hỗ trợ xử lý Bom không cần Thread
    private boolean isBomActive;
    private long lastTimeBom;

    public GoldenFrieza() throws Exception {
        super(BossID.GOLDEN_FRIEZA, BossesData.GOLDEN_FRIEZA);
    }

    @Override
    public void reward(Player plKill) {
        // Drop đồ xịn
        ItemMap it = new ItemMap(this.zone, 629, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                this.location.y - 24), plKill.id);
        it.options.add(new Item.ItemOption(50, 30));
        it.options.add(new Item.ItemOption(77, 30));
        it.options.add(new Item.ItemOption(103, 30));
        it.options.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
        Service.gI().dropItemMap(this.zone, it);

        // Drop đồ rác (Hạn chế số lượng và rải đều)
        int quantity = Util.nextInt(5, 10); // Giảm số lượng để đỡ lag
        for (int i = 0; i < quantity; i++) {
            int range = (i + 1) * 20;
            int xDrop = this.location.x + (i % 2 == 0 ? range : -range);
            if (xDrop < 50) xDrop = 50;
            if (xDrop > this.zone.map.mapWidth - 50) xDrop = this.zone.map.mapWidth - 50;

            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 987, Util.nextInt(1, 2),
                    xDrop, this.zone.map.yPhysicInTop(xDrop, this.location.y - 24), plKill.id));
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) return 0;

        damage = this.nPoint.subDameInjureWithDeff(damage);

        if (!piercing && effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }

        // Cap damage
        if (damage > 125_000) {
            damage = 125_000;
        }

        this.nPoint.subHP(damage);

        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public void autoLeaveMap() {
        if (!TimeUtil.is21H()) {
            this.leaveMap();
        }
    }

    @Override
    public void joinMap() {
        if (TimeUtil.is21H()) {
            this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
            super.joinMap();
            if (this.zone != null) {
                // Giết quái map an toàn hơn
                if(this.zone.mobs != null){
                    for (Mob mob : this.zone.mobs) {
                        if(!mob.isDie()) mob.injured(this, 2_000_000_000, true);
                    }
                }
                this.zone.isGoldenFriezaAlive = true;
            }
        } else {
            this.changeStatus(BossStatus.REST);
        }
    }

    @Override
    public void attack() {
        // Xử lý nổ Bom tại đây thay vì Thread
        if (isBomActive) {
            handleBomExplosion();
            return; // Đang gồng nổ thì không đánh thường
        }

        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();

            // Logic đổi trạng thái
            if (Util.canDoWithTime(lastStatusChange, timeChanges)) {
                callDeathBeam = false;
                timeChanges = Util.nextInt(5000, 10000);
                lastStatusChange = System.currentTimeMillis();
                status = Util.nextInt(3); // 0: Bom, 1: Gọi đệ, 2: Đánh thường
            }

            try {
                switch (status) {
                    case 0: // Tự sát
                        prepareBom();
                        timeChanges = 5000;
                        break;
                    case 1: // Gọi Death Beam
                        handleSummonDeathBeam();
                        break;
                    default: // Đánh thường
                        handleNormalAttack();
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Logic chuẩn bị nổ
    private void prepareBom() {
        if (this.isBomActive) return;

        this.isBomActive = true;
        this.lastTimeBom = System.currentTimeMillis();
        this.playerSkill.prepareTuSat = true;

        // Gửi hiệu ứng gồng
        try {
            Message msg = new Message(-45);
            msg.writer().writeByte(7);
            msg.writer().writeInt((int) this.id);
            msg.writer().writeShort(104);
            msg.writer().writeShort(2000); // Thời gian gồng
            Service.gI().sendMessAllPlayerInMap(this, msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Logic xử lý nổ (Gọi trong update/attack)
    private void handleBomExplosion() {
        // Sau 2.5s thì nổ
        if (Util.canDoWithTime(this.lastTimeBom, 2500)) {
            this.isBomActive = false;
            this.playerSkill.prepareTuSat = false;

            if (this.zone != null && !MapService.gI().isMapOffline(this.zone.map.mapId)) {
                List<Player> playersMap = this.zone.getNotBosses();
                for (Player pl : playersMap) {
                    if (pl != null && !pl.isDie() && !this.equals(pl)) {
                        pl.injured(this, 2_100_000_000, true, false);
                        PlayerService.gI().sendInfoHpMpMoney(pl);
                        Service.gI().Send_Info_NV(pl);
                    }
                }
            }
            // Reset trạng thái sau khi nổ
            this.status = 2; // Quay về đánh thường
            this.lastStatusChange = System.currentTimeMillis();
        }
    }

    private void handleSummonDeathBeam() {
        if (callDeathBeam) {
            boolean checkDeathBeamDie = true;
            for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                if (boss.bossStatus != BossStatus.REST) {
                    checkDeathBeamDie = false;
                    break;
                }
            }
            if (checkDeathBeamDie) {
                status = 2;
                lastStatusChange = System.currentTimeMillis();
                timeChanges = 30000;
            }
            return;
        }
        callDeathBeam = true;

        // Hồi sinh đệ tử
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if (boss.bossStatus == BossStatus.REST) {
                // Truyền người dùng skill (Frieza) cho đệ để đệ tính sát thương
                if (boss instanceof DeathBeam) {
                    ((DeathBeam) boss).playerUseSkill = this;
                }
                boss.changeStatus(BossStatus.RESPAWN);
            }
        }
        timeChanges = 15000;
    }

    private void handleNormalAttack() {
        timeChanges = 30000;
        Player pl = getPlayerAttack();
        if (pl == null || pl.isDie()) return;

        this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

        if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
            // Logic di chuyển ảo diệu
            if (Util.isTrue(5, 20)) {
                int rangeX = SkillUtil.isUseSkillChuong(this) ? Util.nextInt(20, 200) : Util.nextInt(10, 40);
                this.moveTo(pl.location.x + (Util.getOne(-1, 1) * rangeX),
                        pl.location.y - (Util.nextInt(10) % 2 == 0 ? 0 : Util.nextInt(0, 50)));
            }
            SkillService.gI().useSkill(this, pl, null, -1, null);
            checkPlayerDie(pl);
        } else {
            if (Util.isTrue(1, 2)) {
                this.moveToPlayer(pl);
            }
        }
    }

    @Override
    public void leaveMap() {
        this.zone.isGoldenFriezaAlive = false;
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }
}
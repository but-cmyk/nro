package models.boss.boss_list.MajinBuu14H;

import models.boss.Boss;
import managers.boss.FinalBossManager;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import static consts.BossType.FINAL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;
import server.ServerNotify;
import services.ItemTimeService;
import services.SkillService;
import services.TaskService;
import services.map.ChangeMapService;
import models.skill.Skill;
import utils.SkillUtil;

public class Mabu2H extends Boss {

    private long lastTimeEat;
    private long lastTimeUseSkill;
    private long timeUseSkill;
    public List<Player> maBuEat = new java.util.concurrent.CopyOnWriteArrayList<>();

    public Mabu2H() throws Exception {
        super(FINAL, BossID.MABU, BossesData.MABU, BossesData.SUPER_BU, BossesData.BU_TENK, BossesData.BU_HAN, BossesData.KID_BU);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            this.zone = zoneFinal;
        }
        ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
        this.changeStatus(BossStatus.ACTIVE);
    }

    private void eatPlayersInTheMap() {
        if (this.zone == null) return;
        int numPlayers = 0;
        for (Player pl : this.zone.getPlayers()) {
            if (pl != null && !pl.isDie() && Util.isTrue(1, 5)) {
                pl.isMabuHold = true;
                Service.gI().sendMabuEat(this, pl);
                this.maBuEat.add(pl);
                numPlayers++;
            }
        }
        if (numPlayers > 0) {
            this.chat("Măm măm");
        }
    }

    private void petrifyPlayersInTheMap() {
        if (this.zone == null) return;
        for (Player pl : this.zone.getNotBosses()) {
            if (pl != null && !pl.isDie()) {
                if (Util.isTrue(1, 5)) {
                    this.chat("Úm ba la xì bùa");
                    EffectSkillService.gI().setSocola(pl, System.currentTimeMillis(), 30000);
                    Service.gI().Send_Caitrang(pl);
                    ItemTimeService.gI().sendItemTime(pl, 4133, 30);
                }
            }
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100)) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }

                // Logic Mabu Ăn thịt / Hóa đá
                if (Util.canDoWithTime(lastTimeEat, 10000)) {
                    eatPlayersInTheMap();
                    if (this.currentLevel == 0) {
                        petrifyPlayersInTheMap();
                    }
                    this.lastTimeEat = System.currentTimeMillis();
                }

                // Logic Mabu dùng skill đặc biệt (Trói/Kẹo)
                if (this.currentLevel > 0) {
                    if (Util.canDoWithTime(lastTimeUseSkill, timeUseSkill)) {
                        Service.gI().sendMabuAttackSkill(this);
                        lastTimeUseSkill = System.currentTimeMillis();
                        timeUseSkill = Util.nextInt(5000, 10000);
                        return;
                    }
                }

                // Logic đánh thường
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)), pl.location.y);
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)), pl.location.y);
                        }
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // Tỉ lệ rơi đồ: 10%
        if (Util.isTrue(10, 100)) {
            int randomChance = Util.nextInt(0, 99);
            int itemId;

            // Logic chọn ID item dựa trên tỉ lệ
            if (randomChance < 50) {
                itemId = 556; // 50%
            } else if (randomChance < 80) {
                itemId = 558; // 30%
            } else {
                itemId = 560; // 20%
            }

            ItemMap item = new ItemMap(this.zone, itemId, 1,
                    this.location.x,
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                    plKill.id);

            setItemOptions(item, itemId);
            Service.gI().dropItemMap(this.zone, item);
        }
    }

    private void setItemOptions(ItemMap item, int itemType) {
        int hpStat, atkStat;

        switch (itemType) {
            case 556:
                hpStat = Util.nextInt(50, 70);
                atkStat = Util.nextInt(10000, 12000);
                break;
            case 558:
                hpStat = Util.nextInt(50, 60);
                atkStat = Util.nextInt(7050, 8460);
                break;
            default: // 560
                hpStat = Util.nextInt(45, 55);
                atkStat = Util.nextInt(6900, 8280);
                break;
        }

        // Add options: 22 (HP), 27 (SD), 21 (Yêu cầu TV), 209 (Đồ Boss)
        item.options.add(new Item.ItemOption(22, hpStat));
        item.options.add(new Item.ItemOption(27, atkStat));
        item.options.add(new Item.ItemOption(21, 15));
        item.options.add(new Item.ItemOption(209, 15));
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(10, 100)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (plAtt.isPl() && Util.isTrue(1, 5)) {
                plAtt.fightMabu.changePercentPoint((byte) 1);
            }

            damage = this.nPoint.subDameInjureWithDeff(damage);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }

            // Logic Kid Buu (Form cuối) chỉ nhận sát thương kết liễu từ Quả cầu kênh khi, Makankosappo hoặc Tự sát
            if (this.currentLevel == this.data.length - 1) {
                if (plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null
                        && plAtt.playerSkill.skillSelect.template != null) {
                    int skillId = plAtt.playerSkill.skillSelect.template.id;
                    if (skillId != Skill.QUA_CAU_KENH_KHI && skillId != Skill.MAKANKOSAPPO && skillId != Skill.TU_SAT) {
                        if (damage >= this.nPoint.hp) {
                            damage = 0;
                        }
                    }
                } else if (damage >= this.nPoint.hp) {
                    damage = 0;
                }
            }

            if (damage >= 30000000) {
                damage = 30000000 + Util.nextInt(-10000, 10000);
            }

            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                // Giết luôn con trong bụng nếu Mabu chết
                Boss boss = FinalBossManager.gI().getBossById(BossID.SUPERBU, 128, this.zone.zoneId);
                if (boss != null) {
                    boss.changeStatus(BossStatus.DIE);
                }
                die(plAtt);
            }

            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            // Nhả hết người chơi trong bụng ra
            if(!this.maBuEat.isEmpty()){
                List<Player> players = new ArrayList<>(this.maBuEat); // Copy list để tránh ConcurrentModificationException
                for (Player pl : players) {
                    if (pl != null && pl.zone != null && pl.zone.map.mapId == 128) {
                        ChangeMapService.gI().changeMap(pl, 127, this.zone.zoneId, -1, 312);
                    }
                }
                this.maBuEat.clear();
            }

            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }
}
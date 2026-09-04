package models.boss.boss_list.Cell;

import consts.ConstPlayer;
import models.boss.Boss;
import consts.BossID;
import consts.BossStatus;
import models.boss.BossesData;
import models.item.Item;
import models.map.ItemMap;
import models.player.Player;
import services.EffectSkillService;
import services.Service;
import services.TaskService;
import utils.Util;
import services.player.PlayerService;
import utils.Logger;

public class SieuBoHung extends Boss {

    private long st;
    public boolean callCellCon;

    private final String text[] = {"Thưa quý vị và các bạn, đây đúng là trận đấu trời long đất lở", "Vượt xa mọi dự đoán của chúng tôi", "Eo ơi toàn thân lão Xên bốc cháy kìa"};
    private long lastTimeChat;
    private long lastTimeMove;
    private int indexChat = 0;

    public SieuBoHung() throws Exception {
        super(BossID.SIEU_BO_HUNG, BossesData.SIEU_BO_HUNG_1, BossesData.SIEU_BO_HUNG_2);
    }

    @Override
    protected void resetBase() {
        super.resetBase();
        this.callCellCon = false;
    }

    public void callCellCon() {
        try {
            this.changeStatus(BossStatus.AFK);
            this.changeToTypeNonPK();
            this.recoverHP();
            this.callCellCon = true;
            this.chat("Hãy đấu với 7 đứa con của ta, chúng đều là siêu cao thủ");
            
            server.GameLoopManager.gI().schedule(() -> {
                try {
                    if (!this.isDie()) {
                        this.chat("Cứ chưởng tiếp đi haha");
                    }
                } catch (Exception e) {
                    Logger.logException(SieuBoHung.class, e);
                }
            }, 2000);
            
            server.GameLoopManager.gI().schedule(() -> {
                try {
                    if (!this.isDie()) {
                        this.chat("Liệu mà giữ mạng đấy");
                    }
                } catch (Exception e) {
                    Logger.logException(SieuBoHung.class, e);
                }
            }, 4000);
            
            server.GameLoopManager.gI().schedule(() -> {
                try {
                    if (this.zone != null && this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
                        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                            if (boss != null) {
                                switch ((int) boss.id) {
                                    case BossID.XEN_CON_1 -> boss.changeStatus(BossStatus.RESPAWN);
                                    case BossID.XEN_CON_2 -> boss.changeStatus(BossStatus.RESPAWN);
                                    case BossID.XEN_CON_3 -> boss.changeStatus(BossStatus.RESPAWN);
                                    case BossID.XEN_CON_4 -> boss.changeStatus(BossStatus.RESPAWN);
                                    case BossID.XEN_CON_5 -> boss.changeStatus(BossStatus.RESPAWN);
                                    case BossID.XEN_CON_6 -> boss.changeStatus(BossStatus.RESPAWN);
                                    case BossID.XEN_CON_7 -> boss.changeStatus(BossStatus.RESPAWN);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.logException(SieuBoHung.class, e, "Error spawning Cell Con");
                }
            }, 6000);
        } catch (Exception e) {
            Logger.logException(SieuBoHung.class, e, "Error in callCellCon");
        }
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
    }

    @Override
    public void reward(Player plKill) {
        plKill.effect.addPointTrumSanBoss();
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (Util.isTrue(10, 100)) {
            ItemMap it = new ItemMap(this.zone, 16, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (Util.isTrue(10, 100)) {
            ItemMap it = new ItemMap(this.zone, 17, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        if (plKill.playerTask.taskdh.Hagucboss < 30) {
            int required = 30;
            int percentDone = (int) ((double) plKill.playerTask.taskdh.Hagucboss / required * 100);
            plKill.playerTask.taskdh.Hagucboss++;
            plKill.playerTask.taskdh.ResetTime = System.currentTimeMillis();
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (prepareBom) {
            return 0;
        }
        if (!this.callCellCon && damage >= this.nPoint.hp) {
            if (Util.isTrue(1, 2)) {
                this.callCellCon();
                return 0;
            } else {
                this.callCellCon = true;
            }
        }
        if (!this.isDie()) {
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 2;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                setBom(plAtt);
                return 0;
            }
            return (int) damage;
        } else {
            return 0;
        }

    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        this.mc();
        if (this.currentLevel > 0) {
            if (this.bossStatus == BossStatus.AFK) {
                this.changeStatus(BossStatus.ACTIVE);
            }
        }
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    public void mc() {
        Player mc = zone.getNpc();
        if (mc != null) {
            if (Util.canDoWithTime(lastTimeChat, 3000)) {
                String textchat = text[indexChat];
                Service.gI().chat(mc, textchat);
                indexChat++;
                if (indexChat == text.length) {
                    indexChat = 0;
                    lastTimeChat = System.currentTimeMillis() + 7000;
                } else {
                    lastTimeChat = System.currentTimeMillis();
                }
            }

            if (Util.canDoWithTime(lastTimeMove, 15000)) {
                if (Util.isTrue(2, 3)) {
                    int x = this.location.x + Util.nextInt(-100, 100);
                    int y = x > 156 && x < 611 ? 288 : 312;
                    PlayerService.gI().playerMove(mc, x, y);
                }
                lastTimeMove = System.currentTimeMillis();
            }
        }
    }

}

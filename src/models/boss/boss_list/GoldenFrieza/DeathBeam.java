package models.boss.boss_list.GoldenFrieza;

import models.boss.Boss;
import consts.BossStatus;
import models.boss.BossesData;
import static consts.BossType.SKILLSUMMONED;
import models.player.Player;
import services.map.MapService;
import services.Service;
import services.map.ChangeMapService;
import utils.Util;

public class DeathBeam extends Boss {

    private long st;
    private Player playerAtt;
    public Player playerUseSkill; // Người triệu hồi (Frieza)
    private boolean leaveMap;
    private long lastTimeMove;
    private boolean playerKill;
    private long lastTimeUpdate;

    private final int lifeTime; // Thời gian tồn tại (ms)

    // Constructor chung cho cả 3 loại DeathBeam
    public DeathBeam(int bossID, int lifeTime) throws Exception {
        super(SKILLSUMMONED, bossID, BossesData.DEATH_BEAM);
        this.lifeTime = lifeTime;
    }

    @Override
    public void joinMap() {
        st = System.currentTimeMillis();
        this.zone = this.parentBoss.zone;
        // Xuất hiện ngẫu nhiên quanh Frieza
        ChangeMapService.gI().changeMap(this, this.zone,
                this.parentBoss.location.x + Util.nextInt(-100, 100), 300);
        Service.gI().sendFlagBag(this);
        playerAtt = this.getPlayerAttack();
        leaveMap = false;
        playerKill = false;
        this.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public void active() {
        this.attack();
    }

    @Override
    public void afk() {
        if (Util.canDoWithTime(lastTimeUpdate, 3000)) {
            // Thử tìm mục tiêu mới
            this.playerAtt = this.getPlayerAttack();
            if (this.playerAtt != null && !this.playerAtt.isDie()) {
                this.changeStatus(BossStatus.ACTIVE);
            }
            lastTimeUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void autoLeaveMap() {
        // Tự rời map sau lifeTime
        if (!leaveMap) {
            if (Util.canDoWithTime(st, this.lifeTime)) {
                leaveMap = true;
            }
        } else {
            // Hiệu ứng bay lên trời rồi biến mất
            if (Util.canDoWithTime(lastTimeMove, 100)) { // Tăng tốc độ bay lên chút cho mượt
                this.location.y -= 30;
                this.moveTo(this.location.x, this.location.y);
                this.lastTimeMove = System.currentTimeMillis();
                if (this.location.y < 0) {
                    this.leaveMap();
                }
            }
        }
    }

    @Override
    public void moveTo(int x, int y) {
        this.location.x = x;
        this.location.y = y;
        MapService.gI().sendPlayerMove(this);
    }

    @Override
    public void moveToPlayer(Player pl) {
        if (pl != null && pl.location != null) {
            int dis = Math.abs(this.location.x - pl.location.x);
            int dir = this.location.x - pl.location.x > 0 ? -1 : 1;
            int speed = 30;

            int x = this.location.x + dir * speed;
            if (dis < speed) {
                x = pl.location.x;
            }
            moveTo(x, pl.location.y);
        }
    }

    @Override
    public void attack() {
        if (leaveMap) return;

        if (playerAtt == null || playerAtt.location == null || playerAtt.isDie() || !playerAtt.zone.equals(this.zone)) {
            this.changeStatus(BossStatus.AFK);
            return;
        }

        if (Util.canDoWithTime(lastTimeMove, 500)) {
            this.moveToPlayer(playerAtt);
            this.lastTimeMove = System.currentTimeMillis();
        }

        // Tự sát khi chạm vào người chơi
        if (Math.abs(this.location.x - playerAtt.location.x) < 20 && Math.abs(this.location.y - playerAtt.location.y) < 20) {
            if (Util.canDoWithTime(st, 500)) { // Delay 1 chút sau khi xuất hiện mới nổ
                Service.gI().setPos(this, playerAtt.location.x, playerAtt.location.y);
                if (!playerKill) {
                    setDie();
                    playerKill = true;
                    leaveMap = true;
                }
            }
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        return 0; // Bất tử
    }

    @Override
    public void setDie() {
        // Gây dame cực lớn cho người chơi
        if (playerAtt != null && !playerAtt.isDie()) {
            // Dùng playerUseSkill (Frieza) để gây dame nếu có, không thì tự gây dame
            Player attacker = (this.playerUseSkill != null) ? this.playerUseSkill : this;
            playerAtt.injured(attacker, 2_100_000_000, true, false);
        }
    }
}
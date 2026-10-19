package models.tournament;


import managers.tournament.The23rdMartialArtCongressManager;
import services.tournament.The23rdMartialArtCongressService;
import models.boss.boss_list.The23rdMartialArtCongress.TauPayPay;
import models.boss.boss_list.The23rdMartialArtCongress.PonPut;
import models.boss.boss_list.The23rdMartialArtCongress.LiuLiu;
import models.boss.boss_list.The23rdMartialArtCongress.ODo;
import models.boss.boss_list.The23rdMartialArtCongress.JackyChun;
import models.boss.boss_list.The23rdMartialArtCongress.ChaPa;
import models.boss.boss_list.The23rdMartialArtCongress.ThienXinHang;
import models.boss.boss_list.The23rdMartialArtCongress.Yamcha;
import models.boss.boss_list.The23rdMartialArtCongress.SoiHecQuyn;
import models.boss.boss_list.The23rdMartialArtCongress.ChanXu;
import models.boss.boss_list.The23rdMartialArtCongress.Xinbato;
import consts.ConstPlayer;
import models.boss.Boss;
import consts.BossStatus;
import models.player.Player;
import services.EffectSkillService;
import services.ItemTimeService;
import services.player.PlayerService;
import services.Service;
import models.map.Zone;
import models.matches.pvp.DHVT;

public class The23rdMartialArtCongress {

    private Player player;
    private Boss boss;
    private Player npc;
    private int time;
    private int round;
    private int timeWait;
    public boolean endChallenge;
    private Zone zone;

    public Player getPlayer() { return this.player; }
    public void setPlayer(Player player) { this.player = player; }

    public Boss getBoss() { return this.boss; }
    public void setBoss(Boss boss) { this.boss = boss; }

    public Player getNpc() { return this.npc; }
    public void setNpc(Player npc) { this.npc = npc; }

    public int getTime() { return this.time; }
    public void setTime(int time) { this.time = time; }

    public int getRound() { return this.round; }
    public void setRound(int round) { this.round = round; }

    public int getTimeWait() { return this.timeWait; }
    public void setTimeWait(int timeWait) { this.timeWait = timeWait; }

    public Zone getZone() { return this.zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public void update() {

        if (player == null || player.zone == null || !player.zone.equals(zone)) {
            this.endChallenge();
            return;
        }

        if (timeWait > 0) {
            switch (timeWait) {
                case 13:
                    if (round == 4 || round == 6 || round == 8 || round == 10) {
                        Service.gI().releaseCooldownSkill(player);
                    }
                    if (boss != null) {
                        EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 14000);
                        Service.gI().sendInfoPlayerEatPea(boss);
                    }
                    EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 14000);
                    ItemTimeService.gI().sendItemTime(player, 3779, 11000 / 1000);
                    player.nPoint.hp = player.nPoint.hpMax;
                    player.nPoint.mp = player.nPoint.mpMax;
                    Service.gI().Send_Info_NV(player);
                    break;
                case 11:
                    PlayerService.gI().playerMove(npc, 335, 264);
                    Service.gI().chat(npc, "Trận đấu giữa " + player.name + " vs " + (boss != null && boss.name != null ? boss.name : "Đối thủ") + " sắp diễn ra"); // 7 STUN
                    break;
                case 8:
                    Service.gI().chat(npc, "Xin quý vị khán giả cho 1 tràng pháo tay cổ vũ cho 2 đấu thủ nào");
                    break;
                case 6:
                    Service.gI().chat(npc, "Mọi người hãy ổn định chỗ ngồi, trận đấu sẽ bắt đầu sau 3 giây nữa");
                    break;
                case 5:
                    Service.gI().chat(npc, "3");
                    break;
                case 4:
                    Service.gI().chat(npc, "2");
                    break;
                case 3:
                    Service.gI().chat(npc, "1");
                    break;
                case 2:
                    Service.gI().chat(npc, "Trận đấu bắt đầu");
                    PlayerService.gI().playerMove(npc, npc.location.x, 360);
                    EffectSkillService.gI().removeStun(player);
                    if (boss != null) {
                        EffectSkillService.gI().removeStun(boss);
                    }
                    ItemTimeService.gI().removeItemTime(player, 3779);
                    Service.gI().chat(player, "OK");
                    if (boss != null) {
                        Service.gI().chat(boss, "OK");
                    }
                    break;
                case 1:
                    if (boss != null) {
                        The23rdMartialArtCongressService.gI().sendTypePK(player, boss);
                        boss.changeStatus(BossStatus.ACTIVE);
                        new DHVT(player, boss);
                    }
                    PlayerService.gI().changeAndSendTypePK(this.player, ConstPlayer.PK_PVP);
                    setTime(129);
                    break;
            }
            timeWait--;
            return;
        }

        if (time > 0) {
            time--;
            if (player.isDie() || player.lostByDeath) {
                die();
                return;
            }
            if (player != null && player.location != null && player.isPKDHVT && !player.isDie() && player.zone != null) {
                if (boss != null && boss.isDie()) {
                    round++;
                    services.TaskService.gI().checkDoneTaskDHVT(player);
                    boss.leaveMap();
                    toTheNextRound();
                    reward();
                }
                if (player.location.y > 264 && !(player.location.x > 150 && player.location.x < 630)) {
                    leave();
                    return;
                }
                if (!player.isPKDHVT) {
                    leave();
                }
            } else {
                if (boss != null) {
                    boss.leaveMap();
                }
                The23rdMartialArtCongressManager.gI().remove(this);
            }

        } else {
            timeOut();
        }
    }

    public void toTheNextRound() {
        try {
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            if (this.boss != null) {
                this.boss.leaveMap();
                this.boss = null;
            }
            Boss bss = null;
            switch (round) {
                case 0:
                    bss = new SoiHecQuyn(player);
                    break;
                case 1:
                    bss = new ODo(player);
                    break;
                case 2:
                    bss = new Xinbato(player);
                    break;
                case 3:
                    bss = new ChaPa(player);
                    break;
                case 4:
                    bss = new PonPut(player);
                    break;
                case 5:
                    bss = new ChanXu(player);
                    break;
                case 6:
                    bss = new TauPayPay(player);
                    break;
                case 7:
                    bss = new Yamcha(player);
                    break;
                case 8:
                    bss = new JackyChun(player);
                    break;
                case 9:
                    bss = new ThienXinHang(player);
                    break;
                case 10:
                    bss = new LiuLiu(player);
                    break;
                case 11:
                    champion();
                    return;
                default:
                    return;
            }
            setBoss(bss);
            if (bss != null) {
                if (bss.zone == null && this.zone != null) {
                    bss.zone = this.zone;
                }
                bss.joinMap();
            }
            Service.gI().setPos(player, 335, 264);
            setTimeWait(13);
        } catch (Exception e) {
            utils.Logger.logException(The23rdMartialArtCongress.class, e, "Lỗi tại toTheNextRound DHVT 23");
        }
    }

    public void die() {
        player.lostByDeath = false;
        Service.gI().sendThongBao(player, "Thua rồi");
        Service.gI().chat(npc, (boss != null && boss.name != null ? boss.name : "Đối thủ") + " đã chiến thắng");
        if (player.zone != null) {
            endChallenge();
        }
    }

    private void timeOut() {
        Service.gI().sendThongBao(player, "Thua rồi");
        Service.gI().chat(npc, "Hết thời gian thi đấu");
        Service.gI().chat(npc, (boss != null && boss.name != null ? boss.name : "Đối thủ") + " đã chiến thắng");
        endChallenge();
    }

    private void champion() {
        if (player != null) {
            services.TaskService.gI().checkDoneTaskDHVT(player);
        }
        Service.gI().sendThongBao(player, "Chúc mừng " + player.name + " vừa vô địch giải");
        endChallenge();
    }

    public void leave() {
        if (player.levelWoodChest != 11) {
            setTime(0);
            EffectSkillService.gI().removeStun(player);
            Service.gI().sendThongBao(player, "Thua rồi");
            Service.gI().chat(npc, "Đối thủ đã rơi khỏi võ đài, " + (boss != null && boss.name != null ? boss.name : "Đối thủ") + " đã chiến thắng");
            Service.gI().chat(npc, (boss != null && boss.name != null ? boss.name : "Đối thủ") + " đã chiến thắng");
            endChallenge();
        }
    }

    private void reward() {
        if (player.levelWoodChest < round) {
            player.levelWoodChest = round;
        }
    }

    public void endChallenge() {
        if (!endChallenge) {
            endChallenge = true;
            reward();
            if (player.zone != null) {
                player.nPoint.hp = player.nPoint.hpMax;
                player.nPoint.mp = player.nPoint.mpMax;
                Service.gI().Send_Info_NV(player);
//                PlayerService.gI().hoiSinh(player);
            }
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            if (player != null && player.zone != null && player.zone.map.mapId == 129) {
                Service.gI().setPos(player, player.location.x, 408);
            }
            player.isPKDHVT = false;
            Service.gI().sendPlayerVS(player, null, (byte) 0);
            if (boss != null) {
                boss.leaveMap();
            }
            zone = null;
            The23rdMartialArtCongressManager.gI().remove(this);
        }
    }
}

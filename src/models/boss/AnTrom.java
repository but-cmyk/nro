package models.boss;

import consts.BossID;
import consts.ConstPlayer;
import java.util.ArrayList;
import java.util.List;
import models.item.Item;
import models.map.ItemMap;
import models.map.Zone;
import models.player.Player;
import models.skill.Skill;
import server.Client;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.InventoryService;
import utils.Util;

public class AnTrom extends Boss {

    private long lastTimeAnTrom;
    private long lastTimeJoinMap;
    private long goldAnTrom;
    private int stealsInCurrentMap = 0;
    private static final long TIME_CHANGE_MAP = 600000; // Tăng từ 5p lên 10p
    private List<Item> ITEM;
    private long lastTimeAttack;
    private long lastTimeTargetPlayer;
    private int timeTargetPlayer;
    private int stolenItemCount;
    private boolean movedAway;
    private long moveAwayTime;
    private static final long TIME_MOVE_AWAY = 1500;
    private static final long TIME_RETURN = 5000;
    private long lastChatTime = 0;
    private static final long CHAT_COOLDOWN = 5000;
    private long moveAwayStartTime = 0;
    private boolean isMovingAway = false;

    public AnTrom() throws Exception {
        super(BossID.AN_TROM, new BossData(
                "Ăn trộm " + Util.nextInt(50, 100),
                ConstPlayer.TRAI_DAT,
                new short[]{201, 202, 203, -1, -1, -1},
                1,
                new int[]{100},
                new int[]{3, 4, 6, 27, 28, 29, 30,
                    9, 11, 12, 34, 33, 32, 31,
                    16, 17, 18, 19, 37, 38, 36, 35},
                new int[][]{
                    {Skill.THAI_DUONG_HA_SAN, 3, 20000}},
                new String[]{"|-1|Tới giờ làm việc, lụm lụm", "|-1|Cảm giác mình vào phải khu người nghèo :))"},
                new String[]{"|-1|Ái chà vàng vàng", "|-1|Không làm vẫn có ăn hẹ hẹ", "|-2|Giám ăn trộm giữa ban ngày thế à", "|-2|Cút ngay không là ăn đòn"},
                new String[]{"|-1|Híc lần sau ta sẽ cho ngươi phá sản",
                    "|-2|Chừa thói ăn trộm nghe chưa"},
                900 // Tăng thời gian hồi sinh từ 300s (5 phút) lên 900s (15 phút)
        ));
        this.ITEM = new ArrayList<>();
        lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
        this.moveAwayTime = 0;
        this.movedAway = false;
        this.stealsInCurrentMap = 0;
    }

    @Override
    protected void notifyJoinMap() {
    }

    @Override
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0, this.data[this.currentLevel].getMapJoin().length - 1)];
        models.map.Map map = MapService.gI().getMapById(mapId);
        if (map != null && !map.zones.isEmpty()) {
            return map.zones.get(Util.nextInt(0, map.zones.size() - 1));
        }
        return MapService.gI().getMapById(3).zones.get(0);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            damage = Util.nextInt(1, 3);
            moveAwayFromPlayer(plAtt);
            this.movedAway = true;
            moveAwayTime = System.currentTimeMillis();
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            SkillService.gI().useSkill(this, plAtt, mobMe, -1, null);
            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 500) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                if (Util.getDistance(this, pl) <= 40) {
                    if (!Util.canDoWithTime(this.lastTimeAnTrom, 30000) || goldAnTrom > 10_000_000_000L) {
                        return;
                    }

                    if (pl.inventory.gold < 5_000_000) {
                        chatWithCooldown("Nghèo thế, tha cho đấy!");
                        return;
                    }

                    int gold = Util.nextInt(1000, 3000);
                    pl.inventory.gold -= gold;
                    goldAnTrom += gold;
                    this.stolenItemCount += gold;
                    this.lastTimeAnTrom = System.currentTimeMillis();
                    Service.gI().stealMoney(pl, -gold);
                    chatWithCooldown("Trộm được " + Util.powerToString(gold) + " Vàng rồi! Chuồn thôi!");
                    moveAwayFromPlayer(pl);
                    stealsInCurrentMap++;
                    if (stealsInCurrentMap >= 3) {
                        // Cho Ăn trộm ở lại map ít nhất 3 phút nữa rồi mới đổi map thay vì nhảy ngay lập tức
                        if (lastTimeJoinMap - System.currentTimeMillis() > 180000) {
                            lastTimeJoinMap = System.currentTimeMillis() + 180000;
                        }
                    }
                } else {
                    if (this.movedAway && System.currentTimeMillis() - moveAwayTime > TIME_RETURN) {
                        this.moveToPlayer(pl);
                        this.movedAway = false;
                    } else if (Util.isTrue(1, 4)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    private void moveAwayFromPlayer(Player player) {
        int safeDistance = 600;
        int currentDistance = Util.getDistance(this, player);
        if (currentDistance > safeDistance && isMovingAway) {
            return;
        }
        int dx = this.location.x - player.location.x;
        int dy = this.location.y - player.location.y;
        double angle = Math.atan2(dy, dx);
        int moveSpeed = 180;
        int newX = this.location.x + (int) (moveSpeed * Math.cos(angle));
        int newY = this.location.y + (int) (moveSpeed * Math.sin(angle));
        this.moveTo(newX, newY);
        if (!isMovingAway) {
            moveAwayStartTime = System.currentTimeMillis();
            isMovingAway = true;
        }
    }

    private void returnToOriginalPosition() {
        long moveAwayDuration = System.currentTimeMillis() - moveAwayStartTime;
        if (moveAwayDuration >= 60000) {
            this.moveTo(this.location.x, this.location.y);
            isMovingAway = false;
        }
    }

    @Override
    public void reward(Player plKill) {
        if (this.stolenItemCount > 0) {
            for (int i = -10; i <= 100; i += 10) {
                int goldForThisItem = this.stolenItemCount / 15;
                ItemMap item = new ItemMap(this.zone, 190, goldForThisItem, this.location.x + i, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
                Service.gI().dropItemMap(this.zone, item);
            }
            this.ITEM.clear();
            this.stolenItemCount = 0;
        }
        if (plKill.playerTask.taskdh.AnTrom < 30) {
            int percentDone = (int) ((double) plKill.playerTask.taskdh.AnTrom / 30 * 100);
            plKill.playerTask.taskdh.AnTrom++;
            plKill.playerTask.taskdh.ResetTime = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
    }

    private boolean isSafeMap(int mapId) {
        if (mapId == 0 || mapId == 7 || mapId == 14) return true; // Làng Aru, Mori, Kakarot
        if (mapId >= 21 && mapId <= 26) return true; // Nhà riêng và trạm tàu vũ trụ 3 hành tinh
        if (mapId == 5 || mapId == 13 || mapId == 20) return true; // Đảo Kame, Guru, Bunma
        if (mapId >= 39 && mapId <= 62) return true; // Tương lai, địa ngục, phó bản
        if (mapId >= 111 && mapId <= 130) return true; // Mabu, gas, doanh trại
        if (mapId == 102 || mapId == 103) return true;
        return MapService.gI().isMapDoanhTrai(mapId) 
                || MapService.gI().isMapMaBu(mapId) 
                || MapService.gI().isMapBlackBallWar(mapId)
                || MapService.gI().isMapPhoBan(mapId);
    }

    private boolean zoneHasAnTrom(Zone z) {
        if (z == null) return false;
        List<Player> bosses = z.getBosses();
        if (bosses == null) return false;
        for (int i = 0; i < bosses.size(); i++) {
            Player b = bosses.get(i);
            if (b != null && b != this && b instanceof AnTrom && !b.isDie()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (isMovingAway) {
            returnToOriginalPosition();
        }
        if (this.zone != null && System.currentTimeMillis() > this.lastTimeJoinMap) {
            Zone targetZone = null;
            List<Player> players = Client.gI().getPlayers();
            int playerCount = players.size();
            // 10% cơ hội tìm đến map dã ngoại của một player, 90% đi ngẫu nhiên map dã ngoại
            if (Util.isTrue(1, 10) && playerCount > 0) {
                Player randomPlayer = players.get(Util.nextInt(playerCount));
                if (randomPlayer != null && randomPlayer.zone != null 
                        && randomPlayer.zone.isKhongCoTrongTaiTrongKhu() 
                        && !isSafeMap(randomPlayer.zone.map.mapId)
                        && !zoneHasAnTrom(randomPlayer.zone)
                        && randomPlayer.id != -1000000) {
                    targetZone = randomPlayer.zone;
                }
            }
            if (targetZone == null) {
                Zone randomZone = getMapJoin();
                if (randomZone != null && !zoneHasAnTrom(randomZone) && !isSafeMap(randomZone.map.mapId)) {
                    targetZone = randomZone;
                }
            }
            if (targetZone != null && targetZone != this.zone) {
                lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
                this.stealsInCurrentMap = 0;
                ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
                ChangeMapService.gI().exitMap(this);
                this.zone = targetZone;
                this.location.x = Util.nextInt(Math.max(100, zone.map.mapWidth - 100));
                this.location.y = zone.map.yPhysicInTop(this.location.x, 100);
                this.joinMap();
            }
            if (this.zone != null) {
                if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
                    this.playerTarger = this.zone.getRandomPlayerInMap();
                    this.lastTimeTargetPlayer = System.currentTimeMillis();
                    this.timeTargetPlayer = Util.nextInt(5000, 10000);
                }
            }
        }
    }

    private void chatWithCooldown(String text) {
        if (Util.canDoWithTime(this.lastChatTime, CHAT_COOLDOWN)) {
            this.chat(text);
            this.lastChatTime = System.currentTimeMillis();
        }
    }
}

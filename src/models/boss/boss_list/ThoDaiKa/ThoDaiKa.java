package models.boss.boss_list.ThoDaiKa;

import consts.BossID;
import static consts.BossType.HALLOWEEN_EVENT;
import consts.ConstPlayer;
import models.boss.*;
import models.item.Item;
import models.map.ItemMap;
import models.map.Zone;
import models.player.Player;
import models.skill.Skill;
import server.Client;
import services.EffectSkillService;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.map.MapService;
import utils.SkillUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ThoDaiKa extends Boss {

    // Constants
    private static final long TIME_CHANGE_MAP = 180000;
    private static final long TIME_MOVE_AWAY = 1500;
    private static final long TIME_RETURN = 5000;
    private static final long CHAT_COOLDOWN = 5000;
    private static final long MOVE_AWAY_DURATION = 60000;
    private static final int SAFE_DISTANCE = 600;
    private static final int MOVE_SPEED = 180;
    private static final int MAX_MAP_ID = 44;
    private static final int[] FORBIDDEN_MAP_IDS = {51, 113, 129};
    private static final int MAX_PLAYERS_IN_ZONE = 2;
    
    // Instance variables
    private long lastTimeJoinMap;
    private List<Item> itemList;
    private long lastTimeAttack;
    private long lastTimeTargetPlayer;
    private int timeTargetPlayer;
    private boolean movedAway;
    private long moveAwayTime;
    private long lastChatTime = 0;
    private long moveAwayStartTime = 0;
    private boolean isMovingAway = false;
    private int originalX, originalY; // Store original position

    public ThoDaiKa() throws Exception {
        super(BossID.THO_DAI_KA, new BossData(
                "Thỏ Đại Ka " + Util.nextInt(50, 100),
                ConstPlayer.TRAI_DAT,
                new short[]{403, 404, 405, -1, -1, -1},
                1,
                new int[]{100},
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 79, 80, 81, 82, 83, 84, 92, 93, 94, 96, 97, 98, 99, 100, 102, 103, 104, 105, 106, 107, 108, 109, 110}, //map join
                new int[][]{
                    {Skill.THAI_DUONG_HA_SAN, 3, 20000}},
                new String[]{"|-1|Carrot-chan! Ta đến để tìm người mạnh!", "|-1|Các ngươi có phải là những chiến binh mạnh mẽ không?"},
                new String[]{"|-1|Haha! Thật thú vị! Chiến đấu thôi!", "|-1|Ta muốn xem sức mạnh thực sự của các ngươi!", "|-2|Đây là lúc để thể hiện bản lĩnh chiến binh!", "|-2|Hãy cho ta thấy các ngươi mạnh đến đâu!"},
                new String[]{"|-1|Tuyệt vời! Các ngươi thật sự rất mạnh!", "|-2|Ta sẽ còn mạnh hơn nữa! Hẹn gặp lại!"},
                300
        ));
        initializeVariables();
    }

    private void initializeVariables() {
        this.itemList = new ArrayList<>();
        this.lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;
        this.moveAwayTime = 0;
        this.movedAway = false;
        // Store original position when boss joins map
        this.originalX = this.location.x;
        this.originalY = this.location.y;
    }

    @Override
    protected void notifyJoinMap() {
        // Store original position when joining new map
        this.originalX = this.location.x;
        this.originalY = this.location.y;
    }

    @Override
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0, this.data[this.currentLevel].getMapJoin().length - 1)];
        return MapService.gI().getMapById(mapId).zones.get(0);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            damage = 1;
            moveAwayFromPlayer(plAtt);
            this.movedAway = true;
            this.moveAwayTime = System.currentTimeMillis();
            
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

    public void caRot(Player player) {
        if (player.effectSkill != null && !player.effectSkill.isCaRot) {
            chatWithCooldown("|-1|Carrot Magic! Biến thành cà rót đi!");
            EffectSkillService.gI().setIsCaRot(player, 2, 300000);
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
                
                this.nPoint.dame = pl.nPoint.hpMax / Util.nextInt(30, 50);
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        moveNearPlayer(pl);
                    }
                    if (Util.isTrue(1, 10)) {
                        chatWithCooldown("|-1|Haha! Tốc độ của ta như thế nào!");
                    }
                    caRot(pl);
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                // Log exception if needed
            }
        }
    }

    private void moveNearPlayer(Player pl) {
        if (SkillUtil.isUseSkillChuong(this)) {
            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
        } else {
            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
        }
    }

    private void moveAwayFromPlayer(Player player) {
        int currentDistance = Util.getDistance(this, player);
        if (currentDistance > SAFE_DISTANCE && isMovingAway) {
            return;
        }
        
        if (Util.isTrue(1, 15)) {
            chatWithCooldown("|-1|Carrot Glide! Ta sẽ tránh xa một chút!");
        }
        
        int dx = this.location.x - player.location.x;
        int dy = this.location.y - player.location.y;
        double angle = Math.atan2(dy, dx);
        
        int newX = this.location.x + (int) (MOVE_SPEED * Math.cos(angle));
        int newY = this.location.y + (int) (MOVE_SPEED * Math.sin(angle));
        
        this.moveTo(newX, newY);
        
        if (!isMovingAway) {
            moveAwayStartTime = System.currentTimeMillis();
            isMovingAway = true;
        }
    }

    private void returnToOriginalPosition() {
        long moveAwayDuration = System.currentTimeMillis() - moveAwayStartTime;
        if (moveAwayDuration >= MOVE_AWAY_DURATION) {
            // Return to original position stored when joining map
            this.moveTo(originalX, originalY);
            isMovingAway = false;
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        // Update original position when joining new map
        this.originalX = this.location.x;
        this.originalY = this.location.y;
    }

    @Override
    public void update() {
        super.update();
        
        if (isMovingAway) {
            returnToOriginalPosition();
        }
        
        if (this.zone != null) {
            handleMapChange();
            updatePlayerTarget();
        }
    }

    private void handleMapChange() {
        List<Player> players = Client.gI().getPlayers();
        int playerCount = players.size();
        
        if (playerCount > 0) {
            Player randomPlayer = players.get(Util.nextInt(playerCount));
            
            if (shouldChangeMap(randomPlayer)) {
                changeToRandomMap();
            }
        }
    }

    private boolean shouldChangeMap(Player player) {
        return player != null 
                && player.zone != null 
                && player.zone.isKhongCoTrongTaiTrongKhu()
                && !isInForbiddenMap(player.zone.map.mapId)
                && this.zone.getPlayers().size() <= MAX_PLAYERS_IN_ZONE
                && System.currentTimeMillis() > this.lastTimeJoinMap
                && player.id != -1000000;
    }

    private boolean isInForbiddenMap(int mapId) {
        for (int forbiddenId : FORBIDDEN_MAP_IDS) {
            if (mapId == forbiddenId) {
                return true;
            }
        }
        return false;
    }

    private void changeToRandomMap() {
        int randomMapId;

        // Vòng lặp random lại nếu trúng vào map 21, 22, 23
        do {
            randomMapId = Util.nextInt(0, MAX_MAP_ID);
        } while (randomMapId == 21 || randomMapId == 22 || randomMapId == 23);

        lastTimeJoinMap = System.currentTimeMillis() + TIME_CHANGE_MAP;

        // ... (Phần còn lại giữ nguyên) ...
        chatWithCooldown("|-1|Ta sẽ tìm kiếm chiến binh mạnh ở nơi khác!");
        ChangeMapService.gI().spaceShipArrive(this, (byte) 2, ChangeMapService.DEFAULT_SPACE_SHIP);
        ChangeMapService.gI().exitMap(this);

        this.zone = MapService.gI().getMapById(randomMapId).zones.get(0);
        this.location.x = Util.nextInt(Math.max(100, zone.map.mapWidth - 100));
        this.location.y = zone.map.yPhysicInTop(this.location.x, 100);

        this.joinMap();
    }

    private void updatePlayerTarget() {
        if (this.zone != null) {
            if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
                this.playerTarger = this.zone.getRandomPlayerInMap();
                this.lastTimeTargetPlayer = System.currentTimeMillis();
                this.timeTargetPlayer = Util.nextInt(5000, 10000);
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
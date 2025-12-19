package models.boss;

import consts.AppearType;
import consts.BossStatus;
import consts.BossType;
import consts.ConstPlayer;
import interfaces.IBoss;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import managers.boss.*; // Import gọn lại
import models.map.ItemMap;
import models.map.Zone;
import models.mob.Mob;
import static models.player.Player.idOutfitMafuba;
import models.player.Pet;
import models.player.Player;
import models.skill.Skill;
import network.io.Message;
import server.ServerNotify;
import services.EffectSkillService;
import services.Service;
import services.SkillService;
import services.TaskService;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.PlayerService;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;

public class Boss extends Player implements IBoss {

    public int currentLevel = -1;
    public final BossData[] data;
    public BossStatus bossStatus;

    protected Zone lastZone;
    protected long lastTimeRest;
    protected int secondsRest;

    // Chat vars
    protected long lastTimeChatS;
    protected int timeChatS;
    protected byte indexChatS;

    protected long lastTimeChatE;
    protected int timeChatE;
    protected byte indexChatE;

    protected long lastTimeChatM;
    protected int timeChatM;

    // Attack/Target vars
    protected long lastTimeTargetPlayer;
    protected int timeTargetPlayer;
    protected Player playerTarger;
    protected long lastTimeAttack;
    protected long lastTimeMove;

    // Relationship vars
    protected Boss parentBoss;
    public Boss[][] bossAppearTogether;
    public Zone zoneFinal = null;
    public Player playerReward;
    public int lv;

    // State vars
    public int error;
    public boolean isNotifyDisabled;
    public boolean isZone01SpawnDisabled;

    // --- BOM MECHANIC (Sửa lỗi treo server) ---
    public boolean prepareBom;
    private long lastTimeBom; // Timer cho bom

    // --- AI CUSTOM VARS ---
    protected Enum<?> currentState;
    protected long stateTimer;
    protected ItemMap targetItem;
    protected int rewardPlayerId;

    // ================= CONSTRUCTORS (Tối ưu DRY) =================
    public Boss(int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(BossType bossType, int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(bossType, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        validateData(data);
        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;

        // Mặc định add vào BossManager chung
        managers.boss.BossManager.gI().addBoss(this);
        initBossAppearTogether();
    }

    public Boss(BossType bossType, int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        validateData(data);
        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;

        // Đăng ký vào Manager tương ứng
        registerToManager(bossType);
        initBossAppearTogether();
    }

    private void validateData(BossData... data) throws Exception {
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ (Data is null or empty)");
        }
    }

    // Tách logic đăng ký manager ra riêng để dễ quản lý
    private void registerToManager(BossType bossType) {
        switch (bossType) {
            case YARDART ->
                YardartManager.gI().addBoss(this);
            case FINAL ->
                FinalBossManager.gI().addBoss(this);
            case SKILLSUMMONED ->
                SkillSummonedManager.gI().addBoss(this);
            case BROLY ->
                BrolyManager.gI().addBoss(this);
            case PHOBAN ->
                OtherBossManager.gI().addBoss(this);
            case PHOBANDT ->
                RedRibbonHQManager.gI().addBoss(this);
            case PHOBANBDKB ->
                TreasureUnderSeaManager.gI().addBoss(this);
            case PHOBANCDRD ->
                SnakeWayManager.gI().addBoss(this);
            case PHOBANKGHD ->
                GasDestroyManager.gI().addBoss(this);
            case TRUNGTHU_EVENT ->
                TrungThuEventManager.gI().addBoss(this);
            case HALLOWEEN_EVENT ->
                HalloweenEventManager.gI().addBoss(this);
            case CHRISTMAS_EVENT ->
                ChristmasEventManager.gI().addBoss(this);
            case HUNGVUONG_EVENT ->
                HungVuongEventManager.gI().addBoss(this);
            case TET_EVENT ->
                LunarNewYearEventManager.gI().addBoss(this);

            default ->
                managers.boss.BossManager.gI().addBoss(this);
        }
    }

    private void initBossAppearTogether() {
        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    try {
                        Boss boss = managers.boss.BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                        if (boss != null) {
                            boss.parentBoss = this;
                            boss.lv = j;
                            this.bossAppearTogether[i][j] = boss;
                        }
                    } catch (Exception e) {
                        Logger.error("Error creating child boss: " + e.getMessage());
                    }
                }
            }
        }
    }

    // ================= BASE METHODS =================
    @Override
    public void initBase() {
        BossData data = this.data[this.currentLevel];
        this.name = String.format(data.getName(), Util.nextInt(0, 100));
        this.gender = data.getGender();
        this.nPoint.mpg = 31_07_2002; // Magic number, nên đưa vào constant nếu cần
        this.nPoint.dameg = data.getDame();
        this.nPoint.hpg = data.getHp()[Util.nextInt(0, data.getHp().length - 1)];
        this.nPoint.hp = nPoint.hpg;
        this.nPoint.calPoint();
        this.initSkill();
        this.resetBase();
    }

    protected void initSkill() {
        for (Skill skill : this.playerSkill.skills) {
            skill.dispose();
        }
        this.playerSkill.skills.clear();
        this.playerSkill.skillSelect = null;
        int[][] skillTemps = data[this.currentLevel].getSkillTemp();
        for (int[] skillTemp : skillTemps) {
            Skill skill = SkillUtil.createSkill(skillTemp[0], skillTemp[1]);
            if (skillTemp.length == 3) {
                skill.coolDown = skillTemp[2];
            }
            this.playerSkill.skills.add(skill);
        }
    }

    protected void resetBase() {
        this.lastTimeChatS = 0;
        this.lastTimeChatE = 0;
        this.timeChatS = 0;
        this.timeChatE = 0;
        this.indexChatS = 0;
        this.indexChatE = 0;
        this.prepareBom = false; // Reset bom state
    }

    // ... (Các hàm getHead, getBody, getLeg giữ nguyên) ...
    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        return this.data[this.currentLevel].getOutfit()[0];
    }

    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        return this.data[this.currentLevel].getOutfit()[1];
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        return this.data[this.currentLevel].getOutfit()[2];
    }

    @Override
    public short getFlagBag() {
        return this.data[this.currentLevel].getOutfit()[3];
    }

    @Override
    public byte getAura() {
        return (byte) this.data[this.currentLevel].getOutfit()[4];
    }

    @Override
    public byte getEffFront() {
        return (byte) this.data[this.currentLevel].getOutfit()[5];
    }

    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0, this.data[this.currentLevel].getMapJoin().length - 1)];
        return MapService.gI().getMapWithRandZone(mapId);
    }

    @Override
    public void changeStatus(BossStatus status) {
        this.bossStatus = status;
    }

    @Override
    public Player getPlayerAttack() {
        if (this.zone == null) {
            return null;
        }

        // Reset target nếu mục tiêu chết hoặc rời map
        if (this.playerTarger != null && (this.playerTarger.isDie() || !this.zone.equals(this.playerTarger.zone))) {
            this.playerTarger = null;
        }

        // Tìm mục tiêu mới
        if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            this.playerTarger = this.zone.getRandomPlayerInMap();
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(5000, 7000);
        }

        // Không đánh đệ tử của chính mình (nếu có logic này)
        if (this.playerTarger != null && this.playerTarger.isPet && ((Pet) this.playerTarger).master != null && ((Pet) this.playerTarger).master.equals(this)) {
            this.playerTarger = null;
        }
        return this.playerTarger;
    }

    @Override
    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }

    @Override
    public void changeToTypeNonPK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
    }

    @Override
    public void updateInfo() {
        super.update();
    }

    @Override
    public void update() {
        // 🔴 SỬA LỖI BOM: Xử lý bom trong vòng lặp update thay vì while blocking
        if (prepareBom) {
            handleBom();
            return; // Khi đang bom thì không làm gì khác
        }

        super.update();
        this.nPoint.mp = this.nPoint.mpg;

        if (this.effectSkill == null || this.effectSkill.isHaveEffectSkill() || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
            return;
        }

        // Auto leave map logic
        switch (this.bossStatus) {
            case CHAT_S, AFK, ACTIVE ->
                this.autoLeaveMap();
        }

        // State Machine
        switch (this.bossStatus) {
            case REST ->
                this.rest();
            case RESPAWN -> {
                this.respawn();
                this.changeStatus(BossStatus.JOIN_MAP);
            }
            case JOIN_MAP ->
                this.joinMap();
            case CHAT_S -> {
                if (chatS()) {
                    this.doneChatS();
                    this.lastTimeChatM = System.currentTimeMillis();
                    this.timeChatM = 5000;
                    if (this.bossStatus != BossStatus.AFK) {
                        this.changeStatus(BossStatus.ACTIVE);
                    }
                }
            }
            case AFK ->
                this.afk();
            case ACTIVE -> {
                this.chatM();
                if (this.effectSkill.isCharging && !Util.isTrue(1, 20) || this.effectSkill.useTroi) {
                    return;
                }
                this.active();
            }
            case DIE ->
                this.changeStatus(BossStatus.CHAT_E);
            case CHAT_E -> {
                if (chatE()) {
                    this.doneChatE();
                    this.changeStatus(BossStatus.LEAVE_MAP);
                }
            }
            case LEAVE_MAP ->
                this.leaveMap();
        }
    }

    // --- LOGIC BOM MỚI (Non-blocking) ---
    private void handleBom() {
        if (Util.canDoWithTime(lastTimeBom, 2500)) {
            // Thực hiện nổ
            Player plAtt = null; // Cần xác định người giết nếu cần, hoặc để null
            setDie(this);
            die(plAtt);

            if (this.zone != null) {
                long dame = this.nPoint.hpMax;
                for (Mob mob : this.zone.mobs) {
                    mob.injured(this, dame, true);
                }
                List<Player> playersMap = this.zone.getNotBosses();
                if (!MapService.gI().isMapOffline(this.zone.map.mapId)) {
                    for (int i = playersMap.size() - 1; i >= 0; i--) {
                        Player pl = playersMap.get(i);
                        if (!this.equals(pl)) {
                            pl.injured(this, dame, false, false);
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.gI().Send_Info_NV(pl);
                        }
                    }
                }
            }
            prepareBom = false; // Kết thúc bom
        }
    }

    @Override
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }

        if (this.data[nextLevel].getTypeAppear() == AppearType.DEFAULT_APPEAR
                && Util.canDoWithTime(lastTimeRest, secondsRest * 1000)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void afk() {
    }

    @Override
    public void respawn() {
        this.currentLevel++;
        if (this.currentLevel >= this.data.length) {
            this.currentLevel = 0;
        }
        this.initBase();
        this.changeToTypeNonPK();
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.wakeupAnotherBossWhenAppear();
            return;
        }

        // Xác định zone cần join
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }

        if (this.zone == null) {
            this.zone = getMapJoin();
        }

        // Kiểm tra zone hợp lệ
        if (this.zone == null || this.zone.map == null || this.zone.map.zones == null || this.zone.map.zones.isEmpty()) {
            Logger.error("Không thể spawn boss - Map hoặc zones không hợp lệ");
            this.changeStatus(BossStatus.RESPAWN);
            return;
        }

        try {
            if (this.currentLevel == 0) {
                if (this.parentBoss == null) {
                    Zone selectedZone = selectBestZone();

                    // Nếu không tìm được zone phù hợp, fallback về zone 1
                    if (selectedZone == null) {
                        selectedZone = getFallbackZone();
                    }

                    if (selectedZone == null) {
                        Logger.error("Không tìm được zone nào cho boss: " + this.name);
                        this.changeStatus(BossStatus.RESPAWN);
                        return;
                    }

                    this.zone = selectedZone;

                    // Spawn tại vị trí ngẫu nhiên
                    int x = this.zone.map.mapWidth > 100
                            ? Util.nextInt(100, this.zone.map.mapWidth - 100)
                            : Util.nextInt(50, Math.max(51, this.zone.map.mapWidth - 50));
                    int y = this.zone.map.yPhysicInTop(x, 100);

                    ChangeMapService.gI().changeMap(this, this.zone, x, y);
                } else {
                    // Boss đệ tử - spawn gần boss cha
                    int x = this.parentBoss.location.x - (this.lv + 1) * 30;
                    x = Math.max(50, Math.min(x, this.zone.map.mapWidth - 50));
                    int y = this.zone.map.yPhysicInTop(x, 100);

                    ChangeMapService.gI().changeMap(this, this.zone, x, y);
                }
                this.wakeupAnotherBossWhenAppear();
            } else {
                ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            }

            Service.gI().sendFlagBag(this);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);

        } catch (Exception e) {
            Logger.error("Lỗi join map boss: " + e.getMessage() + " - Boss: " + this.name);
            e.printStackTrace();

            if (error < 3) {
                error++;
                this.zone = null;
                this.changeStatus(BossStatus.RESPAWN);
            } else {
                this.changeStatus(BossStatus.REST);
                error = 0;
            }
        }
    }

    /**
     * Chọn zone tốt nhất theo logic cũ
     */
    private Zone selectBestZone() {
        List<Zone> zones = this.zone.map.zones;

        if (zones == null || zones.isEmpty()) {
            return null;
        }

        // --- CASE 1: BOSS ĐẶC BIỆT (isZone01SpawnDisabled) ---
        if (this.isZone01SpawnDisabled) {
            List<Zone> zonesWithPlayers = new ArrayList<>();
            List<Zone> zonesWithoutBoss = new ArrayList<>();

            for (Zone z : zones) {
                if (z == null) {
                    continue;
                }

                boolean hasBoss = z.getBosses() != null && !z.getBosses().isEmpty();
                boolean hasPlayers = z.getNumOfPlayers() > 0;

                if (!hasBoss) {
                    zonesWithoutBoss.add(z);
                    if (hasPlayers) {
                        zonesWithPlayers.add(z);
                    }
                }
            }

            // Ưu tiên 1: Khu có người + không có boss
            if (!zonesWithPlayers.isEmpty()) {
                return zonesWithPlayers.get(Util.nextInt(0, zonesWithPlayers.size() - 1));
            }

            // Ưu tiên 2: Khu không có boss (dù trống người)
            if (!zonesWithoutBoss.isEmpty()) {
                return zonesWithoutBoss.get(Util.nextInt(0, zonesWithoutBoss.size() - 1));
            }

            // Không tìm được zone phù hợp, return null để fallback
            return null;
        } // --- CASE 2: BOSS THƯỜNG ---
        else {
            List<Zone> preferredZones = new ArrayList<>();

            // Thêm zone 1 nếu có
            if (zones.size() > 1 && zones.get(1) != null) {
                preferredZones.add(zones.get(1));
            }

            // Thêm zone 2 nếu có
            if (zones.size() > 2 && zones.get(2) != null) {
                preferredZones.add(zones.get(2));
            }

            // Random chọn zone 1 hoặc 2
            if (!preferredZones.isEmpty()) {
                return preferredZones.get(Util.nextInt(0, preferredZones.size() - 1));
            }

            // Không có zone 1,2 => return null để fallback
            return null;
        }
    }

    /**
     * Fallback zone khi không tìm được zone phù hợp Ưu tiên: Zone 1 > Zone 0
     */
    private Zone getFallbackZone() {
        List<Zone> zones = this.zone.map.zones;

        if (zones == null || zones.isEmpty()) {
            return null;
        }

        // Ưu tiên zone 1
        if (zones.size() > 1 && zones.get(1) != null) {
            Logger.warning("Fallback: Boss spawn tại zone 1 - " + this.name);
            return zones.get(1);
        }

        // Nếu không có zone 1, dùng zone 0
        if (zones.size() > 0 && zones.get(0) != null) {
            Logger.warning("Fallback: Boss spawn tại zone 0 - " + this.name);
            return zones.get(0);
        }

        return null;
    }

    public void joinMapByZone(Zone zone) {
        if (zone != null) {
            this.zone = zone;
            int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
        }
    }

    protected void notifyJoinMap() {
        if (canSendNotify()) {
            ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }

    private boolean canSendNotify() {
        return !(this.isNotifyDisabled || this.zone.map.mapId == 140
                || MapService.gI().isMapPhoBan(this.zone.map.mapId)
                || MapService.gI().isMapMaBu(this.zone.map.mapId)
                || MapService.gI().isMapBlackBallWar(this.zone.map.mapId));
    }

    // ================= CHAT METHODS =================
    @Override
    public boolean chatS() {
        if (Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            if (this.indexChatS == this.data[this.currentLevel].getTextS().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextS()[this.indexChatS];
            if (!processChat(textChat)) {
                return false;
            }

            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = Math.min(textChat.length() * 100, 2000);
            this.indexChatS++;
        }
        return false;
    }

    @Override
    public void doneChatS() {
    }

    @Override
    public void chatM() {
        if (this.typePk == ConstPlayer.NON_PK) {
            return;
        }
        if (this.data[this.currentLevel].getTextM().length == 0) {
            return;
        }

        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }

        String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0, this.data[this.currentLevel].getTextM().length - 1)];
        processChat(textChat);

        this.lastTimeChatM = System.currentTimeMillis();
        this.timeChatM = Util.nextInt(3000, 20000);
    }

    @Override
    public boolean chatE() {
        if (Util.canDoWithTime(lastTimeChatE, timeChatE)) {
            if (this.indexChatE == this.data[this.currentLevel].getTextE().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextE()[this.indexChatE];
            if (!processChat(textChat)) {
                return false;
            }

            this.lastTimeChatE = System.currentTimeMillis();
            this.timeChatE = Math.min(textChat.length() * 100, 2000);
            this.indexChatE++;
        }
        return false;
    }

    @Override
    public void doneChatE() {
    }

    // Helper xử lý chat chung
    private boolean processChat(String textChat) {
        try {
            int separatorIndex = textChat.lastIndexOf("|");
            if (separatorIndex == -1) {
                return false; // Format lỗi
            }
            int prefix = Integer.parseInt(textChat.substring(1, separatorIndex));
            String content = textChat.substring(separatorIndex + 1);
            return this.chat(prefix, content);
        } catch (Exception e) {
            return false;
        }
    }

    // ================= ACTION METHODS =================
    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public void attack() {
        if (this.isDie() || this.effectSkill.isHaveEffectSkill() || this.playerSkill.skills.isEmpty() || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }

        try {
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
                return;
            }

            // --- Logic Di chuyển ---
            if (Util.getDistance(this, pl) > 80) {
                if (Util.canDoWithTime(this.lastTimeMove, 600)) {
                    this.lastTimeMove = System.currentTimeMillis();
                    this.moveToPlayer(pl);
                }
                return;
            }

            // --- Logic Tấn công ---
            if (Util.canDoWithTime(this.lastTimeAttack, 1300)) {//time ra đòn
                this.lastTimeAttack = System.currentTimeMillis();
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    // Hiệu ứng di chuyển nhẹ khi đánh
                    int moveX = Util.isTrue(5, 20) ? (SkillUtil.isUseSkillChuong(this) ? Util.nextInt(20, 80) : Util.nextInt(10, 30)) : 0;
                    if (moveX > 0) {
                        this.moveTo(pl.location.x + (Util.getOne(-1, 1) * moveX), pl.location.y);
                    }

                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            }
        } catch (Exception ex) {
            Logger.logException(Boss.class, ex);
        }
    }

    @Override
    public void checkPlayerDie(Player player) {
    }

    protected int getRangeCanAttackWithSkillSelect() {
        if (this.playerSkill.skillSelect == null) {
            return 500;
        }
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        } else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK || skillId == Skill.LIEN_HOAN || skillId == Skill.KAIOKEN) {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return 500;
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            this.changeStatus(BossStatus.RESPAWN);
        } else {
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
        }
        this.wakeupAnotherBossWhenDisappear();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }

        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }

        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
        }

        damage = this.nPoint.subDameInjureWithDeff(damage);

        if (effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }

        this.nPoint.subHP(damage);

        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }

        return (int) damage;
    }

    @Override
    public void moveToPlayer(Player pl) {
        if (pl.location != null) {
            moveTo(pl.location.x, pl.location.y);
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y + (Util.isTrue(3, 10) ? -50 : 0));
    }

    public void chat(String text) {
        Service.gI().chat(this, text);
    }

    protected boolean chat(int prefix, String textChat) {
        if (prefix == -1) {
            this.chat(textChat);
        } else if (prefix == -2) {
            if (this.zone != null) {
                Player plMap = this.zone.getRandomPlayerInMap();
                if (plMap != null && !plMap.isDie() && Util.getDistance(this, plMap) <= 600) {
                    Service.gI().chat(plMap, textChat);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (prefix == -3) {
            if (this.parentBoss != null && !this.parentBoss.isDie()) {
                this.parentBoss.chat(textChat);
            }
        } else if (prefix >= 0) {
            if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
                Boss boss = this.bossAppearTogether[this.currentLevel][prefix];
                if (boss != null && !boss.isDie()) {
                    boss.chat(textChat);
                }
            } else if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null
                    && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                Boss boss = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][prefix];
                if (boss != null && !boss.isDie()) {
                    boss.chat(textChat);
                }
            }
        }
        return true;
    }

    @Override
    public void wakeupAnotherBossWhenAppear() {
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if (boss == null) {
                continue;
            }
            int nextLevelBoss = boss.currentLevel + 1;
            if (nextLevelBoss >= boss.data.length) {
                nextLevelBoss = 0;
            }

            if (boss.data[nextLevelBoss].getTypeAppear() == AppearType.CALL_BY_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == AppearType.APPEAR_WITH_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
                boss.changeStatus(BossStatus.RESPAWN);
            }
        }
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
    }

    @Override
    public void autoLeaveMap() {
    }

    public void leaveMapNew() {
        if (this.data != null) {
            this.currentLevel = this.data.length;
        }
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    @Override
    public void setBom(Player plAtt) {
        if (!prepareBom) {
            prepareBom = true;
            this.nPoint.hp = 1; // Bất tử khi gồng bom
            this.lastTimeBom = System.currentTimeMillis();

            Service.gI().chat(this, "Ta sẽ kéo các ngươi theo!");
            try {
                Message msg = new Message(-45);
                msg.writer().writeByte(7);
                msg.writer().writeInt((int) this.id);
                msg.writer().writeShort(104);
                msg.writer().writeShort(2000);
                Service.gI().sendMessAllPlayerInMap(this, msg);
                msg.cleanup();
            } catch (IOException e) {
                Logger.logException(Boss.class, e);
            }
        }
        // Logic nổ đã chuyển sang handleBom() trong update()
    }

    // --- AI HELPER METHODS ---
    protected void moveAwayFromPlayer(Player player, int distance) {
        if (player == null) {
            return;
        }
        int dx = this.location.x - player.location.x;
        int targetX = this.location.x + (dx > 0 ? distance : -distance);
        targetX = Math.max(50, Math.min(this.zone.map.mapWidth - 50, targetX));
        moveTo(targetX, this.location.y);
    }

    protected void teleportRandomly() {
        if (this.zone == null) {
            return;
        }
        int newX = Util.nextInt(50, this.zone.map.mapWidth - 50);
        int newY = this.zone.map.yPhysicInTop(newX, 100);
        PlayerService.gI().playerMove(this, newX, newY);
    }

    protected void teleportNearPlayer(Player pl) {
        if (pl == null) {
            return;
        }
        int dis = Util.nextInt(40, 50);
        int newX = pl.location.x + (Util.isTrue(50, 100) ? dis : -dis);
        PlayerService.gI().playerMove(this, newX, pl.location.y);
    }

    protected void leaveMapAfter(String farewellMessage) {
        this.chat(farewellMessage);
        Zone newZone = Util.randomAllMap();
        ChangeMapService.gI().changeMap(this, newZone, Util.nextInt(50, newZone.map.mapWidth - 50), 5);
    }

    protected boolean checkForSpecialItem(int specialItemId, Enum<?> onFoundState, String foundMessage) {
        if (this.zone != null) {
            ItemMap item = this.zone.getItemMapByTempId(specialItemId);
            if (item != null) {
                this.targetItem = item;
                this.rewardPlayerId = (int) item.playerId;
                this.currentState = onFoundState;
                this.chat(foundMessage);
                return true;
            }
        }
        return false;
    }
}

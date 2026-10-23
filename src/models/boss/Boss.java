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
    protected long lastTimeJoinMap;
    protected long lastTimeHadPlayer;

    // --- DYNAMIC COMBAT & SOUL ENGINE ---
    public boolean isLastStandEnabled = true; // Cho phép kích hoạt cơ chế Hơi Thở Cuối Cùng
    public byte countHealLastStand;           // Đếm số lần hồi máu Last Stand (tối đa 1 lần duy nhất trong cả trận)
    public boolean isBerserk;                 // Trạng thái Cuồng Nộ sau khi thức tỉnh
    protected long lastTimeCounterAttack;     // Cooldown phản xạ đỡ đòn & phản kích (ms)
    protected long lastTimeFlashStep;         // Cooldown dịch chuyển áp sát sau lưng (ms)
    protected int rangedAttacksReceived;      // Đếm số lần bị chưởng từ xa liên tiếp
    protected long lastTimeSoulChat;          // Hạn chế tần suất phát ngôn để không spam chat

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
        BossData data = getCurrentBossData();
        if (data == null) {
            return;
        }
        this.name = String.format(data.getName(), Util.nextInt(0, 100));
        this.gender = data.getGender();
        this.nPoint.mpg = 31_07_2002; // Magic number, nên đưa vào constant nếu cần
        this.nPoint.dameg = data.getDame();
        if (data.getHp() != null && data.getHp().length > 0) {
            this.nPoint.hpg = data.getHp()[Util.nextInt(0, data.getHp().length - 1)];
        }
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
        BossData bData = getCurrentBossData();
        if (bData == null || bData.getSkillTemp() == null) {
            return;
        }
        int[][] skillTemps = bData.getSkillTemp();
        for (int[] skillTemp : skillTemps) {
            Skill skill = SkillUtil.createSkill(skillTemp[0], skillTemp[1]);
            if (skill != null) {
                if (skillTemp.length == 3) {
                    skill.coolDown = skillTemp[2];
                }
                this.playerSkill.skills.add(skill);
            }
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
        this.countHealLastStand = 0;
        this.isBerserk = false;
        this.lastTimeCounterAttack = 0;
        this.lastTimeFlashStep = 0;
        this.rangedAttacksReceived = 0;
        this.lastTimeSoulChat = 0;
    }

    // ================= SAFE OUTFIT & DATA GETTERS =================
    public BossData getCurrentBossData() {
        if (this.data == null || this.data.length == 0) {
            return null;
        }
        int level = (this.currentLevel >= 0 && this.currentLevel < this.data.length) ? this.currentLevel : 0;
        return this.data[level];
    }

    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        BossData bData = getCurrentBossData();
        return (bData != null && bData.getOutfit() != null && bData.getOutfit().length > 0) ? bData.getOutfit()[0] : -1;
    }

    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        BossData bData = getCurrentBossData();
        return (bData != null && bData.getOutfit() != null && bData.getOutfit().length > 1) ? bData.getOutfit()[1] : -1;
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        BossData bData = getCurrentBossData();
        return (bData != null && bData.getOutfit() != null && bData.getOutfit().length > 2) ? bData.getOutfit()[2] : -1;
    }

    @Override
    public short getFlagBag() {
        BossData bData = getCurrentBossData();
        return (bData != null && bData.getOutfit() != null && bData.getOutfit().length > 3) ? bData.getOutfit()[3] : -1;
    }

    @Override
    public byte getAura() {
        BossData bData = getCurrentBossData();
        return (byte) ((bData != null && bData.getOutfit() != null && bData.getOutfit().length > 4) ? bData.getOutfit()[4] : -1);
    }

    @Override
    public byte getEffFront() {
        BossData bData = getCurrentBossData();
        return (byte) ((bData != null && bData.getOutfit() != null && bData.getOutfit().length > 5) ? bData.getOutfit()[5] : -1);
    }

    public Zone getMapJoin() {
        BossData bData = getCurrentBossData();
        if (bData == null || bData.getMapJoin() == null || bData.getMapJoin().length == 0) {
            return null;
        }
        int mapId = bData.getMapJoin()[Util.nextInt(0, bData.getMapJoin().length - 1)];
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
        if (this.isDie()) {
            this.prepareBom = false;
            return;
        }
        if (Util.canDoWithTime(lastTimeBom, 2500)) {
            this.prepareBom = false; // Kết thúc bom trước khi gây nổ và die
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
                if (this.zone == null) {
                    this.zone = (this.lastZone != null) ? this.lastZone : getMapJoin();
                }
                if (this.zone != null) {
                    ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
                }
            }

            Service.gI().sendFlagBag(this);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.lastTimeJoinMap = System.currentTimeMillis();
            this.lastTimeHadPlayer = System.currentTimeMillis();

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
     * Kiểm tra xem khu vực (Zone) có khả dụng cho Boss này vào hay không.
     * Quy tắc Độc Quyền Khu: Mỗi khu chỉ cho phép tối đa 1 loại Boss (trừ trường hợp kịch bản đi chung/cha-con).
     */
    public boolean isZoneAvailableForBoss(Zone targetZone) {
        if (targetZone == null || targetZone.map == null) {
            return false;
        }
        List<Player> livingBosses = targetZone.getBosses();
        if (livingBosses == null || livingBosses.isEmpty()) {
            return true;
        }

        for (Player pl : livingBosses) {
            if (pl != null && !pl.isDie() && pl instanceof Boss otherBoss) {
                if (otherBoss.equals(this)) {
                    continue; // Bỏ qua chính bản thân
                }
                // Nếu khu đã có Boss và không thuộc cùng một nhóm/kịch bản -> TỪ CHỐI
                if (!isSameBossGroup(this, otherBoss)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Kiểm tra hai Boss có thuộc cùng một kịch bản/nhóm (Cha-con, phân thân, boss xuất hiện cùng nhau) hay không.
     */
    private boolean isSameBossGroup(Boss b1, Boss b2) {
        if (b1 == null || b2 == null) {
            return false;
        }
        // 1. Cùng quan hệ cha - con (ví dụ: Xên & Xên con, Ninja & Phân thân, Broly & Đệ tử)
        if (b1.parentBoss != null && (b1.parentBoss.equals(b2) || b1.parentBoss.equals(b2.parentBoss))) {
            return true;
        }
        if (b2.parentBoss != null && (b2.parentBoss.equals(b1) || b2.parentBoss.equals(b1.parentBoss))) {
            return true;
        }

        // 2. Cùng kịch bản xuất hiện chung (bossAppearTogether, vd Tiểu đội sát thủ)
        if (b1.bossAppearTogether != null) {
            for (Boss[] group : b1.bossAppearTogether) {
                if (group != null) {
                    for (Boss b : group) {
                        if (b != null && b.equals(b2)) {
                            return true;
                        }
                    }
                }
            }
        }
        if (b2.bossAppearTogether != null) {
            for (Boss[] group : b2.bossAppearTogether) {
                if (group != null) {
                    for (Boss b : group) {
                        if (b != null && b.equals(b1)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Chọn zone tốt nhất đảm bảo độc quyền 1 loại Boss mỗi khu
     */
    private Zone selectBestZone() {
        if (this.zone == null || this.zone.map == null) {
            return null;
        }
        List<Zone> zones = this.zone.map.zones;
        if (zones == null || zones.isEmpty()) {
            return null;
        }

        List<Zone> validZones = new ArrayList<>();
        List<Zone> zonesWithPlayers = new ArrayList<>();

        for (Zone z : zones) {
            if (z == null || !isZoneAvailableForBoss(z)) {
                continue; // Bỏ qua nếu khu đã bị chiếm bởi loại Boss khác
            }

            // Loại bỏ khu quá tải người chơi (>= 15 người)
            if (z.getNumOfPlayers() < 15) {
                validZones.add(z);
                if (z.getNumOfPlayers() > 0) {
                    zonesWithPlayers.add(z);
                }
            }
        }

        // Ưu tiên 1: Khu có người chơi và chưa có loại Boss nào khác
        if (!zonesWithPlayers.isEmpty()) {
            return zonesWithPlayers.get(Util.nextInt(0, zonesWithPlayers.size() - 1));
        }

        // Ưu tiên 2: Khu hợp lệ bất kỳ (chưa có Boss nào)
        if (!validZones.isEmpty()) {
            return validZones.get(Util.nextInt(0, validZones.size() - 1));
        }

        // Không tìm được zone phù hợp => fallback
        return null;
    }

    /**
     * Fallback zone: Chỉ chọn zone trống Boss, tuyệt đối không thả vào khu đã có Boss khác
     */
    private Zone getFallbackZone() {
        if (this.zone == null || this.zone.map == null) {
            return null;
        }
        List<Zone> zones = this.zone.map.zones;
        if (zones == null || zones.isEmpty()) {
            return null;
        }

        List<Zone> emptyBossZones = new ArrayList<>();
        for (Zone z : zones) {
            if (z != null && isZoneAvailableForBoss(z)) {
                emptyBossZones.add(z);
            }
        }

        if (!emptyBossZones.isEmpty()) {
            return emptyBossZones.get(Util.nextInt(0, emptyBossZones.size() - 1));
        }

        return null;
    }

    public void joinMapByZone(Zone zone) {
        if (zone != null) {
            this.zone = zone;
            int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
            this.lastTimeJoinMap = System.currentTimeMillis();
            this.lastTimeHadPlayer = System.currentTimeMillis();
        }
    }

    protected void notifyJoinMap() {
        if (canSendNotify()) {
            ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName + " khu " + this.zone.zoneId);
        }
    }

    private boolean canSendNotify() {
        return this.zone != null && this.zone.map != null
                && !(this.isNotifyDisabled || this.zone.map.mapId == 140
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
                this.indexChatS++; // Bỏ qua câu thoại lỗi định dạng để tránh kẹt vĩnh viễn FSM
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
                this.indexChatE++; // Bỏ qua câu thoại lỗi định dạng để tránh kẹt vĩnh viễn FSM
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

            // Tốc độ ra đòn và di chuyển: bình thường 1300ms/600ms, khi Berserk (sau Last Stand) tăng tốc lên 800ms/400ms
            int attackCooldown = this.isBerserk ? 800 : 1300;
            int moveSpeed = this.isBerserk ? 400 : 600;

            // --- Logic Di chuyển ---
            if (Util.getDistance(this, pl) > 80) {
                if (Util.canDoWithTime(this.lastTimeMove, moveSpeed)) {
                    this.lastTimeMove = System.currentTimeMillis();
                    this.moveToPlayer(pl);
                }
                return;
            }

            // --- Logic Tấn công ---
            if (Util.canDoWithTime(this.lastTimeAttack, attackCooldown)) {//time ra đòn
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
        if (player != null && player.isDie() && Util.canDoWithTime(this.lastTimeSoulChat, 6000)) {
            this.lastTimeSoulChat = System.currentTimeMillis();
            this.chat(getRandomKillPlayerChat());
        }
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
        this.prepareBom = false;
        if (plKill != null) {
            reward(plKill);
            dropVeQuayLuckyRound(plKill);
            String killerName = (plKill.isPet && plKill instanceof Pet && ((Pet) plKill).master != null)
                    ? "Đệ tử của " + ((Pet) plKill).master.name
                    : plKill.name;
            ServerNotify.gI().notify(killerName + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }

    protected void dropVeQuayLuckyRound(Player plKill) {
        if (plKill != null && this.zone != null && !MapService.gI().isMapOffline(this.zone.map.mapId)) {
            // 40% rơi 1-2 Vé quay Thượng Đế (ID 821) khi hạ gục Boss thế giới
            if (Util.isTrue(40, 100)) {
                long plId = (plKill.isPet && plKill instanceof Pet && ((Pet) plKill).master != null)
                        ? ((Pet) plKill).master.id
                        : plKill.id;
                ItemMap it = new ItemMap(this.zone, consts.ConstItem.VE_QUAY_NGOC_VANG, Util.nextInt(1, 2),
                        this.location.x,
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                        plId);
                Service.gI().dropItemMap(this.zone, it);
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            ChangeMapService.gI().exitMap(this); // Làm sạch thực thể cũ khỏi map trước khi biến hình
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
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }

        if (this.prepareBom) {
            return 0; // Đang gồng bom tự sát, miễn nhiễm sát thương
        }

        // --- 1. THOẠI NÉ ĐÒN CÓ HỒN ---
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            chatDodge();
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

        // --- 2. CƠ CHẾ PHẢN XẠ ĐỠ ĐÒN & PHẢN KÍCH (PARRY & COUNTER) ---
        // Khi bị cận chiến dồn dập, có 12% tỷ lệ Boss đỡ đòn (giảm 70% sát thương) và phản kích đẩy lùi
        if (plAtt != null && !piercing && Util.getDistance(this, plAtt) <= 120
                && Util.canDoWithTime(this.lastTimeCounterAttack, 7000) && Util.isTrue(12, 100)) {
            this.lastTimeCounterAttack = System.currentTimeMillis();
            damage = Math.max(1, damage * 30 / 100); // Giảm 70% sát thương
            this.chat(getRandomCounterChat());
            // Đẩy lùi nhẹ player tấn công
            if (plAtt.location != null && this.zone != null && this.zone.map != null) {
                int pushDist = 70;
                int newX = plAtt.location.x + (plAtt.location.x < this.location.x ? -pushDist : pushDist);
                newX = Math.max(50, Math.min(this.zone.map.mapWidth - 50, newX));
                Service.gI().setPos(plAtt, newX, plAtt.location.y);
            }
        }

        // --- 3. ĐẾM SỐ LẦN BỊ BẮN TỪ XA ĐỂ KÍCH HOẠT FLASH STEP ---
        if (plAtt != null && Util.getDistance(this, plAtt) > 220) {
            this.rangedAttacksReceived++;
            if (this.rangedAttacksReceived >= 3 && Util.canDoWithTime(this.lastTimeFlashStep, 8000)) {
                this.rangedAttacksReceived = 0;
                this.lastTimeFlashStep = System.currentTimeMillis();
                triggerFlashStep(plAtt);
            }
        } else {
            this.rangedAttacksReceived = 0;
        }

        // --- 4. THOẠI KHI DÍNH ĐÒN NẶNG (> 15% HP) ---
        if (damage >= this.nPoint.hpMax * 15 / 100 && Util.canDoWithTime(this.lastTimeSoulChat, 6000)) {
            this.lastTimeSoulChat = System.currentTimeMillis();
            this.chat(getRandomHeavyDamageChat());
        }

        // --- 5. CƠ CHẾ "HƠI THỞ CUỐI CÙNG" (LAST STAND & AWAKENING) ---
        // Ngưỡng HP < 20% và chưa từng hồi lần nào (tối đa 1 lần duy nhất trong cả trận)
        if (this.isLastStandEnabled && this.countHealLastStand < 1 && (this.nPoint.hp - damage <= this.nPoint.hpMax * 20 / 100)) {
            // Nếu đòn này làm chết boss, giữ lại 1 HP để thức tỉnh
            if (this.nPoint.hp - damage <= 0) {
                this.nPoint.hp = 1;
                damage = 0;
            } else {
                this.nPoint.subHP(damage);
            }
            triggerLastStand(plAtt);
            return damage;
        }

        this.nPoint.subHP(damage);

        // --- 6. THOẠI KHI HP HẤP HỐI (< 5% HP) ---
        if (this.nPoint.hp > 0 && this.nPoint.hp <= this.nPoint.hpMax * 5 / 100 && Util.canDoWithTime(this.lastTimeSoulChat, 10000)) {
            this.lastTimeSoulChat = System.currentTimeMillis();
            this.chat(getRandomNearDeathChat());
        }

        // --- AI Phản Ứng Khi Bị Tấn Công (Threat / Aggro) ---
        if (plAtt != null && !plAtt.isDie() && this.zone != null && this.zone.equals(plAtt.zone)) {
            if (this.playerTarger == null || this.playerTarger.isDie() || !this.zone.equals(this.playerTarger.zone)
                    || Util.getDistance(this, this.playerTarger) > 400 || Util.getDistance(this, plAtt) <= 250) {
                this.playerTarger = plAtt;
                this.lastTimeTargetPlayer = System.currentTimeMillis();
                this.timeTargetPlayer = Util.nextInt(5000, 7000);
            }
        }

        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }

        return damage;
    }

    @Override
    public void moveToPlayer(Player pl) {
        if (pl != null && pl.location != null) {
            moveTo(pl.location.x, pl.location.y);
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        int targetX = this.location.x + (dir == 1 ? move : -move);
        int targetY = y;
        if (this.zone != null && this.zone.map != null) {
            targetX = Math.max(30, Math.min(this.zone.map.mapWidth - 30, targetX));
            int groundY = this.zone.map.yPhysicInTop(targetX, Math.max(0, y - 24));
            if (groundY > 0) {
                targetY = (Util.isTrue(3, 10) && groundY > 80) ? (groundY - 40) : groundY;
            } else {
                targetY = Math.max(24, Math.min(this.zone.map.mapHeight - 48, y));
            }
        }
        PlayerService.gI().playerMove(this, targetX, targetY);
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
        if (this.zone == null) {
            return;
        }
        // Nếu có người chơi thì reset bộ đếm thời gian vắng người
        if (this.zone.getNumOfPlayers() > 0) {
            this.lastTimeHadPlayer = System.currentTimeMillis();
        } else {
            // Không có người chơi trong map quá 15 phút (900.000 ms) -> rời map
            if (lastTimeHadPlayer > 0 && Util.canDoWithTime(this.lastTimeHadPlayer, 900_000)) {
                this.leaveMap();
                return;
            }
        }
        // Boss tồn tại quá 45 phút (2.700.000 ms) kể từ lúc spawn mà không bị hạ gục -> tự rời map
        if (lastTimeJoinMap > 0 && Util.canDoWithTime(this.lastTimeJoinMap, 2_700_000)) {
            this.leaveMap();
        }
    }

    public void leaveMapNew() {
        if (this.data != null) {
            this.currentLevel = this.data.length;
        }
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    @Override
    public void setBom(Player plAtt) {
        if (!prepareBom && !this.isDie()) {
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
        if (pl == null || pl.location == null) {
            return;
        }
        int dis = Util.nextInt(40, 50);
        int newX = pl.location.x + (Util.isTrue(50, 100) ? dis : -dis);
        if (this.zone != null && this.zone.map != null) {
            newX = Math.max(30, Math.min(this.zone.map.mapWidth - 30, newX));
        }
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

    // ================= DYNAMIC COMBAT & SOUL HELPER METHODS =================

    /**
     * Kích hoạt cơ chế Hơi Thở Cuối Cùng (Last Stand):
     * - Chỉ kích hoạt duy nhất 1 lần trong cả trận (countHealLastStand = 1)
     * - Hồi 35% HP tối đa (không bao giờ hồi đầy 100%)
     * - Sóng xung kích hất văng người chơi xung quanh ra cự ly an toàn
     * - Giải trừ toàn bộ các hiệu ứng khống chế đang dính
     * - Bật trạng thái Cuồng Nộ (Berserk) tăng tốc độ ra đòn
     */
    protected void triggerLastStand(Player plAtt) {
        this.countHealLastStand++;
        this.isBerserk = true;

        // 1. Giải trừ các hiệu ứng khống chế nếu đang dính
        if (this.effectSkill != null) {
            EffectSkillService.gI().removeAnTroi(this);
            EffectSkillService.gI().removeThoiMien(this);
            EffectSkillService.gI().removeStun(this);
            EffectSkillService.gI().removeBlindDCTT(this);
            EffectSkillService.gI().removeMabuHold(this);
        }

        // 2. Hồi phục 35% HP tối đa
        long hpHeal = this.nPoint.hpMax * 35 / 100;
        PlayerService.gI().hoiPhuc(this, hpHeal, 0);

        // 3. Sóng xung kích hất văng người chơi xung quanh (trong bán kính 160px)
        if (this.zone != null) {
            List<Player> players = new ArrayList<>(this.zone.getPlayers());
            for (int i = 0; i < players.size(); i++) {
                Player pl = players.get(i);
                if (pl != null && !pl.isDie() && !pl.equals(this) && Util.getDistance(this, pl) <= 160) {
                    if (pl.location != null && this.zone.map != null) {
                        int pushX = pl.location.x + (pl.location.x < this.location.x ? -120 : 120);
                        pushX = Math.max(50, Math.min(this.zone.map.mapWidth - 50, pushX));
                        Service.gI().setPos(pl, pushX, pl.location.y);
                    }
                }
            }
        }

        // 4. Lời thoại thức tỉnh đầy khí chất
        this.chat(getRandomAwakeningChat());
    }

    /**
     * Dịch chuyển áp sát tức thời ra sau lưng mục tiêu khi bị xả chưởng từ xa liên tục
     */
    protected void triggerFlashStep(Player pl) {
        if (pl == null || pl.location == null || this.zone == null || this.zone.map == null) {
            return;
        }
        int behindX = pl.location.x + (pl.location.x >= this.location.x ? -45 : 45);
        behindX = Math.max(50, Math.min(this.zone.map.mapWidth - 50, behindX));
        this.moveTo(behindX, pl.location.y);
        this.playerTarger = pl;
        this.lastTimeTargetPlayer = System.currentTimeMillis();
        this.timeTargetPlayer = Util.nextInt(6000, 8000);
        this.chat(getRandomFlashStepChat());
    }

    // ================= SOUL DIALOGUE SYSTEM =================
    private void chatDodge() {
        String[] dodgeChats = {
            "Ngươi chỉ đánh trúng cái bóng của ta thôi!",
            "Còn non và xanh lắm!",
            "Gió thoảng qua thôi à?",
            "Xí hụt rồi nhé!",
            "Chậm như rùa bò vậy!"
        };
        this.chat(dodgeChats[Util.nextInt(0, dodgeChats.length - 1)]);
    }

    private String getRandomCounterChat() {
        String[] counterChats = {
            "Quá chậm!",
            "Đòn đánh gãi ngứa!",
            "Chỉ có thế thôi sao?",
            "Ngươi sơ hở rồi!",
            "Trả lại ngươi đòn này!"
        };
        return counterChats[Util.nextInt(0, counterChats.length - 1)];
    }

    private String getRandomFlashStepChat() {
        String[] flashChats = {
            "Ngươi nhìn đi đâu thế?",
            "Ta ở ngay sau lưng ngươi này!",
            "Bắn lén từ xa thế là đủ rồi!",
            "Chạy đi đâu cho thoát?"
        };
        return flashChats[Util.nextInt(0, flashChats.length - 1)];
    }

    private String getRandomHeavyDamageChat() {
        String[] heavyChats = {
            "Hự... Cú đánh khá đấy, nhưng chưa đủ đâu!",
            "Khá khen cho đòn đánh vừa rồi!",
            "Ngươi làm ta bắt đầu thấy đau rồi đấy!",
            "Gan lắm! Dám làm ta trầy xước!"
        };
        return heavyChats[Util.nextInt(0, heavyChats.length - 1)];
    }

    private String getRandomAwakeningChat() {
        String[] awakeningChats = {
            "Các ngươi nghĩ có thể dễ dàng hạ gục ta sao? Chưa đâu!",
            "Khốn kiếp... Ta sẽ cho các ngươi thấy cơn thịnh nộ thực sự!",
            "Sức mạnh thực sự của ta... bây giờ mới bắt đầu bộc phát!",
            "Đừng vội mừng, trò chơi sinh tử chỉ mới bắt đầu thôi!"
        };
        return awakeningChats[Util.nextInt(0, awakeningChats.length - 1)];
    }

    private String getRandomNearDeathChat() {
        String[] nearDeathChats = {
            "Không thể nào... Ta không thể gục ngã ở đây...!",
            "Khốn kiếp... Lũ sâu bọ các ngươi...!",
            "Ta sẽ kéo theo các ngươi chôn cùng!"
        };
        return nearDeathChats[Util.nextInt(0, nearDeathChats.length - 1)];
    }

    private String getRandomKillPlayerChat() {
        String[] killChats = {
            "Một kẻ ngáng đường đã ngã xuống! Ai là người tiếp theo?",
            "Yếu ớt đến thảm hại!",
            "Biết lượng sức mình chưa?",
            "Ngươi còn phải luyện tập thêm nhiều năm nữa!"
        };
        return killChats[Util.nextInt(0, killChats.length - 1)];
    }
}

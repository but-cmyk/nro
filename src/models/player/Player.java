package models.player;

import utils.Functions;
import models.npc.NonInteractiveNPC;
import models.radar.Card;
import models.radar.RadarCard;
import services.RadarService;
import services.phoban.MajinBuuService;
import models.skill.PlayerSkill;
import java.util.List;
import models.clan.Clan;
import models.intrinsic.IntrinsicPlayer;
import models.item.Item;
import models.item.ItemTime;
import models.npc.mabu.MagicTree;
import consts.ConstPlayer;
import consts.ConstTask;
import models.npc.mabu.MabuEgg;
import models.mob.MobMe;
import models.clan.ClanMember;
import consts.ConstAchievement;
import consts.ConstItem;
import consts.ConstMap;
import models.map.Zone;
import interfaces.IPVP;
import java.time.LocalDateTime;
import models.matches.TYPE_LOSE_PVP;
import models.skill.Skill;
import services.Service;
import network.session.MySession;
import models.task.TaskPlayer;
import network.io.Message;
import server.Client;
import services.EffectSkillService;
import services.player.FriendAndEnemyService;
import services.map.MapService;
import services.PetService;
import services.player.PlayerService;
import services.TaskService;
import services.map.ChangeMapService;
import utils.Logger;
import utils.Util;
import java.util.ArrayList;
import java.util.Date;
import services.phoban.BlackBallWarService;
import managers.tournament.The23rdMartialArtCongressManager;
import models.map.ItemMap;
import models.map.MaBuHold;
import models.phoban.MajinBuu14H;
import services.phoban.SuperDivineWaterService;
import models.tournament.The23rdMartialArtCongress;
import services.player.InventoryService;
import services.phoban.NgocRongNamecService;
import models.combine.list.Combine;
import models.map.Boss10;
import server.Maintenance;
import services.map.NpcService;
import services.ItemService;
import models.npc.Npc;
import services.map.NpcManager;
import consts.ConstNpc;

public class Player extends Character {

    public long lastTimeSave;
    public boolean isJoinMap = false;
    public boolean isCookingBanhDay = false;
    public boolean isCookingBanhChung = false;
    public int cookingBanhDayTime;
    public int cookingBanhChungTime;
    public long lastTimeUpdateCooking;
    public long lastTimeEatPea;
    public List<Integer> idEffChar = new ArrayList<>();
    private MySession session;

    public MySession getSession() {
        return this.session;
    }

    public void setSession(MySession session) {
        this.session = session;
    }

    public int luotNhanBuaMienPhi = 1;
    public int diemDanhSuKien = 1;
    public Date firstTimeLogin;
    public int luckySpins;
    public long lastTimeNotifyCantGoToMap; // anti spam thông báo qua khu

    public int diemsukien;
    public int phaobong;
    public int lixi;
    public int account_id;
    public int arenaWins;
    public boolean isConfirmingChangePet;
    public Item itemToConfirmChangePet;
    public long lastTimeChallengeCommeson;

    public PlayerEffect effect;
    public boolean isNewMember;
    public int deltaTime;
    public byte typePk;
    public byte cFlag;
    public boolean haveTennisSpaceShip;
    public long banhtrung;
    public long banhtet;
    public long lastTimeUseOption, lastTimeLeaveClan, lastTimeRemoveClan;

    public boolean isBot;
    public boolean isCopy;
    public boolean beforeDispose;

    public List<String> textRuongGo = new ArrayList<>();
    public List<Integer> BoughtSkill = new ArrayList<>();
    public int mbv = 0;
    public boolean baovetaikhoan;
    public long mbvtime;

    public int timeGohome;
    public long lastUpdateGohomeTime;
    public boolean goHome;

    public long lastPkCommesonTime;

    public boolean callBossPocolo;
    public Zone zoneSieuThanhThuy;
    public boolean winSTT;
    public long lastTimeWinSTT;
    public long lastTimeUpdateSTT;

    public MajinBuu14H maBu2H;
    public boolean isMabuHold;
    public MaBuHold maBuHold;
    public int precentMabuHold;
    public boolean isPhuHoMapMabu;

    public boolean danhanthoivang;
    public long lastRewardGoldBarTime;

    public int timesPerDayBDKB = 0;
    public long lastTimeJoinBDKB;

    public boolean joinCDRD;
    public long lastTimeJoinCDRD;
    public boolean talkToThuongDe;
    public boolean talkToThanMeo;
    public long timeChangeMap144;

    public long lastTimeJoinDT;

    public int typeChibi;
    public long lastTimeChibi;
    public long lastTimeUpdateChibi;

    public boolean doesNotAttack;
    public long lastTimePlayerNotAttack;
    public int timeNotAttack = 1800000;

    public boolean isPet;
    public boolean isNewPet;
    public boolean isNewPet1;
    public boolean isBoss;
    public boolean isPlayer;
    public IPVP pvp;
    public byte maxTime = 30;
    public byte type = 0;
    public boolean isOffline = false;

    public String notify = null;

    public int mapIdBeforeLogout = -1;
    public List<Zone> mapBlackBall;
    public List<Zone> mapMaBu;

    public List<Player> temporaryEnemies = new ArrayList<>();

    public Zone mapBeforeCapsule;
    public List<Zone> mapCapsule;
    public Pet pet;
    public NewPet newPet;
    public MobMe mobMe;
    public SetClothes setClothes;
    public MabuEgg mabuEgg;
    public TaskPlayer playerTask;
    public ItemTime itemTime;
    public Fusion fusion;
    public MagicTree magicTree;
    public IntrinsicPlayer playerIntrinsic;
    public Inventory inventory;
    public PlayerSkill playerSkill;
    public Combine combineNew;
    public IDMark idMark;
    public Charms charms;
    public EffectSkin effectSkin;
    public RewardBlackBall rewardBlackBall;
    public FightMabu fightMabu;
    public NewSkill newSkill;
    public Satellite satellite;
    public Achievement achievement;
    public GiftCode giftCode;
    public Traning traning;

    public Clan clan;
    public ClanMember clanMember;

    public List<Friend> friends;
    public List<Enemy> enemies;

    public boolean justRevived;
    public long lastTimeRevived;
    public long lastTimeAttack;

    public long timeChangeZone;
    public long lastUseOptionTime;

    public short idNRNM = -1;
    public short idGo = -1;
    public long lastTimePickNRNM;

    public List<Card> Cards = new ArrayList<>();

    public int levelWoodChest;
    public long goldChallenge;
    public long rubyChallenge;
    public long lastTimeRewardWoodChest;
    public List<Item> itemsWoodChest = new ArrayList<>();
    public long lastTimePKDHVT23;

    public boolean lostByDeath;
    public boolean isPKDHVT;

    public int xSend;
    public int ySend;
    public boolean isFly;

    public long lastTimeSelectMenu = 0L;
    public long lastTimeShenronAppeared;

    // vo dai sinh tu
    public long lastTimePKVoDaiSinhTu;
    public boolean haveRewardVDST;
    public int thoiVangVoDaiSinhTu;
    public long timePKVDST;

    public int binhChonHatMit;
    public int binhChonPlayer;
    public Zone zoneBinhChon;

    public ItemEvent itemEvent;

    public int levelLuyenTap;
    public boolean isThachDau;
    public long tnsmLuyenTap;
    public boolean dangKyTapTuDong;
    public long lastTimeOffline;
    public int mapIdDangTapTuDong;
    public int lastMapOffline;
    public int lastZoneOffline;
    public int lastXOffline;
    public String thongBaoTapTuDong;
    public boolean teleTapTuDong;

    public boolean isTrade;

    public int timesPerDayCuuSat;
    public long lastTimeCuuSat;
    public boolean nhanVangNangVIP;
    public boolean nhanDeTuNangVIP;
    public boolean nhanSKHVIP;

    public long totalDamageTaken;
    public boolean thongBaoChangeMap;
    public String textThongBaoChangeMap;
    public boolean thongBaoThua;
    public String textThongBaoThua;

    public SuperRank superRank;

    public boolean canReward;
    public boolean changeMapVIP;
    public boolean haveReward;

    public HocKyNang hocKyNang;
    public List<Integer> BoughtSkills = new ArrayList<Integer>();
    public List<Item> itemsTradeWVP = new ArrayList<>();
    public boolean tradeWVP;

    private DropItem dropItem;
    public ItemMap veTinhTriLuc = null;
    public ItemMap veTinhSinhLuc = null;
    public ItemMap veTinhPhongThu = null;
    public ItemMap veTinhTriTue = null;

    public long lastTimeCheckVeTinh = 0;
    public long lastTimeVeTinhTriLuc = 0;
    public long lastTimeVeTinhSinhLuc = 0;
    public LocalDateTime lastCheckIn;

    public Player() {
        effect = new PlayerEffect(this);
        lastTimeUseOption = System.currentTimeMillis();
        lastUseOptionTime = System.currentTimeMillis();
        lastTimeSave = System.currentTimeMillis();
        location = new Location();
        hocKyNang = new HocKyNang();
        nPoint = new NPoint(this);
        inventory = new Inventory();
        playerSkill = new PlayerSkill(this);
        setClothes = new SetClothes(this);
        effectSkill = new EffectSkill(this);
        fusion = new Fusion(this);
        playerIntrinsic = new IntrinsicPlayer();
        rewardBlackBall = new RewardBlackBall(this);
        fightMabu = new FightMabu(this);
        idMark = new IDMark();
        combineNew = new Combine();
        playerTask = new TaskPlayer();
        friends = new ArrayList<>();
        enemies = new ArrayList<>();
        itemTime = new ItemTime(this);
        charms = new Charms();
        effectSkin = new EffectSkin(this);
        newSkill = new NewSkill(this);
        achievement = new Achievement(this);
        giftCode = new GiftCode();
        traning = new Traning();
        itemEvent = new ItemEvent(this);
        superRank = new SuperRank(this);
        dropItem = new DropItem(this);
    }

    @Override
    public boolean isDie() {
        if (this.nPoint != null) {
            return this.nPoint.hp <= 0;
        }
        return true;
    }

    public void sendMessage(Message msg) {
        if (this.session != null) {
            session.sendMessage(msg);
        }
    }

    public boolean isPl() {
        return isPlayer && !isPet && !isBoss && !isBot && !isNewPet && !isNewPet1 && !(this instanceof NonInteractiveNPC);
    }



    public void update() {
        if (!this.beforeDispose && !isBot) {
            try {
                if (this.zone != null || (!this.isPl() && this.zone == null)) {
                    if (itemTime != null) itemTime.update();
                    if (magicTree != null) magicTree.update();

                    if (isCookingBanhDay && cookingBanhDayTime > 0) {
                        if (Util.canDoWithTime(lastTimeUpdateCooking, 1000)) {
                            cookingBanhDayTime--;
                            lastTimeUpdateCooking = System.currentTimeMillis();
                            if (cookingBanhDayTime > 0) {
                                NpcService.gI().npcChat(this, ConstNpc.NOI_BANH,
                                        "Đang nấu bánh dầy\n|7|Thời gian còn lại: " + cookingBanhDayTime + " giây.");
                            } else {
                                finishCookingBanhDay();
                            }
                        }
                    }

                    if (isCookingBanhChung && cookingBanhChungTime > 0) {
                        if (Util.canDoWithTime(lastTimeUpdateCooking, 1000)) {
                            cookingBanhChungTime--;
                            lastTimeUpdateCooking = System.currentTimeMillis();
                            if (cookingBanhChungTime > 0) {
                                NpcService.gI().npcChat(this, ConstNpc.NOI_BANH,
                                        "Đang nấu bánh chưng\n|7|Thời gian còn lại: " + cookingBanhChungTime + " giây.");
                            } else {
                                finishCookingBanhChung();
                            }
                        }
                    }

                    if (this.isPl() && this.zone != null && this.zone.map.mapId == this.gender + 21
                            && (TaskService.gI().getIdTask(this) == ConstTask.TASK_0_0 || TaskService.gI().getIdTask(this) == ConstTask.TASK_0_1)) {
                        this.playerTask.taskMain.index = 2;
                        TaskService.gI().sendTaskMain(this);
                    }
                }

                if ((this.zone != null && !MapService.gI().isHome(this.zone.map.mapId)) || (!this.isPl() && this.zone == null)) {
                    if (isPl() && idMark != null && idMark.isBan() && Util.canDoWithTime(idMark.getLastTimeBan(), 5000)) {
                        Client.gI().kickSession(session);
                        return;
                    }

                    if (nPoint != null) nPoint.update();
                    if (fusion != null) fusion.update();
                    if (effectSkill != null) effectSkill.update();
                    if (mobMe != null) mobMe.update();
                    if (effectSkin != null) effectSkin.update();
                    if (pet != null) pet.update();
                    if (newPet != null) newPet.update();
                    if (satellite != null) satellite.update();

                    // Logic Chibi
                    if (this.isPl() && !this.isDie() && this.effectSkill != null && !this.effectSkill.isChibi && Util.canDoWithTime(lastTimeChibi, 300000)) {
                        if (Util.isTrue(1, 10) && !MapService.gI().isMapBlackBallWar(this.zone.map.mapId)) {
                            EffectSkillService.gI().setChibi(this, 600000);
                        }
                        lastTimeChibi = System.currentTimeMillis();
                    }
                    if (this.isPl() && !this.isDie() && this.effectSkill != null && this.effectSkill.isChibi && Util.canDoWithTime(lastTimeUpdateChibi, 1000)) {
                        int amount;
                        if (this.typeChibi == 1 && this.nPoint.mp < this.nPoint.mpMax) {
                            amount = this.nPoint.mpMax / 10;
                            if (this.nPoint.mpMax - this.nPoint.mp < amount) this.nPoint.mp = this.nPoint.mpMax;
                            else this.nPoint.mp += amount;
                            PlayerService.gI().sendInfoMp(this);
                        } else if (this.typeChibi == 3 && this.nPoint.hp < this.nPoint.hpMax) {
                            amount = this.nPoint.hpMax / 10;
                            if (this.nPoint.hpMax - this.nPoint.hp < amount) this.nPoint.hp = this.nPoint.hpMax;
                            else this.nPoint.hp += amount;
                            PlayerService.gI().sendInfoHp(this);
                        }
                        lastTimeUpdateChibi = System.currentTimeMillis();
                    }

                    if (this.isPl() && this.achievement != null) {
                        this.achievement.done(ConstAchievement.HOAT_DONG_CHAM_CHI, 1000);
                    }

                    // Ve tinh logic
                    // Giải pháp 1: Tạo bản copy (khuyên dùng)
                    if (this.zone != null && !this.zone.items.isEmpty() && Util.canDoWithTime(lastTimeCheckVeTinh, 2000)) {
                        List<ItemMap> itemsCopy = new ArrayList<>(this.zone.items);
                        for (ItemMap item : itemsCopy) {
                            if (item != null && Util.myGetDistance(this.location.x, this.location.y, item.x, item.y) <= ConstMap.RANGE_VE_TINH && item.isBelongToMe(this)) {
                                switch (item.itemTemplate.id) {
                                    case ConstItem.VE_TINH_SINH_LUC -> veTinhSinhLuc = item;
                                    case ConstItem.VE_TINH_PHONG_THU -> veTinhPhongThu = item;
                                    case ConstItem.VE_TINH_TRI_TUE -> veTinhTriTue = item;
                                    case ConstItem.VE_TINH_TRI_LUC -> veTinhTriLuc = item;
                                }
                            }
                        }
                        lastTimeCheckVeTinh = System.currentTimeMillis();
                    }

                    if (!this.isDie() && Util.canDoWithTime(lastTimeVeTinhTriLuc, ConstMap.TIME_HOI_VE_TINH)
                            && ItemMap.isInVeTinhRange(this, ConstItem.VE_TINH_TRI_LUC, this.location.x, this.location.y)) {
                        int mpTriLuc = NPoint.calPercent(this.nPoint.mpMax, ConstMap.PERCENT_VE_TINH_TRI_LUC);
                        this.nPoint.addMp(mpTriLuc);
                        PlayerService.gI().hoiPhuc(this, 0, mpTriLuc);
                        lastTimeVeTinhTriLuc = System.currentTimeMillis();
                    }

                    if (!this.isDie() && Util.canDoWithTime(lastTimeVeTinhSinhLuc, ConstMap.TIME_HOI_VE_TINH)
                            && ItemMap.isInVeTinhRange(this, ConstItem.VE_TINH_SINH_LUC, this.location.x, this.location.y)) {
                        int hpSinhLuc = NPoint.calPercent(this.nPoint.hpMax, ConstMap.PERCENT_VE_TINH_SINH_LUC);
                        this.nPoint.addHp(hpSinhLuc);
                        PlayerService.gI().hoiPhuc(this, hpSinhLuc, 0);
                        lastTimeVeTinhSinhLuc = System.currentTimeMillis();
                    }

                    // Mabu Hold logic
                    if (this.isPl() && this.effectSkill != null && this.effectSkill.isMabuHold) {
                        this.nPoint.subHP(this.nPoint.hpMax / 100);
                        if (Util.isTrue(1, 10)) Service.gI().chat(this, "Cứu tôi với");
                        PlayerService.gI().sendInfoHp(this);
                        if (this.precentMabuHold > 15 || this.nPoint.hp <= 0) {
                            EffectSkillService.gI().removeMabuHold(this);
                            if (this.nPoint.hp <= 0) setDie();
                        }
                    }

                    if (this.zone != null && this.effectSkin != null && !MapService.gI().isMapBlackBallWar(this.zone.map.mapId)) {
                        if (this.effectSkin.xHPKI > 1) {
                            this.effectSkin.xHPKI = 1;
                            this.nPoint.calPoint();
                            Service.gI().point(this);
                        }
                        if (this.effectSkin.xDame > 1) {
                            this.effectSkin.xDame = 1;
                            this.nPoint.calPoint();
                            Service.gI().point(this);
                        }
                    }

                    if (this.isPl() && this.zone != null) {
                        fixBlackBallWar();
                    }

                    if (this.zone != null && this.zone.map.mapId == (21 + this.gender) && this.mabuEgg != null) {
                        this.mabuEgg.sendMabuEgg();
                    }

                    if (this.isPhuHoMapMabu && this.zone != null && !MapService.gI().isMapMabu2H(this.zone.map.mapId)) {
                        this.isPhuHoMapMabu = false;
                        this.nPoint.calPoint();
                        Service.gI().point(this);
                        Service.gI().Send_Info_NV(this);
                        Service.gI().Send_Caitrang(this);
                    }

                    // Logic CDRD
                    if (this.isPl() && this.clan != null && this.clan.ConDuongRanDoc != null && this.joinCDRD
                            && this.clan.ConDuongRanDoc.allMobsDead && this.talkToThanMeo && this.zone.map.mapId == 47
                            && Util.canDoWithTime(timeChangeMap144, 5000)) {
                        ChangeMapService.gI().changeMapYardrat(this, this.clan.ConDuongRanDoc.getMapById(144), 300 + Util.nextInt(-100, 100), 312);
                        this.timeChangeMap144 = System.currentTimeMillis();
                    }

                    if (this.isPl() && this.zone != null && !MapService.gI().isMapMaBu(this.zone.map.mapId) && (this.cFlag == 9 || this.cFlag == 10)) {
                        Service.gI().changeFlag(this, 0);
                    }

                    if (this.isPl() && this.superRank != null) {
                        if (Util.isAfterMidnight(this.superRank.lastRewardTime)) {
                            this.superRank.reward();
                        }
                    }

                    if (this.isPl() && this.zone != null && MapService.gI().isMapMaBu(this.zone.map.mapId) && this.cFlag != 9 && this.cFlag != 10) {
                        Service.gI().changeFlag(this, Util.nextInt(9, 10));
                    }

                    if (dropItem != null) dropItem.update();

                    if (this.zone != null && MapService.gI().isMap22h(this.zone.map.mapId)) {
                        Boss10.gI().update(this);
                    }
                    MajinBuuService.gI().update(this);
                    SuperDivineWaterService.gI().update(this);

                    if (!isBoss && this.idMark != null && this.idMark.isGotoFuture() && Util.canDoWithTime(this.idMark.getLastTimeGoToFuture(), 60000)) {
                        ChangeMapService.gI().changeMapBySpaceShip(this, 102, -1, Util.nextInt(60, 200));
                        this.idMark.setGotoFuture(false);
                    }
                }
            } catch (Exception e) {
                Logger.logException(Player.class, e, "Lỗi tại player update: " + this.name);
            }
        }
    }

    // ... (Giữ nguyên các mảng ID outfit) ...
    private static final short[][] idOutfitFusion = {
            {380, 381, 382}, {383, 384, 385}, {391, 392, 393},
            {870, 871, 872}, {873, 874, 875}, {867, 868, 869},
            {1663, 1666, 1667}, {1673, 1676, 1677}, {1678, 1681, 1682}
    };

    public static final short[][] idOutfitGod = {{-1, 472, 473}, {-1, 476, 477}, {-1, 474, 475}};

    // ... (Giữ nguyên mảng Halloween, CaRot, Socola2, Mafuba) ...
    public static final short[][][] idOutfitHalloween = {
            {{545, 548, 549}, {547, 548, 549}, {546, 548, 549}},
            {{2082, 2085, 2086}, {2084, 2085, 2086}, {2083, 2085, 2086}},
            {{760, 761, 762}, {760, 761, 762}, {760, 761, 762}},
            {{654, 655, 656}, {654, 655, 656}, {654, 655, 656}},
            {{651, 652, 653}, {651, 652, 653}, {651, 652, 653}}};

    public static final short[][][] idOutfitCaRot = {
            {{406, 407, 408},  {406, 407, 408} ,  {406, 407, 408}},
            {{406, 407, 408},  {406, 407, 408} ,  {406, 407, 408}},
            {{406, 407, 408},  {406, 407, 408} ,  {406, 407, 408}},
            {{406, 407, 408},  {406, 407, 408} ,  {406, 407, 408}},
            {{406, 407, 408},  {406, 407, 408} ,  {406, 407, 408}}};

    public static final short[][][] idOutfitSocola2 = {
            {{412, 413, 414}, {412, 413, 414} ,  {412, 413, 414}},
            {{412, 413, 414}, {412, 413, 414} ,  {412, 413, 414}},
            {{412, 413, 414}, {412, 413, 414} ,  {412, 413, 414}},
            {{412, 413, 414}, {412, 413, 414} ,  {412, 413, 414}},
            {{412, 413, 414}, {412, 413, 414} ,  {412, 413, 414}}};

    public static final short[][] idOutfitMafuba = {{2030, 2031, 2032}, {-1, -1, -1}, {1218, 1219, 1220}};

    public int getHat() {
        return -1;
    }

    // FIX: Tối ưu hóa loop, loại bỏ Stream API để tránh NPE và lag
    public byte getAura() {
        if (!isPl() || this.Cards == null || this.Cards.isEmpty()) {
            return -1;
        }
        for (Card card : this.Cards) {
            if (card != null && card.Used == 1 && card.Level > 1) {
                // Kiểm tra Service trước khi gọi
                if (RadarService.gI() != null && RadarService.gI().RADAR_TEMPLATE != null) {
                    for (RadarCard r : RadarService.gI().RADAR_TEMPLATE) {
                        if (r.Id == card.Id) {
                            return (byte) r.AuraId;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public byte getEffFront() {
        if (this.inventory == null || this.inventory.itemsBody == null || this.inventory.itemsBody.size() < 5) {
            return -1;
        }
        // ... (Giữ nguyên logic getEffFront nhưng nên cẩn thận index) ...
        // Logic cũ của bạn khá thủ công, nhưng vẫn hoạt động nếu size đủ.
        // Tôi giữ nguyên để tránh thay đổi logic game, chỉ lưu ý check null ở trên.
        int levelAo = 0;
        Item.ItemOption optionLevelAo = null;
        int levelQuan = 0;
        Item.ItemOption optionLevelQuan = null;
        int levelGang = 0;
        Item.ItemOption optionLevelGang = null;
        int levelGiay = 0;
        Item.ItemOption optionLevelGiay = null;
        int levelNhan = 0;
        Item.ItemOption optionLevelNhan = null;
        Item itemAo = this.inventory.itemsBody.get(0);
        Item itemQuan = this.inventory.itemsBody.get(1);
        Item itemGang = this.inventory.itemsBody.get(2);
        Item itemGiay = this.inventory.itemsBody.get(3);
        Item itemNhan = this.inventory.itemsBody.get(4);
        for (Item.ItemOption io : itemAo.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelAo = io.param;
                optionLevelAo = io;
                break;
            }
        }
        for (Item.ItemOption io : itemQuan.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelQuan = io.param;
                optionLevelQuan = io;
                break;
            }
        }
        for (Item.ItemOption io : itemGang.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelGang = io.param;
                optionLevelGang = io;
                break;
            }
        }
        for (Item.ItemOption io : itemGiay.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelGiay = io.param;
                optionLevelGiay = io;
                break;
            }
        }
        for (Item.ItemOption io : itemNhan.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelNhan = io.param;
                optionLevelNhan = io;
                break;
            }
        }
        if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 8 && levelQuan >= 8 && levelGang >= 8 && levelGiay >= 8 && levelNhan >= 8) {
            return 8;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 7 && levelQuan >= 7 && levelGang >= 7 && levelGiay >= 7 && levelNhan >= 7) {
            return 7;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 6 && levelQuan >= 6 && levelGang >= 6 && levelGiay >= 6 && levelNhan >= 6) {
            return 6;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 5 && levelQuan >= 5 && levelGang >= 5 && levelGiay >= 5 && levelNhan >= 5) {
            return 5;
        } else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
                && levelAo >= 4 && levelQuan >= 4 && levelGang >= 4 && levelGiay >= 4 && levelNhan >= 4) {
            return 4;
        } else {
            return -1;
        }
    }

    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) return idOutfitMafuba[effectSkill.typeBinh][0];
        if (effectSkill != null && effectSkill.isStone) return 454;
        if (effectSkill != null && effectSkill.isSocola2) return idOutfitSocola2[effectSkill.idOutfitSocola2][this.gender][0];
        if (effectSkill != null && effectSkill.isHalloween) return idOutfitHalloween[effectSkill.idOutfitHalloween][this.gender][0];
        if (effectSkill != null && effectSkill.isCaRot) return idOutfitCaRot[effectSkill.idOutfitCaRot][this.gender][0];

        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 412;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                return idOutfitFusion[3 + this.gender][0];
            }  else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            return idOutfitFusion[6 + this.gender][0];
        }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int headId = inventory.itemsBody.get(5).template.head;
            if (headId != -1) return (short) headId;
        }
        return this.head;
    }

    public int version() {
        if (session == null) return 1;
        return session.version;
    }

    public boolean isVersionAbove(int version) {
        if (session == null) return false;
        return version() >= version;
    }

    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) return idOutfitMafuba[effectSkill.typeBinh][1];
        if (effectSkill != null && effectSkill.isStone) return 455;
        if (effectSkill != null && effectSkill.isSocola2) return idOutfitSocola2[effectSkill.idOutfitSocola2][this.gender][1];
        if (effectSkill != null && effectSkill.isHalloween) return idOutfitHalloween[effectSkill.idOutfitHalloween][this.gender][1];
        if (effectSkill != null && effectSkill.isCaRot) return idOutfitCaRot[effectSkill.idOutfitCaRot][this.gender][1];

        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 413;
        } else if (isPhuHoMapMabu && fusion != null && fusion.typeFusion == ConstPlayer.NON_FUSION) {
            return idOutfitGod[this.gender][1];
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                return idOutfitFusion[3 + this.gender][1];
            }else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[6 + this.gender][1];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int body = inventory.itemsBody.get(5).template.body;
            if (body != -1) return (short) body;
        }
        if (inventory != null && inventory.itemsBody.get(0).isNotNullItem()) {
            return inventory.itemsBody.get(0).template.part;
        }
        return (short) (gender == ConstPlayer.NAMEC ? 59 : 57);
    }

    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) return idOutfitMafuba[effectSkill.typeBinh][2];
        if (effectSkill != null && effectSkill.isStone) return 456;
        if (effectSkill != null && effectSkill.isSocola2) return idOutfitSocola2[effectSkill.idOutfitSocola2][this.gender][2];
        if (effectSkill != null && effectSkill.isHalloween) return idOutfitHalloween[effectSkill.idOutfitHalloween][this.gender][2];
        if (effectSkill != null && effectSkill.isCaRot) return idOutfitCaRot[effectSkill.idOutfitCaRot][this.gender][2];

        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 414;
        } else if (isPhuHoMapMabu && fusion != null && fusion.typeFusion == ConstPlayer.NON_FUSION) {
            return idOutfitGod[this.gender][2];
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                return idOutfitFusion[3 + this.gender][2];
            }else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[6 + this.gender][2];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int leg = inventory.itemsBody.get(5).template.leg;
            if (leg != -1) return (short) leg;
        }
        if (inventory != null && inventory.itemsBody.get(1).isNotNullItem()) {
            return inventory.itemsBody.get(1).template.part;
        }
        return (short) (gender == 1 ? 60 : 58);
    }

    // FIX: Làm gọn logic, xóa đoạn lặp lại, sửa index check
    public short getFlagBag() {
        if (this.idMark != null && this.idMark.isHoldBlackBall()) {
            return 31;
        } else if (this.idNRNM >= 353 && this.idNRNM <= 359) {
            return 30;
        }
        if (TaskService.gI().getIdTask(this) == ConstTask.TASK_3_2) {
            return 28;
        }
        // Kiểm tra an toàn cho inventory
        if (this.inventory != null && this.inventory.itemsBody != null && this.inventory.itemsBody.size() > 8) {
            Item item = this.inventory.itemsBody.get(8); // Index 8 là item thứ 9 (Cờ/Balo)
            if (item != null && item.isNotNullItem()) {
                return item.template.part;
            }
        }
        if (this.clan != null) {
            return (short) this.clan.imgId;
        }
        return -1;
    }

    // FIX: Thêm kiểm tra size để tránh IndexOutOfBounds
    public short getMount() {
        if (isPl() && this.inventory != null && this.inventory.itemsBody != null && this.inventory.itemsBody.size() > 9) {
            Item item = this.inventory.itemsBody.get(9); // Index 9 là item thứ 10 (Thú cưỡi)
            if (item == null || !item.isNotNullItem()) {
                return -1;
            }
            if (item.template.type == 24) {
                if (item.template.gender == 3 || item.template.gender == this.gender) {
                    return item.template.id;
                } else {
                    return -1;
                }
            } else {
                if (item.template.id < 500) {
                    return item.template.id;
                } else {
                    return (short) (item.template.part + 30_000);
                }
            }
        }
        return -1;
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (plAtt != null && !plAtt.equals(this)) {
                setTemporaryEnemies(plAtt);
            }
            if (this.isPl() && this.effectSkill != null && this.effectSkill.isMabuHold && this.zone != null && this.zone.map.mapId == 128) {
                this.precentMabuHold++;
                if (this.precentMabuHold > 15) {
                    services.EffectSkillService.gI().removeMabuHold(this);
                    Service.gI().sendThongBao(this, "Bạn đã được đồng đội giải cứu khỏi Mabư!");
                }
                return 1;
            }
            // ... (Logic cũ giữ nguyên) ...
            if (plAtt != null && plAtt.playerSkill.skillSelect != null && !plAtt.isBoss && MapService.gI().isMapMaBu(this.zone.map.mapId)) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC, Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN ->
                            damage = damage > this.nPoint.hpMax / 20 ? this.nPoint.hpMax / 20 : damage;
                }
            }
            if (plAtt != null && this.nPoint.islinhthuydanhbac) {
                Service.gI().sendThongBao(plAtt, "Không thể tấn công! Vì người chơi này đã nạp lần đầu!");
                return 0;
            }
            if (plAtt != null && plAtt.idNRNM != -1 && (this.isBoss || this.isNewPet)) {
                return 1;
            }
            if (plAtt != null && (plAtt.idNRNM != -1 || this.idNRNM != -1) && plAtt.clan != null && this.clan != null && plAtt.clan == this.clan) {
                Service.gI().chatJustForMe(plAtt, this, "Ê cùng bang mà");
                return 0;
            }
            if (!Util.canDoWithTime(this.lastTimeRevived, 1500)) {
                return 0;
            }

            // ... (Logic tính toán né đòn/giáp giữ nguyên) ...

            int tlGiap = this.nPoint.tlGiap;
            int tlNeDon = this.nPoint.tlNeDon;

            if (plAtt != null && !isMobAttack && plAtt.playerSkill.skillSelect != null) {
                // Giảm né đòn dựa trên cx của đối thủ
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC, Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN, Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.DICH_CHUYEN_TUC_THOI ->
                            tlNeDon -= plAtt.nPoint.tlchinhxac;
                    default ->
                            tlNeDon = 0;
                }
                // Xuyên giáp
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC -> {
                        if (tlGiap - plAtt.nPoint.tlxgc >= 0) tlGiap -= plAtt.nPoint.tlxgc;
                        else tlGiap = 0;
                    }
                    case Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN -> {
                        if (tlGiap - plAtt.nPoint.tlxgcc >= 0) tlGiap -= plAtt.nPoint.tlxgcc;
                        else tlGiap = 0;
                    }
                }
            }

            if (piercing) tlGiap = 0;
            if (tlNeDon > 90) tlNeDon = 90;
            if (tlGiap > 86) tlGiap = 86;

            if (Util.isTrue(tlNeDon, 100)) return 0;

            damage -= ((damage / 100) * tlGiap);

            if (!piercing) {
                damage = this.nPoint.subDameInjureWithDeff(damage);
            }

            // Fix: Đảm bảo damage không âm và không quá lớn
            if (damage < 0) damage = 0;
            damage = Math.min(damage, 2_000_000_000L);

            // ... (Logic Giáp Xên, Khiên...) ...
            boolean isUseGX = false;
            if (!piercing && plAtt != null && plAtt.playerSkill.skillSelect != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC, Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN, Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.DICH_CHUYEN_TUC_THOI ->
                            isUseGX = true;
                }
            }
            if ((isUseGX || isMobAttack) && this.itemTime != null) {
                if (this.itemTime.isUseGiapXen && !this.itemTime.isUseGiapXen2) {
                    damage /= 2;
                }
                if (this.itemTime.isUseGiapXen2) {
                    damage = damage / 100 * 40;
                }
            }
            if (!piercing && effectSkill.isShielding && !isMobAttack) {
                if (this.idMark != null) this.idMark.setDamePST((int) Math.min(damage, 2_000_000_000L));
                if (damage > nPoint.hpMax) EffectSkillService.gI().breakShield(this);
                damage = 1;
            }

            if (!piercing && plAtt == null && isMobAttack && (this.charms.tdBatTu > System.currentTimeMillis() || this.effectSkill != null && this.effectSkill.isHalloween) && damage >= this.nPoint.hp) {
                damage = this.nPoint.hp - 1;
            }

            if (this.zone.map.mapId == 129) {
                if (damage >= this.nPoint.hp) {
                    this.lostByDeath = true;
                    The23rdMartialArtCongress mc = The23rdMartialArtCongressManager.gI().getMC(zone);
                    if (mc != null) mc.die();
                    return 0;
                }
            }
            if (this.zone.map.mapId == 51) {
                this.totalDamageTaken += damage;
            }

            this.nPoint.subHP(damage);

            if ((plAtt != null || isMobAttack) && isDie() && !isBoss && !isNewPet && !isNewPet1) {
                if (plAtt != null && this.isPl()) {
                    if (this.idMark != null && this.idMark.isHoldBlackBall()) {
                        // TaskService.gI().checkDoneTaskNRSD(plAtt);
                    }
                }
                if (Util.isTrue(this.nPoint.tlBom, 100)) {
                    setBom(plAtt);
                } else {
                    setDie(plAtt);
                }
            }
            return (int) damage;
        } else {
            return 0;
        }
    }

    public void setTemporaryEnemies(Player pl) {
        if (!temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    protected void setBom(Player plAtt) {
        setDie(plAtt);
    }

    public void setDie() {
        this.setDie(null);
    }

    protected void setDie(Player plAtt) {
        TaskService.gI().checkDoneTaskKillPlayer(plAtt);
        if (this.isPl()) {
            long vangtru = this.nPoint.power / 1000000;
            if (vangtru > 32000) vangtru = 32000;
            int vang = (int) vangtru - Util.nextInt(10, 100);

            if (this.inventory.gold >= vang && vang >= 1) {
                this.inventory.gold -= vang;
                Service.gI().sendMoney(this);
                vang = vang * 95 / 100;
                if (vang < 10000) {
                    Service.gI().dropItemMap(this.zone, new ItemMap(zone, 189, vang, this.location.x, this.location.y, this.id));
                } else if (vang < 20000) {
                    Service.gI().dropItemMap(this.zone, new ItemMap(zone, 188, vang, this.location.x, this.location.y, this.id));
                } else {
                    Service.gI().dropItemMap(this.zone, new ItemMap(zone, 190, vang, this.location.x, this.location.y, this.id));
                }
            }
        }

        if (this.effectSkin.xHPKI > 1) {
            this.effectSkin.xHPKI = 1;
            Service.gI().point(this);
        }
        if (this.effectSkin.xDame > 1) {
            this.effectSkin.xDame = 1;
            Service.gI().point(this);
        }
        this.playerSkill.prepareQCKK = false;
        this.playerSkill.prepareLaze = false;
        this.playerSkill.prepareTuSat = false;
        this.effectSkill.removeSkillEffectWhenDie();
        nPoint.setHp(0);
        nPoint.setMp(0);
        if (this.mobMe != null) {
            this.mobMe.mobMeDie();
            this.mobMe.dispose();
            this.mobMe = null;
        }
        Service.gI().charDie(this);
        if (!this.isPet && !this.isNewPet && !isBot && !this.isNewPet1 && !this.isBoss && plAtt != null && !plAtt.isPet && !plAtt.isNewPet && !plAtt.isNewPet1 && !plAtt.isBoss) {
            if (!plAtt.itemTime.isUseAnDanh) {
                FriendAndEnemyService.gI().addEnemy(this, plAtt);
            }
        }
        this.typePk = 0;
        if (this.pvp != null && this.zone.map.mapId != 140) {
            this.pvp.lose(this, TYPE_LOSE_PVP.DEAD);
        }
        BlackBallWarService.gI().dropBlackBall(this);
        NgocRongNamecService.gI().dropNamekBall(this);
    }

    public void setClanMember() {
        if (this.clanMember != null) {
            this.clanMember.powerPoint = this.nPoint.power;
            this.clanMember.head = this.getHead();
            this.clanMember.body = this.getBody();
            this.clanMember.leg = this.getLeg();
        }
    }

    public boolean isAdmin() {
        return this.session != null && this.session.isAdmin;
    }

    public void setJustRevivaled() {
        this.justRevived = true;
        this.lastTimeRevived = System.currentTimeMillis();
    }

    public boolean isActive() {
        return (this.isPl() && this.session != null && this.session.actived) || (this.isPet && ((Pet) this).master.session != null && ((Pet) this).master.session.actived);
    }

    public void sendNewPet() {
        if (isPl() && inventory != null && inventory.itemsBody.size() > 7) {
            Item it = inventory.itemsBody.get(7);
            if (it != null && it.isNotNullItem() && newPet == null) {
                switch (it.template.id) {
                    case 942 -> { PetService.Pet2(this, 966, 967, 968); Service.gI().point(this); }
                    case 943 -> { PetService.Pet2(this, 969, 970, 971); Service.gI().point(this); }
                    case 944 -> { PetService.Pet2(this, 972, 973, 974); Service.gI().point(this); }
                    case 967 -> { PetService.Pet2(this, 1050, 1051, 1052); Service.gI().point(this); }
                    case 968 -> { PetService.Pet2(this, 1183, 1184, 1185); Service.gI().point(this); }
                }
            }
        }
    }

    private void fixBlackBallWar() {
        int x = this.location.x;
        int y = this.location.y;
        if (this.zone.map.mapId >= 85 && this.zone.map.mapId <= 91) {
            if (this.isPl()) {
                if (x < 24 || x > this.zone.map.mapWidth - 24 || y < 0 || y > this.zone.map.mapHeight - 24) {
                    if (MapService.gI().getWaypointPlayerIn(this) == null) {
                        Service.gI().resetPoint(this, x, this.zone.map.yPhysicInTop(this.location.x, 100));
                        this.nPoint.hp -= this.nPoint.hpMax / 10;
                        PlayerService.gI().sendInfoHp(this);
                        return;
                    }
                }
                int yTop = this.zone.map.yPhysicInTop(this.location.x, this.location.y);
                if (yTop >= this.zone.map.mapHeight - 24) {
                    Service.gI().resetPoint(this, x, this.zone.map.yPhysicInTop(this.location.x, 100));
                    this.nPoint.hp -= this.nPoint.hpMax / 10;
                    PlayerService.gI().sendInfoHp(this);
                }
            }
        }
    }

    public void move(int _toX, int _toY) {
        if (_toX != this.location.x) this.location.x = _toX;
        if (_toY != this.location.y) this.location.y = _toY;
        MapService.gI().sendPlayerMove(this);
    }

    // FIX: Thêm kiểm tra null trước khi clear để tránh crash
    public void dispose() {
        this.isJoinMap = false;
        if (itemsTradeWVP != null) {
            if (!itemsTradeWVP.isEmpty()) {
                for (Item item : itemsTradeWVP) {
                    InventoryService.gI().addItemBag(this, item);
                }
            }
            itemsTradeWVP.clear();
            itemsTradeWVP = null;
        }
        if (pet != null) {
            pet.dispose();
            pet = null;
        }
        if (newPet != null) {
            newPet.dispose();
            newPet = null;
        }
        if (mapBlackBall != null) {
            mapBlackBall.clear();
            mapBlackBall = null;
        }
        zone = null;
        mapBeforeCapsule = null;
        if (mapMaBu != null) {
            mapMaBu.clear();
            mapMaBu = null;
        }
        mapBeforeCapsule = null;
        if (mapCapsule != null) {
            mapCapsule.clear();
            mapCapsule = null;
        }
        if (mobMe != null) {
            mobMe.dispose();
            mobMe = null;
        }
        location = null;
        if (setClothes != null) {
            setClothes.dispose();
            setClothes = null;
        }
        if (effectSkill != null) {
            effectSkill.dispose();
            effectSkill = null;
        }
        if (mabuEgg != null) {
            mabuEgg.dispose();
            mabuEgg = null;
        }
        if (playerTask != null) {
            playerTask.dispose();
            playerTask = null;
        }
        if (itemTime != null) {
            itemTime.dispose();
            itemTime = null;
        }
        if (fusion != null) {
            fusion.dispose();
            fusion = null;
        }
        if (magicTree != null) {
            magicTree.dispose();
            magicTree = null;
        }
        if (playerIntrinsic != null) {
            playerIntrinsic.dispose();
            playerIntrinsic = null;
        }
        if (inventory != null) {
            inventory.dispose();
            inventory = null;
        }
        if (playerSkill != null) {
            playerSkill.dispose();
            playerSkill = null;
        }
        if (combineNew != null) {
            combineNew.dispose();
            combineNew = null;
        }
        if (idMark != null) {
            idMark.dispose();
            idMark = null;
        }
        if (charms != null) {
            charms.dispose();
            charms = null;
        }
        if (effectSkin != null) {
            effectSkin.dispose();
            effectSkin = null;
        }
        if (nPoint != null) {
            nPoint.dispose();
            nPoint = null;
        }
        if (rewardBlackBall != null) {
            rewardBlackBall.dispose();
            rewardBlackBall = null;
        }
        if (pvp != null) {
            pvp.dispose();
            pvp = null;
        }
        if (superRank != null) {
            superRank.dispose();
            superRank = null;
        }
        if (dropItem != null) {
            dropItem.dispose();
            dropItem = null;
        }
        satellite = null;
        if (achievement != null) {
            achievement.dispose();
            achievement = null;
        }
        if (giftCode != null) {
            giftCode.dispose();
            giftCode = null;
        }
        traning = null;
        if (mapCapsule != null) {
            mapCapsule.clear();
            mapCapsule = null;
        }
        if (Cards != null) {
            Cards.clear();
            Cards = null;
        }
        if (itemsWoodChest != null) {
            itemsWoodChest.clear();
            itemsWoodChest = null;
        }
        if (friends != null) {
            friends.clear();
            friends = null;
        }
        if (enemies != null) {
            enemies.clear();
            enemies = null;
        }
        if (temporaryEnemies != null) {
            temporaryEnemies.clear();
            temporaryEnemies = null;
        }
        itemsWoodChest = null;
        Cards = null;
        itemEvent = null;
        maBu2H = null;
        maBuHold = null;
        zoneSieuThanhThuy = null;
        thongBaoTapTuDong = null;
        notify = null;
        clan = null;
        clanMember = null;
        friends = null;
        enemies = null;
        session = null;
        newSkill = null;
        name = null;
        textThongBaoChangeMap = null;
        textThongBaoThua = null;
    }

    public short getHeadThuCung() {
        if (this.isPl() && this.inventory != null && this.inventory.itemsBody != null
                && this.inventory.itemsBody.size() > 7
                && this.inventory.itemsBody.get(7).isNotNullItem()) {
            return (short) (this.inventory.itemsBody.get(7).template.head);
        }
        return -1;
    }

    public short getBodyThuCung() {
        if (this.isPl() && this.inventory != null && this.inventory.itemsBody != null
                && this.inventory.itemsBody.size() > 7
                && this.inventory.itemsBody.get(7).isNotNullItem()) {
            return (short) (this.inventory.itemsBody.get(7).template.body);
        }
        return -1;
    }

    public short getLegThuCung() {
        if (this.isPl() && this.inventory != null && this.inventory.itemsBody != null
                && this.inventory.itemsBody.size() > 7
                && this.inventory.itemsBody.get(7).isNotNullItem()) {
            return (short) (this.inventory.itemsBody.get(7).template.leg);
        }
        return -1;
    }

    public void finishCookingBanhDay() {
        this.isCookingBanhDay = false;
        this.cookingBanhDayTime = 0;
        Item comNep = InventoryService.gI().findItemBag(this, 1434);
        Item botGao = InventoryService.gI().findItemBag(this, 1440);
        Item muoiTieu = InventoryService.gI().findItemBag(this, 1438);
        Item chaLua = InventoryService.gI().findItemBag(this, 1437);
        Item banhDay = ItemService.gI().createNewItem((short) 1435);
        int vang = 1000000;
        if ((comNep != null && comNep.quantity >= 99)
                && (botGao != null && botGao.quantity >= 5)
                && (muoiTieu != null && muoiTieu.quantity >= 2)
                && (chaLua != null && chaLua.quantity >= 1)
                && this.inventory.gold >= vang) {
            InventoryService.gI().subQuantityItemsBag(this, comNep, 99);
            InventoryService.gI().subQuantityItemsBag(this, botGao, 5);
            InventoryService.gI().subQuantityItemsBag(this, muoiTieu, 2);
            InventoryService.gI().subQuantityItemsBag(this, chaLua, 1);
            this.inventory.gold -= vang;
            Service.gI().sendMoney(this);
            InventoryService.gI().addItemBag(this, banhDay);
            InventoryService.gI().sendItemBags(this);
            NpcService.gI().createMenu(this, ConstNpc.IGNORE_MENU, ConstNpc.NOI_BANH,
                    "Đã nấu xong bánh dầy!\n|7|Bạn đã nhận được " + banhDay.template.name,
                    "Nhận Ngay");
        } else {
            Service.gI().sendThongBao(this, "Thiếu nguyên liệu hoặc vàng để hoàn thành nấu bánh dầy!");
        }
    }

    public void finishCookingBanhChung() {
        this.isCookingBanhChung = false;
        this.cookingBanhChungTime = 0;
        Item comNepNe = InventoryService.gI().findItemBag(this, 1434);
        Item dauXanh = InventoryService.gI().findItemBag(this, 1441);
        Item thitTuoi = InventoryService.gI().findItemBag(this, 1442);
        Item banhChung = ItemService.gI().createNewItem((short) 1436);
        int vang = 5000000;
        if ((comNepNe != null && comNepNe.quantity >= 99)
                && (dauXanh != null && dauXanh.quantity >= 2)
                && (thitTuoi != null && thitTuoi.quantity >= 2)
                && this.inventory.gold >= vang) {
            InventoryService.gI().subQuantityItemsBag(this, comNepNe, 99);
            InventoryService.gI().subQuantityItemsBag(this, dauXanh, 2);
            InventoryService.gI().subQuantityItemsBag(this, thitTuoi, 2);
            this.inventory.gold -= vang;
            Service.gI().sendMoney(this);
            InventoryService.gI().addItemBag(this, banhChung);
            InventoryService.gI().sendItemBags(this);
            NpcService.gI().createMenu(this, ConstNpc.IGNORE_MENU, ConstNpc.NOI_BANH,
                    "Đã nấu xong bánh chưng!\n|7|Bạn đã nhận được " + banhChung.template.name,
                    "Nhận Ngay");
        } else {
            Service.gI().sendThongBao(this, "Thiếu nguyên liệu hoặc vàng để hoàn thành nấu bánh chưng!");
        }
    }
}

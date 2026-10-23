package managers.boss;

import models.boss.Boss;
import consts.BossID;
import models.boss.boss_list.GoldenFrieza.*;
import models.boss.boss_list.Nappa.Rambo;
import models.boss.boss_list.Nappa.MapDauDinh;
import models.boss.boss_list.Nappa.Kuku;
import models.boss.boss_list.Android.Android19;
import models.boss.boss_list.Android.Pic;
import models.boss.boss_list.Android.Android14;
import models.boss.boss_list.Android.Poc;
import models.boss.boss_list.Android.Android13;
import models.boss.boss_list.Android.KingKong;
import models.boss.boss_list.Android.DrKore;
import models.boss.boss_list.Android.Android15;
import models.boss.boss_list.Cooler.Cooler;
import models.boss.boss_list.Cell.SieuBoHung;
import models.boss.boss_list.Cell.XenBoHung;
import models.boss.boss_list.Broly.Broly;
import models.boss.boss_list.ChristmasEvent.OngGiaNoel;
import models.boss.boss_list.HalloweenEvent.BiMa;
import models.boss.boss_list.TaoPaiPai.TaoPaiPai;
import models.boss.boss_list.Frieza.Fide;
import models.boss.boss_list.MajinBuu12H.Mabu;
import models.boss.boss_list.MajinBuu12H.BuiBui;
import models.boss.boss_list.MajinBuu12H.BuiBui2;
import models.boss.boss_list.MajinBuu12H.Cadic;
import models.boss.boss_list.MajinBuu12H.Drabura;
import models.boss.boss_list.MajinBuu12H.Drabura2;
import models.boss.boss_list.MajinBuu12H.Drabura3;
import models.boss.boss_list.MajinBuu12H.Goku;
import models.boss.boss_list.MajinBuu12H.Yacon;
import models.boss.boss_list.MajinBuu14H.Mabu2H;
import models.boss.boss_list.MajinBuu14H.SuperBu;
import models.boss.boss_list.GinyuForce.SO1;
import models.boss.boss_list.GinyuForce.SO2;
import models.boss.boss_list.GinyuForce.SO3;
import models.boss.boss_list.GinyuForce.SO4;
import models.boss.boss_list.GinyuForce.TDT;
import models.boss.boss_list.NamekGinyuForce.SO1_NM;
import models.boss.boss_list.NamekGinyuForce.SO2_NM;
import models.boss.boss_list.NamekGinyuForce.SO3_NM;
import models.boss.boss_list.NamekGinyuForce.SO4_NM;
import models.boss.boss_list.NamekGinyuForce.TDT_NM;
import models.boss.boss_list.Earth.BIDO;
import models.boss.boss_list.Earth.BOJACK;
import models.boss.boss_list.Earth.BUJIN;
import models.boss.boss_list.Earth.KOGU;
import models.boss.boss_list.Earth.SUPER_BOJACK;
import models.boss.boss_list.Earth.ZANGYA;
import models.boss.boss_list.Yardart.CHIENBINH0;
import models.boss.boss_list.Yardart.CHIENBINH1;
import models.boss.boss_list.Yardart.CHIENBINH2;
import models.boss.boss_list.Yardart.CHIENBINH3;
import models.boss.boss_list.Yardart.CHIENBINH4;
import models.boss.boss_list.Yardart.CHIENBINH5;
import models.boss.boss_list.Yardart.DOITRUONG5;
import models.boss.boss_list.Yardart.TANBINH0;
import models.boss.boss_list.Yardart.TANBINH1;
import models.boss.boss_list.Yardart.TANBINH2;
import models.boss.boss_list.Yardart.TANBINH3;
import models.boss.boss_list.Yardart.TANBINH4;
import models.boss.boss_list.Yardart.TANBINH5;
import models.boss.boss_list.Yardart.TAPSU0;
import models.boss.boss_list.Yardart.TAPSU1;
import models.boss.boss_list.Yardart.TAPSU2;
import models.boss.boss_list.Yardart.TAPSU3;
import models.boss.boss_list.Yardart.TAPSU4;
import models.boss.boss_list.Cell.XENCON1;
import models.boss.boss_list.Cell.XENCON2;
import models.boss.boss_list.Cell.XENCON3;
import models.boss.boss_list.Cell.XENCON4;
import models.boss.boss_list.Cell.XENCON5;
import models.boss.boss_list.Cell.XENCON6;
import models.boss.boss_list.Cell.XENCON7;
import models.player.Player;
import network.io.Message;
import services.map.MapService;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;
import models.boss.AnTrom;
import models.boss.boss_list.Black.BlackGoku;
import models.boss.boss_list.ChristmasEvent.TuanLoc;
import models.boss.boss_list.HalloweenEvent.Doi;
import models.boss.boss_list.HalloweenEvent.MaTroi;
import models.boss.boss_list.NewBoss.*;
import models.boss.boss_list.ThoDaiKa.ThoDaiKa;
import models.boss.boss_list.huydiet.Cumber;
import models.map.Zone;
import utils.Logger;
import utils.Util;
import consts.BossStatus;
import consts.ConstItem;
import services.Service;
import services.player.InventoryService;

public class BossManager {

    private static BossManager instance;
    //public static byte ratioReward = 10;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new CopyOnWriteArrayList<>();
    }

    protected final List<Boss> bosses;

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public List<Boss> getBosses() {
        return this.bosses;
    }

    @FunctionalInterface
    public interface BossSupplier {
        Boss get() throws Exception;
    }

    private static final Map<Integer, BossSupplier> BOSS_REGISTRY = new HashMap<>();

    static {
        BOSS_REGISTRY.put(BossID.CUMBER, Cumber::new);
        BOSS_REGISTRY.put(BossID.AN_TROM, AnTrom::new);
        BOSS_REGISTRY.put(BossID.THO_DAI_KA, ThoDaiKa::new);
        BOSS_REGISTRY.put(BossID.TUAN_LOC, TuanLoc::new);
        BOSS_REGISTRY.put(BossID.BLACK_GOKU, BlackGoku::new);
        BOSS_REGISTRY.put(BossID.TAP_SU_0, TAPSU0::new);
        BOSS_REGISTRY.put(BossID.TAP_SU_1, TAPSU1::new);
        BOSS_REGISTRY.put(BossID.TAP_SU_2, TAPSU2::new);
        BOSS_REGISTRY.put(BossID.TAP_SU_3, TAPSU3::new);
        BOSS_REGISTRY.put(BossID.TAP_SU_4, TAPSU4::new);
        BOSS_REGISTRY.put(BossID.TAN_BINH_5, TANBINH5::new);
        BOSS_REGISTRY.put(BossID.TAN_BINH_0, TANBINH0::new);
        BOSS_REGISTRY.put(BossID.TAN_BINH_1, TANBINH1::new);
        BOSS_REGISTRY.put(BossID.TAN_BINH_2, TANBINH2::new);
        BOSS_REGISTRY.put(BossID.TAN_BINH_3, TANBINH3::new);
        BOSS_REGISTRY.put(BossID.TAN_BINH_4, TANBINH4::new);
        BOSS_REGISTRY.put(BossID.CHIEN_BINH_5, CHIENBINH5::new);
        BOSS_REGISTRY.put(BossID.CHIEN_BINH_0, CHIENBINH0::new);
        BOSS_REGISTRY.put(BossID.CHIEN_BINH_1, CHIENBINH1::new);
        BOSS_REGISTRY.put(BossID.CHIEN_BINH_2, CHIENBINH2::new);
        BOSS_REGISTRY.put(BossID.CHIEN_BINH_3, CHIENBINH3::new);
        BOSS_REGISTRY.put(BossID.CHIEN_BINH_4, CHIENBINH4::new);
        BOSS_REGISTRY.put(BossID.DOI_TRUONG_5, DOITRUONG5::new);
        BOSS_REGISTRY.put(BossID.SO_4, SO4::new);
        BOSS_REGISTRY.put(BossID.SO_3, SO3::new);
        BOSS_REGISTRY.put(BossID.SO_2, SO2::new);
        BOSS_REGISTRY.put(BossID.SO_1, SO1::new);
        BOSS_REGISTRY.put(BossID.TIEU_DOI_TRUONG, TDT::new);
        BOSS_REGISTRY.put(BossID.SO_4_NM, SO4_NM::new);
        BOSS_REGISTRY.put(BossID.SO_3_NM, SO3_NM::new);
        BOSS_REGISTRY.put(BossID.SO_2_NM, SO2_NM::new);
        BOSS_REGISTRY.put(BossID.SO_1_NM, SO1_NM::new);
        BOSS_REGISTRY.put(BossID.TIEU_DOI_TRUONG_NM, TDT_NM::new);
        BOSS_REGISTRY.put(BossID.BUJIN, BUJIN::new);
        BOSS_REGISTRY.put(BossID.KOGU, KOGU::new);
        BOSS_REGISTRY.put(BossID.ZANGYA, ZANGYA::new);
        BOSS_REGISTRY.put(BossID.BIDO, BIDO::new);
        BOSS_REGISTRY.put(BossID.BOJACK, BOJACK::new);
        BOSS_REGISTRY.put(BossID.SUPER_BOJACK, SUPER_BOJACK::new);
        BOSS_REGISTRY.put(BossID.KUKU, Kuku::new);
        BOSS_REGISTRY.put(BossID.MAP_DAU_DINH, MapDauDinh::new);
        BOSS_REGISTRY.put(BossID.RAMBO, Rambo::new);
        BOSS_REGISTRY.put(BossID.TAU_PAY_PAY_DONG_NAM_KARIN, TaoPaiPai::new);
        BOSS_REGISTRY.put(BossID.DRABURA, Drabura::new);
        BOSS_REGISTRY.put(BossID.BUI_BUI, BuiBui::new);
        BOSS_REGISTRY.put(BossID.BUI_BUI_2, BuiBui2::new);
        BOSS_REGISTRY.put(BossID.YA_CON, Yacon::new);
        BOSS_REGISTRY.put(BossID.DRABURA_2, Drabura2::new);
        BOSS_REGISTRY.put(BossID.GOKU, Goku::new);
        BOSS_REGISTRY.put(BossID.CADIC, Cadic::new);
        BOSS_REGISTRY.put(BossID.MABU_12H, Mabu::new);
        BOSS_REGISTRY.put(BossID.DRABURA_3, Drabura3::new);
        BOSS_REGISTRY.put(BossID.MABU, Mabu2H::new);
        BOSS_REGISTRY.put(BossID.SUPERBU, SuperBu::new);
        BOSS_REGISTRY.put(BossID.FIDE, Fide::new);
        BOSS_REGISTRY.put(BossID.DR_KORE, DrKore::new);
        BOSS_REGISTRY.put(BossID.ANDROID_19, Android19::new);
        BOSS_REGISTRY.put(BossID.ANDROID_13, Android13::new);
        BOSS_REGISTRY.put(BossID.ANDROID_14, Android14::new);
        BOSS_REGISTRY.put(BossID.ANDROID_15, Android15::new);
        BOSS_REGISTRY.put(BossID.PIC, Pic::new);
        BOSS_REGISTRY.put(BossID.POC, Poc::new);
        BOSS_REGISTRY.put(BossID.KING_KONG, KingKong::new);
        BOSS_REGISTRY.put(BossID.XEN_BO_HUNG, XenBoHung::new);
        BOSS_REGISTRY.put(BossID.SIEU_BO_HUNG, SieuBoHung::new);
        BOSS_REGISTRY.put(BossID.XEN_CON_1, XENCON1::new);
        BOSS_REGISTRY.put(BossID.XEN_CON_2, XENCON2::new);
        BOSS_REGISTRY.put(BossID.XEN_CON_3, XENCON3::new);
        BOSS_REGISTRY.put(BossID.XEN_CON_4, XENCON4::new);
        BOSS_REGISTRY.put(BossID.XEN_CON_5, XENCON5::new);
        BOSS_REGISTRY.put(BossID.XEN_CON_6, XENCON6::new);
        BOSS_REGISTRY.put(BossID.XEN_CON_7, XENCON7::new);
        BOSS_REGISTRY.put(BossID.COOLER, Cooler::new);
        BOSS_REGISTRY.put(BossID.BROLY, Broly::new);
        BOSS_REGISTRY.put(BossID.GOLDEN_FRIEZA, GoldenFrieza::new);
        BOSS_REGISTRY.put(BossID.DEATH_BEAM_1, () -> new DeathBeam(BossID.DEATH_BEAM_1, 14600));
        BOSS_REGISTRY.put(BossID.DEATH_BEAM_2, () -> new DeathBeam(BossID.DEATH_BEAM_2, 14700));
        BOSS_REGISTRY.put(BossID.DEATH_BEAM_3, () -> new DeathBeam(BossID.DEATH_BEAM_3, 14800));
        BOSS_REGISTRY.put(BossID.DEATH_BEAM_4, () -> new DeathBeam(BossID.DEATH_BEAM_4, 14900));
        BOSS_REGISTRY.put(BossID.DEATH_BEAM_5, () -> new DeathBeam(BossID.DEATH_BEAM_5, 15000));
        BOSS_REGISTRY.put(BossID.BIMA, BiMa::new);
        BOSS_REGISTRY.put(BossID.MATROI, MaTroi::new);
        BOSS_REGISTRY.put(BossID.DOI, Doi::new);
        BOSS_REGISTRY.put(BossID.AN_TROM, AnTrom::new);
        BOSS_REGISTRY.put(BossID.THO_DAI_KA, ThoDaiKa::new);
    }

    public void loadBoss() {
        this.createBoss(BossID.KUKU);
        this.createBoss(BossID.MAP_DAU_DINH);
        this.createBoss(BossID.RAMBO);
        this.createBoss(BossID.TIEU_DOI_TRUONG);
        this.createBoss(BossID.FIDE);
        this.createBoss(BossID.DR_KORE);
        this.createBoss(BossID.ANDROID_14);
        this.createBoss(BossID.KING_KONG);

        this.createBoss(BossID.XEN_BO_HUNG);
        this.createBoss(BossID.SIEU_BO_HUNG);

        this.createBoss(BossID.COOLER);
        this.createBoss(BossID.BLACK_GOKU, 2);
        this.createBoss(BossID.GOLDEN_FRIEZA, 5);
        this.createBoss(BossID.BROLY, 10);
        this.createBoss(BossID.CHILLER);
        this.createBoss(BossID.CUMBER);

        this.createBoss(BossID.TIEU_DOI_TRUONG_NM);
        this.createBoss(BossID.BOJACK);
        this.createBoss(BossID.SUPER_BOJACK);
        this.createBoss(BossID.RONG_1_SAO);
        this.createBoss(BossID.RONG_2_SAO);
        this.createBoss(BossID.RONG_3_SAO);
        this.createBoss(BossID.RONG_4_SAO);
        this.createBoss(BossID.RONG_5_SAO);
        this.createBoss(BossID.RONG_6_SAO);
        this.createBoss(BossID.RONG_7_SAO);
        this.createBoss(BossID.AN_TROM, 1);
        this.createBoss(BossID.THO_DAI_KA, 1);

        try {
            new ChoRach();
            new Raiti();
            new Xibachao();
            new BaDo();
        } catch (Exception e) {
        }
    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            BossSupplier supplier = BOSS_REGISTRY.get(bossID);
            if (supplier != null) {
                return supplier.get();
            }
            return null;
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }

    public Boss getBossByIndex(int index) {
        if (index < 0) {
            return null;
        }
        try {
            if (index < this.bosses.size()) {
                return this.bosses.get(index);
            }
            int brolyIndex = index - this.bosses.size();
            if (brolyIndex >= 0 && brolyIndex < BrolyManager.gI().bosses.size()) {
                return BrolyManager.gI().bosses.get(brolyIndex);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void showListBoss(Player player) {
        if (!player.isAdmin() && InventoryService.gI().findItemBag(player, ConstItem.MAY_DO_BOSS) == null) {
            Service.gI().sendThongBao(player, "Bạn cần có Máy dò boss để sử dụng chức năng này!");
            return;
        }
        player.idMark.setMenuType(3);
        Message msg;
        try {
            List<Boss> allBosses = new java.util.ArrayList<>(this.bosses);
            List<Boss> validBosses = new java.util.ArrayList<>();
            for (Boss boss : allBosses) {
                if (boss != null && boss.data != null && boss.data.length > 0 && isBossTaskMain(boss)) {
                    validBosses.add(boss);
                }
            }

            int count = Math.min(validBosses.size(), 100);
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Máy Dò Boss");
            msg.writer().writeByte(count);
            for (int i = 0; i < count; i++) {
                Boss boss = validBosses.get(i);
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null && (boss.bossStatus == BossStatus.ACTIVE || boss.bossStatus == BossStatus.CHAT_S)) {
                    msg.writer().writeUTF("Đang xuất hiện");
                    msg.writer().writeUTF(boss.zone.map.mapName + " Khu " + boss.zone.zoneId);
                } else if (boss.bossStatus == BossStatus.DIE || boss.bossStatus == BossStatus.LEAVE_MAP) {
                    msg.writer().writeUTF("Đã bị hạ");
                    msg.writer().writeUTF("---");
                } else if (boss.bossStatus == BossStatus.RESPAWN || boss.bossStatus == BossStatus.JOIN_MAP) {
                    msg.writer().writeUTF("Sắp xuất hiện");
                    msg.writer().writeUTF("---");
                } else {
                    msg.writer().writeUTF("Chưa xuất hiện");
                    msg.writer().writeUTF("---");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(BossManager.class, e, "Lỗi showListBoss");
        }
    }

    public static boolean isBossTaskMain(Boss boss) {
        if (boss == null) {
            return false;
        }
        int id = (int) boss.id;
        // 1. Kuku, Mập Đầu Đinh, Rambo (Nhiệm vụ 21)
        if (id == BossID.KUKU || id == BossID.MAP_DAU_DINH || id == BossID.RAMBO) {
            return true;
        }
        // 2. Tiểu Đội Sát Thủ (Nhiệm vụ 22)
        if (id == BossID.SO_4 || id == BossID.SO_3 || id == BossID.SO_2 || id == BossID.SO_1 
                || id == BossID.TIEU_DOI_TRUONG || id == BossID.TDST
                || id == BossID.SO_4_NM || id == BossID.SO_3_NM || id == BossID.SO_2_NM 
                || id == BossID.SO_1_NM || id == BossID.TIEU_DOI_TRUONG_NM) {
            return true;
        }
        // 3. Fide Đại Ca (Nhiệm vụ 23)
        if (id == BossID.FIDE) {
            return true;
        }
        // 4. Sát Thủ Android (Nhiệm vụ 25, 26, 27)
        if (id == BossID.ANDROID_19 || id == BossID.DR_KORE 
                || id == BossID.ANDROID_13 || id == BossID.ANDROID_14 || id == BossID.ANDROID_15 
                || id == BossID.PIC || id == BossID.POC || id == BossID.KING_KONG) {
            return true;
        }
        // 5. Xên Bọ Hung & Xên Con (Nhiệm vụ 28, 29)
        if (id == BossID.XEN_BO_HUNG || id == BossID.SIEU_BO_HUNG 
                || (id >= BossID.XEN_CON_7 && id <= BossID.XEN_CON_1)) {
            return true;
        }
        // 6. Ma Bư 12h & Đệ tử Babidi (Nhiệm vụ 30)
        if (id == BossID.DRABURA || id == BossID.DRABURA_2 || id == BossID.DRABURA_3 
                || id == BossID.BUI_BUI || id == BossID.BUI_BUI_2 
                || id == BossID.YA_CON || id == BossID.MABU_12H) {
            return true;
        }
        // 7. Black Goku & Chiller (Nhiệm vụ 31, 32)
        if (id == BossID.BLACK_GOKU || id == BossID.CHILLER || id == BossID.CHILL_1 || id == BossID.CHILL_2) {
            return true;
        }
        return false;
    }

    public Boss getBossById(int bossId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return this.bosses.stream().filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie()).findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream().filter(boss -> boss.id < -100_000_000 && !boss.isDie()).findFirst().orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && boss.zone != null && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst().orElse(null);
    }

    public void update() {
        for (int i = this.bosses.size() - 1; i >= 0; i--) {
            if (i < this.bosses.size()) {
                Boss boss = this.bosses.get(i);
                if (boss != null) {
                    try {
                        boss.update();
                    } catch (Exception e) {
                        Logger.logException(BossManager.class, e, "Lỗi update boss: " + boss.name);
                    }
                }
            }
        }
    }

    public String getStatus() {
        StringBuilder status = new StringBuilder();
        int aliveCount = 0;
        int deadCount = 0;

        for (Boss boss : this.bosses) {
            if (boss.isDie()) {
                deadCount++;
            } else {
                aliveCount++;
                if (boss.zone != null) {
                    status.append(boss.data[0].getName())
                            .append(" - Map: ").append(boss.zone.map.mapName)
                            .append(" (Khu ").append(boss.zone.zoneId).append(")")
                            .append("\n");
                } else {
                    status.append(boss.data[0].getName()).append(" - Chưa spawn\n");
                }
            }
        }

        return String.format("Tổng Boss: %d\nĐang hoạt động: %d\nĐã chết: %d\n\nDanh sách Boss đang hoạt động:\n%s",
                this.bosses.size(), aliveCount, deadCount, status);
    }

    public static boolean isSafeMapForRoamingBoss(int mapId) {
        if (mapId == 0 || mapId == 7 || mapId == 14) return true; // Làng Aru, Mori, Kakarot
        if (mapId >= 21 && mapId <= 26) return true; // Nhà riêng và trạm tàu vũ trụ 3 hành tinh
        if (mapId == 5 || mapId == 13 || mapId == 20) return true; // Đảo Kame, Guru, Bunma
        if (mapId >= 39 && mapId <= 62) return true; // Tương lai, địa ngục, phó bản
        if (mapId >= 111 && mapId <= 130) return true; // Mabu, gas, doanh trại
        if (mapId == 102 || mapId == 103 || mapId == 112) return true;
        return MapService.gI().isMapDoanhTrai(mapId) 
                || MapService.gI().isMapMaBu(mapId) 
                || MapService.gI().isMapBlackBallWar(mapId)
                || MapService.gI().isMapPhoBan(mapId);
    }

    public static Zone getRoamingZoneWithPlayer(Zone currentZone, Class<? extends Boss> bossClass) {
        List<Player> players = server.Client.gI().getPlayers();
        List<Zone> candidateZones = new java.util.ArrayList<>();
        for (Player pl : players) {
            if (pl != null && pl.isPl() && !pl.isBot && !pl.isPet && !pl.isNewPet) {
                Zone z = pl.zone;
                if (z != null && z.map != null && z.isKhongCoTrongTaiTrongKhu() 
                        && !isSafeMapForRoamingBoss(z.map.mapId)
                        && (currentZone == null || !currentZone.equals(z))
                        && pl.id != -1000000) {
                    // Kiểm tra độc quyền khu: Nếu khu đã có bất kỳ Boss nào đang sống -> KHÔNG CHỌN
                    boolean hasLivingBoss = false;
                    List<Player> bosses = z.getBosses();
                    if (bosses != null && !bosses.isEmpty()) {
                        for (Player b : bosses) {
                            if (b != null && !b.isDie()) {
                                hasLivingBoss = true;
                                break;
                            }
                        }
                    }
                    if (!hasLivingBoss) {
                        candidateZones.add(z);
                    }
                }
            }
        }
        if (!candidateZones.isEmpty()) {
            return candidateZones.get(Util.nextInt(0, candidateZones.size() - 1));
        }
        return null;
    }

    public static Zone getRandomOutdoorZone() {
        int[] outdoorMaps = {
            1, 2, 3, 4, 6, 27, 28, 29, 30, // Trái Đất
            8, 9, 10, 11, 12, 31, 32, 33, 34, // Namek
            15, 16, 17, 18, 19, 35, 36, 37, 38, // Sayian
            63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 84 // Vùng lân cận/Fide
        };
        // Tìm zone ngoài trời trống Boss (thử tối đa 15 lần để tìm map/zone hợp lệ)
        for (int i = 0; i < 15; i++) {
            int mapId = outdoorMaps[Util.nextInt(0, outdoorMaps.length - 1)];
            models.map.Map map = MapService.gI().getMapById(mapId);
            if (map != null && !map.zones.isEmpty()) {
                List<Zone> emptyBossZones = new java.util.ArrayList<>();
                for (Zone z : map.zones) {
                    if (z != null) {
                        boolean hasLivingBoss = false;
                        List<Player> bosses = z.getBosses();
                        if (bosses != null && !bosses.isEmpty()) {
                            for (Player b : bosses) {
                                if (b != null && !b.isDie()) {
                                    hasLivingBoss = true;
                                    break;
                                }
                            }
                        }
                        if (!hasLivingBoss) {
                            emptyBossZones.add(z);
                        }
                    }
                }
                if (!emptyBossZones.isEmpty()) {
                    return emptyBossZones.get(Util.nextInt(0, emptyBossZones.size() - 1));
                }
            }
        }
        return null;
    }

    public static void dropGoldAndSPL(Boss boss, Player plKill) {
        if (boss == null || boss.zone == null || boss.zone.map == null) {
            return;
        }
        long plId = (plKill != null) ? plKill.id : -1;

        // 1. Rơi vàng: 3 - 5 cọc (mỗi cọc 30.000 - 80.000 vàng)
        int numPiles = Util.nextInt(3, 5);
        for (int i = 0; i < numPiles; i++) {
            int goldAmount = Util.nextInt(30_000, 80_000);
            int offsetX = (i % 2 == 0 ? 1 : -1) * ((i / 2 + 1) * 20);
            int dropX = Math.max(24, Math.min(boss.zone.map.mapWidth - 24, boss.location.x + offsetX));
            int dropY = boss.zone.map.yPhysicInTop(dropX, boss.location.y - 24);
            models.map.ItemMap itGold = new models.map.ItemMap(boss.zone, 190, goldAmount, dropX, dropY, plId);
            Service.gI().dropItemMap(boss.zone, itGold);
        }

        // 2. Rơi random Sao Pha Lê: 1 - 2 viên (Item 441 - 447)
        int[] splIds = {441, 442, 443, 444, 445, 446, 447};
        int numSpl = Util.nextInt(1, 2);
        for (int i = 0; i < numSpl; i++) {
            int splId = splIds[Util.nextInt(0, splIds.length - 1)];
            int offsetX = (i == 0 ? 0 : 25);
            int dropX = Math.max(24, Math.min(boss.zone.map.mapWidth - 24, boss.location.x + offsetX));
            int dropY = boss.zone.map.yPhysicInTop(dropX, boss.location.y - 24);
            Service.gI().dropItemMap(boss.zone, utils.ItemUtil.saoPhaLe(boss.zone, splId, 1, dropX, dropY, plId));
        }
    }
}

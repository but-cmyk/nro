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

import java.util.ArrayList;
import java.util.List;
import models.boss.AnTrom;
import models.boss.boss_list.Black.BlackGoku;
//import models.boss.boss_list.BossRongNhi.RongNhi1Sao;
//import models.boss.boss_list.BossRongNhi.RongNhi2Sao;
//import models.boss.boss_list.BossRongNhi.RongNhi3Sao;
//import models.boss.boss_list.BossRongNhi.RongNhi4Sao;
//import models.boss.boss_list.BossRongNhi.RongNhi5Sao;
//import models.boss.boss_list.BossRongNhi.RongNhi6Sao;
//import models.boss.boss_list.BossRongNhi.RongNhi7Sao;
//import models.boss.boss_list.ChristmasEvent.BrolySSJ3;
//import models.boss.boss_list.ChristmasEvent.CoolerVang;
//import models.boss.boss_list.ChristmasEvent.SuperXaydaGod;
import models.boss.boss_list.ChristmasEvent.TuanLoc;
import models.boss.boss_list.HalloweenEvent.Doi;
import models.boss.boss_list.HalloweenEvent.MaTroi;
//import models.boss.boss_list.Cooler.Frost;
//import models.boss.boss_list.HungVuongEvent.SonTinh;
//import models.boss.boss_list.HungVuongEvent.ThuyTinh;
import models.boss.boss_list.NewBoss.*;
import models.boss.boss_list.ThoDaiKa.ThoDaiKa;
import models.boss.boss_list.huydiet.Cumber;
//import models.boss.boss_list.huydiet.berus;
//import models.boss.boss_list.huydiet.champa;
//import models.boss.boss_list.huydiet.vados;
//import models.boss.boss_list.huydiet.whis;
import models.map.Zone;
import server.Maintenance;
import server.ServerManager;
import utils.Logger;
import utils.Util;

public class BossManager implements Runnable {

    private static BossManager instance;
    //public static byte ratioReward = 10;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
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

    public void loadBoss() {
        this.createBoss(BossID.KUKU, 10);
        this.createBoss(BossID.MAP_DAU_DINH, 10);
        this.createBoss(BossID.RAMBO, 10);
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
//        this.createBoss(BossID.BERUS);
//        this.createBoss(BossID.CHAMPA);
//        this.createBoss(BossID.VADOS);
//        this.createBoss(BossID.WHIS_1);

        this.createBoss(BossID.TIEU_DOI_TRUONG_NM);
        this.createBoss(BossID.BOJACK);
        this.createBoss(BossID.SUPER_BOJACK);
        //   this.createBoss(BossID.COLLER_GOLD);
//        this.createBoss(BossID.BE_NA, 3);
        this.createBoss(BossID.RONG_1_SAO);
        this.createBoss(BossID.RONG_2_SAO);
        this.createBoss(BossID.RONG_3_SAO);
        this.createBoss(BossID.RONG_4_SAO);
        this.createBoss(BossID.RONG_5_SAO);
        this.createBoss(BossID.RONG_6_SAO);
        this.createBoss(BossID.RONG_7_SAO);
//        this.createBoss(BossID.SON_TINH);
//        this.createBoss(BossID.THUY_TINH);
        this.createBoss(BossID.AN_TROM, 20);
        this.createBoss(BossID.THO_DAI_KA, 5);

//         this.createBoss(BossID.FROST);
        for (int i = 20; i != -1; i--) {//20: số lượng boss
            try {
                new ChoRach().zoneFinal = Util.randomAllMap();
                new Raiti().zoneFinal = Util.randomAllMap();
                new Xibachao().zoneFinal = Util.randomAllMap();
                new BaDo().zoneFinal = Util.randomAllMap();
            } catch (Exception e) {
            }
        }
    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            return switch (bossID) {
                case BossID.CUMBER ->
                    new Cumber();
                case BossID.AN_TROM ->
                    new AnTrom();
                case BossID.THO_DAI_KA ->
                    new ThoDaiKa();
                case BossID.TUAN_LOC ->
                    new TuanLoc();
                //     case BossID.COLLER_GOLD ->
//                    new CoolerVang();
                // case BossID.GOKUGOD ->
                //      new SuperXaydaGod();
                //  case BossID.BROLYSSJ ->
                //      new BrolySSJ3();
//                case BossID.CHILLER ->
//                    new Chiller();
                //   case BossID.FROST ->
                //      new Frost();
                //case BossID.VADOS ->
                //     new vados();
                //   case BossID.CHAMPA ->
                //       new champa();
                //   case BossID.WHIS_1 ->
                //      new whis();
                //   case BossID.BERUS ->
                //       new berus();
                case BossID.BLACK_GOKU ->
                    new BlackGoku();
                case BossID.TAP_SU_0 ->
                    new TAPSU0();
                case BossID.TAP_SU_1 ->
                    new TAPSU1();
                case BossID.TAP_SU_2 ->
                    new TAPSU2();
                case BossID.TAP_SU_3 ->
                    new TAPSU3();
                case BossID.TAP_SU_4 ->
                    new TAPSU4();
                case BossID.TAN_BINH_5 ->
                    new TANBINH5();
                case BossID.TAN_BINH_0 ->
                    new TANBINH0();
                case BossID.TAN_BINH_1 ->
                    new TANBINH1();
                case BossID.TAN_BINH_2 ->
                    new TANBINH2();
                case BossID.TAN_BINH_3 ->
                    new TANBINH3();
                case BossID.TAN_BINH_4 ->
                    new TANBINH4();
                case BossID.CHIEN_BINH_5 ->
                    new CHIENBINH5();
                case BossID.CHIEN_BINH_0 ->
                    new CHIENBINH0();
                case BossID.CHIEN_BINH_1 ->
                    new CHIENBINH1();
                case BossID.CHIEN_BINH_2 ->
                    new CHIENBINH2();
                case BossID.CHIEN_BINH_3 ->
                    new CHIENBINH3();
                case BossID.CHIEN_BINH_4 ->
                    new CHIENBINH4();
                case BossID.DOI_TRUONG_5 ->
                    new DOITRUONG5();
                case BossID.SO_4 ->
                    new SO4();
                case BossID.SO_3 ->
                    new SO3();
                case BossID.SO_2 ->
                    new SO2();
                case BossID.SO_1 ->
                    new SO1();
                case BossID.TIEU_DOI_TRUONG ->
                    new TDT();
                case BossID.SO_4_NM ->
                    new SO4_NM();
                case BossID.SO_3_NM ->
                    new SO3_NM();
                case BossID.SO_2_NM ->
                    new SO2_NM();
                case BossID.SO_1_NM ->
                    new SO1_NM();
                case BossID.TIEU_DOI_TRUONG_NM ->
                    new TDT_NM();
                case BossID.BUJIN ->
                    new BUJIN();
                case BossID.KOGU ->
                    new KOGU();
                case BossID.ZANGYA ->
                    new ZANGYA();
                case BossID.BIDO ->
                    new BIDO();
                case BossID.BOJACK ->
                    new BOJACK();
                case BossID.SUPER_BOJACK ->
                    new SUPER_BOJACK();
                case BossID.KUKU ->
                    new Kuku();
                case BossID.MAP_DAU_DINH ->
                    new MapDauDinh();
                case BossID.RAMBO ->
                    new Rambo();
                case BossID.TAU_PAY_PAY_DONG_NAM_KARIN ->
                    new TaoPaiPai();
                case BossID.DRABURA ->
                    new Drabura();
                case BossID.BUI_BUI ->
                    new BuiBui();
                case BossID.BUI_BUI_2 ->
                    new BuiBui2();
                case BossID.YA_CON ->
                    new Yacon();
                case BossID.DRABURA_2 ->
                    new Drabura2();
                case BossID.GOKU ->
                    new Goku();
                case BossID.CADIC ->
                    new Cadic();
                case BossID.MABU_12H ->
                    new Mabu();
                case BossID.DRABURA_3 ->
                    new Drabura3();
                case BossID.MABU ->
                    new Mabu2H();
                case BossID.SUPERBU ->
                    new SuperBu();
                case BossID.FIDE ->
                    new Fide();
                case BossID.DR_KORE ->
                    new DrKore();
                case BossID.ANDROID_19 ->
                    new Android19();
                case BossID.ANDROID_13 ->
                    new Android13();
                case BossID.ANDROID_14 ->
                    new Android14();
                case BossID.ANDROID_15 ->
                    new Android15();
                case BossID.PIC ->
                    new Pic();
                case BossID.POC ->
                    new Poc();
                case BossID.KING_KONG ->
                    new KingKong();
                case BossID.XEN_BO_HUNG ->
                    new XenBoHung();
                case BossID.SIEU_BO_HUNG ->
                    new SieuBoHung();
                case BossID.XEN_CON_1 ->
                    new XENCON1();
                case BossID.XEN_CON_2 ->
                    new XENCON2();
                case BossID.XEN_CON_3 ->
                    new XENCON3();
                case BossID.XEN_CON_4 ->
                    new XENCON4();
                case BossID.XEN_CON_5 ->
                    new XENCON5();
                case BossID.XEN_CON_6 ->
                    new XENCON6();
                case BossID.XEN_CON_7 ->
                    new XENCON7();
                case BossID.COOLER ->
                    new Cooler();
                case BossID.BROLY ->
                    new Broly();
                case BossID.GOLDEN_FRIEZA ->
                    new GoldenFrieza();
                case BossID.DEATH_BEAM_1 ->
                        new DeathBeam(BossID.DEATH_BEAM_1, 14600); // 14.6 giây

                case BossID.DEATH_BEAM_2 ->
                        new DeathBeam(BossID.DEATH_BEAM_2, 14700); // 14.7 giây

                case BossID.DEATH_BEAM_3 ->
                        new DeathBeam(BossID.DEATH_BEAM_3, 14800); // 14.8 giây

                case BossID.DEATH_BEAM_4 ->
                        new DeathBeam(BossID.DEATH_BEAM_4, 14900); // 14.9 giây

                case BossID.DEATH_BEAM_5 ->
                        new DeathBeam(BossID.DEATH_BEAM_5, 15000); // 15.0 giây
                // boss sk
//                case BossID.KHIDOT ->
//                        new KhiDot();
//                case BossID.NGUYETTHAN ->
//                        new NguyetThan();
//                case BossID.NHATTHAN ->
//                        new NhatThan();
                case BossID.BIMA ->
                    new BiMa();
                case BossID.MATROI ->
                    new MaTroi();
                case BossID.DOI ->
                    new Doi();
//                case BossID.ONG_GIA_NOEL ->
//                    new OngGiaNoel();
//                case BossID.SON_TINH ->
//                    new SonTinh();
//                case BossID.THUY_TINH ->
//                    new ThuyTinh();
//                case BossID.LAN_CON ->
//                    new LanCon();
//                case BossID.RONG_1_SAO ->
//                    new RongNhi1Sao();
//                case BossID.RONG_2_SAO ->
//                    new RongNhi2Sao();
//                case BossID.RONG_3_SAO ->
//                    new RongNhi3Sao();
//                case BossID.RONG_4_SAO ->
//                    new RongNhi4Sao();
//                case BossID.RONG_5_SAO ->
//                    new RongNhi5Sao();
//                case BossID.RONG_6_SAO ->
//                    new RongNhi6Sao();
//                case BossID.RONG_7_SAO ->
//                    new RongNhi7Sao();
                // case BossID.SON_TINH ->
                //    new SonTinh();
                // case BossID.THUY_TINH ->
                //  new ThuyTinh();
                default ->
                    null;
            };
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }

    public Boss getBoss(int id) {
        try {
            Boss boss = this.bosses.get(id);
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void showListBoss(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(3);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss");
            msg.writer().writeByte((int) bosses.stream().filter(boss -> !MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])).count());
            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = this.bosses.get(i);
                if (MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])) {
                    continue;
                }
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF(boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId + "");
                } else {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF("=))");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
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

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                if (ServerManager.isReloading) {
                    continue; // Nếu server đang reload, bỏ qua lượt update này và kiểm tra lại ở vòng lặp sau.
                }
                long st = System.currentTimeMillis();
                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    try {
                        this.bosses.get(i).update();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (150 - (System.currentTimeMillis() - st) > 0) {
                    Thread.sleep(150 - (System.currentTimeMillis() - st));
                }
            } catch (Exception e) {
                e.printStackTrace();
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

}

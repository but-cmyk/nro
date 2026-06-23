package services.phoban;


import models.boss.Boss;
import consts.BossID;
import models.boss.boss_list.Training.Karin;
import models.boss.boss_list.Training.KhiBubbles;
import models.boss.boss_list.Training.MrPoPo;
import models.boss.boss_list.Training.ThanVuTru;
import models.boss.boss_list.Training.ThuongDe;
import models.boss.boss_list.Training.ToSuKaio;
import models.boss.boss_list.Training.Whis;
import models.boss.boss_list.Training.Yajiro;
import consts.ConstNpc;
import models.boss.boss_list.Training.TauPayPay1;
import models.map.Zone;
import models.player.Player;
import services.map.MapService;
import services.map.NpcService;
import services.Service;
import services.map.ChangeMapService;
import utils.Logger;
import utils.Util;

public class TrainingService {

    private static TrainingService instance;

    public static TrainingService gI() {
        if (instance == null) {
            instance = new TrainingService();
        }
        return instance;
    }

    public Player getNonInteractiveNPC(Zone zone, int id) {
        for (Player pl : zone.getNonInteractiveNPCs()) {
            if (pl != null && pl.id == id) {
                return pl;
            }
        }
        return null;
    }

    public int getNpc(int BossId) {
        switch (BossId) {
            case BossID.KARIN -> {
                return ConstNpc.THAN_MEO_KARIN;
            }
            case BossID.THUONG_DE -> {
                return ConstNpc.THUONG_DE;
            }
            case BossID.THAN_VU_TRU -> {
                return ConstNpc.THAN_VU_TRU;
            }
            case BossID.TO_SU_KAIO -> {
                return ConstNpc.TO_SU_KAIO;
            }
            case BossID.WHIS -> {
                return ConstNpc.WHIS;
            }
        }
        return -1;
    }

    public void luyenTapEnd(Player pl, int bossID) {
        if (getNpc(bossID) != -1) {
            Service.gI().sendHideNpc(pl, getNpc(bossID), false);
        }
    }

    public Boss callBoss(Player pl, int bossID, boolean isThachDau) {
        try {
            pl.isThachDau = isThachDau;
            if (getNpc(bossID) != -1) {
                Service.gI().sendHideNpc(pl, getNpc(bossID), true);
            }
            switch (bossID) {
                case BossID.TAUPAYPAY -> {
                    return new TauPayPay1(pl);
                }
                case BossID.KARIN -> {
                    return new Karin(pl);
                }
                case BossID.YAJIRO -> {
                    return new Yajiro(pl);
                }
                case BossID.MRPOPO -> {
                    return new MrPoPo(pl);
                }
                case BossID.THUONG_DE -> {
                    ChangeMapService.gI().changeMap(pl, MapService.gI().getMapCanJoin(pl, 49, 0), 362, 408);
                    return new ThuongDe(pl);
                }
                case BossID.KHI_BUBBLES -> {
                    return new KhiBubbles(pl);
                }
                case BossID.THAN_VU_TRU -> {
                    return new ThanVuTru(pl);
                }
                case BossID.TO_SU_KAIO -> {
                    return new ToSuKaio(pl);
                }
                case BossID.WHIS -> {
                    return new Whis(pl);
                }
            }
        } catch (Exception e) {
            Logger.logException(TrainingService.class, e);
        }
        return null;
    }

  public int getTnsmMoiPhut(Player player) {
    return switch (player.levelLuyenTap) {
        case 0 -> 10000;
        case 1 -> 30000;
        case 2 -> 60000;
        case 3 -> 100000;
        case 4 -> 120000;
        case 5 -> 138888; // đủ 200 triệu/ngày
        default -> (int) Math.max(player.tnsmLuyenTap, 138888);
    };
}


 public void tangTnsmLuyenTap(Player player, long tnsm) {
    if (player.isPl()) {
        // Cộng tnsmLuyenTap mỗi lần train quái
        player.tnsmLuyenTap += Math.max(100, tnsm / (100 * (Service.gI().getCurrLevel(player) + 1)));

        // Tự động tăng level luyện tập khi đủ mốc
        while (player.levelLuyenTap < 5) {
            long required = switch (player.levelLuyenTap) {
                case 0 -> 1_000_000;   // yêu cầu 1 triệu để lên level 1
                case 1 -> 3_000_000;   // 3 triệu để lên level 2
                case 2 -> 5_000_000;   // 6 triệu để lên level 3
                case 3 -> 10_000_000;  // 12 triệu để lên level 4
                case 4 -> 20_000_000;  // 20 triệu để lên level 5
                default -> Long.MAX_VALUE;
            };
            if (player.tnsmLuyenTap >= required) {
                player.tnsmLuyenTap -= required;
                player.levelLuyenTap++;
              //  Service.gI().sendThongBao(player, "Bạn đã tăng cấp luyện tập lên cấp " + player.levelLuyenTap + "!");
            } else {
                break;
            }
        }

        // Giới hạn tránh bug số lớn
        if (player.tnsmLuyenTap > 50_000_000) {
            player.tnsmLuyenTap = 50_000_000;
        }
    }
}




    public void tnsmLuyenTapUp(Player player) {
        long tnsm;
        int time = (int) ((System.currentTimeMillis() - player.lastTimeOffline) / 1000);
        if (time > 60) {
            tnsm = ((long) getTnsmMoiPhut(player) * (long) ((time > 86400 ? 86400 : time)) / 60);
            if (MapService.gI().isMapLuyenTap(player.zone.map.mapId)) {
                NpcService.gI().createTutorial(player, -1, "Bạn tăng được " + Util.powerToString(tnsm) + " sức mạnh trong thời gian " + (time / 60) + " phút tập luyện Offline");
                Service.gI().addSMTN(player, (byte) 2, tnsm, false);
            } else if (player.dangKyTapTuDong && time > 1800) {
                if (player.inventory.getGemAndRuby() > 1) {
                    player.inventory.subGemAndRuby(1);
                    final Player p = player;
                    final long finalTnsm = tnsm;
                    final int finalTime = time;
                    server.GameLoopManager.gI().schedule(() -> {
                        try {
                            if (p != null && !p.beforeDispose && !p.isOffline && p.zone != null) {
                                p.lastMapOffline = p.zone.map.mapId;
                                p.lastZoneOffline = p.zone.zoneId;
                                p.lastXOffline = p.location.x;
                                Service.gI().addSMTN(p, (byte) 2, finalTnsm, false);
                                p.teleTapTuDong = true;
                                p.thongBaoTapTuDong = "Bạn tăng được " + Util.powerToString(finalTnsm) + " sức mạnh trong thời gian " + (finalTime / 60) + " phút tập luyện Offline, -1 ngọc (phí đăng ký tập tự động)";
                                ChangeMapService.gI().changeMapBySpaceShip(p, p.mapIdDangTapTuDong, 0, Util.nextInt(200, 400));
                                Service.gI().sendMoney(p);
                            }
                        } catch (Exception e) {
                        }
                    }, 1000);
                } else {
                    player.dangKyTapTuDong = false;
                    Service.gI().sendThongBao(player, "Bạn không đủ ngọc, đăng ký luyện tập tự động đã bị hủy");
                }
            }
        }
        
}}

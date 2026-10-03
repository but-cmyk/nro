package models.npc.npc_list;

import database.daos.PlayerDAO;
import consts.ConstNpc;
import java.util.ArrayList;
import models.item.Item;
import models.tournament.DeathOrAliveArena;
import managers.tournament.DeathOrAliveArenaManager;
import models.combine.list.ChuyenHoaBangVang;
import models.item.Item.ItemOption;
import services.tournament.DeathOrAliveArenaService;
import models.npc.Npc;
import models.npc.NpcFactory;
import models.player.Player;
import network.io.Message;
import server.Manager;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import services.map.NpcService;
import services.tournament.DeathOrAliveArenaWeeklyService;
import services.map.ChangeMapService;
import services.CombineService;
import services.RewardService;
import services.ShopService;
import utils.Logger;
import utils.Util;

public class BaHatMit extends Npc {

    public BaHatMit(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 5 -> {
                    if (Manager.SU_KIEN == 1) {
                        Item Nguqua = InventoryService.gI().findItemBagByTemp(player, 1390);
                        if (Nguqua != null && Nguqua.quantity >= 1) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Ngươi tìm ta có việc gì?",
                                    "Chức năng\nPha lê", "Võ đài\nSinh Tử", "Nâng Đồ\nKích Hoạt", "Chức năng\nChuyển hoá");
                        } else {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Ngươi tìm ta có việc gì?",
                                    "Chức năng\nPha lê", "Võ đài\nSinh Tử", "Nâng Đồ\nKích Hoạt", "Chức năng\nChuyển hoá");
                        }
                    } else {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ngươi tìm ta có việc gì?",
                                "Chức năng\nPha lê", "Võ đài\nSinh Tử", "Nâng Đồ\nKích Hoạt", "Chức năng\nChuyển hoá");

                    }
                }
                case 112 -> {
                    if (Util.isAfterMidnight(player.lastTimePKVoDaiSinhTu)) {
                        player.thoiVangVoDaiSinhTu = 0;
                    }
                    if (player.haveRewardVDST) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chúc mừng con đã xuất sắc vô địch Võ Đài Sinh Tử!\nTa có phần thưởng giá trị gồm Vàng, Tiềm năng, Vệ tinh, Đá nâng cấp và cơ hội nhận Cải trang Boss Võ Đài quý hiếm!",
                                "Nhận thưởng", "Từ chối");
                        return;
                    }
                    DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                    if (vdst != null) {
                        if (vdst.getPlayer().equals(player)) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Ngươi muốn hủy đăng ký thi đấu võ đài?",
                                    "Hủy Đăng Ký",
                                    "Nhận 3 bình\nmiễn phí",
                                    "Bảng Xếp\nHạng",
                                    "Thưởng Top\nTuần",
                                    "Hướng dẫn",
                                    "Về\nđảo rùa");
                        } else {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                                    "Bình chọn",
                                    "Vào sàn khác",
                                    "Nhận 3 bình\nmiễn phí",
                                    "Bảng Xếp\nHạng",
                                    "Thưởng Top\nTuần",
                                    "Hướng dẫn",
                                    "Về\nđảo rùa");
                        }
                    } else {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                                "Đăng ký\n" + (player.thoiVangVoDaiSinhTu + 1) + " bình nước",
                                "Nhận 3 bình\nmiễn phí",
                                "Bảng Xếp\nHạng",
                                "Thưởng Top\nTuần",
                                "Hướng dẫn",
                                "Về\nđảo rùa");
                    }
                    break;
                }
                case 174 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi tìm ta có việc gì?",
                            "Quay về", "Từ chối");
                case 181 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi tìm ta có việc gì?",
                            "Quay về", "Từ chối");
                default -> {
                    Item nguoituyet = InventoryService.gI().findItemBag(player, 1210);
                    Item nguoituyetbanggia = InventoryService.gI().findItemBag(player, 1211);
                    String nangcapbt = InventoryService.gI().findItemBongTaiCap2(player) ? "Mở chỉ số\nBông tai\nPorata cấp\n2" : "Nâng cấp\nBông tai\nPorata";

                    if (nguoituyet != null && nguoituyetbanggia != null) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm", nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng", "Giao\nNgười Tuyết", "Giao\nNgười Tuyết\nBăng Giá");
                    } else if (nguoituyet != null) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm", nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng", "Giao\nNgười Tuyết");
                    } else if (nguoituyetbanggia != null) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm", nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng", "Giao\nNgười Tuyết\nBăng Giá");
                    } else {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm", nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng");
                    }
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 5 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                createOtherMenu(player, ConstNpc.CHUC_NANG_SAO_PHA_LE,
                                        "Chọn Chức Năng Ngươi Muốn Thực Hiện",
                                        "Ép Sao\n Trang Bị",
                                        "Pha lê\nhóa\ntrang bị");
                            case 1 ->
                                ChangeMapService.gI().changeMapNonSpaceship(player, 112, 200 + Util.nextInt(-100, 100), 408);
                            case 2 ->
                                createOtherMenu(player, ConstNpc.CHUC_NANG_SKH,
                                        "Chọn Chức Năng Ngươi Muốn Thực Hiện",
                                        "Nâng SKH\nThường",
                                        "Nâng SKH\nVIP");
                            case 3 ->
                                createOtherMenu(player, ConstNpc.CHUC_NANG_CHUYEN_HOA,
                                        "Chọn Chức Năng Ngươi Muốn Thực Hiện",
                                        "Chuyển hóa\nbằng vàng",
                                        "Chuyển hóa\nbằng ngọc");
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.CHUC_NANG_CHUYEN_HOA) {
                        switch (select) {
                            case 0 ->
                                CombineService.gI().openTabCombine(player, CombineService.CHUYEN_HOA_BANG_VANG);
                            case 1 ->
                                CombineService.gI().openTabCombine(player, CombineService.CHUYEN_HOA_BANG_NGOC);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.CHUC_NANG_SKH) {
                        switch (select) {
                            case 0 ->
                                CombineService.gI().openTabCombine(player, CombineService.NANG_HUY_DIET_LEN_SKH);
                            case 1 ->
                                CombineService.gI().openTabCombine(player, CombineService.DOI_SKH_HD);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.CHUC_NANG_SAO_PHA_LE) {
                        switch (select) {
                            case 0 ->
                                CombineService.gI().openTabCombine(player, CombineService.EP_SAO_TRANG_BI);
                            case 1 ->
                                CombineService.gI().openTabCombine(player, CombineService.PHA_LE_HOA_TRANG_BI);
                        }
                   } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                        switch (player.combineNew.typeCombine) {
                            // --- TÁCH PHA LÊ HÓA RA XỬ LÝ RIÊNG ---
                            case CombineService.PHA_LE_HOA_TRANG_BI -> {
                                switch (select) {
                                    case 0 -> CombineService.gI().startCombine(player, 10); // Nút đầu (x10)
                                    case 1 -> CombineService.gI().startCombine(player, 1);  // Nút sau (x1)
                                }
                            }

                            // --- CÁC CHỨC NĂNG CÒN LẠI GIỮ NGUYÊN (Đã xóa PHA_LE_HOA_TRANG_BI ở list dưới) ---
                            case CombineService.EP_SAO_TRANG_BI, CombineService.NANG_CAP_SAO_PHA_LE, CombineService.DANH_BONG_SAO_PHA_LE, CombineService.CUONG_HOA_LO_SAO, CombineService.TAO_DA_HEMATITE, CombineService.DOI_DO_THAN, CombineService.DOI_SKH_HD, CombineService.NANG_HUY_DIET_LEN_SKH, CombineService.CHUYEN_HOA_BANG_VANG, CombineService.CHUYEN_HOA_BANG_NGOC -> {
                                switch (select) {
                                    case 0 ->
                                        CombineService.gI().startCombine(player, 100);
                                    case 1 ->
                                        CombineService.gI().startCombine(player, 10);
                                    case 2 ->
                                        CombineService.gI().startCombine(player);
                                    case 9 ->
                                        CombineService.gI().startCombine(player);
                                    case 10 ->
                                        CombineService.gI().startCombine(player);
                                }
                            }

                            // --- CÁC CHỨC NĂNG KHÁC (Nâng cấp vật phẩm, bông tai...) ---
                            case CombineService.NANG_CAP_VAT_PHAM, CombineService.NANG_CAP_BONG_TAI, CombineService.NANG_CHI_SO_BONG_TAI, CombineService.LAM_PHEP_NHAP_DA, CombineService.NHAP_NGOC_RONG, CombineService.GIAM_DINH_SACH, CombineService.TAY_SACH, CombineService.NANG_CAP_SACH_TUYET_KY, CombineService.PHUC_HOI_SACH, CombineService.PHAN_RA_SACH -> {
                                switch (select) {
                                    case 0 ->
                                        CombineService.gI().startCombine(player);
                                    case 1 ->
                                        CombineService.gI().startCombineVip(player, 10);
                                    case 2 ->
                                        CombineService.gI().startCombineVip(player, 100);
                                }
                            }
                        }
                    }
                }
                case 112 -> {
                    if (Util.isAfterMidnight(player.lastTimePKVoDaiSinhTu)) {
                        player.thoiVangVoDaiSinhTu = 0;
                    }

                    if (player.idMark.isBaseMenu()) {
                        if (player.haveRewardVDST) {
                            switch (select) {
                                case 0 -> { // Nhận phần thưởng
                                    if (InventoryService.gI().getCountEmptyBag(player) < 3) {
                                        Service.gI().sendThongBao(player, "Hành trang cần tối thiểu 3 ô trống để nhận toàn bộ phần thưởng!");
                                        return;
                                    }

                                    StringBuilder thongBao = new StringBuilder("Bạn nhận được:\n");

                                    // 1. Vàng (300k - 500k: Kiểm soát lạm phát, hoàn vốn tiền mua bình nước phép + có lời)
                                    long goldReward = Util.nextInt(300_000, 500_000);
                                    player.inventory.gold = Math.min(player.inventory.gold + goldReward, 2_000_000_000L);
                                    Service.gI().sendMoney(player);
                                    thongBao.append("- ").append(Util.format(goldReward)).append(" vàng\n");

                                    // 2. Tiềm năng / Sức mạnh: Phương Án 1 - Phân đoạn theo từng cột mốc sức mạnh (Tier Pacing)
                                    long power = player.nPoint.power;
                                    long tnsm = 0;
                                    if (power < 1_500_000L) {
                                        // Dưới 1.5 triệu SM (Ví dụ 550k): Thưởng 2% - 3% sức mạnh (550k nhận ~11k - 16k)
                                        tnsm = (long) (power * Util.nextInt(20, 30) / 1000.0);
                                        if (tnsm < 5_000L) {
                                            tnsm = 5_000L;
                                        }
                                    } else if (power < 50_000_000L) {
                                        // 1.5 triệu - 50 triệu SM: Thưởng 0.3% - 0.5% sức mạnh (10tr nhận 30k - 50k, 40tr nhận 120k - 200k)
                                        tnsm = (long) (power * Util.nextInt(3, 5) / 1000.0);
                                    } else if (power < 1_500_000_000L) {
                                        // 50 triệu - 1.5 tỷ SM: Thưởng 0.05% sức mạnh (500tr nhận 250k, 1 tỷ nhận 500k)
                                        tnsm = power * 5 / 10000;
                                    } else if (power < 40_000_000_000L) {
                                        // 1.5 tỷ - 40 tỷ SM: Thưởng 300.000 - 500.000 TNSM cố định
                                        tnsm = Util.nextInt(300_000, 500_000);
                                    } else if (power < 90_000_000_000L) {
                                        // 40 tỷ - 80 tỷ SM: Thưởng 50.000 - 100.000 TNSM cố định (tương đương 20 - 50 hit đấm quái)
                                        tnsm = Util.nextInt(50_000, 100_000);
                                    } else {
                                        // Trên 90 tỷ SM (chạm max cap): Tượng trưng 1.000 TNSM
                                        tnsm = 1_000L;
                                    }

                                    if (tnsm > 0) {
                                        Service.gI().addSMTN(player, (byte) 2, tnsm, false);
                                        thongBao.append("- ").append(Util.format(tnsm)).append(" tiềm năng\n");
                                    }

                                    // 3. Vệ tinh ngẫu nhiên (342-345)
                                    short veTinhId = (short) Util.nextInt(342, 345);
                                    Item veTinh = ItemService.gI().createNewItem(veTinhId);
                                    if (veTinh.itemOptions == null) {
                                        veTinh.itemOptions = new ArrayList<>();
                                    }
                                    switch (veTinhId) {
                                        case 342 -> veTinh.itemOptions.add(new Item.ItemOption(81, 5));
                                        case 343 -> veTinh.itemOptions.add(new Item.ItemOption(83, 20));
                                        case 344 -> veTinh.itemOptions.add(new Item.ItemOption(82, 1));
                                        case 345 -> veTinh.itemOptions.add(new Item.ItemOption(80, 5));
                                    }
                                    InventoryService.gI().addItemBag(player, veTinh);
                                    thongBao.append("- ").append(veTinh.template.name).append("\n");

                                    // 4. Đá nâng cấp trang bị (220-224: Ruby, Lam Ngọc, Ngọc Bích, Thạch Anh Tím, Titan)
                                    short daId = (short) Util.nextInt(220, 224);
                                    int slDa = Util.nextInt(1, 3);
                                    Item daNangCap = ItemService.gI().createNewItem(daId, slDa);
                                    InventoryService.gI().addItemBag(player, daNangCap);
                                    thongBao.append("- x").append(slDa).append(" ").append(daNangCap.template.name).append("\n");

                                    // 5. Gacha may mắn: Bùa 1h (tỷ lệ 30%)
                                    if (Util.isTrue(30, 100)) {
                                        short buaId = (short) Util.nextInt(213, 218);
                                        Item bua = ItemService.gI().createNewItem(buaId);
                                        if (bua.itemOptions == null) {
                                            bua.itemOptions = new ArrayList<>();
                                        }
                                        bua.itemOptions.add(new Item.ItemOption(66, 0)); // Bùa 1h
                                        InventoryService.gI().addItemBag(player, bua);
                                        thongBao.append("- [May mắn] 1 ").append(bua.template.name).append("\n");
                                    }

                                    // 6. Gacha may mắn: Thỏi vàng (tỷ lệ 3%)
                                    if (Util.isTrue(3, 100)) {
                                        Item thoiVang = ItemService.gI().createNewItem((short) 457);
                                        InventoryService.gI().addItemBag(player, thoiVang);
                                        thongBao.append("- [May mắn] 1 Thỏi vàng\n");
                                    }

                                    // 7. Gacha may mắn: Cải trang Boss Võ Đài Sinh Tử (Chỉ số chuẩn 100% theo Shop Santa)
                                    // 448: Dracula (opt 104, param 50)
                                    // 449: Người Vô Hình (opt 105, param 0)
                                    // 450: Bông Băng (opt 106, param 0)
                                    // 451: Vua Quỷ Sa Tăng (opt 100, param 20)
                                    // 452: Thỏ Đầu Bạc (opt 101, param 20)
                                    if (Util.isTrue(15, 100)) {
                                        short[] ctIds = {448, 449, 450, 451, 452};
                                        short ctId = ctIds[Util.nextInt(0, ctIds.length - 1)];
                                        Item caiTrang = ItemService.gI().createNewItem(ctId);
                                        if (caiTrang.itemOptions == null) {
                                            caiTrang.itemOptions = new ArrayList<>();
                                        }

                                        // Áp dụng đúng chỉ số chuẩn từ Shop Santa
                                        switch (ctId) {
                                            case 448 -> caiTrang.itemOptions.add(new Item.ItemOption(104, 50));
                                            case 449 -> caiTrang.itemOptions.add(new Item.ItemOption(105, 0));
                                            case 450 -> caiTrang.itemOptions.add(new Item.ItemOption(106, 0));
                                            case 451 -> caiTrang.itemOptions.add(new Item.ItemOption(100, 20));
                                            case 452 -> caiTrang.itemOptions.add(new Item.ItemOption(101, 20));
                                        }

                                        // Tỷ lệ Vĩnh Viễn: ~1% tổng thể (7% trong 15% trúng)
                                        if (Util.isTrue(7, 100)) {
                                            InventoryService.gI().addItemBag(player, caiTrang);
                                            thongBao.append("- [SIÊU PHẨM] ").append(caiTrang.template.name).append(" (VĨNH VIỄN)!\n");
                                            Service.gI().sendThongBaoAllPlayer(player.name + " vừa xuất sắc vô địch Võ Đài Sinh Tử và nhận được " + caiTrang.template.name + " VĨNH VIỄN!");
                                        } else {
                                            int days = Util.nextInt(3, 7);
                                            caiTrang.itemOptions.add(new Item.ItemOption(93, days));
                                            InventoryService.gI().addItemBag(player, caiTrang);
                                            thongBao.append("- [Đặc biệt] ").append(caiTrang.template.name).append(" (HSD: ").append(days).append(" ngày)\n");
                                        }
                                    }

                                    InventoryService.gI().sendItemBags(player);
                                    Service.gI().sendThongBao(player, thongBao.toString());

                                    player.haveRewardVDST = false;
                                    PlayerDAO.updatePlayer(player);
                                    return;
                                }
                                case 1 -> {
                                    // Từ chối nhận phần thưởng
                                    return;
                                }
                            }
                            return; // Thoát sau khi xử lý reward
                        }

                        // Xử lý đăng ký hoặc hủy tham gia VDST
                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                        if (vdst != null) {
                            if (vdst.getPlayer().equals(player)) { // Người chơi là chủ phòng
                                switch (select) {
                                    case 0: // Hủy đăng ký
                                        DeathOrAliveArenaService.gI().cancelChallenge(player);
                                        break;
                                    case 1: // Nhận 3 bình miễn phí
                                        nhanBinhNuocFree(player);
                                        break;
                                    case 2: // Bảng Xếp Hạng
                                        Service.gI().showListTop(player, Manager.topArena);
                                        break;
                                    case 3: // Thưởng Top Tuần
                                        DeathOrAliveArenaWeeklyService.gI().claimReward(player);
                                        break;
                                    case 4: // Hướng dẫn
                                        showHuongDanVDST(player);
                                        break;
                                    case 5: // Về đảo rùa
                                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                                        break;
                                }
                            } else { // Người chơi là khán giả
                                switch (select) {
                                    case 0: // Bình chọn
                                        this.createOtherMenu(player, ConstNpc.DAT_CUOC_HAT_MIT,
                                                "Phí bình chọn là 1 triệu vàng...",
                                                "Bình chọn cho " + vdst.getPlayer().name,
                                                "Bình chọn cho hạt mít");
                                        break;
                                    case 1: // Thách đấu
                                        DeathOrAliveArenaService.gI().startChallenge(player);
                                        break;
                                    case 2: // Nhận 3 bình miễn phí
                                        nhanBinhNuocFree(player);
                                        break;
                                    case 3: // Bảng Xếp Hạng
                                        Service.gI().showListTop(player, Manager.topArena);
                                        break;
                                    case 4: // Thưởng Top Tuần
                                        DeathOrAliveArenaWeeklyService.gI().claimReward(player);
                                        break;
                                    case 5: // Hướng dẫn
                                        showHuongDanVDST(player);
                                        break;
                                    case 6: // Về đảo rùa
                                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                                        break;
                                }
                            }
                        } else { // Chưa có ai đăng ký
                            switch (select) {
                                case 0: // Đăng ký
                                    DeathOrAliveArenaService.gI().startChallenge(player);
                                    break;
                                case 1: // Nhận 3 bình miễn phí
                                    nhanBinhNuocFree(player);
                                    break;
                                case 2: // Bảng Xếp Hạng
                                    Service.gI().showListTop(player, Manager.topArena);
                                    break;
                                case 3: // Thưởng Top Tuần
                                    DeathOrAliveArenaWeeklyService.gI().claimReward(player);
                                    break;
                                case 4: // Hướng dẫn
                                    showHuongDanVDST(player);
                                    break;
                                case 5: // Về đảo rùa
                                    ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                                    break;
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.DAT_CUOC_HAT_MIT) {
                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                        if (vdst != null) {
                            switch (select) {
                                case 0 -> { // bình chọn cho người chơi
                                    if (player.inventory.gold >= 1_000_000) {
                                        vdst.setCuocPlayer(vdst.getCuocPlayer() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonPlayer++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                        Service.gI().sendThongBao(player, "Bạn đã bình chọn thành công cho " + vdst.getPlayer().name);
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.powerToString(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                                case 1 -> { // bình chọn cho hạt mít
                                    if (player.inventory.gold >= 1_000_000) {
                                        vdst.setCuocBaHatMit(vdst.getCuocBaHatMit() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonHatMit++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                        Service.gI().sendThongBao(player, "Bạn đã bình chọn thành công cho Bà Hạt Mít.");
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.powerToString(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                            }
                        }
                    }
                }
                case 174 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    }
                }
                case 181 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    }
                }
                case 42, 43, 44, 84 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                createOtherMenu(player, ConstNpc.SACH_TUYET_KY, "Ta có thể giúp gì cho ngươi ?",
                                        "Đóng thành\nSách cũ",
                                        "Đổi Sách\nTuyệt kỹ",
                                        "Giám định\nSách",
                                        "Tẩy\nSách",
                                        "Nâng cấp\nSách\nTuyệt kỹ",
                                        "Hồi phục\nSách",
                                        "Phân rã\nSách");
                            case 1 -> // shop bùa
                                createOtherMenu(player, ConstNpc.MENU_OPTION_SHOP_BUA,
                                        "Bùa của ta rất lợi hại, nhìn ngươi yếu đuối thế này, chắc muốn mua bùa để "
                                        + "mạnh mẽ à, mua không ta bán cho, xài rồi lại thích cho mà xem.",
                                        "Bùa\n1 giờ", "Bùa\n8 giờ", "Bùa\n1 tháng", "Đóng");
                            case 2 ->
                                CombineService.gI().openTabCombine(player,
                                        CombineService.NANG_CAP_VAT_PHAM);
                            case 3 -> {
                                if (InventoryService.gI().findItemBongTaiCap2(player)) {
                                    CombineService.gI().openTabCombine(player, CombineService.NANG_CHI_SO_BONG_TAI);
                                } else {
                                    CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_BONG_TAI);
                                }
                            }
                            case 4 -> // làm phép nhập đá
                                CombineService.gI().openTabCombine(player, CombineService.LAM_PHEP_NHAP_DA);
                            case 5 -> // nhập ngọc rồng
                                CombineService.gI().openTabCombine(player, CombineService.NHAP_NGOC_RONG);
                            case 6 -> {
                                if (InventoryService.gI().findItemNguoituyet(player)) {
                                    Item item = InventoryService.gI().findItemBag(player, 1210);
                                    if (item != null && item.quantity >= 1) {
                                        RewardService.gI().rewardNguoiTuyet(player);
                                        InventoryService.gI().subQuantityItemsBag(player, item, 1);
                                    }
                                } else if (InventoryService.gI().findItemNguoituyetbanggia(player)) {
                                    Item item = InventoryService.gI().findItemBag(player, 1211);
                                    if (item != null && item.quantity >= 1) {
                                        RewardService.gI().rewardNguoiTuyetBangGia(player);
                                        InventoryService.gI().subQuantityItemsBag(player, item, 1);
                                    }
                                }
                            }
                            case 7 -> {
                                if (InventoryService.gI().findItemNguoituyetbanggia(player)) {
                                    Item item = InventoryService.gI().findItemBag(player, 1211);
                                    if (item != null && item.quantity >= 1) {
                                        RewardService.gI().rewardNguoiTuyetBangGia(player);
                                        InventoryService.gI().sendItemBags(player);
                                        InventoryService.gI().subQuantityItemsBag(player, item, 1);
                                    }
                                }
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.SACH_TUYET_KY) {
                        switch (select) {
                            case 0 -> {
                                Item trangSachCu = InventoryService.gI().findItemBagByTemp(player, 1188);
                                Item biaSach = InventoryService.gI().findItemBagByTemp(player, 1154);
                                if ((trangSachCu != null && trangSachCu.quantity >= 9999) && (biaSach != null && biaSach.quantity >= 1)) {
                                    createOtherMenu(player, ConstNpc.DONG_THANH_SACH_CU,
                                            "|2|Chế tạo Cuốn sách cũ\n"
                                            + "|1|Trang sách cũ " + trangSachCu.quantity + "/9999\n"
                                            + "Bìa sách " + biaSach.quantity + "/1\n"
                                            + "|4|Tỉ lệ thành công: 20%\n"
                                            + "Thất bại mất 99 trang sách và 1 bìa sách", "Đồng ý", "Từ chối");
                                } else {
                                    String NpcSay = "|2|Chế tạo Cuốn sách cũ\n";
                                    if (trangSachCu == null) {
                                        NpcSay += "|7|Trang sách cũ " + "0/9999\n";
                                    } else {
                                        NpcSay += "|1|Trang sách cũ " + trangSachCu.quantity + "/9999\n";
                                    }
                                    if (biaSach == null) {
                                        NpcSay += "|7|Bìa sách " + "0/1\n";
                                    } else {
                                        NpcSay += "|1|Bìa sách " + biaSach.quantity + "/1\n";
                                    }

                                    NpcSay += "|7|Tỉ lệ thành công: 20%\n";
                                    NpcSay += "|7|Thất bại mất 99 trang sách và 1 bìa sách";
                                    createOtherMenu(player, ConstNpc.DONG_THANH_SACH_CU_2,
                                            NpcSay, "Từ chối");
                                }
                            }
                            case 1 -> {
                                Item cuonSachCu = InventoryService.gI().findItemBagByTemp(player, 1187);
                                Item kimBam = InventoryService.gI().findItemBagByTemp(player, 1153);

                                if ((cuonSachCu != null && cuonSachCu.quantity >= 10) && (kimBam != null && kimBam.quantity >= 1)) {
                                    createOtherMenu(player, ConstNpc.DOI_SACH_TUYET_KY,
                                            "|2|Đổi sách tuyệt kỹ 1\n"
                                            + "|1|Cuốn sách cũ " + cuonSachCu.quantity + "/10\n"
                                            + "Kìm bấm giấy " + kimBam.quantity + "/1\n"
                                            + "|4|Tỉ lệ thành công: 20%\n", "Đồng ý", "Từ chối");
                                } else {
                                    String NpcSay = "|2|Đổi sách Tuyệt kỹ 1\n";
                                    if (cuonSachCu == null) {
                                        NpcSay += "|7|Cuốn sách cũ " + "0/10\n";
                                    } else {
                                        NpcSay += "|1|Cuốn sách cũ " + cuonSachCu.quantity + "/10\n";
                                    }
                                    if (kimBam == null) {
                                        NpcSay += "|7|Kìm bấm giấy " + "0/1\n";
                                    } else {
                                        NpcSay += "|1|Kìm bấm giấy " + kimBam.quantity + "/1\n";
                                    }
                                    NpcSay += "|7|Tỉ lệ thành công: 20%\n";
                                    createOtherMenu(player, ConstNpc.DOI_SACH_TUYET_KY_2,
                                            NpcSay, "Từ chối");
                                }
                            }
                            case 2 -> // giám định sách
                                CombineService.gI().openTabCombine(player,
                                        CombineService.GIAM_DINH_SACH);
                            case 3 -> // tẩy sách
                                CombineService.gI().openTabCombine(player,
                                        CombineService.TAY_SACH);
                            case 4 -> // nâng cấp sách
                                CombineService.gI().openTabCombine(player,
                                        CombineService.NANG_CAP_SACH_TUYET_KY);
                            case 5 -> // phục hồi sách
                                CombineService.gI().openTabCombine(player,
                                        CombineService.PHUC_HOI_SACH);
                            case 6 -> // phân rã sách
                                CombineService.gI().openTabCombine(player,
                                        CombineService.PHAN_RA_SACH);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.DONG_THANH_SACH_CU) {
                        switch (select) {
                            case 0 -> {
                                Item trangSachCu = InventoryService.gI().findItemBagByTemp(player, 1188);
                                Item biaSach = InventoryService.gI().findItemBagByTemp(player, 1154);
                                Item cuonSachCu = ItemService.gI().createNewItem((short) 1187);
                                if (Util.isTrue(20, 100)) {
                                    cuonSachCu.itemOptions.add(new ItemOption(30, 0));
                                    try { // send effect success
                                        Message msg = new Message(-81);
                                        msg.writer().writeByte(0);
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeShort(tempId);
                                        player.sendMessage(msg);
                                        msg.cleanup();

                                        msg = new Message(-81);
                                        msg.writer().writeByte(1);
                                        msg.writer().writeByte(2);
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, trangSachCu));
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, biaSach));
                                        player.sendMessage(msg);
                                        msg.cleanup();

                                        msg = new Message(-81);
                                        msg.writer().writeByte(7);
                                        msg.writer().writeShort(cuonSachCu.template.iconID);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        player.sendMessage(msg);
                                        msg.cleanup();

                                    } catch (Exception e) {
                                        Logger.logException(BaHatMit.class, e, "Lỗi dong thanh sach success");
                                    }

                                    InventoryService.gI().addItemList(player.inventory.itemsBag, cuonSachCu);
                                    InventoryService.gI().subQuantityItemsBag(player, trangSachCu, 9999);
                                    InventoryService.gI().subQuantityItemsBag(player, biaSach, 1);
                                    InventoryService.gI().sendItemBags(player);
                                } else {
                                    try { // send effect fail
                                        Message msg = new Message(-81);
                                        msg.writer().writeByte(0);
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeShort(tempId);
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                        msg = new Message(-81);
                                        msg.writer().writeByte(1);
                                        msg.writer().writeByte(2);
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, biaSach));
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, trangSachCu));
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                        msg = new Message(-81);
                                        msg.writer().writeByte(8);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                    } catch (Exception e) {
                                        Logger.logException(BaHatMit.class, e, "Lỗi dong thanh sach fail");
                                    }
                                    InventoryService.gI().subQuantityItemsBag(player, trangSachCu, 99);
                                    InventoryService.gI().subQuantityItemsBag(player, biaSach, 1);
                                    InventoryService.gI().sendItemBags(player);
                                }
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.DOI_SACH_TUYET_KY) {
                        switch (select) {
                            case 0 -> {
                                Item cuonSachCu = InventoryService.gI().findItemBagByTemp(player, 1187);
                                Item kimBam = InventoryService.gI().findItemBagByTemp(player, 1153);

                                short baseValue = 1183;
                                short genderModifier = (player.gender == 0) ? -2 : ((player.gender == 2) ? 2 : (short) 0);

                                Item sachTuyetKy = ItemService.gI().createNewItem((short) (baseValue + genderModifier));

                                if (Util.isTrue(20, 100)) {
                                    sachTuyetKy.itemOptions.add(new ItemOption(224, 0));
                                    sachTuyetKy.itemOptions.add(new ItemOption(21, 40));
                                    sachTuyetKy.itemOptions.add(new ItemOption(30, 0));
                                    sachTuyetKy.itemOptions.add(new ItemOption(87, 1));
                                    sachTuyetKy.itemOptions.add(new ItemOption(225, 5));
                                    sachTuyetKy.itemOptions.add(new ItemOption(226, 1000));
                                    try { // send effect success
                                        Message msg = new Message(-81);
                                        msg.writer().writeByte(0);
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeShort(tempId);
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                        msg = new Message(-81);
                                        msg.writer().writeByte(1);
                                        msg.writer().writeByte(2);
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, kimBam));
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, cuonSachCu));
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                        msg = new Message(-81);
                                        msg.writer().writeByte(7);
                                        msg.writer().writeShort(sachTuyetKy.template.iconID);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                    } catch (Exception e) {
                                        Logger.logException(BaHatMit.class, e, "Lỗi doi sach tuyet ky success");
                                    }
                                    InventoryService.gI().addItemList(player.inventory.itemsBag, sachTuyetKy);
                                    InventoryService.gI().subQuantityItemsBag(player, cuonSachCu, 10);
                                    InventoryService.gI().subQuantityItemsBag(player, kimBam, 1);
                                    InventoryService.gI().sendItemBags(player);
                                } else {
                                    try { // send effect fail
                                        Message msg = new Message(-81);
                                        msg.writer().writeByte(0);
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeUTF("test");
                                        msg.writer().writeShort(tempId);
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                        msg = new Message(-81);
                                        msg.writer().writeByte(1);
                                        msg.writer().writeByte(2);
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, kimBam));
                                        msg.writer().writeByte(InventoryService.gI().getIndexBag(player, cuonSachCu));
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                        msg = new Message(-81);
                                        msg.writer().writeByte(8);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        msg.writer().writeShort(-1);
                                        player.sendMessage(msg);
                                        msg.cleanup();
                                    } catch (Exception e) {
                                        Logger.logException(BaHatMit.class, e, "Lỗi doi sach tuyet ky fail");
                                    }
                                    InventoryService.gI().subQuantityItemsBag(player, cuonSachCu, 5);
                                    InventoryService.gI().subQuantityItemsBag(player, kimBam, 1);
                                    InventoryService.gI().sendItemBags(player);
                                }
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_SHOP_BUA) {
                        switch (select) {
                            case 0 ->
                                ShopService.gI().opendShop(player, "BUA_1H", true);
                            case 1 ->
                                ShopService.gI().opendShop(player, "BUA_8H", true);
                            case 2 ->
                                ShopService.gI().opendShop(player, "BUA_1M", true);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                        switch (player.combineNew.typeCombine) {
                            case CombineService.NANG_CAP_VAT_PHAM, CombineService.NANG_CAP_BONG_TAI, CombineService.NANG_CHI_SO_BONG_TAI, CombineService.LAM_PHEP_NHAP_DA, CombineService.NHAP_NGOC_RONG, CombineService.GIAM_DINH_SACH, CombineService.TAY_SACH, CombineService.NANG_CAP_SACH_TUYET_KY, CombineService.PHUC_HOI_SACH, CombineService.PHAN_RA_SACH -> {
                                switch (select) {
                                    case 0 ->
                                        CombineService.gI().startCombine(player);
                                    case 1 ->
                                        CombineService.gI().startCombineVip(player, 10);
                                    case 2 ->
                                        CombineService.gI().startCombineVip(player, 100);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void nhanBinhNuocFree(Player player) {
        if (Util.isAfterMidnight(player.lastTimeNhanBinhNuocFree)) {
            if (InventoryService.gI().getCountEmptyBag(player) == 0 && InventoryService.gI().findItemBagByTemp(player, 456) == null) {
                Service.gI().sendThongBao(player, "Hành trang của con đã đầy, hãy dọn bớt ô trống nhé!");
                return;
            }
            Item waterBottle = ItemService.gI().createNewItem((short) 456, 3);
            InventoryService.gI().addItemBag(player, waterBottle);
            InventoryService.gI().sendItemBags(player);
            player.lastTimeNhanBinhNuocFree = System.currentTimeMillis();
            Service.gI().sendThongBao(player, "Bà tặng con 3 Bình nước phép miễn phí hôm nay! Chúc con thi đấu may mắn!");
        } else {
            Service.gI().sendThongBao(player, "Hôm nay con đã nhận 3 Bình nước phép rồi. Hãy quay lại sau 0h nhé!");
        }
    }

    private void menuMuaBinhNuoc(Player player) {
        this.createOtherMenu(player, ConstNpc.MENU_MUA_BINH_NUOC_VDST,
                "Bà bán nước phép với giá hữu nghị 1 Ngọc / 1 bình.\n(Ưu tiên trừ hồng ngọc trước, nếu thiếu bù bằng ngọc xanh)\nCon muốn mua bao nhiêu bình?",
                "Mua 1 bình\n(1 ngọc)",
                "Mua 5 bình\n(5 ngọc)",
                "Mua 10 bình\n(10 ngọc)",
                "Mua 20 bình\n(20 ngọc)",
                "Từ chối");
    }

    private void buyBinhNuoc(Player player, int quantity, long gemCost) {
        if (player.inventory.getGemAndRuby() < gemCost) {
            Service.gI().sendThongBao(player, "Con không đủ ngọc, còn thiếu " + (gemCost - player.inventory.getGemAndRuby()) + " ngọc nữa!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) == 0 && InventoryService.gI().findItemBagByTemp(player, 456) == null) {
            Service.gI().sendThongBao(player, "Hành trang của con đã đầy, hãy dọn bớt ô trống nhé!");
            return;
        }
        if (!player.inventory.subGemAndRuby(gemCost)) {
            Service.gI().sendThongBao(player, "Con không đủ ngọc!");
            return;
        }
        Service.gI().sendMoney(player);
        PlayerDAO.updatePlayer(player);
        Item waterBottle = ItemService.gI().createNewItem((short) 456, quantity);
        InventoryService.gI().addItemBag(player, waterBottle);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Con đã mua thành công " + quantity + " Bình nước phép!");
    }

    private void showHuongDanVDST(Player player) {
        String text = "1. THỂ THỨC THI ĐẤU\b"
                + "Võ Đài Sinh Tử gồm 5 Hiệp đấu với 5 Đại Cao Thủ:\b"
                + "Dracula, Người Vô Hình, Bông Băng, Sa Tăng, Thỏ Đầu Bạc\b"
                + "Mỗi hiệp tối đa 3 phút, có 5 giây nghỉ ngơi chuẩn bị\b"
                + "Hạ gục cả 5 Boss để Vô Địch! Hết HP, hết giờ là Thua\n"
                + "2. VÉ THI ĐẤU (BÌNH NƯỚC PHÉP)\b"
                + "Vé vào cửa Võ Đài Sinh Tử là Bình Nước Phép\b"
                + "Mỗi ngày gặp ta nhận MIỄN PHÍ 3 bình (reset lúc 0h đêm)\b"
                + "Lượt 1: 1 bình, Lượt 2: 2 bình, tăng dần mỗi lượt (reset sau 0h)\b"
                + "Nếu hết, mua thêm tại NPC Santa tab Đặc Biệt (1 Ngọc / 1 bình)\n"
                + "3. CƠ CHẾ BÌNH CHỌN CHO KHÁN GIẢ\b"
                + "Khán giả có thể cược 1.000.000 Vàng cho Người Chơi hoặc Bà Hạt Mít\b"
                + "Cược Người Chơi: Thắng khi võ sĩ vô địch cả 5 hiệp\b"
                + "Cược Hạt Mít: Thắng khi võ sĩ thất bại\b"
                + "Phe thắng chia đều toàn bộ tiền cược của phe thua!\b"
                + "Hoàn trả 100% tiền cược nếu chỉ có 1 bên cược\n"
                + "4. PHẦN THƯỞNG MỖI TRẬN THẮNG\b"
                + "Chiến thắng cả 5 Hiệp đấu sẽ nhận được:\b"
                + "300.000 đến 500.000 Vàng, Tiềm Năng & Sức Mạnh khủng\b"
                + "1 Vệ Tinh ngẫu nhiên + 1 đến 3 Đá Nâng Cấp trang bị\b"
                + "May mắn nhận: Bùa 1h (30%), Thỏi Vàng (3%), Cải Trang (15%)\b"
                + "ĐẶC BIỆT: Cơ hội nhận CẢI TRANG BOSS VĨNH VIỄN!\n"
                + "5. ĐUA TOP TUẦN (0H CHỦ NHẬT)\b"
                + "Giải đấu diễn ra liên tục theo chu kỳ 1 tuần\b"
                + "Chốt giải và reset điểm all player về 0 lúc 0h Chủ Nhật hàng tuần\b"
                + "CƠ CẤU PHẦN THƯỞNG TOP NGỌC:\b"
                + "Top 1: 200 Ngọc  |  Top 2: 100 Ngọc\b"
                + "Top 3: 50 Ngọc   |  Top 4: 30 Ngọc   |  Top 5: 20 Ngọc\b"
                + "Quà tự động phát lúc 0h CN hoặc nhận tại 'Thưởng Top Tuần' của ta!";
        NpcService.gI().createTutorial(player, this.tempId, this.avartar, text);
    }
}
package models.npc.npc_list;

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
                        player.haveRewardVDST = false;
                        player.thoiVangVoDaiSinhTu = 0;
                    }
                    if (player.haveRewardVDST) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Đây là phần thưởng cho con.",
                                "vệ tinh\nbất kì", "từ chối");
                        return;
                    }
                    DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
    if (vdst != null) {
        if (vdst.getPlayer().equals(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ngươi muốn hủy đăng ký thi đấu võ đài?",
                    "Bảng Xếp\nHạng", // Thêm vào
                    "Hủy Đăng Ký", // Đổi tên cho rõ ràng
                    "Về\nđảo rùa");
        } else {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                    "Bảng Xếp\nHạng", // Thêm vào
                    "Bình chọn",
                    "Thách Đấu", // Đổi tên cho rõ ràng
                    "Về\nđảo rùa");
        }
    } else {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                "Bảng Xếp\nHạng", // Thêm vào
                "Đăng ký\n" + (player.thoiVangVoDaiSinhTu + 1) + " bình nước", // Sửa lại logic hiển thị chi phí
                "Về\nđảo rùa");
    }
    // ================== KẾT THÚC SỬA ĐỔI ==================
    break;}
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

                    if (player.luotNhanBuaMienPhi == 1) {
                        if (nguoituyet != null && nguoituyetbanggia != null) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                    "Thưởng\nBùa 1h\nngẫu nhiên", "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm",
                                    nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng", "Giao\nNgười Tuyết", "Giao\nNgười Tuyết\nBăng Giá");
                        } else if (nguoituyet != null) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                    "Thưởng\nBùa 1h\nngẫu nhiên", "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm",
                                    nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng", "Giao\nNgười Tuyết");
                        } else if (nguoituyetbanggia != null) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                    "Thưởng\nBùa 1h\nngẫu nhiên", "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm",
                                    nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng", "Giao\nNgười Tuyết\nBăng Giá");
                        } else {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                                    "Thưởng\nBùa 1h\nngẫu nhiên", "Sách\nTuyệt kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm",
                                    nangcapbt, "Làm phép\nNhập đá", "Nhập\nNgọc Rồng");
                        }
                    } else {
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
                        player.haveRewardVDST = false;
                        player.thoiVangVoDaiSinhTu = 0;
                    }

                    if (player.idMark.isBaseMenu()) {
                        if (player.haveRewardVDST) {
                            switch (select) {
                                case 0 -> { // nhận vệ tinh
                                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                        short itemId = (short) Util.nextInt(342, 345); // bao gồm cả 345
                                        Item item = ItemService.gI().createNewItem(itemId);

                                        // đảm bảo itemOptions đã khởi tạo
                                        if (item.itemOptions == null) {
                                            item.itemOptions = new ArrayList<>();
                                        }

                                        // add option tùy itemId
                                        switch (itemId) {
                                            case 342 -> item.itemOptions.add(new Item.ItemOption(81, 5));
                                            case 343 -> item.itemOptions.add(new Item.ItemOption(83, 20));
                                            case 344 -> item.itemOptions.add(new Item.ItemOption(82, 1));
                                            case 345 -> item.itemOptions.add(new Item.ItemOption(80, 5));
                                        }

                                        InventoryService.gI().addItemBag(player, item);
                                        InventoryService.gI().sendItemBags(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);

                                        player.haveRewardVDST = false;
                                        return; // tránh chạy tiếp
                                    } else {
                                        Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống, không thể nhặt thêm");
                                        return;
                                    }
                                }
                                case 1 -> {
                                    // từ chối nhận phần thưởng
                                    return;
                                }
                            }
                            return; // thoát sau khi xử lý reward
                        }

                        // Xử lý đăng ký hoặc hủy tham gia VDST
                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
        if (vdst != null) {
            if (vdst.getPlayer().equals(player)) { // Người chơi là chủ phòng
                switch (select) {
                    case 0: // Bảng Xếp Hạng
                        Service.gI().showListTop(player, Manager.topArena);
                        break;
                    case 1: // Hủy đăng ký
                        DeathOrAliveArenaService.gI().cancelChallenge(player);
                        break;
                    case 2: // Về đảo rùa
                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        break;
                }
            } else { // Người chơi là khán giả
                switch (select) {
                    case 0: // Bảng Xếp Hạng
                        Service.gI().showListTop(player, Manager.topArena);
                        break;
                    case 1: // Bình chọn
                        this.createOtherMenu(player, ConstNpc.DAT_CUOC_HAT_MIT,
                                "Phí bình chọn là 1 triệu vàng...",
                                "Bình chọn cho " + vdst.getPlayer().name,
                                "Bình chọn cho hạt mít");
                        break;
                    case 2: // Thách đấu
                        DeathOrAliveArenaService.gI().startChallenge(player);
                        break;
                    case 3: // Về đảo rùa
                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        break;
                }
            }
        } else { // Chưa có ai đăng ký
            switch (select) {
                case 0: // Bảng Xếp Hạng
                    Service.gI().showListTop(player, Manager.topArena);
                    break;
                case 1: // Đăng ký
                    DeathOrAliveArenaService.gI().startChallenge(player);
                    break;
                case 2: // Về đảo rùa
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
                        if (player.luotNhanBuaMienPhi == 1) {
                            switch (select) {
                                case 0 -> {
                                    if (player.luotNhanBuaMienPhi == 1) {
                                        int idItem = Util.nextInt(213, 219);
                                        player.charms.addTimeCharms(idItem, 60);
                                        Item bua = ItemService.gI().createNewItem((short) idItem);
                                        player.luotNhanBuaMienPhi = 0;
                                        Service.gI().sendThongBao(player, "Bạn vừa nhận thưởng " + bua.getName());
                                    } else {
                                        Service.gI().sendThongBao(player, "Hôm nay bạn đã nhận bùa miễn phí rồi!!!");
                                    }
                                }
                                case 1 ->
                                    createOtherMenu(player, ConstNpc.SACH_TUYET_KY, "Ta có thể giúp gì cho ngươi ?",
                                            "Đóng thành\nSách cũ",
                                            "Đổi Sách\nTuyệt kỹ",
                                            "Giám định\nSách",
                                            "Tẩy\nSách",
                                            "Nâng cấp\nSách\nTuyệt kỹ",
                                            "Hồi phục\nSách",
                                            "Phân rã\nSách");
                                case 2 -> // shop bùa
                                    createOtherMenu(player, ConstNpc.MENU_OPTION_SHOP_BUA,
                                            "Bùa của ta rất lợi hại, nhìn ngươi yếu đuối thế này, chắc muốn mua bùa để "
                                            + "mạnh mẽ à, mua không ta bán cho, xài rồi lại thích cho mà xem.",
                                            "Bùa\n1 giờ", "Bùa\n8 giờ", "Bùa\n1 tháng", "Đóng");
                                case 3 ->
                                    CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_VAT_PHAM);
                                case 4 -> {
                                    if (InventoryService.gI().findItemBongTaiCap2(player)) {
                                        CombineService.gI().openTabCombine(player, CombineService.NANG_CHI_SO_BONG_TAI);
                                    } else {
                                        CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_BONG_TAI);
                                    }
                                }
                                case 5 -> // làm phép nhập đá
                                    CombineService.gI().openTabCombine(player, CombineService.LAM_PHEP_NHAP_DA);
                                case 6 -> // nhập ngọc rồng
                                    CombineService.gI().openTabCombine(player, CombineService.NHAP_NGOC_RONG);
                                case 7 -> {
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
                                case 8 -> {
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
                        } else {
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
}
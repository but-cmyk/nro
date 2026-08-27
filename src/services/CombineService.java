package services;

import consts.ConstNpc;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import models.combine.list.CheTaoDoThienSu;
import models.combine.list.ChuyenHoaBangNgoc;
import models.combine.list.ChuyenHoaBangVang;
import models.item.Item;
import models.combine.list.EpSaoTrangBi;
import models.combine.list.GiamDinhSach;
import models.combine.list.LamPhepNhapDa;
import models.combine.list.NangCapBongTai;
import models.combine.list.NangCapSachTuyetKi;
import models.combine.list.NangCapSetKichHoat;
import models.combine.list.NangCapVatPham;
import models.combine.list.NangChiSoBongTai;
import models.combine.list.NhapNgocRong;
import models.combine.list.PhaLeHoaTrangBi;
//import models.combine.list.PhanRaDoThan;
import models.combine.list.PhanRaSach;
import models.combine.list.PhucHoiSach;
import models.combine.list.TaySach;
import models.item.Item.ItemOption;
import models.player.Player;
import network.io.Message;
import models.npc.Npc;
import server.Manager;
import services.map.NpcManager;
import services.player.InventoryService;
import utils.Logger;
import utils.Util;

public class CombineService {

    public static final byte MAX_STAR_ITEM = 7;
    public static final byte MAX_LEVEL_ITEM = 8;

    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte COMBINE_SUCCESS = 2;
    private static final byte COMBINE_FAIL = 3;
    private static final byte COMBINE_DRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;
    public static final byte COMBINE_DA_VUN = 7;

    public static final int CUONG_HOA_LO_SAO = 102;
    public static final int TAO_DA_HEMATITE = 103;
    public static final int DANH_BONG_SAO_PHA_LE = 104;
    public static final int NANG_CAP_SAO_PHA_LE = 105;

    public static final int EP_SAO_TRANG_BI = 500;
    public static final int PHA_LE_HOA_TRANG_BI = 501;
    public static final int CHUYEN_HOA_TRANG_BI = 502;
    public static final int DOI_DO_THAN = 559;
    public static final int DOI_SKH_HD = 560;
    public static final int NANG_CAP_VAT_PHAM = 510;
    public static final int NANG_CAP_BONG_TAI = 511;
    public static final int LAM_PHEP_NHAP_DA = 512;
    public static final int NHAP_NGOC_RONG = 513;
    public static final int PHAN_RA_DO_THAN_LINH = 514;
    public static final int NANG_CAP_DO_TS = 515;
    public static final int NANG_CHI_SO_BONG_TAI = 517;
    public static final int NANG_CAP_DO_THIEN_SU = 51300;
    public static final int NANG_HUY_DIET_LEN_SKH = 346543;
    // Chuyển hóa trang bị
    public static final int CHUYEN_HOA_BANG_VANG = 46346;
    public static final int CHUYEN_HOA_BANG_NGOC = 58745;

    // sách tuyệt kĩ
    public static final int GIAM_DINH_SACH = 57457;
    public static final int TAY_SACH = 555;
    public static final int NANG_CAP_SACH_TUYET_KY = 556;
    public static final int PHUC_HOI_SACH = 557;
    public static final int PHAN_RA_SACH = 558;

    public static final int PHAN_RA_DO_THAN = 20102003;

    private static CombineService instance;

    public final Npc baHatMit;
    public final Npc shopNew;
    public final Npc whis;

    private CombineService() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.whis = NpcManager.getNpc(ConstNpc.WHIS);
        this.shopNew = NpcManager.getNpc(ConstNpc.SHOP_NEW);
    }

    public static CombineService gI() {
        if (instance == null) {
            instance = new CombineService();
        }
        return instance;
    }

    /**
     * Hiển thị thông tin đập đồ
     *
     * @param player
     * @param index
     */
    public void showInfoCombine(Player player, int[] index) {
        if (player == null || player.combineNew == null || index == null) {
            return;
        }
        player.combineNew.clearItemCombine();
        if (index.length > 0 && player.inventory != null && player.inventory.itemsBag != null) {
            java.util.Set<Integer> addedSlots = new java.util.HashSet<>();
            for (int slot : index) {
                if (slot >= 0 && slot < player.inventory.itemsBag.size()) {
                    if (addedSlots.add(slot)) {
                        Item item = player.inventory.itemsBag.get(slot);
                        if (item != null && item.isNotNullItem()) {
                            player.combineNew.itemsCombine.add(item);
                        }
                    }
                }
            }
        }
        switch (player.combineNew.typeCombine) {
            case GIAM_DINH_SACH:
                GiamDinhSach.showInfoCombine(player);
                break;
            case TAY_SACH:
                TaySach.showInfoCombine(player);
                break;
            case NANG_CAP_SACH_TUYET_KY:
                NangCapSachTuyetKi.showInfoCombine(player);
                break;
            case PHUC_HOI_SACH:
                PhucHoiSach.showInfoCombine(player);
                break;
            case PHAN_RA_SACH:
                PhanRaSach.showInfoCombine(player);
                break;
            case CHUYEN_HOA_BANG_NGOC:
                ChuyenHoaBangNgoc.showInfoCombine(player);
                break;
            case CHUYEN_HOA_BANG_VANG:
                ChuyenHoaBangVang.showInfoCombine(player);
                break;
            case NANG_HUY_DIET_LEN_SKH:
                NangCapSetKichHoat.showInfoCombine(player);
                break;
            case NANG_CAP_DO_THIEN_SU:
                // SỬA LỖI: Thêm lại logic kiểm tra và hiển thị menu xác nhận tại đây
                if (player.combineNew.itemsCombine.size() == 4) {
                    Optional<Item> optCtVip = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThucVip()).findFirst();
                    Optional<Item> optMTS = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS()).findFirst();
                    Optional<Item> optDaNC = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaNangCap1()).findFirst();
                    Optional<Item> optDaMM = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaMayMan()).findFirst();

                    if (!optCtVip.isPresent() || !optMTS.isPresent() || !optDaNC.isPresent() || !optDaMM.isPresent()) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu nguyên liệu chế tạo.", "Đóng");
                        return;
                    }

                    if (optMTS.get().quantity < 999) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần đủ 999 Mảnh Thiên Sứ.", "Đóng");
                        return;
                    }

                    String npcSay = "|2|Con có muốn chế tạo trang bị Thiên Sứ không?\n\n"
                            + "|7|Công thức VIP\n"
                            + "999 Mảnh Thiên Sứ\n"
                            + "1 Đá Nâng Cấp\n"
                            + "1 Đá May Mắn\n\n"
                            + "|1|Cần " + Util.powerToString(50_000_000) + " vàng";

                    if (player.inventory.gold < 50_000_000) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }

                    this.whis.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Chế tạo\n" + Util.powerToString(50_000_000) + " vàng", "Từ chối");

                } else {
                    this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để chế tạo, hãy quay lại sau", "Đóng");
                }
                break;
            case EP_SAO_TRANG_BI:
                EpSaoTrangBi.showInfoCombine(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.showInfoCombine(player);
                break;
            case NHAP_NGOC_RONG:
                NhapNgocRong.showInfoCombine(player);
                break;
            case LAM_PHEP_NHAP_DA:
                LamPhepNhapDa.showInfoCombine(player);
                break;
            case NANG_CAP_VAT_PHAM:
                NangCapVatPham.showInfoCombine(player);
                break;
            case NANG_CAP_BONG_TAI:
                NangCapBongTai.showInfoCombine(player);
                break;
            case NANG_CHI_SO_BONG_TAI:
                NangChiSoBongTai.showInfoCombine(player);
                break;
            case CUONG_HOA_LO_SAO:
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (player.combineNew.itemsCombine.stream().filter(
                            item -> item.isNotNullItem() && (item.template.type <= 5
                            || item.template.type == 32))
                            .count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu Trang Bị Cường Hóa", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 1019)
                            .count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu Đá Hematite", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream()
                            .filter(item -> item.isNotNullItem() && item.template.id == 1020)
                            .count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu Dùi Đục", "Đóng");
                        return;
                    }
                    String npcSay = "Ngươi Muốn Cường Hóa Vật Phẩm Này\n Tỉ Lệ Thành Công : 100% Chứ";
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                            npcSay, "Cường Hóa", "Từ chối");
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Nguyên liệu không phù hợp",
                                "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Còn thiếu nguyên liệu để cường hóa hãy quay lại sau", "Đóng");
                }
                break;
            // ... các case khác giữ nguyên
        }
    }

    /**
     * Bắt đầu đập đồ - điều hướng từng loại đập đồ
     *
     * @param player
     */
    public void startCombine(Player player, int... n) {
        int num = 0;
        if (n.length > 0) {
            num = n[0];
        }
        switch (player.combineNew.typeCombine) {
            case GIAM_DINH_SACH:
                GiamDinhSach.GiamDinhsach(player);
                break;
            case TAY_SACH:
                TaySach.Taysach(player);
                break;
            case NANG_CAP_SACH_TUYET_KY:
                NangCapSachTuyetKi.NangCapSachTuyetki(player);
                break;
            case PHUC_HOI_SACH:
                PhucHoiSach.PhucHoisach(player);
                break;
            case PHAN_RA_SACH:
                PhanRaSach.PhanRasach(player);
                break;
            case CHUYEN_HOA_BANG_NGOC:
                ChuyenHoaBangNgoc.ChuyenHoaBangngoc(player);
                break;
            case CHUYEN_HOA_BANG_VANG:
                ChuyenHoaBangVang.ChuyenHoaBangvang(player);
                break;
            case NANG_HUY_DIET_LEN_SKH:
                NangCapSetKichHoat.nangCapSetKichHoat(player);
                break;
            case NANG_CAP_DO_THIEN_SU:
                CheTaoDoThienSu.startCombine(player);
                break;
            case LAM_PHEP_NHAP_DA:
                LamPhepNhapDa.LamPhepNhapda(player);
                break;
            case EP_SAO_TRANG_BI:
                EpSaoTrangBi.epSaoTrangBi(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.phaLeHoa(player, num);
                break;
            case NHAP_NGOC_RONG:
                NhapNgocRong.nhapNgocRong(player);
                break;
            case NANG_CAP_VAT_PHAM:
                NangCapVatPham.nangCapVatPham(player);
                break;
            case NANG_CAP_BONG_TAI:
                NangCapBongTai.nangCapBongTai(player);
                break;
            case NANG_CHI_SO_BONG_TAI:
                NangChiSoBongTai.nangChiSoBongTai(player);
                break;
            case NANG_CAP_SAO_PHA_LE:
                NangSaoC2(player);
                break;
            case DANH_BONG_SAO_PHA_LE:
                SaoLapLanh(player);
                break;
            case TAO_DA_HEMATITE:
                taodahematite(player);
                break;
            case CUONG_HOA_LO_SAO:
                devminhdung_cuonghoa(player);
                break;
            case DOI_DO_THAN:
                DoiVeNdung(player);
                break;
            case DOI_SKH_HD:
                SkhHDNdung(player);
                break;
        }

        player.idMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();
        database.daos.PlayerDAO.updatePlayerAsync(player);
    }

    public void NangSaoC2(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item SPL = null;
        Item DaHematit = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id == 1019) {
                DaHematit = item;
            }
            if (item.template.id >= 441 && item.template.id <= 447) {
                SPL = item;
            }
        }
        if (SPL == null || DaHematit == null || SPL.quantity < 2 || DaHematit.quantity < 1) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.ruby < 2 || player.inventory.gold < 200_000_000) {
            Service.gI().sendThongBao(player, "Không đủ hồng ngọc hoặc vàng!");
            reOpenItemCombine(player);
            return;
        }
        int newItemId = -1;
        switch (SPL.template.id) {
            case 441:
                newItemId = 1352;  // Đỏ hp
                break;
            case 442:
                newItemId = 1353;  // Lam ki
                break;
            case 443:
                newItemId = 1354;  // Hồng Phản
                break;
            case 444:
                newItemId = 1355;  // Tím Xuyên giáp
                break;
            case 445:
                newItemId = 1356;  // Cam Xuyên giáp cân chiến
                break;
            case 446:
                newItemId = 1357;  // Vàng Rớt Vàng
                break;
            case 447:
                newItemId = 1358;  // Lục tnsm
                break;
        }
        if (newItemId != -1) {
            Item newItem = ItemService.gI().createNewItem((short) newItemId);
            RewardService.gI().initBaseOptionSaoPhaLe(newItem);
            newItem.itemOptions.add(new ItemOption(72, 1));
            InventoryService.gI().addItemBag(player, newItem);
            Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
            InventoryService.gI().subQuantityItemsBag(player, SPL, 2);
            InventoryService.gI().subQuantityItemsBag(player, DaHematit, 1);
            player.inventory.ruby -= 2;
            player.inventory.gold -= 200_000_000;
            sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
        }
    }

    public void SaoLapLanh(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item SPL = null;
        Item Damai = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id == 1018) {
                Damai = item;
            }
            if (item.template.id >= 1350 && item.template.id <= 1358) {
                SPL = item;
            }
        }
        if (SPL == null || Damai == null || SPL.quantity < 5 || Damai.quantity < 1) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.ruby < 2 || player.inventory.gold < 200_000_000) {
            Service.gI().sendThongBao(player, "Không đủ hồng ngọc hoặc vàng!");
            reOpenItemCombine(player);
            return;
        }
        int newItemId = -1;
        switch (SPL.template.id) {
            case 1350:
                newItemId = 1366;
                break; // Đen
            case 1351:
                newItemId = 1367;
                break; // Trắng
            case 1352:
                newItemId = 1359;
                break; // Đỏ
            case 1353:
                newItemId = 1360;
                break; // Lam
            case 1354:
                newItemId = 1361;
                break; // Hồng
            case 1355:
                newItemId = 1362;
                break; // Tím
            case 1356:
                newItemId = 1363;
                break; // Cam
            case 1357:
                newItemId = 1364;
                break; // Vàng
            case 1358:
                newItemId = 1365;
                break; // Lục
        }
        if (newItemId != -1) {
            Item newItem = ItemService.gI().createNewItem((short) newItemId);
            RewardService.gI().initBaseOptionSaoPhaLe(newItem);
            newItem.itemOptions.add(new ItemOption(72, 1));
            InventoryService.gI().addItemBag(player, newItem);
            Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
            InventoryService.gI().subQuantityItemsBag(player, SPL, 5);
            InventoryService.gI().subQuantityItemsBag(player, Damai, 1);
            player.inventory.ruby -= 2;
            player.inventory.gold -= 200_000_000;
            sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
        }
    }

    private void devminhdung_cuonghoa(Player player) {
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && (item.template.type <= 5
                || item.template.type == 32))
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Trang Bị");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1019)
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Đá Hematite");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1020)
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Dùi Đục");
            return;
        }
        Item TrangBi = null;
        Item Hematite = null;
        Item Duiduc = null;
        int checkOption = 0;
        int star = 0;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.type <= 5
                    || item.template.type == 32) {
                TrangBi = item;
            } else if (item.template.id == 1019) {
                Hematite = item;
            } else if (item.template.id == 1020) {
                Duiduc = item;
            }
        }
        for (Item.ItemOption io : TrangBi.itemOptions) {
            if (io.optionTemplate.id == 107) {
                star = io.param;
                checkOption++;
            } else if (io.optionTemplate.id == 230) {
                checkOption = 0;
            }
        }
        if (star <= 7) {
            Service.gI().sendThongBao(player, "Trang Bị Của Ngươi Chưa Đạt 8 Ô Sao Đen");
            return;
        }
        if (checkOption == 0) {
            Service.gI().sendThongBao(player, "Trang Bị Của Ngươi Đã Được Cường Hóa");
            return;
        }
        if ((TrangBi == null || Duiduc == null) || Hematite == null) {
            Service.gI().sendThongBao(player, "Không đủ nguyên liệu nâng cấp!");
            reOpenItemCombine(player);
        } else if (Util.isTrue(35, 100)) {
            InventoryService.gI().subQuantityItemsBag(player, Duiduc, 1);
            InventoryService.gI().subQuantityItemsBag(player, Hematite, 1);
            TrangBi.itemOptions.add(new ItemOption(230, 1));
            sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            reOpenItemCombine(player);
        } else {
            sendEffectSuccessCombine(player);
            sendEffectFailCombine(player);
        }
    }

    public void taodahematite(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item SPL = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id >= 1350 && item.template.id <= 1358) {
                SPL = item;
            }
        }
        if (SPL == null || SPL.quantity < 5) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.ruby < 2 || player.inventory.gold < 200_000_000) {
            Service.gI().sendThongBao(player, "Không đủ hồng ngọc hoặc vàng!");
            reOpenItemCombine(player);
            return;
        }
        Item newItem = ItemService.gI().createNewItem((short) 1019);
        InventoryService.gI().addItemBag(player, newItem);
        Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
        InventoryService.gI().subQuantityItemsBag(player, SPL, 5);
        player.inventory.ruby -= 2;
        player.inventory.gold -= 200_000_000;
        sendEffectSuccessCombine(player);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        reOpenItemCombine(player);
    }

    public void DoiVeNdung(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        Item DTL = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.isDTL()) {
                DTL = item;
            }
        }
        if (DTL == null) {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
            return;
        }
        if (player.inventory.gold < 500_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng!");
            reOpenItemCombine(player);
            return;
        }
        int ManhHD = -1;
        switch (DTL.template.id) {
            case 555 -> // Áo trái đất
                ManhHD = 1368;
            case 557 -> // Áo Namek
                ManhHD = 1369;
            case 559 -> // Áo Xayda
                ManhHD = 1370;
            case 556 -> // Quần Trái Đất
                ManhHD = 1371;
            case 558 -> // Quần Namek
                ManhHD = 1372;
            case 560 -> // Quần Xaday
                ManhHD = 1373;
            case 561 -> // Nhẫn
                ManhHD = 1380;
            case 562 -> // Găng Trái Đất
                ManhHD = 1374;
            case 564 -> // Găng Namek
                ManhHD = 1375;
            case 566 -> // Găng Xayda
                ManhHD = 1376;
            case 563 -> // Giày Trái Đất
                ManhHD = 1377;
            case 565 -> // Giày Namek
                ManhHD = 1378;
            case 567 -> // Giày Xayda
                ManhHD = 1379;
        }
        if (ManhHD != -1) {
            Item newItem = ItemService.gI().createNewItem((short) ManhHD);
            newItem.itemOptions.add(new ItemOption(30, 0));
            InventoryService.gI().addItemBag(player, newItem);
            Service.gI().sendThongBao(player, "|7|Bạn nhận được " + newItem.template.name);
            InventoryService.gI().subQuantityItemsBag(player, DTL, 1);
            player.inventory.gold -= 500_000_000;
            sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm nâng cấp!");
        }
    }

    public void SkhHDNdung(Player player) {
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.gI().sendThongBao(player, "Sai nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).count() != 3) {
            Service.gI().sendThongBao(player, "Thiếu đồ huỷ diệt");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.inventory.gold < 1) {
                Service.gI().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            player.inventory.gold -= 500000000;
            Item dohdodayne = player.combineNew.itemsCombine.stream().filter(Item::isDHD).findFirst().get();
            List<Item> itemdohdlucbovao = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).collect(Collectors.toList());
            CombineService.gI().sendEffectOpenItem(player, dohdodayne.template.iconID, dohdodayne.template.iconID);
            short itemId;
            if (dohdodayne.template.gender == 3 || dohdodayne.template.type == 4) {
                itemId = Manager.radaSKHVip[0];
                if (player.getSession().bdPlayer > 0 && Util.isTrue(1, (int) (100 / player.getSession().bdPlayer))) {
                    itemId = Manager.radaSKHVip[0];
                }
            } else {
                itemId = Manager.doSKHVip[dohdodayne.template.gender][dohdodayne.template.type][0];
                if (player.getSession().bdPlayer > 0 && Util.isTrue(1, (int) (100 / player.getSession().bdPlayer))) {
                    itemId = Manager.doSKHVip[dohdodayne.template.gender][dohdodayne.template.type][0];
                }
            }
            int skhId = ItemService.gI().randomSKHId(player.gender);
            Item item = ItemService.gI().itemSKH(itemId, skhId);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().subQuantityItemsBag(player, dohdodayne, 1);
            itemdohdlucbovao.forEach(it -> InventoryService.gI().subQuantityItemsBag(player, it, 1));
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            player.combineNew.itemsCombine.clear();
            reOpenItemCombine(player);
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public void startCombineVip(Player player, int n) {
        switch (player.combineNew.typeCombine) {
            case PHA_LE_HOA_TRANG_BI:
                PhaLeHoaTrangBi.phaLeHoa(player, n);
                break;
            case NHAP_NGOC_RONG:
                NhapNgocRong.nhapNgocRong(player, n);
                break;
        }

        player.idMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();

    }

    public void openTabCombine(Player player, int type) {
        player.combineNew.setTypeCombine(type);
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            if (player.idMark.getNpcChose() != null) {
                msg.writer().writeShort(player.idMark.getNpcChose().tempId);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

//    public void sendEffectCombineItem(Player player, byte type, short icon1, short icon2) {
//        Message msg = null;
//        try {
//            msg = new Message(-81);
//            msg.writer().writeByte(type);
//            switch (type) {
//                case 0:
//                    msg.writer().writeUTF("");
//                    msg.writer().writeUTF("");
//                    break;
//                case 1:
//                    msg.writer().writeByte(0);
//                    msg.writer().writeByte(-1);
//                    break;
//                case 2: // success 0 eff 0
//                case 3: // success 1 eff 0
//                    break;
//                case 4: // success 0 eff 1
//                    msg.writer().writeShort(icon1);
//                    break;
//                case 5: // success 0 eff 2
//                    msg.writer().writeShort(icon1);
//                    break;
//                case 6: // success 0 eff 3
//                    msg.writer().writeShort(icon1);
//                    msg.writer().writeShort(icon2);
//                    break;
//                case 7: // success 0 eff 4
//                    msg.writer().writeShort(icon1);
//                    break;
//                case 8: // success 1 eff 4
//                    // Lam do ts
//                    break;
//            }
//            msg.writer().writeShort(-1); // id npc
//            msg.writer().writeShort(-1); // x
//            msg.writer().writeShort(-1); // y
//            player.sendMessage(msg);
//        } catch (Exception e) {
//        } finally {
//            if (msg != null) {
//                msg.cleanup();
//            }
//        }
//    }

    public void sendEffectSuccessCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_SUCCESS);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendEffectFailCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_FAIL);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void reOpenItemCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combineNew.itemsCombine.size());
            for (Item it : player.combineNew.itemsCombine) {
                if (it == null) continue;
                for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                    Item bagItem = player.inventory.itemsBag.get(i);
                    if (bagItem != null && (it == bagItem || (it.template != null && bagItem.template != null && it.template.id == bagItem.template.id && it.createTime == bagItem.createTime))) {
                        msg.writer().writeByte(i);
                        break;
                    }
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(CombineService.class, e, "Lỗi reOpenItemCombine");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public boolean issachTuyetKy(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.type == 35) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean checkHaveOption(Item item, int viTriOption, int idOption) {
        if (item != null && item.isNotNullItem() && viTriOption >= 0 && viTriOption < item.itemOptions.size()) {
            Item.ItemOption option = item.itemOptions.get(viTriOption);
            return option != null && option.optionTemplate != null && option.optionTemplate.id == idOption;
        }
        return false;
    }

    public void sendEffectCombineDV(Player player, short icon) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DA_VUN);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEffectCombineDB(Player player, short icon) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public boolean isTrangBiGoc(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (isDoLuongLong(item) || isDoJean(item) || isDoZelot(item) || isDoKaio(item) || isDoDaJean(item) || isDoBacZelot(item) ) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTrangBiChuyenHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (isDoThanXD(item) || isDoThanTD(item) || isDoThanNM(item) || isDoLuongLong(item) || isDoJean(item) || isDoZelot(item)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isCheckTrungTypevsGender(Item item, Item item2) {
        if (item != null && item.isNotNullItem() && item2 != null && item2.isNotNullItem()) {
            if (item.template.type == item2.template.type && item.template.gender == item2.template.gender) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    
     private boolean isDoKaio(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 240 || item.template.id == 252 || item.template.id == 264 || item.template.id == 276 || item.template.id == 280) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoBacZelot(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 5 || item.template.id == 253 - 5 || item.template.id == 265 - 5 || item.template.id == 277 - 5 || item.template.id == 280) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoDaJean(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 9 || item.template.id == 253 - 9 || item.template.id == 265 - 9 || item.template.id == 277 - 9 || item.template.id == 280) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    

    private boolean isDoLuongLong(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 || item.template.id == 253 || item.template.id == 265 || item.template.id == 277 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoZelot(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 4 || item.template.id == 253 - 4 || item.template.id == 265 - 4 || item.template.id == 277 - 4 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoJean(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 241 - 8 || item.template.id == 253 - 8 || item.template.id == 265 - 8 || item.template.id == 277 - 8 || item.template.id == 281) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanXD(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 || item.template.id == 560 || item.template.id == 566 || item.template.id == 567 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDoThanTD(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 - 4 || item.template.id == 560 - 4 || item.template.id == 566 - 4 || item.template.id == 567 - 4 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

//    private String getNameItemC0(int gender, int type) {
//        if (type == 4) {
//            return "Rada cấp 1";
//        }
//        switch (gender) {
//            case 0:
//                switch (type) {
//                    case 0:
//                        return "Áo vải 3 lỗ";
//                    case 1:
//                        return "Quần vải đen";
//                    case 2:
//                        return "Găng thun đen";
//                    case 3:
//                        return "Giầy nhựa";
//                }
//                break;
//            case 1:
//                switch (type) {
//                    case 0:
//                        return "Áo sợi len";
//                    case 1:
//                        return "Quần sợi len";
//                    case 2:
//                        return "Găng sợi len";
//                    case 3:
//                        return "Giầy sợi len";
//                }
//                break;
//            case 2:
//                switch (type) {
//                    case 0:
//                        return "Áo vải thô";
//                    case 1:
//                        return "Quần vải thô";
//                    case 2:
//                        return "Găng vải thô";
//                    case 3:
//                        return "Giầy vải thô";
//                }
//                break;
//        }
//        return "";
//    }

    private boolean isDoThanNM(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 559 - 2 || item.template.id == 560 - 2 || item.template.id == 566 - 2 || item.template.id == 567 - 2 || item.template.id == 561) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private String getTextTopTabCombine(int type) {
        switch (type) {
            case GIAM_DINH_SACH:
                return "Ta sẽ phù phép\ngiám định sách đó cho ngươi";
            case CHUYEN_HOA_BANG_NGOC:
            case CHUYEN_HOA_BANG_VANG:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\n chuyển hóa thành trang bị khác";
            case NANG_HUY_DIET_LEN_SKH:
                return "Ta sẽ nâng cấp đồ huỷ diệt của con lên đồ kích hoạt thường";
            case CUONG_HOA_LO_SAO:
                return "Bà Hạt Mít Cho Phép Bạn Cường Hóa Lỗ Sao Pha Lê";
            case TAO_DA_HEMATITE:
                return "Bà Hạt Mít Cho Phép Bạn Tạo Đá Hematite";
            case NANG_CAP_DO_THIEN_SU:
                return "Chế Tạo\nTrang Bị Thiên Sứ";
            case LAM_PHEP_NHAP_DA:
                return "Ta sẽ phù phép\n99 mảnh đa vụn của ngươi\nthành đá nâng cấp";
            case EP_SAO_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case NHAP_NGOC_RONG:
                return "Ta sẽ phù phép\ncho 7 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case NANG_CAP_VAT_PHAM:
                return "Ta sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case PHAN_RA_DO_THAN:
                return "Ta sẽ phân rã \n  trang bị của người thành điểm!";
            case NANG_CAP_DO_TS:
                return "Ta sẽ nâng cấp \n  trang bị của người thành\n đồ thiên sứ!";
            case NANG_CAP_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành cấp 2";
            case NANG_CHI_SO_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case TAY_SACH:
                return "Ta sẽ phù phép\ntẩy sách đó cho ngươi";
            case NANG_CAP_SACH_TUYET_KY:
                return "Ta sẽ phù phép\nnâng cấp Sách Tuyệt Kỹ cho ngươi";
            case DOI_DO_THAN:
                return "Minh Dũng sẽ phù phép\nnâng cấp";
            case DOI_SKH_HD:
                return "Minh Dũng sẽ phù phép\nnâng cấp";
            case PHUC_HOI_SACH:
                return "Ta sẽ phù phép\nphục hồi sách cho ngươi";
            case PHAN_RA_SACH:
                return "Ta sẽ phù phép\nphân rã sách cho ngươi";
            case PHAN_RA_DO_THAN_LINH:
                return "Ta sẽ phân rã \n  trang bị của người thành Đá!";
            default:
                return "";
        }
    }

    private String getTextInfoTabCombine(int type) {
        switch (type) {
            case TAY_SACH:
                return "Vào hành trang chọn\nsách cần tẩy\nTẩy sách loại bỏ Option cũ";
            case NANG_CAP_SACH_TUYET_KY:
                return "Vào hành trang\nChọn Sách Tuyệt Kỹ 1\n10 Kìm bấm giấy\nSau đó, chọn nâng cấp";
            case PHUC_HOI_SACH:
                return "Vào hành trang\nChọn Sách Tuyệt Kỹ x1\n10 cuốn sách cũ\n Phục hồi sách giúp độ bền sách tăng lên";
            case PHAN_RA_SACH:
                return "Vào hành trang\n Chọn Sách cần phân rã\n Phân rã sách khi số lần tẩy = 0";
            case GIAM_DINH_SACH:
                return "Vào hành trang\nChọn sách cần giám định\nCần thêm bùa giám định\nGiám định sách giúp sách\nCó thêm chỉ số phần trăm HP,KI,SD";
            case NANG_CAP_SAO_PHA_LE:
                return "Vào hành trang\nChọn đá hematite\n chọn sao pha lê (cấp 1)\nSau đó chọn 'Nâng Cấp'";
            case DANH_BONG_SAO_PHA_LE:
                return "Vào hành trang\n chọn sao pha lê cấp 2\ncó từ 2 viên trở lên\nChọn 1 đá mài\nSau đó chọn 'Đánh bóng'";
            case CUONG_HOA_LO_SAO:
                return "Chọn Trang Bị Ô Sao Pha Lê Thứ 8 Chưa Cường Hóa\n Chọn 1 Đá Hematite\n Chọn 1 Dùi Đục"
                        + "\nSau đó chọn 'Cường Hóa'";
            case TAO_DA_HEMATITE:
                return "Chọn 5 Sao Pha Lê Cấp 2 Cùng Màu"
                        + "\nSau đó chọn 'Tạo Đá Hematite'";
            case CHUYEN_HOA_BANG_NGOC:
            case CHUYEN_HOA_BANG_VANG:
                return "Vào hành trang\nChọn trang bị gốc ô 1\n(Áo,quần,găng,giày hoặc rada)\ntừ cấp[+4] trở lên\nChọn tiếp trang bị cần chuyển hóa ô 2\nvà chưa nâng cấp\nsau đó chọn 'Nâng cấp'";
            case NANG_HUY_DIET_LEN_SKH:
                return "Vào hành trang\n Chọn 1 món huỷ diệt bất kỳ và 1 món thần linh bất kì, sau đó chọn 'Nâng câp'";
            case DOI_SKH_HD:
                return "Vào hành trang\n Chọn 3 món huỷ diệt bất kỳ, sau đó chọn\nMón Đầu Tiên Sẽ Là Gốc'Nâng câp'";
            case DOI_DO_THAN:
                return "Vào hành trang\n Chọn 1 món thần linh bất kì, sau đó chọn 'Nâng câp'";
            case NANG_CAP_DO_THIEN_SU:
                return "Vào hành trang\nChọn Công Thức VIP\nKèm Mảnh Thiên Sứ, Đá Nâng Cấp\nvà Đá May Mắn\n\nSau đó chọn 'Chế tạo'";
            case LAM_PHEP_NHAP_DA:
                return "Chọn 99 mảnh đá vụn\n Chọn 1 bình nước phép\n Bấm nâng cấp";
            case EP_SAO_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\nChọn loại sao pha lê\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case NHAP_NGOC_RONG:
                return "Vào hành trang\nChọn 7 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case NANG_CAP_VAT_PHAM:
                return "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để nâng cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PHAN_RA_DO_THAN_LINH:
                return "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để phân rã\n"
                        + "Sau đó chọn 'Phân Rã'";
            case NANG_CAP_DO_TS:
                return "vào hành trang\nChọn 2 trang bị hủy diệt bất kì\nkèm 1 món đồ thần linh\n và 5 mảnh thiên sứ\n "
                        + "sẽ cho ra đồ thiên sứ từ 0-15% chỉ số"
                        + "Sau đó chọn 'Nâng Cấp'";
            case NANG_CAP_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh bông tai để nâng cấp, Số lượng 9999 cái"
                        + "\nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata\nChọn mảnh hồn porata số lượng 99"
                        + "\ncái và đá xanh lam để nâng cấp.\nSau đó chọn 'Nâng cấp chỉ số'";
            case PHAN_RA_DO_THAN:
                return "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa) Thần Linh\n"
                        + "Sau đó chọn 'Phân Rã' , khi phân rã sẽ nhận được Đồng Coin";
            default:
                return "";
        }
    }

}
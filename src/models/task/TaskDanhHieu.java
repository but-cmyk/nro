package models.task;

import models.item.Item;
import models.player.Player;
import models.shop.ItemShop;
import services.Service;
import services.player.InventoryService;

public class TaskDanhHieu {

    public int Nap;
    public int Shenron;
    public int Hagucboss;
    public int DapDo;
    public int SieuHang;
    public int TaskBoMong;
    public int ChoSuong;
    public int ChoNuoc;
    public int NhatDo;
    public int AnTrom;
    public int ODo;
    public int DungLoa;

    public long ResetTime;

    public TaskDanhHieu() {
        this.Nap = 0;             // Nạp Tích Lũy 10000 Ngọc Trong Ngày
        this.Shenron = 0;         // Ước Rồng Thần 1 Sao x10 Lần
        this.Hagucboss = 0;       // Hạ Gục Cumber, Black Goku, Cooler, Xên (30 Lần)
        this.DapDo = 0;           // Đập 3 Trang Bị +7 Trong Ngày
        this.SieuHang = 0;        // Top 1 Đại Hội Võ Đài Siêu Hạng
        this.TaskBoMong = 0;      // Hoàn Thành 10 Nhiệm Vụ Siêu Khó Tại Bò Mộng
        this.ChoSuong = 0;        // Đánh Bại, Hoặc Cho Xương Sói 20 Lần
        this.ChoNuoc = 0;         // Hoàn Thành 5 Lần Nhiệm Vụ Cho Nước Xinbato
        this.NhatDo = 0;          // Nhặt Đồ 500 Lần Trong Ngày
        this.AnTrom = 0;          // Tiêu Diệt 30 Lần Boss Ăn Trộm
        this.ODo = 0;             // Tiêu Diệt 30 Lần Boss Ở Dơ
        this.DungLoa = 0;         // Sử Dụng Loa Liên Vũ Trụ 10 Lần Trong Ngày
    }

    public void renew() {
        if (utils.Util.isAfterMidnight(ResetTime)) {
            this.Nap = 0;
            this.DapDo = 0;
            this.NhatDo = 0;
            this.DungLoa = 0;
            this.ResetTime = System.currentTimeMillis();
        }
    }

    public int getRequired(int itemId) {
        return switch (itemId) {
            case 1286 -> 20;   // Kẻ thao túng sói
            case 1287 -> 5;    // Nước anh bao
            case 1289 -> 5000; // Đại gia mới nhú
            case 1290 -> 10;   // Trùm ước rồng
            case 1291 -> 30;   // Trùm săn Boss
            case 1292 -> 3;    // Thánh đập đồ +7
            case 1293 -> 3;    // Cao thủ siêu hạng
            case 1294 -> 1;    // Nông dân chăm chỉ
            case 1295 -> 500;  // Ông thần ve chai
            case 1296 -> 30;   // Bị móc sạch túi
            case 1300 -> 30;   // Thánh ở dơ
            default -> 0;
        };
    }

    public int getCurrent(int itemId) {
        renew();
        return switch (itemId) {
            case 1286 -> this.ChoSuong;
            case 1287 -> this.ChoNuoc;
            case 1289 -> this.Nap;
            case 1290 -> this.Shenron;
            case 1291 -> this.Hagucboss;
            case 1292 -> this.DapDo;
            case 1293 -> this.SieuHang;
            case 1294 -> this.TaskBoMong;
            case 1295 -> this.NhatDo;
            case 1296 -> this.AnTrom;
            case 1300 -> this.ODo;
            default -> 0;
        };
    }

    public boolean CheckItem(Player player, ItemShop itemShop, int itemId) {
        renew();
        Item existingItem = InventoryService.gI().findItemInAllInventories(player, itemId);
        if (existingItem != null) {
            Service.gI().sendThongBao(player, "Bạn đã sở hữu danh hiệu này rồi.");
            return false;
        }
        int required = getRequired(itemId);
        if (required <= 0) {
            return true;
        }
        int current = getCurrent(itemId);
        if (current < required) {
            Service.gI().sendThongBao(player, "Bạn chưa mở khoá danh hiệu này (" + current + "/" + required + ")");
            return false;
        } else {
            return true;
        }
    }
}

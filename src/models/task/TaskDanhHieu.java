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

    public boolean CheckItem(Player player, ItemShop itemShop, int itemId) {
        Item existingItem = InventoryService.gI().findItemInAllInventories(player, itemId);
        if (existingItem != null) {
            Service.gI().sendThongBao(player, "Bạn đã sở hữu danh hiệu này rồi.");
            return false;
        }
        int required = 0;
        int current = 0;
        switch (itemId) {
            case 1289 -> {
                required = 5000;
                current = player.playerTask.taskdh.Nap;
            }
            case 1287 -> {
                required = 5;
                current = player.playerTask.taskdh.ChoNuoc;
            }
            case 1290 -> {
                required = 50;
                current = player.playerTask.taskdh.Shenron;
            }
            case 1291 -> {
                required = 10;
                current = player.playerTask.taskdh.Hagucboss;
            }
            case 1292 -> {
                required = 30;
                current = player.playerTask.taskdh.DapDo;
            }
            case 1293 -> {
                required = 3;
                current = player.playerTask.taskdh.SieuHang;
            }
            case 1294 -> {
                required = 1;
                current = player.playerTask.taskdh.TaskBoMong;
            }
            case 1295 -> {
                required = 10;
                current = player.playerTask.taskdh.NhatDo;
            }
            case 1296 -> {
                required = 500;
                current = player.playerTask.taskdh.AnTrom;
            }
            case 1300 -> {
                required = 30;
                current = player.playerTask.taskdh.ODo;
            }
            case 1286 -> {
                required = 30;
                current = player.playerTask.taskdh.ChoSuong;
            }
            case 1175 -> {
                required = 1000;
            }
            case 1176 -> {
                required = 1000;
            }
            case 1177 -> {
                required = 1000;
            }
            case 1178 -> {
                required = 1000;
            }
            default -> {
                return true;
            }
        }
        if (current < required) {
            Service.gI().sendThongBao(player, "Bạn chưa mở khoá danh hiệu này");
            return false;
        } else {
            Service.gI().sendThongBao(player, "Bạn đã sở hữu danh hiệu này");
            return true;
        }
    }
}

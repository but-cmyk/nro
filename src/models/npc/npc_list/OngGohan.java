package models.npc.npc_list;

import java.text.DecimalFormat;
import consts.ConstNpc;
import consts.ConstTask;
import database.daos.PlayerDAO;
import models.item.Item;
import models.npc.Npc;
import models.player.Player;
import services.player.InventoryService;
import utils.Util;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.Input;
import services.map.ChangeMapService;
import services.map.NpcService;

/*public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào con , Đến với thế giới Ngọc Rồng Mobi \nGift-code : tanthu , caitrang , nromobi , nromobi1 , nromobi2\n Server miễn phí , chịu khó cày cuốc nhé , Cơ chế như Teamobi.",
                        "Nhập\nGiftcode", "Quên Mã\nBảo Vệ", "Quy Đổi\nThỏi Vàng\nHồng Ngọc", "Đổi Mật\nKhẩu", "Bỏ Qua\nNhiệm Vụ");

            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 3 ->
                        Input.gI().createFormChangePassword(player);
                    case 0 -> {
                        Input.gI().createFormGiftCode(player);
                    }
                    case 1 -> {
                        Input.gI().createFormMBV(player);
                    }
                    case 2 -> {
                        this.createOtherMenu(player, ConstNpc.MENU_OPEN_VND,
                                "Con muốn đổi vnd thành gì ?\n"
                                + "|7|Số tiền của bạn còn : " + dcm.format(player.getSession().sotien) + " VND",
                                "Mua Hồng Ngọc", "Mua Thỏi Vàng", "Đóng");
                    }
                    // case 5 -> {
                    // this.createOtherMenu(player, ConstNpc.MENU_MTV,
                    // "Mở thành viên 1đ hoặc đạt 15 tỷ sức mạnh ?\n",
                    // "Mở thành viên\n10k", "Đạt 15 tỷ\nSức Mạnh");
                    // }
                    case 4 -> {
                        switch (player.playerTask.taskMain.id) {
                            case 10 -> {
                                if (player.playerTask.taskMain.index == 1) {
                                    TaskService.gI().doneTask(player, ConstTask.TASK_10_1);
                                } else {
                                    Service.gI().sendThongBao(player, "Ta đã giúp con hoàn thành nhiệm vụ");
                                }
                            }
                            case 19 -> {
                                if (player.playerTask.taskMain.index == 1) {
                                    TaskService.gI().doneTask(player, ConstTask.TASK_19_0);
                                    TaskService.gI().doneTask(player, ConstTask.TASK_19_1);
                                    TaskService.gI().doneTask(player, ConstTask.TASK_19_2);
                                } else {
                                    Service.gI().sendThongBao(player, "Ta đã giúp con hoàn thành nhiệm vụ");
                                }
                            }
                            default ->
                                Service.gI().sendThongBao(player,
                                        "Ta chỉ giúp con bỏ qua nhiệm vụ Tàu 77 và nhiệm vụ Trung Uy Trắng được thôi");
                        }
                    }
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPEN_VND) {
                switch (select) {
                    case 0 ->
                        this.createOtherMenu(player, ConstNpc.MENU_OPEN_HONG_NGOC,
                                "|7|Số tiền của bạn còn : " + dcm.format(player.getSession().sotien) + " VNĐ\n"
                                + "Muốn quy đổi không",
                                "Quy Đổi\n10.000\n 1000 Hồng Ngọc", "Quy Đổi\n20.000\n 2000 Hồng Ngọc",
                                "Quy Đổi\n50.000\n 5100 Hồng Ngọc", "Quy Đổi\n100.000\n 11000 Hồng Ngọc",
                                "Quy Đổi\n500.000 \n55000 Hồng Ngọc", "Quy Đổi\n1.000.000 \n120000 Hồng Ngọc", "Đóng");
                    case 1 ->
                        this.createOtherMenu(player, ConstNpc.MENU_THOI_VANG,
                                "|7|Số tiền của bạn còn : " + dcm.format(player.getSession().sotien) + " VNĐ\n"
                                + "Muốn quy đổi không",
                                "Quy Đổi\n10.000\n 10 Thỏi Vàng", "Quy Đổi\n20.000\n 20 Thỏi Vàng",
                                "Quy Đổi\n50.000\n 50 Thỏi Vàng", "Quy Đổi\n100.000\n 105 Thỏi Vàng",
                                "Quy Đổi\n200.000 \n210 Thỏi Vàng", "Quy Đổi\n500.000 \n550 Thỏi Vàng", "Quy Đổi\n1.000.000 \n1100 Thỏi Vàng", "Đóng");
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_MTV) {
                switch (select) {
                    case 0:
                        if (!player.getSession().actived) {
                            if (player.getSession().sotien >= 10000) {
                                if (PlayerDAO.subVND2(player, 10000)) {
                                    Item itemqua;
                                    itemqua = ItemService.gI().createNewItem((short) 987, 5);
                                    InventoryService.gI().addItemBag(player, itemqua);
                                    Service.gI().sendThongBao(player, "Bạn Đã Nhận Được " + itemqua.template.name);
                                    Service.gI().sendThongBao(player, "Đã mở thành viên thành công");
                                } else {
                                    this.npcChat(player, "Lỗi vui lòng báo admin...");
                                }
                            } else {
                                Service.gI().sendThongBao(player, "Không đủ 10k");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên rồi");
                        }
                        break;

                    case 1:
                        if (player.getSession().player.nPoint.power >= 15_000_000_000L) {
                            if (PlayerDAO.subVND2(player, 0)) {
                                Service.gI().sendThongBao(player, "Đã mở thành viên thành công");
                            } else {
                                this.npcChat(player, "Lỗi vui lòng báo admin...");
                            }
                        } else {
                            this.npcChat(player, "Bạn chưa đủ sức mạnh");
                            Service.gI().sendThongBao(player, "Yêu cầu sm lớn hơn 15 tỷ");
                        }
                        break;
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPEN_HONG_NGOC) {
                int required = 5000;
                int percentDone = (int) ((double) player.playerTask.taskdh.Nap / required * 100);
                if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                    Service.gI().sendThongBaoOK(player, "Cần ít nhất 2 ô trống hành trang!");
                    return;
                }
                switch (select) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        _HandlerChangeRuby(player, select);
                        Service.gI().sendThongBao(player, "Tiến độ hiện tại: " + percentDone + "%");
                        break;
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_THOI_VANG) {
                 if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                    Service.gI().sendThongBaoOK(player, "Cần ít nhất 2 ô trống hành trang!");
                    return;
                }
                switch (select) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        _HandlerChangThoiVang(player, select);
                        break;
                }
            }
        }
    }

    int[] money = {10_000, 20_000, 50_000, 100_000, 500_000, 1_000_000};
    int[] ruby = {1_000, 2_000, 5_100, 11_000, 55_000, 120_000};
    
    int[] vnd  = {10_000, 20_000, 50_000, 100_000, 200_000, 500_000, 1_000_000};
    int[] vang = {10, 20, 50, 105, 210, 550, 1100};

    void _HandlerChangeRuby(Player player, int index) {
        int MONEY = money[index];
        int RUBY = ruby[index];
        if (player.getSession().sotien >= MONEY) {
            if (PlayerDAO.subvnd(player, MONEY)) {
                player.inventory.ruby += RUBY;
                Service.gI().sendMoney(player);
                Service.gI().sendThongBao(player,
                        "Bạn đổi thành công " + dcm.format(MONEY) + " thành " + RUBY + " hồng ngọc !");
            } else {
                Service.gI().sendThongBao(player, "LỖI TRỪ TIỀN !");
            }
        } else {
            this.npcChat(player,
                    "Con cần nạp thêm " + (Util.numberToMoney(MONEY - player.getSession().sotien) + " nữa !"));
        }

    }
    void _HandlerChangThoiVang(Player player, int index) {
        int VND = vnd[index];
        int THOIVANG = vang[index];
        if (player.getSession().sotien >= VND) {
            if (PlayerDAO.subvnd(player, VND)) {
                Item thoiVang = ItemService.gI().createNewItem((short)457);
                thoiVang.quantity = THOIVANG;
                thoiVang.itemOptions.add(new Item.ItemOption(100,1));
                thoiVang.itemOptions.add(new Item.ItemOption(86,0));
                Service.gI().sendMoney(player);
                InventoryService.gI().addItemBag(player, thoiVang);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player,
                        "Bạn đổi thành công " + dcm.format(VND) + " thành " + THOIVANG + " Thỏi Vàng !");
            } else {
                Service.gI().sendThongBao(player, "LỖI TRỪ TIỀN !");
            }
        } else {
            this.npcChat(player,
                    "Con cần nạp thêm " + (Util.numberToMoney(VND - player.getSession().sotien) + " nữa !"));
        }

    }

    DecimalFormat dcm = new DecimalFormat("##,###");
}*/
public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                NpcService.gI().createTutorial(player, tempId, this.avartar,
                        "Con cố gắng theo Quy Lão Kame học thành tài, đừng lo lắng cho ta.");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {

                    }
                }
            }
        }
    }
}


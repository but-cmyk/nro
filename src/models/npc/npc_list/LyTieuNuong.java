package models.npc.npc_list;

import consts.ConstNpc;
import models.npc.Npc;
import models.player.Player;
import services.Service;
import services.TaskService;
import services.func.Input;
import services.func.minigame.CSMM;
import utils.TimeUtil;

public class LyTieuNuong extends Npc {

    public LyTieuNuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Xin chào, Ta là Lý Tiểu Nương\nCasino ta mở xuyên ngày đêm quay số may mắn với giá trị 1 ngọc /1 số.",
                        "Thể lệ", "Con số may mắn", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player pl, int select) {
        if (canOpenNpc(pl)) {
            switch (pl.idMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 ->
                            createOtherMenu(pl, ConstNpc.IGNORE_MENU,
                                    ConstNpc.TextNpc(pl, ConstNpc.TAI_XIU_HD), "Ok");
                        case 1 -> {
                            this.createOtherMenu(pl, 2,
                                    "Chúc mừng các người chơi trúng số lần trước!"
                                    + "\nTrò chơi sẽ bắt đầu sau: "
                                    + TimeUtil.getTimeLeftInSeconds(CSMM.gI().lastTimeRollCSMM, CSMM.gI().SecondsTarget) + " nữa"
                                    + (CSMM.gI().NumRANDOM != -1
                                    ? ("\n Kết quả giải trước là: " + CSMM.gI().NumRANDOM)
                                    : "")
                                    + (CSMM.gI().listRegNumber.containsKey(pl)
                                    ? ("\nBạn đã đăng ký: " + CSMM.gI().listRegNumber.get(pl).stream()
                                            .map(String::valueOf)
                                            .reduce((a, b) -> a + ", " + b).orElse("Không có số nào."))
                                    : "\nBạn chưa đăng ký số nào."),
                                     "Dự đoán", "Cập nhật", "Đóng");
                        }
                    }
                }

                case 2 -> {
                    switch (select) {
                        case 0 ->
                            Input.gI().createFormCSMM(pl);
                        case 1 ->
                            this.createOtherMenu(pl, 2,
                                    "Chúc mừng các người chơi trúng số lần trước!"
                                    + "\nTrò chơi sẽ bắt đầu sau: "
                                    + TimeUtil.getTimeLeftInSeconds(CSMM.gI().lastTimeRollCSMM, CSMM.gI().SecondsTarget) + " nữa"
                                    + (CSMM.gI().NumRANDOM != -1
                                    ? ("\nKết quả giải trước là: " + CSMM.gI().NumRANDOM)
                                    : "")
                                    + (CSMM.gI().listRegNumber.containsKey(pl)
                                    ? ("\nBạn đã đăng ký: " + CSMM.gI().listRegNumber.get(pl).stream()
                                            .map(String::valueOf)
                                            .reduce((a, b) -> a + ", " + b).orElse("Không có số nào."))
                                    : "\nBạn chưa đăng ký số nào."),
                                    "Dự đoán", "Cập nhật", "Đóng");
                    }
                }
            }
        }
    }
}

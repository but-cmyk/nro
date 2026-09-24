package models.npc.npc_list;

import consts.ConstNpc;
import consts.ConstTask;
import database.AlyraManager;
import models.item.Item;
import models.npc.Npc;
import models.player.Player;
import services.*;
import services.func.Input;
import services.player.InventoryService;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import services.player.PlayerService;

public class BoMong extends Npc {

    public BoMong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (this.mapId == 47 || this.mapId == 84) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi muốn vip, có nhiều cách, nạp thẻ là nhanh nhất, còn không thì chịu khó cày hãy nghe lời thầy dạy cần cù bù siêng năng.",
                            "Nhiệm vụ\nhàng ngày", "Nhiệm vụ\nthành tích", "Nhập\nGiftcode", "Quên Mã\nBảo Vệ", "Đổi Mật\nKhẩu", "Từ chối");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 47 || this.mapId == 84) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> {
                            player.playerTask.sideTask.renew();
                            if (player.playerTask.sideTask.template != null) {
                                String npcSay = "Nhiệm vụ hiện tại: " + player.playerTask.sideTask.getName() + " ("
                                        + player.playerTask.sideTask.getLevel() + ")"
                                        + "\nHiện tại đã hoàn thành: " + player.playerTask.sideTask.count + "/"
                                        + player.playerTask.sideTask.maxCount + " ("
                                        + player.playerTask.sideTask.getPercentProcess() + "%)\nSố nhiệm vụ còn lại trong ngày: "
                                        + player.playerTask.sideTask.leftTask + "/" + ConstTask.MAX_SIDE_TASK;
                                this.createOtherMenu(player, ConstNpc.MENU_OPTION_PAY_SIDE_TASK,
                                        npcSay, "Trả nhiệm\nvụ", "Hủy nhiệm\nvụ");
                            } else if (player.playerTask.sideTask.leftTask <= 0) {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Hôm nay bạn đã hoàn thành tối đa " + ConstTask.MAX_SIDE_TASK + " nhiệm vụ rồi!\nHãy nghỉ ngơi và quay lại vào ngày mai nhé.",
                                        "Đóng");
                            } else if (player.playerTask.sideTask.cancelCount > 3 && player.playerTask.sideTask.getRemainingCooldownSeconds() > 0) {
                                int remainSeconds = player.playerTask.sideTask.getRemainingCooldownSeconds();
                                int m = remainSeconds / 60;
                                int s = remainSeconds % 60;
                                String timeStr = (m > 0 ? m + " phút " : "") + s + " giây";
                                this.createOtherMenu(player, ConstNpc.MENU_QUICK_RESET_SIDE_TASK,
                                        "Bạn đã hủy nhiệm vụ quá 3 lần hôm nay!\nCần chờ " + timeStr + " mới có thể nhận nhiệm vụ mới.\n"
                                        + "Bạn có muốn dùng 1 Ngọc Xanh để đổi nhiệm vụ ngay lập tức?",
                                        "Đổi ngay\n(1 Ngọc)", "Chờ đợi");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK,
                                        "Tôi có vài nhiệm vụ theo cấp bậc, sức cậu có thể làm được cái nào?\n(Lưu ý: Cần đủ Sức mạnh và đã mở bản đồ tương ứng)",
                                        "Dễ\n(Tân thủ)",
                                        "Bình thường\n(>= 1.5M SM)",
                                        "Khó\n(>= 15M SM)",
                                        "Siêu khó\n(>= 150M SM)",
                                        "Địa ngục\n(>= 1.5 Tỷ SM)",
                                        "Từ chối");
                            }
                        }
                        case 1 ->
                            AchievementService.gI().openAchievementUI(player);
                        case 2 ->
                            Input.gI().createFormGiftCode(player);
                        case 3 ->
                            Input.gI().createFormMBV(player);

                        case 4 ->
                            Input.gI().createFormChangePassword(player);

                    }

                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK) {
                    switch (select) {
                        case 0, 1, 2, 3, 4 ->
                            TaskService.gI().changeSideTask(player, (byte) select);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_PAY_SIDE_TASK) {
                    switch (select) {
                        case 0 ->
                            TaskService.gI().paySideTask(player);
                        case 1 ->
                            TaskService.gI().removeSideTask(player);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_QUICK_RESET_SIDE_TASK) {
                    switch (select) {
                        case 0 ->
                            TaskService.gI().quickResetSideTaskWithGem(player, this);
                    }
                }
            }
        }
    }
}

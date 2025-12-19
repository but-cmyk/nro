package models.npc.npc_list;

import consts.ConstNpc;
import models.npc.Npc;
import models.player.NPoint;
import models.player.Player;
import services.OpenPowerService;
import services.Service;
import utils.Util;

public class QuocVuong extends Npc {

    public QuocVuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                "Bản thân", "Đệ tử", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            // Menu chính: Chọn mở cho bản thân hay đệ tử
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0: { // Lựa chọn: Mở cho BẢN THÂN (Giữ nguyên logic cũ theo yêu cầu)
                        if (player.nPoint.canOpenNextPowerLimit()) {
                            this.createOtherMenu(player, ConstNpc.OPEN_POWER_MYSEFT,
                                    "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của bản thân lên "
                                            + Util.powerToString(player.nPoint.getPowerNextLimit()),
                                    "Nâng\ngiới hạn\nsức mạnh",
                                    "Nâng ngay\n" + Util.powerToString(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " ngọc", "Đóng");
                        } else {
                            if (player.nPoint.limitPower >= NPoint.MAX_LIMIT) {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Sức mạnh của con đã đạt tới giới hạn tối đa.", "Đóng");
                            } else {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Sức mạnh của con chưa đạt tới giới hạn hiện tại ("
                                                + Util.powerToString(player.nPoint.getPowerLimit()) + "), hãy luyện tập thêm đi.", "Đóng");
                            }
                        }
                        break;
                    }
                    case 1: { // Lựa chọn: Mở cho ĐỆ TỬ
                        if (player.pet != null) {
                            // --- SỬA ĐỔI Ở ĐÂY ---
                            // Không dùng player.pet.nPoint.canOpenNextPowerLimit() nữa
                            // Chỉ kiểm tra xem giới hạn hiện tại có nhỏ hơn giới hạn tối đa của game hay không

                            if (player.pet.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                // Cho phép mở menu nâng cấp luôn mà không cần check sức mạnh hiện tại
                                this.createOtherMenu(player, ConstNpc.OPEN_POWER_PET,
                                        "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của đệ tử lên "
                                                + Util.powerToString(player.pet.nPoint.getPowerNextLimit()),
                                        "Nâng ngay\n" + Util.powerToString(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " ngọc", "Đóng");
                            } else {
                                // Nếu đã max cấp server
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Sức mạnh của đệ tử con đã đạt tới giới hạn tối đa.", "Đóng");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Con không có đệ tử.");
                        }
                        break;
                    }
                }
            }
            // Menu con: Xác nhận nâng cấp cho bản thân
            else if (player.idMark.getIndexMenu() == ConstNpc.OPEN_POWER_MYSEFT) {
                switch (select) {
                    case 0:
                        OpenPowerService.gI().openPowerBasic(player);
                        break;
                    case 1: {
                        if (player.inventory.gem >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                            if (OpenPowerService.gI().openPowerSpeed(player)) {
                                player.inventory.gem -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                                Service.gI().sendMoney(player);
                            }
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Con không đủ ngọc để mở, còn thiếu "
                                            + Util.powerToString((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gem)) + " ngọc");
                        }
                        break;
                    }
                }
            }
            // Menu con: Xác nhận nâng cấp cho đệ tử
            else if (player.idMark.getIndexMenu() == ConstNpc.OPEN_POWER_PET) {
                if (select == 0) {
                    if (player.inventory.gem >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                        // Lưu ý: Cần kiểm tra xem hàm openPowerSpeed trong OpenPowerService
                        // có kiểm tra điều kiện sức mạnh không. Nếu có thì phải sửa cả bên Service.
                        // Nhưng thường thì hàm NPC là cổng kiểm tra chính.
                        if (OpenPowerService.gI().openPowerSpeed(player.pet)) {
                            player.inventory.gem -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                            Service.gI().sendMoney(player);
                        }
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con không đủ ngọc để mở, còn thiếu "
                                        + Util.powerToString((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gem)) + " ngọc");
                    }
                }
            }
        }
    }
}
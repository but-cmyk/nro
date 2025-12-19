package models.npc.npc_list;

import consts.ConstNpc;
import models.npc.Npc;
import models.player.Player;
import services.TaskService;
import services.ShopService;

public class Appule extends Npc {

    public Appule(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                // Đã bỏ đoạn kiểm tra if (player.gender != 2)
                // Hiện menu cho tất cả mọi người
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Ngươi cần trang bị gì cứ đến chỗ ta nhé", "Cửa\nhàng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {
                        // Shop
                        // Đã bỏ kiểm tra giới tính ở đây để ai cũng mở được shop
                        ShopService.gI().opendShop(player, "APPULE", true);
                    }
//                    case 1 -> {
//                        if (!player.inventory.itemsDaBan.isEmpty()) {
//                            ShopService.gI().opendShop(player, "ITEMS_DABAN", true);
//                        }
//                    }
                }
            }
        }
    }
}
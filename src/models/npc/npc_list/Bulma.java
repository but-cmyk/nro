package models.npc.npc_list;

import consts.ConstNpc;
import models.npc.Npc;
import models.player.Player;
import services.TaskService;
import services.ShopService;

public class Bulma extends Npc {

    public Bulma(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                // Đã bỏ if (player.gender != 0)
                // Cho phép tất cả người chơi mở menu cửa hàng
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Cậu cần trang bị gì cứ đến chỗ tôi nhé", "Cửa\nhàng");
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
                        // Đã bỏ kiểm tra giới tính, mở shop cho tất cả
                        ShopService.gI().opendShop(player, "BUNMA", true);
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
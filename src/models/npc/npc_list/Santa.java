package models.npc.npc_list;

import consts.ConstNpc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.item.Item;
import models.npc.Npc;
import models.player.Player;
import services.Service;
import services.ShopService;
import services.func.Input;
import services.player.InventoryService;

public class Santa extends Npc {

    public Santa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {

            Item pGG = InventoryService.gI().findItem(player.inventory.itemsBag, 459);
            int soLuong = 0;
            if (pGG != null) {
                soLuong = pGG.quantity;
            }
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Cửa hàng",
                    "Mở rộng\nHành trang\nRương đồ",
                    "Nhập mã\nquà tặng",
                  
                    "Tiệm\nHớt tóc",
                    "Danh\nhiệu"));

            if (soLuong >= 1) {
                menu.add(1, "Giảm giá\n80%");
            }

            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?", menus);
        }

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            Item pGG = InventoryService.gI().findItem(player.inventory.itemsBag, 459);
            int soLuong = 0;
            if (pGG != null) {
                soLuong = pGG.quantity;
            }

            if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
               if (player.idMark.isBaseMenu()) {
    if (soLuong >= 1) {
        switch (select) {
            case 0 -> ShopService.gI().opendShop(player, "SANTA", false);
            case 1 -> ShopService.gI().opendShop(player, "SANTA_PGG", true);
            case 2 -> ShopService.gI().opendShop(player, "SANTA_MO_RONG_HANH_TRANG", false);
            case 3 -> Input.gI().createFormGiftCode(player);
          //  case 4 -> ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
            case 4 -> ShopService.gI().opendShop(player, "SANTA_HEAD", false);
            case 5 -> ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
        }
    } else {
        switch (select) {
            case 0 -> ShopService.gI().opendShop(player, "SANTA", false);
            case 1 -> ShopService.gI().opendShop(player, "SANTA_MO_RONG_HANH_TRANG", false);
            case 2 -> Input.gI().createFormGiftCode(player);
           // case 3 -> ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
            case 3 -> ShopService.gI().opendShop(player, "SANTA_HEAD", false);
            case 4 -> ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
        }
    }
}

            }
        }
    }
}

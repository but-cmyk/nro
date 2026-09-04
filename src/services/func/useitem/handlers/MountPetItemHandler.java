package services.func.useitem.handlers;

import models.item.Item;
import models.player.Player;
import services.PetService;
import services.Service;
import services.func.useitem.ItemActionHandler;
import services.player.InventoryService;

/**
 * Xử lý sử dụng trang bị vào Body: Thú cưỡi (Type 23, 24), Flag Bag (Type 11),
 * Thú cưng Pet2 (Type 21), Pet Follow (Type 72), Danh hiệu hiệu ứng (Type 36, 35).
 */
public class MountPetItemHandler implements ItemActionHandler {

    @Override
    public boolean canHandle(Player player, Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        int type = item.template.type;
        return type == 23 || type == 24 || type == 11 || type == 21 || type == 72 || type == 36 || type == 35;
    }

    @Override
    public void handle(Player player, Item item, int bagIndex) {
        switch (item.template.type) {
            case 23, 24, 35 -> InventoryService.gI().itemBagToBody(player, bagIndex);
            case 11 -> {
                InventoryService.gI().itemBagToBody(player, bagIndex);
                Service.gI().sendFlagBag(player);
            }
            case 21 -> {
                InventoryService.gI().itemBagToBody(player, bagIndex);
                PetService.Pet2(player, player.getHeadThuCung(), player.getBodyThuCung(), player.getLegThuCung());
                Service.gI().point(player);
            }
            case 72 -> {
                InventoryService.gI().itemBagToBody(player, bagIndex);
                Service.gI().sendPetFollow(player, (short) (item.template.iconID - 1));
            }
            case 36 -> {
                InventoryService.gI().itemBagToBody(player, bagIndex);
                Service.gI().sendEffPlayer(player);
            }
        }
    }
}

package services.func.useitem.handlers;

import models.item.Item;
import models.map.vetinh.*;
import models.player.Player;
import services.Service;
import services.func.useitem.ItemActionHandler;
import services.player.InventoryService;

/**
 * Xử lý sử dụng các loại Vệ Tinh (MP, EXP, DEF, HP) - Type 22.
 */
public class SatelliteItemHandler implements ItemActionHandler {

    @Override
    public boolean canHandle(Player player, Item item) {
        return item != null && item.isNotNullItem() && item.template.type == 22;
    }

    @Override
    public void handle(Player player, Item item, int bagIndex) {
        if (player.zone == null) {
            return;
        }
        if (player.zone.getSatellites().size() >= 3) {
            Service.gI().sendThongBaoOK(player, "Đã đạt tối đa số lượng vệ tinh có thể đặt trong khu!");
            return;
        }

        Satellite satellite = null;
        int yPos = player.zone.map.yPhysicInTop(player.location.x, player.location.y);

        switch (item.template.id) {
            case 342 -> satellite = new SatelliteMp(player.zone, 342, player.location.x, yPos, player);
            case 343 -> satellite = new SatelliteExp(player.zone, 343, player.location.x, yPos, player);
            case 344 -> satellite = new SatelliteDef(player.zone, 344, player.location.x, yPos, player);
            case 345 -> satellite = new SatelliteHp(player.zone, 345, player.location.x, yPos, player);
        }

        if (satellite != null) {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            satellite.sendVeTinh();
        }
    }
}

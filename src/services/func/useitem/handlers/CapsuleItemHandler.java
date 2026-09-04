package services.func.useitem.handlers;

import consts.ConstMap;
import models.item.Item;
import models.map.Zone;
import models.player.Player;
import services.Service;
import services.func.useitem.ItemActionHandler;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.InventoryService;

/**
 * Xử lý sử dụng Capsule 1 lần (193), Capsule vô hạn (194) và chọn Map di chuyển bằng phi thuyền.
 */
public class CapsuleItemHandler implements ItemActionHandler {

    @Override
    public boolean canHandle(Player player, Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        int id = item.template.id;
        return id == 193 || id == 194;
    }

    @Override
    public void handle(Player player, Item item, int bagIndex) {
        if (item.template.id == 193) {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        }
        openCapsuleUI(player);
    }

    public static void openCapsuleUI(Player pl) {
        pl.idMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public static void choseMapCapsule(Player pl, int index) {
        if (pl.idNRNM != -1) {
            Service.gI().sendThongBao(pl, "Không thể mang ngọc rồng này lên Phi thuyền");
            Service.gI().hideWaitDialog(pl);
            return;
        }

        int zoneId = -1;
        if (index > pl.mapCapsule.size() - 1 || index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        zoneChose = ChangeMapService.gI().checkMapCanJoin(pl, zoneChose);
        if (zoneChose == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        if (zoneChose.getNumOfPlayers() > 25
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        pl.changeMapVIP = true;
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }
}

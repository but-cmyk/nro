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
        if (player.isDie()) {
            Service.gI().sendThongBao(player, "Bạn đang kiệt sức, không thể dùng phi thuyền!");
            return;
        }
        if (player.effectSkill != null && (player.effectSkill.isHaveEffectSkill() || player.effectSkill.useTroi)) {
            Service.gI().sendThongBao(player, "Bạn đang bị khống chế, không thể dùng phi thuyền!");
            return;
        }
        if (player.idNRNM != -1) {
            Service.gI().sendThongBao(player, "Không thể mang ngọc rồng này lên Phi thuyền");
            return;
        }
        if (player.zone != null && player.zone.map != null) {
            int currentMapId = player.zone.map.mapId;
            // Cho phép dùng capsule trong tất cả phó bản (Doanh Trại, Kho Báu, Rắn Độc, Khí Gas)
            if (MapService.gI().isMapMaBu(currentMapId)) {
                Service.gI().sendThongBao(player, "Không thể sử dụng phi thuyền tại khu vực Ma Bư!");
                return;
            }
            if (MapService.gI().isMapBlackBallWar(currentMapId)) {
                Service.gI().sendThongBao(player, "Không thể sử dụng phi thuyền trong khu vực Ngọc Rồng Sao Đen!");
                return;
            }
            if (currentMapId == 186 || currentMapId == 187) {
                Service.gI().sendThongBao(player, "Không thể sử dụng phi thuyền tại Võ Đài Whis!");
                return;
            }
            if (currentMapId == 51 || currentMapId == 112 || currentMapId == 113 || currentMapId == 129 || currentMapId == 183) {
                Service.gI().sendThongBao(player, "Không thể sử dụng phi thuyền tại Đại Hội Võ Thuật!");
                return;
            }
        }
        if (item.template.id == 193) {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            InventoryService.gI().sendItemBags(player);
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

        if (pl.mapCapsule == null || index > pl.mapCapsule.size() - 1 || index < 0) {
            Service.gI().sendThongBao(pl, "Danh sách bản đồ đã đóng hoặc không hợp lệ!");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        if (zoneChose == null || zoneChose.map == null) {
            Service.gI().sendThongBao(pl, "Bản đồ không tồn tại!");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone validZone = ChangeMapService.gI().checkMapCanJoin(pl, zoneChose);
        if (validZone == null) {
            String reason = ChangeMapService.gI().getReasonCannotJoinMap(pl, zoneChose);
            Service.gI().sendThongBao(pl, reason != null ? reason : "Bạn chưa thể đến khu vực này!");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        if (MapService.gI().isMapMaBu(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Không thể dùng phi thuyền bay trực tiếp vào khu vực Ma Bư!");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        if (MapService.gI().isMapHuyDiet(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Không thể dùng phi thuyền bay trực tiếp vào khu vực Hủy Diệt!");
            Service.gI().hideWaitDialog(pl);
            return;
        }

        int zoneId = -1;
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        pl.changeMapVIP = true;
        ChangeMapService.gI().changeMapBySpaceShip(pl, zoneChose.map.mapId, zoneId, -1);
    }
}

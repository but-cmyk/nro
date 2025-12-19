package models.npc.npc_list;

import consts.ConstNpc;
import models.npc.Npc;
import models.player.Player;
import services.Service;
import services.map.ChangeMapService;

public class GiuMaDauBo extends Npc {

    public GiuMaDauBo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            // Thêm tùy chọn "Về Đảo Kamê" (Map 5) vào menu
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ngươi đang muốn tìm mảnh vỡ và mảnh hồn bông tai Porata trong truyền thuyết, ta sẽ đưa ngươi đến đó ?",
                    "Vào map\nbang hội", "Về Đảo\nKamê", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (select) {
                case 0 -> {
                    // Logic cũ: Vào map bang hội
                    if (player.playerTask.taskMain.id >= 29) {
                        player.type = 5;
                        player.maxTime = 5;
                        Service.gI().Transport(player);
                    } else {
                        Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ Xên bọ hung mới có thể vào map này!");
                    }
                }
                case 1 -> {
                    // Logic mới: Chuyển về map 5 (Đảo Kamê)
                    // changeMapBySpaceShip(player, mapId, zoneId, x)
                    // mapId: 5, zoneId: -1 (tự động), x: 100 (tọa độ đứng)
                    ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 100);
                }
                default -> {
                    // Nút Đóng hoặc trường hợp khác
                }
            }
        }
    }
}
package models.npc.npc_list;

import consts.ConstNpc;
import models.map.Boss10;
import models.npc.Npc;
import models.player.Player;
import services.map.ChangeMapService;
import services.map.NpcService;

public class Tapion extends Npc {

    public Tapion(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            Boss10.gI().setTimeJoinMap22h();
        }
        if (this.mapId == 19) {
            long now = System.currentTimeMillis();
            if (now > Boss10.TIME_OPEN_22h && now < Boss10.TIME_CLOSE_22h) {
                this.createOtherMenu(player, ConstNpc.MENU_OPEN_MMB, "Phong ấn đã bị phá vỡ, "
                        + "Xin hãy cứu lấy người dân",
                        "Hướng dẫn\nthêm", "Tham gia", "Từ chối");
            } else {
                this.createOtherMenu(player, ConstNpc.MENU_NOT_OPEN_MMB,
                        "Ác quỷ truyền thuyết Hirudegarn đã thoát khỏi phong ấn ngàn năm nHãy giúp tôi chế ngự nó?",
                        "Hướng dẫn", "Từ chối");
            }
        } else if (this.mapId == 126) {
            // SỬA DÒNG NÀY: Đổi "Về nhà" thành "Trở về" cho hợp ngữ cảnh
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ác quỷ truyền thuyết Hirudegarn đã thoát khỏi phong ấn ngàn năm nHãy giúp tôi chế ngự nó?",
                    "Hướng dẫn", "Trở về", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 19:
                    switch (player.idMark.getIndexMenu()) {
                        case ConstNpc.MENU_REWARD_MMB:
                        case ConstNpc.MENU_OPEN_MMB:
                            if (select == 0) {
                                NpcService.gI().createTutorial(player, this.avartar, ConstNpc.HUONG_DAN_MAP_CHI22H);
                            }
                            if (select == 1) {
                                ChangeMapService.gI().changeMapBySpaceShip(player, 126, -1, 140);
                            }
                            break;
                        case ConstNpc.MENU_NOT_OPEN_BDW:
                            if (select == 0) {
                                NpcService.gI().createTutorial(player, this.avartar, ConstNpc.HUONG_DAN_MAP_2H);
                            }
                            break;
                    }
                    break;
                case 126:
                    switch (player.idMark.getIndexMenu()) {
                        case ConstNpc.BASE_MENU:
                            if (select == 0) {
                                NpcService.gI().createTutorial(player, this.avartar, ConstNpc.HUONG_DAN_MAP_CHI22H);
                            }
                            if (select == 1) {
                               
                                ChangeMapService.gI().changeMapBySpaceShip(player, 19, -1, 1050); 
                                // Nếu muốn về đúng chỗ Tapion đứng thì điền tọa độ thay vì -1, ví dụ: 19, -1, 1090, 266
                            }
                            break;
                    }
                    break;
            }
        }
    }
}
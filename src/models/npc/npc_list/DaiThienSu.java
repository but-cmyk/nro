package models.npc.npc_list;

import consts.ConstNpc;
import models.npc.Npc;
import models.player.Player;
import server.Manager;
import services.Service;

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (player.zone.map.mapId == 7 || player.zone.map.mapId == 14 || player.zone.map.mapId == 0) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Con muốn xem bảng xếp hạng nào ?",
                        "BXH\nSức Mạnh", "BXH\nNhiệm Vụ", "BXH\nNạp Thẻ", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.idMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                            Service.gI().showListTop(player, Manager.topSM);
                        }
                        case 1 -> {
                            Service.gI().showListTop(player, Manager.topNV);
                        }
                        case 2 -> {
                            Service.gI().showListTop(player, Manager.topNap);
                        }
                       
                        
                    }
                }
            }
        }
    }
}

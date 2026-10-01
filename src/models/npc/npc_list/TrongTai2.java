package models.npc.npc_list;

import consts.ConstNpc;
import models.npc.Npc;
import models.player.Player;
import models.tournament.whis_arena.WhisArenaService;

public class TrongTai2 extends Npc {

    public TrongTai2(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 187) {
                WhisArenaService.gI().openMenuAtU6Tournament(this, player);
            } else if (this.mapId == 186) {
                WhisArenaService.gI().openMenuAtArena(this, player);
            } else {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Tôi là Trọng tài điều hành Giải đấu Liên Vũ Trụ 6-7!",
                        "Đến Võ Đài\n(Map 186)", "Từ chối");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 187) {
                WhisArenaService.gI().confirmMenuAtU6Tournament(this, player, select);
            } else if (this.mapId == 186) {
                WhisArenaService.gI().confirmMenuAtArena(this, player, select);
            } else {
                if (select == 0) {
                    WhisArenaService.gI().goToArena(player);
                }
            }
        }
    }
}

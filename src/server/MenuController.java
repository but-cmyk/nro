package server;

import java.io.IOException;
import java.util.Objects;
import consts.ConstNpc;
import models.npc.Npc;
import services.map.NpcManager;
import network.session.MySession;
import models.player.Player;
import services.Service;
import services.func.TransactionService;

public class MenuController {

    private static MenuController instance;

    public static MenuController gI() {
        if (instance == null) {
            instance = new MenuController();
        }
        return instance;
    }

    public void openMenuNPC(MySession session, int idnpc, Player player) {
        TransactionService.gI().cancelTrade(player);
        Npc npc;
        if (idnpc == ConstNpc.CALICK && player.zone.map.mapId != 102) {
            npc = NpcManager.getNpc(ConstNpc.CALICK);
        } else if (idnpc == ConstNpc.LY_TIEU_NUONG) {
            npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
        } else {
            npc = player.zone.map.getNpc(player, idnpc);
        }
        if (npc != null) {
            if (idnpc != ConstNpc.CALICK && idnpc != ConstNpc.LY_TIEU_NUONG && player.location != null
                    && utils.Util.getDistance(player.location.x, player.location.y, npc.cx, npc.cy) > 200) {
                Service.gI().sendThongBao(player, "Bạn ở quá xa NPC!");
                Service.gI().hideWaitDialog(player);
                return;
            }
            npc.openBaseMenu(player);
        } else {
            Service.gI().hideWaitDialog(player);
        }
    }

    public void doSelectMenu(Player player, int npcId, int select) throws IOException {
        if (player == null) return;
        synchronized (player) {
            long now = System.currentTimeMillis();
            if (now - player.lastTimeSelectMenu < 250) {
                return;
            }
            player.lastTimeSelectMenu = now;

            TransactionService.gI().cancelTrade(player);
            switch (npcId) {
                case ConstNpc.RONG_THIENG, ConstNpc.CON_MEO ->
                    Objects.requireNonNull(NpcManager.getNpc((byte) npcId)).confirmMenu(player, select);
                default -> {
                    Npc npc = null;
                    if (npcId == ConstNpc.CALICK && player.zone.map.mapId != 102) {
                        npc = NpcManager.getNpc(ConstNpc.CALICK);
                    } else if (npcId == ConstNpc.LY_TIEU_NUONG) {
                        npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
                    } else if (player.zone != null) {
                        npc = player.zone.map.getNpc(player, npcId);
                    }
                    if (npc != null) {
                        if (npcId != ConstNpc.CALICK && npcId != ConstNpc.LY_TIEU_NUONG && player.location != null
                                && utils.Util.getDistance(player.location.x, player.location.y, npc.cx, npc.cy) > 200) {
                            Service.gI().sendThongBao(player, "Bạn ở quá xa NPC!");
                            Service.gI().hideWaitDialog(player);
                            return;
                        }
                        npc.confirmMenu(player, select);
                    } else {
                        Service.gI().hideWaitDialog(player);
                    }
                }
            }
        }
    }
}

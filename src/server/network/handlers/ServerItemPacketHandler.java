package server.network.handlers;

import models.player.Player;
import network.io.Message;
import network.session.MySession;
import server.network.IServerPacketHandler;
import services.map.ItemMapService;

/**
 * Bộ xử lý gói tin Vật Phẩm Map (Item Packet Handler) phía Server.
 * Xử lý Opcode -20 (Nhặt item map).
 */
public class ServerItemPacketHandler implements IServerPacketHandler {

    @Override
    public void handle(MySession session, Player player, Message msg) throws Exception {
        if (player == null || player.isDie()) {
            return;
        }
        byte cmd = msg.command;
        switch (cmd) {
            case -20:
                int itemMapId = msg.reader().readShort();
                ItemMapService.gI().pickItem(player, itemMapId, false);
                break;
        }
    }
}

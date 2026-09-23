package server.network.handlers;

import models.player.Player;
import network.io.Message;
import network.session.MySession;
import server.network.IServerPacketHandler;
import services.func.TransactionService;

/**
 * Bộ xử lý gói tin Giao Dịch (Trade Packet Handler) phía Server.
 * Chịu trách nhiệm cho Opcode -86 (Giao dịch người chơi).
 */
public class ServerTradePacketHandler implements IServerPacketHandler {

    @Override
    public void handle(MySession session, Player player, Message msg) throws Exception {
        if (player == null) {
            return;
        }
        TransactionService.gI().controller(player, msg);
    }
}

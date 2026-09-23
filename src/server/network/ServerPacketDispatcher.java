package server.network;

import models.player.Player;
import network.io.Message;
import network.session.MySession;
import server.network.handlers.ServerItemPacketHandler;
import server.network.handlers.ServerTradePacketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Bộ điều phối gói tin (Server Packet Dispatcher) phía Server.
 * Áp dụng Dispatcher Pattern tương đồng với Client, phân rã God Class Controller.java.
 */
public class ServerPacketDispatcher {

    private static final ServerPacketDispatcher instance = new ServerPacketDispatcher();

    private final Map<Byte, IServerPacketHandler> handlers = new HashMap<>();

    public static ServerPacketDispatcher gI() {
        return instance;
    }

    private ServerPacketDispatcher() {
        registerHandlers();
    }

    private void registerHandlers() {
        ServerTradePacketHandler tradeHandler = new ServerTradePacketHandler();
        register((byte) -86, tradeHandler);

        ServerItemPacketHandler itemHandler = new ServerItemPacketHandler();
        register((byte) -20, itemHandler);
    }

    public void register(byte opcode, IServerPacketHandler handler) {
        handlers.put(opcode, handler);
    }

    public boolean dispatch(MySession session, Player player, Message msg) throws Exception {
        if (msg == null) {
            return false;
        }
        IServerPacketHandler handler = handlers.get(msg.command);
        if (handler != null) {
            handler.handle(session, player, msg);
            return true;
        }
        return false;
    }
}

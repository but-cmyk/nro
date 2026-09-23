package server.network;

import models.player.Player;
import network.io.Message;
import network.session.MySession;

/**
 * Giao diện chuẩn cho các bộ xử lý gói tin (Packet Handlers) chuyên biệt phía Server.
 * Giúp phân rã God Class Controller.java, dễ bảo trì, mở rộng và test độc lập.
 */
public interface IServerPacketHandler {
    void handle(MySession session, Player player, Message msg) throws Exception;
}

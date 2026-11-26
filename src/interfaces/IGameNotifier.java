package interfaces;

import models.player.Player;

/**
 * Domain Port cho các thông báo từ tầng Domain (models) ra tầng Service/Network,
 * giúp phá vỡ phụ thuộc vòng chéo trực tiếp từ models sang services.Service.
 */
public interface IGameNotifier {

    void sendThongBao(Player pl, String thongBao);

    void sendThongBaoOK(Player pl, String text);

    void point(Player player);

    void chat(Player player, String text);

    void chatJustForMe(Player me, Player plChat, String text);

    void Send_Info_NV(Player pl);

    void Send_Caitrang(Player player);

    void sendMoney(Player pl);

    void sendChiSoPetGoc(Player pl);

    void showInfoPet(Player pl);
}

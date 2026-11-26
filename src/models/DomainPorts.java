package models;

import interfaces.IGameNotifier;
import models.player.Player;

/**
 * Điểm truy cập các Domain Ports phía tầng models.
 * Tầng services đăng ký implementation khi khởi tạo, giúp models không import trực tiếp services.
 */
public final class DomainPorts {

    private static final IGameNotifier NO_OP_NOTIFIER = new IGameNotifier() {
        @Override
        public void sendThongBao(Player pl, String thongBao) {
        }

        @Override
        public void sendThongBaoOK(Player pl, String text) {
        }

        @Override
        public void point(Player player) {
        }

        @Override
        public void chat(Player player, String text) {
        }

        @Override
        public void chatJustForMe(Player me, Player plChat, String text) {
        }

        @Override
        public void Send_Info_NV(Player pl) {
        }

        @Override
        public void Send_Caitrang(Player player) {
        }

        @Override
        public void sendMoney(Player pl) {
        }

        @Override
        public void sendChiSoPetGoc(Player pl) {
        }

        @Override
        public void showInfoPet(Player pl) {
        }
    };

    private static volatile IGameNotifier notifier = NO_OP_NOTIFIER;

    private DomainPorts() {
    }

    public static void registerNotifier(IGameNotifier gameNotifier) {
        notifier = gameNotifier != null ? gameNotifier : NO_OP_NOTIFIER;
    }

    public static IGameNotifier notifier() {
        return notifier;
    }
}

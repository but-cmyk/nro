package services.player;
import consts.ConstNpc;
import java.io.IOException;
import java.util.List;
import models.intrinsic.Intrinsic;
import models.player.Player;
import network.io.Message;
import server.Manager;
import services.Service;
import services.map.NpcService;
import utils.Util;

public class IntrinsicService {

    private static IntrinsicService i;
    private static final int[] COST_OPEN = {10, 20, 40, 80, 160, 320, 640, 1280};

    public static IntrinsicService gI() {
        if (i == null) {
            i = new IntrinsicService();
        }
        return i;
    }

    public List<Intrinsic> getIntrinsics(byte playerGender) {
        return switch (playerGender) {
            case 0 -> Manager.INTRINSIC_TD;
            case 1 -> Manager.INTRINSIC_NM;
            default -> Manager.INTRINSIC_XD;
        };
    }

    public Intrinsic getIntrinsicById(int id) {
        for (Intrinsic intrinsic : Manager.INTRINSICS) {
            if (intrinsic.id == id) {
                return new Intrinsic(intrinsic);
            }
        }
        return null;
    }

    public void sendInfoIntrinsic(Player player) {
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.playerIntrinsic.intrinsic.icon);
            msg.writer().writeUTF(player.playerIntrinsic.intrinsic.getName());
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void showAllIntrinsic(Player player) {
        List<Intrinsic> listIntrinsic = getIntrinsics(player.gender);
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(1);
            msg.writer().writeByte(1); //count tab
            msg.writer().writeUTF("Nội tại");
            msg.writer().writeByte(listIntrinsic.size() - 1);
            for (int a = 1; a < listIntrinsic.size(); a++) {
                msg.writer().writeShort(listIntrinsic.get(a).icon);
                msg.writer().writeUTF(listIntrinsic.get(a).getDescription());
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void showMenu(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.INTRINSIC, -1,
                "Nội tại là một kỹ năng bị động hỗ trợ đặc biệt\nBạn có muốn mở hoặc thay đổi nội tại không?",
                "Xem\ntất cả\nNội Tại", "Mở\nNội Tại", "Mở VIP", "Từ chối");
    }

    public void showConfirmOpen(Player player) {
        int index = player.playerIntrinsic.countOpen;
        if (index >= 0 && index < COST_OPEN.length) {
            NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC, -1, "Bạn muốn đổi Nội Tại khác\nvới giá là "
                    + COST_OPEN[player.playerIntrinsic.countOpen] + " Tr vàng ?", "Mở\nNội Tại", "Từ chối");
        } else {
            Service.gI().sendThongBao(player, "Bạn hãy chuyển qua mở VIP");
        }
    }

    public void showConfirmOpenVip(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC_VIP, -1,
                "Bạn có muốn mở Nội Tại\nvới giá là 100 ngọc và\ntái lập giá vàng quay lại ban đầu không?", "Mở\nNội VIP", "Từ chối");
    }

    // 1. Thay thế hàm changeIntrinsic cũ
    private void changeIntrinsic(Player player) {
        List<Intrinsic> listIntrinsic = getIntrinsics(player.gender);
        player.playerIntrinsic.intrinsic = new Intrinsic(listIntrinsic.get(Util.nextInt(1, listIntrinsic.size() - 1)));

        // Gọi hàm tính chỉ số phân tầng mới
        player.playerIntrinsic.intrinsic.param1 = getParamThreeTiers(
                player.playerIntrinsic.intrinsic.paramFrom1,
                player.playerIntrinsic.intrinsic.paramTo1
        );

        player.playerIntrinsic.intrinsic.param2 = getParamThreeTiers(
                player.playerIntrinsic.intrinsic.paramFrom2,
                player.playerIntrinsic.intrinsic.paramTo2
        );

        Service.gI().sendThongBao(player, "Bạn nhận được Nội tại:\n" + player.playerIntrinsic.intrinsic.getName().substring(0, player.playerIntrinsic.intrinsic.getName().indexOf(" [")));
        sendInfoIntrinsic(player);
    }

    // Thay thế hàm cũ, giữ nguyên logic chia dải số, thay đổi logic tỉ lệ
    private short getParamThreeTiers(int min, int max) {
        if (min >= max) return (short) min;

        int range = max - min;

        // --- BƯỚC 1: GIỮ NGUYÊN CÁCH CHIA DẢI GIÁ TRỊ ---
        // VIP: Top 20% cuối
        int thresholdVip = min + (int)(range * 0.8);
        // Khá: 20% kế tiếp
        int thresholdMedium = min + (int)(range * 0.6);

        // Xử lý an toàn
        if (thresholdVip >= max) thresholdVip = max - 1;
        if (thresholdMedium >= thresholdVip) thresholdMedium = thresholdVip - 1;
        if (thresholdMedium < min) thresholdMedium = min;


        // --- BƯỚC 2: TỈ LỆ SIÊU KHÓ (HỆ 1000) ---
        // Thay vì nextInt(100), ta dùng nextInt(1000) để chia nhỏ tỉ lệ
        int chance = Util.nextInt(1000); // Ra số từ 0 đến 999

        if (chance < 5) {
            // [0.5% Tỉ lệ VIP] (chance là 0,1,2,3,4)
            // Cực hiếm: Ra số trong dải Top 20%
            return (short) Util.nextInt(thresholdVip, max + 1);

        } else if (chance < 55) { // 5 + 50 = 55
            // [5% Tỉ lệ Khá] (chance từ 5 đến 54 -> 50 số)
            // Hiếm: Ra số trong dải Khá
            return (short) Util.nextInt(thresholdMedium, thresholdVip + 1);

        } else {
            // [94.5% Tỉ lệ Thường]
            // Rất phổ biến: Ra số cùi bắp
            return (short) Util.nextInt(min, thresholdMedium + 1);
        }
    }

    public void open(Player player) {
        if (player.nPoint.power >= 10000000000L) {
            int goldRequire = COST_OPEN[player.playerIntrinsic.countOpen] * 1000000;
            if (player.inventory.gold >= goldRequire) {
                player.inventory.gold -= goldRequire;
                PlayerService.gI().sendInfoHpMpMoney(player);
                changeIntrinsic(player);
                player.playerIntrinsic.countOpen++;
            } else {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                        + Util.powerToString(goldRequire - player.inventory.gold) + " vàng nữa");
            }
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu sức mạnh tối thiểu 10 tỷ");
        }
    }

    public void openVip(Player player) {
        if (player.nPoint.power >= 10000000000L) {
            int gemRequire = 100;
            if (player.inventory.gem >= 100) {
                player.inventory.gem -= gemRequire;
                PlayerService.gI().sendInfoHpMpMoney(player);
                changeIntrinsic(player);
                player.playerIntrinsic.countOpen = 0;
            } else {
                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc, còn thiếu "
                        + (gemRequire - player.inventory.gem) + " ngọc nữa");
            }
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu sức mạnh tối thiểu 10 tỷ");
        }
    }

}

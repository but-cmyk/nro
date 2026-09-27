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

        String intrinsicName = player.playerIntrinsic.intrinsic.getName();
        int indexBracket = intrinsicName.indexOf(" [");
        if (indexBracket != -1) {
            intrinsicName = intrinsicName.substring(0, indexBracket);
        }
        Service.gI().sendThongBao(player, "Bạn nhận được Nội tại:\n" + intrinsicName);
        sendInfoIntrinsic(player);
    }

    // Phân tầng dải giá trị và tỉ lệ mở nội tại:
    // - Cực khó ra (2%): rơi vào Top 25% dải chỉ số cao nhất [thresholdVip, max] (ví dụ đấm: 20-25%)
    // - Khó ra (13%): rơi vào 25% dải kế tiếp [thresholdMedium, thresholdVip - 1] (ví dụ đấm: 15-19%)
    // - Bình thường (85%): rơi vào 50% dải còn lại [min, thresholdMedium - 1] (ví dụ đấm: 5-14%)
    private short getParamThreeTiers(int min, int max) {
        if (min >= max) {
            return (short) min;
        }

        int range = max - min;

        // Phân tầng dải giá trị:
        // Top 25% cuối (VIP - Cực khó)
        int thresholdVip = min + (int) Math.round(range * 0.75);
        // 25% kế tiếp (Khá - Khó)
        int thresholdMedium = min + (int) Math.round(range * 0.50);

        // Xử lý an toàn tránh tràn dải
        if (thresholdVip > max) {
            thresholdVip = max;
        }
        if (thresholdMedium >= thresholdVip) {
            thresholdMedium = thresholdVip - 1;
        }
        if (thresholdMedium < min) {
            thresholdMedium = min;
        }

        // Tỉ lệ xác suất mở ra (hệ 1000):
        // 2% Cực khó ra (0..19), 13% Khó ra (20..149), 85% Bình thường (150..999)
        int chance = Util.nextInt(1000); // 0 đến 999

        if (chance < 20) {
            // [2% Tỉ lệ VIP - Cực khó ra]
            return (short) Util.nextInt(thresholdVip, max);
        } else if (chance < 150) { // 20 + 130 = 150 -> 13%
            // [13% Tỉ lệ Khá - Khó ra]
            int upper = thresholdVip - 1;
            if (upper < thresholdMedium) {
                upper = thresholdMedium;
            }
            return (short) Util.nextInt(thresholdMedium, upper);
        } else {
            // [85% Tỉ lệ Thường - Bình thường]
            int upper = thresholdMedium - 1;
            if (upper < min) {
                upper = min;
            }
            return (short) Util.nextInt(min, upper);
        }
    }

    public void open(Player player) {
        if (player.playerIntrinsic.countOpen < 0 || player.playerIntrinsic.countOpen >= COST_OPEN.length) {
            Service.gI().sendThongBao(player, "Bạn hãy chuyển qua mở VIP");
            return;
        }
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

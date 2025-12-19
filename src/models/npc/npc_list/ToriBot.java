//package models.npc.npc_list;
//
//import consts.ConstNpc;
//import models.npc.Npc;
//import models.player.Player;
//import services.func.UseItem;
//
//public class ToriBot extends Npc {
//
//    public ToriBot(int mapId, int status, int cx, int cy, int tempId, int avartar) {
//        super(mapId, status, cx, cy, tempId, avartar);
//    }
//
//    @Override
//    public void openBaseMenu(Player player) {
//        if (canOpenNpc(player)) {
//            createOtherMenu(player, ConstNpc.BASE_MENU,
//                    "Nếu bạn đã nạp đủ số tiền tương ứng\nsẽ nhận được phần quà giá trị.",
//                    "VIP 1", "VIP 2", "VIP 3", "Đóng");
//        }
//    }
//
//    @Override
//    public void confirmMenu(Player player, int select) {
//        if (canOpenNpc(player)) {
//            if (this.mapId == 0 || this.mapId == 7 || this.mapId == 14) {
//                if (player.idMark.isBaseMenu()) {
//                    switch (select) {
//                        case 0 ->
//                            createOtherMenu(player, 2,
//                                    "Nếu đã nạp 100k\nbạn sẽ nhận được"
//                                    + "\nDanh hiệu VIP 03 5% chỉ số hp ki sd vv"
//                                    + "\nx20 sao pha lê tnsm"
//                                    + "\nCải trang Karin Kid Lân 15 ngày"
//                                    + "\nx3 hộp mù bé ba"
//                                    + "\nVán bay mây mưa 15 ngày.",
//                                    "Nhận", "Đóng");
//                        case 1 ->
//                            createOtherMenu(player, 4,
//                                    "Nếu đã nạp 200k\nbạn sẽ nhận được"
//                                    + "\nDanh hiệu VIP 02 8% chỉ số hp ki sd vv"
//                                    + "\nx10 đá bảo vệ"
//                                    + "\nbán bay mây mưa vv"
//                                    + "\nCT Karin múa Lân vv"
//                                    + "\nlính bảo vệ tròn vv"
//                                    + "\nx10 mảnh đội trưởng vàng"
//                                    + "\nx5 hộp mù bé ba",
//                                    "Nhận", "Đóng");
//                        case 2 ->
//                            createOtherMenu(player, 6,
//                                    "Nếu đã nạp 500k\nbạn sẽ nhận được"
//                                    + "\nDanh hiệu VIP 01 10% chỉ số hp ki sd vv"
//                                    + "\nx30 đá bảo vệ"
//                                    + "\nbán bay máy bay 41 vv"
//                                    + "\nCT Bunma Rider vv"
//                                    + "\nCánh thiên thần ác quỷ vv"
//                                    + "\npet cá mập vv"
//                                    + "\nx20 mảnh đội trưởng vàng"
//                                    + "\nx10 hộp mù bé ba",
//                                    "Nhận", "Đóng");
//
//                    }
//                } else if (player.idMark.getIndexMenu() == 2) {
//                    switch (select) {
//                        case 0 -> {
//                            UseItem.gI().ComfirmNhanVIP(player, select == 0);
//                        }
//                    }
//                } else if (player.idMark.getIndexMenu() == 4) {
//                    switch (select) {
//                        case 0 -> {
//                            UseItem.gI().ComfirmNhanVIP2(player, select == 0);
//                        }
//                    }
//                } else if (player.idMark.getIndexMenu() == 6) {
//                    switch (select) {
//                        case 0 -> {
//                            UseItem.gI().ComfirmNhanVIP3(player, select == 0);
//                        }
//                    }
//                }
//
//            }
//        }
//    }
//}

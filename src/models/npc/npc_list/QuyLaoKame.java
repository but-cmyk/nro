package models.npc.npc_list;

import models.clan.Clan;
import consts.ConstNpc;
import models.item.Item;
import java.util.ArrayList;
import models.phoban.TreasureUnderSea;
import services.phoban.TreasureUnderSeaService;
import models.npc.Npc;
import static models.npc.NpcFactory.PLAYERID_OBJECT;
import models.player.Player;
import server.Manager;
import services.ShopService;
import services.TaskService;
import services.map.ChangeMapService;
import services.func.Input;
import services.player.InventoryService;
import services.map.NpcService;
import services.RewardService;
import services.Service;
import utils.Util;

public class QuyLaoKame extends Npc {

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
            if (!player.canReward) {
                menu.add("Nói\nchuyện");
                if (ruacon != null && ruacon.quantity >= 1) {
                    menu.add("Giao\n Bé na");
                }
            } else {
                menu.add("Giao\n Bé na");
            }
            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward) {
                RewardService.gI().rewardLancon(player);
                return;
            }
            switch (player.idMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                            ArrayList<String> menu = new ArrayList<>();
                            menu.add("Nhiệm vụ");
                            menu.add("Học\nKỹ năng");
                            Clan clan = player.clan;
                            if (clan != null) {
                                menu.add("Về khu\nvực bang");
                                if (clan.isLeader(player)) {
                                    menu.add("Giải tán\nBang hội");
                                }
                            }
                            menu.add("Kho báu\ndưới biển");
                            String[] menus = menu.toArray(String[]::new);

                            this.createOtherMenu(player, 0,
                                    "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menus);
                        }
                    }
                }
                case 0 -> {
                    switch (select) {
                        case 0 ->
                                NpcService.gI().createTutorial(player, tempId, avartar, player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                        case 1 ->
                                ShopService.gI().opendShop(player, "HOC_KY_NANG", false);
                        case 2 -> {
                            Clan clan = player.clan;
                            if (clan != null && select == 2) {
                                ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
                            } else {
                                // Menu Kho báu dưới biển
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                                    + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 3 -> {
                            boolean clanCheck = true;
                            Clan clan = player.clan;
                            if (clan != null) {
                                clanCheck = false;
                                if (clan.isLeader(player)) {
                                    createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                                } else {
                                    clanCheck = true;
                                }
                            }
                            if (clanCheck) {
                                // Menu Kho báu dưới biển (nếu index bị lệch do không có clan)
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                                    + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 4 -> {
                            // Menu Kho báu dưới biển (nếu là Leader có nút giải tán)
                            if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                        "Bang hội con đang ở hang kho báu cấp "
                                                + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                        "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                        "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                        "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                            }
                        }
                    }
                }
                case 3 -> {
                    Clan clan = player.clan;
                    if (clan != null) {
                        if (clan.isLeader(player)) {
                            if (select == 0) {
                                Input.gI().createFormGiaiTanBangHoi(player);
                            }
                        }
                    }
                }

                // --- XỬ LÝ MENU KHO BÁU DƯỚI BIỂN ---
                case ConstNpc.MENU_OPENED_DBKB, ConstNpc.MENU_OPEN_DBKB -> {
                    switch (select) {
                        case 0: // Top Bang hội
                            Service.gI().showListTop(player, Manager.topBDKB);
                            break;
                        case 1: // Thành tích Bang
                            if (player.clan != null) {
                                String thanhTich = "Thành tích bang " + player.clan.name + ":\n"
                                        + "Cấp độ cao nhất: " + player.clan.bdkb_level + "\n"
                                        + "Thời gian: " + Util.getFormatTime(player.clan.bdkb_time);
                                Service.gI().sendThongBao(player, thanhTich);
                            } else {
                                Service.gI().sendThongBao(player, "Con chưa vào bang hội");
                            }
                            break;
                        case 2: // Chọn cấp độ / Vào map
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            // Nếu đang ở MENU_OPENED_DBKB -> Vào luôn
                            if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPENED_DBKB) {
                                ChangeMapService.gI().goToDBKB(player);
                            }
                            // Nếu đang ở MENU_OPEN_DBKB -> Chọn cấp độ
                            else {
                                if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                    Input.gI().createFormChooseLevelBDKB(player);
                                } else {
                                    this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                            + Util.powerToString(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                                }
                            }
                            break;
                    }
                    break;
                }

                case ConstNpc.MENU_ACCEPT_GO_TO_BDKB -> {
                    switch (select) {
                        case 0 ->
                                TreasureUnderSeaService.gI().openBanDoKhoBau(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                    }
                }
            }
        }
    }
}
package services;

import consts.ConstNpc;
import models.player.Player;
import server.Client;
import network.io.Message;
import services.func.MuaCtService;
import services.map.NpcService;
import utils.Util;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class SubMenuService {

    public static final int BAN = 500;
    public static final int BUFF_PET = 501;
    public static final int OTT = 502;
    public static final int CUU_SAT = 503;
    public static final int MENU = 504;

    public static final int VIEW_AND_BUY = 506;

    private static volatile SubMenuService instance;

    private SubMenuService() {
    }

    public static SubMenuService gI() {
        if (instance == null) {
            synchronized (SubMenuService.class) {
                if (instance == null) {
                    instance = new SubMenuService();
                }
            }
        }
        return instance;
    }

    public void controller(Player player, int playerTarget, int menuId) {
        Player plTarget = Client.gI().getPlayer(playerTarget);

        try {
            switch (menuId) {
                case VIEW_AND_BUY -> {
                    MuaCtService.gI().openPurchaseConfirmationMenu(player, playerTarget);
                    Service.gI().hideWaitDialog(player);
                }
                case MENU -> {
                    if (plTarget != null) {
                        String[] selects = {"Kéo , Búa , Bao", "Hủy"};
                        NpcService.gI().createMenuConMeo(
                                player, ConstNpc.SUB_MENU, -1,
                                "|0|Ngọc Rồng Online\n" + plTarget.name + " (sức mạnh " + Util.powerToString(plTarget.nPoint.power) + ")",
                                selects, plTarget
                        );
                    }
                }
                case BAN -> {
                    if (plTarget != null) {
                        String[] selects = {"Đồng ý", "Hủy"};
                        NpcService.gI().createMenuConMeo(
                                player, ConstNpc.BAN_PLAYER, -1,
                                "Bạn có chắc chắn muốn ban " + plTarget.name, selects, plTarget
                        );
                    }
                }
                case BUFF_PET -> {
                    if (plTarget != null) {
                        String[] selects = {"Đồng ý", "Hủy"};
                        NpcService.gI().createMenuConMeo(
                                player, ConstNpc.BUFF_PET, -1,
                                "Bạn có chắc chắn muốn phát đệ tử cho " + plTarget.name, selects, plTarget
                        );
                    }
                }
                case OTT -> handleOTT(player, plTarget);
                default -> {
                }
            }
        } catch (Exception e) {
            Logger.logException(SubMenuService.class, e);
        } finally {
            Service.gI().hideWaitDialog(player);
        }
    }

    private void handleOTT(Player player, Player plTarget) {
        if (plTarget == null) return;

        if (plTarget.isBoss) {
            String[] selects = {"Kéo", "Búa", "Bao", "Hủy"};
            NpcService.gI().createMenuConMeo(player, ConstNpc.IGNORE_MENU, -1,
                    "Chơi oẳn tù tì với " + plTarget.name + " mức cược 5tr.", selects);
            return;
        }

        if (!plTarget.getSession().actived) {
            Service.gI().sendThongBao(player, plTarget.name + " chưa kích hoạt tài khoản!");
            return;
        }
        if (!player.getSession().actived) {
            Service.gI().sendThongBao(player, "Bạn chưa kích hoạt tài khoản!");
            return;
        }
        if (plTarget.inventory.gold < 5_000_000) {
            Service.gI().sendThongBao(player, plTarget.name + " không có đủ 5tr vàng.");
            return;
        }
        if (player.inventory.gold < 5_000_000) {
            Service.gI().sendThongBao(player, "Bạn không có đủ 5tr vàng.");
            return;
        }

        String[] selects = {"Kéo", "Búa", "Bao", "Hủy"};
        NpcService.gI().createMenuConMeo(player, ConstNpc.OTT, -1,
                "Chơi oẳn tù tì với " + plTarget.name + " mức cược 5tr.", selects, plTarget);
    }

    public void showMenu(Player player) {
        List<SubMenu> subMenusList = new ArrayList<>();
        subMenusList.add(new SubMenu(VIEW_AND_BUY, "Xem Cải Trang", "Mua với giá rẻ hơn"));
        subMenusList.add(new SubMenu(MENU, "Giao lưu tí", "Chơi Kéo , Búa , Bao..."));

        showSubMenu(player, subMenusList.toArray(new SubMenu[0]));
    }

    public void showSubMenu(Player player, SubMenu... subMenus) {
        try {
            Message msg = Service.gI().messageSubCommand((byte) 63);
            msg.writer().writeByte(subMenus.length);
            for (SubMenu subMenu : subMenus) {
                msg.writer().writeUTF(subMenu.caption1());
                msg.writer().writeUTF(subMenu.caption2());
                msg.writer().writeShort((short) subMenu.id());
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(SubMenuService.class, e);
        }
    }

    public record SubMenu(int id, String caption1, String caption2) {
    }
}

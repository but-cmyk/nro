package models.combine.list;
import consts.ConstNpc;
import models.item.Item;
import services.CombineService;
import utils.CombineUtil;
import models.player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

public class EpSaoTrangBi {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineUtil.isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (CombineUtil.isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0; //sao pha lê đã ép
            int starEmpty = 0; //lỗ sao pha lê
            int starvip = 0; //lỗ sao pha lê
            if (trangBi != null && daPhaLe != null) {
                for (Item.ItemOption io : trangBi.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 102 -> star = io.param;
                        case 107 -> starEmpty = io.param;
                        case 230 -> starvip = io.param;
                        default -> {
                        }
                    }
                }
                if (star < starEmpty) {
                    if (starEmpty < 7 || star < 7 || starvip == 1) {
                        player.combineNew.gemCombine = CombineUtil.getGemEpSao();
                        String npcSay = trangBi.template.name + "\n|2|";
                        for (Item.ItemOption io : trangBi.itemOptions) {
                            if (io.optionTemplate.id != 102) {
                                npcSay += io.getOptionString() + "\n";
                            }
                        }
                        if (daPhaLe.template.type == 30) {
                            for (Item.ItemOption io : daPhaLe.itemOptions) {
                                npcSay += "|7|" + io.getOptionString() + "\n";
                            }
                        } else {
                            npcSay += "|7|" + ItemService.gI().getItemOptionTemplate(CombineUtil.getOptionDaPhaLe(daPhaLe)).name.replaceAll("#", CombineUtil.getParamDaPhaLe(daPhaLe) + "") + "\n";
                        }

                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                "Nâng cấp");
                    } else {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Vật Phẩm Đã Tới Giới Hạn Vui Lòng Cường Hóa Lỗ Vật Phẩm", "Đóng");
                    }
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
        }
    }

    public static void epSaoTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineUtil.isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (CombineUtil.isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0; //sao pha lê đã ép
            int starEmpty = 0; //lỗ sao pha lê
            if (trangBi != null && daPhaLe != null) {
                Item.ItemOption optionStar = null;
                for (Item.ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 102) {
                        star = io.param;
                        optionStar = io;
                    } else if (io.optionTemplate.id == 107) {
                        starEmpty = io.param;
                    }
                }
                if (star < starEmpty) {
                    player.inventory.gem -= gem;
                    int optionId = CombineUtil.getOptionDaPhaLe(daPhaLe);
                    int param = CombineUtil.getParamDaPhaLe(daPhaLe);
                    Item.ItemOption option = null;
                    for (Item.ItemOption io : trangBi.itemOptions) {
                        if (io.optionTemplate.id == optionId) {
                            option = io;
                            break;
                        }
                    }
                    if (option != null) {
                        option.param += param;
                    } else {
                        trangBi.itemOptions.add(new Item.ItemOption(optionId, param));
                    }
                    if (optionStar != null) {
                        optionStar.param++;
                    } else {
                        trangBi.itemOptions.add(new Item.ItemOption(102, 1));
                    }

                    InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);
                    CombineService.gI().sendEffectSuccessCombine(player);
                }
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }
}

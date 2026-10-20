package models.combine.list;
import consts.ConstNpc;
import models.Template.ItemOptionTemplate;
import models.item.Item;
import services.CombineService;
import utils.CombineUtil;
import models.player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;

public class EpSaoTrangBi {

    public static void showInfoCombine(Player player) {
        if (player == null || player.combineNew == null || player.combineNew.itemsCombine == null) {
            return;
        }
        if (player.combineNew.itemsCombine.size() == 2) {
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item != null && item.isNotNullItem()) {
                    if (CombineUtil.isTrangBiPhaLeHoa(item)) {
                        trangBi = item;
                    } else if (CombineUtil.isDaPhaLe(item)) {
                        daPhaLe = item;
                    }
                }
            }
            int star = 0; // sao pha lê đã ép
            int starEmpty = 0; // lỗ sao pha lê
            int starvip = 0; // lỗ sao pha lê vip
            if (trangBi != null && daPhaLe != null && trangBi.itemOptions != null) {
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
                        StringBuilder npcSay = new StringBuilder(trangBi.template.name).append("\n|2|");
                        for (Item.ItemOption io : trangBi.itemOptions) {
                            if (io.optionTemplate.id != 102) {
                                npcSay.append(io.getOptionString()).append("\n");
                            }
                        }
                        if (daPhaLe.template.type == 30) {
                            if (daPhaLe.itemOptions != null) {
                                for (Item.ItemOption io : daPhaLe.itemOptions) {
                                    npcSay.append("|7|").append(io.getOptionString()).append("\n");
                                }
                            }
                        } else {
                            int optId = CombineUtil.getOptionDaPhaLe(daPhaLe);
                            ItemOptionTemplate optTemp = optId != -1 ? ItemService.gI().getItemOptionTemplate(optId) : null;
                            if (optTemp != null) {
                                npcSay.append("|7|").append(optTemp.name.replaceAll("#", CombineUtil.getParamDaPhaLe(daPhaLe) + "")).append("\n");
                            }
                        }

                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
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
        if (player == null || player.combineNew == null || player.combineNew.itemsCombine == null || player.inventory == null) {
            return;
        }
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendThongBaoResetWait(player, "Nguyên liệu không hợp lệ!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        // --- BƯỚC 1: VALIDATE (Kiểm tra đầy đủ tính hợp lệ trước khi thao tác tài sản) ---
        Item trangBi = null;
        Item daPhaLe = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem()) {
                if (CombineUtil.isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (CombineUtil.isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
        }

        if (trangBi == null || daPhaLe == null || trangBi.itemOptions == null || daPhaLe.quantity < 1) {
            Service.gI().sendThongBaoResetWait(player, "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        // Kiểm tra option đá pha lê
        if (daPhaLe.template.type == 30 && (daPhaLe.itemOptions == null || daPhaLe.itemOptions.isEmpty())) {
            Service.gI().sendThongBaoResetWait(player, "Đá pha lê không có chỉ số hợp lệ!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        int optionId = CombineUtil.getOptionDaPhaLe(daPhaLe);
        int param = CombineUtil.getParamDaPhaLe(daPhaLe);
        if (optionId == -1 || param <= 0) {
            Service.gI().sendThongBaoResetWait(player, "Đá pha lê không hợp lệ!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        // Kiểm tra số sao hiện tại và lỗ sao
        int star = 0; // sao pha lê đã ép (option 102)
        int starEmpty = 0; // lỗ sao pha lê (option 107)
        int starvip = 0; // cường hóa lỗ sao (option 230)
        Item.ItemOption optionStar = null;
        for (Item.ItemOption io : trangBi.itemOptions) {
            if (io.optionTemplate.id == 102) {
                star = io.param;
                optionStar = io;
            } else if (io.optionTemplate.id == 107) {
                starEmpty = io.param;
            } else if (io.optionTemplate.id == 230) {
                starvip = io.param;
            }
        }

        if (star >= starEmpty) {
            Service.gI().sendThongBaoResetWait(player, "Trang bị không còn lỗ sao trống để ép!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        if (star >= 7 && starvip != 1 && starEmpty >= 7) {
            Service.gI().sendThongBaoResetWait(player, "Vật phẩm đã tới giới hạn, vui lòng cường hóa lỗ vật phẩm!");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        int gem = player.combineNew.gemCombine > 0 ? player.combineNew.gemCombine : CombineUtil.getGemEpSao();
        if (!player.inventory.checkGemAndRuby(gem)) {
            Service.gI().sendThongBaoResetWait(player, "Không đủ ngọc để thực hiện");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        // --- BƯỚC 2: MUTATE (Chỉ thực thi khi 100% validate thành công) ---
        player.inventory.subGemAndRuby(gem);
        InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);

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

        // --- BƯỚC 3: NOTIFY (Phản hồi kết quả cho Client) ---
        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}

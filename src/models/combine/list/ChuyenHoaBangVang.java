package models.combine.list;

import consts.ConstNpc;
import models.item.Item;
import models.item.Item.ItemOption;
import models.player.Player;
import services.CombineService;
import services.ItemService;
import services.Service;
import services.player.InventoryService;

public class ChuyenHoaBangVang {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item trangBiGoc = player.combineNew.itemsCombine.get(0);
            Item trangBiCanChuyenHoa = player.combineNew.itemsCombine.get(1);

            int levelTrangBi = 0;
            int soLanRotCap = 0;
            int khongthechuyenhoa = 0;

            for (ItemOption io : trangBiGoc.itemOptions) {
                if (io.optionTemplate.id == 72) {
                    levelTrangBi = io.param - 1;
                } else if (io.optionTemplate.id == 230) {
                    soLanRotCap += io.param;
                } else if (io.optionTemplate.id == 30) {
                    khongthechuyenhoa = 1;
                }
            }

            int chisogoc = trangBiCanChuyenHoa.itemOptions.get(0).param;
            for (int i = 0; i < levelTrangBi; i++) {
                chisogoc += chisogoc * 0.1;
            }
            chisogoc -= chisogoc * (soLanRotCap * 0.1);

            boolean trangBi_daNangCap_daPhaLeHoa = trangBiCanChuyenHoa.itemOptions.stream()
                    .anyMatch(io -> io.optionTemplate.id == 72 || io.optionTemplate.id == 102);

            if (khongthechuyenhoa == 1) {
                Service.gI().sendThongBaoOK(player, "Không thể chuyển hóa đồ không giao dịch");
            } else if (!CombineService.gI().isTrangBiGoc(trangBiGoc)) {
                Service.gI().sendThongBaoOK(player, "Trang bị gốc phải từ bậc lưỡng long, Jean hoặc Zelot");
            } else if (levelTrangBi < 3) {
                Service.gI().sendThongBaoOK(player, "Trang bị gốc phải từ [+4] trở lên");
            } else if (!CombineService.gI().isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                Service.gI().sendThongBaoOK(player, "Trang bị chuyển hóa phải là đồ thần linh");
            } else if (trangBi_daNangCap_daPhaLeHoa) {
                Service.gI().sendThongBaoOK(player, "Trang bị chuyển hóa phải chưa nâng cấp và pha lê hóa trang bị");
            } else if (!CombineService.gI().isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                Service.gI().sendThongBaoOK(player, "Trang bị gốc và Trang bị chuyển hóa phải cùng loại và hành tinh");
            } else {
                StringBuilder NpcSay = new StringBuilder("|2|Hiện tại " + trangBiCanChuyenHoa.getName() + "\n");
                for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                    if (io.optionTemplate.id != 72) {
                        NpcSay.append("|0|").append(io.getOptionString()).append("\n");
                    }
                }

                NpcSay.append("|2|Sau khi nâng cấp (+").append(levelTrangBi + 1).append(")\n");
                for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                    if (io.optionTemplate.id != 72) {
                        if (io.optionTemplate.id == 0 || io.optionTemplate.id == 47 || io.optionTemplate.id == 6 ||
                            io.optionTemplate.id == 7 || io.optionTemplate.id == 14 || io.optionTemplate.id == 22 ||
                            io.optionTemplate.id == 23) {
                            NpcSay.append("|1|").append(io.getOptionString(chisogoc)).append("\n");
                        } else {
                            NpcSay.append("|1|").append(io.getOptionString()).append("\n");
                        }
                    }
                }

                for (ItemOption io : trangBiGoc.itemOptions) {
                    if (io.optionTemplate.id != 72 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107 &&
                        io.optionTemplate.id != 0 && io.optionTemplate.id != 47 && io.optionTemplate.id != 6 &&
                        io.optionTemplate.id != 7 && io.optionTemplate.id != 14 && io.optionTemplate.id != 22 &&
                        io.optionTemplate.id != 23) {
                        NpcSay.append(io.getOptionString()).append("\n");
                    }
                }

                NpcSay.append("Chuyển qua tất cả sao pha lê\n");
                NpcSay.append("|2|Cần 2 tỷ vàng");

                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, NpcSay.toString(),
                        "Nâng cấp\n2 tỷ\nvàng", "Từ chối");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị có cấp từ [+4] và 1 trang bị không có cấp nhưng cao hơn 1 bậc");
        }
    }

    public static void ChuyenHoaBangvang(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                Service.gI().sendThongBao(player, "Cần 1 ô trống hành trang");
                return;
            }

            Item trangBiGoc = player.combineNew.itemsCombine.get(0);
            Item trangBiCanChuyenHoa = player.combineNew.itemsCombine.get(1);
            Item trangBiCanChuyenHoa_2 = ItemService.gI().createNewItem(trangBiCanChuyenHoa.template.id);
            long goldChuyenHoa = 2_000_000_000L;

            int levelTrangBi = 0;
            int soLanRotCap = 0;
            int khongchuyenhoa = 0;

            for (ItemOption io : trangBiGoc.itemOptions) {
                if (io.optionTemplate.id == 72) {
                    levelTrangBi = io.param - 1;
                } else if (io.optionTemplate.id == 230) {
                    soLanRotCap += io.param;
                } else if (io.optionTemplate.id == 30) {
                    khongchuyenhoa = 1;
                }
            }

            int chisogoc = trangBiCanChuyenHoa.itemOptions.get(0).param;
            for (int i = 0; i < levelTrangBi; i++) {
                chisogoc += chisogoc * 0.1;
            }
            chisogoc -= chisogoc * (soLanRotCap * 0.1);

            if (player.inventory.gold < goldChuyenHoa) {
                Service.gI().sendThongBao(player, "Không đủ vàng!");
                return;
            }

            if (khongchuyenhoa == 1) {
                Service.gI().sendThongBaoOK(player, "Không thể chuyển hóa vật phẩm không thể giao dịch");
                return;
            } else if (!CombineService.gI().isTrangBiGoc(trangBiGoc)) {
                Service.gI().sendThongBaoOK(player, "Trang bị phải từ bậc lưỡng long, Jean hoặc Zelot trở lên");
                return;
            } else if (levelTrangBi < 3) {
                Service.gI().sendThongBaoOK(player, "Trang bị gốc phải từ [+4] trở lên");
                return;
            } else if (!CombineService.gI().isTrangBiChuyenHoa(trangBiCanChuyenHoa)) {
                Service.gI().sendThongBaoOK(player, "Trang bị chuyển hóa phải là đồ thần linh");
                return;
            } else if (!CombineService.gI().isCheckTrungTypevsGender(trangBiGoc, trangBiCanChuyenHoa)) {
                Service.gI().sendThongBaoOK(player, "Trang bị gốc và chuyển hóa phải cùng loại và hành tinh");
                return;
            }

            trangBiCanChuyenHoa.itemOptions.get(0).param = chisogoc;
            for (int i = 1; i < trangBiGoc.itemOptions.size(); i++) {
                ItemOption io = trangBiGoc.itemOptions.get(i);
                trangBiCanChuyenHoa.itemOptions.add(new ItemOption(io.optionTemplate.id, io.param));
            }

            for (ItemOption io : trangBiCanChuyenHoa.itemOptions) {
                trangBiCanChuyenHoa_2.itemOptions.add(new ItemOption(io.optionTemplate.id, io.param));
            }

            player.inventory.gold -= goldChuyenHoa;
            Service.gI().sendMoney(player);
            InventoryService.gI().subQuantityItemsBag(player, trangBiGoc, 1);
            InventoryService.gI().subQuantityItemsBag(player, trangBiCanChuyenHoa, 1);
            InventoryService.gI().addItemBag(player, trangBiCanChuyenHoa_2);
            InventoryService.gI().sendItemBags(player);
            CombineService.gI().reOpenItemCombine(player);
            CombineService.gI().sendEffectSuccessCombine(player);
        }
    }
}

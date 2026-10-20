package models.combine.list;
import consts.ConstNpc;
import models.item.Item;
import java.util.Objects;
import services.CombineService;
import static services.CombineService.MAX_LEVEL_ITEM;
import utils.CombineUtil;
import models.player.Player;
import server.ServerNotify;
import services.player.InventoryService;
import services.Service;
import utils.Util;

public class NangCapVatPham {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type < 5).count() < 1) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                return;
            }
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type == 14).count() < 1) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá nâng cấp", "Đóng");
                return;
            }
            if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 987).count() < 1) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                return;
            }
            Item itemDo = null;
            Item itemDNC = null;
            Item itemDBV = null;
            for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.get(j).template.id == 987) {
                        itemDBV = player.combineNew.itemsCombine.get(j);
                        continue;
                    }
                    if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                        itemDo = player.combineNew.itemsCombine.get(j);
                    } else {
                        itemDNC = player.combineNew.itemsCombine.get(j);
                    }
                }
            }
            if (CombineUtil.isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                int level = 0;
                for (Item.ItemOption io : itemDo.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        level = io.param;
                        break;
                    }
                }
                if (level < CombineService.MAX_LEVEL_ITEM) {
                    player.combineNew.goldCombine = CombineUtil.getGoldNangCapDo(level);
                    player.combineNew.ratioCombine = (float) CombineUtil.getTileNangCapDo(level);
                    player.combineNew.countDaNangCap = CombineUtil.getCountDaNangCapDo(level);
                    player.combineNew.countDaBaoVe = (short) CombineUtil.getCountDaBaoVe(level);
                    player.combineNew.gemCombine = CombineUtil.getGemNangCapDo(level);
                    String npcSay = "|2|Hiện tại " + itemDo.template.name + " (+" + level + ")\n|0|";
                    for (Item.ItemOption io : itemDo.itemOptions) {
                        if (io.optionTemplate.id != 72) {
                            npcSay += io.getOptionString() + "\n";
                        }
                    }
                    String option = null;
                    int param = 0;
                    for (Item.ItemOption io : itemDo.itemOptions) {
                        if (io.optionTemplate.id == 47
                                || io.optionTemplate.id == 6
                                || io.optionTemplate.id == 0
                                || io.optionTemplate.id == 7
                                || io.optionTemplate.id == 14
                                || io.optionTemplate.id == 22
                                || io.optionTemplate.id == 23) {
                            option = io.optionTemplate.name;
                            param = io.param + (io.param * 10 / 100);
                            break;
                        }
                    }
                    npcSay += "|2|Sau khi nâng cấp (+" + (level + 1) + ")\n|7|"
                            + option.replaceAll("#", String.valueOf(param))
                            + "\n|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n"
                            + (player.combineNew.countDaNangCap > itemDNC.quantity ? "|7|" : "|1|")
                            + "Cần " + player.combineNew.countDaNangCap + " " + itemDNC.template.name
                            + "\n" + (player.combineNew.goldCombine > player.inventory.gold ? "|7|" : "|1|")
                            + "Cần " + Util.powerToString(player.combineNew.goldCombine) + " vàng";

                    if (player.combineNew.gemCombine > 0) {
                        boolean isEnoughGem = player.inventory.checkGemAndRuby(player.combineNew.gemCombine);
                        npcSay += "\n" + (isEnoughGem ? "|1|" : "|7|")
                                + "Cần " + player.combineNew.gemCombine + " ngọc (ưu tiên hồng ngọc)";
                    }

                    String daNPC = player.combineNew.itemsCombine.size() == 3 && itemDBV != null ? String.format("\nCần tốn %s đá bảo vệ", player.combineNew.countDaBaoVe) : "";

                    if (player.combineNew.countDaNangCap > itemDNC.quantity) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                npcSay, "Còn thiếu\n" + (player.combineNew.countDaNangCap - itemDNC.quantity) + " " + itemDNC.template.name);
                    } else if (player.combineNew.goldCombine > player.inventory.gold) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                npcSay, "Còn thiếu\n" + Util.powerToString((player.combineNew.goldCombine - player.inventory.gold)) + " vàng");
                    } else if (player.combineNew.gemCombine > 0 && !player.inventory.checkGemAndRuby(player.combineNew.gemCombine)) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                npcSay, "Còn thiếu\n" + (player.combineNew.gemCombine - player.inventory.getGemAndRuby()) + " ngọc");
                    } else if (player.combineNew.itemsCombine.size() == 3 && Objects.nonNull(itemDBV) && itemDBV.quantity < player.combineNew.countDaBaoVe) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                npcSay, "Còn thiếu\n" + (player.combineNew.countDaBaoVe - itemDBV.quantity) + " đá bảo vệ");
                    } else {
                        long maxTimesGold = player.combineNew.goldCombine > 0 ? (player.inventory.gold / player.combineNew.goldCombine) : 999;
                        long maxTimesGem = player.combineNew.gemCombine > 0 ? (player.inventory.getGemAndRuby() / player.combineNew.gemCombine) : 999;
                        long maxTimesDNC = player.combineNew.countDaNangCap > 0 ? (itemDNC.quantity / player.combineNew.countDaNangCap) : 999;
                        long maxTimesDBV = (player.combineNew.itemsCombine.size() == 3 && itemDBV != null && player.combineNew.countDaBaoVe > 0)
                                ? (itemDBV.quantity / player.combineNew.countDaBaoVe)
                                : 999;
                        long maxTimes = Math.min(Math.min(maxTimesGold, maxTimesGem), Math.min(maxTimesDNC, maxTimesDBV));

                        java.util.List<String> menuSelects = new java.util.ArrayList<>();
                        player.combineNew.quantities.clear();

                        menuSelects.add("Nâng cấp");
                        player.combineNew.quantities.add(1);

                        if (maxTimes >= 5) {
                            menuSelects.add("Nâng cấp\n5 lần");
                            player.combineNew.quantities.add(5);
                        }
                        if (maxTimes >= 10) {
                            menuSelects.add("Nâng cấp\n10 lần");
                            player.combineNew.quantities.add(10);
                        }
                        menuSelects.add("Từ chối");

                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                npcSay, menuSelects.toArray(new String[0]));
                    }
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Trang bị của ngươi đã đạt cấp tối đa", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
            }
        } else {
            if (player.combineNew.itemsCombine.size() > 3) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                return;
            }
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
        }
    }

    public static void nangCapVatPham(Player player, int... args) {
        if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type < 5).count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type == 14).count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 987).count() != 1) {
                return; // admin
            }

            Item itemDo = null;
            Item itemDNC = null;
            Item itemDBV = null;
            for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.get(j).template.id == 987) {
                        itemDBV = player.combineNew.itemsCombine.get(j);
                        continue;
                    }
                    if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                        itemDo = player.combineNew.itemsCombine.get(j);
                    } else {
                        itemDNC = player.combineNew.itemsCombine.get(j);
                    }
                }
            }

            if (CombineUtil.isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                int maxAttempts = (args.length > 0 && args[0] > 0) ? args[0] : 1;
                int countDaNangCap = player.combineNew.countDaNangCap;
                int gold = player.combineNew.goldCombine;
                short countDaBaoVe = player.combineNew.countDaBaoVe;

                int level = 0;
                Item.ItemOption optionLevel = null;
                for (Item.ItemOption io : itemDo.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        level = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                int gem = player.combineNew.gemCombine > 0 ? player.combineNew.gemCombine : CombineUtil.getGemNangCapDo(level);

                if (level >= MAX_LEVEL_ITEM) {
                    Service.gI().sendThongBao(player, "Trang bị đã đạt cấp tối đa");
                    return;
                }

                Item.ItemOption option = null;
                Item.ItemOption option2 = null;
                for (Item.ItemOption io : itemDo.itemOptions) {
                    if (io.optionTemplate.id == 47
                            || io.optionTemplate.id == 6
                            || io.optionTemplate.id == 0
                            || io.optionTemplate.id == 7
                            || io.optionTemplate.id == 14
                            || io.optionTemplate.id == 22
                            || io.optionTemplate.id == 23) {
                        option = io;
                    } else if (io.optionTemplate.id == 27
                            || io.optionTemplate.id == 28) {
                        option2 = io;
                    }
                }
                if (option == null) {
                    Service.gI().sendThongBaoResetWait(player, "Trang bị không có chỉ số phù hợp để nâng cấp!");
                    CombineService.gI().reOpenItemCombine(player);
                    return;
                }

                if (player.inventory.gold < gold) {
                    Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                    return;
                }
                if (gem > 0 && !player.inventory.checkGemAndRuby(gem)) {
                    Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                    return;
                }
                if (itemDNC.quantity < countDaNangCap) {
                    Service.gI().sendThongBao(player, "Không đủ đá nâng cấp để thực hiện");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (Objects.isNull(itemDBV) || itemDBV.quantity < countDaBaoVe) {
                        Service.gI().sendThongBao(player, "Không đủ đá bảo vệ để thực hiện");
                        return;
                    }
                }

                int attemptsDone = 0;
                boolean isSuccess = false;

                for (int i = 0; i < maxAttempts; i++) {
                    long neededGold = (long) gold * (attemptsDone + 1);
                    long neededGem = (long) gem * (attemptsDone + 1);
                    long neededDNC = (long) countDaNangCap * (attemptsDone + 1);
                    long neededDBV = (long) countDaBaoVe * (attemptsDone + 1);

                    if (player.inventory.gold < neededGold
                            || (gem > 0 && !player.inventory.checkGemAndRuby(neededGem))
                            || itemDNC.quantity < neededDNC
                            || (player.combineNew.itemsCombine.size() == 3 && (itemDBV == null || itemDBV.quantity < neededDBV))) {
                        if (i > 0) {
                            Service.gI().sendServerMessage(player, "Dừng lại tại lần thứ " + i + " do không đủ tài nguyên.");
                        }
                        break;
                    }

                    attemptsDone++;
                    if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                        isSuccess = true;
                        break;
                    }
                }

                // Trừ tài nguyên một lần
                if (attemptsDone > 0) {
                    player.inventory.subGold((long) gold * attemptsDone);
                    if (gem > 0) {
                        player.inventory.subGemAndRuby((long) gem * attemptsDone);
                    }
                    InventoryService.gI().subQuantityItemsBag(player, itemDNC, countDaNangCap * attemptsDone);
                    if (player.combineNew.itemsCombine.size() == 3 && itemDBV != null) {
                        InventoryService.gI().subQuantityItemsBag(player, itemDBV, countDaBaoVe * attemptsDone);
                    }
                }

                if (isSuccess) {
                    option.param += (option.param * 10 / 100) < 1 ? 1 : (option.param * 10 / 100);
                    if (option2 != null) {
                        option2.param += (option2.param * 10 / 100) < 1 ? 1 : (option2.param * 10 / 100);
                    }
                    if (optionLevel == null) {
                        itemDo.itemOptions.add(new Item.ItemOption(72, 1));
                    } else {
                        optionLevel.param++;
                    }
                    if (optionLevel != null && optionLevel.param >= 5) {
                        ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa nâng cấp "
                                + "thành công " + itemDo.template.name + " lên +" + optionLevel.param);
                    }
                    if (optionLevel != null && optionLevel.param == 7 && player.playerTask != null && player.playerTask.taskdh != null) {
                        if (player.playerTask.taskdh.DapDo < 3) {
                            player.playerTask.taskdh.DapDo++;
                            player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
                        }
                    }
                    CombineService.gI().sendEffectSuccessCombine(player);
                    player.effect.addPointThanhDapDo();

                    if (maxAttempts > 1) {
                        Service.gI().sendServerMessage(player, "Thành công sau " + attemptsDone + " lần thử.");
                    }
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                    if (maxAttempts > 1) {
                        Service.gI().sendServerMessage(player, "Thất bại sau " + attemptsDone + " lần thử.");
                    }
                }

                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }

}

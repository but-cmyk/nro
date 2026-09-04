package services.func.useitem.handlers;

import consts.ConstPlayer;
import models.item.Item;
import models.map.ItemMap;
import models.npc.mabu.MabuEgg;
import models.player.Player;
import services.ItemTimeService;
import services.Service;
import services.func.useitem.ItemActionHandler;
import services.player.InventoryService;
import services.player.PlayerService;
import utils.Util;

/**
 * Xử lý sử dụng các Vật Phẩm Tiêu Hao: Đậu thần, Nho hồi sinh lực, Item Time (Bổ huyết, Cuồng nộ, Giáp xên,...),
 * Tự động luyện tập TDLT, Bình nước Xinbatô, Xương sói, Trứng Mabu.
 */
public class ConsumableItemHandler implements ItemActionHandler {

    @Override
    public boolean canHandle(Player player, Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        if (item.template.type == 6) { // Đậu thần
            return true;
        }
        int id = item.template.id;
        return isItemTimeId(id) || id == 211 || id == 212 || id == 521 || id == 456 || id == 460 || id == 568;
    }

    @Override
    public void handle(Player player, Item item, int bagIndex) {
        if (item.template.type == 6) {
            eatPea(player);
            return;
        }
        int id = item.template.id;
        if (isItemTimeId(id)) {
            useItemTime(player, item);
        } else if (id == 211 || id == 212) {
            eatGrapes(player, item);
        } else if (id == 521) {
            useTDLT(player, item);
        } else if (id == 456) {
            useBinhNuocXinbato(player, item);
        } else if (id == 460) {
            useXuongSoi(player, item);
        } else if (id == 568) {
            useTrungMabu(player, item);
        }
    }

    private static boolean isItemTimeId(int id) {
        return switch (id) {
            case 379, 381, 382, 383, 384, 385, 579, 638, 663, 664, 665, 666, 667,
                 752, 753, 764, 880, 881, 882, 1045, 1099, 1100, 1101, 1102, 1103,
                 1109, 1136, 1137, 1261, 1262, 1397, 2160 -> true;
            default -> false;
        };
    }

    public static void eatPea(Player player) {
        boolean inCombat = (player.typePk != ConstPlayer.NON_PK || player.pvp != null);
        int peaCooldown = inCombat ? 3000 : 1000;
        if (!Util.canDoWithTime(player.lastTimeEatPea, peaCooldown)) {
            return;
        }
        player.lastTimeEatPea = System.currentTimeMillis();
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            int hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (Item.ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc);
            player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc);
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int stamina = 100 * lvPea;
                player.pet.nPoint.stamina += stamina;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc);
                player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc);
                Service.gI().sendInfoPlayerEatPea(player.pet);
                Service.gI().chatJustForMe(player, player.pet, "Cám ơn sư phụ");
            }

            InventoryService.gI().subQuantityItemsBag(player, pea, 1);
            InventoryService.gI().sendItemBags(player);
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percent = (item.template.id == 211) ? 20 : 100;
        if (pl.zone.map.mapId == 21 || pl.zone.map.mapId == 22 || pl.zone.map.mapId == 23) {
            if (percent == 100) {
                pl.nPoint.stamina = pl.nPoint.maxStamina;
            } else {
                pl.nPoint.stamina = (short) Math.min(pl.nPoint.stamina + (pl.nPoint.maxStamina * percent / 100), pl.nPoint.maxStamina);
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            PlayerService.gI().sendCurrentStamina(pl);
            PlayerService.gI().changeAndSendTypePK(pl, ConstPlayer.NON_PK);
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục");
        } else {
            Service.gI().sendThongBao(pl, "Hãy về nhà để sử dụng");
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void useBinhNuocXinbato(Player pl, Item item) {
        boolean hasXinbato = pl.zone.getBosses().stream().anyMatch(b -> b.name.equals("Xinbatô"));
        if (hasXinbato) {
            if (item.quantity >= 1) {
                InventoryService.gI().subQuantityItemsBag(pl, item, 99);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().dropItemMap(pl.zone, new ItemMap(pl.zone, 456, 1, pl.location.x, pl.location.y, pl.id));
                Service.gI().sendThongBao(pl, "Bạn đã đặt bình nước xuống đất, Xinbatô đã chú ý đến nó!");
            } else {
                Service.gI().sendThongBao(pl, "Bạn không đủ 1 bình nước.");
            }
        } else {
            Service.gI().sendThongBao(pl, "Không thể sử dụng, hãy đến nơi có Xinbatô.");
        }
    }

    private void useXuongSoi(Player pl, Item item) {
        if (pl.zone.getBosses().stream().anyMatch(b -> b.name.equals("Sói hẹc quyn"))) {
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().dropItemMap(pl.zone, new ItemMap(pl.zone, 460, 1, pl.location.x, pl.location.y, pl.id));
        } else {
            Service.gI().sendThongBao(pl, "Không thể vứt cục xương, hãy đến nơi có Sói hẹc quyn.");
        }
    }

    private void useTrungMabu(Player pl, Item item) {
        if (pl.mabuEgg == null) {
            MabuEgg.createMabuEgg(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBao(pl, "Con đang có trứng Mabu chưa mở ở nhà rồi.");
        }
    }

    private void useItemTime(Player pl, Item item) {
        long now = System.currentTimeMillis();
        switch (item.template.id) {
            case 1397 -> { pl.itemTime.lastTimeBuaTNSM = now; pl.itemTime.isBuaTNSM = true; }
            case 382 -> { pl.itemTime.lastTimeBoHuyet = now; pl.itemTime.isUseBoHuyet = true; }
            case 383 -> { pl.itemTime.lastTimeBoKhi = now; pl.itemTime.isUseBoKhi = true; }
            case 384 -> { pl.itemTime.lastTimeGiapXen = now; pl.itemTime.isUseGiapXen = true; }
            case 381 -> { pl.itemTime.lastTimeCuongNo = now; pl.itemTime.isUseCuongNo = true; }
            case 385 -> { pl.itemTime.lastTimeAnDanh = now; pl.itemTime.isUseAnDanh = true; }
            case 379 -> { pl.itemTime.lastTimeUseMayDo = now; pl.itemTime.isUseMayDo = true; }
            case 1099 -> { pl.itemTime.lastTimeCuongNo2 = now; pl.itemTime.isUseCuongNo2 = true; }
            case 1100 -> { pl.itemTime.lastTimeBoHuyet2 = now; pl.itemTime.isUseBoHuyet2 = true; }
            case 1101 -> { pl.itemTime.lastTimeBoKhi2 = now; pl.itemTime.isUseBoKhi2 = true; }
            case 1102 -> { pl.itemTime.lastTimeGiapXen2 = now; pl.itemTime.isUseGiapXen2 = true; }
            case 1103 -> { pl.itemTime.lastTimeAnDanh2 = now; pl.itemTime.isUseAnDanh2 = true; }
            case 764 -> { pl.itemTime.lastTimeKhauTrang = now; pl.itemTime.isKhauTrang = true; }
            case 1136 -> { pl.itemTime.lastTimeTnDeTu = now; pl.itemTime.isTnDeTu = true; }
            case 638 -> { pl.itemTime.lastTimeUseCMS = now; pl.itemTime.isUseCMS = true; }
            case 2160 -> { pl.itemTime.lastTimeUseNCD = now; pl.itemTime.isUseNCD = true; }
            case 579, 1045 -> { pl.itemTime.lastTimeUseDK = now; pl.itemTime.isUseDK = true; }
            case 663, 664, 665, 666, 667 -> {
                pl.itemTime.lastTimeEatMeal = now;
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
            }
            case 880, 881, 882 -> {
                pl.itemTime.lastTimeEatMeal2 = now;
                pl.itemTime.isEatMeal2 = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal2);
                pl.itemTime.iconMeal2 = item.template.iconID;
            }
            case 1109 -> { pl.itemTime.lastTimeUseMayDo2 = now; pl.itemTime.isUseMayDo2 = true; }
            case 1137 -> { pl.itemTime.lastTimeUseCo4La = now; pl.itemTime.isUseCo4La = true; }
            case 753 -> { pl.itemTime.banhchunglastTime = now; pl.itemTime.banhchung = true; }
            case 752 -> { pl.itemTime.banhtetlastTime = now; pl.itemTime.banhtet = true; }
            case 1261 -> { pl.itemTime.lastTimeXimuoihoadao = now; pl.itemTime.isXimuoihoadao = true; }
            case 1262 -> { pl.itemTime.lastTimeXimuoihoamai = now; pl.itemTime.isXimuoihoamai = true; }
        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
    }
}

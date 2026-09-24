package services.func.useitem.handlers;

import consts.ConstItem;
import consts.ConstNpc;
import consts.ConstPlayer;
import models.item.Item;
import models.player.Player;
import models.radar.Card;
import models.radar.RadarCard;
import services.ItemService;
import services.PetService;
import services.RadarService;
import services.Service;
import services.func.SummonDragon;
import services.func.useitem.ItemActionHandler;
import services.map.NpcService;
import services.player.InventoryService;
import utils.Util;

/**
 * Xử lý các vật phẩm đặc biệt: Bông tai Porata (454, 921, 1346), Gọi Rồng Thần (Type 12),
 * Thẻ Radar Card (Type 33), Đổi Đệ Tử (401), Mảnh giấy vụn (726), Siêu thần thủy (727, 728).
 */
public class SpecialItemHandler implements ItemActionHandler {

    @Override
    public boolean canHandle(Player player, Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        if (item.template.type == 12 || item.template.type == 33) {
            return true;
        }
        int id = item.template.id;
        return id == 454 || id == 921 || id == 1346 || id == 401 || id == 726 || id == 727 || id == 728;
    }

    @Override
    public void handle(Player player, Item item, int bagIndex) {
        if (item.template.type == 12) {
            controllerCallRongThan(player, item);
            return;
        }
        if (item.template.type == 33) {
            useCard(player, item);
            return;
        }

        switch (item.template.id) {
            case 454 -> usePorata(player);
            case 921 -> usePorata2(player);
            case 1346 -> usePorata3(player);
            case 401 -> changePet(player, item);
            case 726 -> itemManhGiay(player, item);
            case 727, 728 -> itemSieuThanThuy(player, item);
        }
    }

    public static void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    public static void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    public static void usePorata3(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion3(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if ((tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) || tempId == ConstItem.BI_NGO_1_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO, SummonDragon.NGOC_RONG_2_SAO, SummonDragon.NGOC_RONG_3_SAO ->
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13), SummonDragon.DRAGON_SHENRON);
                case ConstItem.BI_NGO_1_SAO ->
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) 702, SummonDragon.DRAGON_BLACK_SHENRON);
                default ->
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON, -1,
                            "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
            }
        } else if (tempId >= 925 && tempId <= 931) {
            Service.gI().sendThongBao(pl, "Sự kiện Rồng Băng đã kết thúc!");
        }
    }

    public static void useCard(Player pl, Item item) {
        RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                .filter(c -> c.Id == item.template.id)
                .findFirst().orElse(null);
        if (radarTemplate == null) {
            return;
        }

        if (radarTemplate.Require != -1) {
            RadarCard radarRequireTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                    .filter(r -> r.Id == radarTemplate.Require)
                    .findFirst().orElse(null);
            if (radarRequireTemplate == null) {
                return;
            }

            Card cardRequire = pl.Cards.stream()
                    .filter(r -> r.Id == radarRequireTemplate.Id)
                    .findFirst().orElse(null);
            if (cardRequire == null || cardRequire.Level < radarTemplate.RequireLevel) {
                Service.gI().sendThongBao(pl, "Bạn cần sưu tầm " + radarRequireTemplate.Name + " ở cấp độ "
                        + radarTemplate.RequireLevel + " mới có thể sử dụng thẻ này");
                return;
            }
        }

        Card card = pl.Cards.stream()
                .filter(r -> r.Id == item.template.id)
                .findFirst().orElse(null);

        if (card == null) {
            Card newCard = new Card(item.template.id, (byte) 1, radarTemplate.Max, (byte) -1, radarTemplate.Options);
            for (Card c : pl.Cards) {
                c.Used = 0;
            }
            newCard.Used = 1;
            pl.Cards.add(newCard);
            RadarService.gI().RadarSetAmount(pl, newCard.Id, newCard.Amount, newCard.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, newCard.Id, newCard.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            if (card.Level >= 2) {
                Service.gI().sendThongBao(pl, "Thẻ này đã đạt cấp tối đa");
                for (Card c : pl.Cards) {
                    c.Used = 0;
                }
                card.Used = 1;
                RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
                return;
            }
            card.Amount++;
            if (card.Amount >= card.MaxAmount) {
                card.Amount = 0;
                card.Level++;
            }
            RadarService.gI().RadarSetAmount(pl, card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        }
    }

    public static void changePet(Player player, Item item) {
        if (!player.isConfirmingChangePet) {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Bạn không có đệ tử để đổi.");
                return;
            }
            player.isConfirmingChangePet = true;
            player.itemToConfirmChangePet = item;
            NpcService.gI().createMenuConMeo(player,
                    ConstNpc.CONFIRM_CHANGE_PET,
                    -1,
                    "Bạn có chắc chắn muốn đổi đệ tử hiện tại không?\nMọi chỉ số và trang bị của đệ tử cũ sẽ bị mất vĩnh viễn.",
                    "Đồng ý", "Từ chối"
            );
        } else {
            if (player.pet != null) {
                int gender = player.pet.gender + 1;
                if (gender > 2) {
                    gender = 0;
                }
                PetService.gI().changeNormalPet(player, gender);
                InventoryService.gI().subQuantityItemsBag(player, player.itemToConfirmChangePet, 1);
                InventoryService.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        }
    }

    public static void itemManhGiay(Player pl, Item item) {
        if (pl.winSTT && !Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            Service.gI().sendThongBao(pl, "Hãy gặp thần mèo Karin để sử dụng");
            return;
        } else if (pl.winSTT && Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            pl.winSTT = false;
            pl.callBossPocolo = false;
            pl.zoneSieuThanhThuy = null;
        }
        NpcService.gI().createMenuConMeo(pl, item.template.id, 564,
                "Đây chính là dấu hiệu riêng của...\nĐại Ma Vương Pôcôlô\nĐó là một tên quỷ dữ đội lốt người, một kẻ đại gian ác\ncó sức mạnh vô địch và lòng tham không đáy...\nĐối phó với hắn không phải dễ\nCon có chắc chắn muốn tìm hắn không?",
                "Đồng ý", "Từ chối");
    }

    public static void itemSieuThanThuy(Player pl, Item item) {
        long tnsm = 500_000;
        int n = 0;
        switch (item.template.id) {
            case 727 -> n = 1;
            case 728 -> n = 2;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        if (Util.isTrue(50, 100)) {
            Service.gI().sendThongBao(pl, "Bạn đã bị chết vì độc của thuốc tăng lực siêu thần thủy.");
            pl.setDie();
        } else {
            for (int i = 0; i < n; i++) {
                Service.gI().addSMTN(pl, (byte) 2, tnsm, true);
            }
        }
    }
}

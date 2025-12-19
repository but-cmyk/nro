package services.func;

import consts.ConstNpc;
import models.item.Item;
import models.npc.NpcFactory; 
import models.player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import services.ShopService;
import services.map.NpcService;
import server.Client;
import models.shop.ItemShop;
import server.Manager;
import models.shop.Shop;
import models.shop.TabShop;

public class MuaCtService {

    private static final double DISCOUNT_RATE_FOR_BUYER = 0.05;
    private static final double COMMISSION_RATE_FOR_SELLER = 0.20;

    private static MuaCtService i;

    public static MuaCtService gI() {
        if (i == null) {
            i = new MuaCtService();
        }
        return i;
    }

    public void openPurchaseConfirmationMenu(Player viewer, int sellerId) {
        if (viewer == null || viewer.zone == null) return;
        Player seller = viewer.zone.getPlayerInMap(sellerId);
        if (seller == null || seller.id == viewer.id) {
            Service.gI().sendThongBao(viewer, "Không thể tìm thấy người chơi này.");
            return;
        }
        Item cosmeticItem = seller.inventory.itemsBody.get(5);
        if (cosmeticItem == null || !cosmeticItem.isNotNullItem()) {
            Service.gI().sendThongBao(viewer, "Người chơi này không mặc cải trang.");
            return;
        }
        int originalPrice = ShopService.gI().getRubyPrice(cosmeticItem.template.id);
        if (originalPrice <= 0) {
            Service.gI().sendThongBao(viewer, "Cải trang này không có bán.");
            return;
        }
        int discountPrice = (int) (originalPrice * (1 - DISCOUNT_RATE_FOR_BUYER));
        int commissionAmount = (int) (originalPrice * COMMISSION_RATE_FOR_SELLER);

        String menuText = String.format("Bạn có muốn mua %s không?\n|1|Giá ưu đãi: %d ngọc\n|2|%s sẽ nhận được %d ngọc hoa hồng",
                cosmeticItem.template.name, discountPrice, seller.name, commissionAmount);
        
        // ---- THAY ĐỔI QUAN TRỌNG ----
        // Lưu dữ liệu tạm thời vào PLAYERID_OBJECT của NpcFactory
        NpcFactory.PLAYERID_OBJECT.put(viewer.id, new Object[]{seller, cosmeticItem});
        
        NpcService.gI().createMenuConMeo(viewer, ConstNpc.CONFIRM_BUY_INSPECT_ITEM, -1,
                menuText, "Đồng ý", "Hủy");
    }

    public void handlePurchase(Player buyer, Player seller, Item itemToBuy) {
        // (Hàm này giữ nguyên như cũ, không cần thay đổi)
        if (buyer == null || seller == null || itemToBuy == null) return;
        ItemShop itemShopSource = null;
    for (Shop shop : Manager.SHOPS) {
        for (TabShop tab : shop.tabShops) {
            for (ItemShop is : tab.itemShops) {
                if (is.temp.id == itemToBuy.template.id && is.typeSell == ShopService.gI().COST_GEM) {
                    itemShopSource = is;
                    break;
                }
            }
            if (itemShopSource != null) break;
        }
        if (itemShopSource != null) break;
    }

    if (itemShopSource == null) {
        Service.gI().sendThongBao(buyer, "Vật phẩm này không còn được bán.");
        return;
    }

    int originalPrice = (int) itemShopSource.cost;
        int discountPrice = (int) (originalPrice * (1 - DISCOUNT_RATE_FOR_BUYER));
        int commissionAmount = (int) (originalPrice * COMMISSION_RATE_FOR_SELLER);
        if (buyer.inventory.gem < discountPrice) {
            Service.gI().sendThongBao(buyer, "Bạn không đủ ngọc.");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(buyer) <= 0) {
            Service.gI().sendThongBao(buyer, "Hành trang không đủ chỗ trống.");
            return;
        }
        buyer.inventory.gem -= discountPrice;
        seller.inventory.gem += commissionAmount;
        Item newItem = ItemService.gI().createItemFromItemShop(itemShopSource);
        InventoryService.gI().addItemBag(buyer, newItem);
        InventoryService.gI().sendItemBags(buyer);
        Service.gI().sendMoney(buyer);
        Service.gI().sendThongBao(buyer, "Bạn đã mua thành công " + newItem.template.name + "!");
        if (seller.isPl()) {
             Service.gI().sendMoney(seller);
             Service.gI().sendThongBao(seller, "Người chơi " + buyer.name + " đã mua " + newItem.template.name + 
                                               " khi xem bạn. Bạn nhận được " + commissionAmount + " ngọc hoa hồng!");
        }
    }
}
package services.func.useitem;

import models.item.Item;
import models.player.Player;

/**
 * Interface chuẩn cho mọi Handler xử lý hành vi sử dụng vật phẩm trong NRO.
 * Áp dụng Strategy & Command Pattern để phân rã switch-case 2.000 dòng.
 */
public interface ItemActionHandler {

    /**
     * Kiểm tra xem handler này có chấp nhận xử lý vật phẩm này không.
     * @param player Người chơi sử dụng
     * @param item Vật phẩm được dùng
     * @return true nếu xử lý được
     */
    boolean canHandle(Player player, Item item);

    /**
     * Thực thi logic sử dụng vật phẩm.
     * @param player Người chơi
     * @param item Vật phẩm
     * @param bagIndex Vị trí trong túi đồ
     */
    void handle(Player player, Item item, int bagIndex);
}

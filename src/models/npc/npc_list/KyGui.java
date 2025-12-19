package models.npc.npc_list;


import models.npc.Npc;
import models.player.Player;
import services.ConsignShopService;
import services.Service;
import services.map.NpcService;

public class KyGui extends Npc {

    public KyGui(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, 0,
                    "Cửa hàng chúng tôi chuyên mua bán hàng hiệu, hàng độc, cảm ơn bạn đã ghé thăm.",
                    "Hướng\ndẫn\nthêm", "Mua bán ký gửi", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player pl, int select) {
        if (canOpenNpc(pl)) {
            switch (select) {
                case 0 ->
                    NpcService.gI().createTutorial(pl, tempId, avartar,
                            "Cửa hàng chuyên nhận ký gửi mua bán vật phẩm\bChỉ với 5 ngọc\bMột người bán, vạn người mua, mại dô, mại dô");
                case 1 -> {
                    if (!pl.getSession().actived) {
                        Service.gI().sendThongBao(pl, "Vui lòng kích hoạt tài khoản để sử dụng chức năng này");
                    } else {
                        ConsignShopService.gI().openShopKyGui(pl);

                    }
                }
            }
        }
    }
}

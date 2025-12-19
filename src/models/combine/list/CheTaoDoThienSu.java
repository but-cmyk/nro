package models.combine.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import models.item.Item;
import models.item.Item.ItemOption;
import models.player.Player;
import services.CombineService;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class CheTaoDoThienSu {

    private static final int GOLD_COST = 50_000_000; // Giá vàng để chế tạo
    private static final int MANH_TS_COST = 999;     // Số lượng mảnh thiên sứ yêu cầu

    public static void startCombine(Player player) {
        // Bước 1: Kiểm tra các điều kiện cơ bản
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        if (player.inventory.gold < GOLD_COST) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }

        // Bước 2: Kiểm tra và lấy các vật phẩm từ ô kết hợp
        Optional<Item> optCtVip = getItem(player, 1);
        Optional<Item> optMTS = getItem(player, 2);
        Optional<Item> optDaNC = getItem(player, 3);
        Optional<Item> optDaMM = getItem(player, 4);

        if (!optCtVip.isPresent() || !optMTS.isPresent() || !optDaNC.isPresent() || !optDaMM.isPresent()) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm, vui lòng kiểm tra lại");
            return;
        }

        Item ctVip = optCtVip.get();
        Item mTS = optMTS.get();
        Item daNC = optDaNC.get();
        Item daMM = optDaMM.get();

        if (mTS.quantity < MANH_TS_COST) {
            Service.gI().sendThongBao(player, "Bạn cần có ít nhất " + MANH_TS_COST + " Mảnh Thiên Sứ");
            return;
        }

        // Bước 3: Trừ vàng và nguyên liệu trước khi đập
        player.inventory.gold -= GOLD_COST;
        InventoryService.gI().subQuantityItemsBag(player, ctVip, 1);
        InventoryService.gI().subQuantityItemsBag(player, mTS, MANH_TS_COST);
        InventoryService.gI().subQuantityItemsBag(player, daNC, 1);
        InventoryService.gI().subQuantityItemsBag(player, daMM, 1);

        // Bước 4: Tính toán tỷ lệ thành công
        int baseRate = 90; // Tỷ lệ cơ bản
        int luckyRateBonus = 5; // Tỷ lệ thêm từ đá may mắn
        
        baseRate += (daNC.template.id - 1073);
        luckyRateBonus += luckyRateBonus * (daMM.template.id - 1078);
        
        // Bước 5: Quay số và xử lý kết quả
        if (Util.isTrue(baseRate, 100)) {
            // ---- THÀNH CÔNG ----
            Service.gI().sendThongBao(player, "Chế tạo thành công!");
            CombineService.gI().sendEffectSuccessCombine(player);

            short[][] itemIds = {
                {1048, 1051, 1054, 1057, 1060}, // Trái Đất
                {1049, 1052, 1055, 1058, 1061}, // Namec
                {1050, 1053, 1056, 1059, 1062}  // Xayda
            };
            
            int gender = ctVip.template.gender > 2 ? player.gender : ctVip.template.gender;
            int type = mTS.typeIdManh();
            
            Item itemTS = ItemService.gI().DoThienSu(itemIds[gender][type], gender);
            
            // Thêm option may mắn
            int randomLucky = Util.nextInt(0, 50);
            if (randomLucky <= luckyRateBonus) {
                int saoMayMan = 1;
                if (randomLucky <= (luckyRateBonus / 2)) saoMayMan = 3;
                else if (randomLucky <= (luckyRateBonus * 0.75)) saoMayMan = 2;
                
                itemTS.itemOptions.add(new ItemOption(107, saoMayMan));
                
                ArrayList<Integer> listOptionBonus = new ArrayList<>(Arrays.asList(50, 77, 103, 108, 5));
                for (int j = 0; j < saoMayMan; j++) {
                    int optionIndex = Util.nextInt(0, listOptionBonus.size() - 1);
                    itemTS.itemOptions.add(new ItemOption(listOptionBonus.get(optionIndex), Util.nextInt(1, 5)));
                    listOptionBonus.remove(optionIndex);
                }
            }
            
            InventoryService.gI().addItemBag(player, itemTS);
        } else {
            // ---- THẤT BẠI ----
            Service.gI().sendThongBao(player, "Chế tạo thất bại, vận may chưa mỉm cười với bạn!");
            CombineService.gI().sendEffectFailCombine(player);
        }

        // Bước 6: Cập nhật lại thông tin cho người chơi
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        player.combineNew.itemsCombine.clear(); // Dòng code gây lỗi đã được SỬA
        CombineService.gI().reOpenItemCombine(player);
    }


    private static Optional<Item> getItem(Player player, int type) {
        return player.combineNew.itemsCombine.stream().filter(item -> {
            if (item.isNotNullItem()) {
                switch (type) {
                    case 1: return item.isCongThucVip();
                    case 2: return item.isManhTS();
                    case 3: return item.isDaNangCap1();
                    case 4: return item.isDaMayMan();
                }
            }
            return false;
        }).findFirst();
    }
}
package server;

//import bot.BotManager;
import consts.ConstNpc;
import data.DataGame;
import data.ItemData;
import database.AlyraManager;
import managers.AdminToolFrame;
import managers.GiftCodeManager;
import managers.boss.*;
import models.item.Item;
import models.player.Pet;
import models.player.Player;
import network.session.SessionManager;
import services.*;
import services.func.Input;
import services.map.ChangeMapService;
import services.map.NpcService;
import services.player.InventoryService;
import utils.Logger; // Giả sử bạn có class Logger
import utils.SystemMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import models.skill.Skill;
import network.io.Message;
import utils.ErrorRes;

public class Command {

    private static Command instance;

    private final Map<String, Consumer<Player>> adminCommands = new HashMap<>();
    private final Map<String, BiConsumer<Player, String>> parameterizedCommands = new HashMap<>();

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    private Command() {
        initAdminCommands();
        initParameterizedCommands();
    }

    private void initAdminCommands() {
        adminCommands.put("h", player -> Service.gI().releaseCooldownSkill(player));
        adminCommands.put("code", player -> GiftCodeManager.gI().checkInfomationGiftCode(player));
        adminCommands.put("a", player -> BossManager.gI().showListBoss(player));
        adminCommands.put("b", player -> BrolyManager.gI().showListBoss(player));
        adminCommands.put("hlw", player -> HalloweenEventManager.gI().showListBoss(player));
        adminCommands.put("lbpb", player -> OtherBossManager.gI().showListBoss(player));
        adminCommands.put("lbdt", player -> RedRibbonHQManager.gI().showListBoss(player));
        adminCommands.put("lbbdkb", player -> TreasureUnderSeaManager.gI().showListBoss(player));
        adminCommands.put("bu2", player -> Input.gI().createFormSenditem1(player));
        adminCommands.put("lbcdrd", player -> SnakeWayManager.gI().showListBoss(player));
        adminCommands.put("lbkghd", player -> GasDestroyManager.gI().showListBoss(player));
          adminCommands.put("adm", player -> {
            // Gọi bảng hiển thị lên Server
            AdminToolFrame.showFrame(); 
            // Thông báo cho người chơi biết đã mở
            Service.gI().sendThongBao(player, "Đã mở bảng Admin Tool trên máy chủ!");
        });


        adminCommands.put("s", player -> {
            SkillService.gI().learSkillSpecial(player, Skill.MA_PHONG_BA);
            SkillService.gI().learSkillSpecial(player, Skill.SUPER_KAME);
            SkillService.gI().learSkillSpecial(player, Skill.LIEN_HOAN_CHUONG);
        });
        adminCommands.put("lbtt", player -> TrungThuEventManager.gI().showListBoss(player));
        adminCommands.put("ad", player -> NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                "|0|Time start: " + ServerManager.timeStart + "\nClients: " + Client.gI().getPlayers().size()
                + " người chơi\n Sessions: " + SessionManager.gI().getNumSession() + "\nThreads: " + Thread.activeCount()
                + " luồng" + "\n" + SystemMetrics.ToString(),
                "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "VND + Tổng nạp", "Save", "Đóng"));
        adminCommands.put("bot", player -> NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_BOT, -1,
                "Player online : " + Client.gI().getPlayers().size() + "\n"
                + "\b|1|Thread: " + Thread.activeCount() + "\n"
                + "Bot online : " + Thread.activeCount(),
                "Bot\nPem Quái", "Bot\nPem Nappa", "Bot\nPem Tương Lai", "Bot\nPem Cold", "Bot\nSăn Boss", "Đóng"));
        adminCommands.put("bu1", player -> Input.gI().createFormGiveItem(player));
        adminCommands.put("bu", player -> Input.gI().createFormGetItem(player));
        adminCommands.put("d", player -> Service.gI().setPos(player, player.location.x, player.location.y + 10));
        adminCommands.put("f", player -> {
            try {
            } catch (Exception e) {
                ErrorRes.howToFix(e.toString());
            }
        });

        // ==================== LOAD LẠI DATABASE ====================
        adminCommands.put("reload", this::reloadDataAndUpdateAllPlayers);
    }

    private void initParameterizedCommands() {
        parameterizedCommands.put("m ", (player, text) -> {
            int mapId = Integer.parseInt(text.replace("m ", ""));
            ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
        });

        parameterizedCommands.put("i ", (player, text) -> {
            try {
                String[] txt = text.split(" ");
                int itemId = Integer.parseInt(txt[1]);
                int quan = 1;
                if (txt.length > 2) {
                    quan = Integer.parseInt(txt[2]);
                }
                Item item = ItemService.gI().createNewItem(((short) itemId), quan);
                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) itemId);
                if (!ops.isEmpty()) {
                    item.itemOptions = ops;
                }
                InventoryService.gI().addItemBag(player, item);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "GET " + item.template.name + " [" + item.template.id + "] SUCCESS !");
            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Nhập sai cú pháp !");
            }
        });

        parameterizedCommands.put("diem ", (player, text) -> {
            try {
                String[] point = text.split(" ");
                int diem = Integer.parseInt(point[1]);
                if (point.length > 2) {
                    Player pl = Client.gI().getPlayer(point[2]);
                    if (pl != null) {
                        pl.inventory.coupon += diem;
                        Service.gI().sendThongBao(player, "Cộng thành công " + diem + " Sự kiện Quy lão cho " + pl.name);
                    } else {
                        Service.gI().sendThongBao(player, "Người chơi không tồn tại hoặc không trực tuyến !");
                    }
                } else {
                    player.inventory.coupon += diem;
                    Service.gI().sendThongBao(player, "Cộng thành công " + diem + " Sự kiện Quy lão !");
                }
            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Nhập sai cú pháp !");
            }
        });
    }

    public void chat(Player player, String text) {
        if (!check(player, text)) {
            Service.gI().chat(player, text);
        }
    }

    public boolean check(Player player, String text) {
        if (player.isAdmin()) {
            // Xử lý lệnh không có tham số
            if (adminCommands.containsKey(text)) {
                adminCommands.get(text).accept(player);
                return true;
            }

            // Xử lý lệnh có tham số
            for (Map.Entry<String, BiConsumer<Player, String>> entry : parameterizedCommands.entrySet()) {
                if (text.startsWith(entry.getKey())) {
                    entry.getValue().accept(player, text);
                    return true;
                }
            }
        }

        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        }

        if (player.pet != null) {
            switch (text) {
                case "di theo", "follow" ->
                    player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" ->
                    player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" ->
                    player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" ->
                    player.pet.changeStatus(Pet.GOHOME);
                case "bien hinh" ->
                    player.pet.transform();
            }
        }
        return false;
    }

    public void reloadDataAndUpdateAllPlayers(Player admin) {
        if (ServerManager.isReloading) {
            Service.gI().sendThongBao(admin, "Server đang trong quá trình tải lại dữ liệu, vui lòng đợi giây lát.");
            return;
        }

        long startTime = System.currentTimeMillis();

        try {

            ServerManager.isReloading = true;
            Service.gI().sendThongBao(admin, "Bắt đầu quá trình tải lại dữ liệu... Server có thể bị lag trong giây lát.");
            Logger.log(Logger.YELLOW, "ADMIN [" + admin.name + "] thực hiện lệnh RELOAD DATA... TẠM DỪNG CÁC LUỒNG UPDATE.");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            AlyraManager.reloadData();
            Manager.gI().reloadData();

            Logger.log(Logger.GREEN, "Tải lại dữ liệu gốc từ database và file thành công.");

        } catch (Exception e) {
            Logger.logException(Command.class, e, "LỖI NGHIÊM TRỌNG: Không thể tải lại dữ liệu gốc của server.");
            Service.gI().sendThongBao(admin, "Có lỗi nghiêm trọng xảy ra khi tải lại dữ liệu. Vui lòng kiểm tra console!");

            ServerManager.isReloading = false;
            return;
        } finally {

            ServerManager.isReloading = false;
            Logger.log(Logger.YELLOW, "Tải lại dữ liệu gốc hoàn tất. KÍCH HOẠT LẠI CÁC LUỒNG UPDATE.");
        }

        Logger.log(Logger.YELLOW, "Bắt đầu đẩy dữ liệu mới xuống cho tất cả người chơi online...");
        List<Player> playersToUpdate = new ArrayList<>(Client.gI().getPlayers());

        for (Player player : playersToUpdate) {
            if (player != null && player.getSession() != null && player.isPl()) {
                try {

                    DataGame.sendVersionGame(player.getSession()); // Gửi lại phiên bản và sức mạnh chuẩn
                    DataGame.updateMap(player.getSession());       // Gửi lại dữ liệu map, NPC, quái
                    DataGame.updateSkill(player.getSession());     // Gửi lại dữ liệu skill
                    ItemData.updateItem(player.getSession());      // Gửi lại toàn bộ dữ liệu item, bao gồm TÊN MỚI

                    Service.gI().player(player);
                    Service.gI().point(player);
                    player.playerSkill.sendSkillShortCut();
                    Service.gI().Send_Caitrang(player);

                    Service.gI().sendThongBao(player, "Dữ liệu game vừa được quản trị viên làm mới thành công.");
                } catch (Exception e) {
                    Logger.error("Lỗi khi đẩy dữ liệu cho người chơi: " + player.name);
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        String message = "Hoàn tất! Quá trình tải lại và đẩy dữ liệu mất " + duration + " ms. Đã cập nhật cho " + playersToUpdate.size() + " người chơi.";
        Service.gI().sendThongBao(admin, message);
        Logger.log(Logger.GREEN, message);
    }
}

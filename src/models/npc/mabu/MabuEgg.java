package models.npc.mabu;

import models.item.Item;
import services.map.ChangeMapService;
import services.PetService;
import services.player.InventoryService;
import models.player.Player;
import utils.Util;
import network.io.Message;
import services.Service;
import utils.Logger;

public class MabuEgg {

//    private static final long DEFAULT_TIME_DONE = 7776000000L;
    private static final long DEFAULT_TIME_DONE = 86400000L;

    private Player player;
    public long lastTimeCreate;
    public long timeDone;

    private final short id = 50;

    public MabuEgg(Player player, long lastTimeCreate, long timeDone) {
        this.player = player;
        this.lastTimeCreate = lastTimeCreate;
        this.timeDone = timeDone;
    }

    public static void createMabuEgg(Player player) {
        player.mabuEgg = new MabuEgg(player, System.currentTimeMillis(), DEFAULT_TIME_DONE);
    }

    public void sendMabuEgg() {
        Message msg;
        try {

            msg = new Message(-122);
            msg.writer().writeShort(this.id);
            msg.writer().writeByte(1);
            msg.writer().writeShort(4664);
            msg.writer().writeByte(0);
            msg.writer().writeInt(this.getSecondDone());
            this.player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(MabuEgg.class, e);
        }
    }

    public int getSecondDone() {
        int seconds = (int) ((lastTimeCreate + timeDone - System.currentTimeMillis()) / 1000);
        return seconds > 0 ? seconds : 0;
    }

    public boolean isOpening = false;

    public void openEgg(int gender) {
        if (gender < 0 || gender > 2) {
            return;
        }
        if (this.player == null) {
            return;
        }
        if (this.isOpening) {
            Service.gI().sendThongBao(player, "Trứng đang nở, vui lòng chờ trong giây lát!");
            return;
        }
        if (this.player.pet == null) {
            Service.gI().sendThongBao(player, "Yêu cầu phải có đệ tử");
            return;
        }

        // Pre-check: Kiểm tra chỗ trống hành trang + rương đồ để bảo vệ trang bị đệ cũ
        int countEquipped = 0;
        if (this.player.pet.inventory != null && this.player.pet.inventory.itemsBody != null) {
            for (Item it : this.player.pet.inventory.itemsBody) {
                if (it != null && it.isNotNullItem()) {
                    countEquipped++;
                }
            }
        }
        int totalEmptySlots = InventoryService.gI().getCountEmptyBag(this.player) + InventoryService.gI().getCountEmptyBox(this.player);
        if (totalEmptySlots < countEquipped) {
            Service.gI().sendThongBao(this.player, "Hành trang và rương phải còn ít nhất " + countEquipped + " ô trống để cất trang bị đệ tử cũ!");
            return;
        }

        this.isOpening = true;
        try {
            // Gửi hiệu ứng vỡ trứng về client (-117 sub 101) mà chưa xóa dữ liệu quả trứng
            sendEffectDestroyEgg();

            final Player p = this.player;
            server.GameLoopManager.gI().schedule(() -> {
                try {
                    if (p != null && !p.beforeDispose && !p.isOffline) {
                        if (p.pet == null) {
                            PetService.gI().createMabuPet(p, gender);
                        } else {
                            PetService.gI().changeMabuPet(p, gender);
                        }
                        // Chỉ hủy trứng sau khi đệ tử Mabu đã khởi tạo và liên kết thành công
                        p.mabuEgg = null;

                        int mapId = 21 + gender;
                        ChangeMapService.gI().changeMapInYard(p, mapId, -1, Util.nextInt(300, 500));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, 4000);
        } catch (Exception e) {
            this.isOpening = false;
            e.printStackTrace();
        }
    }

    public void sendEffectDestroyEgg() {
        try {
            Message msg = new Message(-117);
            msg.writer().writeByte(101);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ignored) {
        }
    }

    public void destroyEgg() {
        sendEffectDestroyEgg();
        if (this.player != null) {
            this.player.mabuEgg = null;
        }
    }

//    public void subTimeDone(int d, int h, int m, int s) {
//        this.timeDone -= ((d * 24 * 60 * 60 * 1000) + (h * 60 * 60 * 1000) + (m * 60 * 1000) + (s * 1000));
//        this.sendMabuEgg();
//    }

    public void dispose() {
        this.player = null;
    }
}

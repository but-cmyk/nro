package services.tournament;

import models.tournament.DeathOrAliveArena;
import managers.tournament.DeathOrAliveArenaManager;
import models.player.Player;
import consts.ConstNpc;
import java.io.IOException;
import models.item.Item;
import models.map.Map;
import models.map.Zone;
import models.npc.Npc;
import network.io.Message;
import server.Manager;
import services.ItemService;
import services.map.NpcManager;
import services.player.InventoryService;
import services.map.MapService;
import services.Service;
import services.map.ChangeMapService;
import utils.Logger;
import static utils.Util.setTimeout;

public class DeathOrAliveArenaService {

    private static DeathOrAliveArenaService i;

    public static DeathOrAliveArenaService gI() {
        if (i == null) {
            i = new DeathOrAliveArenaService();
        }
        return i;
    }

    public void startChallenge(Player player) {
        if (player.zone.map.mapId != 112) {
            return;
        }

        Zone zone = getMapChallenge(112);
        if (zone == null) {
            Service.gI().sendThongBao(player, "Hiện tại các võ đài đều đang bận, vui lòng quay lại sau!");
            return;
        }

        int cost = player.thoiVangVoDaiSinhTu;
        if (cost == 0) cost = 1;
        Item waterBottle = InventoryService.gI().findItemBagByTemp(player, 456);

        if (waterBottle == null || waterBottle.quantity < cost) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + cost + " bình nước!");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, waterBottle, cost);
        InventoryService.gI().sendItemBags(player);

        player.thoiVangVoDaiSinhTu += 1;
        player.lastTimePKVoDaiSinhTu = System.currentTimeMillis();
        player.arenaWins++;

        Service.gI().sendThongBao(player, "Bạn đã tham gia Võ Đài Sinh Tử " + player.arenaWins + " lần!");

        if (!zone.equals(player.zone)) {
            ChangeMapService.gI().changeMap(player, zone, player.location.x, 408);
        }

        final int costUsed = cost;
        setTimeout(() -> {
            try {
                Npc baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
                DeathOrAliveArena vdst = new DeathOrAliveArena();

                vdst.setPlayer(player);
                vdst.setNpc(baHatMit);
                vdst.setRound(0);
                vdst.setZone(zone);
                vdst.setTimeTotal(0);
                vdst.costWaterBottle = costUsed;
                vdst.endChallenge = false;

                DeathOrAliveArenaManager.gI().add(vdst);

                vdst.toTheNextRound();

                if (baHatMit != null) {
                    baHatMit.npcChat(player, "Số thứ tự của ngươi là " + player.id + ", chuẩn bị thi đấu nhé.");
                }

                Service.gI().releaseCooldownSkill(player);
                player.isPKDHVT = true;
                player.lastTimePKDHVT23 = System.currentTimeMillis();

            } catch (Exception e) {
                Logger.logException(DeathOrAliveArenaService.class, e, "Lỗi tại startChallenge VDST");
            }
        }, 500);
    }


    public void cancelChallenge(Player player) {
        if (player.zone.map.mapId != 112) {
            return;
        }

        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);

        // Chỉ chủ phòng mới có thể hủy
        if (vdst != null && vdst.getPlayer() != null && vdst.getPlayer().equals(player)) {

            // Hoàn lại phí đã đăng ký của lượt này
            int cost = vdst.costWaterBottle;
            if (cost > 0) {
                Item waterBottleRefund = ItemService.gI().createNewItem((short) 456, cost);
                InventoryService.gI().addItemBag(player, waterBottleRefund);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Đã hoàn lại " + cost + " bình nước.");
            }

            // Dọn dẹp boss nếu đã sinh ra trên map
            if (vdst.getBoss() != null) {
                vdst.getBoss().leaveMap();
            }

            // Giảm lại 1 lượt vì đã hủy
            player.thoiVangVoDaiSinhTu = Math.max(0, player.thoiVangVoDaiSinhTu - 1);
            player.isPKDHVT = false;

            // Xóa trận đấu khỏi manager để khu vực này trống trở lại
            DeathOrAliveArenaManager.gI().remove(vdst);

            Service.gI().sendThongBao(player, "Đã hủy đăng ký thi đấu thành công.");

            // Cập nhật lại menu cho NPC, chuyển về trạng thái ban đầu
            player.idMark.setIndexMenu(ConstNpc.BASE_MENU);
        } else {
            Service.gI().sendThongBao(player, "Bạn chưa đăng ký, không thể hủy.");
        }
    }

    public void sendTypePK(Player player, Player boss) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public Zone getMapChallenge(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        Zone zone = null;
        try {
            if (map != null) {
                int zoneId = 0;
                while (zoneId < map.zones.size()) {
                    Zone zonez = map.zones.get(zoneId);
                    if (DeathOrAliveArenaManager.gI().getVDST(zonez) == null) {
                        zone = zonez;
                        break;
                    }
                    zoneId++;
                }
            }
        } catch (Exception e) {
        }
        return zone;
    }
}
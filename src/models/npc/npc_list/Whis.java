package models.npc.npc_list;

import consts.BossID;
import consts.ConstNpc;
import models.item.Item;
import java.io.IOException;
import models.combine.list.CheTaoDoThienSu;
import network.io.Message;
import models.npc.Npc;
import models.player.Player;
import services.player.InventoryService;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.CombineService;
import services.phoban.TrainingService;
import services.ShopService;
import models.skill.Skill;
import utils.SkillUtil;
import utils.Util;
import server.Manager; // Import class Manager để lấy topWhis

public class Whis extends Npc {

    private static final int COST_HD = 50000000;

    public Whis(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 154 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Thử đánh với ta xem nào.\nNgươi còn 1 lượt nữa cơ mà.",
                            "Nói chuyện",
                            "Top 100",
                            "[LV:" + (player.traning.getTop() + 1) + "]",
                            "Đến vùng đất hủy diệt");
                }
                case 164 -> {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                            "Quay về", "Từ chối");
                }
                case 169 -> {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Cậu không chịu nổi khi ở đây sao?\nCậu sẽ khó mà mạnh lên được", "Trốn về", "Ở lại");
                }
                case 48 -> models.tournament.whis_arena.WhisArenaService.gI().openMenuAtKaio(this, player);
                case 186 -> models.tournament.whis_arena.WhisArenaService.gI().openMenuAtArena(this, player);
                case 187 -> models.tournament.whis_arena.WhisArenaService.gI().openMenuAtU6Tournament(this, player);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.getIndexMenu() == 100 || player.idMark.getIndexMenu() == 103) {
                models.tournament.whis_arena.WhisArenaService.gI().confirmMenuAtKaio(this, player, select);
                return;
            } else if (player.idMark.getIndexMenu() == 101) {
                models.tournament.whis_arena.WhisArenaService.gI().confirmMenuAtArena(this, player, select);
                return;
            } else if (player.idMark.getIndexMenu() == 102) {
                models.tournament.whis_arena.WhisArenaService.gI().confirmMenuAtU6Tournament(this, player, select);
                return;
            }
            if (player.idMark.isBaseMenu() && this.mapId == 154) {
                switch (select) {
                    case 0 -> { // Nói chuyện
                        if (player.setClothes.checkSetDes()) {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Ngươi hãy trang bị đủ 5 món trang bị Hủy Diệt rồi ta nói chuyện tiếp.",
                                    "OK");
                            return;
                        }
                        this.createOtherMenu(player, 5,
                                "Ta sẽ giúp ngươi chế tạo trang bị thiên sứ",
                                "Shop thiên sứ",
                                "Chế tạo",
                                "Từ chối");
                    }
                    case 1 -> { // Top 100
                        Service.gI().showListTop(player, Manager.topWhis);
                    }
                    case 2 -> { // Vào đánh Boss Whis
                        TrainingService.gI().callBoss(player, BossID.WHIS, false);
                    }
                    case 3 -> { // Đến vùng đất hủy diệt
                        vaoMapHD(player);
                    }
                }
            } 
            // --- CÁC MENU KHÁC (Confirm Shop, Combine, etc.) ---
            else if (player.idMark.getIndexMenu() == 5) {
                switch (select) {
                    case 0: // Shop thiên sứ
                        ShopService.gI().opendShop(player, "THIEN_SU", true);
                        break;
                    case 1: // Chế tạo
                        CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_DO_THIEN_SU);
                        break;
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                if (player.combineNew.typeCombine == CombineService.NANG_CAP_DO_THIEN_SU) {
                    if (select == 0) {
                        CheTaoDoThienSu.startCombine(player);
                    }
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_DAP_DO) {
                if (select == 0) {
                    CombineService.gI().startCombine(player);
                }
            } else if (player.idMark.isBaseMenu() && this.mapId == 169) {
                if (select == 0) {
                    ChangeMapService.gI().changeMapBySpaceShip(player, 154, -1, 450);
                }
            }
        }
    }

    private void vaoMapHD(Player player) {
        if (player.getSession().player.nPoint.power >= 80000000000L && player.inventory.gold > COST_HD) {
            player.inventory.gold -= COST_HD;
            Service.gI().sendMoney(player);
            ChangeMapService.gI().changeMapBySpaceShip(player, 169, -1, 168);
        } else {
            this.npcChat(player, "Bạn chưa đủ điều kiện để vào");
            Service.gI().sendThongBao(player, "Yêu cầu sm lớn hơn 80 Tỷ và 50 Tr vàng.");
        }
    }

}
package services.func;

import consts.ConstItem;
import services.CombineService;
import services.ShenronEventService;
import models.radar.Card;
import services.RadarService;
import models.radar.RadarCard;
import consts.ConstMap;
import models.item.Item;
import consts.ConstNpc;
import consts.ConstPlayer;
import database.daos.PlayerDAO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.boss.Boss;
import models.item.Item.ItemOption;
import models.map.ItemMap;
import models.map.Zone;
import models.map.vetinh.Satellite;
import models.map.vetinh.SatelliteDef;
import models.map.vetinh.SatelliteExp;
import models.map.vetinh.SatelliteHp;
import models.map.vetinh.SatelliteMp;
import models.npc.mabu.MabuEgg;
import models.player.Inventory;
import services.map.NpcService;
import models.player.Player;
import models.skill.Skill;
import network.io.Message;
import services.map.ChangeMapService;
import utils.SkillUtil;
import services.Service;
import utils.Util;
import network.session.MySession;
import services.ItemService;
import services.ItemTimeService;
import services.PetService;
import services.RewardService;
import services.player.PlayerService;
import services.TaskService;
import services.player.InventoryService;
import services.map.MapService;
import services.phoban.NgocRongNamecService;
import utils.ItemUtil;
import utils.Logger;
import utils.TimeUtil;

public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;

    private static UseItem instance;

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(MySession session, Message msg) {
        Player player = session.player;
        if (player == null) {
            return;
        }
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG:
                    InventoryService.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                    break;
                case ITEM_BAG_TO_BOX:
                    InventoryService.gI().itemBagToBox(player, index);
                    break;
                case ITEM_BODY_TO_BOX:
                    InventoryService.gI().itemBodyToBox(player, index);
                    break;
                case ITEM_BAG_TO_BODY:
                    InventoryService.gI().itemBagToBody(player, index);
                    break;
                case ITEM_BODY_TO_BAG:
                    InventoryService.gI().itemBodyToBag(player, index);
                    break;
                case ITEM_BAG_TO_PET_BODY:
                    InventoryService.gI().itemBagToPetBody(player, index);
                    break;
                case ITEM_BODY_PET_TO_BAG:
                    InventoryService.gI().itemPetBodyToBag(player, index);
                    break;
            }
            if (player.setClothes != null) {
                player.setClothes.setup();
            }
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            Service.gI().sendFlagBag(player);
            Service.gI().point(player);
            Service.gI().sendSpeedPlayer(player, -1);
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);

        }
    }

    public Item finditem(Player player, int iditem) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == iditem) {
                return item;
            }
        }
        return null;
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg = null;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM:
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index < 0) {
                                return;
                            }
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học "
                                            + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else if (item.template.id == 570) {

                                    if (player.getSession().isAdmin) {
                                        openWoodChest(player, item);
                                        return;
                                    }

                                    if (!Util.isAfterMidnight(player.lastTimeRewardWoodChest)) {
                                        Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn mở\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else if (item.template.type == 22) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn dùng\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else {
                                    UseItem.gI().useItem(player, item, index);
                                }
                            }
                        } else {
                            int iditem = _msg.reader().readShort();
                            Item item = finditem(player, iditem);
                            UseItem.gI().useItem(player, item, index);
                        }
                    }
                    break;
                case DO_THROW_ITEM:
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (index < 0) {
                            return;
                        }
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }
                        if (item.isNotNullItem() && item.template.id == 570) {
                            Service.gI().sendThongBao(player, "Không thể bỏ vật phẩm này.");
                            return;
                        }
                        if (!item.isNotNullItem()) {
                            return;
                        }
                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case ACCEPT_THROW_ITEM:
                    InventoryService.gI().throwItem(player, where, index);
                    Service.gI().point(player);
                    InventoryService.gI().sendItemBags(player);
                    break;
                case ACCEPT_USE_ITEM:
                    UseItem.gI().useItem(player, player.inventory.itemsBag.get(index), index);
                    break;
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

//=============================USE VỆ TINH======================================
    public void useSatellite(Player player, Item item) {
        Satellite satellite = null;
        if (player.zone != null) {
            if (player.zone.getSatellites().size() < 3) {//max 3 vệ tinh in map
                switch (item.template.id) {
                    case 342 ->
                        satellite = new SatelliteMp(player.zone, 342, player.location.x,
                                player.zone.map.yPhysicInTop(player.location.x, player.location.y), player);
                    case 343 ->
                        satellite = new SatelliteExp(player.zone, 343, player.location.x,
                                player.zone.map.yPhysicInTop(player.location.x, player.location.y), player);
                    case 344 ->
                        satellite = new SatelliteDef(player.zone, 344, player.location.x,
                                player.zone.map.yPhysicInTop(player.location.x, player.location.y), player);
                    case 345 ->
                        satellite = new SatelliteHp(player.zone, 345, player.location.x,
                                player.zone.map.yPhysicInTop(player.location.x, player.location.y), player);
                }
                if (satellite != null) {
                    InventoryService.gI().subQuantityItemsBag(player, item, 1);
                    satellite.sendVeTinh();
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Đã đạt tối đa số lượng vệ tinh có thể đặt trong khu!");
            }
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                if (!Util.isAfterMidnight(pl.lastTimeRewardWoodChest)) {
                    Service.gI().sendThongBao(pl, "Hãy chờ đến ngày mai");
                } else {
                    openWoodChest(pl, item);
                }
                return;
            }
            if (item.template.strRequire <= pl.nPoint.power) {
                switch (item.template.type) {
                    case 33: // card
                        UseCard(pl, item);
                        break;
                    case 7: // sách học, nâng skill
                        learnSkill(pl, item);
                        break;
                    case 6: // đậu thần
                        this.eatPea(pl);
                        break;
                    case 12: // ngọc rồng các loại
                        controllerCallRongThan(pl, item);
                        break;
                    case 22:
                        useSatellite(pl, item);
                        break;
                    case 23: // thú cưỡi mới
                    case 24: // thú cưỡi cũ
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        break;
                    case 11: // item bag
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendFlagBag(pl);
                        break;
                    case 21:
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        PetService.Pet2(pl, pl.getHeadThuCung(), pl.getBodyThuCung(), pl.getLegThuCung());
                        Service.gI().point(pl);
                        break;
                    case 72: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendPetFollow(pl, (short) (item.template.iconID - 1));
                        break;
                    }
                    case 36: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendEffPlayer(pl);
                        break;
                    }
                    case 35: {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        break;
                    }
                    default:
                        switch (item.template.id) {

//====================BÌNH NƯỚC XINBATO=========================================
                            case 456:
                                // 1. Kiểm tra xem có boss Xinbatô trong khu không
                                boolean hasXinbato = pl.zone.getBosses().stream().anyMatch(b -> b.name.equals("Xinbatô"));

                                if (hasXinbato) {
                                    // 2. Kiểm tra người chơi có đủ 99 bình nước không
                                    if (item.quantity >= 1) {
                                        // 3. Trừ 99 bình nước khỏi túi đồ
                                        InventoryService.gI().subQuantityItemsBag(pl, item, 99);
                                        InventoryService.gI().sendItemBags(pl);

                                        // 4. Thả 1 bình nước (ItemMap) ra đất để boss "thấy"
                                        // Đây là bước quan trọng nhất để kích hoạt AI của boss
                                        Service.gI().dropItemMap(pl.zone,
                                                new ItemMap(pl.zone, 456, 1, pl.location.x, pl.location.y, pl.id));

                                        Service.gI().sendThongBao(pl, "Bạn đã đặt bình nước xuống đất, Xinbatô đã chú ý đến nó!");

                                        // Nhiệm vụ tăng tiến độ sẽ được xử lý bên trong AI của boss sau khi nó "uống nước" xong
                                        // Bạn cần thêm logic cộng nhiệm vụ vào file Xibachao.java
                                    } else {
                                        Service.gI().sendThongBao(pl, "Bạn không đủ 1 bình nước.");
                                    }
                                } else {
                                    Service.gI().sendThongBao(pl, "Không thể sử dụng, hãy đến nơi có Xinbatô.");
                                }
                                break;
//====================XƯƠNG SÓI HẸC=========================================                                
                            case 460:
                                if (pl.zone.getBosses().stream().anyMatch(b -> b.name.equals("Sói hẹc quyn"))) {
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    InventoryService.gI().sendItemBags(pl);
                                    Service.gI().dropItemMap(pl.zone,
                                            new ItemMap(pl.zone, 460, 1, pl.location.x, pl.location.y, pl.id));
                                    //     Service.gI().sendThongBao(pl, "Đã ném xương, hãy đợi Sói hẹc quyn ăn xong để nhận tiến độ.");
                                } else {
                                    Service.gI().sendThongBao(pl, "Không thể vứt cục xương, hãy đến nơi có Sói hẹc quyn.");
                                }
                                break;
//====================TRỨNG MABU=========================================
                            case 568:
                                if (pl.mabuEgg == null) {
                                    MabuEgg.createMabuEgg(pl);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    InventoryService.gI().sendItemBags(pl);
                                } else {
                                    Service.gI().sendThongBao(pl, "Con đang có trứng Mabu chưa mở ở nhà rồi.");
                                }
                                break;
//====================TRỨNG ĐỆ THƯỜNG=========================================
                            case 1396:
                                PetService.gI().createNormalPet(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBags(pl);
                                break;
//====================CAPSULE ĐEO LƯNG NRO=========================================                                
                            case 1138:
                                Input.gI().createFormThongBao(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBags(pl);
                                if (pl.playerTask.taskdh.DungLoa < 10) {
                                    pl.playerTask.taskdh.DungLoa++;
                                    pl.playerTask.taskdh.ResetTime = System.currentTimeMillis();
                                }
                                break;
                            case 992: // Nhan thoi khong
                                pl.type = 2;
                                pl.maxTime = 5;
                                Service.gI().Transport(pl);
                                break;
                            case 718: // Vé tặng ngọc (ID 718)
                                Input.gI().createFormTangGem(pl);
                                break;

                            case 1345:
                                // Sửa title thành Tặng Thỏi vàng
                                Input.gI().createForm(pl, Input.TANG_THOI_VANG, "Tặng Thỏi Vàng",
                                        new Input.SubInput("Tên người nhận", Input.ANY),
                                        new Input.SubInput("Số lượng Thỏi vàng", Input.NUMERIC));
                                break;
//====================RADA DÒ NRO NAMEK=========================================                                                               
                            case 361:
                                pl.idGo = (short) Util.nextInt(0, 6);
                                NgocRongNamecService.gI().menuCheckTeleNamekBall(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBags(pl);
                                break;
//====================PET đang lỗi=========================================                                
//                            case 1128://heo bướm
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1404, 1405, 1406);
//                                Service.gI().point(pl);
//                                break;
//                            case 1129://quái vật
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1407, 1408, 1409);
//                                Service.gI().point(pl);
//                                break;
//                            case 1232://Pet Rắn 2025
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1500, 1501, 1502);
//                                Service.gI().point(pl);
//                                break;
//                            case 1130://Chó Shiba
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1410, 1411, 1412);
//                                Service.gI().point(pl);
//                                break;
//                            case 1131://Chim Cánh Cụt
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1413, 1414, 1415);
//                                Service.gI().point(pl);
//                                break;                         
//                            case 1132://Pet Godzilla
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1416, 1417, 1418);
//                                Service.gI().point(pl);
//                                break;
//                            case 1133://Pet KingKong
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1419, 1420, 1421);
//                                Service.gI().point(pl);
//                                break;
//                            case 1146://Capybara xì mũi
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1435, 1436, 1437);
//                                Service.gI().point(pl);
//                                break;
//                            case 1145://Capybara đeo ba lô
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1438, 1439, 1440);
//                                Service.gI().point(pl);
//                                break;
//                            case 1144://Pet Hải Ly
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1441, 1442, 1443);
//                                Service.gI().point(pl);
//                                break;                         
//                            case 1222://Pet ông già noel satan lùn
//                                InventoryService.gI().itemBagToBody(pl, indexBag);
//                                PetService.Pet2(pl, 1485, 1486, 1487);
//                                Service.gI().point(pl);
//                                break;
                            case 211: // nho tím
                            case 212: // nho xanh
                                eatGrapes(pl, item);
                                break;
                            case 380: // cskb
                                openCSKB(pl, item);
                                break;
                            case 381: // cuồng nộ
                            case 382: // bổ huyết
                            case 383: // bổ khí
                            case 384: // giáp xên
                            case 385: // ẩn danh
                            case 379: // máy dò capsule
                            case 638: // commeson
                            case 2075: // rocket
                            case 2160: // Nồi cơm điện
                            case 579:
                            case 1045: // đuôi khỉ
                            case 663: // bánh pudding
                            case 664: // xúc xíc
                            case 665: // kem dâu
                            case 666: // mì ly
                            case 667: // sushi
                            case 1099://Cuồng Nộ Siêu Cấp
                            case 1100://Bổ Huyết Siêu Cấp
                            case 1101://Bổ Khí Siêu Cấp
                            case 1102://Giáp Xên Siêu Cấp
                            case 1103://Ẩn Danh Đặc Biệt
                            case 764://Khẩu trang
                            case 1136://Bùa x2 tn,sm
                            case 1137://Cỏ bốn lá
                            case 753://Bánh chưng
                            case 752://Bánh tét
                            case 1261://Xí muội Hoa đào
                            case 1262://Xí muội Hoa mai
                            case 1397://Bùa TNSM
                                useItemTime(pl, item);
                                break;
                            case 1467://Hộp món kích hoạt Vip Trái Đất
                                UseItem.gI().ItemSKH(pl, item);
                                break;
                            case 1468://Hộp món kích hoạt Vip Namec
                                UseItem.gI().ItemSKH(pl, item);
                                break;
                            case 1469://Hộp món kích hoạt Vip XAYDA
                                UseItem.gI().ItemSKH(pl, item);
                                break;
                            case 1426://Trứng Vàng Rồng Nhí
                                open1426(pl, item);
                                break;
                            case 1429://Hộp quà giỗ tổ cao cấp
                                open1429(pl, item);
                                break;
                            case 1428://Hộp quà giỗ tổ
                                open1428(pl, item);
                                break;

                            case 1427://Mảnh Trứng Rồng Nhí
                                Item trungRong = InventoryService.gI().findItemBag(pl, 1427);
                                if (trungRong != null && trungRong.quantity >= 99) {
                                    open1427(pl, item);
                                } else {
                                    Service.gI().sendThongBao(pl, "Đã đủ địt đâu và sử dụng gom x99 giùm bố cái");
                                }
                                break;
                            case 880://Cua rang me
                            case 881:
                            case 882:
                                if (pl.itemTime.isEatMeal2) {
                                    Service.gI().sendThongBao(pl, "Chỉ được sử dụng 1 cái");
                                    break;
                                }
                                useItemTime(pl, item);
                                break;
                            case 570:
                                openWoodChest(pl, item);
                                break;
                            case 521: // tdlt
                                useTDLT(pl, item);
                                break;
//============HỘP QUÀ TÂN THỦ================================                               
                            case 1391:
                                OpenQuaQue(pl, item);
                                break;
                            case 454: // bông tai
                                UseItem.gI().usePorata(pl);
                                break;
//============Capsule 1 món kích hoạt Trái Đất=======================
                            case 1227:
                                UseItem.gI().ItemSKH(pl, item);
                                break;
                            case 1228:
                                UseItem.gI().ItemSKH(pl, item);
                                break;
                            case 1229:
                                UseItem.gI().ItemSKH(pl, item);
                                break;

                            case 921: // bông tai
                                UseItem.gI().usePorata2(pl);
                                break;
                            case 1346: // bông tai
                                UseItem.gI().usePorata3(pl);
                                break;
                            case 193: // gói 10 viên capsule
                                openCapsuleUI(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            case 194: // capsule đặc biệt
                                openCapsuleUI(pl);
                                break;
                            case 401: // đổi đệ tử
                                changePet(pl, item);
                                break;
                            case 402: // sách nâng chiêu 1 đệ tử
                            case 403: // sách nâng chiêu 2 đệ tử
                            case 404: // sách nâng chiêu 3 đệ tử
                            case 759: // sách nâng chiêu 4 đệ tử
                                upSkillPet(pl, item);
                                break;
                            case 726:
                                UseItem.gI().ItemManhGiay(pl, item);
                                break;
                            case 727:
                            case 728:
                                UseItem.gI().ItemSieuThanThuy(pl, item);
                                break;
                            case 648:
                                ItemService.gI().OpenItem648(pl, item);
                                break;
                            case 1143:
                                ItemService.gI().OpenItem1143(pl, item);
                                break;
                            case 1198:
                                ItemService.gI().OpenItem1198(pl, item);
                                break;
                            case 1199:
                                ItemService.gI().OpenItem1199(pl, item);
                                break;
                            case 1200:
                                ItemService.gI().OpenItem1200(pl, item);
                                break;
                            case 736:
                                ItemService.gI().OpenItem736(pl, item);
                                break;
                            case 987:
                                Service.gI().sendThongBao(pl, "Bảo vệ trang bị không bị rớt cấp"); // đá bảo vệ
                                break;
                            case 2006:
                                Input.gI().createFormChangeNameByItem(pl);
                                break;
                            case 1156, 1157:
                                ItemService.gI().OpenCapsuleCaiTrang(pl, item);
                                break;
                            case 1190:
                                Input.gI().createFormTangRuby(pl);
                                break;
                            case 1189:
                                Input.gI().createFormTangGem(pl);
                                break;
                            case 1259:
                                ItemService.gI().OpenItem1259(pl, item);
                                break;
                            case 1245:
                                ItemService.gI().OpenItem1245(pl, item);
                                break;
                            case 1258:
                                ItemService.gI().OpenItem1258(pl, item);
                                break;
                            case 1246:
                                ItemService.gI().OpenItem1246(pl, item);
                                break;
                            case 1247:
                                ItemService.gI().OpenItem1247(pl, item);
                                break;
                            case 1305:
                                ItemService.gI().OpenItem1305(pl, item);
                                break;
                            case 1381:
                                if (pl.pet == null) { // Đổi skill 2
                                    Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                                    break;
                                }
                                if (pl.pet.playerSkill.skills.get(1).skillId != -1) {
                                    pl.pet.openSkill2();
                                    InventoryService.gI().subQuantityItem(pl.inventory.itemsBag, item, 1);
                                    InventoryService.gI().sendItemBags(pl);
                                    Service.gI().sendThongBao(pl, "Đã đổi thành công chiêu 2 đệ tử");
                                } else {
                                    Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                                }
                                break;
                            case 1382:
                                if (pl.pet == null) { // Đổi skill 3
                                    Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                                    break;
                                }
                                if (pl.pet.playerSkill.skills.get(2).skillId != -1) {
                                    pl.pet.openSkill3();
                                    InventoryService.gI().subQuantityItem(pl.inventory.itemsBag, item, 1);
                                    InventoryService.gI().sendItemBags(pl);
                                    Service.gI().sendThongBao(pl, "Đã đổi thành công chiêu 3");
                                } else {
                                    Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                }
                                break;
                            case 1383:
                                if (pl.pet == null) { // Đổi skill 4
                                    Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                                    break;
                                }
                                if (pl.pet.playerSkill.skills.get(3).skillId != -1) {
                                    pl.pet.openSkill4();
                                    InventoryService.gI().subQuantityItem(pl.inventory.itemsBag, item, 1);
                                    InventoryService.gI().sendItemBags(pl);
                                    Service.gI().sendThongBao(pl, "Đã đổi thành công chiêu 4");
                                } else {
                                    Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 4 chứ!");
                                }
                                break;
                            case 1032: {
                                Item giaymau = InventoryService.gI().findItemBagByTemp(pl, 1032);
                                boolean hasgiaymau = giaymau != null && giaymau.quantity >= 99;
                                StringBuilder npcSay = new StringBuilder();
                                npcSay.append("|2|Chế tạo Hộp đựng quà\n");
                                npcSay.append(hasgiaymau ? "|1|" : "|7|").append("Giấy màu ")
                                        .append(hasgiaymau ? giaymau.quantity : "0").append("/99\n");
                                NpcService.gI().createMenuConMeo(pl, ConstNpc.CHE_TAO_HOP_QUA, -1, npcSay.toString(),
                                        "Đồng ý", "Từ chối");
                            }
                            break;
                            case 1544: {
                                Item hoahonggiay = InventoryService.gI().findItemBagByTemp(pl, 1308);
                                Item socola = InventoryService.gI().findItemBagByTemp(pl, 1307);
                                Item hopdungqua = InventoryService.gI().findItemBagByTemp(pl, 1313);
                                boolean hashoahonggiay = hoahonggiay != null && hoahonggiay.quantity >= 30;
                                boolean hassocola = socola != null && socola.quantity >= 5;
                                boolean hashopdungqua = hopdungqua != null && hopdungqua.quantity >= 1;

                                StringBuilder npcSay = new StringBuilder();
                                npcSay.append("|2|Chế tạo Hộp quà Nhẹ Nhàng\n");
                                npcSay.append(hashoahonggiay ? "|1|" : "|7|").append("Hoa hồng giấy: ")
                                        .append(hashoahonggiay ? hoahonggiay.quantity : "0").append("/30\n");
                                npcSay.append(hassocola ? "|1|" : "|7|").append("Socola: ")
                                        .append(hassocola ? socola.quantity : "0").append("/5\n");
                                npcSay.append(hashopdungqua ? "|1|" : "|7|").append("Hộp đựng quà: ")
                                        .append(hashopdungqua ? hopdungqua.quantity : "0").append("/1\n");

                                NpcService.gI().createMenuConMeo(pl, ConstNpc.CHE_TAO_HOP_QUA_NHE_NHANG, -1,
                                        npcSay.toString(), "Đồng ý", "Từ chối");
                            }
                            break;
                            case 1545: {
                                Item hoahonggiay = InventoryService.gI().findItemBagByTemp(pl, 1308);
                                Item socola = InventoryService.gI().findItemBagByTemp(pl, 1307);
                                Item hopdungqua = InventoryService.gI().findItemBagByTemp(pl, 1313);
                                boolean hashoahonggiay = hoahonggiay != null && hoahonggiay.quantity >= 30;
                                boolean hassocola = socola != null && socola.quantity >= 5;
                                boolean hashopdungqua = hopdungqua != null && hopdungqua.quantity >= 1;

                                StringBuilder npcSay = new StringBuilder();
                                npcSay.append("|2|Chế tạo Hộp quà Nhẹ Nhàng\n");
                                npcSay.append(hashoahonggiay ? "|1|" : "|7|").append("Hoa hồng giấy: ")
                                        .append(hashoahonggiay ? hoahonggiay.quantity : "0").append("/30\n");
                                npcSay.append(hassocola ? "|1|" : "|7|").append("Socola: ")
                                        .append(hassocola ? socola.quantity : "0").append("/5\n");
                                npcSay.append(hashopdungqua ? "|1|" : "|7|").append("Hộp đựng quà: ")
                                        .append(hashopdungqua ? hopdungqua.quantity : "0").append("/1\n");

                                NpcService.gI().createMenuConMeo(pl, ConstNpc.CHE_TAO_HOP_QUA_NHE_NHANG, -1,
                                        npcSay.toString(), "Đồng ý", "Từ chối");
                            }
                            break;
                            case 1550: {
                                Item hoahonggiay = InventoryService.gI().findItemBagByTemp(pl, 1308);
                                Item socola = InventoryService.gI().findItemBagByTemp(pl, 1307);
                                Item hopdungqua = InventoryService.gI().findItemBagByTemp(pl, 1313);
                                boolean hashoahonggiay = hoahonggiay != null && hoahonggiay.quantity >= 30;
                                boolean hassocola = socola != null && socola.quantity >= 5;
                                boolean hashopdungqua = hopdungqua != null && hopdungqua.quantity >= 1;

                                StringBuilder npcSay = new StringBuilder();
                                npcSay.append("|2|Chế tạo Hộp quà Nhẹ Nhàng\n");
                                npcSay.append(hashoahonggiay ? "|1|" : "|7|").append("Hoa hồng giấy: ")
                                        .append(hashoahonggiay ? hoahonggiay.quantity : "0").append("/30\n");
                                npcSay.append(hassocola ? "|1|" : "|7|").append("Socola: ")
                                        .append(hassocola ? socola.quantity : "0").append("/5\n");
                                npcSay.append(hashopdungqua ? "|1|" : "|7|").append("Hộp đựng quà: ")
                                        .append(hashopdungqua ? hopdungqua.quantity : "0").append("/1\n");

                                NpcService.gI().createMenuConMeo(pl, ConstNpc.CHE_TAO_HOP_QUA_NHE_NHANG, -1,
                                        npcSay.toString(), "Đồng ý", "Từ chối");
                            }
                            break;
                            case 1548: {
                                Item hoahonggiay = InventoryService.gI().findItemBagByTemp(pl, 1308);
                                Item socola = InventoryService.gI().findItemBagByTemp(pl, 1307);
                                Item hopdungqua = InventoryService.gI().findItemBagByTemp(pl, 1313);
                                Item no = InventoryService.gI().findItemBagByTemp(pl, 1311);

                                boolean hashoahonggiay = hoahonggiay != null && hoahonggiay.quantity >= 30;
                                boolean hassocola = socola != null && socola.quantity >= 5;
                                boolean hashopdungqua = hopdungqua != null && hopdungqua.quantity >= 1;
                                boolean hasno = no != null && no.quantity >= 1;

                                StringBuilder npcSay = new StringBuilder();
                                npcSay.append("|2|Chế tạo Hộp quà Chỉn chu\n");
                                npcSay.append(hashoahonggiay ? "|1|" : "|7|").append("Hoa hồng giấy: ")
                                        .append(hashoahonggiay ? hoahonggiay.quantity : "0").append("/30\n");
                                npcSay.append(hassocola ? "|1|" : "|7|").append("Socola: ")
                                        .append(hassocola ? socola.quantity : "0").append("/5\n");
                                npcSay.append(hashopdungqua ? "|1|" : "|7|").append("Hộp đựng quà: ")
                                        .append(hashopdungqua ? hopdungqua.quantity : "0").append("/1\n");
                                npcSay.append(hasno ? "|1|" : "|7|").append("Nơ: ").append(hasno ? no.quantity : "0")
                                        .append("/1\n");

                                NpcService.gI().createMenuConMeo(pl, ConstNpc.CHE_TAO_HOP_QUA_CHIN_CHU, -1,
                                        npcSay.toString(), "Đồng ý", "Từ chối");
                            }
                            break;
                            case 1097: {
                                Item dattrong = InventoryService.gI().findItemBagByTemp(pl, 1306);
                                Item ongtrenuoc = InventoryService.gI().findItemBagByTemp(pl, 1312);
                                Item hatgiong = InventoryService.gI().findItemBagByTemp(pl, 1309);
                                Item chaudat = InventoryService.gI().findItemBagByTemp(pl, 1322);

                                boolean hasdattrong = dattrong != null && dattrong.quantity >= 99;
                                boolean hasongtrenuoc = ongtrenuoc != null && ongtrenuoc.quantity >= 5;
                                boolean hashatgiong = hatgiong != null && hatgiong.quantity >= 1;
                                boolean haschaudat = chaudat != null && chaudat.quantity >= 1;

                                StringBuilder npcSay = new StringBuilder();
                                npcSay.append("|2|Trồng hoa hồng\n");
                                npcSay.append(hasdattrong ? "|1|" : "|7|").append("Đất trồng: ")
                                        .append(hasdattrong ? dattrong.quantity : "0").append("/99\n");
                                npcSay.append(hasongtrenuoc ? "|1|" : "|7|").append("Ống tre nước: ")
                                        .append(hasongtrenuoc ? ongtrenuoc.quantity : "0").append("/5\n");
                                npcSay.append(hashatgiong ? "|1|" : "|7|").append("Hạt giống: ")
                                        .append(hashatgiong ? hatgiong.quantity : "0").append("/1\n");
                                npcSay.append(haschaudat ? "|1|" : "|7|").append("Chậu đất: ")
                                        .append(haschaudat ? chaudat.quantity : "0").append("/1\n");

                                NpcService.gI().createMenuConMeo(pl, ConstNpc.TRONG_HOA_HONG, -1, npcSay.toString(),
                                        "Đồng ý", "Từ chối");
                            }
                            break;
                            case 1096: {
                                Item dattrong = InventoryService.gI().findItemBagByTemp(pl, 1306);
                                Item ongtrenuoc = InventoryService.gI().findItemBagByTemp(pl, 1312);
                                Item hatgiong = InventoryService.gI().findItemBagByTemp(pl, 1309);
                                Item chaudat = InventoryService.gI().findItemBagByTemp(pl, 1322);
                                Item thuoctangtruong = InventoryService.gI().findItemBagByTemp(pl, 1323);

                                boolean hasdattrong = dattrong != null && dattrong.quantity >= 99;
                                boolean hasongtrenuoc = ongtrenuoc != null && ongtrenuoc.quantity >= 5;
                                boolean hashatgiong = hatgiong != null && hatgiong.quantity >= 1;
                                boolean haschaudat = chaudat != null && chaudat.quantity >= 1;
                                boolean hasthuoctangtruong = thuoctangtruong != null && thuoctangtruong.quantity >= 1;

                                StringBuilder npcSay = new StringBuilder();
                                npcSay.append("|2|Trồng hoa hồng\n");
                                npcSay.append(hasdattrong ? "|1|" : "|7|").append("Đất trồng: ")
                                        .append(hasdattrong ? dattrong.quantity : "0").append("/99\n");
                                npcSay.append(hasongtrenuoc ? "|1|" : "|7|").append("Ống tưới nước: ")
                                        .append(hasongtrenuoc ? ongtrenuoc.quantity : "0").append("/5\n");
                                npcSay.append(hashatgiong ? "|1|" : "|7|").append("Hạt giống: ")
                                        .append(hashatgiong ? hatgiong.quantity : "0").append("/1\n");
                                npcSay.append(haschaudat ? "|1|" : "|7|").append("Chậu đất: ")
                                        .append(haschaudat ? chaudat.quantity : "0").append("/1\n");
                                npcSay.append(hasthuoctangtruong ? "|1|" : "|7|").append("Thuốc tăng trưởng: ")
                                        .append(hasthuoctangtruong ? thuoctangtruong.quantity : "0").append("/1\n");

                                NpcService.gI().createMenuConMeo(pl, ConstNpc.TRONG_HOA_HONG_1, -1, npcSay.toString(),
                                        "Đồng ý", "Từ chối");
                            }
                            break;
                            case 1321:
                                useHopbabytry(pl, item);
                                break;
                            case 1338:
                                useradangocrong(pl, item);
                                break;
                            case 1324:
                                HopThanLinh(pl, item);
                                break;
                        }
                        break;
                }
                TaskService.gI().checkDoneTaskUseItem(pl, item);
                InventoryService.gI().sendItemBags(pl);
            } else {
                Service.gI().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
            }
        }
    }

    private void HopThanLinh(Player pl, Item item) {
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Chọn hành tinh của bạn!", "Set\nTrái đất",
                "Set\nNamec", "Set\nXayda", "Từ chối");
    }

    private int randClothes(int level) {
        return ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][level - 1];
    }

    private void useHopbabytry(Player pl, Item item) { // phụ kiện
        if (InventoryService.gI().getCountEmptyBag(pl) < 1) {
            Service.gI().sendThongBao(pl, "Hãy chừa 1 ô trống để mở.");
            return;
        }

        int[] items = new int[]{1318, 1319};
        int itemID = items[Util.nextInt(0, items.length - 1)];
        Item reward;
        switch (itemID) {
            case 1319:
                reward = ItemService.gI().createNewItem((short) 1319);
                reward.itemOptions.add(new ItemOption(50, 18));
                reward.itemOptions.add(new ItemOption(77, 7));
                reward.itemOptions.add(new ItemOption(103, 7));
                reward.itemOptions.add(new ItemOption(5, 11));
                if (Util.isTrue(99, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                }
                break;
            case 1318:
                reward = ItemService.gI().createNewItem((short) 1318);
                reward.itemOptions.add(new ItemOption(50, 18));
                reward.itemOptions.add(new ItemOption(5, 8));
                reward.itemOptions.add(new ItemOption(14, 5));
                if (Util.isTrue(99, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                }
                break;
            default:
                reward = ItemService.gI().createNewItem((short) itemID, 1);
                break;
        }
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        Service.gI().sendThongBao(pl, "Bạn đã nhận được " + reward.template.name);
    }

    private void useradangocrong(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) < 1) {
            Service.gI().sendThongBao(pl, "Hãy chừa 1 ô trống để mở.");
            return;
        }

        int[] items = new int[]{1329, 1330, 1331, 1332, 1333, 1334, 1335, 1336};
        int itemID = items[Util.nextInt(0, items.length - 1)];
        Item reward;
        switch (itemID) {
            case 1336:
                reward = ItemService.gI().createNewItem((short) 1336);
                reward.itemOptions.add(new ItemOption(50, 22));
                reward.itemOptions.add(new ItemOption(77, 22));
                reward.itemOptions.add(new ItemOption(103, 22));
                reward.itemOptions.add(new ItemOption(108, 10));
                reward.itemOptions.add(new ItemOption(94, 12));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            case 1335:
                reward = ItemService.gI().createNewItem((short) 1335);
                reward.itemOptions.add(new ItemOption(50, 12));
                reward.itemOptions.add(new ItemOption(77, 12));
                reward.itemOptions.add(new ItemOption(103, 12));
                reward.itemOptions.add(new ItemOption(14, 5));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            case 1333:
                reward = ItemService.gI().createNewItem((short) 1333);
                reward.itemOptions.add(new ItemOption(50, 15));
                reward.itemOptions.add(new ItemOption(77, 15));
                reward.itemOptions.add(new ItemOption(103, 15));
                reward.itemOptions.add(new ItemOption(14, 5));
                reward.itemOptions.add(new ItemOption(108, 5));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            case 1330:
                reward = ItemService.gI().createNewItem((short) 1330);
                reward.itemOptions.add(new ItemOption(50, 18));
                reward.itemOptions.add(new ItemOption(77, 18));
                reward.itemOptions.add(new ItemOption(103, 18));
                reward.itemOptions.add(new ItemOption(108, 10));
                reward.itemOptions.add(new ItemOption(94, 10));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            case 1329:
                reward = ItemService.gI().createNewItem((short) 1329);
                reward.itemOptions.add(new ItemOption(50, 20));
                reward.itemOptions.add(new ItemOption(77, 20));
                reward.itemOptions.add(new ItemOption(103, 20));
                reward.itemOptions.add(new ItemOption(108, 12));
                reward.itemOptions.add(new ItemOption(94, 12));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            case 1334:
                reward = ItemService.gI().createNewItem((short) 1334);
                reward.itemOptions.add(new ItemOption(50, 13));
                reward.itemOptions.add(new ItemOption(77, 13));
                reward.itemOptions.add(new ItemOption(103, 13));
                reward.itemOptions.add(new ItemOption(108, 5));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            case 1332:
                reward = ItemService.gI().createNewItem((short) 1332);
                reward.itemOptions.add(new ItemOption(50, 16));
                reward.itemOptions.add(new ItemOption(77, 16));
                reward.itemOptions.add(new ItemOption(103, 16));
                reward.itemOptions.add(new ItemOption(14, 5));
                reward.itemOptions.add(new ItemOption(5, 5));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            case 1331:
                reward = ItemService.gI().createNewItem((short) 1331);
                reward.itemOptions.add(new ItemOption(50, 17));
                reward.itemOptions.add(new ItemOption(77, 17));
                reward.itemOptions.add(new ItemOption(103, 17));
                reward.itemOptions.add(new ItemOption(108, 5));
                reward.itemOptions.add(new ItemOption(94, 5));
                reward.itemOptions.add(new ItemOption(30, 0));
                if (Util.isTrue(95, 100)) {
                    reward.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                break;
            default:
                reward = ItemService.gI().createNewItem((short) itemID, 1);
                break;
        }
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        Service.gI().sendThongBao(pl, "Bạn đã nhận được " + reward.template.name);
    }

    private void openWoodChest(Player pl, Item item) {
        int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
        if (time == 0) {
            Service.gI().sendThongBao(pl, "Vì bạn quên không lấy chìa nên cần đợi 24h để bẻ khóa");
            return;
        }

        int param = item.itemOptions.get(0).param;
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        String text = "Bạn nhận được\n";
        List<Item> rewards = new ArrayList<>();

        // Danh sách item chia theo tier
        int[] itemT1 = {223, 224, 225, 17, 18};
        int[] itemT2 = {441, 442, 443, 444};
        int[] itemT3 = {445, 446, 447, 19, 20};

        // Gold theo cấp độ
        gold = 1000 * (int) Math.pow(param, 2) * 9;

        // Ngọc (có từ cấp 6 trở lên)
        if (param >= 6 && param <= 9) {
            gem = Util.nextInt(1, 3);
        } else if (param == 10) {
            gem = Util.nextInt(3, 6);
        } else if (param > 10) {
            gem = Util.nextInt(5, 10);
            ruby = Util.nextInt(1, 3);
        }

        // Trang bị (số lượng và tier theo cấp)
        int numClothes = param < 6 ? 1 : (param < 10 ? 2 : 3);
        for (int i = 0; i < numClothes; i++) {
            int clothesId = randClothes(param);
            Item eq = ItemService.gI().createNewItem((short) clothesId);
            RewardService.gI().initBaseOptionClothes(eq.template.id, eq.template.type, eq.itemOptions);
            RewardService.gI().initStarOption(eq, new RewardService.RatioStar[]{
                new RewardService.RatioStar((byte) 1, 40 - param, 100),
                new RewardService.RatioStar((byte) 2, 20, 100),
                new RewardService.RatioStar((byte) 3, 10, 100),
                new RewardService.RatioStar((byte) 4, 5, 100),});
            rewards.add(eq);
        }

        // Item thường
        int[] selectedPool = param <= 4 ? itemT1 : (param <= 7 ? itemT2 : itemT3);
        int numItems = Math.min(1 + param / 3, selectedPool.length);
        int[] randomItems = Util.pickNRandInArr(selectedPool, numItems);
        for (int id : randomItems) {
            Item it = ItemService.gI().createNewItem((short) id);
            it.quantity = 1;
            RewardService.gI().initBaseOptionSaoPhaLe(it);
            rewards.add(it);
        }

        // Đặc biệt cho param >= 11
        if (param >= 11) {
            Item manhNhan = ItemService.gI().createNewItem((short) ConstItem.MANH_NHAN);
            manhNhan.quantity = Util.nextInt(1, 2);
            rewards.add(manhNhan);
        }

        // Cộng thưởng vào player
        pl.inventory.gold += gold;
        pl.inventory.gem += gem;
        pl.inventory.ruby += ruby;
        if (gold > 0) {
            pl.textRuongGo.add(text + "|4| " + Util.powerToString(gold) + " Vàng");
        }
        if (gem > 0) {
            pl.textRuongGo.add(text + "|1| " + gem + " Ngọc");
        }
        if (ruby > 0) {
            pl.textRuongGo.add(text + "|1| " + ruby + " Hồng Ngọc");
        }

        for (Item reward : rewards) {
            InventoryService.gI().addItemBag(pl, reward);
            pl.textRuongGo.add(text + reward.getInfoItem());
        }

        NpcService.gI().createMenuConMeo(pl, ConstNpc.RUONG_GO, -1,
                "Bạn nhận được\n|1|+" + Util.powerToString(gold) + " vàng", "OK [" + pl.textRuongGo.size() + "]");

        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);
    }

    private void OpenQuaQue(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {17, 18, 19, 20, 188, 189, 441, 442, 443, 444, 445, 446, 447, 190, 381, 382, 383, 384, 385};
            int[][] gold = {{5000, 20000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                if (it.template.id == 1392 || it.template.id == 1393) {
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(1, 21)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(1, 21)));
                    it.itemOptions.add(new ItemOption(14, Util.nextInt(1, 21)));
                } else {
                    it.itemOptions.add(new ItemOption(73, 0));
                }
                it.itemOptions.add(new ItemOption(73, 0));
                RewardService.gI().initBaseOptionSaoPhaLe(it);
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void changePet(Player player, Item item) {
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

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;

        // Nếu thể lực còn trên 50%, không cần phải hồi phục và thoát ra ngay
        if (percentCurrentStatima > 50) {
            Service.gI().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;  // Thoát khỏi phương thức nếu không cần hồi phục
        }

        // Tiến hành hồi phục thể lực nếu item là nho tím (ID 211) hoặc nho xanh (ID 212)
        if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;  // Hồi phục 100% thể lực
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);  // Hồi phục 20% thể lực
            if (pl.nPoint.stamina > pl.nPoint.maxStamina) {
                pl.nPoint.stamina = pl.nPoint.maxStamina;  // Đảm bảo thể lực không vượt quá giới hạn
            }
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }

        // Giảm số lượng item sau khi sử dụng
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);  // Cập nhật túi đồ của người chơi

        // Gửi thông tin thể lực hiện tại của người chơi
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {76, 188, 189, 190, 381, 382, 383, 384, 385};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;

            // Nếu index <= 3 thì ra Vàng (giữ nguyên logic random ra vàng hay ra đồ của bạn)
            if (index <= 3) {
                int goldReceived = 0;
                // Random tỉ lệ từ 1 đến 100
                int chance = Util.nextInt(1, 100);

                if (chance <= 82) { // 82% cơ hội
                    goldReceived = Util.nextInt(5000, 20000);
                } else if (chance <= 92) { // 10% cơ hội tiếp theo (82 + 10)
                    goldReceived = Util.nextInt(20000, 30000);
                } else if (chance <= 97) { // 5% cơ hội tiếp theo (92 + 5)
                    goldReceived = Util.nextInt(30000, 1000000);
                } else { // 3% cơ hội còn lại
                    goldReceived = Util.nextInt(1000000, 11000000);
                }

                // Cộng vàng và kiểm tra giới hạn
                pl.inventory.gold += goldReceived;
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }

                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930; // Icon thỏi vàng hoặc icon tiền
            } else {
                // Logic ra đồ (giữ nguyên)
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }

            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy");
        }
    }
    //==============TRỨNG RỒNG NHÍ================================

    private static void open1427(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {1426};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            Item it = ItemService.gI().createNewItem(temp[index]);
            InventoryService.gI().addItemBag(pl, it);
            Service.gI().sendThongBao(pl, "Bạn Nhận Được Quả Trứng Rồng Nhí");
            icon[1] = 15127;
            InventoryService.gI().subQuantityItemsBag(pl, item, 99);
            InventoryService.gI().sendItemBags(pl);
            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }
//================EVENT DỖ TỔ HÙNG VƯƠNG==================================

    private static void open1428(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item2;
            int rand = Util.nextInt(100); // sử dụng hệ số 1000 để tăng độ chính xác

            if (rand < 50) { // 0.5% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1413);
                item2.itemOptions.add(new ItemOption(77, 15));
                item2.itemOptions.add(new ItemOption(108, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 50) { // 1% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1414);
                item2.itemOptions.add(new ItemOption(50, 15));
                item2.itemOptions.add(new ItemOption(14, 6));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else {
                Service.gI().sendThongBao(pl, "Bạn không nhận được vật phẩm nào.");
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                return;
            }
            // Thêm option ngẫu nhiên
            if (Util.nextInt(100) < 99) {
                item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            }

            InventoryService.gI().addItemBag(pl, item2);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + item2.template.name);
            InventoryService.gI().sendItemBags(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

//================EVENT DỖ TỔ HÙNG VƯƠNG==================================
    private static void open1429(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item2;
            int rand = Util.nextInt(1000); // sử dụng hệ số 1000 để tăng độ chính xác

            if (rand < 5) { // 0.5% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1416);
                item2.itemOptions.add(new ItemOption(50, 26));
                item2.itemOptions.add(new ItemOption(77, 23));
                item2.itemOptions.add(new ItemOption(103, 23));
                item2.itemOptions.add(new ItemOption(95, 10));
                item2.itemOptions.add(new ItemOption(97, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 15) { // 1% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1113);
                item2.itemOptions.add(new Item.ItemOption(50, 23));
                item2.itemOptions.add(new Item.ItemOption(77, 22));
                item2.itemOptions.add(new Item.ItemOption(27, 22));
                item2.itemOptions.add(new Item.ItemOption(108, 5));
                item2.itemOptions.add(new Item.ItemOption(30, 0));
            } else if (rand < 65) { // 5% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1413);
                item2.itemOptions.add(new ItemOption(77, 17));
                item2.itemOptions.add(new ItemOption(108, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 135) { // 7% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1414);
                item2.itemOptions.add(new ItemOption(50, 17));
                item2.itemOptions.add(new ItemOption(14, 11));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 385) { // 25% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1447);
                item2.itemOptions.add(new ItemOption(50, 14));
                item2.itemOptions.add(new ItemOption(77, 14));
                item2.itemOptions.add(new ItemOption(103, 14));
                item2.itemOptions.add(new ItemOption(97, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 685) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1448);
                item2.itemOptions.add(new ItemOption(50, 14));
                item2.itemOptions.add(new ItemOption(77, 14));
                item2.itemOptions.add(new ItemOption(103, 14));
                item2.itemOptions.add(new ItemOption(97, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 985) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1449);
                item2.itemOptions.add(new ItemOption(50, 14));
                item2.itemOptions.add(new ItemOption(77, 14));
                item2.itemOptions.add(new ItemOption(103, 14));
                item2.itemOptions.add(new ItemOption(97, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else {
                Service.gI().sendThongBao(pl, "Bạn không nhận được vật phẩm nào.");
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                return;
            }
            // Thêm option ngẫu nhiên
            if (Util.nextInt(100) < 99) {
                item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            }

            InventoryService.gI().addItemBag(pl, item2);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + item2.template.name);
            InventoryService.gI().sendItemBags(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    //==============TRỨNG VÀNG RỒNG NHÍ================================
    private static void open1426(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item2;
            int rand = Util.nextInt(1000); // sử dụng hệ số 1000 để tăng độ chính xác

            if (rand < 5) { // 0.5% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1418);
                item2.itemOptions.add(new ItemOption(50, 20));
                item2.itemOptions.add(new ItemOption(77, 20));
                item2.itemOptions.add(new ItemOption(103, 20));
                item2.itemOptions.add(new ItemOption(108, 12));
                item2.itemOptions.add(new ItemOption(94, 12));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 15) { // 1% - cực hiếm
                item2 = ItemService.gI().createNewItem((short) 1419);
                item2.itemOptions.add(new ItemOption(50, 18));
                item2.itemOptions.add(new ItemOption(77, 18));
                item2.itemOptions.add(new ItemOption(103, 18));
                item2.itemOptions.add(new ItemOption(108, 10));
                item2.itemOptions.add(new ItemOption(94, 10));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 65) { // 5% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1420);
                item2.itemOptions.add(new ItemOption(77, 18));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(108, 7));
                item2.itemOptions.add(new ItemOption(94, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 135) { // 7% - hiếm hơn
                item2 = ItemService.gI().createNewItem((short) 1421);
                item2.itemOptions.add(new ItemOption(77, 18));
                item2.itemOptions.add(new ItemOption(5, 7));
                item2.itemOptions.add(new ItemOption(14, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 385) { // 25% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1422);
                item2.itemOptions.add(new ItemOption(50, 18));
                item2.itemOptions.add(new ItemOption(94, 15));
                item2.itemOptions.add(new ItemOption(108, 7));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 685) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1423);
                item2.itemOptions.add(new ItemOption(50, 18));
                item2.itemOptions.add(new ItemOption(5, 7));
                item2.itemOptions.add(new ItemOption(14, 5));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else if (rand < 985) { // 30% - tỉ lệ cao hơn
                item2 = ItemService.gI().createNewItem((short) 1424);
                item2.itemOptions.add(new ItemOption(77, 16));
                item2.itemOptions.add(new ItemOption(50, 16));
                item2.itemOptions.add(new ItemOption(103, 16));
                item2.itemOptions.add(new ItemOption(236, 20));
                item2.itemOptions.add(new ItemOption(30, 0));
            } else {
                Service.gI().sendThongBao(pl, "Bạn không nhận được vật phẩm nào.");
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                return;
            }
            // Thêm option ngẫu nhiên
            if (Util.nextInt(100) < 99) {
                item2.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            }

            InventoryService.gI().addItemBag(pl, item2);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + item2.template.name);
            InventoryService.gI().sendItemBags(pl);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useItemTime(Player pl, Item item) {
        switch (item.template.id) {
            case 1397:
                pl.itemTime.lastTimeBuaTNSM = System.currentTimeMillis();
                pl.itemTime.isBuaTNSM = true;
                break;
            case 382: // bổ huyết
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                break;
            case 383: // bổ khí
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                break;
            case 384: // giáp xên
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                break;
            case 381: // cuồng nộ
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                Service.gI().point(pl);
                break;
            case 385: // ẩn danh
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            case 379: // máy dò capsule
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                break;
            case 1099:// cn
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                Service.gI().point(pl);

                break;
            case 1100:// bo huyet
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                break;
            case 764:
                pl.itemTime.lastTimeKhauTrang = System.currentTimeMillis();
                pl.itemTime.isKhauTrang = true;
                break;
            case 1136:
                pl.itemTime.lastTimeTnDeTu = System.currentTimeMillis();
                pl.itemTime.isTnDeTu = true;
                break;
            case 1101:// bo khi
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                break;
            case 1102:// gx
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                break;
            case 1103:// an danh
                pl.itemTime.lastTimeAnDanh2 = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh2 = true;
                break;
            case 638: // Commeson
                pl.itemTime.lastTimeUseCMS = System.currentTimeMillis();
                pl.itemTime.isUseCMS = true;
                break;
            case 2160: // Nồi cơm điện
                pl.itemTime.lastTimeUseNCD = System.currentTimeMillis();
                pl.itemTime.isUseNCD = true;
                break;
            case 579:
            case 1045: // Đuôi khỉ
                pl.itemTime.lastTimeUseDK = System.currentTimeMillis();
                pl.itemTime.isUseDK = true;
                break;
            case 663: // bánh pudding
            case 664: // xúc xíc
            case 665: // kem dâu
            case 666: // mì ly
            case 667: // sushi
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                break;
            case 880:
            case 881:
            case 882:
                pl.itemTime.lastTimeEatMeal2 = System.currentTimeMillis();
                pl.itemTime.isEatMeal2 = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal2);
                pl.itemTime.iconMeal2 = item.template.iconID;
                break;
            case 1109: // máy dò đồ
                pl.itemTime.lastTimeUseMayDo2 = System.currentTimeMillis();
                pl.itemTime.isUseMayDo2 = true;
                break;
            case 1137:
                pl.itemTime.lastTimeUseCo4La = System.currentTimeMillis();
                pl.itemTime.isUseCo4La = true;
                break;
            case 753:
                pl.itemTime.banhchunglastTime = System.currentTimeMillis();
                pl.itemTime.banhchung = true;
                break;
            case 752:
                pl.itemTime.banhtetlastTime = System.currentTimeMillis();
                pl.itemTime.banhtet = true;
                break;
            case 1261:
                pl.itemTime.lastTimeXimuoihoadao = System.currentTimeMillis();
                pl.itemTime.isXimuoihoadao = true;
                break;
            case 1262:
                pl.itemTime.lastTimeXimuoihoamai = System.currentTimeMillis();
                pl.itemTime.isXimuoihoamai = true;
                break;
        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO || tempId == ConstItem.BI_NGO_1_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO:
                case SummonDragon.NGOC_RONG_2_SAO:
                case SummonDragon.NGOC_RONG_3_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13), SummonDragon.DRAGON_SHENRON);
                    break;
                case ConstItem.BI_NGO_1_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) 702, SummonDragon.DRAGON_BLACK_SHENRON);
                    break;
                default:
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON,
                            -1, "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
                    break;
            }
        } else if (tempId >= ShenronEventService.NGOC_RONG_1_SAO && tempId <= ShenronEventService.NGOC_RONG_7_SAO) {
            ShenronEventService.gI().openMenuSummonShenron(pl, 0);
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                            pl.BoughtSkills.add((int) item.template.id);
                        } else {
                            Skill skillNeed = SkillUtil
                                    .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl,
                                    "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            // System.out.println(curSkill.template.name + " - " + curSkill.point);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                            pl.BoughtSkills.add((int) item.template.id);

                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp "
                                    + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata3(Player pl) {
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

    private void usePorata2(Player pl) {
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

    private void usePorata(Player pl) {
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

    private void openCapsuleUI(Player pl) {
        pl.idMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {

        if (pl.idNRNM != -1) {
            Service.gI().sendThongBao(pl, "Không thể mang ngọc rồng này lên Phi thuyền");
            Service.gI().hideWaitDialog(pl);
            return;
        }

        int zoneId = -1;
        if (index > pl.mapCapsule.size() - 1 || index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        // Kiểm tra số lượng người trong khu
        zoneChose = ChangeMapService.gI().checkMapCanJoin(pl, zoneChose);
        if (zoneChose == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        if (zoneChose.getNumOfPlayers() > 25
                //    || MapService.gI().isMapDoanhTrai(zoneChose.map.mapId)
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        pl.changeMapVIP = true;
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    public void eatPea(Player player) {
        if (!Util.canDoWithTime(player.lastTimeEatPea, 1000)) {
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
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += statima;
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

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402: // skill 1
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 403: // skill 2
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 404: // skill 3
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 759: // skill 4
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;

            }

        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void ItemManhGiay(Player pl, Item item) {
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

    private void ItemSieuThanThuy(Player pl, Item item) {
        long tnsm = 500_000;
        int n = 0;
        switch (item.template.id) {
            case 727:
                n = 1;
                break;
            case 728:
                n = 2;
                break;
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

    public void UseCard(Player pl, Item item) {
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
            // Nếu thẻ chưa có, tạo mới
            Card newCard = new Card(item.template.id, (byte) 1, radarTemplate.Max, (byte) -1, radarTemplate.Options);

            // Reset tất cả thẻ khác không còn được sử dụng
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
                // Cập nhật aura ngay cả khi đạt tối đa
                byte auraId = pl.getAura();
                if (auraId != -1) {
                    RadarService.gI().sendAura(pl, auraId, 0);
                }
                return;
            }

            card.Amount++;
            if (card.Amount >= card.MaxAmount) {
                card.Amount = 0;
                if (card.Level == -1) {
                    card.Level = 1;
                } else {
                    card.Level++;
                }
                Service.gI().point(pl);
            }

            for (Card c : pl.Cards) {
                c.Used = 0;
            }
            card.Used = 1;

            RadarService.gI().RadarSetAmount(pl, card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        }

        // ✅ Cập nhật aura ngay lập tức khi đã đủ điều kiện (Used = 1, Level > 1)
        byte auraId = pl.getAura();
        if (auraId != -1) {
            RadarService.gI().sendAura(pl, auraId, 0); // 0 nếu không dùng hiệu ứng set item riêng
        }
    }

//
//    public static final int[][][] LIST_ITEM_CLOTHES = {
//        // áo , quần , găng ,giày,rada
//        // td -> nm -> xd
//        {{0, 33, 3, 34, 136, 137, 138, 139, 230, 231, 232, 233, 555},
//        {6, 35, 9, 36, 140, 141, 142, 143, 242, 243, 244, 245, 556},
//        {21, 24, 37, 38, 144, 145, 146, 147, 254, 255, 256, 257, 562},
//        {27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269, 563},
//        {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}},
//        {{1, 41, 4, 42, 152, 153, 154, 155, 234, 235, 236, 237, 557},
//        {7, 43, 10, 44, 156, 157, 158, 159, 246, 247, 248, 249, 558},
//        {22, 46, 25, 45, 160, 161, 162, 163, 258, 259, 260, 261, 564},
//        {28, 47, 31, 48, 164, 165, 166, 167, 270, 271, 272, 273, 565},
//        {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}},
//        {{2, 49, 5, 50, 168, 169, 170, 171, 238, 239, 240, 241, 559},
//        {8, 51, 11, 52, 172, 173, 174, 175, 250, 251, 252, 253, 560},
//        {23, 53, 26, 54, 176, 177, 178, 179, 262, 263, 264, 265, 566},
//        {29, 55, 32, 56, 180, 181, 182, 183, 274, 275, 276, 277, 567},
//        {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}}
//    };
    private void ItemSKH(Player pl, Item item) {
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Hãy chọn một món quà", "Áo", "Quần", "Găng", "Giày",
                "Rada", "Từ Chối");
    }

}

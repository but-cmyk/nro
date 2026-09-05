package models.npc;

import services.player.ClanService;
import services.Service;
import services.ItemService;
import services.phoban.NgocRongNamecService;
import services.player.IntrinsicService;
import services.player.InventoryService;
import services.map.NpcService;
import services.PetService;
import services.player.PlayerService;
import services.player.FriendAndEnemyService;
import consts.ConstNpc;
import managers.boss.BossManager;
import models.clan.Clan;
import services.func.MuaCtService;
import models.item.Item;

import java.util.HashMap;
import managers.ConsignShopManager;
import models.ConsignItem;

import services.map.ChangeMapService;
import services.func.SummonDragon;
import static services.func.SummonDragon.SHENRON_1_STAR_WISHES_1;
import static services.func.SummonDragon.SHENRON_1_STAR_WISHES_2;
import static services.func.SummonDragon.SHENRON_SAY;

import models.player.Player;
import models.item.Item;
import models.matches.PVPService;
import server.Client;
import server.Maintenance;
import server.Manager;
import services.func.Input;
import utils.Logger;
import utils.Util;
import services.phoban.SuperDivineWaterService;
import services.SubMenuService;
import models.npc.npc_list.*;
import models.skill.Skill;
import network.io.Message;
import services.ConsignShopService;
import services.func.SummonDragonNamek;
import utils.SkillUtil;

public class NpcFactory {

    public static final java.util.Map<Long, Object> PLAYERID_OBJECT = new HashMap<>();

    public static Npc createNPC(int mapId, int status, int cx, int cy, int tempId) {
        int avatar = Manager.NPC_TEMPLATES.get(tempId).avatar;
        try {
            return switch (tempId) {
             //   case ConstNpc.SHOP_NEW -> new ShopNew(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.HUNG_VUONG -> new HungVuong(mapId, status, cx, cy, tempId, avatar);
//                case ConstNpc.DUONG_TANG -> new DuongTang(mapId, status, cx, cy, tempId, avatar);
//                case ConstNpc.NGO_KHONG -> new NgoKhong(mapId, status, cx, cy, tempId, avatar);
                // case ConstNpc.CAY_THONG ->
                // new CayThong(mapId, status, cx, cy, tempId, avatar);
//                case ConstNpc.TORI_BOT -> new ToriBot(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.GHI_DANH -> new GhiDanh(mapId, status, cx, cy, tempId, avatar);
//                case ConstNpc.CHI_CHI -> new ChiChi(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TRONG_TAI -> new TrongTai(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.POTAGE -> new Potage(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.MR_POPO -> new MrPoPo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.QUY_LAO_KAME -> new QuyLaoKame(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TRUONG_LAO_GURU -> new TruongLaoGuru(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.VUA_VEGETA -> new VuaVegeta(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CUA_HANG_KY_GUI -> new KyGui(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.ONG_GOHAN -> new OngGohan(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.ONG_MOORI -> new OngMoori(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.ONG_PARAGUS -> new OngParagus(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BUNMA -> new Bulma(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DENDE -> new Dende(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.APPULE -> new Appule(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DR_DRIEF -> new DrDrief(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CARGO -> new Cargo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CUI -> new Cui(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.SANTA -> new Santa(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.URON -> new Uron(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BA_HAT_MIT -> new BaHatMit(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RUONG_DO -> new RuongDo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DAU_THAN -> new DauThan(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CALICK -> new Calick(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.JACO -> new Jaco(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.THUONG_DE -> new ThuongDe(mapId, status, cx, cy, tempId, avatar);
//                case ConstNpc.VADOS -> new Vados(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.THAN_VU_TRU -> new ThanVuTru(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.KIBIT -> new Kibit(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.OSIN -> new Osin(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BABIDAY -> new Babiday(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.LY_TIEU_NUONG -> new LyTieuNuong(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.LINH_CANH -> new LinhCanh(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.QUA_TRUNG -> new QuaTrung(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.QUOC_VUONG -> new QuocVuong(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BUNMA_TL -> new BulmaTuongLai(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_OMEGA -> new RongOmega(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_1S -> new Rong1Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_2S -> new Rong2Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_3S -> new Rong3Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_4S -> new Rong4Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_5S -> new Rong5Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_6S -> new Rong6Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_7S -> new Rong7Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DAI_THIEN_SU -> new DaiThienSu(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.WHIS -> new Whis(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BILL -> new Bill(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BO_MONG -> new BoMong(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.THAN_MEO_KARIN -> new Karin(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.GOKU_SSJ -> new GokuSSJ(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.GOKU_SSJ_2 -> new GokuSSJ2(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TAPION -> new Tapion(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DOC_NHAN -> new DocNhan(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.GIUMA_DAU_BO -> new GiuMaDauBo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TO_SU_KAIO -> new ToSuKaio(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BARDOCK -> new Bardock(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BERRY -> new Berry(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.NOI_BANH -> new NoiBanh(mapId, status, cx, cy, tempId, avatar);

                default -> new Npc(mapId, status, cx, cy, tempId, avatar) {
                    @Override
                    public void openBaseMenu(Player player) {
                        if (canOpenNpc(player)) {
                            super.openBaseMenu(player);
                        }
                    }

                    @Override
                    public void confirmMenu(Player player, int select) {
                        if (canOpenNpc(player)) {
                        }
                    }
                };
            };
        } catch (Exception e) {
            Logger.logException(NpcFactory.class,
                    e, "Lỗi load npc");
            return null;
        }
    }

    public static void createNpcRongThieng() {
        new Npc(-1, -1, -1, -1, ConstNpc.RONG_THIENG, -1) {
            @Override
            public void confirmMenu(Player player, int select) {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.IGNORE_MENU:
                        break;
                    case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM:
                        SummonDragonNamek.gI().showConfirmShenron(player, player.idMark.getIndexMenu(), (byte) select);
                        break;
                    case ConstNpc.SHENRON_NAMEK_CONFIRM:
                        if (select == 0) {
                            SummonDragonNamek.gI().confirmWish();
                        } else if (select == 1) {
                            SummonDragonNamek.gI().sendWhishesNamec(player);
                        }
                        break;
                    case ConstNpc.SHENRON_CONFIRM:
                        if (select == 0) {
                            SummonDragon.gI().confirmWish();
                        } else if (select == 1) {
                            SummonDragon.gI().reOpenShenronWishes(player);
                        }
                        break;
                    case ConstNpc.SHENRON_1_1:
                        if (player.idMark.getIndexMenu() == ConstNpc.SHENRON_1_1
                                && select == SHENRON_1_STAR_WISHES_1.length - 1) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_1_2, SHENRON_SAY,
                                    SHENRON_1_STAR_WISHES_2);
                            break;
                        }
                    case ConstNpc.SHENRON_1_2:
                        if (player.idMark.getIndexMenu() == ConstNpc.SHENRON_1_2
                                && select == SHENRON_1_STAR_WISHES_2.length - 1) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_1_1, SHENRON_SAY,
                                    SHENRON_1_STAR_WISHES_1);
                            break;
                        }
                    default:
                        SummonDragon.gI().showConfirmShenron(player, player.idMark.getIndexMenu(), (byte) select);
                        break;
                }
            }
        };
    }

    public static void createNpcConMeo() {
        new Npc(-1, -1, -1, -1, ConstNpc.CON_MEO, 351) {
            @Override
            public void confirmMenu(Player player, int select) {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.IGNORE_MENU -> {
                    }
                    case ConstNpc.CONFIRM_CHANGE_PET -> {
                    if (select == 0) {
                        services.func.UseItem.gI().changePet(player, null);
                    }
                    player.isConfirmingChangePet = false;
                    player.itemToConfirmChangePet = null;
                    break;
                }
                    case ConstNpc.MAKE_MATCH_PVP -> {
                        if (Maintenance.isRunning) {
                        }
                        PVPService.gI().sendInvitePVP(player, (byte) select);
                    }
                    case ConstNpc.CHE_TAO_HOP_QUA -> {
                        Item giaymau = InventoryService.gI().findItemBagByTemp(player, 1032);
                        boolean hasgiaymau = giaymau != null && giaymau.quantity >= 99;
                        if (hasgiaymau) {
                            Item hopQua = ItemService.gI().createNewItem((short) 1313);
                            InventoryService.gI().subQuantityItemsBag(player, giaymau, 99);
                            InventoryService.gI().addItemBag(player, hopQua);
                            InventoryService.gI().sendItemBags(player);
                            Service.gI().sendThongBao(player,
                                    "Chúc mừng con nhé! Bạn đã chế tạo thành công Hộp đựng quà.");
                        } else {
                            Service.gI().sendThongBao(player, "Con không đủ nguyên liệu để chế tạo Hộp đựng quà.");
                        }
                    }
                    case ConstNpc.CONFIRM_BUY_INSPECT_ITEM -> {
                    if (select == 0) { // Người chơi chọn "Đồng ý"
                        try {
                            // Lấy lại thông tin người bán và vật phẩm đã lưu trong PLAYERID_OBJECT
                            Object[] data = (Object[]) PLAYERID_OBJECT.get(player.id);
                            Player seller = (Player) data[0];
                            Item itemToBuy = (Item) data[1];

                            // Gọi hàm xử lý mua hàng
                            MuaCtService.gI().handlePurchase(player, seller, itemToBuy);
                        } catch (Exception e) {
                            Service.gI().sendThongBao(player, "Giao dịch đã hết hạn hoặc có lỗi xảy ra.");
                        }
                    }
                    // Xóa đối tượng tạm sau khi xử lý xong
                    PLAYERID_OBJECT.remove(player.id);
                    break;
                    }

                    case ConstNpc.CHE_TAO_HOP_QUA_NHE_NHANG -> {
                        Item hoahonggiay = InventoryService.gI().findItemBagByTemp(player, 1308);
                        Item socola = InventoryService.gI().findItemBagByTemp(player, 1307);
                        Item hopdungqua = InventoryService.gI().findItemBagByTemp(player, 1313);

                        boolean hasHoahonggiay = hoahonggiay != null && hoahonggiay.quantity >= 30;
                        boolean hasSocola = socola != null && socola.quantity >= 5;
                        boolean hasHopdungqua = hopdungqua != null && hopdungqua.quantity >= 1;

                        if (hasHoahonggiay && hasSocola && hasHopdungqua) {
                            Item hopquanhenhang = ItemService.gI().createNewItem((short) 1314);
                            InventoryService.gI().subQuantityItemsBag(player, hoahonggiay, 30);
                            InventoryService.gI().subQuantityItemsBag(player, socola, 5);
                            InventoryService.gI().subQuantityItemsBag(player, hopdungqua, 1);
                            InventoryService.gI().addItemBag(player, hopquanhenhang);
                            InventoryService.gI().sendItemBags(player);

                            this.npcChat(player, "Chúc mừng! Bạn đã chế tạo thành công Hộp quà nhẹ nhàng.");
                        } else {
                            this.npcChat(player, "Con không đủ nguyên liệu để chế tạo Hộp quà nhẹ nhàng.");
                        }
                    }
                    case ConstNpc.CHE_TAO_HOP_QUA_CHIN_CHU -> {
                        Item hoahonggiay = InventoryService.gI().findItemBagByTemp(player, 1308);
                        Item socola = InventoryService.gI().findItemBagByTemp(player, 1307);
                        Item hopdungqua = InventoryService.gI().findItemBagByTemp(player, 1313);
                        Item no = InventoryService.gI().findItemBagByTemp(player, 1311);

                        boolean hasHoahonggiay = hoahonggiay != null && hoahonggiay.quantity >= 30;
                        boolean hasSocola = socola != null && socola.quantity >= 5;
                        boolean hasHopdungqua = hopdungqua != null && hopdungqua.quantity >= 1;
                        boolean hasNo = no != null && no.quantity >= 1;

                        if (hasHoahonggiay && hasSocola && hasHopdungqua && hasNo) {
                            Item hopQuaChinChu = ItemService.gI().createNewItem((short) 1315);
                            InventoryService.gI().subQuantityItemsBag(player, hoahonggiay, 30);
                            InventoryService.gI().subQuantityItemsBag(player, socola, 5);
                            InventoryService.gI().subQuantityItemsBag(player, hopdungqua, 1);
                            InventoryService.gI().subQuantityItemsBag(player, no, 1);
                            InventoryService.gI().addItemBag(player, hopQuaChinChu);
                            InventoryService.gI().sendItemBags(player);

                            this.npcChat(player, "Chúc mừng con! Con đã chế tạo thành công Hộp quà Chỉn chu.");
                        } else {
                            this.npcChat(player, "Con không đủ nguyên liệu để chế tạo Hộp quà Chỉn chu.");
                        }
                    }
                    case ConstNpc.TRONG_HOA_HONG -> {
                        Item dattrong = InventoryService.gI().findItemBagByTemp(player, 1306);
                        Item ongtrenuoc = InventoryService.gI().findItemBagByTemp(player, 1312);
                        Item hatgiong = InventoryService.gI().findItemBagByTemp(player, 1309);
                        Item chaudat = InventoryService.gI().findItemBagByTemp(player, 1322);

                        boolean hasdattrong = dattrong != null && dattrong.quantity >= 99;
                        boolean hasongtrenuoc = ongtrenuoc != null && ongtrenuoc.quantity >= 5;
                        boolean hashatgiong = hatgiong != null && hatgiong.quantity >= 1;
                        boolean haschaudat = chaudat != null && chaudat.quantity >= 1;

                        if (hasdattrong && hasongtrenuoc && hashatgiong && haschaudat) {

                            InventoryService.gI().subQuantityItemsBag(player, dattrong, 99);
                            InventoryService.gI().subQuantityItemsBag(player, ongtrenuoc, 5);
                            InventoryService.gI().subQuantityItemsBag(player, hatgiong, 1);
                            InventoryService.gI().subQuantityItemsBag(player, chaudat, 1);

                            Item hoahong = ItemService.gI().createNewItem((short) 589);
                            hoahong.quantity = Util.nextInt(1, 3);
                            InventoryService.gI().addItemBag(player, hoahong);

                            InventoryService.gI().sendItemBags(player);
                            this.npcChat(player, "Chúc mừng! Bạn đã trồng được Hoa hồng.");
                        } else {
                            this.npcChat(player, "Bạn không đủ nguyên liệu để trồng Hoa hồng.");
                        }
                    }
                    case ConstNpc.TRONG_HOA_HONG_1 -> {
                        Item dattrong = InventoryService.gI().findItemBagByTemp(player, 1306);
                        Item ongtrenuoc = InventoryService.gI().findItemBagByTemp(player, 1312);
                        Item hatgiong = InventoryService.gI().findItemBagByTemp(player, 1309);
                        Item chaudat = InventoryService.gI().findItemBagByTemp(player, 1322);
                        Item thuoctangtruong = InventoryService.gI().findItemBagByTemp(player, 1323);

                        boolean hasdattrong = dattrong != null && dattrong.quantity >= 99;
                        boolean hasongtrenuoc = ongtrenuoc != null && ongtrenuoc.quantity >= 5;
                        boolean hashatgiong = hatgiong != null && hatgiong.quantity >= 1;
                        boolean haschaudat = chaudat != null && chaudat.quantity >= 1;
                        boolean hasthuoctangtruong = thuoctangtruong != null && thuoctangtruong.quantity >= 1;

                        if (hasdattrong && hasongtrenuoc && hashatgiong && haschaudat && hasthuoctangtruong) {

                            InventoryService.gI().subQuantityItemsBag(player, dattrong, 99);
                            InventoryService.gI().subQuantityItemsBag(player, ongtrenuoc, 5);
                            InventoryService.gI().subQuantityItemsBag(player, hatgiong, 1);
                            InventoryService.gI().subQuantityItemsBag(player, chaudat, 1);
                            InventoryService.gI().subQuantityItemsBag(player, thuoctangtruong, 1);

                            Item hoahong = ItemService.gI().createNewItem((short) 589);
                            hoahong.quantity = Util.nextInt(1, 5);
                            InventoryService.gI().addItemBag(player, hoahong);

                            InventoryService.gI().sendItemBags(player);
                            this.npcChat(player, "Chúc mừng! Bạn đã trồng được Hoa hồng.");
                        } else {
                            this.npcChat(player, "Bạn không đủ nguyên liệu để trồng Hoa hồng.");
                        }
                    }
                    case ConstNpc.MAKE_FRIEND -> {
                        if (select == 0) {
                            Object playerId = PLAYERID_OBJECT.get(player.id);
                            if (playerId != null) {
                                try {
                                    FriendAndEnemyService.gI().acceptMakeFriend(player,
                                            Integer.parseInt(String.valueOf(playerId)));
                                } catch (NumberFormatException e) {
                                }
                            }
                        }
                    }
                    case ConstNpc.TUTORIAL_SUMMON_DRAGON -> {
                        if (select == 0) {
                            NpcService.gI().createTutorial(player, -1, SummonDragon.SUMMON_SHENRON_TUTORIAL);
                        }
                    }
                    case ConstNpc.SUMMON_SHENRON -> {
                        if (select == 0) {
                            NpcService.gI().createTutorial(player, -1, SummonDragon.SUMMON_SHENRON_TUTORIAL);
                        } else if (select == 1) {
                            SummonDragon.gI().summonShenron(player);
                        }
                    }
                    case ConstNpc.SUMMON_BLACK_SHENRON -> {
                        if (select == 0) {
                            SummonDragon.gI().summonBlackShenron(player);
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM726 -> {
                        if (select == 0) {
                            SuperDivineWaterService.gI().joinMapThanhThuy(player);
                        }
                    }
                    case ConstNpc.MENU_SIEU_THAN_THUY -> {
                        if (select == 0) {
                            ChangeMapService.gI().changeMap(player, 46, -1, Util.nextInt(300, 400), 408);
                        }
                    }
                    case ConstNpc.TAP_TU_DONG_CONFIRM -> {
                        if (select == 0) {
                            ChangeMapService.gI().changeMapBySpaceShip(player, player.lastMapOffline,
                                    player.lastZoneOffline, player.lastXOffline);
                        }
                    }
                    case ConstNpc.INTRINSIC -> {
                        switch (select) {
                            case 0 -> IntrinsicService.gI().showAllIntrinsic(player);
                            case 1 -> IntrinsicService.gI().showConfirmOpen(player);
                            case 2 -> IntrinsicService.gI().showConfirmOpenVip(player);
                            default -> {
                            }
                        }
                    }
                    case ConstNpc.CONFIRM_OPEN_INTRINSIC -> {
                        if (select == 0) {
                            IntrinsicService.gI().open(player);
                        }
                    }
                    case ConstNpc.CONFIRM_OPEN_INTRINSIC_VIP -> {
                        if (select == 0) {
                            IntrinsicService.gI().openVip(player);
                        }
                    }
                    case ConstNpc.CONFIRM_LEAVE_CLAN -> {
                        if (select == 0) {
                            ClanService.gI().leaveClan(player);
                        }
                    }
                    case ConstNpc.CONFIRM_NHUONG_PC -> {
                        if (select == 0) {
                            ClanService.gI().phongPc(player, (int) PLAYERID_OBJECT.get(player.id));
                        }
                    }

                    case ConstNpc.BAN_PLAYER -> {
                        if (select == 0) {
                            PlayerService.gI().banPlayer((Player) PLAYERID_OBJECT.get(player.id));
                            Service.gI().sendThongBao(player,
                                    "Ban người chơi " + ((Player) PLAYERID_OBJECT.get(player.id)).name + " thành công");
                        }
                    }
                    case ConstNpc.BUFF_PET -> {
                        if (select == 0) {
                            Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                            if (pl.pet == null) {
                                PetService.gI().createNormalPet(pl);
                                Service.gI().sendThongBao(player, "Phát đệ tử cho "
                                        + ((Player) PLAYERID_OBJECT.get(player.id)).name + " thành công");
                            }
                        }
                    }
                    case ConstNpc.OTT -> {
                        if (select < 3) {
                            Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                            player.idMark.setOtt(select);
                            String[] selects = new String[] { "Kéo", "Búa", "Bao", "Hủy" };
                            NpcService.gI().createMenuConMeo(pl, ConstNpc.OTT_ACCEPT, -1,
                                    player.name + " muốn chơi oẳn tù tì với bạn mức cược 5tr.", selects, player);
                        }
                    }
                    case ConstNpc.OTT_ACCEPT -> {
                        if (select < 3) {
                            Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                            if (pl == null) {
                                Service.gI().sendThongBao(player, "Đối thủ không còn online!");
                                PLAYERID_OBJECT.remove(player.id);
                                return;
                            }
                            // Ordered lock chống deadlock khi 2 player cùng thực hiện
                            Player firstLock = player.id < pl.id ? player : pl;
                            Player secondLock = player.id < pl.id ? pl : player;
                            synchronized (firstLock) {
                                synchronized (secondLock) {
                                    int slp1 = pl.idMark.getOtt();
                                    int slp2 = select;
                                    if (slp1 == -1 || slp2 == -1) {
                                        PLAYERID_OBJECT.remove(player.id);
                                        return;
                                    }
                                    long betGold = 5_000_000L;
                                    long rewardGold = 4_800_000L;

                                    // BẮT BUỘC KIỂM TRA SỐ DƯ CẢ 2 BÊN TRÁNH IN TIỀN TỪ SỐ ÂM
                                    if (pl.inventory.gold < betGold) {
                                        Service.gI().sendThongBao(player, pl.name + " không đủ " + Util.format(betGold) + " vàng để cá cược!");
                                        Service.gI().sendThongBao(pl, "Bạn không đủ " + Util.format(betGold) + " vàng để cá cược!");
                                        pl.idMark.setOtt(-1);
                                        PLAYERID_OBJECT.remove(player.id);
                                        return;
                                    }
                                    if (player.inventory.gold < betGold) {
                                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.format(betGold) + " vàng để cá cược!");
                                        Service.gI().sendThongBao(pl, player.name + " không đủ " + Util.format(betGold) + " vàng để cá cược!");
                                        pl.idMark.setOtt(-1);
                                        PLAYERID_OBJECT.remove(player.id);
                                        return;
                                    }

                                    pl.idMark.setOtt(-1);
                                    String[] selects = new String[] { "Kéo", "Búa", "Bao" };
                                    Service.gI().chat(pl, selects[slp1]);
                                    Service.gI().chat(player, selects[slp2]);
                                    Service.gI().sendEffAllPlayer(pl, 1000 + slp1, 1, 2, 1);
                                    Service.gI().sendEffAllPlayer(player, 1000 + slp2, 1, 2, 1);

                                    if (slp1 == slp2) {
                                        Service.gI().sendThongBao(pl, "Hòa!");
                                        Service.gI().sendThongBao(player, "Hòa!");
                                    } else if ((slp1 == 0 && slp2 == 2) || (slp1 == 1 && slp2 == 0) || (slp1 == 2 && slp2 == 1)) {
                                        // pl Thắng, player Thua
                                        player.inventory.subGold(betGold);
                                        pl.inventory.addGold(rewardGold);
                                        Service.gI().sendThongBao(pl, "Thắng! Nhận được " + Util.format(rewardGold) + " vàng");
                                        Service.gI().sendThongBao(player, "Thua! Bị trừ " + Util.format(betGold) + " vàng");
                                    } else {
                                        // player Thắng, pl Thua
                                        pl.inventory.subGold(betGold);
                                        player.inventory.addGold(rewardGold);
                                        Service.gI().sendThongBao(player, "Thắng! Nhận được " + Util.format(rewardGold) + " vàng");
                                        Service.gI().sendThongBao(pl, "Thua! Bị trừ " + Util.format(betGold) + " vàng");
                                    }
                                    Service.gI().sendMoney(pl);
                                    Service.gI().sendMoney(player);
                                    PLAYERID_OBJECT.remove(player.id);
                                    PLAYERID_OBJECT.remove(pl.id);
                                }
                            }
                        }
                    }
                    case ConstNpc.SUB_MENU -> {
                        Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                        switch (select) {
                            case 0 -> SubMenuService.gI().controller(player, (int) pl.id, SubMenuService.OTT);
                            case 1 -> SubMenuService.gI().controller(player, (int) pl.id, SubMenuService.CUU_SAT);
                        }
                    }

                    case ConstNpc.BUY_BACK -> {
                        // if (select == 0) {
                        // Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                        // BuyBackService.gI().buyItem(player, pl);
                        // }
                    }
                    case ConstNpc.MENU_BOT -> {
                        switch (select) {
                            case 0 -> {
                                Input.gI().createFormBotQuai(player);
                            }
                            case 1 -> {
                                Input.gI().createFormBotQuaiNappa(player);
                            }
                            case 2 -> {
                                Input.gI().createFormBotQuaiTuonglai(player);
                            }
                            case 3 -> {
                                Input.gI().createFormBotQuaiCold(player);
                            }
                            case 4 -> {
                                Input.gI().createFormBotBoss(player);
                            }
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM1227 -> {
                        try {
                            ItemService.gI().OpenSKH(player, player.idMark.getIndexMenu(), select);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error("Lỗi mở hộp quà");
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM1228 -> {
                        try {
                            ItemService.gI().OpenSKH(player, player.idMark.getIndexMenu(), select);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error("Lỗi mở hộp quà");
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM1229 -> {
                        try {
                            ItemService.gI().OpenSKH(player, player.idMark.getIndexMenu(), select);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error("Lỗi mở hộp quà");
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM1467 -> {
                        try {
                            ItemService.gI().OpenSKHVIP(player, player.idMark.getIndexMenu(), select);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error("Lỗi mở hộp quà");
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM1468 -> {
                        try {
                            ItemService.gI().OpenSKHVIP(player, player.idMark.getIndexMenu(), select);
                        } catch (Exception e) {

                            e.printStackTrace();
                            Logger.error("Lỗi mở hộp quà");
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM1469 -> {
                        try {
                            ItemService.gI().OpenSKHVIP(player, player.idMark.getIndexMenu(), select);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.error("Lỗi mở hộp quà");
                        }
                    }
                    case ConstNpc.MENU_ADMIN -> {
                        switch (select) {
                            case 0 -> {
                                for (int i = 925; i <= 931; i++) {
                                    Item item = ItemService.gI().createNewItem((short) i);
                                    InventoryService.gI().addItemBag(player, item);
                                }
                                InventoryService.gI().sendItemBags(player);
                            }
                            case 1 -> {
                                if (player.pet == null) {
                                    PetService.gI().createNormalPet(player);
                                }
                            }
                            case 2 -> {
                                if (player.isAdmin()) {
                                    System.out.println(player.name + " Đang bảo trì game!");
                                    Maintenance.gI().start(60);
                                }
                            }
                            case 3 -> Input.gI().createFormFindPlayer(player);
                            case 4 -> BossManager.gI().showListBoss(player);
                            case 5 -> Input.gI().createFormBuffVND(player);
                        }
                    }
                    case ConstNpc.CONFIRM_DISSOLUTION_CLAN -> {
                        switch (select) {
                            case 0 -> {
                                Clan clan = player.clan;
                                clan.deleteDB(clan.id);
                                Manager.CLANS.remove(clan);
                                player.clan = null;
                                player.clanMember = null;
                                ClanService.gI().sendMyClan(player);
                                ClanService.gI().sendClanId(player);
                                Service.gI().sendThongBao(player, "Đã giải tán bang hội.");
                            }
                        }
                    }

                    case ConstNpc.CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND -> {
                        if (select == 0) {
                            synchronized (player.inventory.itemsBoxCrackBall) {
                                player.inventory.itemsBoxCrackBall.clear();
                            }
                            Service.gI().sendThongBao(player, "Đã xóa hết vật phẩm trong rương");
                        }
                    }

                    case ConstNpc.MENU_FIND_PLAYER -> {
                        Player p = (Player) PLAYERID_OBJECT.get(player.id);
                        if (p != null) {
                            switch (select) {
                                case 0 -> {
                                    if (p.zone != null) {
                                        ChangeMapService.gI().changeMapYardrat(player, p.zone, p.location.x,
                                                p.location.y);
                                    }
                                }
                                case 1 -> {
                                    if (p.zone != null) {
                                        ChangeMapService.gI().changeMap(p, player.zone, player.location.x,
                                                player.location.y);
                                    }
                                }
                                case 2 -> Input.gI().createFormChangeName(player, p);
                                case 3 -> {
                                    String[] selects = new String[] { "Đồng ý", "Hủy" };
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.BAN_PLAYER, -1,
                                            "Bạn có chắc chắn muốn ban " + p.name, selects, p);
                                }
                                case 4 -> {
                                    Service.gI().sendThongBao(player, "Kik người chơi " + p.name + " thành công");
                                    Client.gI().getPlayers().remove(p);
                                    Client.gI().kickSession(p.getSession());
                                }
                            }
                        }
                    }
                    case ConstNpc.CONFIRM_TELE_NAMEC -> {
                        if (select == 0) {
                            NgocRongNamecService.gI().teleportToNrNamec(player);
                            player.inventory.subGemAndRuby(50);
                            Service.gI().sendMoney(player);
                        }
                    }
                    case ConstNpc.MA_BAO_VE -> {
                        if (select == 0) {
                            if (player.mbv == 0) {
                                if (player.inventory.gold >= 30000) {
                                    player.inventory.gold -= 30000;
                                    Service.gI().sendMoney(player);
                                    player.mbv = player.idMark.getMbv();
                                    player.baovetaikhoan = true;
                                    Service.gI().sendThongBao(player,
                                            "Kích hoạt thành công, tài khoản đang được bảo vệ");
                                } else {
                                    Service.gI().sendThongBao(player,
                                            "Bạn không đủ tiền để kích hoạt bảo vệ tài khoản");
                                }
                            } else {
                                if (player.baovetaikhoan) {
                                    player.baovetaikhoan = false;
                                    Service.gI().sendThongBao(player, "Chức năng bảo vệ tài khoản đang tắt");
                                } else {
                                    player.baovetaikhoan = true;
                                    Service.gI().sendThongBao(player, "Tài khoản đang được bảo vệ");
                                }
                            }
                        }
                    }
                    case ConstNpc.RUONG_GO -> {
                        int size = player.textRuongGo.size();
                        if (size > 0) {
                            String menuselect = "OK [" + (size - 1) + "]";
                            if (size == 1) {
                                menuselect = "OK";
                            }
                            NpcService.gI().createMenuConMeo(player, ConstNpc.RUONG_GO, -1,
                                    player.textRuongGo.get(size - 1), menuselect);
                            player.textRuongGo.remove(size - 1);
                        }
                    }
                    case ConstNpc.HOP_QUA_THAN_LINH -> {
                        switch (select) {
                            case 0:
                                try {
                                    ItemService.gI().settltd(player);
                                } catch (Exception e) {
                                }
                                break;
                            case 1:
                                try {
                                    ItemService.gI().settlnm(player);
                                } catch (Exception e) {
                                }
                                break;
                            default:
                                try {
                                    ItemService.gI().settlxd(player);
                                } catch (Exception e) {
                                }
                                break;
                        }
                    }

                    case ConstNpc.MENU_XUONG_TANG_DUOI -> {
                        if (player.fightMabu.pointMabu >= player.fightMabu.POINT_MAX && player.zone.map.mapId != 120) {
                            ChangeMapService.gI().changeMap(player,
                                    player.zone.map.mapIdNextMabu((short) player.zone.map.mapId), -1, -1, 100);
                        }
                    }
                      case ConstNpc.UP_TOP_ITEM -> {
                        if (select == 0) {
                            if (player.idMark != null && player.idMark.getIdItemUpTop() != -1) {
                                if (player.inventory.gem >= 5) {
                                    ConsignItem it = ConsignShopManager.gI().getItemById(player.idMark.getIdItemUpTop());
                                    if (it == null || it.isBuy) {
                                        Service.gI().sendThongBao(player, "Vật phẩm không tồn tại hoặc đã được bán");
                                        return;
                                    }
                                    if (it.player_sell != player.id) {
                                        Service.gI().sendThongBao(player, "Vật phẩm không thuộc quyền sở hữu");
                                        ConsignShopService.gI().openShopKyGui(player);
                                        return;
                                    }
                                    player.inventory.gem -= 5;
                                    Service.gI().sendMoney(player);
                                    it.isUpTop = (byte) Math.min(100, it.isUpTop + 1);
                                    ConsignShopManager.gI().updateItemUpTopAsync(it.id, it.isUpTop);
                                    database.daos.PlayerDAO.updatePlayerAsync(player);
                                    Service.gI().sendThongBao(player, "Đưa vật phẩm lên trang đầu thành công!");
                                    ConsignShopService.gI().openShopKyGui(player);
                                } else {
                                    Service.gI().sendThongBao(player, "Bạn không đủ Ngọc Xanh (yêu cầu 5 ngọc)");
                                    player.idMark.setIdItemUpTop(-1);
                                }
                            }
                        }
                    }
                    case ConstNpc.HOC_KY_NANG -> {
                        if (select == 0) {
                            //

                            int level = player.hocKyNang.Level;
                            Skill curSkill = SkillUtil.getSkillByItemID(player, player.hocKyNang.ItemTemplateSkillId);
                            if (curSkill.point == 7) {
                                Service.gI().sendThongBao(player, "Kỹ năng đã đạt tối đa!");
                            } else {
                                Message msg;
                                try {
                                    if (curSkill.point == 0) {
                                        if (level == 1) {
                                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(
                                                    player.hocKyNang.ItemTemplateSkillId), level);
                                            SkillUtil.setSkill(player, curSkill);
                                            msg = Service.gI().messageSubCommand((byte) 23);
                                            msg.writer().writeShort(curSkill.skillId);
                                            player.sendMessage(msg);
                                            msg.cleanup();
                                            player.BoughtSkills.add((int) player.hocKyNang.ItemTemplateSkillId);
                                            player.nPoint.tiemNangUp(-player.hocKyNang.PotentialLearn);
                                            Service.gI().point(player);
                                        } else {
                                            Skill skillNeed = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(
                                                    player.hocKyNang.ItemTemplateSkillId), level);
                                            Service.gI().sendThongBao(player,
                                                    "Vui lòng học " + skillNeed.template.name + " cấp 1 trước!");
                                        }
                                    } else {
                                        if (curSkill.point + 1 == level) {
                                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(
                                                    player.hocKyNang.ItemTemplateSkillId), level);
                                            // System.out.println(curSkill.template.name + " - " + curSkill.point);
                                            SkillUtil.setSkill(player, curSkill);
                                            msg = Service.gI().messageSubCommand((byte) 62);
                                            msg.writer().writeShort(curSkill.skillId);
                                            player.sendMessage(msg);
                                            msg.cleanup();
                                            player.BoughtSkills.add((int) player.hocKyNang.ItemTemplateSkillId);
                                            player.nPoint.tiemNangUp(-player.hocKyNang.PotentialLearn);
                                            Service.gI().point(player);
                                        } else {
                                            Service.gI().sendThongBao(player, "Vui lòng học " + curSkill.template.name
                                                    + " cấp " + (curSkill.point + 1) + " trước!");
                                        }
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    }

                 case ConstNpc.REVENGE -> {
                 if (select == 0) {
                 PVPService.gI().acceptRevenge(player);
                 }
                 }
            }}
        };
        }

}

package services.func;
import network.io.Message;
import consts.ConstNpc;
import database.daos.NDVSqlFetcher;
import database.daos.PlayerDAO;
import models.item.Item;
import java.util.List;
import models.map.Zone;
import models.player.Player;
import server.Client;
import services.player.InventoryService;
import services.player.PlayerService;
import services.ItemService;
import services.map.NpcService;
import services.Service;
import services.ItemTimeService;
import utils.Util;

public class SummonDragonNamek {

    public static final byte DRAGON_PORUNGA = 1;
    private static SummonDragonNamek instance;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;
    private boolean isShenronAppear;
    public Player playerSummonShenron;
    private int playerSummonShenronId;
    private Zone mapShenronAppear;
    private int menuShenron;
    private byte select;
    private final Thread update;
    private boolean active;
    public boolean isPlayerDisconnect;
    private long lastTimeShenronWait;
    private final int timeShenronWait = 300000;

    public static SummonDragonNamek gI() {
        if (instance == null) {
            instance = new SummonDragonNamek();
        }
        return instance;
    }

    private SummonDragonNamek() {
        this.update = new Thread(() -> {
            while (active) {
                try {
                    if (isShenronAppear) {
                        if (isPlayerDisconnect) {
                            List<Player> players = mapShenronAppear.getPlayers();
                            for (Player plMap : players) {
                                if (plMap.isPl() && plMap.id == playerSummonShenronId) {
                                    playerSummonShenron = plMap;
                                    reSummonShenron();
                                    isPlayerDisconnect = false;
                                    break;
                                }
                            }

                        }
                        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        this.active();
    }

    private void active() {
        if (!active) {
            active = true;
            this.update.start();
        }
    }

    public void summonNamec(Player pl) {
        if (pl.zone.map.mapId == 7) {
            playerSummonShenron = pl;
            playerSummonShenronId = (int) pl.id;
            mapShenronAppear = pl.zone;
            lastTimeShenronWait = System.currentTimeMillis();
            sendNotifyShenronNamekAppear();
            activeShenron(pl, true, DRAGON_PORUNGA);
            sendWhishesNamec(pl);
        } else {
            Service.gI().sendThongBaoResetWait(pl, "Rồng thần Namek chỉ có thể triệu hồi tại Làng Mori (Hành tinh Namek)!");
        }
    }

    private void reSummonShenron() {
        activeShenron(playerSummonShenron, true, DRAGON_PORUNGA);
        sendWhishesNamec(playerSummonShenron);
    }

    private void activeShenron(Player pl, boolean appear, byte type) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeUTF(pl.name);
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(type);
                isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayer(msg);
        } catch (Exception e) {
        }
    }

    private void sendNotifyShenronNamekAppear() {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng thần namek tại "
                    + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
            Service.gI().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void confirmWish() {
        switch (this.menuShenron) {
            case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM:
                try {
                    switch (select) {
                        case 0: // Bùa Trí Tuệ (x2 TNSM) 7 ngày toàn bang
                            if (playerSummonShenron.clan != null) {
                                playerSummonShenron.clan.members.forEach(m -> {
                                    Player p = Client.gI().getPlayer(m.id);
                                    if (p != null) {
                                        p.charms.addTimeCharms(213, 10080);
                                        ItemTimeService.gI().sendCanAutoPlay(p);
                                        Service.gI().point(p);
                                        Service.gI().sendThongBao(p, "Bang hội của bạn vừa nhận được 7 ngày Bùa Trí Tuệ (x2 TNSM) từ Rồng Thần Namek!");
                                    } else {
                                        Player pOff = NDVSqlFetcher.loadById(m.id);
                                        if (pOff != null) {
                                            pOff.charms.addTimeCharms(213, 10080);
                                            PlayerDAO.updatePlayer(pOff);
                                        }
                                    }
                                });
                            } else {
                                playerSummonShenron.charms.addTimeCharms(213, 10080);
                                ItemTimeService.gI().sendCanAutoPlay(playerSummonShenron);
                                Service.gI().point(playerSummonShenron);
                                Service.gI().sendThongBaoOK(playerSummonShenron, "Chúc mừng! Bạn nhận được 7 ngày Bùa Trí Tuệ (x2 TNSM) từ Rồng Thần Namek!");
                            }
                            break;
                        case 1: // 50 Hồng ngọc + 3 Đá bảo vệ (Khóa)
                            if (playerSummonShenron.clan != null) {
                                playerSummonShenron.clan.members.forEach(m -> {
                                    Player p = Client.gI().getPlayer(m.id);
                                    if (p != null) {
                                        p.inventory.ruby += 50;
                                        PlayerService.gI().sendInfoHpMpMoney(p);
                                        if (InventoryService.gI().getCountEmptyBag(p) >= 1) {
                                            Item daBaoVe = ItemService.gI().createNewItem((short) 987, 3);
                                            daBaoVe.itemOptions.add(new Item.ItemOption(30, 0));
                                            InventoryService.gI().addItemBag(p, daBaoVe);
                                            InventoryService.gI().sendItemBags(p);
                                            Service.gI().sendThongBaoOK(p, "Chúc mừng! Nhận được 50 Hồng Ngọc và 3 Đá Bảo Vệ từ Rồng Thần Namek!");
                                        } else {
                                            Service.gI().sendThongBaoOK(p, "Hành trang đầy (cần ít nhất 1 ô trống để nhận Đá Bảo Vệ)!");
                                        }
                                    } else {
                                        Player pOff = NDVSqlFetcher.loadById(m.id);
                                        if (pOff != null) {
                                            pOff.inventory.ruby += 50;
                                            if (InventoryService.gI().getCountEmptyBag(pOff) >= 1) {
                                                Item daBaoVe = ItemService.gI().createNewItem((short) 987, 3);
                                                daBaoVe.itemOptions.add(new Item.ItemOption(30, 0));
                                                InventoryService.gI().addItemBag(pOff, daBaoVe);
                                            }
                                            PlayerDAO.updatePlayer(pOff);
                                        }
                                    }
                                });
                            } else {
                                playerSummonShenron.inventory.ruby += 50;
                                PlayerService.gI().sendInfoHpMpMoney(playerSummonShenron);
                                if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) >= 1) {
                                    Item daBaoVe = ItemService.gI().createNewItem((short) 987, 3);
                                    daBaoVe.itemOptions.add(new Item.ItemOption(30, 0));
                                    InventoryService.gI().addItemBag(playerSummonShenron, daBaoVe);
                                    InventoryService.gI().sendItemBags(playerSummonShenron);
                                    Service.gI().sendThongBaoOK(playerSummonShenron, "Chúc mừng! Nhận được 50 Hồng Ngọc và 3 Đá Bảo Vệ từ Rồng Thần Namek!");
                                } else {
                                    Service.gI().sendThongBaoOK(playerSummonShenron, "Hành trang đầy (cần ít nhất 1 ô trống để nhận Đá Bảo Vệ)!");
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        shenronLeave(this.playerSummonShenron, WISHED);
    }

    public void showConfirmShenron(Player pl, int menu, byte select) {
        this.menuShenron = menu;
        this.select = select;
        String wish = null;
        switch (menu) {
            case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM:
                switch (select) {
                    case 0:
                        wish = "Bùa Trí Tuệ (x2 Tiềm Năng Sức Mạnh) 7 ngày cho toàn Bang Hội";
                        break;
                    case 1:
                        wish = "50 Hồng ngọc và 3 Đá bảo vệ (Khóa giao dịch) cho toàn Bang Hội";
                        break;
                }
                break;
        }
        NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_NAMEK_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void sendWhishesNamec(Player pl) {
        NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM,
                "Ta sẽ ban cho cả bang hội ngươi 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định",
                "Bùa Trí Tuệ\nToàn bang\n(7 ngày)",
                "50 Hồng ngọc\n+ Đá bảo vệ\n(Khóa)");
    }

    public void shenronLeave(Player pl, byte type) {
        if (type == WISHED) {
            NpcService.gI().createTutorial(pl, 0, "Điều ước của bang hội ngươi đã được thực hiện. Tạm biệt!");
        } else {
            NpcService.gI().createMenuRongThieng(pl, ConstNpc.IGNORE_MENU, "Thời gian 5 phút đã kết thúc, ta trở về hành tinh Namek đây. Tạm biệt!");
        }
        activeShenron(pl, false, SummonDragon.DRAGON_SHENRON);
        this.isShenronAppear = false;
        this.menuShenron = -1;
        this.select = -1;
        this.playerSummonShenron = null;
        this.playerSummonShenronId = -1;
        this.mapShenronAppear = null;
    }
}

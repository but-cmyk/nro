package models.npc.npc_list;
import models.boss.Boss;
import consts.BossID;
import managers.boss.BossManager;
import consts.ConstNpc;
import consts.ConstTask;
import models.map.Zone;
import models.npc.Npc;
import models.player.Player;
import services.map.MapService;
import services.map.NpcService;
import services.Service;
import services.TaskService;
import services.map.ChangeMapService;
import utils.Util;

public class Cui extends Npc {

    private final int COST_FIND_BOSS = 5;

    public Cui(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (canOpenNpc(pl)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(pl, this)) {
                if (pl.playerTask.taskMain.id == 7) {
                    NpcService.gI().createTutorial(pl, this.avartar, "Hãy lên đường cứu đứa bé nhà tôi\nChắc bây giờ nó đang sợ hãi lắm rồi");
                } else {
                    switch (this.mapId) {
                        case 19 -> {
                            int taskId = TaskService.gI().getIdTask(pl);
                            switch (taskId) {
                                case ConstTask.TASK_21_0 ->
                                    this.createOtherMenu(pl, ConstNpc.MENU_FIND_KUKU,
                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                            "Đến chỗ\nKuku\n(" + Util.powerToString(COST_FIND_BOSS) + " ngọc)", "Đến Cold",
                                            "Đến\nNappa", "Từ chối");
                                case ConstTask.TASK_21_1 ->
                                    this.createOtherMenu(pl, ConstNpc.MENU_FIND_MAP_DAU_DINH,
                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                            "Đến chỗ\nMập đầu đinh\n(" + Util.powerToString(COST_FIND_BOSS) + " ngọc)",
                                            "Đến Cold", "Đến\nNappa", "Từ chối");
                                case ConstTask.TASK_21_2 ->
                                    this.createOtherMenu(pl, ConstNpc.MENU_FIND_RAMBO,
                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                            "Đến chỗ\nRambo\n(" + Util.powerToString(COST_FIND_BOSS) + " ngọc)", "Đến Cold",
                                            "Đến\nNappa", "Từ chối");
                                default ->
                                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                            "Đội quân của Fide đang ở Thung lũng Nappa, ta sẽ đưa ngươi đến đó",
                                            "Đến Cold", "Đến\nNappa", "Từ chối");
                            }
                        }
                        case 68 ->
                            this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                    "Ngươi muốn về Thành Phố Vegeta", "Đồng ý", "Từ chối");
                        default ->
                            this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                                    "Tàu vũ trụ Xayda sử dụng công nghệ mới nhất, "
                                    + "có thể đưa ngươi đi bất kỳ đâu, chỉ cần trả tiền là được.",
                                    "Đến\nTrái Đất", "Đến\nNamếc", "Siêu thị");
                    }
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 26) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 24, -1, -1);
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 25, -1, -1);
                        case 2 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 84, -1, -1);
                    }
                }
            }
            if (this.mapId == 19) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_FIND_KUKU) {
                    switch (select) {
                        case 0 -> {
                            Boss boss = BossManager.gI().getBossById(BossID.KUKU);
                            if (boss != null && !boss.isDie() && boss.zone != null && !MapService.gI().isMapPhoBan(boss.zone.map.mapId)) {
                                if (player.inventory.gem >= COST_FIND_BOSS) {
                                    Zone z = MapService.gI().getMapCanJoin(player, boss.zone.map.mapId, boss.zone.zoneId);
                                    if (z.getNumOfPlayers() < z.maxPlayer) {
                                        player.inventory.gem -= COST_FIND_BOSS;
                                        ChangeMapService.gI().changeMap(player, boss.zone, boss.location.x, boss.location.y);
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Khu vực đã đầy!");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không đủ ngọc, còn thiếu "
                                            + Util.powerToString(COST_FIND_BOSS - player.inventory.gem) + " ngọc");
                                }
                                break;
                            }
                            Service.gI().sendThongBao(player, "Boss kuku chưa xuất hiện!");
                        }
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                        case 2 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_FIND_MAP_DAU_DINH) {
                    switch (select) {
                        case 0 -> {
                            Boss boss = BossManager.gI().getBossById(BossID.MAP_DAU_DINH);
                            if (boss != null && !boss.isDie() && boss.zone != null && !MapService.gI().isMapPhoBan(boss.zone.map.mapId)) {
                                if (player.inventory.gem >= COST_FIND_BOSS) {
                                    Zone z = MapService.gI().getMapCanJoin(player, boss.zone.map.mapId, boss.zone.zoneId);
                                    if (z.getNumOfPlayers() < z.maxPlayer) {
                                        player.inventory.gem -= COST_FIND_BOSS;
                                        ChangeMapService.gI().changeMap(player, boss.zone, boss.location.x, boss.location.y);
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Khu vực đã đầy!.");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không đủ ngọc, còn thiếu "
                                            + Util.powerToString(COST_FIND_BOSS - player.inventory.gem) + " ngọc");
                                }
                                break;
                            }
                            Service.gI().sendThongBao(player, "Boss mập đầu đinh chưa xuất hiện!");
                        }
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                        case 2 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_FIND_RAMBO) {
                    switch (select) {
                        case 0 -> {
                            Boss boss = BossManager.gI().getBossById(BossID.RAMBO);
                            if (boss != null && !boss.isDie() && boss.zone != null && !MapService.gI().isMapPhoBan(boss.zone.map.mapId)) {
                                if (player.inventory.gem >= COST_FIND_BOSS) {
                                    Zone z = MapService.gI().getMapCanJoin(player, boss.zone.map.mapId, boss.zone.zoneId);
                                    if (z.getNumOfPlayers() < z.maxPlayer) {
                                        player.inventory.gem -= COST_FIND_BOSS;
                                        ChangeMapService.gI().changeMap(player, boss.zone, boss.location.x, boss.location.y);
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Khu vực đã đầy!");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không đủ ngọc, còn thiếu "
                                            + Util.powerToString(COST_FIND_BOSS - player.inventory.gem) + " ngọc");
                                }
                                break;
                            }
                            Service.gI().sendThongBao(player, "Boss rambo chưa xuất hiện!");
                        }
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 109, -1, 295);
                        case 2 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 68, -1, 90);
                    }
                }
            }
            if (this.mapId == 68) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 19, -1, 1100);
                    }
                }
            }
        }
    }
}

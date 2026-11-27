package services;

import consts.ConstItem; // Import class ConstItem bạn đã cung cấp
import consts.ConstMob;
import consts.ConstNpc;
import consts.ConstPlayer;
import models.player.Player;
import models.player.Pet;
import consts.ConstTask;
import models.boss.Boss;
import consts.BossID;
import models.clan.ClanMember;
import models.item.Item;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.map.ItemMap;
import models.map.Zone;
import models.mob.Mob;
import models.npc.Npc;
import models.task.SideTask;
import models.task.SideTaskTemplate;
import models.task.SubTaskMain;
import models.task.TaskMain;
import server.Manager;
import network.io.Message;
import services.map.MapService;
import services.map.NpcService;
import services.player.ClanService;
import services.player.InventoryService;
import utils.Logger;
import utils.PlayerAuditLogger;
import utils.Util;
import server.Client;
import models.task.ClanTask;
import models.task.ClanTaskTemplate;
import services.phoban.TrainingService;
import services.player.PlayerService;

public abstract class TaskServiceBase1 {
    protected abstract String transformName(Player player, String text);
    public abstract void checkDoneSideTaskArena(Player player);
    public abstract void checkDoneSideTaskDungeon(Player player, int dungeonTaskId);
    public abstract void checkDoneSideTaskKillBoss(Player player, Boss boss);

    public TaskMain getTaskMainById(Player player, int id) {
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == id) {
                TaskMain newTaskMain = new TaskMain(tm);
                newTaskMain.detail = transformName(player, newTaskMain.detail);
                for (SubTaskMain stm : newTaskMain.subTasks) {
                    stm.mapId = (short) transformMapId(player, stm.mapId);
                    stm.npcId = (byte) transformNpcId(player, stm.npcId);
                    stm.notify = transformName(player, stm.notify);
                    stm.name = transformName(player, stm.name);
                }
                return newTaskMain;
            }
        }
        return player.playerTask.taskMain;
    }

    //gửi thông tin nhiệm vụ chính
    public void sendTaskMain(Player player) {
        Message msg = null;
        try {
            if (player == null || player.playerTask == null || player.playerTask.taskMain == null
                    || player.playerTask.taskMain.subTasks == null || player.playerTask.taskMain.subTasks.isEmpty()) {
                return;
            }
            if (player.playerTask.taskMain.id > 3) {
                Item duaBe = InventoryService.gI().findItemBagByTemp(player, ConstItem.DUA_BE);
                if (duaBe != null) {
                    InventoryService.gI().subQuantityItemsBag(player, duaBe, duaBe.quantity);
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendFlagBag(player);
                }
            }
            TaskMain tm = player.playerTask.taskMain;
            if (tm.index < 0) {
                tm.index = 0;
            }
            if (tm.index >= tm.subTasks.size()) {
                tm.index = tm.subTasks.size() - 1;
            }
            msg = new Message(40);
            msg.writer().writeShort(tm.id);
            msg.writer().writeByte(tm.index);
            msg.writer().writeUTF(tm.name + "[" + tm.id + "]");
            msg.writer().writeUTF(tm.detail != null ? tm.detail.replace("\\r\\n", "\n").replace("\\n", "\n") : "");
            msg.writer().writeByte(tm.subTasks.size());
            for (SubTaskMain stm : tm.subTasks) {
                msg.writer().writeUTF(stm.name != null ? stm.name : "");
                msg.writer().writeByte(stm.npcId);
                msg.writer().writeShort(stm.mapId);
                msg.writer().writeUTF(stm.notify != null ? stm.notify : "");
            }
            msg.writer().writeShort(tm.subTasks.get(tm.index).count);
            for (SubTaskMain stm : tm.subTasks) {
                msg.writer().writeShort(stm.maxCount);
            }
            player.sendMessage(msg);
            this.sendUpdateCountSubTask(player);
        } catch (Exception e) {
            Logger.logException(TaskService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    //chuyển sang task mới
    public void sendNextTaskMain(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return;
        }
        rewardDoneTask(player);
        if (player.playerTask != null) {
            player.playerTask.isJustDoneMainTask = true;
        }
        int nextTaskId = switch (player.playerTask.taskMain.id) {
            case 3 -> player.gender + 4;
            case 4, 5, 6 -> 7;
            default -> player.playerTask.taskMain.id + 1;
        };
        TaskMain nextTask = getTaskMainById(player, nextTaskId);
        if (nextTask != null && nextTask != player.playerTask.taskMain && nextTask.id == nextTaskId) {
            player.playerTask.taskMain = nextTask;
            sendTaskMain(player);
            if (player.playerTask.taskMain.subTasks != null && !player.playerTask.taskMain.subTasks.isEmpty()
                    && player.playerTask.taskMain.index >= 0 && player.playerTask.taskMain.index < player.playerTask.taskMain.subTasks.size()) {
                Service.gI().sendThongBao(player, "Nhiệm vụ tiếp theo của bạn là "
                        + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
            }
            if (player.nPoint != null) {
                checkDoneTaskPower(player, player.nPoint.power);
            }
            Service.gI().point(player);
        } else {
            // Đã đạt mốc nhiệm vụ tối đa của server
            if (player.playerTask.taskMain.subTasks != null && !player.playerTask.taskMain.subTasks.isEmpty()) {
                player.playerTask.taskMain.index = player.playerTask.taskMain.subTasks.size() - 1;
                SubTaskMain lastSub = player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index);
                lastSub.count = lastSub.maxCount;
            }
            sendTaskMain(player);
            Service.gI().sendThongBao(player, "Chúc mừng bạn đã hoàn thành tất cả nhiệm vụ chính tuyến hiện tại!");
        }
    }

    //số lượng đã hoàn thành
    public void sendUpdateCountSubTask(Player player) {
        Message msg = null;
        try {
            if (player == null || player.playerTask == null || player.playerTask.taskMain == null
                    || player.playerTask.taskMain.subTasks == null || player.playerTask.taskMain.subTasks.isEmpty()) {
                return;
            }
            TaskMain tm = player.playerTask.taskMain;
            if (tm.index < 0) {
                tm.index = 0;
            }
            if (tm.index >= tm.subTasks.size()) {
                tm.index = tm.subTasks.size() - 1;
            }
            msg = new Message(43);
            SubTaskMain stm = tm.subTasks.get(tm.index);
            msg.writer().writeShort(stm.count);
            
            short x_hint = 0;
            short y_hint = 0;
            if (tm.id <= 2 && player != null && player.zone != null && player.zone.map != null) {
                if (stm.mapId == player.zone.map.mapId) {
                    if (stm.npcId != -1) {
                        for (Npc npc : Manager.NPCS) {
                            if (npc.mapId == stm.mapId && npc.tempId == stm.npcId) {
                                x_hint = (short) npc.cx;
                                y_hint = (short) npc.cy;
                                break;
                            }
                        }
                    } else if (player.zone.mobs != null) {
                        for (models.mob.Mob mob : player.zone.mobs) {
                            if (mob != null && !mob.isDie() && mob.location != null) {
                                x_hint = (short) mob.location.x;
                                y_hint = (short) mob.location.y;
                                break;
                            }
                        }
                    }
                } else if (player.zone.map.wayPoints != null) {
                    for (models.map.WayPoint wp : player.zone.map.wayPoints) {
                        if (wp != null && wp.goMap == stm.mapId) {
                            x_hint = (short) ((wp.minX + wp.maxX) / 2);
                            y_hint = (short) wp.maxY;
                            break;
                        }
                    }
                }
            }
            msg.writer().writeShort(x_hint);
            msg.writer().writeShort(y_hint);
            
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(TaskService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    //chuyển sub task tiếp theo
    public void sendNextSubTask(Player player) {
        Message msg = null;
        try {
            msg = new Message(41);
            player.sendMessage(msg);
            Service.gI().sendThongBao(player, "Nhiệm vụ tiếp theo: "
                    + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
        } catch (Exception e) {
            Logger.logException(TaskService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendInfoCurrentTask(Player player) {
        Service.gI().sendThongBao(player, "Nhiệm vụ hiện tại của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    public boolean checkDoneTaskTalkNpc(Player player, Npc npc) {
        if (checkPendingNpcSay(player, npc)) {
            return true;
        }
        return switch (npc.tempId) {
            case ConstNpc.QUY_LAO_KAME ->
                player.gender == ConstPlayer.TRAI_DAT && (doneTask(player, ConstTask.TASK_11_0)
                || (player.clan != null && player.clan.getMembers().size() >= 1 && doneTask(player, ConstTask.TASK_12_0))
                || doneTask(player, ConstTask.TASK_12_1)
                || doneTask(player, ConstTask.TASK_13_3)
                || doneTask(player, ConstTask.TASK_14_2)
                || doneTask(player, ConstTask.TASK_15_4)
                || doneTask(player, ConstTask.TASK_16_1)
                || doneTask(player, ConstTask.TASK_17_4)
                || doneTask(player, ConstTask.TASK_18_2)
                || doneTask(player, ConstTask.TASK_20_6)
                || doneTask(player, ConstTask.TASK_21_3)
                || doneTask(player, ConstTask.TASK_22_4)
                || doneTask(player, ConstTask.TASK_23_3)
                || doneTask(player, ConstTask.TASK_24_2)
                || doneTask(player, ConstTask.TASK_19_2));
            case ConstNpc.TRUONG_LAO_GURU ->
                player.gender == ConstPlayer.NAMEC && (doneTask(player, ConstTask.TASK_11_0)
                || (player.clan != null && player.clan.getMembers().size() >= 1 && doneTask(player, ConstTask.TASK_12_0))
                || doneTask(player, ConstTask.TASK_12_1)
                || doneTask(player, ConstTask.TASK_13_3)
                || doneTask(player, ConstTask.TASK_14_2)
                || doneTask(player, ConstTask.TASK_15_4)
                || doneTask(player, ConstTask.TASK_16_1)
                || doneTask(player, ConstTask.TASK_17_4)
                || doneTask(player, ConstTask.TASK_18_2)
                || doneTask(player, ConstTask.TASK_20_6)
                || doneTask(player, ConstTask.TASK_21_3)
                || doneTask(player, ConstTask.TASK_22_4)
                || doneTask(player, ConstTask.TASK_23_3)
                || doneTask(player, ConstTask.TASK_19_2));
            case ConstNpc.VUA_VEGETA ->
                player.gender == ConstPlayer.XAYDA && (doneTask(player, ConstTask.TASK_11_0)
                || (player.clan != null && player.clan.getMembers().size() >= 1 && doneTask(player, ConstTask.TASK_12_0))
                || doneTask(player, ConstTask.TASK_12_1)
                || doneTask(player, ConstTask.TASK_13_3)
                || doneTask(player, ConstTask.TASK_15_4)
                || doneTask(player, ConstTask.TASK_14_2)
                || doneTask(player, ConstTask.TASK_16_1)
                || doneTask(player, ConstTask.TASK_18_2)
                || doneTask(player, ConstTask.TASK_17_4)
                || doneTask(player, ConstTask.TASK_20_6)
                || doneTask(player, ConstTask.TASK_21_3)
                || doneTask(player, ConstTask.TASK_22_4)
                || doneTask(player, ConstTask.TASK_23_3)
                || doneTask(player, ConstTask.TASK_19_2));
            case ConstNpc.ONG_GOHAN, ConstNpc.ONG_MOORI, ConstNpc.ONG_PARAGUS -> {
                if (player.playerTask != null && player.playerTask.taskMain != null
                        && player.playerTask.taskMain.id == 2 && player.playerTask.taskMain.index == 0) {
                    Item chickenLeg = InventoryService.gI().findItemBagByTemp(player, ConstItem.DUI_GA);
                    if (chickenLeg != null && chickenLeg.quantity >= 10) {
                        int cur = player.playerTask.taskMain.subTasks.get(0).count;
                        for (int i = cur; i < 10; i++) {
                            doneTask(player, ConstTask.TASK_2_0);
                        }
                    }
                }
                if (player.playerTask != null && player.playerTask.taskMain != null
                        && player.playerTask.taskMain.id > 3) {
                    Item duaBe = InventoryService.gI().findItemBagByTemp(player, ConstItem.DUA_BE);
                    if (duaBe != null) {
                        InventoryService.gI().subQuantityItemsBag(player, duaBe, duaBe.quantity);
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendFlagBag(player);
                    }
                }
                yield (doneTask(player, ConstTask.TASK_0_2)
                || doneTask(player, ConstTask.TASK_0_5)
                || doneTask(player, ConstTask.TASK_1_1)
                || doneTask(player, ConstTask.TASK_2_1)
                || doneTask(player, ConstTask.TASK_3_2)
                || doneTask(player, ConstTask.TASK_4_3)
                || doneTask(player, ConstTask.TASK_5_3)
                || doneTask(player, ConstTask.TASK_6_3)
                || doneTask(player, ConstTask.TASK_7_3)
                || doneTask(player, ConstTask.TASK_7_2)
                || doneTask(player, ConstTask.TASK_8_2)
                || doneTask(player, ConstTask.TASK_10_3)
                || doneTask(player, ConstTask.TASK_11_1)
                || doneTask(player, ConstTask.TASK_24_0));
            }
            case ConstNpc.BO_MONG ->
                (doneTask(player, ConstTask.TASK_9_0)
                || doneTask(player, ConstTask.TASK_10_2));
            case ConstNpc.BARDOCK ->
                (doneTask(player, ConstTask.TASK_31_2)
                || doneTask(player, ConstTask.TASK_31_4)
                || doneTask(player, ConstTask.TASK_31_8));
            case ConstNpc.BERRY ->
                (doneTask(player, ConstTask.TASK_31_5)
                || doneTask(player, ConstTask.TASK_32_1));
            case ConstNpc.DR_DRIEF, ConstNpc.CARGO, ConstNpc.CUI ->
                false;
            case ConstNpc.BUNMA, ConstNpc.DENDE, ConstNpc.APPULE ->
                doneTask(player, ConstTask.TASK_7_2);
            case ConstNpc.BUNMA_TL ->
                (doneTask(player, ConstTask.TASK_24_3)
                || doneTask(player, ConstTask.TASK_24_5)
                || doneTask(player, ConstTask.TASK_25_4)
                || doneTask(player, ConstTask.TASK_26_4)
                || doneTask(player, ConstTask.TASK_27_5)
                || doneTask(player, ConstTask.TASK_28_5)
                || doneTask(player, ConstTask.TASK_29_5));
            case ConstNpc.CALICK ->
                doneTask(player, ConstTask.TASK_24_1);
            case ConstNpc.THAN_MEO_KARIN -> {
                if (player.playerTask.taskMain.id == 29) {
                    if (player.nPoint.dameg >= 10000) {
                        yield doneTask(player, ConstTask.TASK_29_0);
                    } else {
                        Service.gI().sendThongBao(player, "Sức đánh gốc của con chưa đạt 10.000! Hãy luyện tập nâng thêm rồi quay lại gặp ta.");
                        yield false;
                    }
                }
                yield doneTask(player, ConstTask.TASK_9_3);
            }
            case ConstNpc.OSIN ->
                doneTask(player, ConstTask.TASK_30_0)
                || doneTask(player, ConstTask.TASK_30_7);
            default ->
                false;
        };
    }

    //kiểm tra hoàn thành nhiệm vụ gia nhập bang hội
    public void checkDoneTaskJoinClan(Player player) {
        if (!player.isBoss && !player.isPet) {
            if (player.clan != null && player.clan.getMembers().size() >= 1) {
                doneTask(player, ConstTask.TASK_12_0);
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ lấy item từ rương
    public void checkDoneTaskGetItemBox(Player player) {
        if (!player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_0_3);
        }
    }

    //kiểm tra hoàn thành nhiệm vụ sức mạnh
    public void checkDoneTaskPower(Player player, long power) {
        if (!player.isBoss && !player.isPet) {
            if (power >= 16000) {
                doneTask(player, ConstTask.TASK_7_0);
            }
            if (power >= 40000) {
                doneTask(player, ConstTask.TASK_8_0);
            }
            if (power >= 200000) {
                doneTask(player, ConstTask.TASK_14_0);
            }
            if (power >= 500000) {
                doneTask(player, ConstTask.TASK_15_0);
            }
            if (power >= 1500000) {
                doneTask(player, ConstTask.TASK_17_0);
            }
            if (power >= 5000000) {
                doneTask(player, ConstTask.TASK_18_0);
            }
            if (power >= 50000000) {
                doneTask(player, ConstTask.TASK_20_0);
            }
            if (power >= 15000000) {
                doneTask(player, ConstTask.TASK_19_0);
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi player sử dụng tiềm năng
    public void checkDoneTaskUseTiemNang(Player player) {
        if (!player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_3_0);
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi player sử dụng item
    public void checkDoneTaskUseItem(Player player, Item item) {
        if (player.isPl() && item.isNotNullItem()) {
            if (item.template.id == ConstItem.NHAN_THOI_KHONG_SAI_LECH) { // Sử dụng const thay vì 992
                doneTask(player, ConstTask.TASK_31_1);
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi vào map nào đó
    public void checkDoneTaskGoToMap(Player player, Zone zoneJoin) {
        if (!player.isBoss && !player.isPet) {
            if (player.clan != null && player.clan.getMembers().size() >= 1) {
                checkDoneTaskJoinClan(player);
            }
            if (zoneJoin != null) {
                switch (zoneJoin.map.mapId) {
                    case 39:
                    case 40:
                    case 41:
                        if (player.location.x >= 362) {
                            doneTask(player, ConstTask.TASK_0_0);
                        }
                        break;
                    case 21:
                    case 22:
                    case 23:
                        doneTask(player, ConstTask.TASK_0_1);
                        break;
                    case 47:
                        doneTask(player, ConstTask.TASK_8_3);
                        break;
                    case 93:
                        doneTask(player, ConstTask.TASK_25_0);
                        break;
                    case 161:
                        doneTask(player, ConstTask.TASK_32_0);
                        break;
                    case 104:
                        doneTask(player, ConstTask.TASK_26_0);
                        break;
                    case 97:
                        doneTask(player, ConstTask.TASK_27_0);
                        break;
                    case 100:
                        doneTask(player, ConstTask.TASK_28_0);
                        break;
                    case 103:
                        doneTask(player, ConstTask.TASK_29_2);
                        break;
                    case 114:
                        doneTask(player, ConstTask.TASK_30_0);
                        break;
                    case 46:
                        doneTask(player, ConstTask.TASK_9_2);
                        break;
                }
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi nhặt item
    public void checkDoneTaskPickItem(Player player, ItemMap item) {
        if (!player.isBoss && !player.isPet && item != null && item.itemTemplate != null) {
            switch (item.itemTemplate.id) {
                case ConstItem.DUI_GA:
                case ConstItem.DUI_GA_NUONG:
                    if (TaskService.gI().isCurrentTask(player, ConstTask.TASK_2_0)) {
                        Item chickenLeg = InventoryService.gI().findItemBagByTemp(player, ConstItem.DUI_GA);
                        int bagCount = chickenLeg != null ? chickenLeg.quantity : 0;
                        int currentCount = player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count;
                        int targetCount = Math.max(currentCount + 1, bagCount);
                        int toAdd = Math.min(targetCount, 10) - currentCount;
                        if (toAdd <= 0) {
                            toAdd = 1;
                        }
                        for (int i = 0; i < toAdd; i++) {
                            doneTask(player, ConstTask.TASK_2_0);
                        }
                    }
                    break;
                case ConstItem.DUA_BE:
                    doneTask(player, ConstTask.TASK_3_1);
                    Service.gI().sendFlagBag(player);
                    break;
                case ConstItem.NGOC_RONG_7_SAO:
                    doneTask(player, ConstTask.TASK_8_1);
                    break;
                case ConstItem.TRUYEN_TRANH:
                    doneTask(player, ConstTask.TASK_14_1);
                    break;
                case ConstItem.VIEN_CAPSULE_KI_BI:
                    doneTask(player, ConstTask.TASK_29_1);
                    break;
                case ConstItem.GIO_THUC_AN:
                    doneTask(player, ConstTask.TASK_31_6);
                    break;
                case ConstItem.NHAN_THOI_KHONG_SAI_LECH:
                    doneTask(player, ConstTask.TASK_31_0);
                    break;
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi xác nhận menu npc nào đó
    public void checkDoneTaskConfirmMenuNpc(Player player, Npc npc, byte select) {
        if (!player.isBoss && !player.isPet) {
            if (npc.tempId == ConstNpc.DAU_THAN) {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.MAGIC_TREE_NON_UPGRADE_LEFT_PEA:
                    case ConstNpc.MAGIC_TREE_NON_UPGRADE_FULL_PEA:
                        if (select == 0) {
                            doneTask(player, ConstTask.TASK_0_4);
                        }
                }
            }
        }
    }

    public void checkDoneTaskKillPlayer(Player player) {
    }

    // Kiểm tra hoàn thành nhiệm vụ hàng ngày
    public void checkDoneTaskSideTask(Player player) {
    }

    // Kiểm tra hoàn thành nhiệm vụ vô địch Võ Đài Sinh Tử tại Bà Hạt Mít ở Đảo Kame (Task 16)
    public void checkDoneTaskVoDaiSinhTu(Player player) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_16_0);
        }
    }

    // Kiểm tra hoàn thành nhiệm vụ tiêu diệt Trung Úy Trại Độc Nhãn (Task 19)
    // Hỗ trợ: khi 5 thành viên trong bang đi doanh trại, chỉ cần 1 thành viên bất kỳ hoặc đệ tử tiêu diệt Boss thì các thành viên còn lại cũng được hoàn thành nhiệm vụ
    public void checkDoneTaskTrungUy(Player player, Boss boss) {
        player = getMasterIfPet(player);
        if (player == null || player.isBoss || player.isPet) {
            return;
        }

        // 1. Hoàn thành cho người trực tiếp tiêu diệt (hoặc sư phụ của đệ tử)
        doneTask(player, ConstTask.TASK_19_1);

        // 2. Chia sẻ hoàn thành nhiệm vụ cho tất cả các thành viên trong bang đi Doanh trại
        if (player.clan != null) {
            if (player.clan.doanhTrai != null && player.clan.doanhTrai.zones != null) {
                for (Zone z : player.clan.doanhTrai.zones) {
                    if (z != null) {
                        for (Player pl : z.getPlayers()) {
                            if (pl != null && !pl.isBoss && !pl.isPet && pl.clan != null && pl.clan.id == player.clan.id) {
                                if (doneTask(pl, ConstTask.TASK_19_1)) {
                                    Service.gI().sendThongBao(pl, "Đồng đội đã tiêu diệt Trung Úy! Bạn đã hoàn thành nhiệm vụ tiêu diệt Trung Úy.");
                                }
                            }
                        }
                    }
                }
            } else if (boss != null && boss.zone != null) {
                for (Player pl : boss.zone.getPlayers()) {
                    if (pl != null && !pl.isBoss && !pl.isPet && pl.clan != null && pl.clan.id == player.clan.id) {
                        if (doneTask(pl, ConstTask.TASK_19_1)) {
                            Service.gI().sendThongBao(pl, "Đồng đội đã tiêu diệt Trung Úy! Bạn đã hoàn thành nhiệm vụ tiêu diệt Trung Úy.");
                        }
                    }
                }
            }
        }
    }

    // Kiểm tra hoàn thành nhiệm vụ thắng giải ĐHVT (Task 18: ĐHVT 23, Siêu Hạng, Siêu Cấp...)
    // =========================================================================
    // DANH HIỆU (TITLES & ACHIEVEMENTS)
    // =========================================================================

    /**
     * Tiến trình danh hiệu [Ông thần ve chai] (ID: 1295, yêu cầu: 500)
     * Kích hoạt khi: Người chơi bán đồ vào Shop HOẶC nhặt đồ trên đất.
     */
    public void checkDoneTaskVeChai(Player player, int amount) {
        player = getMasterIfPet(player);
        if (player == null || player.isBoss || player.isPet) return;
        if (player.playerTask == null || player.playerTask.taskdh == null) return;

        if (player.playerTask.taskdh.NhatDo < 500) {
            player.playerTask.taskdh.addVeChai(amount);
            if (player.effect != null) {
                player.effect.setPointOngThanVeChai(player.playerTask.taskdh.NhatDo);
            }
            if (player.playerTask.taskdh.NhatDo >= 500) {
                Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã hoàn thành danh hiệu [Ông thần ve chai] (500/500). Đến Santa để nhận!");
            }
        }
    }

    /**
     * Tiến trình danh hiệu [Đại gia mới nhú] (ID: 1289, yêu cầu: 5000 ngọc)
     */
    public void checkDoneTaskNapNgoc(Player player, int ngoc) {
        player = getMasterIfPet(player);
        if (player == null || player.isBoss || player.isPet || ngoc <= 0) return;
        if (player.playerTask == null || player.playerTask.taskdh == null) return;

        if (player.playerTask.taskdh.Nap < 5000) {
            player.playerTask.taskdh.addNap(ngoc);
            if (player.effect != null) {
                player.effect.setPointDaiGiaMoiNhu(player.playerTask.taskdh.Nap);
            }
            if (player.playerTask.taskdh.Nap >= 5000) {
                Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã hoàn thành danh hiệu [Đại gia mới nhú] (5000/5000). Đến Santa để nhận!");
            }
        }
    }

    /**
     * Tiến trình danh hiệu [Cao thủ siêu hạng] (ID: 1293, yêu cầu: 3)
     */
    public void checkDoneTaskSieuHang(Player player) {
        player = getMasterIfPet(player);
        if (player == null || player.isBoss || player.isPet) return;
        if (player.playerTask == null || player.playerTask.taskdh == null) return;

        if (player.playerTask.taskdh.SieuHang < 3) {
            player.playerTask.taskdh.addSieuHang();
            if (player.playerTask.taskdh.SieuHang >= 3) {
                Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã hoàn thành danh hiệu [Cao thủ siêu hạng] (3/3). Đến Santa để nhận!");
            }
        }
    }

    /**
     * Tiến trình danh hiệu dùng Loa Thế Giới (yêu cầu: 10 trong ngày)
     */
    public void checkDoneTaskDungLoa(Player player) {
        player = getMasterIfPet(player);
        if (player == null || player.isBoss || player.isPet) return;
        if (player.playerTask == null || player.playerTask.taskdh == null) return;

        player.playerTask.taskdh.addDungLoa();
    }

    public void checkDoneTaskDHVT(Player player) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_18_1);
            checkDoneSideTaskArena(player);
        }
    }

    public Player getMasterIfPet(Player player) {
        if (player != null && player.isPet && player instanceof Pet && ((Pet) player).master != null) {
            return ((Pet) player).master;
        }
        return player;
    }

    //kiểm tra hoàn thành nhiệm vụ khi tiêu diệt được boss (hỗ trợ cả khi đệ tử tiêu diệt Boss)
    public void checkDoneTaskKillBoss(Player player, Boss boss) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet) {
            // Hook hoàn thành SideTask tiêu diệt Boss thế giới (ID 64)
            checkDoneSideTaskKillBoss(player, boss);

            // Hook hoàn thành SideTask phó bản (Doanh trại 62, Kho báu/Gas 63)
            if (boss.zone != null && boss.zone.map != null) {
                int mapId = boss.zone.map.mapId;
                if (MapService.gI().isMapDoanhTrai(mapId)) {
                    checkDoneSideTaskDungeon(player, 62);
                } else if (MapService.gI().isMapBanDoKhoBau(mapId) || MapService.gI().isMapKhiGasHuyDiet(mapId)) {
                    checkDoneSideTaskDungeon(player, 63);
                }
            }
            switch ((int) boss.id) {
                case BossID.TRUNG_UY_TRANG, BossID.TRUNG_UY_THEP, BossID.TRUNG_UY_XANH_LO:
                    checkDoneTaskTrungUy(player, boss);
                    break;
                case BossID.KUKU:
                    doneTask(player, ConstTask.TASK_21_0);
                    break;
                case BossID.MAP_DAU_DINH:
                    doneTask(player, ConstTask.TASK_21_1);
                    break;
                case BossID.RAMBO:
                    doneTask(player, ConstTask.TASK_21_2);
                    break;
                case BossID.SO_4:
                    doneTask(player, ConstTask.TASK_22_0);
                    break;
                case BossID.SO_3:
                    doneTask(player, ConstTask.TASK_22_1);
                    break;
                case BossID.SO_1:
                    doneTask(player, ConstTask.TASK_22_2);
                    break;
                case BossID.TIEU_DOI_TRUONG:
                    doneTask(player, ConstTask.TASK_22_3);
                    break;
                case BossID.FIDE:
                    switch (boss.currentLevel) {
                        case 0 ->
                            doneTask(player, ConstTask.TASK_23_0);
                        case 1 ->
                            doneTask(player, ConstTask.TASK_23_1);
                        case 2 ->
                            doneTask(player, ConstTask.TASK_23_2);
                    }
                    break;
                case BossID.ANDROID_19:
                    doneTask(player, ConstTask.TASK_25_1);
                    break;
                case BossID.DR_KORE:
                    doneTask(player, ConstTask.TASK_25_2);
                    break;
                case BossID.ANDROID_15:
                    doneTask(player, ConstTask.TASK_26_1);
                    break;
                case BossID.ANDROID_14:
                    doneTask(player, ConstTask.TASK_26_2);
                    break;
                case BossID.ANDROID_13:
                    doneTask(player, ConstTask.TASK_26_3);
                    break;
                case BossID.POC:
                    doneTask(player, ConstTask.TASK_27_1);
                    break;
                case BossID.PIC:
                    doneTask(player, ConstTask.TASK_27_2);
                    break;
                case BossID.KING_KONG:
                    doneTask(player, ConstTask.TASK_27_3);
                    break;
                case BossID.XEN_BO_HUNG:
                    switch (boss.currentLevel) {
                        case 0 ->
                            doneTask(player, ConstTask.TASK_28_1);
                        case 1 ->
                            doneTask(player, ConstTask.TASK_28_2);
                        case 2 ->
                            doneTask(player, ConstTask.TASK_28_3);
                    }
                    break;
                case BossID.XEN_CON_1, BossID.XEN_CON_2, BossID.XEN_CON_3, BossID.XEN_CON_4, BossID.XEN_CON_5, BossID.XEN_CON_6, BossID.XEN_CON_7:
                    doneTask(player, ConstTask.TASK_29_3);
                    break;
                case BossID.SIEU_BO_HUNG:
                    doneTask(player, ConstTask.TASK_29_4);
                    break;
                case BossID.DRABURA, BossID.DRABURA_2, BossID.DRABURA_3:
                    doneTask(player, ConstTask.TASK_30_1);
                    doneTask(player, ConstTask.TASK_30_5);
                    break;
                case BossID.BUI_BUI, BossID.BUI_BUI_2:
                    doneTask(player, ConstTask.TASK_30_2);
                    doneTask(player, ConstTask.TASK_30_3);
                    break;
                case BossID.YA_CON:
                    doneTask(player, ConstTask.TASK_30_4);
                    break;
                case BossID.MABU_12H:
                    doneTask(player, ConstTask.TASK_30_6);
                    break;
                case BossID.CHILLER:
                    doneTask(player, ConstTask.TASK_32_5);
                    switch (boss.currentLevel) {
                        case 0 ->
                            doneTask(player, ConstTask.TASK_32_3);
                        case 1 ->
                            doneTask(player, ConstTask.TASK_32_4);
                    }
                    break;
                case BossID.BLACK_GOKU:
                    doneTask(player, ConstTask.TASK_31_0);
                    break;
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi giết được quái (hỗ trợ cả khi đệ tử tiêu diệt Quái)
    public void checkDoneTaskKillMob(Player player, Mob mob) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet) {
            switch (mob.tempId) {
                case ConstMob.MOC_NHAN:
                    doneTask(player, ConstTask.TASK_1_0);
                    break;
                case ConstMob.KHUNG_LONG_ME:
                    doneTask(player, ConstTask.TASK_4_0);
                    doneTask(player, ConstTask.TASK_5_1);
                    doneTask(player, ConstTask.TASK_6_1);
                    break;
                case ConstMob.LON_LOI_ME:
                    doneTask(player, ConstTask.TASK_4_1);
                    doneTask(player, ConstTask.TASK_5_0);
                    doneTask(player, ConstTask.TASK_6_2);
                    break;
                case ConstMob.QUY_DAT_ME:
                    doneTask(player, ConstTask.TASK_4_2);
                    doneTask(player, ConstTask.TASK_5_2);
                    doneTask(player, ConstTask.TASK_6_0);
                    break;
                case ConstMob.THAN_LAN_BAY:
                case ConstMob.PHI_LONG:
                case ConstMob.QUY_BAY:
                    doneTask(player, ConstTask.TASK_7_1);
                    break;

                case ConstMob.HEO_RUNG:
                    doneTask(player, ConstTask.TASK_13_0);
                    break;
                case ConstMob.HEO_DA_XANH:
                    doneTask(player, ConstTask.TASK_13_1);
                    break;
                case ConstMob.HEO_XAYDA:
                    doneTask(player, ConstTask.TASK_13_2);
                    break;

                case ConstMob.BULON:
                    doneTask(player, ConstTask.TASK_15_1);
                    break;
                case ConstMob.UKULELE:
                    doneTask(player, ConstTask.TASK_15_2);
                    break;
                case ConstMob.QUY_MAP:
                    doneTask(player, ConstTask.TASK_15_3);
                    break;

                case ConstMob.TAMBOURINE:
                    doneTask(player, ConstTask.TASK_17_2);
                    break;
                case ConstMob.DRUM:
                    doneTask(player, ConstTask.TASK_17_3);
                    break;
                case ConstMob.AKKUMAN:
                    doneTask(player, ConstTask.TASK_17_1);
                    break;
                case ConstMob.NAPPA:
                    doneTask(player, ConstTask.TASK_20_1);
                    break;
                case ConstMob.SOLDIER:
                    doneTask(player, ConstTask.TASK_20_2);
                    break;
                case ConstMob.APPULE:
                    doneTask(player, ConstTask.TASK_20_3);
                    break;
                case ConstMob.RASPBERRY:
                    doneTask(player, ConstTask.TASK_20_4);
                    break;
                case ConstMob.THAN_LAN_XANH:
                    doneTask(player, ConstTask.TASK_20_5);
                    break;
                case ConstMob.TOBI:
                case ConstMob.CABIRA:
                    doneTask(player, ConstTask.TASK_31_3);
                    doneTask(player, ConstTask.TASK_31_7);
                    doneTask(player, ConstTask.TASK_32_2);
                    break;

                case ConstMob.XEN_CON_CAP_1:
                    doneTask(player, ConstTask.TASK_24_4);
                    break;
                case ConstMob.XEN_CON_CAP_3:
                    doneTask(player, ConstTask.TASK_25_3);
                    break;
                case ConstMob.XEN_CON_CAP_5:
                    doneTask(player, ConstTask.TASK_27_4);
                    break;
                case ConstMob.XEN_CON_CAP_8:
                    doneTask(player, ConstTask.TASK_28_4);
                    break;

            }
        }
    }

    //xong nhiệm vụ nào đó (hỗ trợ cả khi gọi với đối tượng đệ tử)
    public boolean doneTask(Player player, int idTaskCustom) {
        player = getMasterIfPet(player);
        if (player != null && TaskService.gI().isCurrentTask(player, idTaskCustom)) {
            if (this.addDoneSubTask(player)) {
                switch (idTaskCustom) {
                    case ConstTask.TASK_0_2:
                        npcSay(player, ConstTask.NPC_NHA, "Con đã tỉnh dậy rồi à? Mau mở rương đồ phía bên trái nhà để lấy quần áo đi con!");
                        break;
                    case ConstTask.TASK_0_3:
                        Service.gI().sendThongBao(player, "Đã lấy trang bị từ rương thành công!");
                        npcSay(player, ConstTask.NPC_NHA, "Tốt lắm. Bây giờ con hãy thu hoạch 1 hạt đậu thần từ cây đậu thần sau nhà nhé!");
                        break;
                    case ConstTask.TASK_0_4:
                        npcSay(player, ConstTask.NPC_NHA, "Đậu thần ăn vào sẽ giúp con phục hồi HP và KI ngay lập tức. Giờ hãy lại báo cáo với ta.");
                        break;
                    case ConstTask.TASK_0_5:
                        npcSay(player, ConstTask.NPC_NHA, "Rất tốt! Ta có nhiệm vụ mới cho con đây. Hãy luyện tập bằng cách đánh ngã 5 mộc nhân trước nhà nhé!");
                        break;
                    case ConstTask.TASK_1_0:
                        npcSay(player, ConstTask.NPC_NHA, "Con tập luyện rất chăm chỉ! Giờ hãy lại đây báo cáo với ta.");
                        break;
                    case ConstTask.TASK_1_1:
                        npcSay(player, ConstTask.NPC_NHA, "Tốt lắm! Sức mạnh của con đã tăng lên đáng kể. Ta giao cho con nhiệm vụ tiếp theo: hãy sang bản đồ kế bên tiêu diệt quái và nhặt về 10 đùi gà để ăn lấy sức!");
                        break;
                    case ConstTask.TASK_2_0:
                        npcSay(player, ConstTask.NPC_NHA, "Con nhặt được rất nhiều đùi gà rồi đấy! Mau về báo cáo với ta nào.");
                        break;
                    case ConstTask.TASK_2_1:
                        Item chickenLeg = InventoryService.gI().findItemBagByTemp(player, ConstItem.DUI_GA);
                        if (chickenLeg != null) {
                            InventoryService.gI().subQuantityItemsBag(player, chickenLeg, 10);
                            InventoryService.gI().sendItemBags(player);
                        }
                        npcSay(player, ConstTask.NPC_NHA, "Tuyệt vời, ta đã dạy con kỹ năng bay lượn! Vừa có một vật thể lạ rơi xuống hành tinh chúng ta, con hãy sử dụng tiềm năng nâng cao sức mạnh rồi đi khám phá vật thể lạ đó nhé!");
                        break;
                    case ConstTask.TASK_3_0:
                        npcSay(player, ConstTask.NPC_NHA, "Giỏi lắm, giờ hãy đi tìm vật thể lạ ở bản đồ kế bên!");
                        break;
                    case ConstTask.TASK_3_1:
                        npcSay(player, ConstTask.NPC_NHA, "Con đã thấy vật thể lạ chưa? Hãy mau chóng kiểm tra nó rồi quay về báo cáo với ta.");
                        break;
                    case ConstTask.TASK_3_2:
                        Item duaBe = InventoryService.gI().findItemBagByTemp(player, ConstItem.DUA_BE);
                        if (duaBe != null) {
                            InventoryService.gI().subQuantityItemsBag(player, duaBe, duaBe.quantity);
                            InventoryService.gI().sendItemBags(player);
                        }
                        Service.gI().sendFlagBag(player);
                        npcSay(player, ConstTask.NPC_NHA, "Vật thể lạ đó chính là phi thuyền của người Saiyan! Sẽ có nhiều thử thách lớn phía trước. Bây giờ, con hãy di chuyển đến trạm tàu vũ trụ để đi sang các hành tinh khác thách đấu nhé!");
                        break;
                    case ConstTask.TASK_7_2:
                        npcSay(player, ConstTask.NPC_SHOP_LANG, "Cảm ơn cậu đã cứu tớ! Hãy về báo cáo với ông của cậu nhé!");
                        break;
                    case ConstTask.TASK_7_3:
                        npcSay(player, ConstTask.NPC_NHA, "Ông rất tự hào về con! Con hãy tiếp tục luyện tập đạt 40.000 sức mạnh để đi tìm viên ngọc rồng 7 sao đang bị bọn cướp lấy mất.");
                        break;
                    case ConstTask.TASK_8_1:
                        npcSay(player, ConstTask.NPC_NHA, "Con đã tìm thấy ngọc rồng 7 sao! Hãy đem về báo cáo cho ta.");
                        break;
                    case ConstTask.TASK_8_2:
                        Item nr7 = InventoryService.gI().findItemBagByTemp(player, ConstItem.NGOC_RONG_7_SAO);
                        if (nr7 != null) {
                            InventoryService.gI().subQuantityItemsBag(player, nr7, 1);
                            InventoryService.gI().sendItemBags(player);
                        }
                        npcSay(player, ConstTask.NPC_NHA, "Tốt lắm! Bây giờ con hãy đi tìm Bò Mộng ở rừng Karin để hỏi thêm tin tức về các viên ngọc rồng khác.");
                        break;
                    case ConstTask.TASK_14_2:
                        Item truyenTranh = InventoryService.gI().findItemBagByTemp(player, ConstItem.TRUYEN_TRANH);
                        if (truyenTranh != null) {
                            InventoryService.gI().subQuantityItemsBag(player, truyenTranh, truyenTranh.quantity);
                            InventoryService.gI().sendItemBags(player);
                        }
                        npcSay(player, ConstTask.NPC_QUY_LAO, "Con đã tìm được truyện tranh! Rất tốt, giờ hãy tiếp tục luyện tập nhé.");
                        break;
                    case ConstTask.TASK_9_0:
                        npcSay(player, ConstNpc.BO_MONG, "Cẩn thận! Tàu Pảy Pảy đang ở đây và hắn rất nguy hiểm!");
                        TrainingService.gI().callBoss(player, BossID.TAUPAYPAY, false);
                        break;
                    case ConstTask.TASK_9_1:
                        Service.gI().sendThongBao(player, "Hắn quá mạnh! Mau bỏ chạy lên tháp Karin!");
                        break;
                    case ConstTask.TASK_9_3:
                        npcSay(player, ConstNpc.THAN_MEO_KARIN, "Ta là Thần Mèo Karin. Nếu con muốn đánh bại Tàu Pảy Pảy, con phải vượt qua thử thách tập luyện của ta!");
                        break;
                    case ConstTask.TASK_10_2:
                        npcSay(player, ConstNpc.BO_MONG, "Con quả thực rất mạnh mẽ! Hãy mau đem ngọc rồng về báo cáo với ông của con.");
                        break;
                    case ConstTask.TASK_10_3:
                        npcSay(player, ConstTask.NPC_NHA, "Con đã lập được công lớn! Bây giờ con hãy đi tìm Quy Lão Kame/Trưởng lão Guru/Vua Vegeta để bái sư học võ công cao cường hơn.");
                        break;
                    case ConstTask.TASK_11_0:
                        npcSay(player, ConstTask.NPC_QUY_LAO, "Con có tư chất rất tốt, ta đồng ý nhận con làm đệ tử! Hãy về báo cáo với ông của con.");
                        break;
                    case ConstTask.TASK_11_1:
                        npcSay(player, ConstTask.NPC_NHA, "Con đã bái sư thành công! Giờ hãy tìm cách gia nhập một bang hội để có những người đồng đội hỗ trợ nhau nhé.");
                        break;
                    case ConstTask.TASK_12_0:
                        npcSay(player, ConstTask.NPC_QUY_LAO, "Tốt lắm, giờ con đã có đồng đội! Hãy cùng bang hội đi tiêu diệt heo rừng để tăng sự gắn kết.");
                        break;
                    case ConstTask.TASK_16_0:
                        npcSay(player, ConstTask.NPC_QUY_LAO, "Tuyệt vời! Con đã xuất sắc vô địch Võ Đài Sinh Tử tại đảo Kame! Mau về đây báo cáo với ta.");
                        break;
                    case ConstTask.TASK_19_1:
                        npcSay(player, ConstTask.NPC_QUY_LAO, "Tuyệt vời! Con và đồng đội đã tiêu diệt được Trung Úy trong trại Độc Nhãn! Mau về đây báo cáo với ta.");
                        break;
                }
                if (player.playerTask != null) {
                    player.playerTask.isJustDoneMainTask = false;
                }
            }
            PlayerService.gI().sendInfoHpMpMoney(player);
            return true;
        }
        return false;
    }

    public boolean checkPendingNpcSay(Player player, Npc npc) {
        if (player == null || npc == null || player.playerTask == null) {
            return false;
        }
        if (player.playerTask.pendingNpcSayText != null) {
            int targetNpcId = player.playerTask.pendingNpcSayNpcId;
            if (targetNpcId == npc.tempId || targetNpcId == -1) {
                String text = player.playerTask.pendingNpcSayText;
                player.playerTask.pendingNpcSayText = null;
                player.playerTask.pendingNpcSayNpcId = -1;
                int avatar = NpcService.gI().getAvatar(npc.tempId);
                NpcService.gI().createTutorial(player, avatar, text);
                return true;
            }
        }
        return false;
    }

    protected void npcSay(Player player, int npcId, String text) {
        if (player == null) {
            return;
        }
        if (player.playerTask != null && player.playerTask.isJustDoneMainTask) {
            player.playerTask.isJustDoneMainTask = false;
            player.playerTask.pendingNpcSayNpcId = transformNpcId(player, npcId);
            player.playerTask.pendingNpcSayText = transformName(player, text);
            return;
        }
        npcId = transformNpcId(player, npcId);
        text = transformName(player, text);
        int avatar = NpcService.gI().getAvatar(npcId);
        NpcService.gI().createTutorial(player, avatar, text);
    }

    public long getRewardSMTNByTaskId(int taskId) {
        return switch (taskId) {
            case 1 -> 500L;
            case 2 -> 1_000L;
            case 3 -> 2_000L;
            case 4, 5, 6 -> 4_000L;
            case 7 -> 8_000L;
            case 8, 10 -> 15_000L;
            case 12 -> 20_000L;
            case 14 -> 80_000L;
            case 15, 16 -> 150_000L;
            case 17 -> 200_000L;
            case 18 -> 500_000L;
            case 19 -> 5_000_000L;
            case 20 -> 50_000_000L;
            case 21, 22, 23 -> 20_000_000L;
            case 24, 25, 26, 27, 28, 29 -> 1_000_000L;
            case 31 -> 10_000_000L;
            default -> 0L;
        };
    }

    protected void rewardDoneTask(Player player) {
        if (player != null && player.nPoint != null && player.playerTask != null && player.playerTask.taskMain != null) {
            int taskId = player.playerTask.taskMain.id;
            if (taskId == 3) {
                Item duaBe = InventoryService.gI().findItemBagByTemp(player, ConstItem.DUA_BE);
                if (duaBe != null) {
                    InventoryService.gI().subQuantityItemsBag(player, duaBe, duaBe.quantity);
                    InventoryService.gI().sendItemBags(player);
                }
                Service.gI().sendFlagBag(player);
            } else if (taskId == 14) {
                Item truyenTranh = InventoryService.gI().findItemBagByTemp(player, ConstItem.TRUYEN_TRANH);
                if (truyenTranh != null) {
                    InventoryService.gI().subQuantityItemsBag(player, truyenTranh, truyenTranh.quantity);
                    InventoryService.gI().sendItemBags(player);
                }
            }
            // 1. Thưởng Sức Mạnh & Tiềm Năng đúng chuẩn hiển thị trong giao diện nhiệm vụ
            long rewardSMTN = getRewardSMTNByTaskId(taskId);
            if (rewardSMTN > 0) {
                Service.gI().addSMTN(player, (byte) 2, rewardSMTN, false);
            }

            // 2. Thưởng vàng hợp lý theo mốc nhiệm vụ (giảm đi 1 nửa theo yêu cầu)
            int goldReward = switch (taskId) {
                case 0, 1, 2, 3 -> 0;
                case 4, 5, 6 -> 500;
                case 7 -> 2_500;
                case 8, 10 -> 5_000;
                case 12 -> 10_000;
                case 14 -> 25_000;
                case 15, 16 -> 50_000;
                case 17 -> 100_000;
                case 18 -> 250_000;
                case 19 -> 500_000;
                case 20 -> 2_500_000;
                case 21, 22, 23 -> 1_000_000;
                case 24, 25, 26, 27, 28, 29 -> 500_000;
                case 31 -> 1_000_000;
                default -> 0;
            };
            if (goldReward > 0) {
                player.inventory.addGold(goldReward);
            }

            // 2.1. Thưởng Hồng Ngọc theo từng nhiệm vụ chính hoàn thành (thay vì ngọc xanh)
            int rubyReward = switch (taskId) {
                case 0, 1, 2, 3 -> 5;
                case 4, 5, 6 -> 10;
                case 7 -> 20; // Mốc đệ tử / gia nhập bang
                case 8, 9, 10 -> 10;
                case 11, 12, 13, 14 -> 15;
                case 15, 16, 17, 18 -> 20;
                case 19, 20, 21, 22 -> 25;
                case 23 -> 100; // Mốc Fide
                case 24, 25, 26, 27, 28 -> 30;
                case 29 -> 50; // Mốc Siêu Bọ Hung
                case 30, 31 -> 50;
                default -> 10;
            };
            player.inventory.ruby += rubyReward;

            // 3. Thưởng vật phẩm mốc lớn (Milestone Cột Mốc)
            String milestoneRewardStr = null;
            switch (taskId) {
                case 7 -> {
                    milestoneRewardStr = "Thưởng cột mốc: 20 Hồng Ngọc";
                }
                case 11 -> {
                    Item capsule = ItemService.gI().createNewItem((short) 193, 10);
                    InventoryService.gI().addItemBag(player, capsule);
                    milestoneRewardStr = "Sư phụ thưởng: 10 Capsule Vàng";
                }
                case 23 -> {
                    Item thoiVang = ItemService.gI().createNewItem((short) 457, 5);
                    InventoryService.gI().addItemBag(player, thoiVang);
                    milestoneRewardStr = "Chiến thắng Fide: 100 Hồng Ngọc & 5 Thỏi Vàng";
                }
                case 29 -> {
                    Item daNangCap = ItemService.gI().createNewItem((short) 223, 10);
                    InventoryService.gI().addItemBag(player, daNangCap);
                    milestoneRewardStr = "Chinh phục Siêu Bọ Hung: 50 Hồng Ngọc & 10 Đá May Mắn";
                }
            }

            // 4. Thưởng Cà chua & Cà rốt (giảm 1 nửa và chỉ thưởng khi chưa hoàn thành nhiệm vụ trung úy trắng: taskId <= 19)
            int vegQuantity = 0;
            if (taskId <= 19) {
                vegQuantity = taskId <= 10 ? 1 : 2;
                Item caChua = ItemService.gI().createNewItem((short) 191, vegQuantity);
                Item caRot = ItemService.gI().createNewItem((short) 192, vegQuantity);
                InventoryService.gI().addItemBag(player, caChua);
                InventoryService.gI().addItemBag(player, caRot);
            }

            InventoryService.gI().sendItemBags(player);
            PlayerService.gI().sendInfoHpMpMoney(player);

            // Bảng thông báo tổng hợp toàn bộ phần thưởng hoàn thành nhiệm vụ
            StringBuilder sbReward = new StringBuilder("Chúc mừng hoàn thành nhiệm vụ!\nPhần thưởng nhận được:\n");
            if (rewardSMTN > 0) {
                sbReward.append("- ").append(Util.formatNumber(rewardSMTN)).append(" Sức mạnh & Tiềm năng\n");
            }
            if (goldReward > 0) {
                sbReward.append("- ").append(Util.formatNumber(goldReward)).append(" Vàng\n");
            }
            if (rubyReward > 0) {
                sbReward.append("- ").append(rubyReward).append(" Hồng Ngọc\n");
            }
            if (vegQuantity > 0) {
                sbReward.append("- ").append(vegQuantity).append(" Cà chua & ").append(vegQuantity).append(" Cà rốt\n");
            }
            if (milestoneRewardStr != null) {
                sbReward.append("- ").append(milestoneRewardStr).append("\n");
            }
            Service.gI().sendThongBaoOK(player, sbReward.toString().trim());

            Service.gI().point(player);
            PlayerAuditLogger.logAction(player, "TASK_REWARD", "Completed Task " + taskId + " -> SMTN: +" + rewardSMTN + ", Gold: +" + goldReward);
        }
    }

    protected boolean addDoneSubTask(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            return false;
        }
        synchronized (player.playerTask) {
            TaskMain tm = player.playerTask.taskMain;
            if (tm.subTasks == null || tm.subTasks.isEmpty()) {
                return false;
            }
            if (tm.index < 0) {
                tm.index = 0;
            }
            if (tm.index >= tm.subTasks.size()) {
                tm.index = tm.subTasks.size() - 1;
                return false;
            }
            SubTaskMain currentStm = tm.subTasks.get(tm.index);
            currentStm.count += 1;
            PlayerAuditLogger.logAction(player, "TASK_PROGRESS", "Task ID " + tm.id + " SubTask " + tm.index + ": count=" + currentStm.count + "/" + currentStm.maxCount);
            if (currentStm.count >= currentStm.maxCount) {
                if (player.playerTask.pendingNpcSayText != null) {
                    player.playerTask.pendingNpcSayText = null;
                    player.playerTask.pendingNpcSayNpcId = -1;
                }
                tm.index++;
                if (tm.index >= tm.subTasks.size()) {
                    this.sendNextTaskMain(player);
                } else {
                    this.sendNextSubTask(player);
                }
                return true;
            } else {
                this.sendUpdateCountSubTask(player);
                return false;
            }
        }
    }

    public int transformMapId(Player player, int id) {
        if (id == ConstTask.MAP_NHA) {
            return (short) (player.gender + 21);
        } else if (id == ConstTask.MAP_200) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 1 : (player.gender == ConstPlayer.NAMEC
                            ? 8 : 15);
        } else if (id == ConstTask.MAP_VACH_NUI) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 39 : (player.gender == ConstPlayer.NAMEC
                            ? 40 : 41);
        } else if (id == ConstTask.MAP_500) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 2 : (player.gender == ConstPlayer.NAMEC
                            ? 9 : 16);
        } else if (id == ConstTask.MAP_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 24 : (player.gender == ConstPlayer.NAMEC
                            ? 25 : 26);
        } else if (id == ConstTask.MAP_QUAI_BAY_600) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 3 : (player.gender == ConstPlayer.NAMEC
                            ? 11 : 17);
        } else if (id == ConstTask.MAP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 0 : (player.gender == ConstPlayer.NAMEC
                            ? 7 : 14);
        } else if (id == ConstTask.MAP_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 5 : (player.gender == ConstPlayer.NAMEC
                            ? 13 : 20);
        }
        return id;
    }

    protected int transformNpcId(Player player, int id) {
        if (id == ConstTask.NPC_NHA) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.ONG_GOHAN : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.ONG_MOORI : ConstNpc.ONG_PARAGUS);
        } else if (id == ConstTask.NPC_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.DR_DRIEF : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.CARGO : ConstNpc.CUI);
        } else if (id == ConstTask.NPC_SHOP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.BUNMA : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.DENDE : ConstNpc.APPULE);
        } else if (id == ConstTask.NPC_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.QUY_LAO_KAME : (player.gender == ConstPlayer.NAMEC
                            ? ConstNpc.TRUONG_LAO_GURU : ConstNpc.VUA_VEGETA);
        }
        return id;
    }
}

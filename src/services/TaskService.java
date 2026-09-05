package services;

import consts.ConstItem; // Import class ConstItem bạn đã cung cấp
import consts.ConstMob;
import consts.ConstNpc;
import consts.ConstPlayer;
import models.player.Player;
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
import services.map.NpcService;
import services.player.ClanService;
import services.player.InventoryService;
import utils.Logger;
import utils.Util;
import server.Client;
import models.task.ClanTask;
import models.task.ClanTaskTemplate;
import services.phoban.TrainingService;
import services.player.PlayerService;

public class TaskService {

    private static services.TaskService i;

    // OPTIMIZATION: Map lưu trữ quan hệ giữa ID Nhiệm vụ và ID Quái cần giết
    // Key: Task Template ID, Value: List Mob ID
    private static final Map<Integer, List<Integer>> SIDE_TASK_MOB_MAP = new HashMap<>();
    private static final Map<Integer, List<Integer>> CLAN_TASK_MOB_MAP = new HashMap<>();

    // Khởi tạo dữ liệu static mapping để tránh dùng if-else dài dòng
    static {
        // Cấu hình cho nhiệm vụ Bò Mộng (Side Task)
        initTaskMobMap(SIDE_TASK_MOB_MAP);
        // Cấu hình cho nhiệm vụ Bang Hội (Clan Task)
        initTaskMobMap(CLAN_TASK_MOB_MAP);
        // Nếu nhiệm vụ bang hội khác nhiệm vụ bò mộng thì sửa lại hàm init riêng
    }

    // Helper để điền dữ liệu vào map (Giả sử ID nhiệm vụ và Mob tương ứng nhau như code cũ)
    private static void initTaskMobMap(Map<Integer, List<Integer>> map) {
        map.put(0, Arrays.asList((int) ConstMob.KHUNG_LONG));
        map.put(1, Arrays.asList((int) ConstMob.LON_LOI));
        map.put(2, Arrays.asList((int) ConstMob.QUY_DAT));
        map.put(3, Arrays.asList((int) ConstMob.KHUNG_LONG_ME));
        map.put(4, Arrays.asList((int) ConstMob.LON_LOI_ME));
        map.put(5, Arrays.asList((int) ConstMob.QUY_DAT_ME));
        map.put(6, Arrays.asList((int) ConstMob.THAN_LAN_BAY));
        map.put(7, Arrays.asList((int) ConstMob.PHI_LONG));
        map.put(8, Arrays.asList((int) ConstMob.QUY_BAY));
        map.put(9, Arrays.asList((int) ConstMob.THAN_LAN_ME));
        map.put(10, Arrays.asList((int) ConstMob.PHI_LONG_ME));
        map.put(11, Arrays.asList((int) ConstMob.QUY_BAY_ME));
        map.put(12, Arrays.asList((int) ConstMob.HEO_RUNG));
        map.put(13, Arrays.asList((int) ConstMob.HEO_DA_XANH));
        map.put(14, Arrays.asList((int) ConstMob.HEO_XAYDA));
        map.put(15, Arrays.asList((int) ConstMob.OC_MUON_HON));
        map.put(16, Arrays.asList((int) ConstMob.OC_SEN));
        map.put(17, Arrays.asList((int) ConstMob.HEO_XAYDA_ME));
        map.put(18, Arrays.asList((int) ConstMob.KHONG_TAC));
        map.put(19, Arrays.asList((int) ConstMob.QUY_DAU_TO));
        map.put(20, Arrays.asList((int) ConstMob.QUY_DIA_NGUC));
        map.put(21, Arrays.asList((int) ConstMob.HEO_RUNG_ME));
        map.put(22, Arrays.asList((int) ConstMob.HEO_XANH_ME));
        map.put(23, Arrays.asList((int) ConstMob.ALIEN));
        map.put(24, Arrays.asList((int) ConstMob.TAMBOURINE));
        map.put(25, Arrays.asList((int) ConstMob.DRUM));
        map.put(26, Arrays.asList((int) ConstMob.AKKUMAN));
        map.put(27, Arrays.asList((int) ConstMob.NAPPA));
        map.put(28, Arrays.asList((int) ConstMob.SOLDIER));
        map.put(29, Arrays.asList((int) ConstMob.APPULE));
        map.put(30, Arrays.asList((int) ConstMob.RASPBERRY));
        map.put(31, Arrays.asList((int) ConstMob.THAN_LAN_XANH));
        map.put(32, Arrays.asList((int) ConstMob.QUY_DAU_NHON));
        map.put(33, Arrays.asList((int) ConstMob.QUY_DAU_VANG));
        map.put(34, Arrays.asList((int) ConstMob.QUY_DA_TIM));
        map.put(35, Arrays.asList((int) ConstMob.QUY_GIA));
        map.put(36, Arrays.asList((int) ConstMob.CA_SAU));
        map.put(37, Arrays.asList((int) ConstMob.DOI_DA_XANH));
        map.put(38, Arrays.asList((int) ConstMob.QUY_CHIM));
        map.put(39, Arrays.asList((int) ConstMob.LINH_DAU_TROC));
        map.put(40, Arrays.asList((int) ConstMob.LINH_TAI_DAI));
        map.put(41, Arrays.asList((int) ConstMob.LINH_VU_TRU));
        map.put(42, Arrays.asList((int) ConstMob.KHI_LONG_DEN));
        map.put(43, Arrays.asList((int) ConstMob.KHI_GIAP_SAT));
        map.put(44, Arrays.asList((int) ConstMob.KHI_LONG_DO));
        map.put(45, Arrays.asList((int) ConstMob.KHI_LONG_VANG));
        map.put(46, Arrays.asList((int) ConstMob.XEN_CON_CAP_1));
        map.put(47, Arrays.asList((int) ConstMob.XEN_CON_CAP_2));
        map.put(48, Arrays.asList((int) ConstMob.XEN_CON_CAP_3));
        map.put(49, Arrays.asList((int) ConstMob.XEN_CON_CAP_4));
        map.put(50, Arrays.asList((int) ConstMob.XEN_CON_CAP_5));
        map.put(51, Arrays.asList((int) ConstMob.XEN_CON_CAP_6));
        map.put(52, Arrays.asList((int) ConstMob.XEN_CON_CAP_7));
        map.put(53, Arrays.asList((int) ConstMob.XEN_CON_CAP_8));
        map.put(54, Arrays.asList((int) ConstMob.TAI_TIM));
        map.put(55, Arrays.asList((int) ConstMob.ABO));
        map.put(56, Arrays.asList((int) ConstMob.KADO));
        map.put(57, Arrays.asList((int) ConstMob.DA_XANH));
    }

    public static services.TaskService gI() {
        if (i == null) {
            i = new services.TaskService();
        }
        return i;
    }

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
            msg.writer().writeUTF(tm.detail != null ? tm.detail : "");
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
            if (player != null && player.zone != null && player.zone.map != null) {
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

    //gửi thông tin nhiệm vụ hiện tại
    public void sendInfoCurrentTask(Player player) {
        Service.gI().sendThongBao(player, "Nhiệm vụ hiện tại của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    public boolean checkDoneTaskTalkNpc(Player player, Npc npc) {
        return switch (npc.tempId) {
            case ConstNpc.QUY_LAO_KAME ->
                player.gender == ConstPlayer.TRAI_DAT && (doneTask(player, ConstTask.TASK_11_0)
                || doneTask(player, ConstTask.TASK_12_0)
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
                || doneTask(player, ConstTask.TASK_12_0)
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
                || doneTask(player, ConstTask.TASK_12_0)
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
            case ConstNpc.ONG_GOHAN, ConstNpc.ONG_MOORI, ConstNpc.ONG_PARAGUS ->
                (doneTask(player, ConstTask.TASK_0_2)
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
            doneTask(player, ConstTask.TASK_12_0);
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
                    case 52:
                        doneTask(player, ConstTask.TASK_18_1);
                        break;
                    case 59:
                        doneTask(player, ConstTask.TASK_19_1);
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
        if (!player.isBoss && !player.isPet && item != null) {
            switch (item.itemTemplate.id) {
                case ConstItem.DUI_GA:
                    doneTask(player, ConstTask.TASK_2_0);
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
        doneTask(player, ConstTask.TASK_16_0);
    }

    //kiểm tra hoàn thành nhiệm vụ khi tiêu diệt được boss
    public void checkDoneTaskKillBoss(Player player, Boss boss) {
        if (player != null && !player.isBoss && !player.isPet) {
            switch ((int) boss.id) {
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

    //kiểm tra hoàn thành nhiệm vụ khi giết được quái
    public void checkDoneTaskKillMob(Player player, Mob mob) {
        if (!player.isBoss && !player.isPet) {
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

    //xong nhiệm vụ nào đó
    public boolean doneTask(Player player, int idTaskCustom) {
        if (TaskService.gI().isCurrentTask(player, idTaskCustom)) {
            if (this.addDoneSubTask(player)) {
                switch (idTaskCustom) {
                    case ConstTask.TASK_0_2:
                        npcSay(player, ConstTask.NPC_NHA, "Con đã tỉnh dậy rồi à? Mau mở rương đồ phía bên trái nhà để lấy quần áo đi con!");
                        break;
                    case ConstTask.TASK_0_3:
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
                        npcSay(player, ConstTask.NPC_NHA, "Tuyệt vời, ta đã dạy con kỹ năng bay lượn! Vừa có một vật thể lạ rơi xuống hành tinh chúng ta, con hãy sử dụng tiềm năng nâng cao sức mạnh rồi đi khám phá vật thể lạ đó nhé!");
                        break;
                    case ConstTask.TASK_3_0:
                        npcSay(player, ConstTask.NPC_NHA, "Giỏi lắm, giờ hãy đi tìm vật thể lạ ở bản đồ kế bên!");
                        break;
                    case ConstTask.TASK_3_1:
                        npcSay(player, ConstTask.NPC_NHA, "Con đã thấy vật thể lạ chưa? Hãy mau chóng kiểm tra nó rồi quay về báo cáo với ta.");
                        break;
                    case ConstTask.TASK_3_2:
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
                        npcSay(player, ConstTask.NPC_NHA, "Tốt lắm! Bây giờ con hãy đi tìm Bò Mộng ở rừng Karin để hỏi thêm tin tức về các viên ngọc rồng khác.");
                        break;
                    case ConstTask.TASK_9_0:
                        npcSay(player, ConstNpc.BO_MONG, "Cẩn thận! Tàu Pảy Pảy đang ở đây và hắn rất nguy hiểm!");
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
                }
            }
            PlayerService.gI().sendInfoHpMpMoney(player);
            return true;
        }
        return false;
    }

    private void npcSay(Player player, int npcId, String text) {
        npcId = transformNpcId(player, npcId);
        text = transformName(player, text);
        int avatar = NpcService.gI().getAvatar(npcId);
        NpcService.gI().createTutorial(player, avatar, text);
    }

    private void rewardDoneTask(Player player) {
        if (player != null && player.nPoint != null && player.playerTask != null && player.playerTask.taskMain != null) {
            int taskId = player.playerTask.taskMain.id;
            // 1. Tiềm năng tăng dần theo cấp độ nhiệm vụ
            long reward = (long) Math.pow(taskId + 1, 2) * 50_000L + 100_000L;
            player.nPoint.tiemNangUp(reward);

            // 2. Thưởng vàng
            int goldReward = (taskId + 1) * 500_000;
            player.inventory.addGold(goldReward);

            // 3. Thưởng mốc lớn (Milestone Cột Mốc)
            switch (taskId) {
                case 7 -> {
                    player.inventory.gem += 20;
                    Service.gI().sendThongBao(player, "Thưởng cột mốc: 20 Ngọc Xanh!");
                }
                case 11 -> {
                    Item capsule = ItemService.gI().createNewItem((short) 193, 10);
                    InventoryService.gI().addItemBag(player, capsule);
                    Service.gI().sendThongBao(player, "Sư phụ thưởng: 10 Capsule Vàng!");
                }
                case 23 -> {
                    player.inventory.gem += 100;
                    Item thoiVang = ItemService.gI().createNewItem((short) 457, 5);
                    InventoryService.gI().addItemBag(player, thoiVang);
                    Service.gI().sendThongBao(player, "Chiến thắng Fide: Thưởng 100 Ngọc & 5 Thỏi Vàng!");
                }
                case 29 -> {
                    player.inventory.ruby += 50;
                    Item daNangCap = ItemService.gI().createNewItem((short) 223, 10);
                    InventoryService.gI().addItemBag(player, daNangCap);
                    Service.gI().sendThongBao(player, "Chinh phục Siêu Bọ Hung: Thưởng 50 Hồng Ngọc & 10 Đá May Mắn!");
                }
            }

            InventoryService.gI().sendItemBags(player);
            PlayerService.gI().sendInfoHpMpMoney(player);
            Service.gI().sendThongBao(player, "Chúc mừng bạn nhận được " + utils.Util.formatNumber(reward) + " tiềm năng và "
                    + utils.Util.formatNumber(goldReward) + " vàng!");
            Service.gI().point(player);
        }
    }

    private boolean addDoneSubTask(Player player) {
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
            if (currentStm.count >= currentStm.maxCount) {
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

    private int transformNpcId(Player player, int id) {
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

    // OPTIMIZATION: Chỉ replace dựa trên hành tinh để tránh tạo rác bộ nhớ
    private String transformName(Player player, String text) {
        if (text == null) {
            return "";
        }
        switch (player.gender) {
            case ConstPlayer.TRAI_DAT:
                text = text.replaceAll(ConstTask.TEN_QUAI_1000, "phi long mẹ");
                text = text.replaceAll(ConstTask.TEN_MAP_600, "Rừng nấm");
                text = text.replaceAll(ConstTask.TEN_NPC_QUY_LAO, "Quy Lão Kame");
                text = text.replaceAll(ConstTask.TEN_MAP_QUY_LAO, "Đảo Kamê");
                text = text.replaceAll(ConstTask.TEN_QUAI_3000, "ốc mượn hồn");
                text = text.replaceAll(ConstTask.TEN_LANG, "Làng Aru");
                text = text.replaceAll(ConstTask.TEN_NPC_NHA, "ông Gôhan");
                text = text.replaceAll(ConstTask.TEN_QUAI_200, "khủng long");
                text = text.replaceAll(ConstTask.TEN_MAP_200, "Đồi hoa cúc");
                text = text.replaceAll(ConstTask.TEN_VACH_NUI, "Vách núi Aru");
                text = text.replaceAll(ConstTask.TEN_MAP_500, "Thung lũng tre");
                text = text.replaceAll(ConstTask.TEN_NPC_TTVT, "Dr. Brief");
                text = text.replaceAll(ConstTask.TEN_QUAI_BAY_600, "thằn lằn bay");
                text = text.replaceAll(ConstTask.TEN_NPC_SHOP_LANG, "Bunma");
                break;
            case ConstPlayer.NAMEC:
                text = text.replaceAll(ConstTask.TEN_QUAI_1000, "quỷ bay mẹ");
                text = text.replaceAll(ConstTask.TEN_MAP_600, "Thung lũng Namếc");
                text = text.replaceAll(ConstTask.TEN_NPC_QUY_LAO, "Trưởng lão Guru");
                text = text.replaceAll(ConstTask.TEN_MAP_QUY_LAO, "Đảo Guru");
                text = text.replaceAll(ConstTask.TEN_QUAI_3000, "ốc sên");
                text = text.replaceAll(ConstTask.TEN_LANG, "Làng Mori");
                text = text.replaceAll(ConstTask.TEN_NPC_NHA, "ông Moori");
                text = text.replaceAll(ConstTask.TEN_QUAI_200, "lợn lòi");
                text = text.replaceAll(ConstTask.TEN_MAP_200, "Đồi nấm tím");
                text = text.replaceAll(ConstTask.TEN_VACH_NUI, "Vách núi Moori");
                text = text.replaceAll(ConstTask.TEN_MAP_500, "Thị trấn Moori");
                text = text.replaceAll(ConstTask.TEN_NPC_TTVT, "Cargo");
                text = text.replaceAll(ConstTask.TEN_QUAI_BAY_600, "phi long");
                text = text.replaceAll(ConstTask.TEN_NPC_SHOP_LANG, "Dende");
                break;
            case ConstPlayer.XAYDA:
                text = text.replaceAll(ConstTask.TEN_QUAI_1000, "thằn lằn mẹ");
                text = text.replaceAll(ConstTask.TEN_MAP_600, "Rừng nguyên sinh");
                text = text.replaceAll(ConstTask.TEN_NPC_QUY_LAO, "Vua Vegeta");
                text = text.replaceAll(ConstTask.TEN_MAP_QUY_LAO, "Vách núi đen");
                text = text.replaceAll(ConstTask.TEN_QUAI_3000, "heo Xayda mẹ");
                text = text.replaceAll(ConstTask.TEN_LANG, "Làng Kakarot");
                text = text.replaceAll(ConstTask.TEN_NPC_NHA, "ông Paragus");
                text = text.replaceAll(ConstTask.TEN_QUAI_200, "quỷ đất");
                text = text.replaceAll(ConstTask.TEN_MAP_200, "Đồi hoang");
                text = text.replaceAll(ConstTask.TEN_VACH_NUI, "Vách núi Kakarot");
                text = text.replaceAll(ConstTask.TEN_MAP_500, "Làng Plant");
                text = text.replaceAll(ConstTask.TEN_NPC_TTVT, "Cui");
                text = text.replaceAll(ConstTask.TEN_QUAI_BAY_600, "quỷ bay");
                text = text.replaceAll(ConstTask.TEN_NPC_SHOP_LANG, "Appule");
                break;
        }
        return text;
    }

    private boolean isCurrentTask(Player player, int idTaskCustom) {
        return (player != null && player.playerTask != null && player.playerTask.taskMain != null && idTaskCustom == (player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1);
    }

    public int getIdTask(Player player) {
        if (player.isPet || player.isBoss || player.playerTask == null || player.playerTask.taskMain == null) {
            return -1;
        }
        return (player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1;
    }

    //========================SIDE TASK========================
    public SideTaskTemplate getSideTaskTemplateById(int id) {
        if (id != -1) {
            return Manager.SIDE_TASKS_TEMPLATE.get(id);
        }
        return null;
    }

    public boolean checkConditionSideTask(Player player, byte level) {
        if (player.playerTask.sideTask.leftTask <= 0) {
            Service.gI().sendThongBao(player, "Hôm nay bạn đã hoàn thành tối đa 20/20 nhiệm vụ rồi. Hãy quay lại vào ngày mai nhé!");
            return false;
        }

        if (player.playerTask.sideTask.cancelCount > 3) {
            int remainSeconds = player.playerTask.sideTask.getRemainingCooldownSeconds();
            if (remainSeconds > 0) {
                int minutes = remainSeconds / 60;
                int seconds = remainSeconds % 60;
                String timeStr = (minutes > 0 ? minutes + " phút " : "") + seconds + " giây";
                Service.gI().sendThongBao(player, "Bạn đã hủy nhiệm vụ quá 3 lần hôm nay! Cần chờ " + timeStr + " mới có thể nhận lại (hoặc dùng 1 Ngọc Xanh để đổi ngay).");
                return false;
            }
        }

        long power = player.nPoint.power;
        int taskMainId = player.playerTask.taskMain.id;

        switch (level) {
            case ConstTask.EASY:
                if (power < 1000) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn cần đạt tối thiểu 1.000 để nhận nhiệm vụ Dễ!");
                    return false;
                }
                break;
            case ConstTask.NORMAL:
                if (power < 1_500_000) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn chưa đủ! Cần đạt tối thiểu 1.500.000 sức mạnh để nhận nhiệm vụ Bình thường!");
                    return false;
                }
                if (taskMainId < 8) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ Tàu vũ trụ (Nhiệm vụ chính cấp 8) mới có thể đi tới bản đồ của cấp độ này!");
                    return false;
                }
                break;
            case ConstTask.HARD:
                if (power < 15_000_000) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn chưa đủ! Cần đạt tối thiểu 15.000.000 sức mạnh để nhận nhiệm vụ Khó!");
                    return false;
                }
                if (taskMainId < 16) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ chính tuyến đến Fide (Nhiệm vụ chính cấp 16) mới có thể tới được các bản đồ này!");
                    return false;
                }
                break;
            case ConstTask.VERY_HARD:
                if (power < 150_000_000) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn chưa đủ! Cần đạt tối thiểu 150.000.000 sức mạnh để nhận nhiệm vụ Siêu khó!");
                    return false;
                }
                if (taskMainId < 22) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ đến Tương Lai / Xên Bọ Hung (Nhiệm vụ chính cấp 22) mới có thể gặp được quái cấp độ này!");
                    return false;
                }
                break;
            case ConstTask.HELL:
                if (power < 1_500_000_000L) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn chưa đủ! Cần đạt tối thiểu 1.5 Tỷ sức mạnh để nhận nhiệm vụ Địa ngục!");
                    return false;
                }
                if (taskMainId < 25) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ chính tuyến cấp 25 mới có thể diện kiến và săn lùng quái vật Địa ngục!");
                    return false;
                }
                break;
        }

        return true;
    }

    public void changeSideTask(Player player, byte level) {
        player.playerTask.sideTask.renew();
        if (!checkConditionSideTask(player, level)) {
            return;
        }

        player.playerTask.sideTask.reset();

        List<SideTaskTemplate> suitableTasks = getSideTaskTemplatesByLevel(player, level);
        if (suitableTasks.isEmpty()) {
            suitableTasks.add(Manager.SIDE_TASKS_TEMPLATE.get(0));
        }

        SideTaskTemplate temp = suitableTasks.get(Util.nextInt(0, suitableTasks.size() - 1));
        player.playerTask.sideTask.template = temp;

        int baseCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
        if (baseCount <= 0) {
            baseCount = 10;
        }
        player.playerTask.sideTask.maxCount = baseCount;
        player.playerTask.sideTask.level = level;
        player.playerTask.sideTask.receivedTime = System.currentTimeMillis();
        Service.gI().sendThongBao(player, "Bạn nhận được nhiệm vụ: " + player.playerTask.sideTask.getName()
                + " (" + player.playerTask.sideTask.getLevel() + ")");
    }

    private List<SideTaskTemplate> getSideTaskTemplatesByLevel(Player player, byte level) {
        List<SideTaskTemplate> list = new ArrayList<>();
        int currentTaskID = player.playerTask.taskMain.id;

        switch (level) {
            case ConstTask.EASY: {
                List<Integer> allowedTaskIds = new ArrayList<>();
                if (currentTaskID < 7) {
                    if (player.gender == ConstPlayer.TRAI_DAT) {
                        allowedTaskIds.addAll(Arrays.asList(0, 3, 6, 9, 12));
                    } else if (player.gender == ConstPlayer.NAMEC) {
                        allowedTaskIds.addAll(Arrays.asList(1, 4, 7, 10, 13));
                    } else {
                        allowedTaskIds.addAll(Arrays.asList(2, 5, 8, 11, 14));
                    }
                } else {
                    for (int i = 0; i <= 14; i++) {
                        allowedTaskIds.add(i);
                    }
                }
                allowedTaskIds.add(58); // Nhặt vàng
                for (int tId : allowedTaskIds) {
                    SideTaskTemplate t = getSideTaskTemplateById(tId);
                    if (t != null) {
                        list.add(t);
                    }
                }
                break;
            }
            case ConstTask.NORMAL: {
                for (int i = 15; i <= 23; i++) {
                    SideTaskTemplate t = getSideTaskTemplateById(i);
                    if (t != null) {
                        list.add(t);
                    }
                }
                SideTaskTemplate goldTask = getSideTaskTemplateById(58);
                if (goldTask != null) {
                    list.add(goldTask);
                }
                break;
            }
            case ConstTask.HARD: {
                for (int i = 24; i <= 45; i++) {
                    SideTaskTemplate t = getSideTaskTemplateById(i);
                    if (t != null) {
                        list.add(t);
                    }
                }
                break;
            }
            case ConstTask.VERY_HARD: {
                for (int i = 46; i <= 53; i++) {
                    SideTaskTemplate t = getSideTaskTemplateById(i);
                    if (t != null) {
                        list.add(t);
                    }
                }
                break;
            }
            case ConstTask.HELL: {
                for (int i = 52; i <= 57; i++) {
                    SideTaskTemplate t = getSideTaskTemplateById(i);
                    if (t != null) {
                        list.add(t);
                    }
                }
                break;
            }
        }

        if (list.isEmpty()) {
            list.add(Manager.SIDE_TASKS_TEMPLATE.get(0));
        }
        return list;
    }

    public void removeSideTask(Player player) {
        if (player.playerTask.sideTask.template != null) {
            Service.gI().sendThongBao(player, "Bạn vừa hủy bỏ nhiệm vụ: " + player.playerTask.sideTask.getName());
            player.playerTask.sideTask.lastTimeCancel = System.currentTimeMillis();
            player.playerTask.sideTask.cancelCount++;
            player.playerTask.sideTask.reset();

            if (player.playerTask.sideTask.cancelCount <= 3) {
                int leftFree = 3 - player.playerTask.sideTask.cancelCount;
                Service.gI().sendThongBao(player, "Bạn còn " + leftFree + " lần đổi nhiệm vụ miễn phí hôm nay.");
            } else {
                Service.gI().sendThongBao(player, "Bạn đã hết lượt đổi miễn phí! Lần sau cần chờ hồi chiêu 5 phút (hoặc dùng 1 Ngọc Xanh để đổi ngay).");
            }
        }
    }

    public void quickResetSideTaskWithGem(Player player, Npc npc) {
        if (player.inventory.gem < 1) {
            Service.gI().sendThongBao(player, "Bạn không đủ Ngọc Xanh để đổi nhiệm vụ ngay!");
            return;
        }
        player.inventory.subGem(1);
        Service.gI().sendMoney(player);
        player.playerTask.sideTask.lastTimeCancel = 0; // Xóa thời gian chờ
        Service.gI().sendThongBao(player, "Đã xóa thời gian chờ thành công! Bạn có thể chọn nhiệm vụ mới ngay bây giờ.");
        if (npc != null) {
            npc.createOtherMenu(player, ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK,
                    "Tôi có vài nhiệm vụ theo cấp bậc, sức cậu có thể làm được cái nào?\n(Lưu ý: Cần đủ Sức mạnh và đã mở bản đồ tương ứng)",
                    "Dễ\n(Tân thủ)",
                    "Bình thường\n(>= 1.5M SM)",
                    "Khó\n(>= 15M SM)",
                    "Siêu khó\n(>= 150M SM)",
                    "Địa ngục\n(>= 1.5 Tỷ SM)",
                    "Từ chối");
        }
    }

    public void paySideTask(Player player) {
        if (player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.isDone()) {
                if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                    Service.gI().sendThongBao(player, "Hành trang cần tối thiểu 2 ô trống để nhận thưởng!");
                    return;
                }

                int level = player.playerTask.sideTask.level;
                long tnsmReward = 0;
                int goldReward = 0;
                int gemReward = 0;
                int thoiVangQty = 0;
                Item luckyReward = null;

                switch (level) {
                    case ConstTask.EASY:
                        tnsmReward = Util.nextInt(100_000, 300_000);
                        goldReward = 50_000;
                        gemReward = 2;
                        if (Util.isTrue(1, 10)) { // 10% Capsule 1 lần
                            luckyReward = ItemService.gI().createNewItem((short) 193, 1);
                        }
                        break;
                    case ConstTask.NORMAL:
                        tnsmReward = Util.nextInt(1_000_000, 3_000_000);
                        goldReward = 200_000;
                        gemReward = 5;
                        if (Util.isTrue(2, 10)) { // 20% Đá nâng cấp cấp 1 (Lục bảo, Saphia)
                            luckyReward = ItemService.gI().createNewItem((short) Util.nextInt(220, 221), 1);
                        }
                        break;
                    case ConstTask.HARD:
                        tnsmReward = Util.nextInt(5_000_000, 15_000_000);
                        goldReward = 1_000_000;
                        gemReward = 10;
                        thoiVangQty = 1;
                        if (Util.isTrue(3, 10)) { // 30% Đá Ruby, Titan, Thạch anh tím (222-224)
                            luckyReward = ItemService.gI().createNewItem((short) Util.nextInt(222, 224), 1);
                        }
                        break;
                    case ConstTask.VERY_HARD:
                        tnsmReward = Util.nextInt(25_000_000, 60_000_000);
                        goldReward = 3_000_000;
                        gemReward = 20;
                        thoiVangQty = 2;
                        if (player.playerTask.taskdh.TaskBoMong < 10) {
                            player.playerTask.taskdh.TaskBoMong++;
                            player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
                        }
                        if (Util.isTrue(5, 10)) { // 50% Ngọc rồng 4-7 sao
                            luckyReward = ItemService.gI().createNewItem((short) Util.nextInt(17, 20), 1);
                        }
                        break;
                    case ConstTask.HELL:
                        tnsmReward = Util.nextInt(100_000_000, 250_000_000);
                        goldReward = 10_000_000;
                        gemReward = 50;
                        thoiVangQty = 5;
                        if (player.playerTask.taskdh.TaskBoMong < 10) {
                            player.playerTask.taskdh.TaskBoMong++;
                            player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
                        }
                        luckyReward = ItemService.gI().createNewItem((short) Util.nextInt(222, 224), Util.nextInt(1, 3));
                        break;
                }

                // Trừ 1 lượt hoàn thành trong ngày
                player.playerTask.sideTask.leftTask--;

                // Trao Tiềm năng & Sức mạnh
                if (tnsmReward > 0) {
                    Service.gI().addSMTN(player, (byte) 2, tnsmReward, true);
                }

                // Trao Vàng
                if (goldReward > 0) {
                    player.inventory.gold += goldReward;
                    if (player.inventory.gold > models.player.Inventory.LIMIT_GOLD) {
                        player.inventory.gold = models.player.Inventory.LIMIT_GOLD;
                    }
                }

                // Trao Ngọc xanh
                if (gemReward > 0) {
                    player.inventory.addGem(gemReward);
                }

                // Trao Thỏi Vàng
                if (thoiVangQty > 0) {
                    Item thoiVang = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, thoiVangQty);
                    InventoryService.gI().addItemBag(player, thoiVang);
                }

                // Trao Quà may mắn
                if (luckyReward != null) {
                    InventoryService.gI().addItemBag(player, luckyReward);
                }

                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);

                // Thông báo chi tiết
                StringBuilder sb = new StringBuilder("Chúc mừng! Bạn nhận được:\n");
                sb.append("- ").append(Util.powerToString(tnsmReward)).append(" Tiềm năng & Sức mạnh\n");
                sb.append("- ").append(Util.powerToString(goldReward)).append(" Vàng\n");
                sb.append("- ").append(gemReward).append(" Ngọc Xanh");
                if (thoiVangQty > 0) {
                    sb.append("\n- ").append(thoiVangQty).append(" Thỏi Vàng");
                }
                if (luckyReward != null) {
                    sb.append("\n- ").append(luckyReward.template.name);
                }
                Service.gI().sendThongBao(player, sb.toString());

                player.playerTask.sideTask.reset();
            } else {
                Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            }
        }
    }

    // OPTIMIZATION: Thay thế 100 dòng if-else bằng Map lookup
    public void checkDoneSideTaskKillMob(Player player, Mob mob) {
        if (player.playerTask != null && player.playerTask.sideTask.template != null) {
            int currentTaskId = player.playerTask.sideTask.template.id;
            // Lấy danh sách quái cần giết cho task hiện tại
            List<Integer> requiredMobs = SIDE_TASK_MOB_MAP.get(currentTaskId);

            // Kiểm tra xem quái vừa giết có nằm trong danh sách không
            if (requiredMobs != null && requiredMobs.contains((int) mob.tempId)) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }

            // Logic riêng cho task nhặt item (ID 58) nếu cần xử lý ở đây,
            // nhưng thường xử lý ở checkDoneSideTaskPickItem
        }
    }

    public void checkDoneSideTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask != null && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if ((player.playerTask.sideTask.template.id == 58 && item.itemTemplate.type == 9)) {
                player.playerTask.sideTask.count += item.quantity;
                notifyProcessSideTask(player);
            }
        }
    }

    // Refactor: Tách logic kiểm tra % ra để tái sử dụng hoặc cho gọn code
    private void notifyProcessSideTask(Player player) {
        SideTask st = player.playerTask.sideTask;
        if (st.count > st.maxCount) {
            st.count = st.maxCount;
        }
        int percentDone = st.getPercentProcess();
        boolean notify = false;
        if (percentDone < 100) {
            if (!st.notify90 && percentDone >= 90) {
                st.notify90 = true;
                notify = true;
            } else if (!st.notify80 && percentDone >= 80) {
                st.notify80 = true;
                notify = true;
            } else if (!st.notify70 && percentDone >= 70) {
                st.notify70 = true;
                notify = true;
            } else if (!st.notify60 && percentDone >= 60) {
                st.notify60 = true;
                notify = true;
            } else if (!st.notify50 && percentDone >= 50) {
                st.notify50 = true;
                notify = true;
            } else if (!st.notify40 && percentDone >= 40) {
                st.notify40 = true;
                notify = true;
            } else if (!st.notify30 && percentDone >= 30) {
                st.notify30 = true;
                notify = true;
            } else if (!st.notify20 && percentDone >= 20) {
                st.notify20 = true;
                notify = true;
            } else if (!st.notify10 && percentDone >= 10) {
                st.notify10 = true;
                notify = true;
            } else if (!st.notify0 && percentDone >= 0) {
                st.notify0 = true;
                notify = true;
            }

            if (notify) {
                Service.gI().sendThongBao(player, "Nhiệm vụ: "
                        + st.getName() + " đã hoàn thành: "
                        + st.count + "/" + st.maxCount + " ("
                        + percentDone + "%)");
            }
        } else {
            if (!st.notify100) {
                st.notify100 = true;
                Service.gI().sendThongBao(player, "Chúc mừng bạn đã hoàn thành nhiệm vụ, "
                        + "bây giờ hãy quay về Bò Mộng trả nhiệm vụ.");
            }
        }
    }

    //========================CLAN TASK========================
    public ClanTaskTemplate getClanTaskTemplateById(int id) {
        if (id != -1) {
            return Manager.CLAN_TASKS_TEMPLATE.get(id);
        }
        return null;
    }

    public void changeClanTask(Npc npc, Player player, byte level) {
        player.playerTask.clanTask.renew();
        if (player.playerTask.clanTask.leftTask > 0) {
            player.playerTask.clanTask.reset();
            ClanTaskTemplate temp = Manager.CLAN_TASKS_TEMPLATE.get(Util.nextInt(0, Manager.CLAN_TASKS_TEMPLATE.size() - 1));
            player.playerTask.clanTask.template = temp;
            player.playerTask.clanTask.maxCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
            player.playerTask.clanTask.level = level;
            player.playerTask.clanTask.receivedTime = System.currentTimeMillis();
            player.playerTask.clanTask.leftTask--;
            npc.createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
        } else {
            npc.createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Đã hết nhiệm vụ cho hôm nay, hãy chờ đến ngày mai", "OK", "Từ chối");
        }
    }

    public void removeClanTask(Player player) {
        Service.gI().sendThongBao(player, "Đã hủy nhiệm vụ bang.");
        player.playerTask.clanTask.reset();
    }

    public void payClanTask(Player player) {
        if (player.playerTask.clanTask.template != null) {
            if (player.playerTask.clanTask.isDone()) {
                int capsuleClan = (player.playerTask.clanTask.level + 1) * 10;
                player.playerTask.clanTask.reset();
                Service.gI().sendThongBao(player, "Bạn vừa nhận được "
                        + Util.powerToString(capsuleClan) + " capsule bang.");
                if (player.clan != null) {
                    player.clan.capsuleClan += capsuleClan;
                    for (ClanMember cm : player.clan.getMembers()) {
                        if (cm.id == player.id) {
                            cm.memberPoint += capsuleClan;
                            cm.clanPoint += capsuleClan;
                            break;
                        }
                    }
                    player.clan.sendMyClanForAllMember();
                    player.clan.update();
                }
            } else {
                Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            }
        }
    }

    // OPTIMIZATION: Tương tự như Side Task, dùng Map lookup
    public void checkDoneClanTaskKillMob(Player player, Mob mob) {
        if (player.playerTask != null && player.playerTask.clanTask.template != null) {
            int currentTaskId = player.playerTask.clanTask.template.id;
            List<Integer> requiredMobs = CLAN_TASK_MOB_MAP.get(currentTaskId);

            if (requiredMobs != null && requiredMobs.contains((int) mob.tempId)) {
                player.playerTask.clanTask.count++;
                notifyProcessClanTask(player);
            }
        }
    }

    public void checkDoneClanTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask != null && player.playerTask.clanTask != null && player.playerTask.clanTask.template != null && item != null && item.itemTemplate != null) {
            if ((player.playerTask.clanTask.template.id == 58 && item.itemTemplate.type == 9)) {
                player.playerTask.clanTask.count += item.quantity;
                notifyProcessClanTask(player);
            }
        }
    }

    private void notifyProcessClanTask(Player player) {
        ClanTask ct = player.playerTask.clanTask;
        if (ct.count > ct.maxCount) {
            ct.count = ct.maxCount;
        }
        int percentDone = ct.getPercentProcess();
        boolean notify = false;
        if (percentDone < 100) {
            if (!ct.notify90 && percentDone >= 90) {
                ct.notify90 = true;
                notify = true;
            } else if (!ct.notify80 && percentDone >= 80) {
                ct.notify80 = true;
                notify = true;
            } else if (!ct.notify70 && percentDone >= 70) {
                ct.notify70 = true;
                notify = true;
            } else if (!ct.notify60 && percentDone >= 60) {
                ct.notify60 = true;
                notify = true;
            } else if (!ct.notify50 && percentDone >= 50) {
                ct.notify50 = true;
                notify = true;
            } else if (!ct.notify40 && percentDone >= 40) {
                ct.notify40 = true;
                notify = true;
            } else if (!ct.notify30 && percentDone >= 30) {
                ct.notify30 = true;
                notify = true;
            } else if (!ct.notify20 && percentDone >= 20) {
                ct.notify20 = true;
                notify = true;
            } else if (!ct.notify10 && percentDone >= 10) {
                ct.notify10 = true;
                notify = true;
            } else if (!ct.notify0 && percentDone >= 0) {
                ct.notify0 = true;
                notify = true;
            }
            if (notify) {
                Service.gI().sendThongBao(player, "Nhiệm vụ: "
                        + ct.getName() + " đã hoàn thành: "
                        + ct.count + "/" + ct.maxCount + " ("
                        + percentDone + "%)");
            }
        } else {
            if (!ct.notify100) {
                ct.notify100 = true;
                Service.gI().sendThongBao(player, "Tiếp theo hãy về Bang hội báo cáo.");
            }
        }
    }
}

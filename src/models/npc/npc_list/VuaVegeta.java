package models.npc.npc_list;

import consts.ConstNpc;
import consts.ConstPlayer;
import models.npc.Npc;
import models.player.Player;
import services.map.NpcService;
import services.TaskService;

public class VuaVegeta extends Npc {

    public VuaVegeta(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            // Kiểm tra xem có đang làm nhiệm vụ cần nói chuyện với NPC này không
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                
                // Kiểm tra hành tinh (Vua Vegeta chỉ tiếp người Xayda)
                if (player.gender != ConstPlayer.XAYDA) {
                    NpcService.gI().createTutorial(player, tempId, avartar, "Con hãy về hành tinh của mình mà thể hiện");
                    return;
                }

                // Chỉ hiển thị 1 menu duy nhất là "Nhiệm vụ"
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào con, ta rất vui khi gặp được con\nCon muốn làm gì nào ?", "Nhiệm vụ");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                // Xử lý khi chọn menu "Nhiệm vụ" (index 0)
                if (select == 0) {
                    NpcService.gI().createTutorial(player, tempId, avartar, 
                            player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                }
            }
        }
    }
}
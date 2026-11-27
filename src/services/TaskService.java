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

public class TaskService extends TaskServiceBase1 {

    protected static services.TaskService i;

    // OPTIMIZATION: Map lưu trữ quan hệ giữa ID Nhiệm vụ và ID Quái cần giết
    // Key: Task Template ID, Value: List Mob ID
    protected static final Map<Integer, List<Integer>> SIDE_TASK_MOB_MAP = new HashMap<>();
    protected static final Map<Integer, List<Integer>> CLAN_TASK_MOB_MAP = new HashMap<>();

    // Lưu trữ điều kiện bản đồ & hành tinh của từng Side Task để tránh giao nhiệm vụ ở map chưa tới được
    protected static class SideTaskRequirement {
        final int planet;          // 0: Trái Đất, 1: Namếc, 2: Xayda, -1: Tất cả/Liên hành tinh
        final int minTaskMain;     // taskMain.id tối thiểu cần đạt để tới được map quái
        final int targetMapId;     // ID bản đồ chính xác nhiệm vụ diễn ra (-1 nếu không cố định)
        final long minPower;       // Sức mạnh tối thiểu để đến map / đủ sức thực hiện
        final boolean requireClan; // Bắt buộc phải có Bang Hội

        SideTaskRequirement(int planet, int minTaskMain, int targetMapId, long minPower, boolean requireClan) {
            this.planet = planet;
            this.minTaskMain = minTaskMain;
            this.targetMapId = targetMapId;
            this.minPower = minPower;
            this.requireClan = requireClan;
        }

        SideTaskRequirement(int planet, int minTaskMain, int targetMapId, long minPower) {
            this(planet, minTaskMain, targetMapId, minPower, false);
        }

        SideTaskRequirement(int planet, int minTaskMain, int targetMapId) {
            this(planet, minTaskMain, targetMapId, 0L, false);
        }

        SideTaskRequirement(int planet, int minTaskMain) {
            this(planet, minTaskMain, -1, 0L, false);
        }
    }

    protected static final Map<Integer, SideTaskRequirement> SIDE_TASK_REQ_MAP = new HashMap<>();

    // Khởi tạo dữ liệu static mapping để tránh dùng if-else dài dòng
    static {
        // Cấu hình cho nhiệm vụ Bò Mộng (Side Task)
        initTaskMobMap(SIDE_TASK_MOB_MAP);
        // Cấu hình cho nhiệm vụ Bang Hội (Clan Task)
        initTaskMobMap(CLAN_TASK_MOB_MAP);
        // Cấu hình điều kiện hành tinh & mốc nhiệm vụ chính của từng Side Task
        initSideTaskRequirements();
    }

    // Helper để điền dữ liệu vào map (Giả sử ID nhiệm vụ và Mob tương ứng nhau như code cũ)
    protected static void initTaskMobMap(Map<Integer, List<Integer>> map) {
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

    protected static void initSideTaskRequirements() {
        // EASY (0 - 14): Mobs tân thủ & các map sơ cấp trên 3 hành tinh
        SIDE_TASK_REQ_MAP.put(0, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 1, 1));   // Khủng long (Đồi hoa cúc 1)
        SIDE_TASK_REQ_MAP.put(1, new SideTaskRequirement(ConstPlayer.NAMEC, 1, 8));      // Lợn lòi (Đồi nấm tím 8)
        SIDE_TASK_REQ_MAP.put(2, new SideTaskRequirement(ConstPlayer.XAYDA, 1, 15));     // Quỷ đất (Đồi hoang 15)
        SIDE_TASK_REQ_MAP.put(3, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 4, 2));   // Khủng long mẹ (Thung lũng tre 2)
        SIDE_TASK_REQ_MAP.put(4, new SideTaskRequirement(ConstPlayer.NAMEC, 4, 9));      // Lợn lòi mẹ (Thị trấn Moori 9)
        SIDE_TASK_REQ_MAP.put(5, new SideTaskRequirement(ConstPlayer.XAYDA, 4, 16));     // Quỷ đất mẹ (Làng Plant 16)
        SIDE_TASK_REQ_MAP.put(6, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 7, 3));   // Thằn lằn bay (Rừng nấm 3)
        SIDE_TASK_REQ_MAP.put(7, new SideTaskRequirement(ConstPlayer.NAMEC, 7, 11));     // Phi long (Thung lũng Maima 11)
        SIDE_TASK_REQ_MAP.put(8, new SideTaskRequirement(ConstPlayer.XAYDA, 7, 17));     // Quỷ bay (Rừng nguyên sinh 17)
        SIDE_TASK_REQ_MAP.put(9, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 7, 4));   // Thằn lằn mẹ (Rừng xương 4, qua map 3 req 7)
        SIDE_TASK_REQ_MAP.put(10, new SideTaskRequirement(ConstPlayer.NAMEC, 7, 12));    // Phi long mẹ (Vực Maima 12, qua map 11 req 7)
        SIDE_TASK_REQ_MAP.put(11, new SideTaskRequirement(ConstPlayer.XAYDA, 7, 18));    // Quỷ bay mẹ (Rừng thông Xayda 18, qua map 17 req 7)
        SIDE_TASK_REQ_MAP.put(12, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 13, 27)); // Heo rừng (Rừng Bamboo 27)
        SIDE_TASK_REQ_MAP.put(13, new SideTaskRequirement(ConstPlayer.NAMEC, 13, 31));    // Heo da xanh (Núi hoa vàng 31)
        SIDE_TASK_REQ_MAP.put(14, new SideTaskRequirement(ConstPlayer.XAYDA, 13, 35));    // Heo xayda (Rừng cọ 35)

        // NORMAL (15 - 23): Mobs trung cấp (Đảo Kame/Guru/Vách núi đen, Bulông, Karin/Vegeta)
        SIDE_TASK_REQ_MAP.put(15, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 10, 5));  // Ốc mượn hồn (Đảo Kamê 5)
        SIDE_TASK_REQ_MAP.put(16, new SideTaskRequirement(ConstPlayer.NAMEC, 10, 13));    // Ốc sên (Đảo Guru 13)
        SIDE_TASK_REQ_MAP.put(17, new SideTaskRequirement(ConstPlayer.XAYDA, 10, 20));    // Heo xayda mẹ (Vách núi đen 20)
        SIDE_TASK_REQ_MAP.put(18, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 15, 30)); // Không tặc (Đảo Bulông 30)
        SIDE_TASK_REQ_MAP.put(19, new SideTaskRequirement(ConstPlayer.NAMEC, 15, 34));    // Quỷ đầu to (Đông Nam Guru 34)
        SIDE_TASK_REQ_MAP.put(20, new SideTaskRequirement(ConstPlayer.XAYDA, 15, 38));    // Quỷ địa ngục (Bờ vực đen 38)
        SIDE_TASK_REQ_MAP.put(21, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 16, 6));  // Heo rừng mẹ (Đông Karin 6)
        SIDE_TASK_REQ_MAP.put(22, new SideTaskRequirement(ConstPlayer.NAMEC, 16, 10));    // Heo xanh mẹ (Thung lũng Namếc 10)
        SIDE_TASK_REQ_MAP.put(23, new SideTaskRequirement(ConstPlayer.XAYDA, 16, 19));    // Alien (Thành phố Vegeta 19)

        // HARD (24 - 45): Đệ tử Piccolo, Lính Fide, Quái Cold / Yardrat
        SIDE_TASK_REQ_MAP.put(24, new SideTaskRequirement(ConstPlayer.XAYDA, 15, 38));    // Tambourine (Bờ vực đen 38)
        SIDE_TASK_REQ_MAP.put(25, new SideTaskRequirement(ConstPlayer.TRAI_DAT, 16, 6));  // Drum (Đông Karin 6)
        SIDE_TASK_REQ_MAP.put(26, new SideTaskRequirement(ConstPlayer.NAMEC, 16, 10));    // Akkuman (Thung lũng Namếc 10)
        SIDE_TASK_REQ_MAP.put(27, new SideTaskRequirement(ConstPlayer.XAYDA, 16, 19));    // Nappa (Thành phố Vegeta 19)
        SIDE_TASK_REQ_MAP.put(28, new SideTaskRequirement(-1, 20, 69, 1_500_000L));       // Soldier (Vực cấm 69)
        SIDE_TASK_REQ_MAP.put(29, new SideTaskRequirement(-1, 20, 70, 1_500_000L));       // Appule (Núi Appule 70)
        SIDE_TASK_REQ_MAP.put(30, new SideTaskRequirement(-1, 20, 71, 1_500_000L));       // Raspberry (Căn cứ Raspberry 71)
        SIDE_TASK_REQ_MAP.put(31, new SideTaskRequirement(-1, 20, 64, 1_500_000L));       // Thằn lằn xanh (Núi dây leo 64)
        SIDE_TASK_REQ_MAP.put(32, new SideTaskRequirement(-1, 20, 64, 1_500_000L));       // Quỷ đầu nhọn (Núi dây leo 64)
        SIDE_TASK_REQ_MAP.put(33, new SideTaskRequirement(-1, 20, 65, 1_500_000L));       // Quỷ đầu vàng (Núi cây quỷ 65)
        SIDE_TASK_REQ_MAP.put(34, new SideTaskRequirement(-1, 21, 63, 1_500_000L));       // Quỷ da tím (Trại lính Fide 63)
        SIDE_TASK_REQ_MAP.put(35, new SideTaskRequirement(-1, 21, 66, 1_500_000L));       // Quỷ già (Trại quỷ già 66)
        SIDE_TASK_REQ_MAP.put(36, new SideTaskRequirement(-1, 21, 73, 1_500_000L));       // Cá sấu (Thung lũng chết 73)
        SIDE_TASK_REQ_MAP.put(37, new SideTaskRequirement(-1, 21, 74, 1_500_000L));       // Dơi da xanh (Đồi cây Fide 74)
        SIDE_TASK_REQ_MAP.put(38, new SideTaskRequirement(-1, 21, 76, 1_500_000L));       // Quỷ chim (Núi đá 76)
        SIDE_TASK_REQ_MAP.put(39, new SideTaskRequirement(-1, 21, 75, 1_500_000L));       // Lính đầu trọc (Khe núi tử thần 75)
        SIDE_TASK_REQ_MAP.put(40, new SideTaskRequirement(-1, 21, 75, 1_500_000L));       // Lính tai dài (Khe núi tử thần 75)
        SIDE_TASK_REQ_MAP.put(41, new SideTaskRequirement(-1, 21, 77, 1_500_000L));       // Lính vũ trụ (Rừng đá 77)
        SIDE_TASK_REQ_MAP.put(42, new SideTaskRequirement(-1, 22, 82, 15_000_000L));      // Khỉ lông đen (Núi khỉ đen 82)
        SIDE_TASK_REQ_MAP.put(43, new SideTaskRequirement(-1, 22, 83, 15_000_000L));      // Khỉ giáp sắt (Hang khỉ đen 83)
        SIDE_TASK_REQ_MAP.put(44, new SideTaskRequirement(-1, 22, 79, 15_000_000L));      // Khỉ lông đỏ (Núi khỉ đỏ 79)
        SIDE_TASK_REQ_MAP.put(45, new SideTaskRequirement(-1, 23, 80, 15_000_000L));      // Khỉ lông vàng (Núi khỉ vàng 80)

        // VERY_HARD (46 - 53): Xên con cấp 1 -> 8 (Tương lai & Thị trấn Ginder)
        SIDE_TASK_REQ_MAP.put(46, new SideTaskRequirement(-1, 23, 92, 150_000_000L));     // Xên con cấp 1 (Thành phố phía đông 92)
        SIDE_TASK_REQ_MAP.put(47, new SideTaskRequirement(-1, 23, 93, 150_000_000L));     // Xên con cấp 2 (Thành phố phía nam 93)
        SIDE_TASK_REQ_MAP.put(48, new SideTaskRequirement(-1, 23, 94, 150_000_000L));     // Xên con cấp 3 (Đảo Balê 94)
        SIDE_TASK_REQ_MAP.put(49, new SideTaskRequirement(-1, 23, 96, 150_000_000L));     // Xên con cấp 4 (Cao nguyên 96)
        SIDE_TASK_REQ_MAP.put(50, new SideTaskRequirement(-1, 27, 97, 150_000_000L));     // Xên con cấp 5 (Thành phố phía bắc 97)
        SIDE_TASK_REQ_MAP.put(51, new SideTaskRequirement(-1, 27, 98, 150_000_000L));     // Xên con cấp 6 (Ngọn núi phía bắc 98)
        SIDE_TASK_REQ_MAP.put(52, new SideTaskRequirement(-1, 27, 99, 150_000_000L));     // Xên con cấp 7 (Thung lũng phía bắc 99)
        SIDE_TASK_REQ_MAP.put(53, new SideTaskRequirement(-1, 27, 100, 150_000_000L));    // Xên con cấp 8 (Thị trấn Ginder 100)

        // HELL (54 - 57): Vùng Băng Tuyết (Cánh đồng tuyết, Rừng tuyết, Sông băng, Hang băng)
        SIDE_TASK_REQ_MAP.put(54, new SideTaskRequirement(-1, 29, 105, 1_500_000_000L));   // Tai tím (Cánh đồng tuyết 105)
        SIDE_TASK_REQ_MAP.put(55, new SideTaskRequirement(-1, 29, 106, 1_500_000_000L));   // Abo (Rừng tuyết 106)
        SIDE_TASK_REQ_MAP.put(56, new SideTaskRequirement(-1, 29, 108, 1_500_000_000L));   // Kado (Dòng sông băng 108)
        SIDE_TASK_REQ_MAP.put(57, new SideTaskRequirement(-1, 29, 109, 1_500_000_000L));   // Da xanh (Rừng băng 109)

        // NHẶT VÀNG (58): Toàn bộ hành tinh, mọi cấp độ
        SIDE_TASK_REQ_MAP.put(58, new SideTaskRequirement(-1, 0, -1, 0L, false));

        // HÁI ĐẬU THẦN (59): Mọi hành tinh, có sẵn từ đầu
        SIDE_TASK_REQ_MAP.put(59, new SideTaskRequirement(-1, 0, 21, 0L, false));

        // VÕ ĐÀI SIÊU HẠNG (60): Mở giải đấu (taskMain >= 15, SM >= 1.5M)
        SIDE_TASK_REQ_MAP.put(60, new SideTaskRequirement(-1, 15, -1, 1_500_000L, false));

        // CƯỜNG HÓA / ÉP SAO BÀ HẠT MÍT (61): Mở Đảo Kame (taskMain >= 10, Map 5)
        SIDE_TASK_REQ_MAP.put(61, new SideTaskRequirement(-1, 10, 5, 0L, false));

        // DOANH TRẠI ĐỘC NHÃN (62): Bắt buộc Bang hội, taskMain >= 14, SM >= 1.5M
        SIDE_TASK_REQ_MAP.put(62, new SideTaskRequirement(-1, 14, 53, 1_500_000L, true));

        // PHÓ BẢN KHÍ GAS / KHO BÁU (63): Bắt buộc Bang hội, taskMain >= 20, SM >= 15M
        SIDE_TASK_REQ_MAP.put(63, new SideTaskRequirement(-1, 20, 135, 15_000_000L, true));

        // SĂN BOSS THẾ GIỚI (64): Cấp cao (taskMain >= 20, SM >= 150M)
        SIDE_TASK_REQ_MAP.put(64, new SideTaskRequirement(-1, 20, -1, 150_000_000L, false));

        // Tự động khởi tạo các template nhiệm vụ mới nếu chưa có trong DB
        initCustomSideTaskTemplates();
    }

    protected static void initCustomSideTaskTemplates() {
        addCustomSideTaskTemplate(59, "Thu hoạch %1 hạt đậu thần tại nhà", new int[][]{
            {3, 5}, {3, 5}, {5, 7}, {5, 8}, {5, 10}
        });
        addCustomSideTaskTemplate(60, "Thách đấu %1 trận tại Võ Đài Siêu Hạng", new int[][]{
            {1, 1}, {1, 2}, {1, 2}, {2, 3}, {2, 3}
        });
        addCustomSideTaskTemplate(61, "Cường hóa trang bị hoặc ép Sao Pha Lê %1 lần", new int[][]{
            {1, 1}, {1, 2}, {1, 2}, {2, 3}, {2, 3}
        });
        addCustomSideTaskTemplate(62, "Tham gia Doanh Trại Độc Nhãn %1 lần", new int[][]{
            {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}
        });
        addCustomSideTaskTemplate(63, "Tham gia Bản Đồ Kho Báu hoặc Khí Gas %1 lần", new int[][]{
            {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}
        });
        addCustomSideTaskTemplate(64, "Tham gia tiêu diệt Boss Thế Giới %1 lần", new int[][]{
            {1, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}
        });
    }

    protected static void addCustomSideTaskTemplate(int id, String name, int[][] count) {
        for (SideTaskTemplate t : Manager.SIDE_TASKS_TEMPLATE) {
            if (t.id == id) {
                return;
            }
        }
        SideTaskTemplate st = new SideTaskTemplate();
        st.id = id;
        st.name = name;
        st.count = count;
        Manager.SIDE_TASKS_TEMPLATE.add(st);
    }

    public boolean isSideTaskAccessible(Player player, int taskId) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null || player.nPoint == null) {
            return false;
        }
        SideTaskRequirement req = SIDE_TASK_REQ_MAP.get(taskId);
        if (req == null) {
            return false;
        }

        int currentTaskMainId = player.playerTask.taskMain.id;
        long currentPower = player.nPoint.power;

        // 1. Kiểm tra yêu cầu Bang Hội (Clan Check):
        // Nếu nhiệm vụ yêu cầu Bang Hội (Doanh Trại 62, Kho Báu / Khí Gas 63) mà người chơi chưa vào bang -> Bỏ qua
        if (req.requireClan && player.clan == null) {
            return false;
        }

        // 2. Kiểm tra yêu cầu Sức Mạnh tối thiểu (Power Check)
        if (currentPower < req.minPower) {
            return false;
        }

        // 3. Kiểm tra tiến trình Nhiệm Vụ Chính (TaskMain Check)
        if (currentTaskMainId < req.minTaskMain) {
            return false;
        }

        // 4. Kiểm tra giới hạn hành tinh khi chưa có Tàu Vũ Trụ (trước nhiệm vụ 8)
        if (currentTaskMainId < 8) {
            if (req.planet != -1 && req.planet != (int) player.gender) {
                return false;
            }
        }

        // 5. Kiểm tra tính khả dụng đặc thù theo từng loại nhiệm vụ
        switch (taskId) {
            case 58 -> { // Nhặt vàng: Ai cũng làm được
                return true;
            }
            case 59 -> { // Hái đậu: Cần có cây đậu thần và cây không đang trong quá trình nâng cấp
                if (player.magicTree == null || player.magicTree.isUpgrade) {
                    return false;
                }
            }
            case 60 -> { // Võ Đài Siêu Hạng: Cần mở giải đấu (taskMain >= 15) và SM >= 1.5M
                if (currentTaskMainId < 15 || currentPower < 1_500_000L) {
                    return false;
                }
            }
            case 61 -> { // Cường hóa / Ép sao Bà Hạt Mít: Cần mở Đảo Kame (taskMain >= 10)
                if (currentTaskMainId < 10) {
                    return false;
                }
            }
            case 62 -> { // Doanh Trại: Cần có bang và taskMain >= 14
                if (player.clan == null || currentTaskMainId < 14) {
                    return false;
                }
            }
            case 63 -> { // Khí Gas / Kho Báu: Cần có bang, taskMain >= 20 và SM >= 15M
                if (player.clan == null || currentTaskMainId < 20 || currentPower < 15_000_000L) {
                    return false;
                }
            }
            case 64 -> { // Săn Boss Thế Giới: Cần taskMain >= 20 và SM >= 150M
                if (currentTaskMainId < 20 || currentPower < 150_000_000L) {
                    return false;
                }
            }
        }

        // 6. Kiểm tra khả năng tiếp cận Map thực tế
        if (req.targetMapId != -1) {
            // Kiểm tra map có tồn tại trên server không
            if (MapService.gI().getMapById(req.targetMapId) == null) {
                return false;
            }
            // Map Tương Lai: Cần hoàn thành task Fide (taskMain >= 23) và SM >= 150M
            if (MapService.gI().isMapTuongLai(req.targetMapId)) {
                if (currentTaskMainId < 23 || currentPower < 150_000_000L) {
                    return false;
                }
            }
            // Map Băng Tuyết (Cold 105 - 109): Cần taskMain >= 29 và SM >= 1.5 tỷ
            if (req.targetMapId >= 105 && req.targetMapId <= 109) {
                if (currentTaskMainId < 29 || currentPower < 1_500_000_000L) {
                    return false;
                }
            }
            // Map Nappa / Fide (63 - 83): Cần taskMain >= 20 và SM >= 1.5M
            if (MapService.gI().isMapNappa(req.targetMapId)) {
                if (currentTaskMainId < 20 || currentPower < 1_500_000L) {
                    return false;
                }
            }
            // Map Phó Bản Bang Hội: Nếu player không có bang -> không thể vào bất kỳ phó bản nào
            if (MapService.gI().isMapPhoBan(req.targetMapId) && player.clan == null) {
                return false;
            }
        }

        return true;
    }

    public static services.TaskService gI() {
        if (i == null) {
            i = new services.TaskService();
        }
        return i;
    }

    // OPTIMIZATION: Chỉ replace dựa trên hành tinh để tránh tạo rác bộ nhớ
    protected String transformName(Player player, String text) {
        if (text == null) {
            return "";
        }
        text = text.replace("\\r\\n", "\n").replace("\\n", "\n");
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

    public boolean isCurrentTask(Player player, int idTaskCustom) {
        player = getMasterIfPet(player);
        return (player != null && player.playerTask != null && player.playerTask.taskMain != null && idTaskCustom == (player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1);
    }

    public int getIdTask(Player player) {
        player = getMasterIfPet(player);
        if (player == null || player.isPet || player.isBoss || player.playerTask == null || player.playerTask.taskMain == null) {
            return -1;
        }
        return (player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1;
    }

    //========================SIDE TASK========================
    public SideTaskTemplate getSideTaskTemplateById(int id) {
        if (id != -1) {
            for (SideTaskTemplate t : Manager.SIDE_TASKS_TEMPLATE) {
                if (t.id == id) {
                    return t;
                }
            }
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
                if (taskMainId < 10) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ chính tuyến đến Đảo Kamê / Đảo Guru (Cấp 10) mới có thể đi tới bản đồ của cấp độ này!");
                    return false;
                }
                break;
            case ConstTask.HARD:
                if (power < 15_000_000) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn chưa đủ! Cần đạt tối thiểu 15.000.000 sức mạnh để nhận nhiệm vụ Khó!");
                    return false;
                }
                if (taskMainId < 15) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ chính tuyến cấp 15 mới có thể tới được các bản đồ của cấp độ này!");
                    return false;
                }
                break;
            case ConstTask.VERY_HARD:
                if (power < 150_000_000) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn chưa đủ! Cần đạt tối thiểu 150.000.000 sức mạnh để nhận nhiệm vụ Siêu khó!");
                    return false;
                }
                if (taskMainId < 23) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ đến Tương Lai / Xên Bọ Hung (Nhiệm vụ chính cấp 23) mới có thể gặp được quái cấp độ này!");
                    return false;
                }
                break;
            case ConstTask.HELL:
                if (power < 1_500_000_000L) {
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn chưa đủ! Cần đạt tối thiểu 1.5 Tỷ sức mạnh để nhận nhiệm vụ Địa ngục!");
                    return false;
                }
                if (taskMainId < 29) {
                    Service.gI().sendThongBao(player, "Bạn cần hoàn thành nhiệm vụ chính tuyến cấp 29 (Vùng Băng Tuyết) mới có thể diện kiến và săn lùng quái vật Địa ngục!");
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
            baseCount = 100;
        }
        player.playerTask.sideTask.maxCount = baseCount;
        player.playerTask.sideTask.level = level;
        player.playerTask.sideTask.receivedTime = System.currentTimeMillis();
        Service.gI().sendThongBao(player, "Bạn nhận được nhiệm vụ: " + player.playerTask.sideTask.getName()
                + " (" + player.playerTask.sideTask.getLevel() + ")");
    }

    /**
     * Xác định cấp độ nhiệm vụ hàng ngày cao nhất mà player đủ sức mạnh VÀ đã mở map tương ứng.
     * Sử dụng cùng ngưỡng power/taskMain.id như checkConditionSideTask() nhưng KHÔNG reject
     * — chỉ trả về level phù hợp nhất cho player.
     */
    protected byte determineBestLevel(long power, int taskMainId) {
        if (power >= 1_500_000_000L && taskMainId >= 29) return ConstTask.HELL;
        if (power >= 150_000_000   && taskMainId >= 23) return ConstTask.VERY_HARD;
        if (power >= 15_000_000    && taskMainId >= 15) return ConstTask.HARD;
        if (power >= 1_500_000     && taskMainId >= 10) return ConstTask.NORMAL;
        return ConstTask.EASY;
    }

    /**
     * Tự động gán nhiệm vụ hàng ngày phù hợp cho MỌI player (từ tân thủ tới VIP).
     * Thuật toán:
     *  1. Đọc power + taskMain.id → xác định level cao nhất player đủ điều kiện
     *  2. Quét pool task từ level cao nhất xuống thấp nhất
     *     (getSideTaskTemplatesByLevel đã gọi isSideTaskAccessible bên trong
     *      để lọc map player đến được dựa trên planet + minTaskMain)
     *  3. Random chọn 1 task từ pool → gán vào player.playerTask.sideTask
     */
    public void autoAssignSideTask(Player player) {
        player.playerTask.sideTask.renew();

        // Guard: hết lượt trong ngày
        if (player.playerTask.sideTask.leftTask <= 0) {
            Service.gI().sendThongBao(player, "Hôm nay bạn đã hoàn thành tối đa "
                    + ConstTask.MAX_SIDE_TASK + "/" + ConstTask.MAX_SIDE_TASK
                    + " nhiệm vụ rồi. Hãy quay lại vào ngày mai nhé!");
            return;
        }

        // Guard: cooldown hủy quá 3 lần
        if (player.playerTask.sideTask.cancelCount > 3) {
            int remainSeconds = player.playerTask.sideTask.getRemainingCooldownSeconds();
            if (remainSeconds > 0) {
                int minutes = remainSeconds / 60;
                int seconds = remainSeconds % 60;
                String timeStr = (minutes > 0 ? minutes + " phút " : "") + seconds + " giây";
                Service.gI().sendThongBao(player, "Bạn đã hủy nhiệm vụ quá 3 lần hôm nay! Cần chờ "
                        + timeStr + " mới có thể nhận lại (hoặc dùng 1 Ngọc Xanh để đổi ngay).");
                return;
            }
        }

        // Bước 1: Tính level cao nhất phù hợp với player
        byte bestLevel = determineBestLevel(player.nPoint.power, player.playerTask.taskMain.id);

        // Bước 2: Quét pool — ưu tiên level cao nhất, fallback xuống thấp hơn nếu pool rỗng
        List<SideTaskTemplate> pool = null;
        byte assignedLevel = bestLevel;
        for (byte lv = bestLevel; lv >= ConstTask.EASY; lv--) {
            List<SideTaskTemplate> candidates = getSideTaskTemplatesByLevel(player, lv);
            if (!candidates.isEmpty()) {
                pool = candidates;
                assignedLevel = lv;
                break;
            }
        }

        // Fallback cuối cùng: nhặt vàng (ID 58) — ai cũng làm được
        if (pool == null || pool.isEmpty()) {
            pool = new ArrayList<>();
            SideTaskTemplate goldTask = getSideTaskTemplateById(58);
            if (goldTask != null) {
                pool.add(goldTask);
            } else if (!Manager.SIDE_TASKS_TEMPLATE.isEmpty()) {
                pool.add(Manager.SIDE_TASKS_TEMPLATE.get(0));
            }
            assignedLevel = ConstTask.EASY;
        }

        // Bước 3: Random chọn 1 task từ pool → gán cho player
        player.playerTask.sideTask.reset();
        SideTaskTemplate chosen = pool.get(Util.nextInt(0, pool.size() - 1));

        int baseCount;
        if (chosen.id == 59) {
            baseCount = Util.nextInt(3, 5); // 3 - 5 hạt đậu thần
        } else if (chosen.id == 60) {
            baseCount = (assignedLevel >= ConstTask.VERY_HARD) ? 2 : 1; // 1 - 2 trận Võ Đài Siêu Hạng
        } else if (chosen.id == 61) {
            baseCount = (assignedLevel >= ConstTask.VERY_HARD) ? 2 : 1; // 1 - 2 lần Cường hóa / Ép sao
        } else if (chosen.id == 62 || chosen.id == 63 || chosen.id == 64) {
            baseCount = 1; // 1 lần phó bản hoặc 1 Boss thế giới
        } else if (chosen.id == 58) {
            baseCount = Util.nextInt(10_000, 30_000); // 10k - 30k vàng
        } else if (assignedLevel == ConstTask.HELL) {
            baseCount = 100; // [User feedback] Diệt 100 quái map cao
        } else if (assignedLevel == ConstTask.VERY_HARD) {
            baseCount = Util.nextInt(30, 50);
        } else if (assignedLevel == ConstTask.HARD) {
            baseCount = Util.nextInt(25, 30);
        } else if (assignedLevel == ConstTask.NORMAL) {
            baseCount = Util.nextInt(20, 25);
        } else {
            baseCount = Util.nextInt(15, 20); // EASY: 15 - 20 con
        }

        player.playerTask.sideTask.template = chosen;
        player.playerTask.sideTask.maxCount = baseCount;
        player.playerTask.sideTask.level = assignedLevel;
        player.playerTask.sideTask.receivedTime = System.currentTimeMillis();

        Service.gI().sendThongBao(player, "Bạn nhận được nhiệm vụ: " + player.playerTask.sideTask.getName()
                + " (" + player.playerTask.sideTask.getLevel() + ")"
                + "\nSố nhiệm vụ còn lại: " + player.playerTask.sideTask.leftTask
                + "/" + ConstTask.MAX_SIDE_TASK);
    }

    protected List<SideTaskTemplate> getSideTaskTemplatesByLevel(Player player, byte level) {
        List<SideTaskTemplate> list = new ArrayList<>();
        List<Integer> candidateTaskIds = new ArrayList<>();

        switch (level) {
            case ConstTask.EASY: {
                for (int i = 0; i <= 14; i++) {
                    candidateTaskIds.add(i);
                }
                candidateTaskIds.add(58); // Nhặt vàng
                candidateTaskIds.add(59); // Thu hoạch đậu thần tại nhà
                break;
            }
            case ConstTask.NORMAL: {
                for (int i = 15; i <= 23; i++) {
                    candidateTaskIds.add(i);
                }
                candidateTaskIds.add(58); // Nhặt vàng
                candidateTaskIds.add(60); // Võ đài Siêu Hạng
                candidateTaskIds.add(61); // Cường hóa / ép sao Bà Hạt Mít
                break;
            }
            case ConstTask.HARD: {
                for (int i = 24; i <= 45; i++) {
                    candidateTaskIds.add(i);
                }
                candidateTaskIds.add(60); // Võ đài Siêu Hạng
                candidateTaskIds.add(61); // Cường hóa / ép sao Bà Hạt Mít
                candidateTaskIds.add(62); // Doanh Trại Độc Nhãn
                break;
            }
            case ConstTask.VERY_HARD: {
                for (int i = 46; i <= 53; i++) {
                    candidateTaskIds.add(i);
                }
                candidateTaskIds.add(60); // Võ đài Siêu Hạng
                candidateTaskIds.add(61); // Cường hóa / ép sao Bà Hạt Mít
                candidateTaskIds.add(62); // Doanh Trại Độc Nhãn
                candidateTaskIds.add(63); // Bản Đồ Kho Báu / Khí Gas
                break;
            }
            case ConstTask.HELL: {
                for (int i = 54; i <= 57; i++) {
                    candidateTaskIds.add(i);
                }
                candidateTaskIds.add(60); // Võ đài Siêu Hạng
                candidateTaskIds.add(61); // Cường hóa / ép sao Bà Hạt Mít
                candidateTaskIds.add(63); // Bản Đồ Kho Báu / Khí Gas
                candidateTaskIds.add(64); // Tiêu diệt Boss Thế Giới
                break;
            }
        }

        for (int tId : candidateTaskIds) {
            if (isSideTaskAccessible(player, tId)) {
                SideTaskTemplate t = getSideTaskTemplateById(tId);
                if (t != null) {
                    list.add(t);
                }
            }
        }

        // Đảm bảo không bao giờ rỗng: nếu ở cấp EASY mà không có nv nào phù hợp thì fallback về nhặt vàng hoặc quái làng
        if (list.isEmpty() && level == ConstTask.EASY) {
            SideTaskTemplate goldTask = getSideTaskTemplateById(58);
            if (goldTask != null) {
                list.add(goldTask);
            } else {
                SideTaskTemplate starterTask = getSideTaskTemplateById((int) player.gender);
                if (starterTask != null) {
                    list.add(starterTask);
                } else if (!Manager.SIDE_TASKS_TEMPLATE.isEmpty()) {
                    list.add(Manager.SIDE_TASKS_TEMPLATE.get(0));
                }
            }
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
        if (!player.inventory.checkGemAndRuby(1)) {
            Service.gI().sendThongBao(player, "Bạn không đủ Ngọc để đổi nhiệm vụ ngay!");
            return;
        }
        player.inventory.subGemAndRuby(1);
        Service.gI().sendMoney(player);
        player.playerTask.sideTask.lastTimeCancel = 0; // Xóa thời gian chờ
        Service.gI().sendThongBao(player, "Đã xóa thời gian chờ thành công!");
        // Auto-assign nhiệm vụ mới phù hợp với player ngay lập tức
        autoAssignSideTask(player);
    }

    public void paySideTask(Player player) {
        if (player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.isDone()) {
                if (InventoryService.gI().getCountEmptyBag(player) < 8) {
                    Service.gI().sendThongBao(player, "Hành trang cần tối thiểu 8 ô trống để nhận toàn bộ phần thưởng!");
                    return;
                }

                int level = player.playerTask.sideTask.level;
                // 1. Cố định cho tất cả độ khó theo yêu cầu của User: 100k TNSM, 100k Vàng, 5 Hồng Ngọc
                long tnsmReward = 100_000L;
                int goldReward = 100_000;
                int rubyReward = 5;

                if (level == ConstTask.VERY_HARD || level == ConstTask.HELL) {
                    if (player.playerTask.taskdh.TaskBoMong < 10) {
                        player.playerTask.taskdh.TaskBoMong++;
                        player.playerTask.taskdh.ResetTime = System.currentTimeMillis();
                    }
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

                // Trao Hồng Ngọc (Ruby)
                if (rubyReward > 0) {
                    player.inventory.addRuby(rubyReward);
                }

                // 2. Trao vật phẩm theo Tầng: Tất cả độ khó đều nhận x5 Đá Nâng Cấp (5 loại: ID 220 -> 224)
                for (short stoneId = 220; stoneId <= 224; stoneId++) {
                    Item stone = ItemService.gI().createNewItem(stoneId, 5);
                    InventoryService.gI().addItemBag(player, stone);
                }
                String tierItemRewardStr = "- x5 Đá Nâng Cấp (5 loại: Lục Bảo, Saphia, Ruby, Titan, Thạch Anh Tím)\n";

                // 3. Trao Vé quay Thượng Đế [Theo feedback User: VIP 7 vé, Tầm trung 1-2 vé, Tân thủ 50% 1 vé]
                int veQuayQty = switch (level) {
                    case ConstTask.EASY -> Util.isTrue(1, 2) ? 1 : 0;
                    case ConstTask.NORMAL -> 1;
                    case ConstTask.HARD -> 2;
                    case ConstTask.VERY_HARD -> 4;
                    case ConstTask.HELL -> 7; // [Theo feedback User: 7 vé cho VIP]
                    default -> 1;
                };
                if (veQuayQty > 0) {
                    Item veQuay = ItemService.gI().createNewItem((short) ConstItem.VE_QUAY_NGOC_VANG, veQuayQty);
                    InventoryService.gI().addItemBag(player, veQuay);
                }

                // 4. Trao Cà chua & Cà rốt theo độ khó
                int vegSideQty = switch (level) {
                    case ConstTask.EASY -> 2;
                    case ConstTask.NORMAL -> 3;
                    case ConstTask.HARD -> 5;
                    case ConstTask.VERY_HARD -> 7;
                    case ConstTask.HELL -> 10;
                    default -> 2;
                };
                Item caChuaSide = ItemService.gI().createNewItem((short) 191, vegSideQty);
                Item caRotSide = ItemService.gI().createNewItem((short) 192, vegSideQty);
                InventoryService.gI().addItemBag(player, caChuaSide);
                InventoryService.gI().addItemBag(player, caRotSide);

                // 5. Trao Bùa 1h ngẫu nhiên
                int[] randomCharmIds = {213, 214, 215, 216, 217, 218, 219};
                String[] randomCharmNames = {
                    "Bùa Trí Tuệ (1h)", "Bùa Mạnh Mẽ (1h)", "Bùa Da Trâu (1h)",
                    "Bùa Oai Hùng (1h)", "Bùa Bất Tử (1h)", "Bùa Dẻo Dai (1h)", "Bùa Thu Hút (1h)"
                };
                int charmIdx = Util.nextInt(0, randomCharmIds.length - 1);
                int charmId = randomCharmIds[charmIdx];
                String charmName = randomCharmNames[charmIdx];
                player.charms.addTimeCharms(charmId, 60);

                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);

                // Thông báo chi tiết
                StringBuilder sb = new StringBuilder("Chúc mừng! Bạn nhận được:\n");
                sb.append("- ").append(Util.powerToString(tnsmReward)).append(" Tiềm năng & Sức mạnh\n");
                sb.append("- ").append(Util.powerToString(goldReward)).append(" Vàng\n");
                sb.append("- ").append(rubyReward).append(" Hồng Ngọc\n");
                sb.append(tierItemRewardStr);
                sb.append("- ").append(vegSideQty).append(" Cà chua & ").append(vegSideQty).append(" Cà rốt\n");
                sb.append("- ").append(charmName);
                if (veQuayQty > 0) {
                    sb.append("\n- ").append(veQuayQty).append(" Vé quay Thượng Đế");
                }
                Service.gI().sendThongBaoOK(player, sb.toString());

                player.playerTask.sideTask.reset();
                checkDoneTaskSideTask(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            }
        }
    }

    // OPTIMIZATION: Thay thế 100 dòng if-else bằng Map lookup (hỗ trợ cả đệ tử tiêu diệt quái)
    public void checkDoneSideTaskKillMob(Player player, Mob mob) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet && player.playerTask != null && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
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
        if (player.playerTask != null && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null && item != null && item.itemTemplate != null) {
            if ((player.playerTask.sideTask.template.id == 58 && item.itemTemplate.type == 9)) {
                player.playerTask.sideTask.count += item.quantity;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskHarvestMagicTree(Player player, int quantity) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet && player.playerTask != null
                && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.template.id == 59) {
                player.playerTask.sideTask.count += quantity;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskArena(Player player) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet && player.playerTask != null
                && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.template.id == 60) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskUpgrade(Player player) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet && player.playerTask != null
                && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.template.id == 61) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskDungeon(Player player, int dungeonTaskId) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet && player.playerTask != null
                && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.template.id == dungeonTaskId) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskKillBoss(Player player, Boss boss) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet && player.playerTask != null
                && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.template.id == 64) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }
        }
    }

    // Refactor: Tách logic kiểm tra % ra để tái sử dụng hoặc cho gọn code
    protected void notifyProcessSideTask(Player player) {
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
            List<ClanTaskTemplate> accessibleClanTasks = new ArrayList<>();
            for (ClanTaskTemplate ct : Manager.CLAN_TASKS_TEMPLATE) {
                if (isSideTaskAccessible(player, ct.id)) {
                    accessibleClanTasks.add(ct);
                }
            }
            if (accessibleClanTasks.isEmpty()) {
                accessibleClanTasks.addAll(Manager.CLAN_TASKS_TEMPLATE);
            }
            ClanTaskTemplate temp = accessibleClanTasks.get(Util.nextInt(0, accessibleClanTasks.size() - 1));
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
                checkDoneTaskSideTask(player);
                Service.gI().sendThongBaoOK(player, "Chúc mừng! Bạn nhận được "
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

    // OPTIMIZATION: Tương tự như Side Task, dùng Map lookup (hỗ trợ cả đệ tử tiêu diệt quái)
    public void checkDoneClanTaskKillMob(Player player, Mob mob) {
        player = getMasterIfPet(player);
        if (player != null && !player.isBoss && !player.isPet && player.playerTask != null && player.playerTask.clanTask != null && player.playerTask.clanTask.template != null) {
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

    protected void notifyProcessClanTask(Player player) {
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

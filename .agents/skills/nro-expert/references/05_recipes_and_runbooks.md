# NRO Recipes & Runbooks (Quy Trình Thực Chiến Bước-Qua-Bước)

Tài liệu cung cấp các công thức chuẩn hóa để thêm mới tính năng, trang bị, NPC, Boss và Packet mạng vào dự án Ngọc Rồng Online mà không gây lỗi xung đột hay lệch dữ liệu.

---

## Recipe 1: Thêm Một Vật Phẩm / Trang Bị Mới

### Bước 1: Khai báo ID trong `ConstItem.java`
Mở file [ConstItem.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/consts/ConstItem.java) và thêm ID mới:
```java
public static final int HOP_QUA_TET_2026 = 2150;
```

### Bước 2: Thêm bản ghi Template vào CSDL (`nro_data.item_template`)
```sql
INSERT INTO `item_template` (`id`, `name`, `type`, `gender`, `description`, `icon_id`, `part`, `is_up_to_up`) 
VALUES (2150, 'Hộp Quà Tết 2026', 27, 3, 'Mở ra nhận vô số vật phẩm quý giá', 15234, -1, 0);
```
*(Ghi chú: Type 27 là vật phẩm sử dụng được trong túi).*

### Bước 3: Xử lý chức năng trong `UseItem.java`
Mở file [UseItem.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/services/func/UseItem.java), tìm đến phương thức `useItem()`:
```java
case ConstItem.HOP_QUA_TET_2026:
    if (InventoryService.gI().getCountEmptyBag(player) < 3) {
        Service.gI().sendThongBao(player, "Hành trang cần ít nhất 3 ô trống!");
        return;
    }
    // Trừ 1 hộp quà khỏi túi
    InventoryService.gI().subQuantityItemsBag(player, item, 1);
    
    // Thưởng vật phẩm ngẫu nhiên
    Item thoiVang = ItemService.gI().createNewItem((short) 457, Util.nextInt(1, 5));
    InventoryService.gI().addItemBag(player, thoiVang);
    InventoryService.gI().sendItemBag(player);
    Service.gI().sendThongBao(player, "Bạn nhận được thỏi vàng may mắn!");
    break;
```

---

## Recipe 2: Tạo Một NPC & Menu Sự Kiện Mới

### Bước 1: Khai báo ID NPC trong `ConstNpc.java`
```java
public static final int THAN_TAI_2026 = 115;
```

### Bước 2: Viết Lớp NPC Kế Thừa Trong `models/npc/npc_list/`
Tạo file `ThanTai.java`:
```java
package models.npc.npc_list;

import models.npc.Npc;
import models.player.Player;
import services.Service;

public class ThanTai extends Npc {

    public ThanTai(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            player.idMark.setIndexMenu(0);
            createOtherMenu(player, 0,
                "Xin chào! Ta là Thần Tài.\nNgươi muốn nhận lộc đầu năm chứ?",
                "Nhận Lộc", "Đổi Quà", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.getIndexMenu() == 0) {
                switch (select) {
                    case 0: // Nhận Lộc
                        Service.gI().sendThongBao(player, "Chúc bạn năm mới an khang thịnh vượng!");
                        break;
                    case 1: // Đổi Quà
                        // Chuyển sang menu cấp 2
                        player.idMark.setIndexMenu(1);
                        createOtherMenu(player, 1, "Chọn gói quà ngươi muốn đổi:", "Gói 1", "Gói 2", "Quay lại");
                        break;
                }
            }
        }
    }
}
```

### Bước 3: Đăng ký NPC trong `NpcFactory.java`
Mở [NpcFactory.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/npc/NpcFactory.java), thêm nhánh khởi tạo vào phương thức `createNPC()`:
```java
case ConstNpc.THAN_TAI_2026:
    return new ThanTai(mapId, status, cx, cy, tempId, avartar);
```

---

## Recipe 3: Thêm Một Boss AI Mới Xuất Hiện Trong Map

### Bước 1: Định nghĩa dữ liệu Boss trong `BossesData.java`
```java
public static final BossData SUPER_BROLY_2026 = new BossData(
    "Super Broly Bạo Nộ", // Tên hiển thị
    ConstPlayer.XAYDA,    // Hành tinh
    new short[]{294, 295, 296, -1, -1, -1}, // Trang phục outfit [đầu, thân, chân, túi, cờ, cánh]
    150000,               // Sức đánh
    new int[]{500000000}, // Máu (500 Triệu)
    new int[]{5},         // Map xuất hiện (Map Đảo Kame)
    new int[][]{
        {Skill.KAMEJOKO, 7, 1000},
        {Skill.TAI_DUONG_HA_SAN, 7, 15000}
    },                    // Kỹ năng sử dụng
    new String[]{"|-1|Ai dám thách thức ta?"}, // Lời thoại khi xuất hiện
    new String[]{"|-1|Các ngươi quá yếu ớt!"}, // Lời thoại khi tấn công
    new String[]{"|-1|Ta sẽ trở lại..."},       // Lời thoại khi bị tiêu diệt
    600000                // Thời gian hồi sinh sau khi chết (10 phút)
);
```

### Bước 2: Tạo lớp Boss kế thừa `Boss.java`
Tạo file `SuperBroly.java` trong `models/boss/boss_list/`:
```java
package models.boss.boss_list;

import models.boss.Boss;
import models.boss.BossesData;
import models.player.Player;
import services.RewardService;
import services.Service;

public class SuperBroly extends Boss {

    public SuperBroly() throws Exception {
        super(BossesData.SUPER_BROLY_2026);
    }

    @Override
    public void reward(Player plKill) {
        // Thưởng vật phẩm cho người kết liễu boss
        Service.gI().sendThongBaoAllPlayer("Boss " + this.name + " đã bị tiêu diệt bởi " + plKill.name);
        RewardService.gI().rewardBroly(plKill, this.zone, this.location.x, this.location.y);
    }
}
```

### Bước 3: Đăng ký Boss trong `BossManager.java`
Khởi tạo và đưa Boss vào danh sách quản lý của server:
```java
this.createBoss(BossID.SUPER_BROLY_2026);
```

---

## Recipe 4: Thêm Cặp Gói Tin Mạng Mới (Client $\leftrightarrow$ Server RPC)

Giả sử cần thêm chức năng: **Gửi yêu cầu Bật/Tắt chế độ Auto Nhặt Đồ từ Client lên Server**.

### Bước 1: Khai báo Opcode
- Server: [Cmd_message.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/consts/Cmd_message.java):
  ```java
  public static final byte TOGGLE_AUTO_PICK = -118;
  ```
- Client: `Cmd.cs`:
  ```csharp
  public const sbyte TOGGLE_AUTO_PICK = -118;
  ```

### Bước 2: Viết hàm gửi ở Client (`Service.cs`)
```csharp
public void sendToggleAutoPick(bool isEnable)
{
    Message msg = null;
    try
    {
        msg = new Message((sbyte)-118);
        msg.writer().writeBoolean(isEnable);
        session.sendMessage(msg);
    }
    catch (Exception ex)
    {
        Cout.println("Lỗi gửi auto pick: " + ex.Message);
    }
    finally
    {
        if (msg != null) msg.cleanup();
    }
}
```

### Bước 3: Xử lý ở Server (`Controller.java`)
Trong `onMessage(ISession s, Message _msg)`:
```java
case -118:
    if (player != null) {
        boolean isEnable = _msg.reader().readBoolean();
        player.isAutoPick = isEnable;
        Service.gI().sendThongBao(player, "Đã " + (isEnable ? "bật" : "tắt") + " chế độ tự nhặt!");
    }
    break;
```

---

## Recipe 5: Quy Trình Build & Kiểm Thử Server

1. **Biên Dịch Server**:
   Mở terminal hoặc chạy [build.bat](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/build.bat):
   ```powershell
   .\build.bat
   ```
   Nếu màn hình thông báo: `[SUCCESS] BUILD COMPLETED 100%! Artifact: dist\NROK.jar` tức là toàn bộ mã nguồn Java 21 LTS đã hợp lệ.
2. **Khởi Chạy Kiểm Thử**:
   - Chạy native: [run.bat](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/run.bat) (Tùy chọn 1).
   - Hoặc chạy Docker Compose: `docker compose up -d`.

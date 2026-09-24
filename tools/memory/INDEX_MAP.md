# Bản Đồ Định Tuyến Nhanh (Fast-Path Code Index Map) - NRO Project

Tài liệu này cung cấp chỉ mục định tuyến cấp cao để AI Agent và Lập trình viên định vị file nguồn chính xác trong vòng 1 giây, **chấm dứt việc phải quét toàn bộ codebase**.

---

## 1. Bảng Tra Cứu Nhanh Theo Nghiệp Vụ

| Nghiệp Vụ / Chủ Đề | File Trọng Tâm (Core Files) | Method Then Chốt | File Tri Thức |
| :--- | :--- | :--- | :--- |
| **Vật phẩm, Túi đồ, Dupe đồ** | [ItemService.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/services/ItemService.java), [CombineService.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/services/CombineService.java) | `combine()`, `addItemBag()`, `subQuantityItemsBag()` | [src/services/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/services/GEMINI.md) |
| **Giao dịch người chơi** | `src/services/func/Trade.java`, `TransactionService.java` | `tradeItem()`, `acceptTrade()` | [src/services/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/services/GEMINI.md) |
| **Chỉ số, HP, KI, Tràn số** | [Player.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/player/Player.java), [NPoint.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/player/NPoint.java) | `calPoint()`, `setHp()`, `addHp()` | [src/models/player/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/player/GEMINI.md) |
| **Đệ tử, Pet, Sư phụ** | `src/services/PetService.java`, `Player.java` | `createPet()`, `changePet()` | [src/models/player/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/player/GEMINI.md) |
| **Bản đồ, Quái, Realtime Loop** | [Map.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/map/Map.java), [Zone.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/map/Zone.java), [Mob.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/map/Mob.java) | `Zone.update()`, `Mob.injured()`, `changeMap()` | [src/models/map/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/map/GEMINI.md) |
| **Hệ thống Boss & Event** | [Boss.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/boss/Boss.java), `src/models/boss/BossManager.java` | `Boss.update()`, `Boss.leaveMap()`, `reward()` | [src/models/boss/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/boss/GEMINI.md) |
| **Nhiệm vụ, Danh hiệu** | [TaskDanhHieu.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/task/TaskDanhHieu.java), `TaskService.java` | `checkDoneTask()`, `addCount()` | [src/models/task/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/task/GEMINI.md) |
| **Packet mạng & Socket** | [Controller.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/server/Controller.java), [Service.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/services/Service.java) | `onMessage()`, `sendMessage()` | [src/network/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/network/GEMINI.md) |
| **Database & DAO** | [PlayerDAO.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/database/daos/PlayerDAO.java), [SuperRankDAO.java](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/database/daos/SuperRankDAO.java) | `updatePlayer()`, `loadRank()` | [src/database/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/database/GEMINI.md) |
| **Client Unity C#** | [GameCanvas.cs](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/Client/Client/Assets/Scripts/Game1/GameCanvas.cs), [Panel.cs](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/Client/Client/Assets/Scripts/Game1/Panel.cs), [GameScr.cs](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/Client/Client/Assets/Scripts/Game1/GameScr.cs) | `GameCanvas.paint()`, `Panel.setType()`, `CombatPacketHandler.cs` | [Client/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/Client/Client/Assets/Scripts/GEMINI.md) |

---

## 2. Quy Tắc Điều Hướng Của Agent
Khi nhận một task từ người dùng:
1. Đọc bảng trên hoặc truy vấn [CODE_ROUTING.json](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/tools/memory/CODE_ROUTING.json) để lấy danh sách file liên quan.
2. Đọc file `GEMINI.md` của module đó để nạp Bất Biến Bắt Buộc (Invariants).
3. Chỉ mở và sửa đúng hàm/dòng code cần thiết (Targeted edit). Tuyệt đối **không** dùng `view_file` toàn bộ class lớn nếu chỉ cần sửa một hàm.

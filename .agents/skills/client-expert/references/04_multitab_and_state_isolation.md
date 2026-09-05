# Kiến Trúc 6TabFixed & Cô Lập Trạng Thái Đa Tab (Multi-Tab Isolation)

Tài liệu quy chuẩn xử lý kiến trúc chạy đồng thời 6 tài khoản trên cùng một Client Unity (6TabFixed), kiểm soát rò rỉ biến tĩnh (Static State Leak) và đồng bộ mượt mà.

---

## 1. Bản Chất Kiến Trúc 6TabFixed

Trong client Unity NRO truyền thống (1 Tab):
- Hầu hết các class cốt lõi đều dùng mô hình **Singleton Tĩnh** (`Char.myCharz()`, `GameScr.gI()`, `Panel.gI()`, `TileMap.gI()`, `Service.gI()`).
- Khi nâng cấp lên **6 Tab Chạy Song Song** trong 1 tiến trình:
  - Nếu vẫn giữ Singleton tĩnh dùng chung, Tab 2 di chuyển sẽ làm lệch tọa độ của Tab 1!
  - Tab 1 mở hành trang sẽ đè dữ liệu lên Tab 2!
  - Socket của Tab 3 nhận packet sẽ parse nhầm vào nhân vật của Tab 4!

---

## 2. Các Quy Tắc Sống Còn Để Cô Lập Trạng Thái 6 Tab

### A. Tuyệt Đối Không Dùng Static Field Để Lưu State Nhân Vật / Socket
- **Sai Lầm Chết Người**:
  ```csharp
  public class Session_ME {
      public static Session_ME gI; // ❌ Hỏng: 6 Tab sẽ ghi đè session của nhau!
  }
  public class Char {
      public static Char myChar; // ❌ Hỏng: Tọa độ, HP bị dùng chung giữa 6 tab!
  }
  ```
- **Chuẩn Senior (TabContext Pattern)**:
  Mỗi Tab phải sở hữu một `TabInstance` độc lập:
  ```csharp
  public class TabInstance {
      public int tabId; // 0..5
      public Char myChar;
      public GameScr gameScr;
      public Panel panel;
      public Session_ME session;
      public Controller controller;
      public Service service;
      public TileMap tileMap;
      public bool isFocused;
  }
  ```

---

### B. Kiểm Soát Render & Layout 6 Tab Trên 1 Màn Hình
1. **Grid Layout & Focus Handling**:
   - 6 tab được chia layout theo lưới (2x3 hoặc 1 màn to + 5 màn phụ hoặc tab switching).
   - Chỉ tab nào đang được **Active/Focused** mới nhận input chuột/bàn phím từ hệ thống.
   - Các tab chạy nền (Background Tabs) vẫn phải duy trì chạy `update()` nhận gói tin mạng để không bị server ngắt kết nối (Disconnect do timeout), nhưng có thể giảm tần số vẽ (`paint()`) xuống 15-20 FPS để tiết kiệm GPU/CPU.
2. **Audio & Sound Isolation**:
   - Chỉ phát âm thanh, nhạc nền của tab đang được chọn (`Active Tab`).
   - Tắt âm thanh các tab chạy ngầm để tránh loạn âm thanh (Audio overlapping).

---

### C. Bộ Công Cụ Mod / God Tools Trên Đa Tab (`God/`)
Khi người chơi bật các tính năng Mod Auto (như Tàn Sát, Auto Đậu Thần, Khóa Mục Tiêu, Auto Chat):
1. **Độc lập cờ Mod theo từng Tab**:
   Mỗi tab có thể bật chế độ riêng (Ví dụ: Tab 0 làm acc chính PK, Tab 1-5 làm acc đệ tử tự động farm quái).
2. Không lưu cờ Mod vào biến tĩnh toàn cục (`public static bool isTanSat`). Phải lưu vào `tabInstance.modSettings.isTanSat`.
3. Khi gửi packet từ tool mod (ví dụ tự động đánh quái): Phải gọi qua `tabInstance.service.sendPlayerAttack(...)`, không gọi `Service.gI()`.

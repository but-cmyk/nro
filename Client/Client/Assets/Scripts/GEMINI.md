# Module GEMINI: Client Unity C# Single-Client & Xử Lý Giao Diện (Client Engine)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Xử lý toàn bộ logic đồ họa 2D, input bàn phím/chuột, kiến trúc Single-Client (1 Cửa Sổ = 1 Tài Khoản Duy Nhất), giải mã packet nhị phân và hiển thị GameCanvas.
- **Class trọng tâm**:
  - `GameCanvas.cs` / `MotherCanvas.cs`: Khung vẽ đồ họa chính, điều hướng màn hình (`currentScreen`).
  - `GameScr.cs`: Màn hình gameplay chính (bản đồ, thanh máu, chat, joystick, danh sách nhân vật quanh mình).
  - `Panel.cs`: God class quản lý UI (Hành trang, Cửa hàng, Nâng cấp, Bang hội, Nhiệm vụ).
  - `Session_ME.cs`: Socket client TCP kết nối tới Server cổng 14445.
  - `Controller.cs`: Tiếp nhận packet từ Server và cập nhật trạng thái client.
  - `Service.cs`: Bộ đóng gói và phát packet từ Client lên Server.

## 2. Landmark Index & Core Methods
- `GameCanvas.paint(mGraphics g)`: Vòng lặp vẽ đồ họa 60 FPS (cấm cấp phát bộ nhớ rác GC).
- `GameCanvas.update()`: Cập nhật logic màn hình hiện tại.
- `Panel.setType(int type)`: Mở giao diện tương ứng (Hành trang, Shop, Đập đồ).
- `Controller.onMessage(Message msg)`: Đọc và giải mã opcode từ Server gửi về.
- `CombatPacketHandler.cs` / `TradePacketHandler.cs`: Xử lý chuyên biệt packet chiến đấu và giao dịch.

## 3. Các Bất Biến Bắt Buộc
- **Đồng Bộ Dòng Đọc/Ghi Với Server (Protocol Sync 1:1)**:
  - Mọi packet gửi từ `Service.cs` lên Server hoặc nhận tại `Controller.cs` phải đối ứng từng byte với phía Server Java (`writeByte`, `writeShort`, `writeInt`, `writeUTF`).
- **Reset Cờ Trạng Thái Toàn Cục (State Flag Cleanliness)**:
  - Khi chuyển màn hình (từ `CreateCharScr`, `LoginScr` sang `GameScr`), toàn bộ cờ tạm (`isCreateChar`, `isLoadingMap`, `isGetData`, popup `InfoDlg`) bắt buộc phải được reset để tránh treo giao diện.
- **Kiến Trúc Single-Client Thuần Túy**:
  - Không duy trì bất kỳ mã tàn dư nào liên quan đến đa tab (multi-tab) hay chia sẻ session chéo; mỗi instance game độc lập 100%.
- Tối Ưu GC Paint UI: Cấm gọi Substring hoặc nối chuỗi trong paintMultiLine/paint 60 FPS; phải pre-parse màu trước khi render
- Tach biet Target HUD va Player Info: Khong tai su dung vi tri Target Bar cho Suc Manh ban than de tranh mat so khi target quai
- Quy tắc Option Hiển Thị: Dummy option (ID 73, 206) và template rỗng/placeholder phải trả về string.Empty trong getOptionString(); tuyệt đối không fallback in chuỗi debug thô Option <id> cho người chơi.
- Khóa Đồng Bộ Luồng: Luôn đảm bảo lock trạng thái khi thao tác đa luồng hoặc spam packet để chống bug dupe. `[2026-09-24]`
- Kiểm tra quantity > 0 trước khi trừ, không bao giờ để quantity âm. `[2026-09-24]`
- Đồng Bộ Trạng Thái GUIStyle IMGUI: Khi vẽ text bằng GUIStyle/GUI.Label, phải gán đồng bộ textColor cho toàn bộ trạng thái (hover, active, focused, on*) bằng color1 để tránh text đổi màu trắng khi rê chuột hoặc click. `[2026-09-24]`
- myReader trong Unity tra ve 0 khi EOF (khong nem Exception). Do do khi doc subCommand 62 (nang cap skill), neu khong kiem tra available() > 0 truoc khi readSByte(), bien b bi gan 0 khien client nham sang cap nhat exp chieu dac biet thay vi cap nhat level skill vSkill. `[2026-09-24]`
- Phòng Ngừa Tràn Số: Luôn sử dụng kiểu `long` cho các trường tích lũy (HP, sức mạnh, tiền, kinh nghiệm) để tránh tràn mốc 2 tỷ. `[2026-09-24]`
- Đồng Bộ Gói Tin Nhị Phân 1:1: Mọi thứ tự write/read packet giữa Server Java và Client Unity C# phải hoàn toàn tương ứng. `[2026-09-24]`
- Dùng CopyOnWriteArrayList/ConcurrentHashMap cho collection duyệt trong vòng lặp Zone update. `[2026-09-24]`
- **Kiến Trúc Audio Môi Trường & SFX**: Phân tách rõ các AudioSource (`SoundBGLoop` cho nhạc nền, `SoundWater` cho âm thanh môi trường như mưa rơi/nước chảy, `SoundRun` cho di chuyển, `PlayOneShot` cho SFX). Không trừ volume khiến âm lượng bị triệt tiêu (về 0.01f), luôn kiểm tra trạng thái rain active trong updateEff() để tự động bật/tắt loop âm thanh môi trường theo map. `[2026-09-26]`

## 4. Lỗi Thường Gặp Cần Tránh
- Treo đơ UI (Freeze) khi Server phản hồi chậm hoặc disconnect nhưng Client vẫn chờ trong `InfoDlg.showWait()`.
- Rò rỉ GC Allocations do tạo mới chuỗi string và mảng trong hàm `paint()` hoặc `update()` chạy 60 FPS.

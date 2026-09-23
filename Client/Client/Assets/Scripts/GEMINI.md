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

## 2. Các Bất Biến Bắt Buộc
- **Đồng Bộ Dòng Đọc/Ghi Với Server (Protocol Sync 1:1)**:
  - Mọi packet gửi từ `Service.cs` lên Server hoặc nhận tại `Controller.cs` phải đối ứng từng byte với phía Server Java (`writeByte`, `writeShort`, `writeInt`, `writeUTF`).
- **Reset Cờ Trạng Thái Toàn Cục (State Flag Cleanliness)**:
  - Khi chuyển màn hình (từ `CreateCharScr`, `LoginScr` sang `GameScr`), toàn bộ cờ tạm (`isCreateChar`, `isLoadingMap`, `isGetData`, popup `InfoDlg`) bắt buộc phải được reset để tránh treo giao diện.
- **Kiến Trúc Single-Client Thuần Túy**:
  - Không duy trì bất kỳ mã tàn dư nào liên quan đến đa tab (multi-tab) hay chia sẻ session chéo; mỗi instance game độc lập 100%.

## 3. Lỗi Thường Gặp Cần Tránh
- Treo đơ UI (Freeze) khi Server phản hồi chậm hoặc disconnect nhưng Client vẫn chờ trong `InfoDlg.showWait()`.
- Rò rỉ GC Allocations do tạo mới chuỗi string và mảng trong hàm `paint()` hoặc `update()` chạy 60 FPS.

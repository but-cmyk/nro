# Module GEMINI: Client Unity C# Đa Tab & Xử Lý Giao Diện (Client Engine)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Xử lý toàn bộ logic đồ họa, input bàn phím/chuột, quản lý đa tab (6 tab tài khoản đồng thời), giải mã packet và hiển thị GameCanvas.
- **Class trọng tâm**:
  - `GameCanvas.cs` / `MotherCanvas.cs`: Khung vẽ đồ họa chính, điều hướng màn hình (`currentScreen`).
  - `GameScr.cs`: Màn hình gameplay chính (bản đồ, thanh máu, chat, joystick, danh sách nhân vật quanh mình).
  - `Panel.cs`: God class quản lý UI (Hành trang, Cửa hàng, Nâng cấp, Bang hội, Nhiệm vụ).
  - `Session_ME.cs`: Socket client TCP kết nối tới Server.
  - `Controller.cs`: Tiếp nhận packet từ Server và cập nhật trạng thái client.
  - `TabManagement.cs`: Quản lý chuyển đổi giữa 6 tab game.

## 2. Các Bất Biến Bắt Buộc
- **Đồng Bộ Dòng Đọc/Ghi Với Server**:
  - Mọi packet gửi từ `Service.cs` lên Server hoặc nhận tại `Controller.cs` phải đối ứng từng byte với phía Server Java.
- **Reset Cờ Trạng Thái Toàn Cục (State Flag Cleanliness)**:
  - Khi chuyển màn hình (từ `CreateCharScr`, `LoginScr` sang `GameScr`), toàn bộ cờ tạm (`isCreateChar`, `isLoadingMap`, `isGetData`, popup `InfoDlg`) bắt buộc phải được reset để tránh treo giao diện.
- **Cô Lập Dữ Liệu Đa Tab**:
  - Tránh lạm dụng biến static toàn cục chia sẻ dữ liệu nhân vật giữa các tab, gây hiện tượng click tab 1 nhưng ảnh hưởng nhân vật ở tab 2.

## 3. Lỗi Thường Gặp Cần Tránh
- Treo đơ UI (Freeze) khi Server phản hồi chậm hoặc disconnect nhưng Client vẫn chờ trong `InfoDlg.showWait()`.
- Rò rỉ GC Allocations do tạo mới chuỗi string và mảng trong hàm `paint()` hoặc `update()` chạy 60 FPS.

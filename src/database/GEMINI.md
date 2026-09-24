# Module GEMINI: Tầng Lưu Trữ & Quản Trị Cơ Sở Dữ Liệu (Database Layer)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Quản lý pool kết nối HikariCP, thực thi truy vấn CRUD cho tài khoản, người chơi, bang hội, sự kiện và nhật ký giao dịch.
- **Class trọng tâm**:
  - `AlyraManager.java`: Quản trị DataSource, cấp phát `Connection` an toàn.
  - `PlayerDAO.java`: Lưu và cập nhật trạng thái người chơi (`updatePlayer`, `createNewPlayer`).
  - `NDVSqlFetcher.java`: Nạp dữ liệu toàn diện của người chơi khi login.
  - `SuperRankDAO.java`: Dữ liệu bảng xếp hạng võ đài liên vũ trụ.

## 2. Landmark Index & Core Methods
- `AlyraManager.getConnection()`: Lấy connection từ HikariCP pool.
- `PlayerDAO.updatePlayer(Player player)`: Ghi nhận dữ liệu nhân vật vào MySQL.
- `PlayerDAO.createNewPlayer(int userId, String name, ...)`: Tạo nhân vật mới với các bảng con mặc định.
- `NDVSqlFetcher.loadPlayer(Session session, int userId)`: Parse JSON dữ liệu nạp vào đối tượng Player.

## 3. Các Bất Biến Bắt Buộc
- **Nguyên Tắc Đóng Tài Nguyên Tuyệt Đối**:
  - Bắt buộc 100% các hàm truy cập DB phải dùng cú pháp `try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = ...; ResultSet rs = ...) { ... }`.
  - Không bao giờ lưu giữ đối tượng `Connection` dưới dạng biến static dùng chung.
- **Chuẩn Hóa MySQL 8.0**:
  - Từ khóa hệ thống như `rank`, `order`, `group` bắt buộc phải được bọc trong dấu backtick: `` `rank` `` để tránh crash cú pháp trên MySQL 8.x.
- **Serialize JSON Nhất Quán**:
  - Dữ liệu túi đồ, chỉ số được chuyển thành chuỗi JSON trước khi ghi vào MySQL. Nếu xảy ra lỗi phân tích cú pháp JSON khi load, phải có cơ chế fallback an toàn, không được để crash thread login.

## 4. Lỗi Thường Gặp Cần Tránh
- Rò rỉ kết nối HikariCP dẫn tới lỗi `Timeout: Connection is not available, request timed out after 30000ms`.
- Lỗi cập nhật dữ liệu hàng loạt khi tắt Server đột ngột (phải dùng hook shutdown an toàn).

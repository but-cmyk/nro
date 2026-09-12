# Module GEMINI: Vật Phẩm, Túi Đồ & Chống Bug Dupe Đồ (Economy Core)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Xử lý toàn bộ logic liên quan tới vật phẩm, giao dịch người chơi, nâng cấp/ép sao pha lê, cửa hàng và ký gửi.
- **Class trọng tâm**:
  - `ItemService.java`: Tạo item từ template, thêm/bớt item vào túi, kiểm tra slot trống.
  - `CombineService.java`: Logic đập đồ, nâng cấp trang bị, pha lê hóa tại NPC Bà Hạt Mít.
  - `Trade.java` / `TransactionService.java`: Giao dịch an toàn giữa 2 người chơi.
  - `ShopService.java` / `ConsignShopService.java`: Hệ thống bán hàng, ký gửi.

## 2. Các Bất Biến Bắt Buộc
- **Bảo Vệ Đơn Hàng & Thao Tác Khoản Mục**:
  - Trước khi trừ nguyên liệu hoặc item, bắt buộc phải kiểm tra: `item != null && item.quantity >= quantityRequired`.
  - Tuyệt đối không cho phép số lượng âm (`quantity <= 0`) trong túi đồ hoặc rương.
- **Khóa Giao Dịch Chống Dupe (Concurrency Lock)**:
  - Trong quá trình Trade hoặc Combine, item phải được "khóa trạng thái" (lock) để ngăn chặn việc người chơi lợi dụng spam packet cùng một lúc từ nhiều client/tab để nhân bản đồ.
- **Clone Item An Toàn**:
  - Khi chuyển item từ Shop hoặc Template vào túi người chơi, luôn dùng `ItemService.gI().createNewItem(...)` hoặc deep-copy, cấm gán tham chiếu trực tiếp từ Template tĩnh.

## 3. Lỗi Thường Gặp Cần Tránh
- Dupe đồ qua ngắt kết nối mạng ngay thời điểm xác nhận giao dịch.
- Trừ item thành công nhưng ném ngoại lệ khi add item mới dẫn đến mất đồ của người chơi.

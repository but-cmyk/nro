# Module GEMINI: Vật Phẩm, Túi Đồ & Chống Bug Dupe Đồ (Economy Core)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Xử lý toàn bộ logic liên quan tới vật phẩm, giao dịch người chơi, nâng cấp/ép sao pha lê, cửa hàng và ký gửi.
- **Class trọng tâm**:
  - `ItemService.java`: Tạo item từ template, thêm/bớt item vào túi, kiểm tra slot trống.
  - `CombineService.java`: Logic đập đồ, nâng cấp trang bị, pha lê hóa tại NPC Bà Hạt Mít.
  - `Trade.java` / `TransactionService.java`: Giao dịch an toàn giữa 2 người chơi.
  - `ShopService.java` / `ConsignShopService.java`: Hệ thống bán hàng, ký gửi.

## 2. Landmark Index & Core Methods
- `ItemService.createNewItem(short tempId, int quantity)`: Khởi tạo instance item độc lập, deep-copy từ template.
- `ItemService.addItemBag(Player player, Item item)`: Thêm item vào hành trang có kiểm tra ô trống.
- `ItemService.subQuantityItemsBag(Player player, Item item, int quantity)`: Trừ số lượng an toàn.
- `CombineService.combine(Player player)`: Thực thi đập đồ có khóa đối tượng chống spam packet dupe.
- `Trade.tradeItem()`: Chuyển giao đồ giữa 2 người chơi có lock trạng thái 2 chiều.

## 3. Các Bất Biến Bắt Buộc
- **Bảo Vệ Đơn Hàng & Thao Tác Khoản Mục**:
  - Trước khi trừ nguyên liệu hoặc item, bắt buộc phải kiểm tra: `item != null && item.quantity >= quantityRequired`.
  - Tuyệt đối không cho phép số lượng âm (`quantity <= 0`) trong túi đồ hoặc rương.
- **Khóa Giao Dịch Chống Dupe (Concurrency Lock)**:
  - Trong quá trình Trade hoặc Combine, item phải được "khóa trạng thái" (lock) để ngăn chặn việc người chơi lợi dụng spam packet cùng một lúc từ nhiều client/tab để nhân bản đồ.
- **Clone Item An Toàn**:
  - Khi chuyển item từ Shop hoặc Template vào túi người chơi, luôn dùng `ItemService.gI().createNewItem(...)` hoặc deep-copy, cấm gán tham chiếu trực tiếp từ Template tĩnh.
- Bảo Vệ Giao Dịch ACID: Lưu DB đồng bộ trong Trade; nếu DB lưu thất bại bắt buộc phải rollback RAM về bagBefore để chống bug dupe qua ngắt kết nối
- Khóa Đồng Bộ Luồng: Luôn đảm bảo lock trạng thái khi thao tác đa luồng hoặc spam packet để chống bug dupe. `[2026-09-24]`
- Kiểm tra quantity > 0 trước khi trừ, không bao giờ để quantity âm. `[2026-09-24]`
- Đồng bộ Stream Shop Ký Gửi: Gói -44 và -100 phải ghi writeUTF(sellerName) khi version >= 237 để khớp với Controller Client tránh lệch byte stream. `[2026-09-24]`
- Phòng Ngừa Tràn Số: Luôn sử dụng kiểu `long` cho các trường tích lũy (HP, sức mạnh, tiền, kinh nghiệm) để tránh tràn mốc 2 tỷ. `[2026-09-24]`
- Đồng Bộ Gói Tin Nhị Phân 1:1: Mọi thứ tự write/read packet giữa Server Java và Client Unity C# phải hoàn toàn tương ứng. `[2026-09-24]`
- Dùng CopyOnWriteArrayList/ConcurrentHashMap cho collection duyệt trong vòng lặp Zone update. `[2026-09-24]`

## 4. Lỗi Thường Gặp Cần Tránh
- Dupe đồ qua ngắt kết nối mạng ngay thời điểm xác nhận giao dịch.
- Trừ item thành công nhưng ném ngoại lệ khi add item mới dẫn đến mất đồ của người chơi.

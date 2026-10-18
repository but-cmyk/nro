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
- Đồng bộ Stream Shop Ký Gửi: Gói -44 và -100 phải ghi writeUTF(sellerName) khi version >= 237 để khớp với Controller Client tránh lệch byte stream. `[2026-09-24]`
- Cơ Chế Mở Nội Tại 3 Phân Tầng: Top 25% dải chỉ số cao nhất là Cực khó ra (2%), 25% dải kế tiếp là Khó ra (13%), 50% dải còn lại là Bình thường (85%); dùng Util.nextInt(from, to) inclusive chuẩn xác `[2026-09-27]`
- Vòng quay Thượng Đế: Lock synchronized(player) bao trọn kiểm tra và trừ tiền (subGold long, subGem), hạn chế tỷ lệ Thỏi vàng dưới 5% chống lạm phát farm vàng vô hạn `[2026-09-28]`
- Vòng quay Thượng Đế: Bỏ quay vàng, chỉ giữ quay ngọc và quay vé. Vé quay (ID 821) cày free qua Boss rơi (40%) và Nhiệm vụ hàng ngày (1-5 vé). Phần thưởng chuẩn logic NRO: Ngọc rồng, Đá nâng cấp, Đá bảo vệ, Thức ăn, Cải trang. `[2026-09-28]`
- Khi nạp options từ DB trong parseItemSafe, bắt buộc clear itemOptions mặc định trước khi duyệt nạp options để tránh nhân bản trùng lặp options sau mỗi lần lưu/tải (đặc biệt với vật phẩm có default option như Cà chua 191, Cà rốt 192). `[2026-10-02]`
- Bat buoc dung sendThongBaoResetWait (gui kem opcode -99 truoc khi sendThongBao) tai moi handler tu choi UI/Menu/Shop de giai phong InfoDlg ngay lap tuc; thay toan bo cau generic bang thong bao ro nghiep vu Dragon Ball va dung chinh ta hanh trang (ref L0008) `[2026-10-04]`
- Trong parseItemSafe bat buoc clear itemOptions mac dinh truoc khi duyet nap options de tranh nhan ban options (ref L0001) `[2026-10-18]`

## 4. Lỗi Thường Gặp Cần Tránh
- Dupe đồ qua ngắt kết nối mạng ngay thời điểm xác nhận giao dịch.
- Trừ item thành công nhưng ném ngoại lệ khi add item mới dẫn đến mất đồ của người chơi.

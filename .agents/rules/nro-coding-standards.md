# Quy Chuẩn Lập Trình Dự Án Ngọc Rồng Online (NRO Coding Standards)

Quy tắc này áp dụng tự động cho toàn bộ mã nguồn của dự án Ngọc Rồng Online (Server Java 21 LTS và Client Unity C# `6TabFixed`). Mọi agent khi đọc, phân tích, sửa đổi hoặc thêm code mới BẮT BUỘC tuân thủ các nguyên tắc sau:

---

## 1. Nguyên Tắc Thép Về Giao Thức Mạng (Binary Packet Protocol)
1. **Khớp Thứ Tự Kiểu Dữ Liệu 1:1 Tuyệt Đối**:
   - Khi Server gửi gói tin (`Service.java`), Client (`Controller.cs` hoặc `IPacketHandler`) phải đọc chính xác từng byte theo đúng thứ tự.
   - Bảng kiểu dữ liệu tương ứng:
     - `writeByte(b)` $\leftrightarrow$ `readByte()` (sbyte trong C#, byte trong Java: -128 đến 127).
     - `readUnsignedByte()` (0 đến 255) chỉ dùng khi giá trị luôn dương (như số lượng chỉ số option, loại hành động).
     - `writeShort(s)` $\leftrightarrow$ `readShort()` (16-bit signed).
     - `writeInt(i)` $\leftrightarrow$ `readInt()` (32-bit signed).
     - `writeLong(l)` $\leftrightarrow$ `readLong()` (64-bit signed).
     - `writeUTF(str)` $\leftrightarrow$ `readUTF()` (chuỗi String mã hóa UTF-8 kèm tiền tố độ dài 2 byte).
   - **Bảo hiểm Version**: Kiểm tra phiên bản client (`player.getSession().version`) nếu có sự khác biệt (ví dụ: version >= 222 dùng `readInt()` cho số lượng item, version thấp hơn dùng `readByte()`).
2. **Giải Phóng Bộ Nhớ Gói Tin**:
   - Ở Server: Sau khi khởi tạo `Message msg = new Message(cmd);`, luôn đặt trong khối `try...finally` và gọi `msg.cleanup();` nếu xảy ra lỗi hoặc sau khi đã `session.sendMessage(msg);`.
   - Ở Client: Tương tự, luôn có khối `finally { if (message != null) message.cleanup(); }`.

---

## 2. Nguyên Tắc An Toàn Đa Luồng & Chống Dupe Đồ (Thread-Safety & Anti-Dupe)
1. **Thao Tác Hành Trang & Vật Phẩm**:
   - Không bao giờ sửa đổi số lượng hoặc xóa item trong `itemsBag`, `itemsBox`, `itemsBody` mà không kiểm tra tồn tại và số lượng khả dụng.
   - Khi giao dịch (`Trade`), ký gửi (`ConsignShop`), nâng cấp (`Combine`), hoặc chuyển đồ qua rương:
     - Phải kiểm tra trạng thái bận (`TransactionService.gI().check(player)`).
     - Phải kiểm tra bảo vệ tài khoản (`player.baovetaikhoan`).
     - Sử dụng đồng bộ (`synchronized`) trên đối tượng người chơi hoặc đối tượng giao dịch để ngăn race condition (2 luồng rút đồ cùng 1 mili-giây).
2. **Duyệt Danh Sách Trong Map / Zone**:
   - Không bao giờ lặp trực tiếp trên `zone.getPlayers()` hoặc `zone.items` nếu trong vòng lặp có khả năng người chơi thoát map hoặc nhặt vật phẩm.
   - Luôn tạo bản sao an toàn: `List<Player> players = new ArrayList<>(zone.getPlayers());` trước khi lặp, hoặc sử dụng Concurrent collection để tránh `ConcurrentModificationException`.

---

## 3. Nguyên Tắc Tính Toán Chỉ Số (NPoint Integrity)
1. **Quy Trình Cập Nhật Chỉ Số**:
   - Mọi thay đổi về trang bị (mặc đồ, tháo đồ), hợp thể Porata, cắn bùa, biến khỉ, kích hoạt nội tại, thay đổi cải trang BẮT BUỘC phải gọi:
     ```java
     player.nPoint.calPoint();
     Service.gI().point(player); // Đồng bộ thanh HP/MP/Dame về Client
     ```
2. **Ngăn Chặn Âm Máu & Bất Tử**:
   - Trong `NPoint.setHp()`, `setMp()`, không bao giờ để giá trị vượt quá `hpMax`/`mpMax` hoặc rơi vào trạng thái âm mà không xử lý chết (`isDie = true`).
   - Thứ tự tính toán trong `calPoint()`:
     1. Reset chỉ số cộng thêm (`hpAdd = 0; mpAdd = 0; dameAdd = 0; defAdd = 0; critAdd = 0;`).
     2. Xóa danh sách tỷ lệ (`tlHp.clear(); tlMp.clear(); tlDef.clear(); tlDame.clear();`).
     3. Đọc options từ 6-8 món trang bị (`itemsBody`).
     4. Kích hoạt set đồ (Set thần linh, set kích hoạt Kaioken/Galick/Kame...).
     5. Áp dụng nội tại (`Intrinsic`) và Bùa (`Charms`).
     6. Áp dụng Biến hình (Khỉ nhân HP/Dame) và Hợp thể Porata (cộng chỉ số Đệ tử).
     7. Giới hạn theo mốc sức mạnh (`limitPower`).

---

## 4. Nguyên Tắc Cơ Sở Dữ Liệu (Database & Persistence)
1. **Giải Phóng Kết Nối HikariCP**:
   - Luôn sử dụng `try-with-resources` cho mọi thao tác JDBC:
     ```java
     try (Connection con = AlyraManager.getConnection();
          PreparedStatement ps = con.prepareStatement(sql)) {
         // Thực thi truy vấn
     } catch (SQLException e) {
         Logger.error("Lỗi truy vấn SQL: " + e.getMessage());
     }
     ```
   - Tuyệt đối không để rò rỉ `Connection`, nếu không toàn bộ pool sẽ treo sau vài giờ.
2. **Đồng Bộ Dữ Liệu Hai Chiều**:
   - `nro_acc`: Chứa thông tin bảo mật, tài khoản, mật khẩu bCrypt, số dư tiền tệ, trạng thái ban.
   - `nro_data`: Chứa dữ liệu trò chơi dưới dạng JSON (`items_bag`, `items_box`, `skills`, `pet_info`). Khi thêm thuộc tính mới vào nhân vật, phải cập nhật cả hàm nạp `NDVSqlFetcher.loadPlayer()` và hàm lưu `PlayerDAO.savePlayer()`.

---

## 5. Nguyên Tắc Phát Triển Client Unity C#
1. **Kiến Trúc Module Hóa**:
   - Sử dụng `PacketDispatcher` và `IPacketHandler` để xử lý các gói tin mới, tránh nhồi nhét thêm hàng nghìn dòng vào `Controller.cs`.
   - Sử dụng `UIWindowManager` và `IUIWindow` cho các cửa sổ giao diện mới thay vì can thiệp trực tiếp vào mã nguồn nguyên thủy của `GameCanvas` hay `Panel.cs`.
2. **Khả Năng Đa Tab (Multi-Tab Safety)**:
   - Các biến trạng thái của nhân vật đang chơi phải lấy qua `Char.myCharz()` của phiên hiện tại, không dùng biến static toàn cục làm lẫn lộn dữ liệu giữa 6 tab.

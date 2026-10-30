# Báo Cáo Rà Soát Toàn Diện Mã Nguồn & Kiến Trúc Dự Án NRO

- **Thời gian thực hiện**: `2026-10-30 09:08:41`
- **Tổng số vấn đề phát hiện**: **16**

| Mức Độ | Số Lượng | Ý Nghĩa Thực Tế |
| :--- | :---: | :--- |
| 🔴 **P0 - CRITICAL** | **0** | Nguy cơ sập Server/Client, Dupe đồ, Sập pool database. |
| 🟠 **P1 - HIGH** | **16** | Tràn số âm điểm, Race condition, Mất item khi disconnect. |
| 🟡 **P2 - MEDIUM** | **0** | GC frame drop giật lag mobile, Thiếu backtick MySQL 8. |
| 🟢 **P3 - LOW / INFO** | **0** | Code smells, dead code, tối ưu thuật toán. |

## Danh Sách Chi Tiết Các Vấn Đề Thật

### 1. [P1 HIGH] Truy vấn JDBC không dùng try-with-resources
- **Vị trí**: `src\database\daos\PlayerDAO.java:1228`
- **Chiều phân tích**: `Database`
- **Triệu chứng thực tế**: Rò rỉ connection pool HikariCP khi xảy ra ngoại lệ SQL, dẫn đến cạn kiệt pool và đơ server.
- **Nguyên nhân gốc rễ**: `Lệnh mở tài nguyên: `con = AlyraManager.getConnection();``
- **Giải pháp khắc phục chuẩn**: Chuyển sang cú pháp: try (Connection conn = ...; PreparedStatement ps = ...) { ... }

### 2. [P1 HIGH] Truy vấn JDBC không dùng try-with-resources
- **Vị trí**: `src\database\daos\PlayerDAO.java:1230`
- **Chiều phân tích**: `Database`
- **Triệu chứng thực tế**: Rò rỉ connection pool HikariCP khi xảy ra ngoại lệ SQL, dẫn đến cạn kiệt pool và đơ server.
- **Nguyên nhân gốc rễ**: `Lệnh mở tài nguyên: `ps = con.prepareStatement(QUERY_UPDATE_PLAYER);``
- **Giải pháp khắc phục chuẩn**: Chuyển sang cú pháp: try (Connection conn = ...; PreparedStatement ps = ...) { ... }

### 3. [P1 HIGH] Truy vấn JDBC không dùng try-with-resources
- **Vị trí**: `src\database\daos\PlayerDAO.java:1299`
- **Chiều phân tích**: `Database`
- **Triệu chứng thực tế**: Rò rỉ connection pool HikariCP khi xảy ra ngoại lệ SQL, dẫn đến cạn kiệt pool và đơ server.
- **Nguyên nhân gốc rễ**: `Lệnh mở tài nguyên: `.prepareStatement("update account set diem_da_nhan = (diem_da_nhan + ?) where id = ?")) {``
- **Giải pháp khắc phục chuẩn**: Chuyển sang cú pháp: try (Connection conn = ...; PreparedStatement ps = ...) { ... }

### 4. [P1 HIGH] Truy vấn JDBC không dùng try-with-resources
- **Vị trí**: `src\database\daos\PlayerDAO.java:1716`
- **Chiều phân tích**: `Database`
- **Triệu chứng thực tế**: Rò rỉ connection pool HikariCP khi xảy ra ngoại lệ SQL, dẫn đến cạn kiệt pool và đơ server.
- **Nguyên nhân gốc rễ**: `Lệnh mở tài nguyên: `.prepareStatement("update account set sotien = (sotien + ?) where username = ? ")) {``
- **Giải pháp khắc phục chuẩn**: Chuyển sang cú pháp: try (Connection conn = ...; PreparedStatement ps = ...) { ... }

### 5. [P1 HIGH] Truy vấn JDBC không dùng try-with-resources
- **Vị trí**: `src\database\daos\PlayerDAO.java:1731`
- **Chiều phân tích**: `Database`
- **Triệu chứng thực tế**: Rò rỉ connection pool HikariCP khi xảy ra ngoại lệ SQL, dẫn đến cạn kiệt pool và đơ server.
- **Nguyên nhân gốc rễ**: `Lệnh mở tài nguyên: `.prepareStatement("update account set danap = (danap + ?) where username = ?")) {``
- **Giải pháp khắc phục chuẩn**: Chuyển sang cú pháp: try (Connection conn = ...; PreparedStatement ps = ...) { ... }

### 6. [P1 HIGH] Truy vấn JDBC không dùng try-with-resources
- **Vị trí**: `src\database\daos\PlayerDAO.java:1746`
- **Chiều phân tích**: `Database`
- **Triệu chứng thực tế**: Rò rỉ connection pool HikariCP khi xảy ra ngoại lệ SQL, dẫn đến cạn kiệt pool và đơ server.
- **Nguyên nhân gốc rễ**: `Lệnh mở tài nguyên: `.prepareStatement("update account set danap = (danap + ?) where username = ?")) {``
- **Giải pháp khắc phục chuẩn**: Chuyển sang cú pháp: try (Connection conn = ...; PreparedStatement ps = ...) { ... }

### 7. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:653`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 8. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:745`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().subQuantityItemsBag(player, pGG, 1);` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 9. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:762`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().addItemBag(player, item);` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 10. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:840`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, itSpec),` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 11. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:965`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().subQuantityItemsBag(pl, item, quantity);` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 12. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:1002`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `if (InventoryService.gI().addItemBag(player, item)) {` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 13. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:1029`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `if (InventoryService.gI().addItemBag(player, it)) {` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 14. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:1077`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().addItemBag(player, item);` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 15. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:1138`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().subQuantityItemsBag(player, doAn, 99);` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

### 16. [P1 HIGH] Thao tác biến đổi Item thiếu khối khóa synchronized
- **Vị trí**: `src\services\ShopService.java:1192`
- **Chiều phân tích**: `Security`
- **Triệu chứng thực tế**: Người chơi có thể dupe đồ bằng cách gửi liên tục 2 packet thao tác cùng lúc trong vài mili-giây.
- **Nguyên nhân gốc rễ**: `Gọi trừ/cộng đồ: `InventoryService.gI().addItemBag(player, item);` không nằm trong synchronized block.`
- **Giải pháp khắc phục chuẩn**: Bọc toàn bộ khối kiểm tra và trừ/cộng vật phẩm trong synchronized (player.inventory.itemsBag) { ... }

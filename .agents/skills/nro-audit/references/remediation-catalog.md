# Cẩm Nang Mẫu Sửa Chuẩn Hóa Cho Dự Án NRO (Remediation Catalog)

Tài liệu này tập hợp các mẫu giải pháp sửa lỗi chuẩn mực (Before $\leftrightarrow$ After) cho 6 nhóm vấn đề nan giải nhất của dự án NRO. Mọi giải pháp đưa ra trong quá trình Audit bắt buộc phải tham chiếu và tuân thủ các khuôn mẫu này.

---

## 1. An Ninh: Mẫu Khóa Đồng Bộ Chống Dupe Đồ (Anti-Dupe Lock Pattern)

### 🔴 Triệu chứng lỗi (Vấn đề thật):
Người chơi mở 2 tab hoặc gửi 2 gói tin Combine / Trade liên tiếp trong 5ms. Cả 2 luồng đều kiểm tra thấy item còn trong túi và nhân đôi vật phẩm.

### ❌ Code Lỗi (Trước khi sửa):
```java
// KHÔNG AN TOÀN: Kiểm tra và trừ item không đồng bộ
public void combineItem(Player player, int type) {
    Item item = player.inventory.findItemBag(457);
    if (item != null && item.quantity >= 10) {
        // Luồng 2 có thể lọt vào đây trước khi luồng 1 trừ số lượng!
        InventoryService.gI().subQuantityItemsBag(player, item, 10);
        Item reward = ItemService.gI().createNewItem((short) 1150);
        InventoryService.gI().addItemBag(player, reward);
    }
}
```

### ✅ Code Chuẩn (Sau khi sửa):
```java
// AN TOÀN TUYỆT ĐỐI: Khóa đồng bộ trên lock player hoặc inventory
public void combineItem(Player player, int type) {
    if (player == null || player.inventory == null) return;
    
    // 1. Cooldown thao tác chống spam packet
    long now = System.currentTimeMillis();
    if (now - player.lastTimeCombine < 500) {
        Service.gI().sendThongBao(player, "Thao tác quá nhanh, vui lòng chờ!");
        return;
    }
    player.lastTimeCombine = now;

    // 2. Khóa đồng bộ danh sách túi đồ
    synchronized (player.inventory.itemsBag) {
        Item item = player.inventory.findItemBag(457);
        if (item != null && item.quantity >= 10) {
            InventoryService.gI().subQuantityItemsBag(player, item, 10);
            Item reward = ItemService.gI().createNewItem((short) 1150);
            InventoryService.gI().addItemBag(player, reward);
            InventoryService.gI().sendItemBags(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ vật phẩm!");
        }
    }
}
```

---

## 2. Giao Thức: Mẫu Đồng Bộ Stream 1:1 Server Java $\longleftrightarrow$ Client C#

### 🔴 Triệu chứng lỗi (Vấn đề thật):
Server gửi thêm trường thông tin mới nhưng Client đọc thiếu hoặc sai kiểu dữ liệu, gây lỗi `EndOfStreamException` hoặc mất kết nối.

### ❌ Code Lỗi:
```java
// Server Java ghi 4 trường:
msg.writer().writeByte(type);
msg.writer().writeInt(id);
msg.writer().writeUTF(name);
msg.writer().writeLong(power); // Trường mới thêm
```
```csharp
// Client C# chỉ đọc 3 trường hoặc đọc sai kiểu:
sbyte type = msg.reader().readByte();
int id = msg.reader().readInt();
string name = msg.reader().readUTF();
// THIẾU readLong() -> Dữ liệu phía sau bị lệch toàn bộ!
```

### ✅ Code Chuẩn:
```java
// Server Java (Controller.java / Service.java):
Message msg = new Message(-105);
msg.writer().writeByte(type);
msg.writer().writeInt(id);
msg.writer().writeUTF(name != null ? name : "");
msg.writer().writeLong(power);
player.sendMessage(msg);
msg.cleanup();
```
```csharp
// Client C# (Controller.cs):
case -105: {
    sbyte type = msg.reader().readByte();
    int id = msg.reader().readInt();
    string name = msg.reader().readUTF();
    long power = msg.reader().readLong(); // Khớp tuyệt đối 1:1 từng byte!
    GameScr.gI().updatePlayerPower(id, power);
    break;
}
```

---

## 3. Database: Mẫu JDBC Try-With-Resources & Backtick MySQL 8

### 🔴 Triệu chứng lỗi (Vấn đề thật):
Kết nối database không đóng khi có exception làm cạn kiệt pool HikariCP (lỗi `Connection timeout: No available connections`). Tên cột như `rank`, `order` gây syntax error trên MySQL 8.0+.

### ❌ Code Lỗi:
```java
// KHÔNG AN TOÀN: Rò rỉ connection nếu xảy ra lỗi SQL
Connection conn = AlyraManager.getConnection();
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT id, rank, group FROM player");
while (rs.next()) { ... }
// Nếu rs.next() quăng exception, conn không bao giờ được đóng!
```

### ✅ Code Chuẩn:
```java
// AN TOÀN TUYỆT ĐỐI: 100% tài nguyên tự động đóng, cột bọc backtick MySQL 8
String sql = "SELECT `id`, `rank`, `group` FROM `player` WHERE `id` = ?";
try (Connection conn = AlyraManager.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setLong(1, playerId);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            long id = rs.getLong("id");
            int rank = rs.getInt("rank");
            int group = rs.getInt("group");
        }
    }
} catch (Exception e) {
    Logger.logException(PlayerDAO.class, e, "Lỗi truy vấn player ID: " + playerId);
}
```

---

## 4. Số Học: Mẫu Ngăn Chặn Tràn Số `long` (Numeric Overflow Guard)

### 🔴 Triệu chứng lỗi (Vấn đề thật):
Sức mạnh vượt qua $2,147,483,647$ (giới hạn `int`) bị tràn thành số âm âm hàng tỷ, nhân vật bị reset sức mạnh về 0 hoặc lỗi game.

### ❌ Code Lỗi:
```java
public int power; // LỖI TRÀN SỐ!
public void addPower(int amount) {
    this.power += amount; // 2,100,000,000 + 100,000,000 -> -2,094,967,296 !
}
```

### ✅ Code Chuẩn:
```java
public long power; // Bắt buộc dùng long cho mọi chỉ số tích lũy
public static final long MAX_POWER = 200_000_000_000L; // 200 tỷ

public void addPower(long amount) {
    if (amount <= 0) return;
    this.power = Math.min(MAX_POWER, this.power + amount);
}
```

---

## 5. Hiệu Năng Client: Mẫu Zero-GC Trong Vòng Lặp Vẽ (Zero-GC Render Loop)

### 🔴 Triệu chứng lỗi (Vấn đề thật):
Trong hàm `paint()` hoặc `update()` gọi 60 lần/giây, lập trình viên tạo mới `new Vector()`, `new string` làm bộ thu gom rác (Garbage Collector) của Mono/Unity chạy liên tục, gây khựng hình giật lag trên mobile.

### ❌ Code Lỗi:
```csharp
// KHÔNG AN TOÀN: 60 FPS x new = GC spike hàng triệu byte mỗi phút!
public void paint(mGraphics g) {
    string info = "HP: " + player.hp + "/" + player.maxHp; // Cấp phát new string!
    Vector2 pos = new Vector2(x, y); // new đối tượng trong paint!
    g.drawString(info, (int)pos.x, (int)pos.y, 0);
}
```

### ✅ Code Chuẩn:
```csharp
// AN TOÀN: Tái sử dụng bộ đệm (Pre-allocated / Static Buffer)
private static readonly StringBuilder strBuffer = new StringBuilder(64);

public void paint(mGraphics g) {
    strBuffer.Length = 0;
    strBuffer.Append("HP: ").Append(player.hp).Append("/").Append(player.maxHp);
    
    // Tái sử dụng tọa độ primitive, không new đối tượng
    g.drawString(strBuffer.ToString(), x, y, 0);
}
```


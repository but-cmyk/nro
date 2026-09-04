# NRO Binary Packet Protocol Cheatsheet

Tài liệu chuẩn hóa toàn bộ giao thức truyền nhận nhị phân (Binary Protocol) giữa Server Java và Client Unity C# trong Ngọc Rồng Online.

---

## 1. Cơ Bản Về Luồng Nhị Phân (Binary Stream Standard)

Tất cả các gói tin trong NRO đều tuân thủ định dạng Big-Endian (Network Byte Order).

| Kiểu Dữ Liệu | Java (`DataOutputStream` / `DataInputStream`) | C# (`myWriter` / `myReader`) | Kích thước | Ghi Chú |
| :--- | :--- | :--- | :--- | :--- |
| **byte / sbyte** | `writeByte(int v)` / `readByte()` | `writeByte(sbyte v)` / `readByte()` | 1 byte | Giá trị từ -128 đến 127 |
| **unsigned byte** | `readUnsignedByte()` | `readUnsignedByte()` | 1 byte | Giá trị từ 0 đến 255 (Dùng cho Type, Index, Option ID) |
| **boolean** | `writeBoolean(boolean v)` / `readBoolean()` | `writeBoolean(bool v)` / `readBoolean()` | 1 byte | 1 = true, 0 = false |
| **short** | `writeShort(int v)` / `readShort()` | `writeShort(short v)` / `readShort()` | 2 bytes | Tọa độ X, Y, Item Template ID |
| **int** | `writeInt(int v)` / `readInt()` | `writeInt(int v)` / `readInt()` | 4 bytes | Máu HP, Ki MP, Sức đánh, Vàng |
| **long** | `writeLong(long v)` / `readLong()` | `writeLong(long v)` / `readLong()` | 8 bytes | Điểm tiềm năng, sức mạnh, thời gian timestamp |
| **UTF-8 String** | `writeUTF(String str)` / `readUTF()` | `writeUTF(string str)` / `readUTF()` | 2 bytes len + bytes | Chuỗi văn bản tiếng Việt Unicode |

---

## 2. Bảng Tra Cứu Opcode Phổ Biến (Master Opcode Table)

| CMD | Hằng Số Java (`Cmd_message`) | Hằng Số C# (`Cmd`) | Hướng | Mô Tả Chức Năng |
| :--- | :--- | :--- | :--- | :--- |
| `0` | `LOGIN` | `LOGIN` | C $\rightarrow$ S | Đăng nhập tài khoản |
| `-26`| `SUB_COMMAND` | `UPDATE_BODY` | S $\rightarrow$ C | Cập nhật thông tin trang bị/body người chơi |
| `-7` | `ME_MOVE` | `PLAYER_MOVE` | C $\rightarrow$ S / S $\rightarrow$ C | Nhân vật di chuyển (X, Y) |
| `-60`| `PLAYER_ATTACK_PLAYER` | `PLAYER_ATTACK_PLAYER` | C $\rightarrow$ S | Người chơi tấn công đối thủ |
| `54` | `PLAYER_ATTACK_NPC` | `PLAYER_ATTACK_MOB` | C $\rightarrow$ S | Tấn công quái vật |
| `-43`| `USE_ITEM` | `USE_ITEM` | C $\rightarrow$ S | Sử dụng vật phẩm trong túi đồ |
| `-36`| `ITEM_BAG` | `ITEM_BAG` | S $\rightarrow$ C | Đồng bộ toàn bộ danh sách đồ trong hành trang |
| `-37`| `ITEM_BOX` | `ITEM_BOX` | S $\rightarrow$ C | Đồng bộ danh sách đồ trong rương |
| `-32`| `OPEN_MENU` | `OPEN_MENU` | S $\rightarrow$ C | Server gửi menu lựa chọn của NPC cho Client |
| `32` | `CONFIRM_MENU` | `CONFIRM_MENU` | C $\rightarrow$ S | Client gửi lựa chọn menu của NPC lên Server |
| `-44`| `CHAT` | `CHAT` | Bidirectional | Chat kênh bản đồ / Chat mật |
| `92` | `CHAT_GLOBAL` | `CHAT_GLOBAL` | Bidirectional | Chat thế giới (loa phóng thanh) |
| `-86`| `TRADE` | `TRADE` | Bidirectional | Giao dịch người chơi với người chơi |
| `-100`| `CONSIGN_SHOP` | `CONSIGN_SHOP` | Bidirectional | Siêu thị ký gửi (Mua/Bán/Nhận vàng) |
| `6`   | `UPDATE_MAP` | `UPDATE_MAP` | S $\rightarrow$ C | Đồng bộ dữ liệu map mới tải |
| `-69`| `UPDATE_BAG_INDEX` | `UPDATE_BAG_INDEX`| S $\rightarrow$ C | Cập nhật số lượng/vị trí 1 ô đồ trong túi |

---

## 3. Bản Thiết Kế Cấu Trúc Payload (Packet Blueprints)

### A. Gói Tin Di Chuyển (CMD -7: `ME_MOVE`)
**Client gửi lên (`Service.cs`):**
```csharp
Message msg = new Message((sbyte)-7);
msg.writer().writeByte(typeMove); // 0: chạy, 1: bay/nhảy
msg.writer().writeShort(cx);      // Tọa độ X
msg.writer().writeShort(cy);      // Tọa độ Y
session.sendMessage(msg);
```
**Server đọc (`Controller.java`):**
```java
byte typeMove = msg.reader().readByte();
short x = msg.reader().readShort();
short y = msg.reader().readShort();
MapService.gI().playerMove(player, x, y);
```

### B. Gói Tin Mở Menu NPC (CMD -32: `OPEN_MENU`)
**Server gửi về (`Service.java`):**
```java
Message msg = new Message((byte)-32);
msg.writer().writeShort(npcId);         // ID NPC đang tương tác
msg.writer().writeUTF(npcSay);          // Lời thoại NPC
msg.writer().writeByte(menus.length);    // Số lượng lựa chọn
for (String menu : menus) {
    msg.writer().writeUTF(menu);        // Tên từng nút lựa chọn
}
session.sendMessage(msg);
```
**Client đọc (`TaskNpcPacketHandler.cs` hoặc `Controller.cs`):**
```csharp
short npcId = msg.reader().readShort();
string npcSay = msg.reader().readUTF();
sbyte totalMenu = msg.reader().readByte();
string[] menuSelect = new string[totalMenu];
for (int i = 0; i < totalMenu; i++) {
    menuSelect[i] = msg.reader().readUTF();
}
// Hiển thị popup thoại lên GameCanvas
```

### C. Gói Tin Chọn Menu NPC (CMD 32: `CONFIRM_MENU`)
**Client gửi lên:**
```csharp
Message msg = new Message((sbyte)32);
msg.writer().writeShort(npcId);     // ID NPC
msg.writer().writeByte(selectIndex); // Chỉ số nút bấm người chơi chọn (0, 1, 2...)
session.sendMessage(msg);
```
**Server đọc:**
```java
short npcId = msg.reader().readShort();
byte select = msg.reader().readByte();
MenuController.gI().doMenu(player, npcId, select);
```

### D. Gói Tin Ký Gửi (CMD -100: `CONSIGN_SHOP`)
*Lưu ý cành rẽ Version*:
```java
byte action = msg.reader().readByte(); // 0: Ký gửi, 1: Hủy, 2: Nhận tiền...
if (action == 0) {
    short idItem = msg.reader().readShort();
    byte moneyType = msg.reader().readByte();
    int money = msg.reader().readInt();
    int quantity;
    if (player.getSession().version >= 222) {
        quantity = msg.reader().readInt(); // Bản mới đọc 4-byte
    } else {
        quantity = msg.reader().readByte(); // Bản cũ đọc 1-byte
    }
    ConsignShopService.gI().KiGui(player, idItem, money, moneyType, quantity);
}
```

---

## 4. Nguyên Tắc Tránh Lỗi Khi Thêm Packet Mới

1. **Không Chèn Thừa/Thiếu Byte**:
   Nếu phía gửi ghi 3 trường `[short, int, UTF]` mà phía nhận chỉ đọc `[short, int]` rồi kết thúc, chuỗi `UTF` còn tồn lại trong buffer của socket sẽ biến thành rác ở gói tin kế tiếp $\rightarrow$ Crash toàn bộ kết nối của người chơi!
2. **Luôn Bọc Try-Catch Khi Đọc Chuỗi**:
   Hàm `readUTF()` sẽ quăng `UTFDataFormatException` nếu buffer bị xê dịch lệch con trỏ dù chỉ 1 byte.
3. **Thứ Tự Tương Thích Ngược**:
   Nếu muốn truyền thêm thông tin trong gói tin cũ, luôn đưa trường dữ liệu mới xuống cuối cùng của payload để không làm xáo trộn các trường cơ bản ban đầu.

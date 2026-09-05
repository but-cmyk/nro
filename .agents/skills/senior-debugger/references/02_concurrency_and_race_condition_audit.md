# Pháp Y Đa Luồng & Triệt Tiêu Race Condition Trong Game Server

## 1. Bản Chất Đa Luồng Trong Kiến Trúc Máy Chủ Game NRO

Máy chủ NRO hoạt động dựa trên mô hình đa luồng lai (Hybrid Multi-Threading):
1. **Netty I/O Worker Threads (N luồng)**: Chuyên trách giải mã gói tin (Decode) và gọi hàm trong `Controller.onMessage()`. Hàng trăm packet từ các client khác nhau bay vào server trên nhiều thread song song.
2. **Game Loop Tick Thread (1 luồng chính)**: Chạy trong `GameLoopManager`, định kỳ quét toàn bộ `players.update()`, `zone.update()`, `bosses.update()`.
3. **Async Worker Pool (Image Resize, Save DB, Scheduled Cron)**: Chạy ngầm trong background threads.

Khi một trường dữ liệu (ví dụ: `player.inventory.items`, `player.zone`, `player.nPoint.hp`, `zone.items`) được đọc bởi luồng GameLoop nhưng lại bị ghi đồng thời bởi luồng Netty Worker, hiểm họa **RACE CONDITION & DATA CORRUPTION** bùng nổ.

---

## 2. 4 Vector Lỗ Hổng Đa Luồng Phổ Biến Nhất

### Vector 1: Check-Then-Act Race Condition (Thời Gian Giữa Kiểm Tra & Hành Động)
- **Code sai**:
  ```java
  // Luồng A & B cùng chạy cùng 1 miligiây
  if (InventoryService.gI().getCountEmptyBag(player) > 0) {
      // Khoảng trống nguy hiểm: Cả 2 luồng đều thấy còn chỗ!
      InventoryService.gI().addItemBag(player, item); 
  }
  ```
- **Hậu quả**: Cả 2 luồng cùng thêm đồ vào cùng 1 slot hoặc vượt quá 20 ô hành trang, làm đè mất đồ cũ.
- **Khắc phục**: Gom toàn bộ chuỗi Check-Then-Act vào một khối nguyên tử (Atomic/Synchronized) trên monitor của `player`:
  ```java
  synchronized (player) {
      if (InventoryService.gI().getCountEmptyBag(player) > 0) {
          InventoryService.gI().addItemBag(player, item);
      }
  }
  ```

---

### Vector 2: Memory Visibility (Thiếu Rào Cản Bộ Nhớ - Memory Barrier)
- **Hiện tượng**: Một luồng thay đổi giá trị của biến (ví dụ `player.isDie = true`), nhưng luồng khác chạy trên Core CPU khác vẫn thấy `isDie = false` trong CPU L1/L2 Cache, dẫn đến việc người chơi đã chết mà vẫn đánh tiếp hoặc dùng skill được!
- **Khắc phục**:
  - Khai báo `volatile` cho các cờ trạng thái boolean/int đơn giản.
  - Hoặc dùng `AtomicBoolean`, `AtomicInteger`, `AtomicLong`.

---

### Vector 3: Iterator Mutation (`ConcurrentModificationException`)
- **Code sai**:
  ```java
  for (ItemMap it : this.items) { // this.items là ArrayList thông thường
      if (it.isTimeout()) {
          this.items.remove(it); // NÉM ConcurrentModificationException!
      }
  }
  ```
- **Khắc phục**:
  - Dùng vòng lặp ngược `for (int i = list.size() - 1; i >= 0; i--)`.
  - Hoặc dùng `Iterator.remove()`.
  - Hoặc bọc `synchronized (this.items)` và sử dụng `CopyOnWriteArrayList` nếu đọc nhiều ghi ít.

---

### Vector 4: Lock Ordering Deadlock (Khóa Chéo Gây Treo Cứng Server)
- **Kịch bản Deadlock**:
  - Luồng 1 (Giao dịch A $\rightarrow$ B): Khóa A $\rightarrow$ chờ khóa B.
  - Luồng 2 (Giao dịch B $\rightarrow$ A): Khóa B $\rightarrow$ chờ khóa A.
  - Cả 2 luồng đứng đợi nhau mãi mãi $\rightarrow$ Treo luồng Netty, lag đứt kết nối toàn server!
- **Khắc phục chuẩn Senior**: Sắp xếp thứ tự khóa cố định (Lock Ordering) dựa theo ID định danh:
  ```java
  Player lockFirst = player1.id < player2.id ? player1 : player2;
  Player lockSecond = player1.id < player2.id ? player2 : player1;
  synchronized (lockFirst) {
      synchronized (lockSecond) {
          // Giao dịch an toàn tuyệt đối, không bao giờ deadlock!
      }
  }
  ```

# Hướng Dẫn Tối Ưu Hiệu Năng & Triệt Tiêu GC Allocations Client Unity NRO

Tài liệu chuyên sâu về cơ chế bộ nhớ, Garbage Collection (Mono SGen/Boehm GC) trong Unity C# đối với dự án Ngọc Rồng Online 6TabFixed.

---

## 1. Bản Chất Vấn Đề GC Trong NRO Client (Unity C#)

Client Ngọc Rồng Online được ported từ phiên bản J2ME (Java Micro Edition) sang Unity C#. Do đó, mã nguồn chứa rất nhiều tàn tích của Java thế hệ cũ:
1. **Lạm dụng `MyVector` và `MyHashTable`**: Không sử dụng Generic (`List<T>`, `Dictionary<TKey, TValue>`), dẫn đến **Boxing/Unboxing** kiểu dữ liệu nguyên thủy (`int`, `short`, `long`, `float`) mỗi khi thêm/lấy phần tử.
2. **Cấp phát bộ nhớ trong `paint()` và `update()`**: Vòng lặp vẽ chạy 30-60 lần mỗi giây. Bất kỳ đối tượng tạm nào được khởi tạo (`new Point()`, `new Rect()`, `new Vector2()`, chuỗi `string + string`) đều bị đẩy vào Mono Heap.
3. **Mono GC Spike**: Khi Heap đầy, Garbage Collector sẽ kích hoạt cơ chế **Stop-The-World**, làm dừng toàn bộ luồng chính từ 10ms - 50ms, gây hiện tượng **giật khựng (Micro-Stuttering / FPS Drops)** cực kỳ khó chịu, đặc biệt khi mở nhiều tab (6 Tab).

---

## 2. Bảng Nhận Diện Anti-Patterns Gây Tích Tụ Rác (GC Leaks)

### A. Nối Chuỗi (String Concatenation) Trong Vòng Lặp Vẽ
- **Code Xấu**:
  ```csharp
  // Trong GameScr.paint() hoặc Char.paint()
  mFont.tahoma_7b_white.drawString(g, "HP: " + c.hp + "/" + c.hpFull, x, y, 0);
  ```
  *Mỗi frame tạo ra 3-4 object String mới trên Heap. Với 60 FPS và 6 Tab -> Hàng ngàn chuỗi rác mỗi giây!*
- **Chuẩn Senior Refactor**:
  Sử dụng `StringBuilder` tĩnh / pooled hoặc bộ đệm chuỗi có cache:
  ```csharp
  private static readonly StringBuilder strBuilder = new StringBuilder(64);

  public static string FormatHp(long hp, long hpFull) {
      strBuilder.Length = 0;
      strBuilder.Append("HP: ").Append(hp).Append('/').Append(hpFull);
      return strBuilder.ToString();
  }
  ```
  Hoặc chỉ cập nhật text khi chỉ số HP thực sự thay đổi, lưu vào `char.hpStringCached`.

---

### B. Boxing / Unboxing Kiểu Nguyên Thủy
- **Code Xấu (Tàn tích J2ME)**:
  ```csharp
  public MyVector listChar = new MyVector();
  listChar.addElement(100); // int bị box thành object!
  int val = (int)listChar.elementAt(0); // unbox!
  ```
- **Chuẩn Senior Refactor**:
  Chuyển đổi hoàn toàn sang Generic Collections:
  ```csharp
  public List<int> listChar = new List<int>(32);
  ```

---

### C. Khởi Tạo Đối Tượng Tạm Trong `update()` / `paint()`
- **Code Xấu**:
  ```csharp
  void update() {
      Position pos = new Position(cx, cy); // Cấp phát Heap mỗi tick!
      MoveTo(pos);
  }
  ```
- **Chuẩn Senior Refactor**:
  1. Chuyển class nhỏ thành `struct` nếu chỉ mang dữ liệu tọa độ (Value Type, nằm trên Stack, 0 byte rác GC):
     ```csharp
     public struct Vector2Short {
         public short x;
         public short y;
         public Vector2Short(short x, short y) { this.x = x; this.y = y; }
     }
     ```
  2. Sử dụng Object Pooling cho các hiệu ứng, đạn, popup text (`Effect_End`, `MonsterDart`, `ChatPopup`).

---

### D. Sử Dụng `foreach` Trên Collection Không Tối Ưu
- Trên một số phiên bản Mono/Unity cũ, duyệt `foreach` trên `IEnumerable` hoặc Dictionary sinh ra `IEnumerator` object trên Heap mỗi lần gọi.
- **Chuẩn Senior**: Dùng vòng lặp `for (int i = 0; i < count; i++)` truyền thống với index đối với các danh sách truy cập liên tục trong `paint()` và `update()`.

---

## 3. Checklist Kiểm Tra Hiệu Năng Chuẩn Senior (Performance Checklist)

| Mục kiểm tra | Nguy cơ | Giải pháp Senior |
| :--- | :--- | :--- |
| `mGraphics.drawImage()` | Nạp ảnh liên tục từ RMS / disk | Cache `Image` vào HashTable/Dictionary với Key là Id ảnh. |
| `String.Format` / `+` trong Paint | Sinh rác chuỗi mỗi frame | Dùng cache chuỗi hoặc `StringBuilder`. |
| `Camera.update()` | Đa tab chạy camera giật | Tách rời camera logic theo từng `TabInstance`. |
| `Session_ME` thread | Race condition với Unity MainThread | Dùng `ConcurrentQueue<Message>` đẩy về `Update()` xử lý. |
| Array resize `list.Add()` | GC realloc mảng nội bộ | Khởi tạo sẵn kích thước `new List<T>(capacity)`. |
| Linq (`Where`, `Select`) | Cực nhiều delegate/enumerator alloc | Tuyệt đối cấm dùng Linq trong vòng lặp game 30 FPS. |

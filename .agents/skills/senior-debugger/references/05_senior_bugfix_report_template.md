# Senior Bugfix Report Template (Bản Mẫu Báo Cáo Sửa Lỗi Chuẩn Senior)

Bản mẫu này là tiêu chuẩn định dạng bắt buộc cho mọi báo cáo sửa lỗi khi kích hoạt skill `senior-debugger`. Báo cáo phải loại bỏ hoàn toàn các nhận định mơ hồ ("có vẻ như", "thử sửa xem"), tập trung 100% vào dữ liệu kỹ thuật, bằng chứng mã nguồn và phân tích chuỗi nhân quả.

---

```markdown
# [BÁO CÁO VI PHẪU MÃ NGUỒN] VÁ LỖ HỔNG / SỰ CỐ: <TÊN SỰ CỐ / BUG ID>

## 1. TỔNG QUAN SỰ CỐ & PHÂN CẤP NGUY HIỂM (INCIDENT PROFILE)
- **Mã định danh**: [BUG-XXX]
- **Hệ thống liên quan**: [Ví dụ: Hệ Thống Bản Đồ / ChangeMapService]
- **Mức độ nghiêm trọng**: [CRITICAL / HIGH / MEDIUM / LOW]
- **Phân loại kỹ thuật**: [Crash Loop / Protocol Desync / Race Condition / Memory Leak / Logic Flaw]
- **Tác động nghiệp vụ**: [Mô tả ảnh hưởng trực tiếp tới người chơi hoặc máy chủ: sập server, lag, dupe đồ, văng game...]

---

## 2. GIẢI PHẪU TRIỆU CHỨNG & KỊCH BẢN TÁI HIỆN (SYMPTOM & PoC)
- **Điểm bộc phát**: `[Path/File.java:LineXX]`
- **Stack Trace / Dữ liệu bất thường**:
  ```text
  java.lang.IndexOutOfBoundsException: Index 15 out of bounds for length 15
      at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
      at models.map.MapService.getZone(MapService.java:334)
  ```
- **Kịch bản tái hiện lỗi (Proof of Concept - 100% Repro Steps)**:
  1. Bước 1: [Thao tác ban đầu...]
  2. Bước 2: [Thao tác kích hoạt điều kiện biên...]
  3. Kết quả sai lệch: [Quan sát thấy điều gì xảy ra...]

---

## 3. PHÂN TÍCH NGUYÊN NHÂN GỐC RỄ (ROOT-CAUSE FORENSIC)
- **5-Whys Chain**:
  - *Tại sao 1*: ...
  - *Tại sao 2*: ...
  - *Tại sao 3*: ...
  - *Căn nguyên*: ...
- **Điều kiện bất biến bị vi phạm (Broken Invariant)**:
  [Giải thích nguyên lý kiến trúc hoặc hợp đồng dữ liệu bị phá vỡ trong code cũ]

---

## 4. ĐÁNH GIÁ BÁN KÍNH CHẤN ĐỘNG (BLAST RADIUS AUDIT)
- **Các thành phần phụ thuộc (Caller Graph)**:
  - Caller 1: `ClassA.methodX()` $\rightarrow$ An toàn / Không bị ảnh hưởng.
  - Caller 2: `ClassB.methodY()` $\rightarrow$ Đã được cập nhật đồng bộ.
- **Tương thích giao thức (Protocol / Client Sync)**: Khớp 1:1 byte order, không lệch opcode.
- **Tương thích cơ sở dữ liệu (DB Backward Compatibility)**: Không phá vỡ dữ liệu đã lưu.

---

## 5. BẢN VÁ VI PHẪU (SURGICAL CODE DIFF)

```diff
--- a/src/services/map/MapService.java
+++ b/src/services/map/MapService.java
@@ -329,7 +329,12 @@ public class MapService {
-        int z = 0;
-        while (map.zones.get(z).getNumOfPlayers() >= map.zones.get(z).maxPlayer) {
-            z++;
-        }
-        return map.zones.get(z);
+        for (Zone zone : map.zones) {
+            if (zone.getNumOfPlayers() < zone.maxPlayer) {
+                return zone;
+            }
+        }
+        return map.zones.get(0); // Fallback an toàn, triệt tiêu OutOfBounds
```

---

## 6. BẰNG CHỨNG NGHIỆM THU & BẢO VỆ CHỐNG HỒI QUY (VERIFICATION & REGRESSION PROOF)
- **Kiểm tra biên dịch**: `mvn clean compile` $\rightarrow$ SUCCESS (0 errors, 0 warnings).
- **Kiểm tra PoC sau bản vá**: Thực hiện lại 3 bước tại Mục 2 $\rightarrow$ Kết quả: Hoạt động trơn tru, không còn lỗi.
- **Cam kết chất lượng**: Không sử dụng try-catch rỗng; giải quyết tận gốc rễ; bảo đảm tính toàn vẹn đa luồng.
```

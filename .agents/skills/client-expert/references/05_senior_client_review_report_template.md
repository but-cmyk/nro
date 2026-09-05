# Khung Mẫu Báo Cáo Kiểm Định Client Chuẩn Senior Architect / Claude

Mẫu báo cáo đánh giá toàn diện mã nguồn Client Unity C# dành cho dự án Ngọc Rồng Online. Mọi báo cáo review client phải tuân theo cấu trúc nghiêm ngặt này.

---

# [BÁO CÁO KIỂM ĐỊNH CLIENT UNITY] <TÊN TÍNH NĂNG / FILE ĐƯỢC REVIEW>

## I. TỔNG QUAN KIẾN TRÚC & PHẠM VI (ARCHITECTURAL SCOPE)
- **File mục tiêu**: `Path/To/File.cs` (Tổng số dòng: ..., Dung lượng: ...)
- **Chức năng nghiệp vụ**: Mô tả ngắn gọn trách nhiệm của module/file trong game.
- **Mức độ ảnh hưởng (Blast Radius)**: [Cực Cao / Cao / Trung Bình / Thấp].
- **Mức độ rủi ro (Risk Level)**: [Critical / High / Medium / Low].

---

## II. MA TRẬN 7 TRỤ CỘT ĐÁNH GIÁ (THE 7 PILLARS SCORECARD)

| Trụ cột đánh giá | Tiêu chí thẩm định | Điểm số (Thang 10) | Nhận xét nhanh |
| :--- | :--- | :---: | :--- |
| **1. GC Allocations & Frame Drops** | Không cấp phát rác trong `paint`/`update`, không nối chuỗi lặp | .../10 | ... |
| **2. Protocol Binary Sync** | Khớp 1:1 từng byte với Java Server, không tràn số đọc nhầm | .../10 | ... |
| **3. God Class & Modularization** | Trách nhiệm đơn nhất (SRP), không ôm đồm, dễ phân rã | .../10 | ... |
| **4. Multi-Tab & State Isolation** | Không leak static state, độc lập giữa 6 tab | .../10 | ... |
| **5. Memory Leaks & Resource Lifecycle** | Image, Textures, Sound, RMS, Event unsubscribed sạch sẽ | .../10 | ... |
| **6. Modding & God Tools Architecture** | Hooking sạch, không xung đột GameLoop, phím tắt an toàn | .../10 | ... |
| **7. Threading & Network Safety** | Socket thread đẩy về MainThread qua Queue an toàn | .../10 | ... |
| **TỔNG ĐIỂM HỆ THỐNG** | **Điểm trung bình toàn diện** | **.../100** | **[Xuất Sắc / Đạt / Cần Cải Tổ / Nguy Hiểm]** |

---

## III. CHI TIẾT CÁC LỖI & CODE SMELLS THEO THỨ TỰ ƯU TIÊN

### 1. [CRITICAL / HIGH] <Tên Lỗi / Vấn Đề Cốt Lõi>
- **Vị trí**: `File.cs:dòng-đến-dòng`
- **Hiện tượng & Nguy cơ**: Mô tả chính xác điều gì xảy ra ở runtime (Crash game, giật lag tụt FPS, desync gói tin, ghi đè dữ liệu giữa các tab).
- **Phân tích cơ chế gốc rễ**: Giải thích tại sao code lại gây ra lỗi này.
- **Minh họa Code Trước & Sau Sửa (Before / After Refactor)**:
  ```csharp
  // ❌ TRƯỚC: Code cũ gây lỗi / sinh rác GC
  ...
  
  //  SAU: Chuẩn Senior tối ưu hóa triệt để
  ...
  ```

---

## IV. ĐÁNH GIÁ TÁC ĐỘNG TỚI TRẢI NGHIỆM NGƯỜI CHƠI (PLAYER UX & FPS)
- **Tác động FPS**: Đánh giá mức độ tụt khung hình khi giao tranh đông người (Doanh Trại, Đại Hội Võ Thuật, PK Bang).
- **Độ phản hồi (Input Latency)**: Độ trễ khi bấm phím skill, nhặt đồ, mở menu panel.
- **Độ ổn định kết nối**: Khả năng chịu đựng khi mạng lag hoặc ngắt kết nối đột ngột.

---

## V. LỘ TRÌNH TRIỂN KHAI TỐI ƯU (ACTION PLAN)
1. **Bước 1 (Hotfix khẩn cấp)**: Sửa ngay các lỗi Critical (Crash, Desync packet, Leak state đa tab).
2. **Bước 2 (Tối ưu hiệu năng GC)**: Triệt tiêu rác bộ nhớ trong `paint()` và `update()`.
3. **Bước 3 (Tái cấu trúc bền vững)**: Tách module theo Component Pattern và PacketDispatcher.

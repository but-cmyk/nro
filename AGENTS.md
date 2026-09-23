# Hướng Dẫn Vận Hành AI Agent (Loop Engineering Operating Guide) - Project NRO

> **Triết lý cốt lõi**: *"Đừng chỉ ngồi prompt thủ công, mà hãy xây dựng hệ thống có khả năng tự quản lý và tự tối ưu chính nó."*

Tài liệu này là quy chuẩn vận hành bắt buộc cho mọi AI Agent khi thực thi bất kỳ tác vụ nào trong workspace này.

---

## 1. Nguyên Tắc Sống Còn: Chống "Đọc Lại Code Khổng Lồ"

Codebase NRO bao gồm hàng trăm nghìn dòng mã Java và C# Unity. **Tuyệt đối không sử dụng `grep_search` hoặc `view_file` toàn bộ class lớn một cách mù quáng.**

### Quy Trình Điều Hướng 4 Bước (The 4-Step Loop)

```
[User Yêu Cầu]
      ↓
[Bước 1: Fast-Path Routing]
Tra cứu tools/memory/CODE_ROUTING.json hoặc chạy:
python tools/memory/guardrails.py --test-routing "<từ khóa>"
      ↓
[Bước 2: Nạp Bất Biến Module]
Đọc file GEMINI.md tương ứng (src/services/, src/models/player/, Client/,...)
Nắm vững các Bất Biến Bắt Buộc và Landmark Index
      ↓
[Bước 3: Targeted Edit & Verification]
Chỉ mở đúng class và method then chốt (dưới 100 dòng).
Biên dịch & kiểm thử (build.bat / test scripts)
      ↓
[Bước 4: Dreaming & Memory Consolidation]
Đúc kết bài học sau phiên:
python tools/memory/dreaming_engine.py --dry-run
```

---

## 2. Bản Đồ Module Tri Thức (Knowledge Modules)

Trước khi chạm vào bất kỳ file nguồn nào, hãy nạp bất biến từ module tương ứng:

1. **Vật phẩm, Đập đồ Bà Hạt Mít, Dupe đồ**: [src/services/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/services/GEMINI.md)
2. **Người chơi, Chỉ số HP/KI/Dame, Tràn số, Đệ tử**: [src/models/player/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/player/GEMINI.md)
3. **Bản đồ, Quái vật, Zone Realtime Loop**: [src/models/map/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/map/GEMINI.md)
4. **Hệ thống Boss, Trùm Fide/Broly, Sự kiện**: [src/models/boss/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/boss/GEMINI.md)
5. **Nhiệm vụ, Danh hiệu, Tiến trình**: [src/models/task/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/models/task/GEMINI.md)
6. **Mạng Netty, Gói tin nhị phân, Stream**: [src/network/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/network/GEMINI.md)
7. **Database MySQL, HikariCP, DAO**: [src/database/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/src/database/GEMINI.md)
8. **Client Unity C#, Single-Client, Giao diện**: [Client/Client/Assets/Scripts/GEMINI.md](file:///d:/SRC_NRO_219/SRC_NRO_219/SRC_NRO_189/SRC_NRO_OK/SRC_NRO_OK/DEMO_NETBEAN_tsSv2_new/Client/Client/Assets/Scripts/GEMINI.md)

---

## 3. Các Bất Biến Bắt Buộc Chung Toàn Dự Án

- **Ngăn Chặn Tràn Số (Numeric Overflow)**: Bắt buộc dùng `long` cho toàn bộ các biến tích lũy (HP, sức đánh, tiềm năng, exp, tiền tệ).
- **Chống Bug Dupe**: Khóa luồng đồng bộ (`synchronized` hoặc lock item) khi thao tác giao dịch (Trade) hoặc đập đồ (Combine).
- **Đóng Tài Nguyên DB**: 100% truy vấn JDBC dùng cú pháp `try-with-resources`. Bọc dấu backtick `` `name` `` cho tên cột MySQL 8.x.
- **Đồng Bộ Stream 1:1**: Thứ tự write byte/int/UTF ở Server Java phải khớp tuyệt đối từng byte với Controller Client Unity C#.
- **Zero-GC Trong Vòng Lặp Vẽ**: Cấm cấp phát `new` đối tượng trong `paint()` và `update()` của Client Unity.

---

## 4. Vận Hành Dreaming Consolidation (Sau Mỗi Task)

Khi hoàn tất sửa đổi code và kiểm thử thành công:
1. Chạy lệnh:
   ```bash
   python tools/memory/dreaming_engine.py
   ```
2. Nếu phát hiện một kinh nghiệm hoặc quy tắc mới cần lưu lại ngay, dùng cú pháp:
   ```bash
   python tools/memory/dreaming_engine.py --learn "Mô tả quy tắc/bài học mới" --module "src/services/GEMINI.md" --apply
   ```
3. Luôn đảm bảo kiểm tra guardrails trước khi commit:
   ```bash
   python tools/memory/guardrails.py --check-all
   ```

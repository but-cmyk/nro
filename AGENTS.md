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
[Bước 1: Fast-Path Routing & Recall]
1.1 Tra cứu file nguồn: python tools/memory/guardrails.py --test-routing "<từ khóa>"
1.2 Nhớ lại bài học cũ: python tools/memory/memory.py recall "<mô tả task>" --files "<file dự kiến>"
      ↓
[Bước 2: Nạp Bất Biến Module]
Đọc file GEMINI.md tương ứng (src/services/, src/models/player/, src/models/tournament/, Client/,...)
Nắm vững các Bất Biến Bắt Buộc và Landmark Index
      ↓
[Bước 3: Targeted Edit & Verification]
Chỉ mở đúng class và method then chốt (dưới 100 dòng).
Biên dịch & kiểm thử (build.bat / test scripts / rà soát asset đĩa).
Tự vấn Checklist lỗi nghiêm ngặt trước khi báo cáo hoàn thành!
      ↓
[Bước 4: Feedback & Memory Capture]
- Nếu áp dụng thành công bài học cũ: python tools/memory/memory.py feedback <L_ID> +1
- Nếu có bài học/lỗi mới đáng giá: python tools/memory/memory.py capture --files "..." --tags "..." --symptom "..." --cause "..." --rule "..."
- Xem tổng hợp phiên: python tools/memory/memory.py digest
```

---

## 2. Bản Đồ Module Tri Thức (Knowledge Modules)

Trước khi chạm vào bất kỳ file nguồn nào, hãy nạp bất biến từ module tương ứng:

1. **Vật phẩm, Đập đồ Bà Hạt Mít, Dupe đồ, NPC**: [src/services/GEMINI.md](file:///d:/SRC_NRO_219/src/services/GEMINI.md)
2. **Người chơi, Chỉ số HP/KI/Dame, Tràn số, Đệ tử**: [src/models/player/GEMINI.md](file:///d:/SRC_NRO_219/src/models/player/GEMINI.md)
3. **Bản đồ, Quái vật, Zone Realtime Loop**: [src/models/map/GEMINI.md](file:///d:/SRC_NRO_219/src/models/map/GEMINI.md)
4. **Hệ thống Boss, Trùm Fide/Broly, Sự kiện**: [src/models/boss/GEMINI.md](file:///d:/SRC_NRO_219/src/models/boss/GEMINI.md)
5. **Võ Đài Whis, Giải Đấu Liên Vũ Trụ, Sinh Tử, Bot**: [src/models/tournament/GEMINI.md](file:///d:/SRC_NRO_219/src/models/tournament/GEMINI.md)
6. **Nhiệm vụ, Danh hiệu, Tiến trình**: [src/models/task/GEMINI.md](file:///d:/SRC_NRO_219/src/models/task/GEMINI.md)
7. **Mạng Netty, Gói tin nhị phân, Stream**: [src/network/GEMINI.md](file:///d:/SRC_NRO_219/src/network/GEMINI.md)
8. **Database MySQL, HikariCP, DAO**: [src/database/GEMINI.md](file:///d:/SRC_NRO_219/src/database/GEMINI.md)
9. **Client Unity C#, Single-Client, Giao diện**: [Client/Client/Assets/Scripts/GEMINI.md](file:///d:/SRC_NRO_219/Client/Client/Assets/Scripts/GEMINI.md)

*Ghi chú*: Thư mục `src/models/` có file điều hướng con [src/models/GEMINI.md](file:///d:/SRC_NRO_219/src/models/GEMINI.md) tự động chỉ đường tới module đúng khi sửa các class không có module riêng.

---

## 3. Các Bất Biến Bắt Buộc Chung Toàn Dự Án

- **Ngăn Chặn Tràn Số (Numeric Overflow)**: Bắt buộc dùng `long` cho toàn bộ các biến tích lũy (HP, sức đánh, tiềm năng, exp, tiền tệ).
- **Chống Bug Dupe**: Khóa luồng đồng bộ (`synchronized` hoặc lock item) khi thao tác giao dịch (Trade) hoặc đập đồ (Combine).
- **Đóng Tài Nguyên DB**: 100% truy vấn JDBC dùng cú pháp `try-with-resources`. Bọc dấu backtick `` `name` `` cho tên cột MySQL 8.x.
- **Đồng Bộ Stream 1:1**: Thứ tự write byte/int/UTF ở Server Java phải khớp tuyệt đối từng byte với Controller Client Unity C#.
- **Zero-GC Trong Vòng Lặp Vẽ**: Cấm cấp phát `new` đối tượng trong `paint()` và `update()` của Client Unity.
- **Cổng Phê Duyệt Hiệu Năng (Human-in-the-Loop Approval Gate)**: Khi phát hiện bất kỳ chức năng nào bị chậm (qua `PERFORMANCE_ALERTS.json` hoặc log), AI Agent bắt buộc phải: (1) Bóc tách rõ nguyên nhân gốc rễ, (2) Lập kế hoạch fix chi tiết trình User. **Chỉ khi User phê duyệt mới được bắt đầu sửa code.**
- **Cổng Kiểm Thử & Tự Soát Lỗi Nghiêm Ngặt Trước Khi Báo Cáo (Pre-Completion Verification Gate)**:
  - **Tuyệt đối cấm báo cáo hoàn thành khi chưa kiểm tra thực tế**: AI Agent không được tuyên bố "đã hoàn thành" hoặc "đã fix xong" nếu chưa chủ động rà soát toàn diện tác động phụ (side effects), trạng thái runtime và log phát sinh.
  - **Xác minh tài nguyên thực tế trên đĩa (Asset Reality Check)**: Khi can thiệp vào logic nạp tài nguyên (âm thanh, ảnh, animation, template), bắt buộc kiểm tra sự tồn tại thực tế của tệp tin trên ổ cứng (`.wav`, `.png`, `.meta`). Tuyệt đối không giả định hay suy đoán tài nguyên tồn tại.
  - **Ngăn chặn cảnh báo giả làm đỏ Console (No False Error Badges)**: Cân nhắc kỹ lưỡng phân cấp log (`LogWarning` vs `LogError`). Không nâng các cảnh báo tài nguyên tùy chọn (optional assets) thành `LogError`, tránh gây hoang mang hoặc làm vọt icon chấm than đỏ trên Unity Console.
  - **Checklist tự vấn trước khi phản hồi User**:
    1. Code có lỗi biên dịch (compile error) hoặc crash ngầm không?
    2. Tệp tài nguyên liên quan có thực sự tồn tại trên đĩa không?
    3. Thay đổi có làm xuất hiện icon lỗi đỏ trên Console Client không?
    4. Đã chạy guardrails (`python tools/memory/guardrails.py --check-all`) để bảo toàn bất biến chưa?

---

## 4. Quản Trị Trí Nhớ & Học Tăng Cường (Memory System)

Hệ thống trí nhớ được quản lý qua skill `.agents/skills/nro-memory/SKILL.md` và các công cụ:
- **`tools/memory/memory.py`**: Lưu trữ và truy xuất bài học có cấu trúc (`LESSONS.jsonl`), chấm điểm feedback +1/-1, thăng hạng (promote) bài học lên module `GEMINI.md`.
- **`tools/memory/dreaming_engine.py`**: Tổng hợp phiên làm việc (`digest`), bổ sung quy tắc mới trực tiếp (`--learn`).
- **`tools/memory/guardrails.py`**: Kiểm tra toàn vẹn schema, dung lượng token, kiểm tra routing và quét vi phạm code (`--check-all`).


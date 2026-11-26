---
name: nro-compile-check
description: >-
  Kỹ năng bắt buộc kiểm tra lỗi biên dịch (Compile Verification Gate) cho toàn bộ Client Unity C# và Server Java.
  Sử dụng trước khi báo cáo hoàn thành bất kỳ tác vụ nào có chỉnh sửa mã nguồn (.cs hoặc .java).
  Skill này đảm bảo không bao giờ để lọt lỗi biên dịch (CS0103, CS0246, CS1061, javac errors) làm đỏ Console của người dùng.
---

# Kỹ Năng Kiểm Tra Biên Dịch Tự Động NRO (Compile Verification Gate)

> **Bất Biến Bắt Buộc**: *Tuyệt đối cấm báo cáo "đã hoàn thành" hoặc "đã fix xong" nếu chưa chạy `python tools/compile_check.py` và đạt 100% 0 lỗi biên dịch.*

---

## 1. Mục Đích & Vai Trò

Codebase NRO gồm 2 phần độc lập:
1. **Client Unity C#**: Chạy trên Unity 2022.3, ngôn ngữ C# 9.0 (.NET Framework 4.7.1 / .NET Standard 2.0).
2. **Server Java**: Chạy trên Java 21, Netty 4.x, HikariCP, MySQL 8.x.

Khi AI Agent sửa code, nguy cơ thiếu `using UnityEngine;`, sai tên phương thức, lệch tham số hoặc thiếu import rất dễ xảy ra. Skill này cung cấp công cụ tự động hóa việc biên dịch thử toàn bộ tệp nguồn trước khi phản hồi người dùng.

---

## 2. Công Cụ Thực Thi (`tools/compile_check.py`)

Công cụ `tools/compile_check.py` tự động:
- Với Client C#: Quét 247+ tệp `.cs`, nạp 360+ reference assemblies từ Unity 2022.3 Editor và gọi Roslyn C# 9.0 compiler (`csc.dll`).
- Với Server Java: Quét 590+ tệp `.java`, nạp toàn bộ thư viện `lib/*.jar` và gọi `javac` biên dịch thử.

### Các Lệnh Thực Thi Chuẩn:

```bash
# Kiểm tra toàn diện cả Client C# và Server Java (Khuyên dùng):
python tools/compile_check.py

# Chỉ kiểm tra biên dịch Client Unity C# (khi chỉ sửa file trong Client/):
python tools/compile_check.py --client

# Chỉ kiểm tra biên dịch Server Java (khi chỉ sửa file trong src/):
python tools/compile_check.py --server
```

---

## 3. Bảng Tra Cứu Lỗi Biên Dịch Thường Gặp & Cách Khắc Phục

| Mã Lỗi | Triệu chứng / Thông báo | Nguyên nhân gốc rễ | Giải pháp chuẩn |
| :--- | :--- | :--- | :--- |
| **CS0103** | `The name 'X' does not exist in the current context` | Thiếu chỉ thị `using` (ví dụ thiếu `using UnityEngine;` khi gọi `Resources`, `Debug`, `Time`) | Thêm `using UnityEngine;` ở đầu file hoặc dùng namespace đầy đủ `UnityEngine.X` |
| **CS0246** | `The type or namespace name 'X' could not be found` | Chưa khai báo thư viện, sai chính tả tên class hoặc thiếu namespace con | Rà soát tên class, thêm `using Game1...;` |
| **CS1061** | `'X' does not contain a definition for 'Y'` | Gọi nhầm method không tồn tại trên đối tượng, hoặc phương thức là private | Kiểm tra lại định nghĩa class gốc, không suy đoán tên hàm |
| **CS1513/CS1002** | `} expected` / `; expected` | Lỗi cú pháp ngoặc nhọn hoặc thiếu dấu chấm phẩy do edit file bị lệch dòng | Đọc lại phạm vi code xung quanh và sửa đúng cú pháp |
| **javac symbol** | `error: cannot find symbol` | Gọi method/biến không tồn tại ở Server Java hoặc thiếu `import` | Kiểm tra lại class Java tương ứng, import đúng package |
| **javac type** | `error: incompatible types` | Ép kiểu sai hoặc lệch kiểu dữ liệu (vd gán `long` cho `int` không cast) | Cast tường minh hoặc chuyển sang kiểu dữ liệu phù hợp |

---

## 4. Quy Trình 3 Bước Nghiệm Thu Bắt Buộc

Mọi AI Agent khi hoàn tất sửa code PHẢI tuân thủ quy trình:

```
[Hoàn tất Edit Code]
         ↓
[Bước 1: Chạy Compile Check]
python tools/compile_check.py (hoặc --client / --server)
         ↓
         ├─→ [Có lỗi 🔴]: Dừng lại, đọc mã lỗi CS/javac, sửa hết lỗi, chạy lại Bước 1.
         ↓
         └─→ [Đạt chuẩn ✅ 0 Errors]
         ↓
[Bước 2: Chạy Guardrails Toàn Vẹn]
python tools/memory/guardrails.py --check-all
         ↓
[Bước 3: Báo Cáo Hoàn Thành Cho User]
```

# Module GEMINI: Thực Thể Người Chơi, Chỉ Số & Chiến Đấu (Player Engine)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Quản lý toàn bộ vòng đời người chơi trong game: khởi tạo, tải từ database, tính toán chỉ số (HP, KI, Dame, Giáp, Chí mạng), hiệu ứng kỹ năng và đệ tử/boss.
- **Class trọng tâm**:
  - `Player.java`: Thực thể gốc của cả Player, Pet, Boss, Bot.
  - `NPoint.java`: Bộ máy tính toán chỉ số sức mạnh (`calPoint()`, `setHp()`, `setMp()`).
  - `Inventory.java`: Quản lý hành trang, rương đồ, trang bị trên người (`itemsBody`).
  - `PlayerSkill.java` / `Skill.java`: Danh sách kỹ năng và thời gian hồi chiêu.

## 2. Các Bất Biến Bắt Buộc
- **Phân định rõ Type thực thể**:
  - Trong `NPoint.calPoint()`, luôn có cờ kiểm tra: `if (this.player.isPet) { ... } else if (this.player.isBoss) { ... } else { ... }`.
  - Không bao giờ áp dụng logic trang bị cải trang hoặc set kích hoạt của Player cho Boss trừ khi có kịch bản đặc thù.
- **Tránh Tràn Số (Numeric Overflow)**:
  - Chỉ số Tiềm năng / Sức mạnh / Kinh nghiệm / HP tối đa ở các server mở rộng dễ vượt mốc `2^31 - 1`. Luôn ưu tiên dùng `long` cho các trường tích lũy này.
- **Khởi Tạo Trạng Thái Toàn Vẹn**:
  - Khi tạo mới Player hoặc load từ DB, các đối tượng phụ thuộc như `taskMain`, `location`, `nPoint` phải được khởi tạo trước khi gọi logic hồi phục HP/vào map.

## 3. Lỗi Thường Gặp Cần Tránh
- NullPointerException khi truy cập `player.inventory` hoặc `player.playerTask` trên nhân vật mới tạo.
- Tính toán chỉ số cộng dồn không giới hạn dẫn đến lỗi HP âm hoặc bug bất tử.

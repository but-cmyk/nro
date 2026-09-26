# Module GEMINI: Hệ Thống Boss, Trùm & Sự Kiện (Boss & Event Engine)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Khởi tạo và quản lý vòng đời Boss, AI hành vi (tự do tìm người chơi, trả đũa), chiêu thức đặc biệt, cơ chế biến hình qua nhiều phase (Fide, Broly, Cooler) và rơi thưởng vật phẩm.
- **Class trọng tâm**:
  - `Boss.java`: Thực thể Boss gốc kế thừa từ `Player`, triển khai vòng lặp `update()`, trạng thái `REST`, `RESPAWN`, `ACTIVE`, `DIE`.
  - `BossData.java` / `BossesData.java`: Cấu hình chỉ số HP, MP, Dame, Skill, trang phục (Head/Body/Leg), lời thoại và thời gian xuất hiện.
  - `src/models/boss/boss_list/`: Danh mục các Boss cụ thể (Broly, Fide, SuperBroly, Android,...).
  - `src/services/BossManager.java`: Quản lý danh sách Boss toàn server, điều phối spawn vào Zone.

## 2. Landmark Index & Core Methods
- `Boss.update()`: Vòng lặp AI điều khiển tấn công, di chuyển hoặc hồi máu.
- `Boss.afk()`: Trạng thái chờ người chơi bước vào map hoặc hết thời gian nghỉ.
- `Boss.leaveMap()`: Dọn sạch Boss khỏi Zone khi bị tiêu diệt hoặc hết thời gian tồn tại.
- `Boss.reward(Player plKill)`: Tính toán người kết liễu và rơi vật phẩm/đệ tử/sao pha lê.

## 3. Các Bất Biến Bắt Buộc
- **Phân Biệt Type Thực Thể Tuyệt Đối**:
  - `Boss` kế thừa từ `Player` nhưng cờ `this.isBoss = true`. Không bao giờ kích hoạt các logic trang bị cải trang hoặc set kích hoạt của Player thường cho Boss trừ khi có kịch bản đặc thù.
- **An Toàn Khi Rời Map & Hủy Tham Chiếu**:
  - Trước khi gán `this.zone = null`, bắt buộc gọi `this.zone.removePlayer(this)` và gửi packet biến mất tới tất cả người chơi trong khu vực.
- **Phòng Ngừa Bất Tử / Mắc Kẹt Phase**:
  - Khi Boss chuyển trạng thái (phase change) hoặc hồi sinh, chỉ số máu phải được reset an toàn: `this.nPoint.hp = this.nPoint.hpMax`. Tránh bug Boss còn 0 HP nhưng không chết.
- Giam ty le xuat hien sieu quai (lvMob) tu 10% xuong 2% trong Mob.java. Giam so luong boss lang thang trong BossManager: AnTrom tu 2 xuong 1, ThoDaiKa tu 5 xuong 1, ChoRach/Raiti/Xibachao/BaDo tu 21 xuong 3 con moi loai. Tang thoi gian nghi sau khi chet tu 5p len 15p va thoi gian doi map tu 3-5p len 10p. `[2026-09-24]`
- Khóa Đồng Bộ Luồng: Luôn đảm bảo lock trạng thái khi thao tác đa luồng hoặc spam packet để chống bug dupe. `[2026-09-24]`
- Phòng Ngừa Tràn Số: Luôn sử dụng kiểu `long` cho các trường tích lũy (HP, sức mạnh, tiền, kinh nghiệm) để tránh tràn mốc 2 tỷ. `[2026-09-24]`
- Đồng Bộ Gói Tin Nhị Phân 1:1: Mọi thứ tự write/read packet giữa Server Java và Client Unity C# phải hoàn toàn tương ứng. `[2026-09-24]`
- Dùng CopyOnWriteArrayList/ConcurrentHashMap cho collection duyệt trong vòng lặp Zone update. `[2026-09-24]`

## 4. Lỗi Thường Gặp Cần Tránh
- NullPointerException khi Boss tìm mục tiêu tấn công nhưng người chơi vừa out map hoặc đổi khu.
- Boss biến mất đột ngột không gọi `leaveMap()`, khiến packet nhân vật vẫn tồn tại ảo trên Client gây lag màn hình.

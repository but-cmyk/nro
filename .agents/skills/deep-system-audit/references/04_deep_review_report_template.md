# Senior Deep Review Report Template

Bản mẫu này là **tiêu chuẩn bắt buộc** cho mọi báo cáo phân tích, đánh giá hệ thống lớn khi kích hoạt skill `deep-system-audit`. Báo cáo phải loại bỏ hoàn toàn các câu nhận xét chung chung, sáo rỗng; tập trung 100% vào dữ liệu kỹ thuật, luồng người chơi, điểm ma sát vi mô và giải pháp code cụ thể.

---

## Cấu Trúc Khung Mẫu Báo Cáo Chuẩn

```markdown
# [BÁO CÁO KIỂM TOÁN CHUYÊN SÂU] HỆ THỐNG: <TÊN HỆ THỐNG>

## 1. TỔNG QUAN KIẾN TRÚC & ĐIỂM SỨC KHỎE (SYSTEM HEALTH METRICS)
- **Hệ thống mục tiêu**: [Ví dụ: Hệ Thống Nhiệm Vụ Hàng Ngày / Side Task Service]
- **Phạm vi mã nguồn**: Các class, method, table database liên quan.
- **Điểm sức khỏe tổng thể**: [X/100]
  - *Kiến trúc & Đa luồng (Architecture & Concurrency)*: [X/20]
  - *Hành trình & Trải nghiệm người chơi (Player Journey & Flow)*: [X/20]
  - *Phòng thủ gian lận & Lách luật (Anti-Abuse & Exploit Defense)*: [X/20]
  - *Cân bằng kinh tế & Phần thưởng (Economy & Faucet-Sink)*: [X/20]
  - *Tính toàn vẹn dữ liệu & Khôi phục (Persistence & Resilience)*: [X/20]

---

## 2. BẢN ĐỒ PHÂN RÃ HỆ THỐNG 3 TẦNG (RECURSIVE DECOMPOSITION MAP)

### Tầng 1: Khối Đại Thể (Macro Module)
[Mô tả mục đích kinh doanh/gameplay cốt lõi của toàn hệ thống]

### Tầng 2: Danh Sách Phân Hệ Vi Mạch (Subsystems)
1. **Phân hệ 1**: [Tên phân hệ - VD: Quản lý gán và lọc nhiệm vụ (Assignment Engine)]
2. **Phân hệ 2**: [Tên phân hệ - VD: Quản lý tiến trình & đếm sự kiện (Progress Tracking Engine)]
3. **Phân hệ 3**: [Tên phân hệ - VD: Quản lý reroll, hủy & quota ngày (Lifecycle & Quota Engine)]
4. **Phân hệ 4**: [Tên phân hệ - VD: Quản lý trả thưởng & kinh tế (Reward & Faucet Engine)]

### Tầng 3: Điểm Neo Mã Nguồn (Code Endpoints & Data Structures)
| Phân Hệ | File / Class | Method / Function | Cấu Trúc Dữ Liệu |
|---|---|---|---|
| Assignment | `TaskService.java` | `changeSideTask(Player, byte)` | `TaskOrder.java`, `mob.id` |
| Progress | `TaskService.java` | `checkDoneSideTask(Player, Mob)` | `SideTask.count`, `maxCount` |
| Quota/Reroll | `TaskService.java` | `paySideTask(...)`, `cancelSideTask(...)` | `player.dailyTaskCount`, `cancelCount` |
| Reward | `TaskService.java` | `rewardSideTask(...)` | `player.inventory.gold`, `InventoryService` |

---

## 3. MA TRẬN ĐÁNH GIÁ ĐA CHIỀU 5 CHIỀU (THE 5-DIMENSION MATRIX)

### Chiều 1: Kiến Trúc Kỹ Thuật (Technical Integrity)
- **Điểm nghẽn đa luồng (Concurrency)**: Có `synchronized` hoặc Atomic không? Có nguy cơ deadlock khi 2 player tương tác chéo không?
- **Quản lý bộ nhớ (Memory/GC)**: Có rò rỉ session, listener hoặc new object rác liên tục trong tick loop không?
- **Khớp giao thức (Packet Protocol)**: Client và Server có đồng bộ 1:1 opcode, kiểu dữ liệu Big-endian không?

### Chiều 2: Hành Trình Người Chơi (Player Journey & UX)
- **Độ dốc sức mạnh (Power Gating)**: Nhiệm vụ có phù hợp với chỉ số HP/Sát thương/Giáp hiện tại không?
- **Ngõ cụt di chuyển (Map Barrier)**: Quái yêu cầu có bị chặn sau Barrier cốt truyện (Tàu vũ trụ, Fide, Xên, Cold) mà player chưa mở không?
- **Trải nghiệm thao tác (Action Feedback)**: Khi thất bại hoặc bị chặn, hệ thống có giải thích rõ nguyên nhân bằng tiếng Việt có dấu và hướng dẫn bước tiếp theo không?

### Chiều 3: Phòng Chống Gian Lận (Exploit & Abuse Defense)
- **Vector Reroll Spam**: Người chơi có thể spam hủy nhận liên tục để lấy nhiệm vụ dễ/ngon không?
- **Vector Double Claim**: Có chặn được việc click chuột siêu tốc 10 lần/giây hoặc lag mạng nhận quà nhân đôi không?
- **Vector Clone Dồn Đồ**: Quà có bị bot clone cấp 1 cày thuê rồi tuồn về acc chính phá hoại kinh tế không?

### Chiều 4: Cân Bằng Kinh Tế (Economy Balance)
- **Tỷ lệ Faucet vs Sink**: Lượng vàng/ngọc/đồ bơm ra có lớn hơn lượng tiêu hao không?
- **Độ hấp dẫn theo cấp bậc (Tiered Incentive)**: Người chơi VIP/Endgame có nhận quà xứng tầm thời gian bỏ ra không? Hay quà cùi khiến người chơi bỏ qua tính năng?

### Chiều 5: Dữ Liệu & Bền Vững (Persistence & Recovery)
- **Xử lý chuyển ngày (Midnight Transition)**: Khi đồng hồ điểm 00:00:00, cơ chế reset có an toàn không? Có bị mất lượt hoặc dupe lượt không?
- **Lưu trữ database**: Dữ liệu có lưu vào MySQL đúng lúc không? Nếu server crash đột ngột thì player mất bao nhiêu tiến trình?

---

## 4. DANH SÁCH ĐIỂM MA SÁT VI MÔ (MICRO FRICTION AUDIT LOG)

Mỗi lỗi hoặc điểm yếu phải được mổ xẻ theo format vi mô:

#### [VẤN ĐỀ #1]: [Tiêu đề ngắn gọn, chuẩn xác]
- **Vị trí code**: `[Path/File.java:LineXX-LineYY]`
- **Phân loại**: `[Exploit / UX Dead-End / Economy Leak / Concurrency]`
- **Mức độ nghiêm trọng**: `[CRITICAL / HIGH / MEDIUM / LOW]`
- **Hiện tượng thực tế**: [Mô tả chi tiết điều gì xảy ra với người chơi hoặc server]
- **Nguyên nhân gốc rễ**: [Giải thích logic code / thiếu sót thuật toán]
- **Kịch bản khai thác (PoC - Proof of Concept)**:
  1. Bước 1: Người chơi làm X...
  2. Bước 2: Dùng tool gửi packet Y...
  3. Hậu quả: Nhận Z bất hợp pháp.

---

## 5. BẢN VẼ TÁI CẤU TRÚC & MÃ NGUỒN ĐỀ XUẤT (ACTIONABLE REFACTORING BLUEPRINT)

Cung cấp code diff hoặc giải pháp code hoàn chỉnh chuẩn Senior Clean Code, xử lý triệt để các vấn đề nêu tại Mục 4.

```diff
- // Code cũ có lỗ hổng
+ // Code mới an toàn, chuẩn xác
```

---

## 6. KẾ HOẠCH KIỂM THỬ ĐA KỊCH BẢN (VERIFICATION PROTOCOL)

1. **Kiểm thử biên (Boundary Testing)**:
   - Thử nghiệm với người chơi mới tạo (0 sức mạnh, Map 0).
   - Thử nghiệm với người chơi Endgame (Hàng chục tỷ sức mạnh, full đồ).
2. **Kiểm thử áp lực & gian lận (Stress & Exploit Testing)**:
   - Bấm nút hủy 100 lần liên tiếp.
   - Thử nhận quà khi hành trang đầy 0 ô trống.
   - Đổi giờ hệ thống qua 00:01:00 để kiểm tra chuyển ngày.
3. **Biện pháp khôi phục (Rollback & Safe-guard)**:
   - Kế hoạch cứu hộ nếu dữ liệu phát sinh lỗi trên môi trường production.
```

# Playbook Phân Rã Đệ Quy Hệ Thống Lớn (Recursive Decomposition)

## 1. Nguyên Tắc Cốt Lõi: Độ Sâu Tối Thiểu 3 Tầng (The 3-Tier Depth Rule)

Khi người dùng yêu cầu: *"Review hệ thống X"*, Agent **KHÔNG ĐƯỢC** đọc lướt qua các class rồi viết kết luận. Thay vào đó, Agent phải lập tức vẽ ra cây phân rã 3 tầng:

$$\text{Tầng 1: Đại Thể (Macro System)} \longrightarrow \text{Tầng 2: Hệ Thống Con (Subsystems)} \longrightarrow \text{Tầng 3: Luồng Vi Mạch (Action Flows & Code Endpoints)}$$

---

## 2. Bản Mẫu Phân Rã Cho 6 Hệ Thống Lớn Trong NRO

### 🎯 Hệ Thống 1: Nhiệm Vụ (Task System)
- **Subsystem 1: Nhiệm vụ chính (Main Task)**
  - *Luồng 1*: Điều kiện kích hoạt & Map Gating (`checkDoneTaskGoToMap`).
  - *Luồng 2*: Tiến trình giết quái / nhặt đồ / nói chuyện NPC (`addDoneSubTask`).
  - *Luồng 3*: Giới hạn cực đại (Max task) & Chống crash loop.
  - *Luồng 4*: Công thức tiềm năng thưởng & Chống lạm phát.
  - *Luồng 5*: Parse dữ liệu đăng nhập (`NDVSqlFetcher`) chống văng khi Task ID > 127.
- **Subsystem 2: Nhiệm vụ hàng ngày (Side Task - Bò Mộng)**
  - *Luồng 1*: Điều kiện nhận theo Sức mạnh (Power Gate) & Map đã mở.
  - *Luồng 2*: Lọc quái theo hành tinh quê hương (Tân thủ không kẹt hành tinh).
  - *Luồng 3*: Cơ chế Reroll: Cooldown sau khi hủy & Phí đổi bằng Ngọc xanh.
  - *Luồng 4*: Giới hạn ngày: Tối đa 20 lượt hoàn thành, không trừ lượt khi hủy.
  - *Luồng 5*: Cơ cấu thưởng đa tầng: TNSM + Vàng + Thỏi vàng + Ngọc + Đá quý.
- **Subsystem 3: Nhiệm vụ Bang Hội (Clan Task)**
  - *Luồng 1*: Quyền hạn nhận & Đồng bộ trừ lượt khi nhận.
  - *Luồng 2*: Đếm quái & Trao Capsule Bang.
  - *Luồng 3*: Chống spam popup 100%.
- **Subsystem 4: Nhiệm vụ Danh Hiệu (TaskDanhHieu)**
  - *Luồng 1*: Phân tách vòng đời: Tích lũy trọn đời vs Reset trong ngày.
  - *Luồng 2*: Đồng bộ chỉ tiêu `required` khớp 1:1 với giới hạn logic code.
  - *Luồng 3*: Móc nối sự kiện tăng tiến độ (Hạ Boss, Đập đồ, Gọi rồng, Ăn trộm).
- **Subsystem 5: Hệ thống Thành Tích (Achievement System)**
  - *Luồng 1*: Bounds checking index `select`.
  - *Luồng 2*: Nhận tiền tệ ngọc xanh trực tiếp, không chặn ô trống hành trang vô lý.

---

### 👑 Hệ Thống 2: Boss AI & Vòng Đời Boss (Boss System)
- **Subsystem 1: Máy trạng thái FSM (State Machine)**
  - `REST` $\rightarrow$ `RESPAWN` $\rightarrow$ `JOIN_MAP` $\rightarrow$ `CHAT_S` $\rightarrow$ `ACTIVE` $\rightarrow$ `CHAT_E` $\rightarrow$ `LEAVE_MAP` $\rightarrow$ `DIE`.
  - Kiểm tra bẫy kẹt thoại vô tận (Chat Deadlock).
- **Subsystem 2: Cơ chế Tấn công & Khóa Mục Tiêu (Aggro / Threat Target)**
  - Mục tiêu ngẫu nhiên vs Phản ứng khi bị đánh hội đồng cự ly gần.
- **Subsystem 3: Đổi Dạng / Biến Hình (Multi-Form Boss: Fide, Xên, Broly)**
  - Làm sạch thực thể cũ khỏi map (`ChangeMapService.exitMap`) tránh ghost entity.
- **Subsystem 4: Chiêu thức Đặc Biệt (Bom Tự Sát, Miễn Nhiễm, Hồi Máu)**
  - Chống chết 2 lần / dupe quà khi nổ bom.
- **Subsystem 5: Cơ chế Rơi Đồ & Trao Thưởng (Loot Drop & Anti-Dupe)**
  - Quyền nhặt của người kết liễu vs người gây nhiều sát thương nhất.

---

### 👶 Hệ Thống 3: Đệ Tử & Hợp Thể (Pet & Fusion System)
- **Subsystem 1: Vòng đời & AI Đệ Tử**
  - Trạng thái: Đi theo, Bảo vệ, Tấn công, Về nhà.
  - Cơ chế nhận TNSM của Đệ và chia sẻ về Sư phụ (`calSubTNSM`).
- **Subsystem 2: Thăng Cấp Chiêu Thức Đệ Tử (Skill 1..4)**
  - Mở skill theo mốc sức mạnh (1.5M, 15M, 150M).
  - Tỉ lệ mở skill (Kamejoko, Antomic, Masenko, Thái Dương Hạ San...).
- **Subsystem 3: Trang Bị Cho Đệ Tử**
  - Đeo/tháo đồ pet, chỉ số cộng thêm cho đệ tử.
- **Subsystem 4: Hợp Thể (Fusion Dance & Porata)**
  - Công thức cộng dồn chỉ số (HP, KI, Dame, Giáp, Chí mạng).
  - Đếm ngược thời gian hợp thể thường (10 phút) & Bông tai Porata cấp 1, cấp 2.
  - Đồng bộ hiển thị Head/Body/Leg khi hợp thể.

---

### 🏰 Hệ Thống 4: Bang Hội & Phó Bản (Clan & Dungeon System)
- **Subsystem 1: Quản lý Thành Viên & Cấp Bậc**
  - Bang chủ, Phó bang, Thành viên, Xin vào, Rời bang.
- **Subsystem 2: Nâng Cấp Bang & Capsule Bang**
  - Tích lũy điểm bang, nâng cấp level bang, kho bang.
- **Subsystem 3: Phó Bản Clan (Bản Đồ Kho Báu, Doanh Trại, Khí Gas, Rắn Độc)**
  - Static Zone Pool: Hỗ trợ đa clan song song (AVAILABLE $\ge 5$).
  - Vòng đời phó bản: Mở cửa $\rightarrow$ Vượt ải $\rightarrow$ Hạ Boss $\rightarrow$ Đóng cửa & Giải phóng tài nguyên.

---

### ⚔️ Hệ Thống 5: PK & Giải Đấu (PVP & Tournament System)
- **Subsystem 1: Thách Đấu & Trả Thù (PVP 1v1)**
  - Đặt cược vàng/ngọc, đếm ngược, phân định thắng thua, hoàn tiền khi hòa/lỗi mạng.
- **Subsystem 2: Đại Hội Võ Thuật (23rd Martial Arts Congress)**
  - Chia bảng theo cấp độ (Nhi đồng, Siêu cấp, Ngoại hạng).
  - Ghép cặp tự động, sàn đấu, khán đài.
- **Subsystem 3: Giải Đấu Siêu Hạng (Super Rank)**
  - Boss clone độc lập (Triệt tiêu shallow copy mutation `nPoint`).
  - Lịch sử đấu JSON, hoán đổi thứ hạng, nhận thưởng hàng ngày.
- **Subsystem 4: Ngọc Rồng Sao Đen (Black Ball War)**
  - Tranh đoạt 7 viên ngọc sao đen (20h00 - 21h00 mỗi ngày).
  - Cờ bang chung, cơ chế buff sao đen, rơi ngọc khi chết.

---

### 💎 Hệ Thống 6: Giao Dịch & Kinh Tế (Trade, Consign & Economy)
- **Subsystem 1: Giao dịch Trực tiếp (Trade 1v1)**
  - 2-Phase Commit: Lock 2 bên $\rightarrow$ Xác nhận $\rightarrow$ Hoán đổi $\rightarrow$ Transaction Log.
  - Chống race condition hủy/nhận đồng thời.
- **Subsystem 2: Siêu Thị Ký Gửi (Consign Shop)**
  - Ký gửi vàng/ngọc, mua đồ, nhận tiền, hủy bán, up top danh sách.
  - Tối ưu tra cứu RAM, loại bỏ clone snapshot rác bộ nhớ GC.
- **Subsystem 3: Rương Đồ & Hành Trang (Inventory & Box)**
  - Thuật toán sắp xếp đồ Two-pointer $O(N)$ (Khử triệt để đệ quy StackOverflow).
  - Bounds checking toàn diện, clamp tràn số nguyên 2 tỷ.

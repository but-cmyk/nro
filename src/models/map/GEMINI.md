# Module GEMINI: Bản Đồ, Khu Vực & Quái Vật (World Realtime Engine)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Quản lý không gian bản đồ, chia khu vực (Zone), định tuyến tọa độ, spawn quái vật và các waypoint chuyển map.
- **Class trọng tâm**:
  - `Map.java`: Cấu trúc bản đồ chung, chứa danh sách các `Zone`.
  - `Zone.java`: Không gian thực thi thời gian thực, chứa `players`, `mobs`, `items`. Vòng lặp `update()` của Zone chạy định kỳ để điều khiển quái đánh, hồi sinh quái, biến mất item rớt sàn.
  - `Mob.java`: AI quái vật, máu quái, cơ chế tấn công trả đũa.
  - `TileMap.java`: Dữ liệu vật lý địa hình (block va chạm, nhảy, bơi, rơi tự do).

## 2. Landmark Index & Core Methods
- `Zone.update()`: Vòng lặp realtime của khu vực, duyệt người chơi và quái vật.
- `Zone.load_players()` / `Zone.load_mobs()`: Nạp đối tượng khi tham gia khu vực.
- `Mob.injured(Player plAtt, long dame, boolean die)`: Xử lý quái nhận sát thương.
- `src/services/MapService.changeMap()`: Điều hướng nhân vật sang tọa độ và khu vực mới.

## 3. Các Bất Biến Bắt Buộc
- **Thread-Safety Trong Zone Loop**:
  - Danh sách người chơi và quái vật trong Zone được truy cập liên tục bởi cả luồng mạng (Netty Worker) và luồng update nội tại. Bắt buộc dùng `ConcurrentHashMap`, `CopyOnWriteArrayList` hoặc synchronize danh sách khi duyệt qua để tránh `ConcurrentModificationException`.
- **Giới Hạn Tọa Độ & Chuyển Zone**:
  - Khi player di chuyển hoặc dịch chuyển, tọa độ X, Y phải được kiểm tra trong biên của `map.mapWidth` và `map.mapHeight`.
- Khóa Đồng Bộ Luồng: Luôn đảm bảo lock trạng thái khi thao tác đa luồng hoặc spam packet để chống bug dupe. `[2026-09-24]`
- Phòng Ngừa Tràn Số: Luôn sử dụng kiểu `long` cho các trường tích lũy (HP, sức mạnh, tiền, kinh nghiệm) để tránh tràn mốc 2 tỷ. `[2026-09-24]`
- Đồng Bộ Gói Tin Nhị Phân 1:1: Mọi thứ tự write/read packet giữa Server Java và Client Unity C# phải hoàn toàn tương ứng. `[2026-09-24]`
- Dùng CopyOnWriteArrayList/ConcurrentHashMap cho collection duyệt trong vòng lặp Zone update. `[2026-09-24]`

## 4. Lỗi Thường Gặp Cần Tránh
- Crash cả Zone khi 1 Player out map đột ngột trong lúc Server đang duyệt danh sách gửi packet khu vực.
- Quái chết nhưng không kích hoạt timer hồi sinh hoặc rơi vật phẩm sai tỷ lệ.

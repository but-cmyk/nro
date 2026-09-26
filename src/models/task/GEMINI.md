# Module GEMINI: Hệ Thống Nhiệm Vụ & Danh Hiệu (Task & Achievement Engine)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Điều khiển tiến trình nhiệm vụ chính tuyến (TaskMain), nhiệm vụ phụ (SideTask), nhiệm vụ bang hội (ClanTask) và chuỗi nhiệm vụ danh hiệu đặc biệt (TaskDanhHieu).
- **Class trọng tâm**:
  - `TaskDanhHieu.java`: Logic theo dõi tiến độ nhiệm vụ danh hiệu, cộng điểm tích lũy và mở khóa danh hiệu hiển thị trên đầu nhân vật.
  - `TaskPlayer.java` / `TaskMain.java`: Trạng thái nhiệm vụ cốt truyện của người chơi (`id`, `index`, `count`).
  - `src/services/TaskService.java`: Bộ điều phối kiểm tra hoàn thành nhiệm vụ, phát thưởng và gửi packet cập nhật.

## 2. Landmark Index & Core Methods
- `TaskService.checkDoneTask(Player player, int idTask)`: Kiểm tra điều kiện hoàn thành nhiệm vụ chính.
- `TaskService.rewardTask(Player player)`: Trao thưởng kinh nghiệm, ngọc hoặc vật phẩm nhiệm vụ.
- `TaskDanhHieu.addCount(Player pl, int type, int amount)`: Tích lũy tiến độ danh hiệu (ví dụ số lần diệt quái, đập đồ, nạp thẻ).
- `TaskDanhHieu.checkDone(Player pl)`: Xác nhận đạt mốc danh hiệu và lưu cờ vào DB.

## 3. Các Bất Biến Bắt Buộc
- **Đồng Bộ Dữ Liệu Với Client**:
  - `id` và `index` nhiệm vụ Server bắt buộc phải khớp hoàn toàn với cấu trúc template nhiệm vụ khai báo phía Client (bản mô tả XML/Assets).
- **Cập Nhật Độc Lập & Bền Vững (Persistence)**:
  - Khi người chơi hoàn thành một mốc nhiệm vụ hoặc danh hiệu, phải đồng thời ghi nhận vào memory người chơi và gọi `PlayerDAO.updatePlayer(player)` để tránh mất tiến trình khi server crash hoặc ngắt kết nối.
- **Tránh Spam Packet Task**:
  - Chỉ gửi packet `Service.gI().sendTask(player)` khi chỉ số `count` thay đổi thực sự hoặc chuyển mốc sub-task mới.
- Loc nhiem vu hang ngay (Side Task / Clan Task): Bat buoc kiem tra quyen tiep can map cua nguoi choi dua tren tien trinh nhiem vu chinh (taskMain.id) va quyen di lien hanh tinh (taskMain.id >= 8) truoc khi giao nhiem vu quai. `[2026-09-24]`

## 4. Lỗi Thường Gặp Cần Tránh
- Không kiểm tra index mảng nhiệm vụ dẫn tới `ArrayIndexOutOfBoundsException` khi người chơi đạt nhiệm vụ cuối cùng.
- Nhận thưởng nhiệm vụ 2 lần do người chơi spam packet báo cáo NPC tại thời điểm mạng giật (cần cờ lock trạng thái nhận thưởng).

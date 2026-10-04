# Module GEMINI: Võ Đài, Giải Đấu & Bot Thi Đấu (Tournament Engine)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Võ Đài Whis 1vs1 / Liên Server (Map 186), Giải Đấu Liên Vũ Trụ 6-7 (Map 187), BXH tuần, Bot/Clone AI thi đấu, các trận đấu trong `src/models/matches/`.
- **Class trọng tâm** (`src/models/tournament/whis_arena/`):
  - `WhisArenaService.java`: Menu NPC (Kaio/Arena), ghép trận, đổi Capsule Vàng.
  - `WhisArenaMatch.java` / `WhisArenaBossMatch.java`: Vòng đời trận PvP / trận Boss U6.
  - `WhisArenaBotManager.java` / `WhisArenaBot.java` / `WhisArenaRival.java`: 100 Bot leo rank + Clone AI.

## 2. Landmark Index & Core Methods
- `WhisArenaService.openMenuAtKaio()` / `openMenuAtArena()`: Điểm vào menu.
- `WhisArenaService.findMatch()`: Random solo với toàn BXH (Top 1-100).
- `WhisArenaService.exchangeCapsuleVang()`: Đổi Capsule Vàng Kì Bí (ID 574).
- `WhisArenaBotManager.getPlayerRank()`: Tra hạng BXH.
- Bảng xếp hạng: Opcode `-96`, `typeTop = 0` → Panel vTop (head, headICON, body, leg, name, info1, info2).

## 3. Các Bất Biến Bắt Buộc
- Tọa độ Map 186/187: Sảnh chờ Y=360, Sàn đấu Y=264. Hạ boss xong đưa player xuống đất (255, 360).
- Lịch: 21h-22h hằng ngày (`TEST_MODE_24_7` mở 24/24 để test). Chốt tuần 22h CN, reset 23h59 CN. Chỉ Top 1-5 BXH tuần được đổi Capsule Vàng (ID 574).
- Lên/xuống sàn: **cấm `changeMap`** sang zone khác hoặc về zone 0 (gây giật, đen màn hình, reload map). Giữ `player.zone`, chỉ dùng `playerMove` + `setPos0`; chờ 3 giây ở sảnh (Y=360) rồi lên sàn (Y=264).
- Ghép trận: đối thủ bận/offline → tự động tạo Clone AI đủ ngoại hình, HP/Dame, Rank; không giới hạn Top 5. Trước trận hồi 100% HP/KI + xóa cooldown skill. Thua = 0 điểm an ủi.
- Giải Liên Vũ Trụ 6-7: vào tab tại Map 186/Kaio → sang Map 187 (tile 23, clone ĐHVT 23). Bill mở menu 3 nút [Quay về Giải đấu Liên Vũ Trụ] / [Sẵn sàng] / [Từ chối]; chỉ "Sẵn sàng" mới lên đài đấu boss round hiện tại.
- Tiến trình round lưu ở `player.whisArenaU6Round` (Frost → Kyabe → Autamegetta → Hit). Hạ 1 boss → về sảnh, hồi 100% HP/MP, gỡ PK.
- **Không tự mở menu qua timeout**; player phải chủ động click NPC.
- Ngoại trang Bot: head = Part type 0, body = type 1, leg = type 2. Cấm dùng part type 0 cho body/leg (lỗi tách rời đầu-thân-chân).
- Text thông báo/chat/BXH: không dùng mã màu `|0|`, `|1|`, `|2|`, `|7|` (ép font tối/lỗi render). Dùng tiếng Việt thường để client hiển thị font mặc định `tahoma_7b_white`.

## 4. Lỗi Thường Gặp Cần Tránh
- NPE trong `openZoneUI` khi zone đang `isCompeting` — phải kiểm tra trước khi mở menu Đổi khu vực.
- Đưa player về zone 0 bằng `changeMap` sau trận → đen màn hình, reload map.
- Bot ghép part sai type → nhân vật bị tách rời.

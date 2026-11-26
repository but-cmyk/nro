# Báo Cáo Kiểm Toán Bảo Mật, Chống Dupe & Đa Luồng (NRO)

Báo cáo này tổng hợp kết quả kiểm toán dựa trên `digest_security_concurrency.json` và **đã xác minh trực tiếp trên mã nguồn Java/C#**.

---

## 1. Các Lỗ Hổng Bảo Mật, Dupe Kinh Tế & Anti-Cheat (Đã Xác Minh Trực Tiếp)

### 1.1. Self-Trade & Snapshot-Overwrite Dupe Đồ
- **Vị trí**: `src/services/func/TransactionService.java:63-107`, `src/services/func/Trade.java:54-57, 451-452`
- **Trạng thái**: **CONFIRMED (P0)**
- **Bằng chứng & Kịch bản khai thác**:
  1. **Self-Trade**: `TransactionService` cho phép gửi lời mời (`SEND_INVITE_TRADE`) và chấp nhận (`ACCEPT_TRADE`) với chính `pl.id` của bản thân (`plMap = pl.zone.getPlayerInMap(playerId)` không kiểm tra `playerId != pl.id` hay `plMap != pl`). Khi gọi `new Trade(pl, pl)`, `itemsBag1` và `itemsBag2` là 2 bản sao tách biệt của cùng một túi đồ; khi commit, `itemsTrade1` được cộng vào `itemsBag2` và `player2.inventory.itemsBag = itemsBag2` ghi đè túi đồ, nhân đôi vật phẩm/vàng.
  2. **Snapshot-Overwrite**: `Trade` sao chép `itemsBag1`/`itemsBag2` lúc khởi tạo (`Trade.java:56-57`) và ghi đè trực tiếp `player1.inventory.itemsBag = itemsBag1` lúc commit (`Trade.java:451-452`). Trong khi giao dịch đang mở, các luồng khác (như `Input.BANSLL`, `Input.USE_CARD_QUANTITY`, `Input.TANG_NGOC_HONG`) vẫn có thể thay đổi túi đồ thật do thiếu kiểm tra `player.isTrade` hoặc lock đồng bộ, dẫn đến mất hoặc nhân đôi vật phẩm khi `Trade` commit đè lên.
- **Cách vá**:
  - Chặn ngay `if (playerId == pl.id || plMap == pl)` trong `TransactionService` (`SEND_INVITE_TRADE` và `ACCEPT_TRADE`).
  - Khóa đồng thời theo thứ tự ID (`synchronized` trên `pl1`/`pl2` theo thứ tự `id` tăng dần để tránh deadlock) trong suốt quá trình commit `Trade`, đồng thời kiểm tra `isTrade`/`TransactionService.gI().check(player)` ở mọi handler chỉnh sửa `inventory` (`Input`, `UseItem`, `ShopService`, `InventoryService`).

### 1.2. Thiếu Kiểm Tra Quyền Admin & Không Reset `typeInput` Trong `Input.java`
- **Vị trí**: `src/services/func/Input.java:96-155, 404-567, 662-699`
- **Trạng thái**: **CONFIRMED (P0)**
- **Bằng chứng & Kịch bản khai thác**:
  - Các nhánh `DUNGLOA` (404), `BUFFVND` (415), `BOTQUAI_NAPPA` (448), `BOTQUAI_TUONGLAI` (455), `BOTQUAI_COLD` (462), `BOTBOSS` (469), `BOTQUAI` (476), `GIVE_IT` (484), `CHANGE_NAME` (546) **hoàn toàn thiếu** kiểm tra `if (!player.isAdmin())` (trong khi `GET_IT` ở dòng 514 có kiểm tra). Bất kỳ client nào có `typeInput` phù hợp hoặc gửi packet giả mạo đều có thể tự buff VND, spawn item tùy ý, đổi tên người chơi khác hoặc spam thông báo toàn server.
  - `doInput()` không reset `player.idMark.setTypeInput(-1)` sau khi xử lý và không `synchronized (player)`, cho phép gửi lặp lại (replay) gói tin để race/double-spend trong `TANG_NGOC_HONG` (116-155) và `BANSLL` (662-699).
- **Cách vá**:
  - Thêm kiểm tra `if (!player.isAdmin()) { return; }` cho toàn bộ các case admin (`BUFFVND`, `GIVE_IT`, `DUNGLOA`, `BOT*`, `CHANGE_NAME`, `FIND_PLAYER`).
  - Reset `player.idMark.setTypeInput(-1)` ngay đầu/cuối `doInput()` (trong khối `try/finally`) trước khi xử lý logic, và bọc `synchronized (player)` + kiểm tra `player.isTrade` cho các case thay đổi tài sản (`TANG_NGOC_HONG`, `BANSLL`, `USE_CARD_QUANTITY`).

### 1.3. Ký Gửi Đồ Khóa (Option 30) & Bypass Kiểm Tra Ép Đồ / Sách Tuyệt Kỹ
- **Vị trí**:
  - `src/services/ConsignShopService.java:376-390`
  - `src/models/combine/list/ChuyenHoaBangNgoc.java:102-155`
  - `src/models/combine/list/NangCapSachTuyetKi.java:42-74`
- **Trạng thái**: **CONFIRMED (P0)**
- **Bằng chứng & Kịch bản khai thác**:
  1. `ConsignShopService.itemCanConsign()` kiểm tra `if (hasConsignOption || it.template.type == 14 || it.template.type == 15) return true;` (dòng 381-383) **trước** khi kiểm tra `if (it.isHaveOption(30)) return false;` (dòng 385-387). Do đó mọi vật phẩm loại 14/15 hoặc có option 86/87 nhưng bị khóa giao dịch (option 30) vẫn ký gửi được bình thường (check option 30 trở thành dead code).
  2. `ChuyenHoaBangNgoc.ChuyenHoaBangngoc()` thiếu kiểm tra `option 72 || option 102` trên `trangBiCanChuyenHoa` (dù `showInfoCombine` ở dòng 39-40 có kiểm tra), cho phép chuyển hóa vào trang bị đã nâng cấp/pha lê hóa.
  3. `NangCapSachTuyetKi.NangCapSachTuyetki()` không kiểm tra giới hạn `template.id` (`1181-1183`) và **không quay tỉ lệ 10%** (`Util.isTrue(10, 100)`), luôn cho thành công 100% (`sendEffectSuccessCombine` ở dòng 66).
- **Cách vá**:
  - Đưa `if (it.isHaveOption(30)) return false;` lên đầu `itemCanConsign()` (trước mọi điều kiện `return true`).
  - Đồng bộ toàn bộ điều kiện validate từ `showInfoCombine` vào hàm thực thi ở `ChuyenHoaBangNgoc` (và `ChuyenHoaBangVang`).
  - Thêm kiểm tra `template.id` (`1181..1183`) và roll tỉ lệ `Util.isTrue(10, 100)` trong `NangCapSachTuyetki` (thất bại chỉ trừ 10 Kìm bấm giấy).

### 1.4. Nhân Chỉ Số x127 Miễn Phí Tại Ngọc Rồng Sao Đen (`BlackBallWarService`)
- **Vị trí**: `src/services/phoban/BlackBallWarService.java:128-181`
- **Trạng thái**: **CONFIRMED (P0)**
- **Bằng chứng & Kịch bản khai thác**:
  - `xHPKI(Player player, byte x)` (128-155) và `xDame(Player player, byte x)` (157-181) khởi tạo `int cost = 0;` và chỉ gán `cost` trong `switch (x)` cho `X3`, `X5`, `X7` mà **không có `default:`**. Nếu client gửi byte `x = 127`, `cost` vẫn là `0`, `checkGemAndRuby(0)` trả về `true`, và server gán thẳng `effectSkin.xHPKI = 127` / `xDame = 127` với giá 0 ngọc.
- **Cách vá**:
  - Thêm `default: Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ"); return;` trong cả `xHPKI` và `xDame`.

### 1.5. Rửa Hồng Ngọc Sang Ngọc Xanh Ở Đại Hội Võ Thuật (`WorldMartialArtsTournamentService`)
- **Vị trí**: `src/services/tournament/WorldMartialArtsTournamentService.java:158-219`
- **Trạng thái**: **CONFIRMED (P0)**
- **Bằng chứng & Kịch bản khai thác**:
  - Lúc đăng ký, server trừ cả Ngọc Xanh và Hồng Ngọc qua `player.inventory.subGemAndRuby(gem)` (dòng 166, 200). Nhưng lúc hủy đăng ký, server lại hoàn trả **100% Ngọc Xanh** qua `player.inventory.addGem(gem)` (dòng 213) và tính phí hoàn trả theo `getTournament()` tại thời điểm hủy (dòng 205) thay vì số tiền thực tế đã đóng. Người chơi có thể lặp lại đăng ký/hủy để đổi toàn bộ Hồng Ngọc thành Ngọc Xanh.
- **Cách vá**:
  - Lưu lại chính xác loại tiền và số lượng (`gemPaid`, `rubyPaid`) của người chơi khi đăng ký vào manager/map theo `player.id` và hoàn trả đúng số lượng từng loại khi hủy.

### 1.6. Các Lỗ Hổng P0 Khác Đã Ghi Nhận Trong Digest
- **Bypass Anti-Teleport (`Controller.java:489-513`)**: `isNearMobInZone` tin tưởng tọa độ `(toX, toY)` từ client gửi lên mà không kiểm tra `player.isTDLT`, cho phép mọi người chơi dịch chuyển tức thời đến bất kỳ quái nào trong map.
- **Instant-Cast QCKK / Makankosappo (`SkillService.java:478-542`)**: Không kiểm tra thời gian tụ lực (4000ms / 3000ms) ở lần gọi thứ 2.
- **Admin API Bypass (`AdminApiService.java:54, 120-160`)**: Hardcode token `"nro_admin_secret_2026"`, bypass auth cho `127.0.0.1` kèm `Access-Control-Allow-Origin: *` (dễ bị tấn công CSRF/CORS từ trình duyệt).
- **Thiếu kiểm tra/khóa ở Shop & NPC**: `ShopService.buyItemHD` / `buyItem` / `buyItemDaBan`, `BaHatMit` (đóng sách), `CayThong` / `HungVuong`, `SummonDragon`, `SpecialItemHandler.changePet`, `ItemService.OpenSKH/OpenSKHVIP`, `GiftCodeService`.

---

## 2. Các Vấn Đề Đa Luồng, Deadlock & Race Condition (`Zone`, `Boss`, `PhoBan`, `Session`)

### 2.1. Busy-Wait 2.5 Giây Trong `synchronized` Ở Boss `SAIBAMEN`
- **Vị trí**: `src/models/boss/boss_list/SnakeWay/SAIBAMEN.java:72, 104-172`
- **Trạng thái**: **CONFIRMED (P0)**
- **Phân tích**:
  - `SAIBAMEN.injured(...)` được khai báo `public synchronized long injured(...)` (dòng 104). Khi boss hết máu (`isDie()`), nó gọi thẳng `setBom(plAtt)` (dòng 119). Bên trong `setBom()` chạy vòng lặp `while (this.playerSkill.prepareTuSat)` kiểm tra `Util.canDoWithTime(..., 2500)` (dòng 152-171) — **spin-wait 100% CPU suốt 2.5 giây** ngay trên thread xử lý đòn đánh của người chơi, đồng thời giữ lock `synchronized` của `SAIBAMEN`. Ngoài ra dòng 133-134 gọi `chat(plAtt, ...)` và `startStun(plAtt, ...)` không kiểm tra `plAtt != null`, và `afk()` (dòng 72) gọi `Functions.sleep(1500)` chặn thread update boss.
- **Cách vá**:
  - Loại bỏ vòng lặp `while` busy-wait và `Functions.sleep(1500)`; chuyển sang cơ chế state machine trong `update()` của `SAIBAMEN` (đánh dấu `prepareTuSat = true`, kiểm tra `Util.canDoWithTime(..., 2500)` mỗi tick mà không block thread). Kiểm tra `if (plAtt != null)` trước khi chat/stun.

### 2.2. TOCTOU 500ms Đặt Zone Đấu Trường (`DeathOrAliveArenaService`)
- **Vị trí**: `src/services/tournament/DeathOrAliveArenaService.java:39-109`
- **Trạng thái**: **CONFIRMED (P0)**
- **Phân tích**:
  - `getMapChallenge(112)` tìm zone trống dựa trên `DeathOrAliveArenaManager.getVDST(zone) == null` (dòng 39), nhưng đối tượng `DeathOrAliveArena` chỉ được tạo và `add` vào manager **500ms sau đó** bên trong `setTimeout()` (dòng 94). Hai người chơi bấm thách đấu trong cùng khoảng 500ms sẽ nhận cùng một `zone`, đè trận đấu của nhau và mất vé/bình nước phép. Nếu người chơi thoát game trong 500ms đó, `player.isPKDHVT = true` vẫn bị gán ở dòng 103.
- **Cách vá**:
  - Đặt lock/reserve `zone` ngay lập tức (hoặc tạo và đăng ký `DeathOrAliveArena` vào manager đồng bộ dưới lock trước khi `setTimeout`), và kiểm tra `player.zone != null && player.zone.map.mapId == 112` trong callback.

### 2.3. Race Condition Mở Phó Bản & Nhặt Ngọc Namek (`PhoBan` Services)
- **Vị trí**: `src/services/phoban/NgocRongNamecService.java:357-398`, `DestronGasService.java:36-75`, `SnakeWayService.java:38-77` (và `TreasureUnderSeaService`, `RedRibbonHQService`)
- **Trạng thái**: **CONFIRMED (P0)**
- **Phân tích**:
  - Kiểm tra và cấp phát phó bản (`if (player.clan.KhiGasHuyDiet == null)` -> duyệt tìm `!kghd.isOpened` -> `openKhiGasHuyDiet`) hoàn toàn không có `synchronized` trên `clan` hay danh sách phó bản. Hai thành viên bang hội bấm mở cùng lúc có thể chiếm 2 slot phó bản hoặc ghi đè `clan.KhiGasHuyDiet` / `ConDuongRanDoc`.
  - `NgocRongNamecService.pickNamekBall()` cập nhật các mảng dùng chung (`mapNrNamec`, `idpNrNamec`, ...) không đồng bộ hóa.
- **Cách vá**:
  - Bọc `synchronized (clan)` và đồng bộ hóa trên pool phó bản khi kiểm tra & gán `isOpened = true`; đồng bộ hóa thao tác nhặt/cập nhật trạng thái Ngọc Rồng Namek.

### 2.4. Bất Nhất Đồng Bộ Trong `Zone.java`
- **Vị trí**: `src/models/map/Zone.java:167-223, 242-250`
- **Trạng thái**: **CONFIRMED (P0/P1)**
- **Phân tích**:
  - `Zone` dùng `CopyOnWriteArrayList` cho `players`, `humanoids`, `notBosses`, `bosses` (gây tốn bộ nhớ/copy mảng mỗi lần người chơi chuyển map, và `if (!this.humanoids.contains(player)) this.humanoids.add(player)` vẫn có thể thêm trùng khi gọi đồng thời).
  - Trong khi đó `items`, `mobs`, `trapMaps` là `ArrayList` thường. `Zone.udItem()` khóa `synchronized (this.items)` (dòng 172), nhưng các hàm bên ngoài như `NgocRongNamecService.removeStoneNrNamec()` lại xóa phần tử trên `zone.items` không giữ lock `zone.items`, dễ gây `ConcurrentModificationException` hoặc `IndexOutOfBoundsException`.
- **Cách vá**:
  - Chuẩn hóa cơ chế lock khi truy cập/xóa `zone.items` (hoặc dùng `ConcurrentHashMap.newKeySet()` / `CopyOnWriteArrayList` cho tập ít thay đổi, và iterator/removeIf dưới `synchronized (zone.items)`).

### 2.5. Client `Session_ME2.cs` Thiếu Lock Hàng Đợi Gửi/Nhận Đa Luồng
- **Vị trí**: `Client/Client/Assets/Scripts/Game1/Session_ME2.cs:13-61`
- **Trạng thái**: **CONFIRMED (P0)**
- **Phân tích**:
  - `Sender.sendingMessage` (`List<Message>`) được thêm từ Main thread (`AddMessage` dòng 22-25) và đọc/xóa (`sendingMessage[0]`, `RemoveAt(0)` dòng 37-39) từ Sender thread **không có `lock`**, dễ gây `ArgumentOutOfRangeException`, hỏng cấu trúc `List` hoặc rớt gói tin.
- **Cách vá**:
  - Áp dụng `lock (sendingMessage)` (hoặc dùng `ConcurrentQueue<Message>`) tương tự `Session_ME.cs` khi `AddMessage`, đọc/xóa và `Clear`.

### 2.6. Race Condition Đăng Nhập / Đăng Xuất Async (`MySession.java`, `Client.java`, `PlayerDAO.java`)
- **Vị trí**: `src/network/session/MySession.java:125-172`, `src/server/Client.java:162-197`, `src/database/daos/PlayerDAO.java:374-393`
- **Phân tích (từ Digest P0)**:
  - `ACCOUNT_LOCKS.remove(username, accountLock)` trong `finally` của `MySession.login` có thể xóa lock trong khi luồng thứ 2 đang chờ vào `synchronized(accountLock)`, khiến luồng thứ 3 tạo lock mới và chạy song song với luồng thứ 2 -> tải 2 instance `Player` cho cùng 1 tài khoản.
  - `Client.removePlayerFromGame` xóa player khỏi map online trước rồi mới đẩy `PlayerDAO.updatePlayer` lên Virtual Thread bất đồng bộ mà không có rào chắn (save barrier) khi `login` lại ngay lập tức -> đăng nhập lại đọc dữ liệu cũ từ DB (rollback/dupe đồ).

---

## 3. Đánh Giá Tổng Quan 2 Khía Cạnh

| Khía cạnh | Điểm (/10) | Nhận định chính |
| :--- | :---: | :--- |
| **Bảo mật & Chống Dupe Kinh Tế** | **2.5 / 10** | Có quá nhiều lỗ hổng mức P0 dễ khai thác trực tiếp qua packet: thiếu check `isAdmin()` trong `Input.java`, self-trade & snapshot-overwrite dupe trong `Trade`, bypass tỉ lệ/điều kiện ép đồ, nhân chỉ số miễn phí, đổi Ruby->Gem, ký gửi đồ khóa. Cần vá khẩn cấp trước khi mở server public. |
| **Concurrency & Realtime Loop** | **3.0 / 10** | Mô hình đa luồng thiếu kỷ luật khóa thống nhất: spin-wait 2.5s trong `synchronized` (`SAIBAMEN`), TOCTOU 500ms khi tạo trận đấu, async save không đồng bộ với login, thiếu lock ở `InventoryService`/`PhoBan`/`Zone.items`, và race condition queue ở Client `Session_ME2`. |

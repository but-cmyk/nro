# Báo Cáo Kiểm Toán Toàn Diện Dự Án NRO_219 (`05_FULL_PROJECT_AUDIT_REPORT.md`)

**Ngày lập báo cáo**: `2026-11-26`  
**Vai trò kiểm toán**: Principal Software Architect  
**Phạm vi kiểm toán**: 100% mã nguồn dự án (`0` tệp bỏ sót qua `48` batch phân tích tĩnh & kiểm chứng trực tiếp trên mã nguồn).

---

## 1. Tóm Tắt Điều Hành (Executive Summary) & Quy Mô Dự Án

### 1.1. Quy Mô Mã Nguồn (`00_INDEX_SUMMARY.md`)
| Phân hệ | Số lượng Tệp | Số dòng Code | Công nghệ & Vai trò |
| :--- | ---: | ---: | :--- |
| **Server Java (`src/`)** | 596 files | 120,788 dòng | Java 21, Netty 4, Virtual Threads, HikariCP, MySQL 8, Redis (Jedis) |
| **Client Unity C#** | 247 files | 97,173 dòng | Unity 2022.3 / .NET 4.8, `OnGUI` legacy rendering, TCP Dual-Session |
| **Tooling & Test (`test/`, `build/`, `tools/`)** | 10 files | 1,164 dòng | `compile_check.py`, `guardrails.py`, `TestProfilerEngine`, `TestWhisArenaLogic` |
| **Tổng cộng toàn dự án** | **853 files** | **219,125 dòng** | **Monolithic Client-Server MMORPG** |

### 1.2. Kết Luận Điều Hành (Executive Verdict)
**Điểm trung bình toàn hệ thống: `4.4 / 10` — CHƯA SẴN SÀNG CHO MÔI TRƯỜNG PUBLIC PRODUCTION.**

Dự án NRO_219 đã có những bước tiến kỹ thuật rất đáng ghi nhận ở tầng hạ tầng hiện đại hóa (nâng cấp **Java 21 Virtual Threads + Netty 4**, tách nhịp **GameLoopManager**, tối ưu **HikariCP + `PlayerDAO.extractSnapshot()`**, và xây dựng hệ sinh thái **Loop Engineering / Compile Gate / Telemetry** vượt trội so với mặt bằng chung).

Tuy nhiên, phần lõi nghiệp vụ kế thừa (legacy core) đang gánh khoản nợ kỹ thuật lớn và chứa **hàng loạt lỗ hổng mức P0 đã được xác minh trực tiếp**:
- **Kinh tế & Bảo mật sụp đổ nếu mở Public ngay**: Thiếu kiểm tra `isAdmin()` trên nhiều nhánh `Input.java` (cho phép tự buff VND, spawn item), `Self-Trade` và `Snapshot-Overwrite` dupe đồ trong `Trade`, nhân chỉ số `x127` miễn phí tại `BlackBallWarService`, rửa Hồng Ngọc sang Ngọc Xanh ở ĐHVT, ký gửi đồ khóa (option 30), và bypass tỉ lệ 10% sách tuyệt kỹ.
- **Độ ổn định Runtime & Concurrency mỏng manh**: `SAIBAMEN` spin-wait 100% CPU suốt 2.5 giây ngay trong `synchronized`; `ConsignShopManager.save()` xóa toàn bảng rồi chèn lại xung đột với Virtual Threads bất đồng bộ; `GiftService.isUsedGiftCode()` rò rỉ `Connection` tới cạn HikariCP pool; Client `Controller.cs` rơi vào vòng lặp vô hạn khi `myReader` không ném Exception lúc EOF; `SummonDragonNamek` lệch stream `-83`.
- **Sai lệch Gameplay cốt lõi**: Công thức `NPoint.calPercent()` chia cho `150` thay vì `100` (mọi chỉ số phần trăm chỉ đạt 66.7%), `NewSkill` dùng phép gán `=` thay vì `==`, `Yardart` reset `hp = 1` sau hồi sinh cho phép farm vô hạn item 590, và `SkillService` trị thương nhân bội lượng hồi máu theo số mục tiêu.

---

## 2. Bảng Chấm Điểm Toàn Diện 8 Khía Cạnh

| STT | Khía Cạnh Đánh Giá | Điểm (/10) | Mức Độ Rủi Ro | Nhận Xét Sắc Bén Từ Kiến Trúc Sư |
| :-: | :--- | :---: | :---: | :--- |
| 1 | **Kiến trúc (Architecture)** | **4.5 / 10** | Cao | Netty 4 + Dispatcher mới là hướng đi đúng, nhưng hệ thống bị thống trị bởi 7 God Classes (`Panel.cs` 11.8k dòng, `Char.cs` 8.7k, `GameScr.cs` 8.3k, `Controller.cs` 6.8k) và phụ thuộc vòng chéo nặng (`models -> services`: 818 imports vs `services -> models`: 332 imports). |
| 2 | **Protocol Mạng (Network & Framing)** | **5.5 / 10** | Trung bình - Cao | `NroPacketDecoder` xử lý TCP fragmentation chuẩn không lệch XOR. Ngược lại, Client tồn tại lỗi framing P0 (`Session_ME` không check `ReadExactBytes`, `readMessage2` thiếu check key/OOM), `while(true)` treo cứng với `myReader`, và lệch stream opcode `-83`, `-109`, ký gửi ID > 32767. |
| 3 | **Database & Redis Persistence** | **5.5 / 10** | Trung bình - Cao | Chống SQLi tốt nhờ `PreparedStatement` 100%, `PlayerDAO.extractSnapshot` tách serialize khỏi JDBC I/O rất thông minh. Nhưng `ConsignShopManager.save()` (`DELETE ALL + INSERT ALL`) phá vỡ ACID, `GiftService` leak `Connection`, `subvnd`/`subcash` thiếu `WHERE sotien >= ?`, và `ResultSetImpl` chưa tương thích `LocalDateTime` / alias MySQL 8. |
| 4 | **Bảo mật & Chống Dupe Kinh Tế** | **2.5 / 10** | **Cực Kỳ Nghiêm Trọng** | "Mở cửa" cho packet hacker: `Input.java` thiếu `isAdmin()` cho phép spawn đồ/buff VND, không reset `typeInput` cho phép replay; `Trade` cho phép Self-Trade và Snapshot-Overwrite; `BlackBallWar` buff x127 giá 0 ngọc; `WorldMartialArts` đổi Ruby thành Gem; Bypass anti-teleport & tụ lực skill. |
| 5 | **Concurrency & Realtime Loop** | **3.0 / 10** | **Cực Kỳ Nghiêm Trọng** | Thiếu kỷ luật khóa thống nhất: `SAIBAMEN` busy-wait 2.5s giữ lock `synchronized`; `DeathOrAliveArenaService` có lỗ hổng TOCTOU 500ms; `PhoBan` thiếu lock khi mở map; `Zone.items` bị xóa không lock; `MySession.login` / async save thiếu rào chắn; Client `Session_ME2` thao tác `List` không lock. |
| 6 | **Hiệu năng CPU / RAM / GC** | **4.0 / 10** | Cao | Server ghi file `PERFORMANCE_ALERTS.json` đồng bộ ngay trên Netty IO Thread, khởi tạo 6 `CopyOnWriteArrayList` mỗi `Zone`, leak `OPTION_STRING` & `playerTickMap`. Client vi phạm nặng Zero-GC (`new Color[]`, `ToLower()`, đọc file `/Mob/50` ngay trong `paint()`, gọi `GC.Collect()` đồng bộ). |
| 7 | **Gameplay Logic & Cân Bằng** | **3.5 / 10** | **Nghiêm Trọng** | Hàng loạt bug P0 phá vỡ cân bằng và kinh tế: `calPercent` chia `150`, `NewSkill` gán `= true`, `Yardart` `hp = 1`, `SkillService` trị thương nhân bội & hồi cho địch, `ChestBoxItemHandler` mở rương gỗ vô hạn và chế Trứng Rồng từ 1 mảnh. |
| 8 | **DevOps, Tooling & Testing** | **6.5 / 10** | Thấp - Trung bình | Điểm sáng lớn nhất của dự án: `compile_check.py` (Roslyn + javac), `tools/memory/` guardrails & lessons, `TrajectoryLogger` non-blocking. Điểm trừ là test còn thủ công (`main()`), chưa có CI/CD, còn sót `DdosTester.java` trong `src/` và `TEST_MODE_24_7 = true`. |
| **—** | **TRUNG BÌNH TOÀN DỰ ÁN** | **4.4 / 10** | **CẦN VÁ P0 KHẨN CẤP** | **Cốt lõi hạ tầng tốt nhưng bị các lỗi P0 Bảo mật, Concurrency và Gameplay kéo tụt nghiêm trọng.** |

---

## 3. Điểm Mạnh Cốt Lõi Của Dự Án

1. **Kiến Trúc Mạng Server Hiện Đại (Java 21 + Netty 4)**:
   - `NroPacketDecoder` (`src/network/netty/NroPacketDecoder.java:33-101`) dùng `markReaderIndex()` và con trỏ tạm `tempR` để kiểm tra đủ payload trước khi cập nhật `curR`, giải quyết triệt để lỗi phân mảnh TCP và lệch keystream XOR.
   - `NettySession` (`src/network/netty/NettySession.java:49-98`) tách Netty EventLoop khỏi game logic bằng `messageMailbox` giới hạn `MAX_MAILBOX_SIZE = 300` và Java 21 **Virtual Thread** (`Thread.ofVirtual()`), đảm bảo xử lý tuần tự 1 gói tin/session mà không nghẽn EventLoop.
2. **Định Hướng Tách God Class Bằng Dispatcher Pattern**:
   - Đã khởi tạo `ServerPacketDispatcher` (Server) và `PacketDispatcher` + `ISubPanel` (Client) cùng 8 nhóm handler tách biệt (`Auth`, `PlayerStats`, `TaskNpc`, `Clan`, `Trade`, `Item`, `MapMovement`, `Combat`), đặt nền móng rõ ràng cho việc giải thể `Controller.cs` (6,894 dòng) và `Panel.cs` (11,857 dòng).
3. **Tối Ưu Hóa Vòng Lặp & Persistence Gần Đây**:
   - `GameLoopManager` phân tách tần suất cập nhật hợp lý (`1000ms` cho Player/Zone/Phó Bản, `250ms` cho Combat Boss, giảm tải `5000ms` cho `Zone` rỗng người chơi).
   - `PlayerDAO.extractSnapshot()` khóa `synchronized (player)` chỉ trong pha serialize RAM rồi nhả lock trước khi ghi JDBC, kết hợp `deepHashCode` (`lastSaveHash`) để bỏ qua ghi DB khi dữ liệu không đổi.
   - 100% truy vấn kiểm tra đều dùng `PreparedStatement` tham số hóa chống SQL Injection; `HikariCP` cấu hình chuẩn (`leakDetectionThreshold`, `rewriteBatchedStatements`); `RedisService` có cơ chế fallback in-memory an toàn khi khởi động.
4. **Hệ Sinh Thái Tooling, Telemetry & Guardrails Xuất Sắc**:
   - **Compile Verification Gate (`tools/compile_check.py`)**: Biên dịch chéo tự động toàn bộ Client Unity C# (Roslyn `csc.dll`) và Server Java 21 (`javac`).
   - **Memory & Guardrails (`tools/memory/`)**: Quản lý tri thức lịch sử (`LESSONS.jsonl`), định tuyến nhanh (`CODE_ROUTING.json`), quét vi phạm tự động (`guardrails.py`, `VIOLATIONS_BASELINE.json`).
   - **AI Telemetry & Profiling (`src/telemetry/`, `PacketProfiler`)**: `TrajectoryLogger` dùng `ArrayBlockingQueue` (Ring Buffer non-blocking) + `AntiMacroFilter` + thread nền `AI-Trajectory-Flusher` tránh block Game Loop.

---

## 4. Danh Sách Lỗi Nghiêm Trọng Đã Xác Minh (Verified P0 & Top P1)

*(Lưu ý: Toàn bộ các lỗi dưới đây đã được kiểm chứng trực tiếp trên mã nguồn; các cảnh báo giả như `ConstItem.SIEU_THAN_THUY`, `messWait cleanup()`, `Opcode -73`, hay `rs.dispose() HikariCP leak` đã được loại bỏ).*

### 4.1. Nhóm Lỗi P0 — Cần Vá Ngay Lập Tức (Blockers)

| ID | Lĩnh Vực | Vị Trí (`File:Line`) | Tác Động Thực Tế | Cách Sửa Đề Xuất |
| :-: | :--- | :--- | :--- | :--- |
| **P0-01** `[FIXED]` | **Bảo mật / Admin** | `src/services/func/Input.java:96-170, 426-580, 732-770` | Các nhánh `BUFFVND`, `GIVE_IT`, `DUNGLOA`, `BOT*`, `CHANGE_NAME` **thiếu `isAdmin()`**; `doInput()` không reset `typeInput = -1` và thiếu `synchronized(player)` trong `TANG_NGOC_HONG`, `BANSLL`. | **Đã vá**: Reset `typeInput(-1)` đầu `doInput()`, thêm `isAdmin()` cho 6 nhánh đặc quyền, validate input & bọc `synchronized(player)` + chặn `isTrade`. |
| **P0-02** `[FIXED]` | **Dupe Đồ (Trade)** | `src/services/func/TransactionService.java:63-78`<br>`src/services/func/Trade.java:392-527` | **Self-Trade**: Cho phép giao dịch với chính `pl.id`; **Snapshot-Overwrite** khi luồng khác sửa túi đồ giữa chừng. | **Đã vá**: Chặn `playerId == pl.id || plMap == pl` và `isTrade` tại `TransactionService`; khóa 2 chiều `synchronized(firstLock/secondLock)` theo ID tăng dần trong `Trade.startTrade()`; chặn `Input` sửa túi đồ khi `isTrade`. |
| **P0-03** `[FIXED]` | **Kinh tế / Bypass** | `src/services/ConsignShopService.java:376-390`<br>`ChuyenHoaBangNgoc.java:102-190`<br>`NangCapSachTuyetKi.java:42-86` | Đồ khóa (option 30) vẫn ký gửi được; `ChuyenHoaBangNgoc` thiếu lock & check bag/option 30; `NangCapSachTuyetKi` lỗi NPE trước check null và **luôn thành công 100%**. | **Đã vá**: Đưa check `Option 30` lên đầu `itemCanConsign()`; bọc `synchronized(player)` + validate bag/option 30 ở `ChuyenHoaBangNgoc`; sửa thứ tự check null, kiểm tra ô trống & roll tỉ lệ `10%` ở `NangCapSachTuyetKi`. |
| **P0-04** `[FIXED]` | **Kinh tế / Buff** | `src/services/phoban/BlackBallWarService.java:128-181`<br>`WorldMartialArtsTournamentService.java:144-235`<br>`WorldMartialArtsTournamentManager.java:25,173,209` | `xHPKI`/`xDame` thiếu `default:` cho phép buff `x127` với giá 0 ngọc; ĐHVT hoàn `100% Ngọc Xanh` dù trừ Hồng Ngọc và thiếu check xóa trước khi hoàn tiền. | **Đã vá**: Thêm `default:` chặn hệ số lạ trong `xHPKI`/`xDame`; theo dõi chính xác `(gold, gemPaid, rubyPaid)` trong `regFees` và chỉ hoàn đúng loại tiền khi `listReg.remove(player.id)` thành công. |
| **P0-05** `[FIXED]` | **Anti-Cheat / Skill** | `src/server/Controller.java:488-513`<br>`src/services/SkillService.java:478-542` | `isNearMobInZone` cho phép dịch chuyển khắp map không cần `isUseTDLT`; `QCKK`/`Makankosappo` thiếu kiểm tra thời gian tụ lực. | **Đã vá**: Chỉ miễn kiểm tra tốc độ khi `isUseTDLT`; thêm check `Util.canDoWithTime` (`3700ms`/`2700ms`) trước khi ném `QCKK`/`Makankosappo`. |
| **P0-06** `[FIXED]` | **Gameplay / Chỉ số** | `src/models/player/NPoint.java:927-933`<br>`src/models/player/NewSkill.java:51-55` | `calPercent()` chia cho `150` thay vì `100`; `NewSkill` viết `= true` thay vì `==`. | **Đã vá**: Đổi mẫu số `calPercent` về `100`; sửa `if (this.isStartSkillSpecial)` trong `NewSkill.java`. |
| **P0-07** `[FIXED]` | **Gameplay / Boss & Skill** | `Yardart.java:251-261`<br>`SkillService.java:852-875`<br>`ChestBoxItemHandler.java:122-185, 249-268` | `Yardart` gán `hp = 1` sau `hsChar()`; `TRI_THUONG` hồi bội bản thân & hồi cho địch; `open1427` không check `quantity >= 99`. | **Đã vá**: Xóa `hp = 1` ở `Yardart`; sửa `canHsPlayer(player, pl)` và đưa hồi bản thân ra ngoài vòng lặp; kiểm tra `quantity >= 99`, đủ ô trống và `synchronized(pl)` trong `ChestBoxItemHandler`. |
| **P0-08** `[FIXED]` | **Concurrency / CPU** | `src/models/boss/boss_list/SnakeWay/SAIBAMEN.java:104-175` | `injured()` (`synchronized`) gọi `setBom()` chạy `while` spin-wait 100% CPU 2.5s và thiếu check `plAtt != null`. | **Đã vá**: Gỡ bỏ hoàn toàn vòng `while` spin-wait khỏi `setBom()`, chuyển xử lý nổ sang `SAIBAMEN.update()` không block và check `plAtt != null`. |
| **P0-09** `[FIXED]` | **Concurrency / Race** | `DeathOrAliveArenaService.java:39-115`<br>`NgocRongNamecService.java:357-402`<br>`DestronGasService.java:47-75`, `SnakeWayService.java:53-75` | **TOCTOU 500ms** ở ĐTST; nhặt NRNM & mở Phó Bản thiếu khóa đồng bộ. | **Đã vá**: Khóa `synchronized(DeathOrAliveArenaManager.gI())` giữ chỗ `zone` trước `setTimeout`; bọc `synchronized(player)` khi nhặt NRNM và `synchronized(clan)` khi mở `KhiGas`/`RanDoc`. |
| **P0-10** `[FIXED]` | **Database ACID & Leak** | `src/managers/ConsignShopManager.java:42-135`<br>`src/services/player/GiftService.java:145-162` | `ConsignShopManager.save()` thiếu `rollback()` và return sớm khi rỗng; `GiftService.isUsedGiftCode()` rò rỉ `Connection`. | **Đã vá**: Bỏ return sớm khi rỗng, thêm `con.rollback()` trong `catch` ở `ConsignShopManager.save()`; đưa `Connection` vào `try-with-resources` ở `GiftService`. |
| **P0-11** `[FIXED]` | **Protocol / Client Freeze** | `Client/.../Controller.cs:96-104, 3201-3212`<br>`Client/.../Session_ME.cs:171-250`<br>`Client/.../Session_ME2.cs:13-65, 156-235`<br>`SummonDragonNamek.java:101-125` | `while(true)` treo cứng `myReader`; `ReadBytes` thiếu đọc đủ TCP payload & giới hạn kích thước; `Session_ME2.Sender` không lock; `-83` thiếu `writeInt(pl.id)`. | **Đã vá**: Đổi sang `while (msg.reader().available() > 0)`; thêm `ReadExactBytes` kiểm tra độ dài `0..2MB`; bọc `lock (sendingMessage)` ở `Session_ME2`; bổ sung `writeInt((int)pl.id)` & `finally { msg.cleanup(); }` ở `-83`. |
| **P0-12** `[FIXED]` | **Protocol / Ký Gửi** | `src/services/ConsignShopService.java:181-215, 655-730` | Ghi `writeByte(size)` trước nhưng `continue` khi `itk == null`, gây lệch stream; nguy cơ tràn `writeByte` > 255. | **Đã vá**: Lọc bỏ phần tử `null` và giới hạn `< 255` **trước** khi ghi `writeByte(validItems.size())`. |

---

### 4.2. Top Lỗi P1 — Ưu Tiên Cao (High Priority)

1. **Database & MySQL 8 Compatibility (`ResultSetImpl.java`, `PlayerDAO.java`, `NDVSqlFetcher.java`, `Manager.java`)**:
   - `PlayerDAO.subvnd()` / `subVND2()` / `subcash()` (`PlayerDAO.java:1295-1307, 1641-1674`) trừ tiền không có `AND sotien >= ?` / `AND cash >= ?` và `subVND2` rò rỉ `PreparedStatement`.
   - `ResultSetImpl` (`41-50, 496-541`) dùng `getColumnName` (hỏng mọi SQL `AS alias`) và chưa hỗ trợ `LocalDateTime` của MySQL 8 trong `getTimestamp()`.
   - `PlayerDAO.createNewPlayer()` ghi `"0"` vào cột JSON array (`data_achievement`, `giftcode`), và `extractSnapshot()` (`1160`) hardcode `boughtSkills = "[]"` làm mất skill đã mua khi save.
   - `Manager.queryTopSM` (`159-165`) JOIN sai `p.id = a.id` thay vì `p.account_id = a.id`.
   - `AlyraManager.login()` (`256-332`) trùng lặp nhưng bị hỏng logic (truyền `SELECT id` vào `loadPlayer()` đọc 35 cột).
2. **Protocol & Client Network (`Session_ME.cs`, `Session_ME2.cs`, `myWriter.cs`, `KeyHandler.java`, `NroPacketEncoder.java`)**:
   - `Session_ME2.cs:187, 237` thiếu `|| b == 66` trong `readMessage()` và đặt sai `isMainSession = true`.
   - `Session_ME` / `ME2` dùng `sbyte` cho `keyLen`, `curR`, `curW` (tràn số nếu key >= 128 byte); nhánh `!getKeyComplete` trong `doSendMessage` ghi Little-Endian và thiếu `else` ghi raw `data`.
   - `myWriter.writeSByte` (`192-194`) gọi `checkLenght(0)` gây off-by-one `IndexOutOfRangeException`.
   - `KeyHandler.sendKey` (`23-25`) set `setSentKey(true)` sau khi gửi async (race condition handshake).
3. **Hiệu Năng CPU/RAM/GC (`PacketProfiler.java`, `Item.java`, `TrajectoryLogger.java`, `Zone.java`, `Char.cs`, `mFont.cs`, `Panel.cs`)**:
   - Server: `PacketProfiler.triggerPerformanceAlert()` (`202-249`) giữ `synchronized` toàn cục và ghi file JSON đồng bộ trên Netty IO Thread; `ItemOption.OPTION_STRING` và `TrajectoryLogger.playerTickMap` leak RAM không giới hạn; `PhaLeHoaTrangBi.startCombineVip` (`137-188`) không giới hạn vòng lặp `n` từ client (DoS).
   - Client: `mFont.cs` tạo `new Color[]` mỗi frame; `Char.paintMount1/2` đọc file `/Mob/50` đồng bộ trong `paint()`; `Panel.setTabMap()` / `TileMap.freeTilemap()` gọi `Resources.UnloadUnusedAssets(); GC.Collect();` trên Main Thread; `Image.createImage` rò rỉ `Texture2D` native; `Session_ME2` `Thread.Sleep(5)` giới hạn tải asset còn 200 gói/giây.
4. **Gameplay & Client UI (`SkillService.java`, `Pet.java`, `SetClothes.java`, `SieuBoHung.java`, `Drabura2.java`, `Cadic.java`, `GameCanvas.cs`)**:
   - `Kaioken` cụt HP > 2.14 tỷ về `Integer.MAX_VALUE`; `HuytSao` buff & trừ HP người trung lập; `Pet.followMaster` thiếu `Math.abs()` spam packet; `SetClothes` dùng `break` thay vì `continue` khi gặp ô trống; `SieuBoHung` truyền `plAtt = null`, `Drabura2` reset `damage = 0`, `Cadic` né 100%, `SnakeWay` tràn `int` HP Cadich từ level >= 114.
   - Client: `panel.updateKey()` chạy 2 lần/tick; phím 15 bị kẹt `keyHold[15] = true`; `chatVip` bắt đầu bằng `!` văng Exception; nút Back đổi cờ (`10314`) đóng nhầm `Session_ME`.
5. **Admin API & Production Hygiene (`AdminApiService.java`, `DdosTester.java`, `WhisArenaService.java`)**:
   - `AdminApiService` hardcode token `"nro_admin_secret_2026"`, bypass auth cho `127.0.0.1` kèm CORS `*`.
   - Tồn tại `src/testddos/DdosTester.java` trong `src/` production và `WhisArenaService.TEST_MODE_24_7 = true`.

---

## 5. Lộ Trình Hành Động Đề Xuất (Prioritized Roadmap)

### Tuần 1: Khẩn Cấp — Vá Toàn Bộ P0 (Chống Dupe, Bảo Mật, Crash & ACID)
*Mục tiêu: Đưa hệ thống về trạng thái an toàn tối thiểu để vận hành thử nghiệm / public, không mất dữ liệu và không bị khai thác kinh tế.*
1. **Ngày 1–2 (Bảo mật & Kinh tế)**:
   - Vá `Input.java` (thêm `isAdmin()`, reset `typeInput`, khóa `synchronized(player)`).
   - Vá `Trade.java` & `TransactionService.java` (chặn Self-Trade, khóa theo thứ tự `id`, chặn sửa túi đồ khi đang Trade).
   - Vá `ConsignShopService.itemCanConsign` (option 30 lên đầu), `BlackBallWarService` (`default:` switch), `WorldMartialArtsTournamentService` (hoàn đúng loại tiền), `ChuyenHoaBangNgoc`, `NangCapSachTuyetKi` (roll 10%), `Controller.isNearMobInZone` & `SkillService` (tụ lực).
2. **Ngày 3–4 (Concurrency & Database ACID)**:
   - Vá `GiftService.isUsedGiftCode()` đóng `Connection` bằng `try-with-resources`.
   - Sửa `ConsignShopManager.save()` (loại bỏ `DELETE ALL + INSERT ALL`, chuẩn hóa hàng đợi tuần tự + `rollback()`).
   - Loại bỏ spin-wait 2.5s trong `SAIBAMEN.java`; khóa `zone` đồng bộ trước `setTimeout` ở `DeathOrAliveArenaService`; bọc `synchronized(clan)` khi mở `PhoBan`; chuẩn hóa lock `zone.items`; thêm save barrier cho login/logout async.
3. **Ngày 5–6 (Protocol & Gameplay P0)**:
   - Sửa `NPoint.calPercent()` (`/ 100`), `NewSkill.java` (`== true`), `Yardart.java` (xóa `hp = 1`), `SkillService.TRI_THUONG`, `ChestBoxItemHandler`.
   - Sửa `Controller.cs` (`while (available() > 0)`), `Session_ME`/`ME2` (`ReadExactBytes`, validate `readMessage2`, lock `sendingMessage`), `SummonDragonNamek` (`writeInt(pl.id)` ở `-83`), `ConsignShopService` (lọc `null`, phân trang <= 255, đồng bộ ID).
4. **Ngày 7 (Kiểm Chứng Bắt Buộc)**:
   - Chạy `python tools/compile_check.py` (đạt 0 lỗi cả Server & Client) + `python tools/memory/guardrails.py --check-all` + test tích hợp các luồng Trade, Ký gửi, Đập đồ, Rồng Namek.

### Tuần 2: Ổn Định Hóa — Xử Lý Top P1 (DB, Hiệu Năng, Gameplay & Hygiene)
*Mục tiêu: Triệt tiêu các điểm nghẽn hiệu năng, rò rỉ bộ nhớ, sai lệch hiển thị và dọn mã bẩn.*
1. **Database & MySQL 8**:
   - Thêm `AND sotien >= ?` / `AND cash >= ?` trong `PlayerDAO.subvnd/subcash`; sửa `ResultSetImpl` (`getColumnLabel` + `LocalDateTime`); sửa `createNewPlayer()` (`"[]"`) và `extractSnapshot()` (`BoughtSkills`); sửa `Manager.queryTopSM` JOIN `p.account_id = a.id`; xóa `AlyraManager.login()`.
2. **Hiệu năng Server & Client**:
   - Chuyển ghi file của `PacketProfiler.triggerPerformanceAlert()` sang thread nền bất đồng bộ (hoặc tái sử dụng cơ chế queue như `TrajectoryLogger`).
   - Giới hạn kích thước/LRU cho `ItemOption.OPTION_STRING`, dọn `TrajectoryLogger.playerTickMap` khi logout, giới hạn số lần lặp `PhaLeHoaTrangBi.startCombineVip`.
   - Client: Cache `Color[]` trong `mFont.cs`, đưa `checkLoadMount()` / đọc `/Mob/50` ra khỏi `paint()`, gỡ bỏ `GC.Collect()` đồng bộ khi chuyển map, `Destroy` `Texture2D` cũ trong `Image.createImage`, bỏ `Thread.Sleep(5)` trong `Session_ME2`.
3. **Protocol & Gameplay P1 + Hygiene**:
   - Đồng bộ `Session_ME2` (thêm opcode `66`, `isMainSession = false`), sửa `sbyte -> int` cho XOR key, `myWriter.writeSByte`, `KeyHandler` (`ChannelFutureListener`).
   - Vá `Kaioken`, `HuytSao`, `Pet.followMaster`, `SetClothes`, Boss (`SieuBoHung`, `Drabura2`, `Cadic`, `SnakeWay`), và Client UI input (`updateKey`, phím 15, `chatVip`, nút Back đổi cờ).
   - Xóa `src/testddos/DdosTester.java`, tắt `WhisArenaService.TEST_MODE_24_7`, bảo mật `AdminApiService` (bắt buộc token cấu hình qua env, giới hạn CORS).

### Dài Hạn (Tháng 1–3): Tái Cấu Trúc Kiến Trúc (Architecture Refactoring)
1. **Hoàn Tất Chuyển Đổi Sang Dispatcher & Loại Bỏ Dead Code**:
   - Di dời toàn bộ opcode còn lại trong `Controller.java` và `Controller.cs` sang `ServerPacketDispatcher` và `PacketDispatcher`; xóa bỏ 2 khối `switch` khổng lồ cũ và stack Socket cũ (`Network.java`, `Session.java`); chuẩn hóa lại tên hằng số `NetworkOpcodes.cs`.
2. **Giải Thể God Classes & Phá Vỡ Phụ Thuộc Vòng Chéo**:
   - Tách `Panel.cs` (11.8k dòng) thành các `ISubPanel` độc lập cả logic lẫn render; tách `Char.cs` (8.7k dòng) theo ECS/Component (`CharStats`, `CharPhysics`, `CharRenderer`); tách `GameScr.cs` (8.3k dòng) và xóa bỏ trạng thái `static` rò rỉ.
   - Tái cấu trúc `Zone.java` thay `CopyOnWriteArrayList` bằng cấu trúc dữ liệu phù hợp với tần suất đọc/ghi thực tế; tách tầng Domain (`models`) khỏi `services` và `database` bằng interface/DTO để giảm 818 imports ngược chiều.
   - Kế thừa chuẩn `Mob` cho `BigBoss`, `NewBoss`, `BachTuoc` trên Client (dùng `override` thay `new`) và gộp các boss trùng lặp trên Server (`Yardart`, `XENCON1-7`).
3. **Chuẩn Hóa Testing & CI/CD**:
   - Đưa JUnit 5 (Server) và NUnit/Unity Test Runner (Client) vào pipeline, cô lập môi trường test (không ghi vào `tools/memory/PERFORMANCE_ALERTS.json` production) và tự động chạy `compile_check.py` + `guardrails.py` trên mỗi commit.

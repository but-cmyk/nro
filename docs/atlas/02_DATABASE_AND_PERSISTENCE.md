# Báo Cáo Kiểm Chứng Chéo: Database, Redis & Persistence (MySQL 8 + HikariCP + Redis)

## 1. Kiến trúc Persistence

### 1.1. Tổng quan Schema MySQL (44 bảng)
Dựa trên `docs/atlas/_index/03_database_and_redis_map.md` (parse từ `docker/initdb/01_init.sql` → `04_audit_log.sql`), hệ thống định nghĩa **44 bảng MySQL**, chia thành 4 nhóm chính:
- **Tài khoản & Nhân vật**: `account`, `player`, `super_rank`, `clan`, `history_transaction`, `admin_audit_log`.
- **Kinh tế & Ký gửi / Nạp**: `shop_ky_gui`, `shop`, `tab_shop`, `giftcode`, `payments`, `recharge`, `momo_trans`, `mbbank_log`, `napthe`, `naptien`, `trans_log`, `sell_item`.
- **Template tĩnh (nạp khi khởi động qua `DatabaseTemplateLoader`)**: `item_template`, `item_option_template`, `skill_template`, `mob_template`, `npc_template`, `map_template`, `task_main_template`, `task_sub_template`, `side_task_template`, `clan_task_template`, `achievement_template`, `intrinsic`, `radar`, `part`, `head_avatar`, `flag_bag`, `bg_item_template`, `array_head_2_frames`, `img_by_name`, `notify`.
- **Web/Event/CMS**: `event`, `adminpanel`, `comments`, `posts`, `messages`, `setting`.

### 1.2. Kết nối MySQL (HikariCP) & Tầng Wrapper (`AlyraManager` / `ResultSetImpl`)
- **`AlyraManager.java` (lines 44–58, 447–480)**: Khởi tạo một `HikariDataSource` duy nhất (`minIdle=5`, `maxPoolSize=20`, `maxLifetime=1800000ms`, `leakDetectionThreshold=30000ms`, `connectionTimeout=30000ms`), cấu hình `cachePrepStmts`, `useServerPrepStmts`, `rewriteBatchedStatements`. `sslMode` mặc định `DISABLED` cho localhost và `VERIFY_IDENTITY` cho remote.
- **Cơ chế `executeQuery` (`AlyraManager.java:150-218`)**: Mở `Connection` → `PreparedStatement` → `ResultSet`, copy toàn bộ dữ liệu vào bộ nhớ thông qua `new ResultSetImpl(rs)` (đóng `rs` và `stmt` trong `finally` của `ResultSetImpl`), sau đó đóng `rs`, `ps`, `con` ngay trước khi trả về `AlyraResultSet`.
- **Cơ chế `executeUpdate` (`AlyraManager.java:220-254`)**: Dùng `try-with-resources` cho `Connection` và `PreparedStatement`; hỗ trợ `processInsertQuery` tự bung `values ()` thành `values (?,?,...)`.

### 1.3. Redis Cache (`RedisService.java` & `redis.properties`)
- Cấu hình qua biến môi trường (`REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`) hoặc `data/config/redis.properties` (`127.0.0.1:6379`, `database=0`, `password=` rỗng).
- Dùng `JedisPool` (`maxTotal=64`, `maxIdle=32`, `minIdle=8`, `testOnBorrow=true`, `maxWait=3000ms`).
- **Keyspace hiện có**:
  - `session:user:<userId>` (`SETEX` / `GET` / `DEL`, có TTL `expireSeconds`).
  - `leaderboard:<boardName>` (`ZADD` / `ZREVRANGEWITHSCORES`, không có TTL).
- Nếu ping thất bại lúc khởi động, `isConnected = false` và các hàm trở thành no-op (fallback in-memory).

### 1.4. Vòng đời Save/Load Player & ConsignShop
- **Load Player (`NDVSqlFetcher.login` & `loadPlayer`)**:
  1. Truy vấn `account` theo `username`, kiểm tra BCrypt (`PasswordUtils.verifyPassword`), kiểm tra online duplicate, `ban`, `Manager.DEBUG`, `SECOND_WAIT_LOGIN`.
  2. Kiểm tra tồn tại nhân vật nhẹ (`SELECT id FROM player WHERE account_id = ? LIMIT 1`), sau đó mới `SELECT * FROM player WHERE account_id = ? LIMIT 1` và deserialize ~35 cột JSON/scalar vào `Player` + `SuperRankDAO.loadData(player)`.
  3. Cập nhật `account.last_time_login` và `ip_address`.
- **Save Player (`PlayerDAO.updatePlayer` & `updatePlayersInTransaction`)**:
  - `extractSnapshot(player)` khóa `synchronized (player)` để serialize toàn bộ trạng thái thành `Object[] params` (59 tham số) kèm `deepHashCode` (`lastSaveHash`) để bỏ qua ghi DB nếu dữ liệu không đổi.
  - Ghi JDBC chạy bên ngoài `synchronized (player)` (đồng bộ hoặc qua `Thread.ofVirtual()` ở `updatePlayerAsync` / `updatePlayersInTransactionAsync`). Giao dịch Trade dùng `updatePlayersInTransaction` với `autoCommit=false` + `commit()`/`rollback()`.
- **Vòng đời ConsignShop (`ConsignShopManager`)**:
  - Startup `init()` → `load()` đọc toàn bộ `shop_ky_gui LEFT JOIN player` vào `listItem` (bảo vệ bằng `listLock`) và cập nhật `nextItemId`.
  - Khi giao dịch ký gửi: vừa gọi các hàm Virtual Thread bất đồng bộ (`insertItemAsync`, `updateItemBoughtAsync`, `deleteItemAsync`, `updateItemUpTopAsync`), vừa chạy định kỳ `save()` (lấy snapshot dưới `listLock`, giữ `saveLock`, `DELETE FROM shop_ky_gui` toàn bảng rồi batch `INSERT`).

---

## 2. Bảng các lỗi DB/Redis/ACID đã kiểm chứng trực tiếp trên code

| Mức độ | Vị trí (`File:Line`) | Trạng thái | Nguyên nhân gốc rễ | Cách sửa đề xuất |
| :--- | :--- | :--- | :--- | :--- |
| **P0** | `src/managers/ConsignShopManager.java:42-129, 268-352` | **CONFIRMED** | `save()` thực hiện `DELETE FROM shop_ky_gui` toàn bảng rồi batch `INSERT` dưới `saveLock`, nhưng: (1) Các hàm Virtual Thread (`updateItemBoughtAsync`, `deleteItemAsync`, `updateItemUpTopAsync`, `insertItemAsync`) ghi SQL song song mà **không lấy `saveLock`** và không có thứ tự (delete có thể vượt insert); (2) `save()` thiếu `con.rollback()` trong `catch`; (3) Nếu `itemsToSave.isEmpty()` (line 49-52), `save()` return sớm mà không xóa DB, làm item đã hủy/mua hết hồi sinh khi restart. | Loại bỏ cơ chế `DELETE ALL + INSERT ALL` trong `save()`; chuẩn hóa mọi thao tác DB của `shop_ky_gui` đi qua một hàng đợi tuần tự (single-thread executor) hoặc dùng `saveLock` + UPSERT/delta sync, bổ sung `con.rollback()` và `setAutoCommit(true)` trong `finally`. |
| **P0** | `src/services/player/GiftService.java:148-159` | **CONFIRMED** | `AlyraManager.getConnection().prepareStatement(query)` chỉ bọc `PreparedStatement` trong `try-with-resources`, **không đóng `Connection`**. Mỗi lần gọi `isUsedGiftCode()` làm rò rỉ 1 HikariCP Connection cho tới khi cạn pool. | Tách `try (Connection con = AlyraManager.getConnection(); PreparedStatement stmt = con.prepareStatement(query))` và log lỗi thay vì `catch` rỗng. |
| **P1** | `src/database/AlyraManager.java:256-332, 310, 317` | **CONFIRMED** | Hàm `AlyraManager.login()` trùng lặp với `NDVSqlFetcher.login()`: line 310 query `SELECT id FROM player ...` nhưng line 317 truyền thẳng `rsPlayer` vào `NDVSqlFetcher.loadPlayer()` (vốn đọc ~35 cột như `head`, `data_inventory`...) gây `SQLException` thiếu cột; đồng thời bỏ qua kiểm tra `ban`, `Manager.DEBUG` (bảo trì) và không cập nhật `last_time_login`/`ip_address`. | Xóa hoặc ủy quyền `AlyraManager.login()` sang `NDVSqlFetcher.login()`. |
| **P1** | `src/database/ResultSetImpl.java:41-50, 496-541` | **CONFIRMED** | (1) Constructor dùng `rsmd.getColumnName(j)` thay vì `rsmd.getColumnLabel(j)`, làm mọi alias SQL (`AS sm`, `AS player_name`, `AS max_id`) không tra cứu được theo tên alias; (2) `rs.getObject(j)` với cột `DATETIME` trên MySQL Connector/J 8.x trả về `java.time.LocalDateTime`, khiến `getTimestamp()` ném `Exception("Cannot convert LocalDateTime to Timestamp")`. | Đổi sang `rsmd.getColumnLabel(j)` (giữ thêm `columnName` nếu cần tương thích ngược); trong `getTimestamp()`, bổ sung nhánh `if (value instanceof java.time.LocalDateTime ldt) return Timestamp.valueOf(ldt);`. |
| **P1** | `src/database/daos/NDVSqlFetcher.java:256-258, 716, 1527-1558` & `PlayerDAO.java:365, 1160` | **CONFIRMED** | (1) `NDVSqlFetcher.login()` gọi `.getTimestamp(...).getTime()` cho `last_time_login`, `last_time_logout`, `create_time` không kiểm tra `null` (dễ NPE); (2) `PlayerDAO.createNewPlayer()` ghi số `0` cho `data_achievement` và `giftcode` thay vì `"[]"`, gây `ClassCastException` (`Long` → `JSONArray`) khi load; (3) `PlayerDAO.extractSnapshot()` hardcode `boughtSkills = "[]"` (line 1160) làm mất dữ liệu kỹ năng đã mua khi save. | Thêm null-check cho `Timestamp` trong `login()`; sửa mặc định `createNewPlayer()` thành `"[]"`; serialize `player.BoughtSkills` đúng chuẩn JSONArray trong `extractSnapshot()` và parse an toàn qua `Number` trong `NDVSqlFetcher`. |
| **P1** | `src/database/daos/PlayerDAO.java:1295-1307, 1641-1674` | **CONFIRMED** | `subvnd()`, `subVND2()`, `subcash()` trừ tiền bằng `SET sotien = (sotien - ?)` / `SET cash = (cash - ?)` **không có điều kiện `AND sotien >= ?` / `AND cash >= ?`** và không kiểm tra `affectedRows > 0`, cho phép âm tiền khi có race condition. Ngoài ra `subvnd()`/`subVND2()` khai báo `PreparedStatement ps` ngoài `try-with-resources` (`subVND2` không đóng `ps`). | Dùng `UPDATE account SET sotien = sotien - ? WHERE id = ? AND sotien >= ?`, kiểm tra `ps.executeUpdate() > 0` mới trừ RAM; bọc `PreparedStatement` trong `try-with-resources`. |
| **P1** | `src/server/Manager.java:159-165` | **CONFIRMED** | `queryTopSM` viết `LEFT JOIN account a ON p.id = a.id AND a.is_admin = 1` thay vì `p.account_id = a.id`, khiến BXH Sức Mạnh không loại trừ được tài khoản Admin và loại nhầm người chơi thường có `player.id` trùng `account.id` của Admin. | Sửa điều kiện JOIN thành `LEFT JOIN account a ON p.account_id = a.id AND a.is_admin = 1`. |
| **P2** | `src/services/cache/RedisService.java:23, 97-105, 143-164` | **CONFIRMED** | `isConnected` không khai báo `volatile` và không bao giờ chuyển về `false` khi Redis ngắt kết nối lúc runtime, khiến mọi call tiếp theo bị block tối đa `3000ms` (`maxWait`) khi borrow Jedis. Các key `leaderboard:<boardName>` cũng không có TTL. | Khai báo `private volatile boolean isConnected`, thêm circuit-breaker/backoff khi `JedisConnectionException` xảy ra và cơ chế reconnect định kỳ; cân nhắc đặt TTL cho leaderboard nếu là bảng xếp hạng theo mùa/ngày. |
| **P2** | `src/database/daos/EventDAO.java:31-59` & `src/models/clan/Clan.java:323-419` & `src/services/player/ClanService.java:1065-1105` | **CONFIRMED (theo digest)** | Mở `PreparedStatement` / `ResultSet` ngoài `try-with-resources` (`EventDAO` không đóng; `Clan` có thể NPE trong `finally` nếu `prepareStatement` lỗi; `Clan.deleteDB` rò rỉ `ps` khi lỗi SQL; `ClanService.close` nuốt exception). | Chuẩn hóa 100% `Connection`, `PreparedStatement`, `ResultSet` vào `try-with-resources` và ghi log lỗi đầy đủ. |
| **FALSE_POSITIVE** *(Connection Leak)* | `src/services/func/Input.java:549, 570` & `src/services/Service.java:114-130` | **FALSE_POSITIVE** (chỉ rò rỉ tham chiếu RAM nhỏ) | Digest cảnh báo bỏ qua `rs.dispose()` gây rò rỉ HikariCP Connection. **Thực tế kiểm chứng** trên `AlyraManager.java:167-172, 203-208` và `ResultSetImpl.java:62-81` cho thấy `AlyraManager.executeQuery()` đã đọc hết dữ liệu vào RAM và **đóng `rs`, `ps`, `con` ngay trước khi trả về**. `dispose()` chỉ xóa mảng `data`/`values` trong bộ nhớ Heap. | Có thể gọi `rs.dispose()` trong `finally` hoặc cho `AlyraResultSet` kế thừa `AutoCloseable` để giải phóng Heap sớm, nhưng **không phải lỗi leak HikariCP Connection**. |

---

## 3. Đánh giá tổng thể khía cạnh Database & Persistence

- **Điểm đánh giá**: **5.5 / 10**

### Điểm mạnh
1. **Chống SQL Injection tốt**: Toàn bộ các truy vấn kiểm tra đều sử dụng `PreparedStatement` với tham số `?` (kể cả `AlyraManager.executeQuery`/`executeUpdate` với `setParameters`).
2. **Connection Pool chuẩn & tối ưu**: `HikariCP` được cấu hình bài bản (`leakDetectionThreshold`, `cachePrepStmts`, `rewriteBatchedStatements`, `useServerPrepStmts`).
3. **Cơ chế Snapshot & Dirty-Check (`PlayerDAO`)**: `extractSnapshot()` tách việc serialize khỏi JDBC I/O (không giữ lock `player` khi chờ DB) và dùng `lastSaveHash` để tránh ghi trùng lặp khi dữ liệu không thay đổi; Trade sử dụng transaction `updatePlayersInTransaction` có `rollback()`.
4. **Redis Fault-Tolerant lúc khởi động**: `RedisService` có fallback in-memory nếu Redis chưa bật lúc boot server.

### Điểm yếu
1. **Rủi ro mất/lệch dữ liệu ACID & Race Condition**: `ConsignShopManager.save()` (`DELETE ALL + INSERT ALL`) xung đột trực tiếp với các luồng Virtual Thread bất đồng bộ; `subvnd`/`subcash` không kiểm tra số dư ở mức SQL (`WHERE sotien >= ?`); nhiều thao tác offline (`BlackBallWar`, `SuperRank`, `Input`) đọc-sửa-ghi không có lock/transaction.
2. **Bất đồng bộ chuẩn MySQL 8 & Wrapper cũ**: `ResultSetImpl` dùng `getColumnName` (làm hỏng alias) và chưa hỗ trợ `LocalDateTime` của MySQL 8; nhiều câu SQL vẫn thiếu dấu backtick `` ` `` cho từ khóa như `rank` hoặc tên bảng/cột.
3. **Rò rỉ tài nguyên JDBC cục bộ**: Vẫn tồn tại lỗi leak `Connection` nghiêm trọng ở `GiftService.isUsedGiftCode` và leak `PreparedStatement`/`ResultSet` ở các DAO cũ (`EventDAO`, `Clan`, `ClanService`, `PlayerDAO.subvnd/subVND2`).
4. **Dữ liệu mặc định & Dead-code nguy hiểm**: `AlyraManager.login()` tồn tại song song với `NDVSqlFetcher.login()` nhưng bị hỏng logic; `PlayerDAO.createNewPlayer()` ghi `"0"` vào cột JSON array; `PlayerDAO.extractSnapshot()` hardcode `boughtSkills = "[]"`.

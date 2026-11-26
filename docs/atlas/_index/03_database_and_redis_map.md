# NRO Database Schema & Redis Map
- SQL files parsed: `docker/initdb/01_init.sql, docker/initdb/02_nro_data.sql, docker/initdb/03_optimize_db.sql, docker/initdb/04_audit_log.sql`
- Tables defined: `44`

## Tables & Java References
| Table | Engine | Cols | Referenced By Java Files |
| :--- | :--- | ---: | :--- |
| `account` | unknown | 2 | `AlyraManager.java`, `NDVSqlFetcher.java`, `PlayerDAO.java`, `Client.java`, `Manager.java`, `ServerManager.java` (+3) |
| `achievement_template` | unknown | 5 | `DatabaseTemplateLoader.java` |
| `admin_audit_log` | unknown | 2 | `AuditLogService.java` |
| `adminpanel` | unknown | 2 | - |
| `array_head_2_frames` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `bg_item_template` | unknown | 5 | `DatabaseTemplateLoader.java` |
| `clan` | unknown | 2 | `DatabaseTemplateLoader.java`, `Clan.java`, `Manager.java`, `ClanService.java` |
| `clan_task_template` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `comments` | unknown | 3 | - |
| `event` | unknown | 3 | `EventDAO.java` |
| `flag_bag` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `giftcode` | unknown | 7 | `DatabaseTemplateLoader.java`, `GiftCodeManager.java`, `AdminApiService.java` |
| `head_avatar` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `history_transaction` | unknown | 2 | `HistoryTransactionDAO.java` |
| `img_by_name` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `intrinsic` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `item_option_template` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `item_template` | unknown | 4 | `DatabaseTemplateLoader.java` |
| `map_template` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `mbbank_log` | unknown | 2 | - |
| `messages` | unknown | 2 | - |
| `mob_template` | unknown | 3 | `DatabaseTemplateLoader.java` |
| `momo_trans` | unknown | 2 | - |
| `napthe` | unknown | 2 | - |
| `naptien` | unknown | 12 | - |
| `notify` | unknown | 3 | `DatabaseTemplateLoader.java` |
| `npc_template` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `part` | unknown | 3 | `DatabaseTemplateLoader.java` |
| `payments` | unknown | 2 | - |
| `player` | unknown | 3 | `AlyraManager.java`, `NDVSqlFetcher.java`, `PlayerDAO.java`, `SuperRankDAO.java`, `TraningDAO.java`, `ConsignShopManager.java` (+9) |
| `posts` | unknown | 2 | - |
| `radar` | unknown | 7 | `DatabaseTemplateLoader.java` |
| `recharge` | unknown | 2 | - |
| `sell_item` | unknown | 8 | - |
| `setting` | unknown | 2 | - |
| `shop` | unknown | 3 | `ShopDAO.java` |
| `shop_ky_gui` | unknown | 8 | `DatabaseTemplateLoader.java`, `ConsignShopManager.java`, `AdminApiService.java`, `ConsignShopService.java` |
| `side_task_template` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `skill_template` | unknown | 3 | `DatabaseTemplateLoader.java` |
| `super_rank` | unknown | 12 | `SuperRankDAO.java` |
| `tab_shop` | unknown | 3 | `ShopDAO.java` |
| `task_main_template` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `task_sub_template` | unknown | 2 | `DatabaseTemplateLoader.java` |
| `trans_log` | unknown | 10 | - |

## JDBC & Redis Hotspots in `src/`
| Java File | `getConnection` | `try(...)` | `executeQuery` | `executeUpdate` | Redis Refs |
| :--- | ---: | ---: | ---: | ---: | ---: |
| `src/database/AlyraManager.java` | 8 | 2 | 6 | 4 | 0 |
| `src/database/daos/EventDAO.java` | 2 | 2 | 1 | 1 | 0 |
| `src/database/daos/HistoryTransactionDAO.java` | 2 | 2 | 1 | 3 | 0 |
| `src/database/daos/NDVSqlFetcher.java` | 0 | 0 | 5 | 1 | 0 |
| `src/database/daos/PlayerDAO.java` | 13 | 13 | 2 | 17 | 0 |
| `src/database/daos/ShopDAO.java` | 0 | 0 | 3 | 0 | 0 |
| `src/database/daos/SuperRankDAO.java` | 0 | 0 | 7 | 3 | 0 |
| `src/database/daos/TraningDAO.java` | 0 | 0 | 0 | 1 | 0 |
| `src/database/loaders/DatabaseTemplateLoader.java` | 25 | 25 | 25 | 0 | 0 |
| `src/managers/ConsignShopManager.java` | 7 | 7 | 1 | 5 | 0 |
| `src/managers/GiftCodeManager.java` | 3 | 3 | 1 | 3 | 0 |
| `src/models/clan/Clan.java` | 3 | 3 | 0 | 3 | 0 |
| `src/models/tournament/whis_arena/WhisArenaService.java` | 3 | 3 | 1 | 2 | 0 |
| `src/server/Client.java` | 0 | 0 | 0 | 1 | 0 |
| `src/server/Controller.java` | 0 | 0 | 1 | 0 | 0 |
| `src/server/Manager.java` | 0 | 0 | 1 | 0 | 0 |
| `src/server/ServerManager.java` | 0 | 0 | 1 | 1 | 0 |
| `src/services/ConsignShopService.java` | 2 | 2 | 1 | 1 | 0 |
| `src/services/Service.java` | 0 | 0 | 1 | 2 | 0 |
| `src/services/TopService.java` | 1 | 1 | 0 | 0 | 0 |
| `src/services/admin/AdminApiService.java` | 1 | 1 | 1 | 1 | 0 |
| `src/services/admin/AdminService.java` | 3 | 3 | 0 | 4 | 0 |
| `src/services/admin/AuditLogService.java` | 4 | 4 | 2 | 2 | 0 |
| `src/services/cache/RedisService.java` | 0 | 0 | 0 | 0 | 23 |
| `src/services/func/Input.java` | 0 | 0 | 2 | 2 | 0 |
| `src/services/func/TransactionService.java` | 1 | 1 | 0 | 0 | 0 |
| `src/services/player/ClanService.java` | 2 | 2 | 0 | 1 | 0 |
| `src/services/player/GiftService.java` | 3 | 3 | 2 | 1 | 0 |
| `src/services/player/PlayerService.java` | 0 | 0 | 0 | 1 | 0 |
| `src/services/tournament/DeathOrAliveArenaWeeklyService.java` | 2 | 2 | 1 | 1 | 0 |
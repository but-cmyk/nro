-- ===================================================================
-- SCRIPT TỐI ƯU HÓA DATABASE & COLLATION (NRO_DATA)
-- Tự động thực thi sau 02_nro_data.sql trong docker-entrypoint-initdb.d
-- ===================================================================

USE `nro_data`;

-- 1. Chuẩn hóa 100% cột utf8mb3 sang utf8mb4_unicode_ci trong bảng player
ALTER TABLE `player` 
  MODIFY `notify` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  MODIFY `baovetaikhoan` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '[]';

-- 2. Thêm B-Tree Index cho history_transaction(time_tran) tối ưu hóa truy vấn dọn dẹp log (deleteHistory)
ALTER TABLE `history_transaction` 
  ADD INDEX `idx_time_tran` (`time_tran`) USING BTREE;

-- 3. Bổ sung cột trade_id UUID và UNIQUE INDEX chống dupe / idempotency cho giao dịch Trade (Nhóm B1)
ALTER TABLE `history_transaction`
  ADD COLUMN `trade_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL AFTER `id`,
  ADD UNIQUE KEY `uk_trade_id` (`trade_id`);

-- 4. Bảng Audit Admin ghi nhận toàn bộ thao tác nhạy cảm (A5)
CREATE TABLE IF NOT EXISTS `admin_audit` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `actor` VARCHAR(64) NOT NULL,
  `action` VARCHAR(64) NOT NULL,
  `target` VARCHAR(64),
  `detail` JSON,
  `ip` VARCHAR(45),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_actor_time` (`actor`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

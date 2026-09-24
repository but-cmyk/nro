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

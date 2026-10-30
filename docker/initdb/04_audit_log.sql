-- ===================================================================
-- ADMIN AUDIT LOG TABLE (LIVE OPS & AUDIT TRAIL)
-- Tự động khởi tạo trong docker-entrypoint-initdb.d
-- ===================================================================

USE `nro_data`;

CREATE TABLE IF NOT EXISTS `admin_audit_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `admin_name` VARCHAR(50) NOT NULL,
  `action` VARCHAR(50) NOT NULL,
  `target_name` VARCHAR(50) NULL,
  `detail` TEXT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_admin` (`admin_name`),
  INDEX `idx_target` (`target_name`),
  INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


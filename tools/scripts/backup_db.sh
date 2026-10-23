#!/usr/bin/env bash
# ==============================================================================
# Script tự động sao lưu dữ liệu MySQL định kỳ cho NRO Game Server (Nhóm F)
# Cách chạy định kỳ (cron):
#   0 3 * * * /path/to/tools/scripts/backup_db.sh >> /var/log/nro_backup.log 2>&1
# ==============================================================================

set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/nro_data_${TIMESTAMP}.sql.gz"
RETENTION_DAYS=7

mkdir -p "${BACKUP_DIR}"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Bắt đầu quy trình sao lưu database nro_data..."

# Kiểm tra nếu đang chạy MySQL trong Docker container
if docker ps --format '{{.Names}}' | grep -q "^nro_mysql$"; then
    echo ">> Phát hiện container nro_mysql đang chạy, thực hiện dump qua Docker..."
    docker exec nro_mysql mysqldump \
        --single-transaction \
        --quick \
        --skip-lock-tables \
        -uroot -proot \
        nro_data | gzip > "${BACKUP_FILE}"
else
    DB_USER="${DATABASE_USER:-root}"
    DB_PASS="${DATABASE_PASS:-root}"
    DB_HOST="${DATABASE_HOST:-127.0.0.1}"
    DB_PORT="${DATABASE_PORT:-3308}"
    DB_NAME="${DATABASE_NAME:-nro_data}"

    echo ">> Thực hiện mysqldump trực tiếp tới ${DB_HOST}:${DB_PORT}..."
    mysqldump \
        --host="${DB_HOST}" \
        --port="${DB_PORT}" \
        --user="${DB_USER}" \
        --password="${DB_PASS}" \
        --single-transaction \
        --quick \
        --skip-lock-tables \
        "${DB_NAME}" | gzip > "${BACKUP_FILE}"
fi

FILE_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
echo "[$(date '+%Y-%m-%d %H:%M:%S')] [SUCCESS] Sao lưu thành công: ${BACKUP_FILE} (Dung lượng: ${FILE_SIZE})"

# Dọn dẹp các bản backup cũ hơn RETENTION_DAYS
echo ">> Kiểm tra và dọn dẹp các bản sao lưu cũ hơn ${RETENTION_DAYS} ngày..."
find "${BACKUP_DIR}" -name "nro_data_*.sql.gz" -type f -mtime "+${RETENTION_DAYS}" -exec rm -f {} +
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Hoàn tất chu kỳ sao lưu an toàn."

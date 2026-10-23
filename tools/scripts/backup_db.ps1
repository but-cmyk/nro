# ==============================================================================
# Script tu dong sao luu du lieu MySQL dinh ky cho NRO Game Server tren Windows (Nhom F)
# ==============================================================================

param(
    [string]$BackupDir = ".\backups",
    [int]$RetentionDays = 7
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = Join-Path $BackupDir "nro_data_$Timestamp.sql"
$GzipFile = "$BackupFile.gz"

Write-Host "[$((Get-Date).ToString('yyyy-MM-dd HH:mm:ss'))] Bat dau quy trinh sao luu database nro_data..." -ForegroundColor Cyan

# Kiem tra container nro_mysql
$runningContainers = docker ps --format '{{.Names}}'
if ($runningContainers -match "nro_mysql") {
    Write-Host ">> Phat hien container nro_mysql dang chay, thuc hien dump qua Docker..." -ForegroundColor Yellow
    docker exec nro_mysql mysqldump --single-transaction --quick --skip-lock-tables -uroot -proot nro_data > $BackupFile
} else {
    Write-Host ">> Thuc hien mysqldump truc tiep..." -ForegroundColor Yellow
    mysqldump --host=127.0.0.1 --port=3308 --user=root --password=root --single-transaction --quick --skip-lock-tables nro_data > $BackupFile
}

if (Test-Path $BackupFile) {
    # Nen gzip bang .NET de tiet kiem dung luong
    $srcStream = [System.IO.File]::OpenRead($BackupFile)
    $destFile = [System.IO.File]::Create($GzipFile)
    $gzipStream = [System.IO.Compression.GZipStream]::new($destFile, [System.IO.Compression.CompressionMode]::Compress)
    $srcStream.CopyTo($gzipStream)
    $gzipStream.Dispose()
    $destFile.Dispose()
    $srcStream.Dispose()
    Remove-Item $BackupFile -Force

    $size = (Get-Item $GzipFile).Length / 1MB
    Write-Host "[$((Get-Date).ToString('yyyy-MM-dd HH:mm:ss'))] [SUCCESS] Sao luu thanh cong: $GzipFile ($([Math]::Round($size, 2)) MB)" -ForegroundColor Green
}

# Don dep cac ban backup cu
$cutoff = (Get-Date).AddDays(-$RetentionDays)
Get-ChildItem -Path $BackupDir -Filter "nro_data_*.sql.gz" | Where-Object { $_.LastWriteTime -lt $cutoff } | Remove-Item -Force
Write-Host "[$((Get-Date).ToString('yyyy-MM-dd HH:mm:ss'))] Hoan tat chu ky sao luu an toan." -ForegroundColor Cyan

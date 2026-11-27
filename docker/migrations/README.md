# Database Schema Migrations (NRO_219)

Thư mục này lưu trữ các script nâng cấp lược đồ MySQL 8 theo thứ tự phiên bản (`V1__...`, `V2__...`).

## Cách áp dụng
- Trên Docker: sao chép hoặc mount vào `./docker/initdb/` cho DB mới, hoặc chạy trực tiếp qua MySQL client trên DB đang vận hành:
  ```bash
  mysql -u root -p nro_data < docker/migrations/V1__optimize_indices_and_utf8mb4.sql
  ```

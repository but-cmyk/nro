# Ngọc Rồng Online (NRO) - Game Server & Client

Repository chứa toàn bộ mã nguồn máy chủ (Server Netty Java) và Client (Unity C#) trò chơi Chú Bé Rồng Online (Dragon Boy / NRO).

## Cấu Trúc Dự Án
- `src/`: Mã nguồn máy chủ Game Server (Java, Netty 4.x, HikariCP, MySQL).
- `Client/`: Unity C# Client Project (phiên bản tối ưu hóa Single-Client).
- `data/`: Cấu hình game, dữ liệu bản đồ (`tile_map_data`), cấu hình server (`alyra.properties`).
- `docker/`: File Docker Compose và cơ sở dữ liệu khởi tạo (`02_nro_data.sql`).
- `tools/`: Công cụ kiểm tra, bảo trì hệ thống và fix encoding UTF-8 cơ sở dữ liệu.
- `build.bat`: Script tự động biên dịch toàn bộ source code Server thành `dist/NROK.jar`.
- `run.bat`: Script khởi chạy Game Server.

## Yêu Cầu Môi Trường
- **Java**: JDK 21 LTS (bắt buộc; build.bat dùng `--release 21`).
- **Cơ sở dữ liệu**: MySQL 8.0+ (hoặc dùng Docker Compose tại cổng 3308).
- **Client**: Unity Editor hỗ trợ C# .NET.

> **Bảo mật trước khi chạy thật:** copy `.env.example` → `.env`, đặt mật khẩu mạnh;
> copy `data/config/alyra.properties.example` → `data/config/alyra.properties`.
> Không commit hai file này (đã có trong `.gitignore`).

## Hướng Dẫn Khởi Chạy Nhanh
1. **Khởi động Database qua Docker**:
   ```bash
   docker-compose up -d
   ```
2. **Biên dịch Server**:
   Chạy file `build.bat` trên Windows:
   ```cmd
   build.bat
   ```
3. **Khởi động Server**:
   Chạy file `run.bat`:
   ```cmd
   run.bat
   ```

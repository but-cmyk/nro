# TLS certificates

> Không bao giờ commit private key (`*.key`, `*.pem`). Thư mục này đã ignore các file đó.

## 1. Production (khuyến nghị)

Dùng cert của domain thật (Let's Encrypt / Certbot):

```bash
certbot certonly --standalone -d nro.example.com
cp /etc/letsencrypt/live/nro.example.com/fullchain.pem certs/fullchain.pem
cp /etc/letsencrypt/live/nro.example.com/privkey.pem  certs/privkey.pem
```

Bật trong `data/config/alyra.properties`:

```properties
server.tls.enabled=true
server.tls.cert=certs/fullchain.pem
server.tls.key=certs/privkey.pem
```

## 2. Dev / self-signed

OpenSSL 1.0.2 không có `-addext`, dùng file cấu hình:

```ini
# certs/dev-openssl.cnf
[req]
distinguished_name = dn
x509_extensions = v3
prompt = no
[dn]
CN = localhost
[v3]
subjectAltName = DNS:localhost,IP:127.0.0.1
```

```bash
openssl req -x509 -newkey rsa:2048 -nodes -days 365 \
  -keyout certs/dev-key.pem -out certs/dev-cert.pem \
  -config certs/dev-openssl.cnf
```

Trỏ config tạm:

```properties
server.tls.enabled=true
server.tls.cert=certs/dev-cert.pem
server.tls.key=certs/dev-key.pem
```

## 3. Tính pin SHA-256 cho client

### Cách A: Pin Public Key (Khuyến nghị — không bị đổi khi renew cert cùng key pair)
```bash
openssl x509 -in certs/fullchain.pem -pubkey -noout \
  | openssl rsa -pubin -outform DER 2>/dev/null \
  | openssl dgst -sha256 -binary \
  | openssl base64 -A
```

### Cách B: Pin toàn bộ DER certificate
```bash
openssl x509 -in certs/fullchain.pem -outform DER \
  | openssl dgst -sha256 -binary \
  | openssl base64 -A
```

Copy chuỗi base64 vào `Client/Client/Assets/Scripts/Game1/TlsConfig.cs` (`PinnedSha256`).
Client hỗ trợ cả 2 cách: nếu khớp với 1 trong 2 hash, kết nối được chấp thuận.

## 4. Kịch Bản Rollout (Dual-Port Transition)

1. **Giai đoạn chuyển tiếp (30 ngày)**:
   - Trong `data/config/alyra.properties`:
     ```properties
     server.port=14445
     server.tls.enabled=true
     server.tls.port=14446
     server.tls.cert=certs/fullchain.pem
     server.tls.key=certs/privkey.pem
     ```
   - Server lắng nghe song song:
     - Cổng `14445`: Plaintext cho client cũ (khi đăng nhập sẽ nhận thông báo yêu cầu tải bản mới).
     - Cổng `14446`: TLS 1.2/1.3 cho client mới đã cấu hình TLS.
   - Phát hành client mới mặc định trỏ cổng 14446.
2. **Giai đoạn chính thức**:
   - Tắt cổng plaintext: đặt `server.port=14446` (hoặc chuyển hẳn 14445 sang TLS bằng `server.tls.port=0` và `server.tls.enabled=true`).
   - Mọi kết nối plaintext bị từ chối 100%.

## 5. Lưu ý

- Server dùng `SslProvider.JDK` (TLS 1.2/1.3), không phụ thuộc OpenSSL native.
- Client Unity/Mono dùng `SslProtocols.Tls12` để tương thích rộng trên Windows/Android/iOS.
- Không dùng `AllowUntrustedForDev=true` trên production.
- Để tạo nhanh chứng chỉ dev local, chạy: `pwsh tools/security/gen_dev_cert.ps1`.

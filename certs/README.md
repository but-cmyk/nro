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

```bash
openssl x509 -in certs/fullchain.pem -outform DER \
  | openssl dgst -sha256 -binary \
  | openssl base64 -A
```

Copy chuỗi base64 vào `Client/Client/Assets/Scripts/Game1/TlsConfig.cs`
(`PinnedSha256`). Khi renew cert, thêm pin mới vào mảng trước, giữ pin cũ
30 ngày để client cũ còn kết nối được.

## 4. Rollout

1. Phát hành client có TLS + pin (client mới).
2. Bật `server.tls.enabled=true`, restart server.
3. Client cũ sẽ không kết nối được — thông báo bắt buộc cập nhật.
4. Nếu cần rollback: đặt `server.tls.enabled=false` và phát hành lại client cũ.

## 5. Lưu ý

- Server dùng `SslProvider.JDK` (TLS 1.2/1.3), không phụ thuộc OpenSSL native.
- Client Unity/Mono dùng `SslProtocols.Tls12` để tương thích rộng.
- Pin là SHA-256 của toàn bộ DER certificate (không phải SPKI). Renew → đổi pin.
- Không dùng `AllowUntrustedForDev=true` trên production.

namespace Game1
{
    /// <summary>
    /// TLS configuration for the game socket.
    ///
    /// After generating/renewing a certificate, compute its DER SHA-256
    /// or Public Key SHA-256 (see certs/README.md or run tools/security/gen_dev_cert.ps1)
    /// and put the base64 value into PinnedSha256.
    /// Never enable AllowUntrustedForDev on a production build.
    /// </summary>
    public static class TlsConfig
    {
        /// <summary>Bật TLS cho mọi kết nối game (server phải bật server.tls.enabled hoặc server.tls.port).</summary>
        public static readonly bool Enabled = true;

        /// <summary>SNI/hostname dùng cho handshake (domain của server hoặc để rỗng nếu dùng IP trực tiếp).</summary>
        public static readonly string ServerName = "nro.example.com";

        /// <summary>
        /// Danh sách pin SHA-256 (base64) của DER certificate HOẶC Public Key.
        /// Chấp nhận cả 2 định dạng:
        ///   1. Public Key SHA-256: không đổi khi renew cert cùng key pair (khuyến nghị).
        ///   2. DER Cert SHA-256: giữ 2 pin trong giai đoạn renew 30 ngày.
        /// </summary>
        public static readonly string[] PinnedSha256 = new string[]
        {
            "REPLACE_WITH_BASE64_SHA256_OF_CERT_OR_PUBKEY"
        };

        /// <summary>CHỈ dùng cho dev local. Production phải để false.</summary>
        public static readonly bool AllowUntrustedForDev = false;
    }
}

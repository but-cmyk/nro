namespace Game1
{
    /// <summary>
    /// TLS configuration for the game socket.
    ///
    /// After generating/renewing a certificate, compute its DER SHA-256
    /// (see certs/README.md) and put the base64 value into PinnedSha256.
    /// Never enable AllowUntrustedForDev on a production build.
    /// </summary>
    public static class TlsConfig
    {
        /// <summary>Bật TLS cho mọi kết nối game (server phải bật server.tls.enabled).</summary>
        public static readonly bool Enabled = true;

        /// <summary>SNI/hostname dùng cho handshake (domain của server).</summary>
        public static readonly string ServerName = "nro.example.com";

        /// <summary>
        /// Danh sách pin SHA-256 (base64) của DER certificate.
        /// Giữ 2 pin trong giai đoạn renew để client cũ vẫn kết nối được.
        /// </summary>
        public static readonly string[] PinnedSha256 = new string[]
        {
            "REPLACE_WITH_BASE64_SHA256_OF_CERT"
        };

        /// <summary>CHỈ dùng cho dev local. Production phải để false.</summary>
        public static readonly bool AllowUntrustedForDev = false;
    }
}

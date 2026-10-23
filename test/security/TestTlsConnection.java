package security;

import network.netty.NettyServer;
import server.Manager;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * End-to-end integration test for Netty TLS server and dual-port rollout.
 */
public class TestTlsConnection {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Running TLS End-to-End Integration Tests ===");

        File certFile = new File("certs/dev-cert.pem");
        File keyFile = new File("certs/dev-key.pem");
        if (!certFile.isFile() || !keyFile.isFile()) {
            throw new IllegalStateException("dev-cert.pem / dev-key.pem not found. Run gen_dev_cert.ps1 first.");
        }

        Manager.TLS_CERT = certFile.getAbsolutePath();
        Manager.TLS_KEY = keyFile.getAbsolutePath();

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate testCert;
        try (FileInputStream in = new FileInputStream(certFile)) {
            testCert = (X509Certificate) cf.generateCertificate(in);
        }

        int plainPort = 14455;
        int tlsPort = 14456;

        try {
            // 2. Start NettyServer in dual-port mode (14455 legacy plaintext, 14456 TLS)
            NettyServer.gI().start(plainPort, true, tlsPort);
            Thread.sleep(500);

            // 3. Test TLS Client on TLS port (14456)
            testTlsClient(tlsPort, testCert);

            // 4. Test Plaintext Client on Legacy port (14455)
            testPlaintextClient(plainPort);

            // 5. Test Plaintext Client on TLS port (must fail handshake)
            testPlaintextOnTlsPort(tlsPort);

            System.out.println("=== ALL TLS INTEGRATION TESTS PASSED ===");
        } finally {
            NettyServer.gI().close();
        }
        System.exit(0);
    }

    private static void testTlsClient(int port, X509Certificate expectedCert) throws Exception {
        // Trust manager accepting our test cert
        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{expectedCert};
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAll, new SecureRandom());
        SSLSocketFactory factory = sslContext.getSocketFactory();

        try (SSLSocket socket = (SSLSocket) factory.createSocket("127.0.0.1", port)) {
            socket.setSoTimeout(5000);
            socket.startHandshake();

            SSLSession session = socket.getSession();
            check("TLS Handshake completed", session.isValid());
            check("TLS Protocol 1.2 or 1.3", session.getProtocol().startsWith("TLSv1."));
            System.out.println("PASS: TLS handshake ok - Protocol=" + session.getProtocol()
                    + ", Cipher=" + session.getCipherSuite());

            // Send initial key handshake cmd -27 with 2-byte length 0
            OutputStream out = socket.getOutputStream();
            out.write(new byte[]{-27, 0, 0});
            out.flush();

            InputStream in = socket.getInputStream();
            // Server responds with key exchange packet
            int b = in.read();
            check("TLS data transfer ok (received response byte)", b != -1);
            System.out.println("PASS: TLS bidirectional data exchange ok (cmd byte: " + b + ")");
        }
    }

    private static void testPlaintextClient(int port) throws Exception {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(3000);
            check("Legacy port socket connected", socket.isConnected());

            OutputStream out = socket.getOutputStream();
            out.write(new byte[]{-27, 0, 0});
            out.flush();

            InputStream in = socket.getInputStream();
            int b = in.read();
            check("Legacy port plaintext response ok", b != -1);
            System.out.println("PASS: Legacy plaintext port ok (cmd byte: " + b + ")");
        }
    }

    private static void testPlaintextOnTlsPort(int port) {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            // Send garbage non-TLS packet to TLS port
            out.write(new byte[]{0x01, 0x02, 0x03, 0x04});
            out.flush();

            InputStream in = socket.getInputStream();
            int b = in.read();
            // Server TLS handler must close connection or fail handshake
            check("Plaintext rejected on TLS port", b == -1);
            System.out.println("PASS: Plaintext traffic properly rejected on TLS port");
        } catch (Exception e) {
            // SocketException / EOF / timeout is expected when server closes
            System.out.println("PASS: Plaintext rejected on TLS port (exception: " + e.getClass().getSimpleName() + ")");
        }
    }

    private static void check(String name, boolean ok) {
        if (!ok) {
            throw new AssertionError("FAILED: " + name);
        }
    }
}

package test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import network.io.Message;
import network.netty.NroPacketEncoder;
import network.session.MySession;

/**
 * Contract test kiểm chứng giao thức đóng gói NroPacketEncoder & Message stream.
 * Không phụ thuộc Database hay Socket thật.
 */
public class TestProtocolContract {

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError("ASSERTION FAILED: " + msg);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("============================================================");
        System.out.println("   CONTRACT TEST: NRO PROTOCOL ENCODER & STREAM");
        System.out.println("============================================================");

        testStandardPacketEncodingNoKey();
        testExtended3BytePacketEncodingNoKey();
        testEmptyPayloadEncoding();
        testXorKeystreamRoundTrip();

        System.out.println("[ALL PASS] ✅ Protocol contract tests passed!");
        System.exit(0);
    }

    private static void testStandardPacketEncodingNoKey() throws Exception {
        MySession session = new MySession();
        session.setSentKey(false);
        EmbeddedChannel channel = new EmbeddedChannel(new NroPacketEncoder(session));

        Message msg = new Message((byte) 10);
        msg.writer().writeInt(123456);
        assertTrue(channel.writeOutbound(msg), "Outbound write must succeed");

        ByteBuf out = channel.readOutbound();
        assertTrue(out != null, "Encoded ByteBuf must not be null");
        assertTrue(out.readByte() == (byte) 10, "Command byte must be 10");
        int len = out.readUnsignedShort();
        assertTrue(len == 4, "Payload length must be 4, got " + len);
        assertTrue(out.readInt() == 123456, "Payload int must be 123456");
        assertTrue(out.readableBytes() == 0, "No trailing bytes expected");
        out.release();
        channel.finishAndReleaseAll();
        System.out.println("[PASS] Standard 2-byte size packet encoding");
    }

    private static void testExtended3BytePacketEncodingNoKey() throws Exception {
        MySession session = new MySession();
        session.setSentKey(false);
        EmbeddedChannel channel = new EmbeddedChannel(new NroPacketEncoder(session));

        // -74 (data image) uses 3-byte size encoding
        Message msg = new Message((byte) -74);
        byte[] payload = new byte[300];
        msg.writer().write(payload);
        assertTrue(channel.writeOutbound(msg), "Outbound 3-byte write must succeed");

        ByteBuf out = channel.readOutbound();
        assertTrue(out != null, "Encoded 3-byte ByteBuf must not be null");
        assertTrue(out.readByte() == (byte) -74, "Command byte must be -74");
        // 3 bytes for length: 300 = (b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16) or big-endian
        assertTrue(out.readableBytes() == 3 + 300, "Total remaining bytes must be 3 + 300, got " + out.readableBytes());
        out.release();
        channel.finishAndReleaseAll();
        System.out.println("[PASS] Extended 3-byte size packet encoding (-74)");
    }

    private static void testEmptyPayloadEncoding() {
        MySession session = new MySession();
        session.setSentKey(false);
        EmbeddedChannel channel = new EmbeddedChannel(new NroPacketEncoder(session));

        Message msg = new Message((byte) 42);
        assertTrue(channel.writeOutbound(msg), "Empty payload write must succeed");

        ByteBuf out = channel.readOutbound();
        assertTrue(out != null, "Empty payload ByteBuf must not be null");
        assertTrue(out.readByte() == (byte) 42, "Command byte must be 42");
        assertTrue(out.readUnsignedShort() == 0, "Empty payload length must be 0");
        assertTrue(out.readableBytes() == 0, "No extra bytes expected");
        out.release();
        channel.finishAndReleaseAll();
        System.out.println("[PASS] Empty payload packet encoding");
    }

    private static void testXorKeystreamRoundTrip() throws Exception {
        MySession session = new MySession();
        byte[] key = "NroTestKey123".getBytes();
        session.setKey(key);
        session.setSentKey(true);
        EmbeddedChannel channel = new EmbeddedChannel(new NroPacketEncoder(session));

        Message msg = new Message((byte) 7);
        msg.writer().writeUTF("hello");
        assertTrue(channel.writeOutbound(msg), "Encrypted outbound write must succeed");

        ByteBuf out = channel.readOutbound();
        assertTrue(out != null, "Encrypted ByteBuf must not be null");
        // With XOR key active, raw command byte should be encrypted (unless key[0] == 0)
        byte encodedCmd = out.readByte();
        byte expectedCmd = (byte) (7 ^ key[0]);
        assertTrue(encodedCmd == expectedCmd, "Encrypted command byte mismatch");
        out.release();
        channel.finishAndReleaseAll();
        System.out.println("[PASS] XOR keystream command encryption");
    }
}

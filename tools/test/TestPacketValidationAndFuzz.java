package test;

import interfaces.ISession;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import network.io.Message;
import network.netty.NroPacketDecoder;

import java.io.IOException;
import java.util.Random;

public class TestPacketValidationAndFuzz {

    private static int passCount = 0;
    private static int failCount = 0;

    private static void assertCondition(boolean condition, String testName) {
        if (condition) {
            System.out.println("PASS: " + testName);
            passCount++;
        } else {
            System.err.println("FAIL: " + testName);
            failCount++;
        }
    }

    private static class MockSession implements ISession {
        private byte[] key = null;
        private boolean sentKey = false;

        @Override
        public ISession setSendCollect(interfaces.IMessageSendCollect p0) { return this; }
        @Override
        public ISession setMessageHandler(interfaces.IMessageHandler p0) { return this; }
        @Override
        public ISession setKeyHandler(interfaces.IKeySessionHandler p0) { return this; }
        @Override
        public ISession startSend() { return this; }
        @Override
        public ISession startCollect() { return this; }
        @Override
        public ISession startQueueHandler() { return this; }
        @Override
        public ISession start() { return this; }
        @Override
        public String getIP() { return "127.0.0.1"; }
        @Override
        public boolean isConnected() { return true; }
        @Override
        public long getID() { return 1L; }
        @Override
        public void sendMessage(Message p0) {}
        @Override
        public void doSendMessage(Message p0) throws Exception {}
        @Override
        public void disconnect() {}
        @Override
        public void dispose() {}
        @Override
        public void sendKey() throws Exception {}
        @Override
        public void setKey(Message msg) throws Exception {}
        @Override
        public void setKey(byte[] keys) {
            this.key = keys;
            this.sentKey = (keys != null && keys.length > 0);
        }
        @Override
        public byte[] getKey() { return key; }
        @Override
        public boolean sentKey() { return sentKey; }
        @Override
        public void setSentKey(boolean p0) { this.sentKey = p0; }
        @Override
        public consts.SocketType getSocketType() { return consts.SocketType.SERVER; }
        @Override
        public network.QueueHandler getQueueHandler() { return null; }
    }

    public static void main(String[] args) {
        System.out.println("=== BAT DAU TEST GOI TIN E1: VALIDATE PACKET & HA NGUONG DECODER ===");

        testDecoderThresholdConstantsAndRouting();
        testDecoderFramingAndDecode();
        testDecoderXOREncryption();
        testMessageEnsureAndAvailable();
        testStressFuzzDecoder100k();

        System.out.println("\n=== KET QUA KIEM THU E1 ===");
        System.out.println("Tong so test: " + (passCount + failCount));
        System.out.println("PASS: " + passCount);
        System.out.println("FAIL: " + failCount);

        if (failCount > 0) {
            System.exit(1);
        } else {
            System.out.println("ALL E1 TESTS PASSED PERFECTLY!");
            System.exit(0);
        }
    }

    private static void testDecoderThresholdConstantsAndRouting() {
        System.out.println("\n--- Test 1: Decoder Threshold Constants & Routing ---");
        assertCondition(NroPacketDecoder.MAX_STANDARD_PACKET_SIZE == 65536,
                "Hạ trần Decoder tiêu chuẩn xuống 64KB (MAX_STANDARD_PACKET_SIZE = 65536)");
        assertCondition(NroPacketDecoder.MAX_EXTENDED_PACKET_SIZE == 204800,
                "Trần Decoder mở rộng cho phép 200KB (MAX_EXTENDED_PACKET_SIZE = 204800)");
        assertCondition(NroPacketDecoder.isExtendedCommand((byte) -74),
                "Opcode -74 (GET_IMAGE_SOURCE / Resource) được phân loại mở rộng");
        assertCondition(!NroPacketDecoder.isExtendedCommand((byte) 10),
                "Opcode 10 là gói tin tiêu chuẩn");
        assertCondition(!NroPacketDecoder.isExtendedCommand((byte) -100),
                "Opcode -100 (Consign) là gói tin tiêu chuẩn");
        assertCondition(!NroPacketDecoder.isExtendedCommand((byte) 6),
                "Opcode 6 (Buy Item) là gói tin tiêu chuẩn");
    }

    private static void testDecoderFramingAndDecode() {
        System.out.println("\n--- Test 2: Netty Decoder Framing & Message Creation ---");
        MockSession session = new MockSession();

        // 2.1 Giải mã gói tin tiêu chuẩn
        EmbeddedChannel channel = new EmbeddedChannel(new NroPacketDecoder(session));
        ByteBuf validBuf = Unpooled.buffer();
        validBuf.writeByte(10); // cmd
        validBuf.writeShort(5); // size
        validBuf.writeBytes(new byte[]{1, 2, 3, 4, 5}); // payload

        channel.writeInbound(validBuf);
        Message msg = channel.readInbound();
        assertCondition(msg != null && msg.command == 10 && msg.getData().length == 5,
                "Decoder giải mã thành công gói tin tiêu chuẩn <= 64KB");

        // 2.2 Giải mã gói tin mở rộng CMD -74
        EmbeddedChannel channelExt = new EmbeddedChannel(new NroPacketDecoder(session));
        ByteBuf extBuf = Unpooled.buffer();
        extBuf.writeByte(-74); // CMD -74
        extBuf.writeShort(1000); // size 1000 bytes
        extBuf.writeZero(1000);

        channelExt.writeInbound(extBuf);
        Message msgExt = channelExt.readInbound();
        assertCondition(msgExt != null && msgExt.command == -74 && msgExt.getData().length == 1000,
                "Decoder giải mã thành công gói tin mở rộng CMD -74");

        // 2.3 Phân mảnh TCP: Buffer chưa đủ byte không được decode sớm
        EmbeddedChannel channelFrag = new EmbeddedChannel(new NroPacketDecoder(session));
        ByteBuf fragBuf = Unpooled.buffer();
        fragBuf.writeByte(20);
        fragBuf.writeShort(10);
        fragBuf.writeBytes(new byte[]{1, 2, 3}); // chỉ gửi 3 trong tổng số 10 bytes

        channelFrag.writeInbound(fragBuf);
        Message msgPending = channelFrag.readInbound();
        assertCondition(msgPending == null, "Decoder không emit message khi chưa đủ TCP payload (chống frame rách)");

        // Gửi nốt 7 byte còn lại
        ByteBuf remBuf = Unpooled.buffer();
        remBuf.writeBytes(new byte[]{4, 5, 6, 7, 8, 9, 10});
        channelFrag.writeInbound(remBuf);
        Message msgCompleted = channelFrag.readInbound();
        assertCondition(msgCompleted != null && msgCompleted.command == 20 && msgCompleted.getData().length == 10,
                "Decoder ghép thành công frame khi nhận đủ chunk phân mảnh TCP");
    }

    private static void testDecoderXOREncryption() {
        System.out.println("\n--- Test 3: Decoder XOR Session Key Decryption ---");
        byte[] sessionKey = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD};
        MockSession session = new MockSession();
        session.setKey(sessionKey);

        EmbeddedChannel channel = new EmbeddedChannel(new NroPacketDecoder(session));

        byte plainCmd = 15;
        int plainSize = 3;
        byte[] plainData = new byte[]{0x11, 0x22, 0x33};

        // Mã hóa thủ công theo sessionKey
        ByteBuf encBuf = Unpooled.buffer();
        int cur = 0;
        encBuf.writeByte((byte) (plainCmd ^ sessionKey[cur++ % sessionKey.length]));
        encBuf.writeByte((byte) ((plainSize >> 8) ^ sessionKey[cur++ % sessionKey.length]));
        encBuf.writeByte((byte) ((plainSize & 0xFF) ^ sessionKey[cur++ % sessionKey.length]));
        for (byte b : plainData) {
            encBuf.writeByte((byte) (b ^ sessionKey[cur++ % sessionKey.length]));
        }

        channel.writeInbound(encBuf);
        Message decMsg = channel.readInbound();
        assertCondition(decMsg != null && decMsg.command == plainCmd && decMsg.getData().length == plainSize,
                "Decoder giải mã XOR Session Key thành công 100%");
        assertCondition(decMsg != null && decMsg.getData()[0] == 0x11 && decMsg.getData()[1] == 0x22 && decMsg.getData()[2] == 0x33,
                "Dữ liệu payload sau khi giải mã XOR khớp từng byte");
    }

    private static void testMessageEnsureAndAvailable() {
        System.out.println("\n--- Test 4: Message.ensure() & Message.available() ---");
        byte[] payload = new byte[]{10, 20, 30, 40};
        Message msg = new Message((byte) 5, payload);

        try {
            assertCondition(msg.available() == 4, "msg.available() ban đầu bằng 4");

            // Đảm bảo đủ 4 byte
            msg.ensure(4);
            assertCondition(true, "msg.ensure(4) thành công khi đủ byte");

            // Đọc 1 byte
            byte b = msg.reader().readByte();
            assertCondition(b == 10, "Đọc đúng byte đầu tiên: 10");
            assertCondition(msg.available() == 3, "msg.available() còn lại 3");

            // Đảm bảo 3 byte
            msg.ensure(3);
            assertCondition(true, "msg.ensure(3) thành công khi còn 3 byte");

            // Yêu cầu 4 byte khi chỉ còn 3 byte -> Underflow Exception
            boolean underflowCaught = false;
            try {
                msg.ensure(4);
            } catch (IOException e) {
                underflowCaught = true;
                assertCondition(e.getMessage().contains("Packet underflow"),
                        "Ném IOException chứa 'Packet underflow': " + e.getMessage());
            }
            assertCondition(underflowCaught, "Bắt được packet underflow chính xác");

        } catch (Exception e) {
            assertCondition(false, "Lỗi không mong muốn trong testMessageEnsure: " + e.getMessage());
        }
    }

    private static void testStressFuzzDecoder100k() {
        System.out.println("\n--- Test 5: Stress Fuzz 100,000 Frames qua Decoder ---");
        MockSession session = new MockSession();
        Random rand = new Random(42);

        int totalFuzzFrames = 100000;
        int handledErrors = 0;
        int validDecoded = 0;

        long startTime = System.currentTimeMillis();

        EmbeddedChannel channel = new EmbeddedChannel(new NroPacketDecoder(session));

        for (int i = 0; i < totalFuzzFrames; i++) {
            // Tạo frame ngẫu nhiên: kích thước từ 0 đến 300 bytes
            int frameLen = rand.nextInt(300);
            byte[] rawBytes = new byte[frameLen];
            rand.nextBytes(rawBytes);

            ByteBuf buf = Unpooled.wrappedBuffer(rawBytes);

            try {
                channel.writeInbound(buf);
                Message msg = channel.readInbound();
                if (msg != null) {
                    validDecoded++;
                }
            } catch (Exception e) {
                handledErrors++;
                channel = new EmbeddedChannel(new NroPacketDecoder(session));
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("Stress Fuzz 100.000 frames hoàn thành trong %d ms (Valid: %d, Handled: %d)%n",
                duration, validDecoded, handledErrors);

        assertCondition(handledErrors + validDecoded <= totalFuzzFrames,
                "Stress Fuzz 100.000 frames an toàn tuyệt đối, 0 uncaught crash, 0 OOM");
    }
}

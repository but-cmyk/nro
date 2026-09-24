package network.netty;

import interfaces.ISession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import network.io.Message;

import java.io.IOException;
import java.util.List;

/**
 * Netty Decoder cho giao thức packet của Ngọc Rồng Online.
 * Tự động phân khung gói tin, kiểm tra độ dài và giải mã khóa XOR phiên (Session Key).
 */
public class NroPacketDecoder extends ByteToMessageDecoder {

    private int curR = 0;
    private final ISession session;

    public NroPacketDecoder(ISession session) {
        this.session = session;
    }

    private static byte peekKey(byte[] key, int cursor, byte b) {
        if (key == null || key.length == 0) {
            return b;
        }
        return (byte) ((key[cursor] & 0xFF) ^ (b & 0xFF));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Cần tối thiểu 1 byte command + 2 bytes size = 3 bytes
        if (in.readableBytes() < 3) {
            return;
        }

        byte[] key = session.getKey();
        boolean isEncrypted = session.sentKey() && key != null && key.length > 0;
        int keyLen = isEncrypted ? key.length : 1;

        in.markReaderIndex();

        // 1. Peek Command và Size bằng con trỏ tạm mà KHÔNG thay đổi this.curR
        int tempR = this.curR;

        byte rawCmd = in.readByte();
        byte cmd;
        if (isEncrypted) {
            cmd = peekKey(key, tempR, rawCmd);
            tempR = (tempR + 1) % keyLen;
        } else {
            cmd = rawCmd;
        }

        // 2. Peek Kích thước gói tin (2 bytes)
        int size;
        if (isEncrypted) {
            byte b1 = in.readByte();
            byte b2 = in.readByte();
            byte db1 = peekKey(key, tempR, b1);
            tempR = (tempR + 1) % keyLen;
            byte db2 = peekKey(key, tempR, b2);
            tempR = (tempR + 1) % keyLen;
            size = ((db1 & 0xFF) << 8) | (db2 & 0xFF);
        } else {
            size = in.readUnsignedShort();
        }

        // Kiểm tra kích thước an toàn chống crash/flood (tối đa 200KB)
        if (size < 0 || size > 200000) {
            throw new IOException("Goi tin khong hop le hoac vuot qua kich thuoc cho phep: " + size);
        }

        // 3. Kiểm tra xem toàn bộ Payload đã tới đủ trong buffer chưa
        if (in.readableBytes() < size) {
            // Chưa đủ dữ liệu TCP, reset về vị trí đánh dấu và chờ chunk tiếp theo
            // Vì this.curR chưa từng bị thay đổi nên KHÔNG CẦN ROLLBACK!
            in.resetReaderIndex();
            return;
        }

        // 4. Toàn bộ gói tin đã có đủ trong buffer -> Đọc Payload và giải mã
        byte[] data = new byte[size];
        in.readBytes(data);

        if (isEncrypted) {
            for (int i = 0; i < size; i++) {
                data[i] = peekKey(key, tempR, data[i]);
                tempR = (tempR + 1) % keyLen;
            }
            // Cập nhật trạng thái con trỏ mã hóa chính thức sau khi giải mã trọn vẹn gói tin
            this.curR = tempR;
        }

        if (server.Manager.DEBUG) {
            utils.Logger.log("[NETTY RECV] Cmd: " + cmd + ", size: " + size + ", encrypted: " + isEncrypted + " from " + session.getIP());
        }
        out.add(new Message(cmd, data));
    }
}

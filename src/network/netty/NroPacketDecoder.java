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

    public byte readKey(byte b) {
        byte[] key = session.getKey();
        if (key == null || key.length == 0) {
            return b;
        }
        byte result = (byte) ((key[this.curR++] & 0xFF) ^ (b & 0xFF));
        if (this.curR >= key.length) {
            this.curR %= key.length;
        }
        return result;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Cần tối thiểu 1 byte command + 2 bytes size = 3 bytes
        if (in.readableBytes() < 3) {
            return;
        }

        in.markReaderIndex();

        // 1. Đọc Command
        byte rawCmd = in.readByte();
        byte cmd = session.sentKey() ? this.readKey(rawCmd) : rawCmd;

        // 2. Đọc Kích thước gói tin
        int size;
        if (session.sentKey()) {
            if (in.readableBytes() < 2) {
                in.resetReaderIndex();
                this.curR = (this.curR - 1 + session.getKey().length) % session.getKey().length;
                return;
            }
            byte b1 = in.readByte();
            byte b2 = in.readByte();
            size = ((this.readKey(b1) & 0xFF) << 8) | (this.readKey(b2) & 0xFF);
        } else {
            if (in.readableBytes() < 2) {
                in.resetReaderIndex();
                return;
            }
            size = in.readUnsignedShort();
        }

        // Kiểm tra kích thước an toàn chống crash/flood (tối đa 200KB)
        if (size < 0 || size > 200000) {
            throw new IOException("Goi tin khong hop le hoac vuot qua kich thuoc cho phep: " + size);
        }

        // 3. Đọc dữ liệu Payload
        if (in.readableBytes() < size) {
            in.resetReaderIndex();
            // Rollback cursor read key
            if (session.sentKey()) {
                int rollbackSteps = 3; // 1 byte cmd + 2 bytes size
                this.curR = (this.curR - rollbackSteps + (session.getKey().length * 10)) % session.getKey().length;
            }
            return;
        }

        byte[] data = new byte[size];
        in.readBytes(data);

        // 4. Giải mã dữ liệu Payload nếu đã kích hoạt Key
        if (session.sentKey()) {
            for (int i = 0; i < data.length; i++) {
                data[i] = this.readKey(data[i]);
            }
        }

        if (server.Manager.DEBUG) {
            utils.Logger.log("[NETTY RECV] Cmd: " + cmd + ", size: " + size + ", encrypted: " + session.sentKey() + " from " + session.getIP());
        }
        out.add(new Message(cmd, data));
    }
}

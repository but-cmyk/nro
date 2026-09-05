package network.netty;

import interfaces.ISession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import network.io.Message;

/**
 * Netty Encoder cho giao thức packet của Ngọc Rồng Online.
 * Đóng gói Command, Length và Payload theo mã hóa khóa XOR phiên.
 */
public class NroPacketEncoder extends MessageToByteEncoder<Message> {

    private int curW = 0;
    private final ISession session;

    public NroPacketEncoder(ISession session) {
        this.session = session;
    }

    public byte writeKey(byte b) {
        byte[] key = session.getKey();
        if (key == null || key.length == 0) {
            return b;
        }
        byte result = (byte) ((key[this.curW++] & 0xFF) ^ (b & 0xFF));
        if (this.curW >= key.length) {
            this.curW %= key.length;
        }
        return result;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] data = msg.getData();

        // 1. Ghi Command
        boolean shouldEncrypt = session.sentKey() && msg.command != -27; // Bỏ qua mã hóa cho gói tin Handshake xin Key
        if (shouldEncrypt) {
            out.writeByte(this.writeKey(msg.command));
        } else {
            out.writeByte(msg.command);
        }

        // 2. Ghi Độ dài (Size) và Payload
        if (data != null) {
            int size = data.length;
            // Trường hợp đặc biệt: Các command gửi resource/ảnh lớn dùng 3 bytes size
            if (msg.command == -32 || msg.command == -66 || msg.command == -74 || msg.command == 11
                    || msg.command == -67 || msg.command == -87 || msg.command == 66) {
                byte b2 = this.writeKey((byte) size);
                out.writeByte(b2 - 128);
                byte b3 = this.writeKey((byte) (size >> 8));
                out.writeByte(b3 - 128);
                byte b4 = this.writeKey((byte) (size >> 16));
                out.writeByte(b4 - 128);
            } else if (shouldEncrypt) {
                byte byte1 = this.writeKey((byte) (size >> 8));
                out.writeByte(byte1);
                byte byte2 = this.writeKey((byte) (size & 0xFF));
                out.writeByte(byte2);
            } else {
                out.writeShort(size);
            }

            // 3. Ghi Payload
            if (shouldEncrypt) {
                byte[] encryptedData = new byte[size];
                for (int i = 0; i < size; i++) {
                    encryptedData[i] = this.writeKey(data[i]);
                }
                out.writeBytes(encryptedData);
            } else {
                out.writeBytes(data);
            }
        } else {
            out.writeShort(0);
        }

        if (server.Manager.DEBUG) {
            utils.Logger.log("[NETTY SEND] Cmd: " + msg.command + ", size: " + (data != null ? data.length : 0) + ", encrypted: " + shouldEncrypt + " to " + session.getIP());
        }
        // msg.cleanup() không được gọi ở đây để tránh race condition khi broadcast cùng 1 Message tới nhiều kênh
    }
}

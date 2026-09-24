# Module GEMINI: Hệ Thống Mạng & Gói Tin Nhị Phân (Netty Socket)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Xử lý kết nối TCP socket đồng thời từ hàng nghìn client, giải mã gói tin (Decoder), quản lý phiên (`Session`) và phân phối message tới Controller.
- **Class trọng tâm**:
  - `NettyServer.java`: Khởi động Netty pipeline, thiết lập ChannelInitializer.
  - `NettySession.java` / `MySession.java`: Đại diện cho 1 socket client, lưu trữ khóa mã hóa, IP, trạng thái đăng nhập.
  - `Message.java`: Đóng gói packet nhị phân gồm opcode (`byte command`) và payload (`byte[]`).
  - `src/server/Controller.java`: Tiếp nhận và giải mã Message theo từng Command opcode.
  - `src/services/Service.java`: Bộ phát packet từ Server về Client.

## 2. Landmark Index & Core Methods
- `Controller.onMessage(Session session, Message msg)`: Dispatcher trung tâm giải mã toàn bộ opcode từ client.
- `Service.sendMessage(Session session, Message msg)`: Đẩy packet vào pipeline mạng.
- `Message.writer()` / `Message.reader()`: DataOutputStream và DataInputStream nhị phân.
- `NettySession.close()`: Đóng socket và giải phóng phiên.

## 3. Các Bất Biến Bắt Buộc
- **Protocol Sync 1:1**:
  - Khi Server gọi `msg.writer().writeByte(a); msg.writer().writeInt(b);`, Client tại `Controller.cs` bắt buộc phải đọc đúng thứ tự: `msg.reader().readByte(); msg.reader().readInt();`.
  - Mọi trường chuỗi UTF-8 (`writeUTF`) nếu giá trị `null` phải ghi chuỗi rỗng `""`, tránh ném ngoại lệ stream.
- **Backpressure & Flush an toàn**:
  - Kiểm tra `channel.isWritable()` trước khi gửi khối dữ liệu lớn (như dữ liệu data game `-74` hay danh sách item).
  - Không drop hoặc đóng socket đột ngột khi đang trong tiến trình truyền tải dữ liệu ban đầu.

## 4. Lỗi Thường Gặp Cần Tránh
- Đọc thiếu byte trong `Controller.java`: Khiến byte rác dồn vào packet kế tiếp, làm vỡ khung truyền thông của toàn bộ session.
- Quên release ByteBuf trong Netty pipeline dẫn đến rò rỉ bộ nhớ Direct Memory (Native OOM).

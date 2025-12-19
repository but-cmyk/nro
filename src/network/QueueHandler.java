package network;

import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import interfaces.IMessageHandler;
import interfaces.ISession;
import network.io.Message;

public class QueueHandler implements Runnable {
    // Thêm volatile để đảm bảo tính nhất quán giữa các luồng khi biến này thay đổi
    private volatile ISession session; 
    private BlockingDeque<Message> messages;
    @Setter
    private IMessageHandler messageHandler;

    public QueueHandler(@NonNull ISession session) {
        // Bỏ try-catch thừa thãi để code sạch hơn. 
        // @NonNull sẽ tự ném lỗi nếu session null.
        this.session = session;
        this.messages = new LinkedBlockingDeque<>();
    }

    @Override
    public void run() {
        try {
            // SỬA QUAN TRỌNG: Thêm 'session != null' trước khi gọi isConnected()
            while (session != null && session.isConnected()) {
                // Dùng take() để chặn luồng cho đến khi có message (tiết kiệm CPU)
                Message message = messages.take();
                
                // Kiểm tra lại session lần nữa sau khi tỉnh dậy từ take()
                if (session == null || !session.isConnected()) {
                    break;
                }

                if (message != null) {
                    if (this.messageHandler != null) {
                        this.messageHandler.onMessage(this.session, message);
                    }
                    // message.cleanup(); // Giữ nguyên việc comment dòng này như ý bạn
                }
            }
        } catch (InterruptedException e) {
            // Luồng bị ngắt (thường do dispose hoặc shutdown), không cần in stack trace
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMessage(Message msg) {
        try {
            if (session != null && session.isConnected() && messages.size() < 500) {
                messages.add(msg);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public void close() {
        if (messages != null) {
            messages.clear();
        }
    }

    public void dispose() {
        // Ngắt kết nối trước khi null hoá
        this.session = null;
        this.messages = null;
    }
}
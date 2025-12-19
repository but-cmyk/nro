package data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import models.Template.ArrHead2Frames;
import models.Template.ItemOptionTemplate;
import models.Template.ItemTemplate;
import server.Manager;
import network.io.Message;
import network.session.MySession;
import utils.Logger;

public class ItemData {

    // Danh sách thực phẩm (giữ nguyên logic cũ)
    public static final List<Integer> list_thuc_an = Arrays.asList(663, 664, 665, 666, 667);

    // Hằng số giới hạn số lượng item gửi trong 1 gói (tránh quá tải buffer client)
    private static final int SPLIT_SIZE = 750;

    // --- BỘ NHỚ ĐỆM (CACHE) ---
    // Lưu trữ mảng byte đã được tính toán sẵn
    private static byte[] CACHE_ITEM_OPTIONS;
    private static byte[] CACHE_TEMPLATE_PART_1; // Reload (0 -> 750)
    private static byte[] CACHE_TEMPLATE_PART_2; // Add (750 -> End)
    private static byte[] CACHE_HEAD_2_FRAMES;

    /**
     * Hàm khởi tạo dữ liệu Cache. BẮT BUỘC GỌI hàm này trong ServerManager khi
     * khởi động Server!
     */
    public static void init() {
        try {
            long st = System.currentTimeMillis();

            // 1. Cache Item Options
            CACHE_ITEM_OPTIONS = createCacheItemOption();

            // 2. Cache Item Templates (Chia làm 2 phần)
            CACHE_TEMPLATE_PART_1 = createCacheItemTemplatePart1();
            if (Manager.ITEM_TEMPLATES.size() > SPLIT_SIZE) {
                CACHE_TEMPLATE_PART_2 = createCacheItemTemplatePart2();
            }

            // 3. Cache Head Frames
            CACHE_HEAD_2_FRAMES = createCacheArrHead2Frames();

            Logger.success("Init ItemData (Cache) thành công! (" + (System.currentTimeMillis() - st) + "ms)");
        } catch (Exception e) {
            Logger.error("Lỗi khởi tạo ItemData: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Lỗi dữ liệu nghiêm trọng -> Dừng server
        }
    }

    /**
     * Gửi dữ liệu Item cho người chơi (Sử dụng Cache)
     */
    public static void updateItem(MySession session) {
        if (CACHE_TEMPLATE_PART_1 == null) {
            Logger.error("CHƯA GỌI ItemData.init() KHI KHỞI ĐỘNG SERVER!");
            return;
        }

        // Gửi lần lượt các gói tin đã cache
        // Command -28 là command chung cho update data
        sendCachedMessage(session, -28, CACHE_ITEM_OPTIONS);
        sendCachedMessage(session, -28, CACHE_HEAD_2_FRAMES);
        sendCachedMessage(session, -28, CACHE_TEMPLATE_PART_1);

        // Nếu có phần 2 (số lượng item > 750) thì gửi tiếp
        if (CACHE_TEMPLATE_PART_2 != null) {
            sendCachedMessage(session, -28, CACHE_TEMPLATE_PART_2);
        }
    }

    // --- CÁC HÀM TẠO CACHE (INTERNAL) ---
    private static byte[] createCacheItemOption() throws IOException {
        Message msg = new Message(-28); // Command chỉ để khởi tạo, ta lấy data body
        DataOutputStream ds = msg.writer();

        ds.writeByte(8);
        ds.writeByte(DataGame.vsItem);
        ds.writeByte(0); // update option
        ds.writeByte(Manager.ITEM_OPTION_TEMPLATES.size());
        for (ItemOptionTemplate io : Manager.ITEM_OPTION_TEMPLATES) {
            ds.writeUTF(io.name);
            ds.writeByte(io.type);
        }
        ds.flush();
        byte[] data = msg.getData();
        msg.cleanup();
        return data;
    }

    private static byte[] createCacheItemTemplatePart1() throws IOException {
        Message msg = new Message(-28);
        DataOutputStream ds = msg.writer();

        int count = Math.min(SPLIT_SIZE, Manager.ITEM_TEMPLATES.size());

        ds.writeByte(8);
        ds.writeByte(DataGame.vsItem);
        ds.writeByte(1); // reload itemtemplate
        ds.writeShort(count);

        for (int i = 0; i < count; i++) {
            writeItemProperties(ds, Manager.ITEM_TEMPLATES.get(i));
        }

        ds.flush();
        byte[] data = msg.getData();
        msg.cleanup();
        return data;
    }

    private static byte[] createCacheItemTemplatePart2() throws IOException {
        Message msg = new Message(-28);
        DataOutputStream ds = msg.writer();

        int start = SPLIT_SIZE;
        int end = Manager.ITEM_TEMPLATES.size();

        ds.writeByte(8);
        ds.writeByte(DataGame.vsItem);
        ds.writeByte(2); // add itemtemplate
        ds.writeShort(start);
        ds.writeShort(end);

        for (int i = start; i < end; i++) {
            writeItemProperties(ds, Manager.ITEM_TEMPLATES.get(i));
        }

        ds.flush();
        byte[] data = msg.getData();
        msg.cleanup();
        return data;
    }

    private static byte[] createCacheArrHead2Frames() throws IOException {
        Message msg = new Message(-28);
        DataOutputStream ds = msg.writer();

        ds.writeByte(8);
        ds.writeByte(DataGame.vsItem);
        ds.writeByte(100);
        ds.writeShort(Manager.ARR_HEAD_2_FRAMES.size());
        for (ArrHead2Frames io : Manager.ARR_HEAD_2_FRAMES) {
            ds.writeByte(io.frames.size());
            for (int i : io.frames) {
                ds.writeShort(i);
            }
        }

        ds.flush();
        byte[] data = msg.getData();
        msg.cleanup();
        return data;
    }

    // Hàm helper để viết thuộc tính Item (Tránh lặp code)
    private static void writeItemProperties(DataOutputStream ds, ItemTemplate temp) throws IOException {
        ds.writeByte(temp.type);
        ds.writeByte(temp.gender);
        ds.writeUTF(temp.name);
        ds.writeUTF(temp.description);
        ds.writeByte(temp.level);
        ds.writeInt(temp.strRequire);
        ds.writeShort(temp.iconID);
        ds.writeShort(temp.part);
        ds.writeBoolean(temp.isUpToUp);
    }

    // Hàm gửi gói tin từ dữ liệu cache
    private static void sendCachedMessage(MySession session, int command, byte[] cachedBody) {
        Message msg = null;
        try {
            msg = new Message(command);
            // Ghi toàn bộ nội dung đã cache vào message mới
            msg.writer().write(cachedBody);
            session.doSendMessage(msg);
        } catch (Exception e) {
            Logger.error("Lỗi gửi gói tin cache ItemData");
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}

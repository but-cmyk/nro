# Module GEMINI: Vật Phẩm, Túi Đồ & Chống Bug Dupe Đồ (Economy Core)

## 1. Trách Nhiệm & Kiến Trúc
- **Phạm vi**: Xử lý toàn bộ logic liên quan tới vật phẩm, giao dịch người chơi, nâng cấp/ép sao pha lê, cửa hàng và ký gửi.
- **Class trọng tâm**:
  - `ItemService.java`: Tạo item từ template, thêm/bớt item vào túi, kiểm tra slot trống.
  - `CombineService.java`: Logic đập đồ, nâng cấp trang bị, pha lê hóa tại NPC Bà Hạt Mít.
  - `Trade.java` / `TransactionService.java`: Giao dịch an toàn giữa 2 người chơi.
  - `ShopService.java` / `ConsignShopService.java`: Hệ thống bán hàng, ký gửi.

## 2. Landmark Index & Core Methods
- `ItemService.createNewItem(short tempId, int quantity)`: Khởi tạo instance item độc lập, deep-copy từ template.
- `ItemService.addItemBag(Player player, Item item)`: Thêm item vào hành trang có kiểm tra ô trống.
- `ItemService.subQuantityItemsBag(Player player, Item item, int quantity)`: Trừ số lượng an toàn.
- `CombineService.combine(Player player)`: Thực thi đập đồ có khóa đối tượng chống spam packet dupe.
- `Trade.tradeItem()`: Chuyển giao đồ giữa 2 người chơi có lock trạng thái 2 chiều.

## 3. Các Bất Biến Bắt Buộc
- **Bảo Vệ Đơn Hàng & Thao Tác Khoản Mục**:
  - Trước khi trừ nguyên liệu hoặc item, bắt buộc phải kiểm tra: `item != null && item.quantity >= quantityRequired`.
  - Tuyệt đối không cho phép số lượng âm (`quantity <= 0`) trong túi đồ hoặc rương.
- **Khóa Giao Dịch Chống Dupe (Concurrency Lock)**:
  - Trong quá trình Trade hoặc Combine, item phải được "khóa trạng thái" (lock) để ngăn chặn việc người chơi lợi dụng spam packet cùng một lúc từ nhiều client/tab để nhân bản đồ.
- **Clone Item An Toàn**:
  - Khi chuyển item từ Shop hoặc Template vào túi người chơi, luôn dùng `ItemService.gI().createNewItem(...)` hoặc deep-copy, cấm gán tham chiếu trực tiếp từ Template tĩnh.
- Bảo Vệ Giao Dịch ACID: Lưu DB đồng bộ trong Trade; nếu DB lưu thất bại bắt buộc phải rollback RAM về bagBefore để chống bug dupe qua ngắt kết nối
- Khóa Đồng Bộ Luồng: Luôn đảm bảo lock trạng thái khi thao tác đa luồng hoặc spam packet để chống bug dupe. `[2026-09-24]`
- Kiểm tra quantity > 0 trước khi trừ, không bao giờ để quantity âm. `[2026-09-24]`
- Đồng bộ Stream Shop Ký Gửi: Gói -44 và -100 phải ghi writeUTF(sellerName) khi version >= 237 để khớp với Controller Client tránh lệch byte stream. `[2026-09-24]`
- Phòng Ngừa Tràn Số: Luôn sử dụng kiểu `long` cho các trường tích lũy (HP, sức mạnh, tiền, kinh nghiệm) để tránh tràn mốc 2 tỷ. `[2026-09-24]`
- Đồng Bộ Gói Tin Nhị Phân 1:1: Mọi thứ tự write/read packet giữa Server Java và Client Unity C# phải hoàn toàn tương ứng. `[2026-09-24]`
- Dùng CopyOnWriteArrayList/ConcurrentHashMap cho collection duyệt trong vòng lặp Zone update. `[2026-09-24]`
- Cơ Chế Mở Nội Tại 3 Phân Tầng: Top 25% dải chỉ số cao nhất là Cực khó ra (2%), 25% dải kế tiếp là Khó ra (13%), 50% dải còn lại là Bình thường (85%); dùng Util.nextInt(from, to) inclusive chuẩn xác `[2026-09-27]`
- Vòng quay Thượng Đế: Lock synchronized(player) bao trọn kiểm tra và trừ tiền (subGold long, subGem), hạn chế tỷ lệ Thỏi vàng dưới 5% chống lạm phát farm vàng vô hạn `[2026-09-28]`
- Vòng quay Thượng Đế: Bỏ quay vàng, chỉ giữ quay ngọc và quay vé. Vé quay (ID 821) cày free qua Boss rơi (40%) và Nhiệm vụ hàng ngày (1-5 vé). Phần thưởng chuẩn logic NRO: Ngọc rồng, Đá nâng cấp, Đá bảo vệ, Thức ăn, Cải trang. `[2026-09-28]`
- Vo Dai Whis 1vs1 (Map 48 Kaio -> Map 113): Sanh cho Y=360, Len dai Y=264. Khung gio 21h-22h hang ngay (TEST_MODE_24_7 mo 24/24 test). Chu ky tuan chot 22h CN, chi Top 1-5 BXH tuan duoc doi Capsule Vang Ki Bi (ID 574), reset 23h59 CN. Tich hop 100 Bot Player trong WhisArenaBotManager de thi dau leo rank. `[2026-09-29]`
- Map clone rieng cho Vo Dai Whis la Map ID 186 ('Vo Dai Whis'). Giao dien Bang Xep Hang dung Opcode -96 voi typeTop = 0 mo Panel vTop chuan Client voi avatar head, headICON, body, leg, name, info1, info2. `[2026-09-29]`
- Vo Dai Lien Server / Whis (Map 186): Cho phep Random Solo voi ALL Bang Xep Hang (Top 1-100). Khi khiêu chiến hoặc ghép trận nếu đối thủ là player đang bận thi đấu hoặc offline, hệ thống tự động kích hoạt Bản Sao AI (Clone Bot) với đầy đủ ngoại hình, chỉ số HP/Dame và Rank để vào sàn solo ngay lập tức, không bị khoanh vùng top 5 và không bị chặn. Trước trận hồi full 100% HP/KI và giải trừ cooldown skill. Thua trận không cộng điểm an ủi (0 điểm). Hết trận tự động đưa người chơi về sảnh Zone 0 Map 186. `[2026-10-01]`
- Giai Dau Lien Vu Tru 6-7 (Map 186): Khi danh xong 1 Boss, he thong tu dong dua player xuong dai ve Sanh cho Zone 0 (hoi phuc 100% HP/MP, go PK), dong thoi luu tien trinh round (player.whisArenaU6Round). Khi player tro chuyen voi NPC va chon 'Giai Dau Lien Vu Tru 6-7' lan tiep theo, player moi len dai dau voi Boss vong tiep theo (Frost -> Kyabe -> Autamegetta -> Hit). `[2026-10-01]`
- Giai Dau Lien Vu Tru 6-7: Khi an vao tab Giai Dau Lien Vu Tru 6-7 tai Map 186/Kaio, he thong dua player sang Map 187 rieng (tile 23 clone DHVT 23). Tai day Bill mo tab: 'Giai dau Lien Vu Tru 6-7 quy tu nhieu cao thu... Nguoi co dam tham gia khong? Se co nhung phan thuong hap dan' voi 3 nut: [Quay ve Giai dau Lien Vu Tru], [San sang], [Tu choi]. Bam 'San sang' moi len dai danh boss round hien tai. Ha xong 1 boss dua player xuong dat (255, 360). Tuyet doi KHONG tu dong mo menu qua timeout; player phai chu dong click vao NPC thi menu moi hien thi. `[2026-10-01]`
- Quy chuan ngoai trang Bot: Bo ba (head, body, leg) phai dung loai Part (head=type 0, body=type 1, leg=type 2). Tuyet doi khong dung part type 0 lam body hoac leg vi se gay loi ghep dau than chan bi loi tach roi. `[2026-10-01]`
- Chuan hoa font chu va mau sac: Tuyet doi khong lam dung cac ma mau ky tu so doc (|0|, |1|, |2|, |7|) trong chuoi thong bao, chat va bang xep hang vi se ep client chuyen sang font mau toi kho nhin hoac loi render. Hay su dung van ban tieng Viet chuan khong gan tag mau de client hien thi font mac dinh trang vien den (tahoma_7b_white) ro net giong Dai hoi vo thuat 23 va Sieu hang. `[2026-10-01]`
- Co che Len/Xuong san dau Vo Dai Lien Server: Tuyet doi khong dung changeMap de dua player sang zone khac hoac tro ve zone 0 sau tran dau vi se gay giat lag, den man hinh va reload map dot ngot. Hay giu nguyen zone hien tai cua player (uu tien player.zone), chi su dung playerMove va setPos0 de dua 2 player len san (Y=264) sau khi doi 3 giay tai sanh cho (Y=360), va xuong san (Y=360) sau tran dau. Dong thoi bo sung tuy chon Doi khu vuc trong menu NPC va khac phuc NPE trong openZoneUI khi zone dang isCompeting. `[2026-10-01]`
- Khi nạp options từ DB trong parseItemSafe, bắt buộc clear itemOptions mặc định trước khi duyệt nạp options để tránh nhân bản trùng lặp options sau mỗi lần lưu/tải (đặc biệt với vật phẩm có default option như Cà chua 191, Cà rốt 192). `[2026-10-02]`

## 4. Lỗi Thường Gặp Cần Tránh
- Dupe đồ qua ngắt kết nối mạng ngay thời điểm xác nhận giao dịch.
- Trừ item thành công nhưng ném ngoại lệ khi add item mới dẫn đến mất đồ của người chơi.

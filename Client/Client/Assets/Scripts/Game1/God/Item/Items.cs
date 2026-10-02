using System.Collections.Generic;

namespace Game1.God
{
    /*Author: HAIRMOD*/
    public class Items : IActionListener, IChatable
    {
        private static Items instance { get; set; }
        public static Items getInstance()
        {
            return (instance == null) ? (instance = new Items()) : instance;
        }
        private List<ItemGroup> itemUses = new List<ItemGroup>();
        private List<ItemGroup> itemBuys = new List<ItemGroup>();
        private ItemGroup listUses;
        private ItemGroup listBuys;
        private string[] inputUses;
        private string[] inputBuys;
        private long timeUses, timeBuys;
        private int quantity;
        public Items()
        {
            inputUses = new string[2] { "Tự Động Sử Dụng Vật Phẩm", "Nhập Số Lượng Sử Dụng" };
            inputBuys = new string[2] { "Tự Động Mua Vật Phẩm", "Nhập Số Lượng Vật Phẩm Cần Mua" };
        }
        public static bool isItemTimeBuff(int itemId)
        {
            return getIconIdByItemTemplate(itemId) != -1;
        }

        public static short getIconIdByItemTemplate(int templateId)
        {
            switch (templateId)
            {
                case 191: return 931;   // Cà chua
                case 192: return 933;   // Cà rốt
                case 379: return 2758;  // Máy dò Capsule kì bí
                case 381: return 2754;  // Cuồng nộ
                case 382: return 2755;  // Bổ huyết
                case 383: return 2756;  // Bổ khí
                case 384: return 2757;  // Giáp Xên bọ hung
                case 385: return 2760;  // Ẩn danh
                case 521: return 4387;  // Tự động luyện tập (TDLT)
                case 579:
                case 1045: return 5072; // Đuôi khỉ
                case 582: return 5829;  // Cuồng nộ cấp 2
                case 638: return 5829;  // CMS
                case 663: return 6324;  // Bánh Pudding
                case 664: return 6325;  // Xúc xích
                case 665: return 6326;  // Kem dâu
                case 666: return 6327;  // Mì ly
                case 667: return 6328;  // Sushi
                case 752: return 7079;  // Bánh tét
                case 753: return 7080;  // Bánh chưng
                case 764:
                case 865: return 7149;  // Khẩu trang
                case 880: return 8060;  // Cua rang me
                case 881: return 8061;  // Bạch tuộc nướng
                case 882: return 8062;  // Tôm tẩm bột chiên xù
                case 899: return 8243;  // Kẹo một mắt
                case 900: return 8244;  // Súp bí hắc ám
                case 902: return 8246;  // Bánh gato nhện
                case 903: return 8247;  // Hamburger sâu
                case 1089:
                case 1262: return 10904; // Xí muội hoa mai
                case 1090:
                case 1261: return 10905; // Xí muội hoa đào
                case 1099: return 10716; // Cuồng Nộ Siêu Cấp
                case 1100: return 10714; // Bổ Huyết Siêu Cấp
                case 1101: return 10715; // Bổ Khí Siêu Cấp
                case 1102: return 10712; // Giáp Xên Siêu Cấp
                case 1103: return 10717; // Ẩn Danh Đặc Biệt
                case 1109: return 2758;  // Máy dò
                case 1117:
                case 2160: return 11173; // Nước Mía NCD
                case 1136: return 13540; // Tiềm Năng Đệ Tử
                case 1137: return 13618; // Cỏ bốn lá
                case 1143:
                case 1397: return 11474; // Bùa TNSM
                case 1253: return 14776; // Canh khổ qua
                case 1254: return 14775; // Thịt kho trứng
                case 1465: return 16940; // Cuốn chả giò
                case 1466: return 16941; // Chai Cuke 2 lít
                default: return -1;
            }
        }

        public static bool isBuffActive(int itemId)
        {
            short iconId = getIconIdByItemTemplate(itemId);
            if (iconId == -1)
            {
                return false;
            }
            if (Char.vItemTime == null || Char.vItemTime.size() == 0)
            {
                return false;
            }
            for (int i = 0; i < Char.vItemTime.size(); i++)
            {
                ItemTime it = (ItemTime)Char.vItemTime.elementAt(i);
                if (it != null && it.idIcon == iconId)
                {
                    if (it.minute > 0 || it.second > 1)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        private void autoUse()
        {
            if (itemUses == null || itemUses.Count == 0) return;
            long now = mSystem.currentTimeMillis();
            for (int i = itemUses.Count - 1; i >= 0; i--)
            {
                var item = itemUses[i];
                if (item.quantityUse <= 0)
                {
                    itemUses.RemoveAt(i);
                    GameScr.info1.addInfo("Đã dùng xong!", 0);
                    continue;
                }

                // Kiểm tra xem vật phẩm còn tồn tại trong hành trang không
                bool hasItem = false;
                if (Char.myCharz() != null && Char.myCharz().arrItemBag != null)
                {
                    for (int j = 0; j < Char.myCharz().arrItemBag.Length; j++)
                    {
                        var it = Char.myCharz().arrItemBag[j];
                        if (it != null && it.template != null && it.template.id == item.id && it.quantity > 0)
                        {
                            hasItem = true;
                            break;
                        }
                    }
                }

                if (!hasItem)
                {
                    ItemTemplate tpl = ItemTemplates.get((short)item.id);
                    string itName = (tpl != null) ? tpl.name : "Vật phẩm";
                    itemUses.RemoveAt(i);
                    GameScr.info1.addInfo($"Hết {itName} trong hành trang!", 0);
                    continue;
                }

                // Không tự động dùng khi nhân vật đang chết
                if (Char.myCharz() != null && Char.myCharz().statusMe == 14)
                {
                    continue;
                }

                bool isTimeItem = isItemTimeBuff(item.id);
                if (isTimeItem)
                {
                    // Đối với item time: Chỉ ăn tiếp khi buff trước đó đã hết hiệu lực
                    if (isBuffActive(item.id))
                    {
                        continue;
                    }

                    // Đợi tối thiểu 2.5 giây giữa 2 lần dùng liên tiếp
                    if (now - item.lastTimeUse < 2500L)
                    {
                        continue;
                    }

                    item.lastTimeUse = now;
                    item.quantityUse--;
                    itemUses[i] = item;
                    Utils.UseItem(item.id);

                    ItemTemplate tpl = ItemTemplates.get((short)item.id);
                    string itName = (tpl != null) ? tpl.name : "Vật phẩm";
                    if (item.quantityUse <= 0)
                    {
                        itemUses.RemoveAt(i);
                        GameScr.info1.addInfo($"Auto dùng: Đã dùng hết {itName}!", 0);
                    }
                    else
                    {
                        GameScr.info1.addInfo($"Auto dùng {itName}: Còn lại {item.quantityUse} cái", 0);
                    }
                }
                else
                {
                    // Vật phẩm thường (dùng liên tiếp mỗi 200ms)
                    if (now - item.lastTimeUse >= 200L)
                    {
                        item.lastTimeUse = now;
                        item.quantityUse--;
                        itemUses[i] = item;
                        Utils.UseItem(item.id);
                        if (item.quantityUse <= 0)
                        {
                            itemUses.RemoveAt(i);
                            GameScr.info1.addInfo("Sử dụng hoàn tất!", 0);
                        }
                    }
                }
            }
        }
        private void autoBuy()
        {
            if (itemBuys == null || itemBuys.Count == 0) return;
            if (quantity <= 0)
            {
                itemBuys.Clear();
                GameScr.info1.addInfo("Xong!", 0);
                return;
            }
            if (mSystem.currentTimeMillis() - timeBuys >= 200L)
            {
                timeBuys = mSystem.currentTimeMillis();
                var item = itemBuys[0];
                Service.gI().buyItem((sbyte)(item.buyGold ? 0 : 1), item.id, 0);
                quantity--;
                if (quantity <= 0)
                {
                    itemBuys.Clear();
                    GameScr.info1.addInfo("Mua thành công!", 0);
                }
            }
        }
        private void addGroupItemToListUse(ItemGroup groupItem)
        {
            GameCanvas.panel.hide();
            Utils.startChat(this, inputUses[0], inputUses[1], TField.INPUT_TYPE_NUMERIC);
            this.listUses = groupItem;
        }
        private void addGroupItemToListBuy(ItemGroup groupItem)
        {
            GameCanvas.panel.hide();
            Utils.startChat(this, inputBuys[0], inputBuys[1], TField.INPUT_TYPE_NUMERIC);
            this.listBuys = groupItem;
        }
        public bool checkContains(int id)
        {
            if (itemUses.Count == 0) return false;
            foreach (var item in itemUses)
            {
                if (item.id == id && item.quantityUse > 0)
                {
                    return true;
                }
            }
            return false;
        }
        private void removeItem(int id)
        {
            for (int i = 0; i < itemUses.Count; i++)
            {
                if (itemUses[i].id == id)
                {
                    itemUses.RemoveAt(i);
                    break;
                }
            }
        }
        public void onChatFromMe(string text, string to)
        {
            ChatTextField chatTextField = ChatTextField.gI();
            if (chatTextField.strChat.Equals(inputUses[0]))
            {
                if (int.TryParse(chatTextField.tfChat.getText().Trim(), out int count) && count > 0)
                {
                    int totalInBag = 0;
                    if (Char.myCharz() != null && Char.myCharz().arrItemBag != null)
                    {
                        foreach (var it in Char.myCharz().arrItemBag)
                        {
                            if (it != null && it.template != null && it.template.id == listUses.id)
                            {
                                totalInBag += it.quantity;
                            }
                        }
                    }
                    if (totalInBag > 0 && count > totalInBag)
                    {
                        count = totalInBag;
                    }
                    listUses.quantityUse = count;
                    listUses.lastTimeUse = 0;
                    ItemTemplate tpl = ItemTemplates.get((short)listUses.id);
                    string tplName = (tpl != null) ? tpl.name : "vật phẩm";
                    GameScr.info1.addInfo($"Bật auto dùng {tplName}: {count} cái", 0);
                    removeItem(listUses.id);
                    itemUses.Add(listUses);
                }
                else
                {
                    GameScr.info1.addInfo("Số lượng không hợp lệ!", 0);
                }
                Utils.resetTF();
                return;
            }
            else if (chatTextField.strChat.Equals(inputBuys[0]))
            {
                if (int.TryParse(chatTextField.tfChat.getText().Trim(), out int num) && num > 0)
                {
                    quantity = num;
                    GameScr.info1.addInfo($"Số Lượng: {num}", 0);
                    itemBuys.Add(listBuys);
                }
                else
                {
                    GameScr.info1.addInfo("Số lượng không hợp lệ!", 0);
                }
                Utils.resetTF();
                return;
            }
        }
        public void Update()
        {
            autoBuy();
            autoUse();
        }
        public void perform(int idAction, object p)
        {
            switch (idAction)
            {
                case 1:
                    addGroupItemToListUse((ItemGroup)p);
                    break;
                case 2:
                    GameScr.info1.addInfo("Đã xóa ra khỏi danh sách", 0);
                    int id = System.Convert.ToInt32(p);
                    removeItem(id);
                    break;
                case 3:
                    addGroupItemToListBuy((ItemGroup)p);
                    break;
            }
        }
        public void onCancelChat() => Utils.resetTF();
    }
}

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
            inputUses = new string[2] { "Tự Động Sử Dụng Vật Phẩm", "Nhập Thời Gian Sử Dụng (Giây)"};
            inputBuys = new string[2] { "Tự Động Mua Vật Phẩm", "Nhập Số Lượng Vật Phẩm Cần Mua"};
        }
        private void autoUse()
        {
            if (itemUses == null || itemUses.Count == 0) return;
            foreach(var item in itemUses)
            {
                Utils.useItemWithTime(item.id, timeUses * 1000L);
            }
        }
        private void autoBuy()
        {
            if (itemBuys == null && itemBuys.Count == 0) return;
            foreach (var item in itemBuys)
            {
                if (quantity == 0)
                {
                    itemBuys.Remove(item);
                    GameScr.info1.addInfo("Xong!", 0);
                    break;
                }
                while (quantity > 0 && mSystem.currentTimeMillis() - timeBuys >= 200L)
                {
                    Service.gI().buyItem((sbyte)((item.buyGold) ? 0 : 1), item.id, 0);
                    quantity--;
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
                if (item.id == id)
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
                int time = int.Parse(chatTextField.tfChat.getText());
                timeUses = time;
                GameScr.info1.addInfo($"Delay: {time} giây", 0);
                itemUses.Add(listUses);
                Utils.resetTF();
                return;
            }
            else if (chatTextField.strChat.Equals(inputBuys[0]))
            {
                int num = int.Parse(chatTextField.tfChat.getText());
                quantity = num;
                GameScr.info1.addInfo($"Số Lượng: {num}", 0);
                itemBuys.Add(listBuys);
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
                    short id = (short)p;
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

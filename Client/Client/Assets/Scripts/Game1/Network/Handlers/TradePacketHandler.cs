namespace Game1
{
    using System;

    /// <summary>
    /// TradePacketHandler: Chuyên trách phân tích và xử lý các gói tin giao dịch giữa người chơi với nhau.
    /// Bao gồm: Yêu cầu giao dịch, khóa GD, đồng ý, hủy và hoàn tất (cmd -86, 39).
    /// </summary>
    public class TradePacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case NetworkOpcodes.TRADE_ACTION:
                        {
                            sbyte action = msg.reader().readByte();
                            Res.outz("server gui ve giao dich action = " + action);
                            if (action == 0)
                            {
                                int playerID = msg.reader().readInt();
                                GameScr.gI().giaodich(playerID);
                            }
                            else if (action == 1)
                            {
                                int charId = msg.reader().readInt();
                                Char targetChar = GameScr.findCharInMap(charId);
                                if (targetChar == null)
                                {
                                    return true;
                                }
                                GameCanvas.panel.setTypeGiaoDich(targetChar);
                                GameCanvas.panel.show();
                                Service.gI().getPlayerMenu(charId);
                            }
                            else if (action == 2)
                            {
                                sbyte itemIndex = msg.reader().readByte();
                                for (int i = 0; i < GameCanvas.panel.vMyGD.size(); i++)
                                {
                                    Item item = (Item)GameCanvas.panel.vMyGD.elementAt(i);
                                    if (item.indexUI == itemIndex)
                                    {
                                        GameCanvas.panel.vMyGD.removeElement(item);
                                        break;
                                    }
                                }
                            }
                            else if (action == 5)
                            {
                                // Reserved action
                            }
                            else if (action == 6)
                            {
                                GameCanvas.panel.isFriendLock = true;
                                if (GameCanvas.panel2 != null)
                                {
                                    GameCanvas.panel2.isFriendLock = true;
                                }
                                GameCanvas.panel.vFriendGD.removeAllElements();
                                if (GameCanvas.panel2 != null)
                                {
                                    GameCanvas.panel2.vFriendGD.removeAllElements();
                                }
                                int friendMoneyGD = msg.reader().readInt();
                                sbyte itemCount = msg.reader().readByte();
                                Res.outz("item size = " + itemCount);
                                for (int j = 0; j < itemCount; j++)
                                {
                                    Item item = new Item();
                                    item.template = ItemTemplates.get(msg.reader().readShort());
                                    item.quantity = msg.reader().readInt();
                                    int optCount = msg.reader().readUnsignedByte();
                                    if (optCount != 0)
                                    {
                                        item.itemOption = new ItemOption[optCount];
                                        for (int k = 0; k < item.itemOption.Length; k++)
                                        {
                                            int optId = msg.reader().readUnsignedByte();
                                            int optParam = msg.reader().readUnsignedShort();
                                            if (optId != -1)
                                            {
                                                item.itemOption[k] = new ItemOption(optId, optParam);
                                                item.compare = GameCanvas.panel.getCompare(item);
                                            }
                                        }
                                    }
                                    if (GameCanvas.panel2 != null)
                                    {
                                        GameCanvas.panel2.vFriendGD.addElement(item);
                                    }
                                    else
                                    {
                                        GameCanvas.panel.vFriendGD.addElement(item);
                                    }
                                }
                                if (GameCanvas.panel2 != null)
                                {
                                    GameCanvas.panel2.setTabGiaoDich(false);
                                    GameCanvas.panel2.friendMoneyGD = friendMoneyGD;
                                }
                                else
                                {
                                    GameCanvas.panel.friendMoneyGD = friendMoneyGD;
                                    if (GameCanvas.panel.currentTabIndex == 2)
                                    {
                                        GameCanvas.panel.setTabGiaoDich(false);
                                    }
                                }
                            }
                            else if (action == 7)
                            {
                                InfoDlg.hide();
                                if (GameCanvas.panel.isShow)
                                {
                                    GameCanvas.panel.hide();
                                }
                            }
                            return true;
                        }

                    case NetworkOpcodes.TRADE_ORDER:
                        {
                            GameCanvas.debug("SA49", 2);
                            GameScr.gI().typeTradeOrder = 2;
                            if (GameScr.gI().typeTrade >= 2 && GameScr.gI().typeTradeOrder >= 2)
                            {
                                InfoDlg.showWait();
                            }
                            return true;
                        }
                }
            }
            catch (Exception ex)
            {
                Res.err("[TradePacketHandler] Error handling cmd " + msg.command + ": " + ex.Message);
            }
            return false;
        }
    }
}

namespace Game1
{
    using System;

    /// <summary>
    /// ItemPacketHandler: Chuyên trách phân tích và xử lý các gói tin vật phẩm rơi/nhặt trên bản đồ (ItemMap).
    /// Opcodes: -21 (xóa item), -20 (mình nhặt item), -19 (người khác nhặt), -18 (mình vứt item), -14 (người khác vứt), 68 (item rớt xuống map).
    /// </summary>
    public class ItemPacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case NetworkOpcodes.ITEM_MAP_REMOVE:
                        {
                            GameCanvas.debug("SA60", 2);
                            short itemMapID = msg.reader().readShort();
                            for (int i = 0; i < GameScr.vItemMap.size(); i++)
                            {
                                if (((ItemMap)GameScr.vItemMap.elementAt(i)).itemMapID == itemMapID)
                                {
                                    GameScr.vItemMap.removeElementAt(i);
                                    break;
                                }
                            }
                            return true;
                        }

                    case NetworkOpcodes.ITEM_MAP_PICK:
                        {
                            GameCanvas.debug("SA61", 2);
                            Char.myCharz().itemFocus = null;
                            short itemMapID = msg.reader().readShort();
                            for (int j = 0; j < GameScr.vItemMap.size(); j++)
                            {
                                ItemMap itemMap = (ItemMap)GameScr.vItemMap.elementAt(j);
                                if (itemMap.itemMapID != itemMapID)
                                {
                                    continue;
                                }
                                itemMap.setPoint(Char.myCharz().cx, Char.myCharz().cy - 10);
                                string text = msg.reader().readUTF();
                                int quantity = 0;
                                try
                                {
                                    quantity = msg.reader().readShort();
                                    if (itemMap.template.type == ItemTypes.GOLD)
                                    {
                                        Char.myCharz().xu += quantity;
                                        Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                                    }
                                    else if (itemMap.template.type == ItemTypes.GEM)
                                    {
                                        Char.myCharz().luong += quantity;
                                        Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                                    }
                                    else if (itemMap.template.type == ItemTypes.LOCKED_GEM)
                                    {
                                        Char.myCharz().luongKhoa += quantity;
                                        Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                                    }
                                }
                                catch (Exception)
                                {
                                }

                                if (text.Equals(string.Empty))
                                {
                                    if (itemMap.template.type == ItemTypes.GOLD)
                                    {
                                        GameScr.startFlyText(((quantity >= 0) ? "+" : string.Empty) + quantity, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -2, mFont.YELLOW);
                                        SoundMn.gI().getItem();
                                    }
                                    else if (itemMap.template.type == ItemTypes.GEM)
                                    {
                                        GameScr.startFlyText(((quantity >= 0) ? "+" : string.Empty) + quantity, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -2, mFont.GREEN);
                                        SoundMn.gI().getItem();
                                    }
                                    else if (itemMap.template.type == ItemTypes.LOCKED_GEM)
                                    {
                                        GameScr.startFlyText(((quantity >= 0) ? "+" : string.Empty) + quantity, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -2, mFont.RED);
                                        SoundMn.gI().getItem();
                                    }
                                    else
                                    {
                                        GameScr.info1.addInfo(mResources.you_receive + " " + ((quantity <= 0) ? string.Empty : (quantity + " ")) + itemMap.template.name, 0);
                                        SoundMn.gI().getItem();
                                    }

                                    if (quantity > 0 && Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 4683)
                                    {
                                        ServerEffect.addServerEffect(55, Char.myCharz().petFollow.cmx, Char.myCharz().petFollow.cmy, 1);
                                        ServerEffect.addServerEffect(55, Char.myCharz().cx, Char.myCharz().cy, 1);
                                    }
                                }
                                else if (text.Length == 1)
                                {
                                    Cout.LogError3("strInf.Length =1:  " + text);
                                }
                                else
                                {
                                    GameScr.info1.addInfo(text, 0);
                                }
                                break;
                            }
                            return true;
                        }

                    case NetworkOpcodes.ITEM_MAP_OTHER_PICK:
                        {
                            GameCanvas.debug("SA62", 2);
                            short itemMapID = msg.reader().readShort();
                            Char target = GameScr.findCharInMap(msg.reader().readInt());
                            for (int k = 0; k < GameScr.vItemMap.size(); k++)
                            {
                                ItemMap itemMap = (ItemMap)GameScr.vItemMap.elementAt(k);
                                if (itemMap.itemMapID != itemMapID)
                                {
                                    continue;
                                }
                                if (target == null)
                                {
                                    return true;
                                }
                                itemMap.setPoint(target.cx, target.cy - 10);
                                if (itemMap.x < target.cx)
                                {
                                    target.cdir = -1;
                                }
                                else if (itemMap.x > target.cx)
                                {
                                    target.cdir = 1;
                                }
                                break;
                            }
                            return true;
                        }

                    case NetworkOpcodes.ITEM_MAP_DROP:
                        {
                            GameCanvas.debug("SA63", 2);
                            int bagIndex = msg.reader().readByte();
                            GameScr.vItemMap.addElement(new ItemMap(msg.reader().readShort(), Char.myCharz().arrItemBag[bagIndex].template.id, Char.myCharz().cx, Char.myCharz().cy, msg.reader().readShort(), msg.reader().readShort()));
                            Char.myCharz().arrItemBag[bagIndex] = null;
                            return true;
                        }

                    case NetworkOpcodes.ITEM_MAP_OTHER_DROP:
                        {
                            GameCanvas.debug("SA64", 2);
                            Char thrower = GameScr.findCharInMap(msg.reader().readInt());
                            if (thrower == null)
                            {
                                return true;
                            }
                            GameScr.vItemMap.addElement(new ItemMap(msg.reader().readShort(), msg.reader().readShort(), thrower.cx, thrower.cy, msg.reader().readShort(), msg.reader().readShort()));
                            return true;
                        }

                    case NetworkOpcodes.ITEM_MAP_ADD:
                        {
                            Res.outz("ADD ITEM TO MAP --------------------------------------");
                            GameCanvas.debug("SA6333", 2);
                            short itemMapID = msg.reader().readShort();
                            short itemTemplateID = msg.reader().readShort();
                            int x = msg.reader().readShort();
                            int y = msg.reader().readShort();
                            int playerId = msg.reader().readInt();
                            short r = 0;
                            if (playerId == -2)
                            {
                                r = msg.reader().readShort();
                            }
                            ItemMap itemMap = new ItemMap(playerId, itemMapID, itemTemplateID, x, y, r);
                            bool flag = false;
                            for (int l = 0; l < GameScr.vItemMap.size(); l++)
                            {
                                ItemMap existing = (ItemMap)GameScr.vItemMap.elementAt(l);
                                if (existing != null && existing.itemMapID == itemMap.itemMapID)
                                {
                                    flag = true;
                                    break;
                                }
                            }
                            if (!flag)
                            {
                                GameScr.vItemMap.addElement(itemMap);
                            }
                            return true;
                        }
                }
            }
            catch (Exception ex)
            {
                Res.err("[ItemPacketHandler] Error handling cmd " + msg.command + ": " + ex.Message);
            }
            return false;
        }
    }
}

namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;

    public partial class Panel
    {
        public void paintInventory(mGraphics g)
        {
            if (Char.myCharz() == null || Char.myCharz().arrItemBody == null || Char.myCharz().arrItemBag == null)
            {
                return;
            }
            bool flag = true;
            if (flag && isnewInventory)
            {
                Item[] arrItemBody = Char.myCharz().arrItemBody;
                Item[] arrItemBag = Char.myCharz().arrItemBag;
                g.setColor(16711680);
                int num = arrItemBody.Length + arrItemBag.Length;
                int num2 = num / 20 + ((num % 20 > 0) ? 1 : 0) + 1;
                int num3 = 0;
                TAB_W_NEW = wScroll / num2;
                for (int i = num3; i < num2; i++)
                {
                    int num4 = ((i == newSelected && selected == 0) ? ((GameCanvas.gameTick % 10 < 7) ? (-1) : 0) : 0);
                    g.setColor((i != newSelected) ? 15723751 : 16383818);
                    g.fillRect(xScroll + i * TAB_W_NEW, 89 + num4 - 10, TAB_W_NEW - 1, 21);
                    if (i == newSelected)
                    {
                        g.setColor(13524492);
                        int x = xScroll + i * TAB_W_NEW;
                        int num5 = 89 + num4 - 10 + 21;
                        g.fillRect(x, num5 - 3, TAB_W_NEW - 1, 3);
                    }
                    mFont.tahoma_7_grey.drawString(g, string.Empty + (i + 1), xScroll + i * TAB_W_NEW + TAB_W_NEW / 2, 91 + num4 - 10, mFont.CENTER);
                }
                num3 = 1;
                int num6 = xScroll;
                int num7 = yScroll + num3 * ITEM_HEIGHT;
                int num8 = 34;
                int num9 = ITEM_HEIGHT - 1;
                for (int j = 0; j < 4; j++)
                {
                    num6 = xScroll;
                    num7 = yScroll + (j + num3) * ITEM_HEIGHT;
                    bool flag2 = true;
                    for (int k = 0; k < 5; k++)
                    {
                        Item item = null;
                        int num10 = 0;
                        if (newSelected > 0)
                        {
                            num10 = (newSelected - 1) * 20;
                            if (j * 5 + k + num10 < arrItemBag.Length)
                            {
                                item = arrItemBag[j * 5 + k + num10];
                                num6 = xScroll + num8 * k;
                                int num11 = sellectInventory % 5;
                                int num12 = sellectInventory / 5;
                                if (newSelected > 0)
                                {
                                    g.setColor(15196114);
                                }
                                else
                                {
                                    g.setColor(9993045);
                                }
                                g.drawRect(num6, num7, num8, num9);
                                if (j == num12 && k == num11 && selected > 0)
                                {
                                    g.setColor(16383818);
                                    itemInvenNew = item;
                                }
                                g.fillRect(num6 + 2, num7 + 2, num8 - 3, num9 - 3);
                                if (item != null)
                                {
                                    int x2 = num6 + imgNew.getWidth() / 2;
                                    int y = num7;
                                    int num13 = 34;
                                    int h = ITEM_HEIGHT - 1;
                                    SmallImage.drawSmallImage(g, item.template.iconID, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                                    if (item.quantity > 1)
                                    {
                                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num6, num7 - mFont.tahoma_7_yellow.getHeight(), 1);
                                    }
                                    if (item.newItem && GameCanvas.gameTick % 10 > 5)
                                    {
                                        g.drawImage(imgNew, x2, y, 3);
                                    }
                                    if (item.itemOption != null)
                                    {
                                        for (int l = 0; l < item.itemOption.Length; l++)
                                        {
                                            if (item.itemOption[l] != null && item.itemOption[l].optionTemplate != null)
                                                paintOptSlotItem(g, item.itemOption[l].optionTemplate.id, item.itemOption[l].param, x2, y, num13, h);
                                        }
                                    }
                                }
                                if (!flag2)
                                {
                                    break;
                                }
                                continue;
                            }
                            flag2 = false;
                            break;
                        }
                        if (j * 5 + k < arrItemBody.Length)
                        {
                            item = arrItemBody[j * 5 + k];
                            flag2 = false;
                        }
                        else
                        {
                            flag2 = false;
                        }
                        break;
                    }
                }
                num3 = ((newSelected != 0) ? 5 : 3);
                int num14 = yScroll + num3 * ITEM_HEIGHT + 5;
                int num15 = 2;
                if (newSelected == 0)
                {
                    num15 = 4;
                }
                num6 = xScroll;
                num7 = yScroll + num3 * ITEM_HEIGHT;
                num8 = 34;
                num9 = ITEM_HEIGHT - 1;
                if (newSelected == 0)
                {
                    g.setColor(15196114);
                    num3 = 1;
                    nTableItem = 10;
                    int num16 = 5;
                    if (eBanner != null)
                    {
                        eBanner.paint(g);
                        eBanner.x = num6 + 34 + 34;
                        eBanner.y = num7 + num9 - 25;
                    }
                    for (int m = 0; m < 10; m++)
                    {
                        Item item2 = null;
                        item2 = arrItemBody[m];
                        if (m < 5)
                        {
                            num16 = 0;
                            num6 = xScroll;
                            num7 = yScroll + (m + num3) * ITEM_HEIGHT;
                        }
                        else
                        {
                            num16 = 5;
                            num6 = xScroll + 4 * num8;
                            num7 = yScroll + (m - num16 + num3) * ITEM_HEIGHT;
                        }
                        g.setColor(15196114);
                        g.drawRect(num6, num7, num8, num9);
                        if (sellectInventory == m)
                        {
                            itemInvenNew = item2;
                            g.setColor(16383818);
                        }
                        else
                        {
                            g.setColor(9993045);
                        }
                        g.fillRect(num6 + 2, num7 + 2, num8 - 3, num9 - 3);
                        if (item2 == null)
                        {
                            screenTab6.drawFrame(m, num6 + num8 / 2 - 8, num7 + num9 / 2 - 8, 0, mGraphics.TOP | mGraphics.LEFT, g);
                        }
                        if (item2 != null)
                        {
                            SmallImage.drawSmallImage(g, item2.template.iconID, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                            if (item2.quantity > 1)
                            {
                                mFont.tahoma_7_yellow.drawString(g, string.Empty + item2.quantity, num6 + 4 * num8, num7 - mFont.tahoma_7_yellow.getHeight(), 1);
                            }
                        }
                    }
                    num3 = 1;
                    num6 = xScroll + 34;
                    num7 = yScroll + num3 * ITEM_HEIGHT;
                    num8 = 102;
                    num9 = 4 * (ITEM_HEIGHT - 1);
                    Char.myCharz().paintCharBody(g, num6 + 34 + 17, num7 + num9 - 25, 1, 0, true);
                    num3 = 3;
                    num15 = 2;
                    num6 = xScroll + 34;
                    num7 = yScroll + (1 + num3) * ITEM_HEIGHT - 1;
                    num8 = 102;
                    num9 = ITEM_HEIGHT * num15;
                    g.setColor(15196114);
                    g.drawRect(num6, num7, num8, num9);
                    g.setColor(9993045);
                    g.fillRect(num6 + 1, num7 + 1, num8 - 2, num9 - 2);
                    paintItemBodyBagInfo(g, num6 + 3, num7 - 2);
                    num3 = ((newSelected != 0) ? 5 : 6);
                    num14 = yScroll + num3 * ITEM_HEIGHT;
                    g.setColor(15196114);
                    if (newSelected == 0)
                    {
                        num15 = 1;
                    }
                    g.drawRect(xScroll, num14, wScroll, ITEM_HEIGHT * num15);
                    g.setColor(16777215);
                    g.fillRect(xScroll + 1, num14 + 1, wScroll - 2, ITEM_HEIGHT * num15 - 2);
                }
                if (itemInvenNew != null && itemInvenNew.itemOption != null)
                {
                    string text = string.Empty;
                    mFont mFont2 = mFont.tahoma_7_green2;
                    if (itemInvenNew.itemOption != null)
                    {
                        for (int n = 0; n < itemInvenNew.itemOption.Length; n++)
                        {
                            if (itemInvenNew.itemOption[n].optionTemplate.id == 72)
                            {
                                text = " [+" + itemInvenNew.itemOption[n].param + "]";
                            }
                            if (itemInvenNew.itemOption[n].optionTemplate.id == 41)
                            {
                                if (itemInvenNew.itemOption[n].param == 1)
                                {
                                    mFont2 = GetFont(0);
                                }
                                else if (itemInvenNew.itemOption[n].param == 2)
                                {
                                    mFont2 = GetFont(2);
                                }
                                else if (itemInvenNew.itemOption[n].param == 3)
                                {
                                    mFont2 = GetFont(8);
                                }
                                else if (itemInvenNew.itemOption[n].param == 4)
                                {
                                    mFont2 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont2.drawString(g, itemInvenNew.template.name + text, xScroll + 5, num14 + 1, 0);
                    if (itemInvenNew != lastItemInvenNew)
                    {
                        lastItemInvenNew = itemInvenNew;
                        string text2 = string.Empty;
                        if (itemInvenNew.itemOption != null)
                        {
                            for (int num17 = 0; num17 < itemInvenNew.itemOption.Length; num17++)
                            {
                                if (itemInvenNew.itemOption[num17] != null && (itemInvenNew.itemOption[num17].optionTemplate == null || (itemInvenNew.itemOption[num17].optionTemplate.id != 102 && itemInvenNew.itemOption[num17].optionTemplate.id != 107)))
                                {
                                    string optStr = itemInvenNew.itemOption[num17].getOptionString();
                                    if (!string.IsNullOrEmpty(optStr))
                                    {
                                        text2 = string.IsNullOrEmpty(text2) ? optStr : (text2 + "," + optStr);
                                        if (text2.Contains(",")) break;
                                    }
                                }
                            }
                            cachedOptionFont = mFont.tahoma_7_blue;
                            if (itemInvenNew.compare < 0 && itemInvenNew.template.type != 5)
                            {
                                cachedOptionFont = mFont.tahoma_7_red;
                            }
                        }
                        try
                        {
                            if (cachedOptionFont.getWidth(text2) > wScroll)
                            {
                                text2 = cachedOptionFont.splitFontArray(text2, wScroll)[0];
                            }
                        }
                        catch (Exception ex)
                        {
                            logPanelPaintErrorThrottled("paintItemOptionSplit", ex);
                        }
                        cachedOptionText = text2;
                    }
                    cachedOptionFont.drawString(g, cachedOptionText, xScroll + 5, num14 + 11, mFont.LEFT);
                }
            }
            if (flag && isnewInventory)
            {
                return;
            }
            g.setColor(16711680);
            Item[] arrItemBody2 = Char.myCharz().arrItemBody;
            Item[] arrItemBag2 = Char.myCharz().arrItemBag;
            currentListLength = checkCurrentListLength(arrItemBody2.Length + arrItemBag2.Length);
            int num18 = (arrItemBody2.Length + arrItemBag2.Length) / 20 + (((arrItemBody2.Length + arrItemBag2.Length) % 20 > 0) ? 1 : 0);
            TAB_W_NEW = wScroll / num18;
            for (int num19 = 0; num19 < num18; num19++)
            {
                int num20 = ((num19 == newSelected && selected == 0) ? ((GameCanvas.gameTick % 10 < 7) ? (-1) : 0) : 0);
                g.setColor((num19 != newSelected) ? 15723751 : 16383818);
                g.fillRect(xScroll + num19 * TAB_W_NEW, 89 + num20 - 10, TAB_W_NEW - 1, 21);
                if (num19 == newSelected)
                {
                    g.setColor(13524492);
                    int x3 = xScroll + num19 * TAB_W_NEW;
                    int num21 = 89 + num20 - 10 + 21;
                    g.fillRect(x3, num21 - 3, TAB_W_NEW - 1, 3);
                }
                mFont.tahoma_7_grey.drawString(g, string.Empty + (num19 + 1), xScroll + num19 * TAB_W_NEW + TAB_W_NEW / 2, 91 + num20 - 10, mFont.CENTER);
            }
            g.setClip(xScroll, yScroll + 21, wScroll, hScroll - 21);
            g.translate(0, -cmy);
            for (int num22 = 1; num22 < currentListLength; num22++)
            {
                try
                {
                    int num23 = xScroll + 36;
                    int num24 = yScroll + num22 * ITEM_HEIGHT;
                    int num25 = wScroll - 36;
                    int h2 = ITEM_HEIGHT - 1;
                    int num26 = xScroll;
                    int num27 = yScroll + num22 * ITEM_HEIGHT;
                    int num28 = 34;
                    int num29 = ITEM_HEIGHT - 1;
                    if (num24 - cmy > yScroll + hScroll || num24 - cmy < yScroll - ITEM_HEIGHT)
                    {
                        continue;
                    }
                    bool inventorySelect_isbody = GetInventorySelect_isbody(num22, newSelected, Char.myCharz().arrItemBody);
                    int inventorySelect_body = GetInventorySelect_body(num22, newSelected);
                    int inventorySelect_bag = GetInventorySelect_bag(num22, newSelected, Char.myCharz().arrItemBody);
                    g.setColor((num22 == selected) ? 16383818 : ((!inventorySelect_isbody) ? 15723751 : 15196114));
                    g.fillRect(num23, num24, num25, h2);
                    g.setColor((num22 == selected) ? 9541120 : ((!inventorySelect_isbody) ? 11837316 : 9993045));
                    Item item3 = null;
                    if (inventorySelect_isbody)
                    {
                        if (arrItemBody2 != null && inventorySelect_body >= 0 && inventorySelect_body < arrItemBody2.Length)
                        {
                            item3 = arrItemBody2[inventorySelect_body];
                        }
                    }
                    else
                    {
                        if (arrItemBag2 != null && inventorySelect_bag >= 0 && inventorySelect_bag < arrItemBag2.Length)
                        {
                            item3 = arrItemBag2[inventorySelect_bag];
                        }
                    }
                    if (item3 != null && item3.itemOption != null)
                    {
                        for (int num30 = 0; num30 < item3.itemOption.Length; num30++)
                        {
                            if (item3.itemOption[num30] != null && item3.itemOption[num30].optionTemplate != null
                                && item3.itemOption[num30].optionTemplate.id == 72 && item3.itemOption[num30].param > 0)
                            {
                                byte id = (byte)GetColor_Item_Upgrade(item3.itemOption[num30].param);
                                int color_ItemBg = GetColor_ItemBg(id);
                                if (color_ItemBg != -1)
                                {
                                    g.setColor((num22 != selected) ? GetColor_ItemBg(id) : GetColor_ItemBg(id));
                                }
                            }
                        }
                    }
                    g.fillRect(num26, num27, num28, num29);
                    if (item3 != null && item3.isSelect && GameCanvas.panel.type == 12)
                    {
                        g.setColor((num22 != selected) ? 6047789 : 7040779);
                        g.fillRect(num26, num27, num28, num29);
                    }
                    if (item3 == null)
                    {
                        continue;
                    }
                    string text3 = string.Empty;
                    mFont mFont4 = mFont.tahoma_7_green2;
                    if (item3.itemOption != null)
                    {
                        for (int num31 = 0; num31 < item3.itemOption.Length; num31++)
                        {
                            if (item3.itemOption[num31] == null || item3.itemOption[num31].optionTemplate == null)
                            {
                                continue;
                            }
                            if (item3.itemOption[num31].optionTemplate.id == 72)
                            {
                                text3 = " [+" + item3.itemOption[num31].param + "]";
                            }
                            if (item3.itemOption[num31].optionTemplate.id == 107)
                            {
                                paintItemStar(g, item3.itemOption[num31].param.ToString(), num23 + 125, num24 + 1);
                            }
                            if (item3.itemOption[num31].optionTemplate.id == 41)
                            {
                                if (item3.itemOption[num31].param == 1)
                                {
                                    mFont4 = GetFont(0);
                                }
                                else if (item3.itemOption[num31].param == 2)
                                {
                                    mFont4 = GetFont(2);
                                }
                                else if (item3.itemOption[num31].param == 3)
                                {
                                    mFont4 = GetFont(8);
                                }
                                else if (item3.itemOption[num31].param == 4)
                                {
                                    mFont4 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont4.drawString(g, item3.template.name + text3, num23 + 5, num24 + 1, 0);
                    string text4 = string.Empty;
                    if (item3.itemOption != null)
                    {
                        for (int num32 = 0; num32 < item3.itemOption.Length; num32++)
                        {
                            if (item3.itemOption[num32] != null && (item3.itemOption[num32].optionTemplate == null || (item3.itemOption[num32].optionTemplate.id != 102 && item3.itemOption[num32].optionTemplate.id != 107)))
                            {
                                string optStr = item3.itemOption[num32].getOptionString();
                                if (!string.IsNullOrEmpty(optStr))
                                {
                                    text4 = string.IsNullOrEmpty(text4) ? optStr : (text4 + "," + optStr);
                                    if (text4.Contains(",")) break;
                                }
                            }
                        }
                        if (!string.IsNullOrEmpty(text4))
                        {
                            mFont mFont5 = mFont.tahoma_7_blue;
                            if (item3.compare < 0 && item3.template.type != 5)
                            {
                                mFont5 = mFont.tahoma_7_red;
                            }
                            mFont5.drawString(g, text4, num23 + 5, num24 + 11, mFont.LEFT);
                        }
                    }
                    SmallImage.drawSmallImage(g, item3.template.iconID, num26 + num28 / 2, num27 + num29 / 2, 0, 3);
                    if (item3.itemOption != null)
                    {
                        for (int num33 = 0; num33 < item3.itemOption.Length; num33++)
                        {
                            if (item3.itemOption[num33] != null && item3.itemOption[num33].optionTemplate != null)
                            {
                                paintOptItem(g, item3.itemOption[num33].optionTemplate.id, item3.itemOption[num33].param, num26, num27, num28, num29);
                            }
                        }
                        for (int num34 = 0; num34 < item3.itemOption.Length; num34++)
                        {
                            if (item3.itemOption[num34] != null && item3.itemOption[num34].optionTemplate != null)
                            {
                                paintOptSlotItem(g, item3.itemOption[num34].optionTemplate.id, item3.itemOption[num34].param, num26, num27, num28, num29);
                            }
                        }
                    }
                    if (item3.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item3.quantity, num26 + num28, num27 + num29 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
                catch (Exception ex)
            {
                logPanelPaintErrorThrottled("paintInventoryTab", ex);
            }
            }
            paintScrollArrow(g);
        }
    }
}

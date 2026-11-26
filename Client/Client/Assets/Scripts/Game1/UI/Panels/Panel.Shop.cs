namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;

    public partial class Panel
    {
        public void paintShop(mGraphics g)
        {
            try
            {
                if (type == 1 && currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null && typeShop != 2)
                {
                    paintInventory(g);
                    return;
                }
                g.setColor(16711680);
                g.setClip(xScroll, yScroll, wScroll, hScroll);
                if (typeShop == 2 && Equals(GameCanvas.panel))
                {
                    if (currentTabIndex <= 3 && GameCanvas.isTouch)
                    {
                        if (cmy < -50)
                        {
                            GameCanvas.paintShukiren(xScroll + wScroll / 2, yScroll + 30, g);
                        }
                        else if (cmy < 0)
                        {
                            mFont.tahoma_7_grey.drawString(g, mResources.getDown, xScroll + wScroll / 2, yScroll + 15, 2);
                        }
                        else if (cmyLim >= 0)
                        {
                            if (cmy > cmyLim + 50)
                            {
                                GameCanvas.paintShukiren(xScroll + wScroll / 2, yScroll + hScroll - 30, g);
                            }
                            else if (cmy > cmyLim)
                            {
                                mFont.tahoma_7_grey.drawString(g, mResources.getUp, xScroll + wScroll / 2, yScroll + hScroll - 25, 2);
                            }
                        }
                    }
                    if (Char.myCharz().arrItemShop == null || Char.myCharz().arrItemShop[currentTabIndex] == null || (Char.myCharz().arrItemShop[currentTabIndex].Length == 0 && type != 17))
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.notYetSell, xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                        return;
                    }
                }
                g.translate(0, -cmy);
                if (Char.myCharz().arrItemShop == null)
                {
                    return;
                }
                Item[] array = (currentTabIndex >= 0 && currentTabIndex < Char.myCharz().arrItemShop.Length) ? Char.myCharz().arrItemShop[currentTabIndex] : null;
                if (typeShop == 2 && (currentTabIndex == 4 || type == 17))
                {
                    if (Char.myCharz().arrItemShop.Length > 4)
                    {
                        array = Char.myCharz().arrItemShop[4];
                    }
                    if (array == null || array.Length == 0)
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.notYetSell, xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                        return;
                    }
                }
                if (array == null || array.Length == 0)
                {
                    mFont.tahoma_7_grey.drawString(g, "Hiện tại chưa có vật phẩm trong mục này", xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                    return;
                }
                int num = array.Length;
                if (num == 0)
                {
                    mFont.tahoma_7_grey.drawString(g, "Hiện tại chưa có vật phẩm trong mục này", xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                    return;
                }
                for (int i = 0; i < num; i++)
                {
                    int num2 = xScroll + 26;
                    int num3 = yScroll + i * ITEM_HEIGHT;
                    int num4 = wScroll - 26;
                    int h = ITEM_HEIGHT - 1;
                    int num5 = xScroll;
                    int num6 = yScroll + i * ITEM_HEIGHT;
                    int num7 = 24;
                    int num8 = ITEM_HEIGHT - 1;
                    if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                    {
                        continue;
                    }
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(num2, num3, num4, h);
                    g.setColor((i != selected) ? 9993045 : 9541120);
                    g.fillRect(num5, num6, num7, num8);
                    Item item = array[i];
                    if (item != null)
                    {
                        string text = string.Empty;
                        mFont mFont2 = mFont.tahoma_7_green2;
                        if (item.isMe != 0 && typeShop == 2 && currentTabIndex <= 3 && !Equals(GameCanvas.panel2) && item.template != null && item.template.name != null && item.template.name.Length < 20)
                        {
                            mFont2 = mFont.tahoma_7b_green;
                        }
                        if (item.itemOption != null)
                        {
                            for (int j = 0; j < item.itemOption.Length; j++)
                            {
                                if (item.itemOption[j] == null || item.itemOption[j].optionTemplate == null)
                                {
                                    continue;
                                }
                                if (item.itemOption[j].optionTemplate.id == 72)
                                {
                                    text = " [+" + item.itemOption[j].param + "]";
                                }
                                if (item.itemOption[j].optionTemplate.id == 107)
                                {
                                    paintItemStar(g, item.itemOption[j].param.ToString(), num2 + 125, num3 + 1);
                                }
                                if (item.itemOption[j].optionTemplate.id == 41)
                                {
                                    if (item.itemOption[j].param == 1)
                                    {
                                        mFont2 = GetFont(0);
                                    }
                                    else if (item.itemOption[j].param == 2)
                                    {
                                        mFont2 = GetFont(2);
                                    }
                                    else if (item.itemOption[j].param == 3)
                                    {
                                        mFont2 = GetFont(8);
                                    }
                                    else if (item.itemOption[j].param == 4)
                                    {
                                        mFont2 = GetFont(7);
                                    }
                                }
                            }
                        }
                        if (item.template != null && item.template.name != null)
                        {
                            mFont2.drawString(g, item.template.name + text, num2 + 5, num3 + 1, 0);
                        }
                        string text2 = string.Empty;
                        if (item.itemOption != null && item.itemOption.Length >= 1)
                        {
                            for (int l = 0; l < item.itemOption.Length; l++)
                            {
                                if (item.itemOption[l] != null && (item.itemOption[l].optionTemplate == null || (item.itemOption[l].optionTemplate.id != 102 && item.itemOption[l].optionTemplate.id != 107)))
                                {
                                    string optStr = item.itemOption[l].getOptionString();
                                    if (!string.IsNullOrEmpty(optStr))
                                    {
                                        text2 = string.IsNullOrEmpty(text2) ? optStr : (text2 + "," + optStr);
                                    }
                                }
                            }
                            if (!string.IsNullOrEmpty(text2))
                            {
                                mFont mFont3 = mFont.tahoma_7_blue;
                                if (item.compare < 0 && item.template != null && item.template.type != 5)
                                {
                                    mFont3 = mFont.tahoma_7_red;
                                }
                                if (typeShop != 2 || (typeShop == 2 && item.buyType <= 1))
                                {
                                    mFont3.drawString(g, text2, num2 + 5, num3 + 11, 0);
                                }
                            }
                        }
                        if (item.buySpec > 0)
                        {
                            SmallImage.drawSmallImage(g, item.iconSpec, num2 + num4 - 7, num3 + 9, 0, 3);
                            mFont.tahoma_7b_blue.drawString(g, Res.formatNumber(item.buySpec), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                        }
                        if (item.buyCoin != 0 || item.buyGold != 0)
                        {
                            if (typeShop != 2 && item.powerRequire == 0)
                            {
                                if (item.buyCoin > 0 && item.buyGold > 0)
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7 + 11, 3);
                                        mFont.tahoma_7b_green.drawString(g, Res.formatNumber(item.buyGold), num2 + num4 - 15, num3 + 12, mFont.RIGHT);
                                    }
                                }
                                else
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7, 3);
                                        mFont.tahoma_7b_green.drawString(g, Res.formatNumber(item.buyGold), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                }
                            }
                            if (typeShop == 2 && currentTabIndex <= 3 && !Equals(GameCanvas.panel2))
                            {
                                if (item.buyCoin > 0 && item.buyGold > 0)
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont2 = ((Char.myCharz().xu >= item.buyCoin) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7 + 11, 3);
                                        mFont2 = ((Char.myCharz().luong >= item.buyGold) ? mFont.tahoma_7b_green : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 12, mFont.RIGHT);
                                    }
                                }
                                else
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont2 = ((Char.myCharz().xu >= item.buyCoin) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7, 3);
                                        mFont2 = ((Char.myCharz().luong >= item.buyGold) ? mFont.tahoma_7b_green : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    try
                                    {
                                        if (!string.IsNullOrEmpty(item.nameNguoiKyGui))
                                        {
                                            mFont2 = mFont.tahoma_7b_green;
                                            mFont2.drawString(g, item.nameNguoiKyGui, num2 + num4, num3 + 1 + mFont.tahoma_7b_red.getHeight(), mFont.RIGHT);
                                        }
                                    }
                                    catch (Exception ex)
                                    {
                                        logPanelPaintErrorThrottled("paintConsignmentName", ex);
                                    }
                                }
                            }
                        }
                        if (item.template != null)
                        {
                            SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                        }
                        if (item.quantity > 1)
                        {
                            mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                        }
                        if (item.newItem && GameCanvas.gameTick % 10 > 5)
                        {
                            g.drawImage(imgNew, num5 + num7 / 2, num3 + 19, 3);
                        }
                    }
                    if (typeShop != 2 || (!Equals(GameCanvas.panel2) && currentTabIndex != 4) || item.buyType == 0)
                    {
                        continue;
                    }
                    if (item.buyType == 1)
                    {
                        mFont.tahoma_7_green.drawString(g, mResources.dangban, num2 + num4 - 5, num3 + 1, mFont.RIGHT);
                        if (item.buyCoin != -1)
                        {
                            g.drawImage(imgXu, num2 + num4 - 7, num3 + 19, 3);
                            mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 13, mFont.RIGHT);
                        }
                        else if (item.buyGold != -1)
                        {
                            g.drawImage(imgLuongKhoa, num2 + num4 - 7, num3 + 17, 3);
                            mFont.tahoma_7b_red.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                        }
                    }
                    else if (item.buyType == 2)
                    {
                        mFont.tahoma_7b_blue.drawString(g, mResources.daban, num2 + num4 - 5, num3 + 1, mFont.RIGHT);
                        if (item.buyCoin != -1)
                        {
                            g.drawImage(imgXu, num2 + num4 - 7, num3 + 17, 3);
                            mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                        }
                        else if (item.buyGold != -1)
                        {
                            g.drawImage(imgLuongKhoa, num2 + num4 - 7, num3 + 17, 3);
                            mFont.tahoma_7b_red.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                        }
                    }
                }
                paintScrollArrow(g);
            }
            catch (Exception ex)
            {
                Cout.LogError("Loi paintShop: " + ex.ToString());
            }
        }
    }
}

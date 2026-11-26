namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;

    public partial class Panel
    {
        public void paintBox(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            try
            {
                Item[] arrItemBox = Char.myCharz().arrItemBox;
                currentListLength = checkCurrentListLength(arrItemBox.Length);
                int num = arrItemBox.Length / 20 + ((arrItemBox.Length % 20 > 0) ? 1 : 0);
                TAB_W_NEW = wScroll / num;
                for (int i = 0; i < currentListLength; i++)
                {
                    int num2 = xScroll + 36;
                    int num3 = yScroll + i * ITEM_HEIGHT;
                    int num4 = wScroll - 36;
                    int h = ITEM_HEIGHT - 1;
                    int num5 = xScroll;
                    int num6 = yScroll + i * ITEM_HEIGHT;
                    int num7 = 34;
                    int num8 = ITEM_HEIGHT - 1;
                    if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                    {
                        continue;
                    }
                    if (i == 0)
                    {
                        for (int j = 0; j < num; j++)
                        {
                            int num9 = ((j == newSelected && selected == 0) ? ((GameCanvas.gameTick % 10 < 7) ? (-1) : 0) : 0);
                            g.setColor((j != newSelected) ? 15723751 : 16383818);
                            g.fillRect(xScroll + j * TAB_W_NEW, num3 + 9 + num9, TAB_W_NEW - 1, 14);
                            mFont.tahoma_7_grey.drawString(g, string.Empty + j, xScroll + j * TAB_W_NEW + TAB_W_NEW / 2, yScroll + 11 + num9, mFont.CENTER);
                        }
                        continue;
                    }
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(num2, num3, num4, h);
                    g.setColor((i != selected) ? 9993045 : 9541120);
                    int inventorySelect_body = GetInventorySelect_body(i, newSelected);
                    Item item = arrItemBox[inventorySelect_body];
                    if (item != null && item.itemOption != null)
                    {
                        for (int k = 0; k < item.itemOption.Length; k++)
                        {
                            if (item.itemOption[k] == null || item.itemOption[k].optionTemplate == null) continue;
                            if (item.itemOption[k].optionTemplate.id == 72 && item.itemOption[k].param > 0)
                            {
                                sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[k].param);
                                int color_ItemBg = GetColor_ItemBg(color_Item_Upgrade);
                                if (color_ItemBg != -1)
                                {
                                    g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                                }
                            }
                        }
                    }
                    g.fillRect(num5, num6, num7, num8);
                    if (item == null)
                    {
                        continue;
                    }
                    string text = string.Empty;
                    mFont mFont2 = mFont.tahoma_7_green2;
                    if (item.itemOption != null)
                    {
                        for (int l = 0; l < item.itemOption.Length; l++)
                        {
                            if (item.itemOption[l] == null || item.itemOption[l].optionTemplate == null) continue;
                            if (item.itemOption[l].optionTemplate.id == 72)
                            {
                                text = " [+" + item.itemOption[l].getOptionString() + "]";
                            }
                            if (item.itemOption[l].optionTemplate.id == 107)
                            {
                                paintItemStar(g, item.itemOption[l].param.ToString(), num2 + 125, num3 + 1);
                            }
                            if (item.itemOption[l].optionTemplate.id == 41)
                            {
                                if (item.itemOption[l].param == 1)
                                {
                                    mFont2 = GetFont(0);
                                }
                                else if (item.itemOption[l].param == 2)
                                {
                                    mFont2 = GetFont(2);
                                }
                                else if (item.itemOption[l].param == 3)
                                {
                                    mFont2 = GetFont(8);
                                }
                                else if (item.itemOption[l].param == 4)
                                {
                                    mFont2 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont2.drawString(g, item.template.name + text, num2 + 5, num3 + 1, 0);
                    string text2 = string.Empty;
                    if (item.itemOption != null)
                    {
                        for (int m = 0; m < item.itemOption.Length; m++)
                        {
                            if (item.itemOption[m] != null && (item.itemOption[m].optionTemplate == null || (item.itemOption[m].optionTemplate.id != 102 && item.itemOption[m].optionTemplate.id != 107)))
                            {
                                string optStr = item.itemOption[m].getOptionString();
                                if (!string.IsNullOrEmpty(optStr))
                                {
                                    text2 = string.IsNullOrEmpty(text2) ? optStr : (text2 + "," + optStr);
                                }
                            }
                        }
                        if (!string.IsNullOrEmpty(text2))
                        {
                            mFont mFont3 = mFont.tahoma_7_blue;
                            if (item.compare < 0 && item.template.type != 5)
                            {
                                mFont3 = mFont.tahoma_7_red;
                            }
                            mFont3.drawString(g, text2, num2 + 5, num3 + 11, mFont.LEFT);
                        }
                    }
                    SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                    if (item.itemOption != null)
                    {
                        for (int n = 0; n < item.itemOption.Length; n++)
                        {
                            if (item.itemOption[n] != null && item.itemOption[n].optionTemplate != null)
                                paintOptItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8);
                        }
                        for (int num10 = 0; num10 < item.itemOption.Length; num10++)
                        {
                            if (item.itemOption[num10] != null && item.itemOption[num10].optionTemplate != null)
                                paintOptSlotItem(g, item.itemOption[num10].optionTemplate.id, item.itemOption[num10].param, num5, num6, num7, num8);
                        }
                    }
                    if (item.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
            }
            catch (Exception ex)
            {
                logPanelPaintErrorThrottled("paintPetInventory", ex);
            }
            paintScrollArrow(g);
        }
    }
}

namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;

    public partial class Panel
    {
        public void paintCombine(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            if (vItemCombine.size() == 0)
            {
                if (combineInfo != null)
                {
                    for (int i = 0; i < combineInfo.Length; i++)
                    {
                        mFont.tahoma_7b_dark.drawString(g, combineInfo[i], xScroll + wScroll / 2, yScroll + hScroll / 2 - combineInfo.Length * 14 / 2 + i * 14 + 5, 2);
                    }
                }
                return;
            }
            for (int j = 0; j < vItemCombine.size() + 1; j++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + j * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int num4 = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + j * ITEM_HEIGHT;
                int num7 = 34;
                int num8 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                if (j == vItemCombine.size())
                {
                    if (vItemCombine.size() > 0)
                    {
                        if (!GameCanvas.isTouch && j == selected)
                        {
                            g.setColor(16383818);
                            g.fillRect(num5, num2, wScroll, num4 + 2);
                        }
                        if ((j == selected && keyTouchCombine == 1) || (!GameCanvas.isTouch && j == selected))
                        {
                            g.drawImage(GameScr.imgLbtnFocus, xScroll + wScroll / 2, num2 + num4 / 2 + 1, StaticObj.VCENTER_HCENTER);
                            mFont.tahoma_7b_green2.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                        else
                        {
                            g.drawImage(GameScr.imgLbtn, xScroll + wScroll / 2, num2 + num4 / 2 + 1, StaticObj.VCENTER_HCENTER);
                            mFont.tahoma_7b_dark.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                    }
                    continue;
                }
                g.setColor((j != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, num4);
                g.setColor((j != selected) ? 9993045 : 9541120);
                Item item = (Item)vItemCombine.elementAt(j);
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
                                g.setColor((j != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
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
                            text = " [+" + item.itemOption[l].param + "]";
                        }
                        if (item.itemOption[l].optionTemplate.id == 107)
                        {
                            paintItemStar(g, item.itemOption[l].param.ToString(), num + 125, num2 + 1);
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
                mFont2.drawString(g, item.template.name + text, num + 5, num2 + 1, 0);
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
                        mFont3.drawString(g, text2, num + 5, num2 + 11, mFont.LEFT);
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
                    for (int num9 = 0; num9 < item.itemOption.Length; num9++)
                    {
                        if (item.itemOption[num9] != null && item.itemOption[num9].optionTemplate != null)
                            paintOptSlotItem(g, item.itemOption[num9].optionTemplate.id, item.itemOption[num9].param, num5, num6, num7, num8);
                    }
                }
                if (item.quantity > 1)
                {
                    mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                }
            }
            paintScrollArrow(g);
        }
    }
}

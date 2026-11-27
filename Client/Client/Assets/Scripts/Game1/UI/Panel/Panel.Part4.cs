namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        public void paintTop(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Đang tải bảng xếp hạng...", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                return;
            }
            int num = (cmy + hScroll) / 24 + 1;
            if (num < hScroll / 24 + 1)
            {
                num = hScroll / 24 + 1;
            }
            if (num > currentListLength)
            {
                num = currentListLength;
            }
            int num2 = cmy / 24;
            if (num2 >= num)
            {
                num2 = num - 1;
            }
            if (num2 < 0)
            {
                num2 = 0;
            }
            for (int i = num2; i < num; i++)
            {
                int num3 = xScroll;
                int num4 = yScroll + i * ITEM_HEIGHT;
                int num5 = 24;
                int h = ITEM_HEIGHT - 1;
                int num6 = xScroll + num5;
                int num7 = yScroll + i * ITEM_HEIGHT;
                int num8 = wScroll - num5;
                int num9 = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num6, num7, num8, num9);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num3, num4, num5, h);
                TopInfo topInfo = (TopInfo)vTop.elementAt(i);
                if (topInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, topInfo.headICON, num3, num4, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[topInfo.headID];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num3 + part.pi[Char.CharInfo[0][0][0]].dx, num4 + num9 - 1, 0, mGraphics.BOTTOM | mGraphics.LEFT);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                if (topInfo.pId != Char.myCharz().charID)
                {
                    mFont.tahoma_7b_green.drawString(g, topInfo.name, num6 + 5, num7, 0);
                }
                else
                {
                    mFont.tahoma_7b_red.drawString(g, topInfo.name, num6 + 5, num7, 0);
                }
                mFont.tahoma_7_blue.drawString(g, topInfo.info, num6 + num8 - 5, num7 + 11, 1);
                mFont.tahoma_7_green2.drawString(g, mResources.rank + ": " + topInfo.rank + string.Empty, num6 + 5, num7 + 11, 0);
            }
            paintScrollArrow(g);
        }
    
        private static long lastLogPanelPaintErrorTime;
        private static void logPanelPaintErrorThrottled(string context, Exception ex)
        {
            long now = mSystem.currentTimeMillis();
            if (now - lastLogPanelPaintErrorTime > 3000)
            {
                lastLogPanelPaintErrorTime = now;
                Cout.LogWarning($"[Panel.{context}] Lỗi vẽ giao diện: {ex.Message}\n{ex.StackTrace}");
            }
        }

        public void paint(mGraphics g)
        {
            g.translate(-g.getTranslateX(), -g.getTranslateY() + mGraphics.addYWhenOpenKeyBoard);
            g.translate(-cmx, 0);
            g.translate(X, Y);
            if (GameCanvas.panel.combineSuccess != -1)
            {
                if (Equals(GameCanvas.panel))
                {
                    paintCombineEff(g);
                }
                return;
            }
            GameCanvas.paintz.paintFrameSimple(X, Y, W, H, g);
            try
            {
                paintTopInfo(g);
            }
            catch (Exception ex)
            {
                logPanelPaintErrorThrottled("paintTopInfo", ex);
            }
            paintBottomMoneyInfo(g);
            paintTab(g);
            ISubPanel activeSubPanel = GetCurrentSubPanel();
            bool subPanelHandled = false;
            if (activeSubPanel != null)
            {
                subPanelHandled = activeSubPanel.Paint(g, X, Y, W, H);
            }
            if (!subPanelHandled)
            {
                switch (type)
                {
                case TYPE_ARCHIVEMENT:
                    paintArchivement(g);
                    break;
                case TYPE_PET_MAIN:
                    if (currentTabIndex == PanelConstants.PetTabs.PET_INVENTORY)
                    {
                        paintPetInventory(g);
                    }
                    if (currentTabIndex == PanelConstants.PetTabs.PET_STATUS)
                    {
                        paintPetStatus(g);
                    }
                    if (currentTabIndex == PanelConstants.PetTabs.INVENTORY)
                    {
                        paintInventory(g);
                    }
                    break;
                case TYPE_GAMEINFOSUB:
                    paintGameSubInfo(g);
                    break;
                case TYPE_GAMEINFO:
                    paintGameInfo(g);
                    break;
                case TYPE_MAIN:
                    if (currentTabIndex == PanelConstants.MainTabs.TASK)
                    {
                        paintTask(g);
                    }
                    if (currentTabIndex == PanelConstants.MainTabs.INVENTORY)
                    {
                        paintInventory(g);
                    }
                    if (currentTabIndex == PanelConstants.MainTabs.SKILL)
                    {
                        paintSkill(g);
                    }
                    if (currentTabIndex == PanelConstants.MainTabs.CLANS)
                    {
                        if (mainTabName.Length == 4)
                        {
                            paintTools(g);
                        }
                        else
                        {
                            paintClans(g);
                        }
                    }
                    if (currentTabIndex == PanelConstants.MainTabs.TOOLS)
                    {
                        paintTools(g);
                    }
                    break;
                case TYPE_BOX:
                    if (currentTabIndex == PanelConstants.ChestTabs.CHEST)
                    {
                        paintBox(g);
                    }
                    if (currentTabIndex == PanelConstants.ChestTabs.INVENTORY)
                    {
                        paintInventory(g);
                    }
                    break;
                case TYPE_ZONE:
                    paintZone(g);
                    break;
                case TYPE_SHOP:
                    paintShop(g);
                    break;
                case TYPE_SPEACIALSKILL:
                    paintSpeacialSkill(g);
                    break;
                case TYPE_MAP:
                    paintMap(g);
                    break;
                case TYPE_BODY:
                    paintInventory(g);
                    break;
                case TYPE_KIGUI:
                    paintShop(g);
                    break;
                case TYPE_MESS:
                    paintLogChat(g);
                    break;
                case TYPE_OPTION - 9: // TYPE_PLAYER_MENU = 10
                    paintPlayerMenu(g);
                    break;
                case TYPE_FRIEND:
                    paintFriend(g);
                    break;
                case TYPE_ENEMY:
                    paintEnemy(g);
                    break;
                case TYPE_TOP:
                    paintTop(g);
                    break;
                case TYPE_COMBINE:
                    if (currentTabIndex == PanelConstants.CombineTabs.COMBINE)
                    {
                        paintCombine(g);
                    }
                    if (currentTabIndex == PanelConstants.CombineTabs.INVENTORY)
                    {
                        paintInventory(g);
                    }
                    break;
                case TYPE_GIAODICH:
                    if (currentTabIndex == 0)
                    {
                        if (Equals(GameCanvas.panel))
                        {
                            paintInventory(g);
                        }
                        else
                        {
                            paintGiaoDich(g, false);
                        }
                    }
                    if (currentTabIndex == 1)
                    {
                        paintGiaoDich(g, true);
                    }
                    if (currentTabIndex == 2)
                    {
                        paintGiaoDich(g, false);
                    }
                    break;
                case 14:
                    paintMapTrans(g);
                    break;
                case 18:
                    paintFlagChange(g);
                    break;
                case 19:
                    paintOption(g);
                    break;
                case 20:
                    paintAccount(g);
                    break;
                case 22:
                    paintAuto(g);
                    break;
                case 28:
                    if (currentTabIndex == 0)
                    {
                        AutoSettingManager.getInstance().paint(g, this);
                    }
                    else if (currentTabIndex == 1)
                    {
                        AutoSettingManager.getInstance().paintMobTanSat(g, this);
                    }
                    break;
                }
            }
            GameScr.resetTranslate(g);
            paintDetail(g);
            if (cmx == cmtoX && !GameCanvas.menu.showMenu)
            {
                cmdClose.paint(g);
            }
            if (tabIcon != null && tabIcon.isShow)
            {
                tabIcon.paint(g);
            }
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            g.translate(X, Y);
            g.translate(-cmx, 0);
        }
    
    
        private void paintAuto(mGraphics g)
        {
            mFont.tahoma_7b_dark.drawString(g, "Tính năng Auto đang cập nhật...", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
        }
    
        private void paintPetStatus(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strStatus.Length; i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    mFont.tahoma_7b_dark.drawString(g, strStatus[i], xScroll + wScroll / 2, num + 6, mFont.CENTER);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintPetSkill()
        {
        }
    
        private void paintPetInventory(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            if (Char.myPetz() == null || Char.myPetz().arrItemBody == null || Char.myPetz().arrPetSkill == null)
            {
                mFont.tahoma_7b_dark.drawString(g, "Bạn chưa có đệ tử", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                return;
            }
            Item[] arrItemBody = Char.myPetz().arrItemBody;
            Skill[] arrPetSkill = Char.myPetz().arrPetSkill;
            for (int i = 0; i < arrItemBody.Length + arrPetSkill.Length; i++)
            {
                bool flag = i < arrItemBody.Length;
                int num = i;
                int num2 = i - arrItemBody.Length;
                int num3 = xScroll + 36;
                int num4 = yScroll + i * ITEM_HEIGHT;
                int num5 = wScroll - 36;
                int h = ITEM_HEIGHT - 1;
                int num6 = xScroll;
                int num7 = yScroll + i * ITEM_HEIGHT;
                int num8 = 34;
                int num9 = ITEM_HEIGHT - 1;
                if (num4 - cmy > yScroll + hScroll || num4 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                Item item = ((!flag) ? null : arrItemBody[num]);
                g.setColor((i == selected) ? 16383818 : ((!flag) ? 15723751 : 15196114));
                g.fillRect(num3, num4, num5, h);
                g.setColor((i == selected) ? 9541120 : ((!flag) ? 11837316 : 9993045));
                if (item != null && item.itemOption != null)
                {
                    for (int j = 0; j < item.itemOption.Length; j++)
                    {
                        if (item.itemOption[j] == null || item.itemOption[j].optionTemplate == null) continue;
                        if (item.itemOption[j].optionTemplate.id == 72 && item.itemOption[j].param > 0)
                        {
                            sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[j].param);
                            int color_ItemBg = GetColor_ItemBg(color_Item_Upgrade);
                            if (color_ItemBg != -1)
                            {
                                g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                            }
                        }
                    }
                }
                g.fillRect(num6, num7, num8, num9);
                if (item != null && item.isSelect && GameCanvas.panel.type == 12)
                {
                    g.setColor((i != selected) ? 6047789 : 7040779);
                    g.fillRect(num6, num7, num8, num9);
                }
                if (item != null)
                {
                    string text = string.Empty;
                    mFont mFont2 = mFont.tahoma_7_green2;
                    if (item.itemOption != null)
                    {
                        for (int k = 0; k < item.itemOption.Length; k++)
                        {
                            if (item.itemOption[k] == null || item.itemOption[k].optionTemplate == null) continue;
                            if (item.itemOption[k].optionTemplate.id == 72)
                            {
                                text = " [+" + item.itemOption[k].param + "]";
                            }
                            if (item.itemOption[k].optionTemplate.id == 107)
                            {
                                paintItemStar(g, item.itemOption[k].param.ToString(), num3 + 125, num4 + 1);
                            }
                            if (item.itemOption[k].optionTemplate.id == 41)
                            {
                                if (item.itemOption[k].param == 1)
                                {
                                    mFont2 = GetFont(0);
                                }
                                else if (item.itemOption[k].param == 2)
                                {
                                    mFont2 = GetFont(2);
                                }
                                else if (item.itemOption[k].param == 3)
                                {
                                    mFont2 = GetFont(8);
                                }
                                else if (item.itemOption[k].param == 4)
                                {
                                    mFont2 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont2.drawString(g, item.template.name + text, num3 + 5, num4 + 1, 0);
                    string text2 = string.Empty;
                    if (item.itemOption != null)
                    {
                        for (int l = 0; l < item.itemOption.Length; l++)
                        {
                            if (item.itemOption[l] != null && (item.itemOption[l].optionTemplate == null || (item.itemOption[l].optionTemplate.id != 102 && item.itemOption[l].optionTemplate.id != 107)))
                            {
                                string optStr = item.itemOption[l].getOptionString();
                                if (!string.IsNullOrEmpty(optStr))
                                {
                                    text2 = string.IsNullOrEmpty(text2) ? optStr : (text2 + "," + optStr);
                                    if (text2.Contains(",")) break;
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
                            mFont3.drawString(g, text2, num3 + 5, num4 + 11, mFont.LEFT);
                        }
                    }
                    SmallImage.drawSmallImage(g, item.template.iconID, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                    if (item.itemOption != null)
                    {
                        for (int m = 0; m < item.itemOption.Length; m++)
                        {
                            if (item.itemOption[m] != null && item.itemOption[m].optionTemplate != null)
                                paintOptItem(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, num6, num7, num8, num9);
                        }
                        for (int n = 0; n < item.itemOption.Length; n++)
                        {
                            if (item.itemOption[n] != null && item.itemOption[n].optionTemplate != null)
                                paintOptSlotItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num6, num7, num8, num9);
                        }
                    }
                    if (item.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num6 + num8, num7 + num9 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
                else if (!flag)
                {
                    Skill skill = arrPetSkill[num2];
                    g.drawImage(GameScr.imgSkill, num6 + num8 / 2, num7 + num9 / 2, 3);
                    if (skill.template != null)
                    {
                        mFont.tahoma_7_blue.drawString(g, skill.template.name, num3 + 5, num4 + 1, 0);
                        mFont.tahoma_7_green2.drawString(g, mResources.level + ": " + skill.point + string.Empty, num3 + 5, num4 + 11, 0);
                        SmallImage.drawSmallImage(g, skill.template.iconId, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                    }
                    else
                    {
                        mFont.tahoma_7_green2.drawString(g, skill.moreInfo, num3 + 5, num4 + 5, 0);
                        if (GameScr.efs != null && GameScr.efs.Length > 98 && GameScr.efs[98] != null && GameScr.efs[98].arrEfInfo != null && GameScr.efs[98].arrEfInfo.Length > 0)
                        {
                            SmallImage.drawSmallImage(g, GameScr.efs[98].arrEfInfo[0].idImg, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                        }
                    }
                }
            }
            paintScrollArrow(g);
        }
    
        public void paintScrollArrow(mGraphics g)
        {
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            if ((cmy > 24 && currentListLength > 0) || (Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1))
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll - 12, yScroll + 3, 0);
            }
            if ((cmy < cmyLim && currentListLength > 0) || (Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1))
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll - 12, yScroll + hScroll - 8, 0);
            }
        }
    
        private void paintTools(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strTool.Length; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, h);
                mFont.tahoma_7b_dark.drawString(g, strTool[i], xScroll + wScroll / 2, num2 + 6, mFont.CENTER);
                if (!strTool[i].Equals(mResources.gameInfo))
                {
                    continue;
                }
                for (int j = 0; j < vGameInfo.size(); j++)
                {
                    GameInfo gameInfo = (GameInfo)vGameInfo.elementAt(j);
                    if (!gameInfo.hasRead)
                    {
                        if (GameCanvas.gameTick % 20 > 10)
                        {
                            g.drawImage(imgNew, num + 10, num2 + 10, 3);
                        }
                        break;
                    }
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintGameSubInfo(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < contenInfo.Length; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * 15;
                int num3 = wScroll - 1;
                int num4 = ITEM_HEIGHT - 1;
                if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
                {
                    mFont.tahoma_7b_dark.drawString(g, contenInfo[i], xScroll + 5, num2 + 6, mFont.LEFT);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintGameInfo(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < vGameInfo.size(); i++)
            {
                GameInfo gameInfo = (GameInfo)vGameInfo.elementAt(i);
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(num, num2, num3, h);
                    mFont.tahoma_7b_dark.drawString(g, gameInfo.main, xScroll + wScroll / 2, num2 + 6, mFont.CENTER);
                    if (!gameInfo.hasRead && GameCanvas.gameTick % 20 > 10)
                    {
                        g.drawImage(imgNew, num + 10, num2 + 10, 3);
                    }
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintSkill(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            int num = Char.myCharz().nClass.skillTemplates.Length;
            for (int i = 0; i < num + 6; i++)
            {
                int num2 = xScroll + 30;
                int num3 = yScroll + i * ITEM_HEIGHT;
                int num4 = wScroll - 30;
                int h = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + i * ITEM_HEIGHT;
                int num7 = 34;
                int num8 = ITEM_HEIGHT - 1;
                if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                if (i == 5)
                {
                    g.setColor((i != selected) ? 16765060 : 16776068);
                }
                g.fillRect(num2, num3, num4, h);
                g.drawImage(GameScr.imgSkill, num5, num6, 0);
                if (i == 0)
                {
                    SmallImage.drawSmallImage(g, 567, num5 + 4, num6 + 4, 0, 0);
                    string st = mResources.HP + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cHPGoc);
                    mFont.tahoma_7b_blue.drawString(g, st, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().cHPGoc + 1000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().hpFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 1)
                {
                    SmallImage.drawSmallImage(g, 569, num5 + 4, num6 + 4, 0, 0);
                    string st2 = mResources.KI + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cMPGoc);
                    mFont.tahoma_7b_blue.drawString(g, st2, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().cMPGoc + 1000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().mpFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 2)
                {
                    SmallImage.drawSmallImage(g, 568, num5 + 4, num6 + 4, 0, 0);
                    string st3 = mResources.hit_point + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cDamGoc);
                    mFont.tahoma_7b_blue.drawString(g, st3, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().cDamGoc * 100) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().damFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 3)
                {
                    SmallImage.drawSmallImage(g, 721, num5 + 4, num6 + 4, 0, 0);
                    string st4 = mResources.armor + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cDefGoc);
                    mFont.tahoma_7b_blue.drawString(g, st4, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(500000 + Char.myCharz().cDefGoc * 100000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().defFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 4)
                {
                    SmallImage.drawSmallImage(g, 719, num5 + 4, num6 + 4, 0, 0);
                    string st5 = mResources.critical + " " + mResources.root + ": " + Char.myCharz().cCriticalGoc + "%";
                    long num9 = 50000000L;
                    int num10 = Char.myCharz().cCriticalGoc;
                    if (num10 > t_tiemnang.Length - 1)
                    {
                        num10 = t_tiemnang.Length - 1;
                    }
                    num9 = t_tiemnang[num10];
                    mFont.tahoma_7b_blue.drawString(g, st5, num2 + 5, num3 + 3, 0);
                    long number = num9;
                    mFont.tahoma_7_green2.drawString(g, Res.formatNumber2(number) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().criticalFrom1000Tiemnang, num2 + 5, num3 + 15, 0);
                }
                if (i == 5)
                {
                    if (specialInfo != null)
                    {
                        SmallImage.drawSmallImage(g, spearcialImage, num5 + 4, num6 + 4, 0, 0);
                        string[] array = mFont.tahoma_7.splitFontArray(specialInfo, 120);
                        for (int j = 0; j < array.Length; j++)
                        {
                            mFont.tahoma_7_green2.drawString(g, array[j], num2 + 5, num3 + 3 + j * 12, 0);
                        }
                    }
                    else
                    {
                        mFont.tahoma_7_green2.drawString(g, string.Empty, num2 + 5, num3 + 9, 0);
                    }
                }
                if (i < 6)
                {
                    continue;
                }
                int num11 = i - 6;
                SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[num11];
                SmallImage.drawSmallImage(g, skillTemplate.iconId, num5 + 4, num6 + 4, 0, 0);
                Skill skill = Char.myCharz().getSkill(skillTemplate);
                if (skill != null)
                {
                    mFont.tahoma_7b_blue.drawString(g, skillTemplate.name, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_blue.drawString(g, mResources.level + ": " + skill.point, num2 + num4 - 5, num3 + 3, mFont.RIGHT);
                    if (skill.point == skillTemplate.maxPoint)
                    {
                        mFont.tahoma_7_green2.drawString(g, mResources.max_level_reach, num2 + 5, num3 + 15, 0);
                    }
                    else if (skill.template.isSkillSpec())
                    {
                        string text = mResources.proficiency + ": ";
                        int x = mFont.tahoma_7_green2.getWidthExactOf(text) + num2 + 5;
                        int num12 = num3 + 15;
                        mFont.tahoma_7_green2.drawString(g, text, num2 + 5, num12, 0);
                        mFont.tahoma_7_green2.drawString(g, "(" + skill.strCurExp() + ")", num2 + num4 - 5, num12, mFont.RIGHT);
                        num12 += 4;
                        g.setColor(7169134);
                        g.fillRect(x, num12, 50, 5);
                        int num13 = skill.curExp * 50 / 1000;
                        g.setColor(11992374);
                        g.fillRect(x, num12, num13, 5);
                        if (skill.curExp < 1000)
                        {
                        }
                    }
                    else
                    {
                        Skill skill2 = skillTemplate.skills[skill.point];
                        mFont.tahoma_7_green2.drawString(g, mResources.level + " " + (skill.point + 1) + " " + mResources.need + " " + Res.formatNumber2(skill2.powRequire) + " " + mResources.potential, num2 + 5, num3 + 15, 0);
                    }
                }
                else
                {
                    Skill skill3 = skillTemplate.skills[0];
                    string st6 = mResources.need_upper + " " + Res.formatNumber2(skill3.powRequire) + " " + mResources.potential_to_learn;
                    if (skill3.template.id == 24 || skill3.template.id == 25 || skill3.template.id == 26)
                    {
                        st6 = mResources.need_upper + " " + Res.formatNumber2(skill3.powRequire) + " " + mResources.potential_to_learn_tuyetKi;
                    }
                    mFont.tahoma_7b_green.drawString(g, skillTemplate.name, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, st6, num2 + 5, num3 + 15, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintMapTrans(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            if (mapNames == null || mapNames.Length == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Không có bản đồ khả dụng", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                return;
            }
            for (int i = 0; i < mapNames.Length; i++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = 34;
                int num7 = ITEM_HEIGHT - 1;
                if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(xScroll, num2, wScroll, h);
                    mFont.tahoma_7b_blue.drawString(g, mapNames[i], 5, num2 + 1, 0);
                    mFont.tahoma_7_grey.drawString(g, planetNames[i], 5, num2 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintZone(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            int[] zones = GameScr.gI().zones;
            int[] pts = GameScr.gI().pts;
            if (zones == null || pts == null || pts.Length == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Đang tải danh sách khu vực...", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                return;
            }
            for (int i = 0; i < pts.Length; i++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll;
                int y = yScroll + i * ITEM_HEIGHT;
                int num5 = 34;
                int h2 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, h);
                g.setColor(zoneColor[pts[i]]);
                g.fillRect(num4, y, num5, h2);
                if (zones[i] != -1)
                {
                    if (pts[i] != 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, zones[i] + string.Empty, num4 + num5 / 2, num2 + 6, mFont.CENTER);
                    }
                    else
                    {
                        mFont.tahoma_7_grey.drawString(g, zones[i] + string.Empty, num4 + num5 / 2, num2 + 6, mFont.CENTER);
                    }
                    mFont.tahoma_7_green2.drawString(g, GameScr.gI().numPlayer[i] + "/" + GameScr.gI().maxPlayer[i], num + 5, num2 + 6, 0);
                }
                if (GameScr.gI().rankName1[i] != null)
                {
                    mFont.tahoma_7_grey.drawString(g, GameScr.gI().rankName1[i] + "(Top " + GameScr.gI().rank1[i] + ")", num + num3 - 2, num2 + 1, mFont.RIGHT);
                    mFont.tahoma_7_grey.drawString(g, GameScr.gI().rankName2[i] + "(Top " + GameScr.gI().rank2[i] + ")", num + num3 - 2, num2 + 11, mFont.RIGHT);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintSpeacialSkill(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Bạn chưa học kỹ năng đặc biệt nào", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                return;
            }
            int num = (cmy + hScroll) / 24 + 1;
            if (num < hScroll / 24 + 1)
            {
                num = hScroll / 24 + 1;
            }
            if (num > currentListLength)
            {
                num = currentListLength;
            }
            int num2 = cmy / 24;
            if (num2 >= num)
            {
                num2 = num - 1;
            }
            if (num2 < 0)
            {
                num2 = 0;
            }
            for (int i = num2; i < num; i++)
            {
                int num3 = xScroll;
                int num4 = yScroll + i * ITEM_HEIGHT;
                int num5 = 24;
                int num6 = ITEM_HEIGHT - 1;
                int num7 = xScroll + num5;
                int num8 = yScroll + i * ITEM_HEIGHT;
                int num9 = wScroll - num5;
                int h = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num7, num8, num9, h);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num3, num4, num5, num6);
                SmallImage.drawSmallImage(g, Char.myCharz().imgSpeacialSkill[currentTabIndex][i], num3 + num5 / 2, num4 + num6 / 2, 0, 3);
                string rawSkillInfo = Char.myCharz().infoSpeacialSkill[currentTabIndex][i];
                string[] array = getCachedSpecialSkillLines(currentTabIndex, i, rawSkillInfo);
                for (int j = 0; j < array.Length; j++)
                {
                    mFont.tahoma_7_grey.drawString(g, array[j], num7 + 5, num8 + 1 + j * 11, 0);
                }
            }
            paintScrollArrow(g);
        }

        private int cachedSpecialTab = -1;
        private string[] cachedSpecialRaw;
        private string[][] cachedSpecialSplit;

        private string[] getCachedSpecialSkillLines(int tab, int index, string raw)
        {
            if (raw == null)
            {
                return new string[0];
            }
            int len = Char.myCharz().infoSpeacialSkill[tab].Length;
            if (cachedSpecialTab != tab || cachedSpecialRaw == null || cachedSpecialRaw.Length != len)
            {
                cachedSpecialTab = tab;
                cachedSpecialRaw = new string[len];
                cachedSpecialSplit = new string[len][];
            }
            if (!object.ReferenceEquals(cachedSpecialRaw[index], raw))
            {
                cachedSpecialRaw[index] = raw;
                cachedSpecialSplit[index] = mFont.tahoma_7_grey.splitFontArray(raw, 140);
            }
            return cachedSpecialSplit[index];
        }
    
    
        public Member getCurrMember()
        {
            if (selected < 2)
            {
                return null;
            }
            if (selected > ((member == null) ? myMember.size() : member.size()) + 1)
            {
                return null;
            }
            return (member == null) ? ((Member)myMember.elementAt(selected - 2)) : ((Member)member.elementAt(selected - 2));
        }
    
        public ClanMessage getCurrMessage()
        {
            if (selected < 2)
            {
                return null;
            }
            if (selected > ClanMessage.vMessage.size() + 1)
            {
                return null;
            }
            return (ClanMessage)ClanMessage.vMessage.elementAt(selected - 2);
        }
    
        public Clan getCurrClan()
        {
            if (selected < 2)
            {
                return null;
            }
            if (selected > clans.Length + 1)
            {
                return null;
            }
            return clans[selected - 2];
        }
    
        private void paintLogChat(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (logChat.size() == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_msg, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2 + 24, 2);
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = 24;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll + num3;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = wScroll - num3;
                int num7 = ITEM_HEIGHT - 1;
                if (i == 0)
                {
                    g.setColor(15196114);
                    g.fillRect(num, num5, wScroll, num7);
                    g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num5 + 2, StaticObj.TOP_RIGHT);
                    ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, (!isViewChatServer) ? mResources.on : mResources.off, xScroll + wScroll - 22, num5 + 7, 2);
                    mFont.tahoma_7_grey.drawString(g, (!isViewChatServer) ? mResources.onPlease : mResources.offPlease, xScroll + 5, num5 + num7 / 2 - 4, mFont.LEFT);
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num4, num5, num6, num7);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num, num2, num3, h);
                InfoItem infoItem = (InfoItem)logChat.elementAt(i - 1);
                if (infoItem.charInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[infoItem.charInfo.head];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                mFont tahoma_7b_dark = mFont.tahoma_7b_dark;
                tahoma_7b_dark = mFont.tahoma_7b_green2;
                tahoma_7b_dark.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                if (!infoItem.isChatServer)
                {
                    mFont.tahoma_7_blue.drawString(g, Res.split(infoItem.s, "|", 0)[2], num4 + 5, num5 + 11, 0);
                }
                else
                {
                    mFont.tahoma_7_red.drawString(g, Res.split(infoItem.s, "|", 0)[2], num4 + 5, num5 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintFlagChange(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0 || vFlag == null || vFlag.size() == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Không có cờ khả dụng", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                return;
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll + 26;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 26;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = 24;
                int num7 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, h);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num4, num5, num6, num7);
                Item item = (Item)vFlag.elementAt(i);
                if (item == null)
                {
                    continue;
                }
                mFont.tahoma_7_green2.drawString(g, item.template.name, num + 5, num2 + 1, 0);
                string text = string.Empty;
                if (item.itemOption != null && item.itemOption.Length >= 1)
                {
                    if (item.itemOption[0] != null && item.itemOption[0].optionTemplate.id != 102 && item.itemOption[0].optionTemplate.id != 107)
                    {
                        text += item.itemOption[0].getOptionString();
                    }
                    mFont tahoma_7_blue = mFont.tahoma_7_blue;
                    tahoma_7_blue.drawString(g, text, num + 5, num2 + 11, 0);
                    SmallImage.drawSmallImage(g, item.template.iconID, num4 + num6 / 2, num5 + num7 / 2, 0, 3);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintEnemy(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_enemy, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
                return;
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = 24;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll + num3;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = wScroll - num3;
                int h2 = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num4, num5, num6, h2);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num, num2, num3, h);
                InfoItem infoItem = (InfoItem)vEnemy.elementAt(i);
                if (infoItem.charInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[infoItem.charInfo.head];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                if (infoItem.isOnline)
                {
                    mFont.tahoma_7b_green.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_blue.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_grey.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    }
}

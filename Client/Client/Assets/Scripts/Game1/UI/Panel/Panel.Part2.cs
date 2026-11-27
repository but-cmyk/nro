namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        public void updateKey()
        {
            ISubPanel activeSubPanel = GetCurrentSubPanel();
            if (activeSubPanel != null && activeSubPanel.UpdateKey())
            {
                return;
            }
            if ((chatTField != null && chatTField.isShow) || !GameCanvas.panel.isDoneCombine || InfoDlg.isShow)
            {
                return;
            }
            if (tabIcon != null && tabIcon.isShow)
            {
                tabIcon.updateKey();
            }
            else
            {
                if (isClose || !isShow)
                {
                    return;
                }
                if (cmdClose.isPointerPressInside())
                {
                    cmdClose.performAction();
                    return;
                }
                if (GameKeys.IsSoftRightPressed())
                {
                    if (type != TYPE_MAP)
                    {
                        hide();
                        return;
                    }
                    setTypeMain();
                    cmx = (cmtoX = 0);
                }
                if (GameKeys.IsSoftLeftPressed() || GameKeys.IsFirePressed())
                {
                    if (left.idAction > 0)
                    {
                        perform(left.idAction, left.p);
                    }
                    else
                    {
                        waitToPerform = 2;
                    }
                }
                if (Equals(GameCanvas.panel) && GameCanvas.panel2 == null && GameCanvas.isPointerJustRelease && !GameCanvas.isPointer(X, Y, W, H) && !pointerIsDowning)
                {
                    hide();
                    return;
                }
                if (!isClanOption)
                {
                    updateKeyInTabBar();
                }
                switch (type)
                {
                    case 23:
                    case 24:
                    case 30:
                        updateKeyScrollView();
                        break;
                    case 21:
                        if (currentTabIndex == 0)
                        {
                            updateKeyScrollView();
                        }
                        if (currentTabIndex == 1)
                        {
                            updateKeyPetStatus();
                        }
                        if (currentTabIndex == 2)
                        {
                            updateKeyScrollView();
                        }
                        break;
                    case 0:
                        if (currentTabIndex == 0)
                        {
                            updateKeyQuest();
                            GameCanvas.clearKeyPressed();
                            return;
                        }
                        if (currentTabIndex == 1)
                        {
                            updateKeyInventory();
                        }
                        if (currentTabIndex == 2)
                        {
                            updateKeySkill();
                        }
                        if (currentTabIndex == 3)
                        {
                            if (mainTabName.Length == 4)
                            {
                                updateKeyTool();
                            }
                            else
                            {
                                updateKeyClans();
                            }
                        }
                        if (currentTabIndex == 4)
                        {
                            updateKeyTool();
                        }
                        break;
                    case 2:
                        updateKeyInventory();
                        break;
                    case 3:
                        updateKeyScrollView();
                        break;
                    case 14:
                        updateKeyScrollView();
                        break;
                    case 1:
                    case 17:
                    case 25:
                        if (currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                        {
                            updateKeyScrollView();
                        }
                        else if (typeShop == 0)
                        {
                            updateKeyInventory();
                        }
                        else
                        {
                            updateKeyScrollView();
                        }
                        break;
                    case 4:
                        updateKeyMap();
                        GameCanvas.clearKeyPressed();
                        return;
                    case 7:
                        updateKeyInventory();
                        break;
                    case 8:
                        updateKeyScrollView();
                        break;
                    case 9:
                        updateKeyScrollView();
                        break;
                    case 10:
                        updateKeyScrollView();
                        break;
                    case 11:
                    case 16:
                        updateKeyScrollView();
                        break;
                    case 15:
                        updateKeyScrollView();
                        break;
                    case 12:
                        updateKeyCombine();
                        break;
                    case 13:
                        updateKeyGiaoDich();
                        break;
                    case 18:
                        updateKeyScrollView();
                        break;
                    case 19:
                        updateKeyOption();
                        break;
                    case 20:
                        updateKeyOption();
                        break;
                    case 22:
                        updateKeyAuto();
                        break;
                    case 28:
                        updateKeyAutoSetting();
                        break;
                }
                GameCanvas.clearKeyHold();
                for (int i = 0; i < GameCanvas.keyPressed.Length; i++)
                {
                    GameCanvas.keyPressed[i] = false;
                }
            }
        }
    
        private void updateKeyAuto()
        {
        }
    
        private void updateKeyPetStatus()
        {
            updateKeyScrollView();
        }
    
        private void updateKeyPetSkill()
        {
        }
    
        private void keyGiaodich()
        {
            updateKeyScrollView();
        }
    
        private void updateKeyGiaoDich()
        {
            if (currentTabIndex == 0)
            {
                if (Equals(GameCanvas.panel))
                {
                    updateKeyInventory();
                }
                if (Equals(GameCanvas.panel2))
                {
                    keyGiaodich();
                }
            }
            if (currentTabIndex == 1 || currentTabIndex == 2)
            {
                keyGiaodich();
            }
        }
    
        private void updateKeyTool()
        {
            updateKeyScrollView();
        }
    
        private void updateKeySkill()
        {
            updateKeyScrollView();
        }
    
        private void updateKeyClanIcon()
        {
            updateKeyScrollView();
        }
    
        public void setTabGiaoDich(bool isMe)
        {
            currentListLength = ((!isMe) ? (vFriendGD.size() + 3) : (vMyGD.size() + 3));
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        public void setTypeGiaoDich(Char cGD)
        {
            type = 13;
            tabName[type] = boxGD;
            isAccept = false;
            isLock = false;
            isFriendLock = false;
            vMyGD.removeAllElements();
            vFriendGD.removeAllElements();
            moneyGD = 0;
            friendMoneyGD = 0;
            if (GameCanvas.w > 2 * WIDTH_PANEL)
            {
                Panel panel2 = EnsurePanel2();
                panel2.type = 13;
                panel2.tabName[type] = new string[1][] { mResources.item_receive };
                panel2.setType(1);
                panel2.setTabGiaoDich(false);
                GameCanvas.panel.tabName[type] = new string[2][]
                {
                    mResources.inventory,
                    mResources.item_give
                };
                panel2.show();
                panel2.charMenu = cGD;
            }
            if (Equals(GameCanvas.panel))
            {
                setType(0);
            }
            if (currentTabIndex == 0)
            {
                setTabInventory(true);
            }
            if (currentTabIndex == 1)
            {
                setTabGiaoDich(true);
            }
            if (currentTabIndex == 2)
            {
                setTabGiaoDich(false);
            }
            charMenu = cGD;
        }
    
        private void paintGiaoDich(mGraphics g, bool isMe)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            MyVector myVector = ((!isMe) ? vFriendGD : vMyGD);
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int num4 = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + i * ITEM_HEIGHT;
                int num7 = 34;
                int num8 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                if (i == currentListLength - 1)
                {
                    if (!isMe)
                    {
                        continue;
                    }
                    g.setColor(15196114);
                    g.fillRect(num5, num2, wScroll, num4);
                    if (!isLock)
                    {
                        if (!isFriendLock)
                        {
                            mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.not_lock_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                        else
                        {
                            mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.locked_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                    }
                    else if (isFriendLock)
                    {
                        g.setColor(15196114);
                        g.fillRect(num5, num2, wScroll, num4);
                        g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                        ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.done, xScroll + wScroll - 22, num2 + 7, 2);
                        mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.locked_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                    }
                    else
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.not_lock_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    continue;
                }
                if (i == currentListLength - 2)
                {
                    if (isMe)
                    {
                        g.setColor(15196114);
                        g.fillRect(num5, num2, wScroll, num4);
                        if (!isAccept)
                        {
                            if (!isLock)
                            {
                                g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                                ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.mlock, xScroll + wScroll - 22, num2 + 7, 2);
                                mFont.tahoma_7_grey.drawString(g, mResources.you + mResources.not_lock_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                            }
                            else
                            {
                                g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                                ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.CANCEL, xScroll + wScroll - 22, num2 + 7, 2);
                                mFont.tahoma_7_grey.drawString(g, mResources.you + mResources.locked_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                            }
                        }
                    }
                    else if (!isFriendLock)
                    {
                        mFont.tahoma_7b_dark.drawString(g, mResources.not_lock_trade_upper, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    else
                    {
                        mFont.tahoma_7b_dark.drawString(g, mResources.locked_trade_upper, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    continue;
                }
                if (i == currentListLength - 3)
                {
                    if (isLock)
                    {
                        g.setColor(13748667);
                    }
                    else
                    {
                        g.setColor((i != selected) ? 15196114 : 16383818);
                    }
                    g.fillRect(num, num2, num3, num4);
                    if (isLock)
                    {
                        g.setColor(13748667);
                    }
                    else
                    {
                        g.setColor((i != selected) ? 9993045 : 7300181);
                    }
                    g.fillRect(num5, num6, num7, num8);
                    g.drawImage(imgXu, num5 + num7 / 2, num6 + num8 / 2, 3);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys((!isMe) ? friendMoneyGD : moneyGD) + " " + mResources.XU, num + 5, num2 + 11, 0);
                    mFont.tahoma_7_green.drawString(g, mResources.money_trade, num + 5, num2, 0);
                    continue;
                }
                if (myVector.size() == 0)
                {
                    return;
                }
                if (isLock)
                {
                    g.setColor(13748667);
                }
                else
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                }
                g.fillRect(num, num2, num3, num4);
                if (isLock)
                {
                    g.setColor(13748667);
                }
                else
                {
                    g.setColor((i != selected) ? 9993045 : 9541120);
                }
                Item item = (Item)myVector.elementAt(i);
                if (item != null && item.itemOption != null)
                {
                    for (int j = 0; j < item.itemOption.Length; j++)
                    {
                        if (item.itemOption[j] == null || item.itemOption[j].optionTemplate == null) continue;
                        if (item.itemOption[j].optionTemplate.id != 72 || item.itemOption[j].param <= 0)
                        {
                            continue;
                        }
                        sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[j].param);
                        int color_ItemBg = GetColor_ItemBg(color_Item_Upgrade);
                        if (color_ItemBg != -1)
                        {
                            if (isLock)
                            {
                                g.setColor(13748667);
                            }
                            else
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
                    for (int k = 0; k < item.itemOption.Length; k++)
                    {
                        if (item.itemOption[k] == null || item.itemOption[k].optionTemplate == null) continue;
                        if (item.itemOption[k].optionTemplate.id == 72)
                        {
                            text = " [+" + item.itemOption[k].param + "]";
                        }
                        if (item.itemOption[k].optionTemplate.id == 107)
                        {
                            paintItemStar(g, item.itemOption[k].param.ToString(), num + 125, num2 + 1);
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
                mFont2.drawString(g, item.template.name + text, num + 5, num2 + 1, 0);
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
                    for (int m = 0; m < item.itemOption.Length; m++)
                    {
                        if (item.itemOption[m] != null && item.itemOption[m].optionTemplate != null)
                            paintOptItem(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, num5, num6, num7, num8);
                    }
                    for (int n = 0; n < item.itemOption.Length; n++)
                    {
                        if (item.itemOption[n] != null && item.itemOption[n].optionTemplate != null)
                            paintOptSlotItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8);
                    }
                }
                if (item.quantity > 1)
                {
                    mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                }
            }
            paintScrollArrow(g);
        }
    
        private void updateKeyMap()
        {
            if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
            {
                yMove -= 5;
                cmyMap = yMove - (yScroll + hScroll / 2);
                if (yMove < yScroll)
                {
                    yMove = yScroll;
                }
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 8 : 22])
            {
                yMove += 5;
                cmyMap = yMove - (yScroll + hScroll / 2);
                if (yMove > yScroll + 200)
                {
                    yMove = yScroll + 200;
                }
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
            {
                xMove -= 5;
                cmxMap = xMove - wScroll / 2;
                if (xMove < 16)
                {
                    xMove = 16;
                }
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
            {
                xMove += 5;
                cmxMap = xMove - wScroll / 2;
                if (xMove > 250)
                {
                    xMove = 250;
                }
            }
            if (GameCanvas.isPointerDown)
            {
                pointerIsDowning = true;
                if (!trans)
                {
                    pa1 = cmxMap;
                    pa2 = cmyMap;
                    trans = true;
                }
                cmxMap = pa1 + (GameCanvas.pxLast - GameCanvas.px);
                cmyMap = pa2 + (GameCanvas.pyLast - GameCanvas.py);
            }
            if (GameCanvas.isPointerJustRelease)
            {
                trans = false;
                GameCanvas.pxLast = GameCanvas.px;
                GameCanvas.pyLast = GameCanvas.py;
                pX = GameCanvas.pxLast + cmxMap;
                pY = GameCanvas.pyLast + cmyMap;
            }
            if (GameCanvas.isPointerClick)
            {
                pointerIsDowning = false;
            }
            if (cmxMap < 0)
            {
                cmxMap = 0;
            }
            if (cmxMap > cmxMapLim)
            {
                cmxMap = cmxMapLim;
            }
            if (cmyMap < 0)
            {
                cmyMap = 0;
            }
            if (cmyMap > cmyMapLim)
            {
                cmyMap = cmyMapLim;
            }
        }
    
        private void updateKeyCombine()
        {
            if (currentTabIndex == 0)
            {
                updateKeyScrollView();
                keyTouchCombine = -1;
                if (selected == vItemCombine.size() && GameCanvas.isPointerClick)
                {
                    GameCanvas.isPointerClick = false;
                    keyTouchCombine = 1;
                }
            }
            if (currentTabIndex == 1)
            {
                updateKeyScrollView();
            }
        }
    
        private void updateKeyQuest()
        {
            if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
            {
                cmyQuest -= 5;
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 8 : 22])
            {
                cmyQuest += 5;
            }
            if (cmyQuest < 0)
            {
                cmyQuest = 0;
            }
            int num = indexRowMax * 12 - (hScroll - 60);
            if (num < 0)
            {
                num = 0;
            }
            if (cmyQuest > num)
            {
                cmyQuest = num;
            }
            if (scroll != null)
            {
                if (!GameCanvas.isTouch)
                {
                    scroll.cmy = cmyQuest;
                }
                scroll.updateKey();
            }
            int num2 = xScroll + wScroll / 2 - 35;
            int num3 = ((GameCanvas.h <= 300) ? 15 : 20);
            int num4 = yScroll + hScroll - num3 - 15;
            int px = GameCanvas.px;
            int py = GameCanvas.py;
            keyTouchMapButton = -1;
            if (isPaintMap && !GameScr.gI().isMapDocNhan() && px >= num2 && px <= num2 + 70 && py >= num4 && py <= num4 + 30 && (scroll == null || !scroll.pointerIsDowning))
            {
                keyTouchMapButton = 1;
                if (GameCanvas.isPointerJustRelease)
                {
                    SoundMn.gI().buttonClick();
                    waitToPerform = 2;
                    GameCanvas.clearAllPointerEvent();
                }
            }
        }
    
        private void getCurrClanOtion()
        {
            isClanOption = false;
            if (type != 0 || mainTabName.Length != 5 || currentTabIndex != 3)
            {
                return;
            }
            isClanOption = false;
            if (selected == 0)
            {
                currClanOption = new int[clansOption.Length];
                for (int i = 0; i < currClanOption.Length; i++)
                {
                    currClanOption[i] = i;
                }
                if (!isViewMember)
                {
                    isClanOption = true;
                }
            }
            else if (selected != 1 && !isSearchClan && selected > 0)
            {
                currClanOption = new int[1];
                for (int j = 0; j < currClanOption.Length; j++)
                {
                    currClanOption[j] = j;
                }
                isClanOption = true;
            }
        }
    
        private void updateKeyClansOption()
        {
            if (currClanOption == null)
            {
                return;
            }
            if (GameKeys.IsLeftPressed())
            {
                currMess = getCurrMessage();
                cSelected--;
                if (selected == 0 && cSelected < 0)
                {
                    cSelected = currClanOption.Length - 1;
                }
                if (selected > 1 && isMessage && currMess.option != null && cSelected < 0)
                {
                    cSelected = currMess.option.Length - 1;
                }
            }
            else if (GameKeys.IsRightPressed())
            {
                currMess = getCurrMessage();
                cSelected++;
                if (selected == 0 && cSelected > currClanOption.Length - 1)
                {
                    cSelected = 0;
                }
                if (selected > 1 && isMessage && currMess.option != null && cSelected > currMess.option.Length - 1)
                {
                    cSelected = 0;
                }
            }
        }
    
        private void updateKeyClans()
        {
            updateKeyScrollView();
            updateKeyClansOption();
        }
    
        private void checkOptionSelect()
        {
            try
            {
                if (type != 0 || currentTabIndex != 3 || mainTabName.Length != 5 || selected == -1)
                {
                    return;
                }
                int num = 0;
                if (selected == 0)
                {
                    num = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                    cSelected = (GameCanvas.px - num) / TAB_W;
                }
                else
                {
                    currMess = getCurrMessage();
                    if (currMess != null && currMess.option != null)
                    {
                        num = xScroll + wScroll - 2 - currMess.option.Length * 40;
                        cSelected = (GameCanvas.px - num) / 40;
                    }
                }
                if (GameCanvas.px < num)
                {
                    cSelected = -1;
                }
            }
            catch (Exception ex)
            {
                Res.outz("Throw err " + ex.StackTrace);
            }
        }
    
        public void updateScroolMouse(int a)
        {
            bool flag = false;
            if (GameCanvas.pxMouse > wScroll)
            {
                return;
            }
            if (indexMouse == -1)
            {
                indexMouse = selected;
            }
            if (a > 0)
            {
                indexMouse -= a;
                flag = true;
            }
            else if (a < 0)
            {
                indexMouse += -a;
                flag = true;
            }
            if (indexMouse < 0)
            {
                indexMouse = 0;
            }
            if (flag)
            {
                cmtoY = indexMouse * 12;
                if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
            }
        }
    }
}

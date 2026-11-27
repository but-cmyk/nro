namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        private void updateKeyScrollView()
        {
            if (currentListLength <= 0)
            {
                return;
            }
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            bool flag = false;
            if (GameKeys.IsUpPressed())
            {
                flag = true;
                if (isTabInven() && isnewInventory)
                {
                    if (selected > 0 && sellectInventory == 0)
                    {
                        selected--;
                    }
                }
                else
                {
                    selected--;
                    if (type == TYPE_GAMEINFOSUB)
                    {
                        selected -= 2;
                        if (selected < 0)
                        {
                            selected = 0;
                        }
                    }
                    else if (selected < 0)
                    {
                        if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                        {
                            InfoDlg.showWait();
                            if (currPageShop[currentTabIndex] <= 0)
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, maxPageShop[currentTabIndex] - 1, -1);
                            }
                            else
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] - 1, -1);
                            }
                            return;
                        }
                        selected = currentListLength - 1;
                        if (isClanOption)
                        {
                            selected = -1;
                        }
                        if (size_tab > 0)
                        {
                            selected = -1;
                        }
                    }
                    lastSelect[currentTabIndex] = selected;
                    cSelected = 0;
                    getCurrClanOtion();
                }
            }
            else if (GameKeys.IsDownPressed())
            {
                flag = true;
                if (isTabInven() && isnewInventory)
                {
                    if (selected < 1 && sellectInventory == 0)
                    {
                        selected++;
                    }
                }
                else
                {
                    selected++;
                    if (type == 24)
                    {
                        selected += 2;
                        if (selected > currentListLength - 1)
                        {
                            selected = currentListLength - 1;
                        }
                    }
                    else if (selected > currentListLength - 1)
                    {
                        if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                        {
                            InfoDlg.showWait();
                            if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                            }
                            else
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                            }
                            return;
                        }
                        selected = 0;
                    }
                    lastSelect[currentTabIndex] = selected;
                    cSelected = 0;
                    getCurrClanOtion();
                }
            }
            if (isnewInventory && GameCanvas.keyPressed[5] && itemInvenNew != null)
            {
                pointerDownTime = 0;
                waitToPerform = 2;
            }
            if (flag)
            {
                cmtoY = selected * ITEM_HEIGHT - hScroll / 2;
                if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
                cmy = cmtoY;
            }
            if (GameCanvas.isPointerDown)
            {
                justRelease = false;
                if (!pointerIsDowning && GameCanvas.isPointer(xScroll, yScroll, wScroll, hScroll))
                {
                    for (int i = 0; i < pointerDownLastX.Length; i++)
                    {
                        pointerDownLastX[0] = GameCanvas.py;
                    }
                    pointerDownFirstX = GameCanvas.py;
                    pointerIsDowning = true;
                    isDownWhenRunning = cmRun != 0;
                    cmRun = 0;
                }
                else if (pointerIsDowning)
                {
                    pointerDownTime++;
                    if (pointerDownTime > 5 && pointerDownFirstX == GameCanvas.py && !isDownWhenRunning)
                    {
                        pointerDownFirstX = -1000;
                        int clickY = cmtoY + GameCanvas.py - yScroll;
                        if (clickY < 0)
                        {
                            selected = -1;
                        }
                        else
                        {
                            selected = clickY / ITEM_HEIGHT;
                        }
                        if (selected >= currentListLength)
                        {
                            selected = -1;
                        }
                        checkOptionSelect();
                    }
                    else
                    {
                        indexMouse = -1;
                    }
                    int num = GameCanvas.py - pointerDownLastX[0];
                    if (num != 0 && selected != -1)
                    {
                        selected = -1;
                        cSelected = -1;
                    }
                    for (int num2 = pointerDownLastX.Length - 1; num2 > 0; num2--)
                    {
                        pointerDownLastX[num2] = pointerDownLastX[num2 - 1];
                    }
                    pointerDownLastX[0] = GameCanvas.py;
                    cmtoY -= num;
                    if (cmtoY < 0)
                    {
                        cmtoY = 0;
                    }
                    if (cmtoY > cmyLim)
                    {
                        cmtoY = cmyLim;
                    }
                    if (cmy < 0 || cmy > cmyLim)
                    {
                        num /= 2;
                    }
                    cmy -= num;
                    if (cmy < -(GameCanvas.h / 3))
                    {
                        wantUpdateList = true;
                    }
                    else
                    {
                        wantUpdateList = false;
                    }
                    if (isnewInventory)
                    {
                        int num3 = GameCanvas.px - xScroll;
                        int num4 = GameCanvas.py - yScroll;
                        sellectInventory = num4 / 34 * 5 + num3 / 34;
                    }
                }
            }
            if (!GameCanvas.isPointerJustRelease || !pointerIsDowning)
            {
                return;
            }
            justRelease = true;
            int i2 = GameCanvas.py - pointerDownLastX[0];
            GameCanvas.isPointerJustRelease = false;
            if (Res.abs(i2) < 20 && Res.abs(GameCanvas.py - pointerDownFirstX) < 20 && !isDownWhenRunning)
            {
                cmRun = 0;
                cmtoY = cmy;
                if (cmtoY < 0)
                {
                    cmtoY = (cmy = 0);
                }
                pointerDownFirstX = -1000;
                int clickY = cmtoY + GameCanvas.py - yScroll;
                if (clickY < 0)
                {
                    selected = -1;
                }
                else
                {
                    selected = clickY / ITEM_HEIGHT;
                }
                if (selected >= currentListLength)
                {
                    selected = -1;
                }
                checkOptionSelect();
                pointerDownTime = 0;
                waitToPerform = 10;
                if (isnewInventory)
                {
                    waitToPerform = -1;
                }
                SoundMn.gI().panelClick();
            }
            else if (selected != -1 && pointerDownTime > 5)
            {
                pointerDownTime = 0;
                waitToPerform = 1;
            }
            else if (selected == -1 && !isDownWhenRunning)
            {
                if (cmy < 0)
                {
                    cmtoY = 0;
                }
                else if (cmy > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                else
                {
                    int num5 = GameCanvas.py - pointerDownLastX[0] + (pointerDownLastX[0] - pointerDownLastX[1]) + (pointerDownLastX[1] - pointerDownLastX[2]);
                    num5 = ((num5 > 10) ? 10 : ((num5 < -10) ? (-10) : 0));
                    cmRun = -num5 * 100;
                }
            }
            int num6 = 0;
            if ((isTabInven() || type == 13) && GameCanvas.py < yScroll + 21)
            {
                selected = 0;
    
                updateKeyInvenTab();
            }
            pointerIsDowning = false;
            pointerDownTime = 0;
            GameCanvas.isPointerJustRelease = false;
        }
    
        public string subArray(string[] str)
        {
            return null;
        }
    
        private void updateKeyInTabBar()
        {
            if ((scroll != null && scroll.pointerIsDowning) || pointerIsDowning)
            {
                return;
            }
            int num = currentTabIndex;
            if (isTabInven() && isnewInventory)
            {
                if (selected == -1)
                {
                    if (GameCanvas.keyPressed[6])
                    {
                        currentTabIndex++;
                        if (currentTabIndex >= currentTabName.Length)
                        {
                            if (GameCanvas.panel2 != null)
                            {
                                currentTabIndex = currentTabName.Length - 1;
                                GameCanvas.isFocusPanel2 = true;
                            }
                            else
                            {
                                currentTabIndex = 0;
                            }
                        }
                        selected = lastSelect[currentTabIndex];
                        lastTabIndex[type] = currentTabIndex;
                    }
                    if (GameCanvas.keyPressed[4])
                    {
                        currentTabIndex--;
                        if (currentTabIndex < 0)
                        {
                            currentTabIndex = currentTabName.Length - 1;
                        }
                        if (GameCanvas.isFocusPanel2)
                        {
                            GameCanvas.isFocusPanel2 = false;
                        }
                        selected = lastSelect[currentTabIndex];
                        lastTabIndex[type] = currentTabIndex;
                    }
                }
                else if (selected > 0)
                {
                    if (GameCanvas.keyPressed[8])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory++;
                        }
                        else
                        {
                            sellectInventory += 5;
                        }
                    }
                    else if (GameCanvas.keyPressed[2])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory--;
                        }
                        else
                        {
                            sellectInventory -= 5;
                        }
                    }
                    else if (GameCanvas.keyPressed[4])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory -= 5;
                        }
                        else
                        {
                            sellectInventory--;
                        }
                    }
                    else if (GameCanvas.keyPressed[6])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory += 5;
                        }
                        else
                        {
                            sellectInventory++;
                        }
                    }
                }
                if (sellectInventory < 0)
                {
                }
                if (sellectInventory == nTableItem)
                {
                    sellectInventory = 0;
                }
            }
            else if (!IsTabOption())
            {
                if (GameKeys.IsRightPressed())
                {
                    if (isTabInven())
                    {
                        if (selected >= 0)
                        {
                            updateKeyInvenTab();
                        }
                        else
                        {
                            currentTabIndex++;
                            if (currentTabIndex >= currentTabName.Length)
                            {
                                if (GameCanvas.panel2 != null)
                                {
                                    currentTabIndex = currentTabName.Length - 1;
                                    GameCanvas.isFocusPanel2 = true;
                                }
                                else
                                {
                                    currentTabIndex = 0;
                                }
                            }
                            selected = lastSelect[currentTabIndex];
                            lastTabIndex[type] = currentTabIndex;
                        }
                    }
                    else
                    {
                        currentTabIndex++;
                        if (currentTabIndex >= currentTabName.Length)
                        {
                            if (GameCanvas.panel2 != null)
                            {
                                currentTabIndex = currentTabName.Length - 1;
                                GameCanvas.isFocusPanel2 = true;
                            }
                            else
                            {
                                currentTabIndex = 0;
                            }
                        }
                        selected = lastSelect[currentTabIndex];
                        lastTabIndex[type] = currentTabIndex;
                    }
                }
                if (GameKeys.IsLeftPressed())
                {
                    currentTabIndex--;
                    if (currentTabIndex < 0)
                    {
                        currentTabIndex = currentTabName.Length - 1;
                    }
                    if (GameCanvas.isFocusPanel2)
                    {
                        GameCanvas.isFocusPanel2 = false;
                    }
                    selected = lastSelect[currentTabIndex];
                    lastTabIndex[type] = currentTabIndex;
                }
            }
            keyTouchTab = -1;
            for (int i = 0; i < currentTabName.Length; i++)
            {
                if (!GameCanvas.isPointer(startTabPos + i * TAB_W, 52, TAB_W - 1, 25))
                {
                    continue;
                }
                keyTouchTab = i;
                if (GameCanvas.isPointerJustRelease)
                {
                    currentTabIndex = i;
                    lastTabIndex[type] = i;
                    GameCanvas.isPointerJustRelease = false;
                    selected = lastSelect[currentTabIndex];
                    if (num == currentTabIndex && cmRun == 0)
                    {
                        cmtoY = 0;
                        selected = (GameCanvas.isTouch ? (-1) : 0);
                    }
                    break;
                }
            }
            if (num == currentTabIndex)
            {
                return;
            }
            size_tab = 0;
            SoundMn.gI().panelClick();
            switch (type)
            {
                case 21:
                    if (currentTabIndex == 0)
                    {
                        setTabPetInventory();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabPetStatus();
                    }
                    if (currentTabIndex == 2)
                    {
                        setTabInventory(true);
                    }
                    break;
                case 0:
                    if (currentTabIndex == 0)
                    {
                        setTabTask();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabInventory(true);
                    }
                    if (currentTabIndex == 2)
                    {
                        setTabSkill();
                    }
                    if (currentTabIndex == 3)
                    {
                        if (mainTabName.Length > 4)
                        {
                            setTabClans();
                        }
                        else
                        {
                            setTabTool();
                        }
                    }
                    if (currentTabIndex == 4)
                    {
                        setTabTool();
                    }
                    break;
                case 2:
                    if (currentTabIndex == 0)
                    {
                        setTabBox();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabInventory(true);
                    }
                    break;
                case 3:
                    setTabZone();
                    break;
                case 1:
                    setTabShop();
                    break;
                case 25:
                    setTabSpeacialSkill();
                    break;
                case 12:
                    if (currentTabIndex == 0)
                    {
                        setTabCombine();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabInventory(true);
                    }
                    break;
                case 13:
                    if (currentTabIndex == 0)
                    {
                        if (Equals(GameCanvas.panel))
                        {
                            setTabInventory(true);
                        }
                        else if (Equals(GameCanvas.panel2))
                        {
                            setTabGiaoDich(false);
                        }
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabGiaoDich(true);
                    }
                    if (currentTabIndex == 2)
                    {
                        setTabGiaoDich(false);
                    }
                    break;
                case 28:
                    if (currentTabIndex == 0)
                    {
                        AutoSettingManager.getInstance().setTabAutoSetting(this);
                    }
                    else if (currentTabIndex == 1)
                    {
                        AutoSettingManager.getInstance().setTabMobTanSat(this);
                    }
                    break;
            }
            selected = lastSelect[currentTabIndex];
        }
    
        private void setTabPetStatus()
        {
            currentListLength = strStatus.Length;
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
    
        private void setTabPetSkill()
        {
        }
    
        private void setTabTool()
        {
            SoundMn.gI().getSoundOption();
            currentListLength = strTool.Length;
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
    
        public void initTabClans()
        {
            if (isSearchClan)
            {
                currentListLength = ((clans != null) ? (clans.Length + 2) : 2);
                clanInfo = mResources.clan_list;
            }
            else if (isViewMember)
            {
                clanReport = string.Empty;
                currentListLength = ((member != null) ? member.size() : myMember.size()) + 2;
                clanInfo = mResources.member + " " + ((currClan == null) ? Char.myCharz().clan.name : currClan.name);
            }
            else if (isMessage)
            {
                currentListLength = ClanMessage.vMessage.size() + 2;
                clanInfo = mResources.msg;
                clanReport = string.Empty;
            }
            if (Char.myCharz().clan == null)
            {
                clansOption = new string[2][]
                {
                    mResources.findClan,
                    mResources.createClan
                };
            }
            else if (!isViewMember)
            {
                if (myMember.size() > 1)
                {
                    clansOption = new string[3][]
                    {
                        mResources.chatClan,
                        mResources.request_pea2,
                        mResources.memberr
                    };
                }
                else
                {
                    clansOption = new string[1][] { mResources.memberr };
                }
            }
            else if (Char.myCharz().role > 0)
            {
                clansOption = new string[2][]
                {
                    mResources.msgg,
                    mResources.leaveClan
                };
            }
            else if (myMember.size() > 1)
            {
                clansOption = new string[4][]
                {
                    mResources.msgg,
                    mResources.leaveClan,
                    mResources.khau_hieuu,
                    mResources.bieu_tuongg
                };
            }
            else
            {
                clansOption = new string[3][]
                {
                    mResources.msgg,
                    mResources.khau_hieuu,
                    mResources.bieu_tuongg
                };
            }
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
    
    
        public void initLogMessage()
        {
            currentListLength = logChat.size() + 1;
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
            cmx = (cmtoX = 0);
        }
    
        private void setTabMessage()
        {
            ITEM_HEIGHT = 24;
            initLogMessage();
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
    
        private void setTabSkill()
        {
            ITEM_HEIGHT = 30;
            currentListLength = Char.myCharz().nClass.skillTemplates.Length + 6;
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
                cmy = cmyLim;
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabMapTrans()
        {
            ITEM_HEIGHT = 24;
            currentListLength = mapNames.Length;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = 0);
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabZone()
        {
            ITEM_HEIGHT = 24;
            currentListLength = GameScr.gI().zones.Length;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = 0);
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
    
        private void setTabPetInventory()
        {
            ITEM_HEIGHT = 30;
            if (Char.myPetz() == null)
            {
                currentListLength = 0;
                return;
            }
            Item[] arrItemBody = Char.myPetz().arrItemBody;
            Skill[] arrPetSkill = Char.myPetz().arrPetSkill;
            int len1 = (arrItemBody != null) ? arrItemBody.Length : 0;
            int len2 = (arrPetSkill != null) ? arrPetSkill.Length : 0;
            currentListLength = len1 + len2;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (currentTabIndex >= 0 && currentTabIndex < cmyLast.Length)
            {
                cmy = (cmtoY = cmyLast[currentTabIndex]);
            }
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = 0);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
    
        private void setTabMap()
        {
            if (!isPaintMap)
            {
                return;
            }
            if (TileMap.lastPlanetId != TileMap.planetID)
            {
                Res.outz("LOAD TAM HINH");
                imgMap = GameCanvas.loadImageRMS("/img/map" + TileMap.planetID + ".png");
                TileMap.lastPlanetId = TileMap.planetID;
            }
            cmxMap = getXMap() - wScroll / 2;
            cmyMap = getYMap() + yScroll - (yScroll + hScroll / 2);
            pa1 = cmxMap;
            pa2 = cmyMap;
            cmxMapLim = 250 - wScroll;
            cmyMapLim = 220 - hScroll;
            if (cmxMapLim < 0)
            {
                cmxMapLim = 0;
            }
            if (cmyMapLim < 0)
            {
                cmyMapLim = 0;
            }
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (TileMap.mapID == mapId[TileMap.planetID][i])
                {
                    xMove = mapX[TileMap.planetID][i] + xScroll;
                    yMove = mapY[TileMap.planetID][i] + yScroll + 5;
                    break;
                }
            }
            xMap = getXMap() + xScroll;
            yMap = getYMap() + yScroll;
            xMapTask = getXMapTask() + xScroll;
            yMapTask = getYMapTask() + yScroll;
            Resources.UnloadUnusedAssets();
        }
    
        private void setTabTask()
        {
            cmyQuest = 0;
        }
    
        public void moveCamera()
        {
            if (timeShow > 0)
            {
                timeShow--;
            }
            if (justRelease && Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1)
            {
                if (cmy < -50)
                {
                    InfoDlg.showWait();
                    justRelease = false;
                    if (currPageShop[currentTabIndex] <= 0)
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, maxPageShop[currentTabIndex] - 1, -1);
                    }
                    else
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] - 1, -1);
                    }
                }
                else if (cmy > cmyLim + 50)
                {
                    justRelease = false;
                    InfoDlg.showWait();
                    if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                    }
                    else
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                    }
                }
            }
            if (cmx != cmtoX && !pointerIsDowning)
            {
                cmvx = cmtoX - cmx << 2;
                cmdx += cmvx;
                cmx += cmdx >> 3;
                cmdx &= 15;
            }
            if (Math.abs(cmtoX - cmx) < 10)
            {
                cmx = cmtoX;
            }
            if (isClose)
            {
                isClose = false;
                cmtoX = wScroll;
            }
            if (cmtoX >= wScroll - 10 && cmx >= wScroll - 10 && position == 0)
            {
                isShow = false;
                cleanCombine();
                if (isChangeZone)
                {
                    isChangeZone = false;
                    if (Char.myCharz().cHP > 0 && Char.myCharz().statusMe != 14)
                    {
                        InfoDlg.showWait();
                        if (type == 3)
                        {
                            Service.gI().requestChangeZone(selected, -1);
                        }
                        else if (type == 14)
                        {
                            Service.gI().requestMapSelect(selected);
                        }
                    }
                }
                if (isSelectPlayerMenu)
                {
                    isSelectPlayerMenu = false;
                    int num = vPlayerMenu.size() - vPlayerMenu_id.size();
                    if (Char.myCharz().charFocus != null)
                    {
                        if (selected - num < 0)
                        {
                            Char.myCharz().charFocus.menuSelect = selected;
                        }
                        else
                        {
                            Char.myCharz().charFocus.menuSelect = short.Parse((string)vPlayerMenu_id.elementAt(selected - num));
                        }
                    }
                    Command command = (Command)vPlayerMenu.elementAt(selected);
                    command.performAction();
                }
                vPlayerMenu.removeAllElements();
                vPlayerMenu_id.removeAllElements();
                charMenu = null;
            }
            if (cmRun != 0 && !pointerIsDowning)
            {
                cmtoY += cmRun / 100;
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
                else if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                else
                {
                    cmy = cmtoY;
                }
                cmRun = cmRun * 9 / 10;
                if (cmRun < 100 && cmRun > -100)
                {
                    cmRun = 0;
                }
            }
            if (cmy != cmtoY && !pointerIsDowning)
            {
                cmvy = cmtoY - cmy << 2;
                cmdy += cmvy;
                cmy += cmdy >> 4;
                cmdy &= 15;
            }
            cmyLast[currentTabIndex] = cmy;
        }
    
        public void paintDetail(mGraphics g)
        {
            if (cp == null || cp.says == null)
            {
                return;
            }
            cp.paint(g);
            int num = cp.cx + 13;
            int num2 = cp.cy + 11;
            if (type == 15)
            {
                num += 5;
                num2 += 26;
            }
            if (type == 0 && currentTabIndex == 3)
            {
                if (isSearchClan)
                {
                    num -= 5;
                }
                else if (partID != null || charInfo != null)
                {
                    num = cp.cx + 21;
                    num2 = cp.cy + 40;
                }
            }
            if (partID != null)
            {
                Part part = GameScr.parts[partID[0]];
                Part part2 = GameScr.parts[partID[1]];
                Part part3 = GameScr.parts[partID[2]];
                SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + Char.CharInfo[0][0][1] + part.pi[Char.CharInfo[0][0][0]].dx, num2 - Char.CharInfo[0][0][2] + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                SmallImage.drawSmallImage(g, part2.pi[Char.CharInfo[0][1][0]].id, num + Char.CharInfo[0][1][1] + part2.pi[Char.CharInfo[0][1][0]].dx, num2 - Char.CharInfo[0][1][2] + part2.pi[Char.CharInfo[0][1][0]].dy, 0, 0);
                SmallImage.drawSmallImage(g, part3.pi[Char.CharInfo[0][2][0]].id, num + Char.CharInfo[0][2][1] + part3.pi[Char.CharInfo[0][2][0]].dx, num2 - Char.CharInfo[0][2][2] + part3.pi[Char.CharInfo[0][2][0]].dy, 0, 0);
            }
            else if (charInfo != null)
            {
                charInfo.paintCharBody(g, num + 5, num2 + 25, 1, 0, true);
            }
            else if (idIcon != -1)
            {
                SmallImage.drawSmallImage(g, idIcon, cp.cx + 8, cp.cy + 2, 0, mGraphics.TOP | mGraphics.LEFT);
            }
            if (currItem != null && currItem.template.type != 5)
            {
                if (currItem.compare > 0)
                {
                    g.drawImage(imgUp, num - 7, num2 + 13, 3);
                    mFont.tahoma_7b_green.drawString(g, Res.abs(currItem.compare).ToString(), num + 1, num2 + 8, 0);
                }
                else if (currItem.compare < 0 && currItem.compare != -1)
                {
                    g.drawImage(imgDown, num - 7, num2 + 13, 3);
                    mFont.tahoma_7b_red.drawString(g, Res.abs(currItem.compare).ToString(), num + 1, num2 + 8, 0);
                }
            }
        }
    }
}

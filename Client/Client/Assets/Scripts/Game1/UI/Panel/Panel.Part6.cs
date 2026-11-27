namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        public void paintTask(mGraphics g)
        {
            int num = ((GameCanvas.h <= 300) ? 15 : 20);
            if (isPaintMap && !GameScr.gI().isMapDocNhan() && !GameScr.gI().isMapFize())
            {
                g.drawImage((keyTouchMapButton != 1) ? GameScr.imgLbtn : GameScr.imgLbtnFocus, xScroll + wScroll / 2, yScroll + hScroll - num, 3);
                mFont.tahoma_7b_dark.drawString(g, mResources.map, xScroll + wScroll / 2, yScroll + hScroll - (num + 5), mFont.CENTER);
            }
            xstart = xScroll + 5;
            ystart = yScroll + 14;
            yPaint = ystart;
            g.setClip(xScroll, yScroll, wScroll, hScroll - 35);
            if (scroll != null)
            {
                if (scroll.cmy > 0)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll - 12, yScroll + 3, 0);
                }
                if (scroll.cmy < scroll.cmyLim)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll - 12, yScroll + hScroll - 45, 0);
                }
                g.translate(0, -scroll.cmy);
            }
            indexRowMax = 0;
            if (indexMenu == 0)
            {
                bool flag = false;
                if (Char.myCharz().taskMaint != null)
                {
                    for (int i = 0; i < Char.myCharz().taskMaint.names.Length; i++)
                    {
                        mFont.tahoma_7_grey.drawString(g, Char.myCharz().taskMaint.names[i], xScroll + wScroll / 2, yPaint - 5 + i * 12, mFont.CENTER);
                        indexRowMax++;
                    }
                    yPaint += (Char.myCharz().taskMaint.names.Length - 1) * 12;
                    int num2 = 0;
                    string empty = string.Empty;
                    for (int j = 0; j < Char.myCharz().taskMaint.subNames.Length; j++)
                    {
                        if (Char.myCharz().taskMaint.subNames[j] != null)
                        {
                            num2 = j;
                            empty = "- " + Char.myCharz().taskMaint.subNames[j];
                            if (Char.myCharz().taskMaint.counts[j] != -1)
                            {
                                if (Char.myCharz().taskMaint.index == j)
                                {
                                    if (Char.myCharz().taskMaint.counts[j] != 1)
                                    {
                                        string text = empty;
                                        empty = text + " (" + Char.myCharz().taskMaint.count + "/" + Char.myCharz().taskMaint.counts[j] + ")";
                                    }
                                    if (Char.myCharz().taskMaint.count == Char.myCharz().taskMaint.counts[j])
                                    {
                                        mFont.tahoma_7.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                                    }
                                    else
                                    {
                                        mFont tahoma_7_grey = mFont.tahoma_7_grey;
                                        if (!flag)
                                        {
                                            flag = true;
                                            tahoma_7_grey = mFont.tahoma_7_blue;
                                            tahoma_7_grey.drawString(g, empty, xstart + 5 + ((tahoma_7_grey == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                        }
                                        else
                                        {
                                            tahoma_7_grey.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                        }
                                    }
                                }
                                else if (Char.myCharz().taskMaint.index > j)
                                {
                                    if (Char.myCharz().taskMaint.counts[j] != 1)
                                    {
                                        string text = empty;
                                        empty = text + " (" + Char.myCharz().taskMaint.counts[j] + "/" + Char.myCharz().taskMaint.counts[j] + ")";
                                    }
                                    mFont.tahoma_7_white.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                                }
                                else
                                {
                                    if (Char.myCharz().taskMaint.counts[j] != 1)
                                    {
                                        empty = empty + " 0/" + Char.myCharz().taskMaint.counts[j];
                                    }
                                    mFont tahoma_7_grey2 = mFont.tahoma_7_grey;
                                    if (!flag)
                                    {
                                        flag = true;
                                        tahoma_7_grey2 = mFont.tahoma_7_blue;
                                        tahoma_7_grey2.drawString(g, empty, xstart + 5 + ((tahoma_7_grey2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                    }
                                    else
                                    {
                                        tahoma_7_grey2.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                    }
                                }
                            }
                            else if (Char.myCharz().taskMaint.index > j)
                            {
                                mFont.tahoma_7_white.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                            }
                            else
                            {
                                mFont tahoma_7_grey3 = mFont.tahoma_7_grey;
                                if (!flag)
                                {
                                    flag = true;
                                    tahoma_7_grey3 = mFont.tahoma_7_blue;
                                    tahoma_7_grey3.drawString(g, empty, xstart + 5 + ((tahoma_7_grey3 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                }
                                else
                                {
                                    tahoma_7_grey3.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey3 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                }
                            }
                            indexRowMax++;
                        }
                        else if (Char.myCharz().taskMaint.index <= j)
                        {
                            empty = "- " + Char.myCharz().taskMaint.subNames[num2];
                            mFont mFont2 = mFont.tahoma_7_grey;
                            if (!flag)
                            {
                                flag = true;
                                mFont2 = mFont.tahoma_7_blue;
                            }
                            mFont2.drawString(g, empty, xstart + 5 + ((mFont2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                        }
                    }
                    yPaint += 5;
                    for (int k = 0; k < Char.myCharz().taskMaint.details.Length; k++)
                    {
                        mFont.tahoma_7_green2.drawString(g, Char.myCharz().taskMaint.details[k], xstart + 5, yPaint += 12, 0);
                        indexRowMax++;
                    }
                }
                else
                {
                    int taskMapId = GameScr.getTaskMapId();
                    sbyte taskNpcId = GameScr.getTaskNpcId();
                    string empty2 = string.Empty;
                    if (taskMapId == -3 || taskNpcId == -3)
                    {
                        empty2 = mResources.DES_TASK[3];
                    }
                    else if (Char.myCharz().taskMaint == null && Char.myCharz().ctaskId == 9 && Char.myCharz().nClass.classId == 0)
                    {
                        empty2 = mResources.TASK_INPUT_CLASS;
                    }
                    else
                    {
                        if (taskNpcId < 0 || taskMapId < 0)
                        {
                            return;
                        }
                        empty2 = mResources.DES_TASK[0] + Npc.arrNpcTemplate[taskNpcId].name + mResources.DES_TASK[1] + TileMap.mapNames[taskMapId] + mResources.DES_TASK[2];
                    }
                    string[] array = mFont.tahoma_7_white.splitFontArray(empty2, 150);
                    for (int l = 0; l < array.Length; l++)
                    {
                        if (l == 0)
                        {
                            mFont.tahoma_7_white.drawString(g, array[l], xstart + 5, yPaint = ystart, 0);
                        }
                        else
                        {
                            mFont.tahoma_7_white.drawString(g, array[l], xstart + 5, yPaint += 12, 0);
                        }
                    }
                }
            }
            else if (indexMenu == 1)
            {
                yPaint = ystart - 12;
                for (int m = 0; m < Char.myCharz().taskOrders.size(); m++)
                {
                    TaskOrder taskOrder = (TaskOrder)Char.myCharz().taskOrders.elementAt(m);
                    mFont.tahoma_7_white.drawString(g, taskOrder.name, xstart + 5, yPaint += 12, 0);
                    if (taskOrder.count == taskOrder.maxCount)
                    {
                        mFont.tahoma_7_white.drawString(g, ((taskOrder.taskId != 0) ? mResources.KILLBOSS : mResources.KILL) + " " + Mob.arrMobTemplate[taskOrder.killId].name + " (" + taskOrder.count + "/" + taskOrder.maxCount + ")", xstart + 5, yPaint += 12, 0);
                    }
                    else
                    {
                        mFont.tahoma_7_blue.drawString(g, ((taskOrder.taskId != 0) ? mResources.KILLBOSS : mResources.KILL) + " " + Mob.arrMobTemplate[taskOrder.killId].name + " (" + taskOrder.count + "/" + taskOrder.maxCount + ")", xstart + 5, yPaint += 12, 0);
                    }
                    indexRowMax += 3;
                    inforW = popupW - 25;
                    paintMultiLine(g, mFont.tahoma_7_grey, taskOrder.description, xstart + 5, yPaint += 12, 0);
                    yPaint += 12;
                }
            }
            if (scroll == null)
            {
                scroll = new Scroll();
                scroll.setStyle(indexRowMax, 12, xScroll, yScroll, wScroll, hScroll - num - 40, true, 1);
            }
        }
    
        public void paintMultiLine(mGraphics g, mFont f, string[] arr, string str, int x, int y, int align)
        {
            for (int i = 0; i < arr.Length; i++)
            {
                string text = arr[i];
                if (text.StartsWith("c"))
                {
                    if (text.StartsWith("c0"))
                    {
                        text = text.Substring(2);
                        f = mFont.tahoma_7b_dark;
                    }
                    else if (text.StartsWith("c1"))
                    {
                        text = text.Substring(2);
                        f = mFont.tahoma_7b_yellow;
                    }
                    else if (text.StartsWith("c2"))
                    {
                        text = text.Substring(2);
                        f = mFont.tahoma_7b_green;
                    }
                }
                if (i == 0)
                {
                    f.drawString(g, text, x, y, align);
                    continue;
                }
                if (i < indexRow + 30 && i > indexRow - 30)
                {
                    f.drawString(g, text, x, y += 12, align);
                }
                else
                {
                    y += 12;
                }
                yPaint += 12;
                indexRowMax++;
            }
        }
    
        public void paintMultiLine(mGraphics g, mFont f, string str, int x, int y, int align)
        {
            int num = ((!GameCanvas.isTouch || GameCanvas.w < 320) ? 10 : 20);
            string[] array = f.splitFontArray(str, inforW - num);
            for (int i = 0; i < array.Length; i++)
            {
                if (i == 0)
                {
                    f.drawString(g, array[i], x, y, align);
                    continue;
                }
                if (i < indexRow + 15 && i > indexRow - 15)
                {
                    f.drawString(g, array[i], x, y += 12, align);
                }
                else
                {
                    y += 12;
                }
                yPaint += 12;
                indexRowMax++;
            }
        }
    
        public void cleanCombine()
        {
            for (int i = 0; i < vItemCombine.size(); i++)
            {
                ((Item)vItemCombine.elementAt(i)).isSelect = false;
            }
            vItemCombine.removeAllElements();
        }
    
        public void hideNow()
        {
            if (timeShow > 0)
            {
                isClose = false;
                return;
            }
            cp = null;
            if (isTypeShop() || TileMap.mapID == 45)
            {
                Char.myCharz().resetPartTemp();
            }
            if (chatTField != null && type == 13 && chatTField.isShow)
            {
                chatTField = null;
            }
            if (type == 13 && !isAccept)
            {
                Service.gI().giaodich(3, -1, -1, -1);
            }
            Res.outz("HIDE PANELLLLLLLLLLLLLLLLLLLLLL");
            SoundMn.gI().buttonClose();
            GameScr.isPaint = true;
            TileMap.lastPlanetId = -1;
            imgMap = null;
            mSystem.gcc();
            isClanOption = false;
            isClose = true;
            cleanCombine();
            Hint.clickNpc();
            GameCanvas.panel2 = null;
            GameCanvas.clearAllPointerEvent();
            GameCanvas.clearKeyPressed();
            pointerDownTime = (pointerDownFirstX = 0);
            pointerIsDowning = false;
            isShow = false;
            if ((Char.myCharz().cHP <= 0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5) && Char.myCharz().meDead)
            {
                GameScr.gI().center = GetDeadCenterCommand();
                Char.myCharz().cHP = 0;
            }
        }

        private static Command cmdDeadCenter;
        private static Command GetDeadCenterCommand()
        {
            if (cmdDeadCenter == null)
            {
                cmdDeadCenter = new Command(mResources.DIES[0], 11038, GameScr.gI());
            }
            return cmdDeadCenter;
        }
    
        public void hide()
        {
            if (timeShow > 0)
            {
                isClose = false;
                return;
            }
            GetCurrentSubPanel()?.OnClose();
            cp = null;
            if (isTypeShop() || TileMap.mapID == 45)
            {
                Char.myCharz().resetPartTemp();
            }
            if (chatTField != null && type == 13 && chatTField.isShow)
            {
                chatTField = null;
            }
            if (type == 13 && !isAccept)
            {
                Service.gI().giaodich(3, -1, -1, -1);
            }
            if (type == 15)
            {
                Service.gI().sendThachDau(-1);
            }
            SoundMn.gI().buttonClose();
            GameScr.isPaint = true;
            TileMap.lastPlanetId = -1;
            if (imgMap != null)
            {
                imgMap.texture = null;
                imgMap = null;
            }
            mSystem.gcc();
            isClanOption = false;
            if (type != 4)
            {
                if (type == 24)
                {
                    setTypeGameInfo();
                }
                else if(type == 30)
                {
                    setTypeMain();
                }
                else if (type == 23)
                {
                    setTypeMain();
                }
                else if (type == 3 || type == 14)
                {
                    if (isChangeZone)
                    {
                        isClose = true;
                    }
                    else
                    {
                        setTypeMain();
                        cmx = (cmtoX = 0);
                    }
                }
                else if (type == 18 || type == 19 || type == 20 || type == 21)
                {
                    setTypeMain();
                    cmx = (cmtoX = 0);
                }
                else if (type == 8 || type == 11 || type == 16)
                {
                    setTypeAccount();
                    cmx = (cmtoX = 0);
                }
                else
                {
                    isClose = true;
                }
            }
            else
            {
                setTypeMain();
                cmx = (cmtoX = 0);
            }
            Hint.clickNpc();
            GameCanvas.panel2 = null;
            GameCanvas.clearAllPointerEvent();
            GameCanvas.clearKeyPressed();
            GameCanvas.isFocusPanel2 = false;
            pointerDownTime = (pointerDownFirstX = 0);
            pointerIsDowning = false;
            if ((Char.myCharz().cHP <= 0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5) && Char.myCharz().meDead)
            {
                GameScr.gI().center = GetDeadCenterCommand();
                Char.myCharz().cHP = 0;
            }
        }
    
        public void update()
        {
            ISubPanel activeSubPanel = GetCurrentSubPanel();
            if (activeSubPanel != null)
            {
                activeSubPanel.Update();
            }
            if (chatTField != null && chatTField.isShow)
            {
                chatTField.update();
                return;
            }
            if (isKiguiXu)
            {
                delayKigui++;
                if (delayKigui == 10)
                {
                    delayKigui = 0;
                    isKiguiXu = false;
                    chatTField.tfChat.setText(string.Empty);
                    chatTField.strChat = mResources.kiguiXuchat + " ";
                    chatTField.tfChat.name = mResources.input_money;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                    chatTField.tfChat.setMaxTextLenght(10);
                    if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                }
                return;
            }
            if (isKiguiLuong)
            {
                delayKigui++;
                if (delayKigui == 10)
                {
                    delayKigui = 0;
                    isKiguiLuong = false;
                    chatTField.tfChat.setText(string.Empty);
                    chatTField.strChat = mResources.kiguiLuongchat + "  ";
                    chatTField.tfChat.name = mResources.input_money;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                    chatTField.tfChat.setMaxTextLenght(10);
                    if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                }
                return;
            }
            if (scroll != null)
            {
                scroll.updatecm();
            }
            if (tabIcon != null && tabIcon.isShow)
            {
                tabIcon.update();
                return;
            }
            moveCamera();
            if (isTabInven() && isnewInventory)
            {
                if (eBanner == null)
                {
                    eBanner = new Effect(205, 0, 0, 3, 10, -1);
                    eBanner.typeEff = 2;
                }
                if (eBanner != null)
                {
                    eBanner.update();
                }
            }
            if (waitToPerform > 0)
            {
                waitToPerform--;
                if (waitToPerform == 0)
                {
                    lastSelect[currentTabIndex] = selected;
                    switch (type)
                    {
                        case 30:
                            doFireMoneyAward();
                            break;
                        case 23:
                            doFireGameInfo();
                            break;
                        case 21:
                            doFirePetMain();
                            break;
                        case 0:
                            doFireMain();
                            break;
                        case 2:
                            doFireBox();
                            break;
                        case 3:
                            doFireZone();
                            break;
                        case 1:
                        case 17:
                            doFireShop();
                            break;
                        case 25:
                            doSpeacialSkill();
                            break;
                        case 4:
                            doFireMap();
                            break;
                        case 14:
                            doFireMapTrans();
                            break;
                        case 7:
                            if (Equals(GameCanvas.panel2) && GameCanvas.panel.type == 2)
                            {
                                doFireBox();
                                return;
                            }
                            doFireInventory();
                            break;
                        case 8:
                            doFireLogMessage();
                            break;
                        case 9:
                            doFireArchivement();
                            break;
                        case 10:
                            doFirePlayerMenu();
                            break;
                        case 11:
                            doFireFriend();
                            break;
                        case 16:
                            doFireEnemy();
                            break;
                        case 15:
                            doFireTop();
                            break;
                        case 12:
                            doFireCombine();
                            break;
                        case 13:
                            doFireGiaoDich();
                            break;
                        case 18:
                            doFireChangeFlag();
                            break;
                        case 19:
                            doFireOption();
                            break;
                        case 20:
                            doFireAccount();
                            break;
                        case 22:
                            doFireAuto();
                            break;
                        case 28:
                            if (currentTabIndex == 0)
                            {
                                AutoSettingManager.getInstance().doFire(this, selected);
                            }
                            else if (currentTabIndex == 1)
                            {
                                AutoSettingManager.getInstance().doFireMobTanSat(this, selected);
                            }
                            break;
                    }
                }
            }
            for (int i = 0; i < ClanMessage.vMessage.size(); i++)
            {
                ((ClanMessage)ClanMessage.vMessage.elementAt(i)).update();
            }
            updateCombineEff();
        }
    
        private void doSpeacialSkill()
        {
        }
    
        private void doFireGameInfo()
        {
            if (selected != -1)
            {
                infoSelect = selected;
                ((GameInfo)vGameInfo.elementAt(infoSelect)).hasRead = true;
                Rms.saveRMSInt(((GameInfo)vGameInfo.elementAt(infoSelect)).id + string.Empty, 1);
                setTypeGameSubInfo();
            }
        }
        private void doFireMoneyAward()
        {
            if (selected != -1)
            {
            }
        }
    
        private void doFireAuto()
        {
        }
    
        private void doFirePetMain()
        {
            if (currentTabIndex == 0)
            {
                if (selected == -1 || selected > Char.myPetz().arrItemBody.Length - 1)
                {
                    return;
                }
                MyVector myVector = new MyVector(string.Empty);
                Item item = Char.myPetz().arrItemBody[selected];
                currItem = item;
                if (currItem != null)
                {
                    myVector.addElement(new Command(mResources.MOVEOUT, this, 2006, currItem));
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
            if (currentTabIndex == 1)
            {
                doFirePetStatus();
            }
            if (currentTabIndex == 2)
            {
                doFireInventory();
            }
        }
    
        private void doFirePetStatus()
        {
            if (selected == -1)
            {
                return;
            }
            if (selected == 5)
            {
                GameCanvas.startYesNoDlg(mResources.sure_fusion, new Command(mResources.YES, 888351), new Command(mResources.NO, 2001));
                return;
            }
            Service.gI().petStatus((sbyte)selected);
            if (selected < 4)
            {
                Char.myPetz().petStatus = (sbyte)selected;
            }
        }
    
        private void doFireTop()
        {
            if (selected >= 0 && selected < vTop.size())
            {
                TopInfo topInfo = (TopInfo)vTop.elementAt(selected);
                if (topInfo == null) return;
                MyVector myVector = new MyVector(string.Empty);
                if (topInfo.pId != Char.myCharz().charID)
                {
                    myVector.addElement(new Command("Thách đấu", this, 9998, topInfo));
                }
                myVector.addElement(new Command("Đóng", this, 9999, topInfo));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addThachDauDetail(topInfo);
            }
        }
    
        private void doFireMapTrans()
        {
            doFireZone();
        }
    
        private void doFireGiaoDich()
        {
            if (currentTabIndex == 0 && Equals(GameCanvas.panel))
            {
                doFireInventory();
                return;
            }
            if ((currentTabIndex == 0 && Equals(GameCanvas.panel2)) || currentTabIndex == 2)
            {
                if (Equals(GameCanvas.panel2))
                {
                    currItem = (Item)GameCanvas.panel2.vFriendGD.elementAt(selected);
                }
                else
                {
                    currItem = (Item)GameCanvas.panel.vFriendGD.elementAt(selected);
                }
                Res.outz2("toi day select= " + selected);
                MyVector myVector = new MyVector();
                myVector.addElement(new Command(mResources.CLOSE, this, 8000, currItem));
                if (currItem != null)
                {
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
            if (currentTabIndex == 1)
            {
                if (selected == currentListLength - 3)
                {
                    if (isLock)
                    {
                        return;
                    }
                    putMoney();
                }
                else if (selected == currentListLength - 2)
                {
                    if (!isAccept)
                    {
                        isLock = !isLock;
                        if (isLock)
                        {
                            Service.gI().giaodich(5, -1, -1, -1);
                        }
                        else
                        {
                            hide();
                            InfoDlg.showWait();
                            Service.gI().giaodich(3, -1, -1, -1);
                        }
                    }
                    else
                    {
                        isAccept = false;
                    }
                }
                else if (selected == currentListLength - 1)
                {
                    if (isLock && !isAccept && isFriendLock)
                    {
                        GameCanvas.startYesNoDlg(mResources.do_u_sure_to_trade, new Command(mResources.YES, this, 7002, null), new Command(mResources.NO, this, 4005, null));
                    }
                }
                else
                {
                    if (isLock)
                    {
                        return;
                    }
                    currItem = (Item)GameCanvas.panel.vMyGD.elementAt(selected);
                    MyVector myVector2 = new MyVector();
                    myVector2.addElement(new Command(mResources.CLOSE, this, 8000, currItem));
                    if (currItem != null)
                    {
                        GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addItemDetail(currItem);
                    }
                    else
                    {
                        cp = null;
                    }
                }
            }
            if (GameCanvas.isTouch)
            {
                selected = -1;
            }
        }
    
    
        private void doFirePlayerMenu()
        {
            if (selected != -1)
            {
                isSelectPlayerMenu = true;
                hide();
            }
        }
    
    
        private void doFireArchivement()
        {
            if (selected >= 0 && Char.myCharz().arrArchive[selected].isFinish && !Char.myCharz().arrArchive[selected].isRecieve)
            {
                if (!GameCanvas.isTouch)
                {
                    Service.gI().getArchivemnt(selected);
                }
                else if (GameCanvas.px > xScroll + wScroll - 40)
                {
                    Service.gI().getArchivemnt(selected);
                }
            }
        }
    
    
        private void doRada()
        {
            hide();
            if (RadarScr.list == null || RadarScr.list.size() == 0)
            {
                Service.gI().SendRada(0, -1);
                RadarScr.gI().switchToMe();
            }
            else
            {
                RadarScr.gI().switchToMe();
            }
        }
    
        private void doFireTool()
        {
            if (selected < 0)
            {
                return;
            }
            if (selected >= 0 && selected < strTool.Length && strTool[selected].Equals("Cài Đặt Auto"))
            {
                setTypeAutoSetting();
                return;
            }
            if (selected >= 0 && selected < strTool.Length && strTool[selected].Equals(mResources.change_account))
            {
                GameCanvas.loginScr.backToRegister();
                return;
            }
            if (SoundMn.IsDelAcc && selected == strTool.Length - 1)
            {
                Service.gI().sendDelAcc();
                return;
            }
            if (!Char.myCharz().havePet)
            {
                switch (selected)
                {
                    case 0:
                        hide();
                        doRada();
                        break;
                    case 1:
                        Service.gI().openMenu(54);
                        break;
                    case 2:
                        setTypeGameInfo();
                        break;
                    case 3:
                        Service.gI().getFlag(0, -1);
                        InfoDlg.showWait();
                        break;
                    case 4:
                        if (Char.myCharz().statusMe == 14)
                        {
                            GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                        }
                        else
                        {
                            Service.gI().openUIZone();
                        }
                        break;
                    case 5:
                        GameCanvas.endDlg();
                        if (Char.myCharz().checkLuong() < 5)
                        {
                            GameCanvas.startOKDlg(mResources.not_enough_luong_world_channel);
                            break;
                        }
                        if (chatTField == null)
                        {
                            chatTField = new ChatTextField();
                            chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                            chatTField.initChatTextField();
                            chatTField.parentScreen = GameCanvas.panel;
                        }
                        chatTField.strChat = mResources.world_channel_5_luong;
                        chatTField.tfChat.name = mResources.CHAT;
                        chatTField.to = string.Empty;
                        chatTField.isShow = true;
                        chatTField.tfChat.isFocus = true;
                        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                        if (Main.isWindowsPhone)
                        {
                            chatTField.tfChat.strInfo = chatTField.strChat;
                        }
                        if (!Main.isPC)
                        {
                            chatTField.startChat2(this, string.Empty);
                        }
                        else if (GameCanvas.isTouch)
                        {
                            chatTField.tfChat.doChangeToTextBox();
                        }
                        break;
                    case 6:
                        setTypeAccount();
                        break;
                    case 7:
                        setTypeOption();
                        break;
                    case 8:
                        GameCanvas.loginScr.backToRegister();
                        break;
                    case 9:
                        if (GameCanvas.loginScr.isLogin2)
                        {
                            SoundMn.gI().backToRegister();
                        }
                        break;
                }
                return;
            }
            switch (selected)
            {
                case 0:
                    hide();
                    doRada();
                    break;
                case 1:
                    Service.gI().openMenu(54);
                    break;
                case 2:
                    setTypeGameInfo();
                    break;
                case 3:
                    doFirePet();
                    break;
                case 4:
                    Service.gI().getFlag(0, -1);
                    InfoDlg.showWait();
                    break;
                case 5:
                    if (Char.myCharz().statusMe == 14)
                    {
                        GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                    }
                    else
                    {
                        Service.gI().openUIZone();
                    }
                    break;
                case 6:
                    GameCanvas.endDlg();
                    if (Char.myCharz().checkLuong() < 5)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_luong_world_channel);
                        break;
                    }
                    if (chatTField == null)
                    {
                        chatTField = new ChatTextField();
                        chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                        chatTField.initChatTextField();
                        chatTField.parentScreen = GameCanvas.panel;
                    }
                    chatTField.strChat = mResources.world_channel_5_luong;
                    chatTField.tfChat.name = mResources.CHAT;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.isFocus = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                    else if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    break;
                case 7:
                    setTypeAccount();
                    break;
                case 8:
                    setTypeOption();
                    break;
                case 9:
                    GameCanvas.loginScr.backToRegister();
                    break;
                case 10:
                    if (GameCanvas.loginScr.isLogin2)
                    {
                        SoundMn.gI().backToRegister();
                    }
                    break;
            }
        }
    
        private void setTypeGameSubInfo()
        {
            string content = ((GameInfo)vGameInfo.elementAt(infoSelect)).content;
            contenInfo = mFont.tahoma_7_grey.splitFontArray(content, wScroll - 40);
            currentListLength = contenInfo.Length;
            ITEM_HEIGHT = 16;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
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
                cmy = (cmtoY = cmyLim);
            }
            type = 24;
            setType(0);
        }
    
        private void setTypeGameInfo()
        {
            currentListLength = vGameInfo.size();
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
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
                cmy = (cmtoY = cmyLim);
            }
            type = 23;
            setType(0);
        }
    
        private void doFirePet()
        {
            InfoDlg.showWait();
            Service.gI().petInfo();
            if (GameCanvas.w > 2 * Panel.WIDTH_PANEL)
            {
                Panel panel2 = EnsurePanel2();
                panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                panel2.setTypeBodyOnly();
                panel2.show();
                GameCanvas.panel.setTypePetMain();
                GameCanvas.panel.show();
            }
            else
            {
                GameCanvas.panel.tabName[21] = mResources.petMainTab;
                GameCanvas.panel.setTypePetMain();
                GameCanvas.panel.show();
            }
            timeShow = 20;
        }
    }
}

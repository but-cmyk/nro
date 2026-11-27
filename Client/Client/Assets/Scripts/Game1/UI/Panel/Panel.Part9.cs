namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        private void updateCombineEff()
        {
            countUpdate--;
            if (countUpdate < 0)
            {
                countUpdate = 0;
            }
            countR--;
            if (countR < 0)
            {
                countR = 0;
            }
            if (countUpdate != 0)
            {
                return;
            }
            if (!isCompleteEffCombine)
            {
                if (time > 0)
                {
                    if (combineSuccess != -1)
                    {
                        if (typeCombine == 3)
                        {
                            if (GameCanvas.gameTick % 10 == 0)
                            {
                                Effect me = new Effect(21, xS - 10, yS + 25, 4, 1, 1);
                                EffecMn.addEff(me);
                                time--;
                            }
                        }
                        else
                        {
                            if (GameCanvas.gameTick % 2 == 0)
                            {
                                if (isSpeedCombine)
                                {
                                    if (speed < 40)
                                    {
                                        speed += 2;
                                    }
                                }
                                else if (speed > 10)
                                {
                                    speed -= 2;
                                }
                            }
                            if (countR == 0)
                            {
                                if (isSpeedCombine)
                                {
                                    if (rS > 0)
                                    {
                                        rS -= 5;
                                    }
                                    else if (GameCanvas.gameTick % 10 == 0)
                                    {
                                        isSpeedCombine = false;
                                        time--;
                                        countR = 5;
                                        countWait = 10;
                                    }
                                }
                                else if (rS < 90)
                                {
                                    rS += 5;
                                }
                                else if (GameCanvas.gameTick % 10 == 0)
                                {
                                    isSpeedCombine = true;
                                    countR = 10;
                                }
                            }
                            angleS = angleO;
                            angleS -= speed;
                            if (angleS >= 360)
                            {
                                angleS -= 360;
                            }
                            if (angleS < 0)
                            {
                                angleS = 360 + angleS;
                            }
                            angleO = angleS;
                            setDotStar();
                        }
                    }
                }
                else if (GameCanvas.gameTick % 20 == 0)
                {
                    isCompleteEffCombine = true;
                }
                if (GameCanvas.gameTick % 20 == 0)
                {
                    if (typeCombine != 3)
                    {
                        EffectPanel.addServerEffect(132, xS, yS, 2);
                    }
                    EffectPanel.addServerEffect(114, xS, yS + 20, 2);
                }
            }
            else
            {
                if (!isCompleteEffCombine)
                {
                    return;
                }
                if (combineSuccess == 1)
                {
                    if (countWait == 10)
                    {
                        Effect me2 = new Effect(22, xS - 3, yS + 25, 4, 1, 1);
                        EffecMn.addEff(me2);
                    }
                    countWait--;
                    if (countWait < 0)
                    {
                        countWait = 0;
                    }
                    if (rS < 300)
                    {
                        rS = Res.abs(rS + 10);
                        if (rS == 20)
                        {
                            addTextCombineNPC(idNPC, mResources.combineFail);
                        }
                    }
                    else if (GameCanvas.gameTick % 20 == 0)
                    {
                        if (GameCanvas.w > 2 * WIDTH_PANEL)
                        {
                            Panel panel2 = EnsurePanel2();
                            panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                            panel2.setTypeBodyOnly();
                            panel2.show();
                        }
                        combineSuccess = -1;
                        isDoneCombine = true;
                        if (typeCombine == 4)
                        {
                            GameCanvas.panel.hideNow();
                        }
                    }
                    setDotStar();
                }
                else
                {
                    if (combineSuccess != 0)
                    {
                        return;
                    }
                    if (countWait == 10)
                    {
                        if (typeCombine == 2)
                        {
                            Effect me3 = new Effect(20, xS - 3, yS + 15, 4, 2, 1);
                            EffecMn.addEff(me3);
                        }
                        else
                        {
                            Effect me4 = new Effect(21, xS - 10, yS + 25, 4, 1, 1);
                            EffecMn.addEff(me4);
                        }
                        addTextCombineNPC(idNPC, mResources.combineSuccess);
                        isPaintCombine = false;
                    }
                    if (isPaintCombine)
                    {
                        return;
                    }
                    countWait--;
                    if (countWait < -50)
                    {
                        countWait = -50;
                        if (typeCombine < 3 && GameCanvas.w > 2 * WIDTH_PANEL)
                        {
                            Panel panel2 = EnsurePanel2();
                            panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                            panel2.setTypeBodyOnly();
                            panel2.show();
                        }
                        combineSuccess = -1;
                        isDoneCombine = true;
                        if (typeCombine == 4)
                        {
                            GameCanvas.panel.hideNow();
                        }
                    }
                }
            }
        }
    
        public void paintCombineEff(mGraphics g)
        {
            GameScr.gI().paintBlackSky(g);
            paintCombineNPC(g);
            if (GameCanvas.gameTick % 4 == 0)
            {
                g.drawImage(ItemMap.imageFlare, xS, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
            }
            if (typeCombine == 0)
            {
                for (int i = 0; i < yArgS.Length; i++)
                {
                    SmallImage.drawSmallImage(g, iconID1, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    if (isPaintCombine)
                    {
                        SmallImage.drawSmallImage(g, iconID2, xDotS[i], yDotS[i], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    }
                }
            }
            else if (typeCombine == 1)
            {
                if (!isPaintCombine)
                {
                    SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    return;
                }
                for (int j = 0; j < yArgS.Length; j++)
                {
                    SmallImage.drawSmallImage(g, iconID1, xDotS[0], yDotS[0], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    SmallImage.drawSmallImage(g, iconID2, xDotS[1], yDotS[1], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            else if (typeCombine == 2)
            {
                if (!isPaintCombine)
                {
                    SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    return;
                }
                for (int k = 0; k < yArgS.Length; k++)
                {
                    SmallImage.drawSmallImage(g, iconID1, xDotS[k], yDotS[k], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            else if (typeCombine == 3)
            {
                if (!isPaintCombine)
                {
                    SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
                else
                {
                    SmallImage.drawSmallImage(g, iconID1, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            else
            {
                if (typeCombine != 4)
                {
                    return;
                }
                if (!isPaintCombine)
                {
                    if (iconID3 != -1)
                    {
                        SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    }
                }
                else
                {
                    for (int l = 0; l < iconID.Length; l++)
                    {
                        SmallImage.drawSmallImage(g, iconID[l], xDotS[l], yDotS[l], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    }
                }
            }
        }
    
        private void setDotStar()
        {
            for (int i = 0; i < yArgS.Length; i++)
            {
                if (angleS >= 360)
                {
                    angleS -= 360;
                }
                if (angleS < 0)
                {
                    angleS = 360 + angleS;
                }
                yArgS[i] = Res.abs(rS * Res.sin(angleS) / 1024);
                xArgS[i] = Res.abs(rS * Res.cos(angleS) / 1024);
                if (angleS < 90)
                {
                    xDotS[i] = xS + xArgS[i];
                    yDotS[i] = yS - yArgS[i];
                }
                else if (angleS >= 90 && angleS < 180)
                {
                    xDotS[i] = xS - xArgS[i];
                    yDotS[i] = yS - yArgS[i];
                }
                else if (angleS >= 180 && angleS < 270)
                {
                    xDotS[i] = xS - xArgS[i];
                    yDotS[i] = yS + yArgS[i];
                }
                else
                {
                    xDotS[i] = xS + xArgS[i];
                    yDotS[i] = yS + yArgS[i];
                }
                angleS -= iAngleS;
            }
        }
    
        public void paintCombineNPC(mGraphics g)
        {
            g.translate(-GameScr.cmx, -GameScr.cmy);
            if (typeCombine < 3)
            {
                for (int i = 0; i < GameScr.vNpc.size(); i++)
                {
                    Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                    if (npc.template.npcTemplateId == idNPC)
                    {
                        npc.paint(g);
                        if (npc.chatInfo != null)
                        {
                            npc.chatInfo.paint(g, npc.cx, npc.cy - npc.ch - GameCanvas.transY, npc.cdir);
                        }
                    }
                }
            }
            GameCanvas.resetTrans(g);
            if (GameCanvas.gameTick % 4 == 0)
            {
                g.drawImage(ItemMap.imageFlare, xS - 5, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
                g.drawImage(ItemMap.imageFlare, xS + 5, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
                g.drawImage(ItemMap.imageFlare, xS, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
            }
            for (int j = 0; j < Effect2.vEffect3.size(); j++)
            {
                Effect2 effect = (Effect2)Effect2.vEffect3.elementAt(j);
                effect.paint(g);
            }
        }
    
        public void addTextCombineNPC(int idNPC, string text)
        {
            if (typeCombine >= 3)
            {
                return;
            }
            for (int i = 0; i < GameScr.vNpc.size(); i++)
            {
                Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                if (npc.template.npcTemplateId == idNPC)
                {
                    npc.addInfo(text);
                }
            }
        }
    
        public void setTypeAutoSetting()
        {
            type = 28;
            setType(0);
            if (currentTabIndex == 0)
            {
                AutoSettingManager.getInstance().setTabAutoSetting(this);
            }
            else if (currentTabIndex == 1)
            {
                AutoSettingManager.getInstance().setTabMobTanSat(this);
            }
            cmx = (cmtoX = 0);
        }

        private void updateKeyAutoSetting()
        {
            updateKeyOption();
            if (currentTabIndex == 0)
            {
                if (selected >= 0 && selected < AutoSettingManager.getInstance().items.Count && AutoSettingManager.getInstance().items[selected].typeControl == 1)
                {
                    if (GameKeys.IsLeftPressed())
                    {
                        GameKeys.ClearLeft();
                        AutoSettingManager.getInstance().doKeyStep(selected, -1);
                    }
                    if (GameKeys.IsRightPressed())
                    {
                        GameKeys.ClearRight();
                        AutoSettingManager.getInstance().doKeyStep(selected, 1);
                    }
                }
            }
            else if (currentTabIndex == 1)
            {
                if (GameCanvas.gameTick % 30 == 0)
                {
                    int curMobSize = (GameScr.vMob != null) ? GameScr.vMob.size() : 0;
                    if (curMobSize != AutoSettingManager.getInstance().lastMobListSize || TileMap.mapID != AutoSettingManager.getInstance().lastMobMapId)
                    {
                        AutoSettingManager.getInstance().lastMobListSize = curMobSize;
                        AutoSettingManager.getInstance().lastMobMapId = TileMap.mapID;
                        AutoSettingManager.getInstance().UpdateCurrentMapMobs();
                        currentListLength = AutoSettingManager.getInstance().mobEntries.Count;
                        cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
                        if (cmyLim < 0)
                        {
                            cmyLim = 0;
                        }
                        if (selected >= currentListLength)
                        {
                            selected = currentListLength - 1;
                        }
                    }
                }
            }
        }

        public void setTypeOption()
        {
            type = TYPE_OPTION;
            setType(0);
            setTabOption();
            cmx = (cmtoX = 0);
        }
    
        private void setTabOption()
        {
            SoundMn.gI().getStrOption();
            currentListLength = strCauhinh.Length;
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
    
        private void paintOption(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strCauhinh.Length; i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    mFont.tahoma_7b_dark.drawString(g, strCauhinh[i], xScroll + 25, num + 6, mFont.LEFT);
                }
            }
            paintScrollArrow(g);
        }
    
        private void doFireOption()
        {
            if (selected < 0)
            {
                return;
            }
            switch (selected)
            {
                case 0:
                    SoundMn.gI().AuraToolOption();
                    break;
                case 1:
                    SoundMn.gI().AuraToolOption2();
                    break;
                case 2:
                    SoundMn.gI().soundToolOption();
                    break;
                case 3:
                    if (Main.isPC)
                    {
                        Main.setResolutionPC(960, 540, 1);
                        SoundMn.gI().getStrOption();
                        GameScr.info1.addInfo("Đã đổi: Màn hình nhỏ", 0);
                    }
                    else
                    {
                        SoundMn.gI().CaseSizeScr();
                    }
                    break;
                case 4:
                    if (Main.isPC)
                    {
                        Main.setResolutionPC(1280, 720, 0);
                        SoundMn.gI().getStrOption();
                        GameScr.info1.addInfo("Đã đổi: Màn hình mặc định", 0);
                    }
                    else
                    {
                        SoundMn.gI().CaseAnalog();
                    }
                    break;
                case 5:
                    if (Main.isPC)
                    {
                        Main.setResolutionPC(1920, 1080, 2);
                        SoundMn.gI().getStrOption();
                        GameScr.info1.addInfo("Đã đổi: Màn hình lớn", 0);
                    }
                    else
                    {
                        SoundMn.gI().CaseAnalog();
                    }
                    break;
            }
        }
    
        public void setTypeAccount()
        {
            type = 20;
            setType(0);
            setTabAccount();
            cmx = (cmtoX = 0);
        }
    
        private void setTabAccount()
        {
            if (Main.IphoneVersionApp)
            {
                strAccount = new string[4]
                {
                    mResources.inventory_Pass,
                    mResources.friend,
                    mResources.enemy,
                    mResources.msg
                };
                if (GameScr.canAutoPlay)
                {
                    strAccount = new string[5]
                    {
                        mResources.inventory_Pass,
                        mResources.friend,
                        mResources.enemy,
                        mResources.msg,
                        mResources.autoFunction
                    };
                }
            }
            else
            {
                strAccount = new string[5]
                {
                    mResources.inventory_Pass,
                    mResources.friend,
                    mResources.enemy,
                    mResources.msg,
                    mResources.charger
                };
                if (GameScr.canAutoPlay)
                {
                    strAccount = new string[6]
                    {
                        mResources.inventory_Pass,
                        mResources.friend,
                        mResources.enemy,
                        mResources.msg,
                        mResources.charger,
                        mResources.autoFunction
                    };
                }
                if ((mSystem.clientType == 2 || mSystem.clientType == 7) && mResources.language != 2)
                {
                    strAccount = new string[5]
                    {
                        mResources.inventory_Pass,
                        mResources.friend,
                        mResources.enemy,
                        mResources.msg,
                        mResources.charger
                    };
                    if (GameScr.canAutoPlay)
                    {
                        strAccount = new string[6]
                        {
                            mResources.inventory_Pass,
                            mResources.friend,
                            mResources.enemy,
                            mResources.msg,
                            mResources.charger,
                            mResources.autoFunction
                        };
                    }
                }
            }
            currentListLength = strAccount.Length;
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
    
        private void paintAccount(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strAccount.Length; i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    mFont.tahoma_7b_dark.drawString(g, strAccount[i], xScroll + wScroll / 2, num + 6, mFont.CENTER);
                }
            }
            paintScrollArrow(g);
        }
    
        private void doFireAccount()
        {
            if (selected < 0)
            {
                return;
            }
            switch (selected)
            {
                case 0:
                    GameCanvas.endDlg();
                    if (chatTField == null)
                    {
                        chatTField = new ChatTextField();
                        chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                        chatTField.initChatTextField();
                        chatTField.parentScreen = GameCanvas.panel;
                    }
                    chatTField.tfChat.setText(string.Empty);
                    chatTField.strChat = mResources.input_Inventory_Pass;
                    chatTField.tfChat.name = mResources.input_Inventory_Pass;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.isFocus = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                    if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    break;
                case 1:
                    Service.gI().friend(0, -1);
                    InfoDlg.showWait();
                    break;
                case 2:
                    Service.gI().enemy(0, -1);
                    InfoDlg.showWait();
                    break;
                case 3:
                    setTypeMessage();
                    if (chatTField == null)
                    {
                        chatTField = new ChatTextField();
                        chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                        chatTField.initChatTextField();
                        chatTField.parentScreen = GameCanvas.panel;
                    }
                    break;
                case 4:
                    if (mResources.language == 2)
                    {
                        string url = "http://dragonball.indonaga.com/coda/?username=" + GameCanvas.loginScr.tfUser.getText();
                        hideNow();
                        try
                        {
                            GameMidlet.instance.platformRequest(url);
                            break;
                        }
                        catch (Exception ex)
                        {
                            ex.StackTrace.ToString();
                            break;
                        }
                    }
                    hideNow();
                    if (Char.myCharz().taskMaint.taskId <= 10)
                    {
                        GameCanvas.startOKDlg(mResources.finishBomong);
                    }
                    else
                    {
                        MoneyCharge.gI().switchToMe();
                    }
                    break;
                case 5:
                    setTypeAuto();
                    break;
            }
        }
    
        private void updateKeyOption()
        {
            updateKeyScrollView();
        }
    
        public void setTypeSpeacialSkill()
        {
            type = 25;
            setType(0);
            setTabSpeacialSkill();
            currentTabIndex = 0;
        }
    
        private void setTabSpeacialSkill()
        {
            ITEM_HEIGHT = 24;
            currentListLength = Char.myCharz().infoSpeacialSkill[currentTabIndex].Length;
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
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        public bool isTypeShop()
        {
            if (type == 1)
            {
                return true;
            }
            return false;
        }
    
        private void doNotiRuby(int type)
        {
            try
            {
                currItem.buyRuby = int.Parse(chatTField.tfChat.getText());
            }
            catch (Exception)
            {
                GameCanvas.startOKDlg(mResources.input_money_wrong);
                chatTField.isShow = false;
                return;
            }
            Command cmdYes = new Command(mResources.YES, this, (type != 0) ? 11001 : 11000, null);
            Command cmdNo = new Command(mResources.NO, this, 11002, null);
            GameCanvas.startYesNoDlg(mResources.notiRuby, cmdYes, cmdNo);
        }
    
        public static void paintUpgradeEffect(int x, int y, int wItem, int hItem, int nline, int cl, mGraphics g)
        {
            try
            {
                int num = (wItem << 1) + (hItem << 1);
                int num2 = num / nline;
                nsize = sizeUpgradeEff.Length;
                if (nline > 4)
                {
                    nsize = 2;
                }
                for (int i = 0; i < nline; i++)
                {
                    for (int j = 0; j < nsize; j++)
                    {
                        int wSize = ((sizeUpgradeEff[j] <= 1) ? 1 : ((sizeUpgradeEff[j] >> 1) + 1));
                        int x2 = x + upgradeEffectX(num2 * i, GameCanvas.gameTick - j * 4, wItem, hItem, wSize);
                        int y2 = y + upgradeEffectY(num2 * i, GameCanvas.gameTick - j * 4, wItem, hItem, wSize);
                        g.setColor(colorUpgradeEffect[cl][j]);
                        g.fillRect(x2, y2, sizeUpgradeEff[j], sizeUpgradeEff[j]);
                    }
                }
            }
            catch (Exception ex)
            {
                logPanelPaintErrorThrottled("paintUpgradeEffect", ex);
            }
        }
    
        private static int upgradeEffectX(int dk, int tick, int wItem, int hitem, int wSize)
        {
            int num = (tick + dk) % ((wItem << 1) + (hitem << 1));
            if (0 <= num && num < wItem)
            {
                return num % wItem;
            }
            if (wItem <= num && num < wItem + hitem)
            {
                return wItem - wSize;
            }
            if (wItem + hitem <= num && num < (wItem << 1) + hitem)
            {
                return wItem - (num - hitem) % wItem - wSize;
            }
            return 0;
        }
    
        private static int upgradeEffectY(int dk, int tick, int wItem, int hitem, int wSize)
        {
            int num = (tick + dk) % ((wItem << 1) + (hitem << 1));
            if (0 <= num && num < wItem)
            {
                return 0;
            }
            if (wItem <= num && num < wItem + hitem)
            {
                return num % wItem;
            }
            if (wItem + hitem <= num && num < (wItem << 1) + hitem)
            {
                return hitem - wSize;
            }
            return hitem - (num - (wItem << 1)) % hitem - wSize;
        }
    
        public static int GetColor_ItemBg(int id)
        {
            switch (id)
            {
                case 4:
                    return 1269146;
                case 1:
                    return 2786816;
                case 5:
                    return 13279744;
                case 3:
                    return 12537346;
                case 2:
                    return 7078041;
                case 6:
                    return 11599872;
                default:
                    return -1;
            }
        }
    
        public static sbyte GetColor_Item_Upgrade(int lv)
        {
            if (lv < 0)
            {
                return 0;
            }
            switch (lv)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return 0;
                case 9:
                    return 4;
                case 10:
                    return 1;
                case 11:
                    return 5;
                case 12:
                    return 3;
                case 13:
                    return 2;
                default:
                    return 6;
            }
        }
    
        public static mFont GetFont(int color)
        {
            mFont result = mFont.tahoma_7;
            switch (color)
            {
                case -1:
                    result = mFont.tahoma_7;
                    break;
                case 0:
                    result = mFont.tahoma_7b_dark;
                    break;
                case 1:
                    result = mFont.tahoma_7b_green;
                    break;
                case 2:
                    result = mFont.tahoma_7b_blue;
                    break;
                case 3:
                    result = mFont.tahoma_7_red;
                    break;
                case 4:
                    result = mFont.tahoma_7_green;
                    break;
                case 5:
                    result = mFont.tahoma_7_blue;
                    break;
                case 7:
                    result = mFont.tahoma_7b_red;
                    break;
                case 8:
                    result = mFont.tahoma_7b_yellow;
                    break;
            }
            return result;
        }
    
        public void paintOptItem(mGraphics g, int idOpt, int param, int x, int y, int w, int h)
        {
            switch (idOpt)
            {
                case 34:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x, y + h - imgo_0.getHeight());
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_0.png");
                    }
                    if (imgo_1 != null)
                    {
                        g.drawImage(imgo_1, x, y + h - imgo_1.getHeight());
                    }
                    else
                    {
                        imgo_1 = mSystem.loadImage("/mainImage/o_1.png");
                    }
                    break;
                case 35:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x, y + h - imgo_0.getHeight());
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_0.png");
                    }
                    if (imgo_2 != null)
                    {
                        g.drawImage(imgo_2, x, y + h - imgo_2.getHeight());
                    }
                    else
                    {
                        imgo_2 = mSystem.loadImage("/mainImage/o_2.png");
                    }
                    break;
                case 36:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x, y + h - imgo_0.getHeight());
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_0.png");
                    }
                    if (imgo_3 != null)
                    {
                        g.drawImage(imgo_3, x, y + h - imgo_3.getHeight());
                    }
                    else
                    {
                        imgo_3 = mSystem.loadImage("/mainImage/o_3.png");
                    }
                    break;
            }
        }
    
        public void paintOptSlotItem(mGraphics g, int idOpt, int param, int x, int y, int w, int h)
        {
            if (idOpt == 102 && param > ChatPopup.numSlot)
            {
                sbyte color_Item_Upgrade = GetColor_Item_Upgrade(param);
                int nline = param - ChatPopup.numSlot;
                paintUpgradeEffect(x, y, w, h, nline, color_Item_Upgrade, g);
            }
        }
    
        public static mFont setTextColor(int id, int type)
        {
            if (type == 0)
            {
                switch (id)
                {
                    case 0:
                        return mFont.bigNumber_While;
                    case 1:
                        return mFont.bigNumber_green;
                    case 3:
                        return mFont.bigNumber_orange;
                    case 4:
                        return mFont.bigNumber_blue;
                    case 5:
                        return mFont.bigNumber_yellow;
                    case 6:
                        return mFont.bigNumber_red;
                    default:
                        return mFont.bigNumber_While;
                }
            }
            switch (id)
            {
                case 0:
                    return mFont.tahoma_7b_white;
                case 1:
                    return mFont.tahoma_7b_green;
                case 3:
                    return mFont.tahoma_7b_yellowSmall2;
                case 4:
                    return mFont.tahoma_7b_blue;
                case 5:
                    return mFont.tahoma_7b_yellow;
                case 6:
                    return mFont.tahoma_7b_red;
                case 7:
                    return mFont.tahoma_7b_dark;
                default:
                    return mFont.tahoma_7b_white;
            }
        }
    
        private bool GetInventorySelect_isbody(int select, int subSelect, Item[] arrItem)
        {
            int num = select - 1 + subSelect * 20;
            return subSelect == 0 && num < arrItem.Length;
        }
    
        private int GetInventorySelect_body(int select, int subSelect)
        {
            return select - 1 + subSelect * 20;
        }
    
        private int GetInventorySelect_bag(int select, int subSelect, Item[] arrItem)
        {
            int num = select - 1 + subSelect * 20;
            return num - arrItem.Length;
        }
    
        private bool isTabInven()
        {
            if ((type == TYPE_MAIN && currentTabIndex == PanelConstants.MainTabs.INVENTORY) || 
                (type == TYPE_BODY && currentTabIndex == PanelConstants.BodyTabs.INVENTORY))
            {
                return true;
            }
            return false;
        }
    }
}

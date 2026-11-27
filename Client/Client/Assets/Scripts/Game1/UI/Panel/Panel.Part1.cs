namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        public int getYMap()
        {
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (TileMap.mapID == mapId[TileMap.planetID][i])
                {
                    return mapY[TileMap.planetID][i];
                }
            }
            return -1;
        }
    
        public int getXMapTask()
        {
            if (Char.myCharz().taskMaint == null)
            {
                return -1;
            }
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (GameScr.mapTasks[Char.myCharz().taskMaint.index] == mapId[TileMap.planetID][i])
                {
                    return mapX[TileMap.planetID][i];
                }
            }
            return -1;
        }
    
        public int getYMapTask()
        {
            if (Char.myCharz().taskMaint == null)
            {
                return -1;
            }
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (GameScr.mapTasks[Char.myCharz().taskMaint.index] == mapId[TileMap.planetID][i])
                {
                    return mapY[TileMap.planetID][i];
                }
            }
            return -1;
        }
    
        private void setType(int position)
        {
            typeShop = -1;
            W = WIDTH_PANEL;
            H = GameCanvas.h;
            X = 0;
            Y = 0;
            ITEM_HEIGHT = 24;
            this.position = position;
            switch (position)
            {
                case 0:
                    xScroll = 2;
                    yScroll = 80;
                    wScroll = W - 4;
                    hScroll = H - 96;
                    cmx = wScroll;
                    cmtoX = 0;
                    X = 0;
                    break;
                case 1:
                    wScroll = W - 4;
                    xScroll = GameCanvas.w - wScroll;
                    yScroll = 80;
                    hScroll = H - 96;
                    X = xScroll - 2;
                    cmx = -(GameCanvas.w + W);
                    cmtoX = GameCanvas.w - W;
                    break;
            }
            TAB_W = W / 5 - 1;
            currentTabIndex = 0;
            currentTabName = tabName[type];
            if (currentTabName.Length < 5)
            {
                TAB_W += 5;
            }
            startTabPos = xScroll + wScroll / 2 - currentTabName.Length * TAB_W / 2;
            lastSelect = new int[currentTabName.Length];
            cmyLast = new int[currentTabName.Length];
            for (int i = 0; i < currentTabName.Length; i++)
            {
                lastSelect[i] = (GameCanvas.isTouch ? (-1) : 0);
            }
            if (lastTabIndex[type] != -1)
            {
                currentTabIndex = lastTabIndex[type];
            }
            if (currentTabIndex < 0)
            {
                currentTabIndex = 0;
            }
            if (currentTabIndex > currentTabName.Length - 1)
            {
                currentTabIndex = currentTabName.Length - 1;
            }
            scroll = null;
        }
    
        public void setTypeMapTrans()
        {
            type = 14;
            setType(0);
            setTabMapTrans();
            cmx = (cmtoX = 0);
        }
    
        public void setTypeInfomatioin()
        {
            type = 6;
            cmx = wScroll;
            cmtoX = 0;
        }
    
        public void setTypeMap()
        {
            if (!GameScr.gI().isMapFize() && isPaintMap)
            {
                if (Hint.isOnTask(2, 0))
                {
                    Hint.isViewMap = true;
                    GameScr.info1.addInfo(mResources.go_to_quest, 0);
                }
                if (Hint.isOnTask(3, 0))
                {
                    Hint.isViewPotential = true;
                }
                type = 4;
                currentTabName = tabName[type];
                startTabPos = xScroll + wScroll / 2 - currentTabName.Length * TAB_W / 2;
                cmx = (cmtoX = 0);
                setTabMap();
            }
        }
    
        public void setTypeArchivement()
        {
            currentListLength = Char.myCharz().arrArchive.Length;
            setType(0);
            type = 9;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            cmy = (cmtoY = cmyLast[currentTabIndex]);
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
    
        public void setTypeKiGuiOnly()
        {
            type = 17;
            setType(1);
            setTabKiGui();
            typeShop = 2;
            currentTabIndex = 0;
        }
    
        public void setTabChatManager()
        {
            currentListLength = chats.Count;
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
        }
    
        public void setTabChatPlayer()
        {
        }
    
        public void setTypeChatPlayer()
        {
        }
    
        public void setTabKiGui()
        {
            ITEM_HEIGHT = 24;
            currentListLength = Char.myCharz().arrItemShop[4].Length;
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
    
        public void setTypeBodyOnly()
        {
            type = 7;
            setType(1);
            setTabInventory(true);
            currentTabIndex = 0;
        }
    
        public void addChatMessage(InfoItem info)
        {
            logChat.insertElementAt(info, 0);
            if (logChat.size() > 20)
            {
                logChat.removeElementAt(logChat.size() - 1);
            }
        }
    
        private bool IsNewMessage(string name)
        {
            return false;
        }
    
        public bool IsHaveNewMessage()
        {
            return false;
        }
    
        private void ClearNewMessage(string name)
        {
        }
    
        public void addPlayerMenu(Command pm)
        {
            vPlayerMenu.addElement(pm);
        }
    
        public void setTabPlayerMenu()
        {
            ITEM_HEIGHT = 24;
            currentListLength = vPlayerMenu.size();
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
    
        public void setTypeFlag()
        {
            type = 18;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabFlag();
        }
    
        public void setTabFlag()
        {
            currentListLength = vFlag.size();
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
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTypePlayerMenu(Char c)
        {
            type = 10;
            setType(0);
            setTabPlayerMenu();
            charMenu = c;
        }
    
        public void setTypeFriend()
        {
            type = 11;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabFriend();
        }
    
        public void setTypeEnemy()
        {
            type = 16;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabEnemy();
        }
    
        public void setTypeTop(sbyte t)
        {
            type = 15;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabTop();
            isThachDau = ((t != 0) ? true : false);
        }
    
        public void setTabTop()
        {
            currentListLength = vTop.size();
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
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTabFriend()
        {
            currentListLength = vFriend.size();
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
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTabEnemy()
        {
            currentListLength = vEnemy.size();
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
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTypeMessage()
        {
            type = 8;
            setType(0);
            setTabMessage();
            currentTabIndex = 0;
        }
    
        public void setTypeLockInventory()
        {
            type = 8;
            setType(0);
            setTabMessage();
            currentTabIndex = 0;
        }
    
        public void setTypeShop(int typeShop)
        {
            type = 1;
            setType(0);
            setTabShop();
            currentTabIndex = 0;
            this.typeShop = typeShop;
        }
    
        public void setTypeBox()
        {
            type = 2;
            if (GameCanvas.w > 2 * WIDTH_PANEL)
            {
                boxTabName = new string[1][] { mResources.chestt };
            }
            else
            {
                boxTabName = new string[2][]
                {
                    mResources.chestt,
                    mResources.inventory
                };
            }
            tabName[2] = boxTabName;
            setType(0);
            if (currentTabIndex == 0)
            {
                setTabBox();
            }
            if (currentTabIndex == 1)
            {
                setTabInventory(true);
            }
            if (GameCanvas.w > 2 * WIDTH_PANEL)
            {
                Panel panel2 = EnsurePanel2();
                panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                panel2.setTypeBodyOnly();
                panel2.show();
            }
        }
    
        public void setTypeCombine()
        {
            type = 12;
            if (GameCanvas.w > 2 * WIDTH_PANEL)
            {
                boxCombine = new string[1][] { mResources.combine };
            }
            else
            {
                boxCombine = new string[2][]
                {
                    mResources.combine,
                    mResources.inventory
                };
            }
            tabName[type] = boxCombine;
            setType(0);
            if (currentTabIndex == 0)
            {
                setTabCombine();
            }
            if (currentTabIndex == 1)
            {
                setTabInventory(true);
            }
            if (GameCanvas.w > 2 * WIDTH_PANEL)
            {
                Panel panel2 = EnsurePanel2();
                panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                panel2.setTypeBodyOnly();
                panel2.show();
            }
            combineSuccess = -1;
            isDoneCombine = true;
        }
    
    
        public void setTypeAuto()
        {
            type = 22;
            setType(0);
            setTabAuto();
            cmx = (cmtoX = 0);
        }
    
        private void setTabAuto()
        {
            currentListLength = strAuto.Length;
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
    
        public void setTypePetMain()
        {
            type = 21;
            if (GameCanvas.panel2 != null)
            {
                boxPet = mResources.petMainTab2;
            }
            else
            {
                boxPet = mResources.petMainTab;
            }
            tabName[21] = boxPet;
            if (Char.myCharz().cgender == 1)
            {
                strStatus = new string[6]
                {
                    mResources.follow,
                    mResources.defend,
                    mResources.attack,
                    mResources.gohome,
                    mResources.fusion,
                    mResources.fusionForever
                };
            }
            else
            {
                strStatus = new string[5]
                {
                    mResources.follow,
                    mResources.defend,
                    mResources.attack,
                    mResources.gohome,
                    mResources.fusion
                };
            }
            setType(2);
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
        }
    
        public void setTypeMain()
        {
            type = 0;
            setType(0);
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
                if (mainTabName.Length == 4)
                {
                    setTabTool();
                }
                else
                {
                    setTabClans();
                }
            }
            if (currentTabIndex == 4)
            {
                setTabTool();
            }
        }
    
        public void setTypeZone()
        {
            type = 3;
            setType(0);
            setTabZone();
            cmx = (cmtoX = 0);
        }
    
        public void addItemDetail(Item item)
        {
            if (item == null || item.template == null)
            {
                return;
            }
            try
            {
                cp = new ChatPopup();
                string empty = string.Empty;
                string text = string.Empty;
                if (item.template.gender != Char.myCharz().cgender)
                {
                    if (item.template.gender == 0)
                    {
                        text = text + "|7|1|" + mResources.from_earth + "\n";
                    }
                    else if (item.template.gender == 1)
                    {
                        text = text + "|7|1|" + mResources.from_namec + "\n";
                    }
                    else if (item.template.gender == 2)
                    {
                        text = text + "|7|1|" + mResources.from_sayda + "\n";
                    }
                }
                string text2 = string.Empty;
                if (item.itemOption != null)
                {
                    for (int i = 0; i < item.itemOption.Length; i++)
                    {
                        if (item.itemOption[i] == null || item.itemOption[i].optionTemplate == null) continue;
                        if (item.itemOption[i].optionTemplate.id == 72)
                        {
                            text2 = " [+" + item.itemOption[i].param + "]";
                        }
                    }
                }
                bool flag = false;
                if (item.itemOption != null)
                {
                    for (int j = 0; j < item.itemOption.Length; j++)
                    {
                        if (item.itemOption[j] == null || item.itemOption[j].optionTemplate == null) continue;
                        if (item.itemOption[j].optionTemplate.id == 41)
                        {
                            flag = true;
                            if (item.itemOption[j].param == 1)
                            {
                                text = text + "|0|1|" + item.template.name + text2;
                            }
                            if (item.itemOption[j].param == 2)
                            {
                                text = text + "|2|1|" + item.template.name + text2;
                            }
                            if (item.itemOption[j].param == 3)
                            {
                                text = text + "|8|1|" + item.template.name + text2;
                            }
                            if (item.itemOption[j].param == 4)
                            {
                                text = text + "|7|1|" + item.template.name + text2;
                            }
                        }
                    }
                }
                if (!flag)
                {
                    text = text + "|0|1|" + item.template.name + text2;
                }
                if (item.itemOption != null)
                {
                    for (int k = 0; k < item.itemOption.Length; k++)
                    {
                        if (item.itemOption[k] == null || item.itemOption[k].optionTemplate == null) continue;
                        if (item.itemOption[k].optionTemplate.name != null && item.itemOption[k].optionTemplate.name.StartsWith("$") ? true : false)
                        {
                            empty = item.itemOption[k].getOptiongColor();
                            if (item.itemOption[k].param == 1)
                            {
                                text = text + "\n|1|1|" + empty;
                            }
                            if (item.itemOption[k].param == 0)
                            {
                                text = text + "\n|0|1|" + empty;
                            }
                        }
                        else
                        {
                            empty = item.itemOption[k].getOptionString();
                            if (!empty.Equals(string.Empty))
                            {
                                if (item.itemOption[k].optionTemplate.id == 72)
                                {
                                    continue;
                                }
                                if (item.itemOption[k].optionTemplate.id == 102)
                                {
                                    cp.starSlot = (sbyte)item.itemOption[k].param;
                                }
                                else if (item.itemOption[k].optionTemplate.id == 107)
                                {
                                    cp.maxStarSlot = (sbyte)item.itemOption[k].param;
                                }
                                else
                                {
                                    text = text + "\n|1|1|" + empty;
                                }
                            }
                        }
                        if (item.itemOption[k].optionTemplate.id != 228)
                        {
                            continue;
                        }
                        Res.outz("========>>> " + item.itemOption[k].optionTemplate.name + "_" + item.itemOption[k].param);
                        if (item.itemOption[k].param > 7)
                        {
                            for (int l = 0; l < item.itemOption[k].param - 7; l++)
                            {
                                if (cp.starCuongHoa != null && l + 7 < cp.starCuongHoa.Length)
                                {
                                    cp.starCuongHoa[l + 7] = true;
                                }
                            }
                        }
                    }
                }
                if (item.template.strRequire > 1)
                {
                    long currentPower = (type == 21 && Char.myPetz() != null) ? Char.myPetz().cPower : Char.myCharz().cPower;
                    string powLabel = (type == 21) ? "Sức mạnh đệ tử" : mResources.your_pow;
                    string text3 = mResources.pow_request + ": " + Res.formatNumber(item.template.strRequire);
                    if (item.template.strRequire > currentPower)
                    {
                        text = text + "\n|3|1|" + text3;
                        string text4 = text;
                        text = text4 + "\n|3|1|" + powLabel + ": " + Res.formatNumber(currentPower);
                    }
                    else
                    {
                        text = text + "\n|6|1|" + text3;
                    }
                }
                item.compare = getCompare(item);
                if (!string.IsNullOrEmpty(item.template.description))
                {
                    text += "\n--";
                    text = text + "\n|6|" + item.template.description;
                }
                if (!string.IsNullOrEmpty(item.reason))
                {
                    if (string.IsNullOrEmpty(item.template.description))
                    {
                        text += "\n--";
                    }
                    text = text + "\n|2|" + item.reason;
                }
                if (cp.maxStarSlot > 0)
                {
                    text += "\n\n";
                }
                popUpDetailInit(cp, text);
                idIcon = item.template.iconID;
                partID = null;
                charInfo = null;
            }
            catch (Exception ex)
            {
                Res.outz("ex " + ex.StackTrace);
            }
        }
    
        public void popUpDetailInit(ChatPopup cp, string chat)
        {
            cp.isClip = false;
            cp.sayWidth = 180;
            cp.cx = 3 + X - ((X != 0) ? (Res.abs(cp.sayWidth - W) + 8) : 0);
            cp.says = mFont.tahoma_7_red.splitFontArray(chat, cp.sayWidth - 10);
            cp.delay = 10000000;
            cp.c = null;
            cp.sayRun = 7;
            cp.ch = 15 - cp.sayRun + cp.says.Length * 12 + 10;
            if (cp.ch > GameCanvas.h - 80)
            {
                cp.ch = GameCanvas.h - 80;
                cp.lim = cp.says.Length * 12 - cp.ch + 17;
                if (cp.lim < 0)
                {
                    cp.lim = 0;
                }
                ChatPopup.cmyText = 0;
                cp.isClip = true;
            }
            cp.cy = GameCanvas.menu.menuY - cp.ch;
            while (cp.cy < 10)
            {
                cp.cy++;
                GameCanvas.menu.menuY++;
            }
            cp.mH = 0;
            cp.strY = 10;
        }
    
        public void popUpDetailInitArray(ChatPopup cp, string[] chat)
        {
            cp.sayWidth = 160;
            cp.cx = 3 + X;
            cp.says = chat;
            cp.delay = 10000000;
            cp.c = null;
            cp.sayRun = 7;
            cp.ch = 15 - cp.sayRun + cp.says.Length * 12 + 10;
            cp.cy = GameCanvas.menu.menuY - cp.ch;
            cp.mH = 0;
            cp.strY = 10;
        }
    
        public void addMessageDetail(ClanMessage cm)
        {
            cp = new ChatPopup();
            string text = "|0|" + cm.playerName;
            text = text + "\n|1|" + Member.getRole(cm.role);
            for (int i = 0; i < myMember.size(); i++)
            {
                Member member = (Member)myMember.elementAt(i);
                if (cm.playerId == member.ID)
                {
                    string text2 = text;
                    text = text2 + "\n|5|" + mResources.clan_capsuledonate + ": " + member.clanPoint;
                    text2 = text;
                    text = text2 + "\n|5|" + mResources.clan_capsuleself + ": " + member.curClanPoint;
                    text2 = text;
                    text = text2 + "\n|4|" + mResources.give_pea + ": " + member.donate + mResources.time;
                    text2 = text;
                    text = text2 + "\n|4|" + mResources.receive_pea + ": " + member.receive_donate + mResources.time;
                    partID = new int[3] { member.head, member.leg, member.body };
                    break;
                }
            }
            text += "\n--";
            for (int j = 0; j < cm.chat.Length; j++)
            {
                text = text + "\n" + cm.chat[j];
            }
            if (cm.type == 1)
            {
                string text2 = text;
                text = text2 + "\n|6|" + mResources.received + " " + cm.recieve + "/" + cm.maxCap;
            }
            popUpDetailInit(cp, text);
            charInfo = null;
        }
    
        public void addThachDauDetail(TopInfo t)
        {
            string text = "|0|1|" + t.name;
            text = text + "\n|1|Top " + t.rank;
            text = text + "\n|1|" + t.info;
            text = text + "\n|2|" + t.info2;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            partID = new int[3] { t.headID, t.leg, t.body };
            currItem = null;
            charInfo = null;
        }
    
        public void addClanMemberDetail(Member m)
        {
            string text = "|0|1|" + m.name;
            string text2 = "\n|2|1|";
            if (m.role == 0)
            {
                text2 = "\n|7|1|";
            }
            if (m.role == 1)
            {
                text2 = "\n|1|1|";
            }
            if (m.role == 2)
            {
                text2 = "\n|0|1|";
            }
            text = text + text2 + Member.getRole(m.role);
            string text3 = text;
            text = text3 + "\n|2|1|" + mResources.power + ": " + m.powerPoint;
            text += "\n--";
            text3 = text;
            text = text3 + "\n|5|" + mResources.clan_capsuledonate + ": " + m.clanPoint;
            text3 = text;
            text = text3 + "\n|5|" + mResources.clan_capsuleself + ": " + m.curClanPoint;
            text3 = text;
            text = text3 + "\n|4|" + mResources.give_pea + ": " + m.donate + mResources.time;
            text3 = text;
            text = text3 + "\n|4|" + mResources.receive_pea + ": " + m.receive_donate + mResources.time;
            text3 = text;
            text = text3 + "\n|6|" + mResources.join_date + ": " + m.joinTime;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            partID = new int[3] { m.head, m.leg, m.body };
            currItem = null;
            charInfo = null;
        }
    
        public void addClanDetail(Clan cl)
        {
            try
            {
                string text = "|0|" + cl.name;
                string[] array = mFont.tahoma_7_green.splitFontArray(cl.slogan, wScroll - 60);
                for (int i = 0; i < array.Length; i++)
                {
                    text = text + "\n|2|" + array[i];
                }
                text += "\n--";
                string text2 = text;
                text = text2 + "\n|7|" + mResources.clan_leader + ": " + cl.leaderName;
                text2 = text;
                text = text2 + "\n|1|" + mResources.power_point + ": " + cl.powerPoint;
                text2 = text;
                text = text2 + "\n|4|" + mResources.member + ": " + cl.currMember + "/" + cl.maxMember;
                text2 = text;
                text = text2 + "\n|4|" + mResources.level + ": " + cl.level;
                text2 = text;
                text = text2 + "\n|4|" + mResources.clan_birthday + ": " + NinjaUtil.getDate(cl.date);
                cp = new ChatPopup();
                popUpDetailInit(cp, text);
                idIcon = ClanImage.getClanImage((short)cl.imgID).idImage[0];
                currItem = null;
            }
            catch (Exception ex)
            {
                Res.outz("Throw  exception " + ex.StackTrace);
            }
        }
    
        public void addSkillDetail(SkillTemplate tp, Skill skill, Skill nextSkill)
        {
            string text = "|0|" + tp.name;
            for (int i = 0; i < tp.description.Length; i++)
            {
                text = text + "\n|4|" + tp.description[i];
            }
            text += "\n--";
            if (skill != null)
            {
                string text2 = text;
                text = text2 + "\n|2|" + mResources.cap_do + ": " + skill.point;
                text = text + "\n|5|" + NinjaUtil.replace(tp.damInfo, "#", skill.damage + string.Empty);
                text2 = text;
                text = text2 + "\n|5|" + mResources.KI_consume + skill.manaUse + ((tp.manaUseType != 1) ? string.Empty : "%");
                text2 = text;
                text = text2 + "\n|5|" + mResources.cooldown + ": " + skill.strTimeReplay() + "s";
                text += "\n--";
                if (skill.point == tp.maxPoint)
                {
                    text = text + "\n|0|" + mResources.max_level_reach;
                }
                else
                {
                    if (!skill.template.isSkillSpec())
                    {
                        text2 = text;
                        text = text2 + "\n|1|" + mResources.next_level_require + Res.formatNumber(nextSkill.powRequire) + " " + mResources.potential;
                    }
                    text = text + "\n|4|" + NinjaUtil.replace(tp.damInfo, "#", nextSkill.damage + string.Empty);
                }
            }
            else
            {
                text = text + "\n|2|" + mResources.not_learn;
                string text2 = text;
                text = text2 + "\n|1|" + mResources.learn_require + Res.formatNumber(nextSkill.powRequire) + " " + mResources.potential;
                text = text + "\n|4|" + NinjaUtil.replace(tp.damInfo, "#", nextSkill.damage + string.Empty);
                text2 = text;
                text = text2 + "\n|4|" + mResources.KI_consume + nextSkill.manaUse + ((tp.manaUseType != 1) ? string.Empty : "%");
                text2 = text;
                text = text2 + "\n|4|" + mResources.cooldown + ": " + nextSkill.strTimeReplay() + "s";
            }
            currItem = null;
            partID = null;
            charInfo = null;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            idIcon = 0;
        }
    
        public void show()
        {
            if (GameCanvas.isTouch)
            {
                cmdClose.x = 156;
                cmdClose.y = 3;
            }
            else
            {
                cmdClose.x = GameCanvas.w - 19;
                cmdClose.y = GameCanvas.h - 19;
            }
            cmdClose.isPlaySoundButton = false;
            ChatPopup.currChatPopup = null;
            ChatPopup.serverChatPopUp = null;
            InfoDlg.hide();
            timeShow = 20;
            isShow = true;
            isClose = false;
            SoundMn.gI().panelOpen();
            if (isTypeShop())
            {
                Char.myCharz().setPartOld();
            }
        }
    
        public void chatTFUpdateKey()
        {
            if (chatTField != null && chatTField.isShow)
            {
                if (chatTField.left != null && (GameCanvas.keyPressed[12] || mScreen.getCmdPointerLast(chatTField.left)) && chatTField.left != null)
                {
                    chatTField.left.performAction();
                }
                if (chatTField.right != null && (GameKeys.IsSoftRightPressed() || mScreen.getCmdPointerLast(chatTField.right)) && chatTField.right != null)
                {
                    chatTField.right.performAction();
                }
                if (chatTField.center != null && (GameKeys.IsFirePressed() || mScreen.getCmdPointerLast(chatTField.center)) && chatTField.center != null)
                {
                    chatTField.center.performAction();
                }
                if (chatTField.isShow && GameCanvas.keyAsciiPress != 0)
                {
                    chatTField.keyPressed(GameCanvas.keyAsciiPress);
                    GameCanvas.keyAsciiPress = 0;
                }
                GameCanvas.clearKeyHold();
                GameCanvas.clearKeyPressed();
            }
        }
    }
}

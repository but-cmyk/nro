namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        private void searchClan()
        {
            chatTField.strChat = mResources.input_clan_name;
            chatTField.tfChat.name = mResources.clan_name;
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
        }
    
        private void chatClan()
        {
            chatTField.strChat = mResources.chat_clan;
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
        }
    
        public void creatClan()
        {
            chatTField.strChat = mResources.input_clan_name_to_create;
            chatTField.tfChat.name = mResources.input_clan_name;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat2(this, string.Empty);
            }
        }
    
        public void putMoney()
        {
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = GameCanvas.panel;
            }
            chatTField.strChat = mResources.input_money_to_trade;
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
    
        public void putQuantily()
        {
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = GameCanvas.panel;
            }
            chatTField.strChat = mResources.input_quantity_to_trade;
            chatTField.tfChat.name = mResources.input_quantity;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
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
    
        public void chagenSlogan()
        {
            chatTField.strChat = mResources.input_clan_slogan;
            chatTField.tfChat.name = mResources.input_clan_slogan;
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
        }
    
        public void changeIcon()
        {
            if (tabIcon == null)
            {
                tabIcon = new TabClanIcon();
            }
            tabIcon.text = chatTField.tfChat.getText();
            tabIcon.show(false);
            chatTField.isShow = false;
        }
    
        private void addFriend(InfoItem info)
        {
            string text = "|0|1|" + info.charInfo.cName;
            text += "\n";
            text = ((!info.isOnline) ? (text + "|3|1|" + mResources.is_offline) : (text + "|4|1|" + mResources.is_online));
            text += "\n--";
            string text2 = text;
            text = text2 + "\n|5|" + mResources.power + ": " + info.s;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            charInfo = info.charInfo;
            currItem = null;
        }
    
        private void doFireEnemy()
        {
            if (selected >= 0 && vEnemy.size() != 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected;
                myVector.addElement(new Command(mResources.REVENGE, this, 10000, (InfoItem)vEnemy.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.DELETE, this, 10001, (InfoItem)vEnemy.elementAt(currInfoItem)));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addFriend((InfoItem)vEnemy.elementAt(selected));
            }
        }
    
        private void doFireFriend()
        {
            if (selected >= 0 && vFriend.size() != 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected;
                myVector.addElement(new Command(mResources.CHAT, this, 8001, (InfoItem)vFriend.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.DELETE, this, 8002, (InfoItem)vFriend.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.den, this, 8004, (InfoItem)vFriend.elementAt(currInfoItem)));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addFriend((InfoItem)vFriend.elementAt(selected));
            }
        }
    
        private void doFireChangeFlag()
        {
            if (selected >= 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected;
                myVector.addElement(new Command(mResources.change_flag, this, 10030, null));
                myVector.addElement(new Command(mResources.BACK, this, 10031, null));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            }
        }
    
        private void doFireLogMessage()
        {
            if (selected == 0)
            {
                isViewChatServer = !isViewChatServer;
                Rms.saveRMSInt("viewchat", isViewChatServer ? 1 : 0);
                if (GameCanvas.isTouch)
                {
                    selected = -1;
                }
            }
            else if (selected >= 0 && logChat.size() != 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected - 1;
                myVector.addElement(new Command(mResources.CHAT, this, 8001, (InfoItem)logChat.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.make_friend, this, 8003, (InfoItem)logChat.elementAt(currInfoItem)));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addLogMessage((InfoItem)logChat.elementAt(selected - 1));
            }
        }
    
        private void doFireClanOption()
        {
            try
            {
                partID = null;
                charInfo = null;
                Res.outz("cSelect= " + cSelected);
                if (selected < 0)
                {
                    cSelected = -1;
                    return;
                }
                if (Char.myCharz().clan == null)
                {
                    if (selected == 0)
                    {
                        if (cSelected == 0)
                        {
                            searchClan();
                        }
                        else if (cSelected == 1)
                        {
                            InfoDlg.showWait();
                            creatClan();
                            Service.gI().getClan(1, -1, null);
                        }
                    }
                    else if (selected != -1)
                    {
                        if (selected == 1)
                        {
                            if (isSearchClan)
                            {
                                Service.gI().searchClan(string.Empty);
                            }
                            else if (isViewMember && currClan != null)
                            {
                                GameCanvas.startYesNoDlg(mResources.do_u_want_join_clan + currClan.name, new Command(mResources.YES, this, 4000, currClan), new Command(mResources.NO, this, 4005, currClan));
                            }
                        }
                        else if (isSearchClan)
                        {
                            currClan = getCurrClan();
                            if (currClan != null)
                            {
                                MyVector myVector = new MyVector();
                                myVector.addElement(new Command(mResources.request_join_clan, this, 4000, currClan));
                                myVector.addElement(new Command(mResources.view_clan_member, this, 4001, currClan));
                                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                                addClanDetail(getCurrClan());
                            }
                        }
                        else if (isViewMember)
                        {
                            currMem = getCurrMember();
                            if (currMem != null)
                            {
                                MyVector myVector2 = new MyVector();
                                myVector2.addElement(new Command(mResources.CLOSE, this, 8000, currClan));
                                GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                                GameCanvas.menu.startAt(myVector2, 0, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                                addClanMemberDetail(currMem);
                            }
                        }
                    }
                }
                else if (selected == 0)
                {
                    if (isMessage)
                    {
                        if (cSelected == 0)
                        {
                            if (myMember.size() > 1)
                            {
                                chatClan();
                            }
                            else
                            {
                                member = null;
                                isSearchClan = false;
                                isViewMember = true;
                                isMessage = false;
                                currentListLength = myMember.size() + 2;
                                initTabClans();
                            }
                        }
                        if (cSelected == 1)
                        {
                            Service.gI().clanMessage(1, null, -1);
                        }
                        if (cSelected == 2)
                        {
                            member = null;
                            isSearchClan = false;
                            isViewMember = true;
                            isMessage = false;
                            currentListLength = myMember.size() + 2;
                            initTabClans();
                            getCurrClanOtion();
                        }
                    }
                    else if (isViewMember)
                    {
                        if (cSelected == 0)
                        {
                            isSearchClan = false;
                            isViewMember = false;
                            isMessage = true;
                            currentListLength = ClanMessage.vMessage.size() + 2;
                            initTabClans();
                        }
                        if (cSelected == 1)
                        {
                            if (myMember.size() > 1)
                            {
                                Service.gI().leaveClan();
                            }
                            else
                            {
                                chagenSlogan();
                            }
                        }
                        if (cSelected == 2)
                        {
                            if (myMember.size() > 1)
                            {
                                chagenSlogan();
                            }
                            else
                            {
                                Service.gI().getClan(3, -1, null);
                            }
                        }
                        if (cSelected == 3)
                        {
                            Service.gI().getClan(3, -1, null);
                        }
                    }
                }
                else if (selected == 1)
                {
                    if (isSearchClan)
                    {
                        Service.gI().searchClan(string.Empty);
                    }
                }
                else if (isSearchClan)
                {
                    currClan = getCurrClan();
                    if (currClan != null)
                    {
                        MyVector myVector3 = new MyVector();
                        myVector3.addElement(new Command(mResources.view_clan_member, this, 4001, currClan));
                        GameCanvas.menu.startAt(myVector3, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addClanDetail(getCurrClan());
                    }
                }
                else if (isViewMember)
                {
                    Res.outz("TOI DAY 1");
                    currMem = getCurrMember();
                    if (currMem != null)
                    {
                        MyVector myVector4 = new MyVector();
                        Res.outz("TOI DAY 2");
                        if (member != null)
                        {
                            myVector4.addElement(new Command(mResources.CLOSE, this, 8000, null));
                            Res.outz("TOI DAY 3");
                        }
                        else if (myMember != null)
                        {
                            Res.outz("TOI DAY 4");
                            Res.outz("my role= " + Char.myCharz().role);
                            if (Char.myCharz().charID == currMem.ID || Char.myCharz().role == 2)
                            {
                                myVector4.addElement(new Command(mResources.CLOSE, this, 8000, currMem));
                            }
                            if (Char.myCharz().role < 2 && Char.myCharz().charID != currMem.ID)
                            {
                                Res.outz("TOI DAY");
                                if (currMem.role == 0 || currMem.role == 1)
                                {
                                    myVector4.addElement(new Command(mResources.CLOSE, this, 8000, currMem));
                                }
                                if (currMem.role == 2)
                                {
                                    myVector4.addElement(new Command(mResources.create_clan_co_leader, this, 5002, currMem));
                                }
                                if (Char.myCharz().role == 0)
                                {
                                    myVector4.addElement(new Command(mResources.create_clan_leader, this, 5001, currMem));
                                    if (currMem.role == 1)
                                    {
                                        myVector4.addElement(new Command(mResources.disable_clan_mastership, this, 5003, currMem));
                                    }
                                }
                            }
                            if (Char.myCharz().role < currMem.role)
                            {
                                myVector4.addElement(new Command(mResources.kick_clan_mem, this, 5004, currMem));
                            }
                        }
                        GameCanvas.menu.startAt(myVector4, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addClanMemberDetail(currMem);
                    }
                }
                else if (isMessage)
                {
                    currMess = getCurrMessage();
                    if (currMess != null)
                    {
                        if (currMess.type == 0)
                        {
                            MyVector myVector5 = new MyVector();
                            myVector5.addElement(new Command(mResources.CLOSE, this, 8000, currMess));
                            GameCanvas.menu.startAt(myVector5, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                            addMessageDetail(currMess);
                        }
                        else if (currMess.type == 1)
                        {
                            if (currMess.playerId != Char.myCharz().charID && cSelected != -1)
                            {
                                Service.gI().clanDonate(currMess.id);
                            }
                        }
                        else if (currMess.type == 2 && currMess.option != null)
                        {
                            if (cSelected == 0)
                            {
                                Service.gI().joinClan(currMess.id, 1);
                            }
                            else if (cSelected == 1)
                            {
                                Service.gI().joinClan(currMess.id, 0);
                            }
                        }
                    }
                }
                if (GameCanvas.isTouch)
                {
                    cSelected = -1;
                    selected = -1;
                }
            }
            catch (Exception)
            {
                throw;
            }
        }
    
        private void doFireMain()
        {
            try
            {
                if (currentTabIndex == 0)
                {
                    setTypeMap();
                }
                if (currentTabIndex == 1)
                {
                    doFireInventory();
                }
                if (currentTabIndex == 2)
                {
                    doFireSkill();
                }
                if (currentTabIndex == 3)
                {
                    if (mainTabName.Length == 4)
                    {
                        doFireTool();
                    }
                    else
                    {
                        doFireClanOption();
                    }
                }
                if (currentTabIndex == 4)
                {
                    doFireTool();
                }
            }
            catch (Exception ex)
            {
                Res.outz("Throw ex " + ex.StackTrace);
            }
        }
    
    
        private void doFireSkill()
        {
            if (selected < 0)
            {
                return;
            }
            if (Char.myCharz().statusMe == 14)
            {
                GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                return;
            }
            if (selected == 0 || selected == 1 || selected == 2 || selected == 3 || selected == 4 || selected == 5)
            {
                long cTiemNang = Char.myCharz().cTiemNang;
                long cHPGoc = Char.myCharz().cHPGoc;
                long cMPGoc = Char.myCharz().cMPGoc;
                long cDamGoc = Char.myCharz().cDamGoc;
                int cDefGoc = Char.myCharz().cDefGoc;
                int cCriticalGoc = Char.myCharz().cCriticalGoc;
                int num = 0;
                int num2 = 1000;
                if (selected == 0)
                {
                    if (cTiemNang < Char.myCharz().cHPGoc + num2)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + (Char.myCharz().cHPGoc + num2), false);
                        return;
                    }
                    if (cTiemNang > cHPGoc && cTiemNang < 10 * (2 * (cHPGoc + num2) + 180) / 2)
                    {
                        GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cHPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().hpFrom1000TiemNang + mResources.for_HP, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                        return;
                    }
                    if (cTiemNang >= 10 * (2 * (cHPGoc + num2) + 180) / 2 && cTiemNang < 100 * (2 * (cHPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector = new MyVector(string.Empty);
                        myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(cHPGoc + num2), this, 9000, null));
                        myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * (cHPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                    if (cTiemNang >= 100 * (2 * (cHPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector2 = new MyVector(string.Empty);
                        myVector2.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(cHPGoc + num2), this, 9000, null));
                        myVector2.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * (cHPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector2.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(100 * (2 * (cHPGoc + num2) + 1980) / 2), this, 9007, null));
                        myVector2.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                }
                if (selected == 1)
                {
                    if (Char.myCharz().cTiemNang < Char.myCharz().cMPGoc + num2)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + (Char.myCharz().cMPGoc + num2));
                        return;
                    }
                    if (cTiemNang > cMPGoc && cTiemNang < 10 * (2 * (cMPGoc + num2) + 180) / 2)
                    {
                        GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cMPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().mpFrom1000TiemNang + mResources.for_KI, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                        return;
                    }
                    if (cTiemNang >= 10 * (2 * (cMPGoc + num2) + 180) / 2 && cTiemNang < 100 * (2 * (cMPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector3 = new MyVector(string.Empty);
                        myVector3.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(cHPGoc + num2), this, 9000, null));
                        myVector3.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * (cHPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector3.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 2, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector3, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                    if (cTiemNang >= 100 * (2 * (cMPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector4 = new MyVector(string.Empty);
                        myVector4.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(cMPGoc + num2), this, 9000, null));
                        myVector4.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * (cMPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector4.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(100 * (2 * (cMPGoc + num2) + 1980) / 2), this, 9007, null));
                        myVector4.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 2, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector4, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                }
                if (selected == 2)
                {
                    if (Char.myCharz().cTiemNang < Char.myCharz().cDamGoc * Char.myCharz().expForOneAdd)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + cDamGoc * 100);
                        return;
                    }
                    if (cTiemNang > cDamGoc && cTiemNang < 10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd)
                    {
                        GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + cDamGoc * 100 + mResources.use_potential_point_for2 + Char.myCharz().damFrom1000TiemNang + mResources.for_hit_point, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                        return;
                    }
                    if (cTiemNang >= 10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd && cTiemNang < 100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd)
                    {
                        MyVector myVector5 = new MyVector(string.Empty);
                        myVector5.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(cDamGoc * 100), this, 9000, null));
                        myVector5.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, null));
                        myVector5.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), selected + 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector5, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                    if (cTiemNang >= 100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd)
                    {
                        MyVector myVector6 = new MyVector(string.Empty);
                        myVector6.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(cDamGoc * 100), this, 9000, null));
                        myVector6.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, null));
                        myVector6.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd), this, 9007, null));
                        myVector6.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), selected + 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector6, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                }
                if (selected == 3)
                {
                    if (Char.myCharz().cTiemNang < 50000 + Char.myCharz().cDefGoc * 1000)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + NinjaUtil.getMoneys(Char.myCharz().cTiemNang) + mResources.not_enough_potential_point2 + NinjaUtil.getMoneys(50000 + Char.myCharz().cDefGoc * 1000));
                        return;
                    }
                    long number = (long)(2 * (cDefGoc + 5)) / 2L * 100000;
                    long number2 = 10L * (long)(2 * (cDefGoc + 5) + 9) / 2 * 100000;
                    long number3 = 100L * (long)(2 * (cDefGoc + 5) + 99) / 2 * 100000;
                    mResources.use_potential_point_for1 = mResources.increase_upper;
                    MyVector myVector7 = new MyVector(string.Empty);
                    myVector7.addElement(new Command(mResources.use_potential_point_for1 + "\n1 " + mResources.armor + "\n" + Res.formatNumber2(number), this, 9000, null));
                    myVector7.addElement(new Command(mResources.use_potential_point_for1 + "\n10 " + mResources.armor + "\n" + Res.formatNumber2(number2), this, 9006, null));
                    myVector7.addElement(new Command(mResources.use_potential_point_for1 + "\n100 " + mResources.armor + "\n" + Res.formatNumber2(number3), this, 9007, null));
                    myVector7.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), selected + 1, TypePoint.Master));
                    GameCanvas.menu.startAt(myVector7, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addSkillDetail2(selected);
                }
                else if (selected == 4)
                {
                    long num3 = 50000000L;
                    int num4 = Char.myCharz().cCriticalGoc;
                    if (num4 > t_tiemnang.Length - 1)
                    {
                        num4 = t_tiemnang.Length - 1;
                    }
                    num3 = t_tiemnang[num4];
                    if (Char.myCharz().cTiemNang < num3)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Res.formatNumber2(Char.myCharz().cTiemNang) + mResources.not_enough_potential_point2 + Res.formatNumber2(num3));
                        return;
                    }
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + Res.formatNumber(num3) + mResources.use_potential_point_for2 + Char.myCharz().criticalFrom1000Tiemnang + mResources.for_crit, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                }
                else if (selected == 5)
                {
                    Service.gI().speacialSkill(0);
                }
                return;
            }
            int num5 = selected - 6;
            SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[num5];
            Skill skill = Char.myCharz().getSkill(skillTemplate);
            Skill skill2 = null;
            MyVector myVector8 = new MyVector(string.Empty);
            if (skill != null)
            {
                if (skill.point == skillTemplate.maxPoint)
                {
                    myVector8.addElement(new Command(mResources.make_shortcut, this, 9003, skill.template));
                    myVector8.addElement(new Command(mResources.CLOSE, 2));
                }
                else
                {
                    skill2 = skillTemplate.skills[skill.point];
                    myVector8.addElement(new Command(mResources.UPGRADE, this, 9002, skill2));
                    myVector8.addElement(new Command(mResources.make_shortcut, this, 9003, skill.template));
                }
            }
            else
            {
                skill2 = skillTemplate.skills[0];
                myVector8.addElement(new Command(mResources.learn, this, 9004, skill2));
            }
            GameCanvas.menu.startAt(myVector8, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addSkillDetail(skillTemplate, skill, skill2);
        }
        private void addLogMessage(InfoItem info)
        {
            string text = "|0|1|" + info.charInfo.cName;
            text += "\n";
            text += "\n--";
            text = text + "\n|5|" + Res.split(info.s, "|", 0)[2];
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            charInfo = info.charInfo;
            currItem = null;
        }
    
        private void addSkillDetail2(int type)
        {
            string empty = string.Empty;
            long num = 0;
            if (selected == 0)
            {
                num = Char.myCharz().cHPGoc + 1000;
            }
            if (selected == 1)
            {
                num = Char.myCharz().cMPGoc + 1000;
            }
            if (selected == 2)
            {
                num = Char.myCharz().cDamGoc * Char.myCharz().expForOneAdd;
            }
            if (selected == 3)
            {
                num = 500000 + Char.myCharz().cDefGoc * 100000;
            }
            string text = empty;
            empty = text + "|5|2|" + mResources.USE + " " + num + " " + mResources.potential;
            if (type == 0)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_20hp;
            }
            if (type == 1)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_20mp;
            }
            if (type == 2)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_1pow;
            }
            if (type == 3)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_1pow;
            }
            currItem = null;
            partID = null;
            charInfo = null;
            idIcon = -1;
            cp = new ChatPopup();
            popUpDetailInit(cp, empty);
        }
    
        private void doFireClanIcon()
        {
        }
    
        private void doFireMap()
        {
            if (imgMap != null)
            {
                imgMap.texture = null;
                imgMap = null;
            }
            TileMap.lastPlanetId = -1;
            mSystem.gcc();
            SmallImage.loadBigRMS();
            setTypeMain();
            cmx = (cmtoX = 0);
        }
    
        private void doFireZone()
        {
            if (selected != -1)
            {
                Res.outz("FIRE ZONE");
                isChangeZone = true;
                GameCanvas.panel.hide();
            }
        }
    
        public void updateRequest(int recieve, int maxCap)
        {
            cp.says[cp.says.Length - 1] = mResources.received + " " + recieve + "/" + maxCap;
        }
    
    
        public void itemRequest(sbyte itemAction, string info, sbyte where, sbyte index)
        {
            GameCanvas.endDlg();
            ItemObject itemObject = new ItemObject();
            itemObject.type = itemAction;
            itemObject.id = index;
            itemObject.where = where;
            GameCanvas.startYesNoDlg(info, new Command(mResources.YES, this, 2004, itemObject), new Command(mResources.NO, this, 4005, null));
        }
    
        public void saleRequest(sbyte type, string info, short id)
        {
            ItemObject itemObject = new ItemObject();
            itemObject.type = type;
            itemObject.id = id;
            GameCanvas.startYesNoDlg(info, new Command(mResources.YES, this, 3003, itemObject), new Command(mResources.NO, this, 4005, null));
        }
    }
}

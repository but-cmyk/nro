namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        public void perform(int idAction, object p)
        {
            if (idAction == 9998)
            {
                cp = null;
                GameCanvas.menu.doCloseMenu();
                TopInfo topInfo = (TopInfo)p;
                if (topInfo != null)
                {
                    hide();
                    Service.gI().sendThachDau(topInfo.pId);
                }
                return;
            }
            if (idAction == 9999)
            {
                cp = null;
                GameCanvas.menu.doCloseMenu();
                return;
            }
            if (idAction == 170391)
            {
                Rms.clearAll();
                if (mGraphics.zoomLevel > 1)
                {
                    Rms.saveRMSInt("levelScreenKN", 1);
                }
                else
                {
                    Rms.saveRMSInt("levelScreenKN", 0);
                }
                GameMidlet.instance.exit();
            }
            if (idAction == 6001)
            {
                Item item = (Item)p;
                item.isSelect = false;
                GameCanvas.panel.vItemCombine.removeElement(item);
                if (GameCanvas.panel.currentTabIndex == 0)
                {
                    GameCanvas.panel.setTabCombine();
                }
            }
            if (idAction == 6000)
            {
                Item item2 = (Item)p;
                for (int i = 0; i < GameCanvas.panel.vItemCombine.size(); i++)
                {
                    Item item3 = (Item)GameCanvas.panel.vItemCombine.elementAt(i);
                    if (item3.template.id == item2.template.id)
                    {
                        GameCanvas.startOKDlg(mResources.already_has_item);
                        return;
                    }
                }
                item2.isSelect = true;
                GameCanvas.panel.vItemCombine.addElement(item2);
                if (GameCanvas.panel.currentTabIndex == 0)
                {
                    GameCanvas.panel.setTabCombine();
                }
            }
            if (idAction == 7000)
            {
                if (isLock)
                {
                    GameCanvas.startOKDlg(mResources.unlock_item_to_trade);
                    return;
                }
                Item item4 = (Item)p;
                for (int j = 0; j < GameCanvas.panel.vMyGD.size(); j++)
                {
                    Item item5 = (Item)GameCanvas.panel.vMyGD.elementAt(j);
                    if (item5.indexUI == item4.indexUI)
                    {
                        GameCanvas.startOKDlg(mResources.already_has_item);
                        return;
                    }
                }
                if (item4.quantity > 1)
                {
                    putQuantily();
                    return;
                }
                item4.isSelect = true;
                Item item6 = new Item();
                item6.template = item4.template;
                item6.itemOption = item4.itemOption;
                item6.indexUI = item4.indexUI;
                GameCanvas.panel.vMyGD.addElement(item6);
                Service.gI().giaodich(2, -1, (sbyte)item6.indexUI, item6.quantity);
            }
            if (idAction == 7001)
            {
                Item item7 = (Item)p;
                item7.isSelect = false;
                GameCanvas.panel.vMyGD.removeElement(item7);
                if (GameCanvas.panel.currentTabIndex == 1)
                {
                    GameCanvas.panel.setTabGiaoDich(true);
                }
                Service.gI().giaodich(4, -1, (sbyte)item7.indexUI, -1);
            }
            if (idAction == 7002)
            {
                isAccept = true;
                GameCanvas.endDlg();
                Service.gI().giaodich(7, -1, -1, -1);
                InfoDlg.showWait();
            }
            if (idAction == 8003)
            {
                InfoItem infoItem = (InfoItem)p;
                Service.gI().friend(1, infoItem.charInfo.charID);
                if (type != 8)
                {
                }
            }
            if (idAction == 8002)
            {
                InfoItem infoItem2 = (InfoItem)p;
                Service.gI().friend(2, infoItem2.charInfo.charID);
            }
            if (idAction == 8004)
            {
                InfoItem infoItem3 = (InfoItem)p;
                Service.gI().gotoPlayer(infoItem3.charInfo.charID);
            }
            if (idAction == 8001)
            {
                Res.outz("chat player");
                InfoItem infoItem4 = (InfoItem)p;
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = GameCanvas.panel;
                }
                chatTField.strChat = mResources.chat_player;
                chatTField.tfChat.name = mResources.chat_with + " " + infoItem4.charInfo.cName;
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
            if (idAction == 1000)
            {
                Service.gI().getItem(BOX_BAG, (sbyte)GetInventorySelect_body(selected, newSelected));
            }
            if (idAction == 1001)
            {
                sbyte id = (sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
                Service.gI().getItem(BAG_BOX, id);
            }
            if (idAction == 1003)
            {
                hide();
            }
            if (idAction == 1002)
            {
                Service.gI().getItem(BODY_BOX, (sbyte)GetInventorySelect_body(selected, newSelected));
            }
            if (idAction == 2011)
            {
                Service.gI().useItem(1, 2, (sbyte)GetInventorySelect_body(selected, newSelected), -1);
            }
            if (idAction == 2010)
            {
                Service.gI().useItem(0, 2, (sbyte)GetInventorySelect_body(selected, newSelected), -1);
                Item item8 = (Item)p;
                if (item8 != null && (item8.template.id == 193 || item8.template.id == 194))
                {
                    GameCanvas.panel.hide();
                }
            }
            if (idAction == 2000)
            {
                Item[] arrItemBody = Char.myCharz().arrItemBody;
                sbyte id2 = (sbyte)GetInventorySelect_bag(selected, newSelected, arrItemBody);
                if (isnewInventory)
                {
                    id2 = (sbyte)currItem.indexUI;
                }
                Service.gI().getItem(BAG_BODY, id2);
            }
            if (idAction == 2001 || idAction == 2026)
            {
                Res.outz("use item");
                Item item9 = (Item)p;
                sbyte useType = (sbyte)(idAction == 2026 ? 4 : 0);
                bool inventorySelect_isbody = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
                sbyte b = 0;
                b = (inventorySelect_isbody ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
                if (isnewInventory)
                {
                    b = (sbyte)currItem.indexUI;
                    sbyte where = 0;
                    if (newSelected != 0)
                    {
                        where = 1;
                    }
                    Service.gI().useItem(useType, where, b, -1);
                }
                else
                {
                    Service.gI().useItem(useType, (!inventorySelect_isbody) ? ((sbyte)1) : ((sbyte)0), b, -1);
                }
                if (item9.template.id == 193 || item9.template.id == 194)
                {
                    GameCanvas.panel.hide();
                }
            }
            if (idAction == 2002)
            {
                if (isnewInventory)
                {
                    Service.gI().getItem(BODY_BAG, (sbyte)sellectInventory);
                }
                else
                {
                    Service.gI().getItem(BODY_BAG, (sbyte)GetInventorySelect_body(selected, newSelected));
                }
            }
            if (idAction == 2003)
            {
                Res.outz("remove item");
                bool inventorySelect_isbody2 = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
                sbyte b2 = 0;
                b2 = (inventorySelect_isbody2 ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
                Service.gI().useItem(1, (!inventorySelect_isbody2) ? ((sbyte)1) : ((sbyte)0), b2, -1);
            }
            if (idAction == 2004)
            {
                GameCanvas.endDlg();
                ItemObject itemObject = (ItemObject)p;
                sbyte where2 = (sbyte)itemObject.where;
                sbyte index = (sbyte)itemObject.id;
                Service.gI().useItem((sbyte)((itemObject.type != 0) ? 2 : 3), where2, index, -1);
            }
            if (idAction == 2005)
            {
                sbyte id3 = (sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
                Service.gI().getItem(BAG_PET, id3);
            }
            if (idAction == 2006)
            {
                Item[] arrItemBody2 = Char.myPetz().arrItemBody;
                sbyte id4 = (sbyte)selected;
                Service.gI().getItem(PET_BAG, id4);
            }
            if (idAction == 30001)
            {
                Res.outz("nhan do");
                Service.gI().buyItem(0, selected, 0);
            }
            if (idAction == 30002)
            {
                Res.outz("xoa do");
                Service.gI().buyItem(1, selected, 0);
            }
            if (idAction == 30003)
            {
                Res.outz("nhan tat");
                Service.gI().buyItem(2, selected, 0);
            }
            if (idAction == 3000)
            {
                Res.outz("mua do");
                Item item10 = (Item)p;
                Service.gI().buyItem(0, item10.template.id, 0);
            }
            if (idAction == 3001)
            {
                Item item11 = (Item)p;
                GameCanvas.msgdlg.pleasewait();
                Service.gI().buyItem(1, item11.template.id, 0);
            }
            if (idAction == 3002)
            {
                GameCanvas.endDlg();
                bool inventorySelect_isbody3 = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
                sbyte b3 = 0;
                b3 = (inventorySelect_isbody3 ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
                Service.gI().saleItem(0, (!inventorySelect_isbody3) ? ((sbyte)1) : ((sbyte)0), b3);
            }
            if (idAction == 3003)
            {
                GameCanvas.endDlg();
                ItemObject itemObject2 = (ItemObject)p;
                Service.gI().saleItem(1, (sbyte)itemObject2.type, (short)itemObject2.id);
            }
            if (idAction == 3004)
            {
                Item item12 = (Item)p;
                Service.gI().buyItem(3, item12.template.id, 0);
            }
            if (idAction == 3005)
            {
                Res.outz("mua do");
                Item item13 = (Item)p;
                Service.gI().buyItem(3, item13.template.id, 0);
            }
            if (idAction == 4000)
            {
                Clan clan = (Clan)p;
                if (clan != null)
                {
                    GameCanvas.endDlg();
                    Service.gI().clanMessage(2, null, clan.ID);
                }
            }
            if (idAction == 4001)
            {
                Clan clan2 = (Clan)p;
                if (clan2 != null)
                {
                    InfoDlg.showWait();
                    clanReport = mResources.PLEASEWAIT;
                    Service.gI().clanMember(clan2.ID);
                }
            }
            if (idAction == 4005)
            {
                GameCanvas.endDlg();
            }
            if (idAction == 4007)
            {
                GameCanvas.endDlg();
            }
            if (idAction == 4006)
            {
                ClanMessage clanMessage = (ClanMessage)p;
                Service.gI().clanDonate(clanMessage.id);
            }
            if (idAction == 5001)
            {
                Member member = (Member)p;
                Service.gI().clanRemote(member.ID, 0);
            }
            if (idAction == 5002)
            {
                Member member2 = (Member)p;
                Service.gI().clanRemote(member2.ID, 1);
            }
            if (idAction == 5003)
            {
                Member member3 = (Member)p;
                Service.gI().clanRemote(member3.ID, 2);
            }
            if (idAction == 5004)
            {
                Member member4 = (Member)p;
                Service.gI().clanRemote(member4.ID, -1);
            }
            if (idAction == 9000)
            {
                Service.gI().upPotential(selected, 1);
                GameCanvas.endDlg();
                InfoDlg.showWait();
            }
            if (idAction == 9006)
            {
                Service.gI().upPotential(selected, 10);
                GameCanvas.endDlg();
                InfoDlg.showWait();
            }
            if (idAction == 9007)
            {
                Service.gI().upPotential(selected, 100);
                GameCanvas.endDlg();
                InfoDlg.showWait();
            }
            if (idAction == 9002)
            {
                Skill skill = (Skill)p;
                if (skill.template.isSkillSpec())
                {
                    GameCanvas.startOKDlg(mResources.updSkill);
                }
                else
                {
                    GameCanvas.startOKDlg(mResources.can_buy_from_Uron1 + skill.powRequire + mResources.can_buy_from_Uron2 + skill.moreInfo + mResources.can_buy_from_Uron3);
                }
            }
            if (idAction == 9003)
            {
                if (GameCanvas.isTouch && !Main.isPC)
                {
                    GameScr.gI().doSetOnScreenSkill((SkillTemplate)p);
                }
                else
                {
                    GameScr.gI().doSetKeySkill((SkillTemplate)p);
                }
            }
            if (idAction == 9004)
            {
                Skill skill2 = (Skill)p;
                if (skill2.template.isSkillSpec())
                {
                    GameCanvas.startOKDlg(mResources.learnSkill);
                }
                else
                {
                    GameCanvas.startOKDlg(mResources.can_buy_from_Uron1 + skill2.powRequire + mResources.can_buy_from_Uron2 + skill2.moreInfo + mResources.can_buy_from_Uron3);
                }
            }
            if (idAction == 10000)
            {
                InfoItem infoItem5 = (InfoItem)p;
                Service.gI().enemy(1, infoItem5.charInfo.charID);
                GameCanvas.panel.hideNow();
            }
            if (idAction == 10001)
            {
                InfoItem infoItem6 = (InfoItem)p;
                Service.gI().enemy(2, infoItem6.charInfo.charID);
                InfoDlg.showWait();
            }
            if (idAction == 10021)
            {
            }
            if (idAction == 10012)
            {
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = ((GameCanvas.panel2 != null) ? GameCanvas.panel2 : GameCanvas.panel);
                }
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                chatTField.tfChat.setText(string.Empty);
                if (currItem.quantity == 1)
                {
                    chatTField.strChat = mResources.kiguiXuchat;
                    chatTField.tfChat.name = mResources.input_money;
                }
                else
                {
                    chatTField.strChat = mResources.input_quantity + " ";
                    chatTField.tfChat.name = mResources.input_quantity;
                }
                chatTField.tfChat.setMaxTextLenght(10);
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
            if (idAction == 10013)
            {
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = ((GameCanvas.panel2 != null) ? GameCanvas.panel2 : GameCanvas.panel);
                }
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                chatTField.tfChat.setText(string.Empty);
                if (currItem.quantity == 1)
                {
                    chatTField.strChat = mResources.kiguiLuongchat;
                    chatTField.tfChat.name = mResources.input_money;
                }
                else
                {
                    chatTField.strChat = mResources.input_quantity + "  ";
                    chatTField.tfChat.name = mResources.input_quantity;
                }
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
            if (idAction == 10014)
            {
                Item item14 = (Item)p;
                Service.gI().kigui(1, item14.itemId, -1, -1, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10015)
            {
                Item item15 = (Item)p;
                Service.gI().kigui(2, item15.itemId, -1, -1, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10016)
            {
                Item item16 = (Item)p;
                Service.gI().kigui(3, item16.itemId, 0, item16.buyCoin, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10017)
            {
                Item item17 = (Item)p;
                Service.gI().kigui(3, item17.itemId, 1, item17.buyGold, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10018)
            {
                Item item18 = (Item)p;
                Service.gI().kigui(5, item18.itemId, -1, -1, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10019)
            {
                Session_ME.gI().close();
                Rms.saveRMSString("acc", string.Empty);
                Rms.saveRMSString("pass", string.Empty);
                GameCanvas.loginScr.tfPass.setText(string.Empty);
                GameCanvas.loginScr.tfUser.setText(string.Empty);
                GameCanvas.loginScr.isLogin2 = false;
                GameCanvas.loginScr.switchToMe();
                GameCanvas.endDlg();
                hide();
            }
            if (idAction == 10020)
            {
                GameCanvas.endDlg();
            }
            if (idAction == 10030)
            {
                Service.gI().getFlag(1, (sbyte)selected);
                GameCanvas.panel.hideNow();
            }
            if (idAction == 10031)
            {
                Session_ME.gI().close();
            }
            if (idAction == 11000)
            {
                Service.gI().kigui(0, currItem.itemId, 1, currItem.buyRuby, 1);
                GameCanvas.endDlg();
            }
            if (idAction == 11001)
            {
                Service.gI().kigui(0, currItem.itemId, 1, currItem.buyRuby, currItem.quantilyToBuy);
                GameCanvas.endDlg();
            }
            if (idAction == 11002)
            {
                chatTField.isShow = false;
                GameCanvas.endDlg();
            }
        }
    
        public void onChatFromMe(string text, string to)
        {
            if (chatTField.tfChat.getText() == null || chatTField.tfChat.getText().Equals(string.Empty) || text.Equals(string.Empty) || text == null)
            {
                chatTField.isShow = false;
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_clan_name))
            {
                InfoDlg.showWait();
                chatTField.isShow = false;
                Service.gI().searchClan(text);
                return;
            }
            if (chatTField.strChat.Equals(mResources.chat_clan))
            {
                InfoDlg.showWait();
                chatTField.isShow = false;
                Service.gI().clanMessage(0, text, -1);
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_clan_name_to_create))
            {
                if (chatTField.tfChat.getText() == string.Empty)
                {
                    GameScr.info1.addInfo(mResources.clan_name_blank, 0);
                    return;
                }
                if (tabIcon == null)
                {
                    tabIcon = new TabClanIcon();
                }
                tabIcon.text = chatTField.tfChat.getText();
                tabIcon.show(false);
                chatTField.isShow = false;
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_clan_slogan))
            {
                if (chatTField.tfChat.getText() == string.Empty)
                {
                    GameScr.info1.addInfo(mResources.clan_slogan_blank, 0);
                    return;
                }
                Service.gI().getClan(4, (sbyte)Char.myCharz().clan.imgID, chatTField.tfChat.getText());
                chatTField.isShow = false;
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_Inventory_Pass))
            {
                try
                {
                    int lockInventory = int.Parse(chatTField.tfChat.getText());
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    hide();
                    if (chatTField.tfChat.getText().Length != 6 || chatTField.tfChat.getText().Equals(string.Empty))
                    {
                        GameCanvas.startOKDlg(mResources.input_Inventory_Pass_wrong);
                    }
                    else
                    {
                        Service.gI().setLockInventory(lockInventory);
                        chatTField.isShow = false;
                        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                        hide();
                    }
                    return;
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.ALERT_PRIVATE_PASS_2);
                    return;
                }
            }
            if (chatTField.strChat.Equals(mResources.world_channel_5_luong))
            {
                if (!chatTField.tfChat.getText().Equals(string.Empty))
                {
                    Service.gI().chatGlobal(chatTField.tfChat.getText());
                    chatTField.isShow = false;
                    hide();
                }
            }
            else if (chatTField.strChat.Equals(mResources.chat_player))
            {
                chatTField.isShow = false;
                InfoItem infoItem = null;
                if (type == 8)
                {
                    infoItem = (InfoItem)logChat.elementAt(currInfoItem);
                }
                else if (type == 11)
                {
                    infoItem = (InfoItem)vFriend.elementAt(currInfoItem);
                }
                if (infoItem.charInfo.charID != Char.myCharz().charID)
                {
                    Service.gI().chatPlayer(text, infoItem.charInfo.charID);
                }
            }
            else if (chatTField.strChat.Equals(mResources.input_quantity_to_trade))
            {
                int num = 0;
                try
                {
                    num = int.Parse(chatTField.tfChat.getText());
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    return;
                }
                if (num <= 0 || num > currItem.quantity)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    return;
                }
                currItem.isSelect = true;
                Item item = new Item();
                item.template = currItem.template;
                item.quantity = num;
                item.indexUI = currItem.indexUI;
                item.itemOption = currItem.itemOption;
                GameCanvas.panel.vMyGD.addElement(item);
                Service.gI().giaodich(2, -1, (sbyte)item.indexUI, item.quantity);
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            }
            else if (chatTField.strChat == mResources.input_money_to_trade)
            {
                int num2 = 0;
                try
                {
                    num2 = int.Parse(chatTField.tfChat.getText());
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_money_wrong);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    return;
                }
                if (num2 > Char.myCharz().xu)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_money);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                }
                else
                {
                    moneyGD = num2;
                    Service.gI().giaodich(2, -1, -1, num2);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                }
            }
            else if (chatTField.strChat.Equals(mResources.kiguiXuchat))
            {
                try
                {
                    Service.gI().kigui(0, currItem.itemId, 0, int.Parse(chatTField.tfChat.getText()), 1);
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_money_wrong);
                }
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.kiguiXuchat + " "))
            {
                try
                {
                    Service.gI().kigui(0, currItem.itemId, 0, int.Parse(chatTField.tfChat.getText()), currItem.quantilyToBuy);
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_money_wrong);
                }
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.kiguiLuongchat))
            {
                doNotiRuby(0);
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.kiguiLuongchat + "  "))
            {
                doNotiRuby(1);
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.input_quantity + " "))
            {
                currItem.quantilyToBuy = int.Parse(chatTField.tfChat.getText());
                if (currItem.quantilyToBuy > currItem.quantity)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    return;
                }
                isKiguiXu = true;
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.input_quantity + "  "))
            {
                currItem.quantilyToBuy = int.Parse(chatTField.tfChat.getText());
                if (currItem.quantilyToBuy > currItem.quantity)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    return;
                }
                isKiguiLuong = true;
                chatTField.isShow = false;
            }
        }
    
        public void onCancelChat()
        {
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        }
    
        public void setCombineEff(int type)
        {
            typeCombine = type;
            rS = 90;
            if (typeCombine == 0)
            {
                iDotS = 5;
                angleS = (angleO = 90);
                time = 2;
                for (int i = 0; i < vItemCombine.size(); i++)
                {
                    Item item = (Item)vItemCombine.elementAt(i);
                    if (item != null)
                    {
                        if (item.template.type == 14)
                        {
                            iconID2 = item.template.iconID;
                        }
                        else
                        {
                            iconID1 = item.template.iconID;
                        }
                    }
                }
            }
            else if (typeCombine == 1)
            {
                iDotS = 2;
                angleS = (angleO = 0);
                time = 1;
                for (int j = 0; j < vItemCombine.size(); j++)
                {
                    Item item2 = (Item)vItemCombine.elementAt(j);
                    if (item2 != null)
                    {
                        if (j == 0)
                        {
                            iconID1 = item2.template.iconID;
                        }
                        else
                        {
                            iconID2 = item2.template.iconID;
                        }
                    }
                }
            }
            else if (typeCombine == 2)
            {
                iDotS = 7;
                angleS = (angleO = 25);
                time = 1;
                for (int k = 0; k < vItemCombine.size(); k++)
                {
                    Item item3 = (Item)vItemCombine.elementAt(k);
                    if (item3 != null)
                    {
                        iconID1 = item3.template.iconID;
                    }
                }
            }
            else if (typeCombine == 3)
            {
                xS = GameCanvas.hw;
                yS = GameCanvas.hh;
                iDotS = 1;
                angleS = (angleO = 1);
                time = 4;
                for (int l = 0; l < vItemCombine.size(); l++)
                {
                    Item item4 = (Item)vItemCombine.elementAt(l);
                    if (item4 != null)
                    {
                        iconID1 = item4.template.iconID;
                    }
                }
            }
            else if (typeCombine == 4)
            {
                iDotS = vItemCombine.size();
                iconID = new short[iDotS];
                angleS = (angleO = 25);
                time = 1;
                for (int m = 0; m < vItemCombine.size(); m++)
                {
                    Item item5 = (Item)vItemCombine.elementAt(m);
                    if (item5 != null)
                    {
                        iconID[m] = item5.template.iconID;
                    }
                }
            }
            speed = 1;
            isSpeedCombine = true;
            isDoneCombine = false;
            isCompleteEffCombine = false;
            iAngleS = 360 / iDotS;
            xArgS = new int[iDotS];
            yArgS = new int[iDotS];
            xDotS = new int[iDotS];
            yDotS = new int[iDotS];
            setDotStar();
            isPaintCombine = true;
            countUpdate = 10;
            countR = 30;
            countWait = 10;
            addTextCombineNPC(idNPC, mResources.combineSpell);
        }
    }
}

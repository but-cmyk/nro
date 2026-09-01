namespace Game1
{
    using System;

    public class TaskNpcPacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case -70: // BIG MESSAGE
                        {
                            Res.outz("BIG MESSAGE .......................................");
                            GameCanvas.endDlg();
                            int avatar2 = msg.reader().readShort();
                            string chat3 = msg.reader().readUTF();
                            Npc npc6 = new Npc(-1, 0, 0, 0, 0, 0);
                            npc6.avatar = avatar2;
                            ChatPopup.addBigMessage(chat3, 100000, npc6);
                            sbyte b47 = msg.reader().readByte();
                            if (b47 == 0)
                            {
                                ChatPopup.serverChatPopUp.cmdMsg1 = new Command(mResources.CLOSE, ChatPopup.serverChatPopUp, 1001, null);
                                ChatPopup.serverChatPopUp.cmdMsg1.x = GameCanvas.w / 2 - 35;
                                ChatPopup.serverChatPopUp.cmdMsg1.y = GameCanvas.h - 35;
                            }
                            if (b47 == 1)
                            {
                                string p2 = msg.reader().readUTF();
                                string caption2 = msg.reader().readUTF();
                                ChatPopup.serverChatPopUp.cmdMsg1 = new Command(caption2, ChatPopup.serverChatPopUp, 1000, p2);
                                ChatPopup.serverChatPopUp.cmdMsg1.x = GameCanvas.w / 2 - 75;
                                ChatPopup.serverChatPopUp.cmdMsg1.y = GameCanvas.h - 35;
                                ChatPopup.serverChatPopUp.cmdMsg2 = new Command(mResources.CLOSE, ChatPopup.serverChatPopUp, 1001, null);
                                ChatPopup.serverChatPopUp.cmdMsg2.x = GameCanvas.w / 2 + 11;
                                ChatPopup.serverChatPopUp.cmdMsg2.y = GameCanvas.h - 35;
                            }
                            return true;
                        }

                    case 38: // OPEN_UI_SAY (ChatPopup with NPC)
                        {
                            GameCanvas.debug("SA67", 2);
                            InfoDlg.hide();
                            int num87 = msg.reader().readShort();
                            Res.outz("OPEN_UI_SAY ID= " + num87);
                            string str = msg.reader().readUTF();
                            str = Res.changeString(str);
                            for (int num121 = 0; num121 < GameScr.vNpc.size(); num121++)
                            {
                                Npc npc4 = (Npc)GameScr.vNpc.elementAt(num121);
                                if (npc4 != null && npc4.template != null && npc4.template.npcTemplateId == num87)
                                {
                                    ChatPopup.addChatPopupMultiLine(str, 100000, npc4);
                                    if (GameCanvas.panel != null) GameCanvas.panel.hideNow();
                                    return true;
                                }
                            }
                            int defaultGender = (Char.myCharz() != null) ? Char.myCharz().cgender : 0;
                            int charIdVal = (GameScr.info1 != null && GameScr.info1.charId != null && defaultGender >= 0 && defaultGender < GameScr.info1.charId.Length) 
                                ? GameScr.info1.charId[defaultGender][2] : 0;
                            Npc npc5 = new Npc(num87, 0, 0, 0, num87, charIdVal);
                            if (npc5.template != null && npc5.template.npcTemplateId == 5)
                            {
                                npc5.charID = 5;
                            }
                            try
                            {
                                npc5.avatar = msg.reader().readShort();
                            }
                            catch (Exception)
                            {
                            }
                            ChatPopup.addChatPopupMultiLine(str, 100000, npc5);
                            if (GameCanvas.panel != null) GameCanvas.panel.hideNow();
                            return true;
                        }

                    case 32: // NPC Menu
                        {
                            GameCanvas.debug("SA68", 2);
                            int num87 = msg.reader().readShort();
                            for (int num88 = 0; num88 < GameScr.vNpc.size(); num88++)
                            {
                                Npc npc = (Npc)GameScr.vNpc.elementAt(num88);
                                if (npc != null && npc.template != null && npc.template.npcTemplateId == num87 && Char.myCharz() != null && npc.Equals(Char.myCharz().npcFocus))
                                {
                                    string chat = msg.reader().readUTF();
                                    string[] array7 = new string[msg.reader().readByte()];
                                    for (int num89 = 0; num89 < array7.Length; num89++)
                                    {
                                        array7[num89] = msg.reader().readUTF();
                                    }
                                    GameScr.gI().createMenu(array7, npc);
                                    ChatPopup.addChatPopup(chat, 100000, npc);
                                    return true;
                                }
                            }
                            int defaultGender2 = (Char.myCharz() != null) ? Char.myCharz().cgender : 0;
                            int charIdVal2 = (GameScr.info1 != null && GameScr.info1.charId != null && defaultGender2 >= 0 && defaultGender2 < GameScr.info1.charId.Length) 
                                ? GameScr.info1.charId[defaultGender2][2] : 0;
                            Npc npc2 = new Npc(num87, 0, -100, 100, num87, charIdVal2);
                            string chat2 = msg.reader().readUTF();
                            string[] array8 = new string[msg.reader().readByte()];
                            for (int num90 = 0; num90 < array8.Length; num90++)
                            {
                                array8[num90] = msg.reader().readUTF();
                            }
                            try
                            {
                                short avatar = msg.reader().readShort();
                                npc2.avatar = avatar;
                            }
                            catch (Exception)
                            {
                            }
                            GameScr.gI().createMenu(array8, npc2);
                            ChatPopup.addChatPopup(chat2, 100000, npc2);
                            return true;
                        }
                }
            }
            catch (Exception ex)
            {
                Cout.println("Loi TaskNpcPacketHandler: " + ex.ToString());
            }
            return false;
        }
    }
}

namespace Game1
{
    using System;

    public class ClanPacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case -51:
                        InfoDlg.hide();
                        controller.readClanMsg(msg, 0);
                        if (GameCanvas.panel.isMessage && GameCanvas.panel.type == 5)
                        {
                            GameCanvas.panel.initTabClans();
                        }
                        return true;

                    case -53:
                        {
                            InfoDlg.hide();
                            bool flag8 = false;
                            int num107 = msg.reader().readInt();
                            Res.outz("clanId= " + num107);
                            if (num107 == -1)
                            {
                                flag8 = true;
                                Char.myCharz().clan = null;
                                ClanMessage.vMessage.removeAllElements();
                                if (GameCanvas.panel.member != null)
                                {
                                    GameCanvas.panel.member.removeAllElements();
                                }
                                if (GameCanvas.panel.myMember != null)
                                {
                                    GameCanvas.panel.myMember.removeAllElements();
                                }
                                if (GameCanvas.currentScreen == GameScr.gI())
                                {
                                    GameCanvas.panel.setTabClans();
                                }
                                return true;
                            }
                            GameCanvas.panel.tabIcon = null;
                            if (Char.myCharz().clan == null)
                            {
                                Char.myCharz().clan = new Clan();
                            }
                            Char.myCharz().clan.ID = num107;
                            Char.myCharz().clan.name = msg.reader().readUTF();
                            Char.myCharz().clan.slogan = msg.reader().readUTF();
                            Char.myCharz().clan.imgID = msg.reader().readUnsignedByte();
                            Char.myCharz().clan.powerPoint = msg.reader().readUTF();
                            Char.myCharz().clan.leaderName = msg.reader().readUTF();
                            Char.myCharz().clan.currMember = msg.reader().readUnsignedByte();
                            Char.myCharz().clan.maxMember = msg.reader().readUnsignedByte();
                            Char.myCharz().role = msg.reader().readByte();
                            Char.myCharz().clan.clanPoint = msg.reader().readInt();
                            Char.myCharz().clan.level = msg.reader().readByte();
                            GameCanvas.panel.myMember = new MyVector();
                            for (int num108 = 0; num108 < Char.myCharz().clan.currMember; num108++)
                            {
                                Member member5 = new Member();
                                member5.ID = msg.reader().readInt();
                                member5.head = msg.reader().readShort();
                                member5.headICON = msg.reader().readShort();
                                member5.leg = msg.reader().readShort();
                                member5.body = msg.reader().readShort();
                                member5.name = msg.reader().readUTF();
                                member5.role = msg.reader().readByte();
                                member5.powerPoint = msg.reader().readUTF();
                                member5.donate = msg.reader().readInt();
                                member5.receive_donate = msg.reader().readInt();
                                member5.clanPoint = msg.reader().readInt();
                                member5.curClanPoint = msg.reader().readInt();
                                member5.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                GameCanvas.panel.myMember.addElement(member5);
                            }
                            int num109 = msg.reader().readUnsignedByte();
                            for (int num110 = 0; num110 < num109; num110++)
                            {
                                controller.readClanMsg(msg, -1);
                            }
                            if (GameCanvas.panel.isSearchClan || GameCanvas.panel.isViewMember || GameCanvas.panel.isMessage)
                            {
                                GameCanvas.panel.setTabClans();
                            }
                            if (flag8)
                            {
                                GameCanvas.panel.setTabClans();
                            }
                            Res.outz("=>>>>>>>>>>>>>>>>>>>>>> -537 MY CLAN INFO");
                            return true;
                        }

                    case -52:
                        {
                            sbyte b20 = msg.reader().readByte();
                            if (b20 == 0)
                            {
                                Member member2 = new Member();
                                member2.ID = msg.reader().readInt();
                                member2.head = msg.reader().readShort();
                                member2.headICON = msg.reader().readShort();
                                member2.leg = msg.reader().readShort();
                                member2.body = msg.reader().readShort();
                                member2.name = msg.reader().readUTF();
                                member2.role = msg.reader().readByte();
                                member2.powerPoint = msg.reader().readUTF();
                                member2.donate = msg.reader().readInt();
                                member2.receive_donate = msg.reader().readInt();
                                member2.clanPoint = msg.reader().readInt();
                                member2.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                if (GameCanvas.panel.myMember == null)
                                {
                                    GameCanvas.panel.myMember = new MyVector();
                                }
                                GameCanvas.panel.myMember.addElement(member2);
                                GameCanvas.panel.initTabClans();
                            }
                            if (b20 == 1)
                            {
                                GameCanvas.panel.myMember.removeElementAt(msg.reader().readByte());
                                GameCanvas.panel.currentListLength--;
                                GameCanvas.panel.initTabClans();
                            }
                            if (b20 == 2)
                            {
                                Member member3 = new Member();
                                member3.ID = msg.reader().readInt();
                                member3.head = msg.reader().readShort();
                                member3.headICON = msg.reader().readShort();
                                member3.leg = msg.reader().readShort();
                                member3.body = msg.reader().readShort();
                                member3.name = msg.reader().readUTF();
                                member3.role = msg.reader().readByte();
                                member3.powerPoint = msg.reader().readUTF();
                                member3.donate = msg.reader().readInt();
                                member3.receive_donate = msg.reader().readInt();
                                member3.clanPoint = msg.reader().readInt();
                                member3.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                for (int num34 = 0; num34 < GameCanvas.panel.myMember.size(); num34++)
                                {
                                    Member member4 = (Member)GameCanvas.panel.myMember.elementAt(num34);
                                    if (member4.ID == member3.ID)
                                    {
                                        if (Char.myCharz().charID == member3.ID)
                                        {
                                            Char.myCharz().role = member3.role;
                                        }
                                        Member o = member3;
                                        GameCanvas.panel.myMember.removeElement(member4);
                                        GameCanvas.panel.myMember.insertElementAt(o, num34);
                                        return true;
                                    }
                                }
                            }
                            Res.outz("=>>>>>>>>>>>>>>>>>>>>>> -52  MY CLAN UPDSTE");
                            return true;
                        }

                    case -50:
                        {
                            InfoDlg.hide();
                            GameCanvas.panel.member = new MyVector();
                            sbyte b17 = msg.reader().readByte();
                            for (int num26 = 0; num26 < b17; num26++)
                            {
                                Member member = new Member();
                                member.ID = msg.reader().readInt();
                                member.head = msg.reader().readShort();
                                member.headICON = msg.reader().readShort();
                                member.leg = msg.reader().readShort();
                                member.body = msg.reader().readShort();
                                member.name = msg.reader().readUTF();
                                member.role = msg.reader().readByte();
                                member.powerPoint = msg.reader().readUTF();
                                member.donate = msg.reader().readInt();
                                member.receive_donate = msg.reader().readInt();
                                member.clanPoint = msg.reader().readInt();
                                member.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                GameCanvas.panel.member.addElement(member);
                            }
                            GameCanvas.panel.isViewMember = true;
                            GameCanvas.panel.isSearchClan = false;
                            GameCanvas.panel.isMessage = false;
                            GameCanvas.panel.currentListLength = GameCanvas.panel.member.size() + 2;
                            GameCanvas.panel.initTabClans();
                            return true;
                        }

                    case -47:
                        {
                            InfoDlg.hide();
                            sbyte b10 = msg.reader().readByte();
                            Res.outz("clan = " + b10);
                            if (b10 == 0)
                            {
                                GameCanvas.panel.clanReport = mResources.cannot_find_clan;
                                GameCanvas.panel.clans = null;
                            }
                            else
                            {
                                GameCanvas.panel.clans = new Clan[b10];
                                Res.outz("clan search lent= " + GameCanvas.panel.clans.Length);
                                for (int k = 0; k < GameCanvas.panel.clans.Length; k++)
                                {
                                    GameCanvas.panel.clans[k] = new Clan();
                                    GameCanvas.panel.clans[k].ID = msg.reader().readInt();
                                    GameCanvas.panel.clans[k].name = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].slogan = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].imgID = msg.reader().readUnsignedByte();
                                    GameCanvas.panel.clans[k].powerPoint = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].leaderName = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].currMember = msg.reader().readUnsignedByte();
                                    GameCanvas.panel.clans[k].maxMember = msg.reader().readUnsignedByte();
                                    GameCanvas.panel.clans[k].date = msg.reader().readInt();
                                }
                            }
                            GameCanvas.panel.isSearchClan = true;
                            GameCanvas.panel.isViewMember = false;
                            GameCanvas.panel.isMessage = false;
                            if (GameCanvas.panel.isSearchClan)
                            {
                                GameCanvas.panel.initTabClans();
                            }
                            return true;
                        }

                    case -46:
                        {
                            InfoDlg.hide();
                            sbyte b65 = msg.reader().readByte();
                            if (b65 == 1 || b65 == 3)
                            {
                                GameCanvas.endDlg();
                                ClanImage.vClanImage.removeAllElements();
                                int num161 = msg.reader().readUnsignedByte();
                                for (int num162 = 0; num162 < num161; num162++)
                                {
                                    ClanImage clanImage3 = new ClanImage();
                                    clanImage3.ID = msg.reader().readUnsignedByte();
                                    clanImage3.name = msg.reader().readUTF();
                                    clanImage3.xu = msg.reader().readInt();
                                    clanImage3.luong = msg.reader().readInt();
                                    if (!ClanImage.isExistClanImage(clanImage3.ID))
                                    {
                                        ClanImage.addClanImage(clanImage3);
                                        continue;
                                    }
                                    ClanImage.getClanImage((short)clanImage3.ID).name = clanImage3.name;
                                    ClanImage.getClanImage((short)clanImage3.ID).xu = clanImage3.xu;
                                    ClanImage.getClanImage((short)clanImage3.ID).luong = clanImage3.luong;
                                }
                                if (Char.myCharz().clan != null)
                                {
                                    GameCanvas.panel.changeIcon();
                                }
                            }
                            if (b65 == 4)
                            {
                                Char.myCharz().clan.imgID = msg.reader().readUnsignedByte();
                                Char.myCharz().clan.slogan = msg.reader().readUTF();
                            }
                            return true;
                        }
                }
            }
            catch (Exception ex)
            {
                Cout.println("Loi ClanPacketHandler: " + ex.ToString());
                UnityEngine.Debug.LogError($"[ClanPacketHandler] Error cmd={msg.command}: {ex.Message}\n{ex.StackTrace}");
            }
            return false;
        }
    }
}

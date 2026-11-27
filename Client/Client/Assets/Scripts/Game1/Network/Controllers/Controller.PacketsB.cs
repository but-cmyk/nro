namespace Game1
{
    using Game1.Assets.src.e;
    using Assets.src.f;
    using Game1.Assets.src.g;
    using System;
    using UnityEngine;

    public partial class Controller : IMessageHandler
    {
        private bool handlePacketGroupB(Message msg, ref Char @char, ref Mob mob, MyVector myVector, ref int num)
        {
            switch (msg.command)
            {
                    case -43:
                        {
                            sbyte itemAction = msg.reader().readByte();
                            sbyte where = msg.reader().readByte();
                            sbyte index = msg.reader().readByte();
                            string info = msg.reader().readUTF();
                            GameCanvas.panel.itemRequest(itemAction, info, where, index);
                            break;
                        }
                    case -59:
                        {
                            sbyte typePK = msg.reader().readByte();
                            GameScr.gI().player_vs_player(msg.reader().readInt(), msg.reader().readInt(), msg.reader().readUTF(), typePK);
                            break;
                        }
                    case -62:
                        {
                            int num51 = msg.reader().readUnsignedByte();
                            sbyte b26 = msg.reader().readByte();
                            if (b26 <= 0)
                            {
                                break;
                            }
                            ClanImage clanImage2 = ClanImage.getClanImage((short)num51);
                            if (clanImage2 == null)
                            {
                                clanImage2 = new ClanImage();
                                clanImage2.ID = num51;
                                ClanImage.vClanImage.addElement(clanImage2);
                            }
                            clanImage2.idImage = new short[b26];
                            for (int num52 = 0; num52 < b26; num52++)
                            {
                                clanImage2.idImage[num52] = msg.reader().readShort();
                                if (clanImage2.idImage[num52] > 0)
                                {
                                    SmallImage.vKeys.addElement(clanImage2.idImage[num52] + string.Empty);
                                }
                            }
                            break;
                        }
                    case -65:
                        {
                            InfoDlg.hide();
                            int num114 = msg.reader().readInt();
                            sbyte b41 = msg.reader().readByte();
                            if (b41 == 0)
                            {
                                break;
                            }
                            if (Char.myCharz().charID == num114)
                            {
                                isStopReadMessage = true;
                                GameScr.lockTick = 500;
                                GameScr.gI().center = null;
                                if (b41 == 0 || b41 == 1 || b41 == 3)
                                {
                                    Teleport p = new Teleport(Char.myCharz().cx, Char.myCharz().cy, Char.myCharz().head, Char.myCharz().cdir, 0, true, (b41 != 1) ? b41 : Char.myCharz().cgender);
                                    Teleport.addTeleport(p);
                                }
                                if (b41 == 2)
                                {
                                    GameScr.lockTick = 50;
                                    Char.myCharz().hide();
                                }
                            }
                            else
                            {
                                Char char7 = GameScr.findCharInMap(num114);
                                if ((b41 == 0 || b41 == 1 || b41 == 3) && char7 != null)
                                {
                                    char7.isUsePlane = true;
                                    Teleport teleport = new Teleport(char7.cx, char7.cy, char7.head, char7.cdir, 0, false, (b41 != 1) ? b41 : char7.cgender);
                                    teleport.id = num114;
                                    Teleport.addTeleport(teleport);
                                }
                                if (b41 == 2)
                                {
                                    char7.hide();
                                }
                            }
                            break;
                        }
                    case -64:
                        {
                            int num95 = msg.reader().readInt();
                            int num96 = msg.reader().readUnsignedByte();
                            @char = null;
                            @char = ((num95 != Char.myCharz().charID) ? GameScr.findCharInMap(num95) : Char.myCharz());
                            if (@char == null)
                            {
                                return true;
                            }
                            @char.bag = num96;
                            for (int num97 = 0; num97 < 54; num97++)
                            {
                                @char.removeEffChar(0, 201 + num97);
                            }
                            if (@char.bag >= 201 && @char.bag < 255)
                            {
                                Effect effect = new Effect(@char.bag, @char, 2, -1, 10, 1);
                                effect.typeEff = 5;
                                @char.addEffChar(effect);
                            }
                            Res.outz("cmd:-64 UPDATE BAG PLAER = " + ((@char != null) ? @char.cName : string.Empty) + num95 + " BAG ID= " + num96);
                            if (num96 == 30 && @char.me)
                            {
                                GameScr.isPickNgocRong = true;
                            }
                            break;
                        }
                    case -63:
                        {
                            Res.outz("GET BAG");
                            int num16 = msg.reader().readUnsignedByte();
                            sbyte b9 = msg.reader().readByte();
                            ClanImage clanImage = new ClanImage();
                            clanImage.ID = num16;
                            if (b9 > 0)
                            {
                                clanImage.idImage = new short[b9];
                                for (int j = 0; j < b9; j++)
                                {
                                    clanImage.idImage[j] = msg.reader().readShort();
                                    Res.outz("ID=  " + num16 + " frame= " + clanImage.idImage[j]);
                                }
                                ClanImage.idImages.put(num16 + string.Empty, clanImage);
                            }
                            break;
                        }
                    case -57:
                        {
                            string strInvite = msg.reader().readUTF();
                            int clanID = msg.reader().readInt();
                            int code = msg.reader().readInt();
                            GameScr.gI().clanInvite(strInvite, clanID, code);
                            break;
                        }
                    case -61:
                        {
                            int num131 = msg.reader().readInt();
                            if (num131 != Char.myCharz().charID)
                            {
                                if (GameScr.findCharInMap(num131) != null)
                                {
                                    GameScr.findCharInMap(num131).clanID = msg.reader().readInt();
                                    if (GameScr.findCharInMap(num131).clanID == -2)
                                    {
                                        GameScr.findCharInMap(num131).isCopy = true;
                                    }
                                }
                            }
                            else if (Char.myCharz().clan != null)
                            {
                                Char.myCharz().clan.ID = msg.reader().readInt();
                            }
                            break;
                        }
                    case -42:
                        Char.myCharz().cHPGoc = msg.readInt3Byte();
                        Char.myCharz().cMPGoc = msg.readInt3Byte();
                        Char.myCharz().cDamGoc = msg.reader().readInt();
                        Char.myCharz().cHPFull = msg.readInt3Byte();
                        Char.myCharz().cMPFull = msg.readInt3Byte();
                        Char.myCharz().cHP = msg.readInt3Byte();
                        Char.myCharz().cMP = msg.readInt3Byte();
                        Char.myCharz().cspeed = msg.reader().readByte();
                        Char.myCharz().hpFrom1000TiemNang = msg.reader().readByte();
                        Char.myCharz().mpFrom1000TiemNang = msg.reader().readByte();
                        Char.myCharz().damFrom1000TiemNang = msg.reader().readByte();
                        Char.myCharz().cDamFull = msg.reader().readInt();
                        Char.myCharz().cDefull = msg.reader().readInt();
                        Char.myCharz().cCriticalFull = msg.reader().readByte();
                        Char.myCharz().cTiemNang = msg.reader().readLong();
                        Char.myCharz().expForOneAdd = msg.reader().readShort();
                        Char.myCharz().cDefGoc = msg.reader().readShort();
                        Char.myCharz().cCriticalGoc = msg.reader().readByte();
                        try
                        {
                            if (msg.reader().available() >= 8)
                            {
                                Char.myCharz().cPower = msg.reader().readLong();
                                Char.myCharz().applyCharLevelPercent();
                            }
                        }
                        catch (Exception catchErr_1)
                        {
                            Cout.LogWarning("[Controller.PacketsB.cs] " + catchErr_1.Message);
                        }
                        InfoDlg.hide();
                        break;
                    case 1:
                        {
                            bool flag9 = msg.reader().readBool();
                            Res.outz("isRes= " + flag9);
                            if (!flag9)
                            {
                                GameCanvas.startOKDlg(msg.reader().readUTF());
                                break;
                            }
                            GameCanvas.loginScr.isLogin2 = false;
                            Rms.saveRMSString("userAo" + ServerListScreen.ipSelect, string.Empty);
                            GameCanvas.endDlg();
                            GameCanvas.loginScr.doLogin();
                            break;
                        }
                    case 2:
                        Char.isLoadingMap = false;
                        LoginScr.isLoggingIn = false;
                        if (!GameScr.isLoadAllData)
                        {
                            GameScr.gI().initSelectChar();
                        }
                        BgItem.clearHashTable();
                        GameCanvas.endDlg();
                        CreateCharScr.isCreateChar = true;
                        CreateCharScr.gI().switchToMe();
                        return true;
                    case -107:
                        {
                            sbyte b37 = msg.reader().readByte();
                            if (b37 == 0)
                            {
                                Char.myCharz().havePet = false;
                            }
                            if (b37 == 1)
                            {
                                Char.myCharz().havePet = true;
                            }
                            if (b37 != 2)
                            {
                                break;
                            }
                            InfoDlg.hide();
                            Char.myPetz().head = msg.reader().readShort();
                            Char.myPetz().setDefaultPart();
                            int num98 = msg.reader().readUnsignedByte();
                            Res.outz("num body = " + num98);
                            Char.myPetz().arrItemBody = new Item[num98];
                            for (int num99 = 0; num99 < num98; num99++)
                            {
                                short num100 = msg.reader().readShort();
                                Res.outz("template id= " + num100);
                                if (num100 == -1)
                                {
                                    continue;
                                }
                                Res.outz("1");
                                Char.myPetz().arrItemBody[num99] = new Item();
                                Char.myPetz().arrItemBody[num99].template = ItemTemplates.get(num100);
                                int num101 = Char.myPetz().arrItemBody[num99].template.type;
                                Char.myPetz().arrItemBody[num99].quantity = msg.reader().readInt();
                                Res.outz("3");
                                Char.myPetz().arrItemBody[num99].info = msg.reader().readUTF();
                                Char.myPetz().arrItemBody[num99].content = msg.reader().readUTF();
                                int num102 = msg.reader().readUnsignedByte();
                                Res.outz("option size= " + num102);
                                if (num102 != 0)
                                {
                                    Char.myPetz().arrItemBody[num99].itemOption = new ItemOption[num102];
                                    for (int num103 = 0; num103 < Char.myPetz().arrItemBody[num99].itemOption.Length; num103++)
                                    {
                                        int num104 = msg.reader().readUnsignedByte();
                                        int param5 = msg.reader().readUnsignedShort();
                                        if (num104 != -1)
                                        {
                                            Char.myPetz().arrItemBody[num99].itemOption[num103] = new ItemOption(num104, param5);
                                        }
                                    }
                                }
                                switch (num101)
                                {
                                    case 0:
                                        Char.myPetz().body = Char.myPetz().arrItemBody[num99].template.part;
                                        break;
                                    case 1:
                                        Char.myPetz().leg = Char.myPetz().arrItemBody[num99].template.part;
                                        break;
                                }
                            }
                            Char.myPetz().cHP = msg.readInt3Byte();
                            Char.myPetz().cHPFull = msg.readInt3Byte();
                            Char.myPetz().cMP = msg.readInt3Byte();
                            Char.myPetz().cMPFull = msg.readInt3Byte();
                            Char.myPetz().cDamFull = msg.readInt3Byte();
                            Char.myPetz().cName = msg.reader().readUTF();
                            Char.myPetz().currStrLevel = msg.reader().readUTF();
                            Char.myPetz().cPower = msg.reader().readLong();
                            Char.myPetz().cTiemNang = msg.reader().readLong();
                            Char.myPetz().petStatus = msg.reader().readByte();
                            Char.myPetz().cStamina = msg.reader().readShort();
                            Char.myPetz().cMaxStamina = msg.reader().readShort();
                            Char.myPetz().cCriticalFull = msg.reader().readByte();
                            Char.myPetz().cDefull = msg.reader().readShort();
                            Char.myPetz().arrPetSkill = new Skill[msg.reader().readByte()];
                            Res.outz("SKILLENT = " + Char.myPetz().arrPetSkill);
                            for (int num105 = 0; num105 < Char.myPetz().arrPetSkill.Length; num105++)
                            {
                                short num106 = msg.reader().readShort();
                                if (num106 != -1)
                                {
                                    Char.myPetz().arrPetSkill[num105] = Skills.get(num106);
                                    continue;
                                }
                                Char.myPetz().arrPetSkill[num105] = new Skill();
                                Char.myPetz().arrPetSkill[num105].template = null;
                                Char.myPetz().arrPetSkill[num105].moreInfo = msg.reader().readUTF();
                            }
    
                            break;
                        }
                    case -37:
                        {
                            sbyte b27 = msg.reader().readByte();
                            Res.outz("cAction= " + b27);
                            if (b27 != 0)
                            {
                                break;
                            }
                            Char.myCharz().head = msg.reader().readShort();
                            Char.myCharz().setDefaultPart();
                            int num53 = msg.reader().readUnsignedByte();
                            Res.outz("num body = " + num53);
                            Char.myCharz().arrItemBody = new Item[num53];
                            for (int num54 = 0; num54 < num53; num54++)
                            {
                                short num55 = msg.reader().readShort();
                                if (num55 == -1)
                                {
                                    continue;
                                }
                                Char.myCharz().arrItemBody[num54] = new Item();
                                ItemTemplate itBody = ItemTemplates.get(num55);
                                Char.myCharz().arrItemBody[num54].template = itBody;
                                int num56 = (itBody != null) ? itBody.type : 0;
                                Char.myCharz().arrItemBody[num54].quantity = msg.reader().readInt();
                                Char.myCharz().arrItemBody[num54].info = msg.reader().readUTF();
                                Char.myCharz().arrItemBody[num54].content = msg.reader().readUTF();
                                int num57 = msg.reader().readUnsignedByte();
                                if (num57 != 0)
                                {
                                    Char.myCharz().arrItemBody[num54].itemOption = new ItemOption[num57];
                                    for (int num58 = 0; num58 < Char.myCharz().arrItemBody[num54].itemOption.Length; num58++)
                                    {
                                        int num59 = msg.reader().readUnsignedByte();
                                        int param3 = msg.reader().readUnsignedShort();
                                        if (num59 != -1)
                                        {
                                            Char.myCharz().arrItemBody[num54].itemOption[num58] = new ItemOption(num59, param3);
                                        }
                                    }
                                }
                                switch (num56)
                                {
                                    case 0:
                                        if (itBody != null)
                                        {
                                            Char.myCharz().body = itBody.part;
                                        }
                                        break;
                                    case 1:
                                        if (itBody != null)
                                        {
                                            Char.myCharz().leg = itBody.part;
                                        }
                                        break;
                                }
                            }
                            break;
                        }
                    case -36:
                        {
                            sbyte b18 = msg.reader().readByte();
                            Res.outz("cAction= " + b18);
                            GameScr.isudungCapsun4 = false;
                            GameScr.isudungCapsun3 = false;
                            if (b18 == 0)
                            {
                                int num27 = msg.reader().readUnsignedByte();
                                Char.myCharz().arrItemBag = new Item[num27];
                                GameScr.hpPotion = 0;
                                Res.outz("numC=" + num27);
                                for (int num28 = 0; num28 < num27; num28++)
                                {
                                    short num29 = msg.reader().readShort();
                                    if (num29 == -1)
                                    {
                                        continue;
                                    }
                                    Char.myCharz().arrItemBag[num28] = new Item();
                                    Char.myCharz().arrItemBag[num28].template = ItemTemplates.get(num29);
                                    Char.myCharz().arrItemBag[num28].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBag[num28].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBag[num28].content = msg.reader().readUTF();
                                    Char.myCharz().arrItemBag[num28].indexUI = num28;
                                    int num30 = msg.reader().readUnsignedByte();
                                    if (num30 != 0)
                                    {
                                        Char.myCharz().arrItemBag[num28].itemOption = new ItemOption[num30];
                                        for (int num31 = 0; num31 < Char.myCharz().arrItemBag[num28].itemOption.Length; num31++)
                                        {
                                            int num32 = msg.reader().readUnsignedByte();
                                            int param2 = msg.reader().readUnsignedShort();
                                            if (num32 != -1)
                                            {
                                                Char.myCharz().arrItemBag[num28].itemOption[num31] = new ItemOption(num32, param2);
                                            }
                                        }
                                        Char.myCharz().arrItemBag[num28].compare = GameCanvas.panel.getCompare(Char.myCharz().arrItemBag[num28]);
                                    }
                                    if (Char.myCharz().arrItemBag[num28].template.type == 11)
                                    {
                                    }
                                    if (Char.myCharz().arrItemBag[num28].template.type == 6)
                                    {
                                        GameScr.hpPotion += Char.myCharz().arrItemBag[num28].quantity;
                                    }
                                    if (Char.myCharz().arrItemBag[num28].template.id == 194)
                                    {
                                        GameScr.isudungCapsun4 = Char.myCharz().arrItemBag[num28].quantity > 0;
                                    }
                                    else if (Char.myCharz().arrItemBag[num28].template.id == 193 && !GameScr.isudungCapsun4)
                                    {
                                        GameScr.isudungCapsun3 = Char.myCharz().arrItemBag[num28].quantity > 0;
                                    }
                                }
                            }
                            if (b18 == 2)
                            {
                                sbyte b19 = msg.reader().readByte();
                                int num33 = msg.reader().readInt();
                                int quantity2 = Char.myCharz().arrItemBag[b19].quantity;
                                int id = Char.myCharz().arrItemBag[b19].template.id;
                                Char.myCharz().arrItemBag[b19].quantity = num33;
                                if (Char.myCharz().arrItemBag[b19].quantity < quantity2 && Char.myCharz().arrItemBag[b19].template.type == 6)
                                {
                                    GameScr.hpPotion -= quantity2 - Char.myCharz().arrItemBag[b19].quantity;
                                }
                                if (Char.myCharz().arrItemBag[b19].quantity == 0)
                                {
                                    Char.myCharz().arrItemBag[b19] = null;
                                }
                                switch (id)
                                {
                                    case 194:
                                        GameScr.isudungCapsun4 = num33 > 0;
                                        break;
                                    case 193:
                                        GameScr.isudungCapsun3 = num33 > 0;
                                        break;
                                }
                            }
                            break;
                        }
                    case -35:
                        {
                            sbyte b12 = msg.reader().readByte();
                            Res.outz("cAction= " + b12);
                            if (b12 == 0)
                            {
                                int num20 = msg.reader().readUnsignedByte();
                                Char.myCharz().arrItemBox = new Item[num20];
                                GameCanvas.panel.hasUse = 0;
                                for (int m = 0; m < num20; m++)
                                {
                                    short num21 = msg.reader().readShort();
                                    if (num21 == -1)
                                    {
                                        continue;
                                    }
                                    Char.myCharz().arrItemBox[m] = new Item();
                                    Char.myCharz().arrItemBox[m].template = ItemTemplates.get(num21);
                                    Char.myCharz().arrItemBox[m].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBox[m].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBox[m].content = msg.reader().readUTF();
                                    int num22 = msg.reader().readUnsignedByte();
                                    if (num22 != 0)
                                    {
                                        Char.myCharz().arrItemBox[m].itemOption = new ItemOption[num22];
                                        for (int n = 0; n < Char.myCharz().arrItemBox[m].itemOption.Length; n++)
                                        {
                                            int num23 = msg.reader().readUnsignedByte();
                                            int param = msg.reader().readUnsignedShort();
                                            if (num23 != -1)
                                            {
                                                Char.myCharz().arrItemBox[m].itemOption[n] = new ItemOption(num23, param);
                                            }
                                        }
                                    }
                                    GameCanvas.panel.hasUse++;
                                }
                            }
                            if (b12 == 1)
                            {
                                bool isBoxClan = false;
                                try
                                {
                                    sbyte b13 = msg.reader().readByte();
                                    if (b13 == 1)
                                    {
                                        isBoxClan = true;
                                    }
                                }
                                catch (Exception catchErr_2)
                                {
                                    Cout.LogWarning("[Controller.PacketsB.cs] " + catchErr_2.Message);
                                }
                                GameCanvas.panel.setTypeBox();
                                GameCanvas.panel.isBoxClan = isBoxClan;
                                GameCanvas.panel.show();
                            }
                            if (b12 == 2)
                            {
                                sbyte b14 = msg.reader().readByte();
                                int quantity = msg.reader().readInt();
                                Char.myCharz().arrItemBox[b14].quantity = quantity;
                                if (Char.myCharz().arrItemBox[b14].quantity == 0)
                                {
                                    Char.myCharz().arrItemBox[b14] = null;
                                }
                            }
                            break;
                        }
                    case -45:
                        {
                            sbyte b51 = msg.reader().readByte();
                            int num135 = msg.reader().readInt();
                            short num136 = msg.reader().readShort();
                            Res.outz(">.SKILL_NOT_FOCUS      skillNotFocusID: " + num136 + " skill type= " + b51 + "   player use= " + num135);
                            if (b51 == 20)
                            {
                                sbyte b52 = msg.reader().readByte();
                                sbyte dir = msg.reader().readByte();
                                short timeGong = msg.reader().readShort();
                                bool isFly = ((msg.reader().readByte() != 0) ? true : false);
                                sbyte typePaint = msg.reader().readByte();
                                sbyte typeItem = -1;
                                try
                                {
                                    typeItem = msg.reader().readByte();
                                }
                                catch (Exception catchErr_3)
                                {
                                    Cout.LogWarning("[Controller.PacketsB.cs] " + catchErr_3.Message);
                                }
                                Res.outz(">.SKILL_NOT_FOCUS  skill typeFrame= " + b52);
                                @char = ((Char.myCharz().charID != num135) ? GameScr.findCharInMap(num135) : Char.myCharz());
                                @char.SetSkillPaint_NEW(num136, isFly, b52, typePaint, dir, timeGong, typeItem);
                            }
                            if (b51 == 21)
                            {
                                Point point = new Point();
                                point.x = msg.reader().readShort();
                                point.y = msg.reader().readShort();
                                short timeDame = msg.reader().readShort();
                                short rangeDame = msg.reader().readShort();
                                sbyte typePaint2 = 0;
                                sbyte typeItem2 = -1;
                                Point[] array10 = null;
                                @char = ((Char.myCharz().charID != num135) ? GameScr.findCharInMap(num135) : Char.myCharz());
                                try
                                {
                                    typePaint2 = msg.reader().readByte();
                                    sbyte b53 = msg.reader().readByte();
                                    if (b53 > 0)
                                    {
                                        array10 = new Point[b53];
                                        for (int num137 = 0; num137 < array10.Length; num137++)
                                        {
                                            array10[num137] = new Point();
                                            array10[num137].type = msg.reader().readByte();
                                            if (array10[num137].type == 0)
                                            {
                                                array10[num137].id = msg.reader().readByte();
                                            }
                                            else
                                            {
                                                array10[num137].id = msg.reader().readInt();
                                            }
                                        }
                                    }
                                }
                                catch (Exception catchErr_4)
                                {
                                    Cout.LogWarning("[Controller.PacketsB.cs] " + catchErr_4.Message);
                                }
                                try
                                {
                                    typeItem2 = msg.reader().readByte();
                                }
                                catch (Exception catchErr_5)
                                {
                                    Cout.LogWarning("[Controller.PacketsB.cs] " + catchErr_5.Message);
                                }
                                Res.outz(">.SKILL_NOT_FOCUS  skill targetDame= " + point.x + ":" + point.y + "    c:" + @char.cx + ":" + @char.cy + "   cdir:" + @char.cdir);
                                @char.SetSkillPaint_STT(1, num136, point, timeDame, rangeDame, typePaint2, array10, typeItem2);
                            }
                            if (b51 == 0)
                            {
                                Res.outz("id use= " + num135);
                                if (Char.myCharz().charID != num135)
                                {
                                    @char = GameScr.findCharInMap(num135);
                                    bool isGround = (TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2;
                                    @char.setSkillPaint(GameScr.getSkillPaint(num136), isGround ? 0 : 1);
                                    if (!isGround)
                                    {
                                        @char.delayFall = 20;
                                    }
                                }
                                else
                                {
                                    Char.myCharz().saveLoadPreviousSkill();
                                    Res.outz("LOAD LAST SKILL");
                                }
                                sbyte b54 = msg.reader().readByte();
                                Res.outz("npc size= " + b54);
                                for (int num138 = 0; num138 < b54; num138++)
                                {
                                    sbyte b55 = msg.reader().readByte();
                                    sbyte b56 = msg.reader().readByte();
                                    Res.outz("index= " + b55);
                                    if (num136 >= 42 && num136 <= 48)
                                    {
                                        ((Mob)GameScr.vMob.elementAt(b55)).isFreez = true;
                                        ((Mob)GameScr.vMob.elementAt(b55)).seconds = b56;
                                        ((Mob)GameScr.vMob.elementAt(b55)).last = (((Mob)GameScr.vMob.elementAt(b55)).cur = mSystem.currentTimeMillis());
                                    }
                                }
                                sbyte b57 = msg.reader().readByte();
                                for (int num139 = 0; num139 < b57; num139++)
                                {
                                    int num140 = msg.reader().readInt();
                                    sbyte b58 = msg.reader().readByte();
                                    Res.outz("player ID= " + num140 + " my ID= " + Char.myCharz().charID);
                                    if (num136 < 42 || num136 > 48)
                                    {
                                        continue;
                                    }
                                    if (num140 == Char.myCharz().charID)
                                    {
                                        if (!Char.myCharz().isFlyAndCharge && !Char.myCharz().isStandAndCharge)
                                        {
                                            GameScr.gI().isFreez = true;
                                            Char.myCharz().isFreez = true;
                                            Char.myCharz().freezSeconds = b58;
                                            Char.myCharz().lastFreez = (Char.myCharz().currFreez = mSystem.currentTimeMillis());
                                            Char.myCharz().isLockMove = true;
                                        }
                                    }
                                    else
                                    {
                                        @char = GameScr.findCharInMap(num140);
                                        if (@char != null && !@char.isFlyAndCharge && !@char.isStandAndCharge)
                                        {
                                            @char.isFreez = true;
                                            @char.seconds = b58;
                                            @char.freezSeconds = b58;
                                            @char.lastFreez = (GameScr.findCharInMap(num140).currFreez = mSystem.currentTimeMillis());
                                        }
                                    }
                                }
                            }
                            if (b51 == 1 && num135 != Char.myCharz().charID)
                            {
                                Char targetChar = GameScr.findCharInMap(num135);
                                if (targetChar != null)
                                {
                                    targetChar.isCharge = true;
                                }
                                else
                                {
                                    Cout.LogWarning($"[Controller.onMessage] Không tìm thấy Char ID {num135} trên Map để bật isCharge");
                                }
                            }
                            if (b51 == 3)
                            {
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().isCharge = false;
                                    SoundMn.gI().taitaoPause();
                                    Char.myCharz().saveLoadPreviousSkill();
                                }
                                else
                                {
                                    Char targetChar = GameScr.findCharInMap(num135);
                                    if (targetChar != null)
                                    {
                                        targetChar.isCharge = false;
                                    }
                                }
                            }
                            if (b51 == 4)
                            {
                                short sec4 = (short)(msg.reader().readShort() - 1000);
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().seconds = sec4;
                                    Char.myCharz().last = mSystem.currentTimeMillis();
                                    Res.outz("second= " + Char.myCharz().seconds + " last= " + Char.myCharz().last);
                                }
                                else
                                {
                                    Char targetChar = GameScr.findCharInMap(num135);
                                    short secRemain = msg.reader().readShort();
                                    if (targetChar != null)
                                    {
                                        switch (targetChar.cgender)
                                        {
                                            case 0:
                                                if (TileMap.mapID != 170)
                                                {
                                                    targetChar.useChargeSkill(false);
                                                    break;
                                                }
                                                if (num136 >= 77 && num136 <= 83)
                                                {
                                                    targetChar.useChargeSkill(true);
                                                }
                                                if (num136 >= 70 && num136 <= 76)
                                                {
                                                    targetChar.useChargeSkill(false);
                                                }
                                                break;
                                            case 1:
                                                {
                                                    if (TileMap.mapID != 170)
                                                    {
                                                        targetChar.useChargeSkill(true);
                                                        break;
                                                    }
                                                    bool isGround2 = true;
                                                    if (num136 >= 70 && num136 <= 76)
                                                    {
                                                        isGround2 = false;
                                                    }
                                                    if (num136 >= 77 && num136 <= 83)
                                                    {
                                                        isGround2 = true;
                                                    }
                                                    targetChar.useChargeSkill(isGround2);
                                                    break;
                                                }
                                            default:
                                                if (TileMap.mapID == 170)
                                                {
                                                    bool isGround = true;
                                                    if (num136 >= 70 && num136 <= 76)
                                                    {
                                                        isGround = false;
                                                    }
                                                    if (num136 >= 77 && num136 <= 83)
                                                    {
                                                        isGround = true;
                                                    }
                                                    targetChar.useChargeSkill(isGround);
                                                }
                                                break;
                                        }
                                        targetChar.skillTemplateId = num136;
                                        if (num136 >= 70 && num136 <= 76)
                                        {
                                            targetChar.isUseSkillAfterCharge = true;
                                        }
                                        targetChar.seconds = secRemain;
                                        targetChar.last = mSystem.currentTimeMillis();
                                    }
                                }
                            }
                            if (b51 == 5)
                            {
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().stopUseChargeSkill();
                                }
                                else
                                {
                                    Char targetChar = GameScr.findCharInMap(num135);
                                    if (targetChar != null)
                                    {
                                        targetChar.stopUseChargeSkill();
                                    }
                                }
                            }
                            if (b51 == 6)
                            {
                                SkillPaint sp = (GameScr.sks != null && num136 >= 0 && num136 < GameScr.sks.Length) ? GameScr.sks[num136] : null;
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().setAutoSkillPaint(sp, 0);
                                }
                                else
                                {
                                    Char targetChar = GameScr.findCharInMap(num135);
                                    if (targetChar != null)
                                    {
                                        targetChar.setAutoSkillPaint(sp, 0);
                                        SoundMn.gI().gong();
                                    }
                                }
                            }
                            if (b51 == 7)
                            {
                                short sec7 = msg.reader().readShort();
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().seconds = sec7;
                                    Res.outz("second = " + Char.myCharz().seconds);
                                    Char.myCharz().last = mSystem.currentTimeMillis();
                                }
                                else
                                {
                                    Char targetChar = GameScr.findCharInMap(num135);
                                    if (targetChar != null)
                                    {
                                        targetChar.useChargeSkill(true);
                                        targetChar.seconds = sec7;
                                        targetChar.last = mSystem.currentTimeMillis();
                                        SoundMn.gI().gong();
                                    }
                                }
                            }
                            if (b51 == 8 && num135 != Char.myCharz().charID)
                            {
                                Char targetChar = GameScr.findCharInMap(num135);
                                if (targetChar != null)
                                {
                                    SkillPaint sp = (GameScr.sks != null && num136 >= 0 && num136 < GameScr.sks.Length) ? GameScr.sks[num136] : null;
                                    targetChar.setAutoSkillPaint(sp, 0);
                                }
                            }
                            break;
                        }
                    case -44:
                        {
                            bool flag6 = false;
                            if (GameCanvas.w > 2 * Panel.WIDTH_PANEL)
                            {
                                flag6 = true;
                            }
                            sbyte b33 = msg.reader().readByte();
                            int num78 = msg.reader().readUnsignedByte();
                            Char.myCharz().arrItemShop = new Item[num78][];
                            GameCanvas.panel.shopTabName = new string[num78 + ((!flag6) ? 1 : 0)][];
                            for (int num79 = 0; num79 < GameCanvas.panel.shopTabName.Length; num79++)
                            {
                                GameCanvas.panel.shopTabName[num79] = new string[2];
                            }
                            if (b33 == 2)
                            {
                                GameCanvas.panel.maxPageShop = new int[num78];
                                GameCanvas.panel.currPageShop = new int[num78];
                            }
                            if (!flag6)
                            {
                                GameCanvas.panel.shopTabName[num78] = mResources.inventory;
                            }
                            for (int num80 = 0; num80 < num78; num80++)
                            {
                                string[] array5 = Res.split(msg.reader().readUTF(), "\n", 0);
                                if (b33 == 2)
                                {
                                    GameCanvas.panel.maxPageShop[num80] = msg.reader().readUnsignedByte();
                                }
                                if (array5.Length == 2)
                                {
                                    GameCanvas.panel.shopTabName[num80] = array5;
                                }
                                if (array5.Length == 1)
                                {
                                    GameCanvas.panel.shopTabName[num80][0] = array5[0];
                                    GameCanvas.panel.shopTabName[num80][1] = string.Empty;
                                }
                                int num81 = msg.reader().readUnsignedByte();
                                Char.myCharz().arrItemShop[num80] = new Item[num81];
                                Panel.strWantToBuy = mResources.say_wat_do_u_want_to_buy;
                                if (b33 == 1)
                                {
                                    Panel.strWantToBuy = mResources.say_wat_do_u_want_to_buy2;
                                }
                                for (int num82 = 0; num82 < num81; num82++)
                                {
                                    short num83 = msg.reader().readShort();
                                    if (num83 == -1)
                                    {
                                        continue;
                                    }
                                    Char.myCharz().arrItemShop[num80][num82] = new Item();
                                    Char.myCharz().arrItemShop[num80][num82].template = ItemTemplates.get(num83);
                                    Res.outz("name " + num80 + " = " + Char.myCharz().arrItemShop[num80][num82].template.name + " id templat= " + Char.myCharz().arrItemShop[num80][num82].template.id);
                                    if (b33 == 8)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].buyCoin = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyGold = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].quantity = msg.reader().readInt();
                                    }
                                    else if (b33 == 4)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].reason = msg.reader().readUTF();
                                    }
                                    else if (b33 == 0)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].buyCoin = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyGold = msg.reader().readInt();
                                    }
                                    else if (b33 == 1)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].powerRequire = msg.reader().readLong();
                                    }
                                    else if (b33 == 2)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].itemId = msg.reader().readShort();
                                        Char.myCharz().arrItemShop[num80][num82].buyCoin = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyGold = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyType = msg.reader().readByte();
                                        Char.myCharz().arrItemShop[num80][num82].quantity = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].isMe = msg.reader().readByte();
                                    }
                                    else if (b33 == 3)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].isBuySpec = true;
                                        Char.myCharz().arrItemShop[num80][num82].iconSpec = msg.reader().readShort();
                                        Char.myCharz().arrItemShop[num80][num82].buySpec = msg.reader().readInt();
                                    }
                                    int num84 = msg.reader().readUnsignedByte();
                                    if (num84 != 0)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].itemOption = new ItemOption[num84];
                                        for (int num85 = 0; num85 < Char.myCharz().arrItemShop[num80][num82].itemOption.Length; num85++)
                                        {
                                            int num86 = msg.reader().readUnsignedByte();
                                            int param4 = msg.reader().readUnsignedShort();
                                            if (num86 != -1)
                                            {
                                                Char.myCharz().arrItemShop[num80][num82].itemOption[num85] = new ItemOption(num86, param4);
                                                Char.myCharz().arrItemShop[num80][num82].compare = GameCanvas.panel.getCompare(Char.myCharz().arrItemShop[num80][num82]);
                                            }
                                        }
                                    }
                                    sbyte b34 = msg.reader().readByte();
                                    Char.myCharz().arrItemShop[num80][num82].newItem = ((b34 != 0) ? true : false);
                                    sbyte b35 = msg.reader().readByte();
                                    if (b35 == 1)
                                    {
                                        int headTemp = msg.reader().readShort();
                                        int bodyTemp = msg.reader().readShort();
                                        int legTemp = msg.reader().readShort();
                                        int bagTemp = msg.reader().readShort();
                                        Char.myCharz().arrItemShop[num80][num82].setPartTemp(headTemp, bodyTemp, legTemp, bagTemp);
                                    }
                                    if (b33 == 2 && GameMidlet.intVERSION >= 237)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].nameNguoiKyGui = msg.reader().readUTF();
                                        Res.err("nguoi ki gui  " + Char.myCharz().arrItemShop[num80][num82].nameNguoiKyGui);
                                    }
                                }
                            }
                            if (flag6)
                            {
                                if (b33 != 2)
                                {
                                    GameCanvas.panel2 = new Panel();
                                    GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                                    GameCanvas.panel2.setTypeBodyOnly();
                                    GameCanvas.panel2.show();
                                }
                                else
                                {
                                    GameCanvas.panel2 = new Panel();
                                    GameCanvas.panel2.setTypeKiGuiOnly();
                                    GameCanvas.panel2.show();
                                }
                            }
                            GameCanvas.panel.tabName[1] = GameCanvas.panel.shopTabName;
                            if (b33 == 2)
                            {
                                string[][] array6 = GameCanvas.panel.tabName[1];
                                if (flag6)
                                {
                                    GameCanvas.panel.tabName[1] = new string[4][]
                                    {
                                array6[0],
                                array6[1],
                                array6[2],
                                array6[3]
                                    };
                                }
                                else
                                {
                                    GameCanvas.panel.tabName[1] = new string[5][]
                                    {
                                array6[0],
                                array6[1],
                                array6[2],
                                array6[3],
                                array6[4]
                                    };
                                }
                            }
                            GameCanvas.panel.setTypeShop(b33);
                            GameCanvas.panel.show();
                            break;
                        }
                    case -41:
                        {
                            sbyte b28 = msg.reader().readByte();
                            Char.myCharz().strLevel = new string[b28];
                            for (int num60 = 0; num60 < b28; num60++)
                            {
                                string text2 = msg.reader().readUTF();
                                Char.myCharz().strLevel[num60] = text2;
                            }
                            Res.outz("---   xong  level caption cmd : " + msg.command);
                            break;
                        }
                    case -34:
                        {
                            sbyte b15 = msg.reader().readByte();
                            Res.outz("act= " + b15);
                            if (b15 == 0 && GameScr.gI().magicTree != null)
                            {
                                Res.outz("toi duoc day");
                                MagicTree magicTree = GameScr.gI().magicTree;
                                magicTree.id = msg.reader().readShort();
                                magicTree.name = msg.reader().readUTF();
                                magicTree.name = Res.changeString(magicTree.name);
                                magicTree.x = msg.reader().readShort();
                                magicTree.y = msg.reader().readShort();
                                magicTree.level = msg.reader().readByte();
                                magicTree.currPeas = msg.reader().readShort();
                                magicTree.maxPeas = msg.reader().readShort();
                                Res.outz("curr Peas= " + magicTree.currPeas);
                                magicTree.strInfo = msg.reader().readUTF();
                                magicTree.seconds = msg.reader().readInt();
                                magicTree.timeToRecieve = magicTree.seconds;
                                sbyte b16 = msg.reader().readByte();
                                magicTree.peaPostionX = new int[b16];
                                magicTree.peaPostionY = new int[b16];
                                for (int num24 = 0; num24 < b16; num24++)
                                {
                                    magicTree.peaPostionX[num24] = msg.reader().readByte();
                                    magicTree.peaPostionY[num24] = msg.reader().readByte();
                                }
                                magicTree.isUpdate = msg.reader().readBool();
                                magicTree.last = (magicTree.cur = mSystem.currentTimeMillis());
                                GameScr.gI().magicTree.isUpdateTree = true;
                            }
                            if (b15 == 1)
                            {
                                myVector = new MyVector();
                                try
                                {
                                    while (msg.reader().available() > 0)
                                    {
                                        string caption = msg.reader().readUTF();
                                        myVector.addElement(new Command(caption, GameCanvas.instance, 888392, null));
                                    }
                                }
                                catch (Exception ex5)
                                {
                                    Cout.println("Loi MAGIC_TREE " + ex5.ToString());
                                }
                                GameCanvas.menu.startAt(myVector, 3);
                            }
                            if (b15 == 2)
                            {
                                GameScr.gI().magicTree.remainPeas = msg.reader().readShort();
                                GameScr.gI().magicTree.seconds = msg.reader().readInt();
                                GameScr.gI().magicTree.last = (GameScr.gI().magicTree.cur = mSystem.currentTimeMillis());
                                GameScr.gI().magicTree.isUpdateTree = true;
                                GameScr.gI().magicTree.isPeasEffect = true;
                            }
                            break;
                        }
                default:
                    return false;
            }
            return true;
        }
    }
}

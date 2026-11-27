namespace Game1
{
    using Game1.Assets.src.e;
    using Assets.src.f;
    using Game1.Assets.src.g;
    using System;
    using UnityEngine;

    public partial class Controller : IMessageHandler
    {
        public void messageSubCommand(Message msg)
        {
            try
            {
                GameCanvas.debug("SA12", 2);
                sbyte b = msg.reader().readByte();
                switch (b)
                {
                    case 63:
                        {
                            sbyte b5 = msg.reader().readByte();
                            if (b5 > 0)
                            {
                                GameCanvas.panel.vPlayerMenu_id.removeAllElements();
                                InfoDlg.showWait();
                                MyVector vPlayerMenu = GameCanvas.panel.vPlayerMenu;
                                for (int j = 0; j < b5; j++)
                                {
                                    string caption = msg.reader().readUTF();
                                    string caption2 = msg.reader().readUTF();
                                    short num5 = msg.reader().readShort();
                                    GameCanvas.panel.vPlayerMenu_id.addElement(num5 + string.Empty);
                                    Char.myCharz().charFocus.menuSelect = num5;
                                    Command command = new Command(caption, 11115, Char.myCharz().charFocus);
                                    command.caption2 = caption2;
                                    vPlayerMenu.addElement(command);
                                }
                                InfoDlg.hide();
                                GameCanvas.panel.setTabPlayerMenu();
                            }
                            break;
                        }
                    case 1:
                        GameCanvas.debug("SA13", 2);
                        Char.myCharz().nClass = GameScr.nClasss[msg.reader().readByte()];
                        Char.myCharz().cTiemNang = msg.reader().readLong();
                        Char.myCharz().vSkill.removeAllElements();
                        Char.myCharz().vSkillFight.removeAllElements();
                        Char.myCharz().myskill = null;
                        break;
                    case 2:
                        {
                            GameCanvas.debug("SA14", 2);
                            if (Char.myCharz().statusMe != 14 && Char.myCharz().statusMe != 5)
                            {
                                Char.myCharz().cHP = Char.myCharz().cHPFull;
                                Char.myCharz().cMP = Char.myCharz().cMPFull;
                                Cout.LogError2(" ME_LOAD_SKILL");
                            }
                            Char.myCharz().vSkill.removeAllElements();
                            Char.myCharz().vSkillFight.removeAllElements();
                            sbyte b2 = msg.reader().readByte();
                            for (sbyte b3 = 0; b3 < b2; b3++)
                            {
                                short skillId = msg.reader().readShort();
                                Skill skill2 = Skills.get(skillId);
                                useSkill(skill2);
                            }
                            GameScr.gI().sortSkill();
                            if (GameScr.isPaintInfoMe)
                            {
                                GameScr.indexRow = -1;
                                GameScr.gI().left = (GameScr.gI().center = null);
                            }
                            break;
                        }
                    case 19:
                        GameCanvas.debug("SA17", 2);
                        Char.myCharz().boxSort();
                        break;
                    case 21:
                        {
                            GameCanvas.debug("SA19", 2);
                            int num3 = msg.reader().readInt();
                            Char.myCharz().xuInBox -= num3;
                            Char.myCharz().xu += num3;
                            Char.myCharz().xuStr = mSystem.numberTostring(Char.myCharz().xu);
                            break;
                        }
                    case 0:
                        {
                            GameCanvas.debug("SA21", 2);
                            RadarScr.list = new MyVector();
                            Teleport.vTeleport.removeAllElements();
                            GameScr.vCharInMap.removeAllElements();
                            GameScr.vItemMap.removeAllElements();
                            Char.vItemTime.removeAllElements();
                            GameScr.loadImg();
                            GameScr.currentCharViewInfo = Char.myCharz();
                            Char.myCharz().charID = msg.reader().readInt();
                            Char.myCharz().ctaskId = msg.reader().readByte();
                            Char.myCharz().cgender = msg.reader().readByte();
                            Char.myCharz().head = msg.reader().readShort();
                            Char.myCharz().cName = msg.reader().readUTF();
                            Char.myCharz().cPk = msg.reader().readByte();
                            Char.myCharz().cTypePk = msg.reader().readByte();
                            Char.myCharz().cPower = msg.reader().readLong();
                            Char.myCharz().applyCharLevelPercent();
                            Char.myCharz().eff5BuffHp = msg.reader().readShort();
                            Char.myCharz().eff5BuffMp = msg.reader().readShort();
                            Char.myCharz().nClass = GameScr.nClasss[msg.reader().readByte()];
                            Char.myCharz().vSkill.removeAllElements();
                            Char.myCharz().vSkillFight.removeAllElements();
                            GameScr.gI().dHP = Char.myCharz().cHP;
                            GameScr.gI().dMP = Char.myCharz().cMP;
                            sbyte b2 = msg.reader().readByte();
                            for (sbyte b6 = 0; b6 < b2; b6++)
                            {
                                Skill skill3 = Skills.get(msg.reader().readShort());
                                useSkill(skill3);
                            }
                            GameScr.gI().sortSkill();
                            GameScr.gI().loadSkillShortcut();
                            Char.myCharz().xu = msg.reader().readLong();
                            Char.myCharz().luongKhoa = msg.reader().readInt();
                            Char.myCharz().luong = msg.reader().readInt();
                            Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                            Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                            Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                            Char.myCharz().arrItemBody = new Item[msg.reader().readByte()];
                            try
                            {
                                Char.myCharz().setDefaultPart();
                                for (int k = 0; k < Char.myCharz().arrItemBody.Length; k++)
                                {
                                    short num6 = msg.reader().readShort();
                                    if (num6 == -1)
                                    {
                                        continue;
                                    }
                                    ItemTemplate itemTemplate = ItemTemplates.get(num6);
                                    if (itemTemplate == null)
                                    {
                                        itemTemplate = new ItemTemplate(num6, 0, 0, "Item " + num6, "", 0, 0, 0, 0, false);
                                    }
                                    int num7 = itemTemplate.type;
                                    Char.myCharz().arrItemBody[k] = new Item();
                                    Char.myCharz().arrItemBody[k].template = itemTemplate;
                                    Char.myCharz().arrItemBody[k].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBody[k].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBody[k].content = msg.reader().readUTF();
                                    int num8 = msg.reader().readUnsignedByte();
                                    if (num8 != 0)
                                    {
                                        Char.myCharz().arrItemBody[k].itemOption = new ItemOption[num8];
                                        for (int l = 0; l < Char.myCharz().arrItemBody[k].itemOption.Length; l++)
                                        {
                                            int num9 = msg.reader().readUnsignedByte();
                                            int param = msg.reader().readUnsignedShort();
                                            if (num9 != -1)
                                            {
                                                Char.myCharz().arrItemBody[k].itemOption[l] = new ItemOption(num9, param);
                                            }
                                        }
                                    }
                                    switch (num7)
                                    {
                                        case 0:
                                            Res.outz("toi day =======================================" + Char.myCharz().body);
                                            Char.myCharz().body = Char.myCharz().arrItemBody[k].template.part;
                                            break;
                                        case 1:
                                            Char.myCharz().leg = Char.myCharz().arrItemBody[k].template.part;
                                            Res.outz("toi day =======================================" + Char.myCharz().leg);
                                            break;
                                    }
                                }
                            }
                            catch (Exception catchErr_1)
                            {
                                Cout.LogWarning("[Controller.SubCommand.cs] " + catchErr_1.Message);
                            }
                            try
                            {
                                Char.myCharz().arrItemBag = new Item[msg.reader().readByte()];
                                GameScr.hpPotion = 0;
                                GameScr.isudungCapsun4 = false;
                                GameScr.isudungCapsun3 = false;
                                for (int m = 0; m < Char.myCharz().arrItemBag.Length; m++)
                                {
                                    short num10 = msg.reader().readShort();
                                    if (num10 == -1)
                                    {
                                        continue;
                                    }
                                    ItemTemplate itemBagTemplate = ItemTemplates.get(num10);
                                    if (itemBagTemplate == null)
                                    {
                                        itemBagTemplate = new ItemTemplate(num10, 0, 0, "Item " + num10, "", 0, 0, 0, 0, false);
                                    }
                                    Char.myCharz().arrItemBag[m] = new Item();
                                    Char.myCharz().arrItemBag[m].template = itemBagTemplate;
                                    Char.myCharz().arrItemBag[m].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBag[m].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBag[m].content = msg.reader().readUTF();
                                    Char.myCharz().arrItemBag[m].indexUI = m;
                                    sbyte b7 = msg.reader().readByte();
                                    if (b7 != 0)
                                    {
                                        Char.myCharz().arrItemBag[m].itemOption = new ItemOption[b7];
                                        for (int n = 0; n < Char.myCharz().arrItemBag[m].itemOption.Length; n++)
                                        {
                                            int num11 = msg.reader().readUnsignedByte();
                                            int param2 = msg.reader().readUnsignedShort();
                                            if (num11 != -1)
                                            {
                                                Char.myCharz().arrItemBag[m].itemOption[n] = new ItemOption(num11, param2);
                                                Char.myCharz().arrItemBag[m].getCompare();
                                            }
                                        }
                                    }
                                    if (Char.myCharz().arrItemBag[m].template != null && Char.myCharz().arrItemBag[m].template.type == 6)
                                    {
                                        GameScr.hpPotion += Char.myCharz().arrItemBag[m].quantity;
                                    }
                                    switch (num10)
                                    {
                                        case 194:
                                            GameScr.isudungCapsun4 = Char.myCharz().arrItemBag[m].quantity > 0;
                                            break;
                                        case 193:
                                            if (!GameScr.isudungCapsun4)
                                            {
                                                GameScr.isudungCapsun3 = Char.myCharz().arrItemBag[m].quantity > 0;
                                            }
                                            break;
                                    }
                                }
                            }
                            catch (Exception exBag)
                            {
                                Cout.println("Error load arrItemBag: " + exBag.ToString());
                            }
                            try
                            {
                                Char.myCharz().arrItemBox = new Item[msg.reader().readByte()];
                                GameCanvas.panel.hasUse = 0;
                                for (int num12 = 0; num12 < Char.myCharz().arrItemBox.Length; num12++)
                                {
                                    short num13 = msg.reader().readShort();
                                    if (num13 == -1)
                                    {
                                        continue;
                                    }
                                    ItemTemplate itemBoxTemplate = ItemTemplates.get(num13);
                                    if (itemBoxTemplate == null)
                                    {
                                        itemBoxTemplate = new ItemTemplate(num13, 0, 0, "Item " + num13, "", 0, 0, 0, 0, false);
                                    }
                                    Char.myCharz().arrItemBox[num12] = new Item();
                                    Char.myCharz().arrItemBox[num12].template = itemBoxTemplate;
                                    Char.myCharz().arrItemBox[num12].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBox[num12].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBox[num12].content = msg.reader().readUTF();
                                    Char.myCharz().arrItemBox[num12].itemOption = new ItemOption[msg.reader().readByte()];
                                    for (int num14 = 0; num14 < Char.myCharz().arrItemBox[num12].itemOption.Length; num14++)
                                    {
                                        int num15 = msg.reader().readUnsignedByte();
                                        int param3 = msg.reader().readUnsignedShort();
                                        if (num15 != -1)
                                        {
                                            Char.myCharz().arrItemBox[num12].itemOption[num14] = new ItemOption(num15, param3);
                                            Char.myCharz().arrItemBox[num12].getCompare();
                                        }
                                    }
                                    GameCanvas.panel.hasUse++;
                                }
                            }
                            catch (Exception exBox)
                            {
                                Cout.println("Error load arrItemBox: " + exBox.ToString());
                            }
                            Char.myCharz().statusMe = 4;
                            int num16 = Rms.loadRMSInt(Char.myCharz().cName + "vci");
                            if (num16 < 1)
                            {
                                GameScr.isViewClanInvite = false;
                            }
                            else
                            {
                                GameScr.isViewClanInvite = true;
                            }
                            short num17 = msg.reader().readShort();
                            Char.idHead = new short[num17];
                            Char.idAvatar = new short[num17];
                            for (int num18 = 0; num18 < num17; num18++)
                            {
                                Char.idHead[num18] = msg.reader().readShort();
                                Char.idAvatar[num18] = msg.reader().readShort();
                            }
                            for (int num19 = 0; num19 < GameScr.info1.charId.Length; num19++)
                            {
                                GameScr.info1.charId[num19] = new int[3];
                            }
                            GameScr.info1.charId[Char.myCharz().cgender][0] = msg.reader().readShort();
                            GameScr.info1.charId[Char.myCharz().cgender][1] = msg.reader().readShort();
                            GameScr.info1.charId[Char.myCharz().cgender][2] = msg.reader().readShort();
                            Char.myCharz().isNhapThe = msg.reader().readByte() == 1;
                            Res.outz("NHAP THE= " + Char.myCharz().isNhapThe);
                            GameScr.deltaTime = mSystem.currentTimeMillis() - (long)msg.reader().readInt() * 1000L;
                            GameScr.isNewMember = msg.reader().readByte();
                            Service.gI().updateCaption((sbyte)Char.myCharz().cgender);
                            Service.gI().androidPack();
                            try
                            {
                                Char.myCharz().idAuraEff = msg.reader().readShort();
                                Char.myCharz().idEff_Set_Item = msg.reader().readSByte();
                                Char.myCharz().idHat = msg.reader().readShort();
                                break;
                            }
                            catch (Exception)
                            {
                                break;
                            }
                        }
                    case 4:
                        GameCanvas.debug("SA23", 2);
                        Char.myCharz().xu = msg.reader().readLong();
                        Char.myCharz().luong = msg.reader().readInt();
                        Char.myCharz().cHP = msg.readInt3Byte();
                        Char.myCharz().cMP = msg.readInt3Byte();
                        Char.myCharz().luongKhoa = msg.reader().readInt();
                        Char.myCharz().xuStr = Res.formatNumber2(Char.myCharz().xu);
                        Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                        Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                        break;
                    case 5:
                        {
                            GameCanvas.debug("SA24", 2);
                            long cHP = Char.myCharz().cHP;
                            Char.myCharz().cHP = msg.readInt3Byte();
                            if (Char.myCharz().cHP > cHP && Char.myCharz().cTypePk != 4)
                            {
                                GameScr.startFlyText("+" + (Char.myCharz().cHP - cHP) + " " + mResources.HP, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 20, 0, -1, mFont.HP);
                                SoundMn.gI().HP_MPup();
                                if (Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 5003)
                                {
                                    try
                                    {
                                        int dartType = (GameScr.darts != null && GameScr.darts.Length > 29 && GameScr.darts[29] != null) ? 29 : 0;
                                        MonsterDart.addMonsterDart(Char.myCharz().petFollow.cmx + ((Char.myCharz().petFollow.dir != 1) ? (-10) : 10), Char.myCharz().petFollow.cmy + 10, true, -1, -1, Char.myCharz(), dartType);
                                    }
                                    catch (Exception catchErr_2)
                                    {
                                        Cout.LogWarning("[Controller.SubCommand.cs] " + catchErr_2.Message);
                                    }
                                }
                            }
                            if (Char.myCharz().cHP < cHP)
                            {
                                GameScr.startFlyText("-" + (cHP - Char.myCharz().cHP) + " " + mResources.HP, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 20, 0, -1, mFont.HP);
                            }
                            GameScr.gI().dHP = Char.myCharz().cHP;
                            if (GameScr.isPaintInfoMe)
                            {
                            }
                            break;
                        }
                    case 6:
                        {
                            GameCanvas.debug("SA25", 2);
                            if (Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5)
                            {
                                break;
                            }
                            long cMP = Char.myCharz().cMP;
                            Char.myCharz().cMP = msg.readInt3Byte();
                            if (Char.myCharz().cMP > cMP)
                            {
                                GameScr.startFlyText("+" + (Char.myCharz().cMP - cMP) + " " + mResources.KI, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 23, 0, -2, mFont.MP);
                                SoundMn.gI().HP_MPup();
                                if (Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 5001)
                                {
                                    try
                                    {
                                        int dartType = (GameScr.darts != null && GameScr.darts.Length > 29 && GameScr.darts[29] != null) ? 29 : 0;
                                        MonsterDart.addMonsterDart(Char.myCharz().petFollow.cmx + ((Char.myCharz().petFollow.dir != 1) ? (-10) : 10), Char.myCharz().petFollow.cmy + 10, true, -1, -1, Char.myCharz(), dartType);
                                    }
                                    catch (Exception catchErr_3)
                                    {
                                        Cout.LogWarning("[Controller.SubCommand.cs] " + catchErr_3.Message);
                                    }
                                }
                            }
                            if (Char.myCharz().cMP < cMP)
                            {
                                GameScr.startFlyText("-" + (cMP - Char.myCharz().cMP) + " " + mResources.KI, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 23, 0, -2, mFont.MP);
                            }
                            GameScr.gI().dMP = Char.myCharz().cMP;
                            if (GameScr.isPaintInfoMe)
                            {
                            }
                            break;
                        }
                    case 7:
                        {
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                break;
                            }
                            @char.clanID = msg.reader().readInt();
                            if (@char.clanID == -2)
                            {
                                @char.isCopy = true;
                            }
                            readCharInfo(@char, msg);
                            try
                            {
                                @char.idAuraEff = msg.reader().readShort();
                                @char.idEff_Set_Item = msg.reader().readSByte();
                                @char.idHat = msg.reader().readShort();
                                if (@char.bag >= 201)
                                {
                                    Effect effect = new Effect(@char.bag, @char, 2, -1, 10, 1);
                                    effect.typeEff = 5;
                                    @char.addEffChar(effect);
                                }
                                else
                                {
                                    @char.removeEffChar(0, 201);
                                }
                                break;
                            }
                            catch (Exception)
                            {
                                break;
                            }
                        }
                    case 8:
                        {
                            GameCanvas.debug("SA26", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cspeed = msg.reader().readByte();
                            }
                            break;
                        }
                    case 9:
                        {
                            GameCanvas.debug("SA27", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                            }
                            break;
                        }
                    case 10:
                        {
                            GameCanvas.debug("SA28", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.eff5BuffHp = msg.reader().readShort();
                                @char.eff5BuffMp = msg.reader().readShort();
                                @char.wp = msg.reader().readShort();
                                if (@char.wp == -1)
                                {
                                    @char.setDefaultWeapon();
                                }
                            }
                            break;
                        }
                    case 12:
                        {
                            GameCanvas.debug("SA30", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.eff5BuffHp = msg.reader().readShort();
                                @char.eff5BuffMp = msg.reader().readShort();
                                @char.leg = msg.reader().readShort();
                                if (@char.leg == -1)
                                {
                                    @char.setDefaultLeg();
                                }
                            }
                            break;
                        }
                    case 13:
                        {
                            GameCanvas.debug("SA31", 2);
                            int num2 = msg.reader().readInt();
                            Char @char = ((num2 != Char.myCharz().charID) ? GameScr.findCharInMap(num2) : Char.myCharz());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.eff5BuffHp = msg.reader().readShort();
                                @char.eff5BuffMp = msg.reader().readShort();
                            }
                            break;
                        }
                    case 14:
                        {
                            GameCanvas.debug("SA32", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                break;
                            }
                            @char.cHP = msg.readInt3Byte();
                            sbyte b4 = msg.reader().readByte();
                            Res.outz("player load hp type= " + b4);
                            if (b4 == 1)
                            {
                                ServerEffect.addServerEffect(11, @char, 5);
                                ServerEffect.addServerEffect(104, @char, 4);
                            }
                            if (b4 == 2)
                            {
                                @char.doInjure();
                            }
                            try
                            {
                                @char.cHPFull = msg.readInt3Byte();
                                break;
                            }
                            catch (Exception)
                            {
                                break;
                            }
                        }
                    case 15:
                        {
                            GameCanvas.debug("SA33", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.cx = msg.reader().readShort();
                                @char.cy = msg.reader().readShort();
                                @char.statusMe = 1;
                                @char.cp3 = 3;
                                ServerEffect.addServerEffect(109, @char, 2);
                            }
                            break;
                        }
                    case 35:
                        {
                            GameCanvas.debug("SY3", 2);
                            int num4 = msg.reader().readInt();
                            Res.outz("CID = " + num4);
                            if (TileMap.mapID == 130)
                            {
                                GameScr.gI().starVS();
                            }
                            if (num4 == Char.myCharz().charID)
                            {
                                Char.myCharz().cTypePk = msg.reader().readByte();
                                if (GameScr.gI().isVS() && Char.myCharz().cTypePk != 0)
                                {
                                    GameScr.gI().starVS();
                                }
                                Res.outz("type pk= " + Char.myCharz().cTypePk);
                                Char.myCharz().npcFocus = null;
                                if (!GameScr.gI().isMeCanAttackMob(Char.myCharz().mobFocus))
                                {
                                    Char.myCharz().mobFocus = null;
                                }
                                Char.myCharz().itemFocus = null;
                            }
                            else
                            {
                                Char @char = GameScr.findCharInMap(num4);
                                if (@char != null)
                                {
                                    Res.outz("type pk= " + @char.cTypePk);
                                    @char.cTypePk = msg.reader().readByte();
                                    if (@char.isAttacPlayerStatus())
                                    {
                                        Char.myCharz().charFocus = @char;
                                    }
                                }
                            }
                            for (int i = 0; i < GameScr.vCharInMap.size(); i++)
                            {
                                Char char2 = GameScr.findCharInMap(i);
                                if (char2 != null && char2.cTypePk != 0 && char2.cTypePk == Char.myCharz().cTypePk)
                                {
                                    if (!Char.myCharz().mobFocus.isMobMe)
                                    {
                                        Char.myCharz().mobFocus = null;
                                    }
                                    Char.myCharz().npcFocus = null;
                                    Char.myCharz().itemFocus = null;
                                    break;
                                }
                            }
                            Res.outz("update type pk= ");
                            break;
                        }
                    case 61:
                        {
                            string text = msg.reader().readUTF();
                            sbyte[] data = new sbyte[msg.reader().readInt()];
                            msg.reader().read(ref data);
                            if (data.Length == 0)
                            {
                                data = null;
                            }
                            if (text.Equals("KSkill"))
                            {
                                GameScr.gI().onKSkill(data);
                            }
                            else if (text.Equals("OSkill"))
                            {
                                GameScr.gI().onOSkill(data);
                            }
                            else if (text.Equals("CSkill"))
                            {
                                GameScr.gI().onCSkill(data);
                            }
                            break;
                        }
                    case 23:
                        {
                            short num = msg.reader().readShort();
                            Skill skill = Skills.get(num);
                            useSkill(skill);
                            if (num != 0 && num != 14 && num != 28)
                            {
                                GameScr.info1.addInfo(mResources.LEARN_SKILL + " " + skill.template.name, 0);
                            }
                            break;
                        }
                    case 62:
                        Res.outz("ME UPDATE SKILL");
                        read_UpdateSkill(msg);
                        break;
                }
            }
            catch (Exception ex5)
            {
                Cout.println("Loi tai Sub : " + ex5.ToString());
            }
            finally
            {
                if (msg != null)
                {
                    msg.cleanup();
                }
            }
        }
    
        private void useSkill(Skill skill)
        {
            if (Char.myCharz().myskill == null)
            {
                Char.myCharz().myskill = skill;
            }
            else if (skill.template.Equals(Char.myCharz().myskill.template))
            {
                Char.myCharz().myskill = skill;
            }
            Char.myCharz().vSkill.addElement(skill);
            if ((skill.template.type == 1 || skill.template.type == 4 || skill.template.type == 2 || skill.template.type == 3) && (skill.template.maxPoint == 0 || (skill.template.maxPoint > 0 && skill.point > 0)))
            {
                if (skill.template.id == Char.myCharz().skillTemplateId)
                {
                    Service.gI().selectSkill(Char.myCharz().skillTemplateId);
                }
                Char.myCharz().vSkillFight.addElement(skill);
            }
        }
    
        public bool readCharInfo(Char c, Message msg)
        {
            try
            {
                c.clevel = msg.reader().readByte();
                c.isInvisiblez = msg.reader().readBoolean();
                c.cTypePk = msg.reader().readByte();
                Res.outz("ADD TYPE PK= " + c.cTypePk + " to player " + c.charID + " @@ " + c.cName);
                c.nClass = GameScr.nClasss[msg.reader().readByte()];
                c.cgender = msg.reader().readByte();
                c.head = msg.reader().readShort();
                c.cName = msg.reader().readUTF();
                if (c.charID <= -310720000 || c.cName == "$" || (c.cName != null && c.cName.StartsWith("$") && c.cName.Length == 1))
                {
                    c.isPetFollower = true;
                    c.isPet = true;
                    c.cName = string.Empty;
                    c.paintName = false;
                }
                c.cHP = msg.readInt3Byte();
                c.dHP = c.cHP;
                if (c.cHP == 0)
                {
                    c.statusMe = 14;
                }
                c.cHPFull = msg.readInt3Byte();
                if (c.cy >= TileMap.pxh - 100)
                {
                    c.isFlyUp = true;
                }
                c.body = msg.reader().readShort();
                c.leg = msg.reader().readShort();
                c.bag = msg.reader().readUnsignedByte();
                Res.outz(" body= " + c.body + " leg= " + c.leg + " bag=" + c.bag + "BAG ==" + c.bag + "*********************************");
                c.isShadown = true;
                sbyte b = msg.reader().readByte();
                if (c.wp == -1)
                {
                    c.setDefaultWeapon();
                }
                if (c.body == -1)
                {
                    c.setDefaultBody();
                }
                if (c.leg == -1)
                {
                    c.setDefaultLeg();
                }
                c.cx = msg.reader().readShort();
                c.cy = msg.reader().readShort();
                c.xSd = c.cx;
                c.ySd = c.cy;
                c.eff5BuffHp = msg.reader().readShort();
                c.eff5BuffMp = msg.reader().readShort();
                int num = msg.reader().readByte();
                for (int i = 0; i < num; i++)
                {
                    EffectChar effectChar = new EffectChar(msg.reader().readByte(), msg.reader().readInt(), msg.reader().readInt(), msg.reader().readShort());
                    c.vEff.addElement(effectChar);
                    if (effectChar.template.type == 12 || effectChar.template.type == 11)
                    {
                        c.isInvisiblez = true;
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                ex.StackTrace.ToString();
            }
            return false;
        }
    
    }
}

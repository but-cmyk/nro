namespace Game1
{
    using Game1.Assets.src.e;
    using Assets.src.f;
    using Game1.Assets.src.g;
    using System;
    using UnityEngine;

    public partial class Controller : IMessageHandler
    {
        private bool handlePacketGroupA(Message msg, ref Char @char, ref Mob mob, MyVector myVector, ref int num)
        {
            switch (msg.command)
            {
                    case 0:
                        readLogin(msg);
                        break;
                    case 24:
                        read_opt(msg);
                        break;
                    case 20:
                        phuban_Info(msg);
                        return true;
                    case 66:
                        readGetImgByName(msg);
                        return true;
                    case 65:
                        {
                            sbyte b68 = msg.reader().readSByte();
                            string text6 = msg.reader().readUTF();
                            short num166 = msg.reader().readShort();
                            if (ItemTime.isExistMessage(b68))
                            {
                                if (num166 != 0)
                                {
                                    ItemTime.getMessageById(b68).initTimeText(b68, text6, num166);
                                }
                                else
                                {
                                    GameScr.textTime.removeElement(ItemTime.getMessageById(b68));
                                }
                            }
                            else
                            {
                                ItemTime itemTime = new ItemTime();
                                itemTime.initTimeText(b68, text6, num166);
                                GameScr.textTime.addElement(itemTime);
                            }
                            break;
                        }
                    case 112:
                        {
                            sbyte b24 = msg.reader().readByte();
                            Res.outz("spec type= " + b24);
                            if (b24 == 0)
                            {
                                Panel.spearcialImage = msg.reader().readShort();
                                Panel.specialInfo = msg.reader().readUTF();
                            }
                            else
                            {
                                if (b24 != 1)
                                {
                                    break;
                                }
                                sbyte b25 = msg.reader().readByte();
                                Char.myCharz().infoSpeacialSkill = new string[b25][];
                                Char.myCharz().imgSpeacialSkill = new short[b25][];
                                GameCanvas.panel.speacialTabName = new string[b25][];
                                for (int num48 = 0; num48 < b25; num48++)
                                {
                                    GameCanvas.panel.speacialTabName[num48] = new string[2];
                                    string[] array4 = Res.split(msg.reader().readUTF(), "\n", 0);
                                    if (array4.Length == 2)
                                    {
                                        GameCanvas.panel.speacialTabName[num48] = array4;
                                    }
                                    if (array4.Length == 1)
                                    {
                                        GameCanvas.panel.speacialTabName[num48][0] = array4[0];
                                        GameCanvas.panel.speacialTabName[num48][1] = string.Empty;
                                    }
                                    int num49 = msg.reader().readByte();
                                    Char.myCharz().infoSpeacialSkill[num48] = new string[num49];
                                    Char.myCharz().imgSpeacialSkill[num48] = new short[num49];
                                    for (int num50 = 0; num50 < num49; num50++)
                                    {
                                        Char.myCharz().imgSpeacialSkill[num48][num50] = msg.reader().readShort();
                                        Char.myCharz().infoSpeacialSkill[num48][num50] = msg.reader().readUTF();
                                    }
                                }
                                GameCanvas.panel.tabName[25] = GameCanvas.panel.speacialTabName;
                                GameCanvas.panel.setTypeSpeacialSkill();
                                GameCanvas.panel.show();
                            }
                            break;
                        }
                    case -98:
                        {
                            sbyte b66 = msg.reader().readByte();
                            GameCanvas.menu.showMenu = false;
                            if (b66 == 0)
                            {
                                GameCanvas.startYesNoDlg(msg.reader().readUTF(), new Command(mResources.YES, GameCanvas.instance, 888397, msg.reader().readUTF()), new Command(mResources.NO, GameCanvas.instance, 888396, null));
                            }
                            break;
                        }
                    case -96:
                        {
                            sbyte typeTop = msg.reader().readByte();
                            GameCanvas.panel.vTop.removeAllElements();
                            string topName = msg.reader().readUTF();
                            sbyte b49 = msg.reader().readByte();
                            for (int num128 = 0; num128 < b49; num128++)
                            {
                                int rank = msg.reader().readInt();
                                int pId = msg.reader().readInt();
                                short headID = msg.reader().readShort();
                                short headICON = msg.reader().readShort();
                                short body = msg.reader().readShort();
                                short leg = msg.reader().readShort();
                                string name = msg.reader().readUTF();
                                string info3 = msg.reader().readUTF();
                                TopInfo topInfo = new TopInfo();
                                topInfo.rank = rank;
                                topInfo.headID = headID;
                                topInfo.headICON = headICON;
                                topInfo.body = body;
                                topInfo.leg = leg;
                                topInfo.name = name;
                                topInfo.info = info3;
                                topInfo.info2 = msg.reader().readUTF();
                                topInfo.pId = pId;
                                GameCanvas.panel.vTop.addElement(topInfo);
                            }
                            GameCanvas.panel.topName = topName;
                            GameCanvas.panel.setTypeTop(typeTop);
                            GameCanvas.panel.show();
                            break;
                        }
                    case -95:
                        {
                            sbyte b29 = msg.reader().readByte();
                            Res.outz("type= " + b29);
                            if (b29 == 0)
                            {
                                int num61 = msg.reader().readInt();
                                short templateId = msg.reader().readShort();
                                long num62 = msg.readInt3Byte();
                                SoundMn.gI().explode_1();
                                if (num61 == Char.myCharz().charID)
                                {
                                    Char.myCharz().mobMe = new Mob(num61, false, false, false, false, false, templateId, 1, num62, 0, num62, (short)(Char.myCharz().cx + ((Char.myCharz().cdir != 1) ? (-40) : 40)), (short)Char.myCharz().cy, 4, 0);
                                    Char.myCharz().mobMe.isMobMe = true;
                                    EffecMn.addEff(new Effect(18, Char.myCharz().mobMe.x, Char.myCharz().mobMe.y, 2, 10, -1));
                                    Char.myCharz().tMobMeBorn = 30;
                                    GameScr.vMob.addElement(Char.myCharz().mobMe);
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num61);
                                    if (@char != null)
                                    {
                                        Mob mob4 = new Mob(num61, false, false, false, false, false, templateId, 1, num62, 0, num62, (short)@char.cx, (short)@char.cy, 4, 0);
                                        mob4.isMobMe = true;
                                        @char.mobMe = mob4;
                                        GameScr.vMob.addElement(@char.mobMe);
                                    }
                                    else
                                    {
                                        Mob mob5 = GameScr.findMobInMap(num61);
                                        if (mob5 == null)
                                        {
                                            mob5 = new Mob(num61, false, false, false, false, false, templateId, 1, num62, 0, num62, -100, -100, 4, 0);
                                            mob5.isMobMe = true;
                                            GameScr.vMob.addElement(mob5);
                                        }
                                    }
                                }
                            }
                            if (b29 == 1)
                            {
                                int num63 = msg.reader().readInt();
                                int mobId = msg.reader().readByte();
                                Res.outz("mod attack id= " + num63);
                                if (num63 == Char.myCharz().charID)
                                {
                                    if (GameScr.findMobInMap(mobId) != null)
                                    {
                                        Char.myCharz().mobMe.attackOtherMob(GameScr.findMobInMap(mobId));
                                    }
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num63);
                                    if (@char != null && GameScr.findMobInMap(mobId) != null)
                                    {
                                        @char.mobMe.attackOtherMob(GameScr.findMobInMap(mobId));
                                    }
                                }
                            }
                            if (b29 == 2)
                            {
                                int num64 = msg.reader().readInt();
                                int num65 = msg.reader().readInt();
                                long num66 = msg.readInt3Byte();
                                long cHPNew = msg.readInt3Byte();
                                if (num64 == Char.myCharz().charID)
                                {
                                    Res.outz("mob dame= " + num66);
                                    @char = GameScr.findCharInMap(num65);
                                    if (@char != null)
                                    {
                                        @char.cHPNew = cHPNew;
                                        if (Char.myCharz().mobMe.isBusyAttackSomeOne)
                                        {
                                            @char.doInjure(num66, 0, false, true);
                                        }
                                        else
                                        {
                                            Char.myCharz().mobMe.dame = num66;
                                            Char.myCharz().mobMe.setAttack(@char);
                                        }
                                    }
                                }
                                else
                                {
                                    mob = GameScr.findMobInMap(num64);
                                    if (mob != null)
                                    {
                                        if (num65 == Char.myCharz().charID)
                                        {
                                            Char.myCharz().cHPNew = cHPNew;
                                            if (mob.isBusyAttackSomeOne)
                                            {
                                                Char.myCharz().doInjure(num66, 0, false, true);
                                            }
                                            else
                                            {
                                                mob.dame = num66;
                                                mob.setAttack(Char.myCharz());
                                            }
                                        }
                                        else
                                        {
                                            @char = GameScr.findCharInMap(num65);
                                            if (@char != null)
                                            {
                                                @char.cHPNew = cHPNew;
                                                if (mob.isBusyAttackSomeOne)
                                                {
                                                    @char.doInjure(num66, 0, false, true);
                                                }
                                                else
                                                {
                                                    mob.dame = num66;
                                                    mob.setAttack(@char);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (b29 == 3)
                            {
                                int num67 = msg.reader().readInt();
                                int mobId2 = msg.reader().readInt();
                                long hp = msg.readInt3Byte();
                                long num68 = msg.readInt3Byte();
                                @char = null;
                                @char = ((Char.myCharz().charID != num67) ? GameScr.findCharInMap(num67) : Char.myCharz());
                                if (@char != null)
                                {
                                    mob = GameScr.findMobInMap(mobId2);
                                    if (@char.mobMe != null)
                                    {
                                        @char.mobMe.attackOtherMob(mob);
                                    }
                                    if (mob != null)
                                    {
                                        mob.hp = hp;
                                        mob.updateHp_bar();
                                        if (num68 == 0)
                                        {
                                            mob.x = mob.xFirst;
                                            mob.y = mob.yFirst;
                                            GameScr.startFlyText(mResources.miss, mob.x, mob.y - mob.h, 0, -2, mFont.MISS);
                                        }
                                        else
                                        {
                                            GameScr.startFlyText("-" + num68, mob.x, mob.y - mob.h, 0, -2, mFont.ORANGE);
                                        }
                                    }
                                }
                            }
                            if (b29 == 4)
                            {
                            }
                            if (b29 == 5)
                            {
                                int num69 = msg.reader().readInt();
                                sbyte b30 = msg.reader().readByte();
                                int mobId3 = msg.reader().readInt();
                                long num70 = msg.readInt3Byte();
                                long hp2 = msg.readInt3Byte();
                                @char = null;
                                @char = ((num69 != Char.myCharz().charID) ? GameScr.findCharInMap(num69) : Char.myCharz());
                                if (@char == null)
                                {
                                    return true;
                                }
                                @char.setSkillPaint(GameScr.getSkillPaint(b30), ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2) ? 0 : 1);
                                Mob mob6 = GameScr.findMobInMap(mobId3);
                                if (@char.cx <= mob6.x)
                                {
                                    @char.cdir = 1;
                                }
                                else
                                {
                                    @char.cdir = -1;
                                }
                                @char.mobFocus = mob6;
                                mob6.hp = hp2;
                                mob6.updateHp_bar();
                                GameCanvas.debug("SA83v2", 2);
                                if (num70 == 0)
                                {
                                    mob6.x = mob6.xFirst;
                                    mob6.y = mob6.yFirst;
                                    GameScr.startFlyText(mResources.miss, mob6.x, mob6.y - mob6.h, 0, -2, mFont.MISS);
                                }
                                else
                                {
                                    GameScr.startFlyText("-" + num70, mob6.x, mob6.y - mob6.h, 0, -2, mFont.ORANGE);
                                }
                            }
                            if (b29 == 6)
                            {
                                int num71 = msg.reader().readInt();
                                if (num71 == Char.myCharz().charID)
                                {
                                    Char.myCharz().mobMe.startDie();
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num71);
                                    if (@char != null)
                                    {
                                        @char.mobMe.startDie();
                                    }
                                }
                            }
                            if (b29 != 7)
                            {
                                break;
                            }
                            int num72 = msg.reader().readInt();
                            if (num72 == Char.myCharz().charID)
                            {
                                Char.myCharz().mobMe = null;
                                for (int num73 = 0; num73 < GameScr.vMob.size(); num73++)
                                {
                                    if (((Mob)GameScr.vMob.elementAt(num73)).mobId == num72)
                                    {
                                        GameScr.vMob.removeElementAt(num73);
                                    }
                                }
                                break;
                            }
                            @char = GameScr.findCharInMap(num72);
                            for (int num74 = 0; num74 < GameScr.vMob.size(); num74++)
                            {
                                if (((Mob)GameScr.vMob.elementAt(num74)).mobId == num72)
                                {
                                    GameScr.vMob.removeElementAt(num74);
                                }
                            }
                            if (@char != null)
                            {
                                @char.mobMe = null;
                            }
                            break;
                        }
                    case -92:
                        Main.typeClient = msg.reader().readByte();
                        if (Rms.loadRMSString("ResVersion") == null)
                        {
                            Rms.clearAll();
                        }
                        Rms.saveRMSInt("clienttype", Main.typeClient);
                        Rms.saveRMSInt("lastZoomlevel", mGraphics.zoomLevel);
                        if (Rms.loadRMSString("ResVersion") == null)
                        {
                            GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
                        }
                        break;
                    case -90:
                        {
                            sbyte b63 = msg.reader().readByte();
                            int num149 = msg.reader().readInt();
                            Res.outz("===> UPDATE_BODY:    type = " + b63);
                            @char = ((Char.myCharz().charID != num149) ? GameScr.findCharInMap(num149) : Char.myCharz());
                            if (b63 != -1)
                            {
                                short num150 = msg.reader().readShort();
                                short num151 = msg.reader().readShort();
                                short num152 = msg.reader().readShort();
                                sbyte isMonkey = msg.reader().readByte();
                                if (@char != null)
                                {
                                    if (@char.charID == num149)
                                    {
                                        @char.isMask = true;
                                        @char.isMonkey = isMonkey;
                                        if (@char.isMonkey != 0)
                                        {
                                            @char.isWaitMonkey = false;
                                            @char.isLockMove = false;
                                        }
                                    }
                                    else if (@char != null)
                                    {
                                        @char.isMask = true;
                                        @char.isMonkey = isMonkey;
                                    }
                                    if (num150 != -1)
                                    {
                                        @char.head = num150;
                                    }
                                    if (num151 != -1)
                                    {
                                        @char.body = num151;
                                    }
                                    if (num152 != -1)
                                    {
                                        @char.leg = num152;
                                    }
                                }
                            }
                            if (b63 == -1 && @char != null)
                            {
                                @char.isMask = false;
                                @char.isMonkey = 0;
                            }
                            if (@char == null)
                            {
                                break;
                            }
                            for (int num153 = 0; num153 < 54; num153++)
                            {
                                @char.removeEffChar(0, 201 + num153);
                            }
                            if (@char.bag >= 201 && @char.bag < 255)
                            {
                                Effect effect2 = new Effect(@char.bag, @char, 2, -1, 10, 1);
                                effect2.typeEff = 5;
                                @char.addEffChar(effect2);
                            }
                            if (@char.bag == 30 && @char.me)
                            {
                                GameScr.isPickNgocRong = true;
                            }
                            if (!@char.me)
                            {
                                break;
                            }
                            GameScr.isudungCapsun4 = false;
                            GameScr.isudungCapsun3 = false;
                            if (Char.myCharz() != null && Char.myCharz().arrItemBag != null)
                            {
                                for (int num154 = 0; num154 < Char.myCharz().arrItemBag.Length; num154++)
                                {
                                    Item item4 = Char.myCharz().arrItemBag[num154];
                                    if (item4 == null || item4.template == null)
                                    {
                                        continue;
                                    }
                                    if (item4.template.id == 194)
                                    {
                                        GameScr.isudungCapsun4 = item4.quantity > 0;
                                        if (GameScr.isudungCapsun4)
                                        {
                                            break;
                                        }
                                    }
                                    else if (item4.template.id == 193)
                                    {
                                        GameScr.isudungCapsun3 = item4.quantity > 0;
                                    }
                                }
                            }
                            break;
                        }
                    case -88:
                        GameCanvas.endDlg();
                        GameCanvas.serverScreen.switchToMe();
                        break;
                    case -87:
                        {
                            Res.outz("GET UPDATE_DATA " + msg.reader().available() + " bytes");
                            msg.reader().mark(100000);
                            createData(msg.reader(), true);
                            msg.reader().reset();
                            sbyte[] data2 = new sbyte[msg.reader().available()];
                            msg.reader().readFully(ref data2);
                            sbyte[] data3 = new sbyte[1] { GameScr.vcData };
                            Rms.saveRMS("NRdataVersion", data3);
                            LoginScr.isUpdateData = false;
                            if (GameScr.vsData == GameScr.vcData && GameScr.vsMap == GameScr.vcMap && GameScr.vsSkill == GameScr.vcSkill && GameScr.vsItem == GameScr.vcItem)
                            {
                                Res.outz(GameScr.vsData + "," + GameScr.vsMap + "," + GameScr.vsSkill + "," + GameScr.vsItem);
                                GameScr.gI().readDart();
                                GameScr.gI().readEfect();
                                GameScr.gI().readArrow();
                                GameScr.gI().readSkill();
                                Service.gI().clientOk();
                                return true;
                            }
                            break;
                        }
                    case -85:
                        {
                            Res.outz("CAP CHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                            sbyte b69 = msg.reader().readByte();
                            if (b69 == 0)
                            {
                                int num167 = msg.reader().readUnsignedShort();
                                Res.outz("lent =" + num167);
                                sbyte[] data5 = new sbyte[num167];
                                msg.reader().read(ref data5, 0, num167);
                                GameScr.imgCapcha = Image.createImage(data5, 0, num167);
                                GameScr.gI().keyInput = "-----";
                                GameScr.gI().strCapcha = msg.reader().readUTF();
                                GameScr.gI().keyCapcha = new int[GameScr.gI().strCapcha.Length];
                                GameScr.gI().mobCapcha = new Mob();
                                GameScr.gI().right = null;
                            }
                            if (b69 == 1)
                            {
                                MobCapcha.isAttack = true;
                            }
                            if (b69 == 2)
                            {
                                MobCapcha.explode = true;
                                GameScr.gI().right = GameScr.gI().cmdFocus;
                            }
                            break;
                        }
                    case -112:
                        {
                            sbyte b50 = msg.reader().readByte();
                            if (b50 == 0)
                            {
                                sbyte mobIndex = msg.reader().readByte();
                                GameScr.findMobInMap(mobIndex).clearBody();
                            }
                            if (b50 == 1)
                            {
                                sbyte mobIndex2 = msg.reader().readByte();
                                GameScr.findMobInMap(mobIndex2).setBody(msg.reader().readShort());
                            }
                            break;
                        }
                    case -84:
                        {
                            int index2 = msg.reader().readUnsignedByte();
                            Mob mob7 = null;
                            try
                            {
                                mob7 = (Mob)GameScr.vMob.elementAt(index2);
                            }
                            catch (Exception catchErr_1)
                            {
                                Cout.LogWarning("[Controller.PacketsA.cs] " + catchErr_1.Message);
                            }
                            if (mob7 != null)
                            {
                                mob7.maxHp = msg.reader().readInt();
                            }
                            break;
                        }
                    case -83:
                        {
                            sbyte b7 = msg.reader().readByte();
                            if (b7 == 0)
                            {
                                int num11 = msg.reader().readShort();
                                int bgRID = msg.reader().readShort();
                                int num12 = msg.reader().readUnsignedByte();
                                int num13 = msg.reader().readInt();
                                string text = msg.reader().readUTF();
                                int num14 = msg.reader().readShort();
                                int num15 = msg.reader().readShort();
                                sbyte b8 = msg.reader().readByte();
                                if (b8 == 1)
                                {
                                    GameScr.gI().isRongNamek = true;
                                }
                                else
                                {
                                    GameScr.gI().isRongNamek = false;
                                }
                                GameScr.gI().xR = num14;
                                GameScr.gI().yR = num15;
                                Res.outz("xR= " + num14 + " yR= " + num15 + " +++++++++++++++++++++++++++++++++++++++");
                                if (Char.myCharz().charID == num13)
                                {
                                    GameCanvas.panel.hideNow();
                                    GameScr.gI().activeRongThanEff(true);
                                }
                                else if (TileMap.mapID == num11 && TileMap.zoneID == num12)
                                {
                                    GameScr.gI().activeRongThanEff(false);
                                }
                                else if (mGraphics.zoomLevel > 1)
                                {
                                    GameScr.gI().doiMauTroi();
                                }
                                GameScr.gI().mapRID = num11;
                                GameScr.gI().bgRID = bgRID;
                                GameScr.gI().zoneRID = num12;
                            }
                            if (b7 == 1)
                            {
                                Res.outz("map RID = " + GameScr.gI().mapRID + " zone RID= " + GameScr.gI().zoneRID);
                                Res.outz("map ID = " + TileMap.mapID + " zone ID= " + TileMap.zoneID);
                                if (TileMap.mapID == GameScr.gI().mapRID && TileMap.zoneID == GameScr.gI().zoneRID)
                                {
                                    GameScr.gI().hideRongThanEff();
                                }
                                else
                                {
                                    GameScr.gI().isRongThanXuatHien = false;
                                    if (GameScr.gI().isRongNamek)
                                    {
                                        GameScr.gI().isRongNamek = false;
                                    }
                                }
                            }
                            if (b7 != 2)
                            {
                            }
                            break;
                        }
                    case -82:
                        {
                            sbyte b38 = msg.reader().readByte();
                            TileMap.tileIndex = new int[b38][][];
                            TileMap.tileType = new int[b38][];
                            for (int num111 = 0; num111 < b38; num111++)
                            {
                                sbyte b39 = msg.reader().readByte();
                                TileMap.tileType[num111] = new int[b39];
                                TileMap.tileIndex[num111] = new int[b39][];
                                for (int num112 = 0; num112 < b39; num112++)
                                {
                                    TileMap.tileType[num111][num112] = msg.reader().readInt();
                                    sbyte b40 = msg.reader().readByte();
                                    TileMap.tileIndex[num111][num112] = new int[b40];
                                    for (int num113 = 0; num113 < b40; num113++)
                                    {
                                        TileMap.tileIndex[num111][num112][num113] = msg.reader().readByte();
                                    }
                                }
                            }
                            break;
                        }
                    case -81:
                        {
                            sbyte b42 = msg.reader().readByte();
                            if (b42 == 0)
                            {
                                string src = msg.reader().readUTF();
                                string src2 = msg.reader().readUTF();
                                GameCanvas.panel.setTypeCombine();
                                GameCanvas.panel.combineInfo = mFont.tahoma_7b_blue.splitFontArray(src, Panel.WIDTH_PANEL);
                                GameCanvas.panel.combineTopInfo = mFont.tahoma_7.splitFontArray(src2, Panel.WIDTH_PANEL);
                                GameCanvas.panel.show();
                            }
                            if (b42 == 1)
                            {
                                GameCanvas.panel.vItemCombine.removeAllElements();
                                sbyte b43 = msg.reader().readByte();
                                for (int num115 = 0; num115 < b43; num115++)
                                {
                                    sbyte b44 = msg.reader().readByte();
                                    for (int num116 = 0; num116 < Char.myCharz().arrItemBag.Length; num116++)
                                    {
                                        Item item = Char.myCharz().arrItemBag[num116];
                                        if (item != null && item.indexUI == b44)
                                        {
                                            item.isSelect = true;
                                            GameCanvas.panel.vItemCombine.addElement(item);
                                        }
                                    }
                                }
                                if (GameCanvas.panel.isShow)
                                {
                                    GameCanvas.panel.setTabCombine();
                                }
                            }
                            if (b42 == 2)
                            {
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(0);
                            }
                            if (b42 == 3)
                            {
                                GameCanvas.panel.combineSuccess = 1;
                                GameCanvas.panel.setCombineEff(0);
                            }
                            if (b42 == 4)
                            {
                                short iconID = msg.reader().readShort();
                                GameCanvas.panel.iconID3 = iconID;
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(1);
                            }
                            if (b42 == 5)
                            {
                                short iconID2 = msg.reader().readShort();
                                GameCanvas.panel.iconID3 = iconID2;
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(2);
                            }
                            if (b42 == 6)
                            {
                                short iconID3 = msg.reader().readShort();
                                short iconID4 = msg.reader().readShort();
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(3);
                                GameCanvas.panel.iconID1 = iconID3;
                                GameCanvas.panel.iconID3 = iconID4;
                            }
                            if (b42 == 7)
                            {
                                short iconID5 = msg.reader().readShort();
                                GameCanvas.panel.iconID3 = iconID5;
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(4);
                            }
                            if (b42 == 8)
                            {
                                GameCanvas.panel.iconID3 = -1;
                                GameCanvas.panel.combineSuccess = 1;
                                GameCanvas.panel.setCombineEff(4);
                            }
                            short num117 = 21;
                            int num118 = 0;
                            int num119 = 0;
                            try
                            {
                                num117 = msg.reader().readShort();
                                num118 = msg.reader().readShort();
                                num119 = msg.reader().readShort();
                                GameCanvas.panel.xS = num118 - GameScr.cmx;
                                GameCanvas.panel.yS = num119 - GameScr.cmy;
                            }
                            catch (Exception catchErr_2)
                            {
                                Cout.LogWarning("[Controller.PacketsA.cs] " + catchErr_2.Message);
                            }
                            for (int num120 = 0; num120 < GameScr.vNpc.size(); num120++)
                            {
                                Npc npc3 = (Npc)GameScr.vNpc.elementAt(num120);
                                if (npc3.template.npcTemplateId == num117)
                                {
                                    GameCanvas.panel.xS = npc3.cx - GameScr.cmx;
                                    GameCanvas.panel.yS = npc3.cy - GameScr.cmy;
                                    GameCanvas.panel.idNPC = num117;
                                    break;
                                }
                            }
                            break;
                        }
                    case -80:
                        {
                            sbyte b21 = msg.reader().readByte();
                            InfoDlg.hide();
                            if (b21 == 0)
                            {
                                GameCanvas.panel.vFriend.removeAllElements();
                                int num35 = msg.reader().readUnsignedByte();
                                for (int num36 = 0; num36 < num35; num36++)
                                {
                                    Char char6 = new Char();
                                    char6.charID = msg.reader().readInt();
                                    char6.head = msg.reader().readShort();
                                    char6.headICON = msg.reader().readShort();
                                    char6.body = msg.reader().readShort();
                                    char6.leg = msg.reader().readShort();
                                    char6.bag = msg.reader().readUnsignedByte();
                                    char6.cName = msg.reader().readUTF();
                                    bool isOnline = msg.reader().readBoolean();
                                    InfoItem infoItem2 = new InfoItem(mResources.power + ": " + msg.reader().readUTF());
                                    infoItem2.charInfo = char6;
                                    infoItem2.isOnline = isOnline;
                                    GameCanvas.panel.vFriend.addElement(infoItem2);
                                }
                                GameCanvas.panel.setTypeFriend();
                                GameCanvas.panel.show();
                            }
                            if (b21 == 3)
                            {
                                MyVector vFriend = GameCanvas.panel.vFriend;
                                int num37 = msg.reader().readInt();
                                Res.outz("online offline id=" + num37);
                                for (int num38 = 0; num38 < vFriend.size(); num38++)
                                {
                                    InfoItem infoItem3 = (InfoItem)vFriend.elementAt(num38);
                                    if (infoItem3.charInfo != null && infoItem3.charInfo.charID == num37)
                                    {
                                        Res.outz("online= " + infoItem3.isOnline);
                                        infoItem3.isOnline = msg.reader().readBoolean();
                                        break;
                                    }
                                }
                            }
                            if (b21 != 2)
                            {
                                break;
                            }
                            MyVector vFriend2 = GameCanvas.panel.vFriend;
                            int num39 = msg.reader().readInt();
                            for (int num40 = 0; num40 < vFriend2.size(); num40++)
                            {
                                InfoItem infoItem4 = (InfoItem)vFriend2.elementAt(num40);
                                if (infoItem4.charInfo != null && infoItem4.charInfo.charID == num39)
                                {
                                    vFriend2.removeElement(infoItem4);
                                    break;
                                }
                            }
                            if (GameCanvas.panel.isShow)
                            {
                                GameCanvas.panel.setTabFriend();
                            }
                            break;
                        }
                    case -99:
                        {
                            InfoDlg.hide();
                            sbyte b11 = msg.reader().readByte();
                            if (b11 == 0)
                            {
                                GameCanvas.panel.vEnemy.removeAllElements();
                                int num19 = msg.reader().readUnsignedByte();
                                for (int l = 0; l < num19; l++)
                                {
                                    Char char4 = new Char();
                                    char4.charID = msg.reader().readInt();
                                    char4.head = msg.reader().readShort();
                                    char4.headICON = msg.reader().readShort();
                                    char4.body = msg.reader().readShort();
                                    char4.leg = msg.reader().readShort();
                                    char4.bag = msg.reader().readShort();
                                    char4.cName = msg.reader().readUTF();
                                    InfoItem infoItem = new InfoItem(msg.reader().readUTF());
                                    bool flag3 = msg.reader().readBoolean();
                                    infoItem.charInfo = char4;
                                    infoItem.isOnline = flag3;
                                    Res.outz("isonline = " + flag3);
                                    GameCanvas.panel.vEnemy.addElement(infoItem);
                                }
                                GameCanvas.panel.setTypeEnemy();
                                GameCanvas.panel.show();
                            }
                            break;
                        }
                    case -79:
                        {
                            InfoDlg.hide();
                            int num127 = msg.reader().readInt();
                            Char charMenu = GameCanvas.panel.charMenu;
                            if (charMenu == null)
                            {
                                return true;
                            }
                            charMenu.cPower = msg.reader().readLong();
                            charMenu.currStrLevel = msg.reader().readUTF();
                            break;
                        }
                    case -93:
                        {
                            short num163 = msg.reader().readShort();
                            BgItem.newSmallVersion = new sbyte[num163];
                            for (int num164 = 0; num164 < num163; num164++)
                            {
                                BgItem.newSmallVersion[num164] = msg.reader().readByte();
                            }
                            break;
                        }
                    case -77:
                        {
                            short num129 = msg.reader().readShort();
                            SmallImage.newSmallVersion = new sbyte[num129];
                            SmallImage.maxSmall = num129;
                            SmallImage.imgNew = new Small[num129];
                            for (int num130 = 0; num130 < num129; num130++)
                            {
                                SmallImage.newSmallVersion[num130] = msg.reader().readByte();
                            }
                            break;
                        }
                    case -76:
                        {
                            sbyte b45 = msg.reader().readByte();
                            if (b45 == 0)
                            {
                                sbyte b46 = msg.reader().readByte();
                                if (b46 <= 0)
                                {
                                    return true;
                                }
                                Char.myCharz().arrArchive = new Archivement[b46];
                                for (int num122 = 0; num122 < b46; num122++)
                                {
                                    Char.myCharz().arrArchive[num122] = new Archivement();
                                    Char.myCharz().arrArchive[num122].info1 = num122 + 1 + ". " + msg.reader().readUTF();
                                    Char.myCharz().arrArchive[num122].info2 = msg.reader().readUTF();
                                    Char.myCharz().arrArchive[num122].money = msg.reader().readShort();
                                    Char.myCharz().arrArchive[num122].isFinish = msg.reader().readBoolean();
                                    Char.myCharz().arrArchive[num122].isRecieve = msg.reader().readBoolean();
                                }
                                GameCanvas.panel.setTypeArchivement();
                                GameCanvas.panel.show();
                            }
                            else if (b45 == 1)
                            {
                                int num123 = msg.reader().readUnsignedByte();
                                if (Char.myCharz().arrArchive[num123] != null)
                                {
                                    Char.myCharz().arrArchive[num123].isRecieve = true;
                                }
                            }
                            break;
                        }
                    case -74:
                        {
                            if (ServerListScreen.stopDownload)
                            {
                                return true;
                            }
                            if (!GameCanvas.isGetResourceFromServer())
                            {
                                Service.gI().getResource(3, null);
                                SmallImage.loadBigRMS();
                                if (!(GameCanvas.currentScreen is SplashScr))
                                {
                                    SplashScr.imgLogo = null;
                                    if (Rms.loadRMSString("acc") != null || Rms.loadRMSString("userAo" + ServerListScreen.ipSelect) != null)
                                    {
                                        LoginScr.isContinueToLogin = true;
                                    }
                                    GameCanvas.loginScr = new LoginScr();
                                    GameCanvas.loginScr.switchToMe();
                                }
                                return true;
                            }
                            bool flag7 = true;
                            sbyte b36 = msg.reader().readByte();
                            if (b36 == 0)
                            {
                                int num91 = msg.reader().readInt();
                                string text3 = Rms.loadRMSString("ResVersion");
                                int num92 = ((text3 == null || !(text3 != string.Empty)) ? (-1) : int.Parse(text3));
                                if (text3 != null)
                                {
                                    mSystem.println(num91 + ">>>strVersion: " + text3 + " >> " + num92);
                                }
                                else
                                {
                                    mSystem.println(">>>strVersion: nulll: " + num92);
                                }
                                if (Session_ME.gI().isCompareIPConnect())
                                {
                                    if (num92 == -1 || num92 != num91)
                                    {
                                        GameCanvas.serverScreen.show2();
                                    }
                                    else
                                    {
                                        Res.outz("login ngay");
                                        SmallImage.loadBigRMS();
                                        ServerListScreen.loadScreen = true;
                                        if (!(GameCanvas.currentScreen is SplashScr))
                                        {
                                            SplashScr.imgLogo = null;
                                            if (GameCanvas.currentScreen != GameCanvas.loginScr)
                                            {
                                                GameCanvas.serverScreen.switchToMe();
                                            }
                                            else
                                            {
                                                if (GameCanvas.loginScr == null)
                                                {
                                                    GameCanvas.loginScr = new LoginScr();
                                                }
                                                GameCanvas.loginScr.doLogin();
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    Session_ME.gI().close();
                                    ServerListScreen.loadScreen = true;
                                    ServerListScreen.isAutoConect = false;
                                    ServerListScreen.countDieConnect = 1000;
                                    GameCanvas.serverScreen.switchToMe();
                                }
                            }
                            if (b36 == 1)
                            {
                                ServerListScreen.strWait = mResources.downloading_data;
                                short nBig = msg.reader().readShort();
                                ServerListScreen.nBig = nBig;
                                ServerListScreen.demPercent = 0;
                                ServerListScreen.percent = 0;
                                Service.gI().getResource(2, null);
                            }
                            if (b36 == 2)
                            {
                                try
                                {
                                    isLoadingData = true;
                                    GameCanvas.endDlg();
                                    ServerListScreen.demPercent++;
                                    int totalFiles = (ServerListScreen.nBig > 0) ? ServerListScreen.nBig : 1;
                                    ServerListScreen.percent = ServerListScreen.demPercent * 100 / totalFiles;
                                    if (ServerListScreen.percent > 100)
                                    {
                                        ServerListScreen.percent = 100;
                                    }
                                    string original = msg.reader().readUTF();
                                    string[] array9 = Res.split(original, "/", 0);
                                    string filename = "x" + mGraphics.zoomLevel + array9[array9.Length - 1];
                                    int num93 = msg.reader().readInt();
                                    sbyte[] data = new sbyte[num93];
                                    msg.reader().read(ref data, 0, num93);
                                    Rms.saveRMS(filename, data);
                                }
                                catch (Exception)
                                {
                                    GameCanvas.startOK(mResources.pls_restart_game_error, 8885, null);
                                }
                            }
                            if (b36 == 3 && flag7)
                            {
                                isLoadingData = false;
                                ServerListScreen.isGetData = false;
                                int num94 = msg.reader().readInt();
                                Res.outz("last version= " + num94);
                                Rms.saveRMSString("ResVersion", num94 + string.Empty);
                                Service.gI().getResource(3, null);
                                GameCanvas.endDlg();
                                if (!(GameCanvas.currentScreen is SplashScr))
                                {
                                    SplashScr.imgLogo = null;
                                }
                                SmallImage.loadBigRMS();
                                mSystem.gcc();
                                ServerListScreen.bigOk = true;
                                ServerListScreen.loadScreen = true;
                                GameScr.gI().loadGameScr();
                                if (GameCanvas.currentScreen != GameCanvas.loginScr)
                                {
                                    GameCanvas.serverScreen.switchToMe();
                                }
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

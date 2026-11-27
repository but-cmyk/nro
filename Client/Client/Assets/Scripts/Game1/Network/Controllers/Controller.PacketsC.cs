namespace Game1
{
    using Game1.Assets.src.e;
    using Assets.src.f;
    using Game1.Assets.src.g;
    using System;
    using UnityEngine;

    public partial class Controller : IMessageHandler
    {
        private bool handlePacketGroupC(Message msg, ref Char @char, ref Mob mob, MyVector myVector, ref int num)
        {
            switch (msg.command)
            {
                    case 11:
                        {
                            GameCanvas.debug("SA9", 2);
                            int num9 = msg.reader().readUnsignedByte();
                            sbyte b6 = msg.reader().readByte();
                            sbyte[] dataBytes = NinjaUtil.readByteArray(msg);
                            Mob.EnsureMobTemplateCapacity(num9);
                            if (Mob.arrMobTemplate != null && num9 >= 0 && num9 < Mob.arrMobTemplate.Length)
                            {
                                if (Mob.arrMobTemplate[num9] == null)
                                {
                                    Mob.arrMobTemplate[num9] = new MobTemplate();
                                    Mob.arrMobTemplate[num9].mobTemplateId = (sbyte)num9;
                                }
                                if (Mob.arrMobTemplate[num9].data == null)
                                {
                                    Mob.arrMobTemplate[num9].data = new EffectData();
                                }
                                if (dataBytes != null)
                                {
                                    if (b6 != 0)
                                    {
                                        Mob.arrMobTemplate[num9].data.readDataNewBoss(dataBytes, b6);
                                    }
                                    else
                                    {
                                        Mob.arrMobTemplate[num9].data.readData(dataBytes);
                                    }
                                }
                                for (int i = 0; i < GameScr.vMob.size(); i++)
                                {
                                    mob = (Mob)GameScr.vMob.elementAt(i);
                                    if (mob != null && mob.templateId == num9 && Mob.arrMobTemplate[num9].data != null)
                                    {
                                        mob.w = Mob.arrMobTemplate[num9].data.width;
                                        mob.h = Mob.arrMobTemplate[num9].data.height;
                                    }
                                }
                            }
                            sbyte[] array2 = NinjaUtil.readByteArray(msg);
                            if (array2 != null && array2.Length > 0 && Mob.arrMobTemplate != null && num9 >= 0 && num9 < Mob.arrMobTemplate.Length && Mob.arrMobTemplate[num9] != null && Mob.arrMobTemplate[num9].data != null)
                            {
                                try
                                {
                                    Image img = Image.createImage(array2, 0, array2.Length);
                                    if (img != null)
                                    {
                                        Mob.arrMobTemplate[num9].data.img = img;
                                    }
                                }
                                catch (Exception exImg)
                                {
                                    Cout.LogError("Loi load anh MobTemplate " + num9 + ": " + exImg.ToString());
                                }
                            }
                            int num10 = msg.reader().readByte();
                            if (Mob.arrMobTemplate != null && num9 >= 0 && num9 < Mob.arrMobTemplate.Length && Mob.arrMobTemplate[num9] != null && Mob.arrMobTemplate[num9].data != null)
                            {
                                Mob.arrMobTemplate[num9].data.typeData = num10;
                            }
                            if (num10 == 1 || num10 == 2)
                            {
                                readFrameBoss(msg, num9);
                            }
                            return true;
                        }
                    case -67:
                        {
                            demCount += 1f;
                            int num168 = msg.reader().readInt();
                            sbyte[] array17 = null;
                            try
                            {
                                array17 = NinjaUtil.readByteArray(msg);
                                if (num168 >= 0)
                                {
                                    if (SmallImage.imgNew == null)
                                    {
                                        SmallImage.imgNew = new Small[(SmallImage.maxSmall > 0) ? SmallImage.maxSmall : 1000];
                                    }
                                    if (num168 >= SmallImage.imgNew.Length)
                                    {
                                        Small[] newArr = new Small[num168 + 100];
                                        Array.Copy(SmallImage.imgNew, newArr, SmallImage.imgNew.Length);
                                        SmallImage.imgNew = newArr;
                                    }
                                    Image iconImg = (array17 != null && array17.Length >= 8) ? createImage(array17) : null;
                                    if (iconImg != null && iconImg.texture != null)
                                    {
                                        if (SmallImage.imgNew[num168] == null)
                                        {
                                            SmallImage.imgNew[num168] = new Small(iconImg, num168);
                                        }
                                        else
                                        {
                                            SmallImage.imgNew[num168].img = iconImg;
                                        }
                                        if (mGraphics.zoomLevel > 1)
                                        {
                                            Rms.saveRMS(mGraphics.zoomLevel + "Small" + num168, array17);
                                        }
                                    }
                                    else
                                    {
                                        if (SmallImage.imgNew[num168] == null)
                                        {
                                            SmallImage.imgNew[num168] = new Small(SmallImage.imgEmpty, num168);
                                        }
                                        else
                                        {
                                            SmallImage.imgNew[num168].img = SmallImage.imgEmpty;
                                        }
                                    }
                                }
                            }
                            catch (Exception exIcon)
                            {
                                Cout.LogError("Loi case -67: " + exIcon.ToString());
                            }
                            break;
                        }
                    case -66:
                        {
                            try
                            {
                                short id3 = msg.reader().readShort();
                                sbyte[] data4 = NinjaUtil.readByteArray(msg);
                                EffectData effDataById = Effect.getEffDataById(id3);
                                sbyte b67 = msg.reader().readSByte();
                                if (effDataById != null)
                                {
                                    if (b67 == 0)
                                    {
                                        effDataById.readData(data4);
                                    }
                                    else
                                    {
                                        effDataById.readDataNewBoss(data4, b67);
                                    }
                                    sbyte[] array15 = NinjaUtil.readByteArray(msg);
                                    if (array15 != null && array15.Length > 0)
                                    {
                                        effDataById.img = Image.createImage(array15, 0, array15.Length);
                                    }
                                }
                                else
                                {
                                    NinjaUtil.readByteArray(msg);
                                }
                            }
                            catch (Exception exEff)
                            {
                                Cout.LogError("Loi case -66: " + exEff.ToString());
                            }
                            break;
                        }
                    case -32:
                        {
                            short num156 = msg.reader().readShort();
                            int num157 = msg.reader().readInt();
                            sbyte[] array11 = null;
                            Image image = null;
                            try
                            {
                                array11 = new sbyte[num157];
                                for (int num158 = 0; num158 < num157; num158++)
                                {
                                    array11[num158] = msg.reader().readByte();
                                }
                                image = Image.createImage(array11, 0, num157);
                                BgItem.imgNew.put(num156 + string.Empty, image);
                            }
                            catch (Exception)
                            {
                                array11 = null;
                                BgItem.imgNew.put(num156 + string.Empty, Image.createRGBImage(new int[1], 1, 1, true));
                            }
                            if (array11 != null)
                            {
                                if (mGraphics.zoomLevel > 1)
                                {
                                    Rms.saveRMS(mGraphics.zoomLevel + "bgItem" + num156, array11);
                                }
                                BgItemMn.blendcurrBg(num156, image);
                            }
                            break;
                        }
                    case 92:
                        {
                            if (GameCanvas.currentScreen == GameScr.instance)
                            {
                                GameCanvas.endDlg();
                            }
                            string text4 = msg.reader().readUTF();
                            string str2 = msg.reader().readUTF();
                            str2 = Res.changeString(str2);
                            string empty = string.Empty;
                            Char char8 = null;
                            sbyte b48 = 0;
                            if (!text4.Equals(string.Empty))
                            {
                                char8 = new Char();
                                char8.charID = msg.reader().readInt();
                                char8.head = msg.reader().readShort();
                                char8.headICON = msg.reader().readShort();
                                char8.body = msg.reader().readShort();
                                char8.bag = msg.reader().readShort();
                                char8.leg = msg.reader().readShort();
                                b48 = msg.reader().readByte();
                                char8.cName = text4;
                            }
                            empty += str2;
                            InfoDlg.hide();
                            if (text4.Equals(string.Empty))
                            {
                                if (empty.StartsWith("BOSS") || empty.StartsWith("Boss"))
                                {
                                    Game1.God.BossData.getInstance().getBOSSInfo(empty);
                                }
                                GameScr.info1.addInfo(empty, 0);
                                break;
                            }
                            GameScr.info2.addInfoWithChar(empty, char8, b48 == 0);
                            if (GameCanvas.panel.isShow && GameCanvas.panel.type == 8)
                            {
                                GameCanvas.panel.initLogMessage();
                            }
                            break;
                        }
                    case -25:
                        GameCanvas.debug("SA3", 2);
                        string notice25 = msg.reader().readUTF();
                        if (notice25.StartsWith("BOSS") || notice25.StartsWith("Boss"))
                        {
                            Game1.God.BossData.getInstance().getBOSSInfo(notice25);
                        }
                        GameScr.info1.addInfo(notice25, 0);
                        break;
                    case 94:
                        GameCanvas.debug("SA3", 2);
                        GameScr.info1.addInfo(msg.reader().readUTF(), 0);
                        break;
                    case 47:
                        GameCanvas.debug("SA4", 2);
                        GameScr.gI().resetButton();
                        break;
                    case 81:
                        {
                            GameCanvas.debug("SXX4", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isDisable = msg.reader().readBool();
                            break;
                        }
                    case 82:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isDontMove = msg.reader().readBool();
                            break;
                        }
                    case 85:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isFire = msg.reader().readBool();
                            break;
                        }
                    case 86:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isIce = msg.reader().readBool();
                            if (!mob8.isIce)
                            {
                                ServerEffect.addServerEffect(77, mob8.x, mob8.y - 9, 1);
                            }
                            break;
                        }
                    case 87:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isWind = msg.reader().readBool();
                            break;
                        }
                    case -29:
                        messageNotLogin(msg);
                        break;
                    case -28:
                        messageNotMap(msg);
                        break;
                    case -30:
                        messageSubCommand(msg);
                        break;
                    case 62:
                        GameCanvas.debug("SZ3", 2);
                        @char = GameScr.findCharInMap(msg.reader().readInt());
                        if (@char != null)
                        {
                            @char.killCharId = Char.myCharz().charID;
                            Char.myCharz().npcFocus = null;
                            Char.myCharz().mobFocus = null;
                            Char.myCharz().itemFocus = null;
                            Char.myCharz().charFocus = @char;
                            Char.isManualFocus = true;
                            GameScr.info1.addInfo(@char.cName + mResources.CUU_SAT, 0);
                        }
                        return true;
                    case 63:
                        GameCanvas.debug("SZ4", 2);
                        Char.myCharz().killCharId = msg.reader().readInt();
                        Char.myCharz().npcFocus = null;
                        Char.myCharz().mobFocus = null;
                        Char.myCharz().itemFocus = null;
                        Char.myCharz().charFocus = GameScr.findCharInMap(Char.myCharz().killCharId);
                        Char.isManualFocus = true;
                        return true;
                    case 64:
                        GameCanvas.debug("SZ5", 2);
                        @char = Char.myCharz();
                        try
                        {
                            @char = GameScr.findCharInMap(msg.reader().readInt());
                        }
                        catch (Exception ex2)
                        {
                            Cout.println("Loi CLEAR_CUU_SAT " + ex2.ToString());
                        }
                        @char.killCharId = -9999;
                        break;
                    case 57:
                        {
                            GameCanvas.debug("SZ6", 2);
                            MyVector myVector2 = new MyVector();
                            myVector2.addElement(new Command(msg.reader().readUTF(), GameCanvas.instance, 88817, null));
                            GameCanvas.menu.startAt(myVector2, 3);
                            break;
                        }
                    case 88:
                        {
                            string info4 = msg.reader().readUTF();
                            short num172 = msg.reader().readShort();
                            GameCanvas.inputDlg.show(info4, new Command(mResources.ACCEPT, GameCanvas.instance, 88818, num172), TField.INPUT_TYPE_ANY);
                            break;
                        }
                    case 27:
                        {
                            myVector = new MyVector();
                            string text7 = msg.reader().readUTF();
                            int num169 = msg.reader().readByte();
                            for (int num170 = 0; num170 < num169; num170++)
                            {
                                string caption4 = msg.reader().readUTF();
                                short num171 = msg.reader().readShort();
                                myVector.addElement(new Command(caption4, GameCanvas.instance, 88819, num171));
                            }
                            GameCanvas.menu.startWithoutCloseButton(myVector, 3);
                            break;
                        }
                    case 33:
                        {
                            GameCanvas.debug("SA51", 2);
                            InfoDlg.hide();
                            GameCanvas.clearKeyHold();
                            GameCanvas.clearKeyPressed();
                            myVector = new MyVector();
                            try
                            {
                                while (msg.reader().available() > 0)
                                {
                                    string caption3 = msg.reader().readUTF();
                                    myVector.addElement(new Command(caption3, GameCanvas.instance, 88822, null));
                                }
                            }
                            catch (Exception ex24)
                            {
                                Cout.println("Loi OPEN_UI_MENU " + ex24.ToString());
                            }
                            if (Char.myCharz().npcFocus == null)
                            {
                                return true;
                            }
                            for (int num165 = 0; num165 < Char.myCharz().npcFocus.template.menu.Length; num165++)
                            {
                                string[] array16 = Char.myCharz().npcFocus.template.menu[num165];
                                myVector.addElement(new Command(array16[0], GameCanvas.instance, 88820, array16));
                            }
                            GameCanvas.menu.startAt(myVector, 3);
                            break;
                        }
                    case 40:
                        {
                            GameCanvas.debug("SA52", 2);
                            GameCanvas.taskTick = 150;
                            short taskId = msg.reader().readShort();
                            sbyte index3 = msg.reader().readByte();
                            string str3 = msg.reader().readUTF();
                            str3 = Res.changeString(str3);
                            string str4 = msg.reader().readUTF();
                            str4 = Res.changeString(str4);
                            string[] array12 = new string[msg.reader().readByte()];
                            string[] array13 = new string[array12.Length];
                            GameScr.tasks = new int[array12.Length];
                            GameScr.mapTasks = new int[array12.Length];
                            short[] array14 = new short[array12.Length];
                            short count = -1;
                            for (int num159 = 0; num159 < array12.Length; num159++)
                            {
                                string str5 = msg.reader().readUTF();
                                str5 = Res.changeString(str5);
                                GameScr.tasks[num159] = msg.reader().readByte();
                                GameScr.mapTasks[num159] = msg.reader().readShort();
                                string str6 = msg.reader().readUTF();
                                str6 = Res.changeString(str6);
                                array14[num159] = -1;
                                array12[num159] = str5;
                                if (!str6.Equals(string.Empty))
                                {
                                    array13[num159] = str6;
                                }
                            }
                            try
                            {
                                count = msg.reader().readShort();
                                for (int num160 = 0; num160 < array12.Length; num160++)
                                {
                                    array14[num160] = msg.reader().readShort();
                                }
                            }
                            catch (Exception ex23)
                            {
                                Cout.println("Loi TASK_GET " + ex23.ToString());
                            }
                            Char.myCharz().taskMaint = new Task(taskId, index3, str3, str4, array12, array14, count, array13);
                            if (Char.myCharz().npcFocus != null)
                            {
                                Npc.clearEffTask();
                            }
                            Char.taskAction(true);
                            break;
                        }
                    case 41:
                        GameCanvas.debug("SA53", 2);
                        GameCanvas.taskTick = 100;
                        Res.outz("TASK NEXT");
                        Char.myCharz().taskMaint.index++;
                        Char.myCharz().taskMaint.count = 0;
                        Npc.clearEffTask();
                        Char.taskAction(true);
                        break;
                    case 50:
                        {
                            try
                            {
                                sbyte b64 = msg.reader().readByte();
                                Panel.vGameInfo.removeAllElements();
                                for (int num155 = 0; num155 < b64; num155++)
                                {
                                    GameInfo gameInfo = new GameInfo();
                                    gameInfo.id = msg.reader().readShort();
                                    gameInfo.main = msg.reader().readUTF();
                                    gameInfo.content = msg.reader().readUTF();
                                    Panel.vGameInfo.addElement(gameInfo);
                                    bool hasRead = ((Rms.loadRMSInt(gameInfo.id + string.Empty) != -1) ? true : false);
                                    gameInfo.hasRead = hasRead;
                                }
                            }
                            catch (Exception ex)
                            {
                                Cout.println("Loi read case 50: " + ex.ToString());
                            }
                            break;
                        }
                    case 43:
                        GameCanvas.taskTick = 50;
                        GameCanvas.debug("SA55", 2);
                        Char.myCharz().taskMaint.count = msg.reader().readShort();
                        if (Char.myCharz().npcFocus != null)
                        {
                            Npc.clearEffTask();
                        }
                        try
                        {
                            short x_hint = msg.reader().readShort();
                            short y_hint = msg.reader().readShort();
                            if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId > 2)
                            {
                                x_hint = 0;
                                y_hint = 0;
                            }
                            Char.myCharz().x_hint = x_hint;
                            Char.myCharz().y_hint = y_hint;
                        }
                        catch (Exception catchErr_1)
                        {
                            Cout.LogWarning("[Controller.PacketsC.cs] " + catchErr_1.Message);
                        }
                        break;
                    case 90:
                        GameCanvas.debug("SA577", 2);
                        requestItemPlayer(msg);
                        break;
                    case 29:
                        GameCanvas.debug("SA58", 2);
                        GameScr.gI().openUIZone(msg);
                        break;
                    case 69:
                        SoundMn.IsDelAcc = ((msg.reader().readByte() != 0) ? true : false);
                        break;
                    case 7:
                        {
                            sbyte type = msg.reader().readByte();
                            short id2 = msg.reader().readShort();
                            string info2 = msg.reader().readUTF();
                            GameCanvas.panel.saleRequest(type, info2, id2);
                            return true;
                        }
                    case -24:
                        Res.outz("***************MAP_INFO**************");
                        GameScr.isPickNgocRong = false;
                        Char.isLoadingMap = true;
                        Cout.println("GET MAP INFO");
                        GameScr.gI().magicTree = null;
                        GameCanvas.isLoading = true;
                        GameCanvas.debug("SA75", 2);
                        GameScr.resetAllvector();
                        GameCanvas.endDlg();
                        TileMap.vGo.removeAllElements();
                        PopUp.vPopups.removeAllElements();
                        mSystem.gcc();
                        TileMap.mapID = msg.reader().readUnsignedByte();
                        TileMap.planetID = msg.reader().readByte();
                        TileMap.tileID = msg.reader().readByte();
                        TileMap.bgID = msg.reader().readByte();
                        GameScr.isPaint_CT = TileMap.mapID != 170;
                        Cout.println("load planet from server: " + TileMap.planetID + "bgType= " + TileMap.bgType + ".............................");
                        TileMap.typeMap = msg.reader().readByte();
                        TileMap.mapName = msg.reader().readUTF();
                        TileMap.zoneID = msg.reader().readByte();
                        GameCanvas.debug("SA75x1", 2);
                        try
                        {
                            TileMap.loadMapFromResource(TileMap.mapID);
                        }
                        catch (Exception)
                        {
                            Service.gI().requestMaptemplate(TileMap.mapID);
                            messWait = msg;
                            break;
                        }
                        loadInfoMap(msg);
                        try
                        {
                            sbyte b32 = msg.reader().readByte();
                            TileMap.isMapDouble = ((b32 != 0) ? true : false);
                        }
                        catch (Exception catchErr_2)
                        {
                            Cout.LogWarning("[Controller.PacketsC.cs] " + catchErr_2.Message);
                        }
                        GameScr.cmx = GameScr.cmtoX;
                        GameScr.cmy = GameScr.cmtoY;
                        GameCanvas.isRequestMapID = 2;
                        GameCanvas.waitingTimeChangeMap = mSystem.currentTimeMillis() + 1000;
                        break;
                    case -31:
                        {
                            TileMap.vItemBg.removeAllElements();
                            short num75 = msg.reader().readShort();
                            Res.err("[ITEM_BACKGROUND] nItem= " + num75);
                            for (int num76 = 0; num76 < num75; num76++)
                            {
                                BgItem bgItem = new BgItem();
                                bgItem.id = num76;
                                bgItem.idImage = msg.reader().readShort();
                                bgItem.layer = msg.reader().readByte();
                                bgItem.dx = msg.reader().readShort();
                                bgItem.dy = msg.reader().readShort();
                                sbyte b31 = msg.reader().readByte();
                                bgItem.tileX = new int[b31];
                                bgItem.tileY = new int[b31];
                                for (int num77 = 0; num77 < b31; num77++)
                                {
                                    bgItem.tileX[num77] = msg.reader().readByte();
                                    bgItem.tileY[num77] = msg.reader().readByte();
                                }
                                TileMap.vItemBg.addElement(bgItem);
                            }
                            break;
                        }
                    case -4:
                        {
                            GameCanvas.debug("SA76", 2);
                            @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                return true;
                            }
                            int idSkillPaint = msg.reader().readUnsignedByte();
                            @char.setSkillPaint(GameScr.getSkillPaint(idSkillPaint), ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2) ? 0 : 1);
                            GameCanvas.debug("SA76v2", 2);
                            @char.attMobs = new Mob[msg.reader().readByte()];
                            for (int num25 = 0; num25 < @char.attMobs.Length; num25++)
                            {
                                Mob mob3 = (Mob)GameScr.vMob.elementAt(msg.reader().readByte());
                                @char.attMobs[num25] = mob3;
                                if (num25 == 0)
                                {
                                    if (@char.cx <= mob3.x)
                                    {
                                        @char.cdir = 1;
                                    }
                                    else
                                    {
                                        @char.cdir = -1;
                                    }
                                }
                            }
                            GameCanvas.debug("SA76v3", 2);
                            @char.charFocus = null;
                            @char.mobFocus = @char.attMobs[0];
                            Char[] array = new Char[10];
                            num = 0;
                            try
                            {
                                for (num = 0; num < array.Length; num++)
                                {
                                    int num18 = msg.reader().readInt();
                                    Char char5 = (array[num] = ((num18 != Char.myCharz().charID) ? GameScr.findCharInMap(num18) : Char.myCharz()));
                                    if (num == 0)
                                    {
                                        if (@char.cx <= char5.cx)
                                        {
                                            @char.cdir = 1;
                                        }
                                        else
                                        {
                                            @char.cdir = -1;
                                        }
                                    }
                                }
                            }
                            catch (Exception ex6)
                            {
                                Cout.println("Loi PLAYER_ATTACK_N_P " + ex6.ToString());
                            }
                            GameCanvas.debug("SA76v4", 2);
                            if (num > 0)
                            {
                                @char.attChars = new Char[num];
                                for (num = 0; num < @char.attChars.Length; num++)
                                {
                                    @char.attChars[num] = array[num];
                                }
                                @char.charFocus = @char.attChars[0];
                                @char.mobFocus = null;
                            }
                            GameCanvas.debug("SA76v5", 2);
                            break;
                        }
                    case 54:
                        {
                            @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                return true;
                            }
                            int num17 = msg.reader().readUnsignedByte();
                            SkillPaint sp54 = null;
                            if (GameScr.sks != null && num17 >= 0 && num17 < GameScr.sks.Length)
                            {
                                sp54 = GameScr.sks[num17];
                            }
                            if (sp54 == null && GameScr.sks != null && GameScr.sks.Length > 0)
                            {
                                sp54 = GameScr.sks[0];
                            }
                            if ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2)
                            {
                                @char.setSkillPaint(sp54, 0);
                            }
                            else
                            {
                                @char.setSkillPaint(sp54, 1);
                            }
                            Mob[] array3 = new Mob[10];
                            num = 0;
                            try
                            {
                                for (num = 0; num < array3.Length; num++)
                                {
                                    Mob mob2 = (array3[num] = (Mob)GameScr.vMob.elementAt(msg.reader().readByte()));
                                    if (num == 0)
                                    {
                                        if (@char.cx <= mob2.x)
                                        {
                                            @char.cdir = 1;
                                        }
                                        else
                                        {
                                            @char.cdir = -1;
                                        }
                                    }
                                }
                            }
                            catch (Exception catchErr_3)
                            {
                                Cout.LogWarning("[Controller.PacketsC.cs] " + catchErr_3.Message);
                            }
                            if (num > 0)
                            {
                                @char.attMobs = new Mob[num];
                                for (num = 0; num < @char.attMobs.Length; num++)
                                {
                                    @char.attMobs[num] = array3[num];
                                }
                                @char.charFocus = null;
                                @char.mobFocus = @char.attMobs[0];
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

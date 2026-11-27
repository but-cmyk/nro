namespace Game1
{
    using Game1.Assets.src.e;
    using Assets.src.f;
    using Game1.Assets.src.g;
    using System;
    using UnityEngine;

    public partial class Controller : IMessageHandler
    {
        private bool handlePacketGroupD(Message msg, ref Char @char, ref Mob mob, MyVector myVector, ref int num)
        {
            switch (msg.command)
            {
                    case -60:
                        {
                            GameCanvas.debug("SA7666", 2);
                            int num2 = msg.reader().readInt();
                            int num3 = -1;
                            if (num2 != Char.myCharz().charID)
                            {
                                Char char2 = GameScr.findCharInMap(num2);
                                if (char2 == null)
                                {
                                    return true;
                                }
                                if (char2.currentMovePoint != null)
                                {
                                    char2.createShadow(char2.cx, char2.cy, 10);
                                    char2.cx = char2.currentMovePoint.xEnd;
                                    char2.cy = char2.currentMovePoint.yEnd;
                                }
                                int num4 = msg.reader().readUnsignedByte();
                                char2.setSkillPaint(GameScr.getSkillPaint(num4), ((TileMap.tileTypeAtPixel(char2.cx, char2.cy) & 2) == 2) ? 0 : 1);
                                sbyte b = msg.reader().readByte();
                                Char[] array = new Char[b];
                                for (num = 0; num < array.Length; num++)
                                {
                                    num3 = msg.reader().readInt();
                                    Char char3;
                                    if (num3 == Char.myCharz().charID)
                                    {
                                        char3 = Char.myCharz();
                                        if (!GameScr.isChangeZone && GameScr.isAutoPlay && GameScr.canAutoPlay)
                                        {
                                            Service.gI().requestChangeZone(-1, -1);
                                            GameScr.isChangeZone = true;
                                        }
                                    }
                                    else
                                    {
                                        char3 = GameScr.findCharInMap(num3);
                                    }
                                    array[num] = char3;
                                    if (num == 0)
                                    {
                                        if (char2.cx <= char3.cx)
                                        {
                                            char2.cdir = 1;
                                        }
                                        else
                                        {
                                            char2.cdir = -1;
                                        }
                                    }
                                }
                                if (num > 0)
                                {
                                    char2.attChars = new Char[num];
                                    for (num = 0; num < char2.attChars.Length; num++)
                                    {
                                        char2.attChars[num] = array[num];
                                    }
                                    char2.mobFocus = null;
                                    char2.charFocus = char2.attChars[0];
                                }
                            }
                            else
                            {
                                sbyte b2 = msg.reader().readByte();
                                sbyte b3 = msg.reader().readByte();
                                num3 = msg.reader().readInt();
                            }
                            try
                            {
                                sbyte b4 = msg.reader().readByte();
                                Res.outz("isRead continue = " + b4);
                                if (b4 != 1)
                                {
                                    break;
                                }
                                sbyte b5 = msg.reader().readByte();
                                Res.outz("type skill = " + b5);
                                if (num3 == Char.myCharz().charID)
                                {
                                    bool flag = false;
                                    @char = Char.myCharz();
                                    long num5 = msg.readInt3Byte();
                                    Res.outz("dame hit = " + num5);
                                    @char.isDie = msg.reader().readBoolean();
                                    if (@char.isDie)
                                    {
                                        Char.isLockKey = true;
                                    }
                                    Res.outz("isDie=" + @char.isDie + "---------------------------------------");
                                    long num6 = 0;
                                    flag = (@char.isCrit = msg.reader().readBoolean());
                                    @char.isMob = false;
                                    num5 = (@char.damHP = num5 + num6);
                                    if (b5 == 0)
                                    {
                                        @char.doInjure(num5, 0, flag, false);
                                    }
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num3);
                                    if (@char == null)
                                    {
                                        return true;
                                    }
                                    bool flag2 = false;
                                    long num7 = msg.readInt3Byte();
                                    Res.outz("dame hit= " + num7);
                                    @char.isDie = msg.reader().readBoolean();
                                    Res.outz("isDie=" + @char.isDie + "---------------------------------------");
                                    long num8 = 0;
                                    flag2 = (@char.isCrit = msg.reader().readBoolean());
                                    @char.isMob = false;
                                    num7 = (@char.damHP = num7 + num8);
                                    if (b5 == 0)
                                    {
                                        @char.doInjure(num7, 0, flag2, false);
                                    }
                                }
                            }
                            catch (Exception catchErr_1)
                            {
                                Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_1.Message);
                            }
                            break;
                        }
                }
                switch (msg.command)
                {
                    case -2:
                        {
                            GameCanvas.debug("SA77", 22);
                            int num197 = msg.reader().readInt();
                            Char.myCharz().yen += num197;
                            GameScr.startFlyText((num197 <= 0) ? (string.Empty + num197) : ("+" + num197), Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 10, 0, -2, mFont.YELLOW);
                            break;
                        }
                    case 95:
                        {
                            GameCanvas.debug("SA77", 22);
                            int num184 = msg.reader().readInt();
                            Char.myCharz().xu += num184;
                            Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                            GameScr.startFlyText((num184 <= 0) ? (string.Empty + num184) : ("+" + num184), Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 10, 0, -2, mFont.YELLOW);
                            break;
                        }
                    case 96:
                        GameCanvas.debug("SA77a", 22);
                        Char.myCharz().taskOrders.addElement(new TaskOrder(msg.reader().readByte(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readUTF(), msg.reader().readUTF(), msg.reader().readByte(), msg.reader().readByte()));
                        break;
                    case 97:
                        {
                            sbyte b75 = msg.reader().readByte();
                            for (int num190 = 0; num190 < Char.myCharz().taskOrders.size(); num190++)
                            {
                                TaskOrder taskOrder = (TaskOrder)Char.myCharz().taskOrders.elementAt(num190);
                                if (taskOrder.taskId == b75)
                                {
                                    taskOrder.count = msg.reader().readShort();
                                    break;
                                }
                            }
                            break;
                        }
                    case -1:
                        {
                            GameCanvas.debug("SA77", 222);
                            int num196 = msg.reader().readInt();
                            Char.myCharz().xu += num196;
                            Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                            Char.myCharz().yen -= num196;
                            GameScr.startFlyText("+" + num196, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 10, 0, -2, mFont.YELLOW);
                            break;
                        }
                    case -3:
                        {
                            GameCanvas.debug("SA78", 2);
                            sbyte b71 = msg.reader().readByte();
                            int num181 = msg.reader().readInt();
                            if (b71 == 0)
                            {
                                Char.myCharz().cPower += num181;
                            }
                            if (b71 == 1)
                            {
                                Char.myCharz().cTiemNang += num181;
                            }
                            if (b71 == 2)
                            {
                                Char.myCharz().cPower += num181;
                                Char.myCharz().cTiemNang += num181;
                            }
                            try
                            {
                                if (msg.reader().available() >= 16)
                                {
                                    Char.myCharz().cPower = msg.reader().readLong();
                                    Char.myCharz().cTiemNang = msg.reader().readLong();
                                }
                                else if (msg.reader().available() >= 8)
                                {
                                    Char.myCharz().cPower = msg.reader().readLong();
                                }
                            }
                            catch (Exception catchErr_2)
                            {
                                Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_2.Message);
                            }
                            Char.myCharz().applyCharLevelPercent();
                            if (Char.myCharz().cTypePk != 3)
                            {
                                GameScr.startFlyText(((num181 <= 0) ? string.Empty : "+") + num181, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -4, mFont.GREEN);
                                if (num181 > 0 && Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 5002)
                                {
                                    ServerEffect.addServerEffect(55, Char.myCharz().petFollow.cmx, Char.myCharz().petFollow.cmy, 1);
                                    ServerEffect.addServerEffect(55, Char.myCharz().cx, Char.myCharz().cy, 1);
                                }
                            }
                            break;
                        }
                    case -73:
                        {
                            sbyte b77 = msg.reader().readByte();
                            for (int num195 = 0; num195 < GameScr.vNpc.size(); num195++)
                            {
                                Npc npc7 = (Npc)GameScr.vNpc.elementAt(num195);
                                if (npc7.template.npcTemplateId == b77)
                                {
                                    sbyte b78 = msg.reader().readByte();
                                    if (b78 == 0)
                                    {
                                        npc7.isHide = true;
                                    }
                                    else
                                    {
                                        npc7.isHide = false;
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    case -5:
                        {
                            GameCanvas.debug("SA79", 2);
                            int charID = msg.reader().readInt();
                            int num186 = msg.reader().readInt();
                            Char char16;
                            if (num186 != -100)
                            {
                                char16 = new Char();
                                char16.charID = charID;
                                char16.clanID = num186;
                            }
                            else
                            {
                                char16 = new Mabu();
                                char16.charID = charID;
                                char16.clanID = num186;
                            }
                            if (char16.clanID == -2)
                            {
                                char16.isCopy = true;
                            }
                            if (readCharInfo(char16, msg))
                            {
                                sbyte b73 = msg.reader().readByte();
                                if (char16.cy <= 10 && b73 != 0 && b73 != 2)
                                {
                                    Res.outz("nhân vật bay trên trời xuống x= " + char16.cx + " y= " + char16.cy);
                                    Teleport teleport2 = new Teleport(char16.cx, char16.cy, char16.head, char16.cdir, 1, false, (b73 != 1) ? b73 : char16.cgender);
                                    teleport2.id = char16.charID;
                                    char16.isTeleport = true;
                                    Teleport.addTeleport(teleport2);
                                }
                                if (b73 == 2)
                                {
                                    char16.show();
                                }
                                for (int num187 = 0; num187 < GameScr.vMob.size(); num187++)
                                {
                                    Mob mob10 = (Mob)GameScr.vMob.elementAt(num187);
                                    if (mob10 != null && mob10.isMobMe && mob10.mobId == char16.charID)
                                    {
                                        Res.outz("co 1 con quai");
                                        char16.mobMe = mob10;
                                        char16.mobMe.x = char16.cx;
                                        char16.mobMe.y = char16.cy - 40;
                                        break;
                                    }
                                }
                                if (GameScr.findCharInMap(char16.charID) == null)
                                {
                                    GameScr.vCharInMap.addElement(char16);
                                }
                                char16.isMonkey = msg.reader().readByte();
                                short num188 = msg.reader().readShort();
                                Res.outz("mount id= " + num188 + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                                if (num188 != -1)
                                {
                                    char16.isHaveMount = true;
                                    switch (num188)
                                    {
                                        case 346:
                                        case 347:
                                        case 348:
                                            char16.isMountVip = false;
                                            break;
                                        case 349:
                                        case 350:
                                        case 351:
                                            char16.isMountVip = true;
                                            break;
                                        case 396:
                                            char16.isEventMount = true;
                                            break;
                                        case 532:
                                            char16.isSpeacialMount = true;
                                            break;
                                        default:
                                            if (num188 >= Char.ID_NEW_MOUNT)
                                            {
                                                char16.idMount = num188;
                                            }
                                            break;
                                    }
                                }
                                else
                                {
                                    char16.isHaveMount = false;
                                }
                            }
                            sbyte b74 = msg.reader().readByte();
                            Res.outz("addplayer:   " + b74);
                            char16.cFlag = b74;
                            char16.isNhapThe = msg.reader().readByte() == 1;
                            try
                            {
                                char16.idAuraEff = msg.reader().readShort();
                                char16.idEff_Set_Item = msg.reader().readSByte();
                                char16.idHat = msg.reader().readShort();
                                if (char16.bag >= 201 && char16.bag < 255)
                                {
                                    Effect effect3 = new Effect(char16.bag, char16, 2, -1, 10, 1);
                                    effect3.typeEff = 5;
                                    char16.addEffChar(effect3);
                                }
                                else
                                {
                                    for (int num189 = 0; num189 < 54; num189++)
                                    {
                                        char16.removeEffChar(0, 201 + num189);
                                    }
                                }
                            }
                            catch (Exception ex38)
                            {
                                Res.outz("cmd: -5 err: " + ex38.StackTrace);
                            }
                            GameScr.gI().getFlagImage(char16.charID, char16.cFlag);
                            break;
                        }
                    case -7:
                        {
                            GameCanvas.debug("SA80", 2);
                            int num179 = msg.reader().readInt();
                            short toX = msg.reader().readShort();
                            short toY = msg.reader().readShort();
                            for (int num182 = 0; num182 < GameScr.vCharInMap.size(); num182++)
                            {
                                Char char15 = (Char)GameScr.vCharInMap.elementAt(num182);
                                if (char15 != null && char15.charID == num179)
                                {
                                    GameCanvas.debug("SA8x2y" + num182, 2);
                                    char15.moveTo(toX, toY, 0);
                                    char15.lastUpdateTime = mSystem.currentTimeMillis();
                                    break;
                                }
                            }
                            GameCanvas.debug("SA80x3", 2);
                            break;
                        }
                    case -6:
                        {
                            GameCanvas.debug("SA81", 2);
                            int num179 = msg.reader().readInt();
                            for (int num180 = 0; num180 < GameScr.vCharInMap.size(); num180++)
                            {
                                Char char14 = (Char)GameScr.vCharInMap.elementAt(num180);
                                if (char14 != null && char14.charID == num179)
                                {
                                    if (!char14.isInvisiblez && !char14.isUsePlane)
                                    {
                                        ServerEffect.addServerEffect(60, char14.cx, char14.cy, 1);
                                    }
                                    if (!char14.isUsePlane)
                                    {
                                        GameScr.vCharInMap.removeElementAt(num180);
                                    }
                                    return true;
                                }
                            }
                            break;
                        }
                    case -13:
                        {
                            GameCanvas.debug("SA82", 2);
                            int num191 = msg.reader().readUnsignedByte();
                            if (num191 > GameScr.vMob.size() - 1 || num191 < 0)
                            {
                                return true;
                            }
                            Mob mob9 = (Mob)GameScr.vMob.elementAt(num191);
                            mob9.sys = msg.reader().readByte();
                            mob9.levelBoss = msg.reader().readByte();
                            if (mob9.levelBoss != 0)
                            {
                                mob9.typeSuperEff = Res.random(0, 3);
                            }
                            mob9.x = mob9.xFirst;
                            mob9.y = mob9.yFirst;
                            mob9.status = 5;
                            mob9.injureThenDie = false;
                            mob9.hp = msg.reader().readInt();
                            mob9.maxHp = mob9.hp;
                            mob9.updateHp_bar();
                            ServerEffect.addServerEffect(60, mob9.x, mob9.y, 1);
                            break;
                        }
                    case -75:
                        {
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception catchErr_3)
                            {
                                Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_3.Message);
                            }
                            if (mob9 != null)
                            {
                                mob9.levelBoss = msg.reader().readByte();
                                if (mob9.levelBoss > 0)
                                {
                                    mob9.typeSuperEff = Res.random(0, 3);
                                }
                            }
                            break;
                        }
                    case -9:
                        {
                            GameCanvas.debug("SA83", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception catchErr_4)
                            {
                                Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_4.Message);
                            }
                            GameCanvas.debug("SA83v1", 2);
                            if (mob9 != null)
                            {
                                mob9.hp = msg.readInt3Byte();
                                mob9.updateHp_bar();
                                long num183 = msg.readInt3Byte();
                                if (num183 == 1)
                                {
                                    return true;
                                }
                                if (num183 > 1)
                                {
                                    mob9.setInjure();
                                }
                                bool flag11 = false;
                                try
                                {
                                    flag11 = msg.reader().readBoolean();
                                }
                                catch (Exception catchErr_5)
                                {
                                    Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_5.Message);
                                }
                                sbyte b72 = msg.reader().readByte();
                                if (b72 != -1)
                                {
                                    EffecMn.addEff(new Effect(b72, mob9.x, mob9.getY(), 3, 1, -1));
                                }
                                GameCanvas.debug("SA83v2", 2);
                                if (flag11)
                                {
                                    GameScr.startFlyText("-" + num183, mob9.x, mob9.getY() - mob9.getH(), 0, -2, mFont.FATAL);
                                }
                                else if (num183 == 0)
                                {
                                    mob9.x = mob9.xFirst;
                                    mob9.y = mob9.yFirst;
                                    GameScr.startFlyText(mResources.miss, mob9.x, mob9.getY() - mob9.getH(), 0, -2, mFont.MISS);
                                }
                                else if (num183 > 1)
                                {
                                    GameScr.startFlyText("-" + num183, mob9.x, mob9.getY() - mob9.getH(), 0, -2, mFont.ORANGE);
                                }
                            }
                            GameCanvas.debug("SA83v3", 2);
                            break;
                        }
                    case 45:
                        {
                            GameCanvas.debug("SA84", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception ex29)
                            {
                                Cout.println("Loi tai NPC_MISS  " + ex29.ToString());
                            }
                            if (mob9 != null)
                            {
                                mob9.hp = msg.reader().readInt();
                                mob9.updateHp_bar();
                                GameScr.startFlyText(mResources.miss, mob9.x, mob9.y - mob9.h, 0, -2, mFont.MISS);
                            }
                            break;
                        }
                    case -12:
                        {
                            Res.outz("SERVER SEND MOB DIE");
                            GameCanvas.debug("SA85", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception)
                            {
                                Cout.println("LOi tai NPC_DIE cmd " + msg.command);
                            }
                            if (mob9 == null || mob9.status == 0 || mob9.status == 0)
                            {
                                break;
                            }
                            mob9.startDie();
                            try
                            {
                                long num192 = msg.readInt3Byte();
                                if (msg.reader().readBool())
                                {
                                    GameScr.startFlyText("-" + num192, mob9.x, mob9.y - mob9.h, 0, -2, mFont.FATAL);
                                }
                                else
                                {
                                    GameScr.startFlyText("-" + num192, mob9.x, mob9.y - mob9.h, 0, -2, mFont.ORANGE);
                                }
                                sbyte b76 = msg.reader().readByte();
                                for (int num193 = 0; num193 < b76; num193++)
                                {
                                    ItemMap itemMap6 = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), mob9.x, mob9.y, msg.reader().readShort(), msg.reader().readShort());
                                    int num194 = (itemMap6.playerId = msg.reader().readInt());
                                    Res.outz("playerid= " + num194 + " my id= " + Char.myCharz().charID);
                                    GameScr.vItemMap.addElement(itemMap6);
                                    if (Res.abs(itemMap6.y - Char.myCharz().cy) < 24 && Res.abs(itemMap6.x - Char.myCharz().cx) < 24)
                                    {
                                        Char.myCharz().charFocus = null;
                                    }
                                }
                            }
                            catch (Exception catchErr_6)
                            {
                                Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_6.Message);
                            }
                            break;
                        }
                    case 74:
                        {
                            GameCanvas.debug("SA85", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception)
                            {
                                Cout.println("Loi tai NPC CHANGE " + msg.command);
                            }
                            if (mob9 != null && mob9.status != 0 && mob9.status != 0)
                            {
                                mob9.status = 0;
                                ServerEffect.addServerEffect(60, mob9.x, mob9.y, 1);
                                ItemMap itemMap5 = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), mob9.x, mob9.y, msg.reader().readShort(), msg.reader().readShort());
                                GameScr.vItemMap.addElement(itemMap5);
                                if (Res.abs(itemMap5.y - Char.myCharz().cy) < 24 && Res.abs(itemMap5.x - Char.myCharz().cx) < 24)
                                {
                                    Char.myCharz().charFocus = null;
                                }
                            }
                            break;
                        }
                    case -11:
                        {
                            GameCanvas.debug("SA86", 2);
                            Mob mob9 = null;
                            try
                            {
                                int index4 = msg.reader().readUnsignedByte();
                                mob9 = (Mob)GameScr.vMob.elementAt(index4);
                            }
                            catch (Exception ex27)
                            {
                                Res.outz("Loi tai NPC_ATTACK_ME " + msg.command + " err= " + ex27.StackTrace);
                            }
                            if (mob9 != null)
                            {
                                Char.myCharz().isDie = false;
                                Char.isLockKey = false;
                                long num176 = msg.readInt3Byte();
                                long num177;
                                try
                                {
                                    num177 = msg.readInt3Byte();
                                }
                                catch (Exception)
                                {
                                    num177 = 0L;
                                }
                                if (mob9.isBusyAttackSomeOne)
                                {
                                    Char.myCharz().doInjure(num176, num177, false, true);
                                    break;
                                }
                                mob9.dame = num176;
                                mob9.dameMp = num177;
                                mob9.setAttack(Char.myCharz());
                            }
                            break;
                        }
                    case -10:
                        {
                            GameCanvas.debug("SA87", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception catchErr_7)
                            {
                                Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_7.Message);
                            }
                            GameCanvas.debug("SA87x1", 2);
                            if (mob9 != null)
                            {
                                GameCanvas.debug("SA87x2", 2);
                                @char = GameScr.findCharInMap(msg.reader().readInt());
                                if (@char == null)
                                {
                                    return true;
                                }
                                GameCanvas.debug("SA87x3", 2);
                                long num185 = msg.readInt3Byte();
                                mob9.dame = @char.cHP - num185;
                                @char.cHPNew = num185;
                                GameCanvas.debug("SA87x4", 2);
                                try
                                {
                                    @char.cMP = msg.readInt3Byte();
                                }
                                catch (Exception catchErr_8)
                                {
                                    Cout.LogWarning("[Controller.PacketsD.cs] " + catchErr_8.Message);
                                }
                                GameCanvas.debug("SA87x5", 2);
                                if (mob9.isBusyAttackSomeOne)
                                {
                                    @char.doInjure(mob9.dame, 0, false, true);
                                }
                                else
                                {
                                    mob9.setAttack(@char);
                                }
                                GameCanvas.debug("SA87x6", 2);
                            }
                            break;
                        }
                    case -17:
                        GameCanvas.debug("SA88", 2);
                        Char.myCharz().meDead = true;
                        Char.myCharz().cPk = msg.reader().readByte();
                        Char.myCharz().startDie(msg.reader().readShort(), msg.reader().readShort());
                        try
                        {
                            if (msg.reader().available() >= 8)
                            {
                                Char.myCharz().cPower = msg.reader().readLong();
                                Char.myCharz().applyCharLevelPercent();
                            }
                        }
                        catch (Exception)
                        {
                            Cout.println("Loi tai ME_DIE " + msg.command);
                        }
                        Char.myCharz().countKill = 0;
                        break;
                    case 66:
                        Res.outz("ME DIE XP DOWN NOT IMPLEMENT YET!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        break;
                    case -8:
                        GameCanvas.debug("SA89", 2);
                        @char = GameScr.findCharInMap(msg.reader().readInt());
                        if (@char == null)
                        {
                            return true;
                        }
                        @char.cPk = msg.reader().readByte();
                        @char.waitToDie(msg.reader().readShort(), msg.reader().readShort());
                        break;
                    case -16:
                        GameCanvas.debug("SA90", 2);
                        if (Char.myCharz().wdx != 0 || Char.myCharz().wdy != 0)
                        {
                            Char.myCharz().cx = Char.myCharz().wdx;
                            Char.myCharz().cy = Char.myCharz().wdy;
                            Char.myCharz().wdx = (Char.myCharz().wdy = 0);
                        }
                        Char.myCharz().liveFromDead();
                        Char.myCharz().isLockMove = false;
                        Char.myCharz().meDead = false;
                        break;
                    case 44:
                        {
                            GameCanvas.debug("SA91", 2);
                            int num178 = msg.reader().readInt();
                            string text8 = msg.reader().readUTF();
                            Res.outz("user id= " + num178 + " text= " + text8);
                            @char = ((Char.myCharz().charID != num178) ? GameScr.findCharInMap(num178) : Char.myCharz());
                            if (@char == null)
                            {
                                return true;
                            }
                            @char.addInfo(text8);
                            break;
                        }
                    case 18:
                        {
                            sbyte b70 = msg.reader().readByte();
                            for (int num175 = 0; num175 < b70; num175++)
                            {
                                int charId = msg.reader().readInt();
                                int cx = msg.reader().readShort();
                                int cy = msg.reader().readShort();
                                long cHPShow = msg.readInt3Byte();
                                Char char13 = GameScr.findCharInMap(charId);
                                if (char13 != null)
                                {
                                    char13.cx = cx;
                                    char13.cy = cy;
                                    char13.cHP = (char13.cHPShow = cHPShow);
                                    char13.lastUpdateTime = mSystem.currentTimeMillis();
                                }
                            }
                            break;
                        }
                    case 19:
                        Char.myCharz().countKill = msg.reader().readUnsignedShort();
                        Char.myCharz().countKillMax = msg.reader().readUnsignedShort();
                        break;
                default:
                    return false;
            }
            return true;
        }
    }
}

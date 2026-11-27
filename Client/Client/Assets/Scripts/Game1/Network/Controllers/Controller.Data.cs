namespace Game1
{
    using Game1.Assets.src.e;
    using Assets.src.f;
    using Game1.Assets.src.g;
    using System;
    using UnityEngine;

    public partial class Controller : IMessageHandler
    {
        private void readLogin(Message msg)
        {
            sbyte b = msg.reader().readByte();
            ChooseCharScr.playerData = new PlayerData[b];
            Res.outz("[LEN] sl nguoi choi " + b);
            for (int i = 0; i < b; i++)
            {
                int playerID = msg.reader().readInt();
                string name = msg.reader().readUTF();
                short head = msg.reader().readShort();
                short body = msg.reader().readShort();
                short leg = msg.reader().readShort();
                long ppoint = msg.reader().readLong();
                ChooseCharScr.playerData[i] = new PlayerData(playerID, name, head, body, leg, ppoint);
            }
            GameCanvas.chooseCharScr.switchToMe();
            GameCanvas.chooseCharScr.updateChooseCharacter((byte)b);
        }
    
        private void createItem(myReader d)
        {
            GameScr.vcItem = d.readByte();
            ItemTemplates.itemTemplates.clear();
            GameScr.gI().iOptionTemplates = new ItemOptionTemplate[d.readUnsignedByte()];
            for (int i = 0; i < GameScr.gI().iOptionTemplates.Length; i++)
            {
                GameScr.gI().iOptionTemplates[i] = new ItemOptionTemplate();
                GameScr.gI().iOptionTemplates[i].id = i;
                GameScr.gI().iOptionTemplates[i].name = d.readUTF();
                GameScr.gI().iOptionTemplates[i].type = d.readByte();
            }
            int num = d.readShort();
            for (int j = 0; j < num; j++)
            {
                ItemTemplate it = new ItemTemplate((short)j, d.readByte(), d.readByte(), d.readUTF(), d.readUTF(), d.readByte(), d.readInt(), d.readShort(), d.readShort(), d.readBool());
                ItemTemplates.add(it);
            }
        }
    
        private void createSkill(myReader d)
        {
            GameScr.vcSkill = d.readByte();
            GameScr.gI().sOptionTemplates = new SkillOptionTemplate[d.readByte()];
            for (int i = 0; i < GameScr.gI().sOptionTemplates.Length; i++)
            {
                GameScr.gI().sOptionTemplates[i] = new SkillOptionTemplate();
                GameScr.gI().sOptionTemplates[i].id = i;
                GameScr.gI().sOptionTemplates[i].name = d.readUTF();
            }
            GameScr.nClasss = new NClass[d.readByte()];
            for (int j = 0; j < GameScr.nClasss.Length; j++)
            {
                GameScr.nClasss[j] = new NClass();
                GameScr.nClasss[j].classId = j;
                GameScr.nClasss[j].name = d.readUTF();
                GameScr.nClasss[j].skillTemplates = new SkillTemplate[d.readByte()];
                for (int k = 0; k < GameScr.nClasss[j].skillTemplates.Length; k++)
                {
                    GameScr.nClasss[j].skillTemplates[k] = new SkillTemplate();
                    GameScr.nClasss[j].skillTemplates[k].id = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].name = d.readUTF();
                    GameScr.nClasss[j].skillTemplates[k].maxPoint = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].manaUseType = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].type = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].iconId = d.readShort();
                    GameScr.nClasss[j].skillTemplates[k].damInfo = d.readUTF();
                    int lineWidth = 130;
                    if (GameCanvas.w == 128 || GameCanvas.h <= 208)
                    {
                        lineWidth = 100;
                    }
                    GameScr.nClasss[j].skillTemplates[k].description = mFont.tahoma_7_green2.splitFontArray(d.readUTF(), lineWidth);
                    GameScr.nClasss[j].skillTemplates[k].skills = new Skill[d.readByte()];
                    for (int l = 0; l < GameScr.nClasss[j].skillTemplates[k].skills.Length; l++)
                    {
                        GameScr.nClasss[j].skillTemplates[k].skills[l] = new Skill();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].skillId = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].template = GameScr.nClasss[j].skillTemplates[k];
                        GameScr.nClasss[j].skillTemplates[k].skills[l].point = d.readByte();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].powRequire = d.readLong();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].manaUse = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].coolDown = d.readInt();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].dx = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].dy = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].maxFight = d.readByte();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].damage = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].price = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].moreInfo = d.readUTF();
                        Skills.add(GameScr.nClasss[j].skillTemplates[k].skills[l]);
                    }
                }
            }
        }
    
        private void createMap(myReader d)
        {
            GameScr.vcMap = d.readByte();
            TileMap.mapNames = new string[d.readUnsignedByte()];
            for (int i = 0; i < TileMap.mapNames.Length; i++)
            {
                TileMap.mapNames[i] = d.readUTF();
            }
            int npcLen = d.readUnsignedByte();
            Npc.arrNpcTemplate = new NpcTemplate[System.Math.Max(256, npcLen)];
            for (int b = 0; b < npcLen; b++)
            {
                Npc.arrNpcTemplate[b] = new NpcTemplate();
                Npc.arrNpcTemplate[b].npcTemplateId = (sbyte)b;
                Npc.arrNpcTemplate[b].name = d.readUTF();
                Npc.arrNpcTemplate[b].headId = d.readShort();
                Npc.arrNpcTemplate[b].bodyId = d.readShort();
                Npc.arrNpcTemplate[b].legId = d.readShort();
                int menuLen = d.readByte();
                if (menuLen > 0)
                {
                    Npc.arrNpcTemplate[b].menu = new string[menuLen][];
                    for (int j = 0; j < menuLen; j++)
                    {
                        int subMenuLen = d.readByte();
                        Npc.arrNpcTemplate[b].menu[j] = new string[subMenuLen];
                        for (int k = 0; k < subMenuLen; k++)
                        {
                            Npc.arrNpcTemplate[b].menu[j][k] = d.readUTF();
                        }
                    }
                }
            }
            int mobLen = d.readUnsignedByte();
            Mob.arrMobTemplate = new MobTemplate[System.Math.Max(256, mobLen)];
            for (int b2 = 0; b2 < mobLen; b2++)
            {
                Mob.arrMobTemplate[b2] = new MobTemplate();
                Mob.arrMobTemplate[b2].mobTemplateId = (sbyte)b2;
                Mob.arrMobTemplate[b2].type = d.readByte();
                Mob.arrMobTemplate[b2].name = d.readUTF();
                Mob.arrMobTemplate[b2].hp = d.readInt();
                Mob.arrMobTemplate[b2].rangeMove = d.readByte();
                Mob.arrMobTemplate[b2].speed = d.readByte();
                Mob.arrMobTemplate[b2].dartType = d.readByte();
            }
        }
    
        private void createData(myReader d, bool isSaveRMS)
        {
            GameScr.vcData = d.readByte();
            if (isSaveRMS)
            {
                Rms.saveRMS("NR_dart", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_arrow", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_effect", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_image", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_part", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_skill", NinjaUtil.readByteArray(d));
                Rms.DeleteStorage("NRdata");
            }
        }
    
        private Image createImage(sbyte[] arr)
        {
            try
            {
                return Image.createImage(arr, 0, arr.Length);
            }
            catch (Exception catchErr_1)
            {
                Cout.LogWarning("[Controller.Data.cs] " + catchErr_1.Message);
            }
            return null;
        }
    
        public int[] arrayByte2Int(sbyte[] b)
        {
            int[] array = new int[b.Length];
            for (int i = 0; i < b.Length; i++)
            {
                int num = b[i];
                if (num < 0)
                {
                    num += 256;
                }
                array[i] = num;
            }
            return array;
        }
    
        public void readClanMsg(Message msg, int index)
        {
            try
            {
                ClanMessage clanMessage = new ClanMessage();
                sbyte b = msg.reader().readByte();
                clanMessage.type = b;
                clanMessage.id = msg.reader().readInt();
                clanMessage.playerId = msg.reader().readInt();
                clanMessage.playerName = msg.reader().readUTF();
                clanMessage.role = msg.reader().readByte();
                clanMessage.time = msg.reader().readInt() + 1000000000;
                bool flag = false;
                GameScr.isNewClanMessage = false;
                if (b == 0)
                {
                    string text = msg.reader().readUTF();
                    GameScr.isNewClanMessage = true;
                    if (mFont.tahoma_7.getWidth(text) > Panel.WIDTH_PANEL - 60)
                    {
                        clanMessage.chat = mFont.tahoma_7.splitFontArray(text, Panel.WIDTH_PANEL - 10);
                    }
                    else
                    {
                        clanMessage.chat = new string[1];
                        clanMessage.chat[0] = text;
                    }
                    clanMessage.color = msg.reader().readByte();
                }
                else if (b == 1)
                {
                    clanMessage.recieve = msg.reader().readByte();
                    clanMessage.maxCap = msg.reader().readByte();
                    flag = msg.reader().readByte() == 1;
                    if (flag)
                    {
                        GameScr.isNewClanMessage = true;
                    }
                    if (clanMessage.playerId != Char.myCharz().charID)
                    {
                        if (clanMessage.recieve < clanMessage.maxCap)
                        {
                            clanMessage.option = new string[1] { mResources.donate };
                        }
                        else
                        {
                            clanMessage.option = null;
                        }
                    }
                    if (GameCanvas.panel.cp != null)
                    {
                        GameCanvas.panel.updateRequest(clanMessage.recieve, clanMessage.maxCap);
                    }
                }
                else if (b == 2 && Char.myCharz().role == 0)
                {
                    GameScr.isNewClanMessage = true;
                    clanMessage.option = new string[2]
                    {
                        mResources.CANCEL,
                        mResources.receive
                    };
                }
                if (GameCanvas.currentScreen != GameScr.instance)
                {
                    GameScr.isNewClanMessage = false;
                }
                else if (GameCanvas.panel.isShow && GameCanvas.panel.type == 0 && GameCanvas.panel.currentTabIndex == 3)
                {
                    GameScr.isNewClanMessage = false;
                }
                ClanMessage.addMessage(clanMessage, index, flag);
            }
            catch (Exception ex)
            {
                Cout.println("LOI TAI CMD -= " + msg.command + " | " + ex.Message);
                Debug.LogError($"[Controller] LOI TAI CMD={msg.command}: {ex.Message}\n{ex.StackTrace}");
            }
        }
    

        private void readGetImgByName(Message msg)
        {
            try
            {
                string name = msg.reader().readUTF();
                sbyte nFrame = msg.reader().readByte();
                sbyte[] array = null;
                array = NinjaUtil.readByteArray(msg);
                Image img = createImage(array);
                ImgByName.SetImage(name, img, nFrame);
                if (array == null)
                {
                }
            }
            catch (Exception catchErr_2)
            {
                Cout.LogWarning("[Controller.Data.cs] " + catchErr_2.Message);
            }
        }
    
        private void createItemNew(myReader d)
        {
            try
            {
                loadItemNew(d, -1, true);
            }
            catch (Exception catchErr_3)
            {
                Cout.LogWarning("[Controller.Data.cs] " + catchErr_3.Message);
            }
        }
    
        private void loadItemNew(myReader d, sbyte type, bool isSave)
        {
            try
            {
                d.mark(100000);
                GameScr.vcItem = d.readByte();
                type = d.readByte();
                if (type == 0)
                {
                    GameScr.gI().iOptionTemplates = new ItemOptionTemplate[d.readUnsignedByte()];
                    for (int i = 0; i < GameScr.gI().iOptionTemplates.Length; i++)
                    {
                        GameScr.gI().iOptionTemplates[i] = new ItemOptionTemplate();
                        GameScr.gI().iOptionTemplates[i].id = i;
                        GameScr.gI().iOptionTemplates[i].name = d.readUTF();
                        GameScr.gI().iOptionTemplates[i].type = d.readByte();
                    }
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data = new sbyte[d.available()];
                        d.readFully(ref data);
                        Rms.saveRMS("NRitem0", data);
                    }
                }
                else if (type == 1)
                {
                    ItemTemplates.itemTemplates.clear();
                    int num = d.readShort();
                    for (int j = 0; j < num; j++)
                    {
                        ItemTemplate it = new ItemTemplate((short)j, d.readByte(), d.readByte(), d.readUTF(), d.readUTF(), d.readByte(), d.readInt(), d.readShort(), d.readShort(), d.readBoolean());
                        ItemTemplates.add(it);
                    }
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data2 = new sbyte[d.available()];
                        d.readFully(ref data2);
                        Rms.saveRMS("NRitem1", data2);
                    }
                }
                else if (type == 2)
                {
                    int num2 = d.readShort();
                    int num3 = d.readShort();
                    for (int k = num2; k < num3; k++)
                    {
                        ItemTemplate it2 = new ItemTemplate((short)k, d.readByte(), d.readByte(), d.readUTF(), d.readUTF(), d.readByte(), d.readInt(), d.readShort(), d.readShort(), d.readBoolean());
                        ItemTemplates.add(it2);
                    }
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data3 = new sbyte[d.available()];
                        d.readFully(ref data3);
                        Rms.saveRMS("NRitem2", data3);
                        sbyte[] data4 = new sbyte[1] { GameScr.vcItem };
                        Rms.saveRMS("NRitemVersion", data4);
                        LoginScr.isUpdateItem = false;
                        if (GameScr.vsData == GameScr.vcData && GameScr.vsMap == GameScr.vcMap && GameScr.vsSkill == GameScr.vcSkill && GameScr.vsItem == GameScr.vcItem)
                        {
                            GameScr.gI().readDart();
                            GameScr.gI().readEfect();
                            GameScr.gI().readArrow();
                            GameScr.gI().readSkill();
                            Service.gI().clientOk();
                        }
                    }
                }
                else if (type == 100)
                {
                    Char.Arr_Head_2Fr = readArrHead(d);
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data5 = new sbyte[d.available()];
                        d.readFully(ref data5);
                        Rms.saveRMS("NRitem100", data5);
                    }
                }
                else
                {
                    if (type != 101)
                    {
                        return;
                    }
                    try
                    {
                        int num4 = d.readShort();
                        Char.Arr_Head_FlyMove = new short[num4];
                        for (int l = 0; l < num4; l++)
                        {
                            short num5 = d.readShort();
                            Char.Arr_Head_FlyMove[l] = num5;
                        }
                        if (isSave)
                        {
                            d.reset();
                            sbyte[] data6 = new sbyte[d.available()];
                            d.readFully(ref data6);
                            Rms.saveRMS("NRitem101", data6);
                        }
                        return;
                    }
                    catch (Exception)
                    {
                        Char.Arr_Head_FlyMove = new short[0];
                        return;
                    }
                }
            }
            catch (Exception ex2)
            {
                ex2.ToString();
            }
        }
    
        private void readFrameBoss(Message msg, int mobTemplateId)
        {
            try
            {
                int num = msg.reader().readByte();
                int[][] array = new int[num][];
                for (int i = 0; i < num; i++)
                {
                    int num2 = msg.reader().readByte();
                    array[i] = new int[num2];
                    for (int j = 0; j < num2; j++)
                    {
                        array[i][j] = msg.reader().readByte();
                    }
                }
                frameHT_NEWBOSS.put(mobTemplateId + string.Empty, array);
            }
            catch (Exception catchErr_4)
            {
                Cout.LogWarning("[Controller.Data.cs] " + catchErr_4.Message);
            }
        }
    
        private int[][] readArrHead(myReader d)
        {
            int[][] array = new int[1][] { new int[2] { 542, 543 } };
            try
            {
                int num = d.readShort();
                array = new int[num][];
                for (int i = 0; i < array.Length; i++)
                {
                    int num2 = d.readByte();
                    array[i] = new int[num2];
                    for (int j = 0; j < num2; j++)
                    {
                        array[i][j] = d.readShort();
                    }
                }
            }
            catch (Exception catchErr_5)
            {
                Cout.LogWarning("[Controller.Data.cs] " + catchErr_5.Message);
            }
            return array;
        }
    
        public void phuban_Info(Message msg)
        {
            try
            {
                sbyte b = msg.reader().readByte();
                if (b == 0)
                {
                    readPhuBan_CHIENTRUONGNAMEK(msg, b);
                }
            }
            catch (Exception catchErr_6)
            {
                Cout.LogWarning("[Controller.Data.cs] " + catchErr_6.Message);
            }
        }
    
        private void readPhuBan_CHIENTRUONGNAMEK(Message msg, int type_PB)
        {
            try
            {
                sbyte b = msg.reader().readByte();
                if (b == 0)
                {
                    short idmapPaint = msg.reader().readShort();
                    string nameTeam = msg.reader().readUTF();
                    string nameTeam2 = msg.reader().readUTF();
                    int maxPoint = msg.reader().readInt();
                    short timeSecond = msg.reader().readShort();
                    int maxLife = msg.reader().readByte();
                    GameScr.phuban_Info = new InfoPhuBan(type_PB, idmapPaint, nameTeam, nameTeam2, maxPoint, timeSecond);
                    GameScr.phuban_Info.maxLife = maxLife;
                    GameScr.phuban_Info.updateLife(type_PB, 0, 0);
                }
                else if (b == 1)
                {
                    int pointTeam = msg.reader().readInt();
                    int pointTeam2 = msg.reader().readInt();
                    if (GameScr.phuban_Info != null)
                    {
                        GameScr.phuban_Info.updatePoint(type_PB, pointTeam, pointTeam2);
                    }
                }
                else if (b == 2)
                {
                    sbyte b2 = msg.reader().readByte();
                    short type = 0;
                    short num = -1;
                    if (b2 == 1)
                    {
                        type = 1;
                        num = 3;
                    }
                    else if (b2 == 2)
                    {
                        type = 2;
                    }
                    num = -1;
                    GameScr.phuban_Info = null;
                    GameScr.addEffectEnd(type, num, 0, GameCanvas.hw, GameCanvas.hh, 0, 0, -1, null);
                }
                else if (b == 5)
                {
                    short timeSecond2 = msg.reader().readShort();
                    if (GameScr.phuban_Info != null)
                    {
                        GameScr.phuban_Info.updateTime(type_PB, timeSecond2);
                    }
                }
                else if (b == 4)
                {
                    int lifeTeam = msg.reader().readByte();
                    int lifeTeam2 = msg.reader().readByte();
                    if (GameScr.phuban_Info != null)
                    {
                        GameScr.phuban_Info.updateLife(type_PB, lifeTeam, lifeTeam2);
                    }
                }
            }
            catch (Exception catchErr_7)
            {
                Cout.LogWarning("[Controller.Data.cs] " + catchErr_7.Message);
            }
        }
    
        public void read_opt(Message msg)
        {
            try
            {
                sbyte b = msg.reader().readByte();
                if (b == 0)
                {
                    short idHat = msg.reader().readShort();
                    Char.myCharz().idHat = idHat;
                    SoundMn.gI().getStrOption();
                }
                else if (b == 2)
                {
                    int num = msg.reader().readInt();
                    sbyte b2 = msg.reader().readByte();
                    short num2 = msg.reader().readShort();
                    string v = num2 + "," + b2;
                    MainImage imagePath = ImgByName.getImagePath("banner_" + num2, ImgByName.hashImagePath);
                    GameCanvas.danhHieu.put(num + string.Empty, v);
                }
                else if (b == 3)
                {
                    short num3 = msg.reader().readShort();
                    SmallImage.createImage(num3);
                    BackgroudEffect.id_water1 = num3;
                }
                else if (b == 4)
                {
                    string o = msg.reader().readUTF();
                    GameCanvas.messageServer.addElement(o);
                }
            }
            catch (Exception e)
            {
                Debug.LogException(e);
            }
        }
    
        public void read_UpdateSkill(Message msg)
        {
            try
            {
                short num = msg.reader().readShort();
                sbyte b = -1;
                if (msg.reader().available() > 0)
                {
                    b = msg.reader().readSByte();
                }
                if (b == 0)
                {
                    short curExp = msg.reader().readShort();
                    for (int i = 0; i < Char.myCharz().vSkill.size(); i++)
                    {
                        Skill skill = (Skill)Char.myCharz().vSkill.elementAt(i);
                        if (skill != null && skill.skillId == num)
                        {
                            skill.curExp = curExp;
                            break;
                        }
                    }
                }
                else if (b == 1)
                {
                    sbyte b2 = msg.reader().readByte();
                    for (int j = 0; j < Char.myCharz().vSkill.size(); j++)
                    {
                        Skill skill2 = (Skill)Char.myCharz().vSkill.elementAt(j);
                        if (skill2 != null && skill2.skillId == num)
                        {
                            for (int k = 0; k < 20; k++)
                            {
                                string nameImg = "Skills_" + skill2.template.id + "_" + b2 + "_" + k;
                                MainImage imagePath = ImgByName.getImagePath(nameImg, ImgByName.hashImagePath);
                            }
                            break;
                        }
                    }
                }
                else
                {
                    if (b != -1)
                    {
                        return;
                    }
                    Skill skill3 = Skills.get(num);
                    if (skill3 == null)
                    {
                        return;
                    }
                    bool found = false;
                    for (int l = 0; l < Char.myCharz().vSkill.size(); l++)
                    {
                        Skill skill4 = (Skill)Char.myCharz().vSkill.elementAt(l);
                        if (skill4 != null && skill4.template != null && skill4.template.id == skill3.template.id)
                        {
                            Char.myCharz().vSkill.setElementAt(skill3, l);
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        useSkill(skill3);
                    }
                    for (int m = 0; m < Char.myCharz().vSkillFight.size(); m++)
                    {
                        Skill skill5 = (Skill)Char.myCharz().vSkillFight.elementAt(m);
                        if (skill5 != null && skill5.template != null && skill5.template.id == skill3.template.id)
                        {
                            Char.myCharz().vSkillFight.setElementAt(skill3, m);
                            break;
                        }
                    }
                    for (int n = 0; n < GameScr.onScreenSkill.Length; n++)
                    {
                        if (GameScr.onScreenSkill[n] != null && GameScr.onScreenSkill[n].template != null && GameScr.onScreenSkill[n].template.id == skill3.template.id)
                        {
                            GameScr.onScreenSkill[n] = skill3;
                            break;
                        }
                    }
                    for (int num2 = 0; num2 < GameScr.keySkill.Length; num2++)
                    {
                        if (GameScr.keySkill[num2] != null && GameScr.keySkill[num2].template != null && GameScr.keySkill[num2].template.id == skill3.template.id)
                        {
                            GameScr.keySkill[num2] = skill3;
                            break;
                        }
                    }
                    if (Char.myCharz().myskill != null && Char.myCharz().myskill.template != null && Char.myCharz().myskill.template.id == skill3.template.id)
                    {
                        Char.myCharz().myskill = skill3;
                    }
                    if (skill3.template != null)
                    {
                        GameScr.info1.addInfo(mResources.hasJustUpgrade1 + skill3.template.name + mResources.hasJustUpgrade2 + skill3.point, 0);
                    }
                }
            }
            catch (Exception ex)
            {
                Cout.LogError("read_UpdateSkill error: " + ex.ToString());
            }
        }
    }
}

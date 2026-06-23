using Game2.Assets.src.e;
using UnityEngine;

namespace Game2.God
{
/*Author: HAIRMOD*/
    public class ClientManager : IActionListener, IChatable
    {
        private static ClientManager instance { get; set; }
        private int xJ, yJ;
        private int speedRun = -1;
        public static ClientManager getInstance()
        {
            return (instance == null) ? (instance = new ClientManager()) : instance;
        }
        public void Update()
        {
            NPoint.getInstance().Update();
            Items.getInstance().Update();
            Revive.getInstance().Update();
            Mobs.Update();
            nSkill.getInstance().Update();
            PetService.getInstance().Update();
            ListChars.getInstance().Update();
            if (speedRun != -1 && Char.myCharz() != null)
            {
                Char.myCharz().cspeed = speedRun;
            }
        }
        public void UpdateTouch()
        {
            ListChars.getInstance().UpdateTouch();
            if (GameScr.isAnalog == 1)
            {
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(160, 2, 30, 30) && GameScr.isAnalog == 1)
                {
                    mScreen.keyTouch = 999;
                    if (GameCanvas.isPointerJustRelease && GameCanvas.isPointerClick)
                    {
                        GameCanvas.keyAsciiPress = 'x';
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(GameScr.xHP + 20, GameScr.yHP + 20 - 6 - 60, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 14;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'c';
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(GameScr.xTG, GameScr.yTG - 33, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1000;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'f';
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(GameScr.xHP - 40, GameScr.yHP, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1001;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'm';
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
                xJ = 20;
                yJ = GameCanvas.h - 45 - GameScr.imgAnalog1.getHeight() - 10;
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(xJ, yJ, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1002;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'j';
                        GameCanvas.clearAllPointerEvent();
                    }
                    return;
                }
                xJ += GameScr.imgNut.getWidth() + 20;
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(xJ, yJ, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1003;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'k';
                        GameCanvas.clearAllPointerEvent();
                    }
                    return;
                }
                xJ += GameScr.imgNut.getWidth() + 20;
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(xJ, yJ, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1004;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'l';
                        GameCanvas.clearAllPointerEvent();
                    }
                    return;
                }
            }
        }
        public void GUI(mGraphics g)
        {
            PlayerInfo.getInstance().paintInfoPlayer(g);
            Boss.getInstance().PaintBossInfo(g);
            ListChars.getInstance().paintPlayerMap(g);
            if (GameScr.isAnalog == 1)
            {
                if (GameCanvas.isTouch && !GameCanvas.panel.isShow && !ChatTextField.gI().isShow && !GameCanvas.menu.showMenu)
                {
                    g.setColor((mScreen.keyTouch == 999) ? 225225225 : 0, 0.5f);
                    g.fillRect(160, 2, 30, 30, 20);
                    SmallImage.drawSmallImage(g, 4387, 165, 8, 0, 0);
                    if (Utils.findItemBag(921))
                    {
                        Small img = SmallImage.imgNew[7993];
                        if (img == null)
                        {
                            SmallImage.createImage(7993);
                            return;
                        }
                        g.drawImage((mScreen.keyTouch != 1000) ? GameScr.imgNut : GameScr.imgNutF, GameScr.xTG + 20, GameScr.yTG - 20, mGraphics.HCENTER | mGraphics.VCENTER);
                        g.drawImage(img.img, GameScr.xTG + 21, GameScr.yTG - 20, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    else if (Utils.findItemBag(454))
                    {
                        Small img = SmallImage.imgNew[3896];
                        if (img == null)
                        {
                            SmallImage.createImage(3896);
                            return;
                        }
                        g.drawImage((mScreen.keyTouch != 1000) ? GameScr.imgNut : GameScr.imgNutF, GameScr.xTG + 20, GameScr.yTG - 20, mGraphics.HCENTER | mGraphics.VCENTER);
                        g.drawImage(img.img, GameScr.xTG + 21, GameScr.yTG - 20, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    else
                    {
                        Item item = Utils.getItemFromName("bông tai");
                        if (item != null)
                        {
                            Small img = SmallImage.imgNew[item.template.iconID];
                            if (img == null)
                            {
                                SmallImage.createImage(item.template.iconID);
                                return;
                            }
                            g.drawImage((mScreen.keyTouch != 1000) ? GameScr.imgNut : GameScr.imgNutF, GameScr.xTG + 20, GameScr.yTG - 20, mGraphics.HCENTER | mGraphics.VCENTER);
                            g.drawImage(img.img, GameScr.xTG + 21, GameScr.yTG - 20, mGraphics.HCENTER | mGraphics.VCENTER);
                        }
                    }
                    g.drawImage((mScreen.keyTouch != 1001) ? GameScr.imgNut : GameScr.imgNutF, GameScr.xHP - 20, GameScr.yHP + 20, mGraphics.HCENTER | mGraphics.VCENTER);
                    g.drawImage(GameScr.doikhu, GameScr.xHP - 20, GameScr.yHP + 20, mGraphics.HCENTER | mGraphics.VCENTER);
                    mFont.tahoma_7b_white.drawString(g, TileMap.zoneID.ToString(), GameScr.xHP - 20, GameScr.yHP + 15, mGraphics.HCENTER | mGraphics.VCENTER, mFont.tahoma_7b_green2);
                    xJ = 20;
                    yJ = GameCanvas.h - 45 - GameScr.imgAnalog1.getHeight() - 10;
                    g.drawImage((mScreen.keyTouch != 1002) ? GameScr.imgNut : GameScr.imgNutF, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                    g.drawImage(GameScr.arrow5, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                    xJ += GameScr.imgNut.getWidth() + 20;
                    g.drawImage((mScreen.keyTouch != 1003) ? GameScr.imgNut : GameScr.imgNutF, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                    g.drawImage(GameScr.arrow4, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                    xJ += GameScr.imgNut.getWidth() + 20;
                    g.drawImage((mScreen.keyTouch != 1004) ? GameScr.imgNut : GameScr.imgNutF, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                    g.drawImage(GameScr.arrow, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                    g.drawImage((mScreen.keyTouch != 14) ? GameScr.imgNut : GameScr.imgNutF, GameScr.xHP + 20 + 5, GameScr.yHP + 20 - 6 - 40, mGraphics.HCENTER | mGraphics.VCENTER);
                    SmallImage.drawSmallImage(g, 1088, GameScr.xHP + 20 - 7 + 5, GameScr.yHP - 33, 0, 0);
                }
            }
            if(LoginScr.imgTitle != null) { 
                g.drawImage(LoginScr.imgTitle, GameCanvas.w / 2, 35, mGraphics.VCENTER | mGraphics.HCENTER);
            }
        }
        public void KeyPressed(int keyCode)
        {
            switch (keyCode)
            {
                case 'x':
                    ClientManager.getInstance().MenuClient();
                    break;
                case 'j':
                    MapController.getInstance().NextMap(0);
                    break;
                case 'k':
                    MapController.getInstance().NextMap(2);
                    break;
                case 'l':
                    MapController.getInstance().NextMap(1);
                    break;
                case 'c':
                    if (Utils.findItemBag(193))
                    {
                        Utils.UseItem(193);
                    }
                    else if (Utils.findItemBag(194))
                    {
                        Utils.UseItem(194);
                    }
                    else
                    {
                        GameScr.info1.addInfo("Không Có Capsule", 0);
                    }
                    break;
                case 'f':
                    if (Utils.findItemBag(454))
                    {
                        Utils.UseItem(454);
                    }
                    else if (Utils.findItemBag(921))
                    {
                        Utils.UseItem(921);
                    }
                    else if(Utils.findItemName("bông tai"))
                    {
                        Item item = Utils.getItemFromName("bông tai");
                        if (item != null)
                        {
                            Utils.UseItem(item.template.id);
                        }
                    }
                    else
                    {
                        GameScr.info1.addInfo("Không Có Bông Tai", 0);
                    }
                    break;
                case 'm':
                    Service.gI().openUIZone();
                    break;
                case 'e':
                    perform(1, null);
                    break;
                case 'a':
                    perform(2, null);
                    break;
                case 'g':
                    bool flag10 = Char.myCharz().charFocus == null;
                    if (flag10)
                    {
                        GameScr.info1.addInfo("Vui Lòng Chọn Mục Tiêu!", 0);
                    }
                    else
                    {
                        Service.gI().giaodich(0, Char.myCharz().charFocus.charID, -1, -1);
                        GameScr.info1.addInfo("Đã Gửi Lời Mời Giao Dịch Đến: " + Char.myCharz().charFocus.cName, 0);
                    }
                    break;
            }
        }
        private void MenuClient()
        {
            string[] listIndex;
            listIndex = new string[]
            { 
                "Hồi Sinh",
                "Tự Đánh",
                "Tàn Sát",
                "Auto Up Đệ",
                "Thông Báo BOSS",
                "D.s Nhân Vật",
                "Auto Nhặt",
                "Giảm Đồ Họa",
                "Auto Login",
                "Tốc Độ Game",
                "Tốc Độ Di Chuyển",
                "Nút Chuyển Tab"
            };
            MyVector myVector = new MyVector();
            for(int i = 0; i < listIndex.Length; i++)
            {
                myVector.addElement(new Command(listIndex[i], this, i + 1, null));
            }
            GameCanvas.menu.startAt(myVector, 0);
        }
        public bool Chat(string text)
        {
            if(text.StartsWith("cheat "))
            {
                int cheat = int.Parse(text.Split(' ')[1]);
                Time.timeScale = cheat;
                return true;
            }
            if(text.StartsWith("k "))
            {
                int zone = int.Parse(text.Split(' ')[1]);
                Service.gI().requestChangeZone(zone, -1);
                return true;
            }
            switch (text)
            {
                case "ahs":
                    perform(1, null);
                    return true;
                case "alogin":
                    perform(9, null);
                    return true;
                case "ak":
                    perform(2, null);
                    return true;
                case "gdh":
                    perform(8, null);
                    return true;
                case "tbb":
                    perform(5, null);
                    return true;
                case "dsnv":
                    perform(6, null);
                    return true;
                case "adt":
                    perform(4, null);
                    return true;
            }
            return false;
        }
        public void perform(int idAction, object p)
        {
            switch (idAction)
            {
                case 1:
                    Revive.getInstance().setRevive();
                    Utils.addInfo1("Tự Động Hồi Sinh", Revive.getInstance().getRevive());
                    break;
                case 2:
                    nSkill.getInstance().canAttack =! nSkill.getInstance().canAttack;
                    Utils.addInfo1("Tự Đánh", nSkill.getInstance().canAttack);
                    break;
                case 3:
                    Mobs.IsTanSat =! Mobs.IsTanSat;
                    Utils.addInfo1("Tàn Sát", Mobs.IsTanSat);
                    break;
                case 4:
                    PetService.getInstance().setUp();
                    Utils.addInfo1("Auto Up Đệ", PetService.getInstance().getUp());
                    break;
                case 5:
                    Boss.getInstance().isShow =! Boss.getInstance().isShow;
                    Utils.addInfo1("Thông báo BOSS", Boss.getInstance().isShow);
                    break;
                case 6:
                    ListChars.getInstance().isShow = !ListChars.getInstance().isShow;
                    Utils.addInfo1("D.s Nhân Vật", ListChars.getInstance().isShow);
                    break;
                case 7:
                    Mobs.IsAutoPickItems =!Mobs.IsAutoPickItems;
                    Utils.addInfo1("Auto Nhặt", Mobs.IsAutoPickItems);
                    break;
                case 8:
                    ListChars.getInstance().HideMap = !ListChars.getInstance().HideMap;
                    Utils.addInfo1("Giảm Đồ Họa", ListChars.getInstance().HideMap);
                    break;
                case 9:
                    PlayerInfo.getInstance().canLogin =!PlayerInfo.getInstance().canLogin;
                    Utils.addInfo1("Auto Login", PlayerInfo.getInstance().canLogin);
                    break;
                case 10:
                    Utils.startChat(this, "Tốc Độ Game", "Nhập tốc độ game (1 - 100)", TField.INPUT_TYPE_NUMERIC);
                    break;
                case 11:
                    Utils.startChat(this, "Tốc Độ Di Chuyển", "Nhập tốc độ di chuyển (4 - 50)", TField.INPUT_TYPE_NUMERIC);
                    break;
            }
        }

        public void onChatFromMe(string text, string to)
        {
            ChatTextField chat = ChatTextField.gI();
            if (chat.strChat.Equals("Tốc Độ Game"))
            {
                try
                {
                    int speed = int.Parse(chat.tfChat.getText());
                    Time.timeScale = speed;
                    GameScr.info1.addInfo("Tốc độ game: " + speed, 0);
                }
                catch (System.Exception)
                {
                    GameScr.info1.addInfo("Giá trị không hợp lệ!", 0);
                }
                Utils.resetTF();
            }
            else if (chat.strChat.Equals("Tốc Độ Di Chuyển"))
            {
                try
                {
                    int speed = int.Parse(chat.tfChat.getText());
                    speedRun = speed;
                    if (Char.myCharz() != null)
                    {
                        Char.myCharz().cspeed = speed;
                    }
                    GameScr.info1.addInfo("Tốc độ di chuyển: " + speed, 0);
                }
                catch (System.Exception)
                {
                    GameScr.info1.addInfo("Giá trị không hợp lệ!", 0);
                }
                Utils.resetTF();
            }
        }

        public void onCancelChat()
        {
            Utils.resetTF();
        }
    }
}

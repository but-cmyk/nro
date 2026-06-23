using System.Collections;
using System.Threading;
using UnityEngine;

namespace Game5.God
{
    /*Author: HairMod*/
    public class PlayerInfo
    {
        private static PlayerInfo instance { get; set; }
        public bool canLogin;
        private long timeLogin, timeWait;
        public static PlayerInfo getInstance()
        {
            return (instance == null) ? (instance = new PlayerInfo()) : instance;
        }
        private void drawString(mGraphics g, string s, bool a, int x, int y)
        {
            mFont.tahoma_7_green2.drawString(g, a ? s + " Bật" : s + " Tắt", x, y, 0);
        }
        public void paintInfoPlayer(mGraphics g)
        {
            if (Char.myCharz().charFocus != null)
            {
                string charInfo = $"{Char.myCharz().charFocus.cName} [{NinjaUtil.getMoneys(Char.myCharz().charFocus.cHP)}/{NinjaUtil.getMoneys(Char.myCharz().charFocus.cHPFull)}]";
                if (Char.myCharz().charFocus.clanID != -1)
                {
                    charInfo += $"\nClan ID: {Char.myCharz().charFocus.clanID}";
                }
                mFont.tahoma_7b_red.drawString(g, charInfo, GameCanvas.w / 2, 80, mGraphics.VCENTER | mGraphics.HCENTER);
            }
            else if (Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.getTemplate() != null)
            {
                string mobInfo = $"{Char.myCharz().mobFocus.getTemplate().name} [{NinjaUtil.getMoneys(Char.myCharz().mobFocus.hp)}/{NinjaUtil.getMoneys(Char.myCharz().mobFocus.maxHp)}]";
                mFont.tahoma_7b_red.drawString(g, mobInfo, GameCanvas.w / 2, 80, mGraphics.VCENTER | mGraphics.HCENTER);
            }
            mFont.number_orange.drawStringBd(g, NinjaUtil.getMoneys(Char.myCharz().cHP), 90, 4, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_green.drawStringBd(g, NinjaUtil.getMoneys(Char.myCharz().cMP), 90, 17, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_white.drawStringBd(g, TileMap.mapName + $"[{TileMap.zoneID}]", 85, 30, 0, mFont.tahoma_7_grey);

            int x = 17;
            int y = GameCanvas.hh / 2 + 12;
            if (Revive.getInstance().getRevive())
            {
                drawString(g, "Tự Động Hồi Sinh: ", Revive.getInstance().getRevive(), x, y);
                y += 10;
            }
            if (nSkill.getInstance().canAttack)
            {
                drawString(g, "Tự Đánh: ", nSkill.getInstance().canAttack, x, y);
                y += 10;
            }
            if (Mobs.IsTanSat)
            {
                drawString(g, "Tàn Sát: ", Mobs.IsTanSat, x, y);
                y += 10;
            }
            if (PetService.getInstance().getUp())
            {
                drawString(g, "Auto Up Đệ: ", PetService.getInstance().getUp(), x, y);
                y += 10;
            }
            if (Boss.getInstance().isShow)
            {
                drawString(g, "Thông Báo BOSS: ", Boss.getInstance().isShow, x, y);
                y += 10;
            }
            if (ListChars.getInstance().isShow)
            {
                drawString(g, "D.s Nhân Vật: ", ListChars.getInstance().isShow, x, y);
                y += 10;
            }
            if (Mobs.IsAutoPickItems)
            {
                drawString(g, "Auto Nhặt: ", Mobs.IsAutoPickItems, x, y);
                y += 10;
            }
            if (ListChars.getInstance().HideMap)
            {
                drawString(g, "Giảm Đồ Họa: ", ListChars.getInstance().HideMap, x, y);
                y += 10;
            }
            if (canLogin)
            {
                drawString(g, "Auto Login: ", canLogin, x, y);
                y += 10;
            }
        }
        public void Update()
        {
            if (canLogin) Login();
        }
        private void Login()
        {
            if(GameCanvas.currentScreen is LoginScr || GameCanvas.currentScreen is ServerListScreen)
            {
                if(GameCanvas.loginScr == null)
                {
                    GameCanvas.loginScr = new LoginScr();
                }
                GameCanvas.loginScr.switchToMe();
                if (mSystem.currentTimeMillis() - timeWait >= 15000L)
                {
                    timeWait = mSystem.currentTimeMillis();
                    if (mSystem.currentTimeMillis() - timeLogin >= 2000L)
                    {
                        timeLogin = mSystem.currentTimeMillis();
                        if (GameCanvas.currentScreen is LoginScr)
                            GameCanvas.loginScr.doLogin();
                    }
                }
            }
        }
    }
}

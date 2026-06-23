using System.Collections;
using System.Threading;
using UnityEngine;

namespace Game1.God
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
            // Fully removed per user request
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

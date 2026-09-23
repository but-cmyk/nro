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
            if (GameCanvas.currentScreen is ServerListScreen)
            {
                long now = mSystem.currentTimeMillis();
                if (timeWait == 0)
                {
                    timeWait = now;
                }
                // Chờ 3 giây để người chơi nhận biết thông báo mất kết nối, sau đó tự đăng nhập lại
                if (now - timeWait >= 3000L)
                {
                    timeWait = now + 10000L; // Cooldown 10s giữa các lần thử lại
                    GameCanvas.endDlg();
                    if (GameCanvas.serverScreen != null)
                    {
                        GameCanvas.serverScreen.perform(3, null); // Chọn "Chơi tiếp: [acc]"
                    }
                }
            }
            else if (GameCanvas.currentScreen is LoginScr)
            {
                long now = mSystem.currentTimeMillis();
                if (timeWait == 0)
                {
                    timeWait = now;
                }
                if (now - timeWait >= 3000L)
                {
                    timeWait = now + 10000L;
                    GameCanvas.endDlg();
                    if (GameCanvas.loginScr != null)
                    {
                        GameCanvas.loginScr.doLogin();
                    }
                }
            }
            else
            {
                timeWait = 0;
            }
        }
    }
}

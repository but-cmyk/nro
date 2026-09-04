namespace Game1
{
    using System;

    public class AuthPacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case -26: // SERVER_ALERT
                        ServerListScreen.testConnect = 2;
                        GameCanvas.debug("SA2", 2);
                        string alertMsg = msg.reader().readUTF();
                        GameCanvas.startOKDlg(alertMsg);
                        InfoDlg.hide();
                        LoginScr.isContinueToLogin = false;
                        Char.isLoadingMap = false;
                        if (GameCanvas.currentScreen == GameCanvas.loginScr || GameCanvas.currentScreen == GameCanvas.registerScr)
                        {
                            GameCanvas.serverScreen.switchToMe();
                        }
                        return true;
                }
            }
            catch (Exception ex)
            {
                Cout.println("Loi AuthPacketHandler: " + ex.ToString());
            }
            return false;
        }
    }
}

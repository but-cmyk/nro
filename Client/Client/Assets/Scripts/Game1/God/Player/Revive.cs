namespace Game1.God
{
/*Author: HAIRMOD*/
    public class Revive
    {
        private static Revive instance { get; set; }
        private bool canRevive = false;
        public static int reviveCount = 0;
        public static int maxReviveCount = 5;
        public static long lastReviveTime = 0;

        public static Revive getInstance()
        {
            return (instance == null) ? (instance = new Revive()) : instance;
        }
        public void setRevive()
        {
            canRevive = !canRevive;
            if (canRevive) reviveCount = 0;
        }
        public void setRevive(bool val)
        {
            canRevive = val;
            if (canRevive) reviveCount = 0;
        }
        public bool getRevive()
        {
            return canRevive;
        }
        private void PlayerRevive()
        {
            if (canRevive && Char.myCharz().meDead)
            {
                long now = mSystem.currentTimeMillis();
                if (reviveCount >= maxReviveCount)
                {
                    canRevive = false;
                    reviveCount = 0;
                    GameScr.info1.addInfo("Đã ngắt Auto Hồi Sinh (" + maxReviveCount + " lần) để bảo vệ Ngọc!", 0);
                    AutoSettingManager.getInstance().updateItemState("ahs", false);
                    return;
                }

                if (GameCanvas.gameTick % 20 == 0)
                {
                    Service.gI().wakeUpFromDead();
                    reviveCount++;
                    lastReviveTime = now;
                }
            }
            else if (!Char.myCharz().meDead && canRevive && reviveCount > 0)
            {
                if (mSystem.currentTimeMillis() - lastReviveTime > 60000L)
                {
                    reviveCount = 0;
                }
            }
        }
        public void Update()
        {
            PlayerRevive();
        }
    }
}

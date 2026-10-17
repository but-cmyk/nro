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
                if (GameCanvas.gameTick % 20 == 0)
                {
                    Service.gI().wakeUpFromDead();
                    reviveCount++;
                    lastReviveTime = mSystem.currentTimeMillis();
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

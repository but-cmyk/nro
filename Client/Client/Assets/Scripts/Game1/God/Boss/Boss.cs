namespace Game1.God
{
    /*Author: HairMod*/
    public class Boss
    {
        private static Boss instance { get; set; }
        public bool isShow = true;
        public static Boss getInstance()
        {
            return instance == null ? instance = new Boss() : instance;
        }
        public void PaintBossInfo(mGraphics g)
        {
            if (!isShow) return;
            var bosses = BossData.getInstance().listData;
            for (int i = 0; i < bosses.Count; i++)
            {
                var bossInfos = bosses[i];
                string bBoss = bossInfos.getDisplayString();
                mFont.tahoma_7b_yellow.drawString(g, bBoss, GameCanvas.w - 5, 37 + 12 * i, mFont.RIGHT, mFont.tahoma_7);
            }
        }
    }
}

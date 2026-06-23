namespace Game5.God
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
                string name = bossInfos.name;
                string map = bossInfos.getMapName();
                var time = bossInfos.getStartTimeSpan();
                string bBoss = string.Concat(new object[] {
                name + " - " + map + " - " + time
                });
                mFont.tahoma_7b_yellow.drawString(g, bBoss, GameCanvas.w, 37 + 12 * i, mFont.RIGHT, mFont.tahoma_7);
            }
        }
    }
}

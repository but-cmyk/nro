namespace Game1.God
{
    /*Author: HAIRMOD*/
    public struct ItemGroup
    {
        public int id;
        public int indexUI;
        public bool buyGold;
        public bool buyCoin;
        public long lastTimeUse;
        public long delayMs;
        public ItemGroup(int id, int indexUI, bool buyGold, bool buyCoin)
        {
            this.id = id;
            this.indexUI = indexUI;
            this.buyGold = buyGold;
            this.buyCoin = buyCoin;
            this.lastTimeUse = 0;
            this.delayMs = 0;
        }
    }
}

namespace Game1
{
    using System;

    /// <summary>
    /// PanelConstants: Định danh toàn bộ các hằng số Type và Tab con của Panel.cs.
    /// Giải quyết triệt để vấn đề: if ((type == 0 && currentTabIndex == 1) || (type == 7 && currentTabIndex == 0))
    /// </summary>
    public static class PanelConstants
    {
        #region Panel Types
        public const int TYPE_MAIN = 0;
        public const int TYPE_SHOP = 1;
        public const int TYPE_BOX = 2;
        public const int TYPE_ZONE = 3;
        public const int TYPE_MAP = 4;
        public const int TYPE_CLANS = 5;
        public const int TYPE_INFOMATION = 6;
        public const int TYPE_BODY = 7;
        public const int TYPE_MESS = 8;
        public const int TYPE_ARCHIVEMENT = 9;
        public const int TYPE_PLAYER_MENU = 10;
        public const int TYPE_FRIEND = 11;
        public const int TYPE_COMBINE = 12;
        public const int TYPE_GIAODICH = 13;
        public const int TYPE_MAPTRANS = 14;
        public const int TYPE_TOP = 15;
        public const int TYPE_ENEMY = 16;
        public const int TYPE_KIGUI = 17;
        public const int TYPE_FLAG = 18;
        public const int TYPE_OPTION = 19;
        public const int TYPE_ACCOUNT = 20;
        public const int TYPE_PET_MAIN = 21;
        public const int TYPE_AUTO = 22;
        public const int TYPE_GAMEINFO = 23;
        public const int TYPE_GAMEINFOSUB = 24;
        public const int TYPE_SPEACIALSKILL = 25;
        #endregion

        #region Sub-Panel Tab Indexes

        /// <summary>
        /// Các tab trong Giao diện chính (TYPE_MAIN = 0)
        /// </summary>
        public static class MainTabs
        {
            public const int TASK = 0;        // Nhiệm vụ
            public const int INVENTORY = 1;   // Hành trang
            public const int SKILL = 2;       // Kỹ năng
            public const int CLANS = 3;       // Bang hội
            public const int TOOLS = 4;       // Chức năng / Công cụ
        }

        /// <summary>
        /// Các tab trong Rương đồ (TYPE_BOX = 2)
        /// </summary>
        public static class ChestTabs
        {
            public const int CHEST = 0;       // Rương đồ
            public const int INVENTORY = 1;   // Hành trang
        }

        /// <summary>
        /// Các tab trong Ép đồ / Nâng cấp (TYPE_COMBINE = 12)
        /// </summary>
        public static class CombineTabs
        {
            public const int COMBINE = 0;     // Bàn ép đồ
            public const int INVENTORY = 1;   // Hành trang
        }

        /// <summary>
        /// Các tab trong Đệ tử (TYPE_PET_MAIN = 21)
        /// </summary>
        public static class PetTabs
        {
            public const int PET_INVENTORY = 0; // Đồ đệ tử
            public const int PET_STATUS = 1;    // Chỉ số đệ tử
            public const int INVENTORY = 2;     // Hành trang sư phụ
        }

        /// <summary>
        /// Các tab trong Giao dịch (TYPE_GIAODICH = 13)
        /// </summary>
        public static class TradeTabs
        {
            public const int INVENTORY = 0;     // Túi đồ giao dịch của mình
            public const int PARTNER = 1;       // Bảng đồ đối phương
        }

        /// <summary>
        /// Các tab trong Trang bị nhân vật (TYPE_BODY = 7)
        /// </summary>
        public static class BodyTabs
        {
            public const int INVENTORY = 0;     // Đồ trên người
        }

        #endregion
    }
}

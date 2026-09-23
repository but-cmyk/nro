namespace Game1
{
    using System;

    /// <summary>
    /// NetworkOpcodes: Tập trung hóa toàn bộ các mã lệnh gói tin (Opcode) giao thức TCP Socket với Server Java Netty.
    /// Khai báo const sbyte để tương thích 100% với msg.command mà không cần ép kiểu.
    /// </summary>
    public static class NetworkOpcodes
    {
        #region Xác thực & Tài khoản
        public const sbyte LOGIN = 0;
        public const sbyte LOGOUT = -26;
        #endregion

        #region Chỉ số & Cập nhật nhân vật
        public const sbyte UPDATE_HP = 6;
        public const sbyte UPDATE_MP = -69;
        public const sbyte UPDATE_EXP = -68;
        public const sbyte UPDATE_NANG_DONG = -97;
        #endregion

        #region Nhiệm vụ & Tương tác NPC
        public const sbyte TASK_UPDATE = -70;
        public const sbyte NPC_MENU = 38;
        public const sbyte NPC_CHAT = 32;
        #endregion

        #region Bang hội (Clan)
        public const sbyte CLAN_MESSAGE = -51;
        public const sbyte CLAN_INFO = -53;
        public const sbyte CLAN_UPDATE = -52;
        public const sbyte CLAN_MEMBER = -50;
        public const sbyte CLAN_SEARCH = -47;
        public const sbyte CLAN_CREATE_INFO = -46;
        #endregion

        #region Giao dịch (Trade)
        public const sbyte TRADE_ACTION = -86;
        public const sbyte TRADE_ORDER = 39;
        #endregion

        #region Vật phẩm bản đồ (Item Map)
        public const sbyte ITEM_MAP_DROP = -18;
        public const sbyte ITEM_MAP_OTHER_DROP = -14;
        public const sbyte ITEM_MAP_PICK = -20;
        public const sbyte ITEM_MAP_OTHER_PICK = -19;
        public const sbyte ITEM_MAP_REMOVE = -21;
        public const sbyte ITEM_MAP_ADD = 68;
        #endregion

        #region Bản đồ & Di chuyển (Map & Movement)
        public const sbyte CHANGE_MAP_PREPARE = -22;
        public const sbyte MAP_INFO = -24;
        public const sbyte MAP_TRANS = -91;
        public const sbyte RESET_POINT = 46;
        public const sbyte MOVE_FAST = 58;
        #endregion

        #region Chiến đấu, Sát thương & Kỹ năng (Combat)
        public const sbyte CHAR_INJURE = 56;
        public const sbyte REVIVE = 84;
        public const sbyte MOB_ME_ATTACK = 83;
        public const sbyte SKILL_COOLDOWN = -94;
        public const sbyte MOB_ME_EVENT = -95;
        #endregion
    }
}

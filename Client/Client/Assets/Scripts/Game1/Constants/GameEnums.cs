namespace Game1
{
    using System;

    /// <summary>
    /// Giới tính hành tinh của nhân vật trong Ngọc Rồng Online.
    /// </summary>
    public enum CharacterGender : sbyte
    {
        TraiDat = 0,    // Trái Đất (Earth)
        Namek = 1,      // Namếc (Namekian)
        Xayda = 2       // Xayda (Saiyan)
    }

    /// <summary>
    /// Trạng thái hoạt động của nhân vật (statusMe).
    /// </summary>
    public enum CharacterStatus : int
    {
        Stand = 1,
        Move = 2,
        Fly = 3,
        Fall = 4,
        Dead = 14
    }

    /// <summary>
    /// Chế độ PK của nhân vật (cTypePk).
    /// </summary>
    public enum PkMode : int
    {
        None = 0,           // Bình thường (Không PK)
        Red = 1,            // Đồ sát (Cờ đỏ)
        Flag = 2,           // Bật cờ PK
        Challenge = 3,      // Thách đấu 1vs1
        Clan = 4,           // Đồ sát bang hội / Đệ tử
        Tournament = 5      // Đại hội võ thuật
    }

    /// <summary>
    /// Phân loại loại vật phẩm (ItemTemplate.type).
    /// </summary>
    public static class ItemTypes
    {
        public const int SHIRT = 0;         // Áo
        public const int PANT = 1;          // Quần
        public const int GLOVE = 2;         // Găng
        public const int SHOE = 3;          // Giày
        public const int RADAR = 4;         // Rada
        public const int DISGUISE = 5;      // Cải trang / Tóc
        public const int PEA = 6;           // Đậu thần
        public const int SKILL_BOOK = 7;    // Sách kỹ năng
        public const int GOLD = 9;          // Xu / Vàng
        public const int GEM = 10;          // Ngọc xanh (Lượng)
        public const int DRAGON_BALL = 12;  // Ngọc Rồng
        public const int LOCKED_GEM = 34;   // Ngọc đỏ (Lượng khóa)
    }

    /// <summary>
    /// Giới tính trang bị (ItemTemplate.gender).
    /// </summary>
    public static class ItemGenders
    {
        public const int TRAI_DAT = 0;
        public const int NAMEK = 1;
        public const int XAYDA = 2;
        public const int ALL = 3;           // Dùng chung cho cả 3 hành tinh
    }
}

namespace Game1
{
    using System;

    /// <summary>
    /// TabControll (Deprecated / No-Op).
    /// Client đã được chuẩn hóa sang kiến trúc Single-Client (1 Cửa Sổ = 1 Tài Khoản Duy Nhất).
    /// Class này được giữ lại dưới dạng No-Op để đảm bảo tính toàn vẹn và tương thích 100% cho mã nguồn.
    /// </summary>
    public class TabControll : mScreen
    {
        private static TabControll _Instance;
        public static TabControll Instance => _Instance ?? (_Instance = new TabControll());

        public static bool selectTab
        {
            get => false;
            set { }
        }

        public static bool isShow
        {
            get => false;
            set { }
        }

        public override void paint(mGraphics g)
        {
            // No-op: Single-Client khong can ve thanh tab
        }

        public bool isPointerHoldInTab()
        {
            return false;
        }

        public override void updateKey()
        {
        }
    }
}

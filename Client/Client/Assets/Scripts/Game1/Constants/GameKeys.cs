namespace Game1
{
    using System;

    /// <summary>
    /// GameKeys: Hệ thống chuẩn hóa mã phím và chỉ số mảng GameCanvas.keyPressed[].
    /// Loại bỏ triệt để các Magic Numbers kiểu: (!Main.isPC) ? 4 : 23 rải rác khắp mã nguồn.
    /// </summary>
    public static class GameKeys
    {
        // Phím trên điện thoại (Keypad Phone / J2ME)
        public const int PHONE_KEY_0 = 0;
        public const int PHONE_KEY_1 = 1;
        public const int PHONE_KEY_UP = 2;
        public const int PHONE_KEY_3 = 3;
        public const int PHONE_KEY_LEFT = 4;
        public const int PHONE_KEY_FIRE = 5;
        public const int PHONE_KEY_RIGHT = 6;
        public const int PHONE_KEY_7 = 7;
        public const int PHONE_KEY_DOWN = 8;
        public const int PHONE_KEY_9 = 9;
        public const int PHONE_KEY_STAR = 10;
        public const int PHONE_KEY_POUND = 11;

        // Phím chức năng mềm
        public const int KEY_SOFT_LEFT = 12;
        public const int KEY_SOFT_RIGHT = 13;
        public const int KEY_CLEAR = 14;

        // Phím trên bàn phím máy tính (PC Keyboard)
        public const int PC_KEY_UP = 21;
        public const int PC_KEY_DOWN = 22;
        public const int PC_KEY_LEFT = 23;
        public const int PC_KEY_RIGHT = 24;
        public const int PC_KEY_FIRE = 25;

        #region Helpers Kiểm Tra Trạng Thái Phím (Zero Allocation)

        /// <summary>
        /// Kiểm tra phím mũi tên TRÁI có đang được nhấn hay không (tự động phát hiện PC hay Phone).
        /// Thay thế cho biểu thức: GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23]
        /// </summary>
        public static bool IsLeftPressed()
        {
            return GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_LEFT : PC_KEY_LEFT];
        }

        /// <summary>
        /// Kiểm tra phím mũi tên PHẢI có đang được nhấn hay không.
        /// Thay thế cho biểu thức: GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24]
        /// </summary>
        public static bool IsRightPressed()
        {
            return GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_RIGHT : PC_KEY_RIGHT];
        }

        /// <summary>
        /// Kiểm tra phím mũi tên LÊN có đang được nhấn hay không.
        /// Thay thế cho biểu thức: GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21]
        /// </summary>
        public static bool IsUpPressed()
        {
            return GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_UP : PC_KEY_UP];
        }

        /// <summary>
        /// Kiểm tra phím mũi tên XUỐNG có đang được nhấn hay không.
        /// Thay thế cho biểu thức: GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22]
        /// </summary>
        public static bool IsDownPressed()
        {
            return GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_DOWN : PC_KEY_DOWN];
        }

        /// <summary>
        /// Kiểm tra phím CHỌN / BẮN / ENTER có đang được nhấn hay không.
        /// Thay thế cho biểu thức: GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25]
        /// </summary>
        public static bool IsFirePressed()
        {
            return GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_FIRE : PC_KEY_FIRE];
        }

        /// <summary>
        /// Kiểm tra phím chức năng TRÁI (Soft Left) có đang được nhấn hay không.
        /// </summary>
        public static bool IsSoftLeftPressed()
        {
            return GameCanvas.keyPressed[KEY_SOFT_LEFT];
        }

        /// <summary>
        /// Kiểm tra phím chức năng PHẢI (Soft Right) có đang được nhấn hay không.
        /// </summary>
        public static bool IsSoftRightPressed()
        {
            return GameCanvas.keyPressed[KEY_SOFT_RIGHT];
        }

        /// <summary>
        /// Xóa trạng thái phím trái sau khi đã xử lý xong.
        /// </summary>
        public static void ClearLeft()
        {
            GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_LEFT : PC_KEY_LEFT] = false;
        }

        /// <summary>
        /// Xóa trạng thái phím phải sau khi đã xử lý xong.
        /// </summary>
        public static void ClearRight()
        {
            GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_RIGHT : PC_KEY_RIGHT] = false;
        }

        /// <summary>
        /// Xóa trạng thái phím lên sau khi đã xử lý xong.
        /// </summary>
        public static void ClearUp()
        {
            GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_UP : PC_KEY_UP] = false;
        }

        /// <summary>
        /// Xóa trạng thái phím xuống sau khi đã xử lý xong.
        /// </summary>
        public static void ClearDown()
        {
            GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_DOWN : PC_KEY_DOWN] = false;
        }

        /// <summary>
        /// Xóa trạng thái phím chọn sau khi đã xử lý xong.
        /// </summary>
        public static void ClearFire()
        {
            GameCanvas.keyPressed[(!Main.isPC) ? PHONE_KEY_FIRE : PC_KEY_FIRE] = false;
        }

        #endregion
    }
}

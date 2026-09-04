namespace Game1
{
    /// <summary>
    /// Chuẩn Interface cho từng Tab/SubPanel con trong kiến trúc phân rã Panel.cs.
    /// Áp dụng Component Pattern & Strangler Fig Pattern để giảm tải God Class 11.500 dòng.
    /// </summary>
    public interface ISubPanel
    {
        void Init(Panel parent);

        /// <summary>
        /// Thực thi vẽ giao diện của SubPanel.
        /// </summary>
        /// <returns>true nếu SubPanel đã hoàn tất render (để Panel cha bỏ qua switch-case cũ)</returns>
        bool Paint(mGraphics g, int x, int y, int w, int h);

        void Update();

        /// <summary>
        /// Xử lý phím tắt và tương tác bàn phím.
        /// </summary>
        /// <returns>true nếu phím đã được xử lý</returns>
        bool UpdateKey();

        void OnSelectTab();

        void OnClose();
    }
}

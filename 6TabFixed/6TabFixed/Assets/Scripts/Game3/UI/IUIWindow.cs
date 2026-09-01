namespace Game3
{
    public interface IUIWindow
    {
        bool IsVisible { get; }
        bool IsModal { get; }
        void Update();
        void UpdateKey();
        void Paint(mGraphics g);
        void OnOpen();
        void OnClose();
    }
}

namespace Game3
{
    public interface ISubPanel
    {
        void Paint(mGraphics g, int x, int y, int w, int h);
        void Update();
        void UpdateKey();
        void OnSelectTab();
    }
}

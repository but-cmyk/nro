namespace Game1
{
    using System;

    /// <summary>
    /// SubPanel chuyên trách xử lý và hiển thị Tab Hành Trang (TYPE_BODY = 1).
    /// </summary>
    public class InventorySubPanel : ISubPanel
    {
        protected Panel parent;

        public void Init(Panel parent)
        {
            this.parent = parent;
        }

        public bool Paint(mGraphics g, int x, int y, int w, int h)
        {
            if (parent != null)
            {
                parent.paintInventory(g);
                return true;
            }
            return false;
        }

        public void Update()
        {
        }

        public bool UpdateKey()
        {
            return false;
        }

        public void OnSelectTab()
        {
        }

        public void OnClose()
        {
        }

        public bool PointerHoldIn(int x, int y)
        {
            return false;
        }

        public bool PointerClick(int x, int y)
        {
            return false;
        }

        public void OnTabChanged(int newTab)
        {
        }
    }
}

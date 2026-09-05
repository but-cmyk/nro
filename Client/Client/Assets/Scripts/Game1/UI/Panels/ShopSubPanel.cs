namespace Game1
{
    using System;

    /// <summary>
    /// ShopSubPanel: SubPanel chuyên trách xử lý và hiển thị Cửa Hàng / Shop Mua Bán (TYPE_SHOP = 1).
    /// </summary>
    public class ShopSubPanel : ISubPanel
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
                parent.paintShop(g);
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
    }
}

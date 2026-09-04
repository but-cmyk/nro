namespace Game1
{
    using System;

    /// <summary>
    /// SubPanel chuyên trách xử lý và hiển thị Tab Nâng Cấp / Ép Đồ (TYPE_COMBINE = 12).
    /// </summary>
    public class CombineSubPanel : ISubPanel
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
                parent.paintCombine(g);
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

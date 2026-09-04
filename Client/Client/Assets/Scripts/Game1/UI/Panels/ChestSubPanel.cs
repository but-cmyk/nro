namespace Game1
{
    using System;

    /// <summary>
    /// SubPanel chuyên trách xử lý và hiển thị Tab Rương Đồ (TYPE_BOX = 0).
    /// </summary>
    public class ChestSubPanel : ISubPanel
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
                parent.paintBox(g);
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

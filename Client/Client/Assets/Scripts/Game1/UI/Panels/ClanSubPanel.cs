namespace Game1
{
    using System;

    /// <summary>
    /// ClanSubPanel: SubPanel chuyên trách xử lý và hiển thị Giao diện Bang Hội / Clan (TYPE_CLANS = 5).
    /// Áp dụng Strangler Fig Pattern để phân rã Panel.cs.
    /// </summary>
    public class ClanSubPanel : ISubPanel
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
                parent.paintClans(g);
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
            if (parent != null)
            {
                parent.setTabClans();
            }
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

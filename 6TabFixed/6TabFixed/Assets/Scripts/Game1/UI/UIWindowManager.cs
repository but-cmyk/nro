namespace Game1
{
    using System;
    using System.Collections.Generic;

    public class UIWindowManager
    {
        private static UIWindowManager _instance;
        public static UIWindowManager gI()
        {
            if (_instance == null)
            {
                _instance = new UIWindowManager();
            }
            return _instance;
        }

        private readonly List<IUIWindow> _activeWindows = new List<IUIWindow>();

        public IUIWindow TopWindow
        {
            get
            {
                if (_activeWindows.Count > 0)
                {
                    return _activeWindows[_activeWindows.Count - 1];
                }
                return null;
            }
        }

        public bool HasActiveModal
        {
            get
            {
                for (int i = _activeWindows.Count - 1; i >= 0; i--)
                {
                    if (_activeWindows[i] != null && _activeWindows[i].IsVisible && _activeWindows[i].IsModal)
                    {
                        return true;
                    }
                }
                return false;
            }
        }

        public void Push(IUIWindow window)
        {
            if (window == null) return;

            // Bring to top if already present
            _activeWindows.Remove(window);

            // Auto-hide overlapping panels if window is a modal
            if (window.IsModal)
            {
                if (GameCanvas.panel != null && GameCanvas.panel.isShow)
                {
                    GameCanvas.panel.hide();
                }
                if (GameCanvas.panel2 != null && GameCanvas.panel2.isShow)
                {
                    GameCanvas.panel2.hide();
                }
            }

            _activeWindows.Add(window);
            window.OnOpen();
        }

        public void Close(IUIWindow window)
        {
            if (window == null) return;
            if (_activeWindows.Remove(window))
            {
                window.OnClose();
            }
        }

        public void Pop()
        {
            if (_activeWindows.Count > 0)
            {
                IUIWindow top = _activeWindows[_activeWindows.Count - 1];
                _activeWindows.RemoveAt(_activeWindows.Count - 1);
                top.OnClose();
            }
        }

        public void CloseAll()
        {
            for (int i = _activeWindows.Count - 1; i >= 0; i--)
            {
                if (_activeWindows[i] != null)
                {
                    _activeWindows[i].OnClose();
                }
            }
            _activeWindows.Clear();
        }

        public bool UpdateKey()
        {
            for (int i = _activeWindows.Count - 1; i >= 0; i--)
            {
                IUIWindow win = _activeWindows[i];
                if (win != null && win.IsVisible)
                {
                    win.UpdateKey();
                    if (win.IsModal)
                    {
                        return true; // Modal consumes input, blocking background layers
                    }
                }
            }
            return false;
        }

        public void Update()
        {
            for (int i = 0; i < _activeWindows.Count; i++)
            {
                IUIWindow win = _activeWindows[i];
                if (win != null && win.IsVisible)
                {
                    win.Update();
                }
            }
        }

        public void Paint(mGraphics g)
        {
            for (int i = 0; i < _activeWindows.Count; i++)
            {
                IUIWindow win = _activeWindows[i];
                if (win != null && win.IsVisible)
                {
                    win.Paint(g);
                }
            }
        }
    }
}

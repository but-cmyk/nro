using System.Collections;
using UnityEngine;

namespace Game1.God
{
    public class AutoControlPanel
    {
        private static AutoControlPanel instance;
        public bool isShow = false;
        private int xBtn, yBtn;
        private int panelW = 110;
        private int panelH = 140;
        
        public static AutoControlPanel getInstance()
        {
            if (instance == null) instance = new AutoControlPanel();
            return instance;
        }
        
        public void paint(mGraphics g)
        {
            // Auto button and panel removed per user request
        }
        
        public void updateTouch()
        {
            // Auto button touch handling removed per user request
        }
        
        private bool checkToggleClick(int x, int y)
        {
            if (GameCanvas.isPointerHoldIn(x, y, panelW, 15))
            {
                GameCanvas.isPointerClick = false;
                return true;
            }
            return false;
        }
    }
}

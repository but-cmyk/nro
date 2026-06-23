using System;

namespace Game2
{
    
    public class TabCommand : BaseCommand
    {
        private static Image menu, menu1;
        public TabCommand(string caption, Action action) : base(caption, action)
        {
            this.caption = caption;
            this.action = action;
            this.w = 20;
            this.h = 20;
        }
        public static void loadBG()
        {
            menu = GameCanvas.loadImage("/mainImage/img1.png");
            menu1 = GameCanvas.loadImage("/mainImage/img2.png");
        }
        public override void paint(mGraphics g)
        {
            g.drawImage(isFocus ? menu1 : menu, x, y);
            mFont.tahoma_7b_dark.drawString(g, caption, x + w / 2 + 1, y + h / 2 - mFont.tahoma_7b_dark.getHeight() / 2, 3);
        }
    
        public override bool isPointerInside()
        {
            isFocus = false;
            if (GameCanvas.isPointerHoldIn(x, y, w, h))
            {
                if (GameCanvas.isPointerDown)
                {
                    isFocus = true;
                }
                if(GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                {
                    return true;
                }
            }
            return false;
        }
        public override void Invoke()
        {
            GameCanvas.clearAllPointerEvent();
            action?.Invoke();
        }
    }
}

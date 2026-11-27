namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    
    public partial class Char : IMapObject
    {
    
    	public void printlog()
    	{
    		string empty = string.Empty;
    		string text = empty;
    		empty = text + "isInjure " + isInjure + "\n";
    		text = empty;
    		empty = text + "isInjure " + isMonkey + "\n";
    		text = empty;
    		empty = text + "isInjure " + isAddChopMat + "\n";
    		text = empty;
    		empty = text + "isInjure " + isAttack + "\n";
    		text = empty;
    		empty = text + "isInjure " + isAttFly + "\n";
    		text = empty;
    		empty = text + "isInjure " + ischangingMap + "\n";
    		text = empty;
    		empty = text + "isInjure " + isCharge + "\n";
    		text = empty;
    		empty = text + "isInjure " + isCopy + "\n";
    		text = empty;
    		empty = text + "isInjure " + isCreateDark + "\n";
    		text = empty;
    		empty = text + "isInjure " + isCrit + "\n";
    		text = empty;
    		empty = text + "isInjure " + isDirtyPostion + "\n";
    		text = empty;
    		empty = text + "isInjure " + isEndMount + "\n";
    		text = empty;
    		empty = text + "isInjure " + isEventMount + "\n";
    		text = empty;
    		empty = text + "isInjure " + isMafuba + "\n";
    		text = empty;
    		empty = text + "isInjure " + isFusion + "\n";
    		text = empty;
    		empty = text + "isInjure " + isFeetEff + "\n";
    		text = empty;
    		empty = text + "isInjure " + isFlying + "\n";
    		text = empty;
    		empty = text + "isInjure " + isWaitMonkey + "\n";
    		text = empty;
    		empty = text + "isInjure " + isUseSkillSpec() + "\n";
    		text = empty;
    		empty = text + "isInjure " + isDie + "\n";
    		text = empty;
    		empty = text + "isInjure " + isDie + "\n";
    		text = empty;
    		empty = text + "isInjure " + isDie + "\n";
    		text = empty;
    		empty = text + "isInjure " + isDie + "\n";
    		Res.outz(empty);
    	}
    
    	public void setDanhHieu(int smallDanhHieu, int frame)
    	{
    		smallDanhHieu = 0;
    		frame = 1;
    		if (mainImg == null)
    		{
    			mainImg = ImgByName.getImagePath("banner_" + 0, ImgByName.hashImagePath);
    		}
    		if (mainImg.img != null)
    		{
    			int num = mainImg.img.getHeight() / mainImg.nFrame;
    			if (num < 1)
    			{
    				num = 1;
    			}
    			fraDanhHieu = new FrameImage(mainImg.img, mainImg.img.getWidth(), num);
    		}
    		Res.err("===== tim thay DanhHieu ve danh hieu ra");
    	}
    }
}

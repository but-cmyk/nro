namespace Game1
{
    using System;

    public class ServerEffect : Effect2
    {
    	public EffectCharPaint eff;
    
    	private int i0;
    
    	private int dx0;
    
    	private int dy0;
    
    	private int x;
    
    	private int y;
    
    	private Char c;
    
    	private Mob m;
    
    	private short loopCount;
    
    	private long endTime;
    
    	private int trans;

        private static EffectCharPaint getEffect(int id)
        {
            try
            {
                if (GameScr.efs == null)
                {
                    GameScr.gI().readEfect();
                }
                if (GameScr.efs != null && id - 1 >= 0 && id - 1 < GameScr.efs.Length)
                {
                    return GameScr.efs[id - 1];
                }
            }
            catch (Exception) { }
            return null;
        }
    
    	public static void addServerEffect(int id, int cx, int cy, int loopCount)
    	{
            try
            {
                EffectCharPaint eff = getEffect(id);
                if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0) return;
                ServerEffect serverEffect = new ServerEffect();
                serverEffect.eff = eff;
                serverEffect.x = cx;
                serverEffect.y = cy;
                serverEffect.loopCount = (short)loopCount;
                if (Effect2.vEffect2 != null) Effect2.vEffect2.addElement(serverEffect);
            }
            catch (Exception) { }
    	}
    
    	public static void addServerEffect(int id, int cx, int cy, int loopCount, int trans)
    	{
            try
            {
                EffectCharPaint eff = getEffect(id);
                if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0) return;
                ServerEffect serverEffect = new ServerEffect();
                serverEffect.eff = eff;
                serverEffect.x = cx;
                serverEffect.y = cy;
                serverEffect.loopCount = (short)loopCount;
                serverEffect.trans = trans;
                if (Effect2.vEffect2 != null) Effect2.vEffect2.addElement(serverEffect);
            }
            catch (Exception) { }
    	}
    
    	public static void addServerEffect(int id, Mob m, int loopCount)
    	{
            try
            {
                EffectCharPaint eff = getEffect(id);
                if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0) return;
                ServerEffect serverEffect = new ServerEffect();
                serverEffect.eff = eff;
                serverEffect.m = m;
                serverEffect.loopCount = (short)loopCount;
                if (Effect2.vEffect2 != null) Effect2.vEffect2.addElement(serverEffect);
            }
            catch (Exception) { }
    	}
    
    	public static void addServerEffect(int id, Char c, int loopCount)
    	{
            try
            {
                EffectCharPaint eff = getEffect(id);
                if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0) return;
                ServerEffect serverEffect = new ServerEffect();
                serverEffect.eff = eff;
                serverEffect.c = c;
                serverEffect.loopCount = (short)loopCount;
                if (Effect2.vEffect2 != null) Effect2.vEffect2.addElement(serverEffect);
            }
            catch (Exception) { }
    	}
    
    	public static void addServerEffect(int id, Char c, int loopCount, int trans)
    	{
            try
            {
                EffectCharPaint eff = getEffect(id);
                if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0) return;
                ServerEffect serverEffect = new ServerEffect();
                serverEffect.eff = eff;
                serverEffect.c = c;
                serverEffect.loopCount = (short)loopCount;
                serverEffect.trans = trans;
                if (Effect2.vEffect2 != null) Effect2.vEffect2.addElement(serverEffect);
            }
            catch (Exception) { }
    	}
    
    	public static void addServerEffectWithTime(int id, int cx, int cy, int timeLengthInSecond)
    	{
            try
            {
                EffectCharPaint eff = getEffect(id);
                if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0) return;
                ServerEffect serverEffect = new ServerEffect();
                serverEffect.eff = eff;
                serverEffect.x = cx;
                serverEffect.y = cy;
                serverEffect.endTime = mSystem.currentTimeMillis() + timeLengthInSecond * 1000;
                if (Effect2.vEffect2 != null) Effect2.vEffect2.addElement(serverEffect);
            }
            catch (Exception) { }
    	}
    
    	public static void addServerEffectWithTime(int id, Char c, int timeLengthInSecond)
    	{
            try
            {
                EffectCharPaint eff = getEffect(id);
                if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0) return;
                ServerEffect serverEffect = new ServerEffect();
                serverEffect.eff = eff;
                serverEffect.c = c;
                serverEffect.endTime = mSystem.currentTimeMillis() + timeLengthInSecond * 1000;
                if (Effect2.vEffect2 != null) Effect2.vEffect2.addElement(serverEffect);
            }
            catch (Exception) { }
    	}
    
    	public override void paint(mGraphics g)
    	{
            if (eff == null || eff.arrEfInfo == null || i0 < 0 || i0 >= eff.arrEfInfo.Length)
            {
                return;
            }
    		if (mGraphics.zoomLevel == 1)
    		{
    			GameScr.countEff++;
    		}
    		if (GameScr.countEff < 8)
    		{
    			if (c != null)
    			{
    				x = c.cx;
    				y = c.cy + GameCanvas.transY;
    			}
    			if (m != null)
    			{
    				x = m.x;
    				y = m.y + GameCanvas.transY;
    			}
    			int num = x + dx0 + eff.arrEfInfo[i0].dx;
    			int num2 = y + dy0 + eff.arrEfInfo[i0].dy;
    			if (GameCanvas.isPaint(num, num2))
    			{
    				SmallImage.drawSmallImage(g, eff.arrEfInfo[i0].idImg, num, num2, trans, mGraphics.VCENTER | mGraphics.HCENTER);
    			}
    		}
    	}
    
    	public override void update()
    	{
            if (eff == null || eff.arrEfInfo == null || eff.arrEfInfo.Length == 0)
            {
                if (Effect2.vEffect2 != null) Effect2.vEffect2.removeElement(this);
                return;
            }
    		if (endTime != 0)
    		{
    			i0++;
    			if (i0 >= eff.arrEfInfo.Length)
    			{
    				i0 = 0;
    			}
    			if (mSystem.currentTimeMillis() - endTime > 0)
    			{
    				Effect2.vEffect2.removeElement(this);
    			}
    		}
    		else
    		{
    			i0++;
    			if (i0 >= eff.arrEfInfo.Length)
    			{
    				loopCount--;
    				if (loopCount <= 0)
    				{
    					Effect2.vEffect2.removeElement(this);
    				}
    				else
    				{
    					i0 = 0;
    				}
    			}
    		}
    		if (GameCanvas.gameTick % 11 == 0 && c != null && c != Char.myCharz() && !GameScr.vCharInMap.contains(c))
    		{
    			Effect2.vEffect2.removeElement(this);
    		}
    	}
    }
}

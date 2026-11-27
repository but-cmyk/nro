namespace Game1
{
    using System;
    
    public partial class Effect_End
    {
    	public const sbyte Lvlpaint_All = -1;
    
    	public const sbyte Lvlpaint_Front = 0;
    
    	public const sbyte Lvlpaint_Mid = 1;
    
    	public const sbyte Lvlpaint_Mid_2 = 2;
    
    	public const sbyte Lvlpaint_Behind = 3;
    
    	public const short End_String_Lose = 0;
    
    	public const short End_String_Win = 1;
    
    	public const short End_String_Draw = 2;
    
    	public const short End_FireWork = 3;
    
    	public const short End_line_in = 9;
    
    	public const short End_e8_rock = 10;
    
    	public const short End_e8_ice = 11;
    
    	public const short End_SUB_MaFuBa = 16;
    
    	public const short End_SUB_Destroy = 17;
    
    	public const short End_POW_Kamex10 = 18;
    
    	public const short End_POW_Destroy = 19;
    
    	public const short End_POW_MaFuBa = 20;
    
    	public const short End_GONG_Kamex10 = 21;
    
    	public const short End_GONG_Destroy = 22;
    
    	public const short End_GONG_MaFuBa = 23;
    
    	public const short End_Skill_Kamex10 = 24;
    
    	public const short End_Skill_Destroy = 25;
    
    	public const short End_Skill_MaFuBa = 26;
    
    	private MyVector VecEffEnd = new MyVector("EffectEnd VecEffEnd");
    
    	public FrameImage fraImgEff;
    
    	public byte[] nFrame = new byte[10];
    
    	public byte[] nFrame_2 = new byte[10];
    
    	public int typePaint;
    
    	public int typeEffect;
    
    	public int typeSub;
    
    	public int range;
    
    	public short idEndeff;
    
    	public int fRemove;
    
    	public int fMove;
    
    	public int n_frame;
    
    	public int x;
    
    	public int y;
    
    	public int w;
    
    	public int h;
    
    	public int dir;
    
    	public int dir_nguoc;
    
    	public int levelPaint;
    
    	public int f;
    
    	public int frame;
    
    	public int fSpeed;
    
    	public int vx;
    
    	public int vy;
    
    	public int x1000;
    
    	public int y1000;
    
    	public int vx1000;
    
    	public int vy1000;
    
    	public int dy_throw;
    
    	public int vMax;
    
    	public int toX;
    
    	public int toY;
    
    	public int stt;
    
    	public int dx;
    
    	public int dy;
    
    	public short timeRemove;
    
    	public long time;
    
    	public bool isRemove;
    
    	public bool isAddSub;
    
    	public Char charUse;
    
    	public Point[] listObj;
    
    	public Point target;
    
    	public static short[][] arrInfoEff = new short[29][]
    	{
    		new short[3] { 68, 264, 4 },
    		new short[3] { 30, 120, 4 },
    		new short[3] { 66, 280, 4 },
    		new short[3] { 0, 0, 1 },
    		new short[3] { 111, 68, 2 },
    		new short[3] { 90, 68, 2 },
    		new short[3] { 125, 68, 2 },
    		new short[3] { 47, 282, 6 },
    		new short[3] { 10, 40, 4 },
    		new short[3] { 92, 525, 7 },
    		new short[3] { 62, 372, 6 },
    		new short[3] { 80, 352, 4 },
    		new short[3] { 80, 352, 4 },
    		new short[3] { 80, 352, 4 },
    		new short[3] { 72, 240, 3 },
    		new short[3] { 20, 42, 3 },
    		new short[3] { 65, 160, 4 },
    		new short[3] { 50, 300, 6 },
    		new short[3] { 84, 168, 2 },
    		new short[3] { 90, 540, 6 },
    		new short[3] { 180, 900, 6 },
    		new short[3] { 62, 186, 3 },
    		new short[3] { 34, 80, 4 },
    		new short[3] { 140, 560, 4 },
    		new short[3] { 64, 600, 6 },
    		new short[3] { 36, 200, 5 },
    		new short[3] { 35, 200, 5 },
    		new short[3] { 50, 250, 5 },
    		new short[3] { 50, 240, 6 }
    	};
    
    	public int life;
    
    	public int goc_Arc;
    
    	public int va;
    
    	public int gocT_Arc;
    
    	public byte[] mpaintone_Arrow = new byte[24]
    	{
    		12, 11, 10, 9, 8, 7, 6, 5, 4, 3,
    		2, 1, 0, 23, 22, 21, 20, 19, 18, 17,
    		16, 15, 14, 13
    	};
    
    	public byte[] mImageArrow = new byte[24]
    	{
    		0, 0, 2, 1, 1, 2, 0, 0, 2, 1,
    		1, 2, 0, 0, 2, 1, 1, 2, 0, 0,
    		2, 1, 1, 2
    	};
    
    	public byte[] mXoayArrow = new byte[24]
    	{
    		2, 2, 3, 3, 3, 4, 5, 5, 5, 5,
    		5, 1, 0, 0, 0, 0, 0, 7, 6, 6,
    		6, 6, 6, 2
    	};
    
    	private int rS;
    
    	private int angleS;
    
    	private int angleO;
    
    	private int iAngleS;
    
    	private int iDotS;
    
    	private int[] xArgS;
    
    	private int[] yArgS;
    
    	private int[] xDotS;
    
    	private int[] yDotS;
    
    	public static int[][] colorStar = new int[3][]
    	{
    		new int[3] { 16310304, 16298056, 16777215 },
    		new int[3] { 7045120, 12643960, 16777215 },
    		new int[3] { 2407423, 11987199, 16777215 }
    	};
    
    	private int[] colorpaint;
    
    	private int indexColorStar;
    
    	private int xline;
    
    	private int yline;
    
    	private FrameImage[] fra_skill;
    
    	public Effect_End(int type, int typeSub, int x, int y, int levelPaint, int dir, short timeRemove, Point[] listObj)
    	{
    		f = 0;
    		stt = 0;
    		typeEffect = type;
    		this.typeSub = typeSub;
    		this.x = x;
    		this.y = y;
    		this.levelPaint = levelPaint;
    		this.dir = dir;
    		dir_nguoc = ((dir == -1) ? 2 : 0);
    		time = mSystem.currentTimeMillis();
    		this.timeRemove = timeRemove;
    		isRemove = (isAddSub = false);
    		n_frame = 4;
    		if (listObj != null)
    		{
    			this.listObj = new Point[listObj.Length];
    			for (int i = 0; i < this.listObj.Length; i++)
    			{
    				this.listObj[i] = listObj[i];
    			}
    		}
    		get_Img_Skill();
    		create_Effect();
    	}
    
    	public Effect_End(int type, int typeSub, int typePaint, Char charUse, Point target, int levelPaint, short timeRemove, short range)
    	{
    		f = 0;
    		stt = 0;
    		typeEffect = type;
    		this.typeSub = typeSub;
    		this.typePaint = typePaint;
    		this.charUse = charUse;
    		if (charUse.containsCaiTrang(1265))
    		{
    			if (typeEffect == 21 || typeEffect == 22 || typeEffect == 23)
    			{
    				this.charUse.cx += 10 * this.charUse.cdir;
    			}
    			else if (typeEffect == 18 || typeEffect == 19 || typeEffect == 20)
    			{
    				this.charUse.cx += -15 * this.charUse.cdir;
    			}
    			else
    			{
    				this.charUse.cx += 15 * this.charUse.cdir;
    			}
    		}
    		x = this.charUse.cx;
    		y = this.charUse.cy;
    		dir = this.charUse.cdir;
    		dir_nguoc = ((dir == -1) ? 2 : 0);
    		this.target = target;
    		this.levelPaint = levelPaint;
    		time = mSystem.currentTimeMillis();
    		this.timeRemove = timeRemove;
    		this.range = range;
    		isRemove = (isAddSub = false);
    		n_frame = 4;
    		get_Img_Skill();
    		create_Effect();
    	}
    
    	public Effect_End(int type, int typeSub, int typePaint, int x, int y, int levelPaint, int dir, short timeRemove, Point[] listObj)
    	{
    		f = 0;
    		stt = 0;
    		typeEffect = type;
    		this.typeSub = typeSub;
    		this.typePaint = typePaint;
    		this.x = x;
    		this.y = y;
    		this.levelPaint = levelPaint;
    		this.dir = dir;
    		dir_nguoc = ((dir == -1) ? 2 : 0);
    		time = mSystem.currentTimeMillis();
    		this.timeRemove = timeRemove;
    		isRemove = (isAddSub = false);
    		n_frame = 4;
    		if (listObj != null)
    		{
    			this.listObj = new Point[listObj.Length];
    			for (int i = 0; i < this.listObj.Length; i++)
    			{
    				this.listObj[i] = listObj[i];
    			}
    		}
    		get_Img_Skill();
    		create_Effect();
    	}
    
    	public static Image getImage(int id)
    	{
    		if (id < 0)
    		{
    			return null;
    		}
    		string path = "/e/e_" + id + ".png";
    		Image result = null;
    		try
    		{
    			result = mSystem.loadImage(path);
    		}
    		catch (Exception)
    		{
    		}
    		return result;
    	}
    
    	public static void setSoundSkill_END(int x, int y, int typeEffect)
    	{
    		try
    		{
    			int num = -1;
    			int num2 = Res.random(3);
    			if (num >= 0)
    			{
    				SoundMn.playSound(x, y, num, SoundMn.volume);
    			}
    		}
    		catch (Exception ex)
    		{
    			Res.err("ERR setSoundSkill_END: " + ex.ToString());
    		}
    	}
    
    	public void create_Effect()
    	{
    		try
    		{
    			setSoundSkill_END(x, y, typeEffect);
    			switch (typeEffect)
    			{
    			case 0:
    			case 1:
    			case 2:
    				set_End_String(typeEffect);
    				break;
    			case 3:
    				set_FireWork();
    				break;
    			case 16:
    			case 17:
    				set_Sub();
    				break;
    			case 18:
    			case 19:
    			case 20:
    				set_Pow();
    				break;
    			case 21:
    			case 22:
    			case 23:
    				set_Gong();
    				break;
    			case 24:
    				set_Skill_Kamex10();
    				break;
    			case 25:
    				set_Skill_Destroy();
    				break;
    			case 26:
    				set_Skill_MaFuba();
    				break;
    			case 9:
    				set_LINE_IN();
    				break;
    			case 10:
    			case 11:
    				set_End_Rock();
    				break;
    			}
    		}
    		catch (Exception ex)
    		{
    			Res.err("ERR create_Effect: " + ex.ToString());
    			removeEff();
    		}
    	}
    
    	public void update()
    	{
    		try
    		{
    			f++;
    			switch (typeEffect)
    			{
    			case 0:
    			case 1:
    			case 2:
    				upd_End_String();
    				break;
    			case 3:
    				upd_FireWork();
    				break;
    			case 16:
    			case 17:
    				upd_Sub();
    				break;
    			case 18:
    			case 19:
    			case 20:
    				upd_Pow();
    				break;
    			case 21:
    			case 22:
    			case 23:
    				upd_Gong();
    				break;
    			case 24:
    				upd_Skill_Kamex10();
    				break;
    			case 25:
    				upd_Skill_Destroy();
    				break;
    			case 26:
    				upd_Skill_MaFuba();
    				break;
    			case 9:
    				upd_LINE_IN();
    				break;
    			case 10:
    			case 11:
    				upd_End_Rock();
    				break;
    			}
    		}
    		catch (Exception ex)
    		{
    			Res.err("ERR update: " + ex.ToString());
    			removeEff();
    		}
    	}
    
    	public void paint(mGraphics g)
    	{
    		try
    		{
    			if (isRemove || f < 0)
    			{
    				return;
    			}
    			switch (typeEffect)
    			{
    			case 0:
    			case 1:
    			case 2:
    				pnt_End_String(g);
    				break;
    			case 3:
    				pnt_FireWork(g);
    				break;
    			case 17:
    				pnt_Sub(g, mGraphics.VCENTER);
    				break;
    			case 16:
    				if (typeSub == 0)
    				{
    					pnt_Sub(g, mGraphics.BOTTOM | mGraphics.HCENTER);
    				}
    				else
    				{
    					pnt_Sub(g, mGraphics.VCENTER | mGraphics.HCENTER);
    				}
    				break;
    			case 18:
    			case 19:
    			case 20:
    				pnt_Pow(g, mGraphics.BOTTOM | mGraphics.HCENTER);
    				break;
    			case 21:
    			case 22:
    			case 23:
    				pnt_Gong(g, mGraphics.VCENTER | mGraphics.HCENTER);
    				break;
    			case 24:
    				pnt_Skill_Kamex10(g);
    				break;
    			case 25:
    				pnt_Skill_Destroy(g);
    				break;
    			case 26:
    				pnt_Skill_MaFuba(g);
    				break;
    			case 9:
    				pnt_LINE_IN(g);
    				break;
    			case 10:
    			case 11:
    				pnt_End_Rock(g);
    				break;
    			}
    		}
    		catch (Exception ex)
    		{
    			Res.err(ex.ToString());
    			removeEff();
    		}
    	}
    
    	public void removeEff()
    	{
    		isRemove = true;
    	}
    
    	public void createDanFocus(bool isRandom, Char obj)
    	{
    		if (isRandom)
    		{
    			switch (Res.random(4))
    			{
    			case 0:
    				gocT_Arc = 90;
    				break;
    			case 1:
    				gocT_Arc = 270;
    				break;
    			case 2:
    				gocT_Arc = 180;
    				break;
    			case 3:
    				gocT_Arc = 0;
    				break;
    			}
    		}
    		else if (obj.cdir == 1)
    		{
    			gocT_Arc = 0;
    		}
    		else
    		{
    			gocT_Arc = 180;
    		}
    		va = (short)(256 * vMax);
    		vx = 0;
    		vy = 0;
    		life = 0;
    		vx1000 = va * Res.cos(gocT_Arc) >> 10;
    		vy1000 = va * Res.sin(gocT_Arc) >> 10;
    	}
    
    	public void updateAngleXP(int fmove)
    	{
    		if (f < fmove)
    		{
    			return;
    		}
    		if (charUse == null || target == null || f >= fRemove)
    		{
    			f = fRemove;
    			return;
    		}
    		int num = target.x - charUse.cx;
    		int num2 = target.y - charUse.cy;
    		life++;
    		if ((Res.abs(num) < 10 && Res.abs(num2) < 10) || life > fRemove)
    		{
    			f = fRemove;
    			return;
    		}
    		int num3 = Res.angle(num, num2);
    		if (Res.abs(num3 - gocT_Arc) < 90 || num * num + num2 * num2 > 4096)
    		{
    			if (Res.abs(num3 - gocT_Arc) < 15)
    			{
    				gocT_Arc = num3;
    			}
    			else if ((num3 - gocT_Arc >= 0 && num3 - gocT_Arc < 180) || num3 - gocT_Arc < -180)
    			{
    				gocT_Arc = Res.fixangle(gocT_Arc + 15);
    			}
    			else
    			{
    				gocT_Arc = Res.fixangle(gocT_Arc - 15);
    			}
    		}
    		if (f > fRemove * 2 / 3 && va < 8192)
    		{
    			va += 3096;
    		}
    		vx1000 = va * Res.cos(gocT_Arc) >> 10;
    		vy1000 = va * Res.sin(gocT_Arc) >> 10;
    		num += vx1000;
    		int num4 = num >> 10;
    		x += num4;
    		num &= 0x3FF;
    		num2 += vy1000;
    		int num5 = num2 >> 10;
    		y += num5;
    		num2 &= 0x3FF;
    	}
    
    	public int setFrameAngle(int goc)
    	{
    		if (goc <= 15 || goc > 345)
    		{
    			return 12;
    		}
    		int num = (goc - 15) / 15 + 1;
    		if (num > 24)
    		{
    			num = 24;
    		}
    		return mpaintone_Arrow[num];
    	}
    
    	public void create_Arrow(int vMax, Point targetPoint)
    	{
    		this.vMax = vMax;
    		int num = 0;
    		int num2 = 0;
    		if (targetPoint != null)
    		{
    			num = targetPoint.x - x;
    			num2 = targetPoint.y - y;
    			toX = targetPoint.x;
    			toY = targetPoint.y;
    		}
    		else
    		{
    			num = toX - x;
    			num2 = toY - y;
    		}
    		if (x > toX)
    		{
    			dir = 2;
    			dir_nguoc = 0;
    		}
    		else
    		{
    			dir = 0;
    			dir_nguoc = 2;
    		}
    		int frameAngle = Res.angle(num, num2);
    		frame = setFrameAngle(frameAngle);
    		fSpeed = frame;
    		create_Speed(num, num2);
    	}
    
    	public void create_Speed(int dx, int dy)
    	{
    		int num = 0;
    		int num2 = 0;
    		int num3 = Res.getDistance(dx, dy) / vMax;
    		if (num3 == 0)
    		{
    			num3 = 1;
    		}
    		num = dx / num3;
    		num2 = dy / num3;
    		if (num == 0 && dx < num3)
    		{
    			num = ((dx >= 0) ? 1 : (-1));
    		}
    		if (num2 == 0 && dy < num3)
    		{
    			num2 = ((dy >= 0) ? 1 : (-1));
    		}
    		if (Res.abs(num) > Res.abs(dx))
    		{
    			num = dx;
    		}
    		if (Res.abs(num2) > Res.abs(dy))
    		{
    			num2 = dy;
    		}
    		vx = num;
    		vy = num2;
    	}
    
    	public void moveTo_xy(int toX, int toY, int fMove, int typeEff_End, int rangeEnd)
    	{
    		if (f < fMove)
    		{
    			frame = setFrameAngle((dir == -1) ? 180 : 0);
    			return;
    		}
    		frame = fSpeed;
    		if (Res.abs(x - toX) < Res.abs(vx))
    		{
    			x = toX;
    			vx = 0;
    		}
    		else
    		{
    			x += vx;
    		}
    		if (Res.abs(y - toY) < Res.abs(vy))
    		{
    			y = toY;
    			vy = 0;
    		}
    		else
    		{
    			y += vy;
    		}
    		if (Res.abs(x - toX) >= Res.abs(vMax) || Res.abs(y - toY) >= Res.abs(vMax) || typeEff_End < 0)
    		{
    			return;
    		}
    		if (target != null)
    		{
    			int num = target.x;
    			int num2 = target.y;
    			if (rangeEnd > 0)
    			{
    				num += Res.random_Am(0, rangeEnd);
    				num2 += Res.random_Am(0, rangeEnd);
    			}
    			GameScr.addEffectEnd(typeEff_End, 0, 0, num, num2, 1, 0, -1, null);
    			removeEff();
    		}
    		else if (isAddSub)
    		{
    			isAddSub = false;
    			int num3 = x;
    			int num4 = y;
    			if (rangeEnd > 1)
    			{
    				num3 += Res.random_Am_0(rangeEnd);
    				num4 += Res.random_Am_0(rangeEnd);
    			}
    			GameScr.addEffectEnd(typeEff_End, 0, 0, num3, num4, 1, 0, -1, null);
    		}
    	}
    
    	public void paint_Arrow(mGraphics g, FrameImage frm, int index, int x, int y, int anchor, bool isCountFr)
    	{
    		if (frm != null)
    		{
    			int num = frm.nFrame / 3;
    			if (num < 1)
    			{
    				num = 1;
    			}
    			int num2 = 0;
    			int num3 = 3;
    			if (frm.nFrame <= 6)
    			{
    				num2 = ((frm.nFrame <= 3) ? (f % num) : ((f / num3 % 2 != 0) ? 3 : 0));
    			}
    			else
    			{
    				num = 1;
    				num2 = ((f / num3 - fMove > 8) ? 6 : ((f / num3 - fMove > 4) ? 3 : 0));
    			}
    			int idx = num * mImageArrow[index] + num2;
    			if (frm.nFrame < 3)
    			{
    				idx = f / num3 % frm.nFrame;
    			}
    			if (isCountFr)
    			{
    				idx = f / num3 % frm.nFrame;
    			}
    			frm.drawFrame(idx, x, y, mXoayArrow[index], anchor, g);
    		}
    	}
    
    	private void set_End_String(int typeEffect)
    	{
    		switch (typeEffect)
    		{
    		case 0:
    			fraImgEff = new FrameImage(4);
    			break;
    		case 1:
    			fraImgEff = new FrameImage(5);
    			break;
    		case 2:
    			fraImgEff = new FrameImage(6);
    			break;
    		}
    		fRemove = 100;
    		dy_throw = GameCanvas.h / 3 + 10;
    		vy = 10;
    		y1000 = 0;
    		isAddSub = false;
    	}
    
    	private void upd_End_String()
    	{
    		x = GameCanvas.hw;
    		y = y1000;
    		if (f > fRemove)
    		{
    			removeEff();
    		}
    		vy++;
    		if (vy > 15)
    		{
    			vy = 15;
    		}
    		if (y1000 + vy < dy_throw)
    		{
    			y1000 += vy;
    			return;
    		}
    		y1000 = dy_throw;
    		if (!isAddSub)
    		{
    			isAddSub = true;
    			if (typeSub != -1)
    			{
    				GameScr.addEffectEnd(typeSub, 0, 0, x, y, levelPaint, 0, -1, null);
    			}
    		}
    	}
    
    	private void pnt_End_String(mGraphics g)
    	{
    		if (fraImgEff != null)
    		{
    			fraImgEff.drawFrame(f / 5 % fraImgEff.nFrame, x, y, 0, 33, g);
    		}
    	}
    
    	private void set_FireWork()
    	{
    		int num = 0;
    		num = Res.random(3, 5);
    		fRemove = 90;
    		for (int i = 0; i < num; i++)
    		{
    			Point point = new Point();
    			point.x = x + Res.random_Am_0(4);
    			point.y = y + Res.random_Am_0(5);
    			if (typeSub == 0)
    			{
    				point.fRe = Res.random(10);
    				int num2 = 1;
    				if (i % 2 == 0)
    				{
    					num2 = -1;
    				}
    				point.x = x + Res.random(arrInfoEff[5][0] / 2) * num2;
    				point.y = y - Res.random(arrInfoEff[5][1] / 2);
    				point.fraImgEff = new FrameImage(7);
    			}
    			VecEffEnd.addElement(point);
    		}
    	}
    
    	private void upd_FireWork()
    	{
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Point point = (Point)VecEffEnd.elementAt(i);
    			point.update();
    			if (point.f == point.fRe)
    			{
    				SoundMn.playSound(point.x, point.y, SoundMn.FIREWORK, SoundMn.volume);
    			}
    			if (point.f - point.fRe <= point.fraImgEff.nFrame * 3 - 1)
    			{
    				continue;
    			}
    			point.f = 0;
    			if (typeSub == 0)
    			{
    				point.fRe = Res.random(10);
    				int num = 1;
    				if (i % 2 == 0)
    				{
    					num = -1;
    				}
    				point.x = x + Res.random(arrInfoEff[5][0] / 2) * num;
    				point.y = y - Res.random(arrInfoEff[5][1] / 2);
    			}
    		}
    		if (f >= fRemove)
    		{
    			removeEff();
    		}
    	}
    
    	private void pnt_FireWork(mGraphics g)
    	{
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Point point = (Point)VecEffEnd.elementAt(i);
    			if (point.f - point.fRe > -1 && point.fraImgEff != null)
    			{
    				point.fraImgEff.drawFrame((point.f - point.fRe) / 3 % point.fraImgEff.nFrame, point.x, point.y, 0, 3, g);
    			}
    		}
    	}
    
    	private void set_Skill_Kamex10()
    	{
    		w = fra_skill[0].frameWidth;
    		h = fra_skill[0].frameHeight;
    		vMax = Res.abs(x - target.x);
    		nFrame = new byte[6] { 0, 0, 0, 1, 1, 1 };
    		isAddSub = false;
    		SoundMn.playSound(x, y, SoundMn.KAMEX10_1, SoundMn.volume);
    	}
    
    	private void upd_Skill_Kamex10()
    	{
    		fSpeed++;
    		w += 20;
    		if (w > vMax)
    		{
    			w = vMax;
    		}
    		x = charUse.cx + 10;
    		y = charUse.cy - 3;
    		if (dir == -1)
    		{
    			x = charUse.cx - w - 10;
    		}
    		if (!isAddSub && GameCanvas.timeNow - time >= timeRemove)
    		{
    			f = 0;
    			nFrame = new byte[6] { 2, 2, 2, 3, 3, 3 };
    			isAddSub = true;
    		}
    		if (f > nFrame.Length - 1)
    		{
    			if (isAddSub)
    			{
    				removeEff();
    			}
    			else
    			{
    				f = 0;
    			}
    		}
    	}
    
    	private void pnt_Skill_Kamex10(mGraphics g)
    	{
    		if (fra_skill != null)
    		{
    			g.setClip(x, y - h / 2, w, h);
    			Fill_Rect_Img(g, fra_skill[0], fra_skill[1], fra_skill[2], nFrame[f], x, y, vMax);
    			GameCanvas.resetTransGameScr(g);
    			if (dir == -1 && fra_skill[0] != null)
    			{
    				fra_skill[0].drawFrame(nFrame[f], x + w - fra_skill[0].frameWidth, y - fra_skill[0].frameHeight / 2 - 1, 2, 0, g);
    			}
    		}
    	}
    
    	private void set_Skill_Destroy()
    	{
    		x = charUse.cx + 20 * charUse.cdir;
    		int num = 15;
    		fMove = timeRemove / num;
    		if (target != null)
    		{
    			for (int i = 0; i < num; i++)
    			{
    				Point point = new Point();
    				point.fraImgEff = fra_skill[0];
    				point.fraImgEff_2 = fra_skill[2];
    				point.x = x;
    				point.y = y;
    				if (target != null)
    				{
    					point.toX = target.x;
    					point.toY = target.y;
    					if (range > 0)
    					{
    						point.toX += Res.random_Am(0, range);
    						point.toY += Res.random_Am(0, range);
    					}
    				}
    				vMax = Res.random(9, 12);
    				if (i == num - 1)
    				{
    					point.fraImgEff = fra_skill[1];
    					point.fraImgEff_2 = fra_skill[3];
    					point.toX = target.x;
    					point.toY = target.y;
    					vMax = 9;
    				}
    				point.isPaint = false;
    				point.isChange = false;
    				point.isRemove = false;
    				point.create_Arrow(vMax);
    				VecEffEnd.addElement(point);
    			}
    		}
    		else
    		{
    			removeEff();
    		}
    	}
    
    	private void upd_Skill_Destroy()
    	{
    		int num = 0;
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Point point = (Point)VecEffEnd.elementAt(i);
    			if (!point.isPaint && GameCanvas.timeNow - time >= i * fMove)
    			{
    				point.isPaint = true;
    				GameScr.addEffectEnd(17, 0, typePaint, charUse.cx, charUse.cy - 3, 2, dir_nguoc, -1, null);
    				if (i == VecEffEnd.size() - 1)
    				{
    					SoundMn.playSound(point.x, point.y, SoundMn.DESTROY_1, SoundMn.volume);
    				}
    				else
    				{
    					SoundMn.playSound(point.x, point.y, SoundMn.DESTROY_0, SoundMn.volume);
    				}
    			}
    			if (point.isPaint && !point.isRemove)
    			{
    				point.f++;
    				if (!point.isChange)
    				{
    					if (point.f < 10 && i == VecEffEnd.size() - 1 && charUse != null && !TileMap.tileTypeAt(charUse.cx - (charUse.chw + 1) * charUse.cdir, charUse.cy, (charUse.cdir != 1) ? 4 : 8))
    					{
    						charUse.cx -= charUse.cdir;
    					}
    					point.moveTo_xy(point.toX, point.toY);
    					if (point.x == point.toX)
    					{
    						point.isChange = true;
    						point.f = 0;
    					}
    				}
    				if (point.isChange && point.f >= n_frame * point.fraImgEff_2.nFrame)
    				{
    					point.isRemove = true;
    				}
    			}
    			if (point.isRemove)
    			{
    				num++;
    			}
    		}
    		if (num == VecEffEnd.size())
    		{
    			removeEff();
    		}
    	}
    
    	private void pnt_Skill_Destroy(mGraphics g)
    	{
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Point point = (Point)VecEffEnd.elementAt(i);
    			if (point.isPaint && !point.isRemove)
    			{
    				if (!point.isChange)
    				{
    					point.paint_Arrow(g, point.fraImgEff, mGraphics.VCENTER | mGraphics.HCENTER, false);
    				}
    				if (point.isChange)
    				{
    					point.fraImgEff_2.drawFrame(point.f / n_frame % point.fraImgEff_2.nFrame, point.x, point.y, dir_nguoc, mGraphics.VCENTER | mGraphics.HCENTER, g);
    				}
    			}
    		}
    	}
    }
}

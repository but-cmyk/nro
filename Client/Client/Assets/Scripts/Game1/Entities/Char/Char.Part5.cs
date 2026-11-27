namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    
    public partial class Char : IMapObject
    {
    
    	public void sendUseChargeSkill()
    	{
    		if (me && (isFreez || isUsePlane))
    		{
    			GameScr.gI().auto = 0;
    			return;
    		}
    		long num = mSystem.currentTimeMillis();
    		if (me)
    		{
    			if (num < myskill.lastTimeUseThisSkill)
    			{
    				myskill.lastTimeUseThisSkill = num - myskill.coolDown;
    			}
    			if (num - myskill.lastTimeUseThisSkill < myskill.coolDown)
    			{
    				myskill.paintCanNotUseSkill = true;
    				return;
    			}
    			myskill.paintCanNotUseSkill = false;
    		}
    		if (myskill.template.id == 10)
    		{
    			useChargeSkill(false);
    		}
    		if (myskill.template.id == 11)
    		{
    			useChargeSkill(true);
    		}
    	}
    
    	public void stopUseChargeSkill()
    	{
    		isFlyAndCharge = false;
    		isStandAndCharge = false;
    		isUseSkillAfterCharge = false;
    		isCreateDark = false;
    		if (me && statusMe != 14 && statusMe != 5)
    		{
    			isLockMove = false;
    		}
    		GameScr.gI().auto = 0;
    	}
    
    	public void useChargeSkill(bool isGround)
    	{
    		if (isCreateDark)
    		{
    			return;
    		}
    		GameScr.gI().auto = 0;
    		if (isGround)
    		{
    			if (isStandAndCharge)
    			{
    				return;
    			}
    			chargeCount = 0;
    			seconds = 50000;
    			posDisY = 0;
    			last = mSystem.currentTimeMillis();
    			if (me)
    			{
    				isLockMove = true;
    				if (cgender == 1)
    				{
    					Service.gI().skill_not_focus(4);
    				}
    				if (TileMap.mapID == 170 && cgender != 1)
    				{
    					Service.gI().skill_not_focus(4);
    				}
    			}
    			if (cgender == 1)
    			{
    				SoundMn.gI().gongName();
    			}
    			if (TileMap.mapID == 170 && cgender != 1)
    			{
    				SoundMn.gI().gongName();
    			}
    			isStandAndCharge = true;
    		}
    		else if (!isFlyAndCharge)
    		{
    			if (me)
    			{
    				GameScr.gI().auto = 0;
    				isLockMove = true;
    				Service.gI().skill_not_focus(4);
    			}
    			isUseSkillAfterCharge = false;
    			chargeCount = 0;
    			isFlyAndCharge = true;
    			posDisY = 0;
    			seconds = 50000;
    			isFlying = TileMap.tileTypeAt(cx, cy, 2);
    		}
    	}
    
    	public void setAutoSkillPaint(SkillPaint skillPaint, int sType)
    	{
    		if (skillPaint == null)
    		{
    			if (GameScr.sks != null && GameScr.sks.Length > 0 && GameScr.sks[0] != null)
    			{
    				skillPaint = GameScr.sks[0];
    			}
    			else
    			{
    				return;
    			}
    		}
    		this.skillPaint = skillPaint;
    		if (skillPaint.id >= 0 && skillPaint.id <= 6)
    		{
    			int num = Res.random(0, skillPaint.id + 4) - 1;
    			if (num < 0)
    			{
    				num = 0;
    			}
    			if (num > 6)
    			{
    				num = 6;
    			}
    			skillPaintRandomPaint = GameScr.sks[num];
    		}
    		else if (skillPaint.id >= 14 && skillPaint.id <= 20)
    		{
    			int num2 = Res.random(0, skillPaint.id - 14 + 4) - 1;
    			if (num2 < 0)
    			{
    				num2 = 0;
    			}
    			if (num2 > 6)
    			{
    				num2 = 6;
    			}
    			skillPaintRandomPaint = GameScr.sks[num2 + 14];
    		}
    		else if (skillPaint.id >= 28 && skillPaint.id <= 34)
    		{
    			int num3 = Res.random(0, ((isMonkey != 1) ? skillPaint.id : 105) - ((isMonkey != 1) ? 28 : 105) + 4) - 1;
    			if (num3 < 0)
    			{
    				num3 = 0;
    			}
    			if (num3 > 6)
    			{
    				num3 = 6;
    			}
    			if (isMonkey == 1)
    			{
    				num3 = 0;
    			}
    			skillPaintRandomPaint = GameScr.sks[num3 + ((isMonkey != 1) ? 28 : 105)];
    		}
    		else if (skillPaint.id >= 63 && skillPaint.id <= 69)
    		{
    			int num4 = Res.random(0, skillPaint.id - 63 + 4) - 1;
    			if (num4 < 0)
    			{
    				num4 = 0;
    			}
    			if (num4 > 6)
    			{
    				num4 = 6;
    			}
    			skillPaintRandomPaint = GameScr.sks[num4 + 63];
    		}
    		else if (skillPaint.id >= 107 && skillPaint.id <= 109)
    		{
    			int num5 = Res.random(0, skillPaint.id - 107 + 4) - 1;
    			if (num5 < 0)
    			{
    				num5 = 0;
    			}
    			if (num5 > 6)
    			{
    				num5 = 6;
    			}
    			skillPaintRandomPaint = GameScr.sks[num5 + 107];
    		}
    		else
    		{
    			skillPaintRandomPaint = skillPaint;
    		}
    		this.sType = sType;
    		indexSkill = 0;
    		i0 = (i1 = (i2 = (dx0 = (dx1 = (dx2 = (dy0 = (dy1 = (dy2 = 0))))))));
    		eff0 = null;
    		eff1 = null;
    		eff2 = null;
    		cvy = 0;
    	}
    
    	public SkillInfoPaint[] skillInfoPaint()
    	{
    		if (skillPaint == null)
    		{
    			return null;
    		}
    		if (skillPaintRandomPaint == null)
    		{
    			return null;
    		}
    		if (sType == 0)
    		{
    			return skillPaintRandomPaint.skillStand;
    		}
    		return skillPaintRandomPaint.skillfly;
    	}
    
    	public void setAttack()
    	{
    		if (me)
    		{
    			SkillPaint skillPaint = skillPaintRandomPaint;
    			if (dart != null)
    			{
    				skillPaint = dart.skillPaint;
    			}
    			if (skillPaint == null)
    			{
    				return;
    			}
    			MyVector myVector = new MyVector();
    			MyVector myVector2 = new MyVector();
    			if (charFocus != null)
    			{
    				myVector2.addElement(charFocus);
    			}
    			else if (mobFocus != null)
    			{
    				myVector.addElement(mobFocus);
    			}
    			effPaints = new EffectPaint[myVector.size() + myVector2.size()];
    			for (int i = 0; i < myVector.size(); i++)
    			{
    				effPaints[i] = new EffectPaint();
    				effPaints[i].effCharPaint = GameScr.getEffect(skillPaint.effectHappenOnMob);
    				if (!isSelectingSkillUseAlone())
    				{
    					effPaints[i].eMob = (Mob)myVector.elementAt(i);
    				}
    			}
    			for (int j = 0; j < myVector2.size(); j++)
    			{
    				effPaints[j + myVector.size()] = new EffectPaint();
    				effPaints[j + myVector.size()].effCharPaint = GameScr.getEffect(skillPaint.effectHappenOnMob);
    				effPaints[j + myVector.size()].eChar = (Char)myVector2.elementAt(j);
    			}
    			int type = 0;
    			if (mobFocus != null)
    			{
    				type = 1;
    			}
    			else if (charFocus != null)
    			{
    				type = 2;
    			}
    			if (myVector.size() == 0 && myVector2.size() == 0)
    			{
    				stopUseChargeSkill();
    			}
    			if (me && !isSelectingSkillUseAlone() && !hasSendAttack)
    			{
    				Service.gI().sendPlayerAttack(myVector, myVector2, type);
    				hasSendAttack = true;
    			}
    			return;
    		}
    		SkillPaint skillPaint2 = skillPaintRandomPaint;
    		if (dart != null)
    		{
    			skillPaint2 = dart.skillPaint;
    		}
    		if (skillPaint2 == null)
    		{
    			skillPaint2 = skillPaint;
    		}
    		if (skillPaint2 == null && GameScr.sks != null && GameScr.sks.Length > 0)
    		{
    			skillPaint2 = GameScr.sks[0];
    		}
    		if (skillPaint2 == null)
    		{
    			return;
    		}
    		if (attMobs != null)
    		{
    			effPaints = new EffectPaint[attMobs.Length];
    			for (int k = 0; k < attMobs.Length; k++)
    			{
    				effPaints[k] = new EffectPaint();
    				effPaints[k].effCharPaint = GameScr.getEffect(skillPaint2.effectHappenOnMob);
    				effPaints[k].eMob = attMobs[k];
    			}
    			attMobs = null;
    		}
    		else if (attChars != null)
    		{
    			effPaints = new EffectPaint[attChars.Length];
    			for (int l = 0; l < attChars.Length; l++)
    			{
    				effPaints[l] = new EffectPaint();
    				effPaints[l].effCharPaint = GameScr.getEffect(skillPaint2.effectHappenOnMob);
    				effPaints[l].eChar = attChars[l];
    			}
    			attChars = null;
    		}
    	}
    
    	public bool isOutX()
    	{
    		if (cx < GameScr.cmx - 60)
    		{
    			return true;
    		}
    		if (cx > GameScr.cmx + GameScr.gW + 60)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isPaint()
    	{
    		if (cy < GameScr.cmy - 80)
    		{
    			return false;
    		}
    		if (cy > GameScr.cmy + GameScr.gH + 50)
    		{
    			return false;
    		}
    		if (isOutX())
    		{
    			return false;
    		}
    		if (isSetPos)
    		{
    			return false;
    		}
    		if (isFusion)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public void createShadow(int x, int y, int life)
    	{
    		shadowX = x;
    		shadowY = y;
    		shadowLife = life;
    	}
    
    	public void setMabuHold(bool m)
    	{
    		isMabuHold = m;
    	}
    
    
    	private void paint_map_line(mGraphics g)
    	{
    		if (isPaintNewSkill || x_hint == 0 || y_hint == 0 || statusMe == 14 || (taskMaint != null && taskMaint.taskId > 2))
    		{
    			return;
    		}
    		int arg = 0;
    		int x = cx - 30;
    		int y = cy - 15;
    		int num = -30;
    		int num2 = 5;
    		if (Res.abs(cy - y_hint) > 150)
    		{
    			if (cy > y_hint)
    			{
    				arg = 7;
    				x = cx;
    				y = cy - 15 - 60;
    			}
    			else
    			{
    				arg = 5;
    				x = cx;
    				y = cy - 15 + 60;
    			}
    		}
    		else if (cx > x_hint)
    		{
    			arg = 2;
    		}
    		else if (cx <= x_hint)
    		{
    			x = cx + 30;
    		}
    		if (GameCanvas.gameTick % 10 >= 5)
    		{
    			if (Res.abs(cx - x_hint) > 100)
    			{
    				g.drawRegion(GameScr.arrow, 0, 0, 13, 16, arg, x, y, StaticObj.VCENTER_HCENTER);
    			}
    			else if (Res.abs(cx - x_hint) < 50)
    			{
    				g.drawImage(Panel.imgBantay, x_hint + num, y_hint - 60 + num2, 0);
    			}
    		}
    	}
    
    	private void paintEff_Pet(mGraphics g)
    	{
    		for (int i = 0; i < vEffChar.size(); i++)
    		{
    			Effect effect = (Effect)vEffChar.elementAt(i);
    			if (effect.effId >= 201)
    			{
    				effect.paint(g);
    			}
    		}
    	}
    
    	private void paintSuperEffBehind(mGraphics g)
    	{
    		if ((me && !isPaintAura2) || idAuraEff > -1 || (statusMe != 1 && statusMe != 6) || mSystem.currentTimeMillis() - timeBlue <= 0 || isCopy || clevel < 16)
    		{
    			return;
    		}
    		int num = 7598;
    		int num2 = 4;
    		if (clevel >= 19)
    		{
    			num = 7676;
    		}
    		if (clevel >= 22)
    		{
    			num = 7677;
    		}
    		if (clevel >= 25)
    		{
    			num = 7678;
    		}
    		if (num != -1)
    		{
    			Small small = SmallImage.imgNew[num];
    			if (small == null)
    			{
    				SmallImage.createImage(num);
    				return;
    			}
    			int y = GameCanvas.gameTick / 4 % num2 * (mGraphics.getImageHeight(small.img) / num2);
    			g.drawRegion(small.img, 0, y, mGraphics.getImageWidth(small.img), mGraphics.getImageHeight(small.img) / num2, 0, cx, cy + 2, mGraphics.BOTTOM | mGraphics.HCENTER);
    		}
    	}
    
    	private void paintSuperEffFront(mGraphics g)
    	{
    		if (!isPaintAura2)
    		{
    			return;
    		}
    		if (statusMe == 1 || statusMe == 6)
    		{
    			if (mSystem.currentTimeMillis() - timeBlue <= 0)
    			{
    				return;
    			}
    			if (isCopy)
    			{
    				if (GameCanvas.gameTick % 2 == 0)
    				{
    					tBlue++;
    				}
    				if (tBlue > 6)
    				{
    					tBlue = 0;
    				}
    				g.drawImage(GameCanvas.imgViolet[tBlue], cx, cy + 9, mGraphics.BOTTOM | mGraphics.HCENTER);
    				return;
    			}
    			if (clevel >= 14 && !GameCanvas.lowGraphic)
    			{
    				bool flag = false;
    				if (mSystem.currentTimeMillis() - timeBlue > -1000 && IsAddDust1)
    				{
    					flag = true;
    					IsAddDust1 = false;
    				}
    				if (mSystem.currentTimeMillis() - timeBlue > -500 && IsAddDust2)
    				{
    					flag = true;
    					IsAddDust2 = false;
    				}
    				if (flag)
    				{
    					GameCanvas.gI().startDust(-1, cx - -8, cy);
    					GameCanvas.gI().startDust(1, cx - 8, cy);
    					addDustEff(1);
    				}
    			}
    			if (clevel == 14)
    			{
    				if (GameCanvas.gameTick % 2 == 0)
    				{
    					tBlue++;
    				}
    				if (tBlue > 6)
    				{
    					tBlue = 0;
    				}
    				g.drawImage(GameCanvas.imgBlue[tBlue], cx, cy + 9, mGraphics.BOTTOM | mGraphics.HCENTER);
    			}
    			else if (clevel == 15)
    			{
    				if (GameCanvas.gameTick % 2 == 0)
    				{
    					tBlue++;
    				}
    				if (tBlue > 6)
    				{
    					tBlue = 0;
    				}
    				g.drawImage(GameCanvas.imgViolet[tBlue], cx, cy + 9, mGraphics.BOTTOM | mGraphics.HCENTER);
    			}
    			else
    			{
    				if (clevel < 16)
    				{
    					return;
    				}
    				int num = -1;
    				int num2 = 4;
    				if (clevel >= 16 && clevel < 22)
    				{
    					num = 7599;
    					num2 = 4;
    				}
    				if (num != -1)
    				{
    					Small small = SmallImage.imgNew[num];
    					if (small == null)
    					{
    						SmallImage.createImage(num);
    						return;
    					}
    					int y = GameCanvas.gameTick / 4 % num2 * (mGraphics.getImageHeight(small.img) / num2);
    					g.drawRegion(small.img, 0, y, mGraphics.getImageWidth(small.img), mGraphics.getImageHeight(small.img) / num2, 0, cx, cy + 2, mGraphics.BOTTOM | mGraphics.HCENTER);
    				}
    			}
    		}
    		else
    		{
    			timeBlue = mSystem.currentTimeMillis() + 1500;
    			IsAddDust1 = true;
    			IsAddDust2 = true;
    		}
    	}
    
    	private void paintEffect(mGraphics g)
    	{
    		if (effPaints != null)
    		{
    			for (int i = 0; i < effPaints.Length; i++)
    			{
    				if (effPaints[i] == null)
    				{
    					continue;
    				}
    				if (effPaints[i].eMob != null)
    				{
    					int y = effPaints[i].eMob.y;
    					if (effPaints[i].eMob is BigBoss)
    					{
    						y = effPaints[i].eMob.y - 60;
    					}
    					if (effPaints[i].eMob is BigBoss2)
    					{
    						y = effPaints[i].eMob.y - 50;
    					}
    					if (effPaints[i].eMob is BachTuoc)
    					{
    						y = effPaints[i].eMob.y - 40;
    					}
    					SmallImage.drawSmallImage(g, effPaints[i].getImgId(), effPaints[i].eMob.x, y, 0, mGraphics.BOTTOM | mGraphics.HCENTER);
    				}
    				else if (effPaints[i].eChar != null)
    				{
    					SmallImage.drawSmallImage(g, effPaints[i].getImgId(), effPaints[i].eChar.cx, effPaints[i].eChar.cy, 0, mGraphics.BOTTOM | mGraphics.HCENTER);
    				}
    			}
    		}
    		if (indexEff >= 0 && eff != null)
    		{
    			SmallImage.drawSmallImage(g, eff.arrEfInfo[indexEff].idImg, cx + eff.arrEfInfo[indexEff].dx, cy + eff.arrEfInfo[indexEff].dy, 0, mGraphics.VCENTER | mGraphics.HCENTER);
    		}
    		if (indexEffTask >= 0 && effTask != null)
    		{
    			SmallImage.drawSmallImage(g, effTask.arrEfInfo[indexEffTask].idImg, cx + effTask.arrEfInfo[indexEffTask].dx, cy + effTask.arrEfInfo[indexEffTask].dy, 0, mGraphics.VCENTER | mGraphics.HCENTER);
    		}
    	}
    
    	private void paintArrowAttack(mGraphics g)
    	{
    	}
    
    	public void paintHp(mGraphics g, int x, int y)
    	{
    		if (cHPFull <= 0)
    		{
    			return;
    		}
    		int num = (int)(cHP * 100 / cHPFull / 10 - 1);
    		if (num < 0)
    		{
    			num = 0;
    		}
    		if (num > 9)
    		{
    			num = 9;
    		}
    		if (!me)
    		{
    			g.drawRegion(Mob.imgHP, 0, 6 * (9 - num), 9, 6, 0, x, y - mFont.tahoma_7.getHeight() - 6, 3);
    		}
    		if (cTypePk == 0 && (myCharz().cFlag == 0 || cFlag == 0 || (cFlag != 8 && myCharz().cFlag != 8 && cFlag == myCharz().cFlag)))
    		{
    			return;
    		}
    		if (cHPFull <= 0)
    		{
    			cHPFull = 1;
    		}
    		len = (int)((long)cHP * 100L / cHPFull * w_hp_bar) / 100;
    		if (len > w_hp_bar)
    		{
    			len = w_hp_bar;
    		}
    		else if (len < 0)
    		{
    			len = 0;
    		}
    		num = (int)((long)cHP * 100L / cHPFull);
    		if (num > 100)
    		{
    			num = 100;
    		}
    		else if (num < 0)
    		{
    			num = 0;
    		}
    		if (num < 30)
    		{
    			imgHPtem = GameScr.imgHP_tm_do;
    		}
    		else if (num < 60)
    		{
    			imgHPtem = GameScr.imgHP_tm_vang;
    		}
    		else
    		{
    			imgHPtem = GameScr.imgHP_tm_xanh;
    		}
    		int imageWidth = mGraphics.getImageWidth(GameScr.imgHP_tm_xam);
    		int imageHeight = mGraphics.getImageHeight(GameScr.imgHP_tm_xam);
    		int w = imageWidth * num / 100;
    		if (w > imageWidth)
    		{
    			w = imageWidth;
    		}
    		else if (w < 0)
    		{
    			w = 0;
    		}
    		g.drawImage(GameScr.imgHP_tm_xam, x - (imageWidth >> 1), y - 1, mGraphics.TOP | mGraphics.LEFT);
    		if (len < 5)
    		{
    			if (GameCanvas.gameTick % 6 < 3)
    			{
    				g.drawRegion(imgHPtem, 0, 0, w, imageHeight, 0, x - (imageWidth >> 1), y - 1, mGraphics.TOP | mGraphics.LEFT);
    			}
    		}
    		else
    		{
    			g.drawRegion(imgHPtem, 0, 0, w, imageHeight, 0, x - (imageWidth >> 1), y - 1, mGraphics.TOP | mGraphics.LEFT);
    		}
    	}
    
    	public int getClassColor()
    	{
    		int result = 9145227;
    		if (nClass.classId == 1 || nClass.classId == 2)
    		{
    			result = 16711680;
    		}
    		else if (nClass.classId == 3 || nClass.classId == 4)
    		{
    			result = 33023;
    		}
    		else if (nClass.classId == 5 || nClass.classId == 6)
    		{
    			result = 7443811;
    		}
    		return result;
    	}
    
    	public void paintNameInSameParty(mGraphics g)
    	{
    		if (cTypePk != 3 && cTypePk != 5 && isPaint())
    		{
    			if (myCharz().charFocus == null || !myCharz().charFocus.Equals(this))
    			{
    				mFont.tahoma_7_yellow.drawString(g, cName, cx, cy - ch - mFont.tahoma_7_green.getHeight() - 5, mFont.CENTER, mFont.tahoma_7_grey);
    			}
    			else if (myCharz().charFocus != null && myCharz().charFocus.Equals(this))
    			{
    				mFont.tahoma_7_yellow.drawString(g, cName, cx, cy - ch - mFont.tahoma_7_green.getHeight() - 10, mFont.CENTER, mFont.tahoma_7_grey);
    			}
    		}
    	}
    
	private void paintCharName_HP_MP_Overhead(mGraphics g)
	{
		try
		{
			if (GameScr.parts == null)
			{
				return;
			}
			int headIndex = getFHead(head);
			if (headIndex < 0 || headIndex >= GameScr.parts.Length)
			{
				return;
			}
			Part part = GameScr.parts[headIndex];
			if (part == null || part.pi == null || CharInfo[cf][0][0] >= part.pi.Length)
			{
				return;
			}
			int num = CharInfo[cf][0][2] - part.pi[CharInfo[cf][0][0]].dy + 5;
			if ((isInvisiblez && !me) || (!me && TileMap.mapID == 113 && cy >= 360))
			{
				return;
			}
			if (me)
			{
				num += 5;
				paintHp(g, cx, cy - num + 3);
				if (fraDanhHieu != null)
				{
					int x = cx - fraDanhHieu.frameWidth / 2;
					int y = cy - num + 3 - mFont.tahoma_7.getHeight() - (fraDanhHieu.frameHeight + 5);
					if (GameCanvas.gameTick % 5 == 0)
					{
						danhHieuFramme++;
					}
					if (danhHieuFramme >= fraDanhHieu.nFrame)
					{
						danhHieuFramme = 0;
					}
					fraDanhHieu.drawFrame(danhHieuFramme, x, y, 0, mGraphics.TOP | mGraphics.LEFT, g);
				}
				return;
			}
			bool flag = myChar.clan != null && clanID == myChar.clan.ID;
			bool flag2 = cTypePk == 3 || cTypePk == 5;
			bool flag3 = cTypePk == 4;
			if (!isPetFollower && (charID <= -310720000 || cName == "$" || (isPet && string.IsNullOrEmpty(cName))))
			{
				isPetFollower = true;
				isPet = true;
				cName = string.Empty;
				paintName = false;
			}
			if (isPetFollower || string.IsNullOrEmpty(cName))
			{
				return;
			}
			if (!isPet && cName != null && cName.Length > 1 && cName.StartsWith("$"))
			{
				cName = cName.Substring(1);
				isPet = true;
			}
			if (!isMiniPet && cName != null && cName.Length > 1 && cName.StartsWith("#"))
			{
				cName = cName.Substring(1);
				isMiniPet = true;
			}
			if (myCharz().charFocus != null && myCharz().charFocus.Equals(this))
			{
				num += 5;
				paintHp(g, cx, cy - num + 3);
				if (fraDanhHieu != null)
				{
					int x2 = cx - fraDanhHieu.frameWidth / 2;
					int y2 = cy - num + 3 - mFont.tahoma_7.getHeight() - (fraDanhHieu.frameHeight + 5);
					if (GameCanvas.gameTick % 5 == 0)
					{
						danhHieuFramme++;
					}
					if (danhHieuFramme >= fraDanhHieu.nFrame)
					{
						danhHieuFramme = 0;
					}
					fraDanhHieu.drawFrame(danhHieuFramme, x2, y2, 0, mGraphics.TOP | mGraphics.LEFT, g);
				}
			}
			num += mFont.tahoma_7_white.getHeight();
			mFont mFont2 = mFont.tahoma_7_whiteSmall;
			if (isPet || isMiniPet)
			{
				mFont2 = mFont.tahoma_7_blue1Small;
			}
			else if (flag2)
			{
				mFont2 = mFont.nameFontRed;
			}
			else if (flag3)
			{
				mFont2 = mFont.nameFontYellow;
			}
			else if (flag)
			{
				mFont2 = mFont.nameFontGreen;
			}
			if (TileMap.mapID == 170)
			{
				if (flagImage == 2325)
				{
					mFont2 = mFont.tahoma_7_blue;
				}
				else if (flagImage == 2323)
				{
					mFont2 = mFont.tahoma_7_red;
				}
			}
			if ((paintName || flag2 || flag3) && !flag)
			{
				if (mSystem.clientType == 1)
				{
					mFont2.drawString(g, cName, cx, cy - num, mFont.CENTER, mFont.tahoma_7_greySmall);
				}
				else if (charID == -83)
				{
					mFont2.drawString(g, cName, cx, cy - num, mFont.CENTER, mFont.tahoma_7_greySmall);
				}
				else
				{
					mFont2.drawString(g, cName, cx, cy - num, mFont.CENTER);
				}
				num += mFont.tahoma_7.getHeight();
			}
			if (flag)
			{
				if (myCharz().charFocus != null && myCharz().charFocus.Equals(this))
				{
					mFont2.drawString(g, cName, cx, cy - num, mFont.CENTER, mFont.tahoma_7_greySmall);
				}
				else if (charFocus == null)
				{
					mFont2.drawString(g, cName, cx - 10, cy - num + 3, mFont.LEFT, mFont.tahoma_7_grey);
					paintHp(g, cx - 16, cy - num + 10);
				}
			}
		}
		catch (Exception)
		{
		}
	}
    
    	public void paintShadow(mGraphics g)
    	{
    		if (isMabuHold || head == 377 || leg == 471 || isTeleport || isFlyUp || !isPaint())
    		{
    			return;
    		}
    		int num = TileMap.size;
    		if ((TileMap.mapID < 114 || TileMap.mapID > 120) && TileMap.mapID != 127 && TileMap.mapID != 128 && !TileMap.tileTypeAt(xSd + num / 2, ySd + 1, 4))
    		{
    			if (TileMap.tileTypeAt((xSd - num / 2) / num, (ySd + 1) / num) == 0)
    			{
    				g.setClip(xSd / num * num, (ySd - 30) / num * num, 100, 100);
    			}
    			else if (TileMap.tileTypeAt((xSd + num / 2) / num, (ySd + 1) / num) == 0)
    			{
    				g.setClip(xSd / num * num, (ySd - 30) / num * num, num, 100);
    			}
    			else if (TileMap.tileTypeAt(xSd - num / 2, ySd + 1, 8))
    			{
    				g.setClip(xSd / 24 * num, (ySd - 30) / num * num, num, 100);
    			}
    		}
    		g.drawImage(TileMap.bong, xSd, ySd, 3);
    		g.setClip(GameScr.cmx, GameScr.cmy - GameCanvas.transY, GameScr.gW, GameScr.gH + 2 * GameCanvas.transY);
    	}
    
    	public void updateShadown()
    	{
    		int num = 0;
    		xSd = cx;
    		if (TileMap.tileTypeAt(cx, cy, 2))
    		{
    			ySd = cy;
    			return;
    		}
    		ySd = cy;
    		while (num < 30)
    		{
    			num++;
    			ySd += 24;
    			if (TileMap.tileTypeAt(xSd, ySd, 2))
    			{
    				if (ySd % 24 != 0)
    				{
    					ySd -= ySd % 24;
    				}
    				break;
    			}
    		}
    	}
    
    	private void paintCharWithoutSkill(mGraphics g)
    	{
    		try
    		{
    			if (isMafuba)
    			{
    				paintCharBody(g, xMFB, yMFB, cdir, cf, false);
    				return;
    			}
    			if (isInvisiblez)
    			{
    				if (me)
    				{
    					if (GameCanvas.gameTick % 50 == 48 || GameCanvas.gameTick % 50 == 90)
    					{
    						SmallImage.drawSmallImage(g, 1196, cx, cy - 18, 0, mGraphics.VCENTER | mGraphics.HCENTER);
    					}
    					else
    					{
    						SmallImage.drawSmallImage(g, 1195, cx, cy - 18, 0, mGraphics.VCENTER | mGraphics.HCENTER);
    					}
    				}
    			}
    			else
    			{
    				paintCharBody(g, cx, cy + fy, cdir, cf, true);
    			}
    			if (isLockAttack)
    			{
    				SmallImage.drawSmallImage(g, 290, cx, cy, 0, mGraphics.BOTTOM | mGraphics.HCENTER);
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi paint char without skill: " + ex.ToString());
    		}
    	}
    
    	public void paintBag(mGraphics g, short[] id, int x, int y, int dir, bool isPaintChar)
    	{
    		int num = 0;
    		int num2 = 0;
    		if (statusMe == 6)
    		{
    			num = 8;
    			num2 = 17;
    		}
    		if (statusMe == 1)
    		{
    			if (cp1 % 15 < 5)
    			{
    				num = 8;
    				num2 = 17;
    			}
    			else
    			{
    				num = 8;
    				num2 = 18;
    			}
    		}
    		if (statusMe == 2)
    		{
    			if (cf <= 3)
    			{
    				num = 7;
    				num2 = 17;
    			}
    			else
    			{
    				num = 7;
    				num2 = 18;
    			}
    		}
    		if (statusMe == 3 || statusMe == 9)
    		{
    			num = 5;
    			num2 = 20;
    		}
    		if (statusMe == 4)
    		{
    			if (cf == 8)
    			{
    				num = 5;
    				num2 = 16;
    			}
    			else
    			{
    				num = 5;
    				num2 = 20;
    			}
    		}
    		if (statusMe == 10)
    		{
    			if (cf == 8)
    			{
    				num = 0;
    				num2 = 23;
    			}
    			else
    			{
    				num = 5;
    				num2 = 22;
    			}
    		}
    		if (isInjure > 0)
    		{
    			num = 5;
    			num2 = 18;
    		}
    		if (skillPaint != null && skillInfoPaint() != null && indexSkill < skillInfoPaint().Length)
    		{
    			num = -1;
    			num2 = 17;
    		}
    		fBag++;
    		if (fBag > 10000)
    		{
    			fBag = 0;
    		}
    		sbyte b = (sbyte)(fBag / 4 % id.Length);
    		if (!isPaintChar)
    		{
    			if (id.Length == 2)
    			{
    				b = 1;
    			}
    			if (id.Length == 3)
    			{
    				if (id[2] >= 0)
    				{
    					b = 2;
    					if (GameCanvas.gameTick % 10 > 5)
    					{
    						b = 1;
    					}
    				}
    				else
    				{
    					b = 1;
    				}
    			}
    		}
    		else if (id.Length > 1 && (b == 0 || b == 1) && statusMe != 1 && statusMe != 6)
    		{
    			fBag = 0;
    			b = 0;
    			if (GameCanvas.gameTick % 10 > 5)
    			{
    				b = 1;
    			}
    		}
    		SmallImage.drawSmallImage(g, id[b], x + ((dir != 1) ? num : (-num)), y - num2, (dir != 1) ? 2 : 0, StaticObj.VCENTER_HCENTER);
    	}
    }
}

namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    
    public partial class Char : IMapObject
    {
    
    	private void updateEffect()
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
    					if (!effPaints[i].isFly)
    					{
    						effPaints[i].eMob.setInjure();
    						effPaints[i].eMob.injureBy = this;
    						if (me)
    						{
    							effPaints[i].eMob.hpInjure = myCharz().cDamFull / 2 - myCharz().cDamFull * NinjaUtil.randomNumber(11) / 100;
    						}
    						int num = effPaints[i].eMob.h >> 1;
    						if (effPaints[i].eMob.isBigBoss())
    						{
    							num = effPaints[i].eMob.getY() + 20;
    						}
    						GameScr.startSplash(effPaints[i].eMob.x, effPaints[i].eMob.y - num, cdir);
    						effPaints[i].isFly = true;
    					}
    				}
    				else if (effPaints[i].eChar != null && !effPaints[i].isFly)
    				{
    					if (effPaints[i].eChar.charID >= 0)
    					{
    						effPaints[i].eChar.doInjure();
    					}
    					GameScr.startSplash(effPaints[i].eChar.cx, effPaints[i].eChar.cy - (effPaints[i].eChar.ch >> 1), cdir);
    					effPaints[i].isFly = true;
    				}
    				effPaints[i].index++;
    				if (effPaints[i].index >= effPaints[i].effCharPaint.arrEfInfo.Length)
    				{
    					effPaints[i] = null;
    				}
    			}
    		}
    		if (indexEff >= 0 && eff != null && GameCanvas.gameTick % 2 == 0)
    		{
    			indexEff++;
    			if (indexEff >= eff.arrEfInfo.Length)
    			{
    				indexEff = -1;
    				eff = null;
    			}
    		}
    		if (indexEffTask >= 0 && effTask != null && GameCanvas.gameTick % 2 == 0)
    		{
    			indexEffTask++;
    			if (indexEffTask >= effTask.arrEfInfo.Length)
    			{
    				indexEffTask = -1;
    				effTask = null;
    			}
    		}
    	}
    
    	private void checkPerformEndMovePointAction()
    	{
    		if (endMovePointCommand != null)
    		{
    			Command command = endMovePointCommand;
    			endMovePointCommand = null;
    			command.performAction();
    		}
    	}
    
    	private void checkHideCharName()
    	{
    		if (GameCanvas.gameTick % 20 != 0 || charID < 0)
    		{
    			return;
    		}
    		paintName = true;
    		for (int i = 0; i < GameScr.vCharInMap.size(); i++)
    		{
    			Char @char = null;
    			try
    			{
    				@char = (Char)GameScr.vCharInMap.elementAt(i);
    			}
    			catch (Exception)
    			{
    			}
    			if (@char != null && !@char.Equals(this) && ((@char.cy == cy && Res.abs(@char.cx - cx) < 35) || (cy - @char.cy < 32 && cy - @char.cy > 0 && Res.abs(@char.cx - cx) < 24)))
    			{
    				paintName = false;
    			}
    		}
    		for (int j = 0; j < GameScr.vNpc.size(); j++)
    		{
    			Npc npc = null;
    			try
    			{
    				npc = (Npc)GameScr.vNpc.elementAt(j);
    			}
    			catch (Exception)
    			{
    			}
    			if (npc != null && npc.cy == cy && Res.abs(npc.cx - cx) < 24)
    			{
    				paintName = false;
    			}
    		}
    	}
    
    	private void updateMobMe()
    	{
    		if (tMobMeBorn != 0)
    		{
    			tMobMeBorn--;
    		}
    		if (tMobMeBorn == 0)
    		{
    			mobMe.xFirst = ((cdir != 1) ? (cx + 30) : (cx - 30));
    			mobMe.yFirst = cy - 60;
    			int num = mobMe.xFirst - mobMe.x;
    			int num2 = mobMe.yFirst - mobMe.y;
    			mobMe.x += num / 4;
    			mobMe.y += num2 / 4;
    			mobMe.dir = cdir;
    		}
    	}
    
    	private void updateSkillPaint()
    	{
    		if (statusMe == 14 || statusMe == 5)
    		{
    			return;
    		}
    		if (skillPaint != null && ((charFocus != null && isMeCanAttackOtherPlayer(charFocus) && charFocus.statusMe == 14) || (mobFocus != null && mobFocus.status == 0)))
    		{
    			if (!me)
    			{
    				if ((TileMap.tileTypeAtPixel(cx, cy) & 2) == 2)
    				{
    					statusMe = 1;
    				}
    				else
    				{
    					statusMe = 6;
    				}
    				cp3 = 0;
    			}
    			indexSkill = 0;
    			skillPaint = null;
    			skillPaintRandomPaint = null;
    			eff0 = (eff1 = (eff2 = null));
    			i0 = (i1 = (i2 = 0));
    			mobFocus = null;
    			charFocus = null;
    			effPaints = null;
    			currentMovePoint = null;
    			arr = null;
    			if ((TileMap.tileTypeAtPixel(cx, cy) & 2) != 2)
    			{
    				delayFall = 5;
    			}
    		}
    		if (skillPaint != null && arr == null && skillInfoPaint() != null && indexSkill >= skillInfoPaint().Length)
    		{
    			if (!me)
    			{
    				if ((TileMap.tileTypeAtPixel(cx, cy) & 2) == 2)
    				{
    					statusMe = 1;
    				}
    				else
    				{
    					statusMe = 6;
    				}
    				cp3 = 0;
    			}
    			indexSkill = 0;
    			skillPaint = null;
    			skillPaintRandomPaint = null;
    			eff0 = (eff1 = (eff2 = null));
    			i0 = (i1 = (i2 = 0));
    			arr = null;
    			if ((TileMap.tileTypeAtPixel(cx, cy) & 2) != 2)
    			{
    				delayFall = 5;
    			}
    		}
    		SkillInfoPaint[] array = skillInfoPaint();
    		if (array == null || indexSkill < 0 || indexSkill > array.Length - 1)
    		{
    			return;
    		}
    		if (array[indexSkill].effS0Id != 0)
    		{
    			eff0 = GameScr.getEffect(array[indexSkill].effS0Id);
    			i0 = (dx0 = (dy0 = 0));
    		}
    		if (array[indexSkill].effS1Id != 0)
    		{
    			eff1 = GameScr.getEffect(array[indexSkill].effS1Id);
    			i1 = (dx1 = (dy1 = 0));
    		}
    		if (array[indexSkill].effS2Id != 0)
    		{
    			eff2 = GameScr.getEffect(array[indexSkill].effS2Id);
    			i2 = (dx2 = (dy2 = 0));
    		}
    		SkillInfoPaint[] array2 = array;
    		int num = indexSkill;
    		if (array2 != null && array2[num] != null && num >= 0 && num <= array2.Length - 1 && array2[num].arrowId != 0)
    		{
    			int arrowId = array2[num].arrowId;
    			if (arrowId >= 100)
    			{
    				object obj;
    				if (mobFocus == null)
    				{
    					IMapObject mapObject = charFocus;
    					obj = mapObject;
    				}
    				else
    				{
    					obj = mobFocus;
    				}
    				IMapObject mapObject2 = (IMapObject)obj;
    				if (mapObject2 != null)
    				{
    					int num2 = 0;
    					int num3 = Res.abs(mapObject2.getX() - cx);
    					int num4 = Res.abs(mapObject2.getY() - cy);
    					if (num3 > 4 * num4)
    					{
    						num2 = 0;
    					}
    					else
    					{
    						num2 = ((mapObject2.getY() >= cy) ? 3 : (-3));
    						if (mapObject2 is BigBoss)
    						{
    							BigBoss bigBoss = (BigBoss)mapObject2;
    							if (bigBoss.haftBody)
    							{
    								num2 = -20;
    							}
    						}
    					}
    					dart = new PlayerDart(this, arrowId - 100, skillPaintRandomPaint, cx + (array2[num].adx - 10) * cdir, cy + array2[num].ady + num2);
    					if (myskill != null)
    					{
    						if (myskill.template.id == 1)
    						{
    							SoundMn.gI().traidatKame();
    						}
    						else if (myskill.template.id == 3)
    						{
    							SoundMn.gI().namekKame();
    						}
    						else if (myskill.template.id == 5)
    						{
    							SoundMn.gI().xaydaKame();
    						}
    						else if (myskill.template.id == 11)
    						{
    							SoundMn.gI().nameLazer();
    						}
    					}
    				}
    				else if (isFlyAndCharge || isUseSkillAfterCharge)
    				{
    					stopUseChargeSkill();
    				}
    			}
    			else
    			{
    				Res.outz("g");
    				arr = new Arrow(this, GameScr.arrs[arrowId - 1]);
    				arr.life = 10;
    				arr.ax = cx + array2[num].adx;
    				arr.ay = cy + array2[num].ady;
    			}
    		}
    		if ((mobFocus != null || (!me && charFocus != null) || (me && charFocus != null && (isMeCanAttackOtherPlayer(charFocus) || isSelectingSkillBuffToPlayer()) && arr == null && dart == null)) && indexSkill == array.Length - 1)
    		{
    			setAttack();
    			if (me && myskill.template.isAttackSkill())
    			{
    				saveLoadPreviousSkill();
    			}
    		}
    		if (me)
    		{
    			return;
    		}
    		IMapObject mapObject3 = null;
    		if (mobFocus != null)
    		{
    			mapObject3 = mobFocus;
    		}
    		else if (charFocus != null)
    		{
    			mapObject3 = charFocus;
    		}
    		if (mapObject3 == null)
    		{
    			return;
    		}
    		if (Res.abs(mapObject3.getX() - cx) < 10)
    		{
    			if (mapObject3.getX() > cx)
    			{
    				cx -= 10;
    			}
    			else
    			{
    				cx += 10;
    			}
    		}
    		if (mapObject3.getX() > cx)
    		{
    			cdir = 1;
    		}
    		else
    		{
    			cdir = -1;
    		}
    	}
    
    	public void saveLoadPreviousSkill()
    	{
    	}
    
    	public void setResetPoint(int x, int y)
    	{
    		InfoDlg.hide();
    		currentMovePoint = null;
    		int num = cx - x;
    		if (cy - y == 0)
    		{
    			cx = x;
    			ischangingMap = false;
    			isLockKey = false;
    			return;
    		}
    		statusMe = 16;
    		cp2 = x;
    		cp3 = y;
    		cp1 = 0;
    		myCharz().cxSend = x;
    		myCharz().cySend = y;
    	}
    
    	private void updateCharDeadFly()
    	{
    		isFreez = false;
    		if (isCharge)
    		{
    			isCharge = false;
    			SoundMn.gI().taitaoPause();
    			Service.gI().skill_not_focus(3);
    		}
    		cp1++;
    		cx += (cp2 - cx) / 4;
    		if (cp1 > 7)
    		{
    			cy += (cp3 - cy) / 4;
    		}
    		else
    		{
    			cy += cp1 - 10;
    		}
    		if (Res.abs(cp2 - cx) < 4 && Res.abs(cp3 - cy) < 10)
    		{
    			cx = cp2;
    			cy = cp3;
    			statusMe = 14;
    			if (me)
    			{
    				GameScr.gI().resetButton();
    				Service.gI().charMove();
    			}
    		}
    		cf = 23;
    	}
    
    	private void updateResetPoint()
    	{
    		InfoDlg.hide();
    		GameCanvas.clearAllPointerEvent();
    		currentMovePoint = null;
    		cp1++;
    		cx += (cp2 - cx) / 4;
    		if (cp1 > 7)
    		{
    			cy += (cp3 - cy) / 4;
    		}
    		else
    		{
    			cy += cp1 - 10;
    		}
    		if (Res.abs(cp2 - cx) < 4 && Res.abs(cp3 - cy) < 10)
    		{
    			cx = cp2;
    			cy = cp3;
    			statusMe = 1;
    			cp3 = 0;
    			ischangingMap = false;
    			Service.gI().charMove();
    		}
    		cf = 23;
    	}
    
    	public void updateSkillFall()
    	{
    	}
    
    	public void updateSkillStand()
    	{
    		ty = 0;
    		cp1++;
    		if (cdir == 1)
    		{
    			if ((TileMap.tileTypeAtPixel(cx + chw, cy - chh) & 4) == 4)
    			{
    				cvx = 0;
    			}
    		}
    		else if ((TileMap.tileTypeAtPixel(cx - chw, cy - chh) & 8) == 8)
    		{
    			cvx = 0;
    		}
    		if (cy > ch && TileMap.tileTypeAt(cx, cy - ch + 24, 8192))
    		{
    			if (!TileMap.tileTypeAt(cx, cy, 2))
    			{
    				statusMe = 4;
    				cp1 = 0;
    				cp2 = 0;
    				cvy = 1;
    			}
    			else
    			{
    				cy = TileMap.tileYofPixel(cy);
    			}
    		}
    		cx += cvx;
    		cy += cvy;
    		if (cy < 0)
    		{
    			cy = (cvy = 0);
    		}
    		if (cvy == 0)
    		{
    			if ((TileMap.tileTypeAtPixel(cx, cy) & 2) != 2)
    			{
    				statusMe = 4;
    				cvx = (cspeed >> 1) * cdir;
    				cp1 = (cp2 = 0);
    			}
    		}
    		else if (cvy < 0)
    		{
    			cvy++;
    			if (cvy == 0)
    			{
    				cvy = 1;
    			}
    		}
    		else
    		{
    			if (cvy < 20 && cp1 % 5 == 0)
    			{
    				cvy++;
    			}
    			if (cvy > 3)
    			{
    				cvy = 3;
    			}
    			if ((TileMap.tileTypeAtPixel(cx, cy + 3) & 2) == 2 && cy <= TileMap.tileXofPixel(cy + 3))
    			{
    				cvx = (cvy = 0);
    				cy = TileMap.tileXofPixel(cy + 3);
    			}
    		}
    		if (cvx > 0)
    		{
    			cvx--;
    		}
    		else if (cvx < 0)
    		{
    			cvx++;
    		}
    	}
    
    	public void updateCharAutoJump()
    	{
    		isFreez = false;
    		if (isCharge)
    		{
    			isCharge = false;
    			SoundMn.gI().taitaoPause();
    			Service.gI().skill_not_focus(3);
    		}
    		cx += cvx * cdir;
    		cy += cvyJump;
    		cvyJump++;
    		if (cp1 == 0)
    		{
    			cf = 7;
    		}
    		else
    		{
    			cf = 23;
    		}
    		if (cvyJump == -3)
    		{
    			cf = 8;
    		}
    		else if (cvyJump == -2)
    		{
    			cf = 9;
    		}
    		else if (cvyJump == -1)
    		{
    			cf = 10;
    		}
    		else if (cvyJump == 0)
    		{
    			cf = 11;
    		}
    		if (cvyJump == 0)
    		{
    			statusMe = 6;
    			cp3 = 0;
    			((MovePoint)vMovePoints.firstElement()).status = 4;
    			isJump = true;
    			cp1 = 0;
    			cvy = 1;
    		}
    	}
    
    	public int getVx(int size, int dx, int dy)
    	{
    		if (dy > 0 && !TileMap.tileTypeAt(cx, cy, 2))
    		{
    			if (dx - dy <= 10)
    			{
    				return 5;
    			}
    			if (dx - dy <= 30)
    			{
    				return 6;
    			}
    			if (dx - dy <= 50)
    			{
    				return 7;
    			}
    			if (dx - dy <= 70)
    			{
    				return 8;
    			}
    		}
    		if (dx <= 30)
    		{
    			return 4;
    		}
    		if (dx <= 160)
    		{
    			return 5;
    		}
    		if (dx <= 270)
    		{
    			return 6;
    		}
    		if (dx <= 320)
    		{
    			return 7;
    		}
    		return 8;
    	}
    
    	public void hide()
    	{
    		isHide = true;
    		EffecMn.addEff(new Effect(107, cx, cy + 25, 3, 15, 1));
    	}
    
    	public void show()
    	{
    		isHide = false;
    		EffecMn.addEff(new Effect(107, cx, cy + 25, 3, 10, 1));
    	}
    
    	public int getVy(int size, int dx, int dy)
    	{
    		if (dy <= 10)
    		{
    			return 5;
    		}
    		if (dy <= 20)
    		{
    			return 6;
    		}
    		if (dy <= 30)
    		{
    			return 7;
    		}
    		if (dy <= 40)
    		{
    			return 8;
    		}
    		if (dy <= 50)
    		{
    			return 9;
    		}
    		return 10;
    	}
    
    	public int returnAct(int xFirst, int yFirst, int xEnd, int yEnd)
    	{
    		int num = xEnd - xFirst;
    		int num2 = yEnd - yFirst;
    		if (num == 0 && num2 == 0)
    		{
    			return 1;
    		}
    		if (num2 == 0 && yFirst % 24 == 0 && TileMap.tileTypeAt(xFirst, yFirst, 2))
    		{
    			return 2;
    		}
    		if (num2 > 0 && (yFirst % 24 != 0 || !TileMap.tileTypeAt(xFirst, yFirst, 2)))
    		{
    			return 4;
    		}
    		cvy = -10;
    		cp1 = 0;
    		cdir = ((num > 0) ? 1 : (-1));
    		if (num <= 5)
    		{
    			cvx = 0;
    		}
    		else if (num <= 10)
    		{
    			cvx = 3;
    		}
    		else
    		{
    			cvx = 5;
    		}
    		return 9;
    	}
    
    	public void setAutoJump()
    	{
    		int num = ((MovePoint)vMovePoints.firstElement()).xEnd - cx;
    		cvyJump = -10;
    		cp1 = 0;
    		cdir = ((num > 0) ? 1 : (-1));
    		if (num <= 6)
    		{
    			cvx = 0;
    		}
    		else if (num <= 20)
    		{
    			cvx = 3;
    		}
    		else
    		{
    			cvx = 5;
    		}
    	}
    
    	public void updateCharStand()
    	{
    		isSoundJump = false;
    		isAttack = false;
    		isAttFly = false;
    		cvx = 0;
    		cvy = 0;
    		cp1++;
    		if (cp1 > 30)
    		{
    			cp1 = 0;
    		}
    		if (cp1 % 15 < 5)
    		{
    			cf = 0;
    		}
    		else
    		{
    			cf = 1;
    		}
    		updateCharInBridge();
    		if (!me)
    		{
    			cp3++;
    			if (cp3 > 50)
    			{
    				cp3 = 0;
    				currentMovePoint = null;
    			}
    		}
    		updateSuperEff();
    		if (!me || GameScr.vCharInMap.size() == 0 || TileMap.mapID != 50)
    		{
    			return;
    		}
    		Char @char = (Char)GameScr.vCharInMap.elementAt(0);
    		if (!@char.changePos)
    		{
    			if (@char.statusMe != 2)
    			{
    				@char.moveTo(cx - 45, cy, 0);
    			}
    			@char.lastUpdateTime = mSystem.currentTimeMillis();
    			if (Res.abs(cx - 45 - @char.cx) <= 10)
    			{
    				@char.changePos = true;
    			}
    		}
    		else
    		{
    			if (@char.statusMe != 2)
    			{
    				@char.moveTo(cx + 45, cy, 0);
    			}
    			@char.lastUpdateTime = mSystem.currentTimeMillis();
    			if (Res.abs(cx + 45 - @char.cx) <= 10)
    			{
    				@char.changePos = false;
    			}
    		}
    		if (GameCanvas.gameTick % 100 == 0)
    		{
    			@char.addInfo("Cắc cùm cum");
    		}
    	}
    
    	public void updateSuperEff()
    	{
    		if (isCopy || isFusion || isSetPos || isPet || isMiniPet || isMonkey == 1)
    		{
    			return;
    		}
    		if (me)
    		{
    			if (!isPaintAura2 && idAuraEff > -1)
    			{
    				return;
    			}
    		}
    		else if (idAuraEff > -1)
    		{
    			return;
    		}
    		ty++;
    		if (clevel >= 14)
    		{
    			return;
    		}
    		if (clevel >= 9 && !GameCanvas.lowGraphic && (ty == 40 || ty == 50))
    		{
    			GameCanvas.gI().startDust(-1, cx - -8, cy);
    			GameCanvas.gI().startDust(1, cx - 8, cy);
    			addDustEff(1);
    		}
    		if (ty <= 50 || clevel < 9)
    		{
    			return;
    		}
    		int num = 0;
    		if (cgender == 0)
    		{
    			if (clevel >= 13)
    			{
    				if (GameCanvas.gameTick % 25 == 0)
    				{
    					num = 114;
    					ServerEffect.addServerEffect(num, this, 1);
    				}
    				if (GameCanvas.gameTick % 4 == 0)
    				{
    					num = 132;
    					ServerEffect.addServerEffect(num, this, 1);
    				}
    			}
    			else if (GameCanvas.gameTick % 25 == 0)
    			{
    				num = 173 + (clevel - 10);
    				ServerEffect.addServerEffect(num, this, 1);
    			}
    		}
    		if (cgender == 1)
    		{
    			if (GameCanvas.gameTick % 4 == 0)
    			{
    				num = 132;
    				ServerEffect.addServerEffect(num, this, 1);
    			}
    			if (clevel >= 13 && GameCanvas.gameTick % 7 == 0)
    			{
    				num = 131;
    				ServerEffect.addServerEffect(num, this, 1);
    			}
    		}
    		if (cgender == 2)
    		{
    			if (GameCanvas.gameTick % 7 == 0)
    			{
    				num = 131;
    				ServerEffect.addServerEffect(num, this, 1);
    			}
    			if (clevel >= 13 && GameCanvas.gameTick % 25 == 0)
    			{
    				num = 114;
    				ServerEffect.addServerEffect(num, this, 1);
    			}
    		}
    	}
    
    	public float getSoundVolumn()
    	{
    		if (me)
    		{
    			return 0.1f;
    		}
    		int num = Res.abs(myChar.cx - cx);
    		if (num >= 0 && num <= 50)
    		{
    			return 0.1f;
    		}
    		return 0.05f;
    	}
        public string getGenderName()
        {
            if (cgender == 0)
                return "TĐ";
            if (cgender == 1)
                return "NM";
            return "XD";
        }
        public void updateCharRun()
    	{
    		int num = ((isMonkey != 1 || me) ? 1 : 1);
    		if (cx >= GameScr.cmx && cx <= GameScr.cmx + GameCanvas.w)
    		{
    			if (isMonkey == 0)
    			{
    				SoundMn.gI().charRun(getSoundVolumn());
    			}
    			else
    			{
    				SoundMn.gI().monkeyRun(getSoundVolumn());
    			}
    		}
    		ty = 0;
    		isFreez = false;
    		if (isCharge)
    		{
    			isCharge = false;
    			SoundMn.gI().taitaoPause();
    			Service.gI().skill_not_focus(3);
    		}
    		int num2 = 0;
    		if (!me && currentMovePoint != null)
    		{
    			num2 = abs(cx - currentMovePoint.xEnd);
    		}
    		cp1++;
    		if (cp1 >= 10)
    		{
    			cp1 = 0;
    			cBonusSpeed = 0;
    		}
    		cf = (cp1 >> 1) + 2;
    		if ((TileMap.tileTypeAtPixel(cx, cy - 1) & 0x40) == 64)
    		{
    			cx += cvx * num >> 1;
    		}
    		else
    		{
    			cx += cvx * num;
    		}
    		if (cdir == 1)
    		{
    			if (TileMap.tileTypeAt(cx + chw, cy - chh, 4))
    			{
    				if (me)
    				{
    					cvx = 0;
    					cx = TileMap.tileXofPixel(cx + chw) - chw;
    				}
    				else
    				{
    					stop();
    				}
    			}
    		}
    		else if (TileMap.tileTypeAt(cx - chw - 1, cy - chh, 8))
    		{
    			if (me)
    			{
    				cvx = 0;
    				cx = TileMap.tileXofPixel(cx - chw - 1) + TileMap.size + chw;
    			}
    			else
    			{
    				stop();
    			}
    		}
    		if (me)
    		{
    			if (cvx > 0)
    			{
    				cvx--;
    			}
    			else if (cvx < 0)
    			{
    				cvx++;
    			}
    			else
    			{
    				if (cx - cxSend != 0 && me)
    				{
    					Service.gI().charMove();
    				}
    				statusMe = 1;
    				cBonusSpeed = 0;
    			}
    		}
    		if ((TileMap.tileTypeAtPixel(cx, cy) & 2) != 2)
    		{
    			if (me)
    			{
    				if (cx - cxSend != 0 || cy - cySend != 0)
    				{
    					Service.gI().charMove();
    				}
    				cf = 7;
    				statusMe = 4;
    				delayFall = 0;
    				cvx = 3 * cdir;
    				cp2 = 0;
    			}
    			else
    			{
    				stop();
    			}
    		}
    		if (!me && currentMovePoint != null)
    		{
    			int num3 = abs(cx - currentMovePoint.xEnd);
    			if (num3 > num2)
    			{
    				stop();
    			}
    		}
    		updateCharInBridge();
    	}
    
    	private void stop()
    	{
    		statusMe = 6;
    		cp3 = 0;
    		cvx = 0;
    		cvy = 0;
    		cp1 = (cp2 = 0);
    	}
    
    	public static int abs(int i)
    	{
    		return (i <= 0) ? (-i) : i;
    	}
    }
}

namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    
    public partial class Mob : IMapObject
    {
    
    	private void updateMobStandWait()
    	{
    		checkFrameTick(stand);
    		switch (arrMobTemplate[templateId].type)
    		{
    		case 0:
    		case 1:
    		case 2:
    		case 3:
    			p1++;
    			if (p1 > 10 + mobId % 10 && (cFocus == null || Res.abs(cFocus.cx - x) > 80) && (mobToAttack == null || Res.abs(mobToAttack.x - x) > 80))
    			{
    				status = 5;
    			}
    			break;
    		case 4:
    		case 5:
    			p1++;
    			if (p1 > mobId % 3 && (cFocus == null || Res.abs(cFocus.cx - x) > 80) && (mobToAttack == null || Res.abs(mobToAttack.x - x) > 80))
    			{
    				status = 5;
    			}
    			break;
    		}
    		if (cFocus != null && GameCanvas.gameTick % (10 + p1 % 20) == 0)
    		{
    			if (cFocus.cx > x)
    			{
    				dir = 1;
    			}
    			else
    			{
    				dir = -1;
    			}
    		}
    		else if (mobToAttack != null && GameCanvas.gameTick % (10 + p1 % 20) == 0)
    		{
    			if (mobToAttack.x > x)
    			{
    				dir = 1;
    			}
    			else
    			{
    				dir = -1;
    			}
    		}
    		if (forceWait > 0)
    		{
    			forceWait--;
    			status = 2;
    		}
    	}
    
    	public virtual void updateMobAttack()
    	{
    		int[] array = ((p3 != 0) ? attack2 : attack1);
    		if (tick < array.Length)
    		{
    			checkFrameTick(array);
    			if (x >= GameScr.cmx && x <= GameScr.cmx + GameCanvas.w && p3 == 0 && GameCanvas.gameTick % 2 == 0)
    			{
    				SoundMn.gI().charPunch(false, 0.05f);
    			}
    		}
    		if (p1 == 0)
    		{
    			int num = 0;
    			int num2 = 0;
    			num = ((cFocus == null) ? mobToAttack.x : cFocus.cx);
    			num2 = ((cFocus == null) ? mobToAttack.y : cFocus.cy);
    			if (!isNewMod())
    			{
    				if (x > xFirst + arrMobTemplate[templateId].rangeMove)
    				{
    					p1 = 1;
    				}
    				if (x < xFirst - arrMobTemplate[templateId].rangeMove)
    				{
    					p1 = 1;
    				}
    			}
    			if ((arrMobTemplate[templateId].type == 4 || arrMobTemplate[templateId].type == 5) && !isDontMove)
    			{
    				y += (num2 - y) / 20;
    			}
    			p2++;
    			if (p2 > array.Length - 1 || p1 == 1)
    			{
    				p1 = 1;
    				if (p3 == 0)
    				{
    					if (cFocus != null)
    					{
    						cFocus.doInjure(dame, dameMp, false, true);
    					}
    					else
    					{
    						mobToAttack.setInjure();
    					}
    					isBusyAttackSomeOne = false;
    				}
    				else
    				{
    					if (cFocus != null)
    					{
    						MonsterDart.addMonsterDart(x + dir * w, y, checkIsBoss(), dame, dameMp, cFocus, getTemplate().dartType);
    					}
    					else
    					{
    						Char @char = new Char();
    						@char.cx = mobToAttack.x;
    						@char.cy = mobToAttack.y;
    						@char.charID = -100;
    						MonsterDart.addMonsterDart(x + dir * w, y, checkIsBoss(), dame, dameMp, @char, getTemplate().dartType);
    					}
    					isBusyAttackSomeOne = false;
    				}
    			}
    			dir = ((x < num) ? 1 : (-1));
    		}
    		else if (p1 == 1)
    		{
    			if (arrMobTemplate[templateId].type == 0 || isDontMove || isIce || !isWind)
    			{
    			}
    			if (tick == array.Length)
    			{
    				status = 2;
    				p1 = 0;
    				p2 = 0;
    				tick = 0;
    			}
    		}
    		if (tick == 5 && cFocus != null && cFocus.charID == Char.myCharz().charID)
    		{
    			if (templateId == 88 && p3 != 0)
    			{
    				GameScr.shock_scr = 2;
    			}
    			if (templateId == 89)
    			{
    				GameScr.shock_scr = 2;
    			}
    		}
    	}
    
    	public virtual void updateMobWalk()
    	{
    		int num = 0;
    		try
    		{
    			if (injureThenDie)
    			{
    				status = 1;
    				p2 = injureBy.cdir << 3;
    				p1 = -5;
    				p3 = 0;
    			}
    			num = 1;
    			if (isIce)
    			{
    				return;
    			}
    			if (isDontMove || isWind)
    			{
    				checkFrameTick(stand);
    				return;
    			}
    			switch (arrMobTemplate[templateId].type)
    			{
    			case 0:
    				if (isNewModStand())
    				{
    					frame = stand[GameCanvas.gameTick % stand.Length];
    				}
    				else
    				{
    					frame = 0;
    				}
    				num = 2;
    				break;
    			case 1:
    			case 2:
    			case 3:
    			{
    				num = 3;
    				sbyte b = arrMobTemplate[templateId].speed;
    				if (b == 1)
    				{
    					if (GameCanvas.gameTick % 2 == 1)
    					{
    						break;
    					}
    				}
    				else if (b > 2)
    				{
    					b += (sbyte)(mobId % 2);
    				}
    				else if (GameCanvas.gameTick % 2 == 1)
    				{
    					b--;
    				}
    				x += b * dir;
    				if (x > xFirst + arrMobTemplate[templateId].rangeMove)
    				{
    					dir = -1;
    				}
    				else if (x < xFirst - arrMobTemplate[templateId].rangeMove)
    				{
    					dir = 1;
    				}
    				if (Res.abs(x - Char.myCharz().cx) < 40 && Res.abs(x - xFirst) < arrMobTemplate[templateId].rangeMove)
    				{
    					dir = ((x <= Char.myCharz().cx) ? 1 : (-1));
    					if (Res.abs(x - Char.myCharz().cx) < 20)
    					{
    						x -= dir * 10;
    					}
    					status = 2;
    					forceWait = 20;
    				}
    				checkFrameTick((w <= 30) ? moveFast : move);
    				break;
    			}
    			case 4:
    			{
    				num = 4;
    				sbyte speed2 = arrMobTemplate[templateId].speed;
    				speed2 += (sbyte)(mobId % 2);
    				x += speed2 * dir;
    				if (GameCanvas.gameTick % 10 > 2)
    				{
    					y += speed2 * dirV;
    				}
    				speed2 += (sbyte)((GameCanvas.gameTick + mobId) % 2);
    				if (x > xFirst + arrMobTemplate[templateId].rangeMove)
    				{
    					dir = -1;
    					status = 2;
    					forceWait = GameCanvas.gameTick % 20 + 20;
    					p1 = 0;
    				}
    				else if (x < xFirst - arrMobTemplate[templateId].rangeMove)
    				{
    					dir = 1;
    					status = 2;
    					forceWait = GameCanvas.gameTick % 20 + 20;
    					p1 = 0;
    				}
    				if (y > yFirst + 24)
    				{
    					dirV = -1;
    				}
    				else if (y < yFirst - (20 + GameCanvas.gameTick % 10))
    				{
    					dirV = 1;
    				}
    				checkFrameTick(move);
    				break;
    			}
    			case 5:
    			{
    				num = 5;
    				sbyte speed = arrMobTemplate[templateId].speed;
    				speed += (sbyte)(mobId % 2);
    				x += speed * dir;
    				speed += (sbyte)((GameCanvas.gameTick + mobId) % 2);
    				if (GameCanvas.gameTick % 10 > 2)
    				{
    					y += speed * dirV;
    				}
    				if (x > xFirst + arrMobTemplate[templateId].rangeMove)
    				{
    					dir = -1;
    					status = 2;
    					forceWait = GameCanvas.gameTick % 20 + 20;
    					p1 = 0;
    				}
    				else if (x < xFirst - arrMobTemplate[templateId].rangeMove)
    				{
    					dir = 1;
    					status = 2;
    					forceWait = GameCanvas.gameTick % 20 + 20;
    					p1 = 0;
    				}
    				if (y > yFirst + 24)
    				{
    					dirV = -1;
    				}
    				else if (y < yFirst - (20 + GameCanvas.gameTick % 10))
    				{
    					dirV = 1;
    				}
    				if (TileMap.tileTypeAt(x, y, 2))
    				{
    					if (GameCanvas.gameTick % 10 > 5)
    					{
    						y = TileMap.tileYofPixel(y);
    						status = 4;
    						p1 = 0;
    						dirV = -1;
    					}
    					else
    					{
    						dirV = -1;
    					}
    				}
    				break;
    			}
    			}
    		}
    		catch (Exception)
    		{
    			Cout.println("lineee: " + num);
    		}
    	}
    
    	public MobTemplate getTemplate()
    	{
    		EnsureMobTemplateCapacity(templateId);
    		if (arrMobTemplate != null && templateId >= 0 && templateId < arrMobTemplate.Length)
    		{
    			if (arrMobTemplate[templateId] == null)
    			{
    				arrMobTemplate[templateId] = new MobTemplate();
    				arrMobTemplate[templateId].mobTemplateId = (sbyte)templateId;
    			}
    			return arrMobTemplate[templateId];
    		}
    		return null;
    	}
    
    	public virtual bool isPaint()
    	{
    		if (x < GameScr.cmx - 50)
    		{
    			return false;
    		}
    		if (x > GameScr.cmx + GameScr.gW + 50)
    		{
    			return false;
    		}
    		if (y < GameScr.cmy - 50)
    		{
    			return false;
    		}
    		if (y > GameScr.cmy + GameScr.gH + 50)
    		{
    			return false;
    		}
    		MobTemplate t = getTemplate();
    		if (t == null || t.data == null || t.data.img == null || status == 0)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public virtual bool isUpdate()
    	{
    		MobTemplate t = getTemplate();
    		if (t == null || t.data == null || status == 0)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public virtual bool checkIsBoss()
    	{
    		if (isBoss || levelBoss > 0)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void updateHp_bar()
    	{
    		if (maxHp <= 0)
    		{
    			maxHp = 1;
    		}
    		if (hp > maxHp)
    		{
    			maxHp = hp;
    		}
    		per = (int)((long)hp * 100L / maxHp);
    		if (per > 100)
    		{
    			per = 100;
    		}
    		else if (per < 0)
    		{
    			per = 0;
    		}
    		len = (int)((long)hp * 100L / maxHp * w_hp_bar) / 100;
    		if (len > w_hp_bar)
    		{
    			len = w_hp_bar;
    		}
    		else if (len < 0)
    		{
    			len = 0;
    		}
    		if (per == 100)
    		{
    			per_tem = per;
    		}
    		if (per >= 100)
    		{
    			per_tem = per;
    		}
    		offset = 0;
    		if (per < 30)
    		{
    			color = 15473700;
    			imgHPtem = GameScr.imgHP_tm_do;
    		}
    		else if (per < 60)
    		{
    			color = 16744448;
    			imgHPtem = GameScr.imgHP_tm_vang;
    		}
    		else
    		{
    			color = 11992374;
    			imgHPtem = GameScr.imgHP_tm_xanh;
    		}
    	}
    
    	public virtual void paint(mGraphics g)
    	{
    		if (isHide || !isPaint())
    		{
    			return;
    		}
    		if (isMafuba)
    		{
    			if (!changBody)
    			{
    				if (arrMobTemplate != null && templateId >= 0 && templateId < arrMobTemplate.Length && arrMobTemplate[templateId] != null && arrMobTemplate[templateId].data != null)
    				{
    					arrMobTemplate[templateId].data.paintFrame(g, frame, xMFB, yMFB, (dir != 1) ? 1 : 0, 2);
    				}
    			}
    			else
    			{
    				SmallImage.drawSmallImage(g, smallBody, xMFB, yMFB, (dir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER);
    			}
    			return;
    		}
    		if (isShadown && status != 0)
    		{
    			paintShadow(g);
    		}
    		if (status == 1 && p3 > 0 && GameCanvas.gameTick % 3 == 0)
    		{
    			return;
    		}
    		g.translate(0, GameCanvas.transY);
    		if (!changBody)
    		{
    			if (arrMobTemplate != null && templateId >= 0 && templateId < arrMobTemplate.Length && arrMobTemplate[templateId] != null && arrMobTemplate[templateId].data != null)
    			{
    				arrMobTemplate[templateId].data.paintFrame(g, frame, x, y + fy, (dir != 1) ? 1 : 0, 2);
    			}
    		}
    		else
    		{
    			SmallImage.drawSmallImage(g, smallBody, x, y + fy - 9, (dir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER);
    		}
    		g.translate(0, -GameCanvas.transY);
    		if (Char.myCharz().mobFocus == null || !Char.myCharz().mobFocus.Equals(this) || status == 1 || hp <= 0 || imgHPtem == null)
    		{
    			return;
    		}
    		int imageWidth = mGraphics.getImageWidth(imgHPtem);
    		int imageHeight = mGraphics.getImageHeight(imgHPtem);
    		int num = imageWidth * per / 100;
    		if (num > imageWidth)
    		{
    			num = imageWidth;
    		}
    		else if (num < 0)
    		{
    			num = 0;
    		}
    		int num2 = num;
    		if (per_tem >= per)
    		{
    			num2 = imageWidth * (per_tem -= ((GameCanvas.gameTick % 6 <= 3) ? offset : offset++)) / 100;
    			if (per_tem <= 0)
    			{
    				per_tem = 0;
    			}
    			if (per_tem < per)
    			{
    				per_tem = per;
    			}
    			if (offset >= 3)
    			{
    				offset = 3;
    			}
    		}
    		if (num2 > imageWidth)
    		{
    			num2 = imageWidth;
    		}
    		else if (num2 < 0)
    		{
    			num2 = 0;
    		}
    		g.drawImage(GameScr.imgHP_tm_xam, x - (imageWidth >> 1), y - h - 5, mGraphics.TOP | mGraphics.LEFT);
    		g.setColor(16777215);
    		g.fillRect(x - (imageWidth >> 1), y - h - 5, num2, 2);
    		g.drawRegion(imgHPtem, 0, 0, num, imageHeight, 0, x - (imageWidth >> 1), y - h - 5, mGraphics.TOP | mGraphics.LEFT);
    	}
    
    	public virtual int getHPColor()
    	{
    		return 16711680;
    	}
    
    	public virtual void startDie()
    	{
    		hp = 0;
    		injureThenDie = true;
    		hp = 0;
    		status = 1;
    		Res.outz("MOB DIEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEe");
    		p1 = -3;
    		p2 = -dir;
    		p3 = 0;
    	}
    
    	public virtual void attackOtherMob(Mob mobToAttack)
    	{
    		this.mobToAttack = mobToAttack;
    		isBusyAttackSomeOne = true;
    		cFocus = null;
    		p1 = 0;
    		p2 = 0;
    		status = 3;
    		tick = 0;
    		dir = ((mobToAttack.x > x) ? 1 : (-1));
    		int num = mobToAttack.x;
    		int num2 = mobToAttack.y;
    		if (Res.abs(num - x) < w * 2 && Res.abs(num2 - y) < h * 2)
    		{
    			if (x < num)
    			{
    				x = num - w;
    			}
    			else
    			{
    				x = num + w;
    			}
    			p3 = 0;
    		}
    		else
    		{
    			p3 = 1;
    		}
    	}
    
    	public virtual int getX()
    	{
    		return x;
    	}
    
    	public virtual int getY()
    	{
    		return y;
    	}
    
    	public virtual int getH()
    	{
    		return h;
    	}
    
    	public virtual int getW()
    	{
    		return w;
    	}
    
    	public virtual void stopMoving()
    	{
    		if (status == 5)
    		{
    			status = 2;
    			p1 = (p2 = (p3 = 0));
    			forceWait = 50;
    		}
    	}
    
    	public virtual bool isInvisible()
    	{
    		return status == 0 || status == 1;
    	}
    
    	public virtual void removeHoldEff()
    	{
    		if (holdEffID != 0)
    		{
    			holdEffID = 0;
    		}
    	}
    
    	public virtual void removeBlindEff()
    	{
    		blindEff = false;
    	}
    
    	public virtual void removeSleepEff()
    	{
    		sleepEff = false;
    	}
    
    	public virtual void GetFrame()
    	{
    		MobTemplate t = getTemplate();
    		if (isGetFr && isTypeNewMod() && t != null && t.data != null)
    		{
    			frameArr = (int[][])Controller.frameHT_NEWBOSS.get(templateId + string.Empty);
    			stand = frameArr[0];
    			move = frameArr[1];
    			moveFast = frameArr[2];
    			attack1 = frameArr[3];
    			attack2 = frameArr[4];
    			hurt = frameArr[5];
    			isGetFr = false;
    		}
    	}
    
    	private bool isTypeNewMod()
    	{
    		MobTemplate t = getTemplate();
    		if (t != null && t.data != null && t.data.typeData == 2)
    		{
    			return true;
    		}
    		return false;
    	}
    }
}

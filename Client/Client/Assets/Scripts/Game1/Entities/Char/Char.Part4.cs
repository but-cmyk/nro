namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    
    public partial class Char : IMapObject
    {
    
    	public void updateCharJump()
    	{
    		setMountIsStart();
    		ty = 0;
    		isFreez = false;
    		if (isCharge)
    		{
    			isCharge = false;
    			SoundMn.gI().taitaoPause();
    			Service.gI().skill_not_focus(3);
    		}
    		addDustEff(3);
    		cx += cvx;
    		cy += cvy;
    		if (cy < 0)
    		{
    			cy = 0;
    			cvy = -1;
    		}
    		cvy++;
    		if (cvy > 0)
    		{
    			cvy = 0;
    		}
    		if (!me && currentMovePoint != null)
    		{
    			int num = currentMovePoint.xEnd - cx;
    			if (num > 0)
    			{
    				if (cvx > num)
    				{
    					cvx = num;
    				}
    				if (cvx < 0)
    				{
    					cvx = num;
    				}
    			}
    			else if (num < 0)
    			{
    				if (cvx < num)
    				{
    					cvx = num;
    				}
    				if (cvx > 0)
    				{
    					cvx = num;
    				}
    			}
    			else
    			{
    				cvx = num;
    			}
    		}
    		if (cdir == 1)
    		{
    			if ((TileMap.tileTypeAtPixel(cx + chw, cy - 1) & 4) == 4 && cx <= TileMap.tileXofPixel(cx + chw) + 12)
    			{
    				cx = TileMap.tileXofPixel(cx + chw) - chw;
    				cvx = 0;
    			}
    		}
    		else if ((TileMap.tileTypeAtPixel(cx - chw, cy - 1) & 8) == 8 && cx >= TileMap.tileXofPixel(cx - chw) + 12)
    		{
    			cx = TileMap.tileXofPixel(cx + 24 - chw) + chw;
    			cvx = 0;
    		}
    		if (cvy == 0)
    		{
    			if (!isAttFly)
    			{
    				if (me)
    				{
    					setCharFallFromJump();
    				}
    				else
    				{
    					stop();
    				}
    			}
    			else
    			{
    				setCharFallFromJump();
    			}
    		}
    		if (me && !ischangingMap && isInWaypoint())
    		{
    			Service.gI().charMove();
    			if (TileMap.isTrainingMap())
    			{
    				ischangingMap = true;
    				Service.gI().getMapOffline();
    			}
    			else
    			{
    				Service.gI().requestChangeMap();
    			}
    			isLockKey = true;
    			ischangingMap = true;
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    			InfoDlg.showWait();
    			return;
    		}
    		if (statusMe != 16 && (TileMap.tileTypeAt(cx, cy - ch + 24, 8192) || cy < 0))
    		{
    			statusMe = 4;
    			cp1 = 0;
    			cp2 = 0;
    			cvy = 1;
    			delayFall = 0;
    			if (cy < 0)
    			{
    				cy = 0;
    			}
    			cy = TileMap.tileYofPixel(cy + 25);
    			GameCanvas.clearKeyHold();
    		}
    		if (cp3 < 0)
    		{
    			cp3++;
    		}
    		cf = 7;
    		if (!me && currentMovePoint != null && cy < currentMovePoint.yEnd)
    		{
    			stop();
    		}
    	}
    
    	public bool checkInRangeJump(int x1, int xw1, int xmob, int y1, int yh1, int ymob)
    	{
    		if (xmob > xw1 || xmob < x1 || ymob > y1 || ymob < yh1)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public void setCharFallFromJump()
    	{
    		cyStartFall = cy;
    		cp1 = 0;
    		cp2 = 0;
    		statusMe = 10;
    		cvx = cdir << 2;
    		cvy = 0;
    		cy = TileMap.tileYofPixel(cy) + 12;
    		if (me && (cx - cxSend != 0 || cy - cySend != 0) && (Res.abs(myCharz().cx - myCharz().cxSend) > 96 || Res.abs(myCharz().cy - myCharz().cySend) > 24))
    		{
    			Service.gI().charMove();
    		}
    	}
    
    	public void updateCharFall()
    	{
    		if (holder)
    		{
    			return;
    		}
    		ty = 0;
    		if (cy + 4 >= TileMap.pxh)
    		{
    			statusMe = 1;
    			if (me)
    			{
    				SoundMn.gI().charFall();
    			}
    			cvx = (cvy = 0);
    			cp3 = 0;
    			return;
    		}
    		if (cy % 24 == 0 && (TileMap.tileTypeAtPixel(cx, cy) & 2) == 2)
    		{
    			delayFall = 0;
    			if (me)
    			{
    				if (cy - cySend > 0)
    				{
    					Service.gI().charMove();
    				}
    				else if (cx - cxSend != 0 || cy - cySend < 0)
    				{
    					Service.gI().charMove();
    				}
    				cvx = (cvy = 0);
    				cp1 = (cp2 = 0);
    				statusMe = 1;
    				cp3 = 0;
    				return;
    			}
    			stop();
    			cf = 0;
    			GameCanvas.gI().startDust(-1, cx - -8, cy);
    			GameCanvas.gI().startDust(1, cx - 8, cy);
    			addDustEff(1);
    		}
    		if (delayFall > 0)
    		{
    			delayFall--;
    			if (delayFall % 10 > 5)
    			{
    				cy++;
    			}
    			else
    			{
    				cy--;
    			}
    			return;
    		}
    		if (cvy < -4)
    		{
    			cf = 7;
    		}
    		else
    		{
    			cf = 12;
    		}
    		cx += cvx;
    		if (!me && currentMovePoint != null)
    		{
    			int num = currentMovePoint.xEnd - cx;
    			if (num > 0)
    			{
    				if (cvx > num)
    				{
    					cvx = num;
    				}
    				if (cvx < 0)
    				{
    					cvx = num;
    				}
    			}
    			else if (num < 0)
    			{
    				if (cvx < num)
    				{
    					cvx = num;
    				}
    				if (cvx > 0)
    				{
    					cvx = num;
    				}
    			}
    			else
    			{
    				cvx = num;
    			}
    		}
    		cvy++;
    		if (cvy > 8)
    		{
    			cvy = 8;
    		}
    		if (skillPaintRandomPaint == null)
    		{
    			cy += cvy;
    		}
    		if (cdir == 1)
    		{
    			if ((TileMap.tileTypeAtPixel(cx + chw, cy - 1) & 4) == 4 && cx <= TileMap.tileXofPixel(cx + chw) + 12)
    			{
    				cx = TileMap.tileXofPixel(cx + chw) - chw;
    				cvx = 0;
    			}
    		}
    		else if ((TileMap.tileTypeAtPixel(cx - chw, cy - 1) & 8) == 8 && cx >= TileMap.tileXofPixel(cx - chw) + 12)
    		{
    			cx = TileMap.tileXofPixel(cx + 24 - chw) + chw;
    			cvx = 0;
    		}
    		if (cvy > 3 && (cyStartFall == 0 || cyStartFall <= TileMap.tileYofPixel(cy + 3)) && (TileMap.tileTypeAtPixel(cx, cy + 3) & 2) == 2)
    		{
    			if (me)
    			{
    				cyStartFall = 0;
    				cvx = (cvy = 0);
    				cp1 = (cp2 = 0);
    				cy = TileMap.tileYofPixel(cy + 3);
    				statusMe = 1;
    				if (me)
    				{
    					SoundMn.gI().charFall();
    				}
    				cp3 = 0;
    				GameCanvas.gI().startDust(-1, cx - -8, cy);
    				GameCanvas.gI().startDust(1, cx - 8, cy);
    				addDustEff(1);
    				if (cy - cySend > 0)
    				{
    					if (me)
    					{
    						Service.gI().charMove();
    					}
    				}
    				else if ((cx - cxSend != 0 || cy - cySend < 0) && me)
    				{
    					Service.gI().charMove();
    				}
    			}
    			else
    			{
    				stop();
    				cy = TileMap.tileYofPixel(cy + 3);
    				cf = 0;
    				GameCanvas.gI().startDust(-1, cx - -8, cy);
    				GameCanvas.gI().startDust(1, cx - 8, cy);
    				addDustEff(1);
    				currentMovePoint = null;
    			}
    			return;
    		}
    		cf = 12;
    		if (me)
    		{
    			if (!isAttack)
    			{
    			}
    			return;
    		}
    		if ((TileMap.tileTypeAtPixel(cx, cy + 1) & 2) == 2)
    		{
    			cf = 0;
    		}
    		if (currentMovePoint != null && cy > currentMovePoint.yEnd)
    		{
    			stop();
    			cy = TileMap.tileYofPixel(cy + 3);
    			currentMovePoint = null;
    		}
    	}
    
    	public void updateCharFly()
    	{
    		int num = ((isMonkey != 1 || me) ? 1 : 2);
    		setMountIsStart();
    		if (statusMe != 16 && (TileMap.tileTypeAt(cx, cy - ch + 24, 8192) || cy < 0))
    		{
    			if (cy - ch < 0)
    			{
    				cy = ch;
    			}
    			cf = 7;
    			statusMe = 4;
    			cvx = 0;
    			cp2 = 0;
    			currentMovePoint = null;
    			return;
    		}
    		int num2 = cy;
    		if (isHead_Fly(head))
    		{
    			if (GameCanvas.gameTick % 3 == 0)
    			{
    				cp1++;
    			}
    			if (cp1 > 4)
    			{
    				cp1 = 0;
    			}
    			cf = cp1 + 2;
    		}
    		else
    		{
    			cp1++;
    			if (cp1 >= 9)
    			{
    				cp1 = 0;
    				if (!me)
    				{
    					cvx = (cvy = 0);
    				}
    				cBonusSpeed = 0;
    			}
    			cf = 8;
    			if (Res.abs(cvx) <= 4 && me)
    			{
    				if (currentMovePoint != null)
    				{
    					int num3 = abs(cx - currentMovePoint.xEnd);
    					int num4 = abs(cy - currentMovePoint.yEnd);
    					if (num3 > num4 * 10)
    					{
    						cf = 8;
    					}
    					else if (num3 > num4 && num3 > 48 && num4 > 32)
    					{
    						cf = 8;
    					}
    					else
    					{
    						cf = 7;
    					}
    				}
    				else
    				{
    					if (cvy < 0)
    					{
    						cvy = 0;
    					}
    					if (cvy > 16)
    					{
    						cvy = 16;
    					}
    					cf = 7;
    				}
    			}
    			if (!me)
    			{
    				if (abs(cvx) < 2)
    				{
    					cvx = (cdir << 1) * num;
    				}
    				if (cvy != 0)
    				{
    					cf = 7;
    				}
    				if (abs(cvx) <= 2)
    				{
    					cp2++;
    					if (cp2 > 32)
    					{
    						statusMe = 4;
    						cvx = 0;
    						cvy = 0;
    					}
    				}
    			}
    		}
    		if (cdir == 1)
    		{
    			if (TileMap.tileTypeAt(cx + chw, cy - 1, 4))
    			{
    				cvx = 0;
    				cx = TileMap.tileXofPixel(cx + chw) - chw;
    				if (cvy == 0)
    				{
    					currentMovePoint = null;
    				}
    			}
    		}
    		else if (TileMap.tileTypeAt(cx - chw - 1, cy - 1, 8))
    		{
    			cvx = 0;
    			cx = TileMap.tileXofPixel(cx - chw - 1) + TileMap.size + chw;
    			if (cvy == 0)
    			{
    				currentMovePoint = null;
    			}
    		}
    		cx += cvx * num;
    		cy += cvy * num;
    		if (!isMount && num2 - cy == 0)
    		{
    			ty++;
    			wt++;
    			fy += ((!wy) ? 1 : (-1));
    			if (wt == 10)
    			{
    				wt = 0;
    				wy = !wy;
    			}
    			if (ty > 20)
    			{
    				delayFall = 10;
    				if (GameCanvas.gameTick % 3 == 0)
    				{
    					ServerEffect.addServerEffect(111, cx + ((cdir != 1) ? 27 : (-17)), cy + fy + 13, 1, (cdir != 1) ? 2 : 0);
    				}
    			}
    		}
    		if (!me)
    		{
    			return;
    		}
    		if (cvx > 0)
    		{
    			cvx--;
    		}
    		else if (cvx < 0)
    		{
    			cvx++;
    		}
    		else if (cvy == 0)
    		{
    			statusMe = 4;
    			checkDelayFallIfTooHigh();
    			Service.gI().charMove();
    		}
    		if ((TileMap.tileTypeAtPixel(cx, cy + 20) & 2) == 2 || (TileMap.tileTypeAtPixel(cx, cy + 40) & 2) == 2)
    		{
    			if (cvy == 0)
    			{
    				delayFall = 0;
    			}
    			cyStartFall = 0;
    			cvx = (cvy = 0);
    			cp1 = (cp2 = 0);
    			statusMe = 4;
    			addDustEff(3);
    		}
    		if (abs(cx - cxSend) > 96 || abs(cy - cySend) > 24)
    		{
    			Service.gI().charMove();
    		}
    	}
    
    	private bool isHead_Fly(int head2)
    	{
    		if (Arr_Head_FlyMove.Length > 0)
    		{
    			for (int i = 0; i < Arr_Head_FlyMove.Length; i++)
    			{
    				if (Arr_Head_FlyMove[i] == head2)
    				{
    					return true;
    				}
    			}
    		}
    		return false;
    	}
    
    	public void setMount(int cid, int ctrans, int cgender)
    	{
    		idcharMount = cid;
    		transMount = ctrans;
    		genderMount = cgender;
    		speedMount = 30;
    		if (transMount < 0)
    		{
    			transMount = 0;
    			xMount = GameScr.cmx + GameCanvas.w + 50;
    			dxMount = -19;
    		}
    		else if (transMount == 1)
    		{
    			transMount = 2;
    			xMount = GameScr.cmx - 100;
    			dxMount = -33;
    		}
    		dyMount = -17;
    		yMount = cy;
    		frameMount = 0;
    		frameNewMount = 0;
    		isMount = false;
    		isEndMount = false;
    	}
    
    	public void updateMount()
    	{
    		frameMount++;
    		if (frameMount > FrameMount.Length - 1)
    		{
    			frameMount = 0;
    		}
    		frameNewMount++;
    		if (frameNewMount > 1000)
    		{
    			frameNewMount = 0;
    		}
    		if (isStartMount && !isMount)
    		{
    			yMount = cy;
    			if (transMount == 0)
    			{
    				if (xMount - cx >= speedMount)
    				{
    					xMount -= speedMount;
    					return;
    				}
    				xMount = cx;
    				isMount = true;
    				isEndMount = false;
    			}
    			else if (transMount == 2)
    			{
    				if (cx - xMount >= speedMount)
    				{
    					xMount += speedMount;
    					return;
    				}
    				xMount = cx;
    				isMount = true;
    				isEndMount = false;
    			}
    		}
    		else if (isMount)
    		{
    			if (statusMe == 14 || ySd - cy < 24)
    			{
    				setMountIsEnd();
    			}
    			if (cp1 % 15 < 5)
    			{
    				cf = 0;
    			}
    			else
    			{
    				cf = 1;
    			}
    			transMount = cdir;
    			updateSuperEff();
    			if (transMount < 0)
    			{
    				transMount = 0;
    				dxMount = -19;
    			}
    			else if (transMount == 1)
    			{
    				transMount = 2;
    				dxMount = -31;
    				if (isEventMount)
    				{
    					dxMount = -38;
    				}
    			}
    			if (skillInfoPaint() != null)
    			{
    				dyMount = -15;
    			}
    			else
    			{
    				dyMount = -17;
    			}
    			yMount = cy;
    			xMount = cx;
    		}
    		else if (isEndMount)
    		{
    			if (transMount == 0)
    			{
    				if (xMount > GameScr.cmx - 100)
    				{
    					xMount -= 20;
    					return;
    				}
    				isStartMount = false;
    				isMount = false;
    				isEndMount = false;
    			}
    			else if (transMount == 2)
    			{
    				if (xMount < GameScr.cmx + GameCanvas.w + 50)
    				{
    					xMount += 20;
    					return;
    				}
    				isStartMount = false;
    				isMount = false;
    				isEndMount = false;
    			}
    		}
    		else if (!isStartMount || !isMount || !isEndMount)
    		{
    			xMount = GameScr.cmx - 100;
    			yMount = GameScr.cmy - 100;
    		}
    	}
    
    	public void getMountData()
    	{
    		if (Mob.arrMobTemplate[50].data == null)
    		{
    			Mob.arrMobTemplate[50].data = new EffectData();
    			string text = "/Mob/" + 50;
    			DataInputStream dataInputStream = null;
    			dataInputStream = MyStream.readFile(text);
    			if (dataInputStream != null)
    			{
    				Mob.arrMobTemplate[50].data.readData(text + "/data");
    				Mob.arrMobTemplate[50].data.img = GameCanvas.loadImage(text + "/img.png");
    			}
    			else
    			{
    				Service.gI().requestModTemplate(50);
    			}
    			Mob.lastMob.addElement(50 + string.Empty);
    		}
    	}
    
    	public void checkFrameTick(int[] array)
    	{
    		t++;
    		if (t > array.Length - 1)
    		{
    			t = 0;
    		}
    		fM = array[t];
    	}
    
    	private int cachedNewMountId0 = -1;
    	private string cachedNewMountName0;
    	private int cachedNewMountId1 = -1;
    	private string cachedNewMountName1;

    
    
    	public void setMountIsStart()
    	{
    		if (me)
    		{
    			isHaveMount = checkHaveMount();
    			if (TileMap.isVoDaiMap())
    			{
    				isHaveMount = false;
    			}
    		}
    		if (isHaveMount)
    		{
    			if (ySd - cy <= 20)
    			{
    				xChar = cx;
    			}
    			if (xdis < 100)
    			{
    				xdis = Res.abs(xChar - cx);
    			}
    			if (xdis >= 70 && ySd - cy > 30 && !isStartMount && !isEndMount)
    			{
    				setMount(charID, cdir, (mountGender >= 0) ? mountGender : cgender);
    				isStartMount = true;
    			}
    		}
    	}
    
    	public void setMountIsEnd()
    	{
    		if (ySd - cy < 24 && !isEndMount)
    		{
    			isStartMount = false;
    			isMount = false;
    			isEndMount = true;
    			xdis = 0;
    		}
    	}
    
    	public bool checkHaveMount()
    	{
    		bool result = false;
    		short num = -1;
    		Item[] array = arrItemBody;
    		for (int i = 0; i < array.Length; i++)
    		{
    			if (array[i] != null && (array[i].template.type == 24 || array[i].template.type == 23))
    			{
    				num = ((array[i].template.part < 0) ? array[i].template.id : ((short)(ID_NEW_MOUNT + array[i].template.part)));
    				result = true;
    				break;
    			}
    		}
    		isMountVip = false;
    		isSpeacialMount = false;
    		isEventMount = false;
    		idMount = -1;
    		mountGender = -1;
    		switch (num)
    		{
    		case 346:
    			mountGender = 0;
    			break;
    		case 347:
    			mountGender = 1;
    			break;
    		case 348:
    			mountGender = 2;
    			break;
    		case 349:
    			isMountVip = true;
    			mountGender = 0;
    			break;
    		case 350:
    			isMountVip = true;
    			mountGender = 1;
    			break;
    		case 351:
    			isMountVip = true;
    			mountGender = 2;
    			break;
    		case 396:
    			isEventMount = true;
    			break;
    		case 532:
    			isSpeacialMount = true;
    			break;
    		default:
    			if (num >= ID_NEW_MOUNT)
    			{
    				idMount = num;
    			}
    			break;
    		}
    		return result;
    	}
    
    	private void checkDelayFallIfTooHigh()
    	{
    		bool flag = true;
    		for (int i = 0; i < 150; i += 24)
    		{
    			if ((TileMap.tileTypeAtPixel(cx, cy + i) & 2) == 2 || cy + i > TileMap.tmh * TileMap.size - 24)
    			{
    				flag = false;
    				break;
    			}
    		}
    		if (flag)
    		{
    			delayFall = 40;
    		}
    	}
    
    	public void setDefaultPart()
    	{
    		setDefaultWeapon();
    		setDefaultBody();
    		setDefaultLeg();
    	}
    
    	public void setDefaultWeapon()
    	{
    		if (cgender == 0)
    		{
    			wp = 0;
    		}
    	}
    
    	public void setDefaultBody()
    	{
    		if (cgender == 0)
    		{
    			body = 57;
    		}
    		else if (cgender == 1)
    		{
    			body = 59;
    		}
    		else if (cgender == 2)
    		{
    			body = 57;
    		}
    	}
    
    	public void setDefaultLeg()
    	{
    		if (cgender == 0)
    		{
    			leg = 58;
    		}
    		else if (cgender == 1)
    		{
    			leg = 60;
    		}
    		else if (cgender == 2)
    		{
    			leg = 58;
    		}
    	}
    
    	public bool isSelectingSkillUseAlone()
    	{
    		return myskill != null && myskill.template.isUseAlone();
    	}
    
    	public bool isUseSkillSpec()
    	{
    		return myskill != null && myskill.template.isSkillSpec();
    	}
    
    	public bool isSelectingSkillBuffToPlayer()
    	{
    		return myskill != null && myskill.template.isBuffToPlayer();
    	}
    
    	public bool isUseChargeSkill()
    	{
    		return !isUseSkillAfterCharge && myskill != null && (myskill.template.id == 10 || myskill.template.id == 11);
    	}
    
    	public virtual void setSkillPaint(SkillPaint skillPaint, int sType)
    	{
    		hasSendAttack = false;
    		if (stone || (me && myskill != null && myskill.template != null && myskill.template.id == 9 && cHP <= cHPFull / 10))
    		{
    			return;
    		}
    		if (me)
    		{
    			if (mobFocus == null && charFocus == null)
    			{
    				stopUseChargeSkill();
    			}
    			if (mobFocus != null && (mobFocus.status == 1 || mobFocus.status == 0))
    			{
    				stopUseChargeSkill();
    			}
    			if (charFocus != null && (charFocus.statusMe == 14 || charFocus.statusMe == 5))
    			{
    				stopUseChargeSkill();
    			}
    			if (myskill != null && myskill.template != null && ((myskill.template.id == 23 && ((charFocus != null && charFocus.holdEffID != 0) || (mobFocus != null && mobFocus.holdEffID != 0) || holdEffID != 0)) || sleepEff || blindEff))
    			{
    				return;
    			}
    		}
    		if (skillPaint == null)
    		{
    			if (GameScr.sks == null || GameScr.sks.Length == 0)
    			{
    				GameScr.gI().readSkill();
    			}
    			if (GameScr.sks != null && GameScr.sks.Length > 0 && GameScr.sks[0] != null)
    			{
    				skillPaint = GameScr.sks[0];
    			}
    			else
    			{
    				skillPaint = new SkillPaint();
    				skillPaint.id = 0;
    			}
    		}
    		if ((me && dart != null) || (TileMap.isOfflineMap() && mobFocus == null))
    		{
    			return;
    		}
    		long num = mSystem.currentTimeMillis();
    		if (me)
    		{
    			if (myskill == null && vSkillFight != null && vSkillFight.size() > 0)
    			{
    				myskill = (Skill)vSkillFight.elementAt(0);
    			}
    			if (myskill == null)
    			{
    				return;
    			}
    			if (isSelectingSkillBuffToPlayer() && charFocus == null)
    			{
    				return;
    			}
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
    			myskill.lastTimeUseThisSkill = num;
    			if (myskill.template.manaUseType == 2)
    			{
    				cMP = 1;
    			}
    			else if (myskill.template.manaUseType != 1)
    			{
    				cMP -= myskill.manaUse;
    			}
    			else
    			{
    				cMP -= myskill.manaUse * cMPFull / 100;
    			}
    			myCharz().cStamina--;
    			GameScr.gI().isInjureMp = true;
    			GameScr.gI().twMp = 0;
    			if (cMP < 0)
    			{
    				cMP = 0;
    			}
    		}
    		if (me)
    		{
    			if (myskill.template.id == 10)
    			{
    				Service.gI().skill_not_focus(4);
    			}
    			if (myskill.template.id == 11)
    			{
    				Service.gI().skill_not_focus(4);
    			}
    			if (myskill.template.id == 7)
    			{
    				SoundMn.gI().hoisinh();
    			}
    			if (myskill.template.id == 6)
    			{
    				Service.gI().skill_not_focus(0);
    				GameScr.gI().isUseFreez = true;
    				SoundMn.gI().thaiduonghasan();
    			}
    			if (myskill.template.id == 8)
    			{
    				if (!isCharge)
    				{
    					SoundMn.gI().taitaoPause();
    					Service.gI().skill_not_focus(1);
    					isCharge = true;
    					last = (cur = mSystem.currentTimeMillis());
    				}
    				else
    				{
    					Service.gI().skill_not_focus(3);
    					isCharge = false;
    					SoundMn.gI().taitaoPause();
    				}
    			}
    			if (myskill.template.id == 13)
    			{
    				if (isMonkey != 0)
    				{
    					GameScr.gI().auto = 0;
    				}
    				else if (!isCreateDark)
    				{
    					SoundMn.gI().gong();
    					Service.gI().skill_not_focus(6);
    					chargeCount = 0;
    					isWaitMonkey = true;
    				}
    				return;
    			}
    			if (myskill.template.id == 14)
    			{
    				SoundMn.gI().gong();
    				Service.gI().skill_not_focus(7);
    				useChargeSkill(true);
    			}
    			if (myskill.template.id == 21)
    			{
    				Service.gI().skill_not_focus(10);
    				return;
    			}
    			if (myskill.template.id == 12)
    			{
    				Service.gI().skill_not_focus(8);
    			}
    			if (myskill.template.id == 19)
    			{
    				Service.gI().skill_not_focus(9);
    				return;
    			}
    		}
    		if (isMonkey == 1 && skillPaint.id >= 35 && skillPaint.id <= 41)
    		{
    			skillPaint = GameScr.sks[106];
    		}
    		if (skillPaint.id >= 128 && skillPaint.id <= 134)
    		{
    			skillPaint = GameScr.sks[skillPaint.id - 65];
    			if (charFocus != null)
    			{
    				cx = charFocus.cx;
    				cy = charFocus.cy;
    				currentMovePoint = null;
    			}
    			if (mobFocus != null)
    			{
    				cx = mobFocus.x;
    				cy = mobFocus.y;
    				currentMovePoint = null;
    			}
    			ServerEffect.addServerEffect(60, cx, cy, 1);
    			telePortSkill = true;
    		}
    		if (skillPaint.id >= 107 && skillPaint.id <= 113)
    		{
    			skillPaint = GameScr.sks[skillPaint.id - 44];
    			EffecMn.addEff(new Effect(23, cx, cy + ch / 2, 3, 2, 1));
    		}
    		setAutoSkillPaint(skillPaint, sType);
    	}
    
    	public void useSkillNotFocus()
    	{
    		GameScr.gI().auto = 0;
    		if (GameScr.sks == null || GameScr.sks.Length == 0)
    		{
    			GameScr.gI().readSkill();
    		}
    		SkillPaint sp = null;
    		if (GameScr.sks != null && myCharz().myskill != null && myCharz().myskill.skillId >= 0 && myCharz().myskill.skillId < GameScr.sks.Length)
    		{
    			sp = GameScr.sks[myCharz().myskill.skillId];
    		}
    		if (sp == null && GameScr.sks != null && GameScr.sks.Length > 0)
    		{
    			sp = GameScr.sks[0];
    		}
    		if (sp == null)
    		{
    			sp = new SkillPaint();
    			sp.id = 0;
    		}
    		myCharz().setSkillPaint(sp, (!TileMap.tileTypeAt(myCharz().cx, myCharz().cy, 2)) ? 1 : 0);
    	}
    }
}

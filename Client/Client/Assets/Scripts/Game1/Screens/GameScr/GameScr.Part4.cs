namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameScr : mScreen, IChatable
    {
    
    	private bool checkClickToPopup(int xClick, int yClick)
    	{
    		for (int i = 0; i < PopUp.vPopups.size(); i++)
    		{
    			PopUp popUp = (PopUp)PopUp.vPopups.elementAt(i);
    			if (inRectangle(xClick, yClick, popUp.cx, popUp.cy, popUp.cw, popUp.ch))
    			{
    				if (popUp.cy <= 24 && TileMap.isInAirMap() && Char.myCharz().cTypePk != 0)
    				{
    					return false;
    				}
    				if (popUp.isPaint)
    				{
    					popUp.doClick(10);
    					return true;
    				}
    			}
    		}
    		return false;
    	}
    
    	private void checkClickMoveTo(int xClick, int yClick, int index)
    	{
    		if (gamePad.disableClickMove())
    		{
    			return;
    		}
    		Char.myCharz().cancelAttack();
    		if (xClick < TileMap.pxw && xClick > TileMap.pxw - 32)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(TileMap.pxw, yClick);
    			return;
    		}
    		if (xClick < 32 && xClick > 0)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(0, yClick);
    			return;
    		}
    		if (xClick < TileMap.pxw && xClick > TileMap.pxw - 48)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(TileMap.pxw, yClick);
    			return;
    		}
    		if (xClick < 48 && xClick > 0)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(0, yClick);
    			return;
    		}
    		clickToX = xClick;
    		clickToY = yClick;
    		clickOnTileTop = false;
    		Char.myCharz().delayFall = 0;
    		int num = ((!Char.myCharz().canFly || Char.myCharz().cMP <= 0) ? 1000 : 0);
    		if (clickToY > Char.myCharz().cy && Res.abs(clickToX - Char.myCharz().cx) < 12)
    		{
    			return;
    		}
    		for (int i = 0; i < 60 + num && clickToY + i < TileMap.pxh - 24; i += 24)
    		{
    			if (TileMap.tileTypeAt(clickToX, clickToY + i, 2))
    			{
    				clickToY = TileMap.tileYofPixel(clickToY + i);
    				clickOnTileTop = true;
    				break;
    			}
    		}
    		for (int j = 0; j < 40 + num; j += 24)
    		{
    			if (TileMap.tileTypeAt(clickToX, clickToY - j, 2))
    			{
    				clickToY = TileMap.tileYofPixel(clickToY - j);
    				clickOnTileTop = true;
    				break;
    			}
    		}
    		clickMoving = true;
    		clickMovingRed = false;
    		clickMovingP1 = ((!clickOnTileTop) ? 30 : ((yClick >= clickToY) ? clickToY : yClick));
    		Char.myCharz().delayFall = 0;
    		if (!clickOnTileTop && clickToY < Char.myCharz().cy - 50)
    		{
    			Char.myCharz().delayFall = 20;
    		}
    		clickMovingTimeOut = 30;
    		auto = 0;
    		if (Char.myCharz().holder)
    		{
    			Char.myCharz().removeHoleEff();
    		}
    		Char.myCharz().currentMovePoint = new MovePoint(clickToX, clickToY);
    		Char.myCharz().cdir = ((Char.myCharz().cx - Char.myCharz().currentMovePoint.xEnd <= 0) ? 1 : (-1));
    		Char.myCharz().endMovePointCommand = null;
    		isAutoPlay = false;
    	}
    
    	private void checkAuto()
    	{
    		long num = mSystem.currentTimeMillis();
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21] || GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23] || GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24] || GameCanvas.keyPressed[1] || GameCanvas.keyPressed[3])
    		{
    			auto = 0;
    			isAutoPlay = false;
    		}
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] && !isPaintPopup())
    		{
    			if (auto == 0)
    			{
    				if (num - lastFire < 800 && checkSkillValid2() && (Char.myCharz().mobFocus != null || (Char.myCharz().charFocus != null && Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus))))
    				{
    					Res.outz("toi day");
    					auto = 10;
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				}
    			}
    			else
    			{
    				auto = 0;
    				GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23] = (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24] = false);
    			}
    			lastFire = num;
    		}
    		if (GameCanvas.gameTick % 5 == 0 && auto > 0 && Char.myCharz().currentMovePoint == null)
    		{
    			if (Char.myCharz().myskill != null && (Char.myCharz().myskill.template.isUseAlone() || Char.myCharz().myskill.paintCanNotUseSkill))
    			{
    				return;
    			}
    			if ((Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.status != 1 && Char.myCharz().mobFocus.status != 0 && Char.myCharz().charFocus == null) || (Char.myCharz().charFocus != null && Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus)))
    			{
    				if (Char.myCharz().myskill != null && Char.myCharz().myskill.paintCanNotUseSkill)
    				{
    					return;
    				}
    				doFire(false, true);
    			}
    		}
    		if (auto > 1)
    		{
    			auto--;
    		}
    	}
    
    	public void doUseHP()
    	{
    		if (Char.myCharz().stone || Char.myCharz().blindEff || Char.myCharz().holdEffID > 0)
    		{
    			return;
    		}
    		long num = mSystem.currentTimeMillis();
    		if (num - lastUsePotion >= 10000)
    		{
    			if (!Char.myCharz().doUsePotion())
    			{
    				info1.addInfo(mResources.HP_EMPTY, 0);
    				return;
    			}
    			ServerEffect.addServerEffect(11, Char.myCharz(), 5);
    			ServerEffect.addServerEffect(104, Char.myCharz(), 4);
    			lastUsePotion = num;
    			SoundMn.gI().eatPeans();
    		}
    	}
    
    	public void activeSuperPower(int x, int y)
    	{
    		if (!isSuperPower)
    		{
    			SoundMn.gI().bigeExlode();
    			isSuperPower = true;
    			tPower = 0;
    			dxPower = 0;
    			xPower = x - cmx;
    			yPower = y - cmy;
    		}
    	}
    
    	public void activeRongThanEff(bool isMe)
    	{
    		activeRongThan = true;
    		isUseFreez = true;
    		isMeCallRongThan = true;
    		if (isMe)
    		{
    			Effect me = new Effect(20, Char.myCharz().cx, Char.myCharz().cy - 77, 2, 8, 1);
    			EffecMn.addEff(me);
    		}
    	}
    
    	public void hideRongThanEff()
    	{
    		activeRongThan = false;
    		isUseFreez = true;
    		isMeCallRongThan = false;
    	}
    
    	public void doiMauTroi()
    	{
    		isRongThanXuatHien = true;
    		mautroi = mGraphics.blendColor(0.4f, 0, GameCanvas.colorTop[GameCanvas.colorTop.Length - 1]);
    	}
    
    	public void callRongThan(int x, int y)
    	{
    		Res.outz("VE RONG THAN O VI TRI x= " + x + " y=" + y);
    		doiMauTroi();
    		Effect me = new Effect((!isRongNamek) ? 17 : 25, x, y - 77, 2, -1, 1);
    		EffecMn.addEff(me);
    	}
    
    	public void hideRongThan()
    	{
    		isRongThanXuatHien = false;
    		EffecMn.removeEff(17);
    		if (isRongNamek)
    		{
    			isRongNamek = false;
    			EffecMn.removeEff(25);
    		}
    	}
    
    	private void autoPlay()
    	{
    		if (timeSkill > 0)
    		{
    			timeSkill--;
    		}
    		if (!canAutoPlay || isChangeZone || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5 || Char.myCharz().isCharge || Char.myCharz().isFlyAndCharge || Char.myCharz().isUseChargeSkill())
    		{
    			return;
    		}
    		bool flag = false;
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			Mob mob = (Mob)vMob.elementAt(i);
    			if (mob.status != 0 && mob.status != 1)
    			{
    				flag = true;
    			}
    		}
    		if (!flag)
    		{
    			return;
    		}
    		bool flag2 = false;
    		for (int j = 0; j < Char.myCharz().arrItemBag.Length; j++)
    		{
    			Item item = Char.myCharz().arrItemBag[j];
    			if (item != null && item.template.type == 6)
    			{
    				flag2 = true;
    				break;
    			}
    		}
    		if (!flag2 && GameCanvas.gameTick % 150 == 0)
    		{
    			Service.gI().requestPean();
    		}
    		if (Char.myCharz().cHP <= Char.myCharz().cHPFull * 20 / 100 || Char.myCharz().cMP <= Char.myCharz().cMPFull * 20 / 100)
    		{
    			doUseHP();
    		}
    		if (Char.myCharz().mobFocus == null || (Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.isMobMe))
    		{
    			for (int k = 0; k < vMob.size(); k++)
    			{
    				Mob mob2 = (Mob)vMob.elementAt(k);
    				if (mob2.status != 0 && mob2.status != 1 && mob2.hp > 0 && !mob2.isMobMe)
    				{
    					int targetX = (mob2.xFirst != 0) ? mob2.xFirst : mob2.x;
    					int targetY = (mob2.yFirst != 0) ? mob2.yFirst : mob2.y;
    					Char.myCharz().cx = targetX;
    					Char.myCharz().cy = targetY;
    					Char.myCharz().mobFocus = mob2;
    					Service.gI().charMove();
    					break;
    				}
    			}
    		}
    		else if (Char.myCharz().mobFocus.hp <= 0 || Char.myCharz().mobFocus.status == 1 || Char.myCharz().mobFocus.status == 0)
    		{
    			Char.myCharz().mobFocus = null;
    		}
    		if (Char.myCharz().mobFocus == null || timeSkill != 0 || Char.myCharz().dart != null || Char.myCharz().arr != null || Char.myCharz().skillInfoPaint() != null)
    		{
    			return;
    		}
    		Skill skill = null;
    		if (GameCanvas.isTouch)
    		{
    			for (int l = 0; l < onScreenSkill.Length; l++)
    			{
    				if (onScreenSkill[l] == null || onScreenSkill[l].paintCanNotUseSkill || onScreenSkill[l].template.id == 10 || onScreenSkill[l].template.id == 11 || onScreenSkill[l].template.id == 14 || onScreenSkill[l].template.id == 23 || onScreenSkill[l].template.id == 7 || Char.myCharz().skillInfoPaint() != null || onScreenSkill[l].template.isSkillSpec())
    				{
    					continue;
    				}
    				long num = 0;
    				num = ((onScreenSkill[l].template.manaUseType == 2) ? 1 : ((onScreenSkill[l].template.manaUseType == 1) ? (onScreenSkill[l].manaUse * Char.myCharz().cMPFull / 100) : onScreenSkill[l].manaUse));
    				if (Char.myCharz().cMP >= num)
    				{
    					if (skill == null)
    					{
    						skill = onScreenSkill[l];
    					}
    					else if (skill.coolDown < onScreenSkill[l].coolDown)
    					{
    						skill = onScreenSkill[l];
    					}
    				}
    			}
    			if (skill != null)
    			{
    				doSelectSkill(skill, true);
    				doDoubleClickToObj(Char.myCharz().mobFocus);
    			}
    			return;
    		}
    		for (int m = 0; m < keySkill.Length; m++)
    		{
    			if (keySkill[m] == null || keySkill[m].paintCanNotUseSkill || keySkill[m].template.id == 10 || keySkill[m].template.id == 11 || keySkill[m].template.id == 14 || keySkill[m].template.id == 23 || keySkill[m].template.id == 7 || Char.myCharz().skillInfoPaint() != null)
    			{
    				continue;
    			}
    			long num2 = 0;
    			num2 = ((keySkill[m].template.manaUseType == 2) ? 1 : ((keySkill[m].template.manaUseType == 1) ? (keySkill[m].manaUse * Char.myCharz().cMPFull / 100) : keySkill[m].manaUse));
    			if (Char.myCharz().cMP >= num2)
    			{
    				if (skill == null)
    				{
    					skill = keySkill[m];
    				}
    				else if (skill.coolDown < keySkill[m].coolDown)
    				{
    					skill = keySkill[m];
    				}
    			}
    		}
    		if (skill != null)
    		{
    			doSelectSkill(skill, true);
    			doDoubleClickToObj(Char.myCharz().mobFocus);
    		}
    	}
    
    	private void doFire(bool isFireByShortCut, bool skipWaypoint)
    	{
    		tam++;
    		Waypoint waypoint = Char.myCharz().isInEnterOfflinePoint();
    		Waypoint waypoint2 = Char.myCharz().isInEnterOnlinePoint();
    		if (!skipWaypoint && waypoint != null && (Char.myCharz().mobFocus == null || (Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.templateId == 0)))
    		{
    			waypoint.popup.command.performAction();
    		}
    		else if (!skipWaypoint && waypoint2 != null && (Char.myCharz().mobFocus == null || (Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.templateId == 0)))
    		{
    			waypoint2.popup.command.performAction();
    		}
    		else
    		{
    			if ((TileMap.mapID == 51 && Char.myCharz().npcFocus != null) || Char.myCharz().statusMe == 14)
    			{
    				return;
    			}
    			Char.myCharz().cvx = (Char.myCharz().cvy = 0);
    			if (Char.myCharz().isSelectingSkillUseAlone() && Char.myCharz().focusToAttack())
    			{
    				if (checkSkillValid())
    				{
    					Char.myCharz().currentFireByShortcut = isFireByShortCut;
    					Char.myCharz().useSkillNotFocus();
    				}
    			}
    			else if (isAttack())
    			{
    				if (Char.myCharz().isUseChargeSkill() && Char.myCharz().focusToAttack())
    				{
    					if (checkSkillValid())
    					{
    						Char.myCharz().currentFireByShortcut = isFireByShortCut;
    						Char.myCharz().sendUseChargeSkill();
    					}
    					else
    					{
    						Char.myCharz().stopUseChargeSkill();
    					}
    				}
    				else
    				{
    					if (sks == null || sks.Length == 0)
    					{
    						readSkill();
    					}
    					bool flag = TileMap.tileTypeAt(Char.myCharz().cx, Char.myCharz().cy, 2);
    					SkillPaint skillPaint = null;
    					if (sks != null && Char.myCharz().myskill != null && Char.myCharz().myskill.skillId >= 0 && Char.myCharz().myskill.skillId < sks.Length)
    					{
    						skillPaint = sks[Char.myCharz().myskill.skillId];
    					}
    					if (skillPaint == null && sks != null && sks.Length > 0)
    					{
    						skillPaint = sks[0];
    					}
    					if (skillPaint == null)
    					{
    						skillPaint = new SkillPaint();
    						skillPaint.id = 0;
    					}
    					Char.myCharz().setSkillPaint(skillPaint, (!flag) ? 1 : 0);
    					if (flag)
    					{
    						Char.myCharz().delayFall = 20;
    					}
    					Char.myCharz().currentFireByShortcut = isFireByShortCut;
    				}
    			}
    			if (Char.myCharz().isSelectingSkillBuffToPlayer())
    			{
    				auto = 0;
    			}
    		}
    	}
    
    	private void askToPick()
    	{
    		Npc npc = new Npc(5, 0, -100, 100, 5, info1.charId[Char.myCharz().cgender][2]);
    		string nhatvatpham = mResources.nhatvatpham;
    		string[] menu = new string[2]
    		{
    			mResources.YES,
    			mResources.NO
    		};
    		npc.idItem = 673;
    		gI().createMenu(menu, npc);
    		ChatPopup.addChatPopupWithIcon(nhatvatpham, 100000, npc, 5820);
    	}
    
    	private void pickItem()
    	{
    		if (Char.myCharz().itemFocus == null)
    		{
    			return;
    		}
    		if (Char.myCharz().cx < Char.myCharz().itemFocus.x)
    		{
    			Char.myCharz().cdir = 1;
    		}
    		else
    		{
    			Char.myCharz().cdir = -1;
    		}
    		int num = Math.abs(Char.myCharz().cx - Char.myCharz().itemFocus.x);
    		int num2 = Math.abs(Char.myCharz().cy - Char.myCharz().itemFocus.y);
    		if (num <= 40 && num2 < 40)
    		{
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    			if (Char.myCharz().itemFocus.template.id != 673)
    			{
    				Service.gI().pickItem(Char.myCharz().itemFocus.itemMapID);
    			}
    			else
    			{
    				askToPick();
    			}
    		}
    		else
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().itemFocus.x, Char.myCharz().itemFocus.y);
    			Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    		}
    	}
    
    	public bool isCharging()
    	{
    		if (Char.myCharz().isFlyAndCharge || Char.myCharz().isUseSkillAfterCharge || Char.myCharz().isStandAndCharge || Char.myCharz().isWaitMonkey || isSuperPower || Char.myCharz().isFreez)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void doSelectSkill(Skill skill, bool isShortcut)
    	{
    		if (Char.myCharz().isCreateDark || isCharging() || Char.myCharz().taskMaint.taskId <= 1)
    		{
    			return;
    		}
    		Char.myCharz().myskill = skill;
    		if (lastSkill != skill && lastSkill != null)
    		{
    			Service.gI().selectSkill(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    			lastSkill = skill;
    			selectedIndexSkill = -1;
    			gI().auto = 0;
    			return;
    		}
    		if (Char.myCharz().isUseSkillSpec())
    		{
    			Res.outz(">>>use skill spec: " + skill.template.id);
    			Char.myCharz().sendNewAttack(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    			lastSkill = skill;
    			selectedIndexSkill = -1;
    			gI().auto = 0;
    			return;
    		}
    		if (Char.myCharz().isSelectingSkillUseAlone())
    		{
    			Res.outz("use skill not focus");
    			doUseSkillNotFocus(skill);
    			lastSkill = skill;
    			return;
    		}
    		selectedIndexSkill = -1;
    		if (skill == null)
    		{
    			return;
    		}
    		Res.outz("only select skill");
    		if (lastSkill != skill)
    		{
    			Service.gI().selectSkill(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    		}
    		if (Char.myCharz().charFocus != null || !Char.myCharz().isSelectingSkillBuffToPlayer())
    		{
    			if (Char.myCharz().focusToAttack())
    			{
    				doFire(isShortcut, true);
    				doSeleckSkillFlag = true;
    			}
    			lastSkill = skill;
    		}
    	}
    
    	public void doUseSkill(Skill skill, bool isShortcut)
    	{
    		if ((TileMap.mapID == 112 || TileMap.mapID == 113) && Char.myCharz().cTypePk == 0)
    		{
    			return;
    		}
    		if (Char.myCharz().isSelectingSkillUseAlone())
    		{
    			Res.outz("HERE");
    			doUseSkillNotFocus(skill);
    			return;
    		}
    		selectedIndexSkill = -1;
    		if (skill != null)
    		{
    			Service.gI().selectSkill(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    			Char.myCharz().myskill = skill;
    			doFire(isShortcut, true);
    		}
    	}
    
    	public void doUseSkillNotFocus(Skill skill)
    	{
    		if (((TileMap.mapID != 112 && TileMap.mapID != 113) || Char.myCharz().cTypePk != 0) && checkSkillValid())
    		{
    			selectedIndexSkill = -1;
    			if (skill != null)
    			{
    				Service.gI().selectSkill(skill.template.id);
    				saveRMSCurrentSkill(skill.template.id);
    				resetButton();
    				Char.myCharz().myskill = skill;
    				Char.myCharz().useSkillNotFocus();
    				Char.myCharz().currentFireByShortcut = true;
    				auto = 0;
    			}
    		}
    	}
    
    	public void sortSkill()
    	{
    		for (int i = 0; i < Char.myCharz().vSkillFight.size() - 1; i++)
    		{
    			Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    			for (int j = i + 1; j < Char.myCharz().vSkillFight.size(); j++)
    			{
    				Skill skill2 = (Skill)Char.myCharz().vSkillFight.elementAt(j);
    				if (skill2.template.id < skill.template.id)
    				{
    					Skill skill3 = skill2;
    					skill2 = skill;
    					skill = skill3;
    					Char.myCharz().vSkillFight.setElementAt(skill, i);
    					Char.myCharz().vSkillFight.setElementAt(skill2, j);
    				}
    			}
    		}
    	}
    
    	public void updateKeyTouchCapcha()
    	{
    		if (isNotPaintTouchControl())
    		{
    			return;
    		}
    		for (int i = 0; i < strCapcha.Length; i++)
    		{
    			keyCapcha[i] = -1;
    			if (!GameCanvas.isTouchControl)
    			{
    				continue;
    			}
    			int num = (GameCanvas.w - strCapcha.Length * disXC) / 2;
    			int w = strCapcha.Length * disXC;
    			int y = GameCanvas.h - 40;
    			int h = disXC;
    			if (!GameCanvas.isPointerHoldIn(num, y, w, h))
    			{
    				continue;
    			}
    			int num2 = (GameCanvas.px - num) / disXC;
    			if (i == num2)
    			{
    				keyCapcha[i] = 1;
    			}
    			if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease && i == num2)
    			{
    				char[] array = keyInput.ToCharArray();
    				MyVector myVector = new MyVector();
    				for (int j = 0; j < array.Length; j++)
    				{
    					myVector.addElement(array[j] + string.Empty);
    				}
    				myVector.removeElementAt(0);
    				myVector.insertElementAt(strCapcha[i] + string.Empty, myVector.size());
    				keyInput = string.Empty;
    				for (int k = 0; k < myVector.size(); k++)
    				{
    					keyInput += ((string)myVector.elementAt(k)).ToUpper();
    				}
    				Service.gI().mobCapcha(strCapcha[i]);
    			}
    		}
    	}
    
    	public bool checkClickToCapcha()
    	{
    		if (mobCapcha == null)
    		{
    			return false;
    		}
    		int x = (GameCanvas.w - 5 * disXC) / 2;
    		int w = 5 * disXC;
    		int y = GameCanvas.h - 40;
    		int h = disXC;
    		if (GameCanvas.isPointerHoldIn(x, y, w, h))
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void checkMouseChat()
    	{
    		if (GameCanvas.isMouseFocus(xC, yC, 34, 34))
    		{
    			if (!TileMap.isOfflineMap())
    			{
    				mScreen.keyMouse = 15;
    			}
    		}
    		else if (GameCanvas.isMouseFocus(xHP, yHP, 40, 40))
    		{
    			if (Char.myCharz().statusMe != 14)
    			{
    				mScreen.keyMouse = 10;
    			}
    		}
    		else if (GameCanvas.isMouseFocus(xF, yF, 40, 40))
    		{
    			if (Char.myCharz().statusMe != 14)
    			{
    				mScreen.keyMouse = 5;
    			}
    		}
    		else if (cmdMenu != null && GameCanvas.isMouseFocus(cmdMenu.x, cmdMenu.y, cmdMenu.w / 2, cmdMenu.h))
    		{
    			mScreen.keyMouse = 1;
    		}
    		else
    		{
    			mScreen.keyMouse = -1;
    		}
    	}
    
    	private void updateKeyTouchControl()
    	{
    		if (isNotPaintTouchControl())
    		{
    			return;
    		}
    		mScreen.keyTouch = -1;
    		if (GameCanvas.isTouchControl)
    		{
    			if (GameCanvas.isPointerHoldIn(0, 0, 60, 50) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    			{
    				if (Char.myCharz().cmdMenu != null)
    				{
    					Char.myCharz().cmdMenu.performAction();
    				}
    				Char.myCharz().currentMovePoint = null;
    				GameCanvas.clearAllPointerEvent();
    				flareFindFocus = true;
    				flareTime = 5;
    				return;
    			}
    			if (Main.isPC)
    			{
    				checkMouseChat();
    			}
    			if (!TileMap.isOfflineMap() && GameCanvas.isPointerHoldIn(xC, yC, 34, 34))
    			{
    				mScreen.keyTouch = 15;
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					ChatTextField.gI().startChat(this, string.Empty);
    					SoundMn.gI().buttonClick();
    					Char.myCharz().currentMovePoint = null;
    					GameCanvas.clearAllPointerEvent();
    					return;
    				}
    			}
    			if (Char.myCharz().cmdMenu != null && GameCanvas.isPointerHoldIn(Char.myCharz().cmdMenu.x - 17, Char.myCharz().cmdMenu.y - 17, 34, 34))
    			{
    				mScreen.keyTouch = 20;
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					GameCanvas.clearAllPointerEvent();
    					Char.myCharz().cmdMenu.performAction();
    					return;
    				}
    			}
    			updateGamePad();
    			if (((isAnalog != 0) ? GameCanvas.isPointerHoldIn(xHP, yHP + 10, 34, 34) : GameCanvas.isPointerHoldIn(xHP, yHP + 10, 40, 40)) && Char.myCharz().statusMe != 14 && mobCapcha == null)
    			{
    				mScreen.keyTouch = 10;
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					GameCanvas.keyPressed[10] = true;
    					GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    				}
    			}
    			if (((isAnalog != 0) ? GameCanvas.isPointerHoldIn(xHP + 5, yHP - 6 - 34 + 10, 34, 34) : GameCanvas.isPointerHoldIn(xHP + 5, yHP - 6 - 40 + 10, 40, 40)) && Char.myCharz().statusMe != 14 && mobCapcha == null)
    			{
    				if (isPickNgocRong)
    				{
    					mScreen.keyTouch = 14;
    					GameCanvas.isPointerJustDown = false;
    					isPointerDowning = false;
    					if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    					{
    						GameCanvas.keyPressed[14] = true;
    						GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    						isPickNgocRong = false;
    						Service.gI().useItem(-1, -1, -1, -1);
    					}
    				}
    				else if (isudungCapsun4)
    				{
    					mScreen.keyTouch = 14;
    					GameCanvas.isPointerJustDown = false;
    					isPointerDowning = false;
    					if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    					{
    						GameCanvas.keyPressed[14] = true;
    						GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    						for (int i = 0; i < Char.myCharz().arrItemBag.Length; i++)
    						{
    							Item item = Char.myCharz().arrItemBag[i];
    							if (item == null)
    							{
    								continue;
    							}
    							if (item.template.id == 194)
    							{
    								isudungCapsun4 = item.quantity > 0;
    								if (isudungCapsun4)
    								{
    									Service.gI().useItem(0, 1, (sbyte)i, -1);
    									break;
    								}
    							}
    						}
    					}
    				}
    				else if (isudungCapsun3)
    				{
    					mScreen.keyTouch = 14;
    					GameCanvas.isPointerJustDown = false;
    					isPointerDowning = false;
    					if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    					{
    						GameCanvas.keyPressed[14] = true;
    						GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    						for (int j = 0; j < Char.myCharz().arrItemBag.Length; j++)
    						{
    							Item item2 = Char.myCharz().arrItemBag[j];
    							if (item2 != null && item2.template.id == 193)
    							{
    								isudungCapsun3 = ((item2.quantity > 0) ? true : false);
    								if (isudungCapsun3)
    								{
    									Service.gI().useItem(0, 1, (sbyte)j, -1);
    									break;
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    		if (mobCapcha != null)
    		{
    			updateKeyTouchCapcha();
    		}
    		else if (isHaveSelectSkill)
    		{
    			if (isCharging())
    			{
    				return;
    			}
    			keyTouchSkill = -1;
    			bool flag = false;
    			if (onScreenSkill.Length > 5 && (GameCanvas.isPointerHoldIn(xSkill + xS[0] - wSkill / 2 + 12, yS[0] - wSkill / 2 + 12, 5 * wSkill, wSkill) || GameCanvas.isPointerHoldIn(xSkill + xS[5] - wSkill / 2 + 12, yS[5] - wSkill / 2 + 12, 5 * wSkill, wSkill)))
    			{
    				flag = true;
    			}
    			if (flag || GameCanvas.isPointerHoldIn(xSkill + xS[0] - wSkill / 2 + 12, yS[0] - wSkill / 2 + 12, 5 * wSkill, wSkill) || (!GameCanvas.isTouchControl && GameCanvas.isPointerHoldIn(xSkill + xS[0] - wSkill / 2 + 12, yS[0] - wSkill / 2 + 12, wSkill, onScreenSkill.Length * wSkill)))
    			{
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				int num = (GameCanvas.pxLast - (xSkill + xS[0] - wSkill / 2 + 12)) / wSkill;
    				if (flag && GameCanvas.pyLast < yS[0])
    				{
    					num += 5;
    				}
    				keyTouchSkill = num;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    					selectedIndexSkill = num;
    					if (indexSelect < 0)
    					{
    						indexSelect = 0;
    					}
    					if (!Main.isPC)
    					{
    						if (selectedIndexSkill > onScreenSkill.Length - 1)
    						{
    							selectedIndexSkill = onScreenSkill.Length - 1;
    						}
    					}
    					else if (selectedIndexSkill > keySkill.Length - 1)
    					{
    						selectedIndexSkill = keySkill.Length - 1;
    					}
    					Skill skill = null;
    					skill = (Main.isPC ? keySkill[selectedIndexSkill] : onScreenSkill[selectedIndexSkill]);
    					if (skill == null && Main.isPC && onScreenSkill != null && selectedIndexSkill < onScreenSkill.Length)
    					{
    						skill = onScreenSkill[selectedIndexSkill];
    					}
    					else if (skill == null && keySkill != null && selectedIndexSkill < keySkill.Length)
    					{
    						skill = keySkill[selectedIndexSkill];
    					}
    					Res.outz("[CLIENT_SKILL_CLICK] Slot: " + selectedIndexSkill + ", Main.isPC=" + Main.isPC + ", Skill: " + (skill != null ? (skill.template.name + " id=" + skill.template.id) : "NULL"));
    					if (skill != null)
    					{
    						doSelectSkill(skill, true);
    					}
    				}
    			}
    		}
    		if (GameCanvas.isPointerJustRelease)
    		{
    			if (GameCanvas.keyHold[1] || GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] || GameCanvas.keyHold[3] || GameCanvas.keyHold[(!Main.isPC) ? 4 : 23] || GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    			{
    				GameCanvas.isPointerJustRelease = false;
    			}
    			GameCanvas.keyHold[1] = false;
    			GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] = false;
    			GameCanvas.keyHold[3] = false;
    			GameCanvas.keyHold[(!Main.isPC) ? 4 : 23] = false;
    			GameCanvas.keyHold[(!Main.isPC) ? 6 : 24] = false;
    		}
    	}
    
    	public void setCharJumpAtt()
    	{
    		Char.myCharz().cvy = -10;
    		Char.myCharz().statusMe = 3;
    		Char.myCharz().cp1 = 0;
    	}
    
    	public void setCharJump(int cvx)
    	{
    		if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    		{
    			Service.gI().charMove();
    		}
    		Char.myCharz().cvy = -10;
    		Char.myCharz().cvx = cvx;
    		Char.myCharz().statusMe = 3;
    		Char.myCharz().cp1 = 0;
    	}
    
    	public void updateOpen()
    	{
    		if (isstarOpen)
    		{
    			if (moveUp > -3)
    			{
    				moveUp -= 4;
    			}
    			else
    			{
    				moveUp = -2;
    			}
    			if (moveDow < GameCanvas.h + 3)
    			{
    				moveDow += 4;
    			}
    			else
    			{
    				moveDow = GameCanvas.h + 2;
    			}
    			if (moveUp <= -2 && moveDow >= GameCanvas.h + 2)
    			{
    				isstarOpen = false;
    			}
    		}
    	}
    
    	public void initCreateCommand()
    	{
    	}
    
    	public void checkCharFocus()
    	{
    	}
    
    	public void updateXoSo()
    	{
    		if (tShow == 0)
    		{
    			return;
    		}
    		currXS = mSystem.currentTimeMillis();
    		if (currXS - lastXS > 1000)
    		{
    			lastXS = mSystem.currentTimeMillis();
    			secondXS++;
    		}
    		if (secondXS > 20)
    		{
    			for (int i = 0; i < winnumber.Length; i++)
    			{
    				randomNumber[i] = winnumber[i];
    			}
    			tShow--;
    			if (tShow == 0)
    			{
    				yourNumber = string.Empty;
    				info1.addInfo(strFinish, 0);
    				secondXS = 0;
    			}
    			return;
    		}
    		if (moveIndex > winnumber.Length - 1)
    		{
    			tShow--;
    			if (tShow == 0)
    			{
    				yourNumber = string.Empty;
    				info1.addInfo(strFinish, 0);
    			}
    			return;
    		}
    		if (moveIndex < randomNumber.Length)
    		{
    			if (tMove[moveIndex] == 15)
    			{
    				if (randomNumber[moveIndex] == winnumber[moveIndex] - 1)
    				{
    					delayMove[moveIndex] = 10;
    				}
    				if (randomNumber[moveIndex] == winnumber[moveIndex])
    				{
    					tMove[moveIndex] = -1;
    					moveIndex++;
    				}
    			}
    			else if (GameCanvas.gameTick % 5 == 0)
    			{
    				tMove[moveIndex]++;
    			}
    		}
    		for (int j = 0; j < winnumber.Length; j++)
    		{
    			if (tMove[j] == -1)
    			{
    				continue;
    			}
    			moveCount[j]++;
    			if (moveCount[j] > tMove[j] + delayMove[j])
    			{
    				moveCount[j] = 0;
    				randomNumber[j]++;
    				if (randomNumber[j] >= 10)
    				{
    					randomNumber[j] = 0;
    				}
    			}
    		}
    	}
    
	private static long lastLogCharUpdateTime;
	public static void logCharUpdateErrorThrottled(int charId, string charName, Exception ex)
	{
		long now = mSystem.currentTimeMillis();
		if (now - lastLogCharUpdateTime > 3000)
		{
			lastLogCharUpdateTime = now;
			Cout.LogError($"[GameScr.update] Lỗi cập nhật Char ID {charId} ('{charName}'): {ex.GetType().Name} - {ex.Message}\nStack: {ex.StackTrace}");
		}
	}

	private static long lastLogMobUpdateTime;
	public static void logMobUpdateErrorThrottled(int mobId, Exception ex)
	{
		long now = mSystem.currentTimeMillis();
		if (now - lastLogMobUpdateTime > 3000)
		{
			lastLogMobUpdateTime = now;
			Cout.LogError($"[GameScr.update] Lỗi cập nhật Mob ID {mobId}: {ex.GetType().Name} - {ex.Message}\nStack: {ex.StackTrace}");
		}
	}
    }
}

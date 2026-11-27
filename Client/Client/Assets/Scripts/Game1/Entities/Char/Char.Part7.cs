namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    
    public partial class Char : IMapObject
    {
    
    	public void doInjure(long HPShow, long MPShow, bool isCrit, bool isMob)
    	{
    		this.isCrit = isCrit;
    		this.isMob = isMob;
    		Res.outz("CHP= " + cHP + " dame -= " + HPShow + " HP FULL= " + cHPFull);
    		cHP -= HPShow;
    		cMP -= MPShow;
    		GameScr.gI().isInjureHp = true;
    		GameScr.gI().twHp = 0;
    		GameScr.gI().isInjureMp = true;
    		GameScr.gI().twMp = 0;
    		if (cHP < 0)
    		{
    			cHP = 0;
    		}
    		if (cMP < 0)
    		{
    			cMP = 0;
    		}
    		if (isMob || (!isMob && cTypePk != 4 && damMP != -100))
    		{
    			if (HPShow <= 0)
    			{
    				if (me)
    				{
    					GameScr.startFlyText(mResources.miss, cx, cy - ch, 0, -2, mFont.MISS_ME);
    				}
    				else
    				{
    					GameScr.startFlyText(mResources.miss, cx, cy - ch, 0, -2, mFont.MISS);
    				}
    			}
    			else
    			{
    				GameScr.startFlyText("-" + HPShow, cx, cy - ch, 0, -2, isCrit ? mFont.FATAL : mFont.RED);
    			}
    		}
    		if (HPShow > 0)
    		{
    			isInjure = 6;
    		}
    		ServerEffect.addServerEffect(80, this, 1);
    		if (isDie)
    		{
    			isDie = false;
    			isLockKey = false;
    			startDie((short)xSd, (short)ySd);
    		}
    	}
    
    	public void doInjure()
    	{
    		GameScr.gI().isInjureHp = true;
    		GameScr.gI().twHp = 0;
    		GameScr.gI().isInjureMp = true;
    		GameScr.gI().twMp = 0;
    		isInjure = 6;
    		ServerEffect.addServerEffect(8, this, 1);
    		isInjureHp = true;
    		twHp = 0;
    	}
    
    	public void startDie(short toX, short toY)
    	{
    		isMonkey = 0;
    		isWaitMonkey = false;
    		if (me && isDie)
    		{
    			return;
    		}
    		if (me)
    		{
    			isLockMove = true;
    			for (int i = 0; i < GameScr.vCharInMap.size(); i++)
    			{
    				Char @char = (Char)GameScr.vCharInMap.elementAt(i);
    				@char.killCharId = -9999;
    			}
    			if (GameCanvas.panel != null && GameCanvas.panel.cp != null)
    			{
    				GameCanvas.panel.cp = null;
    			}
    			if (GameCanvas.panel2 != null && GameCanvas.panel2.cp != null)
    			{
    				GameCanvas.panel2.cp = null;
    			}
    		}
    		statusMe = 5;
    		cp2 = toX;
    		cp3 = toY;
    		cp1 = 0;
    		cHP = 0;
    		testCharId = -9999;
    		killCharId = -9999;
    		if (me && myskill != null && myskill.template.id != 14)
    		{
    			stopUseChargeSkill();
    		}
    		cTypePk = 0;
    	}
    
    	public void waitToDie(short toX, short toY)
    	{
    		wdx = toX;
    		wdy = toY;
    	}
    
    	public void liveFromDead()
    	{
    		cHP = cHPFull;
    		cMP = cMPFull;
    		statusMe = 1;
    		cp1 = (cp2 = (cp3 = 0));
    		ServerEffect.addServerEffect(109, this, 2);
    		GameScr.gI().center = null;
    		GameScr.isHaveSelectSkill = true;
    	}
    
    	public bool doUsePotion()
    	{
    		if (arrItemBag == null)
    		{
    			return false;
    		}
    		for (int i = 0; i < arrItemBag.Length; i++)
    		{
    			if (arrItemBag[i] != null && arrItemBag[i].template.type == 6)
    			{
    				Service.gI().useItem(0, 1, -1, arrItemBag[i].template.id);
    				return true;
    			}
    		}
    		return false;
    	}
    
    	public bool isLang()
    	{
    		if (TileMap.mapID == 1 || TileMap.mapID == 27 || TileMap.mapID == 72 || TileMap.mapID == 10 || TileMap.mapID == 17 || TileMap.mapID == 22 || TileMap.mapID == 32 || TileMap.mapID == 38 || TileMap.mapID == 43 || TileMap.mapID == 48)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isMeCanAttackOtherPlayer(Char cAtt)
    	{
    		if (cAtt == null || myCharz().myskill == null || myCharz().myskill.template.type == 2 || (myCharz().myskill.template.type == 4 && cAtt.statusMe != 14 && cAtt.statusMe != 5))
    		{
    			return false;
    		}
    		return ((cAtt.cTypePk == 3 && myCharz().cTypePk == 3) || myCharz().cTypePk == 5 || cAtt.cTypePk == 5 || (myCharz().cTypePk == 1 && cAtt.cTypePk == 1) || (myCharz().cTypePk == 4 && cAtt.cTypePk == 4) || (myCharz().testCharId >= 0 && myCharz().testCharId == cAtt.charID) || (myCharz().killCharId >= 0 && myCharz().killCharId == cAtt.charID && !isLang()) || (cAtt.killCharId >= 0 && cAtt.killCharId == myCharz().charID && !isLang()) || (myCharz().cFlag == 8 && cAtt.cFlag != 0) || (myCharz().cFlag != 0 && cAtt.cFlag == 8) || (myCharz().cFlag != cAtt.cFlag && myCharz().cFlag != 0 && cAtt.cFlag != 0)) && cAtt.statusMe != 14 && cAtt.statusMe != 5;
    	}
    
    	public void clearTask()
    	{
    		myCharz().taskMaint = null;
    		for (int i = 0; i < myCharz().arrItemBag.Length; i++)
    		{
    			if (myCharz().arrItemBag[i] != null && myCharz().arrItemBag[i].template.type == 8)
    			{
    				myCharz().arrItemBag[i] = null;
    			}
    		}
    		Npc.clearEffTask();
    	}
    
    	public int getX()
    	{
    		return cx;
    	}
    
    	public int getY()
    	{
    		return cy;
    	}
    
    	public int getH()
    	{
    		return 32;
    	}
    
    	public int getW()
    	{
    		return 24;
    	}
    
    	public void focusManualTo(object objectz)
    	{
    		if (objectz is Mob)
    		{
    			mobFocus = (Mob)objectz;
    			deFocusNPC();
    			charFocus = null;
    			itemFocus = null;
    		}
    		else if (objectz is Npc)
    		{
    			myCharz().mobFocus = null;
    			myCharz().deFocusNPC();
    			myCharz().npcFocus = (Npc)objectz;
    			myCharz().charFocus = null;
    			myCharz().itemFocus = null;
    		}
    		else if (objectz is Char)
    		{
    			Char @char = (Char)objectz;
    			if (@char.isPetFollower || @char.charID <= -310720000 || @char.cName == "$" || (@char.isPet && string.IsNullOrEmpty(@char.cName)))
    			{
    				return;
    			}
    			myCharz().mobFocus = null;
    			myCharz().deFocusNPC();
    			myCharz().charFocus = @char;
    			myCharz().itemFocus = null;
    		}
    		else if (objectz is ItemMap)
    		{
    			myCharz().mobFocus = null;
    			myCharz().deFocusNPC();
    			myCharz().charFocus = null;
    			myCharz().itemFocus = (ItemMap)objectz;
    		}
    		isManualFocus = true;
    	}
    
    	public void stopMoving()
    	{
    	}
    
    	public void cancelAttack()
    	{
    	}
    
    	public bool isInvisible()
    	{
    		return false;
    	}
    
    	public bool focusToAttack()
    	{
    		return mobFocus != null || (charFocus != null && isMeCanAttackOtherPlayer(charFocus));
    	}
    
    	public void addDustEff(int type)
    	{
    		if (GameCanvas.lowGraphic)
    		{
    			return;
    		}
    		switch (type)
    		{
    		case 1:
    			if (clevel >= 9)
    			{
    				Effect effect3 = new Effect(19, cx - 5, cy + 20, 2, 1, -1);
    				EffecMn.addEff(effect3);
    			}
    			break;
    		case 2:
    			break;
    		case 3:
    			if (clevel >= 9 && ySd - cy <= 5)
    			{
    				Effect effect = new Effect(19, cx - 5, ySd + 20, 2, 1, -1);
    				EffecMn.addEff(effect);
    			}
    			break;
    		}
    	}
    
    	public bool isGetFlagImage(sbyte getFlag)
    	{
    		bool result = true;
    		for (int i = 0; i < GameScr.vFlag.size(); i++)
    		{
    			PKFlag pKFlag = (PKFlag)GameScr.vFlag.elementAt(i);
    			if (pKFlag != null)
    			{
    				if (pKFlag.cflag == getFlag)
    				{
    					return true;
    				}
    				result = false;
    			}
    		}
    		return result;
    	}
    
    	private void paintPKFlag(mGraphics g)
    	{
    		if (cdir == 1)
    		{
    			if (cFlag != 0 && cFlag != -1)
    			{
    				SmallImage.drawSmallImage(g, flagImage, cx - 10, cy - ch - ((!me) ? 30 : 30) + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), 2, 0);
    			}
    		}
    		else if (cFlag != 0 && cFlag != -1)
    		{
    			SmallImage.drawSmallImage(g, flagImage, cx, cy - ch - ((!me) ? 30 : 30) + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), 0, 0);
    		}
    	}
    
    	public void removeHoleEff()
    	{
    		if (holder)
    		{
    			holder = false;
    			charHold = null;
    			mobHold = null;
    		}
    		else
    		{
    			holdEffID = 0;
    			charHold = null;
    			mobHold = null;
    		}
    	}
    
    	public void removeProtectEff()
    	{
    		protectEff = false;
    		eProtect = null;
    	}
    
    	public void removeBlindEff()
    	{
    		blindEff = false;
    	}
    
    	public void removeEffect()
    	{
    		if (holdEffID != 0)
    		{
    			holdEffID = 0;
    		}
    		if (holder)
    		{
    			holder = false;
    		}
    		if (protectEff)
    		{
    			protectEff = false;
    		}
    		eProtect = null;
    		charHold = null;
    		mobHold = null;
    		blindEff = false;
    		sleepEff = false;
    	}
    
    	public void setPos(short xPos, short yPos, sbyte typePos)
    	{
    		isSetPos = true;
    		this.xPos = xPos;
    		this.yPos = yPos;
    		this.typePos = typePos;
    		tpos = 0;
    		if (me)
    		{
    			if (GameCanvas.panel != null)
    			{
    				GameCanvas.panel.hide();
    			}
    			if (GameCanvas.panel2 != null)
    			{
    				GameCanvas.panel2.hide();
    			}
    		}
    	}
    
    	public void removeHuytSao()
    	{
    		huytSao = false;
    	}
    
    	public void fusionComplete()
    	{
    		isFusion = false;
    		isLockKey = false;
    		tFusion = 0;
    	}
    
    	public void setFusion(sbyte fusion)
    	{
    		tFusion = 0;
    		if (fusion == 4 || fusion == 5)
    		{
    			if (me)
    			{
    				Service.gI().funsion(fusion);
    			}
    			EffecMn.addEff(new Effect(34, cx, cy + 12, 2, 1, -1));
    		}
    		if (fusion == 6)
    		{
    			EffecMn.addEff(new Effect(38, cx, cy + 12, 2, 1, -1));
    		}
    		if (me)
    		{
    			GameCanvas.panel.hideNow();
    			isLockKey = true;
    		}
    		isFusion = true;
    		if (fusion == 1)
    		{
    			isNhapThe = false;
    		}
    		else
    		{
    			isNhapThe = true;
    		}
    	}
    
    	public void removeSleepEff()
    	{
    		sleepEff = false;
    	}
    
    	public void setPartOld()
    	{
    		headTemp = head;
    		bodyTemp = body;
    		legTemp = leg;
    		bagTemp = bag;
    	}
    
    	public void setPartTemp(int head, int body, int leg, int bag)
    	{
    		if (head != -1)
    		{
    			this.head = head;
    		}
    		if (body != -1)
    		{
    			this.body = body;
    		}
    		if (leg != -1)
    		{
    			this.leg = leg;
    		}
    		if (bag != -1)
    		{
    			this.bag = bag;
    		}
    	}
    
    	public void resetPartTemp()
    	{
    		if (headTemp != -1)
    		{
    			head = headTemp;
    			headTemp = -1;
    		}
    		if (bodyTemp != -1)
    		{
    			body = bodyTemp;
    			bodyTemp = -1;
    		}
    		if (legTemp != -1)
    		{
    			leg = legTemp;
    			legTemp = -1;
    		}
    		if (bagTemp != -1)
    		{
    			bag = bagTemp;
    			bagTemp = -1;
    		}
    	}
    
    	public Effect getEffById(int id)
    	{
    		for (int i = 0; i < vEffChar.size(); i++)
    		{
    			Effect effect = (Effect)vEffChar.elementAt(i);
    			if (effect.effId == id)
    			{
    				return effect;
    			}
    		}
    		return null;
    	}
    
    	public void addEffChar(Effect e)
    	{
    		removeEffChar(0, e.effId);
    		vEffChar.addElement(e);
    	}
    
    	public void removeEffChar(int type, int id)
    	{
    		if (type == -1)
    		{
    			vEffChar.removeAllElements();
    		}
    		else if (getEffById(id) != null)
    		{
    			vEffChar.removeElement(getEffById(id));
    		}
    	}
    
    	public void paintEffBehind(mGraphics g)
    	{
    		for (int i = 0; i < vEffChar.size(); i++)
    		{
    			Effect effect = (Effect)vEffChar.elementAt(i);
    			if (effect.layer == 0)
    			{
    				bool flag = true;
    				if (effect.isStand == 0)
    				{
    					flag = ((statusMe == 1 || statusMe == 6) ? true : false);
    				}
    				if (flag)
    				{
    					effect.paint(g);
    				}
    			}
    		}
    	}
    
    	public void paintEffFront(mGraphics g)
    	{
    		for (int i = 0; i < vEffChar.size(); i++)
    		{
    			Effect effect = (Effect)vEffChar.elementAt(i);
    			if (effect.layer == 1)
    			{
    				bool flag = true;
    				if (effect.isStand == 0)
    				{
    					flag = ((statusMe == 1 || statusMe == 6) ? true : false);
    				}
    				if (flag)
    				{
    					effect.paint(g);
    				}
    			}
    		}
    	}
    
    	public void updEffChar()
    	{
    		for (int i = 0; i < vEffChar.size(); i++)
    		{
    			((Effect)vEffChar.elementAt(i)).update();
    		}
    	}
    
    	public int checkLuong()
    	{
    		return luong + luongKhoa;
    	}
    
    	public void updateEye()
    	{
    		if (head != 934)
    		{
    			return;
    		}
    		if (GameCanvas.timeNow - timeAddChopmat > 0)
    		{
    			fChopmat++;
    			if (fChopmat > frEye.Length - 1)
    			{
    				fChopmat = 0;
    				timeAddChopmat = GameCanvas.timeNow + Res.random(2000, 3500);
    				frEye = frChopCham;
    				if (Res.random(2) == 0)
    				{
    					frEye = frChopNhanh;
    				}
    			}
    		}
    		else
    		{
    			fChopmat = 0;
    		}
    	}
    
    	private void paintRedEye(mGraphics g, int xx, int yy, int trans, int anchor)
    	{
    		if (head != 934 || (statusMe != 1 && statusMe != 6))
    		{
    			return;
    		}
    		if (fraRedEye == null || fraRedEye.imgFrame == null)
    		{
    			Image img = mSystem.loadImage("/redeye.png");
    			fraRedEye = new FrameImage(img, 14, 10);
    		}
    		else if (frEye[fChopmat] != -1)
    		{
    			int num = 8;
    			int num2 = 15;
    			if (trans == 2)
    			{
    				num = -8;
    			}
    			fraRedEye.drawFrame(frEye[fChopmat], xx + num, yy + num2, trans, anchor, g);
    		}
    	}
    
    	public bool isHead_2Fr(int idHead)
    	{
    		for (int i = 0; i < Arr_Head_2Fr.Length; i++)
    		{
    			if (Arr_Head_2Fr[i][0] == idHead)
    			{
    				return true;
    			}
    		}
    		return false;
    	}
    
    	private void updateFHead()
    	{
    		if (isHead_2Fr(head))
    		{
    			fHead++;
    			if (fHead > 10000)
    			{
    				fHead = 0;
    			}
    		}
    		else
    		{
    			fHead = 0;
    		}
    	}
    
    	private int getFHead(int idHead)
    	{
    		for (int i = 0; i < Arr_Head_2Fr.Length; i++)
    		{
    			if (Arr_Head_2Fr[i][0] == idHead)
    			{
    				return Arr_Head_2Fr[i][fHead / 4 % Arr_Head_2Fr[i].Length];
    			}
    		}
    		return idHead;
    	}
    
    	public void paintAuraBehind(mGraphics g)
    	{
    		if ((!me || isPaintAura) && idAuraEff > -1 && (statusMe == 1 || statusMe == 6) && !GameCanvas.panel.isShow && mSystem.currentTimeMillis() - timeBlue > 0)
    		{
    			string nameImg = strEffAura + idAuraEff + "_0";
    			FrameImage fraImage = mSystem.getFraImage(nameImg);
    			if (fraImage != null)
    			{
    				fraImage.drawFrame(GameCanvas.gameTick / 4 % fraImage.nFrame, cx, cy, (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    			}
    		}
    	}
    
    	public void paintAuraFront(mGraphics g)
    	{
    		if ((me && !isPaintAura) || idAuraEff <= -1)
    		{
    			return;
    		}
    		if (statusMe == 1 || statusMe == 6)
    		{
    			if (GameCanvas.panel.isShow || GameCanvas.lowGraphic)
    			{
    				return;
    			}
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
    			if (mSystem.currentTimeMillis() - timeBlue > 0)
    			{
    				string nameImg = strEffAura + idAuraEff + "_1";
    				FrameImage fraImage = mSystem.getFraImage(nameImg);
    				if (fraImage != null)
    				{
    					fraImage.drawFrame(GameCanvas.gameTick / 4 % fraImage.nFrame, cx, cy + 2, (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
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
    
    	public void paintEff_Lvup_behind(mGraphics g)
    	{
    		if (idEff_Set_Item != -1)
    		{
    			if (fraEff != null)
    			{
    				fraEff.drawFrame(GameCanvas.gameTick / 4 % fraEff.nFrame, cx, cy + 3, (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    			}
    			else
    			{
    				fraEff = mSystem.getFraImage(strEff_Set_Item + idEff_Set_Item + "_0");
    			}
    		}
    	}
    
    	public void paintEff_Lvup_front(mGraphics g)
    	{
    		if (idEff_Set_Item != -1)
    		{
    			if (fraEffSub != null)
    			{
    				fraEffSub.drawFrame(GameCanvas.gameTick / 4 % fraEffSub.nFrame, cx, cy + 8, (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    			}
    			else
    			{
    				fraEffSub = mSystem.getFraImage(strEff_Set_Item + idEff_Set_Item + "_1");
    			}
    		}
    	}
    
    	public void paintHat_behind(mGraphics g, int cf, int yh)
    	{
    		try
    		{
    			if (idHat == -1)
    			{
    				return;
    			}
    			if (isFrNgang(cf))
    			{
    				if (fraHat_behind_2 != null)
    				{
    					fraHat_behind_2.drawFrame(GameCanvas.gameTick / 4 % fraHat_behind_2.nFrame, cx + hatInfo[cf][0] * ((cdir == 1) ? 1 : (-1)), yh + hatInfo[cf][1], (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    				}
    				else
    				{
    					fraHat_behind_2 = mSystem.getFraImage(strHat_behind + strNgang + idHat);
    				}
    			}
    			else if (fraHat_behind != null)
    			{
    				fraHat_behind.drawFrame(GameCanvas.gameTick / 4 % fraHat_behind.nFrame, cx + hatInfo[cf][0] * ((cdir == 1) ? 1 : (-1)), yh + hatInfo[cf][1], (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    			}
    			else
    			{
    				fraHat_behind = mSystem.getFraImage(strHat_behind + idHat);
    			}
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public void paintHat_front(mGraphics g, int cf, int yh)
    	{
    		try
    		{
    			if (idHat == -1)
    			{
    				return;
    			}
    			if (isFrNgang(cf))
    			{
    				if (fraHat_font_2 != null)
    				{
    					fraHat_font_2.drawFrame(GameCanvas.gameTick / 4 % fraHat_font_2.nFrame, cx + hatInfo[cf][0] * ((cdir == 1) ? 1 : (-1)), yh + hatInfo[cf][1], (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    				}
    				else
    				{
    					fraHat_font_2 = mSystem.getFraImage(strHat_font + strNgang + idHat);
    				}
    			}
    			else if (fraHat_font != null)
    			{
    				fraHat_font.drawFrame(GameCanvas.gameTick / 4 % fraHat_font.nFrame, cx + hatInfo[cf][0] * ((cdir == 1) ? 1 : (-1)), yh + hatInfo[cf][1], (cdir != 1) ? 2 : 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    			}
    			else
    			{
    				fraHat_font = mSystem.getFraImage(strHat_font + idHat);
    			}
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public bool isFrNgang(int fr)
    	{
    		if (fr == 2 || fr == 3 || fr == 4 || fr == 5 || fr == 6 || fr == 9 || fr == 10 || fr == 13 || fr == 14 || fr == 15 || fr == 16 || fr == 26 || fr == 27 || fr == 28 || fr == 29)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void sendNewAttack(short idTemplateSkill)
    	{
    		short x = -1;
    		short y = -1;
    		if (mobFocus != null)
    		{
    			x = (short)mobFocus.x;
    			y = (short)mobFocus.y;
    		}
    		if (charFocus != null && !charFocus.isPet && !charFocus.isMiniPet)
    		{
    			x = (short)charFocus.cx;
    			y = (short)charFocus.cy;
    		}
    		Service.gI().new_skill_not_focus((sbyte)idTemplateSkill, (sbyte)cdir, x, y);
    	}
    
    	public void SetSkillPaint_NEW(short idskillPaint, bool isFly, sbyte typeFrame, sbyte typePaint, sbyte dir, short timeGong, sbyte typeItem)
    	{
    		isPaintNewSkill = true;
    		timeReset_newSkill = GameCanvas.timeNow + 10000;
    		this.idskillPaint = idskillPaint;
    		this.isFly = isFly;
    		this.typeFrame = typeFrame;
    		this.typePaint = typePaint;
    		this.typeItem = typeItem;
    		cdir = dir;
    		count_NEW = 0;
    		stt = 0;
    		long lastTimeUseThisSkill = mSystem.currentTimeMillis();
    		if (me)
    		{
    			saveLoadPreviousSkill();
    			myskill.lastTimeUseThisSkill = lastTimeUseThisSkill;
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
    		switch (idskillPaint)
    		{
    		case 24:
    			GameScr.addEffectEnd_Target(18, 0, typePaint, clone(), null, 3, timeGong, 0);
    			GameScr.addEffectEnd_Target(21, 0, typePaint, clone(), null, 1, timeGong, 0);
    			break;
    		case 25:
    			GameScr.addEffectEnd_Target(19, 0, typePaint, clone(), null, 3, timeGong, 0);
    			GameScr.addEffectEnd_Target(22, 0, typePaint, clone(), null, 1, timeGong, 0);
    			break;
    		case 26:
    			GameScr.addEffectEnd_Target(20, 0, typePaint, clone(), null, 3, timeGong, 0);
    			GameScr.addEffectEnd_Target(23, 0, typePaint, clone(), null, 1, timeGong, 0);
    			break;
    		}
    		if (this.typeFrame == 1)
    		{
    			if (!this.isFly)
    			{
    				fr_start = new byte[7] { 20, 20, 20, 20, 20, 20, 19 };
    				fr_atk = new byte[1] { 20 };
    				fr_end = new byte[1];
    			}
    			else
    			{
    				fr_start = new byte[7] { 31, 31, 31, 31, 31, 31, 30 };
    				fr_atk = new byte[1] { 31 };
    				fr_end = new byte[1] { 12 };
    			}
    		}
    		if (this.typeFrame == 2)
    		{
    			if (!this.isFly)
    			{
    				fr_start = new byte[1] { 20 };
    				fr_atk = new byte[6] { 13, 13, 13, 14, 14, 14 };
    				fr_end = new byte[1];
    			}
    			else
    			{
    				fr_start = new byte[1] { 31 };
    				fr_atk = new byte[6] { 26, 26, 26, 27, 27, 27 };
    				fr_end = new byte[1] { 12 };
    			}
    		}
    		if (this.typeFrame == 4)
    		{
    			if (!this.isFly)
    			{
    				fr_start = new byte[6] { 17, 17, 17, 18, 18, 18 };
    				fr_atk = new byte[1] { 18 };
    				fr_end = new byte[1];
    			}
    			else
    			{
    				fr_start = new byte[7] { 7, 7, 7, 12, 12, 12, 12 };
    				fr_atk = new byte[1] { 12 };
    				fr_end = new byte[1] { 12 };
    			}
    		}
    		if (this.typeFrame == 3)
    		{
    			if (!this.isFly)
    			{
    				fr_start = new byte[9] { 24, 24, 24, 17, 17, 17, 18, 18, 18 };
    				fr_atk = new byte[1] { 20 };
    				fr_end = new byte[1];
    			}
    			else
    			{
    				fr_start = new byte[10] { 23, 23, 23, 7, 7, 7, 12, 12, 12, 12 };
    				fr_atk = new byte[1] { 31 };
    				fr_end = new byte[1] { 12 };
    			}
    		}
    	}
    
    	public void SetSkillPaint_STT(int stt, short idskillPaint, Point targetDame, short timeDame, short rangeDame, sbyte typePaint, Point[] listObj, sbyte typeItem)
    	{
    		this.stt = stt;
    		this.idskillPaint = idskillPaint;
    		count_NEW = 0;
    		this.targetDame = targetDame;
    		this.typePaint = typePaint;
    		this.timeDame = mSystem.currentTimeMillis() + timeDame;
    		this.rangeDame = rangeDame;
    		this.typeItem = typeItem;
    		if (this.stt == 1)
    		{
    			if (this.idskillPaint == 24)
    			{
    				GameScr.addEffectEnd_Target(18, 1, typePaint, this, null, 3, timeDame, 0);
    				GameScr.addEffectEnd_Target(24, 0, typePaint, this, this.targetDame, 1, timeDame, rangeDame);
    			}
    			if (this.idskillPaint == 25)
    			{
    				GameScr.addEffectEnd_Target(19, 0, typePaint, this, null, 3, timeDame, 0);
    				GameScr.addEffectEnd_Target(25, 0, typePaint, this, this.targetDame, 1, timeDame, rangeDame);
    			}
    			if (this.idskillPaint == 26)
    			{
    				GameScr.addEffectEnd_Target(20, 0, typePaint, this, null, 3, timeDame, 0);
    				GameScr.addEffectEnd(26, typeItem, typePaint, targetDame.x, targetDame.y, 1, 0, timeDame, listObj);
    			}
    		}
    	}
    
    	public void UpdSkillPaint_NEW()
    	{
    		if (stt == 0)
    		{
    			if (isFly && count_NEW < 20)
    			{
    				cvy = -3;
    				cy += cvy;
    			}
    			if (fr_start.Length == 1)
    			{
    				cf = fr_start[0];
    			}
    			else if (count_NEW > fr_start.Length - 1)
    			{
    				cf = fr_start[fr_start.Length - 1];
    			}
    			else
    			{
    				cf = fr_start[count_NEW];
    			}
    		}
    		else if (stt == 1)
    		{
    			cf = fr_atk[count_NEW % fr_atk.Length];
    			if (mSystem.currentTimeMillis() - timeDame > 0)
    			{
    				SetSkillPaint_STT(2, 0, null, 0, 0, 0, null, 0);
    			}
    			if (count_NEW % 5 == 0)
    			{
    				GameScr.shock_scr = 5;
    			}
    			if (typeFrame == 1 && count_NEW < 10 && !TileMap.tileTypeAt(cx - (chw + 1) * cdir, cy, (cdir != 1) ? 4 : 8))
    			{
    				cx -= cdir;
    			}
    			if (typeFrame != 2)
    			{
    			}
    		}
    		else if (stt == 2)
    		{
    			if (fr_end.Length == 1)
    			{
    				cf = fr_end[0];
    			}
    			else if (count_NEW > fr_end.Length - 1)
    			{
    				cf = fr_end[fr_end.Length - 1];
    			}
    			else
    			{
    				cf = fr_end[count_NEW];
    			}
    			if (isFly)
    			{
    				cvx = (cvy = 0);
    				statusMe = 4;
    			}
    			isPaintNewSkill = false;
    		}
    		count_NEW++;
    	}
    
    	public Char clone()
    	{
    		Char @char = new Char();
    		@char.charID = charID;
    		@char.cx = cx;
    		@char.cy = cy;
    		@char.cdir = cdir;
    		@char.chw = chw;
    		@char.chh = chh;
    		@char.arrItemBody = arrItemBody;
    		return @char;
    	}
    
    	public bool containsCaiTrang(int v)
    	{
    		if (arrItemBody != null)
    		{
    			for (int i = 0; i < arrItemBody.Length; i++)
    			{
    				if (arrItemBody[i] != null && arrItemBody[i].template != null && arrItemBody[i].template.id == v)
    				{
    					return true;
    				}
    			}
    		}
    		Res.err("tim kiem id cai trang " + v + " ko tim thay");
    		return false;
    	}
    }
}

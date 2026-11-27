namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameScr : mScreen, IChatable
    {

    	public override void update()
        {
            ClientManager.getInstance().Update();
            if (GameCanvas.keyPressed[16])
    		{
    			GameCanvas.keyPressed[16] = false;
    			Char.myCharz().findNextFocusByKey();
    		}
    		if (GameCanvas.keyPressed[13] && !GameCanvas.panel.isShow)
    		{
    			GameCanvas.keyPressed[13] = false;
    			Char.myCharz().findNextFocusByKey();
    		}
    		if (GameCanvas.keyPressed[17])
    		{
    			GameCanvas.keyPressed[17] = false;
    			Char.myCharz().searchItem();
    			if (Char.myCharz().itemFocus != null)
    			{
    				pickItem();
    			}
    		}
    		if (GameCanvas.gameTick % 100 == 0 && TileMap.mapID == 137)
    		{
    			shock_scr = 30;
    		}
    		if (isAutoPlay && GameCanvas.gameTick % 20 == 0)
    		{
    			autoPlay();
    		}
    		updateXoSo();
    		mSystem.checkAdComlete();
    		SmallImage.update();
    		try
    		{
    			if (LoginScr.isContinueToLogin)
    			{
    				LoginScr.isContinueToLogin = false;
    			}
    			if (tickMove == 1)
    			{
    				lastTick = mSystem.currentTimeMillis();
    			}
    			if (tickMove == 100)
    			{
    				tickMove = 0;
    				currTick = mSystem.currentTimeMillis();
    				int second = (int)(currTick - lastTick) / 1000;
    				Service.gI().checkMMove(second);
    			}
    			if (lockTick > 0)
    			{
    				lockTick--;
    				if (lockTick == 0)
    				{
    					Controller.isStopReadMessage = false;
    				}
    			}
    			checkCharFocus();
    			GameCanvas.debug("E1", 0);
    			updateCamera();
    			GameCanvas.debug("E2", 0);
    			ChatTextField.gI().update();
    			GameCanvas.debug("E3", 0);
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				Char c = (Char)vCharInMap.elementAt(i);
    				if (c != null)
    				{
    					try { c.update(); } catch (Exception ex) { logCharUpdateErrorThrottled(c.charID, c.cName, ex); }
    				}
    			}
    			for (int i = 0; i < Teleport.vTeleport.size(); i++)
    			{
    				Teleport tp = (Teleport)Teleport.vTeleport.elementAt(i);
    				if (tp != null)
    				{
    					try { tp.update(); } catch (Exception) { }
    				}
    			}
    			try
    			{
    				if (Char.myCharz() != null)
    				{
    					Char.myCharz().update();
    				}
    			}
    			catch (Exception ex)
    			{
    				logCharUpdateErrorThrottled(Char.myCharz() != null ? Char.myCharz().charID : -1, Char.myCharz() != null ? Char.myCharz().cName : "myCharz", ex);
    			}
    			if (popUpYesNo != null)
    			{
    				try { popUpYesNo.update(); } catch (Exception) { }
    			}
    			EffecMn.update();
    			GameCanvas.debug("E5x", 0);
    			for (int i = 0; i < vMob.size(); i++)
    			{
    				Mob mob = (Mob)vMob.elementAt(i);
    				if (mob != null)
    				{
    					try { mob.update(); } catch (Exception ex) { logMobUpdateErrorThrottled(mob.mobId, ex); }
    				}
    			}
    			GameCanvas.debug("E6", 0);
    			for (int i = 0; i < vNpc.size(); i++)
    			{
    				Npc npc = (Npc)vNpc.elementAt(i);
    				if (npc != null)
    				{
    					try { npc.update(); } catch (Exception) { }
    				}
    			}
    			nSkill = onScreenSkill.Length;
    			for (int i = onScreenSkill.Length - 1; i >= 0; i--)
    			{
    				Skill skill = onScreenSkill[i];
    				if (skill != null)
    				{
    					nSkill = i + 1;
    					break;
    				}
    				nSkill--;
    			}
    			setSkillBarPosition();
    			GameCanvas.debug("E7", 0);
    			GameCanvas.gI().updateDust();
    			GameCanvas.debug("E8", 0);
    			updateFlyText();
    			PopUp.updateAll();
    			updateSplash();
    			updateSS();
    			GameCanvas.updateBG();
    			GameCanvas.debug("E9", 0);
    			updateClickToArrow();
    			GameCanvas.debug("E10", 0);
    			for (int i = 0; i < vItemMap.size(); i++)
    			{
    				ItemMap item = (ItemMap)vItemMap.elementAt(i);
    				if (item != null)
    				{
    					try { item.update(); } catch (Exception) { }
    				}
    			}
    			GameCanvas.debug("E11", 0);
    			GameCanvas.debug("E13", 0);
    			for (int i = Effect2.vRemoveEffect2.size() - 1; i >= 0; i--)
    			{
    				Effect2.vEffect2.removeElement(Effect2.vRemoveEffect2.elementAt(i));
    				Effect2.vRemoveEffect2.removeElementAt(i);
    			}
    			for (int i = 0; i < Effect2.vEffect2.size(); i++)
    			{
    				Effect2 effect = (Effect2)Effect2.vEffect2.elementAt(i);
    				effect.update();
    			}
    			for (int i = 0; i < Effect2.vEffect2Outside.size(); i++)
    			{
    				Effect2 effect2 = (Effect2)Effect2.vEffect2Outside.elementAt(i);
    				effect2.update();
    			}
    			for (int i = 0; i < Effect2.vAnimateEffect.size(); i++)
    			{
    				Effect2 effect3 = (Effect2)Effect2.vAnimateEffect.elementAt(i);
    				effect3.update();
    			}
    			for (int i = 0; i < Effect2.vEffectFeet.size(); i++)
    			{
    				Effect2 effect4 = (Effect2)Effect2.vEffectFeet.elementAt(i);
    				effect4.update();
    			}
    			for (int i = 0; i < Effect2.vEffect3.size(); i++)
    			{
    				Effect2 effect5 = (Effect2)Effect2.vEffect3.elementAt(i);
    				effect5.update();
    			}
    			BackgroudEffect.updateEff();
    			info1.update();
    			info2.update();
    			GameCanvas.debug("E15", 0);
    			if (currentCharViewInfo != null && !currentCharViewInfo.Equals(Char.myCharz()))
    			{
    				currentCharViewInfo.update();
    			}
    			runArrow++;
    			if (runArrow > 3)
    			{
    				runArrow = 0;
    			}
    			if (isInjureHp)
    			{
    				twHp++;
    				if (twHp == 20)
    				{
    					twHp = 0;
    					isInjureHp = false;
    				}
    			}
    			else if (dHP > Char.myCharz().cHP)
    			{
    				long num = (dHP - Char.myCharz().cHP) >> 1;
    				if (num < 1)
    				{
    					num = 1;
    				}
    				dHP -= num;
    			}
    			else
    			{
    				dHP = Char.myCharz().cHP;
    			}
    			if (isInjureMp)
    			{
    				twMp++;
    				if (twMp == 20)
    				{
    					twMp = 0;
    					isInjureMp = false;
    				}
    			}
    			else if (dMP > Char.myCharz().cMP)
    			{
    				long num2 = (dMP - Char.myCharz().cMP) >> 1;
    				if (num2 < 1)
    				{
    					num2 = 1;
    				}
    				dMP -= num2;
    			}
    			else
    			{
    				dMP = Char.myCharz().cMP;
    			}
    			if (tMenuDelay > 0)
    			{
    				tMenuDelay--;
    			}
    			if (isRongThanMenu())
    			{
    				int num3 = 100;
    				while (yR - num3 < cmy)
    				{
    					cmy--;
    				}
    			}
    			for (int i = 0; i < Char.vItemTime.size(); i++)
    			{
    				((ItemTime)Char.vItemTime.elementAt(i)).update();
    			}
    			ItemTime.checkAutoUsePeriodic();
    			for (int i = 0; i < textTime.size(); i++)
    			{
    				((ItemTime)textTime.elementAt(i)).update();
    			}
    			updateChatVip();
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi GameScr.update: " + ex.Message);
    		}
    		int num4 = GameCanvas.gameTick % 4000;
    		if (num4 == 1000)
    		{
    			checkRemoveImage();
    		}
    		EffectManager.update();
    	}
    
    	public void updateKeyChatPopUp()
    	{
    	}
    
    	public bool isRongThanMenu()
    	{
    		if (isMeCallRongThan)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void paintEffect(mGraphics g)
    	{
    		for (int i = 0; i < Effect2.vEffect2.size(); i++)
    		{
    			Effect2 effect = (Effect2)Effect2.vEffect2.elementAt(i);
    			if (effect != null && !(effect is ChatPopup))
    			{
    				effect.paint(g);
    			}
    		}
    		if (!GameCanvas.lowGraphic)
    		{
    			for (int i = 0; i < Effect2.vAnimateEffect.size(); i++)
    			{
    				Effect2 effect2 = (Effect2)Effect2.vAnimateEffect.elementAt(i);
    				effect2.paint(g);
    			}
    		}
    		for (int i = 0; i < Effect2.vEffect2Outside.size(); i++)
    		{
    			Effect2 effect3 = (Effect2)Effect2.vEffect2Outside.elementAt(i);
    			effect3.paint(g);
    		}
    	}
    
    	public void paintBgItem(mGraphics g, int layer)
    	{
    		for (int i = 0; i < TileMap.vCurrItem.size(); i++)
    		{
    			BgItem bgItem = (BgItem)TileMap.vCurrItem.elementAt(i);
    			if (bgItem.idImage != -1 && bgItem.layer == layer)
    			{
    				bgItem.paint(g);
    			}
    		}
    		if (TileMap.mapID == 48 && layer == 3 && GameCanvas.bgW != null && GameCanvas.bgW[0] != 0)
    		{
    			for (int j = 0; j < TileMap.pxw / GameCanvas.bgW[0] + 1; j++)
    			{
    				g.drawImage(GameCanvas.imgBG[0], j * GameCanvas.bgW[0], TileMap.pxh - GameCanvas.bgH[0] - 70, 0);
    			}
    		}
    	}
    
    	public void paintBlackSky(mGraphics g)
    	{
    		if (!GameCanvas.lowGraphic)
    		{
    			g.fillTrans(imgTrans, 0, 0, GameCanvas.w, GameCanvas.h);
    		}
    	}
    
    	public void paintCapcha(mGraphics g)
    	{
    		MobCapcha.paint(g, Char.myCharz().cx, Char.myCharz().cy);
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		if (GameCanvas.menu.showMenu || GameCanvas.panel.isShow || ChatPopup.currChatPopup != null || !GameCanvas.isTouch)
    		{
    			return;
    		}
    		for (int i = 0; i < strCapcha.Length; i++)
    		{
    			int x = (GameCanvas.w - strCapcha.Length * disXC) / 2 + i * disXC + disXC / 2;
    			if (keyCapcha[i] == -1)
    			{
    				g.drawImage(imgNut, x, GameCanvas.h - 25, 3);
    				mFont.tahoma_7b_dark.drawString(g, strCapcha[i] + string.Empty, x, GameCanvas.h - 30, 2);
    			}
    			else
    			{
    				g.drawImage(imgNutF, x, GameCanvas.h - 25, 3);
    				mFont.tahoma_7b_green2.drawString(g, strCapcha[i] + string.Empty, x, GameCanvas.h - 30, 2);
    			}
    		}
    	}
    
    	public override void paint(mGraphics g)
    	{
    		try { 
    		countEff = 0;
    		if (!isPaint)
    		{
    			return;
    		}
    		GameCanvas.debug("PA1", 1);
    		if (isFreez || (isUseFreez && ChatPopup.currChatPopup == null))
    		{
    			dem++;
    			if ((dem < 30 && dem >= 0 && GameCanvas.gameTick % 4 == 0) || (dem >= 30 && dem <= 50 && GameCanvas.gameTick % 3 == 0) || dem > 50)
    			{
    				g.setColor(16777215);
    				g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    				if (dem <= 50)
    				{
    					return;
    				}
    				if (isUseFreez)
    				{
    					isUseFreez = false;
    					dem = 0;
    					if (activeRongThan)
    					{
    						callRongThan(xR, yR);
    					}
    					else
    					{
    						hideRongThan();
    					}
    				}
    				paintInfoBar(g);
    				g.translate(-cmx, -cmy);
    				g.translate(0, GameCanvas.transY);
    				Char.myCharz().paint(g);
    				mSystem.paintFlyText(g);
    				resetTranslate(g);
    				paintSelectedSkill(g);
    				return;
    			}
    		}
    		GameCanvas.debug("PA2", 1);
    		if (Main.isWindowFocused)
    		{
    			GameCanvas.paintBGGameScr(g);
    			paint_ios_bg(g);
    			if ((isRongThanXuatHien || isFireWorks) && TileMap.bgID != 3)
    			{
    				paintBlackSky(g);
    			}
    		}
    		else
    		{
    			g.setColor(0);
    			g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    		}
    		GameCanvas.debug("PA3", 1);
    		if (shock_scr > 0)
    		{
    			g.translate(-cmx + shock_x[shock_scr % shock_x.Length], -cmy + shock_y[shock_scr % shock_y.Length]);
    			shock_scr--;
    		}
    		else
    		{
    			g.translate(-cmx, -cmy);
    		}
    		if (isSuperPower)
    		{
    			int tx = ((GameCanvas.gameTick % 3 != 0) ? (-3) : 3);
    			g.translate(tx, 0);
    		}
    		if (Main.isWindowFocused)
    		{
    			BackgroudEffect.paintBehindTileAll(g);
    			EffecMn.paintLayer1(g);
    			TileMap.paintTilemap(g);
    			TileMap.paintOutTilemap(g);
    		}
    		if (TileMap.mapID == 128)
    		{
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				Char @char = (Char)vCharInMap.elementAt(i);
    				if (@char != null && @char.isMabuHold)
    				{
    					@char.paintHeadWithXY(g, @char.cx, @char.cy, 0);
    				}
    			}
    			if (Char.myCharz().isMabuHold)
    			{
    				Char.myCharz().paintHeadWithXY(g, Char.myCharz().cx, Char.myCharz().cy, 0);
    			}
    		}
    		paintBgItem(g, 2);
    		if (Char.myCharz().cmdMenu != null && GameCanvas.isTouch)
    		{
    			if (mScreen.keyTouch == 20)
    			{
    				g.drawImage(imgChat2, Char.myCharz().cmdMenu.x + cmx, Char.myCharz().cmdMenu.y + cmy, mGraphics.HCENTER | mGraphics.VCENTER);
    			}
    			else
    			{
    				g.drawImage(imgChat, Char.myCharz().cmdMenu.x + cmx, Char.myCharz().cmdMenu.y + cmy, mGraphics.HCENTER | mGraphics.VCENTER);
    			}
    		}
    		GameCanvas.debug("PA4", 1);
    		GameCanvas.debug("PA5", 1);
    		BackgroudEffect.paintBackAll(g);
    		EffectManager.lowEffects.paintAll(g);
    		for (int i = 0; i < Effect2.vEffectFeet.size(); i++)
    		{
    			Effect2 effect = (Effect2)Effect2.vEffectFeet.elementAt(i);
    			effect.paint(g);
    		}
    		for (int i = 0; i < Teleport.vTeleport.size(); i++)
    		{
    			((Teleport)Teleport.vTeleport.elementAt(i)).paintHole(g);
    		}
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc = (Npc)vNpc.elementAt(i);
    			if (npc.cHP > 0)
    			{
    				npc.paintShadow(g);
    			}
    		}
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			((Npc)vNpc.elementAt(i)).paint(g);
    		}
    		g.translate(0, GameCanvas.transY);
    		GameCanvas.debug("PA7", 1);
    		GameCanvas.debug("PA8", 1);
    		bool canPaintChar = (!GameCanvas.panel.isShow || !GameCanvas.panel.isTypeShop());
    		if (canPaintChar)
    		{
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				Char char2 = (Char)vCharInMap.elementAt(i);
    				if (char2 != null && char2.isShadown && char2.isPaint())
    				{
    					char2.paintShadow(g);
    				}
    			}
    		}
    		Char.myCharz().paintShadow(g);
    		EffecMn.paintLayer2(g);
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			((Mob)vMob.elementAt(i)).paint(g);
    		}
    		for (int i = 0; i < Teleport.vTeleport.size(); i++)
    		{
    			((Teleport)Teleport.vTeleport.elementAt(i)).paint(g);
    		}
    		if (canPaintChar)
    		{
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				Char char3 = (Char)vCharInMap.elementAt(i);
    				if (char3 != null && char3.isPaint())
    				{
    					char3.paint(g);
    				}
    			}
    		}
    		Char.myCharz().paint(g);
    		if (Char.myCharz().skillPaint != null && Char.myCharz().skillInfoPaint() != null && Char.myCharz().indexSkill < Char.myCharz().skillInfoPaint().Length)
    		{
    			Char.myCharz().paintCharWithSkill(g);
    			Char.myCharz().paintMount2(g);
    		}
    		if (canPaintChar)
    		{
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				Char char4 = (Char)vCharInMap.elementAt(i);
    				if (char4 != null && char4.isPaint() && char4.skillPaint != null && char4.skillInfoPaint() != null && char4.indexSkill < char4.skillInfoPaint().Length)
    				{
    					char4.paintCharWithSkill(g);
    					char4.paintMount2(g);
    				}
    			}
    		}
    		for (int i = 0; i < vItemMap.size(); i++)
    		{
    			ItemMap it = (ItemMap)vItemMap.elementAt(i);
    			if (it != null && it.isInViewport())
    			{
    				it.paint(g);
    			}
    		}
    		g.translate(0, -GameCanvas.transY);
    		GameCanvas.debug("PA9", 1);
    		paintSplash(g);
    		GameCanvas.debug("PA10", 1);
    		GameCanvas.debug("PA11", 1);
    		GameCanvas.debug("PA13", 1);
    		paintEffect(g);
    		paintBgItem(g, 3);
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc2 = (Npc)vNpc.elementAt(i);
    			npc2.paintName(g);
    		}
    		EffecMn.paintLayer3(g);
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc3 = (Npc)vNpc.elementAt(i);
    			if (npc3.chatInfo != null && npc3 != null)
    			{
    				npc3.chatInfo.paint(g, npc3.cx, npc3.cy - npc3.ch - GameCanvas.transY, npc3.cdir);
    			}
    		}
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char char5 = null;
    			try
    			{
    				char5 = (Char)vCharInMap.elementAt(i);
    			}
    			catch (Exception)
    			{
    			}
    			if (char5 != null && char5.chatInfo != null)
    			{
    				char5.chatInfo.paint(g, char5.cx, char5.cy - char5.ch, char5.cdir);
    			}
    		}
    		if (Char.myCharz().chatInfo != null)
    		{
    			Char.myCharz().chatInfo.paint(g, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, Char.myCharz().cdir);
    		}
    		EffectManager.mid_2Effects.paintAll(g);
    		EffectManager.midEffects.paintAll(g);
    		BackgroudEffect.paintFrontAll(g);
    		for (int j = 0; j < TileMap.vCurrItem.size(); j++)
    		{
    			BgItem bgItem = (BgItem)TileMap.vCurrItem.elementAt(j);
    			if (bgItem.idImage != -1 && bgItem.layer > 3)
    			{
    				bgItem.paint(g);
    			}
    		}
    		PopUp.paintAll(g);
    		if (TileMap.mapID == 120)
    		{
    			if (percentMabu != 100)
    			{
    				int w = percentMabu * mGraphics.getImageWidth(imgHPLost) / 100;
    				int num = percentMabu;
    				g.drawImage(imgHPLost, TileMap.pxw / 2 - mGraphics.getImageWidth(imgHPLost) / 2, 220, 0);
    				g.setClip(TileMap.pxw / 2 - mGraphics.getImageWidth(imgHPLost) / 2, 220, w, 10);
    				g.drawImage(imgHP, TileMap.pxw / 2 - mGraphics.getImageWidth(imgHPLost) / 2, 220, 0);
    				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			}
    			if (mabuEff)
    			{
    				tMabuEff++;
    				if (GameCanvas.gameTick % 3 == 0)
    				{
    					Effect me = new Effect(19, Res.random(TileMap.pxw / 2 - 50, TileMap.pxw / 2 + 50), 340, 2, 1, -1);
    					EffecMn.addEff(me);
    				}
    				if (GameCanvas.gameTick % 15 == 0)
    				{
    					Effect me2 = new Effect(18, Res.random(TileMap.pxw / 2 - 5, TileMap.pxw / 2 + 5), Res.random(300, 320), 2, 1, -1);
    					EffecMn.addEff(me2);
    				}
    				if (tMabuEff == 100)
    				{
    					activeSuperPower(TileMap.pxw / 2, 300);
    				}
    				if (tMabuEff == 110)
    				{
    					tMabuEff = 0;
    					mabuEff = false;
    				}
    			}
    		}
    		BackgroudEffect.paintFog(g);
    		bool flag = true;
    		for (int i = 0; i < BackgroudEffect.vBgEffect.size(); i++)
    		{
    			BackgroudEffect backgroudEffect = (BackgroudEffect)BackgroudEffect.vBgEffect.elementAt(i);
    			if (backgroudEffect.typeEff == 0)
    			{
    				flag = false;
    				break;
    			}
    		}
    		if (mGraphics.zoomLevel <= 1 || Main.isIpod || Main.isIphone4)
    		{
    			flag = false;
    		}
    		if (flag && !isRongThanXuatHien)
    		{
    			int num2 = TileMap.pxw / (mGraphics.getImageWidth(TileMap.imgLight) + 50);
    			if (num2 <= 0)
    			{
    				num2 = 1;
    			}
    			if (TileMap.tileID != 28)
    			{
    				for (int i = 0; i < num2; i++)
    				{
    					int num3 = 100 + i * (mGraphics.getImageWidth(TileMap.imgLight) + 50) - cmx / 2;
    					int num4 = -20;
    					int imageWidth = mGraphics.getImageWidth(TileMap.imgLight);
    					if (num3 + imageWidth >= cmx && num3 <= cmx + GameCanvas.w && num4 + mGraphics.getImageHeight(TileMap.imgLight) >= cmy && num4 <= cmy + GameCanvas.h)
    					{
    						g.drawImage(TileMap.imgLight, 100 + i * (mGraphics.getImageWidth(TileMap.imgLight) + 50) - cmx / 2, num4, 0);
    					}
    				}
    			}
    		}
    		mSystem.paintFlyText(g);
    		GameCanvas.debug("PA14", 1);
    		GameCanvas.debug("PA15", 1);
    		GameCanvas.debug("PA16", 1);
    		paintArrowPointToNPC(g);
    		GameCanvas.debug("PA17", 1);
    		if (!isPaintOther && isPaintRada == 1 && !GameCanvas.panel.isShow)
    		{
    			paintInfoBar(g);
    		}
    		resetTranslate(g);
    		paint_xp_bar(g);
    		if (!isPaintOther)
    		{
    			if (GameCanvas.open3Hour && TileMap.mapID != 170)
    			{
    				if (GameCanvas.w > 250)
    				{
    					g.drawImage(GameCanvas.img12, 160, 6, 0);
    					mFont.tahoma_7_white.drawString(g, "Dành cho người chơi trên 12 tuổi.", 180, 2, 0);
    					mFont.tahoma_7_white.drawString(g, "Chơi quá 180 phút mỗi ngày ", 180, 12, 0);
    					mFont.tahoma_7_white.drawString(g, "sẽ hại sức khỏe.", 180, 22, 0);
    				}
    				else
    				{
    					g.drawImage(GameCanvas.img12, 5, GameCanvas.h - 67, 0);
    					mFont.tahoma_7_white.drawString(g, "Dành cho người chơi trên 12 tuổi.", 25, GameCanvas.h - 70, 0);
    					mFont.tahoma_7_white.drawString(g, "Chơi quá 180 phút mỗi ngày sẽ hại sức khỏe.", 25, GameCanvas.h - 60, 0);
    				}
    			}
    			GameCanvas.debug("PA21", 1);
    			GameCanvas.debug("PA18", 1);
    			g.translate(-g.getTranslateX(), -g.getTranslateY());
    			if ((TileMap.mapID == 128 || TileMap.mapID == 127) && mabuPercent != 0)
    			{
    				int num5 = 30;
    				int num6 = 200;
    				g.setColor(0);
    				g.fillRect(num5 - 27, num6 - 112, 54, 8);
    				g.setColor(16711680);
    				g.setClip(num5 - 25, num6 - 110, mabuPercent, 4);
    				g.fillRect(num5 - 25, num6 - 110, 50, 4);
    				g.setClip(0, 0, 3000, 3000);
    				mFont.tahoma_7b_white.drawString(g, "Mabu", num5, num6 - 112 + 10, 2, mFont.tahoma_7b_dark);
    			}
    			if (Char.myCharz().isFusion)
    			{
    				Char.myCharz().tFusion++;
    				if (GameCanvas.gameTick % 3 == 0)
    				{
    					g.setColor(16777215);
    					g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    				}
    				if (Char.myCharz().tFusion >= 100)
    				{
    					Char.myCharz().fusionComplete();
    				}
    			}
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				Char char6 = null;
    				try
    				{
    					char6 = (Char)vCharInMap.elementAt(i);
    				}
    				catch (Exception)
    				{
    				}
    				if (char6 != null && char6.isFusion && Char.isCharInScreen(char6))
    				{
    					char6.tFusion++;
    					if (GameCanvas.gameTick % 3 == 0)
    					{
    						g.setColor(16777215);
    						g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    					}
    					if (char6.tFusion >= 100)
    					{
    						char6.fusionComplete();
    					}
    				}
    			}
    			GameCanvas.paintz.paintTabSoft(g);
    			GameCanvas.debug("PA19", 1);
    			GameCanvas.debug("PA20", 1);
    			resetTranslate(g);
    			paintSelectedSkill(g);
    			GameCanvas.debug("PA22", 1);
    			resetTranslate(g);
    			if (GameCanvas.isTouch && GameCanvas.isTouchControl)
    			{
    				paintTouchControl(g);
    			}
    			resetTranslate(g);
    			paintChatVip(g);
    			if (!GameCanvas.panel.isShow && GameCanvas.currentDialog == null && ChatPopup.currChatPopup == null && ChatPopup.serverChatPopUp == null && GameCanvas.currentScreen.Equals(instance))
    			{
    				base.paint(g);
    				if (mScreen.keyMouse == 1 && cmdMenu != null)
    				{
    					g.drawImage(ItemMap.imageFlare, cmdMenu.x + 7, cmdMenu.y + 15, 3);
    				}
    			}
    			resetTranslate(g);
    			int num7 = 100 + ((Char.vItemTime.size() != 0) ? (textTime.size() * 12) : 0);
    			if (Char.myCharz().clan != null)
    			{
    				int num8 = 0;
    				int num9 = 0;
    				int num10 = (GameCanvas.h - 100 - 60) / 12;
    				for (int i = 0; i < vCharInMap.size(); i++)
    				{
    					Char char7 = (Char)vCharInMap.elementAt(i);
    					if (char7.clanID == -1 || char7.clanID != Char.myCharz().clan.ID)
    					{
    						continue;
    					}
    					if (char7.isOutX() && char7.cx < Char.myCharz().cx)
    					{
    						int num11 = num10;
    						if (Char.vItemTime.size() != 0)
    						{
    							num11 -= textTime.size();
    						}
    						if (num8 <= num11)
    						{
    							mFont.tahoma_7_green.drawString(g, char7.cName, 20, num7 - 12 + num8 * 12, mFont.LEFT, mFont.tahoma_7_grey);
    							char7.paintHp(g, 10, num7 + num8 * 12 - 5);
    							num8++;
    						}
    					}
    					else if (char7.isOutX() && char7.cx > Char.myCharz().cx && num9 <= num10)
    					{
    						mFont.tahoma_7_green.drawString(g, char7.cName, GameCanvas.w - 25, num7 - 12 + num9 * 12, mFont.RIGHT, mFont.tahoma_7_grey);
    						char7.paintHp(g, GameCanvas.w - 15, num7 + num9 * 12 - 5);
    						num9++;
    					}
    				}
    			}
    			ChatTextField.gI().paint(g);
    			if (isNewClanMessage && !GameCanvas.panel.isShow && GameCanvas.gameTick % 4 == 0)
    			{
    				g.drawImage(ItemMap.imageFlare, cmdMenu.x + 15, cmdMenu.y + 30, mGraphics.BOTTOM | mGraphics.HCENTER);
    			}
    			if (isSuperPower)
    			{
    				dxPower += 5;
    				if (tPower >= 0)
    				{
    					tPower += dxPower;
    				}
    				Res.outz("x power= " + xPower);
    				if (tPower < 0)
    				{
    					tPower--;
    					if (tPower == -20)
    					{
    						isSuperPower = false;
    						tPower = 0;
    						dxPower = 0;
    					}
    				}
    				else if ((xPower - tPower > 0 || tPower < TileMap.pxw) && tPower > 0)
    				{
    					g.setColor(16777215);
    					if (!GameCanvas.lowGraphic)
    					{
    						g.fillArg(0, 0, GameCanvas.w, GameCanvas.h, 0, 0);
    					}
    					else
    					{
    						g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    					}
    				}
    				else
    				{
    					tPower = -1;
    				}
    			}
    			for (int i = 0; i < Char.vItemTime.size(); i++)
    			{
    				((ItemTime)Char.vItemTime.elementAt(i)).paint(g, cmdMenu.x + 32 + i * 24, 55);
    			}
    			if (Char.myCharz().clan != null)
    			{
    				int totalClanInMap = 1;
    				for (int j = 0; j < vCharInMap.size(); j++)
    				{
    					Char ch = (Char)vCharInMap.elementAt(j);
    					if (ch != null && ch.clanID != -1 && ch.clanID == Char.myCharz().clan.ID && ch.statusMe != 14 && ch.statusMe != 5)
    					{
    						totalClanInMap++;
    					}
    				}
    				if (totalClanInMap >= 2 || ItemTime.isExistMessage(5))
    				{
    					int members = (totalClanInMap < 2) ? 2 : totalClanInMap;
    					int percentPT = (5 + (members - 2) > 10) ? 10 : (5 + (members - 2));
    					int ptX = cmdMenu.x + 32 + Char.vItemTime.size() * 24;
    					int ptY = 55;
    					ClanImage cm = ClanImage.getClanImage((short)Char.myCharz().clan.imgID);
    					if (cm == null)
    					{
    						cm = new ClanImage();
    						cm.ID = Char.myCharz().clan.imgID;
    						ClanImage.addClanImage(cm);
    					}
    					int drawIconId = 1025;
    					if (cm != null && cm.idImage != null && cm.idImage.Length > 0 && cm.idImage[0] > 0)
    					{
    						drawIconId = cm.idImage[0];
    					}
    					else if (ClanImage.idImages != null && ClanImage.idImages.containsKey(Char.myCharz().clan.imgID + string.Empty))
    					{
    						ClanImage bagImg = (ClanImage)ClanImage.idImages.get(Char.myCharz().clan.imgID + string.Empty);
    						if (bagImg != null && bagImg.idImage != null && bagImg.idImage.Length > 0 && bagImg.idImage[0] > 0)
    						{
    							drawIconId = bagImg.idImage[0];
    						}
    					}
    					SmallImage.drawSmallImage(g, drawIconId, ptX, ptY, 0, 3);
    					mFont.tahoma_7_green.drawString(g, "PT", ptX + 7, ptY - 6, 2, mFont.tahoma_7b_dark);
    					mFont.tahoma_7_yellow.drawString(g, "+" + percentPT + "%", ptX + 7, ptY + 15, 2, mFont.tahoma_7b_dark);
    					if (GameCanvas.isPointerJustRelease && GameCanvas.isPointerHoldIn(ptX - 4, ptY - 6, 24, 30))
    					{
    						GameCanvas.isPointerJustRelease = false;
    						info1.addInfo("Tổ đội bang (" + members + " người): Đứng gần nhau cùng khu tăng +" + percentPT + "% TNSM!", 0);
    					}
    				}
    			}
    			for (int i = 0; i < textTime.size(); i++)
    			{
    				((ItemTime)textTime.elementAt(i)).paintText(g, cmdMenu.x + ((Char.vItemTime.size() == 0) ? 25 : 5), ((Char.vItemTime.size() == 0) ? 45 : 90) + i * 12);
    			}
    			paintXoSo(g);
    			if (mResources.language == 1)
    			{
    				long second = mSystem.currentTimeMillis() - deltaTime;
    				mFont.tahoma_7b_white.drawString(g, NinjaUtil.getDate2(second), 10, GameCanvas.h - 65, 0, mFont.tahoma_7b_dark);
    			}
    			if (!yourNumber.Equals(string.Empty))
    			{
    				for (int i = 0; i < strPaint.Length; i++)
    				{
    					mFont.tahoma_7b_white.drawString(g, strPaint[i], 5, 85 + i * 18, 0, mFont.tahoma_7b_dark);
    				}
    			}
    		}
    		int num12 = 0;
    		int num13 = GameCanvas.hw;
    		if (num13 > 200)
    		{
    			num13 = 200;
    		}
    		paintPhuBanBar(g, num12 + GameCanvas.w / 2, 0, num13);
    		EffectManager.hiEffects.paintAll(g);
    		if (nCT_timeBallte > mSystem.currentTimeMillis() && TileMap.mapID == 170 && isPaint_CT && nCT_nBoyBaller / 2 > 0)
    		{
    			try
    			{
    				paint_CT(g, num12 + GameCanvas.w / 2, 0, num13);
    			}
    			catch (Exception)
    			{
    			}
    		}
    		if (TileMap.mapID == 172)
    		{
    			string text = mResources.WAIT + "  " + nUSER_CT + "/" + nUSER_MAX_CT;
    			mFont.tahoma_7b_dark.drawString(g, mResources.WAIT + "  " + nUSER_CT + "/" + nUSER_MAX_CT, GameCanvas.w - 10, 40, 1);
    		}
            ClientManager.getInstance().GUI(g);
        }
    		catch(Exception e)
    		{
    			Debug.LogException(e);
    		}
    		}
    	private void paintXoSo(mGraphics g)
    	{
    		if (tShow != 0)
    		{
    			string text = string.Empty;
    			for (int i = 0; i < winnumber.Length; i++)
    			{
    				text = text + randomNumber[i] + " ";
    			}
    			PopUp.paintPopUp(g, 20, 45, 95, 35, 16777215, false);
    			mFont.tahoma_7b_dark.drawString(g, mResources.kquaVongQuay, 68, 50, 2);
    			mFont.tahoma_7b_dark.drawString(g, text + string.Empty, 68, 65, 2);
    		}
    	}
    
    	private void checkEffToObj(IMapObject obj, bool isnew)
    	{
    		if (obj == null || tDoubleDelay > 0)
    		{
    			return;
    		}
    		tDoubleDelay = 10;
    		int x = obj.getX();
    		int num = 1;
    		int num2 = Res.abs(Char.myCharz().cx - x);
    		num = ((num2 <= 80) ? 1 : ((num2 > 80 && num2 <= 200) ? 2 : ((num2 <= 200 || num2 > 400) ? 4 : 3)));
    		if (!isnew)
    		{
    			if (obj.Equals(Char.myCharz().mobFocus) || (obj.Equals(Char.myCharz().charFocus) && Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus)))
    			{
    				ServerEffect.addServerEffect(135, obj.getX(), obj.getY(), num);
    			}
    			else if (obj.Equals(Char.myCharz().npcFocus) || obj.Equals(Char.myCharz().itemFocus) || obj.Equals(Char.myCharz().charFocus))
    			{
    				ServerEffect.addServerEffect(136, obj.getX(), obj.getY(), num);
    			}
    		}
    		else
    		{
    			ServerEffect.addServerEffect(136, obj.getX(), obj.getY(), num);
    		}
    	}
    
    	private void updateClickToArrow()
    	{
    		if (tDoubleDelay > 0)
    		{
    			tDoubleDelay--;
    		}
    		if (clickMoving)
    		{
    			clickMoving = false;
    			IMapObject mapObject = findClickToItem(clickToX, clickToY);
    			if (mapObject == null || (mapObject != null && mapObject.Equals(Char.myCharz().npcFocus) && TileMap.mapID == 51))
    			{
    				ServerEffect.addServerEffect(134, clickToX, clickToY + GameCanvas.transY / 2, 3);
    			}
    		}
    	}
    
    	private void paintWaypointArrow(mGraphics g)
    	{
    		int num = 10;
    		Task taskMaint = Char.myCharz().taskMaint;
    		if (taskMaint != null && taskMaint.taskId == 0 && ((taskMaint.index != 1 && taskMaint.index < 6) || taskMaint.index == 0))
    		{
    			return;
    		}
    		for (int i = 0; i < TileMap.vGo.size(); i++)
    		{
    			Waypoint waypoint = (Waypoint)TileMap.vGo.elementAt(i);
    			if (waypoint.minY == 0 || waypoint.maxY >= TileMap.pxh - 24)
    			{
    				if (waypoint.maxY <= TileMap.pxh / 2)
    				{
    					int x = waypoint.minX + (waypoint.maxX - waypoint.minX) / 2;
    					int y = waypoint.minY + (waypoint.maxY - waypoint.minY) / 2 + runArrow;
    					if (GameCanvas.isTouch)
    					{
    						y = waypoint.maxY + (waypoint.maxY - waypoint.minY) + runArrow + num;
    					}
    					g.drawRegion(arrow, 0, 0, 13, 16, 6, x, y, StaticObj.VCENTER_HCENTER);
    				}
    				else if (waypoint.minY >= TileMap.pxh / 2)
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 4, waypoint.minX + (waypoint.maxX - waypoint.minX) / 2, waypoint.minY - 12 - runArrow, StaticObj.VCENTER_HCENTER);
    				}
    			}
    			else if (waypoint.minX >= 0 && waypoint.minX < 24)
    			{
    				if (!GameCanvas.isTouch)
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 2, waypoint.maxX + 12 + runArrow, waypoint.maxY - 12, StaticObj.VCENTER_HCENTER);
    				}
    				else
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 2, waypoint.maxX + 12 + runArrow, waypoint.maxY - 32, StaticObj.VCENTER_HCENTER);
    				}
    			}
    			else if (waypoint.minX <= TileMap.tmw * 24 && waypoint.minX >= TileMap.tmw * 24 - 48)
    			{
    				if (!GameCanvas.isTouch)
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 0, waypoint.minX - 12 - runArrow, waypoint.maxY - 12, StaticObj.VCENTER_HCENTER);
    				}
    				else
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 0, waypoint.minX - 12 - runArrow, waypoint.maxY - 32, StaticObj.VCENTER_HCENTER);
    				}
    			}
    			else
    			{
    				g.drawRegion(arrow, 0, 0, 13, 16, 4, waypoint.minX + (waypoint.maxX - waypoint.minX) / 2, waypoint.maxY - 48 - runArrow, StaticObj.VCENTER_HCENTER);
    			}
    		}
    	}
    
    	public static Npc findNPCInMap(short id)
    	{
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc = (Npc)vNpc.elementAt(i);
    			if (npc.template.npcTemplateId == id)
    			{
    				return npc;
    			}
    		}
    		return null;
    	}
    
    	public static Char findCharInMap(int charId)
    	{
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char @char = (Char)vCharInMap.elementAt(i);
    			if (@char.charID == charId)
    			{
    				return @char;
    			}
    		}
    		return null;
    	}
    }
}

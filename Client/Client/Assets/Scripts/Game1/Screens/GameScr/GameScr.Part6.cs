namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameScr : mScreen, IChatable
    {
    
    	public static Mob findMobInMap(sbyte mobIndex)
    	{
    		return (Mob)vMob.elementAt(mobIndex);
    	}
    
    	public static Mob findMobInMap(int mobId)
    	{
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			Mob mob = (Mob)vMob.elementAt(i);
    			if (mob.mobId == mobId)
    			{
    				return mob;
    			}
    		}
    		return null;
    	}
    
    	public static Npc getNpcTask()
    	{
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc = (Npc)vNpc.elementAt(i);
    			if (npc.template.npcTemplateId == getTaskNpcId())
    			{
    				return npc;
    			}
    		}
    		return null;
    	}
    
    	private void paintArrowPointToNPC(mGraphics g)
    	{
    		try
    		{
    			if (ChatPopup.currChatPopup != null)
    			{
    				return;
    			}
    			int num = getTaskNpcId();
    			if (num == -1)
    			{
    				return;
    			}
    			Npc npc = null;
    			for (int i = 0; i < vNpc.size(); i++)
    			{
    				Npc npc2 = (Npc)vNpc.elementAt(i);
    				if (npc2.template.npcTemplateId == num)
    				{
    					if (npc == null)
    					{
    						npc = npc2;
    					}
    					else if (Res.abs(npc2.cx - Char.myCharz().cx) < Res.abs(npc.cx - Char.myCharz().cx))
    					{
    						npc = npc2;
    					}
    				}
    			}
    			if (npc == null || npc.statusMe == 15 || (npc.cx > cmx && npc.cx < cmx + gW && npc.cy > cmy && npc.cy < cmy + gH) || GameCanvas.gameTick % 10 < 5)
    			{
    				return;
    			}
    			int num2 = npc.cx - Char.myCharz().cx;
    			int num3 = npc.cy - Char.myCharz().cy;
    			int x = 0;
    			int y = 0;
    			int arg = 0;
    			if (num2 > 0 && num3 >= 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = gW - 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 0;
    				}
    				else
    				{
    					x = gW / 2;
    					y = gH - 10;
    					arg = 5;
    				}
    			}
    			else if (num2 >= 0 && num3 < 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = gW - 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 0;
    				}
    				else
    				{
    					x = gW / 2;
    					y = 10;
    					arg = 6;
    				}
    			}
    			if (num2 < 0 && num3 >= 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 3;
    				}
    				else
    				{
    					x = gW / 2;
    					y = gH - 10;
    					arg = 5;
    				}
    			}
    			else if (num2 <= 0 && num3 < 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 3;
    				}
    				else
    				{
    					x = gW / 2;
    					y = 10;
    					arg = 6;
    				}
    			}
    			resetTranslate(g);
    			g.drawRegion(arrow, 0, 0, 13, 16, arg, x, y, StaticObj.VCENTER_HCENTER);
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham arrow to npc: " + ex.ToString());
    		}
    	}
    
    	public static void resetTranslate(mGraphics g)
    	{
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		g.setClip(0, -200, GameCanvas.w, 200 + GameCanvas.h);
    	}
    
    	private void paintTouchControl(mGraphics g)
    	{
    		if (isNotPaintTouchControl())
    		{
    			return;
    		}
    		resetTranslate(g);
    		if (!TileMap.isOfflineMap() && !isVS())
    		{
    			if (mScreen.keyTouch == 15 || mScreen.keyMouse == 15)
    			{
    				g.drawImage((!Main.isPC) ? imgChat2 : imgChatsPC2, xC + 17, yC + 17 + mGraphics.addYWhenOpenKeyBoard, mGraphics.HCENTER | mGraphics.VCENTER);
    			}
    			else
    			{
    				g.drawImage((!Main.isPC) ? imgChat : imgChatPC, xC + 17, yC + 17 + mGraphics.addYWhenOpenKeyBoard, mGraphics.HCENTER | mGraphics.VCENTER);
    			}
    		}
    		if (isUseTouch)
    		{
    		}
    	}
    
    	public void paintImageBarRight(mGraphics g, Char c)
    	{
    		int num = (c.cHPFull > 0) ? (int)(c.cHP * hpBarW / c.cHPFull) : 0;
    		int num2 = (int)(c.cMP * mpBarW);
    		int num3 = (c.cHPFull > 0) ? (int)(dHP * hpBarW / c.cHPFull) : 0;
    		int num4 = (int)(dMP * mpBarW);
    		g.setClip(GameCanvas.w / 2 + 58 - mGraphics.getImageWidth(imgPanel), 0, 95, 100);
    		g.drawRegion(imgPanel, 0, 0, mGraphics.getImageWidth(imgPanel), mGraphics.getImageHeight(imgPanel), 2, GameCanvas.w / 2 + 60, 0, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - hpBarW + hpBarW - num3), 5, num3, 10);
    		g.drawImage(imgHPLost, GameCanvas.w / 2 + 60 - 83, 5, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - hpBarW + hpBarW - num), 5, num, 10);
    		g.drawImage(imgHP, GameCanvas.w / 2 + 60 - 83, 5, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - mpBarW + hpBarW - num4), 20, num4, 6);
    		g.drawImage(imgMPLost, GameCanvas.w / 2 + 60 - 83, 20, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - mpBarW + hpBarW - num2), 20, num2, 6);
    		g.drawImage(imgMP, GameCanvas.w / 2 + 60 - 83, 20, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    	}
    
    	private void paintImageBar(mGraphics g, bool isLeft, Char c)
    	{
    		if (c != null)
    		{
    			int num = 0;
    			int num2 = 0;
    			int num3 = 0;
    			int num4 = 0;
    			if (c.charID == Char.myCharz().charID)
    			{
    				num = (c.cHPFull > 0) ? (int)(dHP * hpBarW / c.cHPFull) : 0;
    				num2 = (c.cMPFull > 0) ? (int)(dMP * mpBarW / c.cMPFull) : 0;
    				num3 = (c.cHPFull > 0) ? (int)(c.cHP * hpBarW / c.cHPFull) : 0;
    				num4 = (c.cMPFull > 0) ? (int)(c.cMP * mpBarW / c.cMPFull) : 0;
    			}
    			else
    			{
    				num = (c.cHPFull > 0) ? (int)(c.dHP * hpBarW / c.cHPFull) : 0;
    				num2 = c.perCentMp * mpBarW / 100;
    				num3 = (c.cHPFull > 0) ? (int)(c.cHP * hpBarW / c.cHPFull) : 0;
    				num4 = c.perCentMp * mpBarW / 100;
    			}
    			if (Char.myCharz().secondPower > 0)
    			{
    				int w = (Char.myCharz().maxPowerPoint > 0) ? (Char.myCharz().powerPoint * spBarW / Char.myCharz().maxPowerPoint) : 0;
    				g.drawImage(imgPanel2, 58, 29, 0);
    				g.setClip(83, 31, w, 10);
    				g.drawImage(imgSP, 83, 31, 0);
    				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    				mFont.tahoma_7_white.drawString(g, Char.myCharz().strInfo + ":" + Char.myCharz().powerPoint + "/" + Char.myCharz().maxPowerPoint, 115, 29, 2);
    			}
    			if (c.charID != Char.myCharz().charID)
    			{
    				g.setClip(mGraphics.getImageWidth(imgPanel) - 95, 0, 95, 100);
    			}
    			g.drawImage(imgPanel, 0, 0, 0);
    			if (isLeft)
    			{
    				g.setClip(83, 5, num, 10);
    			}
    			else
    			{
    				g.setClip((int)(83 + hpBarW - num), 5, num, 10);
    			}
    			g.drawImage(imgHPLost, 83, 5, 0);
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			if (isLeft)
    			{
    				g.setClip(83, 5, num3, 10);
    			}
    			else
    			{
    				g.setClip((int)(83 + hpBarW - num3), 5, num3, 10);
    			}
    			g.drawImage(imgHP, 83, 5, 0);
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			if (isLeft)
    			{
    				g.setClip(83, 20, num2, 6);
    			}
    			else
    			{
    				g.setClip(83 + mpBarW - num2, 20, num2, 6);
    			}
    			g.drawImage(imgMPLost, 83, 20, 0);
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			if (isLeft)
    			{
    				g.setClip(83, 20, num2, 6);
    			}
    			else
    			{
    				g.setClip(83 + mpBarW - num4, 20, num4, 6);
    			}
    			g.drawImage(imgMP, 83, 20, 0);
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			if (Char.myCharz().cMP == 0 && GameCanvas.gameTick % 10 > 5)
    			{
    				g.setClip(83, 20, 2, 6);
    				g.drawImage(imgMPLost, 83, 20, 0);
    				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			}
    		}
    	}
    
    	public void getInjure()
    	{
    	}
    
    	public void starVS()
    	{
    		curr = (last = mSystem.currentTimeMillis());
    		secondVS = 180;
    	}
    
    	private Char findCharVS1()
    	{
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char @char = (Char)vCharInMap.elementAt(i);
    			if (@char.cTypePk != 0)
    			{
    				return @char;
    			}
    		}
    		return null;
    	}
    
    	private Char findCharVS2()
    	{
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char @char = (Char)vCharInMap.elementAt(i);
    			if (@char.cTypePk != 0 && @char != findCharVS1())
    			{
    				return @char;
    			}
    		}
    		return null;
    	}
    
    	private void paintInfoBar(mGraphics g)
    	{
    		resetTranslate(g);
    		if (TileMap.mapID == 130 && findCharVS1() != null && findCharVS2() != null)
    		{
    			g.translate(GameCanvas.w / 2 - 62, 0);
    			paintImageBar(g, true, findCharVS1());
    			g.translate(-(GameCanvas.w / 2 - 65), 0);
    			paintImageBarRight(g, findCharVS2());
    			findCharVS1().paintHeadWithXY(g, 137, 25, 0);
    			findCharVS2().paintHeadWithXY(g, GameCanvas.w - 15 - 122, 25, 2);
    		}
    		else if (isVS() && Char.myCharz().charFocus != null)
    		{
    			g.translate(GameCanvas.w / 2 - 62, 0);
    			paintImageBar(g, true, Char.myCharz().charFocus);
    			g.translate(-(GameCanvas.w / 2 - 65), 0);
    			paintImageBarRight(g, Char.myCharz());
    			Char.myCharz().paintHeadWithXY(g, 137, 25, 0);
    			Char.myCharz().charFocus.paintHeadWithXY(g, GameCanvas.w - 15 - 122, 25, 2);
    		}
    		else if (ispaintPhubangBar() && isSmallScr())
    		{
    			paintHPBar_NEW(g, 1, 1, Char.myCharz());
    		}
    		else
    		{
    			paintImageBar(g, true, Char.myCharz());
    			if (Char.myCharz().isInEnterOfflinePoint() != null || Char.myCharz().isInEnterOnlinePoint() != null)
    			{
    				mFont.tahoma_7_green2.drawString(g, mResources.enter, imgScrW / 2, 8 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    			}
    			else if (Char.myCharz().mobFocus != null)
    			{
    				if (Char.myCharz().mobFocus.getTemplate() != null)
    				{
    					mFont.tahoma_7b_green2.drawString(g, Char.myCharz().mobFocus.getTemplate().name, imgScrW / 2, 9 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    				}
    				if (Char.myCharz().mobFocus.templateId != 0)
    				{
    					mFont.tahoma_7b_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().mobFocus.hp), imgScrW / 2, 22 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    				}
    			}
    			else if (Char.myCharz().npcFocus != null)
    			{
    				mFont.tahoma_7b_green2.drawString(g, Char.myCharz().npcFocus.template.name, imgScrW / 2, 9 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    				if (Char.myCharz().npcFocus.template.npcTemplateId == 4 && gI().magicTree != null)
    				{
    					mFont.tahoma_7b_green2.drawString(g, gI().magicTree.currPeas + "/" + gI().magicTree.maxPeas, imgScrW / 2, 22 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    				}
    			}
    			else if (Char.myCharz().charFocus != null && !Char.myCharz().charFocus.isPetFollower && Char.myCharz().charFocus.charID > -310720000 && !string.IsNullOrEmpty(Char.myCharz().charFocus.cName) && Char.myCharz().charFocus.cName != "$")
    			{
    				mFont.tahoma_7b_green2.drawString(g, Char.myCharz().charFocus.cName, imgScrW / 2, 9 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    				mFont.tahoma_7b_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().charFocus.cHP), imgScrW / 2, 22 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    			}
    			else
    			{
    				mFont.tahoma_7b_green2.drawString(g, Char.myCharz().cName, imgScrW / 2, 9 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    				mFont.tahoma_7b_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().cPower), imgScrW / 2, 22 + mGraphics.addYWhenOpenKeyBoard, mFont.CENTER);
    			}
    		}
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		if (isVS() && secondVS > 0)
    		{
    			curr = mSystem.currentTimeMillis();
    			if (curr - last >= 1000)
    			{
    				last = mSystem.currentTimeMillis();
    				secondVS--;
    			}
    			mFont.tahoma_7b_white.drawString(g, secondVS.ToString(), GameCanvas.w / 2, 13, 2, mFont.tahoma_7b_dark);
    		}
    		if (flareFindFocus)
    		{
    			g.drawImage(ItemMap.imageFlare, 40, 35, mGraphics.BOTTOM | mGraphics.HCENTER);
    			flareTime--;
    			if (flareTime < 0)
    			{
    				flareTime = 0;
    				flareFindFocus = false;
    			}
    		}
    	}
    
    	public bool isVS()
    	{
    		if (TileMap.isVoDaiMap() && (Char.myCharz().cTypePk != 0 || (TileMap.mapID == 130 && findCharVS1() != null && findCharVS2() != null)))
    		{
    			return true;
    		}
    		return false;
    	}
    
    	private void paintSelectedSkill(mGraphics g)
    	{
    		if (Char.myCharz() == null || Char.myCharz().taskMaint == null || GameCanvas.panel == null || GameCanvas.menu == null || ChatTextField.gI() == null)
    		{
    			return;
    		}
    		if (mobCapcha != null)
    		{
    			paintCapcha(g);
    		}
    		else
    		{
    			if (GameCanvas.currentDialog != null || ChatPopup.currChatPopup != null || GameCanvas.menu.showMenu || isPaintPopup() || GameCanvas.panel.isShow || ChatTextField.gI().isShow || GameCanvas.currentScreen == MoneyCharge.instance)
    			{
    				return;
    			}
    			long num = mSystem.currentTimeMillis();
    			long num2 = num - lastUsePotion;
    			int num3 = 0;
    			if (num2 < 10000)
    			{
    				num3 = (int)(num2 * 20 / 10000);
    			}
    			if (!GameCanvas.isTouch)
    			{
    				g.drawImage((mScreen.keyTouch != 10) ? imgSkill : imgSkill2, xSkill + xHP - 1, yHP - 1, 0);
    				SmallImage.drawSmallImage(g, 542, xSkill + xHP + 3, yHP + 3, 0, 0);
    				mFont.number_gray.drawString(g, string.Empty + hpPotion, xSkill + xHP + 22, yHP + 15, 1);
    				if (num2 < 10000)
    				{
    					g.setColor(2721889);
    					num3 = (int)(num2 * 20 / 10000);
    					g.fillRect(xSkill + xHP + 3, yHP + 3 + num3, 20, 20 - num3);
    				}
    			}
    			else if (Char.myCharz().statusMe != 14)
    			{
    				if (gamePad.isSmallGamePad)
    				{
    					if (isAnalog != 1)
    					{
    						g.setColor(9670800);
    						g.fillRect(xHP + 9, yHP + 10 + 10, 22, 20);
    						g.setColor(16777215);
    						g.fillRect(xHP + 9, yHP + 10 + ((num3 != 0) ? (20 - num3) : 0) + 10, 22, (num3 == 0) ? 20 : num3);
    						g.drawImage((mScreen.keyTouch != 10) ? imgHP1 : imgHP2, xHP, yHP + 10, 0);
    						mFont.tahoma_7_red.drawString(g, string.Empty + hpPotion, xHP + 20, yHP + 15 + 10, 2);
    						if (isPickNgocRong)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNR1 : imgNR2, xHP + 5, yHP - 6 - 40 + 10, 0);
    						}
    						else if (isudungCapsun4)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNutF : imgNut, xHP + 5, yHP - 6 - 40 + 10, 0);
    							SmallImage.drawSmallImage(g, 1088, xHP - 7 + 5, yHP - 6 - 40 - 7 + 10, 0, 0);
    						}
    						else if (isudungCapsun3)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNutF : imgNut, xHP + 5, yHP - 6 - 40 + 10, 0);
    							SmallImage.drawSmallImage(g, 1087, xHP - 7 + 5, yHP - 6 - 40 - 7 + 10, 0, 0);
    						}
    					}
    					else if (isAnalog == 1)
    					{
    						int num4 = 10;
    						g.drawImage((mScreen.keyTouch != 10) ? imgSkill : imgSkill2, xSkill + xHP - 1, yHP - 1 + num4, 0);
    						SmallImage.drawSmallImage(g, 542, xSkill + xHP + 3, yHP + 3 + num4, 0, 0);
    						mFont.number_gray.drawString(g, string.Empty + hpPotion, xSkill + xHP + 22, yHP + 13 + num4, 1);
    						if (num2 < 10000)
    						{
    							g.setColor(2721889);
    							num3 = (int)(num2 * 20 / 10000);
    							g.fillRect(xSkill + xHP + 3, yHP + 3 + num3 + num4, 20, 20 - num3);
    						}
    						if (isPickNgocRong)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNR3 : imgNR4, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    						}
    						else if (isudungCapsun4)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNut : imgNutF, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    							SmallImage.drawSmallImage(g, 1088, xHP + 20 - 7 + 5, yHP + 20 - 6 - 40 - 7 + 10, 0, 0);
    						}
    						else if (isudungCapsun3)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNut : imgNutF, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    							SmallImage.drawSmallImage(g, 1087, xHP + 20 - 7 + 5, yHP + 20 - 6 - 40 - 7 + 10, 0, 0);
    						}
    					}
    				}
    				else if (isAnalog != 1)
    				{
    					g.setColor(9670800);
    					g.fillRect(xHP + 9, yHP + 10 - 6, 22, 20);
    					g.setColor(16777215);
    					g.fillRect(xHP + 9, yHP + 10 + ((num3 != 0) ? (20 - num3) : 0) - 6, 22, (num3 == 0) ? 20 : num3);
    					g.drawImage((mScreen.keyTouch != 10) ? imgHP1 : imgHP2, xHP, yHP - 6, 0);
    					mFont.tahoma_7_red.drawString(g, string.Empty + hpPotion, xHP + 20, yHP + 15 - 6, 2);
    					if (isPickNgocRong)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNR1 : imgNR2, xHP, yHP - 6 - 40, 0);
    					}
    					else if (isudungCapsun4)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNut : imgNutF, xHP + 20, yHP + 20 - 6 - 40, mGraphics.HCENTER | mGraphics.VCENTER);
    						SmallImage.drawSmallImage(g, 1088, xHP + 20 - 7, yHP + 20 - 6 - 40 - 7, 0, 0);
    					}
    					else if (isudungCapsun3)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNut : imgNutF, xHP + 20, yHP + 20 - 6 - 40, mGraphics.HCENTER | mGraphics.VCENTER);
    						SmallImage.drawSmallImage(g, 1087, xHP + 20 - 7, yHP + 20 - 6 - 40 - 7, 0, 0);
    					}
    				}
    				else
    				{
    					g.setColor(9670800);
    					g.fillRect(xHP + 10, yHP + 10 - 6 + 10, 20, 18);
    					g.setColor(16777215);
    					g.fillRect(xHP + 10, yHP + 10 + ((num3 != 0) ? (20 - num3) : 0) - 6 + 10, 20, (num3 == 0) ? 18 : num3);
    					g.drawImage((mScreen.keyTouch != 10) ? imgHP3 : imgHP4, xHP + 20, yHP + 20 - 6 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    					mFont.tahoma_7_red.drawString(g, string.Empty + hpPotion, xHP + 20, yHP + 15 - 6 + 10, 2);
    					if (isPickNgocRong)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNR3 : imgNR4, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    					}
    					else if (isudungCapsun4)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNut : imgNutF, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    						SmallImage.drawSmallImage(g, 1088, xHP + 20 - 7 + 5, yHP + 20 - 6 - 40 - 7 + 10, 0, 0);
    					}
    					else if (isudungCapsun3)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNut : imgNutF, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    						SmallImage.drawSmallImage(g, 1087, xHP + 20 - 7 + 5, yHP + 20 - 6 - 40 - 7 + 10, 0, 0);
    					}
    				}
    			}
    			if (isHaveSelectSkill)
    			{
    				Skill[] array = (Main.isPC ? keySkill : ((!GameCanvas.isTouch) ? keySkill : onScreenSkill));
    				if (mScreen.keyTouch == 10)
    				{
    				}
    				if (!GameCanvas.isTouch)
    				{
    					g.setColor(11152401);
    					g.fillRect(xSkill + xHP + 2, yHP - 10 + 6, 20, 10);
    					mFont.tahoma_7_white.drawString(g, "*", xSkill + xHP + 12, yHP - 8 + 6, mFont.CENTER);
    				}
    				int num5 = (Main.isPC ? array.Length : ((!GameCanvas.isTouch) ? array.Length : nSkill));
    				for (int i = 0; i < num5; i++)
    				{
    					if (Main.isPC)
    					{
    						string[] array2 = (TField.isQwerty ? new string[10] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" } : new string[5] { "7", "8", "9", "10", "11" });
    						int num6 = -13;
    						if (num5 > 5 && i < 5)
    						{
    							num6 = 27;
    						}
    						mFont.tahoma_7b_dark.drawString(g, array2[i], xSkill + xS[i] + 14, yS[i] + num6, mFont.CENTER);
    						mFont.tahoma_7b_white.drawString(g, array2[i], xSkill + xS[i] + 14, yS[i] + num6 + 1, mFont.CENTER);
    					}
    					else if (!GameCanvas.isTouch)
    					{
    						string[] array3 = (TField.isQwerty ? new string[5] { "Q", "W", "E", "R", "T" } : new string[5] { "7", "8", "9", "1", "3" });
    						g.setColor(11152401);
    						g.fillRect(xSkill + xS[i] + 2, yS[i] - 10 + 8, 20, 10);
    						mFont.tahoma_7_white.drawString(g, array3[i], xSkill + xS[i] + 12, yS[i] - 10 + 6, mFont.CENTER);
    					}
    					Skill skill = array[i];
    					if (skill != Char.myCharz().myskill)
    					{
    						g.drawImage(imgSkill, xSkill + xS[i] - 1, yS[i] - 1, 0);
    					}
    					if (skill == null)
    					{
    						continue;
    					}
    					if (skill == Char.myCharz().myskill)
    					{
    						g.drawImage(imgSkill2, xSkill + xS[i] - 1, yS[i] - 1, 0);
    						if (GameCanvas.isTouch && !Main.isPC)
    						{
    							g.drawRegion(Mob.imgHP, 0, 12, 9, 6, 0, xSkill + xS[i] + 8, yS[i] - 7, 0);
    						}
    					}
    					skill.paint(xSkill + xS[i] + 13, yS[i] + 13, g);
    					if ((i == selectedIndexSkill && !isPaintUI() && GameCanvas.gameTick % 10 > 5) || i == keyTouchSkill)
    					{
    						g.drawImage(ItemMap.imageFlare, xSkill + xS[i] + 13, yS[i] + 14, 3);
    					}
    				}
    			}
    			paintGamePad(g);
    		}
    	}
    
    	public void paintOpen(mGraphics g)
    	{
    		if (isstarOpen)
    		{
    			g.translate(-g.getTranslateX(), -g.getTranslateY());
    			g.fillRect(0, 0, GameCanvas.w, moveUp);
    			g.setColor(10275899);
    			g.fillRect(0, moveUp - 1, GameCanvas.w, 1);
    			g.fillRect(0, moveDow + 1, GameCanvas.w, 1);
    		}
    	}
    
    	public static void startFlyText(string flyString, int x, int y, int dx, int dy, int color)
    	{
    		int num = -1;
    		for (int i = 0; i < 5; i++)
    		{
    			if (flyTextState[i] == -1)
    			{
    				num = i;
    				break;
    			}
    		}
    		if (num == -1)
    		{
    			return;
    		}
    		flyTextColor[num] = color;
    		flyTextString[num] = flyString;
    		flyTextX[num] = x;
    		flyTextY[num] = y;
    		flyTextDx[num] = dx;
    		flyTextDy[num] = ((dy >= 0) ? 5 : (-5));
    		flyTextState[num] = 0;
    		flyTime[num] = 0;
    		flyTextYTo[num] = 10;
    		for (int j = 0; j < 5; j++)
    		{
    			if (flyTextState[j] != -1 && num != j && flyTextDy[num] < 0 && Res.abs(flyTextX[num] - flyTextX[j]) <= 20 && flyTextYTo[num] == flyTextYTo[j])
    			{
    				flyTextYTo[num] += 10;
    			}
    		}
    	}
    
    	public static void updateFlyText()
    	{
    		for (int i = 0; i < 5; i++)
    		{
    			if (flyTextState[i] == -1)
    			{
    				continue;
    			}
    			if (flyTextState[i] > flyTextYTo[i])
    			{
    				flyTime[i]++;
    				if (flyTime[i] == 25)
    				{
    					flyTime[i] = 0;
    					flyTextState[i] = -1;
    					flyTextYTo[i] = 0;
    					flyTextDx[i] = 0;
    					flyTextX[i] = 0;
    				}
    			}
    			else
    			{
    				flyTextState[i] += Res.abs(flyTextDy[i]);
    				flyTextX[i] += flyTextDx[i];
    				flyTextY[i] += flyTextDy[i];
    			}
    		}
    	}
    
    	public static void loadSplash()
    	{
    		if (imgSplash == null)
    		{
    			imgSplash = new Image[3];
    			for (int i = 0; i < 3; i++)
    			{
    				imgSplash[i] = GameCanvas.loadImage("/e/sp" + i + ".png");
    			}
    		}
    		splashX = new int[2];
    		splashY = new int[2];
    		splashState = new int[2];
    		splashF = new int[2];
    		splashDir = new int[2];
    		splashState[0] = (splashState[1] = -1);
    	}
    
    	public static bool startSplash(int x, int y, int dir)
    	{
    		int num = ((splashState[0] != -1) ? 1 : 0);
    		if (splashState[num] != -1)
    		{
    			return false;
    		}
    		splashState[num] = 0;
    		splashDir[num] = dir;
    		splashX[num] = x;
    		splashY[num] = y;
    		return true;
    	}
    
    	public static void updateSplash()
    	{
    		for (int i = 0; i < 2; i++)
    		{
    			if (splashState[i] != -1)
    			{
    				splashState[i]++;
    				splashX[i] += splashDir[i] << 2;
    				splashY[i]--;
    				if (splashState[i] >= 6)
    				{
    					splashState[i] = -1;
    				}
    				else
    				{
    					splashF[i] = (splashState[i] >> 1) % 3;
    				}
    			}
    		}
    	}
    
    	public static void paintSplash(mGraphics g)
    	{
    		if (splashState == null || imgSplash == null || splashF == null || splashX == null || splashY == null || splashDir == null)
    		{
    			return;
    		}
    		for (int i = 0; i < 2; i++)
    		{
    			if (splashState[i] != -1)
    			{
    				if (splashDir[i] == 1)
    				{
    					g.drawImage(imgSplash[splashF[i]], splashX[i], splashY[i], 3);
    				}
    				else
    				{
    					g.drawRegion(imgSplash[splashF[i]], 0, 0, mGraphics.getImageWidth(imgSplash[splashF[i]]), mGraphics.getImageHeight(imgSplash[splashF[i]]), 2, splashX[i], splashY[i], 3);
    				}
    			}
    		}
    	}
    
    	private void loadInforBar()
    	{
    		imgScrW = 84;
    		hpBarW = 66L;
    		mpBarW = 59;
    		hpBarX = 52;
    		hpBarY = 10;
    		spBarW = 61;
    		expBarW = gW - 61;
    	}
    
    	public void updateSS()
    	{
    		if (indexMenu != -1)
    		{
    			if (cmySK != cmtoYSK)
    			{
    				cmvySK = cmtoYSK - cmySK << 2;
    				cmdySK += cmvySK;
    				cmySK += cmdySK >> 4;
    				cmdySK &= 15;
    			}
    			if (Math.abs(cmtoYSK - cmySK) < 15 && cmySK < 0)
    			{
    				cmtoYSK = 0;
    			}
    			if (Math.abs(cmtoYSK - cmySK) < 15 && cmySK > cmyLimSK)
    			{
    				cmtoYSK = cmyLimSK;
    			}
    		}
    	}
    
    	public void updateKeyAlert()
    	{
    		if (!isPaintAlert || GameCanvas.currentDialog != null)
    		{
    			return;
    		}
    		bool flag = false;
    		if (GameCanvas.keyPressed[Key.NUM8])
    		{
    			indexRow++;
    			if (indexRow >= texts.size())
    			{
    				indexRow = 0;
    			}
    			flag = true;
    		}
    		else if (GameCanvas.keyPressed[Key.NUM2])
    		{
    			indexRow--;
    			if (indexRow < 0)
    			{
    				indexRow = texts.size() - 1;
    			}
    			flag = true;
    		}
    		if (flag)
    		{
    			scrMain.moveTo(indexRow * scrMain.ITEM_SIZE);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    		}
    		if (GameCanvas.isTouch)
    		{
    			ScrollResult scrollResult = scrMain.updateKey();
    			if (scrollResult.isDowning || scrollResult.isFinish)
    			{
    				indexRow = scrollResult.selected;
    				flag = true;
    			}
    		}
    		if (!flag || indexRow < 0 || indexRow >= texts.size())
    		{
    			return;
    		}
    		string text = (string)texts.elementAt(indexRow);
    		int num = -1;
    		fnick = null;
    		alertURL = null;
    		center = null;
    		ChatTextField.gI().center = null;
    		if ((num = text.IndexOf("http://")) >= 0)
    		{
    			Cout.println("currentLine: " + text);
    			alertURL = text.Substring(num);
    			center = new Command(mResources.open_link, 12000);
    			if (!GameCanvas.isTouch)
    			{
    				ChatTextField.gI().center = new Command(mResources.open_link, null, 12000, null);
    			}
    		}
    		else
    		{
    			if ((num = text.IndexOf("@")) < 0)
    			{
    				return;
    			}
    			string text2 = text.Substring(2);
    			text2 = text2.Trim();
    			num = text2.IndexOf("@");
    			string text3 = text2.Substring(num);
    			int num2 = -1;
    			num2 = text3.IndexOf(" ");
    			num2 = ((num2 > 0) ? (num2 + num) : (num + text3.Length));
    			fnick = text2.Substring(num + 1, num2);
    			if (!fnick.Equals(string.Empty) && !fnick.Equals(Char.myCharz().cName))
    			{
    				center = new Command(mResources.SELECT, 12009, fnick);
    				if (!GameCanvas.isTouch)
    				{
    					ChatTextField.gI().center = new Command(mResources.SELECT, null, 12009, fnick);
    				}
    			}
    			else
    			{
    				fnick = null;
    				center = null;
    			}
    		}
    	}
    
    	public bool isPaintPopup()
    	{
    		if (isPaintItemInfo || isPaintInfoMe || isPaintStore || isPaintWeapon || isPaintNonNam || isPaintNonNu || isPaintAoNam || isPaintAoNu || isPaintGangTayNam || isPaintGangTayNu || isPaintQuanNam || isPaintQuanNu || isPaintGiayNam || isPaintGiayNu || isPaintLien || isPaintNhan || isPaintNgocBoi || isPaintPhu || isPaintStack || isPaintStackLock || isPaintGrocery || isPaintGroceryLock || isPaintUpGrade || isPaintConvert || isPaintSplit || isPaintUpPearl || isPaintBox || isPaintTrade || isPaintAlert || isPaintZone || isPaintTeam || isPaintClan || isPaintFindTeam || isPaintTask || isPaintFriend || isPaintEnemies || isPaintCharInMap || isPaintMessage)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isNotPaintTouchControl()
    	{
    		if (!GameCanvas.isTouchControl && GameCanvas.currentScreen == gI())
    		{
    			return true;
    		}
    		if (!GameCanvas.isTouch)
    		{
    			return true;
    		}
    		if (ChatTextField.gI().isShow)
    		{
    			return true;
    		}
    		if (InfoDlg.isShow)
    		{
    			return true;
    		}
    		if (GameCanvas.currentDialog != null || ChatPopup.currChatPopup != null || GameCanvas.menu.showMenu || GameCanvas.panel.isShow || isPaintPopup())
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isPaintUI()
    	{
    		if (isPaintStore || isPaintWeapon || isPaintNonNam || isPaintNonNu || isPaintAoNam || isPaintAoNu || isPaintGangTayNam || isPaintGangTayNu || isPaintQuanNam || isPaintQuanNu || isPaintGiayNam || isPaintGiayNu || isPaintLien || isPaintNhan || isPaintNgocBoi || isPaintPhu || isPaintStack || isPaintStackLock || isPaintGrocery || isPaintGroceryLock || isPaintUpGrade || isPaintConvert || isPaintSplit || isPaintUpPearl || isPaintBox || isPaintTrade)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isOpenUI()
    	{
    		if (isPaintItemInfo || isPaintInfoMe || isPaintStore || isPaintNonNam || isPaintNonNu || isPaintAoNam || isPaintAoNu || isPaintGangTayNam || isPaintGangTayNu || isPaintQuanNam || isPaintQuanNu || isPaintGiayNam || isPaintGiayNu || isPaintLien || isPaintNhan || isPaintNgocBoi || isPaintPhu || isPaintWeapon || isPaintStack || isPaintStackLock || isPaintGrocery || isPaintGroceryLock || isPaintUpGrade || isPaintConvert || isPaintUpPearl || isPaintBox || isPaintSplit || isPaintTrade)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public static void setPopupSize(int w, int h)
    	{
    		if (GameCanvas.w == 128 || GameCanvas.h <= 208)
    		{
    			w = 126;
    			h = 160;
    		}
    		indexTitle = 0;
    		popupW = w;
    		popupH = h;
    		popupX = gW2 - w / 2;
    		popupY = gH2 - h / 2;
    		if (GameCanvas.isTouch && !isPaintZone && !isPaintTeam && !isPaintClan && !isPaintCharInMap && !isPaintFindTeam && !isPaintFriend && !isPaintEnemies && !isPaintTask && !isPaintMessage)
    		{
    			if (GameCanvas.h <= 240)
    			{
    				popupY -= 10;
    			}
    			if (GameCanvas.isTouch && !GameCanvas.isTouchControlSmallScreen && GameCanvas.currentScreen is GameScr)
    			{
    				popupW = 310;
    				popupX = gW / 2 - popupW / 2;
    				if (isPaintInfoMe && indexMenu > 0)
    				{
    					popupW = w;
    					popupX = gW2 - w / 2;
    				}
    			}
    		}
    		if (popupY < -10)
    		{
    			popupY = -10;
    		}
    		if (GameCanvas.h > 208 && popupY < 0)
    		{
    			popupY = 0;
    		}
    		if (GameCanvas.h == 208 && popupY < 10)
    		{
    			popupY = 10;
    		}
    	}
    
    	public static void loadImg()
    	{
    		TileMap.loadTileImage();
    	}
    
    	public void paintTitle(mGraphics g, string title, bool arrow)
    	{
    		int num = 0;
    		num = gW / 2;
    		g.setColor(Paint.COLORDARK);
    		g.fillRoundRect(num - mFont.tahoma_8b.getWidth(title) / 2 - 12, popupY + 4, mFont.tahoma_8b.getWidth(title) + 22, 24, 6, 6);
    		if ((indexTitle == 0 || GameCanvas.isTouch) && arrow)
    		{
    			SmallImage.drawSmallImage(g, 989, num - mFont.tahoma_8b.getWidth(title) / 2 - 15 - 7 - ((GameCanvas.gameTick % 8 <= 3) ? 2 : 0), popupY + 16, 2, StaticObj.VCENTER_HCENTER);
    			SmallImage.drawSmallImage(g, 989, num + mFont.tahoma_8b.getWidth(title) / 2 + 15 + 5 + ((GameCanvas.gameTick % 8 <= 3) ? 2 : 0), popupY + 16, 0, StaticObj.VCENTER_HCENTER);
    		}
    		if (indexTitle == 0)
    		{
    			g.setColor(Paint.COLORFOCUS);
    		}
    		else
    		{
    			g.setColor(Paint.COLORBORDER);
    		}
    		g.drawRoundRect(num - mFont.tahoma_8b.getWidth(title) / 2 - 12, popupY + 4, mFont.tahoma_8b.getWidth(title) + 22, 24, 6, 6);
    		mFont.tahoma_8b.drawString(g, title, num, popupY + 9, 2);
    	}
    
    	public static int getTaskMapId()
    	{
    		int num = 0;
    		if (Char.myCharz().taskMaint == null)
    		{
    			return -1;
    		}
    		return mapTasks[Char.myCharz().taskMaint.index];
    	}
    
    	public static sbyte getTaskNpcId()
    	{
    		sbyte result = 0;
    		if (Char.myCharz().taskMaint == null)
    		{
    			result = -1;
    		}
    		else if (Char.myCharz().taskMaint.index <= tasks.Length - 1)
    		{
    			result = (sbyte)tasks[Char.myCharz().taskMaint.index];
    		}
    		return result;
    	}
    
    	public void refreshTeam()
    	{
    	}
    
    	public void onChatFromMe(string text, string to)
    	{
    		Res.outz("CHAT");
    		if (!isPaintMessage || GameCanvas.isTouch)
    		{
    			ChatTextField.gI().isShow = false;
    		}
    		if (to.Equals(mResources.chat_player))
    		{
    			if (info2.playerID != Char.myCharz().charID)
    			{
    				Service.gI().chatPlayer(text, info2.playerID);
    			}
    		}
    		else if (!text.Equals(string.Empty))
    		{
    			Service.gI().chat(text);
    		}
    	}
    
    	public void onCancelChat()
    	{
    		if (isPaintMessage)
    		{
    			isPaintMessage = false;
    			ChatTextField.gI().center = null;
    		}
    	}
    
    	public void openWeb(string strLeft, string strRight, string url, string title, string str)
    	{
    		isPaintAlert = true;
    		isLockKey = true;
    		indexRow = 0;
    		setPopupSize(175, 200);
    		textsTitle = title;
    		texts = mFont.tahoma_7.splitFontVector(str, popupW - 30);
    		center = null;
    		left = new Command(strLeft, 11068, url);
    		right = new Command(strRight, 11069);
    	}
    
    	public void sendSms(string strLeft, string strRight, short port, string syntax, string title, string str)
    	{
    		isPaintAlert = true;
    		isLockKey = true;
    		indexRow = 0;
    		setPopupSize(175, 200);
    		textsTitle = title;
    		texts = mFont.tahoma_7.splitFontVector(str, popupW - 30);
    		center = null;
    		MyVector myVector = new MyVector();
    		myVector.addElement(string.Empty + port);
    		myVector.addElement(syntax);
    		left = new Command(strLeft, 11074);
    		right = new Command(strRight, 11075);
    	}
    }
}

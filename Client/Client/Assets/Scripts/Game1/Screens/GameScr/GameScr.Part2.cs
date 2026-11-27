namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameScr : mScreen, IChatable
    {

    	public static void updateUIPositions()
    	{
    		if (GameCanvas.isTouch)
    		{
    			yTouchBar = gH - 88;
    			xC = gW - 40;
    			yC = 2;
    			if (GameCanvas.w <= 240)
    			{
    				xC = gW - 35;
    				yC = 5;
    			}
    			xF = gW - 55;
    			yF = yTouchBar + 35;
    			xTG = gW - 37;
    			yTG = yTouchBar - 1;
    			if (GameCanvas.w >= 450)
    			{
    				yTG -= 12;
    				yHP -= 7;
    				xF -= 10;
    				yF -= 5;
    				xTG -= 10;
    			}
    			if (isAnalog != 0)
    			{
    				setTouchBtn();
    			}
    		}
    		setSkillBarPosition();
    	}

    	public static void setSkillBarPosition()
    	{
    		Skill[] array = ((!GameCanvas.isTouch) ? keySkill : onScreenSkill);
    		xS = new int[array.Length];
    		yS = new int[array.Length];
    		if (GameCanvas.isTouchControlSmallScreen && isUseTouch)
    		{
    			xSkill = 23;
    			ySkill = 52;
    			padSkill = 5;
    			for (int i = 0; i < xS.Length; i++)
    			{
    				xS[i] = i * (25 + padSkill);
    				yS[i] = ySkill;
    				if (xS.Length > 5 && i >= xS.Length / 2)
    				{
    					xS[i] = (i - xS.Length / 2) * (25 + padSkill);
    					yS[i] = ySkill - 32;
    				}
    			}
    			xHP = array.Length * (25 + padSkill);
    			yHP = ySkill;
    		}
    		else
    		{
    			wSkill = 30;
    			if (GameCanvas.w <= 320)
    			{
    				ySkill = gH - wSkill - 6;
    				xSkill = gW2 - array.Length * wSkill / 2 - 25;
    			}
    			else
    			{
    				wSkill = 40;
    				xSkill = 10;
    				ySkill = GameCanvas.h - wSkill + 7;
    			}
    			for (int j = 0; j < xS.Length; j++)
    			{
    				xS[j] = j * wSkill;
    				yS[j] = ySkill;
    				if (xS.Length > 5 && j >= xS.Length / 2)
    				{
    					xS[j] = (j - xS.Length / 2) * wSkill;
    					yS[j] = ySkill - 32;
    				}
    			}
    			xHP = array.Length * wSkill;
    			yHP = ySkill;
    		}
    		if (!GameCanvas.isTouch)
    		{
    			return;
    		}
    		xSkill = 17;
    		ySkill = GameCanvas.h - 40;
    		if (gamePad.isSmallGamePad && isAnalog == 1)
    		{
    			xHP = array.Length * wSkill;
    			yHP = ySkill;
    		}
    		else
    		{
    			xHP = GameCanvas.w - 45;
    			yHP = GameCanvas.h - 45;
    		}
    		setTouchBtn();
    		for (int k = 0; k < xS.Length; k++)
    		{
    			xS[k] = k * wSkill;
    			yS[k] = ySkill;
    			if (xS.Length > 5 && k >= xS.Length / 2)
    			{
    				xS[k] = (k - xS.Length / 2) * wSkill;
    				yS[k] = ySkill - 32;
    			}
    		}
    	}
    
    	private static void updateCamera()
    	{
    		if (isPaintOther)
    		{
    			return;
    		}
    		if (cmx != cmtoX || cmy != cmtoY)
    		{
    			cmvx = cmtoX - cmx << 2;
    			cmvy = cmtoY - cmy << 2;
    			cmdx += cmvx;
    			cmx += cmdx >> 4;
    			cmdx &= 15;
    			cmdy += cmvy;
    			cmy += cmdy >> 4;
    			cmdy &= 15;
    			int mapW = (TileMap.tmw - 1) * TileMap.size;
    			int mapH = (TileMap.tmh - 1) * TileMap.size;
    			if (mapW <= gW)
    			{
    				cmx = (cmtoX = (TileMap.pxw - gW) / 2);
    			}
    			else
    			{
    				if (cmx < 24)
    				{
    					cmx = 24;
    				}
    				if (cmx > cmxLim)
    				{
    					cmx = cmxLim;
    				}
    			}
    			if (mapH <= gH)
    			{
    				cmy = (cmtoY = mapH - gH);
    			}
    			else
    			{
    				if (cmy < 0)
    				{
    					cmy = 0;
    				}
    				if (cmy > cmyLim)
    				{
    					cmy = cmyLim;
    				}
    			}
    		}
    		gssx = cmx / TileMap.size - 1;
    		if (gssx < 0)
    		{
    			gssx = 0;
    		}
    		gssy = cmy / TileMap.size;
    		gssxe = gssx + gssw;
    		gssye = gssy + gssh;
    		if (gssy < 0)
    		{
    			gssy = 0;
    		}
    		if (gssxe > TileMap.tmw)
    		{
    			gssxe = TileMap.tmw;
    		}
    		if (gssye > TileMap.tmh)
    		{
    			gssye = TileMap.tmh;
    		}
    		TileMap.gssx = (Char.myCharz().cx - 2 * gW) / TileMap.size;
    		if (TileMap.gssx < 0)
    		{
    			TileMap.gssx = 0;
    		}
    		TileMap.gssxe = TileMap.gssx + TileMap.countx;
    		if (TileMap.gssxe > TileMap.tmw)
    		{
    			TileMap.gssxe = TileMap.tmw;
    			TileMap.gssx = TileMap.gssxe - TileMap.countx;
    		}
    		if (TileMap.gssx < 0)
    		{
    			TileMap.gssx = 0;
    		}
    		TileMap.gssy = (Char.myCharz().cy - 2 * gH) / TileMap.size;
    		if (TileMap.gssy < 0)
    		{
    			TileMap.gssy = 0;
    		}
    		TileMap.gssye = TileMap.gssy + TileMap.county;
    		if (TileMap.gssye > TileMap.tmh)
    		{
    			TileMap.gssye = TileMap.tmh;
    			TileMap.gssy = TileMap.gssye - TileMap.county;
    		}
    		if (TileMap.gssy < 0)
    		{
    			TileMap.gssy = 0;
    		}
    		scrMain.updatecm();
    		scrInfo.updatecm();
    	}
    
    	public bool testAct()
    	{
    		for (sbyte b = 2; b < 9; b += 2)
    		{
    			if (GameCanvas.keyHold[b])
    			{
    				return false;
    			}
    		}
    		return true;
    	}
    
    	public void clanInvite(string strInvite, int clanID, int code)
    	{
    		ClanObject clanObject = new ClanObject();
    		clanObject.code = code;
    		clanObject.clanID = clanID;
    		startYesNoPopUp(strInvite, new Command(mResources.YES, 12002, clanObject), new Command(mResources.NO, 12003, clanObject));
    	}
    
    	public void playerMenu(Char c)
    	{
    		auto = 0;
    		GameCanvas.clearKeyHold();
    		if (Char.myCharz().charFocus.charID < 0 || Char.myCharz().charID < 0)
    		{
    			return;
    		}
    		MyVector vPlayerMenu = GameCanvas.panel.vPlayerMenu;
    		if (vPlayerMenu.size() > 0)
    		{
    			return;
    		}
    		if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId > 1)
    		{
    			vPlayerMenu.addElement(new Command(mResources.make_friend, 11112, Char.myCharz().charFocus));
    			vPlayerMenu.addElement(new Command(mResources.trade, 11113, Char.myCharz().charFocus));
    		}
    		if (Char.myCharz().clan != null && Char.myCharz().role < 2 && Char.myCharz().charFocus.clanID == -1)
    		{
    			vPlayerMenu.addElement(new Command(mResources.CHAR_ORDER[4], 110391));
    		}
    		if (Char.myCharz().charFocus.statusMe != 14 && Char.myCharz().charFocus.statusMe != 5)
    		{
    			if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId >= 14)
    			{
    				vPlayerMenu.addElement(new Command(mResources.CHAR_ORDER[0], 2003));
    			}
    		}
    		else if (Char.myCharz().myskill.template.type != 4)
    		{
    		}
    		if (Char.myCharz().clan != null && Char.myCharz().clan.ID == Char.myCharz().charFocus.clanID && Char.myCharz().charFocus.statusMe != 14 && Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId >= 14)
    		{
    			vPlayerMenu.addElement(new Command(mResources.CHAR_ORDER[1], 2004));
    		}
    		int num = Char.myCharz().nClass.skillTemplates.Length;
    		for (int i = 0; i < num; i++)
    		{
    			SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[i];
    			Skill skill = Char.myCharz().getSkill(skillTemplate);
    			if (skill != null && skillTemplate.isBuffToPlayer() && skill.point >= 1)
    			{
    				vPlayerMenu.addElement(new Command(skillTemplate.name, 12004, skill));
    			}
    		}
    	}
    
    	public bool isAttack()
    	{
    		if (checkClickToBotton(Char.myCharz().charFocus))
    		{
    			return false;
    		}
    		if (checkClickToBotton(Char.myCharz().mobFocus))
    		{
    			return false;
    		}
    		if (checkClickToBotton(Char.myCharz().npcFocus))
    		{
    			return false;
    		}
    		if (ChatTextField.gI().isShow)
    		{
    			return false;
    		}
    		if (InfoDlg.isLock || Char.myCharz().isLockAttack || Char.isLockKey)
    		{
    			return false;
    		}
    		if (Char.myCharz().myskill != null && Char.myCharz().myskill.template.id == 6 && Char.myCharz().itemFocus != null)
    		{
    			pickItem();
    			return false;
    		}
    		if (Char.myCharz().myskill != null && Char.myCharz().myskill.template.type == 2 && Char.myCharz().npcFocus == null && Char.myCharz().myskill.template.id != 6)
    		{
    			if (!checkSkillValid())
    			{
    				return false;
    			}
    			return true;
    		}
    		if (Char.myCharz().skillPaint != null || (Char.myCharz().mobFocus == null && Char.myCharz().npcFocus == null && Char.myCharz().charFocus == null && Char.myCharz().itemFocus == null))
    		{
    			return false;
    		}
    		if (Char.myCharz().mobFocus != null)
    		{
    			if (Char.myCharz().mobFocus.isBigBoss() && Char.myCharz().mobFocus.status == 4)
    			{
    				Char.myCharz().mobFocus = null;
    				Char.myCharz().currentMovePoint = null;
    			}
    			isAutoPlay = true;
    			if (!isMeCanAttackMob(Char.myCharz().mobFocus))
    			{
    				Res.outz("can not attack");
    				return false;
    			}
    			if (mobCapcha != null)
    			{
    				return false;
    			}
    			if (Char.myCharz().myskill == null)
    			{
    				return false;
    			}
    			if (Char.myCharz().isSelectingSkillUseAlone())
    			{
    				return false;
    			}
    			int num = -1;
    			int num2 = Res.abs(Char.myCharz().cx - cmx) * mGraphics.zoomLevel;
    			if (Char.myCharz().charFocus != null)
    			{
    				num = Res.abs(Char.myCharz().cx - Char.myCharz().charFocus.cx) * mGraphics.zoomLevel;
    			}
    			else if (Char.myCharz().mobFocus != null)
    			{
    				num = Res.abs(Char.myCharz().cx - Char.myCharz().mobFocus.x) * mGraphics.zoomLevel;
    			}
    			if ((Char.myCharz().mobFocus.status == 1 || Char.myCharz().mobFocus.status == 0 || Char.myCharz().myskill.template.type == 4 || num == -1 || num > num2) && Char.myCharz().myskill.template.type == 4)
    			{
    				if (Char.myCharz().mobFocus.x < Char.myCharz().cx)
    				{
    					Char.myCharz().cdir = -1;
    				}
    				else
    				{
    					Char.myCharz().cdir = 1;
    				}
    				doSelectSkill(Char.myCharz().myskill, true);
    			}
    			if (!checkSkillValid())
    			{
    				return false;
    			}
    			if (Char.myCharz().cx < Char.myCharz().mobFocus.getX())
    			{
    				Char.myCharz().cdir = 1;
    			}
    			else
    			{
    				Char.myCharz().cdir = -1;
    			}
    			int num3 = Math.abs(Char.myCharz().cx - Char.myCharz().mobFocus.getX());
    			int num4 = Math.abs(Char.myCharz().cy - Char.myCharz().mobFocus.getY());
    			Char.myCharz().cvx = 0;
    			int reachX = Char.myCharz().myskill.dx;
    			int reachY = Char.myCharz().myskill.dy;
    			if (Char.myCharz().myskill.template.type == 1)
    			{
    				if (reachX < 60) reachX = 60;
    				if (reachY < 60) reachY = 60;
    			}
    			if (num3 <= reachX && num4 <= reachY)
    			{
    				if (Char.myCharz().myskill.template.id == 20)
    				{
    					return true;
    				}
    				if (num4 > num3 && Res.abs(Char.myCharz().cy - Char.myCharz().mobFocus.getY()) > 40 && Char.myCharz().mobFocus.getTemplate().type == 4)
    				{
    					Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().cx + Char.myCharz().cdir, Char.myCharz().mobFocus.getY());
    					Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    					GameCanvas.clearKeyHold();
    					GameCanvas.clearKeyPressed();
    					return false;
    				}
    				int num5 = 20;
    				bool flag = false;
    				if (Char.myCharz().mobFocus is BigBoss || Char.myCharz().mobFocus is BigBoss2)
    				{
    					flag = true;
    				}
    				if (Char.myCharz().myskill.dx > 100)
    				{
    					num5 = 60;
    					if (num3 < 20)
    					{
    						Char.myCharz().createShadow(Char.myCharz().cx, Char.myCharz().cy, 10);
    					}
    				}
    				bool flag2 = false;
    				if ((TileMap.tileTypeAtPixel(Char.myCharz().cx, Char.myCharz().cy + 3) & 2) == 2)
    				{
    					int num6 = ((Char.myCharz().cx > Char.myCharz().mobFocus.getX()) ? 1 : (-1));
    					if ((TileMap.tileTypeAtPixel(Char.myCharz().mobFocus.getX() + num5 * num6, Char.myCharz().cy + 3) & 2) != 2)
    					{
    						flag2 = true;
    					}
    				}
    				if (num3 <= num5 && !flag2)
    				{
    					if (Char.myCharz().cx > Char.myCharz().mobFocus.getX())
    					{
    						int num7 = Char.myCharz().mobFocus.getX() + num5 + (flag ? 30 : 0);
    						int i = Char.myCharz().mobFocus.getX();
    						bool flag3 = false;
    						for (; i < num7; i += 24)
    						{
    							if (TileMap.tileTypeAtPixel(i, Char.myCharz().cy + 3) == 8 || TileMap.tileTypeAtPixel(i, Char.myCharz().cy + 3) == 4)
    							{
    								flag3 = true;
    								break;
    							}
    						}
    						if (flag3)
    						{
    							Char.myCharz().cx = i - 24;
    						}
    						else
    						{
    							Char.myCharz().cx = num7;
    						}
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						int num8 = Char.myCharz().mobFocus.getX() - num5 - (flag ? 30 : 0);
    						int num9 = Char.myCharz().mobFocus.getX();
    						bool flag4 = false;
    						while (num9 > num8)
    						{
    							if (TileMap.tileTypeAtPixel(num9, Char.myCharz().cy + 3) == 8 || TileMap.tileTypeAtPixel(num9, Char.myCharz().cy + 3) == 4)
    							{
    								flag4 = true;
    								break;
    							}
    							num9 -= 24;
    						}
    						if (flag4)
    						{
    							Char.myCharz().cx = num9 + 24;
    						}
    						else
    						{
    							Char.myCharz().cx = num8;
    						}
    						Char.myCharz().cdir = 1;
    					}
    					Service.gI().charMove();
    				}
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    				return true;
    			}
    			bool flag5 = false;
    			if (Char.myCharz().mobFocus is BigBoss || Char.myCharz().mobFocus is BigBoss2)
    			{
    				flag5 = true;
    			}
    			int approachDist = System.Math.Min(reachX - 15, 30);
    			if (approachDist < 20) approachDist = 20;
    			int num10 = ((!flag5) ? approachDist : 50) * ((Char.myCharz().cx > Char.myCharz().mobFocus.getX()) ? 1 : (-1));
    			Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().mobFocus.getX() + num10, Char.myCharz().mobFocus.getY());
    			Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    			return false;
    		}
    		if (Char.myCharz().npcFocus != null)
    		{
    			if (Char.myCharz().npcFocus.isHide)
    			{
    				return false;
    			}
    			if (Char.myCharz().cx < Char.myCharz().npcFocus.cx)
    			{
    				Char.myCharz().cdir = 1;
    			}
    			else
    			{
    				Char.myCharz().cdir = -1;
    			}
    			if (Char.myCharz().cx < Char.myCharz().npcFocus.cx)
    			{
    				Char.myCharz().npcFocus.cdir = -1;
    			}
    			else
    			{
    				Char.myCharz().npcFocus.cdir = 1;
    			}
    			int num11 = Math.abs(Char.myCharz().cx - Char.myCharz().npcFocus.cx);
    			int num12 = Math.abs(Char.myCharz().cy - Char.myCharz().npcFocus.cy);
    			if (num12 > 40)
    			{
    				Char.myCharz().cy = Char.myCharz().npcFocus.cy - 40;
    			}
    			if (num11 < 60)
    			{
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    				if (tMenuDelay == 0)
    				{
    					if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId == 0)
    					{
    						if (Char.myCharz().taskMaint.index < 4 && Char.myCharz().npcFocus.template.npcTemplateId == 4)
    						{
    							return false;
    						}
    						if (Char.myCharz().taskMaint.index < 3 && Char.myCharz().npcFocus.template.npcTemplateId == 3)
    						{
    							return false;
    						}
    					}
    					tMenuDelay = 50;
    					InfoDlg.showWait();
    					Service.gI().charMove();
    					Service.gI().openMenu(Char.myCharz().npcFocus.template.npcTemplateId);
    				}
    			}
    			else
    			{
    				int num13 = (20 + Res.r.nextInt(20)) * ((Char.myCharz().cx > Char.myCharz().npcFocus.cx) ? 1 : (-1));
    				Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().npcFocus.cx + num13, Char.myCharz().cy);
    				Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    			}
    			return false;
    		}
    		if (Char.myCharz().charFocus != null)
    		{
    			if (mobCapcha != null)
    			{
    				return false;
    			}
    			if (Char.myCharz().cx < Char.myCharz().charFocus.cx)
    			{
    				Char.myCharz().cdir = 1;
    			}
    			else
    			{
    				Char.myCharz().cdir = -1;
    			}
    			int num14 = Math.abs(Char.myCharz().cx - Char.myCharz().charFocus.cx);
    			int num15 = Math.abs(Char.myCharz().cy - Char.myCharz().charFocus.cy);
    			if (Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus) || Char.myCharz().isSelectingSkillBuffToPlayer())
    			{
    				if (Char.myCharz().myskill == null)
    				{
    					return false;
    				}
    				if (!checkSkillValid())
    				{
    					return false;
    				}
    				if (Char.myCharz().cx < Char.myCharz().charFocus.cx)
    				{
    					Char.myCharz().cdir = 1;
    				}
    				else
    				{
    					Char.myCharz().cdir = -1;
    				}
    				Char.myCharz().cvx = 0;
    				if (num14 <= Char.myCharz().myskill.dx && num15 <= Char.myCharz().myskill.dy)
    				{
    					if (Char.myCharz().myskill.template.id == 20)
    					{
    						return true;
    					}
    					int num16 = 20;
    					if (Char.myCharz().myskill.dx > 60)
    					{
    						num16 = 60;
    						if (num14 < 20)
    						{
    							Char.myCharz().createShadow(Char.myCharz().cx, Char.myCharz().cy, 10);
    						}
    					}
    					bool flag6 = false;
    					if ((TileMap.tileTypeAtPixel(Char.myCharz().cx, Char.myCharz().cy + 3) & 2) == 2)
    					{
    						int num17 = ((Char.myCharz().cx > Char.myCharz().charFocus.cx) ? 1 : (-1));
    						if ((TileMap.tileTypeAtPixel(Char.myCharz().charFocus.cx + num16 * num17, Char.myCharz().cy + 3) & 2) != 2)
    						{
    							flag6 = true;
    						}
    					}
    					if (num14 <= num16 && !flag6)
    					{
    						if (Char.myCharz().cx > Char.myCharz().charFocus.cx)
    						{
    							Char.myCharz().cx = Char.myCharz().charFocus.cx + num16;
    							Char.myCharz().cdir = -1;
    						}
    						else
    						{
    							Char.myCharz().cx = Char.myCharz().charFocus.cx - num16;
    							Char.myCharz().cdir = 1;
    						}
    						Service.gI().charMove();
    					}
    					GameCanvas.clearKeyHold();
    					GameCanvas.clearKeyPressed();
    					return true;
    				}
    				int num18 = (Char.myCharz().myskill.dx - 20) * ((Char.myCharz().cx > Char.myCharz().charFocus.cx) ? 1 : (-1));
    				if (num14 <= Char.myCharz().myskill.dx)
    				{
    					num18 = 0;
    				}
    				Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().charFocus.cx + num18, Char.myCharz().charFocus.cy);
    				Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    				return false;
    			}
    			if (num14 < 60 && num15 < 40)
    			{
    				playerMenu(Char.myCharz().charFocus);
    				if (!GameCanvas.isTouch && Char.myCharz().charFocus.charID >= 0 && TileMap.mapID != 51 && TileMap.mapID != 52 && popUpYesNo == null)
    				{
    					GameCanvas.panel.setTypePlayerMenu(Char.myCharz().charFocus);
    					GameCanvas.panel.show();
    					Service.gI().getPlayerMenu(Char.myCharz().charFocus.charID);
    					Service.gI().messagePlayerMenu(Char.myCharz().charFocus.charID);
    				}
    			}
    			else
    			{
    				int num19 = (20 + Res.r.nextInt(20)) * ((Char.myCharz().cx > Char.myCharz().charFocus.cx) ? 1 : (-1));
    				Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().charFocus.cx + num19, Char.myCharz().charFocus.cy);
    				Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    			}
    			return false;
    		}
    		if (Char.myCharz().itemFocus != null)
    		{
    			pickItem();
    			return false;
    		}
    		return true;
    	}
    
    	public bool isMeCanAttackMob(Mob m)
    	{
    		if (m == null)
    		{
    			return false;
    		}
    		if (m.isMobMe)
    		{
    			if (Char.myCharz().mobMe != null && m.Equals(Char.myCharz().mobMe))
    			{
    				return false;
    			}
    			Char @char = findCharInMap(m.mobId);
    			if (@char == null)
    			{
    				return false;
    			}
    			if (@char.cTypePk == 5 || Char.myCharz().cTypePk == 5)
    			{
    				return true;
    			}
    			return Char.myCharz().isMeCanAttackOtherPlayer(@char);
    		}
    		return true;
    	}
    
    	private bool checkSkillValid()
    	{
    		if (Char.myCharz().myskill != null && ((Char.myCharz().myskill.template.manaUseType != 1 && Char.myCharz().cMP < Char.myCharz().myskill.manaUse) || (Char.myCharz().myskill.template.manaUseType == 1 && Char.myCharz().cMP < Char.myCharz().cMPFull * Char.myCharz().myskill.manaUse / 100)))
    		{
    			info1.addInfo(mResources.NOT_ENOUGH_MP, 0);
    			auto = 0;
    			return false;
    		}
    		if (Char.myCharz().myskill == null || (Char.myCharz().myskill.template.maxPoint > 0 && Char.myCharz().myskill.point == 0))
    		{
    			GameCanvas.startOKDlg(mResources.SKILL_FAIL);
    			return false;
    		}
    		return true;
    	}
    
    	private bool checkSkillValid2()
    	{
    		if (Char.myCharz().myskill != null && ((Char.myCharz().myskill.template.manaUseType != 1 && Char.myCharz().cMP < Char.myCharz().myskill.manaUse) || (Char.myCharz().myskill.template.manaUseType == 1 && Char.myCharz().cMP < Char.myCharz().cMPFull * Char.myCharz().myskill.manaUse / 100)))
    		{
    			return false;
    		}
    		if (Char.myCharz().myskill == null || (Char.myCharz().myskill.template.maxPoint > 0 && Char.myCharz().myskill.point == 0))
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public void resetButton()
    	{
    		GameCanvas.menu.showMenu = false;
    		ChatTextField.gI().close();
    		ChatTextField.gI().center = null;
    		isLockKey = false;
    		typeTrade = 0;
    		indexMenu = 0;
    		indexSelect = 0;
    		indexItemUse = -1;
    		indexRow = -1;
    		indexRowMax = 0;
    		indexTitle = 0;
    		typeTrade = (typeTradeOrder = 0);
    		mSystem.endKey();
    		if (Char.myCharz().cHP <= 0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5)
    		{
    			if (Char.myCharz().meDead)
    			{
    				cmdDead = new Command(mResources.DIES[0], 11038);
    				center = cmdDead;
    				Char.myCharz().cHP = 0;
    			}
    			isHaveSelectSkill = false;
    		}
    		else
    		{
    			isHaveSelectSkill = true;
    		}
    		scrMain.clear();
    	}
    
    	public override void keyPress(int keyCode)
    	{
    		base.keyPress(keyCode);
    	}
    }
}

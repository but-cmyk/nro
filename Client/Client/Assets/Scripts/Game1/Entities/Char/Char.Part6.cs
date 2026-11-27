namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    
    public partial class Char : IMapObject
    {
    
	public bool isCharBodyImageID(int id)
	{
		if (GameScr.parts == null || head < 0 || head >= GameScr.parts.Length || leg < 0 || leg >= GameScr.parts.Length || body < 0 || body >= GameScr.parts.Length)
		{
			return false;
		}
		Part part = GameScr.parts[head];
		Part part2 = GameScr.parts[leg];
		Part part3 = GameScr.parts[body];
		if (part == null || part2 == null || part3 == null || part.pi == null || part2.pi == null || part3.pi == null)
		{
			return false;
		}
		for (int i = 0; i < CharInfo.Length; i++)
		{
			if (CharInfo[i][0][0] < part.pi.Length && id == part.pi[CharInfo[i][0][0]].id)
			{
				return true;
			}
			if (CharInfo[i][1][0] < part2.pi.Length && id == part2.pi[CharInfo[i][1][0]].id)
			{
				return true;
			}
			if (CharInfo[i][2][0] < part3.pi.Length && id == part3.pi[CharInfo[i][2][0]].id)
			{
				return true;
			}
		}
		return false;
	}

	public void paintHead(mGraphics g, int cx, int cy, int look)
	{
		if (GameScr.parts == null || head < 0 || head >= GameScr.parts.Length)
		{
			return;
		}
		Part part = GameScr.parts[head];
		if (part == null || part.pi == null || CharInfo[0][0][0] >= part.pi.Length)
		{
			return;
		}
		SmallImage.drawSmallImage(g, part.pi[CharInfo[0][0][0]].id, cx, cy, (look != 0) ? 2 : 0, mGraphics.RIGHT | mGraphics.VCENTER);
	}

	public void paintHeadWithXY(mGraphics g, int x, int y, int look)
	{
		if (GameScr.parts == null || head < 0 || head >= GameScr.parts.Length)
		{
			return;
		}
		Part part = GameScr.parts[head];
		if (part == null || part.pi == null || CharInfo[0][0][0] >= part.pi.Length)
		{
			return;
		}
		SmallImage.drawSmallImage(g, part.pi[CharInfo[0][0][0]].id, x + CharInfo[0][0][1] + part.pi[CharInfo[0][0][0]].dx - 3, y + 3, look, mGraphics.LEFT | mGraphics.BOTTOM);
	}

	private long lastLogPaintErrorTime;
	private void logPaintErrorThrottled(string reason)
	{
		long now = mSystem.currentTimeMillis();
		if (now - lastLogPaintErrorTime > 3000)
		{
			lastLogPaintErrorTime = now;
			Cout.LogError($"[paintCharBody] LỖI KHÔNG THỂ VẼ NHÂN VẬT '{cName}' (charID={charID}, head={head}, body={body}, leg={leg}): {reason}");
		}
	}

	public virtual void paintCharBody(mGraphics g, int cx, int cy, int cdir, int cf, bool isPaintBag)
	{
		if (GameScr.parts == null || head < 0 || head >= GameScr.parts.Length || leg < 0 || leg >= GameScr.parts.Length || body < 0 || body >= GameScr.parts.Length)
		{
			logPaintErrorThrottled($"Part ID vượt giới hạn mảng GameScr.parts (Length={(GameScr.parts != null ? GameScr.parts.Length : -1)})! head={head}, body={body}, leg={leg}");
			return;
		}
		ph = GameScr.parts[head];
		pl = GameScr.parts[leg];
		pb = GameScr.parts[body];
		if (ph == null || pl == null || pb == null || ph.pi == null || pl.pi == null || pb.pi == null)
		{
			logPaintErrorThrottled($"Part hoặc Part.pi bị NULL! head={head} (ph={(ph == null ? "NULL" : (ph.pi == null ? "pi=NULL" : "OK"))}), body={body} (pb={(pb == null ? "NULL" : (pb.pi == null ? "pi=NULL" : "OK"))}), leg={leg} (pl={(pl == null ? "NULL" : (pl.pi == null ? "pi=NULL" : "OK"))})");
			return;
		}
		if (cf < 0 || cf >= CharInfo.Length || CharInfo[cf] == null || CharInfo[cf].Length < 3 || CharInfo[cf][0] == null || CharInfo[cf][0].Length < 3 || CharInfo[cf][1] == null || CharInfo[cf][1].Length < 3 || CharInfo[cf][2] == null || CharInfo[cf][2].Length < 3)
		{
			return;
		}
		if (CharInfo[cf][0][0] >= ph.pi.Length || CharInfo[cf][1][0] >= pl.pi.Length || CharInfo[cf][2][0] >= pb.pi.Length)
		{
			logPaintErrorThrottled($"Frame cf={cf} vượt độ dài mảng PartImage! Yêu cầu: headIdx={CharInfo[cf][0][0]}/{ph.pi.Length}, legIdx={CharInfo[cf][1][0]}/{pl.pi.Length}, bodyIdx={CharInfo[cf][2][0]}/{pb.pi.Length}");
			return;
		}
		if (bag >= 0 && statusMe != 14)
		{
			if (!ClanImage.idImages.containsKey(bag + string.Empty))
			{
				ClanImage.idImages.put(bag + string.Empty, new ClanImage());
				Service.gI().requestBagImage((sbyte)bag);
			}
			else
			{
				ClanImage clanImage = (ClanImage)ClanImage.idImages.get(bag + string.Empty);
				if (clanImage.idImage != null && isPaintBag)
				{
					paintBag(g, clanImage.idImage, cx, cy, cdir, true);
				}
			}
		}
		int num = 2;
		int anchor = 24;
		int anchor2 = StaticObj.TOP_RIGHT;
		int num2 = -1;
		if (cdir == 1)
		{
			num = 0;
			anchor = 0;
			anchor2 = 0;
			num2 = 1;
		}
		if (statusMe == 14)
		{
			if (GameCanvas.gameTick % 4 > 0)
			{
				g.drawImage(ItemMap.imageFlare, cx, cy - ch - 11, mGraphics.HCENTER | mGraphics.VCENTER);
			}
			int num3 = 0;
			if (head == 89 || head == 457 || head == 460 || head == 461 || head == 462 || head == 463 || head == 464 || head == 465 || head == 466)
			{
				num3 = 15;
			}
			if (head == 1291)
			{
				num3 = 23;
			}
			SmallImage.drawSmallImage(g, 834, cx, cy - CharInfo[cf][2][2] + (pb.pi[CharInfo[cf][2][0]] != null ? pb.pi[CharInfo[cf][2][0]].dy : 0) - 2 + num3, num, StaticObj.TOP_CENTER);
			SmallImage.drawSmallImage(g, 79, cx, cy - ch - 8, 0, mGraphics.HCENTER | mGraphics.BOTTOM);
			if (ph.pi[CharInfo[cf][0][0]] != null)
			{
				SmallImage.drawSmallImage(g, ph.pi[CharInfo[cf][0][0]].id, cx + (CharInfo[cf][0][1] + ph.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + ph.pi[CharInfo[cf][0][0]].dy, num, anchor);
			}
			paintHat_behind(g, cf, cy - CharInfo[cf][2][2] + (pb.pi[CharInfo[cf][2][0]] != null ? pb.pi[CharInfo[cf][2][0]].dy : 0));
			if (isHead_2Fr(head))
			{
				int fHeadIdx = getFHead(head);
				if (fHeadIdx >= 0 && fHeadIdx < GameScr.parts.Length && GameScr.parts[fHeadIdx] != null)
				{
					Part part = GameScr.parts[fHeadIdx];
					if (part.pi != null && CharInfo[cf][0][0] < part.pi.Length && part.pi[CharInfo[cf][0][0]] != null)
					{
						SmallImage.drawSmallImage(g, part.pi[CharInfo[cf][0][0]].id, cx + (CharInfo[cf][0][1] + part.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + part.pi[CharInfo[cf][0][0]].dy, num, anchor);
					}
				}
			}
			else
			{
				if (ph.pi[CharInfo[cf][0][0]] != null)
				{
					SmallImage.drawSmallImage(g, ph.pi[CharInfo[cf][0][0]].id, cx + (CharInfo[cf][0][1] + ph.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + ph.pi[CharInfo[cf][0][0]].dy, num, anchor);
				}
			}
			paintHat_front(g, cf, cy - CharInfo[cf][2][2] + (pb.pi[CharInfo[cf][2][0]] != null ? pb.pi[CharInfo[cf][2][0]].dy : 0));
			if (ph.pi[CharInfo[cf][0][0]] != null)
			{
				paintRedEye(g, cx + (CharInfo[cf][0][1] + ph.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + ph.pi[CharInfo[cf][0][0]].dy, num, anchor);
			}
		}
		else
		{
			paintHat_behind(g, cf, cy - CharInfo[cf][2][2] + (pb.pi[CharInfo[cf][2][0]] != null ? pb.pi[CharInfo[cf][2][0]].dy : 0));
			if (isHead_2Fr(head))
			{
				int fHeadIdx = getFHead(head);
				if (fHeadIdx >= 0 && fHeadIdx < GameScr.parts.Length && GameScr.parts[fHeadIdx] != null)
				{
					Part part2 = GameScr.parts[fHeadIdx];
					if (part2.pi != null && CharInfo[cf][0][0] < part2.pi.Length && part2.pi[CharInfo[cf][0][0]] != null)
					{
						SmallImage.drawSmallImage(g, part2.pi[CharInfo[cf][0][0]].id, cx + (CharInfo[cf][0][1] + part2.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + part2.pi[CharInfo[cf][0][0]].dy, num, anchor);
					}
				}
			}
			else
			{
				if (ph.pi[CharInfo[cf][0][0]] != null)
				{
					SmallImage.drawSmallImage(g, ph.pi[CharInfo[cf][0][0]].id, cx + (CharInfo[cf][0][1] + ph.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + ph.pi[CharInfo[cf][0][0]].dy, num, anchor);
				}
			}
			if (pl.pi[CharInfo[cf][1][0]] != null)
			{
				SmallImage.drawSmallImage(g, pl.pi[CharInfo[cf][1][0]].id, cx + (CharInfo[cf][1][1] + pl.pi[CharInfo[cf][1][0]].dx) * num2, cy - CharInfo[cf][1][2] + pl.pi[CharInfo[cf][1][0]].dy, num, anchor);
			}
			if (pb.pi[CharInfo[cf][2][0]] != null)
			{
				SmallImage.drawSmallImage(g, pb.pi[CharInfo[cf][2][0]].id, cx + (CharInfo[cf][2][1] + pb.pi[CharInfo[cf][2][0]].dx) * num2, cy - CharInfo[cf][2][2] + pb.pi[CharInfo[cf][2][0]].dy, num, anchor);
			}
			if (ph.pi[CharInfo[cf][0][0]] != null)
			{
				paintRedEye(g, cx + (CharInfo[cf][0][1] + ph.pi[CharInfo[cf][0][0]].dx) * num2, cy - CharInfo[cf][0][2] + ph.pi[CharInfo[cf][0][0]].dy, num, anchor);
			}
		}
		ch = ((isMonkey != 1 && !isFusion) ? (CharInfo[0][0][2] + (ph.pi[CharInfo[0][0][0]] != null ? ph.pi[CharInfo[0][0][0]].dy : 0) + 10) : 60);
		int phDy = (ph.pi[CharInfo[cf][0][0]] != null ? ph.pi[CharInfo[cf][0][0]].dy : 0);
		int num4 = ((Res.abs(phDy) < 22) ? phDy : ((phDy >= 0) ? (phDy - 5) : (phDy + 5)));
		cH_new = cy - CharInfo[cf][0][2] + num4;
		if (statusMe == 1 && charID > 0 && !isMask && !isUseChargeSkill() && !isWaitMonkey && skillPaint == null && cf != 23 && bag < 0 && ((GameCanvas.gameTick + charID) % 30 == 0 || isFreez))
		{
			if (eyeTraiDat == null || eyeNamek == null)
			{
				checkLoadMount();
			}
			g.drawImage((cgender != 1) ? eyeTraiDat : eyeNamek, cx + -((cgender != 1) ? 2 : 2) * num2, cy - 32 + ((cgender != 1) ? 11 : 10) - cf, anchor2);
		}
		if (eProtect != null)
		{
			eProtect.paint(g);
		}
		if (eDanhHieu != null)
		{
			eDanhHieu.paint(g);
		}
		paintPKFlag(g);
	}
    
    	public void paintCharWithSkill(mGraphics g)
    	{
    		if (!isPaint())
    		{
    			return;
    		}
    		ty = 0;
    		SkillInfoPaint[] array = skillInfoPaint();
    		if (array == null || indexSkill < 0 || indexSkill >= array.Length || array[indexSkill] == null)
    		{
    			paintCharWithoutSkill(g);
    			return;
    		}
    		cf = array[indexSkill].status;
    		paintCharWithoutSkill(g);
    		if (cdir == 1)
    		{
    			if (eff0 != null)
    			{
    				if (dx0 == 0)
    				{
    					dx0 = array[indexSkill].e0dx;
    				}
    				if (dy0 == 0)
    				{
    					dy0 = array[indexSkill].e0dy;
    				}
    				SmallImage.drawSmallImage(g, eff0.arrEfInfo[i0].idImg, cx + dx0 + eff0.arrEfInfo[i0].dx, cy + dy0 + eff0.arrEfInfo[i0].dy, 0, mGraphics.VCENTER | mGraphics.HCENTER);
    				i0++;
    				if (i0 >= eff0.arrEfInfo.Length)
    				{
    					eff0 = null;
    					i0 = (dx0 = (dy0 = 0));
    				}
    			}
    			if (eff1 != null)
    			{
    				if (dx1 == 0)
    				{
    					dx1 = array[indexSkill].e1dx;
    				}
    				if (dy1 == 0)
    				{
    					dy1 = array[indexSkill].e1dy;
    				}
    				SmallImage.drawSmallImage(g, eff1.arrEfInfo[i1].idImg, cx + dx1 + eff1.arrEfInfo[i1].dx, cy + dy1 + eff1.arrEfInfo[i1].dy, 0, mGraphics.VCENTER | mGraphics.HCENTER);
    				i1++;
    				if (i1 >= eff1.arrEfInfo.Length)
    				{
    					eff1 = null;
    					i1 = (dx1 = (dy1 = 0));
    				}
    			}
    			if (eff2 != null)
    			{
    				if (dx2 == 0)
    				{
    					dx2 = array[indexSkill].e2dx;
    				}
    				if (dy2 == 0)
    				{
    					dy2 = array[indexSkill].e2dy;
    				}
    				SmallImage.drawSmallImage(g, eff2.arrEfInfo[i2].idImg, cx + dx2 + eff2.arrEfInfo[i2].dx, cy + dy2 + eff2.arrEfInfo[i2].dy, 0, mGraphics.VCENTER | mGraphics.HCENTER);
    				i2++;
    				if (i2 >= eff2.arrEfInfo.Length)
    				{
    					eff2 = null;
    					i2 = (dx2 = (dy2 = 0));
    				}
    			}
    		}
    		else
    		{
    			if (eff0 != null)
    			{
    				if (dx0 == 0)
    				{
    					dx0 = array[indexSkill].e0dx;
    				}
    				if (dy0 == 0)
    				{
    					dy0 = array[indexSkill].e0dy;
    				}
    				SmallImage.drawSmallImage(g, eff0.arrEfInfo[i0].idImg, cx - dx0 - eff0.arrEfInfo[i0].dx, cy + dy0 + eff0.arrEfInfo[i0].dy, 2, mGraphics.VCENTER | mGraphics.HCENTER);
    				i0++;
    				if (i0 >= eff0.arrEfInfo.Length)
    				{
    					eff0 = null;
    					i0 = 0;
    					dx0 = 0;
    					dy0 = 0;
    				}
    			}
    			if (eff1 != null)
    			{
    				if (dx1 == 0)
    				{
    					dx1 = array[indexSkill].e1dx;
    				}
    				if (dy1 == 0)
    				{
    					dy1 = array[indexSkill].e1dy;
    				}
    				SmallImage.drawSmallImage(g, eff1.arrEfInfo[i1].idImg, cx - dx1 - eff1.arrEfInfo[i1].dx, cy + dy1 + eff1.arrEfInfo[i1].dy, 2, mGraphics.VCENTER | mGraphics.HCENTER);
    				i1++;
    				if (i1 >= eff1.arrEfInfo.Length)
    				{
    					eff1 = null;
    					i1 = 0;
    					dx1 = 0;
    					dy1 = 0;
    				}
    			}
    			if (eff2 != null)
    			{
    				if (dx2 == 0)
    				{
    					dx2 = array[indexSkill].e2dx;
    				}
    				if (dy2 == 0)
    				{
    					dy2 = array[indexSkill].e2dy;
    				}
    				SmallImage.drawSmallImage(g, eff2.arrEfInfo[i2].idImg, cx - dx2 - eff2.arrEfInfo[i2].dx, cy + dy2 + eff2.arrEfInfo[i2].dy, 2, mGraphics.VCENTER | mGraphics.HCENTER);
    				i2++;
    				if (i2 >= eff2.arrEfInfo.Length)
    				{
    					eff2 = null;
    					i2 = 0;
    					dx2 = 0;
    					dy2 = 0;
    				}
    			}
    		}
    		indexSkill++;
    	}
    
    	public static int getIndexChar(int ID)
    	{
    		for (int i = 0; i < GameScr.vCharInMap.size(); i++)
    		{
    			Char @char = (Char)GameScr.vCharInMap.elementAt(i);
    			if (@char.charID == ID)
    			{
    				return i;
    			}
    		}
    		return -1;
    	}
    
    	public void moveTo(int toX, int toY, int type)
    	{
    		if (type == 1 || Res.abs(toX - cx) > 100 || Res.abs(toY - cy) > 300)
    		{
    			createShadow(cx, cy, 10);
    			cx = toX;
    			cy = toY;
    			vMovePoints.removeAllElements();
    			statusMe = 6;
    			cp3 = 0;
    			currentMovePoint = null;
    			cf = 25;
    			return;
    		}
    		int dir = 0;
    		int act = 0;
    		int num = toX - cx;
    		int num2 = toY - cy;
    		if (num == 0 && num2 == 0)
    		{
    			act = 1;
    			cp3 = 0;
    		}
    		else if (num2 == 0)
    		{
    			act = 2;
    			if (num > 0)
    			{
    				dir = 1;
    			}
    			if (num < 0)
    			{
    				dir = -1;
    			}
    		}
    		else if (num2 != 0)
    		{
    			if (num2 < 0)
    			{
    				act = 3;
    			}
    			if (num2 > 0)
    			{
    				act = 4;
    			}
    			if (num < 0)
    			{
    				dir = -1;
    			}
    			if (num > 0)
    			{
    				dir = 1;
    			}
    		}
    		vMovePoints.addElement(new MovePoint(toX, toY, act, dir));
    		if (statusMe != 6)
    		{
    			statusBeforeNothing = statusMe;
    		}
    		statusMe = 6;
    		cp3 = 0;
    	}
    
    	public static void getcharInjure(int cID, int dx, int dy, int HP)
    	{
    		Char target = null;
    		for (int i = 0; i < GameScr.vCharInMap.size(); i++)
    		{
    			Char c = (Char)GameScr.vCharInMap.elementAt(i);
    			if (c != null && (c.charID == cID || i == cID))
    			{
    				target = c;
    				break;
    			}
    		}
    		if (target != null && target.vMovePoints != null && target.vMovePoints.size() != 0)
    		{
    			MovePoint movePoint = (MovePoint)target.vMovePoints.lastElement();
    			if (movePoint != null)
    			{
    				int xEnd = movePoint.xEnd + dx;
    				int yEnd = movePoint.yEnd + dy;
    				target.cHP -= HP;
    				if (target.cHP < 0)
    				{
    					target.cHP = 0;
    				}
    				target.cHPShow = target.cHP - HP;
    				target.statusMe = 6;
    				target.cp3 = 0;
    				target.vMovePoints.addElement(new MovePoint(xEnd, yEnd, 8, target.cdir));
    			}
    		}
    	}
    
    	public bool isMagicTree()
    	{
    		if (GameScr.gI().magicTree != null)
    		{
    			int x = GameScr.gI().magicTree.x;
    			int y = GameScr.gI().magicTree.y;
    			if (cx > x - 30 && cx < x + 30 && cy > y - 30 && cy < y + 30)
    			{
    				return true;
    			}
    			return false;
    		}
    		return false;
    	}
    
    	public void searchItem()
    	{
    		int[] array = new int[4] { -1, -1, -1, -1 };
    		if (itemFocus != null)
    		{
    			return;
    		}
    		for (int i = 0; i < GameScr.vItemMap.size(); i++)
    		{
    			ItemMap itemMap = (ItemMap)GameScr.vItemMap.elementAt(i);
    			int num = Math.abs(myCharz().cx - itemMap.x);
    			int num2 = Math.abs(myCharz().cy - itemMap.y);
    			int num3 = ((num <= num2) ? num2 : num);
    			if (num > 48 || num2 > 48 || (itemFocus != null && num3 >= array[3]))
    			{
    				continue;
    			}
    			if (GameScr.gI().auto != 0 && GameScr.gI().isBagFull())
    			{
    				if (itemMap.template.type == 9)
    				{
    					itemFocus = itemMap;
    					array[3] = num3;
    				}
    			}
    			else
    			{
    				itemFocus = itemMap;
    				array[3] = num3;
    			}
    		}
    	}
    
    	public void searchFocus()
    	{
    		if (myCharz().skillPaint != null || myCharz().arr != null || myCharz().dart != null)
    		{
    			timeFocusToMob = 200;
    			return;
    		}
    		if (timeFocusToMob > 0)
    		{
    			timeFocusToMob--;
    			return;
    		}
    		if (isManualFocus && charFocus != null && (charFocus.statusMe == 15 || charFocus.isInvisiblez))
    		{
    			charFocus = null;
    		}
    		if (GameCanvas.gameTick % 2 == 0 || isMeCanAttackOtherPlayer(charFocus))
    		{
    			return;
    		}
    		int num = 0;
    		if (nClass != null && (nClass.classId == 0 || nClass.classId == 1 || nClass.classId == 3 || nClass.classId == 5))
    		{
    			num = 40;
    		}
    		int[] array = new int[4] { -1, -1, -1, -1 };
    		int num2 = GameScr.cmx - 10;
    		int num3 = GameScr.cmx + GameCanvas.w + 10;
    		int cmy = GameScr.cmy;
    		int num4 = GameScr.cmy + GameCanvas.h - GameScr.cmdBarH + 10;
    		if (isManualFocus)
    		{
    			if ((mobFocus != null && mobFocus.status != 1 && mobFocus.status != 0 && num2 <= mobFocus.x && mobFocus.x <= num3 && cmy <= mobFocus.y && mobFocus.y <= num4) || (npcFocus != null && num2 <= npcFocus.cx && npcFocus.cx <= num3 && cmy <= npcFocus.cy && npcFocus.cy <= num4) || (charFocus != null && num2 <= charFocus.cx && charFocus.cx <= num3 && cmy <= charFocus.cy && charFocus.cy <= num4) || (itemFocus != null && num2 <= itemFocus.x && itemFocus.x <= num3 && cmy <= itemFocus.y && itemFocus.y <= num4))
    			{
    				return;
    			}
    			isManualFocus = false;
    		}
    		num2 = myCharz().cx - 80;
    		num3 = myCharz().cx + 80;
    		cmy = myCharz().cy - 30;
    		num4 = myCharz().cy + 30;
    		if (npcFocus != null && npcFocus.template.npcTemplateId == 6)
    		{
    			num2 = myCharz().cx - 20;
    			num3 = myCharz().cx + 20;
    			cmy = myCharz().cy - 10;
    			num4 = myCharz().cy + 10;
    		}
    		if (npcFocus == null)
    		{
    			for (int i = 0; i < GameScr.vNpc.size(); i++)
    			{
    				Npc npc = (Npc)GameScr.vNpc.elementAt(i);
    				if (npc.statusMe != 15)
    				{
    					int num5 = Math.abs(myCharz().cx - npc.cx);
    					int num6 = Math.abs(myCharz().cy - npc.cy);
    					int num7 = ((num5 <= num6) ? num6 : num5);
    					num2 = myCharz().cx - 80;
    					num3 = myCharz().cx + 80;
    					cmy = myCharz().cy - 30;
    					num4 = myCharz().cy + 30;
    					if (npc.template.npcTemplateId == 6)
    					{
    						num2 = myCharz().cx - 20;
    						num3 = myCharz().cx + 20;
    						cmy = myCharz().cy - 10;
    						num4 = myCharz().cy + 10;
    					}
    					if (num2 <= npc.cx && npc.cx <= num3 && cmy <= npc.cy && npc.cy <= num4 && (npcFocus == null || num7 < array[1]))
    					{
    						npcFocus = npc;
    						array[1] = num7;
    					}
    				}
    			}
    		}
    		else
    		{
    			if (num2 <= npcFocus.cx && npcFocus.cx <= num3 && cmy <= npcFocus.cy && npcFocus.cy <= num4)
    			{
    				clearFocus(1);
    				return;
    			}
    			deFocusNPC();
    			for (int j = 0; j < GameScr.vNpc.size(); j++)
    			{
    				Npc npc2 = (Npc)GameScr.vNpc.elementAt(j);
    				if (npc2.statusMe != 15)
    				{
    					int num8 = Math.abs(myCharz().cx - npc2.cx);
    					int num9 = Math.abs(myCharz().cy - npc2.cy);
    					int num10 = ((num8 <= num9) ? num9 : num8);
    					num2 = myCharz().cx - 80;
    					num3 = myCharz().cx + 80;
    					cmy = myCharz().cy - 30;
    					num4 = myCharz().cy + 30;
    					if (npc2.template.npcTemplateId == 6)
    					{
    						num2 = myCharz().cx - 20;
    						num3 = myCharz().cx + 20;
    						cmy = myCharz().cy - 10;
    						num4 = myCharz().cy + 10;
    					}
    					if (num2 <= npc2.cx && npc2.cx <= num3 && cmy <= npc2.cy && npc2.cy <= num4 && (npcFocus == null || num10 < array[1]))
    					{
    						npcFocus = npc2;
    						array[1] = num10;
    					}
    				}
    			}
    		}
    		if (itemFocus == null)
    		{
    			for (int k = 0; k < GameScr.vItemMap.size(); k++)
    			{
    				ItemMap itemMap = (ItemMap)GameScr.vItemMap.elementAt(k);
    				int num11 = Math.abs(myCharz().cx - itemMap.x);
    				int num12 = Math.abs(myCharz().cy - itemMap.y);
    				int num13 = ((num11 <= num12) ? num12 : num11);
    				if (num11 > 48 || num12 > 48 || (itemFocus != null && num13 >= array[3]))
    				{
    					continue;
    				}
    				if (GameScr.gI().auto != 0 && GameScr.gI().isBagFull())
    				{
    					if (itemMap.template.type == 9)
    					{
    						itemFocus = itemMap;
    						array[3] = num13;
    					}
    				}
    				else
    				{
    					itemFocus = itemMap;
    					array[3] = num13;
    				}
    			}
    		}
    		else
    		{
    			if (num2 <= itemFocus.x && itemFocus.x <= num3 && cmy <= itemFocus.y && itemFocus.y <= num4)
    			{
    				clearFocus(3);
    				return;
    			}
    			itemFocus = null;
    			for (int l = 0; l < GameScr.vItemMap.size(); l++)
    			{
    				ItemMap itemMap2 = (ItemMap)GameScr.vItemMap.elementAt(l);
    				int num14 = Math.abs(myCharz().cx - itemMap2.x);
    				int num15 = Math.abs(myCharz().cy - itemMap2.y);
    				int num16 = ((num14 <= num15) ? num15 : num14);
    				if (num2 > itemMap2.x || itemMap2.x > num3 || cmy > itemMap2.y || itemMap2.y > num4 || (itemFocus != null && num16 >= array[3]))
    				{
    					continue;
    				}
    				if (GameScr.gI().auto != 0 && GameScr.gI().isBagFull())
    				{
    					if (itemMap2.template.type == 9)
    					{
    						itemFocus = itemMap2;
    						array[3] = num16;
    					}
    				}
    				else
    				{
    					itemFocus = itemMap2;
    					array[3] = num16;
    				}
    			}
    		}
    		num2 = myCharz().cx - myCharz().getdxSkill() - 10;
    		num3 = myCharz().cx + myCharz().getdxSkill() + 10;
    		cmy = myCharz().cy - myCharz().getdySkill() - num - 20;
    		num4 = myCharz().cy + myCharz().getdySkill() + 20;
    		if (num4 > myCharz().cy + 30)
    		{
    			num4 = myCharz().cy + 30;
    		}
    		if (mobFocus == null)
    		{
    			for (int m = 0; m < GameScr.vMob.size(); m++)
    			{
    				Mob mob = (Mob)GameScr.vMob.elementAt(m);
    				int num17 = Math.abs(myCharz().cx - mob.x);
    				int num18 = Math.abs(myCharz().cy - mob.y);
    				int num19 = ((num17 <= num18) ? num18 : num17);
    				if (num2 <= mob.x && mob.x <= num3 && cmy <= mob.y && mob.y <= num4 && (mobFocus == null || num19 < array[0]))
    				{
    					mobFocus = mob;
    					array[0] = num19;
    				}
    			}
    		}
    		else
    		{
    			if (mobFocus.status != 1 && mobFocus.status != 0 && num2 <= mobFocus.x && mobFocus.x <= num3 && cmy <= mobFocus.y && mobFocus.y <= num4)
    			{
    				clearFocus(0);
    				return;
    			}
    			mobFocus = null;
    			for (int n = 0; n < GameScr.vMob.size(); n++)
    			{
    				Mob mob2 = (Mob)GameScr.vMob.elementAt(n);
    				int num20 = Math.abs(myCharz().cx - mob2.x);
    				int num21 = Math.abs(myCharz().cy - mob2.y);
    				int num22 = ((num20 <= num21) ? num21 : num20);
    				if (num2 <= mob2.x && mob2.x <= num3 && cmy <= mob2.y && mob2.y <= num4 && (mobFocus == null || num22 < array[0]))
    				{
    					mobFocus = mob2;
    					array[0] = num22;
    				}
    			}
    		}
    		if (charFocus == null)
    		{
    			for (int num23 = 0; num23 < GameScr.vCharInMap.size(); num23++)
    			{
    				Char @char = (Char)GameScr.vCharInMap.elementAt(num23);
    				if (@char.statusMe != 15 && !@char.isInvisiblez && wdx == 0 && wdy == 0)
    				{
    					int num24 = Math.abs(myCharz().cx - @char.cx);
    					int num25 = Math.abs(myCharz().cy - @char.cy);
    					int num26 = ((num24 <= num25) ? num25 : num24);
    					if (num2 <= @char.cx && @char.cx <= num3 && cmy <= @char.cy && @char.cy <= num4 && (charFocus == null || num26 < array[2]))
    					{
    						charFocus = @char;
    						array[2] = num26;
    					}
    				}
    			}
    		}
    		else
    		{
    			if (num2 <= charFocus.cx && charFocus.cx <= num3 && cmy <= charFocus.cy && charFocus.cy <= num4 && charFocus.statusMe != 15 && !charFocus.isInvisiblez)
    			{
    				clearFocus(2);
    				return;
    			}
    			charFocus = null;
    			for (int num27 = 0; num27 < GameScr.vCharInMap.size(); num27++)
    			{
    				Char char2 = (Char)GameScr.vCharInMap.elementAt(num27);
    				if (char2.statusMe != 15 && !char2.isInvisiblez && wdx == 0 && wdy == 0)
    				{
    					int num28 = Math.abs(myCharz().cx - char2.cx);
    					int num29 = Math.abs(myCharz().cy - char2.cy);
    					int num30 = ((num28 <= num29) ? num29 : num28);
    					if (num2 <= char2.cx && char2.cx <= num3 && cmy <= char2.cy && char2.cy <= num4 && (charFocus == null || num30 < array[2]))
    					{
    						charFocus = char2;
    						array[2] = num30;
    					}
    				}
    			}
    		}
    		int num31 = -1;
    		for (int num32 = 0; num32 < array.Length; num32++)
    		{
    			if (num31 == -1)
    			{
    				if (array[num32] != -1)
    				{
    					num31 = num32;
    				}
    			}
    			else if (array[num32] < array[num31] && array[num32] != -1)
    			{
    				num31 = num32;
    			}
    		}
    		clearFocus(num31);
    		if (me && isAttacPlayerStatus())
    		{
    			if (charFocus != null && mobFocus != null && !mobFocus.isMobMe)
    			{
    				mobFocus = null;
    			}
    			npcFocus = null;
    			itemFocus = null;
    		}
    	}
    
    	public void clearFocus(int index)
    	{
    		switch (index)
    		{
    		case 0:
    			deFocusNPC();
    			charFocus = null;
    			itemFocus = null;
    			break;
    		case 1:
    			mobFocus = null;
    			charFocus = null;
    			itemFocus = null;
    			break;
    		case 2:
    			mobFocus = null;
    			deFocusNPC();
    			itemFocus = null;
    			break;
    		case 3:
    			mobFocus = null;
    			deFocusNPC();
    			charFocus = null;
    			break;
    		}
    	}
    
    	public static bool isCharInScreen(Char c)
    	{
    		int cmx = GameScr.cmx;
    		int num = GameScr.cmx + GameCanvas.w;
    		int num2 = GameScr.cmy + 10;
    		int num3 = GameScr.cmy + GameScr.gH;
    		if (c.statusMe != 15 && !c.isInvisiblez && cmx <= c.cx && c.cx <= num && num2 <= c.cy && c.cy <= num3)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isAttacPlayerStatus()
    	{
    		return cTypePk == 4 || cTypePk == 3;
    	}
    
    	public void setHoldChar(Char r)
    	{
    		if (cx < r.cx)
    		{
    			cdir = 1;
    		}
    		else
    		{
    			cdir = -1;
    		}
    		charHold = r;
    		holder = true;
    	}
    
    	public void setHoldMob(Mob r)
    	{
    		if (cx < r.x)
    		{
    			cdir = 1;
    		}
    		else
    		{
    			cdir = -1;
    		}
    		mobHold = r;
    		holder = true;
    	}
    
    	public void findNextFocusByKey()
    	{
    		Res.outz("focus size= " + focus.size());
    		if ((myCharz().skillPaint != null || myCharz().arr != null || myCharz().dart != null || myCharz().skillInfoPaint() != null) && focus.size() == 0)
    		{
    			return;
    		}
    		focus.removeAllElements();
    		int num = 0;
    		int num2 = GameScr.cmx + 10;
    		int num3 = GameScr.cmx + GameCanvas.w - 10;
    		int num4 = GameScr.cmy + 10;
    		int num5 = GameScr.cmy + GameScr.gH;
    		for (int i = 0; i < GameScr.vCharInMap.size(); i++)
    		{
    			Char @char = (Char)GameScr.vCharInMap.elementAt(i);
    			if (@char.statusMe != 15 && !@char.isInvisiblez && !@char.isPetFollower && @char.charID > -310720000 && @char.cName != "$" && num2 <= @char.cx && @char.cx <= num3 && num4 <= @char.cy && @char.cy <= num5 && @char.charID != -114 && (TileMap.mapID != 129 || (TileMap.mapID == 129 && myCharz().cy > 264)))
    			{
    				focus.addElement(@char);
    				if (charFocus != null && @char.Equals(charFocus))
    				{
    					num = focus.size();
    				}
    			}
    		}
    		if (me && isAttacPlayerStatus())
    		{
    			Res.outz("co the tan cong nguoi");
    			for (int j = 0; j < GameScr.vMob.size(); j++)
    			{
    				Mob mob = (Mob)GameScr.vMob.elementAt(j);
    				if (!GameScr.gI().isMeCanAttackMob(mob))
    				{
    					Res.outz("khong the tan cong quai");
    					mobFocus = null;
    					continue;
    				}
    				Res.outz("co the tan ong quai");
    				focus.addElement(mob);
    				if (mobFocus != null)
    				{
    					num = focus.size();
    				}
    			}
    			npcFocus = null;
    			itemFocus = null;
    			if (focus.size() > 0)
    			{
    				if (num >= focus.size())
    				{
    					num = 0;
    				}
    				focusManualTo(focus.elementAt(num));
    			}
    			else
    			{
    				mobFocus = null;
    				deFocusNPC();
    				charFocus = null;
    				itemFocus = null;
    				isManualFocus = false;
    			}
    			return;
    		}
    		for (int k = 0; k < GameScr.vItemMap.size(); k++)
    		{
    			ItemMap itemMap = (ItemMap)GameScr.vItemMap.elementAt(k);
    			if (num2 <= itemMap.x && itemMap.x <= num3 && num4 <= itemMap.y && itemMap.y <= num5)
    			{
    				focus.addElement(itemMap);
    				if (itemFocus != null && itemMap.Equals(itemFocus))
    				{
    					num = focus.size();
    				}
    			}
    		}
    		for (int l = 0; l < GameScr.vMob.size(); l++)
    		{
    			Mob mob2 = (Mob)GameScr.vMob.elementAt(l);
    			if (mob2.status != 1 && mob2.status != 0 && num2 <= mob2.x && mob2.x <= num3 && num4 <= mob2.y && mob2.y <= num5)
    			{
    				focus.addElement(mob2);
    				if (mobFocus != null && mob2.Equals(mobFocus))
    				{
    					num = focus.size();
    				}
    			}
    		}
    		for (int m = 0; m < GameScr.vNpc.size(); m++)
    		{
    			Npc npc = (Npc)GameScr.vNpc.elementAt(m);
    			if (npc.statusMe != 15 && num2 <= npc.cx && npc.cx <= num3 && num4 <= npc.cy && npc.cy <= num5)
    			{
    				focus.addElement(npc);
    				if (npcFocus != null && npc.Equals(npcFocus))
    				{
    					num = focus.size();
    				}
    			}
    		}
    		if (focus.size() > 0)
    		{
    			if (num >= focus.size())
    			{
    				num = 0;
    			}
    			focusManualTo(focus.elementAt(num));
    		}
    		else
    		{
    			mobFocus = null;
    			deFocusNPC();
    			charFocus = null;
    			itemFocus = null;
    			isManualFocus = false;
    		}
    	}
    
    	public void deFocusNPC()
    	{
    		if (me && npcFocus != null)
    		{
    			if (!GameCanvas.menu.showMenu)
    			{
    				chatPopup = null;
    			}
    			npcFocus = null;
    		}
    	}
    
    	public void updateCharInBridge()
    	{
    		if (!GameCanvas.lowGraphic)
    		{
    			if (TileMap.tileTypeAt(cx, cy + 1, 1024))
    			{
    				TileMap.setTileTypeAtPixel(cx, cy + 1, 512);
    				TileMap.setTileTypeAtPixel(cx, cy - 2, 512);
    			}
    			if (TileMap.tileTypeAt(cx - TileMap.size, cy + 1, 512))
    			{
    				TileMap.killTileTypeAt(cx - TileMap.size, cy + 1, 512);
    				TileMap.killTileTypeAt(cx - TileMap.size, cy - 2, 512);
    			}
    			if (TileMap.tileTypeAt(cx + TileMap.size, cy + 1, 512))
    			{
    				TileMap.killTileTypeAt(cx + TileMap.size, cy + 1, 512);
    				TileMap.killTileTypeAt(cx + TileMap.size, cy - 2, 512);
    			}
    		}
    	}
    
    	public static void sort(int[] data)
    	{
    		int num = 5;
    		for (int i = 0; i < num - 1; i++)
    		{
    			for (int j = i + 1; j < num; j++)
    			{
    				if (data[i] < data[j])
    				{
    					int num2 = data[j];
    					data[j] = data[i];
    					data[i] = num2;
    				}
    			}
    		}
    	}
    
    	public static bool setInsc(int cmX, int cmWx, int x, int cmy, int cmyH, int y)
    	{
    		if (x > cmWx || x < cmX || y > cmyH || y < cmy)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public void kickOption(Item item, int maxKick)
    	{
    		int num = 0;
    		if (item == null || item.options == null)
    		{
    			return;
    		}
    		for (int i = 0; i < item.options.size(); i++)
    		{
    			ItemOption itemOption = (ItemOption)item.options.elementAt(i);
    			itemOption.active = 0;
    			if (itemOption.optionTemplate.type == 2)
    			{
    				if (num < maxKick)
    				{
    					itemOption.active = 1;
    					num++;
    				}
    			}
    			else if (itemOption.optionTemplate.type == 3 && item.upgrade >= 4)
    			{
    				itemOption.active = 1;
    			}
    			else if (itemOption.optionTemplate.type == 4 && item.upgrade >= 8)
    			{
    				itemOption.active = 1;
    			}
    			else if (itemOption.optionTemplate.type == 5 && item.upgrade >= 12)
    			{
    				itemOption.active = 1;
    			}
    			else if (itemOption.optionTemplate.type == 6 && item.upgrade >= 14)
    			{
    				itemOption.active = 1;
    			}
    			else if (itemOption.optionTemplate.type == 7 && item.upgrade >= 16)
    			{
    				itemOption.active = 1;
    			}
    		}
    	}
    }
}

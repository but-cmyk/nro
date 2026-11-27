namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;

    public partial class Char
    {
    	public void paintMount1(mGraphics g)
    	{
    		if (xMount <= GameScr.cmx || xMount >= GameScr.cmx + GameCanvas.w) return;
    		if (me && !isEndMount && !isStartMount && !isMount) return;
    		checkLoadMount();
    		if (me)
    		{
    			if (idMount >= ID_NEW_MOUNT)
    			{
    				if (cachedNewMountId0 != idMount) { cachedNewMountId0 = idMount; cachedNewMountName0 = strMount + (idMount - ID_NEW_MOUNT) + "_0"; }
    				FrameImage fraImage = mSystem.getFraImage(cachedNewMountName0);
    				if (fraImage != null)
    				{
    					fraImage.drawFrame(frameNewMount / 2 % fraImage.nFrame, xMount, yMount + fy, transMount, 3, g);
    				}
    			}
    			else
    			{
    				if (isSpeacialMount)
    				{
    					return;
    				}
    				if (isEventMount)
    				{
    					g.drawRegion(imgEventMountWing, 0, FrameMount[frameMount] * 60, 60, 60, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    				else if (genderMount == 2)
    				{
    					if (!isMountVip)
    					{
    						g.drawRegion(imgMount_XD, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    					}
    					else
    					{
    						g.drawRegion(imgMount_XD_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    					}
    				}
    				else if (genderMount == 1)
    				{
    					if (!isMountVip)
    					{
    						g.drawRegion(imgMount_NM, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    					}
    					else
    					{
    						g.drawRegion(imgMount_NM_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    					}
    				}
    			}
    		}
    		else
    		{
    			if (me)
    			{
    				return;
    			}
    			if (idMount >= ID_NEW_MOUNT)
    			{
    				string nameImg2 = strMount + (idMount - ID_NEW_MOUNT) + "_0";
    				FrameImage fraImage2 = mSystem.getFraImage(nameImg2);
    				if (fraImage2 != null)
    				{
    					fraImage2.drawFrame(frameNewMount / 2 % fraImage2.nFrame, xMount, yMount + fy, transMount, 3, g);
    				}
    			}
    			else
    			{
    				if (isSpeacialMount)
    				{
    					return;
    				}
    				if (isEventMount)
    				{
    					g.drawRegion(imgEventMountWing, 0, FrameMount[frameMount] * 60, 60, 60, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    				else
    				{
    					if (!isMount)
    					{
    						return;
    					}
    					if (genderMount == 2)
    					{
    						if (!isMountVip)
    						{
    							g.drawRegion(imgMount_XD, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    						}
    						else
    						{
    							g.drawRegion(imgMount_XD_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    						}
    					}
    					else if (genderMount == 1)
    					{
    						if (!isMountVip)
    						{
    							g.drawRegion(imgMount_NM, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    						}
    						else
    						{
    							g.drawRegion(imgMount_NM_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    						}
    					}
    				}
    			}
    		}
    	}

    	public void paintMount2(mGraphics g)
    	{
    		if (!isPaint()) return;
    		if (xMount <= GameScr.cmx || xMount >= GameScr.cmx + GameCanvas.w) return;
    		if (me && !isEndMount && !isStartMount && !isMount) return;
    		checkLoadMount();
    		if (me)
    		{
    			if (idMount >= ID_NEW_MOUNT)
    			{
    				if (cachedNewMountId1 != idMount) { cachedNewMountId1 = idMount; cachedNewMountName1 = strMount + (idMount - ID_NEW_MOUNT) + "_1"; }
    				FrameImage fraImage = mSystem.getFraImage(cachedNewMountName1);
    				if (fraImage != null)
    				{
    					fraImage.drawFrame(frameNewMount / 2 % fraImage.nFrame, xMount, yMount + fy, transMount, 3, g);
    				}
    			}
    			else if (isSpeacialMount)
    			{
    				checkFrameTick(move);
    				if (Mob.arrMobTemplate[50] != null && Mob.arrMobTemplate[50].data != null)
    				{
    					Mob.arrMobTemplate[50].data.paintFrame(g, fM, xMount + ((cdir != 1) ? 8 : (-8)), yMount + 35, (cdir != 1) ? 1 : 0, 0);
    				}
    				else
    				{
    					getMountData();
    				}
    			}
    			else if (isEventMount)
    			{
    				g.drawRegion(imgEventMount, 0, FrameMount[frameMount] * 60, 60, 60, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    			}
    			else if (genderMount == 0)
    			{
    				if (!isMountVip)
    				{
    					g.drawRegion(imgMount_TD, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    				else
    				{
    					g.drawRegion(imgMount_TD_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    			}
    			else if (genderMount == 1)
    			{
    				if (!isMountVip)
    				{
    					g.drawRegion(imgMount_NM_1, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    				else
    				{
    					g.drawRegion(imgMount_NM_1_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    			}
    		}
    		else
    		{
    			if (me)
    			{
    				return;
    			}
    			if (idMount >= ID_NEW_MOUNT)
    			{
    				string nameImg2 = strMount + (idMount - ID_NEW_MOUNT) + "_1";
    				FrameImage fraImage2 = mSystem.getFraImage(nameImg2);
    				if (fraImage2 != null)
    				{
    					fraImage2.drawFrame(frameNewMount / 2 % fraImage2.nFrame, xMount, yMount + fy, transMount, 3, g);
    				}
    				return;
    			}
    			if (isSpeacialMount)
    			{
    				checkFrameTick(move);
    				if (Mob.arrMobTemplate[50] != null && Mob.arrMobTemplate[50].data != null)
    				{
    					Mob.arrMobTemplate[50].data.paintFrame(g, fM, xMount + ((cdir != 1) ? 8 : (-8)), yMount + 35, (cdir != 1) ? 1 : 0, 0);
    				}
    				else
    				{
    					getMountData();
    				}
    				return;
    			}
    			if (isEventMount)
    			{
    				g.drawRegion(imgEventMount, 0, FrameMount[frameMount] * 60, 60, 60, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    			}
    			if (!isMount)
    			{
    				return;
    			}
    			if (genderMount == 0)
    			{
    				if (!isMountVip)
    				{
    					g.drawRegion(imgMount_TD, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    				else
    				{
    					g.drawRegion(imgMount_TD_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    			}
    			else if (genderMount == 1)
    			{
    				if (!isMountVip)
    				{
    					g.drawRegion(imgMount_NM_1, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    				else
    				{
    					g.drawRegion(imgMount_NM_1_VIP, 0, FrameMount[frameMount] * 40, 50, 40, transMount, xMount + dxMount, yMount + dyMount + fy, 0);
    				}
    			}
    		}
    	}

    	public virtual void paint(mGraphics g)
    	{
    		if (isHide)
    		{
    			return;
    		}
    		if (isMafuba)
    		{
    			paintCharWithoutSkill(g);
    		}
    		else if (isMabuHold)
    		{
    			if (cmtoChar)
    			{
    				GameScr.cmtoX = cx - GameScr.gW2;
    				GameScr.cmtoY = cy - GameScr.gH23;
    				if (!GameCanvas.isTouchControl)
    				{
    					GameScr.cmtoX += GameScr.gW6 * cdir;
    				}
    			}
    		}
    		else
    		{
    			if (!isPaint() || (!me && GameScr.notPaint))
    			{
    				return;
    			}
    			if (petFollow != null)
    			{
    				petFollow.paint(g);
    			}
    			paintMount1(g);
    			if ((TileMap.isInAirMap() && cy >= TileMap.pxh - 48) || isTeleport)
    			{
    				return;
    			}
    			if (holder && GameCanvas.gameTick % 2 == 0)
    			{
    				g.setColor(16185600);
    				if (charHold != null)
    				{
    					g.drawLine(cx, cy - ch / 2, charHold.cx, charHold.cy - charHold.ch / 2);
    				}
    				if (mobHold != null)
    				{
    					g.drawLine(cx, cy - ch / 2, mobHold.x, mobHold.y - mobHold.h / 2);
    				}
    			}
    			paintSuperEffBehind(g);
    			paintAuraBehind(g);
    			paintEffBehind(g);
    			paintEff_Lvup_behind(g);
    			paintEff_Pet(g);
    			if (shadowLife > 0)
    			{
    				if (GameCanvas.gameTick % 2 == 0)
    				{
    					paintCharBody(g, shadowX, shadowY, cdir, 25, true);
    				}
    				else if (shadowLife > 5)
    				{
    					paintCharBody(g, shadowX, shadowY, cdir, 7, true);
    				}
    			}
    			if (!isPaint() && skillPaint != null && (skillPaint.id < 70 || skillPaint.id > 76) && (skillPaint.id < 77 || skillPaint.id > 83))
    			{
    				if (skillPaint != null)
    				{
    					indexSkill = skillInfoPaint().Length;
    					skillPaint = null;
    				}
    				effPaints = null;
    				eff = null;
    				effTask = null;
    				indexEff = -1;
    				indexEffTask = -1;
    			}
    			else if (statusMe != 15 && (moveFast == null || moveFast[0] <= 0))
    			{
    				paintCharName_HP_MP_Overhead(g);
    				if (skillPaint == null || skillInfoPaint() == null || indexSkill >= skillInfoPaint().Length)
    				{
    					paintCharWithoutSkill(g);
    				}
    				if (arr != null)
    				{
    					arr.paint(g);
    				}
    				if (dart != null)
    				{
    					dart.paint(g);
    				}
    				paintEffect(g);
    				if (mobMe != null)
    				{
    				}
    				paintMount2(g);
    				paintEff_Lvup_front(g);
    				paintSuperEffFront(g);
    				paintAuraFront(g);
    				paintEffFront(g);
    				paint_map_line(g);
    			}
    		}
    	}
    }
}

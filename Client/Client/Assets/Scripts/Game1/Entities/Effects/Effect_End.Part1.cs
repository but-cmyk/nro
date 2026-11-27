namespace Game1
{
    using System;
    
    public partial class Effect_End
    {
    
    	private void set_Skill_MaFuba()
    	{
    		nFrame = new byte[9] { 0, 0, 0, 1, 1, 1, 2, 2, 2 };
    		isAddSub = false;
    		fMove = 10;
    		x1000 = x;
    		y1000 = y + 12;
    		dy = 25;
    		dy_throw = 19;
    		if (typeSub == 1)
    		{
    			dy_throw = 21;
    		}
    		else if (typeSub == 2)
    		{
    			dy_throw = 31;
    		}
    		h = fra_skill[1].frameHeight + 50 - dy_throw;
    		vy = 1;
    		vy1000 = 1;
    		y = y1000 - h;
    		rS = 90;
    		vMax = 1;
    		angleS = (angleO = 25);
    		iDotS = 1;
    		if (listObj != null && listObj.Length > 0)
    		{
    			iDotS = listObj.Length;
    		}
    		iAngleS = 360 / iDotS;
    		xArgS = new int[iDotS];
    		yArgS = new int[iDotS];
    		xDotS = new int[iDotS];
    		yDotS = new int[iDotS];
    		GameScr.addEffectEnd(16, 0, typePaint, x1000, y1000, 1, 0, -1, null);
    		SoundMn.playSound(x, y, SoundMn.MAFUBA_0, SoundMn.volume);
    	}
    
    	private void changeAngleStar()
    	{
    		if (vMax < 40)
    		{
    			vMax += 2;
    		}
    		angleS = angleO;
    		angleS -= vMax;
    		if (angleS >= 360)
    		{
    			angleS -= 360;
    		}
    		if (angleS < 0)
    		{
    			angleS = 360 + angleS;
    		}
    		angleO = angleS;
    	}
    
    	private void setDotStar()
    	{
    		for (int i = 0; i < yArgS.Length; i++)
    		{
    			if (angleS >= 360)
    			{
    				angleS -= 360;
    			}
    			if (angleS < 0)
    			{
    				angleS = 360 + angleS;
    			}
    			yArgS[i] = Res.abs(rS * Res.sin(angleS) / 1024);
    			xArgS[i] = Res.abs(rS * Res.cos(angleS) / 1024);
    			if (angleS < 90)
    			{
    				xDotS[i] = x + xArgS[i];
    				yDotS[i] = y - yArgS[i];
    			}
    			else if (angleS >= 90 && angleS < 180)
    			{
    				xDotS[i] = x - xArgS[i];
    				yDotS[i] = y - yArgS[i];
    			}
    			else if (angleS >= 180 && angleS < 270)
    			{
    				xDotS[i] = x - xArgS[i];
    				yDotS[i] = y + yArgS[i];
    			}
    			else
    			{
    				xDotS[i] = x + xArgS[i];
    				yDotS[i] = y + yArgS[i];
    			}
    			angleS -= iAngleS;
    		}
    	}
    
    	private void upd_Skill_MaFuba()
    	{
    		if (stt == 0)
    		{
    			if (f == 3)
    			{
    				SoundMn.playSound(x, y, SoundMn.MAFUBA_1, SoundMn.volume);
    			}
    			frame++;
    			if (frame > nFrame.Length - 1)
    			{
    				frame = nFrame.Length - 1;
    			}
    			if (f == fMove + 4)
    			{
    				GameScr.addEffectEnd(16, 1, typePaint, x, y, 3, 0, 2945, null);
    			}
    			if (f > fMove + 4)
    			{
    				rS--;
    				if (rS < 0)
    				{
    					rS = 0;
    					f = 0;
    					fSpeed = 0;
    					nFrame_2 = new byte[22]
    					{
    						1, 1, 0, 0, 0, 0, 1, 1, 1, 1,
    						0, 0, 0, 1, 1, 1, 0, 0, 1, 1,
    						1, 2
    					};
    					hideListObj_Mafuba(true);
    					stt = 1;
    				}
    				else
    				{
    					changeAngleStar();
    					setDotStar();
    					updListObj_Mafuba(true);
    				}
    			}
    		}
    		else if (stt == 1)
    		{
    			fSpeed++;
    			if (fSpeed > nFrame_2.Length - 1)
    			{
    				fSpeed = nFrame_2.Length - 1;
    				if (GameCanvas.gameTick % 2 == 0)
    				{
    					vy1000++;
    				}
    				vy += vy1000;
    				if (vy >= h - fra_skill[0].frameHeight - dy + dy_throw)
    				{
    					vy = h - fra_skill[0].frameHeight - dy + dy_throw;
    					f = 0;
    					fSpeed = 0;
    					stt = 2;
    					nFrame_2 = new byte[11]
    					{
    						3, 3, 3, 3, 3, 4, 4, 4, 5, 5,
    						5
    					};
    				}
    			}
    		}
    		else if (stt == 2)
    		{
    			fSpeed++;
    			if (fSpeed > nFrame_2.Length - 1)
    			{
    				stt = 3;
    				frame = 0;
    				nFrame = new byte[17]
    				{
    					2, 2, 1, 1, 0, 0, 3, 3, 3, 0,
    					0, 0, 4, 4, 4, 0, 0
    				};
    			}
    		}
    		else if (stt == 3)
    		{
    			frame++;
    			if (frame == 3)
    			{
    				SoundMn.playSound(x, y, SoundMn.MAFUBA_1, SoundMn.volume);
    			}
    			if (frame > nFrame.Length - 1)
    			{
    				frame = 0;
    				stt = 4;
    				nFrame = new byte[51]
    				{
    					0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    					0, 0, 0, 0, 0, 0, 0, 3, 3, 3,
    					0, 0, 0, 4, 4, 4, 0, 0, 0, 0,
    					0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    					0, 0, 0, 0, 0, 3, 3, 0, 0, 4,
    					4
    				};
    			}
    		}
    		else
    		{
    			frame++;
    			if (frame > nFrame.Length - 1)
    			{
    				frame = 0;
    			}
    			if (GameCanvas.timeNow - time >= timeRemove)
    			{
    				GameScr.addEffectEnd(16, 0, typePaint, x1000, y1000, 1, 0, -1, null);
    				updListObj_Mafuba(false);
    				removeEff();
    			}
    		}
    	}
    
    	private void pnt_Skill_MaFuba(mGraphics g)
    	{
    		if (fra_skill == null)
    		{
    			return;
    		}
    		if (nFrame != null)
    		{
    			fra_skill[0].drawFrame(nFrame[frame], x1000, y1000, 0, mGraphics.BOTTOM | mGraphics.HCENTER, g);
    		}
    		if (stt == 1 || stt == 2)
    		{
    			int anchor = mGraphics.BOTTOM | mGraphics.HCENTER;
    			int num = dy;
    			if (nFrame_2[fSpeed] == 0 || nFrame_2[fSpeed] == 1)
    			{
    				anchor = mGraphics.VCENTER | mGraphics.HCENTER;
    				num = 0;
    			}
    			fra_skill[1].drawFrame(nFrame_2[fSpeed], x, y + num + vy, 0, anchor, g);
    		}
    	}
    
    	private void Fill_Rect_Img(mGraphics g, FrameImage head, FrameImage body, FrameImage foot, int frame, int x, int y, int w)
    	{
    		int num = 0;
    		int num2 = w;
    		bool flag = false;
    		if (head != null && foot != null)
    		{
    			flag = true;
    			num2 = w - (head.frameWidth + foot.frameWidth);
    		}
    		if (num2 > 0)
    		{
    			num = num2 / body.frameWidth;
    			if (num2 % body.frameWidth > 0)
    			{
    				num++;
    			}
    			if (dir == -1)
    			{
    				for (int i = 0; i < num; i++)
    				{
    					int num3 = 0;
    					num3 = ((i != num - 1) ? ((!flag) ? (x + i * body.frameWidth) : (x + foot.frameWidth + body.frameWidth + i * body.frameWidth)) : ((!flag) ? (x + w - body.frameWidth) : (x + foot.frameWidth)));
    					body.drawFrame(frame, num3, y - body.frameHeight / 2, 2, 0, g);
    				}
    			}
    			else
    			{
    				for (int j = 0; j < num; j++)
    				{
    					int num4 = 0;
    					num4 = ((j != num - 1) ? ((!flag) ? (x + j * body.frameWidth) : (x + j * body.frameWidth + head.frameWidth)) : ((!flag) ? (x + w - body.frameWidth) : (x + w - (body.frameWidth + foot.frameWidth))));
    					body.drawFrame(frame, num4, y - body.frameHeight / 2, 0, 0, g);
    				}
    			}
    		}
    		if (dir == -1)
    		{
    			if (head != null)
    			{
    				head.drawFrame(frame, x + w - head.frameWidth, y - head.frameHeight / 2, 2, 0, g);
    			}
    			if (foot != null)
    			{
    				foot.drawFrame(frame, x, y - foot.frameHeight / 2, 2, 0, g);
    			}
    		}
    		else
    		{
    			if (head != null)
    			{
    				head.drawFrame(frame, x, y - head.frameHeight / 2, 0, 0, g);
    			}
    			if (foot != null)
    			{
    				foot.drawFrame(frame, x + w - foot.frameWidth - 1, y - foot.frameHeight / 2, 0, 0, g);
    			}
    		}
    	}
    
    	private void set_LINE_IN()
    	{
    		indexColorStar = typeSub;
    		x1000 = x * 1000;
    		y1000 = y * 1000;
    		fRemove = Res.random(4, 6);
    		vMax = 5;
    		xline = 10;
    		yline = 20;
    		create_Star_Line_In(vMax, xline, yline, 0);
    	}
    
    	private void upd_LINE_IN()
    	{
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Line line = (Line)VecEffEnd.elementAt(i);
    			line.update();
    			if (f >= fRemove)
    			{
    				VecEffEnd.removeElement(line);
    				i--;
    			}
    		}
    		if (f >= fRemove)
    		{
    			if (GameCanvas.timeNow - time >= timeRemove)
    			{
    				VecEffEnd.removeAllElements();
    				removeEff();
    			}
    			else
    			{
    				fRemove = Res.random(4, 6);
    				f = 0;
    				create_Star_Line_In(vMax, xline, yline, 0);
    			}
    		}
    	}
    
    	private void create_Star_Line_In(int vline, int minline, int maxline, int numpoint)
    	{
    		if (f == -1)
    		{
    			VecEffEnd.removeAllElements();
    		}
    		int num = 4;
    		colorpaint = new int[num];
    		if (maxline <= minline)
    		{
    			maxline = minline + 1;
    		}
    		for (int i = 0; i < num; i++)
    		{
    			if (Res.random(2) == 0)
    			{
    				colorpaint[i] = colorStar[indexColorStar][Res.random(3)];
    			}
    			else
    			{
    				colorpaint[i] = colorStar[indexColorStar][2];
    			}
    		}
    		for (int j = 0; j < num; j++)
    		{
    			Line line = new Line();
    			int num2 = 5 + 180 / num * j;
    			int num3 = 180 / num + 180 / num * j - 5;
    			if (num3 <= num2)
    			{
    				num3 = num2 + 1;
    			}
    			int num4 = Res.random(minline, maxline);
    			int num5 = Res.random(vline, vline + 3);
    			int num6 = Res.random(num2, num3);
    			int num7 = Res.random(13, 23);
    			bool is2Line = Res.random(4) == 0;
    			num6 = Res.fixangle(num6 % 360);
    			line.setLine(x1000 - Res.sin(num6) * (num4 + num7), y1000 - Res.cos(num6) * (num4 + num7), x1000 - Res.sin(num6) * num7, y1000 - Res.cos(num6) * num7, Res.sin(num6) * num5, Res.cos(num6) * num5, is2Line);
    			if (numpoint > 0)
    			{
    				line.type = Res.random(numpoint);
    			}
    			VecEffEnd.addElement(line);
    			line = new Line();
    			num6 += 180 + Res.random_Am(2, 5);
    			num6 = Res.fixangle(num6 % 360);
    			line.setLine(x1000 - Res.sin(num6) * (num4 + num7), y1000 - Res.cos(num6) * (num4 + num7), x1000 - Res.sin(num6) * num7, y1000 - Res.cos(num6) * num7, Res.sin(num6) * num5, Res.cos(num6) * num5, is2Line);
    			if (numpoint > 0)
    			{
    				line.type = Res.random(numpoint);
    			}
    			VecEffEnd.addElement(line);
    		}
    	}
    
    	private void pnt_LINE_IN(mGraphics g)
    	{
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Line line = (Line)VecEffEnd.elementAt(i);
    			if (line != null)
    			{
    				int color = 0;
    				if (i / 2 < colorpaint.Length)
    				{
    					color = colorpaint[i / 2];
    				}
    				g.setColor(color);
    				g.drawLine(line.x0 / 1000, line.y0 / 1000, line.x1 / 1000, line.y1 / 1000);
    				if (line.is2Line)
    				{
    					g.drawLine(line.x0 / 1000 + 1, line.y0 / 1000, line.x1 / 1000 + 1, line.y1 / 1000);
    				}
    			}
    		}
    	}
    
    	private void set_End_Rock()
    	{
    		fraImgEff = new FrameImage(8);
    		fRemove = Res.random(23, 27);
    		int num = Res.random(1, 3);
    		toY = y - 40;
    		for (int i = 0; i < num; i++)
    		{
    			Point point = new Point();
    			point.x = x + Res.random_Am(0, 20);
    			point.y = y + Res.random_Am_0(7);
    			if (typeEffect == 10)
    			{
    				point.frame = Res.random(0, fraImgEff.nFrame - 2);
    			}
    			else if (typeEffect == 11)
    			{
    				point.frame = Res.random(2, fraImgEff.nFrame);
    			}
    			else
    			{
    				point.frame = Res.random(0, fraImgEff.nFrame);
    			}
    			point.dis = Res.random(2);
    			point.vy = -Res.random(1, 4);
    			VecEffEnd.addElement(point);
    		}
    	}
    
    	private void upd_End_Rock()
    	{
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Point point = (Point)VecEffEnd.elementAt(i);
    			point.update();
    			if (point.y < toY)
    			{
    				VecEffEnd.removeElementAt(i);
    				i--;
    			}
    		}
    		if (f >= fRemove)
    		{
    			removeEff();
    		}
    	}
    
    	private void pnt_End_Rock(mGraphics g)
    	{
    		for (int i = 0; i < VecEffEnd.size(); i++)
    		{
    			Point point = (Point)VecEffEnd.elementAt(i);
    			if (fraImgEff != null)
    			{
    				fraImgEff.drawFrame(point.frame, point.x, point.y, 0, mGraphics.VCENTER | mGraphics.HCENTER, g);
    			}
    		}
    	}
    
    	private void updListObj_Mafuba(bool ismafuba)
    	{
    		if (listObj == null)
    		{
    			return;
    		}
    		for (int i = 0; i < listObj.Length; i++)
    		{
    			if (listObj[i] == null)
    			{
    				continue;
    			}
    			if (listObj[i].type == 0)
    			{
    				Mob mob = GameScr.findMobInMap(listObj[i].id);
    				if (mob != null)
    				{
    					mob.isMafuba = ismafuba;
    					mob.isHide = false;
    					mob.xMFB = xDotS[i];
    					mob.yMFB = yDotS[i];
    				}
    				continue;
    			}
    			Char @char = null;
    			@char = ((Char.myCharz().charID != listObj[i].id) ? GameScr.findCharInMap(listObj[i].id) : Char.myCharz());
    			if (@char != null)
    			{
    				@char.isMafuba = ismafuba;
    				@char.isHide = false;
    				@char.xMFB = xDotS[i];
    				@char.yMFB = yDotS[i];
    			}
    		}
    	}
    
    	private void hideListObj_Mafuba(bool ishide)
    	{
    		if (listObj == null)
    		{
    			return;
    		}
    		for (int i = 0; i < listObj.Length; i++)
    		{
    			if (listObj[i] == null)
    			{
    				continue;
    			}
    			if (listObj[i].type == 0)
    			{
    				Mob mob = GameScr.findMobInMap(listObj[i].id);
    				if (mob != null)
    				{
    					mob.isHide = ishide;
    				}
    				continue;
    			}
    			Char @char = null;
    			@char = ((Char.myCharz().charID != listObj[i].id) ? GameScr.findCharInMap(listObj[i].id) : Char.myCharz());
    			if (@char != null)
    			{
    				@char.isHide = ishide;
    			}
    		}
    	}
    
    	private void get_Img_Skill()
    	{
    		int num = 0;
    		int[] array = null;
    		int[] array2 = null;
    		switch (typeEffect)
    		{
    		case 18:
    			num = 24;
    			array = new int[1];
    			array2 = new int[1] { 9 };
    			break;
    		case 21:
    			num = 24;
    			array = new int[1] { 1 };
    			array2 = new int[1] { 10 };
    			break;
    		case 24:
    			num = 24;
    			array = new int[3] { 2, 3, 4 };
    			array2 = new int[3] { 11, 12, 13 };
    			break;
    		case 19:
    			num = 25;
    			array = new int[1];
    			array2 = new int[1] { 14 };
    			break;
    		case 22:
    			num = 25;
    			array = new int[1] { 1 };
    			array2 = new int[1] { 15 };
    			break;
    		case 17:
    			num = 25;
    			array = new int[1] { 2 };
    			array2 = new int[1] { 16 };
    			break;
    		case 25:
    			num = 25;
    			array = new int[4] { 3, 4, 5, 6 };
    			array2 = new int[4] { 17, 18, 19, 20 };
    			break;
    		case 20:
    			num = 26;
    			array = new int[1];
    			array2 = new int[1] { 21 };
    			break;
    		case 23:
    			num = 26;
    			array = new int[1] { 1 };
    			array2 = new int[1] { 22 };
    			break;
    		case 16:
    			num = 26;
    			if (typeSub == 0)
    			{
    				array = new int[1] { 7 };
    				array2 = new int[1] { 28 };
    			}
    			if (typeSub == 1)
    			{
    				array = new int[1] { 2 };
    				array2 = new int[1] { 23 };
    			}
    			break;
    		case 26:
    		{
    			num = 26;
    			int num2 = 0;
    			int num3 = 0;
    			if (typeSub == 0)
    			{
    				num2 = 4;
    				num3 = 25;
    			}
    			else if (typeSub == 1)
    			{
    				num2 = 5;
    				num3 = 26;
    			}
    			else if (typeSub == 2)
    			{
    				num2 = 6;
    				num3 = 27;
    			}
    			array = new int[2] { num2, 3 };
    			array2 = new int[2] { num3, 24 };
    			break;
    		}
    		}
    		if (array == null || array2 == null)
    		{
    			return;
    		}
    		fra_skill = new FrameImage[array.Length];
    		for (int i = 0; i < array.Length; i++)
    		{
    			string nameImg = "Skills_" + num + "_" + typePaint + "_" + array[i];
    			FrameImage frameImage = mSystem.getFraImage(nameImg);
    			if (frameImage == null)
    			{
    				frameImage = new FrameImage(array2[i]);
    			}
    			if (frameImage != null)
    			{
    				fra_skill[i] = frameImage;
    			}
    		}
    	}
    
    	private void set_Gong()
    	{
    		if (charUse != null)
    		{
    			if (typeEffect == 21)
    			{
    				x = charUse.cx - 3 * charUse.cdir;
    				y = charUse.cy;
    				SoundMn.playSound(x, y, SoundMn.KAMEX10_0, SoundMn.volume);
    			}
    			else if (typeEffect == 22)
    			{
    				x = charUse.cx + 20 * charUse.cdir;
    				y = charUse.cy - 4;
    				SoundMn.playSound(x, y, SoundMn.DESTROY_2, SoundMn.volume);
    			}
    			else if (typeEffect == 23)
    			{
    				x = charUse.cx;
    				y = charUse.cy - 50;
    				SoundMn.playSound(x, y, SoundMn.MAFUBA_2, SoundMn.volume);
    			}
    			else
    			{
    				x = charUse.cx;
    				y = charUse.cy;
    			}
    		}
    	}
    
    	private void upd_Gong()
    	{
    		if (charUse != null)
    		{
    			if (typeEffect == 21)
    			{
    				x = charUse.cx - 3 * charUse.cdir;
    				y = charUse.cy;
    			}
    			else if (typeEffect == 22)
    			{
    				x = charUse.cx + 20 * charUse.cdir;
    				y = charUse.cy - 4;
    			}
    			else if (typeEffect == 23)
    			{
    				x = charUse.cx;
    				y = charUse.cy - 50;
    			}
    			else
    			{
    				x = charUse.cx;
    				y = charUse.cy;
    			}
    		}
    		if (timeRemove > 0)
    		{
    			if (GameCanvas.timeNow - time >= timeRemove)
    			{
    				removeEff();
    			}
    		}
    		else if (f >= fra_skill[0].nFrame * n_frame)
    		{
    			removeEff();
    		}
    	}
    
    	private void pnt_Gong(mGraphics g, int anchor)
    	{
    		if (fra_skill[0] != null)
    		{
    			fra_skill[0].drawFrame(f / n_frame % fra_skill[0].nFrame, x, y, dir_nguoc, anchor, g);
    		}
    	}
    
    	private void set_Pow()
    	{
    		nFrame = null;
    		n_frame = 3;
    		if (typeEffect == 18)
    		{
    			if (typeSub == 0)
    			{
    				nFrame = new byte[9] { 0, 0, 0, 1, 1, 1, 2, 2, 2 };
    			}
    			else
    			{
    				nFrame = new byte[12]
    				{
    					3, 3, 3, 4, 4, 4, 5, 5, 5, 6,
    					6, 6
    				};
    			}
    		}
    	}
    
    	private void upd_Pow()
    	{
    		if (charUse != null)
    		{
    			x = charUse.cx;
    			y = charUse.cy + 13;
    		}
    		if (timeRemove > 0)
    		{
    			if (GameCanvas.timeNow - time >= timeRemove)
    			{
    				removeEff();
    			}
    		}
    		else if (nFrame != null)
    		{
    			if (f > nFrame.Length)
    			{
    				removeEff();
    			}
    		}
    		else if (f >= fra_skill[0].nFrame * n_frame)
    		{
    			removeEff();
    		}
    	}
    
    	private void pnt_Pow(mGraphics g, int anchor)
    	{
    		if (fra_skill[0] != null)
    		{
    			if (nFrame != null)
    			{
    				fra_skill[0].drawFrame(nFrame[f % nFrame.Length], x, y, dir_nguoc, anchor, g);
    			}
    			else
    			{
    				fra_skill[0].drawFrame(f / n_frame % fra_skill[0].nFrame, x, y, dir_nguoc, anchor, g);
    			}
    		}
    	}
    
    	private void set_Sub()
    	{
    		if (typeEffect == 17)
    		{
    			x += ((dir != 0) ? (-fra_skill[0].frameWidth) : 0);
    		}
    	}
    
    	private void upd_Sub()
    	{
    		if (timeRemove > 0)
    		{
    			if (GameCanvas.timeNow - time >= timeRemove)
    			{
    				removeEff();
    			}
    		}
    		else if (f >= fra_skill[0].nFrame * n_frame)
    		{
    			removeEff();
    		}
    	}
    
    	private void pnt_Sub(mGraphics g, int anchor)
    	{
    		fra_skill[0].drawFrame(f / n_frame % fra_skill[0].nFrame, x, y, dir, anchor, g);
    	}
    
    	private void set_()
    	{
    	}
    
    	private void upd_()
    	{
    	}
    
    	private void pnt_(mGraphics g)
    	{
    	}
    }
}

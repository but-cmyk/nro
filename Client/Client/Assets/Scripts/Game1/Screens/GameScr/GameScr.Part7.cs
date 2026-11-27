namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameScr : mScreen, IChatable
    {
    
    	public void actMenu()
    	{
    		GameCanvas.panel.setTypeMain();
    		GameCanvas.panel.show();
    	}
    
    	public void openUIZone(Message message)
    	{
    		InfoDlg.hide();
    		try
    		{
    			zones = new int[message.reader().readByte()];
    			pts = new int[zones.Length];
    			numPlayer = new int[zones.Length];
    			maxPlayer = new int[zones.Length];
    			rank1 = new int[zones.Length];
    			rankName1 = new string[zones.Length];
    			rank2 = new int[zones.Length];
    			rankName2 = new string[zones.Length];
    			for (int i = 0; i < zones.Length; i++)
    			{
    				zones[i] = message.reader().readByte();
    				pts[i] = message.reader().readByte();
    				numPlayer[i] = message.reader().readByte();
    				maxPlayer[i] = message.reader().readByte();
    				sbyte b = message.reader().readByte();
    				if (b == 1)
    				{
    					rankName1[i] = message.reader().readUTF();
    					rank1[i] = message.reader().readInt();
    					rankName2[i] = message.reader().readUTF();
    					rank2[i] = message.reader().readInt();
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham OPEN UIZONE " + ex.ToString());
    		}
    		GameCanvas.panel.setTypeZone();
    		GameCanvas.panel.show();
    	}
    
    	public void showViewInfo()
    	{
    		indexMenu = 3;
    		isPaintInfoMe = true;
    		setPopupSize(175, 200);
    	}
    
    	private void actDead()
    	{
    		MyVector myVector = new MyVector();
    		myVector.addElement(new Command(mResources.DIES[1], 110381));
    		myVector.addElement(new Command(mResources.DIES[2], 110382));
    		myVector.addElement(new Command(mResources.DIES[3], 110383));
    		GameCanvas.menu.startAt(myVector, 3);
    	}
    
    	public void startYesNoPopUp(string info, Command cmdYes, Command cmdNo)
    	{
    		popUpYesNo = new PopUpYesNo();
    		popUpYesNo.setPopUp(info, cmdYes, cmdNo);
    	}
    
    	public void player_vs_player(int playerId, int xu, string info, sbyte typePK)
    	{
    		Char @char = findCharInMap(playerId);
    		if (@char != null)
    		{
    			if (typePK == 3)
    			{
    				startYesNoPopUp(info, new Command(mResources.OK, 2000, @char), new Command(mResources.CLOSE, 2009, @char));
    			}
    			if (typePK == 4)
    			{
    				startYesNoPopUp(info, new Command(mResources.OK, 2005, @char), new Command(mResources.CLOSE, 2009, @char));
    			}
    		}
    	}
    
    	public void giaodich(int playerID)
    	{
    		Char @char = findCharInMap(playerID);
    		if (@char != null)
    		{
    			startYesNoPopUp(@char.cName + mResources.want_to_trade, new Command(mResources.YES, 11114, @char), new Command(mResources.NO, 2009, @char));
    		}
    	}
    
    	public void getFlagImage(int charID, sbyte cflag)
    	{
    		if (vFlag.size() == 0)
    		{
    			Service.gI().getFlag(2, cflag);
    			return;
    		}
    		if (charID == Char.myCharz().charID)
    		{
    			if (Char.myCharz().isGetFlagImage(cflag))
    			{
    				for (int i = 0; i < vFlag.size(); i++)
    				{
    					PKFlag pKFlag = (PKFlag)vFlag.elementAt(i);
    					if (pKFlag != null && pKFlag.cflag == cflag)
    					{
    						Char.myCharz().flagImage = pKFlag.IDimageFlag;
    					}
    				}
    			}
    			else if (!Char.myCharz().isGetFlagImage(cflag))
    			{
    				Service.gI().getFlag(2, cflag);
    			}
    			return;
    		}
    		if (findCharInMap(charID) == null)
    		{
    			return;
    		}
    		if (findCharInMap(charID).isGetFlagImage(cflag))
    		{
    			for (int j = 0; j < vFlag.size(); j++)
    			{
    				PKFlag pKFlag2 = (PKFlag)vFlag.elementAt(j);
    				if (pKFlag2 != null && pKFlag2.cflag == cflag)
    				{
    					findCharInMap(charID).flagImage = pKFlag2.IDimageFlag;
    				}
    			}
    		}
    		else if (!findCharInMap(charID).isGetFlagImage(cflag))
    		{
    			Service.gI().getFlag(2, cflag);
    		}
    	}
    
    	public void actionPerform(int idAction, object p)
    	{
    		Cout.println("PERFORM WITH ID = " + idAction);
    		switch (idAction)
    		{
    		case 888351:
    			Service.gI().petStatus(5);
    			GameCanvas.endDlg();
    			break;
    		case 11112:
    		{
    			Char @char = (Char)p;
    			Service.gI().friend(1, @char.charID);
    			break;
    		}
    		case 11113:
    		{
    			Char char2 = (Char)p;
    			if (char2 != null)
    			{
    				Service.gI().giaodich(0, char2.charID, -1, -1);
    			}
    			break;
    		}
    		case 11114:
    		{
    			popUpYesNo = null;
    			GameCanvas.endDlg();
    			Char char3 = (Char)p;
    			if (char3 != null)
    			{
    				Service.gI().giaodich(1, char3.charID, -1, -1);
    			}
    			break;
    		}
    		case 11111:
    			if (Char.myCharz().charFocus != null)
    			{
    				InfoDlg.showWait();
    				if (GameCanvas.panel.vPlayerMenu.size() <= 0)
    				{
    					playerMenu(Char.myCharz().charFocus);
    				}
    				GameCanvas.panel.setTypePlayerMenu(Char.myCharz().charFocus);
    				GameCanvas.panel.show();
    				Service.gI().getPlayerMenu(Char.myCharz().charFocus.charID);
    				Service.gI().messagePlayerMenu(Char.myCharz().charFocus.charID);
    			}
    			break;
    		case 11115:
    			if (Char.myCharz().charFocus != null)
    			{
    				InfoDlg.showWait();
    				Service.gI().playerMenuAction(Char.myCharz().charFocus.charID, (short)Char.myCharz().charFocus.menuSelect);
    			}
    			break;
    		case 2000:
    			popUpYesNo = null;
    			GameCanvas.endDlg();
    			if ((Char)p == null)
    			{
    				Service.gI().player_vs_player(1, 3, -1);
    				break;
    			}
    			Service.gI().player_vs_player(1, 3, ((Char)p).charID);
    			Service.gI().charMove();
    			break;
    		case 2001:
    			GameCanvas.endDlg();
    			break;
    		case 2003:
    			GameCanvas.endDlg();
    			InfoDlg.showWait();
    			Service.gI().player_vs_player(0, 3, Char.myCharz().charFocus.charID);
    			break;
    		case 2004:
    			GameCanvas.endDlg();
    			Service.gI().player_vs_player(0, 4, Char.myCharz().charFocus.charID);
    			break;
    		case 2005:
    			GameCanvas.endDlg();
    			popUpYesNo = null;
    			if ((Char)p == null)
    			{
    				Service.gI().player_vs_player(1, 4, -1);
    			}
    			else
    			{
    				Service.gI().player_vs_player(1, 4, ((Char)p).charID);
    			}
    			break;
    		case 2009:
    			popUpYesNo = null;
    			break;
    		case 2006:
    			GameCanvas.endDlg();
    			Service.gI().player_vs_player(2, 4, Char.myCharz().charFocus.charID);
    			break;
    		case 2007:
    			GameCanvas.endDlg();
    			GameMidlet.instance.exit();
    			break;
    		case 11038:
    			actDead();
    			break;
    		case 110382:
    			Service.gI().returnTownFromDead();
    			break;
    		case 110383:
    			Service.gI().wakeUpFromDead();
    			break;
    		case 1:
    			GameCanvas.endDlg();
    			break;
    		case 2:
    			GameCanvas.menu.showMenu = false;
    			break;
    		case 8002:
    			doFire(false, true);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    			break;
    		case 11057:
    		{
    			Effect2.vEffect2Outside.removeAllElements();
    			Effect2.vEffect2.removeAllElements();
    			Npc npc = (Npc)p;
    			if (npc.idItem == 0)
    			{
    				Service.gI().confirmMenu((short)npc.template.npcTemplateId, (sbyte)GameCanvas.menu.menuSelectedItem);
    			}
    			else if (GameCanvas.menu.menuSelectedItem == 0)
    			{
    				Service.gI().pickItem(npc.idItem);
    			}
    			break;
    		}
    		case 11000:
    			actMenu();
    			break;
    		case 11001:
    			Char.myCharz().findNextFocusByKey();
    			break;
    		case 11002:
    			GameCanvas.panel.hide();
    			break;
    		case 11120:
    		{
    			object[] array2 = (object[])p;
    			Skill skill4 = (Skill)array2[0];
    			int num2 = int.Parse((string)array2[1]);
    			for (int j = 0; j < onScreenSkill.Length; j++)
    			{
    				if (onScreenSkill[j] == skill4)
    				{
    					onScreenSkill[j] = null;
    				}
    			}
    			onScreenSkill[num2] = skill4;
    			saveonScreenSkillToRMS();
    			break;
    		}
    		case 11121:
    		{
    			object[] array = (object[])p;
    			Skill skill3 = (Skill)array[0];
    			int num = int.Parse((string)array[1]);
    			for (int i = 0; i < keySkill.Length; i++)
    			{
    				if (keySkill[i] == skill3)
    				{
    					keySkill[i] = null;
    				}
    			}
    			keySkill[num] = skill3;
    			saveKeySkillToRMS();
    			break;
    		}
    		case 110001:
    			GameCanvas.panel.setTypeMain();
    			GameCanvas.panel.show();
    			break;
    		case 110004:
    			GameCanvas.menu.showMenu = false;
    			break;
    		case 11067:
    			if (TileMap.zoneID != indexSelect)
    			{
    				Service.gI().requestChangeZone(indexSelect, indexItemUse);
    				InfoDlg.showWait();
    			}
    			else
    			{
    				info1.addInfo(mResources.ZONE_HERE, 0);
    			}
    			break;
    		case 11059:
    		{
    			Skill skill2 = onScreenSkill[selectedIndexSkill];
    			doUseSkill(skill2, false);
    			center = null;
    			break;
    		}
    		case 12000:
    			Service.gI().getClan(1, -1, null);
    			break;
    		case 12001:
    			GameCanvas.endDlg();
    			break;
    		case 12002:
    		{
    			GameCanvas.endDlg();
    			ClanObject clanObject = (ClanObject)p;
    			Service.gI().clanInvite(1, -1, clanObject.clanID, clanObject.code);
    			popUpYesNo = null;
    			break;
    		}
    		case 12003:
    		{
    			ClanObject clanObject = (ClanObject)p;
    			GameCanvas.endDlg();
    			Service.gI().clanInvite(2, -1, clanObject.clanID, clanObject.code);
    			popUpYesNo = null;
    			break;
    		}
    		case 12004:
    		{
    			Skill skill = (Skill)p;
    			doUseSkill(skill, true);
    			Char.myCharz().saveLoadPreviousSkill();
    			break;
    		}
    		case 110391:
    			Service.gI().clanInvite(0, Char.myCharz().charFocus.charID, -1, -1);
    			break;
    		case 12005:
    			if (GameCanvas.serverScr == null)
    			{
    				GameCanvas.serverScr = new ServerScr();
    			}
    			GameCanvas.serverScr.switchToMe();
    			GameCanvas.endDlg();
    			break;
    		case 12006:
    			GameMidlet.instance.exit();
    			break;
    		}
    	}
    
    	private static void setTouchBtn()
    	{
    		if (isAnalog != 0)
    		{
    			xTG = (xF = GameCanvas.w - 45);
    			if (gamePad.isLargeGamePad)
    			{
    				xSkill = gamePad.wZone + 20;
    				wSkill = 35;
    				xHP = xF - 45;
    			}
    			else if (gamePad.isMediumGamePad)
    			{
    				xHP = xF - 45;
    			}
    			yF = GameCanvas.h - 45;
    			yTG = yF - 45;
    		}
    	}
    
    	private void updateGamePad()
    	{
    		if (isAnalog == 0 || Char.myCharz().statusMe == 14)
    		{
    			return;
    		}
    		if (GameCanvas.isPointerHoldIn(xF, yF, 40, 40))
    		{
    			mScreen.keyTouch = 5;
    			if (GameCanvas.isPointerJustRelease)
    			{
    				GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = true;
    				GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    			}
    		}
    		gamePad.update();
    		if (GameCanvas.isPointerHoldIn(xTG, yTG, 34, 34))
    		{
    			mScreen.keyTouch = 13;
    			GameCanvas.isPointerJustDown = false;
    			isPointerDowning = false;
    			if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    			{
    				Char.myCharz().findNextFocusByKey();
    				GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    			}
    		}
    	}
    
    	private void paintGamePad(mGraphics g)
    	{
    		if (isAnalog != 0 && Char.myCharz().statusMe != 14)
    		{
    			g.drawImage((mScreen.keyTouch != 5 && mScreen.keyMouse != 5) ? imgFire0 : imgFire1, xF + 20, yF + 20, mGraphics.HCENTER | mGraphics.VCENTER);
    			gamePad.paint(g);
    			g.drawImage((mScreen.keyTouch != 13) ? imgFocus : imgFocus2, xTG + 20, yTG + 20, mGraphics.HCENTER | mGraphics.VCENTER);
    		}
    	}
    
    	public void showWinNumber(string num, string finish)
    	{
    		winnumber = new int[num.Length];
    		randomNumber = new int[num.Length];
    		tMove = new int[num.Length];
    		moveCount = new int[num.Length];
    		delayMove = new int[num.Length];
    		try
    		{
    			for (int i = 0; i < num.Length; i++)
    			{
    				winnumber[i] = short.Parse(num[i].ToString());
    				randomNumber[i] = Res.random(0, 11);
    				tMove[i] = 1;
    				delayMove[i] = 0;
    			}
    		}
    		catch (Exception)
    		{
    		}
    		tShow = 100;
    		moveIndex = 0;
    		strFinish = finish;
    		lastXS = (currXS = mSystem.currentTimeMillis());
    	}
    
    	public void chatVip(string chatVip)
    	{
    		if (!startChat)
    		{
    			currChatWidth = mFont.tahoma_7b_yellowSmall.getWidth(chatVip);
    			xChatVip = GameCanvas.w;
    			startChat = true;
    		}
    		if (chatVip.StartsWith("!"))
    		{
    			chatVip = chatVip.Substring(1, chatVip.Length);
    			isFireWorks = true;
    		}
    		vChatVip.addElement(chatVip);
    	}
    
    	public void clearChatVip()
    	{
    		vChatVip.removeAllElements();
    		xChatVip = GameCanvas.w;
    		startChat = false;
    	}
    
    	public void paintChatVip(mGraphics g)
    	{
    		if (vChatVip.size() != 0 && isPaintChatVip)
    		{
    			g.setClip(0, GameCanvas.h - 13, GameCanvas.w, 15);
    			g.fillRect(0, GameCanvas.h - 13, GameCanvas.w, 15, 0, 90);
    			string st = (string)vChatVip.elementAt(0);
    			mFont.tahoma_7b_yellow.drawString(g, st, xChatVip, GameCanvas.h - 13, 0, mFont.tahoma_7b_dark);
    		}
    	}
    
    	public void updateChatVip()
    	{
    		if (!startChat)
    		{
    			return;
    		}
    		xChatVip -= 2;
    		if (xChatVip < -currChatWidth)
    		{
    			xChatVip = GameCanvas.w;
    			vChatVip.removeElementAt(0);
    			if (vChatVip.size() == 0)
    			{
    				isFireWorks = false;
    				startChat = false;
    			}
    			else
    			{
    				currChatWidth = mFont.tahoma_7b_white.getWidth((string)vChatVip.elementAt(0));
    			}
    		}
    	}
    
    	public void showYourNumber(string strNum)
    	{
    		yourNumber = strNum;
    		strPaint = mFont.tahoma_7.splitFontArray(yourNumber, 500);
    	}
    
    	public static void checkRemoveImage()
    	{
    		ImgByName.checkDelHash(ImgByName.hashImagePath, 10, false);
    	}
    
    	public static void StartServerPopUp(string strMsg)
    	{
    		GameCanvas.endDlg();
    		int avatar = 1139;
    		Npc npc = new Npc(-1, 0, 0, 0, 0, 0);
    		npc.avatar = avatar;
    		ChatPopup.addBigMessage(strMsg, 100000, npc);
    		ChatPopup.serverChatPopUp.cmdMsg1 = new Command(mResources.CLOSE, ChatPopup.serverChatPopUp, 1001, null);
    		ChatPopup.serverChatPopUp.cmdMsg1.x = GameCanvas.w / 2 - 35;
    		ChatPopup.serverChatPopUp.cmdMsg1.y = GameCanvas.h - 35;
    	}
    
    	public static bool ispaintPhubangBar()
    	{
    		if (TileMap.mapPhuBang() && phuban_Info.type_PB == 0)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void paintPhuBanBar(mGraphics g, int x, int y, int w)
    	{
    		if (phuban_Info == null || isPaintOther || isPaintRada != 1 || GameCanvas.panel.isShow || !ispaintPhubangBar())
    		{
    			return;
    		}
    		if (w < fra_PVE_Bar_1.frameWidth + fra_PVE_Bar_0.frameWidth * 4)
    		{
    			w = fra_PVE_Bar_1.frameWidth + fra_PVE_Bar_0.frameWidth * 4;
    		}
    		if (x > GameCanvas.w - w / 2)
    		{
    			x = GameCanvas.w - w / 2;
    		}
    		if (x < mGraphics.getImageWidth(imgKhung) + w / 2 + 10)
    		{
    			x = mGraphics.getImageWidth(imgKhung) + w / 2 + 10;
    		}
    		int frameHeight = fra_PVE_Bar_0.frameHeight;
    		int num = y + frameHeight + mGraphics.getImageHeight(imgBall) / 2 + 2;
    		int frameWidth = fra_PVE_Bar_1.frameWidth;
    		int num2 = w / 2 - frameWidth / 2;
    		int num3 = x - w / 2;
    		int num4 = x + frameWidth / 2;
    		int y2 = y + 3;
    		int num5 = num2 - fra_PVE_Bar_0.frameWidth;
    		int num6 = num5 / fra_PVE_Bar_0.frameWidth;
    		if (num5 % fra_PVE_Bar_0.frameWidth > 0)
    		{
    			num6++;
    		}
    		for (int i = 0; i < num6; i++)
    		{
    			if (i < num6 - 1)
    			{
    				fra_PVE_Bar_0.drawFrame(1, num3 + fra_PVE_Bar_0.frameWidth + i * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    			}
    			else
    			{
    				fra_PVE_Bar_0.drawFrame(1, num3 + num5, y2, 0, 0, g);
    			}
    			if (i < num6 - 1)
    			{
    				fra_PVE_Bar_0.drawFrame(1, num4 + i * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    			}
    			else
    			{
    				fra_PVE_Bar_0.drawFrame(1, num4 + num5 - fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    			}
    		}
    		fra_PVE_Bar_0.drawFrame(0, num3, y2, 2, 0, g);
    		fra_PVE_Bar_0.drawFrame(0, num4 + num5, y2, 0, 0, g);
    		if (phuban_Info.pointTeam1 > 0)
    		{
    			int idx = 2;
    			int idx2 = 3;
    			if (phuban_Info.color_1 == 4)
    			{
    				idx = 4;
    				idx2 = 5;
    			}
    			int num7 = phuban_Info.pointTeam1 * num2 / phuban_Info.maxPoint;
    			if (num7 < 0)
    			{
    				num7 = 0;
    			}
    			if (num7 > num2)
    			{
    				num7 = num2;
    			}
    			g.setClip(num3 + num2 - num7, y2, num7, frameHeight);
    			for (int j = 0; j < num6; j++)
    			{
    				if (j < num6 - 1)
    				{
    					fra_PVE_Bar_0.drawFrame(idx2, num3 + fra_PVE_Bar_0.frameWidth + j * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    				}
    				else
    				{
    					fra_PVE_Bar_0.drawFrame(idx2, num3 + num5, y2, 0, 0, g);
    				}
    			}
    			fra_PVE_Bar_0.drawFrame(idx, num3, y2, 2, 0, g);
    			GameCanvas.resetTrans(g);
    		}
    		if (phuban_Info.pointTeam2 > 0)
    		{
    			int idx3 = 2;
    			int idx4 = 3;
    			if (phuban_Info.color_2 == 4)
    			{
    				idx3 = 4;
    				idx4 = 5;
    			}
    			int num8 = phuban_Info.pointTeam2 * num2 / phuban_Info.maxPoint;
    			if (num8 < 0)
    			{
    				num8 = 0;
    			}
    			if (num8 > num2)
    			{
    				num8 = num2;
    			}
    			g.setClip(num4, y2, num8, frameHeight);
    			for (int k = 0; k < num6; k++)
    			{
    				if (k < num6 - 1)
    				{
    					fra_PVE_Bar_0.drawFrame(idx4, num4 + k * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    				}
    				else
    				{
    					fra_PVE_Bar_0.drawFrame(idx4, num4 + num5 - fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    				}
    			}
    			fra_PVE_Bar_0.drawFrame(idx3, num4 + num5, y2, 0, 0, g);
    			GameCanvas.resetTrans(g);
    		}
    		fra_PVE_Bar_1.drawFrame(0, x - frameWidth / 2, y, 0, 0, g);
    		string timeCountDown = mSystem.getTimeCountDown(phuban_Info.timeStart, phuban_Info.timeSecond, true, false);
    		mFont.tahoma_7b_yellow.drawString(g, timeCountDown, x + 1, y + fra_PVE_Bar_1.frameHeight / 2 - mFont.tahoma_7b_green2.getHeight() / 2, 2);
    		Panel.setTextColor(phuban_Info.color_1, 1).drawString(g, phuban_Info.nameTeam1, x - 5, num + 5, 1);
    		Panel.setTextColor(phuban_Info.color_2, 1).drawString(g, phuban_Info.nameTeam2, x + 5, num + 5, 0);
    		if (phuban_Info.type_PB != 0)
    		{
    			int y3 = y + frameHeight / 2 - 2;
    			mFont.bigNumber_While.drawString(g, string.Empty + phuban_Info.pointTeam1, num3 + num2 / 2, y3, 2);
    			mFont.bigNumber_While.drawString(g, string.Empty + phuban_Info.pointTeam2, num4 + num2 / 2, y3, 2);
    		}
    		g.drawImage(imgVS, x, y + fra_PVE_Bar_1.frameHeight + 2, 3);
    		if (phuban_Info.type_PB == 0)
    		{
    			paintChienTruong_Life(g, phuban_Info.maxLife, phuban_Info.color_1, phuban_Info.lifeTeam1, x - 13, phuban_Info.color_2, phuban_Info.lifeTeam2, x + 13, num);
    		}
    	}
    
    	public static void paintChienTruong_Life(mGraphics g, int maxLife, int cl1, int lifeTeam1, int x1, int cl2, int lifeTeam2, int x2, int y)
    	{
    		if (imgBall == null)
    		{
    			return;
    		}
    		int num = mGraphics.getImageHeight(imgBall) / 2;
    		for (int i = 0; i < maxLife; i++)
    		{
    			int num2 = 0;
    			if (i < lifeTeam1)
    			{
    				num2 = 1;
    			}
    			g.drawRegion(imgBall, 0, num2 * num, mGraphics.getImageWidth(imgBall), num, 0, x1 - i * (num + 1), y, mGraphics.VCENTER | mGraphics.HCENTER);
    		}
    		for (int j = 0; j < maxLife; j++)
    		{
    			int num3 = 0;
    			if (j < lifeTeam2)
    			{
    				num3 = 1;
    			}
    			g.drawRegion(imgBall, 0, num3 * num, mGraphics.getImageWidth(imgBall), num, 0, x2 + j * (num + 1), y, mGraphics.VCENTER | mGraphics.HCENTER);
    		}
    	}
    
    	public static void paintHPBar_NEW(mGraphics g, int x, int y, Char c)
    	{
    		g.drawImage(imgKhung, x, y, 0);
    		int x2 = x + 3;
    		int num = y + 19;
    		int num2 = 0;
    		int num3 = 0;
    		int width = imgHP_NEW.getWidth();
    		int num4 = imgHP_NEW.getHeight() / 2;
    		num2 = (c.cHPFull > 0) ? (int)(c.cHP * width / c.cHPFull) : 0;
    		if (num2 <= 0)
    		{
    			num2 = 1;
    		}
    		else if (num2 > width)
    		{
    			num2 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, num4, num2, num4, 0, x2, num, 0);
    		num3 = (c.cMPFull > 0) ? (int)(c.cMP * width / c.cMPFull) : 0;
    		if (num3 <= 0)
    		{
    			num3 = 1;
    		}
    		else if (num3 > width)
    		{
    			num3 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, 0, num3, num4, 0, x2, num + 6, 0);
    		int x3 = x + imgKhung.getWidth() / 2 + 1;
    		int y2 = num + 13;
    		mFont.tahoma_7_green2.drawString(g, c.cName, x3, y + 4, 2);
    		if (c.mobFocus != null)
    		{
    			if (c.mobFocus.getTemplate() != null)
    			{
    				mFont.tahoma_7_green2.drawString(g, c.mobFocus.getTemplate().name, x3, y2, 2);
    			}
    		}
    		else if (c.npcFocus != null)
    		{
    			mFont.tahoma_7_green2.drawString(g, c.npcFocus.template.name, x3, y2, 2);
    		}
    		else if (c.charFocus != null)
    		{
    			mFont.tahoma_7_green2.drawString(g, c.charFocus.cName, x3, y2, 2);
    		}
    	}
    
    	public static void addEffectEnd(int type, int subtype, int typePaint, int x, int y, int levelPaint, int dir, short timeRemove, Point[] listObj)
    	{
    		Effect_End eff = new Effect_End(type, subtype, typePaint, x, y, levelPaint, dir, timeRemove, listObj);
    		addEffect2Vector(eff);
    	}
    
    	public static void addEffectEnd_Target(int type, int subtype, int typePaint, Char charUse, Point target, int levelPaint, short timeRemove, short range)
    	{
    		Effect_End eff = new Effect_End(type, subtype, typePaint, charUse.clone(), target, levelPaint, timeRemove, range);
    		addEffect2Vector(eff);
    	}
    
    	public static void addEffect2Vector(Effect_End eff)
    	{
    		if (eff.levelPaint == 0)
    		{
    			EffectManager.addHiEffect(eff);
    		}
    		else if (eff.levelPaint == 1)
    		{
    			EffectManager.addMidEffects(eff);
    		}
    		else if (eff.levelPaint == 2)
    		{
    			EffectManager.addMid_2Effects(eff);
    		}
    		else
    		{
    			EffectManager.addLowEffect(eff);
    		}
    	}
    
    	public static bool setIsInScreen(int x, int y, int wOne, int hOne)
    	{
    		if (x < cmx - wOne || x > cmx + GameCanvas.w + wOne || y < cmy - hOne || y > cmy + GameCanvas.h + hOne * 3 / 2)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public static bool isSmallScr()
    	{
    		if (GameCanvas.w <= 320)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	private void paint_xp_bar(mGraphics g)
    	{
    		g.setColor(8421504);
    		g.fillRect(0, GameCanvas.h - 2, GameCanvas.w, 2);
    		int w = (int)(Char.myCharz().cLevelPercent * GameCanvas.w / 10000);
    		g.setColor(16777215);
    		g.fillRect(0, GameCanvas.h - 2, w, 2);
    		g.setColor(0);
    		w = GameCanvas.w / 10;
    		for (int i = 1; i < 10; i++)
    		{
    			g.fillRect(i * w, GameCanvas.h - 2, 1, 2);
    		}
    	}
    
    	private void paint_ios_bg(mGraphics g)
    	{
    		if (mSystem.clientType == 5)
    		{
    			if (imgBgIOS != null)
    			{
    				g.setColor(16777215);
    				g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    				g.drawImage(imgBgIOS, GameCanvas.w / 2, GameCanvas.h / 2, mGraphics.VCENTER | mGraphics.HCENTER);
    			}
    			else
    			{
    				int num = ((TileMap.bgID % 2 != 0) ? 1 : 2);
    				imgBgIOS = GameCanvas.loadImage("/bg/bg_ios_" + num + ".png");
    			}
    		}
    	}
    
    	public void paint_CT(mGraphics g, int x, int y, int w)
    	{
    		w = 194;
    		w = 182;
    		w = 170;
    		int num = 66;
    		int num2 = 11;
    		if (x > GameCanvas.w - w / 2)
    		{
    			x = GameCanvas.w - w / 2;
    		}
    		if (x < mGraphics.getImageWidth(imgKhung) + w / 2 + 10)
    		{
    			x = mGraphics.getImageWidth(imgKhung) + w / 2 + 10;
    		}
    		int frameHeight = fra_PVE_Bar_0.frameHeight;
    		int num3 = y + frameHeight + mGraphics.getImageHeight(imgBall) / 2 + 2;
    		int frameWidth = fra_PVE_Bar_1.frameWidth;
    		int num4 = w / 2 - frameWidth / 2;
    		int num5 = x - w / 2 + 3;
    		int num6 = x + frameWidth / 2;
    		int num7 = y + 3;
    		int num8 = num4 - fra_PVE_Bar_0.frameWidth;
    		int num9 = num8 / fra_PVE_Bar_0.frameWidth;
    		if (num8 % fra_PVE_Bar_0.frameWidth > 0)
    		{
    			num9++;
    		}
    		for (int i = 0; i < num9; i++)
    		{
    			if (i < num9 - 1)
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5 + fra_PVE_Bar_0.frameWidth + i * fra_PVE_Bar_0.frameWidth, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    			else
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5 + num8, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    			if (i < num9 - 1)
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num6 + i * fra_PVE_Bar_0.frameWidth, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    			else
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num6 + num8 - fra_PVE_Bar_0.frameWidth, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    		}
    		fra_PVE_Bar_0.drawFrame(0, num5, num7, 2, 0, g);
    		fra_PVE_Bar_0.drawFrame(0, num6 + num8, num7, 0, 0, g);
    		int num10 = nCT_TeamA * 100 / (nCT_nBoyBaller / 2) * num / 100;
    		if (num10 > 0)
    		{
    			if (num10 < 6)
    			{
    				num10 = 6;
    			}
    			g.setClip(num5, num7, num10, 15);
    		}
    		if (nCT_TeamA > 0)
    		{
    			for (int j = 0; j < num2; j++)
    			{
    				if (j == 0)
    				{
    					g.drawRegion(img_ct_bar_0, 0, 60, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    				else
    				{
    					g.drawRegion(img_ct_bar_0, 0, 75, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5 + j * 6, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    			}
    		}
    		GameCanvas.resetTrans(g);
    		int num11 = nCT_TeamB * 100 / (nCT_nBoyBaller / 2) * num / 100;
    		if (num - (num - num11) > 0)
    		{
    			if (num11 < 6)
    			{
    				num11 = 6;
    			}
    			g.setClip(num6 + num - num11, num7, num - (num - num11), 15);
    		}
    		if (nCT_TeamB > 0)
    		{
    			for (int k = 0; k < num2; k++)
    			{
    				if (k == 0)
    				{
    					g.drawRegion(img_ct_bar_0, 0, 30, mGraphics.getImageWidth(img_ct_bar_0), 15, 0, num6 + num8, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    				else
    				{
    					g.drawRegion(img_ct_bar_0, 0, 45, mGraphics.getImageWidth(img_ct_bar_0), 15, 0, num6 + num8 - k * 6, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    			}
    		}
    		GameCanvas.resetTrans(g);
    		fra_PVE_Bar_1.drawFrame(0, x - frameWidth / 2 + 1, y, 0, 0, g);
    		string st = NinjaUtil.getTime((int)((nCT_timeBallte - mSystem.currentTimeMillis()) / 1000)) + string.Empty;
    		mFont.tahoma_7b_yellow.drawString(g, st, num5 + w / 2 - 2, y + 5, 2);
    		mFont.tahoma_7_grey.drawString(g, "Tầng " + nCT_floor, num5 + w / 2 - 3, y + fra_PVE_Bar_1.frameHeight, mFont.CENTER);
    		int width = mFont.tahoma_7b_red.getWidth(nCT_TeamA + string.Empty);
    		mFont.tahoma_7b_blue.drawString(g, nCT_TeamA + string.Empty, x - frameWidth / 2 - width, num7 + fra_PVE_Bar_1.frameHeight, 0);
    		SmallImage.drawSmallImage(g, 2325, x - frameWidth / 2 - width - 15, num7 + fra_PVE_Bar_1.frameHeight, 2, mGraphics.TOP | mGraphics.LEFT);
    		width = mFont.tahoma_7b_red.getWidth(nCT_TeamB + string.Empty);
    		mFont.tahoma_7b_red.drawString(g, nCT_TeamB + string.Empty, x + frameWidth / 2, num7 + fra_PVE_Bar_1.frameHeight, 0);
    		SmallImage.drawSmallImage(g, 2323, x + frameWidth / 2 + width + 3, num7 + fra_PVE_Bar_1.frameHeight, 0, mGraphics.TOP | mGraphics.LEFT);
    		paint_board_CT(g, GameCanvas.w - mFont.tahoma_7b_dark.getWidth("#01 AAAAAAAAAA"), 40);
    		GameCanvas.resetTrans(g);
    	}
    
    	private void paint_board_CT(mGraphics g, int x, int y)
    	{
    		if (!is_Paint_boardCT_Expand)
    		{
    			string s = "#01 nnnnnnnnnnnn";
    			int width = mFont.tahoma_7.getWidth(s);
    			int num = GameCanvas.w - width - 20;
    			for (int i = 0; i < nTop; i++)
    			{
    				mFont mFont2 = mFont.tahoma_7_white;
    				switch (i)
    				{
    				case 0:
    					mFont2 = mFont.tahoma_7_red;
    					break;
    				case 1:
    					mFont2 = mFont.tahoma_7_yellow;
    					break;
    				case 2:
    					mFont2 = mFont.tahoma_7_blue;
    					break;
    				}
    				if (i == nTop - 1)
    				{
    					mFont2 = mFont.tahoma_7_green;
    				}
    				string[] array = Res.split((string)res_CT.elementAt(i), "|", 0);
    				int[] array2 = new int[2] { 0, 18 };
    				for (int j = 0; j < 2; j++)
    				{
    					mFont2.drawString(g, array[j], num + array2[j], y + i * mFont.tahoma_7.getHeight(), 0, mFont.tahoma_7);
    				}
    			}
    			GameCanvas.resetTrans(g);
    			xRect = num;
    			yRect = y;
    			wRect = width + 10;
    			hRect = mFont.tahoma_7b_dark.getHeight() * 6;
    		}
    		else
    		{
    			string s2 = "#01 namec1000000 0001   00000";
    			int[] array3 = new int[4] { 0, 18, 80, 101 };
    			int width2 = mFont.tahoma_7.getWidth(s2);
    			int num2 = GameCanvas.w - width2 - 20;
    			int num3 = y;
    			for (int k = 0; k < nTop; k++)
    			{
    				string[] array4 = Res.split((string)res_CT.elementAt(k), "|", 0);
    				mFont mFont3 = mFont.tahoma_7_white;
    				switch (k)
    				{
    				case 0:
    					mFont3 = mFont.tahoma_7_red;
    					break;
    				case 1:
    					mFont3 = mFont.tahoma_7_yellow;
    					break;
    				case 2:
    					mFont3 = mFont.tahoma_7_blue;
    					break;
    				}
    				if (k == nTop - 1)
    				{
    					mFont3 = mFont.tahoma_7_green;
    				}
    				num3 = k * mFont.tahoma_7_white.getHeight() + y;
    				for (int l = 0; l < array3.Length; l++)
    				{
    					mFont3.drawString(g, array4[l], num2 + array3[l], num3, 0, mFont.tahoma_7);
    				}
    			}
    			xRect = num2;
    			yRect = y;
    			wRect = width2 + 10;
    			hRect = mFont.tahoma_7b_dark.getHeight() * 6;
    		}
    		GameCanvas.resetTrans(g);
    	}
    
    	private void paintHPCT(mGraphics g, int x, int y, Char c)
    	{
    		g.drawImage(imgKhung, x, y, 0);
    		int x2 = x + 3;
    		int num = y + 19;
    		int num2 = 0;
    		int num3 = 0;
    		int width = imgHP_NEW.getWidth();
    		int num4 = imgHP_NEW.getHeight() / 2;
    		num2 = (c.cHPFull > 0) ? (int)(c.cHP * width / c.cHPFull) : 0;
    		if (num2 <= 0)
    		{
    			num2 = 1;
    		}
    		else if (num2 > width)
    		{
    			num2 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, num4, 80, num4, 0, x2, num, 0);
    		num3 = (c.cMPFull > 0) ? (int)(c.cMP * width / c.cMPFull) : 0;
    		if (num3 <= 0)
    		{
    			num3 = 1;
    		}
    		else if (num3 > width)
    		{
    			num3 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, 0, 80, num4, 0, x2, num + 6, 0);
    	}
    }
}

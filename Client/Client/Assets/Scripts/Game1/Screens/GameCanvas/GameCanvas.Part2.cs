namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameCanvas : IActionListener
    {

    	private static void applyMapWeatherEffect(int typeBG)
    	{
    		try
    		{
    			// 1. Dọn dẹp các hiệu ứng thời tiết cũ của map trước
    			BackgroudEffect.clearWeatherEffects();

    			// 2. Không tạo hiệu ứng nếu đang bật chế độ giảm đồ họa hoặc ẩn map
    			if (GameCanvas.lowGraphic || ListChars.getInstance().HideMap)
    			{
    				return;
    			}

    			int mapId = TileMap.mapID;

    			// 3. Map Trong Nhà / Phi thuyền / Nhà Bang Hội (21, 22, 23, 39, 40, 41, 102...)
    			if (TileMap.isOfflineMap() || mapId == 21 || mapId == 22 || mapId == 23 || mapId == 39 || mapId == 40 || mapId == 41 || mapId == 102)
    			{
    				// Hoàn toàn không có mưa, gió, tuyết trong nhà
    				return;
    			}

    			// 4. Map Thần Điện Kami & Thần Giới (45-50, Kami, Beerus, Kaio, Time Room...)
    			if ((mapId >= 45 && mapId <= 50) || TileMap.bgType == TileMap.KAMISAMA || TileMap.bgType == TileMap.TIME_ROOM || TileMap.bgType == TileMap.BEERUS)
    			{
    				// Mây trôi bồng bềnh thanh khiết
    				BackgroudEffect.addEffect(13);
    				return;
    			}

    			// 5. Map Võ Đài thi đấu (51, 103, 112, 113, 129, 130...)
    			if (TileMap.isVoDaiMap())
    			{
    				// Đấu trường sạch sẽ, không mưa che khuất tầm nhìn đấu thủ
    				BackgroudEffect.addEffect(13);
    				return;
    			}

    			// 6. Map Băng Tuyết (105-110...)
    			if (TileMap.isMapCold())
    			{
    				BackgroudEffect.addEffect(11); // Tuyết rơi
    				return;
    			}

    			// 7. Đảo Kame (Map 5 - Bờ biển nắng vàng)
    			if (mapId == 5 || TileMap.bgType == TileMap.TRAIDAT_DAORUA)
    			{
    				BackgroudEffect.addEffect(13); // Mây trôi biển xanh
    				return;
    			}

    			// 8. Địa Ngục (HELL)
    			if (TileMap.bgType == TileMap.HELL || TileMap.bgType == TileMap.THE_HELL)
    			{
    				return;
    			}

    			// 9. Hành tinh Namếc (planetID == 1 hoặc bgType thuộc Namếc)
    			if (TileMap.planetID == 1 || TileMap.bgType == TileMap.NAMEK_DOINUI || TileMap.bgType == TileMap.NAMEK_THUNGLUNG || TileMap.bgType == TileMap.NAMEK_RUNG || TileMap.bgType == TileMap.NAMEK_DAO)
    			{
    				// Lá cây Namếc rơi xanh ngọc
    				int leafType = (Res.random(0, 2) == 0) ? 5 : 7;
    				BackgroudEffect.addEffect(leafType);
    				if (Res.random(0, 3) == 0)
    				{
    					BackgroudEffect.addEffect(13);
    				}
    			}
    			// 10. Hành tinh Xayda (planetID == 2 hoặc bgType thuộc Sayai)
    			else if (TileMap.planetID == 2 || TileMap.bgType == TileMap.SAYAI_DOINUI || TileMap.bgType == TileMap.SAYAI_RUNG || TileMap.bgType == TileMap.SAYAI_CITY || TileMap.bgType == TileMap.SAYAI_NIGHT)
    			{
    				if (TileMap.bgType == TileMap.SAYAI_NIGHT)
    				{
    					BackgroudEffect.addEffect(16); // Đom đóm đêm Xayda
    					BackgroudEffect.addEffect(4);  // Sao trời
    				}
    				else
    				{
    					BackgroudEffect.addEffect(6);  // Tàn tro / lá đỏ Xayda
    				}
    			}
    			else
    			{
    				// 11. Hành tinh Trái Đất (planetID == 0 hoặc map tự nhiên ngoài trời)
    				int leafEarth = (Res.random(0, 2) == 0) ? 1 : 2;
    				BackgroudEffect.addEffect(leafEarth);
    			}

    			// Mưa Động: Đã giảm 10 lần tần suất xuất hiện, cách cơn mưa trước 450 giây (7.5 phút), xác suất 6%
    			long now = mSystem.currentTimeMillis();
    			if (BackgroudEffect.isEnableRain && isOutdoorRainMap() && (now - lastRainTime > 450000) && Res.random(0, 100) < 6)
    			{
    				lastRainTime = now;
    				BackgroudEffect.addEffect(0);
    				if (GameCanvas.isPlaySound)
    				{
    					SoundMn.gI().rain();
    				}
    			}
    		}
    		catch (Exception catchErr_1)
    		{
    		    Cout.LogWarning("[GameCanvas.Part2.cs] " + catchErr_1.Message);
    		}
    	}

    	private static void randomRaintEff(int typeBG)
    	{
    		applyMapWeatherEffect(typeBG);
    	}
    
    	public void keyPressedz(int keyCode)
    	{
    		lastTimePress = mSystem.currentTimeMillis();
    		if ((keyCode >= 48 && keyCode <= 57) || (keyCode >= 65 && keyCode <= 122) || keyCode == 10 || keyCode == 8 || keyCode == 13 || keyCode == 32 || keyCode == 31)
    		{
    			keyAsciiPress = keyCode;
    		}
    		mapKeyPress(keyCode);
    	}
    
    	public void mapKeyPress(int keyCode)
    	{
    		if (currentDialog != null)
    		{
    			currentDialog.keyPress(keyCode);
    			keyAsciiPress = 0;
    			return;
    		}
    		currentScreen.keyPress(keyCode);
    		switch (keyCode)
    		{
    		case -38:
    		case -1:
    			if ((currentScreen is GameScr || currentScreen is CrackBallScr) && Char.myCharz().isAttack)
    			{
    				clearKeyHold();
    				clearKeyPressed();
    			}
    			else
    			{
    				keyHold[21] = true;
    				keyPressed[21] = true;
    			}
    			break;
    		case -39:
    		case -2:
    			if ((currentScreen is GameScr || currentScreen is CrackBallScr) && Char.myCharz().isAttack)
    			{
    				clearKeyHold();
    				clearKeyPressed();
    			}
    			else
    			{
    				keyHold[22] = true;
    				keyPressed[22] = true;
    			}
    			break;
    		case -3:
    			if ((currentScreen is GameScr || currentScreen is CrackBallScr) && Char.myCharz().isAttack)
    			{
    				clearKeyHold();
    				clearKeyPressed();
    			}
    			else
    			{
    				keyHold[23] = true;
    				keyPressed[23] = true;
    			}
    			break;
    		case -4:
    			if ((currentScreen is GameScr || currentScreen is CrackBallScr) && Char.myCharz().isAttack)
    			{
    				clearKeyHold();
    				clearKeyPressed();
    			}
    			else
    			{
    				keyHold[24] = true;
    				keyPressed[24] = true;
    			}
    			break;
    		case -5:
    		case 10:
    			if ((currentScreen is GameScr || currentScreen is CrackBallScr) && Char.myCharz().isAttack)
    			{
    				clearKeyHold();
    				clearKeyPressed();
    				break;
    			}
    			keyHold[25] = true;
    			keyPressed[25] = true;
    			keyHold[15] = true;
    			keyPressed[15] = true;
    			break;
    		case 48:
    			keyHold[0] = true;
    			keyPressed[0] = true;
    			break;
    		case 49:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && !ChatTextField.gI().isShow))
    			{
    				keyHold[1] = true;
    				keyPressed[1] = true;
    			}
    			break;
    		case 51:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && !ChatTextField.gI().isShow))
    			{
    				keyHold[3] = true;
    				keyPressed[3] = true;
    			}
    			break;
    		case 55:
    			keyHold[7] = true;
    			keyPressed[7] = true;
    			break;
    		case 57:
    			keyHold[9] = true;
    			keyPressed[9] = true;
    			break;
    		case 42:
    			keyHold[10] = true;
    			keyPressed[10] = true;
    			break;
    		case 35:
    			keyHold[11] = true;
    			keyPressed[11] = true;
    			break;
    		case -21:
    		case -6:
    			keyHold[12] = true;
    			keyPressed[12] = true;
    			break;
    		case -22:
    		case -7:
    			keyHold[13] = true;
    			keyPressed[13] = true;
    			break;
    		case 50:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && !ChatTextField.gI().isShow))
    			{
    				keyHold[2] = true;
    				keyPressed[2] = true;
    			}
    			break;
    		case 52:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && !ChatTextField.gI().isShow))
    			{
    				keyHold[4] = true;
    				keyPressed[4] = true;
    			}
    			break;
    		case 54:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && !ChatTextField.gI().isShow))
    			{
    				keyHold[6] = true;
    				keyPressed[6] = true;
    			}
    			break;
    		case 56:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && !ChatTextField.gI().isShow))
    			{
    				keyHold[8] = true;
    				keyPressed[8] = true;
    			}
    			break;
    		case 53:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && !ChatTextField.gI().isShow))
    			{
    				keyHold[5] = true;
    				keyPressed[5] = true;
    			}
    			break;
    		case -8:
    			keyHold[14] = true;
    			keyPressed[14] = true;
    			break;
    		case -26:
    			keyHold[16] = true;
    			keyPressed[16] = true;
    			break;
    		case 113:
    			keyHold[17] = true;
    			keyPressed[17] = true;
    			break;
    		}
    	}
    
    	public void keyReleasedz(int keyCode)
    	{
    		keyAsciiPress = 0;
    		mapKeyRelease(keyCode);
    	}
    
    	public void mapKeyRelease(int keyCode)
    	{
    		switch (keyCode)
    		{
    		case -38:
    		case -1:
    			keyHold[21] = false;
    			break;
    		case -39:
    		case -2:
    			keyHold[22] = false;
    			break;
    		case -3:
    			keyHold[23] = false;
    			break;
    		case -4:
    			keyHold[24] = false;
    			break;
    		case -5:
    		case 10:
    			keyHold[25] = false;
    			keyReleased[25] = true;
    			keyHold[15] = true;
    			keyPressed[15] = true;
    			break;
    		case 48:
    			keyHold[0] = false;
    			keyReleased[0] = true;
    			break;
    		case 49:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && isMoveNumberPad && !ChatTextField.gI().isShow))
    			{
    				keyHold[1] = false;
    				keyReleased[1] = true;
    			}
    			break;
    		case 51:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && isMoveNumberPad && !ChatTextField.gI().isShow))
    			{
    				keyHold[3] = false;
    				keyReleased[3] = true;
    			}
    			break;
    		case 55:
    			keyHold[7] = false;
    			keyReleased[7] = true;
    			break;
    		case 57:
    			keyHold[9] = false;
    			keyReleased[9] = true;
    			break;
    		case 42:
    			keyHold[10] = false;
    			keyReleased[10] = true;
    			break;
    		case 35:
    			keyHold[11] = false;
    			keyReleased[11] = true;
    			break;
    		case -21:
    		case -6:
    			keyHold[12] = false;
    			keyReleased[12] = true;
    			break;
    		case -22:
    		case -7:
    			keyHold[13] = false;
    			keyReleased[13] = true;
    			break;
    		case 50:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && isMoveNumberPad && !ChatTextField.gI().isShow))
    			{
    				keyHold[2] = false;
    				keyReleased[2] = true;
    			}
    			break;
    		case 52:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && isMoveNumberPad && !ChatTextField.gI().isShow))
    			{
    				keyHold[4] = false;
    				keyReleased[4] = true;
    			}
    			break;
    		case 54:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && isMoveNumberPad && !ChatTextField.gI().isShow))
    			{
    				keyHold[6] = false;
    				keyReleased[6] = true;
    			}
    			break;
    		case 56:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && isMoveNumberPad && !ChatTextField.gI().isShow))
    			{
    				keyHold[8] = false;
    				keyReleased[8] = true;
    			}
    			break;
    		case 53:
    			if (currentScreen == CrackBallScr.instance || (currentScreen == GameScr.instance && isMoveNumberPad && !ChatTextField.gI().isShow))
    			{
    				keyHold[5] = false;
    				keyReleased[5] = true;
    			}
    			break;
    		case -8:
    			keyHold[14] = false;
    			break;
    		case -26:
    			keyHold[16] = false;
    			break;
    		case 113:
    			keyHold[17] = false;
    			keyReleased[17] = true;
    			break;
    		}
    	}
    
    	public void pointerMouse(int x, int y)
    	{
    		pxMouse = x;
    		pyMouse = y;
    	}
    
    	public void scrollMouse(int a)
    	{
    		pXYScrollMouse = a;
    		if (panel != null && panel.isShow)
    		{
    			panel.updateScroolMouse(a);
    		}
    	}
    
    	public void pointerDragged(int x, int y)
    	{
    		isPointerSelect = false;
    		if (Res.abs(x - pxLast) >= 10 || Res.abs(y - pyLast) >= 10)
    		{
    			isPointerClick = false;
    			isPointerDown = true;
    			isPointerMove = true;
    		}
    		px = x;
    		py = y;
    		curPos++;
    		if (curPos > 3)
    		{
    			curPos = 0;
    		}
    		arrPos[curPos] = new Position(x, y);
    	}
    
    	public static bool isHoldPress()
    	{
    		if (mSystem.currentTimeMillis() - lastTimePress >= 800)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void pointerPressed(int x, int y)
    	{
    		isPointerSelect = false;
    		isPointerJustRelease = false;
    		isPointerJustDown = true;
    		isPointerDown = true;
    		isPointerClick = false;
    		isPointerMove = false;
    		lastTimePress = mSystem.currentTimeMillis();
    		pxFirst = x;
    		pyFirst = y;
    		pxLast = x;
    		pyLast = y;
    		px = x;
    		py = y;
    	}
    
    	public void pointerReleased(int x, int y)
    	{
    		if (!isPointerMove)
    		{
    			isPointerSelect = true;
    		}
    		isPointerDown = false;
    		isPointerJustRelease = true;
    		isPointerMove = false;
    		isPointerClick = true;
    		mScreen.keyTouch = -1;
    		px = x;
    		py = y;
    	}
    
    	public static bool isPointerHoldIn(int x, int y, int w, int h)
    	{
    		if (!isPointerDown && !isPointerJustRelease)
    		{
    			return false;
    		}
    		if (px >= x && px <= x + w && py >= y && py <= y + h)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public static bool isPointSelect(int x, int y, int w, int h)
    	{
    		if (!isPointerSelect)
    		{
    			return false;
    		}
    		if (px >= x && px <= x + w && py >= y && py <= y + h)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public static bool isMouseFocus(int x, int y, int w, int h)
    	{
    		if (pxMouse >= x && pxMouse <= x + w && pyMouse >= y && pyMouse <= y + h)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public static void clearKeyPressed()
    	{
    		for (int i = 0; i < keyPressed.Length; i++)
    		{
    			keyPressed[i] = false;
    		}
    		isPointerJustRelease = false;
    	}
    
    	public static void clearKeyHold()
    	{
    		for (int i = 0; i < keyHold.Length; i++)
    		{
    			keyHold[i] = false;
    		}
    	}
    
    	public static void checkBackButton()
    	{
    		if (ChatPopup.serverChatPopUp == null && ChatPopup.currChatPopup == null)
    		{
    			startYesNoDlg(mResources.DOYOUWANTEXIT, new Command(mResources.YES, instance, 8885, null), new Command(mResources.NO, instance, 8882, null));
    		}
    	}
    
    	public void paintChangeMap(mGraphics g)
    	{
    		string empty = string.Empty;
    		resetTrans(g);
    		g.setColor(0);
    		g.fillRect(0, 0, w, h);
    		g.drawImage(LoginScr.imgTitle, w / 2, h / 2 - 24, StaticObj.BOTTOM_HCENTER);
    		paintShukiren(hw, h / 2 + 24, g);
    		mFont.tahoma_7b_white.drawString(g, mResources.PLEASEWAIT + ((LoginScr.timeLogin <= 0) ? empty : (" " + LoginScr.timeLogin + "s")), w / 2, h / 2, 2);
    	}
    
    	public void paint(mGraphics gx)
    	{
    		try
    		{
    			GUI.color = Color.white;
    			GUI.backgroundColor = Color.white;
    			g.reset();
    			if (currentScreen != null)
    			{
    				currentScreen.paint(g);
    			}
    			debug("PB", 1);
    			g.translate(-g.getTranslateX(), -g.getTranslateY());
    			g.setClip(0, 0, w, h);
    			if (panel.isShow)
    			{
    				panel.paint(g);
    				if (panel2 != null && panel2.isShow)
    				{
    					panel2.paint(g);
    				}
    				if (panel.chatTField != null && panel.chatTField.isShow)
    				{
    					panel.chatTField.paint(g);
    				}
    				if (panel2 != null && panel2.chatTField != null && panel2.chatTField.isShow)
    				{
    					panel2.chatTField.paint(g);
    				}
    			}
    			Res.paintOnScreenDebug(g);
    			InfoDlg.paint(g);
    			if (currentDialog != null)
    			{
    				debug("PC", 1);
    				currentDialog.paint(g);
    			}
    			else if (menu.showMenu)
    			{
    				debug("PD", 1);
    				resetTrans(g);
    				menu.paintMenu(g);
    			}
    			GameScr.info1.paint(g);
    			GameScr.info2.paint(g);
    			if (currentDialog == null && !panel.isShow && !menu.showMenu)
    			{
    				if (GameScr.gI().popUpYesNo != null)
    				{
    					GameScr.gI().popUpYesNo.paint(g);
    				}
    				if (ChatPopup.currChatPopup != null)
    				{
    					ChatPopup.currChatPopup.paint(g);
    				}
    				Hint.paint(g);
    				if (ChatPopup.serverChatPopUp != null)
    				{
    					ChatPopup.serverChatPopUp.paint(g);
    				}
    			}
    			for (int i = 0; i < Effect2.vEffect2.size(); i++)
    			{
    				Effect2 effect = (Effect2)Effect2.vEffect2.elementAt(i);
    				if (effect is ChatPopup && !effect.Equals(ChatPopup.currChatPopup) && !effect.Equals(ChatPopup.serverChatPopUp))
    				{
    					effect.paint(g);
    				}
    			}
    			if (currentDialog != null)
    			{
    				currentDialog.paint(g);
    			}
    			if (Char.isLoadingMap || (currentScreen != GameScr.instance && (LoginScr.isContinueToLogin || ServerListScreen.waitToLogin || ServerListScreen.isWait)))
    			{
    				paintChangeMap(g);
    				if (timeLoading > 0 && LoginScr.timeLogin <= 0)
    				{
    					if (mSystem.currentTimeMillis() - TIMEOUT >= 1000)
    					{
    						timeLoading--;
    						Res.outz("[COUNT] == " + timeLoading);
    						if (timeLoading == 0)
    						{
    							timeLoading = 15;
    						}
    						TIMEOUT = mSystem.currentTimeMillis();
    					}
    				}
    				if (mSystem.currentTimeMillis() > timeBreakLoading)
    				{
    					timeBreakLoading = mSystem.currentTimeMillis() + 30000;
    					if (currentScreen != null && !(currentScreen is GameScr) && !(currentScreen is SplashScr))
    					{
    						if (!(currentScreen is LoginScr) || currentDialog != null || isLoading)
    						{
    							Char.isLoadingMap = false;
    							LoginScr.isContinueToLogin = false;
    							ServerListScreen.waitToLogin = false;
    							ServerListScreen.isWait = false;
    							isLoading = false;
    							endDlg();
    							try
    							{
    								Session_ME.gI().close();
    							}
    							catch (Exception catchErr_2)
    							{
    							    Cout.LogWarning("[GameCanvas.Part2.cs] " + catchErr_2.Message);
    							}
    							instance.resetToLoginScrz();
    							startOKDlg("Đăng nhập quá lâu hoặc dữ liệu bị lỗi, vui lòng thử lại!");
    						}
    					}
    				}
    			}
    			debug("PE", 1);
    			resetTrans(g);
    			EffecMn.paintLayer4(g);
    			if (open3Hour && !isLoading)
    			{
    				if (currentScreen == loginScr || currentScreen == serverScreen || currentScreen == serverScr)
    				{
    					g.drawImage(img12, 5, 5, 0);
    				}
    				if (currentScreen == CreateCharScr.instance)
    				{
    					g.drawImage(img12, 5, 20, 0);
    				}
    			}
    			resetTrans(g);
    			int num = h / 4;
    			if (currentScreen != null && currentScreen is GameScr && thongBaoTest != null)
    			{
    				g.setClip(60, num, w - 120, mFont.tahoma_7_white.getHeight() + 2);
    				mFont.tahoma_7_grey.drawString(g, thongBaoTest, xThongBaoTranslate, num + 1, 0);
    				mFont.tahoma_7_yellow.drawString(g, thongBaoTest, xThongBaoTranslate, num, 0);
    				g.setClip(0, 0, w, h);
    			}
    		}
    		catch (Exception ex)
    		{
    			Debug.LogException(ex);
    		}
    	}
    
    	public static void endDlg()
    	{
    		if (inputDlg != null)
    		{
    			inputDlg.tfInput.setMaxTextLenght(500);
    		}
    		currentDialog = null;
    		InfoDlg.hide();
    	}
    
    	public static void startOKDlg(string info)
    	{
    		closeKeyBoard();
    		msgdlg.setInfo(info, null, new Command(mResources.OK, instance, 8882, null), null);
    		currentDialog = msgdlg;
    	}
    
    	public static void startWaitDlg(string info)
    	{
    		closeKeyBoard();
    		msgdlg.setInfo(info, null, new Command(mResources.CANCEL, instance, 8882, null), null);
    		currentDialog = msgdlg;
    		msgdlg.isWait = true;
    	}
    
    	public static void startOKDlg(string info, bool isError)
    	{
    		closeKeyBoard();
    		msgdlg.setInfo(info, null, new Command(mResources.CANCEL, instance, 8882, null), null);
    		currentDialog = msgdlg;
    		msgdlg.isWait = true;
    	}
    
    	public static void startWaitDlg()
    	{
    		closeKeyBoard();
    		Char.isLoadingMap = true;
    	}
    
    	public void openWeb(string strLeft, string strRight, string url, string str)
    	{
    		msgdlg.setInfo(str, new Command(strLeft, this, 8881, url), null, new Command(strRight, this, 8882, null));
    		currentDialog = msgdlg;
    	}
    
    	public static void startOK(string info, int actionID, object p)
    	{
    		closeKeyBoard();
    		msgdlg.setInfo(info, null, new Command(mResources.OK, instance, actionID, p), null);
    		msgdlg.show();
    	}
    
    	public static void startYesNoDlg(string info, int iYes, object pYes, int iNo, object pNo)
    	{
    		closeKeyBoard();
    		msgdlg.setInfo(info, new Command(mResources.YES, instance, iYes, pYes), new Command(string.Empty, instance, iYes, pYes), new Command(mResources.NO, instance, iNo, pNo));
    		msgdlg.show();
    	}
    
    	public static void startYesNoDlg(string info, Command cmdYes, Command cmdNo)
    	{
    		closeKeyBoard();
    		msgdlg.setInfo(info, cmdYes, null, cmdNo);
    		msgdlg.show();
    	}
    
    	public static void startserverThongBao(string msgSv)
    	{
    		thongBaoTest = msgSv;
    		xThongBaoTranslate = w - 60;
    		dir_ = -1;
    	}
    
    	public static string getMoneys(long m)
    	{
    		string text = string.Empty;
    		long num = m / 1000 + 1;
    		for (int i = 0; i < num; i++)
    		{
    			if (m >= 1000)
    			{
    				long num2 = m % 1000;
    				text = ((num2 != 0) ? ((num2 >= 10) ? ((num2 >= 100) ? ("." + num2 + text) : (".0" + num2 + text)) : (".00" + num2 + text)) : (".000" + text));
    				m /= 1000;
    				continue;
    			}
    			text = m + text;
    			break;
    		}
    		return text;
    	}
    
    	public static int getX(int start, int w)
    	{
    		return (px - start) / w;
    	}
    
    	public static int getY(int start, int w)
    	{
    		return (py - start) / w;
    	}
    
    	public void sizeChanged(int newW, int newH)
    	{
    		w = newW;
    		h = newH;
    		hw = w / 2;
    		hh = h / 2;
    		wd3 = w / 3;
    		hd3 = h / 3;
    		w2d3 = 2 * w / 3;
    		h2d3 = 2 * h / 3;
    		w3d4 = 3 * w / 4;
    		h3d4 = 3 * h / 4;
    		wd6 = w / 6;
    		hd6 = h / 6;
    		if (w < 320)
    		{
    			isTouchControlSmallScreen = true;
    			isTouchControlLargeScreen = false;
    		}
    		else
    		{
    			isTouchControlSmallScreen = false;
    			isTouchControlLargeScreen = true;
    		}
    		if (h <= 160)
    		{
    			Paint.hTab = 15;
    		}
    		else
    		{
    			Paint.hTab = 24;
    		}
    		mScreen.initPos();
    		if (currentScreen == GameScr.instance && GameScr.instance != null)
    		{
    			GameScr.loadCamera(false, -1, -1);
    			GameScr.updateUIPositions();
    			GameScr.gI().initSelectChar();
    		}
    		else if (currentScreen != null)
    		{
    			currentScreen.switchToMe();
    		}
    		if (panel != null && panel.isShow)
    		{
    			panel.init();
    			panel.H = h;
    			panel.hScroll = panel.H - 96;
    		}
    		if (panel2 != null && panel2.isShow)
    		{
    			panel2.init();
    			panel2.H = h;
    			panel2.hScroll = panel2.H - 96;
    		}
    		if (ChatTextField.gI() != null && ChatTextField.gI().isShow)
    		{
    			ChatTextField.gI().initChatTextField();
    		}
    	}
    
    	public static bool isGetResourceFromServer()
    	{
    		return true;
    	}
    
    	public static Image loadImageRMS(string path)
	{
		string rawPath = path;
		path = Main.res + "/x" + mGraphics.zoomLevel + path;
		path = cutPng(path);
		Image result = null;
		try
		{
			result = Image.createImage(path);
		}
		catch (Exception ex)
		{
			try
			{
				string[] array = Res.split(path, "/", 0);
				string filename = "x" + mGraphics.zoomLevel + array[array.Length - 1];
				sbyte[] array2 = Rms.loadRMS(filename);
				if (array2 != null)
				{
					result = Image.createImage(array2, 0, array2.Length);
					array2 = null;
				}
			}
			catch (Exception)
			{
				Cout.LogError("Loi ham khong tim thay a: " + ex.ToString());
			}
		}
		if (result == null && mGraphics.zoomLevel != 1)
		{
			try
			{
				string pathX1 = Main.res + "/x1" + rawPath;
				pathX1 = cutPng(pathX1);
				result = Image.createImage(pathX1);
			}
			catch (Exception catchErr_3)
			{
			    Cout.LogWarning("[GameCanvas.Part2.cs] " + catchErr_3.Message);
			}
		}
		return result;
	}
    
    	public static Image loadImage(string path)
    	{
    		path = Main.res + "/x" + mGraphics.zoomLevel + path;
    		path = cutPng(path);
    		Image result = null;
    		try
    		{
    			result = Image.createImage(path);
    		}
    		catch (Exception catchErr_4)
    		{
    		    Cout.LogWarning("[GameCanvas.Part2.cs] " + catchErr_4.Message);
    		}
    		return result;
    	}
    
    	public static string cutPng(string str)
    	{
    		string result = str;
    		if (str.Contains(".png"))
    		{
    			result = str.Replace(".png", string.Empty);
    		}
    		return result;
    	}
    
    	public static int random(int a, int b)
    	{
    		return a + r.nextInt(b - a);
    	}
    
    	public bool startDust(int dir, int x, int y)
    	{
    		return false;
    	}
    
    	public void loadWaterSplash()
    	{
    		if (!lowGraphic)
    		{
    			imgWS = new Image[3];
    			for (int i = 0; i < 3; i++)
    			{
    				imgWS[i] = loadImage("/e/w" + i + ".png");
    			}
    			wsX = new int[2];
    			wsY = new int[2];
    			wsState = new int[2];
    			wsF = new int[2];
    			wsState[0] = (wsState[1] = -1);
    		}
    	}
    
    	public bool startWaterSplash(int x, int y)
    	{
    		if (lowGraphic)
    		{
    			return false;
    		}
    		int num = ((wsState[0] != -1) ? 1 : 0);
    		if (wsState[num] != -1)
    		{
    			return false;
    		}
    		wsState[num] = 0;
    		wsX[num] = x;
    		wsY[num] = y;
    		return true;
    	}
    
    	public void updateWaterSplash()
    	{
    		if (lowGraphic)
    		{
    			return;
    		}
    		for (int i = 0; i < 2; i++)
    		{
    			if (wsState[i] == -1)
    			{
    				continue;
    			}
    			wsY[i]--;
    			if (gameTick % 2 == 0)
    			{
    				wsState[i]++;
    				if (wsState[i] > 2)
    				{
    					wsState[i] = -1;
    				}
    				else
    				{
    					wsF[i] = wsState[i];
    				}
    			}
    		}
    	}
    
    	public void updateDust()
    	{
    	}

    	public static bool isPaint(int x, int y)
    	{
    		if (x < GameScr.cmx)
    		{
    			return false;
    		}
    		if (x > GameScr.cmx + GameScr.gW)
    		{
    			return false;
    		}
    		if (y < GameScr.cmy)
    		{
    			return false;
    		}
    		if (y > GameScr.cmy + GameScr.gH + 30)
    		{
    			return false;
    		}
    		return true;
    	}

    	public void paintDust(mGraphics g)
    	{
    	}

    	public void loadDust()
    	{
    		if (imgDust == null)
    		{
    			imgDust = new Image[2][];
    			for (int i = 0; i < imgDust.Length; i++)
    			{
    				imgDust[i] = new Image[5];
    			}
    			for (int j = 0; j < 2; j++)
    			{
    				for (int k = 0; k < 5; k++)
    				{
    					imgDust[j][k] = loadImage("/e/d" + j + k + ".png");
    				}
    			}
    		}
    		if (dustState == null || dustState.Length != MAX_DUST)
    		{
    			dustX = new int[MAX_DUST];
    			dustY = new int[MAX_DUST];
    			dustState = new int[MAX_DUST];
    			dustDir = new int[MAX_DUST];
    		}
    		for (int i = 0; i < MAX_DUST; i++)
    		{
    			dustState[i] = -1;
    		}
    	}
    
    	public static void paintShukiren(int x, int y, mGraphics g)
    	{
    		Main.f = (gameTick / 2) % 8;
    		g.drawRegion(imgShuriken, 0, Main.f * 16, 16, 16, 0, x, y, mGraphics.HCENTER | mGraphics.VCENTER);
    	}
    
    	public void resetToLoginScrz()
    	{
    		resetToLoginScr = true;
    	}
    
    	public static bool isPointer(int x, int y, int w, int h)
    	{
    		if (!isPointerDown && !isPointerJustRelease)
    		{
    			return false;
    		}
    		if (px >= x && px <= x + w && py >= y && py <= y + h)
    		{
    			return true;
    		}
    		return false;
    	}
    }
}

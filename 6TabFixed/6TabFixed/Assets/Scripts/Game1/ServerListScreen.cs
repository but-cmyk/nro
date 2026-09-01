namespace Game1
{
    using System;
    
    public class ServerListScreen : mScreen, IActionListener
    {
    	public static string[] nameServer;
    
    	public static string[] address;
    
    	public static sbyte serverPriority;
    
    	public static bool[] hasConnected;
    
    	public static short[] port;
    
    	public static int selected;
    
    	public static bool isWait;
    
    	public static Command cmdUpdateServer;
    
    	public static sbyte[] language;
    
    	public static sbyte[] typeSv;
    
    	public static sbyte[] isNew;
    
    	private Command[] cmd;
    
    	private Command cmdCallHotline;
    
    	private int nCmdPlay;
    
    	public static Command cmdDeleteRMS;
    
    	private int lY;
    
    	public static string smartPhoneVN;
    
    	public static string javaVN;
    
    	public static string smartPhoneIn;
    
    	public static string javaIn;
    
    	public static string smartPhoneE;
    
    	public static string javaE;
    
    	public static string linkGetHost;
    
    	public static string linkDefault;
    
    	public const sbyte languageVersion = 2;
    
    	public new int keyTouch = -1;
    
    	private int tam;
    
    	public static bool stopDownload;
    
    	public static string linkweb;
    
    	public static int countDieConnect;
    
    	public static bool waitToLogin;
    
    	public static int tWaitToLogin;
    
    	public static string RMS_NRlink;
    
    	public static int[] lengthServer;
    
    	public static int ipSelect;
    
    	public static int flagServer;
    
    	public static bool bigOk;
    
    	public static int percent;
    
    	public static string strWait;
    
    	public static int nBig;
    
    	public static int nBg;
    
    	public static int demPercent;
    
    	public static int maxBg;
    
    	public static bool isGetData;
    
    	public static Command cmdDownload;
    
    	private Command cmdStart;
    
    	public string dataSize;
    
    	public static int p;
    
    	public static int testConnect;
    
    	public static bool loadScreen;
    
    	public static bool isAutoConect;
    
    	public ServerListScreen()
    	{
    		int num = 4;
    		int num2 = num * 32 + 23 + 33;
    		if (num2 >= GameCanvas.w)
    		{
    			num--;
    			num2 = num * 32 + 23 + 33;
    		}
    		initCommand();
    		if (!GameCanvas.isTouch)
    		{
    			selected = 0;
    			processInput();
    		}
    		GameScr.loadCamera(true, -1, -1);
    		GameScr.cmx = 100;
    		GameScr.cmy = 200;
    		if (cmdCallHotline == null)
    		{
    			cmdCallHotline = new Command("Gọi hotline", this, 13, null);
    			cmdCallHotline.x = GameCanvas.w - 75;
    			if (mSystem.clientType == 1 && !GameCanvas.isTouch)
    			{
    				cmdCallHotline.y = GameCanvas.h - 20;
    			}
    			else
    			{
    				int num3 = 2;
    				cmdCallHotline.y = num3 + 6;
    			}
    		}
    		cmdUpdateServer = new Command();
    		cmdUpdateServer.actionChat = delegate(string str)
    		{
    			string text = str;
    			string text2 = str;
    			if (text == null)
    			{
    				text = linkDefault;
    			}
    			else
    			{
    				if (text == null && text2 != null)
    				{
    					if (text2.Equals(string.Empty) || text2.Length < 20)
    					{
    						text2 = linkDefault;
    					}
    					getServerList(text2);
    				}
    				if (text != null && text2 == null)
    				{
    					if (text.Equals(string.Empty) || text.Length < 20)
    					{
    						text = linkDefault;
    					}
    					getServerList(text);
    				}
    				if (text != null && text2 != null)
    				{
    					if (text.Length > text2.Length)
    					{
    						getServerList(text);
    					}
    					else
    					{
    						getServerList(text2);
    					}
    				}
    			}
    		};
    		setLinkDefault(mSystem.LANGUAGE);
    	}
    
    	public static void createDeleteRMS()
    	{
    		if (cmdDeleteRMS == null)
    		{
    			if (GameCanvas.serverScreen == null)
    			{
    				GameCanvas.serverScreen = new ServerListScreen();
    			}
    			cmdDeleteRMS = new Command(string.Empty, GameCanvas.serverScreen, 14, null);
    			cmdDeleteRMS.w = 92;
    			cmdDeleteRMS.h = 22;
    			cmdDeleteRMS.x = GameCanvas.w - 98;
    			cmdDeleteRMS.y = GameCanvas.h - 28;
    		}
    	}
    
    	private void initCommand()
    	{
    		nCmdPlay = 0;
    		string text = Rms.loadRMSString("acc");
    		if (text == null)
    		{
    			if (Rms.loadRMS("userAo" + ipSelect) != null)
    			{
    				nCmdPlay = 1;
    			}
    		}
    		else if (text.Equals(string.Empty))
    		{
    			if (Rms.loadRMS("userAo" + ipSelect) != null)
    			{
    				nCmdPlay = 1;
    			}
    		}
    		else
    		{
    			nCmdPlay = 1;
    		}
    		cmd = new Command[(mGraphics.zoomLevel <= 1) ? (4 + nCmdPlay) : (3 + nCmdPlay)];
    		int num = GameCanvas.hh - 15 * cmd.Length + 28;
    		for (int i = 0; i < cmd.Length; i++)
    		{
    			switch (i)
    			{
    			case 0:
    				cmd[0] = new Command(string.Empty, this, 3, null);
    				if (text == null)
    				{
    					cmd[0].caption = mResources.playNew;
    					if (Rms.loadRMS("userAo" + ipSelect) != null)
    					{
    						cmd[0].caption = mResources.choitiep;
    					}
    					break;
    				}
    				if (text.Equals(string.Empty))
    				{
    					cmd[0].caption = mResources.playNew;
    					if (Rms.loadRMS("userAo" + ipSelect) != null)
    					{
    						cmd[0].caption = mResources.choitiep;
    					}
    					break;
    				}
    				cmd[0].caption = mResources.playAcc + ": " + text;
    				if (cmd[0].caption.Length > 23)
    				{
    					cmd[0].caption = cmd[0].caption.Substring(0, 23);
    					cmd[0].caption += "...";
    				}
    				break;
    			case 1:
    				if (nCmdPlay == 1)
    				{
    					cmd[1] = new Command(string.Empty, this, 10100, null);
    					cmd[1].caption = mResources.playNew;
    				}
    				else
    				{
    					cmd[1] = new Command(mResources.change_account, this, 7, null);
    				}
    				break;
    			case 2:
    				if (nCmdPlay == 1)
    				{
    					cmd[2] = new Command(mResources.change_account, this, 7, null);
    				}
    				else
    				{
    					cmd[2] = new Command(string.Empty, this, 17, null);
    				}
    				break;
    			case 3:
    				if (nCmdPlay == 1)
    				{
    					cmd[3] = new Command(string.Empty, this, 17, null);
    				}
    				else
    				{
    					cmd[3] = new Command(mResources.option, this, 8, null);
    				}
    				break;
    			case 4:
    				cmd[4] = new Command(mResources.option, this, 8, null);
    				break;
    			}
    			cmd[i].y = num;
    			cmd[i].setType();
    			cmd[i].x = (GameCanvas.w - cmd[i].w) / 2;
    			num += 30;
    		}
    	}
    
    	public static void doUpdateServer()
    	{
    		if (cmdUpdateServer == null && GameCanvas.serverScreen == null)
    		{
    			GameCanvas.serverScreen = new ServerListScreen();
    		}
    	}
    
    	public static void getServerList(string str)
    	{
    		lengthServer = new int[3];
    		string[] array = Res.split(str.Trim(), ",", 0);
    		Res.outz(">>> getServerList= " + str);
    		mResources.loadLanguague(sbyte.Parse(array[array.Length - 2]));
    		nameServer = new string[array.Length - 2];
    		address = new string[array.Length - 2];
    		port = new short[array.Length - 2];
    		language = new sbyte[array.Length - 2];
    		typeSv = new sbyte[array.Length - 2];
    		isNew = new sbyte[array.Length - 2];
    		hasConnected = new bool[2];
    		for (int i = 0; i < array.Length - 2; i++)
    		{
    			string[] array2 = Res.split(array[i].Trim(), ":", 0);
    			nameServer[i] = array2[0];
    			address[i] = array2[1];
    			port[i] = short.Parse(array2[2]);
    			language[i] = sbyte.Parse(array2[3].Trim());
    			try
    			{
    				typeSv[i] = sbyte.Parse(array2[4].Trim());
    			}
    			catch (Exception)
    			{
    				typeSv[i] = 0;
    			}
    			try
    			{
    				isNew[i] = sbyte.Parse(array2[5].Trim());
    			}
    			catch (Exception)
    			{
    				isNew[i] = 0;
    			}
    			lengthServer[language[i]]++;
    		}
    		serverPriority = sbyte.Parse(array[array.Length - 1]);
    		saveIP();
    	}
    
    	public override void paint(mGraphics g)
    	{
    		int num = 105;
		GameCanvas.paintBGGameScr(g);
		int num2 = 2;
		mFont.tahoma_7_white.drawString(g, "v" + GameMidlet.VERSION + "(" + mGraphics.zoomLevel + ")", GameCanvas.w - 2, num2 + 15, 1, mFont.tahoma_7_grey);
		string empty = string.Empty;
		empty = ((testConnect != 0) ? (empty + nameServer[ipSelect] + " connected") : (empty + nameServer[ipSelect] + " disconnect"));
		if (mSystem.isTest)
		{
			mFont.tahoma_7_white.drawString(g, empty, GameCanvas.w - 2, num2 + 15 + 15, 1, mFont.tahoma_7_grey);
		}
		if (!isGetData || loadScreen)
		{
			if (mSystem.clientType == 1 && !GameCanvas.isTouch)
			{
				mFont.tahoma_7_white.drawString(g, linkweb, GameCanvas.w - 2, GameCanvas.h - 15, 1, mFont.tahoma_7_grey);
			}
			else
			{
				mFont.tahoma_7_white.drawString(g, linkweb, GameCanvas.w - 2, num2, 1, mFont.tahoma_7_grey);
			}
		}
		else
		{
			mFont.tahoma_7_white.drawString(g, linkweb, GameCanvas.w - 2, num2, 1, mFont.tahoma_7_grey);
		}
		int num3 = ((GameCanvas.w < 200) ? 160 : 180);
		if (cmdDeleteRMS != null)
		{
			int btnW = 96;
			int btnH = 22;
			int btnX = GameCanvas.w - btnW - 6;
			int btnY = GameCanvas.h - btnH - 6;
			cmdDeleteRMS.x = btnX;
			cmdDeleteRMS.y = btnY;
			cmdDeleteRMS.w = btnW;
			cmdDeleteRMS.h = btnH;

			bool isHover = cmdDeleteRMS.isPointerPressInside();
			g.setColor(isHover ? 0x242a38 : 0x141822);
			g.fillRect(btnX, btnY, btnW, btnH, 4);
			g.setColor(isHover ? 0x00d2d3 : 0x3d4a60);
			g.drawRect(btnX, btnY, btnW, btnH);
			mFont.tahoma_7_white.drawString(g, "🔄 " + mResources.xoadulieu, btnX + btnW / 2, btnY + 5, 2);
		}
		if (GameCanvas.currentDialog == null)
		{
			if (!loadScreen)
			{
				if (!bigOk)
				{
					g.drawImage(LoginScr.imgTitle, GameCanvas.hw, GameCanvas.hh - 48, 3);
					
					int cardW = (GameCanvas.w < 260) ? (GameCanvas.w - 20) : 240;
					int cardH = 92;
					int cardX = GameCanvas.hw - cardW / 2;
					int cardY = GameCanvas.hh - 12;

					g.setColor(0x0e121e);
					g.fillRect(cardX, cardY, cardW, cardH, 6);
					g.setColor(0x2d3748);
					g.drawRect(cardX, cardY, cardW, cardH);

					if (!isGetData)
					{
						mFont.tahoma_7b_yellow.drawString(g, "TÀI NGUYÊN CHƯA TẢI", cardX + cardW / 2, cardY + 12, 2);
						mFont.tahoma_7_white.drawString(g, mResources.taidulieudechoi, cardX + cardW / 2, cardY + 30, 2);
						if (cmdDownload != null)
						{
							cmdDownload.x = GameCanvas.hw - cmdDownload.w / 2;
							cmdDownload.y = cardY + 52;
							cmdDownload.paint(g);
						}
					}
					else
					{
						mFont.tahoma_7b_yellow.drawString(g, "ĐANG ĐỒNG BỘ TÀI NGUYÊN...", cardX + cardW / 2, cardY + 10, 2);
						int totalFiles = (nBig > 0) ? nBig : 100;
						mFont.tahoma_7_white.drawString(g, "Đã tải: " + demPercent + "/" + totalFiles + " tệp (" + percent + "%)", cardX + cardW / 2, cardY + 28, 2);
						
						int barW = cardW - 36;
						int barH = 10;
						int barX = cardX + 18;
						int barY = cardY + 46;

						g.setColor(0x222736);
						g.fillRect(barX, barY, barW, barH, 4);

						int fillW = (int)((float)barW * (float)percent / 100f);
						if (fillW > barW) fillW = barW;
						if (fillW > 0)
						{
							g.setColor(0x00d2d3);
							g.fillRect(barX, barY, fillW, barH, 4);
							g.setColor(0xffffff);
							g.fillRect(barX, barY + 1, fillW, 2);
						}

						mFont.tahoma_7_grey.drawString(g, "Vui lòng giữ kết nối mạng ổn định", cardX + cardW / 2, cardY + 66, 2);
						
						if (cmdDownload != null)
						{
							cmdDownload.x = GameCanvas.hw - cmdDownload.w / 2;
							cmdDownload.y = cardY + cardH + 8;
							cmdDownload.paint(g);
						}
					}
				}
			}
			else
			{
				int num4 = GameCanvas.hh - 15 * cmd.Length - 15;
				if (num4 < 25)
				{
					num4 = 25;
				}
				if (LoginScr.imgTitle != null)
				{
					g.drawImage(LoginScr.imgTitle, GameCanvas.hw, num4, 3);
				}
				for (int i = 0; i < cmd.Length; i++)
				{
					cmd[i].paint(g);
				}
				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
				if (testConnect == -1)
				{
					if (GameCanvas.gameTick % 20 > 10)
					{
						g.drawRegion(GameScr.imgRoomStat, 0, 14, 7, 7, 0, (GameCanvas.w - mFont.tahoma_7b_dark.getWidth(cmd[2 + nCmdPlay].caption) >> 1) - 10, cmd[2 + nCmdPlay].y + 10, 0);
					}
				}
				else
				{
					g.drawRegion(GameScr.imgRoomStat, 0, testConnect * 7, 7, 7, 0, (GameCanvas.w - mFont.tahoma_7b_dark.getWidth(cmd[2 + nCmdPlay].caption) >> 1) - 10, cmd[2 + nCmdPlay].y + 9, 0);
				}
			}
		}
		base.paint(g);
    	}
    
    	public void selectServer()
    	{
    		flagServer = 30;
    		GameCanvas.startWaitDlg(mResources.PLEASEWAIT);
    		Session_ME.gI().close();
    		GameMidlet.IP = address[ipSelect];
    		GameMidlet.PORT = port[ipSelect];
    		GameMidlet.LANGUAGE = language[ipSelect];
    		Rms.saveRMSInt("svselect", ipSelect);
    		if (language[ipSelect] != mResources.language)
    		{
    			mResources.loadLanguague(language[ipSelect]);
    		}
    		LoginScr.serverName = nameServer[ipSelect];
    		initCommand();
    		loadScreen = true;
    		countDieConnect = 0;
    		Controller.isConnectOK = false;
    		testConnect = -1;
    		isAutoConect = true;
    	}
    
    	public override void update()
    	{
    		if (waitToLogin)
    		{
    			tWaitToLogin++;
    			if (tWaitToLogin == 50)
    			{
    				GameCanvas.serverScreen.selectServer();
    			}
    			if (tWaitToLogin == 100)
    			{
    				if (GameCanvas.loginScr == null)
    				{
    					GameCanvas.loginScr = new LoginScr();
    				}
    				GameCanvas.loginScr.doLogin();
    				Service.gI().finishUpdate();
    				waitToLogin = false;
    			}
    		}
    		if (flagServer > 0)
    		{
    			flagServer--;
    			if (flagServer == 0)
    			{
    				GameCanvas.endDlg();
    			}
    			if (testConnect == 2)
    			{
    				flagServer = 0;
    				GameCanvas.endDlg();
    			}
    		}
    		if (flagServer <= 0 && isAutoConect)
    		{
    			countDieConnect++;
    			if (countDieConnect > 100000)
    			{
    				countDieConnect = 0;
    			}
    		}
    		for (int i = 0; i < cmd.Length; i++)
    		{
    			if (i == selected)
    			{
    				cmd[i].isFocus = true;
    			}
    			else
    			{
    				cmd[i].isFocus = false;
    			}
    		}
    		GameScr.cmx++;
    		if (!loadScreen && (bigOk || percent == 100))
    		{
    			cmdDownload = null;
    		}
    		base.update();
    		if (Char.isLoadingMap || !loadScreen || !isAutoConect || GameCanvas.currentScreen != this || testConnect == 2)
    		{
    			return;
    		}
    		if (countDieConnect < ((mSystem.clientType != 1) ? 5 : 2))
    		{
    			if (flagServer <= 0)
    			{
    				flagServer = 30;
    				GameCanvas.startWaitDlg(mResources.PLEASEWAIT);
    				GameCanvas.connect();
    			}
    		}
    		else if (!Session_ME.gI().isConnected())
    		{
    			if (flagServer <= 0)
    			{
    				Command cmdYes = new Command(mResources.YES, GameCanvas.serverScreen, 18, null);
    				Command cmdNo = new Command(mResources.NO, GameCanvas.serverScreen, 19, null);
    				GameCanvas.startYesNoDlg(mResources.maychutathoacmatsong + "." + mResources.confirmChangeServer, cmdYes, cmdNo);
    				flagServer = 30;
    			}
    		}
    		else if (flagServer <= 0)
    		{
    			countDieConnect = 0;
    		}
    	}
    
    	private void processInput()
    	{
    		if (loadScreen)
    		{
    			center = new Command(string.Empty, this, cmd[selected].idAction, null);
    		}
    		else
    		{
    			center = cmdDownload;
    		}
    	}
    
    	public static void updateDeleteData()
    	{
    		if (cmdDeleteRMS != null && cmdDeleteRMS.isPointerPressInside())
    		{
    			cmdDeleteRMS.performAction();
    		}
    	}
    
    	public override void updateKey()
    	{
    		if (GameCanvas.isTouch)
    		{
    			updateDeleteData();
    			if (cmdCallHotline != null && cmdCallHotline.isPointerPressInside())
    			{
    				cmdCallHotline.performAction();
    			}
    			if (!loadScreen)
    			{
    				if (cmdDownload != null && cmdDownload.isPointerPressInside())
    				{
    					cmdDownload.performAction();
    				}
    				base.updateKey();
    				return;
    			}
    			for (int i = 0; i < cmd.Length; i++)
    			{
    				if (cmd[i] == null || !cmd[i].isPointerPressInside())
    				{
    					continue;
    				}
    				if (testConnect == -1 || testConnect == 0)
    				{
    					if (cmd[i].caption.IndexOf(mResources.server) != -1)
    					{
    						cmd[i].performAction();
    					}
    				}
    				else
    				{
    					cmd[i].performAction();
    				}
    			}
    		}
    		else if (loadScreen)
    		{
    			if (GameCanvas.keyPressed[8])
    			{
    				int num = ((mGraphics.zoomLevel <= 1) ? 4 : 2);
    				GameCanvas.keyPressed[8] = false;
    				selected++;
    				if (selected > num)
    				{
    					selected = 0;
    				}
    				processInput();
    			}
    			if (GameCanvas.keyPressed[2])
    			{
    				int num2 = ((mGraphics.zoomLevel <= 1) ? 4 : 2);
    				GameCanvas.keyPressed[2] = false;
    				selected--;
    				if (selected < 0)
    				{
    					selected = num2;
    				}
    				processInput();
    			}
    		}
    		if (!isWait)
    		{
    			base.updateKey();
    		}
    	}
    
    	public static void saveIP()
    	{
    		DataOutputStream dataOutputStream = new DataOutputStream();
    		try
    		{
    			dataOutputStream.writeByte(mResources.language);
    			dataOutputStream.writeByte((sbyte)nameServer.Length);
    			for (int i = 0; i < nameServer.Length; i++)
    			{
    				dataOutputStream.writeUTF(nameServer[i]);
    				dataOutputStream.writeUTF(address[i]);
    				dataOutputStream.writeShort(port[i]);
    				dataOutputStream.writeByte(language[i]);
    				try
    				{
    					dataOutputStream.writeByte(typeSv[i]);
    				}
    				catch (Exception)
    				{
    					dataOutputStream.writeByte(0);
    				}
    				try
    				{
    					dataOutputStream.writeByte(isNew[i]);
    				}
    				catch (Exception)
    				{
    					dataOutputStream.writeByte(0);
    				}
    			}
    			serverPriority = (sbyte)((serverPriority >= 0 && serverPriority < nameServer.Length) ? serverPriority : 0);
    			dataOutputStream.writeByte(serverPriority);
    			Rms.saveRMS(RMS_NRlink, dataOutputStream.toByteArray());
    			dataOutputStream.close();
    			SplashScr.loadIP();
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public static bool allServerConnected()
    	{
    		for (int i = 0; i < 2; i++)
    		{
    			if (!hasConnected[i])
    			{
    				return false;
    			}
    		}
    		return true;
    	}
    
    	public static void loadIP()
    	{
    		sbyte[] array = Rms.loadRMS(RMS_NRlink);
    		if (array == null)
    		{
    			getServerList(linkDefault);
    			return;
    		}
    		DataInputStream dataInputStream = new DataInputStream(array);
    		if (dataInputStream == null)
    		{
    			return;
    		}
    		try
    		{
    			lengthServer = new int[3];
    			mResources.loadLanguague(dataInputStream.readByte());
    			sbyte b = dataInputStream.readByte();
    			nameServer = new string[b];
    			address = new string[b];
    			port = new short[b];
    			language = new sbyte[b];
    			typeSv = new sbyte[b];
    			isNew = new sbyte[b];
    			for (int i = 0; i < b; i++)
    			{
    				nameServer[i] = dataInputStream.readUTF();
    				address[i] = dataInputStream.readUTF();
    				port[i] = dataInputStream.readShort();
    				language[i] = dataInputStream.readByte();
    				try
    				{
    					typeSv[i] = dataInputStream.readByte();
    				}
    				catch (Exception)
    				{
    					typeSv[i] = 0;
    				}
    				try
    				{
    					isNew[i] = dataInputStream.readByte();
    				}
    				catch (Exception)
    				{
    					isNew[i] = 0;
    				}
    				lengthServer[language[i]]++;
    			}
    			serverPriority = dataInputStream.readByte();
    			dataInputStream.close();
    			SplashScr.loadIP();
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public override void switchToMe()
    	{
    		EffectManager.remove();
    		GameScr.cmy = 0;
    		GameScr.cmx = 0;
    		initCommand();
    		isWait = false;
    		GameCanvas.loginScr = null;
    		string text = Rms.loadRMSString("ResVersion");
    		int num = ((text == null || !(text != string.Empty)) ? (-1) : int.Parse(text));
    		if (num > 0)
    		{
    			loadScreen = true;
    			GameCanvas.loadBG(0);
    		}
    		bigOk = true;
    		if (nameServer == null || ipSelect < 0 || ipSelect >= nameServer.Length)
    		{
    			getServerList(linkDefault);
    			ipSelect = 0;
    		}
    		cmd[2 + nCmdPlay].caption = mResources.server + ": " + nameServer[ipSelect];
    		center = new Command(string.Empty, this, cmd[selected].idAction, null);
    		cmd[1 + nCmdPlay].caption = mResources.change_account;
    		if (cmd.Length == 4 + nCmdPlay)
    		{
    			cmd[3 + nCmdPlay].caption = mResources.option;
    		}
    		Char.isLoadingMap = false;
    		mSystem.resetCurInapp();
    		base.switchToMe();
    	}
    
    	public void switchToMe2()
    	{
    		GameScr.cmy = 0;
    		GameScr.cmx = 0;
    		initCommand();
    		isWait = false;
    		GameCanvas.loginScr = null;
    		string text = Rms.loadRMSString("ResVersion");
    		int num = ((text == null || !(text != string.Empty)) ? (-1) : int.Parse(text));
    		if (num > 0)
    		{
    			loadScreen = true;
    			GameCanvas.loadBG(0);
    		}
    		bigOk = true;
    		if (nameServer == null || ipSelect < 0 || ipSelect >= nameServer.Length)
    		{
    			getServerList(linkDefault);
    			ipSelect = 0;
    		}
    		cmd[2 + nCmdPlay].caption = mResources.server + ": " + nameServer[ipSelect];
    		center = new Command(string.Empty, this, cmd[selected].idAction, null);
    		cmd[1 + nCmdPlay].caption = mResources.change_account;
    		if (cmd.Length == 4 + nCmdPlay)
    		{
    			cmd[3 + nCmdPlay].caption = mResources.option;
    		}
    		mSystem.resetCurInapp();
    		base.switchToMe();
    	}
    
    	public void connectOk()
    	{
    	}
    
    	public void cancel()
    	{
    		if (GameCanvas.serverScreen == null)
    		{
    			GameCanvas.serverScreen = new ServerListScreen();
    		}
    		demPercent = 0;
    		percent = 0;
    		stopDownload = true;
    		GameCanvas.serverScreen.show2();
    		isGetData = false;
    		mSystem.println(">>>>>isGetData: " + isGetData);
    		cmdDownload.isFocus = true;
    		center = new Command(string.Empty, this, 2, null);
    	}
    
    	public void perform(int idAction, object p)
    	{
    		Res.outz("perform " + idAction);
    		if (idAction == 1000)
    		{
    			GameCanvas.connect();
    		}
    		if (idAction == 1 || idAction == 4)
    		{
    			Session_ME.gI().close();
    			isAutoConect = false;
    			countDieConnect = 0;
    			loadScreen = true;
    			testConnect = 0;
    			isGetData = false;
    			mSystem.println(">>>>>isGetData: " + isGetData);
    			Rms.clearAll();
    			switchToMe();
    		}
    		if (idAction == 2)
    		{
    			stopDownload = false;
    			cmdDownload = new Command(mResources.huy, this, 4, null);
    			cmdDownload.x = GameCanvas.w / 2 - mScreen.cmdW / 2;
    			cmdDownload.y = GameCanvas.hh + 65;
    			right = null;
    			if (!GameCanvas.isTouch)
    			{
    				cmdDownload.x = GameCanvas.w / 2 - mScreen.cmdW / 2;
    				cmdDownload.y = GameCanvas.h - mScreen.cmdH - 1;
    			}
    			center = new Command(string.Empty, this, 4, null);
    			if (!isGetData)
    			{
    				Service.gI().getResource(1, null);
    				if (!GameCanvas.isTouch)
    				{
    					cmdDownload.isFocus = true;
    					center = new Command(string.Empty, this, 4, null);
    					mSystem.println(">>>>>isGetData: " + isGetData);
    				}
    				isGetData = true;
    			}
    		}
    		if (idAction == 3)
    		{
    			Res.outz("toi day");
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			GameCanvas.loginScr.switchToMe();
    			bool flag = Rms.loadRMSString("acc") != null && !Rms.loadRMSString("acc").Equals(string.Empty);
    			bool flag2 = Rms.loadRMSString("userAo" + ipSelect) != null && !Rms.loadRMSString("userAo" + ipSelect).Equals(string.Empty);
    			if (!flag && !flag2)
    			{
    				GameCanvas.connect();
    				string text = Rms.loadRMSString("userAo" + ipSelect);
    				if (text == null || text.Equals(string.Empty))
    				{
    					Service.gI().login2(string.Empty);
    				}
    				else
    				{
    					GameCanvas.loginScr.isLogin2 = true;
    					GameCanvas.connect();
    					Service.gI().setClientType();
    					Service.gI().login(text, string.Empty, GameMidlet.VERSION, 1);
    				}
    				if (Session_ME.connected)
    				{
    					GameCanvas.startWaitDlg();
    				}
    				else
    				{
    					GameCanvas.startOKDlg(mResources.maychutathoacmatsong);
    				}
    			}
    			else
    			{
    				GameCanvas.loginScr.doLogin();
    			}
    			LoginScr.serverName = nameServer[ipSelect];
    		}
    		if (idAction == 10100)
    		{
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			GameCanvas.loginScr.switchToMe();
    			GameCanvas.connect();
    			Service.gI().login2(string.Empty);
    			Res.outz("tao user ao");
    			GameCanvas.startWaitDlg();
    			LoginScr.serverName = nameServer[ipSelect];
    		}
    		if (idAction == 5)
    		{
    			doUpdateServer();
    			if (nameServer.Length == 1)
    			{
    				return;
    			}
    			MyVector myVector = new MyVector(string.Empty);
    			for (int i = 0; i < nameServer.Length; i++)
    			{
    				myVector.addElement(new Command(nameServer[i], this, 6, null));
    			}
    			GameCanvas.menu.startAt(myVector, 0);
    			if (!GameCanvas.isTouch)
    			{
    				GameCanvas.menu.menuSelectedItem = ipSelect;
    			}
    		}
    		if (idAction == 6)
    		{
    			ipSelect = GameCanvas.menu.menuSelectedItem;
    			selectServer();
    		}
    		if (idAction == 7)
    		{
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			GameCanvas.loginScr.switchToMe();
    		}
    		if (idAction == 8)
    		{
    			bool flag3 = Rms.loadRMSInt("lowGraphic") == 1;
    			MyVector myVector2 = new MyVector("cau hinh");
    			myVector2.addElement(new Command(mResources.cauhinhthap, this, 9, null));
    			myVector2.addElement(new Command(mResources.cauhinhcao, this, 10, null));
    			GameCanvas.menu.startAt(myVector2, 0);
    			if (flag3)
    			{
    				GameCanvas.menu.menuSelectedItem = 0;
    			}
    			else
    			{
    				GameCanvas.menu.menuSelectedItem = 1;
    			}
    		}
    		if (idAction == 9)
    		{
    			Rms.saveRMSInt("lowGraphic", 1);
    			GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
    		}
    		if (idAction == 10)
    		{
    			Rms.saveRMSInt("lowGraphic", 0);
    			GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
    		}
    		if (idAction == 11)
    		{
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			GameCanvas.loginScr.switchToMe();
    			string text2 = Rms.loadRMSString("userAo" + ipSelect);
    			if (text2 == null || text2.Equals(string.Empty))
    			{
    				Service.gI().login2(string.Empty);
    			}
    			else
    			{
    				GameCanvas.loginScr.isLogin2 = true;
    				GameCanvas.connect();
    				Service.gI().setClientType();
    				Service.gI().login(text2, string.Empty, GameMidlet.VERSION, 1);
    			}
    			GameCanvas.startWaitDlg(mResources.PLEASEWAIT);
    			Res.outz("tao user ao");
    		}
    		if (idAction == 12)
    		{
    			GameMidlet.instance.exit();
    		}
    		if (idAction == 13 && (!isGetData || loadScreen))
    		{
    			switch (mSystem.clientType)
    			{
    			case 1:
    				mSystem.callHotlineJava();
    				break;
    			case 3:
    			case 5:
    				mSystem.callHotlineIphone();
    				break;
    			case 6:
    				mSystem.callHotlineWindowsPhone();
    				break;
    			case 4:
    				mSystem.callHotlinePC();
    				break;
    			}
    		}
    		if (idAction == 14)
    		{
    			Command cmdYes = new Command(mResources.YES, this, 15, null);
    			Command cmdNo = new Command(mResources.NO, this, 16, null);
    			GameCanvas.startYesNoDlg("Bạn có muốn xóa dữ liệu cũ và tải lại phiên bản mới nhất từ máy chủ ngay bây giờ?", cmdYes, cmdNo);
    		}
    		if (idAction == 15)
    		{
    			GameCanvas.endDlg();
    			Rms.clearAll();
    			SmallImage.freeBig();
    			BgItem.clearHashTable();
    			TileMap.vItemBg.removeAllElements();
    			show2();
    			if (!isGetData)
    			{
    				isGetData = true;
    				stopDownload = false;
    				demPercent = 0;
    				percent = 0;
    				GameCanvas.connect();
    				Service.gI().getResource(1, null);
    			}
    		}
    		if (idAction == 16)
    		{
    			InfoDlg.hide();
    			GameCanvas.currentDialog = null;
    		}
    		if (idAction == 17)
    		{
    			if (GameCanvas.serverScr == null)
    			{
    				GameCanvas.serverScr = new ServerScr();
    			}
    			GameCanvas.serverScr.switchToMe();
    		}
    		if (idAction == 18)
    		{
    			GameCanvas.endDlg();
    			InfoDlg.hide();
    			if (GameCanvas.serverScr == null)
    			{
    				GameCanvas.serverScr = new ServerScr();
    			}
    			GameCanvas.serverScr.switchToMe();
    		}
    		if (idAction == 19)
    		{
    			if (mSystem.clientType == 1)
    			{
    				InfoDlg.hide();
    				GameCanvas.currentDialog = null;
    			}
    			else
    			{
    				countDieConnect = 0;
    				testConnect = 0;
    				isAutoConect = true;
    			}
    		}
    	}
    
    	public void init()
    	{
    		if (!loadScreen)
    		{
    			cmdDownload = new Command(mResources.taidulieu, this, 2, null);
    			cmdDownload.isFocus = true;
    			cmdDownload.x = GameCanvas.w / 2 - mScreen.cmdW / 2;
    			cmdDownload.y = GameCanvas.hh + 45;
    			if (cmdDownload.y > GameCanvas.h - 26)
    			{
    				cmdDownload.y = GameCanvas.h - 26;
    			}
    		}
    		if (!GameCanvas.isTouch)
    		{
    			selected = 0;
    			processInput();
    		}
    	}
    
    	public void show2()
    	{
    		GameScr.cmx = 0;
    		GameScr.cmy = 0;
    		initCommand();
    		loadScreen = false;
    		percent = 0;
    		bigOk = false;
    		isGetData = false;
    		mSystem.println(">>>>>isGetData: " + isGetData);
    		p = 0;
    		demPercent = 0;
    		strWait = mResources.PLEASEWAIT;
    		Char.isLoadingMap = false;
    		GameCanvas.loadBG(0);
    		init();
    		base.switchToMe();
    	}
    
    	public void setLinkDefault(sbyte language)
    	{
    		if (language == 2)
    		{
    			if (mSystem.clientType == 1)
    			{
    				linkDefault = javaIn;
    			}
    			else
    			{
    				linkDefault = smartPhoneIn;
    			}
    		}
    		else if (language == 1)
    		{
    			linkDefault = javaE;
    			if (mSystem.clientType == 1)
    			{
    				linkDefault = javaE;
    			}
    			else
    			{
    				linkDefault = smartPhoneE;
    			}
    		}
    		else
    		{
    			linkDefault = javaVN;
    			if (mSystem.clientType == 1)
    			{
    				linkDefault = javaVN;
    			}
    			else
    			{
    				linkDefault = smartPhoneVN;
    			}
    		}
    		mSystem.AddIpTest();
    	}
    
    	static ServerListScreen()
    	{
            smartPhoneVN = "Localhost:127.0.0.1:14445:0:0:0,0,0";
    		javaVN = smartPhoneVN;
    		smartPhoneIn = "Naga:dragon.indonaga.com:14446:2:0:0,2,0";
    		javaIn = "Naga:52.74.230.22:14446:2:0:0,2,0";
    		smartPhoneE = "Universe 1:dragon.indonaga.com:14445:1:0:0,1,0";
    		javaE = "Universe 1:52.74.230.22:14445:1:0:0,1,0";
    		linkGetHost = "http://sv1.ngocrongonline.com/game/ngocrong031_t.php";
    		linkDefault = javaVN;
    		linkweb = "http://ngocrongonline.com";
    		RMS_NRlink = "NRlink3";
    		lengthServer = new int[3];
    		isGetData = false;
    		testConnect = -1;
    		isAutoConect = true;
    	}
    }
}

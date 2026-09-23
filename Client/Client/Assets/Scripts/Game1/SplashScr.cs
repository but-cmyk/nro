namespace Game1
{
    public class SplashScr : mScreen
    {
    	public const int MIN_SPLASH_TICKS = 45;

    	public static mScreen pendingScreen;

    	public static int splashScrStat;
    
    	private bool isCheckConnect;
    
    	private bool isSwitchToLogin;
    
    	public static int nData = -1;
    
    	public static int maxData = -1;
    
    	public static SplashScr instance;
    
    	public static Image imgLogo;
    
    	private int timeLoading = 10;
    
    	public long TIMEOUT;
    
    	public SplashScr()
    	{
    		instance = this;
    	}
    
    	public static void loadSplashScr()
    	{
    		splashScrStat = 0;
    		pendingScreen = null;
    	}
    
    	public override void update()
    	{
    		if (splashScrStat == 20 && !isCheckConnect)
    		{
    			isCheckConnect = true;
    			if (Rms.loadRMSInt("serverchat") != -1)
    			{
    				GameScr.isPaintChatVip = Rms.loadRMSInt("serverchat") == 0;
    			}
    			if (Rms.loadRMSInt("isPlaySound") != -1)
    			{
    				GameCanvas.isPlaySound = Rms.loadRMSInt("isPlaySound") == 1;
    			}
    			if (GameCanvas.isPlaySound)
    			{
    				SoundMn.gI().loadSound(TileMap.mapID);
    			}
    			SoundMn.gI().getStrOption();
    			if (Rms.loadRMSInt("svselect") == -1)
    			{
    				ServerListScreen.getServerList(ServerListScreen.linkDefault);
    				GameCanvas.serverScr.switchToMe();
    			}
    			else
    			{
    				ServerListScreen.loadIP();
    			}
    		}
    		splashScrStat++;
    		ServerListScreen.updateDeleteData();
    		if (pendingScreen != null && splashScrStat >= MIN_SPLASH_TICKS && nData == -1)
    		{
    			mScreen next = pendingScreen;
    			pendingScreen = null;
    			next.switchToMe();
    			return;
    		}
    		if (splashScrStat >= 150)
    		{
    			Res.outz("cho man hinh nay qa lau");
    			if (Session_ME.gI().isConnected())
    			{
    				ServerListScreen.loadScreen = true;
    				GameCanvas.serverScreen.switchToMe();
    			}
    			else
    			{
    				mSystem.onDisconnected();
    			}
    		}
    	}
    
    	public static void loadIP()
    	{
    		if (Rms.loadRMSInt("svselect") == -1)
    		{
    			Res.outz(">>>loadIP:  svselect == -1");
    			int num = 0;
    			if (mResources.language > 0)
    			{
    				for (int i = 0; i < mResources.language; i++)
    				{
    					num += ServerListScreen.lengthServer[i];
    				}
    			}
    			if (ServerListScreen.serverPriority == -1)
    			{
    				ServerListScreen.ipSelect = num + Res.random(0, ServerListScreen.lengthServer[mResources.language]);
    			}
    			else
    			{
    				ServerListScreen.ipSelect = ServerListScreen.serverPriority;
    			}
    			Rms.saveRMSInt("svselect", ServerListScreen.ipSelect);
    			GameMidlet.IP = ServerListScreen.address[ServerListScreen.ipSelect];
    			GameMidlet.PORT = ServerListScreen.port[ServerListScreen.ipSelect];
    			mResources.loadLanguague(ServerListScreen.language[ServerListScreen.ipSelect]);
    			LoginScr.serverName = ServerListScreen.nameServer[ServerListScreen.ipSelect];
    			GameCanvas.connect();
    		}
    		else
    		{
    			ServerListScreen.ipSelect = Rms.loadRMSInt("svselect");
    			Res.outz(">>>loadIP:  ipSelect == " + ServerListScreen.ipSelect);
    			if (ServerListScreen.nameServer != null && (ServerListScreen.ipSelect > ServerListScreen.nameServer.Length - 1 || ServerListScreen.ipSelect < 0))
    			{
    				ServerListScreen.ipSelect = 0;
    				Rms.saveRMSInt("svselect", ServerListScreen.ipSelect);
    			}
    			if (ServerListScreen.address != null && ServerListScreen.ipSelect >= 0 && ServerListScreen.ipSelect < ServerListScreen.address.Length)
    			{
    				GameMidlet.IP = ServerListScreen.address[ServerListScreen.ipSelect];
    				GameMidlet.PORT = ServerListScreen.port[ServerListScreen.ipSelect];
    				mResources.loadLanguague(ServerListScreen.language[ServerListScreen.ipSelect]);
    				LoginScr.serverName = ServerListScreen.nameServer[ServerListScreen.ipSelect];
    			}
    			GameCanvas.connect();
    		}
    	}
    
    	public override void paint(mGraphics g)
    	{
    		if (nData != -1)
    		{
    			if (!GameCanvas.paintBG || GameCanvas.imgBG == null)
    			{
    				TileMap.lastBgID = -1;
    				TileMap.lastType = -1;
    				GameCanvas.loadBG(0);
    			}
    			if (GameScr.cmy == 0 && GameScr.cmx == 0)
    			{
    				GameScr.loadCamera(true, -1, -1);
    				GameScr.cmx = 100;
    				GameScr.cmy = 200;
    			}
    			GameCanvas.paintBGGameScr(g);
    			if (LoginScr.imgTitle != null)
    			{
    				g.drawImage(LoginScr.imgTitle, GameCanvas.w / 2, GameCanvas.h / 2 - 40, StaticObj.BOTTOM_HCENTER);
    			}

    			int percent = (maxData > 0) ? (nData * 100 / maxData) : 0;
    			mFont.tahoma_7b_dark.drawString(g, mResources.downloading_data + percent + "%", GameCanvas.w / 2, GameCanvas.h / 2 - 12, 2);

    			int barW = 190;
    			int barH = 11;
    			int barX = GameCanvas.w / 2 - barW / 2;
    			int barY = GameCanvas.h / 2 + 6;

    			g.setColor(0x4a1403);
    			g.fillRect(barX, barY, barW, barH, 4);
    			g.setColor(0x8c2b06);
    			g.drawRect(barX, barY, barW, barH);

    			int fillW = (int)((float)barW * (float)percent / 100f);
    			if (fillW > barW) fillW = barW;
    			if (fillW > 0)
    			{
    				g.setColor(0x00d2d3);
    				g.fillRect(barX, barY, fillW, barH, 4);
    				g.setColor(0xffffff);
    				g.fillRect(barX, barY + 1, fillW, 2);
    			}
    			return;
    		}

    		// Nền tối sang trọng (Dark Slate Navy)
    		g.setColor(0x0c0f17);
    		g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

    		// 1. Logo Game
    		if (LoginScr.imgTitle == null)
    		{
    			LoginScr.imgTitle = GameCanvas.loadImage("/mainImage/logo1.png");
    		}
    		if (LoginScr.imgTitle != null)
    		{
    			g.drawImage(LoginScr.imgTitle, GameCanvas.hw, GameCanvas.hh - 28, StaticObj.BOTTOM_HCENTER);
    		}

    		// 2. Viên ngọc rồng xoay xoay
    		int ballY = GameCanvas.hh + 18;
    		GameCanvas.paintShukiren(GameCanvas.hw, ballY, g);

    		// 3. Dòng chữ trang nhã kèm dấu ba chấm động
    		string dots = "";
    		int dotCount = (splashScrStat / 8) % 4;
    		for (int i = 0; i < dotCount; i++)
    		{
    			dots += ".";
    		}
    		string loadingText = "Đang kiểm tra dữ liệu và kết nối máy chủ" + dots;
    		int textY = ballY + 18;

    		// Đổ bóng chữ để tạo chiều sâu thẩm mỹ
    		mFont.tahoma_7_grey.drawString(g, loadingText, GameCanvas.hw + 1, textY + 1, 2);
    		mFont.tahoma_7b_white.drawString(g, loadingText, GameCanvas.hw, textY, 2);

    		// 4. Nút Xóa dữ liệu (góc dưới bên phải)
    		if (ServerListScreen.cmdDeleteRMS != null)
    		{
    			int btnW = 96;
    			int btnH = 24;
    			int btnX = GameCanvas.w - btnW - 6;
    			int btnY = GameCanvas.h - btnH - 6;
    			ServerListScreen.cmdDeleteRMS.x = btnX;
    			ServerListScreen.cmdDeleteRMS.y = btnY;
    			ServerListScreen.cmdDeleteRMS.w = btnW;
    			ServerListScreen.cmdDeleteRMS.h = btnH;

    			bool isHover = ServerListScreen.cmdDeleteRMS.isPointerPressInside();
    			if (Command.btn0left != null && Command.btn0mid != null && Command.btn0right != null)
    			{
    				Command.paintOngMau(isHover ? Command.btn1left : Command.btn0left, isHover ? Command.btn1mid : Command.btn0mid, isHover ? Command.btn1right : Command.btn0right, btnX, btnY, btnW, g);
    			}
    			else
    			{
    				g.setColor(0x6d1f05);
    				g.fillRect(btnX, btnY, btnW, btnH, 5);
    				g.setColor(0xfcd34d);
    				g.fillRect(btnX + 1, btnY + 1, btnW - 2, btnH - 2, 4);
    				g.setColor(isHover ? 0xf58e38 : 0xe67824);
    				g.fillRect(btnX + 2, btnY + 2, btnW - 4, btnH - 4, 3);
    			}
    			mFont.tahoma_7b_dark.drawString(g, mResources.xoadulieu, btnX + btnW / 2, btnY + 6, 2);
    		}
    	}
    
    	public static void loadImg()
    	{
    		imgLogo = GameCanvas.loadImage("/gamelogo.png");
    		if (LoginScr.imgTitle == null)
    		{
    			LoginScr.imgTitle = GameCanvas.loadImage("/mainImage/logo1.png");
    		}
    	}
    }
}

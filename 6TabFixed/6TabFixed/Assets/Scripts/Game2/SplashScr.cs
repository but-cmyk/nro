
namespace Game2
{
    public class SplashScr : mScreen
    {
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
    	}
    
    	public override void update()
    	{
    		if (splashScrStat == 30 && !isCheckConnect)
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
    		if (imgLogo != null && splashScrStat < 30)
    		{
    			g.setColor(16777215);
    			g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    			g.drawImage(imgLogo, GameCanvas.w / 2, GameCanvas.h / 2, 3);
    		}
    		if (nData != -1)
    		{
    			GameCanvas.paintBGGameScr(g);
    			g.drawImage(LoginScr.imgTitle, GameCanvas.w / 2, GameCanvas.h / 2 - 36, StaticObj.BOTTOM_HCENTER);

    			int percent = (maxData > 0) ? (nData * 100 / maxData) : 0;
    			mFont.tahoma_7b_white.drawString(g, mResources.downloading_data + percent + "%", GameCanvas.w / 2, GameCanvas.h / 2 - 10, 2);

    			int barW = 180;
    			int barH = 10;
    			int barX = GameCanvas.w / 2 - barW / 2;
    			int barY = GameCanvas.h / 2 + 8;

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
    		}
    		else if (splashScrStat >= 30)
    		{
    			g.setColor(0);
    			g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    			GameCanvas.paintShukiren(GameCanvas.hw, GameCanvas.hh, g);
    			if (ServerListScreen.cmdDeleteRMS != null)
    			{
    				int btnW = 96;
    				int btnH = 22;
    				int btnX = GameCanvas.w - btnW - 6;
    				int btnY = GameCanvas.h - btnH - 6;
    				ServerListScreen.cmdDeleteRMS.x = btnX;
    				ServerListScreen.cmdDeleteRMS.y = btnY;
    				ServerListScreen.cmdDeleteRMS.w = btnW;
    				ServerListScreen.cmdDeleteRMS.h = btnH;

    				bool isHover = ServerListScreen.cmdDeleteRMS.isPointerPressInside();
    				g.setColor(isHover ? 0x242a38 : 0x141822);
    				g.fillRect(btnX, btnY, btnW, btnH, 4);
    				g.setColor(isHover ? 0x00d2d3 : 0x3d4a60);
    				g.drawRect(btnX, btnY, btnW, btnH);
    				mFont.tahoma_7_white.drawString(g, "🔄 " + mResources.xoadulieu, btnX + btnW / 2, btnY + 5, 2);
    			}
    		}
    	}
    
    	public static void loadImg()
    	{
    		imgLogo = GameCanvas.loadImage("/gamelogo.png");
    	}
    }
}

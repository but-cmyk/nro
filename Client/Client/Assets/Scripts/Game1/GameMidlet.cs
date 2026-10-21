namespace Game1
{
    using UnityEngine;
    
    public class GameMidlet
    {
    	public static string IP = "112.213.94.23";
    
    	public static int PORT = 14445;
    
    	public static string IP2;
    
    	public static int PORT2;
    
    	public static sbyte PROVIDER;
    
    	public static int LANGUAGE;
    
    	public static string VERSION = "2.4.0";
    
    	public static int intVERSION = 240;
    
    	public static GameCanvas gameCanvas;
    
    	public static GameMidlet instance;
    
    	public static bool isConnect2;
    
    	public static bool isBackWindowsPhone;
    
    	public GameMidlet()
    	{
    		initGame();
    	}
    
    	public void initGame()
    	{
    		instance = this;
    		MotherCanvas.instance = new MotherCanvas();
    		Session_ME.gI().setHandler(Controller.gI());
    		Session_ME2.gI().setHandler(Controller.gI());
    		Session_ME2.isMainSession = false;
    		instance = this;
    		gameCanvas = new GameCanvas();
    		gameCanvas.start();
    		SplashScr.loadImg();
    		SplashScr.loadSplashScr();
    		
    		if (Rms.loadRMSInt("serverchat") != -1)
    		{
    			GameScr.isPaintChatVip = Rms.loadRMSInt("serverchat") == 0;
    		}
    		if (Rms.loadRMSInt("default_cfg_v1") == -1)
    		{
    			Rms.saveRMSInt("default_cfg_v1", 1);
    			Rms.saveRMSInt("isPaintAura", 1);
    			Rms.saveRMSInt("isPaintAura2", 1);
    			Rms.saveRMSInt("isPlaySound", 1);
    			Char.isPaintAura = true;
    			Char.isPaintAura2 = true;
    			GameCanvas.isPlaySound = true;
    		}
    		else
    		{
    			Char.isPaintAura = (Rms.loadRMSInt("isPaintAura") != 0);
    			Char.isPaintAura2 = (Rms.loadRMSInt("isPaintAura2") != 0);
    			GameCanvas.isPlaySound = (Rms.loadRMSInt("isPlaySound") != 0);
    		}
    		SoundMn.gI().getStrOption();

    		if (GameCanvas.serverScreen == null)
    		{
    			GameCanvas.serverScreen = new ServerListScreen();
    		}
    		if (Rms.loadRMSInt("svselect") == -1)
    		{
    			ServerListScreen.getServerList(ServerListScreen.linkDefault);
    		}
    		ServerListScreen.loadIP();
    		ServerListScreen.loadScreen = true;
    		ServerListScreen.createDeleteRMS();

    		if (SplashScr.instance == null)
    		{
    			SplashScr.instance = new SplashScr();
    		}
    		SplashScr.loadImg();
    		SplashScr.loadSplashScr();
    		SplashScr.instance.switchToMe();
    	}
    
    	public void exit()
    	{
    		if (Main.typeClient == 6)
    		{
    			mSystem.exitWP();
    			return;
    		}
    		GameCanvas.bRun = false;
    		mSystem.gcc();
    		notifyDestroyed();
    	}
    
    	public static void sendSMS(string data, string to, Command successAction, Command failAction)
    	{
    		Cout.println("SEND SMS");
    	}
    
    	public static void flatForm(string url)
    	{
    		Cout.LogWarning("PLATFORM REQUEST: " + url);
    		Application.OpenURL(url);
    	}
    
    	public void notifyDestroyed()
    	{
    		Main.exit();
    	}
    
    	public void platformRequest(string url)
    	{
    		Cout.LogWarning("PLATFORM REQUEST: " + url);
    		Application.OpenURL(url);
    	}
    }
}

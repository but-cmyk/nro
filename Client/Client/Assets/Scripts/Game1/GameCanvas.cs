namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameCanvas : IActionListener
    {
    	public static long timeNow = 0L;
    
    	public static bool open3Hour;
    
    	public static bool lowGraphic = false;
    
    	public static bool serverchat = false;
    
    	public static bool isMoveNumberPad = true;
    
    	public static bool isLoading;
    
    	public static bool isTouch = false;
    
    	public static bool isTouchControl;
    
    	public static bool isTouchControlSmallScreen;
    
    	public static bool isTouchControlLargeScreen;
    
    	public static bool isConnectFail;
    
    	public static GameCanvas instance;
    
    	public static bool bRun;
    
    	public static bool[] keyPressed = new bool[30];
    
    	public static bool[] keyReleased = new bool[30];
    
    	public static bool[] keyHold = new bool[30];
    
    	public static bool isPointerDown;
    
    	public static bool isPointerClick;
    
    	public static bool isPointerJustRelease;
    
    	public static bool isPointerSelect;
    
    	public static bool isPointerMove;
    
    	public static int px;
    
    	public static int py;
    
    	public static int pxFirst;
    
    	public static int pyFirst;
    
    	public static int pxLast;
    
    	public static int pyLast;
    
    	public static int pxMouse;
    
    	public static int pyMouse;
    
    	public static Position[] arrPos = new Position[4];
    
    	public static int gameTick;
    
    	public static int taskTick;
    
    	public static bool isEff1;
    
    	public static bool isEff2;
    
    	public static long timeTickEff1;
    
    	public static long timeTickEff2;
    
    	public static int w;
    
    	public static int h;
    
    	public static int hw;
    
    	public static int hh;
    
    	public static int wd3;
    
    	public static int hd3;
    
    	public static int w2d3;
    
    	public static int h2d3;
    
    	public static int w3d4;
    
    	public static int h3d4;
    
    	public static int wd6;
    
    	public static int hd6;
    
    	public static mScreen currentScreen;
    
    	public static Menu menu = new Menu();
    
    	public static Panel panel;
    
    	public static Panel panel2;
    
    	public static ChooseCharScr chooseCharScr;
    
    	public static LoginScr loginScr;
    
    	public static RegisterScreen registerScr;
    
    	public static Dialog currentDialog;
    
    	public static MsgDlg msgdlg;
    
    	public static InputDlg inputDlg;
    
    	public static MyVector currentPopup = new MyVector();
    
    	public static int requestLoseCount;
    
    	public static MyVector listPoint;
    
    	public static Paint paintz;
    
    	public static bool isGetResFromServer;
    
    	public static Image[] imgBG;
    
    	public static int skyColor;
    
    	public static int curPos = 0;
    
    	public static int[] bgW;
    
    	public static int[] bgH;
    
    	public static int planet = 0;
    
    	private mGraphics g = new mGraphics();
    
    	public static Image img12;
    
    	public static Image[] imgBlue = new Image[7];
    
    	public static Image[] imgViolet = new Image[7];
    
    	public static MyHashTable danhHieu = new MyHashTable();
    
    	public static MyVector messageServer = new MyVector(string.Empty);
    
    	public static bool isPlaySound = true;
    
    	private static int clearOldData;
    
    	public static int timeOpenKeyBoard;
    
    	public static bool isFocusPanel2;
    
    	public static int fps = 0;
    
    	public static int max;
    
    	public static int up;
    
    	public static int upmax;
    
    	private long timefps = mSystem.currentTimeMillis() + 1000;
    
    	private long timeup = mSystem.currentTimeMillis() + 1000;
    
    	public static int isRequestMapID = -1;
    
    	public static long waitingTimeChangeMap;
    
    	private static int dir_ = -1;
    
    	private int tickWaitThongBao;
    
    	public bool isPaintCarret;
    
    	public static MyVector debugUpdate;
    
    	public static MyVector debugPaint;
    
    	public static MyVector debugSession;
    
    	private static bool isShowErrorForm = false;
    
    	public static bool paintBG;
    
    	public static int gsskyHeight;
    
    	public static int gsgreenField1Y;
    
    	public static int gsgreenField2Y;
    
    	public static int gshouseY;
    
    	public static int gsmountainY;
    
    	public static int bgLayer0y;
    
    	public static int bgLayer1y;
    
    	public static Image imgCloud;
    
    	public static Image imgSun;
    
    	public static Image imgSun2;
    
    	public static Image imgClear;
    
    	public static Image[] imgBorder = new Image[3];
    
    	public static Image[] imgSunSpec = new Image[3];
    
    	public static int borderConnerW;
    
    	public static int borderConnerH;
    
    	public static int borderCenterW;
    
    	public static int borderCenterH;
    
    	public static int[] cloudX;
    
    	public static int[] cloudY;
    
    	public static int sunX;
    
    	public static int sunY;
    
    	public static int sunX2;
    
    	public static int sunY2;
    
    	public static int[] layerSpeed;
    
    	public static int[] moveX;
    
    	public static int[] moveXSpeed;

    	private static void SetLayerSpeed5(int a, int b, int c, int d, int e)
    	{
    		if (layerSpeed == null || layerSpeed.Length < 5) layerSpeed = new int[5];
    		layerSpeed[0] = a; layerSpeed[1] = b; layerSpeed[2] = c; layerSpeed[3] = d; layerSpeed[4] = e;
    	}

    	private static void SetLayerSpeed4(int a, int b, int c, int d)
    	{
    		if (layerSpeed == null || layerSpeed.Length < 4) layerSpeed = new int[4];
    		layerSpeed[0] = a; layerSpeed[1] = b; layerSpeed[2] = c; layerSpeed[3] = d;
    	}

    	private static void SetMoveX(int a, int b, int c, int d, int e)
    	{
    		if (moveX == null || moveX.Length < 5) moveX = new int[5];
    		moveX[0] = a; moveX[1] = b; moveX[2] = c; moveX[3] = d; moveX[4] = e;
    	}

    	private static void SetMoveXSpeed(int a, int b, int c, int d, int e)
    	{
    		if (moveXSpeed == null || moveXSpeed.Length < 5) moveXSpeed = new int[5];
    		moveXSpeed[0] = a; moveXSpeed[1] = b; moveXSpeed[2] = c; moveXSpeed[3] = d; moveXSpeed[4] = e;
    	}

    	private static void ResetMoveX()
    	{
    		if (moveX == null || moveX.Length < 5) moveX = new int[5]; else Array.Clear(moveX, 0, moveX.Length);
    		if (moveXSpeed == null || moveXSpeed.Length < 5) moveXSpeed = new int[5]; else Array.Clear(moveXSpeed, 0, moveXSpeed.Length);
    	}
    
    	public static bool isBoltEff;
    
    	public static bool boltActive;
    
    	public static int tBolt;
    
    	public static Image imgBgIOS;
    
    	public static int typeBg = -1;
    
    	public static int transY;
    
    	public static int[] yb = new int[5];
    
    	public static int[] colorTop;
    
    	public static int[] colorBotton;
    
    	public static int yb1;
    
    	public static int yb2;
    
    	public static int yb3;
    
    	public static int nBg = 0;
    
    	public static int lastBg = -1;
    
    	public static int[] bgRain = new int[3] { 1, 4, 11 };
    
    	public static int[] bgRainFont = new int[1] { -1 };
    
    	public static Image imgCaycot;
    
    	public static Image tam;
    
    	public static int typeBackGround = -1;
    
    	public static int saveIDBg = -10;
    
    	public static bool isLoadBGok;
    
    	private static long lastTimePress = 0L;
    
    	public static int keyAsciiPress;
    
    	public static int pXYScrollMouse;
    
    	private static Image imgSignal;
    
    	public static MyVector flyTexts = new MyVector();
    
    	public int longTime;
    
    	public static long timeBreakLoading;
    
    	private static string thongBaoTest;
    
    	public static int xThongBaoTranslate = w - 60;
    
    	public static bool isPointerJustDown = false;
    
    	private int count = 1;
    
    	public static bool csWait;
    
    	public static MyRandom r = new MyRandom();
    
    	public static bool isBlackScreen;
    
    	public static int[] bgSpeed;
    
    	public static int cmdBarX;
    
    	public static int cmdBarY;
    
    	public static int cmdBarW;
    
    	public static int cmdBarH;
    
    	public static int cmdBarLeftW;
    
    	public static int cmdBarRightW;
    
    	public static int cmdBarCenterW;
    
    	public static int hpBarX;
    
    	public static int hpBarY;
    
    	public static int hpBarW;
    
    	public static int expBarW;
    
    	public static int lvPosX;
    
    	public static int moneyPosX;
    
    	public static int hpBarH;
    
    	public static int girlHPBarY;
    
    	public int timeOut;
    
    	public int[] dustX;
    
    	public int[] dustY;
    
    	public int[] dustState;

    	public int[] dustDir;

    	public const int MAX_DUST = 30;
    
    	public static int[] wsX;
    
    	public static int[] wsY;
    
    	public static int[] wsState;
    
    	public static int[] wsF;
    
    	public static Image[] imgWS;
    
    	public static Image imgShuriken;
    
    	public static Image[][] imgDust;
    
    	public static bool isResume;
    
    	public static ServerListScreen serverScreen;
    
    	public static ServerScr serverScr;
    
    	public bool resetToLoginScr;
    
    	public static long TIMEOUT;
    
    	public static int timeLoading = 15;
    
    	public GameCanvas()
    	{
    		switch (Rms.loadRMSInt("languageVersion"))
    		{
    		case -1:
    			Rms.saveRMSInt("languageVersion", 2);
    			break;
    		default:
    			Main.main.doClearRMS();
    			Rms.saveRMSInt("languageVersion", 2);
    			break;
    		case 2:
    			break;
    		}
    		clearOldData = Rms.loadRMSInt(GameMidlet.VERSION);
    		if (clearOldData != 1)
    		{
    			Main.main.doClearRMS();
    			Rms.saveRMSInt(GameMidlet.VERSION, 1);
    		}
    		initGame();
    	}
    
    	public static string getPlatformName()
    	{
    		return "Pc platform xxx";
    	}
    
    	public void initGame()
    	{
    		MotherCanvas.instance.setChildCanvas(this);
    		w = MotherCanvas.instance.getWidthz();
    		h = MotherCanvas.instance.getHeightz();
    		hw = w / 2;
    		hh = h / 2;
    		isTouch = true;
    		if (w >= 240)
    		{
    			isTouchControl = true;
    		}
    		if (w < 320)
    		{
    			isTouchControlSmallScreen = true;
    		}
    		if (w >= 320)
    		{
    			isTouchControlLargeScreen = true;
    		}
    		msgdlg = new MsgDlg();
    		if (h <= 160)
    		{
    			Paint.hTab = 15;
    			mScreen.cmdH = 17;
    		}
    		GameScr.d = ((w <= h) ? h : w) + 20;
    		instance = this;
    		mFont.init();
    		mScreen.ITEM_HEIGHT = mFont.tahoma_8b.getHeight() + 8;
    		initPaint();
    		loadDust();
    		loadWaterSplash();
    		panel = new Panel();
    		imgShuriken = loadImage("/mainImage/myTexture2df.png");
    		int num = Rms.loadRMSInt("clienttype");
    		if (num != -1)
    		{
    			if (num > 7)
    			{
    				Rms.saveRMSInt("clienttype", mSystem.clientType);
    			}
    			else
    			{
    				mSystem.clientType = num;
    			}
    		}
    		if (mSystem.clientType == 7 && (Rms.loadRMSString("fake") == null || Rms.loadRMSString("fake") == string.Empty))
    		{
    			imgShuriken = loadImage("/mainImage/wait.png");
    		}
    		imgClear = loadImage("/mainImage/myTexture2der.png");
    		img12 = loadImage("/mainImage/12+.png");
    		debugUpdate = new MyVector();
    		debugPaint = new MyVector();
    		debugSession = new MyVector();
    		for (int i = 0; i < 3; i++)
    		{
    			imgBorder[i] = loadImage("/mainImage/myTexture2dbd" + i + ".png");
    		}
    		borderConnerW = mGraphics.getImageWidth(imgBorder[0]);
    		borderConnerH = mGraphics.getImageHeight(imgBorder[0]);
    		borderCenterW = mGraphics.getImageWidth(imgBorder[1]);
    		borderCenterH = mGraphics.getImageHeight(imgBorder[1]);
    		Panel.graphics = Rms.loadRMSInt("lowGraphic");
    		lowGraphic = Rms.loadRMSInt("lowGraphic") == 1;
    		Main.ApplyFrameRatePolicy(Main.isWindowFocused);
    		GameScr.isPaintChatVip = ((Rms.loadRMSInt("serverchat") != 1) ? true : false);
    		if (Rms.loadRMSInt("default_cfg_v1") == -1)
    		{
    			Rms.saveRMSInt("default_cfg_v1", 1);
    			Rms.saveRMSInt("isPaintAura", 1);
    			Rms.saveRMSInt("isPaintAura2", 1);
    			Rms.saveRMSInt("isPlaySound", 1);
    			Char.isPaintAura = true;
    			Char.isPaintAura2 = true;
    			isPlaySound = true;
    		}
    		else
    		{
    			Char.isPaintAura = (Rms.loadRMSInt("isPaintAura") != 0);
    			Char.isPaintAura2 = (Rms.loadRMSInt("isPaintAura2") != 0);
    			isPlaySound = (Rms.loadRMSInt("isPlaySound") != 0);
    		}
    		Res.init();
    		SmallImage.loadBigImage();
    		Panel.WIDTH_PANEL = 182;
    		if (Panel.WIDTH_PANEL > w)
    		{
    			Panel.WIDTH_PANEL = w;
    		}
    		InfoMe.gI().loadCharId();
    		Command.btn0left = loadImage("/mainImage/btn0left.png");
    		Command.btn0mid = loadImage("/mainImage/btn0mid.png");
    		Command.btn0right = loadImage("/mainImage/btn0right.png");
    		Command.btn1left = loadImage("/mainImage/btn1left.png");
    		Command.btn1mid = loadImage("/mainImage/btn1mid.png");
    		Command.btn1right = loadImage("/mainImage/btn1right.png");
    		serverScreen = new ServerListScreen();
    		img12 = loadImage("/mainImage/12+.png");
    		for (int j = 0; j < 7; j++)
    		{
    			imgBlue[j] = loadImage("/effectdata/blue/" + j + ".png");
    			imgViolet[j] = loadImage("/effectdata/violet/" + j + ".png");
    		}
    		ServerListScreen.createDeleteRMS();
    		serverScr = new ServerScr();
    		loginScr = new LoginScr();
    	}
    
    	public static GameCanvas gI()
    	{
    		return instance;
    	}
    
    	public void initPaint()
    	{
    		paintz = new Paint();
    	}
    
    	public static void closeKeyBoard()
    	{
    		mGraphics.addYWhenOpenKeyBoard = 0;
    		timeOpenKeyBoard = 0;
    		Main.closeKeyBoard();
    	}
    
    	public void update()
    	{
    		if (gameTick % 100 == 0)
    		{
    		}
    		if (isRequestMapID == 2 && waitingTimeChangeMap < mSystem.currentTimeMillis() && gameTick % 2 == 0 && currentScreen != null)
    		{
    			if (currentScreen == GameScr.gI())
    			{
    				if (Char.isLoadingMap)
    				{
    					Char.isLoadingMap = false;
    				}
    				if (ServerListScreen.waitToLogin)
    				{
    					ServerListScreen.waitToLogin = false;
    				}
    			}
    			if (SmallImage.vt_images_watingDowload.size() > 0)
    			{
    				Small small = (Small)SmallImage.vt_images_watingDowload.elementAt(0);
    				Service.gI().requestIcon(small.id);
    				SmallImage.vt_images_watingDowload.removeElementAt(0);
    			}
    			if (Effect.dowloadEff.size() <= 0)
    			{
    			}
    		}
    		if (mSystem.currentTimeMillis() > timefps)
    		{
    			timefps += 1000L;
    			max = fps;
    			fps = 0;
    		}
    		fps++;
    		if (messageServer.size() > 0 && thongBaoTest == null)
    		{
    			startserverThongBao((string)messageServer.elementAt(0));
    			messageServer.removeElementAt(0);
    		}
    		if (gameTick % 5 == 0)
    		{
    			timeNow = mSystem.currentTimeMillis();
    		}
    		Res.updateOnScreenDebug();
    		try
    		{
    			if (TouchScreenKeyboard.visible)
    			{
    				timeOpenKeyBoard++;
    				if (timeOpenKeyBoard > ((!Main.isWindowsPhone) ? 10 : 5))
    				{
    					mGraphics.addYWhenOpenKeyBoard = 94;
    				}
    			}
    			else
    			{
    				mGraphics.addYWhenOpenKeyBoard = 0;
    				timeOpenKeyBoard = 0;
    			}
    			long num = mSystem.currentTimeMillis();
    			if (num - timeTickEff1 >= 780 && !isEff1)
    			{
    				timeTickEff1 = num;
    				isEff1 = true;
    			}
    			else
    			{
    				isEff1 = false;
    			}
    			if (num - timeTickEff2 >= 7800 && !isEff2)
    			{
    				timeTickEff2 = num;
    				isEff2 = true;
    			}
    			else
    			{
    				isEff2 = false;
    			}
    			if (taskTick > 0)
    			{
    				taskTick--;
    			}
    			gameTick++;
    			if (gameTick > 10000)
    			{
    				if (mSystem.currentTimeMillis() - lastTimePress > 20000 && currentScreen == loginScr)
    				{
    					GameMidlet.instance.exit();
    				}
    				gameTick = 0;
    			}
                    if (currentScreen != null)
    			{
    				if (currentDialog != null)
    				{
    					debug("B", 0);
    					currentDialog.update();
    				}
    				else if (menu.showMenu)
    				{
    					debug("C", 0);
    					menu.updateMenu();
    					debug("D", 0);
    					menu.updateMenuKey();
    				}
    				else if (panel.isShow)
    				{
    					panel.update();
    					if (isPointer(panel.X, panel.Y, panel.W, panel.H))
    					{
    						isFocusPanel2 = false;
    					}
    					if (panel2 != null && panel2.isShow)
    					{
    						panel2.update();
    						if (isPointer(panel2.X, panel2.Y, panel2.W, panel2.H))
    						{
    							isFocusPanel2 = true;
    						}
    					}
    					if (panel2 != null)
    					{
    						if (isFocusPanel2)
    						{
    							panel2.updateKey();
    						}
    						else
    						{
    							panel.updateKey();
    						}
    					}
    					else
    					{
    						panel.updateKey();
    					}
    					if (panel.chatTField != null && panel.chatTField.isShow)
    					{
    						panel.chatTFUpdateKey();
    					}
    					else if (panel2 != null && panel2.chatTField != null && panel2.chatTField.isShow)
    					{
    						panel2.chatTFUpdateKey();
    					}
    					else if ((isPointer(panel.X, panel.Y, panel.W, panel.H) && panel2 != null) || panel2 == null)
    					{
    						panel.updateKey();
    					}
    					else if (panel2 != null && panel2.isShow && isPointer(panel2.X, panel2.Y, panel2.W, panel2.H))
    					{
    						panel2.updateKey();
    					}
    					if (isPointer(panel.X + panel.W, panel.Y, w - panel.W * 2, panel.H) && isPointerJustRelease && panel.isDoneCombine)
    					{
    						panel.hide();
    					}
    				}
    				else if (ChatPopup.serverChatPopUp != null)
    				{
    					ChatPopup.serverChatPopUp.update();
    					ChatPopup.serverChatPopUp.updateKey();
    				}
    				else if (ChatPopup.currChatPopup != null)
    				{
    					ChatPopup.currChatPopup.update();
    					ChatPopup.currChatPopup.updateKey();
    				}
    				debug("E", 0);
    				if (!isLoading)
    				{
    					currentScreen.update();
    				}
    				debug("F", 0);
    				if (!panel.isShow && ChatPopup.serverChatPopUp == null)
    				{
    					currentScreen.updateKey();
    				}
    				Hint.update();
    				SoundMn.gI().update();
    			}
    			debug("Ix", 0);
    			Timer.update();
    			debug("Hx", 0);
    			InfoDlg.update();
    			debug("G", 0);
    			if (resetToLoginScr)
    			{
    				resetToLoginScr = false;
    				doResetToLoginScr(serverScreen);
    			}
    			debug("Zzz", 0);
    			if ((currentScreen != serverScr || !serverScr.isPaintNewUi) && Controller.isConnectOK)
    			{
    				if (Controller.isMain)
    				{
    					GameMidlet.IP = ServerListScreen.address[ServerListScreen.ipSelect];
    					GameMidlet.PORT = ServerListScreen.port[ServerListScreen.ipSelect];
    					ServerListScreen.testConnect = 2;
    					Rms.saveRMSInt("svselect", ServerListScreen.ipSelect);
    					Rms.saveIP(GameMidlet.IP + ":" + GameMidlet.PORT);
    					Service.gI().setClientType();
    					Service.gI().androidPack();
    				}
    				else
    				{
    					Service.gI().setClientType2();
    					Service.gI().androidPack2();
    				}
    				Controller.isConnectOK = false;
    			}
    			if (Controller.isDisconnected)
    			{
    				Debug.Log("disconnect");
    				if (!Controller.isMain)
    				{
    					if (currentScreen == serverScreen && !Service.reciveFromMainSession)
    					{
    						serverScreen.cancel();
    					}
    					if (currentScreen == loginScr && !Service.reciveFromMainSession)
    					{
    						onDisconnected();
    					}
    				}
    				else
    				{
    					onDisconnected();
    				}
    				Controller.isDisconnected = false;
    			}
    			if (Controller.isConnectionFail)
    			{
    				Debug.Log("connect fail");
    				if (!Controller.isMain)
    				{
    					if (currentScreen == serverScreen && ServerListScreen.isGetData && !Service.reciveFromMainSession)
    					{
    						ServerListScreen.testConnect = 0;
    						serverScreen.cancel();
    					}
    					if (currentScreen == loginScr && !Service.reciveFromMainSession)
    					{
    						onConnectionFail();
    					}
    				}
    				else if (Session_ME.gI().isCompareIPConnect())
    				{
    					onConnectionFail();
    				}
    				Controller.isConnectionFail = false;
    			}
    			if (Main.isResume)
    			{
    				Main.isResume = false;
    				if (currentDialog != null && currentDialog.left != null && currentDialog.left.actionListener != null)
    				{
    					currentDialog.left.performAction();
    				}
    			}
    			if (currentScreen != null && currentScreen is GameScr)
    			{
    				xThongBaoTranslate += dir_ * 2;
    				if (xThongBaoTranslate - Panel.imgNew.getWidth() <= 60)
    				{
    					dir_ = 0;
    					tickWaitThongBao++;
    					if (tickWaitThongBao > 150)
    					{
    						tickWaitThongBao = 0;
    						thongBaoTest = null;
    					}
    				}
    			}
    			if (currentScreen != null && currentScreen.Equals(GameScr.gI()))
    			{
    				if (GameScr.info1 != null)
    				{
    					GameScr.info1.update();
    				}
    				if (GameScr.info2 != null)
    				{
    					GameScr.info2.update();
    				}
    			}
    			isPointerSelect = false;
    		}
    		catch (Exception ex)
    		{
    			Debug.LogWarning("[GameCanvas.update] Error: " + ex.Message + "\n" + ex.StackTrace);
    		}
    	}
    
    	public void onDisconnected()
    	{
    		if (Controller.isConnectionFail)
    		{
    			Controller.isConnectionFail = false;
    		}
    		isResume = true;
    		Session_ME.gI().clearSendingMessage();
    		Session_ME.gI().close();
    		if (Controller.isLoadingData)
    		{
    			instance.resetToLoginScrz();
    			startOK(mResources.pls_restart_game_error, 8885, null);
    			Controller.isDisconnected = false;
    			return;
    		}
    		if (currentScreen != serverScreen)
    		{
    			startOKDlg("Mất kết nối với máy chủ, vui lòng thử lại!");
    		}
    		else
    		{
    			endDlg();
    		}
    		Char.isLoadingMap = false;
    		if (Controller.isMain)
    		{
    			ServerListScreen.testConnect = 0;
    		}
    		instance.resetToLoginScrz();
    		mSystem.endKey();
    	}
    
    	public void onConnectionFail()
    	{
    		if (currentScreen.Equals(SplashScr.instance))
    		{
    			if (ServerListScreen.hasConnected != null)
    			{
    				ServerListScreen.getServerList(ServerListScreen.linkDefault);
    				if (!ServerListScreen.hasConnected[0])
    				{
    					ServerListScreen.hasConnected[0] = true;
    					ServerListScreen.ipSelect = 0;
    					GameMidlet.IP = ServerListScreen.address[ServerListScreen.ipSelect];
    					Rms.saveRMSInt("svselect", ServerListScreen.ipSelect);
    					connect();
    				}
    				else if (!ServerListScreen.hasConnected[2])
    				{
    					ServerListScreen.hasConnected[2] = true;
    					ServerListScreen.ipSelect = 2;
    					GameMidlet.IP = ServerListScreen.address[ServerListScreen.ipSelect];
    					Rms.saveRMSInt("svselect", ServerListScreen.ipSelect);
    					connect();
    				}
    				else
    				{
    					ServerListScreen.hasConnected = null;
    				}
    			}
    			return;
    		}
    		Session_ME.gI().clearSendingMessage();
    		ServerListScreen.isWait = false;
    		if (Controller.isLoadingData)
    		{
    			startOK(mResources.pls_restart_game_error, 8885, null);
    			Controller.isConnectionFail = false;
    			return;
    		}
    		isResume = true;
    		LoginScr.isContinueToLogin = false;
    		if (loginScr != null)
    		{
    			instance.resetToLoginScrz();
    		}
    		else
    		{
    			loginScr = new LoginScr();
    		}
    		LoginScr.serverName = ServerListScreen.nameServer[ServerListScreen.ipSelect];
    		if (currentScreen != serverScreen)
    		{
    			ServerListScreen.countDieConnect = 0;
    		}
    		else
    		{
    			endDlg();
    			ServerListScreen.loadScreen = true;
    			serverScreen.switchToMe();
    		}
    		Char.isLoadingMap = false;
    		if (Controller.isMain)
    		{
    			ServerListScreen.testConnect = 0;
    		}
    		mSystem.endKey();
    	}
    
    	public static bool isWaiting()
    	{
    		if (InfoDlg.isShow || (msgdlg != null && msgdlg.info.Equals(mResources.PLEASEWAIT)) || Char.isLoadingMap || LoginScr.isContinueToLogin)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public static void connect()
    	{
    		Debug.Log(">>connect:" + GameMidlet.IP + ":" + GameMidlet.PORT);
    		if (!Session_ME.gI().isConnected())
    		{
    			Session_ME.gI().connect(GameMidlet.IP, GameMidlet.PORT);
    		}
    	}
    
    	public static void resetTrans(mGraphics g)
    	{
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		g.setClip(0, 0, w, h);
    	}
    
    	public static void resetTransGameScr(mGraphics g)
    	{
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		g.translate(0, 0);
    		g.setClip(0, 0, w, h);
    		g.translate(-GameScr.cmx, -GameScr.cmy);
    	}
    
    	public void initGameCanvas()
    	{
    		debug("SP2i1", 0);
    		w = MotherCanvas.instance.getWidthz();
    		h = MotherCanvas.instance.getHeightz();
    		debug("SP2i2", 0);
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
    		debug("SP2i3", 0);
    		mScreen.initPos();
    		debug("SP2i4", 0);
    		debug("SP2i5", 0);
    		inputDlg = new InputDlg();
    		debug("SP2i6", 0);
    		listPoint = new MyVector();
    		debug("SP2i7", 0);
    	}
    
    	public void start()
    	{
    	}
    
    	public int getWidth()
    	{
    		return (int)ScaleGUI.WIDTH;
    	}
    
    	public int getHeight()
    	{
    		return (int)ScaleGUI.HEIGHT;
    	}
    
    	public static void debug(string s, int type)
    	{
    	}
    
    	public void doResetToLoginScr(mScreen screen)
    	{
    		try
    		{
    			SoundMn.gI().stopAll();
    			LoginScr.isContinueToLogin = false;
    			TileMap.lastType = (TileMap.bgType = 0);
    			Char.clearMyChar();
    			GameScr.clearGameScr();
    			GameScr.resetAllvector();
    			InfoDlg.hide();
    			GameScr.info1.hide();
    			GameScr.info2.hide();
    			GameScr.info2.cmdChat = null;
    			Hint.isShow = false;
    			ChatPopup.currChatPopup = null;
    			ChatPopup.serverChatPopUp = null;
    			Controller.isStopReadMessage = false;
    			GameScr.loadCamera(true, -1, -1);
    			GameScr.cmx = 100;
    			panel.currentTabIndex = 0;
    			panel.selected = (isTouch ? (-1) : 0);
    			panel.init();
    			panel2 = null;
    			GameScr.isPaint = true;
    			ClanMessage.vMessage.removeAllElements();
    			GameScr.textTime.removeAllElements();
    			GameScr.vClan.removeAllElements();
    			GameScr.vFriend.removeAllElements();
    			GameScr.vEnemies.removeAllElements();
    			TileMap.vCurrItem.removeAllElements();
    			BackgroudEffect.vBgEffect.removeAllElements();
    			EffecMn.vEff.removeAllElements();
    			Effect.newEff.removeAllElements();
    			menu.showMenu = false;
    			panel.vItemCombine.removeAllElements();
    			panel.isShow = false;
    			if (panel.tabIcon != null)
    			{
    				panel.tabIcon.isShow = false;
    			}
    			if (mGraphics.zoomLevel == 1)
    			{
    				SmallImage.clearHastable();
    			}
    			Session_ME.gI().close();
    			screen.switchToMe();
    		}
    		catch (Exception ex)
    		{
    			Cout.println("Loi tai doResetToLoginScr " + ex.ToString());
    		}
    		ServerListScreen.isAutoConect = true;
    		ServerListScreen.countDieConnect = 0;
    		ServerListScreen.testConnect = -1;
    		ServerListScreen.loadScreen = true;
    	}
    
    	public static void showErrorForm(int type, string moreInfo)
    	{
    	}
    
    	public static void paintCloud(mGraphics g)
    	{
    	}
    }
}

namespace Game1
{
	using UnityEngine;

	public class SplashScr : mScreen
	{
		public const int MIN_SPLASH_TICKS = 0;

		public static mScreen pendingScreen;

		public static int splashScrStat;

		private bool isCheckConnect;

		public static int nData = -1;

		public static int maxData = -1;

		public static SplashScr instance;

		public static Image imgLogo;

		public long TIMEOUT;

		// Constants thời gian hiệu ứng chuyển cảnh (frames/ticks)
		private const int FADE_IN_TICKS = 15;   // ~0.5s mờ dần bóng đen -> hiện rõ logo
		private const int HOLD_TICKS = 35;      // ~1.1s logo rõ ràng, viên ngọc rồng xoay
		private const int FADE_OUT_TICKS = 15;  // ~0.5s mờ dần logo -> tối đen hoàn toàn
		private const int TOTAL_SPLASH_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS; // 65 ticks (~2.1s)

		private float fadeAlpha = 1.0f;

		private bool isFinished;

		// Mảng chuỗi trạng thái Zero-GC (tránh cấp phát new string trong paint)
		private static readonly string[] loadingTexts = new string[]
		{
			"Đang kiểm tra dữ liệu và kết nối máy chủ",
			"Đang kiểm tra dữ liệu và kết nối máy chủ.",
			"Đang kiểm tra dữ liệu và kết nối máy chủ..",
			"Đang kiểm tra dữ liệu và kết nối máy chủ..."
		};

		public SplashScr()
		{
			instance = this;
		}

		public static void loadSplashScr()
		{
			splashScrStat = 0;
			pendingScreen = null;
			if (instance != null)
			{
				instance.fadeAlpha = 1.0f;
				instance.isFinished = false;
				instance.isCheckConnect = false;
			}
		}

		public override void switchToMe()
		{
			base.switchToMe();
			splashScrStat = 0;
			fadeAlpha = 1.0f;
			isFinished = false;
			isCheckConnect = false;
			if (imgLogo == null)
			{
				loadImg();
			}
		}

		public override void update()
		{
			if (!isCheckConnect)
			{
				isCheckConnect = true;
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
				if (Rms.loadRMSInt("svselect") == -1)
				{
					ServerListScreen.getServerList(ServerListScreen.linkDefault);
				}
				ServerListScreen.loadIP();
				ServerListScreen.loadScreen = true;
				if (GameCanvas.serverScreen == null)
				{
					GameCanvas.serverScreen = new ServerListScreen();
				}
			}

			splashScrStat++;
			ServerListScreen.updateDeleteData();

			// Nếu đang trong chế độ tải dữ liệu gói của server
			if (nData != -1)
			{
				return;
			}

			// Quản lý chuyển cảnh dần (Fade in -> Giữ sáng & tải -> Fade out)
			if (splashScrStat <= FADE_IN_TICKS)
			{
				fadeAlpha = 1.0f - ((float)splashScrStat / (float)FADE_IN_TICKS);
				if (fadeAlpha < 0f) fadeAlpha = 0f;
			}
			else if (splashScrStat <= FADE_IN_TICKS + HOLD_TICKS)
			{
				fadeAlpha = 0f;
			}
			else if (splashScrStat <= TOTAL_SPLASH_TICKS)
			{
				fadeAlpha = (float)(splashScrStat - (FADE_IN_TICKS + HOLD_TICKS)) / (float)FADE_OUT_TICKS;
				if (fadeAlpha > 1.0f) fadeAlpha = 1.0f;
			}
			else
			{
				if (!isFinished)
				{
					isFinished = true;
					fadeAlpha = 1.0f;
					if (pendingScreen != null)
					{
						mScreen next = pendingScreen;
						pendingScreen = null;
						next.switchToMe();
						return;
					}
					if (GameCanvas.serverScreen == null)
					{
						GameCanvas.serverScreen = new ServerListScreen();
					}
					ServerListScreen.startTransitionIn();
					GameCanvas.serverScreen.switchToMe();
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
			// Chế độ nạp data gói của game
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

				int dBarW = 190;
				int dBarH = 11;
				int dBarX = GameCanvas.w / 2 - dBarW / 2;
				int dBarY = GameCanvas.h / 2 + 6;

				g.setColor(0x4a1403);
				g.fillRect(dBarX, dBarY, dBarW, dBarH, 4);
				g.setColor(0x8c2b06);
				g.drawRect(dBarX, dBarY, dBarW, dBarH);

				int dFillW = (int)((float)dBarW * (float)percent / 100f);
				if (dFillW > dBarW) dFillW = dBarW;
				if (dFillW > 0)
				{
					g.setColor(0x00d2d3);
					g.fillRect(dBarX, dBarY, dFillW, dBarH, 4);
					g.setColor(0xffffff);
					g.fillRect(dBarX, dBarY + 1, dFillW, 2);
				}
				return;
			}

			// 1. Nền đen hoàn toàn (tương ứng nền ảnh logo Blue Dragon Studio)
			g.setColor(0x000000);
			g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

			// 2. Logo Blue Dragon Studio ở trung tâm màn hình
			if (imgLogo == null)
			{
				loadImg();
			}
			int logoY = GameCanvas.hh - 24;
			if (imgLogo != null)
			{
				g.drawImage(imgLogo, GameCanvas.hw, logoY, 3);
			}

			// 3. Hiệu ứng viên ngọc rồng xoay tròn huyền thoại NRO
			if (GameCanvas.imgShuriken == null)
			{
				GameCanvas.imgShuriken = GameCanvas.loadImage("/mainImage/myTexture2df.png");
			}
			int logoH = (imgLogo != null) ? imgLogo.getHeight() : 80;
			int ballY = logoY + (logoH / 2) + 20;
			GameCanvas.paintShukiren(GameCanvas.hw, ballY, g);

			// 4. Dòng chữ trạng thái tiến trình Zero-GC
			int textY = ballY + 16;
			string statusText = loadingTexts[(splashScrStat / 8) % 4];
			mFont.tahoma_7_grey.drawString(g, statusText, GameCanvas.hw + 1, textY + 1, 2);
			mFont.tahoma_7b_white.drawString(g, statusText, GameCanvas.hw, textY, 2);

			// 5. Nút Xóa dữ liệu (góc dưới bên phải)
			if (ServerListScreen.cmdDeleteRMS != null)
			{
				int btnW = 76;
				int btnH = 22;
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
				mFont.tahoma_7b_dark.drawString(g, mResources.xoadulieu, btnX + btnW / 2, btnY + 5, 2);
			}

			// 6. Lớp che mờ chuyển cảnh Fade In & Fade Out
			if (fadeAlpha > 0.01f)
			{
				float quantizedAlpha = Mathf.Clamp01(Mathf.Round(fadeAlpha * 25f) / 25f);
				if (quantizedAlpha > 0.01f)
				{
					g.setColor(0x000000, quantizedAlpha);
					g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
				}
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

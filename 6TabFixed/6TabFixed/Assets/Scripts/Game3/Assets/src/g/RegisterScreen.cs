using System;

namespace Game3.Assets.src.g
{
	public class RegisterScreen : mScreen, IActionListener
	{
		public TField tfUser;
		public TField tfPass;
		public TField tfRePass;

		public static bool isContinueToLogin = false;
		private int focus;
		private int wC;
		private int yL;
		private int defYL;

		private Command cmdOK;
		private Command cmdExit;

		private int xLog;
		private int yLog;
		private int wP;
		private int hP;

		public static Image imgTitle;

		public RegisterScreen(sbyte haveName)
		{
			TileMap.bgID = (sbyte)(mSystem.currentTimeMillis() % 9);
			if (TileMap.bgID == 5 || TileMap.bgID == 6)
			{
				TileMap.bgID = 4;
			}
			GameScr.loadCamera(true, -1, -1);
			GameScr.cmx = 100;
			GameScr.cmy = 200;
			if (GameCanvas.h > 200)
			{
				defYL = GameCanvas.hh - 80;
			}
			else
			{
				defYL = GameCanvas.hh - 65;
			}
			resetLogo();
			wC = ((GameCanvas.w < 220) ? 150 : 180);

			wP = wC + 40;
			hP = 160;
			xLog = (GameCanvas.w - wP) / 2;
			yLog = (GameCanvas.h - hP) / 2;

			tfUser = new TField();
			tfUser.width = wC;
			tfUser.height = mScreen.ITEM_HEIGHT + 2;
			tfUser.isFocus = true;
			tfUser.name = "Tên tài khoản";
			tfUser.setIputType(TField.INPUT_TYPE_ANY);

			tfPass = new TField();
			tfPass.width = wC;
			tfPass.height = mScreen.ITEM_HEIGHT + 2;
			tfPass.name = "Mật khẩu";
			tfPass.setIputType(TField.INPUT_TYPE_PASSWORD);

			tfRePass = new TField();
			tfRePass.width = wC;
			tfRePass.height = mScreen.ITEM_HEIGHT + 2;
			tfRePass.name = "Nhập lại MK";
			tfRePass.setIputType(TField.INPUT_TYPE_PASSWORD);

			focus = 0;

			cmdOK = new Command(mResources.OK, this, 2008, null);
			cmdExit = new Command("Thoát", this, 1003, null);

			cmdOK.x = xLog + 15;
			cmdOK.y = yLog + hP - 30;
			cmdExit.x = xLog + wP - 85;
			cmdExit.y = yLog + hP - 30;

			if (GameCanvas.w < 250)
			{
				cmdOK.x = GameCanvas.w / 2 - 80;
				cmdExit.x = GameCanvas.w / 2 + 10;
				cmdExit.y = (cmdOK.y = GameCanvas.h - 25);
			}

			center = cmdOK;
			left = cmdExit;
		}

		public new void switchToMe()
		{
			Res.outz("Res switch");
			SoundMn.gI().stopAll();
			focus = 0;
			processFocus();
			base.switchToMe();
		}

		public override void update()
		{
			tfUser.update();
			tfPass.update();
			tfRePass.update();

			for (int i = 0; i < Effect2.vEffect2.size(); i++)
			{
				Effect2 effect = (Effect2)Effect2.vEffect2.elementAt(i);
				effect.update();
			}

			GameScr.cmx++;
			if (GameScr.cmx > GameCanvas.w * 3 + 100)
			{
				GameScr.cmx = 100;
			}
			if (ChatPopup.currChatPopup != null)
			{
				return;
			}
			updateLogo();
			center = cmdOK;
			left = cmdExit;
		}

		public void updateLogo()
		{
			if (defYL != yL)
			{
				yL += defYL - yL >> 1;
			}
		}

		public void resetLogo()
		{
			yL = -50;
		}

		public override void keyPress(int keyCode)
		{
			if (tfUser.isFocus)
			{
				tfUser.keyPressed(keyCode);
			}
			else if (tfPass.isFocus)
			{
				tfPass.keyPressed(keyCode);
			}
			else if (tfRePass.isFocus)
			{
				tfRePass.keyPressed(keyCode);
			}
			base.keyPress(keyCode);
		}

		public override void paint(mGraphics g)
		{
			GameCanvas.paintBGGameScr(g);
			if (ChatPopup.currChatPopup != null || ChatPopup.serverChatPopUp != null)
			{
				return;
			}
			if (GameCanvas.currentDialog == null)
			{
				wP = wC + 40;
				hP = 160;
				xLog = (GameCanvas.w - wP) / 2;
				yLog = (GameCanvas.h - hP) / 2;

				PopUp.paintPopUp(g, xLog, yLog, wP, hP, -1, true);

				tfUser.x = xLog + 20;
				tfUser.y = yLog + 20;
				tfPass.x = xLog + 20;
				tfPass.y = tfUser.y + 35;
				tfRePass.x = xLog + 20;
				tfRePass.y = tfPass.y + 35;

				cmdOK.x = xLog + 15;
				cmdOK.y = yLog + hP - 30;
				cmdExit.x = xLog + wP - 85;
				cmdExit.y = yLog + hP - 30;

				tfUser.paint(g);
				tfPass.paint(g);
				tfRePass.paint(g);
			}
			string vERSION = GameMidlet.VERSION;
			g.setColor(GameCanvas.skyColor);
			g.fillRect(GameCanvas.w - 40, 4, 36, 11);
			mFont.tahoma_7_grey.drawString(g, vERSION, GameCanvas.w - 22, 4, mFont.CENTER);
			GameCanvas.resetTrans(g);
			base.paint(g);
		}

		private void turnOffFocus()
		{
			tfUser.isFocus = false;
			tfPass.isFocus = false;
			tfRePass.isFocus = false;
		}

		private void processFocus()
		{
			turnOffFocus();
			switch (focus)
			{
			case 0:
				tfUser.isFocus = true;
				break;
			case 1:
				tfPass.isFocus = true;
				break;
			case 2:
				tfRePass.isFocus = true;
				break;
			}
		}

		public override void updateKey()
		{
			if (isContinueToLogin)
			{
				return;
			}
			if (GameCanvas.keyPressed[21])
			{
				focus--;
				if (focus < 0)
				{
					focus = 2;
				}
				processFocus();
			}
			else if (GameCanvas.keyPressed[22])
			{
				focus++;
				if (focus > 2)
				{
					focus = 0;
				}
				processFocus();
			}
			if (GameCanvas.keyPressed[21] || GameCanvas.keyPressed[22])
			{
				GameCanvas.clearKeyPressed();
			}
			if (GameCanvas.isPointerJustRelease)
			{
				if (GameCanvas.isPointerHoldIn(tfUser.x, tfUser.y, tfUser.width, tfUser.height))
				{
					focus = 0;
					processFocus();
				}
				else if (GameCanvas.isPointerHoldIn(tfPass.x, tfPass.y, tfPass.width, tfPass.height))
				{
					focus = 1;
					processFocus();
				}
				else if (GameCanvas.isPointerHoldIn(tfRePass.x, tfRePass.y, tfRePass.width, tfRePass.height))
				{
					focus = 2;
					processFocus();
				}
			}
			base.updateKey();
			GameCanvas.clearKeyPressed();
		}

		private bool isValidAccount(string text)
		{
			if (string.IsNullOrEmpty(text))
			{
				return false;
			}
			for (int i = 0; i < text.Length; i++)
			{
				char c = text[i];
				if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')))
				{
					return false;
				}
			}
			return true;
		}

		private bool isValidPassword(string text)
		{
			if (string.IsNullOrEmpty(text))
			{
				return false;
			}
			for (int i = 0; i < text.Length; i++)
			{
				char c = text[i];
				if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')))
				{
					return false;
				}
			}
			return true;
		}

		protected void doRegister()
		{
			string user = tfUser.getText().Trim();
			string pass = tfPass.getText().Trim();
			string rePass = tfRePass.getText().Trim();

			if (string.IsNullOrEmpty(user))
			{
				GameCanvas.startOKDlg("Vui lòng nhập tên tài khoản!");
				focus = 0;
				processFocus();
				return;
			}
			if (user.Length < 5)
			{
				GameCanvas.startOKDlg("Tên tài khoản phải có ít nhất 5 ký tự!");
				focus = 0;
				processFocus();
				return;
			}
			if (user.Length > 18)
			{
				GameCanvas.startOKDlg("Tên tài khoản không được vượt quá 18 ký tự!");
				focus = 0;
				processFocus();
				return;
			}
			if (!isValidAccount(user))
			{
				GameCanvas.startOKDlg("Tài khoản chỉ được gồm chữ cái và số (không dấu, không khoảng trắng)!");
				focus = 0;
				processFocus();
				return;
			}

			if (string.IsNullOrEmpty(pass))
			{
				GameCanvas.startOKDlg("Vui lòng nhập mật khẩu!");
				focus = 1;
				processFocus();
				return;
			}
			if (pass.Length < 5)
			{
				GameCanvas.startOKDlg("Mật khẩu phải có ít nhất 5 ký tự!");
				focus = 1;
				processFocus();
				return;
			}
			if (pass.Length > 18)
			{
				GameCanvas.startOKDlg("Mật khẩu không được vượt quá 18 ký tự!");
				focus = 1;
				processFocus();
				return;
			}
			if (!isValidPassword(pass))
			{
				GameCanvas.startOKDlg("Mật khẩu chỉ được gồm chữ cái và số!");
				focus = 1;
				processFocus();
				return;
			}

			if (string.IsNullOrEmpty(rePass))
			{
				GameCanvas.startOKDlg("Vui lòng nhập lại mật khẩu!");
				focus = 2;
				processFocus();
				return;
			}
			if (!pass.Equals(rePass))
			{
				GameCanvas.startOKDlg("Mật khẩu nhập lại không trùng khớp!");
				focus = 2;
				processFocus();
				return;
			}

			Rms.saveRMSString("acc", user);
			Rms.saveRMSPassword("pass", pass);
			GameCanvas.startWaitDlg(mResources.PLEASEWAIT);
			Service.gI().charInfo(user, pass);
		}

		public void perform(int idAction, object p)
		{
			switch (idAction)
			{
			case 1003:
				Session_ME.gI().close();
				GameCanvas.serverScreen.switchToMe();
				break;
			case 2008:
				doRegister();
				break;
			}
		}
	}
}

namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameCanvas : IActionListener
    {
    
    	public void perform(int idAction, object p)
    	{
    		switch (idAction)
    		{
    		case 9000:
    			endDlg();
    			SplashScr.imgLogo = null;
    			SmallImage.loadBigRMS();
    			mSystem.gcc();
    			ServerListScreen.bigOk = true;
    			ServerListScreen.loadScreen = true;
    			GameScr.gI().loadGameScr();
    			if (currentScreen != loginScr)
    			{
    				serverScreen.switchToMe2();
    			}
    			break;
    		case 999:
    			mSystem.closeBanner();
    			endDlg();
    			break;
    		case 888396:
    			endDlg();
    			break;
    		case 888397:
    		{
    			string text5 = (string)p;
    			break;
    		}
    		case 9999:
    			endDlg();
    			connect();
    			Service.gI().setClientType();
    			if (loginScr == null)
    			{
    				loginScr = new LoginScr();
    			}
    			loginScr.doLogin();
    			break;
    		case 8881:
    		{
    			string url = (string)p;
    			try
    			{
    				GameMidlet.instance.platformRequest(url);
    			}
    			catch (Exception catchErr_1)
    			{
    			    Cout.LogWarning("[GameCanvas.Part3.cs] " + catchErr_1.Message);
    			}
    			currentDialog = null;
    			break;
    		}
    		case 8882:
    			InfoDlg.hide();
    			currentDialog = null;
    			ServerListScreen.isAutoConect = false;
    			ServerListScreen.countDieConnect = 0;
    			if (currentScreen == registerScr)
    			{
    				serverScreen.switchToMe();
    			}
    			break;
    		case 8884:
    			endDlg();
    			loginScr.switchToMe();
    			break;
    		case 8885:
    			GameMidlet.instance.exit();
    			break;
    		case 8886:
    		{
    			endDlg();
    			string name = (string)p;
    			Service.gI().addFriend(name);
    			break;
    		}
    		case 8887:
    		{
    			endDlg();
    			int charId = (int)p;
    			Service.gI().addPartyAccept(charId);
    			break;
    		}
    		case 8888:
    		{
    			int charId2 = (int)p;
    			Service.gI().addPartyCancel(charId2);
    			endDlg();
    			break;
    		}
    		case 8889:
    		{
    			string str = (string)p;
    			endDlg();
    			Service.gI().acceptPleaseParty(str);
    			break;
    		}
    		case 88810:
    		{
    			int playerMapId = (int)p;
    			endDlg();
    			Service.gI().acceptInviteTrade(playerMapId);
    			break;
    		}
    		case 88811:
    			endDlg();
    			Service.gI().cancelInviteTrade();
    			break;
    		case 88814:
    		{
    			Item[] items = (Item[])p;
    			endDlg();
    			Service.gI().crystalCollectLock(items);
    			break;
    		}
    		case 88817:
    			ChatPopup.addChatPopup(string.Empty, 1, Char.myCharz().npcFocus);
    			Service.gI().menu(Char.myCharz().npcFocus.template.npcTemplateId, menu.menuSelectedItem, 0);
    			break;
    		case 88818:
    		{
    			short menuId2 = (short)p;
    			Service.gI().textBoxId(menuId2, inputDlg.tfInput.getText());
    			endDlg();
    			break;
    		}
    		case 88819:
    		{
    			short menuId = (short)p;
    			Service.gI().menuId(menuId);
    			break;
    		}
    		case 88820:
    		{
    			string[] array = (string[])p;
    			if (Char.myCharz().npcFocus == null)
    			{
    				break;
    			}
    			int menuSelectedItem = menu.menuSelectedItem;
    			if (array.Length > 1)
    			{
    				MyVector myVector = new MyVector();
    				for (int i = 0; i < array.Length - 1; i++)
    				{
    					myVector.addElement(new Command(array[i + 1], instance, 88821, menuSelectedItem));
    				}
    				menu.startAt(myVector, 3);
    			}
    			else
    			{
    				ChatPopup.addChatPopup(string.Empty, 1, Char.myCharz().npcFocus);
    				Service.gI().menu(Char.myCharz().npcFocus.template.npcTemplateId, menuSelectedItem, 0);
    			}
    			break;
    		}
    		case 88821:
    		{
    			int menuId3 = (int)p;
    			ChatPopup.addChatPopup(string.Empty, 1, Char.myCharz().npcFocus);
    			Service.gI().menu(Char.myCharz().npcFocus.template.npcTemplateId, menuId3, menu.menuSelectedItem);
    			break;
    		}
    		case 88822:
    			ChatPopup.addChatPopup(string.Empty, 1, Char.myCharz().npcFocus);
    			Service.gI().menu(Char.myCharz().npcFocus.template.npcTemplateId, menu.menuSelectedItem, 0);
    			break;
    		case 88823:
    			startOKDlg(mResources.SENTMSG);
    			break;
    		case 88824:
    			startOKDlg(mResources.NOSENDMSG);
    			break;
    		case 88825:
    			startOKDlg(mResources.sendMsgSuccess, false);
    			break;
    		case 88826:
    			startOKDlg(mResources.cannotSendMsg, false);
    			break;
    		case 88827:
    			startOKDlg(mResources.sendGuessMsgSuccess);
    			break;
    		case 88828:
    			startOKDlg(mResources.sendMsgFail);
    			break;
    		case 88829:
    		{
    			string text4 = inputDlg.tfInput.getText().Trim();
    			if (!text4.Equals(string.Empty))
    			{
    				Service.gI().changeName(text4, (int)p);
    				InfoDlg.showWait();
    			}
    			else
    			{
    				startOKDlg("Tên nhân vật không được để trống!");
    			}
    			break;
    		}
    		case 88836:
    			inputDlg.tfInput.setMaxTextLenght(6);
    			inputDlg.show(mResources.INPUT_PRIVATE_PASS, new Command(mResources.ACCEPT, instance, 888361, null), TField.INPUT_TYPE_NUMERIC);
    			break;
    		case 888361:
    		{
    			string text3 = inputDlg.tfInput.getText();
    			endDlg();
    			if (text3.Length < 6 || text3.Equals(string.Empty))
    			{
    				startOKDlg(mResources.ALERT_PRIVATE_PASS_1);
    				break;
    			}
    			try
    			{
    				Service.gI().activeAccProtect(int.Parse(text3));
    				break;
    			}
    			catch (Exception ex3)
    			{
    				startOKDlg(mResources.ALERT_PRIVATE_PASS_2);
    				Cout.println("Loi tai 888361 Gamescavas " + ex3.ToString());
    				break;
    			}
    		}
    		case 88837:
    		{
    			string text2 = inputDlg.tfInput.getText();
    			endDlg();
    			try
    			{
    				Service.gI().openLockAccProtect(int.Parse(text2.Trim()));
    				break;
    			}
    			catch (Exception ex2)
    			{
    				Cout.println("Loi tai 88837 " + ex2.ToString());
    				break;
    			}
    		}
    		case 88839:
    		{
    			string text = inputDlg.tfInput.getText();
    			endDlg();
    			if (text.Length < 6 || text.Equals(string.Empty))
    			{
    				startOKDlg(mResources.ALERT_PRIVATE_PASS_1);
    				break;
    			}
    			try
    			{
    				startYesNoDlg(mResources.cancelAccountProtection, 888391, text, 8882, null);
    				break;
    			}
    			catch (Exception)
    			{
    				startOKDlg(mResources.ALERT_PRIVATE_PASS_2);
    				break;
    			}
    		}
    		case 888391:
    		{
    			string s = (string)p;
    			endDlg();
    			Service.gI().clearAccProtect(int.Parse(s));
    			break;
    		}
    		case 888392:
    			Service.gI().menu(4, menu.menuSelectedItem, 0);
    			break;
    		case 888393:
    			if (loginScr == null)
    			{
    				loginScr = new LoginScr();
    			}
    			loginScr.doLogin();
    			Main.closeKeyBoard();
    			break;
    		case 888394:
    			endDlg();
    			break;
    		case 888395:
    			endDlg();
    			break;
    		case 101023:
    			Main.numberQuit = 0;
    			break;
    		case 101024:
    			Res.outz("output 101024");
    			endDlg();
    			break;
    		case 101025:
    			endDlg();
    			if (ServerListScreen.loadScreen)
    			{
    				serverScreen.switchToMe();
    			}
    			else
    			{
    				serverScreen.show2();
    			}
    			break;
    		case 101026:
    			mSystem.onDisconnected();
    			break;
    		case 100001:
    			Service.gI().getFlag(0, -1);
    			InfoDlg.showWait();
    			break;
    		case 100002:
    			if (loginScr == null)
    			{
    				loginScr = new LoginScr();
    			}
    			loginScr.backToRegister();
    			break;
    		case 100005:
    			if (Char.myCharz().statusMe == 14)
    			{
    				startOKDlg(mResources.can_not_do_when_die);
    			}
    			else
    			{
    				Service.gI().openUIZone();
    			}
    			break;
    		case 100006:
    			mSystem.onDisconnected();
    			break;
    		case 100016:
    		{
    			sbyte b = 17;
    			ServerListScreen.ipSelect = b;
    			instance.doResetToLoginScr(serverScreen);
    			ServerListScreen.waitToLogin = true;
    			endDlg();
    			break;
    		}
    		}
    	}
    
    	public static void clearAllPointerEvent()
    	{
    		isPointerClick = false;
    		isPointerDown = false;
    		isPointerJustDown = false;
    		isPointerJustRelease = false;
    		isPointerSelect = false;
    		GameScr.gI().lastSingleClick = 0L;
    		GameScr.gI().isPointerDowning = false;
    	}
    
    	public static void backToRegister()
    	{
    	}
    }
}

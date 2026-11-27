namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameScr : mScreen, IChatable
    {
    
    	public override void updateKey()
    	{
    		if (Controller.isStopReadMessage || Char.myCharz().isTeleport || Char.myCharz().isPaintNewSkill || InfoDlg.isLock)
    		{
    			return;
    		}
    		if (GameCanvas.isTouch && !ChatTextField.gI().isShow && !GameCanvas.menu.showMenu)
    		{
    			updateKeyTouchControl();
                ClientManager.getInstance().UpdateTouch();
            }
    		checkAuto();
    		GameCanvas.debug("F2", 0);
    		if (ChatPopup.currChatPopup != null)
    		{
    			Command cmdNextLine = ChatPopup.currChatPopup.cmdNextLine;
    			if ((GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(cmdNextLine)) && cmdNextLine != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				mScreen.keyTouch = -1;
    				if (cmdNextLine != null)
    				{
    					cmdNextLine.performAction();
    				}
    			}
    		}
    		else if (!ChatTextField.gI().isShow)
    		{
    			if ((GameCanvas.keyPressed[12] || mScreen.getCmdPointerLast(GameCanvas.currentScreen.left)) && left != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.isPointerClick = false;
    				GameCanvas.keyPressed[12] = false;
    				mScreen.keyTouch = -1;
    				if (left != null)
    				{
    					left.performAction();
    				}
    			}
    			if ((GameCanvas.keyPressed[13] || mScreen.getCmdPointerLast(GameCanvas.currentScreen.right)) && right != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.isPointerClick = false;
    				GameCanvas.keyPressed[13] = false;
    				mScreen.keyTouch = -1;
    				if (right != null)
    				{
    					right.performAction();
    				}
    			}
    			if ((GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(GameCanvas.currentScreen.center)) && center != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				mScreen.keyTouch = -1;
    				if (center != null)
    				{
    					center.performAction();
    				}
    			}
    		}
    		else
    		{
    			if (ChatTextField.gI().left != null && (GameCanvas.keyPressed[12] || mScreen.getCmdPointerLast(ChatTextField.gI().left)) && ChatTextField.gI().left != null)
    			{
    				ChatTextField.gI().left.performAction();
    			}
    			if (ChatTextField.gI().right != null && (GameCanvas.keyPressed[13] || mScreen.getCmdPointerLast(ChatTextField.gI().right)) && ChatTextField.gI().right != null)
    			{
    				ChatTextField.gI().right.performAction();
    			}
    			if (ChatTextField.gI().center != null && (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(ChatTextField.gI().center)) && ChatTextField.gI().center != null)
    			{
    				ChatTextField.gI().center.performAction();
    			}
    		}
    		GameCanvas.debug("F6", 0);
    		updateKeyAlert();
    		GameCanvas.debug("F7", 0);
    		if (Char.myCharz().currentMovePoint != null)
    		{
    			for (int i = 0; i < GameCanvas.keyPressed.Length; i++)
    			{
    				if (GameCanvas.keyPressed[i])
    				{
    					Char.myCharz().currentMovePoint = null;
    					break;
    				}
    			}
    		}
    		GameCanvas.debug("F8", 0);
    		if (ChatTextField.gI().isShow && GameCanvas.keyAsciiPress != 0)
    		{
    			ChatTextField.gI().keyPressed(GameCanvas.keyAsciiPress);
    			GameCanvas.keyAsciiPress = 0;
    		}
    		else if (isLockKey)
    		{
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    		}
    		else
    		{
    			if (GameCanvas.menu.showMenu || isOpenUI() || Char.isLockKey)
    			{
    				return;
    			}
    			if (GameCanvas.keyPressed[10])
    			{
    				GameCanvas.keyPressed[10] = false;
    				doUseHP();
    				GameCanvas.clearKeyPressed();
    			}
    			if (GameCanvas.keyPressed[11] && mobCapcha == null)
    			{
    				if (popUpYesNo != null)
    				{
    					popUpYesNo.cmdYes.performAction();
    				}
    				else if (info2.info.info != null && info2.info.info.charInfo != null)
    				{
    					GameCanvas.panel.setTypeMessage();
    					GameCanvas.panel.show();
    				}
    				GameCanvas.keyPressed[11] = false;
    				GameCanvas.clearKeyPressed();
    			}
    			if (GameCanvas.keyAsciiPress != 0 && TField.isQwerty && GameCanvas.keyAsciiPress == 32)
    			{
    				doUseHP();
    				GameCanvas.keyAsciiPress = 0;
    				GameCanvas.clearKeyPressed();
    			}
    			if (GameCanvas.keyAsciiPress != 0 && mobCapcha == null && TField.isQwerty && GameCanvas.keyAsciiPress == 121)
    			{
    				if (popUpYesNo != null)
    				{
    					popUpYesNo.cmdYes.performAction();
    					GameCanvas.keyAsciiPress = 0;
    					GameCanvas.clearKeyPressed();
    				}
    				else if (info2.info.info != null && info2.info.info.charInfo != null)
    				{
    					GameCanvas.panel.setTypeMessage();
    					GameCanvas.panel.show();
    					GameCanvas.keyAsciiPress = 0;
    					GameCanvas.clearKeyPressed();
    				}
    			}
    			if (GameCanvas.keyPressed[10] && mobCapcha == null)
    			{
    				GameCanvas.keyPressed[10] = false;
    				info2.doClick(10);
    				GameCanvas.clearKeyPressed();
    			}
    			checkDrag();
    			if (!Char.myCharz().isFlyAndCharge)
    			{
    				checkClick();
    			}
    			if (Char.myCharz().cmdMenu != null && Char.myCharz().cmdMenu.isPointerPressInside())
    			{
    				Char.myCharz().cmdMenu.performAction();
    			}
    			if (Char.myCharz().skillPaint != null)
    			{
    				return;
    			}
    			if (GameCanvas.keyAsciiPress != 0)
    			{
    				if (mobCapcha == null)
    				{
    					if (TField.isQwerty)
    					{
    						if (GameCanvas.keyAsciiPress == 49 || GameCanvas.keyPressed[1])
    						{
    							if (keySkill[0] != null)
    							{
    								doSelectSkill(keySkill[0], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 50 || GameCanvas.keyPressed[2])
    						{
    							if (keySkill[1] != null)
    							{
    								doSelectSkill(keySkill[1], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 51 || GameCanvas.keyPressed[3])
    						{
    							if (keySkill[2] != null)
    							{
    								doSelectSkill(keySkill[2], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 52 || GameCanvas.keyPressed[4])
    						{
    							if (keySkill[3] != null)
    							{
    								doSelectSkill(keySkill[3], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 53 || GameCanvas.keyPressed[5])
    						{
    							if (keySkill[4] != null)
    							{
    								doSelectSkill(keySkill[4], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 54 || GameCanvas.keyPressed[6])
    						{
    							if (keySkill[5] != null)
    							{
    								doSelectSkill(keySkill[5], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 55 || GameCanvas.keyPressed[7])
    						{
    							if (keySkill[6] != null)
    							{
    								doSelectSkill(keySkill[6], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 56 || GameCanvas.keyPressed[8])
    						{
    							if (keySkill[7] != null)
    							{
    								doSelectSkill(keySkill[7], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 57 || GameCanvas.keyPressed[9])
    						{
    							if (keySkill[8] != null)
    							{
    								doSelectSkill(keySkill[8], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 48 || GameCanvas.keyPressed[0])
    						{
    							if (keySkill[9] != null)
    							{
    								doSelectSkill(keySkill[9], true);
    							}
    							GameCanvas.keyAsciiPress = 0;
    						}
    						else if (GameCanvas.keyAsciiPress == 114)
    						{
    							ChatTextField.gI().startChat(this, string.Empty);
    							GameCanvas.keyAsciiPress = 0;
    						}
                            // ClientManager.getInstance().KeyPressed(GameCanvas.keyAsciiPress);
                        }
    					else if (!GameCanvas.isMoveNumberPad)
    					{
    						ChatTextField.gI().startChat(GameCanvas.keyAsciiPress, this, string.Empty);
    					}
    					else if (GameCanvas.keyAsciiPress == 55)
    					{
    						if (keySkill[0] != null)
    						{
    							doSelectSkill(keySkill[0], true);
    						}
    					}
    					else if (GameCanvas.keyAsciiPress == 56)
    					{
    						if (keySkill[1] != null)
    						{
    							doSelectSkill(keySkill[1], true);
    						}
    					}
    					else if (GameCanvas.keyAsciiPress == 57)
    					{
    						if (keySkill[2] != null)
    						{
    							doSelectSkill(keySkill[2], true);
    						}
    					}
    					else if (GameCanvas.keyAsciiPress == 48)
    					{
    						ChatTextField.gI().startChat(this, string.Empty);
    					}
    				}
    				else
    				{
    					char[] array = keyInput.ToCharArray();
    					MyVector myVector = new MyVector();
    					for (int j = 0; j < array.Length; j++)
    					{
    						myVector.addElement(array[j] + string.Empty);
    					}
    					myVector.removeElementAt(0);
    					string text = (char)GameCanvas.keyAsciiPress + string.Empty;
    					if (text.Equals(string.Empty) || text == null || text.Equals("\n"))
    					{
    						text = "-";
    					}
    					myVector.insertElementAt(text, myVector.size());
    					keyInput = string.Empty;
    					for (int k = 0; k < myVector.size(); k++)
    					{
    						keyInput += ((string)myVector.elementAt(k)).ToUpper();
    					}
    					Service.gI().mobCapcha((char)GameCanvas.keyAsciiPress);
    				}
    				GameCanvas.keyAsciiPress = 0;
    			}
    			if (Char.myCharz().statusMe == 1)
    			{
    				GameCanvas.debug("F10", 0);
    				if (!doSeleckSkillFlag)
    				{
    					if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    					{
    						GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    						doFire(false, false);
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
    					{
    						if (!Char.myCharz().isLockMove)
    						{
    							setCharJump(0);
    						}
    					}
    					else if (GameCanvas.keyHold[1] && mobCapcha == null)
    					{
    						if (!Main.isPC)
    						{
    							Char.myCharz().cdir = -1;
    							if (!Char.myCharz().isLockMove)
    							{
    								setCharJump(-4);
    							}
    						}
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 5 : 25] && mobCapcha == null)
    					{
    						if (!Main.isPC)
    						{
    							Char.myCharz().cdir = 1;
    							if (!Char.myCharz().isLockMove)
    							{
    								setCharJump(4);
    							}
    						}
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    					{
    						isAutoPlay = false;
    						Char.myCharz().isAttack = false;
    						if (Char.myCharz().cdir == 1)
    						{
    							Char.myCharz().cdir = -1;
    						}
    						else if (!Char.myCharz().isLockMove)
    						{
    							if (Char.myCharz().cx - Char.myCharz().cxSend != 0)
    							{
    								Service.gI().charMove();
    							}
    							Char.myCharz().statusMe = 2;
    							Char.myCharz().cvx = -Char.myCharz().cspeed;
    						}
    						Char.myCharz().holder = false;
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    					{
    						isAutoPlay = false;
    						Char.myCharz().isAttack = false;
    						if (Char.myCharz().cdir == -1)
    						{
    							Char.myCharz().cdir = 1;
    						}
    						else if (!Char.myCharz().isLockMove)
    						{
    							if (Char.myCharz().cx - Char.myCharz().cxSend != 0)
    							{
    								Service.gI().charMove();
    							}
    							Char.myCharz().statusMe = 2;
    							Char.myCharz().cvx = Char.myCharz().cspeed;
    						}
    						Char.myCharz().holder = false;
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 2)
    			{
    				GameCanvas.debug("F11", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
    				{
    					if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    					{
    						Service.gI().charMove();
    					}
    					Char.myCharz().cvy = -10;
    					Char.myCharz().statusMe = 3;
    					Char.myCharz().cp1 = 0;
    				}
    				else if (GameCanvas.keyHold[1] && mobCapcha == null)
    				{
    					if (Main.isPC)
    					{
    						if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    						{
    							Service.gI().charMove();
    						}
    						Char.myCharz().cdir = -1;
    						Char.myCharz().cvy = -10;
    						Char.myCharz().cvx = -4;
    						Char.myCharz().statusMe = 3;
    						Char.myCharz().cp1 = 0;
    					}
    				}
    				else if (GameCanvas.keyHold[3] && mobCapcha == null)
    				{
    					if (!Main.isPC)
    					{
    						if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    						{
    							Service.gI().charMove();
    						}
    						Char.myCharz().cdir = 1;
    						Char.myCharz().cvy = -10;
    						Char.myCharz().cvx = 4;
    						Char.myCharz().statusMe = 3;
    						Char.myCharz().cp1 = 0;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cvx = -Char.myCharz().cspeed + Char.myCharz().cBonusSpeed;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cvx = Char.myCharz().cspeed + Char.myCharz().cBonusSpeed;
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 3)
    			{
    				isAutoPlay = false;
    				GameCanvas.debug("F12", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23] || (GameCanvas.keyHold[1] && mobCapcha == null))
    				{
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cvx = -Char.myCharz().cspeed;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24] || (GameCanvas.keyHold[3] && mobCapcha == null))
    				{
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cvx = Char.myCharz().cspeed;
    					}
    				}
    				if ((GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] || ((GameCanvas.keyHold[1] || GameCanvas.keyHold[3]) && mobCapcha == null)) && Char.myCharz().canFly && Char.myCharz().cMP > 0 && Char.myCharz().cp1 < 8 && Char.myCharz().cvy > -4)
    				{
    					Char.myCharz().cp1++;
    					Char.myCharz().cvy = -7;
    				}
    			}
    			else if (Char.myCharz().statusMe == 4)
    			{
    				GameCanvas.debug("F13", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] && Char.myCharz().cMP > 0 && Char.myCharz().canFly)
    				{
    					isAutoPlay = false;
    					if ((Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0) && (Res.abs(Char.myCharz().cx - Char.myCharz().cxSend) > 96 || Res.abs(Char.myCharz().cy - Char.myCharz().cySend) > 24))
    					{
    						Service.gI().charMove();
    					}
    					Char.myCharz().cvy = -10;
    					Char.myCharz().statusMe = 3;
    					Char.myCharz().cp1 = 0;
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cp1++;
    						Char.myCharz().cvx = -Char.myCharz().cspeed;
    						if (Char.myCharz().cp1 > 5 && Char.myCharz().cvy > 6)
    						{
    							Char.myCharz().statusMe = 10;
    							Char.myCharz().cp1 = 0;
    							Char.myCharz().cvy = 0;
    						}
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cp1++;
    						Char.myCharz().cvx = Char.myCharz().cspeed;
    						if (Char.myCharz().cp1 > 5 && Char.myCharz().cvy > 6)
    						{
    							Char.myCharz().statusMe = 10;
    							Char.myCharz().cp1 = 0;
    							Char.myCharz().cvy = 0;
    						}
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 10)
    			{
    				GameCanvas.debug("F14", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				if (Char.myCharz().canFly && Char.myCharz().cMP > 0)
    				{
    					if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
    					{
    						isAutoPlay = false;
    						if ((Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0) && (Res.abs(Char.myCharz().cx - Char.myCharz().cxSend) > 96 || Res.abs(Char.myCharz().cy - Char.myCharz().cySend) > 24))
    						{
    							Service.gI().charMove();
    						}
    						Char.myCharz().cvy = -10;
    						Char.myCharz().statusMe = 3;
    						Char.myCharz().cp1 = 0;
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    					{
    						isAutoPlay = false;
    						if (Char.myCharz().cdir == 1)
    						{
    							Char.myCharz().cdir = -1;
    						}
    						else
    						{
    							Char.myCharz().cvx = -(Char.myCharz().cspeed + 1);
    						}
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    					{
    						if (Char.myCharz().cdir == -1)
    						{
    							Char.myCharz().cdir = 1;
    						}
    						else
    						{
    							Char.myCharz().cvx = Char.myCharz().cspeed + 1;
    						}
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 7)
    			{
    				GameCanvas.debug("F15", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cvx = -Char.myCharz().cspeed + 2;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cvx = Char.myCharz().cspeed - 2;
    					}
    				}
    			}
    			GameCanvas.debug("F17", 0);
    			if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] && GameCanvas.keyAsciiPress != 56)
    			{
    				GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] = false;
    				Char.myCharz().delayFall = 0;
    			}
    			if (GameCanvas.keyPressed[10])
    			{
    				GameCanvas.keyPressed[10] = false;
    				doUseHP();
    			}
    			GameCanvas.debug("F20", 0);
    			GameCanvas.clearKeyPressed();
    			GameCanvas.debug("F23", 0);
    			doSeleckSkillFlag = false;
    		}
    	}
    
    	public bool isVsMap()
    	{
    		return true;
    	}
    
    	private void checkDrag()
    	{
    		if (isAnalog == 1 || gamePad.disableCheckDrag())
    		{
    			return;
    		}
    		Char.myCharz().cmtoChar = true;
    		if (isUseTouch)
    		{
    			return;
    		}
    		if (GameCanvas.isPointerJustDown)
    		{
    			GameCanvas.isPointerJustDown = false;
    			isPointerDowning = true;
    			ptDownTime = 0;
    			ptLastDownX = (ptFirstDownX = GameCanvas.px);
    			ptLastDownY = (ptFirstDownY = GameCanvas.py);
    		}
    		if (isPointerDowning)
    		{
    			int num = GameCanvas.px - ptLastDownX;
    			int num2 = GameCanvas.py - ptLastDownY;
    			if (!isChangingCameraMode && (Res.abs(GameCanvas.px - ptFirstDownX) > 15 || Res.abs(GameCanvas.py - ptFirstDownY) > 15))
    			{
    				isChangingCameraMode = true;
    			}
    			ptLastDownX = GameCanvas.px;
    			ptLastDownY = GameCanvas.py;
    			ptDownTime++;
    			if (isChangingCameraMode)
    			{
    				Char.myCharz().cmtoChar = false;
    				cmx -= num;
    				cmy -= num2;
    				if (cmx < 24)
    				{
    					int num3 = (24 - cmx) / 3;
    					if (num3 != 0)
    					{
    						cmx += num - num / num3;
    					}
    				}
    				if (cmx < (isVsMap() ? 24 : 0))
    				{
    					cmx = (isVsMap() ? 24 : 0);
    				}
    				if (cmx > cmxLim)
    				{
    					int num4 = (cmx - cmxLim) / 3;
    					if (num4 != 0)
    					{
    						cmx += num - num / num4;
    					}
    				}
    				if (cmx > cmxLim + ((!isVsMap()) ? 24 : 0))
    				{
    					cmx = cmxLim + ((!isVsMap()) ? 24 : 0);
    				}
    				if (cmy < 0)
    				{
    					int num5 = -cmy / 3;
    					if (num5 != 0)
    					{
    						cmy += num2 - num2 / num5;
    					}
    				}
    				if (cmy < -((!isVsMap()) ? 24 : 0))
    				{
    					cmy = -((!isVsMap()) ? 24 : 0);
    				}
    				if (cmy > cmyLim)
    				{
    					cmy = cmyLim;
    				}
    				cmtoX = cmx;
    				cmtoY = cmy;
    			}
    		}
    		if (isPointerDowning && GameCanvas.isPointerJustRelease)
    		{
    			isPointerDowning = false;
    			isChangingCameraMode = false;
    			if (Res.abs(GameCanvas.px - ptFirstDownX) > 15 || Res.abs(GameCanvas.py - ptFirstDownY) > 15)
    			{
    				GameCanvas.isPointerJustRelease = false;
    			}
    		}
    	}
    
    	private void checkClick()
    	{
    		if (isCharging())
    		{
    			return;
    		}
    		if (popUpYesNo != null && popUpYesNo.cmdYes != null && popUpYesNo.cmdYes.isPointerPressInside())
    		{
    			popUpYesNo.cmdYes.performAction();
    		}
    		else
    		{
    			if (checkClickToCapcha())
    			{
    				return;
    			}
    			long num = mSystem.currentTimeMillis();
    			if (lastSingleClick != 0)
    			{
    				lastSingleClick = 0L;
    				GameCanvas.isPointerJustDown = false;
    				if (!disableSingleClick)
    				{
    					checkSingleClick();
    					GameCanvas.isPointerJustRelease = false;
    					isWaitingDoubleClick = true;
    					timeStartDblClick = mSystem.currentTimeMillis();
    				}
    			}
    			if (isWaitingDoubleClick)
    			{
    				timeEndDblClick = mSystem.currentTimeMillis();
    				if (timeEndDblClick - timeStartDblClick < 300 && GameCanvas.isPointerJustRelease)
    				{
    					isWaitingDoubleClick = false;
    					checkDoubleClick();
    					GameCanvas.isPointerJustRelease = false;
    				}
    			}
    			if (GameCanvas.isPointerJustRelease)
    			{
    				disableSingleClick = checkSingleClickEarly();
    				lastSingleClick = num;
    				lastClickCMX = cmx;
    				lastClickCMY = cmy;
    				GameCanvas.isPointerJustRelease = false;
    			}
    		}
    	}
    
    	private IMapObject findClickToItem(int px, int py)
    	{
    		IMapObject mapObject = null;
    		int num = 0;
    		int num2 = 30;
    		MyVector[] array = new MyVector[4] { vMob, vNpc, vItemMap, vCharInMap };
    		for (int i = 0; i < array.Length; i++)
    		{
    			for (int j = 0; j < array[i].size(); j++)
    			{
    				IMapObject mapObject2 = (IMapObject)array[i].elementAt(j);
    				if (mapObject2.isInvisible())
    				{
    					continue;
    				}
    				if (mapObject2 is Mob)
    				{
    					Mob mob = (Mob)mapObject2;
    					if (mob.isMobMe && mob.Equals(Char.myCharz().mobMe))
    					{
    						continue;
    					}
    				}
    				if (mapObject2 is Char)
    				{
    					Char c = (Char)mapObject2;
    					if (c.isPetFollower || c.charID <= -310720000 || c.cName == "$" || (c.isPet && string.IsNullOrEmpty(c.cName)))
    					{
    						continue;
    					}
    				}
    				int x = mapObject2.getX();
    				int y = mapObject2.getY();
    				int w = mapObject2.getW();
    				int h = mapObject2.getH();
    				if (!inRectangle(px, py, x - w / 2 - num2, y - h - num2, w + num2 * 2, h + num2 * 2))
    				{
    					continue;
    				}
    				if (mapObject == null)
    				{
    					mapObject = mapObject2;
    					num = Res.abs(px - x) + Res.abs(py - y);
    					if (i == 1)
    					{
    						return mapObject;
    					}
    				}
    				else
    				{
    					int num3 = Res.abs(px - x) + Res.abs(py - y);
    					if (num3 < num)
    					{
    						mapObject = mapObject2;
    						num = num3;
    					}
    				}
    			}
    		}
    		return mapObject;
    	}
    
    	private Mob findClickToMOB(int px, int py)
    	{
    		int num = 30;
    		Mob mob = null;
    		int num2 = 0;
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			Mob mob2 = (Mob)vMob.elementAt(i);
    			if (mob2.isInvisible())
    			{
    				continue;
    			}
    			if (mob2 != null)
    			{
    				Mob mob3 = mob2;
    				if (mob3.isMobMe && mob3.Equals(Char.myCharz().mobMe))
    				{
    					continue;
    				}
    			}
    			int x = mob2.getX();
    			int y = mob2.getY();
    			int w = mob2.getW();
    			int h = mob2.getH();
    			if (!inRectangle(px, py, x - w / 2 - num, y - h - num, w + num * 2, h + num * 2))
    			{
    				continue;
    			}
    			if (mob == null)
    			{
    				mob = mob2;
    				num2 = Res.abs(px - x) + Res.abs(py - y);
    				continue;
    			}
    			int num3 = Res.abs(px - x) + Res.abs(py - y);
    			if (num3 < num2)
    			{
    				mob = mob2;
    				num2 = num3;
    			}
    		}
    		return mob;
    	}
    
    	private bool inRectangle(int xClick, int yClick, int x, int y, int w, int h)
    	{
    		return xClick >= x && xClick <= x + w && yClick >= y && yClick <= y + h;
    	}
    
    	private bool checkSingleClickEarly()
    	{
    		int num = GameCanvas.px + cmx;
    		int num2 = GameCanvas.py + cmy;
    		Char.myCharz().cancelAttack();
    		IMapObject mapObject = findClickToItem(num, num2);
    		if (mapObject != null)
    		{
    			if (Char.myCharz().isAttacPlayerStatus() && Char.myCharz().charFocus != null && !mapObject.Equals(Char.myCharz().charFocus) && !mapObject.Equals(Char.myCharz().charFocus.mobMe) && mapObject is Char)
    			{
    				Char @char = (Char)mapObject;
    				if (@char.cTypePk != 5 && !@char.isAttacPlayerStatus())
    				{
    					checkClickMoveTo(num, num2, 2);
    					return false;
    				}
    			}
    			if (Char.myCharz().mobFocus == mapObject || Char.myCharz().itemFocus == mapObject)
    			{
    				doDoubleClickToObj(mapObject);
    				return true;
    			}
    			if (TileMap.mapID == 51 && mapObject.Equals(Char.myCharz().npcFocus))
    			{
    				checkClickMoveTo(num, num2, 3);
    				return false;
    			}
    			if (Char.myCharz().skillPaint != null || Char.myCharz().arr != null || Char.myCharz().dart != null || Char.myCharz().skillInfoPaint() != null)
    			{
    				return false;
    			}
    			Char.myCharz().focusManualTo(mapObject);
    			mapObject.stopMoving();
    			return false;
    		}
    		return false;
    	}
    
    	private void checkDoubleClick()
    	{
    		int num = GameCanvas.px + lastClickCMX;
    		int num2 = GameCanvas.py + lastClickCMY;
    		int cy = Char.myCharz().cy;
    		if (isLockKey)
    		{
    			return;
    		}
    		IMapObject mapObject = findClickToItem(num, num2);
    		if (mapObject != null)
    		{
    			if (mapObject is Mob && !isMeCanAttackMob((Mob)mapObject))
    			{
    				checkClickMoveTo(num, num2, 4);
    			}
    			else
    			{
    				if (checkClickToBotton(mapObject) || (!mapObject.Equals(Char.myCharz().npcFocus) && mobCapcha != null))
    				{
    					return;
    				}
    				if (Char.myCharz().isAttacPlayerStatus() && Char.myCharz().charFocus != null && !mapObject.Equals(Char.myCharz().charFocus) && !mapObject.Equals(Char.myCharz().charFocus.mobMe) && mapObject is Char)
    				{
    					Char @char = (Char)mapObject;
    					if (@char.cTypePk != 5 && !@char.isAttacPlayerStatus())
    					{
    						checkClickMoveTo(num, num2, 5);
    						return;
    					}
    				}
    				if (TileMap.mapID == 51 && mapObject.Equals(Char.myCharz().npcFocus))
    				{
    					checkClickMoveTo(num, num2, 6);
    				}
    				else
    				{
    					doDoubleClickToObj(mapObject);
    				}
    			}
    		}
    		else if (!checkClickToPopup(num, num2) && !checkClipTopChatPopUp(num, num2) && !Main.isPC)
    		{
    			checkClickMoveTo(num, num2, 7);
    		}
    	}
    
    	private bool checkClickToBotton(IMapObject Object)
    	{
    		if (Object == null)
    		{
    			return false;
    		}
    		int y = Object.getY();
    		int num = Char.myCharz().cy;
    		if (y < num)
    		{
    			while (y < num)
    			{
    				num -= 5;
    				if (TileMap.tileTypeAt(Char.myCharz().cx, num, 8192))
    				{
    					auto = 0;
    					Char.myCharz().cancelAttack();
    					Char.myCharz().currentMovePoint = null;
    					return true;
    				}
    			}
    		}
    		return false;
    	}
    
    	public void doDoubleClickToObj(IMapObject obj)
    	{
    		if ((obj.Equals(Char.myCharz().npcFocus) || mobCapcha == null) && !checkClickToBotton(obj))
    		{
    			checkEffToObj(obj, false);
    			Char.myCharz().cancelAttack();
    			Char.myCharz().currentMovePoint = null;
    			Char.myCharz().cvx = (Char.myCharz().cvy = 0);
    			obj.stopMoving();
    			if (obj is Mob)
    			{
    				Char.myCharz().mobFocus = (Mob)obj;
    			}
    			if (Char.myCharz().myskill == null && Char.myCharz().vSkillFight.size() > 0)
    			{
    				Char.myCharz().myskill = (Skill)Char.myCharz().vSkillFight.elementAt(0);
    			}
    			auto = 10;
    			doFire(false, true);
    			clickToX = obj.getX();
    			clickToY = obj.getY();
    			clickOnTileTop = false;
    			clickMoving = true;
    			clickMovingRed = true;
    			clickMovingTimeOut = 20;
    			clickMovingP1 = 30;
    		}
    	}
    
    	private void checkSingleClick()
    	{
    		int xClick = GameCanvas.px + lastClickCMX;
    		int yClick = GameCanvas.py + lastClickCMY;
    		if (!isLockKey && !checkClickToPopup(xClick, yClick) && !checkClipTopChatPopUp(xClick, yClick))
    		{
    			checkClickMoveTo(xClick, yClick, 0);
    		}
    	}
    
    	private bool checkClipTopChatPopUp(int xClick, int yClick)
    	{
    		if (Equals(info2) && gI().popUpYesNo != null)
    		{
    			return false;
    		}
    		if (info2.info.info != null && info2.info.info.charInfo != null)
    		{
    			int num = 0;
    			int num2 = 0;
    			num = Res.abs(info2.cmx) + info2.info.X - 40;
    			num2 = Res.abs(info2.cmy) + info2.info.Y;
    			if (inRectangle(xClick - cmx, yClick - cmy, num, num2, 200, info2.info.H))
    			{
    				info2.doClick(10);
    				return true;
    			}
    		}
    		return false;
    	}
    }
}

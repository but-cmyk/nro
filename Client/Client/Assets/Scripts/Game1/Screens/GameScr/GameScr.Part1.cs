namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    using UnityEngine;
    
    public partial class GameScr : mScreen, IChatable
    {
    
    	public static void initVersionFromRMS()
    	{
    		sbyte[] array = Rms.loadRMS("NRdataVersion");
    		sbyte[] array2 = Rms.loadRMS("NRmapVersion");
    		sbyte[] array3 = Rms.loadRMS("NRskillVersion");
    		sbyte[] array4 = Rms.loadRMS("NRitemVersion");
    		if (array != null && array.Length > 0)
    		{
    			vcData = array[0];
    		}
    		if (array2 != null && array2.Length > 0)
    		{
    			vcMap = array2[0];
    		}
    		if (array3 != null && array3.Length > 0)
    		{
    			vcSkill = array3[0];
    		}
    		if (array4 != null && array4.Length > 0)
    		{
    			vcItem = array4[0];
    		}
    	}
    
    	public GameScr()
    	{
    		if (imgMenu == null)
    		{
    			loadBg();
    		}
    		if (GameCanvas.w == 128 || GameCanvas.h <= 208)
    		{
    			indexSize = 20;
    		}
    		cmdback = new Command(string.Empty, 11021);
    		cmdMenu = new Command("menu", 11000);
    		cmdFocus = new Command(string.Empty, 11001);
    		cmdMenu.img = imgMenu;
    		cmdMenu.w = mGraphics.getImageWidth(cmdMenu.img) + 20;
    		cmdMenu.isPlaySoundButton = false;
    		cmdFocus.img = imgFocus;
    		if (GameCanvas.isTouch)
    		{
    			cmdMenu.x = 0;
    			cmdMenu.y = 50;
    			cmdFocus = null;
    		}
    		else
    		{
    			cmdMenu.x = 0;
    			cmdMenu.y = gH - 30;
    			cmdFocus.x = gW - 32;
    			cmdFocus.y = gH - 32;
    		}
    		right = cmdFocus;
    		isPaintRada = 1;
    		if (GameCanvas.isTouch)
    		{
    			isHaveSelectSkill = true;
    		}
    		cmdDoiCo = new Command("Đổi cờ", GameCanvas.gI(), 100001, null);
    		cmdLogOut = new Command("Logout", GameCanvas.gI(), 100002, null);
    		cmdChatTheGioi = new Command("chat world", GameCanvas.gI(), 100003, null);
    		cmdshowInfo = new Command("InfoLog", GameCanvas.gI(), 100004, null);
    		cmdDoiCo.setType();
    		cmdLogOut.setType();
    		cmdChatTheGioi.setType();
    		cmdshowInfo.setType();
    		cmdChatTheGioi.x = GameCanvas.w - cmdChatTheGioi.w;
    		cmdshowInfo.x = GameCanvas.w - cmdshowInfo.w;
    		cmdLogOut.x = GameCanvas.w - cmdLogOut.w;
    		cmdDoiCo.x = GameCanvas.w - cmdDoiCo.w;
    		cmdChatTheGioi.y = cmdChatTheGioi.h + mFont.tahoma_7_white.getHeight();
    		cmdshowInfo.y = cmdChatTheGioi.h * 2 + mFont.tahoma_7_white.getHeight();
    		cmdLogOut.y = cmdChatTheGioi.h * 3 + mFont.tahoma_7_white.getHeight();
    		cmdDoiCo.y = cmdChatTheGioi.h * 4 + mFont.tahoma_7_white.getHeight();
    	}
    
    	public static void loadBg()
        {
            doikhu = GameCanvas.loadImage("/mainImage/doikhu.png");
            arrow4 = GameCanvas.loadImage("/mainImage/myTexture2darrow4.png");
            arrow5 = GameCanvas.loadImage("/mainImage/myTexture2darrow5.png");
            fra_PVE_Bar_0 = new FrameImage(mSystem.loadImage("/mainImage/i_pve_bar_0.png"), 6, 15);
    		fra_PVE_Bar_1 = new FrameImage(mSystem.loadImage("/mainImage/i_pve_bar_1.png"), 38, 21);
    		imgVS = mSystem.loadImage("/mainImage/i_vs.png");
    		imgBall = mSystem.loadImage("/mainImage/i_charlife.png");
    		imgHP_NEW = mSystem.loadImage("/mainImage/i_hp.png");
    		imgKhung = mSystem.loadImage("/mainImage/i_khung.png");
    		imgLbtn = GameCanvas.loadImage("/mainImage/myTexture2dbtnl.png");
    		imgLbtnFocus = GameCanvas.loadImage("/mainImage/myTexture2dbtnlf.png");
    		imgLbtn2 = GameCanvas.loadImage("/mainImage/myTexture2dbtnl2.png");
    		imgLbtnFocus2 = GameCanvas.loadImage("/mainImage/myTexture2dbtnlf2.png");
    		imgPanel = GameCanvas.loadImage("/mainImage/myTexture2dpanel.png");
    		imgPanel2 = GameCanvas.loadImage("/mainImage/panel2.png");
    		imgHP = GameCanvas.loadImage("/mainImage/myTexture2dHP.png");
    		imgSP = GameCanvas.loadImage("/mainImage/SP.png");
    		imgHPLost = GameCanvas.loadImage("/mainImage/myTexture2dhpLost.png");
    		imgMPLost = GameCanvas.loadImage("/mainImage/myTexture2dmpLost.png");
    		imgMP = GameCanvas.loadImage("/mainImage/myTexture2dMP.png");
    		imgSkill = GameCanvas.loadImage("/mainImage/myTexture2dskill.png");
    		imgSkill2 = GameCanvas.loadImage("/mainImage/myTexture2dskill2.png");
    		imgMenu = GameCanvas.loadImage("/mainImage/myTexture2dmenu.png");
    		imgFocus = GameCanvas.loadImage("/mainImage/myTexture2dfocus.png");
    		imgHP_tm_do = GameCanvas.loadImage("/mainImage/tm-do.png");
    		imgHP_tm_vang = GameCanvas.loadImage("/mainImage/tm-vang.png");
    		imgHP_tm_xam = GameCanvas.loadImage("/mainImage/tm-xam.png");
    		imgHP_tm_xanh = GameCanvas.loadImage("/mainImage/tm-xanh.png");
    		imgChatPC = GameCanvas.loadImage("/pc/chat.png");
    		imgChatsPC2 = GameCanvas.loadImage("/pc/chat2.png");
    		if (GameCanvas.isTouch)
    		{
    			imgArrow = GameCanvas.loadImage("/mainImage/myTexture2darrow.png");
    			imgArrow2 = GameCanvas.loadImage("/mainImage/myTexture2darrow2.png");
    			imgChat = GameCanvas.loadImage("/mainImage/myTexture2dchat.png");
    			imgChat2 = GameCanvas.loadImage("/mainImage/myTexture2dchat2.png");
    			imgFocus2 = GameCanvas.loadImage("/mainImage/myTexture2dfocus2.png");
    			imgHP1 = GameCanvas.loadImage("/mainImage/myTexture2dPea0.png");
    			imgHP2 = GameCanvas.loadImage("/mainImage/myTexture2dPea1.png");
    			imgAnalog1 = GameCanvas.loadImage("/mainImage/myTexture2danalog1.png");
    			imgAnalog2 = GameCanvas.loadImage("/mainImage/myTexture2danalog2.png");
    			imgHP3 = GameCanvas.loadImage("/mainImage/myTexture2dPea2.png");
    			imgHP4 = GameCanvas.loadImage("/mainImage/myTexture2dPea3.png");
    			imgFire0 = GameCanvas.loadImage("/mainImage/myTexture2dfirebtn0.png");
    			imgFire1 = GameCanvas.loadImage("/mainImage/myTexture2dfirebtn1.png");
    		}
    		imgNR1 = GameCanvas.loadImage("/mainImage/myTexture2dPea_0.png");
    		imgNR2 = GameCanvas.loadImage("/mainImage/myTexture2dPea_1.png");
    		imgNR3 = GameCanvas.loadImage("/mainImage/myTexture2dPea_2.png");
    		imgNR4 = GameCanvas.loadImage("/mainImage/myTexture2dPea_3.png");
    		flyTextX = new int[5];
    		flyTextY = new int[5];
    		flyTextDx = new int[5];
    		flyTextDy = new int[5];
    		flyTextState = new int[5];
    		flyTextString = new string[5];
    		flyTextYTo = new int[5];
    		flyTime = new int[5];
    		flyTextColor = new int[8];
    		for (int i = 0; i < 5; i++)
    		{
    			flyTextState[i] = -1;
    		}
    		initVersionFromRMS();
    		imgNut = GameCanvas.loadImage("/mainImage/myTexture2dnut.png");
    		imgNutF = GameCanvas.loadImage("/mainImage/myTexture2dnutF.png");
    		MobCapcha.init();
    		isAnalog = !Main.isPC ? 1 : 0;
    		gamePad = new GamePad();
    		arrow = GameCanvas.loadImage("/mainImage/myTexture2darrow3.png");
    		imgTrans = GameCanvas.loadImage("/bg/trans.png");
    		imgRoomStat = GameCanvas.loadImage("/mainImage/myTexture2dstat.png");
    		frBarPow0 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor20.png");
    		frBarPow1 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor21.png");
    		frBarPow2 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor22.png");
    		frBarPow20 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor00.png");
    		frBarPow21 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor01.png");
    		frBarPow22 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor02.png");
    	}
    
    	public void initSelectChar()
    	{
    		readPart();
    		SmallImage.init();
    		SmallImage.loadBigRMS();
    	}
    
    	public static void paintOngMauPercent(Image img0, Image img1, Image img2, float x, float y, int size, float pixelPercent, mGraphics g)
    	{
    		int clipX = g.getClipX();
    		int clipY = g.getClipY();
    		int clipWidth = g.getClipWidth();
    		int clipHeight = g.getClipHeight();
    		g.setClip((int)x, (int)y, (int)pixelPercent, 13);
    		int num = size / 15 - 2;
    		for (int i = 0; i < num; i++)
    		{
    			g.drawImage(img1, x + (float)((i + 1) * 15), y, 0);
    		}
    		g.drawImage(img0, x, y, 0);
    		g.drawImage(img1, x + (float)size - 30f, y, 0);
    		g.drawImage(img2, x + (float)size - 15f, y, 0);
    		g.setClip(clipX, clipY, clipWidth, clipHeight);
    	}
    
    	public void initTraining()
    	{
    		if (CreateCharScr.isCreateChar)
    		{
    			CreateCharScr.isCreateChar = false;
    			right = null;
    		}
    	}
    
    	public bool isMapDocNhan()
    	{
    		if (TileMap.mapID >= 53 && TileMap.mapID <= 62)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isMapFize()
    	{
    		if (TileMap.mapID >= 63)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public override void switchToMe()
    	{
    		Sound.isIngame = true;
    		if (GameCanvas.isPlaySound)
    		{
    			SoundMn.gI().loadSound(TileMap.mapID);
    		}
    		vChatVip.removeAllElements();
    		ServerListScreen.isWait = false;
    		ServerListScreen.waitToLogin = false;
    		if (BackgroudEffect.isHaveRain())
    		{
    			SoundMn.gI().rain();
    		}
    		else
    		{
    			SoundMn.gI().stopRain();
    		}
    		LoginScr.isContinueToLogin = false;
    		Char.isLoadingMap = false;
    		if (!isPaintOther)
    		{
    			Service.gI().finishLoadMap();
    		}
    		if (TileMap.isTrainingMap())
    		{
    			initTraining();
    		}
    		info1.isUpdate = true;
    		info2.isUpdate = true;
    		resetButton();
    		isLoadAllData = true;
    		isPaintOther = false;
    		base.switchToMe();
    		if (Char.myCharz() != null && Char.myCharz().charID > 0)
    		{
    			Game1.God.AutoSettingManager.getInstance().Load(Char.myCharz().charID);
    		}
    	}
    
    	public static int getMaxExp(int level)
    	{
    		int num = 0;
    		for (int i = 0; i <= level; i++)
    		{
    			num += (int)exps[i];
    		}
    		return num;
    	}
    
    	public static void resetAllvector()
    	{
    		vCharInMap.removeAllElements();
    		Teleport.vTeleport.removeAllElements();
    		vItemMap.removeAllElements();
    		Effect2.vEffect2.removeAllElements();
    		Effect2.vAnimateEffect.removeAllElements();
    		Effect2.vEffect2Outside.removeAllElements();
    		Effect2.vEffectFeet.removeAllElements();
    		Effect2.vEffect3.removeAllElements();
    		vMobAttack.removeAllElements();
    		vMob.removeAllElements();
    		vNpc.removeAllElements();
    		Char.myCharz().vMovePoints.removeAllElements();
    	}
    
    	public void loadSkillShortcut()
    	{
    	}
    
    	public void onOSkill(sbyte[] oSkillID)
    	{
    		Cout.println("GET onScreenSkill!");
    		onScreenSkill = new Skill[10];
    		if (oSkillID == null)
    		{
    			loadDefaultonScreenSkill();
    			return;
    		}
    		for (int i = 0; i < oSkillID.Length; i++)
    		{
    			for (int j = 0; j < Char.myCharz().vSkillFight.size(); j++)
    			{
    				Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(j);
    				if (skill.template.id == oSkillID[i])
    				{
    					onScreenSkill[i] = skill;
    					break;
    				}
    			}
    		}
    		if (onScreenSkill[0] == null && Char.myCharz().vSkillFight.size() > 0)
    		{
    			onScreenSkill[0] = (Skill)Char.myCharz().vSkillFight.elementAt(0);
    		}
    	}
    
    	public void onKSkill(sbyte[] kSkillID)
    	{
    		Cout.println("GET KEYSKILL!");
    		keySkill = new Skill[10];
    		if (kSkillID == null)
    		{
    			loadDefaultKeySkill();
    			return;
    		}
    		for (int i = 0; i < kSkillID.Length; i++)
    		{
    			for (int j = 0; j < Char.myCharz().vSkillFight.size(); j++)
    			{
    				Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(j);
    				if (skill.template.id == kSkillID[i])
    				{
    					keySkill[i] = skill;
    					break;
    				}
    			}
    		}
    		if (keySkill[0] == null && Char.myCharz().vSkillFight.size() > 0)
    		{
    			keySkill[0] = (Skill)Char.myCharz().vSkillFight.elementAt(0);
    		}
    	}
    
    	public void onCSkill(sbyte[] cSkillID)
    	{
    		Cout.println("GET CURRENTSKILL!");
    		if (cSkillID == null || cSkillID.Length == 0)
    		{
    			if (Char.myCharz().vSkillFight.size() > 0)
    			{
    				Char.myCharz().myskill = (Skill)Char.myCharz().vSkillFight.elementAt(0);
    			}
    		}
    		else
    		{
    			for (int i = 0; i < Char.myCharz().vSkillFight.size(); i++)
    			{
    				Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    				if (skill.template.id == cSkillID[0])
    				{
    					Char.myCharz().myskill = skill;
    					break;
    				}
    			}
    		}
    		if (Char.myCharz().myskill != null)
    		{
    			Service.gI().selectSkill(Char.myCharz().myskill.template.id);
    			saveRMSCurrentSkill(Char.myCharz().myskill.template.id);
    		}
    	}
    
    	private void loadDefaultonScreenSkill()
    	{
    		Cout.println("LOAD DEFAULT ONmScreen SKILL");
    		for (int i = 0; i < onScreenSkill.Length && i < Char.myCharz().vSkillFight.size(); i++)
    		{
    			Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    			onScreenSkill[i] = skill;
    		}
    		saveonScreenSkillToRMS();
    	}
    
    	private void loadDefaultKeySkill()
    	{
    		Cout.println("LOAD DEFAULT KEY SKILL");
    		for (int i = 0; i < keySkill.Length && i < Char.myCharz().vSkillFight.size(); i++)
    		{
    			Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    			keySkill[i] = skill;
    		}
    		saveKeySkillToRMS();
    	}
    
    	public void doSetOnScreenSkill(SkillTemplate skillTemplate)
    	{
    		Skill skill = Char.myCharz().getSkill(skillTemplate);
    		MyVector myVector = new MyVector();
    		for (int i = 0; i < 10; i++)
    		{
    			Command command = new Command(p: new object[2]
    			{
    				skill,
    				i + string.Empty
    			}, caption: mResources.into_place + (i + 1), action: 11120);
    			Skill skill2 = onScreenSkill[i];
    			if (skill2 != null)
    			{
    				command.isDisplay = true;
    			}
    			myVector.addElement(command);
    		}
    		GameCanvas.menu.startAt(myVector, 0);
    	}
    
    	public void doSetKeySkill(SkillTemplate skillTemplate)
    	{
    		Cout.println("DO SET KEY SKILL");
    		Skill skill = Char.myCharz().getSkill(skillTemplate);
    		string[] array = ((!TField.isQwerty) ? mResources.key_skill : mResources.key_skill_qwerty);
    		MyVector myVector = new MyVector();
    		for (int i = 0; i < 10; i++)
    		{
    			myVector.addElement(new Command(p: new object[2]
    			{
    				skill,
    				i + string.Empty
    			}, caption: array[i], action: 11121));
    		}
    		GameCanvas.menu.startAt(myVector, 0);
    	}
    
    	public void saveonScreenSkillToRMS()
    	{
    		sbyte[] array = new sbyte[onScreenSkill.Length];
    		for (int i = 0; i < onScreenSkill.Length; i++)
    		{
    			if (onScreenSkill[i] == null)
    			{
    				array[i] = -1;
    			}
    			else
    			{
    				array[i] = onScreenSkill[i].template.id;
    			}
    		}
    		Service.gI().changeOnKeyScr(array);
    	}
    
    	public void saveKeySkillToRMS()
    	{
    		sbyte[] array = new sbyte[keySkill.Length];
    		for (int i = 0; i < keySkill.Length; i++)
    		{
    			if (keySkill[i] == null)
    			{
    				array[i] = -1;
    			}
    			else
    			{
    				array[i] = keySkill[i].template.id;
    			}
    		}
    		Service.gI().changeOnKeyScr(array);
    	}
    
    	public void saveRMSCurrentSkill(sbyte id)
    	{
    	}
    
    	public void addSkillShortcut(Skill skill)
    	{
    		Cout.println("ADD SKILL SHORTCUT TO SKILL " + skill.template.id);
    		for (int i = 0; i < onScreenSkill.Length; i++)
    		{
    			if (onScreenSkill[i] == null)
    			{
    				onScreenSkill[i] = skill;
    				break;
    			}
    		}
    		for (int j = 0; j < keySkill.Length; j++)
    		{
    			if (keySkill[j] == null)
    			{
    				keySkill[j] = skill;
    				break;
    			}
    		}
    		if (Char.myCharz().myskill == null)
    		{
    			Char.myCharz().myskill = skill;
    		}
    		saveKeySkillToRMS();
    		saveonScreenSkillToRMS();
    	}
    
    	public bool isBagFull()
    	{
    		for (int num = Char.myCharz().arrItemBag.Length - 1; num >= 0; num--)
    		{
    			if (Char.myCharz().arrItemBag[num] == null)
    			{
    				return false;
    			}
    		}
    		return true;
    	}
    
    	public void createConfirm(string[] menu, Npc npc)
    	{
    		resetButton();
    		isLockKey = true;
    		left = new Command(menu[0], 130011, npc);
    		right = new Command(menu[1], 130012, npc);
    	}
    
    	public void createMenu(string[] menu, Npc npc)
    	{
    		MyVector myVector = new MyVector();
    		for (int i = 0; i < menu.Length; i++)
    		{
    			myVector.addElement(new Command(menu[i], 11057, npc));
    		}
    		GameCanvas.menu.startAt(myVector, 2);
    	}
    
    	public void readPart()
    	{
    		DataInputStream dataInputStream = null;
    		int currentPartIndex = -1;
    		int currentPartType = -1;
    		try
    		{
    			sbyte[] data = Rms.loadRMS("NR_part");
    			if (data == null)
    			{
    				return;
    			}
    			dataInputStream = new DataInputStream(data);
    			int num = dataInputStream.readShort();
    			parts = new Part[num];
    			for (int i = 0; i < num; i++)
    			{
    				currentPartIndex = i;
    				int type = dataInputStream.readByte();
    				currentPartType = type;
    				parts[i] = new Part(type);
    				if (parts[i].pi == null || parts[i].pi.Length == 0)
    				{
    					Cout.LogError($"[readPart] LỖI CẤU TRÚC PART: Part index {i}/{num} có type không hợp lệ ({type}), pi bị rỗng! Byte stream có thể đã bị lệch (desync).");
    					continue;
    				}
    				for (int j = 0; j < parts[i].pi.Length; j++)
    				{
    					parts[i].pi[j] = new PartImage();
    					parts[i].pi[j].id = dataInputStream.readShort();
    					parts[i].pi[j].dx = dataInputStream.readByte();
    					parts[i].pi[j].dy = dataInputStream.readByte();
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError($"[readPart] LỖI TẠI readPart: {ex.GetType().Name} - {ex.Message} tại Part Index {currentPartIndex} (Type={currentPartType}).\nStack: {ex.StackTrace}");
    		}
    		finally
    		{
    			try
    			{
    				if (dataInputStream != null)
    				{
    					dataInputStream.close();
    				}
    			}
    			catch (Exception ex2)
    			{
    				Res.outz2("LOI TAI readPart 2" + ex2.StackTrace);
    			}
    		}
    	}
    
    	public void readEfect()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			sbyte[] data = Rms.loadRMS("NR_effect");
    			if (data == null)
    			{
    				return;
    			}
    			dataInputStream = new DataInputStream(data);
    			int num = dataInputStream.readShort();
    			efs = new EffectCharPaint[num];
    			for (int i = 0; i < num; i++)
    			{
    				efs[i] = new EffectCharPaint();
    				efs[i].idEf = dataInputStream.readShort();
    				efs[i].arrEfInfo = new EffectInfoPaint[dataInputStream.readByte()];
    				for (int j = 0; j < efs[i].arrEfInfo.Length; j++)
    				{
    					efs[i].arrEfInfo[j] = new EffectInfoPaint();
    					efs[i].arrEfInfo[j].idImg = dataInputStream.readShort();
    					efs[i].arrEfInfo[j].dx = dataInputStream.readByte();
    					efs[i].arrEfInfo[j].dy = dataInputStream.readByte();
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi readEfect: " + ex.ToString());
    		}
    		finally
    		{
    			try
    			{
    				if (dataInputStream != null)
    				{
    					dataInputStream.close();
    				}
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham Eff: " + ex2.ToString());
    			}
    		}
    	}

    	public static EffectCharPaint getEffect(int id)
    	{
    		try
    		{
    			if (efs == null)
    			{
    				GameScr.gI().readEfect();
    			}
    			if (efs != null && id - 1 >= 0 && id - 1 < efs.Length)
    			{
    				return efs[id - 1];
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError($"[getEffect] Lỗi lấy effect ID={id}: {ex.Message}");
    		}
    		return null;
    	}
    
    	public void readArrow()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			dataInputStream = new DataInputStream(Rms.loadRMS("NR_arrow"));
    			int num = dataInputStream.readShort();
    			arrs = new Arrowpaint[num];
    			for (int i = 0; i < num; i++)
    			{
    				arrs[i] = new Arrowpaint();
    				arrs[i].id = dataInputStream.readShort();
    				arrs[i].imgId[0] = dataInputStream.readShort();
    				arrs[i].imgId[1] = dataInputStream.readShort();
    				arrs[i].imgId[2] = dataInputStream.readShort();
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError($"[readArrow] Lỗi nạp NR_arrow: {ex.Message}\n{ex.StackTrace}");
    		}
    		finally
    		{
    			try
    			{
    				dataInputStream.close();
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham readArrow: " + ex2.ToString());
    			}
    		}
    	}
    
    	public void readDart()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			sbyte[] array = Rms.loadRMS("NR_dart");
    			if (array == null)
    			{
    				if (darts == null)
    				{
    					darts = new DartInfo[0];
    				}
    				return;
    			}
    			dataInputStream = new DataInputStream(array);
    			int num = dataInputStream.readShort();
    			darts = new DartInfo[num];
    			for (int i = 0; i < num; i++)
    			{
    				darts[i] = new DartInfo();
    				darts[i].id = dataInputStream.readShort();
    				darts[i].nUpdate = dataInputStream.readShort();
    				darts[i].va = dataInputStream.readShort() * 256;
    				darts[i].xdPercent = dataInputStream.readShort();
    				int num2 = dataInputStream.readShort();
    				darts[i].tail = new short[num2];
    				for (int j = 0; j < num2; j++)
    				{
    					darts[i].tail[j] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].tailBorder = new short[num2];
    				for (int k = 0; k < num2; k++)
    				{
    					darts[i].tailBorder[k] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].xd1 = new short[num2];
    				for (int l = 0; l < num2; l++)
    				{
    					darts[i].xd1[l] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].xd2 = new short[num2];
    				for (int m = 0; m < num2; m++)
    				{
    					darts[i].xd2[m] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].head = new short[num2][];
    				for (int n = 0; n < num2; n++)
    				{
    					int num3 = dataInputStream.readShort();
    					darts[i].head[n] = new short[num3];
    					for (int num4 = 0; num4 < num3; num4++)
    					{
    						darts[i].head[n][num4] = dataInputStream.readShort();
    					}
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].headBorder = new short[num2][];
    				for (int num5 = 0; num5 < num2; num5++)
    				{
    					int num6 = dataInputStream.readShort();
    					darts[i].headBorder[num5] = new short[num6];
    					for (int num7 = 0; num7 < num6; num7++)
    					{
    						darts[i].headBorder[num5][num7] = dataInputStream.readShort();
    					}
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham ReadDart: " + ex.ToString());
    			if (darts == null)
    			{
    				darts = new DartInfo[0];
    			}
    		}
    		finally
    		{
    			try
    			{
    				if (dataInputStream != null)
    				{
    					dataInputStream.close();
    				}
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham reaaDart: " + ex2.ToString());
    			}
    		}
    	}
    
    	public void readSkill()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			sbyte[] data = Rms.loadRMS("NR_skill");
    			if (data == null)
    			{
    				Res.outz("[CLIENT_READ_SKILL_WARN] NR_skill RMS is null! Requesting updateData from server...");
    				Service.gI().updateData();
    				return;
    			}
    			dataInputStream = new DataInputStream(data);
    			int num = dataInputStream.readShort();
    			sks = new SkillPaint[System.Math.Max(512, num + 100)];
    			Res.outz("[CLIENT_READ_SKILL_SUCCESS] Parsing " + num + " skills from NR_skill RMS");
    			for (int i = 0; i < num; i++)
    			{
    				short num3 = dataInputStream.readShort();
    				if (num3 == 1111)
    				{
    					num3 = (short)(num - 1);
    				}
    				if (num3 >= 0 && num3 < sks.Length)
    				{
    					sks[num3] = new SkillPaint();
    					sks[num3].id = num3;
    					sks[num3].effectHappenOnMob = dataInputStream.readShort();
    					if (sks[num3].effectHappenOnMob <= 0)
    					{
    						sks[num3].effectHappenOnMob = 80;
    					}
    					sks[num3].numEff = dataInputStream.readByte();
    					sks[num3].skillStand = new SkillInfoPaint[dataInputStream.readByte()];
    					for (int j = 0; j < sks[num3].skillStand.Length; j++)
    					{
    						sks[num3].skillStand[j] = new SkillInfoPaint();
    						sks[num3].skillStand[j].status = dataInputStream.readByte();
    						sks[num3].skillStand[j].effS0Id = dataInputStream.readShort();
    						sks[num3].skillStand[j].e0dx = dataInputStream.readShort();
    						sks[num3].skillStand[j].e0dy = dataInputStream.readShort();
    						sks[num3].skillStand[j].effS1Id = dataInputStream.readShort();
    						sks[num3].skillStand[j].e1dx = dataInputStream.readShort();
    						sks[num3].skillStand[j].e1dy = dataInputStream.readShort();
    						sks[num3].skillStand[j].effS2Id = dataInputStream.readShort();
    						sks[num3].skillStand[j].e2dx = dataInputStream.readShort();
    						sks[num3].skillStand[j].e2dy = dataInputStream.readShort();
    						sks[num3].skillStand[j].arrowId = dataInputStream.readShort();
    						sks[num3].skillStand[j].adx = dataInputStream.readShort();
    						sks[num3].skillStand[j].ady = dataInputStream.readShort();
    					}
    					sks[num3].skillfly = new SkillInfoPaint[dataInputStream.readByte()];
    					for (int k = 0; k < sks[num3].skillfly.Length; k++)
    					{
    						sks[num3].skillfly[k] = new SkillInfoPaint();
    						sks[num3].skillfly[k].status = dataInputStream.readByte();
    						sks[num3].skillfly[k].effS0Id = dataInputStream.readShort();
    						sks[num3].skillfly[k].e0dx = dataInputStream.readShort();
    						sks[num3].skillfly[k].e0dy = dataInputStream.readShort();
    						sks[num3].skillfly[k].effS1Id = dataInputStream.readShort();
    						sks[num3].skillfly[k].e1dx = dataInputStream.readShort();
    						sks[num3].skillfly[k].e1dy = dataInputStream.readShort();
    						sks[num3].skillfly[k].effS2Id = dataInputStream.readShort();
    						sks[num3].skillfly[k].e2dx = dataInputStream.readShort();
    						sks[num3].skillfly[k].e2dy = dataInputStream.readShort();
    						sks[num3].skillfly[k].arrowId = dataInputStream.readShort();
    						sks[num3].skillfly[k].adx = dataInputStream.readShort();
    						sks[num3].skillfly[k].ady = dataInputStream.readShort();
    					}
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham readSkill: " + ex.ToString());
    		}
    		finally
    		{
    			try
    			{
    				if (dataInputStream != null)
    				{
    					dataInputStream.close();
    				}
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham readskill: " + ex2.ToString());
    			}
    		}
    	}
    
    	public static GameScr gI()
    	{
    		if (instance == null)
    		{
    			instance = new GameScr();
    		}
    		return instance;
    	}
    
    	public static void clearGameScr()
    	{
    		instance = null;
    	}
    
    	public void loadGameScr()
    	{
    		loadSplash();
    		Res.init();
    		loadInforBar();
    	}
    
    	public void doMenuInforMe()
    	{
    		scrMain.clear();
    		scrInfo.clear();
    		isViewNext = false;
    		cmdBag = new Command(mResources.MENUME[0], 1100011);
    		cmdSkill = new Command(mResources.MENUME[1], 1100012);
    		cmdTiemnang = new Command(mResources.MENUME[2], 1100013);
    		cmdInfo = new Command(mResources.MENUME[3], 1100014);
    		cmdtrangbi = new Command(mResources.MENUME[4], 1100015);
    		MyVector myVector = new MyVector();
    		myVector.addElement(cmdBag);
    		myVector.addElement(cmdSkill);
    		myVector.addElement(cmdTiemnang);
    		myVector.addElement(cmdInfo);
    		myVector.addElement(cmdtrangbi);
    		GameCanvas.menu.startAt(myVector, 3);
    	}
    
    	public void doMenusynthesis()
    	{
    		MyVector myVector = new MyVector();
    		myVector.addElement(new Command(mResources.SYNTHESIS[0], 110002));
    		myVector.addElement(new Command(mResources.SYNTHESIS[1], 1100032));
    		myVector.addElement(new Command(mResources.SYNTHESIS[2], 1100033));
    		GameCanvas.menu.startAt(myVector, 3);
    	}
    
    	public static void loadCamera(bool fullmScreen, int cx, int cy)
    	{
    		gW = GameCanvas.w;
    		cmdBarH = 39;
    		gH = GameCanvas.h;
    		cmdBarW = gW;
    		cmdBarX = 0;
    		cmdBarY = GameCanvas.h - Paint.hTab - cmdBarH;
    		girlHPBarY = 0;
    		csPadMaxH = GameCanvas.h / 6;
    		if (csPadMaxH < 48)
    		{
    			csPadMaxH = 48;
    		}
    		gW2 = gW >> 1;
    		gH2 = gH >> 1;
    		gW3 = gW / 3;
    		gH3 = gH / 3;
    		gW23 = gH - 120;
    		gH23 = gH * 2 / 3;
    		gW34 = 3 * gW / 4;
    		gH34 = 3 * gH / 4;
    		gW6 = gW / 6;
    		gH6 = gH / 6;
    		gssw = gW / TileMap.size + 2;
    		gssh = gH / TileMap.size + 2;
    		if (gW % 24 != 0)
    		{
    			gssw++;
    		}
    		int mapW = (TileMap.tmw - 1) * TileMap.size;
    		int mapH = (TileMap.tmh - 1) * TileMap.size;
    		if (mapW <= gW)
    		{
    			cmxLim = (TileMap.pxw - gW) / 2;
    			cmx = (cmtoX = cmxLim);
    		}
    		else
    		{
    			cmxLim = mapW - gW;
    			if (cx == -1 && cy == -1)
    			{
    				if (Char.myCharz() != null)
    				{
    					cmx = (cmtoX = Char.myCharz().cx - gW2 + gW6 * Char.myCharz().cdir);
    				}
    			}
    			else
    			{
    				if (Char.myCharz() != null)
    				{
    					cmx = (cmtoX = cx - gW23 + gW6 * Char.myCharz().cdir);
    				}
    			}
    			if (cmx < 24)
    			{
    				cmx = (cmtoX = 24);
    			}
    			if (cmx > cmxLim)
    			{
    				cmx = (cmtoX = cmxLim);
    			}
    		}
    		if (mapH <= gH)
    		{
    			cmyLim = mapH - gH;
    			cmy = (cmtoY = cmyLim);
    		}
    		else
    		{
    			cmyLim = mapH - gH;
    			if (cx == -1 && cy == -1)
    			{
    				if (Char.myCharz() != null)
    				{
    					cmy = (cmtoY = Char.myCharz().cy - gH23);
    				}
    			}
    			else
    			{
    				cmy = (cmtoY = cy - gH23);
    			}
    			if (cmy < 0)
    			{
    				cmy = (cmtoY = 0);
    			}
    			if (cmy > cmyLim)
    			{
    				cmy = (cmtoY = cmyLim);
    			}
    		}
    		firstY = cmy;
    		gssx = cmx / TileMap.size - 1;
    		if (gssx < 0)
    		{
    			gssx = 0;
    		}
    		gssy = cmy / TileMap.size;
    		gssxe = gssx + gssw;
    		gssye = gssy + gssh;
    		if (gssy < 0)
    		{
    			gssy = 0;
    		}
    		if (gssxe > TileMap.tmw)
    		{
    			gssxe = TileMap.tmw;
    		}
    		if (gssye > TileMap.tmh)
    		{
    			gssye = TileMap.tmh;
    		}
    		TileMap.countx = (gssxe - gssx) * 4;
    		if (TileMap.countx > TileMap.tmw)
    		{
    			TileMap.countx = TileMap.tmw;
    		}
    		TileMap.county = (gssye - gssy) * 4;
    		if (TileMap.county > TileMap.tmh)
    		{
    			TileMap.county = TileMap.tmh;
    		}
    		TileMap.gssx = (Char.myCharz().cx - 2 * gW) / TileMap.size;
    		if (TileMap.gssx < 0)
    		{
    			TileMap.gssx = 0;
    		}
    		TileMap.gssxe = TileMap.gssx + TileMap.countx;
    		if (TileMap.gssxe > TileMap.tmw)
    		{
    			TileMap.gssxe = TileMap.tmw;
    		}
    		TileMap.gssy = (Char.myCharz().cy - 2 * gH) / TileMap.size;
    		if (TileMap.gssy < 0)
    		{
    			TileMap.gssy = 0;
    		}
    		TileMap.gssye = TileMap.gssy + TileMap.county;
    		if (TileMap.gssye > TileMap.tmh)
    		{
    			TileMap.gssye = TileMap.tmh;
    		}
    		ChatTextField.gI().parentScreen = instance;
    		ChatTextField.gI().tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
    		ChatTextField.gI().initChatTextField();
    		updateUIPositions();
    		disXC = ((GameCanvas.w <= 200) ? 30 : 40);
    		if (Rms.loadRMSInt("viewchat") == -1)
    		{
    			GameCanvas.panel.isViewChatServer = true;
    		}
    		else
    		{
    			GameCanvas.panel.isViewChatServer = Rms.loadRMSInt("viewchat") == 1;
    		}
    	}
    }
}

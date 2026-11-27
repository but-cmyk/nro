namespace Game1
{
    using System;
    using Game1.Assets.src.e;
    using Game1.Assets.src.g;
    
    public partial class Char : IMapObject
    {
    
    	public int tFusion;
    
    	public bool huytSao;
    
    	public bool blindEff;
    
    	public bool telePortSkill;
    
    	public bool sleepEff;
    
    	public bool stone;
    
    	public int perCentMp = 100;
    
    	public long dHP;
    
    	public int headTemp = -1;
    
    	public int bodyTemp = -1;
    
    	public int legTemp = -1;
    
    	public int bagTemp = -1;
    
    	public int wpTemp = -1;
    
    	public MyVector vEffChar = new MyVector("vEff");
    
    	public static FrameImage fraRedEye;
    
    	private int fChopmat;
    
    	private bool isAddChopMat;
    
    	private long timeAddChopmat;
    
    	private int[] frChopNhanh = new int[34]
    	{
    		-1, -1, -1, -1, 0, 0, 1, 1, 0, 0,
    		1, 1, 0, 0, 1, 1, 0, 0, 1, 1,
    		0, 0, 1, 1, 0, 0, 1, 1, 0, 0,
    		-1, -1, -1, -1
    	};
    
    	private int[] frChopCham = new int[23]
    	{
    		-1, -1, -1, -1, 0, 0, 1, 1, 1, 0,
    		0, 1, 1, 1, 0, 0, 1, 1, 1, -1,
    		-1, -1, -1
    	};
    
    	private int[] frEye = new int[30]
    	{
    		-1, -1, 0, 0, 1, 1, 0, 0, 1, 1,
    		0, 0, 1, 1, 0, 0, 1, 1, 0, 0,
    		1, 1, 0, 0, 1, 1, 0, 0, -1, -1
    	};
    
    	public static int[][] Arr_Head_2Fr = new int[1][] { new int[2] { 542, 543 } };
    
    	private int fHead;
    
    	private string strEffAura = "aura_";
    
    	public short idAuraEff = -1;
    
    	public static bool isPaintAura = true;
    
    	public static bool isPaintAura2 = true;
    
    	private FrameImage fraEff;
    
    	private FrameImage fraEffSub;
    
    	private string strEff_Set_Item = "set_eff_";
    
    	public short idEff_Set_Item = -1;
    
    	private FrameImage fraHat_behind;
    
    	private FrameImage fraHat_font;
    
    	private FrameImage fraHat_behind_2;
    
    	private FrameImage fraHat_font_2;
    
    	private string strHat_behind = "hat_sau_";
    
    	private string strHat_font = "hat_truoc_";
    
    	private string strNgang = "ngang_";
    
    	public short idHat = -1;
    
    	public static int[][] hatInfo = new int[32][]
    	{
    		new int[2] { 5, -7 },
    		new int[2] { 5, -7 },
    		new int[2] { 5, -8 },
    		new int[2] { 5, -7 },
    		new int[2] { 5, -6 },
    		new int[2] { 5, -8 },
    		new int[2] { 5, -7 },
    		new int[2] { 9, 0 },
    		new int[2] { 11, 1 },
    		new int[2] { 4, 0 },
    		new int[2] { 4, -1 },
    		new int[2] { 4, 8 },
    		new int[2] { 6, 5 },
    		new int[2] { 6, -6 },
    		new int[2] { 2, -5 },
    		new int[2] { 7, -8 },
    		new int[2] { 7, -6 },
    		new int[2] { 8, 0 },
    		new int[2] { 7, 5 },
    		new int[2] { 9, -7 },
    		new int[2] { 7, -3 },
    		new int[2] { 2, 8 },
    		new int[2] { 4, 5 },
    		new int[2] { 10, -5 },
    		new int[2] { 9, -5 },
    		new int[2] { 9, -5 },
    		new int[2] { 6, -6 },
    		new int[2] { 2, -5 },
    		new int[2] { 7, -8 },
    		new int[2] { 7, -6 },
    		new int[2] { 9, -7 },
    		new int[2] { 7, -3 }
    	};
    
    	public static short[] Arr_Head_FlyMove = new short[0];
    
    	public const byte TYPE_SKILL_KAMEX10 = 1;
    
    	public const byte TYPE_SKILL_FINAL = 2;
    
    	public const byte TYPE_SKILL_MAFUBA = 3;
    
    	public const byte TYPE_SKILL_GENKI = 4;
    
    	public bool isPaintNewSkill;
    
    	private bool isFly;
    
    	private long timeReset_newSkill;
    
    	private sbyte typeFrame;
    
    	private short idskillPaint;
    
    	private byte[] fr_start;
    
    	private byte[] fr_atk;
    
    	private byte[] fr_end;
    
    	private int count_NEW;
    
    	private int stt;
    
    	private short rangeDame;
    
    	private sbyte typePaint;
    
    	private sbyte typeItem;
    
    	private Point targetDame;
    
    	private long timeDame;
    
    	public bool isMafuba;
    
    	private short countMafuba;
    
    	public int xMFB;
    
    	public int yMFB;
    
    	public int timeGongSkill;
    
    	private FrameImage fraDanhHieu;
    
    	private MainImage mainImg;
    
    	public Char()
    	{
    		statusMe = 6;
    	}
    
    	public void applyCharLevelPercent()
    	{
    		try
    		{
    			long num = 1L;
    			long num2 = 0L;
    			int num3 = 0;
    			for (int num4 = GameScr.exps.Length - 1; num4 >= 0; num4--)
    			{
    				if (cPower >= GameScr.exps[num4])
    				{
    					num = ((num4 != GameScr.exps.Length - 1) ? (GameScr.exps[num4 + 1] - GameScr.exps[num4]) : 1);
    					num2 = cPower - GameScr.exps[num4];
    					num3 = num4;
    					break;
    				}
    			}
    			clevel = num3;
    			cLevelPercent = (int)(num2 * 10000 / num);
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi char level percent: " + ex.ToString());
    		}
    	}
    
    	public int getdxSkill()
    	{
    		if (myskill != null)
    		{
    			return myskill.dx;
    		}
    		return 0;
    	}
    
    	public int getdySkill()
    	{
    		if (myskill != null)
    		{
    			return myskill.dy;
    		}
    		return 0;
    	}
    
    	public static void taskAction(bool isNextStep)
    	{
    		Task task = myCharz().taskMaint;
    		if (task.index > task.contentInfo.Length - 1)
    		{
    			task.index = task.contentInfo.Length - 1;
    		}
    		string text = task.contentInfo[task.index];
    		if (text != null && !text.Equals(string.Empty))
    		{
    			if (text.StartsWith("#"))
    			{
    				text = NinjaUtil.replace(text, "#", string.Empty);
    				Npc npc = new Npc(5, 0, -100, -100, 5, GameScr.info1.charId[myCharz().cgender][2]);
    				npc.cx = (npc.cy = -100);
    				npc.avatar = GameScr.info1.charId[myCharz().cgender][2];
    				npc.charID = 5;
    				if (GameCanvas.currentScreen == GameScr.instance)
    				{
    					ChatPopup.addNextPopUpMultiLine(text, npc);
    				}
    			}
    			else if (isNextStep)
    			{
    				GameScr.info1.addInfo(text, 0);
    			}
    		}
    		GameScr.isHaveSelectSkill = true;
    		Cout.println("TASKx " + myCharz().taskMaint.taskId);
    		if (myCharz().taskMaint.taskId <= 2)
    		{
    			myCharz().canFly = false;
    		}
    		else
    		{
    			myCharz().canFly = true;
    		}
    		GameScr.gI().left = null;
    		if (task.taskId == 0)
    		{
    			Hint.isViewMap = false;
    			Hint.isViewPotential = false;
    			GameScr.gI().right = null;
    			GameScr.isHaveSelectSkill = false;
    			GameScr.gI().left = null;
    			if (task.index < 4)
    			{
    				MagicTree.isPaint = false;
    				GameScr.isPaintRada = -1;
    			}
    			if (task.index == 4)
    			{
    				GameScr.isPaintRada = 1;
    				MagicTree.isPaint = true;
    			}
    			if (task.index >= 5)
    			{
    				GameScr.gI().right = GameScr.gI().cmdFocus;
    			}
    		}
    		if (task.taskId == 1)
    		{
    			GameScr.isHaveSelectSkill = true;
    		}
    		if (task.taskId >= 1)
    		{
    			GameScr.gI().right = GameScr.gI().cmdFocus;
    			GameScr.gI().left = GameScr.gI().cmdMenu;
    		}
    		if (task.taskId >= 0)
    		{
    			Panel.isPaintMap = true;
    		}
    		else
    		{
    			Panel.isPaintMap = false;
    		}
    		if (task.taskId < 12)
    		{
    			GameCanvas.panel.mainTabName = mResources.mainTab1;
    		}
    		else
    		{
    			GameCanvas.panel.mainTabName = mResources.mainTab2;
    		}
    		GameCanvas.panel.tabName[0] = GameCanvas.panel.mainTabName;
    		if (myChar.taskMaint.taskId > 10)
    		{
    			Rms.saveRMSString("fake", "aa");
    		}
    	}
    
    	public string getStrLevel()
    	{
    		string text = strLevel[clevel] + "+" + cLevelPercent / 100 + "." + cLevelPercent % 100 + "%";
    		if (text.Length > 23 && text.IndexOf("cấp ") >= 0)
    		{
    			text = Res.replace(text, "cấp ", "c");
    		}
    		return text;
    	}
    
    	public int avatarz()
    	{
    		return getAvatar(head);
    	}
    
    	public int getAvatar(int headId)
    	{
    		for (int i = 0; i < idHead.Length; i++)
    		{
    			if (headId == idHead[i])
    			{
    				return idAvatar[i];
    			}
    		}
    		return -1;
    	}
    
    	public void setPowerInfo(string info, short p, short maxP, short sc)
    	{
    		powerPoint = p;
    		strInfo = info;
    		maxPowerPoint = maxP;
    		secondPower = sc;
    		lastS = (currS = mSystem.currentTimeMillis());
    	}
    
    	public void addInfo(string info)
    	{
    		if (chatInfo == null)
    		{
    			chatInfo = new Info();
    		}
    		Char cInfo = null;
    		chatInfo.addInfo(info, 0, cInfo, false);
    	}
    
    	public int getSys()
    	{
    		if (nClass.classId == 1 || nClass.classId == 2)
    		{
    			return 1;
    		}
    		if (nClass.classId == 3 || nClass.classId == 4)
    		{
    			return 2;
    		}
    		if (nClass.classId == 5 || nClass.classId == 6)
    		{
    			return 3;
    		}
    		return 0;
    	}
    
    	public static Char myCharz()
    	{
    		if (myChar == null)
    		{
    			myChar = new Char();
    			myChar.me = true;
    			myChar.cmtoChar = true;
    		}
    		return myChar;
    	}
    
    	public static Char myPetz()
    	{
    		if (myPet == null)
    		{
    			myPet = new Char();
    			myPet.me = false;
    		}
    		return myPet;
    	}
    
    	public static void clearMyChar()
    	{
    		myChar = null;
    	}
    
    	public void bagSort()
    	{
    		try
    		{
    			MyVector myVector = new MyVector();
    			for (int i = 0; i < arrItemBag.Length; i++)
    			{
    				Item item = arrItemBag[i];
    				if (item != null && item.template.isUpToUp && !item.isExpires)
    				{
    					myVector.addElement(item);
    				}
    			}
    			for (int j = 0; j < myVector.size(); j++)
    			{
    				Item item2 = (Item)myVector.elementAt(j);
    				if (item2 == null)
    				{
    					continue;
    				}
    				for (int k = j + 1; k < myVector.size(); k++)
    				{
    					Item item3 = (Item)myVector.elementAt(k);
    					if (item3 != null && item2.template.Equals(item3.template) && item2.isLock == item3.isLock)
    					{
    						item2.quantity += item3.quantity;
    						arrItemBag[item3.indexUI] = null;
    						myVector.setElementAt(null, k);
    					}
    				}
    			}
    			for (int l = 0; l < arrItemBag.Length; l++)
    			{
    				if (arrItemBag[l] == null)
    				{
    					continue;
    				}
    				for (int m = 0; m <= l; m++)
    				{
    					if (arrItemBag[m] == null)
    					{
    						arrItemBag[m] = arrItemBag[l];
    						arrItemBag[m].indexUI = m;
    						arrItemBag[l] = null;
    						break;
    					}
    				}
    			}
    		}
    		catch (Exception)
    		{
    			Cout.println("Char.bagSort()");
    		}
    	}
    
    	public void boxSort()
    	{
    		try
    		{
    			MyVector myVector = new MyVector();
    			for (int i = 0; i < arrItemBox.Length; i++)
    			{
    				Item item = arrItemBox[i];
    				if (item != null && item.template.isUpToUp && !item.isExpires)
    				{
    					myVector.addElement(item);
    				}
    			}
    			for (int j = 0; j < myVector.size(); j++)
    			{
    				Item item2 = (Item)myVector.elementAt(j);
    				if (item2 == null)
    				{
    					continue;
    				}
    				for (int k = j + 1; k < myVector.size(); k++)
    				{
    					Item item3 = (Item)myVector.elementAt(k);
    					if (item3 != null && item2.template.Equals(item3.template) && item2.isLock == item3.isLock)
    					{
    						item2.quantity += item3.quantity;
    						arrItemBox[item3.indexUI] = null;
    						myVector.setElementAt(null, k);
    					}
    				}
    			}
    			for (int l = 0; l < arrItemBox.Length; l++)
    			{
    				if (arrItemBox[l] == null)
    				{
    					continue;
    				}
    				for (int m = 0; m <= l; m++)
    				{
    					if (arrItemBox[m] == null)
    					{
    						arrItemBox[m] = arrItemBox[l];
    						arrItemBox[m].indexUI = m;
    						arrItemBox[l] = null;
    						break;
    					}
    				}
    			}
    		}
    		catch (Exception)
    		{
    			Cout.println("Char.boxSort()");
    		}
    	}
    
    	public void useItem(int indexUI)
    	{
    		Item item = arrItemBag[indexUI];
    		if (!item.isTypeBody())
    		{
    			return;
    		}
    		item.isLock = true;
    		item.typeUI = 5;
    		Item item2 = arrItemBody[item.template.type];
    		arrItemBag[indexUI] = null;
    		if (item2 != null)
    		{
    			item2.typeUI = 3;
    			arrItemBody[item.template.type] = null;
    			item2.indexUI = indexUI;
    			arrItemBag[indexUI] = item2;
    		}
    		item.indexUI = item.template.type;
    		arrItemBody[item.indexUI] = item;
    		for (int i = 0; i < arrItemBody.Length; i++)
    		{
    			Item item3 = arrItemBody[i];
    			if (item3 != null)
    			{
    				if (item3.template.type == 0)
    				{
    					body = item3.template.part;
    				}
    				else if (item3.template.type == 1)
    				{
    					leg = item3.template.part;
    				}
    			}
    		}
    	}
    
    	public Skill getSkill(SkillTemplate skillTemplate)
    	{
    		for (int i = 0; i < vSkill.size(); i++)
    		{
    			if (((Skill)vSkill.elementAt(i)).template.id == skillTemplate.id)
    			{
    				return (Skill)vSkill.elementAt(i);
    			}
    		}
    		return null;
    	}
    
    	public Waypoint isInEnterOfflinePoint()
    	{
    		Task task = myChar.taskMaint;
    		if (task != null && task.taskId == 0 && task.index < 6)
    		{
    			return null;
    		}
    		int num = TileMap.vGo.size();
    		for (sbyte b = 0; b < num; b++)
    		{
    			Waypoint waypoint = (Waypoint)TileMap.vGo.elementAt(b);
    			if (PopUp.vPopups.size() >= num)
    			{
    				PopUp popUp = (PopUp)PopUp.vPopups.elementAt(b);
    				if (!popUp.isPaint)
    				{
    					return null;
    				}
    			}
    			if (cx >= waypoint.minX && cx <= waypoint.maxX && cy >= waypoint.minY && cy <= waypoint.maxY && waypoint.isEnter && waypoint.isOffline)
    			{
    				return waypoint;
    			}
    		}
    		return null;
    	}
    
    	public Waypoint isInEnterOnlinePoint()
    	{
    		Task task = myChar.taskMaint;
    		if (task != null && task.taskId == 0 && task.index < 6)
    		{
    			return null;
    		}
    		int num = TileMap.vGo.size();
    		for (sbyte b = 0; b < num; b++)
    		{
    			Waypoint waypoint = (Waypoint)TileMap.vGo.elementAt(b);
    			if (PopUp.vPopups.size() >= num)
    			{
    				PopUp popUp = (PopUp)PopUp.vPopups.elementAt(b);
    				if (!popUp.isPaint)
    				{
    					return null;
    				}
    			}
    			if (cx >= waypoint.minX && cx <= waypoint.maxX && cy >= waypoint.minY && cy <= waypoint.maxY && waypoint.isEnter && !waypoint.isOffline)
    			{
    				return waypoint;
    			}
    		}
    		return null;
    	}
    
    	public bool isInWaypoint()
    	{
    		if (TileMap.isInAirMap() && cy >= TileMap.pxh - 48)
    		{
    			return true;
    		}
    		if (isTeleport || isUsePlane)
    		{
    			return false;
    		}
    		int num = TileMap.vGo.size();
    		for (sbyte b = 0; b < num; b++)
    		{
    			Waypoint waypoint = (Waypoint)TileMap.vGo.elementAt(b);
    			if ((TileMap.mapID == 47 || TileMap.isInAirMap()) && cy <= waypoint.minY + waypoint.maxY && cx > waypoint.minX && cx < waypoint.maxX)
    			{
    				if (TileMap.isInAirMap() && cTypePk != 0)
    				{
    					return false;
    				}
    				return true;
    			}
    			if (cx >= waypoint.minX && cx <= waypoint.maxX && cy >= waypoint.minY && cy <= waypoint.maxY && !waypoint.isEnter)
    			{
    				return true;
    			}
    		}
    		return false;
    	}
    
    	public bool isPunchKickSkill()
    	{
    		if (skillPaint == null)
    		{
    			return false;
    		}
    		if (skillPaint.id >= 0 && skillPaint.id <= 6)
    		{
    			return true;
    		}
    		if (skillPaint.id >= 14 && skillPaint.id <= 20)
    		{
    			return true;
    		}
    		if (skillPaint.id >= 28 && skillPaint.id <= 34)
    		{
    			return true;
    		}
    		if (skillPaint.id >= 63 && skillPaint.id <= 69)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void soundUpdate()
    	{
    		if (me && statusMe == 10 && cf == 8 && ty > 20 && GameCanvas.gameTick % 20 == 0)
    		{
    			SoundMn.gI().charFly();
    		}
    		if (skillPaint != null && skillInfoPaint() != null && indexSkill < skillInfoPaint().Length && isPunchKickSkill() && (me || (!me && cx >= GameScr.cmx && cx <= GameScr.cmx + GameCanvas.w)) && GameCanvas.gameTick % 5 == 0)
    		{
    			if (cf == 9 || cf == 10 || cf == 11)
    			{
    				SoundMn.gI().charPunch(true, (!me) ? 0.05f : 0.1f);
    			}
    			else
    			{
    				SoundMn.gI().charPunch(false, (!me) ? 0.05f : 0.1f);
    			}
    		}
    	}
    
    	public void updateChargeSkill()
    	{
    	}
    }
}

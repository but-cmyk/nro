using System.Collections.Generic;

namespace Game1
{
    public class ItemTime : IActionListener
    {
    	public short idIcon;
    
    	public int second;
    
    	public int minute;
    
    	private long curr;
    
    	private long last;
    
    	private bool isText;
    
    	private bool dontClear;
    
    	private string text;
    
    	private bool isPaint_coolDownBar;
    
    	public int time;
    
    	public int coutTime;
    
    	private int per = 100;
    
    	public ItemTime()
    	{
    	}
    
    	public ItemTime(short idIcon, int s)
    	{
    		this.idIcon = idIcon;
    		minute = s / 60;
    		second = s % 60;
    		time = s;
    		coutTime = s;
    		curr = (last = mSystem.currentTimeMillis());
    		isPaint_coolDownBar = idIcon == 14;
    	}
    
    	public void initTimeText(sbyte id, string text, int time)
    	{
    		if (time == -1)
    		{
    			dontClear = true;
    		}
    		else
    		{
    			dontClear = false;
    		}
    		isText = true;
    		minute = time / 60;
    		second = time % 60;
    		idIcon = id;
    		this.time = time;
    		coutTime = time;
    		this.text = text;
    		curr = (last = mSystem.currentTimeMillis());
    		isPaint_coolDownBar = idIcon == 14;
    	}
    
    	public void initTime(int time, bool isText)
    	{
    		minute = time / 60;
    		second = time % 60;
    		this.time = time;
    		coutTime = time;
    		this.isText = isText;
    		curr = (last = mSystem.currentTimeMillis());
    	}
    
    	public static bool isExistItem(int id)
    	{
    		for (int i = 0; i < Char.vItemTime.size(); i++)
    		{
    			ItemTime itemTime = (ItemTime)Char.vItemTime.elementAt(i);
    			if (itemTime.idIcon == id)
    			{
    				return true;
    			}
    		}
    		return false;
    	}
    
    	public static ItemTime getMessageById(int id)
    	{
    		for (int i = 0; i < GameScr.textTime.size(); i++)
    		{
    			ItemTime itemTime = (ItemTime)GameScr.textTime.elementAt(i);
    			if (itemTime.idIcon == id)
    			{
    				return itemTime;
    			}
    		}
    		return null;
    	}
    
    	public static bool isExistMessage(int id)
    	{
    		for (int i = 0; i < GameScr.textTime.size(); i++)
    		{
    			ItemTime itemTime = (ItemTime)GameScr.textTime.elementAt(i);
    			if (itemTime.idIcon == id)
    			{
    				return true;
    			}
    		}
    		return false;
    	}
    
    	public static ItemTime getItemById(int id)
    	{
    		for (int i = 0; i < Char.vItemTime.size(); i++)
    		{
    			ItemTime itemTime = (ItemTime)Char.vItemTime.elementAt(i);
    			if (itemTime.idIcon == id)
    			{
    				return itemTime;
    			}
    		}
    		return null;
    	}
    
    	public void initTime(int time)
    	{
    		minute = time / 60;
    		second = time % 60;
    		coutTime = time;
    		curr = (last = mSystem.currentTimeMillis());
    	}
    
    	public void paint(mGraphics g, int x, int y)
    	{
    		SmallImage.drawSmallImage(g, idIcon, x, y, 0, 3);
    		string empty = string.Empty;
    		empty = minute + "'";
    		if (minute == 0)
    		{
    			empty = second + "s";
    		}
    		mFont.tahoma_7b_white.drawString(g, empty, x, y + 15, 2, mFont.tahoma_7b_dark);
    	}
    
    	public void paintText(mGraphics g, int x, int y)
    	{
    		if (isPaint_coolDownBar)
    		{
    			if (Char.myCharz() != null)
    			{
    				int num = 80;
    				int x2 = GameCanvas.w / 2 - num / 2;
    				int y2 = GameCanvas.h - 80;
    				g.setColor(8421504);
    				g.fillRect(x2, y2, num, 2);
    				g.setColor(16777215);
    				if (per > 0)
    				{
    					g.fillRect(x2, y2, num * per / 100, 2);
    				}
    			}
    			return;
    		}
    		string empty = string.Empty;
    		empty = minute + "'";
    		if (minute < 1)
    		{
    			empty = second + "s";
    		}
    		if (minute < 0)
    		{
    			empty = string.Empty;
    		}
    		if (dontClear)
    		{
    			empty = string.Empty;
    		}
    		mFont.tahoma_7b_white.drawString(g, text + " " + empty, x, y, 0, mFont.tahoma_7b_dark);
    	}
    
    	public void update()
    	{
    		curr = mSystem.currentTimeMillis();
    		if (curr - last >= 1000)
    		{
    			last = mSystem.currentTimeMillis();
    			second--;
    			coutTime--;
    			if (second <= 0)
    			{
    				second = 60;
    				minute--;
    			}
    			if (time > 0)
    			{
    				per = coutTime * 100 / time;
    			}
    		}
    		if (minute < 0 && !isText)
    		{
    			if (isAutoUse(idIcon))
    			{
    				triggerAutoUse(idIcon);
    			}
    			if (idIcon == 433)
    			{
    				if (Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID >= 5000 && Char.myCharz().petFollow.smallID <= 5003)
    				{
    					Char.myCharz().petFollow.remove();
    					Char.myCharz().petFollow = null;
    				}
    			}
    			Char.vItemTime.removeElement(this);
    		}
    		if (minute < 0 && isText && !dontClear)
    		{
    			GameScr.textTime.removeElement(this);
    		}
    	}

    	// =========================================================================
    	// AUTO USE ITEM TIME SYSTEM
    	// =========================================================================
    	public static HashSet<short> autoUseIconIds = new HashSet<short>();
    	public static long lastTimeCheckAutoUse;

    	public static bool isAutoUse(short iconId)
    	{
    		return autoUseIconIds.Contains(iconId);
    	}

    	public static void toggleAutoUse(short iconId)
    	{
    		if (autoUseIconIds.Contains(iconId))
    		{
    			autoUseIconIds.Remove(iconId);
    			if (GameScr.info1 != null)
    			{
    				GameScr.info1.addInfo("|1|Đã tắt tự động dùng: " + getBuffName(iconId), 0);
    			}
    		}
    		else
    		{
    			autoUseIconIds.Add(iconId);
    			if (GameScr.info1 != null)
    			{
    				GameScr.info1.addInfo("|2|Đã bật tự động dùng: " + getBuffName(iconId), 0);
    			}
    		}
    	}

    	public void showMenuAutoUse()
    	{
    		MyVector menuItems = new MyVector();
    		bool isAuto = isAutoUse(idIcon);
    		string toggleCaption = isAuto ? "Tắt Auto Dùng" : "Bật Auto Dùng";
    		string timeStr = (minute > 0 ? (minute + "p ") : "") + second + "s";
    		string infoTitle = getBuffName(idIcon) + " (" + timeStr + ")";
    		menuItems.addElement(new Command(infoTitle, this, 0, null));
    		menuItems.addElement(new Command(toggleCaption, this, 1, this));
    		menuItems.addElement(new Command("Đóng", this, 2, null));
    		GameCanvas.menu.startAt(menuItems, 3);
    	}

    	public void perform(int idAction, object p)
    	{
    		GameCanvas.menu.doCloseMenu();
    		if (idAction == 1)
    		{
    			toggleAutoUse(idIcon);
    		}
    	}

    	public static string getBuffName(short iconId)
    	{
    		switch (iconId)
    		{
    			case 2754: return "Cuồng Nộ";
    			case 2755: return "Bổ Huyết";
    			case 2756: return "Bổ Khí";
    			case 2757: return "Giáp Xên";
    			case 2760: return "Ẩn Danh";
    			case 2758: return "Máy Dò Capsule";
    			case 931:  return "Cà Chua";
    			case 933:  return "Cà Rốt";
    			case 7149: return "Khẩu Trang";
    			case 4387: return "Tự Động Luyện Tập";
    			case 10716: return "Cuồng Nộ 2";
    			case 10714: return "Bổ Huyết 2";
    			case 10715: return "Bổ Khí 2";
    			case 10712: return "Giáp Xên 2";
    			case 10717: return "Ẩn Danh 2";
    			case 11474: return "Bùa TNSM";
    			case 13540: return "Tiềm Năng Đệ Tử";
    			case 10905: return "Xí Muội Hoa Đào";
    			case 10904: return "Xí Muội Hoa Mai";
    			case 11173: return "Nước Mía NCD";
    			case 5829: return "Cuồng Nộ Cấp 2";
    			default:   return "Vật Phẩm Bổ Trợ";
    		}
    	}

    	public static int[] getItemTemplatesForIcon(short iconId)
    	{
    		switch (iconId)
    		{
    			case 2754: return new int[] { 381, 1099, 1100, 1101, 1102 };
    			case 2755: return new int[] { 382, 1099, 1100, 1101, 1102 };
    			case 2756: return new int[] { 383, 1099, 1100, 1101, 1102 };
    			case 2757: return new int[] { 384, 1099, 1100, 1101, 1102 };
    			case 2760: return new int[] { 385, 1099, 1100, 1101, 1102 };
    			case 2758: return new int[] { 379 };
    			case 931:  return new int[] { 191 };
    			case 933:  return new int[] { 192 };
    			case 7149: return new int[] { 865 };
    			case 4387: return new int[] { 521 };
    			case 10716: return new int[] { 1099, 1100, 1101, 1102 };
    			case 10714: return new int[] { 1099, 1100, 1101, 1102 };
    			case 10715: return new int[] { 1099, 1100, 1101, 1102 };
    			case 10712: return new int[] { 1099, 1100, 1101, 1102 };
    			case 10717: return new int[] { 1099, 1100, 1101, 1102 };
    			case 11474: return new int[] { 1143 };
    			case 13540: return new int[] { 1145 };
    			case 10905: return new int[] { 1090 };
    			case 10904: return new int[] { 1089 };
    			case 11173: return new int[] { 1117 };
    			case 5829: return new int[] { 582 };
    			default:   return null;
    		}
    	}

    	public static void triggerAutoUse(short iconId)
    	{
    		int[] itemIds = getItemTemplatesForIcon(iconId);
    		if (itemIds == null || itemIds.Length == 0)
    		{
    			return;
    		}

    		if (Char.myCharz() == null || Char.myCharz().arrItemBag == null)
    		{
    			return;
    		}

    		Item targetItem = null;
    		for (int i = 0; i < Char.myCharz().arrItemBag.Length; i++)
    		{
    			Item it = Char.myCharz().arrItemBag[i];
    			if (it != null && it.template != null)
    			{
    				for (int j = 0; j < itemIds.Length; j++)
    				{
    					if (it.template.id == itemIds[j])
    					{
    						targetItem = it;
    						break;
    					}
    				}
    				if (targetItem != null)
    				{
    					break;
    				}
    			}
    		}

    		if (targetItem != null)
    		{
    			Service.gI().useItem(0, 1, (sbyte)targetItem.indexUI, -1);
    			if (GameScr.info1 != null)
    			{
    				GameScr.info1.addInfo("|2|Tự động dùng: " + getBuffName(iconId), 0);
    			}
    		}
    		else
    		{
    			autoUseIconIds.Remove(iconId);
    			if (GameScr.info1 != null)
    			{
    				GameScr.info1.addInfo("|1|Đã hết " + getBuffName(iconId) + " để tự động dùng!", 0);
    			}
    		}
    	}

    	public static void checkAutoUsePeriodic()
    	{
    		if (autoUseIconIds == null || autoUseIconIds.Count == 0)
    		{
    			return;
    		}
    		long now = mSystem.currentTimeMillis();
    		if (now - lastTimeCheckAutoUse < 3000)
    		{
    			return;
    		}
    		lastTimeCheckAutoUse = now;

    		if (Char.myCharz() == null || Char.myCharz().statusMe == 14)
    		{
    			return;
    		}

    		List<short> toCheck = new List<short>(autoUseIconIds);
    		for (int i = 0; i < toCheck.Count; i++)
    		{
    			short iconId = toCheck[i];
    			if (!isExistItem(iconId))
    			{
    				triggerAutoUse(iconId);
    			}
    		}
    	}
    }
}

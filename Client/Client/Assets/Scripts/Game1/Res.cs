namespace Game1
{
    using System;
    using UnityEngine;
    
    public class Res
    {
    	private static short[] sinz = new short[91]
    	{
    		0, 18, 36, 54, 71, 89, 107, 125, 143, 160,
    		178, 195, 213, 230, 248, 265, 282, 299, 316, 333,
    		350, 367, 384, 400, 416, 433, 449, 465, 481, 496,
    		512, 527, 543, 558, 573, 587, 602, 616, 630, 644,
    		658, 672, 685, 698, 711, 724, 737, 749, 761, 773,
    		784, 796, 807, 818, 828, 839, 849, 859, 868, 878,
    		887, 896, 904, 912, 920, 928, 935, 943, 949, 956,
    		962, 968, 974, 979, 984, 989, 994, 998, 1002, 1005,
    		1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024,
    		1024
    	};
    
    	private static short[] cosz;
    
    	private static int[] tanz;
    
    	public static string[] LOG_CAT = new string[5]
    	{
    		"<color=#ff0000ff>[  LOG_CAT  ]</color>",
    		"<color=#ff0000ff>[LOG_SESSION]</color>",
    		"<color=#ffff00ff>[LOG_SESSION]</color>",
    		"<color=#ff0000ff>[LOG_MOBILE ]</color>",
    		string.Empty
    	};
    
    	public static int count;
    
    	public static bool isIcon;
    
    	public static bool isBig;
    
    	public static MyVector debug = new MyVector();
    
    	public static MyRandom r = new MyRandom();
    
    	public static void init()
    	{
    		cosz = new short[91];
    		tanz = new int[91];
    		for (int i = 0; i <= 90; i++)
    		{
    			cosz[i] = sinz[90 - i];
    			if (cosz[i] == 0)
    			{
    				tanz[i] = int.MaxValue;
    			}
    			else
    			{
    				tanz[i] = (sinz[i] << 10) / cosz[i];
    			}
    		}
    	}
    
    	public static int sin(int a)
    	{
    		a = fixangle(a);
    		if (a >= 0 && a < 90)
    		{
    			return sinz[a];
    		}
    		if (a >= 90 && a < 180)
    		{
    			return sinz[180 - a];
    		}
    		if (a >= 180 && a < 270)
    		{
    			return -sinz[a - 180];
    		}
    		return -sinz[360 - a];
    	}
    
    	public static int cos(int a)
    	{
    		a = fixangle(a);
    		if (a >= 0 && a < 90)
    		{
    			return cosz[a];
    		}
    		if (a >= 90 && a < 180)
    		{
    			return -cosz[180 - a];
    		}
    		if (a >= 180 && a < 270)
    		{
    			return -cosz[a - 180];
    		}
    		return cosz[360 - a];
    	}
    
    	public static int tan(int a)
    	{
    		a = fixangle(a);
    		if (a >= 0 && a < 90)
    		{
    			return tanz[a];
    		}
    		if (a >= 90 && a < 180)
    		{
    			return -tanz[180 - a];
    		}
    		if (a >= 180 && a < 270)
    		{
    			return tanz[a - 180];
    		}
    		return -tanz[360 - a];
    	}
    
    	public static int atan(int a)
    	{
    		for (int i = 0; i <= 90; i++)
    		{
    			if (tanz[i] >= a)
    			{
    				return i;
    			}
    		}
    		return 0;
    	}
    
    	public static int angle(int dx, int dy)
    	{
    		int num;
    		if (dx != 0)
    		{
    			int a = Math.abs((dy << 10) / dx);
    			num = atan(a);
    			if (dy >= 0 && dx < 0)
    			{
    				num = 180 - num;
    			}
    			if (dy < 0 && dx < 0)
    			{
    				num = 180 + num;
    			}
    			if (dy < 0 && dx >= 0)
    			{
    				num = 360 - num;
    			}
    		}
    		else
    		{
    			num = ((dy <= 0) ? 270 : 90);
    		}
    		return num;
    	}
    
    	public static int fixangle(int angle)
    	{
    		if (angle >= 360)
    		{
    			angle -= 360;
    		}
    		if (angle < 0)
    		{
    			angle += 360;
    		}
    		return angle;
    	}
    
    	public static sbyte[] TakeSnapShot()
    	{
    		return null;
    	}
    
    	public static void outz(string s)
    	{
    		if (mSystem.isTest && !string.IsNullOrEmpty(s) && UnityLogger.ShouldLog(s, LogType.Log))
    		{
    			Debug.Log(s);
    		}
    	}
    
    	public static void outz(string s, int logIndex)
    	{
    		if (mSystem.isTest && !string.IsNullOrEmpty(s))
    		{
    			string full = ((logIndex >= 0 && logIndex < LOG_CAT.Length) ? LOG_CAT[logIndex] : string.Empty) + s;
    			if (UnityLogger.ShouldLog(full, LogType.Log))
    			{
    				Debug.Log(full);
    			}
    		}
    	}
    
    	public static void err(string s)
    	{
    		if (string.IsNullOrEmpty(s))
    		{
    			return;
    		}
    		// Chỉ xuất icon lỗi đỏ (Debug.LogError) khi thực sự có dấu hiệu lỗi/ngoại lệ để tránh cảnh báo giả làm đỏ Console
    		if (s.IndexOf("error", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    s.IndexOf("loi", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    s.IndexOf("fail", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    s.IndexOf("exception", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    s.IndexOf("cannot", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    s.IndexOf("err:", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    s.IndexOf("err ", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    s.StartsWith("err", System.StringComparison.OrdinalIgnoreCase))
    		{
    			if (UnityLogger.ShouldLog(s, LogType.Error))
    			{
    				Debug.LogError(s);
    			}
    		}
    		else
    		{
    			outz(s);
    		}
    	}
    
    	public static void outz2(string s)
    	{
    	}
    
    	public static string[] debugLogs = new string[6];
    	public static int debugLogCount = 0;
    	public static string debugLastFile = string.Empty;
    	public static string debugLastError = string.Empty;
    	public static bool showDebugHUD = true;

    	public static void onScreenDebug(string s)
    	{
    		if (s == null) return;
    		try
    		{
    			UnityEngine.Debug.Log("[NRO_HUD] " + s);
    			// Đẩy vào circular log array (tối đa 6 dòng)
    			for (int i = debugLogs.Length - 1; i > 0; i--)
    			{
    				debugLogs[i] = debugLogs[i - 1];
    			}
    			debugLogs[0] = s;
    			if (debugLogCount < debugLogs.Length)
    			{
    				debugLogCount++;
    			}
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public static void paintOnScreenDebug(mGraphics g)
    	{
    		if (!showDebugHUD || g == null)
    		{
    			return;
    		}
    		try
    		{
    			g.reset();
    			int hudW = 280;
    			if (hudW > GameCanvas.w - 8)
    			{
    				hudW = GameCanvas.w - 8;
    			}
    			int hudH = 100;
    			int hudX = 4;
    			int hudY = 4;

    			// Khung nền đen mờ bo tròn
    			g.setColor(0x0a1128, 0.82f);
    			g.fillRect(hudX, hudY, hudW, hudH, 4);
    			g.setColor(0x00f2fe, 0.9f);
    			g.drawRect(hudX, hudY, hudW, hudH);

    			// Tiêu đề HUD
    			mFont.tahoma_7b_white.drawString(g, "=== NRO DIAGNOSTIC HUD ===", hudX + 4, hudY + 3, 0);

    			// Dòng 1: Màn hình & State
    			string scrName = (GameCanvas.currentScreen != null) ? GameCanvas.currentScreen.GetType().Name : "null";
    			string s1 = "SCR: " + scrName + " | LoadScr: " + ServerListScreen.loadScreen + " | BigOk: " + ServerListScreen.bigOk;
    			mFont.tahoma_7_white.drawString(g, s1, hudX + 4, hudY + 15, 0);

    			// Dòng 2: Mạng & Key
    			string s2 = "NET: " + GameMidlet.IP + ":" + GameMidlet.PORT + " | Conn: " + Session_ME.connected + " | Key: " + Session_ME.getKeyComplete;
    			mFont.tahoma_7_white.drawString(g, s2, hudX + 4, hudY + 27, 0);

    			// Dòng 3: Tải dữ liệu CMD -74
    			string s3 = "DOWN: isGet: " + ServerListScreen.isGetData + " | " + ServerListScreen.demPercent + "/" + ServerListScreen.nBig + " (" + ServerListScreen.percent + "%)";
    			mFont.tahoma_7_yellow.drawString(g, s3, hudX + 4, hudY + 39, 0);

    			// Dòng 4: Tệp gần nhất
    			if (!string.IsNullOrEmpty(debugLastFile))
    			{
    				mFont.tahoma_7_grey.drawString(g, "FILE: " + debugLastFile, hudX + 4, hudY + 51, 0);
    			}
    			else
    			{
    				mFont.tahoma_7_grey.drawString(g, "FILE: (none)", hudX + 4, hudY + 51, 0);
    			}

    			// Dòng 5: Lỗi gần nhất (màu đỏ nếu có)
    			if (!string.IsNullOrEmpty(debugLastError))
    			{
    				mFont.tahoma_7_red.drawString(g, "ERR: " + debugLastError, hudX + 4, hudY + 63, 0);
    			}

    			// Dòng 6+: Log dòng gần nhất
    			int logY = hudY + 75;
    			if (debugLogs[0] != null)
    			{
    				mFont.tahoma_7_white.drawString(g, "> " + debugLogs[0], hudX + 4, logY, 0);
    			}
    			if (debugLogs[1] != null && hudH >= 100)
    			{
    				mFont.tahoma_7_grey.drawString(g, "> " + debugLogs[1], hudX + 4, logY + 11, 0);
    			}
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public static void updateOnScreenDebug()
    	{
    	}
    
    	public static string changeString(string str)
    	{
    		return str;
    	}
    
    	public static string replace(string _text, string _searchStr, string _replacementStr)
    	{
    		return _text.Replace(_searchStr, _replacementStr);
    	}
    
    	public static int xetVX(int goc, int d)
    	{
    		return cos(fixangle(goc)) * d >> 10;
    	}
    
    	public static int xetVY(int goc, int d)
    	{
    		return sin(fixangle(goc)) * d >> 10;
    	}
    
    	public static int random(int a, int b)
    	{
    		if (a == b)
    		{
    			return a;
    		}
    		if (b < a)
    		{
    			int num = a;
    			a = b;
    			b = num;
    		}
    		int diff = b - a;
    		if (diff <= 0)
    		{
    			return a;
    		}
    		return a + r.nextInt(diff);
    	}
    
    	public static int random(int a)
    	{
    		if (a <= 0) return 0;
    		return r.nextInt(a);
    	}
    
    	public static int random_Am(int a, int b)
    	{
    		if (b <= a) return a;
    		int num = a + r.nextInt(b - a);
    		if (random(2) == 0)
    		{
    			num = -num;
    		}
    		return num;
    	}
    
    	public static int random_Am_0(int a)
    	{
    		if (a <= 0) return 0;
    		int num;
    		for (num = 0; num == 0; num = r.nextInt() % a)
    		{
    		}
    		return num;
    	}
    
    	public static int s2tick(int currentTimeMillis)
    	{
    		int num = 0;
    		num = currentTimeMillis * 16 / 1000;
    		if (currentTimeMillis * 16 % 1000 >= 5)
    		{
    			num++;
    		}
    		return num;
    	}
    
    	public static int distance(int x1, int y1, int x2, int y2)
    	{
    		return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    	}
    
    	public static int getDistance(int x, int y)
    	{
    		return sqrt(x * x + y * y);
    	}
    
    	public static int sqrt(int a)
    	{
    		if (a <= 0)
    		{
    			return 0;
    		}
    		int num = (a + 1) / 2;
    		int num2;
    		do
    		{
    			num2 = num;
    			num = num / 2 + a / (2 * num);
    		}
    		while (Math.abs(num2 - num) > 1);
    		return num;
    	}
    
    	public static int rnd(int a)
    	{
    		return r.nextInt(a);
    	}
    
    	public static int abs(int i)
    	{
    		return (i <= 0) ? (-i) : i;
    	}
    
    	public static bool inRect(int x1, int y1, int width, int height, int x2, int y2)
    	{
    		return x2 >= x1 && x2 <= x1 + width && y2 >= y1 && y2 <= y1 + height;
    	}
    
    	public static string[] split(string original, string separator, int count)
    	{
    		int num = original.IndexOf(separator);
    		string[] array;
    		if (num >= 0)
    		{
    			array = split(original.Substring(num + separator.Length), separator, count + 1);
    		}
    		else
    		{
    			array = new string[count + 1];
    			num = original.Length;
    		}
    		array[count] = original.Substring(0, num);
    		return array;
    	}
    
    	public static string formatNumber(long number)
    	{
    		string empty = string.Empty;
    		string empty2 = string.Empty;
    		empty = string.Empty;
    		if (number >= 1000000000)
    		{
    			empty2 = mResources.billion;
    			long num = number % 1000000000 / 100000000;
    			number /= 1000000000;
    			empty = number + string.Empty;
    			if (num > 0)
    			{
    				string text = empty;
    				return text + "," + num + empty2;
    			}
    			return empty + empty2;
    		}
    		if (number >= 1000000)
    		{
    			empty2 = mResources.million;
    			long num2 = number % 1000000 / 100000;
    			number /= 1000000;
    			empty = number + string.Empty;
    			if (num2 > 0)
    			{
    				string text = empty;
    				return text + "," + num2 + empty2;
    			}
    			return empty + empty2;
    		}
    		return number + string.Empty;
    	}
    
    	public static string formatNumber2(long number)
    	{
    		string empty = string.Empty;
    		string empty2 = string.Empty;
    		empty = string.Empty;
    		if (number >= 1000000000)
    		{
    			empty2 = mResources.billion;
    			long num = number % 1000000000 / 10000000;
    			number /= 1000000000;
    			empty = number + string.Empty;
    			if (num >= 10)
    			{
    				if (num % 10 == 0)
    				{
    					num /= 10;
    				}
    				string text = empty;
    				return text + "," + num + empty2;
    			}
    			if (num > 0)
    			{
    				string text = empty;
    				return text + ",0" + num + empty2;
    			}
    			return empty + empty2;
    		}
    		if (number >= 1000000)
    		{
    			empty2 = mResources.million;
    			long num2 = number % 1000000 / 10000;
    			number /= 1000000;
    			empty = number + string.Empty;
    			if (num2 >= 10)
    			{
    				if (num2 % 10 == 0)
    				{
    					num2 /= 10;
    				}
    				string text = empty;
    				return text + "," + num2 + empty2;
    			}
    			if (num2 > 0)
    			{
    				string text = empty;
    				return text + ",0" + num2 + empty2;
    			}
    			return empty + empty2;
    		}
    		if (number >= 10000)
    		{
    			empty2 = "k";
    			long num3 = number % 1000 / 10;
    			number /= 1000;
    			empty = number + string.Empty;
    			if (num3 >= 10)
    			{
    				if (num3 % 10 == 0)
    				{
    					num3 /= 10;
    				}
    				string text = empty;
    				return text + "," + num3 + empty2;
    			}
    			if (num3 > 0)
    			{
    				string text = empty;
    				return text + ",0" + num3 + empty2;
    			}
    			return empty + empty2;
    		}
    		return number + string.Empty;
    	}

    	public static string formatNumberWithCommas(long number)
    	{
    		bool isNegative = number < 0;
    		if (isNegative)
    		{
    			number = -number;
    		}
    		string s = number.ToString();
    		if (s.Length <= 3)
    		{
    			return isNegative ? ("-" + s) : s;
    		}
    		System.Text.StringBuilder sb = new System.Text.StringBuilder();
    		if (isNegative)
    		{
    			sb.Append('-');
    		}
    		int mod = s.Length % 3;
    		if (mod > 0)
    		{
    			sb.Append(s, 0, mod);
    			if (s.Length > mod)
    			{
    				sb.Append('.');
    			}
    		}
    		for (int i = mod; i < s.Length; i += 3)
    		{
    			sb.Append(s, i, 3);
    			if (i + 3 < s.Length)
    			{
    				sb.Append('.');
    			}
    		}
    		return sb.ToString();
    	}
    }
}

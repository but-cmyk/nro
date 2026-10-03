namespace Game1
{
    using UnityEngine;
    
    public class Cout
    {
    	public static int count;
    
    	public static void println(string s)
    	{
    		if (mSystem.isTest && !string.IsNullOrEmpty(s) && UnityLogger.ShouldLog(s, LogType.Log))
    		{
    			Debug.Log(((count % 2 != 0) ? "***--- " : ">>>--- ") + s);
    			count++;
    		}
    	}
    
    	public static void Log(string str)
    	{
    		if (mSystem.isTest && !string.IsNullOrEmpty(str) && UnityLogger.ShouldLog(str, LogType.Log))
    		{
    			Debug.Log(str);
    		}
    	}
    
    	public static void LogError(string str)
    	{
    		if (string.IsNullOrEmpty(str))
    		{
    			return;
    		}
    		if (str.IndexOf("error", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("loi", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("fail", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("exception", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("cannot", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("err:", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("err ", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.StartsWith("err", System.StringComparison.OrdinalIgnoreCase))
    		{
    			if (UnityLogger.ShouldLog(str, LogType.Error))
    			{
    				Debug.LogError(str);
    			}
    		}
    		else
    		{
    			Log(str);
    		}
    	}
    
    	public static void LogError2(string str)
    	{
    	}
    
    	public static void LogError3(string str)
    	{
    		if (string.IsNullOrEmpty(str))
    		{
    			return;
    		}
    		if (str.IndexOf("error", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("loi", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("fail", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("exception", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("cannot", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("err:", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.IndexOf("err ", System.StringComparison.OrdinalIgnoreCase) >= 0 ||
    		    str.StartsWith("err", System.StringComparison.OrdinalIgnoreCase))
    		{
    			if (UnityLogger.ShouldLog(str, LogType.Error))
    			{
    				Debug.LogError(str);
    			}
    		}
    		else
    		{
    			Log(str);
    		}
    	}
    
    	public static void LogWarning(string str)
    	{
    		if (!string.IsNullOrEmpty(str) && UnityLogger.ShouldLog(str, LogType.Warning))
    		{
    			Debug.LogWarning(str);
    		}
    	}
    }
}

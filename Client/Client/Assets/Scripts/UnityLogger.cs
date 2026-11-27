using System;
using UnityEngine;

/// <summary>
/// Bộ lọc log cổng trước (Gatekeeper) giúp loại bỏ log rác chu kỳ cao ở Console Unity
/// mà không tạo background thread hay ghi tệp client_log.txt ra đĩa.
/// </summary>
public static class UnityLogger
{
    public enum LogLevel
    {
        None = 0,
        Error = 1,
        Warning = 2,
        Info = 3,
        Debug = 4,
        Verbose = 5
    }

    public static LogLevel CurrentLogLevel = LogLevel.Info;

    private static readonly string[] SpamBlacklist = new string[]
    {
        "[READ] cmd=",
        "SEND MSG:",
        ">>SEND ATTACT",
        "[CLIENT_SEND_ATTACK]",
        "[CLIENT_SET_SKILL_PAINT_BLOCKED]",
        "Cooldown not ready",
        "curr MP=",
        "---messageSubCommand",
        "modbHoldID=",
        "only select skill",
        "set auto skill",
        "skill id=",
        "remove 2",
        "Load Audio ",
        ">>>--- PERFORM WITH ID",
        "REQUEST ICON ",
        "dame hit =",
        "dame hit=",
        "hit eff=",
        "my cflag:",
        "TOI DAY",
        "can not attack",
        "toi day",
        "HERE",
        "CAP CHAAAA"
    };

    private static readonly string[] ValueWhitelist = new string[]
    {
        "[NET]",
        "[AUTH]",
        "[MAP]",
        "[TRADE]",
        "[COMBINE]",
        "[APP]",
        "[ASSET]",
        "[FATAL]",
        "[ERROR]",
        "[WARN]",
        "[INFO]",
        "[ALERT]",
        "connect",
        "disconnect",
        "fail",
        "Session_ME",
        "Loi "
    };

    public static bool IsSpam(string message)
    {
        if (string.IsNullOrEmpty(message))
        {
            return true;
        }
        for (int i = 0; i < SpamBlacklist.Length; i++)
        {
            if (message.IndexOf(SpamBlacklist[i], StringComparison.OrdinalIgnoreCase) >= 0)
            {
                return true;
            }
        }
        return false;
    }

    public static bool IsValuable(string message)
    {
        if (string.IsNullOrEmpty(message))
        {
            return false;
        }
        for (int i = 0; i < ValueWhitelist.Length; i++)
        {
            if (message.IndexOf(ValueWhitelist[i], StringComparison.OrdinalIgnoreCase) >= 0)
            {
                return true;
            }
        }
        return false;
    }

    public static bool ShouldLog(string message, LogType type)
    {
        if (CurrentLogLevel == LogLevel.None || string.IsNullOrEmpty(message))
        {
            return false;
        }

        if (type == LogType.Exception || type == LogType.Error || type == LogType.Assert)
        {
            return CurrentLogLevel >= LogLevel.Error;
        }

        if (type == LogType.Warning)
        {
            return CurrentLogLevel >= LogLevel.Warning;
        }

        if (IsSpam(message))
        {
            return false;
        }

        if (IsValuable(message))
        {
            return CurrentLogLevel >= LogLevel.Info;
        }

        return CurrentLogLevel >= LogLevel.Debug;
    }

    public static void LogInfo(string tag, string message)
    {
        if (CurrentLogLevel >= LogLevel.Info)
        {
            Debug.Log(string.Format("[{0}] {1}", tag, message));
        }
    }

    public static void LogWarning(string tag, string message)
    {
        if (CurrentLogLevel >= LogLevel.Warning)
        {
            Debug.LogWarning(string.Format("[{0}] {1}", tag, message));
        }
    }

    public static void LogError(string tag, string message, Exception ex = null)
    {
        if (CurrentLogLevel >= LogLevel.Error)
        {
            if (ex != null)
            {
                Debug.LogError(string.Format("[{0}] {1}\n{2}", tag, message, ex));
            }
            else
            {
                Debug.LogError(string.Format("[{0}] {1}", tag, message));
            }
        }
    }
}

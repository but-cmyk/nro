using UnityEngine;
using System.IO;
using System;

public static class UnityLogger
{
    private static readonly string LogFilePath = Path.Combine(Application.dataPath, string.Format("../unity_log_{0}.txt", System.Diagnostics.Process.GetCurrentProcess().Id));
    private static readonly object logLock = new object();

    private static bool isInitialized;

    [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSceneLoad)]
    private static void Initialize()
    {
        if (isInitialized)
        {
            return;
        }
        isInitialized = true;

        try
        {
            lock (logLock)
            {
                using (FileStream fs = new FileStream(LogFilePath, FileMode.Create, FileAccess.Write, FileShare.ReadWrite))
                using (StreamWriter sw = new StreamWriter(fs))
                {
                    sw.WriteLine("=== UNITY LOG STARTED AT " + DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + " ===");
                }
            }
        }
        catch (Exception)
        {
            // If another process or previous instance holds the file, ignore instead of crashing/error badge
        }

        try
        {
            Application.logMessageReceivedThreaded -= HandleLog;
            Application.logMessageReceivedThreaded += HandleLog;
        }
        catch (Exception)
        {
        }
    }

    private static void HandleLog(string logString, string stackTrace, LogType type)
    {
        try
        {
            string time = DateTime.Now.ToString("HH:mm:ss.fff");
            string formattedLog = string.Format("[{0}] [{1}] {2}\n", time, type, logString);
            if (type == LogType.Exception || type == LogType.Error)
            {
                formattedLog += stackTrace + "\n";
            }
            lock (logLock)
            {
                using (FileStream fs = new FileStream(LogFilePath, FileMode.Append, FileAccess.Write, FileShare.ReadWrite))
                using (StreamWriter sw = new StreamWriter(fs))
                {
                    sw.Write(formattedLog);
                }
            }
        }
        catch (Exception)
        {
            // Ignore log writing exceptions to prevent any potential infinite loops
        }
    }
}

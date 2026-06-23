using UnityEngine;
using System.IO;
using System;

public static class UnityLogger
{
    private static readonly string LogFilePath = Path.Combine(Application.dataPath, string.Format("../unity_log_{0}.txt", System.Diagnostics.Process.GetCurrentProcess().Id));

    [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSceneLoad)]
    private static void Initialize()
    {
        try
        {
            // Clear previous log on startup to keep it clean and clear
            File.WriteAllText(LogFilePath, "=== UNITY LOG STARTED AT " + DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + " ===\n");
            
            Application.logMessageReceivedThreaded += HandleLog;
        }
        catch (Exception ex)
        {
            Debug.LogError("Failed to initialize file logger: " + ex.Message);
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
            File.AppendAllText(LogFilePath, formattedLog);
        }
        catch (Exception)
        {
            // Ignore log writing exceptions to prevent any potential infinite loops
        }
    }
}

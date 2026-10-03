using System;
using System.Collections.Concurrent;
using System.IO;
using System.Threading;
using UnityEngine;

public static class UnityLogger
{
    private static readonly string LogDir = Path.GetFullPath(Path.Combine(Application.dataPath, ".."));
    private static readonly string LogFilePath = Path.Combine(LogDir, "client_log.txt");
    private const long MaxLogFileSizeBytes = 3 * 1024 * 1024; // 3 MB
    private const int MaxBackupFiles = 2;

    private static readonly ConcurrentQueue<string> logQueue = new ConcurrentQueue<string>();
    private static readonly AutoResetEvent logSignal = new AutoResetEvent(false);

    private static Thread workerThread;
    private static volatile bool isRunning;
    private static bool isInitialized;

    public enum LogLevel
    {
        None = 0,
        Error = 1,
        Warning = 2,
        Info = 3,
        Debug = 4,
        Verbose = 5
    }

    /// <summary>
    /// Cấp độ log hiện tại của Client.
    /// Mặc định là Info: Chỉ in thông tin sự kiện quan trọng ([NET], [MAP], [AUTH], [TRADE]), Warning và Error.
    /// Chặn sạch toàn bộ log debug vết, đòn đánh và packet chu kỳ cao.
    /// </summary>
    public static LogLevel CurrentLogLevel = LogLevel.Info;

    // Danh sách các từ khóa rác chu kỳ cao gây nghẽn I/O & sụt giảm FPS
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

    // Danh sách thẻ thông tin có giá trị nghiệp vụ cần lưu vết
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

    /// <summary>
    /// Kiểm tra chuỗi có thuộc danh sách log rác chu kỳ cao hay không.
    /// </summary>
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

    /// <summary>
    /// Kiểm tra chuỗi có thuộc danh sách thông tin nghiệp vụ giá trị hay không.
    /// </summary>
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

    /// <summary>
    /// Bộ lọc cổng trước (Gatekeeper) kiểm tra trước khi gọi UnityEngine.Debug.Log.
    /// Ngăn chặn cấp phát GC bộ nhớ và dội bom Console Unity.
    /// </summary>
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

        // Với Log thông thường (LogType.Log)
        if (IsSpam(message))
        {
            return false; // Chặn triệt để log rác
        }

        if (IsValuable(message))
        {
            return CurrentLogLevel >= LogLevel.Info;
        }

        // Log debug vết thông thường: chỉ cho phép khi bật mức Debug hoặc Verbose
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

    [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSceneLoad)]
    private static void Initialize()
    {
        if (isInitialized)
        {
            return;
        }
        isInitialized = true;

        // Dọn dẹp các tệp log rác cũ tích lũy từ các phiên trước
        CleanupOldLogFiles();

        try
        {
            isRunning = true;
            workerThread = new Thread(ProcessLogQueue)
            {
                IsBackground = true,
                Name = "AsyncLoggerWorker",
                Priority = System.Threading.ThreadPriority.BelowNormal
            };
            workerThread.Start();

            // Đăng ký nhận log từ engine Unity
            Application.logMessageReceivedThreaded -= HandleLog;
            Application.logMessageReceivedThreaded += HandleLog;

            // Đăng ký dọn dẹp khi thoát game
            Application.quitting -= OnApplicationQuitting;
            Application.quitting += OnApplicationQuitting;
            AppDomain.CurrentDomain.ProcessExit -= OnProcessExit;
            AppDomain.CurrentDomain.ProcessExit += OnProcessExit;

            EnqueueLog(string.Format("[{0}] [APP] === CLIENT LOG STARTED: PID={1}, OS={2}, UNITY={3} ===",
                DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
                System.Diagnostics.Process.GetCurrentProcess().Id,
                SystemInfo.operatingSystem,
                Application.unityVersion));
        }
        catch (Exception ex)
        {
            Debug.LogError("Failed to initialize UnityLogger: " + ex.Message);
        }
    }

    private static void HandleLog(string logString, string stackTrace, LogType type)
    {
        if (!isRunning || string.IsNullOrEmpty(logString))
        {
            return;
        }

        // 1. Ngoại lệ & Lỗi nghiêm trọng: Luôn luôn ghi nhận kèm Call Stack
        if (type == LogType.Exception || type == LogType.Error || type == LogType.Assert)
        {
            string time = DateTime.Now.ToString("HH:mm:ss.fff");
            string entry = string.Format("[{0}] [{1}] {2}\n{3}", time, type, logString, stackTrace);
            EnqueueLog(entry);
            return;
        }

        // 2. Kiểm tra bộ lọc loại bỏ spam gói tin & đòn đánh chu kỳ cao
        if (IsSpam(logString))
        {
            return;
        }

        // 3. Xử lý đặc thù cho log âm thanh: chỉ ghi khi thất bại
        if (logString.IndexOf("Loaded sound:", StringComparison.OrdinalIgnoreCase) >= 0)
        {
            if (logString.IndexOf("SUCCESS", StringComparison.OrdinalIgnoreCase) >= 0)
            {
                return; // Bỏ qua nếu tải thành công
            }
        }

        // 4. Kiểm tra các nhóm thông tin có giá trị (Whitelist)
        bool isValuable = IsValuable(logString);

        if (isValuable || type == LogType.Warning)
        {
            string time = DateTime.Now.ToString("HH:mm:ss.fff");
            string entry = string.Format("[{0}] [{1}] {2}", time, type, logString);
            EnqueueLog(entry);
        }
    }

    private static void EnqueueLog(string message)
    {
        logQueue.Enqueue(message);
        try
        {
            logSignal.Set();
        }
        catch
        {
        }
    }

    private static void ProcessLogQueue()
    {
        FileStream fileStream = null;
        StreamWriter writer = null;

        try
        {
            fileStream = new FileStream(LogFilePath, FileMode.Append, FileAccess.Write, FileShare.ReadWrite);
            writer = new StreamWriter(fileStream, System.Text.Encoding.UTF8);

            while (isRunning)
            {
                // Chờ tín hiệu hoặc định kỳ 1000ms flush
                logSignal.WaitOne(1000);

                bool hasWritten = false;
                while (logQueue.TryDequeue(out string logItem))
                {
                    writer.WriteLine(logItem);
                    hasWritten = true;
                }

                if (hasWritten)
                {
                    writer.Flush();

                    // Kiểm tra kích thước file để xoay vòng (Log Rotation)
                    if (fileStream.Length > MaxLogFileSizeBytes)
                    {
                        writer.Dispose();
                        fileStream.Dispose();

                        RotateLogFiles();

                        fileStream = new FileStream(LogFilePath, FileMode.Create, FileAccess.Write, FileShare.ReadWrite);
                        writer = new StreamWriter(fileStream, System.Text.Encoding.UTF8);
                    }
                }
            }

            // Ghi nốt dữ liệu còn lại trong hàng đợi trước khi tắt
            while (logQueue.TryDequeue(out string remainingLog))
            {
                writer.WriteLine(remainingLog);
            }
            writer.Flush();
        }
        catch (Exception)
        {
            // Bỏ qua lỗi ghi log ngầm để không bao giờ ảnh hưởng tới gameplay
        }
        finally
        {
            if (writer != null)
            {
                try { writer.Dispose(); } catch { }
            }
            if (fileStream != null)
            {
                try { fileStream.Dispose(); } catch { }
            }
        }
    }

    private static void RotateLogFiles()
    {
        try
        {
            for (int i = MaxBackupFiles - 1; i >= 1; i--)
            {
                string src = Path.Combine(LogDir, string.Format("client_log.{0}.txt", i));
                string dst = Path.Combine(LogDir, string.Format("client_log.{0}.txt", i + 1));
                if (File.Exists(src))
                {
                    if (File.Exists(dst))
                    {
                        File.Delete(dst);
                    }
                    File.Move(src, dst);
                }
            }

            string backupFirst = Path.Combine(LogDir, "client_log.1.txt");
            if (File.Exists(backupFirst))
            {
                File.Delete(backupFirst);
            }
            if (File.Exists(LogFilePath))
            {
                File.Move(LogFilePath, backupFirst);
            }
        }
        catch
        {
        }
    }

    private static void CleanupOldLogFiles()
    {
        try
        {
            string[] oldLogs = Directory.GetFiles(LogDir, "unity_log_*.txt");
            for (int i = 0; i < oldLogs.Length; i++)
            {
                try
                {
                    File.Delete(oldLogs[i]);
                }
                catch
                {
                }
            }
        }
        catch
        {
        }
    }

    private static void OnApplicationQuitting()
    {
        Shutdown();
    }

    private static void OnProcessExit(object sender, EventArgs e)
    {
        Shutdown();
    }

    private static void Shutdown()
    {
        if (!isRunning)
        {
            return;
        }

        EnqueueLog(string.Format("[{0}] [APP] === CLIENT LOG STOPPED ===", DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss")));
        isRunning = false;

        try
        {
            logSignal.Set();
            if (workerThread != null && workerThread.IsAlive)
            {
                workerThread.Join(500); // Chờ tối đa 500ms để worker flush xong
            }
        }
        catch
        {
        }
    }
}

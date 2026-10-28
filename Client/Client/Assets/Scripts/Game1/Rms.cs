namespace Game1
{
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Security.Cryptography;
    using System.Text;
    using System.Threading;
    using UnityEngine;
    
    public class Rms
    {
        public static int status;
    
        public static sbyte[] data;
    
        public static string filename;

        private static readonly object rmsLock = new object();
        private static readonly Dictionary<string, sbyte[]> rmsCache = new Dictionary<string, sbyte[]>();

        private static string cachedPath = null;
        public static string rootPersistentPath = null;

        public static void saveRMS(string filename, sbyte[] data)
        {
            if (string.IsNullOrEmpty(filename)) return;
            lock (rmsLock)
            {
                if (data != null)
                {
                    rmsCache[filename] = data;
                }
                else
                {
                    rmsCache.Remove(filename);
                }
                __saveRMS(filename, data);
            }
        }
    
        public static sbyte[] loadRMS(string filename)
        {
            if (string.IsNullOrEmpty(filename)) return null;
            lock (rmsLock)
            {
                sbyte[] cached;
                if (rmsCache.TryGetValue(filename, out cached))
                {
                    return cached;
                }
                sbyte[] diskData = __loadRMS(filename);
                if (diskData != null)
                {
                    rmsCache[filename] = diskData;
                }
                return diskData;
            }
        }
    
        public static string loadRMSString(string fileName)
        {
            sbyte[] array = loadRMS(fileName);
            if (array == null)
            {
                return null;
            }
            DataInputStream dataInputStream = new DataInputStream(array);
            try
            {
                string result = dataInputStream.readUTF();
                dataInputStream.close();
                return result;
            }
            catch (Exception ex)
            {
                Cout.println(ex.StackTrace);
            }
            return null;
        }

        public static byte[] convertSbyteToByte(sbyte[] var)
        {
            byte[] array = new byte[var.Length];
            for (int i = 0; i < var.Length; i++)
            {
                if (var[i] > 0)
                {
                    array[i] = (byte)var[i];
                }
                else
                {
                    array[i] = (byte)(var[i] + 256);
                }
            }
            return array;
        }
    
        public static void saveRMSString(string filename, string data)
        {
            DataOutputStream dataOutputStream = new DataOutputStream();
            try
            {
                dataOutputStream.writeUTF(data);
                saveRMS(filename, dataOutputStream.toByteArray());
                dataOutputStream.close();
            }
            catch (Exception ex)
            {
                Cout.println(ex.StackTrace);
            }
        }

        public static void saveRMSPassword(string filename, string password)
        {
            if (password == null)
            {
                saveRMSString(filename, string.Empty);
                return;
            }
            try
            {
                byte[] plain = Encoding.UTF8.GetBytes(password);
                byte[] key = getPasswordKey(filename);
                using (Aes aes = Aes.Create())
                {
                    aes.Key = key;
                    aes.GenerateIV();
                    using (ICryptoTransform encryptor = aes.CreateEncryptor())
                    {
                        byte[] cipher = encryptor.TransformFinalBlock(plain, 0, plain.Length);
                        byte[] payload = new byte[1 + aes.IV.Length + cipher.Length];
                        payload[0] = 1;
                        Buffer.BlockCopy(aes.IV, 0, payload, 1, aes.IV.Length);
                        Buffer.BlockCopy(cipher, 0, payload, 1 + aes.IV.Length, cipher.Length);
                        saveRMSString(filename, "enc:" + Convert.ToBase64String(payload));
                    }
                }
            }
            catch (Exception ex)
            {
                Debug.LogError("Cannot securely save password: " + ex.Message);
            }
        }

        public static string loadRMSPassword(string filename)
        {
            string stored = loadRMSString(filename);
            if (string.IsNullOrEmpty(stored) || !stored.StartsWith("enc:", StringComparison.Ordinal))
            {
                return stored;
            }
            try
            {
                byte[] payload = Convert.FromBase64String(stored.Substring(4));
                if (payload.Length < 18 || payload[0] != 1)
                {
                    return null;
                }
                byte[] iv = new byte[16];
                Buffer.BlockCopy(payload, 1, iv, 0, iv.Length);
                int cipherLength = payload.Length - 1 - iv.Length;
                byte[] cipher = new byte[cipherLength];
                Buffer.BlockCopy(payload, 1 + iv.Length, cipher, 0, cipherLength);
                using (Aes aes = Aes.Create())
                {
                    aes.Key = getPasswordKey(filename);
                    aes.IV = iv;
                    using (ICryptoTransform decryptor = aes.CreateDecryptor())
                    {
                        byte[] plain = decryptor.TransformFinalBlock(cipher, 0, cipher.Length);
                        return Encoding.UTF8.GetString(plain);
                    }
                }
            }
            catch (Exception ex)
            {
                Debug.LogError("Cannot securely load password: " + ex.Message);
                return null;
            }
        }

        private static byte[] getPasswordKey(string filename)
        {
            string deviceId = SystemInfo.deviceUniqueIdentifier;
            if (string.IsNullOrEmpty(deviceId))
            {
                deviceId = Application.identifier;
            }
            using (SHA256 sha = SHA256.Create())
            {
                return sha.ComputeHash(Encoding.UTF8.GetBytes(deviceId + "|NRO-6Tab|" + filename));
            }
        }
    
        private static void _saveRMS(string filename, sbyte[] data)
        {
            saveRMS(filename, data);
        }
    
        private static sbyte[] _loadRMS(string filename)
        {
            return loadRMS(filename);
        }

        public static void update()
        {
            // Direct thread-safe I/O with memory cache; no busy-wait polling required
        }

        public static int loadRMSInt(string file)
        {
            sbyte[] array = loadRMS(file);
            return (array != null) ? array[0] : (-1);
        }

        public static void saveRMSInt(string file, int x)
        {
            try
            {
                saveRMS(file, new sbyte[1] { (sbyte)x });
            }
            catch (Exception)
            {
            }
        }

        public static void Init()
        {
            try
            {
                rootPersistentPath = Application.persistentDataPath;
                cachedPath = rootPersistentPath + "/Game1";
                if (!Directory.Exists(cachedPath))
                {
                    Directory.CreateDirectory(cachedPath);
                }
            }
            catch (Exception ex)
            {
                Debug.LogWarning("Rms.Init exception: " + ex.Message);
            }
        }

        public static string GetiPhoneDocumentsPath()
        {
            if (cachedPath == null)
            {
                try
                {
                    rootPersistentPath = Application.persistentDataPath;
                    cachedPath = rootPersistentPath + "/Game1";
                }
                catch
                {
                    return (rootPersistentPath != null) ? (rootPersistentPath + "/Game1") : "Game1";
                }
            }
            if (!Directory.Exists(cachedPath))
            {
                try
                {
                    Directory.CreateDirectory(cachedPath);
                }
                catch
                {
                }
            }
            return cachedPath;
        }

        private static void __saveRMS(string filename, sbyte[] data)
        {
            try
            {
                string text = GetiPhoneDocumentsPath() + "/" + filename;
                using (FileStream fileStream = new FileStream(text, FileMode.Create, FileAccess.Write, FileShare.ReadWrite))
                {
                    fileStream.Write(ArrayCast.cast(data), 0, data.Length);
                    fileStream.Flush();
                }
                Main.setBackupIcloud(text);
            }
            catch (Exception ex)
            {
                Debug.LogError("Error __saveRMS: " + ex.Message);
            }
        }

        private static sbyte[] __loadRMS(string filename)
        {
            if (string.IsNullOrEmpty(filename))
            {
                return null;
            }
            try
            {
                string text = GetiPhoneDocumentsPath() + "/" + filename;
                if (!File.Exists(text))
                {
                    string root = rootPersistentPath;
                    if (string.IsNullOrEmpty(root))
                    {
                        try
                        {
                            root = Application.persistentDataPath;
                        }
                        catch
                        {
                        }
                    }
                    if (!string.IsNullOrEmpty(root))
                    {
                        string fallback = root + "/" + filename;
                        if (File.Exists(fallback))
                        {
                            text = fallback;
                        }
                        else
                        {
                            string fallbackGame1 = root + "/Game1/" + filename;
                            if (File.Exists(fallbackGame1))
                            {
                                text = fallbackGame1;
                            }
                        }
                    }
                }
                if (!File.Exists(text))
                {
                    if (filename == "ResVersion" || filename == "vsRes" || filename.StartsWith("NR"))
                    {
                        return null;
                    }
                    try
                    {
                        string userProfile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
                        string[] possiblePaths = new string[]
                        {
                            userProfile + "/AppData/LocalLow/Team/NgocRongOnline/Game1/" + filename,
                            userProfile + "/AppData/LocalLow/Team/NgocRongOnline/" + filename,
                            userProfile + "/AppData/LocalLow/Team/Nro6Tab/Game1/" + filename,
                            userProfile + "/AppData/LocalLow/Team/Nro6Tab/" + filename,
                            "Game1/" + filename,
                            filename
                        };
                        foreach (string p in possiblePaths)
                        {
                            if (File.Exists(p))
                            {
                                text = p;
                                break;
                            }
                        }
                    }
                    catch
                    {
                    }
                }
                if (!File.Exists(text))
                {
                    return null;
                }
                using (FileStream fileStream = new FileStream(text, FileMode.Open, FileAccess.Read, FileShare.ReadWrite))
                {
                    byte[] array = new byte[fileStream.Length];
                    fileStream.Read(array, 0, array.Length);
                    return ArrayCast.cast(array);
                }
            }
            catch (Exception)
            {
                return null;
            }
        }

        public static void clearAll()
        {
            try
            {
                lock (rmsLock)
                {
                    rmsCache.Clear();
                }
                Cout.Log("clean rms");
                string userProfile = string.Empty;
                try
                {
                    userProfile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
                }
                catch
                {
                }
                string[] folders = new string[]
                {
                    GetiPhoneDocumentsPath(),
                    rootPersistentPath,
                    Application.persistentDataPath,
                    (rootPersistentPath != null) ? (rootPersistentPath + "/Game1") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/NgocRongOnline/Game1") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/NgocRongOnline") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/Nro6Tab/Game1") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/Nro6Tab") : null,
                    "Game1",
                    "."
                };
                foreach (string folder in folders)
                {
                    if (string.IsNullOrEmpty(folder) || !Directory.Exists(folder)) continue;
                    try
                    {
                        string resVerPath = folder + "/ResVersion";
                        if (File.Exists(resVerPath)) File.Delete(resVerPath);
                        string vsResPath = folder + "/vsRes";
                        if (File.Exists(vsResPath)) File.Delete(vsResPath);
                    }
                    catch
                    {
                    }
                }
                string dirPath = GetiPhoneDocumentsPath();
                if (Directory.Exists(dirPath))
                {
                    FileInfo[] files = new DirectoryInfo(dirPath).GetFiles();
                    foreach (FileInfo fileInfo in files)
                    {
                        try
                        {
                            fileInfo.Delete();
                        }
                        catch
                        {
                        }
                    }
                }
                clearLoginData();
                try
                {
                    PlayerPrefs.DeleteAll();
                    PlayerPrefs.Save();
                }
                catch
                {
                }
            }
            catch
            {
            }
        }

        public static void clearLoginData()
        {
            try
            {
                string[] filesToDelete = new string[] { "acc", "pass", "check", "NRIPlink", "svselect", "ResVersion", "vsRes" };
                string userProfile = string.Empty;
                try
                {
                    userProfile = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
                }
                catch
                {
                }

                string[] folders = new string[]
                {
                    GetiPhoneDocumentsPath(),
                    rootPersistentPath,
                    Application.persistentDataPath,
                    (rootPersistentPath != null) ? (rootPersistentPath + "/Game1") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/NgocRongOnline/Game1") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/NgocRongOnline") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/Nro6Tab/Game1") : null,
                    !string.IsNullOrEmpty(userProfile) ? (userProfile + "/AppData/LocalLow/Team/Nro6Tab") : null,
                    "Game1",
                    "."
                };

                foreach (string folder in folders)
                {
                    if (string.IsNullOrEmpty(folder) || !Directory.Exists(folder))
                    {
                        continue;
                    }
                    try
                    {
                        foreach (string fname in filesToDelete)
                        {
                            string fpath = folder + "/" + fname;
                            if (File.Exists(fpath))
                            {
                                File.Delete(fpath);
                            }
                        }

                        DirectoryInfo dInfo = new DirectoryInfo(folder);
                        FileInfo[] allFiles = dInfo.GetFiles();
                        foreach (FileInfo fi in allFiles)
                        {
                            if (fi.Name.StartsWith("userAo") || fi.Name.StartsWith("passAo"))
                            {
                                try
                                {
                                    fi.Delete();
                                }
                                catch
                                {
                                }
                            }
                        }
                    }
                    catch
                    {
                    }
                }

                try
                {
                    PlayerPrefs.DeleteKey("Game1_acc");
                    PlayerPrefs.DeleteKey("Game1_pass");
                    PlayerPrefs.DeleteKey("Game1_check");
                    PlayerPrefs.DeleteKey("Game1_ResVersion");
                    PlayerPrefs.DeleteKey("acc");
                    PlayerPrefs.DeleteKey("pass");
                    PlayerPrefs.DeleteKey("check");
                    PlayerPrefs.DeleteKey("ResVersion");
                    for (int i = 0; i <= 30; i++)
                    {
                        PlayerPrefs.DeleteKey("Game1_userAo" + i);
                        PlayerPrefs.DeleteKey("userAo" + i);
                        PlayerPrefs.DeleteKey("Game1_passAo" + i);
                        PlayerPrefs.DeleteKey("passAo" + i);
                    }
                    PlayerPrefs.Save();
                }
                catch
                {
                }
            }
            catch (Exception ex)
            {
                Debug.LogWarning("clearLoginData exception: " + ex.Message);
            }
        }

        public static void DeleteStorage(string path)
        {
            try
            {
                File.Delete(GetiPhoneDocumentsPath() + "/" + path);
            }
            catch (Exception)
            {
            }
        }

        public static string ByteArrayToString(byte[] ba)
        {
            string text = BitConverter.ToString(ba);
            return text.Replace("-", string.Empty);
        }

        public static byte[] StringToByteArray(string hex)
        {
            int length = hex.Length;
            byte[] array = new byte[length / 2];
            for (int i = 0; i < length; i += 2)
            {
                array[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
            }
            return array;
        }

        public static void deleteRecord(string name)
        {
            lock (rmsLock)
            {
                if (!string.IsNullOrEmpty(name))
                {
                    rmsCache.Remove(name);
                }
                try
                {
                    DeleteStorage(name);
                    PlayerPrefs.DeleteKey("Game1_" + name);
                }
                catch (Exception ex)
                {
                    Cout.println("loi xoa RMS --------------------------" + ex.ToString());
                }
            }
        }

        public static void clearRMS()
        {
            deleteRecord("data");
            deleteRecord("dataVersion");
            deleteRecord("map");
            deleteRecord("mapVersion");
            deleteRecord("skill");
            deleteRecord("killVersion");
            deleteRecord("skillVersion");
            deleteRecord("item");
            deleteRecord("itemVersion");
            deleteRecord("NRdata");
            deleteRecord("NRdataVersion");
            deleteRecord("NRmap");
            deleteRecord("NRmapVersion");
            deleteRecord("NRskill");
            deleteRecord("NRskillVersion");
            deleteRecord("NRitem0");
            deleteRecord("NRitem1");
            deleteRecord("NRitem2");
            deleteRecord("NRitem100");
            deleteRecord("NRitem101");
            deleteRecord("NRitemVersion");
            deleteRecord("NR_dart");
            deleteRecord("NR_arrow");
            deleteRecord("NR_effect");
            deleteRecord("NR_image");
            deleteRecord("NR_part");
            deleteRecord("NR_skill");
        }

        public static void saveIP(string strID)
        {
            saveRMSString("NRIPlink", strID);
        }

        public static string loadIP()
        {
            string text = loadRMSString("NRIPlink");
            if (text == null)
            {
                return null;
            }
            return text;
        }
    }
}

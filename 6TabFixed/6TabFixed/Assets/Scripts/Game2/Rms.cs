using System;
    using System.IO;
    using System.Security.Cryptography;
    using System.Text;
using System.Threading;
using UnityEngine;

namespace Game2
{
    
    public class Rms
    {
        public static int status;
    
        public static sbyte[] data;
    
        public static string filename;
    
        private const int INTERVAL = 5;
    
        private const int MAXTIME = 500;
    
        public static void saveRMS(string filename, sbyte[] data)
        {
            if (Thread.CurrentThread.Name == Main.mainThreadName)
            {
                __saveRMS(filename, data);
            }
            else
            {
                _saveRMS(filename, data);
            }
        }
    
        public static sbyte[] loadRMS(string filename)
        {
            if (Thread.CurrentThread.Name == Main.mainThreadName)
            {
                return __loadRMS(filename);
            }
            return _loadRMS(filename);
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
            if (status != 0)
            {
                Debug.LogError("Cannot save RMS " + filename + " because current is saving " + Rms.filename);
                return;
            }
            Rms.filename = filename;
            Rms.data = data;
            status = 2;
            int i;
            for (i = 0; i < 500; i++)
            {
                Thread.Sleep(5);
                if (status == 0)
                {
                    break;
                }
            }
            if (i == 500)
            {
                Debug.LogError("TOO LONG TO SAVE RMS " + filename);
            }
        }
    
        private static sbyte[] _loadRMS(string filename)
        {
            if (status != 0)
            {
                Debug.LogError("Cannot load RMS " + filename + " because current is loading " + Rms.filename);
                return null;
            }
            Rms.filename = filename;
            data = null;
            status = 3;
            int i;
            for (i = 0; i < 500; i++)
            {
                Thread.Sleep(5);
                if (status == 0)
                {
                    break;
                }
            }
            if (i == 500)
            {
                Debug.LogError("TOO LONG TO LOAD RMS " + filename);
            }
            return data;
        }

        public static void update()
        {
            if (status == 2)
            {
                status = 1;
                __saveRMS(filename, data);
                status = 0;
            }
            else if (status == 3)
            {
                status = 1;
                data = __loadRMS(filename);
                status = 0;
            }
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

        public static string GetiPhoneDocumentsPath()
        {
            string path = Application.persistentDataPath + "/Game2";
            if (!Directory.Exists(path)) {
                Directory.CreateDirectory(path);
            }
            return path;
        }

        private static void __saveRMS(string filename, sbyte[] data)
        {
            try
            {
                string text = GetiPhoneDocumentsPath() + "/" + filename;
                using (FileStream fileStream = new FileStream(text, FileMode.Create, FileAccess.Write, FileShare.None))
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
            try
            {
                string text = GetiPhoneDocumentsPath() + "/" + filename;
                if (!File.Exists(text))
                {
                    return null;
                }
                using (FileStream fileStream = new FileStream(text, FileMode.Open, FileAccess.Read, FileShare.Read))
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
                Cout.Log("clean rms");
                FileInfo[] files = new DirectoryInfo(GetiPhoneDocumentsPath() + "/").GetFiles();
                foreach (FileInfo fileInfo in files)
                {
                    fileInfo.Delete();
                }
            }
            catch
            {
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
            try
            {
                PlayerPrefs.DeleteKey("Game2_" + name);
            }
            catch (Exception ex)
            {
                Cout.println("loi xoa RMS --------------------------" + ex.ToString());
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
            deleteRecord("item");
            deleteRecord("itemVersion");
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

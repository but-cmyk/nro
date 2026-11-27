namespace Game1
{
    using System;
    using System.IO;
    using System.Threading;
    using UnityEngine;
    using UnityEngine.Networking;

    /// <summary>
    /// Nạp tài nguyên nhị phân (.bytes) và hình ảnh (.png) ưu tiên từ StreamingAssets
    /// để tránh phình bảng chỉ mục Resources/ trong RAM khi khởi động, có fallback về Resources.Load.
    /// </summary>
    public static class AssetLoader
    {
        private static string cachedStreamingPath;
        private static float lastUnloadTime = -60f;

        public static void UnloadUnusedAssetsIfNeeded()
        {
            float now = Time.realtimeSinceStartup;
            if (now - lastUnloadTime >= 60f)
            {
                lastUnloadTime = now;
                Resources.UnloadUnusedAssets();
            }
        }

        private static string GetStreamingRoot()
        {
            if (cachedStreamingPath == null)
            {
                cachedStreamingPath = Application.streamingAssetsPath;
            }
            return cachedStreamingPath;
        }

        private static string NormalizePath(string resourcePath)
        {
            if (string.IsNullOrEmpty(resourcePath))
            {
                return string.Empty;
            }
            return resourcePath.Replace('\\', '/').TrimStart('/');
        }

        private static byte[] ReadAndroidStreamingFile(string url)
        {
            using (UnityWebRequest req = UnityWebRequest.Get(url))
            {
                req.timeout = 2;
                UnityWebRequestAsyncOperation op = req.SendWebRequest();
                int waitMs = 0;
                while (!op.isDone && waitMs < 2000)
                {
                    Thread.Sleep(1);
                    waitMs++;
                }
                if (req.result == UnityWebRequest.Result.Success && req.downloadHandler != null)
                {
                    return req.downloadHandler.data;
                }
            }
            return null;
        }

        private static byte[] TryReadStreamingFile(string normalizedPath, bool preferPng)
        {
            string root = GetStreamingRoot();
            if (string.IsNullOrEmpty(root))
            {
                return null;
            }

            try
            {
                if (root.IndexOf("://", StringComparison.Ordinal) >= 0)
                {
                    if (preferPng)
                    {
                        byte[] pngBytes = ReadAndroidStreamingFile(root.TrimEnd('/') + "/" + normalizedPath + ".png");
                        if (pngBytes != null && pngBytes.Length > 0) return pngBytes;
                    }
                    byte[] directBytes = ReadAndroidStreamingFile(root.TrimEnd('/') + "/" + normalizedPath);
                    if (directBytes != null && directBytes.Length > 0) return directBytes;
                    byte[] rawBytes = ReadAndroidStreamingFile(root.TrimEnd('/') + "/" + normalizedPath + ".bytes");
                    if (rawBytes != null && rawBytes.Length > 0) return rawBytes;
                    return null;
                }
                if (preferPng)
                {
                    string pngPath = Path.Combine(root, normalizedPath + ".png");
                    if (File.Exists(pngPath))
                    {
                        return File.ReadAllBytes(pngPath);
                    }
                }

                string directPath = Path.Combine(root, normalizedPath);
                if (File.Exists(directPath))
                {
                    return File.ReadAllBytes(directPath);
                }

                string bytesPath = Path.Combine(root, normalizedPath + ".bytes");
                if (File.Exists(bytesPath))
                {
                    return File.ReadAllBytes(bytesPath);
                }

                if (!preferPng)
                {
                    string pngPath = Path.Combine(root, normalizedPath + ".png");
                    if (File.Exists(pngPath))
                    {
                        return File.ReadAllBytes(pngPath);
                    }
                }
            }
            catch (Exception)
            {
            }
            return null;
        }

        public static byte[] LoadBytes(string resourcePath)
        {
            string normalized = NormalizePath(resourcePath);
            if (string.IsNullOrEmpty(normalized))
            {
                return null;
            }

            byte[] streamingBytes = TryReadStreamingFile(normalized, preferPng: false);
            if (streamingBytes != null && streamingBytes.Length > 0)
            {
                return streamingBytes;
            }

            TextAsset textAsset = Resources.Load(normalized, typeof(TextAsset)) as TextAsset;
            if (textAsset != null && textAsset.bytes != null && textAsset.bytes.Length > 0)
            {
                return textAsset.bytes;
            }
            return null;
        }

        public static Texture2D LoadTexture2D(string resourcePath)
        {
            return LoadTexture2D(resourcePath, out _);
        }

        public static Texture2D LoadTexture2D(string resourcePath, out bool isOwned)
        {
            isOwned = false;
            string normalized = NormalizePath(resourcePath);
            if (string.IsNullOrEmpty(normalized))
            {
                return null;
            }

            byte[] bytes = TryReadStreamingFile(normalized, preferPng: true);
            if (bytes != null && bytes.Length > 0)
            {
                Texture2D tex = new Texture2D(2, 2, TextureFormat.RGBA32, false);
                tex.anisoLevel = 0;
                tex.filterMode = FilterMode.Point;
                tex.mipMapBias = 0f;
                tex.wrapMode = TextureWrapMode.Clamp;
                if (ImageConversion.LoadImage(tex, bytes, false))
                {
                    isOwned = true;
                    return tex;
                }
                UnityEngine.Object.Destroy(tex);
            }

            return Resources.Load(normalized) as Texture2D;
        }
    }
}

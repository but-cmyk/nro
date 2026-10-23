namespace Game1
{
    using System;
    using System.IO;

    public class ArrayCast
    {
        public const int DEFAULT_MAX_ALLOC = 2097152; // 2MB default guardrail

        public static sbyte[] SafeAllocSByte(int len, int maxAllowed = DEFAULT_MAX_ALLOC)
        {
            if (len < 0 || len > maxAllowed)
            {
                throw new IOException($"SafeAllocSByte: kich thuoc buffer vuot nguong cho phep ({len} bytes, max={maxAllowed})");
            }
            return new sbyte[len];
        }

        public static byte[] SafeAllocByte(int len, int maxAllowed = DEFAULT_MAX_ALLOC)
        {
            if (len < 0 || len > maxAllowed)
            {
                throw new IOException($"SafeAllocByte: kich thuoc buffer vuot nguong cho phep ({len} bytes, max={maxAllowed})");
            }
            return new byte[len];
        }

        public static sbyte[] cast(byte[] data)
        {
            if (data == null)
            {
                return null;
            }
            sbyte[] array = new sbyte[data.Length];
            for (int i = 0; i < array.Length; i++)
            {
                array[i] = (sbyte)data[i];
            }
            return array;
        }

        public static byte[] cast(sbyte[] data)
        {
            if (data == null)
            {
                return null;
            }
            byte[] array = new byte[data.Length];
            for (int i = 0; i < array.Length; i++)
            {
                array[i] = (byte)data[i];
            }
            return array;
        }

        public static char[] ToCharArray(sbyte[] data)
        {
            if (data == null)
            {
                return null;
            }
            char[] array = new char[data.Length];
            for (int i = 0; i < array.Length; i++)
            {
                array[i] = (char)data[i];
            }
            return array;
        }
    }
}


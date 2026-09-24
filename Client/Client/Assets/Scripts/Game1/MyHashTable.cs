namespace Game1
{
    using System;
    using System.Collections;
    using System.Collections.Generic;

    /// <summary>
    /// MyHashTable: Bảng băm tương thích J2ME được tối ưu hóa thao tác tra cứu O(1),
    /// cung cấp thêm Generic MyHashTable<TKey, TValue> cho các module hiện đại.
    /// </summary>
    public class MyHashTable : IEnumerable
    {
        public Hashtable h = new Hashtable();

        public object get(object k)
        {
            if (k == null) return null;
            return h[k];
        }

        public T get<T>(object k) where T : class
        {
            if (k == null) return null;
            return h[k] as T;
        }

        public void clear()
        {
            h.Clear();
        }

        public void Clear()
        {
            h.Clear();
        }

        public IDictionaryEnumerator GetEnumerator()
        {
            return h.GetEnumerator();
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return h.GetEnumerator();
        }

        public int size()
        {
            return h.Count;
        }

        public int Count => h.Count;

        public void put(object k, object v)
        {
            if (k != null)
            {
                h[k] = v; // Trực tiếp gán O(1), thay vì ContainsKey -> Remove -> Add
            }
        }

        public void remove(object k)
        {
            if (k != null)
            {
                h.Remove(k);
            }
        }

        public void Remove(string key)
        {
            if (key != null)
            {
                h.Remove(key);
            }
        }

        public bool containsKey(object key)
        {
            return key != null && h.ContainsKey(key);
        }

        public object this[object key]
        {
            get => get(key);
            set => put(key, value);
        }
    }

    /// <summary>
    /// Generic MyHashTable<TKey, TValue>: Loại bỏ 100% Boxing/Unboxing cho Dictionary hiện đại.
    /// </summary>
    public class MyHashTable<TKey, TValue> : IEnumerable<KeyValuePair<TKey, TValue>>
    {
        private readonly Dictionary<TKey, TValue> dict;

        public MyHashTable()
        {
            dict = new Dictionary<TKey, TValue>();
        }

        public MyHashTable(int capacity)
        {
            dict = new Dictionary<TKey, TValue>(capacity);
        }

        public TValue get(TKey key)
        {
            if (key == null) return default;
            dict.TryGetValue(key, out TValue val);
            return val;
        }

        public void put(TKey key, TValue val)
        {
            if (key != null)
            {
                dict[key] = val;
            }
        }

        public void clear() => dict.Clear();
        public int size() => dict.Count;
        public int Count => dict.Count;
        public bool containsKey(TKey key) => key != null && dict.ContainsKey(key);
        public bool remove(TKey key) => key != null && dict.Remove(key);

        public TValue this[TKey key]
        {
            get => get(key);
            set => put(key, value);
        }

        public IEnumerator<KeyValuePair<TKey, TValue>> GetEnumerator() => dict.GetEnumerator();
        IEnumerator IEnumerable.GetEnumerator() => dict.GetEnumerator();
    }
}

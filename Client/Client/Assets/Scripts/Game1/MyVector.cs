namespace Game1
{
    using System;
    using System.Collections;
    using System.Collections.Generic;

    /// <summary>
    /// MyVector: Collection tương thích J2ME được hiện đại hóa với Generic List bên trong,
    /// triệt tiêu cấp phát bộ nhớ thừa và hỗ trợ cả untyped lẫn typed generic.
    /// </summary>
    public class MyVector : IEnumerable
    {
        protected readonly List<object> a;

        public MyVector()
        {
            a = new List<object>();
        }

        public MyVector(int initialCapacity)
        {
            a = new List<object>(initialCapacity);
        }

        public MyVector(string s)
        {
            a = new List<object>();
        }

        public MyVector(ArrayList legacyList)
        {
            if (legacyList != null)
            {
                a = new List<object>(legacyList.Count);
                foreach (object item in legacyList)
                {
                    a.Add(item);
                }
            }
            else
            {
                a = new List<object>();
            }
        }

        public MyVector(IEnumerable<object> collection)
        {
            a = collection != null ? new List<object>(collection) : new List<object>();
        }

        public void addElement(object o)
        {
            a.Add(o);
        }

        public bool contains(object o)
        {
            return a.Contains(o);
        }

        public int size()
        {
            return a.Count;
        }

        public int Count => a.Count;

        public object elementAt(int index)
        {
            if (index >= 0 && index < a.Count)
            {
                return a[index];
            }
            return null;
        }

        public T elementAt<T>(int index) where T : class
        {
            if (index >= 0 && index < a.Count)
            {
                return a[index] as T;
            }
            return null;
        }

        public void set(int index, object obj)
        {
            if (index >= 0 && index < a.Count)
            {
                a[index] = obj;
            }
        }

        public void setElementAt(object obj, int index)
        {
            if (index >= 0 && index < a.Count)
            {
                a[index] = obj;
            }
        }

        public int indexOf(object o)
        {
            return a.IndexOf(o);
        }

        public void removeElementAt(int index)
        {
            if (index >= 0 && index < a.Count)
            {
                a.RemoveAt(index);
            }
        }

        public void removeElement(object o)
        {
            a.Remove(o);
        }

        public void removeAllElements()
        {
            a.Clear();
        }

        public void Clear()
        {
            a.Clear();
        }

        public void insertElementAt(object o, int i)
        {
            if (i >= 0 && i <= a.Count)
            {
                a.Insert(i, o);
            }
        }

        public object firstElement()
        {
            return a.Count > 0 ? a[0] : null;
        }

        public object lastElement()
        {
            return a.Count > 0 ? a[a.Count - 1] : null;
        }

        public object this[int index]
        {
            get => elementAt(index);
            set => set(index, value);
        }

        public IEnumerator GetEnumerator()
        {
            return a.GetEnumerator();
        }

        public List<object> ToList()
        {
            return new List<object>(a);
        }
    }

    /// <summary>
    /// Generic MyVector<T>: Hỗ trợ Type-Safe hoàn toàn, loại bỏ 100% Boxing/Unboxing cho mã mới.
    /// </summary>
    public class MyVector<T> : IEnumerable<T>
    {
        private readonly List<T> list;

        public MyVector()
        {
            list = new List<T>();
        }

        public MyVector(int capacity)
        {
            list = new List<T>(capacity);
        }

        public MyVector(IEnumerable<T> items)
        {
            list = items != null ? new List<T>(items) : new List<T>();
        }

        public void addElement(T item) => list.Add(item);
        public void Add(T item) => list.Add(item);
        public bool contains(T item) => list.Contains(item);
        public int size() => list.Count;
        public int Count => list.Count;
        public T elementAt(int index) => (index >= 0 && index < list.Count) ? list[index] : default;
        public T this[int index]
        {
            get => elementAt(index);
            set
            {
                if (index >= 0 && index < list.Count) list[index] = value;
            }
        }
        public int indexOf(T item) => list.IndexOf(item);
        public void removeElementAt(int index)
        {
            if (index >= 0 && index < list.Count) list.RemoveAt(index);
        }
        public bool removeElement(T item) => list.Remove(item);
        public void removeAllElements() => list.Clear();
        public void Clear() => list.Clear();
        public void insertElementAt(T item, int i)
        {
            if (i >= 0 && i <= list.Count) list.Insert(i, item);
        }
        public T firstElement() => list.Count > 0 ? list[0] : default;
        public T lastElement() => list.Count > 0 ? list[list.Count - 1] : default;

        public IEnumerator<T> GetEnumerator() => list.GetEnumerator();
        IEnumerator IEnumerable.GetEnumerator() => list.GetEnumerator();
    }
}

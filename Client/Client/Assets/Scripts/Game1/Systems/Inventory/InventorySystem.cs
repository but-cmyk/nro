namespace Game1.Systems.Inventory
{
    using System;
    using System.Collections.Generic;
    using Game1;

    /// <summary>
    /// InventorySystem: Module chuyên trách quản lý hành trang túi đồ, rương đồ và trang bị của nhân vật.
    /// Giúp phân tách logic dữ liệu vật phẩm ra khỏi giao diện Panel.cs và Char.cs.
    /// </summary>
    public class InventorySystem
    {
        public List<Item> ItemsBag { get; } = new List<Item>();
        public List<Item> ItemsBody { get; } = new List<Item>();
        public List<Item> ItemsBox { get; } = new List<Item>();

        public long Gold { get; set; }
        public int Gem { get; set; }
        public int Ruby { get; set; }

        public event Action OnInventoryChanged;

        public void SetBagItems(Item[] items)
        {
            ItemsBag.Clear();
            if (items != null)
            {
                ItemsBag.AddRange(items);
            }
            OnInventoryChanged?.Invoke();
        }

        public void SetBodyItems(Item[] items)
        {
            ItemsBody.Clear();
            if (items != null)
            {
                ItemsBody.AddRange(items);
            }
            OnInventoryChanged?.Invoke();
        }

        public void SetBoxItems(Item[] items)
        {
            ItemsBox.Clear();
            if (items != null)
            {
                ItemsBox.AddRange(items);
            }
            OnInventoryChanged?.Invoke();
        }

        public Item GetBagItemAt(int index)
        {
            if (index >= 0 && index < ItemsBag.Count)
            {
                return ItemsBag[index];
            }
            return null;
        }

        public Item GetBodyItemAt(int index)
        {
            if (index >= 0 && index < ItemsBody.Count)
            {
                return ItemsBody[index];
            }
            return null;
        }

        public int CountAvailableSlots(int maxSlots = 28)
        {
            int used = 0;
            foreach (var it in ItemsBag)
            {
                if (it != null && it.template != null)
                {
                    used++;
                }
            }
            return System.Math.Max(0, maxSlots - used);
        }
    }
}

using System;
using System.Collections.Generic;
using System.Text;
using UnityEngine;

namespace Game1.God
{
    public class AutoSettingItem
    {
        public string key;
        public string name;
        public int typeControl; // 0: Toggle [BẬT/TẮT], 1: Stepper [< >]
        public bool isEnabled;
        public int value;
        public int minVal;
        public int maxVal;
        public int stepVal;
        public Action<AutoSettingItem> onApply;

        public AutoSettingItem(string key, string name, int typeControl, bool defaultEnabled, int defaultValue, int minVal, int maxVal, int stepVal, Action<AutoSettingItem> onApply)
        {
            this.key = key;
            this.name = name;
            this.typeControl = typeControl;
            this.isEnabled = defaultEnabled;
            this.value = defaultValue;
            this.minVal = minVal;
            this.maxVal = maxVal;
            this.stepVal = stepVal;
            this.onApply = onApply;
        }
    }

    public class AutoSettingManager
    {
        private static AutoSettingManager instance;
        public List<AutoSettingItem> items = new List<AutoSettingItem>();
        public int lastLoadedCharId = -1;

        public static AutoSettingManager getInstance()
        {
            if (instance == null)
            {
                instance = new AutoSettingManager();
            }
            return instance;
        }

        public AutoSettingManager()
        {
            InitDefaultItems();
        }

        private void InitDefaultItems()
        {
            items.Clear();

            // 0. Tự động hồi sinh
            items.Add(new AutoSettingItem("ahs", "Tự Động Hồi Sinh", 0, false, 0, 0, 1, 1, (item) =>
            {
                Revive.getInstance().setRevive(item.isEnabled);
            }));

            // 1. Tự động đánh
            items.Add(new AutoSettingItem("ak", "Tự Động Đánh Quái", 0, false, 0, 0, 1, 1, (item) =>
            {
                nSkill.getInstance().canAttack = item.isEnabled;
            }));

            // 2. Tàn sát quái
            items.Add(new AutoSettingItem("ts", "Tàn Sát Quái", 0, false, 0, 0, 1, 1, (item) =>
            {
                Mobs.IsTanSat = item.isEnabled;
            }));

            // 3. Né siêu quái khi tàn sát
            items.Add(new AutoSettingItem("nesieuquai", "Né Siêu Quái Khi TS", 0, true, 0, 0, 1, 1, (item) =>
            {
                Mobs.neSieuQuai = item.isEnabled;
            }));

            // 4. Tự động nhặt đồ
            items.Add(new AutoSettingItem("pick", "Tự Động Nhặt Đồ", 0, false, 0, 0, 1, 1, (item) =>
            {
                Mobs.IsAutoPickItems = item.isEnabled;
            }));

            // 5. Bộ lọc nhặt đồ (0: Tất Cả, 1: Đồ Quý, 2: Không Rác)
            items.Add(new AutoSettingItem("pickfilter", "Lọc Nhặt Đồ", 1, false, 0, 0, 2, 1, (item) =>
            {
                Mobs.pickFilterMode = item.value;
            }));

            // 6. Tự động bơm đậu thần (0: Tắt, 10% -> 50%)
            items.Add(new AutoSettingItem("autobean", "Tự Bơm Đậu (HP/KI)", 1, false, 0, 0, 50, 10, (item) =>
            {
                Mobs.HpBuff = item.value;
                Mobs.MpBuff = item.value;
            }));

            // 7. Tự động hái đậu ở nhà khi chín
            items.Add(new AutoSettingItem("haidau", "Tự Hái Đậu Ở Nhà", 0, false, 0, 0, 1, 1, (item) =>
            {
                Mobs.autoThuHoachDau = item.isEnabled;
            }));

            // 8. Auto Up Đệ
            items.Add(new AutoSettingItem("upde", "Auto Up Đệ Tử", 0, false, 0, 0, 1, 1, (item) =>
            {
                PetService.getInstance().setUp(item.isEnabled);
            }));

            // 9. Thông báo Boss
            items.Add(new AutoSettingItem("boss", "Thông Báo Boss (5 Boss)", 0, true, 0, 0, 1, 1, (item) =>
            {
                Boss.getInstance().isShow = item.isEnabled;
            }));

            // 10. Hiệu ứng mưa bản đồ
            items.Add(new AutoSettingItem("rain", "Hiệu Ứng Mưa Bản Đồ", 0, true, 0, 0, 1, 1, (item) =>
            {
                BackgroudEffect.isEnableRain = item.isEnabled;
                if (!item.isEnabled)
                {
                    BackgroudEffect.clearAllRain();
                }
            }));

            // 11. Danh sách nhân vật trong map
            items.Add(new AutoSettingItem("dsnv", "D.S Nhân Vật Map", 0, false, 0, 0, 1, 1, (item) =>
            {
                ListChars.getInstance().isShow = item.isEnabled;
            }));

            // 12. Giảm đồ họa (Ẩn map)
            items.Add(new AutoSettingItem("lowg", "Giảm Đồ Họa (Ẩn Map)", 0, false, 0, 0, 1, 1, (item) =>
            {
                ListChars.getInstance().HideMap = item.isEnabled;
            }));

            // 13. Tự đăng nhập lại khi mất kết nối
            items.Add(new AutoSettingItem("alogin", "Tự Đăng Nhập Lại", 0, false, 0, 0, 1, 1, (item) =>
            {
                PlayerInfo.getInstance().canLogin = item.isEnabled;
            }));

            // 14. Tốc độ Game (1x -> 10x)
            items.Add(new AutoSettingItem("speed", "Tốc Độ Game", 1, false, 1, 1, 10, 1, (item) =>
            {
                Main.gameSpeed = Mathf.Clamp(item.value, item.minVal, item.maxVal);
            }));

            // 15. Tốc độ di chuyển (4 -> 30)
            items.Add(new AutoSettingItem("movespeed", "Tốc Độ Chạy", 1, false, 4, 4, 30, 1, (item) =>
            {
                int spd = Mathf.Clamp(item.value, item.minVal, item.maxVal);
                ClientManager.speedRun = spd;
                if (Char.myCharz() != null)
                {
                    Char.myCharz().cspeed = spd;
                }
            }));
        }

        public void updateItemState(string key, bool enabled)
        {
            for (int i = 0; i < items.Count; i++)
            {
                if (items[i].key.Equals(key))
                {
                    items[i].isEnabled = enabled;
                    break;
                }
            }
            if (Char.myCharz() != null)
            {
                Save(Char.myCharz().charID);
            }
        }

        public static string GetItemValueString(AutoSettingItem it)
        {
            if (it.key.Equals("pickfilter"))
            {
                switch (it.value)
                {
                    case 1: return "Đồ Quý";
                    case 2: return "Ko Rác";
                    default: return "Tất Cả";
                }
            }
            if (it.key.Equals("autobean"))
            {
                return (it.value == 0) ? "Tắt" : (it.value + "%");
            }
            if (it.maxVal <= 10)
            {
                return it.value + "x";
            }
            return it.value.ToString();
        }

        public void Save(int charId)
        {
            if (charId <= 0) return;
            try
            {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.Count; i++)
                {
                    AutoSettingItem it = items[i];
                    sb.Append(it.key).Append(":");
                    if (it.typeControl == 0)
                    {
                        sb.Append(it.isEnabled ? "1" : "0");
                    }
                    else
                    {
                        sb.Append(it.value);
                    }
                    if (i < items.Count - 1)
                    {
                        sb.Append(";");
                    }
                }
                Rms.saveRMSString("nro_auto_" + charId, sb.ToString());
            }
            catch (Exception ex)
            {
                Debug.LogException(ex);
            }
        }

        public void Load(int charId)
        {
            if (charId <= 0) return;
            lastLoadedCharId = charId;
            try
            {
                string raw = Rms.loadRMSString("nro_auto_" + charId);
                if (!string.IsNullOrEmpty(raw))
                {
                    string[] pairs = raw.Split(';');
                    for (int i = 0; i < pairs.Length; i++)
                    {
                        string[] kv = pairs[i].Split(':');
                        if (kv.Length == 2)
                        {
                            string k = kv[0].Trim();
                            string v = kv[1].Trim();
                            for (int j = 0; j < items.Count; j++)
                            {
                                if (items[j].key.Equals(k))
                                {
                                    if (items[j].typeControl == 0)
                                    {
                                        items[j].isEnabled = (v.Equals("1") || v.ToLower().Equals("true"));
                                    }
                                    else
                                    {
                                        int parsedVal;
                                        if (int.TryParse(v, out parsedVal))
                                        {
                                            items[j].value = Mathf.Clamp(parsedVal, items[j].minVal, items[j].maxVal);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                applyAll();
            }
            catch (Exception ex)
            {
                Debug.LogException(ex);
            }
        }

        public void applyAll()
        {
            for (int i = 0; i < items.Count; i++)
            {
                try
                {
                    items[i].onApply?.Invoke(items[i]);
                }
                catch (Exception)
                {
                }
            }
        }

        public void setTabAutoSetting(Panel panel)
        {
            panel.currentListLength = items.Count;
            panel.ITEM_HEIGHT = 26;
            panel.selected = (GameCanvas.isTouch ? (-1) : 0);
            panel.cmyLim = panel.currentListLength * panel.ITEM_HEIGHT - panel.hScroll;
            if (panel.cmyLim < 0)
            {
                panel.cmyLim = 0;
            }
            panel.cmy = (panel.cmtoY = 0);
        }

        public void paint(mGraphics g, Panel panel)
        {
            g.setClip(panel.xScroll, panel.yScroll, panel.wScroll, panel.hScroll);
            g.translate(0, -panel.cmy);

            for (int i = 0; i < items.Count; i++)
            {
                int x = panel.xScroll;
                int num = panel.yScroll + i * panel.ITEM_HEIGHT;
                int num2 = panel.wScroll - 1;
                int h = panel.ITEM_HEIGHT - 1;

                // Viewport culling
                if (num - panel.cmy <= panel.yScroll + panel.hScroll && num - panel.cmy >= panel.yScroll - panel.ITEM_HEIGHT)
                {
                    AutoSettingItem it = items[i];

                    // Item background
                    g.setColor((i != panel.selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);

                    // Name
                    mFont.tahoma_7b_dark.drawString(g, it.name, x + 8, num + 6, mFont.LEFT);

                    if (it.typeControl == 0) // TOGGLE
                    {
                        if (it.isEnabled)
                        {
                            // Green ON button
                            g.setColor(2532152);
                            g.fillRect(x + num2 - 56, num + 3, 48, h - 6);
                            mFont.tahoma_7b_white.drawString(g, "BẬT", x + num2 - 32, num + 6, mFont.CENTER);
                        }
                        else
                        {
                            // Gray OFF button
                            g.setColor(11184810);
                            g.fillRect(x + num2 - 56, num + 3, 48, h - 6);
                            mFont.tahoma_7b_white.drawString(g, "TẮT", x + num2 - 32, num + 6, mFont.CENTER);
                        }
                    }
                    else if (it.typeControl == 1) // STEPPER [< >]
                    {
                        // Decrement [<]
                        g.setColor(13553358);
                        g.fillRect(x + num2 - 86, num + 3, 20, h - 6);
                        mFont.tahoma_7b_dark.drawString(g, "<", x + num2 - 76, num + 6, mFont.CENTER);

                        // Value
                        string valStr = GetItemValueString(it);
                        mFont.tahoma_7b_blue.drawString(g, valStr, x + num2 - 45, num + 6, mFont.CENTER);

                        // Increment [>]
                        g.setColor(13553358);
                        g.fillRect(x + num2 - 24, num + 3, 20, h - 6);
                        mFont.tahoma_7b_dark.drawString(g, ">", x + num2 - 14, num + 6, mFont.CENTER);
                    }
                }
            }

            panel.paintScrollArrow(g);
        }

        public void doFire(Panel panel, int selected)
        {
            if (selected < 0 || selected >= items.Count) return;

            AutoSettingItem it = items[selected];
            if (it.typeControl == 0) // TOGGLE
            {
                it.isEnabled = !it.isEnabled;
                it.onApply?.Invoke(it);
                Utils.addInfo1(it.name, it.isEnabled);
            }
            else if (it.typeControl == 1) // STEPPER
            {
                int x = panel.xScroll;
                int num2 = panel.wScroll - 1;
                // Kiểm tra nếu chạm hoặc click vào nửa nút [<] (tâm là x + num2 - 45)
                if (GameCanvas.px < x + num2 - 45)
                {
                    it.value -= it.stepVal;
                    if (it.value < it.minVal) it.value = it.minVal;
                }
                else
                {
                    it.value += it.stepVal;
                    if (it.value > it.maxVal) it.value = it.maxVal;
                }
                it.onApply?.Invoke(it);
                string valStr = GetItemValueString(it);
                GameScr.info1.addInfo(it.name + ": " + valStr, 0);
            }

            if (Char.myCharz() != null)
            {
                Save(Char.myCharz().charID);
            }
        }

        public void doKeyStep(int selected, int delta)
        {
            if (selected < 0 || selected >= items.Count) return;
            AutoSettingItem it = items[selected];
            if (it.typeControl == 1)
            {
                it.value += (delta * it.stepVal);
                it.value = Mathf.Clamp(it.value, it.minVal, it.maxVal);
                it.onApply?.Invoke(it);
                string valStr = GetItemValueString(it);
                GameScr.info1.addInfo(it.name + ": " + valStr, 0);

                if (Char.myCharz() != null)
                {
                    Save(Char.myCharz().charID);
                }
            }
        }
    }
}

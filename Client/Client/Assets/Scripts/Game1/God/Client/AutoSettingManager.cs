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

    public class MobTanSatEntry
    {
        public int templateId;
        public string name;
        public long hp;
        public int count;
        public bool isAll;
        public string infoStr;

        public MobTanSatEntry(int templateId, string name, long hp, int count, bool isAll)
        {
            this.templateId = templateId;
            this.name = name;
            this.hp = hp;
            this.count = count;
            this.isAll = isAll;
            if (isAll)
            {
                this.infoStr = "(Đánh mọi loại quái trong map)";
            }
            else if (templateId == -2)
            {
                this.infoStr = string.Empty;
            }
            else
            {
                this.infoStr = "HP: " + Res.formatNumber2(hp) + " | SL: " + count;
            }
        }
    }

    public class AutoSettingManager
    {
        private static AutoSettingManager instance;
        public List<AutoSettingItem> items = new List<AutoSettingItem>();
        public List<MobTanSatEntry> mobEntries = new List<MobTanSatEntry>();
        public int lastLoadedCharId = -1;
        public int lastMobListSize = -1;
        public int lastMobMapId = -1;

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
            items.Add(new AutoSettingItem("ak", "Tự Động Đánh", 0, false, 0, 0, 1, 1, (item) =>
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

            // 4. Chọn quái tàn sát
            items.Add(new AutoSettingItem("ts_mobs", "-> Chọn Quái Tàn Sát...", 2, false, 0, 0, 0, 0, (item) =>
            {
                if (GameCanvas.panel != null && GameCanvas.panel.type == 28)
                {
                    GameCanvas.panel.currentTabIndex = 1;
                    setTabMobTanSat(GameCanvas.panel);
                }
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

            // 14. Tốc độ Game (0.8x -> 10x)
            items.Add(new AutoSettingItem("speed", "Tốc Độ Game", 1, false, 1, 1, 10, 1, (item) =>
            {
                if (item.value <= 1)
                {
                    Main.gameSpeed = 0.8f;
                }
                else
                {
                    Main.gameSpeed = Mathf.Clamp(item.value, item.minVal, item.maxVal);
                }
            }));

            // 15. Tốc độ di chuyển (1 -> 30)
            items.Add(new AutoSettingItem("movespeed", "Tốc Độ Chạy", 1, false, 3, 1, 30, 1, (item) =>
            {
                int spd = Mathf.Clamp(item.value, item.minVal, item.maxVal);
                ClientManager.speedRun = spd;
                if (Char.myCharz() != null)
                {
                    Char.myCharz().cspeed = spd;
                }
            }));
        }

        public bool toggleItem(string key)
        {
            for (int i = 0; i < items.Count; i++)
            {
                if (items[i].key.Equals(key))
                {
                    items[i].isEnabled = !items[i].isEnabled;
                    try
                    {
                        items[i].onApply?.Invoke(items[i]);
                    }
                    catch (Exception)
                    {
                    }
                    Save(Char.myCharz() != null ? Char.myCharz().charID : lastLoadedCharId);
                    return items[i].isEnabled;
                }
            }
            return false;
        }

        public void updateItemState(string key, bool enabled)
        {
            for (int i = 0; i < items.Count; i++)
            {
                if (items[i].key.Equals(key))
                {
                    items[i].isEnabled = enabled;
                    try
                    {
                        items[i].onApply?.Invoke(items[i]);
                    }
                    catch (Exception)
                    {
                    }
                    break;
                }
            }
            Save(Char.myCharz() != null ? Char.myCharz().charID : lastLoadedCharId);
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
            if (it.key.Equals("speed"))
            {
                return (it.value <= 1) ? "0.8x" : (it.value + "x");
            }
            if (it.maxVal <= 10)
            {
                return it.value + "x";
            }
            return it.value.ToString();
        }

        public void Save(int charId)
        {
            if (charId <= 0 && Char.myCharz() != null)
            {
                charId = Char.myCharz().charID;
            }
            try
            {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < items.Count; i++)
                {
                    AutoSettingItem it = items[i];
                    if (it.typeControl == 2) continue;
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
                string data = sb.ToString();
                if (charId > 0)
                {
                    Rms.saveRMSString("nro_auto_" + charId, data);
                }
                Rms.saveRMSString("nro_auto_default", data);
            }
            catch (Exception ex)
            {
                Debug.LogException(ex);
            }
        }

        public void Load(int charId)
        {
            if (charId <= 0 && Char.myCharz() != null)
            {
                charId = Char.myCharz().charID;
            }
            // Nếu đã load cấu hình cho nhân vật này trong phiên chơi, khi chuyển map giữ nguyên dữ liệu trong RAM
            if (charId > 0 && lastLoadedCharId == charId && items.Count > 0)
            {
                applyAll();
                return;
            }
            if (charId > 0)
            {
                lastLoadedCharId = charId;
            }
            try
            {
                string raw = null;
                if (charId > 0)
                {
                    raw = Rms.loadRMSString("nro_auto_" + charId);
                }
                if (string.IsNullOrEmpty(raw))
                {
                    raw = Rms.loadRMSString("nro_auto_default");
                }
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
                                            if (items[j].key.Equals("movespeed") && parsedVal == 4)
                                            {
                                                parsedVal = 2;
                                            }
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
                    else if (it.typeControl == 2) // BUTTON / ACTION
                    {
                        g.setColor(4348574);
                        g.fillRect(x + num2 - 56, num + 3, 48, h - 6);
                        mFont.tahoma_7b_white.drawString(g, "MỞ >", x + num2 - 32, num + 6, mFont.CENTER);
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
            else if (it.typeControl == 2) // BUTTON / ACTION
            {
                it.onApply?.Invoke(it);
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

        public void UpdateCurrentMapMobs()
        {
            mobEntries.Clear();

            // Entry 0: Tàn Sát Tất Cả
            mobEntries.Add(new MobTanSatEntry(-1, "Tàn Sát Tất Cả Quái", 0, 0, true));

            bool hasAnyMobInMap = false;
            if (GameScr.vMob != null && GameScr.vMob.size() > 0)
            {
                Dictionary<int, MobTanSatEntry> dict = new Dictionary<int, MobTanSatEntry>();
                for (int i = 0; i < GameScr.vMob.size(); i++)
                {
                    Mob mob = (Mob)GameScr.vMob.elementAt(i);
                    if (mob == null || mob.isMobMe) continue;

                    int tid = mob.templateId;
                    if (!dict.ContainsKey(tid))
                    {
                        string mobName = "Quái #" + tid;
                        long maxHp = mob.maxHp;
                        MobTemplate t = mob.getTemplate();
                        if (t != null)
                        {
                            if (!string.IsNullOrEmpty(t.name)) mobName = t.name;
                            if (t.hp > 0) maxHp = t.hp;
                        }
                        dict[tid] = new MobTanSatEntry(tid, mobName, maxHp, 1, false);
                    }
                    else
                    {
                        dict[tid].count++;
                        dict[tid].infoStr = "HP: " + Res.formatNumber2(dict[tid].hp) + " | SL: " + dict[tid].count;
                    }
                }

                foreach (var kv in dict)
                {
                    mobEntries.Add(kv.Value);
                    hasAnyMobInMap = true;
                }
            }

            if (!hasAnyMobInMap)
            {
                mobEntries.Add(new MobTanSatEntry(-2, "(Map hiện tại không có quái)", 0, 0, false));
            }

            // Kiểm tra tính hợp lệ của Mobs.TypeMobsTanSat với map hiện tại
            if (Mobs.TypeMobsTanSat != null && Mobs.TypeMobsTanSat.Count > 0)
            {
                bool anyMatched = false;
                for (int i = 0; i < mobEntries.Count; i++)
                {
                    if (mobEntries[i].templateId >= 0 && Mobs.TypeMobsTanSat.Contains(mobEntries[i].templateId))
                    {
                        anyMatched = true;
                        break;
                    }
                }
                // Nếu không có bất kỳ quái nào trong danh sách chọn tồn tại trong map mới -> trở về tàn sát tất cả
                if (!anyMatched && hasAnyMobInMap)
                {
                    Mobs.TypeMobsTanSat.Clear();
                }
            }
        }

        public void setTabMobTanSat(Panel panel)
        {
            UpdateCurrentMapMobs();
            lastMobListSize = (GameScr.vMob != null) ? GameScr.vMob.size() : 0;
            lastMobMapId = TileMap.mapID;
            panel.currentListLength = mobEntries.Count;
            panel.ITEM_HEIGHT = 28;
            panel.selected = (GameCanvas.isTouch ? (-1) : 0);
            panel.cmyLim = panel.currentListLength * panel.ITEM_HEIGHT - panel.hScroll;
            if (panel.cmyLim < 0)
            {
                panel.cmyLim = 0;
            }
            panel.cmy = (panel.cmtoY = 0);
        }

        public void paintMobTanSat(mGraphics g, Panel panel)
        {
            g.setClip(panel.xScroll, panel.yScroll, panel.wScroll, panel.hScroll);
            g.translate(0, -panel.cmy);

            for (int i = 0; i < mobEntries.Count; i++)
            {
                int x = panel.xScroll;
                int num = panel.yScroll + i * panel.ITEM_HEIGHT;
                int num2 = panel.wScroll - 1;
                int h = panel.ITEM_HEIGHT - 1;

                // Viewport culling
                if (num - panel.cmy <= panel.yScroll + panel.hScroll && num - panel.cmy >= panel.yScroll - panel.ITEM_HEIGHT)
                {
                    MobTanSatEntry entry = mobEntries[i];

                    // Item background
                    g.setColor((i != panel.selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);

                    if (entry.templateId == -2)
                    {
                        mFont.tahoma_7_grey.drawString(g, entry.name, x + num2 / 2, num + 8, mFont.CENTER);
                        continue;
                    }

                    // Tên quái
                    mFont.tahoma_7b_dark.drawString(g, entry.name, x + 8, num + 3, mFont.LEFT);

                    // Thông tin phụ (HP, SL hoặc mô tả)
                    if (!string.IsNullOrEmpty(entry.infoStr))
                    {
                        mFont.tahoma_7_grey.drawString(g, entry.infoStr, x + 8, num + 15, mFont.LEFT);
                    }

                    // Trạng thái Checkbox
                    bool isChecked = false;
                    if (entry.isAll)
                    {
                        isChecked = (Mobs.TypeMobsTanSat.Count == 0);
                    }
                    else
                    {
                        isChecked = Mobs.TypeMobsTanSat.Contains(entry.templateId);
                    }

                    int boxW = 24;
                    int boxH = 18;
                    int boxX = x + num2 - 32;
                    int boxY = num + (h - boxH) / 2;

                    if (isChecked)
                    {
                        // Checkbox nền xanh lá nổi bật
                        g.setColor(2532152);
                        g.fillRect(boxX, boxY, boxW, boxH);
                        g.setColor(14013909);
                        g.drawRect(boxX, boxY, boxW, boxH);
                        mFont.tahoma_7b_white.drawString(g, "X", boxX + boxW / 2, boxY + 2, mFont.CENTER);
                    }
                    else
                    {
                        // Checkbox rỗng xám viền đậm
                        g.setColor(13553358);
                        g.fillRect(boxX, boxY, boxW, boxH);
                        g.setColor(11184810);
                        g.drawRect(boxX, boxY, boxW, boxH);
                    }
                }
            }

            panel.paintScrollArrow(g);
        }

        public void doFireMobTanSat(Panel panel, int selected)
        {
            if (selected < 0 || selected >= mobEntries.Count) return;

            MobTanSatEntry entry = mobEntries[selected];
            if (entry.templateId == -2) return;

            if (entry.isAll)
            {
                // Chọn tàn sát tất cả
                Mobs.TypeMobsTanSat.Clear();
                GameScr.info1.addInfo("Tàn sát tất cả quái trong map", 0);
            }
            else
            {
                int tid = entry.templateId;
                if (Mobs.TypeMobsTanSat.Count == 0)
                {
                    // Trước đó đang tàn sát tất cả -> chuyển sang chọn riêng quái này
                    Mobs.TypeMobsTanSat.Clear();
                    Mobs.TypeMobsTanSat.Add(tid);
                    GameScr.info1.addInfo("Tàn sát: " + entry.name, 0);
                }
                else
                {
                    if (Mobs.TypeMobsTanSat.Contains(tid))
                    {
                        // Đã chọn -> bỏ chọn
                        Mobs.TypeMobsTanSat.Remove(tid);
                        if (Mobs.TypeMobsTanSat.Count == 0)
                        {
                            GameScr.info1.addInfo("Tàn sát tất cả quái trong map", 0);
                        }
                        else
                        {
                            GameScr.info1.addInfo("Bỏ chọn: " + entry.name, 0);
                        }
                    }
                    else
                    {
                        // Chưa chọn -> thêm vào danh sách tàn sát
                        Mobs.TypeMobsTanSat.Add(tid);
                        GameScr.info1.addInfo("Chọn thêm: " + entry.name, 0);
                    }
                }
            }

            // Reset focus ngay lập tức để chuyển mục tiêu sang loại quái mới chọn
            if (Char.myCharz() != null)
            {
                Char.myCharz().mobFocus = null;
                Char.myCharz().charFocus = null;
                Char.myCharz().currentMovePoint = null;
            }

            // Tự động bật Tàn Sát Quái nếu đang tắt để người chơi được hưởng ngay
            if (!Mobs.IsTanSat)
            {
                updateItemState("ts", true);
            }
        }
    }
}

using System;
using System.Collections.Generic;

namespace Game1.God
{
    /*Author: HAIRMOD*/
    public class BossData
    {
        private static BossData instance{ get; set; }
        private string map;
        public string name;
        public List<BossData> listData = new List<BossData>();
        public DateTime? timeStart;

        public static BossData getInstance()
        {
            return instance == null ? instance = new BossData() : instance;
        }

        public string getElapsedTime()
        {
            if (this.timeStart == null) return "00:00";
            TimeSpan elapsed = DateTime.Now - this.timeStart.Value;
            if (elapsed.TotalSeconds < 0)
            {
                elapsed = TimeSpan.Zero;
            }
            if (elapsed.TotalHours >= 1)
            {
                return string.Format("{0:D2}:{1:D2}:{2:D2}", (int)elapsed.TotalHours, elapsed.Minutes, elapsed.Seconds);
            }
            return string.Format("{0:D2}:{1:D2}", elapsed.Minutes, elapsed.Seconds);
        }

        public string getDisplayString()
        {
            return this.name + " - " + this.getMapName() + " [" + this.getElapsedTime() + "]";
        }

        public string getStartTimeSpan()
        {
            return getElapsedTime();
        }

        public string getMapName()
        {
            if (this.map != null && !(this.map == ""))
            {
                return this.map;
            }
            return "Chưa có thông tin";
        }

        public void getBOSSInfo(string string_0)
        {
            if (string.IsNullOrEmpty(string_0)) return;
            try
            {
                string text = string_0.StartsWith("BOSS ") ? string_0.Substring(5) : (string_0.StartsWith("Boss ") ? string_0.Substring(5) : string_0);
                string[] array = text.Replace(" vừa xuất hiện tại ", "|").Replace(" appear at ", "|").Split('|');
                if (array.Length < 2) return;

                DateTime now = DateTime.Now;
                string bName = array[0].Trim();
                string bMap = array[1].Trim();

                BossData bossInfo = new BossData
                {
                    name = bName,
                    map = bMap,
                    timeStart = now
                };

                listData.Add(bossInfo);
                if (listData.Count > 5)
                {
                    listData.RemoveAt(0);
                }
            }
            catch (Exception)
            {
            }
        }
    }
}

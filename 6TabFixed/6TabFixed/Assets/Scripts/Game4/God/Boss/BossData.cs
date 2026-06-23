using System;
using System.Collections.Generic;

namespace Game4.God
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
        public string getStartTimeSpan()
        {
            TimeSpan timeSpan = DateTime.Now.Subtract(this.timeStart.Value);
            int num = (int)timeSpan.TotalSeconds;
            return string.Concat(new string[]
            {
            this.timeStart.Value.ToString("HH"),
            "h:",
            this.timeStart.Value.ToString("mm")
            });
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
            string[] array = string_0.Replace(string_0.StartsWith("BOSS") ? "BOSS " : "Boss ", "").Replace(" vừa xuất hiện tại ", "|").Replace(" appear at ", "|")
                .Split(new char[] { '|' });
            BossData bossInfo = new BossData
            {
                name = array[0].Trim(),
                map = array[1].Trim(),
                timeStart = new DateTime?(DateTime.Now)
            };
            listData.Add(bossInfo);
            if (listData.Count > 5)
            {
                listData.RemoveAt(0);
            }
        }
    }
}

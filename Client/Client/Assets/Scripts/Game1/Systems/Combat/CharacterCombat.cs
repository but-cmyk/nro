namespace Game1.Systems.Combat
{
    using System;
    using Game1;
    using Char = Game1.Char;

    /// <summary>
    /// CharacterCombat: Module chuyên trách quản lý mục tiêu, hoạt ảnh ra chiêu và trạng thái chiến đấu của nhân vật.
    /// Giúp phân tách logic chiến đấu ra khỏi class khổng lồ Char.cs.
    /// </summary>
    public class CharacterCombat
    {
        private readonly Char owner;

        public Mob MobFocus { get; private set; }
        public Char CharFocus { get; private set; }
        public SkillPaint CurrentSkillPaint { get; private set; }
        public SkillPaint RandomSkillPaint { get; private set; }

        public bool HasSentAttack { get; set; }
        public long LastAttackTimestamp { get; private set; }

        public CharacterCombat(Char owner)
        {
            this.owner = owner ?? throw new ArgumentNullException(nameof(owner));
        }

        public void SetTarget(Mob mob)
        {
            MobFocus = mob;
            CharFocus = null;
        }

        public void SetTarget(Char targetChar)
        {
            CharFocus = targetChar;
            MobFocus = null;
        }

        public void ClearTarget(bool pkModeOnly = false)
        {
            if (pkModeOnly)
            {
                if (CharFocus != null)
                {
                    MobFocus = null;
                }
            }
            else
            {
                MobFocus = null;
                CharFocus = null;
            }
        }

        public bool HasValidTarget()
        {
            if (MobFocus != null && MobFocus.status != 0 && MobFocus.status != 1 && MobFocus.hp > 0)
            {
                return true;
            }
            if (CharFocus != null && CharFocus.statusMe != 14 && CharFocus.statusMe != 5 && CharFocus.cHP > 0)
            {
                return true;
            }
            return false;
        }

        /// <summary>
        /// Chuẩn bị hoạt ảnh kỹ năng an toàn với cơ chế fallback tự động.
        /// </summary>
        public SkillPaint ResolveSkillPaint(int skillId)
        {
            SkillPaint sp = null;
            if (GameScr.sks != null && skillId >= 0 && skillId < GameScr.sks.Length)
            {
                sp = GameScr.sks[skillId];
            }
            if (sp == null && GameScr.sks != null && GameScr.sks.Length > 0)
            {
                sp = GameScr.sks[0]; // Fallback đấm cơ bản
            }
            CurrentSkillPaint = sp;
            return sp;
        }

        public void RecordAttack()
        {
            HasSentAttack = true;
            LastAttackTimestamp = mSystem.currentTimeMillis();
        }

        public bool CanPerformAttack(long minIntervalMs = 300)
        {
            long now = mSystem.currentTimeMillis();
            return (now - LastAttackTimestamp) >= minIntervalMs;
        }
    }
}

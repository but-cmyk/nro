namespace Game1
{
    using System;

    /// <summary>
    /// CombatPacketHandler: Chuyên trách phân tích và xử lý các gói tin chiến đấu, sát thương, hồi sinh và kỹ năng.
    /// Opcodes: 56 (sát thương nhận vào), 84 (hồi sinh), -94 (hồi chiêu kỹ năng / cooldown), 83 (pet tấn công quái).
    /// </summary>
    public class CombatPacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case NetworkOpcodes.CHAR_INJURE:
                        {
                            GameCanvas.debug("SXX6", 2);
                            int charId = msg.reader().readInt();
                            if (charId == Char.myCharz().charID)
                            {
                                bool isFatal = false;
                                Char myChar = Char.myCharz();
                                myChar.cHP = msg.readInt3Byte();
                                int damage = msg.readInt3Byte();
                                Res.outz("dame hit = " + damage);
                                if (damage != 0)
                                {
                                    myChar.doInjure();
                                }
                                try
                                {
                                    isFatal = msg.reader().readBoolean();
                                    sbyte effId = msg.reader().readByte();
                                    if (effId != -1)
                                    {
                                        Res.outz("hit eff= " + effId);
                                        EffecMn.addEff(new Effect(effId, myChar.cx, myChar.cy, 3, 1, -1));
                                    }
                                }
                                catch (Exception)
                                {
                                }
                                if (myChar.cTypePk != (int)PkMode.Clan)
                                {
                                    if (damage == 0)
                                    {
                                        GameScr.startFlyText(mResources.miss, myChar.cx, myChar.cy - myChar.ch, 0, -3, mFont.MISS_ME);
                                    }
                                    else
                                    {
                                        GameScr.startFlyText("-" + damage, myChar.cx, myChar.cy - myChar.ch, 0, -3, isFatal ? mFont.FATAL : mFont.RED);
                                    }
                                }
                                return true;
                            }

                            Char other = GameScr.findCharInMap(charId);
                            if (other == null)
                            {
                                return true;
                            }
                            other.cHP = msg.readInt3Byte();
                            bool isFatalOther = false;
                            int damageOther = msg.readInt3Byte();
                            if (damageOther != 0)
                            {
                                other.doInjure();
                            }
                            try
                            {
                                isFatalOther = msg.reader().readBoolean();
                                sbyte effIdOther = msg.reader().readByte();
                                if (effIdOther != -1)
                                {
                                    Res.outz("hit eff= " + effIdOther);
                                    EffecMn.addEff(new Effect(effIdOther, other.cx, other.cy, 3, 1, -1));
                                }
                            }
                            catch (Exception)
                            {
                            }
                            if (other.cTypePk != (int)PkMode.Clan)
                            {
                                if (damageOther == 0)
                                {
                                    GameScr.startFlyText(mResources.miss, other.cx, other.cy - other.ch, 0, -3, mFont.MISS);
                                }
                                else
                                {
                                    GameScr.startFlyText("-" + damageOther, other.cx, other.cy - other.ch, 0, -3, isFatalOther ? mFont.FATAL : mFont.ORANGE);
                                }
                            }
                            return true;
                        }

                    case NetworkOpcodes.REVIVE:
                        {
                            int targetId = msg.reader().readInt();
                            Char revivedChar = (targetId == Char.myCharz().charID) ? Char.myCharz() : GameScr.findCharInMap(targetId);
                            if (revivedChar == null)
                            {
                                return true;
                            }
                            revivedChar.cHP = revivedChar.cHPFull;
                            revivedChar.cMP = revivedChar.cMPFull;
                            revivedChar.cx = msg.reader().readShort();
                            revivedChar.cy = msg.reader().readShort();
                            revivedChar.liveFromDead();
                            return true;
                        }

                    case NetworkOpcodes.MOB_ME_ATTACK:
                        {
                            GameCanvas.debug("SXX8", 2);
                            int charId = msg.reader().readInt();
                            Char owner = ((charId != Char.myCharz().charID) ? GameScr.findCharInMap(charId) : Char.myCharz());
                            if (owner == null)
                            {
                                return true;
                            }
                            Mob mobToAttack = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            if (owner.mobMe != null)
                            {
                                owner.mobMe.attackOtherMob(mobToAttack);
                            }
                            return true;
                        }

                    case NetworkOpcodes.SKILL_COOLDOWN:
                        {
                            while (msg.reader().available() > 0)
                            {
                                short skillId = msg.reader().readShort();
                                int cooldown = msg.reader().readInt();
                                for (int k = 0; k < Char.myCharz().vSkill.size(); k++)
                                {
                                    Skill skill = (Skill)Char.myCharz().vSkill.elementAt(k);
                                    if (skill != null && skill.skillId == skillId)
                                    {
                                        if (cooldown < skill.coolDown)
                                        {
                                            skill.lastTimeUseThisSkill = mSystem.currentTimeMillis() - (skill.coolDown - cooldown);
                                        }
                                        Res.outz("1 chieu id= " + skill.template.id + " cooldown= " + cooldown + "curr cool down= " + skill.coolDown);
                                    }
                                }
                            }
                            return true;
                        }
                }
            }
            catch (Exception ex)
            {
                Res.err("[CombatPacketHandler] Error handling cmd " + msg.command + ": " + ex.Message);
            }
            return false;
        }
    }
}

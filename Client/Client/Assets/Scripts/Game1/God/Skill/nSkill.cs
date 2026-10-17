using System;
using System.Diagnostics;

namespace Game1.God
{
    /*Author: HairMod*/
    public class nSkill
    {
        private static nSkill instance { get; set; }
        public bool canAttack;
        private long[] timeAttack = new long[10];
        public static nSkill getInstance()
        {
            return instance == null ? instance = new nSkill() : instance;
        }
        private int getSkill()
        {
            for (int i = 0; i < GameScr.keySkill.Length; i++)
            {
                if (GameScr.keySkill[i] == Char.myCharz().myskill)
                {
                    return i;
                }
            }
            return 0;
        }
        private static readonly MyVector attackMobVector = new MyVector();
        private static readonly MyVector attackCharVector = new MyVector();
        private static readonly MyVector emptyVector = new MyVector();
        private void vAttack()
        {
            try
            {
                attackMobVector.removeAllElements();
                attackMobVector.addElement(Char.myCharz().mobFocus);
                Service.gI().sendPlayerAttack(attackMobVector, emptyVector, 1);
            }
            catch(Exception e)
            {
                UnityEngine.Debug.LogException(e);
            }
        }
        private void sendAttackPlayer()
        {
            try
            {
                attackCharVector.removeAllElements();
                attackCharVector.addElement(Char.myCharz().charFocus);
                Service.gI().sendPlayerAttack(emptyVector, attackCharVector, 2);
            }
            catch
            {
            }
        }
        private long getMana(Skill skill)
        {
            if (skill.template.id != 20 && skill.template.id != 22 && skill.template.id != 7 && skill.template.id != 18)
            {
                if (skill.template.id != 23)
                {
                    long num = (long)((double)skill.coolDown * 1.2);
                    if (num < 415L)
                    {
                        return 415L;
                    }
                    return num;
                }
            }
            return (long)skill.coolDown + 500L;
        }
        public bool checkSkill(Char @char)
        {
            if (@char == null || @char == Char.myCharz() || Char.myCharz().myskill == null)
            {
                return false;
            }
            if (@char.statusMe == 14 || @char.statusMe == 5 || @char.cHP <= 0)
            {
                return false;
            }
            if (Char.myCharz().myskill.template.type == 2 || Char.myCharz().myskill.template.type == 3)
            {
                return false;
            }

            // Map Đại hội võ thuật / Lôi đài
            if (TileMap.mapID == 113)
            {
                return @char.cTypePk == 5 || @char.cTypePk == 3;
            }

            // Kiểm tra Boss: Boss thường có cTypePk == 5 hoặc theo nhận diện danh sách Boss
            if (@char.cTypePk == 5 || ListChars.getInstance().isBoss(@char))
            {
                return true;
            }

            // Kiểm tra các điều kiện PK đối kháng tiêu chuẩn
            if (Char.myCharz().isMeCanAttackOtherPlayer(@char))
            {
                return true;
            }

            if (Char.myCharz().cTypePk == 5 || @char.cTypePk == 3 || @char.cTypePk == 1 || @char.cTypePk == 4)
            {
                return true;
            }
            if (Char.myCharz().cFlag != 0 && @char.cFlag != 0 && (Char.myCharz().cFlag == 8 || @char.cFlag == 8 || Char.myCharz().cFlag != @char.cFlag))
            {
                return true;
            }

            return false;
        }

        public void sendAttack()
        {
            Char myChar = Char.myCharz();
            if (myChar == null || myChar.myskill == null)
            {
                return;
            }
            if (myChar.meDead || myChar.cHP <= 0 || myChar.statusMe == 14 || myChar.statusMe == 5)
            {
                return;
            }
            if (myChar.myskill.template.type == 3 || myChar.myskill.template.id == 10 || myChar.myskill.template.id == 11)
            {
                return;
            }
            if (myChar.myskill.paintCanNotUseSkill && !GameCanvas.panel.isShow)
            {
                return;
            }

            int num = this.getSkill();
            if (mSystem.currentTimeMillis() - this.timeAttack[num] <= this.getMana(myChar.myskill))
            {
                return;
            }

            // 1. Ưu tiên mục tiêu Boss / Char (người chơi đang focus)
            Char cFocus = myChar.charFocus;
            if (cFocus != null)
            {
                if (cFocus.statusMe == 14 || cFocus.statusMe == 5 || cFocus.cHP <= 0)
                {
                    myChar.charFocus = null;
                }
                else if (this.checkSkill(cFocus))
                {
                    int reachX = System.Math.Max(80, (int)(myChar.myskill.dx * 1.7));
                    int reachY = System.Math.Max(80, (int)(myChar.myskill.dy * 1.7));
                    if (Res.abs(cFocus.cx - myChar.cx) <= reachX && Res.abs(cFocus.cy - myChar.cy) <= reachY)
                    {
                        myChar.myskill.lastTimeUseThisSkill = mSystem.currentTimeMillis();
                        this.sendAttackPlayer();
                        this.timeAttack[num] = mSystem.currentTimeMillis();
                        return;
                    }
                }
            }

            // 2. Mục tiêu Mob
            Mob mFocus = myChar.mobFocus;
            if (mFocus != null)
            {
                if (mFocus.status == 0 || mFocus.status == 1 || mFocus.hp <= 0)
                {
                    myChar.mobFocus = null;
                }
                else if (GameScr.gI().isMeCanAttackMob(mFocus))
                {
                    int reachX = System.Math.Max(80, (int)(myChar.myskill.dx * 1.7));
                    int reachY = System.Math.Max(80, (int)(myChar.myskill.dy * 1.7));
                    int targetX = (mFocus.x != 0) ? mFocus.x : mFocus.xFirst;
                    int targetY = (mFocus.y != 0) ? mFocus.y : mFocus.yFirst;
                    if (Res.abs(targetX - myChar.cx) <= reachX && Res.abs(targetY - myChar.cy) <= reachY)
                    {
                        myChar.myskill.lastTimeUseThisSkill = mSystem.currentTimeMillis();
                        this.vAttack();
                        this.timeAttack[num] = mSystem.currentTimeMillis();
                        return;
                    }
                }
            }
        }
        public void Update()
        {
            if (canAttack) sendAttack();
        }
    }
}

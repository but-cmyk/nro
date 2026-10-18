
using System;
using System.Collections.Generic;
using System.Linq;
namespace Game1.God
{
    public class MobController
    {
        private enum TpyePickItem
        {
            CanNotPickItem,
            PickItemNormal,
            PickItemTDLT,
            PickItemTanSat
        }

        private static readonly sbyte[] IdSkillsMelee = new sbyte[5] { 0, 9, 2, 17, 4 };

        private static readonly sbyte[] IdSkillsCanNotAttack = new sbyte[5] { 10, 11, 14, 23, 7 };

        private static readonly MobController _Instance = new MobController();

        public static bool IsPickingItems;

        private static bool IsWait;

        private static long TimeStartWait;

        private static long TimeWait;

        public static List<ItemMap> ItemPicks = new List<ItemMap>();

        private static int IndexItemPick = 0;

        private static long lastHarvestBeanTime = 0L;

        public static void Update()
        {
            if (IsWaiting())
            {
                return;
            }
            Char @char = Char.myCharz();
            if (@char.statusMe == 14 || !(@char.cHP > 0))
            {
                return;
            }
            if (Mobs.HpBuff > 0 && GameScr.hpPotion >= 1 && (@char.cHP <= @char.cHPFull * Mobs.HpBuff / 100 || @char.cMP <= @char.cMPFull * Mobs.MpBuff / 100))
            {
                GameScr.gI().doUseHP();
            }
            if (Mobs.autoThuHoachDau && GameScr.gI().magicTree != null)
            {
                MagicTree tree = GameScr.gI().magicTree;
                if (tree.currPeas >= tree.maxPeas && tree.maxPeas > 0)
                {
                    if (mSystem.currentTimeMillis() - lastHarvestBeanTime > 10000L)
                    {
                        lastHarvestBeanTime = mSystem.currentTimeMillis();
                        Service.gI().magicTree(1);
                    }
                }
            }
            if (Mobs.IsAutoPickItems)
            {
                if (IsPickingItems)
                {
                    if (IndexItemPick >= ItemPicks.Count)
                    {
                        IsPickingItems = false;
                        ItemPicks.Clear();
                        IndexItemPick = 0;
                        Wait(100);
                        return;
                    }
                    ItemMap itemMap = ItemPicks[IndexItemPick];
                    if (itemMap != null && GameScr.vItemMap.contains(itemMap))
                    {
                        Service.gI().pickItem(itemMap.itemMapID);
                        itemMap.countAutoPick++;
                    }
                    Wait(500);
                    IndexItemPick++;
                    return;
                }
                else
                {
                    ItemPicks.Clear();
                    IndexItemPick = 0;
                    for (int i = 0; i < GameScr.vItemMap.size(); i++)
                    {
                        ItemMap itemMap2 = (ItemMap)GameScr.vItemMap.elementAt(i);
                        if (GetTypePickItem(itemMap2) != 0)
                        {
                            ItemPicks.Add(itemMap2);
                        }
                    }
                    if (ItemPicks.Count > 0)
                    {
                        IsPickingItems = true;
                        return;
                    }
                }
            }
            bool isTDLT = ItemTime.isExistItem(4387);
            bool canTele = Mobs.telePem || isTDLT;
            if (Mobs.IsTanSat)
            {
                if (@char.isCharge)
                {
                    Wait(100);
                    return;
                }
                if (@char.mobFocus != null && !IsMobTanSat(@char.mobFocus))
                {
                    @char.mobFocus = null;
                }
                if (@char.mobFocus == null)
                {
                    @char.mobFocus = GetMobTanSat();
                    if (canTele && @char.mobFocus != null)
                    {
                        int tX = (@char.mobFocus.x != 0) ? @char.mobFocus.x : @char.mobFocus.xFirst;
                        int tY = (@char.mobFocus.y != 0) ? @char.mobFocus.y : @char.mobFocus.yFirst;
                        int approachOffset = (@char.cx >= tX) ? 35 : -35;
                        int standX = tX + approachOffset;
                        if (Math.abs(tX - @char.cx) >= 20 || Math.abs(tY - @char.cy) >= 20)
                        {
                            MoveTo(standX, tY);
                        }
                        return;
                    }
                }
                if (@char.mobFocus != null)
                {
                    Mob mobFocus = @char.mobFocus;
                    int targetX = (mobFocus.x != 0) ? mobFocus.x : mobFocus.xFirst;
                    int targetY = (mobFocus.y != 0) ? mobFocus.y : mobFocus.yFirst;

                    // Tiếp cận cách tâm quái 35px theo hướng tiếp cận (không đứng đè tâm quái gây lật hướng cdir)
                    int approachOffset = (@char.cx >= targetX) ? 35 : -35;
                    int standX = targetX + approachOffset;

                    Skill skillAttack = GetSkillAttack2();
                    if (skillAttack == null)
                    {
                        skillAttack = @char.myskill;
                    }
                    if (skillAttack == null && @char.vSkillFight != null && @char.vSkillFight.size() > 0)
                    {
                        skillAttack = (Skill)@char.vSkillFight.elementAt(0);
                    }

                    int skillDx = (skillAttack != null) ? skillAttack.dx : 40;
                    int skillDy = (skillAttack != null) ? skillAttack.dy : 30;
                    int maxDistX = Math.max(65, (int)(skillDx * 1.2));
                    int maxDistY = Math.max(55, (int)(skillDy * 1.2));

                    bool inRange = Math.abs(targetX - @char.cx) <= maxDistX && Math.abs(targetY - @char.cy) <= maxDistY;

                    if (inRange)
                    {
                        // Đã vào tầm đánh quái: Chỉ dừng di chuyển khi đã gần điểm dừng (standX) hoặc đã đứng vững trên đất
                        bool isGrounded = (TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2;
                        if (isGrounded || Math.abs(standX - @char.cx) <= 15 || Math.abs(targetX - @char.cx) <= 45)
                        {
                            if (@char.currentMovePoint != null || @char.statusMe == 2)
                            {
                                @char.currentMovePoint = null;
                                @char.cvx = 0;
                                @char.cvy = 0;
                                if (isGrounded)
                                {
                                    @char.statusMe = 1;
                                }
                            }
                        }

                        if (@char.skillInfoPaint() == null && skillAttack != null && CanUseSkill(skillAttack))
                        {
                            if (Char.myCharz().myskill != skillAttack)
                            {
                                GameScr.gI().doSelectSkill(skillAttack, isShortcut: true);
                            }

                            // Dịch chuyển đến quái bay khi có TDLT hoặc telePem
                            if (canTele && GameCanvas.gameTick % 50 == 0 && mobFocus.templateId < Mob.arrMobTemplate.Length && Mob.arrMobTemplate[mobFocus.templateId] != null && Mob.arrMobTemplate[mobFocus.templateId].type == 4)
                            {
                                MoveTo(standX, targetY);
                            }
                            if (skillAttack.template.isAttackSkill())
                            {
                                AttackMob(mobFocus);
                                SetUsedSkill(@char.myskill);
                            }
                            else if (skillAttack.template.isUseAlone())
                            {
                                GameScr.gI().doDoubleClickToObj(mobFocus);
                            }
                        }
                    }
                    else
                    {
                        // Chưa tới tầm đánh: Dịch chuyển nhanh thẳng đến điểm tiếp cận cạnh quái (y hệt TDLT)
                        if (canTele)
                        {
                            if (Math.abs(targetX - @char.cx) >= 20 || Math.abs(targetY - @char.cy) >= 20)
                            {
                                MoveTo(standX, targetY);
                            }
                        }
                        else
                        {
                            Move(standX, targetY);
                        }
                    }
                }
                else
                {
                    // Khi trong map tất cả quái được chọn bị tiêu diệt: Đứng yên, lúc nào quái xuất hiện mới đánh tiếp
                    if (@char.currentMovePoint != null || @char.statusMe == 2)
                    {
                        @char.currentMovePoint = null;
                        @char.cvx = 0;
                        @char.cvy = 0;
                        if ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2)
                        {
                            @char.statusMe = 1;
                        }
                    }
                }
                Wait(100);
            }
            else
            {
                if (!Mobs.tsPlayer)
                {
                    return;
                }
                if (@char.isCharge)
                {
                    Wait(100);
                    return;
                }
                if (@char.charFocus != null && !IsCharTanSat(@char.charFocus))
                {
                    @char.charFocus = null;
                }
                if (@char.charFocus == null)
                {
                    @char.charFocus = GetCharTanSat();
                    if (@char.charFocus != null)
                    {
                        if (Mobs.telePem)
                        {
                            if (Math.abs(@char.charFocus.cx - @char.cx) >= 20 || Math.abs(@char.charFocus.cy - @char.cy) >= 20)
                            {
                                MoveTo(@char.charFocus.cx, @char.charFocus.cy);
                            }
                            return;
                        }
                        @char.cx = @char.charFocus.cx;
                        @char.cy = @char.charFocus.cy;
                        Service.gI().charMove();
                    }
                }
                if (@char.charFocus != null && @char.skillInfoPaint() == null)
                {
                    Skill skillAttack2 = GetSkillAttack2();
                    if (skillAttack2 != null && !skillAttack2.paintCanNotUseSkill)
                    {
                        Char charFocus = @char.charFocus;
                        if (@char.myskill != skillAttack2)
                        {
                            GameScr.gI().doSelectSkill(skillAttack2, isShortcut: true);
                        }
                        if (Res.distance(charFocus.cx, charFocus.cy, @char.cx, @char.cy) <= 48)
                        {
                            GameScr.gI().doDoubleClickToObj(charFocus);
                        }
                        else
                        {
                            if (Mobs.telePem)
                            {
                                if (Math.abs(charFocus.cx - @char.cx) >= 20 || Math.abs(charFocus.cy - @char.cy) >= 20)
                                {
                                    MoveTo(charFocus.cx, charFocus.cy);
                                }
                                return;
                            }
                            Move(charFocus.cx, charFocus.cy);
                        }
                    }
                }
                Wait(100);
            }
        }
        public static void SetUsedSkill(Skill skill)
        {
            skill.paintCanNotUseSkill = true;
            skill.lastTimeUseThisSkill = mSystem.currentTimeMillis();
        }
        private static readonly MyVector attackMobVector = new MyVector();
        private static readonly MyVector emptyVector = new MyVector();
        public static void AttackMob(Mob mob)
        {
            try
            {
                Char myChar = Char.myCharz();
                if (mob != null && myChar != null)
                {
                    int targetX = (mob.x != 0) ? mob.x : mob.xFirst;
                    if (targetX > myChar.cx)
                    {
                        myChar.cdir = 1;
                    }
                    else if (targetX < myChar.cx)
                    {
                        myChar.cdir = -1;
                    }
                }
                attackMobVector.removeAllElements();
                attackMobVector.addElement(mob);
                Service.gI().sendPlayerAttack(attackMobVector, emptyVector, 1);
            }
            catch
            {
            }
        }
        public static void MoveTo(int x, int y)
        {
            Char.myCharz().cx = x;
            Char.myCharz().cy = y;
            Service.gI().charMove();
        }
        public static void Move(int x, int y)
        {
            Char @char = Char.myCharz();
            int destX = x;
            int destY = y;
            if (Mobs.vuotDiaHinh && (@char.statusMe == 1 || @char.statusMe == 2))
            {
                int[] pointYsdMax = GetPointYsdMax(@char.cx, x);
                if (pointYsdMax[0] > 0 && pointYsdMax[1] > 0 && pointYsdMax[1] < y && pointYsdMax[1] < @char.cy)
                {
                    destX = pointYsdMax[0];
                    destY = pointYsdMax[1];
                }
            }
            // Dung sai 24px: Không khởi tạo lại MovePoint nếu mục tiêu xEnd/yEnd chỉ dịch chuyển rất nhỏ do animation quái
            if (@char.currentMovePoint != null && Math.abs(@char.currentMovePoint.xEnd - destX) <= 24 && Math.abs(@char.currentMovePoint.yEnd - destY) <= 24)
            {
                return;
            }
            @char.currentMovePoint = new MovePoint(destX, destY);
        }

        private static TpyePickItem GetTypePickItem(ItemMap itemMap)
        {
            Char @char = Char.myCharz();
            if (itemMap.playerId != @char.charID && itemMap.playerId != -1 && !Mobs.IsPickItemsAll)
            {
                return TpyePickItem.CanNotPickItem;
            }
            if (Mobs.IsLimitTimesPickItem && itemMap.countAutoPick > Mobs.TimesAutoPickItemMax)
            {
                return TpyePickItem.CanNotPickItem;
            }
            if (!FilterItemPick(itemMap))
            {
                return TpyePickItem.CanNotPickItem;
            }
            if (Mobs.IsPickItemsDis || (Res.abs(@char.cx - itemMap.xEnd) < 100 && Res.abs(@char.cy - itemMap.yEnd) < 100))
            {
                return TpyePickItem.PickItemNormal;
            }
            if (ItemTime.isExistItem(4387))
            {
                return TpyePickItem.PickItemTDLT;
            }
            if (Mobs.IsTanSat)
            {
                return TpyePickItem.PickItemTanSat;
            }
            return TpyePickItem.CanNotPickItem;
        }

        private static bool FilterItemPick(ItemMap itemMap)
        {
            if (itemMap == null || itemMap.template == null)
            {
                return false;
            }
            if (Mobs.pickFilterMode == 1)
            {
                bool isGold = itemMap.template.type == 9 || itemMap.template.id == 76 || (itemMap.template.id >= 188 && itemMap.template.id <= 190);
                bool isGemOrDragonBall = itemMap.template.type == 12 || (itemMap.template.id >= 14 && itemMap.template.id <= 20);
                bool isUpgradeStone = itemMap.template.id >= 220 && itemMap.template.id <= 224;
                bool isRareItem = itemMap.template.isUpToUp || itemMap.template.type == 5 || itemMap.template.id == 457 || itemMap.template.id == 590;
                if (!isGold && !isGemOrDragonBall && !isUpgradeStone && !isRareItem)
                {
                    return false;
                }
            }
            else if (Mobs.pickFilterMode == 2)
            {
                bool isTrashEquip = itemMap.template.type >= 0 && itemMap.template.type <= 4 && !itemMap.template.isUpToUp;
                if (isTrashEquip)
                {
                    return false;
                }
            }
            if ((Mobs.IdItemPicks.Count == 0 || Mobs.IdItemPicks.Contains(itemMap.template.id)) && (Mobs.IdItemBlocks.Count == 0 || !Mobs.IdItemBlocks.Contains(itemMap.template.id)) && (Mobs.TypeItemPicks.Count == 0 || Mobs.TypeItemPicks.Contains(itemMap.template.type)))
            {
                if (Mobs.TypeItemBlock.Count != 0)
                {
                    return !Mobs.TypeItemBlock.Contains(itemMap.template.type);
                }
                return true;
            }
            return false;
        }

        private static Mob GetMobTanSat()
        {
            Mob result = null;
            int num = int.MaxValue;
            Char @char = Char.myCharz();
            for (int i = 0; i < GameScr.vMob.size(); i++)
            {
                Mob mob = (Mob)GameScr.vMob.elementAt(i);
                if (mob == null || !IsMobTanSat(mob)) continue;
                int mx = (mob.x != 0) ? mob.x : mob.xFirst;
                int my = (mob.y != 0) ? mob.y : mob.yFirst;
                int num2 = (mx - @char.cx) * (mx - @char.cx) + (my - @char.cy) * (my - @char.cy);
                if (num2 < num)
                {
                    result = mob;
                    num = num2;
                }
            }
            return result;
        }

        private static Char GetCharTanSat()
        {
            Char result = null;
            for (int i = 0; i < GameScr.vCharInMap.size(); i++)
            {
                Char c = (Char)GameScr.vCharInMap.elementAt(i);
                if (IsCharTanSat(c))
                {
                    result = c;
                }
            }
            return result;
        }

        private static Mob GetMobNext()
        {
            Mob result = null;
            long num = mSystem.currentTimeMillis();
            for (int i = 0; i < GameScr.vMob.size(); i++)
            {
                Mob mob = (Mob)GameScr.vMob.elementAt(i);
                if (IsMobNext(mob) && mob.timeLastDie < num)
                {
                    result = mob;
                    num = mob.timeLastDie;
                }
            }
            return result;
        }

        private static bool IsMobTanSat(Mob mob)
        {
            if (mob.status == 0 || mob.status == 1 || mob.hp <= 0 || mob.isMobMe)
            {
                return false;
            }
            bool flag = Mobs.neSieuQuai && !ItemTime.isExistItem(4387);
            if (flag && (mob.levelBoss != 0 || mob.isBoss || mob.hp > mob.maxHp * 2))
            {
                return false;
            }
            return FilterMobTanSat(mob);
        }

        private static bool IsCharTanSat(Char c)
        {
            return Char.myCharz().isMeCanAttackOtherPlayer(c);
        }

        private static bool IsMobNext(Mob mob)
        {
            if (mob.isMobMe)
            {
                return false;
            }
            if (!FilterMobTanSat(mob))
            {
                return false;
            }
            if (Mobs.neSieuQuai && !ItemTime.isExistItem(4387) && mob.getTemplate().hp >= 3000)
            {
                if (mob.levelBoss != 0 || mob.isBoss)
                {
                    Mob mob2 = null;
                    bool flag4 = false;
                    for (int i = 0; i < GameScr.vMob.size(); i++)
                    {
                        mob2 = (Mob)GameScr.vMob.elementAt(i);
                        if (mob2.countDie == 10 && (mob2.status == 0 || mob2.status == 1))
                        {
                            flag4 = true;
                            break;
                        }
                    }
                    if (!flag4)
                    {
                        return false;
                    }
                    mob.timeLastDie = mob2.timeLastDie;
                }
                else if (mob.countDie == 10 && (mob.status == 0 || mob.status == 1))
                {
                    return false;
                }
            }
            return true;
        }

        private static bool FilterMobTanSat(Mob mob)
        {
            if ((Mobs.IdMobsTanSat.Count == 0 || Mobs.IdMobsTanSat.Contains(mob.mobId)) && (Mobs.TypeMobsTanSat.Count == 0 || Mobs.TypeMobsTanSat.Contains(mob.templateId)))
            {
                return !mob.isMobMe;
            }
            return false;
        }

        private static Skill GetSkillAttack2()
        {
            Skill skill = null;
            Skill[] skills = (GameCanvas.isTouch ? GameScr.onScreenSkill : GameScr.keySkill);
            foreach (Skill s in skills)
            {
                if (s != null && s.template.id != 10 && s.template.id != 11 && s.template.id != 14 && s.template.id != 23 && !s.template.isBuffToPlayer() && !s.template.isSkillSpec() && IsSkillBetter(s, skill))
                {
                    skill = s;
                }
            }
            return skill;
        }

        private static bool IsSkillBetter(Skill SkillBetter, Skill skill)
        {
            if (SkillBetter == null)
            {
                return false;
            }
            if (skill == null)
            {
                return true;
            }
            if (!CanUseSkill(SkillBetter))
            {
                return false;
            }
            if (!CanUseSkill(skill))
            {
                return true;
            }
            bool flag3 = (SkillBetter.template.id == 17 && skill.template.id == 2) || (SkillBetter.template.id == 9 && skill.template.id == 0);
            return skill.coolDown < SkillBetter.coolDown || flag3;
        }

        private static bool CanUseSkill(Skill skill)
        {
            if (mSystem.currentTimeMillis() - skill.lastTimeUseThisSkill > (long)skill.coolDown + 25L)
            {
                skill.paintCanNotUseSkill = false;
            }
            if (!skill.paintCanNotUseSkill)
            {
                return Char.myCharz().cMP >= GetManaUseSkill(skill);
            }
            return false;
        }

        private static double GetManaUseSkill(Skill skill)
        {
            if (skill.template.manaUseType == 2)
            {
                return 1;
            }
            if (skill.template.manaUseType == 1)
            {
                return skill.manaUse * Char.myCharz().cMPFull / 100;
            }
            return skill.manaUse;
        }

        public static int GetYsd(int xsd)
        {
            Char @char = Char.myCharz();
            int num = TileMap.pxh;
            int result = -1;
            for (int i = 24; i < TileMap.pxh; i += 24)
            {
                if (TileMap.tileTypeAt(xsd, i, 2))
                {
                    int num2 = Res.abs(i - @char.cy);
                    if (num2 < num)
                    {
                        num = num2;
                        result = i;
                    }
                }
            }
            return result;
        }

        private static int[] GetPointYsdMax(int xStart, int xEnd)
        {
            int num = TileMap.pxh;
            int num2 = -1;
            if (xStart > xEnd)
            {
                for (int i = xEnd; i < xStart; i += 24)
                {
                    int ysd = GetYsd(i);
                    if (ysd <= 0) continue;
                    if (ysd < num)
                    {
                        num = ysd;
                        num2 = i;
                    }
                }
            }
            else
            {
                for (int j = xEnd; j > xStart; j -= 24)
                {
                    int ysd2 = GetYsd(j);
                    if (ysd2 <= 0) continue;
                    if (ysd2 < num)
                    {
                        num = ysd2;
                        num2 = j;
                    }
                }
            }
            if (num2 == -1)
            {
                return new int[2] { xEnd, TileMap.pxh };
            }
            return new int[2] { num2, num };
        }

        public static void Wait(int time)
        {
            IsWait = true;
            TimeStartWait = mSystem.currentTimeMillis();
            TimeWait = time;
        }

        public static bool IsWaiting()
        {
            if (IsWait && mSystem.currentTimeMillis() - TimeStartWait >= TimeWait)
            {
                IsWait = false;
            }
            return IsWait;
        }
    }
}

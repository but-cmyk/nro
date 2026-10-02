package utils;

import models.player.Player;
import models.skill.Skill;

public class PlayerAuditLogger {

    public static void logAction(Player player, String action, String details) {
        Logger.audit(action, player, details);
    }

    public static void logPacket(Player player, byte cmd, String cmdDesc, String extra) {
        String extraStr = (extra != null && !extra.isEmpty()) ? (" -> " + extra) : "";
        String msg = String.format("Cmd:%d (%s)%s", cmd, cmdDesc, extraStr);
        Logger.enqueue(Logger.LogCategory.AUDIT, Logger.LogLevel.INFO, Logger.formatPlayerContext(player), "[PACKET] " + msg, null);
    }

    public static void logSkillSelect(Player player, int reqSkillTemplateId, Skill matchedSkill, String status) {
        String matchedInfo = (matchedSkill != null && matchedSkill.template != null)
                ? String.format("Found '%s' (TemplateID:%d, SkillID:%d, Lv:%d, Mana:%d, CD:%dms)",
                matchedSkill.template.name, matchedSkill.template.id, matchedSkill.skillId, matchedSkill.point, matchedSkill.manaUse, matchedSkill.coolDown)
                : "NOT_FOUND_IN_SKILL_LIST";

        String msg = String.format("[SKILL_SELECT] ReqTemplateID:%d => %s | Result: %s", reqSkillTemplateId, matchedInfo, status);
        Logger.enqueue(Logger.LogCategory.AUDIT, Logger.LogLevel.INFO, Logger.formatPlayerContext(player), msg, null);
    }

    public static void logSkillUsage(Player player, String skillName, int skillTemplateId, String target, boolean success, String reason) {
        String statusStr = success ? "SUCCESS" : "BLOCKED";
        String msg = String.format("[SKILL_EXEC] Skill:'%s' (ID:%d) -> Target:%s | Status:%s | Reason:%s",
                skillName, skillTemplateId, target, statusStr, reason);
        Logger.enqueue(Logger.LogCategory.AUDIT, Logger.LogLevel.INFO, Logger.formatPlayerContext(player), msg, null);
    }

    public static void logAttackMob(Player player, int mobId, boolean isMobMe, int masterId, String status, String reason) {
        String msg = String.format("[ATTACK_MOB] TargetMobID:%d (isMobMe:%b, masterId:%d) | Status:%s | Details:%s",
                mobId, isMobMe, masterId, status, reason);
        Logger.enqueue(Logger.LogCategory.AUDIT, Logger.LogLevel.INFO, Logger.formatPlayerContext(player), msg, null);
    }

    public static void logAttackPlayer(Player player, int targetPlayerId, String status, String reason) {
        String msg = String.format("[ATTACK_PLAYER] TargetPlayerID:%d | Status:%s | Details:%s",
                targetPlayerId, status, reason);
        Logger.enqueue(Logger.LogCategory.AUDIT, Logger.LogLevel.INFO, Logger.formatPlayerContext(player), msg, null);
    }
}

package utils;

import models.player.Player;
import models.skill.Skill;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerAuditLogger {

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\033[0;36m";
    private static final String PURPLE = "\033[0;35m";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final BlockingQueue<LogEntry> LOG_QUEUE = new LinkedBlockingQueue<>(20000);
    private static volatile boolean running = true;

    private static class LogEntry {
        final String type; // "ACTION" or "SKILL"
        final String formattedLine;

        LogEntry(String type, String formattedLine) {
            this.type = type;
            this.formattedLine = formattedLine;
        }
    }

    static {
        File dir = new File("logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Thread worker = new Thread(() -> {
            BufferedWriter actionWriter = null;
            BufferedWriter skillWriter = null;
            try {
                actionWriter = new BufferedWriter(new FileWriter("logs/player_actions.log", true));
                skillWriter = new BufferedWriter(new FileWriter("logs/player_skills.log", true));

                while (running || !LOG_QUEUE.isEmpty()) {
                    LogEntry entry = LOG_QUEUE.poll();
                    if (entry == null) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ignored) {
                        }
                        continue;
                    }

                    if ("SKILL".equals(entry.type)) {
                        skillWriter.write(entry.formattedLine);
                        skillWriter.newLine();
                        skillWriter.flush();
                    }

                    // All entries also recorded to player_actions.log
                    actionWriter.write(entry.formattedLine);
                    actionWriter.newLine();
                    actionWriter.flush();
                }
            } catch (IOException e) {
                System.err.println("[PlayerAuditLogger] Error writing logs: " + e.getMessage());
            } finally {
                try {
                    if (actionWriter != null) actionWriter.close();
                    if (skillWriter != null) skillWriter.close();
                } catch (IOException ignored) {
                }
            }
        }, "PlayerAuditLogger-Thread");
        worker.setDaemon(true);
        worker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
        }));
    }

    private static String getTimestamp() {
        return DATE_FORMAT.format(new Date());
    }

    private static String getPlayerInfo(Player player) {
        if (player == null) {
            return "[Player: NULL]";
        }
        String mapZone = "";
        try {
            if (player.zone != null && player.zone.map != null) {
                mapZone = String.format(" | Map:%d-Z:%d", player.zone.map.mapId, player.zone.zoneId);
            }
        } catch (Exception ignored) {
        }
        return String.format("[Player: %s (ID:%d%s)]", player.name != null ? player.name : "noname", player.id, mapZone);
    }

    /**
     * Log general player actions (opcodes, movement, dialogs, item clicks...)
     */
    public static void logAction(Player player, String action, String details) {
        String time = getTimestamp();
        String plInfo = getPlayerInfo(player);
        String logLine = String.format("[%s] [ACTION] %s %s: %s", time, plInfo, action, details);

        System.out.println(CYAN + logLine + RESET);
        LOG_QUEUE.offer(new LogEntry("ACTION", logLine));
    }

    /**
     * Log raw incoming network packet received from player session
     */
    public static void logPacket(Player player, byte cmd, String cmdDesc, String extra) {
        String time = getTimestamp();
        String plInfo = getPlayerInfo(player);
        String extraStr = (extra != null && !extra.isEmpty()) ? (" -> " + extra) : "";
        String logLine = String.format("[%s] [PACKET] %s Cmd:%d (%s)%s", time, plInfo, cmd, cmdDesc, extraStr);

        System.out.println(PURPLE + logLine + RESET);
        LOG_QUEUE.offer(new LogEntry("ACTION", logLine));
    }

    /**
     * Log skill selection (opcode 34)
     */
    public static void logSkillSelect(Player player, int reqSkillTemplateId, Skill matchedSkill, String status) {
        String time = getTimestamp();
        String plInfo = getPlayerInfo(player);
        String matchedInfo = (matchedSkill != null && matchedSkill.template != null)
                ? String.format("Found '%s' (TemplateID:%d, SkillID:%d, Lv:%d, Mana:%d, CD:%dms)",
                matchedSkill.template.name, matchedSkill.template.id, matchedSkill.skillId, matchedSkill.point, matchedSkill.manaUse, matchedSkill.coolDown)
                : "NOT_FOUND_IN_SKILL_LIST";

        String logLine = String.format("[%s] [SKILL_SELECT] %s ReqTemplateID:%d => %s | Result: %s",
                time, plInfo, reqSkillTemplateId, matchedInfo, status);

        if (matchedSkill != null) {
            System.out.println(YELLOW + logLine + RESET);
        } else {
            System.out.println(RED + logLine + RESET);
        }
        LOG_QUEUE.offer(new LogEntry("SKILL", logLine));
    }

    /**
     * Log skill usage outcome (useSkill, attackMob, attackPlayer)
     */
    public static void logSkillUsage(Player player, String skillName, int skillTemplateId, String target, boolean success, String reason) {
        String time = getTimestamp();
        String plInfo = getPlayerInfo(player);
        String statusStr = success ? "SUCCESS" : "BLOCKED";
        String logLine = String.format("[%s] [SKILL_EXEC] %s Skill:'%s' (ID:%d) -> Target:%s | Status:%s | Reason:%s",
                time, plInfo, skillName, skillTemplateId, target, statusStr, reason);

        if (success) {
            System.out.println(GREEN + logLine + RESET);
        } else {
            System.out.println(RED + logLine + RESET);
        }
        LOG_QUEUE.offer(new LogEntry("SKILL", logLine));
    }

    /**
     * Log mob attack action
     */
    public static void logAttackMob(Player player, int mobId, boolean isMobMe, int masterId, String status, String reason) {
        String time = getTimestamp();
        String plInfo = getPlayerInfo(player);
        String logLine = String.format("[%s] [ATTACK_MOB] %s TargetMobID:%d (isMobMe:%b, masterId:%d) | Status:%s | Details:%s",
                time, plInfo, mobId, isMobMe, masterId, status, reason);

        if ("SUCCESS".equals(status)) {
            System.out.println(GREEN + logLine + RESET);
        } else {
            System.out.println(RED + logLine + RESET);
        }
        LOG_QUEUE.offer(new LogEntry("SKILL", logLine));
    }

    /**
     * Log player attack action
     */
    public static void logAttackPlayer(Player player, int targetPlayerId, String status, String reason) {
        String time = getTimestamp();
        String plInfo = getPlayerInfo(player);
        String logLine = String.format("[%s] [ATTACK_PLAYER] %s TargetPlayerID:%d | Status:%s | Details:%s",
                time, plInfo, targetPlayerId, status, reason);

        if ("SUCCESS".equals(status)) {
            System.out.println(GREEN + logLine + RESET);
        } else {
            System.out.println(RED + logLine + RESET);
        }
        LOG_QUEUE.offer(new LogEntry("SKILL", logLine));
    }
}

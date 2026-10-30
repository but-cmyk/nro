package server.command;

import server.ServerManager;
import server.Maintenance;
import server.ServerNotify;
import server.Client;
import server.Manager;
import services.player.ClanService;
import services.Service;
import services.admin.AdminService;
import utils.Logger;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private static final Map<String, ConsoleCommand> commands = new HashMap<>();

    static {
        // Server Maintenance
        commands.put("baotri", args -> {
            int minutes = args.length > 0 ? Integer.parseInt(args[0]) : 5;
            AdminService.gI().startMaintenance("Console", minutes);
        });

        commands.put("athread", args -> ServerNotify.gI().notify("ACTIVE THREADS: " + Thread.activeCount()));

        commands.put("nplayer", args -> {
            int playerCount = Client.gI().getPlayers().size();
            Logger.log(Logger.GREEN, "Players in game: " + playerCount + "\n");
        });

        commands.put("memory", args -> {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            Logger.log(Logger.PURPLE, "Memory Usage: " + (usedMemory / 1024 / 1024) + "MB / " + (maxMemory / 1024 / 1024) + "MB\n");
            Logger.log(Logger.PURPLE, "CLIENTS map size: " + ServerManager.CLIENTS.size() + "\n");
        });

        commands.put("latency", args -> server.network.PacketProfiler.gI().printReport());
        commands.put("prof", args -> server.network.PacketProfiler.gI().printReport());

        commands.put("gc", args -> {
            Runtime rt = Runtime.getRuntime();
            long freeMB = rt.freeMemory() / (1024 * 1024);
            long totalMB = rt.totalMemory() / (1024 * 1024);
            long maxMB = rt.maxMemory() / (1024 * 1024);
            Logger.warning("Manual JVM Garbage Collection is bypassed in production to prevent Stop-The-World latency spikes. Memory: "
                    + (totalMB - freeMB) + "/" + maxMB + " MB (Free: " + freeMB + " MB)\n");
        });

        // Telemetry AI Trajectory Logger command
        commands.put("telemetry", args -> {
            if (args.length == 0) {
                Logger.log(Logger.PURPLE, "[Telemetry Stats] " + telemetry.TrajectoryLogger.gI().getStatusJson() + "\n");
                return;
            }
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "on" -> {
                    telemetry.TelemetryConfig.toggleEnabled(true);
                    Logger.log(Logger.GREEN, "[Telemetry] Enabled globally.\n");
                }
                case "off" -> {
                    telemetry.TelemetryConfig.toggleEnabled(false);
                    Logger.log(Logger.YELLOW, "[Telemetry] Disabled globally.\n");
                }
                case "map" -> {
                    if (args.length > 2 && "add".equalsIgnoreCase(args[1])) {
                        int mapId = Integer.parseInt(args[2]);
                        telemetry.TelemetryConfig.addWhitelistMap(mapId);
                        Logger.log(Logger.GREEN, "[Telemetry] Added map whitelist: " + mapId + "\n");
                    } else if (args.length > 2 && "remove".equalsIgnoreCase(args[1])) {
                        int mapId = Integer.parseInt(args[2]);
                        telemetry.TelemetryConfig.removeWhitelistMap(mapId);
                        Logger.log(Logger.YELLOW, "[Telemetry] Removed map whitelist: " + mapId + "\n");
                    } else if (args.length > 1 && "clear".equalsIgnoreCase(args[1])) {
                        telemetry.TelemetryConfig.clearWhitelistMaps();
                        Logger.log(Logger.PURPLE, "[Telemetry] Cleared map whitelist (all maps enabled).\n");
                    } else {
                        Logger.warning("Usage: telemetry map [add <mapId>|remove <mapId>|clear]\n");
                    }
                }
                case "player" -> {
                    if (args.length > 2 && "add".equalsIgnoreCase(args[1])) {
                        long pId = Long.parseLong(args[2]);
                        telemetry.TelemetryConfig.addWhitelistPlayer(pId);
                        Logger.log(Logger.GREEN, "[Telemetry] Added player whitelist: " + pId + "\n");
                    } else if (args.length > 2 && "remove".equalsIgnoreCase(args[1])) {
                        long pId = Long.parseLong(args[2]);
                        telemetry.TelemetryConfig.removeWhitelistPlayer(pId);
                        Logger.log(Logger.YELLOW, "[Telemetry] Removed player whitelist: " + pId + "\n");
                    } else if (args.length > 1 && "clear".equalsIgnoreCase(args[1])) {
                        telemetry.TelemetryConfig.clearWhitelistPlayers();
                        Logger.log(Logger.PURPLE, "[Telemetry] Cleared player whitelist (all real players enabled).\n");
                    } else {
                        Logger.warning("Usage: telemetry player [add <pId>|remove <pId>|clear]\n");
                    }
                }
                case "reset" -> {
                    telemetry.TrajectoryLogger.gI().resetMetrics();
                    Logger.log(Logger.GREEN, "[Telemetry] Reset metrics counters.\n");
                }
                default -> Logger.warning("Usage: telemetry [on|off|map|player|reset]\n");
            }
        });

        // Bot Management & Namec AI Fleet command
        commands.put("bot", args -> {
            if (args.length == 0) {
                int botCount = bot.BotManager.gI().bot.size();
                Logger.log(Logger.PURPLE, "[Bot Fleet] Active bots: " + botCount + "\n");
                Logger.warning("Usage: bot [namec <count> | clear]\n");
                return;
            }
            String sub = args[0].toLowerCase();
            if ("namec".equals(sub)) {
                int count = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
                bot.NewBot.gI().spawnNamecArmy(count);
                Logger.log(Logger.GREEN, "[Bot] Successfully spawned " + count + " Namec AI bots!\n");
            } else if ("clear".equals(sub)) {
                int size = bot.BotManager.gI().bot.size();
                for (bot.Bot b : new java.util.ArrayList<>(bot.BotManager.gI().bot)) {
                    services.map.ChangeMapService.gI().exitMap(b);
                }
                bot.BotManager.gI().bot.clear();
                Logger.log(Logger.YELLOW, "[Bot] Cleared " + size + " active bots.\n");
            } else {
                Logger.warning("Usage: bot [namec <count> | clear]\n");
            }
        });

        commands.put("admin", args -> {
            ServerManager.gI().getExecutorService().submit(() -> Client.gI().close());
        });

        // Player Actions
        commands.put("kick", args -> {
            if (args.length > 0) {
                String target = args[0];
                String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Kicked by Console";
                boolean ok = AdminService.gI().kickPlayer("Console", target, reason);
                Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da kick: " + target + " (Ly do: " + reason + ")" : "Khong tim thay online player: " + target);
            } else {
                // Legacy: kick clones and save
                ServerManager.gI().getExecutorService().submit(() -> {
                    try {
                        Client.gI().cloneMySessionNotConnect();
                        ServerManager.gI().saveAllPlayersData();
                        Logger.log(Logger.GREEN, "Kick session clone and saved player data.\n");
                    } catch (Exception e) {
                        Logger.error("Error during kick operation: " + e.getMessage() + "\n");
                    }
                });
            }
        });

        commands.put("ban", args -> {
            if (args.length > 0) {
                String target = args[0];
                int hours = 0;
                String reason = "Banned by Console";
                if (args.length > 1) {
                    try {
                        hours = Integer.parseInt(args[1]);
                        if (args.length > 2) {
                            reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                        }
                    } catch (NumberFormatException e) {
                        hours = 0;
                        reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    }
                }
                boolean ok = AdminService.gI().banPlayer("Console", target, hours, reason);
                Logger.log(ok ? Logger.GREEN : Logger.RED, ok
                        ? "Da ban tai khoan: " + target + (hours > 0 ? " (" + hours + " gio)" : " (vinh vien)") + " - Ly do: " + reason
                        : "Loi khi ban: " + target);
            } else {
                Logger.warning("Cu phap: ban <ten_nhan_vat> [so_gio] [ly_do]\n  VD: ban player1 24 Hack do\n      ban player1 Vi pham quy dinh\n");
            }
        });

        commands.put("unban", args -> {
            if (args.length > 0) {
                String target = args[0];
                boolean ok = AdminService.gI().unbanAccount("Console", target);
                Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da unban thanh cong: " + target : "Khong tim thay account: " + target);
            } else {
                Logger.warning("Cu phap: unban <username_hoac_ten_nv>\n");
            }
        });

        commands.put("mute", args -> {
            if (args.length > 0) {
                String target = args[0];
                int mins = 15;
                String reason = "Console Mute";
                if (args.length > 1) {
                    try {
                        mins = Integer.parseInt(args[1]);
                        if (args.length > 2) {
                            reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                        }
                    } catch (NumberFormatException e) {
                        reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    }
                }
                boolean ok = AdminService.gI().mutePlayer("Console", target, mins, reason);
                Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da mute " + target + " trong " + mins + "p (Ly do: " + reason + ")" : "Khong tim thay player online");
            } else {
                Logger.warning("Cu phap: mute <ten_nhan_vat> [so_phut] [ly_do]\n");
            }
        });

        commands.put("unmute", args -> {
            if (args.length > 0) {
                String target = args[0];
                boolean ok = AdminService.gI().unmutePlayer("Console", target);
                Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da unmute " + target : "Khong tim thay player online");
            } else {
                Logger.warning("Cu phap: unmute <ten_nhan_vat>\n");
            }
        });

        commands.put("freeze", args -> {
            if (args.length > 0) {
                String target = args[0];
                boolean ok = AdminService.gI().freezePlayer("Console", target);
                Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da freeze " + target : "Khong tim thay player online");
            } else {
                Logger.warning("Cu phap: freeze <ten_nhan_vat>\n");
            }
        });

        commands.put("unfreeze", args -> {
            if (args.length > 0) {
                String target = args[0];
                boolean ok = AdminService.gI().unfreezePlayer("Console", target);
                Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da unfreeze " + target : "Khong tim thay player online");
            } else {
                Logger.warning("Cu phap: unfreeze <ten_nhan_vat>\n");
            }
        });

        commands.put("teleport", args -> {
            if (args.length > 0) {
                String target = args[0];
                if (args.length == 1) {
                    boolean ok = AdminService.gI().teleportToVillage("Console", target);
                    Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da dua " + target + " ve lang" : "Khong tim thay player online");
                } else {
                    try {
                        int mapId = Integer.parseInt(args[1]);
                        int zoneId = args.length > 2 ? Integer.parseInt(args[2]) : 0;
                        int x = args.length > 3 ? Integer.parseInt(args[3]) : 200;
                        int y = args.length > 4 ? Integer.parseInt(args[4]) : 300;
                        boolean ok = AdminService.gI().teleportPlayer("Console", target, mapId, zoneId, x, y);
                        Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da chuyen " + target + " toi Map " + mapId + " (Zone " + zoneId + ")" : "Khong tim thay player online");
                    } catch (Exception e) {
                        Logger.error("Cu phap: teleport <ten_nv> [mapId] [zoneId] [x] [y]\n");
                    }
                }
            } else {
                Logger.warning("Cu phap: teleport <ten_nhan_vat> [mapId] [zoneId] [x] [y]\n");
            }
        });

        commands.put("buff", args -> {
            if (args.length >= 2) {
                try {
                    String target = args[0];
                    short itemId = Short.parseShort(args[1]);
                    int qty = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                    String opts = args.length > 3 ? args[3] : "";
                    boolean ok = AdminService.gI().buffItem("Console", target, itemId, qty, opts);
                    Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da buff item [" + itemId + " x" + qty + "] cho " + target : "Loi khi buff item");
                } catch (Exception e) {
                    Logger.error("Loi tham so: buff <ten_nv> <item_id> [so_luong] [options_str]\n");
                }
            } else {
                Logger.warning("Cu phap: buff <ten_nv> <item_id> [so_luong] [options_str (VD: 50-20v77-10)]\n");
            }
        });

        commands.put("buffmoney", args -> {
            if (args.length >= 2) {
                try {
                    String target = args[0];
                    long gold = parseAmount(args[1]);
                    int gem = args.length > 2 ? (int) parseAmount(args[2]) : 0;
                    int ruby = args.length > 3 ? (int) parseAmount(args[3]) : 0;
                    boolean ok = AdminService.gI().buffMoney("Console", target, gold, gem, ruby);
                    Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Da buff tien cho " + target : "Loi khi buff tien");
                } catch (Exception e) {
                    Logger.error("Loi tham so: buffmoney <ten_nv> <gold> [gem] [ruby]\n");
                }
            } else {
                Logger.warning("Cu phap: buffmoney <ten_nv> <gold> [gem] [ruby] (Ho tro k, m, b vi du: 100m 50k)\n");
            }
        });

        commands.put("status", args -> printStatus());
        commands.put("stats", args -> printStatus());

        commands.put("boss", args -> {
            var bosses = managers.boss.BossManager.gI().getBosses();
            Logger.log(Logger.GREEN, "================== DANH SACH BOSS HOAT DONG (" + bosses.size() + ") ==================");
            for (models.boss.Boss b : bosses) {
                if (b != null && b.zone != null && b.zone.map != null) {
                    Logger.log(Logger.CYAN, String.format(" [ID: %d] %s | Map: %s (Khu %d) | HP: %s/%s",
                            b.id, b.name, b.zone.map.mapName, b.zone.zoneId,
                            utils.Util.formatNumber(b.nPoint != null ? b.nPoint.hp : 0),
                            utils.Util.formatNumber(b.nPoint != null ? b.nPoint.hpMax : 0)));
                }
            }
            Logger.log(Logger.GREEN, "==================================================================");
        });

        commands.put("bang", args -> {
            ServerManager.gI().getExecutorService().submit(() -> {
                try {
                    ClanService.gI().close();
                    Logger.success("Saved " + Manager.CLANS.size() + " clans\n");
                } catch (Exception e) {
                    Logger.error("Error saving clan data: " + e.getMessage() + "\n");
                }
            });
        });

        commands.put("reload", args -> {
            String category = args.length > 0 ? args[0] : "ALL";
            boolean ok = AdminService.gI().reloadGameData("Console", category);
            Logger.log(ok ? Logger.GREEN : Logger.RED, ok ? "Reload thanh cong!" : "Reload that bai hoac server dang ban");
        });

        commands.put("save", args -> {
            int count = AdminService.gI().saveAllPlayers("Console");
            Logger.log(Logger.GREEN, "Da luu " + count + " player vao database.");
        });

        commands.put("a", args -> {
            if (args.length > 0) {
                String message = String.join(" ", args);
                AdminService.gI().broadcast("Console", message, true);
            }
        });

        commands.put("help", args -> {
            Logger.log(Logger.YELLOW, """
                    ================== DANH SACH LENH CONSOLE ==================
                    baotri [phut]              : Bat dau dem nguoc bao tri server
                    status / stats             : Thong so tong quan may chu (CCU, RAM, DB)
                    nplayer                    : Hien thi so nguoi choi online
                    memory / athread           : Thong so RAM va so luong Thread
                    prof / latency             : Bao cao do tre goi tin Netty
                    boss                       : Danh sach Boss dang hoat dong
                    kick <ten_nv> [ly_do]      : Kick nguoi choi khoi game
                    ban <ten_nv> [gio] [ly_do] : Ban tai khoan nguoi choi (luu DB)
                    unban <user/ten_nv>        : Mo khoa tai khoan
                    mute <ten_nv> [phut] [lydo]: Cam chat nguoi choi
                    unmute <ten_nv>            : Mo khoa chat
                    freeze <ten_nv>            : Dong bang nguoi choi
                    unfreeze <ten_nv>          : Go dong bang
                    teleport <ten> [map] [zone]: Dich chuyen nguoi choi (ve lang hoac map)
                    buff <ten> <id> [sl] [opt] : Buff vat pham cho nguoi choi
                    buffmoney <ten> <vang>...  : Buff vang/ngoc (ho tro 100m, 50k, 2b)
                    reload [category]          : Hot-reload du lieu server
                    save                       : Luu dong bo tat ca player
                    a <thong_diep>             : Thong bao do toan server
                    help                       : Hien thi tro giup nay
                    ============================================================
                    """);
        });
    }

    private static long parseAmount(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        str = str.trim().toLowerCase();
        long multiplier = 1;
        if (str.endsWith("k")) {
            multiplier = 1_000L;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("m")) {
            multiplier = 1_000_000L;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("b")) {
            multiplier = 1_000_000_000L;
            str = str.substring(0, str.length() - 1);
        }
        return Long.parseLong(str) * multiplier;
    }

    private static void printStatus() {
        Runtime rt = Runtime.getRuntime();
        long usedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMB = rt.maxMemory() / (1024 * 1024);
        int ccu = Client.gI().getPlayers().size();
        int activeThreads = Thread.activeCount();

        int dbActive = 0, dbIdle = 0, dbWaiting = 0;
        try {
            var ds = database.AlyraManager.getDataSource();
            if (ds != null && ds.getHikariPoolMXBean() != null) {
                var pool = ds.getHikariPoolMXBean();
                dbActive = pool.getActiveConnections();
                dbIdle = pool.getIdleConnections();
                dbWaiting = pool.getThreadsAwaitingConnection();
            }
        } catch (Exception ignored) {}

        Logger.log(Logger.GREEN, """
                ======================== TRANG THAI HE THONG ========================
                 - Online Players (CCU) : %d
                 - RAM JVM Used / Max   : %d MB / %d MB (%d%%)
                 - Active JVM Threads   : %d
                 - HikariCP Pool        : %d Active | %d Idle | %d Waiting
                 - Web Live Dashboard   : http://localhost:14446/dashboard
                ====================================================================
                """.formatted(ccu, usedMB, maxMB, (maxMB > 0 ? (usedMB * 100 / maxMB) : 0),
                activeThreads, dbActive, dbIdle, dbWaiting));
    }

    /**
     * Dispatches the raw command line to the registered commands.
     * @param line the raw command line entered by the user
     */
    public static void dispatch(String line) {
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        String[] parts = line.trim().split("\\s+");
        String cmdName = parts[0].toLowerCase();

        // Extract arguments
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        ConsoleCommand command = commands.get(cmdName);
        if (command != null) {
            try {
                command.execute(args);
            } catch (Exception e) {
                Logger.error("Error executing command '" + cmdName + "': " + e.getMessage() + "\n");
            }
        } else {
            // Check legacy prefix matches
            if (line.startsWith("kick ")) {
                commands.get("kick").execute(args);
            } else if (line.startsWith("bang")) {
                commands.get("bang").execute(args);
            } else if (line.startsWith("a ")) {
                String message = line.substring(2);
                commands.get("a").execute(new String[]{message});
            } else {
                Logger.warning("Unknown command: " + cmdName + " (Type 'help' for list of commands)\n");
            }
        }
    }
}

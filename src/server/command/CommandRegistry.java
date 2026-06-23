package server.command;

import server.ServerManager;
import server.Maintenance;
import server.ServerNotify;
import server.Client;
import server.Manager;
import services.player.ClanService;
import services.Service;
import utils.Logger;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private static final Map<String, ConsoleCommand> commands = new HashMap<>();

    static {
        // Register all commands
        commands.put("baotri", args -> Maintenance.gI().start(5));
        
        commands.put("athread", args -> ServerNotify.gI().notify("ACTIVE THREADS: " + Thread.activeCount()));
        
        commands.put("nplayer", args -> {
            int playerCount = Client.gI().getPlayers().size();
            Logger.error("Players in game: " + playerCount + "\n");
        });
        
        commands.put("memory", args -> {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            Logger.error("Memory Usage: " + (usedMemory / 1024 / 1024) + "MB / " + (maxMemory / 1024 / 1024) + "MB\n");
            Logger.error("CLIENTS map size: " + ServerManager.CLIENTS.size() + "\n");
        });
        
        commands.put("gc", args -> {
            System.gc();
            Logger.success("Garbage collection triggered manually\n");
        });
        
        commands.put("admin", args -> {
            ServerManager.gI().getExecutorService().submit(() -> Client.gI().close());
        });
        
        commands.put("kick", args -> {
            ServerManager.gI().getExecutorService().submit(() -> {
                try {
                    Client.gI().cloneMySessionNotConnect();
                    ServerManager.gI().saveAllPlayersData();
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.error("Error during kick operation: " + e.getMessage() + "\n");
                }
            });
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

        commands.put("a", args -> {
            if (args.length > 0) {
                String message = String.join(" ", args);
                Service.gI().sendThongBaoAllPlayer(message);
            }
        });
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
            // Check legacy prefix matches like startsWith "kick" or startsWith "bang" or startsWith "a "
            if (line.startsWith("kick")) {
                commands.get("kick").execute(args);
            } else if (line.startsWith("bang")) {
                commands.get("bang").execute(args);
            } else if (line.startsWith("a ")) {
                String message = line.substring(2);
                commands.get("a").execute(new String[]{message});
            } else {
                Logger.warning("Unknown command: " + cmdName + "\n");
            }
        }
    }
}

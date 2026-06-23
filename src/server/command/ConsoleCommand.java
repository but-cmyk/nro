package server.command;

public interface ConsoleCommand {
    /**
     * Executes the command with the given arguments.
     * @param args the command arguments (excluding the command name itself)
     */
    void execute(String[] args);
}

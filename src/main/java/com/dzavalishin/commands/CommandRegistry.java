package com.dzavalishin.commands;

import com.dzavalishin.game.Global;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of supported console commands
 */
public class CommandRegistry {
    public static final CommandRegistry INSTANCE = new CommandRegistry();

    private final Map<String, Command> commands = new ConcurrentHashMap<>();

    {
        put(new HelpCommand());
        put(new PrintCurrentWorkDirectoryCommand());
        put(new AddAliasCommand());
        put(new StopAllCommand());
        put(new ResetEnginesCommand());
        put(new ListCommandsCommand());
        put(new ScrollToCommand());
        put(new ListVariablesCommand());
        put(new ListAliasesCommand());
        put(new EchoCommand());
        put(new EchoColoredCommand());
        put(new ExitCommand());
        put(new ClearCommand());
        put(new PatchCommand());

        put(new NetworkClientsCommand());
        put(new BanCommand());
        put(new NetworkStatusCommand());
        put(new NetworkRemoteConsoleCommand());
        put(new NetworkConnectCommand());

        if (Global.debugEnabled)
            put(new ResetTileCommand());
    }

    private CommandRegistry() {}

    /**
     * returns command implementation by command name
     *
     * @param commandName command name
     * @return command implementation
     */
    public Optional<Command> getCommand(String commandName) {
        return Optional.ofNullable(commands.get(commandName));
    }

    /**
     * put command implementation into registry
     *
     * @param commandName command name
     * @param implementation command implementation
     */
    public void putCommand(String commandName, Command implementation) {
        commands.putIfAbsent(commandName, implementation);
    }

    /**
     * returns list of all supported commands
     *
     * @return list of all supported commands
     */
    public List<Command> commands() {
        return Collections.unmodifiableList(new LinkedList<>(commands.values()));
    }

    private void put(Command command) {
        commands.put(command.getKeyWord(), command);
    }
}

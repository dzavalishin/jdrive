package com.dzavalishin.commands;

/**
 * Default alias implementation
 */
public class DefaultAlias implements Alias {
    private final String name;
    private final String command;

    /**
     * ctor
     *
     * @param name alias name
     * @param command alias command
     */
    public DefaultAlias(String name, String command) {
        this.name = name;
        this.command = command;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getName() {
        return name;
    }
}

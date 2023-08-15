package com.dzavalishin.commands;

import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

import java.util.Iterator;
import java.util.List;

/**
 * Add new alias command implementation
 */
public class AddAliasCommand implements Command {
    private final List<Parameter> commandParameters;

    public AddAliasCommand() {
        this.commandParameters = List.of(
                new DefaultParameter(
                        new DefaultParameterDescription("alias"),
                        Type.STRING
                ),
                new DefaultParameter(
                        new DefaultParameterDescription("command"),
                        Type.STRING
                )
        );
    }

    @Override
    public List<Parameter> getParameters() {
        return commandParameters;
    }

    @Override
    public String getKeyWord() {
        return "alias";
    }

    @Override
    public String getDescription() {
        return "add a new alias, or redefine the behavior of an existing alias";
    }

    @Override
    public void run(Console console, UserInput input) {
        List<String> params = input.parameters();
        if (params.size() == 2) {
            Iterator<String> iterator = params.iterator();

            String alias = getParameter(0, iterator);
            String command = getParameter(1, iterator);
            AliasRegistry.INSTANCE.put(new DefaultAlias(alias, command.replaceAll("\\s\\s+", " ")));
        }
    }
}

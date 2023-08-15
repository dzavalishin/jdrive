package com.dzavalishin.commands;

import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

import java.util.Collections;
import java.util.List;

import static com.dzavalishin.console.ConsoleColor.WHITE;

/**
 * Command prints list of supported commands
 */
public class ListCommandsCommand implements Command {
    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getKeyWord() {
        return "list_cmds";
    }

    @Override
    public String getDescription() {
        return "list all registered commands";
    }

    @Override
    public void run(Console console, UserInput input) {
        CommandRegistry.INSTANCE.commands().stream()
                .filter(c -> !c.getKeyWord().equals(getKeyWord()))
                .forEach(c -> console.println(c.getKeyWord(), WHITE));
    }
}

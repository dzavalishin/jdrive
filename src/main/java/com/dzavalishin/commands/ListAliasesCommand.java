package com.dzavalishin.commands;

import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

import java.util.Collections;
import java.util.List;

import static com.dzavalishin.console.ConsoleColor.WHITE;

/**
 * List all registered aliases command implementation
 */
public class ListAliasesCommand implements Command {
    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getKeyWord() {
        return "list_aliases";
    }

    @Override
    public String getDescription() {
        return "list all registered aliases";
    }

    @Override
    public void run(Console console, UserInput input) {
        AliasRegistry.INSTANCE.aliases().forEach(a -> console.println(a.getName(), WHITE));
    }
}

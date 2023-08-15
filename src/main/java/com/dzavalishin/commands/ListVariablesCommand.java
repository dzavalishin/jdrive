package com.dzavalishin.commands;

import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.variables.VariableRegistry;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

import java.util.Collections;
import java.util.List;

import static com.dzavalishin.console.ConsoleColor.WHITE;

/**
 * List all registered variables command implementation
 */
public class ListVariablesCommand implements Command {
    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getKeyWord() {
        return "list_vars";
    }

    @Override
    public String getDescription() {
        return "list all registered variables";
    }

    @Override
    public void run(Console console, UserInput input) {
        VariableRegistry.INSTANCE.variables().forEach(variable -> {
            console.println(variable.getName(), WHITE);
        });
    }
}

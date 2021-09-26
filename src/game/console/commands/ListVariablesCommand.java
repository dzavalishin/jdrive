package game.console.commands;

import game.console.Console;
import game.console.UserInput;
import game.console.parameters.Parameter;
import game.console.variables.VariableRegistry;

import java.util.Collections;
import java.util.List;

import static game.console.ConsoleColor.WHITE;

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

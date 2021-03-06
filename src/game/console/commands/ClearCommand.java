package game.console.commands;

import game.console.Console;
import game.console.UserInput;
import game.console.parameters.Parameter;

import java.util.Collections;
import java.util.List;

/**
 * Clear command implementation
 */
public class ClearCommand implements Command {
    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getKeyWord() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "clear the console buffer";
    }

    @Override
    public void run(Console console, UserInput input) {
        console.clear();
    }
}

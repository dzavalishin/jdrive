package game.console.commands;

import game.Engine;
import game.console.Console;
import game.console.UserInput;
import game.console.parameters.Parameter;

import java.util.Collections;
import java.util.List;

/**
 * Reset engines command implementation
 */
public class ResetEnginesCommand implements Command {
    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getKeyWord() {
        return "resetengines";
    }

    @Override
    public String getDescription() {
        return "reset status data all engines. This might solve issues with 'lost' engines";
    }

    @Override
    public void run(Console console, UserInput input) {
        Engine.StartupEngines();
    }
}

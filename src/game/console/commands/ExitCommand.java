package game.console.commands;

import game.Global;
import game.console.Console;
import game.console.UserInput;
import game.console.parameters.Parameter;

import java.util.Collections;
import java.util.List;

/**
 * Exit game command implementation
 */
public class ExitCommand implements Command {
    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getKeyWord() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "exit the game";
    }

    @Override
    public void run(Console console, UserInput input) {
        Global._exit_game = true;
    }
}

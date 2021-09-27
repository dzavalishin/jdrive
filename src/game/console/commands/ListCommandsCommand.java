package game.console.commands;

import game.console.Console;
import game.console.UserInput;
import game.console.parameters.Parameter;

import java.util.Collections;
import java.util.List;

import static game.console.ConsoleColor.WHITE;

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

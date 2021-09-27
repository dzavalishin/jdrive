package game.console.commands;

import game.console.Console;
import game.console.UserInput;
import game.console.parameters.DefaultParameter;
import game.console.parameters.DefaultParameterDescription;
import game.console.parameters.Parameter;
import game.console.parameters.Type;

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
            AliasRegistry.INSTANCE.put(new DefaultAlias(alias, command));
        }
    }
}

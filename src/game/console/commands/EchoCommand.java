package game.console.commands;

import game.console.Console;
import game.console.ConsoleColor;
import game.console.UserInput;
import game.console.parameters.DefaultParameter;
import game.console.parameters.DefaultParameterDescription;
import game.console.parameters.Parameter;
import game.console.parameters.Type;

import java.util.List;

import static game.console.ConsoleColor.WHITE;

/**
 * Echo command implementation
 */
public class EchoCommand implements Command {
    private List<Parameter> commandParameters = List.of(new DefaultParameter(
            new DefaultParameterDescription(
                    "string",
                    "the string that should be printed"
            ),
            Type.STRING
    ));
    @Override
    public List<Parameter> getParameters() {
        return commandParameters;
    }

    @Override
    public String getKeyWord() {
        return "echo";
    }

    @Override
    public String getDescription() {
        return "print back the first argument to the console";
    }

    @Override
    public void run(Console console, UserInput input) {
        List<String> parameters = input.parameters();
        if (parameters.size() == 1) {
            console.println(getParameter(0, parameters.iterator()), WHITE);
        }
    }
}

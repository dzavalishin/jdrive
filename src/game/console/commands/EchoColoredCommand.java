package game.console.commands;

import game.console.Console;
import game.console.ConsoleColor;
import game.console.UserInput;
import game.console.parameters.DefaultParameter;
import game.console.parameters.DefaultParameterDescription;
import game.console.parameters.Parameter;
import game.console.parameters.Type;

import java.util.Iterator;
import java.util.List;

import static game.console.ConsoleColor.WHITE;

/**
 * Echo with color command implementation
 */
public class EchoColoredCommand implements Command {
    private final List<Parameter> commandParameters = List.of(
            new DefaultParameter(
                    new DefaultParameterDescription(
                            "string",
                            "the string that should be printed"
                    ),
                    Type.STRING
            ),
            new DefaultParameter(
                    new DefaultParameterDescription(
                            "color",
                            "output color (1, 2, 3, 5, 13)"
                    ),
                    Type.INTEGER
            )
    );

    @Override
    public List<Parameter> getParameters() {
        return commandParameters;
    }

    @Override
    public String getKeyWord() {
        return "echoc";
    }

    @Override
    public String getDescription() {
        return "print back the first argument to the console in a given colour";
    }

    @Override
    public void run(Console console, UserInput input) {
        List<String> parameters = input.parameters();
        if (parameters.size() == 2) {
            Iterator<String> parametersIterator = parameters.iterator();
            console.println(getParameter(0, parametersIterator), ConsoleColor.fromInteger(getParameter(1, parametersIterator)).orElse(WHITE));
        }
    }
}

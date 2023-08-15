package com.dzavalishin.commands;

import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.ConsoleColor;
import com.dzavalishin.console.UserInput;

import java.util.Iterator;
import java.util.List;

import static com.dzavalishin.console.ConsoleColor.WHITE;

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

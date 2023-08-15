package com.dzavalishin.commands;

import static com.dzavalishin.console.ConsoleColor.WHITE;

import java.util.List;

import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

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

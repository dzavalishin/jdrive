package com.dzavalishin.commands;

import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

import java.util.Iterator;
import java.util.List;

/**
 * Console command interface
 */
public interface Command {
    /**
     * @return command parameters
     */
    List<Parameter> getParameters();

    /**
     * @return command name
     */
    String getKeyWord();

    /**
     * @return command description
     */
    String getDescription();

    void run(Console console, UserInput input);

    /**
     * command documentation
     *
     * @return string with command help
     */
    default String getCommandDocumentation() {
        StringBuilder builder = new StringBuilder("NAME\n")
                .append(getKeyWord())
                .append(" - ")
                .append(getDescription());

        if (!getParameters().isEmpty())
            builder.append("\nPARAMETERS").append("\n");
        for (Parameter parameter : getParameters()) {
            builder.append(parameter.getDescription().getName())
                    .append(" [")
                    .append(parameter.getType())
                    .append("] - ")
                    .append(parameter.getDescription().getDocumentation())
                    .append("\n");
        }
        return builder.toString();
    }

    /**
     * returns resolved command parameter by index
     *
     * @param id parameter index
     * @param inputParameters user input
     * @param <T> result type
     * @return resolved command parameter
     */
    default <T> T getParameter(int id, Iterator<String> inputParameters) {
        return getParameters().get(id).getType().resolve(inputParameters.next());
    }
}

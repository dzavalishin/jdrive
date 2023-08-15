package com.dzavalishin.commands;

import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.variables.VariableRegistry;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

import java.util.List;
import java.util.Optional;

import static com.dzavalishin.console.ConsoleColor.BLUE;
import static com.dzavalishin.console.ConsoleColor.WHITE;

/**
 * Help command implementation
 */
public class HelpCommand implements Command {
    private final List<Parameter> commandParameters;

    public HelpCommand() {
        this.commandParameters = List.of(new DefaultParameter(
                new DefaultParameterDescription("command"),
                Type.STRING
        ));
    }

    @Override
    public List<Parameter> getParameters() {
        return commandParameters;
    }

    @Override
    public String getKeyWord() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "help";
    }

    @Override
    public void run(Console console, UserInput input) {
        List<String> parameters = input.parameters();
        if (parameters.size() > 0) {
            String name = commandParameters.get(0).getType().resolve(parameters.iterator().next());
            CommandRegistry.INSTANCE.getCommand(name).ifPresent(c -> {
                c.getCommandDocumentation().lines()
                        .forEach(s -> console.println(s, BLUE));
            });
            VariableRegistry.INSTANCE.get(name).ifPresent(v -> {
               console.println(v.getName() + " - " + v.getDescription(), BLUE);
            });
            AliasRegistry.INSTANCE.get(name).ifPresent(a -> {
                Optional<Command> command = CommandRegistry.INSTANCE.getCommand(a.getCommand());
                if (command.isPresent()) {
                    command.get().getCommandDocumentation().lines().forEach(s -> console.println(s, BLUE));
                } else {
                    console.println(a.getName() + " - " + a.getCommand(), BLUE);
                }
            });
        } else {
            console.println(" ---- NextTTD Console Help ---- ", BLUE);
            console.println(" - variables: [command to list all variables: list_vargs]", WHITE);
            console.println(" set vlaue with '<var> = <value>', use '++/--' to in-or decrement", WHITE);
            console.println(" or omit '=' and just '<var> <value>'. get value with typing '<var>'", WHITE);
            console.println(" - commands: [command to list all commands: list_cmds]", WHITE);
            console.println(" call commands with '<command> <arg2> <arg3>...'", WHITE);
            console.println(" - to assign strings, or use them as arguments, enclose it within quotes", WHITE);
            console.println(" like this: '<command> \"string argument with spaces\"'", WHITE);
            console.println(" - use 'help <command> | <variable>' to get specific information", WHITE);
            console.println(" - scroll console output with shift + (up | down) | (pageup | pagedown))", WHITE);
            console.println(" - scroll console input history with the up | down arrows", WHITE);
            console.println("", WHITE);
        }
    }
}

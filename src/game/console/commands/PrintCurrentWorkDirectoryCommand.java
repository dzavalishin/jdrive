package game.console.commands;

import game.console.Console;
import game.console.UserInput;
import game.console.parameters.Parameter;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static game.console.ConsoleColor.WHITE;

/**
 * Print current working directory command implementation
 */
public class PrintCurrentWorkDirectoryCommand implements Command {
    @Override
    public String getKeyWord() {
        return "pwd";
    }

    @Override
    public String getDescription() {
        return "print out the current working directory";
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public void run(Console console, UserInput input) {
        console.println(Path.of(".").toAbsolutePath().toString(), WHITE);
    }
}

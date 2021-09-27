package game.console.commands;

import game.Landscape;
import game.TileIndex;
import game.console.Console;
import game.console.UserInput;
import game.console.parameters.DefaultParameter;
import game.console.parameters.DefaultParameterDescription;
import game.console.parameters.Parameter;
import game.console.parameters.Type;

import java.util.List;

import static game.console.ConsoleColor.WHITE;

/**
 * Reset tile command implementation
 */
public class ResetTileCommand implements Command {
    private final List<Parameter> commandParameters;

    public ResetTileCommand() {
        this.commandParameters = List.of(
                new DefaultParameter(
                        new DefaultParameterDescription("tile number"),
                        Type.INTEGER
                )
        );
    }

    @Override
    public List<Parameter> getParameters() {
        return commandParameters;
    }

    @Override
    public String getKeyWord() {
        return "resettile";
    }

    @Override
    public String getDescription() {
        return "reset a tile to bare land. (Tile can be either decimal (34161) or hexadecimal (0x4a5b)";
    }

    @Override
    public void run(Console console, UserInput input) {
        List<String> params = input.parameters();
        if (params.size() == 1) {
            Integer tileId = getParameter(0, params.iterator());
            Landscape.DoClearSquare(TileIndex.get(tileId));
            console.println(String.format("Tile with id %d was reseted", tileId), WHITE);
        }
    }
}

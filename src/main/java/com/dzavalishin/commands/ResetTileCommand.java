package com.dzavalishin.commands;

import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;

import java.util.List;

import static com.dzavalishin.console.ConsoleColor.WHITE;

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

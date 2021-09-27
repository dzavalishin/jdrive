package game.console.commands;

import game.TileIndex;
import game.console.Console;
import game.console.UserInput;
import game.console.parameters.DefaultParameter;
import game.console.parameters.DefaultParameterDescription;
import game.console.parameters.Parameter;
import game.console.parameters.Type;
import game.xui.ViewPort;

import java.util.List;

/**
 * Scroll to certain tile command implementation
 */
public class ScrollToCommand implements Command {
    private final List<Parameter> commandParameters;

    /**
     * ctor
     */
    public ScrollToCommand() {
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
        return "scrollto";
    }

    @Override
    public String getDescription() {
        return "center the screen on a given tile. (Tile can ne either decimal (34161) or hexadecimal (0x4a5b)";
    }

    @Override
    public void run(Console console, UserInput input) {
        List<String> parameters = input.parameters();
        if (parameters.size() == 1) {
            Integer tileId = getParameter(0, parameters.iterator());
            ViewPort.ScrollMainWindowToTile(TileIndex.get(tileId));
        }
    }
}

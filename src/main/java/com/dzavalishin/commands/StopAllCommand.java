package com.dzavalishin.commands;

import com.dzavalishin.game.Vehicle;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;
import com.dzavalishin.xui.Window;

import java.util.Collections;
import java.util.List;

/**
 * Stop all vehicles command implementation
 */
public class StopAllCommand implements Command {
    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getKeyWord() {
        return "stopall";
    }

    @Override
    public String getDescription() {
        return "stop all vehicles in the game. For debugging only! Use at your own risk...";
    }

    @Override
    public void run(Console console, UserInput input) {
        Vehicle.forEach(v -> {
            if (v.isValid()) {
                /* Code ripped from CmdStartStopTrain. Can't call it, because of
                 * ownership problems, so we'll duplicate some code, for now */
                v.stop();
                Window.InvalidateWindowWidget(Window.WC_VEHICLE_VIEW, v.index, Vehicle.STATUS_BAR);
                Window.InvalidateWindow(Window.WC_VEHICLE_DEPOT, v.getTile().getTile());
            }
        });
    }
}

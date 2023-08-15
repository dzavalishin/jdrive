package com.dzavalishin.commands;

import static com.dzavalishin.console.ConsoleColor.BLUE;
import static com.dzavalishin.console.ConsoleColor.WHITE;

import java.util.List;

import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.UserInput;
import com.dzavalishin.xui.SettingsGui;

public class PatchCommand implements Command {
	private final List<Parameter> commandParameters;

	public PatchCommand() {
		this.commandParameters = List.of(
				new DefaultParameter(
						new DefaultParameterDescription("patch_name"),
						Type.STRING
						),
				new DefaultParameter(
						new DefaultParameterDescription("patch_value"),
						Type.STRING
						)

				);
	}

	@Override
	public List<Parameter> getParameters() {
		return commandParameters;
	}

	@Override
	public String getKeyWord() {
		return "patch";
	}

	@Override
	public String getDescription() {
		return "Change patch variables for all players. Usage: 'patch <name> [<value>]'. Omitting <value> will print out the current value of the patch-setting.";
	}

	@Override
	public void run(Console console, UserInput input) {
		List<String> parameters = input.parameters();
		if (parameters.size() <= 0 || parameters.size() > 2) {
			console.println(getKeyWord(), BLUE);
			console.println(getDescription(), WHITE);
			console.println("", WHITE);
		}
		if (parameters.size() == 1)
			SettingsGui.IConsoleGetPatchSetting(parameters.get(0));
		else
			SettingsGui.IConsoleSetPatchSetting(parameters.get(0), parameters.get(1));
	}
}

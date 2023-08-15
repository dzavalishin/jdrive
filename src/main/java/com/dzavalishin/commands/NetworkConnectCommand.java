package com.dzavalishin.commands;

import static com.dzavalishin.console.ConsoleColor.BLUE;
import static com.dzavalishin.console.ConsoleColor.WHITE;

import java.util.List;

import com.dzavalishin.game.Global;
import com.dzavalishin.net.Net;
import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.DefaultConsole;
import com.dzavalishin.console.UserInput;
import com.dzavalishin.net.NetGui;

public class NetworkConnectCommand implements Command {
	private final List<Parameter> commandParameters;

	public NetworkConnectCommand() {
		this.commandParameters = List.of(
				new DefaultParameter(
						new DefaultParameterDescription("server"),
						Type.STRING
						)				);
	}

	@Override
	public List<Parameter> getParameters() {
		return commandParameters;
	}

	@Override
	public String getKeyWord() {
		return "connect";
	}

	@Override
	public String getDescription() {
		return "Connect to a remote NextTTD server and join the game. Usage: 'connect <ip>'. IP can contain port and player: 'IP#Player:Port', eg: 'server.ottd.org#2:443'";
	}

	@Override
	public void run(Console console, UserInput input) {
		List<String> parameters = input.parameters();
		if (parameters.size() != 1) {
			console.println(getKeyWord(), BLUE);
			console.println(getDescription(), WHITE);
			console.println("", WHITE);
		}

		if (Global._networking) // We are in network-mode, first close it!
		{
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_warn, "Disconnecting");
			Net.NetworkDisconnect();
		}

		int rport = Net.NETWORK_DEFAULT_PORT;

		String [] ip = {null};
		String [] port = {null};
		String [] player = {null};
		NetGui.ParseConnectionString(player, port, ip, parameters.get(0));

		DefaultConsole.IConsolePrintF(DefaultConsole._icolour_def, "Connecting to %s...", ip[0]);
		if (player[0] != null) {
			Global._network_playas = Integer.parseInt(player[0]);
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_def, "    player-no: %s", player[0]);
		}
		if (port[0] != null) {
			rport = Integer.parseInt(port[0]);
			DefaultConsole.IConsolePrintF(DefaultConsole._icolour_def, "    port: %s", port[0]);
		}

		Net.NetworkClientConnectGame(ip[0], rport);		
	}
}

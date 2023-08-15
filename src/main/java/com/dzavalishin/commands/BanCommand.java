package com.dzavalishin.commands;

import static com.dzavalishin.console.ConsoleColor.WHITE;

import java.util.List;

import com.dzavalishin.net.Net;
import com.dzavalishin.net.NetServer;
import com.dzavalishin.net.NetworkClientInfo;
import com.dzavalishin.net.NetworkErrorCode;
import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.DefaultConsole;
import com.dzavalishin.console.UserInput;

public class BanCommand implements Command {
    private List<Parameter> commandParameters = List.of(new DefaultParameter(
            new DefaultParameterDescription(
                    "Client-ID",
                    "client to be banned"
            ),
            Type.INTEGER
    ));

	@Override
	public List<Parameter> getParameters() {
		return commandParameters;
	}

	@Override
	public String getKeyWord() {
		return "ban";
	}

	@Override
	public String getDescription() {
		return "Ban a player from a network game. Usage: 'ban <client-id>'. For client-id's, see the command 'clients'";
	}

	@Override
	public void run(Console console, UserInput input) {
        List<String> parameters = input.parameters();
        if (parameters.size() != 1) {
        	console.println(getDescription(), WHITE);
            console.println(getParameter(0, parameters.iterator()), WHITE);
            return;
        }

		int index = getParameter(0, parameters.iterator());//Integer.parseInt(argv[1]);

		if (index == Net.NETWORK_SERVER_INDEX) {
			//IConsolePrint(_icolour_def, "Silly boy, you can not ban yourself!");
			console.println("Silly boy, you can not ban yourself!", DefaultConsole._icolour_def );
			return;
		}
		if (index == 0) {
			DefaultConsole.IConsoleError("Invalid Client-ID");
			return;
		}

		NetworkClientInfo ci = Net.NetworkFindClientInfoFromIndex(index);

		if (ci != null) {
			// Add user to ban-list 
			for (int i = 0; i < Net._network_ban_list.length; i++) {
				if (Net._network_ban_list[i] == null || Net._network_ban_list[i].isBlank()) {
					//Net._network_ban_list[i] = strdup(inet_ntoa(*(struct in_addr *)&ci.client_ip));
					Net._network_ban_list[i] = ci.getAddress();
					break;
				}
			}

			NetServer.NetworkPacketSend_PACKET_SERVER_ERROR_command(Net.NetworkFindClientStateFromIndex(index), NetworkErrorCode.KICKED);
		} else
			DefaultConsole.IConsoleError("Client-ID not found");

	}

}

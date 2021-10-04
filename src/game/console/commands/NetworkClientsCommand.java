package game.console.commands;

import java.util.Collections;
import java.util.List;

import game.console.Console;
import game.console.DefaultConsole;
import game.console.UserInput;
import game.console.parameters.Parameter;
import game.net.Net;
import game.net.NetworkClientInfo;

public class NetworkClientsCommand implements Command {

	@Override
	public List<Parameter> getParameters() {
        return Collections.emptyList();
	}

	@Override
	public String getKeyWord() { return "clients"; }

	@Override
	public String getDescription() {
		return "Get a list of connected clients including their ID, name, company-id, and IP. Usage: 'clients'";
	}

	@Override
	public void run(Console console, UserInput input) {

		//for (ci = _network_client_info; ci != &_network_client_info[MAX_CLIENT_INFO]; ci++) {
		Net.FOR_ALL_CLIENTS(cs ->
		{
			NetworkClientInfo ci = cs.getCi(); //DEREF_CLIENT_INFO(cs);
			
			if (ci.client_index != Net.NETWORK_EMPTY_INDEX) {
				DefaultConsole.IConsolePrintF(8, "Client #%1d  name: '%s'  company: %1d  IP: %s",
					ci.client_index, ci.client_name, ci.client_playas, ci.GetPlayerIP());
			}
		});
	}

}

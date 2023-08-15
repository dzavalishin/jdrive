package com.dzavalishin.commands;

import java.util.Collections;
import java.util.List;

import com.dzavalishin.net.Net;
import com.dzavalishin.net.NetworkClientInfo;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.DefaultConsole;
import com.dzavalishin.console.UserInput;

public class NetworkStatusCommand implements Command {

	@Override
	public List<Parameter> getParameters() {
        return Collections.emptyList();
	}

	@Override
	public String getKeyWord() { return "status"; }

	@Override
	public String getDescription() {
		return "List the status of all clients connected to the server: Usage 'status'";
	}

	@Override
	public void run(Console console, UserInput input) 
	{
		boolean [] have = {false};
		
		Net.FOR_ALL_CLIENTS(cs -> {
			int lag = Net.NetworkCalculateLag(cs);
			final NetworkClientInfo ci = cs.getCi(); // DEREF_CLIENT_INFO(cs);

			//status = (cs.getStatus().ordinal() <= ClientStatus.ACTIVE.ordinal()) ? ClientStatus.stat_str[cs.status] : "unknown";
			String status = cs.getStatus().toString();
			DefaultConsole.IConsolePrintF(8, "Client #%1d  name: '%s'  status: '%s'  frame-lag: %3d  company: %1d  IP: %s  unique-id: '%s'",
				cs.getIndex(), ci.client_name, status, lag, ci.client_playas, ci.GetPlayerIP(), ci.getUniqueId());
			have[0] = true;
		});

		if(!have[0])
			DefaultConsole.IConsolePrintF(8, "No one is connected" );
	}

}

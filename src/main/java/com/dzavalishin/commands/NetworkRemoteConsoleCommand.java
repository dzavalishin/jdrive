package com.dzavalishin.commands;

import java.io.IOException;
import java.util.List;

import com.dzavalishin.game.Global;
import com.dzavalishin.parameters.DefaultParameter;
import com.dzavalishin.parameters.DefaultParameterDescription;
import com.dzavalishin.parameters.Parameter;
import com.dzavalishin.parameters.Type;
import com.dzavalishin.console.Console;
import com.dzavalishin.console.DefaultConsole;
import com.dzavalishin.console.UserInput;
import com.dzavalishin.net.NetClient;

public class NetworkRemoteConsoleCommand implements Command {

	private List<Parameter> commandParameters = List.of(

			new DefaultParameter(
					new DefaultParameterDescription(
							"password",
							"remote console password"
							), 
					Type.STRING ),

			new DefaultParameter(
					new DefaultParameterDescription(
							"command",
							"command to be executed on remote node (enclose in quotes!)"
							), 
					Type.STRING )

			);

	@Override
	public List<Parameter> getParameters() {
		return commandParameters;
	}

	@Override
	public String getKeyWord() { return "rcon"; }

	@Override
	public String getDescription() {
		return "Remote control the server from another client. Usage: 'rcon <password> <command>'. Remember to enclose the command in quotes, otherwise only the first parameter is sent";
	}

	@Override
	public void run(Console console, UserInput input) 
	{
        List<String> parameters = input.parameters();
        if(parameters.size() != 2)
        {
        	DefaultConsole.IConsolePrintF(DefaultConsole._icolour_def, getDescription());
        	return;
        }
        
		String passwd = getParameter(0, parameters.iterator());
		String cmd = getParameter(0, parameters.iterator());
		
		try {
			NetClient.NetworkPacketSend_PACKET_CLIENT_RCON_command(passwd, cmd);
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
		}
		
	}

}

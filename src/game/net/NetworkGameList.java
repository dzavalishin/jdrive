package game.net;

import java.util.ArrayList;
import java.util.List;

import game.Global;

//
// This file handles the GameList
// Also, it handles the request to a server for data about the server
public class NetworkGameList {
	NetworkGameInfo info;
	int ip;
	int port;
	boolean online;																		// False if the server did not respond (default status)
	boolean manually;																	// True if the server was added manually
	//NetworkGameList next;



	public static List<NetworkGameList> _network_game_list = new ArrayList<>();



	static NetworkGameList addItem(int ip, int port)
	{
		for( NetworkGameList item : _network_game_list )
		{
			if (item.ip == ip && item.port == port)
				return item;
		}

		Global.DEBUG_net( 4, "[NET][GameList] Added server to list");

		NetworkGameList item = new NetworkGameList();

		//item.next = NULL;
		item.ip = ip;
		item.port = port;
		Net._network_game_count++;

		_network_game_list.add(item);

		NetGui.UpdateNetworkGameWindow(false);

		return item;
	}

	static void removeItem(NetworkGameList remove)
	{
		Global.DEBUG_net( 4, "[NET][GameList] Removed server from list");
		_network_game_list.remove(remove);
	}

}

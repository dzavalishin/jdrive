package game.net;

public class NetworkGameList {
		NetworkGameInfo info;
		int ip;
		int port;
		boolean online;																		// False if the server did not respond (default status)
		boolean manually;																	// True if the server was added manually
		NetworkGameList next;

}

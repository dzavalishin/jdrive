package game.net;

import java.net.InetAddress;

public class NetworkClientInfo 
{
		int client_index;                          /// Index of the client (same as ClientState->index)
		String client_name; /// Name of the client
		int client_lang;                             /// The language of the client
		int client_playas;                           /// As which player is this client playing (PlayerID)
		InetAddress client_ip;                             /// IP-address of the client (so he can be banned)
		int join_date;                             /// Gamedate the player has joined
		String unique_id;          /// Every play sends an unique id so we can indentify him
}

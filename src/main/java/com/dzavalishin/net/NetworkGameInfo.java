package com.dzavalishin.net;

// This is the struct used by both client and server
//  some fields will be empty on the client (like game_password) by default
//  and only filled with data a player enters.

public class NetworkGameInfo {
		String server_name;					// Server name
		String hostname;					// Hostname of the server (if any)
		String server_revision;	// The SVN version number the server is using (e.g.: 'r304')
																										//  It even shows a SVN version in release-version, so
																										//  it is easy to compare if a server is of the correct version
		int server_lang;																// Language of the server (we should make a nice table for this)
		boolean use_password;															// Is set to != 0 if it uses a password
		String server_password;	// On the server: the game password, on the client: != "" if server has password
		int clients_max;																// Max clients allowed on server
		int clients_on;																// Current count of clients on server
		int spectators_on;															// How many spectators do we have?
		int game_date;																// Current date
		int start_date;															// When the game started
		public String map_name;							// Map which is played ["random" for a randomized map]
		int map_width;																// Map width
		int map_height;															// Map height
		int map_set;																		// Graphical set
		boolean dedicated;																	// Is this a dedicated server?
		String rcon_password;		// RCon password for the server. "" if rcon is disabled

}

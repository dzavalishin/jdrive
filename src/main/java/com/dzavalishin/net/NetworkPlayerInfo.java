package com.dzavalishin.net;

public class NetworkPlayerInfo implements NetDefs 
{
		String company_name;					// Company name
		public String password;					// The password for the player
		int inaugurated_year;													// What year the company started in
		long company_value;														// The company value
		long money;																		// The amount of money the company has
		long income;																		// How much did the company earned last year
		int performance;															// What was his performance last month?
		int use_password;													// 0: No password 1: There is a password
		int [] num_vehicle = new int [NETWORK_VEHICLE_TYPES];			// How many vehicles are there of this type?
		int [] num_station = new int [NETWORK_STATION_TYPES];			// How many stations are there of this type?
		//char players[NETWORK_PLAYERS_LENGTH];						// The players that control this company (Name1, name2, ..)
		String players;
		public int months_empty;														// How many months the company is empty

}

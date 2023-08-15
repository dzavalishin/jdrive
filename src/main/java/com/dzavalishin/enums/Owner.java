package com.dzavalishin.enums;

import com.dzavalishin.ids.PlayerID;

public class Owner {
	private Owner() { }

	//typedef enum {
	public static final int OWNER_TOWN			= 0x0F;	// a town owns the tile
	public static final int OWNER_NONE			= 0x10;	// nobody owns the tile
	public static final int OWNER_WATER			= 0x11;	// "water" owns the tile
	public static final int OWNER_SPECTATOR		= 0xff;	// spectator in MP or in scenario editor
	//} Owner;

	public static final PlayerID OWNER_TOWN_ID		= PlayerID.get(OWNER_TOWN);
	public static final PlayerID OWNER_NONE_ID		= PlayerID.get(OWNER_NONE);
	public static final PlayerID OWNER_WATER_ID		= PlayerID.get(OWNER_WATER);
	public static final PlayerID OWNER_SPECTATOR_ID	= PlayerID.get(OWNER_SPECTATOR);

}

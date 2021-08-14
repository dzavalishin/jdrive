package game;

import game.ids.PlayerID;

public class Owner {
	private Owner(int m1) {
		//owner = m1;
	}
	//int owner; 

	//typedef enum {
	static final int OWNER_TOWN			= 0xf;	// a town owns the tile
	static final int OWNER_NONE			= 0x10;	// nobody owns the tile
	static final int OWNER_WATER		= 0x11;	// "water" owns the tile
	static final int OWNER_SPECTATOR	= 0xff;	// spectator in MP or in scenario editor
	//} Owner;

	static final PlayerID OWNER_TOWN_ID			= PlayerID.get(OWNER_TOWN);
	static final PlayerID OWNER_NONE_ID			= PlayerID.get(OWNER_NONE);
	static final PlayerID OWNER_WATER_ID		= PlayerID.get(OWNER_WATER);
	static final PlayerID OWNER_SPECTATOR_ID	= PlayerID.get(OWNER_SPECTATOR);

}

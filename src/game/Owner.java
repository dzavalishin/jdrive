package game;

public class Owner {
	public Owner(int m1) {
		owner = m1;
	}
	int owner; 

	//typedef enum {
	static final int OWNER_TOWN			= 0xf;	// a town owns the tile
	static final int OWNER_NONE			= 0x10;	// nobody owns the tile
	static final int OWNER_WATER			= 0x11;	// "water" owns the tile
	static final int OWNER_SPECTATOR	= 0xff;	// spectator in MP or in scenario editor
	//} Owner;

}

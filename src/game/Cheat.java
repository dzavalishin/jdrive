package game;

public class Cheat {
	boolean been_used;	// has this cheat been used before?
	byte value;			// active?

	public Cheat() {
		been_used = false;	
		value = 0;			
	}


	public static Cheat magic_bulldozer;		// dynamite industries, unmovables
	public static Cheat switch_player;			// change to another player
	public static Cheat money;					// get rich
	public static Cheat crossing_tunnels;		// allow tunnels that cross each other
	public static Cheat build_in_pause;			// build while in pause mode
	public static Cheat no_jetcrash;			// no jet will crash on small airports anymore
	public static Cheat switch_climate;
	public static Cheat change_date;			//changes date ingame
	public static Cheat setup_prod;				//setup raw-material production in game
	public static Cheat day_length;				// change day length
	
	private static Cheat [] _cheats = {
			magic_bulldozer,
			switch_player,	
			money,			
			crossing_tunnels,
			build_in_pause,	
			no_jetcrash,		
			switch_climate,
			change_date,		
			setup_prod,		
			day_length,		
	};

	
	/** @Return true if any cheat has been used, false otherwise */
	public static boolean CheatHasBeenUsed()
	{
		for (Cheat cht : _cheats) {
			if (cht.been_used)
				return true;
		}
		return false;
	}
	
}




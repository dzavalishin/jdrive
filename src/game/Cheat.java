package game;

public class Cheat {
	boolean been_used;	// has this cheat been used before?
	//byte value;			// active?
	public final boolean value;			// active?

	public Cheat() {
		been_used = false;	
		value = false;			
	}

	public static class Cheats {
		public Cheat magic_bulldozer;		// dynamite industries, unmovables
		public Cheat switch_player;			// change to another player
		public Cheat money;							// get rich
		public Cheat crossing_tunnels;		// allow tunnels that cross each other
		public Cheat build_in_pause;			// build while in pause mode
		public Cheat no_jetcrash;				// no jet will crash on small airports anymore
		public Cheat switch_climate;
		public Cheat change_date;				//changes date ingame
		public Cheat setup_prod;				//setup raw-material production in game
		public Cheat day_length;				// change day length

		public Cheats() {

		magic_bulldozer = new Cheat();		// dynamite industries, unmovables
		switch_player = new Cheat();			// change to another player
		money = new Cheat();							// get rich
		crossing_tunnels = new Cheat();		// allow tunnels that cross each other
		build_in_pause = new Cheat();			// build while in pause mode
		no_jetcrash = new Cheat();				// no jet will crash on small airports anymore
		switch_climate = new Cheat();
		change_date = new Cheat();				//changes date ingame
		setup_prod = new Cheat();				//setup raw-material production in game
		day_length = new Cheat();				// change day length
		}	
	} 	

	static Cheats cs = new Cheats(); 
	
	/*
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
	*/
	
	
	static final Cheat [] _cheats = {
			cs.magic_bulldozer,
			cs.switch_player,	
			cs.money,			
			cs.crossing_tunnels,
			cs.build_in_pause,	
			cs.no_jetcrash,		
			cs.switch_climate,
			cs.change_date,		
			cs.setup_prod,		
			cs.day_length,
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




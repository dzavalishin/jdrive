package com.dzavalishin.game;

public class Cheat {
	boolean been_used;	// has this cheat been used before?
	//byte value;			// active?
	public final boolean value;			// active?

	public Cheat() {
		been_used = false;	
		value = false;			
	}

	public static class Cheats {
		public final Cheat magic_bulldozer;		// dynamite industries, unmovables
		public final Cheat switch_player;			// change to another player
		public final Cheat money;							// get rich
		public final Cheat crossing_tunnels;		// allow tunnels that cross each other
		public final Cheat build_in_pause;			// build while in pause mode
		public final Cheat no_jetcrash;				// no jet will crash on small airports anymore
		public final Cheat switch_climate;
		public final Cheat change_date;				//changes date ingame
		public final Cheat setup_prod;				//setup raw-material production in game
		public final Cheat day_length;				// change day length

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

	static final Cheats cs = new Cheats();
	
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

	
	/** @return true if any cheat has been used, false otherwise */
	public static boolean CheatHasBeenUsed()
	{
		for (Cheat cht : _cheats) {
			if (cht.been_used)
				return true;
		}
		return false;
	}
	
}




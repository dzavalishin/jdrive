package game;

import game.tables.SettingsTables;

public class GameOptions 
{
	public GameDifficulty diff = SettingsTables._default_game_diff[0]; // TODO must be set in some other way
	public byte diff_level = 0;
	public byte currency;
	public boolean kilometers;
	public byte town_name;
	public byte landscape = Landscape.LT_NORMAL;
	public byte snow_line;
	public byte autosave;
	public byte road_side;

	/* These are the options for the current game
	 * either ingame, or loaded. Also used for networking games */
	public static GameOptions _opt = new GameOptions(); // TODO must be set up!

	/* These are the default options for a new game */
	public static GameOptions _opt_newgame = new GameOptions(); // TODO must be set up!

	// Pointer to one of the two _opt OR _opt_newgame structs
	public static GameOptions _opt_ptr;


	public void assign(GameOptions src)
	{
		diff =  src.diff.makeClone();
		//diff = src.diff;

		diff_level = src.diff_level;
		currency = src.currency;
		kilometers = src.kilometers;
		town_name = src.town_name;
		landscape = src.landscape;
		snow_line = src.snow_line;
		autosave = src.autosave;
		road_side = src.road_side;
	}

}



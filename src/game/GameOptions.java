package game;

public class GameOptions 
{
	public GameDifficulty diff;
	public byte diff_level;
	public byte currency;
	public boolean kilometers;
	public byte town_name;
	public byte landscape;
	public byte snow_line;
	public byte autosave;
	public byte road_side;

	/* These are the options for the current game
	 * either ingame, or loaded. Also used for networking games */
	public static GameOptions _opt;

	/* These are the default options for a new game */
	public static GameOptions _opt_newgame;

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


class GameDifficulty {
	public int max_no_competitors;
	public int competitor_start_time;
	public int number_towns;
	public int number_industries;
	public int max_loan;
	public int initial_interest;
	public int vehicle_costs;
	public int competitor_speed;
	public int competitor_intelligence; // no longer in use
	public int vehicle_breakdowns;
	public int subsidy_multiplier;
	public int construction_cost;
	public int terrain_type;
	public int quantity_sea_lakes;
	public int economy;
	public int line_reverse_mode;
	public int disasters;
	public int town_council_tolerance;	// minimum required town ratings to be allowed to demolish stuff

	protected GameDifficulty makeClone() // throws CloneNotSupportedException {
	{
		GameDifficulty ret = new GameDifficulty();

		ret.max_no_competitors = max_no_competitors;
		ret.competitor_start_time =		competitor_start_time;
		ret.number_towns = number_towns;
		ret.number_industries = number_industries;
		ret.max_loan = max_loan;
		ret.initial_interest = initial_interest;
		ret.vehicle_costs = vehicle_costs;
		ret.competitor_speed = competitor_speed;
		ret.competitor_intelligence = competitor_intelligence; // no longer in use
		ret.vehicle_breakdowns = vehicle_breakdowns;
		ret.subsidy_multiplier = subsidy_multiplier;
		ret.construction_cost = construction_cost;
		ret.terrain_type = terrain_type;
		ret.quantity_sea_lakes = quantity_sea_lakes;
		ret.economy = economy;
		ret.line_reverse_mode = line_reverse_mode;
		ret.disasters = disasters;
		ret.town_council_tolerance = town_council_tolerance;	// minimum required town ratings to be allowed to demolish stuff

		return ret;
	}

	@Override
	protected GameDifficulty clone() throws CloneNotSupportedException 
	{
		return makeClone();
	}

} 

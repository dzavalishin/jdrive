package game;

public class Town 
{
	TileIndex xy;

	// Current population of people and amount of houses.
	int num_houses;
	int population;

	// Town name
	int townnametype;
	int townnameparts;

	// NOSAVE: Location of name sign, UpdateTownVirtCoord updates this.
	ViewportSign sign;

	// Makes sure we don't build certain house types twice.
	byte flags12;

	// Which players have a statue?
	byte statues;

	// Sort index in listings
	byte sort_index_obsolete;

	// Player ratings as well as a mask that determines which players have a rating.
	byte have_ratings;
	int unwanted[]; // how many months companies aren't wanted by towns (bribe)
	PlayerID exclusivity;        // which player has exslusivity
	int exclusive_counter;     // months till the exclusivity expires
	int ratings[];

	// Maximum amount of passengers and mail that can be transported.
	int max_pass;
	int max_mail;
	int new_max_pass;
	int new_max_mail;
	int act_pass;
	int act_mail;
	int new_act_pass;
	int new_act_mail;

	// Amount of passengers that were transported.
	byte pct_pass_transported;
	byte pct_mail_transported;

	// Amount of food and paper that was transported. Actually a bit mask would be enough.
	int act_food;
	int act_water;
	int new_act_food;
	int new_act_water;

	// Time until we rebuild a house.
	byte time_until_rebuild;

	// When to grow town next time.
	byte grow_counter;
	byte growth_rate;

	// Fund buildings program in action?
	byte fund_buildings_months;

	// Fund road reconstruction in action?
	byte road_build_months;

	// Index in town array
	int index;

	// NOSAVE: UpdateTownRadius updates this given the house count.
	int radius[];

	
	public Town() {
		radius = new int[5];
		unwanted = new int[Global.MAX_PLAYERS]; // how many months companies aren't wanted by towns (bribe)
		ratings = new int[Global.MAX_PLAYERS];

	}

	// These refer to the maximums, so Appalling is -1000 to -400
	// MAXIMUM RATINGS BOUNDARIES
	public static final int RATING_MINIMUM 		= -1000;
	public static final int RATING_APPALLING 	= -400;
	public static final int RATING_VERYPOOR 	= -200;
	public static final int RATING_POOR 			= 0;
	public static final int RATING_MEDIOCRE		= 200;
	public static final int RATING_GOOD				= 400;
	public static final int RATING_VERYGOOD		= 600;
	public static final int RATING_EXCELLENT	= 800;
	public static final int RATING_OUTSTANDING= 1000; 	// OUTSTANDING

	public static final int RATING_MAXIMUM = RATING_OUTSTANDING;

	// RATINGS AFFECTING NUMBERS
	public static final int RATING_TREE_DOWN_STEP = -35;
	public static final int RATING_TREE_MINIMUM = RATING_MINIMUM;
	public static final int RATING_TREE_UP_STEP = 7;
	public static final int RATING_TREE_MAXIMUM = 220;

	public static final int RATING_TUNNEL_BRIDGE_DOWN_STEP = -250;
	public static final int RATING_TUNNEL_BRIDGE_MINIMUM = 0;

	public static final int RATING_INDUSTRY_DOWN_STEP = -1500;
	public static final int RATING_INDUSTRY_MINIMUM = RATING_MINIMUM;

	public static final int RATING_ROAD_DOWN_STEP = -50;
	public static final int RATING_ROAD_MINIMUM = -100;
	public static final int RATING_HOUSE_MINIMUM = RATING_MINIMUM;

	public static final int RATING_BRIBE_UP_STEP = 200;
	public static final int RATING_BRIBE_MAXIMUM = 800;
	public static final int RATING_BRIBE_DOWN_TO = -50; 					// XXX SHOULD BE SOMETHING LOWER?

}

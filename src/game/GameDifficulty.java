package game;

public class GameDifficulty 
{
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

	public GameDifficulty(int i, int j, int k, int l, int m, int n, int o, int p, int q, int r, int s, int t, int u,
			int v, int w, int x, int y, int z) {
		 max_no_competitors = i;
		 competitor_start_time = j;
		 number_towns = k;
		 number_industries = l;
		 max_loan = m;
		 initial_interest = n;
		 vehicle_costs = o;
		 competitor_speed = p;
		 competitor_intelligence = q; // no longer in use
		 vehicle_breakdowns = r;
		 subsidy_multiplier = s;
		 construction_cost = t;
		 terrain_type = u;
		 quantity_sea_lakes = v;
		 economy = w;
		 line_reverse_mode = x;
		 disasters = y;
		 town_council_tolerance = z;	// minimum required town ratings to be allowed to demolish stuff
	}

	protected GameDifficulty() {}
	
	public GameDifficulty makeClone() // throws CloneNotSupportedException {
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
	protected GameDifficulty clone() {
		return makeClone();
	}

} 

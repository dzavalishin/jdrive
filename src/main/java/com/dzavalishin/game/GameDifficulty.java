package com.dzavalishin.game;

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

	public int getAsInt(int index)
	{
		switch(index)
		{
		case 0: return max_no_competitors;
		case 1: return competitor_start_time;
		case 2: return number_towns;
		case 3: return number_industries;
		case 4: return max_loan;
		case 5: return initial_interest;
		case 6: return vehicle_costs;
		case 7: return competitor_speed;
		case 8: return competitor_intelligence;
		case 9: return vehicle_breakdowns;
		case 10: return subsidy_multiplier;
		case 11: return construction_cost;
		case 12: return terrain_type;
		case 13: return quantity_sea_lakes;
		case 14: return economy;
		case 15: return line_reverse_mode;
		case 16: return disasters;
		case 17: return town_council_tolerance;
		}

		assert false;
		return 0;
	}

	public void setAsInt(int index, int value)
	{
		switch(index)
		{
		case 0:  max_no_competitors = value; break;
		case 1:  competitor_start_time = value; break;
		case 2:  number_towns = value; break;
		case 3:  number_industries = value; break;
		case 4:  max_loan = value; break;
		case 5:  initial_interest = value; break;
		case 6:  vehicle_costs = value; break;
		case 7:  competitor_speed = value; break;
		case 8:  competitor_intelligence = value; break;
		case 9:  vehicle_breakdowns = value; break;
		case 10: subsidy_multiplier = value; break;
		case 11: construction_cost = value; break;
		case 12: terrain_type = value; break;
		case 13: quantity_sea_lakes = value; break;
		case 14: economy = value; break;
		case 15: line_reverse_mode = value; break;
		case 16: disasters = value; break;
		case 17: town_council_tolerance = value; break;
		}
	}

} 

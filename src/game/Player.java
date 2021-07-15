package game;

public class Player 
{

		long name_2;
		int name_1;

		int president_name_1;
		long president_name_2;

		long face;

		int player_money;
		int current_loan;
		long money64; // internal 64-bit version of the money. the 32-bit field will be clamped to plus minus 2 billion

		byte player_color;
		byte player_money_fraction;
		byte avail_railtypes;
		byte block_preview;
		PlayerID index;

		int cargo_types; // which cargo types were transported the last year 

		TileIndex location_of_house;
		TileIndex last_build_coordinate;

		PlayerID share_owners[];

		byte inaugurated_year;
		byte num_valid_stat_ent;

		byte quarters_of_bankrupcy;
		byte bankrupt_asked; // which players were asked about buying it?
		int bankrupt_timeout;
		int bankrupt_value;

		boolean is_active;
		byte is_ai;
		//PlayerAI ai;
		PlayerAiNew ainew;

		//long yearly_expenses[3][13];
		
		PlayerEconomyEntry cur_economy;
		PlayerEconomyEntry old_economy[];
		EngineID engine_replacement[];
		boolean engine_renew;
		boolean renew_keep_length;
		int engine_renew_months;
		long engine_renew_money;


		public Player()
		{
			share_owners = new PlayerID[4];
			old_economy = new PlayerEconomyEntry[24];
			engine_replacement = new EngineID[Global.TOTAL_NUM_ENGINES];
		}
}

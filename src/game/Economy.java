package game;

public class Economy 
{


	// Maximum possible loan
	int max_loan;
	int max_loan_unround;
	// Economy fluctuation status
	int fluct;
	// Interest
	byte interest_rate;
	byte infl_amount;
	byte infl_amount_pr;



	class Subsidy {
		byte cargo_type;
		byte age;
		int from;
		int to;
	} 


	public static final int SCORE_VEHICLES = 0;
	public static final int SCORE_STATIONS = 1;
	public static final int SCORE_MIN_PROFIT = 2;
	public static final int SCORE_MIN_INCOME = 3;
	public static final int SCORE_MAX_INCOME = 4;
	public static final int SCORE_DELIVERED = 5;
	public static final int SCORE_CARGO = 6;
	public static final int SCORE_MONEY = 7;
	public static final int SCORE_LOAN = 8;
	public static final int SCORE_TOTAL = 9; // This must always be the last entry

	public static final int NUM_SCORE = 10; // How many scores are there..

	public static final int SCORE_MAX = 1000; 	// The max score that can be in the performance history
	//  the scores together of public static final int SCORE_info is allowed to be more!

	class ScoreInfo {
		int id;			// Unique ID of the score
		int needed;			// How much you need to get the perfect score
		int score;			// How much score it will give
		
		public ScoreInfo(int id, int needed, int score ) {
			this.id = id;
			this.needed = needed;
			this.score = score;
		}
	} 

	//static ScoreInfo _score_info[];
	int _score_part[][] = new int [Global.MAX_PLAYERS][NUM_SCORE];

	static Subsidy[] _subsidies = new Subsidy[Global.MAX_PLAYERS];



	// Score info
	static final ScoreInfo _score_info[] = {
	    new ScoreInfo( SCORE_VEHICLES,		120, 			100),
	    new ScoreInfo( SCORE_STATIONS,		80, 			100),
	    new ScoreInfo( SCORE_MIN_PROFIT,	10000,		100),
	    new ScoreInfo( SCORE_MIN_INCOME,	50000,		50),
	    new ScoreInfo( SCORE_MAX_INCOME,	100000,		100),
	    new ScoreInfo( SCORE_DELIVERED,		40000, 		400),
	    new ScoreInfo( SCORE_CARGO,				8,				50),
	    new ScoreInfo( SCORE_MONEY,				10000000,	50),
	    new ScoreInfo( SCORE_LOAN,				250000,		50),
	    new ScoreInfo( SCORE_TOTAL,				0,				0)
	};

	//int _score_part[MAX_PLAYERS][NUM_SCORE];

	void UpdatePlayerHouse(Player p, int score)
	{
		byte val;
		TileIndex tile = p.location_of_house;

		if (tile == null)
			return;

		// ignore x - java syntax needs it
		int x = (val = 128, score < 170) ||
		(val+= 4, score < 350) ||
		(val+= 4, score < 520) ||
		(val+= 4, score < 720) ||
		(val+= 4, true);

		(void)x;
	/* house is already big enough */
		if (val <= tile.getMap().m5)
			return;

		int ti = tile.getTile();
		
		Global._m[ti + TileDiffXY(0, 0)].m5 =   val;
		Global._m[ti + TileDiffXY(0, 1)].m5 = ++val;
		Global._m[ti + TileDiffXY(1, 0)].m5 = ++val;
		Global._m[ti + TileDiffXY(1, 1)].m5 = ++val;

		MarkTileDirtyByTile(tile + TileDiffXY(0, 0));
		MarkTileDirtyByTile(tile + TileDiffXY(0, 1));
		MarkTileDirtyByTile(tile + TileDiffXY(1, 0));
		MarkTileDirtyByTile(tile + TileDiffXY(1, 1));
	}

	long CalculateCompanyValue(final Player p)
	{
		PlayerID owner = p.index;
		long value;

		{
			Station st;
			int num = 0;

			FOR_ALL_STATIONS(st) {
				if (st.xy != 0 && st.owner == owner) {
					int facil = st.facilities;
					do num += (facil&1); while (facil >>= 1);
				}
			}

			value = num * _price.station_value * 25;
		}

		{
			Vehicle v;

			FOR_ALL_VEHICLES(v) {
				if (v.owner != owner)
					continue;
				if (v.type == VEH_Train ||
						v.type == VEH_Road ||
						(v.type == VEH_Aircraft && v.subtype<=2) ||
						v.type == VEH_Ship) {
					value += v.value * 3 >> 1;
				}
			}
		}

		value += p.money64 - p.current_loan; // add real money value

		return Long.max(value, 1);
	}

	// if update is set to true, the economy is updated with this score
	//  (also the house is updated, should only be true in the on-tick event)
	int UpdateCompanyRatingAndValue(Player p, boolean update)
	{
		byte owner = p.index;
		int score = 0;

		memset(_score_part[owner], 0, sizeof(_score_part[owner]));

	/* Count vehicles */
		{
			Vehicle v;
			int min_profit = _score_info[SCORE_MIN_PROFIT].needed;
			int num = 0;

			FOR_ALL_VEHICLES(v) {
				if (v.owner != owner)
					continue;
				if ((v.type == VEH_Train && IsFrontEngine(v)) ||
						v.type == VEH_Road ||
						(v.type == VEH_Aircraft && v.subtype<=2) ||
						v.type == VEH_Ship) {
					num++;
					if (v.age > 730) {
						if (min_profit > v.profit_last_year)
							min_profit = v.profit_last_year;
					}
				}
			}

			_score_part[owner][SCORE_VEHICLES] = num;
			if (min_profit > 0)
				_score_part[owner][SCORE_MIN_PROFIT] = min_profit;
		}

	/* Count stations */
		{
			int num = 0;
			Station st;

			FOR_ALL_STATIONS(st) {
				if (st.xy != 0 && st.owner == owner) {
					int facil = st.facilities;
					do num += facil&1; while (facil>>=1);
				}
			}
			_score_part[owner][SCORE_STATIONS] = num;
		}

	/* Generate statistics depending on recent income statistics */
		{
			PlayerEconomyEntry pee;
			int numec;
			int min_income;
			int max_income;

			numec = min(p.num_valid_stat_ent, 12);
			if (numec != 0) {
				min_income = 0x7FFFFFFF;
				max_income = 0;
				pee = p.old_economy;
				do {
					min_income = Integer.min(min_income, pee.income + pee.expenses);
					max_income = Integer.max(max_income, pee.income + pee.expenses);
				} while (++pee,--numec);

				if (min_income > 0)
					_score_part[owner][SCORE_MIN_INCOME] = min_income;

				_score_part[owner][SCORE_MAX_INCOME] = max_income;
			}
		}

	/* Generate score depending on amount of transported cargo */
		{
			PlayerEconomyEntry pee;
			int numec;
			int total_delivered;

			numec = min(p.num_valid_stat_ent, 4);
			if (numec != 0) {
				pee = p.old_economy;
				total_delivered = 0;
				do {
					total_delivered += pee.delivered_cargo;
				} while (++pee,--numec);

				_score_part[owner][SCORE_DELIVERED] = total_delivered;
			}
		}

	/* Generate score for variety of cargo */
		{
			int cargo = p.cargo_types;
			int num = 0;
			do num += cargo&1; while (cargo>>=1);
			_score_part[owner][SCORE_CARGO] = num;
			if (update)
				p.cargo_types = 0;
		}

	/* Generate score for player money */
		{
			int money = p.player_money;
			if (money > 0) {
				_score_part[owner][SCORE_MONEY] = money;
			}
		}

	/* Generate score for loan */
		{
			_score_part[owner][SCORE_LOAN] = _score_info[SCORE_LOAN].needed - p.current_loan;
		}

		// Now we calculate the score for each item..
		{
			int i;
			int total_score = 0;
			int s;
			score = 0;
			for (i=0;i<NUM_SCORE;i++) {
				// Skip the total
				if (i == SCORE_TOTAL) continue;
				// Check the score
				s = (_score_part[owner][i] >= _score_info[i].needed) ?
					_score_info[i].score :
					((_score_part[owner][i] * _score_info[i].score) / _score_info[i].needed);
				if (s < 0) s = 0;
				score += s;
				total_score += _score_info[i].score;
			}

			_score_part[owner][SCORE_TOTAL] = score;

			// We always want the score scaled to SCORE_MAX (1000)
			if (total_score != SCORE_MAX)
				score = score * SCORE_MAX / total_score;
		}

		if (update) {
	    	p.old_economy[0].performance_history = score;
	    	UpdatePlayerHouse(p, score);
	    	p.old_economy[0].company_value = CalculateCompanyValue(p);
	    }

		InvalidateWindow(WC_PERFORMANCE_DETAIL, 0);
		return score;
	}

	// use Owner.OWNER_SPECTATOR as new_player to delete the player.
	void ChangeOwnershipOfPlayerItems(PlayerID old_player, PlayerID new_player)
	{
		PlayerID old = _current_player;
		_current_player = old_player;

		if (new_player == Owner.OWNER_SPECTATOR) {
			Subsidy s;

			for (s = _subsidies; s != endof(_subsidies); s++) {
				if (s.cargo_type != AcceptedCargo.CT_INVALID && s.age >= 12) {
					if (GetStation(s.to).owner == old_player)
						s.cargo_type = AcceptedCargo.CT_INVALID;
				}
			}
		}

		/* Take care of rating in towns */
		{ Town t;
			if (new_player != Owner.OWNER_SPECTATOR) {
				FOR_ALL_TOWNS(t) {
					/* If a player takes over, give the ratings to that player. */
					if (IsValidTown(t) && HASBIT(t.have_ratings, old_player)) {
						if (HASBIT(t.have_ratings, new_player)) {
							// use max of the two ratings.
							t.ratings[new_player] = max(t.ratings[new_player], t.ratings[old_player]);
						} else {
							SETBIT(t.have_ratings, new_player);
							t.ratings[new_player] = t.ratings[old_player];
						}
					}

					/* Reset ratings for the town */
					if (IsValidTown(t)) {
						t.ratings[old_player] = 500;
						CLRBIT(t.have_ratings, old_player);
					}
				}
			}
		}

		{
			int num_train = 0;
			int num_road = 0;
			int num_ship = 0;
			int num_aircraft = 0;
			Vehicle v;

			// Determine Ids for the new vehicles
			FOR_ALL_VEHICLES(v) {
				if (v.owner == new_player) {
					switch (v.type) {
						case VEH_Train:
							if (IsFrontEngine(v)) num_train++;
							break;
						case VEH_Road:
							num_road++;
							break;
						case VEH_Ship:
							num_ship++;
							break;
						case VEH_Aircraft:
							if (v.subtype <= 2) num_aircraft++;
							break;
						default: break;
					}
				}
			}

			FOR_ALL_VEHICLES(v) {
				if (v.owner == old_player && IS_BYTE_INSIDE(v.type, VEH_Train, VEH_Aircraft+1) ) {
					if (new_player == Owner.OWNER_SPECTATOR) {
						DeleteWindowById(WC_VEHICLE_VIEW, v.index);
						DeleteWindowById(WC_VEHICLE_DETAILS, v.index);
						DeleteWindowById(WC_VEHICLE_ORDERS, v.index);
						DeleteVehicle(v);
					} else {
						v.owner = new_player;
						if (v.type == VEH_Train && IsFrontEngine(v))
							v.unitnumber = ++num_train;
						else if (v.type == VEH_Road)
							v.unitnumber = ++num_road;
						else if (v.type == VEH_Ship)
							v.unitnumber = ++num_ship;
						else if (v.type == VEH_Aircraft && v.subtype <= 2)
							v.unitnumber = ++num_aircraft;
					}
				}
			}
		}

		// Change ownership of tiles
		{
			TileIndex tile = 0;
			do {
				ChangeTileOwner(tile, old_player, new_player);
			} while (++tile != MapSize());
		}

		// Change color of existing windows
		if (new_player != Owner.OWNER_SPECTATOR) {
			Window w;
			for (w = _windows; w != _last_window; w++) {
				if (w.caption_color == old_player)
					w.caption_color = new_player;
			}
		}

		{
			//Player p;
			int i;

			/* Check for shares */
			//FOR_ALL_PLAYERS(p) 
			for( Player p : Global._players )
			{
				for (i = 0; i < 4; i++) {
					/* 'Sell' the share if this player has any */
					if (p.share_owners[i] == _current_player)
						p.share_owners[i] = Owner.OWNER_SPECTATOR;
				}
			}
			p = GetPlayer(_current_player);
			/* Sell all the shares that people have on this company */
			for (i = 0; i < 4; i++)
				p.share_owners[i] = Owner.OWNER_SPECTATOR;
		}

		_current_player = old;

		MarkWholeScreenDirty();
	}

	static void PlayersCheckBankrupt(Player p)
	{
		PlayerID owner;
		long val;

		// If the player has money again, it does not go bankrupt
		if (p.player_money >= 0) {
			p.quarters_of_bankrupcy = 0;
			return;
		}

		p.quarters_of_bankrupcy++;

		owner = p.index;

		switch (p.quarters_of_bankrupcy) {
			case 2:
				AddNewsItem( (StringID)(owner + 16),
					NEWS_FLAGS(NM_CALLBACK, 0, NT_COMPANY_INFO, DNC_BANKRUPCY),0,0);
				break;
			case 3: {
				/* XXX - In multiplayer, should we ask other players if it wants to take
			          over when it is a human company? -- TrueLight */
				if (IS_HUMAN_PLAYER(owner)) {
					AddNewsItem( (StringID)(owner + 16),
						NEWS_FLAGS(NM_CALLBACK, 0, NT_COMPANY_INFO, DNC_BANKRUPCY),0,0);
					break;
				}

				// Check if the company has any value.. if not, declare it bankrupt
				//  right now
				val = CalculateCompanyValue(p);
				if (val > 0) {
					p.bankrupt_value = val;
					p.bankrupt_asked = 1 << owner; // Don't ask the owner
					p.bankrupt_timeout = 0;
					break;
				}
				// Else, falltrue to case 4...
			}
			case 4: {
				// Close everything the owner has open
				DeletePlayerWindows(owner);

//			Show bankrupt news
				Global.SetDParam(0, p.name_1);
				Global.SetDParam(1, p.name_2);
				AddNewsItem( (StringID)(owner + 16*3), NEWS_FLAGS(NM_CALLBACK, 0, NT_COMPANY_INFO, DNC_BANKRUPCY),0,0);

				// If the player is human, and it is no network play, leave the player playing
				if (IS_HUMAN_PLAYER(owner) && !_networking) {
					p.bankrupt_asked = 255;
					p.bankrupt_timeout = 0x456;
				} else {
	/*#ifdef ENABLE_NETWORK
					if (IS_HUMAN_PLAYER(owner) && _network_server) {
						// If we are the server, make sure it is clear that his player is no
						//  longer with us!
						NetworkClientInfo *ci;
						NetworkClientState *cs;
						// * Find all clients that were in control of this company * /
						FOR_ALL_CLIENTS(cs) {
							ci = DEREF_CLIENT_INFO(cs);
							if ((ci.client_playas-1) == owner) {
								ci.client_playas = Owner.OWNER_SPECTATOR;
								// Send the new info to all the clients
								NetworkUpdateClientInfo(_network_own_client_index);
							}
						}
					}
					// Make sure the player no longer controls the company
					if (IS_HUMAN_PLAYER(owner) && owner == _local_player) {
						// Switch the player to spectator..
						_local_player = Owner.OWNER_SPECTATOR;
					}
	#endif /* ENABLE_NETWORK */

					// Convert everything the player owns to NO_OWNER
					p.money64 = p.player_money = 100000000;
					ChangeOwnershipOfPlayerItems(owner, Owner.Owner.OWNER_SPECTATOR);
					// Register the player as not-active
					p.is_active = false;

					if (!IS_HUMAN_PLAYER(owner) && (!_networking || _network_server) && _ai.enabled)
						AI_PlayerDied(owner);
					if (IS_HUMAN_PLAYER(owner) && owner == _local_player && _ai.network_client)
						AI_PlayerDied(owner);
				}
			}
		}
	}

	void DrawNewsBankrupcy(Window w)
	{
		Player p;

		DrawNewsBorder(w);

		p = GetPlayer(WP(w,news_d).ni.string_id & 15);
		DrawPlayerFace(p.face, p.player_color, 2, 23);
		GfxFillRect(3, 23, 3+91, 23+118, 0x323 | USE_COLORTABLE);

		Global.SetDParam(0, p.president_name_1);
		Global.SetDParam(1, p.president_name_2);

		DrawStringMultiCenter(49, 148, STR_7058_PRESIDENT, 94);

		switch(WP(w,news_d).ni.string_id >> 4) {
		case 1:
			DrawStringCentered(w.width>>1, 1, STR_7056_TRANSPORT_COMPANY_IN_TROUBLE, 0);

			Global.SetDParam(0, p.name_1);
			Global.SetDParam(1, p.name_2);

			DrawStringMultiCenter(
				((w.width - 101) >> 1) + 98,
				90,
				STR_7057_WILL_BE_SOLD_OFF_OR_DECLARED,
				w.width - 101);
			break;

		case 2: {
			int price;

			DrawStringCentered(w.width>>1, 1, STR_7059_TRANSPORT_COMPANY_MERGER, 0);
			COPY_IN_DPARAM(0,WP(w,news_d).ni.params, 2);
			Global.SetDParam(2, p.name_1);
			Global.SetDParam(3, p.name_2);
			price = WP(w,news_d).ni.params[2];
			Global.SetDParam(4, price);
			DrawStringMultiCenter(
				((w.width - 101) >> 1) + 98,
				90,
				price==0 ? STR_707F_HAS_BEEN_TAKEN_OVER_BY : STR_705A_HAS_BEEN_SOLD_TO_FOR,
				w.width - 101);
			break;
		}

		case 3:
			DrawStringCentered(w.width>>1, 1, STR_705C_BANKRUPT, 0);
			COPY_IN_DPARAM(0,WP(w,news_d).ni.params, 2);
			DrawStringMultiCenter(
				((w.width - 101) >> 1) + 98,
				90,
				STR_705D_HAS_BEEN_CLOSED_DOWN_BY,
				w.width - 101);
			break;

		case 4:
			DrawStringCentered(w.width>>1, 1, STR_705E_NEW_TRANSPORT_COMPANY_LAUNCHED, 0);
			Global.SetDParam(0, p.name_1);
			Global.SetDParam(1, p.name_2);
			COPY_IN_DPARAM(2,WP(w,news_d).ni.params, 2);
			DrawStringMultiCenter(
				((w.width - 101) >> 1) + 98,
				90,
				STR_705F_STARTS_CONSTRUCTION_NEAR,
				w.width - 101);
			break;

		default:
			NOT_REACHED();
		}
	}

	StringID GetNewsStringBankrupcy(final NewsItem ni)
	{
		final Player p = GetPlayer(ni.string_id & 0xF);

		switch (ni.string_id >> 4) {
		case 1:
			Global.SetDParam(0, STR_7056_TRANSPORT_COMPANY_IN_TROUBLE);
			Global.SetDParam(1, STR_7057_WILL_BE_SOLD_OFF_OR_DECLARED);
			Global.SetDParam(2, p.name_1);
			Global.SetDParam(3, p.name_2);
			return STR_02B6;
		case 2:
			Global.SetDParam(0, STR_7059_TRANSPORT_COMPANY_MERGER);
			Global.SetDParam(1, STR_705A_HAS_BEEN_SOLD_TO_FOR);
			COPY_IN_DPARAM(2,ni.params, 2);
			Global.SetDParam(4, p.name_1);
			Global.SetDParam(5, p.name_2);
			COPY_IN_DPARAM(6,ni.params + 2, 1);
			return STR_02B6;
		case 3:
			Global.SetDParam(0, STR_705C_BANKRUPT);
			Global.SetDParam(1, STR_705D_HAS_BEEN_CLOSED_DOWN_BY);
			COPY_IN_DPARAM(2,ni.params, 2);
			return STR_02B6;
		case 4:
			Global.SetDParam(0, STR_705E_NEW_TRANSPORT_COMPANY_LAUNCHED);
			Global.SetDParam(1, STR_705F_STARTS_CONSTRUCTION_NEAR);
			Global.SetDParam(2, p.name_1);
			Global.SetDParam(3, p.name_2);
			COPY_IN_DPARAM(4,ni.params, 2);
			return STR_02B6;
		default:
			NOT_REACHED();
		}

		/* useless, but avoids compiler warning this way */
		return 0;
	}

	static void PlayersGenStatistics()
	{
		Station st;
		Player p;

		FOR_ALL_STATIONS(st) {
			if (st.xy != 0) {
				_current_player = st.owner;
				SET_EXPENSES_TYPE(EXPENSES_PROPERTY);
				SubtractMoneyFromPlayer(_price.station_value >> 1);
			}
		}

		if (!HASBIT(1<<0|1<<3|1<<6|1<<9, _cur_month))
			return;

		FOR_ALL_PLAYERS(p) {
			if (p.is_active) {
				memmove(&p.old_economy, &p.cur_economy, sizeof(p.old_economy));
				memset(&p.cur_economy, 0, sizeof(p.cur_economy));

				if (p.num_valid_stat_ent != 24)
					p.num_valid_stat_ent++;

				UpdateCompanyRatingAndValue(p, true);
				PlayersCheckBankrupt(p);

				if (p.block_preview != 0)
					p.block_preview--;
			}
		}

		InvalidateWindow(WC_INCOME_GRAPH, 0);
		InvalidateWindow(WC_OPERATING_PROFIT, 0);
		InvalidateWindow(WC_DELIVERED_CARGO, 0);
		InvalidateWindow(WC_PERFORMANCE_HISTORY, 0);
		InvalidateWindow(WC_COMPANY_VALUE, 0);
		InvalidateWindow(WC_COMPANY_LEAGUE, 0);
	}

	static void AddSingleInflation(int *value, uint16 *frac, int amt)
	{
		long tmp;
		int low;
		tmp = BIGMULS(*value, amt);
		*frac = (uint16)(low = (uint16)tmp + *frac);
		*value += (int)(tmp >> 16) + (low >> 16);
	}

	static void AddInflation()
	{
		int i;
		int inf = _economy.infl_amount * 54;

		for (i = 0; i != NUM_PRICES; i++) {
			AddSingleInflation((int*)&_price + i, _price_frac + i, inf);
		}

		_economy.max_loan_unround += BIGMULUS(_economy.max_loan_unround, inf, 16);

		if (_economy.max_loan + 50000 <= _economy.max_loan_unround)
			_economy.max_loan += 50000;

		inf = _economy.infl_amount_pr * 54;
		for (i = 0; i != NUM_CARGO; i++) {
			AddSingleInflation(
				(int*)_cargo_payment_rates + i,
				_cargo_payment_rates_frac + i,
				inf
			);
		}

		InvalidateWindowClasses(WC_BUILD_VEHICLE);
		InvalidateWindowClasses(WC_REPLACE_VEHICLE);
		InvalidateWindowClasses(WC_VEHICLE_DETAILS);
		InvalidateWindow(WC_PAYMENT_RATES, 0);
	}

	static void PlayersPayInterest()
	{
		final Player p;
		int interest = _economy.interest_rate * 54;

		FOR_ALL_PLAYERS(p) {
			if (!p.is_active) continue;

			_current_player = p.index;
			SET_EXPENSES_TYPE(EXPENSES_LOAN_INT);

			SubtractMoneyFromPlayer(BIGMULUS(p.current_loan, interest, 16));

			SET_EXPENSES_TYPE(EXPENSES_OTHER);
			SubtractMoneyFromPlayer(_price.station_value >> 2);
		}
	}

	static void HandleEconomyFluctuations()
	{
		if (_opt.diff.economy == 0) return;

		if (--_economy.fluct == 0) {
			_economy.fluct = -(int)GB(Random(), 0, 2);
			AddNewsItem(STR_7073_WORLD_RECESSION_FINANCIAL, NEWS_FLAGS(NM_NORMAL,0,NT_ECONOMY,0), 0, 0);
		} else if (_economy.fluct == -12) {
			_economy.fluct = GB(Random(), 0, 8) + 312;
			AddNewsItem(STR_7074_RECESSION_OVER_UPTURN_IN, NEWS_FLAGS(NM_NORMAL,0,NT_ECONOMY,0), 0, 0);
		}
	}

	static byte _price_category[] = {
		0, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 1, 1, 1, 1, 1, 1,
		2,
	};

	static final int _price_base[] = {
		100,		// station_value
		100,		// build_rail
		95,			// build_road
		65,			// build_signals
		275,		// build_bridge
		600,		// build_train_depot
		500,		// build_road_depot
		700,		// build_ship_depot
		450,		// build_tunnel
		200,		// train_station_track
		180,		// train_station_length
		600,		// build_airport
		200,		// build_bus_station
		200,		// build_truck_station
		350,		// build_dock
		400000,	// build_railvehicle
		2000,		// build_railwagon
		700000,	// aircraft_base
		14000,	// roadveh_base
		65000,	// ship_base
		20,			// build_trees
		250,		// terraform
		20,			// clear_1
		40,			// purchase_land
		200,		// clear_2
		500,		// clear_3
		20,			// remove_trees
		-70,		// remove_rail
		10,			// remove_signals
		50,			// clear_bridge
		80,			// remove_train_depot
		80,			// remove_road_depot
		90,			// remove_ship_depot
		30,			// clear_tunnel
		10000,	// clear_water
		50,			// remove_rail_station
		30,			// remove_airport
		50,			// remove_bus_station
		50,			// remove_truck_station
		55,			// remove_dock
		1600,		// remove_house
		40,			// remove_road
		5600,		// running_rail[0] railroad
		5200,		// running_rail[1] monorail
		4800,		// running_rail[2] maglev
		9600,		// aircraft_running
		1600,		// roadveh_running
		5600,		// ship_running
		1000000, // build_industry
	};

	static byte price_base_multiplier[] = new byte[NUM_PRICES];

	/**
	 * Reset changes to the price base multipliers.
	 */
	void ResetPriceBaseMultipliers()
	{
		int i;

		// 8 means no multiplier.
		for (i = 0; i < NUM_PRICES; i++)
			price_base_multiplier[i] = 8;
	}

	/**
	 * Change a price base by the given factor.
	 * The price base is altered by factors of two, with an offset of 8.
	 * NewBaseCost = OldBaseCost * 2^(n-8)
	 * @param price Index of price base to change.
	 * @param factor Amount to change by.
	 */
	void SetPriceBaseMultiplier(int price, byte factor)
	{
		assert(price < NUM_PRICES);
		price_base_multiplier[price] = factor;
	}

	void StartupEconomy()
	{
		int i;

		assert(sizeof(_price) == NUM_PRICES * sizeof(int));

		for(i=0; i!=NUM_PRICES; i++) {
			int price = _price_base[i];
			if (_price_category[i] != 0) {
				int mod = _price_category[i] == 1 ? _opt.diff.vehicle_costs : _opt.diff.construction_cost;
				if (mod < 1) {
					price = price * 3 >> 2;
				} else if (mod > 1) {
					price = price * 9 >> 3;
				}
			}
			if (price_base_multiplier[i] > 8) {
				price <<= price_base_multiplier[i] - 8;
			} else {
				price >>= 8 - price_base_multiplier[i];
			}
			((int*)&_price)[i] = price;
			_price_frac[i] = 0;
		}

		_economy.interest_rate = _opt.diff.initial_interest;
		_economy.infl_amount = _opt.diff.initial_interest;
		_economy.infl_amount_pr = max(0, _opt.diff.initial_interest - 1);
		_economy.max_loan_unround = _economy.max_loan = _opt.diff.max_loan * 1000;
		_economy.fluct = GB(Random(), 0, 8) + 168;
	}

	Pair SetupSubsidyDecodeParam(final Subsidy s, boolean mode)
	{
		TileIndex tile;
		TileIndex tile2;
		Pair tp;

		/* if mode is false, use the singular form */
		Global.SetDParam(0, _cargoc.names_s[s.cargo_type] + (mode ? 0 : 32));

		if (s.age < 12) {
			if (s.cargo_type != AcceptedCargo.CT_PASSENGERS && s.cargo_type != AcceptedCargo.CT_MAIL) {
				Global.SetDParam(1, STR_INDUSTRY);
				Global.SetDParam(2, s.from);
				tile = GetIndustry(s.from).xy;

				if (s.cargo_type != AcceptedCargo.CT_GOODS && s.cargo_type != AcceptedCargo.CT_FOOD) {
					Global.SetDParam(4, STR_INDUSTRY);
					Global.SetDParam(5, s.to);
					tile2 = GetIndustry(s.to).xy;
				} else {
					Global.SetDParam(4, STR_TOWN);
					Global.SetDParam(5, s.to);
					tile2 = GetTown(s.to).xy;
				}
			} else {
				Global.SetDParam(1, STR_TOWN);
				Global.SetDParam(2, s.from);
				tile = GetTown(s.from).xy;

				Global.SetDParam(4, STR_TOWN);
				Global.SetDParam(5, s.to);
				tile2 = GetTown(s.to).xy;
			}
		} else {
			Global.SetDParam(1, s.from);
			tile = GetStation(s.from).xy;

			Global.SetDParam(2, s.to);
			tile2 = GetStation(s.to).xy;
		}

		tp.a = tile;
		tp.b = tile2;

		return tp;
	}

	void DeleteSubsidyWithIndustry(uint16 index)
	{
		Subsidy *s;

		for(s=_subsidies; s != endof(_subsidies); s++) {
			if (s.cargo_type != AcceptedCargo.CT_INVALID && s.age < 12 &&
					s.cargo_type != AcceptedCargo.CT_PASSENGERS && s.cargo_type != AcceptedCargo.CT_MAIL &&
					(index == s.from || (s.cargo_type!=AcceptedCargo.CT_GOODS && s.cargo_type!=AcceptedCargo.CT_FOOD && index==s.to))) {
				s.cargo_type = AcceptedCargo.CT_INVALID;
			}
		}
	}

	void DeleteSubsidyWithStation(uint16 index)
	{
		Subsidy s;
		boolean dirty = false;

		for(s=_subsidies; s != endof(_subsidies); s++) {
			if (s.cargo_type != AcceptedCargo.CT_INVALID && s.age >= 12 &&
					(s.from == index || s.to == index)) {
				s.cargo_type = AcceptedCargo.CT_INVALID;
				dirty = true;
			}
		}

		if (dirty)
			InvalidateWindow(WC_SUBSIDIES_LIST, 0);
	}

	class FoundRoute {
		int distance;
		byte cargo;
		void *from;
		void *to;
	} 

	static void FindSubsidyPassengerRoute(FoundRoute *fr)
	{
		Town *from,*to;

		fr.distance = (int)-1;

		fr.from = from = GetTown(RandomRange(_total_towns));
		if (from.xy == 0 || from.population < 400)
			return;

		fr.to = to = GetTown(RandomRange(_total_towns));
		if (from==to || to.xy == 0 || to.population < 400 || to.pct_pass_transported > 42)
			return;

		fr.distance = DistanceManhattan(from.xy, to.xy);
	}

	static void FindSubsidyCargoRoute(FoundRoute fr)
	{
		Industry i;
		int trans, total;
		byte cargo;

		fr.distance = (int)-1;

		fr.from = i = GetIndustry(RandomRange(_total_industries));
		if (i.xy == 0)
			return;

		// Randomize cargo type
		if (Global.Random()&1 && i.produced_cargo[1] != AcceptedCargo.CT_INVALID) {
			cargo = i.produced_cargo[1];
			trans = i.pct_transported[1];
			total = i.total_production[1];
		} else {
			cargo = i.produced_cargo[0];
			trans = i.pct_transported[0];
			total = i.total_production[0];
		}

		// Quit if no production in this industry
		//  or if the cargo type is passengers
		//  or if the pct transported is already large enough
		if (total == 0 || trans > 42 || cargo == AcceptedCargo.CT_INVALID || cargo == AcceptedCargo.CT_PASSENGERS)
			return;

		fr.cargo = cargo;

		if (cargo == AcceptedCargo.CT_GOODS || cargo == AcceptedCargo.CT_FOOD) {
			// The destination is a town
			Town t = GetTown(RandomRange(_total_towns));

			// Only want big towns
			if (t.xy == 0 || t.population < 900)
				return;
			fr.distance = DistanceManhattan(i.xy, t.xy);
			fr.to = t;
		} else {
			// The destination is an industry
			Industry i2 = GetIndustry(RandomRange(_total_industries));

			// The industry must accept the cargo
			if (i == i2 || i2.xy == 0 ||
					(cargo != i2.accepts_cargo[0] &&
					cargo != i2.accepts_cargo[1] &&
					cargo != i2.accepts_cargo[2]))
				return;
			fr.distance = DistanceManhattan(i.xy, i2.xy);
			fr.to = i2;
		}
	}

	static boolean CheckSubsidyDuplicate(Subsidy s)
	{
		final Subsidy ss;

		for (ss = _subsidies; ss != endof(_subsidies); ss++) {
			if (s != ss &&
					ss.from == s.from &&
					ss.to == s.to &&
					ss.cargo_type == s.cargo_type) {
				s.cargo_type = AcceptedCargo.CT_INVALID;
				return true;
			}
		}
		return false;
	}


	static void SubsidyMonthlyHandler()
	{
		Subsidy s;
		Pair pair;
		Station st;
		int n;
		FoundRoute fr;
		boolean modified = false;

		for(s=_subsidies; s != endof(_subsidies); s++) {
			if (s.cargo_type == AcceptedCargo.CT_INVALID)
				continue;

			if (s.age == 12-1) {
				pair = SetupSubsidyDecodeParam(s, 1);
				AddNewsItem(STR_202E_OFFER_OF_SUBSIDY_EXPIRED, NEWS_FLAGS(NM_NORMAL, NF_TILE, NT_SUBSIDIES, 0), pair.a, pair.b);
				s.cargo_type = AcceptedCargo.CT_INVALID;
				modified = true;
			} else if (s.age == 2*12-1) {
				st = GetStation(s.to);
				if (st.owner == _local_player) {
					pair = SetupSubsidyDecodeParam(s, 1);
					AddNewsItem(STR_202F_SUBSIDY_WITHDRAWN_SERVICE, NEWS_FLAGS(NM_NORMAL, NF_TILE, NT_SUBSIDIES, 0), pair.a, pair.b);
				}
				s.cargo_type = AcceptedCargo.CT_INVALID;
				modified = true;
			} else {
				s.age++;
			}
		}

		// 25% chance to go on
		if (CHANCE16(1,4)) {
			// Find a free slot
			s = _subsidies;
			while (s.cargo_type != AcceptedCargo.CT_INVALID) {
				if (++s == endof(_subsidies))
					goto no_add;
			}

			n = 1000;
			do {
				FindSubsidyPassengerRoute(&fr);
				if (fr.distance <= 70) {
					s.cargo_type = AcceptedCargo.CT_PASSENGERS;
					s.from = ((Town*)fr.from).index;
					s.to = ((Town*)fr.to).index;
					goto add_subsidy;
				}
				FindSubsidyCargoRoute(&fr);
				if (fr.distance <= 70) {
					s.cargo_type = fr.cargo;
					s.from = ((Industry*)fr.from).index;
					s.to = (fr.cargo == AcceptedCargo.CT_GOODS || fr.cargo == AcceptedCargo.CT_FOOD) ? ((Town*)fr.to).index : ((Industry*)fr.to).index;
		add_subsidy:
					if (!CheckSubsidyDuplicate(s)) {
						s.age = 0;
						pair = SetupSubsidyDecodeParam(s, 0);
						AddNewsItem(STR_2030_SERVICE_SUBSIDY_OFFERED, NEWS_FLAGS(NM_NORMAL, NF_TILE, NT_SUBSIDIES, 0), pair.a, pair.b);
						modified = true;
						break;
					}
				}
			} while (n--);
		}
	no_add:;
		if (modified)
			InvalidateWindow(WC_SUBSIDIES_LIST, 0);
	}

	static final SaveLoad _subsidies_desc[] = {
		SLE_VAR(Subsidy,cargo_type,		SLE_UINT8),
		SLE_VAR(Subsidy,age,					SLE_UINT8),
		SLE_CONDVAR(Subsidy,from,			SLE_FILE_U8 | SLE_VAR_U16, 0, 4),
		SLE_CONDVAR(Subsidy,from,			SLE_UINT16, 5, 255),
		SLE_CONDVAR(Subsidy,to,				SLE_FILE_U8 | SLE_VAR_U16, 0, 4),
		SLE_CONDVAR(Subsidy,to,				SLE_UINT16, 5, 255),
		SLE_END()
	};

	static void Save_SUBS()
	{
		int i;
		Subsidy s;

		for(i=0; i!=lengthof(_subsidies); i++) {
			s = &_subsidies[i];
			if (s.cargo_type != AcceptedCargo.CT_INVALID) {
				SlSetArrayIndex(i);
				SlObject(s, _subsidies_desc);
			}
		}
	}

	static void Load_SUBS()
	{
		int index;
		while ((index = SlIterateArray()) != -1)
			SlObject(&_subsidies[index], _subsidies_desc);
	}

	int GetTransportedGoodsIncome(int num_pieces, int dist, byte transit_days, byte cargo_type)
	{
		int cargo = cargo_type;
		byte f;

		/* zero the distance if it's the bank and very short transport. */
		if (_opt.landscape == LT_NORMAL && cargo == AcceptedCargo.CT_VALUABLES && dist < 10)
			dist = 0;

		f = 255;
		if (transit_days > _cargoc.transit_days_1[cargo]) {
			transit_days -= _cargoc.transit_days_1[cargo];
			f -= transit_days;

			if (transit_days > _cargoc.transit_days_2[cargo]) {
				transit_days -= _cargoc.transit_days_2[cargo];

				if (f < transit_days)
					f = 0;
				else
					f -= transit_days;
			}
		}
		if (f < 31) f = 31;

		return BIGMULSS(dist * f * num_pieces, _cargo_payment_rates[cargo], 21);
	}

	static void DeliverGoodsToIndustry(TileIndex xy, byte cargo_type, int num_pieces)
	{
		Industry ind, best;
		int t, u;

		/* Check if there's an industry close to the station that accepts
		 * the cargo */
		best = NULL;
		u = Global._patches.station_spread + 8;
		FOR_ALL_INDUSTRIES(ind) {
			if (ind.xy != 0 && (cargo_type == ind.accepts_cargo[0] || cargo_type
					 == ind.accepts_cargo[1] || cargo_type == ind.accepts_cargo[2]) &&
					 ind.produced_cargo[0] != CT_INVALID &&
					 ind.produced_cargo[0] != cargo_type &&
					 (t = DistanceManhattan(ind.xy, xy)) < 2 * u) {
				u = t;
				best = ind;
			}
		}

		/* Found one? */
		if (best != NULL) {
			best.was_cargo_delivered = true;
			best.cargo_waiting[0] = min(best.cargo_waiting[0] + num_pieces, 0xFFFF);
		}
	}

	static boolean CheckSubsidised(Station from, Station to, byte cargo_type)
	{
		Subsidy s;
		TileIndex xy;
		Pair pair;
		Player p;

		// check if there is an already existing subsidy that applies to us
		for(s=_subsidies; s != endof(_subsidies); s++) {
			if (s.cargo_type == cargo_type &&
					s.age >= 12 &&
					s.from == from.index &&
					s.to == to.index)
				return true;
		}

		/* check if there's a new subsidy that applies.. */
		for(s=_subsidies; s != endof(_subsidies); s++) {
			if (s.cargo_type == cargo_type && s.age < 12) {

				/* Check distance from source */
				if (cargo_type == AcceptedCargo.CT_PASSENGERS || cargo_type == AcceptedCargo.CT_MAIL) {
					xy = GetTown(s.from).xy;
				} else {
					xy = (GetIndustry(s.from)).xy;
				}
				if (DistanceMax(xy, from.xy) > 9)
					continue;

				/* Check distance from dest */
				if (cargo_type == AcceptedCargo.CT_PASSENGERS || cargo_type == AcceptedCargo.CT_MAIL || cargo_type == AcceptedCargo.CT_GOODS || cargo_type == AcceptedCargo.CT_FOOD) {
					xy = GetTown(s.to).xy;
				} else {
					xy = (GetIndustry(s.to)).xy;
				}

				if (DistanceMax(xy, to.xy) > 9)
					continue;

				/* Found a subsidy, change the values to indicate that it's in use */
				s.age = 12;
				s.from = from.index;
				s.to = to.index;

				/* Add a news item */
				pair = SetupSubsidyDecodeParam(s, 0);
				InjectDParam(2);

				p = GetPlayer(_current_player);
				Global.SetDParam(0, p.name_1);
				Global.SetDParam(1, p.name_2);
				AddNewsItem(
					STR_2031_SERVICE_SUBSIDY_AWARDED + _opt.diff.subsidy_multiplier,
					NEWS_FLAGS(NM_NORMAL, NF_TILE, NT_SUBSIDIES, 0),
					pair.a, pair.b);

				InvalidateWindow(WC_SUBSIDIES_LIST, 0);
				return true;
			}
		}
		return false;
	}

	static int DeliverGoods(int num_pieces, byte cargo_type, uint16 source, uint16 dest, byte days_in_transit)
	{
		boolean subsidised;
		Station s_from, s_to;
		int profit;

	 	assert(num_pieces > 0);

		// Update player statistics
		{
			Player p = GetPlayer(_current_player);
			p.cur_economy.delivered_cargo += num_pieces;
			SETBIT(p.cargo_types, cargo_type);
		}

		// Get station pointers.
		s_from = GetStation(source);
		s_to = GetStation(dest);

		// Check if a subsidy applies.
		subsidised = CheckSubsidised(s_from, s_to, cargo_type);

		// Increase town's counter for some special goods types
		if (cargo_type == AcceptedCargo.CT_FOOD) s_to.town.new_act_food += num_pieces;
		if (cargo_type == AcceptedCargo.CT_WATER)  s_to.town.new_act_water += num_pieces;

		// Give the goods to the industry.
		DeliverGoodsToIndustry(s_to.xy, cargo_type, num_pieces);

		// Determine profit
		profit = GetTransportedGoodsIncome(num_pieces, DistanceManhattan(s_from.xy, s_to.xy), days_in_transit, cargo_type);


		// Modify profit if a subsidy is in effect
		if (subsidised) {
			if (_opt.diff.subsidy_multiplier < 1) {
				/* 1.5x */
				profit += profit >> 1;
			} else if (_opt.diff.subsidy_multiplier == 1) {
				/* 2x */
				profit *= 2;
			} else if (_opt.diff.subsidy_multiplier == 2) {
				/* 3x */
				profit *= 3;
			} else {
				/* 4x */
				profit *= 4;
			}
		}

		return profit;
	}

	/*
	 * Returns true if Vehicle v should wait loading because other vehicle is
	 * already loading the same cargo type
	 * v = vehicle to load, u = GetFirstInChain(v)
	 */
	static boolean LoadWait(final Vehicle v, final Vehicle u) {
		final Vehicle w;
		final Vehicle x;
		boolean has_any_cargo = false;

		if (!(u.current_order.flags & OF_FULL_LOAD)) return false;

		for (w = u; w != NULL; w = w.next) {
			if (w.cargo_count != 0) {
				if (v.cargo_type == w.cargo_type &&
						u.last_station_visited == w.cargo_source)
					return false;
				has_any_cargo = true;
			}
		}

		FOR_ALL_VEHICLES(x) {
			if ((x.type != VEH_Train || IsFrontEngine(x)) && // for all locs
					u.last_station_visited == x.last_station_visited && // at the same station
					!(x.vehstatus & VS_STOPPED) && // not stopped
					x.current_order.type == OT_LOADING && // loading
					u != x) { // not itself
				boolean other_has_any_cargo = false;
				boolean has_space_for_same_type = false;
				boolean other_has_same_type = false;

				for (w = x; w != NULL; w = w.next) {
					if (w.cargo_count < w.cargo_cap && v.cargo_type == w.cargo_type)
						has_space_for_same_type = true;

					if (w.cargo_count != 0) {
						if (v.cargo_type == w.cargo_type &&
								u.last_station_visited == w.cargo_source)
							other_has_same_type = true;
						other_has_any_cargo = true;
					}
				}

				if (has_space_for_same_type) {
					if (other_has_same_type) return true;
					if (other_has_any_cargo && !has_any_cargo) return true;
				}
			}
		}

		return false;
	}

	int LoadUnloadVehicle(Vehicle v)
	{
		StationID original_cargo_source = v.cargo_source;
		int profit = 0;
		int v_profit; //virtual profit for feeder systems
		int v_profit_total = 0;
		int unloading_time = 20;
		Vehicle u = v;
		int result = 0;
		uint16 last_visited;
		Station st;
		int t;
		int count, cap;
		PlayerID old_player;
		boolean completely_empty = true;

		assert(v.current_order.type == OT_LOADING);

		v.cur_speed = 0;

		old_player = _current_player;
		_current_player = v.owner;

		st = GetStation(last_visited = v.last_station_visited);

		for (; v != NULL; v = v.next) {
			GoodsEntry ge;

			if (v.cargo_cap == 0) continue;

			//ge = &st.goods[v.cargo_type];
			ge = st.goods[v.cargo_type];

			/* unload? */
			if (v.cargo_count != 0) {
				if (v.cargo_source != last_visited && ge.waiting_acceptance & 0x8000 && !(u.current_order.flags & OF_TRANSFER)) {
					// deliver goods to the station
					st.time_since_unload = 0;

					unloading_time += v.cargo_count; /* TTDBUG: bug in original TTD */
	//<<<<<<< .mine
					//profit += DeliverGoods(v.cargo_count, v.cargo_type, v.cargo_source, last_visited, v.cargo_days);				








	//=======

					/* Aircraft planespeed patch: don't let profit get out of control because
					 * aircraft are delivering in very short time!
					 */
					if(v.type == VEH_Aircraft)
						profit += DeliverGoods(v.cargo_count, v.cargo_type, v.cargo_source, last_visited, v.cargo_days * Global._patches.aircraft_speed_coeff);
					else
						profit += DeliverGoods(v.cargo_count, v.cargo_type, v.cargo_source, last_visited, v.cargo_days);

	//>>>>>>> .theirs
					result |= 1;
					v.cargo_count = 0;
				} else if (u.current_order.flags & (OF_UNLOAD | OF_TRANSFER)) {
					/* unload goods and let it wait at the station */
					st.time_since_unload = 0;

					/* Aircraft planespeed patch: don't let profit get out of control because
					 * aircraft are delivering in very short time!
					 */
					if(v.type == VEH_Aircraft) {
						v_profit = GetTransportedGoodsIncome(
								v.cargo_count,
								DistanceManhattan(GetStation(v.cargo_source).xy, GetStation(last_visited).xy),
								v.cargo_days * Global._patches.aircraft_speed_coeff,
								v.cargo_type) * 3 / 2;
					} else {
						v_profit = GetTransportedGoodsIncome(
								v.cargo_count,
								DistanceManhattan(GetStation(v.cargo_source).xy, GetStation(last_visited).xy),
								v.cargo_days,
								v.cargo_type) * 3 / 2;
					}
					v_profit_total += v_profit;
					unloading_time += v.cargo_count;
					t = BitOps.GB(ge.waiting_acceptance, 0, 12);
					if (t == 0) {
						// No goods waiting at station
						ge.enroute_time = v.cargo_days;
						ge.enroute_from = v.cargo_source;
					} else {
						// Goods already waiting at station. Set counters to the worst value.
						if (v.cargo_days >= ge.enroute_time)
							ge.enroute_time = v.cargo_days;
						if (last_visited != ge.enroute_from)
							ge.enroute_from = v.cargo_source;
					}
					// Update amount of waiting cargo
					SB(ge.waiting_acceptance, 0, 12, min(v.cargo_count + t, 0xFFF));
					ge.feeder_profit += v_profit;
					u.profit_this_year += v_profit;
					result |= 2;
					v.cargo_count = 0;
				}

				if (v.cargo_count != 0)
					completely_empty = false;
			}

			/* don't pick up goods that we unloaded */
			if (u.current_order.flags & OF_UNLOAD) continue;

			/* update stats */
			ge.days_since_pickup = 0;
			t = u.max_speed;
			if (u.type == VEH_Road) t >>=1;
			if (u.type == VEH_Train) t = u.u.rail.cached_max_speed;

			// if last speed is 0, we treat that as if no vehicle has ever visited the station.
			ge.last_speed = t < 255 ? t : 255;
			ge.last_age = _cur_year - v.build_year;

			// If there's goods waiting at the station, and the vehicle
			//  has capacity for it, load it on the vehicle.
			count = BitOps.GB(ge.waiting_acceptance, 0, 12);
			if (count != 0 &&
					(cap = v.cargo_cap - v.cargo_count) != 0) {
				int cargoshare;
				int feeder_profit_share;

				if (v.cargo_count == 0)
					TriggerVehicle(v, VEHICLE_TRIGGER_NEW_CARGO);

				/* Skip loading this vehicle if another train/vehicle is already handling
				 * the same cargo type at this station */
				if (Global._patches.improved_load && LoadWait(v,u)) continue;

				/* TODO: Regarding this, when we do gradual loading, we
				 * should first unload all vehicles and then start
				 * loading them. Since this will cause
				 * VEHICLE_TRIGGER_EMPTY to be called at the time when
				 * the whole vehicle chain is really totally empty, the
				 * @completely_empty assignment can then be safely
				 * removed; that's how TTDPatch behaves too. --pasky */
				completely_empty = false;

				if (cap > count) cap = count;
				cargoshare = cap * 10000 / ge.waiting_acceptance;
				feeder_profit_share = ge.feeder_profit * cargoshare / 10000;
				v.cargo_count += cap;
				ge.waiting_acceptance -= cap;
				v.profit_this_year -= feeder_profit_share;
				ge.feeder_profit -= feeder_profit_share;
				unloading_time += cap;
				st.time_since_load = 0;

				// And record the source of the cargo, and the days in travel.
				v.cargo_source = st.index;	//changed this for feeder systems
				v.cargo_days = ge.enroute_time;
				result |= 2;
				st.last_vehicle = v.index;
			}
		}


		v = u;

		if (v_profit_total > 0)
			ShowFeederIncomeAnimation(v.x_pos, v.y_pos, v.z_pos, v_profit_total);

		if (v.type == VEH_Train) {
			// Each platform tile is worth 2 rail vehicles.
			int overhang = v.u.rail.cached_total_length - GetStationPlatforms(st, v.tile) * 16;
			if (overhang > 0) {
				unloading_time <<= 1;
				unloading_time += (overhang * unloading_time) / 8;
			}
		}

		v.load_unload_time_rem = unloading_time;

		if (completely_empty) {
			TriggerVehicle(v, VEHICLE_TRIGGER_EMPTY);
		}

		if (result != 0) {
			InvalidateWindow(WC_VEHICLE_DETAILS, v.index);

			if (result & 2)
				InvalidateWindow(WC_STATION_VIEW, last_visited);

			if (profit != 0) {

				if (GetStation(last_visited).owner == Owner.OWNER_TOWN 
					&& GetStation(original_cargo_source).owner == Owner.OWNER_TOWN)
					
					MA_Tax(profit*2, v);

				else if (GetStation(last_visited).owner == Owner.OWNER_TOWN 
					|| GetStation(original_cargo_source).owner == Owner.OWNER_TOWN)

					MA_Tax(profit, v);

				v.profit_this_year += profit;
				SubtractMoneyFromPlayer(-profit);

				if (IsLocalPlayer()) SndPlayVehicleFx(SND_14_CASHTILL, v);

				ShowCostOrIncomeAnimation(v.x_pos, v.y_pos, v.z_pos, -profit);
			}
		}

		_current_player = old_player;
		return result;
	}

	void PlayersMonthlyLoop()
	{
		PlayersGenStatistics();
		if (Global._patches.inflation && Global._cur_year < MAX_YEAR_END)
			AddInflation();
		PlayersPayInterest();
		// Reset the _current_player flag
		Global._current_player = Owner.OWNER_NONE;
		HandleEconomyFluctuations();
		SubsidyMonthlyHandler();
	}

	static void DoAcquireCompany(Player p)
	{
		Player owner;
		int i,pi;
		long value;

		Global.SetDParam(0, p.name_1);
		Global.SetDParam(1, p.name_2);
		Global.SetDParam(2, p.bankrupt_value);
		AddNewsItem( (StringID)(_current_player + 16*2), NEWS_FLAGS(NM_CALLBACK, 0, NT_COMPANY_INFO, DNC_BANKRUPCY),0,0);

		// original code does this a little bit differently
		pi = p.index;
		ChangeOwnershipOfPlayerItems(pi, _current_player);

		if (p.bankrupt_value == 0) {
			owner = GetPlayer(_current_player);
			owner.current_loan += p.current_loan;
		}

		value = CalculateCompanyValue(p) >> 2;
		for(i=0; i!=4; i++) {
			if (p.share_owners[i] != Owner.Owner.OWNER_SPECTATOR) {
				owner = GetPlayer(p.share_owners[i]);
				owner.money64 += value;
				owner.yearly_expenses[0][EXPENSES_OTHER] += value;
				UpdatePlayerMoney32(owner);
			}
		}

		p.is_active = false;

		DeletePlayerWindows(pi);
		RebuildVehicleLists();	//Updates the open windows to add the newly acquired vehicles to the lists
	}

	//extern int GetAmountOwnedBy(Player p, byte owner);

	/** Acquire shares in an opposing company.
	 * @param x,y unused
	 * @param p1 player to buy the shares from
	 * @param p2 unused
	 */
	int CmdBuyShareInCompany(int x, int y, int flags, int p1, int p2)
	{
		Player p;
		long cost;

		/* Check if buying shares is allowed (protection against modified clients */
		if (p1 >= Global.MAX_PLAYERS || !Global._patches.allow_shares) return CMD_ERROR;

		SET_EXPENSES_TYPE(EXPENSES_OTHER);
		p = GetPlayer(p1);

		/* Protect new companies from hostile takeovers */
		if (Global._cur_year - p.inaugurated_year < 6) return_cmd_error(STR_7080_PROTECTED);

		/* Those lines are here for network-protection (clients can be slow) */
		if (GetAmountOwnedBy(p, Owner.Owner.OWNER_SPECTATOR) == 0) return 0;

		/* We can not buy out a real player (temporarily). TODO: well, enable it obviously */
		if (GetAmountOwnedBy(p, Owner.Owner.OWNER_SPECTATOR) == 1 && !p.is_ai) return 0;

		cost = CalculateCompanyValue(p) >> 2;
		if (flags & DC_EXEC) {
			PlayerID b = p.share_owners;
			int i;

			while (*b != Owner.Owner.OWNER_SPECTATOR) b++; /* share owners is guaranteed to contain at least one Owner.OWNER_SPECTATOR */
			*b = _current_player;

			for (i = 0; p.share_owners[i] == _current_player;) {
				if (++i == 4) {
					p.bankrupt_value = 0;
					DoAcquireCompany(p);
					break;
				}
			}
			InvalidateWindow(WC_COMPANY, (int)p1);
		}
		return cost;
	}

	/** Sell shares in an opposing company.
	 * @param x,y unused
	 * @param p1 player to sell the shares from
	 * @param p2 unused
	 */
	int CmdSellShareInCompany(int x, int y, int flags, int p1, int p2)
	{
		Player p;
		long cost;

		/* Check if buying shares is allowed (protection against modified clients */
		if (p1 >= Global.MAX_PLAYERS || !Global._patches.allow_shares) return CMD_ERROR;

		SET_EXPENSES_TYPE(EXPENSES_OTHER);
		p = GetPlayer(p1);

		/* Those lines are here for network-protection (clients can be slow) */
		if (GetAmountOwnedBy(p, _current_player) == 0) return 0;

		/* adjust it a little to make it less profitable to sell and buy */
		cost = CalculateCompanyValue(p) >> 2;
		cost = -(cost - (cost >> 7));

		if (flags & DC_EXEC) {
			PlayerID b = p.share_owners;
			while (*b != _current_player) b++; /* share owners is guaranteed to contain player */
			*b = Owner.Owner.OWNER_SPECTATOR;
			InvalidateWindow(WC_COMPANY, (int)p1);
		}
		return cost;
	}

	/** Buy up another company.
	 * When a competing company is gone bankrupt you get the chance to purchase
	 * that company.
	 * @todo currently this only works for AI players
	 * @param x,y unused
	 * @param p1 player/company to buy up
	 * @param p2 unused
	 */
	int CmdBuyCompany(int x, int y, int flags, int p1, int p2)
	{
		Player p;

		/* Disable takeovers in multiplayer games */
		if (p1 >= Global.MAX_PLAYERS || _networking) return CMD_ERROR;

		SET_EXPENSES_TYPE(EXPENSES_OTHER);
		p = GetPlayer(p1);

		if (!p.is_ai) return CMD_ERROR;

		if (flags & DC_EXEC) {
			DoAcquireCompany(p);
		}
		return p.bankrupt_value;
	}
/*
	// Prices
	static void SaveLoad_PRIC()
	{
		SlArray(&_price, NUM_PRICES, SLE_INT32);
		SlArray(&_price_frac, NUM_PRICES, SLE_UINT16);
	}

	// Cargo payment rates
	static void SaveLoad_CAPR()
	{
		SlArray(&_cargo_payment_rates, NUM_CARGO, SLE_INT32);
		SlArray(&_cargo_payment_rates_frac, NUM_CARGO, SLE_UINT16);
	}

	static final SaveLoad _economy_desc[] = {
		SLE_VAR(Economy,max_loan,						SLE_INT32),
		SLE_VAR(Economy,max_loan_unround,		SLE_INT32),
		SLE_VAR(Economy,fluct,							SLE_FILE_I16 | SLE_VAR_I32),
		SLE_VAR(Economy,interest_rate,			SLE_UINT8),
		SLE_VAR(Economy,infl_amount,				SLE_UINT8),
		SLE_VAR(Economy,infl_amount_pr,			SLE_UINT8),
		SLE_END()
	};

	// Economy variables
	static void SaveLoad_ECMY()
	{
		SlObject(&_economy, _economy_desc);
	}

	final ChunkHandler _economy_chunk_handlers[] = {
		{ 'PRIC', SaveLoad_PRIC, SaveLoad_PRIC, CH_RIFF | CH_AUTO_LENGTH},
		{ 'CAPR', SaveLoad_CAPR, SaveLoad_CAPR, CH_RIFF | CH_AUTO_LENGTH},
		{ 'SUBS', Save_SUBS,			Load_SUBS, CH_ARRAY},
		{ 'ECMY', SaveLoad_ECMY, SaveLoad_ECMY, CH_RIFF | CH_LAST},
	};
*/
}

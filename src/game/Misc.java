package game;

public class Misc {

	extern void StartupEconomy();

	char _name_array[512][32];

	#ifndef MERSENNE_TWISTER

	#ifdef RANDOM_DEBUG

	int DoHal.Random(int line, final char *file)
	#else // RANDOM_DEBUG
	int Hal.Random()
	#endif // RANDOM_DEBUG
	{

	int s;
	int t;

	#ifdef RANDOM_DEBUG
		if (_networking && (DEREF_CLIENT(0).status != STATUS_INACTIVE || !_network_server))
			printf("Random [%d/%d] %s:%d\n",_frame_counter, Global._current_player, file, line);
	#endif

		s = _random_seeds[0][0];
		t = _random_seeds[0][1];
		_random_seeds[0][0] = s + ROR(t ^ 0x1234567F, 7) + 1;
		return _random_seeds[0][1] = ROR(s, 3) - 1;
	}
	#endif // MERSENNE_TWISTER

	#if defined(RANDOM_DEBUG) && !defined(MERSENNE_TWISTER)
	int DoRandomRange(int max, int line, final char *file)
	{
		return BitOps.GB(DoHal.Random(line, file), 0, 16) * max >> 16;
	}
	#else
	int RandomRange(int max)
	{
		return BitOps.GB(Hal.Random(), 0, 16) * max >> 16;
	}
	#endif

	/*
	int InteractiveHal.Random()
	{
		int t = _random_seeds[1][1];
		int s = _random_seeds[1][0];
		_random_seeds[1][0] = s + ROR(t ^ 0x1234567F, 7) + 1;
		return _random_seeds[1][1] = ROR(s, 3) - 1;
	}

	int InteractiveRandomRange(int max)
	{
		return BitOps.GB(InteractiveHal.Random(), 0, 16) * max >> 16;
	}

	void SetDate(int date)
	{
		YearMonthDay ymd;
		ConvertDayToYMD(&ymd, _date = date);
		_cur_year = ymd.year;
		_cur_month = ymd.month;
	#ifdef ENABLE_NETWORK
		_network_last_advertise_date = 0;
	#endif /* ENABLE_NETWORK */
	}
	*/



	class LandscapePredefVar {
		StringID names[NUM_CARGO];
		byte weights[NUM_CARGO];
		StringID sprites[NUM_CARGO];

		int initial_cargo_payment[NUM_CARGO];
		byte transit_days_table_1[NUM_CARGO];
		byte transit_days_table_2[NUM_CARGO];

		byte railwagon_by_cargo[3][NUM_CARGO];

		byte road_veh_by_cargo_start[NUM_CARGO];
		byte road_veh_by_cargo_count[NUM_CARGO];
	} LandscapePredefVar;




	// Calculate finalants that depend on the landscape type.
	void InitializeLandscapeVariables(boolean only_finalants)
	{
		final LandscapePredefVar *lpd;
		int i;
		StringID str;

		lpd = &_landscape_predef_var[GameOptions._opt.landscape];

		memcpy(_cargoc.ai_railwagon, lpd.railwagon_by_cargo, sizeof(lpd.railwagon_by_cargo));
		memcpy(_cargoc.ai_roadveh_start, lpd.road_veh_by_cargo_start,sizeof(lpd.road_veh_by_cargo_start));
		memcpy(_cargoc.ai_roadveh_count, lpd.road_veh_by_cargo_count,sizeof(lpd.road_veh_by_cargo_count));

		for(i=0; i!=NUM_CARGO; i++) {
			_cargoc.sprites[i] = lpd.sprites[i];

			str = lpd.names[i];
			_cargoc.names_s[i] = str;
			_cargoc.names_long[i] = (str += 0x40);
			_cargoc.names_short[i] = (str += 0x20);
			_cargoc.weights[i] = lpd.weights[i];

			if (!only_finalants) {
				_cargo_payment_rates[i] = lpd.initial_cargo_payment[i];
				_cargo_payment_rates_frac[i] = 0;
			}

			_cargoc.transit_days_1[i] = lpd.transit_days_table_1[i];
			_cargoc.transit_days_2[i] = lpd.transit_days_table_2[i];
		}
	}


	void OnNewDay_Train(Vehicle v);
	void OnNewDay_RoadVeh(Vehicle v);
	void OnNewDay_Aircraft(Vehicle v);
	void OnNewDay_Ship(Vehicle v);
	static void OnNewDay_EffectVehicle(Vehicle v) { /* empty */ }
	void OnNewDay_DisasterVehicle(Vehicle v);

	typedef void OnNewVehicleDayProc(Vehicle v);

	static OnNewVehicleDayProc * _on_new_vehicle_day_proc[] = {
		OnNewDay_Train,
		OnNewDay_RoadVeh,
		OnNewDay_Ship,
		OnNewDay_Aircraft,
		OnNewDay_EffectVehicle,
		OnNewDay_DisasterVehicle,
	};

	void EnginesDailyLoop();
	void DisasterDailyLoop();
	void PlayersMonthlyLoop();
	void EnginesMonthlyLoop();
	void TownsMonthlyLoop();
	void IndustryMonthlyLoop();
	void StationMonthlyLoop();

	void PlayersYearlyLoop();
	void TrainsYearlyLoop();
	void RoadVehiclesYearlyLoop();
	void AircraftYearlyLoop();
	void ShipsYearlyLoop();

	void WaypointsDailyLoop();


	static final int _autosave_months[] = {
		0, // never
		0xFFF, // every month
		0x249, // every 3 months
		0x041, // every 6 months
		0x001, // every 12 months
	};

	/**
	 * Runs the day_proc for every DAY_TICKS vehicle starting at daytick.
	 */
	static void RunVehicleDayProc(int daytick)
	{
		int i, total = _vehicle_pool.total_items;

		for (i = daytick; i < total; i += DAY_TICKS) {
			Vehicle v = GetVehicle(i);
			if (v.type != 0)
				_on_new_vehicle_day_proc[v.type - 0x10](v);
		}
	}

	/*
	void IncreaseDate()
	{
		YearMonthDay ymd;

		if (Global._game_mode == GameModes.GM_MENU) {
			_tick_counter++;
			return;
		}

		RunVehicleDayProc(_date_fract);

		// increase day, and check if a new day is there? 
		_tick_counter++;

		_date_fract++;
		if (_date_fract < (DAY_TICKS*Global._patches.day_length))
			return;
		_date_fract = 0;

		// yeah, increse day counter and call various daily loops 
		_date++;

		TextMessageDailyLoop();

		DisasterDailyLoop();
		WaypointsDailyLoop();

		if (Global._game_mode != GameModes.GM_MENU) {
			InvalidateWindowWidget(Window.WC_STATUS_BAR, 0, 0);
			EnginesDailyLoop();
		}

		// check if we entered a new month? 
		ConvertDayToYMD(&ymd, _date);
		if ((byte)ymd.month == _cur_month)
			return;
		_cur_month = ymd.month;

		// yes, call various monthly loops 
		if (Global._game_mode != GameModes.GM_MENU) {
			if (BitOps.HASBIT(_autosave_months[GameOptions._opt.autosave], _cur_month)) {
				_do_autosave = true;
				RedrawAutosave();
			}

			PlayersMonthlyLoop();
			EnginesMonthlyLoop();
			TownsMonthlyLoop();
			IndustryMonthlyLoop();
			StationMonthlyLoop();
	#ifdef ENABLE_NETWORK
			if (_network_server)
				NetworkServerMonthlyLoop();
	#endif //* ENABLE_NETWORK 
		}

		//* check if we entered a new year? 
		if ((byte)ymd.year == _cur_year)
			return;
		_cur_year = ymd.year;

		//* yes, call various yearly loops 

		PlayersYearlyLoop();
		TrainsYearlyLoop();
		RoadVehiclesYearlyLoop();
		AircraftYearlyLoop();
		ShipsYearlyLoop();
	#ifdef ENABLE_NETWORK
		if (_network_server)
			NetworkServerYearlyLoop();
	#endif // ENABLE_NETWORK 

		/// check if we reached end of the game (31 dec 2050) 
		if (_cur_year == Global._patches.ending_date - MAX_YEAR_BEGIN_REAL) {
				ShowEndGameChart();
		// check if we reached 2090 (MAX_YEAR_END_REAL), that's the maximum year. 
		} else if (_cur_year == (MAX_YEAR_END + 1)) {
			Vehicle v;
			_cur_year = MAX_YEAR_END;
			_date = 62093;
			FOR_ALL_VEHICLES(v) {
				v.date_of_last_service -= 365; // 1 year is 365 days long
			}

			//* Because the _date wraps here, and text-messages expire by game-days, we have to clean out
			//*  all of them if the date is set back, else those messages will hang for ever 
			InitTextMessage();
		}

		if (Global._patches.auto_euro)
			CheckSwitchToEuro();

		// XXX: check if year 2050 was reached 
	}
	*/
	int FindFirstBit(int value)
	{
		// This is much faster than the one that was before here.
		//  Created by Darkvater.. blame him if it is wrong ;)
		// Btw, the macro FINDFIRSTBIT is better to use when your value is
		//  not more than 128.
		byte i = 0;
		if (value & 0xffff0000) { value >>= 16; i += 16; }
		if (value & 0x0000ff00) { value >>= 8;  i += 8; }
		if (value & 0x000000f0) { value >>= 4;  i += 4; }
		if (value & 0x0000000c) { value >>= 2;  i += 2; }
		if (value & 0x00000002) { i += 1; }
		return i;
	}

	//!We're writing an own sort algorithm here, as
	//!qsort isn't stable
	//!Since the number of elements will be low, a
	//!simple bubble sort will have to do :)

	void bubblesort(void *base, size_t nmemb, size_t size, int(*compar)(final void *, final void *))
	{
		int i,k;
		void *buffer = malloc(size);
		char *start = base;

		nmemb--;

		for (i = 0; i < nmemb; i++) {
			for (k = 0; k < nmemb; k++) {
				void *a, *b;
				a = start + size * k;
				b = start + size * (k + 1);
				if (compar(a, b) > 0) {
					memcpy(buffer, a, size);
					memcpy(a, b, size);
					memcpy(b, buffer, size);
				}
			}
		}

		free(buffer);
		buffer = null;
	}

	static void Save_NAME()
	{
		int i;

		for (i = 0; i != lengthof(_name_array); ++i) {
			if (_name_array[i][0] != '\0') {
				SlSetArrayIndex(i);
				SlArray(_name_array[i], strlen(_name_array[i]), SLE_UINT8);
			}
		}
	}

	static void Load_NAME()
	{
		int index;

		while ((index = SlIterateArray()) != -1) {
			SlArray(_name_array[index],SlGetFieldLength(),SLE_UINT8);
		}
	}

	static final SaveLoad _gameGameOptions._opt_desc[] = {
		// added a new difficulty option (town attitude) in version 4
		SLE_CONDARR(GameOptions,diff,						SLE_FILE_I16 | SLE_VAR_I32, 17, 0, 3),
		SLE_CONDARR(GameOptions,diff,						SLE_FILE_I16 | SLE_VAR_I32, 18, 4, 255),
		SLE_VAR(GameOptions,diff_level,			SLE_UINT8),
		SLE_VAR(GameOptions,currency,				SLE_UINT8),
		SLE_VAR(GameOptions,kilometers,			SLE_UINT8),
		SLE_VAR(GameOptions,town_name,			SLE_UINT8),
		SLE_VAR(GameOptions,landscape,			SLE_UINT8),
		SLE_VAR(GameOptions,snow_line,			SLE_UINT8),
		SLE_VAR(GameOptions,autosave,				SLE_UINT8),
		SLE_VAR(GameOptions,road_side,			SLE_UINT8),
		SLE_END()
	};

	// Save load game options
	static void SaveLoad_OPTS()
	{
		SlObject(&GameOptions._opt, _gameGameOptions._opt_desc);
	}


	static final SaveLoadGlobVarList _date_desc[] = {
		{&_date, 										SLE_UINT16, 0, 255},
		{&_date_fract, 							SLE_UINT16, 0, 255},
		{&_tick_counter, 						SLE_UINT16, 0, 255},
		{&_vehicle_id_ctr_day, 			SLE_UINT16, 0, 255},
		{&_age_cargo_skip_counter, 	SLE_UINT8,	0, 255},
		{&_avail_aircraft, 					SLE_UINT8,	0, 255},
		{&_cur_tileloop_tile, 			SLE_FILE_U16 | SLE_VAR_U32, 0, 5},
		{&_cur_tileloop_tile, 			SLE_UINT32, 6, 255},
		{&_disaster_delay, 					SLE_UINT16, 0, 255},
		{&_station_tick_ctr, 				SLE_UINT16, 0, 255},
		{&_random_seeds[0][0], 					SLE_UINT32, 0, 255},
		{&_random_seeds[0][1], 					SLE_UINT32, 0, 255},
		{&_cur_town_ctr, 						SLE_FILE_U8 | SLE_VAR_U32,	0, 9},
		{&_cur_town_ctr,						SLE_UINT32, 10, 255},
		{&_cur_player_tick_index, 	SLE_FILE_U8 | SLE_VAR_UINT, 0, 255},
		{&_next_competitor_start, 	SLE_FILE_U16 | SLE_VAR_UINT, 0, 255},
		{&_trees_tick_ctr, 					SLE_UINT8,	0, 255},
		{&_pause, 									SLE_UINT8,	4, 255},
		{&_cur_town_iter, 						SLE_UINT32,	11, 255},
		{null,											0,					0,   0}
	};

	// Save load date related variables as well as persistent tick counters
	// XXX: currently some unrelated stuff is just put here
	static void SaveLoad_DATE()
	{
		SlGlobList(_date_desc);
	}


	static final SaveLoadGlobVarList _view_desc[] = {
		{&_saved_scrollpos_x,			SLE_FILE_I16 | SLE_VAR_INT, 0, 5},
		{&_saved_scrollpos_x,			SLE_INT32, 6, 255},
		{&_saved_scrollpos_y,			SLE_FILE_I16 | SLE_VAR_INT, 0, 5},
		{&_saved_scrollpos_y,			SLE_INT32, 6, 255},
		{&_saved_scrollpos_zoom,	SLE_UINT8,	0, 255},
		{null,										0,					0,   0}
	};

	static void SaveLoad_VIEW()
	{
		SlGlobList(_view_desc);
	}

	static int _map_dim_x;
	static int _map_dim_y;

	static final SaveLoadGlobVarList _map_dimensions[] = {
		{&_map_dim_x, SLE_UINT32, 6, 255},
		{&_map_dim_y, SLE_UINT32, 6, 255},
		{null, 0, 0, 0}
	};

	static void Save_MAPS()
	{
		_map_dim_x = MapSizeX();
		_map_dim_y = MapSizeY();
		SlGlobList(_map_dimensions);
	}

	static void Load_MAPS()
	{
		SlGlobList(_map_dimensions);
		AllocateMap(_map_dim_x, _map_dim_y);
	}

	static void Load_MAPT()
	{
		int size = MapSize();
		int i;

		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			SlArray(buf, lengthof(buf), SLE_UINT8);
			for (j = 0; j != lengthof(buf); j++) _m[i++].type_height = buf[j];
		}
	}

	static void Save_MAPT()
	{
		int size = MapSize();
		int i;

		SlSetLength(size);
		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			for (j = 0; j != lengthof(buf); j++) buf[j] = _m[i++].type_height;
			SlArray(buf, lengthof(buf), SLE_UINT8);
		}
	}

	static void Load_MAP1()
	{
		int size = MapSize();
		int i;

		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			SlArray(buf, lengthof(buf), SLE_UINT8);
			for (j = 0; j != lengthof(buf); j++) _m[i++].m1 = buf[j];
		}
	}

	static void Save_MAP1()
	{
		int size = MapSize();
		int i;

		SlSetLength(size);
		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			for (j = 0; j != lengthof(buf); j++) buf[j] = _m[i++].m1;
			SlArray(buf, lengthof(buf), SLE_UINT8);
		}
	}

	static void Load_MAP2()
	{
		int size = MapSize();
		int i;

		for (i = 0; i != size;) {
			int buf[4096];
			int j;

			SlArray(buf, lengthof(buf),
				/* In those versions the m2 was 8 bits */
				CheckSavegameVersion(5) ? SLE_FILE_U8 | SLE_VAR_U16 : SLE_UINT16
			);
			for (j = 0; j != lengthof(buf); j++) _m[i++].m2 = buf[j];
		}
	}

	static void Save_MAP2()
	{
		int size = MapSize();
		int i;

		SlSetLength(size * sizeof(_m[0].m2));
		for (i = 0; i != size;) {
			int buf[4096];
			int j;

			for (j = 0; j != lengthof(buf); j++) buf[j] = _m[i++].m2;
			SlArray(buf, lengthof(buf), SLE_UINT16);
		}
	}

	static void Load_MAP3()
	{
		int size = MapSize();
		int i;

		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			SlArray(buf, lengthof(buf), SLE_UINT8);
			for (j = 0; j != lengthof(buf); j++) _m[i++].m3 = buf[j];
		}
	}

	static void Save_MAP3()
	{
		int size = MapSize();
		int i;

		SlSetLength(size);
		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			for (j = 0; j != lengthof(buf); j++) buf[j] = _m[i++].m3;
			SlArray(buf, lengthof(buf), SLE_UINT8);
		}
	}

	static void Load_MAP4()
	{
		int size = MapSize();
		int i;

		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			SlArray(buf, lengthof(buf), SLE_UINT8);
			for (j = 0; j != lengthof(buf); j++) _m[i++].m4 = buf[j];
		}
	}

	static void Save_MAP4()
	{
		int size = MapSize();
		int i;

		SlSetLength(size);
		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			for (j = 0; j != lengthof(buf); j++) buf[j] = _m[i++].m4;
			SlArray(buf, lengthof(buf), SLE_UINT8);
		}
	}

	static void Load_MAP5()
	{
		int size = MapSize();
		int i;

		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			SlArray(buf, lengthof(buf), SLE_UINT8);
			for (j = 0; j != lengthof(buf); j++) _m[i++].m5 = buf[j];
		}
	}

	static void Save_MAP5()
	{
		int size = MapSize();
		int i;

		SlSetLength(size);
		for (i = 0; i != size;) {
			byte buf[4096];
			int j;

			for (j = 0; j != lengthof(buf); j++) buf[j] = _m[i++].m5;
			SlArray(buf, lengthof(buf), SLE_UINT8);
		}
	}

	static void Load_MAPE()
	{
		int size = MapSize();
		int i;

		for (i = 0; i != size;) {
			byte buf[1024];
			int j;

			SlArray(buf, lengthof(buf), SLE_UINT8);
			for (j = 0; j != lengthof(buf); j++) {
				_m[i++].extra = BitOps.GB(buf[j], 0, 2);
				_m[i++].extra = BitOps.GB(buf[j], 2, 2);
				_m[i++].extra = BitOps.GB(buf[j], 4, 2);
				_m[i++].extra = BitOps.GB(buf[j], 6, 2);
			}
		}
	}

	static void Save_MAPE()
	{
		int size = MapSize();
		int i;

		SlSetLength(size / 4);
		for (i = 0; i != size;) {
			byte buf[1024];
			int j;

			for (j = 0; j != lengthof(buf); j++) {
				buf[j]  = _m[i++].extra << 0;
				buf[j] |= _m[i++].extra << 2;
				buf[j] |= _m[i++].extra << 4;
				buf[j] |= _m[i++].extra << 6;
			}
			SlArray(buf, lengthof(buf), SLE_UINT8);
		}
	}


	static void Save_CHTS()
	{
		byte count = sizeof(_cheats)/sizeof(Cheat);
		Cheat* cht = (Cheat*) &_cheats;
		Cheat* cht_last = &cht[count];

		SlSetLength(count*2);
		for(; cht != cht_last; cht++) {
			SlWriteByte(cht.been_used);
			SlWriteByte(cht.value);
		}
	}

	static void Load_CHTS()
	{
		Cheat* cht = (Cheat*) &_cheats;

		int count = SlGetFieldLength()/2;
		for(; count; count--, cht++)
		{
			cht.been_used = (byte)SlReadByte();
			cht.value = (byte)SlReadByte();
		}
	}


	final ChunkHandler _misc_chunk_handlers[] = {
		{ 'MAPS', Save_MAPS, Load_MAPS, CH_RIFF },
		{ 'MAPT', Save_MAPT, Load_MAPT, CH_RIFF },
		{ 'MAPO', Save_MAP1, Load_MAP1, CH_RIFF },
		{ 'MAP2', Save_MAP2, Load_MAP2, CH_RIFF },
		{ 'M3LO', Save_MAP3, Load_MAP3, CH_RIFF },
		{ 'M3HI', Save_MAP4, Load_MAP4, CH_RIFF },
		{ 'MAP5', Save_MAP5, Load_MAP5, CH_RIFF },
		{ 'MAPE', Save_MAPE, Load_MAPE, CH_RIFF },

		{ 'NAME', Save_NAME, Load_NAME, CH_ARRAY},
		{ 'DATE', SaveLoad_DATE, SaveLoad_DATE, CH_RIFF},
		{ 'VIEW', SaveLoad_VIEW, SaveLoad_VIEW, CH_RIFF},
		{ 'OPTS', SaveLoad_OPTS, SaveLoad_OPTS, CH_RIFF},
		{ 'CHTS', Save_CHTS, Load_CHTS, CH_RIFF | CH_LAST}
	};
	
	
}

package com.dzavalishin.game;

import java.util.Arrays;
import java.util.function.Consumer;

import com.dzavalishin.tables.MiscTables;

public class Misc extends MiscTables 
{

	static final int NUM_CARGO = AcceptedCargo.NUM_CARGO;



	// Calculate constants that depend on the landscape type.
	static void InitializeLandscapeVariables(boolean only_constants)
	{
		final LandscapePredefVar lpd;
		int i;
		//StringID 
		int str;

		lpd = _landscape_predef_var[GameOptions._opt.landscape];

		Global._cargoc.ai_railwagon = Arrays.copyOf( lpd.railwagon_by_cargo, lpd.railwagon_by_cargo.length);
		Global._cargoc.ai_roadveh_start = Arrays.copyOf( lpd.road_veh_by_cargo_start, lpd.road_veh_by_cargo_start.length );
		Global._cargoc.ai_roadveh_count = Arrays.copyOf( lpd.road_veh_by_cargo_count, lpd.road_veh_by_cargo_count.length );
		
		for(i=0; i!=NUM_CARGO; i++) {
			Global._cargoc.sprites[i] = lpd.sprites[i];

			str = lpd.names[i];
			Global._cargoc.names_s[i] = str;
			Global._cargoc.names_long[i] = (str += 0x40);
			Global._cargoc.names_short[i] = (str += 0x20);
			Global._cargoc.weights[i] = lpd.weights[i];

			if (!only_constants) {
				Global._cargo_payment_rates[i] = lpd.initial_cargo_payment[i];
				//Global._cargo_payment_rates_frac[i] = 0;
			}

			Global._cargoc.transit_days_1[i] = lpd.transit_days_table_1[i];
			Global._cargoc.transit_days_2[i] = lpd.transit_days_table_2[i];
		}
	}

	static void OnNewDay_EffectVehicle(Vehicle v) { /* empty */ }


	static final OnNewVehicleDayProc[] _on_new_vehicle_day_proc = {
		TrainCmd::OnNewDay_Train,
		RoadVehCmd::OnNewDay_RoadVeh,
		Ship::OnNewDay_Ship,
		AirCraft::OnNewDay_Aircraft,
		Misc::OnNewDay_EffectVehicle,
		DisasterCmd::OnNewDay_DisasterVehicle,
	};



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
	public static void RunVehicleDayProc(int daytick)
	{
		int total = Global.gs._vehicles.total_items();

		for (int i = daytick; i < total; i += Global.DAY_TICKS) {
			Vehicle v = Vehicle.GetVehicle(i);
			if (v.type != 0)
				_on_new_vehicle_day_proc[v.type - 0x10].accept(v);
		}
		
		//Global.gs._vehicle_pool.forEachValid( (v) -> _on_new_vehicle_day_proc[v.type - 0x10].accept(v) );
	}


	//!We're writing an own sort algorithm here, as
	//!qsort isn't stable
	//!Since the number of elements will be low, a
	//!simple bubble sort will have to do :)
	/*
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
	*/
	
	/*
	
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
	// currently some unrelated stuff is just put here
	static void SaveLoad_DATE()
	{
		SlGlobList(_date_desc);
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
				// In those versions the m2 was 8 bits 
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
	*/
	
}


//typedef void OnNewVehicleDayProc(Vehicle v);

@FunctionalInterface
interface OnNewVehicleDayProc extends Consumer<Vehicle> {}



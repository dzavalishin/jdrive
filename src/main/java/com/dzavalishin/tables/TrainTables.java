package com.dzavalishin.tables;

import com.dzavalishin.game.Engine;
import com.dzavalishin.struct.RailtypeSlowdownParams;

public class TrainTables 
{

	public static final int CALLBACK_FAILED = Engine.CALLBACK_FAILED;


	// This enum lists the implemented callbacks
	// Use as argument for the GetCallBackResult function (see comments there)
	//enum CallbackID {
	// Powered wagons; if the result is lower as 0x40 then the wagon is powered
	// TODO: interpret the rest of the result; aka "visual effects"
	public static final int CBID_WAGON_POWER = 0x10;

	// Vehicle length; returns the amount of 1/8's the vehicle is shorter
	// only for train vehicles
	public static final int CBID_VEH_LENGTH = 0x11;

	// Refit capacity; the passed vehicle needs to have its ->cargo_type set to
	// the cargo we are refitting to; returns the new cargo capacity
	public static final int CBID_REFIT_CAP = 0x15;

	public static final int CBID_ARTIC_ENGINE = 0x16;
	//};

	// bit positions for rvi->callbackmask; indicates which callbacks are used by an engine
	// (some callbacks are always used; and dont appear here)
	//enum CallbackMask {
	public static final int CBM_WAGON_POWER = 0;
	public static final int CBM_VEH_LENGTH = 1;
	public static final int CBM_REFIT_CAP = 3;
	public static final int CBM_ARTIC_ENGINE = 4;
	//};


	public static final int RAILTYPE_RAIL   = 0;
	public static final int RAILTYPE_MONO   = 1;
	public static final int RAILTYPE_MAGLEV = 2;
	public static final int RAILTYPE_END = 3;
	public static final int RAILTYPE_MASK   =  0x3;
	public static final int INVALID_RAILTYPE = 0xFF;



	protected static final byte _vehicle_initial_x_fract[] = {10,8,4,8};
	protected static final byte _vehicle_initial_y_fract[] = {8,4,8,10};
	protected static final byte _state_dir_table[] = { 0x20, 8, 0x10, 4 };


	/* These two arrays are used for realistic acceleration. XXX: How should they
	 * be interpreted? */
	public static final byte _curve_neighbours45[][] = {
			{7, 1},
			{0, 2},
			{1, 3},
			{2, 4},
			{3, 5},
			{4, 6},
			{5, 7},
			{6, 0},
	};

	public static final byte _curve_neighbours90[][] = {
			{6, 2},
			{7, 3},
			{0, 4},
			{1, 5},
			{2, 6},
			{3, 7},
			{4, 0},
			{5, 1},
	};


	public static final byte _railveh_score[] = {
			1, 4, 7, 19, 20, 30, 31, 19,
			20, 21, 22, 10, 11, 30, 31, 32,
			33, 34, 35, 29, 45, 32, 50, 40,
			41, 51, 52, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 60, 62,
			63, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 70, 71, 72, 73,
			74, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0,
	};


	//#define MKIT(a,b,c,d) ((a&0xFF)<<24) | ((b&0xFF)<<16) | ((c&0xFF)<<8) | ((d&0xFF)<<0)
	private static int MKIT(int a,int b,int c,int d) { return ((a&0xFF)<<24) | ((b&0xFF)<<16) | ((c&0xFF)<<8) | ((d&0xFF)<<0); }
	public static final int _delta_xy_table[] = {
			MKIT(3, 3, -1, -1),
			MKIT(3, 7, -1, -3),
			MKIT(3, 3, -1, -1),
			MKIT(7, 3, -3, -1),
			MKIT(3, 3, -1, -1),
			MKIT(3, 7, -1, -3),
			MKIT(3, 3, -1, -1),
			MKIT(7, 3, -3, -1),
	};

	public static final byte _initial_tile_subcoord[][][] = {
			{{ 15, 8, 1 },{ 0, 0, 0 },{ 0, 8, 5 },{ 0, 0, 0 }},
			{{  0, 0, 0 },{ 8, 0, 3 },{ 0, 0, 0 },{ 8,15, 7 }},
			{{  0, 0, 0 },{ 7, 0, 2 },{ 0, 7, 6 },{ 0, 0, 0 }},
			{{ 15, 8, 2 },{ 0, 0, 0 },{ 0, 0, 0 },{ 8,15, 6 }},
			{{ 15, 7, 0 },{ 8, 0, 4 },{ 0, 0, 0 },{ 0, 0, 0 }},
			{{  0, 0, 0 },{ 0, 0, 0 },{ 0, 8, 4 },{ 7,15, 0 }},
	};

	public static final int _vehicle_smoke_pos[] = {
			1, 1, 1, 0, -1, -1, -1, 0
	};

	public static final int _reachable_tracks[] = {
			0x10091009,
			0x00160016,
			0x05200520,
			0x2A002A00,
	};

	public static final byte _search_directions[][] = {
			{ 0, 9, 2, 9 }, // track 1
			{ 9, 1, 9, 3 }, // track 2
			{ 9, 0, 3, 9 }, // track upper
			{ 1, 9, 9, 2 }, // track lower
			{ 3, 2, 9, 9 }, // track left
			{ 9, 9, 1, 0 }, // track right
	};

	public static final byte _pick_track_table[] = {1, 3, 2, 2, 0, 0};

	public static final byte _new_vehicle_direction_table[] = {
			0, 7, 6, 0,
			1, 0, 5, 0,
			2, 3, 4,
	};


	/*
	static class RailtypeSlowdownParams 
	{
		int small_turn; 
		int large_turn;
		int z_up; // fraction to remove when moving up
		int z_down; // fraction to remove when moving down

		public RailtypeSlowdownParams(int i, int j, int k, int l) {
			small_turn = i;
			large_turn = j;
			z_up = k;
			z_down = l;
		}
	}*/

	protected static final RailtypeSlowdownParams _railtype_slowdown[] = {
			// normal accel
			new RailtypeSlowdownParams(256/4, 256/2, 256/4, 2), // normal
			new RailtypeSlowdownParams(256/4, 256/2, 256/4, 2), // monorail
			new RailtypeSlowdownParams(0,     256/2, 256/4, 2), // maglev
	};


	public static final int _breakdown_speeds[] = {
			225, 210, 195, 180, 165, 150, 135, 120, 105, 90, 75, 60, 45, 30, 15, 15
	};

	public static final byte _depot_track_ind[] = {0,1,0,1};

	public static final byte _otherside_signal_directions[] = {
			1, 3, 1, 3, 5, 3, 0, 0,
			5, 7, 7, 5, 7, 1,
	};


	public static final byte[] _matching_tracks = {0x30, 1, 0xC, 2, 0x30, 1, 0xC, 2};


	/*
	static final SoundFx sfx[] = {
			SND_04_TRAIN,
			SND_0A_TRAIN_HORN,
			SND_0A_TRAIN_HORN
		}; */


	public static class VehicleAtSignalData {
		//TileIndex tile;
		public int tile;
		public int direction;
	} 




	protected static final /*SpriteID*/ int _engine_sprite_base[] = {
			0x0B59, 0x0B61, 0x0B69, 0x0BE1, 0x0B71, 0x0B75, 0x0B7D, 0x0B7D,
			0x0B85, 0x0B85, 0x0B8D, 0x0B8D, 0x0BC9, 0x0BD1, 0x0BD9, 0x0BE9,
			0x0BED, 0x0BED, 0x0BF5, 0x0BF9, 0x0B79, 0x0B9D, 0x0B9D, 0x0B95,
			0x0B95, 0x0BA5, 0x0BA9, 0x0BA9, 0x0BC1, 0x0BC5, 0x0BB1, 0x0BB9,
			0x0BB9, 0x0AAD, 0x0AB1, 0x0AB5, 0x0AB9, 0x0ABD, 0x0AC1, 0x0AC9,
			0x0ACD, 0x0AD5, 0x0AD1, 0x0AD9, 0x0AC5, 0x0AD1, 0x0AD5, 0x0AF9,
			0x0AFD, 0x0B05, 0x0AB9, 0x0AC1, 0x0AC9, 0x0AD1, 0x0AD9, 0x0AE1,
			0x0AE5, 0x0AE9, 0x0AF1, 0x0AF9, 0x0B0D, 0x0B11, 0x0B15, 0x0B19,
			0x0B1D, 0x0B21, 0x0B29, 0x0B2D, 0x0B35, 0x0B31, 0x0B39, 0x0B25,
			0x0B31, 0x0B35,
	};

	/* For how many directions do we have sprites? (8 or 4; if 4, the other 4
	 * directions are symmetric. */
	protected static final byte _engine_sprite_and[] = {
			7, 7, 7, 7, 3, 3, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 3,
			7, 7, 3, 7, 3, 7, 7, 7,
			7, 3, 7, 7, 3, 3, 7, 7,
			7, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3,
			3, 3,
	};

	/* Non-zero for multihead trains. */
	protected static final byte _engine_sprite_add[] = {
			0, 0, 0, 0, 0, 0, 0, 4,
			0, 4, 0, 4, 0, 0, 0, 0,
			0, 4, 0, 0, 0, 0, 4, 0,
			4, 0, 0, 4, 0, 0, 0, 0,
			4, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0,
	};


	protected static final byte _wagon_full_adder[] = {
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 44, 0, 0, 0, 0, 24,
			24, 24, 24, 0, 0, 32, 32, 0,
			4, 4, 4, 4, 4, 4, 4, 0,
			0, 4, 4, 4, 0, 44, 0, 0,
			0, 0, 24, 24, 24, 24, 0, 0,
			32, 32
	};




}




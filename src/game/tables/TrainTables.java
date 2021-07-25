package game.tables;

public class TrainTables 
{

	public static final int RAILTYPE_RAIL   = 0;
	public static final int RAILTYPE_MONO   = 1;
	public static final int RAILTYPE_MAGLEV = 2;
	public static final int RAILTYPE_END = 3;
	public static final int RAILTYPE_MASK   =  0x3;
	public static final int INVALID_RAILTYPE = 0xFF;

	
	
	static final byte _vehicle_initial_x_fract[] = {10,8,4,8};
	static final byte _vehicle_initial_y_fract[] = {8,4,8,10};
	static final byte _state_dir_table[] = { 0x20, 8, 0x10, 4 };
	

	/* These two arrays are used for realistic acceleration. XXX: How should they
	 * be interpreted? */
	static final byte _curve_neighbours45[][] = {
		{7, 1},
		{0, 2},
		{1, 3},
		{2, 4},
		{3, 5},
		{4, 6},
		{5, 7},
		{6, 0},
	};

	static final byte _curve_neighbours90[][] = {
		{6, 2},
		{7, 3},
		{0, 4},
		{1, 5},
		{2, 6},
		{3, 7},
		{4, 0},
		{5, 1},
	};

	
	static final byte _railveh_score[] = {
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
	static final int _delta_xy_table[] = {
		MKIT(3, 3, -1, -1),
		MKIT(3, 7, -1, -3),
		MKIT(3, 3, -1, -1),
		MKIT(7, 3, -3, -1),
		MKIT(3, 3, -1, -1),
		MKIT(3, 7, -1, -3),
		MKIT(3, 3, -1, -1),
		MKIT(7, 3, -3, -1),
	};

	static final byte _initial_tile_subcoord[][][] = {
	{{ 15, 8, 1 },{ 0, 0, 0 },{ 0, 8, 5 },{ 0, 0, 0 }},
	{{  0, 0, 0 },{ 8, 0, 3 },{ 0, 0, 0 },{ 8,15, 7 }},
	{{  0, 0, 0 },{ 7, 0, 2 },{ 0, 7, 6 },{ 0, 0, 0 }},
	{{ 15, 8, 2 },{ 0, 0, 0 },{ 0, 0, 0 },{ 8,15, 6 }},
	{{ 15, 7, 0 },{ 8, 0, 4 },{ 0, 0, 0 },{ 0, 0, 0 }},
	{{  0, 0, 0 },{ 0, 0, 0 },{ 0, 8, 4 },{ 7,15, 0 }},
	};

	static final int _vehicle_smoke_pos[] = {
			1, 1, 1, 0, -1, -1, -1, 0
		};

	static final int _reachable_tracks[] = {
		0x10091009,
		0x00160016,
		0x05200520,
		0x2A002A00,
	};

	static final byte _search_directions[][] = {
		{ 0, 9, 2, 9 }, // track 1
		{ 9, 1, 9, 3 }, // track 2
		{ 9, 0, 3, 9 }, // track upper
		{ 1, 9, 9, 2 }, // track lower
		{ 3, 2, 9, 9 }, // track left
		{ 9, 9, 1, 0 }, // track right
	};

	static final byte _pick_track_table[] = {1, 3, 2, 2, 0, 0};

	static final byte _new_vehicle_direction_table[] = {
			0, 7, 6, 0,
			1, 0, 5, 0,
			2, 3, 4,
		};
	

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
	}

	static final RailtypeSlowdownParams _railtype_slowdown[] = {
		// normal accel
		new RailtypeSlowdownParams(256/4, 256/2, 256/4, 2), // normal
		new RailtypeSlowdownParams(256/4, 256/2, 256/4, 2), // monorail
		new RailtypeSlowdownParams(0,     256/2, 256/4, 2), // maglev
	}
	
	
	static final int _breakdown_speeds[] = {
			225, 210, 195, 180, 165, 150, 135, 120, 105, 90, 75, 60, 45, 30, 15, 15
		};

	static final byte _depot_track_ind[] = {0,1,0,1};

	static final byte _otherside_signal_directions[] = {
			1, 3, 1, 3, 5, 3, 0, 0,
			5, 7, 7, 5, 7, 1,
		};

	
	static byte _matching_tracks[] = {0x30, 1, 0xC, 2, 0x30, 1, 0xC, 2};

	
	/*
	static final SoundFx sfx[] = {
			SND_04_TRAIN,
			SND_0A_TRAIN_HORN,
			SND_0A_TRAIN_HORN
		}; */
	
	
	protected static class VehicleAtSignalData {
		//TileIndex tile;
		int tile;
		int direction;
	} 

	
}




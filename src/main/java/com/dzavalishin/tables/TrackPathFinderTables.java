package com.dzavalishin.tables;

import com.dzavalishin.game.Rail;

public class TrackPathFinderTables 
{

	public static final byte _bits_mask[] = {
			0x19,
			0x16,
			0x25,
			0x2A,
	};

	public static final byte _tpf_new_direction[] = {
			0,1,0,1,2,1, 0,0,
			2,3,3,2,3,0,
	};

	public static final byte _tpf_prev_direction[] = {
			0,1,1,0,1,2, 0,0,
			2,3,2,3,0,3,
	};


	public static final byte _otherdir_mask[] = {
			0x10,
			0,
			0x5,
			0x2A,
	};

	
	public static final int _tpfmode1_and[] = { 0x1009, 0x16, 0x520, 0x2A00 };
	
	// TODO Why here?! _ffb_64
	public static final byte _ffb_64[] = {
	0,0,1,0,2,0,1,0,
	3,0,1,0,2,0,1,0,
	4,0,1,0,2,0,1,0,
	3,0,1,0,2,0,1,0,
	5,0,1,0,2,0,1,0,
	3,0,1,0,2,0,1,0,
	4,0,1,0,2,0,1,0,
	3,0,1,0,2,0,1,0,

	0,0,0,2,0,4,4,6,
	0,8,8,10,8,12,12,14,
	0,16,16,18,16,20,20,22,
	16,24,24,26,24,28,28,30,
	0,32,32,34,32,36,36,38,
	32,40,40,42,40,44,44,46,
	32,48,48,50,48,52,52,54,
	48,56,56,58,56,60,60,62,
	};
	

	static {        //noinspection ConstantConditions
        assert 128 == _ffb_64.length; }

	
	public static final int _new_track[][] = {
	{0,0xff,8,0xff,},
	{0xff,1,0xff,9,},
	{0xff,2,10,0xff,},
	{3,0xff,0xff,11,},
	{12,4,0xff,0xff,},
	{0xff,0xff,5,13,},
	};

	
	protected static final int _is_upwards_slope[] = {
			0, // no tileh
			(1 << Rail.TRACKDIR_DIAG1_SW) | (1 << Rail.TRACKDIR_DIAG2_NW), // 1
			(1 << Rail.TRACKDIR_DIAG1_SW) | (1 << Rail.TRACKDIR_DIAG2_SE), // 2
			(1 << Rail.TRACKDIR_DIAG1_SW), // 3
			(1 << Rail.TRACKDIR_DIAG1_NE) | (1 << Rail.TRACKDIR_DIAG2_SE), // 4
			0, // 5
			(1 << Rail.TRACKDIR_DIAG2_SE), // 6
			0, // 7
			(1 << Rail.TRACKDIR_DIAG1_NE) | (1 << Rail.TRACKDIR_DIAG2_NW), // 8,
			(1 << Rail.TRACKDIR_DIAG2_NW), // 9
			0, //10
			0, //11,
			(1 << Rail.TRACKDIR_DIAG1_NE), //12
			0, //13
			0, //14
		};
	
	
	
	
	public static final int DIAG_FACTOR = 3;
	public static final int STR_FACTOR  = 2;
	
	// These has to be small cause the max length of a track
	// is currently limited to 16384

	protected static final byte _length_of_track[] = {
		DIAG_FACTOR,DIAG_FACTOR,STR_FACTOR,STR_FACTOR,STR_FACTOR,STR_FACTOR,0,0,
		DIAG_FACTOR,DIAG_FACTOR,STR_FACTOR,STR_FACTOR,STR_FACTOR,STR_FACTOR,0,0
	};
	
	
	public static final int _get_tunlen_inc[] = { -16, 0, 16, 0, -16 };
	
	
}

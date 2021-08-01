package game.tables;

public class RoadVehCmdTables 
{


	public static final int _roadveh_images[] = {
			0xCD4, 0xCDC, 0xCE4, 0xCEC, 0xCF4, 0xCFC, 0xD0C, 0xD14,
			0xD24, 0xD1C, 0xD2C, 0xD04, 0xD1C, 0xD24, 0xD6C, 0xD74,
			0xD7C, 0xC14, 0xC1C, 0xC24, 0xC2C, 0xC34, 0xC3C, 0xC4C,
			0xC54, 0xC64, 0xC5C, 0xC6C, 0xC44, 0xC5C, 0xC64, 0xCAC,
			0xCB4, 0xCBC, 0xD94, 0xD9C, 0xDA4, 0xDAC, 0xDB4, 0xDBC,
			0xDCC, 0xDD4, 0xDE4, 0xDDC, 0xDEC, 0xDC4, 0xDDC, 0xDE4,
			0xE2C, 0xE34, 0xE3C, 0xC14, 0xC1C, 0xC2C, 0xC3C, 0xC4C,
			0xC5C, 0xC64, 0xC6C, 0xC74, 0xC84, 0xC94, 0xCA4
	};

	public static final int _roadveh_full_adder[] = {
			0,  88,   0,   0,   0,   0,  48,  48,
			48,  48,   0,   0,  64,  64,   0,  16,
			16,   0,  88,   0,   0,   0,   0,  48,
			48,  48,  48,   0,   0,  64,  64,   0,
			16,  16,   0,  88,   0,   0,   0,   0,
			48,  48,  48,  48,   0,   0,  64,  64,
			0,  16,  16,   0,   8,   8,   8,   8,
			0,   0,   0,   8,   8,   8,   8
	};


	public static final int _road_veh_fp_ax_or[] = {
			0x100,0x200,1,2,
	};

	public static final int _road_veh_fp_ax_and[] = {
			0x1009, 0x16, 0x520, 0x2A00
	};

	public static final byte _road_reverse_table[] = {
			6, 7, 14, 15
	};

	public static final int _road_pf_table_3[] = {
			0x910, 0x1600, 0x2005, 0x2A
	};

	public static final int _road_pf_directions[] = {
			0, 1, 0, 1, 2, 1, 255, 255,
			2, 3, 3, 2, 3, 0, 255, 255,
	};



	//#define MKIT(a,b,c,d) ((a&0xFF)<<24) | ((b&0xFF)<<16) | ((c&0xFF)<<8) | ((d&0xFF)<<0)
	private static int MKIT(int a, int b, int c, int d) { return ((a&0xFF)<<24) | ((b&0xFF)<<16) | ((c&0xFF)<<8) | ((d&0xFF)<<0); }

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


	public static final byte _road_veh_data_1[] = {
			20, 20, 16, 16, 0, 0, 0, 0,
			19, 19, 15, 15, 0, 0, 0, 0,
			16, 16, 12, 12, 0, 0, 0, 0,
			15, 15, 11, 11
	};

	public static final byte _roadveh_data_2[] = { 0,1,8,9 };

	public static final short _dists[] = {
			-4, -8, -4, -1, 4, 8, 4, 1,
			-4, -1, 4, 8, 4, 1, -4, -8,
		};

	public static final byte _roadveh_new_dir[] = {
			0, 7, 6, 0,
			1, 0, 5, 0,
			2, 3, 4
		};

	
	static final int _turn_prob[] = { -1, 0, 0, 1 };
	
}

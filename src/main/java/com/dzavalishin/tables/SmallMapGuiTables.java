package com.dzavalishin.tables;

import com.dzavalishin.game.Str;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Pixel;

public class SmallMapGuiTables 
{

	//#define MK(a,b) a, b
	//#define MKEND() 0xFFFF
	//#define 0x100|a,b) (a | 0x100), b

	/* Legend text giving the colours to look for on the minimap */
	public static final int _legend_land_contours[] = {
			0x5A, Str.STR_00F0_100M,
			0x5C,Str.STR_00F1_200M,
			0x5E,Str.STR_00F2_300M,
			0x1F,Str.STR_00F3_400M,
			0x27,Str.STR_00F4_500M,

			0x100|0xD7,Str.STR_00EB_ROADS,
			0x0A,Str.STR_00EC_RAILROADS,
			0x98,Str.STR_00ED_STATIONS_AIRPORTS_DOCKS,
			0xB5,Str.STR_00EE_BUILDINGS_INDUSTRIES,
			0x0F,Str.STR_00EF_VEHICLES,
			0xFFFF // MKEND()
	};

	public static final int _legend_vehicles[] = {
			0xB8,Str.STR_00F5_TRAINS,
			0xBF,Str.STR_00F6_ROAD_VEHICLES,
			0x98,Str.STR_00F7_SHIPS,
			0x0F,Str.STR_00F8_AIRCRAFT,
			0x100|0xD7,Str.STR_00F9_TRANSPORT_ROUTES,
			0xB5,Str.STR_00EE_BUILDINGS_INDUSTRIES,
			0xFFFF // MKEND()
	};

	public static final int _legend_industries_normal[] = {
			0xD7,Str.STR_00FA_COAL_MINE,
			0xB8,Str.STR_00FB_POWER_STATION,
			0x56,Str.STR_00FC_FOREST,
			0xC2,Str.STR_00FD_SAWMILL,
			0xBF,Str.STR_00FE_OIL_REFINERY,
			0x0F,Str.STR_0105_BANK,

			0x100|0x30,Str.STR_00FF_FARM,
			0xAE,Str.STR_0100_FACTORY,
			0x98,Str.STR_0102_OIL_WELLS,
			0x37,Str.STR_0103_IRON_ORE_MINE,
			0x0A,Str.STR_0104_STEEL_MILL,
			0xFFFF // MKEND()
	};

	public static final int _legend_industries_hilly[] = {
			0xD7,Str.STR_00FA_COAL_MINE,
			0xB8,Str.STR_00FB_POWER_STATION,
			0x56,Str.STR_00FC_FOREST,
			0x0A,Str.STR_0106_PAPER_MILL,
			0xBF,Str.STR_00FE_OIL_REFINERY,
			0x37,Str.STR_0108_FOOD_PROCESSING_PLANT,
			0x100|0x30,Str.STR_00FF_FARM,

			0xAE,Str.STR_0101_PRINTING_WORKS,
			0x98,Str.STR_0102_OIL_WELLS,
			0xC2,Str.STR_0107_GOLD_MINE,
			0x0F,Str.STR_0105_BANK,
			0xFFFF // MKEND()
	};

	public static final int _legend_industries_desert[] = {
			0xBF,Str.STR_00FE_OIL_REFINERY,
			0x98,Str.STR_0102_OIL_WELLS,
			0x0F,Str.STR_0105_BANK,
			0xB8,Str.STR_0109_DIAMOND_MINE,
			0x37,Str.STR_0108_FOOD_PROCESSING_PLANT,
			0x0A,Str.STR_010A_COPPER_ORE_MINE,
			0x30,Str.STR_00FF_FARM,
			0x100|0x56,Str.STR_010B_FRUIT_PLANTATION,

			0x27,Str.STR_010C_RUBBER_PLANTATION,
			0x25,Str.STR_010D_WATER_SUPPLY,
			0xD0,Str.STR_010E_WATER_TOWER,
			0xAE,Str.STR_0100_FACTORY,
			0xC2,Str.STR_010F_LUMBER_MILL,
			0xFFFF // MKEND()
	};

	public static final int _legend_industries_candy[] = {
			0x30,Str.STR_0110_COTTON_CANDY_FOREST,
			0xAE,Str.STR_0111_CANDY_FACTORY,
			0x27,Str.STR_0112_BATTERY_FARM,
			0x37,Str.STR_0113_COLA_WELLS,
			0xD0,Str.STR_0114_TOY_SHOP,
			0x0A,Str.STR_0115_TOY_FACTORY,
			0x100|0x25,Str.STR_0116_PLASTIC_FOUNTAINS,

			0xB8,Str.STR_0117_FIZZY_DRINK_FACTORY,
			0x98,Str.STR_0118_BUBBLE_GENERATOR,
			0xC2,Str.STR_0119_TOFFEE_QUARRY,
			0x0F,Str.STR_011A_SUGAR_MINE,
			0xFFFF // MKEND()
	};

	public static final int _legend_routes[] = {
			0xD7,Str.STR_00EB_ROADS,
			0x0A,Str.STR_00EC_RAILROADS,
			0xB5,Str.STR_00EE_BUILDINGS_INDUSTRIES,
			0x100|0x56,Str.STR_011B_RAILROAD_STATION,

			0xC2,Str.STR_011C_TRUCK_LOADING_BAY,
			0xBF,Str.STR_011D_BUS_STATION,
			0xB8,Str.STR_011E_AIRPORT_HELIPORT,
			0x98,Str.STR_011F_DOCK,
			0xFFFF // MKEND()
	};

	public static final int _legend_vegetation[] = {
			0x52,Str.STR_0120_ROUGH_LAND,
			0x54,Str.STR_0121_GRASS_LAND,
			0x37,Str.STR_0122_BARE_LAND,
			0x25,Str.STR_0123_FIELDS,
			0x57,Str.STR_0124_TREES,
			0xD0,Str.STR_00FC_FOREST,
			0x100|0x0A,Str.STR_0125_ROCKS,

			0xC2,Str.STR_012A_DESERT,
			0x98,Str.STR_012B_SNOW,
			0xD7,Str.STR_00F9_TRANSPORT_ROUTES,
			0xB5,Str.STR_00EE_BUILDINGS_INDUSTRIES,
			0xFFFF // MKEND()
	};

	public static final int _legend_land_owners[] = {
			0xCA,Str.STR_0126_WATER,
			0x54,Str.STR_0127_NO_OWNER,
			0xB4,Str.STR_0128_TOWNS,
			0x20,Str.STR_0129_INDUSTRIES,
			0xFFFF // MKEND()
	};








	//enum { IND_OFFS = 6 };
	public static final int IND_OFFS = 6;


	protected static final int  _legend_table[][] = {
			_legend_land_contours,
			_legend_vehicles,
			null,
			_legend_routes,
			_legend_vegetation,
			_legend_land_owners,

			_legend_industries_normal,
			_legend_industries_hilly,
			_legend_industries_desert,
			_legend_industries_candy
	};




	/**
	 * @deprecated
	 * @param d
	 * @param val
	 */
	//#if defined(TTD_ALIGNMENT_4)
	@Deprecated 
	static  void WRITE_PIXELS(Pixel d, int val)
	{
		/*#	if defined(TTD_BIG_ENDIAN)
			d[0] = BitOps.GB(val, 24, 8);
			d[1] = BitOps.GB(val, 16, 8);
			d[2] = BitOps.GB(val,  8, 8);
			d[3] = BitOps.GB(val,  0, 8);
	#	elif defined(TTD_LITTLE_ENDIAN) */
		d.w(0, (byte) BitOps.GB(val,  0, 8) );
		d.w(1, (byte) BitOps.GB(val,  8, 8) );
		d.w(2, (byte) BitOps.GB(val, 16, 8) );
		d.w(3, (byte) BitOps.GB(val, 24, 8) );
		//#	endif
	}

	/** need to use OR, otherwise we will overwrite the wrong pixels at the edges :( 
	 * 
	 * @deprecated
	 * 
	 * */
	@Deprecated
	static  void WRITE_PIXELS_OR(Pixel d, int val)
	{
		/*#	if defined(TTD_BIG_ENDIAN)
			d[0] |= BitOps.GB(val, 24, 8);
			d[1] |= BitOps.GB(val, 16, 8);
			d[2] |= BitOps.GB(val,  8, 8);
			d[3] |= BitOps.GB(val,  0, 8);
	#	elif defined(TTD_LITTLE_ENDIAN) */
		d.wor(0, BitOps.GB(val,  0, 8) );
		d.wor(1, BitOps.GB(val,  8, 8) );
		d.wor(2, BitOps.GB(val, 16, 8) );
		d.wor(3, BitOps.GB(val, 24, 8) );
		//#	endif
	}
	/*		
	#else
	#	define WRITE_PIXELS(dst, val)		*(int*)(dst) = (val);
	#	define WRITE_PIXELS_OR(dst,val)	*(int*)(dst) |= (val);
	#endif
	 */
	/*
	#if defined(TTD_BIG_ENDIAN)
	#	define MKCOLOR(x) BSWAP32(x)
	#elif defined(TTD_LITTLE_ENDIAN)
	#	define MKCOLOR(x) (x)
	#endif
	 */
	protected static int MKCOLOR(int x) { return (x); }

	/* Height encodings; 16 levels XXX - needs updating for more/finer heights! */
	protected static final int _map_height_bits[] = {
			MKCOLOR(0x5A5A5A5A),
			MKCOLOR(0x5A5B5A5B),
			MKCOLOR(0x5B5B5B5B),
			MKCOLOR(0x5B5C5B5C),
			MKCOLOR(0x5C5C5C5C),
			MKCOLOR(0x5C5D5C5D),
			MKCOLOR(0x5D5D5D5D),
			MKCOLOR(0x5D5E5D5E),
			MKCOLOR(0x5E5E5E5E),
			MKCOLOR(0x5E5F5E5F),
			MKCOLOR(0x5F5F5F5F),
			MKCOLOR(0x5F1F5F1F),
			MKCOLOR(0x1F1F1F1F),
			MKCOLOR(0x1F271F27),
			MKCOLOR(0x27272727),
			MKCOLOR(0x27272727),
	};






	public static class AndOr {
		public final int mor;
		public final int mand;

		public AndOr(int o, int a) {
			mor = o;
			mand = a;
		}
	}


	public static final AndOr _smallmap_contours_andor[] = {
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x000A0A00),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00B5B500),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x98989898),MKCOLOR(0x00000000)),
			new AndOr(MKCOLOR(0xCACACACA),MKCOLOR(0x00000000)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0xB5B5B5B5),MKCOLOR(0x00000000)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x00B5B500),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x000A0A00),MKCOLOR(0xFF0000FF)),
	};

	protected static final AndOr _smallmap_vehicles_andor[] = {
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00B5B500),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0xCACACACA),MKCOLOR(0x00000000)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0xB5B5B5B5),MKCOLOR(0x00000000)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x00B5B500),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
	};

	public static final AndOr _smallmap_vegetation_andor[] = {
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00B5B500),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00575700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0xCACACACA),MKCOLOR(0x00000000)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0xB5B5B5B5),MKCOLOR(0x00000000)),
			new AndOr(MKCOLOR(0x00000000),MKCOLOR(0xFFFFFFFF)),
			new AndOr(MKCOLOR(0x00B5B500),MKCOLOR(0xFF0000FF)),
			new AndOr(MKCOLOR(0x00D7D700),MKCOLOR(0xFF0000FF)),
	};


	public static final int _vehicle_type_colors[] = {
			184, 191, 152, 15, 215, 184
	};


	/* Industry colours... a total of 175 gfx - XXX - increase if more industries */
	protected static final int _industry_smallmap_colors[] = {
			215,215,215,215,215,215,215,184,
			184,184,184,194,194,194,194,194,
			86, 86,191,191,191,191,191,191,
			152,152,152,152,152,152,152,152,
			152, 48, 48, 48, 48, 48, 48,174,
			174,174,174,174,174,174,174, 10,
			10, 10, 10, 10, 10, 10, 10, 10,
			10, 10, 15, 15, 55, 55, 55, 55,
			10, 10, 10, 10, 10, 10, 10, 10,
			194,194,194,194,194,194,194,194,
			194,194,194,194,194,194,194,194,
			194, 15, 15,184,184,184,184,184,
			184,184,184,184, 55, 55, 55, 55,
			55, 55, 55, 55, 55, 55, 55, 55,
			55, 55, 55, 55, 86, 39, 37, 37,
			208,174,174,174,174,194,194,194,
			194, 48, 48,174,174,174,174, 39,
			39, 55,208,208,208,208, 10, 10,
			10, 10, 10, 10, 37, 37, 37, 37,
			37, 37, 37, 37,184,184,184,184,
			152,152,152,152,194,194,194, 15,
			15, 15, 15, 15, 15, 15, 15,
	};


	public static final int _vegetation_clear_bits[] = {
			MKCOLOR(0x37373737), ///< bare land
			MKCOLOR(0x37373737), ///< 1/3 grass
			MKCOLOR(0x37373737), ///< 2/3 grass
			MKCOLOR(0x54545454), ///< full grass

			MKCOLOR(0x52525252), ///< rough land
			MKCOLOR(0x0A0A0A0A), ///< rocks
			MKCOLOR(0x25252525), ///< fields
			MKCOLOR(0x98989898), ///< snow
			MKCOLOR(0xC2C2C2C2), ///< desert
			MKCOLOR(0x54545454), ///< unused
			MKCOLOR(0x54545454), ///< unused
	};



	public static final int _smallmap_mask_left[] = {
			MKCOLOR(0xFF000000),
			MKCOLOR(0xFFFF0000),
			MKCOLOR(0xFFFFFF00),
	};

	public static final int _smallmap_mask_right[] = {
			MKCOLOR(0x000000FF),
			MKCOLOR(0x0000FFFF),
			MKCOLOR(0x00FFFFFF),
	};

	
	/************************/
	/* COMPANY LEAGUE TABLE */
	/************************/

	public static final int _performance_titles[] = {
		Str.STR_7066_ENGINEER,
		Str.STR_7066_ENGINEER,
		Str.STR_7067_TRAFFIC_MANAGER,
		Str.STR_7067_TRAFFIC_MANAGER,
		Str.STR_7068_TRANSPORT_COORDINATOR,
		Str.STR_7068_TRANSPORT_COORDINATOR,
		Str.STR_7069_ROUTE_SUPERVISOR,
		Str.STR_7069_ROUTE_SUPERVISOR,
		Str.STR_706A_DIRECTOR,
		Str.STR_706A_DIRECTOR,
		Str.STR_706B_CHIEF_EXECUTIVE,
		Str.STR_706B_CHIEF_EXECUTIVE,
		Str.STR_706C_CHAIRMAN,
		Str.STR_706C_CHAIRMAN,
		Str.STR_706D_PRESIDENT,
		Str.STR_706E_TYCOON,
	};
	
	
	
}

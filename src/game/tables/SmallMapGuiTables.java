package game.tables;

import game.Str;

public class SmallMapGuiTables 
{

	//#define MK(a,b) a, b
	//#define MKEND() 0xFFFF
	//#define 0x100|a,b) (a | 0x100), b

	/* Legend text giving the colours to look for on the minimap */
	public static final int _legend_land_contours[] = {
		0x5A,Str.STR_00F0_100M,
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
	
	
}

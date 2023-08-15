package com.dzavalishin.tables;

import com.dzavalishin.game.RoadStop;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.struct.DrawTileSeqStruct;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.struct.TileIndexDiffC;

public class StationTables 
{
	//
	public static final int INVALID_STATION = 0xFFFF;
	public static final int INVALID_SLOT = RoadStop.INVALID_SLOT;
	static public final int INVALID_VEHICLE = Vehicle.INVALID_VEHICLE;

	public static final int NUM_SLOTS = 2;


	public static final int FACIL_TRAIN = 1;
	public static final int FACIL_TRUCK_STOP = 2;
	public static final int FACIL_BUS_STOP = 4;
	public static final int FACIL_AIRPORT = 8;
	public static final int FACIL_DOCK = 0x10;


	//		public static final int HVOT_PENDING_DELETE = 1<<0; // not needed anymore
	public static final int HVOT_TRAIN = 1<<1;
	public static final int HVOT_BUS = 1 << 2;
	public static final int HVOT_TRUCK = 1 << 3;
	public static final int HVOT_AIRCRAFT = 1 << 4;
	public static final int HVOT_SHIP = 1 << 5;
	/* This bit is used to mark stations. No; it does not belong here; but what
	 * can we do? ;-) */
	public static final int HVOT_BUOY = 1 << 6;

	public static final int CA_BUS = 3;
	public static final int CA_TRUCK = 3;
	public static final int CA_AIR_OILPAD = 3;
	public static final int CA_TRAIN = 4;
	public static final int CA_AIR_HELIPORT = 4;
	public static final int CA_AIR_SMALL = 4;
	public static final int CA_AIR_LARGE = 5;
	public static final int CA_DOCK = 5;
	public static final int CA_AIR_METRO = 6;
	public static final int CA_AIR_INTER = 8;
	
	
	
	private static final int PALETTE_MODIFIER_COLOR = Sprite.PALETTE_MODIFIER_COLOR;
	private static final int PALETTE_TO_TRANSPARENT = Sprite.PALETTE_TO_TRANSPARENT;



	private static DrawTileSeqStruct TILE_SEQ_END()	
	{ 
		return new DrawTileSeqStruct( 0x80, 0, 0, 0, 0, 0, 0 ); 
	}

	static public final DrawTileSeqStruct _station_display_datas_0[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16,  5,  2, 0x42E | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 11,  0, 16,  5,  2, 0x430 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_1[] = {
        new DrawTileSeqStruct( 0,  0,  0,  5, 16,  2, 0x42F | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 11,  0,  0,  5, 16,  2, 0x42D | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_2[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16,  5,  2, 0x431 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 11,  0, 16,  5,  2, 0x430 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_3[] = {
        new DrawTileSeqStruct( 0,  0,  0,  5, 16,  2, 0x432 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 11,  0,  0,  5, 16,  2, 0x42D | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_4[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16,  5,  7, 0x434 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 11,  0, 16,  5,  2, 0x430 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0, 16, 16, 16, 10, 0x437 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,0x80, 0,  0,  0, 0x43B | PALETTE_TO_TRANSPARENT ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_5[] = {
        new DrawTileSeqStruct( 0,  0,  0,  5, 16,  2, 0x435 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 11,  0,  0,  5, 16,  2, 0x42D | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0, 16, 16, 16, 10, 0x438 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,0x80, 0,  0,  0, 0x43C | PALETTE_TO_TRANSPARENT ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_6[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16,  5,  2, 0x42E | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 11,  0, 16,  5,  2, 0x436 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0, 16, 16, 16, 10, 0x439 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,0x80, 0,  0,  0, 0x43D | PALETTE_TO_TRANSPARENT ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_7[] = {
        new DrawTileSeqStruct( 0,  0,  0,  5, 16,  2, 0x42F | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 11,  0,  0,  5, 16,  2, 0x433 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0, 16, 16, 16, 10, 0x43A | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,0x80, 0,  0,  0, 0x43E | PALETTE_TO_TRANSPARENT ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_8[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_9[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_10[] = {
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_11[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_12[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_13[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_14[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_15[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_16[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_17[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_18[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_19[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_20[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_21[] = {
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_22[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_23[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_24[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_25[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_26[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_27[] = {
        new DrawTileSeqStruct( 2,  0,  0, 11, 16, 40, 0xA5A | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_28[] = {
        new DrawTileSeqStruct( 3,  3,  0, 10, 10, 60, 0xA5B | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_29[] = {
        new DrawTileSeqStruct( 0,  1,  0, 14, 14, 30, 0xA5C | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_30[] = {
        new DrawTileSeqStruct( 3,  3,  0, 10, 11, 35, 0xA5D | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_31[] = {
        new DrawTileSeqStruct( 0,  3,  0, 16, 11, 40, 0xA5E | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_32[] = {
        new DrawTileSeqStruct( 14,  0,  0,  2, 16, 28, 0xA5F | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,  0,  2, 16, 28, 0xA60 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_33[] = {
        new DrawTileSeqStruct( 7, 11,  0,  3,  3, 14, 0xA63 ),
        new DrawTileSeqStruct( 0,  0,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_34[] = {
        new DrawTileSeqStruct( 2,  7,  0,  3,  3, 14, 0xA64 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_35[] = {
        new DrawTileSeqStruct( 3,  2,  0,  3,  3, 14, 0xA65 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_36[] = {
        new DrawTileSeqStruct( 0,  8,  0, 14,  3, 14, 0xA66 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_37[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_38[] = {
        new DrawTileSeqStruct( 0,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_39[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA78 ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_40[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA79 ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_41[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7A ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_42[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7B ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_43[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7C ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_44[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7D ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_45[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7E ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_46[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7F ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_47[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA80 ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_48[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA81 ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_49[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA82 ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_50[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA83 ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_51[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2, 70, 0xA29 ),
        new DrawTileSeqStruct( 0,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_52[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_53[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_54[] = {
        new DrawTileSeqStruct( 0,  0,  0, 15, 15, 30, 0xA6C | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_55[] = {
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_56[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_57[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_58[] = {
        new DrawTileSeqStruct( 0,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 4, 11,  0,  1,  1, 20, 0xA74 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_59[] = {
        new DrawTileSeqStruct( 0,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 4, 11,  0,  1,  1, 20, 0xA75 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_60[] = {
        new DrawTileSeqStruct( 0,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 4, 11,  0,  1,  1, 20, 0xA76 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_61[] = {
        new DrawTileSeqStruct( 0,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 4, 11,  0,  1,  1, 20, 0xA77 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_62[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_63[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_64[] = {
        new DrawTileSeqStruct( 0, 15,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_65[] = {
        new DrawTileSeqStruct( 14,  0,  0,  2, 16, 28, 0xA61 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,  0,  2, 16, 28, 0xA62 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_66[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16, 16, 60, 0xA49 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_67[] = {
        new DrawTileSeqStruct( 0, 15,  0, 13,  1, 10, 0xA98 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 13,  0,  0,  3, 16, 10, 0xA9C | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 2,  0,  0, 11,  1, 10, 0xAA0 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_68[] = {
        new DrawTileSeqStruct( 15,  3,  0,  1, 13, 10, 0xA99 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,  0, 16,  3, 10, 0xA9D | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  3,  0,  1, 11, 10, 0xAA1 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_69[] = {
        new DrawTileSeqStruct( 3,  0,  0, 13,  1, 10, 0xA9A | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,  0,  3, 16, 10, 0xA9E | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 3, 15,  0, 11,  1, 10, 0xAA2 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_70[] = {
        new DrawTileSeqStruct( 0,  0,  0,  1, 13, 10, 0xA9B | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 13,  0, 16,  3, 10, 0xA9F | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 15,  2,  0,  1, 11, 10, 0xAA3 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_71[] = {
        new DrawTileSeqStruct( 2,  0,  0, 11,  1, 10, 0xA88 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 13,  0,  0,  3, 16, 10, 0xA8C | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 13,  0, 13,  3, 10, 0xA90 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_72[] = {
        new DrawTileSeqStruct( 0,  3,  0,  1, 11, 10, 0xA89 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,  0, 16,  3, 10, 0xA8D | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 13,  3,  0,  3, 13, 10, 0xA91 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_73[] = {
        new DrawTileSeqStruct( 3, 15,  0, 11,  1, 10, 0xA8A | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,  0,  3, 16, 10, 0xA8E | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 3,  0,  0, 13,  3, 10, 0xA92 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_74[] = {
        new DrawTileSeqStruct( 15,  2,  0,  1, 11, 10, 0xA8B | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 13,  0, 16,  3, 10, 0xA8F | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0,  0,  0,  3, 13, 10, 0xA93 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_75[] = {
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_76[] = {
        new DrawTileSeqStruct( 0,  4,  0, 16,  8,  8, 0xAA7 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_77[] = {
        new DrawTileSeqStruct( 4,  0,  0,  8, 16,  8, 0xAA8 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_78[] = {
        new DrawTileSeqStruct( 0,  4,  0, 16,  8,  8, 0xAA9 ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_79[] = {
        new DrawTileSeqStruct( 4,  0,  0,  8, 16,  8, 0xAAA ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_80[] = {
        new DrawTileSeqStruct( 0,  4,  0, 16,  8,  8, 0xAAB ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_81[] = {
        new DrawTileSeqStruct( 4,  0,  0,  8, 16,  8, 0xAAC ),
		TILE_SEQ_END()
	};

	static public final DrawTileSeqStruct _station_display_datas_82[] = {
		TILE_SEQ_END()
	};

	// end of runway
	static public final DrawTileSeqStruct _station_display_datas_083[] = {
		TILE_SEQ_END()
	};

	// runway tiles
	static public final DrawTileSeqStruct _station_display_datas_084[] = {
		TILE_SEQ_END()
	};

	// control tower with concrete underground and no fence
	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_085[] = {
        new DrawTileSeqStruct( 3,  3,  0, 10, 10, 60, 0xA5B | PALETTE_MODIFIER_COLOR ),  // control tower
		TILE_SEQ_END()
	};

	// new airportdepot, facing west
	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_086[] = {
        new DrawTileSeqStruct( 14, 0,  0,  2, 16, 28, 0xA61 | PALETTE_MODIFIER_COLOR ),
        new DrawTileSeqStruct( 0, 0,  0,  2, 16, 28, 0xA62 ),
		TILE_SEQ_END()
	};

	// asphalt tile with fences in north
	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_087[] = {
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// end of runway
	static public final DrawTileSeqStruct _station_display_datas_088[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ), // fences
		TILE_SEQ_END()
	};

	// runway tiles
	static public final DrawTileSeqStruct _station_display_datas_089[] = {
        new DrawTileSeqStruct( 0,  0,  0, 16,  1,  6, 0xA68 | PALETTE_MODIFIER_COLOR ), // fences
		TILE_SEQ_END()
	};

	// turning radar with concrete underground fences on south -- needs 12 tiles
	// concrete underground
	//BEGIN
	static public final DrawTileSeqStruct _station_display_datas_090[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA78 ),   // turning radar
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),  //fences
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_091[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA79 ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_092[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7A ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_093[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7B ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_094[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7C ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_095[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7D ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_096[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7E ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_097[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7F ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_098[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA80 ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_099[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA81 ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0100[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA82 ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0101[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA83 ),
        new DrawTileSeqStruct( 15,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};
	//END

	// turning radar with concrete underground fences on north -- needs 12 tiles
	// concrete underground
	//BEGIN
	static public final DrawTileSeqStruct _station_display_datas_0102[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA78 ),   // turning radar
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0103[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA79 ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0104[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7A ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0105[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7B ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0106[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7C ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0107[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7D ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0108[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7E ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0109[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA7F ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0110[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA80 ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0111[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA81 ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0112[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA82 ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0113[] = {
        new DrawTileSeqStruct( 7,  7,  0,  2,  2,  8, 0xA83 ),
        new DrawTileSeqStruct( 0,  0, 0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};
	//END

	// helipad for international airport
	// concrete underground
	static public final DrawTileSeqStruct _station_display_datas_0114[] = {
        new DrawTileSeqStruct( 10,  6, 0,  0, 0,  0, Sprite.SPR_AIRPORT_HELIPAD ),
        new DrawTileSeqStruct( 15,  0,  0,  1, 16,  6, 0xA67 | PALETTE_MODIFIER_COLOR ),	// fences bottom
		TILE_SEQ_END()
	};

	static public final DrawTileSprites _station_display_datas[] = {
        new DrawTileSprites( 0x3F4, _station_display_datas_0 ),
        new DrawTileSprites( 0x3F3, _station_display_datas_1 ),
        new DrawTileSprites( 0x3F4, _station_display_datas_2 ),
        new DrawTileSprites( 0x3F3, _station_display_datas_3 ),
        new DrawTileSprites( 0x3F4, _station_display_datas_4 ),
        new DrawTileSprites( 0x3F3, _station_display_datas_5 ),
        new DrawTileSprites( 0x3F4, _station_display_datas_6 ),
        new DrawTileSprites( 0x3F3, _station_display_datas_7 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_8 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_9 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_10 ),
        new DrawTileSprites( 0xA4B, _station_display_datas_11 ),
        new DrawTileSprites( 0xA4C, _station_display_datas_12 ),
        new DrawTileSprites( 0xA4D, _station_display_datas_13 ),
        new DrawTileSprites( 0xA4E, _station_display_datas_14 ),
        new DrawTileSprites( 0xA4F, _station_display_datas_15 ),
        new DrawTileSprites( 0xA50, _station_display_datas_16 ),
        new DrawTileSprites( 0xA51, _station_display_datas_17 ),
        new DrawTileSprites( 0xA52, _station_display_datas_18 ),
        new DrawTileSprites( 0xA53, _station_display_datas_19 ),
        new DrawTileSprites( 0xA54, _station_display_datas_20 ),
        new DrawTileSprites( 0xA53, _station_display_datas_21 ),
        new DrawTileSprites( 0xA55, _station_display_datas_22 ),
        new DrawTileSprites( 0xA56, _station_display_datas_23 ),
        new DrawTileSprites( 0xA57, _station_display_datas_24 ),
        new DrawTileSprites( 0xA58, _station_display_datas_25 ),
        new DrawTileSprites( 0xA59, _station_display_datas_26 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_27 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_28 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_29 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_30 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_31 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_32 ),
        new DrawTileSprites( 0xA4B, _station_display_datas_33 ),
        new DrawTileSprites( 0xA4B, _station_display_datas_34 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_35 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_36 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_37 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_38 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_39 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_40 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_41 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_42 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_43 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_44 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_45 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_46 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_47 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_48 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_49 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_50 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_51 ),
        new DrawTileSprites( 0xA69, _station_display_datas_52 ),
        new DrawTileSprites( 0xA6A, _station_display_datas_53 ),
        new DrawTileSprites( 0xA6B | PALETTE_MODIFIER_COLOR, _station_display_datas_54 ),
        new DrawTileSprites( 0xA6D, _station_display_datas_55 ),
        new DrawTileSprites( 0xA6E, _station_display_datas_56 ),
        new DrawTileSprites( 0xA6F, _station_display_datas_57 ),
        new DrawTileSprites( 0xA70, _station_display_datas_58 ),
        new DrawTileSprites( 0xA70, _station_display_datas_59 ),
        new DrawTileSprites( 0xA70, _station_display_datas_60 ),
        new DrawTileSprites( 0xA70, _station_display_datas_61 ),
        new DrawTileSprites( 0xA71, _station_display_datas_62 ),
        new DrawTileSprites( 0xA72, _station_display_datas_63 ),
        new DrawTileSprites( 0xA73, _station_display_datas_64 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_65 ),
        new DrawTileSprites( 0xF8D, _station_display_datas_66 ),
        new DrawTileSprites( 0xA94 | PALETTE_MODIFIER_COLOR, _station_display_datas_67 ),
        new DrawTileSprites( 0xA95 | PALETTE_MODIFIER_COLOR, _station_display_datas_68 ),
        new DrawTileSprites( 0xA96 | PALETTE_MODIFIER_COLOR, _station_display_datas_69 ),
        new DrawTileSprites( 0xA97 | PALETTE_MODIFIER_COLOR, _station_display_datas_70 ),
        new DrawTileSprites( 0xA84 | PALETTE_MODIFIER_COLOR, _station_display_datas_71 ),
        new DrawTileSprites( 0xA85 | PALETTE_MODIFIER_COLOR, _station_display_datas_72 ),
        new DrawTileSprites( 0xA86 | PALETTE_MODIFIER_COLOR, _station_display_datas_73 ),
        new DrawTileSprites( 0xA87 | PALETTE_MODIFIER_COLOR, _station_display_datas_74 ),
        new DrawTileSprites( 0xFDD, _station_display_datas_75 ),
        new DrawTileSprites( 0xFE4, _station_display_datas_76 ),
        new DrawTileSprites( 0xFE5, _station_display_datas_77 ),
        new DrawTileSprites( 0xFE3, _station_display_datas_78 ),
        new DrawTileSprites( 0xFE2, _station_display_datas_79 ),
        new DrawTileSprites( 0xFDD, _station_display_datas_80 ),
        new DrawTileSprites( 0xFDD, _station_display_datas_81 ),
        new DrawTileSprites( 0xFEC, _station_display_datas_82 ),
        new DrawTileSprites( 0xA59, _station_display_datas_083 ),
        new DrawTileSprites( 0xA56, _station_display_datas_084 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_085 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_086 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_087 ),
        new DrawTileSprites( 0xA59, _station_display_datas_088 ),
        new DrawTileSprites( 0xA56, _station_display_datas_089 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_090 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_091 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_092 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_093 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_094 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_095 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_096 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_097 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_098 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_099 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0100 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0101 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0102 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0103 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0104 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0105 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0106 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0107 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0108 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0109 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0110 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0111 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0112 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0113 ),
        new DrawTileSprites( 0xA4A, _station_display_datas_0114 ),
	};
	

	
	
	// FIXME -- need to be embedded into Airport variable. Is dynamically
	// deducteable from graphics-tile array, so will not be needed
	public final static  byte _airport_size_x[] = {4, 6, 1, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
	public final static byte _airport_size_y[] = {3, 6, 1, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};

	public static final  TileIndexDiffC _count_square_table[] = 
		{
				new TileIndexDiffC(-3, -3), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0),
				new TileIndexDiffC(-6,  1), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0),
				new TileIndexDiffC(-6,  1), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0),
				new TileIndexDiffC(-6,  1), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0),
				new TileIndexDiffC(-6,  1), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0),
				new TileIndexDiffC(-6,  1), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0),
				new TileIndexDiffC(-6,  1), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0), new TileIndexDiffC(1, 0)
		};
	

	
	//#define M(x) ((x) - Str.STR_SV_STNAME)
	/*static final  int _gen_station_name_bits[] = {
			0,                                      // 0 
	1 << M(Str.STR_SV_STNAME_AIRPORT),          // 1 
	1 << M(Str.STR_SV_STNAME_OILFIELD),         // 2 
	1 << M(Str.STR_SV_STNAME_DOCKS),            // 3 
	0x1FF << M(Str.STR_SV_STNAME_BUOY_1),       // 4 
	1 << M(Str.STR_SV_STNAME_HELIPORT),         // 5 
	};*/

	public static int M(int x) { return ((x) - Str.STR_SV_STNAME); }

	public static final  int _gen_station_name_bits[] = {
			0,                                      /* 0 */
			1 << (Str.STR_SV_STNAME_AIRPORT - Str.STR_SV_STNAME),          /* 1 */
			1 << (Str.STR_SV_STNAME_OILFIELD - Str.STR_SV_STNAME),         /* 2 */
			1 << (Str.STR_SV_STNAME_DOCKS - Str.STR_SV_STNAME),            /* 3 */
			0x1FF << (Str.STR_SV_STNAME_BUOY_1 - Str.STR_SV_STNAME),       /* 4 */
			1 << (Str.STR_SV_STNAME_HELIPORT - Str.STR_SV_STNAME),         /* 5 */
	};

	public static final  int _direction_and_table[] = {
			~( (1<<M(Str.STR_SV_STNAME_WEST)) | (1<<M(Str.STR_SV_STNAME_EAST)) | (1<<M(Str.STR_SV_STNAME_NORTH)) ),
			~( (1<<M(Str.STR_SV_STNAME_SOUTH)) | (1<<M(Str.STR_SV_STNAME_WEST)) | (1<<M(Str.STR_SV_STNAME_NORTH)) ),
			~( (1<<M(Str.STR_SV_STNAME_SOUTH)) | (1<<M(Str.STR_SV_STNAME_EAST)) | (1<<M(Str.STR_SV_STNAME_NORTH)) ),
			~( (1<<M(Str.STR_SV_STNAME_SOUTH)) | (1<<M(Str.STR_SV_STNAME_WEST)) | (1<<M(Str.STR_SV_STNAME_EAST)) ),
	};


	
	// FIXME -- need to move to its corresponding Airport variable
	// Country Airfield (small)
	static final  byte _airport_map5_tiles_country[] = {
			54, 53, 52, 65,
			58, 57, 56, 55,
			64, 63, 63, 62
	};

	// City Airport (large)
	static final  byte _airport_map5_tiles_town[] = {
			31,  9, 33,  9,  9, 32,
			27, 36, 29, 34,  8, 10,
			30, 11, 35, 13, 20, 21,
			51, 12, 14, 17, 19, 28,
			38, 13, 15, 16, 18, 39,
			26, 22, 23, 24, 25, 26
	};

	// Metropolitain Airport (large) - 2 runways
	static final  byte _airport_map5_tiles_metropolitan[] = {
			31,  9, 33,  9,  9, 32,
			27, 36, 29, 34,  8, 10,
			30, 11, 35, 13, 20, 21,
			102,  8,  8,  8,  8, 28,
			83, 84, 84, 84, 84, 83,
			26, 23, 23, 23, 23, 26
	};

	// International Airport (large) - 2 runways
	static final  byte _airport_map5_tiles_international[] = {
			88, 89, 89, 89, 89, 89,  88,
			51,  8,  8,  8,  8,  8,  32,
			30,  8, 11, 27, 11,  8,  10,
			32,  8, 11, 27, 11,  8, 114,
			87,  8, 11, 85, 11,  8, 114,
			87,  8,  8,  8,  8,  8,  90,
			26, 23, 23, 23, 23, 23,  26
	};

	// Heliport
	static final  byte _airport_map5_tiles_heliport[] = {
			66,
	};

	public static final  byte [][] _airport_map5_tiles = {
			_airport_map5_tiles_country,				// Country Airfield (small)
			_airport_map5_tiles_town,						// City Airport (large)
			_airport_map5_tiles_heliport,				// Heliport
			_airport_map5_tiles_metropolitan,   // Metropolitain Airport (large)
			_airport_map5_tiles_international,	// International Airport (xlarge)
	};
	
	
}

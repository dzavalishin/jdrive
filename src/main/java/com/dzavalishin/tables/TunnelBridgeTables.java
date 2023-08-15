package com.dzavalishin.tables;

import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Str;

public class TunnelBridgeTables {

	public static final Bridge orig_bridge[] = {
			/*
				   year of availablity
				   |  minimum length
				   |  |   maximum length
				   |  |   |    price
				   |  |   |    |    maximum speed
				   |  |   |    |    |  sprite to use in GUI                string with description
				   |  |   |    |    |  |                                   |                            */
				new Bridge(  0, 0, 16,  80,  32, 0xA24                             , Str.STR_5012_WOODEN             , null, 0 ),
				new Bridge(  0, 0,  2, 112,  48, 0xA26 | Sprite.PALETTE_TO_STRUCT_RED     , Str.STR_5013_CONCRETE           , null, 0 ),
				new Bridge( 10, 0,  5, 144,  64, 0xA25                             , Str.STR_500F_GIRDER_STEEL       , null, 0 ),
				new Bridge(  0, 2, 10, 168,  80, 0xA22 | Sprite.PALETTE_TO_STRUCT_CONCRETE, Str.STR_5011_SUSPENSION_CONCRETE, null, 0 ),
				new Bridge( 10, 3, 16, 185,  96, 0xA22                             , Str.STR_500E_SUSPENSION_STEEL   , null, 0 ),
				new Bridge( 10, 3, 16, 192, 112, 0xA22 | Sprite.PALETTE_TO_STRUCT_YELLOW  , Str.STR_500E_SUSPENSION_STEEL   , null, 0 ),
				new Bridge( 10, 3,  7, 224, 160, 0xA23                             , Str.STR_5010_CANTILEVER_STEEL   , null, 0 ),
				new Bridge( 10, 3,  8, 232, 208, 0xA23 | Sprite.PALETTE_TO_STRUCT_BROWN   , Str.STR_5010_CANTILEVER_STEEL   , null, 0 ),
				new Bridge( 10, 3,  9, 248, 240, 0xA23 | Sprite.PALETTE_TO_STRUCT_RED     , Str.STR_5010_CANTILEVER_STEEL   , null, 0 ),
				new Bridge( 10, 0,  2, 240, 256, 0xA27                             , Str.STR_500F_GIRDER_STEEL       , null, 0 ),
				new Bridge( 75, 2, 16, 255, 320, 0xA28                             , Str.STR_5014_TUBULAR_STEEL      , null, 0 ),
				new Bridge( 85, 2, 32, 380, 512, 0xA28 | Sprite.PALETTE_TO_STRUCT_YELLOW  , Str.STR_5014_TUBULAR_STEEL      , null, 0 ),
				new Bridge( 90, 2, 32, 510, 608, 0xA28 | Sprite.PALETTE_TO_STRUCT_GREY    , Str.STR_BRIDGE_TUBULAR_SILICON  , null, 0 )
			};

	public static final int _bridge_foundations[][] = {
			// 0 1  2  3  4 5 6 7  8 9 10 11 12 13 14 15
			{1,16,18,3,20,5,0,7,22,0,10,11,12,13,14},
			{1,15,17,0,19,5,6,7,21,9,10,11, 0,13,14},
	};
	
	protected static final int _new_data_table[] = {0x1002, 0x1001, 0x2005, 0x200A, 0, 0, 0, 0};

	public static final int _updsignals_tunnel_dir[] = { 5, 7, 1, 3};

	
	public static final int _tunnel_fractcoord_1[] = {0x8E,0x18,0x81,0xE8};
	public static final int _tunnel_fractcoord_2[] = {0x81,0x98,0x87,0x38};
	public static final int _tunnel_fractcoord_3[] = {0x82,0x88,0x86,0x48};
	public static final int _exit_tunnel_track[] = {1,2,1,2};

	public static final int _road_exit_tunnel_state[] = {8, 9, 0, 1};
	public static final int _road_exit_tunnel_frame[] = {2, 7, 9, 4};

	public static final int _tunnel_fractcoord_4[] = {0x52, 0x85, 0x98, 0x29};
	public static final int _tunnel_fractcoord_5[] = {0x92, 0x89, 0x58, 0x25};
	public static final int _tunnel_fractcoord_6[] = {0x92, 0x89, 0x56, 0x45};
	public static final int _tunnel_fractcoord_7[] = {0x52, 0x85, 0x96, 0x49};

	
	
	public static final int _build_tunnel_coord_mod[] = { -16, 0, 16, 0, -16 };
	public static final byte _build_tunnel_tileh[] = {3, 9, 12, 6};
	
	
	public static final byte _tileh_bits[][] = {
			{2,1,8,4,  16,11,0,9},
			{1,8,4,2,  11,16,9,0},
			{4,8,1,2,  16,11,0,9},
			{2,4,8,1,  11,16,9,0},
	};
	

	//static final StringID _bridge_tile_str[(MAX_BRIDGES + 3) + (MAX_BRIDGES + 3)] = {
	protected static final /*StringID*/int _bridge_tile_str[] = {
			Str.STR_501F_WOODEN_RAIL_BRIDGE,
			Str.STR_5020_CONCRETE_RAIL_BRIDGE,
			Str.STR_501C_STEEL_GIRDER_RAIL_BRIDGE,
			Str.STR_501E_REINFORCED_CONCRETE_SUSPENSION,
			Str.STR_501B_STEEL_SUSPENSION_RAIL_BRIDGE,
			Str.STR_501B_STEEL_SUSPENSION_RAIL_BRIDGE,
			Str.STR_501D_STEEL_CANTILEVER_RAIL_BRIDGE,
			Str.STR_501D_STEEL_CANTILEVER_RAIL_BRIDGE,
			Str.STR_501D_STEEL_CANTILEVER_RAIL_BRIDGE,
			Str.STR_501C_STEEL_GIRDER_RAIL_BRIDGE,
			Str.STR_5027_TUBULAR_RAIL_BRIDGE,
			Str.STR_5027_TUBULAR_RAIL_BRIDGE,
			Str.STR_5027_TUBULAR_RAIL_BRIDGE,
			0,0,0,

			Str.STR_5025_WOODEN_ROAD_BRIDGE,
			Str.STR_5026_CONCRETE_ROAD_BRIDGE,
			Str.STR_5022_STEEL_GIRDER_ROAD_BRIDGE,
			Str.STR_5024_REINFORCED_CONCRETE_SUSPENSION,
			Str.STR_5021_STEEL_SUSPENSION_ROAD_BRIDGE,
			Str.STR_5021_STEEL_SUSPENSION_ROAD_BRIDGE,
			Str.STR_5023_STEEL_CANTILEVER_ROAD_BRIDGE,
			Str.STR_5023_STEEL_CANTILEVER_ROAD_BRIDGE,
			Str.STR_5023_STEEL_CANTILEVER_ROAD_BRIDGE,
			Str.STR_5022_STEEL_GIRDER_ROAD_BRIDGE,
			Str.STR_5028_TUBULAR_ROAD_BRIDGE,
			Str.STR_5028_TUBULAR_ROAD_BRIDGE,
			Str.STR_5028_TUBULAR_ROAD_BRIDGE,
			0,0,0,
	};
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* $Id: bridge_land.h 3118 2005-11-02 13:06:07Z tron $ */

	/** @file bridge_land.h This file contains all the sprites for bridges
	  * It consists of a number of arrays.
	  * <ul><li>_bridge_sprite_table_n_m. Defines all the sprites of a bridge besides the pylons.
	  * n defines the number of the bridge type, m the number of the section. the highest m for
	  * each bridge set defines the heads.<br>
	  * Sprites for middle secionts are arranged in groups of four, the elements are:
	  * <ol><li>Element containing the track. This element is logically behind the vehicle.</li>
	  * <li>Element containing the structure that is logically between the vehicle and the camera</li>
	  * <li>Element containing the pylons.</li></ol>
	  * First group is for railway in X direction, second for railway in Y direction, two groups each follow for road, monorail and maglev<p>
	  * <br>Elements for heads are arranged in groups of eight:
	  * <ol><li>X direction, north end, flat</li>
	  * <li>Y direction, north end, flat</li>
	  * <li>X direction, south end, flat</li>
	  * <li>Y direction, south end, flat</li>
	  * <li>X direction, north end, sloped</li>
	  * <li>Y direction, north end, sloped</li>
	  * <li>X direction, south end, sloped</li>
	  * <li>Y direction, south end, sloped</li></ol>
	  * This is repeated 4 times, for rail, road, monorail, maglev</li>
	  * <li>_bridge_sprite_table_n_poles. Defines all the sprites needed for the pylons. The first 6 elements are for each
	  * bridge piece (max 5 currently) in X direction, the next 6 elements are for the bridge pieces in Y direction.
	  * The last two elements are used for cantilever bridges</li>
	  * </ul>
	  */

	protected static final /*SpriteID*/ int _bridge_land_below[] = {
		Sprite.SPR_FLAT_GRASS_TILE, Sprite.SPR_FLAT_WATER_TILE, Sprite.SPR_FLAT_SNOWY_TILE, Sprite.SPR_FLAT_WATER_TILE
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_2_0[] = {
		    0x9C3,     0x9C7,     0x9C9,       0x0,     0x9C4,     0x9C8,     0x9CA,       0x0,
		    0x9C5,     0x9C7,     0x9C9,       0x0,     0x9C6,     0x9C8,     0x9CA,       0x0,
		   0x10E4,     0x9C7,     0x9C9,       0x0,    0x10E5,     0x9C8,     0x9CA,       0x0,
		   0x110C,     0x9C7,     0x9C9,       0x0,    0x110D,     0x9C8,     0x9CA,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_2_1[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		0x98E | Sprite.PALETTE_TO_STRUCT_WHITE, 0x990 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x98D | Sprite.PALETTE_TO_STRUCT_WHITE, 0x98F | Sprite.PALETTE_TO_STRUCT_WHITE, 0x992 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x994 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x991 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x993 | Sprite.PALETTE_TO_STRUCT_WHITE,
		0x10E7 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x10E9 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x10E6 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x10E8 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x10EB | Sprite.PALETTE_TO_STRUCT_WHITE, 0x10ED | Sprite.PALETTE_TO_STRUCT_WHITE, 0x10EA | Sprite.PALETTE_TO_STRUCT_WHITE, 0x10EC | Sprite.PALETTE_TO_STRUCT_WHITE,
		0x110F | Sprite.PALETTE_TO_STRUCT_WHITE, 0x1111 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x110E | Sprite.PALETTE_TO_STRUCT_WHITE, 0x1110 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x1113 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x1115 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x1112 | Sprite.PALETTE_TO_STRUCT_WHITE, 0x1114 | Sprite.PALETTE_TO_STRUCT_WHITE,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_2_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 5,
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 3 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 2,
		0x0,

		0x0,
		0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_0[] = {
		    0x9A9,     0x99F,     0x9B1,       0x0,     0x9A5,     0x997,     0x9AD,       0x0,
		    0x99D,     0x99F,     0x9B1,       0x0,     0x995,     0x997,     0x9AD,       0x0,
		   0x10F2,     0x99F,     0x9B1,       0x0,    0x10EE,     0x997,     0x9AD,       0x0,
		   0x111A,     0x99F,     0x9B1,       0x0,    0x1116,     0x997,     0x9AD,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_1[] = {
		    0x9AA,     0x9A0,     0x9B2,       0x0,     0x9A6,     0x998,     0x9AE,       0x0,
		    0x99E,     0x9A0,     0x9B2,       0x0,     0x996,     0x998,     0x9AE,       0x0,
		   0x10F3,     0x9A0,     0x9B2,       0x0,    0x10EF,     0x998,     0x9AE,       0x0,
		   0x111B,     0x9A0,     0x9B2,       0x0,    0x1117,     0x998,     0x9AE,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_2[] = {
		    0x9AC,     0x9A4,     0x9B4,       0x0,     0x9A8,     0x99C,     0x9B0,       0x0,
		    0x9A2,     0x9A4,     0x9B4,       0x0,     0x99A,     0x99C,     0x9B0,       0x0,
		   0x10F5,     0x9A4,     0x9B4,       0x0,    0x10F1,     0x99C,     0x9B0,       0x0,
		   0x111D,     0x9A4,     0x9B4,       0x0,    0x1119,     0x99C,     0x9B0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_3[] = {
		    0x9AB,     0x9A3,     0x9B3,       0x0,     0x9A7,     0x99B,     0x9AF,       0x0,
		    0x9A1,     0x9A3,     0x9B3,       0x0,     0x999,     0x99B,     0x9AF,       0x0,
		   0x10F4,     0x9A3,     0x9B3,       0x0,    0x10F0,     0x99B,     0x9AF,       0x0,
		   0x111C,     0x9A3,     0x9B3,       0x0,    0x1118,     0x99B,     0x9AF,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_4[] = {
		    0x9B6,     0x9BA,     0x9BC,       0x0,     0x9B5,     0x9B9,     0x9BB,       0x0,
		    0x9B8,     0x9BA,     0x9BC,       0x0,     0x9B7,     0x9B9,     0x9BB,       0x0,
		   0x10F7,     0x9BA,     0x9BC,       0x0,    0x10F6,     0x9B9,     0x9BB,       0x0,
		   0x111F,     0x9BA,     0x9BC,       0x0,    0x111E,     0x9B9,     0x9BB,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_5[] = {
		    0x9BD,     0x9C1,       0x0,       0x0,     0x9BE,     0x9C2,       0x0,       0x0,
		    0x9BF,     0x9C1,       0x0,       0x0,     0x9C0,     0x9C2,       0x0,       0x0,
		   0x10F8,     0x9C1,       0x0,       0x0,    0x10F9,     0x9C2,       0x0,       0x0,
		   0x1120,     0x9C1,       0x0,       0x0,    0x1121,     0x9C2,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_6[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		    0x98E,     0x990,     0x98D,     0x98F,     0x992,     0x994,     0x991,     0x993,
		   0x10E7,    0x10E9,    0x10E6,    0x10E8,    0x10EB,    0x10ED,    0x10EA,    0x10EC,
		   0x110F,    0x1111,    0x110E,    0x1110,    0x1113,    0x1115,    0x1112,    0x1114,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_4_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 4,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 4,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 5,
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 0 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 1,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 1,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 2,
		0x0,

		0x0,
		0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_0[] = {
		0x9A9 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99F | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x9A5 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x997 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AD | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x99D | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99F | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x995 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x997 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AD | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x10F2 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99F | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x10EE | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x997 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AD | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x111A | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99F | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x1116 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x997 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AD | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		Sprite.SPR_PILLARS_BASE + 2
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_1[] = {
		0x9AA | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A0 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x9A6 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x998 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AE | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x99E | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A0 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x996 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x998 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AE | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x10F3 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A0 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x10EF | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x998 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AE | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x111B | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A0 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x1117 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x998 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AE | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		Sprite.SPR_PILLARS_BASE + 3
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_2[] = {
		0x9AC | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A4 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B4 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x9A8 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99C | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B0 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x9A2 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A4 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B4 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x99A | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99C | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B0 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x10F5 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A4 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B4 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x10F1 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99C | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B0 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x111D | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A4 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B4 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x1119 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99C | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B0 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		Sprite.SPR_PILLARS_BASE + 3
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_3[] = {
		0x9AB | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A3 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B3 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x9A7 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99B | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AF | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x9A1 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A3 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B3 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x999 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99B | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AF | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x10F4 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A3 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B3 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x10F0 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99B | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AF | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x111C | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9A3 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B3 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x1118 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x99B | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9AF | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		Sprite.SPR_PILLARS_BASE + 2
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_4[] = {
		0x9B6 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BA | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BC | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x9B5 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B9 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BB | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x9B8 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BA | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BC | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x9B7 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B9 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BB | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x10F7 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BA | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BC | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x10F6 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B9 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BB | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		0x111F | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BA | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BC | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0, 0x111E | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9B9 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9BB | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
		Sprite.SPR_PILLARS_BASE + 5, 0x0, 0x0, 0x0, Sprite.SPR_PILLARS_BASE + 4
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_5[] = {
		0x9BD | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0, 0x9BE | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
		0x9BF | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0, 0x9C0 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
		0x10F8 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0, 0x10F9 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
		0x1120 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C1 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0, 0x1121 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x9C2 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
		0x0, Sprite.SPR_PILLARS_BASE + 2
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_6[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		0x98E | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x990 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x98D | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x98F | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x992 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x994 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x991 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x993 | Sprite.PALETTE_TO_STRUCT_YELLOW,
		0x10E7 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x10E9 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x10E6 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x10E8 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x10EB | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x10ED | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x10EA | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x10EC | Sprite.PALETTE_TO_STRUCT_YELLOW,
		0x110F | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x1111 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x110E | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x1110 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x1113 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x1115 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x1112 | Sprite.PALETTE_TO_STRUCT_YELLOW, 0x1114 | Sprite.PALETTE_TO_STRUCT_YELLOW,
		0x0, Sprite.SPR_PILLARS_BASE + 2,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_5_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 4 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 4 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 5 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 0 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 1 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 1 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 2 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		0x0,

		0x0,
		0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_0[] = {
		0x9A9 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99F | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x9A5 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x997 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AD | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x99D | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99F | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x995 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x997 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AD | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x10F2 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99F | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x10EE | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x997 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AD | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x111A | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99F | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x1116 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x997 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AD | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_1[] = {
		0x9AA | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A0 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x9A6 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x998 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AE | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x99E | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A0 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x996 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x998 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AE | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x10F3 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A0 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x10EF | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x998 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AE | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x111B | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A0 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x1117 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x998 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AE | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_2[] = {
		0x9AC | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A4 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B4 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x9A8 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99C | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B0 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x9A2 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A4 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B4 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x99A | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99C | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B0 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x10F5 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A4 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B4 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x10F1 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99C | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B0 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x111D | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A4 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B4 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x1119 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99C | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B0 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_3[] = {
		0x9AB | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A3 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B3 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x9A7 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99B | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AF | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x9A1 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A3 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B3 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x999 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99B | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AF | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x10F4 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A3 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B3 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x10F0 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99B | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AF | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x111C | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9A3 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B3 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x1118 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x99B | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9AF | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_4[] = {
		0x9B6 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BA | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BC | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x9B5 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B9 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BB | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x9B8 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BA | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BC | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x9B7 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B9 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BB | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x10F7 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BA | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BC | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x10F6 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B9 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BB | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
		0x111F | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BA | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BC | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0, 0x111E | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9B9 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9BB | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_5[] = {
		0x9BD | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0, 0x9BE | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0,
		0x9BF | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0, 0x9C0 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0,
		0x10F8 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0, 0x10F9 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0,
		0x1120 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C1 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0, 0x1121 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x9C2 | Sprite.PALETTE_TO_STRUCT_CONCRETE,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_6[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		0x98E | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x990 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x98D | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x98F | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x992 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x994 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x991 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x993 | Sprite.PALETTE_TO_STRUCT_CONCRETE,
		0x10E7 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x10E9 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x10E6 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x10E8 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x10EB | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x10ED | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x10EA | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x10EC | Sprite.PALETTE_TO_STRUCT_CONCRETE,
		0x110F | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x1111 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x110E | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x1110 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x1113 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x1115 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x1112 | Sprite.PALETTE_TO_STRUCT_CONCRETE, 0x1114 | Sprite.PALETTE_TO_STRUCT_CONCRETE,
		0x0, Sprite.SPR_PILLARS_BASE + 2,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_3_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 4 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 4 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 5 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 0 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 1 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 1 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		Sprite.SPR_PILLARS_BASE + 6 * 0 + 2 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_CONCRETE),
		0x0,

		0x0,
		0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_1_1[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		0x98E | Sprite.PALETTE_TO_STRUCT_RED, 0x990 | Sprite.PALETTE_TO_STRUCT_RED, 0x98D | Sprite.PALETTE_TO_STRUCT_RED, 0x98F | Sprite.PALETTE_TO_STRUCT_RED, 0x992 | Sprite.PALETTE_TO_STRUCT_RED, 0x994 | Sprite.PALETTE_TO_STRUCT_RED, 0x991 | Sprite.PALETTE_TO_STRUCT_RED, 0x993 | Sprite.PALETTE_TO_STRUCT_RED,
		0x10E7 | Sprite.PALETTE_TO_STRUCT_RED, 0x10E9 | Sprite.PALETTE_TO_STRUCT_RED, 0x10E6 | Sprite.PALETTE_TO_STRUCT_RED, 0x10E8 | Sprite.PALETTE_TO_STRUCT_RED, 0x10EB | Sprite.PALETTE_TO_STRUCT_RED, 0x10ED | Sprite.PALETTE_TO_STRUCT_RED, 0x10EA | Sprite.PALETTE_TO_STRUCT_RED, 0x10EC | Sprite.PALETTE_TO_STRUCT_RED,
		0x110F | Sprite.PALETTE_TO_STRUCT_RED, 0x1111 | Sprite.PALETTE_TO_STRUCT_RED, 0x110E | Sprite.PALETTE_TO_STRUCT_RED, 0x1110 | Sprite.PALETTE_TO_STRUCT_RED, 0x1113 | Sprite.PALETTE_TO_STRUCT_RED, 0x1115 | Sprite.PALETTE_TO_STRUCT_RED, 0x1112 | Sprite.PALETTE_TO_STRUCT_RED, 0x1114 | Sprite.PALETTE_TO_STRUCT_RED,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_1_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 4,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 4,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 5,
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 3 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 1,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 1,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 3 + 2,
		0x0,

		0x0,
		0x0,
	};


	static final /*PalSpriteID*/ int _bridge_sprite_table_6_0[] = {
		    0x9CD,     0x9D9,       0x0,       0x0,     0x9CE,     0x9DA,       0x0,       0x0,
		    0x9D3,     0x9D9,       0x0,       0x0,     0x9D4,     0x9DA,       0x0,       0x0,
		   0x10FC,     0x9D9,       0x0,       0x0,    0x10FD,     0x9DA,       0x0,       0x0,
		   0x1124,     0x9D9,       0x0,       0x0,    0x1125,     0x9DA,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_6_1[] = {
		    0x9CB,     0x9D7,     0x9DD,       0x0,     0x9D0,     0x9DC,     0x9E0,       0x0,
		    0x9D1,     0x9D7,     0x9DD,       0x0,     0x9D6,     0x9DC,     0x9E0,       0x0,
		   0x10FA,     0x9D7,     0x9DD,       0x0,    0x10FF,     0x9DC,     0x9E0,       0x0,
		   0x1122,     0x9D7,     0x9DD,       0x0,    0x1127,     0x9DC,     0x9E0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_6_2[] = {
		    0x9CC,     0x9D8,     0x9DE,       0x0,     0x9CF,     0x9DB,     0x9DF,       0x0,
		    0x9D2,     0x9D8,     0x9DE,       0x0,     0x9D5,     0x9DB,     0x9DF,       0x0,
		   0x10FB,     0x9D8,     0x9DE,       0x0,    0x10FE,     0x9DB,     0x9DF,       0x0,
		   0x1123,     0x9D8,     0x9DE,       0x0,    0x1126,     0x9DB,     0x9DF,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_6_3[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		    0x98E,     0x990,     0x98D,     0x98F,     0x992,     0x994,     0x991,     0x993,
		   0x10E7,    0x10E9,    0x10E6,    0x10E8,    0x10EB,    0x10ED,    0x10EA,    0x10EC,
		   0x110F,    0x1111,    0x110E,    0x1110,    0x1113,    0x1115,    0x1112,    0x1114,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_6_poles[] = {
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,

		0x0,
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,

		2526,
		2528,
	};


	static final /*PalSpriteID*/ int _bridge_sprite_table_7_0[] = {
		0x9CD | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D9 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0, 0x9CE | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DA | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0,
		0x9D3 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D9 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0, 0x9D4 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DA | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0,
		0x10FC | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D9 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0, 0x10FD | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DA | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0,
		0x1124 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D9 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0, 0x1125 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DA | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_7_1[] = {
		0x9CB | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D7 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DD | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x9D0 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DC | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9E0 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
		0x9D1 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D7 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DD | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x9D6 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DC | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9E0 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
		0x10FA | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D7 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DD | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x10FF | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DC | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9E0 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
		0x1122 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D7 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DD | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x1127 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DC | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9E0 | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_7_2[] = {
		0x9CC | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D8 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DE | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x9CF | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DB | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DF | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
		0x9D2 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D8 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DE | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x9D5 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DB | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DF | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
		0x10FB | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D8 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DE | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x10FE | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DB | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DF | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
		0x1123 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9D8 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DE | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0, 0x1126 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DB | Sprite.PALETTE_TO_STRUCT_BROWN, 0x9DF | Sprite.PALETTE_TO_STRUCT_BROWN,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_7_3[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		0x98E | Sprite.PALETTE_TO_STRUCT_BROWN, 0x990 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x98D | Sprite.PALETTE_TO_STRUCT_BROWN, 0x98F | Sprite.PALETTE_TO_STRUCT_BROWN, 0x992 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x994 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x991 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x993 | Sprite.PALETTE_TO_STRUCT_BROWN,
		0x10E7 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x10E9 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x10E6 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x10E8 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x10EB | Sprite.PALETTE_TO_STRUCT_BROWN, 0x10ED | Sprite.PALETTE_TO_STRUCT_BROWN, 0x10EA | Sprite.PALETTE_TO_STRUCT_BROWN, 0x10EC | Sprite.PALETTE_TO_STRUCT_BROWN,
		0x110F | Sprite.PALETTE_TO_STRUCT_BROWN, 0x1111 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x110E | Sprite.PALETTE_TO_STRUCT_BROWN, 0x1110 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x1113 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x1115 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x1112 | Sprite.PALETTE_TO_STRUCT_BROWN, 0x1114 | Sprite.PALETTE_TO_STRUCT_BROWN,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_7_poles[] = {
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,

		0x0,
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,

		2526,
		2528,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_8_0[] = {
		0x9CD | Sprite.PALETTE_TO_STRUCT_RED, 0x9D9 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0, 0x9CE | Sprite.PALETTE_TO_STRUCT_RED, 0x9DA | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0,
		0x9D3 | Sprite.PALETTE_TO_STRUCT_RED, 0x9D9 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0, 0x9D4 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DA | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0,
		0x10FC | Sprite.PALETTE_TO_STRUCT_RED, 0x9D9 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0, 0x10FD | Sprite.PALETTE_TO_STRUCT_RED, 0x9DA | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0,
		0x1124 | Sprite.PALETTE_TO_STRUCT_RED, 0x9D9 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0, 0x1125 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DA | Sprite.PALETTE_TO_STRUCT_RED,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_8_1[] = {
		0x9CB | Sprite.PALETTE_TO_STRUCT_RED, 0x9D7 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DD | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x9D0 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DC | Sprite.PALETTE_TO_STRUCT_RED, 0x9E0 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
		0x9D1 | Sprite.PALETTE_TO_STRUCT_RED, 0x9D7 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DD | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x9D6 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DC | Sprite.PALETTE_TO_STRUCT_RED, 0x9E0 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
		0x10FA | Sprite.PALETTE_TO_STRUCT_RED, 0x9D7 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DD | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x10FF | Sprite.PALETTE_TO_STRUCT_RED, 0x9DC | Sprite.PALETTE_TO_STRUCT_RED, 0x9E0 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
		0x1122 | Sprite.PALETTE_TO_STRUCT_RED, 0x9D7 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DD | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x1127 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DC | Sprite.PALETTE_TO_STRUCT_RED, 0x9E0 | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_8_2[] = {
		0x9CC | Sprite.PALETTE_TO_STRUCT_RED, 0x9D8 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DE | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x9CF | Sprite.PALETTE_TO_STRUCT_RED, 0x9DB | Sprite.PALETTE_TO_STRUCT_RED, 0x9DF | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
		0x9D2 | Sprite.PALETTE_TO_STRUCT_RED, 0x9D8 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DE | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x9D5 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DB | Sprite.PALETTE_TO_STRUCT_RED, 0x9DF | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
		0x10FB | Sprite.PALETTE_TO_STRUCT_RED, 0x9D8 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DE | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x10FE | Sprite.PALETTE_TO_STRUCT_RED, 0x9DB | Sprite.PALETTE_TO_STRUCT_RED, 0x9DF | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
		0x1123 | Sprite.PALETTE_TO_STRUCT_RED, 0x9D8 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DE | Sprite.PALETTE_TO_STRUCT_RED,       0x0, 0x1126 | Sprite.PALETTE_TO_STRUCT_RED, 0x9DB | Sprite.PALETTE_TO_STRUCT_RED, 0x9DF | Sprite.PALETTE_TO_STRUCT_RED,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_8_3[] = {
		    0x986,     0x988,     0x985,     0x987,     0x98A,     0x98C,     0x989,     0x98B,
		0x98E | Sprite.PALETTE_TO_STRUCT_RED, 0x990 | Sprite.PALETTE_TO_STRUCT_RED, 0x98D | Sprite.PALETTE_TO_STRUCT_RED, 0x98F | Sprite.PALETTE_TO_STRUCT_RED, 0x992 | Sprite.PALETTE_TO_STRUCT_RED, 0x994 | Sprite.PALETTE_TO_STRUCT_RED, 0x991 | Sprite.PALETTE_TO_STRUCT_RED, 0x993 | Sprite.PALETTE_TO_STRUCT_RED,
		0x10E7 | Sprite.PALETTE_TO_STRUCT_RED, 0x10E9 | Sprite.PALETTE_TO_STRUCT_RED, 0x10E6 | Sprite.PALETTE_TO_STRUCT_RED, 0x10E8 | Sprite.PALETTE_TO_STRUCT_RED, 0x10EB | Sprite.PALETTE_TO_STRUCT_RED, 0x10ED | Sprite.PALETTE_TO_STRUCT_RED, 0x10EA | Sprite.PALETTE_TO_STRUCT_RED, 0x10EC | Sprite.PALETTE_TO_STRUCT_RED,
		0x110F | Sprite.PALETTE_TO_STRUCT_RED, 0x1111 | Sprite.PALETTE_TO_STRUCT_RED, 0x110E | Sprite.PALETTE_TO_STRUCT_RED, 0x1110 | Sprite.PALETTE_TO_STRUCT_RED, 0x1113 | Sprite.PALETTE_TO_STRUCT_RED, 0x1115 | Sprite.PALETTE_TO_STRUCT_RED, 0x1112 | Sprite.PALETTE_TO_STRUCT_RED, 0x1114 | Sprite.PALETTE_TO_STRUCT_RED,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_8_poles[] = {
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,

		0x0,
		0x0,
		0x0,
		0x0,
		0x0,
		0x0,

		2526,
		2528,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_0_0[] = {
		    0x9F2,     0x9F6,     0x9F8,       0x0,     0x9F1,     0x9F5,     0x9F7,       0x0,
		    0x9F4,     0x9F6,     0x9F8,       0x0,     0x9F3,     0x9F5,     0x9F7,       0x0,
		   0x1109,     0x9F6,     0x9F8,       0x0,    0x1108,     0x9F5,     0x9F7,       0x0,
		   0x1131,     0x9F6,     0x9F8,       0x0,    0x1130,     0x9F5,     0x9F7,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_0_1[] = {
		    0x9EE,     0x9ED,     0x9F0,     0x9EF,     0x9EA,     0x9E9,     0x9EB,     0x9EC,
		    0x9E6,     0x9E5,     0x9E8,     0x9E7,     0x9E2,     0x9E1,     0x9E3,     0x9E4,
		   0x1105,    0x1104,    0x1107,    0x1106,    0x1101,    0x1100,    0x1102,    0x1103,
		   0x112D,    0x112C,    0x112F,    0x112E,    0x1129,    0x1128,    0x112A,    0x112B,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_0_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 5,
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 1 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 1 + 2,
		0x0,

		0x0,
		0x0,
	};


	static final /*PalSpriteID*/ int _bridge_sprite_table_1_0[] = {
		0x9BD | Sprite.PALETTE_TO_STRUCT_RED, 0x9C1 | Sprite.PALETTE_TO_STRUCT_RED,     0x9C9,       0x0, 0x9BE | Sprite.PALETTE_TO_STRUCT_RED, 0x9C2 | Sprite.PALETTE_TO_STRUCT_RED,     0x9CA,       0x0,
		0x9BF | Sprite.PALETTE_TO_STRUCT_RED, 0x9C1 | Sprite.PALETTE_TO_STRUCT_RED,     0x9C9,       0x0, 0x9C0 | Sprite.PALETTE_TO_STRUCT_RED, 0x9C2 | Sprite.PALETTE_TO_STRUCT_RED,     0x9CA,       0x0,
		0x10F8 | Sprite.PALETTE_TO_STRUCT_RED, 0x9C1 | Sprite.PALETTE_TO_STRUCT_RED,     0x9C9,       0x0, 0x10F9 | Sprite.PALETTE_TO_STRUCT_RED, 0x9C2 | Sprite.PALETTE_TO_STRUCT_RED,     0x9CA,       0x0,
		0x1120 | Sprite.PALETTE_TO_STRUCT_RED, 0x9C1 | Sprite.PALETTE_TO_STRUCT_RED,     0x9C9,       0x0, 0x1121 | Sprite.PALETTE_TO_STRUCT_RED, 0x9C2 | Sprite.PALETTE_TO_STRUCT_RED,     0x9CA,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_9_0[] = {
		    0x9F9,     0x9FD,     0x9C9,       0x0,     0x9FA,     0x9FE,     0x9CA,       0x0,
		    0x9FB,     0x9FD,     0x9C9,       0x0,     0x9FC,     0x9FE,     0x9CA,       0x0,
		   0x110A,     0x9FD,     0x9C9,       0x0,    0x110B,     0x9FE,     0x9CA,       0x0,
		   0x1132,     0x9FD,     0x9C9,       0x0,    0x1133,     0x9FE,     0x9CA,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_10_0[] = {
		    0xA0B,     0xA01,       0x0,       0x0,     0xA0C,     0xA02,       0x0,       0x0,
		    0xA11,     0xA01,       0x0,       0x0,     0xA12,     0xA02,       0x0,       0x0,
		    0xA17,     0xA01,       0x0,       0x0,     0xA18,     0xA02,       0x0,       0x0,
		    0xA1D,     0xA01,       0x0,       0x0,     0xA1E,     0xA02,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_10_1[] = {
		    0xA09,     0x9FF,     0xA05,       0x0,     0xA0E,     0xA04,     0xA08,       0x0,
		    0xA0F,     0x9FF,     0xA05,       0x0,     0xA14,     0xA04,     0xA08,       0x0,
		    0xA15,     0x9FF,     0xA05,       0x0,     0xA1A,     0xA04,     0xA08,       0x0,
		    0xA1B,     0x9FF,     0xA05,       0x0,     0xA20,     0xA04,     0xA08,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_10_2[] = {
		    0xA0A,     0xA00,     0xA06,       0x0,     0xA0D,     0xA03,     0xA07,       0x0,
		    0xA10,     0xA00,     0xA06,       0x0,     0xA13,     0xA03,     0xA07,       0x0,
		    0xA16,     0xA00,     0xA06,       0x0,     0xA19,     0xA03,     0xA07,       0x0,
		    0xA1C,     0xA00,     0xA06,       0x0,     0xA1F,     0xA03,     0xA07,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_10_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 5,
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0,
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 2,
		0x0,

		0x0,
		0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_11_0[] = {
	    0xA0B | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA01 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,     0xA0C | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA02 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
	    0xA11 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA01 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,     0xA12 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA02 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
	    0xA17 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA01 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,     0xA18 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA02 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
	    0xA1D | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA01 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,     0xA1E | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA02 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_11_1[] = {
	    0xA09 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0x9FF | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA05 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA0E | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA04 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA08 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	    0xA0F | Sprite.PALETTE_TO_STRUCT_YELLOW,     0x9FF | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA05 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA14 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA04 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA08 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	    0xA15 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0x9FF | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA05 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA1A | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA04 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA08 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	    0xA1B | Sprite.PALETTE_TO_STRUCT_YELLOW,     0x9FF | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA05 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA20 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA04 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA08 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_11_2[] = {
	    0xA0A | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA00 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA06 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA0D | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA03 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA07 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	    0xA10 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA00 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA06 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA13 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA03 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA07 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	    0xA16 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA00 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA06 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA19 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA03 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA07 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	    0xA1C | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA00 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA06 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,     0xA1F | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA03 | Sprite.PALETTE_TO_STRUCT_YELLOW,     0xA07 | Sprite.PALETTE_TO_STRUCT_YELLOW,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_11_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 5 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 2 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_YELLOW),
		0x0,

		0x0,
		0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_12_0[] = {
	    0xA0B | Sprite.PALETTE_TO_STRUCT_GREY,     0xA01 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,     0xA0C | Sprite.PALETTE_TO_STRUCT_GREY,     0xA02 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,
	    0xA11 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA01 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,     0xA12 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA02 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,
	    0xA17 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA01 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,     0xA18 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA02 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,
	    0xA1D | Sprite.PALETTE_TO_STRUCT_GREY,     0xA01 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,     0xA1E | Sprite.PALETTE_TO_STRUCT_GREY,     0xA02 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_12_1[] = {
	    0xA09 | Sprite.PALETTE_TO_STRUCT_GREY,     0x9FF | Sprite.PALETTE_TO_STRUCT_GREY,     0xA05 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA0E | Sprite.PALETTE_TO_STRUCT_GREY,     0xA04 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA08 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	    0xA0F | Sprite.PALETTE_TO_STRUCT_GREY,     0x9FF | Sprite.PALETTE_TO_STRUCT_GREY,     0xA05 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA14 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA04 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA08 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	    0xA15 | Sprite.PALETTE_TO_STRUCT_GREY,     0x9FF | Sprite.PALETTE_TO_STRUCT_GREY,     0xA05 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA1A | Sprite.PALETTE_TO_STRUCT_GREY,     0xA04 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA08 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	    0xA1B | Sprite.PALETTE_TO_STRUCT_GREY,     0x9FF | Sprite.PALETTE_TO_STRUCT_GREY,     0xA05 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA20 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA04 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA08 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_12_2[] = {
	    0xA0A | Sprite.PALETTE_TO_STRUCT_GREY,     0xA00 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA06 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA0D | Sprite.PALETTE_TO_STRUCT_GREY,     0xA03 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA07 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	    0xA10 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA00 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA06 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA13 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA03 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA07 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	    0xA16 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA00 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA06 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA19 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA03 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA07 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	    0xA1C | Sprite.PALETTE_TO_STRUCT_GREY,     0xA00 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA06 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,     0xA1F | Sprite.PALETTE_TO_STRUCT_GREY,     0xA03 | Sprite.PALETTE_TO_STRUCT_GREY,     0xA07 | Sprite.PALETTE_TO_STRUCT_GREY,       0x0,
	};

	static final /*PalSpriteID*/ int _bridge_sprite_table_12_poles[] = {
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 3 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 5 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		0x0,

		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 0 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		Sprite.SPR_PILLARS_BASE + 6 * 2 + 2 + (Sprite.PALETTE_MODIFIER_COLOR | Sprite.PALETTE_TO_STRUCT_GREY),
		0x0,

		0x0,
		0x0,
	};

	public static final int[] _bridge_sprite_table_2[] = {
		_bridge_sprite_table_2_0,
		_bridge_sprite_table_2_0,
		_bridge_sprite_table_2_0,
		_bridge_sprite_table_2_0,
		_bridge_sprite_table_2_0,
		_bridge_sprite_table_2_0,
		_bridge_sprite_table_2_1,
	};

	public static final int[] _bridge_sprite_table_4[] = {
		_bridge_sprite_table_4_0,
		_bridge_sprite_table_4_1,
		_bridge_sprite_table_4_2,
		_bridge_sprite_table_4_3,
		_bridge_sprite_table_4_4,
		_bridge_sprite_table_4_5,
		_bridge_sprite_table_4_6,
	};

	public static final int[] _bridge_sprite_table_5[] = {
		_bridge_sprite_table_5_0,
		_bridge_sprite_table_5_1,
		_bridge_sprite_table_5_2,
		_bridge_sprite_table_5_3,
		_bridge_sprite_table_5_4,
		_bridge_sprite_table_5_5,
		_bridge_sprite_table_5_6,
	};

	public static final int[] _bridge_sprite_table_3[] = {
		_bridge_sprite_table_3_0,
		_bridge_sprite_table_3_1,
		_bridge_sprite_table_3_2,
		_bridge_sprite_table_3_3,
		_bridge_sprite_table_3_4,
		_bridge_sprite_table_3_5,
		_bridge_sprite_table_3_6,
	};

	public static final int[] _bridge_sprite_table_6[] = {
		_bridge_sprite_table_6_0,
		_bridge_sprite_table_6_1,
		_bridge_sprite_table_6_2,
		_bridge_sprite_table_6_2,
		_bridge_sprite_table_6_2,
		_bridge_sprite_table_6_2,
		_bridge_sprite_table_6_3,
	};

	public static final int[] _bridge_sprite_table_7[] = {
		_bridge_sprite_table_7_0,
		_bridge_sprite_table_7_1,
		_bridge_sprite_table_7_2,
		_bridge_sprite_table_7_2,
		_bridge_sprite_table_7_2,
		_bridge_sprite_table_7_2,
		_bridge_sprite_table_7_3,
	};

	public static final int[] _bridge_sprite_table_8[] = {
		_bridge_sprite_table_8_0,
		_bridge_sprite_table_8_1,
		_bridge_sprite_table_8_2,
		_bridge_sprite_table_8_2,
		_bridge_sprite_table_8_2,
		_bridge_sprite_table_8_2,
		_bridge_sprite_table_8_3,
	};

	public static final int[] _bridge_sprite_table_0[] = {
		_bridge_sprite_table_0_0,
		_bridge_sprite_table_0_0,
		_bridge_sprite_table_0_0,
		_bridge_sprite_table_0_0,
		_bridge_sprite_table_0_0,
		_bridge_sprite_table_0_0,
		_bridge_sprite_table_0_1,
	};

	public static final int[] _bridge_sprite_table_1[] = {
		_bridge_sprite_table_1_0,
		_bridge_sprite_table_1_0,
		_bridge_sprite_table_1_0,
		_bridge_sprite_table_1_0,
		_bridge_sprite_table_1_0,
		_bridge_sprite_table_1_0,
		_bridge_sprite_table_1_1,
	};

	public static final int[] _bridge_sprite_table_9[] = {
		_bridge_sprite_table_9_0,
		_bridge_sprite_table_9_0,
		_bridge_sprite_table_9_0,
		_bridge_sprite_table_9_0,
		_bridge_sprite_table_9_0,
		_bridge_sprite_table_9_0,
		_bridge_sprite_table_4_6,
	};

	public static final int[] _bridge_sprite_table_10[] = {
		_bridge_sprite_table_10_0,
		_bridge_sprite_table_10_1,
		_bridge_sprite_table_10_2,
		_bridge_sprite_table_10_2,
		_bridge_sprite_table_10_2,
		_bridge_sprite_table_10_2,
		_bridge_sprite_table_4_6,
	};

	public static final int[] _bridge_sprite_table_11[] = {
		_bridge_sprite_table_11_0,
		_bridge_sprite_table_11_1,
		_bridge_sprite_table_11_2,
		_bridge_sprite_table_11_2,
		_bridge_sprite_table_11_2,
		_bridge_sprite_table_11_2,
		_bridge_sprite_table_5_6,
	};

	public static final int [][] _bridge_sprite_table_12 = {
		_bridge_sprite_table_12_0,
		_bridge_sprite_table_12_1,
		_bridge_sprite_table_12_2,
		_bridge_sprite_table_12_2,
		_bridge_sprite_table_12_2,
		_bridge_sprite_table_12_2,
		_bridge_sprite_table_3_6,
	};

	public static final int [][][] _bridge_sprite_table = {
		_bridge_sprite_table_0,
		_bridge_sprite_table_1,
		_bridge_sprite_table_2,
		_bridge_sprite_table_3,
		_bridge_sprite_table_4,
		_bridge_sprite_table_5,
		_bridge_sprite_table_6,
		_bridge_sprite_table_7,
		_bridge_sprite_table_8,
		_bridge_sprite_table_9,
		_bridge_sprite_table_10,
		_bridge_sprite_table_11,
		_bridge_sprite_table_12
	};

	public static final int [][] _bridge_poles_table = {
		_bridge_sprite_table_0_poles,
		_bridge_sprite_table_1_poles,
		_bridge_sprite_table_2_poles,
		_bridge_sprite_table_3_poles,
		_bridge_sprite_table_4_poles,
		_bridge_sprite_table_5_poles,
		_bridge_sprite_table_6_poles,
		_bridge_sprite_table_7_poles,
		_bridge_sprite_table_8_poles,
		_bridge_sprite_table_2_poles,
		_bridge_sprite_table_10_poles,
		_bridge_sprite_table_11_poles,
		_bridge_sprite_table_12_poles
	};
	
	
	
	
	
	
	
	
	
	
	
	
}

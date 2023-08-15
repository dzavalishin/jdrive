package com.dzavalishin.tables;

import com.dzavalishin.game.Sprite;

public class RoadTables 
{

	public static class DrawRoadTileStruct {
		public final int image;
		public final int subcoord_x;
		public final int subcoord_y;

		public DrawRoadTileStruct(int i, int j, int k) {
			image = i;
			subcoord_x =  j;
			subcoord_y =  k;
		}
	} 

	public static class DrawRoadSeqStruct {
		public final int image;
		public final int subcoord_x;
		public final int subcoord_y;
		public final int width;
		public final int height;

		public DrawRoadSeqStruct(int x, int i, int j, int k, int l) {
			image = x;
			subcoord_x =  i;
			subcoord_y =  j;
			width =  k;
			height =  l;
		}

	} 



	/* $Id: road_land.h 2702 2005-07-24 15:56:31Z celestar $ */

	//#define TILE_SEQ_BEGIN(x) { x, 0, 0, 0, 0 },
	//#define TILE_SEQ_LINE(a, b, c, d, e) { a, b, c, d, e },
	//#define TILE_SEQ_END() { 0, 0, 0, 0, 0 }

	private static DrawRoadSeqStruct TILE_SEQ_BEGIN(int x) { return new DrawRoadSeqStruct(x, 0, 0, 0, 0); }
	private static DrawRoadSeqStruct TILE_SEQ_LINE(int a, int b, int c, int d, int e) 
	{ 
		return new DrawRoadSeqStruct(a, b, c, d, e); 
	}
	private static DrawRoadSeqStruct TILE_SEQ_END() { return new DrawRoadSeqStruct(0, 0, 0, 0, 0); }

	static public final  DrawRoadSeqStruct _road_display_datas_0[] = {
			TILE_SEQ_BEGIN(0xA4A),
			TILE_SEQ_LINE(0x584 | Sprite.PALETTE_MODIFIER_COLOR, 0, 15, 16, 1),
			TILE_SEQ_END(),
	};

	static public final  DrawRoadSeqStruct _road_display_datas_1[] = {
			TILE_SEQ_BEGIN(0xA4A),
			TILE_SEQ_LINE(0x580, 0, 0, 1, 16),
			TILE_SEQ_LINE(0x581 | Sprite.PALETTE_MODIFIER_COLOR, 15, 0, 1, 16),
			TILE_SEQ_END(),
	};

	static public final  DrawRoadSeqStruct _road_display_datas_2[] = {
			TILE_SEQ_BEGIN(0xA4A),
			TILE_SEQ_LINE(0x582, 0, 0, 16, 1),
			TILE_SEQ_LINE(0x583 | Sprite.PALETTE_MODIFIER_COLOR, 0, 15, 16, 1),
			TILE_SEQ_END(),
	};

	static public final  DrawRoadSeqStruct _road_display_datas_3[] = {
			TILE_SEQ_BEGIN(0xA4A),
			TILE_SEQ_LINE(0x585 | Sprite.PALETTE_MODIFIER_COLOR, 15, 0, 1, 16),
			TILE_SEQ_END(),
	};

	static public final  DrawRoadSeqStruct [] _road_display_datas[] = {
			_road_display_datas_0,
			_road_display_datas_1,
			_road_display_datas_2,
			_road_display_datas_3,
	};



	static public final  /*SpriteID*/ int  _road_tile_sprites_1[] = {
			0, 0x546, 0x545, 0x53B, 0x544, 0x534, 0x53E, 0x539,
			0x543, 0x53C, 0x535, 0x538, 0x53D, 0x537, 0x53A, 0x536
	};



	private static DrawRoadTileStruct MAKELINE(int a, int b, int c) { return new DrawRoadTileStruct( a, b, c ); }
	private static DrawRoadTileStruct ENDLINE() { return new DrawRoadTileStruct( 0, 0, 0 ); }

	static public final  DrawRoadTileStruct _road_display_datas2_0[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_1[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_2[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_3[] = {
			MAKELINE(0x57f,1,8),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_4[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_5[] = {
			MAKELINE(0x57f,1,8),
			MAKELINE(0x57e,14,8),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_6[] = {
			MAKELINE(0x57e,8,1),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_7[] = {
			MAKELINE(0x57f,1,8),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_8[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_9[] = {
			MAKELINE(0x57f,8,14),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_10[] = {
			MAKELINE(0x57f,8,14),
			MAKELINE(0x57e,8,1),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_11[] = {
			MAKELINE(0x57f,8,14),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_12[] = {
			MAKELINE(0x57e,8,1),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_13[] = {
			MAKELINE(0x57e,14,8),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_14[] = {
			MAKELINE(0x57e,8,1),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_15[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_16[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_17[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_18[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_19[] = {
			MAKELINE(0x1212,0,2),
			MAKELINE(0x1212,3,9),
			MAKELINE(0x1212,10,12),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_20[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_21[] = {
			MAKELINE(0x1212,0,2),
			MAKELINE(0x1212,0,10),
			MAKELINE(0x1212,12,2),
			MAKELINE(0x1212,12,10),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_22[] = {
			MAKELINE(0x1212,10,0),
			MAKELINE(0x1212,3,3),
			MAKELINE(0x1212,0,10),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_23[] = {
			MAKELINE(0x1212,0,2),
			MAKELINE(0x1212,0,10),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_24[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_25[] = {
			MAKELINE(0x1212,12,2),
			MAKELINE(0x1212,9,9),
			MAKELINE(0x1212,2,12),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_26[] = {
			MAKELINE(0x1212,2,0),
			MAKELINE(0x1212,10,0),
			MAKELINE(0x1212,2,12),
			MAKELINE(0x1212,10,12),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_27[] = {
			MAKELINE(0x1212,2,12),
			MAKELINE(0x1212,10,12),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_28[] = {
			MAKELINE(0x1212,2,0),
			MAKELINE(0x1212,9,3),
			MAKELINE(0x1212,12,10),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_29[] = {
			MAKELINE(0x1212,12,2),
			MAKELINE(0x1212,12,10),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_30[] = {
			MAKELINE(0x1212,2,0),
			MAKELINE(0x1212,10,0),
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_31[] = {
			ENDLINE(),
	};

	static public final  DrawRoadTileStruct _road_display_datas2_32[] = {
			ENDLINE(),
	};

	//#undef MAKELINE
	//#undef ENDLINE()

	static public final  DrawRoadTileStruct [] _road_display_table_1[] = {
			_road_display_datas2_32,_road_display_datas2_32,
			_road_display_datas2_32,_road_display_datas2_32,
			_road_display_datas2_32,_road_display_datas2_32,
			_road_display_datas2_32,_road_display_datas2_32,
			_road_display_datas2_32,_road_display_datas2_32,
			_road_display_datas2_32,_road_display_datas2_32,
			_road_display_datas2_32,_road_display_datas2_32,
			_road_display_datas2_32,_road_display_datas2_32,
	};

	static public final  DrawRoadTileStruct [] _road_display_table_2[] = {
			_road_display_datas2_0,
			_road_display_datas2_1,
			_road_display_datas2_2,
			_road_display_datas2_3,
			_road_display_datas2_4,
			_road_display_datas2_5,
			_road_display_datas2_6,
			_road_display_datas2_7,
			_road_display_datas2_8,
			_road_display_datas2_9,
			_road_display_datas2_10,
			_road_display_datas2_11,
			_road_display_datas2_12,
			_road_display_datas2_13,
			_road_display_datas2_14,
			_road_display_datas2_15,
	};

	static public final  DrawRoadTileStruct [][]  _road_display_table_3 = {
			_road_display_datas2_16,
			_road_display_datas2_17,
			_road_display_datas2_18,
			_road_display_datas2_19,
			_road_display_datas2_20,
			_road_display_datas2_21,
			_road_display_datas2_22,
			_road_display_datas2_23,

			_road_display_datas2_24,
			_road_display_datas2_25,
			_road_display_datas2_26,
			_road_display_datas2_27,
			_road_display_datas2_28,
			_road_display_datas2_29,
			_road_display_datas2_30,
			_road_display_datas2_31,
	};

	static public final  DrawRoadTileStruct [][][] _road_display_table = {
			_road_display_table_1,
			_road_display_table_1,
			_road_display_table_1,
			_road_display_table_2,
			_road_display_table_1,
			_road_display_table_3,
	};


}

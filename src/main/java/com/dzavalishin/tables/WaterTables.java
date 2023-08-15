package com.dzavalishin.tables;

import com.dzavalishin.game.Sprite;

public class WaterTables 
{

	/* $Id: water_land.h 2781 2005-08-01 16:31:19Z ludde $ */

	public static class WaterDrawTileStruct {
		public final int delta_x;
		public final int delta_y;
		public final int delta_z;
		public final int width;
		public final int height;
		public final int unk;
		//SpriteID 
		public final int image;

		public WaterDrawTileStruct(int i, int j, int k, int l, int m, int n, int img) {
			delta_x = i;
			delta_y = j;
			delta_z = k;
			width   = l;
			height  = m;
			unk     = n;
			image   = img;
		}

	}

	private static WaterDrawTileStruct BEGIN(int image) { return new WaterDrawTileStruct( 0, 0, 0, 0, 0, 0, image ); }
	private static WaterDrawTileStruct END(int y) { return new WaterDrawTileStruct( 0x80, y, 0, 0, 0, 0, 0 ); }

	static public final  WaterDrawTileStruct _shipdepot_display_seq_1[] = {
			BEGIN(0xFDD),
			new WaterDrawTileStruct( 0, 15, 0, 16, 1, 0x14, 0xFE8 | Sprite.PALETTE_MODIFIER_COLOR ),
			END(0)
	};

	static public final  WaterDrawTileStruct _shipdepot_display_seq_2[] = {
			BEGIN(0xFDD),
			new WaterDrawTileStruct( 0,  0, 0, 16, 1, 0x14, 0xFEA ),
			new WaterDrawTileStruct( 0, 15, 0, 16, 1, 0x14, 0xFE6 | Sprite.PALETTE_MODIFIER_COLOR ),
			END(0)
};

static public final  WaterDrawTileStruct _shipdepot_display_seq_3[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct( 15, 0, 0, 1, 0x10, 0x14, 0xFE9 | Sprite.PALETTE_MODIFIER_COLOR ),
		END(0)
};

static public final  WaterDrawTileStruct _shipdepot_display_seq_4[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(  0, 0, 0, 1, 16, 0x14, 0xFEB ),
		new WaterDrawTileStruct( 15, 0, 0, 1, 16, 0x14, 0xFE7 | Sprite.PALETTE_MODIFIER_COLOR ),
		END(0)
};

static public final  WaterDrawTileStruct []  _shipdepot_display_seq[] = {
		_shipdepot_display_seq_1,
		_shipdepot_display_seq_2,
		_shipdepot_display_seq_3,
		_shipdepot_display_seq_4,
};

static public final  WaterDrawTileStruct _shiplift_display_seq_0[] = {
		BEGIN(Sprite.SPR_CANALS_BASE + 6),
		new WaterDrawTileStruct(  0,    0, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 0 + 1 ),
		new WaterDrawTileStruct(  0,  0xF, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 4 + 1 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_1[] = {
		BEGIN(Sprite.SPR_CANALS_BASE + 5),
		new WaterDrawTileStruct(   0, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 0 ),
		new WaterDrawTileStruct( 0xF, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 4 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_2[] = {
		BEGIN(Sprite.SPR_CANALS_BASE + 7),
		new WaterDrawTileStruct(  0,    0, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 0 + 2 ),
		new WaterDrawTileStruct(  0,  0xF, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 4 + 2 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_3[] = {
		BEGIN(Sprite.SPR_CANALS_BASE + 8),
		new WaterDrawTileStruct(   0, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 0 + 3 ),
		new WaterDrawTileStruct( 0xF, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 4 + 3 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_0b[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(  0,    0, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 8 + 1 ),
		new WaterDrawTileStruct(  0,  0xF, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 12 + 1 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_1b[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(   0, 0, 0, 0x1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 8 ),
		new WaterDrawTileStruct( 0xF, 0, 0, 0x1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 12 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_2b[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(  0,    0, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 8 + 2 ),
		new WaterDrawTileStruct(  0,  0xF, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 12 + 2 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_3b[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(   0, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 8 + 3 ),
		new WaterDrawTileStruct( 0xF, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 12 + 3 ),
		END(0)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_0t[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(  0,    0, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 16 + 1 ),
		new WaterDrawTileStruct(  0,  0xF, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 20 + 1 ),
		END(8)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_1t[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(   0, 0, 0, 0x1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 16 ),
		new WaterDrawTileStruct( 0xF, 0, 0, 0x1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 20 ),
		END(8)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_2t[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(  0,    0, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 16 + 2 ),
		new WaterDrawTileStruct(  0,  0xF, 0, 0x10, 1, 0x14, Sprite.SPR_CANALS_BASE + 9 + 20 + 2 ),
		END(8)
};

static public final  WaterDrawTileStruct _shiplift_display_seq_3t[] = {
		BEGIN(0xFDD),
		new WaterDrawTileStruct(   0, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 16 + 3 ),
		new WaterDrawTileStruct( 0xF, 0, 0, 1, 0x10, 0x14, Sprite.SPR_CANALS_BASE + 9 + 20 + 3 ),
		END(8)
};

static public final  WaterDrawTileStruct [] _shiplift_display_seq[] = {
		_shiplift_display_seq_0,
		_shiplift_display_seq_1,
		_shiplift_display_seq_2,
		_shiplift_display_seq_3,

		_shiplift_display_seq_0b,
		_shiplift_display_seq_1b,
		_shiplift_display_seq_2b,
		_shiplift_display_seq_3b,

		_shiplift_display_seq_0t,
		_shiplift_display_seq_1t,
		_shiplift_display_seq_2t,
		_shiplift_display_seq_3t,
};



}

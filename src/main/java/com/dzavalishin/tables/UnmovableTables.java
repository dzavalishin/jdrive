package com.dzavalishin.tables;

import com.dzavalishin.game.Sprite;
import com.dzavalishin.struct.DrawTileSeqStruct;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.struct.DrawTileUnmovableStruct;

public class UnmovableTables 
{

	/* $Id: unmovable_land.h 2702 2005-07-24 15:56:31Z celestar $ */

	private static DrawTileSeqStruct TILE_SEQ_END() { return new DrawTileSeqStruct(0x80, 0, 0, 0, 0, 0, 0);  }

	protected static final DrawTileUnmovableStruct _draw_tile_unmovable_data[] = {
			new DrawTileUnmovableStruct(0xA29, 7,7, 2,2, 70, 0),
			new DrawTileUnmovableStruct(0xA2A, 4,4, 7,7, 61, 0),
	};


	static final DrawTileSeqStruct _unmovable_display_datas_0[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_1[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_2[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_3[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_4[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_5[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_6[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_7[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_8[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 20, 0xA34 | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_9[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 20, 0xA36 | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_10[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 20, 0xA38 | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_11[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_12[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 50, 0xA3B | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_13[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 50, 0xA3D | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_14[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 50, 0xA3F | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_15[] = {
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_16[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 60, 0xA42 | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_17[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 60, 0xA44 | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_18[] = {
		new DrawTileSeqStruct(   0,  0,  0, 16, 16, 60, 0xA46 | Sprite.PALETTE_MODIFIER_COLOR ),
		TILE_SEQ_END()
	};

	static final DrawTileSeqStruct _unmovable_display_datas_19[] = {
		TILE_SEQ_END()
	};

	protected static final DrawTileSprites _unmovable_display_datas[] = {
		new DrawTileSprites( 0xA2B | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_0 ),
		new DrawTileSprites( 0xA2C | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_1 ),
		new DrawTileSprites( 0xA2D | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_2 ),
		new DrawTileSprites( 0xA2E | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_3 ),
		new DrawTileSprites( 0xA2F | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_4 ),
		new DrawTileSprites( 0xA30 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_5 ),
		new DrawTileSprites( 0xA31 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_6 ),
		new DrawTileSprites( 0xA32 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_7 ),
		new DrawTileSprites( 0xA33 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_8 ),
		new DrawTileSprites( 0xA35 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_9 ),
		new DrawTileSprites( 0xA37 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_10 ),
		new DrawTileSprites( 0xA39 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_11 ),
		new DrawTileSprites( 0xA3A | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_12 ),
		new DrawTileSprites( 0xA3C | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_13 ),
		new DrawTileSprites( 0xA3E | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_14 ),
		new DrawTileSprites( 0xA40 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_15 ),
		new DrawTileSprites( 0xA41 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_16 ),
		new DrawTileSprites( 0xA43 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_17 ),
		new DrawTileSprites( 0xA45 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_18 ),
		new DrawTileSprites( 0xA47 | Sprite.PALETTE_MODIFIER_COLOR, _unmovable_display_datas_19 ),
	};
	
}

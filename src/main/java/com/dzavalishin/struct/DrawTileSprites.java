package com.dzavalishin.struct;

public class DrawTileSprites 
{
	//SpriteID 
	public int ground_sprite;
	public DrawTileSeqStruct [] seq;

	
	// Iterate through all DrawTileSeqStructs in DrawTileSprites.
	//#define foreach_draw_tile_seq(idx, list) for (idx = list; ((byte) idx->delta_x) != 0x80; idx++)

	
	public DrawTileSprites(int spr, DrawTileSeqStruct[] seq) {
		ground_sprite = spr;
		this.seq = seq;
		// TODO Auto-generated constructor stub
	}
	
}

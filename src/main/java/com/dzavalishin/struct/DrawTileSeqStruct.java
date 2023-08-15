package com.dzavalishin.struct;

/* The following describes bunch of sprites to be drawn together in a single 3D
 * bounding box. Used especially for various multi-sprite buildings (like
 * depots or stations): */

public class DrawTileSeqStruct 
{
	public int delta_x; // 0x80 is sequence terminator
	public int delta_y;
	public int delta_z;
	public int width;
	public int height;
	public int unk; // 'depth', just z-size; TODO: rename
	public int image;

	public DrawTileSeqStruct(
			int delta_x, // 0x80 is sequence terminator
			int delta_y,
			int delta_z,
			int width,
			int height,
			int unk, // 'depth', just z-size, TODO: rename
			int image
			) {
		this.delta_x = delta_x;
		this.delta_y = delta_y;
		this.delta_z = delta_z;
		this.width = width;
		this.height = height;
		this.unk = unk;
		this.image = image;
	}

	public DrawTileSeqStruct(DrawTileSeqStruct src) 
	{
		this.delta_x = src.delta_x;
		this.delta_y = src.delta_y;
		this.delta_z = src.delta_z;
		this.width = src.width;
		this.height = src.height;
		this.unk = src.unk;
		this.image = src.image;
	}
}

// see also DrawTileSprites
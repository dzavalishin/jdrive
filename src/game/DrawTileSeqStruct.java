package game;

/* The following describes bunch of sprites to be drawn together in a single 3D
 * bounding box. Used especially for various multi-sprite buildings (like
 * depots or stations): */

public class DrawTileSeqStruct 
{
	byte delta_x; // 0x80 is sequence terminator
	byte delta_y;
	byte delta_z;
	byte width,height;
	byte unk; // 'depth', just z-size; TODO: rename
	int  image;
}

// see also DrawTileSprites
package com.dzavalishin.game;

import com.dzavalishin.ids.SpriteID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.struct.RailCursors;
import com.dzavalishin.struct.RailGuiSprites;

public class RailtypeInfo 
{

	public static final int SIG_SEMAPHORE_MASK = (1 << 3);

	// these are the maximums used for updating signal blocks, and checking if a depot is in a pbs block
	public static final int NUM_SSD_ENTRY = 256; // max amount of blocks
	public static final int NUM_SSD_STACK = 32;// max amount of blocks to check recursively

	/** Struct containing the main sprites. @note not all sprites are listed, but only
	 *  the ones used directly in the code */
	public final RailBaseSprites base_sprites;

	/** struct containing the sprites for the rail GUI. @note only sprites referred to
	 * directly in the code are listed */
	public final RailGuiSprites gui_sprites;

	public final RailCursors cursor;

	public final StringID toolbar_caption;

	/** sprite number difference between a piece of track on a snowy ground and the corresponding one on normal ground */
	public final SpriteID snow_offset;

	/** bitmask to the OTHER railtypes that can be used by an engine of THIS railtype */
	public final byte compatible_railtypes;

	/**
	 * Offset between the current railtype and normal rail. This means that:<p>
	 * 1) All the sprites in a railset MUST be in the same order. This order
	 *    is determined by normal rail. Check sprites 1005 and following for this order<p>
	 * 2) The position where the railtype is loaded must always be the same, otherwise
	 *    the offset will fail.<p>
	 *
	 * @apiNote  Something more flexible might be desirable in the future.
	 */
	public final SpriteID total_offset;

	/**
	 * Bridge offset
	 */
	public final SpriteID bridge_offset;



	public RailtypeInfo(
			int[] base, 
			int[] gui, 
			int[] cursors, 
			int captionStr, 
			int snowOffset,
			int i, int j, int k) 
	{
		base_sprites = new RailBaseSprites(base);
		gui_sprites = new RailGuiSprites(gui);
		cursor = new RailCursors(cursors);

		toolbar_caption = new StringID(captionStr);
		snow_offset = SpriteID.get(snowOffset);
		compatible_railtypes = (byte) i;
		total_offset = SpriteID.get(j);
		bridge_offset = SpriteID.get(k);

	}


	// ------------------------------------------------------------



} 




class RailBaseSprites {
	final SpriteID track_y;      ///< single piece of rail in Y direction, with ground
	final SpriteID track_ns;     ///< two pieces of rail in North and South corner (East-West direction)
	final SpriteID ground;       ///< ground sprite for a 3-way switch
	final SpriteID single_y;     ///< single piece of rail in Y direction, without ground
	final SpriteID single_x;     ///< single piece of rail in X direction
	final SpriteID single_n;     ///< single piece of rail in the northern corner
	final SpriteID single_s;     ///< single piece of rail in the southern corner
	final SpriteID single_e;     ///< single piece of rail in the eastern corner
	final SpriteID single_w;     ///< single piece of rail in the western corner
	final SpriteID crossing;     ///< level crossing, rail in X direction
	final SpriteID tunnel;       ///< tunnel sprites base

	public RailBaseSprites(int[] spr) 
	{
		int i = 0;

		track_y = SpriteID.get( spr[i++]);      
		track_ns = SpriteID.get( spr[i++]);     
		ground = SpriteID.get( spr[i++]);       
		single_y = SpriteID.get( spr[i++]);     
		single_x = SpriteID.get( spr[i++]);     
		single_n = SpriteID.get( spr[i++]);     
		single_s = SpriteID.get( spr[i++]);     
		single_e = SpriteID.get( spr[i++]);     
		single_w = SpriteID.get( spr[i++]);     
		crossing = SpriteID.get( spr[i++]);     
		tunnel = SpriteID.get( spr[i++]);       
	}
} 





 


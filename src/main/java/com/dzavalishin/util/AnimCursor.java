package com.dzavalishin.util;

/** 
 * 
 * This class defines all the the animated cursors.
 * <p> 
 * Animated cursors consist of the number of sprites that are
 * displayed in a round-robin manner. Each sprite also has a time
 * associated that indicates how many ticks the corresponding sprite
 * is to be displayed.
 * <p> 
 * All these arrays end up in an array of pointers called _animcursors.
 * 
 */
public class AnimCursor {

	public final int spriteId;
	public final int time;
	

	/** 
	 * Creates array entry that defines one status of the cursor.
	 * 
	 *  @param spriteId The Sprite to be displayed
	 *  @param time The Number of ticks to display the sprite
	 */
	public AnimCursor(int spriteId, int time) 
	{
		this.spriteId = spriteId;
		this.time = time;

	}


	/** Animated cursor elements for demolishion
	 */
	static final AnimCursor _demolish_animcursor[] = {
		new AnimCursor(0x2C0, 29),
		new AnimCursor(0x2C1, 29),
		new AnimCursor(0x2C2, 29),
		new AnimCursor(0x2C3, 29)
	};

	/** Animated cursor elements for lower land
	 */
	static final AnimCursor _lower_land_animcursor[] = {
		new AnimCursor(0x2BB, 29),
		new AnimCursor(0x2BC, 29),
		new AnimCursor(0x2BD, 98)
	};

	/** Animated cursor elements for raise land
	 */
	static final AnimCursor _raise_land_animcursor[] = {
		new AnimCursor(0x2B8, 29),
		new AnimCursor(0x2B9, 29),
		new AnimCursor(0x2BA, 98)
	};

	/** Animated cursor elements for the goto icon
	 */
	static final AnimCursor _pick_station_animcursor[] = {
		new AnimCursor(0x2CC, 29),
		new AnimCursor(0x2CD, 29),
		new AnimCursor(0x2CE, 98),
	};

	/** Animated cursor elements for the build signal icon
	 */
	static final AnimCursor _build_signals_animcursor[] = {
		new AnimCursor(0x50C, 29),
		new AnimCursor(0x50D, 29),
	};

	/** This is an array of pointers to all the animated cursor
	 *  definitions we have above. This is the only thing that is
	 *  accessed directly from other files
	 */
	public static final AnimCursor [][] _animcursors = {
		_demolish_animcursor,
		_lower_land_animcursor,
		_raise_land_animcursor,
		_pick_station_animcursor,
		_build_signals_animcursor
	};
	
	
}

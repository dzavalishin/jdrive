package com.dzavalishin.game;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.SpriteID;
import com.dzavalishin.util.Pixel;
import com.dzavalishin.util.Sprites;

public class Sprite extends Sprites {

	int info;
	int height;
	int width;
	int x_offs;
	int y_offs;
	byte data[];

	public Sprite(int dataSize) 
	{
		info = height = 0;
		width = x_offs = y_offs = 0;
		data = new byte[dataSize];	
	}

	public Sprite() 
	{
		info = height = 0;
		width = x_offs = y_offs = 0;
		data = null;	
	}


	//typedef enum RandomizedSpriteGroupCompareMode {
	//public static final int RSG_CMP_ANY = 0;
	//public static final int RSG_CMP_ALL = 1;
	//} RandomizedSpriteGroupCompareMode;


	// It is natural to have it here
	public static Sprite GetSprite(SpriteID sprite)
	{
		return SpriteCache.GetSprite(sprite);
	}
	public static Sprite GetSprite(int sprite)
	{
		return SpriteCache.GetSprite(sprite);
	}

	public static void UnloadSpriteGroup(SpriteGroup group) {
		// free() is not needed in Java
	}

	public static int PLAYER_SPRITE_COLOR(PlayerID player) {
		return  (Global.gs._player_colors[player.id] + 0x307) << PALETTE_SPRITE_START;
	}

	public static int PLAYER_SPRITE_COLOR(int player) {
		return  (Global.gs._player_colors[player] + 0x307) << PALETTE_SPRITE_START;
	}
	
	public static int SPRITE_PALETTE(int color) {
		return color | PALETTE_MODIFIER_COLOR;
	}

	public static boolean is_custom_sprite(int x) { return x >= 0xFD; }
	public static boolean IS_CUSTOM_FIRSTHEAD_SPRITE(int x)  { return (x == 0xFD); }
	public static boolean IS_CUSTOM_SECONDHEAD_SPRITE(int x)  { return (x == 0xFE);	 }

	public static int RET_MAKE_TRANSPARENT(int image) 
	{
		return (image & SPRITE_MASK) | PALETTE_TO_TRANSPARENT;		
	}







	static SpriteGroup EvalDeterministicSpriteGroup(final DeterministicSpriteGroup dsg, int value)
	{
		int i;

		value >>= dsg.shift_num; // This should bring us to the byte range.
		value &= dsg.and_mask;

		if (dsg.operation != DeterministicSpriteGroupOperation.DSG_OP_NONE)
			value +=  dsg.add_val;

		switch (dsg.operation) {
		case DSG_OP_DIV:
			value /=  dsg.divmod_val;
			break;
		case DSG_OP_MOD:
			value %=  dsg.divmod_val;
			break;
		case DSG_OP_NONE:
			break;
		}

		for (i = 0; i < dsg.num_ranges; i++) {
			DeterministicSpriteGroupRange range = dsg.ranges[i];

			if (range.low <= value && value <= range.high)
				return range.group;
		}

		return dsg.default_group;
	}

	static int GetDeterministicSpriteValue(int ivar)
	{
		switch (ivar) {
		case 0x00:
			return Global.get_date();
		case 0x01:
			return Global.get_cur_year();
		case 0x02:
			return Global.get_cur_month();
		case 0x03:
			return GameOptions._opt.landscape;
		case 0x09:
			return Global.get_date_fract();
		case 0x0A:
			return Global._tick_counter;
		case 0x0C:
			/* If we got here, it means there was no callback or
			 * callbacks aren't supported on our callpath. */
			return 0;
		default:
			return -1;
		}
	}

	//static SpriteGroup [] EvalRandomizedSpriteGroup(final RandomizedSpriteGroup rsg, byte random_bits)
	static SpriteGroup EvalRandomizedSpriteGroup(final RandomizedSpriteGroup rsg, int random_bits)
	{
		int mask;
		int index;

		/* Noone likes mangling with bits, but you don't get around it here.
		 * Sorry. --pasky */
		// rsg.num_groups is always power of 2
		mask = (rsg.num_groups - 1) << rsg.lowest_randbit;
		index = (random_bits & mask) >> rsg.lowest_randbit;
		assert(index < rsg.num_groups);
		return rsg.groups[index];
	}

	static int RandomizedSpriteGroupTriggeredBits(final RandomizedSpriteGroup rsg,
			int triggers, byte [] waiting_triggers)
	{
		int match = rsg.triggers & (waiting_triggers[0] | triggers);
		boolean res;

		if (rsg.cmp_mode == RandomizedSpriteGroupCompareMode.RSG_CMP_ANY) {
			res = (match != 0);
		} else { /* RSG_CTileTypes.MP_ALL */
			res = (match == rsg.triggers);
		}

		if (!res) {
			waiting_triggers[0] |= triggers;
			return 0;
		}

		waiting_triggers[0] &= ~match;

		return (rsg.num_groups - 1) << rsg.lowest_randbit;
	}

	/**
	 * Traverse a sprite group and release its and its child's memory.
	 * A group is only released if its reference count is zero.
	 * We pass a pointer to a pointer so that the original reference can be set to null.
	 * @param group_ptr Pointer to sprite group reference.
	 */
	static void UnloadSpriteGroup(SpriteGroup [][]group_ptr)
	{
		/*
		SpriteGroup []group;
		int i;

		assert(group_ptr != null);
		assert(group_ptr[0] != null);

		group = *group_ptr;
		 *group_ptr = null; // Remove this reference.

		group.ref_count--;
		if (group.ref_count > 0) {
			Global.DEBUG_grf( 6, "UnloadSpriteGroup: Group at `%p' (type %d) has %d reference(s) left.", group, group.type, group.ref_count);
			return; // Still some references left, so don't clear up.
		}

		Global.DEBUG_grf( 6, "UnloadSpriteGroup: Releasing group at `%p'.", group);
		switch (group.type) {
			case SGT_REAL:
			{
				RealSpriteGroup *rsg = &group.g.real;
				for (i = 0; i < rsg.loading_count; i++) {
					if (rsg.loading[i] != null) UnloadSpriteGroup(&rsg.loading[i]);
				}
				for (i = 0; i < rsg.loaded_count; i++) {
					if (rsg.loaded[i] != null) UnloadSpriteGroup(&rsg.loaded[i]);
				}
				free(group);
				return;
			}

			case SGT_DETERMINISTIC:
			{
				DeterministicSpriteGroup *dsg = &group.g.determ;
				for (i = 0; i < group.g.determ.num_ranges; i++) {
					if (dsg.ranges[i].group != null) UnloadSpriteGroup(&dsg.ranges[i].group);
				}
				if (dsg.default_group != null) UnloadSpriteGroup(&dsg.default_group);
				free(group.g.determ.ranges);
				free(group);
				return;
			}

			case SGT_RANDOMIZED:
			{
				for (i = 0; i < group.g.random.num_groups; i++) {
					if (group.g.random.groups[i] != null) UnloadSpriteGroup(&group.g.random.groups[i]);
				}
				free(group.g.random.groups);
				free(group);
				return;
			}

			case SGT_CALLBACK:
			case SGT_RESULT:
				free(group);
				return;
		}

		Global.DEBUG_grf( 1, "Unable to remove unknown sprite group type `0x%x'.", group.type);
		 */
	}

	public int getHeight() { return height;	}
	public int getWidth() {		return width;	}

	public int getX_offs() {		return x_offs;	}
	public int getY_offs() {		return y_offs;	}

	public int getInfo() {		return info;	}
	//public byte[] getData() {		return data;	}

	public Pixel getPointer() { return new Pixel( data ); }




}

// User should decide by object type
class DataCarrier extends Sprite
{
	public DataCarrier(byte [] data) {
		super();
		this.data = data;
		this.info =  0xFF;
	}
}



class SpriteGroup {
	SpriteGroupType type;
	byte ref_count;

	/*
	union {
		RealSpriteGroup real;
		DeterministicSpriteGroup determ;
		RandomizedSpriteGroup random;
		CallbackResultSpriteGroup callback;
		ResultSpriteGroup result;
	} g; */
}



/* The following describes bunch of sprites to be drawn together in a single 3D
 * bounding box. Used especially for various multi-sprite buildings (like
 * depots or stations): */


class RealSpriteGroup extends SpriteGroup {
	// Would anyone ever need more than 16 spritesets? Maybe we should
	// use even less, now we take whole 8kb for custom sprites table, oh my!
	int sprites_per_set; // means number of directions - 4 or 8

	// Loaded = in motion, loading = not moving
	// Each group contains several spritesets, for various loading stages

	// XXX: For stations the meaning is different - loaded is for stations
	// with small amount of cargo whilst loading is for stations with a lot
	// of da stuff.

	//byte loaded_count;
	final SpriteGroup [] loaded = new SpriteGroup[16]; // sprite ids
	//byte loading_count;
	final SpriteGroup [] loading = new SpriteGroup[16]; // sprite ids
	
	public int loaded_count() { return loaded.length; }
	public int loading_count() { return loading.length; }
}

/* Shared by deterministic and random groups. */
enum VarSpriteGroupScope {
	VSG_SCOPE_SELF,
	// Engine of consists for vehicles, city for stations.
	VSG_SCOPE_PARENT,
}

//class DeterministicSpriteGroupRanges DeterministicSpriteGroupRanges;

enum DeterministicSpriteGroupOperation {
	DSG_OP_NONE,
	DSG_OP_DIV,
	DSG_OP_MOD;
	
	static final DeterministicSpriteGroupOperation[] values = values();
} 

//class DeterministicSpriteGroupRange DeterministicSpriteGroupRange;

class DeterministicSpriteGroup extends SpriteGroup {
	// Take this variable:
	VarSpriteGroupScope var_scope;
	int variable;
	byte parameter; ///< Used for variables between 0x60 and 0x7F inclusive.

	// Do this with it:
	byte shift_num;
	byte and_mask;

	// Then do this with it:
	DeterministicSpriteGroupOperation operation;
	byte add_val;
	byte divmod_val;

	// And apply it to this:
	byte num_ranges;
	DeterministicSpriteGroupRange [] ranges; // Dynamically allocated

	// Dynamically allocated, this is the sole owner
	SpriteGroup default_group;
}

enum RandomizedSpriteGroupCompareMode {
	RSG_CMP_ANY,
	RSG_CMP_ALL;
	
	static final RandomizedSpriteGroupCompareMode [] values = values();
}

class RandomizedSpriteGroup extends SpriteGroup {
	// Take this object:
	VarSpriteGroupScope var_scope;

	// Check for these triggers:
	RandomizedSpriteGroupCompareMode cmp_mode;
	byte triggers;

	// Look for this in the per-object randomized bitmask:
	byte lowest_randbit;
	byte num_groups; // must be power of 2

	// Take the group with appropriate index:
	//SpriteGroup [][]groups;
	SpriteGroup []groups;
}

class CallbackResultSpriteGroup extends SpriteGroup {
	int result;
}

class ResultSpriteGroup extends SpriteGroup {
	int result;
	int sprites;
	
	/**
	 * Creates a spritegroup representing a callback result
	 * @param value The value that was used to represent this callback result
	 * @return A spritegroup representing that callback result
	 */
	static ResultSpriteGroup NewCallBackResultSpriteGroup(int value)
	{
		ResultSpriteGroup group = new ResultSpriteGroup(); //calloc(1, sizeof(*group));

		group.type = SpriteGroupType.SGT_CALLBACK;

		// Old style callback results have the highest byte 0xFF so signify it is a callback result
		// New style ones only have the highest bit set (allows 15-bit results, instead of just 8)
		if ((value >> 8) == 0xFF)
			value &= 0xFF;
		else
			value &= ~0x8000;

		group.result = value;

		return group;
	}

	/**
	 * Creates a spritegroup representing a sprite number result.
	 * @param value The sprite number.
	 * @param sprites The number of sprites per set.
	 * @return A spritegroup representing the sprite number result.
	 */
	static ResultSpriteGroup NewResultSpriteGroup(int value, int sprites)
	{
		ResultSpriteGroup group = new ResultSpriteGroup();
		group.type = SpriteGroupType.SGT_RESULT;
		group.result = value;
		group.sprites = sprites;
		return group;
	}
	
}

enum SpriteGroupType {
	SGT_REAL,
	SGT_DETERMINISTIC,
	SGT_RANDOMIZED,
	SGT_CALLBACK,
	SGT_RESULT,
}


class DeterministicSpriteGroupRange {
	SpriteGroup group;
	byte low;
	byte high;
}

/* This takes value (probably of the variable specified in the group) and
 * chooses corresponding SpriteGroup accordingly to the given
 * DeterministicSpriteGroup. */
//SpriteGroup *EvalDeterministicSpriteGroup(const DeterministicSpriteGroup *dsg, int value);
/* Get value of a common deterministic SpriteGroup variable. */
//int GetDeterministicSpriteValue(byte var);

/* This takes randomized bitmask (probably associated with
 * vehicle/station/whatever) and chooses corresponding SpriteGroup
 * accordingly to the given RandomizedSpriteGroup. */
//SpriteGroup *EvalRandomizedSpriteGroup(const RandomizedSpriteGroup *rsg, byte random_bits);
/* Triggers given RandomizedSpriteGroup with given bitmask and returns and-mask
 * of random bits to be reseeded, or zero if there were no triggers matched
 * (then they are |ed to @waiting_triggers instead). */
//byte RandomizedSpriteGroupTriggeredBits(const RandomizedSpriteGroup *rsg, byte triggers, byte *waiting_triggers);

//void UnloadSpriteGroup(SpriteGroup **group_ptr);

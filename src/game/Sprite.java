package game;

import game.util.Sprites;

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
	
	// It is natural to have it here
	public static Sprite GetSprite(SpriteID sprite)
	{
		return SpriteCache.GetSprite(sprite);
	}
	public static Sprite GetSprite(int sprite)
	{
		return SpriteCache.GetSprite(sprite);
	}
}

// User should decide by object type
class DataCarrier extends Sprite
{
	public DataCarrier(byte [] data) {
		super();
		this.data = data;
		this.info = (byte) 0xFF;
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
};



/* The following describes bunch of sprites to be drawn together in a single 3D
 * bounding box. Used especially for various multi-sprite buildings (like
 * depots or stations): */


class RealSpriteGroup extends SpriteGroup {
	// XXX: Would anyone ever need more than 16 spritesets? Maybe we should
	// use even less, now we take whole 8kb for custom sprites table, oh my!
	byte sprites_per_set; // means number of directions - 4 or 8

	// Loaded = in motion, loading = not moving
	// Each group contains several spritesets, for various loading stages

	// XXX: For stations the meaning is different - loaded is for stations
	// with small amount of cargo whilst loading is for stations with a lot
	// of da stuff.

	//byte loaded_count;
	SpriteGroup [] loaded = new SpriteGroup[16]; // sprite ids
	//byte loading_count;
	SpriteGroup [] loading = new SpriteGroup[16]; // sprite ids
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
	DSG_OP_MOD,
} 

//class DeterministicSpriteGroupRange DeterministicSpriteGroupRange;

class DeterministicSpriteGroup extends SpriteGroup {
	// Take this variable:
	VarSpriteGroupScope var_scope;
	byte variable;
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
	RSG_CMP_ALL,
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
	SpriteGroup [][]groups;
}

class CallbackResultSpriteGroup extends SpriteGroup {
	int result;
}

class ResultSpriteGroup extends SpriteGroup {
	int result;
	int sprites;
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

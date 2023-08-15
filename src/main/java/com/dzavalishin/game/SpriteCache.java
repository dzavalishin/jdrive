package com.dzavalishin.game;

import com.dzavalishin.ids.SpriteID;
import com.dzavalishin.util.FileIO;

public class SpriteCache {


	final static int SPRITE_CACHE_SIZE = 1024*1024;

	static final Sprite[] _sprite_ptr = new Sprite[Sprite.MAX_SPRITES];
	static final int [] _sprite_file_pos = new int[Sprite.MAX_SPRITES];



	private static boolean ReadSpriteHeaderSkipData() {
		int num = FileIO.FioReadWord();
		int type; // treat as unsigned, thus int

		if (num == 0)
			return false;

		type = FileIO.FioReadByte();
		if (type == 0xFF) {
			//Global.debug("nonspr %d", num);
			FileIO.FioSkipBytes(num);
			return true;
		}

		FileIO.FioSkipBytes(7);
		num -= 8;

		//Global.debug("spr %d", num);

		if (num == 0)
			return true;

		if (0 != (type & 2)) {
			FileIO.FioSkipBytes(num);
		} else {
			while (num > 0) {
				byte i = (byte) FileIO.FioReadByte();
				if (i >= 0) {
					num -= i;
					FileIO.FioSkipBytes(i);
				} else {
					i = (byte) -(i >> 3);
					num -= i;
					FileIO.FioReadByte();
				}
			}
		}

		return true;
	}


	private static Sprite ReadSprite(int id)
	{
		int num;
		int type; // treated as unsigned, thus int

		//Global.DEBUG_spritecache( 9, "load sprite %d", id);

		if (_sprite_file_pos[id] == 0 && id != 0) {
			Global.error(
				"Tried to load non-existing sprite #%d.\n"+
				"Probable cause: Wrong/missing NewGRFs",
				id
			);
		}

		FileIO.FioSeekToFile(_sprite_file_pos[id]);

		num  = FileIO.FioReadWord();
		type = FileIO.FioReadByte();
		if (type == 0xFF) {
			byte[] dest = FileIO.FioReadBlock(num);

			// Type == 0xFF is non-sprite used to keep some data
			// Create and return special sprite
			return new DataCarrier(dest);
		} else {
			int height = FileIO.FioReadByte();
			int width  = FileIO.FioReadWord();
			Sprite sprite;
			byte[] dest;

			num = (0 != (type & 0x02)) ? width * height : (num - 8);
			sprite = new Sprite(num); // AllocSprite(sizeof(*sprite) + num);
			//sprite = new Sprite(); // AllocSprite(sizeof(*sprite) + num);
			_sprite_ptr[id] = sprite;
			sprite.info   = type;
			sprite.height = ((id != 142) ? height : 10); // Compensate for a TTD bug
			sprite.width  = width;
			sprite.x_offs = FileIO.FioReadSignedWord();
			sprite.y_offs = FileIO.FioReadSignedWord();

			dest = sprite.data;
			int di = 0;
			while (num > 0) {
				byte i = (byte) FileIO.FioReadByte(); // treat as signed!

				if (i >= 0) {
					num -= i;
					for (; i > 0; --i) 
						dest[di++] = (byte) FileIO.FioReadByte();
				} else {
					//const byte* rel = dest - (((i & 7) << 8) | FileIO.FioReadByte());
					int relp = di - (((i & 7) << 8) | FileIO.FioReadByte());

					i = (byte) -(i >> 3);
					num -= i;

					for (; i > 0; --i) 
						dest[di++] = dest[relp++];
				}
			}

			return sprite;
		}
	}

	public static boolean LoadNextSprite(int load_index, int file_index) {
		int file_pos = (int) (FileIO.FioGetPos() | ( (file_index & 0xFF) << 24));

		if (!ReadSpriteHeaderSkipData())
			return false;

		_sprite_file_pos[load_index] = file_pos;

		_sprite_ptr[load_index] = null;

		// #if defined(WANT_NEW_LRU)
		//_sprite_lru_new[load_index] = 0;
		/*
		 * #else _sprite_lru[load_index] = 0xFFFF; _sprite_lru_cur[load_index] = 0;
		 * #endif
		 */

		return true;
	}

	public static void DupSprite(int old, int newp) {
		_sprite_file_pos[newp] = _sprite_file_pos[old];
		_sprite_ptr[newp] = null;
	}

	public static void SkipSprites(int count) {
		for (; count > 0; --count) {
			if (!ReadSpriteHeaderSkipData())
				return;
		}
	}

/*
	#define S_FREE_MASK 1

	static inline MemBlock* NextBlock(MemBlock* block)
	{
		return (MemBlock*)((byte*)block + (block.size & ~S_FREE_MASK));
	}

	static int GetSpriteCacheUsage()
	{
		size_t tot_size = 0;
		MemBlock* s;

		for (s = _spritecache_ptr; s.size != 0; s = NextBlock(s))
			if (!(s.size & S_FREE_MASK)) tot_size += s.size;

		return tot_size;
	}


	void IncreaseSpriteLRU()
	{
		int i;

		// Increase all LRU values
	//#if defined(WANT_NEW_LRU)
		if (_sprite_lru_counter > 16384) {
			DEBUG(spritecache, 2) ("fixing lru %d, inuse=%d", _sprite_lru_counter, GetSpriteCacheUsage());

			for (i = 0; i != MAX_SPRITES; i++)
				if (_sprite_ptr[i] != null) {
					if (_sprite_lru_new[i] >= 0) {
						_sprite_lru_new[i] = -1;
					} else if (_sprite_lru_new[i] != -32768) {
						_sprite_lru_new[i]--;
					}
				}
			_sprite_lru_counter = 0;
		}
	/*#else
		for (i = 0; i != MAX_SPRITES; i++)
			if (_sprite_ptr[i] != null && _sprite_lru[i] != 65535)
				_sprite_lru[i]++;
		// Reset the lru counter.
		_sprite_lru_counter = 0;
	#endif* /

		// Compact sprite cache every now and then.
		if (++_compact_cache_counter >= 740) {
			CompactSpriteCache();
			_compact_cache_counter = 0;
		}
	}

	// Called when holes in the sprite cache should be removed.
	// That is accomplished by moving the cached data.
	static void CompactSpriteCache()
	{
		MemBlock *s;

		DEBUG(spritecache, 2) (
			"compacting sprite cache, inuse=%d", GetSpriteCacheUsage()
		);

		for (s = _spritecache_ptr; s.size != 0;) {
			if (s.size & S_FREE_MASK) {
				MemBlock* next = NextBlock(s);
				MemBlock temp;
				void** i;

				// Since free blocks are automatically coalesced, this should hold true.
				assert(!(next.size & S_FREE_MASK));

				// If the next block is the sentinel block, we can safely return
				if (next.size == 0)
					break;

				// Locate the sprite belonging to the next pointer.
				for (i = _sprite_ptr; *i != next.data; ++i) {
					assert(i != endof(_sprite_ptr));
				}

				*i = s.data; // Adjust sprite array entry
				// Swap this and the next block
				temp = *s;
				memmove(s, next, next.size);
				s = NextBlock(s);
				*s = temp;

				// Coalesce free blocks
				while (NextBlock(s).size & S_FREE_MASK) {
					s.size += NextBlock(s).size & ~S_FREE_MASK;
				}
			} else {
				s = NextBlock(s);
			}
		}
	}

	static void DeleteEntryFromSpriteCache()
	{
		int i;
		int best = -1;
		MemBlock* s;
		int cur_lru;

		DEBUG(spritecache, 2) ("DeleteEntryFromSpriteCache, inuse=%d", GetSpriteCacheUsage());

	//#if defined(WANT_NEW_LRU)
		cur_lru = 0xffff;
		for (i = 0; i != MAX_SPRITES; i++) {
			if (_sprite_ptr[i] != null && _sprite_lru_new[i] < cur_lru) {
				cur_lru = _sprite_lru_new[i];
				best = i;
			}
		}
	/*#else
		{
		int cur_lru = 0, cur_lru_cur = 0xffff;
		for (i = 0; i != MAX_SPRITES; i++) {
			if (_sprite_ptr[i] == null || _sprite_lru[i] < cur_lru) continue;

			// Found a sprite with a higher LRU value, then remember it.
			if (_sprite_lru[i] != cur_lru) {
				cur_lru = _sprite_lru[i];
				best = i;

			// Else if both sprites were very recently referenced, compare by the cur value instead.
			} else if (cur_lru == 0 && _sprite_lru_cur[i] <= cur_lru_cur) {
				cur_lru_cur = _sprite_lru_cur[i];
				cur_lru = _sprite_lru[i];
				best = i;
			}
		}
		}
	#endif* /

		// Display an error message and die, in case we found no sprite at all.
		// This shouldn't really happen, unless all sprites are locked.
		if (best == -1)
			Global.error("Out of sprite memory");

		// Mark the block as free (the block must be in use)
		s = (MemBlock*)_sprite_ptr[best] - 1;
		assert(!(s.size & S_FREE_MASK));
		s.size |= S_FREE_MASK;
		_sprite_ptr[best] = null;

		// And coalesce adjacent free blocks
		for (s = _spritecache_ptr; s.size != 0; s = NextBlock(s)) {
			if (s.size & S_FREE_MASK) {
				while (NextBlock(s).size & S_FREE_MASK) {
					s.size += NextBlock(s).size & ~S_FREE_MASK;
				}
			}
		}
	}

	private void* AllocSprite(size_t mem_req)
	{
		mem_req += sizeof(MemBlock);

		//* Align this to an int boundary. This also makes sure that the 2 least bits are not used, so we could use those for other things. * /
		mem_req = ALIGN(mem_req, sizeof(int));

		for (;;) {
			MemBlock* s;

			for (s = _spritecache_ptr; s.size != 0; s = NextBlock(s)) {
				if (s.size & S_FREE_MASK) {
					size_t cur_size = s.size & ~S_FREE_MASK;

					//* Is the block exactly the size we need or big enough for an additional free block? * /
					if (cur_size == mem_req ||
							cur_size >= mem_req + sizeof(MemBlock)) {
						// Set size and in use
						s.size = mem_req;

						// Do we need to inject a free block too?
						if (cur_size != mem_req) {
							NextBlock(s).size = (cur_size - mem_req) | S_FREE_MASK;
						}

						return s.data;
					}
				}
			}

			// Reached sentinel, but no block found yet. Delete some old entry.
			DeleteEntryFromSpriteCache();
		}
	}
*/
	/*
	#if defined(NEW_ROTATION)
	#define X15(x) else if (s >= x && s < (x+15)) { s = _rotate_tile_sprite[s - x] + x; }
	#define X19(x) else if (s >= x && s < (x+19)) { s = _rotate_tile_sprite[s - x] + x; }
	#define MAP(from,to,map) else if (s >= from && s <= to) { s = map[s - from] + from; }


	static int RotateSprite(int s)
	{
		static const byte _rotate_tile_sprite[19] = { 0,2,4,6,8,10,12,14,1,3,5,7,9,11,13,17,18,16,15 };
		static const byte _coast_map[9] = {0, 4, 3, 1, 2, 6, 8, 5, 7};
		static const byte _fence_map[6] = {1, 0, 5, 4, 3, 2};

		if (0);
		X19(752)
		X15(990-1)
		X19(3924)
		X19(3943)
		X19(3962)
		X19(3981)
		X19(4000)
		X19(4023)
		X19(4042)
		MAP(4061,4069,_coast_map)
		X19(4126)
		X19(4145)
		X19(4164)
		X19(4183)
		X19(4202)
		X19(4221)
		X19(4240)
		X19(4259)
		X19(4259)
		X19(4278)
		MAP(4090, 4095, _fence_map)
		MAP(4096, 4101, _fence_map)
		MAP(4102, 4107, _fence_map)
		MAP(4108, 4113, _fence_map)
		MAP(4114, 4119, _fence_map)
		MAP(4120, 4125, _fence_map)
		return s;
	}
	#endif
	*/

	/*
	void *GetRawSprite(SpriteID sprite)
	{
		void* p;

		assert(sprite < MAX_SPRITES);

	/*#if defined(NEW_ROTATION)
		sprite = RotateSprite(sprite);
	#endif* /

		// Update LRU
	//#if defined(WANT_NEW_LRU)
		_sprite_lru_new[sprite] = ++_sprite_lru_counter;
	/*#else
		_sprite_lru_cur[sprite] = ++_sprite_lru_counter;
		_sprite_lru[sprite] = 0;
	#endif* /

		p = _sprite_ptr[sprite];
		// Load the sprite, if it is not loaded, yet
		if (p == null) p = ReadSprite(sprite);
		return p;
	}*/

	private static Sprite GetRawSprite(SpriteID sprite)
	{
		return GetRawSprite(sprite.id);	
	}
	
	private static Sprite GetRawSprite(int sprite)
	{
		Sprite p;

		assert(sprite < Sprite.MAX_SPRITES);


		p = _sprite_ptr[sprite];
		// Load the sprite, if it is not loaded, yet
		if (p == null) p = ReadSprite(sprite);
		return p;
	}	

	public static Sprite GetSprite(SpriteID sprite)
	{
		return GetRawSprite(sprite);
	}

	public static Sprite GetSprite(int sprite)
	{
		return GetRawSprite(sprite);
	}

	
	/*/ TODO do we need it?
	public static byte[] GetNonSpriteData(SpriteID sprite)
	{
		DataCarrier dc = (DataCarrier) GetRawSprite(sprite);
		return dc.data;
	}*/

	public static byte[] GetNonSprite(int sprite)
	{
		DataCarrier dc = (DataCarrier) GetRawSprite(sprite);
		return dc.data;
	}
	
	
	public static void GfxInitSpriteMem()
	{
		// Unused
	}
	

}


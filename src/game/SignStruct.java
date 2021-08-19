package game;


import java.util.Iterator;
import java.util.function.Consumer;

import game.ids.PlayerID;
import game.ids.StringID;
import game.ifaces.IPoolItem;
import game.ifaces.IPoolItemFactory;
import game.struct.Point;

public class SignStruct implements IPoolItem
{
	StringID     str;
	ViewportSign sign = new ViewportSign();
	int          x;
	int          y;
	byte         z;
	PlayerID     owner; // placed by this player. Anyone can delete them though.
	// OWNER_NONE for gray signs from old games.

	int          index;

	private static IPoolItemFactory<SignStruct> factory = new IPoolItemFactory<SignStruct>() {
		public SignStruct createObject() { return new SignStruct(); };
	};

	private static MemoryPool<SignStruct> _sign_pool = new MemoryPool<SignStruct>(factory);

	
	private void clear() {
		str = null;
		sign = new ViewportSign();
		x = y = z = 0;
		index = 0;
		owner = null;
	}

	public SignStruct() {
		clear();
	}
	
	/**
	 * Check if a Sign really exists.
	 */
	boolean IsValidSign()
	{
		return str != null;
	}

	/**
	 * Get the pointer to the sign with index 'index'
	 */
	static SignStruct GetSign(int index)
	{
		return _sign_pool.GetItemFromPool(index);
	}

	/**
	 * Get the current size of the SignPool
	 */
	static int GetSignPoolSize()
	{
		return _sign_pool.total_items();
	}

	static boolean IsSignIndex(int index)
	{
		return index < GetSignPoolSize();
	}

	//#define FOR_ALL_SIGNS_FROM(ss, start) for (ss = GetSign(start); ss != NULL; ss = (ss.index + 1 < GetSignPoolSize()) ? GetSign(ss.index + 1) : NULL)
	//#define FOR_ALL_SIGNS(ss) FOR_ALL_SIGNS_FROM(ss, 0)

	public static Iterator<SignStruct> getIterator()
	{
		return _sign_pool.pool.values().iterator();
	}

	public static void forEach( Consumer<SignStruct> c )
	{
		_sign_pool.forEach(c);
	}
	
	
	static boolean _sign_sort_dirty;
	//int *_sign_sort;





	private static SignStruct _new_sign_struct;

	/* Max signs: 64000 (4 * 16000) */
	//SIGN_POOL_BLOCK_SIZE_BITS = 2,       /* In bits, so (1 << 2) == 4 */
	//SIGN_POOL_MAX_BLOCKS      = 16000,

	/**
	 * Called if a new block is added to the sign-pool
	 * /
	static void SignPoolNewBlock(int start_item)
	{
		SignStruct *ss;

		FOR_ALL_SIGNS_FROM(ss, start_item)
			ss.index = start_item++;
	}*/

	@Override
	public void setIndex(int index) {
		this.index = index;	
	}


	/* Initialize the sign-pool */
	//MemoryPool _sign_pool = { "Signs", SIGN_POOL_MAX_BLOCKS, SIGN_POOL_BLOCK_SIZE_BITS, sizeof(SignStruct), &SignPoolNewBlock, 0, 0, NULL };

	/**
	 *
	 * Update the coordinate of one sign
	 *
	 */
	private void UpdateSignVirtCoords()
	{
		Point pt = Point.RemapCoords(x, y, z);
		Global.SetDParam(0, str.id);
		ViewPort.UpdateViewportSignPos(sign, pt.x, pt.y - 6, Str.STR_2806);
	}

	/**
	 *
	 * Update the coordinates of all signs
	 *
	 */
	static void UpdateAllSignVirtCoords()
	{
		//SignStruct *ss;

		//FOR_ALL_SIGNS(ss)
		_sign_pool.forEach( (i,ss) ->
		{
			if (ss.str != null)
				ss.UpdateSignVirtCoords();
		});
	}

	/**
	 *
	 * Marks the region of a sign as dirty
	 *
	 * @param ss Pointer to the SignStruct
	 */
	private void MarkSignDirty()
	{
		ViewPort.MarkAllViewportsDirty(
				sign.left - 6,
				sign.top  - 3,
				sign.left + sign.width_1 * 4 + 12,
				sign.top  + 45);
	}

	/**
	 *
	 * Allocates a new sign
	 *
	 * @return The pointer to the new sign, or NULL if there is no more free space
	 */
	static SignStruct AllocateSign()
	{
		SignStruct [] ret = {null};
 
		_sign_pool.forEach( (i,ss) ->
		{
			if (ss.str == null) {
				int index = ss.index;

				//memset(ss, 0, sizeof(SignStruct));
				ss.clear();
				ss.index = index;

				ret[0] = ss;
			}
		});

		if( ret[0] != null ) return ret[0];
		
		/* Check if we can add a block to the pool */
		if(_sign_pool.AddBlockToPool())
			return AllocateSign();

		return null;
	}


	/** Place a sign at the given coordinates. Ownership of sign has
	 * no effect whatsoever except for the colour the sign gets for easy recognition,
	 * but everybody is able to rename/remove it.
	 * @param x,y coordinates to place sign at
	 * @param p1 unused
	 * @param p2 unused
	 */
	public static int CmdPlaceSign(int x, int y, int flags, int p1, int p2)
	{
		SignStruct ss;

		/* Try to locate a new sign */
		ss = AllocateSign();
		if (ss == null) return Cmd.return_cmd_error(Str.STR_2808_TOO_MANY_SIGNS);

		/* When we execute, really make the sign */
		if(0 != (flags & Cmd.DC_EXEC) ) {
			ss.str = new StringID(Str.STR_280A_SIGN);
			ss.x = x;
			ss.y = y;
			ss.owner = Global._current_player; // owner of the sign; just eyecandy
			ss.z = (byte) Landscape.GetSlopeZ(x,y);
			ss.UpdateSignVirtCoords();
			ss.MarkSignDirty();
			Window.InvalidateWindow(Window.WC_SIGN_LIST, 0);
			_sign_sort_dirty = true;
			_new_sign_struct = ss;
		}

		return 0;
	}

	/** Rename a sign. If the new name of the sign is empty, we assume
	 * the user wanted to delete it. So delete it. Ownership of signs
	 * has no meaning/effect whatsoever except for eyecandy
	 * @param x,y unused
	 * @param p1 index of the sign to be renamed/removed
	 * @param p2 unused
	 */
	static int CmdRenameSign(int x, int y, int flags, int p1, int p2)
	{
		if (!IsSignIndex(p1)) return Cmd.CMD_ERROR;

		/* If _cmd_text 0 means the new text for the sign is non-empty.
		 * So rename the sign. If it is empty, it has no name, so delete it */
		if (Global._cmd_text != null) {
			/* Create the name */
			StringID str = Global.AllocateName(Global._cmd_text, 0);
			if (str == null) return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC) ) {
				SignStruct ss = GetSign(p1);

				/* Delete the old name */
				Global.DeleteName(ss.str);
				/* Assign the new one */
				ss.str = str;
				ss.owner = Global._current_player;

				/* Update; mark sign dirty twice, because it can either becom longer, or shorter */
				ss.MarkSignDirty();
				ss.UpdateSignVirtCoords();
				ss.MarkSignDirty();

				Window.InvalidateWindow(Window.WC_SIGN_LIST, 0);
				_sign_sort_dirty = true;
			} else {
				/* Free the name, because we did not assign it yet */
				Global.DeleteName(str);
			}
		} else { /* Delete sign */
			if( 0 != (flags & Cmd.DC_EXEC)) {
				SignStruct ss = GetSign(p1);

				/* Delete the name */
				Global.DeleteName(ss.str);
				ss.str = null;

				ss.MarkSignDirty();
				Window.InvalidateWindow(Window.WC_SIGN_LIST, 0);
				_sign_sort_dirty = true;
			}
		}

		return 0;
	}

	/**
	 *
	 * Callback function that is called after a sign is placed
	 *
	 */
	static void CcPlaceSign(boolean success, TileIndex tile, int p1, int p2)
	{
		if (success) {
			Gui.ShowRenameSignWindow(_new_sign_struct);
			ViewPort.ResetObjectToPlace();
		}
	}

	/**
	 *
	 * PlaceProc function, called when someone pressed the button if the
	 *  sign-tool is selected
	 *
	 */
	static void PlaceProc_Sign(TileIndex tile)
	{
		Cmd.DoCommandP(tile, 0, 0, SignStruct::CcPlaceSign, Cmd.CMD_PLACE_SIGN | Cmd.CMD_MSG(Str.STR_2809_CAN_T_PLACE_SIGN_HERE));
	}

	/**
	 *
	 * Initialize the signs
	 *
	 */
	static void InitializeSigns()
	{
		_sign_pool.CleanPool();
		_sign_pool.AddBlockToPool();
	}
	/*
static const SaveLoad _sign_desc[] = {
	SLE_VAR(SignStruct,str,						SLE_int),
	SLE_CONDVAR(SignStruct,x,					SLE_FILE_I16 | SLE_VAR_I32, 0, 4),
	SLE_CONDVAR(SignStruct,y,					SLE_FILE_I16 | SLE_VAR_I32, 0, 4),
	SLE_CONDVAR(SignStruct,x,					SLE_int, 5, 255),
	SLE_CONDVAR(SignStruct,y,					SLE_int, 5, 255),
	SLE_CONDVAR(SignStruct,owner,			SLE_int8, 6, 255),
	SLE_VAR(SignStruct,z,							SLE_int8),
	SLE_END()
};
	 */
	/**
	 *
	 * Save all signs
	 *
	 * /
	static void Save_SIGN()
	{
		//SignStruct *ss;

		//FOR_ALL_SIGNS(ss) 
		_sign_pool.forEach( (i,ss) ->
		{
			// Don't save empty signs 
			if (ss.str != null) {
				SlSetArrayIndex(ss.index);
				SlObject(ss, _sign_desc);
			}
		});
	}

	/**
	 *
	 * Load all signs
	 *
	 * /
	static void Load_SIGN()
	{
		int index;
		while ((index = SlIterateArray()) != -1) {
			SignStruct ss;

			if (!_sign_pool.AddBlockIfNeeded(index))
				Global.error("Signs: failed loading savegame: too many signs");

			ss = GetSign(index);
			SlObject(ss, _sign_desc);
		}

		_sign_sort_dirty = true;
	}

	final ChunkHandler _sign_chunk_handlers[] = {
			{ 'SIGN', Save_SIGN, Load_SIGN, CH_ARRAY | CH_LAST},
	};
*/






}
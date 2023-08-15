package com.dzavalishin.game;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.function.Consumer;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.Rect;
import com.dzavalishin.struct.StringSpriteToDraw;
import com.dzavalishin.xui.Gui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class SignStruct implements IPoolItem
{
	private static final long serialVersionUID = 1L;
	
	private StringID str;
	private ViewportSign sign;
	private int          x;
	private int          y;
	private byte         z;
	private PlayerID owner; // placed by this player. Anyone can delete them though.
	// OWNER_NONE for gray signs from old games.

	private int          index;

	static final IPoolItemFactory<SignStruct> factory = new IPoolItemFactory<SignStruct>() 
	{
		private static final long serialVersionUID = 1L;

		public SignStruct createObject() { return new SignStruct(); }
	};

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
	public boolean isValid()
	{
		return str != null;
	}

	/**
	 * Get the pointer to the sign with index 'index'
	 */
	public static SignStruct GetSign(int index)
	{
		return Global.gs._signs.GetItemFromPool(index);
	}

	/**
	 * Get the current size of the SignPool
	 */
	public static int GetSignPoolSize()
	{
		return Global.gs._signs.total_items();
	}

	static boolean IsSignIndex(int index)
	{
		return index < GetSignPoolSize();
	}

	//#define FOR_ALL_SIGNS_FROM(ss, start) for (ss = GetSign(start); ss != NULL; ss = (ss.index + 1 < GetSignPoolSize()) ? GetSign(ss.index + 1) : NULL)
	//#define FOR_ALL_SIGNS(ss) FOR_ALL_SIGNS_FROM(ss, 0)

	public static Iterator<SignStruct> getIterator()
	{
		return Global.gs._signs.getIterator(); ////pool.values().iterator();
	}

	public static void forEach( Consumer<SignStruct> c )
	{
		Global.gs._signs.forEach(c);
	}
	
	
	public static boolean _sign_sort_dirty;
	//int *_sign_sort;





	private static SignStruct _new_sign_struct;


	@Override
	public void setIndex(int index) {
		this.index = index;	
	}


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
		/*Global.gs._signs.forEach( (i,ss) ->
		{
			if (ss.str != null)
				ss.UpdateSignVirtCoords();
		});*/

		Global.gs._signs.forEach( (ss) -> { if (ss.str != null) ss.UpdateSignVirtCoords(); } );
	}

	/**
	 *
	 * Marks the region of a sign as dirty
	 *
	 */
	private void MarkSignDirty()
	{
		ViewPort.MarkAllViewportsDirty(
				sign.getLeft() - 6,
				sign.getTop()  - 3,
				sign.getLeft() + sign.getWidth_1() * 4 + 12,
				sign.getTop()  + 45);
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
 
		Global.gs._signs.forEach( ss ->
		{
			if (ss.str == null) {
				int index = ss.index;

				ss.clear();
				ss.index = index;

				ret[0] = ss;
			}
		});

		if( ret[0] != null ) return ret[0];
		
		/* Check if we can add a block to the pool */
		if(Global.gs._signs.AddBlockToPool())
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
			ss.owner = PlayerID.getCurrent(); // owner of the sign; just eyecandy
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
			//if (str == null) return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC) ) {
				SignStruct ss = GetSign(p1);

				/* Delete the old name */
				Global.DeleteName(ss.str);
				/* Assign the new one */
				ss.str = str;
				ss.owner = PlayerID.getCurrent();

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
	public static void CcPlaceSign(boolean success, TileIndex tile, int p1, int p2)
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
	public static void PlaceProc_Sign(TileIndex tile)
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
		Global.gs._signs.CleanPool();
		Global.gs._signs.AddBlockToPool();
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

	final Chunk Handler _sign_chunk_handlers[] = {
			{ 'SIGN', Save_SIGN, Load_SIGN, CH_ARRAY | CH_LAST},
	}; */

	
	/**	
	 * @param bottom 
	 * @param right 
	 * @param top 
	 * @param left 
	 * @param mult 
*/

	public void draw(int left, int top, int right, int bottom, int zoom) 
	{
		int mult = 1;
		int sw = sign.getWidth_1();
		int topAdd = 12;
		switch(zoom)
		{
		case 0:
			break;
		case 1:
			mult = 2;
			topAdd = 24;
			break;
		default:
			mult = 4;
			topAdd = 24;
			sw = sign.getWidth_2() | 0x8000;
			break;
		}
		
		if (str != null &&
				bottom > sign.getTop() &&
				top < sign.getTop() + topAdd &&
				right > sign.getLeft() &&
				left < sign.getLeft() + sign.getWidth_1() * mult) 
		{

			StringSpriteToDraw sstd = ViewPort.AddStringToDraw(sign.getLeft() + 1, sign.getTop() + 1, new StringID(Str.STR_2806), str.id, 0, 0);
			if (sstd != null) {
				sstd.width = sw;
				sstd.color = (owner.isNone() || owner.isTown())?14:Global.gs._player_colors[owner.id];
			}
		}
	}

	public void draw(Rect rect, int zoom) {
		draw(rect.left, rect.top, rect.right, rect.bottom, zoom);		
	}

	public boolean clickIn( int x, int y, int zoom )
	{
		//int mult = 1;
		int sw = sign.getWidth_1();
		int topAdd = 12;
		switch(zoom)
		{
		case 0:
			break;
		case 1:
			//mult = 2;
			topAdd = 24;
			sw = sign.getWidth_1() * 2;
			break;
		default:
			//mult = 4;
			topAdd = 24;
			sw = sign.getWidth_2() * 4;
			break;
		}
		
		
		return str != null &&
				y >= sign.getTop() &&
				y < sign.getTop() + topAdd &&
				x >= sign.getLeft() &&
				x < sign.getLeft() + sw;	
	}

	public int getIndex() {
		return index;
	}

	public StringID getString() {
		return str;
	}

	public PlayerID getOwner() {
		return owner;
	}

	public TileIndex getTile() {
		return TileIndex.TileVirtXY(x, y);
	}


	public static void loadGame(ObjectInputStream oin) throws ClassNotFoundException, IOException
	{
		//_sign_pool = (MemoryPool<SignStruct>) oin.readObject();
	}

	public static void saveGame(ObjectOutputStream oos) throws IOException 
	{
		//oos.writeObject(_sign_pool);		
	}



}
package com.dzavalishin.xui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.WindowEvents;
import com.dzavalishin.game.AirCraft;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.SignStruct;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TextEffect;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.TileInfo;
import com.dzavalishin.game.Town;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.game.ViewportSign;
import com.dzavalishin.game.WayPoint;
import com.dzavalishin.ids.CursorID;
import com.dzavalishin.ids.SpriteID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.struct.ChildScreenSpriteToDraw;
import com.dzavalishin.struct.ParentSpriteToDraw;
import com.dzavalishin.struct.Point;
import com.dzavalishin.struct.Rect;
import com.dzavalishin.struct.StringSpriteToDraw;
import com.dzavalishin.struct.TileMarker;
import com.dzavalishin.struct.TileSpriteToDraw;
import com.dzavalishin.util.AnimCursor;
import com.dzavalishin.util.AutoRail;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Pixel;
import com.dzavalishin.util.Strings;


/**
 * ViewPort is part of window that shows game scene. Main window is just viewport. 
 *
 */

public class ViewPort implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public int left,top;												// screen coordinates for the viewport
	int width, height;									// screen width/height for the viewport

	int virtual_left, virtual_top;			// virtual coordinates
	int virtual_width, virtual_height;	// these are just width << zoom, height << zoom

	int zoom;


	// don't need
	//boolean active; // used instead of bit in _active_viewports

	public int getLeft() {		return left;	}
	public int getTop() {		return top;	}
	public int getWidth() {		return width;	}
	public int getHeight() {		return height;	}

	public int getVirtual_left() {		return virtual_left;	}
	public int getVirtual_top() {		return virtual_top;	}
	public int getVirtual_width() {		return virtual_width;	}
	public int getVirtual_height() {		return virtual_height;	}
	
	public int getZoom() {		return zoom;	}
	

	public static final int VHM_NONE = 0;    // default
	public static final int VHM_RECT = 1;    // rectangle (stations; depots; ...)
	public static final int VHM_POINT = 2;   // point (lower land; raise land; level land; ...)
	public static final int VHM_SPECIAL = 3; // special mode used for highlighting while dragging (and for tunnels/docks)
	public static final int VHM_DRAG = 4;    // dragging items in the depot windows
	public static final int VHM_RAIL = 5;    // rail pieces


	static public final int VPM_X_OR_Y = 0;
	static public final int VPM_FIX_X = 1;
	static public final int VPM_FIX_Y = 2;
	static public final int VPM_RAILDIRS = 3;
	static public final int VPM_X_AND_Y = 4;
	static public final int VPM_X_AND_Y_LIMITED = 5;
	static public final int VPM_SIGNALDIRS = 6;

	// highlighting draw styles
	static public final int HT_NONE = 0;
	static public final int HT_RECT = 0x80;
	static public final int HT_POINT = 0x40;
	static public final int HT_LINE = 0x20; /* used for autorail highlighting (longer streches)
	 * (uses lower bits to indicate direction) */
	static public final int HT_RAIL = 0x10; /* autorail (one piece)
	 * (uses lower bits to indicate direction) */

	/* lower bits (used with static public final int HT_LINE and static public final int HT_RAIL):
	 * (see ASCII art in autorail.h for a visual interpretation) */
	static public final int HT_DIR_X = 0;  // X direction
	static public final int HT_DIR_Y = 1;  // Y direction
	static public final int HT_DIR_HU = 2; // horizontal upper
	static public final int HT_DIR_HL = 3; // horizontal lower
	static public final int HT_DIR_VL = 4; // vertical left
	static public final int HT_DIR_VR = 5; // vertical right










	public static final Point _tile_fract_coords = new Point(0,0);
	public static final TileHighlightData _thd = new TileHighlightData();
	
	public static final List<ViewPort> _viewports = new ArrayList<>();

	public void removeFromAll()
	{
		_viewports.remove(this);
	}

	public void addToAll()
	{
		_viewports.add(this);
	}


	public ViewPort() {
		//_viewports.add(this);
	}


	public ViewPort(int left, int top, int width, int height, int zoom) {
		this.zoom = zoom;
		this.left = 0;
		this.top = 0;
		this.virtual_width = width;
		this.width = width >> zoom;
		this.virtual_height = height;
		this.height = height >> zoom;
		this.virtual_left = left;
		this.virtual_top = top;
	}


	static boolean _added_tile_sprite;
	static boolean _offset_ground_sprites;


	// Quick hack to know how much memory to reserve when allocating from the spritelist
	// to prevent a buffer overflow.
	//#define LARGEST_SPRITELIST_STRUCT ParentSpriteToDraw


	static ViewportDrawer _cur_vd;
	static TileInfo _cur_ti;


	static Point MapXYZToViewport(final ViewPort vp, int x, int y, int z)
	{
		Point p = Point.RemapCoords(x, y, z);
		p.x -= vp.virtual_width / 2;
		p.y -= vp.virtual_height / 2;
		return p;
	}

	public static void AssignWindowViewport(Window w, int x, int y,
			int width, int height, int follow_flags, int zoom)
	{
		ViewPort vp = null;
		Point pt;
		//int bit = 1;
		//for (vp = _viewports, bit = 1; ; vp++, bit <<= 1) 
		for (ViewPort vps : _viewports ) 
		{
			//assert(vp != endof(_viewports));
			if (vps.width == 0)
			{
				vp = vps;
				break;
			}
			//bit <<= 1;
		}

		// [dz] is it ok? Do we need viewports container at all? Yes, we do.
		if( vp == null)
		{
			vp = new ViewPort();
			_viewports.add(vp);
		}


		vp.left = x + w.left;
		vp.top = y + w.top;
		vp.width = width;
		vp.height = height;

		// bring zoom back
		vp.zoom = (byte) zoom;
		//vp.zoom = 0;

		vp.virtual_width = width << zoom;
		vp.virtual_height = height << zoom;

		if(0 != (follow_flags & 0x80000000)) {
			Vehicle veh;

			w.as_vp_d().follow_vehicle = follow_flags & 0xFFFF;
			veh = Vehicle.GetVehicle(w.as_vp_d().follow_vehicle);
			pt = MapXYZToViewport(vp, veh.getX_pos(), veh.getY_pos(), veh.getZ_pos());
		} else {
			TileIndex tt = new TileIndex(follow_flags);
			int tx = tt.TileX() * 16;
			int ty = tt.TileY() * 16;

			w.as_vp_d().follow_vehicle = Vehicle.INVALID_VEHICLE;
			pt = MapXYZToViewport(vp, tx, ty, Landscape.GetSlopeZ(tx, ty));
		}

		w.as_vp_d().scrollpos_x = pt.x;
		w.as_vp_d().scrollpos_y = pt.y;
		w.viewport = vp;
		vp.virtual_left = 0;//pt.x;
		vp.virtual_top = 0;//pt.y;
	}

	static final Point _vp_move_offs = new Point(0, 0);

	static void DoSetViewportPosition(Window startw, int left, int top, int width, int height)
	{
		//for (; w < _last_window; w++) 
		Iterator<Window> ii = Window.getIterator( startw );
		// TODO crashes! ii.next(); // caller func had w+1 as 1st param, so we skip one window
		while(ii.hasNext())
		{
			Window w = ii.next();

			if (left + width > w.left &&
					w.left + w.width > left &&
					top + height > w.top &&
					w.top + w.height > top) {

				if (left < w.left) {
					DoSetViewportPosition(w, left, top, w.left - left, height);
					DoSetViewportPosition(w, left + (w.left - left), top, width - (w.left - left), height);
					return;
				}

				if (left + width > w.left + w.width) {
					DoSetViewportPosition(w, left, top, (w.left + w.width - left), height);
					DoSetViewportPosition(w, left + (w.left + w.width - left), top, width - (w.left + w.width - left) , height);
					return;
				}

				if (top < w.top) {
					DoSetViewportPosition(w, left, top, width, (w.top - top));
					DoSetViewportPosition(w, left, top + (w.top - top), width, height - (w.top - top));
					return;
				}

				if (top + height > w.top + w.height) {
					DoSetViewportPosition(w, left, top, width, (w.top + w.height - top));
					DoSetViewportPosition(w, left, top + (w.top + w.height - top), width , height - (w.top + w.height - top));
					return;
				}

				return;
			}
		}

		{
			int xo = _vp_move_offs.x;
			int yo = _vp_move_offs.y;

			if ((xo) >= width || (yo) >= height) {
				/* fully_outside */
				Gfx.RedrawScreenRect(left, top, left + width, top + height);
				return;
			}

			Gfx.GfxScroll(left, top, width, height, xo, yo);

			if (xo > 0) {
				Gfx.RedrawScreenRect(left, top, xo + left, top + height);
				left += xo;
				width -= xo;
			} else if (xo < 0) {
				Gfx.RedrawScreenRect(left+width+xo, top, left+width, top + height);
				width += xo;
			}

			if (yo > 0) {
				Gfx.RedrawScreenRect(left, top, width+left, top + yo);
			} else if (yo < 0) {
				Gfx.RedrawScreenRect(left, top + height + yo, width+left, top + height);
			}
		}
	}

	static void SetViewportPosition(Window w, int x, int y)
	{
		ViewPort vp = w.getViewport();
		int old_left = vp.virtual_left;
		int old_top = vp.virtual_top;
		int i;
		int left, top, width, height;

		vp.virtual_left = x;
		vp.virtual_top = y;

		old_left >>= vp.zoom;
			old_top >>= vp.zoom;
			x >>= vp.zoom;
			y >>= vp.zoom;

			old_left -= x;
			old_top -= y;

			if (old_top == 0 && old_left == 0) return;

			_vp_move_offs.x = old_left;
			_vp_move_offs.y = old_top;

			left = vp.left;
			top = vp.top;
			width = vp.width;
			height = vp.height;

			if (left < 0) {
				width += left;
				left = 0;
			}

			i = left + width - Hal._screen.width;
			if (i >= 0) width -= i;

			if (width > 0) {
				if (top < 0) {
					height += top;
					top = 0;
				}

				i = top + height - Hal._screen.height;
				if (i >= 0) height -= i;

				// Window wnext = w.getNextWindow?
				//if (height > 0) DoSetViewportPosition(w + 1, left, top, width, height);
				// TODO DoSetViewportPosition starts from next window
				if (height > 0)
				{
					DoSetViewportPosition(w, left, top, width, height);
					Hal.MarkWholeScreenDirty(); // TODO [dz] added, was not here in C code, why? 
				}
			}
	}

	static Point TranslateXYToTileCoord(final ViewPort vp, int x, int y)
	{
		int z;
		int a,b;

		x -= vp.left;
		y -= vp.top;

		if ( x >= vp.width || y >= vp.height
				|| x < 0 || y < 0 )
			return new Point(-1, -1);

		x = ((x << vp.zoom) + vp.virtual_left) >> 2;
		y = ((y << vp.zoom) + vp.virtual_top) >> 1;

		//#if !defined(NEW_ROTATION)
		a = y-x;
		b = y+x;
		//#else*/
		//a = x+y;
		//b = x-y;
		//#endif
		z = Landscape.GetSlopeZ(a, b) >> 1;
		z = Landscape.GetSlopeZ(a+z, b+z) >> 1;
		z = Landscape.GetSlopeZ(a+z, b+z) >> 1;
		z = Landscape.GetSlopeZ(a+z, b+z) >> 1;
		z = Landscape.GetSlopeZ(a+z, b+z) >> 1;

		Point pt = new Point( a+z, b+z );

		//pt.x = a+z;
		//pt.y = b+z;

		if (pt.x >= Global.MapMaxX() * 16 || pt.y >= Global.MapMaxY() * 16
				|| pt.x < 0 || pt.y < 0) {
			pt.x = pt.y = -1;
		}

		return pt;
	}

	/* When used for zooming, check area below current coordinates (x,y)
	 * and return the tile of the zoomed out/in position (zoom_x, zoom_y)
	 * when you just want the tile, make x = zoom_x and y = zoom_y */
	static Point GetTileFromScreenXY(int x, int y, int zoom_x, int zoom_y)
	{
		Window w;
		ViewPort vp;

		if ( (w = Window.FindWindowFromPt(x, y)) != null &&
				(vp = w.IsPtInWindowViewport(x, y)) != null)
			return TranslateXYToTileCoord(vp, zoom_x, zoom_y);

		return new Point(-1, -1);
	}

	static Point GetTileBelowCursor()
	{
		return GetTileFromScreenXY(Hal._cursor.pos.x, Hal._cursor.pos.y, Hal._cursor.pos.x, Hal._cursor.pos.y);
	}


	static Point GetTileZoomCenterWindow(boolean in, Window  w)
	{
		int x, y;
		ViewPort  vp;

		vp = w.getViewport();

		if (in) {
			x = ((Hal._cursor.pos.x - vp.left) >> 1) + (vp.width >> 2);
			y = ((Hal._cursor.pos.y - vp.top) >> 1) + (vp.height >> 2);
		} else {
			x = vp.width - (Hal._cursor.pos.x - vp.left);
			y = vp.height - (Hal._cursor.pos.y - vp.top);
		}
		/* Get the tile below the cursor and center on the zoomed-out center */
		return GetTileFromScreenXY(Hal._cursor.pos.x, Hal._cursor.pos.y, x + vp.left, y + vp.top);
	}

	public static void DrawGroundSpriteAt(int image, int x, int y, int z)
	{
		ViewportDrawer vd = _cur_vd;
		TileSpriteToDraw ts;

		assert((image & Sprite.SPRITE_MASK) < Sprite.MAX_SPRITES);

		ts = new TileSpriteToDraw();

		ts.image = image;
		ts.x = x;
		ts.y = y;
		ts.z = z;

		//*vd.last_tile = ts;
		//vd.last_tile = &ts.next;
		vd.tile_list.add(ts);
	}

	public static void DrawGroundSprite( SpriteID image)
	{
		DrawGroundSprite(image.id);		
	}

	public static void DrawGroundSprite(int image)
	{
		if (_offset_ground_sprites) {
			// offset ground sprite because of foundation?
			AddChildSpriteScreen(image, _cur_vd.offs_x, _cur_vd.offs_y);
		} else {
			_added_tile_sprite = true;
			DrawGroundSpriteAt(image, _cur_ti.x, _cur_ti.y, _cur_ti.z);
		}
	}


	public static void OffsetGroundSprite(int x, int y)
	{
		_cur_vd.offs_x = x;
		_cur_vd.offs_y = y;
		_offset_ground_sprites = true;
	}

	public static void AddCombinedSprite(int image, int x, int y, int z)
	{
		final ViewportDrawer vd = _cur_vd;
		Point pt = Point.RemapCoords(x, y, z);
		final Sprite spr = Sprite.GetSprite(image & Sprite.SPRITE_MASK);

		if (pt.x + spr.getX_offs() >= vd.dpi.left + vd.dpi.width ||
				pt.x + spr.getX_offs() + spr.getWidth() <= vd.dpi.left ||
				pt.y + spr.getY_offs() >= vd.dpi.top + vd.dpi.height ||
				pt.y + spr.getY_offs() + spr.getHeight() <= vd.dpi.top)
			return;

		ParentSpriteToDraw last_parent = vd.parent_list.get( vd.parent_list.size() - 1 );
		AddChildSpriteScreen(image, pt.x - last_parent.left, pt.y - last_parent.top);

		//AddChildSpriteScreen(image, pt.x - vd.parent_list[-1].left, pt.y - vd.parent_list[-1].top);
	}


	public static void AddSortableSpriteToDraw(int image, int x, int y, int w, int h, int dz, int z)
	{
		ViewportDrawer vd = _cur_vd;
		ParentSpriteToDraw ps;
		final Sprite spr;
		Point pt;

		assert((image & Sprite.SPRITE_MASK) < Sprite.MAX_SPRITES);

		if (vd.combine_sprites == 2) {
			AddCombinedSprite(image, x, y, z);
			return;
		}

		vd.last_parent = null;

		ps = new ParentSpriteToDraw();

		ps.image = image;
		ps.tile_x = x;
		ps.tile_right = x + w - 1;

		ps.tile_y = y;
		ps.tile_bottom = y + h - 1;

		ps.tile_z = z;
		ps.tile_z_bottom = z + dz - 1;

		pt = Point.RemapCoords(x, y, z);

		spr = Sprite.GetSprite(image & Sprite.SPRITE_MASK);
		if ((ps.left   = (pt.x += spr.getX_offs())) >= vd.dpi.left + vd.dpi.width ||
				(ps.right  = (pt.x +  spr.getWidth() )) <= vd.dpi.left ||
				(ps.top    = (pt.y += spr.getY_offs())) >= vd.dpi.top + vd.dpi.height ||
				(ps.bottom = (pt.y +  spr.getHeight())) <= vd.dpi.top) {
			return;
		}

		ps.unk16 = 0;

		vd.last_parent = ps;
		vd.parent_list.add( ps );

		if (vd.combine_sprites == 1) vd.combine_sprites = 2;
	}

	public static void StartSpriteCombine()
	{
		_cur_vd.combine_sprites = 1;
	}

	public static void EndSpriteCombine()
	{
		_cur_vd.combine_sprites = 0;
	}

	public static void AddChildSpriteScreen(int image, int x, int y)
	{
		ViewportDrawer vd = _cur_vd;
		ChildScreenSpriteToDraw cs;

		assert((image & Sprite.SPRITE_MASK) < Sprite.MAX_SPRITES);

		cs = new ChildScreenSpriteToDraw();

		if (vd.last_parent == null) 
		{
			Global.error("AddChildSpriteScreen failed: vd.last_parent == null");
			return;
		}

		vd.last_parent.children.add(cs);

		cs.image = image;
		cs.x = x;
		cs.y = y;
	}

	/* Returns a StringSpriteToDraw */
	public static StringSpriteToDraw AddStringToDraw(int x, int y, StringID string, int params_1, int params_2, int params_3)
	{
		return AddStringToDraw( x,  y, string.id, params_1, params_2, params_3);
	}

	/* Returns a StringSpriteToDraw - [dz] was Object / was void **/
	public static StringSpriteToDraw AddStringToDraw(int x, int y, /*StringID*/ int string, int params_1, int params_2, int params_3)
	{
		ViewportDrawer vd = _cur_vd;
		StringSpriteToDraw ss = new StringSpriteToDraw();

		ss.string = string;
		ss.x = x;
		ss.y = y;
		ss.params[0] = params_1;
		ss.params[1] = params_2;
		ss.params[2] = params_3;
		ss.width = 0;

		vd.string_list.add(ss);

		return ss;
	}

	/*
	#ifdef DEBUG_HILIGHT_MARKED_TILES

	static void DrawHighlighedTile(final TileInfo ti)
	{
		if (_m[ti.tile].extra & 0x80) {
			DrawSelectionSprite(Sprite.PALETTE_TILE_RED_PULSATING | (Sprite.SPR_SELECT_TILE + _tileh_to_sprite[ti.tileh]), ti);
		}
	}

	int _debug_marked_tiles, _debug_red_tiles;

	// Helper functions that allow you mark a tile as red.
	void DebugMarkTile(TileIndex tile) {
		_debug_marked_tiles++;
		if (_m[tile].extra & 0x80)
			return;
		_debug_red_tiles++;
		MarkTileDirtyByTile(tile);
		_m[tile].extra = (_m[tile].extra & ~0xE0) | 0x80;
	}

	void DebugClearMarkedTiles()
	{
		int size = MapSize(), i;
		for(i=0; i!=size; i++) {
			if (_m[i].extra & 0x80) {
				_m[i].extra &= ~0x80;
				MarkTileDirtyByTile(i);
			}
		}
		_debug_red_tiles = 0;
		_debug_red_tiles = 0;
	}


	#endif */

	static void DrawSelectionSprite(int image, final TileInfo ti)
	{
		if (_added_tile_sprite && 0 == (_thd.drawstyle & HT_LINE)) { // draw on real ground
			DrawGroundSpriteAt(image, ti.x, ti.y,  (ti.z + 7));
		} else { // draw on top of foundation
			AddSortableSpriteToDraw(image, ti.x, ti.y, 0x10, 0x10, 1, ti.z + 7);
		}
	}

	static boolean IsPartOfAutoLine(int px, int py)
	{
		px -= _thd.selstart.x;
		py -= _thd.selstart.y;

		switch(_thd.drawstyle) {
		case HT_LINE | HT_DIR_X:  return py == 0; // x direction
		case HT_LINE | HT_DIR_Y:  return px == 0; // y direction
		case HT_LINE | HT_DIR_HU: return px == -py || px == -py - 16; // horizontal upper
		case HT_LINE | HT_DIR_HL: return px == -py || px == -py + 16; // horizontal lower
		case HT_LINE | HT_DIR_VL: return px == py || px == py + 16; // vertival left
		case HT_LINE | HT_DIR_VR: return px == py || px == py - 16; // vertical right
		default:
			assert false; //NOT_REACHED();
		}

		/* useless, but avoids compiler warning this way */
		return false;
	}

	// [direction][side]
	static final int _AutorailType[][] = {
			{ HT_DIR_X,  HT_DIR_X },
			{ HT_DIR_Y,  HT_DIR_Y },
			{ HT_DIR_HU, HT_DIR_HL },
			{ HT_DIR_HL, HT_DIR_HU },
			{ HT_DIR_VL, HT_DIR_VR },
			{ HT_DIR_VR, HT_DIR_VL }
	};

	//#include "table/autorail.h"

	static void DrawTileSelection(final TileInfo ti)
	{
		int image;

		//#ifdef DEBUG_HILIGHT_MARKED_TILES
		//	DrawHighlighedTile(ti);
		//#endif

		// Draw a red error square?
		if (_thd.redsq != null && _thd.redsq.equals(ti.tile)) {
			DrawSelectionSprite(Sprite.PALETTE_TILE_RED_PULSATING | (Sprite.SPR_SELECT_TILE + Landscape._tileh_to_sprite[ti.tileh]), ti);
			return;
		}

		// no selection active?
		if (_thd.drawstyle == 0) return;

		// Inside the inner area?
		if (BitOps.IS_INSIDE_1D(ti.x, _thd.pos.x, _thd.size.x) &&
				BitOps.IS_INSIDE_1D(ti.y, _thd.pos.y, _thd.size.y)) {
			if(0 != (_thd.drawstyle & HT_RECT)) {
				image = Sprite.SPR_SELECT_TILE + Landscape._tileh_to_sprite[ti.tileh];
				if (_thd.make_square_red) image |= Sprite.PALETTE_SEL_TILE_RED;
				DrawSelectionSprite(image, ti);
			} else if(0 != (_thd.drawstyle & HT_POINT)) {
				// Figure out the Z coordinate for the single dot.
				int z = ti.z;
				if(0 != (ti.tileh & 8)) {
					z += 8;
					if (0 == (ti.tileh & 2) && (TileIndex.IsSteepTileh(ti.tileh))) z += 8;
				}
				DrawGroundSpriteAt(Hal._cur_dpi.zoom != 2 ? Sprite.SPR_DOT : Sprite.SPR_DOT_SMALL, ti.x, ti.y, z);
			} else if( 0 != (_thd.drawstyle & HT_RAIL /*&& _thd.place_mode == VHM_RAIL*/)) {
				// autorail highlight piece under cursor
				int type = _thd.drawstyle & 0xF;
				assert(type <= 5);
				image = Sprite.SPR_AUTORAIL_BASE + AutoRail._AutorailTilehSprite[ti.tileh][_AutorailType[type][0]];

				if (_thd.make_square_red) image |= Sprite.PALETTE_SEL_TILE_RED;
				DrawSelectionSprite(image, ti);

			} else if (IsPartOfAutoLine(ti.x, ti.y)) {
				// autorail highlighting long line
				int dir = _thd.drawstyle & ~0xF0;
				int side;

				if (dir < 2) {
					side = 0;
				} else {
					TileIndex start = TileIndex.TileVirtXY(_thd.selstart.x, _thd.selstart.y);
					int diffx = Math.abs(start.TileX() - ti.tile.TileX());
					int diffy = Math.abs(start.TileY() - ti.tile.TileY());
					side = Math.abs(diffx - diffy);
				}

				image = Sprite.SPR_AUTORAIL_BASE + AutoRail._AutorailTilehSprite[ti.tileh][_AutorailType[dir][side]];

				if (_thd.make_square_red) image |= Sprite.PALETTE_SEL_TILE_RED;
				DrawSelectionSprite(image, ti);
			}
			return;
		}

		// Check if it's inside the outer area?
		if (_thd.outersize.x != 0 &&
				_thd.size.x < _thd.size.x + _thd.outersize.x &&
				BitOps.IS_INSIDE_1D(ti.x, _thd.pos.x + _thd.offs.x, _thd.size.x + _thd.outersize.x) &&
				BitOps.IS_INSIDE_1D(ti.y, _thd.pos.y + _thd.offs.y, _thd.size.y + _thd.outersize.y)) {
			// Draw a blue rect.
			DrawSelectionSprite(Sprite.PALETTE_SEL_TILE_BLUE | (Sprite.SPR_SELECT_TILE + Landscape._tileh_to_sprite[ti.tileh]), ti);			
		}
	}

	static void ViewportAddLandscape()
	{
		ViewportDrawer vd = _cur_vd;
		int x, y, width, height;
		//TileInfo ti = new TileInfo();
		boolean direction;

		//_cur_ti = ti;

		// Transform into tile coordinates and round to closest full tile
		//#if !defined(NEW_ROTATION)
		x = ((vd.dpi.top >> 1) - (vd.dpi.left >> 2)) & ~0xF;
		y = ((vd.dpi.top >> 1) + (vd.dpi.left >> 2) - 0x10) & ~0xF;
		/*#else
		x = ((vd.dpi.top >> 1) + (vd.dpi.left >> 2) - 0x10) & ~0xF;
		y = ((vd.dpi.left >> 2) - (vd.dpi.top >> 1)) & ~0xF;
	#endif */
		// determine size of area
		{
			Point pt = Point.RemapCoords(x, y, 241);
			width = (vd.dpi.left + vd.dpi.width - pt.x + 95) >> 6;
			height = (vd.dpi.top + vd.dpi.height - pt.y) >> 5 << 1;
		}

		assert(width > 0);
		assert(height > 0);

		direction = false;

		do {
			int width_cur = width;
			int x_cur = x;
			int y_cur = y;

			do {
				TileInfo ti = Landscape.FindLandscapeHeight(x_cur, y_cur);
				_cur_ti = ti;
				//#if !defined(NEW_ROTATION)
				y_cur += 0x10;
				x_cur -= 0x10;
				/*#else
				y_cur += 0x10;
				x_cur += 0x10;
	#endif*/
				_added_tile_sprite = false;
				_offset_ground_sprites = false;

				Landscape.DrawTile(ti);
				DrawTileSelection(ti);
			} while (--width_cur > 0);

			//#if !defined(NEW_ROTATION)
			//if ( (direction^=1) != 0)
			direction=!direction;
			if ( direction )
				y += 0x10;
			else
				x += 0x10;
			/*#else
			if ( (direction^=1) != 0)
				x += 0x10;
			else
				y -= 0x10;
	#endif*/
		} while (--height > 0);
	}


	static void ViewportAddTownNames(DrawPixelInfo dpi)
	{
		//Town t;
		//int left, top, right, bottom;

		if( (0 == (Global._display_opt & Global.DO_SHOW_TOWN_NAMES)) || Global._game_mode == GameModes.GM_MENU)
			return;

		//left = dpi.left;
		//top = dpi.top;
		//right = left + dpi.width;
		//bottom = top + dpi.height;

		Rect rect = new Rect(dpi);
		
		if (dpi.zoom < 1) {

			Iterator<Town> ii = Town.getIterator();
			while(ii.hasNext())
			{
				Town t = ii.next();

				if (t.getXy() != null &&
						//bottom > t.sign.top &&
						//top < t.sign.top + 12 &&
						//right > t.sign.left &&
						//left < t.sign.left + t.sign.width_1
						t.getSign().intersects( rect, t.getSign().getWidth_1(), 12 )
						) {

					//AddStringToDraw(t.getSign().left + 1, t.sign.top + 1,
					//		Global._patches.population_in_label ? Str.STR_TOWN_LABEL_POP : Str.STR_TOWN_LABEL,
					//				t.index, t.getPopulation(), 0);
					t.getSign().draw(
							Global._patches.population_in_label ? Str.STR_TOWN_LABEL_POP : Str.STR_TOWN_LABEL,
									t.index, t.getPopulation(), 0);
				}
			}
		} else if (dpi.zoom == 1) {
			rect.right += 2;
			rect.bottom += 2;

			Iterator<Town> ii = Town.getIterator();
			while(ii.hasNext())
			{
				Town t = ii.next();

				if (t.getXy() != null &&
						//bottom > t.sign.top &&
						//top < t.sign.top + 24 &&
						//right > t.sign.left &&
						//left < t.sign.left + t.sign.width_1*2 
					t.getSign().intersects( rect, t.getSign().getWidth_1()*2, 24 )
				) {
					//AddStringToDraw(t.sign.left + 1, t.sign.top + 1,
					//		Global._patches.population_in_label ? Str.STR_TOWN_LABEL_POP : Str.STR_TOWN_LABEL,
					//				t.index, t.getPopulation(), 0);
					t.getSign().draw( Global._patches.population_in_label ? Str.STR_TOWN_LABEL_POP : Str.STR_TOWN_LABEL,
							t.index, t.getPopulation(), 0);
				}
			}
		} else {
			rect.right += 4;
			rect.bottom += 5;

			assert(dpi.zoom == 2);

			Iterator<Town> ii = Town.getIterator();
			while(ii.hasNext())
			{
				Town t = ii.next();

				if (t.getXy() != null &&
						//bottom > t.sign.top &&
						//top < t.sign.top + 24 &&
						//right > t.sign.left &&
						//left < t.sign.left + t.sign.width_2*4
					t.getSign().intersects( rect, t.getSign().getWidth_2()*4, 24 )
						) {
					//ViewPort.AddStringToDraw(t.sign.left + 5, t.sign.top + 1, Str.STR_TOWN_LABEL_TINY_BLACK, t.index, 0, 0);
					t.getSign().draw( 5, 1, Str.STR_TOWN_LABEL_TINY_BLACK, t.index, 0, 0);
					//ViewPort.AddStringToDraw(t.sign.left + 1, t.sign.top - 3, Str.STR_TOWN_LABEL_TINY_WHITE, t.index, 0, 0);
					t.getSign().draw( 1, -3, Str.STR_TOWN_LABEL_TINY_WHITE, t.index, 0, 0);
				}
			}
		}
	}

	static void ViewportAddStationNames(DrawPixelInfo dpi)
	{
		//int left, top, right, bottom;

		if (0 == (Global._display_opt & Global.DO_SHOW_STATION_NAMES) || Global._game_mode == GameModes.GM_MENU)
			return;

		//left = dpi.left;
		//top = dpi.top;
		//right = left + dpi.width;
		//bottom = top + dpi.height;
		Rect rect = new Rect(dpi);

		if (dpi.zoom < 1) {

			Iterator<Station> ii = Station.getIterator();
			while(ii.hasNext())
			{
				Station st = ii.next();
				if (st.getXy() != null &&
						//bottom > st.sign.top &&
						//top < st.sign.top + 12 &&
						//right > st.sign.left &&
						//left < st.sign.left + st.sign.width_1
					st.getSign().intersects( rect, st.getSign().getWidth_1(), 12 )
						) {

					//StringSpriteToDraw sstd=ViewPort.AddStringToDraw(st.sign.left + 1, st.sign.top + 1, Str.STR_305C_0, st.index, st.facilities, 0);
					StringSpriteToDraw sstd=st.getSign().draw( Str.STR_305C_0, st.getIndex(), st.getFacilities(), 0);
					if (sstd != null) {
						sstd.color = (st.getOwner().isNone() || st.getOwner().isTown() || st.hasNoFacilities()) ? 0xE : Global.gs._player_colors[st.getOwner().id];
						sstd.width = st.getSign().getWidth_1();
					}
				}
			}
		} else if (dpi.zoom == 1) {
			rect.right += 2;
			rect.bottom += 2;

			Iterator<Station> ii = Station.getIterator();
			while(ii.hasNext())
			{
				Station st = ii.next();

				if (st.getXy() != null &&
						//bottom > st.sign.top &&
						//top < st.sign.top + 24 &&
						//right > st.sign.left &&
						//left < st.sign.left + st.sign.width_1*2
					st.getSign().intersects( rect, st.getSign().getWidth_1()*2, 24 )
						) {

					//StringSpriteToDraw sstd= AddStringToDraw(st.sign.left + 1, st.sign.top + 1, Str.STR_305C_0, st.getIndex(), st.getFacilities(), 0);
					StringSpriteToDraw sstd= st.getSign().draw(Str.STR_305C_0, st.getIndex(), st.getFacilities(), 0);
					if (sstd != null) {
						sstd.color = (st.getOwner().isNone() || st.getOwner().isTown() || st.hasNoFacilities()) ? 0xE : Global.gs._player_colors[st.getOwner().id];
						sstd.width = st.getSign().getWidth_1();
					}
				}
			}

		} else {
			assert(dpi.zoom == 2);

			rect.right += 4;
			rect.bottom += 5;

			Iterator<Station> ii = Station.getIterator();
			while(ii.hasNext())
			{
				Station st = ii.next();
				if (st.getXy() != null &&
						//bottom > st.sign.top &&
						//top < st.sign.top + 24 &&
						//right > st.sign.left &&
						//left < st.sign.left + st.sign.width_2*4
					st.getSign().intersects( rect, st.getSign().getWidth_2()*4, 24 )
						) {

					//StringSpriteToDraw sstd=ViewPort.AddStringToDraw(st.sign.left + 1, st.sign.top + 1, Str.STR_STATION_SIGN_TINY, st.getIndex(), st.getFacilities(), 0);
					StringSpriteToDraw sstd=st.getSign().draw(Str.STR_STATION_SIGN_TINY, st.getIndex(), st.getFacilities(), 0);
					if (sstd != null) {
						//sstd.color = (st.getOwner().isNone() || st.getOwner().isTown() || 0 == st.facilities) ? 0xE : Global.gs._player_colors[st.getOwner().id];
						sstd.color = (st.getOwner().isNone() || st.getOwner().isTown() || st.hasNoFacilities()) ? 0xE : Global.gs._player_colors[st.getOwner().id];
						sstd.width = st.getSign().getWidth_2() | 0x8000;
					}
				}
			}
		}
	}

	static void ViewportAddSigns(DrawPixelInfo dpi)
	{
		//int left, top, right, bottom;

		if (0 == (Global._display_opt & Global.DO_SHOW_SIGNS))
			return;

		//left = dpi.left;
		//top = dpi.top;
		//right = left + dpi.width;
		//bottom = top + dpi.height;
		Rect rect = new Rect(dpi);

		Iterator<SignStruct> ii = SignStruct.getIterator();
		
		if (dpi.zoom < 1) 
		{
			while(ii.hasNext())
			{
				SignStruct ss = ii.next();
				//ss.draw(left, top, right, bottom, dpi.zoom);
				ss.draw(rect, dpi.zoom);
			}
		} else if (dpi.zoom == 1) {
			rect.right += 2;
			rect.bottom += 2;

			while(ii.hasNext())
			{
				SignStruct ss = ii.next();
				//ss.draw(left, top, right, bottom, dpi.zoom);
				ss.draw(rect, dpi.zoom);

				/*if (ss.str != null &&
						bottom > ss.sign.top &&
						top < ss.sign.top + 24 &&
						right > ss.sign.left &&
						left < ss.sign.left + ss.sign.width_1*2) {

					StringSpriteToDraw sstd=AddStringToDraw(ss.sign.left + 1, ss.sign.top + 1, new StringID(Str.STR_2806), ss.str.id, 0, 0);
					if (sstd != null) {
						sstd.width = ss.sign.width_1;
						sstd.color = (ss.owner.isNone() || ss.owner.isTown())?14:Global.gs._player_colors[ss.owner.id];
					}
				}*/
			}
		} else {
			rect.right += 4;
			rect.bottom += 5;


			while(ii.hasNext())
			{
				SignStruct ss = ii.next();
				//ss.draw(left, top, right, bottom, dpi.zoom);
				ss.draw(rect, dpi.zoom);

				/*
				if (ss.str != null &&
						bottom > ss.sign.top &&
						top < ss.sign.top + 24 &&
						right > ss.sign.left &&
						left < ss.sign.left + ss.sign.width_2*4) {

					StringSpriteToDraw sstd=AddStringToDraw(ss.sign.left + 1, ss.sign.top + 1, new StringID(Str.STR_2002), ss.str.id, 0, 0);
					if (sstd != null) {
						sstd.width = ss.sign.width_2 | 0x8000;
						sstd.color = (ss.owner.id==Owner.OWNER_NONE || ss.owner.isTown())?14:Global.gs._player_colors[ss.owner.id];
					}
				}*/
			}
		}
	}

	static void ViewportAddWaypoints(DrawPixelInfo dpi)
	{
		//int left, top, right, bottom;

		if (0 == (Global._display_opt & Global.DO_WAYPOINTS))
			return;

		//left = dpi.left;
		//top = dpi.top;
		//right = left + dpi.width;
		//bottom = top + dpi.height;
		Rect rect = new Rect(dpi);

		if (dpi.zoom < 1) {

			Iterator<WayPoint> ii = WayPoint.getIterator();
			while(ii.hasNext())
			{
				WayPoint wp = ii.next();
				if (wp.isValid() &&
						//bottom > wp.sign.top &&
						//top < wp.sign.top + 12 &&
						//right > wp.sign.left &&
						//left < wp.sign.left + wp.sign.width_1
						wp.getSign().intersects( rect, wp.getSign().getWidth_1(), 12 )
						) {

					StringSpriteToDraw sstd = wp.sign.draw(Str.STR_WAYPOINT_VIEWPORT, wp.index, 0, 0);
					if (sstd != null) {
						sstd.width = wp.sign.getWidth_1();
						sstd.color = (wp.deleted != 0 ? 0xE : 11);
					}
				}
			}
		} else if (dpi.zoom == 1) {
			rect.right += 2;
			rect.bottom += 2;
 
			Iterator<WayPoint> ii = WayPoint.getIterator();
			while(ii.hasNext())
			{
				WayPoint wp = ii.next();
				if (wp.isValid() &&
						//bottom > wp.sign.top &&
						//top < wp.sign.top + 24 &&
						//right > wp.sign.left &&
						//left < wp.sign.left + wp.sign.width_1*2
					wp.getSign().intersects( rect, wp.getSign().getWidth_1()*2, 24 )
						) {
					StringSpriteToDraw sstd = wp.sign.draw(Str.STR_WAYPOINT_VIEWPORT, wp.index, 0, 0);
					if (sstd != null) {
						sstd.width = wp.sign.getWidth_1();
						sstd.color = (wp.deleted != 0 ? 0xE : 11);
					}
				}
			}
		} else {
			rect.right += 4;
			rect.bottom += 5;

			Iterator<WayPoint> ii = WayPoint.getIterator();
			while(ii.hasNext())
			{
				WayPoint wp = ii.next();
				if (wp.isValid() &&
						//bottom > wp.sign.top &&
						//top < wp.sign.top + 24 &&
						//right > wp.sign.left &&
						//left < wp.sign.left + wp.sign.getWidth_2()*4
						wp.getSign().intersects( rect, wp.getSign().getWidth_2()*4, 24 )
						) {

					StringSpriteToDraw sstd = wp.sign.draw(Str.STR_WAYPOINT_VIEWPORT_TINY, wp.index, 0, 0);
					if (sstd != null) {
						sstd.width = wp.sign.getWidth_2() | 0x8000;
						sstd.color = (wp.deleted != 0 ? 0xE : 11);
					}
				}
			}
		}
	}

	public static void UpdateViewportSignPos(ViewportSign sign, int left, int top, int str)
	{
		String buffer;
		int w;

		sign.setTop(top);

		buffer = Strings.GetString(str);
		w = Gfx.GetStringWidth(buffer) + 3;
		sign.setWidth_1(w);
		sign.setLeft(left - w / 2);

		// zoomed out version
		Gfx._stringwidth_base = 0xE0;
		w = Gfx.GetStringWidth(buffer) + 3;
		Gfx._stringwidth_base = 0;
		sign.setWidth_2(w);
	}

	
	
	//public static List<TileIndex> markTilesBlue = new ArrayList<>();
	public static List<TileMarker> markTiles = new ArrayList<>();

	public static void clearTileMarkers()
	{
		markTiles.clear();
		//markTilesRed.clear();
	}
	static void ViewportDrawTileSprites(List<TileSpriteToDraw> tiles)
	{
		for( TileSpriteToDraw ts : tiles )
		{
			Point pt = Point.RemapCoords(ts.x, ts.y, ts.z);
			Gfx.DrawSprite(ts.image, pt.x, pt.y);
			
			// [dz] mark tiles with point
			//Point ptr = new Point(pt.x, pt.y+12); 
			//Gfx.GfxFillRect(ptr.x, ptr.y, ptr.x+3, ptr.y+3, 239); // flashing: 234/239 red 222 blue
		}
		
		//markTiles.clear();
		//if(markTiles.isEmpty()) markTiles.add(TileIndex.get(3333));
		//if(markTiles.isEmpty()) markTiles.add(TileIndex.get(1));
		
		/*for(TileIndex tile : markTilesBlue)
		{
			Point pt = Point.RemapCoords(tile);
			Gfx.GfxFillRect(pt.x-1, pt.y-1, pt.x+3, pt.y+3, 222); // flashing: 234/239 red 222 blue
			// [dz] mark tiles with point
			//Point ptr = new Point(pt.x, pt.y+12); 
			//Gfx.GfxFillRect(ptr.x, ptr.y, ptr.x+3, ptr.y+3, 239); // flashing: 234/239 red 222 blue
		}*/

		for(TileMarker m : markTiles)
		{
			Point pt = Point.RemapCoords(m.getTile());
			Gfx.GfxFillRect(pt.x-1, pt.y-1, pt.x+3, pt.y+3, m.getColor()); // flashing: 234/239 red 222 blue
		}
	}

	static void ViewportSortParentSprites(ParentSpriteToDraw [] psd)
	{
		int pp = 0;
		while ( true ) 
		{
			if(pp >= psd.length) break;
			ParentSpriteToDraw  ps = psd[pp];
			if(ps == null)
				break;

			if (0 == (ps.unk16 & 1)) {
				//ParentSpriteToDraw  psd2 = psd;
				int pp2 = pp;
				ps.unk16 |= 1;

				//while (*++psd2 != null) 
				while (true) 
				{
					pp2++;
					if( pp2 >= psd.length) break;
					ParentSpriteToDraw  ps2 = psd[pp2];
					if(ps2 == null)
						break;

					boolean mustswap = false;

					if( 0 != (ps2.unk16 & 1) ) continue;

					// Decide which sort order algorithm to use, based on whether the sprites have some overlapping area.
					if (((ps2.tile_x > ps.tile_x && ps2.tile_x < ps.tile_right) ||
							(ps2.tile_right > ps.tile_x && ps2.tile_x < ps.tile_right)) &&        // overlap in X
							((ps2.tile_y > ps.tile_y && ps2.tile_y < ps.tile_bottom) ||
									(ps2.tile_bottom > ps.tile_y && ps2.tile_y < ps.tile_bottom)) &&      // overlap in Y
							((ps2.tile_z > ps.tile_z && ps2.tile_z < ps.tile_z_bottom) ||
									(ps2.tile_z_bottom > ps.tile_z && ps2.tile_z < ps.tile_z_bottom)) ) { // overlap in Z
						// Sprites overlap.
						// Use X+Y+Z as the sorting order, so sprites nearer the bottom of the screen,
						// and with higher Z elevation, draw in front.
						// Here X,Y,Z are the coordinates of the "center of mass" of the sprite,
						// i.e. X=(left+right)/2, etc.
						// However, since we only care about order, don't actually calculate the division / 2.
						mustswap = ps.tile_x + ps.tile_right + ps.tile_y + ps.tile_bottom + ps.tile_z + ps.tile_z_bottom >
						ps2.tile_x + ps2.tile_right + ps2.tile_y + ps2.tile_bottom + ps2.tile_z + ps2.tile_z_bottom;
					} else {
						// No overlap; use the original TTD sort algorithm.
						mustswap = (ps.tile_right >= ps2.tile_x &&
								ps.tile_bottom >= ps2.tile_y &&
								ps.tile_z_bottom >= ps2.tile_z &&
								(ps.tile_x >= ps2.tile_right ||
								ps.tile_y >= ps2.tile_bottom ||
								ps.tile_z >= ps2.tile_z_bottom));
					}
					if (mustswap) {
						// Swap the two sprites ps and ps2 using bubble-sort algorithm.
						//ParentSpriteToDraw  psd3 = psd;
						int pp3 = pp;
						do {
							/*
							ParentSpriteToDraw  temp = *psd3;
							 *psd3 = ps2;
							ps2 = temp;
							 */
							ParentSpriteToDraw  temp = psd[pp3];
							psd[pp3] = psd[pp2];
							psd[pp2] = temp;

							//psd3++;
							pp3++;
							//} while (psd3 <= psd2);
						} while (pp3 <= pp2);
					}
				}
			} else {
				//psd++;
				pp++;
			}
		}
	}

	static void ViewportDrawParentSprites(ParentSpriteToDraw psd[])
	{
		//for (; *psd != null; psd++) 
		for (final ParentSpriteToDraw ps : psd) {
			//final ParentSpriteToDraw  ps = *psd;
			if (ps == null) break;

			Point pt = Point.RemapCoords(ps.tile_x, ps.tile_y, ps.tile_z);

			//final ChildScreenSpriteToDraw cs;

			Gfx.DrawSprite(ps.image, pt.x, pt.y);

			//for (cs = ps.child; cs != null; cs = cs.next) 
			for (ChildScreenSpriteToDraw cs : ps.children) {
				Gfx.DrawSprite(cs.image, ps.left + cs.x, ps.top + cs.y);
			}
		}
	}

	//static void ViewportDrawStrings(DrawPixelInfo dpi, final StringSpriteToDraw ss)
	static void ViewportDrawStrings(DrawPixelInfo dpi, List<StringSpriteToDraw> ssl )
	{
		DrawPixelInfo dp = new DrawPixelInfo();
		int zoom;

		Hal._cur_dpi = dp;
		dp.assignFrom( dpi );

		zoom = dp.zoom;
		dp.zoom = 0;

		dp.left >>= zoom;
		dp.top >>= zoom;
		dp.width >>= zoom;
		dp.height >>= zoom;

		//do 
		for( StringSpriteToDraw ss : ssl )
		{
			if (ss.width != 0) {
				int x = (ss.x >> zoom) - 1;
				int y = (ss.y >> zoom) - 1;
				int bottom = y + 11;
				int w = ss.width;

				if( 0 != (w & 0x8000) ) {
					w &= ~0x8000;
					y--;
					bottom -= 6;
					w -= 3;
				}

				/* Draw the rectangle if 'tranparent station signs' is off,
				 * or if we are drawing a general text sign (Str.STR_2806) */
				if (0 == (Global._display_opt & Global.DO_TRANS_SIGNS) || ss.string == Str.STR_2806)
					Gfx.DrawFrameRect(
							x, y, x + w, bottom, ss.color,
							(Global._display_opt & Global.DO_TRANS_BUILDINGS) != 0 ? Window.FR_TRANSPARENT | Window.FR_NOBORDER : 0
							);
			}

			Global.SetDParam(0, ss.params[0]);
			Global.SetDParam(1, ss.params[1]);
			Global.SetDParam(2, ss.params[2]);
			/* if we didn't draw a rectangle, or if transparant building is on,
			 * draw the text in the color the rectangle would have */
			if ((
					(Global._display_opt & Global.DO_TRANS_BUILDINGS)!=0 ||
					((Global._display_opt & Global.DO_TRANS_SIGNS) != 0 && ss.string != Str.STR_2806)
					) && ss.width != 0) {
				/* Real colors need the IS_PALETTE_COLOR flag
				 * otherwise colors from _string_colormap are assumed. */
				Gfx.DrawString(
						ss.x >> zoom, (ss.y >> zoom) - ((ss.width & 0x8000) != 0 ? 2 : 0),
						ss.string, (Global._color_list[ss.color].window_color_bgb | Gfx.IS_PALETTE_COLOR)
						);
			} else {
				Gfx.DrawString(
						ss.x >> zoom, (ss.y >> zoom) - ((ss.width & 0x8000) != 0 ? 2 : 0),
						ss.string, 16
						);
			}

			//	ss = ss.next;
		}// while (ss != null);

		Hal._cur_dpi = dpi;
	}

	public static void ViewportDoDraw(final ViewPort vp, int left, int top, int right, int bottom)
	{
		ViewportDrawer vd = new ViewportDrawer();
		int mask;
		int x;
		int y;
		DrawPixelInfo old_dpi;

		_cur_vd = vd;

		old_dpi = Hal._cur_dpi;
		Hal._cur_dpi = vd.dpi;

		vd.dpi.zoom = vp.zoom;
		mask = (-1) << vp.zoom;

		vd.combine_sprites = 0;
		vd.ground_child = false;

		vd.dpi.width = (right - left) & mask;
		vd.dpi.height = (bottom - top) & mask;
		vd.dpi.left = left & mask;
		vd.dpi.top = top & mask;
		vd.dpi.pitch = old_dpi.pitch;

		x = ((vd.dpi.left - (vp.virtual_left&mask)) >> vp.zoom) + vp.left;
		y = ((vd.dpi.top - (vp.virtual_top&mask)) >> vp.zoom) + vp.top;

		//vd.dpi.dst_ptr = old_dpi.dst_ptr;
		//vd.dpi.dst_ptr_shift =  x - old_dpi.left + (y - old_dpi.top) * old_dpi.pitch;

		vd.dpi.dst_ptr = new Pixel(old_dpi.dst_ptr, x - old_dpi.left + (y - old_dpi.top) * old_dpi.pitch );

		//vd.parent_list = parent_list;
		//vd.eof_parent_list = endof(parent_list);
		//vd.spritelist_mem = mem;
		//vd.eof_spritelist_mem = endof(mem) - sizeof(LARGEST_SPRITELIST_STRUCT);
		//vd.last_string = vd.first_string;
		//vd.first_string = null;
		//vd.last_tile = vd.first_tile;
		//vd.first_tile = null;


		ViewportAddLandscape();

		//#if !defined(NEW_ROTATION)
		Vehicle.ViewportAddVehicles(vd.dpi);
		TextEffect.DrawTextEffects(vd.dpi);

		ViewportAddTownNames(vd.dpi);
		ViewportAddStationNames(vd.dpi);
		ViewportAddSigns(vd.dpi);
		ViewportAddWaypoints(vd.dpi);
		//#endif


		ViewportDrawTileSprites(vd.tile_list);


		ParentSpriteToDraw[] parents = vd.parent_list.toArray(ParentSpriteToDraw[]::new);
		if(parents.length > 0)
		{
			ViewportSortParentSprites(parents);
			ViewportDrawParentSprites(parents);
		}

		ViewportDrawStrings(vd.dpi, vd.string_list);

		Hal._cur_dpi = old_dpi;
	}

	// Make sure we don't draw a too big area at a time.
	// If we do, the sprite memory will overflow.
	static void ViewportDrawChk(ViewPort vp, int left, int top, int right, int bottom)
	{
		if (((bottom - top) * (right - left) << vp.zoom) > 180000) {
			if ((bottom - top) > (right - left)) {
				int t = (top + bottom) >> 1;
				ViewportDrawChk(vp, left, top, right, t);
				ViewportDrawChk(vp, left, t, right, bottom);
			} else {
				int t = (left + right) >> 1;
				ViewportDrawChk(vp, left, top, t, bottom);
				ViewportDrawChk(vp, t, top, right, bottom);
			}
		} else {
			ViewportDoDraw(vp,
					((left - vp.left) << vp.zoom) + vp.virtual_left,
					((top - vp.top) << vp.zoom) + vp.virtual_top,
					((right - vp.left) << vp.zoom) + vp.virtual_left,
					((bottom - vp.top) << vp.zoom) + vp.virtual_top
					);
		}
	}

	static void ViewportDraw(ViewPort vp, int left, int top, int right, int bottom)
	{
		if (right <= vp.left || bottom <= vp.top) return;

		if (left >= vp.left + vp.width) return;

		if (left < vp.left) left = vp.left;
		if (right > vp.left + vp.width) right = vp.left + vp.width;

		if (top >= vp.top + vp.height) return;

		if (top < vp.top) top = vp.top;
		if (bottom > vp.top + vp.height) bottom = vp.top + vp.height;

		ViewportDrawChk(vp, left, top, right, bottom);
	}

	static void DrawWindowViewport(Window w)
	{
		DrawPixelInfo dpi = Hal._cur_dpi;

		dpi.left += w.left;
		dpi.top += w.top;

		ViewportDraw(w.getViewport(), dpi.left, dpi.top, dpi.left + dpi.width, dpi.top + dpi.height);

		dpi.left -= w.left;
		dpi.top -= w.top;
	}

	static void UpdateViewportPosition(Window w)
	{
		final ViewPort vp = w.getViewport();

		if (w.as_vp_d().follow_vehicle != Vehicle.INVALID_VEHICLE) {
			final Vehicle veh = Vehicle.GetVehicle(w.as_vp_d().follow_vehicle);
			Point pt = MapXYZToViewport(vp, veh.getX_pos(), veh.getY_pos(), veh.getZ_pos());

			SetViewportPosition(w, pt.x, pt.y);
		} else {
			//#if !defined(NEW_ROTATION)
			int x;
			int y;
			int vx;
			int vy;

			// Center of the viewport is hot spot
			x = w.as_vp_d().scrollpos_x + vp.virtual_width / 2;
			y = w.as_vp_d().scrollpos_y + vp.virtual_height / 2;
			// Convert viewport coordinates to map coordinates
			// Calculation is scaled by 4 to avoid rounding errors
			vx = -x + y * 2;
			vy =  x + y * 2;
			// clamp to size of map
			vx = BitOps.clamp(vx, 0 * 4, Global.MapMaxX() * 16 * 4);
			vy = BitOps.clamp(vy, 0 * 4, Global.MapMaxY() * 16 * 4);
			// Convert map coordinates to viewport coordinates
			x = (-vx + vy) / 2;
			y = ( vx + vy) / 4;
			// Set position
			w.as_vp_d().scrollpos_x = x - vp.virtual_width / 2;
			w.as_vp_d().scrollpos_y = y - vp.virtual_height / 2;
			/*#else
			int x,y,t;
			int err;

			x = w.as_vp_d().scrollpos_x >> 2;
			y = w.as_vp_d().scrollpos_y >> 1;

			t = x;
			x = x + y;
			y = x - y;
			err= 0;

			if (err != 0) {
				// coordinate remap 
				Point pt = Point.RemapCoords(x, y, 0);
				t = (-1) << vp.zoom;
				w.as_vp_d().scrollpos_x = pt.x & t;
				w.as_vp_d().scrollpos_y = pt.y & t;
			}
	#endif */

			SetViewportPosition(w, w.as_vp_d().scrollpos_x, w.as_vp_d().scrollpos_y);
		}
	}

	static void MarkViewportDirty(final ViewPort vp, int left, int top, int right, int bottom)
	{
		right -= vp.virtual_left;
		if (right <= 0) return;

		bottom -= vp.virtual_top;
		if (bottom <= 0) return;

		left = Math.max(0, left - vp.virtual_left);

		if (left >= vp.virtual_width) return;

		top = Math.max(0, top - vp.virtual_top);

		if (top >= vp.virtual_height) return;

		Gfx.SetDirtyBlocks(
				(left >> vp.zoom) + vp.left,
				(top >> vp.zoom) + vp.top,
				(right >> vp.zoom) + vp.left,
				(bottom >> vp.zoom) + vp.top
				);
	}

	public static void MarkAllViewportsDirty(int left, int top, int right, int bottom)
	{
		/*
		final ViewPort vp = _viewports;
		int act = _active_viewports;
		do {
			if (act & 1) {
				assert(vp.width != 0);
				MarkViewportDirty(vp, left, top, right, bottom);
			}
			vp++;
		} while (act>>=1);
		 */

		//int act = _active_viewports;
		for( ViewPort vp : _viewports )
		{
			//if (act & 1) {
			assert(vp.width != 0);
			MarkViewportDirty(vp, left, top, right, bottom);
			//}
			//if( act>>=1 <= 0 ) break;
		}
	}


	/*@Deprecated
	void MarkTileDirtyByTile(TileIndex tile)
	{
		tile.MarkTileDirtyByTile();		
	}*/	
	/*
	void MarkTileDirtyByTile(TileIndex tile)
	{
		Point pt = Point.RemapCoords(tile.TileX() * 16, tile.TileY() * 16, tile.GetTileZ());
		MarkAllViewportsDirty(
			pt.x - 31,
			pt.y - 122,
			pt.x - 31 + 67,
			pt.y - 122 + 154
		);
	}*/

	static void MarkTileDirty(int x, int y)
	{
		int z = 0;
		Point pt;

		if (BitOps.IS_INT_INSIDE(x, 0, Global.MapSizeX() * 16) &&
				BitOps.IS_INT_INSIDE(y, 0, Global.MapSizeY() * 16))
			z = TileIndex.TileVirtXY(x, y).GetTileZ();
		
		pt = Point.RemapCoords(x, y, z);
		//Global.debug("MarkTileDirty %s", pt.toString() );
		MarkAllViewportsDirty(
				pt.x - 31,
				pt.y - 122,
				pt.x - 31 + 67,
				pt.y - 122 + 154
				);
	}

	static void SetSelectionTilesDirty()
	{
		int y_size, x_size;
		int x = _thd.pos.x;
		int y = _thd.pos.y;

		//Global.debug("SetSelectionTilesDirty %d.%d", x, y );
		
		x_size = _thd.size.x;
		y_size = _thd.size.y;

		if (0 != _thd.outersize.x) {
			x_size += _thd.outersize.x;
			x += _thd.offs.x;
			y_size += _thd.outersize.y;
			y += _thd.offs.y;
		}

		assert(x_size > 0);
		assert(y_size > 0);

		x_size += x;
		y_size += y;

		do {
			int y_bk = y;
			do {
				MarkTileDirty(x, y);
			} while ( (y+=16) != y_size);
			y = y_bk;
		} while ( (x+=16) != x_size);
	}


	public static void SetSelectionRed(boolean b)
	{
		_thd.make_square_red = b;
		SetSelectionTilesDirty();
	}


	static boolean CheckClickOnTown(final ViewPort vp, int x, int y)
	{
		//Town t;

		if (0 == (Global._display_opt & Global.DO_SHOW_TOWN_NAMES)) return false;


		if (vp.zoom < 1) {
			x = x - vp.left + vp.virtual_left;
			y = y - vp.top + vp.virtual_top;

			//FOR_ALL_TOWNS(t) 
			Iterator<Town> i = Town.getIterator();
			while(i.hasNext())
			{
				Town t = i.next();
				if (t.getXy() != null &&
						//y >= t.sign.top &&
						//y < t.sign.top + 12 &&
						//x >= t.sign.left &&
						//x < t.sign.left + t.sign.width_1
					t.getSign().pointIn(x, y, t.getSign().getWidth_1(), 12 )
					) {
					TownGui.ShowTownViewWindow(t.index);
					return true;
				}
			}
		} else if (vp.zoom == 1) {
			x = (x - vp.left + 1) * 2 + vp.virtual_left;
			y = (y - vp.top + 1) * 2 + vp.virtual_top;

			//FOR_ALL_TOWNS(t) 
			Iterator<Town> i = Town.getIterator();
			while(i.hasNext())
			{
				Town t = i.next();
				if (t.getXy() != null &&
						//y >= t.sign.top &&
						//y < t.sign.top + 24 &&
						//x >= t.sign.left &&
						//x < t.sign.left + t.sign.width_1 * 2
						t.getSign().pointIn(x, y, t.getSign().getWidth_1()*2, 24 )
						) {
					TownGui.ShowTownViewWindow(t.index);
					return true;
				}
			}
		} else {
			x = (x - vp.left + 3) * 4 + vp.virtual_left;
			y = (y - vp.top + 3) * 4 + vp.virtual_top;

			//FOR_ALL_TOWNS(t) 
			Iterator<Town> i = Town.getIterator();
			while(i.hasNext())
			{
				Town t = i.next();
				if (t.getXy() != null &&
						//y >= t.sign.top &&
						//y < t.sign.top + 24 &&
						//x >= t.sign.left &&
						//x < t.sign.left + t.sign.width_2 * 4
					t.getSign().pointIn(x, y, t.getSign().getWidth_2()*4, 24 )
						) {
					TownGui.ShowTownViewWindow(t.index);
					return true;
				}
			}
		}

		return false;
	}

	static boolean CheckClickOnStation(final ViewPort vp, int x, int y)
	{
		if (0 == (Global._display_opt & Global.DO_SHOW_STATION_NAMES)) return false;


		if (vp.zoom < 1) {
			x = x - vp.left + vp.virtual_left;
			y = y - vp.top + vp.virtual_top;

			Iterator<Station> i = Station.getIterator();
			while(i.hasNext())
			{
				Station st = i.next();
				if (st.getXy() != null &&
						//y >= st.sign.top &&
						//y < st.sign.top + 12 &&
						//x >= st.sign.left &&
						//x < st.sign.left + st.sign.width_1
					st.getSign().pointIn(x, y, st.getSign().getWidth_1(), 12 )
						) {
					StationGui.ShowStationViewWindow(st.getIndex());
					return true;
				}
			}
		} else if (vp.zoom == 1) {
			x = (x - vp.left + 1) * 2 + vp.virtual_left;
			y = (y - vp.top + 1) * 2 + vp.virtual_top;

			Iterator<Station> i = Station.getIterator();
			while(i.hasNext())
			{
				Station st = i.next();
				if (st.getXy() != null &&
						//y >= st.sign.top &&
						//y < st.sign.top + 24 &&
						//x >= st.sign.left &&
						//x < st.sign.left + st.sign.width_1 * 2
					st.getSign().pointIn(x, y, st.getSign().getWidth_1()*2, 24 )
						) {
					StationGui.ShowStationViewWindow(st.getIndex());
					return true;
				}
			}
		} else {
			x = (x - vp.left + 3) * 4 + vp.virtual_left;
			y = (y - vp.top + 3) * 4 + vp.virtual_top;

			Iterator<Station> i = Station.getIterator();
			while(i.hasNext())
			{
				Station st = i.next();
				if (st.getXy() != null &&
						//y >= st.sign.top &&
						//y < st.sign.top + 24 &&
						//x >= st.sign.left &&
						//x < st.sign.left + st.sign.width_2 * 4
					st.getSign().pointIn(x, y, st.getSign().getWidth_2()*4, 24 )
						) {
					StationGui.ShowStationViewWindow(st.getIndex());
					return true;
				}
			}
		}

		return false;
	}

	static boolean CheckClickOnSign(final ViewPort vp, int x, int y)
	{
		if (0 == (Global._display_opt & Global.DO_SHOW_SIGNS)) return false;
		Iterator<SignStruct> i = SignStruct.getIterator();

		if (vp.zoom < 1) {
			x = x - vp.left + vp.virtual_left;
			y = y - vp.top + vp.virtual_top;

			while(i.hasNext())
			{
				SignStruct ss = i.next();
				/*if (ss.str != null &&
						y >= ss.sign.top &&
						y < ss.sign.top + 12 &&
						x >= ss.sign.left &&
						x < ss.sign.left + ss.sign.width_1) */ 
				if(ss.clickIn( x, y, vp.zoom ))
				{
					Gui.ShowRenameSignWindow(ss);
					return true;
				}
			}
		} else if (vp.zoom == 1) {
			x = (x - vp.left + 1) * 2 + vp.virtual_left;
			y = (y - vp.top + 1) * 2 + vp.virtual_top;

			while(i.hasNext())
			{
				SignStruct ss = i.next();
				/*if (ss.str != null &&
						y >= ss.sign.top &&
						y < ss.sign.top + 24 &&
						x >= ss.sign.left &&
						x < ss.sign.left + ss.sign.width_1 * 2)*/ 
				if(ss.clickIn( x, y, vp.zoom ))
				{
					Gui.ShowRenameSignWindow(ss);
					return true;
				}
			}
		} else {
			x = (x - vp.left + 3) * 4 + vp.virtual_left;
			y = (y - vp.top + 3) * 4 + vp.virtual_top;

			while(i.hasNext())
			{
				SignStruct ss = i.next();
				/*if (ss.str != null &&
						y >= ss.sign.top &&
						y < ss.sign.top + 24 &&
						x >= ss.sign.left &&
						x < ss.sign.left + ss.sign.width_2 * 4) */ 
				if(ss.clickIn( x, y, vp.zoom ))
				{
					Gui.ShowRenameSignWindow(ss);
					return true;
				}
			}
		}

		return false;
	}

	static boolean CheckClickOnWaypoint(final ViewPort vp, int x, int y)
	{
		if (0 == (Global._display_opt & Global.DO_WAYPOINTS)) return false;

		if (vp.zoom < 1) {
			x = x - vp.left + vp.virtual_left;
			y = y - vp.top + vp.virtual_top;

			Iterator<WayPoint> i = WayPoint.getIterator();
			while(i.hasNext())
			{
				WayPoint wp = i.next();
				if (wp.isValid() &&
						y >= wp.sign.getTop() &&
						y < wp.sign.getTop() + 12 &&
						x >= wp.sign.getLeft() &&
						x < wp.sign.getLeft() + wp.sign.getWidth_1()) {
					Gui.ShowRenameWaypointWindow(wp);
					return true;
				}
			}
		} else if (vp.zoom == 1) {
			x = (x - vp.left + 1) * 2 + vp.virtual_left;
			y = (y - vp.top + 1) * 2 + vp.virtual_top;

			Iterator<WayPoint> i = WayPoint.getIterator();
			while(i.hasNext())
			{
				WayPoint wp = i.next();
				if (wp.isValid() &&
						y >= wp.sign.getTop() &&
						y < wp.sign.getTop() + 24 &&
						x >= wp.sign.getLeft() &&
						x < wp.sign.getLeft() + wp.sign.getWidth_1() * 2) {
					Gui.ShowRenameWaypointWindow(wp);
					return true;
				}
			}
		} else {
			x = (x - vp.left + 3) * 4 + vp.virtual_left;
			y = (y - vp.top + 3) * 4 + vp.virtual_top;

			Iterator<WayPoint> i = WayPoint.getIterator();
			while(i.hasNext())
			{
				WayPoint wp = i.next();
				if (wp.isValid() &&
						y >= wp.sign.getTop() &&
						y < wp.sign.getTop() + 24 &&
						x >= wp.sign.getLeft() &&
						x < wp.sign.getLeft() + wp.sign.getWidth_2() * 4) {
					Gui.ShowRenameWaypointWindow(wp);
					return true;
				}
			}
		}

		return false;
	}


	static void CheckClickOnLandscape(final ViewPort vp, int x, int y)
	{
		Point pt = TranslateXYToTileCoord(vp, x, y);

		if (pt.x != -1) Landscape.ClickTile(TileIndex.TileVirtXY(pt.x, pt.y));
	}


	static void SafeShowTrainViewWindow(Vehicle v)
	{
		if (!v.IsFrontEngine()) 
			v = v.GetFirstVehicleInChain();
		TrainGui.ShowTrainViewWindow(v);
	}

	static void Nop(final Vehicle v) {/*empty*/}


	static final OnVehicleClickProc[] _on_vehicle_click_proc = {
			ViewPort::SafeShowTrainViewWindow,
			RoadVehGui::ShowRoadVehViewWindow,
			ShipGui::ShowShipViewWindow,
			AirCraft::ShowAircraftViewWindow,
			ViewPort::Nop, // Special vehicles
			ViewPort::Nop  // Disaster vehicles
	};

	void HandleViewportClicked(int x, int y)
	{
		final Vehicle v;
		final ViewPort vp = this;

		if (CheckClickOnTown(vp, x, y)) return;
		if (CheckClickOnStation(vp, x, y)) return;
		if (CheckClickOnSign(vp, x, y)) return;
		if (CheckClickOnWaypoint(vp, x, y)) return;
		CheckClickOnLandscape(vp, x, y);

		v = Vehicle.CheckClickOnVehicle(vp, x, y);
		if (v != null) _on_vehicle_click_proc[v.getType() - 0x10].accept(v);
	}

	public static Vehicle CheckMouseOverVehicle()
	{
		final Window  w;
		final ViewPort  vp;

		int x = Hal._cursor.pos.x;
		int y = Hal._cursor.pos.y;

		w = Window.FindWindowFromPt(x, y);
		if (w == null) return null;

		vp = w.IsPtInWindowViewport(x, y);
		return (vp != null) ? Vehicle.CheckClickOnVehicle(vp, x, y) : null;
	}



	static void PlaceObject()
	{
		Point pt;
		Window w;

		pt = GetTileBelowCursor();
		if (pt.x == -1) return;

		if (_thd.place_mode == VHM_POINT) {
			pt.x += 8;
			pt.y += 8;
		}

		_tile_fract_coords.x = pt.x & 0xF;
		_tile_fract_coords.y = pt.y & 0xF;

		w = Window.GetCallbackWnd();
		if (w != null) {
			WindowEvent e = new WindowEvent();

			e.event = WindowEvents.WE_PLACE_OBJ;
			e.pt = pt;
			e.tile = TileIndex.TileVirtXY(pt.x, pt.y);
			w.wndproc.accept(w, e);
		}
	}


	/* scrolls the viewport in a window to a given location */
	static boolean ScrollWindowTo(int x , int y, Window  w)
	{
		Point pt;

		pt = MapXYZToViewport(w.getViewport(), x, y, Landscape.GetSlopeZ(x, y));
		w.as_vp_d().follow_vehicle = VehicleID.get( Vehicle.INVALID_VEHICLE ).id;

		if (w.as_vp_d().scrollpos_x == pt.x && w.as_vp_d().scrollpos_y == pt.y)
			return false;

		w.as_vp_d().scrollpos_x = pt.x;
		w.as_vp_d().scrollpos_y = pt.y;
		return true;
	}

	/* scrolls the viewport in a window to a given tile */
	static boolean ScrollWindowToTile(TileIndex tile, Window  w)
	{
		return ScrollWindowTo(tile.TileX() * 16 + 8, tile.TileY() * 16 + 8, w);
	}



	public static boolean ScrollMainWindowTo(int x, int y)
	{
		return ScrollWindowTo(x, y, Window.getMain());
	}


	public static boolean ScrollMainWindowToTile(TileIndex tile)
	{
		if(tile == null)
		{
			Global.error("null tile in ScrollMainWindowToTile");
			return false;
		}
		return ScrollMainWindowTo(tile.TileX() * 16 + 8, tile.TileY() * 16 + 8);
	}

	public static void SetRedErrorSquare(TileIndex tile)
	{
		TileIndex old;

		old = _thd.redsq;
		_thd.redsq = tile;

		if (tile == null || !tile.equals(old)) {
			if (tile != null) tile.MarkTileDirtyByTile();
			if (old  != null) old.MarkTileDirtyByTile();
		}
	}

	public static void SetTileSelectSize(int w, int h)
	{
		_thd.new_size.x = w * 16;
		_thd.new_size.y = h * 16;
		_thd.new_outersize.x = 0;
		_thd.new_outersize.y = 0;
	}

	public static void SetTileSelectBigSize(int ox, int oy, int sx, int sy)
	{
		_thd.offs.x = ox * 16;
		_thd.offs.y = oy * 16;
		_thd.new_outersize.x = sx * 16;
		_thd.new_outersize.y = sy * 16;
	}

	/* returns the best autorail highlight type from map coordinates */
	static int GetAutorailHT(int x, int y)
	{
		return HT_RAIL | AutoRail._AutorailPiece[x & 0xF][y & 0xF];
	}

	// called regular to update tile highlighting in all cases
	static void UpdateTileSelection()
	{
		int x1;
		int y1;

		_thd.new_drawstyle = 0;

		if (_thd.place_mode == VHM_SPECIAL) {
			x1 = _thd.selend.x;
			y1 = _thd.selend.y;
			if (x1 != -1) {
				int x2 = _thd.selstart.x;
				int y2 = _thd.selstart.y;
				x1 &= ~0xF;
				y1 &= ~0xF;

				if (x1 >= x2) /* intswap(x1,x2);*/ { int t = x1; x1 = x2; x2 = t; }
				if (y1 >= y2) /* intswap(y1,y2);*/ { int t = y1; y1 = y2; y2 = t; }

				_thd.new_pos.x = x1;
				_thd.new_pos.y = y1;
				_thd.new_size.x = x2 - x1 + 16;
				_thd.new_size.y = y2 - y1 + 16;
				_thd.new_drawstyle = _thd.next_drawstyle;
			}
		} else if (_thd.place_mode != VHM_NONE) 
		{
			Point pt = GetTileBelowCursor();
			x1 = pt.x;
			y1 = pt.y;
			if (x1 != -1) {
				switch (_thd.place_mode) {
				case VHM_RECT:
					_thd.new_drawstyle = HT_RECT;
					break;
				case VHM_POINT:
					_thd.new_drawstyle = HT_POINT;
					x1 += 8;
					y1 += 8;
					break;
				case VHM_RAIL:
					_thd.new_drawstyle = GetAutorailHT(pt.x, pt.y); // draw one highlighted tile
				}
				_thd.new_pos.x = x1 & ~0xF;
				_thd.new_pos.y = y1 & ~0xF;
			}
		}

		//Global.debug("UpdateTileSelection before redraw old %d.%d new %d.%d mode %d", _thd.pos.x, _thd.pos.y, _thd.new_pos.x, _thd.new_pos.y,  _thd.place_mode );
		
		// redraw selection
		if (_thd.drawstyle != _thd.new_drawstyle ||
				_thd.pos.x != _thd.new_pos.x || _thd.pos.y != _thd.new_pos.y ||
				_thd.size.x != _thd.new_size.x || _thd.size.y != _thd.new_size.y) 
		{
			//Global.debug("UpdateTileSelection redraw %d.%d", _thd.pos.x, _thd.pos.y );
			// clear the old selection?
			if (_thd.drawstyle != 0) SetSelectionTilesDirty();

			_thd.drawstyle = _thd.new_drawstyle;
			_thd.pos = new Point( _thd.new_pos );
			_thd.size = new Point(  _thd.new_size );
			_thd.outersize = new Point( _thd.new_outersize );
			_thd.dirty = 0xff;

			// draw the new selection?
			if (_thd.new_drawstyle != 0) SetSelectionTilesDirty();
		}
	}

	// highlighting tiles while only going over them with the mouse
	public static void VpStartPlaceSizing(TileIndex tile, int user)
	{
		_thd.userdata = user;
		_thd.selend.x = tile.TileX() * 16;
		_thd.selstart.x = tile.TileX() * 16;
		_thd.selend.y = tile.TileY() * 16;
		_thd.selstart.y = tile.TileY() * 16;
		if (_thd.place_mode == VHM_RECT) {
			_thd.place_mode = VHM_SPECIAL;
			_thd.next_drawstyle = HT_RECT;
		} else if (_thd.place_mode == VHM_RAIL) { // autorail one piece
			_thd.place_mode = VHM_SPECIAL;
			_thd.next_drawstyle = _thd.drawstyle;
		} else {
			_thd.place_mode = VHM_SPECIAL;
			_thd.next_drawstyle = HT_POINT;
		}
		Window._special_mouse_mode = Window.WSM_SIZING;
	}

	public static void VpSetPlaceSizingLimit(int limit)
	{
		_thd.sizelimit = limit;
	}

	public static void VpSetPresizeRange(TileIndex from, TileIndex to)
	{
		_thd.selend.x = to.TileX() * 16;
		_thd.selend.y = to.TileY() * 16;
		_thd.selstart.x = from.TileX() * 16;
		_thd.selstart.y = from.TileY() * 16;
		_thd.next_drawstyle = HT_RECT;
	}

	static void VpStartPreSizing()
	{
		_thd.selend.x = -1;
		Window._special_mouse_mode = Window.WSM_PRESIZE;
	}

	/* returns information about the 2x1 piece to be build.
	 * The lower bits (0-3) are the track type. */
	static byte Check2x1AutoRail(int mode)
	{
		int fxpy = ViewPort._tile_fract_coords.x + ViewPort._tile_fract_coords.y;
		int sxpy = (_thd.selend.x & 0xF) + (_thd.selend.y & 0xF);
		int fxmy = ViewPort._tile_fract_coords.x - ViewPort._tile_fract_coords.y;
		int sxmy = (_thd.selend.x & 0xF) - (_thd.selend.y & 0xF);

		switch(mode) {
		case 0: // end piece is lower right
			if (fxpy >= 20 && sxpy <= 12) { /*SwapSelection(); DoRailroadTrack(0); */return 3; }
			if (fxmy < -3 && sxmy > 3) {/* DoRailroadTrack(0); */return 5; }
			return 1;

		case 1:
			if (fxmy > 3 && sxmy < -3) { /*SwapSelection(); DoRailroadTrack(0); */return 4; }
			if (fxpy <= 12 && sxpy >= 20) { /*DoRailroadTrack(0); */return 2; }
			return 1;

		case 2:
			if (fxmy > 3 && sxmy < -3) { /*DoRailroadTrack(3);*/ return 4; }
			if (fxpy >= 20 && sxpy <= 12) { /*SwapSelection(); DoRailroadTrack(0); */return 3; }
			return 0;

		case 3:
			if (fxmy < -3 && sxmy > 3) { /*SwapSelection(); DoRailroadTrack(3);*/ return 5; }
			if (fxpy <= 12 && sxpy >= 20) { /*DoRailroadTrack(0); */return 2; }
			return 0;
		}

		return 0; // avoids compiler warnings
	}


	// while dragging
	static void CalcRaildirsDrawstyle(TileHighlightData thd, int x, int y, int method)
	{
		int d;
		int b;//=6;
		int w,h;

		int dx = thd.selstart.x - (thd.selend.x&~0xF);
		int dy = thd.selstart.y - (thd.selend.y&~0xF);
		w = Math.abs(dx) + 16;
		h = Math.abs(dy) + 16;

		if (TileIndex.TileVirtXY(thd.selstart.x, thd.selstart.y).equals(TileIndex.TileVirtXY(x, y)) ) { // check if we're only within one tile
			if (method == VPM_RAILDIRS)
				b = GetAutorailHT(x, y);
			else // rect for autosignals on one tile
				b = HT_RECT;
		} else if (h == 16) { // Is this in X direction?
			if (dx == 16) // 2x1 special handling
				b = (Check2x1AutoRail(3)) | HT_LINE;
			else if (dx == -16)
				b = (Check2x1AutoRail(2)) | HT_LINE;
			else
				b = HT_LINE | HT_DIR_X;
			y = thd.selstart.y;
		} else if (w == 16) { // Or Y direction?
			if (dy == 16) // 2x1 special handling
				b = (Check2x1AutoRail(1)) | HT_LINE;
			else if (dy == -16) // 2x1 other direction
				b = (Check2x1AutoRail(0)) | HT_LINE;
			else
				b = HT_LINE | HT_DIR_Y;
			x = thd.selstart.x;
		} else if (w > h * 2) { // still count as x dir?
			b = HT_LINE | HT_DIR_X;
			y = thd.selstart.y;
		} else if (h > w * 2) { // still count as y dir?
			b = HT_LINE | HT_DIR_Y;
			x = thd.selstart.x;
		} else { // complicated direction
			d = w - h;
			thd.selend.x = thd.selend.x & ~0xF;
			thd.selend.y = thd.selend.y & ~0xF;

			// four cases.
			if (x > thd.selstart.x) {
				if (y > thd.selstart.y) {
					// south
					if (d == 0) {
						b = (x & 0xF) > (y & 0xF) ? HT_LINE | HT_DIR_VL : HT_LINE | HT_DIR_VR;
					} else if (d >= 0) {
						x = thd.selstart.x + h;
						b = HT_LINE | HT_DIR_VL;
						// return px == py || px == py + 16;
					} else {
						y = thd.selstart.y + w;
						b = HT_LINE | HT_DIR_VR;
					} // return px == py || px == py - 16;
				} else {
					// west
					if (d == 0) {
						b = (x & 0xF) + (y & 0xF) >= 0x10 ? HT_LINE | HT_DIR_HL : HT_LINE | HT_DIR_HU;
					} else if (d >= 0) {
						x = thd.selstart.x + h;
						b = HT_LINE | HT_DIR_HL;
					} else {
						y = thd.selstart.y - w;
						b = HT_LINE | HT_DIR_HU;
					}
				}
			} else {
				if (y > thd.selstart.y) {
					// east
					if (d == 0) {
						b = (x & 0xF) + (y & 0xF) >= 0x10 ? HT_LINE | HT_DIR_HL : HT_LINE | HT_DIR_HU;
					} else if (d >= 0) {
						x = thd.selstart.x - h;
						b = HT_LINE | HT_DIR_HU;
						// return px == -py || px == -py - 16;
					} else {
						y = thd.selstart.y + w;
						b = HT_LINE | HT_DIR_HL;
					} // return px == -py || px == -py + 16;
				} else {
					// north
					if (d == 0) {
						b = (x & 0xF) > (y & 0xF) ? HT_LINE | HT_DIR_VL : HT_LINE | HT_DIR_VR;
					} else if (d >= 0) {
						x = thd.selstart.x - h;
						b = HT_LINE | HT_DIR_VR;
						// return px == py || px == py - 16;
					} else {
						y = thd.selstart.y - w;
						b = HT_LINE | HT_DIR_VL;
					} //return px == py || px == py + 16;
				}
			}
		}
		thd.selend.x = x;
		thd.selend.y = y;
		thd.next_drawstyle = b;
	}

	// while dragging
	public static void VpSelectTilesWithMethod(int x, int y, int method)
	{
		int sx;
		int sy;

		if (x == -1) {
			_thd.selend.x = -1;
			return;
		}

		// allow drag in any rail direction
		if (method == VPM_RAILDIRS || method == VPM_SIGNALDIRS) {
			_thd.selend.x = x;
			_thd.selend.y = y;
			CalcRaildirsDrawstyle(_thd, x, y, method);
			return;
		}

		if (_thd.next_drawstyle == HT_POINT) {
			x += 8;
			y += 8;
		}

		sx = _thd.selstart.x;
		sy = _thd.selstart.y;

		switch (method) {
		case VPM_FIX_X:
			x = sx;
			break;

		case VPM_FIX_Y:
			y = sy;
			break;

		case VPM_X_OR_Y:
			if (Math.abs(sy - y) < Math.abs(sx - x)) y = sy; else x = sx;
			break;

		case VPM_X_AND_Y:
			break;

			// limit the selected area to a 10x10 rect.
		case VPM_X_AND_Y_LIMITED: {
			int limit = (_thd.sizelimit - 1) * 16;
			x = sx + BitOps.clamp(x - sx, -limit, limit);
			y = sy + BitOps.clamp(y - sy, -limit, limit);
			break;
		}
		}

		_thd.selend.x = x;
		_thd.selend.y = y;
	}

	// while dragging
	static boolean VpHandlePlaceSizingDrag()
	{
		Window w;
		WindowEvent e = new WindowEvent();

		if (Window._special_mouse_mode != Window.WSM_SIZING) return true;

		e.userdata = _thd.userdata;

		// stop drag mode if the window has been closed
		w = Window.FindWindowById(_thd.window_class,_thd.window_number);
		if (w == null) {
			ResetObjectToPlace();
			return false;
		}

		// while dragging execute the drag procedure of the corresponding window (mostly VpSelectTilesWithMethod() )
		if (Window._left_button_down) {
			e.event = WindowEvents.WE_PLACE_DRAG;
			e.pt = GetTileBelowCursor();
			w.wndproc.accept(w, e);
			return false;
		}

		// mouse button released..
		// keep the selected tool, but reset it to the original mode.
		Window._special_mouse_mode = Window.WSM_NONE;
		if (_thd.next_drawstyle == HT_RECT) {
			_thd.place_mode = VHM_RECT;
		} else if ((e.userdata & 0xF) == VPM_SIGNALDIRS) { // some might call this a hack... -- Dominik
			_thd.place_mode = VHM_RECT;
		} else if(0 != (_thd.next_drawstyle & HT_LINE)) {
			_thd.place_mode = VHM_RAIL;
		} else if(0 !=  (_thd.next_drawstyle & HT_RAIL)) {
			_thd.place_mode = VHM_RAIL;
		} else {
			_thd.place_mode = VHM_POINT;
		}
		SetTileSelectSize(1, 1);

		// and call the mouseup event.
		e.event = WindowEvents.WE_PLACE_MOUSEUP;
		e.pt = _thd.selend;
		e.tile = TileIndex.TileVirtXY(e.pt.x, e.pt.y);
		e.starttile = TileIndex.TileVirtXY(_thd.selstart.x, _thd.selstart.y);
		w.wndproc.accept(w, e);

		return false;
	}

	static void SetObjectToPlaceWnd(CursorID icon, int mode, Window w)
	{
		SetObjectToPlace(icon.id, mode, w.getWindow_class(), w.window_number);
	}

	public static void SetObjectToPlaceWnd(int icon, int mode, Window w)
	{
		SetObjectToPlace(icon, mode, w.getWindow_class(), w.window_number);
	}

	//#include "table/animcursors.h"

	/*
	static void SetObjectToPlace(CursorID icon, int mode, WindowClass window_class, WindowNumber window_num)
	{
		SetObjectToPlace( icon.id,  mode,  window_class.v,  window_num.n);
	}*/
	public static void SetObjectToPlace(int icon, int mode, int window_class, int window_num)
	{
		Window w;

		// undo clicking on button
		if (_thd.place_mode != 0) {
			_thd.place_mode = 0;
			w = Window.FindWindowById(_thd.window_class, _thd.window_number);
			if (w != null) w.CallWindowEventNP(WindowEvents.WE_ABORT_PLACE_OBJ);
		}

		SetTileSelectSize(1, 1);

		_thd.make_square_red = false;

		if (mode == VHM_DRAG) { // mode 4 is for dragdropping trains in the depot window
			mode = 0;
			Window._special_mouse_mode = Window.WSM_DRAGDROP;
		} else {
			Window._special_mouse_mode = Window.WSM_NONE;
		}

		_thd.place_mode = mode;
		_thd.window_class = window_class;
		_thd.window_number = window_num;

		if (mode == VHM_SPECIAL) // special tools, like tunnels or docks start with presizing mode
			VpStartPreSizing();

		if ( icon < 0)
			Hal.SetAnimatedMouseCursor(AnimCursor._animcursors[~icon]);
		else
			Hal.SetMouseCursor( CursorID.get(icon) );
	}

	public static void ResetObjectToPlace()
	{
		SetObjectToPlace(Sprite.SPR_CURSOR_MOUSE, 0, 0, 0);
	}

	public static void clearViewPorts() {
		_viewports.clear();		
	}



}







class ViewportDrawer {
	final DrawPixelInfo dpi = new DrawPixelInfo();

	final ArrayList<TileSpriteToDraw> tile_list = new ArrayList<>();
	final ArrayList<StringSpriteToDraw> string_list = new ArrayList<>();

	ParentSpriteToDraw last_parent;

	final ArrayList<ParentSpriteToDraw> parent_list = new ArrayList<>();

	byte combine_sprites;

	int offs_x, offs_y; // used when drawing ground sprites relative
	boolean ground_child;
} 


//typedef void OnVehicleClickProc(final Vehicle v);
@FunctionalInterface
interface OnVehicleClickProc extends Consumer<Vehicle> {}





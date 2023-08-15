package com.dzavalishin.xui;

import java.util.Iterator;
import java.util.function.Function;

import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.Player;
import com.dzavalishin.game.Sprite;
import com.dzavalishin.game.SpriteCache;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Town;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.struct.Point;
import com.dzavalishin.tables.SmallMapGuiTables;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Pixel;
import com.dzavalishin.util.Sound;

public class SmallMapGui extends SmallMapGuiTables
{

	static final Widget _smallmap_widgets[] = {
			new Widget(  Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5,                Str.STR_018B_CLOSE_WINDOW),
			new Widget(   Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    13,    11,   433,     0,    13, Str.STR_00B0_MAP,            Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_STICKYBOX,     Window.RESIZE_LR,    13,   434,   445,     0,    13, 0x0,                     Str.STR_STICKY_BUTTON),
			new Widget(    Window.WWT_IMGBTN,     Window.RESIZE_RB,    13,     0,   445,    14,   257, 0x0,                     Str.STR_NULL),
			new Widget(         Window.WWT_6,     Window.RESIZE_RB,    13,     2,   443,    16,   255, 0x0,                     Str.STR_NULL),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   380,   401,   258,   279, Sprite.SPR_IMG_SHOW_COUNTOURS,  Str.STR_0191_SHOW_LAND_CONTOURS_ON_MAP),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   402,   423,   258,   279, Sprite.SPR_IMG_SHOW_VEHICLES,   Str.STR_0192_SHOW_VEHICLES_ON_MAP),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   424,   445,   258,   279, Sprite.SPR_IMG_INDUSTRY,        Str.STR_0193_SHOW_INDUSTRIES_ON_MAP),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   380,   401,   280,   301, Sprite.SPR_IMG_SHOW_ROUTES,     Str.STR_0194_SHOW_TRANSPORT_ROUTES_ON),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   402,   423,   280,   301, Sprite.SPR_IMG_PLANTTREES,      Str.STR_0195_SHOW_VEGETATION_ON_MAP),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   424,   445,   280,   301, Sprite.SPR_IMG_COMPANY_GENERAL, Str.STR_0196_SHOW_LAND_OWNERS_ON_MAP),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   358,   379,   258,   279, 0x0,                     Str.STR_NULL),
			new Widget(    Window.WWT_IMGBTN,   Window.RESIZE_LRTB,    13,   358,   379,   280,   301, Sprite.SPR_IMG_TOWN,            Str.STR_0197_TOGGLE_TOWN_NAMES_ON_OFF),
			new Widget(    Window.WWT_IMGBTN,    Window.RESIZE_RTB,    13,     0,   357,   258,   301, 0x0,                     Str.STR_NULL),
			new Widget(     Window.WWT_PANEL,    Window.RESIZE_RTB,    13,     0,   433,   302,   313, 0x0,                     Str.STR_NULL),
			new Widget( Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    13,   434,   445,   302,   313, 0x0,                     Str.STR_RESIZE_BUTTON),

	};

	static int _smallmap_type;
	static boolean _smallmap_show_towns = true;




	static  int ApplyMask(int colour, final AndOr mask)
	{
		return (colour & mask.mand) | mask.mor;
	}



	/**
	 * Draws one column of the small map in a certain mode onto the screen buffer. This
	 * function looks exactly the same for all types
	 *
	 * @param idst Pointer to a part of the screen buffer to write to.
	 * @param xc The X coordinate of the first tile in the column.
	 * @param yc The Y coordinate of the first tile in the column
	 * @param pitch Number of pixels to advance in the screen buffer each time a pixel is written.
	 * @param reps Number of lines to draw
	 * @param mask ?
	 * @param proc Pointer to the colour function
	 * @see GetSmallMapPixels(TileIndex)
	 */
	static void DrawSmallMapStuff(Pixel idst, int xc, int yc, int pitch, int reps, int mask, GetSmallMapPixels proc)
	{
		Pixel dst = new Pixel(idst);
		//Pixel dst_ptr_end = Hal._screen.dst_ptr + Hal._screen.width * Hal._screen.height - Hal._screen.width;
		Pixel dst_ptr_end = new Pixel( Hal._screen.dst_ptr );
		dst_ptr_end.shift(Hal._screen.width * Hal._screen.height - Hal._screen.width); 

		do {
			// check if the tile (xc,yc) is within the map range
			if ( xc > 0 && yc > 0 && xc < Global.MapMaxX() && yc < Global.MapMaxY())
			{
				// check if the dst pointer points to a pixel inside the screen buffer
				//if (dst > Hal._screen.dst_ptr && dst < dst_ptr_end)
				if (dst.inside( Hal._screen.dst_ptr, dst_ptr_end) )
					dst.WRITE_PIXELS_OR(proc.apply(TileIndex.TileXY(xc, yc)) & mask);
			}
			// switch to next tile in the column
			xc++; yc++; dst.madd( pitch );
		} while (--reps != 0);
	}


	static TileTypes GetEffectiveTileType(TileIndex tile)
	{
		TileTypes tt = tile.GetTileType();

		if (tt == TileTypes.MP_TUNNELBRIDGE) {
			int t = tile.getMap().m5;
			if ((t & 0x80) == 0) t >>= 1;
			if ((t & 6) == 0) {
				t = TileTypes.MP_RAILWAY.ordinal();
			} else if ((t & 6) == 2) {
				t = TileTypes.MP_STREET.ordinal();
			} else {
				t = TileTypes.MP_WATER.ordinal();
			}
			return TileTypes.values[t];
		}
		return tt;
	}

	/**
	 * Return the color a tile would be displayed with in the small map in mode "Contour".
	 * @param tile The tile of which we would like to get the color.
	 * @return The color of tile in the small map in mode "Contour"
	 */
	static  int GetSmallMapContoursPixels(TileIndex tile)
	{
		TileTypes t = GetEffectiveTileType(tile);

		return
				ApplyMask(_map_height_bits[tile.TileHeight()], _smallmap_contours_andor[t.ordinal()]);
	}

	/**
	 * Return the color a tile would be displayed with in the small map in mode "Vehicles".
	 *
	 * @param tile The tile of which we would like to get the color.
	 * @return The color of tile in the small map in mode "Vehicles"
	 */
	static  int GetSmallMapVehiclesPixels(TileIndex tile)
	{
		TileTypes t = GetEffectiveTileType(tile);

		return ApplyMask(MKCOLOR(0x54545454), _smallmap_vehicles_andor[t.ordinal()]);
	}


	/**
	 * Return the color a tile would be displayed with in the small map in mode "Industries".
	 *
	 * @param tile The tile of which we would like to get the color.
	 * @return The color of tile in the small map in mode "Industries"
	 */
	static  int GetSmallMapIndustriesPixels(TileIndex tile)
	{
		TileTypes t = GetEffectiveTileType(tile);

		if (t == TileTypes.MP_INDUSTRY) {
			int color = _industry_smallmap_colors[tile.getMap().m5];
			return color + (color << 8) + (color << 16) + (color << 24);
		}

		return ApplyMask(MKCOLOR(0x54545454), _smallmap_vehicles_andor[t.ordinal()]);
	}

	/**
	 * Return the color a tile would be displayed with in the small map in mode "Routes".
	 *
	 * @param tile The tile of which we would like to get the color.
	 * @return The color of tile  in the small map in mode "Routes"
	 */
	static  int GetSmallMapRoutesPixels(TileIndex tile)
	{
		TileTypes t = GetEffectiveTileType(tile);
		int bits;

		if (t == TileTypes.MP_STATION) {
			int m5 = tile.getMap().m5;
			/*
			(bits = MKCOLOR(0x56565656), m5 < 8) ||			//   8 - railroad station (green)
			(bits = MKCOLOR(0xB8B8B8B8), m5 < 0x43) ||	//  67 - airport (red)
			(bits = MKCOLOR(0xC2C2C2C2), m5 < 0x47) ||	//  71 - truck loading bay (orange)
			(bits = MKCOLOR(0xBFBFBFBF), m5 < 0x4B) ||	//  75 - bus station (yellow)
			(bits = MKCOLOR(0x98989898), m5 < 0x53) ||	//  83 - docks (blue)
			(bits = MKCOLOR(0xB8B8B8B8), m5 < 0x73) ||	// 115 - airport (red) (new airports)
			(bits = MKCOLOR(0xFFFFFFFF), true);					// all others
			 */

			if( m5 < 8 ) bits = MKCOLOR(0x56565656);
			else if( m5 < 0x43 ) bits = MKCOLOR(0xB8B8B8B8);
			else if( m5 < 0x47 ) bits = MKCOLOR(0xC2C2C2C2);
			else if( m5 < 0x4B ) bits = MKCOLOR(0xBFBFBFBF);
			else if( m5 < 0x53 ) bits = MKCOLOR(0x98989898);
			else if( m5 < 0x73 ) bits = MKCOLOR(0xB8B8B8B8);
			else bits = MKCOLOR(0xFFFFFFFF);

		} else {
			// ground color
			bits = ApplyMask(MKCOLOR(0x54545454), _smallmap_contours_andor[t.ordinal()]);
		}
		return bits;
	}



	static  int GetSmallMapVegetationPixels(TileIndex tile)
	{
		TileTypes t = GetEffectiveTileType(tile);
		int i;
		int bits;

		switch (t) {
		case MP_CLEAR:
			i = (tile.getMap().m5 & 0x1F) - 4;
			if (i >= 0) i >>= 2;
			bits = _vegetation_clear_bits[i + 4];
			break;

			case MP_INDUSTRY:
				bits = BitOps.IS_BYTE_INSIDE(tile.getMap().m5, 0x10, 0x12) ? MKCOLOR(0xD0D0D0D0) : MKCOLOR(0xB5B5B5B5);
				break;

			case MP_TREES:
				if ((tile.getMap().m2 & 0x30) == 0x20)
					bits = (GameOptions._opt.landscape == Landscape.LT_HILLY) ? MKCOLOR(0x98575798) : MKCOLOR(0xC25757C2);
				else
					bits = MKCOLOR(0x54575754);
				break;

			default:
				bits = ApplyMask(MKCOLOR(0x54545454), _smallmap_vehicles_andor[t.ordinal()]);
				break;
		}

		return bits;
	}


	private static final int[] _owner_colors = new int[256];

	/**
	 * Return the color a tile would be displayed with in the small map in mode "Owner".
	 *
	 * @param tile The tile of which we would like to get the color.
	 * @return The color of tile in the small map in mode "Owner"
	 */
	static  int GetSmallMapOwnerPixels(TileIndex tile)
	{
		int o;

		switch (tile.GetTileType()) {
		case MP_INDUSTRY: o = Owner.OWNER_SPECTATOR;    break;
		case MP_HOUSE:    o = Owner.OWNER_TOWN;         break;
		default:          o = tile.GetTileOwner().id; break;
		}

		return _owner_colors[o];
	}



	/* each tile has 4 x pixels and 1 y pixel */

	static final GetSmallMapPixels[] _smallmap_draw_procs = {
			SmallMapGui::GetSmallMapContoursPixels,
			SmallMapGui::GetSmallMapVehiclesPixels,
			SmallMapGui::GetSmallMapIndustriesPixels,
			SmallMapGui::GetSmallMapRoutesPixels,
			SmallMapGui::GetSmallMapVegetationPixels,
			SmallMapGui::GetSmallMapOwnerPixels,
	};


	static  int dup_byte32(byte b) {
		return b + (b << 8) + (b << 16) + (b << 24);
	}

	static void DrawVertMapIndicator(int x, int y, int x2, int y2)
	{
		Gfx.GfxFillRect(x, y, x2, y + 3, 69);
		Gfx.GfxFillRect(x, y2 - 3, x2, y2, 69);
	}

	static void DrawHorizMapIndicator(int x, int y, int x2, int y2)
	{
		Gfx.GfxFillRect(x, y, x + 3, y2, 69);
		Gfx.GfxFillRect(x2 - 3, y, x2, y2, 69);
	}

	/**
	 * Draws the small map.
	 *
	 * Basically, the small map is draw column of pixels by column of pixels. The pixels
	 * are drawn directly into the screen buffer. The final map is drawn in multiple passes.
	 * The passes are:
	 * <ol><li>The colors of tiles in the different modes.</li>
	 * <li>Town names (optional)</li>
	 *</ol>
	 * @param dpi pointer to pixel to write onto
	 * @param w pointer to Window struct
	 * @param type type of map requested (vegetation, owners, routes, etc)
	 * @param show_towns true if the town names should be displayed, false if not.
	 */
	static void DrawSmallMap(DrawPixelInfo dpi, Window w, int type, boolean show_towns)
	{
		DrawPixelInfo old_dpi;
		Pixel ptr;
		int tile_x;
		int tile_y;
		ViewPort vp;



		old_dpi = Hal._cur_dpi;
		Hal._cur_dpi = dpi;

		/* clear it */
		Gfx.GfxFillRect(dpi.left, dpi.top, dpi.left + dpi.width - 1, dpi.top + dpi.height - 1, 0);

		/* setup owner table */
		if (type == 5) {
			/* fill with some special colors */
			_owner_colors[Owner.OWNER_TOWN] = MKCOLOR(0xB4B4B4B4);
			_owner_colors[Owner.OWNER_NONE] = MKCOLOR(0x54545454);
			_owner_colors[Owner.OWNER_WATER] = MKCOLOR(0xCACACACA);
			_owner_colors[Owner.OWNER_SPECTATOR] = MKCOLOR(0x20202020); /* industry */

			/* now fill with the player colors */
			Player.forEach( (p) ->
			{
				if (p.isActive())
					_owner_colors[p.getIndex().id] =
					dup_byte32(SpriteCache.GetNonSprite(775 + p.getColor())[0xCB]); // XXX - magic pixel
			});
		}

		tile_x = w.as_smallmap_d().scroll_x / 16;
		tile_y = w.as_smallmap_d().scroll_y / 16;

		int dx, dy, x, y;

		dx = dpi.left + w.as_smallmap_d().subscroll;
		tile_x -= dx / 4;
		tile_y += dx / 4;
		dx &= 3;

		dy = dpi.top;
		tile_x += dy / 2;
		tile_y += dy / 2;

		if(0 != (dy & 1)) {
			tile_x++;
			dx += 2;
			if (dx > 3) {
				dx -= 4;
				tile_x--;
				tile_y++;
			}
		}

		ptr = new Pixel( dpi.dst_ptr,  - dx - 4 );
		x = - dx - 4;
		y = 0;

		small_map_draw_helper(x, y, dpi, ptr, tile_x, tile_y, type);

		/* draw vehicles? */
		if (type == 0 || type == 1) 
		{
			Iterator<Vehicle> ii = Vehicle.getIterator();
			while(ii.hasNext())
			{
				Vehicle v = ii.next();

				if (v.getType() != 0 && v.getType() != Vehicle.VEH_Special 
						&& !v.isHidden() && !v.isUnclickable() ) {
					// Remap into flat coordinates.
					Point pt = Point.RemapCoords(
							(v.getX_pos() - w.as_smallmap_d().scroll_x) / 16,
							(v.getY_pos() - w.as_smallmap_d().scroll_y) / 16,
							0);
					int x1 = pt.x;
					int y1 = pt.y;

					// Check if y is out of bounds?
					y1 -= dpi.top;
					if (!BitOps.IS_INT_INSIDE(y1, 0, dpi.height)) continue;

					// Default is to draw both pixels.
					boolean skip = false;

					// Offset X coordinate
					x1 -= w.as_smallmap_d().subscroll + 3 + dpi.left;

					if (x1 < 0) {
						// if x+1 is 0, that means we're on the very left edge,
						//  and should thus only draw a single pixel
						if (++x1 != 0)
							continue;
						skip = true;
					} else if (x1 >= dpi.width - 1) {
						// Check if we're at the very right edge, and if so draw only a single pixel
						if (x1 != dpi.width - 1)
							continue;
						skip = true;
					}

					// Calculate pointer to pixel and the color
					Pixel lptr = new Pixel( dpi.dst_ptr, y * dpi.pitch + x );
					byte lcolor = (byte) ((type == 1) ? _vehicle_type_colors[v.getType()-0x10] : 0xF);

					// And draw either one or two pixels depending on clipping
					lptr.w(0, lcolor);
					if (!skip)
						lptr.w(1, lcolor);
				}
			}
		}

		if (show_towns) {

			Town.forEach( (t) ->
			{
				if (t.getXy() != null) {
					// Remap the town coordinate
					Point pt = Point.RemapCoords(
							(t.getXy().TileX() * 16 - w.as_smallmap_d().scroll_x) / 16,
							(t.getXy().TileY() * 16 - w.as_smallmap_d().scroll_y) / 16,
							0);
					int x1 = pt.x - w.as_smallmap_d().subscroll + 3 - (t.getSign().getWidth_2() >> 1);
					int y1 = pt.y;

					// Check if the town sign is within bounds
					if (x1 + t.getSign().getWidth_2() > dpi.left &&
							x1 < dpi.left + dpi.width &&
							y1 + 6 > dpi.top &&
							y1 < dpi.top + dpi.height) {
						// And draw it.
						Global.SetDParam(0, t.index);
						Gfx.DrawString(x1, y1, Str.STR_2056, 12);
					}
				}
			});
		}

		// Draw map indicators
		{
			Point pt;

			// Find main viewport.
			vp = Window.getMain().getViewport();

			pt = Point.RemapCoords(w.as_smallmap_d().scroll_x, w.as_smallmap_d().scroll_y, 0);

			int x1 = vp.virtual_left - pt.x;
			int y1 = vp.virtual_top - pt.y;
			int x2 = (x1 + vp.virtual_width) / 16;
			int y2 = (y1 + vp.virtual_height) / 16;
			x1 /= 16;
			y1 /= 16;

			x1 -= w.as_smallmap_d().subscroll;
			x2 -= w.as_smallmap_d().subscroll;

			DrawVertMapIndicator(x1, y1, x1, y2);
			DrawVertMapIndicator(x2, y1, x2, y2);

			DrawHorizMapIndicator(x1, y1, x2, y1);
			DrawHorizMapIndicator(x1, y2, x2, y2);
		}
		Hal._cur_dpi = old_dpi;
	}

	private static void small_map_draw_helper(int x, int y, DrawPixelInfo dpi, Pixel ptr, int tile_x, int tile_y, int type)
	{
		for(;;) {
			int mask;
			int reps;
			int t;

			mask = 0xFFFFFFFF;

			//noinspection ConstantConditions
			do { // goto replacement
				/* distance from left edge */
				if (x < 0) {
					if (x < -3) {
						//goto skip_column;
						break; // out of do
					}

					/* mask to use at the left edge */
					mask = _smallmap_mask_left[x + 3];
				}

				/* distance from right edge */
				t = dpi.width - x;
				if (t < 4) {
					if (t <= 0)
					{
						//break; /* exit loop */
						return; // can't break out of 2 loops
					}
					/* mask to use at the right edge */
					mask &= _smallmap_mask_right[t - 1];
				}

				/* number of lines */
				reps = (dpi.height - y + 1) / 2;
				if (reps > 0) {
					//				assert(ptr >= dpi.dst_ptr);
					DrawSmallMapStuff(ptr, tile_x, tile_y, dpi.pitch*2, reps, mask, _smallmap_draw_procs[type]);
				}
			} while(false); // goto / break target
			//skip_column:
			if (y == 0) {
				tile_y++;
				y++;
				ptr.madd( dpi.pitch );
			} else {
				tile_x--;
				y--;
				ptr.madd( -dpi.pitch );
			}
			ptr.madd( 2 );
			x += 2;
		}

	}



	static void SmallMapWindowProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: {
			final int []tbl_mem;
			//int tbl_shift = 0;
			int x,y,y_org;
			DrawPixelInfo new_dpi = new DrawPixelInfo();

			/* draw the window */
			Global.SetDParam(0, Str.STR_00E5_CONTOURS + _smallmap_type);
			w.DrawWindowWidgets();

			/* draw the legend */
			tbl_mem = _legend_table[(_smallmap_type != 2) ? _smallmap_type : (GameOptions._opt.landscape + IND_OFFS)];
			int tbl_shift = 0;

			x = 4;
			y_org = w.height - 43 - 11;
			y = y_org;
			while (true) {
				Gfx.GfxFillRect(x, y+1, x+8, y + 5, 0);
				Gfx.GfxFillRect(x+1, y+2, x+7, y + 4, (byte)tbl_mem[0+tbl_shift]);
				Gfx.DrawString(x+11, y, tbl_mem[1+tbl_shift], 0);

				tbl_shift += 2;
				y += 6;

				if (tbl_mem[0+tbl_shift] == 0xFFFF) {
					break;
				} else if (0 != (tbl_mem[0+tbl_shift] & 0x100) ) {
					x += 123;
					y = y_org;
				}
			}

			if (!DrawPixelInfo.FillDrawPixelInfo(new_dpi, null, 3, 17, w.width - 28 + 22, w.height - 64 - 11))
				return;

			DrawSmallMap(new_dpi, w, _smallmap_type, _smallmap_show_towns);
		} break;

		case WE_CLICK:
			switch (e.widget) {
			case 4: {/* Main wnd */
				Window w2;
				Point pt;

				Window._left_button_clicked = false;

				w2 = Window.getMain();

				pt = Point.RemapCoords(w.as_smallmap_d().scroll_x, w.as_smallmap_d().scroll_y, 0);
				w2.as_vp_d().scrollpos_x = pt.x + ((Hal._cursor.pos.x - w.left + 2) << 4) - (w2.getViewport().virtual_width >> 1);
				w2.as_vp_d().scrollpos_y = pt.y + ((Hal._cursor.pos.y - w.top - 16) << 4) - (w2.getViewport().virtual_height >> 1);
			} break;

			case 5: /* Show land contours */
			case 6: /* Show vehicles */
			case 7: /* Show industries */
			case 8: /* Show transport routes */
			case 9: /* Show vegetation */
			case 10: /* Show land owners */
				w.click_state &= ~(1<<5|1<<6|1<<7|1<<8|1<<9|1<<10);
				w.click_state |= 1 << e.widget;
				_smallmap_type = e.widget - 5;

				w.SetWindowDirty();
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				break;

			case 12: /* toggle town names */
				w.click_state ^= (1 << 12);
				_smallmap_show_towns = BitOps.i2b( (w.click_state >> 12) & 1 );
				w.SetWindowDirty();
				Sound.SndPlayFx(Snd.SND_15_BEEP);
				break;
			}
			break;

		case WE_RCLICK:
			if (e.widget == 4) {
				/*if (Window._scrolling_viewport)
					return;
				Window._scrolling_viewport = true;
				//Hal._cursor.delta.x = 0;				Hal._cursor.delta.y = 0;
				Hal._cursor.scrollRef = new Point( Hal._cursor.pos ); */
				Hal._cursor.startViewportScrolling();

			}
			break;

		case WE_MOUSELOOP:
			/* update the window every now and then */
			if ((++w.vscroll.pos & 0x1F) == 0)
				w.SetWindowDirty();
			break;
		default:
			break;
		}
	}

	static final WindowDesc _smallmap_desc = new WindowDesc(
			-1,-1, 446, 314,
			Window.WC_SMALLMAP,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_smallmap_widgets,
			SmallMapGui::SmallMapWindowProc
			);

	public static void ShowSmallMap()
	{
		Window w;
		ViewPort vp;
		int x,y;

		w = Window.AllocateWindowDescFront(_smallmap_desc, 0);
		if (w != null) {
			w.click_state = ((1<<5) << _smallmap_type) | (BitOps.b2i(_smallmap_show_towns) << 12);
			w.resize.width = 350;
			w.resize.height = 250;

			vp = Window.getMain().getViewport();

			x =  (((vp.virtual_width - (220*32)) / 2) + vp.virtual_left) / 4;
			y = ((((vp.virtual_height- (120*32)) / 2) + vp.virtual_top ) / 2) - 32;
			
			w.as_smallmap_d().scroll_x = (y-x) & ~0xF;
			w.as_smallmap_d().scroll_y = (x+y) & ~0xF;
			w.as_smallmap_d().subscroll = 0;
		}
	}

	/* Extra ViewPort Window Stuff */
	static final Widget _extra_view_port_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,	Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,  Window.RESIZE_RIGHT,    14,    11,   287,     0,    13, Str.STR_EXTRA_VIEW_PORT_TITLE,				Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(  Window.WWT_STICKYBOX,     Window.RESIZE_LR,    14,   288,   299,     0,    13, 0x0,       Str.STR_STICKY_BUTTON),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_RB,    14,     0,   299,    14,   233, 0x0,				Str.STR_NULL),
			new Widget(          Window.WWT_6,     Window.RESIZE_RB,    14,     2,   297,    16,   231, 0x0,				Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,     0,    21,   234,   255, 0x2DF,			Str.STR_017F_ZOOM_THE_VIEW_IN),
			new Widget(      Window.WWT_PANEL,     Window.RESIZE_TB,    14,    22,    43,   234,   255, 0x2E0,			Str.STR_0180_ZOOM_THE_VIEW_OUT),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,    44,   171,   234,   255, Str.STR_EXTRA_VIEW_MOVE_MAIN_TO_VIEW,Str.STR_EXTRA_VIEW_MOVE_MAIN_TO_VIEW_TT),
			new Widget( Window.WWT_PUSHTXTBTN,     Window.RESIZE_TB,    14,   172,   298,   234,   255, Str.STR_EXTRA_VIEW_MOVE_VIEW_TO_MAIN,Str.STR_EXTRA_VIEW_MOVE_VIEW_TO_MAIN_TT),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,   299,   299,   234,   255, 0x0,				Str.STR_NULL),
			new Widget(      Window.WWT_PANEL,    Window.RESIZE_RTB,    14,     0,   287,   256,   267, 0x0,				Str.STR_NULL),
			new Widget(  Window.WWT_RESIZEBOX,   Window.RESIZE_LRTB,    14,   288,   299,   256,   267, 0x0,				Str.STR_RESIZE_BUTTON),

	};

	static void ExtraViewPortWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_CREATE: /* Disable zoom in button */
			w.disabled_state = (1 << 5);
			break;
		case WE_PAINT:
			// set the number in the title bar
			Global.SetDParam(0, (w.window_number+1));

			w.DrawWindowWidgets();
			w.DrawWindowViewport();
			break;

		case WE_CLICK: {
			switch (e.widget) {
			case 5: /* zoom in */
				Gui.DoZoomInOutWindow(Gui.ZOOM_IN, w);
				break;
			case 6: /* zoom out */
				Gui.DoZoomInOutWindow(Gui.ZOOM_OUT, w);
				break;
			case 7: { /* location button (move main view to same spot as this view) 'Paste Location' */
				Window w2 = Window.getMain();
				int x = w.as_vp_d().scrollpos_x; // Where is the main looking at
				int y = w.as_vp_d().scrollpos_y;

				// set this view to same location. Based on the center, adjusting for zoom
				w2.as_vp_d().scrollpos_x =  x - (w2.getViewport().virtual_width -  w.getViewport().virtual_width) / 2;
				w2.as_vp_d().scrollpos_y =  y - (w2.getViewport().virtual_height - w.getViewport().virtual_height) / 2;
			} break;
			case 8: { /* inverse location button (move this view to same spot as main view) 'Copy Location' */
				final Window  w2 = Window.getMain();
				int x = w2.as_vp_d().scrollpos_x;
				int y = w2.as_vp_d().scrollpos_y;

				w.as_vp_d().scrollpos_x =  x + (w2.getViewport().virtual_width -  w.getViewport().virtual_width) / 2;
				w.as_vp_d().scrollpos_y =  y + (w2.getViewport().virtual_height - w.getViewport().virtual_height) / 2;
			} break;
			}
		} break;

		case WE_RESIZE:
			w.resizeViewPort(e);
			//w.viewport.width  += e.diff.x;
			//w.viewport.height += e.diff.y;
			//w.viewport.virtual_width  += e.diff.x;
			//w.viewport.virtual_height += e.diff.y;
			break;
		default:
			break;
		}
	}

	static final WindowDesc _extra_view_port_desc = new WindowDesc(
			-1,-1, 300, 268,
			Window.WC_EXTRA_VIEW_PORT,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS | WindowDesc.WDF_STICKY_BUTTON | WindowDesc.WDF_RESIZABLE,
			_extra_view_port_widgets,
			SmallMapGui::ExtraViewPortWndProc
			);

	static void ShowExtraViewPortWindow()
	{
		Window w, v;
		int i = 0;

		// find next free window number for extra viewport
		while (null != Window.FindWindowById(Window.WC_EXTRA_VIEW_PORT, i) ) {
			i++;
		}

		w = Window.AllocateWindowDescFront(_extra_view_port_desc, i);
		if (null != w) {
			int x, y;
			// the main window with the main view
			v = Window.getMain();
			// New viewport start ats (zero,zero)
			ViewPort.AssignWindowViewport(w, 3, 17, 294, 214, 0 , 0);

			// center on same place as main window (zoom is maximum, no adjustment needed)
			x = w.as_vp_d().scrollpos_x;
			y = w.as_vp_d().scrollpos_y;
			w.as_vp_d().scrollpos_x = x + (v.getViewport().virtual_width  - (294)) / 2;
			w.as_vp_d().scrollpos_y = y + (v.getViewport().virtual_height - (214)) / 2;
		}
	}


}


//typedef int GetSmallMapPixels(TileIndex tile); // typedef callthrough function

@FunctionalInterface
interface GetSmallMapPixels extends Function<TileIndex, Integer> {}

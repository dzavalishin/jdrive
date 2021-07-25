package game;

import game.util.BitOps;

public class Gfx {

	static boolean _dbg_screen_rect;


	static int _pal_first_dirty;
	static int _pal_last_dirty;


	//typedef enum StringColorFlags {
	static final int IS_PALETTE_COLOR = 0x100; // color value is already a real palette color index, not an index of a StringColor
	//} StringColorFlags;


	static Colour [] _cur_palette = new Colour[256];

	// XXX doesn't really belong here, but the only
	// consumers always use it in conjunction with DoDrawString()
	public static final String UPARROW   = String.valueOf(0x80); // "\x80";
	public static final String DOWNARROW = String.valueOf(0xAA); // "\xAA";


	//static void GfxMainBlitter(final Sprite *sprite, int x, int y, int mode);

	private static final int ASCII_LETTERSTART = 32;
	private static int _stringwidth_base = 0;
	//VARDEF byte _stringwidth_table[0x2A0];
	private static byte [] _stringwidth_table = new byte[0x2A0];
	
	static int _stringwidth_out;
	//static /* Pixel */ byte  []_cursor_backup = new /* Pixel */ byte [64 * 64];

	
	
	static byte []_cursor_backup = new byte[64 * 64];
	//static Rect _invalid_rect;
	static final byte []_color_remap_ptr;
	static byte [] _string_colorremap = new byte[3];

	//#define DIRTY_BYTES_PER_LINE (MAX_SCREEN_WIDTH / 64)
	//static byte _dirty_blocks[DIRTY_BYTES_PER_LINE * MAX_SCREEN_HEIGHT / 8];


	/* TODO
	static void memcpy_pitch(void *d, void *s, int w, int h, int spitch, int dpitch)
	{
		byte *dp = (byte*)d;
		byte *sp = (byte*)s;

		assert(h >= 0);
		for (; h != 0; --h) {
			memcpy(dp, sp, w);
			dp += dpitch;
			sp += spitch;
		}
	}*/


	static void GfxScroll(int left, int top, int width, int height, int xo, int yo)
	{
		final /* Pixel */ byte  src;
		/* Pixel */ byte  dst;
		int p;
		int ht;

		if (xo == 0 && yo == 0) return;

		if (Hal._cursor.visible) UndrawMouseCursor();
		UndrawTextMessage();

		p = Hal._screen.pitch;

		if (yo > 0) {
			// Calculate pointers
			dst = Hal._screen.dst_ptr + (top + height - 1) * p + left;
			src = dst - yo * p;

			// Decrease height and increase top
			top += yo;
			height -= yo;
			assert(height > 0);

			// Adjust left & width
			if (xo >= 0) {
				dst += xo;
				left += xo;
				width -= xo;
			} else {
				src -= xo;
				width += xo;
			}

			for (ht = height; ht > 0; --ht) {
				memcpy(dst, src, width);
				src -= p;
				dst -= p;
			}
		} else {
			// Calculate pointers
			dst = Hal._screen.dst_ptr + top * p + left;
			src = dst - yo * p;

			// Decrese height. (yo is <=0).
			height += yo;
			assert(height > 0);

			// Adjust left & width
			if (xo >= 0) {
				dst += xo;
				left += xo;
				width -= xo;
			} else {
				src -= xo;
				width += xo;
			}

			// the y-displacement may be 0 therefore we have to use memmove,
			// because source and destination may overlap
			for (ht = height; ht > 0; --ht) {
				memmove(dst, src, width);
				src += p;
				dst += p;
			}
		}
		// This part of the screen is now dirty.
		Global.hal.make_dirty(left, top, width, height);
	}


	static void GfxFillRect(int left, int top, int right, int bottom, int color)
	{
		final DrawPixelInfo  dpi = Hal._cur_dpi;
		/* Pixel */ byte  *dst;
		final int otop = top;
		final int oleft = left;

		if (dpi.zoom != 0) return;
		if (left > right || top > bottom) return;
		if (right < dpi.left || left >= dpi.left + dpi.width) return;
		if (bottom < dpi.top || top >= dpi.top + dpi.height) return;

		if ( (left -= dpi.left) < 0) left = 0;
		right = right - dpi.left + 1;
		if (right > dpi.width) right = dpi.width;
		right -= left;
		assert(right > 0);

		if ( (top -= dpi.top) < 0) top = 0;
		bottom = bottom - dpi.top + 1;
		if (bottom > dpi.height) bottom = dpi.height;
		bottom -= top;
		assert(bottom > 0);

		dst = dpi.dst_ptr + top * dpi.pitch + left;

		if (!(color & PALETTE_MODIFIER_GREYOUT)) {
			if (!(color & Sprite.USE_COLORTABLE)) {
				do {
					memset(dst, color, right);
					dst += dpi.pitch;
				} while (--bottom);
			} else {
				/* use colortable mode */
				final byte* ctab = GetNonSprite(color & COLORTABLE_MASK) + 1;

				do {
					int i;
					for (i = 0; i != right; i++) dst[i] = ctab[dst[i]];
					dst += dpi.pitch;
				} while (--bottom);
			}
		} else {
			byte bo = (oleft - left + dpi.left + otop - top + dpi.top) & 1;
			do {
				int i;
				for (i = (bo ^= 1); i < right; i += 2) dst[i] = (byte)color;
				dst += dpi.pitch;
			} while (--bottom > 0);
		}
	}

	static static void GfxSetPixel(int x, int y, int color)
	{
		final DrawPixelInfo  dpi = Hal._cur_dpi;
		if ((x-=dpi.left) < 0 || x>=dpi.width || (y-=dpi.top)<0 || y>=dpi.height)
			return;
		dpi.dst_ptr[y * dpi.pitch + x] = color;
	}

	static void GfxDrawLine(int x, int y, int x2, int y2, int color)
	{
		int dy;
		int dx;
		int stepx;
		int stepy;
		int frac;

		// Check clipping first
		{
			DrawPixelInfo dpi = Hal._cur_dpi;
			int t;

			if (x < dpi.left && x2 < dpi.left) return;

			if (y < dpi.top && y2 < dpi.top) return;

			t = dpi.left + dpi.width;
			if (x > t && x2 > t) return;

			t = dpi.top + dpi.height;
			if (y > t && y2 > t) return;
		}

		dy = (y2 - y) * 2;
		if (dy < 0) {
			dy = -dy;
			stepy = -1;
		} else {
			stepy = 1;
		}

		dx = (x2 - x) * 2;
		if (dx < 0) {
			dx = -dx;
			stepx = -1;
		} else {
			stepx = 1;
		}

		GfxSetPixel(x, y, color);
		if (dx > dy) {
			frac = dy - (dx >> 1);
			while (x != x2) {
				if (frac >= 0) {
					y += stepy;
					frac -= dx;
				}
				x += stepx;
				frac += dy;
				GfxSetPixel(x, y, color);
			}
		} else {
			frac = dx - (dy >> 1);
			while (y != y2) {
				if (frac >= 0) {
					x += stepx;
					frac -= dy;
				}
				y += stepy;
				frac += dx;
				GfxSetPixel(x, y, color);
			}
		}
	}

	// ASSIGNMENT OF ASCII LETTERS < 32
	// 0 - end of string
	// 1 - SETX <BYTE>
	// 2 - SETXY <BYTE> <BYTE>
	// 3-7 -
	// 8 - TINYFONT
	// 9 - BIGFONT
	// 10 - newline
	// 11-14 -
	// 15-31 - 17 colors


	//enum {
	public static final int ASCII_SETX = 1;
	public static final int ASCII_SETXY = 2;

	public static final int ASCII_TINYFONT = 8;
	public static final int ASCII_BIGFONT = 9;
	public static final int ASCII_NL = 10;

	public static final int ASCII_COLORSTART = 15;
	//};

	/** Truncate a given string to a maximum width if neccessary.
	 * If the string is truncated, add three dots ('...') to show this.
	 * @param str string that is checked and possibly truncated
	 * @param maxw maximum width in pixels of the string
	 * @param retwidth new width of (truncated) string
	 * @return truncated string
	 * */
	private static String TruncateString(String sstr, int maxw, int[] retwidth)
	{
		StringBuilder sb = new StringBuilder();
		int w = 0;
		int base = _stringwidth_base;
		int ddd, ddd_w;

		int c;
		int ddd_pos;

		base = _stringwidth_base;
		ddd_w = ddd = GetCharacterWidth(base + '.') * 3;

		char[] ca = sstr.toCharArray();
		int cap = 0;
		
		for (ddd_pos = cap; (c = ca[cap++]) != '\0' && cap < ca.length; ) 
		{
			if (c >= ASCII_LETTERSTART) {
				w += GetCharacterWidth(base + c);

				if (w >= maxw) {
					// string got too big... insert dotdotdot
					//ddd_pos[0] = ddd_pos[1] = ddd_pos[2] = '.';
					//ddd_pos[3] = 0;
					//return ddd_w;

					if(retwidth != null) retwidth[0] = w; 
					String s = sb.toString().substring(0, ddd_pos-1);
					return s+"...";
					
				}
			} else {
				if (c == ASCII_SETX) cap++;
				else if (c == ASCII_SETXY) cap += 2;
				else if (c == ASCII_TINYFONT) {
					base = 224;
					ddd = GetCharacterWidth(base + '.') * 3;
				} else if (c == ASCII_BIGFONT) {
					base = 448;
					ddd = GetCharacterWidth(base + '.') * 3;
				}
			}

			// Remember the last position where three dots fit.
			if (w + ddd < maxw) {
				ddd_w = w + ddd;
				ddd_pos = cap;
			}
		}

		if(retwidth != null) retwidth[0] = w; 
		return sb.toString();
	}

	/*private static int TruncateStringID(StringID src, String dest, int maxw)
	{
		GetString(dest, src);
		return TruncateString(dest, maxw);
	}*/
	private static String TruncateStringID(StringID src, int maxw, int [] retmax)
	{
		return TruncateString(Global.GetString(src), maxw, retmax);
	}

	/* returns right coordinate */
	static int DrawString(int x, int y, int str, int color)
	{
		String buffer = String.GetString( str);
		return DoDrawString(buffer, x, y, color);
	}
	/* returns right coordinate */
	static int DrawString(int x, int y, StringID str, int color)
	{
		DrawString(x, y, str.id, color);
	}

	static int DrawStringTruncated(int x, int y, StringID str, int color, int maxw)
	{
		//char buffer[512];
		//TruncateStringID(str, buffer, maxw);
		String buffer = TruncateStringID(str, maxw);
		return DoDrawString(buffer, x, y, color);
	}


	static void DrawStringRightAligned(int x, int y, StringID str, int color)
	{
		//char buffer[512];

		String buffer = Global.GetString(str);
		DoDrawString(buffer, x - GetStringWidth(buffer), y, color);
	}
	static void DrawStringRightAligned(int x, int y, int str, int color)
	{
		//char buffer[512];

		String buffer = Global.GetString(str);
		DoDrawString(buffer, x - GetStringWidth(buffer), y, color);
	}

	static void DrawStringRightAlignedTruncated(int x, int y, StringID str, int color, int maxw)
	{
		//char buffer[512];

		//TruncateStringID(str, buffer, maxw);
		String buffer = TruncateStringID(str, maxw);
		DoDrawString(buffer, x - GetStringWidth(buffer), y, color);
	}

	static int DrawStringCentered(int x, int y, StringID str, int color)
	{
		return DrawStringCentered(x, y, str.id, color);
	}

	static int DrawStringCentered(int x, int y, int str, int color)
	{
		//char buffer[512];
		int w;

		String s = Global.GetString(str);

		w = GetStringWidth(s);
		DoDrawString(s, x - w / 2, y, color);

		return w;
	}

	static int DrawStringCenteredTruncated(int xl, int xr, int y, StringID str, int color)
	{
		//char buffer[512];
		int w[] = {0};
		String s = TruncateStringID(str, xr - xl, w);
		return DoDrawString( s, (xl + xr - w[0]) / 2, y, color);
	}

	static int DoDrawStringCentered(int x, int y, final String str, int color)
	{
		int w = GetStringWidth(str);
		DoDrawString(str, x - w / 2, y, color);
		return w;
	}

	static void DrawStringCenterUnderline(int x, int y, StringID str, int color)
	{
		int w = DrawStringCentered(x, y, str, color);
		GfxFillRect(x - (w >> 1), y + 10, x - (w >> 1) + w, y + 10, _string_colorremap[1]);
	}

	static void DrawStringCenterUnderlineTruncated(int xl, int xr, int y, StringID str, int color)
	{
		int w = DrawStringCenteredTruncated(xl, xr, y, str, color);
		GfxFillRect((xl + xr - w) / 2, y + 10, (xl + xr + w) / 2, y + 10, _string_colorremap[1]);
	}

	private static int FormatStringLinebreaks(String str, int maxw)
	{
		int num = 0;
		int base = _stringwidth_base;
		int w;
		String last_space;
		byte c;

		for(;;) {
			w = 0;
			last_space = null;

			for(;;) {
				c = *str++;
				if (c == ASCII_LETTERSTART) last_space = str;

				if (c >= ASCII_LETTERSTART) {
					w += GetCharacterWidth(base + (byte)c);
					if (w > maxw) {
						str = last_space;
						if (str == null)
							return num + (base << 16);
						break;
					}
				} else {
					if (c == 0) return num + (base << 16);
					if (c == ASCII_NL) break;

					if (c == ASCII_SETX) str++;
					else if (c == ASCII_SETXY) str += 2;
					else if (c == ASCII_TINYFONT) base = 224;
					else if (c == ASCII_BIGFONT) base = 448;
				}
			}

			num++;
			str[-1] = '\0';
		}
	}

	//static void DrawStringMultiCenter(int x, int y, StringID str, int maxw)
	static void DrawStringMultiCenter(int x, int y, int str, int maxw)
	{
		//char buffer[512];
		int tmp;
		int num, w, mt;
		final String src;
		byte c;

		String buffer = Global.GetString(str);

		tmp = FormatStringLinebreaks(buffer, maxw);
		num = BitOps.GB(tmp, 0, 16);

		switch (BitOps.GB(tmp, 16, 16)) {
		case   0: mt = 10; break;
		case 244: mt =  6; break;
		default:  mt = 18; break;
		}

		y -= (mt >> 1) * num;

		src = buffer;

		for(;;) {
			w = GetStringWidth(src);
			DoDrawString(src, x - (w>>1), y, 0xFE);
			_stringwidth_base = _stringwidth_out;

			for(;;) {
				c = *src++;
				if (c == 0) {
					y += mt;
					if (--num < 0) {
						_stringwidth_base = 0;
						return;
					}
					break;
				} else if (c == ASCII_SETX) {
					src++;
				} else if (c == ASCII_SETXY) {
					src+=2;
				}
			}
		}
	}

	static void DrawStringMultiLine(int x, int y, StringID str, int maxw)
	{
		//char buffer[512];
		int tmp;
		int num, mt;
		final String src;
		byte c;

		String buffer = Global.GetString(str);

		tmp = FormatStringLinebreaks(buffer, maxw);
		num = BitOps.GB(tmp, 0, 16);

		switch (BitOps.GB(tmp, 16, 16)) {
		case   0: mt = 10; break;
		case 244: mt =  6; break;
		default:  mt = 18; break;
		}

		src = buffer;

		for(;;) {
			DoDrawString(src, x, y, 0xFE);
			_stringwidth_base = _stringwidth_out;

			for(;;) {
				c = *src++;
				if (c == 0) {
					y += mt;
					if (--num < 0) {
						_stringwidth_base = 0;
						return;
					}
					break;
				} else if (c == ASCII_SETX) {
					src++;
				} else if (c == ASCII_SETXY) {
					src+=2;
				}
			}
		}
	}

	static int GetStringWidth(final String str)
	{
		int w = 0;
		char c;
		int base = _stringwidth_base;
		int strp = 0;
		char [] ca = str.toCharArray();
		for (c = ca[strp]; /*c != '\0' &&*/ strp < ca.length; c = ca[strp++]) {
			if (c >= ASCII_LETTERSTART) {
				w += GetCharacterWidth(base + c);
			} else {
				if (c == ASCII_SETX) strp++;
				else if (c == ASCII_SETXY) strp += 2;
				else if (c == ASCII_TINYFONT) base = 224;
				else if (c == ASCII_BIGFONT) base = 448;
			}
		}
		return w;
	}

	static void DrawFrameRect(int left, int top, int right, int bottom, int ctab, int flags)
	{
		byte color_2 = _color_list[ctab].window_color_1a;
		byte color_interior = _color_list[ctab].window_color_bga;
		byte color_3 = _color_list[ctab].window_color_bgb;
		byte color = _color_list[ctab].window_color_2;

		if (0 ==(flags & 0x8)) {
			if (0==(flags & 0x20)) {
				GfxFillRect(left, top, left, bottom - 1, color);
				GfxFillRect(left + 1, top, right - 1, top, color);
				GfxFillRect(right, top, right, bottom - 1, color_2);
				GfxFillRect(left, bottom, right, bottom, color_2);
				if (0==(flags & 0x10)) {
					GfxFillRect(left + 1, top + 1, right - 1, bottom - 1, color_interior);
				}
			} else {
				GfxFillRect(left, top, left, bottom, color_2);
				GfxFillRect(left + 1, top, right, top, color_2);
				GfxFillRect(right, top + 1, right, bottom - 1, color);
				GfxFillRect(left + 1, bottom, right, bottom, color);
				if (0==(flags & 0x10)) {
					GfxFillRect(left + 1, top + 1, right - 1, bottom - 1,
							0 != (flags & 0x40) ? color_interior : color_3);
				}
			}
		} else if(0 != (flags & 0x1)) {
			// transparency
			GfxFillRect(left, top, right, bottom, 0x322 | Sprite.USE_COLORTABLE);
		} else {
			GfxFillRect(left, top, right, bottom, color_interior);
		}
	}

	static int DoDrawString(final String string, int x, int y, int real_color)
	{
		DrawPixelInfo dpi = Hal._cur_dpi;
		int base = _stringwidth_base;
		byte c;
		byte color;
		int xo = x, yo = y;

		color = real_color & 0xFF;

		if (color != 0xFE) {
			if (x >= dpi.left + dpi.width ||
					x + Hal._screen.width*2 <= dpi.left ||
					y >= dpi.top + dpi.height ||
					y + Hal._screen.height <= dpi.top)
				return x;

			if (color != 0xFF) {
				switch_color:;
				if (real_color & IS_PALETTE_COLOR) {
					_string_colorremap[1] = color;
					_string_colorremap[2] = 215;
				} else {
					_string_colorremap[1] = _string_colormap[color].text;
					_string_colorremap[2] = _string_colormap[color].shadow;
				}
				_color_remap_ptr = _string_colorremap;
			}
		}

		check_bounds:
			if (y + 19 <= dpi.top || dpi.top + dpi.height <= y) {
				skip_char:;
				for(;;) {
					c = *string++;
					if (c < ASCII_LETTERSTART) goto skip_cont;
				}
			}

		for(;;) {
			c = *string++;
			skip_cont:;
			if (c == 0) {
				_stringwidth_out = base;
				return x;
			}
			if (c >= ASCII_LETTERSTART) {
				if (x >= dpi.left + dpi.width) goto skip_char;
				if (x + 26 >= dpi.left) {
					GfxMainBlitter(GetSprite(base + 2 + c - ASCII_LETTERSTART), x, y, 1);
				}
				x += GetCharacterWidth(base + c);
			} else if (c == ASCII_NL) { // newline = {}
				x = xo;
				y += 10;
				if (base != 0) {
					y -= 4;
					if (base != 0xE0)
						y += 12;
				}
				goto check_bounds;
			} else if (c >= ASCII_COLORSTART) { // change color?
				color = (byte)(c - ASCII_COLORSTART);
				goto switch_color;
			} else if (c == ASCII_SETX) { // {SETX}
				x = xo + (byte)*string++;
			} else if (c == ASCII_SETXY) {// {SETXY}
				x = xo + (byte)*string++;
				y = yo + (byte)*string++;
			} else if (c == ASCII_TINYFONT) { // {TINYFONT}
				base = 0xE0;
			} else if (c == ASCII_BIGFONT) { // {BIGFONT}
				base = 0x1C0;
			} else {
				Global.error("Unknown string command character %d\n", c);
			}
		}
	}

	static int DoDrawStringTruncated(final String str, int x, int y, int color, int maxw)
	{
		//char buffer[512];
		//ttd_strlcpy(buffer, str, sizeof(buffer));
		String buffer = TruncateString(str, maxw, null);
		return DoDrawString(buffer, x, y, color);
	}

	static void DrawSprite(int img, int x, int y)
	{
		if( 0 != (img & Sprite.PALETTE_MODIFIER_COLOR) ) {
			_color_remap_ptr = GetNonSprite(BitOps.GB(img, Sprite.PALETTE_SPRITE_START, Sprite.PALETTE_SPRITE_WIDTH)) + 1;
			GfxMainBlitter(GetSprite(img & Sprite.SPRITE_MASK), x, y, 1);
		} else if(0 != (img & Sprite.PALETTE_MODIFIER_TRANSPARENT)) {
			_color_remap_ptr = GetNonSprite(BitOps.GB(img, Sprite.PALETTE_SPRITE_START, Sprite.PALETTE_SPRITE_WIDTH)) + 1;
			GfxMainBlitter(Sprite.GetSprite(img & Sprite.SPRITE_MASK), x, y, 2);
		} else {
			GfxMainBlitter(Sprite.GetSprite(img & Sprite.SPRITE_MASK), x, y, 0);
		}
	}

	class BlitterParams {
		int start_x, start_y;
		final byte* sprite;
		final byte* sprite_org;
		/* Pixel */ byte  *dst;
		int mode;
		int width, height;
		int width_org;
		int height_org;
		int pitch;
		byte info;
	} 

	private static void GfxBlitTileZoomIn(BlitterParams bp)
	{
		final byte* src_o = bp.sprite;
		final byte* src;
		int num, skip;
		byte done;
		/* Pixel */ byte  *dst;
		final byte* ctab;

		if (bp.mode & 1) {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);

			do {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src = src_o + 2;
					src_o += num + 2;

					dst = bp.dst;

					if ( (skip -= bp.start_x) > 0) {
						dst += skip;
					} else {
						src -= skip;
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					ctab = _color_remap_ptr;

					for (; num >= 4; num -=4) {
						dst[3] = ctab[src[3]];
						dst[2] = ctab[src[2]];
						dst[1] = ctab[src[1]];
						dst[0] = ctab[src[0]];
						dst += 4;
						src += 4;
					}
					for (; num != 0; num--) *dst++ = ctab[*src++];
				} while (!(done & 0x80));

				bp.dst += bp.pitch;
			} while (--bp.height != 0);
		} else if (bp.mode & 2) {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			do {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src_o += num + 2;

					dst = bp.dst;

					if ( (skip -= bp.start_x) > 0) {
						dst += skip;
					} else {
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					ctab = _color_remap_ptr;
					for (; num != 0; num--) {
						*dst = ctab[*dst];
						dst++;
					}
				} while (!(done & 0x80));

				bp.dst += bp.pitch;
			} while (--bp.height != 0);
		} else {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			do {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src = src_o + 2;
					src_o += num + 2;

					dst = bp.dst;

					if ( (skip -= bp.start_x) > 0) {
						dst += skip;
					} else {
						src -= skip;
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}
					#if defined(_WIN32)
					if (num & 1) *dst++ = *src++;
					if (num & 2) { *(int*)dst = *(int*)src; dst += 2; src += 2; }
					if (num >>= 2) {
						do {
							*(int*)dst = *(int*)src;
							dst += 4;
							src += 4;
						} while (--num != 0);
					}
					#else
						memcpy(dst, src, num);
					#endif
				} while (!(done & 0x80));

				bp.dst += bp.pitch;
			} while (--bp.height != 0);
		}
	}

	private static void GfxBlitZoomInUncomp(BlitterParams bp)
	{
		final byte *src = bp.sprite;
		/* Pixel */ byte  *dst = bp.dst;
		int height = bp.height;
		int width = bp.width;
		int i;

		assert(height > 0);
		assert(width > 0);

		if (bp.mode & 1) {
			if (bp.info & 1) {
				final byte *ctab = _color_remap_ptr;

				do {
					for (i = 0; i != width; i++) {
						byte b = ctab[src[i]];

						if (b != 0) dst[i] = b;
					}
					src += bp.width_org;
					dst += bp.pitch;
				} while (--height != 0);
			}
		} else if (bp.mode & 2) {
			if (bp.info & 1) {
				final byte *ctab = _color_remap_ptr;

				do {
					for (i = 0; i != width; i++)
						if (src[i] != 0) dst[i] = ctab[dst[i]];
					src += bp.width_org;
					dst += bp.pitch;
				} while (--height != 0);
			}
		} else {
			if (!(bp.info & 1)) {
				do {
					memcpy(dst, src, width);
					src += bp.width_org;
					dst += bp.pitch;
				} while (--height != 0);
			} else {
				do {
					int n = width;

					for (; n >= 4; n -= 4) {
						if (src[0] != 0) dst[0] = src[0];
						if (src[1] != 0) dst[1] = src[1];
						if (src[2] != 0) dst[2] = src[2];
						if (src[3] != 0) dst[3] = src[3];

						dst += 4;
						src += 4;
					}

					for (; n != 0; n--) {
						if (src[0] != 0) dst[0] = src[0];
						src++;
						dst++;
					}

					src += bp.width_org - width;
					dst += bp.pitch - width;
				} while (--height != 0);
			}
		}
	}

	private static void GfxBlitTileZoomMedium(BlitterParams bp)
	{
		final byte* src_o = bp.sprite;
		final byte* src;
		int num, skip;
		byte done;
		/* Pixel */ byte  *dst;
		final byte* ctab;

		if (bp.mode & 1) {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			do {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src = src_o + 2;
					src_o += num + 2;

					dst = bp.dst;

					if (skip & 1) {
						skip++;
						src++;
						if (--num == 0) continue;
					}

					if ( (skip -= bp.start_x) > 0) {
						dst += skip >> 1;
					} else {
						src -= skip;
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					ctab = _color_remap_ptr;
					num = (num + 1) >> 1;
						for (; num != 0; num--) {
							*dst = ctab[*src];
							dst++;
							src += 2;
						}
				} while (!(done & 0x80));
				bp.dst += bp.pitch;
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
			} while (--bp.height != 0);
		} else if (bp.mode & 2) {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			do {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src_o += num + 2;

					dst = bp.dst;

					if (skip & 1) {
						skip++;
						if (--num == 0) continue;
					}

					if ( (skip -= bp.start_x) > 0) {
						dst += skip >> 1;
					} else {
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					ctab = _color_remap_ptr;
					num = (num + 1) >> 1;
						for (; num != 0; num--) {
							*dst = ctab[*dst];
							dst++;
						}
				} while (!(done & 0x80));
				bp.dst += bp.pitch;
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
			} while (--bp.height != 0);
		} else {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			do {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src = src_o + 2;
					src_o += num + 2;

					dst = bp.dst;

					if (skip & 1) {
						skip++;
						src++;
						if (--num == 0) continue;
					}

					if ( (skip -= bp.start_x) > 0) {
						dst += skip >> 1;
					} else {
						src -= skip;
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					num = (num + 1) >> 1;

						for (; num != 0; num--) {
							*dst = *src;
							dst++;
							src += 2;
						}

				} while (!(done & 0x80));

				bp.dst += bp.pitch;
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
			} while (--bp.height != 0);
		}
	}

	private static void GfxBlitZoomMediumUncomp(BlitterParams bp)
	{
		final byte *src = bp.sprite;
		/* Pixel */ byte  *dst = bp.dst;
		int height = bp.height;
		int width = bp.width;
		int i;

		assert(height > 0);
		assert(width > 0);

		if (bp.mode & 1) {
			if (bp.info & 1) {
				final byte *ctab = _color_remap_ptr;

				for (height >>= 1; height != 0; height--) {
					for (i = 0; i != width >> 1; i++) {
						byte b = ctab[src[i * 2]];

						if (b != 0) dst[i] = b;
					}
					src += bp.width_org * 2;
					dst += bp.pitch;
				}
			}
		} else if (bp.mode & 2) {
			if (bp.info & 1) {
				final byte *ctab = _color_remap_ptr;

				for (height >>= 1; height != 0; height--) {
					for (i = 0; i != width >> 1; i++)
						if (src[i * 2] != 0) dst[i] = ctab[dst[i]];
					src += bp.width_org * 2;
					dst += bp.pitch;
				}
			}
		} else {
			if (bp.info & 1) {
				for (height >>= 1; height != 0; height--) {
					for (i = 0; i != width >> 1; i++)
						if (src[i * 2] != 0) dst[i] = src[i * 2];
					src += bp.width_org * 2;
					dst += bp.pitch;
				}
			}
		}
	}

	private static void GfxBlitTileZoomOut(BlitterParams bp)
	{
		final byte* src_o = bp.sprite;
		final byte* src;
		int num, skip;
		byte done;
		/* Pixel */ byte  *dst;
		final byte* ctab;

		if (bp.mode & 1) {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			for(;;) {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src = src_o + 2;
					src_o += num + 2;

					dst = bp.dst;

					if (skip & 1) {
						skip++;
						src++;
						if (--num == 0) continue;
					}

					if (skip & 2) {
						skip += 2;
						src += 2;
						num -= 2;
						if (num <= 0) continue;
					}

					if ( (skip -= bp.start_x) > 0) {
						dst += skip >> 2;
					} else {
						src -= skip;
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					ctab = _color_remap_ptr;
					num = (num + 3) >> 2;
						for (; num != 0; num--) {
							*dst = ctab[*src];
							dst++;
							src += 4;
						}
				} while (!(done & 0x80));
				bp.dst += bp.pitch;
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;
			}
		} else if (bp.mode & 2) {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			for(;;) {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src_o += num + 2;

					dst = bp.dst;

					if (skip & 1) {
						skip++;
						if (--num == 0) continue;
					}

					if (skip & 2) {
						skip += 2;
						num -= 2;
						if (num <= 0) continue;
					}

					if ( (skip -= bp.start_x) > 0) {
						dst += skip >> 2;
					} else {
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					ctab = _color_remap_ptr;
					num = (num + 3) >> 2;
						for (; num != 0; num--) {
							*dst = ctab[*dst];
							dst++;
						}

				} while (!(done & 0x80));
				bp.dst += bp.pitch;
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;
			}
		} else {
			src_o += READ_LE_UINT16(src_o + bp.start_y * 2);
			for(;;) {
				do {
					done = src_o[0];
					num = done & 0x7F;
					skip = src_o[1];
					src = src_o + 2;
					src_o += num + 2;

					dst = bp.dst;

					if (skip & 1) {
						skip++;
						src++;
						if (--num == 0) continue;
					}

					if (skip & 2) {
						skip += 2;
						src += 2;
						num -= 2;
						if (num <= 0) continue;
					}

					if ( (skip -= bp.start_x) > 0) {
						dst += skip >> 2;
					} else {
						src -= skip;
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					num = (num + 3) >> 2;

						for (; num != 0; num--) {
							*dst = *src;
							dst++;
							src += 4;
						}
				} while (!(done & 0x80));

				bp.dst += bp.pitch;
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;

				do {
					done = src_o[0];
					src_o += (done & 0x7F) + 2;
				} while (!(done & 0x80));
				if (--bp.height == 0) return;
			}
		}
	}

	private static void GfxBlitZoomOutUncomp(BlitterParams bp)
	{
		final byte* src = bp.sprite;
		/* Pixel */ byte  *dst = bp.dst;
		int height = bp.height;
		int width = bp.width;
		int i;

		assert(height > 0);
		assert(width > 0);

		if (bp.mode & 1) {
			if (bp.info & 1) {
				final byte *ctab = _color_remap_ptr;

				for (height >>= 2; height != 0; height--) {
					for (i = 0; i != width >> 2; i++) {
						byte b = ctab[src[i * 4]];

						if (b != 0) dst[i] = b;
					}
					src += bp.width_org * 4;
					dst += bp.pitch;
				}
			}
		} else if (bp.mode & 2) {
			if (bp.info & 1) {
				final byte *ctab = _color_remap_ptr;

				for (height >>= 2; height != 0; height--) {
					for (i = 0; i != width >> 2; i++)
						if (src[i * 4] != 0) dst[i] = ctab[dst[i]];
					src += bp.width_org * 4;
					dst += bp.pitch;
				}
			}
		} else {
			if (bp.info & 1) {
				for (height >>= 2; height != 0; height--) {
					for (i = 0; i != width >> 2; i++)
						if (src[i * 4] != 0) dst[i] = src[i * 4];
					src += bp.width_org * 4;
					dst += bp.pitch;
				}
			}
		}
	}

	//typedef void (*BlitZoomFunc)(BlitterParams bp);

	private static void GfxMainBlitter(final Sprite  sprite, int x, int y, int mode)
	{
		final DrawPixelInfo  dpi = Hal._cur_dpi;
		int start_x, start_y;
		byte info;
		BlitterParams bp;
		int zoom_mask = ~((1 << dpi.zoom) - 1);

		static final BlitZoomFunc zf_tile[3] =
			{
					GfxBlitTileZoomIn,
					GfxBlitTileZoomMedium,
					GfxBlitTileZoomOut
			};
		static final BlitZoomFunc zf_uncomp[3] =
			{
					GfxBlitZoomInUncomp,
					GfxBlitZoomMediumUncomp,
					GfxBlitZoomOutUncomp
			};

		/* decode sprite header */
		x += sprite.x_offs;
		y += sprite.y_offs;
		bp.width_org = bp.width = sprite.width;
		bp.height_org = bp.height = sprite.height;
		info = sprite.info;
		bp.info = info;
		bp.sprite_org = bp.sprite = sprite.data;
		bp.dst = dpi.dst_ptr;
		bp.mode = mode;
		bp.pitch = dpi.pitch;

		assert(bp.height > 0);
		assert(bp.width > 0);

		if (info & 8) {
			/* tile blit */
			start_y = 0;

			if (dpi.zoom > 0) {
				start_y += bp.height & ~zoom_mask;
				bp.height &= zoom_mask;
				if (bp.height == 0) return;
				y &= zoom_mask;
			}

			if ( (y -= dpi.top) < 0) {
				bp.height += y;
				if (bp.height <= 0) return;
				start_y -= y;
				y = 0;
			} else {
				bp.dst += bp.pitch * (y >> dpi.zoom);
			}
			bp.start_y = start_y;

			if ( (y = y + bp.height - dpi.height) > 0) {
				bp.height -= y;
				if (bp.height <= 0) return;
			}

			start_x = 0;
			x &= zoom_mask;
			if ( (x -= dpi.left) < 0) {
				bp.width += x;
				if (bp.width <= 0) return;
				start_x -= x;
				x = 0;
			}
			bp.start_x = start_x;
			bp.dst += x >> dpi.zoom;

				if ( (x = x + bp.width - dpi.width) > 0) {
					bp.width -= x;
					if (bp.width <= 0) return;
				}

				zf_tile[dpi.zoom](&bp);
		} else {
			bp.sprite += bp.width * (bp.height & ~zoom_mask);
			bp.height &= zoom_mask;
			if (bp.height == 0) return;

			y &= zoom_mask;

			if ( (y -= dpi.top) < 0) {
				bp.height += y;
				if (bp.height <= 0) return;
				bp.sprite -= bp.width * y;
				y = 0;
			} else {
				bp.dst += bp.pitch * (y >> dpi.zoom);
			}

			if (bp.height > dpi.height - y) {
				bp.height = dpi.height - y;
				if (bp.height <= 0) return;
			}

			x &= zoom_mask;

			if ( (x -= dpi.left) < 0) {
				bp.width += x;
				if (bp.width <= 0) return;
				bp.sprite -= x;
				x = 0;
			}
			bp.dst += x >> dpi.zoom;

				if (bp.width > dpi.width - x) {
					bp.width = dpi.width - x;
					if (bp.width <= 0) return;
				}

				zf_uncomp[dpi.zoom](&bp);
		}
	}

	//void DoPaletteAnimations();

	static void GfxInitPalettes()
	{
		//memcpy(_cur_palette, _palettes[_use_dos_palette ? 1 : 0], sizeof(_cur_palette));
		
		System.arraycopy(_palettes[0], 0, _cur_palette, 0, _cur_palette.length );

		_pal_first_dirty = 0;
		_pal_last_dirty = 255;
		DoPaletteAnimations();
	}

	//#define EXTR(p, q) (((int)(_timer_counter * (p)) * (q)) >> 16)
	//#define EXTR2(p, q) (((int)(~_timer_counter * (p)) * (q)) >> 16)

	static void DoPaletteAnimations()
	{
		final Colour s;
		Colour d;
		/* Amount of colors to be rotated.
		 * A few more for the DOS palette, because the water colors are
		 * 245-254 for DOS and 217-226 for Windows.  */
		final ExtraPaletteValues ev = _extra_palette_values;
		int c = _use_dos_palette ? 38 : 28;
		Colour [] old_val = new Colour[38]; // max(38, 28)
		int i;
		int j;

		d = &_cur_palette[217];
		memcpy(old_val, d, c * sizeof(*old_val));

		// Dark blue water
		s = (_opt.landscape == LT_CANDY) ? ev.ac : ev.a;
		j = EXTR(320, 5);
		for (i = 0; i != 5; i++) {
			*d++ = s[j];
			j++;
			if (j == 5) j = 0;
		}

		// Glittery water
		s = (_opt.landscape == LT_CANDY) ? ev.bc : ev.b;
		j = EXTR(128, 15);
		for (i = 0; i != 5; i++) {
			*d++ = s[j];
			j += 3;
			if (j >= 15) j -= 15;
		}

		s = ev.e;
		j = EXTR2(512, 5);
		for (i = 0; i != 5; i++) {
			*d++ = s[j];
			j++;
			if (j == 5) j = 0;
		}

		// Oil refinery fire animation
		s = ev.oil_ref;
		j = EXTR2(512, 7);
		for (i = 0; i != 7; i++) {
			*d++ = s[j];
			j++;
			if (j == 7) j = 0;
		}

		// Radio tower blinking
		{
			byte i = (_timer_counter >> 1) & 0x7F;
			byte v;

			(v = 255, i < 0x3f) ||
			(v = 128, i < 0x4A || i >= 0x75) ||
			(v = 20);
			d.r = v;
			d.g = 0;
			d.b = 0;
			d++;

			i ^= 0x40;
			(v = 255, i < 0x3f) ||
			(v = 128, i < 0x4A || i >= 0x75) ||
			(v = 20);
			d.r = v;
			d.g = 0;
			d.b = 0;
			d++;
		}

		// Handle lighthouse and stadium animation
		s = ev.lighthouse;
		j = EXTR(256, 4);
		for (i = 0; i != 4; i++) {
			*d++ = s[j];
			j++;
			if (j == 4) j = 0;
		}

		// Animate water for old DOS graphics
		if (_use_dos_palette) {
			// Dark blue water DOS
			s = (_opt.landscape == LT_CANDY) ? ev.ac : ev.a;
			j = EXTR(320, 5);
			for (i = 0; i != 5; i++) {
				*d++ = s[j];
				j++;
				if (j == 5) j = 0;
			}

			// Glittery water DOS
			s = (_opt.landscape == LT_CANDY) ? ev.bc : ev.b;
			j = EXTR(128, 15);
			for (i = 0; i != 5; i++) {
				*d++ = s[j];
				j += 3;
				if (j >= 15) j -= 15;
			}
		}

		if (memcmp(old_val, &_cur_palette[217], c * sizeof(*old_val)) != 0) {
			if (_pal_first_dirty > 217) _pal_first_dirty = 217;
			if (_pal_last_dirty < 217 + c) _pal_last_dirty = 217 + c;
		}
	}




	static void LoadStringWidthTable()
	{
		//byte *b = _stringwidth_table;
		int i;
		int bp = 0;

		// 2 equals space.
		for (i = 2; i != 226; i++) {
			//*b++ = i != 97 && (i < 99 || i > 113) && i != 116 && i != 117 && (i < 123 || i > 129) && (i < 151 || i > 153) && i != 155 ? GetSprite(i).width : 0;
			_stringwidth_table[bp++] = i != 97 && (i < 99 || i > 113) && i != 116 && i != 117 && (i < 123 || i > 129) && (i < 151 || i > 153) && i != 155 ? GetSprite(i).width : 0;
		}

		for (i = 226; i != 450; i++) {
			//*b++ = i != 321 && (i < 323 || i > 353) && i != 367 && (i < 375 || i > 377) && i != 379 ? GetSprite(i).width + 1 : 0;
			_stringwidth_table[bp++] = i != 321 && (i < 323 || i > 353) && i != 367 && (i < 375 || i > 377) && i != 379 ? GetSprite(i).width + 1 : 0;
		}

		for (i = 450; i != 674; i++) {
			//*b++ = (i < 545 || i > 577) && i != 585 && i != 587 && i != 588 && (i < 590 || i > 597) && (i < 599 || i > 601) && i != 603 && i != 633 && i != 665 ? GetSprite(i).width + 1 : 0;
			_stringwidth_table[bp++] = (i < 545 || i > 577) && i != 585 && i != 587 && i != 588 && (i < 590 || i > 597) && (i < 599 || i > 601) && i != 603 && i != 633 && i != 665 ? GetSprite(i).width + 1 : 0;
		}
	}

	static int GetCharacterWidth(int key)
	{
		assert(key >= ASCII_LETTERSTART && key - ASCII_LETTERSTART < lengthof(_stringwidth_table));
		return _stringwidth_table[key - ASCII_LETTERSTART];
	}


	static void ScreenSizeChanged()
	{
		// check the dirty rect
		if (_invalid_rect.right >= Hal._screen.width) _invalid_rect.right = Hal._screen.width;
		if (_invalid_rect.bottom >= Hal._screen.height) _invalid_rect.bottom = Hal._screen.height;

		// screen size changed and the old bitmap is invalid now, so we don't want to undraw it
		Hal._cursor.visible = false;
	}

	static void UndrawMouseCursor()
	{
		if (Hal._cursor.visible) {
			Hal._cursor.visible = false;
			memcpy_pitch(
					Hal._screen.dst_ptr + Hal._cursor.draw_pos.x + Hal._cursor.draw_pos.y * Hal._screen.pitch,
					_cursor_backup,
					Hal._cursor.draw_size.x, Hal._cursor.draw_size.y, Hal._cursor.draw_size.x, Hal._screen.pitch);

			Global.hal._video_driver.make_dirty(Hal._cursor.draw_pos.x, Hal._cursor.draw_pos.y, Hal._cursor.draw_size.x, Hal._cursor.draw_size.y);
		}
	}

	static void DrawMouseCursor()
	{
		int x;
		int y;
		int w;
		int h;

		// Don't draw the mouse cursor if it's already drawn
		if (Hal._cursor.visible) {
			if (!Hal._cursor.dirty) return;
			UndrawMouseCursor();
		}

		w = Hal._cursor.size.x;
		x = Hal._cursor.pos.x + Hal._cursor.offs.x;
		if (x < 0) {
			w += x;
			x = 0;
		}
		if (w > Hal._screen.width - x) w = Hal._screen.width - x;
		if (w <= 0) return;
		Hal._cursor.draw_pos.x = x;
		Hal._cursor.draw_size.x = w;

		h = Hal._cursor.size.y;
		y = Hal._cursor.pos.y + Hal._cursor.offs.y;
		if (y < 0) {
			h += y;
			y = 0;
		}
		if (h > Hal._screen.height - y) h = Hal._screen.height - y;
		if (h <= 0) return;
		Hal._cursor.draw_pos.y = y;
		Hal._cursor.draw_size.y = h;

		assert(w * h < (int)sizeof(_cursor_backup));

		// Make backup of stuff below cursor
		memcpy_pitch(
				_cursor_backup,
				Hal._screen.dst_ptr + Hal._cursor.draw_pos.x + Hal._cursor.draw_pos.y * Hal._screen.pitch,
				Hal._cursor.draw_size.x, Hal._cursor.draw_size.y, Hal._screen.pitch, Hal._cursor.draw_size.x);

		// Draw cursor on screen
		Hal._cur_dpi = Hal._screen;
		DrawSprite(Hal._cursor.sprite, Hal._cursor.pos.x, Hal._cursor.pos.y);

		Global.hal._video_driver.make_dirty(Hal._cursor.draw_pos.x, Hal._cursor.draw_pos.y, Hal._cursor.draw_size.x, Hal._cursor.draw_size.y);

		Hal._cursor.visible = true;
		Hal._cursor.dirty = false;
	}

	/*#if defined(_DEBUG)
	private static void DbgScreenRect(int left, int top, int right, int bottom)
	{
		DrawPixelInfo dp = new ;
		DrawPixelInfo  old;

		old = Hal._cur_dpi;
		Hal._cur_dpi = &dp;
		dp = Hal._screen;
		GfxFillRect(left, top, right - 1, bottom - 1, rand() & 255);
		Hal._cur_dpi = old;
	}
	#endif*/

	static void RedrawScreenRect(int left, int top, int right, int bottom)
	{
		assert(right <= Hal._screen.width && bottom <= Hal._screen.height);
		if (Hal._cursor.visible) {
			if (right > Hal._cursor.draw_pos.x &&
					left < Hal._cursor.draw_pos.x + Hal._cursor.draw_size.x &&
					bottom > Hal._cursor.draw_pos.y &&
					top < Hal._cursor.draw_pos.y + Hal._cursor.draw_size.y) {
				UndrawMouseCursor();
			}
		}
		UndrawTextMessage();

		/* #if defined(_DEBUG)
		if (_dbg_screen_rect)
			DbgScreenRect(left, top, right, bottom);
		else
	#endif */
		DrawOverlappedWindowForAll(left, top, right, bottom);

		Global.hal._video_driver.make_dirty(left, top, right - left, bottom - top);
	}

	static void DrawDirtyBlocks()
	{
		byte *b = _dirty_blocks;
		final int w = BitOps.ALIGN(Hal._screen.width, 64);
		final int h = BitOps.ALIGN(Hal._screen.height, 8);
		int x;
		int y;

		y = 0;
		do {
			x = 0;
			do {
				if (*b != 0) {
					int left;
					int top;
					int right = x + 64;
					int bottom = y;
					byte *p = b;
					int h2;

					// First try coalescing downwards
					do {
						*p = 0;
						p += DIRTY_BYTES_PER_LINE;
						bottom += 8;
					} while (bottom != h && *p != 0);

					// Try coalescing to the right too.
					h2 = (bottom - y) >> 3;
					assert(h2 > 0);
					p = b;

					while (right != w) {
						byte *p2 = ++p;
						int h = h2;
						// Check if a full line of dirty flags is set.
						do {
							if (!*p2) goto no_more_coalesc;
							p2 += DIRTY_BYTES_PER_LINE;
						} while (--h != 0);

						// Wohoo, can combine it one step to the right!
						// Do that, and clear the bits.
						right += 64;

						h = h2;
						p2 = p;
						do {
							*p2 = 0;
							p2 += DIRTY_BYTES_PER_LINE;
						} while (--h != 0);
					}
					no_more_coalesc:

						left = x;
					top = y;

					if (left   < _invalid_rect.left  ) left   = _invalid_rect.left;
					if (top    < _invalid_rect.top   ) top    = _invalid_rect.top;
					if (right  > _invalid_rect.right ) right  = _invalid_rect.right;
					if (bottom > _invalid_rect.bottom) bottom = _invalid_rect.bottom;

					if (left < right && top < bottom) {
						RedrawScreenRect(left, top, right, bottom);
					}

				}
			} while (b++, (x += 64) != w);
		} while (b += -(w >> 6) + DIRTY_BYTES_PER_LINE, (y += 8) != h);

		_invalid_rect.left = w;
		_invalid_rect.top = h;
		_invalid_rect.right = 0;
		_invalid_rect.bottom = 0;
	}

	/*
	void SetDirtyBlocks(int left, int top, int right, int bottom)
	{
		byte *b;
		int width;
		int height;

		if (left < 0) left = 0;
		if (top < 0) top = 0;
		if (right > Hal._screen.width) right = Hal._screen.width;
		if (bottom > Hal._screen.height) bottom = Hal._screen.height;

		if (left >= right || top >= bottom) return;

		if (left   < _invalid_rect.left  ) _invalid_rect.left   = left;
		if (top    < _invalid_rect.top   ) _invalid_rect.top    = top;
		if (right  > _invalid_rect.right ) _invalid_rect.right  = right;
		if (bottom > _invalid_rect.bottom) _invalid_rect.bottom = bottom;

		left >>= 6;
		top  >>= 3;

		b = _dirty_blocks + top * DIRTY_BYTES_PER_LINE + left;

		width  = ((right  - 1) >> 6) - left + 1;
		height = ((bottom - 1) >> 3) - top  + 1;

		assert(width > 0 && height > 0);

		do {
			int i = width;

			do b[--i] = 0xFF; while (i);

			b += DIRTY_BYTES_PER_LINE;
		} while (--height != 0);
	}

	void MarkWholeScreenDirty()
	{
		SetDirtyBlocks(0, 0, Hal._screen.width, Hal._screen.height);
	}

	boolean FillDrawPixelInfo(DrawPixelInfo  n, final DrawPixelInfo  o, int left, int top, int width, int height)
	{
		int t;

		if (o == null) o = Hal._cur_dpi;

		n.zoom = 0;

		assert(width > 0);
		assert(height > 0);

		n.left = 0;
		if ((left -= o.left) < 0) {
			width += left;
			if (width < 0) return false;
			n.left = -left;
			left = 0;
		}

		if ((t=width + left - o.width) > 0) {
			width -= t;
			if (width < 0) return false;
		}
		n.width = width;

		n.top = 0;
		if ((top -= o.top) < 0) {
			height += top;
			if (height < 0) return false;
			n.top = -top;
			top = 0;
		}

		n.dst_ptr = o.dst_ptr + left + top * (n.pitch = o.pitch);

		if ((t=height + top - o.height) > 0) {
			height -= t;
			if (height < 0) return false;
		}
		n.height = height;

		return true;
	}

	static void SetCursorSprite(CursorID cursor)
	{
		CursorVars *cv = &_cursor;
		final Sprite *p;

		if (cv.sprite == cursor) return;

		p = GetSprite(cursor & SPRITE_MASK);
		cv.sprite = cursor;
		cv.size.y = p.height;
		cv.size.x = p.width;
		cv.offs.x = p.x_offs;
		cv.offs.y = p.y_offs;

		cv.dirty = true;
	}

	static void SwitchAnimatedCursor()
	{
		CursorVars *cv = &_cursor;
		final CursorID *cur = cv.animate_cur;
		CursorID sprite;

		// ANIM_CURSOR_END is 0xFFFF in table/animcursors.h
		if (cur == null || *cur == 0xFFFF) cur = cv.animate_list;

		sprite = cur[0];
		cv.animate_timeout = cur[1];
		cv.animate_cur = cur + 2;

		SetCursorSprite(sprite);
	}

	void CursorTick()
	{
		if (_cursor.animate_timeout != 0 && --_cursor.animate_timeout == 0)
			SwitchAnimatedCursor();
	}

	void SetMouseCursor(CursorID cursor)
	{
		// Turn off animation
		_cursor.animate_timeout = 0;
		// Set cursor
		SetCursorSprite(cursor);
	}

	void SetAnimatedMouseCursor(final CursorID *table)
	{
		_cursor.animate_list = table;
		_cursor.animate_cur = null;
		SwitchAnimatedCursor();
	}

	boolean ChangeResInGame(int w, int h)
	{
		return
			(Hal._screen.width == w && Hal._screen.height == h) ||
			Global.hal._video_driver.change_resolution(w, h);
	}

	void ToggleFullScreen(boolean fs) {Global.hal._video_driver.toggle_fullscreen(fs);}

	static int CDECL compare_res(final void *pa, final void *pb)
	{
		int x = ((final int*)pa)[0] - ((final int*)pb)[0];
		if (x != 0) return x;
		return ((final int*)pa)[1] - ((final int*)pb)[1];
	}

	void SortResolutions(int count)
	{
		qsort(_resolutions, count, sizeof(_resolutions[0]), compare_res);
	}

	int GetDrawStringPlayerColor(PlayerID player)
	{
		// Get the color for DrawString-subroutines which matches the color
		//  of the player
		if (player == OWNER_SPECTATOR || player == OWNER_SPECTATOR - 1) return 1;
		return (_color_list[_player_colors[player]].window_color_1b) | IS_PALETTE_COLOR;
	}
	 */	

}




class Colour {
	byte r;
	byte g;
	byte b;
} 
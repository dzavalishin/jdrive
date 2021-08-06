package game;

import java.util.Arrays;
import java.util.function.Consumer;

import game.Gfx.BlitterParams;
import game.tables.PaletteTabs;
import game.util.ArrayPtr;
import game.util.BitOps;
import game.util.Colour;
import game.util.Pixel;

public class Gfx extends PaletteTabs 
{

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

	static final int ASCII_LETTERSTART = 32;
	public static int _stringwidth_base = 0;
	//VARDEF byte _stringwidth_table[0x2A0];
	private static byte [] _stringwidth_table = new byte[0x2A0];

	static int _stringwidth_out;
	//static /* Pixel */ byte  []_cursor_backup = new /* Pixel */ byte [64 * 64];



	static byte [] _cursor_backup = new byte[64 * 64];
	//static Rect _invalid_rect;
	static byte [] _color_remap_ptr;
	static byte [] _string_colorremap = new byte[3];

	static final int DIRTY_BYTES_PER_LINE = (Global.MAX_SCREEN_WIDTH / 64);
	static byte [] _dirty_blocks = new byte[DIRTY_BYTES_PER_LINE * Global.MAX_SCREEN_HEIGHT / 8];


	/* 
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

	static void memcpy_pitch(
			byte[] d, byte []s, 
			int w, int h, 
			int spitch, int dpitch,
			int d_offset, int s_offset
			)
	{
		//byte *dp = (byte*)d;
		//byte *sp = (byte*)s;
		int sp = s_offset;
		int dp = d_offset;

		assert(h >= 0);
		for (; h != 0; --h) {
			//memcpy(dp, sp, w);
			System.arraycopy(s, sp, d, dp, w);

			dp += dpitch;
			sp += spitch;
		}
	}


	static void GfxScroll(int left, int top, int width, int height, int xo, int yo)
	{
		//final /* Pixel */ byte  src;
		///* Pixel */ byte  dst;
		int  src; // index
		int  dst; // index
		int p;
		int ht;

		if (xo == 0 && yo == 0) return;

		if (Hal._cursor.visible) UndrawMouseCursor();
		TextEffect.UndrawTextMessage();

		p = Hal._screen.pitch;

		if (yo > 0) {
			// Calculate pointers
			//dst = Hal._screen.dst_ptr + (top + height - 1) * p + left;
			dst = (top + height - 1) * p + left;
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
				//memcpy(dst, src, width);
				System.arraycopy(Hal._screen.dst_ptr, src, Hal._screen.dst_ptr, dst, width);
				src -= p;
				dst -= p;
			}
		} else {
			// Calculate pointers
			dst = top * p + left;
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
				//memmove(dst, src, width);
				System.arraycopy(Hal._screen.dst_ptr, src, Hal._screen.dst_ptr, dst, width);
				src += p;
				dst += p;
			}
		}
		// This part of the screen is now dirty.
		Global.hal.make_dirty(left, top, width, height);
	}

	static void memset( int[] mem, int dst, int color, int count)
	{
		while(count-- > 0)
			mem[dst++] = color;
	}

	static void GfxFillRect(int left, int top, int right, int bottom, int color)
	{
		final DrawPixelInfo  dpi = Hal._cur_dpi;
		Pixel dst;
		//int dst; // index
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

		dst = new Pixel( dpi.dst_ptr,  top * dpi.pitch + left );
		//dst = top * dpi.pitch + left;

		if (0==(color & Sprite.PALETTE_MODIFIER_GREYOUT)) {
			if (0==(color & Sprite.USE_COLORTABLE)) {
				do {
					//memset(dpi.dst_ptr, dst, color, right);
					dst.memset( (byte)color, right );
					//dst += dpi.pitch;
					dst.madd( dpi.pitch );
				} while (--bottom > 0);
			} else {
				/* use colortable mode */
				final byte[] ctab = BitOps.subArray( SpriteCache.GetNonSprite(color & Sprite.COLORTABLE_MASK), 1 );

				do {
					int i;
					for (i = 0; i != right; i++) 
						//dpi.dst_ptr[dst+i] = ctab[i%ctab.length]; //ctab[dst+i];
						dst.w(i, ctab[i%ctab.length]); //ctab[dst+i];
					dst.madd( dpi.pitch );
				} while (--bottom > 0);
			}
		} else {
			byte bo =(byte) ((oleft - left + dpi.left + otop - top + dpi.top) & 1);
			do {
				int i;
				for (i = (bo ^= 1); i < right; i += 2)
				{
					//dpi.dst_ptr[dst+i] = (byte)color;
					dst.w( i, (byte)color );
				}

				dst.madd( dpi.pitch );
			} while (--bottom > 0);
		}
	}

	static void GfxSetPixel(int x, int y, int color)
	{
		final DrawPixelInfo  dpi = Hal._cur_dpi;
		if ((x-=dpi.left) < 0 || x>=dpi.width || (y-=dpi.top)<0 || y>=dpi.height)
			return;
		dpi.dst_ptr.w( y * dpi.pitch + x,  (byte) color );
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
		String buffer = Global.GetString( str);
		return DoDrawString(buffer, x, y, color);
	}
	/* returns right coordinate */
	static int DrawString(int x, int y, StringID str, int color)
	{
		return DrawString(x, y, str.id, color);
	}

	static int DrawStringTruncated(int x, int y, StringID str, int color, int maxw)
	{
		//char buffer[512];
		//TruncateStringID(str, buffer, maxw);
		String buffer = TruncateStringID(str, maxw, null);
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
		String buffer = TruncateStringID(str, maxw, null);
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
		char c;

		char sc[] = str.toCharArray();
		int sp = 0;

		for(;;) {
			w = 0;
			last_space = null;

			for(;;) {
				c = sc[sp++]; // *str++;
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

					if (c == ASCII_SETX) sp++;
					else if (c == ASCII_SETXY) sp += 2;
					else if (c == ASCII_TINYFONT) base = 224;
					else if (c == ASCII_BIGFONT) base = 448;
				}
			}

			num++;
			//str[-1] = '\0'; TODO why?
		}
	}

	//static void DrawStringMultiCenter(int x, int y, StringID str, int maxw)
	static void DrawStringMultiCenter(int x, int y, int str, int maxw)
	{
		//char buffer[512];
		int tmp;
		int num, w, mt;
		final String src;
		char c;

		String buffer = Global.GetString(str);
		char sc[] = buffer.toCharArray();
		int sp = 0;

		tmp = FormatStringLinebreaks(buffer, maxw); // TODO used?
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
				c = sc[sp++]; // *src++;
				if (c == 0) {
					y += mt;
					if (--num < 0) {
						_stringwidth_base = 0;
						return;
					}
					break;
				} else if (c == ASCII_SETX) {
					sp++;
				} else if (c == ASCII_SETXY) {
					sp+=2;
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
		char c;

		String buffer = Global.GetString(str);

		tmp = FormatStringLinebreaks(buffer, maxw);
		num = BitOps.GB(tmp, 0, 16);

		switch (BitOps.GB(tmp, 16, 16)) {
		case   0: mt = 10; break;
		case 244: mt =  6; break;
		default:  mt = 18; break;
		}

		src = buffer;
		char sc[] = src.toCharArray();
		int sp = 0;

		for(;;) {
			DoDrawString(src, x, y, 0xFE);
			_stringwidth_base = _stringwidth_out;

			for(;;) {
				c = sc[sp++]; // *src++;
				if (c == 0) {
					y += mt;
					if (--num < 0) {
						_stringwidth_base = 0;
						return;
					}
					break;
				} else if (c == ASCII_SETX) {
					sp++;
				} else if (c == ASCII_SETXY) {
					sp+=2;
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
		int color_2 = Global._color_list[ctab].window_color_1a;
		int color_interior = Global._color_list[ctab].window_color_bga;
		int color_3 = Global._color_list[ctab].window_color_bgb;
		int color = Global._color_list[ctab].window_color_2;

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
		return DrawStringStateMachine.DoDrawString(string, x, y, real_color);

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
			_color_remap_ptr = BitOps.subArray( SpriteCache.GetNonSprite(BitOps.GB(img, Sprite.PALETTE_SPRITE_START, Sprite.PALETTE_SPRITE_WIDTH)), 1);
			GfxMainBlitter(SpriteCache.GetSprite(img & Sprite.SPRITE_MASK), x, y, 1);
		} else if(0 != (img & Sprite.PALETTE_MODIFIER_TRANSPARENT)) {
			_color_remap_ptr = BitOps.subArray( SpriteCache.GetNonSprite(BitOps.GB(img, Sprite.PALETTE_SPRITE_START, Sprite.PALETTE_SPRITE_WIDTH)), 1);
			GfxMainBlitter(Sprite.GetSprite(img & Sprite.SPRITE_MASK), x, y, 2);
		} else {
			GfxMainBlitter(Sprite.GetSprite(img & Sprite.SPRITE_MASK), x, y, 0);
		}
	}

	static public class BlitterParams {
		int start_x, start_y;
		//byte[] sprite;
		Pixel sprite;
		//byte[] sprite_org;
		Pixel sprite_org;

		///* Pixel */ byte  *dst;
		//int[]  dst_mem;
		//int  dst_offset;

		Pixel dst;

		int mode;
		int width, height;
		int width_org;
		int height_org;
		int pitch;
		byte info;

		BlitterParams(Pixel screen)
		{
			dst = new Pixel(screen);
		}

		BlitterParams(Pixel screen, int shift)
		{
			dst = new Pixel(screen, shift);
		}

	} 

	private static void GfxBlitTileZoomIn(BlitterParams bp)
	{
		//final byte[] src_o_data = bp.sprite;
		//int src_o_shift = 0; // start index to access, replace src_o_data += x with src_o_shift += x 
		//final byte* src;
		Pixel src_o = new Pixel(bp.sprite);

		//final byte[] src_data;
		//int src_shift = 0;

		int num, skip;
		byte done;
		///* Pixel */ byte  *dst;
		///* Pixel */ byte []  dst_data;
		//int dst_shift = 0;

		//final byte* ctab;
		//final byte[] ctab;

		if(0 != (bp.mode & 1) ) {
			//src_o_shift += BitOps.READ_LE_UINT16(src_o_data, bp.start_y * 2);
			src_o.madd( BitOps.READ_LE_UINT16(src_o.getMem(), bp.start_y * 2) );

			do {
				do {
					done = src_o.read(0); // src_o_data[src_o_shift];
					num = done & 0x7F;
					skip = src_o.read(1); // src_o_data[src_o_shift+1];
					//src = src_o + 2;
					//src_data = src_o_data;
					//src_shift = src_o_shift + 2;
					Pixel src = new Pixel( src_o, 2 );

					//src_o += num + 2;
					//src_o_shift += num + 2;
					src_o.madd( num + 2 );

					//dst_data = bp.dst_mem;
					//dst_shift = 0;
					Pixel dst = new Pixel(bp.dst);

					if ( (skip -= bp.start_x) > 0) {
						//dst += skip;
						//dst_shift += skip;
						dst.madd(skip);
					} else {
						//src -= skip;
						//src_shift -= skip;
						src.madd(-skip);

						num += skip;

						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}

					final byte[] ctab = _color_remap_ptr;

					for (; num >= 4; num -=4) {
						/*
						dst_data[3+dst_shift] = ctab[src_data[3+src_shift]];
						dst_data[2+dst_shift] = ctab[src_data[2+src_shift]];
						dst_data[1+dst_shift] = ctab[src_data[1+src_shift]];
						dst_data[0+dst_shift] = ctab[src_data[0+src_shift]];
						 */
						dst.w( 3, ctab[src.r(3)] );
						dst.w( 2, ctab[src.r(2)] );
						dst.w( 1, ctab[src.r(1)] );
						dst.w( 0, ctab[src.r(0)] );


						//dst_shift += 4;
						//src_shift += 4;
						src.madd(4);
						dst.madd(4);
					}
					for (; num != 0; num--)
					{
						//dst_data[dst_shift++] = ctab[src_data[src_shift++]];
						dst.w( 0, ctab[src.r(0)] );
						src.madd(1);
						dst.madd(1);
					}
				} while (0==(done & 0x80));

				//bp.dst += bp.pitch;
				//bp.dst_offset += bp.pitch;
				bp.dst.madd( bp.pitch );

			} while (--bp.height != 0);
		} 
		else if(0 != (bp.mode & 2) ) 
		{
			//src_o_shift += BitOps.READ_LE_UINT16(src_o_data, bp.start_y * 2);
			src_o.madd( BitOps.READ_LE_UINT16(src_o.getMem(), bp.start_y * 2) );

			do {
				do {
					//done = src_o_data[0+src_o_shift];
					done = src_o.r(0);
					num = done & 0x7F;
					//skip = src_o_data[1+src_o_shift];
					skip = src_o.r(1);
					//src_o_shift += num + 2;
					src_o.madd(2);

					//dst_data = bp.dst_mem;
					Pixel dst = new Pixel( bp.dst );

					if ( (skip -= bp.start_x) > 0) {
						//dst_shift += skip;
						dst.madd(skip);
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

					final byte[] ctab = _color_remap_ptr;
					for (; num != 0; num--) {
						//*dst = ctab[*dst];
						//dst_data[0+dst_shift] = ctab[dst_data[0+dst_shift]]; // TODO displacement
						dst.w( 0, ctab[dst.r(0)] );
						//dst_shift++;
						dst.madd(1);
					}
				} while (0==(done & 0x80));

				//bp.dst_offset += bp.pitch;
				bp.dst.madd(bp.pitch);

			} while (--bp.height != 0);
		} else {
			//src_o_shift += BitOps.READ_LE_UINT16(src_o_data, bp.start_y * 2);
			src_o.madd( BitOps.READ_LE_UINT16(src_o.getMem(), bp.start_y * 2) );
			do {
				do {
					//done = src_o_data[0+src_o_shift];
					done = src_o.r(0);
					num = done & 0x7F;
					//skip = src_o_data[1+src_o_shift];
					skip = src_o.r(1);

					//src_data = src_o_data;
					//src_shift = src_o_shift + 2;
					Pixel src = new Pixel( src_o, 2 );

					//src_o_shift += num + 2;
					src_o.madd( num + 2 );

					//dst_data = bp.dst_mem;
					//dst_shift = bp.dst_offset;

					Pixel dst = new Pixel(bp.dst);

					if ( (skip -= bp.start_x) > 0) {
						//dst_shift += skip;
						dst.madd(skip);
					} else {
						//src_shift -= skip;
						src.madd(-skip);
						num += skip;
						if (num <= 0) continue;
						skip = 0;
					}

					skip = skip + num - bp.width;
					if (skip > 0) {
						num -= skip;
						if (num <= 0) continue;
					}
					/*#if defined(_WIN32)
					if (num & 1) *dst++ = *src++;
					if (num & 2) { *(int*)dst = *(int*)src; dst += 2; src += 2; }
					if (num >>= 2) {
						do {
					 *(int*)dst = *(int*)src;
							dst += 4;
							src += 4;
						} while (--num != 0);
					}
					#else*/
					//memcpy(dst, src, num);
					//System.arraycopy(src_data, src_shift, dst_data, dst_shift, num);
					dst.copyFrom(src, num);
					//#endif
				} while (0==(done & 0x80));

				//bp.dst_offset += bp.pitch;
				bp.dst.madd(bp.pitch);
			} while (--bp.height != 0);
		}
	}

	private static void GfxBlitZoomInUncomp(BlitterParams bp)
	{
		//final byte [] src_data = bp.sprite;
		//int src_shift = 0;
		Pixel src = new Pixel(bp.sprite); 
		///* Pixel */ byte  *dst = bp.dst;

		/* Pixel */ 
		//byte  []dst_data = bp.dst_mem;
		//int dst_shift = bp.dst_offset;
		Pixel dst = new Pixel(bp.dst);

		int height = bp.height;
		int width = bp.width;
		int i;

		assert(height > 0);
		assert(width > 0);

		if(0 != (bp.mode & 1) ) {
			if(0 != (bp.info & 1) ) {
				final byte []ctab = _color_remap_ptr;

				do {
					for (i = 0; i != width; i++) {
						//byte b = ctab[src_data[i+src_shift]];
						byte b = ctab[src.r(i)];

						//if (b != 0) dst_data[i+dst_shift] = b;
						if (b != 0) dst.w(i, b);
					}
					//src_shift += bp.width_org;
					//dst_shift += bp.pitch;
					src.madd(bp.width_org);
					dst.madd(bp.pitch);
				} while (--height != 0);
			}
		} else if(0 != (bp.mode & 2) ) {
			if(0 != (bp.info & 1) ) {
				final byte [] ctab = _color_remap_ptr;

				do {
					for (i = 0; i != width; i++)
					{
						//if (src_data[i+src_shift] != 0) dst_data[i+dst_shift] = ctab[dst_data[i+dst_shift]];
						if (src.r(i) != 0) dst.w(i, ctab[dst.r(i)]);
					}
					//src_shift += bp.width_org;
					//dst_shift += bp.pitch;
					src.madd(bp.width_org);
					dst.madd(bp.pitch);
				} while (--height != 0);
			}
		} else {
			if (0==(bp.info & 1)) {
				do {
					//memcpy(dst, src, width);
					//System.arraycopy(src_data, src_shift, dst_data, dst_shift, width);
					dst.copyFrom( src, width);
					//src_shift += bp.width_org;
					//dst_shift += bp.pitch;
					src.madd(bp.width_org);
					dst.madd(bp.pitch);
				} while (--height != 0);
			} else {
				do {
					int n = width;

					for (; n >= 4; n -= 4) {
						/*
						if (src_data[0+src_shift] != 0) dst_data[0+dst_shift] = src_data[0+src_shift];
						if (src_data[1+src_shift] != 0) dst_data[1+dst_shift] = src_data[1+src_shift];
						if (src_data[2+src_shift] != 0) dst_data[2+dst_shift] = src_data[2+src_shift];
						if (src_data[3+src_shift] != 0) dst_data[3+dst_shift] = src_data[3+src_shift];
						 */
						if( src.r(0) != 0 ) dst.w(0, src.r(0));
						if( src.r(1) != 0 ) dst.w(1, src.r(1));
						if( src.r(2) != 0 ) dst.w(2, src.r(2));
						if( src.r(3) != 0 ) dst.w(3, src.r(3));

						//dst_shift += 4;
						//src_shift += 4;
						src.madd(4);
						dst.madd(4);
					}

					for (; n != 0; n--) {
						//if (src_data[0+src_shift] != 0) dst_data[0+dst_shift] = src_data[0+src_shift];
						if( src.r(0) != 0 ) dst.w(0, src.r(0));
						//src_shift++;
						//dst_shift++;
						src.madd(1);
						dst.madd(1);
					}

					//src_shift += bp.width_org - width;
					//dst_shift += bp.pitch - width;
					src.madd(bp.width_org - width);
					dst.madd(bp.pitch - width);
				} while (--height != 0);
			}
		}
	}

	/*
	private static void GfxBlitTileZoomMedium(BlitterParams bp)
	{
		final byte* src_o = bp.sprite;
		final byte* src;
		int num, skip;
		byte done;
		// Pixel  
		byte  *dst;
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
		// Pixel  
		byte  *dst = bp.dst;
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
		// Pixel  
		byte  *dst;
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
		// Pixel  
		byte  *dst = bp.dst;
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
	 */
	//typedef void (*BlitZoomFunc)(BlitterParams bp);

	static final BlitZoomFunc zf_tile[] =
		{
				Gfx::GfxBlitTileZoomIn,
				Gfx::GfxBlitTileZoomIn,
				Gfx::GfxBlitTileZoomIn,

				//Gfx::GfxBlitTileZoomIn,
				//Gfx::GfxBlitTileZoomMedium,
				//Gfx::GfxBlitTileZoomOut
		};

	static final BlitZoomFunc zf_uncomp[] =
		{
				Gfx::GfxBlitZoomInUncomp,
				Gfx::GfxBlitZoomInUncomp,
				Gfx::GfxBlitZoomInUncomp,

				//Gfx::GfxBlitZoomInUncomp,
				//Gfx::GfxBlitZoomMediumUncomp,
				//Gfx::GfxBlitZoomOutUncomp
		};

	static void GfxMainBlitter(final Sprite  sprite, int x, int y, int mode)
	{
		final DrawPixelInfo  dpi = Hal._cur_dpi;
		int start_x, start_y;
		byte info;
		BlitterParams bp = new BlitterParams(dpi.dst_ptr);

		// TODO Fix Zoom
		dpi.zoom = 0;

		int zoom_mask = ~((1 << dpi.zoom) - 1);


		/* decode sprite header */
		x += sprite.x_offs;
		y += sprite.y_offs;
		bp.width_org = bp.width = sprite.width;
		bp.height_org = bp.height = sprite.height;
		info = (byte) sprite.info;
		bp.info = info;
		//bp.sprite_org = bp.sprite = sprite.data;
		bp.sprite_org = new Pixel( sprite.data );
		bp.sprite = new Pixel( sprite.data );
		//bp.dst = dpi.dst_ptr;
		bp.mode = mode;
		bp.pitch = dpi.pitch;

		assert(bp.height > 0);
		assert(bp.width > 0);

		if(0 != (info & 8) ) {
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
				bp.dst.madd( bp.pitch * (y >> dpi.zoom) );
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
			bp.dst.madd( x >> dpi.zoom );

			if ( (x = x + bp.width - dpi.width) > 0) {
				bp.width -= x;
				if (bp.width <= 0) return;
			}

			zf_tile[dpi.zoom].accept(bp); // TODO apply?
		} else {
			bp.sprite.madd( bp.width * (bp.height & ~zoom_mask) );
			bp.height &= zoom_mask;
			if (bp.height == 0) return;

			y &= zoom_mask;

			if ( (y -= dpi.top) < 0) {
				bp.height += y;
				if (bp.height <= 0) return;
				bp.sprite.madd( - (bp.width * y) );
				y = 0;
			} else {
				bp.dst.madd( bp.pitch * (y >> dpi.zoom) );
			}

			if (bp.height > dpi.height - y) {
				bp.height = dpi.height - y;
				if (bp.height <= 0) return;
			}

			x &= zoom_mask;

			if ( (x -= dpi.left) < 0) {
				bp.width += x;
				if (bp.width <= 0) return;
				bp.sprite.madd( -x );
				x = 0;
			}
			bp.dst.madd( x >> dpi.zoom );

			if (bp.width > dpi.width - x) {
				bp.width = dpi.width - x;
				if (bp.width <= 0) return;
			}

			zf_uncomp[dpi.zoom].accept(bp);
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

	static int EXTR(int p, int q) { return (((int)(Global._timer_counter * p) * q) >> 16); }
	static int EXTR2(int p, int q) { return (((int)(~Global._timer_counter * p) * q) >> 16); }

	static void DoPaletteAnimations()
	{
		Colour [] s;
		/* Amount of colors to be rotated.
		 * A few more for the DOS palette, because the water colors are
		 * 245-254 for DOS and 217-226 for Windows.  */
		final ExtraPaletteValues ev = _extra_palette_values;
		int c = Global._use_dos_palette ? 38 : 28;
		Colour [] old_val = new Colour[38]; // max(38, 28)
		int i;
		int j;

		//Colour d;
		//d = _cur_palette[217];
		//memcpy(old_val, d, c * sizeof(old_val));
		ArrayPtr<Colour> d = new ArrayPtr<Colour>( _cur_palette, 217 );  

		System.arraycopy(_cur_palette, 217, old_val, 0, c);

		// Dark blue water
		s = (GameOptions._opt.landscape == Landscape.LT_CANDY) ? ev.ac : ev.a;
		j = EXTR(320, 5);
		for (i = 0; i != 5; i++) {
			//*d++ = s[j];
			d.wpp(s[j]);
			j++;
			if (j == 5) j = 0;
		}

		// Glittery water
		s = (GameOptions._opt.landscape == Landscape.LT_CANDY) ? ev.bc : ev.b;
		j = EXTR(128, 15);
		for (i = 0; i != 5; i++) {
			//*d++ = s[j];
			d.wpp(s[j]);
			j += 3;
			if (j >= 15) j -= 15;
		}

		s = ev.e;
		j = EXTR2(512, 5);
		for (i = 0; i != 5; i++) {
			//*d++ = s[j];
			//d[i] = s[j];
			d.wpp(s[j] );
			j++;
			if (j == 5) j = 0;
		}

		// Oil refinery fire animation
		s = ev.oil_ref;
		j = EXTR2(512, 7);
		for (i = 0; i != 7; i++) {
			//*d++ = s[j];
			d.wpp(s[j]);
			j++;
			if (j == 7) j = 0;
		}
		// TODO d += 4

		// Radio tower blinking
		{
			//byte 
			i = (Global._timer_counter >> 1) & 0x7F;
			//byte 
			int v;

			/*
			(v = 255, i < 0x3f) ||
			(v = 128, i < 0x4A || i >= 0x75) ||
			(v = 20);
			 */
			if(i >= 0x3f)		v = 255;
			else if(i >= 0x4A && i < 0x75)	v = 128;
			else v = 20;

			/*d.r = v;
			d.g = 0;
			d.b = 0;
			d++;*/
			d.wpp( new Colour(v, 0, 0) );

			i ^= 0x40;

			/*(v = 255, i < 0x3f) ||
			(v = 128, i < 0x4A || i >= 0x75) ||
			(v = 20);*/
			if(i >= 0x3f)		v = 255;
			else if(i >= 0x4A && i < 0x75)	v = 128;
			else v = 20;

			/*d.r = v;
			d.g = 0;
			d.b = 0;
			d++;*/
			d.wpp( new Colour(v, 0, 0) );
		}

		// Handle lighthouse and stadium animation
		s = ev.lighthouse;
		j = EXTR(256, 4);
		for (i = 0; i != 4; i++) {
			//*d++ = s[j];
			d.wpp(s[j]);
			j++;
			if (j == 4) j = 0;
		}

		/* TODO // Animate water for old DOS graphics
		if (Global._use_dos_palette) {
			// Dark blue water DOS
			s = (GameOptions._opt.landscape == LT_CANDY) ? ev.ac : ev.a;
			j = EXTR(320, 5);
			for (i = 0; i != 5; i++) {
				//*d++ = s[j];
				d[i] = s[j];
				j++;
				if (j == 5) j = 0;
			}
			// TODO d += 4

			// Glittery water DOS
			s = (GameOptions._opt.landscape == LT_CANDY) ? ev.bc : ev.b;
			j = EXTR(128, 15);
			for (i = 0; i != 5; i++) {
				//*d++ = s[j];
				d[i] = s[j];
				j += 3;
				if (j >= 15) j -= 15;
			}
			// TODO d += 4
		} */

		//Arrays.compare(null, null)
		//BitOps.memcmp()
		//int cmp = Arrays<Colour>.compare(old_val, 0, c, _cur_palette, 217, 217+c);
		boolean cmp = Arrays.equals(old_val, 0, c, _cur_palette, 217, 217+c);
		
		//if (memcmp(old_val, _cur_palette+217, c * sizeof(old_val[0])) != 0) 
		if (cmp) 
		{
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
			_stringwidth_table[bp++] = (byte) (i != 97 && (i < 99 || i > 113) && i != 116 && i != 117 && (i < 123 || i > 129) && (i < 151 || i > 153) && i != 155 ? SpriteCache.GetSprite(i).width : 0);
		}

		for (i = 226; i != 450; i++) {
			//*b++ = i != 321 && (i < 323 || i > 353) && i != 367 && (i < 375 || i > 377) && i != 379 ? GetSprite(i).width + 1 : 0;
			_stringwidth_table[bp++] = (byte) (i != 321 && (i < 323 || i > 353) && i != 367 && (i < 375 || i > 377) && i != 379 ? SpriteCache.GetSprite(i).width + 1 : 0);
		}

		for (i = 450; i != 674; i++) {
			//*b++ = (i < 545 || i > 577) && i != 585 && i != 587 && i != 588 && (i < 590 || i > 597) && (i < 599 || i > 601) && i != 603 && i != 633 && i != 665 ? GetSprite(i).width + 1 : 0;
			_stringwidth_table[bp++] = (byte) ((i < 545 || i > 577) && i != 585 && i != 587 && i != 588 && (i < 590 || i > 597) && (i < 599 || i > 601) && i != 603 && i != 633 && i != 665 ? SpriteCache.GetSprite(i).width + 1 : 0);
		}
	}

	static int GetCharacterWidth(int key)
	{
		assert(key >= ASCII_LETTERSTART && key - ASCII_LETTERSTART < _stringwidth_table.length);
		return _stringwidth_table[key - ASCII_LETTERSTART];
	}


	static void ScreenSizeChanged()
	{
		/* TODO ScreenSizeChanged()
		// check the dirty rect
		if (Hal._invalid_rect.right >= Hal._screen.width) Hal._invalid_rect.right = Hal._screen.width;
		if (Hal._invalid_rect.bottom >= Hal._screen.height) Hal._invalid_rect.bottom = Hal._screen.height;

		// screen size changed and the old bitmap is invalid now, so we don't want to undraw it
		Hal._cursor.visible = false;
		 */
	}

	static void UndrawMouseCursor()
	{
		if (Hal._cursor.visible) {
			Hal._cursor.visible = false;
			memcpy_pitch(
					//Hal._screen.dst_ptr + Hal._cursor.draw_pos.x + Hal._cursor.draw_pos.y * Hal._screen.pitch,
					Hal._screen.dst_ptr.getMem(),					
					_cursor_backup,

					Hal._cursor.draw_size.x, 
					Hal._cursor.draw_size.y,

					//Hal._cursor.draw_size.x, 
					Hal._cursor.draw_size.x, 
					Hal._screen.pitch,

					Hal._screen.dst_ptr.getDisplacement() + Hal._cursor.draw_pos.x + Hal._cursor.draw_pos.y * Hal._screen.pitch,
					0
					);

			Global.hal.make_dirty(Hal._cursor.draw_pos.x, Hal._cursor.draw_pos.y, Hal._cursor.draw_size.x, Hal._cursor.draw_size.y);
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

		assert(w * h < _cursor_backup.length);

		// Make backup of stuff below cursor
		memcpy_pitch(
				_cursor_backup,
				//Hal._screen.dst_ptr + Hal._cursor.draw_pos.x + Hal._cursor.draw_pos.y * Hal._screen.pitch,
				Hal._screen.dst_ptr.getMem(),

				Hal._cursor.draw_size.x, 
				Hal._cursor.draw_size.y, 
				Hal._screen.pitch, 
				Hal._cursor.draw_size.x,

				0,
				Hal._screen.dst_ptr.getDisplacement()+ Hal._cursor.draw_size.x + Hal._cursor.draw_pos.x + Hal._cursor.draw_pos.y * Hal._screen.pitch
				);

		// Draw cursor on screen
		Hal._cur_dpi = Hal._screen;
		DrawSprite(Hal._cursor.sprite.id, Hal._cursor.pos.x, Hal._cursor.pos.y);

		Global.hal.make_dirty(Hal._cursor.draw_pos.x, Hal._cursor.draw_pos.y, Hal._cursor.draw_size.x, Hal._cursor.draw_size.y);

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
		TextEffect.UndrawTextMessage();

		/* #if defined(_DEBUG)
		if (_dbg_screen_rect)
			DbgScreenRect(left, top, right, bottom);
		else
	#endif */

		Window.DrawOverlappedWindowForAll(left, top, right, bottom);
		Global.hal.make_dirty(left, top, right - left, bottom - top);
	}

	static void DrawDirtyBlocks()
	{
		//byte []b = _dirty_blocks;
		//int bp = 0;
		Pixel b = new Pixel(_dirty_blocks);

		final int w0 = BitOps.ALIGN(Hal._screen.width, 64);
		final int h0 = BitOps.ALIGN(Hal._screen.height, 8);
		int x;
		int y;


		y = 0;
		do {
			x = 0;
			do {
				if (b.r(0) != 0) {
					int left;
					int top;
					int right = x + 64;
					int bottom = y;
					//byte *p = b;
					//ArrayPtr<Byte> p = new ArrayPtr<Byte>(b);
					Pixel p = new Pixel(b);
					int h2;

					// First try coalescing downwards
					do {
						//*p = 0;
						p.w(0, (byte) 0);

						//p += DIRTY_BYTES_PER_LINE;
						p.madd(DIRTY_BYTES_PER_LINE);

						bottom += 8;
					} 
					while (bottom != h0 && p.r(0) != 0);
					//while (bottom != h && *p != 0);

					// Try coalescing to the right too.
					h2 = (bottom - y) >> 3;
					assert(h2 > 0);
					p = b;

					while (right != w0) {
						//byte *p2 = ++p;
						p.madd(1);
						Pixel p2 = new Pixel(p);

						int h3 = h2;
						// Check if a full line of dirty flags is set.
						boolean no_more_coalesc = false;
						do {
							//if (!*p2) goto no_more_coalesc;
							if (0==p2.r(0))
							{	
								//goto no_more_coalesc;
								no_more_coalesc = true;
								break;
							}
							//p2 += DIRTY_BYTES_PER_LINE;
							p2.madd(DIRTY_BYTES_PER_LINE);
						} while (--h3 != 0);

						if(no_more_coalesc)
							break;
						{
							// Wohoo, can combine it one step to the right!
							// Do that, and clear the bits.
							right += 64;

							h3 = h2;
							p2 = p;
							do {
								//*p2 = 0;
								p2.w(0, (byte) 0);
								//p2 += DIRTY_BYTES_PER_LINE;
								p2.madd( DIRTY_BYTES_PER_LINE );
							} while (--h3 != 0);
						}
					}
					//no_more_coalesc:

						left = x;
					top = y;

					if (left   < Hal._invalid_rect.left  ) left   = Hal._invalid_rect.left;
					if (top    < Hal._invalid_rect.top   ) top    = Hal._invalid_rect.top;
					if (right  > Hal._invalid_rect.right ) right  = Hal._invalid_rect.right;
					if (bottom > Hal._invalid_rect.bottom) bottom = Hal._invalid_rect.bottom;

					if (left < right && top < bottom) {
						RedrawScreenRect(left, top, right, bottom);
					}

				}
				//bp++;
				b.madd(1);
			} while ((x += 64) != w0);
			//b += -(w0 >> 6) + DIRTY_BYTES_PER_LINE;
			b.madd( -(w0 >> 6) + DIRTY_BYTES_PER_LINE );
		} while ((y += 8) != h0);

		Hal._invalid_rect.left = w0;
		Hal._invalid_rect.top = h0;
		Hal._invalid_rect.right = 0;
		Hal._invalid_rect.bottom = 0;
	}

	
	void SetDirtyBlocks(int left, int top, int right, int bottom)
	{
		//byte *b;
		int width;
		int height;

		if (left < 0) left = 0;
		if (top < 0) top = 0;
		if (right > Hal._screen.width) right = Hal._screen.width;
		if (bottom > Hal._screen.height) bottom = Hal._screen.height;

		if (left >= right || top >= bottom) return;

		if (left   < Hal._invalid_rect.left  ) Hal._invalid_rect.left   = left;
		if (top    < Hal._invalid_rect.top   ) Hal._invalid_rect.top    = top;
		if (right  > Hal._invalid_rect.right ) Hal._invalid_rect.right  = right;
		if (bottom > Hal._invalid_rect.bottom) Hal._invalid_rect.bottom = bottom;

		left >>= 6;
		top  >>= 3;

		Pixel b = new Pixel( _dirty_blocks, top * DIRTY_BYTES_PER_LINE + left );

		width  = ((right  - 1) >> 6) - left + 1;
		height = ((bottom - 1) >> 3) - top  + 1;

		assert(width > 0 && height > 0);

		do {
			int i = width;

			do
			{
				//b[--i] = 0xFF; 
				b.w( --i, (byte) 0xFF ); 
			}
			while (i > 0);

			//b += DIRTY_BYTES_PER_LINE;
			b.madd( DIRTY_BYTES_PER_LINE );
		} while (--height != 0);
	}

	void MarkWholeScreenDirty()
	{
		SetDirtyBlocks(0, 0, Hal._screen.width, Hal._screen.height);
	}

	boolean FillDrawPixelInfo(DrawPixelInfo  n, DrawPixelInfo  o, int left, int top, int width, int height)
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

		//n.dst_ptr = o.dst_ptr + left + top * (n.pitch = o.pitch);
		n.dst_ptr = new Pixel( o.dst_ptr, left + top * (n.pitch = o.pitch) );

		if ((t=height + top - o.height) > 0) {
			height -= t;
			if (height < 0) return false;
		}
		n.height = height;

		return true;
	}

	static void SetCursorSprite(CursorID cursor)
	{
		CursorVars cv = Hal._cursor;
		final Sprite p;

		if (cv.sprite == cursor) return;

		p = SpriteCache.GetSprite(cursor.id & Sprite.SPRITE_MASK);
		cv.sprite = cursor;
		cv.size.y = p.height;
		cv.size.x = p.width;
		cv.offs.x = p.x_offs;
		cv.offs.y = p.y_offs;

		cv.dirty = true;
	}

	/* Now in Hal and rewritten
	static void SwitchAnimatedCursor()
	{
		CursorVars cv = Hal._cursor;
		final CursorID cur = cv.animate_cur;
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


class DrawStringStateMachine
{
	private DrawPixelInfo dpi = Hal._cur_dpi;
	private int base = Gfx._stringwidth_base;
	private int sp = 0; // string pointer
	private byte color;

	private final char sc[];
	private final int xo, yo;
	private final int real_color;


	public DrawStringStateMachine(String string, int x, int y, int real_color) {
		this.real_color = real_color;
		sc = string.toCharArray();
		xo = x;
		yo = y;
	}

	public static int DoDrawString(String string, int x, int y, int real_color) {
		DrawStringStateMachine me = new DrawStringStateMachine(string, x, y, real_color);

		me.color = (byte) (real_color & 0xFF);

		return me.draw(x,y);
	}

	private int draw(int x, int y) {
		char c;

		if (color != 0xFE) {
			if (x >= dpi.left + dpi.width ||
					x + Hal._screen.width*2 <= dpi.left ||
					y >= dpi.top + dpi.height ||
					y + Hal._screen.height <= dpi.top)
				return x;

			if (color != 0xFF) {
				//switch_color:;
				switchColor();
			}
		}

		while(true) // for goto check_bounds: replacement 
		{
			//check_bounds:
			if (y + 19 <= dpi.top || dpi.top + dpi.height <= y) {
				//skip_char:;
				skipChar();
			}

			for(;;) {
				c = sc[sp++]; //*string++;
				//skip_cont:;
				if (c == 0) {
					Gfx._stringwidth_out = base;
					return x;
				}
				if (c >= Gfx.ASCII_LETTERSTART) {
					if (x >= dpi.left + dpi.width)
					{
						//goto skip_char;
						skipChar();
						continue;
					}
					if (x + 26 >= dpi.left) {
						Gfx.GfxMainBlitter(SpriteCache.GetSprite(base + 2 + c - Gfx.ASCII_LETTERSTART), x, y, 1);
					}
					x += Gfx.GetCharacterWidth(base + c);
				} else if (c == Gfx.ASCII_NL) { // newline = {}
					x = xo;
					y += 10;
					if (base != 0) {
						y -= 4;
						if (base != 0xE0)
							y += 12;
					}
					//goto check_bounds;
					break;

				} else if (c >= Gfx.ASCII_COLORSTART) { // change color?
					color = (byte)(c - Gfx.ASCII_COLORSTART);
					switchColor();
					//goto switch_color;
					//goto check_bounds;
					break;
				} else if (c == Gfx.ASCII_SETX) { // {SETX}
					x = xo + (byte)sc[sp++]; //*string++;
				} else if (c == Gfx.ASCII_SETXY) {// {SETXY}
					x = xo + (byte)sc[sp++]; // *string++;
					y = yo + (byte)sc[sp++]; // *string++;
				} else if (c == Gfx.ASCII_TINYFONT) { // {TINYFONT}
					base = 0xE0;
				} else if (c == Gfx.ASCII_BIGFONT) { // {BIGFONT}
					base = 0x1C0;
				} else {
					Global.error("Unknown string command character %d\n", c);
				}
			}
			// break from loop above leads to check_bounds label
		}
	}

	private void skipChar()
	{
		for(;;) {
			char c = sc[sp++]; //*string++;
			if (c < Gfx.ASCII_LETTERSTART)
			{
				//goto skip_cont;
				sp--;
				// fall through
			}
		}
	}

	private void switchColor()
	{
		if(0 != (real_color & Gfx.IS_PALETTE_COLOR) ) {
			Gfx._string_colorremap[1] = color;
			Gfx._string_colorremap[2] = (byte) 215;
		} else {
			Gfx._string_colorremap[1] = (byte) Gfx._string_colormap[color].text;
			Gfx._string_colorremap[2] = (byte) Gfx._string_colormap[color].shadow;
		}
		Gfx._color_remap_ptr = Gfx._string_colorremap;
	}

}

/*
class Colour {
	byte r;
	byte g;
	byte b;
} 
 */

//typedef void (*BlitZoomFunc)(BlitterParams bp);
@FunctionalInterface
interface BlitZoomFunc extends Consumer<BlitterParams> {}



package game;

import game.util.AnimCursor;
import game.util.BitOps;

public abstract class Hal
{
	// graphics
	public abstract void start_video(String parm);
	public abstract void stop_video();
	public abstract void make_dirty(int left, int top, int width, int height);
	public abstract void main_loop();
	public abstract boolean change_resolution(int w, int h);

	void toggle_fullscreen(boolean fullscreen) { } // TODO

	public static DrawPixelInfo _screen = new DrawPixelInfo();
	public static DrawPixelInfo _cur_dpi = new DrawPixelInfo();

	public static Rect _invalid_rect = new Rect();
	public static CursorVars _cursor = new CursorVars();

	//HalVideoDriver _video_driver;


	static void SetDirtyBlocks(int left, int top, int right, int bottom)
	{
		byte b[];
		int width;
		int height;

		if (left < 0) left = 0;
		if (top < 0) top = 0;
		if (right > _screen.width) right = _screen.width;
		if (bottom > _screen.height) bottom = _screen.height;

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

			do { b[--i] = (byte) 0xFF; } while (i);

			b += DIRTY_BYTES_PER_LINE;
		} while (--height != 0);
	}


	static void MarkWholeScreenDirty()
	{
		SetDirtyBlocks(0, 0, _screen.width, _screen.height);
	}

	boolean FillDrawPixelInfo(DrawPixelInfo n,  DrawPixelInfo o, int left, int top, int width, int height)
	{
		int t;

		if (o == null) o = _cur_dpi;

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

	void SetCursorSprite(CursorID cursor)
	{
		CursorVars cv =_cursor;
		Sprite p;

		if (cv.sprite == cursor) return;

		p = SpriteCache.GetSprite(cursor.id & Sprite.SPRITE_MASK);
		cv.sprite = cursor;
		cv.size.y = p.height;
		cv.size.x = p.width;
		cv.offs.x = p.x_offs;
		cv.offs.y = p.y_offs;

		cv.dirty = true;
	}

	static void SwitchAnimatedCursor()
	{
		CursorVars cv = _cursor;
		
		if(
			(cv.animate_pos >= cv.animate_list.length )
			||
			(cv.animate_list[cv.animate_pos] == null)
				||
				(cv.animate_list[cv.animate_pos].spriteId == 0xFFFF)
				)
		{
			cv.animate_pos = 0;
		}
		
		CursorID sprite = CursorID.get( cv.animate_list[cv.animate_pos].spriteId );
		cv.animate_timeout = cv.animate_list[cv.animate_pos].time;
		//cv.animate_pos += 2;
		cv.animate_pos++;
		
		SetCursorSprite(sprite);
		/*
		CursorID[] cur = cv.animate_cur;
		CursorID sprite;

		// ANIM_CURSOR_END is 0xFFFF in table/animcursors.h
		if (cur[0] == null || cur[0].id == 0xFFFF) cur = cv.animate_list;

		sprite = cur[0];
		cv.animate_timeout = cur[1].id;
		cv.animate_cur = new CursorId( cur.id + 2);

		SetCursorSprite(sprite);
		*/
	}

	static void CursorTick()
	{
		if (_cursor.animate_timeout != 0 && --_cursor.animate_timeout == 0)
			SwitchAnimatedCursor();
	}

	static void SetMouseCursor(CursorID cursor)
	{
		// Turn off animation
		_cursor.animate_timeout = 0;
		// Set cursor
		SetCursorSprite(cursor);
	}

	static void SetAnimatedMouseCursor( AnimCursor[] animcursors)
	{
		_cursor.animate_list = animcursors;
		_cursor.animate_pos = 0;
		SwitchAnimatedCursor();
	}

	static boolean ChangeResInGame(int w, int h)
	{
		return
				(_screen.width == w && _screen.height == h) ||
				change_resolution(w, h);
	}

	void ToggleFullScreen(boolean fs) {toggle_fullscreen(fs);}
	/*
static int  compare_res(const void *pa, const void *pb)
{
	int x = ((const uint16*)pa)[0] - ((const uint16*)pb)[0];
	if (x != 0) return x;
	return ((const uint16*)pa)[1] - ((const uint16*)pb)[1];
}

void SortResolutions(int count)
{
	qsort(_resolutions, count, sizeof(_resolutions[0]), compare_res);
}
	 */
	int GetDrawStringPlayerColor(PlayerID player)
	{
		// Get the color for DrawString-subroutines which matches the color
		//  of the player
		if (player.id == Owner.OWNER_SPECTATOR || player.id == Owner.OWNER_SPECTATOR - 1) return 1;
		return (_color_list[Global._player_colors[player.id]].window_color_1b) | IS_PALETTE_COLOR;
	}

	
	void CSleep(int milliseconds)
	{
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public abstract void ShowOSErrorBox(String buf);
	
	public static int Random() {		
		//return Math.random() * Integer.MAX_VALUE;
		return (int) (Math.random() * 0xFFFF);
	}


	static int InteractiveRandom()
	{
		int t = Global._random_seeds[1][1];
		int s = Global._random_seeds[1][0];
		Global._random_seeds[1][0] = s + BitOps.ROR(t ^ 0x1234567F, 7) + 1;
		return Global._random_seeds[1][1] = BitOps.ROR(s, 3) - 1;
	}

	static int InteractiveRandomRange(int max)
	{
		return BitOps.GB(InteractiveRandom(), 0, 16) * max >> 16;
	}
	
	
	public static void ShowInfo(String help) {
		System.err.println(help);
	}
	
	public static int RandomRange(int max) {
		return Math.abs( BitOps.GB(Random(), 0, 16) * max >> 16 );
	}

	public static TileIndex RandomTile() {
		return TileIndex.RandomTile();
	}


}



class DrawPixelInfo {
	//Pixel dst_ptr;
	int [] dst_ptr; // image buffer - (green << 24) | (red << 16) | blue
	int dst_ptr_shift; // add to dst_ptr index when accessing [dz] to work around absence of pointers
	
	int left, top, width, height;
	int pitch;
	int zoom;
	
	public void assignFrom(DrawPixelInfo dpi) 
	{
		dst_ptr = dpi.dst_ptr; // image buffer - (green << 24) | (red << 16) | blue
		left = dpi.left; 
		top = dpi.top; 
		width = dpi.width; 
		height = dpi.height;
		pitch = dpi.pitch;
		zoom = dpi.zoom;
	}
}

class CursorVars {
	Point pos, size, offs, delta;
	Point draw_pos, draw_size;
	CursorID sprite;

	int wheel; // mouse wheel movement
	AnimCursor[] animate_list;
	int animate_pos;
	int animate_timeout;

	boolean visible;
	boolean dirty;
	boolean fix_at;
} 

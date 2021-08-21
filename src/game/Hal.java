package game;

import game.enums.Owner;
import game.ids.CursorID;
import game.ids.PlayerID;
import game.struct.Rect;
import game.struct.Textbuf;
import game.util.AnimCursor;
import game.util.BitOps;
import game.xui.CursorVars;
import game.xui.DrawPixelInfo;
import game.xui.Gfx;

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



	public static void MarkWholeScreenDirty()
	{
		Gfx.SetDirtyBlocks(0, 0, _screen.width, _screen.height);
	}




	public static void CursorTick()
	{
		_cursor.tick();
	}

	public static void SetMouseCursor(CursorID cursor)
	{
		_cursor.setCursor(cursor);
	}

	public static void SetAnimatedMouseCursor( AnimCursor[] animcursors)
	{
		_cursor.setCursor(animcursors);
	}

	static boolean ChangeResInGame(int w, int h)
	{
		/*
		return
				(_screen.width == w && _screen.height == h) ||
				change_resolution(w, h);
		*/
		return false;
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
		return (Global._color_list[Global.gs._player_colors[player.id]].window_color_1b) | Gfx.IS_PALETTE_COLOR;
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
		return (int) (Math.random() * Integer.MAX_VALUE);
		//return (int) (Math.random() * 0xFFFF);
	}


	public static int InteractiveRandom()
	{
		int t = Global._random_seeds[1][1];
		int s = Global._random_seeds[1][0];
		Global._random_seeds[1][0] = s + BitOps.ROR32(t ^ 0x1234567F, 7) + 1;
		return Global._random_seeds[1][1] = BitOps.ROR32(s, 3) - 1;
	}

	public static int InteractiveRandomRange(int max)
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
	
	public static boolean InsertTextBufferClipboard(Textbuf text) {
		Global.error("InsertTextBufferClipboard");
		return false;
	}
	public static int getScreenWidth() {		return _screen.width;	}
	public static int getScreenHeight() { return _screen.height;	}


}




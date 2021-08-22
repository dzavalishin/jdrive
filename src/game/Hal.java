package game;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
import game.xui.Window;

public class Hal
{
	MainWindow mw = null;
	private static byte[] screen; // TODO static
	
	
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
	
	
	public static int Random() {		
		return (int) (Math.random() * Integer.MAX_VALUE);
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


	
	public void start_video(String parm) 
	{
		JFrame frame = new JFrame("JTTD")
		{

			private static final long serialVersionUID = -2190948795509588568L;
			/*
            @Override
            public void paint(Graphics g) {
                Dimension d = getSize();
                Dimension m = getMaximumSize();
                boolean resize = d.width > m.width || d.height > m.height;
                d.width = Math.min(m.width, d.width);
                d.height = Math.min(m.height, d.height);
                if (resize) {
                    Point p = getLocation();
                    setVisible(false);
                    setSize(d);
                    setLocation(p);
                    setVisible(true);
                }

                super.paint(g);
            }
			 */
		};

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//frame.setUndecorated(true);
		//frame.setSize(600, 400);

		//screen = new int[2048*2048]; // TODO scr size!
		screen = new byte[2048*2048*4]; // TODO scr size!

		mw = new MainWindow(frame,screen);

		//frame.setSize(mw.getMapSizeX(), mw.getMapSizeY());
		frame.setSize(1280+30, 1024);

		Dimension maximumSize = new Dimension(2560, 850);
		frame.setMaximumSize(maximumSize);

		//frame.setIconImages(icons);
		frame.setLayout(new FlowLayout(FlowLayout.LEADING));
		//frame.add(new JLabel("--------------------"));

		frame.add(mw);
		//frame.add(new JLabel("--------------------"));

		//mw.setSize(MainWindow.WIDTH, MainWindow.HEIGHT);
		//mw.setMinimumSize(new Dimension(MainWindow.WIDTH, MainWindow.HEIGHT));
		//mw.setMaximumSize(new Dimension(MainWindow.WIDTH, MainWindow.HEIGHT));

		frame.setVisible(true);

		mw.updateLocation();

		_screen.init(MainWindow.WIDTH, MainWindow.HEIGHT, screen );		
		_cur_dpi = _screen;
	}

	public void stop_video() {
		// TODO Auto-generated method stub

	}

	public void make_dirty(int left, int top, int width, int height) {
		mw.repaint(left, top, width, height);
	}

	public void main_loop() {
		//MSG mesg;
		//uint32 next_tick = GetTickCount() + 30, cur_ticks;

		//_wnd.running = true;

		while(true) {
			try {
				Thread.sleep(10); // TODO 100 fps?
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (Global._exit_game) return;

			/*#if defined(_DEBUG)
				if (_wnd.has_focus && GetAsyncKeyState(VK_SHIFT) < 0) {
					if (
		#else */
			/*
			if (_wnd.has_focus && GetAsyncKeyState(VK_TAB) < 0) {
				// Disable speeding up game with ALT+TAB (if syskey is pressed, the
				//  real key is in the upper 16 bits (see WM_SYSKEYDOWN in WndProcGdi()) 
				if ((_pressed_key >> 16) & Window.WKC_TAB &&
						//#endif
						!_networking && _game_mode != GM_MENU)
					_fast_forward |= 2;
			} else if (_fast_forward & 2)
				_fast_forward = 0;

			cur_ticks = GetTickCount();
			if ((_fast_forward && !_pause) || cur_ticks > next_tick)
				next_tick = cur_ticks;

			if (cur_ticks == next_tick) {
				next_tick += 30;
				_ctrl_pressed = _wnd.has_focus && GetAsyncKeyState(VK_CONTROL)<0;
				_shift_pressed = _wnd.has_focus && GetAsyncKeyState(VK_SHIFT)<0;
				#ifdef _DEBUG
				_dbg_screen_rect = _wnd.has_focus && GetAsyncKeyState(VK_CAPITAL)<0;
				#endif

			 */
			//try {
				Main.GameLoop();
			/*} catch (Throwable e) {
				e.printStackTrace();
			}*/

			_cursor.setDelta(0,0);
			//Window._left_button_clicked = false;
			//Window._right_button_clicked = false;

			// TODO return me 
			if (Global._force_full_redraw)					
				MarkWholeScreenDirty();

			mw.flush();

			//_screen.dst_ptr = _wnd.buffer_bits;
			//try {
				Window.UpdateWindows();
			/*} catch (Throwable e) {
				e.printStackTrace();
			}*/

			checkPaletteAnim();

			/*} else {
				Sleep(1);
				GdiFlush();
				_screen.dst_ptr = _wnd.buffer_bits;
				DrawTextMessage();
				DrawMouseCursor();
			}*/
		}
	}

	private void checkPaletteAnim() 
	{
		if (Gfx._pal_last_dirty == -1)
			return;

		mw.flush();
	}

	public boolean change_resolution(int w, int h) {
		// TODO Auto-generated method stub
		return false;
	}

	public void ShowOSErrorBox(String buf) {
		JOptionPane.showMessageDialog(null, buf);
	}
	
	
}




package game;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import game.util.Pixel;

class JavaHal extends Hal
{
	MainWindow mw = null;
	private static byte[] screen; // TODO static
	
	@Override
	public void start_video(String parm) 
	{
		JFrame frame = new JFrame("JTTD")
		{
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
		
		_screen.dst_ptr = new Pixel( screen );
		_screen.height = MainWindow.HEIGHT;
		_screen.width = MainWindow.WIDTH;
		//_screen.pitch = MainWindow.WIDTH * 3;
		_screen.pitch = MainWindow.WIDTH * 1;
		_screen.left = 0;
		_screen.top = 0;
		_screen.zoom = 0;
		
	}

	@Override
	public void stop_video() {
		// TODO Auto-generated method stub

	}

	@Override
	public void make_dirty(int left, int top, int width, int height) {
		mw.repaint(left, top, width, height);
	}

	@Override
	public void main_loop() {
		//MSG mesg;
		//uint32 next_tick = GetTickCount() + 30, cur_ticks;

		//_wnd.running = true;

		while(true) {
			try {
				//Thread.sleep(100); // TODO 10 fps?
				Thread.sleep(10); // TODO 10 fps?
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			while (PeekMessage(&mesg, NULL, 0, 0, PM_REMOVE)) {
				InteractiveRandom(); // randomness
				TranslateMessage(&mesg);
				DispatchMessage(&mesg);
			}*/
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

				// determine which directional keys are down
				if (_wnd.has_focus) {
					_dirkeys =
							(GetAsyncKeyState(VK_LEFT) < 0 ? 1 : 0) +
							(GetAsyncKeyState(VK_UP) < 0 ? 2 : 0) +
							(GetAsyncKeyState(VK_RIGHT) < 0 ? 4 : 0) +
							(GetAsyncKeyState(VK_DOWN) < 0 ? 8 : 0);
				} else
					_dirkeys = 0;
				*/
				Main.GameLoop();
				_cursor.delta.x = _cursor.delta.y = 0;

				// TODO return me if (Global._force_full_redraw)					
					MarkWholeScreenDirty();

				mw.flush();
				
				//GdiFlush();
				//_screen.dst_ptr = _wnd.buffer_bits;
				Window.UpdateWindows();
				
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

	@Override
	public boolean change_resolution(int w, int h) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void ShowOSErrorBox(String buf) {
		JOptionPane.showMessageDialog(null, buf);
	}


}
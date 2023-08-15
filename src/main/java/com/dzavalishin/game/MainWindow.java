package com.dzavalishin.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.dzavalishin.util.Colour;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.Window;



public class MainWindow extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 7030596255463826051L;


	public static final int TICK_TIME = 10;
	public static final int TICKS_PER_SECOND = 1000 / TICK_TIME;

	public static final int WIDTH = 1280;
	public static final int HEIGHT = 800;


	private final Timer timer = new Timer(TICK_TIME, this);
	private final JFrame frame;
	private byte[] screen;
	//private Point myLocation;

	/*
	public static final java.awt.Font bigMessageFont;
	public static final java.awt.Font defaultMessageFont;

	static {
		bigMessageFont = new java.awt.Font(java.awt.Font.SERIF, java.awt.Font.PLAIN, 48);
		defaultMessageFont = new java.awt.Font(java.awt.Font.SERIF, java.awt.Font.PLAIN, 20 );
	}
	 */
	public void setScreen(byte[] screen2) {
		this.screen = screen2;
	}


	public MainWindow(JFrame frame, byte[] screen2) 
	{
		this.frame = frame;
		this.screen = screen2;

		//myLocation = getLocation();

		//setSize(WIDTH, HEIGHT);
		//setMinimumSize(new Dimension(WIDTH, HEIGHT));
		//setMaximumSize(new Dimension(WIDTH, HEIGHT));


		frame.setFocusTraversalKeysEnabled(false); // Enable Tab key to pass through to us

		frame.addKeyListener(new KeyListener() {		
			//this.addKeyListener(new KeyListener() {		
			@Override
			public void keyTyped(KeyEvent e) { /* is empty */ }

			@Override
			public void keyReleased(KeyEvent e) { processKey(e, false); e.consume(); }

			@Override
			public void keyPressed(KeyEvent e) { processKey(e, true); e.consume(); }
		});


		//frame.addMouseListener( new MouseListener() 
		this.addMouseListener( new MouseListener() 
		{

			@Override
			public void mouseReleased(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON1 ) Window._left_button_down = false;
				if( e.getButton() == MouseEvent.BUTTON3 ) Window._right_button_down = false;								
				e.consume();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON1 ) Window._left_button_down = true;

				if( e.getButton() == MouseEvent.BUTTON3 )
				{
					Window._right_button_down = true;
					Window._right_button_clicked = true; // yes, it is different - why?
				}
				e.consume();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				//if( e.getButton() == MouseEvent.BUTTON1 ) Window._left_button_clicked = true;
				//if( e.getButton() == MouseEvent.BUTTON2 ) Window._right_button_clicked = true;
				e.consume();
				//System.out.printf("click %s", Window._left_button_clicked );
			}

			@Override
			public void mouseExited(MouseEvent e) {
				Window._mouse_inside = false;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				Window._mouse_inside = true;
			}

		});

		//frame.addMouseMotionListener( new MouseMotionListener() 
		this.addMouseMotionListener( new MouseMotionListener() 
		{

			@Override
			public void mouseMoved(MouseEvent e) 
			{
				int x = e.getX(); 
				int y = e.getY();

				e.consume();				
				processMouse(x, y);

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX(); 
				int y = e.getY();

				e.consume();				
				processMouse(x, y);
			}
		});


		//frame.addMouseWheelListener( (e) -> {
		this.addMouseWheelListener( (e) -> {
			Hal._cursor.setWheel( e.getWheelRotation() );
			e.consume();
		});

		frame.setJMenuBar(getMenu());

		//frame.set
		requestFocus();

		timer.start();
	}


	private  JMenuBar getMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu gameMenu = new JMenu("Game");
		menuBar.add(gameMenu);

		/*
        JMenuItem menuItemConnect = new JMenuItem("Fast");
        menuItemConnect.addActionListener( e -> System.out.println("Connected") );
        connectionMenu.add(menuItemConnect);

        JMenuItem menuItemDisconnect = new JMenuItem("Disconnect");
        menuItemDisconnect.addActionListener(e -> System.out.println("Disconnected") );
        connectionMenu.add(menuItemDisconnect);
		 */
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menuItemExit.addActionListener( e -> Global._exit_game = true );
		// NB! Does not work per se - must process it manually below
		menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
		gameMenu.add(menuItemExit);



		JMenu viewMenu = new JMenu("View");
		menuBar.add(viewMenu);	        

		JMenuItem menuItemTranspBuildings = new JMenuItem("Transparent buildings");
		menuItemTranspBuildings.addActionListener( e -> 
		{ 
			Global._display_opt ^= Global.DO_TRANS_BUILDINGS;
			Hal.MarkWholeScreenDirty();
		});
		viewMenu.add(menuItemTranspBuildings);

		return menuBar;
	}


	public void updateLocation()
	{
		//myLocation = getLocation();
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	private void processKey(KeyEvent e, boolean pressed) 
	{
		Global._ctrl_pressed = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0; // _wnd.has_focus && GetAsyncKeyState(VK_CONTROL)<0;
		Global._shift_pressed = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
		Global._alt_pressed = (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;
		boolean meta_pressed = (e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0;// _wnd.has_focus && GetAsyncKeyState(VK_CAPITAL)<0;
		Gfx._dbg_screen_rect = meta_pressed; 

		int key = e.getKeyCode();

		boolean prev_ff = Global._fast_forward;

		if( key == KeyEvent.VK_F4 && Global._alt_pressed ) { Global._exit_game = true; return; }

		switch(key)
		{
		case KeyEvent.VK_LEFT:	modDirKeys(1, pressed); break; 
		case KeyEvent.VK_UP:	modDirKeys(2, pressed); break;
		case KeyEvent.VK_RIGHT:	modDirKeys(4, pressed); break;
		case KeyEvent.VK_DOWN:	modDirKeys(8, pressed); break;

		case KeyEvent.VK_TAB:
		{
			if(!pressed) Global._fast_forward = false;
			else
			{
				if(!(Global._ctrl_pressed || Global._alt_pressed || Global._shift_pressed || meta_pressed ))
					Global._fast_forward = true;
			}
		}
		}

		if( prev_ff != Global._fast_forward)
			Window.InvalidateWindow(Window.WC_MAIN_TOOLBAR, 0);

		if(!pressed) return;

		int fKey = 0; // func
		int aKey = 0; // ascii

		int c = e.getKeyChar(); 
		if( c >= ' ' && c <= 'z' )
		{
			aKey = c;
		}
		else
		{
			switch(key)
			{

			case KeyEvent.VK_SPACE:		fKey = Window.WKC_SPACE;	break;
			case KeyEvent.VK_BACK_SPACE:fKey = Window.WKC_BACKSPACE;	break;
			case KeyEvent.VK_INSERT:    fKey = Window.WKC_INSERT;	break;
			case KeyEvent.VK_DELETE:    fKey = Window.WKC_DELETE;	break;
			case KeyEvent.VK_ENTER:     fKey = Window.WKC_RETURN;	break;

			case KeyEvent.VK_TAB:       fKey = Window.WKC_TAB;	break;
			case KeyEvent.VK_PAUSE:     fKey = Window.WKC_PAUSE;	break;

			case KeyEvent.VK_ESCAPE:    fKey = Window.WKC_ESC;		break;

			case KeyEvent.VK_LEFT:      fKey = Window.WKC_LEFT;		break;
			case KeyEvent.VK_RIGHT:     fKey = Window.WKC_RIGHT;	break;
			case KeyEvent.VK_UP:        fKey = Window.WKC_UP;		break;
			case KeyEvent.VK_DOWN:      fKey = Window.WKC_DOWN;		break;

			case KeyEvent.VK_PAGE_UP:   fKey = Window.WKC_PAGEUP;	break;
			case KeyEvent.VK_PAGE_DOWN: fKey = Window.WKC_PAGEDOWN;	break;

			case KeyEvent.VK_DIVIDE:    fKey = Window.WKC_NUM_DIV;	break;
			case KeyEvent.VK_MULTIPLY:  fKey = Window.WKC_NUM_MUL;	break;
			case KeyEvent.VK_SUBTRACT:  fKey = Window.WKC_NUM_MINUS;	break;
			case KeyEvent.VK_ADD:       fKey = Window.WKC_NUM_PLUS;	break;

			case KeyEvent.VK_DECIMAL:   fKey = Window.WKC_NUM_DECIMAL;	break;


			case KeyEvent.VK_F1:	fKey = Window.WKC_F1;	break;
			case KeyEvent.VK_F2:	fKey = Window.WKC_F2;	break;
			case KeyEvent.VK_F3:	fKey = Window.WKC_F3;	break;
			case KeyEvent.VK_F4:	fKey = Window.WKC_F4;	break;
			case KeyEvent.VK_F5:	fKey = Window.WKC_F5;	break;
			case KeyEvent.VK_F6:	fKey = Window.WKC_F6;	break;
			case KeyEvent.VK_F7:	fKey = Window.WKC_F7;	break;
			case KeyEvent.VK_F8:	fKey = Window.WKC_F8;	break;
			case KeyEvent.VK_F9:	fKey = Window.WKC_F9;	break;
			case KeyEvent.VK_F10:	fKey = Window.WKC_F10;	break;
			case KeyEvent.VK_F11:	fKey = Window.WKC_F11;	break;
			case KeyEvent.VK_F12:	fKey = Window.WKC_F12;	break;

			default:
				if( key >= KeyEvent.VK_NUMPAD0 && key >= KeyEvent.VK_NUMPAD9 )
					fKey = Window.WKC_NUM_0 + key - KeyEvent.VK_NUMPAD0;

			}
		}

		int shifts = 0;

		if(0 != (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK)) shifts |= Window.WKC_SHIFT;
		if(0 != (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK))  shifts |= Window.WKC_CTRL;
		if(0 != (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK))   shifts |= Window.WKC_ALT;

		Global._pressed_key = fKey << 16 | (aKey & 0xFFFF) | (shifts << 16);
		//System.out.printf("k %x\n", Global._pressed_key );
	}




	private void modDirKeys(int i, boolean pressed) {
		if( pressed )	Global._dirkeys |= i;
		else			Global._dirkeys &= ~i;		
	}

	static int startX = 0;

	@Override
	public void paint(Graphics g) 
	{
		//Dimension d = getSize();
		g.setColor(Color.darkGray);
		//setBackground(Color.black);
		//g.clearRect(0, 0, frame.getWidth(), frame.getHeight());
		g.fillRect(0, 0, WIDTH, HEIGHT);

		//TODO DebugDisplay.paint(g);
		/*
		ColorModel cm = ColorModel.getRGBdefault();
		//final int[] pixels = new int[width * height]; // 0xAARRGGBB
		MemoryImageSource source = new MemoryImageSource(WIDTH, HEIGHT, cm, screen, 0, WIDTH);
		//source.setAnimated(true);
		//source.setFullBufferUpdates(true);
		Image image = Toolkit.getDefaultToolkit().createImage(source);
		image.setAccelerationPriority(1f);
		//System.err.print( image.getWidth(null)+" " );
		source.newPixels();
		//source.

		g.drawImage(image, 0, 0, getBackground(), null);
		source.newPixels();
		 */


		//BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);

		//image.setRGB(0, 0, WIDTH, HEIGHT, screen, 0, WIDTH);






		/*

		DataBuffer db = new DataBufferByte(screen, screen.length);
		//WritableRaster wr = Raster.createBandedRaster(TICKS_PER_SECOND, WIDTH, WIDTH, HEIGHT, getLocation());


		int [] off = {0,1,2,3};
		/*ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE, WIDTH, HEIGHT,
                4, // int pixelStride,
                WIDTH*4, // int scanlineStride,
                off//int[] bandOffsets
                );* /

		//WritableRaster wr = WritableRaster.createRaster(sm, db, null);
		//WritableRaster wr = new WritableRaster(sm, db, null);
		WritableRaster	wr = Raster.createInterleavedRaster(db, WIDTH, HEIGHT,
				WIDTH*4, //int scanlineStride, 
				4, //int pixelStride, 
				off, //int[] bandOffsets, 
				null //Point location
				);



		ColorModel cm = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false, 
				Transparency.OPAQUE, DataBuffer.TYPE_BYTE );

		BufferedImage image = new BufferedImage(cm, wr, false, null);

		 */
		BufferedImage image;

		if(Gfx._pal_last_dirty != -1)
			makePalette();

		//int height = Hal._screen.height; // HEIGHT
		//int width  = Hal._screen.width; // WIDTH
		int height = HEIGHT;
		int width  = WIDTH;

		//BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		if(icm!=null)
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);
		else
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);

		//ColorModel cm = image.getColorModel();
		//IndexColorModel icm = (IndexColorModel) cm;

		image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(screen, screen.length), new java.awt.Point(0,0) ) );

		g.drawImage(image, 0, 0, getBackground(), null);
		//System.err.print( image.getWidth(null)+" " );

	}



	/**
	 * 
	 * Called on timer tick, does game step.
	 * 
	 */

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		//flush(); 
		//repaint();
		//frame.repaint();

	}




	public void flush() {
		repaint();		
		frame.repaint();
	}

	static final int PALETTE_SIZE = 256;
	private static IndexColorModel icm = null; 
	public void makePalette()
	{
		/*
		byte[] rp = new byte[PALETTE_SIZE];
		byte[] gp = new byte[PALETTE_SIZE];
		byte[] bp = new byte[PALETTE_SIZE];
		byte[] ap = new byte[PALETTE_SIZE];

		if(Gfx._cur_palette == null || Gfx._cur_palette[0] == null) return;

		for(int i = 0; i < PALETTE_SIZE; i++)
		{
			ap[i] = (byte) 0xFF;
			rp[i] = Gfx._cur_palette[i].r;
			gp[i] = Gfx._cur_palette[i].g;
			bp[i] = Gfx._cur_palette[i].b;
		}

		icm = new IndexColorModel(8, PALETTE_SIZE, rp, gp, bp, ap);
		*/
		if(Gfx._cur_palette == null || Gfx._cur_palette[0] == null) return;
		icm = makePalette(Gfx._cur_palette);
		Gfx._pal_last_dirty = -1;
	}

	public static IndexColorModel makePalette(Colour [] palette)
	{
		int size = palette.length;
		
		byte[] rp = new byte[size];
		byte[] gp = new byte[size];
		byte[] bp = new byte[size];
		byte[] ap = new byte[size];

		if(palette == null || palette[0] == null) return null;

		for(int i = 0; i < size; i++)
		{
			ap[i] = (byte) 0xFF;
			rp[i] = palette[i].r;
			gp[i] = palette[i].g;
			bp[i] = palette[i].b;
		}

		return new IndexColorModel(8, PALETTE_SIZE, rp, gp, bp, ap);
	}
	

	private void processMouse(int x, int y) 
	{
		Hal._cursor.processMouse(x, y);
	}


	static void setLookAndFeel()
	{
		try {
			// Set System L&F
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Throwable e) {
			// Ignore
		}
	}



}
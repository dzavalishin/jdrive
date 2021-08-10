package game;


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
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;



public class MainWindow extends JPanel implements ActionListener
{

	public static final int TICK_TIME = 20;
	public static final int TICKS_PER_SECOND = 1000 / TICK_TIME;

	public static final int WIDTH = 1280;
	public static final int HEIGHT = 1024;


	private Timer timer = new Timer(TICK_TIME, this);	
	private JFrame frame;
	private byte[] screen;


	/*
	public static final java.awt.Font bigMessageFont;
	public static final java.awt.Font defaultMessageFont;

	static {
		bigMessageFont = new java.awt.Font(java.awt.Font.SERIF, java.awt.Font.PLAIN, 48);
		defaultMessageFont = new java.awt.Font(java.awt.Font.SERIF, java.awt.Font.PLAIN, 20 );
	}
	 */
	public MainWindow(JFrame frame, byte[] screen2) 
	{
		this.frame = frame;
		this.screen = screen2;


		//setSize(WIDTH, HEIGHT);
		//setMinimumSize(new Dimension(WIDTH, HEIGHT));
		//setMaximumSize(new Dimension(WIDTH, HEIGHT));


		frame.addKeyListener(new KeyListener() {		
			@Override
			public void keyTyped(KeyEvent e) { }

			@Override
			public void keyReleased(KeyEvent e) { processKey(e, false); }

			@Override
			public void keyPressed(KeyEvent e) { processKey(e, true); }
		});


		frame.addMouseListener( new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON1 ) Window._left_button_down = false;
				if( e.getButton() == MouseEvent.BUTTON2 ) Window._right_button_down = false;								
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON1 ) Window._left_button_down = true;
				if( e.getButton() == MouseEvent.BUTTON2 ) Window._right_button_down = true;								
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON1 ) Window._left_button_clicked = true;
				if( e.getButton() == MouseEvent.BUTTON2 ) Window._right_button_clicked = true;				
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

		});

		frame.addMouseMotionListener( new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) 
			{
				int x = e.getX(); 
				int y = e.getY();

				if (Hal._cursor.fix_at) {
					int dx = x - Hal._cursor.pos.x;
					int dy = y - Hal._cursor.pos.y;
					if (dx != 0 || dy != 0) {
						Hal._cursor.delta.x += dx;
						Hal._cursor.delta.y += dy;

						/* TODO set cursor pos
						pt.x = _cursor.pos.x;
						pt.y = _cursor.pos.y;

						if (_wnd.double_size) {
							pt.x *= 2;
							pt.y *= 2;
						}
						ClientToScreen(hwnd, &pt);
						SetCursorPos(pt.x, pt.y);
						 */
					}
				} else {
					Hal._cursor.delta.x += x - Hal._cursor.pos.x;
					Hal._cursor.delta.y += y - Hal._cursor.pos.y;
					Hal._cursor.pos.x = x;
					Hal._cursor.pos.y = y;
					Hal._cursor.dirty = true;
				}

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		timer.start();
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
		Gfx._dbg_screen_rect = (e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0;// _wnd.has_focus && GetAsyncKeyState(VK_CAPITAL)<0;

		Global._dirkeys = (byte)
				(((e.getKeyCode() == KeyEvent.VK_LEFT) ? 1 : 0) +
						((e.getKeyCode() == KeyEvent.VK_UP)  ? 2 : 0) +
						((e.getKeyCode() == KeyEvent.VK_RIGHT)  ? 4 : 0) +
						((e.getKeyCode() == KeyEvent.VK_DOWN)  ? 8 : 0));


		switch(e.getKeyCode())
		{
		case KeyEvent.VK_SHIFT: 
			//setFastMode(pressed); 
			break;

		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			//ego.goUp(pressed);
			break;

		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			//ego.goDown(pressed);
			break;

		}

		if(!pressed) return;

		if( (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 )
		{
			switch(e.getKeyCode())
			{
			/*
			case KeyEvent.VK_W: ego.giveAllWeapon();   break;

			case KeyEvent.VK_F1: DebugDisplay.togglePaintHitBox();   break;
			case KeyEvent.VK_F2: DebugDisplay.togglePaintTileMesh(); break;
			case KeyEvent.VK_F3: DebugDisplay.togglePaintPath();     break;
			case KeyEvent.VK_F4: debugDark = !debugDark;     break;

			case KeyEvent.VK_F9: LevelData.currentLevel = 0; restartLevel();     break;
			case KeyEvent.VK_F12:   s.setWinFlag();			break;
			 */
			}

			return;
		}

		{
			int c = e.getKeyCode();
			if( c > KeyEvent.VK_A && c < KeyEvent.VK_Z )
			{
				Global._pressed_key = c - KeyEvent.VK_A + 'A';
				return;
			}

			if( c > KeyEvent.VK_0 && c < KeyEvent.VK_9 )
			{
				Global._pressed_key = c - KeyEvent.VK_0 + '0';
				return;
			}
		}

		// Just key press
		switch(e.getKeyCode())
		{
		case KeyEvent.VK_D:
			break;

		case KeyEvent.VK_A:
			break;

		case KeyEvent.VK_SPACE:		Global._pressed_key = Window.WKC_SPACE;	break;
		case KeyEvent.VK_BACK_SPACE:			Global._pressed_key = Window.WKC_BACKSPACE;	break;
		case KeyEvent.VK_INSERT: Global._pressed_key = Window.WKC_INSERT;	break;
		case KeyEvent.VK_DELETE: Global._pressed_key = Window.WKC_DELETE;	break;
		case KeyEvent.VK_ENTER: Global._pressed_key = Window.WKC_RETURN;	break;

		case KeyEvent.VK_OPEN_BRACKET:
			break;

		case KeyEvent.VK_CLOSE_BRACKET:
			break;

		case KeyEvent.VK_TAB: Global._pressed_key = Window.WKC_TAB;	break;
		case KeyEvent.VK_PAUSE: Global._pressed_key = Window.WKC_PAUSE;	break;

		case KeyEvent.VK_ESCAPE: Global._pressed_key = Window.WKC_ESC;	break;

		case KeyEvent.VK_F1:	Global._pressed_key = Window.WKC_F1;	break;
		case KeyEvent.VK_F2:	Global._pressed_key = Window.WKC_F2;	break;
		case KeyEvent.VK_F3:	Global._pressed_key = Window.WKC_F3;	break;
		case KeyEvent.VK_F4:	Global._pressed_key = Window.WKC_F4;	break;
		case KeyEvent.VK_F5:	Global._pressed_key = Window.WKC_F5;	break;
		case KeyEvent.VK_F6:	Global._pressed_key = Window.WKC_F6;	break;
		case KeyEvent.VK_F7:	Global._pressed_key = Window.WKC_F7;	break;
		case KeyEvent.VK_F8:	Global._pressed_key = Window.WKC_F8;	break;
		case KeyEvent.VK_F9:	Global._pressed_key = Window.WKC_F9;	break;
		case KeyEvent.VK_F10:	Global._pressed_key = Window.WKC_F10;	break;
		case KeyEvent.VK_F11:	Global._pressed_key = Window.WKC_F11;	break;
		case KeyEvent.VK_F12:	Global._pressed_key = Window.WKC_F11;	break;


		}

	}

	/* TODO keys
	 * 
	 * 	AM(VK_NUMPAD0,VK_NUMPAD9, WKC_NUM_0, WKC_NUM_9),
	AS(VK_DIVIDE,			WKC_NUM_DIV),
	AS(VK_MULTIPLY,		WKC_NUM_MUL),
	AS(VK_SUBTRACT,		WKC_NUM_MINUS),
	AS(VK_ADD,				WKC_NUM_PLUS),
	AS(VK_DECIMAL,		WKC_NUM_DECIMAL)

	 * 
	AM(VK_PRIOR,VK_DOWN, WKC_PAGEUP, WKC_DOWN),
	 * 
	 */





	static int startX = 0;

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
		/*
		for(int i = 0; i < 100; i++)
			screen[5000+i+startX] = (byte) 0xFF;
		startX++;
		 */
		BufferedImage image;
		
		if(Gfx._pal_last_dirty != -1)
			makePalette();

		//BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		if(icm!=null)
			image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, icm);
		else
			image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_INDEXED);

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
		//if( Math.random() < 0.05 ) RandomImageParticle.emitGlassJunkForTile(s.getOverlay(), 100, 200, 100);


		//repaint();
		//frame.repaint();


		//processTimerStop();
	}




	public void flush() {
		repaint();		
		frame.repaint();
	}

	static final int PALETTE_SIZE = 256;
	private static IndexColorModel icm = null; 
	public void makePalette()
	{
		byte[] rp = new byte[PALETTE_SIZE];
		byte[] gp = new byte[PALETTE_SIZE];
		byte[] bp = new byte[PALETTE_SIZE];
		byte[] ap = new byte[PALETTE_SIZE];
		/*
		java.util.Arrays.fill(ap, (byte) 255);
		java.util.Arrays.fill(rp, (byte) 255);
		java.util.Arrays.fill(gp, (byte) 255);
		java.util.Arrays.fill(bp, (byte) 255);
		//transparent
		rp[0] = gp[0] = bp[0] = ap[0] = 0;
		*/
		
		if(Gfx._cur_palette == null || Gfx._cur_palette[0] == null) return;
		
		for(int i = 0; i < PALETTE_SIZE; i++)
		{
			ap[i] = (byte) 0xFF;
			rp[i] = Gfx._cur_palette[i].r;
			gp[i] = Gfx._cur_palette[i].g;
			bp[i] = Gfx._cur_palette[i].b;
		}
				
		icm = new IndexColorModel(8, PALETTE_SIZE, rp, gp, bp, ap);
		Gfx._pal_last_dirty = -1;
	}



}
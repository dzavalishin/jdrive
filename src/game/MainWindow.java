package game;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;



public class MainWindow extends JPanel implements ActionListener{

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


		// Just key press
		switch(e.getKeyCode())
		{
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			//if(canControl) ego.setXSpeed( 1 ); 
			break;

		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			//if(canControl) ego.setXSpeed( -1 ); 
			break;

		case KeyEvent.VK_SPACE:
			//if(canControl) ego.shootBullet(); 
			break;

		case KeyEvent.VK_ENTER:
			//if(canControl) egoAction();
			break;

		case KeyEvent.VK_R:
			//if(canControl) ego.shootMissile();
			break;

		case KeyEvent.VK_G:
			//if(canControl) ego.shootBFG();
			break;

		case KeyEvent.VK_B:
			//if(canControl) ego.dropBomb();
			break;

		case KeyEvent.VK_OPEN_BRACKET:
			break;

		case KeyEvent.VK_CLOSE_BRACKET:
			break;

		case KeyEvent.VK_P:
			break;

		case KeyEvent.VK_ESCAPE:
			break;

		case KeyEvent.VK_F1:
			break;

		case KeyEvent.VK_PAUSE:
		case KeyEvent.VK_F2:
			break;

		case KeyEvent.VK_F3:
			break;

		case KeyEvent.VK_F9:
			break;

		case KeyEvent.VK_F11:
			/*
			if( (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0 )
				LevelData.currentLevel -= 10;
			else
				LevelData.currentLevel--;
			if( LevelData.currentLevel < 0 )
				LevelData.currentLevel = 0;
			restartLevel();
			*/     
			break;

		case KeyEvent.VK_F12:
			/*
			if( (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0 )
				LevelData.currentLevel += 10;
			else
				LevelData.currentLevel++;
			//if( LevelData.currentLevel < 0 )				LevelData.currentLevel = 0;
			restartLevel();
			*/     
			break;

		}

	}









	public void paint(Graphics g) 
	{
		//Dimension d = getSize();
		g.setColor(Color.black);
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

		
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(screen, screen.length), new java.awt.Point(0,0) ) );

		g.drawImage(image, 0, 0, getBackground(), null);

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


		repaint();


		//processTimerStop();
	}




	public void flush() {
		repaint();		
	}





}
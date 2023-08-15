package com.dzavalishin.game;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.dzavalishin.exceptions.InvalidSpriteFormat;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.Gfx;

public class SingleSprite 
{

	private boolean hasRGB;
	private boolean hasAlpha;
	private boolean hasPalette;
	private boolean hasTransparency;
	private boolean exactSize;

	private int zoomLevel = 0;

	private int ySize;
	private int xSize;

	private int xOffset;
	private int yOffset;

	private int uncompressedSize = -1;
	private int bpp;
	
	private BufferedImage palImage; // decoded image in palette based format
	private BufferedImage rgbImage; // decoded image in RGBA format

	/**
	 * Parse sprite out of NewGRF file.
	 * 
	 * @param type Sprite type (TODO remove from parameters, load from data array)
	 * @param spriteData Part of file representing sprite
	 * @throws InvalidSpriteFormat If can't decode
	 */
	public SingleSprite(int type, byte[] spriteData) throws InvalidSpriteFormat 
	{
		if( type == 0xFF )
		{
			//Global.error("Non-sprite in SingleSprite c'tor");
			//return;
			throw new InvalidSpriteFormat("Non-sprite in SingleSprite c'tor");
		}

		hasRGB			= BitOps.HASBIT(type, 0);
		hasAlpha		= BitOps.HASBIT(type, 1);
		hasPalette		= BitOps.HASBIT(type, 2);
		hasTransparency = BitOps.HASBIT(type, 3);
		exactSize		= BitOps.HASBIT(type, 6);

		bpp = 0;

		if(hasRGB) bpp += 3;
		if(hasAlpha) bpp += 1;
		if(hasPalette) bpp += 1;

		ByteBuffer bb = ByteBuffer.wrap(spriteData);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		bb.get(); // skip type
		zoomLevel = 0xFF & bb.get();

		ySize = bb.getShort();
		xSize = bb.getShort();

		xOffset = bb.getShort(); 
		yOffset = bb.getShort();

		Global.debug("%s",toString());

		if(hasTransparency)
		{
			uncompressedSize = bb.getInt();

			byte [] decompData = new byte[uncompressedSize];
			//Arrays.fill(decompData, (byte)0);

			decompress(decompData,bb);

			ByteBuffer bb2 = ByteBuffer.wrap(decompData);
			bb2.order(ByteOrder.LITTLE_ENDIAN);


			int imageSize = xSize * ySize * bpp;
			byte [] image = new byte[imageSize];


			decodeTile(image, bb2);
			convertImageArray(image);
		}
		else
		{
			uncompressedSize = xSize * ySize * bpp;
			
			int imageSize = xSize * ySize * bpp;
			byte [] image = new byte[imageSize];
			//bb.get(image);
			decompress(image,bb);
			convertImageArray(image);
		}

	}


	/**
	 * Create self from SingleSprite of different zoom level
	 * @param src SingleSprite to use as source
	 * @param newZoomLevel Zoom level for me.
	 */
	public SingleSprite(SingleSprite src, int newZoomLevel) {
		// TODO Auto-generated constructor stub
	}


	private void decodeTile(byte[] decompData, ByteBuffer bb) {
		boolean dwords = uncompressedSize >= 65536;

		int [] offsets = new int[ySize];

		int start = bb.position();

		//int mayBeCount = bb.get();

		for(int i = 0; i < ySize; i++ )
		{
			if(dwords)
				offsets[i] = bb.getInt();
			else
				offsets[i] = /*dwords ? bb.getInt() :*/ bb.getShort() & 0xFFFF;
			//offsets[i] = /*dwords ? bb.getInt() :*/ bb.get() & 0xFF;
		}

		for(int i = 0; i < ySize; i++ )
		{
			bb.position(offsets[i] + start);
			//bb.position(offsets[i]-1);
			decodeLine( i, decompData, bb);
		}		

	}

	private void decodeLine(int i, byte[] decompData, ByteBuffer bb) {
		boolean words = xSize >= 256;

		int lineStart = bpp * i * xSize; 

		while(true)
		{
			int cinfo = words ? (0xFFFF & bb.getShort()) : (0xFF & bb.get());
			int coffs = words ? (0xFFFF & bb.getShort()) : (0xFF & bb.get());

			boolean last = 0 != ( cinfo & (words ? 0x8000 : 0x80) );

			if(words) cinfo &= 0x7FFF;
			else cinfo &= 0x7F;

			assert cinfo <= xSize;

			cinfo *= bpp;
			coffs *= bpp;

			byte [] src = new byte[cinfo];
			bb.get(src);

			System.arraycopy(src, 0, decompData, lineStart+coffs, cinfo);

			if( last ) return;
		}
	}


	@Override
	public String toString() {		
		return String.format("SingleSprite %s (has %s %s %s) %d.%d zoom %d",
				hasTransparency ? " IsTile" : "NotTile",
						hasRGB? "RGB" : "",
								hasAlpha ? "Alpha" : "",
										hasPalette? "Palette" : "",
												xSize, ySize, zoomLevel
				);
	}

	/**
	 * Extract ARGB and palette images from given byte array
	 * @param image
	 */
	private void convertImageArray(byte[] image) 
	{
		// Q'n'D display
		if( hasPalette && !hasRGB && !hasAlpha )
		{
			palImage = createBufferedImage(image, xSize, ySize);
			//DisplayImage(palImage); // TODO make scrollable sprite viewer
			// TODO generate an RGBA image out of palette one
		}
		else
		{
			assert hasRGB;
			
			int pixels = xSize * ySize;
			
			byte [] palImageBuf = null;
			byte [] rgbImageBuf = new byte[pixels * 4]; // allways alpha
			
			if( hasPalette ) palImageBuf = new byte[pixels]; 
			
			int rgbp = 0;
			int palp = 0;
			int srcp = 0;
			
			for(int i = 0; i < pixels; i++)
			{
				rgbImageBuf[rgbp++] = image[srcp++]; // R
				rgbImageBuf[rgbp++] = image[srcp++]; // G
				rgbImageBuf[rgbp++] = image[srcp++]; // B
				
				int a = 0xFF;
				if(hasAlpha) a = image[srcp++];
				rgbImageBuf[rgbp++] = (byte) a;
				
				if(hasPalette)
					palImageBuf[palp++] = image[srcp++];
			}
			
			if(hasPalette)
				palImage = createBufferedImage(palImageBuf, xSize, ySize);
			
				rgbImage = createRgbaImage(rgbImageBuf, xSize, ySize);
		}
		
	}

	private static BufferedImage createRgbaImage(byte[] imageBuf, int width, int height) {
	    // Convert RGBA byte array to Image - TODO use samplesPerPixel/bandOffsets to extract rgba from rgb+palatte data?
		// like samplesPerPixel = 5; bandOffsets = {0,1,2,3}; // RGBA order, M skipped
	    int samplesPerPixel = 4;
	    int[] bandOffsets = {0,1,2,3}; // RGBA order - [dz] or BRGA? Why 'bgraPixelData' name?

	    //byte[] bgraPixelData = new byte[width * height * samplesPerPixel];

	    DataBuffer buffer = new DataBufferByte(imageBuf, imageBuf.length);
	    WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, samplesPerPixel * width, samplesPerPixel, bandOffsets, null);

	    ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

	    BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

	    //System.out.println("image: " + image); // Should print: image: BufferedImage@<hash>: type = 0 ...
	    return image;
	}


	/**
	 * Create Image for indexed color picture byte array
	 * @param pixels
	 * @param width
	 * @param height
	 * @return
	 */
	private BufferedImage createBufferedImage(byte[] pixels, int width, int height) {
		SampleModel sm = getIndexSampleModel(width, height);
		DataBuffer db = new DataBufferByte(pixels, width*height, 0);
		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		IndexColorModel cm = getDefaultColorModel();
		BufferedImage image = new BufferedImage(cm, raster, false, null);
		return image;
	}	

	private SampleModel getIndexSampleModel(int width, int height) {
		IndexColorModel icm = getDefaultColorModel();
		WritableRaster wr = icm.createCompatibleWritableRaster(1, 1);
		SampleModel sampleModel = wr.getSampleModel();
		sampleModel = sampleModel.createCompatibleSampleModel(width, height);
		return sampleModel;
	}

	private IndexColorModel getDefaultColorModel() {
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];

		for(int i=0; i<256; i++) {
			r[i]=(byte)i;
			g[i]=(byte)i;
			b[i]=(byte)i;
		}

		if(Gfx._cur_palette != null && Gfx._cur_palette[0] != null) 
		{
			for(int i = 0; i < 256; i++)
			{
				//ap[i] = (byte) 0xFF;
				r[i] = Gfx._cur_palette[i].r;
				g[i] = Gfx._cur_palette[i].g;
				b[i] = Gfx._cur_palette[i].b;
			}
		}

		IndexColorModel defaultColorModel = new IndexColorModel(8, 256, r, g, b);
		return defaultColorModel;
	}	


	public void DisplayImage(BufferedImage img)
	{
		Image dimg = img.getScaledInstance(xSize*4, ySize*4, Image.SCALE_SMOOTH);
		
		ImageIcon icon=new ImageIcon(dimg);
		
		JPanel p = getFramePanel();
		
		JLabel lbl=new JLabel();
		lbl.setIcon(icon);
		//lbl.setSize(xSize*2, ySize*2);
		
		p.add(lbl);
		//frame.setVisible(true);
		frame.pack();
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	private static JFrame frame = null;
	private static JPanel panel = null;
	
	private JPanel getFramePanel() {
		if(frame == null)
			{		
			frame = new JFrame();
			frame.setLayout(new FlowLayout());
			frame.setSize(200,300);
			frame.setVisible(true);
			
			panel = new JPanel();
			JScrollPane scrPane = new JScrollPane(panel);
			
			frame.add(scrPane);
			}
		return panel;
	}	

	/**
	 * <h1>Compression algorithm</h1>

   <p>The compression used is a variation on the LZ77 algorithm which
   detects redundancy and losslessly reduces the size of the data. Here's
   how the compressed data looks in a GRF file.

   <p>The compressed stream contains either a pointer to an earlier location
   and a length, which means that these bytes are copied over from the
   given location, or it contains a length and a verbatim chunk which is
   copied to the output stream.

   <pre>
   BYTE code
          The high bit of the code shows whether this is a verbatim chunk
          (not set) or a repetition of earlier data (set).
   </pre>

   <p>The meaning of the following bytes depends on whether the high bit of
   code is set.

   <p>If the high bit is not set, what follows is code&0x7f bytes of
   verbatim data.

   <p>If the high bit is set, the code has a slightly different meaning.
   Bits 3 to 7 are now three bits to a length value, stating how much
   data should be copied from the earlier location. Bits 0 to 2 are the
   high bits of an offset, with the low bits being in the next byte.

   <pre>
   BYTE lofs
          Low bits of the offset
   </pre>

   <p>Use this to extract length and offset:

   <pre>
   unsigned long length = -(code >> 3);
   unsigned long offset = ( (code & 7) << 8 ) | lofs;
   </pre>

   <p>It's important that the variables are unsigned and at least two bytes
   large.

   <p>The offset is counted backwards from the current location. So you
   subtract the offset from your position in the output stream and copy
   the given number of bytes.

	 * @param decompData
	 * @param bb
	 */

	private void decompress(byte[] decompData, ByteBuffer bb) 
	{		
		ByteBuffer to = ByteBuffer.wrap(decompData);

		while(bb.hasRemaining())
		{
			int code = bb.get();// & 0xFF;

			//boolean repeat = 0 != (code & 0x80);
			//code &= 0x7F;

			if( code >= 0 ) //!repeat)
			{
				final int len = (code == 0) ? 0x80 : code; // Undocumented
				byte [] copy = new byte[len];
				bb.get(copy);
				to.put(copy);
				continue;
			}

			// copy from old data

			int lofs = bb.get() & 0xFF;

			int length = -(code >> 3); // code >> 3;
			int offset = ( (code & 7) << 8 ) | lofs;

			int toPos = to.position();
			to.position(toPos - offset);

			byte [] copy = new byte[length];
			to.get(copy);
			to.position(toPos);
			to.put(copy);
		}
		
		int topos = to.position();
		if(topos != decompData.length)
			Global.error("SingleSprite.decompress() topos %d, decompData.length %d ", topos, decompData.length );
	}


	
	// -------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------
	
	public boolean isAlpha() {		return hasAlpha;	}
	public boolean isPalette() {		return hasPalette;	}

	public int getZoomLevel() {		return zoomLevel;	}

	public int getySize() {		return ySize;	}
	public int getxSize() {		return xSize;	}

	public int getxOffset() {		return xOffset;	}
	public int getyOffset() {		return yOffset;	}


	public BufferedImage getPalImage() {		return palImage;	}
	public BufferedImage getRgbImage() {		return rgbImage;	}

	public BufferedImage getImage() {		return rgbImage == null ? palImage : rgbImage ;	}

	
}

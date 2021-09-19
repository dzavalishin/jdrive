package game;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import game.exceptions.InvalidSpriteFormat;
import game.util.BitOps;

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
	private int pixelStride;

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
		
		pixelStride = 0;
		
		if(hasRGB) pixelStride += 3;
		if(hasAlpha) pixelStride += 1;
		if(hasPalette) pixelStride += 1;
		
		ByteBuffer bb = ByteBuffer.wrap(spriteData);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		zoomLevel = 0xFF & bb.get();

		ySize = bb.getShort();
		xSize = bb.getShort();

		xOffset = bb.getShort(); 
		yOffset = bb.getShort();
		
		Global.debug("%s",toString());
		
		if(hasTransparency)
		{
			uncompressedSize  = bb.getInt();
			
			byte [] decompData = new byte[uncompressedSize];
			//Arrays.fill(decompData, (byte)0);

			decompress(decompData,bb);

			ByteBuffer bb2 = ByteBuffer.wrap(decompData);
			bb2.order(ByteOrder.LITTLE_ENDIAN);

			
			int imageSize = xSize * ySize * pixelStride;
			byte [] image = new byte[imageSize];
			
			
			decodeTile(image, bb2);
			convertImageArray(image);
		}
		else
		{
			int imageSize = xSize * ySize * pixelStride;
			byte [] image = new byte[imageSize];
			//bb.get(image);
			decompress(image,bb);
			convertImageArray(image);
		}
	
	}


	private void decodeTile(byte[] decompData, ByteBuffer bb) {
		boolean dwords = uncompressedSize >= 65536;

		int [] offsets = new int[ySize];
		
		int start = bb.position();
		
		int mayBeCount = bb.get();
		
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

		int lineStart = pixelStride * i * xSize; 

		while(true)
		{
			int cinfo = words ? (0xFFFF & bb.getShort()) : (0xFF & bb.get());
			int coffs = words ? (0xFFFF & bb.getShort()) : (0xFF & bb.get());
			
			boolean last = 0 != ( cinfo & (words ? 0x8000 : 0x80) );
			
			if(words) cinfo &= 0x7FFF;
			else cinfo &= 0x7F;

			assert cinfo <= xSize;
			
			cinfo *= pixelStride;
			coffs *= pixelStride;
			
			byte [] src = new byte[cinfo];
			bb.get(src);
		
			System.arraycopy(src, 0, decompData, lineStart+coffs, cinfo);
			
			if( last ) return;
		}
	}

	
	@Override
	public String toString() {		
		return String.format("SingleSprite %s (has %s %s %s) %d.%d zoom %d",
				hasTransparency ? "Tile" : "NotTile",
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
	private void convertImageArray(byte[] image) {
		BufferedImage img = createBufferedImage(image, xSize, ySize);
		DisplayImage(img);
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
	    IndexColorModel defaultColorModel = new IndexColorModel(8, 256, r, g, b);
	    return defaultColorModel;
	}	
	
	
	public void DisplayImage(BufferedImage img)
    {
        ImageIcon icon=new ImageIcon(img);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(200,300);
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
			int code = bb.get() & 0xFF;
			
			boolean repeat = 0 != (code & 0x80);
			code &= 0x7F;
			
			if(!repeat)
			{
				final int len = code;
				byte [] copy = new byte[len];
				bb.get(copy);
				to.put(copy);
				continue;
			}

			// copy from old data

			int lofs = bb.get() & 0xFF;
			
			int length = code >> 3;
			int offset = ( (code & 7) << 8 ) | lofs;
			
			int toPos = to.position();
			to.position(toPos - offset);

			byte [] copy = new byte[length];
			to.get(copy);
			to.position(toPos);
			to.put(copy);
		}
	}
	
}

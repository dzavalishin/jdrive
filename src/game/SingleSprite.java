package game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

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
			Arrays.fill(decompData, (byte)0);
			
			decompressTile(decompData, bb);
		}
	
	}

	private void decompressTile(byte[] decompData, ByteBuffer bb) {
		boolean dwords = uncompressedSize >= 65536;

		int [] offsets = new int[ySize];
		
		int start = bb.position();
		
		for(int i = 0; i < ySize; i++ )
		{
			offsets[i] = dwords ? bb.getInt() : bb.getShort();
		}
		
		for(int i = 0; i < ySize; i++ )
		{
			bb.position(offsets[i] + start);
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

			byte [] src = new byte[cinfo];
			bb.get(src);
		
			System.arraycopy(src, 0, decompData, lineStart+coffs, cinfo);
			
			if( last ) return;
		}
	}

	
	@Override
	public String toString() {		
		return String.format("SingleSprite %s has %s %s %s %d.%d zoom %d",
				hasTransparency ? "Tile" : "NotTile",
				hasRGB? "RGB" : "",
				hasAlpha ? "Alpha" : "",
				hasPalette? "Palette" : "",
						xSize, ySize, zoomLevel
				);
	}
}

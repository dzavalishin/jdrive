package game.util;

/**
 * Smart pointer to the video memory
 * 
 * Maintains displacement from memory start.
 * 
 * @author dz
 *
 */

public class Pixel 
{
	private byte [] mem; // real mem 
	private int displ;		// current displacement
	
	public void madd( int add ) { displ += add; }
	public void shift( int add ) { displ += add; }
	
	public void write( int shift, byte b )
	{
		mem[ displ + shift ] = b;
	}
	
	/**
	 * Write a pixel to this video ptr
	 * @param shift from current ptr pos
	 * @param b data to write
	 */
	public void w( int shift, byte b )
	{
		write(shift,b);
	}
	
	public byte read( int shift )
	{
		return mem[ displ + shift ];
	}
	
	public byte r( int shift )
	{
		return read( shift );
	}
	
	public Pixel( byte [] start ) {
		mem = start;
		displ = 0;
	}

	public Pixel( byte [] start, int pos ) {
		mem = start;
		displ = pos;
	}

	public Pixel( Pixel p ) {
		mem = p.mem;
		displ = p.displ;
	}
	
	public Pixel(Pixel p, int shift) {
		mem = p.mem;
		displ = p.displ + shift;
	}
	
	public void wor(int shift, int b) 
	{
		mem[ displ + shift ] |= b;	
	}
	
	public void wand(int shift, int b) 
	{
		mem[ displ + shift ] &= b;	
	}
	
	

	
	
	public void WRITE_PIXELS(int val)
	{
		w(0, (byte) BitOps.GB(val,  0, 8) );
		w(1, (byte) BitOps.GB(val,  8, 8) );
		w(2, (byte) BitOps.GB(val, 16, 8) );
		w(3, (byte) BitOps.GB(val, 24, 8) );
	}

	/* need to use OR, otherwise we will overwrite the wrong pixels at the edges :( */
	public void WRITE_PIXELS_OR(int val)
	{
		wor(0, BitOps.GB(val,  0, 8) );
		wor(1, BitOps.GB(val,  8, 8) );
		wor(2, BitOps.GB(val, 16, 8) );
		wor(3, BitOps.GB(val, 24, 8) );
	}

	public boolean inside(Pixel from, Pixel to) {
		assert( mem == from.mem );
		assert( mem == to.mem );		
		
		return displ >= from.displ && displ <= to.displ;
	}
	
	public void memset(byte color, int right) {
		for( int i = displ; i < displ+right; i++ )
			mem[i] = color;		
	}
	
	
	public byte[] getMem() 			{		return mem;		}
	public int getDisplacement() 	{		return displ;	}

	public void copyFrom(Pixel src, int num) 
	{
		System.arraycopy(src.mem, src.displ, mem, displ, num);		
	}
	
	
	
}

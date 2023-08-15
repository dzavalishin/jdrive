package com.dzavalishin.util;

/**
 * Smart pointer to the video memory. Mostly identical to ByteArrayPtr.  
 * 
 * @author dz
 *
 */

public class Pixel extends ByteArrayPtr
{
	public Pixel( byte [] start ) {
		super(start);
	}	

	public Pixel( byte [] start, int pos ) {
		super( start, pos );
	}	

	public Pixel( ByteArrayPtr p ) {
		super(p);
	}	

	public Pixel(ByteArrayPtr p, int shift) {
		super(p, shift);
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

	
}

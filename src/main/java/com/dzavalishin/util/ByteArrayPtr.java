package com.dzavalishin.util;

/**
 * Smart pointer to byte array.
 * 
 * Maintains displacement from memory start.
 * 
 * @author dz
 *
 */

public class ByteArrayPtr implements IArrayPtr 
{

	private final byte []  mem;		// real mem
	private int            displ;	// current displacement
	
	// --------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------
	
	public ByteArrayPtr( byte [] start ) {
		mem = start;
		displ = 0;
	}

	public ByteArrayPtr( byte [] start, int pos ) {
		mem = start;
		displ = pos;
	}

	public ByteArrayPtr( ByteArrayPtr p ) {
		mem = p.mem;
		displ = p.displ;
	}
	
	public ByteArrayPtr( ByteArrayPtr p, int shift ) {
		mem = p.mem;
		displ = p.displ + shift;
	}
	
	// --------------------------------------------------------------
	// Read/write
	// --------------------------------------------------------------
	
	
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
	
	/**
	 * Read unsigned byte
	 * @param shift position to read from
	 * @return Integer 0..255
	 */
	public int ur( int shift )
	{
		return 0xFF & read( shift );
	}

	
	
	public void wor(int shift, int b) 
	{
		mem[ displ + shift ] |= b;	
	}
	
	public void wand(int shift, int b) 
	{
		mem[ displ + shift ] &= b;	
	}

	/** 
	 * Read and increment pointer
	 * @return <b>signed</b> byte
	 */
	public byte rpp() {
		byte v = r(0);
		shift(1);
		return v;
	}
	
	/** 
	 * Read and increment pointer
	 * @return <b>unsigned</b> byte
	 */
	public int urpp() {
		int v = 0xFF & r(0);
		shift(1);
		return v;
	}

	/**
	 * Write and increment pointer.
	 * @param b Byte to write.
	 */
	public void wpp(byte b) {
		w(0, b);
		shift(1);
		
	}

	// --------------------------------------------------------------
	// Special write
	// --------------------------------------------------------------

	public void copyFrom(ByteArrayPtr src, int num) 
	{
		System.arraycopy(src.mem, src.displ, mem, displ, num);		
	}
	
	public void memset(byte color, int right) {
		for( int i = displ; i < displ+right; i++ )
			mem[i] = color;		
	}


	
	// --------------------------------------------------------------
	// Move position
	// --------------------------------------------------------------

	public void madd( int add ) { displ += add; }
	public void shift( int add ) { displ += add; }
	
	/**
	 * Increment pointer
	 */
	@Override
	public void inc() {
		shift(1);		
	}

	/**
	 * Decrement pointer
	 */
	@Override
	public void dec() {
		shift(-1);		
	}

	// --------------------------------------------------------------
	// Getters
	// --------------------------------------------------------------
	
	public byte[] getMem() 			{		return mem;		}
	
	@Override
	public int getDisplacement() 	{		return displ;	}

	@Override
	public int getPos() 			{		return displ;	}

	// --------------------------------------------------------------
	// Setters
	// --------------------------------------------------------------
	
	@Override
	public void setPos(int i) { displ = i; }
	

	// --------------------------------------------------------------
	// State
	// --------------------------------------------------------------
	
	@Override
	public boolean hasCurrent() {			
		return displ >= 0 && displ < mem.length;
	}

	public int hasBytesLeft() {
		return mem.length-displ;
	}

	public boolean inside(ByteArrayPtr from, ByteArrayPtr to) {
		assert( mem == from.mem );
		assert( mem == to.mem );		
		
		//return displ >= from.displ && displ <= to.displ;
		return displ >= from.displ && displ < to.displ;
	}
	
	
	
	
}
